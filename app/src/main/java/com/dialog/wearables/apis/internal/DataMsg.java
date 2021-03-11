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

import java.util.ArrayList;

public class DataMsg implements Parcelable {

    public String EKID;
    public ArrayList<DataEvent> Events = new ArrayList<>(5);

    public DataMsg(String EKID) {
        this.EKID = EKID;
    }

    protected DataMsg(Parcel in) {
        EKID = in.readString();
        Events = in.createTypedArrayList(DataEvent.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(EKID);
        dest.writeTypedList(Events);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DataMsg> CREATOR = new Creator<DataMsg>() {
        @Override
        public DataMsg createFromParcel(Parcel in) {
            return new DataMsg(in);
        }

        @Override
        public DataMsg[] newArray(int size) {
            return new DataMsg[size];
        }
    };
}
