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

public class ActuationMsg {
    // TODO: 23-May-18 to be removed?

    /**
     * Unique EKID of message target
     */
    public String EKID;

    /**
     * Types of target actuators
     */
    public int[] ActuatorType;

    /**
     * Values to set each actuator to,
     * for all actuators present in the   ActuatorType array.
     * For example if ActuatorType is [0] then only value of LedsValue will be taken into consideration.
     */
    public int[] BuzzerValue;
    public int[] LedsValue;
}
