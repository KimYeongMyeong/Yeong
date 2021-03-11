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

public class VCNL4010 {
    private static final String TAG = "VCNL4010";

    private static class AmbientLightSensor extends com.dialog.wearables.sensor.AmbientLightSensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            lowVoltage = data[offset] == 0;
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset + 2);
            ambientLight = raw / 4;
            value = new Value(ambientLight);
            return value;
        }
    }

    private static class ProximitySensor extends com.dialog.wearables.sensor.ProximitySensor {

        private int raw;

        @Override
        public Value processRawData(byte[] data, int offset) {
            lowVoltage = data[offset] == 0;
            raw = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(offset + 2);
            objectNearby = raw != 0;
            value = new Value(objectNearby ? 1 : 0);
            return value;
        }
    }

    private AmbientLightSensor ambientLightSensor;
    private ProximitySensor proximitySensor;

    public VCNL4010() {
        ambientLightSensor = new AmbientLightSensor();
        proximitySensor = new ProximitySensor();
    }

    public AmbientLightSensor getAmbientLightSensor() {
        return ambientLightSensor;
    }

    public ProximitySensor getProximitySensor() {
        return proximitySensor;
    }
}
