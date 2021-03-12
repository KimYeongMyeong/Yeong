/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.settings;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.device.settings.IotDeviceSettings;
import com.dialog.wearables.device.settings.RangeSeekBarPreference;
import com.dialog.wearables.global.IotSensorsLogger;

import java.util.HashMap;
import java.util.Objects;

/**
 * Settings manager for the basic settings fragment
 */
public class BasicSettingsManagerIotPlus extends IotSettingsManager {
    private static final String TAG = "BasicSettingsManager";

    private IotSensorsApplication application;
    private IotDeviceSettings basicSettings, calibrationSettings, proximitySettings;
    private HashMap<String, Preference> basicPreferences, calibrationPreferences;
    private Preference reset, readConfNv, writeConfNv, proximityCalibration;
    private AlertDialog proximityCalibrationDialog;
    private RangeSeekBarPreference proximityPreference;
    private boolean preferencesEnabled = false;
    private boolean loadingSettings = false;

    public BasicSettingsManagerIotPlus(IotSensorsDevice device, PreferenceFragment context) {
        super(device, context);
        application = IotSensorsApplication.getApplication();

        basicSettings = device.getBasicSettings();
        basicPreferences = new HashMap<>(basicSettings.getPrefKeys().length);
        for (String key : basicSettings.getPrefKeys()) {
            basicPreferences.put(key, context.findPreference(key));
        }
        if (basicSettings.valid())
            updatePreferences(basicSettings, basicPreferences);

        calibrationSettings = device.getCalibrationModesSettings();
        calibrationPreferences = new HashMap<>(calibrationSettings.getPrefKeys().length);
        for (String key : calibrationSettings.getPrefKeys()) {
            calibrationPreferences.put(key, context.findPreference(key));
        }
        if (calibrationSettings.valid())
            updatePreferences(calibrationSettings, calibrationPreferences);

        proximitySettings = device.getProximityHysteresisSettings();
        proximityPreference = (RangeSeekBarPreference) context.findPreference("prefProximityHysteresis");
        if (proximitySettings.valid())
            updatePreferences(proximitySettings, null);

        PreferenceCategory settingsCategory = (PreferenceCategory) context.findPreference("prefSettingsCat");
        if (!device.getFeatures().hasGasSensor()) {
            settingsCategory.removePreference(basicPreferences.get("prefGasSensorEnabled"));
        }
        if (device.getFeatures().hasIntegrationEngine()) {
            settingsCategory.removePreference(basicPreferences.get("prefSensorFusionEnabled"));
            settingsCategory.removePreference(basicPreferences.get("prefSensorFusionRawEnabled"));
        } else {
            settingsCategory.removePreference(basicPreferences.get("prefOperationMode"));
        }

        proximityCalibration = context.findPreference("prefProximityCalibration");
        if (!device.getFeatures().hasProximityCalibration()) {
            settingsCategory.removePreference(proximityCalibration);
        } else {
            proximityCalibration.setOnPreferenceClickListener(this);
        }

        context.findPreference("prefFirmwareVersion").setSummary(device.version);

        reset = context.findPreference("pref_reset_to_defaults");
        readConfNv = context.findPreference("pref_read_conf_from_nv");
        writeConfNv = context.findPreference("pref_write_conf_to_nv");
        reset.setOnPreferenceClickListener(this);
        readConfNv.setOnPreferenceClickListener(this);
        writeConfNv.setOnPreferenceClickListener(this);

        setIsStarted(device.isStarted);
    }

    /**
     * Sends a read command to the bluetooth manager that will trigger a callback with the values
     */
    @Override
    public void readConfiguration() {
        device.manager.sendReadConfigCommand();
        device.manager.sendReadCalibrationModesCommand();
        device.manager.sendReadProximityHysteresisCommand();
    }

