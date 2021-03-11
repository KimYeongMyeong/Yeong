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

public class AccelerometerIntegration extends IotSensor {
    private static final String TAG = "AccelerometerIntegration";

    private static float G = 9.81f;

    protected int qformat;
    protected int[] raw;
    protected float x, y, z;
    protected float integrationRate;
    protected float sensorRate;
    protected int samples;
    protected float avgDeltaX, avgDeltaY, avgDeltaZ;
    protected Value avgDelta = new Value(0);
    protected float accX, accY, accZ;
    protected Value acceleration;
    protected Value accelerationInG;

    public Value getAverageDelta() {
        return avgDelta;
    }

    public Value getAcceleration() {
        return acceleration;
    }

    public Value getAccelerationInG() {
        return accelerationInG;
    }

    public void setRate(float integrationRate, float sensorRate) {
        this.integrationRate = integrationRate;
        this.sensorRate = sensorRate;
        samples = (int) (sensorRate / integrationRate);
    }

    @Override
    public Value processRawData(byte[] data, int offset) {
        qformat = data[offset];
        raw = get3DValuesLE(data, offset + 1);

        float q = (float) Math.pow(2, qformat);
        x = raw[0] / q;
        y = raw[1] / q;
        z = raw[2] / q;
        value = new Value3D(x, y, z);

        if (samples != 0) {
            avgDeltaX = x / samples;
            avgDeltaY = y / samples;
            avgDeltaZ = z / samples;
            avgDelta = new Value3D(avgDeltaX, avgDeltaY, avgDeltaZ);
        }

        accX = x * integrationRate;
        accY = y * integrationRate;
        accZ = z * integrationRate;
        acceleration = new Value3D(accX, accY, accZ);
        accelerationInG = new Value3D(accX / G, accY / G, accZ / G);

        return value;
    }

    public static final String LOG_TAG = "ADV";
    public static final String LOG_UNIT = "m/s";

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
        return accelerationInG;
    }

    @Override
    protected Value getUnprocessedGraphValue() {
        return accelerationInG;
    }
}
