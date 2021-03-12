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
import com.dialog.wearables.apis.cloud.rest.ControlGetRulesRsp;
import com.dialog.wearables.apis.cloud.rest.ControlRule;
import com.dialog.wearables.apis.cloud.rest.ControlSetRuleReq;
import com.dialog.wearables.apis.cloud.rest.DeviceInfo;
import com.dialog.wearables.apis.cloud.rest.GenericRsp;
import com.dialog.wearables.apis.cloud.rest.MgmtGetEKIDRsp;
import com.dialog.wearables.apis.cloud.rest.eControlActuatorValues;
import com.dialog.wearables.apis.cloud.rest.eControlActuators;
import com.dialog.wearables.apis.cloud.rest.eComparisonOperators;
import com.dialog.wearables.apis.cloud.rest.eControlCloudRuleConditions;
import com.dialog.wearables.apis.cloud.rest.eControlCloudRuleSubConditionForex;
import com.dialog.wearables.apis.cloud.rest.eControlCloudRuleSubConditionsWeather;
import com.dialog.wearables.apis.cloud.rest.eControlSetRuleOperationTypes;
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

public class ControlRulesFragment extends Fragment {

    // =============================================================================================
    // Variables
    // =============================================================================================

    private static final String TAG = ControlRulesFragment.class.getSimpleName();

    private Spinner controlOption, devices;
    private EditText ruleName, city, value;
    private Button activeControlRules;
    private Button buttonCity;
    private Button buttonUnits;

    private ArrayAdapter adapter;
    private List<String> devicesEntriesForSpinner = new ArrayList<>();

