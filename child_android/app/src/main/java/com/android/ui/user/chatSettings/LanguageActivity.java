package com.android.ui.user.chatSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.PEApplication;
import com.android.ui.MainActivity;
import com.android.utils.LanguageUtil;
import com.android.utils.SPUtils;
import com.android.utils.StatusBarUtil;
import com.android.widgets.MyActionBar;
import com.koi.chat.R;

import java.util.Locale;

/**
 * 语言设置页面
 */
public class LanguageActivity extends AppCompatActivity {
    private Activity myActivity;//上下文
    private MyActionBar myActionBar;//标题栏
    private RadioGroup rgLanguage;//切换语言
    private String language;//切换语言
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myActivity=this;
        setContentView(R.layout.activity_language);
        language= LanguageUtil.getAppLanguage();//获取保存到本地的语言
        rgLanguage=findViewById(R.id.rg_language);
        myActionBar = findViewById(R.id.myActionBar);
        myActionBar.setData(getResources().getString(R.string.text_language_title), R.drawable.ic_back, getResources().getString(R.string.text_save), 1, 0, new MyActionBar.ActionBarClickListener() {
            @Override
            public void onLeftClick() {
                finish();
            }

            @Override
            public void onRightClick() {
                if ("zh".equals(language)){
                    LanguageUtil.setLocale(Locale.SIMPLIFIED_CHINESE,myActivity);
                }else if ("en".equals(language)){
                    LanguageUtil.setLocale(Locale.ENGLISH,myActivity);
                }
                PEApplication.INSTANCE.closeActivity();//关闭原来主页面的activity
                Intent intent =new Intent(myActivity, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        initView();//初始化页面
        setViewListener();//监听事件
    }
    /**
     * 初始化页面
     */
    private void initView() {
        StatusBarUtil.setStatusBar(myActivity,true);//设置当前界面是否是全屏模式（状态栏）
        StatusBarUtil.setStatusBarLightMode(myActivity,true);//状态栏文字颜色
        //勾选初始化
        if ("zh".equals(language)) {//简体中文
            rgLanguage.check(R.id.rb_language_chinese);
        } else if ("en".equals(language)){//英语
            rgLanguage.check(R.id.rb_language_english);
        }
    }

    /**
     * 监听事件
     */
    private void setViewListener(){
        //切换语言
        rgLanguage.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==R.id.rb_language_chinese){//简体中文
                    language="zh";
                }else if(checkedId==R.id.rb_language_english){//英文
                    language="en";
                }
            }
        });
    }

}
