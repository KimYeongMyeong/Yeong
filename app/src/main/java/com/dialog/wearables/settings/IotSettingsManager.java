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

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.dialog.wearables.device.IotSensorsDevice;

public abstract class IotSettingsManager implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    protected IotSensorsDevice device;
    protected PreferenceFragment context;
    protected PreferenceManager preferenceManager;
    protected SharedPreferences sharedPreferences;
    private Toast toast;

    public IotSettingsManager(IotSensorsDevice device, PreferenceFragment context) {
        this.device = device;
        this.context = context;
        preferenceManager = context.getPreferenceManager();
        sharedPreferences = preferenceManager.getSharedPreferences();
    }

    public abstract void setIsStarted(boolean isStarted);
    public abstract void readConfiguration();
    public abstract void processConfigurationReport(int command, byte[] data);

    public void register() {
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void unregister() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    protected void showToast(int resId) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context.getActivity(), resId, Toast.LENGTH_SHORT);
        toast.show();
    }
}
