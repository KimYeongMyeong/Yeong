/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.apis.cloud.rest;

public class IoTApp {

    private int IoTAppType;

    private boolean IsEnabled;

    private String UserId;

    private String APPID;

    public IoTApp() {
    }

    public IoTApp(int ioTAppType, boolean isEnabled, String userId, String APPID) {
        this.IoTAppType = ioTAppType;
        this.IsEnabled = isEnabled;
        this.UserId = userId;
        this.APPID = APPID;
    }

    public int getIoTAppType() {
        return IoTAppType;
    }

    public void setIoTAppType(int ioTAppType) {
        this.IoTAppType = ioTAppType;
    }

    public boolean isEnabled() {
        return IsEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.IsEnabled = enabled;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }
}

