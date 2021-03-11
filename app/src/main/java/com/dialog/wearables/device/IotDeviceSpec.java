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

import android.preference.PreferenceFragment;

import com.dialog.wearables.R;
import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.settings.CalibrationSettings;
import com.dialog.wearables.device.settings.IotDeviceFeatures;
import com.dialog.wearables.device.settings.IotDeviceSettings;
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
import com.dialog.wearables.settings.AccSettingsManager;
import com.dialog.wearables.settings.BasicSettingsManager;
import com.dialog.wearables.settings.CalibrationSettingsManager;
import com.dialog.wearables.settings.IotSettingsManager;
import com.dialog.wearables.settings.SflSettingsManager;

import java.util.regex.Pattern;

import min3d.core.Object3dContainer;

public abstract class IotDeviceSpec {
    protected static final String TAG = "IotDeviceSpec";

    public static IotDeviceSpec getDeviceSpec(IotSensorsDevice device) {
        switch (device.type) {
            case IotSensorsDevice.TYPE_IOT_580:
                return new IotDongle580(device);
            case IotSensorsDevice.TYPE_WEARABLE:
//                return new Wearable680(device);
                return new Wearable690(device);
            case IotSensorsDevice.TYPE_IOT_585:
                return new IotPlus585(device);
            default:
                throw new IllegalArgumentException("Unsupported device type");
        }
    }

    private static Pattern PATTERN_IOT_580 = Pattern.compile(".*IoT-DK.*", Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_WEARABLE = Pattern.compile(".*(?:WRBL|Wearable|TRKR|Tracker).*", Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_IOT_585 = Pattern.compile(".*(?:585|IoT-Plus|Multi|IoT\\+).*", Pattern.CASE_INSENSITIVE);
    private static Pattern PATTERN_RAW_PROJECT = Pattern.compile(".*-RAW.*", Pattern.CASE_INSENSITIVE);

    public static int getDeviceTypeFromAdvName(String name) {
        if (PATTERN_IOT_580.matcher(name).matches())
            return IotSensorsDevice.TYPE_IOT_580;
        if (PATTERN_WEARABLE.matcher(name).matches())
            return IotSensorsDevice.TYPE_WEARABLE;
        if (PATTERN_IOT_585.matcher(name).matches())
            return IotSensorsDevice.TYPE_IOT_585;
        return IotSensorsDevice.TYPE_IOT_580;
    }

    public static boolean isRawProjectFromAdvName(String name) {
        return PATTERN_RAW_PROJECT.matcher(name).matches();
    }

    public static String getProperNameFromAdvName(String name) {
        if (name.equalsIgnoreCase("IoT-DK-SFL") || name.equalsIgnoreCase("IoT-DK-RAW"))
            return "IoT Sensor DK";
        if (name.equalsIgnoreCase("Dialog WRBL"))
            return "Wearable DK";
        if (name.equalsIgnoreCase("Trkr"))
            return "INFOMARK Tracker";
        if (name.equalsIgnoreCase("IoT-585") || name.equalsIgnoreCase("IoT-Plus") || name.equalsIgnoreCase("IoT-Multi"))
            return "IoT Multi Sensor DK";
        return name;
    }

    public static int getDeviceIcon(int type) {
        switch (type) {
            case IotSensorsDevice.TYPE_IOT_580:
                return IotDongle580.ICON;
            case IotSensorsDevice.TYPE_WEARABLE:
//                return Wearable680.ICON;
                return Wearable690.ICON;
            case IotSensorsDevice.TYPE_IOT_585:
                return IotPlus585.ICON;
            default:
                return 0;
        }
    }

    protected IotSensorsDevice device;

    protected IotDeviceSpec(IotSensorsDevice device) {
        this.device = device;
    }

    public abstract int getDeviceType();
    public abstract boolean isNewVersion();
    public abstract boolean cloudSupport();

    public void startActivationSequence() {
        device.manager.readFeatures();
        device.manager.sendReadConfigCommand(); // before start
        device.manager.sendStartCommand();
        device.manager.sendCalReadCommand();
        device.manager.sendSflReadCommand();
    }

    public int getMainSensorLayout() {
        return 0;
    }

    public int getSecondarySensorLayout() {
        return 0;
    }

    public abstract Object3dContainer get3DModel();

    public abstract int get3DTexture();

    public TemperatureSensor getTemperatureSensor() {
        return null;
    }

    public HumiditySensor getHumiditySensor() {
        return null;
    }

    public PressureSensor getPressureSensor() {
        return null;
    }

    public Accelerometer getAccelerometer() {
        return null;
    }

    public AccelerometerIntegration getAccelerometerIntegration() {
        return null;
    }

    public Gyroscope getGyroscope() {
        return null;
    }

    public GyroscopeAngleIntegration getGyroscopeAngleIntegration() {
        return null;
    }

    public GyroscopeQuaternionIntegration getGyroscopeQuaternionIntegration() {
        return null;
    }

    public Magnetometer getMagnetometer() {
        return null;
    }

    public AmbientLightSensor getAmbientLightSensor() {
        return null;
    }

    public ProximitySensor getProximitySensor() {
        return null;
    }

    public GasSensor getGasSensor() {
        return null;
    }

    public AirQualitySensor getAirQualitySensor() {
        return null;
    }

    public SensorFusion getSensorFusion() {
        return null;
    }

    public ButtonSensor getButtonSensor() {
        return null;
    }

    public IotDeviceFeatures getFeatures() {
        return null;
    }

    public abstract int getBasicSettingsXml();

    public abstract int getCalibrationSettingsXml();

    public int getSensorFusionSettingsXml() {
        return R.xml.sfl_settings;
    }

    public int getAccelerometerSettingsXml() {
        return R.xml.acc_settings;
    }

    public IotSettingsManager getBasicSettingsManager(PreferenceFragment fragment) {
        return new BasicSettingsManager(device, fragment);
    }

    public IotSettingsManager getCalibrationSettingsManager(PreferenceFragment fragment) {
        return new CalibrationSettingsManager(device, fragment);
    }

    public IotSettingsManager getSensorFusionSettingsManager(PreferenceFragment fragment) {
        return new SflSettingsManager(device, fragment);
    }

    public IotSettingsManager getAccelerometerSettingsManager(PreferenceFragment fragment) {
        return new AccSettingsManager(device, fragment);
    }

    public IotDeviceSettings getBasicSettings() {
        return null;
    }

    public IotDeviceSettings getCalibrationModesSettings() {
        return null;
    }

    public IotDeviceSettings getProximityHysteresisSettings() {
        return null;
    }

    public CalibrationSettings getCalibrationSettings() {
        return null;
    }

    public IotDeviceSettings getSensorFusionSettings() {
        return null;
    }

    public int getCalibrationMode(int sensor) {
        return UUIDS.CALIBRATION_MODE_NONE;
    }

    public int getAutoCalibrationMode(int sensor) {
        return UUIDS.CALIBRATION_AUTO_MODE_BASIC;
    }

    public void processActuationEvent(DataEvent event) {
    }
}
