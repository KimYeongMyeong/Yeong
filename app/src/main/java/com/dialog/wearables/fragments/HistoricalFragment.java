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

import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.dialog.wearables.IotSensorsApplication;
import com.dialog.wearables.R;
import com.dialog.wearables.apis.cloud.rest.DeviceInfo;
import com.dialog.wearables.apis.cloud.rest.GenericRsp;
import com.dialog.wearables.apis.cloud.rest.HistoricalGetEnvironmentalReq;
import com.dialog.wearables.apis.cloud.rest.HistoricalGetEnvironmentalRsp;
import com.dialog.wearables.apis.cloud.rest.MgmtGetEKIDRsp;
import com.dialog.wearables.cloud.DataMessenger;
import com.dialog.wearables.cloud.rest.HttpResponse;
import com.dialog.wearables.cloud.rest.RestApi;
import com.dialog.wearables.global.Utils;
import com.dialog.wearables.settings.CloudSettingsManager;
import com.google.gson.Gson;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;


public class HistoricalFragment extends Fragment {

    // =============================================================================================
    // Variables
    // =============================================================================================

    private static final String TAG = HistoricalFragment.class.getSimpleName();

    private Spinner devices;
    private EditText startDate, endDate;
    private DatePickerDialog datePickerDialog;
    private LineChartView chart;

    private ArrayAdapter adapter;

    private List<String> devicesEntriesForSpinner = new ArrayList<>();
    private List<AxisValue> xAxisValues = new ArrayList<>();
    private HistoricalGetEnvironmentalRsp sensorData;

    private int numberOfLines = 1, numberOfPoints = 0;
    private boolean hasAxes = true, hasAxesNames = true, hasXAxisValues = true;
    private boolean hasLines = true, hasPoints = true, isFilled = true;
    private boolean hasLabels = false, isCubic = false, hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor = false;
    private ValueShape shape = ValueShape.CIRCLE;

