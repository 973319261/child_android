package com.preschool.edu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.utils.ALog;
import com.preschool.edu.PEApplication;

/**
 * Created by jac_cheng on 2018/1/18.
 */

public class NetStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            // 获取网络连接管理器
            ConnectivityManager connectivityManager = (ConnectivityManager) PEApplication.INSTANCE.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取当前网络状态信息
            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                ALog.e("监听到可用网络切换,调用重连方法");
                //wifi 4g切换重连websocket
                PEApplication.INSTANCE.connectStompClient();
            }
        }
    }
}