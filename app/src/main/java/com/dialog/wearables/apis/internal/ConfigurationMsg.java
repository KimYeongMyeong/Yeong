/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.apis.internal;

import android.os.Parcel;
import android.os.Parcelable;

public class ConfigurationMsg implements Parcelable
{
    // Select which event types should not be forwarded from the BLE-
    // layer to the Cloud-layer of the mobile application
    public int[] StopFw;

    public ConfigurationMsg(int[] disabledIoTApps) {
        this.StopFw = disabledIoTApps;
    }

    protected ConfigurationMsg(Parcel in) {
        StopFw = in.createIntArray();
    }

    public static final Creator<ConfigurationMsg> CREATOR = new Creator<ConfigurationMsg>() {
        @Override
        public ConfigurationMsg createFromParcel(Parcel in) {
            return new ConfigurationMsg(in);
        }

        @Override
        public ConfigurationMsg[] newArray(int size) {
            return new ConfigurationMsg[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(StopFw);
    }
}
