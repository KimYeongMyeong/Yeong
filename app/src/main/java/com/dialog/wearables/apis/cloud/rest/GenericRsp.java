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

public class GenericRsp {

    private boolean Result;

    private String ReasonCode;

    public GenericRsp() {
    }

    public GenericRsp(boolean result, String reasonCode) {
        this.Result = result;
        this.ReasonCode = reasonCode;
    }

    public boolean isResult() {
        return Result;
    }

    public void setResult(boolean result) {
        this.Result = result;
    }

    public String getReasonCode() {
        return ReasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.ReasonCode = reasonCode;
    }
}
