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

public class MgmtIftttInfo {
    public String IftttApiKey;
    public String UserId;

    public MgmtIftttInfo(String apikey, String userId){
        this.IftttApiKey = apikey;
        this.UserId = userId;
    }
}
