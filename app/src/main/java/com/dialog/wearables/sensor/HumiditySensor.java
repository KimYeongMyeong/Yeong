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

public abstract class HumiditySensor extends IotSensor {

    protected float humidity;

    public float getHumidity() {
        return humidity;
    }

    public static final String LOG_TAG = "HMD";
    public static final String LOG_UNIT = "%";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }
}
