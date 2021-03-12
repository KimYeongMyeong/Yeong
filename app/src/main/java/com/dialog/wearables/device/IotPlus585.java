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
import com.dialog.wearables.apis.common.eActuationTypes;
import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.settings.BasicSettingsIotPlus;
import com.dialog.wearables.device.settings.CalibrationModesSettings;
import com.dialog.wearables.device.settings.CalibrationSettings;
import com.dialog.wearables.device.settings.CalibrationSettingsV2;
import com.dialog.wearables.device.settings.IotDeviceFeatures;
import com.dialog.wearables.device.settings.IotDeviceSettings;
import com.dialog.wearables.device.settings.ProximityHysteresisSettings;
import com.dialog.wearables.device.settings.SensorFusionSettings;
import com.dialog.wearables.global.Object3DLoader;
import com.dialog.wearables.sensor.Accelerometer;
import com.dialog.wearables.sensor.AccelerometerIntegration;
import com.dialog.wearables.sensor.AirQualitySensor;
import com.dialog.wearables.sensor.AmbientLightSensor;
import com.dialog.wearables.sensor.GasSensor;
import com.dialog.wearables.sensor.Gyroscope;
import com.dialog.wearables.sensor.GyroscopeAngleIntegration;
import com.dialog.wearables.sensor.GyroscopeQuaternionIntegration;
import com.dialog.wearables.sensor.HumiditySensor;
import com.dialog.wearables.sensor.Magnetometer;
import com.dialog.wearables.sensor.PressureSensor;
import com.dialog.wearables.sensor.ProximitySensor;
import com.dialog.wearables.sensor.TemperatureSensor;
import com.dialog.wearables.sensor.chip.AK09915C;
import com.dialog.wearables.sensor.chip.BME680;
import com.dialog.wearables.sensor.chip.ICM40605;
import com.dialog.wearables.sensor.chip.VCNL4010;
import com.dialog.wearables.settings.BasicSettingsManagerIotPlus;
import com.dialog.wearables.settings.IotSettingsManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import min3d.core.Object3dContainer;

public class IotPlus585 extends IotDeviceSpec {

    public static final int ICON = R.drawable.icon_585;

    @Override
    public int getDeviceType() {
        return IotSensorsDevice.TYPE_IOT_585;
    }

    @Override
    public boolean isNewVersion() {
        return true;
    }

    @Override
    public boolean cloudSupport() {
        return true;
    }

    private static class SensorFusion extends com.dialog.wearables.sensor.SensorFusion {

