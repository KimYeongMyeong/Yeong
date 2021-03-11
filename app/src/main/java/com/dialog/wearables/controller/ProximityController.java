/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.controller;

import android.app.Fragment;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.sensor.ProximitySensor;
import com.mikepenz.iconics.view.IconicsImageView;

public class ProximityController extends IotSensorController {

    private IconicsImageView proximityImage;
    private IconicsImageView batteryWarningImage;

    public ProximityController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getProximitySensor(), fragment, R.id.proximityView, 0);
        label = (TextView) fragment.getView().findViewById(R.id.proximityLabel);
        proximityImage = (IconicsImageView) fragment.getView().findViewById(R.id.proximityImage);
        proximityImage.bringToFront();
        batteryWarningImage = (IconicsImageView) fragment.getView().findViewById(R.id.proximityBatteryWarning);
    }

    @Override
    protected void setShapeSize(int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) proximityImage.getLayoutParams();
        params.width = size;
        params.height = size;
        proximityImage.setLayoutParams(params);
    }

    @Override
    protected void setLabelValue(float value) {
        final boolean objectNearby = ((ProximitySensor)sensor).isObjectNearby();
        final boolean lowVoltage = ((ProximitySensor)sensor).isLowVoltage();
        setLabelString(fragment.getString(objectNearby ? R.string.value_proximity_on : R.string.value_proximity_off));
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                proximityImage.getIcon().colorRes(objectNearby ? R.color.md_blue_400 : R.color.md_blue_grey_100);
                batteryWarningImage.setVisibility(lowVoltage ? View.VISIBLE : View.GONE);
            }
        });
    }
}
