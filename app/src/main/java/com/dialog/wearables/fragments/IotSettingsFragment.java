/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dialog.wearables.activities.MenuHolderActivity;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.settings.IotSettingsManager;

public abstract class IotSettingsFragment extends PreferenceFragment {
    private static final String TAG = "IotSettingsFragment";

    protected IotSensorsDevice device;
    protected IotSettingsManager settingsManager;
    protected BroadcastReceiver configurationReportReceiver;

    protected abstract String getLogTag();
    protected abstract int getSettingsXml();
    protected abstract IotSettingsManager getSettingsManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        device = ((MenuHolderActivity) getActivity()).getDevice();
        addPreferencesFromResource(getSettingsXml());
        settingsManager = getSettingsManager();

        configurationReportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int command = intent.getIntExtra("command", -1);
                byte[] data = intent.getByteArrayExtra("data");
                Log.d(getLogTag(), "Configuration report: " + command);
                switch (command) {
                    case UUIDS.WEARABLES_COMMAND_CONFIGURATION_START:
                    case UUIDS.WEARABLES_COMMAND_CONFIGURATION_STOP:
                    case UUIDS.WEARABLES_COMMAND_CONFIGURATION_RUNNING_STATE:
                        settingsManager.setIsStarted(device.isStarted);
                        break;
                    default:
                        settingsManager.processConfigurationReport(command, data);
                        break;
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(configurationReportReceiver,
                new IntentFilter(BroadcastUpdate.CONFIGURATION_REPORT));

        setStatus(true);
    }

    public void setStatus(boolean status) {
        if (settingsManager == null)
            return;

        if (status) {
            settingsManager.register();
            settingsManager.readConfiguration();
        } else {
            settingsManager.unregister();
        }
    }

    @Override
    public void onDestroy() {
        settingsManager.unregister();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(configurationReportReceiver);
        super.onDestroy();
    }
}
