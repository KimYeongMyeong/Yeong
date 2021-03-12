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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.apis.cloud.rest.AmazonAccountInfoReq;
import com.dialog.wearables.apis.cloud.rest.DeviceInfo;
import com.dialog.wearables.apis.cloud.rest.GenericRsp;
import com.dialog.wearables.apis.cloud.rest.MgmtGetEKIDRsp;
import com.dialog.wearables.cloud.DataMessenger;
import com.dialog.wearables.cloud.rest.HttpResponse;
import com.dialog.wearables.cloud.rest.RestApi;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.global.Utils;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;
import com.yodiwo.amazonbasedavsclientlibrary.activity.User;

import java.net.UnknownHostException;
import java.util.List;


public class AlexaFragment extends AVSFragment {
    // =============================================================================================
    // Variables
    // =============================================================================================

    private static final String TAG = "AlexaFragment";

    private Button buttonAlexa, buttonLogout, buttonConnectedDevice;
    private ObjectAnimator animation = null;
    private static boolean isVisible = false;

    // =============================================================================================
    // Overrides
    // =============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAlexaAndroid(IotSensorsApplication.getApplication().getApplicationContext());
        if (isVisible) {
            login();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isVisible = true;
            login();
            getEKIDs();
        } else {
            isVisible = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.fragment_alexa, container, false);

        // views
        buttonAlexa = (Button) rootView.findViewById(R.id.asset_tracking_button_alexa);
        buttonLogout = (Button) rootView.findViewById(R.id.alexa_logout);
        buttonConnectedDevice = (Button) rootView.findViewById(R.id.button_connected_device);
        //alexa animation
        animation = ObjectAnimator.ofFloat(buttonAlexa, "rotationY", 0.0f, 360.0f);

