package com.android.widgets;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jac_cheng on 2017/7/19.
 */

public class CountDownButton extends androidx.appcompat.widget.AppCompatTextView {

    private MyCountDownTimer mc; //倒计时线程
    private long millis;
    private TimeButtonCallBack timeButtoncallback;//倒计时按钮回掉接口

    public CountDownButton(Context context) {
        this(context, null);
    }

    public CountDownButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 暴露倒计时时间给使用者调
     *
     * @param endTs
     */
    public void setTime(long endTs) {
        setVisibility(View.GONE);
        this.millis = endTs - System.currentTimeMillis();
        if (endTs == 0 || millis > 3600 * 24 * 1000) {
        } else {
            setVisibility(View.VISIBLE);
            if (mc != null) {
                mc.cancel();
                mc = null;
            }
            mc = new MyCountDownTimer(millis, 1);
            mc.start();
        }
    }

    public void setTimeButtonCallBack(TimeButtonCallBack timeButtoncallback) {
        this.timeButtoncallback = timeButtoncallback;
    }

    /**
     * 倒计时控件回调外部代码的接口。
     */
    public interface TimeButtonCallBack {
        /**
         * 点击按钮后，开始计时前调用的方法。
         *
         * @return 返回true会开始计时，false会退出计时。
         */
        boolean onStart();

        /**
         * 结束啦。
         */
        void onStop();

        /**
         * 数字发生变化了。
         *
         * @param num
         * @return
         */
        void numChanged(int num);

    }

    class MyCountDownTimer extends CountDownTimer {
        /**
         * @param millisInFuture    表示以毫秒为单位 倒计时的总数
         *                          <p>
         *                          例如 millisInFuture=1000 表示1秒
         * @param countDownInterval 表示 间隔 多少微秒 调用一次 onTick 方法
         *                          <p>
         *                          例如: countDownInterval =1000 ; 表示每1000毫秒调用一次onTick()
         */
        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mc.cancel();
            if (timeButtoncallback != null) {
                timeButtoncallback.onStop();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (millisUntilFinished <= 0) {
                setText("剩 00:00:00:0");
                if (timeButtoncallback != null) {
                    timeButtoncallback.onStop();
                }
            } else {
                long ts = millisUntilFinished / 1000;
                int hour = (int) (ts / 3600);
                int minute = (int) (ts % 3600 / 60);
                int second = (int) (ts % 60);

                int milliSecond = (int) (millisUntilFinished - hour * 1000 * 3600 - minute * 1000 * 60 - second * 1000);//获得毫秒数
                setText(String.format("剩 %02d:%02d:%02d:%s", hour, minute, second, String.valueOf(milliSecond).substring(0, 1)));
                if (timeButtoncallback != null) {
                    timeButtoncallback.numChanged((int) millisUntilFinished / 1000);
                }
            }
        }
    }
}
