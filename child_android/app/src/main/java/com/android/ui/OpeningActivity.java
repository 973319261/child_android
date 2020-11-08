package com.android.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.PEApplication;
import com.android.bean.User;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.dialog.PromptDialog;
import com.android.model.MessageEvent;
import com.android.ui.login.LoginActivity;
import com.android.utils.LanguageUtil;
import com.android.utils.MPermissionUtils;
import com.android.utils.StatusBarUtil;
import com.koi.chat.R;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.List;
import java.util.Locale;

/**
 * 开屏页面
 */
public class OpeningActivity extends BaseActivity {
    private ImageView ivSplash;
    private Activity myActivity;
    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity = OpeningActivity.this;
        if (!LanguageUtil.isSetting()) {//未设置
            Locale locale = LanguageUtil.getAppLocale();//获取本地的语言
            LanguageUtil.changeAppLanguage(this,locale,true);//设置语言
        }
        setContentView(R.layout.activity_opening);
        initView();

    }
    private void initView() {
        StatusBarUtil.setStatusBar(myActivity,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(myActivity,true);//状态栏文字颜色
        MPermissionUtils.requestPermissionsResult(myActivity,
                REQUEST_EXTERNAL_STORAGE,
                PERMISSIONS_STORAGE,
                new MPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0){
                                    finish();
                                    return;
                                }
                                /**
                                 * 获取联系人
                                 */
                                if (!"".equals(PEApplication.INSTANCE.getUserLogin())){
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
                                                    goMain();
                                                    EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH,"更新通讯录"));
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {//token过期
                                                PromptDialog promptDialog=new PromptDialog(myActivity,getString(R.string.text_logoff_notification),getString(R.string.text_logoff_notification_content));
                                                promptDialog.setOnConfirmListener(new PromptDialog.OnConfirmListener() {
                                                    @Override
                                                    public void confirm(Dialog dialog) {
                                                        PEApplication.INSTANCE.userLogoutCallback();//关闭资源
                                                        open(LoginActivity.class,true);
                                                    }
                                                });
                                                promptDialog.show(getSupportFragmentManager(),"");
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
                                }else {
                                    open(LoginActivity.class,true);
                                }

                            }
                        }, 2000);
                    }

                    @Override
                    public void onPermissionDenied() {
                        Intent intent2 = new Intent();
                        intent2.setClass(OpeningActivity.this, LoginActivity.class);
                        startActivity(intent2);
                        finish();

                    }
                });
    }


    //权限请求的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onBackPressed() {

    }
}
