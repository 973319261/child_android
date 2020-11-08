package com.preschool.edu.activity;


import android.content.Intent;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.utils.AppUtil;
import com.jaeger.library.StatusBarUtil;
import com.preschool.edu.R;
import com.preschool.edu.activity.fragment.ContactsFragment;
import com.preschool.edu.activity.fragment.DongTaiFragment;
import com.preschool.edu.activity.fragment.IndexFragment;
import com.preschool.edu.activity.fragment.MessageFragment;
import com.preschool.edu.activity.fragment.MyFragment;
import com.preschool.edu.core.Constants;
import com.preschool.edu.model.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.ex.DbException;
import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;

/**
 * Created by jac_cheng on 2017/4/13.
 */
@ContentView(R.layout.activity_index)
public class MainActivity extends BaseActivity {
    @ViewInject(R.id.tab_tv0)
    private TextView tabTv0;
    @ViewInject(R.id.tab_tv1)
    private TextView tabTv1;
    @ViewInject(R.id.tab_tv2)
    private TextView tabTv2;
    @ViewInject(R.id.tab_tv3)
    private TextView tabTv3;
    @ViewInject(R.id.tab_tv4)
    private TextView tabTv4;
    @ViewInject(R.id.msg_unread_img)
    private ImageView msgHasUnreadImg;

    private IndexFragment indexFragment;
    private MessageFragment messageFragment;
    private ContactsFragment contactsFragment;
    private DongTaiFragment dongtaiFragment;
    private MyFragment myFragment;
    private Fragment[] fragments;
    private int currentTabIndex;
    public int fromIndex;
    private long mExitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment();
        EventBus.getDefault().register(this);
        updateMessageUnreadCount();
    }

    private void initFragment() {
        indexFragment = new IndexFragment();
        messageFragment = new MessageFragment();//消息
        contactsFragment = new ContactsFragment();//通讯录
        dongtaiFragment = new DongTaiFragment();//动态
        myFragment = new MyFragment();//个人
        fragments = new Fragment[]{indexFragment, messageFragment, contactsFragment, dongtaiFragment, myFragment};
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, indexFragment).show(indexFragment).hide(messageFragment).commit();
        tabChange(R.id.tab_layout0);
    }

    @Event(value = {R.id.tab_layout0, R.id.tab_layout1, R.id.tab_layout2, R.id.tab_layout3, R.id.tab_layout4})
    private void tabClick(View view) {
        int checkedId = view.getId();
        tabChange(checkedId);
    }

    private void tabChange(int checkedId) {
        tabTv0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m0, 0, 0);
        tabTv1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m1, 0, 0);
        tabTv2.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m2, 0, 0);
        tabTv3.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m3, 0, 0);
        tabTv4.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m4, 0, 0);
        StatusBarUtil.setColor(this, getResources().getColor(R.color.dark_gray));
        if (checkedId == R.id.tab_layout0) {
            tabTv0.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m0_h, 0, 0);
            switchIndex(0);
        } else if (checkedId == R.id.tab_layout1) {
            tabTv1.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m1_h, 0, 0);
            switchIndex(1);
        } else if (checkedId == R.id.tab_layout2) {
            tabTv2.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m2_h, 0, 0);
            switchIndex(2);
        } else if (checkedId == R.id.tab_layout3) {
            tabTv3.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m3_h, 0, 0);
            switchIndex(3);
        } else if (checkedId == R.id.tab_layout4) {
            tabTv4.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.m4_h, 0, 0);
            switchIndex(4);
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
            if (fragments[index] == myFragment) {
            }
        }
        currentTabIndex = index;
    }

    public void showIndex() {
        tabChange(R.id.tab_layout0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
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
        }
    }

    private void updateMessageUnreadCount() {
        try {
            msgHasUnreadImg.setVisibility(com.preschool.edu.model.Message.getUnreadMessage() == 0 ? View.GONE : View.VISIBLE);
        } catch (DbException e) {
            e.printStackTrace();
        }
    }
}
