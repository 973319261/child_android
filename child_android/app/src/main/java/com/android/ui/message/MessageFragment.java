package com.android.ui.message;

import android.database.Cursor;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.bean.Messages;
import com.android.bean.User;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.ui.BaseFragment;
import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.android.PEApplication;
import com.android.utils.OkHttpTool;
import com.android.utils.ToastUtil;
import com.android.widgets.BadgeHelper;
import com.google.gson.Gson;
import com.koi.chat.R;
import com.android.adapter.MessageAdapter;
import com.android.core.Constants;
import com.android.model.Message;
import com.android.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.android.PEApplication.daoConfig;

/**
 * 消息主页面
 */
@ContentView(R.layout.fragment_message)
public class MessageFragment extends BaseFragment implements MessageAdapter.OnMessageClickListener {

    @ViewInject(R.id.listview)
    private ListView mListView;
    private MessageAdapter mAdapter;
    private Gson gson=new Gson().newBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNaviBar(false, getString(R.string.text_main_message), true);
        mAdapter = new MessageAdapter(mActivity, 0);
        mAdapter.setOnMessageClickListener(this);
        mListView.setAdapter(mAdapter);
        x.task().post(new Runnable() {
            @Override
            public void run() {
                refreshMessage();
            }
        });
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
    public void refreshUI(MessageEvent messageEvent) {
        switch (messageEvent.getMsgType()) {
            case Constants.EVENT_MSG_TYPE_CONTACT_REFRESH:
                refreshMessage();
            case Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG:
                refreshMessage();
                break;
        }
    }

    public void refreshMessage() {
        String loginUser = PEApplication.INSTANCE.getUserLogin();
        SqlInfo sql = new SqlInfo();
        sql.setSql("SELECT * FROM (SELECT sendUserLogin AS LXR, * FROM T_ChatMessage WHERE acceptUserLogin = '" + loginUser + "' UNION SELECT acceptUserLogin AS LXR, * FROM T_ChatMessage WHERE sendUserLogin = '" + loginUser + "') GROUP BY LXR ORDER BY sendTime DESC");
        ALog.e(sql.getSql());
        try {
            Cursor cursor = x.getDb(daoConfig).execQuery(sql);
            List<Message> datas = new ArrayList<>();
            while (cursor.moveToNext()) {
                Message msg = new Message(cursor.getInt(cursor.getColumnIndex("type")),
                        cursor.getString(cursor.getColumnIndex("sendUserLogin")),
                        cursor.getString(cursor.getColumnIndex("acceptUserLogin")),
                        cursor.getString(cursor.getColumnIndex("content")),
                        cursor.getString(cursor.getColumnIndex("sendTime")));
                msg.setId(cursor.getInt(cursor.getColumnIndex("id")));
                msg.setSendSuccess(cursor.getInt(cursor.getColumnIndex("sendSuccess")) == 1);
                msg.setRead(cursor.getInt(cursor.getColumnIndex("isRead")) == 1);
                datas.add(msg);
            }
            mAdapter.clear();
            mAdapter.addAll(datas);
        } catch (DbException e) {
            ALog.e(e);
        }
    }

    @Override
    public void onMessageClick(Message msg, final User user) {
        JSONObject jsonObject = AppUtil.toJsonObject(gson.toJson(user));
        Messages chat=new Messages();
        chat.setSubscribeValue(0);
        chat.setAcceptUserLogin(msg.getSendUserLogin());
        chat.setSendUserLogin(msg.getAcceptUserLogin());
        chat.setContent(msg.getContent());
        chat.setType(0);
        chat.setSendTime(msg.getSendTime());
        if (msg.getAcceptUserLogin().equals(PEApplication.INSTANCE.getUserLogin())){
            final RequestParams params = HttpUtil.requestParams("messages/updateMessage");
            OkHttpTool.httpPostJson(params.toString(), gson.toJson(chat), new OkHttpTool.ResponseCallback() {
                @Override
                public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                    try {
                        Message.setMessageReadByUser(user.getLogin());
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                }
            });
        }else {

        }
        AppUtil.addKeyValue2JsonObject(jsonObject, "login", msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUserLogin()) ? msg.getAcceptUserLogin() : msg.getSendUserLogin());
        mActivity.open(ChatActivity.class, "user", jsonObject);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshMessage();
    }
}
