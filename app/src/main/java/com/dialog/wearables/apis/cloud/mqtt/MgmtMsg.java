/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.apis.cloud.mqtt;

public class MgmtMsg {

    /**
     * One of
     * @see eMgmtServiceOperations (Edge->Service) or
     * @see eMgmtEdgeOperations (Service->Edge)
     * depending on message direction
     */
    private int OperationType;

    private String Payload;

    public MgmtMsg() {
    }

    public MgmtMsg(int operationType, String payload) {
        this.OperationType = operationType;
        this.Payload = payload;
    }

    public int getOperationType() {
        return OperationType;
    }

    public void setOperationType(int operationType) {
        this.OperationType = operationType;
    }

    public String getPayload() {
        return Payload;
    }

    public void setPayload(String payload) {
        this.Payload = payload;
    }
}
