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

import com.dialog.wearables.apis.cloud.mqtt.AssetTrackingConfigMsg;
import com.dialog.wearables.apis.cloud.mqtt.eAssetTrackingSetTagsOperations;
import com.dialog.wearables.apis.cloud.mqtt.ScanResult;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ScanResultsThrottlingMechanism {
    private static final int DEFAULT_RSSI_DELTA = 3;
    private static final int DEFAULT_FORCE_SEND_AFTER_SECS = 10;
    private static final int DEFAULT_TRACKED_TAGS_UPDATE_PERIOD = 60;
    private static Integer RssiDelta;
    private static Integer ForceSendAfterSecs;
    private static Integer TrackedTagsUpdatePeriod;
    private static final HashMap<String, ScanResult> currentTrackedTagsMap = new HashMap<>();

    static boolean isAllowed(ScanResult scanResult) {
        return currentTrackedTagsMap.containsKey(scanResult.getTagId());
//        setTrackedTagsDefaults(scanResult);
//        Date currentDate = Calendar.getInstance().getTime();
//        if (currentTrackedTagsMap.get(scanResult.getTagId()) != null) {
//            boolean isAllowed = Math.abs(
//                    scanResult.getRssi() - currentTrackedTagsMap.get(scanResult.getTagId()).getRssi())
//                    > RssiDelta ||
//                    TimeUnit.MILLISECONDS.toSeconds(currentDate.getTime() -
//                            currentTrackedTagsMap.get(scanResult.getTagId()).getTimestamp().getTime())
//                    >= ForceSendAfterSecs;
//            if (isAllowed) {
//                currentTrackedTagsMap.put(scanResult.getTagId(),
//                        new ScanResult(scanResult.getTagId(), scanResult.getRssi(), currentDate));
//            } else {
//                currentTrackedTagsMap.put(scanResult.getTagId(),
//                        new ScanResult(scanResult.getTagId(), scanResult.getRssi(), currentTrackedTagsMap.get(scanResult.getTagId()).getTimestamp()));
//            }
//            return isAllowed;
//        } else {
//            if (currentTrackedTagsMap.containsKey(scanResult.getTagId())) {
//                currentTrackedTagsMap.put(scanResult.getTagId(),
//                    new ScanResult(scanResult.getTagId(), scanResult.getRssi(), currentDate));
//                return false;
//            } else {
//                return false;
//            }
//        }
    }

    public static void reset(String  tagId) {
        currentTrackedTagsMap.put(tagId, null);
    }

    public static void resetConfig(AssetTrackingConfigMsg assetTrackingConfigMsg) {
//        RssiDelta = assetTrackingConfigMsg.getRssiDelta();
//        ForceSendAfterSecs = assetTrackingConfigMsg.getForceSendAfterSecs();
//        TrackedTagsUpdatePeriod = assetTrackingConfigMsg.getTrackedTagsUpdatePeriod();
        switch(assetTrackingConfigMsg.getOperation()) {
            case eAssetTrackingSetTagsOperations.Add:
            case eAssetTrackingSetTagsOperations.Overwrite:
                for (String TagId : assetTrackingConfigMsg.getTrackedTags()) {
                    currentTrackedTagsMap.put(TagId, null);
                }
                break;
            case eAssetTrackingSetTagsOperations.Remove:
                for (String TagId : assetTrackingConfigMsg.getTrackedTags()) {
                    currentTrackedTagsMap.remove(TagId);
                }
                break;
        }

    }

    private static void setTrackedTagsDefaults(ScanResult scanResult) {
//        if (!currentTrackedTagsMap.containsKey(scanResult.getTagId())){
//            currentTrackedTagsMap.put(scanResult.getTagId(), scanResult);
//        }
        if (RssiDelta == null){
            RssiDelta = DEFAULT_RSSI_DELTA;
        }
        if (ForceSendAfterSecs == null) {
            ForceSendAfterSecs = DEFAULT_FORCE_SEND_AFTER_SECS;
        }
        if (TrackedTagsUpdatePeriod == null) {
            TrackedTagsUpdatePeriod = DEFAULT_TRACKED_TAGS_UPDATE_PERIOD;
        }
    }
}
