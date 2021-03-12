/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.device.settings;

import com.dialog.wearables.device.IotSensorsDevice;

import java.util.List;

public abstract class CalibrationSettings extends IotDeviceSettings {

    public CalibrationSettings(IotSensorsDevice device) {
        super(device);
    }

    protected int calMode;
    protected int calAutoMode;

    public void setCalibrationMode(int calMode, int calAutoMode) {
        this.calMode = calMode;
        this.calAutoMode = calAutoMode;
    }

    public abstract List<String> getSettingsForCalibrationMode();
}
