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

public class DataMsg {

    /**
     * One of {@ref eEventTypes} (Edge->Service) or {@ref eActuationTypes} (Service->Edge)
     * depending on message direction
      */
    private int MsgType;

    private String Data;

    public DataMsg() {
    }

    public DataMsg(int eventType, String data) {
        this.MsgType = eventType;
        this.Data = data;
    }

    public int getMsgType() {
        return MsgType;
    }

    public void setMsgType(int msgType) {
        this.MsgType = msgType;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        this.Data = data;
    }
}

