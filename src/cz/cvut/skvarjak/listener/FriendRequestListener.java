package cz.cvut.skvarjak.listener;
/*package cz.cvut.skvarjak;
TODO remove
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.ListView;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class FriendRequestListener implements RequestListener {
	
	protected static final String TAG = "FriendRequestListener";

	@Override
	public void onComplete(final String response) {
//		mSpinner.dismiss();
		try {
			final JSONObject json = new JSONObject(response);
			JSONArray d = json.getJSONArray("data");
			int l = (d != null ? d.length() : 0);
			for (int i = 0; i < 1; i++) {
				JSONObject o = d.getJSONObject(i);
				String n = o.getString("name");
				String id = o.getString("id");
				Friend f = new Friend();
				f.id = id;
				f.name = n;
				friends.add(f);
			}
			
			FacebookClientActivity.this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					friendsArrayAdapter = new FriendsArrayAdapter(FacebookClientActivity.this, R.layout.row_layout, friends);
					ListView.setAdapter(friendsArrayAdapter);
					friendsArrayAdapter.notifyDataSetChanged();
				}
			});
		} catch (JSONException e) {
			Log.w(TAG, "JSON error in response");
		}
	}

	@Override
	public void onIOException(IOException e, Object state) {
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e, Object state) {
	}

	@Override
	public void onMalformedURLException(MalformedURLException e, Object state) {
	}

	@Override
	public void onFacebookError(FacebookError e, Object state) {
	}
}*/