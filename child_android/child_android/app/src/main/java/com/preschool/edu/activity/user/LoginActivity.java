package com.preschool.edu.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;

import org.json.JSONException;
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
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity {

    @ViewInject(R.id.login_mobile_area_code_tv)
    private TextView areaCodeTv;
    @ViewInject(R.id.login_mobile_et)
    private EditText mobileEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mobileEt.addTextChangedListener(new TextWatcher() {
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
        });
        updateUI();
    }

    @Event(value = {R.id.login_mobile_area_code_tv, R.id.login_cancel_tv, R.id.login_protocols_layout,
            R.id.login_question_tv, R.id.login_next_step_btn, R.id.login_with_weixin_btn})
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.login_mobile_area_code_tv:
                open(CountryAreaCodeActivity.class, 300);
                break;
            case R.id.login_cancel_tv:
                goBack();
                break;
            case R.id.login_protocols_layout:
                break;
            case R.id.login_question_tv:
                break;
            case R.id.login_next_step_btn:
                hideKb();
                tryCheckMobile();
                break;
            case R.id.login_with_weixin_btn:
                toast("TODO: 等微信信息");
                break;
        }
    }

    private void updateUI() {
        ImageView nextStepBtn = (ImageView) findViewById(R.id.login_next_step_btn);
        boolean showLoginBtn = etTxt(mobileEt).length() == 11;//要根据不同的国家判断
        nextStepBtn.setEnabled(showLoginBtn);
        nextStepBtn.setFocusable(showLoginBtn);
        nextStepBtn.setFocusableInTouchMode(showLoginBtn);
        if (etIsNull(mobileEt)) {
            nextStepBtn.setVisibility(View.INVISIBLE);
            hideViewId(R.id.login_protocols_layout, false);
            showViewById(R.id.login_with_sns_hint_tv);
            showViewById(R.id.login_with_weixin_btn);
        } else {
            hideViewId(R.id.login_with_sns_hint_tv, false);
            hideViewId(R.id.login_with_weixin_btn, false);
            nextStepBtn.setVisibility(View.VISIBLE);
            showViewById(R.id.login_protocols_layout);

            nextStepBtn.setImageResource(showLoginBtn ? R.mipmap.next_step_arrow_lighted : R.mipmap.next_step_arrow_grey);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 300) {
                areaCodeTv.setText(data.getStringExtra("areaCode"));
            }
        }
    }

    private void tryCheckMobile() {
        showDialog();
        final RequestParams params = HttpUtil.requestParams("external/users/check_exists");
        params.addBodyParameter("login", String.format("%s%s", tvTxt(areaCodeTv), etTxt(mobileEt)));

        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                closeDialog();
                HttpResult httpResult = HttpResult.createWith(result);
                if (httpResult.isSuccess()) {
                    Map<String, Object> args = new HashMap<String, Object>();
                    args.put("areaCode", tvTxt(areaCodeTv));
                    args.put("mobile", etTxt(mobileEt));
                    if ((Boolean) httpResult.payload) {
                        open(LoginSubmitActivity.class, args);
                    } else {
                        open(RegActivity.class, args);
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

    private void tryLoginWithWechat() throws JSONException {
        showDialog();
        JSONObject userLogin = new JSONObject();
        userLogin.put("headerImageUrl", "");
        userLogin.put("name", "");
        userLogin.put("openid", "");
        userLogin.put("sex", "");
        userLogin.put("role", "ROLE_parent");
        final RequestParams params = HttpUtil.requestParams("external/admin/wei-xin-users");
        params.setAsJsonContent(true);
        params.setBodyContent(userLogin.toString());

        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject result) {
                closeDialog();
                HttpResult httpResult = HttpResult.createWith(result);
                if (httpResult.isSuccess()) {

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
}
