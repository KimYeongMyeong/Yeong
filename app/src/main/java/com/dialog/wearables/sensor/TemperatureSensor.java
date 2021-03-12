/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor;

public abstract class TemperatureSensor extends IotSensor {

    public static final int CELSIUS = 0;
    public static final int FAHRENHEIT = 1;

    public static float celsiusToFahrenheit(float celsius) {
        return celsius * 1.8f + 32;
    }

    public static float fahrenheitToCelsius(float fahrenheit) {
        return (fahrenheit - 32) / 1.8f;
    }

    protected int logUnit = CELSIUS;
    protected int displayUnit = CELSIUS;
    protected int graphUnit = CELSIUS;

    public int getLogUnit() {
        return logUnit;
    }

    public void setLogUnit(int logUnit) {
        this.logUnit = logUnit;
    }

    public int getDisplayUnit() {
        return displayUnit;
    }

    public void setDisplayUnit(int displayUnit) {
        this.displayUnit = displayUnit;
    }

    public int getGraphUnit() {
        return graphUnit;
    }

    public void setGraphUnit(int graphUnit) {
        this.graphUnit = graphUnit;
    }

    public abstract int getTemperatureUnit();

    protected float temperature;

    public float getTemperature() {
        return temperature;
    }

    public float getTemperature(int unit) {
        return unit == getTemperatureUnit() ? temperature : unit == FAHRENHEIT ? celsiusToFahrenheit(temperature) : fahrenheitToCelsius(temperature);
    }

    public Value getValue(int unit) {
        return unit == getTemperatureUnit() ? value : new Value(unit == FAHRENHEIT ? celsiusToFahrenheit(temperature) : fahrenheitToCelsius(temperature));
    }

    public static final String LOG_TAG = "TMP";
    public static final String LOG_UNIT_CELSIUS = " C";
    public static final String LOG_UNIT_FAHRENHEIT = " F";

    @Override
    public Value getLogValue() {
        return getValue(logUnit);
    }

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    public String getLogValueUnit() {
        return logUnit == CELSIUS ? LOG_UNIT_CELSIUS : LOG_UNIT_FAHRENHEIT;
    }

    @Override
    public float getDisplayValue() {
        return getTemperature(displayUnit);
    }

    @Override
    public Value getGraphValue() {
        Value graphValue = super.getGraphValue(); // for GraphValueProcessor
        return graphUnit == getTemperatureUnit() ? graphValue : new Value(graphUnit == FAHRENHEIT ? celsiusToFahrenheit(graphValue.get()) : fahrenheitToCelsius(graphValue.get()));
    }

    @Override
    public Value getCloudValue() {
        return getValue(CELSIUS);
    }
}
