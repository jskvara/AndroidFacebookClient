package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.listener.StatusRequestListener;
import cz.cvut.skvarjak.model.GlobalState;
import cz.cvut.skvarjak.model.ImageDownloader;
import cz.cvut.skvarjak.model.JSONParser;
import cz.cvut.skvarjak.util.DateUtil;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends BaseListActivity {
	protected static final String TAG = "FacebookClient.StatusActivity";
	public static final String RESPONSE = "RESPONSE";
	protected JSONParser jsonParser;
	protected ImageDownloader imageDownloader = new ImageDownloader();
	protected String statusId;
	protected StatusAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_layout);

		Bundle extras = getIntent().getExtras();
		String response = extras.getString(RESPONSE);

		jsonParser = new JSONParser(response);
		statusId = jsonParser.getId();

		Button postComment = (Button) findViewById(R.id.post_comment);
		postComment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText et = (EditText) findViewById(R.id.comment_text);
				String comment = et.getText().toString();
				postComment(comment);
			}
		});

		Button like = (Button) findViewById(R.id.like);
		like.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				like();
			}
		});

		mAdapter = new StatusAdapter(getApplicationContext());
		setListAdapter(mAdapter);
	}

	protected void refresh() {
		StatusRequestListener srl = new StatusRequestListener(this);
		srl.getStatus(statusId);
	}

	protected void postComment(String message) {
		message = message.trim();
		if (message.equals("")) {
			Toast.makeText(getApplicationContext(), R.string.comment_empty,
					Toast.LENGTH_LONG).show();
			return;
		}

		Bundle params = new Bundle();
		params.putString("message", message);
		String response = null;
		try {
			response = ((GlobalState) getApplication()).getFacebook().request(
					statusId + "/comments", params, "POST");
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), R.string.comment_not_sent,
					Toast.LENGTH_LONG).show();
			Log.w(TAG, "Comment was not sent", e);
		}

		if (response == null || response.equals("") || response.equals("false")) {
			Toast.makeText(getApplicationContext(), R.string.comment_not_sent,
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.comment_sent,
					Toast.LENGTH_LONG).show();
		}

		refresh();
	}

	protected void like() {
		String response = null;

		try {
			response = ((GlobalState) getApplication()).getFacebook().request(
					statusId + "/likes", new Bundle(), "POST");
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), R.string.like_not_sent,
					Toast.LENGTH_LONG).show();
			Log.w(TAG, "Like was not sent", e);
		}

		if (response == null || response.equals("") || response.equals("false")) {
			Toast.makeText(getApplicationContext(), R.string.like_not_sent,
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getApplicationContext(), R.string.like_sent,
					Toast.LENGTH_LONG).show();
		}

		refresh();
	}

	private class StatusAdapter extends BaseAdapter {
		private static final int TYPE_STATUS = 0;
		private static final int TYPE_PHOTO = 1;
		private static final int TYPE_VIDEO = 2;
		private static final int TYPE_LINK = 3;
		private static final int TYPE_SWF = 4;
		private static final int TYPE_CHECKIN = 5;
		private static final int TYPE_COMMENT = 6;
		private static final int TYPE_MAX_COUNT = TYPE_COMMENT + 1;

		protected Context context;
		protected LayoutInflater mInflater;
		protected ImageDownloader imageDownloader = new ImageDownloader();

		public StatusAdapter(Context context) {
			super();
			this.context = context;
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			int count = 1;
			count += jsonParser.getCommentsCount();

			return count;
		}

		public int getItemViewType(int position) {
			if (position != 0) {
				return TYPE_COMMENT;
			}

			String type = jsonParser.getType();
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

			return TYPE_STATUS;
		}

		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v;
			if (convertView == null) {
				v = newView(position, parent);
			} else {
				v = convertView;
			}
			bindView(v, context, position);
			return v;
		}

		public View newView(int position, ViewGroup parent) {
			View view = null;
			int type = getItemViewType(position);
			switch (type) {
			case TYPE_PHOTO:
			case TYPE_VIDEO:
			case TYPE_LINK:
			case TYPE_SWF:
				view = mInflater.inflate(R.layout.photo_row, parent, false);
				break;
			case TYPE_COMMENT:
				view = mInflater.inflate(R.layout.comments_row, parent, false);
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
			if (type == TYPE_LINK || type == TYPE_PHOTO || type == TYPE_VIDEO
					|| type == TYPE_SWF) {
				holder.photo = (ImageView) view.findViewById(R.id.photo);
			}

			view.setTag(holder);

			return view;
		}

		public void bindView(View view, final Context context, int position) {
			ViewHolder holder = (ViewHolder) view.getTag();

			if (position == 0) {
				holder.from.setText(jsonParser.getFrom());
				holder.likes.setText(jsonParser.getLikesCount() + " "
						+ context.getString(R.string.likes) + ", "
						+ jsonParser.getCommentsCount() + " "
						+ context.getString(R.string.comments));
				String message = jsonParser.getMessage();
				holder.time.setText(DateUtil.format(jsonParser.getTime()));
				imageDownloader.downloadProfilePicture(jsonParser.getFromId(),
						holder.picture);

				String photo = jsonParser.getPicture();
				String type = jsonParser.getType();
				if (type.equals("link") || type.equals("photo")
						|| type.equals("video") || type.equals("swf")) {
					if (holder.photo != null && !photo.equals("")) {
						imageDownloader.downloadPicture(photo, holder.photo);

						final String link = jsonParser.getLink();
						if (!link.equals("")) {
							holder.photo
									.setOnClickListener(new OnClickListener() {
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
							message = message + " - " + jsonParser.getLink();
						}
					} else {
						holder.photo.setVisibility(View.GONE);
						message = message + " - " + jsonParser.getLink();
					}
				}

				holder.message.setText(message);

				return;
			}

			JSONParser comment = jsonParser.getComment(position - 1);
			holder.from.setText(comment.getFrom());
			holder.likes.setText(comment.getLikesCount() + " "
					+ context.getString(R.string.likes));
			holder.message.setText(comment.getMessage());
			holder.time.setText(DateUtil.format(comment.getTime()));
			imageDownloader.downloadProfilePicture(comment.getFromId(),
					holder.picture);
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
	}

	static class ViewHolder {
		public ImageView picture;
		public TextView from;
		public TextView message;
		public ImageView photo;
		public TextView time;
		public TextView likes;
	}
}