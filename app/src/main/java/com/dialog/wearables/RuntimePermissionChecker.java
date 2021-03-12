/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2017 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class RuntimePermissionChecker {

    private static final boolean noRuntimePermissions = Build.VERSION.SDK_INT < 23;

    public static final int PERMISSION_REQUEST_CODE = 4317357;
    public static final int PERMISSION_REQUEST_DIALOG_CANCELLED = 4317358;

    // Configuration
    private static final boolean LOG = true;
    private static final boolean CHECK_THREAD_ON_PERMISSION_REQUEST = true;
    private static final boolean CHECK_THREAD_ON_PERMISSION_RESULT = false;

    // Log
    public static final String TAG = RuntimePermissionChecker.class.getSimpleName();
    private static void log(String msg) {
        if (LOG)
            Log.d(TAG, msg);
    }

    // Runtime permissions
    public static final HashSet<String> runtimePermissions = new HashSet<>();
    public static final HashSet<String> runtimePermissionGroups = new HashSet<>();
    static {
        runtimePermissions.addAll(Arrays.asList(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.ADD_VOICEMAIL,
                Manifest.permission.USE_SIP,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_WAP_PUSH,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
                ));

        runtimePermissionGroups.addAll(Arrays.asList(
                Manifest.permission_group.CALENDAR,
                Manifest.permission_group.CAMERA,
                Manifest.permission_group.CONTACTS,
                Manifest.permission_group.LOCATION,
                Manifest.permission_group.MICROPHONE,
                Manifest.permission_group.PHONE,
                Manifest.permission_group.SENSORS,
                Manifest.permission_group.SMS,
                Manifest.permission_group.STORAGE,
                Manifest.permission.INTERNET
        ));
    }

    public static boolean isRuntimePermission(String permission) {
        return runtimePermissions.contains(permission);
    }

    public static boolean isRuntimePermissionGroup(String group) {
        return runtimePermissionGroups.contains(group);
    }

    public static boolean isDangerousPermission(Context context, String permission) {
        PackageManager pm = context.getPackageManager();
        PermissionInfo permInfo;
        try {
            permInfo = pm.getPermissionInfo(permission, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return (permInfo.flags & PermissionInfo.PROTECTION_DANGEROUS) != 0;
    }

    public boolean isDangerousPermission(String permission) {
        return isDangerousPermission(activity, permission);
    }

    // Permission request and callback
    public interface PermissionRequestCallback {
        void onPermissionRequestResult(int requestCode, String[] permissions, String[] denied);
    }

    private class RegisteredCallback implements PermissionRequestCallback {

        private int requestCode;

        RegisteredCallback(int requestCode) {
            this.requestCode = requestCode;
        }

        public int getRequestCode() {
            return requestCode;
        }

        @Override
        public void onPermissionRequestResult(int requestCode, String[] permissions, String[] denied) {
            PermissionRequestCallback callback = registeredCallbacks.get(this.requestCode);
            if (callback != null)
                callback.onPermissionRequestResult(this.requestCode, permissions, denied);
        }
    }

    public void registerPermissionRequestCallback(int requestCode, PermissionRequestCallback callback) {
        if (noRuntimePermissions)
            return;
        registeredCallbacks.put(requestCode, callback);
    }

    public void unregisterPermissionRequestCallback(int requestCode) {
        if (noRuntimePermissions)
            return;
        registeredCallbacks.remove(requestCode);
    }

    private static class PermissionRequest implements Parcelable {
        int code;
        String[] permissions;
        String rationale;
        PermissionRequestCallback callback;

        public PermissionRequest(int code, String[] permissions, String rationale, PermissionRequestCallback callback) {
            this.code = code;
            this.permissions = permissions;
            this.rationale = rationale;
            this.callback = callback;
        }

        protected PermissionRequest(Parcel in) {
            code = in.readInt();
            permissions = in.createStringArray();
            rationale = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(code);
            dest.writeStringArray(permissions);
            dest.writeString(rationale);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<PermissionRequest> CREATOR = new Creator<PermissionRequest>() {
            @Override
            public PermissionRequest createFromParcel(Parcel in) {
                return new PermissionRequest(in);
            }

            @Override
            public PermissionRequest[] newArray(int size) {
                return new PermissionRequest[size];
            }
        };
    }

    // Rationale dialog
    public static class RationaleDialog extends DialogFragment {

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onCancel(DialogInterface dialog) {
            log("Permission rationale dialog cancelled");
            getActivity().onRequestPermissionsResult(PERMISSION_REQUEST_DIALOG_CANCELLED, new String[0], new int[0]);
            super.onCancel(dialog);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Bundle args = getArguments();
            String rationale = args.getString("rationale");
            final String[] permissions = args.getStringArray("permissions");
            String title = args.getString("title");
            int iconID = args.getInt("iconID");
            int layoutID = args.getInt("layoutID");
            boolean cancellable = args.getBoolean("cancellable");

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(title != null ? title : "Permission Request");
            if (iconID != 0)
                builder.setIcon(iconID);
            if (layoutID == 0) {
                builder.setMessage(rationale);
            } else {
                View view = getActivity().getLayoutInflater().inflate(layoutID, null);
                TextView rationaleView = (TextView) view.findViewWithTag("rationale");
                rationaleView.setText(rationale);
                builder.setView(view);
            }

            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @TargetApi(23)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Request permissions
                            log("Permission request: " + Arrays.toString(permissions));
                            getActivity().requestPermissions(permissions, RuntimePermissionChecker.PERMISSION_REQUEST_CODE);
                        }
                    });
            setCancelable(cancellable);

            return builder.create();
        }
    }

    public void setRationaleTitle(String rationaleTitle) {
        this.rationaleTitle = rationaleTitle;
    }

    public void setRationaleTitle(int rationaleTitleID) {
        this.rationaleTitle = activity.getString(rationaleTitleID);
    }

    public void setRationaleIcon(int rationaleIconID) {
        this.rationaleIconID = rationaleIconID;
    }

    public void setRationaleView(int rationaleLayoutID) {
        this.rationaleLayoutID = rationaleLayoutID;
    }

    public void setRationaleCancellable(boolean rationaleCancellable) {
        this.rationaleCancellable = rationaleCancellable;
    }

    public void setAlwaysShowRationale(boolean alwaysShowRationale) {
        this.alwaysShowRationale = alwaysShowRationale;
    }

    public void setOneTimeRationale(String oneTimeRationale) {
        this.oneTimeRationale = oneTimeRationale;
    }

    public void resetOneTimeRationale() {
        showedOneTimeRationale = false;
    }

    private void permissionRationaleDialogCancelled() {
        resumePendingPermissionRequest();
    }

    // Data
    private static HashSet<String> grantedPermissions = new HashSet<>();
    private Activity activity;
    private Handler handler;
    private PermissionRequest currentRequest;
    private LinkedList<PermissionRequest> pendingRequests = new LinkedList<>();
    private HashMap<Integer, PermissionRequestCallback> registeredCallbacks = new HashMap<>();
    private String rationaleTitle;
    private int rationaleIconID;
    private int rationaleLayoutID;
    private boolean rationaleCancellable;
    private boolean alwaysShowRationale;
    private String oneTimeRationale;
    private boolean showedOneTimeRationale;

    public RuntimePermissionChecker(Activity activity) {
        this.activity = activity;
        handler = new Handler(Looper.getMainLooper());
    }

    public RuntimePermissionChecker(Activity activity, Bundle state) {
        this(activity);
        restoreState(state);
    }

    // Pending permission requests
    public boolean permissionRequestPending() {
        return currentRequest != null;
    }

    private void resumePendingPermissionRequest() {
        currentRequest = null;
        while (currentRequest == null && !pendingRequests.isEmpty()) {
            log("Resume pending permission request");
            PermissionRequest req = pendingRequests.poll();
            if (checkPermissions(req.permissions, req.rationale, req.callback, req.code) && req.callback != null)
                req.callback.onPermissionRequestResult(req.code, req.permissions, null);
        }
    }

    public void cancelPendingPermissionRequests() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (currentRequest != null)
                    currentRequest.callback = null;
                pendingRequests.clear();
            }
        });
    }

    // Activity lifecycle
    public void saveState(Bundle state) {
        if (noRuntimePermissions)
            return;

        String prefix = getClass().getName() + "#";
        state.putBoolean(prefix + "showedOneTimeRationale", showedOneTimeRationale);
        if (currentRequest != null)
            state.putParcelable(prefix + "currentRequest", currentRequest);
        if (!pendingRequests.isEmpty())
            state.putParcelableArray(prefix + "pendingRequests", pendingRequests.toArray(new PermissionRequest[pendingRequests.size()]));
    }

    public void restoreState(Bundle state) {
        if (noRuntimePermissions)
            return;

        if (state == null)
            return;
        String prefix = getClass().getName() + "#";
        showedOneTimeRationale = state.getBoolean(prefix + "showedOneTimeRationale");

        // WARNING: Unregistered callbacks are lost.

        currentRequest = state.getParcelable(prefix + "currentRequest");
        if (currentRequest != null) {
            currentRequest.callback = currentRequest.code != PERMISSION_REQUEST_CODE ? new RegisteredCallback(currentRequest.code) : null;
            log("Restored current request");
        }

        Parcelable[] pending = state.getParcelableArray(prefix + "pendingRequests");
        if (pending != null) {
            for (Parcelable parcel : pending) {
                PermissionRequest req = (PermissionRequest) parcel;
                req.callback = req.code != PERMISSION_REQUEST_CODE ? new RegisteredCallback(req.code): null;
                pendingRequests.add(req);
            }
            log("Restored pending requests");
        }
    }

    // Check and request permissions
    @TargetApi(23)
    public boolean checkPermissionGranted(String permission) {
        return noRuntimePermissions || activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean permissionGranted(String permission) {
        return grantedPermissions.contains(permission);
    }

    public boolean permissionsGranted(String[] permissions) {
        return grantedPermissions.containsAll(Arrays.asList(permissions));
    }

    public HashSet<String> getGrantedPermissions() {
        return grantedPermissions;
    }

    public boolean checkPermission(String permission, int rationaleResID, int requestCode) {
        return noRuntimePermissions || checkPermissions(new String[] { permission }, activity.getString(rationaleResID), null, requestCode);
    }

    public boolean checkPermission(String permission, String rationale, int requestCode) {
        return noRuntimePermissions || checkPermissions(new String[] { permission }, rationale, null, requestCode);
    }

    public boolean checkPermissions(String[] permissions, int rationaleResID, int requestCode) {
        return noRuntimePermissions || checkPermissions(permissions, activity.getString(rationaleResID), null, requestCode);
    }

    public boolean checkPermissions(String[] permissions, String rationale, int requestCode) {
        return noRuntimePermissions || checkPermissions(permissions, rationale, null, requestCode);
    }

    public boolean checkPermission(String permission, int rationaleResID, PermissionRequestCallback callback) {
        return noRuntimePermissions || checkPermissions(new String[] { permission }, activity.getString(rationaleResID), callback, PERMISSION_REQUEST_CODE);
    }

    public boolean checkPermission(String permission, String rationale, PermissionRequestCallback callback) {
        return noRuntimePermissions || checkPermissions(new String[] { permission }, rationale, callback, PERMISSION_REQUEST_CODE);
    }

    public boolean checkPermissions(String[] permissions, int rationaleResID, PermissionRequestCallback callback) {
        return noRuntimePermissions || checkPermissions(permissions, activity.getString(rationaleResID), callback, PERMISSION_REQUEST_CODE);
    }

    public boolean checkPermissions(String[] permissions, String rationale, PermissionRequestCallback callback) {
        return noRuntimePermissions || checkPermissions(permissions, rationale, callback, PERMISSION_REQUEST_CODE);
    }

    @TargetApi(23)
    public boolean checkPermissions(final String[] permissions, final String rationale, PermissionRequestCallback callback, final int requestCode) {
        if (noRuntimePermissions)
            return true;

        // Ensure permission request runs on the main thread
        if (CHECK_THREAD_ON_PERMISSION_REQUEST && Thread.currentThread() != handler.getLooper().getThread()) {
            log("Permission check not on main thread");
            final PermissionRequestCallback requestCallback = callback;
            final PermissionRequestCallback successCallback = callback != null || requestCode == PERMISSION_REQUEST_CODE ? callback : new RegisteredCallback(requestCode);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    log("Permission check moved to main thread");
                    if (checkPermissions(permissions, rationale, requestCallback, requestCode) && successCallback != null)
                        successCallback.onPermissionRequestResult(requestCode, permissions, null);
                }
            });
            return false;
        }

        log("Check permissions: " + Arrays.toString(permissions));

        // Check if already granted
        boolean grantedAll = true;
        for (String perm : permissions) {
            if (!grantedPermissions.contains(perm)) {
                grantedAll = false;
                break;
            }
        }
        if (grantedAll) {
            log("Permissions already granted");
            return true;
        }

        List<String> permList = new ArrayList<>(Arrays.asList(permissions));

        // Remove granted permissions
        for (String perm : permissions) {
            if (grantedPermissions.contains(perm) || activity.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED) {
                log("Permission granted: " + perm);
                grantedPermissions.add(perm);
                permList.remove(perm);
            }
         }

        // Recheck if already granted
        if (permList.isEmpty()) {
            log("Permissions already granted");
            return true;
        }

        // Check for pending request
        if (callback == null && requestCode != PERMISSION_REQUEST_CODE)
            callback = new RegisteredCallback(requestCode);
        if (currentRequest != null) {
            log("Permission request pending");
            pendingRequests.add(new PermissionRequest(requestCode, permissions, rationale, callback));
            return false;
        }
        currentRequest = new PermissionRequest(requestCode, permissions, rationale, callback);

        String[] permArray = permList.toArray(new String[permList.size()]);

        // Show rationale
        boolean showOnce = !showedOneTimeRationale && oneTimeRationale != null;
        if (rationale != null || showOnce) {
            for (String perm : permList) {
                boolean show = rationale != null && (alwaysShowRationale || activity.shouldShowRequestPermissionRationale(perm));
                if (show || showOnce) {
                    showedOneTimeRationale = true;
                    log("Showing permission rationale for: " + perm);
                    RationaleDialog dialog = new RationaleDialog();
                    Bundle args = new Bundle(6);
                    args.putString("rationale", show ? rationale : oneTimeRationale);
                    args.putStringArray("permissions", permArray);
                    args.putString("title", rationaleTitle);
                    args.putInt("iconID", rationaleIconID);
                    args.putInt("layoutID", rationaleLayoutID);
                    args.putBoolean("cancellable", rationaleCancellable);
                    dialog.setArguments(args);
                    dialog.show(activity.getFragmentManager(), "permission rationale dialog");
                    return false;
                }
            }
        }

        // Request permissions
        log("Permission request: " + permList);
        activity.requestPermissions(permArray, PERMISSION_REQUEST_CODE);
        return false;
    }

    public boolean onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE && requestCode != PERMISSION_REQUEST_DIALOG_CANCELLED)
            return false;

        // Ensure we are on the main thread (probably not needed)
        if (CHECK_THREAD_ON_PERMISSION_RESULT && Thread.currentThread() != handler.getLooper().getThread()) {
            log("Permission request result not on main thread");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    log("Permission request result moved to main thread");
                    onRequestPermissionsResult(requestCode, permissions, grantResults);
                }
            });
            return true;
        }

        if (requestCode == PERMISSION_REQUEST_DIALOG_CANCELLED) {
            permissionRationaleDialogCancelled();
            return true;
        }

        // Check request results
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; ++i) {
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            log("Permission " + (granted ? "granted: " : "denied: ") + permissions[i]);
            if (granted)
                grantedPermissions.add(permissions[i]);
            else
                denied.add(permissions[i]);
        }

        if (currentRequest.callback != null)
            currentRequest.callback.onPermissionRequestResult(currentRequest.code, currentRequest.permissions, denied.isEmpty() ? null : denied.toArray(new String[denied.size()]));
        resumePendingPermissionRequest();
        return true;
    }

    // Request all runtime permissions for app
    private List<String> getAllRuntimePermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        try {
            PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
            if (info.requestedPermissions != null)
                for (String perm : info.requestedPermissions)
                    if (isRuntimePermission(perm))
                        permissions.add(perm);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return permissions;
    }

    public boolean requestAllRuntimePermissions(int requestCode) {
        return noRuntimePermissions || requestAllRuntimePermissions(null, null, requestCode);
    }

    public boolean requestAllRuntimePermissions(int rationaleResID, int requestCode) {
        return noRuntimePermissions || requestAllRuntimePermissions(activity.getString(rationaleResID), null, requestCode);
    }

    public boolean requestAllRuntimePermissions(String rationale, int requestCode) {
        return noRuntimePermissions || requestAllRuntimePermissions(rationale, null, requestCode);
    }

    public boolean requestAllRuntimePermissions(PermissionRequestCallback callback) {
        return noRuntimePermissions || requestAllRuntimePermissions(null, callback, PERMISSION_REQUEST_CODE);
    }

    public boolean requestAllRuntimePermissions(int rationaleResID, PermissionRequestCallback callback) {
        return noRuntimePermissions || requestAllRuntimePermissions(activity.getString(rationaleResID), callback, PERMISSION_REQUEST_CODE);
    }

    public boolean requestAllRuntimePermissions(String rationale, PermissionRequestCallback callback) {
        return noRuntimePermissions || requestAllRuntimePermissions(rationale, callback, PERMISSION_REQUEST_CODE);
    }

    public boolean requestAllRuntimePermissions(String rationale, PermissionRequestCallback callback, int requestCode) {
        if (noRuntimePermissions)
            return true;
        List<String> permList = getAllRuntimePermissions();
        log(!permList.isEmpty() ? "Request all runtime permissions" : "No runtime permissions found");
        return permList.isEmpty() || checkPermissions(permList.toArray(new String[permList.size()]), rationale, callback, requestCode);
    }
}
