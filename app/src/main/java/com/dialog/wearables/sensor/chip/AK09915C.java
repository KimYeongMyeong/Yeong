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

public class AK09915C {
    private static final String TAG = "AK09915C";

    private static class Magnetometer extends com.dialog.wearables.sensor.Magnetometer {

        private int[] raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = get3DValuesLE(data, offset);
            x = raw[0] * 0.3174f;
            y = raw[1] * 0.3174f;
            z = raw[2] * 0.1526f;
            calculateHeading();
            value = new Value3D(x, y, z);
            return value;
        }
    }

    private Magnetometer magnetometer;

    public AK09915C() {
        magnetometer = new Magnetometer();
    }

    public Magnetometer getMagnetometer() {
        return magnetometer;
    }
}
