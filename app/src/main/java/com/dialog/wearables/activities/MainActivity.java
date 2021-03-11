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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dialog.wearables.GlobalVar;
import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.RuntimePermissionChecker;
import com.dialog.wearables.adapters.ScanAdapter;
import com.dialog.wearables.apis.common.eEventTypes;
import com.dialog.wearables.apis.internal.ConfigurationMsg;
import com.dialog.wearables.apis.internal.DataEvent;
import com.dialog.wearables.apis.internal.DataMsg;
import com.dialog.wearables.bluetooth.UUIDS;
import com.dialog.wearables.cloud.DataManager;
import com.dialog.wearables.cloud.NetworkConnectivityReceiver;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.defines.ScanItem;
import com.dialog.wearables.defines.StatusUpdates;
import com.dialog.wearables.device.IotDeviceSpec;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.IotSensorsLogger;
import com.dialog.wearables.global.Utils;
import com.dialog.wearables.settings.CloudSettingsManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import min3d.Shared;

import static junit.runner.BaseTestRunner.savePreferences;

public class MainActivity extends IotSensorsActivity {
    public static final String TAG = "MainActivity";

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;

    private IotSensorsApplication application;
    private BluetoothAdapter mBluetoothAdapter;
    private SharedPreferences preferences;
    private Handler handler;
    private Runnable scanTimer;
    private ArrayList<BluetoothDevice> bluetoothDeviceList;
    private ArrayList<ScanItem> deviceList;
    private ScanAdapter bluetoothScanAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BroadcastReceiver connectionStateReceiver, sensorDataReceiver, cloudConfigurationReceiver, bluetoothStateReceiver;
    private NetworkConnectivityReceiver networkConnectivityReceiver;
    private boolean showCloudMenu = false;
    private boolean isScanning = false;
    private boolean connecting = false;
    private Boolean locationServicesRequired;
    private boolean locationServicesSkipCheck;
    @InjectView(R.id.deviceListView) ListView deviceListView;
    @InjectView(R.id.view_container) LinearLayout mViewContainer;
    @InjectView(R.id.progress_container) View mProgressView;
    private RuntimePermissionChecker permissionChecker;

    EditText case_id;
    TextView textView;
    Button btn_case_id;



    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*--case id --*/
        case_id = (EditText)findViewById(R.id.case_id);
        textView = findViewById(R.id.textView);
        /*btn_case_id = (Button)findViewById(R.id.clickSetBt);

        /*--case id --*/

        application = IotSensorsApplication.getApplication();
        application.logger = IotSensorsLogger.getLogger(MainActivity.class, getApplicationContext());
        ButterKnife.inject(this);

