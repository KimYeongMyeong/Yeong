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

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HistoricalGetEnvironmentalRsp {

    private List<Float> Values;

    private List<String> Timestamps;

    public HistoricalGetEnvironmentalRsp() {
    }

    public HistoricalGetEnvironmentalRsp(List<Float> values, List<String> timestamps) {
        this.Values = values;
        this.Timestamps = timestamps;
    }

    public List<Float> getValues() {
        return Values;
    }

    public void setValues(List<Float> values) {
        Values = values;
    }

    public List<String> getTimestamps() {
        return Timestamps;
    }

    public void setTimestamps(List<String> timestamps) {
        Timestamps = timestamps;
    }

    public List<String> getTimestampsInLocalTime() {
        return convertInLocalTime(Timestamps);
    }

    private List<String> convertInLocalTime(List<String> utcTimestamps) {
        List<String> localTimestamps = new ArrayList<>();
        for (String utcTimestamp : utcTimestamps){
            String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
            try {
                TimeZone utcZone = TimeZone.getTimeZone("UTC");
                sdf.setTimeZone(utcZone);// Set UTC time zone
                Date myDate = sdf.parse(utcTimestamp.replace("T", " "));
                sdf.setTimeZone(TimeZone.getDefault());// Set device time zone
                localTimestamps.add(sdf.format(myDate));
            } catch (Exception ex) {
                Log.e("UTC_TO_LOCAL", ex.getMessage());
            }
        }
        return localTimestamps;
    }
}
