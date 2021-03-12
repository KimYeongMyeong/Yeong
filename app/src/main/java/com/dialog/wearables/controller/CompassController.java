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

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.Object3DLoader;
import com.dialog.wearables.sensor.IotSensor;
import com.dialog.wearables.sensor.Magnetometer;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import min3d.core.Object3dContainer;
import min3d.vos.Light;
import min3d.vos.LightType;

public class CompassController extends ThreeDimensionController {

    private static final int[] lineColors = {Color.BLACK, Color.BLACK, Color.BLACK};

    public static final float GRAPH_HEIGHT = 360.0f;
    public static class GraphValueProcessor implements IotSensor.GraphValueProcessor {
        @Override
        public IotSensor.Value process(IotSensor.Value v) {
            return new IotSensor.Value3D(v.getX() + GRAPH_HEIGHT / 2, v.getY() + GRAPH_HEIGHT / 2, v.getZ() + GRAPH_HEIGHT / 2);
        }
    }

    private boolean loadCancelled = false;
    private ObjectLoader objectLoader;

    public CompassController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getMagnetometer(), fragment, R.id.compassView, R.id.compassChart, R.id.compassShape, lineColors);
        graphDataSize = device.compassGraphData.X.capacity();
        label = (TextView) fragment.getView().findViewById(R.id.compassLabel);
    }

    @Override
    protected void generateObject() {
        if (Object3DLoader.arrow == null) {
            getInitSceneHandler().post(new Runnable() {
                @Override
                public void run() {
                    objectLoader = new ObjectLoader();
                    objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } else {
            object3D = Object3DLoader.arrow;
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
                while (Object3DLoader.arrow == null && !isCancelled())
                    Object3DLoader.waitForObject();
            }
            return Object3DLoader.arrow;
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
    protected void setLabelValue(float value) {
        setLabelString(String.valueOf((int) value) + " " + fragment.getString(Magnetometer.getCompassHeading(value)));
    }

    @Override
    public void updateScene() {
        if (!object3DConfigured || !sensor.validValue())
            return;

        object3D.rotation().z = -((Magnetometer)sensor).getDegrees();
    }

    @Override
    protected void addLights() {
        Log.d(TAG, "addLights");
        Light lightGreen;
        lightGreen = new Light();
        lightGreen.ambient.setAll(0xff66b9ed);
        lightGreen.diffuse.setAll(0x993079ba);
        lightGreen.type(LightType.POSITIONAL); // looks nicer, especially with multiple lights interacting with each other
        scene.lights().add(lightGreen);
        scene.lights().add(lightGreen);
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

        if (sensor.validValue())
            setLabelValue(((Magnetometer)sensor).getHeading());
    }

    @Override
    protected List<List<PointValue>> getGraphData3D() {
        List<List<PointValue>> list = new ArrayList<>(3);
        lastX = device.compassGraphData.getList(list);
        /*lastX = device.compassGraphData.getProcessedList(list, new PointValueBuffer3D.DataProcessor() {
            @Override
            public PointValue process(int dim, PointValue p) {
                return new PointValue(p.getX(), p.getY() + GRAPH_HEIGHT / 2);
            }
        });*/
        return list;
    }
}
