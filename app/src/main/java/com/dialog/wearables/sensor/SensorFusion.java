/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.sensor;

public abstract class SensorFusion extends IotSensor {

    protected float qx, qy, qz, qw;
    protected Value quaternion;
    protected float roll, pitch, yaw;
    protected Value valueRad;

    public Value getQuaternion() {
        return quaternion;
    }

    public Value getValueRad() {
        return valueRad;
    }

    protected void sensorFusionCalculation() {
        roll = (float) Math.atan2(2 * qy * qw - 2 * qx * qz, 1 - 2 * qy * qy - 2 * qz * qz);
        pitch = (float) Math.atan2(2 * qx * qw - 2 * qy * qz, 1 - 2 * qx * qx - 2 * qz * qz);
        yaw = (float) Math.asin(2 * qx * qy + 2 * qz * qw);

        valueRad = new Value3D(roll, pitch, yaw);
        value = new Value3D((float)(roll * 180 / Math.PI), (float)(pitch * 180 / Math.PI), (float)(yaw * 180 / Math.PI));
    }

    public static final String LOG_TAG = "SFL";

    @Override
    public String getLogTag() {
        return LOG_TAG;
    }

    @Override
    protected Value getCloudValue() {
        return quaternion;
    }
}
