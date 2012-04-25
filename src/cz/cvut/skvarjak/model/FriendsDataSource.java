package cz.cvut.skvarjak.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.FacebookError;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

public class FriendsDataSource extends AbstractDataSource {
	public static final String TABLE_NAME = "friends";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";

	public FriendsDataSource(Context ctx) {
		super(ctx);
	}

	public Cursor query(String[] columns, String selection,
			String[] selectionArgs, String sortOrder) {
		return mDb.query(TABLE_NAME, columns, selection, selectionArgs,
				null, null, sortOrder);
	}

	public Cursor queryRow(String id) {
		return mDb.query(TABLE_NAME, null, COLUMN_ID + "=?",
				new String[] { id }, null, null, COLUMN_ID, "1");
	}

	public long insert(ContentValues initialValues) {
		return mDb.insert(TABLE_NAME, null, initialValues);
	}

	public long update(ContentValues values) {
		return mDb.update(TABLE_NAME, values, COLUMN_ID + "=?",
				new String[] { values.getAsString(COLUMN_ID) });
	}

	public int delete(String whereClause, String[] whereArgs) {
		return mDb.delete(TABLE_NAME, whereClause, whereArgs);
	}

	public int deleteRow(Long id) {
		return mDb.delete(TABLE_NAME, COLUMN_ID + "=?", 
				new String[] { id.toString() });
	}

	public void importFriends() {
		GlobalState globalState = (GlobalState) mCtx.getApplicationContext();
		AsyncFacebookRunner mAsyncRunner = globalState.getAsyncRunner();
		mAsyncRunner.request("me/friends", new FriendsRequestListener());
	}

	private class FriendsRequestListener extends BaseRequestListener {
		public void onComplete(final String response, final Object state) {
			try {
				JSONArray jsonArray = new JSONObject(response)
						.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String id = jsonObject.getString("id");
					String name = jsonObject.getString("name");

					ContentValues values = new ContentValues();
					values.put(COLUMN_ID, id);
					values.put(COLUMN_NAME, name);

					/*
					 * Cursor c = queryRow(id); if (c.getCount() > 0) { if (null
					 * == c.getBlob(c.getColumnIndex(FRIENDS_COLUMN_PICTURE))) {
					 * 
					 * } }
					 */

					// TODO illegalStateException database not opened -
					// onDestroy
					// try {
					// if (mDb.isOpen()) {
					Long update = update(values);
					Log.d(TAG, "Update: " + values.getAsString("_id") + ", "
							+ update);
					if (update == 0) {
						// spatnej return
						Log.d(TAG,
								"Update Name: "
										+ values.getAsString(COLUMN_NAME));
						insert(values);
					}
					// }
					// } catch () {}*/
				}
			} catch (JSONException e) {
				Log.w(TAG, e.getMessage(), e);
			}
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			super.onFacebookError(e, state);
			// TODO Handler, message
			Toast.makeText(mCtx.getApplicationContext(),
					"Facebook Error: " + e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}
}