/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.controller;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.dialog.wearables.GlobalVar;
import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.Object3DLoader;
import com.dialog.wearables.sensor.AccelerometerIntegration;
import com.dialog.wearables.sensor.IotSensor;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import min3d.core.Object3dContainer;
import min3d.vos.Light;
import min3d.vos.LightType;

public class AccelerometerController extends ThreeDimensionController {

    private static final int[] lineColors = {Color.BLACK, Color.BLACK, Color.BLACK};

    private TextView accParamTv;
    private Activity activity;

    public static final float GRAPH_HEIGHT = 4.0f;
    public static class GraphValueProcessor implements IotSensor.GraphValueProcessor {
        @Override
        public IotSensor.Value process(IotSensor.Value v) {
            return new IotSensor.Value3D(v.getX() + GRAPH_HEIGHT / 2, v.getY() + GRAPH_HEIGHT / 2, v.getZ() + GRAPH_HEIGHT / 2);
        }
    }

    private boolean loadCancelled = false;
    private ObjectLoader objectLoader;

    public AccelerometerController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getAccelerometer(), fragment, R.id.accelerometerView, R.id.accelerometerChart, R.id.accelerometerShape, lineColors);
        accParamTv = fragment.getView().findViewById(R.id.accelerometerParamTv);
        activity = fragment.getActivity();
        graphDataSize = device.accelerometerGraphData.X.capacity();
    }

    @Override
    protected void addLights() {
        Log.d(TAG, "addLights");
        Light lightRed;
        lightRed = new Light();
        lightRed.ambient.setAll(0xff66b9ed);
        lightRed.diffuse.setAll(0x993079ba);
        lightRed.type(LightType.POSITIONAL); // looks nicer, especially with multiple lights interacting with each other
        scene.lights().add(lightRed);
        scene.lights().add(lightRed);
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Viewport v = new Viewport();
        v.bottom = 0;
        v.top = GRAPH_HEIGHT;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    @Override
    protected void generateObject() {
        if (Object3DLoader.cube == null) {
            getInitSceneHandler().post(new Runnable() {
                @Override
                public void run() {
                    objectLoader = new ObjectLoader();
                    objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } else {
            object3D = Object3DLoader.cube;
        }
    }

    @Override
    public void startInterval() {
        if (loadCancelled) {
            loadCancelled = false;
            objectLoader = new ObjectLoader();
            objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        super.startInterval();
    }

    @Override
    public void stopInterval() {
        if (objectLoader != null && !object3DConfigured) {
            objectLoader.cancel(true);
            loadCancelled = true;
        }
        super.stopInterval();
    }

    private class ObjectLoader extends AsyncTask<Void, Void, Object3dContainer> {

        @Override
        protected Object3dContainer doInBackground(Void... nothing) {
            synchronized (Object3DLoader.sync) {
                while (Object3DLoader.cube == null && !isCancelled())
                    Object3DLoader.waitForObject();
            }
            return Object3DLoader.cube;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        @Override
        protected void onPostExecute(Object3dContainer result) {
            object3D = result;
            initScene();
        }
    }

    @Override
    public void updateScene() {
        if (!object3DConfigured || !sensor.validValue())
            return;

        IotSensor.Value value = !device.integrationEngine ? sensor.getValue() : ((AccelerometerIntegration)sensor).getAccelerationInG();
        float x = value.getX();
        float y = value.getY();
        float z = value.getZ();

        Boolean compensateForGravity = false;
        if (compensateForGravity) {
            float q3 = (float) (Math.cos(x / 2f) * Math.cos(y / 2f) * Math.cos(z / 2f) + Math.sin(x / 2f) * Math.sin(y / 2f) * Math.sin(z / 2f));
            float q0 = (float) (Math.sin(x / 2f) * Math.cos(y / 2f) * Math.cos(z / 2f) - Math.cos(x / 2f) * Math.sin(y / 2f) * Math.sin(z / 2f));
            float q1 = (float) (Math.cos(x / 2f) * Math.sin(y / 2f) * Math.cos(z / 2f) + Math.sin(x / 2f) * Math.cos(y / 2f) * Math.sin(z / 2f));
            float q2 = (float) (Math.cos(x / 2f) * Math.cos(y / 2f) * Math.sin(z / 2f) - Math.sin(x / 2f) * Math.sin(y / 2f) * Math.cos(z / 2f));

            float gx = 2 * (q1 * q3 - q0 * q2);
            float gy = 2 * (q0 * q1 + q2 * q3);
            float gz = q0 * q0 - q1 * q1 - q2 * q2 + q3 * q3;

            object3D.position().x = (y - gx) * 0.66f;
            object3D.position().y = (z - gy) * 0.66f;
            object3D.position().z = (x - gz) * 0.66f;
        } else {
            object3D.position().x = y / 4.0f;
            object3D.position().y = z / 4.0f;
            object3D.position().z = x / 4.0f;
        }
        GlobalVar.acc.setVar(value);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                accParamTv.setText(GlobalVar.acc.toString());
            }
        });
    }

    @Override
    protected List<List<PointValue>> getGraphData3D() {
        List<List<PointValue>> list = new ArrayList<>(3);
        lastX = device.accelerometerGraphData.getList(list);
        /*lastX = device.accelerometerGraphData.getProcessedList(list, new PointValueBuffer3D.DataProcessor() {
            @Override
            public PointValue process(int dim, PointValue p) {
                return new PointValue(p.getX(), p.getY() + GRAPH_HEIGHT / 2);
            }
        });*/
        return list;
    }
}
