/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor.chip;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BME280 {
    private static final String TAG = "BME280";

    private static class TemperatureSensor extends com.dialog.wearables.sensor.TemperatureSensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset);
            temperature = raw / 100.f;
            value = new Value(temperature);
            return value;
        }

        @Override
        public int getTemperatureUnit() {
            return CELSIUS;
        }
    }

    private static class HumiditySensor extends com.dialog.wearables.sensor.HumiditySensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset);
            humidity = raw / 1024.f;
            value = new Value(humidity);
            return value;
        }
    }

    private static class PressureSensor extends com.dialog.wearables.sensor.PressureSensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset);
            pressure = raw;
            value = new Value(pressure);
            return value;
        }
    }

    private TemperatureSensor temperatureSensor;
    private HumiditySensor humiditySensor;
    private PressureSensor pressureSensor;

    public BME280() {
        temperatureSensor = new TemperatureSensor();
        humiditySensor = new HumiditySensor();
        pressureSensor = new PressureSensor();
    }

    public TemperatureSensor getTemperatureSensor() {
        return temperatureSensor;
    }

    public HumiditySensor getHumiditySensor() {
        return humiditySensor;
    }

    public PressureSensor getPressureSensor() {
        return pressureSensor;
    }
}
