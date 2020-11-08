package com.android.ui.user.messageSettings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.ui.user.chatSettings.LanguageActivity;
import com.android.utils.SPUtils;
import com.android.utils.StatusBarUtil;
import com.android.widgets.MyActionBar;
import com.koi.chat.R;
import com.suke.widget.SwitchButton;

/**
 * 消息设置页面
 */
public class MessageSettingActivity extends AppCompatActivity {
    private Activity myActivity;//上下文
    private MyActionBar myActionBar;//标题栏
    private SwitchButton switchButton;//语言设置

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity=this;
        setContentView(R.layout.activity_message_setting);
        switchButton = findViewById(R.id.switch_button);
        initView();//初始化页面
        setContextListener();//设置监听事件
        myActionBar = findViewById(R.id.myActionBar);
        myActionBar.setData(getString(R.string.text_user_message), R.drawable.ic_back, "", 1, 0, new MyActionBar.ActionBarClickListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRightClick() {

            }
        });
    }
    /**
     * 初始化页面
     */
    private void initView() {
        StatusBarUtil.setStatusBar(myActivity,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(myActivity,true);//状态栏文字颜色
        boolean flag= SPUtils.getSp(SPUtils.SP_PROMPT_TONE,false);
        if (flag){
            switchButton.setChecked(true);
        }else {
            switchButton.setChecked(false);
        }
    }
    /**
     * 设置监听事件
     */
    private void setContextListener() {
        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked){//开启
                    SPUtils.setSp(SPUtils.SP_PROMPT_TONE,true);
                }else {
                    SPUtils.setSp(SPUtils.SP_PROMPT_TONE,false);
                }
            }
        });
    }
}
