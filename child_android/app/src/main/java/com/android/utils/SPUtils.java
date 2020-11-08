package com.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;


import androidx.multidex.MultiDexApplication;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 持久化类（保存到本地）
 */
public class SPUtils extends MultiDexApplication {
    public static SPUtils INSTANCE;
    public static final String SP_LANGUAGE="language";//切换语言
    public static final String SP_PROMPT_TONE="prompt_tone";//是否开启提示音

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

    /**
     * 获取本地数据
     * @param key
     * @return
     */
    public static boolean getSp(String key) {
        return getSp(key, false);
    }

    public static boolean getSp(String key, boolean defaultValue) {
        return SPUtils.getSp().getBoolean(key, defaultValue);
    }

    public static String getSp(String key, String defaultValue) {
        return SPUtils.getSp().getString(key, defaultValue);
    }

    public static int getSp(String key, int defaultValue) {
        return SPUtils.getSp().getInt(key, defaultValue);
    }

    public static long getSp(String key, long defaultValue) {
        return SPUtils.getSp().getLong(key, defaultValue);
    }

    public static float getSp(String key, float defaultValue) {
        return SPUtils.getSp().getFloat(key, defaultValue);
    }

    /**
     * 保存到本地
     * @param key
     * @param value
     */
    public static void setSp(String key, boolean value) {
        SPUtils.getSp().edit().putBoolean(key, value).commit();
    }

    public static void setSp(String key, String value) {
        SPUtils.getSp().edit().putString(key, value).commit();
    }

    public static void setSp(String key, float value) {
        SPUtils.getSp().edit().putFloat(key, value).commit();
    }

    public static void setSp(String key, int value) {
        SPUtils.getSp().edit().putInt(key, value).commit();
    }

    public static void setSp(String key, long value) {
        SPUtils.getSp().edit().putLong(key, value).commit();
    }

    /**
     * 移除本地数据
     * @param key
     */
    public static void removeSp(String key) {
        SPUtils.getSp().edit().remove(key).commit();
    }

    /**
     * 获取JSON对象
     * @param key
     * @return
     */
    public static JSONObject getJsonObjectSp(String key) {
        return AppUtil.toJsonObject(getSp(key, ""));
    }

    /**
     * 获取JSON数组
     * @param key
     * @return
     */
    public static JSONArray getJsonArraySp(String key) {
        return AppUtil.toJsonArray(getSp(key, ""));
    }
}