    private ArrayAdapter adapterSubCondition;
    private List<String> subConditionsWeatherEntriesForSpinner = Arrays.asList("Temperature");
    private List<String> subConditionsForexEntriesForSpinner = Arrays.asList("EURUSD", "USDJPY",
            "GBPUSD", "USDCHF", "EURGBP", "EURJPY", "EURCHF", "AUDUSD", "USDCAD", "NZDUSD");
    private String selectedEKID;
    private String selectedOperator;
    private String selectedCondition;
    private String selectedSubConditionWeather;
    private String selectedSubConditionForex;
    private String selectedActuatorType;
    private String selectedActuatorValue;

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
            getCloudRules();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_control_rules, container, false);

        // views
        Button sync = (Button) rootView.findViewById(R.id.button_sync_control_rules);
        Button apply = (Button) rootView.findViewById(R.id.button_control_rule_apply);
        Spinner conditions = (Spinner) rootView.findViewById(R.id.conditions_spinner);
        Spinner operators = (Spinner) rootView.findViewById(R.id.comparison_control_operators_spinner);
        Spinner actuatorTypes = (Spinner) rootView.findViewById(R.id.spinner_control_rule_actuator_types);
        Spinner actuatorValues = (Spinner) rootView.findViewById(R.id.spinner_control_rule_actuator_values);
        devices = (Spinner) rootView.findViewById(R.id.control_devices_spinner);
        ruleName = (EditText) rootView.findViewById(R.id.control_rule_name);
        controlOption = (Spinner) rootView.findViewById(R.id.control_rule_option);
        city = (EditText) rootView.findViewById(R.id.control_rule_city);
        value = (EditText) rootView.findViewById(R.id.control_rule_condition_value);
        activeControlRules = (Button) rootView.findViewById(R.id.button_active_control_rules);
        buttonCity = (Button) rootView.findViewById(R.id.button_tag_control_rule_city);
        buttonUnits = (Button) rootView.findViewById(R.id.button_units);

        // init device spinner
        setDeviceSpinnerAdapter();
        setSubConditionSpinnerAdapter();
        // listeners
        conditions.setOnItemSelectedListener(new ConditionsSpinnerListener());
        controlOption.setOnItemSelectedListener(new SubConditionsSpinnerListener());
        sync.setOnClickListener(new SyncCloudRulesButtonOnClickListener());
        apply.setOnClickListener(new ApplyCloudRuleButtonOnClickListener());
        operators.setOnItemSelectedListener(new OperatorSpinnerListener());
        actuatorTypes.setOnItemSelectedListener(new ActuatorTypeSpinnerListener());
        actuatorValues.setOnItemSelectedListener(new ActuatorValueSpinnerListener());
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

    // ---------------------------------------------------------------------------------------------

    private void setSubConditionSpinnerAdapter() {
        List<String> options = ( selectedCondition == null || selectedCondition.equals("Weather") ?
                subConditionsWeatherEntriesForSpinner :
                subConditionsForexEntriesForSpinner);
        adapterSubCondition = new ArrayAdapter<>(getContextAsync(), R.layout.custom_spinner_item, options);
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    controlOption.setAdapter(adapterSubCondition);
                    setSubConditionSpinnerLatestState(); // set user's latest selection
                }
            });
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void setSubConditionSpinnerLatestState() {
        int pos = (selectedCondition == null || selectedCondition.equals("Weather") ?
                (selectedSubConditionWeather != null ? eControlCloudRuleSubConditionsWeather.NameToSubConditionWeatherTypeMap.get(selectedSubConditionWeather) : 0) :
                (selectedSubConditionForex != null ? eControlCloudRuleSubConditionForex.NameToSubConditionForexTypeMap.get(selectedSubConditionForex) : 0)
        );
        if (pos >= 0) {
            controlOption.setSelection(pos);
        }
    }

    // =============================================================================================
    // Helpers - UI UPDATES
    // =============================================================================================

    private void setAsyncButtonText(final Button button, final String text) {
        if (getActivity() != null && button != null && text != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    button.setText(text);
                }
            });
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

    private void getCloudRules() {
        if (!CloudSettingsManager.getUserID(getContextAsync()).isEmpty()) {
            DataMessenger.getInstance().getControlRules(new RestApi.ResponseListener() {
                @Override
                public void start() {
                    Log.i(TAG, "Request started");
                }

                @Override
                public void success(HttpResponse rsp) {
                    if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                        String txt = "0 Active Control Rules";
                        ControlGetRulesRsp controlGetRulesRsp = new Gson().fromJson(rsp.body, ControlGetRulesRsp.class);
                        if (controlGetRulesRsp != null && controlGetRulesRsp.getNumOfActiveCloudRules() != 0) {
                            txt = controlGetRulesRsp.getNumOfActiveCloudRules() + " Active Control Rules";
                        } else {
                            Utils.showToast(getContextAsync(), R.string.get_control_rules_empty);
                        }
                        setAsyncButtonText(activeControlRules, txt);
                    } else {
                        if (rsp != null && rsp.body != null) {
                            GenericRsp genericRsp = new Gson().fromJson(rsp.body, GenericRsp.class);
                            if (genericRsp != null) {
                                if (genericRsp.getReasonCode() != null && !genericRsp.getReasonCode().isEmpty())
                                    Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                            } else {
                                Utils.showToast(getContextAsync(), R.string.get_control_rules_error);
                            }
                        } else {
                            Utils.showToast(getContextAsync(), R.string.get_control_rules_error);
                        }
                    }
                }

                @Override
                public void failure(Exception error) {
                    if (error instanceof UnknownHostException) {
                        Utils.showToast(getContextAsync(), R.string.connectivity_error);
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_control_rules_error);
                    }
                }

                @Override
                public void complete() {
                    Log.i(TAG, "Request completed");
                }
            });
        } else {
            Utils.showToast(getContextAsync(), R.string.get_control_rules_empty_userid);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void postCloudRule(ControlSetRuleReq controlSetRuleReq) {
        DataMessenger.getInstance().postControlRule(controlSetRuleReq, new RestApi.ResponseListener() {
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
                            Utils.showToast(getContextAsync(), R.string.post_control_rule_success);
                        } else {
                            Utils.showToast(getContextAsync(), genericRsp.getReasonCode());
                        }
                    } else {
                        Utils.showToast(getContextAsync(), R.string.post_control_rule_error);
                    }
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_control_rule_error);
                }
            }

            @Override
            public void failure(Exception error) {
                if (error instanceof UnknownHostException) {
                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                } else {
                    Utils.showToast(getContextAsync(), R.string.post_control_rule_error);
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

    private class SyncCloudRulesButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            getCloudRules();
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class ApplyCloudRuleButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            ControlRule controlRule = createControlRule(v.getContext());

            if (controlRule.isValid()) {
                ControlSetRuleReq controlSetRuleReq = new ControlSetRuleReq(
                        CloudSettingsManager.getAppId(v.getContext()),
                        eControlSetRuleOperationTypes.Insert,
                        controlRule
                );
                postCloudRule(controlSetRuleReq);
            } else {
                Utils.showToast(getContextAsync(), R.string.post_control_rule_not_valid_request);
            }
        }

        private ControlRule createControlRule(Context context) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String currentDateTime = sdf.format(new Date());

            ControlRule controlRule = new ControlRule(CloudSettingsManager.getUserID(context), selectedEKID != null ? selectedEKID.split(",")[0].trim() : "");
            controlRule.setName(ruleName.getText().toString().trim());
            controlRule.setCondition(eControlCloudRuleConditions.NameToConditionTypeMap.get(selectedCondition));
            controlRule.setSubCondition(selectedCondition.equals("Weather") ? selectedSubConditionWeather : selectedSubConditionForex);
            controlRule.setCity(city.getText().toString().trim());
            controlRule.setOperatorType(eComparisonOperators.ComparisonSymbolToTypeMap.get(selectedOperator.trim()));
            controlRule.setValue(Float.parseFloat(!value.getText().toString().trim().isEmpty() ? value.getText().toString().trim() : "0.0"));
            controlRule.setActuatorType(eControlActuators.NameToActuatorTypeMap.get(selectedActuatorType));
            controlRule.setActuatorValue(eControlActuatorValues.NameToActuatorStateMap.get(selectedActuatorValue));
            controlRule.setLastUpdated(currentDateTime);
            controlRule.setEnabled(true);
            controlRule.createFriendlyDescription();

            return controlRule;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class ConditionsSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            selectedCondition = parentView.getSelectedItem().toString();
            if (position == 0) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSubConditionSpinnerAdapter();
                        controlOption.setFocusable(false);
                        controlOption.setFocusableInTouchMode(false);
                        controlOption.setClickable(false);
                        buttonCity.setVisibility(View.VISIBLE);
                        buttonUnits.setVisibility(View.VISIBLE);
                        city.setVisibility(View.VISIBLE);
                        city.setText("");
                    }
                });
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setSubConditionSpinnerAdapter();
                        controlOption.setFocusable(true);
                        controlOption.setFocusableInTouchMode(true);
                        controlOption.setClickable(true);
                        buttonCity.setVisibility(View.GONE);
                        buttonUnits.setVisibility(View.GONE);
                        city.setVisibility(View.GONE);
                        city.setText("");
                    }
                });
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parentView) {
            // your code here
        }
    }

    private class SubConditionsSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            if (selectedCondition.equals("Weather")) {
                selectedSubConditionWeather = parentView.getSelectedItem().toString();
            } else {
                selectedSubConditionForex = parentView.getSelectedItem().toString();
            }
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

    // ---------------------------------------------------------------------------------------------

    private class ActuatorTypeSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedActuatorType = parent.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    // ---------------------------------------------------------------------------------------------

    private class ActuatorValueSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedActuatorValue = parent.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
