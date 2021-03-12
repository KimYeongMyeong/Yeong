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
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.dialog.wearables.R;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.device.settings.IotDeviceSettings;

import java.util.HashMap;
import java.util.List;

/**
 * Manager for the Sensor fusion library settings
 */
public class SflSettingsManager extends IotSettingsManager {
    private static final String TAG = "SflSettingsManager";

    private IotDeviceSettings settings;
    private HashMap<String, Preference> preferences;
    private EditTextPreference betaM, betaA;
    private Preference storeNv, reset;
    private boolean loadingSettings;

    public SflSettingsManager(IotSensorsDevice device, PreferenceFragment context) {
        super(device, context);

        betaA = (EditTextPreference) context.findPreference("sfl_beta_a");
        betaM = (EditTextPreference) context.findPreference("sfl_beta_m");

        settings = device.getSensorFusionSettings();
        preferences = new HashMap<>(settings.getPrefKeys().length);
        for (String key : settings.getPrefKeys()) {
            preferences.put(key, context.findPreference(key));
        }
        if (settings.valid())
            updatePreferences();

        storeNv = context.findPreference("calStoreConfigNV");
        reset = context.findPreference("calResetCurrSet");
        reset.setOnPreferenceClickListener(this);
        storeNv.setOnPreferenceClickListener(this);

        setIsStarted(device.isStarted);
    }

    /**
     * Sends a read command to the bluetooth manager that will trigger a callback with the values
     */
    @Override
    public void readConfiguration() {
        device.manager.sendSflReadCommand();
    }

    /**
     * Process the incoming notification from the intent
     *
     * @param data Byte buffer with the settings
     */
    @Override
    public void processConfigurationReport(int command, byte[] data) {
        if (command != UUIDS.WEARABLES_COMMAND_SFL_READ)
            return;
        updatePreferences();
    }

    /**
     * Control the visibility of the preferences
     *
     * @param started Are the sensors started?
     */
    @Override
    public void setIsStarted(boolean started) {
        Log.d(TAG, "setIsStarted " + started);
        betaA.setEnabled(!started);
        betaM.setEnabled(!started);
        storeNv.setEnabled(!started);
        reset.setEnabled(!started);
    }

    /**
     * When the settings on the fragment change, update the internal byte array
     *
     * @param sharedPreferences the shared preferences
     * @param key               The key that has been changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (loadingSettings)
            return;
        if (preferences.containsKey(key)) {
            settings.load(sharedPreferences);
            List<IotDeviceSettings.RangeError> rangeErrors = settings.checkRange();
            if (!rangeErrors.isEmpty()) {
                Toast.makeText(context.getActivity(), String.format("Out of range! [%d..%d]", rangeErrors.get(0).min, rangeErrors.get(0).max), Toast.LENGTH_SHORT).show();
                device.manager.sendSflReadCommand();
                return;
            }
            byte[] data = settings.pack();
            if (settings.modified()) {
                device.manager.sendSflWriteCommand(data);
                device.manager.sendSflReadCommand();
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
        if (preference.getKey().equals("calStoreConfigNV")) {
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
        betaA.setText(sharedPreferences.getString("sfl_beta_a", null));
        betaA.setSummary(betaA.getText());
        betaM.setText(sharedPreferences.getString("sfl_beta_m", null));
        betaM.setSummary(betaM.getText());
        loadingSettings = false;
    }
}
