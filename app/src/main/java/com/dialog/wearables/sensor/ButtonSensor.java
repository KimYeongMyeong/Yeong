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

public abstract class ButtonSensor extends IotSensor {

    protected int id;
    protected boolean state;

    public int getId() {
        return id;
    }

    public boolean getState() {
        return state;
    }

    public boolean isPressed(int id) {
        return getState();
    }

    public boolean isPressed() {
        return getState();
    }

    public static final String LOG_TAG = "BTN";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogEntry() {
        return String.format("%02X %s", id, isPressed() ? "pressed" : "released");
    }

    @Override
    public String getCloudData() {
        return isPressed() ? "true" : "false";
    }
}
