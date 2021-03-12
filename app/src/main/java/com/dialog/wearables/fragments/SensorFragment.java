/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dialog.wearables.R;
import com.dialog.wearables.activities.MenuHolderActivity;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.controller.AccelerometerController;
import com.dialog.wearables.controller.AirQualityController;
import com.dialog.wearables.controller.AmbientLightController;
import com.dialog.wearables.controller.CompassController;
import com.dialog.wearables.controller.GyroscopeController;
import com.dialog.wearables.controller.HumidityController;
import com.dialog.wearables.controller.IotSensorController;
import com.dialog.wearables.controller.PressureController;
import com.dialog.wearables.controller.ProximityController;
import com.dialog.wearables.controller.TemperatureController;
import com.dialog.wearables.controller.ThreeDimensionController;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.device.IotSensorsDevice;

/**
 * Fragment for the 6 sensor overview
 */
public class SensorFragment extends Fragment {
    public static final String TAG = "SensorFragment";

    private IotSensorsDevice device;
    private BroadcastReceiver statusReceiver, sensorReportReceiver, configurationReportReceiver;
    private boolean hasTemperature, hasHumidity, hasPressure, hasCompass, hasGyroscope, hasAccelerometer, hasAmbientLight, hasAirQuality, hasProximity, hasButton;
    private IotSensorController temperatureController, humidityController, pressureController, ambientLightController, proximityController, airQualityController;
    private ThreeDimensionController compassController, gyroscopeController, accelerometerController;
    private ImageView magCalibratedImage, magWarningImage, magnetoStateOverlay, buttonOverlay;
    private TextView accelerometerIntegrationLabel, gyroscopeIntegrationLabel;
    private int calibrationState = -1;
    private boolean showCalibrationState = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        device = ((MenuHolderActivity) getActivity()).getDevice();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        int layout = device.getMainSensorLayout();
        if (getArguments() != null)
            layout = getArguments().getInt("layout", device.getMainSensorLayout());
        View fragmentView = inflater.inflate(layout, container, false);

        hasTemperature = fragmentView.findViewById(R.id.temperatureView) != null;
        hasHumidity = fragmentView.findViewById(R.id.humidityView) != null;
        hasPressure = fragmentView.findViewById(R.id.pressureView) != null;
        hasCompass = fragmentView.findViewById(R.id.compassView) != null;
        hasGyroscope = fragmentView.findViewById(R.id.gyroscopeView) != null;
        hasAccelerometer = fragmentView.findViewById(R.id.accelerometerView) != null;
        hasAmbientLight = fragmentView.findViewById(R.id.ambientLightView) != null;
        hasAirQuality = fragmentView.findViewById(R.id.airQualityView) != null;
        hasProximity = fragmentView.findViewById(R.id.proximityView) != null;
        hasButton = fragmentView.findViewById(R.id.buttonOverlay) != null;

        magCalibratedImage = (ImageView) fragmentView.findViewById(R.id.calibratedImage);
        magWarningImage = (ImageView) fragmentView.findViewById(R.id.magWarningImage);
        magnetoStateOverlay = (ImageView) fragmentView.findViewById(R.id.magOverlayImage);
        calibrationState = -1;
        showCalibrationState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("calStateOverlay", true);

        buttonOverlay = (ImageView) fragmentView.findViewById(R.id.buttonOverlay);
        if (hasButton)
            buttonOverlay.setVisibility(device.getButtonSensor().isPressed() ? View.VISIBLE : View.GONE);

        accelerometerIntegrationLabel = (TextView) fragmentView.findViewById(R.id.accelerometerIntegration);
        gyroscopeIntegrationLabel = (TextView) fragmentView.findViewById(R.id.gyroscopeIntegration);

