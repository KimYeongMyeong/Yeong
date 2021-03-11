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
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.PointValueBuffer;
import com.dialog.wearables.sensor.IotSensor;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

public abstract class IotSensorController {
    protected static final String TAG = "IotSensorController";

    private static final int THREAD_INTERVAL = 200;
    protected static final int GRAPH_TRANSPARENCY = 50;
    private static final int SHAPE_SIZE_PERCENTAGE = 40;

    protected IotSensorsApplication application;
    protected IotSensorsDevice device;
    protected IotSensor sensor;
    protected Thread thread;
    protected boolean active = false;
    protected boolean needsUpdate;
    protected int lineColor = 0x01999999;
    protected Fragment fragment;
    protected GLSurfaceView glSurfaceView;
    protected TextView label;
    protected List<Line> lines;
    protected LineChartData lineChartData;
    protected LineChartView chart;
    protected RelativeLayout graphLayout;
    protected int lastX;
    protected int graphDataSize = IotSensorsDevice.GRAPH_DATA_SIZE;

    protected IotSensorController(IotSensorsDevice device, IotSensor sensor, Fragment fragment, int viewId, int chartId) {
        this.device = device;
        this.sensor = sensor;
        this.fragment = fragment;
        application = IotSensorsApplication.getApplication();

        if (viewId != 0) {
            graphLayout = (RelativeLayout) fragment.getView().findViewById(viewId);
            if (chartId != 0) {
                lines = new ArrayList<>();
                chart = (LineChartView) fragment.getView().findViewById(chartId);
                chart.setZoomEnabled(false);
                chart.setViewportCalculationEnabled(false);
            }

            ViewTreeObserver viewTreeObserver = graphLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        graphLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int shapeSize = SHAPE_SIZE_PERCENTAGE * graphLayout.getHeight() / 100;
                        setShapeSize(shapeSize);
                    }
                });
            }

            startInterval();
        }
    }

    protected abstract void setShapeSize(int size);

    protected List<PointValue> getGraphData() {
        return null;
    }

    public IotSensor getSensor() {
        return sensor;
    }

    public void setSensor(IotSensor sensor) {
        this.sensor = sensor;
    }

    public void startInterval() {
        Log.d(TAG, "startInterval");
        needsUpdate = true;
        if (active)
            return;
        active = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active) {
                    try {
                        if (needsUpdate) {
                            needsUpdate = false;
                            updateUI();
                        }
                        Thread.sleep(THREAD_INTERVAL);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void stopInterval() {
        active = false;
    }

    public void update() {
        needsUpdate = true;
    }

    public void setSurfaceViewVisible(boolean visible) {
        int visibility = visible ? View.VISIBLE : View.GONE;
        glSurfaceView.setVisibility(visibility);
    }

    protected abstract void setLabelValue(float value);

    protected void setLabelString(final String value) {
        fragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (value != null) {
                    label.setText(value);
                }
            }
        });
    }

    protected void updateUI() {
        if (chart != null) {
            Line line = getLine(getGraphData(), lineColor, GRAPH_TRANSPARENCY);
            lines = new ArrayList<>();
            lines.add(line);
            lineChartData = new LineChartData(lines);
            chart.setLineChartData(lineChartData);
        }

        if (sensor.validValue())
            setLabelValue(sensor.getDisplayValue());
    }

    protected List<PointValue> getList(PointValueBuffer buffer) {
        List<PointValue> list = buffer.getList();
        if (!list.isEmpty())
            lastX = (int) list.get(list.size() - 1).getX();
        return list;
    }

    protected static int percentageToPixels(int percentage, int totalPixels) {
        return (totalPixels * percentage) / 100;
    }

    protected static Line getLine(List<PointValue> pointValues, int color, int transparency) {
        return new Line(pointValues)
                .setColor(color)
                .setCubic(true)
                .setHasPoints(false)
                .setFilled(true)
                .setAreaTransparency(transparency)
                .setStrokeWidth(0);
    }
}
