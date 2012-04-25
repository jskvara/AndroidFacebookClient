package cz.cvut.skvarjak.model;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class FriendsAdapter extends SimpleCursorAdapter {
	protected static final String TAG = "FacebookClient.FriendsAdapter";

	public FriendsAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);

		setViewBinder(new FriendsViewBinder());
	}

	public class FriendsViewBinder implements SimpleCursorAdapter.ViewBinder {
		private final ImageDownloader imageDownloader = new ImageDownloader();
		public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
			Log.d(TAG, "ColumnIndex: " + columnIndex);
			int index = cursor
					.getColumnIndex(FriendsDataSource.COLUMN_ID);
			if (columnIndex == index) {
				imageDownloader.downloadProfilePicture(cursor.getString(columnIndex), (ImageView) view);
				return true;
			}
			return false;
		}
	}
}

/*private class FriendsAdapter extends BaseAdapter {
	private final Context mContext;
	private FriendsHolder holder;
	private LayoutInflater mInflater;

	public FriendsAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
	}

	public int getCount() {
		return jsonArray.length();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return 0;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		JSONObject jsonObject = null;
		try {
			jsonObject = jsonArray.getJSONObject(position);
			if (rowView == null) {
				rowView = mInflater.inflate(R.layout.friend_item, null,
						true);
				holder = new FriendsHolder();
				holder.profilePicture = (ImageView) rowView
						.findViewById(R.id.profile_pic);
				holder.name = (TextView) rowView.findViewById(R.id.name);
				holder.info = (TextView) rowView.findViewById(R.id.info);
				rowView.setTag(holder);
			} else {
				holder = (FriendsHolder) rowView.getTag();
			}

			ProfilePicture profilePicture = new ProfilePicture();
			profilePicture.setListener(this);
			holder.profilePicture.setImageBitmap(
			// profilePicture.getImage(jsonObject.getString("id"))
					profilePicture.getUserPic(jsonObject.getString("id")));
			holder.name.setText(jsonObject.getString("name"));
			jsonObject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return rowView;
	}

	private class FriendsHolder {
		ImageView profilePicture;
		TextView name;
		TextView info;
	}
}*/