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

public class SetDeviceReq {

    private int OperationType; // eOperationTypes

    private EKDevice DeviceInfo;

    public SetDeviceReq() {
    }

    public SetDeviceReq(int operationType, EKDevice deviceInfo) {
        this.OperationType = operationType;
        this.DeviceInfo = deviceInfo;
    }

    public int getOperationType() {
        return OperationType;
    }

    public void setOperationType(int operationType) {
        this.OperationType = operationType;
    }

    public EKDevice getDeviceInfo() {
        return DeviceInfo;
    }

    public void setDeviceInfo(EKDevice deviceInfo) {
        this.DeviceInfo = deviceInfo;
    }
}
