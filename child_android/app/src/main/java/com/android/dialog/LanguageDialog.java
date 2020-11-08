package com.android.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.ui.login.LoginActivity;
import com.android.utils.LanguageUtil;
import com.koi.chat.R;

import java.util.Locale;


public class LanguageDialog extends DialogFragment{
    private Activity activity;//上下文
    private Dialog dialog;//弹框
    private View view;
    private RadioGroup rgLanguage;//切换语言
    private LinearLayout llConfirm;//确定
    private String language;//语言
    public LanguageDialog(@NonNull Activity activity) {
        this.activity=activity;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCanceledOnTouchOutside(false);//禁止点击外部关闭
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//软键盘就会把dialog弹起，有的手机则会遮住dialog布局。
        view = View.inflate(getActivity(), R.layout.dialog_language, null);
        dialog.setContentView(view);
        rgLanguage=view.findViewById(R.id.rg_language);
        llConfirm=view.findViewById(R.id.ll_confirm);
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 紧贴底部
        lp.alpha = 1;
        lp.dimAmount = 0.5f;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        lp.windowAnimations = R.style.dialog_bottom_top;//设置弹窗动画
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setViewListener();
        return dialog;
    }

    /**
     * 事件监听
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
        //确定
        llConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("zh".equals(language)){
                    LanguageUtil.setLocale(Locale.SIMPLIFIED_CHINESE,activity);
                }else if ("en".equals(language)){
                    LanguageUtil.setLocale(Locale.ENGLISH,activity);
                    Intent intent=new Intent(activity, LoginActivity.class);
                    startActivity(intent);
                }
                dismiss();//关闭弹窗
            }
        });
    }
}
