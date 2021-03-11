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
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.Object3DLoader;
import com.dialog.wearables.sensor.IotSensor;

import min3d.Utils;
import min3d.core.Object3dContainer;
import min3d.vos.Color4;
import min3d.vos.Color4Managed;
import min3d.vos.Light;
import min3d.vos.Number3dManaged;

public class SensorFusionController extends ThreeDimensionController {
    private static final String TAG = "SensorFusionController";

    private static final int[] lineColors = {Color.BLACK, Color.BLACK, Color.BLACK};

    private boolean loadCancelled = false;
    private ObjectLoader objectLoader;

    public SensorFusionController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getSensorFusion(), fragment, 0, 0, R.id.sensorFusionShape, lineColors);
    }

    @Override
    protected void generateObject() {
        Log.d(TAG, "generateObject: " + device.type);
        Bitmap b = Utils.makeBitmapFromResourceId(device.get3DTexture());
        renderer.getTextureManager().addTextureId(b, "pattern");
        b.recycle();

        if (device.get3DModel() == null) {
            getInitSceneHandler().post(new Runnable() {
                @Override
                public void run() {
                    objectLoader = new ObjectLoader();
                    objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } else {
            object3D = device.get3DModel();
        }
    }

    @Override
    public void startInterval() {
        if (loadCancelled) {
            loadCancelled = false;
            objectLoader = new ObjectLoader();
            objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public void stopInterval() {
        if (objectLoader != null && !object3DConfigured) {
            objectLoader.cancel(true);
            loadCancelled = true;
        }
        getInitSceneHandler().removeCallbacksAndMessages(null);
        getUpdateSceneHandler().removeCallbacksAndMessages(null);
    }

    private class ObjectLoader extends AsyncTask<Void, Void, Object3dContainer> {

        @Override
        protected Object3dContainer doInBackground(Void... nothing) {
            synchronized (Object3DLoader.sync) {
                while (device.get3DModel() == null && !isCancelled())
                    Object3DLoader.waitForObject();
            }
            return device.get3DModel();
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
    protected void addLights() {
        Light light = new Light();
        light.ambient = new Color4Managed(100, 100, 100, 128, light);
        light.position = new Number3dManaged(0f, 10f, 10f, light);
        scene.lights().add(light);

        Light light2 = new Light();
        light2.ambient = new Color4Managed(100, 100, 100, 128, light2);
        light2.position = new Number3dManaged(0f, 10f, -10f, light2);
        scene.lights().add(light2);
    }

    @Override
    public void initScene() {
        super.initScene();

        scene.backgroundColor().setAll(new Color4(224, 237, 246, 255));

        if (object3D != null) {
            object3D.textures().clear();
            object3D.textures().addById("pattern", renderer.getTextureManager());

            object3D.getChildAt(0).textures().clear();
            object3D.getChildAt(0).textures().addById("pattern", renderer.getTextureManager());
            if (object3D.numChildren() > 1) {
                object3D.getChildAt(1).textures().clear();
                object3D.getChildAt(1).textures().addById("pattern", renderer.getTextureManager());
            }
        }
    }

//    todo gyro scene update
    @Override
    public void updateScene() {
        scene.backgroundColor().setAll(new Color4(224, 237, 246, 255));

        if (!object3DConfigured || !sensor.validValue())
            return;

        IotSensor.Value value = sensor.getValue();
        object3D.rotation().x = value.getRoll();
        object3D.rotation().y = value.getYaw();
        object3D.rotation().z = value.getPitch();
    }
}
