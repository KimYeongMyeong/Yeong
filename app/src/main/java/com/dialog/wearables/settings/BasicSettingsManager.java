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

import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.device.settings.IotDeviceSettings;
import com.dialog.wearables.global.IotSensorsLogger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Settings manager for the basic settings fragment
 */
public class BasicSettingsManager extends IotSettingsManager {
    private static final String TAG = "BasicSettingsManager";

    private IotSensorsApplication application;
    private IotDeviceSettings settings;
    private HashMap<String, Preference> preferences;
    private Preference reset, readConfNv, writeConf;
    private boolean preferencesEnabled = false;
    private boolean loadingSettings = false;

    private static final String[] rawDisabledInputs = new String[] {
            "prefSensorFusionRate",
            "prefSensorFusionRawEnabled",
    };

    public BasicSettingsManager(IotSensorsDevice device, PreferenceFragment context) {
        super(device, context);
        application = IotSensorsApplication.getApplication();

        settings = device.getBasicSettings();
        preferences = new HashMap<>(settings.getPrefKeys().length);
        for (String key : settings.getPrefKeys()) {
            preferences.put(key, context.findPreference(key));
        }
        if (settings.valid())
            updatePreferences();

        context.findPreference("prefFirmwareVersion").setSummary(device.version);

        reset = context.findPreference("pref_reset_to_defaults");
        readConfNv = context.findPreference("pref_read_conf_from_nv");
        writeConf = context.findPreference("pref_write_conf_to_nv");
        reset.setOnPreferenceClickListener(this);
        readConfNv.setOnPreferenceClickListener(this);
        writeConf.setOnPreferenceClickListener(this);

        setIsStarted(device.isStarted);

        // Disable first 5 items for gyro-rate if we have SFL enabled.
        if (device.sflEnabled) {
            ListPreference preference = (ListPreference) preferences.get("prefGyroscopeRate");
            preference.setEntries(Arrays.copyOfRange(preference.getEntries(), 5, preference.getEntries().length));
            preference.setEntryValues(Arrays.copyOfRange(preference.getEntryValues(), 5, preference.getEntryValues().length));
        }

        if (!device.sflEnabled) {
            for (String key : rawDisabledInputs) {
                preferences.get(key).setSummary("Not supported in this mode");
            }
        }
    }

    /**
     * Sends a read command to the bluetooth manager that will trigger a callback with the values
     */
    @Override
    public void readConfiguration() {
        device.manager.sendReadConfigCommand();
    }

    /**
     * Process the incoming notification from the intent
     *
     * @param data Byte buffer with the settings
     */
    @Override
    public void processConfigurationReport(int command, byte[] data) {
        if (command != UUIDS.WEARABLES_COMMAND_CONFIGURATION_READ)
            return;
        updatePreferences();
        updateSensorFusionDependencies();
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
        writeConf.setEnabled(!started);
        readConfNv.setEnabled(!started);
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
        updateSensorFusionDependencies();
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
        if (preferences.containsKey(key)) {
            settings.load(sharedPreferences);
            byte[] data = settings.pack();
            if (settings.modified()) {
                device.manager.sendWriteConfigCommand(data);
                device.manager.sendReadConfigCommand();
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
            if (p instanceof ListPreference)
                ((ListPreference)p).setValue(sharedPreferences.getString(key, null));
        }
        loadingSettings = false;
    }

    private void updateSensorFusionDependencies() {
        if (device.sflEnabled) {
            boolean sflEnabled = device.type == IotSensorsDevice.TYPE_IOT_580 || sharedPreferences.getBoolean("prefSensorFusionEnabled", true);

            Preference prefMagnetometerRate = preferences.get("prefMagnetometerRate");
            prefMagnetometerRate.setEnabled(!device.isStarted && !sflEnabled);
            prefMagnetometerRate.setSummary(sflEnabled ? "Not supported in this mode" : "%s");

            ListPreference prefSensorCombination = (ListPreference) preferences.get("prefSensorCombination");
            prefSensorCombination.setTitle(sflEnabled ? R.string.label_sensor_fusion_combination : R.string.label_imu_sensor_combination);
            prefSensorCombination.setDialogTitle(sflEnabled ? R.string.label_sensor_fusion_combination : R.string.label_imu_sensor_combination);
            prefSensorCombination.setEntries(sflEnabled ? R.array.sensor_fusion_combination_labels : R.array.imu_sensor_combination_labels);
            prefSensorCombination.setEntryValues(sflEnabled ? R.array.sensor_fusion_combination_values : R.array.imu_sensor_combination_values);

            if (!device.isStarted) {
                preferences.get("prefSensorFusionRate").setEnabled(sflEnabled);
                preferences.get("prefSensorFusionRawEnabled").setEnabled(sflEnabled);
            }
        } else {
            if (!device.isStarted) {
                for (String key : rawDisabledInputs) {
                    preferences.get(key).setEnabled(false);
                }
            }
        }
    }
}
