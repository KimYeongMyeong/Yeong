/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.apis.common.eEventTypes;
import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.apis.internal.DataMsg;
import com.dialog.wearables.cloud.DataManager;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.defines.StatusUpdates;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.sensor.IotSensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class DataProcessor {
    private static final String TAG = "DataProcessor";

    private static final int MESSAGE_SENSOR_REPORT = 0;
    private static final int MAX_SENSOR_REPORT_DELAY = 100;

    private static final HashMap<Integer, Integer> sensorReportToEventType = new HashMap<>();
    static {
        sensorReportToEventType.put(UUIDS.REPORT_TEMPERATURE, eEventTypes.Temperature);
        sensorReportToEventType.put(UUIDS.REPORT_HUMIDITY, eEventTypes.Humidity);
        sensorReportToEventType.put(UUIDS.REPORT_PRESSURE, eEventTypes.Pressure);
        sensorReportToEventType.put(UUIDS.REPORT_ACCELEROMETER, eEventTypes.Accelerometer);
        sensorReportToEventType.put(UUIDS.REPORT_GYROSCOPE, eEventTypes.Gyroscope);
        sensorReportToEventType.put(UUIDS.REPORT_MAGNETOMETER, eEventTypes.Magnetometer);
        sensorReportToEventType.put(UUIDS.REPORT_SENSOR_FUSION, eEventTypes.Fusion);
        sensorReportToEventType.put(UUIDS.REPORT_AMBIENT_LIGHT, eEventTypes.Brightness);
        sensorReportToEventType.put(UUIDS.REPORT_PROXIMITY, eEventTypes.Proximity);
        sensorReportToEventType.put(UUIDS.REPORT_GAS, eEventTypes.Gas);
        sensorReportToEventType.put(UUIDS.REPORT_AIR_QUALITY, eEventTypes.AirQuality);
        sensorReportToEventType.put(UUIDS.REPORT_BUTTON, eEventTypes.Button);
    }

    private static final HashSet<Integer> highRateSensorReports = new HashSet<>();
    static {
        highRateSensorReports.add(UUIDS.REPORT_ACCELEROMETER);
        highRateSensorReports.add(UUIDS.REPORT_GYROSCOPE);
        highRateSensorReports.add(UUIDS.REPORT_MAGNETOMETER);
        highRateSensorReports.add(UUIDS.REPORT_SENSOR_FUSION);
        highRateSensorReports.add(UUIDS.REPORT_VELOCITY_DELTA);
        highRateSensorReports.add(UUIDS.REPORT_EULER_ANGLE_DELTA);
        highRateSensorReports.add(UUIDS.REPORT_QUATERNION_DELTA);
    }

    private IotSensorsApplication application;
    private IotSensorsDevice device;
    private Handler handler;

    private static class SensorReport {

        long timestamp;
        byte[] data;
        boolean multi;

        SensorReport(byte[] data, boolean multi) {
            this.data = data;
            this.multi = multi;
            timestamp = System.currentTimeMillis();
        }
    }

    public DataProcessor(IotSensorsDevice device) {
        this.device = device;
        application = IotSensorsApplication.getApplication();

        HandlerThread handlerThread = new HandlerThread("DataProcessor");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                SensorReport report = (SensorReport) msg.obj;
                long delay = System.currentTimeMillis() - report.timestamp;
                //Log.d(TAG, "Report delay: " + delay);
                processSensorReport(report.data, report.multi, delay > MAX_SENSOR_REPORT_DELAY);
            }
        };
    }

    public Handler getHandler() {
        return handler;
    }

    public void processSensorReportBackground(byte[] data, boolean multi) {
        handler.sendMessage(handler.obtainMessage(MESSAGE_SENSOR_REPORT, new SensorReport(data, multi)));
    }

    public void processSensorReport(byte[] data, boolean multi) {
        processSensorReport(data, multi, false);
    }

    public void processSensorReport(byte[] data, boolean multi, boolean minimal) {
        IotSensor sensor;
        DataMsg dataMsg = new DataMsg(device.address);

        int curr = 0;
        if (multi) {
            if ((data[0] & 0xff) != UUIDS.MULTI_SENSOR_REPORT_PREAMBLE)
                return;
            //Log.d(TAG, "Multi sensor report: timestamp=" + (data[1] & 0xff));
            curr = 2;
        }

        ArrayList<Integer> reports = new ArrayList<>(10);
        while (curr < data.length) {

            int reportID = data[curr] & 0xff;
            reports.add(reportID);
            sensor = null;

            switch (reportID) {

                // IMU sensors

                case UUIDS.REPORT_ACCELEROMETER:
                    sensor = device.getAccelerometer();
                    sensor.processRawData(data, curr + 3);
                    device.accelerometerGraphData.add(sensor.getGraphValue());
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_ACCELEROMETER_LENGTH;
                    break;

                case UUIDS.REPORT_GYROSCOPE:
                    sensor = device.getGyroscope();
                    sensor.processRawData(data, curr + 3);
                    device.gyroscopeGraphData.add(sensor.getGraphValue());
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_GYROSCOPE_LENGTH;
                    break;

                case UUIDS.REPORT_MAGNETOMETER:
                    device.calibrationState = data[curr + 2] & 0xFF;

                    // Check for one-shot calibration completion
                    if (device.oneShotMode && device.calibrationState == 4) {
                        Log.d(TAG, "One-shot calibration complete");
                        device.oneShotMode = false;
                        device.oneShotModeSelected = false;
                        Intent intent = new Intent(BroadcastUpdate.SENSOR_DATA_UPDATE);
                        intent.putExtra("status", StatusUpdates.STATUS_ONE_SHOT_CALIBRATION_COMPLETE);
                        LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
                    }

                    Intent intent = new Intent();
                    intent.setAction(BroadcastUpdate.STATUS_RECEIVER);
                    intent.putExtra("sensor", UUIDS.REPORT_MAGNETOMETER);
                    intent.putExtra("calibrationState", device.calibrationState);
                    intent.putExtra("sensorState", data[curr + 1] & 0xFF);
                    LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);

                    sensor = device.getMagnetometer();
                    sensor.processRawData(data, curr + 3);
                    device.compassGraphData.add(sensor.getGraphValue());
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_MAGNETOMETER_LENGTH;
                    break;

                case UUIDS.REPORT_SENSOR_FUSION:
                    sensor = device.getSensorFusion();
                    sensor.processRawData(data, curr + 3);
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_SENSOR_FUSION_LENGTH;
                    break;

                case UUIDS.REPORT_VELOCITY_DELTA:
                    sensor = device.getAccelerometerIntegration();
                    sensor.processRawData(data, curr + 2);
                    device.accelerometerGraphData.add(sensor.getGraphValue());
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_VELOCITY_DELTA_LENGTH;
                    break;

                case UUIDS.REPORT_EULER_ANGLE_DELTA:
                    sensor = device.getGyroscopeAngleIntegration();
                    sensor.processRawData(data, curr + 2);
                    device.gyroscopeGraphData.add(sensor.getGraphValue());
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_EULER_ANGLE_DELTA_LENGTH;
                    break;

                case UUIDS.REPORT_QUATERNION_DELTA:
                    sensor = device.getGyroscopeQuaternionIntegration();
                    sensor.processRawData(data, curr + 3);
                    if (!minimal)
                        application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_QUATERNION_DELTA_LENGTH;
                    break;

                // Environmental Sensors

                case UUIDS.REPORT_PRESSURE:
                    sensor = device.getPressureSensor();
                    sensor.processRawData(data, curr + 3);
                    device.pressureGraphData.add(sensor.getGraphValue());
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_PRESSURE_LENGTH;
                    break;

                case UUIDS.REPORT_HUMIDITY:
                    sensor = device.getHumiditySensor();
                    sensor.processRawData(data, curr + 3);
                    device.humidityGraphData.add(sensor.getGraphValue());
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_HUMIDITY_LENGTH;
                    break;

                case UUIDS.REPORT_TEMPERATURE:
                    sensor = device.getTemperatureSensor();
                    sensor.processRawData(data, curr + 3);
                    device.temperatureGraphData.add(sensor.getGraphValue());
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_TEMPERATURE_LENGTH;
                    break;

                case UUIDS.REPORT_GAS:
                    sensor = device.getGasSensor();
                    sensor.processRawData(data, curr + 3);
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_GAS_LENGTH;
                    break;

                case UUIDS.REPORT_AIR_QUALITY:
                    sensor = device.getAirQualitySensor();
                    sensor.processRawData(data, curr + 2);
                    device.airQualityGraphData.add(sensor.getGraphValue());
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_AIR_QUALITY_LENGTH;
                    break;

                case UUIDS.REPORT_AMBIENT_LIGHT:
                    sensor = device.getAmbientLightSensor();
                    sensor.processRawData(data, curr + 1);
                    device.ambientLightGraphData.add(sensor.getGraphValue());
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_AMBIENT_LIGHT_LENGTH;
                    break;

                case UUIDS.REPORT_PROXIMITY:
                    sensor = device.getProximitySensor();
                    sensor.processRawData(data, curr + 1);
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_PROXIMITY_LENGTH;
                    break;

                // Other

                case UUIDS.REPORT_BUTTON:
                    sensor = device.getButtonSensor();
                    sensor.processRawData(data, curr + 1);
                    application.logger.info(String.format("\t%s\t%s", sensor.getLogTag(), sensor.getLogEntry()));
                    curr += UUIDS.REPORT_BUTTON_LENGTH;
                    break;

                default:
                    Log.e(TAG, "Unknown sensor report: " + reportID);
                    reports.remove(reports.size() - 1);
                    multi = false;
                    break;
            }

            // Add data event
            if (device.cloudSupport() && sensor != null && sensorReportToEventType.containsKey(reportID) && (!minimal || !highRateSensorReports.contains(reportID))) {
                dataMsg.Events.add(new DataEvent(sensorReportToEventType.get(reportID), sensor.getCloudData()));
            }

            if (!multi)
                break;
        }

        // Inform UI
        if (!reports.isEmpty()) {
            Intent intent = new Intent(BroadcastUpdate.SENSOR_REPORT);
            intent.putIntegerArrayListExtra("id", reports);
            LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
        }

        // Send to cloud
        if (!dataMsg.Events.isEmpty()) {
            DataManager.sendDataMsg(device.context, dataMsg);
        }
    }

    public void processConfigurationReport(byte[] data) {
        Log.d(TAG, "processConfigurationReport");

        int command = data[1] & 0xff;
        switch (command) {
            case UUIDS.WEARABLES_COMMAND_CONFIGURATION_START:
            case UUIDS.WEARABLES_COMMAND_CONFIGURATION_STOP:
            case UUIDS.WEARABLES_COMMAND_CONFIGURATION_RUNNING_STATE:
                device.isStarted = data[2] == 1;
                Log.d(TAG, "Sensors " + (device.isStarted ? "started" : "stopped"));

                if (device.oneShotModeSelected && device.isStarted) {
                    Log.d(TAG, "One-shot calibration mode detected");
                    device.oneShotMode = true;
                }

                // Check for one-shot calibration completion. Case of IoT dongle in one-shot basic: the dongle stops after sending the OK state.
                if (device.type == IotSensorsDevice.TYPE_IOT_580 && device.oneShotMode && command == UUIDS.WEARABLES_COMMAND_CONFIGURATION_RUNNING_STATE && !device.isStarted && device.calibrationState == 3) {
                    Log.d(TAG, "One-shot calibration complete");
                    device.oneShotMode = false;
                    device.oneShotModeSelected = false;
                    Intent intent = new Intent(BroadcastUpdate.SENSOR_DATA_UPDATE);
                    intent.putExtra("status", StatusUpdates.STATUS_ONE_SHOT_CALIBRATION_COMPLETE);
                    LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
                }
                break;

            case UUIDS.WEARABLES_COMMAND_CONFIGURATION_READ:
                // Check calibration mode for one-shot
                if (device.type != IotSensorsDevice.TYPE_IOT_585 && device.isNewVersion()) {
                    device.oneShotModeSelected = data[11] == 3;
                    if (device.oneShotModeSelected) {
                        Log.d(TAG, "One-shot calibration mode selected");
                        if (device.isStarted) {
                            Log.d(TAG, "One-shot calibration mode detected");
                            device.oneShotMode = true;
                        }
                    } else {
                        device.oneShotMode = false;
                    }
                }

                device.getBasicSettings().process(data, 2);
                break;

            case UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_CONTROL:
                device.getCalibrationSettings().process(data, 2);
                break;

            case UUIDS.WEARABLES_COMMAND_SFL_READ:
                device.getSensorFusionSettings().process(data, 2);
                break;

            case UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_MODES:
                device.getCalibrationModesSettings().process(data, 2);
                break;

            case UUIDS.WEARABLES_COMMAND_PROXIMITY_HYSTERESIS_READ:
                device.getProximityHysteresisSettings().process(data, 2);
                break;

            case UUIDS.WEARABLES_COMMAND_CALIBRATION_COMPLETE: {
                int sensor = data[2] & 0xff;
                boolean ok = data[3] == 0;
                Log.d(TAG, "One-shot calibration complete: Sensor " + sensor + (ok ? "" : ", error"));
                Intent intent = new Intent(BroadcastUpdate.SENSOR_DATA_UPDATE);
                intent.putExtra("status", StatusUpdates.STATUS_ONE_SHOT_CALIBRATION_COMPLETE);
                intent.putExtra("sensor", sensor);
                intent.putExtra("ok", ok);
                LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
                break;
            }

            case UUIDS.WEARABLES_COMMAND_READ_VERSION:
                device.version = new String(Arrays.copyOfRange(data, 2, data.length));
                Log.d(TAG, "Version: " + device.version);
                break;

            case UUIDS.WEARABLES_COMMAND_READ_FEATURES: {
                Log.d(TAG, "Features: " + Arrays.toString(Arrays.copyOfRange(data, 2, data.length)));
                device.getFeatures().processFeaturesReport(data, 2);
                Intent intent = new Intent(BroadcastUpdate.SENSOR_DATA_UPDATE);
                intent.putExtra("status", StatusUpdates.STATUS_FEATURES_READ);
                LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
                break;
            }
        }
    }

    public void processFeaturesCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "processFeaturesCharacteristic: " + Arrays.toString(characteristic.getValue()));

        device.getFeatures().processFeaturesCharacteristic(characteristic.getValue());
        device.initSpec();

        Intent intent = new Intent(BroadcastUpdate.SENSOR_DATA_UPDATE);
        intent.putExtra("status", StatusUpdates.STATUS_FEATURES_READ);
        LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
    }
}
