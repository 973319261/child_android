package com.android.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import android.Manifest.permission;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * 通用设备信息处理类
 */
public class DeviceInfoUtil {

	/**
	 *  获取设备唯一ID
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String 对于GSM手机返回IMEI，对于CDMA手机返回MEID,如果设备不可用则返回NULL，比如在模拟器上
	 */
	public static String getDeviceId(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (StringUtils.isBlank(tm.getDeviceId())) {
			return getSimNumber(context);
		}
		return tm.getDeviceId();
	}

	/**
	 *  获取IMSI 编号
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String 国际移动用户识别码是区别移动用户的标志，储存在SIM卡中
	 */
	public static String getSimNumber(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telManager.getSubscriberId();
	}

	/**
	 *  获取ICCID
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getICCID(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telManager.getSimSerialNumber();
	}

	/**
	 *  获取运营商名称
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String 中国移动,中国联通,中国电信
	 */
	public static String getSimOperatorName(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager telManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = telManager.getSubscriberId();

		if (imsi != null) {
			if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
				// 因为移动网络编号46000下的IMSI已经用完，所以虚拟了一个46002编号，134/159号段使用了此编号 //中国移动
				return "中国移动";

			} else if (imsi.startsWith("46001")) {

				// 中国联通
				return "中国联通";

			} else if (imsi.startsWith("46003")) {

				// 中国电信
				return "中国电信";
			}
		}
		return "unknow";

	}

	/**
	 *  获取设备mac地址
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getMacAddress(Context context) {
		if (context == null) {
			return null;
		}

		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifi == null)
			return "";
		WifiInfo info = wifi.getConnectionInfo();
		if (null != info) {
			return info.getMacAddress();
		}
		return "";
	}

	/**
	 *  获取设备IMEI编号
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getIMEI(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String deviceId = tm.getDeviceId();
		if (null == deviceId) {
			deviceId = getSimNumber(context);
		}
		if (null != deviceId && deviceId.length() > 20) {
			deviceId = deviceId.substring(0, 20);
		}
		if (deviceId == null || TextUtils.isEmpty(deviceId.trim())) {
			deviceId = "000000000000000";
		}
		return deviceId;
	}

	/**
	 *  获取手机型号
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getModel() {
		return Build.MODEL;
	}

	/**
	 *  获取手机品牌
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getBrand() {
		return Build.BRAND;
	}

	/**
	 *  取得特殊手机品牌,例如 鼎为手机
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getSpecilBrand() {
		String version = Build.DISPLAY;
		if (version == null)
			return "";
		String[] arr = version.split("_");
		if (arr == null || arr.length < 2) {
			return "";
		}
		String pid = arr[0];
		/** String cid = arr[1]; **/
		return pid;
	}

	/**
	 *  取得特殊的机型，比如鼎为的 手机
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getSpecilModel() {
		String version = Build.DISPLAY;
		if (version == null)
			return "";
		String[] arr = version.split("_");
		if (arr == null || arr.length < 2) {
			return "";
		}
		/** String pid = arr[0]; **/
		String cid = arr[1];
		return cid;
	}

	/**
	 *  获取android 版本
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getOSVersion() {
		return Build.VERSION.RELEASE;
	}

	/**
	 *  获取国家代码
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getCountoryCode(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSimCountryIso();
	}

	/**
	 *  获取当前设备语言
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getLanguage(Context context) {
		if (context == null) {
			return null;
		}
		String countoryLanguage = context.getResources().getConfiguration().locale
				.getCountry();
		return countoryLanguage;
	}

	/**
	 *  获取移动网络运营商名称
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getNetworkOperatorName(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getNetworkOperatorName();
	}

	/**
	 *  获取屏幕分辨率(像素)
	 * 
	 * @param windowManager
	 *            {@link WindowManager}
	 * @author Henry
	 * @date 2013-4-24
	 * @return int
	 */
	private static int getTruePixelsHeight(WindowManager windowManager) {
		try {
			if (Build.VERSION.SDK_INT == 13) {
				// 13表示3.2系统，需要调用getRealHeight获取真实的屏幕高度
				Display display = windowManager.getDefaultDisplay();
				Class<?> c = Class.forName("android.view.Display");
				Method method = c.getMethod("getRealHeight");
				int height = (Integer) method.invoke(display);
				return height;
			}
		} catch (Exception e) {

		}
		return 0; // 返回0，表示此函数返回值不可用
	}

	/**
	 *  获取屏幕分辨率
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return int[] [0] width [1]height
	 */
	public static int[] getDisplayResolution(Context context) {
		if (context == null) {
			return null;
		}
		int[] resolution = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int trueHeight = getTruePixelsHeight(windowManager);
		if (trueHeight > 0)
			height = trueHeight;

		resolution[0] = width;
		resolution[1] = height;
		return resolution;
	}

	/**
	 *  获取屏幕分辨率
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String width*height
	 */
	public static String getScreen(Context context) {
		if (context == null) {
			return null;
		}
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		int trueHeight = getTruePixelsHeight(windowManager);
		if (trueHeight > 0)
			height = trueHeight;

		String str = "";
		str = String.valueOf(width) + "*" + String.valueOf(height);

		return str;
	}

	/**
	 *  获取屏幕密度
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return int[] int[0] desity 屏幕密度（像素比例：0.75/1.0/1.5/2.0） , int[1]
	 *         desityDIP 屏幕密度（每寸像素：120/160/240/320）
	 */
	public static int[] getDesity(Context context) {
		if (context == null) {
			return null;
		}
		int[] desitys = new int[2];
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		int desity = (int) dm.density;
		desitys[0] = desity;
		desitys[1] = desity;
		return desitys;
	}

	/**
	 *  获取手机当前IP地址
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("IpAddress", ex.toString());
		}
		return null;
	}

	/**
	 *  获取SDK版本号
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}

	/**
	 *  获取手机号码
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getPhoneNumber(Context context) {
		if (context == null) {
			return null;
		}
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getLine1Number();
	}


	/**
	 *  获取WIFI BSSID
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getBSSID(Context context) {
		if (context == null) {
			return null;
		}
		WifiManager wifi_service = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifi_service != null) {
			WifiInfo wifiInfo = wifi_service.getConnectionInfo();
			if (wifiInfo != null) {
				return wifiInfo.getBSSID();
			}
		}
		return null;
	}


	/**
	 *  判断屏幕是否关闭
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return boolean
	 */
	public static boolean isScreenOff(Context context) {
		if (context == null) {
			return false;
		}
		KeyguardManager mKeyguardManager = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);

		return mKeyguardManager.inKeyguardRestrictedInputMode();
	}

	/**
	 *  获取CPU型号和CPU厂商
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return cpuInfo[0]:CPU型号,cpuInfo[1]:CPU厂商
	 */
	public static String[] getCpuInfo() {
		String[] cpuInfo = null;
		try {
			cpuInfo = new String[2];
			FileReader fileReader = new FileReader("/proc/cpuinfo");
			BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
			// get cpu Processor
			String line = bufferedReader.readLine();
			int index = 0;
			if (line != null) {
				index = line.indexOf(": ");
				if (index != -1) {
					cpuInfo[0] = line.substring(index + 2);
				}
			}

			// get cpu Hardware
			line = "";
			while (line != null && !line.contains("Hardware")) {
				line = bufferedReader.readLine();
			}
			if (line != null) {
				index = line.indexOf(": ");
				if (index != -1) {
					cpuInfo[1] = line.substring(index + 2);
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
		}
		return cpuInfo;
	}
	
	/**
	 * 获取CPU核数
	 * @return
	 */
	public static int getCpuCores() {
		 class CpuFilter implements FileFilter {
	        @Override
	        public boolean accept(File pathname) {
	            //Check if filename is "cpu", followed by a single digit number
	            if(Pattern.matches("cpu[0-9]+", pathname.getName())) {
	                return true;
	            }
	            return false;
	        }      
	    }
		 try {
		        //Get directory containing CPU info
		        File dir = new File("/sys/devices/system/cpu/");
		        //Filter to only list the devices we care about
		        File[] files = dir.listFiles(new CpuFilter());
		        //Return the number of cores (virtual CPU devices)
		        return files.length;
		    } catch(Exception e) {
		    	e.printStackTrace();
		    }
		 //Default to return 1 core
		return 1;
	}
	
	

	/**
	 *  判断是否允许安装未知源软件
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return boolean true 允许, false 不允许
	 */
	public static boolean isAllowUnknownRes(Context context) {
		if (context == null) {
			return false;
		}
		boolean allow = false;
		try {
			float f = Secure.getFloat(context.getContentResolver(),
					Secure.INSTALL_NON_MARKET_APPS);
			if (f == 1) {
				allow = true;
			}
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
		return allow;
	}

	/**
	 *  判断是否有SD卡
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return boolean true 存在,false 不存在
	 */
	public static boolean hasSDCard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	/**
	 *  获取当前语言
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String CN "2" TW "3" US "4"
	 */
	public static String getSysLanguage() {
		String languageCode = "5";
		String language = Locale.getDefault().getCountry();
		if ("CN".equals(language)) {
			languageCode = "2";
		} else if ("TW".equals(language)) {
			languageCode = "3";
		} else if ("US".equals(language)) {
			languageCode = "4";
		}
		return languageCode;
	}

	/**
	 *  获取SD卡剩余空间
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return long
	 */
	public static long getSDCardLeftSpace() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			long blockSize = sf.getBlockSize();
			long availCount = sf.getAvailableBlocks();
			return blockSize * availCount;
		}
		return 0;
	}

	/**
	 *  获取/DATA分区大小
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return long
	 */
	public static long getDataLeftSpace() {
		File root = Environment.getDataDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long availCount = sf.getAvailableBlocks();
		return blockSize * availCount;
	}

	/**
	 *  获取手机内部存储剩余空间
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return long
	 */
	public static long getSystemLeftSpace() {
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long availCount = sf.getAvailableBlocks();
		return blockSize * availCount;
	}
	
	/*
	 *  获取手机内部存储空间
	 * 
	 * @author Henry
	 * @date 2014-5-21
	 * @return long
	 */
	public static long getSystemSpace() {
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		long blockSize = sf.getBlockSize();
		long blockCount = sf.getBlockCount();
		return blockSize * blockCount;
	}
	
	/**
	 * 获取Android手机RAM大小
	 * @return
	 * 返回值的单位是字节
	 */
	public static long getTotalMemory() {
		String fileName = "/proc/meminfo";
		String totalMem = "";
		long mTotal = 1;
		BufferedReader localBufferedReader = null;
		try {
			FileReader fr = new FileReader(fileName);
			localBufferedReader = new BufferedReader(fr, 8);
			if ((totalMem = localBufferedReader.readLine()) != null) {
				int begin = totalMem.indexOf(':');
				int end = totalMem.indexOf('k');
				// 采集数量的内存
				totalMem = totalMem.substring(begin + 1, end).trim();
				// 转换为Long型并将得到的内存单位转换为字节
				mTotal = Long.parseLong(totalMem) * 1024;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != localBufferedReader) {
				try {
					localBufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				localBufferedReader = null;
			}
		}
		return mTotal;
	}

	/**
	 *  获取系统内核版本
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return String
	 */
	public static String getKernelVersion() {
		String version = null;
		try {
			FileReader fileReader = new FileReader("/proc/version");
			BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
			String line = bufferedReader.readLine();
			String[] infos = line.split("\\s+");
			// KernelVersion
			version = infos[2];
			bufferedReader.close();
		} catch (IOException e) {
		}
		return version;
	}

	/**
	 *  判断是否有静默安装权限
	 * 
	 * @param context
	 *            {@link Context}
	 * @author Henry
	 * @date 2013-4-24
	 * @return boolean true 有 false 没有
	 */
	public static boolean hasSilentInstallPermission(Context context) {
		if (context == null) {
			return false;
		}
		int id = context
				.checkCallingOrSelfPermission(permission.INSTALL_PACKAGES);
		return PackageManager.PERMISSION_GRANTED == id;
	}

	/**
	 *  判断手机是否已经root
	 * 
	 * @author Henry
	 * @date 2013-4-24
	 * @return boolean
	 */
	public static boolean isRooted() {
		File suFile = new File("/system/bin/su");
		if (!suFile.exists()) {
			suFile = new File("/system/xbin/su");
			if (!suFile.exists()) {
				suFile = new File("/system/local/su");
			}
		}
		return suFile.exists();
	}

	/**
	 * 
	 * 判断sim卡是否可以正常使用用
	 * @author Linleja
	 * @date 2013-10-14
	 * @return boolean
	 */
    public static boolean isSimCardOK(Context context) {
        if (context.checkCallingOrSelfPermission(permission.READ_PHONE_STATE) == 0) {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            return TelephonyManager.SIM_STATE_READY == telephonyManager.getSimState();
        }

        return false;
    }
	
    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;//默认为38，貌似大部分是这样的
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }
    
    /**
     * 屏幕宽度
     * @param c
     * @return
     */
	public static final int getScreenWidth(Activity c) {
		DisplayMetrics metrics = new DisplayMetrics();
		c.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}
	
	 /**
     * 屏幕高度
     * @param c
     * @return
     */
	public static final int getScreenHeight(Activity c) {
		DisplayMetrics metrics = new DisplayMetrics();
		c.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		return metrics.heightPixels;
	}
}
