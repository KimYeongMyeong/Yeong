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

public class AmazonAccountInfoReq {

    private String UserId;

    private String AmazonUserId;

    private String AmazonAccessToken;

    private String AmazonEmail;

    public AmazonAccountInfoReq() {
    }

    public AmazonAccountInfoReq(String userId, String amazonUserId, String amazonAccessToken, String email) {
        this.UserId = userId;
        this.AmazonUserId = amazonUserId;
        this.AmazonAccessToken = amazonAccessToken;
        this.AmazonEmail = email;
    }

    public String getAmazonUserId() {
        return AmazonUserId;
    }

    public void setAmazonUserId(String amazonUserId) {
        this.AmazonUserId = amazonUserId;
    }

    public String getAmazonAccessToken() {
        return AmazonAccessToken;
    }

    public void setAmazonAccessToken(String amazonAccessToken) {
        this.AmazonAccessToken = amazonAccessToken;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getEmail() {
        return AmazonEmail;
    }

    public void setEmail(String email) {
        this.AmazonEmail = email;
    }
}
