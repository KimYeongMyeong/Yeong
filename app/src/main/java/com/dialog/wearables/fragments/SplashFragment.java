/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2017 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dialog.wearables.R;
import com.dialog.wearables.global.BusProvider;

public class SplashFragment extends Fragment {

    private static final int SPLASH_TIME = 3000;
    private static final String TAG = "SplashFragment";
    private Handler mHandler = new Handler();

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splash, container, false);
        // Dismiss timer
        Runnable exitRunnable = new Runnable() {
            @Override
            public void run() {
                BusProvider.getInstance().post(new SplashEvent());
            }
        };
        mHandler.postDelayed(exitRunnable, SPLASH_TIME);
        // Dismiss on click
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BusProvider.getInstance().post(new SplashEvent());
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static class SplashEvent {
        //
    }
}
