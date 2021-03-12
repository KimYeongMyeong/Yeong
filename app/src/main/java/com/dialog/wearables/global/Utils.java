/*
 *******************************************************************************
 *
 * Copyright (C) 2016-2018 Dialog Semiconductor.
 * This computer program includes Confidential, Proprietary Information
 * of Dialog Semiconductor. All Rights Reserved.
 *
 *******************************************************************************
 */

package com.dialog.wearables.global;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    // LOAD SPINNER

    public static void showProgress(final Context context, final View fragmentContainer,
                                    final View progressView, final boolean show) {
        showView(fragmentContainer, !show);
        animate(context, fragmentContainer, !show);
        showView(progressView, show);
        animate(context, progressView, show);
    }

    private static void showView(final View view, final boolean show) {
        if (show) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private static void animate(final Context context, final View view, final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in the
        // progress spinner.
        int shortAnimTime = context.getResources().getInteger(
                android.R.integer.config_shortAnimTime);
        ViewPropertyAnimator animator = view.animate();
        animator.setDuration(shortAnimTime);
        if (show) {
            animator.alpha(1);
        } else {
            animator.alpha(0);
        }
        animator.setListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(final Animator animation) {
                showView(view, show);
            }
        });
    }

    /**
     * @param r Reader
     * @return String
     */
    public static String readerToString(BufferedReader r) {
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = r.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void replaceFragment(final Activity activity, final Fragment fragment,
                                       final int layoutResourceId, final boolean addToBackstack) {
        FragmentTransaction fragmentTransaction = activity.getFragmentManager().beginTransaction();
        fragmentTransaction.replace(layoutResourceId, fragment);
        if (addToBackstack) {
            fragmentTransaction.addToBackStack(fragment.getClass().getName());
        }
        fragmentTransaction.commit();
    }

    public static String[] concatStringArray(String[] A, String[] B) {
        int aLen = A.length;
        int bLen = B.length;
        String[] C = new String[aLen + bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
    }

    public static int compareFirmwareVersion(String a, String b) {
        String[] thisParts = a.split("\\.");
        String[] thatParts = b.split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        Pattern pattern = Pattern.compile("(\\d+)(\\D*.*)");
        for (int i = 0; i < length; i++) {
            String thisPartStr = i < thisParts.length ? thisParts[i] : "0";
            String thatPartStr = i < thatParts.length ? thatParts[i] : "0";
            Matcher thisMatcher = pattern.matcher(thisPartStr);
            Matcher thatMatcher = pattern.matcher(thatPartStr);
            if (thisMatcher.find() && thatMatcher.find()) {
                // Compare numeric part
                int thisPart = Integer.parseInt(thisMatcher.group(1));
                int thatPart = Integer.parseInt(thatMatcher.group(1));
                if (thisPart != thatPart)
                    return thisPart - thatPart;
                // Compare possible alphanumeric part
                int cmp = thisMatcher.group(2).compareTo(thatMatcher.group(2));
                if (cmp != 0)
                    return cmp;
            } else {
                // Compare as string
                int cmp = thisPartStr.compareTo(thatPartStr);
                if (cmp != 0)
                    return cmp;
            }
        }
        return 0;
    }

    /**
     * Toaster
     */
    public static void showToast(@NonNull final Context context, @StringRes final int res) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final Toast toast = Toast.makeText(context, context.getString(res), Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public static void showToast(@NonNull final Context context, final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }
}
