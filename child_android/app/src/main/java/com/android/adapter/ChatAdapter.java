package com.android.adapter;

import android.app.Dialog;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.bean.Messages;
import com.android.bean.User;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.dialog.MessageDialog;
import com.android.ui.contacts.FriendsInfoActivity;
import com.android.ui.login.LoginActivity;
import com.android.ui.user.UserInfoActivity;
import com.android.utils.AppUtil;
import com.android.utils.DateUtil;
import com.android.utils.DensityUtil;
import com.android.utils.GlideRoundTransformUtil;
import com.android.utils.OkHttpTool;
import com.android.utils.ViewHolder;
import com.android.PEApplication;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.model.Message;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 聊天界面列表适配器
 */
public class ChatAdapter extends BaseAdapter {

    public final static int CHAT_LEFT = 0;
    public final static int CHAT_RIGHT = 1;
    private BaseActivity baseActivity;
    private LayoutInflater mInflater;
    private List<Message> messages;
    private SimpleDateFormat format;
    private JSONObject buddy;
    private Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    public ChatAdapter(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
        this.mInflater = LayoutInflater.from(baseActivity);
    }

    public void fillMessages(List<Message> messages) {
        this.messages = messages;
        this.notifyDataSetChanged();
    }

    public void addMessages(Message msg) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(msg);
        this.notifyDataSetChanged();
    }
    public void refresh(Message msg){
        this.messages.set(messages.size()-1,msg);
        this.notifyDataSetChanged();
    }
    //设置对方头像
    public void setAcceptInfo(JSONObject buddy){
        this.buddy=buddy;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            switch (getItemViewType(position)) {
                case CHAT_LEFT:
                    convertView = mInflater.inflate(R.layout.adapter_chat_receiver, null);
                    break;
                case CHAT_RIGHT:
                    convertView = mInflater.inflate(R.layout.adapter_chat_send, null);

                    break;
            }
        }
        TextView sendTimeTv = ViewHolder.get(convertView, R.id.chat_send_time_tv);
        ImageView userAvatarImg = ViewHolder.get(convertView, R.id.chat_user_avatar_img);
        ImageView ivSeedState = ViewHolder.get(convertView, R.id.iv_seed_state);//发送状态图标
        TextView userNameTv = ViewHolder.get(convertView, R.id.chat_user_name_tv);
        TextView contentTv = ViewHolder.get(convertView, R.id.chat_content_tv);

        final Message message = (Message) getItem(position);
        final User user=new User();
        //如果不是第一个Item且发送消息的两次时间在3min之内，则不再显示时间；否则显示时间
        if (position != 0) {
            Message dataBefore = (Message) getItem(position - 1);
            long dateDifference = DateUtil.tsParseToLong(message.getSendTime()) - DateUtil.tsParseToLong(dataBefore.getSendTime());
            if (dateDifference < 1000*60*3) {
                sendTimeTv.setVisibility(View.GONE);
            } else {
                format = new SimpleDateFormat("EEE HH:mm");
                String time = format.format(new Date(DateUtil.tsParseToLong(message.getSendTime())));
                sendTimeTv.setText(time);
            }
        } else {
            format = new SimpleDateFormat("EEE HH:mm");
            String time = format.format(new Date(DateUtil.tsParseToLong(message.getSendTime())));
            sendTimeTv.setText(time);
        }
        user.setLogin(message.getSendUserLogin());
        contentTv.setText(Html.fromHtml(message.getContent()));//设置内容
        String currentUserLogin = PEApplication.INSTANCE.getUser().getLogin();
        switch (getItemViewType(position)) {
            case CHAT_LEFT:
                //设置头像加载失败时的默认头像
                String avatar = Constants.AVATAR+buddy.optString("headPortraitsUrl");
                user.setHeadPortraitsUrl(buddy.optString("headPortraitsUrl"));
                user.setId(buddy.optInt("id"));
                user.setName(buddy.optString("name"));
                user.setSex(buddy.optString("sex"));
                user.setRemark(buddy.optString("remark"));
                Glide.with(baseActivity)
                        .load(avatar)
                        .transform(new GlideRoundTransformUtil(baseActivity, DensityUtil.dip2px(baseActivity, 3)))
                        .placeholder(R.mipmap.default_user_avatar)
                        .into(userAvatarImg);
                userNameTv.setText(message.getSendUserLogin());
                break;
            case CHAT_RIGHT:
                //获取头像信息
                String url= Constants.AVATAR+PEApplication.INSTANCE.getUser().getHeadPortraitsUrl();
                //设置头像加载失败时的默认头像
                user.setHeadPortraitsUrl(PEApplication.INSTANCE.getUser().getHeadPortraitsUrl());
                user.setName(PEApplication.INSTANCE.getUser().getName());
                user.setSex(PEApplication.INSTANCE.getUser().getSex());
                user.setRemark(PEApplication.INSTANCE.getUser().getRemark());
                Glide.with(baseActivity)
                        .load(url)
                        .transform(new GlideRoundTransformUtil(baseActivity, DensityUtil.dip2px(baseActivity, 3)))
                        .placeholder(R.mipmap.default_user_avatar)
                        .into(userAvatarImg);
                userNameTv.setText(currentUserLogin);
                TextView tvState=ViewHolder.get(convertView,R.id.tv_message_state);
                TextView tvValidation=ViewHolder.get(convertView,R.id.tv_friends_validation);
                LinearLayout friendsValidation = ViewHolder.get(convertView, R.id.ll_friends_validation);
                friendsValidation.setVisibility(View.GONE);
                if (ivSeedState!=null){
                    if (message.getSendState()==0){//发送中
                        ivSeedState.setImageResource(R.drawable.ic_seed_load);
                        ivSeedState.setVisibility(View.VISIBLE);
                        tvState.setVisibility(View.GONE);
                    }else if (message.getSendState()==1 || message.isSendSuccess()){//发送成功
                        ivSeedState.setVisibility(View.GONE);
                        tvState.setVisibility(View.VISIBLE);
                        if (message.isRead()){
                            tvState.setText("已读");
                        }else {
                            tvState.setText("未读");
                        }
                    }else if (message.getSendState()==2|| message.getSendState()==3 || !message.isSendSuccess()){//发送失败
                        if (message.getSendState()==3){
                            friendsValidation.setVisibility(View.VISIBLE);
                        }
                        ivSeedState.setImageResource(R.drawable.ic_seed_fail);
                        ivSeedState.setVisibility(View.VISIBLE);
                        tvState.setVisibility(View.GONE);
                        ivSeedState.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                User user = User.findUser(message.getAcceptUserLogin());
                                if (user!=null){
                                    final Messages msg1 = new Messages();
                                    msg1.setSendTime(DateUtil.formatAllDateTime(System.currentTimeMillis()));
                                    msg1.setSubscribeValue(0);//未读
                                    msg1.setContent(message.getContent());
                                    msg1.setAcceptUserLogin(message.getAcceptUserLogin());
                                    msg1.setSendUserLogin(PEApplication.INSTANCE.getUserLogin());
                                    msg1.setType(0);
                                    message.setSendSuccess(true);
                                    message.setRead(false);
                                    message.setSendState(0);//发送中
                                    final RequestParams params = HttpUtil.requestParams("messages/sendToUser");
                                    OkHttpTool.httpPostJson(params.toString(), gson.toJson(msg1), new OkHttpTool.ResponseCallback() {
                                        @Override
                                        public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                                            baseActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (isSuccess && responseCode==200){
                                                        Log.i("response",response);
                                                        try {
                                                            JSONObject jsonObject=new JSONObject(response);
                                                            HttpResult httpResult = HttpResult.createWith(jsonObject);
                                                            if (httpResult.isSuccess()) {
                                                                message.setSendSuccess(true);
                                                                message.setSendState(1);//发送成功
                                                                refresh(message);
                                                                message.save();//保存到本地
                                                            } else {
                                                                message.setSendSuccess(false);
                                                                message.setSendState(2);//发送失败
                                                                baseActivity.toast(httpResult.returnMsg);
                                                                refresh(message);
                                                                message.save();//保存到本地
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }

                                                    }else {
                                                        message.setSendSuccess(false);
                                                        message.setSendState(2);//发送失败
                                                        refresh(message);
                                                        message.save();//保存到本地
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }else {
                                    message.setSendSuccess(false);
                                    message.setSendState(3);//不是好友
                                    refresh(message);
                                    messages.add(message);
                                    message.save();//保存到本地
                                }
                            }
                        });
                        //朋友验证
                        tvValidation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                MessageDialog messageDialog1 = new MessageDialog(baseActivity, R.style.dialog, baseActivity.getResources().getString(R.string.text_is_add_friend), new MessageDialog.OnCloseListener() {
                                    @Override
                                    public void onClick(Dialog dialog, boolean confirm) {
                                        if (confirm==true){//确定按钮
                                            baseActivity.showDialog();
                                            JSONObject json = new JSONObject();
                                            AppUtil.addKeyValue2JsonObject(json, "acceptUserInfoId", buddy.optString("id"));
                                            AppUtil.addKeyValue2JsonObject(json, "sendUserUserInfoId", PEApplication.INSTANCE.getUserInfoId());
                                            final RequestParams params = HttpUtil.requestParams("friendships/send");
                                            params.setAsJsonContent(true);
                                            params.setBodyContent(json.toString());
                                            x.http().post(params, new Callback.CommonCallback<JSONObject>() {
                                                @Override
                                                public void onSuccess(JSONObject jsonObject) {
                                                    HttpResult httpResult = HttpResult.createWith(jsonObject);
                                                    if (httpResult.isSuccess()) {
                                                        baseActivity.toast(baseActivity.getResources().getString(R.string.text_friends_info_apply_succeed));
                                                    } else {
                                                        baseActivity.toast(httpResult.returnMsg);
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable ex, boolean isOnCallback) {
                                                    HttpUtil.onError(params.getUri(), ex);
                                                    baseActivity.toast(BaseActivity.DEFAULT_HTTP_ERROR);
                                                }

                                                @Override
                                                public void onCancelled(CancelledException cex) {
                                                }

                                                @Override
                                                public void onFinished() {
                                                }
                                            });
                                        }
                                        baseActivity.closeDialog();
                                        dialog.dismiss();//关闭弹出框
                                    }
                                });
                                messageDialog1.setTitle(baseActivity.getResources().getString(R.string.text_friend_authentication));
                                messageDialog1.show();//显示弹出框
                            }
                        });
                    }
                }

                break;
        }
        userAvatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseActivity.open(FriendsInfoActivity.class,"user", AppUtil.toJsonObject(gson.toJson(user)));
            }
        });
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = (Message) getItem(position);
        if (msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUser().getLogin())) {
            return CHAT_RIGHT;
        } else {
            return CHAT_LEFT;
        }
    }

    @Override
    public int getCount() {
        if (messages != null) {
            return messages.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (messages != null) {
            return messages.get(position);
        }
        return null;
    }
}
