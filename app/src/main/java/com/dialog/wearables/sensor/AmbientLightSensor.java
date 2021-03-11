/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor;

public abstract class AmbientLightSensor extends IotSensor {

    protected float ambientLight;
    protected boolean lowVoltage;

    public float getAmbientLight() {
        return ambientLight;
    }

    public boolean isLowVoltage() {
        return lowVoltage;
    }

    public static final String LOG_TAG = "AMB";
    public static final String LOG_UNIT = " lux";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }
}
