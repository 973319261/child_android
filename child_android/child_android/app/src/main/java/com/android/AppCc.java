package com.android;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


import androidx.multidex.MultiDexApplication;

import com.android.utils.AppUtil;

import org.json.JSONArray;
import org.json.JSONObject;

public class AppCc extends MultiDexApplication {
    public static AppCc INSTANCE;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
    }

    public final static SharedPreferences getSp() {
        return INSTANCE.getSharedPreferences("1", Activity.MODE_PRIVATE);
    }

    public static boolean getSp(String key) {
        return getSp(key, false);
    }

    public static boolean getSp(String key, boolean defaultValue) {
        return AppCc.getSp().getBoolean(key, defaultValue);
    }

    public static String getSp(String key, String defaultValue) {
        return AppCc.getSp().getString(key, defaultValue);
    }

    public static int getSp(String key, int defaultValue) {
        return AppCc.getSp().getInt(key, defaultValue);
    }

    public static long getSp(String key, long defaultValue) {
        return AppCc.getSp().getLong(key, defaultValue);
    }

    public static float getSp(String key, float defaultValue) {
        return AppCc.getSp().getFloat(key, defaultValue);
    }

    public static void setSp(String key, boolean value) {
        AppCc.getSp().edit().putBoolean(key, value).commit();
    }

    public static void setSp(String key, String value) {
        AppCc.getSp().edit().putString(key, value).commit();
    }

    public static void setSp(String key, float value) {
        AppCc.getSp().edit().putFloat(key, value).commit();
    }

    public static void setSp(String key, int value) {
        AppCc.getSp().edit().putInt(key, value).commit();
    }

    public static void setSp(String key, long value) {
        AppCc.getSp().edit().putLong(key, value).commit();
    }

    public static void removeSp(String key) {
        AppCc.getSp().edit().remove(key).commit();
    }

    public static JSONObject getJsonObjectSp(String key) {
        return AppUtil.toJsonObject(getSp(key, ""));
    }

    public static JSONArray getJsonArraySp(String key) {
        return AppUtil.toJsonArray(getSp(key, ""));
    }
}
