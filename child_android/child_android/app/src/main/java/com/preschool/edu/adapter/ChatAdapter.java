package com.preschool.edu.adapter;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.utils.DateUtil;
import com.android.utils.ViewHolder;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.model.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jac_cheng on 2018/1/18.
 */

public class ChatAdapter extends BaseAdapter {

    public final static int CHAT_LEFT = 0;
    public final static int CHAT_RIGHT = 1;
    private BaseActivity baseActivity;
    private LayoutInflater mInflater;
    private List<Message> messages;
    private SimpleDateFormat format;

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
        TextView userNameTv = ViewHolder.get(convertView, R.id.chat_user_name_tv);
        TextView contentTv = ViewHolder.get(convertView, R.id.chat_content_tv);

        Message message = (Message) getItem(position);
        //如果不是第一个Item且发送消息的两次时间在1min之内，则不再显示时间；否则显示时间
        if (position != 0) {
            Message dataBefore = (Message) getItem(position - 1);
            long dateDifference = DateUtil.tsParseToLong(message.getSendTime()) - DateUtil.tsParseToLong(dataBefore.getSendTime());
            if (dateDifference < 60000) {
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
        contentTv.setText(Html.fromHtml(message.getContent()));
        String currentUserLogin = PEApplication.INSTANCE.getUserInfo().optString("login");
        switch (getItemViewType(position)) {
            case CHAT_LEFT:
                userNameTv.setText(message.getSendUserLogin());
                break;
            case CHAT_RIGHT:
                userNameTv.setText(currentUserLogin);
                break;
        }
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = (Message) getItem(position);
        if (!msg.isSend()) {
            return CHAT_RIGHT;
        }
        if (msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUserInfo().optString("login"))) {
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
