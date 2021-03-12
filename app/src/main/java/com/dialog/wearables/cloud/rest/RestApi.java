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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.dialog.wearables.apis.cloud.rest.AlertingSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.AmazonAccountInfoReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingSetTagReq;
import com.dialog.wearables.apis.cloud.rest.AssetTrackingTag;
import com.dialog.wearables.apis.cloud.rest.ControlSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.HistoricalGetEnvironmentalReq;
import com.dialog.wearables.apis.cloud.rest.MgmtDeviceApplicationLinkInfo;
import com.dialog.wearables.apis.cloud.rest.SetDeviceReq;
import com.dialog.wearables.apis.cloud.rest.SetIoTAppReq;
import com.yodiwo.amazonbasedavsclientlibrary.AsyncCb;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

public class RestApi {
    private static final String TAG = RestApi.class.getSimpleName();

    // =============================================================================================
    // Constructor
    // =============================================================================================
    private Context context;

    public RestApi(Context context) {
        Log.d(TAG, "Starting REST server API.");
        this.context = context;
    }
    
    // =============================================================================================
    // Requests
    // =============================================================================================

    public void getEkIds(final String userId, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get EKIds for userId: " + userId);
        try {
            listener.start();
            HttpRequest request = RequestFactory.getEkIdsRequest(userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get ekIds for userId " + userId + " : " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getTemperatureData(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get temperature sensor info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getTemperatureDataRequest(historicalGetEnvironmentalReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get data for sensor temperature: " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getHumidityData(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get humidity sensor info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getHumidityDataRequest(historicalGetEnvironmentalReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get data for sensor humidity: " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getPressureData(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get pressure sensor info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getPressureDataRequest(historicalGetEnvironmentalReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get data for sensor pressure:" + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getAirQualityData(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get air quality sensor info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getAirQualityDataRequest(historicalGetEnvironmentalReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get data for sensor air quality: " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getBrightnessData(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get humidity sensor info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getBrightnessDataRequest(historicalGetEnvironmentalReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get data for sensor brightness: " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getProximityData(HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get pressure sensor info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getProximityDataRequest(historicalGetEnvironmentalReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get data for sensor proximity:" + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postWebAppLink(String link, String email, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post web app apikey");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendWebAppLinkRequest(link, email, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send web app link ");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getUserIdByToken(final String token, final String appId,  @NonNull final ResponseListener listener) {
        Log.w(TAG, "get user ID using token");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getUserIDByTokenRequest(token, appId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get user ID for token: " + token);
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getRules(final String userId, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get rules for user id: " + userId);
        try {
            listener.start();
            HttpRequest request = RequestFactory.getRulesRequest(userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get rules for user id " + userId + " : " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postRule(AlertingSetRuleReq alertingSetRuleReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post alerting rule");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendRuleRequest(alertingSetRuleReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send alerting rule ");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getIftttApikey(final String userId, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get Ifttt Apikey");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getIftttApikeyRequest(userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get Ifttt Apikey for userID: " + userId);
                    listener.failure(e);
                }


                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postIftttApikey(final String apikey, final String userId, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post Ifttt Apikey");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendIftttApikeyRequest(apikey, userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to post Ifttt Apikey for userID: " + userId);
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postEKDevice(SetDeviceReq setDeviceReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post device application link info");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendDeviceRequest(setDeviceReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send EKDevice");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postIftttButtonTrigger(String data, String apikey, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post button trigger to IFTTT");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendButtonTriggerToIftttRequest(data, apikey, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send button event using IFTTT");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postTemperatureToIfttt(String data, String apikey, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post temperature to IFTTT");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendTemperatureToIftttRequest(data, apikey, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send temperature event using IFTTT");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postHumidityToIfttt(String data, String apikey, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post humidity to IFTTT");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendHumidityToIftttRequest(data, apikey, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send humidity event using IFTTT");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postPressureToIfttt(String data, String apikey, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post pressure to IFTTT");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendPressureIftttRequest(data, apikey, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send pressure event using IFTTT");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void getCloudRules(final String userId, @NonNull final ResponseListener listener) {
        Log.w(TAG, "get control rules for user id: " + userId);
        try {
            listener.start();
            HttpRequest request = RequestFactory.getCloudRulesRequest(userId, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to get control rules for user id " + userId + " : " + e.getMessage());
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postCloudRule(ControlSetRuleReq controlSetRuleReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post control rule");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendCloudRuleRequest(controlSetRuleReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send control rule ");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postAssetTag(AssetTrackingSetTagReq assetTrackingSetTagReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post asset tag");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendAssetTagRequest(assetTrackingSetTagReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send asset tag ");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postAmazonAccountInfo(AmazonAccountInfoReq amazonAccountInfoReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post AmazonAccountInfo");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendAmazonAccountInfoRequest(amazonAccountInfoReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send AmazonAccountInfo ");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    public void postIoTAppInfo(SetIoTAppReq setIoTAppReq, @NonNull final ResponseListener listener) {
        Log.w(TAG, "post IoTAppInfo");
        try {
            listener.start();
            HttpRequest request = RequestFactory.getSendIoTAppInfoRequest(setIoTAppReq, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Unable to send IoTAppInfo ");
                    listener.failure(e);
                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) {
                    try {
                        listener.success(new HttpResponse(response.body().string(), response.code()));
                    } catch (Exception ex) {
                        Log.e(TAG, ex.getMessage());
                        listener.failure(ex);
                    }
                }
            });
            request.execute();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            listener.failure(ex);
        }
        listener.complete();
    }

    // =============================================================================================
    // Teardown
    // =============================================================================================

    public void Teardown() {
    }

    // =============================================================================================
    // Listeners/Callbacks
    // =============================================================================================

    public interface ResponseListener extends AsyncCb<HttpResponse, Exception> {
    }
}
