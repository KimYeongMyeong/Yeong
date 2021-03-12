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

import java.util.Date;

public class ScanResult {

    private String TagId;

    private int Rssi;

    private Date Timestamp;

    public ScanResult() {
    }

    public ScanResult(String tagId, int rssi, Date timestamp) {
        this.TagId = tagId;
        this.Rssi = rssi;
        this.Timestamp = timestamp;
    }

    public String getTagId() {
        return TagId;
    }

    public void setTagId(String tagId) {
        this.TagId = tagId;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        this.Rssi = rssi;
    }

    public Date getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.Timestamp = timestamp;
    }
}
