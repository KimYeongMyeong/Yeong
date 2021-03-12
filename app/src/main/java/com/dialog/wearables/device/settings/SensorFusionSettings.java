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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SensorFusionSettings extends IotDeviceSettings {

    private static final String[] PREF_KEYS = new String[] {
            "sfl_beta_a",
            "sfl_beta_m",
    };

    private static final int LENGTH = 8;
    private byte[] raw = new byte[LENGTH];
    private boolean valid;
    private boolean modified;
    public int sflBetaA;
    public int sflBetaM;

    public SensorFusionSettings(IotSensorsDevice device) {
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

        sflBetaA = buffer.getShort() & 0xffff;
        sflBetaM = buffer.getShort() & 0xffff;
        valid = true;

        if (processCallback != null)
            processCallback.onProcess();
    }

    @Override
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH).order(ByteOrder.LITTLE_ENDIAN);

        buffer.putShort((short) sflBetaA);
        buffer.putShort((short) sflBetaM);
        buffer.putInt(0); // reserved

        modified = !Arrays.equals(raw, buffer.array());
        return buffer.array();
    }

    @Override
    public void save(SharedPreferences pref) {
        SharedPreferences.Editor e = pref.edit();
        e.putString("sfl_beta_a", Integer.toString(sflBetaA));
        e.putString("sfl_beta_m", Integer.toString(sflBetaM));
        e.apply();
    }

    @Override
    public void load(SharedPreferences pref) {
        // In case of parsing error, set invalid values to be caught by range check.
        try {
            sflBetaA = Integer.parseInt(pref.getString("sfl_beta_a", null));
        } catch (NumberFormatException nfe) {
            sflBetaA = Integer.MIN_VALUE;
        }
        try {
            sflBetaM = Integer.parseInt(pref.getString("sfl_beta_m", null));
        } catch (NumberFormatException nfe) {
            sflBetaM = Integer.MIN_VALUE;
        }
    }

    @Override
    public List<RangeError> checkRange() {
        List<RangeError> errors = new ArrayList<>();
        if (sflBetaA < 0 || sflBetaA > 32768)
            errors.add(new RangeError("sfl_beta_a", 0, 32768));
        if (sflBetaM < 0 || sflBetaM > 32768)
            errors.add(new RangeError("sfl_beta_a", 0, 32768));
        return errors;
   }

    @Override
    public String[] getPrefKeys() {
        return PREF_KEYS;
    }
}
