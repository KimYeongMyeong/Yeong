/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.dialog.wearables.R;
import com.dialog.wearables.activities.MenuHolderActivity;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.defines.StatusUpdates;
import com.dialog.wearables.device.IotSensorsDevice;

import java.util.Objects;

public class InfoFragment extends PreferenceFragment {

    private BroadcastReceiver sensorDataReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_info);
        final PreferenceManager preferenceManager = getPreferenceManager();
        final IotSensorsDevice device = ((MenuHolderActivity) getActivity()).getDevice();

        sensorDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                if (intent.getIntExtra("status", -1) == StatusUpdates.STATUS_FEATURES_READ)
                    preferenceManager.findPreference("FirmwareVersion").setSummary(device.version);
            }
        };
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(sensorDataReceiver, new IntentFilter(BroadcastUpdate.SENSOR_DATA_UPDATE));

        if (Objects.equals(device.version, "Unknown"))
            device.manager.readFeatures();

        String version = "N/A";
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        preferenceManager.findPreference("FirmwareVersion").setSummary(!Objects.equals(device.version, "Unknown") ? device.version : "N/A");
        preferenceManager.findPreference("AppVersion").setSummary(version);
        preferenceManager.findPreference("InfoSendMail").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference pref) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("mailto:bluetooth.support@diasemi.com?subject=IoT Sensors application question"));
                getActivity().startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(sensorDataReceiver);
    }
}
