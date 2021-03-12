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

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;

import com.dialog.wearables.R;
import com.dialog.wearables.apis.internal.ConfigurationMsg;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

public class CloudSettingsFragment extends PreferenceFragment {
    private static final String TAG = CloudSettingsFragment.class.getSimpleName();

    private CloudSettingsManager settingsManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.cloud_settings);
        settingsManager = new CloudSettingsManager(this) ;

        setStatus(true);
    }

    public void setStatus(boolean status) {
        if (settingsManager == null)
            return;

        if (status) {
            settingsManager.register();
        } else {
            settingsManager.unregister();
        }
    }

    @Override
    public void onStop(){
        // TODO: 23-May-18 for now
        Intent intent = new Intent();
        intent.setAction(BroadcastUpdate.IOT_APPS_SETTINGS_UPDATE);
        intent.putExtra("state", new ConfigurationMsg(settingsManager.getDisabledIoTApps()));
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        setStatus(false);
        super.onDestroy();
    }
}
