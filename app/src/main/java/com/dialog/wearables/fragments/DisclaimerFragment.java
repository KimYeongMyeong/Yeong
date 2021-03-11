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
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.dialog.wearables.R;
import com.dialog.wearables.global.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DisclaimerFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_disclaimer, container, false);
        AssetManager assetManager = getActivity().getAssets();
        WebView webView = (WebView) fragmentView.findViewById(R.id.webView);

        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(assetManager.open("info.html")));
            String html = Utils.readerToString(r);
            html = html.replace("[version]", "");
            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fragmentView;
    }
}
