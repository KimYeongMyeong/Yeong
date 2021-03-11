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

import com.dialog.wearables.apis.cloud.rest.AlertingSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.AmazonAccountInfoReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingSetTagReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingTag;
import com.dialog.wearables.apis.cloud.rest.ControlSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.HistoricalGetEnvironmentalReq;
import com.dialog.wearables.apis.cloud.rest.MgmtDeviceApplicationLinkInfo;
import com.dialog.wearables.apis.cloud.rest.MgmtIftttInfo;
import com.dialog.wearables.apis.cloud.rest.MgmtWebAppLinkInfo;
import com.dialog.wearables.apis.cloud.rest.SetDeviceReq;
import com.dialog.wearables.apis.cloud.rest.SetIoTAppReq;
import com.google.gson.Gson;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;

public class RequestFactory {

    public static HttpRequest getEkIdsRequest(String userId, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getEkIdsUrl(userId))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getTemperatureDataRequest(HistoricalGetEnvironmentalReq info, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getTemperatureDataUrl(info))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getHumidityDataRequest(HistoricalGetEnvironmentalReq info, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getHumidityDataUrl(info))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getPressureDataRequest(HistoricalGetEnvironmentalReq info, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getPressureDataUrl(info))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getAirQualityDataRequest(HistoricalGetEnvironmentalReq info, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getAirQualityDataUrl(info))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getBrightnessDataRequest(HistoricalGetEnvironmentalReq info, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getBrightnessDataUrl(info))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getProximityDataRequest(HistoricalGetEnvironmentalReq info, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getProximityDataUrl(info))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getUserIDByTokenRequest(String token, String appId, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getUserIdByTokenUrl(token, appId))
                .setCallback(callback)
                .GET();
    }


    public static HttpRequest getIftttApikeyRequest(String userId, Callback callback) {
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getIftttApikeyUrl(userId))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getRulesRequest(String userId, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getRulesUrl(userId))
                .setCallback(callback)
                .GET();
    }

    public static HttpRequest getCloudRulesRequest(String userId, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getCloudRulesUrl(userId))
                .setCallback(callback)
                .GET();
    }
    
    // ----------------------------------------------------------------------------------------------------

    public static HttpRequest getSendIftttApikeyRequest(String apikey, String userId, Callback callback) {
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendIftttApikeyUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(new MgmtIftttInfo(apikey, userId))));
    }

    public static HttpRequest getSendWebAppLinkRequest(String link, String userEmail, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendWebAppLinkUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(new MgmtWebAppLinkInfo(link, userEmail))));
    }

    public static HttpRequest getSendRuleRequest(AlertingSetRuleReq alertingSetRuleReq, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendRuleUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(alertingSetRuleReq)));
    }

    public static HttpRequest getSendCloudRuleRequest(ControlSetRuleReq controlSetRuleReq, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendCloudRuleUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(controlSetRuleReq)));
    }

    public static HttpRequest getSendAssetTagRequest(AssetTrackingSetTagReq assetTrackingSetTagReq, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendAssetTagUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(assetTrackingSetTagReq)));
    }

    public static HttpRequest getSendAmazonAccountInfoRequest(AmazonAccountInfoReq amazonAccountInfoReq, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendAmazonAccountInfoUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(amazonAccountInfoReq)));
    }

    public static HttpRequest getSendIoTAppInfoRequest(SetIoTAppReq setIoTAppReq, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendIoTAppInfoUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(setIoTAppReq)));
    }

    public static HttpRequest getSendDeviceRequest(SetDeviceReq setDeviceReq, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendDeviceUrl())
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("text/plain; charset=utf-8"),
                        new Gson().toJson(setDeviceReq)));
    }

    public static HttpRequest getSendButtonTriggerToIftttRequest(String data, String apikey, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendButtonTriggerToIftttUrl(apikey))
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        data));
    }

    public static HttpRequest getSendTemperatureToIftttRequest(String data, String apikey, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendTemperatureToIftttUrl(apikey))
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        data));
    }

    public static HttpRequest getSendHumidityToIftttRequest(String data, String apikey, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendHumidityToIftttUrl(apikey))
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        data));
    }

    public static HttpRequest getSendPressureIftttRequest(String data, String apikey, Callback callback){
        return new HttpRequest.Builder()
                .setUrl(UrlFactory.getSendPressureToIftttUrl(apikey))
                .setCallback(callback)
                .POST(RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                        data));
    }
    // ---------------------------------------------------------------------------------------------
}
