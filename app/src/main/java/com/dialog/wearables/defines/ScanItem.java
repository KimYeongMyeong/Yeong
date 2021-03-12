/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.defines;

/**
 * ScanItem object
 */
public class ScanItem {
    public int scanIcon;
    public String scanName;
    public String scanDescription;
    public int scanSignal, deviceType;

    public ScanItem(int scanIcon, String scanName, String scanDescription, int scanSignal, int deviceType) {
        this.scanIcon = scanIcon;
        this.scanName = scanName;
        this.scanDescription = scanDescription;
        this.scanSignal = scanSignal;
        this.deviceType = deviceType;
    }
}
