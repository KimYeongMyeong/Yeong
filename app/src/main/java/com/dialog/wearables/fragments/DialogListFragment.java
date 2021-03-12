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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dialog.wearables.R;
import com.dialog.wearables.settings.CalibrationSettingsManager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class DialogListFragment extends DialogFragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "DialogListFragment";
    private ListView fileList;
    private File[] files;
    private CalibrationSettingsManager calibrationSettingsManager;

    public DialogListFragment() {
        File f = new File(Environment.getExternalStorageDirectory().toString() + File.separator + "Dialog Semiconductor" + File.separator + "IoT Sensors" + File.separator);
        files = f.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.toLowerCase().endsWith(".ini");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getPath().compareToIgnoreCase(o2.getPath());
            }
        });
    }

    public void passCalibrationSettingsManager(CalibrationSettingsManager manager) {
        calibrationSettingsManager = manager;
    }

    private ArrayList<String> getFileList() {
        ArrayList<String> filenames = new ArrayList<>();
        for (File file : files) {
            String[] path = String.valueOf(file).split(File.separator);
            filenames.add(path[path.length - 1]);
        }
        return filenames;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_list, null, false);
        fileList = (ListView) view.findViewById(R.id.dialog_list);
        fileList.setEmptyView(view.findViewById(R.id.empty_list));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getFileList());
        fileList.setAdapter(adapter);
        fileList.setOnItemClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.file_select)
                .setView(view);
        return builder.create();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        dismiss();
        Log.d(TAG, String.valueOf(files[position]));
        calibrationSettingsManager.onFileSelection(files[position]);
    }
}
