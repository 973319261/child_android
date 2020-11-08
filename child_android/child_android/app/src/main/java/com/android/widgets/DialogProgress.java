package com.android.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.preschool.edu.R;


public class DialogProgress extends Dialog implements OnKeyListener {
    private String msg;
    private Context context;

    public DialogProgress(Context context) {
        super(context);
        this.context = context;
    }

    public DialogProgress(Context context, int theme, String msg) {
        super(context, theme);
        this.context = context;
        this.msg = msg;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_webtask_progress_overlay);
//		ImageView roundImg = (ImageView) findViewById(R.id.progress_round_img);
//		Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.tip);
//		roundImg.startAnimation(operatingAnim);
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        }
        return false;
    }

}
