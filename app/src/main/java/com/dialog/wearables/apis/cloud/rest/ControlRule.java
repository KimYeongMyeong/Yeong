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

public class ControlRule {

    private String Id; // RuleId

    private String UserId;

    private String EKID;

    private String Name;

    private String FriendlyDescription;

    /**
     * @see eComparisonOperators for available comparison operators
     */
    private int OperatorType;

    private float Value;

    /**
     * @see eControlCloudRuleConditions for available conditions
     */
    private int Condition; // Temperature, FOREX symbol

    private String SubCondition;

    private String City;

    /**
     * @see eControlActuators for available actuators
     */
    private int ActuatorType;

    private boolean ActuatorValue;

    private String LastUpdated;

    private boolean IsEnabled;

    public ControlRule(String userId, String EKID) {
        setUserId(userId);
        setEKID(EKID);
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        this.UserId = userId;
    }

    public String getEKID() {
        return EKID;
    }

    public void setEKID(String EKID) {
        this.EKID = EKID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public String getFriendlyDescription() {
        return FriendlyDescription;
    }

    public void createFriendlyDescription() {
        setFriendlyDescription((getUserId() != null && !getUserId().trim().isEmpty()
                && getSubCondition() != null && !getSubCondition().trim().isEmpty()
                && getEKID() != null && !getEKID().trim().isEmpty()) ?
                String.format("If %s, %s %s is %s %s then trigger %s %s",
                        eControlCloudRuleConditions.ConditionTypeToNameMap.get(getCondition()),
                        getSubCondition(),
                        getCity(),
                        eComparisonOperators.ComparisonTypeToSymbolMap.get(getOperatorType()),
                        getValue(),
                        eControlActuators.ActuatorTypeToNameMap.get(getActuatorType()),
                        eControlActuatorValues.ActuatorStateToNameMap.get(isActuatorValue()))
                : "");
    }

    public void setFriendlyDescription(String friendlyDescription) {
        this.FriendlyDescription = friendlyDescription;
    }

    public int getOperatorType() {
        return OperatorType;
    }

    public void setOperatorType(int operatorType) {
        this.OperatorType = operatorType;
    }

    public float getValue() {
        return Value;
    }

    public void setValue(float value) {
        this.Value = value;
    }

    public int getCondition() {
        return Condition;
    }

    public void setCondition(int condition) {
        this.Condition = condition;
    }

    public String getSubCondition() {
        return SubCondition;
    }

    public void setSubCondition(String subCondition) {
        this.SubCondition = subCondition;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String city) {
        this.City = city;
    }

    public int getActuatorType() {
        return ActuatorType;
    }

    public void setActuatorType(int actuatorType) {
        this.ActuatorType = actuatorType;
    }

    public boolean isActuatorValue() {
        return ActuatorValue;
    }

    public void setActuatorValue(boolean actuatorValue) {
        ActuatorValue = actuatorValue;
    }

    public String getLastUpdated() {
        return LastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.LastUpdated = lastUpdated;
    }

    public boolean isEnabled() {
        return IsEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.IsEnabled = enabled;
    }

    //----------------------------------------------------------------------------------------------
    public boolean isValid() {
        return (getUserId() != null && !getUserId().trim().isEmpty()
                && getSubCondition() != null && !getSubCondition().trim().isEmpty()
                && getEKID() != null && !getEKID().trim().isEmpty());
    }
    //----------------------------------------------------------------------------------------------
}
