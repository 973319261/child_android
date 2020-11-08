package com.android.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.bean.User;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.dialog.MessageDialog;
import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.android.utils.DateUtil;
import com.android.utils.DensityUtil;
import com.android.utils.GlideRoundTransformUtil;
import com.android.utils.ViewHolder;
import com.android.PEApplication;
import com.android.widgets.BadgeHelper;
import com.android.widgets.CircleImageView;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koi.chat.R;
import com.android.model.Message;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 消息列表适配器
 */
public class MessageAdapter extends ArrayAdapter<Message> {

    private LayoutInflater mInflater;
    private Activity context;
    private Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    public MessageAdapter(Context context, int resource) {
        super(context, resource);
        this.context= (Activity) context;
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_message, null);
        }
        final ImageView userAvatarImg = ViewHolder.get(convertView, R.id.msg_user_avatar_img);
        ImageView ivSeedState = ViewHolder.get(convertView, R.id.iv_seed_state);
        CircleImageView msgHasUnreadImg = ViewHolder.get(convertView, R.id.msg_unread_img);
        final TextView userNameTv = ViewHolder.get(convertView, R.id.msg_user_name_tv);
        TextView timeTv = ViewHolder.get(convertView, R.id.msg_time_tv);
        TextView contentTv = ViewHolder.get(convertView, R.id.msg_content_tv);
        final Message msg = getItem(position);
        String mobile=msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUserLogin()) ? msg.getAcceptUserLogin() : msg.getSendUserLogin();
        String time = new SimpleDateFormat("HH:mm").format(new Date(DateUtil.tsParseToLong(msg.getSendTime())));
        timeTv.setText(time);
        final User user=User.findUser(mobile);//获取本地联系人
        String data="";
        if (user!=null){
            //获取头像信息
            String url= Constants.AVATAR+user.getHeadPortraitsUrl();
            //设置头像加载失败时的默认头像
            Glide.with(context)
                    .load(url)
                    .transform(new GlideRoundTransformUtil(context, DensityUtil.dip2px(context, 3)))
                    .placeholder(R.mipmap.default_user_avatar)
                    .into(userAvatarImg);
            userNameTv.setText(AppService.showUserName(user));
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnMessageClickListener != null) {
                        mOnMessageClickListener.onMessageClick(msg,user);
                    }
                }
            });
        }else {//获取服务用户数据
            final RequestParams params = HttpUtil.requestParams(String.format("users/%s/profile", mobile));
            final View finalConvertView = convertView;
            x.http().get(params, new Callback.CommonCallback<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    HttpResult httpResult = HttpResult.createWith(jsonObject);
                    if (httpResult.isSuccess()) {
                        final User user = gson.fromJson(httpResult.payload.toString(),User.class);
                        String url= Constants.AVATAR+user.getHeadPortraitsUrl();
                        //设置头像加载失败时的默认头像
                        Glide.with(context)
                                .load(url)
                                .transform(new GlideRoundTransformUtil(context, DensityUtil.dip2px(context, 3)))
                                .placeholder(R.mipmap.default_user_avatar)
                                .into(userAvatarImg);
                        userNameTv.setText(AppService.showUserName(user));
                        finalConvertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mOnMessageClickListener != null) {
                                    mOnMessageClickListener.onMessageClick(msg,user);
                                }
                            }
                        });
                    } else {
                        ALog.e(httpResult.code + ":" + httpResult.returnMsg);
                    }
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    HttpUtil.onError(params.getUri(), ex);
                }

                @Override
                public void onCancelled(CancelledException cex) {
                }

                @Override
                public void onFinished() {
                }
            });
        }
        if (!msg.isSendSuccess()){//发送失败
            ivSeedState.setVisibility(View.VISIBLE);//显示
            ivSeedState.setImageResource(R.drawable.ic_seed_fail);
        }else {//发送成功
            ivSeedState.setVisibility(View.GONE);//隐藏
        }
        if (!msg.isRead() && !msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUserLogin())){//已读
            msgHasUnreadImg.setVisibility(View.VISIBLE);

        }else {//未读
            msgHasUnreadImg.setVisibility(View.GONE);
        }
        contentTv.setText(msg.getContent());
        //删除
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MessageDialog messageDialog = new MessageDialog(context, R.style.dialog, "删除后，将清空该聊天的消息记录", new MessageDialog.OnCloseListener() {
                    @Override
                    public void onClick(Dialog dialog, boolean confirm) {
                        if (confirm == true) {//确定按钮
                            Message.delete(mobile);
                            remove(msg);
                            notifyDataSetChanged();//刷新
                        }
                        dialog.dismiss();//关闭弹出框
                    }
                });
                messageDialog.setTitle("删除");
                messageDialog.show();//显示弹出框
                return true;
            }
        });
        return convertView;
    }

    private OnMessageClickListener mOnMessageClickListener;

    public void setOnMessageClickListener(OnMessageClickListener mOnMessageClickListener) {
        this.mOnMessageClickListener = mOnMessageClickListener;
    }

    public interface OnMessageClickListener {
        void onMessageClick(Message msg,User user);
    }
}
