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

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;

public class PointValueBuffer3D extends RingBuffer3D<PointValue> {

    private int lastIndex = 0;

    public PointValueBuffer3D(int capacity) {
        super(capacity);
    }

    public synchronized void add(float x, float y, float z) {
        ++lastIndex;
        super.add(new PointValue(lastIndex, x), new PointValue(lastIndex, y), new PointValue(lastIndex, z));
    }

    public void add(IotSensor.Value value) {
        add(value.getX(), value.getY(), value.getZ());
    }

    public int getLastIndex() {
        return lastIndex;
    }

    public synchronized int getList(List<List<PointValue>> list) {
        list.add(X.getList());
        list.add(Y.getList());
        list.add(Z.getList());
        return lastIndex;
    }

    public interface DataProcessor {
        int X = 0;
        int Y = 1;
        int Z = 2;
        PointValue process(int dim, PointValue p);
    }

    public synchronized int getProcessedList(List<List<PointValue>> list, DataProcessor d) {
        List<PointValue> x = new ArrayList<>(X.size());
        List<PointValue> y = new ArrayList<>(Y.size());
        List<PointValue> z = new ArrayList<>(Z.size());
        for (PointValue p : X.getList())
            x.add(d.process(DataProcessor.X, p));
        for (PointValue p : Y.getList())
            y.add(d.process(DataProcessor.Y, p));
        for (PointValue p : Z.getList())
            z.add(d.process(DataProcessor.Z, p));
        list.add(x);
        list.add(y);
        list.add(z);
        return lastIndex;
    }
}
