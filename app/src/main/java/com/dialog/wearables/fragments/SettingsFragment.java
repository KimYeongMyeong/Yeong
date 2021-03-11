/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.adapters.SettingsListAdapter;

import java.util.Arrays;

/**
 * Settings fragment ListView
 */
public class SettingsFragment extends ListFragment {
    private static final String TAG = "SettingsFragment";

    private static final int[] imgId = {
            R.drawable.settings_basic,
            R.drawable.settings_calibration,
            R.drawable.settings_sfl,
            R.drawable.settings_acc,
            R.drawable.settings_cloud,
            R.drawable.settings_ifttt,
    };

    private BasicSettingsFragment basicSettingsFragment;
    private CalibrationSettingsFragment calibrationSettingsFragment;
    private SflSettingsFragment sflSettingsFragment;
    private AccSettingsFragment accSettingsFragment;
    private CloudSettingsFragment cloudSettingsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] values = getResources().getStringArray(R.array.settings_list);
        // Remove cloud settings if no cloud support
        if (!IotSensorsApplication.getApplication().device.cloudSupport())
            values = Arrays.copyOfRange(values, 0, values.length - 1);
        SettingsListAdapter adapter = new SettingsListAdapter(getActivity(), values, imgId);
        setListAdapter(adapter);
    }

    public void updateListeners(int activePage) {
        if (basicSettingsFragment != null) {
            basicSettingsFragment.setStatus(activePage == 0);
        }
        if (calibrationSettingsFragment != null) {
            calibrationSettingsFragment.setStatus(activePage == 1);
        }
        if (sflSettingsFragment != null) {
            sflSettingsFragment.setStatus(activePage == 2);
        }
        if (accSettingsFragment != null) {
            accSettingsFragment.setStatus(activePage == 3);
        }
        if (cloudSettingsFragment != null) {
            cloudSettingsFragment.setStatus(activePage == 4);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        switch (position) {
            case 0:
                // Basic settings
                if (basicSettingsFragment == null) {
                    basicSettingsFragment = new BasicSettingsFragment();
                }
                ft.replace(R.id.fragment_container, basicSettingsFragment);
                break;
            case 1:
                // Calibration
                if (calibrationSettingsFragment == null) {
                    calibrationSettingsFragment = new CalibrationSettingsFragment();
                }
                ft.replace(R.id.fragment_container, calibrationSettingsFragment);
                break;
            case 2:
                // Sensor Fusion
                if (sflSettingsFragment == null) {
                    sflSettingsFragment = new SflSettingsFragment();
                }
                ft.replace(R.id.fragment_container, sflSettingsFragment);
                break;
            case 3:
                // Accelerometer
                if (accSettingsFragment == null) {
                    accSettingsFragment = new AccSettingsFragment();
                }
                ft.replace(R.id.fragment_container, accSettingsFragment);
                break;
            case 4:
                // Cloud
                if (cloudSettingsFragment == null) {
                    cloudSettingsFragment = new CloudSettingsFragment();
                }
                ft.replace(R.id.fragment_container, cloudSettingsFragment);
                break;
            default:
                ft.replace(R.id.fragment_container, new Fragment());
                break;
        }
        ft.commit();
        updateListeners(position);
    }

    /**
     * Destroy fragments so you can use their listeners again
     */
    public void destroyFragments() {
        accSettingsFragment = null;
        calibrationSettingsFragment = null;
        sflSettingsFragment = null;
        basicSettingsFragment = null;
        cloudSettingsFragment = null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }
}
