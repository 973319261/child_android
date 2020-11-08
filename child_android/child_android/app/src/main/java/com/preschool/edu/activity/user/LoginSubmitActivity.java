package com.preschool.edu.activity.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jac_cheng on 2017/12/27.
 */
@ContentView(R.layout.activity_login_submit)
public class LoginSubmitActivity extends BaseActivity implements TextWatcher {

    @ViewInject(R.id.login_password_et)
    private EditText passwordEt;
    @ViewInject(R.id.login_submit_btn)
    private ImageView submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addTextViewByIdAndStr(R.id.login_mobile_tv, String.format("%s%s", getIntentString("areaCode"), getIntentString("mobile")));

        passwordEt.addTextChangedListener(this);
        updateUI();
    }

    @Event(value = {R.id.login_back_img, R.id.login_forgot_password_layout, R.id.login_submit_btn})
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.login_back_img:
                goBack();
                break;
            case R.id.login_forgot_password_layout:
                Map<String, Object> args = new HashMap<>();
                args.put("areaCode", getIntentString("areaCode"));
                args.put("mobile", getIntentString("mobile"));
                open(ForgotPwdActivity.class, args);
                break;
            case R.id.login_submit_btn:
                if (etIsNull(passwordEt)) {
                    toast("请输入登录密码");
                    return;
                }
                hideKb();
                doLogin();
                break;
        }
    }

    private void updateUI() {
        if (etTxt(passwordEt).length() >= 6) {
            submitBtn.setImageResource(R.mipmap.next_step_arrow_lighted);
            submitBtn.setEnabled(true);
            submitBtn.setFocusable(true);
            submitBtn.setFocusableInTouchMode(true);
        } else {
            submitBtn.setImageResource(R.mipmap.next_step_arrow_grey);
            submitBtn.setEnabled(false);
            submitBtn.setFocusable(false);
            submitBtn.setFocusableInTouchMode(false);
        }
    }

    private void doLogin() {
        showDialog();
        final RequestParams params = HttpUtil.requestParams("external/users/login");
        params.addBodyParameter("login", String.format("%s%s", getIntentString("areaCode"), getIntentString("mobile")));
        params.addBodyParameter("passWd", etTxt(passwordEt));
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                closeDialog();
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    PEApplication.INSTANCE.userLoginCallback((JSONObject) httpResult.payload);
                    goMain();
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateUI();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
