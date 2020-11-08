package com.preschool.edu.core;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.ImageViewTarget;

import cn.finalteam.galleryfinal.widget.GFImageView;


/**
 * Created by jac_cheng on 2017/5/1.
 */

public class FinalGlideImageLoader implements cn.finalteam.galleryfinal.ImageLoader {
    private static final long serialVersionUID = -3494423023778315035L;

    private final static int tagKey = 5 << 24;

    @Override
    public void displayImage(Activity activity, String path,
                             final GFImageView imageView, Drawable defaultDrawable, int width,
                             int height) {
        Glide.with(activity).load("file://" + path)
                .placeholder(defaultDrawable).error(defaultDrawable)
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // 不缓存到SD卡
                .skipMemoryCache(true)
                // .centerCrop()
                .into(new ImageViewTarget<GlideDrawable>(imageView) {
                    @Override
                    protected void setResource(GlideDrawable resource) {
                        imageView.setImageDrawable(resource);
                    }

                    @Override
                    public void setRequest(Request request) {
                        imageView.setTag(tagKey, request);
                    }

                    @Override
                    public Request getRequest() {
                        return (Request) imageView.getTag(tagKey);
                    }
                });
    }

    @Override
    public void clearMemoryCache() {
    }
}