        // listeners
        buttonAlexa.setOnClickListener(new AlexaButtonOnClickListener());
        buttonLogout.setOnClickListener(new AlexaLogoutButtonOnClickListener());

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
        clearAnimationAsync(buttonAlexa);
        if (animation != null){
            animation.removeAllListeners();
            animation.end();
            animation.cancel();
        }
    }

    @Override
    protected void stateUserUpdated(User user) {
        super.stateUserUpdated(user);
        AmazonAccountInfoReq amazonAccountInfoReq = new AmazonAccountInfoReq(
                CloudSettingsManager.getUserID(getContextAsync()),
                user.getUserId(),
                user.getAccessToken(),
                user.getEmail()
        );
        setTextAsync(buttonLogout, "Logout");
        postAmazonAccountInfo(amazonAccountInfoReq);
    }

    @Override
    protected void stateUserUnauthorized() {
        super.stateUserUnauthorized();
        Utils.showToast(getContextAsync(), R.string.avs_logout);
    }

    @Override
    protected void stateErroring(Exception error) {
        super.stateErroring(error);
        clearAnimationAsync(buttonAlexa);
        if (animation != null){
            animation.removeAllListeners();
            animation.end();
            animation.cancel();
        }
        setTextAsync(buttonAlexa, "Ask Alexa");
    }

    @Override
    protected void stateAuthErroring(Exception error) {
        super.stateAuthErroring(error);
        setTextAsync(buttonLogout, "Login");
    }

    @Override
    protected void stateSpeaking() {
        super.stateSpeaking();
        clearAnimationAsync(buttonAlexa);
        setTextAsync(buttonAlexa, "Alexa Speaking");
        final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.animate);
        startAnimationAsync(buttonAlexa, anim);
    }

    @Override
    protected void stateFinished() {
        super.stateFinished();
        clearAnimationAsync(buttonAlexa);
        if (animation != null){
            animation.removeAllListeners();
            animation.end();
            animation.cancel();
        }
        setTextAsync(buttonAlexa, "Ask Alexa");

    }
    // =============================================================================================
    // Cloud
    // =============================================================================================

    private void getEKIDs() {
        if (!CloudSettingsManager.getUserID(getContextAsync()).isEmpty()) {
            DataMessenger.getInstance().getEkIds(CloudSettingsManager.getUserID(getContextAsync()), new RestApi.ResponseListener() {
                @Override
                public void start() {
                    Log.i(TAG, "Request started");
                }

                @Override
                public void success(HttpResponse rsp) {
                    if (rsp != null) {
                        if (rsp.statusCode == 200) {
                            if (rsp.body != null) {
                                MgmtGetEKIDRsp result = new Gson().fromJson(rsp.body, MgmtGetEKIDRsp.class);
                                if (result.getDevices().size() == 0) {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_empty);
                                } else {
                                    updateConnectedDeviceInfo(result.getDevices());
                                }
                            } else {
                                Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                            }
                        } else {
                            GenericRsp genericRsp = new Gson().fromJson(rsp.body, GenericRsp.class);
                            if (genericRsp != null && !genericRsp.isResult()) {
                                Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                            } else {
                                Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                            }
                        }
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                    }
                }

                @Override
                public void failure(Exception error) {
                    if (error instanceof UnknownHostException) {
                        Utils.showToast(getContextAsync(), R.string.connectivity_error);
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                    }
                }

                @Override
                public void complete() {
                    Log.i(TAG, "Request completed");
                }
            });
        } else {
            Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_empty_userid);
        }
    }

    private void postAmazonAccountInfo(AmazonAccountInfoReq amazonAccountInfoReq) {
        DataMessenger.getInstance().postAmazonAccountInfo(amazonAccountInfoReq, new RestApi.ResponseListener() {
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
                            Utils.showToast(getContextAsync(), R.string.post_amazon_account_success);
                        } else {
                            Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                        }
                    } else {
                        Utils.showToast(getContextAsync(), R.string.post_amazon_account_error);
                    }
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_amazon_account_error);
                }
            }

            @Override
            public void failure(Exception error) {
                if (error instanceof UnknownHostException) {
                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_amazon_account_error);
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

    private class AlexaButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (buttonAlexa.getText().equals("Ask Alexa")){
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.animate);
                startAnimationAsync(buttonAlexa, anim);
                setTextAsync(buttonAlexa, "CLICK WHEN FINISHED");
                startListening();
            }
            else if (buttonAlexa.getText().equals("CLICK WHEN FINISHED")){
                clearAnimationAsync(buttonAlexa);
                animation.setDuration(2000);
                animation.setRepeatCount(ObjectAnimator.INFINITE);
                animation.setRepeatMode(ObjectAnimator.RESTART);
                animation.setInterpolator(new AccelerateDecelerateInterpolator());
                animation.start();
                setTextAsync(buttonAlexa, "HOLD ON A MOMENT");
                stopListening();
            }
            else {
                if (animation != null){
                    animation.removeAllListeners();
                    animation.end();
                    animation.cancel();
                }
                setTextAsync(buttonAlexa, "Ask Alexa");
            }
        }
    }

    private class AlexaLogoutButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (buttonLogout.getText().equals("Logout")){
                setTextAsync(buttonLogout, "Login");
                stopAlexaAndroid(IotSensorsApplication.getApplication().getApplicationContext());
            }
            else {
                setTextAsync(buttonLogout, "Logout");
                login();
            }
        }
    }

    // =============================================================================================
    // Helpers
    // =============================================================================================

    private Context getContextAsync(){
        return getActivity() == null ? IotSensorsApplication.getApplication().getApplicationContext() : getActivity();
    }

    private void updateConnectedDeviceInfo(List<DeviceInfo> data) {
        for (DeviceInfo device : data) {
            if (IotSensorsApplication.getApplication().device!= null && device.getEKID().equals(IotSensorsApplication.getApplication().device.address)){
                try {
                    setTextAsync(buttonConnectedDevice, ((device.getFriendlyName() != null && !device.getFriendlyName().isEmpty()) ? device.getEKID() + "\n(" + device.getFriendlyName() + ")" : ""));
                } catch (Exception ex) {
                    Log.e(TAG, ex.getMessage());
                }
            }
        }
    }

    // =============================================================================================
    // UI updates
    // =============================================================================================

    private void startAnimationAsync(final View view, final Animation animation) {
        if (getActivity() != null  && view != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(animation);
                }
            });
    }

    private void clearAnimationAsync(final View view) {
        if (getActivity() != null && view != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    view.clearAnimation();
                }
            });
    }

    private void setTextAsync(final Button button, final String text) {
        if (getActivity() != null && button != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setText(text);
                }
            });
    }

}
