/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.cloud;

import com.dialog.wearables.apis.common.eEventTypes;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ThrottlingMechanism {
    private static final int DEFAULT_SAMPLES = 30;
    private static final HashMap<Integer, AtomicInteger> currentMap = new HashMap<>();
    private static final HashMap<Integer, Integer> cloudConfig = new HashMap<>(); // cloud configuration
    private static String lastProximityValue = null;

    static boolean isAllowed(int eventType) {
        if (eventType == eEventTypes.Fusion || eventType == eEventTypes.Proximity) return true;
        setDefaults(eventType);

        boolean result = currentMap.get(eventType).compareAndSet(cloudConfig.get(eventType), 1);
        if (!result){
            currentMap.get(eventType).incrementAndGet();
        }
        return result;
    }

    static boolean isProximityChanged(String value) {
        boolean result = (lastProximityValue == null ||  !value.equals(lastProximityValue));
        lastProximityValue = value;
        return result;
    }

    static void reset(int eventType, int samples) {
        cloudConfig.put(eventType, samples); // update config
        currentMap.put(eventType, new AtomicInteger(1)); // reset counter
    }

    private static void setDefaults(int eventType) {
        if (!currentMap.containsKey(eventType)){
            currentMap.put(eventType, new AtomicInteger(1));
        }
        if (!cloudConfig.containsKey(eventType)){
            cloudConfig.put(eventType, DEFAULT_SAMPLES); // default value
        }
    }
}
