package com.android.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.bean.Messages;
import com.android.bean.User;
import com.android.core.HttpResult;
import com.android.core.HttpUtil;
import com.android.signal.RTCSignalClient;
import com.android.utils.AppUtil;
import com.android.utils.DateUtil;
import com.android.PEApplication;
import com.android.utils.KeyBoardUtil;
import com.android.utils.OkHttpTool;
import com.android.utils.StatusBarUtil;
import com.android.utils.Tools;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.koi.chat.R;
import com.android.ui.BaseActivity;
import com.android.adapter.ChatAdapter;
import com.android.core.AppService;
import com.android.core.Constants;
import com.android.model.Message;
import com.android.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.ex.DbException;
import org.xutils.http.RequestParams;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

/**
 * 聊天页面
 */
@ContentView(R.layout.activity_chat)
public class ChatActivity extends BaseActivity {
    private Gson gson=new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private Activity myActivity;//上下文
    @ViewInject(R.id.chat_content_et)
    private EditText mContentEt;
    @ViewInject(R.id.listview)
    private ListView mListView;
    @ViewInject(R.id.iv_chat_add)
    private ImageView ivChatAdd;
    @ViewInject(R.id.iv_chat_close)
    private ImageView ivChatClose;
    @ViewInject(R.id.chat_send_btn)
    private Button btnSend;
    @ViewInject(R.id.ll_chat_option)
    private LinearLayout llChatOption;
    @ViewInject(R.id.ll_photo)
    private LinearLayout llPhoto;
    @ViewInject(R.id.ll_photograph)
    private LinearLayout llPhotograph;
    @ViewInject(R.id.ll_voice_call)
    private LinearLayout llVoiceCall;
    @ViewInject(R.id.ll_video_call)
    private LinearLayout llVideoCall;
    @ViewInject(R.id.ll_location)
    private LinearLayout llLocation;
    @ViewInject(R.id.ll_voice_input)
    private LinearLayout llVoiceInput;
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
        myActivity=this;
        StatusBarUtil.setStatusBar(this,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(this,true);//状态栏文字颜色
        EventBus.getDefault().register(this);
        buddy = AppUtil.toJsonObject(getIntentString("user"));
        initNaviHeadView();
        mChatAdapter = new ChatAdapter(this);
        mChatAdapter.setAcceptInfo(buddy);
        mListView.setAdapter(mChatAdapter);
        loadChatMessages(false);
        setViewListening();
    }

    private String getChatBuddyLogin() {
        return buddy.optString("login");
    }

