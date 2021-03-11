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

public abstract class Gyroscope extends IotSensor {
    private static final String TAG = "Gyroscope";

    protected float x, y, z;
    protected float rotX, rotY, rotZ;
    protected float accRotX, accRotY, accRotZ;
    protected Value rotation = new Value(0);
    protected Value accumulatedRotation = new Value(0);
    protected float sensitivity;
    protected float rate;

    public Value getRotation() {
        return rotation;
    }

    public Value getAccumulatedRotation() {
        return accumulatedRotation;
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
        Log.d(TAG, "Sensitivity: " + sensitivity);
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
        Log.d(TAG, "Rate:" + rate);
    }

    protected Value processRawData(int[] raw) {
        x = raw[0] / sensitivity;
        y = raw[1] / sensitivity;
        z = raw[2] / sensitivity;
        value = new Value3D(x, y, z);

        rotX = x / rate;
        rotY = y / rate;
        rotZ = z / rate;
        rotation = new Value3D(rotX, rotY, rotZ);

        accRotX += rotX;
        accRotY += rotY;
        accRotZ += rotZ;
        if (accRotX > 360)
            accRotX -= 360;
        if (accRotX < 0)
            accRotX += 360;
        if (accRotY > 360)
            accRotY -= 360;
        if (accRotY < 0)
            accRotY += 360;
        if (accRotZ > 360)
            accRotZ -= 360;
        if (accRotZ < 0)
            accRotZ += 360;
        accumulatedRotation = new Value3D(accRotX, accRotY, accRotZ);

        return value;
    }

    public static final String LOG_TAG = "GYR";
    public static final String LOG_UNIT = "deg";

    @Override
    public Value getLogValue() {
        return rotation;
    }

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }

    @Override
    protected Value getCloudValue() {
        return accumulatedRotation;
    }
}
