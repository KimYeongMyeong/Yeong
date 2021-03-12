/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables;

import android.app.Application;

import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.IotSensorsLogger;
import com.dialog.wearables.global.Object3DLoader;

/**
 * Application wide values
 */
public class IotSensorsApplication extends Application {

    private static IotSensorsApplication application;

    public static IotSensorsApplication getApplication() {
        return application;
    }

    public IotSensorsDevice device;
    public IotSensorsLogger logger;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Object3DLoader.startLoading(getApplicationContext());
    }
}
