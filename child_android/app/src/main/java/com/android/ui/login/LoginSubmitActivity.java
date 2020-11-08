package com.android.ui.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.PEApplication;
import com.android.bean.User;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.model.MessageEvent;
import com.android.utils.AppUtil;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.core.HttpUtil;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 确认登录页面
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
                open(ForgotPasswordActivity.class, args);
                break;
            case R.id.login_submit_btn:
                if (etIsNull(passwordEt)) {
                    toast(getString(R.string.text_login_enter_password));
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
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    PEApplication.INSTANCE.userLoginCallback((JSONObject) httpResult.payload);
                    loadContacts();
                    goMain();
                } else {
                    toast(httpResult.returnMsg);
                }
                closeDialog();
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

    /**
     * 获取联系人
     */
    private void loadContacts() {
        User.deleteAll();//先清空数据
        final RequestParams params = HttpUtil.requestParams("friends/list");
        params.addBodyParameter("userInfoId", PEApplication.INSTANCE.getUserInfoId());
        params.addBodyParameter("page", "0");
        params.addBodyParameter("size", "10000");
        HttpUtil.printUrl(params);
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    JSONObject json=new JSONObject();
                    try {
                        json.put("data",httpResult.payload);
                        JSONObject data=jsonObject.getJSONObject("data");
                        JSONArray contactDatas = data.getJSONArray("content");
                        List<User> list = AppService.parseContacts(contactDatas);
                        for (User user:list){//保存联系人到本地
                            user.insert();
                        }
                        EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH,"更新通讯录"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    toast(httpResult.returnMsg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                HttpUtil.onError(params.getUri(), ex);
                toast(BaseActivity.DEFAULT_HTTP_ERROR);
            }

            @Override
            public void onCancelled(CancelledException cex) {
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
