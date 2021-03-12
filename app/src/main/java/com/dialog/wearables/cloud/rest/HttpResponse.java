/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.cloud.rest;

public class HttpResponse{
    public String body;
    public int statusCode;

    public HttpResponse(String body, int code){
        this.body = body;
        this.statusCode = code;
    }
}