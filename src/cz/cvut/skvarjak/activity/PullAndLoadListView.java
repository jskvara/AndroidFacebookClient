package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class PullAndLoadListView extends PullToRefreshListView {
	protected static final String TAG = "FacebookClient.PullAndLoadListView";
	private OnLoadMoreListener mOnLoadMoreListener;
	private boolean mIsLoadingMore = false;
	private RelativeLayout mFooterView;
	private ProgressBar mProgressBarLoadMore;
	private int displayHeight;
	

	public PullAndLoadListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initComponent(context);
	}

	public PullAndLoadListView(Context context) {
		super(context);
		initComponent(context);
	}

	public PullAndLoadListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initComponent(context);
	}

	public void initComponent(Context context) {
		mFooterView = (RelativeLayout) mInflater.inflate(
				R.layout.load_more_footer, this, false);
		mProgressBarLoadMore = (ProgressBar) mFooterView
				.findViewById(R.id.load_more_progressBar);
		addFooterView(mFooterView);
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		displayHeight = displayMetrics.heightPixels;
	}

	public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
		mOnLoadMoreListener = onLoadMoreListener;
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

		if (mOnLoadMoreListener != null) {
			if (visibleItemCount == totalItemCount) {
				mProgressBarLoadMore.setVisibility(View.GONE);
				return;
			}

			boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;

			if (!mIsLoadingMore && loadMore && mRefreshState != REFRESHING
					&& mCurrentScrollState != SCROLL_STATE_IDLE) {
				mProgressBarLoadMore.setVisibility(View.VISIBLE);
				mIsLoadingMore = true;
				onLoadMore();
			}

		}
	}

	public void onLoadMore() {
		Log.d(TAG, "onLoadMore");
		if (mOnLoadMoreListener != null) {
			mOnLoadMoreListener.onLoadMore();
		}
	}

	public void onLoadMoreComplete() {
		Log.d(TAG, "onLoadMoreComplete");
		mIsLoadingMore = false;
		
		if (mFooterView.getTop() < displayHeight) {
			mProgressBarLoadMore.setVisibility(View.GONE);
		}
	}

	public interface OnLoadMoreListener {
		public void onLoadMore();
	}
}