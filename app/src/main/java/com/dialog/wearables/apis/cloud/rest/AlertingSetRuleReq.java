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

public class AlertingSetRuleReq {

    private String APPID;

    /**
     * @see eAlertingSetRuleOperationTypes for available operation types
     */
    private int OperationType;

    private AlertingRule Rule;

    public AlertingSetRuleReq() {
    }

    public AlertingSetRuleReq(String APPID, int operationType, AlertingRule rule) {
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

    public AlertingRule getRule() {
        return Rule;
    }

    public void setRule(AlertingRule rule) {
        this.Rule = rule;
    }
}
