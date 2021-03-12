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

import java.util.ArrayList;
import java.util.List;

public class EdgeServiceApiMsg {

    private String UserId;

    private String APPID;

    private String EKID;

    private String Timestamp;

    private List<DataMsg> Events;

    private List<MgmtMsg> MgmtMsgs;

    public EdgeServiceApiMsg() {
    }

    public EdgeServiceApiMsg(String userId, String APPID, List<MgmtMsg> mgmtMsgs) {
        this.UserId = userId;
        this.APPID = APPID;
        this.MgmtMsgs = mgmtMsgs;
    }

    public EdgeServiceApiMsg(String userId, String APPID, String EKID, String timestamp,
                             List<DataMsg> events, List<MgmtMsg> mgmtMsgs) {
        this.UserId = userId;
        this.APPID = APPID;
        this.EKID = EKID;
        this.Timestamp = timestamp;
        this.Events = events;
        this.MgmtMsgs = mgmtMsgs;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public String getEKID() {
        return EKID;
    }

    public void setEKID(String EKID) {
        this.EKID = EKID;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.Timestamp = timestamp;
    }

    public void setEvents(List<DataMsg> events) {
        this.Events = new ArrayList<>(events);
    }

    public List<DataMsg> getEvents() {
        return Events;
    }

    public List<MgmtMsg> getMgmtMsgs() {
        return MgmtMsgs;
    }

    public void setMgmtMsgs(List<MgmtMsg> mgmtMsgs) {
        MgmtMsgs = mgmtMsgs;
    }

    public boolean isValid() {
        return (getAPPID() != null && !getAPPID().isEmpty()) &&
//                (getEKID() != null && !getEKID().isEmpty()) &&
                (getTimestamp() != null && !getTimestamp().isEmpty()) &&
                (getEvents() != null && !getEvents().isEmpty());
    }
}
