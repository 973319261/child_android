package com.preschool.edu.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.utils.DateUtil;
import com.android.utils.DensityUtil;
import com.android.utils.JsonArrayAdapter;
import com.android.utils.ViewHolder;
import com.bumptech.glide.Glide;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.core.AppService;
import com.preschool.edu.core.Constants;
import com.preschool.edu.core.GlideRoundTransform;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;
import com.preschool.edu.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by jac_cheng on 2018/1/18.
 */
public class NewBuddyAdapter extends JsonArrayAdapter implements View.OnClickListener {

    private BaseActivity baseActivity;

    public NewBuddyAdapter(Activity context) {
        super(context);
        this.baseActivity = (BaseActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_new_buddy, null);
        }
        ImageView avatarImg = ViewHolder.get(convertView, R.id.user_avatar_img);
        TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
        TextView applyTimeTv = ViewHolder.get(convertView, R.id.user_apply_time_tv);
        TextView agreeBtn = ViewHolder.get(convertView, R.id.user_agree_btn);
        TextView rejectBtn = ViewHolder.get(convertView, R.id.user_reject_btn);

        final JSONObject each = (JSONObject) getItem(position);
        JSONObject applyUser = each.optJSONObject("sendUser");
        Glide.with(baseActivity).load(applyUser.optString("headPortraitsUrl")).transform(new GlideRoundTransform(baseActivity, DensityUtil.dip2px(baseActivity, 5))).placeholder(R.mipmap.default_user_avatar).into(avatarImg);
        userNameTv.setText(AppService.showUserName(applyUser));
        applyTimeTv.setText(String.format("申请时间: %s", DateUtil.showTime(each.optString("createTime"))));
        agreeBtn.setTag(each);
        rejectBtn.setTag(each);
        agreeBtn.setOnClickListener(this);
        rejectBtn.setOnClickListener(this);
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
        JSONObject data = (JSONObject) v.getTag();
        if (v.getId() == R.id.user_agree_btn) {
            tryDoBuddyApply(data, false);
        } else if (v.getId() == R.id.user_reject_btn) {
            tryDoBuddyApply(data, true);
        }
    }

    private void tryDoBuddyApply(final JSONObject data, final boolean reject) {
        baseActivity.showDialog();
        final RequestParams params = HttpUtil.requestParams("friendships/accept");
        params.addBodyParameter("addUserFriendId", data.optString("id"));
        params.addBodyParameter("status", reject ? "3" : "2");
        x.http().post(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    baseActivity.toast("操作成功");
                    JSONArray freshDatas = new JSONArray();
                    for (int i = 0; i < getCount(); i++) {
                        JSONObject each = (JSONObject) getItem(i);
                        if (each.optInt("id") == data.optInt("id")) {
                            continue;
                        }
                        freshDatas.put(each);
                    }
                    fillNewData(freshDatas);
                    if (!reject) {
                        EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CONTACT_REFRESH, "更新通讯录"));
                    }
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
                baseActivity.closeDialog();
            }
        });
    }
}
