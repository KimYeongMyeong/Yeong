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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UUIDS {

//    public static final UUID WEARABLES_SERVICE_580 = UUID.fromString("00002ea7-0000-1000-8000-00805f9b34fb");
    public static final UUID WEARABLES_SERVICE_580 = UUID.fromString("0000ebb4-0000-1000-8000-00805f9b34fb");
    public static final UUID WEARABLES_SERVICE_680 = UUID.fromString("00002800-0000-1000-8000-00805f9b34fb");
//    public static final UUID WEARABLES_SERVICE_UUID = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402400");
    public static final UUID WEARABLES_SERVICE_UUID = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f00");
    public static final UUID IBEACON_ASSET_UUID = UUID.fromString("5ec6ed9b-0025-139a-cc42-011b93de5c58");
    public static final UUID CURRENT_TIME_SERVICE = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    public static final UUID SPOTA_SERVICE_UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb");

    public static final int WEARABLES_COMMAND_CONFIGURATION_STOP = 0;
    public static final int WEARABLES_COMMAND_CONFIGURATION_START = 1;
    public static final int WEARABLES_COMMAND_CONFIGURATION_READ_NV = 2;
    public static final int WEARABLES_COMMAND_CONFIGURATION_RESET_TO_DEFAULTS = 3;
    public static final int WEARABLES_COMMAND_CONFIGURATION_STORE_NV = 4;
    public static final int WEARABLES_COMMAND_CONFIGURATION_RUNNING_STATE = 6;
    public static final int WEARABLES_COMMAND_CONFIGURATION_WRITE = 10;
    public static final int WEARABLES_COMMAND_CONFIGURATION_READ = 11;
    public static final int WEARABLES_COMMAND_SFL_WRITE = 12;
    public static final int WEARABLES_COMMAND_SFL_READ = 13;
    public static final int WEARABLES_COMMAND_CALIBRATION_WRITE_COEFFICIENTS = 14;
    public static final int WEARABLES_COMMAND_CALIBRATION_READ_COEFFICIENTS = 15;
    public static final int WEARABLES_COMMAND_CALIBRATION_WRITE_CONTROL = 16;
    public static final int WEARABLES_COMMAND_CALIBRATION_READ_CONTROL = 17;
    public static final int WEARABLES_COMMAND_CALIBRATION_ACCELEROMETER = 18;
    public static final int WEARABLES_COMMAND_CALIBRATION_SET_MODES = 19;
    public static final int WEARABLES_COMMAND_CALIBRATION_READ_MODES = 20;
    public static final int WEARABLES_COMMAND_READ_FEATURES = 21;
    public static final int WEARABLES_COMMAND_READ_VERSION = 22;
    public static final int WEARABLES_COMMAND_START_LED_BLINK = 23;
    public static final int WEARABLES_COMMAND_STOP_LED_BLINK = 24;
    public static final int WEARABLES_COMMAND_PROXIMITY_HYSTERESIS_WRITE = 25;
    public static final int WEARABLES_COMMAND_PROXIMITY_HYSTERESIS_READ = 26;
    public static final int WEARABLES_COMMAND_CALIBRATION_COMPLETE = 27;
    public static final int WEARABLES_COMMAND_PROXIMITY_CALIBRATION = 28;

    public static final int WEARABLES_COMMAND_CALIBRATION_STORE_NV = 5;
    public static final int WEARABLES_COMMAND_CALIBRATION_RESET = 7;

    public static final int REPORT_ACCELEROMETER = 1;
    public static final int REPORT_GYROSCOPE = 2;
    public static final int REPORT_MAGNETOMETER = 3;
    public static final int REPORT_PRESSURE = 4;
    public static final int REPORT_HUMIDITY = 5;
    public static final int REPORT_TEMPERATURE = 6;
    public static final int REPORT_SENSOR_FUSION = 7;
    public static final int REPORT_AMBIENT_LIGHT = 9;
    public static final int REPORT_PROXIMITY = 10;
    public static final int REPORT_GAS = 11;
    public static final int REPORT_AIR_QUALITY = 12;
    public static final int REPORT_BUTTON = 13;
    public static final int REPORT_VELOCITY_DELTA = 14;
    public static final int REPORT_EULER_ANGLE_DELTA = 15;
    public static final int REPORT_QUATERNION_DELTA = 16;

    public static final int MULTI_SENSOR_REPORT_PREAMBLE = 0xA5;
    public static final int REPORT_LENGTH_SINGLE = 7;
    public static final int REPORT_LENGTH_3D = 9;
    public static final int REPORT_LENGTH_4D = 11;
    public static final int REPORT_ACCELEROMETER_LENGTH = REPORT_LENGTH_3D;
    public static final int REPORT_GYROSCOPE_LENGTH = REPORT_LENGTH_3D;
    public static final int REPORT_MAGNETOMETER_LENGTH = REPORT_LENGTH_3D;
    public static final int REPORT_PRESSURE_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_HUMIDITY_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_TEMPERATURE_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_SENSOR_FUSION_LENGTH = REPORT_LENGTH_4D;
    public static final int REPORT_AMBIENT_LIGHT_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_PROXIMITY_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_GAS_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_AIR_QUALITY_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_BUTTON_LENGTH = REPORT_LENGTH_SINGLE;
    public static final int REPORT_VELOCITY_DELTA_LENGTH = REPORT_LENGTH_3D;
    public static final int REPORT_EULER_ANGLE_DELTA_LENGTH = REPORT_LENGTH_3D;
    public static final int REPORT_QUATERNION_DELTA_LENGTH = REPORT_LENGTH_4D;

//    public static final UUID DWP_ACCELEROMETER = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402401");
//    public static final UUID DWP_GYROSCOPE = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402402");
//    public static final UUID DWP_MAGNETOMETER = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402403");
//    public static final UUID DWP_PRESSURE = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402404");
//    public static final UUID DWP_HUMIDITY = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402405");
//    public static final UUID DWP_TEMPERATURE = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402406");
//    public static final UUID DWP_SENSOR_FUSION = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402407");
//    public static final UUID DWP_MULTI_SENSOR = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402410");
//    public static final UUID WEARABLES_CHARACTERISTIC_FEATURES = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402408");
//    public static final UUID WEARABLES_CHARACTERISTIC_CONTROL = UUID.fromString("2ea78970-7d44-44bb-b097-26183f402409");
//    public static final UUID WEARABLES_CHARACTERISTIC_CONTROL_NOTIFY = UUID.fromString("2ea78970-7d44-44bb-b097-26183f40240a");
    public static final UUID DWP_ACCELEROMETER = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f01");
    public static final UUID DWP_GYROSCOPE = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f02");
    public static final UUID DWP_MAGNETOMETER = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f03");
    public static final UUID DWP_PRESSURE = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f04");
    public static final UUID DWP_HUMIDITY = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f05");
    public static final UUID DWP_TEMPERATURE = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f06");
    public static final UUID DWP_SENSOR_FUSION = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f07");
    public static final UUID DWP_MULTI_SENSOR = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f10");
    public static final UUID WEARABLES_CHARACTERISTIC_FEATURES = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f08");
    public static final UUID WEARABLES_CHARACTERISTIC_CONTROL = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f09");
    public static final UUID WEARABLES_CHARACTERISTIC_CONTROL_NOTIFY = UUID.fromString("ebb46677-7a73-4060-98e4-edd3049d8f0a");
    public static final UUID CURRENT_TIME_CHARACTERISTIC = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb");
    public static final UUID SPOTA_MEM_DEV_UUID = UUID.fromString("8082caa8-41a6-4021-91c6-56f9b954cc34");

    public static final UUID CLIENT_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final int SENSOR_TYPE_ACCELEROMETER = 0;
    public static final int SENSOR_TYPE_GYROSCOPE = 1;
    public static final int SENSOR_TYPE_MAGNETOMETER = 2;

    public static final int CALIBRATION_MODE_NONE = 0;
    public static final int CALIBRATION_MODE_STATIC = 1;
    public static final int CALIBRATION_MODE_CONTINUOUS_AUTO = 2;
    public static final int CALIBRATION_MODE_ONE_SHOT_AUTO = 3;

    public static final int CALIBRATION_AUTO_MODE_BASIC = 0;
    public static final int CALIBRATION_AUTO_MODE_SMART = 1;

    public static final int FEATURE_NONE = 0;
    public static final int FEATURE_ACCELEROMETER = 1;
    public static final int FEATURE_GYROSCOPE = 2;
    public static final int FEATURE_MAGNETOMETER = 3;
    public static final int FEATURE_PRESSURE = 4;
    public static final int FEATURE_HUMIDITY = 5;
    public static final int FEATURE_TEMPERATURE = 6;
    public static final int FEATURE_AMBIENT_LIGHT = 7;
    public static final int FEATURE_PROXIMITY = 8;
    public static final int FEATURE_BUTTON = 9;
    public static final int FEATURE_RAW_GAS = 10;
    public static final int FEATURE_PROXIMITY_CALIBRATION = 11;
    public static final int FEATURE_SENSOR_FUSION = 64;
    public static final int FEATURE_INTEGRATION_ENGINE = 65;
    public static final int FEATURE_AIR_QUALITY = 66;

    public static List<UUID> getAdvertisedServices(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            int length = buffer.get() & 0xff;
            if (length == 0) break;

            int type = buffer.get() & 0xff;
            --length;

            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2 && buffer.remaining() >= 2) {
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16 && buffer.remaining() >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
            }

            if (length > buffer.remaining())
                break;
            buffer.position(buffer.position() + length);
        }

        return uuids;
    }

    public static byte[] getManufacturerSpecificData(byte[] advertisedData, int manufacturer) {
        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            int length = buffer.get() & 0xff;
            if (length == 0) break;

            int type = buffer.get() & 0xff;
            --length;

            if (type == 0xff && length >= 2 && buffer.remaining() >= 2) {
                int id = buffer.getShort() & 0xffff;
                length -= 2;
                if (id == manufacturer && length <= buffer.remaining()) {
                    byte[] data = new byte[length];
                    buffer.get(data);
                    return data;
                }
            }

            if (length > buffer.remaining())
                break;
            buffer.position(buffer.position() + length);
        }

        return null;
    }
}
