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

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.settings.CloudSettingsManager;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class IftttThrottlingMechanism {
    private static final HashMap<Integer, Date> currentIftttMap = new HashMap<>();
    private static final HashMap<Integer, Integer> cloudIftttConfig = new HashMap<>(); //

    static boolean isAllowed(int eventType) {
        setIftttDefaults(eventType);
        if (currentIftttMap.get(eventType) != null){
            Date currentDate = Calendar.getInstance().getTime();
            boolean isAllowed =TimeUnit.MILLISECONDS.toSeconds
                    (currentDate.getTime() - currentIftttMap.get(eventType).getTime())
                    >=
                    cloudIftttConfig.get(eventType);
            if (isAllowed) currentIftttMap.put(eventType, currentDate);
            return isAllowed;
        } else {
            return false;
        }
    }

    public static void reset(int eventType, int seconds) {
        cloudIftttConfig.put(eventType, seconds); // update config
        currentIftttMap.put(eventType, Calendar.getInstance().getTime()); // reset counter
    }

    private static void setIftttDefaults(int eventType) {
        if (!currentIftttMap.containsKey(eventType)){
            currentIftttMap.put(eventType, Calendar.getInstance().getTime());
        }
        if (!cloudIftttConfig.containsKey(eventType)){
            cloudIftttConfig.put(eventType, Integer.parseInt(CloudSettingsManager.getTriggerInterval(IotSensorsApplication.getApplication().getApplicationContext()))); // default value
        }
    }
}
