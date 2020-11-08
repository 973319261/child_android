package com.preschool.edu.activity.buddy;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;

import com.android.utils.JsonArrayAdapter;
import com.android.widgets.ClearableEditText;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.activity.user.ValidCodeActivity;
import com.preschool.edu.adapter.UserSearchAdapter;
import com.preschool.edu.core.HttpResult;
import com.preschool.edu.core.HttpUtil;

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

@ContentView(R.layout.activity_user_search)
public class UserSearchActivity extends ValidCodeActivity implements TextWatcher, JsonArrayAdapter.OnItemClickListener {

    @ViewInject(R.id.user_search_et)
    private ClearableEditText mSearchEt;
    @ViewInject(R.id.listview)
    private ListView mListView;
    private UserSearchAdapter mAdapter;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, "搜索好友");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNaviHeadView();

        mSearchEt.addTextChangedListener(this);
        mAdapter = new UserSearchAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        updateSearchBtn();
    }

    @Event(value = {R.id.user_search_btn})
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.user_search_btn:
                if (etIsNull(mSearchEt)) {
                    toast("请输入好友手机号");
                    return;
                }
                hideKb();
                trySeachUser();
                break;
        }
    }

    private void updateSearchBtn() {
        if (etIsNull(mSearchEt)) {
            hideViewId(R.id.user_search_btn, true);
        } else {
            showViewById(R.id.user_search_btn);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateSearchBtn();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    private void trySeachUser() {
        showDialog();
        final RequestParams params = HttpUtil.requestParams("users/search");
        params.addBodyParameter("login", etTxt(mSearchEt));

        x.http().get(params, new Callback.CommonCallback<JSONObject>() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                HttpResult httpResult = HttpResult.createWith(jsonObject);
                if (httpResult.isSuccess()) {
                    JSONObject user = (JSONObject) httpResult.payload;
                    JSONArray datas = new JSONArray();
                    datas.put(user);
                    mAdapter.fillNewData(datas);
                } else {
                    if (httpResult.code == 404) {
                        toast("用户不存在");
                    } else {
                        toast(httpResult.returnMsg);
                    }
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
                closeDialog();
            }
        });
    }

    @Override
    public void onItemClick(JSONObject data) {
        open(UserProfileActivity.class, "user", data);
    }

}
