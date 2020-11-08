package com.android.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.PEApplication;
import com.android.dialog.PromptDialog;
import com.android.ui.login.LoginActivity;
import com.koi.chat.R;

public class LogoffActivity extends AppCompatActivity {
    private Activity activity;
    private LinearLayout confirm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logoff);
        activity=this;
        confirm=findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PEApplication.INSTANCE.userLogoutCallback();//关闭资源
                Intent intent=new Intent(activity,LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
