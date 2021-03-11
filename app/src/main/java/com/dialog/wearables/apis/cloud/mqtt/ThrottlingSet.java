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

import java.util.ArrayList;

public class ThrottlingSet {

    private ArrayList<Integer> EventTypes;
    private ArrayList<Integer> SubsamplingFactors;

    public ThrottlingSet(ArrayList<Integer> eventTypes, ArrayList<Integer> subsamplingFactors) {
        this.EventTypes = eventTypes;
        this.SubsamplingFactors = subsamplingFactors;
    }

    public ArrayList<Integer> getEventTypes() {
        return EventTypes;
    }

    public void setEventTypes(ArrayList<Integer> eventTypes) {
        this.EventTypes = eventTypes;
    }

    public ArrayList<Integer> getSubsamplingFactors() {
        return SubsamplingFactors;
    }

    public void setSubsamplingFactors(ArrayList<Integer> subsamplingFactors) {
        this.SubsamplingFactors = subsamplingFactors;
    }
}