    /**
     * Process the incoming notification from the intent
     *
     * @param data Byte buffer with the settings
     */
    @Override
    public void processConfigurationReport(int command, byte[] data) {
        switch (command) {
            case UUIDS.WEARABLES_COMMAND_CONFIGURATION_READ:
                updatePreferences(basicSettings, basicPreferences);
                updateDependencies();
                break;
            case UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_MODES:
                updatePreferences(calibrationSettings, calibrationPreferences);
                break;
            case UUIDS.WEARABLES_COMMAND_PROXIMITY_HYSTERESIS_READ:
                updatePreferences(proximitySettings, null);
                break;
            case UUIDS.WEARABLES_COMMAND_PROXIMITY_CALIBRATION:
                switch (data[0]) {
                    case 1:
                        Toast.makeText(context.getActivity(), R.string.proximity_calibration_started, Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        device.manager.sendReadProximityHysteresisCommand();
                        Toast.makeText(context.getActivity(), R.string.proximity_calibration_complete, Toast.LENGTH_SHORT).show();
                        if (proximityCalibrationDialog != null)
                            proximityCalibrationDialog.dismiss();
                        break;
                }
                break;
        }
    }

    /**
     * Control the visibility of the preferences
     *
     * @param started Is the sensor started?
     */
    @Override
    public void setIsStarted(boolean started) {
        Log.d(TAG, "setIsStarted " + started);
        reset.setEnabled(!started);
        writeConfNv.setEnabled(!started);
        readConfNv.setEnabled(!started);
        proximityPreference.setEnabled(!started);
        if (device.getFeatures().hasProximityCalibration())
            proximityCalibration.setEnabled(!started);
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
            for (Preference pref : basicPreferences.values()) {
                if (pref != null) {
                    pref.setEnabled(enabled);
                }
            }
            for (Preference pref : calibrationPreferences.values()) {
                if (pref != null) {
                    pref.setEnabled(enabled);
                }
            }
        }
        updateDependencies();
    }

