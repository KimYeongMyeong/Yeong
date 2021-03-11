/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import com.dialog.wearables.R;
import com.dialog.wearables.fragments.CloudFragment;
import com.dialog.wearables.fragments.CloudSettingsFragment;
import com.dialog.wearables.global.Utils;

public class CloudActivity extends IotSensorsActivity {

    public static final int FRAGMENT_CLOUD = 0;
    public static final int FRAGMENT_CLOUD_SETTINGS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        Fragment fragment = null;
        switch (getIntent().getIntExtra("fragment", -1)) {
            case FRAGMENT_CLOUD:
                fragment = new CloudFragment();
                break;
            case FRAGMENT_CLOUD_SETTINGS:
                fragment = new CloudSettingsFragment();
                break;
        }
        if (fragment != null)
            Utils.replaceFragment(this, fragment, R.id.fragment_container, false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
