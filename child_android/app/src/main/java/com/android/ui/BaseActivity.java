package com.android.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.AppT;
import com.android.utils.AppUtil;
import com.jaeger.library.StatusBarUtil;
import com.android.PEApplication;
import com.koi.chat.R;
import com.android.ui.login.LoginActivity;
import com.android.utils.GlideCircleTransformUtil;
import com.umeng.analytics.MobclickAgent;

import org.apache.commons.lang.StringUtils;
import org.xutils.x;


/**
 * Activity基础页面
 */
public class BaseActivity extends AppT {
    protected boolean fromSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        setStatusBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void setStatusBar() {
        StatusBarUtil.setColor(this, getResources().getColor(R.color.dark_gray));
//        StatusBarUtil.setTranslucent(this, 100);
    }

    public void setTextColor(int tvId, int color) {
        TextView tv = (TextView) findViewById(tvId);
        if (tv != null)
            setTextColor(tv, color);
    }

    public void setTextColor(TextView tv, int color) {
        tv.setTextColor(AppUtil.getColorStateList(color));
    }

    public boolean isLogin() {
        return PEApplication.INSTANCE.isLogin();
    }

    public boolean openNeedLogin(Class<? extends Activity> b) {
        if (!isLogin()) {
            open(LoginActivity.class);
            return false;
        }
        open(b);
        return true;
    }

    public void goMain() {
        if (!PEApplication.INSTANCE.isLogin()) {
            alphaOpen(LoginActivity.class, true);
            return;
        }
        if (this instanceof MainActivity) {
            ((MainActivity) INSTANCE).showIndex();
        } else {
            mTaskManager.closeAllActivity();
            alphaOpen(MainActivity.class, true);
        }
    }

    public String thumbUrl(String url, String suffix) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        return String.format("%s%s", url, suffix);
    }


}
