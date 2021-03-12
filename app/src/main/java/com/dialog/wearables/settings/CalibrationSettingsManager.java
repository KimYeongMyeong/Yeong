/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.settings;

import android.Manifest;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import com.dialog.wearables.R;
import com.dialog.wearables.activities.MenuHolderActivity;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.device.settings.CalibrationSettings;
import com.dialog.wearables.device.settings.IotDeviceSettings;
import com.dialog.wearables.fragments.DialogListFragment;

import org.ini4j.Ini;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Manager for the calibration settings fragment
 */
public class CalibrationSettingsManager extends IotSettingsManager {
    private static final String TAG = "CalibrationSettingsMan";

    private CalibrationSettings settings;
    private HashMap<String, Preference> preferences;
    private Preference loadVecMat, saveVecMat, storeNv, reset;
    private ListPreference currentMode;
    private boolean preferencesEnabled = false;
    private boolean loadingSettings = false;
    private int calMode = 0, calAutoMode = 0;

    public CalibrationSettingsManager(IotSensorsDevice device, PreferenceFragment context) {
        super(device, context);

        settings = device.getCalibrationSettings();
        preferences = new HashMap<>(settings.getPrefKeys().length);
        for (String key : settings.getPrefKeys()) {
            preferences.put(key, context.findPreference(key));
        }
        if (settings.valid())
            updatePreferences();

        currentMode = (ListPreference) context.findPreference("calCurrentCalibration");

        loadVecMat = context.findPreference("calLoadVectorMatrix");
        saveVecMat = context.findPreference("calSaveVectorMatrix");
        storeNv = context.findPreference("calStoreConfigNV");
        reset = context.findPreference("calResetCurrSet");
        loadVecMat.setOnPreferenceClickListener(this);
        saveVecMat.setOnPreferenceClickListener(this);
        reset.setOnPreferenceClickListener(this);
        storeNv.setOnPreferenceClickListener(this);

        initCalibrationMode();
        setIsStarted(device.isStarted);
    }

    /**
     * Sends a read command to the bluetooth manager that will trigger a callback with the settings
     */
    @Override
    public void readConfiguration() {
        if (device.type != IotSensorsDevice.TYPE_IOT_585)
            device.manager.sendReadConfigCommand();
        else
            device.manager.sendReadCalibrationModesCommand();
        device.manager.sendCalReadCommand();
    }

    /**
     * Sends a read command to the bluetooth manager that will trigger a callback with calibration settings
     */
    public void readCalibrationRam() {
        device.manager.sendCalCoeffReadCommand();
    }

    /**
     * Process the incoming notification from the intent
     *
     * @param data Byte buffer with the settings
     */
    @Override
    public void processConfigurationReport(int command, byte[] data) {
        switch (command) {
            case UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_CONTROL:
                updatePreferences();
                updateModeDependencies();
                break;
            case UUIDS.WEARABLES_COMMAND_CONFIGURATION_READ:
            case UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_MODES:
                initCalibrationMode();
                updateModeDependencies();
                break;
            case UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_COEFFICIENTS:
                writeCoefficients(newCoefficientsFile(), new Coefficients(data));
                break;
        }
    }

    private void initCalibrationMode() {
        calMode = device.getCalibrationMode(UUIDS.SENSOR_TYPE_MAGNETOMETER);
        calAutoMode = device.getAutoCalibrationMode(UUIDS.SENSOR_TYPE_MAGNETOMETER);
        currentMode.setValue(Integer.toString(calMode * 2 + (calMode > UUIDS.CALIBRATION_MODE_STATIC ? calAutoMode : 0)));
        settings.setCalibrationMode(calMode, calAutoMode);
    }

    /**
     * Control the visibility of the preferences
     *
     * @param started boolean
     */
    @Override
    public void setIsStarted(boolean started) {
        Log.d(TAG, "setIsStarted " + started);
        loadVecMat.setEnabled(!started);
        reset.setEnabled(!started);
        saveVecMat.setEnabled(!started);
        storeNv.setEnabled(!started);
        setInputPreferencesEnabled(!started);
    }

    /**
     * Function to enable/disable the inputs (started/stopped)
     *
     * @param enabled enabled or not?
     */
    public void setInputPreferencesEnabled(boolean enabled) {
        if (preferencesEnabled != enabled) {
            preferencesEnabled = enabled;
            for (Preference pref : preferences.values()) {
                if (pref != null) {
                    pref.setEnabled(enabled);
                }
            }
        }
        updateModeDependencies();
    }

