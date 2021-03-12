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

public abstract class AirQualitySensor extends IotSensor {

    public static final int UNKNOWN = -1;
    public static final int GOOD = 0;
    public static final int AVERAGE = 1;
    public static final int LITTLE_BAD = 2;
    public static final int BAD = 3;
    public static final int VERY_BAD = 4;
    public static final int GET_OUT = 5;

    public static final int[] ACCURACY = new int[] {
            R.string.air_quality_accuracy_unreliable,
            R.string.air_quality_accuracy_low,
            R.string.air_quality_accuracy_medium,
            R.string.air_quality_accuracy_high,
    };

    public static final int[] QUALITY = new int[] {
            R.string.value_air_quality_good,
            R.string.value_air_quality_average,
            R.string.value_air_quality_little_bad,
            R.string.value_air_quality_bad,
            R.string.value_air_quality_very_bad,
            R.string.value_air_quality_worst,
    };

    public static final int[] COLOR = new int[] {
            R.color.md_green_700,
            R.color.md_yellow_700,
            R.color.md_orange_700,
            R.color.md_red_700,
            R.color.md_purple_700,
            R.color.md_black_1000,
    };

    public static final int[] RANGE = new int[] {
            50,
            100,
            150,
            200,
            300
    };

    protected int accuracy;
    protected float quality;
    protected int airQualityIndex;

    public int getAccuracy() {
        return accuracy;
    }

    public float getQuality() {
        return quality;
    }

    public int getAirQualityIndex() {
        return  airQualityIndex;
    }

    protected void calculateAirQualityIndex() {
        airQualityIndex = GOOD;
        for (int limit : RANGE) {
            if (quality > limit)
                ++airQualityIndex;
            else
                break;
        }
    }

    public static final String LOG_TAG = "AQI";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogEntry() {
        return String.format("%s (%d)", super.getLogEntry(), accuracy);
    }
}