    /**
     * When the settings on the fragment change, update the internal byte array
     *
     * @param sharedPreferences the shared preferences
     * @param key               The key that has been changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Objects.equals(key, IotSensorsLogger.LOG_ENABLED_PREF_KEY)) {
            application.logger.setEnabled(sharedPreferences.getBoolean(IotSensorsLogger.LOG_ENABLED_PREF_KEY, false));
            return;
        }
        if (Objects.equals(key, "prefTemperatureUnit")) {
            int unit = Integer.parseInt(sharedPreferences.getString(key, "0"));
            device.getTemperatureSensor().setDisplayUnit(unit);
            device.getTemperatureSensor().setLogUnit(unit);
            return;
        }
        if (loadingSettings)
            return;
        if (basicPreferences.containsKey(key)) {
            basicSettings.load(sharedPreferences);
            byte[] data = basicSettings.pack();
            if (basicSettings.modified()) {
                device.manager.sendWriteConfigCommand(data);
                device.manager.sendReadConfigCommand();
                showToast(R.string.settings_saved);
            }
        }
        if (calibrationPreferences.containsKey(key)) {
            calibrationSettings.load(sharedPreferences);
            byte[] data = calibrationSettings.pack();
            if (calibrationSettings.modified()) {
                device.manager.sendWriteCalibrationModesCommand(data);
                device.manager.sendReadCalibrationModesCommand();
                showToast(R.string.settings_saved);
            }
        }
        if ("prefProximityHysteresis".equals(key)) {
            proximitySettings.load(sharedPreferences);
            byte[] data = proximitySettings.pack();
            if (proximitySettings.modified()) {
                device.manager.sendWriteProximityHysteresisCommand(data);
                device.manager.sendReadProximityHysteresisCommand();
                showToast(R.string.settings_saved);
            }
        }
    }

    /**
     * Handles the button presses
     *
     * @param preference preference that is being pressed
     * @return returns false when no preference is matched
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pref_reset_to_defaults")) {
            device.manager.sendResetToDefaultsCommand();
            readConfiguration();
        } else if (preference.getKey().equals("pref_read_conf_from_nv")) {
            device.manager.sendReadNvCommand();
            readConfiguration();
        } else if (preference.getKey().equals("pref_write_conf_to_nv")) {
            device.manager.sendWriteConfigToNvCommand();
        } else if (preference.getKey().equals("prefProximityCalibration")) {
            showProximityCalibrationDialog();
        }
        return false;
    }

    private void showProximityCalibrationDialog() {
        proximityCalibrationDialog = new AlertDialog.Builder(context.getActivity())
                .setTitle(R.string.proximity_calibration_dialog_title)
                .setMessage(R.string.proximity_calibration_dialog_message)
                .setPositiveButton(R.string.proximity_calibration_start, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        proximityCalibrationDialog.show();
        proximityCalibrationDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                device.manager.sendProximityCalibrationCommand();
                proximityCalibrationDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
            }
        });
    }

    private void updatePreferences(IotDeviceSettings settings, HashMap<String, Preference> preferences) {
        loadingSettings = true;
        settings.save(sharedPreferences);
        if (preferences == null) {
            proximityPreference.setValue(sharedPreferences.getString("prefProximityHysteresis", null));
            loadingSettings = false;
            return;
        }
        for (String key : preferences.keySet()) {
            Preference p = preferences.get(key);
            if (p == null)
                continue;
            if (p instanceof SwitchPreference)
                ((SwitchPreference)p).setChecked(sharedPreferences.getBoolean(key, false));
            if (p instanceof ListPreference)
                ((ListPreference)p).setValue(sharedPreferences.getString(key, null));
        }
        loadingSettings = false;
    }

    private void updateDependencies() {
        boolean sflEnabled = sharedPreferences.getBoolean("prefSensorFusionEnabled", true);
        boolean integrationEngine = device.integrationEngine;

        Preference prefMagnetometerRate = basicPreferences.get("prefMagnetometerRate");
        prefMagnetometerRate.setEnabled(!device.isStarted && !sflEnabled && !integrationEngine);
        prefMagnetometerRate.setSummary(sflEnabled || integrationEngine ? "Not supported in this mode" : "%s");

        ListPreference prefSensorCombination = (ListPreference) basicPreferences.get("prefSensorCombination");
        prefSensorCombination.setTitle(sflEnabled ? R.string.label_sensor_fusion_combination : R.string.label_imu_sensor_combination);
        prefSensorCombination.setDialogTitle(sflEnabled ? R.string.label_sensor_fusion_combination : R.string.label_imu_sensor_combination);
        prefSensorCombination.setEntries(sflEnabled ? R.array.sensor_fusion_combination_labels : R.array.imu_sensor_combination_labels_585);
        prefSensorCombination.setEntryValues(sflEnabled ? R.array.sensor_fusion_combination_values : R.array.imu_sensor_combination_values_585);

        ListPreference prefAccelerometerRate = (ListPreference) basicPreferences.get("prefAccelerometerRate");
        prefAccelerometerRate.setEntries(sflEnabled || integrationEngine ? R.array.accelerometer_rate_labels_585 : R.array.accelerometer_rate_labels_585_sfl_disabled);
        prefAccelerometerRate.setEntryValues(sflEnabled || integrationEngine ? R.array.accelerometer_rate_values_585 : R.array.accelerometer_rate_values_585_sfl_disabled);

        ListPreference prefGyroscopeRate = (ListPreference) basicPreferences.get("prefGyroscopeRate");
        prefGyroscopeRate.setEntries(sflEnabled || integrationEngine ? R.array.gyroscope_rate_labels_585 : R.array.gyroscope_rate_labels_585_sfl_disabled);
        prefGyroscopeRate.setEntryValues(sflEnabled || integrationEngine ? R.array.gyroscope_rate_values_585 : R.array.gyroscope_rate_values_585_sfl_disabled);

        if (device.getFeatures().hasIntegrationEngine()) {
            ListPreference prefSensorFusionRate = (ListPreference) basicPreferences.get("prefSensorFusionRate");
            int title = sflEnabled || !integrationEngine ? R.string.label_sensor_fusion_rate : R.string.label_integration_engine_rate;
            prefSensorFusionRate.setTitle(title);
            prefSensorFusionRate.setDialogTitle(title);
        }

        if (!device.isStarted) {
            basicPreferences.get("prefSensorFusionRate").setEnabled(sflEnabled || integrationEngine);
            if (!device.getFeatures().hasIntegrationEngine())
                basicPreferences.get("prefSensorFusionRawEnabled").setEnabled(sflEnabled);
        }

        boolean gasEnabled = device.getFeatures().hasGasSensor() && sharedPreferences.getBoolean("prefGasSensorEnabled", false);
        basicPreferences.get("prefEnvironmentalRate").setEnabled(!device.isStarted && !gasEnabled);
    }
}
