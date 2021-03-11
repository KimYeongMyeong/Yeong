/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.controller;

import android.app.Fragment;
import android.widget.TextView;

import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;

import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;

public class HumidityController extends ImageBasedController {
    private static final String UNIT = "%";

    public HumidityController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getHumiditySensor(), fragment, R.id.humidityView, R.id.humidityChart);
        graphDataSize = device.humidityGraphData.capacity();
        label = (TextView) fragment.getView().findViewById(R.id.humidityLabel);
        initImage(R.id.humidityImage);
        setLevel(0);
    }

    @Override
    protected void setLabelValue(float value) {
        int v = (int) value;
        setLabelString(v + UNIT);
        setLevel(v);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Viewport v = new Viewport();
        v.bottom = 0.0f;
        v.top = 100.0f;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    protected List<PointValue> getGraphData() {
        return getList(device.humidityGraphData);
    }
}
