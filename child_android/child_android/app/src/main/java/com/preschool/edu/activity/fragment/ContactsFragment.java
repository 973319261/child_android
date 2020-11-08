package com.preschool.edu.activity.fragment;

import android.os.Bundle;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.android.widgets.SideBar;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.activity.buddy.ChatActivity;
import com.preschool.edu.activity.buddy.NewBuddyListActivity;
import com.preschool.edu.activity.buddy.UserProfileActivity;
import com.preschool.edu.activity.buddy.UserSearchActivity;
import com.preschool.edu.adapter.ContactAdapter;
import com.preschool.edu.core.AppService;
import com.preschool.edu.core.Constants;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;
import com.preschool.edu.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by jac_cheng on 2017/12/27.
 */
@ContentView(R.layout.fragment_contacts)
public class ContactsFragment extends BaseFragment implements ContactAdapter.OnContactListener {

    @ViewInject(R.id.sideBar)
    private SideBar sideBar;
    @ViewInject(R.id.listview)
    private ListView mListView;
    private ContactAdapter mAdapter;
    private JSONArray contactDatas;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNaviBar(false, "通讯录", true);
        mActivity.addTextViewByIdAndStr(mView, R.id.navi_right_txt, "添加");

        mAdapter = new ContactAdapter(mActivity);
        mAdapter.setOnContactClickListener(this);
        mListView.setAdapter(mAdapter);
        sideBar.setTypeEnum(SideBar.TypeEnum.Contacts);
        sideBar.setListView(mListView);

        loadContacts();
    }

    @Event(value = {R.id.navi_right_layout})
    private void onBtnsClick(View view) {
        switch (view.getId()) {
            case R.id.navi_right_layout:
                mActivity.open(UserSearchActivity.class);
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshContacts(MessageEvent messageEvent) {
        if (messageEvent.getMsgType() == Constants.EVENT_MSG_TYPE_CONTACT_REFRESH) {
            loadContacts();
        }
    }

    private void loadContacts() {
        mActivity.showDialog();
        final RequestParams params = HttpUtil.requestParams("friends/list");
        params.addBodyParameter("userInfoId", PEApplication.INSTANCE.getUserInfoId());
        params.addBodyParameter("page", "0");
        params.addBodyParameter("size", "10000");
        HttpUtil.printUrl(params);
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    contactDatas = (JSONArray) httpResult.payload;
                    mAdapter.setup(AppService.parseContacts(contactDatas));
                } else {
                    mActivity.toast(httpResult.returnMsg);
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
    }

    @Override
    public void onSearchEditViewChange(boolean isEmpty) {
        sideBar.setVisibility(isEmpty ? View.VISIBLE : View.VISIBLE);
    }

    @Override
    public void onHeaderButtonClick(ContactAdapter.HeaderBtnType btnType) {
        if (btnType == ContactAdapter.HeaderBtnType.NewBuddy) {
            mActivity.open(NewBuddyListActivity.class);
        } else if (btnType == ContactAdapter.HeaderBtnType.GroupChat) {

        }
    }

    @Override
    public void onContactItemClick(JSONObject contact) {
        mActivity.open(UserProfileActivity.class, "user", contact);
    }
}
