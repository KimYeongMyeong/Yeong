/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.apis.cloud.rest;

import java.util.HashMap;
import java.util.Map;

public class eAlertingSensorTypes {

    public static final int Temperature = 0;

    public static final int Humidity = 1;

    public static final int Pressure = 2;

    public static final int AirQuality = 3;

    public static final int Brightness = 4;

    public static Map<String, Integer> AlertingSensorNameToTypeMap = new HashMap<String, Integer>()
    {{
        put("Temperature", Temperature);
        put("Humidity", Humidity);
        put("Pressure", Pressure);
        put("AirQuality", AirQuality);
        put("Brightness", Brightness);
    }};

    public static Map<Integer, String> AlertingSensorTypeToNameMap = new HashMap<Integer, String>()
    {{
        put(Temperature, "Temperature");
        put(Humidity, "Humidity");
        put(Pressure, "Pressure");
        put(AirQuality, "AirQuality");
        put(Brightness, "Brightness");
    }};
}