        permissionChecker = new RuntimePermissionChecker(this, savedInstanceState);
        if (getPreferences(MODE_PRIVATE).getBoolean("oneTimePermissionRationale", true)) {
            getPreferences(MODE_PRIVATE).edit().putBoolean("oneTimePermissionRationale", false).apply();
            permissionChecker.setOneTimeRationale(getString(R.string.permission_rationale));
        }
        permissionChecker.registerPermissionRequestCallback(REQUEST_LOCATION_PERMISSION, new RuntimePermissionChecker.PermissionRequestCallback() {
            @Override
            public void onPermissionRequestResult(int requestCode, String[] permissions, String[] denied) {
                if (denied == null) {
                    startScan();
                    if (assetTrackingEnabled)
                        startAssetTracking();
                }
            }
        });
        permissionChecker.registerPermissionRequestCallback(REQUEST_STORAGE_PERMISSION, new RuntimePermissionChecker.PermissionRequestCallback() {
            @Override
            public void onPermissionRequestResult(int requestCode, String[] permissions, String[] denied) {
                if (denied == null)
                    IotSensorsLogger.configure(true);
            }
        });

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        handler = new Handler();
        scanTimer = new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        };
        bluetoothDeviceList = new ArrayList<>();
        deviceList = new ArrayList<>();
        bluetoothScanAdapter = new ScanAdapter(this, R.layout.scan_item_row, deviceList);
        deviceListView.setAdapter(bluetoothScanAdapter);

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                stopScan();
                if (!connecting && (application.device == null || application.device.state == IotSensorsDevice.DISCONNECTED)) {
                    connecting = true;
                    Utils.showProgress(MainActivity.this, mViewContainer, mProgressView, true);
                    mBluetoothDevice = bluetoothDeviceList.get(position);
                    application.device = new IotSensorsDevice(mBluetoothDevice, deviceList.get(position).deviceType, MainActivity.this);
                    application.device.connect();
                    if (application.device.cloudSupport()) {
                        CloudSettingsManager.init(MainActivity.this, application.device.address);
                        preferences.edit().putBoolean("showCloudMenuOnScanScreen", true).apply();
                    }
                }
            }
        });

        connectionStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                int connectionState = intent.getIntExtra("state", 0);
                connectionStateChanged(connectionState);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(connectionStateReceiver,
                new IntentFilter(BroadcastUpdate.CONNECTION_STATE_UPDATE));

        sensorDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                switch (intent.getIntExtra("status", -1)) {
                    case StatusUpdates.STATUS_SERVICE_NOT_FOUND:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.service_not_found_title)
                                .setMessage(R.string.service_not_found_msg)
                                .setPositiveButton(android.R.string.ok, null)
                                .show();
                        break;
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(sensorDataReceiver,
                new IntentFilter(BroadcastUpdate.SENSOR_DATA_UPDATE));

        cloudConfigurationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ConfigurationMsg msg = intent.getParcelableExtra("state");
                if (msg == null)
                    return;
                Log.d(TAG, "Configuration message: " + Arrays.toString(msg.StopFw));

                assetTrackingEnabled = true;
                for (int event : msg.StopFw) {
                    if (event == eEventTypes.Advertise) {
                        assetTrackingEnabled = false;
                        break;
                    }
                }
                if (assetTrackingEnabled)
                    startAssetTracking();
                else
                    stopAssetTracking();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(cloudConfigurationReceiver,
                new IntentFilter(BroadcastUpdate.IOT_APPS_SETTINGS_UPDATE));

        bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            stopAssetTracking();
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (assetTrackingEnabled)
                                startAssetTracking();
                            break;
                    }
                }
            }
        };
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && cm !=null) {
            cm.registerDefaultNetworkCallback(networkCallback);
        } else {
            networkConnectivityReceiver = new NetworkConnectivityReceiver();
            registerReceiver(networkConnectivityReceiver, new IntentFilter(NetworkConnectivityReceiver.ACTION));
        }

        initBluetooth();
        startScan();
        DataManager.InitMqtt(this);
        IotSensorsLogger.configure(permissionChecker.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.storage_permission_rationale, REQUEST_STORAGE_PERMISSION));

        assetTrackingEnabled = preferences.getBoolean(getString(R.string.pref_switch_cloud_assettracking_key), false);
        if (assetTrackingEnabled)
            startAssetTracking();

        showCloudMenu = preferences.getBoolean("showCloudMenuOnScanScreen", false);

    }


    public void clickSetBt(View view){
        if(case_id.getText().toString().isEmpty()){
            Toast.makeText(this, "case id를 입력해주세요", Toast.LENGTH_SHORT).show();
        }
        else{
            SharedPreferences sharedPreferences = getSharedPreferences("case_id",MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("case_id", Integer.parseInt(case_id.getText().toString()));
            editor.commit();
            Toast.makeText(this, "case_id 저장",Toast.LENGTH_SHORT).show();
        }
//        Log.d("보여줘욧","prefGyroscopeRate: "+ gyroRate);
//        gyroRate = pref.getString("prefGyroscopeRate", null);
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        String gyroRate = sharedPreferences.getString("prefGyroscopeRate", null);

//        SharedPreferences sharedPreferences = getSharedPreferences("prefGyroscopeRate", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
    }



    @Override
    protected void onResume() {
        super.onResume();
        if (!connecting)
            Utils.showProgress(this, mViewContainer, mProgressView, false);
    }

    @Override
    public void onBackPressed() {
        if (connecting) {
            cancelConnection();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startScan();
            }
        }
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

    @Override
    public void onDestroy() {
        stopScan();
        stopAssetTracking();
        cancelConnection();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionStateReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sensorDataReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(cloudConfigurationReceiver);
        unregisterReceiver(bluetoothStateReceiver);
        DataManager.TearDown(this);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
            unregisterReceiver(networkConnectivityReceiver);
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem menuItemScan = menu.findItem(R.id.menu_scan);
        menuItemScan.setTitle(isScanning ? R.string.menu_stop_scan : R.string.menu_scan);
        if (showCloudMenu) {
            menu.findItem(R.id.menu_cloud).setVisible(true);
            menu.findItem(R.id.menu_cloud_settings).setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int cloudFragment = -1;
        switch (item.getItemId()) {
            case R.id.menu_scan:
                if (isScanning) {
                    stopScan();
                } else {
                    cancelConnection();
                    startScan();
                }
                return true;

            case R.id.menu_info:
                Intent intent = new Intent(this, InfoActivity.class);
                startActivity(intent);
                return true;

            case R.id.menu_cloud:
                if (CloudSettingsManager.getUserID(this).isEmpty()) {
                    Toast.makeText(this, R.string.connect_to_get_id, Toast.LENGTH_LONG).show();
                    return true;
                }
                cloudFragment = CloudActivity.FRAGMENT_CLOUD;
                break;

            case R.id.menu_cloud_settings:
                cloudFragment = CloudActivity.FRAGMENT_CLOUD_SETTINGS;
                break;
        }

        if (cloudFragment != -1) {
            cancelConnection();
            Intent intent = new Intent(this, CloudActivity.class);
            intent.putExtra("fragment", cloudFragment);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            String name = device.getName();
            if (name != null && !name.isEmpty()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processAdvertisingData(device, scanRecord, rssi);
                    }
                });
            }
        }
    };

    /**
     * Start scanning for bluetooth devices. Stops scanning after 10 sec.
     */
    public void startScan() {
        if (mBluetoothAdapter.isEnabled()) {
            if (!permissionChecker.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.location_permission_rationale, REQUEST_LOCATION_PERMISSION))
                return;
            if (!checkLocationServices())
                return;
            Log.d(TAG, "Start scanning");
            isScanning = true;
            bluetoothDeviceList.clear();
            deviceList.clear();
            bluetoothScanAdapter.clear();
            bluetoothScanAdapter.notifyDataSetChanged();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            handler.postDelayed(scanTimer, 10000);
            invalidateOptionsMenu();
        } else {
            Log.e(TAG, "Bluetooth not enabled.");
        }
    }

    private boolean checkLocationServices() {
        if (Build.VERSION.SDK_INT < 23 || locationServicesSkipCheck)
            return true;
        // Check if location services are required by reading the setting from Bluetooth app.
        if (locationServicesRequired == null) {
            locationServicesRequired = true;
            try {
                Resources res = getPackageManager().getResourcesForApplication("com.android.bluetooth");
                int id = res.getIdentifier("strict_location_check", "bool", "com.android.bluetooth");
                locationServicesRequired = res.getBoolean(id);
            } catch (PackageManager.NameNotFoundException | Resources.NotFoundException e) {
                Log.e(TAG, "Failed to read location services requirement setting", e);
            }
            Log.d(TAG, "Location services requirement setting: " + locationServicesRequired);
        }
        if (!locationServicesRequired)
            return true;
        // Check location services setting. Prompt the user to enable them.
        if (Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF) != Settings.Secure.LOCATION_MODE_OFF)
            return true;
        Log.d(TAG, "Location services disabled");
        new AlertDialog.Builder(this)
                .setTitle(R.string.no_location_services_title)
                .setMessage(R.string.no_location_services_msg)
                .setPositiveButton(R.string.enable_location_services, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.no_location_services_scan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationServicesSkipCheck = true;
                        startScan();
                    }
                })
                .show();
        return false;
    }

    private void processAdvertisingData(BluetoothDevice device, byte[] scanRecord, int rssi) {
        if (bluetoothDeviceList.contains(device))
            return;

        if (!showCloudMenu && isAsset(device, scanRecord)) {
            showCloudMenu = true;
            invalidateOptionsMenu();
        }

        List<UUID> uuids = UUIDS.getAdvertisedServices(scanRecord);
        if (!uuids.contains(UUIDS.WEARABLES_SERVICE_580) && !uuids.contains(UUIDS.WEARABLES_SERVICE_680))
            return;
        Log.d(TAG, "WEARABLES_SERVICE found on device " + device.getName() + " " + device.getAddress());
        bluetoothDeviceList.add(device);

        String manufacturerSpecificData = "< UNKNOWN >";
        byte[] data = UUIDS.getManufacturerSpecificData(scanRecord, 0xd2);
        if (data != null)
            manufacturerSpecificData = " <" + IotSensorsLogger.getLogStringFromBytes(data.length == 3 ? data : data.length == 4 ? Arrays.copyOfRange(data, 0, 3) : Arrays.copyOfRange(data, 3, 6)).trim() + ">";

        String deviceName = device.getName();
        int deviceType = IotDeviceSpec.getDeviceTypeFromAdvName(deviceName);
        if (data != null && data.length > 6 && (data[6] & 0xff) <= IotSensorsDevice.TYPE_MAX)
            deviceType = data[6] & 0xff;
        int scanIcon = IotDeviceSpec.getDeviceIcon(deviceType);
        boolean rawProject = deviceType == IotSensorsDevice.TYPE_IOT_580 && IotDeviceSpec.isRawProjectFromAdvName(deviceName);
        String mode = rawProject ? "RAW" : "SFL";
        deviceName = IotDeviceSpec.getProperNameFromAdvName(deviceName);
        String desc = "Software: " + mode + "\nBDA: " + manufacturerSpecificData;

        deviceList.add(new ScanItem(scanIcon, deviceName, desc, rssi, deviceType));
        Log.d(TAG, "deviceName: " + deviceName + " manufacturerSpecificData" + manufacturerSpecificData + " mode: " + mode);
        bluetoothScanAdapter.notifyDataSetChanged();
    }

    /**
     * Stop scanning for bluetooth devices.
     */
    public void stopScan() {
        if (isScanning) {
            isScanning = false;
            handler.removeCallbacks(scanTimer);
            Log.d(TAG, "Stop scanning");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            invalidateOptionsMenu();
        }
    }

    /**
     * Check if Bluetooth is supported and enabled.
     */
    private void initBluetooth() {
        // Initialize Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e(TAG, "Bluetooth not supported.");
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Bluetooth is not supported on this device")
                    .show();
        }

        // If the bluetooth adapter is not enabled, request to enable it
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void connectionStateChanged(int connectionState) {
        Log.d(TAG, "connectionStateChanged: " + connectionState);
        boolean isConnecting = connecting;
        connecting = false;

        String connectionStateString = "";
        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
            connectionStateString = "disconnected";
            Utils.showProgress(this, mViewContainer, mProgressView, false);
            application.device.close();
        } else if (isConnecting && connectionState == BluetoothProfile.STATE_CONNECTED) {
            application.device.state = IotSensorsDevice.CONNECTED;
            connectionStateString = "connected";
            Intent intent = new Intent(this, MenuHolderActivity.class);
            switch (application.device.type) {
                case IotSensorsDevice.TYPE_IOT_580:
                case IotSensorsDevice.TYPE_WEARABLE:
                case IotSensorsDevice.TYPE_IOT_585:
                    startActivity(intent);
                    break;
                default:
                    Log.e(TAG, "Unknown device type in connectionStateChanged() Application can't continue");
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("This device is not supported")
                            .show();
                    application.device.disconnect();
                    Utils.showProgress(this, mViewContainer, mProgressView, false);
                    break;
            }
        }
        if (!connectionStateString.isEmpty()) {
            String message = mBluetoothDevice.getName() + " " + connectionStateString;
            Log.d(TAG, message);
            application.logger.info(mBluetoothDevice.getName() + " [" + mBluetoothDevice.getAddress() + "] " + connectionStateString);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void cancelConnection() {
        if (!connecting)
            return;
        connecting = false;
        application.device.disconnect();
        application.device.close();
        Utils.showProgress(this, mViewContainer, mProgressView, false);
    }

    private ConnectivityManager.NetworkCallback networkCallback = Build.VERSION.SDK_INT < Build.VERSION_CODES.N ? null : new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network){
            DataManager.InitMqtt(IotSensorsApplication.getApplication().getApplicationContext());
        }
        @Override
        public void onLost(Network network){
            Utils.showToast(IotSensorsApplication.getApplication().getApplicationContext(), R.string.connectivity_error);
            DataManager.StopMqtt(IotSensorsApplication.getApplication().getApplicationContext());
        }
    };

    // Background scanning for asset tracking
    private static final boolean BACKGROUND_SCAN_FULL = true;
    private static final int BACKGROUND_SCAN_INTERVAL = 1000;
    private static final int BACKGROUND_SCAN_DUTY_CYCLE = 20;
    private static final int BACKGROUND_SCAN_RESTART_INTERVAL = 60000;

    private boolean assetTrackingEnabled;
    private boolean assetTracking;
    private boolean backgroundScan;
    private boolean useNewScannerApi = Build.VERSION.SDK_INT >= 21;
    private ConcurrentHashMap<String, Integer> assets = new ConcurrentHashMap<>(10);

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startAssetTracking() {
        if (assetTracking)
            return;
        assetTracking = true;
        Log.d(TAG, "Start asset tracking");

        assets.clear();
        if (!mBluetoothAdapter.isEnabled() || !permissionChecker.permissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            assetTracking = false;
            return;
        }

        // On Android 7, a scan timeout was added inside the system Bluetooth app, so we need to restart it periodically.
        if (Build.VERSION.SDK_INT >= 24 && (useNewScannerApi || BACKGROUND_SCAN_FULL))
            handler.postDelayed(backgroundScanRestartTimer, BACKGROUND_SCAN_RESTART_INTERVAL);

        if (!useNewScannerApi) {
            if (BACKGROUND_SCAN_FULL) {
                mBluetoothAdapter.startLeScan(backgroundScanCallback);
                handler.postDelayed(backgroundScanTimer, BACKGROUND_SCAN_INTERVAL);
            } else {
                backgroundScan = false;
                backgroundScanTimer.run();
            }
        } else {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (scanner == null)
                return;
            ScanSettings settings = new ScanSettings.Builder().setScanMode(BACKGROUND_SCAN_FULL ? ScanSettings.SCAN_MODE_LOW_LATENCY : ScanSettings.SCAN_MODE_LOW_POWER).build();
            scanner.startScan(null, settings, backgroundScanCallbackNewApi);
            handler.postDelayed(backgroundScanTimer, BACKGROUND_SCAN_INTERVAL);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopAssetTracking() {
        if (!assetTracking)
            return;
        assetTracking = false;
        Log.d(TAG, "Stop asset tracking");

        handler.removeCallbacks(backgroundScanTimer);
        handler.removeCallbacks(backgroundScanRestartTimer);
        backgroundScan = false;
        assets.clear();
        if (!mBluetoothAdapter.isEnabled())
            return;

        if (!useNewScannerApi) {
            mBluetoothAdapter.stopLeScan(backgroundScanCallback);
        } else {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            if (scanner == null)
                return;
            scanner.stopScan(backgroundScanCallbackNewApi);
        }
    }

    private Runnable backgroundScanTimer = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(backgroundScanTimer);

            if (useNewScannerApi || BACKGROUND_SCAN_FULL) {
                handler.postDelayed(this, BACKGROUND_SCAN_INTERVAL);
                sendAssetsToCloud();
                return;
            }

            if (!backgroundScan) {
                backgroundScan = true;
                assets.clear();
                mBluetoothAdapter.startLeScan(backgroundScanCallback);
                handler.postDelayed(this, BACKGROUND_SCAN_INTERVAL * BACKGROUND_SCAN_DUTY_CYCLE / 100);
            } else {
                backgroundScan = false;
                mBluetoothAdapter.stopLeScan(backgroundScanCallback);
                handler.postDelayed(this, BACKGROUND_SCAN_INTERVAL - (BACKGROUND_SCAN_INTERVAL * BACKGROUND_SCAN_DUTY_CYCLE / 100));
                sendAssetsToCloud();
            }
        }
    };

    private Runnable backgroundScanRestartTimer = new Runnable() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            Log.d(TAG, "Asset tracking: Restart scan");
            handler.postDelayed(this, BACKGROUND_SCAN_RESTART_INTERVAL);
            if (!useNewScannerApi) {
                mBluetoothAdapter.stopLeScan(backgroundScanCallback);
                mBluetoothAdapter.startLeScan(backgroundScanCallback);
            } else {
                BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
                if (scanner == null)
                    return;
                ScanSettings settings = new ScanSettings.Builder().setScanMode(BACKGROUND_SCAN_FULL ? ScanSettings.SCAN_MODE_LOW_LATENCY : ScanSettings.SCAN_MODE_LOW_POWER).build();
                scanner.stopScan(backgroundScanCallbackNewApi);
                scanner.startScan(null, settings, backgroundScanCallbackNewApi);
            }
        }
    };

    private void sendAssetsToCloud() {
        Log.d(TAG, "Asset tracking: Found " + assets.size() + " devices");
        for (String ekid : assets.keySet()) {
            DataMsg msg = new DataMsg(application.device != null ? application.device.address : null);
            msg.Events.add(new DataEvent(eEventTypes.Advertise, String.format(Locale.US, "%s %d", ekid, assets.get(ekid))));
            DataManager.sendDataMsg(this, msg);
        }
        assets.clear();
    }

    private boolean isAsset(BluetoothDevice device, byte[] advData) {
        // Check for IoT+ asset
        byte[] data = UUIDS.getManufacturerSpecificData(advData, 0xd2); // Dialog ID
        if (data != null && data.length != 23) {
            return data.length > 6 && data[6] == IotSensorsDevice.TYPE_IOT_585;
        }

        // Check for iBeacon asset
        if (data == null)
            data = UUIDS.getManufacturerSpecificData(advData, 0x4c); // Apple ID
        if (data != null) {
            if (data.length != 23)
                return false;
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
            // Check subtype/length/UUID
            return buffer.get() == 2 && buffer.get() == 21 && UUIDS.IBEACON_ASSET_UUID.equals(new UUID(buffer.getLong(), buffer.getLong()));
        }

        return false;
    }

    private BluetoothAdapter.LeScanCallback backgroundScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (isAsset(device, scanRecord))
                assets.put(device.getAddress(), rssi);
       }
    };

    private ScanCallback backgroundScanCallbackNewApi = !useNewScannerApi ? null : new ScanCallback() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (isAsset(result.getDevice(), result.getScanRecord().getBytes()))
                assets.put(result.getDevice().getAddress(), result.getRssi());
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                if (isAsset(result.getDevice(), result.getScanRecord().getBytes()))
                    assets.put(result.getDevice().getAddress(), result.getRssi());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Failed to start scan with new API (error=" + errorCode + "). Revert to old API.");
            useNewScannerApi = false;
            assetTracking = false;
            startAssetTracking();
        }
    };
}
