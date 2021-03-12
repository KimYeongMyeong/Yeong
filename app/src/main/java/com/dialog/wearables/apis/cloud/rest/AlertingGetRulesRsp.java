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

public class AlertingGetRulesRsp {

    private List<AlertingRule> Rules;

    public AlertingGetRulesRsp() {
    }

    public AlertingGetRulesRsp(List<AlertingRule> rules) {
        this.Rules = rules;
    }

    public List<AlertingRule> getRules() {
        return Rules;
    }

    public void setRules(List<AlertingRule> rules) {
        this.Rules = rules;
    }

    public int getNumOfActiveRules() {
        int i = 0;
        if (Rules != null && Rules.size() != 0) {
            for (AlertingRule rule: Rules) {
                if (rule.isEnabled()) {
                    i++;
                }
            }
        }
        return i;
    }
}
