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

public class HistoricalGetEnvironmentalReq {

    private String UserId;

    private String startDate;

    private String endDate;

    private String ekId;

    private String appId;

    public HistoricalGetEnvironmentalReq() {
    }

    public HistoricalGetEnvironmentalReq(String startDate, String endDate, String ekId, String appId, String userId) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.ekId = ekId;
        this.appId = appId;
        this.UserId = userId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEkId() {
        return ekId;
    }

    public void setEkId(String ekId) {
        this.ekId = ekId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    //----------------------------------------------------------------------------------------------
    public boolean IsReqValid(){
        return (startDate != null && !startDate.trim().isEmpty()
                && endDate != null && !endDate.trim().isEmpty()
                && ekId != null && !ekId.trim().isEmpty()
                && appId != null && !appId.trim().isEmpty());
    }

    //----------------------------------------------------------------------------------------------
}
