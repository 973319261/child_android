package com.android.utils;

import android.util.Log;

/**
 * 输出日志类
 */
public class ALog {

    public static boolean LOG = false;

    public static void w(Object message, Object... args) {
        if (LOG) {
            Log.w(CS.APP_TAG, String.format(String.valueOf(message), args));
        }
    }

    public static void i(Object message) {
        if (LOG) {
            Log.i(CS.APP_TAG, String.valueOf(message));
        }
    }

    public static void d(Object message) {
        if (LOG) {
            Log.d(CS.APP_TAG, String.valueOf(message));
        }
    }

    public static void w(Object message) {
        if (LOG) {
            Log.w(CS.APP_TAG, String.valueOf(message));
        }
    }

    public static void e(Object message) {
        if (LOG) {
            Log.e(CS.APP_TAG, String.valueOf(message));
        }
    }

    public static void e(Exception e) {
        if (LOG) {
            Log.e(CS.APP_TAG, e.getMessage(), e);
        }
    }
}
