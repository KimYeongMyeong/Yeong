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

public class eControlActuators {

    public static final int Led = 0;

    public static final int Buzzer = 1;

    // helper
    public static Map<String, Integer> NameToActuatorTypeMap = new HashMap<String, Integer>()
    {{
        put("Led", Led);
        put("Buzzer", Buzzer);

    }};

    public static Map<Integer, String> ActuatorTypeToNameMap = new HashMap<Integer, String>()
    {{
        put(Led, "Led");
        put(Buzzer, "Buzzer");

    }};
}
