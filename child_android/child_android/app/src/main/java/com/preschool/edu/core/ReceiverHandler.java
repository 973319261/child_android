package com.preschool.edu.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class ReceiverHandler extends Handler {

	public static Handler handler;

	public static void handleMessage(int what) {
		handleMessage(what, null);
	}

	public static void handleMessage(int what, String message) {
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("content", message);
		msg.what = what;
		msg.setData(b);

		if (null != handler) {
			handler.sendMessage(msg);
		}
	}
}