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

public class BME680 extends BME280 {
    private static final String TAG = "BME680";

    private static class GasSensor extends com.dialog.wearables.sensor.GasSensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset);
            reading = raw;
            value = new Value(reading);
            return value;
        }
    }

    private static class AirQualitySensor extends com.dialog.wearables.sensor.AirQualitySensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            accuracy = data[offset];
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset + 1);
            quality = raw;
            calculateAirQualityIndex();
            value = new Value(quality);
            return value;
        }
    }

    private GasSensor gasSensor;
    private AirQualitySensor airQualitySensor;

    public BME680() {
        gasSensor = new GasSensor();
        airQualitySensor = new AirQualitySensor();
    }

    public GasSensor getGasSensor() {
        return gasSensor;
    }

    public AirQualitySensor getAirQualitySensor() {
        return airQualitySensor;
    }
}
