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

public class ControlSetRuleReq {

    private String APPID;

    /**
      * @see eControlSetRuleOperationTypes for available operation types
      */
    private int OperationType;

    private ControlRule Rule;

    public ControlSetRuleReq() {
    }

    public ControlSetRuleReq(String APPID, int operationType, ControlRule rule) {
        this.APPID = APPID;
        this.OperationType = operationType;
        this.Rule = rule;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setAPPID(String APPID) {
        this.APPID = APPID;
    }

    public int getOperationType() {
        return OperationType;
    }

    public void setOperationType(int operationType) {
        this.OperationType = operationType;
    }

    public ControlRule getRule() {
        return Rule;
    }

    public void setRule(ControlRule rule) {
        this.Rule = rule;
    }
}
