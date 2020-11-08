package com.android.ui.login;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.PEApplication;
import com.android.dialog.LanguageDialog;
import com.android.dialog.PermissionsDialog;
import com.android.dialog.PromptDialog;
import com.android.utils.AppUtil;
import com.android.utils.LanguageUtil;
import com.android.utils.MPermissionUtils;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * 登录页面
 */
@ContentView(R.layout.activity_login)
public class LoginActivity extends BaseActivity {
    @ViewInject(R.id.login_mobile_area_code_tv)
    private TextView areaCodeTv;
    @ViewInject(R.id.login_mobile_et)
    private EditText mobileEt;
    private Activity myActivity;
    private boolean isMain;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity=this;
        if (LanguageUtil.isSetting()){//未设置
            LanguageDialog languageDialog = new LanguageDialog(this);
            languageDialog.show(getSupportFragmentManager(),"");//显示弹出框
        }
        Locale locale = LanguageUtil.getAppLocale();//获取本地的语言
        LanguageUtil.changeAppLanguage(this,locale,true);//设置语言
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
        permissionHint();
        if (getIntentBoolean("isLogoff")){
            PromptDialog promptDialog=new PromptDialog(this,getString(R.string.text_logoff_notification),getString(R.string.text_logoff_notification_content));
            promptDialog.setOnConfirmListener(new PromptDialog.OnConfirmListener() {
                @Override
                public void confirm(Dialog dialog) {
                    PEApplication.INSTANCE.userLogoutCallback();//关闭资源
                    mTaskManager.closeAllActivityExceptOne("LoginActivity");
                }
            });
            promptDialog.show(getSupportFragmentManager(),"");
        }
    }

    @Event(value = {R.id.login_mobile_area_code_tv, R.id.login_cancel_tv, R.id.login_protocols_layout,
            R.id.login_question_tv, R.id.login_next_step_btn})
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
        } else {
            nextStepBtn.setVisibility(View.VISIBLE);
            showViewById(R.id.login_protocols_layout);

            nextStepBtn.setImageResource(showLoginBtn ? R.mipmap.next_step_arrow_lighted : R.mipmap.next_step_arrow_grey);
        }
    }
    /**
     *  //权限温馨提示
     */
    private void permissionHint(){
        String content="";
        int count=0;
        if (!MPermissionUtils.hasAlwaysDeniedPermission(myActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {//缺少手机存储权限
            count++;
            content=content+count+"、"+getString(R.string.text_permissions_storage);
        }
        if (!MPermissionUtils.checkPermissions(myActivity,PERMISSIONS_STORAGE)&& !MPermissionUtils.hasAlwaysDeniedPermission(myActivity,PERMISSIONS_STORAGE)){
            final PermissionsDialog permissionsDialog = new PermissionsDialog(myActivity,content);
            permissionsDialog.setOnOpenListener(new PermissionsDialog.OnOpenListener() {
                @Override
                public void onOpen(Dialog dialog) {
                    MPermissionUtils.startAppSettings(myActivity);
                    dialog.dismiss();
                }
            });
            permissionsDialog.setOnCloseListener(new PermissionsDialog.OnCloseListener() {
                @Override
                public void onClose(Dialog dialog) {
                    dialog.dismiss();
                }
            });
            permissionsDialog.show(getSupportFragmentManager(),"");
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
                        open(RegisterActivity.class, args);
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
