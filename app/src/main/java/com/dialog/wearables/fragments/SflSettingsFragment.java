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

import com.dialog.wearables.settings.IotSettingsManager;

public class SflSettingsFragment extends IotSettingsFragment {
    private static final String TAG = "SflSettingsFragment";

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getSettingsXml() {
        return device.getSensorFusionSettingsXml();
    }

    @Override
    protected IotSettingsManager getSettingsManager() {
        return device.getSensorFusionSettingsManager(this);
    }
}
