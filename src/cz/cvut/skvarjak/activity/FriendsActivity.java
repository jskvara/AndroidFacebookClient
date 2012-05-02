package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.adapter.FriendsAdapter;
import cz.cvut.skvarjak.model.FriendsDataSource;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;

public class FriendsActivity extends BaseListActivity {
	protected static final String TAG = "FacebookClient.FriendsActivity";
	protected CursorAdapter adapter;
	protected FriendsDataSource mFriendsDataSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_layout);

		mFriendsDataSource = new FriendsDataSource(this);
		mFriendsDataSource.open();

		adapter = new FriendsAdapter(this, R.layout.friends_row,
				getFriendsCursor(null), new String[] {FriendsDataSource.COLUMN_NAME,
				FriendsDataSource.COLUMN_ID}, new int[] {R.id.name, R.id.profile_pic});
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
		// if (c.getCount() == 0) { // TODO
		mFriendsDataSource.importFriends();
		// c = mFriendsDataSource.query(null, null, null,
		// FriendsDataSource.FRIENDS_COLUMN_NAME);
		// }
		// mFriendsDataSource.delete(null, null); // deleteAll

		return c;
	}

	public Cursor getFriendsCursor(CharSequence s) {
		if (s == null || s.length() == 0) {
			return mFriendsDataSource.query(null, null, null,
					FriendsDataSource.COLUMN_NAME);
		}

		String constraint = "%" + s.toString() + "%";
		return mFriendsDataSource.query(null,
				FriendsDataSource.COLUMN_NAME + " LIKE ?",
				new String[] { constraint },
				FriendsDataSource.COLUMN_NAME);
	}

	@Override
	protected void onResume() {
		mFriendsDataSource.open();
		refreshAdapter();
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		((SimpleCursorAdapter) getListAdapter()).getCursor().close();
		mFriendsDataSource.close();
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
}