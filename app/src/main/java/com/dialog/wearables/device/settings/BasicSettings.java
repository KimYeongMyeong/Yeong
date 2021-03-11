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
import android.util.Log;

import com.dialog.wearables.device.IotSensorsDevice;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BasicSettings extends IotDeviceSettings {

    private static final String[] PREF_KEYS = new String[]{
            "prefSensorCombination",
            "prefEnvironmentalSensorEnabled",
            "prefAccelerometerRange",
            "prefAccelerometerRate",
            "prefGyroscopeRange",
            "prefGyroscopeRate",
            "prefMagnetometerRate",
            "prefEnvironmentalRate",
            "prefSensorFusionEnabled",
            "prefSensorFusionRate",
            "prefSensorFusionRawEnabled",
            "prefCalibrationMode",
            "prefAutoCalibrationMode",
    };

    private static final int LENGTH = 11;
    private byte[] raw = new byte[LENGTH];
    private boolean valid;
    private boolean modified;
    public byte sensorCombination;
    public int sflCombination = -1;
    public int imuCombination = -1;
    public boolean envEnabled;
    public byte accRange;
    public byte accRate;
    public byte gyroRange;
    public byte gyroRate;
    public byte magnetoRate;
    public byte envRate;
    public byte sflRate;
    public boolean sflEnabled;
    public boolean sflRawEnabled;
    public byte calMode;
    public byte autoCalMode;

    public BasicSettings(IotSensorsDevice device) {
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
    }

    private void packSensorCombination() {
        int sensorCombination = (sflEnabled ? sflCombination : imuCombination) | (envEnabled ? 0x08 : 0);
        this.sensorCombination = (byte) sensorCombination;
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
        byte sflRateSetting = buffer.get();
        if (device.type != IotSensorsDevice.TYPE_WEARABLE) {
            sflEnabled = device.sflEnabled;
            sflRate = sflRateSetting;
        } else {
            sflEnabled = sflRateSetting != 0;
            if (sflEnabled)
                sflRate = sflRateSetting;
            if (sflRate == 0)
                sflRate = 5; // 12.5Hz
        }
        sflRawEnabled = buffer.get() != 0;
        calMode = buffer.get();
        autoCalMode = buffer.get();
        unpackSensorCombination();
        valid = true;

        if (processCallback != null)
            processCallback.onProcess();
    }

    @Override
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH);
        packSensorCombination();
        buffer.put(sensorCombination);
        buffer.put(accRange);
        buffer.put(accRate);
        buffer.put(gyroRange);
        buffer.put(gyroRate);
        buffer.put(magnetoRate);
        buffer.put(envRate);
        buffer.put(sflEnabled ? sflRate : (byte) 0);
        buffer.put((byte) (sflRawEnabled ? 1 : 0));
        buffer.put(calMode);
        buffer.put(autoCalMode);

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
        e.putString("prefAccelerometerRange", Integer.toString(accRange & 0xff));
        e.putString("prefAccelerometerRate", Integer.toString(accRate & 0xff));
        e.putString("prefGyroscopeRange", Integer.toString(gyroRange & 0xff));
        e.putString("prefGyroscopeRate", "50");//Integer.toString(gyroRate & 0xff));
//        Log.d("gyroRate", Integer.toString(gyroRate & 0xff));
        e.putString("prefMagnetometerRate", Integer.toString(magnetoRate & 0xff));
        e.putString("prefEnvironmentalRate", Integer.toString(envRate & 0xff));
        e.putBoolean("prefSensorFusionEnabled", sflEnabled);
        e.putString("prefSensorFusionRate", Integer.toString(sflRate & 0xff));
        e.putBoolean("prefSensorFusionRawEnabled", sflRawEnabled);
        e.putString("prefCalibrationMode", Integer.toString(calMode & 0xff));
        e.putString("prefAutoCalibrationMode", Integer.toString(autoCalMode & 0xff));
        e.apply();

//        Log.d("보여줘욧","prefGyroscopeRate: "+ gyroRate);
    }

    @Override
    public void load(SharedPreferences pref) {
        // In case sensor fusion state is changed, use previous state to select the correct variable.
        if (sflEnabled)
            sflCombination = Integer.parseInt(pref.getString("prefSensorCombination", null));
        else
            imuCombination = Integer.parseInt(pref.getString("prefSensorCombination", null));
        envEnabled = pref.getBoolean("prefEnvironmentalSensorEnabled", false);
        accRange = (byte) Integer.parseInt(pref.getString("prefAccelerometerRange", null));
        accRate = (byte) Integer.parseInt(pref.getString("prefAccelerometerRate", null));
        gyroRange = (byte) Integer.parseInt(pref.getString("prefGyroscopeRange", null));
        gyroRate = (byte) Integer.parseInt(pref.getString("prefGyroscopeRate", null));
        magnetoRate = (byte) Integer.parseInt(pref.getString("prefMagnetometerRate", null));
        envRate = (byte) Integer.parseInt(pref.getString("prefEnvironmentalRate", null));
        if (device.type == IotSensorsDevice.TYPE_WEARABLE)
            sflEnabled = pref.getBoolean("prefSensorFusionEnabled", false);
        sflRate = (byte) Integer.parseInt(pref.getString("prefSensorFusionRate", null));
        sflRawEnabled = pref.getBoolean("prefSensorFusionRawEnabled", false);
        calMode = (byte) Integer.parseInt(pref.getString("prefCalibrationMode", null));
        autoCalMode = (byte) Integer.parseInt(pref.getString("prefAutoCalibrationMode", null));
        packSensorCombination();
    }
}
