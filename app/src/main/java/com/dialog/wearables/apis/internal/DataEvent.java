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

public class DataEvent implements Parcelable {

    public int EventType;
    public String Data;

    public DataEvent() {
    }

    public DataEvent(int eventType, String data) {
        this.EventType = eventType;
        this.Data = data;
    }

    protected DataEvent(Parcel in) {
        EventType = in.readInt();
        Data = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(EventType);
        dest.writeString(Data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DataEvent> CREATOR = new Creator<DataEvent>() {
        @Override
        public DataEvent createFromParcel(Parcel in) {
            return new DataEvent(in);
        }

        @Override
        public DataEvent[] newArray(int size) {
            return new DataEvent[size];
        }
    };
}

