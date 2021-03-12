/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor;

public abstract class PressureSensor extends IotSensor {

    protected float pressure;

    public float getPressure() {
        return pressure;
    }

    public static final String LOG_TAG = "PRS";
    public static final String LOG_UNIT = " Pa";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }
}
