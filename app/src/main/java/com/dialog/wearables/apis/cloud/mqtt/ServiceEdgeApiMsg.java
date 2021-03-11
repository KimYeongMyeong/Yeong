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

public class ServiceEdgeApiMsg {

    private String UserId;

    private String APPID;

    private String EKID;

    private List<DataMsg> Actuations;

    private List<MgmtMsg> MgmtMsgs;

    public ServiceEdgeApiMsg() {
    }

    public ServiceEdgeApiMsg(String userId, String APPID, String EKID, List<DataMsg> actuations,
                             List<MgmtMsg> mgmtMsgs) {
        this.UserId = userId;
        this.APPID = APPID;
        this.EKID = EKID;
        this.Actuations = actuations;
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

    public void setActuations(List<DataMsg> actuations) {
        this.Actuations = new ArrayList<>(actuations);
    }

    public List<DataMsg> getActuations() {
        return Actuations;
    }

    public List<MgmtMsg> getMgmtMsgs() {
        return MgmtMsgs;
    }

    public void setMgmtMsgs(List<MgmtMsg> mgmtMsgs) {
        this.MgmtMsgs = mgmtMsgs;
    }
}
