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

import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.device.IotSensorsDevice;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CalibrationModesSettings extends IotDeviceSettings {

    private static final String[] PREF_KEYS = new String[] {
            "prefAccelerometerCalibrationMode",
            "prefGyroscopeCalibrationMode",
            "prefMagnetometerCalibrationMode",
    };

    private static final int LENGTH = 6;
    private byte[] raw = new byte[LENGTH];
    private boolean valid;
    private boolean modified;
    public byte accCalMode;
    public byte accAutoCalMode;
    public byte gyroCalMode;
    public byte gyroAutoCalMode;
    public byte magnetoCalMode;
    public byte magnetoAutoCalMode;
    public int accMode;
    public int gyroMode;
    public int magnetoMode;

    public CalibrationModesSettings(IotSensorsDevice device) {
        super(device);
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

    private void packModes() {
        accCalMode = (byte) (accMode / 2);
        if (accCalMode > UUIDS.CALIBRATION_MODE_STATIC)
            accAutoCalMode = (byte) (accMode % 2);
        gyroCalMode = (byte) (gyroMode / 2);
        if (gyroCalMode > UUIDS.CALIBRATION_MODE_STATIC)
            gyroAutoCalMode = (byte) (gyroMode % 2);
        magnetoCalMode = (byte) (magnetoMode / 2);
        if (magnetoCalMode > UUIDS.CALIBRATION_MODE_STATIC)
            magnetoAutoCalMode = (byte) (magnetoMode % 2);
    }

    private void unpackModes() {
        accMode = 2 * accCalMode;
        if (accCalMode > UUIDS.CALIBRATION_MODE_STATIC)
            accMode += accAutoCalMode;
        gyroMode = 2 * gyroCalMode;
        if (gyroCalMode > UUIDS.CALIBRATION_MODE_STATIC)
            gyroMode += gyroAutoCalMode;
        magnetoMode = 2 * magnetoCalMode;
        if (magnetoCalMode > UUIDS.CALIBRATION_MODE_STATIC)
            magnetoMode += magnetoAutoCalMode;
    }

    @Override
    public void process(byte[] data, int offset) {
        System.arraycopy(data, offset, raw, 0, LENGTH);
        ByteBuffer buffer = ByteBuffer.wrap(raw);

        accCalMode = buffer.get();
        gyroCalMode = buffer.get();
        magnetoCalMode = buffer.get();
        accAutoCalMode = buffer.get();
        gyroAutoCalMode = buffer.get();
        magnetoAutoCalMode = buffer.get();
        unpackModes();
        valid = true;

        if (processCallback != null)
            processCallback.onProcess();
    }

    @Override
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH);
        packModes();
        buffer.put(accCalMode);
        buffer.put(gyroCalMode);
        buffer.put(magnetoCalMode);
        buffer.put(accAutoCalMode);
        buffer.put(gyroAutoCalMode);
        buffer.put(magnetoAutoCalMode);

        modified = !Arrays.equals(raw, buffer.array());
        return buffer.array();
    }

    @Override
    public void save(SharedPreferences pref) {
        SharedPreferences.Editor e = pref.edit();
        e.putString("prefAccelerometerCalibrationMode", Integer.toString(accMode));
        e.putString("prefGyroscopeCalibrationMode", Integer.toString(gyroMode));
        e.putString("prefMagnetometerCalibrationMode", Integer.toString(magnetoMode));
        e.apply();
    }

    @Override
    public void load(SharedPreferences pref) {
        accMode = Integer.parseInt(pref.getString("prefAccelerometerCalibrationMode", null));
        gyroMode = Integer.parseInt(pref.getString("prefGyroscopeCalibrationMode", null));
        magnetoMode = Integer.parseInt(pref.getString("prefMagnetometerCalibrationMode", null));
        packModes();
    }

    @Override
    public String[] getPrefKeys() {
        return PREF_KEYS;
    }
}
