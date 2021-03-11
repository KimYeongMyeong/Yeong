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

public class CalibrationSettingsV2 extends CalibrationSettings {

    private static final String[] PREF_KEYS = new String[] {
            "calApply",
            "calMatrixApply",
            "calUpdate",
            "calMatrixUpdate",
            "calInitialiseStaticCoeffs",
            "cal_reference_magnitude_key",
            "cal_magnitude_range_key",
            "cal_magnitude_alpha_key",
            "mag_delta_thresh",
            "mu_offset",
            "mu_matrix",
            "err_alpha",
            "err_thresh",
            "calLoadVectorMatrix",
            "calSaveVectorMatrix",
            "calStoreConfigNV",
            "calResetCurrSet",
    };

    private static final int LENGTH = 19;
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
    public int magAlpha;
    public int magDeltaThresh;
    public int muOffset;
    public int muMatrix;
    public int errAlpha;
    public int errThresh;

    public CalibrationSettingsV2(IotSensorsDevice device) {
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
        magAlpha = buffer.getShort() & 0xffff;
        magDeltaThresh = buffer.getShort() & 0xffff;
        muOffset = buffer.getShort() & 0xffff;
        muMatrix = buffer.getShort() & 0xffff;
        errAlpha = buffer.getShort() & 0xffff;
        errThresh = buffer.getShort() & 0xffff;
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
        buffer.putShort((short)magAlpha);
        buffer.putShort((short)magDeltaThresh);
        buffer.putShort((short)muOffset);
        buffer.putShort((short)muMatrix);
        buffer.putShort((short)errAlpha);
        buffer.putShort((short)errThresh);

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
        e.putString("cal_magnitude_alpha_key", Integer.toString(magAlpha));
        e.putString("mag_delta_thresh", Integer.toString(magDeltaThresh));
        e.putString("mu_offset", Integer.toString(muOffset));
        e.putString("mu_matrix", Integer.toString(muMatrix));
        e.putString("err_alpha", Integer.toString(errAlpha));
        e.putString("err_thresh", Integer.toString(errThresh));
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
            magAlpha = Integer.parseInt(pref.getString("cal_magnitude_alpha_key", null));
        } catch (NumberFormatException nfe) {
            magAlpha = Integer.MIN_VALUE;
        }
        try {
            magDeltaThresh = Integer.parseInt(pref.getString("mag_delta_thresh", null));
        } catch (NumberFormatException nfe) {
            magDeltaThresh = Integer.MIN_VALUE;
        }
        try {
            muOffset = Integer.parseInt(pref.getString("mu_offset", null));
        } catch (NumberFormatException nfe) {
            muOffset = Integer.MIN_VALUE;
        }
        try {
            muMatrix = Integer.parseInt(pref.getString("mu_matrix", null));
        } catch (NumberFormatException nfe) {
            muMatrix = Integer.MIN_VALUE;
        }
        try {
            errAlpha = Integer.parseInt(pref.getString("err_alpha", null));
        } catch (NumberFormatException nfe) {
            errAlpha = Integer.MIN_VALUE;
        }
        try {
            errThresh = Integer.parseInt(pref.getString("err_thresh", null));
        } catch (NumberFormatException nfe) {
            errThresh = Integer.MIN_VALUE;
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
        if (magAlpha < 0 || magAlpha > 32768)
            errors.add(new RangeError("cal_magnitude_alpha_key", 0, 32768));
        if (magDeltaThresh < 0 || magDeltaThresh > 32768)
            errors.add(new RangeError("mag_delta_thresh", 0, 32768));

        if (calAutoMode != UUIDS.CALIBRATION_AUTO_MODE_SMART)
            return errors;
        if (muOffset < 0 || muOffset > 32768)
            errors.add(new RangeError("mu_offset", 0, 32768));
        if (muMatrix < 0 || muMatrix > 32768)
            errors.add(new RangeError("mu_matrix", 0, 32768));
        if (errAlpha < 0 || errAlpha > 32768)
            errors.add(new RangeError("err_alpha", 0, 32768));
        if (errThresh < 0 || errThresh > 32768)
            errors.add(new RangeError("err_thresh", 0, 32768));
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
            settings.add("cal_magnitude_alpha_key");
            settings.add("mag_delta_thresh");
            if (calAutoMode == UUIDS.CALIBRATION_AUTO_MODE_SMART) {
                settings.add("mu_offset");
                settings.add("mu_matrix");
                settings.add("err_alpha");
                settings.add("err_thresh");
            }
        }
        return settings;
    }
}
