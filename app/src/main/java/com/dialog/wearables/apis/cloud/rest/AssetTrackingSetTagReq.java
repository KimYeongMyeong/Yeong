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

public class AssetTrackingSetTagReq {

    /**
    * @see eAssetTrackingOperationTypes
    */
    private int OperationType;

    private AssetTrackingTag Tag;

    public AssetTrackingSetTagReq() {
    }

    public AssetTrackingSetTagReq(int operationType, AssetTrackingTag tag) {
        OperationType = operationType;
        Tag = tag;
    }

    public int getOperationType() {
        return OperationType;
    }

    public void setOperationType(int operationType) {
        OperationType = operationType;
    }

    public AssetTrackingTag getTag() {
        return Tag;
    }

    public void setTag(AssetTrackingTag tag) {
        Tag = tag;
    }
}
