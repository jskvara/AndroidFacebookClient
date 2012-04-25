package cz.cvut.skvarjak.activity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.FacebookError;
import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import cz.cvut.skvarjak.model.GlobalState;
import cz.cvut.skvarjak.model.NewsDataSource;
import cz.cvut.skvarjak.util.DateUtil;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsActivity extends BaseListActivity {
	protected static final String TAG = "FacebookClient.NewsActivity";
	protected ProgressDialog dialog;
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
		return mNewsDataSource.query(null, null, null, null/*N.COL_TIME*/);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		TextView tv = (TextView)v.findViewById(R.id.from);
		String statusId = tv.getTag().toString();// "1538358746_3698402988840";
		
		dialog = ProgressDialog.show(NewsActivity.this, "",
				getString(R.string.loading));
		AsyncFacebookRunner mAsyncRunner = ((GlobalState) getApplication())
				.getAsyncRunner();
		mAsyncRunner.request(statusId, new StatusRequestListener());
	}

	private class StatusRequestListener extends BaseRequestListener {
		public void onComplete(final String response, final Object state) {
			dialog.dismiss();
			
			if (response.equals("false")) {
				NewsActivity.this.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(getApplicationContext(), 
								getString(R.string.status_not_exist), 
								Toast.LENGTH_LONG).show();
					}
				});
				return;
			}

			Intent intent = new Intent(NewsActivity.this, StatusActivity.class);
			intent.putExtra(StatusActivity.RESPONSE, response);
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			startActivity(intent);
		}

		public void onFacebookError(FacebookError e, Object state) {
			dialog.dismiss();
			NewsActivity.this.runOnUiThread(new Runnable() {
				public void run() {
						Toast.makeText(getApplicationContext(),
								getString(R.string.status_not_exist), 
								Toast.LENGTH_SHORT).show();
				}
			});
			super.onFacebookError(e, state);
		}
		
		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			dialog.dismiss();
			super.onFileNotFoundException(e, state);
		}
		
		@Override
		public void onIOException(IOException e, Object state) {
			dialog.dismiss();
			super.onIOException(e, state);
		}
		
		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			dialog.dismiss();
			super.onMalformedURLException(e, state);
		}
	}

	private class NewsAdapter extends CursorAdapter {
		private static final int TYPE_STATUS = 0;
		private static final int TYPE_PHOTO = 1;
		private static final int TYPE_VIDEO = 2;
		private static final int TYPE_LINK = 3;
		private static final int TYPE_CHECKIN = 4;
		private static final int TYPE_MAX_COUNT = TYPE_CHECKIN + 1;

		private LayoutInflater mInflater;

		public NewsAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getItemViewType(int position) {
			// return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR :
			// TYPE_ITEM;
			return TYPE_STATUS; // TODO
		}

		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();
	        if (holder == null) {
	            holder = new ViewHolder();
	            holder.from = (TextView) view.findViewById(R.id.from);
	            holder.message = (TextView) view.findViewById(R.id.message);
	            holder.time = (TextView) view.findViewById(R.id.time);
	            holder.columnId = cursor.getColumnIndexOrThrow(NewsDataSource.COLUMN_ID);
	            holder.columnFrom = cursor.getColumnIndexOrThrow(NewsDataSource.COLUMN_FROM);
	            holder.columnMessage = cursor.getColumnIndexOrThrow(NewsDataSource.COLUMN_MESSAGE);
	            holder.columnTime = cursor.getColumnIndexOrThrow(NewsDataSource.COLUMN_TIME);
	            holder.columnLikes = cursor.getColumnIndexOrThrow(NewsDataSource.COLUMN_LIKES);
	            holder.columnComments = cursor.getColumnIndexOrThrow(NewsDataSource.COLUMN_COMMENTS);
	            view.setTag(holder);
	        }
	        
	        holder.from.setText(cursor.getString(holder.columnFrom));
	        holder.from.setTag(cursor.getString(holder.columnId));
	        holder.message.setText(cursor.getString(holder.columnMessage)
	        		+ " (" + cursor.getString(holder.columnLikes) + " "
	        		+ getString(R.string.likes) + ", "
	        		+ cursor.getString(holder.columnComments) + " "
	        		+ getString(R.string.comments) + ")");
	        holder.time.setText(DateUtil.format(cursor.getLong(holder.columnTime)));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final View view = mInflater.inflate(R.layout.status_row, parent, false);
			return view;
		}

//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			/*
//			 * View v = convertView; if (v == null) { LayoutInflater vi =
//			 * (LayoutInflater
//			 * )getSystemService(Context.LAYOUT_INFLATER_SERVICE); v =
//			 * vi.inflate(R.layout.news_row, null); } String s =
//			 * items.get(position); if (s != null) { TextView tt = (TextView)
//			 * v.findViewById(R.id.toptext); TextView bt = (TextView)
//			 * v.findViewById(R.id.bottomtext); if (tt != null) {
//			 * tt.setText("Name: "+ s); } if(bt != null){ //
//			 * bt.setText("Status: "+ o.getOrderStatus()); } } return v;
//			 */
//
//			ViewHolder holder = null;
//			int type = getItemViewType(position);
//			System.out.println("getView " + position + " " + convertView
//					+ " type = " + type);
//			if (convertView == null) {
//				holder = new ViewHolder();
//				switch (type) {
//				case TYPE_STATUS:
//					convertView = mInflater.inflate(R.layout.item1, null);
//					holder.textView = (TextView) convertView
//							.findViewById(R.id.text);
//					break;
//				case TYPE_PHOTO:
//					convertView = mInflater.inflate(R.layout.item2, null);
//					holder.textView = (TextView) convertView
//							.findViewById(R.id.textSeparator);
//					break;
//				}
//				convertView.setTag(holder);
//			} else {
//				holder = (ViewHolder) convertView.getTag();
//			}
//			holder.textView.setText(mData.get(position));
//			return convertView;
//		}
	}
	
	static class ViewHolder {
		public TextView from;
		public TextView message;
		public TextView time;
		public int columnId;
		public int columnFrom;
		public int columnMessage;
		public int columnTime;
		public int columnLikes;
		public int columnComments;
	}
}