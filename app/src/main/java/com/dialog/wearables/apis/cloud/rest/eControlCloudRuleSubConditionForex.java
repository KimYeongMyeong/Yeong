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

public class eControlCloudRuleSubConditionForex {

    public static final int EURUSD = 0;

    public static final int USDJPY = 1;

    public static final int GBPUSD = 2;

    public static final int USDCHF = 3;

    public static final int EURGBP = 4;

    public static final int EURJPY = 5;

    public static final int EURCHF = 6;

    public static final int AUDUSD = 7;

    public static final int USDCAD = 8;

    public static final int NZDUSD = 9;
    // helper
    public static Map<String, Integer> NameToSubConditionForexTypeMap = new HashMap<String, Integer>()
    {{
        put("EURUSD", EURUSD);
        put("USDJPY", USDJPY);
        put("GBPUSD", GBPUSD);
        put("USDCHF", USDCHF);
        put("EURGBP", EURGBP);
        put("EURJPY", EURJPY);
        put("EURCHF", EURCHF);
        put("AUDUSD", AUDUSD);
        put("USDCAD", USDCAD);
        put("NZDUSD", NZDUSD);
    }};

    public static Map<Integer, String> SubConditionForexTypeToNameMap = new HashMap<Integer, String>()
    {{
        put(EURUSD, "EURUSD");
        put(USDJPY, "USDJPY");
        put(GBPUSD, "GBPUSD");
        put(USDCHF, "USDCHF");
        put(EURGBP, "EURGBP");
        put(EURJPY, "EURJPY");
        put(EURCHF, "EURCHF");
        put(AUDUSD, "AUDUSD");
        put(USDCAD, "USDCAD");
        put(NZDUSD, "NZDUSD");
    }};
}
