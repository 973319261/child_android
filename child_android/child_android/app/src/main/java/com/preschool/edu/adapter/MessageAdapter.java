package com.preschool.edu.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.utils.DateUtil;
import com.android.utils.ViewHolder;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.model.Message;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jac_cheng on 2018/1/20.
 */

public class MessageAdapter extends ArrayAdapter<Message> {

    private LayoutInflater mInflater;

    public MessageAdapter(Context context, int resource) {
        super(context, resource);
        this.mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_message, null);
        }
        ImageView userAvatarImg = ViewHolder.get(convertView, R.id.msg_user_avatar_img);
        TextView userNameTv = ViewHolder.get(convertView, R.id.msg_user_name_tv);
        TextView timeTv = ViewHolder.get(convertView, R.id.msg_time_tv);
        TextView contentTv = ViewHolder.get(convertView, R.id.msg_content_tv);

        final Message msg = getItem(position);
        userNameTv.setText(msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUserLogin()) ? msg.getAcceptUserLogin() : msg.getSendUserLogin());
        String time = new SimpleDateFormat("HH:mm").format(new Date(DateUtil.tsParseToLong(msg.getSendTime())));
        timeTv.setText(time);
        contentTv.setText(msg.getContent());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnMessageClickListener != null) {
                    mOnMessageClickListener.onMessageClick(msg);
                }
            }
        });
        return convertView;
    }

    private OnMessageClickListener mOnMessageClickListener;

    public void setOnMessageClickListener(OnMessageClickListener mOnMessageClickListener) {
        this.mOnMessageClickListener = mOnMessageClickListener;
    }

    public interface OnMessageClickListener {
        void onMessageClick(Message msg);
    }
}