    private String selectedEKID;
    private String selectedSensor;
    private String latestYName;
    private HistoricalGetEnvironmentalRsp latestHistoricalGetEnvironmentalRsp;

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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_historical, container, false);

        // get current date
        final Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        // views
        Button apply = (Button) rootView.findViewById(R.id.button_hdata_apply);
        Spinner sensors = (Spinner) rootView.findViewById(R.id.hdata_sensors_spinner);
        devices = (Spinner) rootView.findViewById(R.id.hdata_devices_spinner);
        startDate = (EditText) rootView.findViewById(R.id.from);
        endDate = (EditText) rootView.findViewById(R.id.to);
        chart = (LineChartView) rootView.findViewById(R.id.historicalChart);

        // init device spinner
        setDeviceSpinnerAdapter();

        // init date picker
        setAsyncEditText(startDate, mDay + "-" + (mMonth + 1) + "-" + mYear);
        setAsyncEditText(endDate, mDay + "-" + (mMonth + 1) + "-" + mYear);

        // init chart
        chart.setViewportCalculationEnabled(false); // Disable viewport recalculations, see toggleCubic() method for more info.
        //chart.setScrollContainer(true);
        latestChart();

        // listeners
        apply.setOnClickListener(new ApplyButtonOnClickListener());
        startDate.setOnClickListener(new StartDateClickListener(mYear, mMonth, mDay));
        endDate.setOnClickListener(new EndDateClickListener(mYear, mMonth, mDay));
        chart.setOnValueTouchListener(new ValueTouchListener());
        devices.setOnItemSelectedListener(new DeviceSpinnerListener());
        sensors.setOnItemSelectedListener(new SensorSpinnerListener());

        return rootView;
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
    // Helpers - CHART
    // =============================================================================================

    private void latestChart() {
        if (latestYName != null && latestHistoricalGetEnvironmentalRsp != null) {
            updateChart(latestHistoricalGetEnvironmentalRsp, latestYName);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void resetViewport(int minY, int maxY) {
        // Reset viewport height range to (minY - 10, maxY + 10)
        final Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = minY - 10;
        v.top = maxY + 10;
        v.left = 0;
        v.right = (numberOfPoints > 1) ? numberOfPoints - 1 : 10;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    // ---------------------------------------------------------------------------------------------

    private void updateChart(HistoricalGetEnvironmentalRsp historicalGetEnvironmentalRsp, String yName) {
        latestYName = yName;
        latestHistoricalGetEnvironmentalRsp = historicalGetEnvironmentalRsp;
        List<Line> lines = new ArrayList<>();
        ArrayList<Integer> sensorValues = new ArrayList<>();
        if (historicalGetEnvironmentalRsp != null) {
            sensorData = new HistoricalGetEnvironmentalRsp(
                    historicalGetEnvironmentalRsp.getValues(),
                    historicalGetEnvironmentalRsp.getTimestamps()
            );
            numberOfPoints = (historicalGetEnvironmentalRsp.getValues() != null) ? historicalGetEnvironmentalRsp.getValues().size() : 0;
        }

        // create line
        if (numberOfPoints != 0) {
            for (int i = 0; i < numberOfLines; ++i) {
                List<PointValue> values = new ArrayList<>();
                for (int j = 0; j < numberOfPoints; ++j) {
                    values.add(new PointValue(j, historicalGetEnvironmentalRsp.getValues().get(j)));
                    sensorValues.add(historicalGetEnvironmentalRsp.getValues().get(j).intValue());
                }
                lines.add(createLine(values, ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.length]));
            }
        }

        chart.setLineChartData(createLineChartData(lines, yName, (sensorValues.size() != 0) ? String.valueOf(Collections.max(sensorValues)).length() : 0));
        if (sensorValues.size() != 0) {
            resetViewport(Collections.min(sensorValues), Collections.max(sensorValues));
        } else {
            resetViewport(0, 0);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Line createLine(List<PointValue> values, int color) {
        Line line = new Line(values);
        line.setColor(ChartUtils.COLORS[4]);
        line.setShape(shape);
        line.setCubic(isCubic);
        line.setFilled(isFilled);
        line.setHasLabels(hasLabels);
        line.setHasLabelsOnlyForSelected(hasLabelForSelected);
        line.setHasLines(hasLines);
        line.setHasPoints(hasPoints);

        if (pointsHaveDifferentColor) {
            line.setPointColor(color);
        }
        return line;
    }

    // ---------------------------------------------------------------------------------------------

    private LineChartData createLineChartData(List<Line> lines, String yName, int maxLabelChars) {
        LineChartData data = new LineChartData(lines);
        if (numberOfPoints != 0) {
            if (hasAxes) {
                Axis axisX = new Axis();
                Axis axisY = new Axis().setHasLines(true);
                axisY.setMaxLabelChars(maxLabelChars);
                if (hasAxesNames) {
                    axisX.setName("");
                    axisY.setName(yName);
                }
                if (hasXAxisValues) {

                    for (int i = 0; i < numberOfPoints; i++) {
                        xAxisValues.add(new AxisValue(i, "".toCharArray()));
                    }
                    axisX.setValues(xAxisValues);
                }
                data.setAxisXBottom(axisX);
                data.setAxisYLeft(axisY);
            } else {
                data.setAxisXBottom(null);
                data.setAxisYLeft(null);
            }

            data.setBaseValue(Float.NEGATIVE_INFINITY);
        }

        return data;
    }

    // ---------------------------------------------------------------------------------------------

    private void resetChart() {
        updateChart(new HistoricalGetEnvironmentalRsp(), "");
    }

    // =============================================================================================
    // Helpers - UI UPDATES
    // =============================================================================================

    private void setAsyncEditText(final EditText editText, final String text) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    editText.setText(text);
                }
            });
    }

    // =============================================================================================
    // Helpers - CLOUD
    // =============================================================================================

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

    // =============================================================================================
    // UI listeners
    // =============================================================================================

    // ---------------------------------------------------------------------------------------------

    private class ValueTouchListener implements LineChartOnValueSelectListener {

        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            Utils.showToast(getContextAsync(),
                    "Value: " + value.getY() +
                            "\nTimestamp: " +
                            ((sensorData != null && sensorData.getValues().size() != 0) ?
                                    sensorData.getTimestampsInLocalTime().get((int) value.getX()) : ""));
        }

        @Override
        public void onValueDeselected() {
            //
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class ApplyButtonOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            HistoricalGetEnvironmentalReq historicalGetEnvironmentalReq = new HistoricalGetEnvironmentalReq(
                    startDate.getText().toString(),
                    endDate.getText().toString(),
                    selectedEKID != null ? selectedEKID.split(",")[0].trim() : "",
                    CloudSettingsManager.getAppId(v.getContext()),
                    CloudSettingsManager.getUserID(v.getContext()));

            switch (selectedSensor) {
                case "Temperature":
                    if (historicalGetEnvironmentalReq.IsReqValid()) {
                        DataMessenger.getInstance().getTemperatureData(historicalGetEnvironmentalReq, new RestApi.ResponseListener() {
                            @Override
                            public void start() {
                                Log.i(TAG, "Request started");
                            }

                            @Override
                            public void success(HttpResponse rsp) {
                                if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                                    HistoricalGetEnvironmentalRsp result = new Gson().fromJson(rsp.body, HistoricalGetEnvironmentalRsp.class);

                                    if (result != null && result.getValues().size() != 0) {
                                        updateChart(result, "Temperature ºC");
                                    } else {
                                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_empty);
                                        resetChart();
                                    }
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                    resetChart();
                                }
                            }

                            @Override
                            public void failure(Exception error) {
                                if (error instanceof UnknownHostException) {
                                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                }
                                resetChart();
                            }

                            @Override
                            public void complete() {
                                Log.i(TAG, "Request completed");
                            }
                        });
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_not_valid_request);
                        resetChart();
                    }

                    break;
                case "Humidity":
                    if (historicalGetEnvironmentalReq.IsReqValid()) {
                        DataMessenger.getInstance().getHumidityData(historicalGetEnvironmentalReq, new RestApi.ResponseListener() {
                            @Override
                            public void start() {
                                Log.i(TAG, "Request started");
                            }

                            @Override
                            public void success(HttpResponse rsp) {
                                if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                                    HistoricalGetEnvironmentalRsp result = new Gson().fromJson(rsp.body, HistoricalGetEnvironmentalRsp.class);

                                    if (result != null && result.getValues().size() != 0) {
                                        updateChart(result, "Humidity %");
                                    } else {
                                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_empty);
                                        resetChart();
                                    }
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                    resetChart();
                                }
                            }

                            @Override
                            public void failure(Exception error) {
                                if (error instanceof UnknownHostException) {
                                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                }
                                resetChart();
                            }

                            @Override
                            public void complete() {
                                Log.i(TAG, "Request completed");
                            }
                        });
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_not_valid_request);
                        resetChart();
                    }
                    break;
                case "Pressure":
                    if (historicalGetEnvironmentalReq.IsReqValid()) {
                        DataMessenger.getInstance().getPressureData(historicalGetEnvironmentalReq, new RestApi.ResponseListener() {
                            @Override
                            public void start() {
                                Log.i(TAG, "Request started");
                            }

                            @Override
                            public void success(HttpResponse rsp) {
                                if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                                    HistoricalGetEnvironmentalRsp result = new Gson().fromJson(rsp.body, HistoricalGetEnvironmentalRsp.class);

                                    if (result != null && result.getValues().size() != 0) {
                                        updateChart(result, "Pressure Pa");
                                    } else {
                                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_empty);
                                        resetChart();
                                    }
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                    resetChart();
                                }
                            }

                            @Override
                            public void failure(Exception error) {
                                if (error instanceof UnknownHostException) {
                                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                }
                                resetChart();
                            }

                            @Override
                            public void complete() {
                                Log.i(TAG, "Request completed");
                            }
                        });
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_not_valid_request);
                        resetChart();
                    }
                    break;
                case "AirQuality":
                    if (historicalGetEnvironmentalReq.IsReqValid()) {
                        DataMessenger.getInstance().getAirQualityData(historicalGetEnvironmentalReq, new RestApi.ResponseListener() {
                            @Override
                            public void start() {
                                Log.i(TAG, "Request started");
                            }

                            @Override
                            public void success(HttpResponse rsp) {
                                if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                                    HistoricalGetEnvironmentalRsp result = new Gson().fromJson(rsp.body, HistoricalGetEnvironmentalRsp.class);

                                    if (result != null && result.getValues().size() != 0) {
                                        updateChart(result, "AirQuality");
                                    } else {
                                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_empty);
                                        resetChart();
                                    }
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                    resetChart();
                                }
                            }

                            @Override
                            public void failure(Exception error) {
                                if (error instanceof UnknownHostException) {
                                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                }
                                resetChart();
                            }

                            @Override
                            public void complete() {
                                Log.i(TAG, "Request completed");
                            }
                        });
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_not_valid_request);
                        resetChart();
                    }

                    break;
                case "Brightness":
                    if (historicalGetEnvironmentalReq.IsReqValid()) {
                        DataMessenger.getInstance().getBrightnessData(historicalGetEnvironmentalReq, new RestApi.ResponseListener() {
                            @Override
                            public void start() {
                                Log.i(TAG, "Request started");
                            }

                            @Override
                            public void success(HttpResponse rsp) {
                                if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                                    HistoricalGetEnvironmentalRsp result = new Gson().fromJson(rsp.body, HistoricalGetEnvironmentalRsp.class);

                                    if (result != null && result.getValues().size() != 0) {
                                        updateChart(result, "Brightness lux");
                                    } else {
                                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_empty);
                                        resetChart();
                                    }
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                    resetChart();
                                }
                            }

                            @Override
                            public void failure(Exception error) {
                                if (error instanceof UnknownHostException) {
                                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                }
                                resetChart();
                            }

                            @Override
                            public void complete() {
                                Log.i(TAG, "Request completed");
                            }
                        });
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_not_valid_request);
                        resetChart();
                    }
                    break;
                case "Proximity":
                    if (historicalGetEnvironmentalReq.IsReqValid()) {
                        DataMessenger.getInstance().getProximityData(historicalGetEnvironmentalReq, new RestApi.ResponseListener() {
                            @Override
                            public void start() {
                                Log.i(TAG, "Request started");
                            }

                            @Override
                            public void success(HttpResponse rsp) {
                                if (rsp != null && rsp.body != null && rsp.statusCode == 200) {
                                    HistoricalGetEnvironmentalRsp result = new Gson().fromJson(rsp.body, HistoricalGetEnvironmentalRsp.class);

                                    if (result != null && result.getValues().size() != 0) {
                                        updateChart(result, "Proximity");
                                    } else {
                                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_empty);
                                        resetChart();
                                    }
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                    resetChart();
                                }
                            }

                            @Override
                            public void failure(Exception error) {
                                if (error instanceof UnknownHostException) {
                                    Utils.showToast(getContextAsync(), R.string.connectivity_error);
                                } else {
                                    Utils.showToast(getContextAsync(), R.string.get_cloud_data_error);
                                }
                                resetChart();
                            }

                            @Override
                            public void complete() {
                                Log.i(TAG, "Request completed");
                            }
                        });
                    } else {
                        Utils.showToast(getContextAsync(), R.string.get_cloud_data_not_valid_request);
                        resetChart();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class StartDateClickListener implements View.OnClickListener {
        private int mYear, mMonth, mDay;

        StartDateClickListener(int mYear, int mMonth, int mDay) {
            this.mYear = mYear;
            this.mMonth = mMonth;
            this.mDay = mDay;
        }

        @Override
        public void onClick(View v) {

            datePickerDialog = new DatePickerDialog(getContextAsync(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            setAsyncEditText(startDate, dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    }

    // ---------------------------------------------------------------------------------------------

    private class EndDateClickListener implements View.OnClickListener {
        private int mYear, mMonth, mDay;

        EndDateClickListener(int mYear, int mMonth, int mDay) {
            this.mYear = mYear;
            this.mMonth = mMonth;
            this.mDay = mDay;
        }

        @Override
        public void onClick(View v) {
            datePickerDialog = new DatePickerDialog(getContextAsync(),
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            setAsyncEditText(endDate, dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
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

    private class SensorSpinnerListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedSensor = parent.getSelectedItem().toString();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}
