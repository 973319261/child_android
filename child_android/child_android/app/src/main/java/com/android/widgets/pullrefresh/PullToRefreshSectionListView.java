package com.android.widgets.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;

import com.android.widgets.pullrefresh.internal.EmptyViewMethodAccessor;
import com.android.widgets.sectionlist.PinnedHeaderListView;


@SuppressWarnings("deprecation")
public class PullToRefreshSectionListView extends
		PullToRefreshAdapterViewBase<PinnedHeaderListView> {

	class InternalListView extends PinnedHeaderListView implements
			EmptyViewMethodAccessor {

		public InternalListView(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		public void setEmptyView(View emptyView) {
			PullToRefreshSectionListView.this.setEmptyView(emptyView);
		}

		@Override
		public void setEmptyViewInternal(View emptyView) {
			super.setEmptyView(emptyView);
		}

		public ContextMenuInfo getContextMenuInfo() {
			return super.getContextMenuInfo();
		}
	}

	public PullToRefreshSectionListView(Context context) {
		super(context);
		this.setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshSectionListView(Context context, Mode mode) {
		super(context, mode);
		this.setDisableScrollingWhileRefreshing(false);
	}

	public PullToRefreshSectionListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDisableScrollingWhileRefreshing(false);
	}

	@Override
	public ContextMenuInfo getContextMenuInfo() {
		return ((InternalListView) getRefreshableView()).getContextMenuInfo();
	}

	@Override
	protected final PinnedHeaderListView createRefreshableView(Context context,
			AttributeSet attrs) {
		PinnedHeaderListView lv = new InternalListView(context, attrs);
		lv.setId(android.R.id.list);
		return lv;
	}

	@Override
	public Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

}
