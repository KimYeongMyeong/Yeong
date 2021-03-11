
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

public class AlertingRule {

    private String Id; // Rule id

    private String UserId;

    private String EKID;

    private String Name;

    private String FriendlyDescription;

    /**
     * @see eAlertingSensorTypes for available sensor types
     */
    private int SensorType;

    private String Email;

    /**
     * @see eComparisonOperators for available operators
     */
    private int OperatorType;

    private float Value;

    private String LastUpdated;

    private boolean IsEnabled;

    public AlertingRule(String userId, String EKID) {
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

    public void setFriendlyDescription(String friendlyDescription) {
        this.FriendlyDescription = friendlyDescription;
    }

    public void createFriendlyDescription() {
        setFriendlyDescription(
                getEmail() != null && !getEmail().trim().isEmpty() &&
                        getEKID() != null && !getEKID().trim().isEmpty() ?
                        String.format("If %s is %s %s then send email to %s",
                                eAlertingSensorTypes.AlertingSensorTypeToNameMap.get(getSensorType()),
                                eComparisonOperators.ComparisonTypeToSymbolMap.get(getOperatorType()),
                                getValue(), getEmail())
                        : "");
    }

    public int getSensorType() {
        return SensorType;
    }

    public void setSensorType(int sensorType) {
        this.SensorType = sensorType;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        this.Email = email;
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
    public boolean IsValid() {
        return (getEmail() != null && !getEmail().trim().isEmpty()
                && getUserId() != null && !getUserId().trim().isEmpty()
                && getName() != null && !getName().trim().isEmpty()
                && getEKID() != null && !getEKID().trim().isEmpty());
    }
    //----------------------------------------------------------------------------------------------
}
