package com.preschool.edu.activity.buddy;

import android.os.Bundle;
import android.widget.ListView;

import com.android.utils.JsonArrayAdapter;
import com.android.widgets.pullrefresh.PullToRefreshBase;
import com.android.widgets.pullrefresh.PullToRefreshListView;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.adapter.NewBuddyAdapter;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * Created by jac_cheng on 2017/12/27.
 */

@ContentView(R.layout.activity_signle_pull_to_refresh_listview)
public class NewBuddyListActivity extends BaseActivity implements JsonArrayAdapter.OnItemClickListener {

    @ViewInject(R.id.pull_to_refresh_listview)
    private PullToRefreshListView mPullToRefreshListView;
    private ListView mListView;
    private NewBuddyAdapter mAdapter;
    private boolean taskRunning;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, "新的好友");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                    JSONArray datas = (JSONArray) httpResult.payload;
                    mAdapter.fillNewData(datas);
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
        open(UserProfileActivity.class, "data", data);
    }


}
