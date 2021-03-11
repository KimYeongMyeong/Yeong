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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

public class RingBuffer<T> {

    private T[] data;
    private int start;
    private boolean rewind;

    @SuppressWarnings("unchecked")
    public RingBuffer(int capacity) {
        data = (T[]) new Object[capacity];
    }

    public int capacity() {
        return data.length;
    }

    public synchronized int size() {
        return rewind ? data.length : start;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public synchronized T last() {
        if (isEmpty())
            throw new IndexOutOfBoundsException("Buffer is empty");
        return data[start > 0 ? start - 1 : data.length - 1];
    }

    public synchronized void add(T t) {
        data[start++] = t;
        if (start == data.length) {
            rewind = true;
            start = 0;
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized T[] getData(T[] a) {
        T[] out = a == null ? (T[]) new Object[size()] : (T[]) Array.newInstance(a.getClass().getComponentType(), size());
        if (rewind) {
            System.arraycopy(data, start, out, 0, data.length - start);
            System.arraycopy(data, 0, out, data.length - start, start);
        } else {
            System.arraycopy(data, 0, out, 0, start);
        }
        return out;
    }

    public List<T> getList() {
        return Arrays.asList(getData(null));
    }
}
