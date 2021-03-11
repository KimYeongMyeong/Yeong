/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2017 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables;

import android.opengl.GLES20;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import min3d.core.Renderer;
import min3d.core.Scene;

public class TransparentBackgroundRenderer extends Renderer {

    public TransparentBackgroundRenderer(Scene $scene) {
        super($scene);
    }

    public void onSurfaceCreated(GL10 $gl, EGLConfig eglConfig) {
        super.onSurfaceCreated($gl, eglConfig);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
        super.onSurfaceChanged(gl, w, h);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
    }

}

