/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.apis.cloud.rest.EKDevice;
import com.dialog.wearables.apis.cloud.rest.GenericRsp;
import com.dialog.wearables.apis.cloud.rest.MgmtGetUserIdByTokenRsp;
import com.dialog.wearables.apis.cloud.rest.MgmtIftttInfo;
import com.dialog.wearables.apis.cloud.rest.SetDeviceReq;
import com.dialog.wearables.apis.cloud.rest.SetIoTAppReq;
import com.dialog.wearables.apis.cloud.rest.eOperationTypes;
import com.dialog.wearables.apis.common.eEventTypes;
import com.dialog.wearables.cloud.DataManager;
import com.dialog.wearables.cloud.DataMessenger;
import com.dialog.wearables.cloud.IftttThrottlingMechanism;
import com.dialog.wearables.cloud.rest.HttpResponse;
import com.dialog.wearables.cloud.rest.RestApi;
import com.dialog.wearables.cloud.rest.UrlFactory;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.Utils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Settings manager for the Cloud settings
 */
public class CloudSettingsManager implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = CloudSettingsManager.class.getSimpleName();

    private PreferenceFragment mContext;
    private static final String DEFAULT_IFTTT_SECS = "60";
    private boolean isSyncFromCloud;
    public static final List<Integer> IftttEventTypes = new ArrayList<>
            (Arrays.asList(eEventTypes.Temperature, eEventTypes.Humidity, eEventTypes.Pressure) );

    public static final List<Integer> EventTypesToIftttPrefKeys = Arrays.asList(
            eEventTypes.Temperature, eEventTypes.Humidity, eEventTypes.Pressure, eEventTypes.Button);

    public static final HashMap<Integer, int[]> EventTypesToAppPrefKeys = new HashMap<Integer, int[]>()
    {{
        put(eEventTypes.Temperature,
                new int[]{ R.string.pref_switch_cloud_alert_key, R.string.pref_switch_cloud_historical_key });
        put(eEventTypes.Humidity,
                new int[]{ R.string.pref_switch_cloud_alert_key, R.string.pref_switch_cloud_historical_key });
        put(eEventTypes.Pressure,
                new int[]{ R.string.pref_switch_cloud_alert_key, R.string.pref_switch_cloud_historical_key });
        put(eEventTypes.AirQuality,
                new int[]{ R.string.pref_switch_cloud_alert_key, R.string.pref_switch_cloud_historical_key });
        put(eEventTypes.Brightness,
                new int[]{ R.string.pref_switch_cloud_alert_key, R.string.pref_switch_cloud_historical_key });
        put(eEventTypes.Proximity,
                new int[]{ R.string.pref_switch_cloud_historical_key });
        put(eEventTypes.Advertise,
                new int[]{ R.string.pref_switch_cloud_assettracking_key });
        put(eEventTypes.Fusion,
                new int[]{ R.string.pref_switch_cloud_3dgame_key });

    }};

    // =============================================================================================
    // Constructor
    // =============================================================================================

    public CloudSettingsManager(PreferenceFragment context) {
        this.mContext = context;

        Preference sendLink = mContext.findPreference(mContext.getString(R.string.pref_button_sendlink_key));
        sendLink.setOnPreferenceClickListener(this);

        fetchUserEmail(getUserEmail(context.getActivity()));
        enableCloud(((SwitchPreference) context.findPreference(context.getString(R.string.pref_switch_cloud_enable_key))).isChecked());

//        Preference applyApiKey = mContext.findPreference(mContext.getString(R.string.pref_apply_ifttt_apikey_key));
//        applyApiKey.setOnPreferenceClickListener(this);

        Preference setApiKey = mContext.findPreference(mContext.getString(R.string.pref_button_set_iftttapikey_key));
        setApiKey.setOnPreferenceClickListener(this);

        Preference syncApiKey = mContext.findPreference(mContext.getString(R.string.pref_button_sync_iftttapikey_key));
        syncApiKey.setOnPreferenceClickListener(this);

        fetchApikey(getApikey(mContext.getActivity()));
        fetchTriggerInterval(getTriggerInterval(mContext.getActivity()));
    }

    // =============================================================================================
    // Callbacks
    // =============================================================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(mContext.getString(R.string.pref_set_useremail_key))) {
            EditTextPreference textPreference = (EditTextPreference) mContext.findPreference(key);
            updateEmail(mContext.getActivity(), textPreference.getText().trim());
        } else if (key.equals(mContext.getString(R.string.pref_switch_cloud_enable_key))) {
            if (isAlertDialogCancelled) {
                isAlertDialogCancelled = false;
                setSwitch(R.string.pref_switch_cloud_enable_key, false);
            } else {
                SwitchPreference preference = (SwitchPreference) mContext.findPreference(key);
                enableCloud(preference.isChecked());
            }
        }
        if (key.equals(mContext.getString(R.string.pref_set_trigger_interval_key))) {
            EditTextPreference preference = (EditTextPreference) mContext.findPreference(key);
            updateTriggerInterval(preference.getText().trim());
        }
        if (isSyncFromCloud) {
            isSyncFromCloud = false;
            return;
        }
        if (key.equals(mContext.getString(R.string.pref_apply_ifttt_apikey_key))) {
            EditTextPreference preference = (EditTextPreference) mContext.findPreference(key);
            updateCloudApikey(preference.getText().trim());
        }
        if (key.equals(mContext.getString(R.string.pref_switch_ifttt_enable_key))) {
            SwitchPreference preference = (SwitchPreference) mContext.findPreference(key);
            enableTriggerInterval(preference.isChecked());
        }
