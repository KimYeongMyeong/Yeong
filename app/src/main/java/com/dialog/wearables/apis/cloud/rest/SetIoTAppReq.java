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

public class SetIoTAppReq {

    private int OperationType; // eOperationTypes

    private IoTApp IoTAppInfo;

    public SetIoTAppReq() {
    }

    public SetIoTAppReq(int operationType, IoTApp ioTAppInfo) {
        this.OperationType = operationType;
        this.IoTAppInfo = ioTAppInfo;
    }

    public int getOperationType() {
        return OperationType;
    }

    public void setOperationType(int operationType) {
        this.OperationType = operationType;
    }

    public IoTApp getIoTAppInfo() {
        return IoTAppInfo;
    }

    public void setIoTAppInfo(IoTApp ioTAppInfo) {
        this.IoTAppInfo = ioTAppInfo;
    }
}
