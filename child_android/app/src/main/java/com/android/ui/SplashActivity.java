package com.android.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;

import com.android.utils.AppUtil;
import com.jaeger.library.StatusBarUtil;

import org.json.JSONObject;

/**
 * Created by jac_cheng on 2017/4/13.
 */

public class SplashActivity extends BaseActivity {
    private JSONObject notifyData;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        StatusBarUtil.setTranslucent(this);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        notifyData = AppUtil.toJsonObject(getIntentString("notifyData"));
        mHandler.sendEmptyMessageDelayed(1, 2000);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                if (AppUtil.isNull(notifyData)) {
                    goMain();
                } else {
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(1);
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