        if (hasCompass) {
            statusReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (device.isNewVersion() && intent.getIntExtra("sensor", 0) == 3) {
                        int sensorState = intent.getIntExtra("sensorState", 0);

                        // Calibration
                        if ((sensorState & 0x04) == 0x04) {
                            magCalibratedImage.setVisibility(View.INVISIBLE);
                        } else {
                            magCalibratedImage.setVisibility(View.VISIBLE);
                        }

                        // Warning
                        if ((sensorState & 0x01) == 0x01 && (sensorState & 0x02) == 0x02) {
                            magWarningImage.setVisibility(View.INVISIBLE);
                        } else {
                            magWarningImage.setVisibility(View.VISIBLE);
                        }

                        // Magneto state overlay
                        if (!showCalibrationState)
                            return;
                        int oldCalibrationState = calibrationState;
                        calibrationState = intent.getIntExtra("calibrationState", 0);
                        if (calibrationState == oldCalibrationState)
                            return;
                        magnetoStateOverlay.setVisibility(View.VISIBLE);
                        switch (calibrationState) {
                            case 0: // DISABLED
                                magnetoStateOverlay.setImageResource(R.drawable.mag_disabled);
                                break;
                            case 1: // INIT
                                magnetoStateOverlay.setImageResource(R.drawable.mag_init);
                                break;
                            case 2: // BAD
                                magnetoStateOverlay.setImageResource(R.drawable.mag_bad);
                                break;
                            case 3: // OK
                                magnetoStateOverlay.setImageResource(R.drawable.mag_ok);
                                break;
                            case 4: // GOOD
                                magnetoStateOverlay.setImageResource(R.drawable.mag_good);
                                break;
                            case 5: // ERROR
                                magnetoStateOverlay.setImageResource(R.drawable.mag_error);
                                break;
                        }
                    }
                }
            };
        }

        configurationReportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (device.getFeatures().hasIntegrationEngine()) {
                    if (hasAccelerometer && accelerometerController != null) {
                        accelerometerController.setSensor(!device.integrationEngine ? device.getAccelerometer() : device.getAccelerometerIntegration());
                        accelerometerIntegrationLabel.setVisibility(device.integrationEngine ? View.VISIBLE : View.GONE);
                    }
                    if (hasGyroscope && gyroscopeController != null) {
                        gyroscopeController.setSensor(!device.integrationEngine ? device.getGyroscope() : device.getGyroscopeAngleIntegration());
                        gyroscopeIntegrationLabel.setVisibility(device.integrationEngine ? View.VISIBLE : View.GONE);
                    }
                }
            }
        };

        sensorReportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                for (int id : intent.getIntegerArrayListExtra("id")) {
                    switch (id) {
                        case UUIDS.REPORT_TEMPERATURE:
                            if (hasTemperature)
                                temperatureController.update();
                            break;
                        case UUIDS.REPORT_HUMIDITY:
                            if (hasHumidity)
                                humidityController.update();
                            break;
                        case UUIDS.REPORT_PRESSURE:
                            if (hasPressure)
                                pressureController.update();
                            break;
                        case UUIDS.REPORT_ACCELEROMETER:
                        case UUIDS.REPORT_VELOCITY_DELTA:
                            if (hasAccelerometer)
                                accelerometerController.update();
                            break;
                        case UUIDS.REPORT_GYROSCOPE:
                        case UUIDS.REPORT_EULER_ANGLE_DELTA:
                            if (hasGyroscope)
                                gyroscopeController.update();
                            break;
                        case UUIDS.REPORT_MAGNETOMETER:
                            if (hasCompass)
                                compassController.update();
                            break;
                        case UUIDS.REPORT_AMBIENT_LIGHT:
                            if (hasAmbientLight)
                                ambientLightController.update();
                            break;
                        case UUIDS.REPORT_AIR_QUALITY:
                            if (hasAirQuality)
                                airQualityController.update();
                            break;
                        case UUIDS.REPORT_PROXIMITY:
                            if (hasProximity)
                                proximityController.update();
                            break;
                        case UUIDS.REPORT_BUTTON:
                            if (hasButton)
                                buttonOverlay.setVisibility(device.getButtonSensor().isPressed() ? View.VISIBLE : View.GONE);
                            break;
                    }
                }
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(sensorReportReceiver, new IntentFilter(BroadcastUpdate.SENSOR_REPORT));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(configurationReportReceiver, new IntentFilter(BroadcastUpdate.CONFIGURATION_REPORT));
        if (statusReceiver != null)
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(statusReceiver, new IntentFilter(BroadcastUpdate.STATUS_RECEIVER));

        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initControllers();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(sensorReportReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(configurationReportReceiver);
        if (statusReceiver != null)
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(statusReceiver);
        stop();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();
        if (hasTemperature)
            temperatureController.startInterval();
        if (hasHumidity)
            humidityController.startInterval();
        if (hasPressure)
            pressureController.startInterval();
        if (hasAccelerometer)
            accelerometerController.startInterval();
        if (hasGyroscope)
            gyroscopeController.startInterval();
        if (hasCompass)
            compassController.startInterval();
        if (hasAmbientLight)
            ambientLightController.startInterval();
        if (hasAirQuality)
            airQualityController.startInterval();
        if (hasProximity)
            proximityController.startInterval();
    }

    private void initControllers() {
        if (hasTemperature)
            temperatureController = new TemperatureController(device, this);
        if (hasHumidity)
            humidityController = new HumidityController(device, this);
        if (hasPressure)
            pressureController = new PressureController(device, this);
        if (hasAccelerometer)
            accelerometerController = new AccelerometerController(device, this);
        if (hasGyroscope)
            gyroscopeController = new GyroscopeController(device, this);
        if (hasCompass)
            compassController = new CompassController(device, this);
        if (hasAmbientLight)
            ambientLightController = new AmbientLightController(device, this);
        if (hasAirQuality)
            airQualityController = new AirQualityController(device, this);
        if (hasProximity)
            proximityController = new ProximityController(device, this);
        if (device.getFeatures().hasIntegrationEngine()) {
            if (hasAccelerometer) {
                accelerometerController.setSensor(!device.integrationEngine ? device.getAccelerometer() : device.getAccelerometerIntegration());
                accelerometerIntegrationLabel.setVisibility(device.integrationEngine ? View.VISIBLE : View.GONE);
            }
            if (hasGyroscope) {
                gyroscopeController.setSensor(!device.integrationEngine ? device.getGyroscope() : device.getGyroscopeAngleIntegration());
                gyroscopeIntegrationLabel.setVisibility(device.integrationEngine ? View.VISIBLE : View.GONE);
            }
        }
    }

    public void stop() {
        if (hasTemperature)
            temperatureController.stopInterval();
        if (hasHumidity)
            humidityController.stopInterval();
        if (hasPressure)
            pressureController.stopInterval();
        if (hasAccelerometer)
            accelerometerController.stopInterval();
        if (hasGyroscope)
            gyroscopeController.stopInterval();
        if (hasCompass)
            compassController.stopInterval();
        if (hasAmbientLight)
            ambientLightController.stopInterval();
        if (hasAirQuality)
            airQualityController.stopInterval();
        if (hasProximity)
            proximityController.stopInterval();
    }
}
