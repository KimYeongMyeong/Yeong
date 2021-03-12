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

import java.util.ArrayList;
import java.util.List;

public class MgmtGetEKIDRsp {

    private List<DeviceInfo> Devices;

    public MgmtGetEKIDRsp() {
    }

    public MgmtGetEKIDRsp(List<DeviceInfo> Devices) {
        this.Devices = Devices;
    }

    public List<DeviceInfo> getDevices() {
        return Devices;
    }

    public void setDevices(List<DeviceInfo> devices) {
       this.Devices = devices;
    }
}
