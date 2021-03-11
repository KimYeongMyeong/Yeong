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

import android.net.Uri;

import com.dialog.wearables.apis.Constants;
import com.dialog.wearables.apis.cloud.rest.HistoricalGetEnvironmentalReq;

public class UrlFactory {
    private static String TAG = UrlFactory.class.getSimpleName();

     public static String getEkIdsUrl(String userId) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.Management.G_EKID.NAME)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.UserId.NAME, userId)
                .build()
                .toString();
    }

    public static String getTemperatureDataUrl(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return getSensorInfoUrl(Constants.CloudAPI.Rest.Subroutes.IotApps.G_TEMPERATURE.NAME, historicalGetEnvironmentalReq);
    }

    public static String getHumidityDataUrl(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return getSensorInfoUrl(Constants.CloudAPI.Rest.Subroutes.IotApps.G_HUMIDITY.NAME, historicalGetEnvironmentalReq);
    }

    public static String getPressureDataUrl(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return getSensorInfoUrl(Constants.CloudAPI.Rest.Subroutes.IotApps.G_PRESSURE.NAME, historicalGetEnvironmentalReq);
    }

    public static String getAirQualityDataUrl(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return getSensorInfoUrl(Constants.CloudAPI.Rest.Subroutes.IotApps.G_AIRQUALITY.NAME, historicalGetEnvironmentalReq);
    }

    public static String getBrightnessDataUrl(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return getSensorInfoUrl(Constants.CloudAPI.Rest.Subroutes.IotApps.G_BRIGHTNESS.NAME, historicalGetEnvironmentalReq);
    }

    public static String getProximityDataUrl(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return getSensorInfoUrl(Constants.CloudAPI.Rest.Subroutes.IotApps.G_PROXIMITY.NAME, historicalGetEnvironmentalReq);
    }

    private static String getSensorInfoUrl(String subPathReq, HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.IOTAPPS.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.HISTORICAL.NAME)
                .appendPath(subPathReq)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.StartDate.NAME, historicalGetEnvironmentalReq.getStartDate())
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.EndDate.NAME, historicalGetEnvironmentalReq.getEndDate())
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.EKID.NAME, historicalGetEnvironmentalReq.getEkId())
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.APPID.NAME, historicalGetEnvironmentalReq.getAppId())
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.UserId.NAME, historicalGetEnvironmentalReq.getUserId())
                .build()
                .toString();
    }

    public static String getUserIdByTokenUrl(String token, String appId) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.Management.G_USERIDBYTOKEN.NAME)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.TOKEN.NAME, token)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.APPID.NAME, appId)
                .build()
                .toString();
    }

    public static String getYodiboardsUrl(String userId) {
        return "https://"
                + Constants.Endpoints.YODIBOARDS
                + "?"
                + Constants.CloudAPI.Rest.Parameters.UserId.NAME + "=" + userId + "#/";
         /* todo
         return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .authority()
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.UserId.NAME, userId)
                .build()
                .toString();
                */
    }

    public static String getIftttApikeyUrl(String userId) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.G_IFTTTAPIKEY.NAME)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.UserId.NAME, userId)
                .build()
                .toString();
    }

    public static String getRulesUrl(String userId) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.IOTAPPS.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.ALERTING.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.G_GETRULES.NAME)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.UserId.NAME, userId)
                .build()
                .toString();
    }

    public static String getCloudRulesUrl(String userId) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.IOTAPPS.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.CONTROL.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.G_GETRULES.NAME)
                .appendQueryParameter(Constants.CloudAPI.Rest.Parameters.UserId.NAME, userId)
                .build()
                .toString();
    }

    // ---------------------------------------------------------------------------------------------

    public static String getSendWebAppLinkUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.Management.P_WEBAPPLINK.NAME)
                .build()
                .toString();
    }

    public static String getSendIftttApikeyUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.P_IFTTTAPIKEY.NAME)
                .build()
                .toString();
    }

    public static String getSendRuleUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.IOTAPPS.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.ALERTING.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.P_SETRULE.NAME)
                .build()
                .toString();
    }

    public static String getSendCloudRuleUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.IOTAPPS.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.CONTROL.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.P_SETRULE.NAME)
                .build()
                .toString();
    }

    public static String getSendAssetTagUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.IOTAPPS.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.TRACKING.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.P_SETASSETTAG.NAME)
                .build()
                .toString();
    }

    public static String getSendAmazonAccountInfoUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.IotApps.P_AMAZONINFO.NAME)
                .build()
                .toString();
    }

    public static String getSendIoTAppInfoUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.Management.P_SETIOTAPPINFO.NAME)
                .build()
                .toString();
    }

    public static String getSendDeviceUrl() {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Rest.Server.URL)
                .appendPath(Constants.CloudAPI.Rest.Routes.DIALOG.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.EDGE.NAME)
                .appendPath(Constants.CloudAPI.Rest.Routes.MGMT.NAME)
                .appendPath(Constants.CloudAPI.Rest.Subroutes.Management.P_SETDEVICE.NAME)
                .build()
                .toString();
    }

    // -----------------------------------IFTTT Urls------------------------------------------------

    public static String getSendButtonTriggerToIftttUrl(String apikey) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Ifttt.Server.URI)
                .appendEncodedPath(Constants.CloudAPI.Ifttt.Routes.TRIGGER_BUTTON_WITH_KEY)
                .appendPath(apikey)
                .build()
                .toString();
    }

    public static String getSendTemperatureToIftttUrl(String apikey) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Ifttt.Server.URI)
                .appendEncodedPath(Constants.CloudAPI.Ifttt.Routes.TRIGGER_TEMPERATURE_WITH_KEY)
                .appendPath(apikey)
                .build()
                .toString();
    }

    public static String getSendHumidityToIftttUrl(String apikey) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Ifttt.Server.URI)
                .appendEncodedPath(Constants.CloudAPI.Ifttt.Routes.TRIGGER_HUMIDITY_WITH_KEY)
                .appendPath(apikey)
                .build()
                .toString();
    }

    public static String getSendPressureToIftttUrl(String apikey) {
        return new Uri.Builder()
                .scheme(Constants.CloudAPI.Rest.Server.SCHEME)
                .encodedAuthority(Constants.CloudAPI.Ifttt.Server.URI)
                .appendEncodedPath(Constants.CloudAPI.Ifttt.Routes.TRIGGER_PRESSURE_WITH_KEY)
                .appendPath(apikey)
                .build()
                .toString();
    }
    // ---------------------------------------------------------------------------------------------
}
