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
import com.dialog.wearables.controller.SensorFusionController;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.device.IotSensorsDevice;

public class SensorFusionFragment extends Fragment {
    public static final String TAG = "SensorFusionFragment";

    private IotSensorsDevice device;
    private BroadcastReceiver statusReceiver;
    private SensorFusionController sensorFusionController;
    private ImageView magnetoStateOverlay;
    private int calibrationState = -1;
    private boolean showCalibrationState = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        device = ((MenuHolderActivity) getActivity()).getDevice();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_sensor_fusion, container, false);
        magnetoStateOverlay = (ImageView) fragmentView.findViewById(R.id.magOverlayImage);
        calibrationState = -1;
        showCalibrationState = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("calStateOverlay", true);

        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                // Magneto state overlay
                if (device.isNewVersion() && intent.getIntExtra("sensor", 0) == 3) {
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

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(statusReceiver,
                new IntentFilter(BroadcastUpdate.STATUS_RECEIVER));

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
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(statusReceiver);
        stop();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        sensorFusionController.startInterval();
    }

    private void initControllers() {
        sensorFusionController = new SensorFusionController(device, this);
    }

    public void stop() {
        if (sensorFusionController != null) {
            sensorFusionController.stopInterval();
        }
    }
}
