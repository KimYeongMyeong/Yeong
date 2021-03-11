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
import com.dialog.wearables.sensor.TemperatureSensor;

import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;

public class TemperatureController extends ImageBasedController {
    private static final String UNIT_CELSIUS = "°C";
    private static final String UNIT_FAHRENHEIT = "°F";

    public TemperatureController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getTemperatureSensor(), fragment, R.id.temperatureView, R.id.temperatureChart);
        graphDataSize = device.temperatureGraphData.capacity();
        label = (TextView) fragment.getView().findViewById(R.id.temperatureLabel);
        initImage(R.id.temperatureImage);
        setLevel(0);
    }

    @Override
    protected void setLabelValue(float value) {
        setLabelString(String.format("%.1f %s", value, ((TemperatureSensor)sensor).getDisplayUnit() == TemperatureSensor.CELSIUS ? UNIT_CELSIUS : UNIT_FAHRENHEIT));
        // Convert the range [0-50] degrees to [0-100] percent.
        value = ((TemperatureSensor)sensor).getTemperature(TemperatureSensor.CELSIUS); // always Celsius for percentage
        int percentage = (int) value * 2;
        setLevel(percentage);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Viewport v = new Viewport();
        v.bottom = 10.0f;
        v.top = 50.0f;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    protected List<PointValue> getGraphData() {
        return getList(device.temperatureGraphData);
    }
}
