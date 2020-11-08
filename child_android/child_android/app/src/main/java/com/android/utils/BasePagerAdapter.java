package com.android.utils;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;


public class BasePagerAdapter extends PagerAdapter {

	protected LayoutInflater mInflater;
	protected Context mContext;
	public BasePagerAdapter(Context context) {
		this.mContext = context;
		this.mInflater = LayoutInflater.from(context);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return super.instantiateItem(container, position);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1; 
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}
}
