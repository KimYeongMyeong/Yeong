/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.controller;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.dialog.wearables.GlobalVar;
import com.dialog.wearables.R;
import com.dialog.wearables.device.IotSensorsDevice;
import com.dialog.wearables.global.Object3DLoader;
import com.dialog.wearables.sensor.Gyroscope;
import com.dialog.wearables.sensor.GyroscopeAngleIntegration;
import com.dialog.wearables.sensor.GyroscopeQuaternionIntegration;
import com.dialog.wearables.sensor.IotSensor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import min3d.core.Object3dContainer;
import min3d.vos.Light;
import min3d.vos.LightType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GyroscopeController extends ThreeDimensionController {
    private static final int[] lineColors = {Color.BLACK, Color.BLACK, Color.BLACK};

    //    keti custom
    private TextView gyroscopeParamTv;
    private Activity activity;
    private String qStr;
    private String eulerStr;
    private long oldTimestamp = 0;
    private float hz;
    private int cnt = 0;
    private ArrayList deltaLastX = new ArrayList();

    public static final float GRAPH_HEIGHT = 360.0f;

    public static class GraphValueProcessor implements IotSensor.GraphValueProcessor {
        @Override
        public IotSensor.Value process(IotSensor.Value v) {
            return new IotSensor.Value3D(v.getX() + GRAPH_HEIGHT / 2, v.getY() + GRAPH_HEIGHT / 2, v.getZ() + GRAPH_HEIGHT / 2);
        }
    }

    private boolean loadCancelled = false;
    private ObjectLoader objectLoader;

    public GyroscopeController(IotSensorsDevice device, Fragment fragment) {
        super(device, device.getGyroscope(), fragment, R.id.gyroscopeView, R.id.gyroscopeChart, R.id.gyroscopeShape, lineColors);
        // keti custom
        gyroscopeParamTv = fragment.getView().findViewById(R.id.gyroscopeParamTv);
        activity = fragment.getActivity();
        graphDataSize = device.gyroscopeGraphData.X.capacity();
    }

    @Override
    protected void generateObject() {
        if (Object3DLoader.gyro == null) {
            getInitSceneHandler().post(new Runnable() {
                @Override
                public void run() {
                    objectLoader = new ObjectLoader();
                    objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            });
        } else {
            object3D = Object3DLoader.gyro;
        }
    }

    @Override
    public void startInterval() {
        if (loadCancelled) {
            loadCancelled = false;
            objectLoader = new ObjectLoader();
            objectLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        super.startInterval();
    }

    @Override
    public void stopInterval() {
        if (objectLoader != null && !object3DConfigured) {
            objectLoader.cancel(true);
            loadCancelled = true;
        }
        super.stopInterval();
    }

    private class ObjectLoader extends AsyncTask<Void, Void, Object3dContainer> {

        @Override
        protected Object3dContainer doInBackground(Void... nothing) {
            synchronized (Object3DLoader.sync) {
                while (Object3DLoader.gyro == null && !isCancelled())
                    Object3DLoader.waitForObject();
            }
            return Object3DLoader.gyro;
        }

        @Override
        protected void onProgressUpdate(Void... progress) {
        }

        @Override
        protected void onPostExecute(Object3dContainer result) {
            object3D = result;
            initScene();
        }
    }

    @Override
    protected void addLights() {
        Log.d(TAG, "addLights");
        Light lightBlue;
        lightBlue = new Light();
        lightBlue.ambient.setAll(0xff66b9ed);
        lightBlue.diffuse.setAll(0x993079ba);
        lightBlue.type(LightType.POSITIONAL); // looks nicer, especially with multiple lights interacting with each other
        scene.lights().add(lightBlue);
        scene.lights().add(lightBlue);
    }

    @Override
    public void updateScene() {
        if (!object3DConfigured || !sensor.validValue())
            return;

        IotSensor.Value value = !device.integrationEngine ? ((Gyroscope) sensor).getAccumulatedRotation() : ((GyroscopeAngleIntegration) sensor).getAccumulatedRotation();
//        todo quaterion을 받으려면 조건을 확인해야 함. 현재는 euler를 사용함
//        IotSensor.Value value2 = !device.integrationEngine ? ((Gyroscope)sensor).getAccumulatedRotation() : ((GyroscopeQuaternionIntegration))
        cnt += 1;
        long timestamp = System.currentTimeMillis();
        if (oldTimestamp != 0) {
            hz = 1000 / ((float) (timestamp - oldTimestamp));
        }
        oldTimestamp = timestamp;

            cnt = 0;
            GlobalVar.gyroEuler.setVar(value);
//            eulerStr = String.format("%.2fhz, (roll, pitch, yaw):\n (%.2f, %.2f, %.2f)"
//                    , hz, value.getRoll(), value.getPitch(), value.getYaw());
            //        gyroscopeParamTv.setText(qStr);
            //        Log.i("test", qStr);
            //        Log.i("test2", "roll: " + value.getRoll() + ", pitch: " + value.getPitch() + ", yaw: " + value.getYaw());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gyroscopeParamTv.setText(GlobalVar.gyroEuler.toString());
                }
            });

        object3D.rotation().x = value.getX();
        object3D.rotation().y = value.getY();
        object3D.rotation().z = value.getZ();
    }

    @Override
    protected void updateUI() {
        super.updateUI();

        final Viewport v = new Viewport();
        v.bottom = 0.0f;
        v.top = GRAPH_HEIGHT;
        v.left = lastX - graphDataSize;
        v.right = lastX;

        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
    }

    String tmpTAG = "Gry 3D";
    int oldLastX = 0;

    /*ym*/
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




    /*ym*/
    @Override
    public List<List<PointValue>> getGraphData3D() {
        List<List<PointValue>> list = new ArrayList<>(3);
        lastX = device.gyroscopeGraphData.getList(list);
        List<PointValue> listZero = list.get(0);
        ArrayList eulerDataX = new ArrayList();
        ArrayList eulerDataY = new ArrayList();
        ArrayList eulerDataZ = new ArrayList();
        int deltaLastX;
        List<PointValue> eulerX = null;
        List<PointValue> eulerY = null;
        List<PointValue> eulerZ = null;



        // todo 아래 if 문은 항상 true 이므로 변경이 필요함
        if (0 != oldLastX) {
            deltaLastX = lastX - oldLastX;
            for (int i = 0; i < 3; ++i) {
                List<PointValue> itAxis = list.get(i);
                List<PointValue> axisArr = new ArrayList<>();
                for (int j = itAxis.size() - deltaLastX; j < itAxis.size(); ++j) {
                    PointValue tmpPointValue = itAxis.get(j);
//                    Log.d(tmpTAG, "tmpPointValue: " + tmpPointValue.toString());
                    axisArr.add(tmpPointValue);
                }
//                for (PointValue itAxisPoint : itAxis) {
//                    axisArr.add(itAxisPoint);
//                }
                switch (i) {
                    case 0:
                        eulerX = axisArr;
                        break;
                    case 1:
                        eulerY = axisArr;
                        break;
                    case 2:
                        eulerZ = axisArr;
                        break;
                }
                ArrayList eulerData = new ArrayList();
                eulerDataX.add(eulerX);

                eulerDataY.add(eulerY);
                eulerDataZ.add(eulerZ);

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                String datetimes = dateFormat.format(calendar.getTime());
                Log.d(tmpTAG, "eulerDataX: "+ eulerDataX);
                try{
                    JSONObject jsonData = new JSONObject();
                    jsonData.put("dev_id", 1);
                    jsonData.put("case_id", 0);
                    jsonData.put("datetimes", datetimes);
                    jsonData.put("roll", eulerDataX);
                    jsonData.put("pitch", eulerDataY);
                    jsonData.put("yaw", eulerDataZ);
                    JSONArray jsonDataArr = new JSONArray();
                    jsonDataArr.put(jsonData);
                    JSONObject pushingJsonObj = new JSONObject();
                    pushingJsonObj.put("cmd", "push");
                    pushingJsonObj.put("data", jsonDataArr);
                    sendHttpJson(pushingJsonObj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                if (null != eulerZ) {
//                    Log.d(tmpTAG, "inner eulerZ.size: " + eulerZ.size() + ", eulerZ: " + eulerZ.toString());
//                }


            }
            // todo 전송도 이 안에서 실행


        }
        if (null != eulerZ) {
            Log.d(tmpTAG, "lastX: " + lastX + ", eulerZ.size: " + eulerZ.size() + ", eulerZ: " + eulerZ.toString());
        }
        oldLastX = lastX;
        /*ym customizing data holding part*/
        //deltaLastX = list(lastX) - list(oldLastX);
        /*ym customizing data holding part*/


        //Log.d(tmpTAG, "lastx: " + lastX + ", itList.size(): " + itList.size());
        //Log.d(tmpTAG, "list_values: " + list);

/*        deltaLastX = lastX - oldLastX;
        List list_tmp = list.subList(list.size()-1-deltaLastX, list.size()-1);
        Log.d(tmpTAG, "list_tmp: "+list_tmp);*/
        //oldLastX = lastX;


//        int num = 0;
//        List<PointValue> gyro_data = new ArrayList<>();
//        for (PointValue itList : listZero) {
//            gyro_data.add(itList);
//            Log.d(tmpTAG, "gyro_data.size: " + gyro_data.size() + ", itList: " + itList.toString());
//        }
//        for (PointValue ititList : itList) {
//            Log.d(tmpTAG, "for inner");
        //Log.d(tmpTAG, "lastX: "+ lastX+", values: "+ ititList.toString());
//        deltaLastX = lastX - oldLastX;

//            List<List<PointValue>> gyro_data = list.subList(lastX-1-deltaLastX, lastX-1);
//        Log.d(tmpTAG, "list_size: " + list.size());

//        Log.d(tmpTAG, "deltaLastX: " + deltaLastX);

//            Log.d(tmpTAG, "list_tmp: "+gyro_data);


//            if (num > itList.size() - 20) {
//                Log.d(tmpTAG, "PointValue : " + num + " : " + ititList.toString());
//            }
//            ++num;
//        }
//        ArrayList oldLastX = new ArrayList();
//        List<String> deltaLastX = lastX - oldLastX;
//        oldLastX = lastX;



        /*lastX = device.gyroscopeGraphData.getProcessedList(list, new PointValueBuffer3D.DataProcessor() {
            @Override
            public PointValue process(int dim, PointValue p) {
                return new PointValue(p.getX(), p.getY() + GRAPH_HEIGHT / 2);
            }
        });*/
        return list ;
    }



}
