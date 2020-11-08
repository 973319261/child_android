package com.android.ui.contacts;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.bean.User;
import com.android.model.Message;
import com.android.ui.message.ChatActivity;
import com.android.utils.AppUtil;
import com.android.utils.DensityUtil;
import com.android.utils.StatusBarUtil;
import com.bumptech.glide.Glide;
import com.android.PEApplication;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.utils.GlideRoundTransformUtil;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.HttpMethod;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.InputStream;
import java.util.Locale;

/**
 * 好友信息
 */
@ContentView(R.layout.activity_friends_info)
public class FriendsInfoActivity extends BaseActivity {

    @ViewInject(R.id.user_avatar_img)
    private ImageView userAvatarImg;
    @ViewInject(R.id.iv_user_sex)
    private ImageView ivSex;
    private JSONObject user;
    private String url;
    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, getString(R.string.text_friends_info_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBar(this,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(this,true);//状态栏文字颜色
        user = AppUtil.toJsonObject(getIntentString("user"));
        initNaviHeadView();
        if ("1".equals(user.optString("sex"))){
            ivSex.setImageResource(R.drawable.ic_sex_woman);
        }else {
            ivSex.setImageResource(R.drawable.ic_sex_man);
        }
        //获取头像信息
        url=Constants.AVATAR+user.optString("headPortraitsUrl");
        //设置头像加载失败时的默认头像
        Glide.with(this)
                .load(url)
                .transform(new GlideRoundTransformUtil(this, DensityUtil.dip2px(this, 3)))
                .placeholder(R.mipmap.default_user_avatar)
                .into(userAvatarImg);
        addTextViewByIdAndStr(R.id.user_name_tv, AppService.showUserName(user));
        addTextViewByIdAndStr(R.id.user_login, String.format(Locale.getDefault(),getString(R.string.text_user_account)+"%s ",user.optString("login")));
        addTextViewByIdAndStr(R.id.tv_user_signature, user.optString("remark").equals("null")?"":user.optString("remark"));
        try {
            if (!"".equals(user.optString("pinyin")) || user.optInt("id")==0){//从好友列表进入
                if (!user.optString("login").equals(PEApplication.INSTANCE.getUserLogin())){
                    showViewById(R.id.user_remove_buddy_btn);
                    showViewById(R.id.user_send_msg_btn);
                }else {
                    hideViewId(R.id.user_remove_buddy_btn,true);
                    hideViewId(R.id.user_send_msg_btn, true);
                }
                hideViewId(R.id.user_apply_add_buddy_btn, true);
            }else {
                checkIsBuddy();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Event(value = {R.id.user_apply_add_buddy_btn, R.id.user_remove_buddy_btn, R.id.user_send_msg_btn,R.id.user_avatar_img})
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.user_apply_add_buddy_btn:
                tryApplyAddBuddy();
                break;
            case R.id.user_remove_buddy_btn:
                alertWithCancel(getString(R.string.text_friends_info_delete_sure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tryRemoveBuddy();
                    }
                });
                break;
            case R.id.user_send_msg_btn:
                open(ChatActivity.class, "user", user);
                break;
            case  R.id.user_avatar_img:
                // 全屏显示的方法
                final Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
                ImageView imgView = getView();
                dialog.setContentView(imgView);
                dialog.show();
                imgView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;
        }
    }
    private ImageView getView() {
        final ImageView imgView = new ImageView(this);
        Glide.with(this)
                .load(url)
                .fitCenter()
                .placeholder(R.mipmap.default_user_avatar)
                .into(imgView);

        return imgView;
    }
    private void checkIsBuddy() throws JSONException {
        showDialog();
        final RequestParams params = HttpUtil.requestParams("friendships/check");
        params.addBodyParameter("userInfoId1", user.optString("id"));
        params.addBodyParameter("userInfoId2", PEApplication.INSTANCE.getUserInfoId());
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    boolean isBuddy = (boolean) httpResult.payload;
                    if (isBuddy) {
                        showViewById(R.id.user_remove_buddy_btn);
                        showViewById(R.id.user_send_msg_btn);
                        hideViewId(R.id.user_apply_add_buddy_btn, true);
                    } else {
                        showViewById(R.id.user_apply_add_buddy_btn);
                        hideViewId(R.id.user_remove_buddy_btn,true);
                        hideViewId(R.id.user_send_msg_btn, true);
                    }
                } else {
                    toast(httpResult.returnMsg);
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ex.printStackTrace();
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

    private void tryApplyAddBuddy() {
        showDialog();
        JSONObject json = new JSONObject();
        AppUtil.addKeyValue2JsonObject(json, "acceptUserInfoId", user.optString("id"));
        AppUtil.addKeyValue2JsonObject(json, "sendUserUserInfoId", PEApplication.INSTANCE.getUserInfoId());
        final RequestParams params = HttpUtil.requestParams("friendships/send");
        params.setAsJsonContent(true);
        params.setBodyContent(json.toString());
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    toast(getString(R.string.text_friends_info_apply_succeed));
                    //hideViewId(R.id.user_apply_add_buddy_btn, true);
                    finish();
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

    private void tryRemoveBuddy() {
        showDialog();
        String deleteUrl = String.format("friends/delete?userFriendId=%s", user.optString("id"));
        final RequestParams params = HttpUtil.requestParams(deleteUrl);
        x.http().request(HttpMethod.DELETE, params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    toast(getString(R.string.text_friends_info_delete_succeed));
                    goMain();
                    Message.delete(user.optString("login"));//删除该用户的聊天记录
                    EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CHAT_RECEIVED, "更新聊天列表"));
                    EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH_SERVER, "更新通讯录"));
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
}
