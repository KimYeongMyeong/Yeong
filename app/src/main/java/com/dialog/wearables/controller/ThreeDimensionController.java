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
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.widget.RelativeLayout;

import com.dialog.wearables.TransparentBackgroundRenderer;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.sensor.IotSensor;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import min3d.Shared;
import min3d.core.Object3dContainer;
import min3d.core.Renderer;
import min3d.core.Scene;
import min3d.interfaces.ISceneController;
import min3d.vos.RenderType;

public abstract class ThreeDimensionController extends IotSensorController implements ISceneController {
    protected static final String TAG = "ThreeDimensionControl";

    private static final double SIZE_MULTIPLICATION_VALUE = 2.5;
    protected static final int GRAPH_TRANSPARENCY = 25;

    protected Renderer renderer;
    protected Scene scene;
    private Handler initSceneHandler;
    private Handler updateSceneHandler;
    protected Object3dContainer object3D;
    protected boolean isSceneInitialized = false;
    protected boolean object3DConfigured = false;

    private final Runnable initSceneRunnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    private final Runnable updateSceneRunnable = new Runnable() {
        @Override
        public void run() {
        }
    };

    public ThreeDimensionController(IotSensorsDevice device, IotSensor sensor, Fragment fragment, int viewId, int chartId, int openGlId, int[] lineColors) {
        super(device, sensor, fragment, viewId, chartId);

        initSceneHandler = new Handler();
        updateSceneHandler = new Handler();

        Shared.context(application);

        scene = new Scene(this);

        renderer = new TransparentBackgroundRenderer(scene);
        Shared.renderer(renderer);

        glSurfaceView = (GLSurfaceView) fragment.getView().findViewById(openGlId);
        glSurfaceView.setZOrderOnTop(false);

        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    protected void setShapeSize(int size) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) glSurfaceView.getLayoutParams();
        size *= SIZE_MULTIPLICATION_VALUE;
        params.width = size * 5 / 4;
        params.height = size;
        glSurfaceView.setLayoutParams(params);
    }

    protected abstract void generateObject();

    public void initScene() {
        if (!isSceneInitialized) {
            scene.fogEnabled(false);
            generateObject();
            isSceneInitialized = true;
        }
        if (object3D != null && !object3DConfigured) {
            object3D.texturesEnabled(true);
            object3D.lineWidth(8);
            object3D.renderType(RenderType.LINE_STRIP);
            object3D.doubleSidedEnabled(true);

            addLights();
            scene.lightingEnabled(true);
            object3D.lightingEnabled(true);

            scene.addChild(object3D);
            object3DConfigured = true;
        }
        if (object3DConfigured) {
            object3D.position().x = object3D.position().y = object3D.position().z = 0;
            object3D.rotation().x = object3D.rotation().y = object3D.rotation().z = 0;
        }
    }

    protected abstract void addLights();

    public abstract void updateScene();

    public void renderContinuously(boolean renderContinuously) {
        glSurfaceView.setRenderMode(renderContinuously ? GLSurfaceView.RENDERMODE_CONTINUOUSLY : GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public Handler getInitSceneHandler() {
        return initSceneHandler;
    }

    public Handler getUpdateSceneHandler() {
        return updateSceneHandler;
    }

    public Runnable getInitSceneRunnable() {
        return initSceneRunnable;
    }

    public Runnable getUpdateSceneRunnable() {
        return updateSceneRunnable;
    }

    protected List<List<PointValue>> getGraphData3D() {
        return null;
    }

    @Override
    protected void setLabelValue(float value) {
    }

    @Override
    protected void updateUI() {
        List<Line> lines = new ArrayList<>();
        for (List<PointValue> data : getGraphData3D()) {
            Line line = getLine(data, lineColor, GRAPH_TRANSPARENCY);
            line.setStrokeWidth(0);
            lines.add(line);
        }
        lineChartData = new LineChartData(lines);
        chart.setLineChartData(lineChartData);
    }
}
