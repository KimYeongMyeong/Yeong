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

public class eControlCloudRuleConditions {

    public static final int Weather = 0;

    public static final int Forex = 1;

    // helper
    public static Map<String, Integer> NameToConditionTypeMap = new HashMap<String, Integer>()
    {{
        put("Weather", Weather);
        put("Forex", Forex);

    }};

    public static Map<Integer, String> ConditionTypeToNameMap = new HashMap<Integer, String>()
    {{
        put(Weather, "Weather");
        put(Forex, "Forex");

    }};
}
