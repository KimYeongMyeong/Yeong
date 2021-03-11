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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class eControlCloudRuleSubConditionsWeather {

    public static final int Temperature = 0;
    // helper
    public static Map<String, Integer> NameToSubConditionWeatherTypeMap = new HashMap<String, Integer>()
    {{
        put("Temperature", Temperature);

    }};

    public static Map<Integer, String> SubConditionWeatherTypeToNameMap = new HashMap<Integer, String>()
    {{
        put(Temperature, "Temperature");

    }};
}
