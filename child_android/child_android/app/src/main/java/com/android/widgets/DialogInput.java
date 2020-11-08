package com.android.widgets;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.android.AppT;
import com.preschool.edu.R;

public class DialogInput extends Dialog implements OnKeyListener,
        OnClickListener {

    private String title, placeholder;
    private int flag, inputType;
    private Object defaultVal;
    private EditText inputEt;
    private InputCallBackListener listener;
    private AppT INSTANCE;

    public DialogInput(Context context) {
        super(context);
    }

    public DialogInput(Context context, InputCallBackListener listener,
                       String title, String placeholder, int inputType, int flag,
                       Object defaultVal) {
        super(context, R.style.dialog);
        this.INSTANCE = (AppT) context;
        this.listener = listener;
        this.title = title;
        this.placeholder = placeholder;
        this.inputType = inputType;
        this.flag = flag;
        this.defaultVal = defaultVal;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.0f;
        getWindow().setAttributes(lp);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_input);
        ((TextView) findViewById(R.id.title_txt)).setText(title);
        findViewById(R.id.cancel_txt).setOnClickListener(this);
        findViewById(R.id.ok_txt).setOnClickListener(this);
        inputEt = (EditText) findViewById(R.id.input_et);
        inputEt.setHint(placeholder);
        if (this.inputType != -1) {// -1：默认为小数格式
            inputEt.setInputType(this.inputType);
        } else {
            inputEt.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Editable editable = inputEt.getText();
                    int len = editable.length();
                    int maxLen = editable.toString().contains(".") ? 9 : 6;
                    if (len > maxLen) {
                        int selEndIndex = Selection.getSelectionEnd(editable);
                        String str = editable.toString();
                        String newStr = str.substring(0, maxLen);
                        inputEt.setText(newStr);
                        editable = inputEt.getText();
                        int newLen = editable.length();
                        if (selEndIndex > newLen) {
                            selEndIndex = editable.length();
                        }
                        Selection.setSelection(editable, selEndIndex);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
        inputEt.setText(String.valueOf(defaultVal));
        inputEt.setSelection(inputEt.getText().toString().length());
        findViewById(R.id.dialog_bg_layout).postDelayed(new Runnable() {
            @Override
            public void run() {
                INSTANCE.showKb();
            }
        }, 300);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.cancel_txt) {

        } else if (R.id.ok_txt == v.getId()) {
            if (listener != null) {
                listener.refreshInput(flag, inputEt.getText().toString());
            }
        }
        this.dismiss();
    }

    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HOME
                || keyCode == KeyEvent.KEYCODE_SEARCH) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("static-access")
    @Override
    public void dismiss() {
        ((InputMethodManager) INSTANCE
                .getSystemService(INSTANCE.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        super.dismiss();
    }

    public interface InputCallBackListener {
        void refreshInput(int flag, String result);
    }
}
