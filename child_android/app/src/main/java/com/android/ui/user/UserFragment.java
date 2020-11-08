package com.android.ui.user;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.PEApplication;
import com.android.bean.User;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.dialog.MessageDialog;
import com.android.ui.BaseActivity;
import com.android.ui.BaseFragment;
import com.android.ui.login.LoginActivity;
import com.android.ui.user.chatSettings.CharSettingActivity;
import com.android.ui.user.messageSettings.MessageSettingActivity;
import com.android.utils.DensityUtil;
import com.android.utils.GlideRoundTransformUtil;
import com.android.utils.SPUtils;
import com.bumptech.glide.Glide;
import com.koi.chat.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 用户主页面
 */
@ContentView(R.layout.fragment_user)
public class UserFragment extends BaseFragment {
    private Activity myActivity;
    @ViewInject(R.id.tv_user_nickName)
    private TextView tvNickName;
    @ViewInject(R.id.tv_user_id)
    private TextView tvUserId;
    @ViewInject(R.id.ll_user_info)
    private LinearLayout llInfo;
    @ViewInject(R.id.ll_user_message)
    private LinearLayout llMessage;
    @ViewInject(R.id.ll_user_chat)
    private LinearLayout llChat;
    @ViewInject(R.id.ll_user_exit)
    private LinearLayout llExit;
    @ViewInject(R.id.iv_user_avatar)
    private ImageView ivAvatar;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        myActivity= (Activity) context;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        setViewListener();
    }
    private void initView(){
        User user = PEApplication.INSTANCE.getUser();
        //获取头像信息
        String url=Constants.AVATAR+user.getHeadPortraitsUrl();
        //设置头像加载失败时的默认头像
        Glide.with(myActivity)
                .load(url)
                .transform(new GlideRoundTransformUtil(myActivity, DensityUtil.dip2px(myActivity, 3)))
                .placeholder(R.mipmap.default_user_avatar)
                .into(ivAvatar);
        tvNickName.setText(user.getName());
        tvUserId.setText(user.getLogin());
    }
    /**
     * 监听事件
     */
    private void setViewListener(){
        //查看信息
        llInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(myActivity,UserInfoActivity.class);
                startActivity(intent);
            }
        });
        //消息设置
        llMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(myActivity, MessageSettingActivity.class);
                startActivity(intent);
            }
        });
        //聊天设置
        llChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(myActivity, CharSettingActivity.class);
                startActivity(intent);
            }
        });
        //退出登录
        llExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialog messageDialog = new MessageDialog(myActivity, R.style.dialog, getString(R.string.text_user_logout_sure), new MessageDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm==true){//确定按钮
                            final RequestParams params = HttpUtil.requestParams("users/logout");
                            x.http().get(params, new Callback.CommonCallback<JSONObject>() {
                                @Override
                                public void onSuccess(JSONObject jsonObject) {
                                    HttpResult httpResult = HttpResult.createWith(jsonObject);
                                    if (httpResult.isSuccess()) {

                                    } else {
                                       // mActivity.toast(httpResult.returnMsg);
                                    }
                                }

                                @Override
                                public void onError(Throwable ex, boolean isOnCallback) {
                                    HttpUtil.onError(params.getUri(), ex);
                                    mActivity.toast(BaseActivity.DEFAULT_HTTP_ERROR);
                                }

                                @Override
                                public void onCancelled(CancelledException cex) {
                                }

                                @Override
                                public void onFinished() {
                                    mActivity.closeDialog();
                                }
                            });
                            Toast.makeText(myActivity, R.string.text_user_logout_succeed,Toast.LENGTH_LONG).show();
                            PEApplication.INSTANCE.userLogoutCallback();//关闭资源
                            Intent intent=new Intent(myActivity, LoginActivity.class);
                            startActivity(intent);
                        }
                        dialog.dismiss();//关闭弹出框
                    }
                });
                messageDialog.setTitle(getString(R.string.text_user_logout_title));
                messageDialog.show();//显示弹出框
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        //刷新用户页面
        initView();
    }
}
