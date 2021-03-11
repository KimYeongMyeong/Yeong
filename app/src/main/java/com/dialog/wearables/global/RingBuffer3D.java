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

public class RingBuffer3D<T> {

    public RingBuffer<T> X;
    public RingBuffer<T> Y;
    public RingBuffer<T> Z;

    public RingBuffer3D(int capacity) {
        X = new RingBuffer<>(capacity);
        Y = new RingBuffer<>(capacity);
        Z = new RingBuffer<>(capacity);
    }

    public void add(T x, T y, T z) {
        X.add(x);
        Y.add(y);
        Z.add(z);
    }
}
