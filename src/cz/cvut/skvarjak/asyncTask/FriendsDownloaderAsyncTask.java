package cz.cvut.skvarjak.asyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cz.cvut.skvarjak.model.FriendsDataSource;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;

public class FriendsDownloaderAsyncTask extends
		AsyncTask<String, Void, Boolean> {
	protected static final String TAG = "FacebookClient.FriendsDowloaderAsyncTask";
	protected Context context;

	public FriendsDownloaderAsyncTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		boolean succeeded = false;
		String facebookResult = params[0];

		if (facebookResult != null) {
			succeeded = parse(facebookResult);
		}
		return succeeded;
	}

	private boolean parse(String facebookResult) {
		boolean succeeded = false;

		FriendsDataSource friendsDataSource = new FriendsDataSource(context);
		friendsDataSource.open();
		
		// delete random friends
		friendsDataSource.deleteRandom(20);
		
		try {
			JSONArray jsonArray = new JSONObject(facebookResult)
					.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String id = jsonObject.getString("id");
				String name = jsonObject.getString("name");

				ContentValues values = new ContentValues();
				values.put(FriendsDataSource.COLUMN_ID, id);
				values.put(FriendsDataSource.COLUMN_NAME, name);

				//Log.d(TAG, "insert: " + name);
				try {
					friendsDataSource.insert(values);
				} catch (SQLiteConstraintException e) { // API returns same
														// items
				}
			}

			succeeded = true;
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			friendsDataSource.close();
		}

		return succeeded;
	}
}
