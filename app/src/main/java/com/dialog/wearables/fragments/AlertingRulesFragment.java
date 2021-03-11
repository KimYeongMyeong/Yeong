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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.apis.cloud.rest.AlertingGetRulesRsp;
import com.dialog.wearables.apis.cloud.rest.AlertingRule;
import com.dialog.wearables.apis.cloud.rest.AlertingSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.DeviceInfo;
import com.dialog.wearables.apis.cloud.rest.GenericRsp;
import com.dialog.wearables.apis.cloud.rest.MgmtGetEKIDRsp;
import com.dialog.wearables.apis.cloud.rest.eAlertingSetRuleOperationTypes;
import com.dialog.wearables.apis.cloud.rest.eComparisonOperators;
import com.dialog.wearables.apis.cloud.rest.eAlertingSensorTypes;
import com.dialog.wearables.cloud.DataMessenger;
import com.dialog.wearables.cloud.rest.HttpResponse;
import com.dialog.wearables.cloud.rest.RestApi;
import com.dialog.wearables.global.Utils;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class AlertingRulesFragment extends Fragment {

    // =============================================================================================
    // Variables
    // =============================================================================================

    private static final String TAG = AlertingRulesFragment.class.getSimpleName();

    private Spinner devices;
    private EditText value, email, ruleName;
    private Button activeRules, tagContent;

    private List<String> measurement_units = Arrays.asList("ºC", "%", "Pa", "-", "lux");
    private List<String> devicesEntriesForSpinner = new ArrayList<>();

    private ArrayAdapter adapter;

    private String selectedEKID;
    private String selectedSensor;
    private String selectedOperator;

    // =============================================================================================
    // Overrides
    // =============================================================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getEKIDs();
            getRules();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_alerting_rules, container, false);

        // views
        Button sync = (Button) rootView.findViewById(R.id.button_sync_rule);
        Button apply = (Button) rootView.findViewById(R.id.button_apply_rule);
        Spinner sensors = (Spinner) rootView.findViewById(R.id.sensors_spinner);
        Spinner operators = (Spinner) rootView.findViewById(R.id.comparison_operators_spinner);
        devices = (Spinner) rootView.findViewById(R.id.devices_spinner);
        value = (EditText) rootView.findViewById(R.id.rule_value);
        email = (EditText) rootView.findViewById(R.id.rule_email);
        ruleName = (EditText) rootView.findViewById(R.id.rule_name);
        activeRules = (Button) rootView.findViewById(R.id.button_active_rules);
        tagContent = (Button) rootView.findViewById(R.id.button__tag_content);

        // init device spinner
        setDeviceSpinnerAdapter();

        // listeners
        sync.setOnClickListener(new SyncRulesButtonOnClickListener());
        apply.setOnClickListener(new ApplyRuleButtonOnClickListener());
        sensors.setOnItemSelectedListener(new SensorSpinnerListener());
        operators.setOnItemSelectedListener(new OperatorSpinnerListener());
        devices.setOnItemSelectedListener(new DeviceSpinnerListener());

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // =============================================================================================
    // Helpers - CONTEXT
    // =============================================================================================

    private Context getContextAsync() {
        return getActivity() == null ? IotSensorsApplication.getApplication().getApplicationContext() : getActivity();
    }

    // =============================================================================================
    // Helpers - DEVICE SPINNER
    // =============================================================================================

    // ---------------------------------------------------------------------------------------------

    private void updateAdapter(List<DeviceInfo> data) {
        ArrayList<String> devices = new ArrayList<>();
        for (DeviceInfo device : data) {
            devices.add(device.getEKID() +
                    ((device.getFriendlyName() != null && !device.getFriendlyName().isEmpty()) ? ", " + device.getFriendlyName() : ""));
        }
        try {
            devicesEntriesForSpinner.clear();
            devicesEntriesForSpinner.addAll(devices);
            setDeviceSpinnerAdapter();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void setDeviceSpinnerAdapter() {
        adapter = new ArrayAdapter<>(getContextAsync(), R.layout.custom_spinner_item, devicesEntriesForSpinner);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    devices.setAdapter(adapter);
                    setDeviceSpinnerLatestState(); // set user's latest selection
                }
            });
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void setDeviceSpinnerLatestState() {
        if (selectedEKID != null) {
            int pos = devicesEntriesForSpinner.indexOf(selectedEKID);
            if (pos >= 0) {
                devices.setSelection(pos);
            }
        }
    }

    // =============================================================================================
    // Helpers - UI UPDATES
    // =============================================================================================

    private void setAsyncButtonText(final Button button, final String text) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setText(text);
                }
            });
        }
    }

    // =============================================================================================
    // Helpers - CLOUD
    // =============================================================================================

    // ---------------------------------------------------------------------------------------------

    private void getEKIDs() {
        if (!CloudSettingsManager.getUserID(getContextAsync()).isEmpty()) {
            DataMessenger.getInstance().getEkIds(CloudSettingsManager.getUserID(getContextAsync()), new RestApi.ResponseListener() {
                @Override
                public void start() {
                    Log.i(TAG, "Request started");
                }

                @Override
                public void success(HttpResponse rsp) {
                    if (rsp != null) {
                        if (rsp.statusCode == 200) {
                            if (rsp.body != null) {
                                MgmtGetEKIDRsp result = new Gson().fromJson(rsp.body, MgmtGetEKIDRsp.class);
                                if (result.getDevices().size() == 0) {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_empty);
                                } else {
                                    updateAdapter(result.getDevices());
                                }
                            } else {
                                Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                            }
                        } else {
                            GenericRsp genericRsp = new Gson().fromJson(rsp.body, GenericRsp.class);
                            if (genericRsp != null && !genericRsp.isResult()) {
                                Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                            } else {
                                Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                            }
                        }
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                    }
                }

                @Override
                public void failure(Exception error) {
                    if (error instanceof UnknownHostException) {
                        Utils.showToast(getContextAsync(), R.string.connectivity_error);
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_error);
                    }
                }

                @Override
                public void complete() {
                    Log.i(TAG, "Request completed");
                }
            });
        } else {
            Utils.showToast(getContextAsync(), R.string.get_cloud_ekids_empty_userid);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void getRules() {
        if (!CloudSettingsManager.getUserID(getContextAsync()).isEmpty()) {
            DataMessenger.getInstance().getRules(new RestApi.ResponseListener() {
                @Override
                public void start() {
                    Log.i(TAG, "Request started");
                }

                @Override
                public void success(HttpResponse rsp) {
                    if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                        String txt = "0 Active Rules";
                        AlertingGetRulesRsp alertingGetRulesRsp = new Gson().fromJson(rsp.body, AlertingGetRulesRsp.class);
                        if (alertingGetRulesRsp != null && alertingGetRulesRsp.getNumOfActiveRules() != 0) {
                            txt = alertingGetRulesRsp.getNumOfActiveRules() + " Active Rules";
                        } else {
                            Utils.showToast(getContextAsync(), R.string.get_rules_empty);
                        }
                        setAsyncButtonText(activeRules, txt);
                    } else {
                        if (rsp != null && rsp.body != null) {
                            GenericRsp genericRsp = new Gson().fromJson(rsp.body, GenericRsp.class);
                            if (genericRsp != null && !genericRsp.isResult()) {
                                Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                            } else {
                                Utils.showToast(getContextAsync(), R.string.get_rules_error);
                            }
                        } else {
                            Utils.showToast(getContextAsync(), R.string.get_rules_error);
                        }
                    }
                }

                @Override
                public void failure(Exception error) {
                    if (error instanceof UnknownHostException) {
                        Utils.showToast(getContextAsync(), R.string.connectivity_error);
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_rules_error);
                    }
                }

                @Override
                public void complete() {
                    Log.i(TAG, "Request completed");
                }
            });
        } else {
            Utils.showToast(getContextAsync(), R.string.get_rules_empty_userid);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void postRule(AlertingSetRuleReq alertingSetRuleReq) {
        DataMessenger.getInstance().postRule(alertingSetRuleReq, new RestApi.ResponseListener() {
            @Override
            public void start() {
                Log.i(TAG, "Request started");
            }

            @Override
            public void success(HttpResponse rsp) {
                if (rsp != null && rsp.body != null) {
                    GenericRsp genericRsp = new Gson().fromJson(rsp.body, GenericRsp.class);
                    if (genericRsp != null) {
                        if (genericRsp.isResult()) {
                            Utils.showToast(getContextAsync(), R.string.post_rule_success);
                        } else {
                            Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                        }
                    } else {
                        Utils.showToast(getContextAsync(), R.string.post_rule_error);
                    }
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_rule_error);
                }
            }

            @Override
            public void failure(Exception error) {
                if (error instanceof UnknownHostException) {
                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_rule_error);
                }
            }

            @Override
            public void complete() {
                Log.i(TAG, "Request completed");
            }
        });
    }

    // =============================================================================================
    // UI Listeners
    // =============================================================================================

    // ---------------------------------------------------------------------------------------------

    private class SyncRulesButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getRules();
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class ApplyRuleButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            AlertingRule alertingRule = createAlertingRule(v.getContext());

            if (alertingRule.IsValid()) {
                AlertingSetRuleReq alertingSetRuleReq = new AlertingSetRuleReq(
                        CloudSettingsManager.getAppId(v.getContext()),
                        eAlertingSetRuleOperationTypes.Insert,
                        alertingRule
                );
                postRule(alertingSetRuleReq);
            } else {
                Utils.showToast(getContextAsync(), R.string.post_rule_not_valid_request);
            }
        }

        private AlertingRule createAlertingRule(Context context) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String currentDateTime = sdf.format(new Date());

            AlertingRule alertingRule = new AlertingRule(CloudSettingsManager.getUserID(context), selectedEKID != null ? selectedEKID.split(",")[0].trim() : "");
            alertingRule.setName(ruleName.getText().toString());
            alertingRule.setSensorType(eAlertingSensorTypes.AlertingSensorNameToTypeMap.get(selectedSensor.trim()));
            alertingRule.setOperatorType(eComparisonOperators.ComparisonSymbolToTypeMap.get(selectedOperator.trim()));
            alertingRule.setValue(Float.parseFloat(!value.getText().toString().trim().isEmpty() ? value.getText().toString().trim() : "0.0"));
            alertingRule.setEmail(email.getText().toString());
            alertingRule.setLastUpdated(currentDateTime);
            alertingRule.setEnabled(true);
            alertingRule.createFriendlyDescription();

            return alertingRule;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class SensorSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            selectedSensor = parentView.getSelectedItem().toString();
            setAsyncButtonText(tagContent, measurement_units.get(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class DeviceSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedEKID = parent.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    // ---------------------------------------------------------------------------------------------

    private class OperatorSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedOperator = parent.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
