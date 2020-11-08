package com.preschool.edu.activity.buddy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.android.utils.AppUtil;
import com.android.utils.DensityUtil;
import com.bumptech.glide.Glide;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.AppService;
import com.preschool.edu.core.Constants;
import com.preschool.edu.core.GlideRoundTransform;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;
import com.preschool.edu.model.MessageEvent;

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

/**
 * Created by jac_cheng on 2018/1/18.
 */
@ContentView(R.layout.activity_user_profile)
public class UserProfileActivity extends BaseActivity {

    @ViewInject(R.id.user_avatar_img)
    private ImageView userAvatarImg;
    private JSONObject user;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, "个人信息");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = AppUtil.toJsonObject(getIntentString("user"));
        initNaviHeadView();

        Glide.with(INSTANCE).load(user.optString("headPortraitsUrl")).transform(new GlideRoundTransform(INSTANCE, DensityUtil.dip2px(INSTANCE, 5))).placeholder(R.mipmap.default_user_avatar).into(userAvatarImg);
        addTextViewByIdAndStr(R.id.user_name_tv, AppService.showUserName(user));
        hideViewId(R.id.user_apply_add_buddy_btn, true);
        hideViewId(R.id.user_remove_buddy_btn, true);
        try {
            checkIsBuddy();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Event(value = {R.id.user_apply_add_buddy_btn, R.id.user_remove_buddy_btn, R.id.user_send_msg_btn})
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.user_apply_add_buddy_btn:
                tryApplyAddBuddy();
                break;
            case R.id.user_remove_buddy_btn:
                alertWithCancel("确定要删除该好友吗？", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tryRemoveBuddy();
                    }
                });
                break;
            case R.id.user_send_msg_btn:
                open(ChatActivity.class, "user", user);
                break;
        }
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
                    } else {
                        showViewById(R.id.user_apply_add_buddy_btn);
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
                    toast("申请成功");
                    hideViewId(R.id.user_apply_add_buddy_btn, true);
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
        String deleteUrl = String.format("friends/delete?userInfoId1=%s&userInfoId2=%s", user.optString("id"), PEApplication.INSTANCE.getUserInfoId());
        final RequestParams params = HttpUtil.requestParams(deleteUrl);
        x.http().request(HttpMethod.DELETE, params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    toast("删除成功");
                    hideViewId(R.id.user_remove_buddy_btn, true);
                    showViewById(R.id.user_apply_add_buddy_btn);
                    EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH, "更新通讯录"));
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
