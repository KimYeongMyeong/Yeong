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

public class eControlActuatorValues {

    public static final String On = "On";

    public static final String Off = "Off";

    // helper
    public static Map<String, Boolean> NameToActuatorStateMap = new HashMap<String, Boolean>()
    {{
        put(On, true);
        put(Off, false);

    }};

    public static Map<Boolean, String> ActuatorStateToNameMap = new HashMap<Boolean, String>()
    {{
        put(true, On);
        put(false, Off);

    }};
}
