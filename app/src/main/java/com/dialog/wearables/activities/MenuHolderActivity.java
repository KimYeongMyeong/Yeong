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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

import com.dialog.wearables.GlobalVar;
import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.RuntimePermissionChecker;
import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.apis.internal.DataMsg;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.controller.GyroscopeController;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.defines.StatusUpdates;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.fragments.CloudFragment;
import com.dialog.wearables.fragments.DisclaimerFragment;
import com.dialog.wearables.fragments.InfoFragment;
import com.dialog.wearables.fragments.SensorFragment;
import com.dialog.wearables.fragments.SensorFusionFragment;
import com.dialog.wearables.fragments.SettingsFragment;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Menu activity that holds the menu items and manages the fragments inside of it
 */

public class MenuHolderActivity extends IotSensorsActivity {
    public static final String TAG = "MenuHolderActivity";

    private IotSensorsApplication application;
    private IotSensorsDevice device;
    private SensorFragment sensorFragment, sensorImuFragment;
    private SensorFusionFragment sensorFusionFragment;
    private InfoFragment infoFragment;
    private DisclaimerFragment disclaimerFragment;
    private CloudFragment cloudFragment;
    private SettingsFragment settingsFragment;
    private BroadcastReceiver connectionStateReceiver, configurationReportReceiver, statusReceiver, sensorDataReceiver, actuationReceiver;
    private Toolbar toolbar;
    private MenuItem startStopButton;
    private Drawer drawer;
    private SecondaryDrawerItem drawerItemSync, drawerItemReboot, drawerItemSec, disconnectButton;
    private int menuIoTSensors, menuImuSensors, menuSFL, menuCloud, menuGeneralSettings, menuInfo, menuDisclaimer;
    private int menuPosition;
    private boolean menuPendingAction;
    private int calibrationState = -1;
    private RuntimePermissionChecker permissionChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_holder);
        Log.d(TAG, "onCreate()");
        application = IotSensorsApplication.getApplication();
        device = application.device;
        permissionChecker = new RuntimePermissionChecker(this, savedInstanceState);

        ButterKnife.inject(this);
        toolbar = (Toolbar) findViewById(R.id.sensor_toolbar);
        if (toolbar != null) {
            toolbar.setTitleTextColor(Color.WHITE);
            setSupportActionBar(toolbar);
            toolbar.setTitle(R.string.app_name);
            toolbar.setSubtitle(R.string.dialog_semiconductor);
        } else {
            Log.e(TAG, "onCreate: Toolbar = null!");
        }

        connectionStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int connectionState = intent.getIntExtra("state", 0);
                if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                    finish();
                }
            }
        };

        configurationReportReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int command = intent.getIntExtra("command", -1);
                if (command == UUIDS.WEARABLES_COMMAND_CONFIGURATION_START || command == UUIDS.WEARABLES_COMMAND_CONFIGURATION_STOP || command == UUIDS.WEARABLES_COMMAND_CONFIGURATION_RUNNING_STATE) {
                    updateStartStopButtonTitle();
                }
            }
        };

        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                if (intent.getIntExtra("sensor", 0) == 3 && drawerItemSec != null) {
                    int oldCalibrationState = calibrationState;
                    calibrationState = intent.getIntExtra("calibrationState", 0);

                    // Update magneto status in menu
                    if (calibrationState == oldCalibrationState)
                        return;
                    switch (calibrationState) {
                        case 0: // DISABLED
                            drawerItemSec.withBadge(" ").withDescription(R.string.drawer_mag_status_disabled)
                                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.BLACK).withColorRes(R.color.magstatus_disabled));
                            drawer.updateStickyFooterItem(drawerItemSec);
                            break;
                        case 1: // INIT
                            drawerItemSec.withBadge(" ").withDescription(R.string.drawer_mag_status_init)
                                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.BLACK).withColorRes(R.color.magstatus_init));
                            drawer.updateStickyFooterItem(drawerItemSec);
                            break;
                        case 2: // BAD
                            drawerItemSec.withBadge(" ").withDescription(R.string.drawer_mag_status_bad)
                                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.magstatus_bad));
                            drawer.updateStickyFooterItem(drawerItemSec);
                            break;
                        case 3: // OK
                            drawerItemSec.withBadge(" ").withDescription(R.string.drawer_mag_status_ok)
                                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.magstatus_ok));
                            drawer.updateStickyFooterItem(drawerItemSec);
                            break;
                        case 4: // GOOD
                            drawerItemSec.withBadge(" ").withDescription(R.string.drawer_mag_status_good)
                                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.magstatus_good));
                            drawer.updateStickyFooterItem(drawerItemSec);
                            break;
                        case 5: // ERROR
                            drawerItemSec.withBadge(" ").withDescription(R.string.drawer_mag_status_error)
                                    .withBadgeStyle(new BadgeStyle().withTextColor(Color.WHITE).withColorRes(R.color.magstatus_error));
                            drawer.updateStickyFooterItem(drawerItemSec);
                            break;
                    }
                }
            }
        };

        sensorDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                switch (intent.getIntExtra("status", -1)) {
                    case StatusUpdates.STATUS_FEATURES_READ:
                        // Enable settings menu item
                        if (drawer != null && device.getFeatures().valid()) {
                            PrimaryDrawerItem settings = (PrimaryDrawerItem) drawer.getDrawerItems().get(menuGeneralSettings - 1);
                            settings.withEnabled(true);
                            drawer.updateItem(settings);
                        }
                        // Remove magneto status from menu for old IoT
                        if (!device.isNewVersion() && drawerItemSec != null) {
                            drawer.removeAllStickyFooterItems();
                            drawer.addStickyFooterItem(drawerItemSync);
                            drawer.addStickyFooterItem(drawerItemReboot);
                            drawer.addStickyFooterItem(disconnectButton);
                            drawerItemSec = null;
                        }
                        break;

                    case StatusUpdates.STATUS_ONE_SHOT_CALIBRATION_COMPLETE:
                        int sensor = 0;
                        switch (intent.getIntExtra("sensor", -1)) {
                            case UUIDS.SENSOR_TYPE_ACCELEROMETER:
                                sensor = R.string.accelerometer;
                                break;
                            case UUIDS.SENSOR_TYPE_GYROSCOPE:
                                sensor = R.string.gyroscope;
                                break;
                            case UUIDS.SENSOR_TYPE_MAGNETOMETER:
                                sensor = R.string.magnetometer;
                                break;
                        }
                        if (sensor == 0) {
                            Toast.makeText(getApplicationContext(), R.string.oneshot_calibration_complete, Toast.LENGTH_LONG).show();
                        } else {
                            boolean ok = intent.getBooleanExtra("ok", true);
                            String msg = String.format(getString(ok ? R.string.sensor_oneshot_calibration_complete : R.string.sensor_oneshot_calibration_error), getString(sensor));
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        };

        actuationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                DataMsg msg = intent.getParcelableExtra("EXTRA_DATA");
                if (msg == null)
                    return;
                StringBuilder logMsg = new StringBuilder("Actuation message: [" + msg.EKID + "], events:");
                for (DataEvent event : msg.Events)
                    logMsg.append(" {type=").append(event.EventType).append(", data=").append(event.Data).append("}");
                Log.d(TAG, logMsg.toString());
                device.processActuationMessage(msg);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(connectionStateReceiver,
                new IntentFilter(BroadcastUpdate.CONNECTION_STATE_UPDATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(configurationReportReceiver,
                new IntentFilter(BroadcastUpdate.CONFIGURATION_REPORT));
        LocalBroadcastManager.getInstance(this).registerReceiver(statusReceiver,
                new IntentFilter(BroadcastUpdate.STATUS_RECEIVER));
        LocalBroadcastManager.getInstance(this).registerReceiver(sensorDataReceiver,
                new IntentFilter(BroadcastUpdate.SENSOR_DATA_UPDATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(actuationReceiver,
                new IntentFilter(BroadcastUpdate.IOT_APPS_ACTUATION_UPDATE));

        if (device.state == IotSensorsDevice.DISCONNECTED)
            finish();

        initialize();
    }

    private void initialize() {
        int menuItemsTotal = 7;

        IDrawerItem[] drawerItems = new IDrawerItem[menuItemsTotal];
        int i = 0;

        if (device.getSecondarySensorLayout() == 0) {
            drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_iot_sensors).withIcon(GoogleMaterial.Icon.gmd_speaker_phone);
            menuIoTSensors = i;
        } else {
            drawerItems = Arrays.copyOf(drawerItems, ++menuItemsTotal);
            drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_env_sensors).withIcon(GoogleMaterial.Icon.gmd_speaker_phone);
            menuIoTSensors = i;
            drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_imu_sensors).withIcon(GoogleMaterial.Icon.gmd_explore);
            menuImuSensors = i;
        }
        drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_sensor_fusion).withIcon(R.drawable.icon_sensor_fusion).withSelectedIcon(R.drawable.icon_dialog);
        menuSFL = i;
        drawerItems[i++] = new DividerDrawerItem();
        drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_cloud).withIcon(GoogleMaterial.Icon.gmd_cloud).withEnabled(device.cloudSupport());
        menuCloud = i;
        drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_settings).withIcon(GoogleMaterial.Icon.gmd_settings).withEnabled(device.getFeatures().valid());
        menuGeneralSettings = i;
        drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_information).withIcon(R.drawable.cic_info).withSelectedIcon(R.drawable.cic_info_selected);
        menuInfo = i;
        drawerItems[i++] = new PrimaryDrawerItem().withName(R.string.drawer_disclaimer).withIcon(R.drawable.cic_disclaimer).withSelectedIcon(R.drawable.cic_disclaimer_selected);
        menuDisclaimer = i;
        if (device.type == IotSensorsDevice.TYPE_WEARABLE) {
            drawerItemSync = new SecondaryDrawerItem()
                    .withName(R.string.drawer_sync_clock)
                    .withEnabled(true);
            drawerItemReboot = new SecondaryDrawerItem()
                    .withName(R.string.drawer_reboot_device)
                    .withEnabled(true);
        }
        if (!device.getFeatures().valid() || device.isNewVersion())
            drawerItemSec = new SecondaryDrawerItem()
                    .withName(R.string.drawer_magnetometer)
                    .withDescription(R.string.drawer_mag_status_unknown)
                    .withIdentifier(200)
                    .withEnabled(false);

        disconnectButton = new SecondaryDrawerItem() {
            @Override
            public void onPostBindView(IDrawerItem drawerItem, View view) {
                super.onPostBindView(drawerItem, view);
                view.setBackgroundColor(getResources().getColor(R.color.button_color));
            }

            @Override
            @LayoutRes
            public int getLayoutRes() {
                return R.layout.disconnect_drawer_button;
            }
        };

        disconnectButton
                .withTextColor(ContextCompat.getColor(this, android.R.color.white))
                .withName(R.string.drawer_disconnect)
                .withIdentifier(300)
                .withEnabled(true);

        drawer = createNavDrawer(drawerItems);
        changeFragment(getFragmentItem(1), 1);
    }


    private Drawer createNavDrawer(IDrawerItem[] drawerItems) {
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.navigation_bar_background)
                .addProfiles(new ProfileDrawerItem().withName(getString(R.string.app_name)).withEmail(getString(R.string.dialog_semiconductor)))
                .withProfileImagesClickable(false)
                .withProfileImagesVisible(false)
                .withSelectionListEnabledForSingleProfile(false)
                .withTextColor(ContextCompat.getColor(this, android.R.color.white))
                .build();

        Drawer drawer = new DrawerBuilder().withActivity(this)
                .withAccountHeader(accountHeader)
                .withToolbar(toolbar)
                .addDrawerItems(drawerItems)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        Log.d(TAG, "Menu position: " + String.valueOf(position));
                        if (menuPendingAction || menuPosition == position) {
                            return false;
                        }
                        if (position == -1) {
                            if (drawerItem == drawerItemSync) {
                                device.manager.syncClock(getCurrentTimeInBytes());
                                Toast.makeText(getApplicationContext(), "Clock synced", Toast.LENGTH_SHORT).show();
                                return true;
                            } else if (drawerItem == drawerItemReboot) {
                                device.manager.rebootDevice();
                                Toast.makeText(getApplicationContext(), "Device Rebooted", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        } else {
                            menuPendingAction = true;
                            MenuHolderActivity.this.drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                            changeFragment(getFragmentItem(position), position);
                        }
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        menuPendingAction = false;
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        if (menuPendingAction) {
                            MenuHolderActivity.this.drawer.setSelectionAtPosition(menuPosition, false);
                            MenuHolderActivity.this.drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                            menuPendingAction = false;
                        }
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                    }
                })
                .addStickyDrawerItems(drawerItemSec != null ? new IDrawerItem[]{drawerItemSync, drawerItemReboot, drawerItemSec, disconnectButton} : new IDrawerItem[]{drawerItemSync, drawerItemReboot, disconnectButton})
                .withStickyFooterShadow(false)
                .build();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
        }
        return drawer;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionStateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(configurationReportReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(statusReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sensorDataReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(actuationReceiver);
        if (sensorFragment != null)
            sensorFragment.stop();
        if (sensorImuFragment != null)
            sensorImuFragment.stop();
        if (sensorFusionFragment != null)
            sensorFusionFragment.stop();
        device.manager.startDeactivationSequence();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        permissionChecker.saveState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean checkPermission(String permission) {
        return permissionChecker.checkPermission(permission, null, null);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();

        if (drawer != null && drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else if (count > 0) {
            getFragmentManager().popBackStack();
            if (settingsFragment != null) {
                settingsFragment.destroyFragments();
            }
        } else {
            if (sensorFragment != null)
                sensorFragment.stop();
            if (sensorImuFragment != null)
                sensorImuFragment.stop();
            if (sensorFusionFragment != null)
                sensorFusionFragment.stop();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sensor, menu);
        startStopButton = menu.findItem(R.id.startStopButton);
        updateStartStopButtonTitle();

        return true;
    }

    public void updateStartStopButtonTitle() {
        if (startStopButton != null) {
            startStopButton.setTitle(device.isStarted ? "Stop" : "Start");
        }
    }

    public Fragment getFragmentItem(final int position) {
        if (position == menuIoTSensors) {
            if (sensorFragment == null) {
                sensorFragment = new SensorFragment();
            }
            return sensorFragment;
        } else if (position == menuImuSensors) {
            if (sensorImuFragment == null) {
                sensorImuFragment = new SensorFragment();
                Bundle args = new Bundle();
                args.putInt("layout", device.getSecondarySensorLayout());
                sensorImuFragment.setArguments(args);
            }
            return sensorImuFragment;
        } else if (position == menuSFL) {
            if (sensorFusionFragment == null) {
                sensorFusionFragment = new SensorFusionFragment();
            }
            return sensorFusionFragment;
        } else if (position == menuCloud) {
            if (cloudFragment == null) {
                cloudFragment = new CloudFragment();
            }
            return cloudFragment;
        } else if (position == menuGeneralSettings) {
            if (settingsFragment == null) {
                settingsFragment = new SettingsFragment();
            }
            return settingsFragment;
        } else if (position == menuInfo) {
            if (infoFragment == null) {
                infoFragment = new InfoFragment();
            }
            return infoFragment;
        } else if (position == menuDisclaimer) {
            if (disclaimerFragment == null) {
                disclaimerFragment = new DisclaimerFragment();
            }
            return disclaimerFragment;
        }
        return new Fragment();
    }

    /**
     * 해당 토클 버튼으로 BLE로 부터 데이터를 수집할 수 있음
     * todo 임시로 BLE 데이터를 수집함
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.startStopButton:
                if (device.isStarted) {
                    device.manager.sendStopCommand();
                    if (null != timer) {
                        timer.cancel();
                    }
                } else {
                    device.manager.sendStartCommand();
                    timer = new Timer();
                    timerTask = getTimerTask();
                    timer.schedule(timerTask, 1000, 20);
                }
                device.isStarted = !device.isStarted;
                updateStartStopButtonTitle();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    Timer timer;
    TimerTask timerTask;

    TimerTask getTimerTask() {
        return new TimerTask() {
            private Object GyroscopeController;

            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                String datetimes = dateFormat.format(calendar.getTime());

                SharedPreferences case_id = getSharedPreferences("case_id", MODE_PRIVATE);
                Integer case_num = case_id.getInt("case_id", 0);


//                Toast.makeText(this, case_num, Toast.LENGTH_SHORT).show();
//                long currentDatetime = System.currentTimeMillis();
//                double datetimes = (currentDatetime / 1000) + (0.001 * (currentDatetime % 1000));
                try {
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("dev_id", 1);
                    jsonData.put("case_id", case_num);
                    jsonData.put("datetimes", datetimes);
                    jsonData.put("acc_x", GlobalVar.acc.getX());
                    jsonData.put("acc_y", GlobalVar.acc.getY());
                    jsonData.put("acc_z", GlobalVar.acc.getZ());
                    jsonData.put("roll", GlobalVar.gyroEuler.getRoll());
                    jsonData.put("pitch", GlobalVar.gyroEuler.getPitch());
                    jsonData.put("yaw", GlobalVar.gyroEuler.getYaw());
                    JSONArray jsonDataArr = new JSONArray();
                    jsonDataArr.put(jsonData);
                    JSONObject pushingJsonObj = new JSONObject();
                    pushingJsonObj.put("cmd", "push");
                    pushingJsonObj.put("data", jsonDataArr);
                    Log.d(TAG, "pushing data: " + pushingJsonObj.toString());
//                    sendHttpJson(pushingJsonObj);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public void sendHttpJson(JSONObject jsonObject) {
        try {
            OkHttpClient client = new OkHttpClient();
            String url = "http://qedlab.kr:10001/healthband/";
//            String url = "http://52.231.143.85:10001/healthband";
            Request request = new Request.Builder().url(url)
//                        .post(RequestBody.create(insertJsonData.toString(), JSON))
                    .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString()))
                    .build();
//                response = client.newCall(request).execute();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "error: " + e.toString());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.i(TAG, "response status: " + response.code());
                    Log.d(TAG, "response body: " + response.body().string());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void changeFragment(Fragment newFragment, int position) {
        menuPosition = position;
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fragmentManager.getBackStackEntryAt(0);
            fragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (settingsFragment != null) {
                settingsFragment.destroyFragments();
            }
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, newFragment);
        fragmentTransaction.commit();
    }

    public IotSensorsDevice getDevice() {
        return device;
    }

    /**
     * Generates a byte array with the current time following the bluetooth specifications
     *
     * @return Byte array with time
     * @see "https://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.current_time.xml"
     */
    public byte[] getCurrentTimeInBytes() {
        byte[] bytes = new byte[10];
        Calendar c = new GregorianCalendar();
        bytes[0] = (byte) (c.get(Calendar.YEAR) & 0xFF);
        bytes[1] = (byte) ((c.get(Calendar.YEAR) >> 8) & 0xFF);
        bytes[2] = (byte) (c.get(Calendar.MONTH) + 1);
        bytes[3] = (byte) c.get(Calendar.DAY_OF_MONTH);
        bytes[4] = (byte) c.get(Calendar.HOUR_OF_DAY);
        bytes[5] = (byte) c.get(Calendar.MINUTE);
        bytes[6] = (byte) c.get(Calendar.SECOND);
        switch (c.get(Calendar.DAY_OF_WEEK)) {
            case 0x01:
                bytes[7] = (byte) 0x07;
                break;
            case 0x02:
                bytes[7] = (byte) 0x01;
                break;
            case 0x03:
                bytes[7] = (byte) 0x02;
                break;
            case 0x04:
                bytes[7] = (byte) 0x03;
                break;
            case 0x05:
                bytes[7] = (byte) 0x04;
                break;
            case 0x06:
                bytes[7] = (byte) 0x05;
                break;
            case 0x07:
                bytes[7] = (byte) 0x06;
                break;
            default:
                bytes[7] = (byte) 0x00;
                break;
        }
        bytes[8] = (byte) 0x00;
        bytes[9] = (byte) 0x01; // Manual time update
        return bytes;
    }
}

