package com.android.ui.contacts;

import android.os.Bundle;
import android.widget.ListView;

import com.android.core.AppService;
import com.android.utils.JsonArrayAdapter;
import com.android.utils.StatusBarUtil;
import com.android.widgets.pullrefresh.PullToRefreshBase;
import com.android.widgets.pullrefresh.PullToRefreshListView;
import com.android.PEApplication;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.adapter.NewBuddyAdapter;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 新的好友
 */
@ContentView(R.layout.activity_new_friends)
public class NewFriendsActivity extends BaseActivity implements JsonArrayAdapter.OnItemClickListener {

    @ViewInject(R.id.pull_to_refresh_listview)
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private NewBuddyAdapter mAdapter;
    private boolean taskRunning;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, getString(R.string.text_new_friends_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setStatusBar(this,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(this,true);//状态栏文字颜色
        initNaviHeadView();
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                if (!taskRunning && isNetOk()) {
                    loadDatas();
                } else {
                    mPullToRefreshListView.onRefreshComplete();
                }
            }
        });

        mListView = mPullToRefreshListView.getRefreshableView();
        mAdapter = new NewBuddyAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        mPullToRefreshListView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshListView.setRefreshing(true);
            }
        }, 200);
    }

    private void loadDatas() {
        final RequestParams params = HttpUtil.requestParams("friendships/incoming");
        params.addBodyParameter("acceptUserId", PEApplication.INSTANCE.getUserInfoId());
        params.addBodyParameter("status", "1");
        params.addBodyParameter("page", "0");
        params.addBodyParameter("size", "1000");
        HttpUtil.printUrl(params);
        taskRunning = true;
        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    JSONObject json=new JSONObject();
                    try {
                        json.put("data",httpResult.payload);
                        JSONObject data=jsonObject.getJSONObject("data");
                        JSONArray content = data.getJSONArray("content");
                        mAdapter.fillNewData(content);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                taskRunning = false;
                mPullToRefreshListView.onRefreshComplete();
            }
        });
    }

    @Override
    public void onItemClick(JSONObject data) {
        open(FriendsInfoActivity.class, "user", data);
    }


}
