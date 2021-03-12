/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.bluetooth.async;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.device.IotSensorsDevice;

public class Callback extends android.bluetooth.BluetoothGattCallback {
    public static String TAG = "Callback";

    private IotSensorsDevice device;

    public Callback(IotSensorsDevice device) {
        this.device = device;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            // Workaround for connection notification after disconnection issue.
            if (device.state != IotSensorsDevice.CONNECTING)
                return;
            device.state = IotSensorsDevice.CONNECTED;
            gatt.discoverServices();
        } else {
            device.state = IotSensorsDevice.DISCONNECTED;
        }
        Intent intent = new Intent();
        intent.setAction(BroadcastUpdate.CONNECTION_STATE_UPDATE);
        intent.putExtra("state", newState);
        LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        device.manager.processServices();
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        device.manager.processCharacteristicRead(characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        device.manager.processCharacteristicWrite(characteristic, status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        device.manager.processDescriptorWrite(descriptor, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        device.manager.processCharacteristicChanged(characteristic);
    }
}
