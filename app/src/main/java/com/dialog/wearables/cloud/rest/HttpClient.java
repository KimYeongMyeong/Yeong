/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.cloud.rest;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class HttpClient {

    // =============================================================================================
    // Variables
    // =============================================================================================
    private static OkHttpClient mClient;
    private static final long CONNECTION_POOL_TIMEOUT_MILLISECONDS = 60 * 60 * 1000;
    private static final long CONNECTION_TIMEOUT_MILLISECONDS = 30 * 1000;

    // =============================================================================================
    // Singleton
    // =============================================================================================
    private HttpClient() {
    }

    public static OkHttpClient getClient() {
        if (mClient == null) {
            mClient = getTLS12Client();
        }
        return mClient;
    }

    private static OkHttpClient getTLS12Client() {
        ConnectionPool connectionPool = new ConnectionPool(5,
                CONNECTION_POOL_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS);
        OkHttpClient.Builder client = new OkHttpClient.Builder().connectTimeout(CONNECTION_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)  // 0 => no timeout.
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectionPool(connectionPool);
        return client.build();
    }
}
