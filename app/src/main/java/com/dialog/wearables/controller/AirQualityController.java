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
import com.dialog.wearables.sensor.AirQualitySensor;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;

public class AirQualityController extends IotSensorController {

    private IconicsImageView airQualityImage;
    private TextView accuracy;

    public AirQualityController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getAirQualitySensor(), fragment, R.id.airQualityView, R.id.airQualityChart);
        chart.setViewportCalculationEnabled(true);
        graphDataSize = device.airQualityGraphData.capacity();
        label = (TextView) fragment.getView().findViewById(R.id.airQualityLabel);
        accuracy = (TextView) fragment.getView().findViewById(R.id.airQualityAccuracyLabel);
        airQualityImage = (IconicsImageView) fragment.getView().findViewById(R.id.airQualityImage);
        airQualityImage.bringToFront();
    }

    @Override
    protected void setShapeSize(int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) airQualityImage.getLayoutParams();
        params.width = size * 4 / 5;
        params.height = size * 4 / 5;
        airQualityImage.setLayoutParams(params);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0.0f;
        v.top = ((int) v.top / 50 + 1) * 50;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    protected void setLabelValue(float value) {
        final int index = ((AirQualitySensor)sensor).getAirQualityIndex();
        final int accuracyIndex = ((AirQualitySensor)sensor).getAccuracy();
        if (index == AirQualitySensor.UNKNOWN)
            return;
        setLabelString(fragment.getString(AirQualitySensor.QUALITY[index]));
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                airQualityImage.getIcon().colorRes(AirQualitySensor.COLOR[index]);
                accuracy.setVisibility(View.VISIBLE);
                accuracy.setText(AirQualitySensor.ACCURACY[accuracyIndex]);
            }
        });
    }

    @Override
    protected List<PointValue> getGraphData() {
        return getList(device.airQualityGraphData);
    }
}
