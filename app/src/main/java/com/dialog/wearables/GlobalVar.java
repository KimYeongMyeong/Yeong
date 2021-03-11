package com.dialog.wearables;

import android.content.Intent;

import com.dialog.wearables.activities.MainActivity;
import com.dialog.wearables.sensor.IotSensor;

public class GlobalVar {

    public static class GyroEuler {
        private static float qw;
        private static float qx;
        private static float qy;
        private static float qz;
        private static float roll;
        private static float pitch;
        private static float yaw;

        /*ym customizing*/


        /*ym customizing*/
        public synchronized void setVar(IotSensor.Value value) {
            roll = value.getRoll();
            pitch = value.getPitch();
            yaw = value.getY();
        }
        public synchronized String toString(){
            return String.format("(roll, pitch, yaw):\n (%.2f, %.2f, %.2f)",
                    roll, pitch, yaw);
        }

        public static float getRoll() {
            return roll;
        }

        public static float getPitch() {
            return pitch;
        }

        public static float getYaw() {
            return yaw;
        }
    }

    public static class Acc {
        private static float x;
        private static float y;
        private static float z;

        public void setVar(IotSensor.Value value) {
            x = value.getX();
            y = value.getY();
            z = value.getZ();
        }
        public String toString() {
            return String.format("(x, y, z):\n(%.2f, %.2f, %.2f)", x, y, z);
        }

        public static float getX() {
            return x;
        }

        public static float getY() {
            return y;
        }

        public static float getZ() {
            return z;
        }
    }

//    public static class Atmo {
//        private static float hpa;
//
//        public void setVar(float value) {
//            hpa = value;
//        }
//        public String toString() {
//            return String.format("(x, y, z):\n(%.2f, %.2f, %.2f)", hpa);
//        }
//    }

    public static GyroEuler gyroEuler = new GyroEuler();
    public static Acc acc = new Acc();
//    public static Atmo atmo = new Atmo();
}
