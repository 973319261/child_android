package com.android.ui.user.chatSettings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.PEApplication;
import com.android.utils.LanguageUtil;
import com.android.utils.StatusBarUtil;
import com.android.widgets.MyActionBar;
import com.koi.chat.R;

/**
 * 聊天设置页面
 */
public class CharSettingActivity extends AppCompatActivity {
    private Activity myActivity;//上下文
    private MyActionBar myActionBar;//标题栏
    private LinearLayout llLanguage;//语言设置
    private TextView tvLanguage;//

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity=this;
        setContentView(R.layout.activity_chat_setting);
        llLanguage = findViewById(R.id.ll_char_setting_language);
        tvLanguage = findViewById(R.id.tv_char_setting_language);
        initView();//初始化页面
        setContextListener();//设置监听事件
        myActionBar = findViewById(R.id.myActionBar);
        myActionBar.setData(getString(R.string.text_user_chat), R.drawable.ic_back, "", 1, 0, new MyActionBar.ActionBarClickListener() {
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
        String language= LanguageUtil.getAppLanguage();//获取保存到本地的语言
        if ("zh".equals(language)){
            tvLanguage.setText("简体中文");//设置语言文本
        }else if ("en".equals(language)){
            tvLanguage.setText("English");//设置语言文本
        }
    }
    /**
     * 设置监听事件
     */
    private void setContextListener() {
        //语言设置
        llLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(myActivity,LanguageActivity.class);
                startActivity(intent);
                PEApplication.INSTANCE.setLanguageActivity(myActivity);//保存主页面activity到内存
            }
        });
    }
}
