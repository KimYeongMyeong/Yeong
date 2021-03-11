/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2017 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.bluetooth;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Bluetooth GATT operation queue type.
 */
public class BluetoothCommand {

    public static final int TYPE_READ = 0;
    public static final int TYPE_WRITE = 1;
    public static final int TYPE_WRITE_DESCRIPTOR = 2;

    protected BluetoothGattCharacteristic characteristic;
    protected BluetoothGattDescriptor descriptor;
    protected int type; // 0 = read, 1 = write, 2 = descriptor
    protected byte[] value;

    public BluetoothCommand(int type, BluetoothGattCharacteristic characteristic) {
        this.type = type;
        this.characteristic = characteristic;
        if (this.type == TYPE_WRITE)
            this.value = characteristic.getValue().clone();
    }

    public BluetoothCommand(int type, BluetoothGattDescriptor descriptor) {
        this.type = type;
        this.descriptor = descriptor;
        if (this.type == TYPE_WRITE_DESCRIPTOR)
            this.value = descriptor.getValue().clone();
    }

    public int getType() {
        return this.type;
    }

    public BluetoothGattCharacteristic getCharacteristic() {
        return this.characteristic;
    }

    public BluetoothGattDescriptor getDescriptor() {
        return this.descriptor;
    }

    public byte[] getValue() {
        return this.value;
    }
}
