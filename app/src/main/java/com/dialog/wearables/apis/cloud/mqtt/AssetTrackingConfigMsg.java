/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.apis.cloud.mqtt;

import java.util.List;

public class AssetTrackingConfigMsg {

    private List<String> TrackedTags;

    private int RssiDelta;

    private int ForceSendAfterSecs;

    private int TrackedTagsUpdatePeriod;

    /**
     * @see eAssetTrackingSetTagsOperations
     */
    private int Operation;

    public AssetTrackingConfigMsg() {
    }

    public AssetTrackingConfigMsg(List<String> trackedTags, int rssiDelta, int forceSendAfterSecs, int trackedTagsUpdatePeriod, int operation) {
        this.TrackedTags = trackedTags;
        this.RssiDelta = rssiDelta;
        this.ForceSendAfterSecs = forceSendAfterSecs;
        this.TrackedTagsUpdatePeriod = trackedTagsUpdatePeriod;
        this.Operation = operation;
    }

    public List<String> getTrackedTags() {
        return TrackedTags;
    }

    public void setTrackedTags(List<String> trackedTags) {
        this.TrackedTags = trackedTags;
    }

    public int getRssiDelta() {
        return RssiDelta;
    }

    public void setRssiDelta(int rssiDelta) {
        this.RssiDelta = rssiDelta;
    }

    public int getForceSendAfterSecs() {
        return ForceSendAfterSecs;
    }

    public void setForceSendAfterSecs(int forceSendAfterSecs) {
        this.ForceSendAfterSecs = forceSendAfterSecs;
    }

    public int getTrackedTagsUpdatePeriod() {
        return TrackedTagsUpdatePeriod;
    }

    public void setTrackedTagsUpdatePeriod(int trackedTagsUpdatePeriod) {
        this.TrackedTagsUpdatePeriod = trackedTagsUpdatePeriod;
    }

    public int getOperation() {
        return Operation;
    }

    public void setOperation(int operation) {
        this.Operation = operation;
    }
}
