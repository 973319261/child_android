package com.android.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.android.PEApplication;
import com.koi.chat.R;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * App工具类
 */
public class AppUtil {
	/**
	 * 字符串转JSON数组
	 * @param data
	 * @return
	 */
	public static JSONArray toJsonArray(String data) {
		if (StringUtils.isBlank(data)) {
			return new JSONArray();
		}
		try {
			return new JSONArray(data);
		} catch (JSONException e) {
			return new JSONArray();
		}
	}

	/**
	 * 字符串转JSON对象
	 * @param data
	 * @return
	 */
	public static JSONObject toJsonObject(String data) {
		if (StringUtils.isBlank(data)) {
			return new JSONObject();
		}
		try {
			return new JSONObject(data);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}

	/**
	 * JSON对象转Map
	 * @param data
	 * @return
	 */
	public static Map<String, Object> toMap(JSONObject data) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (data != null) {
			@SuppressWarnings("rawtypes")
			Iterator keys = data.keys();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				result.put(key, data.opt(key));
			}
		}
		return result;
	}

	/**
	 * MAP转JSON对象
	 * @param datas
	 * @return
	 */
	public static JSONObject toJsonObject(Map<String, Object> datas) {
		JSONObject result = new JSONObject();
		if (datas != null) {
			try {
				for (String key : datas.keySet()) {
					result.put(key, datas.get(key));
				}
			} catch (JSONException e) {
			}
		}
		return result;
	}

	/**
	 * JSON数组转JSON对象列表
	 * @param datas
	 * @return
	 */
	public static List<JSONObject> toArray(JSONArray datas) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		if (datas != null) {
			for (int i = 0; i < datas.length(); i++) {
				result.add(datas.optJSONObject(i));
			}
		}
		return result;
	}

	/**
	 * JSON对象列表转JSON数组
	 * @param datas
	 * @return
	 */
	public static JSONArray listToJsonArray(List<JSONObject> datas) {
		JSONArray result = new JSONArray();
		if (datas != null) {
			for (JSONObject each : datas) {
				result.put(each);
			}
		}
		return result;
	}

	/**
	 * JSON数组转list
	 * @return
	 */
	public static <T>List<T> jsonArrayToList(JSONArray array, Class<T> vo) {
		List<T> result = com.alibaba.fastjson.JSONObject.parseArray(array.toString(), vo);
		return result;
	}

	/**
	 * 判断JSON对象是否为空
	 * @param obj
	 * @return
	 */
	public static boolean isNull(JSONObject obj){
		return obj == null || obj.length() == 0;
	}

	/**
	 * 判断JSON数组是否为空
	 * @param array
	 * @return
	 */
	public static boolean isNull(JSONArray array){
		return array == null || array.length() == 0;
	}

	public static void addKeyValue2JsonObject(JSONObject data, String key, Object value) {
		if (data == null) {
			data = new JSONObject();
		}
		try {
			data.put(key, value);
		} catch (JSONException e) {
			ALog.e(e);
		}
	}


	public static ColorStateList getColorStateList(int colorId) {
		Resources resource = (Resources) SPUtils.INSTANCE.getBaseContext().getResources();
		return (ColorStateList) resource.getColorStateList(colorId);
	}


	/**
	 * 获取版本号
	 *
	 * @param context
	 * @return
	 */
	public static String getVersionCode(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo;
		String versionCode = "";
		try {
			packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionCode = packageInfo.versionCode + "";
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	/**
	 *获取版本名称
	 *
	 * @param context
	 * @return
	 */
	public static String getVersionName(Context context) {
		PackageManager packageManager = context.getPackageManager();
		PackageInfo packageInfo;
		String versionName = "";
		try {
			packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
			versionName = packageInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	/**
	 * 安装apk文件
	 */
	public static void installAPK(Context c, Uri apk) {
		// 通过Intent安装APK文件
		Intent intents = new Intent();
		intents.setAction("android.intent.action.VIEW");
		intents.addCategory("android.intent.category.DEFAULT");
		intents.setType("application/vnd.android.package-archive");
		intents.setData(apk);
		intents.setDataAndType(apk, "application/vnd.android.package-archive");
		intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		android.os.Process.killProcess(android.os.Process.myPid());
		// 如果不加上这句的话在apk安装完成之后点击打开会崩溃
		c.startActivity(intents);
	}
	public static void playMsgSound() {
		boolean flag= SPUtils.getSp(SPUtils.SP_PROMPT_TONE,false);
		if (flag){
			MediaPlayer.create(PEApplication.INSTANCE, R.raw.msg_sound).start();
		}
	}

	/**
	 * 判断当前网络是连接的
	 * @return
	 */
	public static boolean isNetConnect() {
		ConnectivityManager connectivity = (ConnectivityManager) PEApplication.INSTANCE.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo info = connectivity.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				// 当前网络是连接的
				if (info.getState() == NetworkInfo.State.CONNECTED) {
					// 当前所连接的网络可用
					return true;
				}
			}
		}
		return false;
	}
}
