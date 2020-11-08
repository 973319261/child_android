package com.preschool.edu.activity.fragment;

import android.database.Cursor;
import android.os.Bundle;

import android.view.View;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.buddy.ChatActivity;
import com.preschool.edu.adapter.MessageAdapter;
import com.preschool.edu.core.Constants;
import com.preschool.edu.model.Message;
import com.preschool.edu.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;
import org.xutils.db.sqlite.SqlInfo;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import static com.preschool.edu.PEApplication.daoConfig;

/**
 * Created by jac_cheng on 2017/12/27.
 */
@ContentView(R.layout.fragment_msg)
public class MessageFragment extends BaseFragment implements MessageAdapter.OnMessageClickListener {

    @ViewInject(R.id.listview)
    private ListView mListView;
    private MessageAdapter mAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initNaviBar(false, "消息", true);

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
            case Constants.EVENT_MSG_TYPE_CHAT_REFRESHED:
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
    public void onMessageClick(Message msg) {
        JSONObject user = new JSONObject();
        AppUtil.addKeyValue2JsonObject(user, "login", msg.getSendUserLogin().equals(PEApplication.INSTANCE.getUserLogin()) ? msg.getAcceptUserLogin() : msg.getSendUserLogin());
        mActivity.open(ChatActivity.class, "user", user);
    }
}
