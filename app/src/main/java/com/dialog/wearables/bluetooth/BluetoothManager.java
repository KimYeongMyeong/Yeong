/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.bluetooth;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.defines.StatusUpdates;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.IotSensorsLogger;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Main bluetooth class
 */
public class BluetoothManager {
    private static final String TAG = "BluetoothManager";

    private static final int COMMAND_QUEUE_TIMEOUT = 5000;
    private IotSensorsDevice device;
    private IotSensorsApplication application;
    private LinkedList<BluetoothCommand> mCommandQueue = new LinkedList<>();
    private boolean mWaitingCommandResponse;
    private Handler mHandler;

    private Runnable mQueueTimeout = new Runnable() {
        @Override
        public void run() {
            if (mCommandQueue.isEmpty())
                return;
            Log.e(TAG, "Queue item timed out! Remaining items: " + mCommandQueue.size());
            dequeCommand();
        }
    };

    public BluetoothManager(IotSensorsDevice device) {
        this.device = device;
        application = IotSensorsApplication.getApplication();
        mHandler = new Handler();
    }

    synchronized public void clearCommandQueue() {
        mHandler.removeCallbacks(mQueueTimeout);
        mCommandQueue.clear();
    }

    /**
     * Write the descriptor containing the disable characteristic value for a specific UUID
     *
     * @param uuid UUID to disable notification off
     */
    public void disableNotification(UUID uuid) {
        Log.d(TAG, "disableNotification()");
        if (device.gatt == null) {
            Log.e(TAG, "BluetoothGatt is null");
            return;
        }
        BluetoothGattService service = device.gatt.getService(UUIDS.WEARABLES_SERVICE_UUID);
        if (service == null) {
            Log.e(TAG, "BluetoothService is null");
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
        if (characteristic == null) {
            Log.e(TAG, "Characteristic not found, UUID: " + uuid.toString());
            return;
        }
        device.gatt.setCharacteristicNotification(characteristic, false);
        BluetoothGattDescriptor desc = characteristic.getDescriptor(UUIDS.CLIENT_CONFIG_DESCRIPTOR);
        if (desc == null) {
            Log.d(TAG, "Descriptor not found for characteristic: " + characteristic.getUuid().toString());
            return;
        }
        desc.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        device.gatt.writeDescriptor(desc);
    }

    synchronized private void dequeCommand() {
        mWaitingCommandResponse = false;
        mHandler.removeCallbacks(mQueueTimeout);
        if (!mCommandQueue.isEmpty()) {
            BluetoothCommand command = mCommandQueue.poll();
            mHandler.postDelayed(mQueueTimeout, COMMAND_QUEUE_TIMEOUT);

            Log.d(TAG, "Process a command");
            switch (command.getType()) {
                case BluetoothCommand.TYPE_WRITE:
                    BluetoothGattCharacteristic characteristic = command.getCharacteristic();
                    characteristic.setValue(command.getValue());
                    sendWriteCommand(characteristic);
                    break;
                case BluetoothCommand.TYPE_READ:
                    sendReadCommand(command.getCharacteristic());
                    break;
                case BluetoothCommand.TYPE_WRITE_DESCRIPTOR:
                    BluetoothGattDescriptor descriptor = command.getDescriptor();
                    descriptor.setValue(command.getValue());
                    sendWriteDescriptorCommand(descriptor);
                    break;
            }
        } else {
            Log.d(TAG, "No more commands left.");
        }
    }

    synchronized private void queueWriteCommand(BluetoothGattCharacteristic characteristic) {
        BluetoothCommand command = new BluetoothCommand(BluetoothCommand.TYPE_WRITE, characteristic);
        mCommandQueue.add(command);
        mHandler.postDelayed(mQueueTimeout, COMMAND_QUEUE_TIMEOUT);
        if (!mWaitingCommandResponse) {
            Log.d(TAG, "Not waiting for commands, executing directly");
            dequeCommand();
        } else {
            Log.d(TAG, "Commands still running, waiting... " + mCommandQueue.size());
        }
    }

    synchronized private void queueReadCommand(BluetoothGattCharacteristic characteristic) {
        BluetoothCommand command = new BluetoothCommand(BluetoothCommand.TYPE_READ, characteristic);
        mCommandQueue.add(command);
        mHandler.postDelayed(mQueueTimeout, COMMAND_QUEUE_TIMEOUT);
        if (!mWaitingCommandResponse) {
            Log.d(TAG, "Not waiting for commands, executing directly");
            dequeCommand();
        } else {
            Log.d(TAG, "Commands still running, waiting... " + mCommandQueue.size());
        }
    }

    synchronized private void queueDescriptorEnNotifyCommand(BluetoothGattCharacteristic characteristic) {
        device.gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor desc = characteristic.getDescriptor(UUIDS.CLIENT_CONFIG_DESCRIPTOR);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        BluetoothCommand command = new BluetoothCommand(BluetoothCommand.TYPE_WRITE_DESCRIPTOR, desc);
        mCommandQueue.add(command);
        mHandler.postDelayed(mQueueTimeout, COMMAND_QUEUE_TIMEOUT);
        if (!mWaitingCommandResponse) {
            Log.d(TAG, "Not waiting for commands, executing directly");
            dequeCommand();
        } else {
            Log.d(TAG, "Commands still running, waiting... " + mCommandQueue.size());
        }
    }

    /**
     * @param serviceUUID        Service UUID
     * @param characteristicUUID Characteristic UUID
     * @param value              Byte array with values
     * @param format             Format of value
     */
    synchronized public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, int value, int format) {
        Log.d(TAG, "writeCharacteristic()");
        if (device.gatt != null) {
            BluetoothGattService service = device.gatt.getService(serviceUUID);
            if (service == null) {
                Log.e(TAG, "Service is null");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic == null) {
                Log.e(TAG, "Characteristic is null");
                return;
            }
            characteristic.setValue((byte[]) null); // reset value
            characteristic.setValue(value, format, 0);
            queueWriteCommand(characteristic);
        } else {
            Log.e(TAG, "BluetoothGatt is null");
        }
    }

