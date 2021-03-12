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

public class AdvertiseMsg {

    private int Rssi;

    private String Mac;

    private String Uuid;

    private int Major;

    private int Minor;

    public AdvertiseMsg() {
    }

    public AdvertiseMsg(int rssi, String mac) {
        this.Rssi = rssi;
        this.Mac = mac;
    }

    public AdvertiseMsg(int rssi, String mac, String uuid, int major, int minor) {
        this.Rssi = rssi;
        this.Mac = mac;
        this.Uuid = uuid;
        this.Major = major;
        this.Minor = minor;
    }

    public int getRssi() {
        return Rssi;
    }

    public void setRssi(int rssi) {
        this.Rssi = rssi;
    }

    public String getMac() {
        return Mac;
    }

    public void setMac(String mac) {
        this.Mac = mac;
    }

    public String getUuid() {
        return Uuid;
    }

    public void setUuid(String uuid) {
        this.Uuid = uuid;
    }

    public int getMajor() {
        return Major;
    }

    public void setMajor(int major) {
        this.Major = major;
    }

    public int getMinor() {
        return Minor;
    }

    public void setMinor(int minor) {
        this.Minor = minor;
    }
}
