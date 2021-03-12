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

public class AssetTrackingTag {

    private String TagId;

    private String FriendlyName;

    private String Description;

    private String SupervisorId;

    private String UserId;

    public AssetTrackingTag() {
    }

    public AssetTrackingTag(String tagId, String friendlyName, String description, String supervisorId, String userId) {
        TagId = tagId;
        FriendlyName = friendlyName;
        Description = description;
        SupervisorId = supervisorId;
        UserId = userId;
    }

    public String getTagId() {
        return TagId;
    }

    public void setTagId(String tagId) {
        TagId = tagId;
    }

    public String getFriendlyName() {
        return FriendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        FriendlyName = friendlyName;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getSupervisorId() {
        return SupervisorId;
    }

    public void setSupervisorId(String supervisorId) {
        SupervisorId = supervisorId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    //----------------------------------------------------------------------------------------------
    public boolean isValid(){
        return (TagId != null && !TagId.trim().isEmpty()
                && FriendlyName != null && !FriendlyName.trim().isEmpty());
    }
    //----------------------------------------------------------------------------------------------
}
