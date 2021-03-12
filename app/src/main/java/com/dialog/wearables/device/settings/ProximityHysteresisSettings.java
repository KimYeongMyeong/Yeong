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
import java.nio.ByteOrder;
import java.util.Arrays;

public class ProximityHysteresisSettings extends IotDeviceSettings {

    private static final String[] PREF_KEYS = new String[] {
            "prefProximityHysteresis",
    };

    private static final int LENGTH = 4;
    private byte[] raw = new byte[LENGTH];
    private boolean valid;
    private boolean modified;
    public int lowLimit;
    public int highLimit;

    public ProximityHysteresisSettings(IotSensorsDevice device) {
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

    @Override
    public void process(byte[] data, int offset) {
        System.arraycopy(data, offset, raw, 0, LENGTH);
        ByteBuffer buffer = ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN);

        lowLimit = buffer.getShort() & 0xffff;
        highLimit = buffer.getShort() & 0xffff;
        valid = true;

        if (processCallback != null)
            processCallback.onProcess();
    }

    @Override
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort((short) lowLimit);
        buffer.putShort((short) highLimit);

        modified = !Arrays.equals(raw, buffer.array());
        return buffer.array();
    }

    @Override
    public void save(SharedPreferences pref) {
        SharedPreferences.Editor e = pref.edit();
        e.putString("prefProximityHysteresis", RangeSeekBarPreference.packMinMax(lowLimit, highLimit));
        e.apply();
    }

    @Override
    public void load(SharedPreferences pref) {
        int[] minMax = RangeSeekBarPreference.unpackMinMax(pref.getString("prefProximityHysteresis", null));
        lowLimit = minMax[0];
        highLimit = minMax[1];
    }

    @Override
    public String[] getPrefKeys() {
        return PREF_KEYS;
    }
}
