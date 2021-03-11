/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.global;

import com.dialog.wearables.sensor.IotSensor;

import lecho.lib.hellocharts.model.PointValue;

public class PointValueBuffer extends RingBuffer<PointValue> {

    private int lastIndex = 0;

    public PointValueBuffer(int capacity) {
        super(capacity);
    }

    public synchronized void add(float value) {
        super.add(new PointValue(++lastIndex, value));
    }

    public void add(IotSensor.Value value) {
        add(value.get());
    }

    public int getLastIndex() {
        return lastIndex;
    }
}
