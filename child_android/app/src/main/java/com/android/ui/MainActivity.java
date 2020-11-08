package com.android.ui;


import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.PEApplication;
import com.android.bean.Messages;
import com.android.dialog.PromptDialog;
import com.android.model.Message;
import com.android.receiver.NetStatusReceiver;
import com.android.ui.login.LoginActivity;
import com.android.utils.AppUtil;
import com.android.utils.LanguageUtil;
import com.google.gson.Gson;
import com.koi.chat.R;
import com.android.ui.contacts.ContactsFragment;
import com.android.ui.dynamic.DynamicFragment;
import com.android.ui.message.MessageFragment;
import com.android.ui.user.UserFragment;
import com.android.core.Constants;
import com.android.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.ex.DbException;

import java.util.Locale;

/**
 * 主页面
 */
public class MainActivity extends BaseActivity {
    private TextView tabTv1;
    private TextView tabTv2;
    private TextView tabTv3;
    private TextView tabTv4;
    private ImageView msgHasUnreadImg;
    private Activity activity;
    private MessageFragment messageFragment;
    private ContactsFragment contactsFragment;
    private DynamicFragment dongtaiFragment;
    private UserFragment userFragment;
    private Fragment[] fragments;
    private int currentTabIndex;
    public int fromIndex;
    private long mExitTime;
    private Gson gson=new Gson().newBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private NetStatusReceiver netStatusReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity=this;
        Locale locale = LanguageUtil.getAppLocale();//获取本地的语言
        LanguageUtil.changeAppLanguage(this,locale,true);//设置语言
        setContentView(R.layout.activity_main);
        tabTv1=findViewById(R.id.tab_tv1);
        tabTv2=findViewById(R.id.tab_tv2);
        tabTv3=findViewById(R.id.tab_tv3);
        tabTv4=findViewById(R.id.tab_tv4);
        msgHasUnreadImg=findViewById(R.id.msg_unread_img);
        initFragment();
        EventBus.getDefault().register(this);
        updateMessageUnreadCount();
    }

    private void initFragment() {
        messageFragment = new MessageFragment();//消息
        contactsFragment = new ContactsFragment();//通讯录
        dongtaiFragment = new DynamicFragment();//动态
        userFragment = new UserFragment();//个人
        PEApplication.INSTANCE.setMainActivity(this);//保存主页面activity到内存
        fragments = new Fragment[]{ messageFragment, contactsFragment, dongtaiFragment, userFragment};
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, messageFragment).show(messageFragment).hide(contactsFragment).commit();
        tabChange(1);
        tabClick();
        //注册网络状态监听广播
        netStatusReceiver=new NetStatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netStatusReceiver, filter);
    }

    private void tabClick() {
        tabTv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabChange(1);
            }
        });
        tabTv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabChange(2);
            }
        });
        tabTv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabChange(3);
            }
        });
        tabTv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabChange(4);
            }
        });

    }

    private void tabChange(int checkedId) {
        tabTv1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m1, 0, 0);
        tabTv2.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m2, 0, 0);
        tabTv3.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m3, 0, 0);
        tabTv4.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m4, 0, 0);
        com.android.utils.StatusBarUtil.setStatusBar(this,true);//设置当前界面是否是全屏模式（状态栏）
        com.android.utils.StatusBarUtil.setStatusBarLightMode(this,true);//状态栏文字颜色
        if (checkedId == 1) {
            tabTv1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m1_h, 0, 0);
            switchIndex(0);
        } else if (checkedId ==2) {
            tabTv2.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m2_h, 0, 0);
            switchIndex(1);
        } else if (checkedId == 3) {
            tabTv3.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m3_h, 0, 0);
            switchIndex(2);
        } else if (checkedId == 4) {
            tabTv4.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m4_h, 0, 0);
            switchIndex(3);
        }
    }

    public void switchIndex(int index) {
        if (currentTabIndex != index) {
            fromIndex = currentTabIndex;
            FragmentTransaction trx = getSupportFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
            if (fragments[index] == userFragment) {
            }
        }
        currentTabIndex = index;
    }

    public void showIndex() {
        tabChange(R.id.tab_layout1);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, R.string.text_press_exit, Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doMessageEvent(MessageEvent messageEvent) {
        switch (messageEvent.getMsgType()) {
            case Constants.EVENT_MSG_TYPE_CHAT_REFRESHED:
                updateMessageUnreadCount();
                break;
            case Constants.EVENT_MSG_TYPE_CHAT_RECEIVED_MSG:
                updateMessageUnreadCount();
                AppUtil.playMsgSound();
            break;
            case Constants.EVENT_MSG_TYPE_LOGOUT://强制登出
                open(LoginActivity.class,"isLogoff",true);
                break;
            case Constants.EVENT_MSG_TYPE_CHAT_READ://刷新已读消息
                try {
                    Messages msg = gson.fromJson(gson.toJson(messageEvent.getPayload()), Messages.class);//解析后台数据
                    Message.setMessageReadByUser(msg.getAcceptUserLogin());
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private void updateMessageUnreadCount() {
        try {
            msgHasUnreadImg.setVisibility(com.android.model.Message.getUnreadMessage() == 0 ? View.GONE : View.VISIBLE);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
