/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public abstract class IotSensor {

    public static class Value {

        private float value1;

        public Value(float value) {
            value1 = value;
        }

        public float get() {
            return value1;
        }

        public float getX() {
            return value1;
        }

        public float getY() {
            return value1;
        }

        public float getZ() {
            return value1;
        }

        public float getW() {
            return value1;
        }

        public float getRoll() {
            return value1;
        }

        public float getPitch() {
            return value1;
        }

        public float getYaw() {
            return value1;
        }

        public int getDim() {
            return 1;
        }
    }

    public static class Value3D extends Value {

        private float value2;
        private float value3;

        public Value3D(float x, float y, float z) {
            super(x);
            this.value2 = y;
            this.value3 = z;
        }

        @Override
        public float getY() {
            return value2;
        }

        @Override
        public float getZ() {
            return value3;
        }

        @Override
        public float getPitch() {
            return value2;
        }

        @Override
        public float getYaw() {
            return value3;
        }

        @Override
        public int getDim() {
            return 3;
        }
    }

    public static class Value4D extends Value3D {

        private float value4;

        public Value4D(float x, float y, float z, float w) {
            super(x, y, z);
            this.value4 = w;
        }

        @Override
        public float getW() {
            return value4;
        }

        @Override
        public int getDim() {
            return 4;
        }
    }

    protected Value value;

    public boolean validValue() {
        return value != null;
    }

    public Value getValue() {
        return value;
    }

    public String getLogTag() {
        return null;
    }

    public Value getLogValue() {
        return value;
    }

    public String getLogValueUnit() {
        return "";
    }

    public String getLogEntry() {
        Value value = getLogValue();
        switch (value.getDim()) {
            case 1:
                return String.format("%.2f%s", value.get(), getLogValueUnit());
            case 3:
                return String.format("%8.2f%4$s %8.2f%4$s %8.2f%4$s", value.getX(), value.getY(), value.getZ(), getLogValueUnit());
            case 4:
                return String.format("%8.2f%5$s %8.2f%5$s %8.2f%5$s %8.2f%5$s", value.getW(), value.getX(), value.getY(), value.getZ(), getLogValueUnit());
        }
        return null;
    }

    public float getDisplayValue() {
        return value.get();
    }

    public interface GraphValueProcessor {
        Value process(Value v);
    }

    protected GraphValueProcessor graphValueProcessor;

    public void setGraphValueProcessor(GraphValueProcessor graphValueProcessor) {
        this.graphValueProcessor = graphValueProcessor;
    }

    public Value getGraphValue() {
        Value value = getUnprocessedGraphValue();
        return graphValueProcessor == null ? value : graphValueProcessor.process(value);
    }

    protected Value getUnprocessedGraphValue() {
        return value;
    }

    public int[] get3DValuesLE(byte[] data, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(offset);
        int x = buffer.getShort();
        int y = buffer.getShort();
        int z = buffer.getShort();
        return new int[]{ x, y, z };
    }

    public int[] get4DValuesLE(byte[] data, int offset) {
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(offset);
        int w = buffer.getShort();
        int x = buffer.getShort();
        int y = buffer.getShort();
        int z = buffer.getShort();
        return new int[]{ x, y, z, w };
    }

    public Value processRawData(byte[] data, int offset) {
        return null;
    }

    protected Value getCloudValue() {
        return value;
    }

    public String getCloudData() {
        Value value = getCloudValue();
        switch (value.getDim()) {
            case 1:
                return String.format(Locale.US, "%.3f", value.get());
            case 3:
                return String.format(Locale.US, "%.3f %.3f %.3f", value.getX(), value.getY(), value.getZ());
            case 4:
                return String.format(Locale.US, "%.3f %.3f %.3f %.3f", value.getX(), value.getY(), value.getZ(), value.getW());
        }
        return null;
    }
}
