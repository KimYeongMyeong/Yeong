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

public class BMI160 {
    private static final String TAG = "BMI160";

    private static HashMap<Integer, Float> ACCELEROMETER_SENSITIVITY = new HashMap<>();
    static {
        ACCELEROMETER_SENSITIVITY.put(3, 16384.f);
        ACCELEROMETER_SENSITIVITY.put(5, 8192.f);
        ACCELEROMETER_SENSITIVITY.put(8, 4096.f);
        ACCELEROMETER_SENSITIVITY.put(12, 2048.f);
    }
    private static final float[] GYROSCOPE_SENSITIVITY = new float[] { 16.4f, 32.8f, 65.6f, 131.2f, 262.4f };
    private static final float[] GYROSCOPE_RATES = new float[] { 0.78f, 1.56f, 3.12f, 6.25f, 12.5f, 25.f, 50.f, 100.f };

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

    public BMI160() {
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
        accelerometer.setSensitivity(ACCELEROMETER_SENSITIVITY.get(range));
    }

    public void processGyroscopeRawConfig(int range, int rate) {
        gyroscope.setSensitivity(GYROSCOPE_SENSITIVITY[range]);
        if (rate > 0) {
            gyroscope.setRate(GYROSCOPE_RATES[rate - 1]);
        }
    }
}
