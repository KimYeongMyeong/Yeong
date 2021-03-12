/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.device.settings;

import android.content.SharedPreferences;

import com.dialog.wearables.device.IotSensorsDevice;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BasicSettingsIotPlus extends IotDeviceSettings {

    private static final String[] PREF_KEYS = new String[]{
            "prefSensorCombination",
            "prefEnvironmentalSensorEnabled",
            "prefGasSensorEnabled",
            "prefAmbientLightSensorEnabled",
            "prefProximitySensorEnabled",
            "prefAccelerometerRange",
            "prefAccelerometerRate",
            "prefGyroscopeRange",
            "prefGyroscopeRate",
            "prefMagnetometerRate",
            "prefEnvironmentalRate",
            "prefOperationMode",
            "prefSensorFusionEnabled",
            "prefSensorFusionRate",
            "prefSensorFusionRawEnabled",
            "prefGasRate",
            "prefProximityMode",
            "prefAmbientLightMode",
            "prefProximityAmbientLightRate",
    };

    private static final int LENGTH = 14;
    private byte[] raw = new byte[LENGTH];
    private boolean valid;
    private boolean modified;
    public byte sensorCombination;
    public int sflCombination = -1;
    public int imuCombination = -1;
    public boolean envEnabled;
    public boolean gasEnabled;
    public boolean amblEnabled;
    public boolean proxEnabled;
    public byte accRange;
    public byte accRate;
    public byte gyroRange;
    public byte gyroRate;
    public byte magnetoRate;
    public byte envRate;
    public byte sflRate;
    public boolean sflEnabled;
    public byte sflMode;
    public boolean sflRawEnabled;
    public byte rawDataType;
    public int operationMode;
    public byte gasRate;
    public byte proxAmblMode;
    public int proxMode;
    public int amblMode;
    public byte proxAmblRate;

    public BasicSettingsIotPlus(IotSensorsDevice device) {
        super(device);
    }

    private void unpackSensorCombination() {
        if (sflEnabled)
            sflCombination = sensorCombination & 0x07;
        else
            imuCombination = sensorCombination & 0x07;
        if (sflCombination == -1)
            sflCombination = 7; // all
        if (imuCombination == -1)
            imuCombination = sflCombination;
        envEnabled = (sensorCombination & 0x08) != 0;
        gasEnabled = (sensorCombination & 0x10) != 0;
        proxEnabled = (sensorCombination & 0x20) != 0;
        amblEnabled = (sensorCombination & 0x40) != 0;
    }

    private void packSensorCombination() {
        int sensorCombination = (sflEnabled ? sflCombination : imuCombination)
                | (envEnabled ? 0x08 : 0)
                | (gasEnabled ? 0x10 : 0)
                | (proxEnabled ? 0x20 : 0)
                | (amblEnabled ? 0x40 : 0);
        this.sensorCombination = (byte) sensorCombination;
    }

    private void unpackProxAmblMode() {
        proxMode = (proxAmblMode >> 2) & 0x03;
        amblMode = proxAmblMode & 0x03;
    }

    private void packProxAmblMode() {
        int proxAmblMode = (proxMode << 2) | amblMode;
        this.proxAmblMode = (byte) proxAmblMode;
    }

    private void unpackOperationMode() {
        operationMode = sflMode * 10 + rawDataType;
    }

    private void packOperationMode() {
        sflMode = (byte) (operationMode / 10);
        sflEnabled = sflMode != 0;
        rawDataType = (byte) (operationMode % 10);
    }

    @Override
    public int length() {
        return LENGTH;
    }

    @Override
    public boolean valid() {
        return valid;
    }

    @Override
    public boolean modified() {
        return modified;
    }

    @Override
    public void process(byte[] data, int offset) {
        System.arraycopy(data, offset, raw, 0, LENGTH);
        ByteBuffer buffer = ByteBuffer.wrap(raw);

        sensorCombination = buffer.get();
        accRange = buffer.get();
        accRate = buffer.get();
        gyroRange = buffer.get();
        gyroRate = buffer.get();
        magnetoRate = buffer.get();
        envRate = buffer.get();
        if (!device.getFeatures().hasIntegrationEngine()) {
            byte sflRateSetting = buffer.get();
            sflEnabled = sflRateSetting != 0;
            if (sflEnabled)
                sflRate = sflRateSetting;
            if (sflRate == 0)
                sflRate = 10;
            sflMode = buffer.get();
            sflRawEnabled = buffer.get() != 0;
        } else {
            sflRate = buffer.get();
            sflMode = buffer.get();
            sflEnabled = sflMode != 0;
            rawDataType = buffer.get();
        }
        buffer.get(); // reserved
        gasRate = buffer.get();
        proxAmblMode = buffer.get();
        proxAmblRate = buffer.get();
        unpackSensorCombination();
        unpackProxAmblMode();
        if (device.getFeatures().hasIntegrationEngine())
            unpackOperationMode();
        valid = true;

        if (processCallback != null)
            processCallback.onProcess();
    }

    @Override
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH);
        packSensorCombination();
        packProxAmblMode();
        boolean integrationEngine = device.getFeatures().hasIntegrationEngine();
        if (integrationEngine)
            packOperationMode();
        buffer.put(sensorCombination);
        buffer.put(accRange);
        buffer.put(sflEnabled || integrationEngine && rawDataType == 2 ? accRate : (byte) Math.max(accRate, 8)); // max 100Hz if sfl disabled
        buffer.put(gyroRange);
        buffer.put(sflEnabled || integrationEngine && rawDataType == 2 ? gyroRate : (byte) Math.max(gyroRate, 8)); // max 100Hz if sfl disabled
        buffer.put(magnetoRate);
        buffer.put(!device.getFeatures().hasGasSensor() || !gasEnabled ? envRate : (byte) 6); // force 0.33Hz if gas enabled
        buffer.put(sflEnabled || integrationEngine ? sflRate : (byte) 0);
        buffer.put(sflMode);
        buffer.put((byte) (integrationEngine ? rawDataType : sflRawEnabled ? 1 : 0));
        buffer.put((byte) 0); // reserved
        buffer.put(gasRate);
        buffer.put(proxAmblMode);
        buffer.put(proxAmblRate);

        modified = !Arrays.equals(raw, buffer.array());
        return buffer.array();
    }

    @Override
    public String[] getPrefKeys() {
        return PREF_KEYS;
    }

    @Override
    public void save(SharedPreferences pref) {
        SharedPreferences.Editor e = pref.edit();
        e.putString("prefSensorCombination", Integer.toString(sflEnabled ? sflCombination : imuCombination));
        e.putBoolean("prefEnvironmentalSensorEnabled", envEnabled);
        e.putBoolean("prefGasSensorEnabled", gasEnabled);
        e.putBoolean("prefAmbientLightSensorEnabled", amblEnabled);
        e.putBoolean("prefProximitySensorEnabled", proxEnabled);
        e.putString("prefAccelerometerRange", Integer.toString(accRange & 0xff));
        e.putString("prefAccelerometerRate", Integer.toString(accRate & 0xff));
        e.putString("prefGyroscopeRange", Integer.toString(gyroRange & 0xff));
        e.putString("prefGyroscopeRate", Integer.toString(gyroRate & 0xff));
        e.putString("prefMagnetometerRate", Integer.toString(magnetoRate & 0xff));
        e.putString("prefEnvironmentalRate", Integer.toString(envRate & 0xff));
        e.putString("prefOperationMode", Integer.toString(operationMode));
        e.putBoolean("prefSensorFusionEnabled", sflEnabled);
        e.putString("prefSensorFusionRate", Integer.toString(sflRate & 0xff));
        e.putBoolean("prefSensorFusionRawEnabled", sflRawEnabled);
        e.putString("prefGasRate", Integer.toString(gasRate & 0xff));
        e.putString("prefProximityMode", Integer.toString(proxMode & 0xff));
        e.putString("prefAmbientLightMode", Integer.toString(amblMode & 0xff));
        e.putString("prefProximityAmbientLightRate", Integer.toString(proxAmblRate & 0xff));
        e.apply();
    }

    @Override
    public void load(SharedPreferences pref) {
        // In case sensor fusion state is changed, use previous state to select the correct variable.
        if (sflEnabled)
            sflCombination = Integer.parseInt(pref.getString("prefSensorCombination", null));
        else
            imuCombination = Integer.parseInt(pref.getString("prefSensorCombination", null));
        boolean prev = envEnabled;
        envEnabled = pref.getBoolean("prefEnvironmentalSensorEnabled", false);
        if (prev != envEnabled && !envEnabled) {
            gasEnabled = false; // Disable gas if environmental changed to disabled.
        } else {
            prev = gasEnabled;
            gasEnabled = pref.getBoolean("prefGasSensorEnabled", false);
            if (prev != gasEnabled && gasEnabled)
                envEnabled = true; // Enable environmental if gas changed to enabled.
        }
        amblEnabled = pref.getBoolean("prefAmbientLightSensorEnabled", false);
        proxEnabled = pref.getBoolean("prefProximitySensorEnabled", false);
        accRange = (byte) Integer.parseInt(pref.getString("prefAccelerometerRange", null));
        accRate = (byte) Integer.parseInt(pref.getString("prefAccelerometerRate", null));
        gyroRange = (byte) Integer.parseInt(pref.getString("prefGyroscopeRange", null));
        gyroRate = (byte) Integer.parseInt(pref.getString("prefGyroscopeRate", null));
        magnetoRate = (byte) Integer.parseInt(pref.getString("prefMagnetometerRate", null));
        envRate = (byte) Integer.parseInt(pref.getString("prefEnvironmentalRate", null));
        operationMode = Integer.parseInt(pref.getString("prefOperationMode", null));
        sflEnabled = pref.getBoolean("prefSensorFusionEnabled", false);
        sflRate = (byte) Integer.parseInt(pref.getString("prefSensorFusionRate", null));
        sflRawEnabled = pref.getBoolean("prefSensorFusionRawEnabled", false);
        gasRate = (byte) Integer.parseInt(pref.getString("prefGasRate", null));
        proxMode = (byte) Integer.parseInt(pref.getString("prefProximityMode", null));
        amblMode = (byte) Integer.parseInt(pref.getString("prefAmbientLightMode", null));
        proxAmblRate = (byte) Integer.parseInt(pref.getString("prefProximityAmbientLightRate", null));
        packSensorCombination();
        packProxAmblMode();
        if (device.getFeatures().hasIntegrationEngine())
            packOperationMode();
    }
}
