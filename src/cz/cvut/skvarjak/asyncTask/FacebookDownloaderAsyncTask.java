package cz.cvut.skvarjak.asyncTask;

import java.text.ParseException;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.AsyncTask;
import android.util.Log;
import cz.cvut.skvarjak.model.GlobalState;
import cz.cvut.skvarjak.model.JSONParser;
import cz.cvut.skvarjak.model.NewsDataSource;
import cz.cvut.skvarjak.util.DateUtil;

public class FacebookDownloaderAsyncTask extends
		AsyncTask<String, Void, Boolean> {
	protected static final String TAG = "FacebookClient.FacebookDowloaderAsyncTask";
	protected Context context;
	protected boolean updated = false;
	protected boolean deleteOld = false;
	protected GlobalState globalState;
	protected boolean canSetUntil = false;

	public FacebookDownloaderAsyncTask(Context context, GlobalState globalState) {
		super();
		this.context = context;
		this.globalState = globalState;
	}
	
	public void setDeletOld(boolean deleteOld) {
		this.deleteOld = deleteOld;
	}
	
	public void canSetUntil(boolean canSetUntil) {
		this.canSetUntil = canSetUntil;
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
		
		NewsDataSource newsDataSource = new NewsDataSource(context);
		newsDataSource.open();

		try {
			JSONObject object = new JSONObject(facebookResult);
			setUntil(object);
			
			JSONArray jsonArray = object.getJSONArray("data");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONParser status = new JSONParser(jsonArray.getJSONObject(i));
				Date time = null;
				try {
					time = DateUtil.parse(status.getTime());
				} catch (IllegalArgumentException e) {
					time = new Date();
					Log.w(TAG, e.getMessage(), e);
				} catch (ParseException e) {
					time = new Date();
					Log.w(TAG, e.getMessage(), e);
				}
				
				//Log.d(TAG, "T: "+ time.getTime());
				updated = true;

				String id = status.getId();
				String type = status.getType();
				String from = status.getFrom();
				String fromId = status.getFromId();
				String message = status.getMessage();
				int likes = status.getLikesCount();
				int comments = status.getCommentsCount();
				String link = status.getLink();
				
				String photo = "";
				if (!type.equals("status")) {
					photo = status.getPicture();
				} 

				ContentValues values = new ContentValues();
				values.put(NewsDataSource.COLUMN_ID, id);
				values.put(NewsDataSource.COLUMN_FROM, from);
				values.put(NewsDataSource.COLUMN_FROM_ID, fromId);
				values.put(NewsDataSource.COLUMN_MESSAGE, message);
				values.put(NewsDataSource.COLUMN_TYPE, type);
				values.put(NewsDataSource.COLUMN_TIME, time.getTime());
				values.put(NewsDataSource.COLUMN_LINK, link);
				values.put(NewsDataSource.COLUMN_PHOTO, photo);
				values.put(NewsDataSource.COLUMN_LIKES, likes);
				values.put(NewsDataSource.COLUMN_COMMENTS, comments);

				try {
					newsDataSource.insert(values);
				} catch (SQLiteConstraintException e) { // API returns same posts
				}
			}
			
			if (deleteOld) {
				newsDataSource.deleteOld();
			}

			succeeded = true;
		} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			newsDataSource.close();
		}

		return succeeded;
	}
	
	private void setUntil(JSONObject jsonObject) {
		if (canSetUntil) {
			try {
				String until = jsonObject.getJSONObject("paging").getString("next");
			
				// parse until from paging
				int ind = until.indexOf("until=");
				if (ind != -1) {
					String u = until.substring(ind + 6, ind + 6 + 10); // until has 10 chars
					globalState.setUntil(u);
				}
			} catch (JSONException e) { // empty response body
			}
		}
	}
}
