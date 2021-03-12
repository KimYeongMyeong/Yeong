/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.cloud.mqtt;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.apis.Constants;
import com.dialog.wearables.apis.cloud.mqtt.AssetTrackingConfigMsg;
import com.dialog.wearables.apis.cloud.mqtt.DataMsg;
import com.dialog.wearables.apis.cloud.mqtt.EdgeServiceApiMsg;
import com.dialog.wearables.apis.cloud.mqtt.MgmtMsg;
import com.dialog.wearables.apis.cloud.mqtt.ServiceEdgeApiMsg;
import com.dialog.wearables.apis.cloud.mqtt.ThrottlingSet;
import com.dialog.wearables.apis.cloud.mqtt.eMgmtEdgeOperations;
import com.dialog.wearables.apis.cloud.mqtt.eMgmtServiceOperations;
import com.dialog.wearables.apis.common.eActuationTypes;
import com.dialog.wearables.cloud.DataMessenger;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MqttClient {

    // =============================================================================================
    // Variables
    // =============================================================================================

    public static final String TAG = MqttClient.class.getSimpleName();

    private static final int RECONNECT_PERIOD = 5 * 1000;

    private Gson gson = new Gson();
    private HashSet<String> subscribedTopics = new HashSet<>();
    private DataMessenger.MsgArrived listener;
    private boolean RxActive, TxActive;
    private Context mContext;

    // =============================================================================================
    // Constructor
    // =============================================================================================

    public MqttClient(Context mContext) {
        Log.d(TAG, "Starting MQTT client API.");

        this.mContext = mContext;
        RxActive = false;
        TxActive = false;
        RequestConnectivityUiUpdate();

        InitMqttClient();
    }

    // =============================================================================================
    // Public API
    // =============================================================================================

    public boolean SendMsg(Object msg) {
        return SendMsg(msg, 2);
    }

    public void registerRxMqttMsgListener(DataMessenger.MsgArrived listener) {
        this.listener = listener;
    }

    public void Teardown() {
        try {
            if (reconnectTimer != null) {
                reconnectTimer.cancel();
                reconnectTimer.purge();
                reconnectTimer = null;
            }

            if (mqttClient != null) {
                //mqttClient.close();
                mqttClient.disconnect(null, new MqttActionListener(mContext, MqttAction.DISCONNECT, null));
                mqttClient = null;
            }
        } catch (MqttException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // =============================================================================================
    // Helpers
    // =============================================================================================

    private boolean SendMsg(Object msg, int qos) {
        try {
            if (_Send(mqttPubTopic, msg, qos))
                return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private boolean _Send(String topic, Object mqtt_msg, int qos) {
        try {
            if (publish(topic, qos, gson.toJson(mqtt_msg)))
                return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private void startRx() {
        if (connectionStatus == ConnectionStatus.CONNECTED) {
            if (!RxStarted) {
                if (!subscribedTopics.contains(mqttSubTopic + "#")) {
                    // Init the subscriptions
                    if (subscribe(mqttSubTopic + "#", 2)) {
                        Log.v(TAG, "requested subscription to: ");
                        Log.v(TAG, mqttSubTopic + "#");
                    }
                }
                mqttClient.registerResources(mContext);
            }
        }
    }

    private void stopRx() {
        RxEnabled = false;
        RequestConnectivityUiUpdate();

        // Unsubscribe
        if (RxStarted) {
            RxStarted = false;

            try {
                subscribedTopics.remove(mqttSubTopic + "#");
                if (mqttClient != null) {
                    mqttClient.unsubscribe(mqttSubTopic + "#");
                    mqttClient.unregisterResources();
                }
            } catch (MqttException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    // =============================================================================================
    // MQTT client Reconnection mechanism
    // =============================================================================================
    private Timer reconnectTimer;

    private class ReconnectTask extends TimerTask {

        @Override
        public void run() {
            Log.i(TAG, "Reconnecting to MQTT");
            InitMqttClient();
        }
    }

    private void InitiateReconnectTry() {
        try {
            Log.i(TAG, "MQTT reconnection(" + RECONNECT_PERIOD + ")");

            // if timerTask is cancelled create a new one
            if (reconnectTimer == null)
                reconnectTimer = new Timer();

            reconnectTimer.schedule(new ReconnectTask(), RECONNECT_PERIOD);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    // =============================================================================================
    // MQTT android client
    // =============================================================================================

    private MqttAndroidClient mqttClient;
    private MqttConnectOptions mqttOpt;
    private ConnectionStatus connectionStatus = ConnectionStatus.NONE;
    private boolean RxStarted = false;
    private boolean RxEnabled = false;
    private String mqttPubTopic;
    private String mqttSubTopic;

    private void InitMqttClient() {
        if (mqttClient == null) {
            // Create uri for mqtt and set for secure connection
            String uri = Constants.CloudAPI.Mqtt.Server.URL;

            // Client ID is the AppId
            mqttClient = new MqttAndroidClient(mContext, uri, CloudSettingsManager.getAppId(mContext));
            mqttClient.setCallback(new MqttCallbackHandler(mContext));
            mqttClient.setTraceCallback(new MqttTraceCallback());
            mqttClient.setTraceEnabled(true);

            mqttOpt = new MqttConnectOptions();

            //If a previous session still exists, and cleanSession=true, then the previous session
            //information at the client and client is cleared. If cleanSession=false the previous session is resumed.
            mqttOpt.setCleanSession(true);
        }
        Connect();
    }

    // ---------------------------------------------------------------------------------------------
    private void Connect() {
        try {
            // not to raise exceptions
            String appid = CloudSettingsManager.getAppId(mContext);
            String userid = CloudSettingsManager.getUserID(mContext);
            if (appid == null) {
                throw new IllegalArgumentException("NULL AppID: How is this possible??");
            }

            //check that we aren't already connected or trying to connect
            if (mqttClient == null
                    || mqttClient.isConnected()
                    || connectionStatus == ConnectionStatus.CONNECTING
                    || connectionStatus == ConnectionStatus.CONNECTED) {
                return;
            }
            connectionStatus = ConnectionStatus.CONNECTING;
            Log.d(TAG, "MQTT status:" + connectionStatus);

            subscribedTopics.clear();

            mqttOpt.setUserName(Constants.CloudAPI.Mqtt.Credentials.USERNAME);
            mqttOpt.setPassword(Constants.CloudAPI.Mqtt.Credentials.PASSWORD.toCharArray());

            // Define a topic prefix for this node
            mqttPubTopic = Constants.CloudAPI.Mqtt.Topic.PUBLISH + "/" + appid;
            mqttSubTopic = Constants.CloudAPI.Mqtt.Topic.SUBSCRIBE + "/" + userid + "/";

            //attempt MQTT connection
            mqttClient.connect(mqttOpt, null, new MqttActionListener(mContext, MqttAction.CONNECT, null));

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------

    private boolean subscribe(String topic, int qos) {
        try {
            mqttClient.subscribe(topic,
                    qos,
                    null,
                    new MqttActionListener(mContext, MqttAction.SUBSCRIBE, topic));
            return true;
        } catch (MqttException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    private boolean publish(String topic, int qos, String message) {
        try {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                Log.d(TAG, "MQTT published: " + topic + "  " + message);
                mqttClient.publish(topic,
                        message.getBytes(),
                        qos,
                        false, // retainer
                        null,
                        new MqttActionListener(mContext, MqttAction.PUBLISH, topic));
                return true;
            }
        } catch (MqttException e) {
            Log.e(TAG, e.getMessage());
        }
        return false;
    }

    // =============================================================================================
    // MQTT client callbacks
    // =============================================================================================

    private class MqttCallbackHandler implements MqttCallback {
        private Context context;

        MqttCallbackHandler(Context context) {
            this.context = context;
        }

        // -----------------------------------------------------------------------------------------
        @Override
        public void connectionLost(Throwable throwable) {
            if (throwable != null) {
                if (connectionStatus != ConnectionStatus.DISCONNECTED) {
                    Log.e(TAG, "MQTT connection lost: ");
                    Log.e(TAG, throwable.getMessage());
                    connectionStatus = ConnectionStatus.DISCONNECTED;

                    //update UI
                    RxActive = false;
                    TxActive = false;
                    RequestConnectivityUiUpdate();

                    //send notification of disconnection to Node Service
                    stopRx();

                    //Initiate reconnection procedure
                    InitiateReconnectTry();
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        @Override
        public void messageArrived(String topic, MqttMessage mqttMessage) {

            Log.i(TAG, "MQTT recv topic:");
            Log.i(TAG, topic);

            // Parse message
            if (mqttMessage != null) {
                String mqttMsg_string = new String(mqttMessage.getPayload()); //get string message

                try {
                    ServiceEdgeApiMsg msg = gson.fromJson(mqttMsg_string, ServiceEdgeApiMsg.class);
                    Log.i(TAG, "ServiceEdgeApiMsg: " + mqttMsg_string);
                    if (msg.getEKID() == null || msg.getEKID().equals("") || msg.getEKID().equals(IotSensorsApplication.getApplication().device.address)) {
                        if (msg.getMgmtMsgs() != null) handleMgmtMsgs(msg.getMgmtMsgs());
                        if (msg.getActuations() != null) handleDataMsgs(msg.getEKID(), msg.getActuations());
                    }
                } catch (Exception ex) {
                    listener.onError(ex);
                    Log.e(TAG, ex.getMessage());
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            try {
                if (iMqttDeliveryToken != null) {
                    String[] topics = iMqttDeliveryToken.getTopics();
                    if (topics != null) {
                        Log.i(TAG, "MQTT send complete: ");
                        Log.i(TAG, topics[0]);
                    } else {
                        Log.i(TAG, iMqttDeliveryToken.getMessage().toString());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        // -----------------------------------------------------------------------------------------
    }

    private class MqttActionListener implements IMqttActionListener {
        MqttAction action;
        Context context;
        String additionalArgs;

        MqttActionListener(Context context, MqttAction action, String additionalArgs) {
            this.action = action;
            this.context = context;
            this.additionalArgs = additionalArgs;
        }

        // -----------------------------------------------------------------------------------------
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            switch (action) {
                case CONNECT:
                    OnConnected();
                    break;
                case DISCONNECT:
                    OnDisconnected();
                    break;
                case SUBSCRIBE:
                    OnSubscribed();
                    break;
                case PUBLISH:
                    OnPublish();
                    break;
            }
        }

        // -----------------------------------------------------------------------------------------
        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable exception) {
            switch (action) {
                case CONNECT:
                    OnConnectFailed(exception);
                    break;
                case DISCONNECT:
                    OnDisconnected(exception);
                    break;
                case SUBSCRIBE:
                    OnSubscribed(exception);
                    break;
                case PUBLISH:
                    OnPublish(exception);
                    break;
            }
        }

        // -----------------------------------------------------------------------------------------
        // Internal functions for status
        private void OnConnected() {
            if (connectionStatus != ConnectionStatus.CONNECTED) {

                connectionStatus = ConnectionStatus.CONNECTED;
                Log.d(TAG, "MQTT status:" + connectionStatus);

                RxActive = true;
                TxActive = true;
                RequestConnectivityUiUpdate();

                // check for pending subscription
                startRx();
            }
            // Send mqtt message Tracked Tags Request
            List<MgmtMsg> mgmtMsgs = new ArrayList<>();
            mgmtMsgs.add(new MgmtMsg(eMgmtServiceOperations.AssetTrackingConfigGet, null));
            SendMsg(
                    new EdgeServiceApiMsg(
                            CloudSettingsManager.getUserID(mContext),
                            CloudSettingsManager.getAppId(mContext),
                            mgmtMsgs
                    ));
        }

        private void OnConnectFailed(Throwable exception) {
            if (connectionStatus != ConnectionStatus.ERROR) {
                connectionStatus = ConnectionStatus.ERROR;

                //update UI
                RxActive = false;
                TxActive = false;
                RequestConnectivityUiUpdate();

                Log.d(TAG, "MQTT status: " + connectionStatus);
                Log.d(TAG, "error:" + exception.getMessage());
                InitiateReconnectTry();
            }
        }

        // -----------------------------------------------------------------------------------------
        private void _onDisconnected() {
            connectionStatus = ConnectionStatus.DISCONNECTED;

            TxActive = false;
            RxActive = false;
            RequestConnectivityUiUpdate();

            RxStarted = false;
        }

        // -----------------------------------------------------------------------------------------
        private void OnDisconnected() {
            Log.d(TAG, "Successful MQTT disconnection by request");
            _onDisconnected();
        }

        private void OnDisconnected(Throwable exception) {
            Log.d(TAG, "Disconnection request failed: ");
            Log.d(TAG, exception.getMessage());

            //we tried to disconnect and failed? what does this even mean?
            _onDisconnected();
        }

        // -----------------------------------------------------------------------------------------
        private void OnSubscribed() {
            Log.i(TAG, "Successfully subscribed");

            RxStarted = true;
            subscribedTopics.add(additionalArgs);

            TxActive = true;
            RxActive = true;
            RequestConnectivityUiUpdate();
        }

        private void OnSubscribed(Throwable exception) {
            Log.i(TAG, "Failed to subscribe");

            // If we failed to subscribe rx path set the variable
            if (additionalArgs.startsWith(mqttSubTopic)) {
                RxStarted = false;
            }
        }

        // -----------------------------------------------------------------------------------------
        private void OnPublish() {
            Log.i(TAG, "Successful publish");
        }

        private void OnPublish(Throwable exception) {
            Log.e(TAG, "Failed to publish");

            //TxActive = false; //check...
        }
        // -----------------------------------------------------------------------------------------
    }

    private class MqttTraceCallback implements MqttTraceHandler {

        public void traceDebug(String arg0, String arg1) {
            Log.i(arg0, arg1);
        }

        public void traceError(String arg0, String arg1) {
            Log.e(arg0, arg1);
        }

        public void traceException(String arg0, String arg1,
                                   Exception arg2) {
            Log.e(arg0, arg1, arg2);
        }
    }

    private void handleDataMsgs(String EKID, List<DataMsg> actuations) {
        for (DataMsg dataMsg : actuations) {
            switch (dataMsg.getMsgType()) {
                case (eMgmtEdgeOperations.Custom):
                    // TODO: 29-May-18
                    break;
                case (eActuationTypes.Leds):
                case (eActuationTypes.Buzzer):
                    if (EKID == null) {
                        EKID = IotSensorsApplication.getApplication().device.address;
                        if (EKID == null) break;
                    }
                    listener.onActuationMsg(EKID, dataMsg);
                    break;
            }
        }
    }

    private void handleMgmtMsgs(List<MgmtMsg> msgs) {
        for (MgmtMsg mgmtMsg : msgs) {
            switch (mgmtMsg.getOperationType()) {
                case (eMgmtEdgeOperations.Custom):
                    // TODO: 21-May-18
                    break;
                case (eMgmtEdgeOperations.ThrottlingSet):
                    ThrottlingSet payload = gson.fromJson(mgmtMsg.getPayload(), ThrottlingSet.class);
                    listener.onThrottlingUpdated(payload.getEventTypes(), payload.getSubsamplingFactors());
                    break;
                case (eMgmtEdgeOperations.AssetTrackingConfigSet):
                    listener.onAssetTrackingConfigMsg(gson.fromJson(mgmtMsg.getPayload(), AssetTrackingConfigMsg.class));
                    break;
            }
        }
    }

    // =============================================================================================
    // MQTT enums
    // =============================================================================================

    private enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        ERROR,
        NONE
    }

    private enum MqttAction {
        CONNECT,
        DISCONNECT,
        SUBSCRIBE,
        PUBLISH
    }

    // =============================================================================================
    // Connectivity broadcast
    // =============================================================================================

    // TODO: 14-May-18 if needed

    public static final String CONNECTIVITY_UI_UPDATE = "ServerAPI.CONNECTIVITY_UI_UPDATE";
    public static final String EXTRA_UPDATED_RX_STATE = "EXTRA_UPDATED_RX_STATE";
    public static final String EXTRA_UPDATED_TX_STATE = "EXTRA_UPDATED_TX_STATE";

    public void RequestConnectivityUiUpdate() {
        Intent intent = new Intent(CONNECTIVITY_UI_UPDATE);
        intent.putExtra(EXTRA_UPDATED_RX_STATE, RxActive);
        intent.putExtra(EXTRA_UPDATED_TX_STATE, TxActive);

        LocalBroadcastManager
                .getInstance(mContext)
                .sendBroadcast(intent);
    }
}
