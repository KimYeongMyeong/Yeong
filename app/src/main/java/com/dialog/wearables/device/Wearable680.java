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

import com.dialog.wearables.R;
import com.dialog.wearables.device.settings.BasicSettings;
import com.dialog.wearables.device.settings.CalibrationSettings;
import com.dialog.wearables.device.settings.CalibrationSettingsV2;
import com.dialog.wearables.device.settings.IotDeviceFeatures;
import com.dialog.wearables.device.settings.IotDeviceSettings;
import com.dialog.wearables.device.settings.SensorFusionSettings;
import com.dialog.wearables.global.Object3DLoader;
import com.dialog.wearables.sensor.Accelerometer;
import com.dialog.wearables.sensor.Gyroscope;
import com.dialog.wearables.sensor.HumiditySensor;
import com.dialog.wearables.sensor.Magnetometer;
import com.dialog.wearables.sensor.PressureSensor;
import com.dialog.wearables.sensor.TemperatureSensor;
import com.dialog.wearables.sensor.chip.BME280;
import com.dialog.wearables.sensor.chip.BMI160;
import com.dialog.wearables.sensor.chip.BMM150;

import min3d.core.Object3dContainer;

public class Wearable680 extends IotDeviceSpec {

    public static final int ICON = R.drawable.icon_680;

    @Override
    public int getDeviceType() {
        return IotSensorsDevice.TYPE_WEARABLE;
    }

    @Override
    public boolean isNewVersion() {
        return true;
    }

    @Override
    public boolean cloudSupport() {
        return false;
    }

    private static final float[] SENSOR_FUSION_RATES = new float[] { 0.78f, 1.56f, 3.12f, 6.25f, 12.5f, 25.f, 50.f };

    private static class SensorFusion extends com.dialog.wearables.sensor.SensorFusion {

        private int[] raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = get4DValuesLE(data, offset);
            qx = -raw[0] / 32768.f;
            qy = -raw[1] / 32768.f;
            qz = raw[2] / 32768.f;
            qw = raw[3] / 32768.f;
            quaternion = new Value4D(qx, qy, qz, qw);
            sensorFusionCalculation();
            return value;
        }
    }

    protected BME280 environmental;
    protected BMI160 imu;
    protected BMM150 magneto;
    protected SensorFusion fusion;
    protected IotDeviceFeatures features;
    protected BasicSettings basicSettings;
    protected SensorFusionSettings sensorFusionSettings;
    protected CalibrationSettingsV2 calibrationSettings;

    public Wearable680(IotSensorsDevice device) {
        super(device);
        environmental = new BME280();
        imu = new BMI160();
        magneto = new BMM150();
        fusion = new SensorFusion();
        features = new IotDeviceFeatures(device);
        basicSettings = new BasicSettings(device);
        calibrationSettings = new CalibrationSettingsV2(device);
        sensorFusionSettings = new SensorFusionSettings(device);

        basicSettings.setProcessCallback(new IotDeviceSettings.ProcessCallback() {
            @Override
            public void onProcess() {
                int accRange = basicSettings.accRange & 0xFF;
                int gyroRange = basicSettings.gyroRange & 0xFF;
                int gyroRate = basicSettings.gyroRate & 0xFF;
                int sflRate = basicSettings.sflRate & 0xFF;

                imu.processAccelerometerRawConfig(accRange);

                boolean sfl = Wearable680.this.device.sflEnabled && basicSettings.sflEnabled;
                imu.processGyroscopeRawConfig(gyroRange, !sfl ? gyroRate : 0);
                if (sfl) {
                    getGyroscope().setRate(SENSOR_FUSION_RATES[sflRate - 1]);
                }
            }
        });
    }

    @Override
    public int getMainSensorLayout() {
        return R.layout.fragment_sensor;
    }

    @Override
    public Object3dContainer get3DModel() {
        return Object3DLoader.watch;
    }

    @Override
    public int get3DTexture() {
        return R.drawable.watch_texture;
    }

    @Override
    public TemperatureSensor getTemperatureSensor() {
        return environmental.getTemperatureSensor();
    }

    @Override
    public HumiditySensor getHumiditySensor() {
        return environmental.getHumiditySensor();
    }

    @Override
    public PressureSensor getPressureSensor() {
        return environmental.getPressureSensor();
    }

    @Override
    public Accelerometer getAccelerometer() {
        return imu.getAccelerometer();
    }

    @Override
    public Gyroscope getGyroscope() {
        return imu.getGyroscope();
    }

    @Override
    public Magnetometer getMagnetometer() {
        return magneto.getMagnetometer();
    }

    @Override
    public SensorFusion getSensorFusion() {
        return fusion;
    }

    @Override
    public IotDeviceFeatures getFeatures() {
        return features;
    }

    @Override
    public int getBasicSettingsXml() {
        return R.xml.basic_settings_wearable;
    }

    @Override
    public BasicSettings getBasicSettings() {
        return basicSettings;
    }

    @Override
    public int getCalibrationSettingsXml() {
        return R.xml.calibration_settings_v2;
    }

    @Override
    public CalibrationSettings getCalibrationSettings() {
        return calibrationSettings;
    }

    @Override
    public SensorFusionSettings getSensorFusionSettings() {
        return sensorFusionSettings;
    }

    @Override
    public int getCalibrationMode(int sensor) {
        return basicSettings.calMode;
    }

    @Override
    public int getAutoCalibrationMode(int sensor) {
        return basicSettings.autoCalMode;
    }
}
