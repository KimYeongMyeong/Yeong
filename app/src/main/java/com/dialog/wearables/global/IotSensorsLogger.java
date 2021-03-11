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

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Logger to log data to SD
 */
public class IotSensorsLogger {

    public static final String LOG_ENABLED_PREF_KEY = "prefLogEnabled";

    private Logger logger;
    private SharedPreferences preferences;
    private boolean enabled;

    private IotSensorsLogger(Class clazz, android.content.Context context) {
        logger = Logger.getLogger(clazz);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        enabled = preferences.getBoolean(LOG_ENABLED_PREF_KEY, false);
    }

    public static IotSensorsLogger getLogger(Class clazz, android.content.Context context) {
        return new IotSensorsLogger(clazz, context);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        preferences.edit().putBoolean(LOG_ENABLED_PREF_KEY, enabled).apply();
    }

    public void debug(String msg) {
        if (enabled)
            logger.debug(msg);
    }

    public void info(String msg) {
        if (enabled)
            logger.info(msg);
    }

    public static void configure(boolean logToFile) {
        LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setRootLevel(Level.ALL);
        logConfigurator.setLevel("org.apache", Level.ALL);
        logConfigurator.setUseFileAppender(logToFile);
        if (logToFile) {
            logConfigurator.setFileName(Environment.getExternalStorageDirectory().toString() + File.separator + "Dialog Semiconductor/IoT Sensors/sensor_data.log");
            logConfigurator.setFilePattern("%d{yyyy/MM/dd HH:mm:ss.SSS}     %m%n");
            logConfigurator.setMaxFileSize(1024 * 1024 * 100); // 100 MB
            //logConfigurator.setImmediateFlush(true);
        }
        Logger.getRootLogger().removeAllAppenders();
        logConfigurator.configure();
    }

    private static final String HEX_DIGITS = "0123456789ABCDEF";

    public static String getLogStringFromBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (byte b : bytes) {
            sb.append(HEX_DIGITS.charAt((b >> 4) & 0x0f)).append(HEX_DIGITS.charAt(b & 0x0f)).append(" ");
        }
        return sb.toString();
    }
}
