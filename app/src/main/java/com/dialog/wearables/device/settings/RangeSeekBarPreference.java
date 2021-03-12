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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.dialog.wearables.R;

public class RangeSeekBarPreference extends DialogPreference {
    private static final String TAG = "RangeSeekBarPreference";

    private CrystalRangeSeekbar seekbar;
    private TextView minLabel, maxLabel;
    private String defaultValue;
    private String value;
    private int min, max, minOrg, maxOrg;

    private void init(Context context, AttributeSet attrs) {
        setDialogLayoutResource(R.layout.range_seekbar_preference_dialog);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RangeSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public RangeSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public RangeSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RangeSeekBarPreference(Context context) {
        this(context, null);
    }

    public static int[] unpackMinMax(String value) {
        String[] values = value.split("_");
        return new int[] { Integer.parseInt(values[0]), Integer.parseInt(values[1]) };
    }

    public static String packMinMax(int min, int max) {
        return min + "_" + max;
    }

    public void setValue(String value) {
        if (value == null)
            return;
        int[] minMax = unpackMinMax(value);
        min = minOrg = minMax[0];
        max = maxOrg = minMax[1];
        if (min > max)
            max = maxOrg = min;
        if (seekbar != null) {
            seekbar.setMinStartValue(min);
            seekbar.setMaxStartValue(max);
            seekbar.apply();
        }
        setSummary(min + ".." + max);
        if (!value.equals(this.value)) {
            this.value = value;
            persistString(value);
            notifyChanged();
        }
    }

    private void setValue(int min, int max) {
        setValue(packMinMax(min, max));
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        ((TextView) view.findViewById(R.id.dialog_message)).setText(getDialogMessage());
        seekbar = (CrystalRangeSeekbar) view.findViewById(R.id.range_seekbar);
        minLabel = (TextView) view.findViewById(R.id.range_min);
        maxLabel = (TextView) view.findViewById(R.id.range_max);

        if (defaultValue != null) {
            int[] minMax = unpackMinMax(defaultValue);
            seekbar.setMinValue(minMax[0] - 0.001f);
            seekbar.setMaxValue(minMax[1] + 0.001f);
        }
        seekbar.setMinStartValue(min);
        seekbar.setMaxStartValue(max);
        seekbar.apply();

        seekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                minLabel.setText(minValue.toString());
                maxLabel.setText(maxValue.toString());
            }
        });

        seekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                min = minValue.intValue();
                max = maxValue.intValue();
            }
        });
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String value = packMinMax(min, max);
            if(callChangeListener(value))
                setValue(value);
        } else {
            min = minOrg;
            max = maxOrg;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        defaultValue = a.getString(index);
        return defaultValue;
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setValue(restorePersistedValue ? getPersistedString(value) : (String) defaultValue);
    }
}
