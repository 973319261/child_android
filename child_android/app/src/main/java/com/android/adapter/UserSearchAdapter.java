package com.android.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.core.Constants;
import com.android.utils.DensityUtil;
import com.android.utils.JsonArrayAdapter;
import com.android.utils.ViewHolder;
import com.bumptech.glide.Glide;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.core.AppService;
import com.android.utils.GlideRoundTransformUtil;

import org.json.JSONObject;

/**
 * 搜索用户适配器
 */
public class UserSearchAdapter extends JsonArrayAdapter implements View.OnClickListener {

    private BaseActivity baseActivity;

    public UserSearchAdapter(Activity context) {
        super(context);
        this.baseActivity = (BaseActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_user_search, null);
        }
        ImageView avatarImg = ViewHolder.get(convertView, R.id.user_avatar_img);
        TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

        final JSONObject each = (JSONObject) getItem(position);
        //获取头像信息
        String url= Constants.AVATAR+each.optString("headPortraitsUrl");
        //设置头像加载失败时的默认头像
        Glide.with(baseActivity)
                .load(url)
                .transform(new GlideRoundTransformUtil(baseActivity, DensityUtil.dip2px(baseActivity, 3)))
                .placeholder(R.mipmap.default_user_avatar)
                .into(avatarImg);
        userNameTv.setText(AppService.showUserName(each));
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(each);
                }
            }
        });
        return convertView;
    }

    @Override
    public void onClick(View v) {
    }

}