        private int[] raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = get4DValuesLE(data, offset);
            qx = raw[0] / 32768.f;
            qy = raw[1] / 32768.f;
            qz = raw[2] / 32768.f;
            qw = raw[3] / 32768.f;
            quaternion = new Value4D(qx, qy, qz, qw);
            sensorFusionCalculation();
            return value;
        }
    }

    private static class ButtonSensor extends com.dialog.wearables.sensor.ButtonSensor {

        @Override
        public Value processRawData(byte[] data, int offset) {
            id = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset + 2);
            state = data[offset] != 0;
            value = new Value(state ? 1 : 0);
            return value;
        }
    }

    protected BME680 environmental;
    protected ICM40605 imu;
    protected AK09915C magneto;
    protected VCNL4010 light;
    protected SensorFusion fusion;
    protected AccelerometerIntegration accelerometerIntegration;
    protected GyroscopeAngleIntegration gyroscopeAngleIntegration;
    protected GyroscopeQuaternionIntegration gyroscopeQuaternionIntegration;
    protected ButtonSensor button;
    protected IotDeviceFeatures features;
    protected BasicSettingsIotPlus basicSettings;
    protected CalibrationModesSettings calibrationModesSettings;
    protected ProximityHysteresisSettings proximityHysteresisSettings;
    protected CalibrationSettingsV2 calibrationSettings;
    protected SensorFusionSettings sensorFusionSettings;

    public IotPlus585(IotSensorsDevice device) {
        super(device);
        environmental = new BME680();
        imu = new ICM40605();
        magneto = new AK09915C();
        light = new VCNL4010();
        fusion = new SensorFusion();
        accelerometerIntegration = new AccelerometerIntegration();
        gyroscopeAngleIntegration = new GyroscopeAngleIntegration();
        gyroscopeQuaternionIntegration = new GyroscopeQuaternionIntegration();
        button = new ButtonSensor();
        features = new IotDeviceFeatures(device);
        basicSettings = new BasicSettingsIotPlus(device);
        calibrationModesSettings = new CalibrationModesSettings(device);
        proximityHysteresisSettings = new ProximityHysteresisSettings(device);
        calibrationSettings = new CalibrationSettingsV2(device);
        sensorFusionSettings = new SensorFusionSettings(device);

        basicSettings.setProcessCallback(new IotDeviceSettings.ProcessCallback() {
            @Override
            public void onProcess() {
                int accRange = basicSettings.accRange & 0xFF;
                int accRate = basicSettings.accRate & 0xFF;
                int gyroRange = basicSettings.gyroRange & 0xFF;
                int gyroRate = basicSettings.gyroRate & 0xFF;
                int sflRate = basicSettings.sflRate & 0xFF;

                imu.processAccelerometerRawConfig(accRange);

                boolean sfl = IotPlus585.this.device.sflEnabled && basicSettings.sflEnabled;
                imu.processGyroscopeRawConfig(gyroRange, !sfl ? gyroRate : 0);
                if (sfl) {
                    getGyroscope().setRate(sflRate);
                }

                IotPlus585.this.device.integrationEngine = IotPlus585.this.device.getFeatures().hasIntegrationEngine() && basicSettings.rawDataType == 2;
                if (IotPlus585.this.device.integrationEngine) {
                    accelerometerIntegration.setRate(sflRate, imu.getAccelerometerRateFromRawConfig(accRate));
                    gyroscopeAngleIntegration.setRate(sflRate, imu.getGyrescopeRateFromRawConfig(gyroRate));
                    gyroscopeQuaternionIntegration.setRate(sflRate, imu.getGyrescopeRateFromRawConfig(gyroRate));
                }
            }
        });
    }

    @Override
    public void startActivationSequence() {
        device.manager.readFeatures(); // old
        device.manager.sendReadFeaturesCommand(); // new
        device.manager.sendReadVersionCommand();
        device.manager.sendReadConfigCommand(); // before start
        device.manager.sendStartCommand();
        device.manager.sendCalReadCommand();
        device.manager.sendSflReadCommand();
        device.manager.sendReadCalibrationModesCommand();
        device.manager.sendReadProximityHysteresisCommand();
    }

    @Override
    public int getMainSensorLayout() {
        return R.layout.fragment_sensor_env;
    }

    @Override
    public int getSecondarySensorLayout() {
        return R.layout.fragment_sensor_imu;
    }

    @Override
    public Object3dContainer get3DModel() {
        return Object3DLoader.iot585;
    }

    @Override
    public int get3DTexture() {
        return R.drawable.iot585_texture;
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
    public AccelerometerIntegration getAccelerometerIntegration() {
        return accelerometerIntegration;
    }

    @Override
    public Gyroscope getGyroscope() {
        return imu.getGyroscope();
    }

    @Override
    public GyroscopeAngleIntegration getGyroscopeAngleIntegration() {
        return gyroscopeAngleIntegration;
    }

    @Override
    public GyroscopeQuaternionIntegration getGyroscopeQuaternionIntegration() {
        return gyroscopeQuaternionIntegration;
    }

    @Override
    public Magnetometer getMagnetometer() {
        return magneto.getMagnetometer();
    }

    @Override
    public AmbientLightSensor getAmbientLightSensor() {
        return light.getAmbientLightSensor();
    }

    @Override
    public ProximitySensor getProximitySensor() {
        return light.getProximitySensor();
    }

    @Override
    public GasSensor getGasSensor() {
        return environmental.getGasSensor();
    }

    @Override
    public AirQualitySensor getAirQualitySensor() {
        return environmental.getAirQualitySensor();
    }

    @Override
    public SensorFusion getSensorFusion() {
        return fusion;
    }

    @Override
    public ButtonSensor getButtonSensor() {
        return button;
    }

    @Override
    public IotDeviceFeatures getFeatures() {
        return features;
    }

    @Override
    public int getBasicSettingsXml() {
        return R.xml.basic_settings_iot_plus;
    }

    @Override
    public IotSettingsManager getBasicSettingsManager(PreferenceFragment fragment) {
        return new BasicSettingsManagerIotPlus(device, fragment);
    }

    @Override
    public IotDeviceSettings getBasicSettings() {
        return basicSettings;
    }

    @Override
    public CalibrationModesSettings getCalibrationModesSettings() {
        return calibrationModesSettings;
    }

    @Override
    public ProximityHysteresisSettings getProximityHysteresisSettings() {
        return proximityHysteresisSettings;
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
        switch (sensor) {
            case UUIDS.SENSOR_TYPE_ACCELEROMETER:
                return calibrationModesSettings.accCalMode;
            case UUIDS.SENSOR_TYPE_GYROSCOPE:
                return calibrationModesSettings.gyroCalMode;
            case UUIDS.SENSOR_TYPE_MAGNETOMETER:
                return calibrationModesSettings.magnetoCalMode;
            default:
                return UUIDS.CALIBRATION_MODE_NONE;
        }
    }

    @Override
    public int getAutoCalibrationMode(int sensor) {
        switch (sensor) {
            case UUIDS.SENSOR_TYPE_ACCELEROMETER:
                return calibrationModesSettings.accAutoCalMode;
            case UUIDS.SENSOR_TYPE_GYROSCOPE:
                return calibrationModesSettings.gyroAutoCalMode;
            case UUIDS.SENSOR_TYPE_MAGNETOMETER:
                return calibrationModesSettings.magnetoAutoCalMode;
            default:
                return UUIDS.CALIBRATION_AUTO_MODE_BASIC;
        }
    }

    @Override
    public void processActuationEvent(DataEvent event) {
        if (event.EventType != eActuationTypes.Leds)
            return;
        switch (event.Data) {
            case "true":
                device.manager.sendStartLedBlinkCommand();
                break;
            case "false":
                device.manager.sendStopLedBlinkCommand();
                break;
        }
    }
}
