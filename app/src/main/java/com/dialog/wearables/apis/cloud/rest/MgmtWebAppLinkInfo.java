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

public class MgmtWebAppLinkInfo {
    public String Url;
    public String Email;

    public MgmtWebAppLinkInfo(String Url, String userEmail){
        this.Url = Url;
        this.Email = userEmail;
    }
}
