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

import java.util.List;

public class ControlGetRulesRsp {

    private List<ControlRule> ControlRules;

    public ControlGetRulesRsp() {
    }

    public ControlGetRulesRsp(List<ControlRule> controlRules) {
        this.ControlRules = controlRules;
    }

    public List<ControlRule> getControlRules() {
        return ControlRules;
    }

    public void setControlRules(List<ControlRule> controlRules) {
        this.ControlRules = controlRules;
    }

    public int getNumOfActiveCloudRules() {
        int i = 0;
        if (ControlRules != null && ControlRules.size() != 0) {
            for (ControlRule rule: ControlRules) {
                if (rule.isEnabled()) {
                    i++;
                }
            }
        }
        return i;
    }
}
