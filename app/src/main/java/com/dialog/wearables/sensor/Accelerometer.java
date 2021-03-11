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

import android.util.Log;

public abstract class Accelerometer extends IotSensor {
    private static final String TAG = "Accelerometer";

    protected float x, y, z;
    protected float sensitivity;

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
        Log.d(TAG, "Sensitivity: " + sensitivity);
    }

    protected Value processRawData(int[] raw) {
        x = raw[0] / sensitivity;
        y = raw[1] / sensitivity;
        z = raw[2] / sensitivity;
        value = new Value3D(x, y, z);
        return value;
    }

    public static final String LOG_TAG = "ACC";
    public static final String LOG_UNIT = "g";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }
}
