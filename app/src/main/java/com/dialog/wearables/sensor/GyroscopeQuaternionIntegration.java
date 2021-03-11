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

public class GyroscopeQuaternionIntegration extends IotSensor {
    private static final String TAG = "GyroscopeQuaternionIntegration";

    protected int[] raw;
    protected float qx, qy, qz, qw;
    protected float integrationRate;
    protected float sensorRate;
    protected int samples;

    public void setRate(float integrationRate, float sensorRate) {
        this.integrationRate = integrationRate;
        this.sensorRate = sensorRate;
        samples = (int) (sensorRate / integrationRate);
    }

    @Override
    public Value processRawData(byte[] data, int offset) {
        raw = get4DValuesLE(data, offset);
        qx = raw[0] / 32768.f;
        qy = raw[1] / 32768.f;
        qz = raw[2] / 32768.f;
        qw = raw[3] / 32768.f;
        value = new Value4D(qx, qy, qz, qw);
        return value;
    }

    public static final String LOG_TAG = "GDQ";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }
}
