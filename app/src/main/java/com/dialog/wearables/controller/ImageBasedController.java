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
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.sensor.IotSensor;

public abstract class ImageBasedController extends IotSensorController {
    protected ImageView image;
    protected ClipDrawable clip;

    protected ImageBasedController(IotSensorsDevice device, IotSensor sensor, Fragment fragment, int viewId, int chartId) {
        super(device, sensor, fragment, viewId, chartId);
    }

    protected void initImage(int imageId) {
        image = (ImageView) fragment.getView().findViewById(imageId);
        clip = (ClipDrawable) ((LayerDrawable)image.getDrawable()).getDrawable(1);
        image.bringToFront();
    }

    @Override
    protected void setShapeSize(int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image.getLayoutParams();
        params.width = size;
        params.height = size;
        image.setLayoutParams(params);
    }

    protected void setLevel(int percentage) {
        percentage = Math.min(100, percentage);
        percentage = Math.max(0, percentage);
        final int level = percentage * 100; // 0..10000
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clip.setLevel(level);
            }
        });
    }
}
