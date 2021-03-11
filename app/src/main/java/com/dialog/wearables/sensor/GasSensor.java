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

public abstract class GasSensor extends IotSensor {

    protected float reading;

    public float getReading() {
        return reading;
    }

    public static final String LOG_TAG = "GAS";
    public static final String LOG_UNIT = " Ohm";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }
}
