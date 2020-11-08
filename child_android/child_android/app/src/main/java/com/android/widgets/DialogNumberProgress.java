package com.android.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.AppT;
import com.daimajia.numberprogressbar.NumberProgressBar;

import com.preschool.edu.R;
import org.apache.commons.lang.StringUtils;


public class DialogNumberProgress extends Dialog {

    private String title;
    private AppT INSTANCE;
    private NumberProgressBar progressBar;
    private int maxProgress;

    public DialogNumberProgress(Context context, String title, int maxProgress) {
        super(context, R.style.dialog);
        this.INSTANCE = (AppT) context;
        this.title = title;
        this.maxProgress = maxProgress;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(false);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_number_progress);
        if (StringUtils.isNotBlank(title)) {
            ((TextView) findViewById(R.id.dialog_title_tv)).setText(title);
        }
        progressBar = (NumberProgressBar) findViewById(R.id.number_progress_bar);
        setMax(maxProgress);
        progressBar.setProgress(0);
    }

    public void setMax(int max) {
        progressBar.setMax(max);
    }

    public void setCurrentProgress(int progress) {
        progressBar.setProgress(progress);
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        }
        return false;
    }
}
