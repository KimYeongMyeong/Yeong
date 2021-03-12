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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dialog.wearables.GlobalVar;
import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;

import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;

public class PressureController extends IotSensorController {
    private static final String UNIT = "hPa";

    protected ImageView imageMiddle;

    private float difference;
    private float previousValue;

    public PressureController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getPressureSensor(), fragment, R.id.pressureView, R.id.pressureChart);
        chart.setViewportCalculationEnabled(true);
        graphDataSize = device.pressureGraphData.capacity();
        label = (TextView) fragment.getView().findViewById(R.id.pressureLabel);
        imageMiddle = (ImageView) fragment.getView().findViewById(R.id.pressureMiddle);
        imageMiddle.bringToFront();
    }

    @Override
    protected void setShapeSize(int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) imageMiddle.getLayoutParams();
        params.width = size;
        params.height = size;
        imageMiddle.setLayoutParams(params);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageMiddle.setTranslationY(-difference);
            }
        });

        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = v.bottom / 1.001f;
        v.top = v.top * 1.001f;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    protected void setLabelValue(float value) {
        if (previousValue != 0)
            difference = value - previousValue;
        previousValue = value;
        setLabelString(String.format("%.1f %s", value / 100.f , UNIT));

//        GlobalVar.atmo.setVar(value / 100.f);
    }

    @Override
    protected List<PointValue> getGraphData() {
        return getList(device.pressureGraphData);
    }
}
