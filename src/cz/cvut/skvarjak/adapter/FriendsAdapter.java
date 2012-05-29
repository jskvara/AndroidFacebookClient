package cz.cvut.skvarjak.adapter;

import cz.cvut.skvarjak.model.FriendsDataSource;
import cz.cvut.skvarjak.model.ImageDownloader;
import android.content.Context;
import android.database.Cursor;
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