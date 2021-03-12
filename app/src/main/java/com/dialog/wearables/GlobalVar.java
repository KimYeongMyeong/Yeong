package com.dialog.wearables;

import android.content.Intent;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.activities.MainActivity;
import com.dialog.wearables.sensor.IotSensor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import ee.ioc.phon.android.speechutils.Log;
import lecho.lib.hellocharts.model.PointValue;

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

    public static class GyroEulerArr {
        private static Queue<Float> eulerDataX = new LinkedList<>();
        private static Queue<Float> eulerDataY = new LinkedList<>();
        private static Queue<Float> eulerDataZ = new LinkedList<>();
        // todo 나머도 동일한 방식으로 구현할 것
        public void pushEulerDataX(PointValue pointValue) {
            eulerDataX.offer(pointValue.getY());
        }

        public void pushEulerDataX(List<PointValue> pointValues) {
            Log.i("List<PointValue> pointValues eulerX: "+ pointValues);
            for (PointValue itVal : pointValues) {
                eulerDataX.offer(itVal.getY());
                Log.i("eulerDataX: "+itVal.getY());
            }
        }

        public void pushEulerDataY(List<PointValue> pointValues) {
            Log.i("List<PointValue> pointValues euler: "+ pointValues);
            for (PointValue itVal : pointValues) {
                eulerDataY.offer(itVal.getY());
                Log.i("eulerDataY: "+itVal.getY());
            }
        }

        public void pushEulerDataZ(List<PointValue> pointValues) {
            for (PointValue itVal : pointValues) {
                eulerDataZ.offer(itVal.getY());
                Log.i("eulerDataZ: "+itVal.getY());
            }
        }

        /**
         *
         * @return isEmpty => null
         */
        public float popEulerDataX() {
            return eulerDataX.poll();
        }
        public boolean isEulerDataXEmpty() {
            return eulerDataX.isEmpty();
        }

        public float popEulerDataY() {
            return eulerDataY.poll();
        }
        public boolean isEulerDataYEmpty() {
            return eulerDataY.isEmpty();
        }

        public float popEulerDataZ() {
            return eulerDataZ.poll();
        }
        public boolean isEulerDataZEmpty() {
            return eulerDataZ.isEmpty();
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
    public static class AccArr {
        private static Queue<Float> accDataX = new LinkedList<>();
        private static Queue<Float> accDataY = new LinkedList<>();
        private static Queue<Float> accDataZ = new LinkedList<>();
        // todo 나머도 동일한 방식으로 구현할 것
        public void pushAccDataX(PointValue pointValue) {
            accDataX.offer(pointValue.getY());
        }

        /**/

        /**/
        public void pushAccDataX(List<PointValue> pointValues) {
            for (PointValue itVal : pointValues) {
                accDataX.offer(itVal.getY());
                Log.i("accXitVal.getY(): "+itVal.getY());
            }
        }

        public void pushAccDataY(List<PointValue> pointValues) {
            for (PointValue itVal : pointValues) {
                accDataY.offer(itVal.getY());
                Log.i("accYitVal.getY(): "+itVal.getY());
            }
        }

        public void pushAccDataZ(List<PointValue> pointValues) {
            for (PointValue itVal : pointValues) {
                accDataZ.offer(itVal.getY());
            }
        }

        /**
         *
         * @return
         */
        public float popAccDataX() {
            return accDataX.poll();
        }
        public boolean isAccDataXEmpty() {
            return accDataX.isEmpty();
        }

        public float popAccDataY() {
            return accDataY.poll();
        }
        public boolean isAccDataYEmpty() {
            return accDataY.isEmpty();
        }

        public  float popAccDataZ() {
            return accDataZ.poll();
        }
        public boolean isAccDataZEmpty() {
            return accDataZ.isEmpty();
        }
    }

    public static GyroEuler gyroEuler = new GyroEuler();
    public static Acc acc = new Acc();
    public static GyroEulerArr gyroEulerArr = new GyroEulerArr();
    public static AccArr accArr = new AccArr();

//    public static Atmo atmo = new Atmo();
}