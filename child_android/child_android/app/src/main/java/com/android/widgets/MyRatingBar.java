package com.android.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.utils.Util;

/**
 * Created by jac_cheng on 2017/8/16.
 */

public class MyRatingBar extends LinearLayout {

    private boolean isIndicator = true;
    private Context context;

    public MyRatingBar(Context context) {
        this(context, null);
    }

    public MyRatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOrientation(HORIZONTAL);
        setRate(0);
    }

    public void setIsIndicator(boolean isIndicator) {
        this.isIndicator = isIndicator;
    }

    public void setRate(int rate) {
        removeAllViews();
        int size = Util.DensityUtil.dip2px(context, 12);
        for (int i = 0; i < 5; i++) {
            ImageView startImg = new ImageView(context);
            startImg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            if (isIndicator) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                startImg.setLayoutParams(params);
            }
//            if (rate > i) {
//                startImg.setImageResource(R.drawable.v2_star_full);
//            } else {
//                startImg.setImageResource(R.drawable.v2_star_empty);
//            }
            this.addView(startImg, i);
        }
    }

}