    /**
     * @param serviceUUID        Service UUID
     * @param characteristicUUID Characteristic UUID
     * @param value              Byte array with values
     */
    synchronized public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value) {
        Log.d(TAG, "writeCharacteristic()");
        if (device.gatt != null) {
            BluetoothGattService service = device.gatt.getService(serviceUUID);
            if (service == null) {
                Log.e(TAG, "Service is null");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic == null) {
                Log.e(TAG, "Characteristic is null");
                return;
            }
            characteristic.setValue(value);
            queueWriteCommand(characteristic);
        } else {
            Log.e(TAG, "BluetoothGatt is null");
        }
    }

    public void readCharacteristic(UUID serviceUUID, UUID characteristicUUID) {
        Log.d(TAG, "readCharacteristic()");
        if (device.gatt != null) {
            BluetoothGattService service = device.gatt.getService(serviceUUID);
            if (service == null) {
                Log.e(TAG, "Service is null");
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
            if (characteristic == null) {
                Log.e(TAG, "Characteristic is null");
                return;
            }
            queueReadCommand(characteristic);
        } else {
            Log.e(TAG, "BluetoothGatt is null");
        }
    }

    private void sendWriteCommand(BluetoothGattCharacteristic characteristic) {
        application.logger.debug("\tSEND\t" + characteristic.getUuid() + "\t" + IotSensorsLogger.getLogStringFromBytes(characteristic.getValue()));
        if (device.gatt.writeCharacteristic(characteristic)) {
            mWaitingCommandResponse = true;
        } else {
            Log.e(TAG, "Error writing characteristic:" + characteristic.getUuid());
        }
    }

    private void sendReadCommand(BluetoothGattCharacteristic command) {
        if (device.gatt.readCharacteristic(command)) {
            mWaitingCommandResponse = true;
        } else {
            Log.e(TAG, "Error reading characteristic:" + command.getUuid());
        }
    }

    private void sendWriteDescriptorCommand(BluetoothGattDescriptor descriptor) {
        if (device.gatt.writeDescriptor(descriptor)) {
            mWaitingCommandResponse = true;
        } else {
            Log.e(TAG, "Error writing descriptor:" + descriptor.getUuid() + " of " + descriptor.getCharacteristic().getUuid());
        }
    }


    public void processServices() {
        Log.d(TAG, "processServices()");
        BluetoothGattService gattService = device.gatt.getService(UUIDS.WEARABLES_SERVICE_UUID);
        if (gattService != null) {
            UUID[] enableNotifications = {
                    UUIDS.WEARABLES_CHARACTERISTIC_CONTROL_NOTIFY,
                    UUIDS.DWP_MULTI_SENSOR,
                    UUIDS.DWP_SENSOR_FUSION,
                    UUIDS.DWP_TEMPERATURE,
                    UUIDS.DWP_HUMIDITY,
                    UUIDS.DWP_PRESSURE,
                    UUIDS.DWP_MAGNETOMETER,
                    UUIDS.DWP_GYROSCOPE,
                    UUIDS.DWP_ACCELEROMETER,
            };
            for (UUID uuid : enableNotifications) {
                BluetoothGattCharacteristic characteristic = gattService.getCharacteristic(uuid);
                if (characteristic != null)
                    queueDescriptorEnNotifyCommand(characteristic);
            }
            startActivationSequence();
        } else {
            Log.e(TAG, "IoT Sensors service not found");
            device.disconnect();
            Intent intent = new Intent(BroadcastUpdate.SENSOR_DATA_UPDATE);
            intent.putExtra("status", StatusUpdates.STATUS_SERVICE_NOT_FOUND);
            LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
        }
    }

    public void processCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        application.logger.debug("\tRECEIVE\t" + characteristic.getUuid() + "\t" + IotSensorsLogger.getLogStringFromBytes(characteristic.getValue()));
        if (characteristic.getUuid().equals(UUIDS.WEARABLES_CHARACTERISTIC_CONTROL_NOTIFY)) {
            byte[] value = characteristic.getValue();
            device.dataProcessor.processConfigurationReport(value);
            Intent intent = new Intent(BroadcastUpdate.CONFIGURATION_REPORT);
            intent.putExtra("command", value[1] & 0xff);
            intent.putExtra("data", Arrays.copyOfRange(value, 2, value.length));
            LocalBroadcastManager.getInstance(device.context).sendBroadcast(intent);
        } else if (characteristic.getUuid().equals(UUIDS.DWP_ACCELEROMETER) ||
                characteristic.getUuid().equals(UUIDS.DWP_GYROSCOPE) ||
                characteristic.getUuid().equals(UUIDS.DWP_HUMIDITY) ||
                characteristic.getUuid().equals(UUIDS.DWP_PRESSURE) ||
                characteristic.getUuid().equals(UUIDS.DWP_MAGNETOMETER) ||
                characteristic.getUuid().equals(UUIDS.DWP_TEMPERATURE) ||
                characteristic.getUuid().equals(UUIDS.DWP_SENSOR_FUSION)) {
            device.dataProcessor.processSensorReportBackground(characteristic.getValue(), false);
        } else if (characteristic.getUuid().equals(UUIDS.DWP_MULTI_SENSOR)) {
            device.dataProcessor.processSensorReportBackground(characteristic.getValue(), true);
        } else {
            Log.e(TAG, "Unknown UUID: " + characteristic.getUuid().toString());
        }
    }

    public void processCharacteristicWrite(BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "processCharacteristicWrite: " + characteristic.getUuid());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "write succeeded");
        } else {
            Log.e(TAG, "write failed: " + status);
        }

        dequeCommand();
    }

    public void processCharacteristicRead(BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "processCharacteristicRead: " + characteristic.getUuid());
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.i(TAG, "read succeeded");
            if (characteristic.getUuid().equals(UUIDS.WEARABLES_CHARACTERISTIC_FEATURES)) {
                device.dataProcessor.processFeaturesCharacteristic(characteristic);
            }
        } else {
            Log.e(TAG, "read failed: " + status);
        }

        dequeCommand();
    }

    public void processDescriptorWrite(BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "processDescriptorWrite: " + descriptor.getCharacteristic().getUuid() + ", status=" + status);
        dequeCommand();
    }


    public void startActivationSequence() {
        Log.d(TAG, "startActivationSequence()");
        device.startActivationSequence();
    }

    public void startDeactivationSequence() {
        Log.d(TAG, "startDeactivationSequence()");
        device.disconnect();
    }

    public void readFeatures() {
        Log.d(TAG, "readFeatures");
        readCharacteristic(UUIDS.WEARABLES_SERVICE_UUID, UUIDS.WEARABLES_CHARACTERISTIC_FEATURES);
    }

    public void sendCommand(int action) {
        writeCharacteristic(UUIDS.WEARABLES_SERVICE_UUID,
                UUIDS.WEARABLES_CHARACTERISTIC_CONTROL, action,
                BluetoothGattCharacteristic.FORMAT_UINT8);
    }

    public void sendCommandWithData(int action, byte[] data) {
        byte[] command = new byte[data.length + 1];
        command[0] = (byte) action;
        System.arraycopy(data, 0, command, 1, data.length);
        writeCharacteristic(UUIDS.WEARABLES_SERVICE_UUID, UUIDS.WEARABLES_CHARACTERISTIC_CONTROL, command);
    }

    public void sendReadFeaturesCommand() {
        Log.d(TAG, "sendReadFeaturesCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_READ_FEATURES);
    }

    public void sendReadVersionCommand() {
        Log.d(TAG, "sendReadVersionCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_READ_VERSION);
    }

    public void sendStartCommand() {
        Log.d(TAG, "sendStartCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CONFIGURATION_START);
    }

    public void sendStopCommand() {
        Log.d(TAG, "sendStopCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CONFIGURATION_STOP);
    }

    // BasicSettingsManager
    public void sendReadConfigCommand() {
        Log.d(TAG, "sendReadConfigCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CONFIGURATION_READ);
    }

    public void sendWriteConfigCommand(byte[] data) {
        Log.d(TAG, "sendWriteConfigCommand");
        sendCommandWithData(UUIDS.WEARABLES_COMMAND_CONFIGURATION_WRITE, data);
    }

    public void sendReadCalibrationModesCommand() {
        Log.d(TAG, "sendReadCalibrationModesCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_MODES);
    }

    public void sendWriteCalibrationModesCommand(byte[] data) {
        Log.d(TAG, "sendWriteCalibrationModesCommand");
        sendCommandWithData(UUIDS.WEARABLES_COMMAND_CALIBRATION_SET_MODES, data);
    }

    public void sendResetToDefaultsCommand() {
        Log.d(TAG, "sendResetToDefaultsCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CONFIGURATION_RESET_TO_DEFAULTS);
    }

    public void sendReadNvCommand() {
        Log.d(TAG, "sendReadNvCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CONFIGURATION_READ_NV);
    }

    public void sendWriteConfigToNvCommand() {
        Log.d(TAG, "sendWriteConfigToNvCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CONFIGURATION_STORE_NV);
    }

    // AccSettingsManager
    public void sendAccCalibrateCommand() {
        Log.d(TAG, "sendAccCalibrateCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CALIBRATION_ACCELEROMETER);
    }

    // CalibrationSettingsManager
    public void sendCalReadCommand() {
        Log.d(TAG, "sendCalReadCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_CONTROL);
    }

    public void sendCalWriteCommand(byte[] data) {
        Log.d(TAG, "sendCalWriteCommand");
        sendCommandWithData(UUIDS.WEARABLES_COMMAND_CALIBRATION_WRITE_CONTROL, data);
    }

    public void sendCalCoeffReadCommand() {
        Log.d(TAG, "sendCalCoeffReadCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CALIBRATION_READ_COEFFICIENTS);
    }

    public void sendCalCoeffWriteCommand(byte[] data) {
        Log.d(TAG, "sendCalCoeffWriteCommand");
        sendCommandWithData(UUIDS.WEARABLES_COMMAND_CALIBRATION_WRITE_COEFFICIENTS, data);
    }

    public void sendCalResetCommand() {
        Log.d(TAG, "sendCalResetCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CALIBRATION_RESET);
    }

    public void sendCalStoreNvCommand() {
        Log.d(TAG, "sendCalStoreNvCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_CALIBRATION_STORE_NV);
    }

    // SflSettingsManager
    public void sendSflReadCommand() {
        Log.d(TAG, "sendSflReadCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_SFL_READ);
    }

    public void sendSflWriteCommand(byte[] data) {
        Log.d(TAG, "sendSflWriteCommand");
        sendCommandWithData(UUIDS.WEARABLES_COMMAND_SFL_WRITE, data);
    }

    // LED blink
    public void sendStartLedBlinkCommand() {
        Log.d(TAG, "sendStartLedBlinkCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_START_LED_BLINK);
    }

    public void sendStopLedBlinkCommand() {
        Log.d(TAG, "sendStopLedBlinkCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_STOP_LED_BLINK);
    }

    // Proximity Hysteresis
    public void sendReadProximityHysteresisCommand() {
        Log.d(TAG, "sendReadProximityHysteresisCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_PROXIMITY_HYSTERESIS_READ);
    }

    public void sendWriteProximityHysteresisCommand(byte[] data) {
        Log.d(TAG, "sendReadProximityHysteresisCommand");
        sendCommandWithData(UUIDS.WEARABLES_COMMAND_PROXIMITY_HYSTERESIS_WRITE, data);
    }

    public void sendProximityCalibrationCommand() {
        Log.d(TAG, "sendProximityCalibrationCommand");
        sendCommand(UUIDS.WEARABLES_COMMAND_PROXIMITY_CALIBRATION);
    }

    public void syncClock(byte[] bytes) {
        writeCharacteristic(UUIDS.CURRENT_TIME_SERVICE, UUIDS.CURRENT_TIME_CHARACTERISTIC, bytes);
    }

    public void rebootDevice() {
        final int REBOOT_SIGNAL = 0xfd000000;
        writeCharacteristic(UUIDS.SPOTA_SERVICE_UUID, UUIDS.SPOTA_MEM_DEV_UUID, REBOOT_SIGNAL, BluetoothGattCharacteristic.FORMAT_UINT32);
    }
}
