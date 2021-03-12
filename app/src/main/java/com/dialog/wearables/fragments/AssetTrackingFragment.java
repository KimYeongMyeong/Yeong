/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingSetTagReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingTag;
import com.dialog.wearables.apis.cloud.rest.GenericRsp;
import com.dialog.wearables.apis.cloud.rest.eAssetTrackingOperationTypes;
import com.dialog.wearables.cloud.DataMessenger;
import com.dialog.wearables.cloud.ScanResultsThrottlingMechanism;
import com.dialog.wearables.cloud.rest.HttpResponse;
import com.dialog.wearables.cloud.rest.RestApi;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.global.Utils;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class AssetTrackingFragment extends Fragment {

    // =============================================================================================
    // Variables
    // =============================================================================================

    private static final String TAG = AssetTrackingFragment.class.getSimpleName();

    private Button buttonScan;
    private TextView deviceId, assetName;
    private BroadcastReceiver scanResultReceiver;
    private ObjectAnimator animation = null;
    private HashMap<String, Integer> devices = new HashMap<>();
    private boolean startScanning = false;
    private int MaxRssi = -30;       // in dBm

    // =============================================================================================
    // Overrides
    // =============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        scanResultReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String scanResult = intent.getStringExtra("scanResult");
                String deviceId = scanResult.split(" ")[0];
                int rssi = Integer.parseInt(scanResult.split(" ")[1]);
                updateScanDevices(deviceId, rssi);
            }
        };

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(scanResultReceiver,
                new IntentFilter(BroadcastUpdate.SCAN_RESULT));
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            // Load your data here or do network operations here
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_asset_tracking, container, false);

        // views
        deviceId = (TextView) rootView.findViewById(R.id.asset_tracking_device_id);
        assetName = (TextView) rootView.findViewById(R.id.asset_tracking_asset_name);
        buttonScan = (Button) rootView.findViewById(R.id.asset_tracking_button_scan);
        Button buttonSelect = (Button) rootView.findViewById(R.id.asset_tracking_button_select);
        Button buttonAddAsset = (Button) rootView.findViewById(R.id.asset_tracking_button_add_asset);
        // listeners
        buttonScan.setOnClickListener(new ScanButtonOnClickListener());
        buttonSelect.setOnClickListener(new SelectDeviceButtonOnClickListener());
        buttonAddAsset.setOnClickListener(new AddAssetButtonOnClickListener());

        return rootView;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        try {
            clearAnimationAsync(deviceId);
            startScanning = false;
            if (animation != null) {
                animation.removeAllListeners();
                animation.end();
                animation.cancel();
            }
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(scanResultReceiver);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        super.onDestroy();
    }

    // =============================================================================================
    // Cloud
    // =============================================================================================

    private void postAssetTag(final AssetTrackingSetTagReq assetTrackingSetTagReq) {
        DataMessenger.getInstance().postAssetTag(assetTrackingSetTagReq, new RestApi.ResponseListener() {
            @Override
            public void start() {
                Log.i(TAG, "Request started");
            }

            @Override
            public void success(HttpResponse rsp) {

                if (rsp != null && rsp.body != null) {
                    GenericRsp genericRsp = new Gson().fromJson(rsp.body, GenericRsp.class);
                    if (genericRsp != null) {
                        if (genericRsp.isResult()) {
                            Utils.showToast(getContextAsync(), R.string.post_asset_tag_success);
                            ScanResultsThrottlingMechanism.reset(assetTrackingSetTagReq.getTag().getTagId());
                        } else {
                            Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                        }
                    } else {
                        Utils.showToast(getContextAsync(), R.string.post_asset_tag_error);
                    }
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_asset_tag_error);
                }
            }

            @Override
            public void failure(Exception error) {
                if (error instanceof UnknownHostException) {
                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_asset_tag_error);
                }
            }

            @Override
            public void complete() {
                Log.i(TAG, "Request completed");
            }
        });
    }

    // =============================================================================================
    // UI Listeners
    // =============================================================================================

    private class ScanButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (buttonScan.getText().equals("Scan")) {
                Animation anim = new AlphaAnimation(0.0f, 1.0f);
                anim.setDuration(50);
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                startAnimationAsync(deviceId, anim);
                startScanning = true;
                setTextAsync(buttonScan, "Stop");
            } else {
                startScanning = false;
                clearAnimationAsync(deviceId);
                setTextAsync(buttonScan, "Scan");
            }
        }
    }

    private class SelectDeviceButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (buttonScan.getText().equals("Stop")) {
                startScanning = false;
                clearAnimationAsync(deviceId);
                setTextAsync(buttonScan, "Scan");
            }
        }
    }

    private class AddAssetButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AssetTrackingTag assetTrackingTag = new AssetTrackingTag(
                    deviceId.getText().toString().trim(), // device id, tagid
                    assetName.getText().toString().trim(), // asset friendly name
                    "", "", CloudSettingsManager.getUserID(getContextAsync())
            );

            if (assetTrackingTag.isValid()) {
                AssetTrackingSetTagReq assetTrackingSetTagReq = new AssetTrackingSetTagReq(
                        eAssetTrackingOperationTypes.Insert, assetTrackingTag);
                postAssetTag(assetTrackingSetTagReq);
            } else {
                Utils.showToast(getContextAsync(), R.string.post_asset_tag_not_valid_request);
            }
        }
    }
    // =============================================================================================
    // Helpers
    // =============================================================================================

    private Context getContextAsync() {
        return getActivity() == null ? IotSensorsApplication.getApplication().getApplicationContext() : getActivity();
    }

    private void updateScanDevices(String id, int rssi) {
        Integer previousRssi = devices.get(id);
        devices.put(id, (previousRssi != null ? (previousRssi + rssi) / 2 : rssi));
        if (startScanning) {
            setTextAsync(deviceId, id);
            selectMaxRssiDevice();
        }
    }

    private void selectMaxRssiDevice() {
        List<String> maxRssiDevices = new ArrayList<>();
        List<String> thresholdRssiDevices = new ArrayList<>();
        for (String key : devices.keySet()) {
            if (devices.get(key) >= MaxRssi) {
                maxRssiDevices.add(key);
            }
        }
        if (maxRssiDevices.size() > 1) {
            Log.i(TAG, "Multiple devices");
            setTextAsync(deviceId, "");
        } else if (maxRssiDevices.size() == 1) {
            String deviceFound = maxRssiDevices.get(0);
            for (String key : devices.keySet()) {
                if (devices.get(key) <= (devices.get(deviceFound) - 15)) {
                    thresholdRssiDevices.add(key);
                }
            }
            if (thresholdRssiDevices.size() != devices.size() - 1) {
                Log.i(TAG, "Multiple devices");
                setTextAsync(deviceId, "");
            } else if (thresholdRssiDevices.size() == devices.size() - 1) {
                setTextAsync(this.deviceId, deviceFound);
                Log.i(TAG, "Device found with rssi " + devices.get(deviceFound));
            }
        } else {
            Log.i(TAG, "No device found");
            setTextAsync(deviceId, "");
            if (MaxRssi != -45) {
                MaxRssi += -5;
            }
        }
    }

    // =============================================================================================
    // UI updates
    // =============================================================================================

    private void startAnimationAsync(final View view, final Animation animation) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(animation);
                }
            });
    }

    private void clearAnimationAsync(final View view) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.clearAnimation();
                }
            });
    }

    private void setTextAsync(final Button button, final String text) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setText(text);
                }
            });
    }

    private void setTextAsync(final TextView textView, final String text) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    textView.setText(text);
                }
            });
    }

}