//        else if (key.equals(mContext.getString(R.string.pref_switch_cloud_alert_key))) {
//            SwitchPreference preference = (SwitchPreference) mContext.findPreference(key);
//            enableAlerting(preference.isChecked());
//        } else if (key.equals(mContext.getString(R.string.pref_switch_cloud_control_key))) {
//            SwitchPreference preference = (SwitchPreference) mContext.findPreference(key);
//            enableControl(preference.isChecked());
//        }
    }

    /**
     * Handles the button presses
     *
     * @param preference preference that is being pressed
     * @return returns false when no preference is matched
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(preference.getContext().getString(R.string.pref_button_sendlink_key))) {
            setCloudWebAppLink(preference.getContext());
        }
        if (preference.getKey().equals(mContext.getString(R.string.pref_button_sync_iftttapikey_key))) {
            getCloudApikey(CloudSettingsManager.getUserID(preference.getContext()));
        }
        if (preference.getKey().equals(mContext.getString(R.string.pref_button_set_iftttapikey_key))) {
            //getCloudApikey(CloudSettingsManager.getUserID(preference.getContext()));
            EditTextPreference editTextPreference = (EditTextPreference) mContext.findPreference(mContext.getString(R.string.pref_apply_ifttt_apikey_key));
            setCloudApikey(editTextPreference.getText());
        }
        return false;
    }

    // =============================================================================================
    // Syncing
    // =============================================================================================

    private void updateEmail(@NonNull Context context, String text) {
        fetchUserEmail(text);
        setUserEmail(context, text);
    }

    // =============================================================================================
    // Init
    // =============================================================================================

    public static void init(@NonNull Context context, String ekId) {
        String appid = createAppId(context);
        createUserID(context, appid, ekId);
    }

    private static String createAppId(Context context) {
        String appid = getAppId(context);
        if (appid.isEmpty()) {
            appid = UUID.randomUUID().toString();
            saveGUID(context, appid);
        }
        return appid;
    }

    private static void createUserID(@NonNull Context context, String appId, String ekId) {
        if (!isCloudEnabled(context))
            return;

        String userid = getUserID(context);
        if (userid.isEmpty()) {
            openDialog(context);
        }
        // TODO set device
        setEKDevice(context, new SetDeviceReq(eOperationTypes.Insert, new EKDevice(ekId, userid, "", "")));
    }

    // =============================================================================================
    // Helpers
    // =============================================================================================

    private Context getContextAsync() {
        return mContext != null && mContext.getActivity() != null ? mContext.getActivity() :
                IotSensorsApplication.getApplication().getApplicationContext();
    }

    // =============================================================================================
    // Register listeners
    // =============================================================================================

    /**
     * Will register the on change listener
     */
    public void register() {
        if (mContext != null) {
            PreferenceManager.getDefaultSharedPreferences(mContext.getActivity()).registerOnSharedPreferenceChangeListener(this);
        }
    }

    /**
     * Will unregister the on change listener
     */
    public void unregister() {
        if (mContext != null) {
            PreferenceManager.getDefaultSharedPreferences(mContext.getActivity()).unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    // =============================================================================================
    // Storage
    // =============================================================================================

    public static String getAppId(@NonNull Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).contains(context.getString(R.string.pref_guid_key))) {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_guid_key), "");
        }
        return "";
    }

    private static void saveGUID(@NonNull Context context, String uuid) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences.putString(context.getString(R.string.pref_guid_key), uuid);
        preferences.apply();
    }

    // -----------------------------------------------------------------------------------------

    private static void saveUserId(@NonNull Context context, @NonNull String id) {
        alertDialog.cancel();
        if (!getUserID(context).isEmpty() && !getUserID(context).equals(id)) {
            return;
        }

        if (getUserID(context).isEmpty()) {
            SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
            preferences.putString(context.getString(R.string.pref_userid_key), id);
            preferences.apply();

            saveWebLink(context, id);
            IotSensorsDevice device = IotSensorsApplication.getApplication().device;
            if (device != null && device.state == IotSensorsDevice.CONNECTED) {
                init(context, device.address);
            }
        }
        //
        DataManager.InitMqtt(IotSensorsApplication.getApplication().getApplicationContext());
    }

    public static String getUserID(@NonNull Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).contains(context.getString(R.string.pref_userid_key))) {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_userid_key), "");
        }
        return "";
    }

    // -----------------------------------------------------------------------------------------

    public static String getWebLink(@NonNull Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).contains(context.getString(R.string.pref_set_webapplink_key))) {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_set_webapplink_key), "");
        }
        return "";
    }

    private static void saveWebLink(@NonNull Context context, String userId) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences.putString(context.getString(R.string.pref_set_webapplink_key), UrlFactory.getYodiboardsUrl(userId));
        preferences.apply();
    }

    // -----------------------------------------------------------------------------------------

    private static void saveCloudSwitch(@NonNull Context context) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(R.string.pref_switch_cloud_enable_key), false)
                .apply();
    }
    // -----------------------------------------------------------------------------------------

    private static void setUserEmail(@NonNull Context context, String userEmail) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences.putString(context.getString(R.string.pref_set_useremail_key), userEmail);
        preferences.apply();
    }

    private static String getUserEmail(@NonNull Context context) {
        if (PreferenceManager.getDefaultSharedPreferences(context).contains(context.getString(R.string.pref_set_useremail_key))) {
            return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_set_useremail_key), "");
        }
        return "";
    }

    // ---------------------------------------------------------------------------------------------

    public static boolean isCloudEnabled(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(R.string.pref_switch_cloud_enable_key), false);
    }

    public static boolean isAppEnabled(@NonNull Context context, int key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getString(key), false);
    }

    // ---------------------------------------------------------------------------------------------

    public int[] getDisabledIoTApps() {
        HashSet<Integer> eventTypes = new HashSet<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContextAsync());

        for (int key : EventTypesToAppPrefKeys.keySet()) {
            boolean isAllowed = false;
            for (int value : EventTypesToAppPrefKeys.get(key)) {
                isAllowed = isAllowed || preferences.getBoolean(getContextAsync().getString(value), false);
            }
            for (int iftttEventType : IftttEventTypes) {
                if (key == iftttEventType) {
                    isAllowed = isAllowed || isIftttEnabled(getContextAsync());
                }
            }
            if (!isAllowed) eventTypes.add(key);
        }
        // return
        int[] array = new int[eventTypes.size()];
        int i = 0;
        for (Integer eventType : eventTypes) {
            array[i++] = eventType;
        }
        return array;
    }

    // =============================================================================================
    // Cloud requests
    // =============================================================================================

    private void setCloudWebAppLink(final Context context) {
        DataMessenger.getInstance().postWebAppLink(getWebLink(context), getUserEmail(context), new RestApi.ResponseListener() {
            @Override
            public void start() {
                onProgress();
//                Utils.showToast(context, R.string.send_link_start);
            }

            @Override
            public void success(HttpResponse result) {
                iftttOffProgress(context);
                if (result != null) {
                    if (result.statusCode == 200) {
                        Utils.showToast(context, R.string.send_link_done);
                    }
                    else {
                        GenericRsp genericRsp = new Gson().fromJson(result.body, GenericRsp.class);
                        if (genericRsp != null && !genericRsp.isResult()) {
                            // Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                        } else {
                            Utils.showToast(getContextAsync(), R.string.send_link_error);
                        }
                    }
                } else {
                    Utils.showToast(getContextAsync(), R.string.send_link_error);
                }
            }

            @Override
            public void failure(Exception error) {
                iftttOffProgress(context);
                Utils.showToast(context, R.string.send_link_error);
            }

            @Override
            public void complete() {

            }
        });
    }

    private static void setEKDevice(final Context context, SetDeviceReq setDeviceReq) {
        DataMessenger.getInstance().postEKDevice(setDeviceReq, new RestApi.ResponseListener() {
            @Override
            public void start() {
                Log.i(TAG, "Request to set EKDevice started");
            }

            @Override
            public void success(HttpResponse result) {
                if (result != null) {
                    if (result.statusCode == 200) {
                        //Utils.showToast(context, R.string.send_device_done);
                    }
                    else {
                        GenericRsp genericRsp = new Gson().fromJson(result.body, GenericRsp.class);
                        if (genericRsp != null && !genericRsp.isResult()) {
                            // Utils.showToast(context, genericRsp.getReasonCode());
                        } else {
                            Utils.showToast(context, R.string.send_device_error);
                        }
                    }
                } else {
                    Utils.showToast(context, R.string.send_device_error);
                }
            }

            @Override
            public void failure(Exception error) {
                Utils.showToast(context, R.string.send_device_error);
            }

            @Override
            public void complete() {
                Log.i(TAG, "Request to set EKDevice completed");
            }
        });
    }

    private static void getUserIdFromAnExistingAccount(final Context context, String token, String appId) {
        DataMessenger.getInstance().getUserIdByToken(token, appId, new RestApi.ResponseListener() {
            @Override
            public void start() {

            }
            @Override
            public void success(HttpResponse result) {
                if (result != null) {
                    if (result.statusCode == 200) {
                        if (result.body != null) {
                            MgmtGetUserIdByTokenRsp mgmtGetUserIdByTokenRsp = new Gson().fromJson(result.body, MgmtGetUserIdByTokenRsp.class);
                            saveUserId(context, mgmtGetUserIdByTokenRsp.getUserId());
                        } else {
                            Utils.showToast(context, R.string.get_cloud_userid_error);
                        }
                    }
                    else {
                        GenericRsp genericRsp = new Gson().fromJson(result.body, GenericRsp.class);
                        if (genericRsp != null && !genericRsp.isResult()) {
                            Utils.showToast(context, genericRsp.getReasonCode());
                        } else {
                            Utils.showToast(context, R.string.get_cloud_userid_error);
                        }
                    }
                } else {
                    Utils.showToast(context, R.string.get_cloud_userid_error);
                }
            }

            @Override
            public void failure(Exception error) {
                Utils.showToast(context, R.string.get_cloud_userid_error);
            }

            @Override
            public void complete() {

            }
        });
    }

    private static void setIoTApp(final Context context, SetIoTAppReq setIoTAppReq) {
        DataMessenger.getInstance().postIoTAppInfo(setIoTAppReq, new RestApi.ResponseListener() {
            @Override
            public void start() {
                Log.i(TAG, "Request to set IoT App started");
            }

            @Override
            public void success(HttpResponse result) {
                if (result != null) {
                    if (result.statusCode == 200) {
                        //Utils.showToast(context, R.string.send_iotapp_done);
                    }
                    else {
                        GenericRsp genericRsp = new Gson().fromJson(result.body, GenericRsp.class);
                        if (genericRsp != null && !genericRsp.isResult()) {
                            // Utils.showToast(context, genericRsp.getReasonCode());
                        } else {
                            Utils.showToast(context, R.string.send_iotapp_error);
                        }
                    }
                } else {
                    Utils.showToast(context, R.string.send_iotapp_error);
                }
            }

            @Override
            public void failure(Exception error) {
                Utils.showToast(context, R.string.send_iotapp_error);
            }

            @Override
            public void complete() {
                Log.i(TAG, "Request to set IoT App completed");
            }
        });
    }
    // =============================================================================================
    // UI fetching
    // =============================================================================================

    private void onProgress() {
        enableSwitch(false);
        enableSendButton(false);
        enableUserEmail(false);
    }

    private void iftttOffProgress(Context context) {
        enableSwitch(true);
        enableUserEmail(true);
        enableSendButton(!getWebLink(context).isEmpty() && !getUserEmail(context).isEmpty());
    }

    private void enableCloud(boolean state) {
        enableUserEmail(state);
        enableSendButton(state);
        enableIotApps(state);
        setSwitch(R.string.pref_switch_webapplink_key, state);
        setSwitch(R.string.pref_switch_cloud_historical_key, state);
        setSwitch(R.string.pref_switch_cloud_alert_key, state);
        setSwitch(R.string.pref_switch_cloud_control_key, state);
        String userId = getUserID(getContextAsync());
        if (state && (userId == null || userId.equals(""))) {
            openDialog(getContextAsync());
        }
        if (!state) {
            DataManager.StopMqtt(IotSensorsApplication.getApplication().getApplicationContext());
        }
        if (state) {
            DataManager.InitMqtt(IotSensorsApplication.getApplication().getApplicationContext());
        }
    }

    private static AlertDialog alertDialog = null;
    private static boolean isAlertDialogCancelled = false;
    private static void openDialog(final Context context){
        isAlertDialogCancelled = false;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );
        if (inflater != null) {
            View alertLayout = inflater.inflate(R.layout.layout_custom_dialog, null);
            final EditText pin = (EditText) alertLayout.findViewById(R.id.dialog_form_pin);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Create/ Add account");
            builder.setView(alertLayout);
            builder.setCancelable(false);
            // Register button click listener.
            Button createNewAccountButton = (Button)alertLayout.findViewById(R.id.dialog_form_create_new_account_button);
            createNewAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        getUserIdFromAnExistingAccount(context, "", createAppId(context));
                        isAlertDialogCancelled = false;
                    } catch(Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            });

            Button addToAnExistingAccountButton = (Button)alertLayout.findViewById(R.id.dialog_form_add_mobile_to_existing_account_button);
            addToAnExistingAccountButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        getUserIdFromAnExistingAccount(context, pin.getText().toString(), createAppId(context));
                        isAlertDialogCancelled = false;
                    } catch(Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                }
            });

            Button cancelButton = (Button)alertLayout.findViewById(R.id.dialog_form_cancel);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                    isAlertDialogCancelled = true;
                    saveCloudSwitch(context);
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void enableUserEmail(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_set_useremail_key))
                            .setEnabled(state);
                }
            });
        }
    }

    private void enableSwitch(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_switch_cloud_enable_key))
                            .setEnabled(state);
                }
            });
        }
    }

    private void enableIotApps(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_category_cloud_iotapps_key))
                            .setEnabled(state);
                }
            });
        }
    }

    private void enableSendButton(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_button_sendlink_key))
                            .setEnabled(state);
                }
            });
        }
    }

