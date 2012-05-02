package cz.cvut.skvarjak.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.model.ImageDownloader;
import cz.cvut.skvarjak.model.NewsDataSource;
import cz.cvut.skvarjak.util.DateUtil;

public class NewsAdapter extends CursorAdapter {
	protected static final String TAG = "FacebookClient.NewsAdapter";
	private static final int TYPE_STATUS = 0;
	private static final int TYPE_PHOTO = 1;
	private static final int TYPE_VIDEO = 2;
	private static final int TYPE_LINK = 3;
	private static final int TYPE_SWF = 4;
	private static final int TYPE_CHECKIN = 5;
	private static final int TYPE_MAX_COUNT = TYPE_CHECKIN + 1;

	protected Context context;
	protected LayoutInflater mInflater;
	protected ImageDownloader imageDownloader = new ImageDownloader();

	public NewsAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		this.context = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return getCursor().getCount();
	}

	public int getItemViewType(int position) {
		Cursor c = getCursor();
		if (!c.isClosed()) {
			c.moveToPosition(position);
			String type = c.getString(c
					.getColumnIndexOrThrow(NewsDataSource.COLUMN_TYPE));
			if (type.equals("status")) {
				return TYPE_STATUS;
			} else if (type.equals("photo")) {
				return TYPE_PHOTO;
			} else if (type.equals("video") || type.equals("swf")) {
				return TYPE_VIDEO;
			} else if (type.equals("link")) {
				return TYPE_LINK;
			} else if (type.equals("checkin")) {
				return TYPE_CHECKIN;
			} else if (type.equals("swf")) {
				return TYPE_SWF;
			} else {
				Log.w(TAG, "Unsupported type: " + type);
			}
		}
		
		return TYPE_STATUS;
	}

	public int getViewTypeCount() {
		return TYPE_MAX_COUNT;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v;
		Cursor c = getCursor();
		if (convertView == null) {
			v = newView(position, c, parent);
		} else {
			v = convertView;
		}
		bindView(v, context, c);
		return v;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		if (cursor.isClosed()) {
			return;
		}

		holder.from.setText(cursor.getString(holder.columnFrom));
		holder.from.setTag(cursor.getString(holder.columnId));
		holder.likes.setText(cursor.getString(
				holder.columnLikes) + " "
				+ context.getString(R.string.likes) + ", "
				+ cursor.getString(holder.columnComments) + " "
				+ context.getString(R.string.comments));
		holder.time.setText(DateUtil.format(cursor.getLong(holder.columnTime)));
		
		imageDownloader.downloadProfilePicture(
				cursor.getString(holder.columnFromId), holder.picture);
		String message = cursor.getString(holder.columnMessage);
		
		String photo = cursor.getString(holder.columnPhoto);
		String type = cursor.getString(holder.columnType);
		if (type.equals("link") || type.equals("photo") || type.equals("video") 
				|| type.equals("swf")) {
			if (holder.photo != null && !photo.equals("")) {
				imageDownloader.downloadPicture(photo, holder.photo);
				
				final String link = cursor.getString(holder.columnLink);
				if (!link.equals("")) {
					holder.photo.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.addCategory(Intent.CATEGORY_BROWSABLE);
							intent.setData(Uri.parse(link));
							context.startActivity(intent);
						}
					});
				}
				
				if (type.equals("video")) {
					message = message + " - " + cursor.getString(holder.columnLink);
				}
			} else {
				holder.photo.setVisibility(View.GONE);
				message = message + " - " + cursor.getString(holder.columnLink);
			}
		}
		
		holder.message.setText(message);
	}

	public View newView(int position, Cursor cursor, ViewGroup parent) {
		View view = null;
		int type = getItemViewType(position);
		switch (type) {
			case TYPE_PHOTO:
			case TYPE_VIDEO:
			case TYPE_LINK:
			case TYPE_SWF:
				view = mInflater.inflate(R.layout.photo_row, parent, false);
				break;
			case TYPE_STATUS:
			default:
				view = mInflater.inflate(R.layout.status_row, parent, false);
				break;
		}

		ViewHolder holder = new ViewHolder();
		holder.picture = (ImageView) view.findViewById(R.id.picture);
		holder.from = (TextView) view.findViewById(R.id.from);
		holder.message = (TextView) view.findViewById(R.id.message);
		holder.time = (TextView) view.findViewById(R.id.time);
		holder.likes = (TextView) view.findViewById(R.id.likes);
		if (type == TYPE_LINK || type == TYPE_PHOTO || type == TYPE_VIDEO || 
				type == TYPE_SWF) {
			holder.photo = (ImageView) view.findViewById(R.id.photo);
		}
		holder.columnId = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_ID);
		holder.columnFrom = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_FROM);
		holder.columnFromId = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_FROM_ID);
		holder.columnType = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_TYPE);
		holder.columnMessage = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_MESSAGE);
		holder.columnPhoto = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_PHOTO);
		holder.columnLink = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_LINK);
		holder.columnTime = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_TIME);
		holder.columnLikes = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_LIKES);
		holder.columnComments = cursor
				.getColumnIndexOrThrow(NewsDataSource.COLUMN_COMMENTS);
		view.setTag(holder);

		return view;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return null;
	}

	static class ViewHolder {
		public ImageView picture;
		public TextView from;
		public TextView message;
		public ImageView photo;
		public TextView time;
		public TextView likes;
		public int columnId;
		public int columnFrom;
		public int columnFromId;
		public int columnType;
		public int columnMessage;
		public int columnPhoto;
		public int columnLink;
		public int columnTime;
		public int columnLikes;
		public int columnComments;
	}
}