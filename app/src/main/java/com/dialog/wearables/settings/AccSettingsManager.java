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

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.dialog.wearables.R;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;

/**
 * Settings manager for the Accelerometer settings
 */
public class AccSettingsManager extends IotSettingsManager {
    private static final String TAG = "AccSettingsManager";

    private Preference start;

    public AccSettingsManager(IotSensorsDevice device, PreferenceFragment context) {
        super(device, context);
        start = context.findPreference("pref_start");
        start.setOnPreferenceClickListener(this);
        setIsStarted(device.isStarted);
    }

    @Override
    public void readConfiguration() {
    }

    /**
     * Process the incoming notification from the intent
     *
     * @param data Byte buffer with the settings
     */
    @Override
    public void processConfigurationReport(int command, byte[] data) {
        if (command != UUIDS.WEARABLES_COMMAND_CALIBRATION_ACCELEROMETER)
            return;
        switch (data[0]) {
            case 1:
                start.setEnabled(false);
                final Toast toast = Toast.makeText(context.getActivity(), R.string.acc_calibration_start, Toast.LENGTH_SHORT);
                toast.show();
                break;
            case 0:
                start.setEnabled(true);
                final Toast toast2 = Toast.makeText(context.getActivity(), R.string.acc_calibration_done, Toast.LENGTH_SHORT);
                toast2.show();
                break;
        }
    }

    @Override
    public void register() {
    }

    @Override
    public void unregister() {
    }

    /**
     * Control the visibility of the preferences
     *
     * @param started Is the sensor started?
     */
    @Override
    public void setIsStarted(boolean started) {
        Log.d(TAG, "setIsStarted " + started);
        start.setEnabled(!started);
    }

    /**
     * Handles the button presses
     *
     * @param preference preference that is being pressed
     * @return returns false when no preference is matched
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals("pref_start")) {
            device.manager.sendAccCalibrateCommand();
        }
        return false;
    }
}
