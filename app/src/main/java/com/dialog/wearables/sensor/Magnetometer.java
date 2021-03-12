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

import com.dialog.wearables.R;

public abstract class Magnetometer extends IotSensor {
    private static final String TAG = "Magnetometer";

    protected float x, y, z;
    protected float degrees;
    protected float heading;

    public void calculateHeading() {
        degrees = (float) (Math.atan2(y, x) * 180. / Math.PI);
        heading = degrees >= 0 ? degrees : 360.f + degrees;
    }

    public float getDegrees() {
        return degrees;
    }

    public float getHeading() {
        return heading;
    }

    private static final int[] COMPASS_HEADING = new int[] {
            R.string.compass_heading_n,
            R.string.compass_heading_ne,
            R.string.compass_heading_e,
            R.string.compass_heading_se,
            R.string.compass_heading_s,
            R.string.compass_heading_sw,
            R.string.compass_heading_w,
            R.string.compass_heading_nw,
            R.string.compass_heading_n,
    };

    public static int getCompassHeading(float degrees) {
        while (degrees < 0)
            degrees += 360;
        while (degrees > 360)
            degrees -= 360;
        return COMPASS_HEADING[Math.round(degrees / 45)];
    }

    public static final String LOG_TAG = "MAG";
    public static final String LOG_UNIT = "uT";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return LOG_UNIT;
    }

    @Override
    public String getLogEntry() {
        return String.format("%s\t%.0fdeg", super.getLogEntry(), heading);
    }

    @Override
    public float getDisplayValue() {
        return getHeading();
    }
}
