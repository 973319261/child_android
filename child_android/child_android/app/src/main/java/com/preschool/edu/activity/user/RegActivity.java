package com.preschool.edu.activity.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.Constants;
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


/**
 * Created by jac_cheng on 2017/12/27.
 */
@ContentView(R.layout.activity_reg)
public class RegActivity extends ValidCodeActivity implements TextWatcher {

    @ViewInject(R.id.reg_mobile_hint_tv)
    private TextView mobileTitleTv;
    @ViewInject(R.id.reg_valid_code_et)
    private EditText validCodeEt;
    @ViewInject(R.id.reg_password_et)
    private EditText passwordEt;
    @ViewInject(R.id.reg_valid_resend_layout)
    private LinearLayout validCodeLayout;
    @ViewInject(R.id.reg_next_step_btn)
    private ImageView nextStepBtn;
    private String mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mobile = String.format("%s%s", getIntentString("areaCode"), getIntentString("mobile"));
        mobileTitleTv.setText(Html.fromHtml(String.format("输入<font color='#e25b64'>%s</font>收到的验证码", mobile)));
        validCodeEt.addTextChangedListener(this);
        passwordEt.addTextChangedListener(this);
        updateUI();
        getValidCode(validCodeLayout, Constants.VALID_CODE_WITH_REG, mobile);
    }

    @Event(value = {R.id.reg_back_img, R.id.reg_next_step_btn })
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.reg_back_img:
                goBack();
                break;
            case R.id.reg_next_step_btn:
                hideKb();
                if (validCode(mobile, etTxt(validCodeEt))) {
                    try {
                        tryDoReg();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void updateUI() {
        if (etTxt(passwordEt).length() >= 6 && etIsNull(validCodeEt) == false) {
            nextStepBtn.setImageResource(R.mipmap.next_step_arrow_lighted);
            nextStepBtn.setEnabled(true);
            nextStepBtn.setFocusable(true);
            nextStepBtn.setFocusableInTouchMode(true);
        } else {
            nextStepBtn.setImageResource(R.mipmap.next_step_arrow_grey);
            nextStepBtn.setEnabled(false);
            nextStepBtn.setFocusable(false);
            nextStepBtn.setFocusableInTouchMode(false);
        }
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

    private void tryDoReg() throws JSONException {
        showDialog();
        JSONObject userLogin = new JSONObject();
        userLogin.put("login", String.format("%s%s", getIntentString("areaCode"), getIntentString("mobile")));
        userLogin.put("passWd", etTxt(passwordEt));
        userLogin.put("role", Constants.CURRENT_USER_ROLE);
        final RequestParams params = HttpUtil.requestParams("external/users/regist");
        params.setAsJsonContent(true);
        params.setBodyContent(userLogin.toString());
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                closeDialog();
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    PEApplication.INSTANCE.userLoginCallback((JSONObject) httpResult.payload);
                    toast("TODO:完善信息");
                    //TODO 完善信息
                } else {
                    toast(httpResult.returnMsg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                closeDialog();
                ex.printStackTrace();
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