package com.android.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.koi.chat.R;

/**
 * 权限管理弹窗
 */
public class PermissionsDialog extends DialogFragment {
    private Activity activity;
    private Dialog dialog;//弹框
    private View view;
    private String content;
    private ImageView ivClose;
    private TextView tvPermissions;
    private Button btnOpen;
    private OnOpenListener onOpenListener;
    private OnCloseListener onCloseListener;
    public PermissionsDialog(Activity activity, String content){
        this.activity=activity;
        this.content=content;
    }
    public void setOnOpenListener(OnOpenListener onOpenListener){
        this.onOpenListener=onOpenListener;
    }
    public void setOnCloseListener(OnCloseListener onCloseListener){
        this.onCloseListener=onCloseListener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog=new Dialog(activity, R.style.dialog);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);//软键盘就会把dialog弹起，有的手机则会遮住dialog布局。
        view= View.inflate(getActivity(), R.layout.dialog_permissions,null);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        initView();//初始化
        setViewListener();//事件监听
        // 设置宽度为屏宽, 靠近屏幕底部。
        Window window = dialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER; //
        lp.alpha = 1;
        lp.dimAmount = 0.5f;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
        lp.windowAnimations= R.style.dialog_bottom_top;//设置弹窗动画
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        return dialog;
    }

    private void setViewListener() {
        //关闭
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (onCloseListener!=null){
                    onCloseListener.onClose(dialog);
                }
            }
        });
        //打开设置
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               if (onOpenListener!=null){
                   onOpenListener.onOpen(dialog);
               }
            }
        });
    }

    private void initView() {
        ivClose=view.findViewById(R.id.iv_close);
        tvPermissions=view.findViewById(R.id.tv_permissions);
        btnOpen=view.findViewById(R.id.btn_open);
        tvPermissions.setText(content);
    }
    //关闭
    public interface OnCloseListener{
        void onClose(Dialog dialog);
    }
    //打开
    public interface OnOpenListener{
        void onOpen(Dialog dialog);
    }
}
