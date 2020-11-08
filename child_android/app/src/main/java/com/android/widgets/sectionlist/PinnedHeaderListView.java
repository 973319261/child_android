package com.android.widgets.sectionlist;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;

public class PinnedHeaderListView extends UUListView implements
		OnScrollListener {

	private OnScrollListener mOnScrollListener;

	public static interface PinnedSectionedHeaderAdapter {
		public boolean isSectionHeader(int position);

		public int getSectionForPosition(int position);

		public View getSectionHeaderView(int section, View convertView,
										 ViewGroup parent);

		public int getSectionHeaderViewType(int section);

		public int getCount();

	}

	private PinnedSectionedHeaderAdapter mAdapter;
	public ViewGroup mCurrentHeader;
	private int mCurrentHeaderViewType = 0;
	private float mHeaderOffset;
	private boolean mShouldPin = true;
	private int mCurrentSection = 0;

	public PinnedHeaderListView(Context context) {
		super(context);
		super.setOnScrollListener(this);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnScrollListener(this);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		super.setOnScrollListener(this);
	}

	public void setPinHeaders(boolean shouldPin) {
		mShouldPin = shouldPin;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		mAdapter = (PinnedSectionedHeaderAdapter) adapter;
		super.setAdapter(adapter);
	}

	/**
	 * @description 当列表发送滚动，需要隐藏部分item选项时候，通过该方法模拟点击事件，结合onScroll方法中对mCurrentHeader的初始化
	 */
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}
	
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem,
					visibleItemCount, totalItemCount);
		}
		
		if (mAdapter == null || mAdapter.getCount() == 0 || !mShouldPin
				|| (firstVisibleItem < getHeaderViewsCount())) {
			mCurrentHeader = null;
			mHeaderOffset = 0.0f;
			for (int i = firstVisibleItem; i < firstVisibleItem
					+ visibleItemCount; i++) {
				View header = getChildAt(i);
				if (header != null) {
					header.setVisibility(VISIBLE);
				}
			}
			return;
		}

		firstVisibleItem -= getHeaderViewsCount();

		int section = mAdapter.getSectionForPosition(firstVisibleItem);
		int viewType = mAdapter.getSectionHeaderViewType(section);
		mCurrentHeader = (ViewGroup) getSectionHeaderView(section,
				mCurrentHeaderViewType != viewType ? null : mCurrentHeader);
		ensurePinnedHeaderLayout(mCurrentHeader);
		mCurrentHeaderViewType = viewType;

		mHeaderOffset = 0.0f;

		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
			if (mAdapter.isSectionHeader(i)) {
				View header = getChildAt(i - firstVisibleItem);
				float headerTop = header.getTop();
				float pinnedHeaderHeight = mCurrentHeader.getMeasuredHeight();
				header.setVisibility(VISIBLE);
				if (pinnedHeaderHeight >= headerTop && headerTop > 0) {
					mHeaderOffset = headerTop - header.getHeight();
				} else if (headerTop <= 0) {
					header.setVisibility(INVISIBLE);
				}
			}
		}
		invalidate();
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	private View getSectionHeaderView(int section, View oldView) {
		boolean shouldLayout = section != mCurrentSection || oldView == null;

		View view = mAdapter.getSectionHeaderView(section, oldView, this);
		if (shouldLayout) {
			// a new section, thus a new header. We should lay it out again
			ensurePinnedHeaderLayout(view);
			mCurrentSection = section;
		}
		return view;
	}

	private void ensurePinnedHeaderLayout(View header) {
		if (header.isLayoutRequested()) {
			int widthSpec = MeasureSpec.makeMeasureSpec(getWidth(),
					MeasureSpec.EXACTLY);
			int heightSpec;
			ViewGroup.LayoutParams layoutParams = header.getLayoutParams();
			if (layoutParams != null && layoutParams.height > 0) {
				heightSpec = MeasureSpec.makeMeasureSpec(layoutParams.height,
						MeasureSpec.EXACTLY);
			} else {
				heightSpec = MeasureSpec.makeMeasureSpec(0,
						MeasureSpec.UNSPECIFIED);
			}

			header.measure(widthSpec, heightSpec);
			int height = header.getMeasuredHeight();
			header.layout(0, 0, getWidth(), height);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mAdapter == null || !mShouldPin || mCurrentHeader == null)
			return;
		int saveCount = canvas.save();
		canvas.translate(0, mHeaderOffset);
		canvas.clipRect(0, 0, getWidth(), mCurrentHeader.getMeasuredHeight()); // needed
		mCurrentHeader.draw(canvas);
		canvas.restoreToCount(saveCount);
	}

	@Override
	public void setOnScrollListener(OnScrollListener l) {
		mOnScrollListener = l;
	}

	public void setOnItemClickListener(
			OnItemClickListener listener) {
		super.setOnItemClickListener(listener);
	}

	public static abstract class OnItemClickListener implements
			AdapterView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view,
				int rawPosition, long id) {
			SectionedBaseAdapter<?> adapter;
			if (adapterView.getAdapter().getClass()
					.equals(HeaderViewListAdapter.class)) {
				HeaderViewListAdapter wrapperAdapter = (HeaderViewListAdapter) adapterView
						.getAdapter();
				adapter = (SectionedBaseAdapter<?>) wrapperAdapter
						.getWrappedAdapter();
			} else {
				adapter = (SectionedBaseAdapter<?>) adapterView.getAdapter();
			}
			int section = adapter.getSectionForPosition(rawPosition);
			int position = adapter.getPositionInSectionForPosition(rawPosition);

			if (position == -1) {
				onSectionClick(adapterView, view, section, id);
			} else {
				onItemClick(adapterView, view, section, position, id);
			}
		}

		public abstract void onItemClick(AdapterView<?> adapterView, View view,
				int section, int position, long id);

		public abstract void onSectionClick(AdapterView<?> adapterView,
				View view, int section, long id);

	}

}
