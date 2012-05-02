package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.activity.PullAndLoadListView.OnLoadMoreListener;
import cz.cvut.skvarjak.activity.PullToRefreshListView.OnRefreshListener;
import cz.cvut.skvarjak.adapter.NewsAdapter;
import cz.cvut.skvarjak.asyncTask.FacebookDownloaderAsyncTask;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import cz.cvut.skvarjak.listener.StatusRequestListener;
import cz.cvut.skvarjak.model.GlobalState;
import cz.cvut.skvarjak.model.NewsDataSource;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsActivity extends BaseListActivity {
	protected static final String TAG = "FacebookClient.NewsActivity";
	protected NewsDataSource mNewsDataSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_layout);

		mNewsDataSource = new NewsDataSource(getApplicationContext());
		Log.d(TAG, "open database");
		mNewsDataSource.open();

		CursorAdapter adapter = new NewsAdapter(this, getNewsCursor());
		setListAdapter(adapter);

		PullAndLoadListView lv = ((PullAndLoadListView) getListView());
		lv.setOnRefreshListener(new OnRefreshListener() {
			public void onRefresh() {
				if (!isLogged()) {
					Toast.makeText(NewsActivity.this, R.string.not_logged,
							Toast.LENGTH_LONG).show();
				} else {
					long newestTime = mNewsDataSource.getNewestTime();
					Bundle param = new Bundle();
					param.putString("since", String.valueOf(newestTime / 1000));
					((GlobalState) getApplication()).getAsyncRunner()
							.request(
									"me/home",
									param,
									new RefreshRequestListener(
											getApplicationContext()));
				}
			}
		});

		lv.setOnLoadMoreListener(new OnLoadMoreListener() {
			public void onLoadMore() {
				if (!isLogged()) {
					Toast.makeText(NewsActivity.this, R.string.not_logged,
							Toast.LENGTH_LONG).show();
				} else {
					Bundle param = new Bundle();
					GlobalState gs = (GlobalState) getApplication();
					String until = gs.getUntil();

					if (!until.equals("")) {
						param.putString("since", until);
					} else {
						long oldestTime = mNewsDataSource.getOldestTime();
						param.putString("until",
								String.valueOf(oldestTime / 1000));
					}
					param.putString("limit", "25");

					gs.getAsyncRunner()
							.request(
									"me/home",
									param,
									new LoadMoreRequestListener(
											getApplicationContext()));
				}
			}
		});
	}

	@Override
	protected void onResume() {
		if (!mNewsDataSource.isOpen()) {
			Log.d(TAG, "open database");
			mNewsDataSource.open();
		}
		refreshAdapter();
		super.onResume();
	}

	public void refreshAdapter() {
		((CursorAdapter) getListAdapter()).changeCursor(getNewsCursor());
	}

	@Override
	protected void onPause() {
		Log.d(TAG, "close database");
		((CursorAdapter) getListAdapter()).getCursor().close();
		if (mNewsDataSource != null) {
			mNewsDataSource.close();
		}
		super.onPause();
	}

	public Cursor getNewsCursor() {
		Cursor c = mNewsDataSource.query(null, null, null,
				NewsDataSource.COLUMN_TIME + " DESC");
		return c;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		TextView tv = (TextView) v.findViewById(R.id.from);
		String statusId = tv.getTag().toString();
		// statusId = "1474524002_3765399941594";

		StatusRequestListener srl = new StatusRequestListener(this);
		srl.getStatus(statusId);
	}

	private class RefreshTask extends FacebookDownloaderAsyncTask {
		public RefreshTask(Context context) {
			super(context, (GlobalState) getApplication());
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (isCancelled()) {
				return false;
			}
			setDeletOld(true);

			return super.doInBackground(params);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (mNewsDataSource.isOpen()) {
				refreshAdapter();
			}
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
			((PullAndLoadListView) getListView()).onRefreshComplete();
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			((PullAndLoadListView) getListView()).onRefreshComplete();
		}
	}

	class LoadMoreTask extends FacebookDownloaderAsyncTask {
		public LoadMoreTask(Context context) {
			super(context, (GlobalState) getApplication());
			canSetUntil(true);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (isCancelled()) {
				return null;
			}

			return super.doInBackground(params);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (mNewsDataSource.isOpen()) {
				refreshAdapter();
			}
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
			((PullAndLoadListView) getListView()).onLoadMoreComplete();
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			((PullAndLoadListView) getListView()).onLoadMoreComplete();
		}
	}

	private class RefreshRequestListener extends BaseRequestListener {
		protected Context context;

		public RefreshRequestListener(Context context) {
			this.context = context;
		}

		public void onComplete(String response, Object state) {
			Log.d(TAG, response);
			new RefreshTask(context).execute(response);
		}
	}

	private class LoadMoreRequestListener extends BaseRequestListener {
		protected Context context;

		public LoadMoreRequestListener(Context context) {
			this.context = context;
		}

		public void onComplete(String response, Object state) {
			Log.d(TAG, response);
			new LoadMoreTask(context).execute(response);
		}
	}
}