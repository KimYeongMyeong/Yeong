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

public class eComparisonOperators {

    public static final int Equal = 0;

    public static final int Greater = 1;

    public static final int GreaterOrEqual = 2;

    public static final int Less = 3;

    public static final int LessOrEqual = 4;

    // helper
    public static Map<String, Integer> ComparisonSymbolToTypeMap = new HashMap<String, Integer>()
    {{
        put("=", Equal);
        put(">", Greater);
        put(">=", GreaterOrEqual);
        put("<", Less);
        put("<=", LessOrEqual);

    }};

    public static Map<Integer, String> ComparisonTypeToSymbolMap = new HashMap<Integer, String>()
    {{
        put(Equal, "=");
        put(Greater, ">");
        put(GreaterOrEqual, ">=");
        put(Less, "<");
        put(LessOrEqual, "<=");

    }};
}
