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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.dialog.wearables.defines.BroadcastUpdate;
import com.dialog.wearables.global.Utils;
import com.yodiwo.amazonbasedavsclientlibrary.AudioRequestBody;
import com.yodiwo.amazonbasedavsclientlibrary.activity.BaseFragment;
import com.yodiwo.amazonbasedavsclientlibrary.activity.User;

import java.io.IOException;

import ee.ioc.phon.android.speechutils.RawAudioRecorder;
import okio.BufferedSink;


public class AVSFragment extends BaseFragment {
    private static final int AUDIO_RATE = 16000;
    private RawAudioRecorder recorder;

    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private static boolean isVisible = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isVisible) {
            requestAudioPermissions();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            isVisible = true;
            requestAudioPermissions();
        } else {
            isVisible = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //tear down our recorder on stop
        if(recorder != null){
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //tear down our recorder on stop
        if(recorder != null){
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override
    protected void stateUserUpdated(User user) {

    }

    @Override
    protected void stateUserAuthorized() {

    }

    @Override
    protected void stateUserUnauthorized() {

    }

    @Override
    protected void stateErroring(Exception error) {

    }

    @Override
    protected void stateAuthErroring(Exception error) {

    }

    @Override
    protected void stateDownchannelClosed() {

    }

    @Override
    protected void stateDownchannelOpened() {

    }

    @Override
    protected void stateListening() {

    }

    @Override
    protected void stateProcessing() {

    }

    @Override
    protected void stateSpeaking() {

    }

    @Override
    protected void stateFinished() {

    }

    @Override
    protected void statePrompting() {

    }

    @Override
    protected void stateNone() {

    }

    public void startListening() {
        if(recorder == null){
            recorder = new RawAudioRecorder(AUDIO_RATE);
        }
        recorder.start();
        sendAudio(requestBody);
    }

    private AudioRequestBody requestBody = new AudioRequestBody() {
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            while (recorder != null && !recorder.isPausing()) {
                if(recorder != null) {
                    if(sink != null && recorder != null) {
                        sink.write(recorder.consumeRecording());
                    }
                }
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            stopListening();
        }

    };

    public void stopListening(){
        if(recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    private void requestAudioPermissions() {
        if (getActivity() != null) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {

                //When permission is not granted by user, show them message why this permission is needed.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.RECORD_AUDIO)) {
                    Utils.showToast(getActivity(), "Please grant permissions to record audio");

                    //Give user option to still opt-in the permissions
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_RECORD_AUDIO);

                } else {
                    // Show user dialog to grant permission to record audio
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_RECORD_AUDIO);
                }
            }
            //If permission is granted, then go ahead recording audio
            else if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }
    //Handling callback
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Utils.showToast(getActivity(), "Permissions Denied to record audio");
                }
                return;
            }
        }
    }
}