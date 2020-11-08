package com.android.utils;

import android.app.Activity;
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

import com.android.AppCc;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppUtil {

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

	public static List<JSONObject> toArray(JSONArray datas) {
		List<JSONObject> result = new ArrayList<JSONObject>();
		if (datas != null) {
			for (int i = 0; i < datas.length(); i++) {
				result.add(datas.optJSONObject(i));
			}
		}
		return result;
	}

	public static JSONArray listToJsonArray(List<JSONObject> datas) {
		JSONArray result = new JSONArray();
		if (datas != null) {
			for (JSONObject each : datas) {
				result.put(each);
			}
		}
		return result;
	}

	public static boolean isNull(JSONObject obj){
		return obj == null || obj.length() == 0;
	}

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

	public static int getResourceId(String resName) {
		@SuppressWarnings("rawtypes")
		Class drawable = R.drawable.class;
		Field field = null;
		int r_id = 0;
		try {
			field = drawable.getField(resName);
			r_id = field.getInt(field.getName());
		} catch (Exception e) {
		}
		return r_id;
	}

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static ColorStateList getColorStateList(int colorId) {
		Resources resource = (Resources) AppCc.INSTANCE.getBaseContext().getResources();
		return (ColorStateList) resource.getColorStateList(colorId);
	}

	/**
	 * 根据经纬度计算距离
	 */
	public static double getDistance(double lat0, double lng0, double lat1, double lng1) {
		final double EARTH_RADIUS = 6378137.0;
		double radLat1 = (lat0 * Math.PI / 180.0);
		double radLat2 = (lat1 * Math.PI / 180.0);
		double a = radLat1 - radLat2;
		double b = (lng0 - lng1) * Math.PI / 180.0;
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
				+ Math.cos(radLat1) * Math.cos(radLat2)
				* Math.pow(Math.sin(b / 2), 2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}

	public static void saveFile2SDCard(String content, String fileName) {
		FileOutputStream os = null;
		try {
			File file = ImageUtil.getFileInDir(ImageUtil.FILES_DIR, fileName);
			os = new FileOutputStream(file, false);
			os.write(content.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static JSONArray readData4SDCard(String fileName) {
		FileInputStream is = null;
		BufferedReader br = null;
		JSONArray datas = null;
		try {
			File file = ImageUtil.getFileInDir(ImageUtil.FILES_DIR, fileName);
			if (file != null && file.exists()) {
				is = new FileInputStream(file);
				br = new BufferedReader(new InputStreamReader(is));
				StringBuffer sb = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				datas = new JSONArray(sb.toString());
			}
		} catch (Exception e) {
			ALog.e(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return datas;
	}

	public static String readRawDatas(int rawId) {
		try {
			InputStream is = AppCc.INSTANCE.getResources().openRawResource(rawId);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			String str = new String(buffer, "utf-8");
			return str.trim();
		} catch (Exception e) {
			ALog.e(e);
		}
		return null;
	}

	/**
	 * get App versionCode
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
	 * get App versionName
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

	public static void brower(Activity activity, String url) {
		Intent intent = new Intent();
		intent.setAction("android.intent.action.VIEW");
		Uri content_uri_browsers = Uri.parse(url);
		intent.setData(content_uri_browsers);
		intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
		activity.startActivity(intent);
	}

	public static void playMsgSound() {
		MediaPlayer.create(PEApplication.INSTANCE, R.raw.msg_sound).start();
	}

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
