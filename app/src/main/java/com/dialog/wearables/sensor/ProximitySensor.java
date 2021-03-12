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

public abstract class ProximitySensor extends IotSensor {

    protected boolean objectNearby;
    protected boolean lowVoltage;

    public boolean isObjectNearby() {
        return objectNearby;
    }

    public boolean isLowVoltage() {
        return lowVoltage;
    }

    public static final String LOG_TAG = "PRX";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogEntry() {
        return isObjectNearby() ? "ON" : "OFF";
    }

    @Override
    public String getCloudData() {
        return isObjectNearby() ? "true" : "false";
    }
}
