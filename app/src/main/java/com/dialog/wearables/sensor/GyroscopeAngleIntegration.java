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

public class GyroscopeAngleIntegration extends IotSensor {
    private static final String TAG = "GyroscopeAngleIntegration";

    protected int qformat;
    protected int[] raw;
    protected float x, y, z;
    protected float integrationRate;
    protected float sensorRate;
    protected int samples;
    protected float avgDeltaX, avgDeltaY, avgDeltaZ;
    protected Value avgDelta = new Value(0);
    protected float rotRateX, rotRateY, rotRateZ;
    protected float accRotX, accRotY, accRotZ;
    protected Value rotationRate;
    protected Value accumulatedRotation;

    public Value getAverageDelta() {
        return avgDelta;
    }

    public Value getRotationRate() {
        return rotationRate;
    }

    public Value getAccumulatedRotation() {
        return accumulatedRotation;
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

        rotRateX = x * integrationRate;
        rotRateY = y * integrationRate;
        rotRateZ = z * integrationRate;
        rotationRate = new Value3D(rotRateX, rotRateY, rotRateZ);

        accRotX += x;
        accRotY += y;
        accRotZ += z;
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

    public static final String LOG_TAG = "GDT";
    public static final String LOG_UNIT = "deg";

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

    @Override
    protected Value getUnprocessedGraphValue() {
        return rotationRate;
    }
}
