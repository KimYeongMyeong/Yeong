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

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dialog.wearables.apis.Constants;
import com.dialog.wearables.apis.internal.DataMsg;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

import java.util.HashMap;

public class DataManager extends IntentService {

    // =============================================================================================
    // Constants
    // =============================================================================================

    private static final String TAG = DataManager.class.getSimpleName();

    public static final String EXTRA_REQUEST_TYPE = "EXTRA_REQUEST_TYPE";
    public static final String EXTRA_DATA = "EXTRA_DATA";

    public static final int REQUEST_SEND_DATA_MSG = 1;
    private static final int REQUEST_INITMQTT = 2;
    private static final int REQUEST_TEARDOWN = 3;
    private static final int REQUEST_STOPMQTT = 4;
    // TODO: 29-May-18
    // private static final int REQUEST_SEND_ACTUATION_MSG = 3;

    // =============================================================================================
    // Variables
    // =============================================================================================

    private DataMessenger mDataMessenger;
    private Gson gson = new Gson();

    // =============================================================================================
    // Constructor
    // =============================================================================================

    public DataManager(String name) {
        super(name);
    }

    public DataManager() {
        super(TAG);
    }

    // =============================================================================================
    // Intent Handler
    // =============================================================================================

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Log.d(TAG, "onHandleIntent invoked");

        Bundle bundle = null;
        int request_type = -1;
        Context context = getApplicationContext();

        // init
        mDataMessenger = DataMessenger.getInstance();
        registerRxHandlers();
        try {
            bundle = intent.getExtras();
            request_type = bundle.getInt(EXTRA_REQUEST_TYPE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        // handle incoming intent requests by type
        try {
            //Log.d(TAG, "Handle incoming request with type: " + request_type);

            switch (request_type) {
                // -------------------------------------
                case REQUEST_SEND_DATA_MSG:
                    DataMsg dataMsg = bundle.getParcelable(EXTRA_DATA);
                    handleRxEvent(dataMsg);
                    break;
                // -------------------------------------
                //    TODO: 29-May-18
//                case REQUEST_SEND_ACTUATION_MSG:
//                    ActuationMsg actuationMsg = bundle.getParcelable(EXTRA_DATA);
//                    handleRxEvent(actuationMsg);
//                    break;
                // -------------------------------------
                case REQUEST_TEARDOWN:
                    try {
                        if (mDataMessenger != null) {
                            mDataMessenger.Teardown();
                        }
                        this.stopSelf();
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                    break;
                case REQUEST_INITMQTT:
                    try {
                        if (mDataMessenger != null) {
                            mDataMessenger.InitMqtt();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                    break;
                case REQUEST_STOPMQTT:
                    try {
                        if (mDataMessenger != null) {
                            mDataMessenger.StopMqtt();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                    }
                    break;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    // =============================================================================================
    // Internal API Rx handlers
    // =============================================================================================

    private static HashMap<String, RxHandler> rxHandlers = null;
    private static HashMap<String, Class<?>> rxHandlersClass = null;

    interface RxHandler {
        void onHandle(Object msg);
    }

    private void registerRxHandlers() {
        if (rxHandlers == null) {
            rxHandlers = new HashMap<>();
            rxHandlersClass = new HashMap<>();

            // ---------------------> DataMsg
            rxHandlersClass.put(Constants.InternalAPI.DataMsg.NAME, Constants.InternalAPI.DataMsg.CLASS);
            rxHandlers.put(Constants.InternalAPI.DataMsg.NAME,
                    new RxHandler() {
                        @Override
                        public void onHandle(Object msg) {
                            DataMsg req = (DataMsg) msg;
                            rxDataMsgSendReq(req);
                        }
                    });
            // TODO: 29-May-18
            // ---------------------> ActuationMsg
//            rxHandlersClass.put(Constants.InternalAPI.ActuationMsg.NAME, Constants.InternalAPI.ActuationMsg.CLASS);
//            rxHandlers.put(Constants.InternalAPI.ActuationMsg.NAME,
//                    new RxHandler() {
//                        @Override
//                        public void onHandle(Object msg) {
//                            ActuationMsg req = (ActuationMsg) msg;
//                            rxActuationMsgSendReq(req);
//                        }
//                    });
        }
    }

    // =============================================================================================
    // Trigger registered RX handlers
    // =============================================================================================

    private void handleRxEvent(Object msg) {
        if (msg == null)
            return;

        String className = msg.getClass().getSimpleName();
        //Log.d(TAG, "handleRxEvent for class: " + className);
        try {
            RxHandler handler;
            if (rxHandlers.containsKey(className)) {
                handler = rxHandlers.get(className);
                handler.onHandle(msg);
            } else {
                //unknown message
                throw new Exception("UNKNOWN MESSAGE ARRIVED: How is this possible?");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    // =============================================================================================
    // Helpers
    // =============================================================================================

    private void rxDataMsgSendReq(DataMsg dataMsg) {
        //Log.d(TAG, "START rxDataMsgSendReq");
        mDataMessenger.sendSensorData(dataMsg.EKID, dataMsg.Events);
        //Log.d(TAG, "STOP rxDataMsgSendReq");
    }
    // TODO: 29-May-18
//    private void rxActuationMsgSendReq(ActuationMsg actuationMsg) {
//        Log.d(TAG, "START rxActuationMsgSendReq");
//        Log.d(TAG, "STOP rxActuationMsgSendReq");
//    }

    // =============================================================================================
    // Public internal API
    // =============================================================================================

    public static void sendDataMsg(Context context, DataMsg dataMsg) {
        if (CloudSettingsManager.isCloudEnabled(context)) {
            Intent intent = new Intent(context, DataManager.class);
            intent.putExtra(EXTRA_REQUEST_TYPE, REQUEST_SEND_DATA_MSG);
            intent.putExtra(EXTRA_DATA, dataMsg);
            context.startService(intent);
        }
    }

    // ---------------------------------------------------------------------------------------------

//    TODO: 29-May-18
//    public static void sendActuationMsg(Context context, ActuationMsg actuationMsg) {
//        Log.d(TAG, "SendActuationMsg requested");
//        Intent intent = new Intent(context, DataManager.class);
//        intent.putExtra(EXTRA_REQUEST_TYPE, REQUEST_SEND_ACTUATION_MSG);
//        intent.putExtra(EXTRA_DATA, actuationMsg);
//        context.startService(intent);
//    }

    // ---------------------------------------------------------------------------------------------

    public static void TearDown(Context context) {
        Log.d(TAG, "TearDown requested");
        Intent intent = new Intent(context, DataManager.class);
        intent.putExtra(EXTRA_REQUEST_TYPE, REQUEST_TEARDOWN);
        context.startService(intent);
    }

    public static void InitMqtt(Context context) {
        if (CloudSettingsManager.isCloudEnabled(context)) {
            Log.d(TAG, "Mqtt connection requested");
            Intent intent = new Intent(context, DataManager.class);
            intent.putExtra(EXTRA_REQUEST_TYPE, REQUEST_INITMQTT);
            context.startService(intent);
        }
    }

    public static void StopMqtt(Context context) {
        Log.d(TAG, "Mqtt disconnection requested");
        Intent intent = new Intent(context, DataManager.class);
        intent.putExtra(EXTRA_REQUEST_TYPE, REQUEST_STOPMQTT);
        context.startService(intent);
    }
}
