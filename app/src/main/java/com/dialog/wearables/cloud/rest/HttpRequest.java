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

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpRequest {

    private String url;
    private RequestBody body;
    private Callback callback;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.callback = builder.callback;
        this.body = builder.body;
    }

    //Builder Class
    public static class Builder {
        private String url;
        private Callback callback;
        private RequestBody body;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Callback getCallback() {
            return this.callback;
        }

        public HttpRequest GET() {
            return new HttpRequest(this);
        }

        public HttpRequest POST(RequestBody body) {
            this.body = body;
            return new HttpRequest(this);
        }
    }

    public void execute() {
        OkHttpClient client = HttpClient.getClient();

        Request.Builder rbuilder = new Request.Builder()
                .url(url);
        if (body != null) {
            rbuilder.post(body);
        } else {
            rbuilder.get();
        }

        client.newCall(rbuilder.build()).enqueue(callback);
    }
}
