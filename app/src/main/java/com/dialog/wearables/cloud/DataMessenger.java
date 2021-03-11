/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.cloud;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.apis.cloud.mqtt.AdvertiseMsg;
import com.dialog.wearables.apis.cloud.mqtt.AssetTrackingConfigMsg;
import com.dialog.wearables.apis.cloud.mqtt.DataMsg;
import com.dialog.wearables.apis.cloud.mqtt.EdgeServiceApiMsg;
import com.dialog.wearables.apis.cloud.mqtt.MgmtMsg;
import com.dialog.wearables.apis.cloud.mqtt.eMgmtServiceOperations;
import com.dialog.wearables.apis.cloud.rest.AlertingSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.AmazonAccountInfoReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingSetTagReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingTag;
import com.dialog.wearables.apis.cloud.rest.ControlSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.HistoricalGetEnvironmentalReq;
import com.dialog.wearables.apis.cloud.rest.IftttData;
import com.dialog.wearables.apis.cloud.rest.MgmtDeviceApplicationLinkInfo;
import com.dialog.wearables.apis.cloud.mqtt.ScanResult;
import com.dialog.wearables.apis.cloud.rest.SetDeviceReq;
import com.dialog.wearables.apis.cloud.rest.SetIoTAppReq;
import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.apis.common.eEventTypes;
import com.dialog.wearables.cloud.mqtt.MqttClient;
import com.dialog.wearables.cloud.rest.HttpResponse;
import com.dialog.wearables.cloud.rest.RestApi;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.global.Utils;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class DataMessenger {

    private static final String TAG = DataMessenger.class.getSimpleName();

    private static DataMessenger instance = null;

    private Context mContext;
    private MqttClient mqttClient;
    private RestApi restAPI;

    public static DataMessenger getInstance() {
        if (instance == null) {
            instance = new DataMessenger(IotSensorsApplication.getApplication().getApplicationContext());
        }
        return instance;
    }

    private DataMessenger(Context context) {
        this.mContext = context;

        this.restAPI = new RestApi(context);
    }

    // =============================================================================================
    // REST
    // =============================================================================================

    // ------------------------------ PUBLIC -------------------------------------------------------

    public void getEkIds(String userId, RestApi.ResponseListener responseListener) {
        restAPI.getEkIds(userId, responseListener);
    }

    public void postWebAppLink(String webAppLink, String userEmail, RestApi.ResponseListener responseListener) {
        restAPI.postWebAppLink(webAppLink, userEmail, responseListener);
    }

    public void getUserIdByToken(String token, String appId, RestApi.ResponseListener responseListener) {
        restAPI.getUserIdByToken(token, appId, responseListener);
    }

    public void postEKDevice(SetDeviceReq setDeviceReq, RestApi.ResponseListener responseListener) {
        restAPI.postEKDevice(setDeviceReq, responseListener);
    }

    public void getTemperatureData(HistoricalGetEnvironmentalReq info, RestApi.ResponseListener responseListener) {
        restAPI.getTemperatureData(info, responseListener);
    }

    public void getHumidityData(HistoricalGetEnvironmentalReq info, RestApi.ResponseListener responseListener) {
        restAPI.getHumidityData(info, responseListener);
    }

    public void getPressureData(HistoricalGetEnvironmentalReq info, RestApi.ResponseListener responseListener) {
        restAPI.getPressureData(info, responseListener);
    }

    public void getAirQualityData(HistoricalGetEnvironmentalReq info, RestApi.ResponseListener responseListener) {
        restAPI.getAirQualityData(info, responseListener);
    }

    public void getBrightnessData(HistoricalGetEnvironmentalReq info, RestApi.ResponseListener responseListener) {
        restAPI.getBrightnessData(info, responseListener);
    }

    public void getProximityData(HistoricalGetEnvironmentalReq info, RestApi.ResponseListener responseListener) {
        restAPI.getProximityData(info, responseListener);
    }

    public void getRules(RestApi.ResponseListener responseListener) {
        restAPI.getRules(CloudSettingsManager.getUserID(mContext), responseListener);
    }

    public void postRule(AlertingSetRuleReq req, RestApi.ResponseListener responseListener) {
        restAPI.postRule(req, responseListener);
    }

    public void getIftttApikey(String userId, RestApi.ResponseListener responseListener) {
        restAPI.getIftttApikey(userId, responseListener);
    }

    public void postIftttApikey(String key, String userID, RestApi.ResponseListener responseListener) {
        restAPI.postIftttApikey(key, userID, responseListener);
    }

    public void getControlRules(RestApi.ResponseListener responseListener) {
        restAPI.getCloudRules(CloudSettingsManager.getUserID(mContext), responseListener);
    }

    public void postControlRule(ControlSetRuleReq req, RestApi.ResponseListener responseListener) {
        restAPI.postCloudRule(req, responseListener);
    }

    public void postAssetTag(AssetTrackingSetTagReq assetTrackingSetTagReq, RestApi.ResponseListener responseListener) {
        restAPI.postAssetTag(assetTrackingSetTagReq, responseListener);
    }

    public void postAmazonAccountInfo(AmazonAccountInfoReq amazonAccountInfoReq, RestApi.ResponseListener responseListener) {
        restAPI.postAmazonAccountInfo(amazonAccountInfoReq, responseListener);
    }

    public void postIoTAppInfo(SetIoTAppReq setIoTAppReq, RestApi.ResponseListener responseListener) {
        restAPI.postIoTAppInfo(setIoTAppReq, responseListener);
    }

    // ------------------------------ PRIVATE ------------------------------------------------------
    private void postIftttButtonTrigger(String json) {
        try {
            restAPI.postIftttButtonTrigger(json, CloudSettingsManager.getApikey(mContext), new IftttResponseListener());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void postIftttTemperature(String json) {
        try {
            restAPI.postTemperatureToIfttt(json, CloudSettingsManager.getApikey(mContext), new IftttResponseListener());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void postIftttHumidity(String json) {
        try {
            restAPI.postHumidityToIfttt(json, CloudSettingsManager.getApikey(mContext), new IftttResponseListener());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private void postIftttPressure(String json) {
        try {
            restAPI.postPressureToIfttt(json, CloudSettingsManager.getApikey(mContext), new IftttResponseListener());
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private class IftttResponseListener implements RestApi.ResponseListener {

        @Override
        public void start() {
            Log.i(TAG, "Ifttt Request started");
        }

        @Override
        public void success(HttpResponse result) {
            Log.i(TAG, result.statusCode != 200 ?
                    mContext.getString(R.string.post_data_to_ifttt_error) :
                    mContext.getString(R.string.post_data_to_ifttt_success));
        }

        @Override
        public void failure(Exception error) {
            if (error instanceof UnknownHostException) {
                Log.i(TAG, mContext.getString(R.string.connectivity_error));
            } else {
                Log.i(TAG, mContext.getString(R.string.post_data_to_ifttt_error));
            }
        }

        @Override
        public void complete() {
            Log.i(TAG, "Ifttt Request completed");
        }
    }

    // =============================================================================================
    // MQTT
    // =============================================================================================

    // ------------------------------ PUBLIC -------------------------------------------------------

    public void sendSensorData(String EKID, final DataEvent dataEvent) {
        sendSensorData(EKID, new ArrayList<DataEvent>() {{
            add(dataEvent);
        }});
    }

    public void sendSensorData(String EKID, final ArrayList<DataEvent> dataEvents) {
        broadcastScanResult(dataEvents);
        send2Cloud(EKID, dataEvents);
        send2Ifttt(dataEvents);
    }

    // ------------------------------ PRIVATE ------------------------------------------------------

    private void broadcastScanResult(List<DataEvent> dataEvents) {
        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.EventType == eEventTypes.Advertise) {
                Intent intent = new Intent();
                intent.setAction(BroadcastUpdate.SCAN_RESULT);
                intent.putExtra("scanResult", dataEvent.Data);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        }
    }

    private void send2Cloud(String EKID, final List<DataEvent> dataEvents) {
        if (CloudSettingsManager.isCloudEnabled(mContext)) {
            InitMqtt();
            ArrayList<DataMsg> result = new ArrayList<>();

            for (DataEvent dataEvent : dataEvents) {
                validateDataEvent(new DataMsg(dataEvent.EventType, dataEvent.Data), result);
            }
            EdgeServiceApiMsg msg = createEdgeServiceApiMsg(EKID, result);

            if (msg.isValid() && mqttClient != null) {
                mqttClient.SendMsg(msg);
            }
        }
    }

    private void send2Ifttt(final List<DataEvent> dataEvents) {
        if (CloudSettingsManager.isIftttEnabled(mContext)) {
            List<DataEvent> result = new ArrayList<>();

            for (DataEvent dataEvent : dataEvents) {
                validateDataEventForIfttt(dataEvent, result);
            }

            for (DataEvent dataEvent : result) {
                switch (dataEvent.EventType) {
                    case (eEventTypes.Temperature):
                        IftttData iftttTemp = new IftttData(dataEvent.Data);
                        postIftttTemperature(new Gson().toJson(iftttTemp));
                        break;
                    case (eEventTypes.Humidity):
                        IftttData iftttHum = new IftttData(dataEvent.Data);
                        postIftttHumidity(new Gson().toJson(iftttHum));
                        break;
                    case (eEventTypes.Pressure):
                        IftttData iftttPres = new IftttData(dataEvent.Data);
                        postIftttPressure(new Gson().toJson(iftttPres));
                        break;
                    case (eEventTypes.Button):
                        IftttData iftttButton = new IftttData(dataEvent.Data);
                        postIftttButtonTrigger(new Gson().toJson(iftttButton));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // ------------------------------ HELPERS ------------------------------------------------------

    private EdgeServiceApiMsg createEdgeServiceApiMsg(String EKID, ArrayList<DataMsg> result) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = sdf.format(new Date());
        return new EdgeServiceApiMsg(
                CloudSettingsManager.getUserID(mContext),
                CloudSettingsManager.getAppId(mContext), EKID,
                currentDateTime, result, null);
    }

    private void validateDataEvent(DataMsg dataMsg,
                                   ArrayList<DataMsg> result) {
        boolean isAllowed = false;
        if (dataMsg.getMsgType() == eEventTypes.Button) {
            result.add(dataMsg);
            return;
        }
        if (CloudSettingsManager.EventTypesToAppPrefKeys.get(dataMsg.getMsgType()) != null) {
	        for (int key : CloudSettingsManager.EventTypesToAppPrefKeys.get(dataMsg.getMsgType())) {
	            isAllowed = isAllowed || CloudSettingsManager.isAppEnabled(mContext, key);
	        }
        }
        if (!isAllowed) {
            return;
        }

        if (dataMsg.getMsgType() == eEventTypes.Advertise) {
            String tagId = dataMsg.getData().split(" ")[0];
            int rssi = Integer.parseInt(dataMsg.getData().split(" ")[1]);
            Date currentDate = Calendar.getInstance().getTime();
            ScanResult scanResult = new ScanResult(tagId, rssi, currentDate);
            if (ScanResultsThrottlingMechanism.isAllowed(scanResult)) {
                AdvertiseMsg advertiseMsg = new AdvertiseMsg(rssi, tagId);
                dataMsg.setData(new Gson().toJson(advertiseMsg));
            }
            else {
                return;
            }
        }
        else if (dataMsg.getMsgType() == eEventTypes.Proximity) {
            if (!ThrottlingMechanism.isProximityChanged(dataMsg.getData())) {
                return;
            }
        }
        else {
            if (!ThrottlingMechanism.isAllowed(dataMsg.getMsgType())) {
                return;
            }
        }
        result.add(dataMsg);
    }

    private void validateDataEventForIfttt(DataEvent dataEvent, List<DataEvent> result) {
        // forward Button events without throttling
        if (dataEvent.EventType == eEventTypes.Button) {
            result.add(dataEvent);
            return;
        }
        if (!IftttThrottlingMechanism.isAllowed(dataEvent.EventType)) {
            return;
        }
        result.add(dataEvent);
    }

    // =============================================================================================
    // Teardown
    // =============================================================================================

    public void Teardown() {
        if (mqttClient != null) {
            mqttClient.Teardown();
        }
        if (restAPI != null) {
            restAPI.Teardown();
        }
        instance = null;
    }

    public void StopMqtt() {
        if (mqttClient != null) {
            mqttClient.Teardown();
            mqttClient = null;
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    public void InitMqtt() {
        if (mqttClient == null && CloudSettingsManager.isCloudEnabled(mContext) && !CloudSettingsManager.getUserID(mContext).isEmpty()) {
            if (isNetworkConnected()) {
                mqttClient = new MqttClient(mContext);
                mqttClient.registerRxMqttMsgListener(new MsgRxListener());
            }
        }
    }

    // =============================================================================================
    // Callbacks
    // =============================================================================================

    public interface MsgArrived {
        void onActuationMsg(String EKID, DataMsg dataMsg);

        void onThrottlingUpdated(ArrayList<Integer> eventType, ArrayList<Integer> samples);

        void onAssetTrackingConfigMsg(AssetTrackingConfigMsg assetTrackingConfigMsg);

        void onError(Exception ex);
    }

    private class MsgRxListener implements MsgArrived {

        @Override
        public void onActuationMsg(String EKID, DataMsg dataMsg) {
            com.dialog.wearables.apis.internal.DataMsg internalDataMsg = new com.dialog.wearables.apis.internal.DataMsg(EKID);
            internalDataMsg.Events.add(new DataEvent(dataMsg.getMsgType(), dataMsg.getData()));
            Intent intent = new Intent();
            intent.setAction(BroadcastUpdate.IOT_APPS_ACTUATION_UPDATE);
            intent.putExtra("EXTRA_DATA", internalDataMsg);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        }

        @Override
        public void onThrottlingUpdated(ArrayList<Integer> eventTypes, ArrayList<Integer> samples) {
            for (int i = 0; i < eventTypes.size(); i++) {
                ThrottlingMechanism.reset(eventTypes.get(i), samples.get(i));
            }
        }

        @Override
        public void onAssetTrackingConfigMsg(AssetTrackingConfigMsg assetTrackingConfigMsg) {
            ScanResultsThrottlingMechanism.resetConfig(assetTrackingConfigMsg);
        }

        @Override
        public void onError(Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }
}
