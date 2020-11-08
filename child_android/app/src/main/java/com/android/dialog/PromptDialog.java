package com.android.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.android.adapter.ContactAdapter;
import com.android.ui.login.LoginActivity;
import com.android.utils.LanguageUtil;
import com.koi.chat.R;

import org.json.JSONObject;

import java.util.Locale;


public class PromptDialog extends DialogFragment{
    private Activity activity;//上下文
    private Dialog dialog;//弹框
    private View view;
    private String title;
    private String content;
    private TextView tvTitle;
    private TextView tvContent;
    private LinearLayout confirm;//确定
    private OnConfirmListener onConfirmListener;
    public PromptDialog(@NonNull Activity activity,String title,String content) {
        this.activity=activity;
        this.title=title;
        this.content=content;
    }
    public void setOnConfirmListener(OnConfirmListener onConfirmListener){
        this.onConfirmListener=onConfirmListener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCanceledOnTouchOutside(false);//禁止点击外部关闭
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//软键盘就会把dialog弹起，有的手机则会遮住dialog布局。
        view = View.inflate(getActivity(), R.layout.dialog_prompt, null);
        dialog.setContentView(view);
        tvTitle=view.findViewById(R.id.title);
        tvContent=view.findViewById(R.id.content);
        confirm=view.findViewById(R.id.confirm);
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; // 紧贴
        lp.alpha = 1;
        lp.dimAmount = 0.5f;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        lp.windowAnimations = R.style.dialog_bottom_top;//设置弹窗动画
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        initView();
        setViewListener();
        return dialog;
    }
    private void initView(){
        tvTitle.setText(title);
        tvContent.setText(content);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });
    }
    /**
     * 事件监听
     */
    private void setViewListener(){
        //确定
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onConfirmListener!=null){
                   onConfirmListener.confirm(dialog);
                }
                dismiss();
            }
        });
    }
    public interface OnConfirmListener {
        void confirm(Dialog dialog);
    }
}
