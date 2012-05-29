package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.adapter.FriendsAdapter;
import cz.cvut.skvarjak.model.FriendsDataSource;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FriendsActivity extends BaseListActivity {
	protected static final String TAG = "FacebookClient.FriendsActivity";
	protected CursorAdapter adapter;
	protected FriendsDataSource mFriendsDataSource;
	protected boolean imported = false;
	protected DataUpdateReceiver dataUpdateReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_layout);

		mFriendsDataSource = new FriendsDataSource(this);
		mFriendsDataSource.openReadable();

		Cursor cursor = getFriendsCursor(null);
		if (cursor.getCount() == 0) {
			TextView tv = (TextView) findViewById(android.R.id.empty);
			tv.setText(R.string.loading);
		}

		adapter = new FriendsAdapter(this, R.layout.friends_row, cursor,
				new String[] { FriendsDataSource.COLUMN_NAME,
						FriendsDataSource.COLUMN_ID }, new int[] { R.id.name,
						R.id.profile_pic });
		adapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				Cursor c = getFriendsCursor(constraint);
				return c;
			}
		});
		setListAdapter(adapter);

		EditText et = (EditText) findViewById(R.id.friends_filter);
		TextWatcher tw = new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				adapter.getFilter().filter(s);
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
			}
		};
		et.addTextChangedListener(tw);
	}

	public Cursor getFriendsCursor() {
		Cursor c = mFriendsDataSource.query(null, null, null,
				FriendsDataSource.COLUMN_NAME);
		return c;
	}

	public Cursor getFriendsCursor(CharSequence s) {
		if (s == null || s.length() == 0) {
			return mFriendsDataSource.query(null, null, null,
					FriendsDataSource.COLUMN_NAME);
		}

		String constraint = "%" + s.toString() + "%";
		return mFriendsDataSource.query(null, FriendsDataSource.COLUMN_NAME
				+ " LIKE ?", new String[] { constraint },
				FriendsDataSource.COLUMN_NAME);
	}

	@Override
	protected void onResume() {
		mFriendsDataSource.open();
		refreshAdapter();
		
		if (dataUpdateReceiver == null) {
			dataUpdateReceiver = new DataUpdateReceiver((BaseAdapter)getListAdapter());
		}
		IntentFilter intentFilter = new IntentFilter(FacebookDownloaderService.FRIENDS_CHANGED);
		registerReceiver(dataUpdateReceiver, intentFilter);
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (dataUpdateReceiver != null) {
			unregisterReceiver(dataUpdateReceiver);
		}
		
		((SimpleCursorAdapter) getListAdapter()).getCursor().close();
		mFriendsDataSource.close();
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		((SimpleCursorAdapter) getListAdapter()).getCursor().close();
		mFriendsDataSource.close();
	}

	public void refreshAdapter() {
		((SimpleCursorAdapter) getListAdapter())
				.changeCursor(getFriendsCursor());
	}
	
	private class DataUpdateReceiver extends BroadcastReceiver {
		protected BaseAdapter a;
		
		public DataUpdateReceiver(BaseAdapter a) {
			this.a = a;
		}
		
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(FacebookDownloaderService.FRIENDS_CHANGED)) {
	        	refreshAdapter();
	        	a.notifyDataSetChanged();
	        }
	    }
	}
}