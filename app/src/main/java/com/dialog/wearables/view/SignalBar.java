/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2017 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.dialog.wearables.R;

/**
 * Drawable for signal bars
 */
public class SignalBar extends View {
    private static final String TAG = "SignalBar";
    private static final int levels[] = new int[] { -95, -80, -70, -60, -40 };
    private Paint mPaint;
    private int bars;

    public SignalBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeWidth(8);

        setWillNotDraw(false);
        invalidate();
    }

    public void setRssi(int rssi) {
        int prevBars = bars;
        bars = 0;
        for (int level : levels) {
            if (rssi >= level)
                ++bars;
        }
        if (bars != prevBars)
            invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int x = canvas.getMaximumBitmapWidth(), y = canvas.getMaximumBitmapHeight(), width = getWidth() / 5, height = getHeight() / 5;
        int color = getResources().getColor(R.color.signal_bar_colour), nonactive = getResources().getColor(R.color.signal_bar_non_active_colour);
        int drawBars = bars;

        // First bar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(--drawBars >= 0 ? color : nonactive);
        canvas.drawRect(0, height * 4, x, y, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(0, height * 4, x, y, mPaint);

        // Second bar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(--drawBars >= 0 ? color : nonactive);
        canvas.drawRect(width, height * 3, x, y, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(width, height * 3, x, y, mPaint);

        // Third bar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(--drawBars >= 0 ? color : nonactive);
        canvas.drawRect(width * 2, height * 2, x, y, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(width * 2, height * 2, x, y, mPaint);

        // Fourth bar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(--drawBars >= 0 ? color : nonactive);
        canvas.drawRect(width * 3, height, x, y, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(width * 3, height, x, y, mPaint);

        // Fifth bar
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(--drawBars >= 0 ? color : nonactive);
        canvas.drawRect(width * 4, 0, x, y, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(width * 4, 0, width * 5, y, mPaint);
    }

}
