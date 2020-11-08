package com.android.ui.contacts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.ui.login.CountryAreaCodeActivity;
import com.android.utils.JsonArrayAdapter;
import com.android.utils.StatusBarUtil;
import com.android.widgets.ClearableEditText;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.ui.login.ValidCodeActivity;
import com.android.adapter.UserSearchAdapter;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.Locale;

/**
 *添加好友页面
 */
@ContentView(R.layout.activity_add_friends)
public class AddFriendsActivity extends ValidCodeActivity implements TextWatcher, JsonArrayAdapter.OnItemClickListener {

    @ViewInject(R.id.login_mobile_area_code_tv)
    private TextView tvAreaCode;
    @ViewInject(R.id.user_search_et)
    private EditText mSearchEt;
    @ViewInject(R.id.listview)
    private ListView mListView;
    private UserSearchAdapter mAdapter;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, getString(R.string.text_friends_add_title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNaviHeadView();
        StatusBarUtil.setStatusBar(this,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(this,true);//状态栏文字颜色
        mSearchEt.addTextChangedListener(this);
        mAdapter = new UserSearchAdapter(this);
        mAdapter.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        updateSearchBtn();
    }

    @Event(value = {R.id.user_search_btn,R.id.login_mobile_area_code_tv})
    private void onBtnsClick(View v) {
        switch (v.getId()) {
            case R.id.user_search_btn:
                if (etIsNull(mSearchEt)) {
                    toast(getString(R.string.text_friends_add_enter_phone));
                    return;
                }
                hideKb();
                trySeachUser();
                break;
            case R.id.login_mobile_area_code_tv:
                open(CountryAreaCodeActivity.class, 300);
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
        params.addBodyParameter("login", String.format(Locale.getDefault(),"%s%s",tvTxt(tvAreaCode),etTxt(mSearchEt)));
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
                        toast(getString(R.string.text_friends_add_user_not_exist));
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
        open(FriendsInfoActivity.class, "user", data);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 300) {
                tvAreaCode.setText(data.getStringExtra("areaCode"));
            }
        }
    }
}
