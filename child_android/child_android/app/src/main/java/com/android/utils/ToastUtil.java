package com.android.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

public class ToastUtil {
	/** default **/
	public static void defaultToast(Context context, String txt) {
		Toast toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
		toast.show();
	}

	public static void centerToast(Context context, String txt) {
		Toast toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}
	
	public static void rightToast(Context context, String txt) {
		Toast toast = Toast.makeText(context, txt, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.RIGHT, 0, 0);
		toast.show();
	}

}
