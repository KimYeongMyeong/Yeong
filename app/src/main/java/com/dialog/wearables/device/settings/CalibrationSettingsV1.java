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
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CalibrationSettingsV1 extends CalibrationSettings {

    private static final String[] PREF_KEYS = new String[] {
            "calApply",
            "calMatrixApply",
            "calUpdate",
            "calMatrixUpdate",
            "calInitialiseStaticCoeffs",
            "cal_reference_magnitude_key",
            "cal_magnitude_range_key",
            "cal_mu_key",
            "calLoadVectorMatrix",
            "calSaveVectorMatrix",
            "calStoreConfigNV",
            "calResetCurrSet",
    };

    private static final int LENGTH = 15;
    private byte[] raw = new byte[LENGTH];
    private boolean valid;
    private boolean modified;
    public byte sensor;
    public short controlFlags;
    public boolean apply;
    public boolean matrixApply;
    public boolean update;
    public boolean matrixUpdate;
    public boolean initFromStatic;
    public int refMag;
    public int magRange;
    public int mu;

    public CalibrationSettingsV1(IotSensorsDevice device) {
        super(device);
    }

    private void unpackControlFlags() {
        apply = (controlFlags & 0x04) != 0;
        matrixApply = (controlFlags & 0x08) != 0;
        update = (controlFlags & 0x10) != 0;
        matrixUpdate = (controlFlags & 0x20) != 0;
        initFromStatic = (controlFlags & 0x40) != 0;
    }

    private void packControlFlags() {
        int controlFlags = this.controlFlags & ~0x7C;
        controlFlags |= (apply ? 0x04 : 0) |
                (matrixApply ? 0x08 : 0) |
                (update ? 0x10 : 0) |
                (matrixUpdate ? 0x20 : 0) |
                (initFromStatic ? 0x40 : 0);
        this.controlFlags = (short) controlFlags;
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

        sensor = buffer.get();
        controlFlags = buffer.getShort();
        refMag = buffer.getShort() & 0xffff;
        magRange = buffer.getShort() & 0xffff;
        mu = buffer.getShort();
        buffer.getShort(); // reserved
        buffer.getShort(); // reserved
        buffer.getShort(); // reserved
        unpackControlFlags();
        valid = true;

        if (processCallback != null)
            processCallback.onProcess();
    }

    @Override
    public byte[] pack() {
        ByteBuffer buffer = ByteBuffer.allocate(LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        packControlFlags();
        buffer.put(sensor);
        buffer.putShort(controlFlags);
        buffer.putShort((short)refMag);
        buffer.putShort((short)magRange);
        buffer.putShort((short)mu);
        buffer.putShort((short)0); // reserved
        buffer.putShort((short)0); // reserved
        buffer.putShort((short)0); // reserved

        modified = !Arrays.equals(raw, buffer.array());
        return buffer.array();
    }

    @Override
    public void save(SharedPreferences pref) {
        SharedPreferences.Editor e = pref.edit();
        e.putBoolean("calApply", apply);
        e.putBoolean("calMatrixApply", matrixApply);
        e.putBoolean("calUpdate", update);
        e.putBoolean("calMatrixUpdate", matrixUpdate);
        e.putBoolean("calInitialiseStaticCoeffs", initFromStatic);
        e.putString("cal_reference_magnitude_key", Integer.toString(refMag));
        e.putString("cal_magnitude_range_key", Integer.toString(magRange));
        e.putString("cal_mu_key", Integer.toString(mu));
        e.apply();
    }

    @Override
    public void load(SharedPreferences pref) {

        apply = pref.getBoolean("calApply", false);
        matrixApply = pref.getBoolean("calMatrixApply", false);
        update = pref.getBoolean("calUpdate", false);
        matrixUpdate = pref.getBoolean("calMatrixUpdate", false);
        initFromStatic = pref.getBoolean("calInitialiseStaticCoeffs", false);

        // In case of parsing error, set invalid values to be caught by range check.
        try {
            refMag = Integer.parseInt(pref.getString("cal_reference_magnitude_key", null));
        } catch (NumberFormatException nfe) {
            refMag = Integer.MIN_VALUE;
        }
        try {
            magRange = Integer.parseInt(pref.getString("cal_magnitude_range_key", null));
        } catch (NumberFormatException nfe) {
            magRange = Integer.MIN_VALUE;
        }
        try {
            mu = Integer.parseInt(pref.getString("cal_mu_key", null));
        } catch (NumberFormatException nfe) {
            mu = Integer.MIN_VALUE;
        }

        packControlFlags();
    }

    @Override
    public List<RangeError> checkRange() {
        List<RangeError> errors = new ArrayList<>();
        if (calMode < UUIDS.CALIBRATION_MODE_CONTINUOUS_AUTO)
            return errors;

        if (refMag < 0 || refMag > 32767)
            errors.add(new RangeError("cal_reference_magnitude_key", 0, 32767));
        if (magRange < 0 || magRange > 32768)
            errors.add(new RangeError("cal_magnitude_range_key", 0, 32768));

        if (calAutoMode != UUIDS.CALIBRATION_AUTO_MODE_SMART)
            return errors;
        if (mu < -32768 || mu > 0)
            errors.add(new RangeError("cal_mu_key", -32768, 0));
        return errors;
   }

    @Override
    public String[] getPrefKeys() {
        return PREF_KEYS;
    }

    @Override
    public List<String> getSettingsForCalibrationMode() {
        ArrayList<String> settings = new ArrayList<>();
        if (calMode == UUIDS.CALIBRATION_MODE_NONE)
            return settings;
        if (calMode >= UUIDS.CALIBRATION_MODE_STATIC) {
            settings.add("calApply");
            settings.add("calMatrixApply");
            settings.add("calLoadVectorMatrix");
            settings.add("calSaveVectorMatrix");
            settings.add("calStoreConfigNV");
            settings.add("calResetCurrSet");
        }
        if (calMode >= UUIDS.CALIBRATION_MODE_CONTINUOUS_AUTO) {
            settings.add("calUpdate");
            settings.add("calMatrixUpdate");
            settings.add("calInitialiseStaticCoeffs");
            settings.add("cal_reference_magnitude_key");
            settings.add("cal_magnitude_range_key");
            if (calAutoMode == UUIDS.CALIBRATION_AUTO_MODE_SMART) {
                settings.add("cal_mu_key");
            }
        }
        return settings;
    }
}