    private void loadChatMessages(final boolean isRead) {
        x.task().post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isRead){
                        long unreadCount = Message.getUnreadMessageFromUser(getChatBuddyLogin());
                        if (unreadCount > 0 ) {
                            Message.setMessageReadByUser(getChatBuddyLogin());
                        }
                    }
                    unreadMsgNeedNotify = true;
                    mChatAdapter.fillMessages(Message.findByFromUser(getChatBuddyLogin()));
                    mListView.setSelection(mChatAdapter.getCount());
                } catch (DbException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void setViewListening(){
        //语音通话
        llVoiceCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Tools.isFastClick()){
                    Intent intent=new Intent(myActivity,CallActivity.class);
                    intent.putExtra("remoteUid",buddy.optString("login"));
                    intent.putExtra("type",RTCSignalClient.SIGNAL_TYPE_VOICE);//语音通话
                    startActivity(intent);
                }
            }
        });
        //视频通话
        llVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Tools.isFastClick()){
                    Intent intent=new Intent(myActivity,CallActivity.class);
                    intent.putExtra("remoteUid",buddy.optString("login"));
                    intent.putExtra("type",RTCSignalClient.SIGNAL_TYPE_VIDEO);//视频通话
                    startActivity(intent);
                }
            }
        });
        mContentEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    llChatOption.setVisibility(View.GONE);
                    ivChatAdd.setVisibility(View.VISIBLE);
                    ivChatClose.setVisibility(View.GONE);
                }

            }
        });
        mContentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0){
                    ivChatAdd.setVisibility(View.GONE);
                    ivChatClose.setVisibility(View.GONE);
                    btnSend.setVisibility(View.VISIBLE);
                }else {
                    btnSend.setVisibility(View.GONE);
                    if (llChatOption.getVisibility()==View.VISIBLE){
                        ivChatAdd.setVisibility(View.GONE);
                        ivChatClose.setVisibility(View.VISIBLE);

                    }else {
                        ivChatAdd.setVisibility(View.VISIBLE);
                        ivChatClose.setVisibility(View.GONE);
                    }
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
        if (messageEvent.getMsgType() == Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG ) {
            Message msg = (Message) messageEvent.getPayload();
            if (msg.getSendUserLogin().equals(getChatBuddyLogin())) {
                msg.setRead(true);
                msg.save();
                unreadMsgNeedNotify = true;
                mChatAdapter.addMessages(msg);
                Messages chat=new Messages();
                chat.setSubscribeValue(0);
                chat.setAcceptUserLogin(msg.getSendUserLogin());
                chat.setSendUserLogin(msg.getAcceptUserLogin());
                chat.setContent(msg.getContent());
                chat.setType(0);
                chat.setSendTime(msg.getSendTime());
                final RequestParams params = HttpUtil.requestParams("messages/updateMessage");
                OkHttpTool.httpPostJson(params.toString(), gson.toJson(chat), new OkHttpTool.ResponseCallback() {
                    @Override
                    public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {

                    }
                });
            }
        }else if (messageEvent.getMsgType() == Constants.EVENT_MSG_TYPE_CHAT_RECEIVED) {
            loadChatMessages(true);
        }else if (messageEvent.getMsgType()==Constants.EVENT_MSG_TYPE_CHAT_READ){
            loadChatMessages(true);
        }
    }

    @Event(value = {R.id.chat_send_btn,R.id.iv_chat_add,R.id.iv_chat_close})
    private void onBtnsClick(View v) throws JSONException {
        switch (v.getId()) {
            case R.id.chat_send_btn:
                if (etIsNull(mContentEt)) {
                    toast("请输入消息内容");
                    return;
                }
                trySendTextMsg();
                break;
            case R.id.iv_chat_add:
                llChatOption.setVisibility(View.VISIBLE);
                ivChatClose.setVisibility(View.VISIBLE);
                ivChatAdd.setVisibility(View.GONE);
                mContentEt.clearFocus();
                KeyBoardUtil.hideKeyboard(v);
                break;
            case R.id.iv_chat_close:
                llChatOption.setVisibility(View.GONE);
                ivChatClose.setVisibility(View.GONE);
                ivChatAdd.setVisibility(View.VISIBLE);
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
        msg.setRead(false);//未读
        msg.setSendSuccess(true);
        msg.setSendState(0);//发送中
        msg.setSend(true);
        mChatAdapter.addMessages(msg);
        final Messages msg1 = new Messages();
        msg1.setSendTime(DateUtil.formatAllDateTime(System.currentTimeMillis()));
        msg1.setSubscribeValue(0);//未读
        msg1.setContent(etTxt(mContentEt));
        msg1.setAcceptUserLogin(getChatBuddyLogin());
        msg1.setSendUserLogin(PEApplication.INSTANCE.getUserLogin());
        msg1.setType(0);
        mContentEt.setText("");
        User user = User.findUser(getChatBuddyLogin());
        if (user!=null){//判断是否为好友
            final RequestParams params = HttpUtil.requestParams("messages/sendToUser");
            OkHttpTool.httpPostJson(params.toString(), gson.toJson(msg1), new OkHttpTool.ResponseCallback() {
                @Override
                public void onResponse(final boolean isSuccess, final int responseCode, final String response, Exception exception) {
                    myActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isSuccess && responseCode==200){
                                Log.i("response",response);
                                try {
                                    JSONObject jsonObject=new JSONObject(response);
                                    HttpResult httpResult = HttpResult.createWith(jsonObject);
                                    if (httpResult.isSuccess()) {
                                        msg.setSendSuccess(true);
                                        msg.setSendState(1);//发送成功
                                        mChatAdapter.refresh(msg);
                                        msg.save();//保存到本地
                                    } else {
                                        msg.setSendSuccess(false);
                                        msg.setSendState(2);//发送失败
                                        toast(httpResult.returnMsg);
                                        mChatAdapter.refresh(msg);
                                        msg.save();//保存到本地
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }else {
                                msg.setSendSuccess(false);
                                msg.setSendState(2);//发送失败
                                mChatAdapter.refresh(msg);
                                msg.save();//保存到本地
                            }
                            closeDialog();
                        }
                    });
                }
            });
        }else {//不是好友
            msg.setSendSuccess(false);
            msg.setSendState(3);//不是好友
            mChatAdapter.refresh(msg);
            msg.save();//保存到本地
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
           if (llChatOption.getVisibility()==View.VISIBLE){
               llChatOption.setVisibility(View.GONE);
               ivChatClose.setVisibility(View.GONE);
               ivChatAdd.setVisibility(View.VISIBLE);
               if (mContentEt.getText().toString().length()>0){
                   ivChatAdd.setVisibility(View.GONE);
                   ivChatClose.setVisibility(View.GONE);
                   btnSend.setVisibility(View.VISIBLE);
               }
           }else {
               finish();
           }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
