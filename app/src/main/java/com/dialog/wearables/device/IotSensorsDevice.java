/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.apis.internal.DataMsg;
import com.dialog.wearables.bluetooth.BluetoothManager;
import com.dialog.wearables.bluetooth.DataProcessor;
import com.dialog.wearables.bluetooth.async.Callback;
import com.dialog.wearables.controller.AccelerometerController;
import com.dialog.wearables.controller.CompassController;
import com.dialog.wearables.controller.GyroscopeController;
import com.dialog.wearables.device.settings.CalibrationSettings;
import com.dialog.wearables.device.settings.IotDeviceFeatures;
import com.dialog.wearables.device.settings.IotDeviceSettings;
import com.dialog.wearables.global.PointValueBuffer;
import com.dialog.wearables.global.PointValueBuffer3D;
import com.dialog.wearables.sensor.Accelerometer;
import com.dialog.wearables.sensor.AccelerometerIntegration;
import com.dialog.wearables.sensor.AirQualitySensor;
import com.dialog.wearables.sensor.AmbientLightSensor;
import com.dialog.wearables.sensor.ButtonSensor;
import com.dialog.wearables.sensor.GasSensor;
import com.dialog.wearables.sensor.Gyroscope;
import com.dialog.wearables.sensor.GyroscopeAngleIntegration;
import com.dialog.wearables.sensor.GyroscopeQuaternionIntegration;
import com.dialog.wearables.sensor.HumiditySensor;
import com.dialog.wearables.sensor.Magnetometer;
import com.dialog.wearables.sensor.PressureSensor;
import com.dialog.wearables.sensor.ProximitySensor;
import com.dialog.wearables.sensor.SensorFusion;
import com.dialog.wearables.sensor.TemperatureSensor;
import com.dialog.wearables.settings.IotSettingsManager;

import min3d.core.Object3dContainer;

public class IotSensorsDevice {
    private static final String TAG = "IotSensorsDevice";

    public static final int GRAPH_DATA_SIZE = 100;

    // Device type
    public static final int TYPE_IOT_580 = 0;
    public static final int TYPE_WEARABLE = 1;
    public static final int TYPE_IOT_585 = 2;
    public static final int TYPE_MAX = 2;
    public static final int TYPE_UNKNOWN = -1;

    // Connection state
    public static final int DISCONNECTED = 0;
    public static final int CONNECTING = 1;
    public static final int CONNECTED = 2;
    public static final int DISCONNECTING = 3;

    public int state = DISCONNECTED;
    public BluetoothDevice btDevice;
    public BluetoothManager manager;
    public BluetoothGatt gatt;
    public Callback callback;
    public DataProcessor dataProcessor;
    public Context context;
    public int type = IotSensorsDevice.TYPE_UNKNOWN;
    public IotDeviceSpec spec;
    public String name;
    public String address;
    public String version = "Unknown";
    public boolean iotNewFirmware = false;
    public boolean sflEnabled = false;
    public boolean isStarted = false;
    public int calibrationState = -1;
    public boolean oneShotModeSelected = false;
    public boolean oneShotMode = false;
    public boolean integrationEngine = false;

    public PointValueBuffer temperatureGraphData, pressureGraphData, humidityGraphData, ambientLightGraphData, airQualityGraphData;
    public PointValueBuffer3D accelerometerGraphData, gyroscopeGraphData, compassGraphData;

    public IotSensorsDevice(BluetoothDevice btDevice, int type, Context context) {
        this.btDevice = btDevice;
        this.type = type;
        this.context = context.getApplicationContext();
        initSpec();
        address = btDevice.getAddress();
        name = btDevice.getName();
        initSensorGraphs();
    }

