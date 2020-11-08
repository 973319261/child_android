package com.preschool.edu.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.utils.AppUtil;
import com.android.utils.DensityUtil;
import com.android.utils.JsonArrayAdapter;
import com.android.utils.ViewHolder;
import com.bumptech.glide.Glide;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.AppService;
import com.preschool.edu.core.GlideRoundTransform;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by jac_cheng on 2018/1/18.
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
        Glide.with(baseActivity).load(each.optString("headPortraitsUrl")).transform(new GlideRoundTransform(baseActivity, DensityUtil.dip2px(baseActivity, 5))).placeholder(R.mipmap.default_user_avatar).into(avatarImg);
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
