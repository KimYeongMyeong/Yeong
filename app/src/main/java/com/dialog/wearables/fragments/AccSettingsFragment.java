/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.settings.IotSettingsManager;

public class AccSettingsFragment extends IotSettingsFragment {
    private static final String TAG = "AccSettingsFragment";

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getSettingsXml() {
        return device.getAccelerometerSettingsXml();
    }

    @Override
    protected IotSettingsManager getSettingsManager() {
        return device.getAccelerometerSettingsManager(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_acc_settings, container, false);
        ((ImageView)view.findViewById(R.id.imageView)).setImageResource(device.type != IotSensorsDevice.TYPE_IOT_585 ? R.drawable.calimage : R.drawable.calimage_iot_plus);
        ((TextView)view.findViewById(R.id.textView)).setText(device.type != IotSensorsDevice.TYPE_IOT_585 ? R.string.acc_calibration_example : R.string.acc_calibration_example_iot_plus);
        return view;
    }
}
