package com.android.widgets.sectionlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListView;

public class UUListView extends ListView {

	public View group = null;

	public UUListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public UUListView(Context context) {
		super(context);
	}

	public UUListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
