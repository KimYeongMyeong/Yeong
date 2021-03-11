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

public class MgmtDeviceApplicationLinkInfo {

    private String APPID;

    private String EKID;

    private String UserId;

    public MgmtDeviceApplicationLinkInfo() {
    }

    public MgmtDeviceApplicationLinkInfo(String APPID, String EKID, String userId) {
        this.APPID = APPID;
        this.EKID = EKID;
        UserId = userId;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getEKID() {
        return EKID;
    }

    public void setEKID(String EKID) {
        this.EKID = EKID;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