//    private void enableAlerting(boolean state) {
//        SetIoTAppReq setIoTAppReq = new SetIoTAppReq(eOperationTypes.Insert,
//                new IoTApp
//                        (
//                                eIoTApps.Alerting,
//                                state,
//                                getUserID(getContextAsync()),
//                                getAppId(getContextAsync())
//                        )
//        );
//        setIoTApp(getContextAsync(), setIoTAppReq);
//    }
//
//    private void enableControl(boolean state) {
//        SetIoTAppReq setIoTAppReq = new SetIoTAppReq(eOperationTypes.Insert,
//                new IoTApp
//                        (
//                                eIoTApps.Control,
//                                state,
//                                getUserID(getContextAsync()),
//                                getAppId(getContextAsync())
//                        )
//        );
//        setIoTApp(getContextAsync(), setIoTAppReq);
//    }

    private void fetchUserEmail(final String email) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext.findPreference(mContext.getString(R.string.pref_set_useremail_key))
                            .setSummary(email);
                }
            });
        }

        // check if email is not valid
        enableSendButton(!email.isEmpty());
    }

    private void setSwitch(final int key, final boolean isChecked) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final SwitchPreference cloudPref = (SwitchPreference) mContext.findPreference(mContext.getString(key));
                    cloudPref.setChecked(isChecked);
                }
            });
        }
    }

    //--------------------------------IFTTT---------------------------------------------------------
    // =============================================================================================
    // Get apikey from local storage
    // =============================================================================================

    public static String getApikey(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_apply_ifttt_apikey_key), "");
    }

    // =============================================================================================
    // Update apikey from UI
    // =============================================================================================

    private void updateCloudApikey(String key) {
        fetchApikey(key);
        setApikey(mContext.getActivity(), key);
    }

    private static void setApikey(@NonNull Context context, String key) {
        SharedPreferences.Editor preferences = PreferenceManager.getDefaultSharedPreferences(context).edit();
        preferences.putString(context.getString(R.string.pref_apply_ifttt_apikey_key), key);
        preferences.apply();
    }

    private void setCloudApikey(String key) {
        if (key != null && !key.trim().isEmpty()) {
            DataMessenger.getInstance().postIftttApikey(key.trim(), CloudSettingsManager.getUserID(getContextAsync()), new RestApi.ResponseListener() {
                @Override
                public void start() {
                    iftttOnProgress();
                }

                @Override
                public void success(HttpResponse result) {
                    iftttOffProgress();
                    if (result != null && result.body != null) {
                        GenericRsp genericRsp = new Gson().fromJson(result.body, GenericRsp.class);
                        if (genericRsp != null) {
                            if (genericRsp.getReasonCode() != null && !genericRsp.getReasonCode().isEmpty())
                                Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                        } else {
                            Utils.showToast(getContextAsync(), R.string.send_cloud_ifttt_apikey_error);
                        }
                    }
                }

                @Override
                public void failure(Exception error) {
                    Utils.showToast(getContextAsync(), R.string.send_cloud_ifttt_apikey_error);
                    iftttOffProgress();
                }

                @Override
                public void complete() {

                }
            });
        }
    }
    // =============================================================================================
    // Syncing
    // =============================================================================================

    private void getCloudApikey(String userId) {
        DataMessenger.getInstance().getIftttApikey(userId, new RestApi.ResponseListener() {
            @Override
            public void start() {
                iftttOnProgress();
            }

            @Override
            public void success(HttpResponse result) {
                if (result.statusCode == 200) {
                    isSyncFromCloud = true;
                    String apikey = new Gson().fromJson(result.body, MgmtIftttInfo.class).IftttApiKey;
                    updateApikey(apikey);
                } else {
                    Utils.showToast(getContextAsync(), R.string.get_cloud_ifttt_apikey_error);
                }
                iftttOffProgress();
            }

            @Override
            public void failure(Exception error) {
                Utils.showToast(getContextAsync(), R.string.get_cloud_ifttt_apikey_error);
                iftttOffProgress();
            }

            @Override
            public void complete() {

            }
        });
    }

    private void updateApikey(String key) {
        fetchApikey(key);
        setApikey(mContext.getActivity(), key);
    }

    private void updateTriggerInterval(String interval) {
        fetchTriggerInterval(interval);
        setTriggerInterval(mContext.getActivity(), interval);
    }

    private void setTriggerInterval( Context context, String interval) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(R.string.pref_set_trigger_interval_key), interval)
                .apply();
        resetIftttThrottling(interval);
    }

    private static void resetIftttThrottling(String interval) {
        for (Integer eventType : EventTypesToIftttPrefKeys) {
            IftttThrottlingMechanism.reset(eventType, Integer.parseInt(interval));
        }
    }

    public static String getTriggerInterval(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_set_trigger_interval_key), DEFAULT_IFTTT_SECS);
    }

    // =============================================================================================
    // UI fetching
    // =============================================================================================

    private void iftttOnProgress() {
        enableApplyText(false);
        enableSetButton(false);
        enableSyncButton(false);
        enableIftttSwitch(false);
        enableTriggerInterval(false);
    }

    private void iftttOffProgress() {
        enableApplyText(true);
        enableSetButton(true);
        enableSyncButton(true);
        enableIftttSwitch(!getApikey(mContext.getActivity()).isEmpty());
        checkSwitch(!getApikey(mContext.getActivity()).isEmpty());
    }

    private void fetchApikey(@NonNull final String key) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_apply_ifttt_apikey_key))
                            .setSummary(key);
                }
            });
        }

        // enable-switch depends on apikey and the previous state
        checkSwitch(!key.isEmpty());
        enableIftttSwitch(!key.isEmpty());
    }

    private void fetchTriggerInterval(@NonNull final String interval) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_set_trigger_interval_key))
                            .setSummary(interval);
                }
            });
        }
    }

    private void enableApplyText(final boolean state) {
        if (mContext != null)
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(getContextAsync().getString(R.string.pref_apply_ifttt_apikey_key))
                            .setEnabled(state);
                }
            });
    }

    private void enableIftttSwitch(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_switch_ifttt_enable_key))
                            .setEnabled(state);
                }
            });
        }
    }

    private void checkSwitch(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SwitchPreference switchRef = (SwitchPreference) mContext
                            .findPreference(mContext.getString(R.string.pref_switch_ifttt_enable_key));
                    switchRef.setChecked(switchRef.isChecked() && state);
                }
            });
        }
        enableTriggerInterval(state);
    }

    private void enableSetButton(final boolean state) {
        if (mContext != null)
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_button_set_iftttapikey_key))
                            .setEnabled(state);
                }
            });
    }

    private void enableSyncButton(final boolean state) {
        if (mContext != null)
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_button_sync_iftttapikey_key))
                            .setEnabled(state);
                }
            });
    }

    private void enableTriggerInterval(final boolean state) {
        if (mContext != null) {
            mContext.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext
                            .findPreference(mContext.getString(R.string.pref_set_trigger_interval_key))
                            .setEnabled(state);
                }
            });
            if (state) resetIftttThrottling(getTriggerInterval(mContext.getActivity()));
        }
    }

    // ---------------------------------------------------------------------------------------------

    public static boolean isIftttEnabled(@NonNull Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_switch_ifttt_enable_key), false) &&
                !PreferenceManager.getDefaultSharedPreferences(context)
                        .getString(context.getString(R.string.pref_apply_ifttt_apikey_key), "").isEmpty();
    }
}
