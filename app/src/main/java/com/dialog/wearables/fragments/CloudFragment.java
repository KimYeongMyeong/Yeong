/*
 *******************************************************************************
 *
 * Copyright (C) 2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.adapters.CloudAdapter;
import com.dialog.wearables.cloud.DataMessenger;

public class CloudFragment extends Fragment {
    private static final String TAG = CloudFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_cloud, container, false);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.pager);
        CloudAdapter cloudAdapter = new CloudAdapter(getChildFragmentManager());
        cloudAdapter.addFragment(new HistoricalFragment(), "Historical");
        cloudAdapter.addFragment(new AlertingRulesFragment(), "Alerting");
        cloudAdapter.addFragment(new ControlRulesFragment(), "Control");
        cloudAdapter.addFragment(new AlexaFragment(), "Alexa");
        cloudAdapter.addFragment(new AssetTrackingFragment(), "Asset Tracking");
        viewPager.setAdapter(cloudAdapter);

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }
}
