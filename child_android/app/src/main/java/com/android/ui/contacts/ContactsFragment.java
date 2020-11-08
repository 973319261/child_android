package com.android.ui.contacts;

import android.os.Bundle;

import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.bean.User;
import com.android.ui.BaseFragment;
import com.android.utils.AppUtil;
import com.android.widgets.SideBar;
import com.android.PEApplication;
import com.google.gson.Gson;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.adapter.ContactAdapter;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * 通讯录页面
 */
@ContentView(R.layout.fragment_contacts)
public class ContactsFragment extends BaseFragment implements ContactAdapter.OnContactListener {

    @ViewInject(R.id.sideBar)
    private SideBar sideBar;
    @ViewInject(R.id.listview)
    private ListView mListView;
    private ContactAdapter mAdapter;
    private Gson gson=new Gson().newBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNaviBar(false, getString(R.string.text_main_contacts), true);
        mActivity.addTextViewByIdAndStr(mView, R.id.navi_right_txt, getString(R.string.text_contacts_add));

        mAdapter = new ContactAdapter(mActivity);
        mAdapter.setOnContactClickListener(this);
        mListView.setAdapter(mAdapter);
        sideBar.setTypeEnum(SideBar.TypeEnum.Contacts);
        sideBar.setListView(mListView);
        loadContacts(false);
    }

    @Event(value = {R.id.navi_right_layout})
    private void onBtnsClick(View view) {
        switch (view.getId()) {
            case R.id.navi_right_layout:
                mActivity.open(AddFriendsActivity.class);
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
            loadContacts(false);//更新本地通讯录
        }else if (messageEvent.getMsgType() == Constants.EVENT_MSG_TYPE_CONTACT_REFRESH_SERVER){
            loadContacts(true);//更新服务器通讯录
        }
    }

    private void loadContacts(boolean isServer) {
        mActivity.showDialog();
        if (isServer){
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
                        JSONObject json=new JSONObject();
                        try {
                            json.put("data",httpResult.payload);
                            JSONObject data=jsonObject.getJSONObject("data");
                            JSONArray contactDatas = data.getJSONArray("content");
                            User.deleteAll();//先清空数据
                            List<User> lists = AppService.parseContacts(contactDatas);
                            for (User user:lists){//保存联系人到本地
                                user.insert();
                            }
                            List<JSONObject> list=new ArrayList<>();
                            if (lists!=null){
                                for (User user:lists){
                                    list.add(AppUtil.toJsonObject(gson.toJson(user)));
                                }
                                mAdapter.setup(list);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Toast.makeText(mActivity,httpResult.returnMsg,Toast.LENGTH_LONG).show();
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
        }else {
            try {
                List<User> lists=User.getAll();
                List<JSONObject> list=new ArrayList<>();
                if (lists!=null){
                    for (User user:lists){
                        list.add(AppUtil.toJsonObject(gson.toJson(user)));
                    }
                    mAdapter.setup(list);
                }
            } catch (DbException e) {
                e.printStackTrace();
            }
        }
        mActivity.closeDialog();

    }

    @Override
    public void onSearchEditViewChange(boolean isEmpty) {
        sideBar.setVisibility(isEmpty ? View.VISIBLE : View.VISIBLE);
    }

    @Override
    public void onHeaderButtonClick(ContactAdapter.HeaderBtnType btnType) {
        if (btnType == ContactAdapter.HeaderBtnType.NewBuddy) {
            mActivity.open(NewFriendsActivity.class);
        } else if (btnType == ContactAdapter.HeaderBtnType.GroupChat) {

        }
    }

    @Override
    public void onContactItemClick(JSONObject contact) {
        mActivity.open(FriendsInfoActivity.class, "user", contact);
    }

}
