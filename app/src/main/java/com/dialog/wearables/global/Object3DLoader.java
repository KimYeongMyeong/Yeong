/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.global;

import android.content.Context;
import android.os.AsyncTask;

import com.dialog.wearables.R;

import min3d.core.Object3dContainer;
import min3d.parser.IParser;
import min3d.parser.Parser;

public class Object3DLoader {

    public static final Object sync = new Object();
    public static Object3dContainer cube;
    public static Object3dContainer arrow;
    public static Object3dContainer gyro;
    public static Object3dContainer dialog;
    public static Object3dContainer watch;
    public static Object3dContainer iot585;

    private interface ObjectLoaded {
        void onObjectLoaded(Object3dContainer object);
    }

    private static class LoadObject extends AsyncTask<Void, Void, Object3dContainer> {

        private Context context;
        private int objResId;
        private ObjectLoaded callback;

        public LoadObject(Context context, int objResId, ObjectLoaded callback) {
            this.context = context;
            this.objResId = objResId;
            this.callback = callback;
        }

        @Override
        protected Object3dContainer doInBackground(Void... nothing) {
            IParser parser = Parser.createParser(Parser.Type.OBJ, context.getResources(), objResId, true);
            parser.parse();
            return parser.getParsedObject();
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        @Override
        protected void onPostExecute(Object3dContainer object) {
            if (callback != null)
                callback.onObjectLoaded(object);
        }
    }

    public static void startLoading(Context context) {
        context = context.getApplicationContext();
        if (cube == null) {
            new LoadObject(context, R.raw.cube_obj, new ObjectLoaded() {
                @Override
                public void onObjectLoaded(Object3dContainer object) {
                    object.scale().setAll(0.5f, 0.5f, 0.5f);
                    synchronized (sync) {
                        cube = object;
                        sync.notifyAll();
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (arrow == null) {
            new LoadObject(context, R.raw.arrow_obj, new ObjectLoaded() {
                @Override
                public void onObjectLoaded(Object3dContainer object) {
                    synchronized (sync) {
                        arrow = object;
                        sync.notifyAll();
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (gyro == null) {
            new LoadObject(context, R.raw.gyro_obj, new ObjectLoaded() {
                @Override
                public void onObjectLoaded(Object3dContainer object) {
                    object.scale().setAll(0.7f, 0.7f, 0.7f);
                    synchronized (sync) {
                        gyro = object;
                        sync.notifyAll();
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (dialog == null) {
            new LoadObject(context, R.raw.dialog3_obj, new ObjectLoaded() {
                @Override
                public void onObjectLoaded(Object3dContainer object) {
                    object.scale().setAll(0.5f, 0.5f, 0.5f);
                    synchronized (sync) {
                        dialog = object;
                        sync.notifyAll();
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (watch == null) {
            new LoadObject(context, R.raw.dialogwatch_obj, new ObjectLoaded() {
                @Override
                public void onObjectLoaded(Object3dContainer object) {
                    object.scale().setAll(0.85f, 0.85f, 0.85f);
                    synchronized (sync) {
                        watch = object;
                        sync.notifyAll();
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if (iot585 == null) {
            new LoadObject(context, R.raw.iot585_obj, new ObjectLoaded() {
                @Override
                public void onObjectLoaded(Object3dContainer object) {
                    object.scale().setAll(0.4f, 0.4f, 0.4f);
                    synchronized (sync) {
                        iot585 = object;
                        sync.notifyAll();
                    }
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public static void waitForObject() {
        try {
            synchronized (sync) {
                sync.wait();
            }
        } catch (InterruptedException e) {
        }
    }
}
