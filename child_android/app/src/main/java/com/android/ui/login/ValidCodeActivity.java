package com.android.ui.login;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.utils.ALog;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 验证码页面
 */
public class ValidCodeActivity extends BaseActivity {
    private final int WAIT_SECOND = Constants.ENABLE_TIME_MAX;
    private int currentWaitSecond = WAIT_SECOND;
    private Timer timer;
    private LinearLayout validCodeLayout;
    private Map<String, String> validCodeMap = new HashMap<String, String>();

    protected void getValidCode(final LinearLayout validCodeLayout, final int type, final String mobile) {
        this.validCodeLayout = validCodeLayout;
        this.validCodeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValidCode(validCodeLayout, type, mobile);
            }
        });
        showDialog();
        final RequestParams params = HttpUtil.requestParams("external/user/sendcode");
        params.addBodyParameter("phone", mobile);
        params.addBodyParameter("way", String.valueOf(type));
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                closeDialog();
                HttpResult httpResult = HttpResult.createWith(result);
                if (httpResult.isSuccess()) {
                    toast(getString(R.string.text_register_valid_code_hint));
                    validCodeMap.put(mobile, (String) httpResult.payload);
                    ALog.e("valid code: " + validCodeMap.get(mobile));
                    currentWaitSecond = WAIT_SECOND;
                    if (timer == null) {
                        timer = new Timer();
                        timer.schedule(task, 1000, 1000);
                    }
                } else {
                    toast(httpResult.returnMsg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                closeDialog();
                HttpUtil.onError(params.getUri(), ex);
                toast(BaseActivity.DEFAULT_HTTP_ERROR);
            }

            @Override
            public void onCancelled(CancelledException cex) {
                closeDialog();
            }

            @Override
            public void onFinished() {
                closeDialog();
            }
        });
    }

    protected boolean validCode(String mobile, String validCode) {
        if (StringUtils.isBlank(validCode)) {
            toast("请输入验证码");
            return false;
        }
//        if (!StringUtils.equals(validCode, validCodeMap.get(mobile))) {
//            toast("验证码错误");
//            return false;
//        }
        return true;
    }

    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView leftTv = (TextView) validCodeLayout.findViewWithTag("10");
                    TextView rightTv = (TextView) validCodeLayout.findViewWithTag("11");
                    if (currentWaitSecond > 0) {
                        currentWaitSecond--;
                        rightTv.setVisibility(View.GONE);
                        leftTv.setText(String.format("%ds", currentWaitSecond));
                        validCodeLayout.setEnabled(false);
                    }
                    if (currentWaitSecond == 0) {
                        leftTv.setText(getResources().getString(R.string.text_register_valid_code_left));
                        rightTv.setVisibility(View.VISIBLE);
                        validCodeLayout.setEnabled(true);
                    }
                }
            });
        }
    };
}
