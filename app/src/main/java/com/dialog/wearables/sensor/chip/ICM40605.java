/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor.chip;

import java.util.HashMap;

public class ICM40605 {
    private static final String TAG = "ICM40605";

    private static final float[] ACCELEROMETER_SENSITIVITY = new float[] { 2048.f, 4096.f, 8192.f, 16384.f };
    private static final float[] GYROSCOPE_SENSITIVITY = new float[] { 16.4f, 32.8f, 65.6f, 131.2f, 262.4f };
    private static final HashMap<Integer, Float> GYROSCOPE_RATES = new HashMap<>();
    private static final HashMap<Integer, Float> ACCELEROMETER_RATES = new HashMap<>();
    static {
        GYROSCOPE_RATES.put(10, 25.f);
        GYROSCOPE_RATES.put(9, 50.f);
        GYROSCOPE_RATES.put(8, 100.f);
        GYROSCOPE_RATES.put(7, 200.f);
        GYROSCOPE_RATES.put(6, 1000.f);
        ACCELEROMETER_RATES.put(10, 25.f);
        ACCELEROMETER_RATES.put(9, 50.f);
        ACCELEROMETER_RATES.put(8, 100.f);
        ACCELEROMETER_RATES.put(7, 200.f);
        ACCELEROMETER_RATES.put(6, 1000.f);
    }

    private static class Accelerometer extends com.dialog.wearables.sensor.Accelerometer {

        private int[] raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = get3DValuesLE(data, offset);
            return super.processRawData(raw);
        }
    }

    private static class Gyroscope extends com.dialog.wearables.sensor.Gyroscope {

        private int[] raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = get3DValuesLE(data, offset);
            return super.processRawData(raw);
        }
    }

    private Accelerometer accelerometer;
    private Gyroscope gyroscope;

    public ICM40605() {
        accelerometer = new Accelerometer();
        gyroscope = new Gyroscope();
        accelerometer.setSensitivity(16384.f);
        gyroscope.setRate(10.f);
        gyroscope.setSensitivity(16.4f);
    }

    public Accelerometer getAccelerometer() {
        return accelerometer;
    }

    public Gyroscope getGyroscope() {
        return gyroscope;
    }

    public void processAccelerometerRawConfig(int range) {
        accelerometer.setSensitivity(ACCELEROMETER_SENSITIVITY[range]);
    }

    public void processGyroscopeRawConfig(int range, int rate) {
        gyroscope.setSensitivity(GYROSCOPE_SENSITIVITY[range]);
        if (rate > 0) {
            gyroscope.setRate(GYROSCOPE_RATES.get(rate));
        }
    }

    public float getAccelerometerRateFromRawConfig(int raw) {
        return ACCELEROMETER_RATES.get(raw);
    }

    public float getGyrescopeRateFromRawConfig(int raw) {
        return GYROSCOPE_RATES.get(raw);
    }
}