    /**
     * When the settings on the fragment change, update the internal byte array
     *
     * @param sharedPreferences the shared preferences
     * @param key               The key that has been changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("calStateOverlay"))
            return;
        if (loadingSettings)
            return;
        if (preferences.containsKey(key)) {
            settings.load(sharedPreferences);
            List<IotDeviceSettings.RangeError> rangeErrors = settings.checkRange();
            if (!rangeErrors.isEmpty()) {
                Toast.makeText(context.getActivity(), String.format("Out of range! [%d..%d]", rangeErrors.get(0).min, rangeErrors.get(0).max), Toast.LENGTH_SHORT).show();
                device.manager.sendCalReadCommand();
                return;
            }
            byte[] data = settings.pack();
            if (settings.modified()) {
                device.manager.sendCalWriteCommand(data);
                device.manager.sendCalReadCommand();
                showToast(R.string.settings_saved);
            }
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("calLoadVectorMatrix")) {
            if (((MenuHolderActivity)context.getActivity()).checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE))
                showFileSelectionDialog();
            else
                Toast.makeText(context.getActivity(), R.string.storage_access_denied, Toast.LENGTH_SHORT).show();
        } else if (preference.getKey().equals("calSaveVectorMatrix")) {
            if (((MenuHolderActivity)context.getActivity()).checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                readCalibrationRam();
            else
                Toast.makeText(context.getActivity(), R.string.storage_access_denied, Toast.LENGTH_SHORT).show();
        } else if (preference.getKey().equals("calStoreConfigNV")) {
            device.manager.sendCalStoreNvCommand();
        } else if (preference.getKey().equals("calResetCurrSet")) {
            device.manager.sendCalResetCommand();
            readConfiguration();
        }
        return false;
    }

    private void updatePreferences() {
        loadingSettings = true;
        settings.save(sharedPreferences);
        for (String key : preferences.keySet()) {
            Preference p = preferences.get(key);
            if (p == null)
                continue;
            if (p instanceof SwitchPreference)
                ((SwitchPreference)p).setChecked(sharedPreferences.getBoolean(key, false));
            if (p instanceof EditTextPreference) {
                ((EditTextPreference)p).setText(sharedPreferences.getString(key, null));
                p.setSummary(((EditTextPreference)p).getText());
            }
        }
        loadingSettings = false;
    }

    private void updateModeDependencies() {
        List<String> keys = settings.getSettingsForCalibrationMode();
        for (String key : preferences.keySet()) {
            Preference p = preferences.get(key);
            p.setEnabled(keys.contains(key) && !device.isStarted);
            /*if (p instanceof EditTextPreference && !keys.contains(key)) {
                String value = ((EditTextPreference)p).getText();
                p.setSummary((value != null ? value + " " : "") + "(not available in this mode)");
            }*/
        }
    }

    private static class Coefficients {

        static int LENGTH = 26;
        byte sensor;
        byte format;
        short[] offset = new short[3];
        short[][] matrix = new short[3][3];

        Coefficients() {
        }

        Coefficients(byte[] data) {
            unpack(data);
        }

        void unpack(byte[] data) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            sensor = buffer.get();
            format = buffer.get();
            for (int i = 0; i < offset.length; ++i)
                offset[i] = buffer.getShort();
            for (int i = 0; i < matrix.length; ++i)
                for (int j = 0; j < matrix[i].length; ++j)
                    matrix[i][j] = buffer.getShort();
        }

        byte[] pack() {
            ByteBuffer buffer = ByteBuffer.allocate(LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            buffer.put(sensor);
            buffer.put(format);
            for (int i = 0; i < offset.length; ++i)
                buffer.putShort(offset[i]);
            for (int i = 0; i < matrix.length; ++i)
                for (int j = 0; j < matrix[i].length; ++j)
                    buffer.putShort(matrix[i][j]);
            return buffer.array();
        }
    }

    private void showFileSelectionDialog() {
        FragmentManager manager = context.getActivity().getFragmentManager();
        DialogListFragment dialog = new DialogListFragment();
        dialog.passCalibrationSettingsManager(this);
        dialog.show(manager, "file selection dialog");
    }

    public void onFileSelection(File file) {
        Coefficients coefficients = readCoefficients(file);
        if (coefficients != null)
            device.manager.sendCalCoeffWriteCommand(coefficients.pack());
    }

    private File newCoefficientsFile() {
        SimpleDateFormat curFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss");
        String path = Environment.getExternalStorageDirectory().toString() + File.separator +
                "Dialog Semiconductor" + File.separator +
                "IoT Sensors" + File.separator +
                curFormat.format(new Date()) + "-calibration.ini";
        return new File(path);
    }

    private void writeCoefficients(File file, Coefficients coefficients) {
        Log.d(TAG, "Saving calibration coefficients to: " + file.getPath());
        try {
            if (file.exists() || file.createNewFile()) {
                Ini ini = new Ini(file);
                ini.put("magnetometer calibration", "sensor_type", coefficients.sensor);
                ini.put("magnetometer calibration", "q_format", coefficients.format);

                StringBuilder buffer = new StringBuilder();
                for (int i = 0; i < coefficients.offset.length; ++i)
                    buffer.append(coefficients.offset[i]).append(",");
                buffer.setLength(buffer.length() - 1); // remove last ","
                ini.put("magnetometer calibration", "offset_vector", buffer.toString());

                buffer.setLength(0);
                for (int i = 0; i < coefficients.matrix.length; ++i)
                    for (int j = 0; j < coefficients.matrix[i].length; ++j)
                        buffer.append(coefficients.matrix[i][j]).append(",");
                buffer.setLength(buffer.length() - 1); // remove last ","
                ini.put("magnetometer calibration", "matrix", buffer.toString());

                ini.store();
                Toast.makeText(context.getActivity(), "Configuration saved.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving calibration coefficients: " + e.getMessage());
        }

    }

    private Coefficients readCoefficients(File file) {
        Log.d(TAG, "Reading calibration coefficients from: " + file.getPath());
        try {
            Ini ini = new Ini(file);
            Log.d(TAG, ini.toString());
            Coefficients coefficients = new Coefficients();
            coefficients.sensor = Byte.valueOf(ini.get("magnetometer calibration", "sensor_type"));
            coefficients.format = Byte.valueOf(ini.get("magnetometer calibration", "q_format"));

            String[] offset = ini.get("magnetometer calibration", "offset_vector").split(",");
            for (int i = 0; i < coefficients.offset.length; ++i)
                coefficients.offset[i] = Short.valueOf(offset[i]);

            String[] matrix = ini.get("magnetometer calibration", "matrix").split(",");
            for (int i = 0; i < coefficients.matrix.length; ++i)
                for (int j = 0; j < coefficients.matrix[i].length; ++j)
                    coefficients.matrix[i][j] = Short.valueOf(matrix[i* 3 + j]);

            return coefficients;
        } catch (Exception e) {
            Log.e(TAG, "Error reading calibration coefficients: " + e.getMessage());
            return null;
        }
    }
}
