package com.preschool.edu.activity.buddy;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.android.utils.ALog;
import com.android.utils.AppUtil;
import com.android.utils.DateUtil;
import com.preschool.edu.PEApplication;
import com.preschool.edu.R;
import com.preschool.edu.activity.BaseActivity;
import com.preschool.edu.adapter.ChatAdapter;
import com.preschool.edu.core.AppService;
import com.preschool.edu.core.Constants;
import com.preschool.edu.model.Message;
import com.preschool.edu.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import rx.Subscriber;

/**
 * Created by jac_cheng on 2018/1/18.
 */
@ContentView(R.layout.activity_chat)
public class ChatActivity extends BaseActivity {

    @ViewInject(R.id.chat_content_et)
    private EditText mContentEt;
    @ViewInject(R.id.listview)
    private ListView mListView;
    private ChatAdapter mChatAdapter;
    private JSONObject buddy;
    private boolean unreadMsgNeedNotify;

    @Override
    protected void initNaviHeadView() {
        super.initNaviHeadView();
        addTextViewByIdAndStr(R.id.navi_title_txt, AppService.showUserName(buddy));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        buddy = AppUtil.toJsonObject(getIntentString("user"));
        initNaviHeadView();

        mChatAdapter = new ChatAdapter(this);
        mListView.setAdapter(mChatAdapter);
        loadChatMessages();
    }

    private String getChatBuddyLogin() {
        return buddy.optString("login");
    }

    private void loadChatMessages() {
        x.task().post(new Runnable() {
            @Override
            public void run() {
                try {
                    long unreadCount = Message.getUnreadMessageFromUser(getChatBuddyLogin());
                    if (unreadCount > 0) {
                        unreadMsgNeedNotify = true;
                        Message.setMessageReadByUser(getChatBuddyLogin());
                    }

                    mChatAdapter.fillMessages(Message.findByFromUser(getChatBuddyLogin()));
                    mListView.setSelection(mChatAdapter.getCount());
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unreadMsgNeedNotify) {
            EventBus.getDefault().post(new MessageEvent(Constants.EVENT_MSG_TYPE_CHAT_REFRESHED, "未读消息更新"));
        }
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateChatMessagesUI(MessageEvent messageEvent) {
        if (messageEvent.getMsgType() == Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG) {
            Message msg = (Message) messageEvent.getPayload();
            if (msg.getSendUserLogin().equals(getChatBuddyLogin())) {
                msg.setRead(true);
                msg.save();

                unreadMsgNeedNotify = true;
                mChatAdapter.addMessages(msg);
            }
        }
    }

    @Event(value = {R.id.chat_send_btn})
    private void onBtnsClick(View v) throws JSONException {
        switch (v.getId()) {
            case R.id.chat_send_btn:
                if (etIsNull(mContentEt)) {
                    toast("请输入消息内容");
                    return;
                }
                trySendTextMsg();
                break;
        }
    }

    private void trySendTextMsg() throws JSONException {
        final Message msg = new Message();
        msg.setType(0);
        msg.setAcceptUserLogin(getChatBuddyLogin());
        msg.setSendUserLogin(PEApplication.INSTANCE.getUserLogin());
        msg.setContent(etTxt(mContentEt));
        msg.setSendTime(DateUtil.formatAllDateTime(System.currentTimeMillis()));
        msg.setRead(true);
        msg.setSend(true);
        msg.save();

        String destination = String.format("/topic/chat/oneToOne/%s", getChatBuddyLogin());
        PEApplication.INSTANCE.getStompClient().send(destination, JSON.toJSONString(msg)).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                ALog.e("成功发送消息--:" + msg.toString());
                msg.setSendSuccess(true);
                msg.save();

                mChatAdapter.addMessages(msg);
                mContentEt.setText("");
                toast("发送成功");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                closeDialog();
                toast("发送错误");
            }

            @Override
            public void onNext(Void aVoid) {

            }
        });
    }

}
