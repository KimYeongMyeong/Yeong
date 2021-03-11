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
import com.dialog.wearables.sensor.AmbientLightSensor;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;

public class AmbientLightController extends IotSensorController {
    private static final String UNIT = " lux";

    protected IconicsImageView ambientLightImage;
    private IconicsImageView batteryWarningImage;

    public AmbientLightController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getAmbientLightSensor(), fragment, R.id.ambientLightView, R.id.ambientLightChart);
        chart.setViewportCalculationEnabled(true);
        graphDataSize = device.ambientLightGraphData.capacity();
        label = (TextView) fragment.getView().findViewById(R.id.ambientLightLabel);
        ambientLightImage = (IconicsImageView) fragment.getView().findViewById(R.id.ambientLightImage);
        ambientLightImage.bringToFront();
        batteryWarningImage = (IconicsImageView) fragment.getView().findViewById(R.id.ambientLightBatteryWarning);
    }

    @Override
    protected void setShapeSize(int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ambientLightImage.getLayoutParams();
        params.width = size * 9 / 10;
        params.height = size * 9 / 10;
        ambientLightImage.setLayoutParams(params);
    }

    @Override
    protected void setLabelValue(final float value) {
        setLabelString((int) value + UNIT);
        final boolean lowVoltage = ((AmbientLightSensor)sensor).isLowVoltage();
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int adjust = Math.min((int) value * 128 / 5000, 128);
                int color = 0xFFCFD8DC - 0x010000 * adjust - 0x000100 * (adjust / 2);
                ambientLightImage.getIcon().color(color);
                batteryWarningImage.setVisibility(lowVoltage ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0.0f;
        v.top = ((int) v.top / 1000 + 1) * 1000;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    protected List<PointValue> getGraphData() {
        return getList(device.ambientLightGraphData);
    }
}
