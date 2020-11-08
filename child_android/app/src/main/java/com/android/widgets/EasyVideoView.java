package com.android.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by jac_cheng on 2017/8/15.
 */

public class EasyVideoView extends VideoView {

    private int width, height;

    public EasyVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public EasyVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EasyVideoView(Context context) {
        super(context);
    }

    public void setMeasure(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {//这里重写onMeasure的方法
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width;
        // 这个之前是默认的拉伸图像
        if (this.width > 0 && this.height > 0) {
            width = this.width;
            height = this.height;
        }
        setMeasuredDimension(width, height);
    }
}