    public void initSpec() {
        if (spec == null || type != spec.getDeviceType()) {
            spec = IotDeviceSpec.getDeviceSpec(this);
            int unit = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context).getString("prefTemperatureUnit", "0"));
            spec.getTemperatureSensor().setDisplayUnit(unit);
            spec.getTemperatureSensor().setLogUnit(unit);
        }
    }

    public IotDeviceSpec getSpec() {
        return spec;
    }

    public void initSensorGraphs() {
        temperatureGraphData = new PointValueBuffer(GRAPH_DATA_SIZE);
        pressureGraphData = new PointValueBuffer(GRAPH_DATA_SIZE);
        humidityGraphData = new PointValueBuffer(GRAPH_DATA_SIZE);
        if (type != TYPE_IOT_585) {
            accelerometerGraphData = new PointValueBuffer3D(GRAPH_DATA_SIZE);
            gyroscopeGraphData = new PointValueBuffer3D(GRAPH_DATA_SIZE);
            compassGraphData = new PointValueBuffer3D(GRAPH_DATA_SIZE);
        } else {
            accelerometerGraphData = new PointValueBuffer3D(2 * GRAPH_DATA_SIZE);
            gyroscopeGraphData = new PointValueBuffer3D(2 * GRAPH_DATA_SIZE);
            compassGraphData = new PointValueBuffer3D(2 * GRAPH_DATA_SIZE);
            ambientLightGraphData = new PointValueBuffer(GRAPH_DATA_SIZE);
            airQualityGraphData = new PointValueBuffer(GRAPH_DATA_SIZE);
        }
        if (spec.getAccelerometer() != null)
            spec.getAccelerometer().setGraphValueProcessor(new AccelerometerController.GraphValueProcessor());
        if (spec.getAccelerometerIntegration() != null)
            spec.getAccelerometerIntegration().setGraphValueProcessor(new AccelerometerController.GraphValueProcessor());
        if (spec.getGyroscope() != null)
            spec.getGyroscope().setGraphValueProcessor(new GyroscopeController.GraphValueProcessor());
        if (spec.getGyroscopeAngleIntegration() != null)
            spec.getGyroscopeAngleIntegration().setGraphValueProcessor(new GyroscopeController.GraphValueProcessor());
        if (spec.getMagnetometer() != null)
            spec.getMagnetometer().setGraphValueProcessor(new CompassController.GraphValueProcessor());
    }

    public void connect() {
        if (state != DISCONNECTED)
            return;
        state = CONNECTING;
        manager = new BluetoothManager(this);
        callback = new Callback(this);
        dataProcessor = new DataProcessor(this);
        gatt = btDevice.connectGatt(context, false, callback);
    }

    public void disconnect() {
        if (dataProcessor != null) {
            dataProcessor.getHandler().getLooper().quit();
        }
        if (manager != null) {
            manager.clearCommandQueue();
        }
        if (gatt != null) {
            gatt.disconnect();
        }
    }

    public void close() {
        state = DISCONNECTED;
        if (dataProcessor != null) {
            dataProcessor.getHandler().getLooper().quit();
        }
        if (manager != null) {
            manager.clearCommandQueue();
        }
        if (gatt != null) {
            gatt.close();
            gatt = null;
        }
    }

    public boolean isNewVersion() {
        return spec.isNewVersion();
    }

    public boolean cloudSupport() {
        return spec.cloudSupport();
    }

    public void startActivationSequence() {
        spec.startActivationSequence();
    }

    public int getMainSensorLayout() {
        return spec.getMainSensorLayout();
    }

    public int getSecondarySensorLayout() {
        return spec.getSecondarySensorLayout();
    }

    public Object3dContainer get3DModel() {
        return spec.get3DModel();
    }

    public int get3DTexture() {
        return spec.get3DTexture();
    }

    public TemperatureSensor getTemperatureSensor() {
        return spec.getTemperatureSensor();
    }

    public HumiditySensor getHumiditySensor() {
        return spec.getHumiditySensor();
    }

    public PressureSensor getPressureSensor() {
        return spec.getPressureSensor();
    }

    public Accelerometer getAccelerometer() {
        return spec.getAccelerometer();
    }

    public AccelerometerIntegration getAccelerometerIntegration() {
        return spec.getAccelerometerIntegration();
    }

    public Gyroscope getGyroscope() {
        return spec.getGyroscope();
    }

    public GyroscopeAngleIntegration getGyroscopeAngleIntegration() {
        return spec.getGyroscopeAngleIntegration();
    }

    public GyroscopeQuaternionIntegration getGyroscopeQuaternionIntegration() {
        return spec.getGyroscopeQuaternionIntegration();
    }

    public Magnetometer getMagnetometer() {
        return spec.getMagnetometer();
    }

    public AmbientLightSensor getAmbientLightSensor() {
        return spec.getAmbientLightSensor();
    }

    public ProximitySensor getProximitySensor() {
        return spec.getProximitySensor();
    }

    public GasSensor getGasSensor() {
        return spec.getGasSensor();
    }

    public AirQualitySensor getAirQualitySensor() {
        return spec.getAirQualitySensor();
    }

    public SensorFusion getSensorFusion() {
        return spec.getSensorFusion();
    }

    public ButtonSensor getButtonSensor() {
        return spec.getButtonSensor();
    }

    public IotDeviceFeatures getFeatures() {
        return spec.getFeatures();
    }

    public int getBasicSettingsXml() {
        return spec.getBasicSettingsXml();
    }

    public int getCalibrationSettingsXml() {
        return spec.getCalibrationSettingsXml();
    }

    public int getSensorFusionSettingsXml() {
        return spec.getSensorFusionSettingsXml();
    }

    public int getAccelerometerSettingsXml() {
        return spec.getAccelerometerSettingsXml();
    }

    public IotSettingsManager getBasicSettingsManager(PreferenceFragment fragment) {
        return spec.getBasicSettingsManager(fragment);
    }

    public IotSettingsManager getCalibrationSettingsManager(PreferenceFragment fragment) {
        return spec.getCalibrationSettingsManager(fragment);
    }

    public IotSettingsManager getSensorFusionSettingsManager(PreferenceFragment fragment) {
        return spec.getSensorFusionSettingsManager(fragment);
    }

    public IotSettingsManager getAccelerometerSettingsManager(PreferenceFragment fragment) {
        return spec.getAccelerometerSettingsManager(fragment);
    }

    public IotDeviceSettings getBasicSettings() {
        return spec.getBasicSettings();
    }

    public IotDeviceSettings getCalibrationModesSettings() {
        return spec.getCalibrationModesSettings();
    }

    public IotDeviceSettings getProximityHysteresisSettings() {
        return spec.getProximityHysteresisSettings();
    }

    public CalibrationSettings getCalibrationSettings() {
        return spec.getCalibrationSettings();
    }

    public IotDeviceSettings getSensorFusionSettings() {
        return spec.getSensorFusionSettings();
    }

    public int getCalibrationMode(int sensor) {
        return spec.getCalibrationMode(sensor);
    }

    public int getAutoCalibrationMode(int sensor) {
        return spec.getAutoCalibrationMode(sensor);
    }

    public void processActuationMessage(DataMsg msg) {
        if (!msg.EKID.equals(address))
            return;
        for(DataEvent event : msg.Events) {
            spec.processActuationEvent(event);
        }
    }
}
