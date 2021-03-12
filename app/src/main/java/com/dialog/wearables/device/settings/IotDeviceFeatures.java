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

import android.util.Log;

import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.Utils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;

public class IotDeviceFeatures {
    private static final String TAG = "IotDeviceFeatures";

    protected IotSensorsDevice device;
    protected byte[] rawFeatures;
    protected HashSet<Integer> features;
    protected boolean temperature = true;
    protected boolean humidity = true;
    protected boolean pressure = true;
    protected boolean accelerometer = true;
    protected boolean gyroscope = true;
    protected boolean magnetometer = true;
    protected boolean ambientLight = false;
    protected boolean proximity = false;
    protected boolean proximityCalibration = false;
    protected boolean rawGas = false;
    protected boolean airQuality = false;
    protected boolean gasSensor = false;
    protected boolean button = false;
    protected boolean sensorFusion = false;
    protected boolean integrationEngine = false;
    protected boolean valid = false;

    public IotDeviceFeatures(IotSensorsDevice device) {
        this.device = device;
    }

    public void processFeaturesReport(byte[] data, int offset) {
        rawFeatures = Arrays.copyOfRange(data, offset, data.length);
        features = new HashSet<>();
        for (int f : rawFeatures)
            if (f != 0)
                features.add(f);
        temperature = hasFeature(UUIDS.FEATURE_TEMPERATURE);
        humidity = hasFeature(UUIDS.FEATURE_HUMIDITY);
        pressure = hasFeature(UUIDS.FEATURE_PRESSURE);
        accelerometer = hasFeature(UUIDS.FEATURE_ACCELEROMETER);
        gyroscope = hasFeature(UUIDS.FEATURE_GYROSCOPE);
        magnetometer = hasFeature(UUIDS.FEATURE_MAGNETOMETER);
        ambientLight = hasFeature(UUIDS.FEATURE_AMBIENT_LIGHT);
        proximity = hasFeature(UUIDS.FEATURE_PROXIMITY);
        proximityCalibration = hasFeature(UUIDS.FEATURE_PROXIMITY_CALIBRATION);
        rawGas = hasFeature(UUIDS.FEATURE_RAW_GAS);
        airQuality = hasFeature(UUIDS.FEATURE_AIR_QUALITY);
        gasSensor = rawGas || airQuality;
        button = hasFeature(UUIDS.FEATURE_BUTTON);
        sensorFusion = hasFeature(UUIDS.FEATURE_SENSOR_FUSION);
        integrationEngine = hasFeature(UUIDS.FEATURE_INTEGRATION_ENGINE);
        device.sflEnabled = sensorFusion;
        Log.d(TAG, "Sensor fusion: " + device.sflEnabled);
        valid = true;
    }

    public void processFeaturesCharacteristic(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);

        // Get features
        accelerometer = buffer.get() == 1;
        gyroscope = buffer.get() == 1;
        magnetometer = buffer.get() == 1;
        pressure = buffer.get() == 1;
        humidity = buffer.get() == 1;
        temperature = buffer.get() == 1;
        sensorFusion = buffer.get() == 1;
        device.sflEnabled = sensorFusion;
        Log.d(TAG, "Sensor fusion: " + device.sflEnabled);

        // Get device type
        if (buffer.array().length > 23) {
            device.type = buffer.get(23) & 0xff;
        } else {
            Log.d(TAG, "Features truncated, no device type available");
        }
        if (device.type == IotSensorsDevice.TYPE_UNKNOWN || device.type > IotSensorsDevice.TYPE_MAX) {
            Log.e(TAG, "Unsupported device type (" + device.type + "), assuming IoT dongle");
            device.type = IotSensorsDevice.TYPE_IOT_580;
        }
        Log.d(TAG, "Device type: " + device.type);

        // Get firmware version
        byte[] version = new byte[16];
        buffer.get(version);
        device.version = new String(version).trim();
        Log.d(TAG, "Version: " + device.version);

        if (device.type == IotSensorsDevice.TYPE_IOT_580) {
            device.iotNewFirmware = Utils.compareFirmwareVersion(device.version, "5.160.01.20") >= 0;
            Log.d(TAG, "IoT dongle firmware: " + (device.iotNewFirmware ? "new" : "old"));
        }

        if (device.type == IotSensorsDevice.TYPE_IOT_580 || device.type == IotSensorsDevice.TYPE_WEARABLE) {
            valid = true;
        }
    }

    public boolean hasFeature(int feature) {
        return features.contains(feature);
    }

    public boolean hasTemperature() {
        return temperature;
    }

    public boolean hasHumidity() {
        return humidity;
    }

    public boolean hasPressure() {
        return pressure;
    }

    public boolean hasAccelerometer() {
        return accelerometer;
    }

    public boolean hasGyroscope() {
        return gyroscope;
    }

    public boolean hasMagnetometer() {
        return magnetometer;
    }

    public boolean hasAmbientLight() {
        return ambientLight;
    }

    public boolean hasProximity() {
        return proximity;
    }

    public boolean hasProximityCalibration() {
        return proximityCalibration;
    }

    public boolean hasRawGas() {
        return rawGas;
    }

    public boolean hasAirQuality() {
        return airQuality;
    }

    public boolean hasGasSensor() {
        return gasSensor;
    }

    public boolean hasButton() {
        return button;
    }

    public boolean hasSensorFusion() {
        return sensorFusion;
    }

    public boolean hasIntegrationEngine() {
        return integrationEngine;
    }

    public boolean valid() {
        return valid;
    }
}
