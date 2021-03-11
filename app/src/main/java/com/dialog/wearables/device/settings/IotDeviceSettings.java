/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.device.settings;

import android.content.SharedPreferences;

import com.dialog.wearables.device.IotSensorsDevice;

import java.util.List;

public abstract class IotDeviceSettings {

    protected IotSensorsDevice device;

    public IotDeviceSettings(IotSensorsDevice device) {
        this.device = device;
    }

    public abstract int length();
    public abstract boolean valid();
    public abstract boolean modified();
    public abstract void process(byte[] data, int offset);
    public abstract byte[] pack();
    public abstract void save(SharedPreferences pref);
    public abstract void load(SharedPreferences pref);
    public abstract String[] getPrefKeys();

    public interface ProcessCallback {
        void onProcess();
    }

    protected ProcessCallback processCallback;

    public void setProcessCallback(ProcessCallback processCallback) {
        this.processCallback = processCallback;
    }

    public static class RangeError {
        public String key;
        public int min;
        public int max;

        public RangeError(String key, int min, int max) {
            this.key = key;
            this.min = min;
            this.max = max;
        }
    }

    public List<RangeError> checkRange() {
        return null;
    }
}
