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

public class EKDevice {

    private String EKID;

    private String UserId;

    private String FriendlyName;

    private String Description;

    public EKDevice() {
    }

    public EKDevice(String EKID, String userId, String friendlyName, String description) {
        this.EKID = EKID;
        this.UserId = userId;
        this.FriendlyName = friendlyName;
        this.Description = description;
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
        this.UserId = userId;
    }

    public String getFriendlyName() {
        return FriendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.FriendlyName = friendlyName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        this.Description = description;
    }
}
