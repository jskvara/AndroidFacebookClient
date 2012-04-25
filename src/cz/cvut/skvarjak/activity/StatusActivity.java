package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.model.ImageDownloader;
import cz.cvut.skvarjak.model.JSONParser;
import cz.cvut.skvarjak.util.DateUtil;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusActivity extends BaseListActivity {
	protected static final String TAG = "FacebookClient.StatusActivity";
	public static final String RESPONSE = "RESPONSE";
	protected JSONParser jsonParser;
	protected ImageDownloader imageDownloader = new ImageDownloader();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status_layout);

		Bundle extras = getIntent().getExtras();
		String response = extras.getString(RESPONSE);

		jsonParser = new JSONParser(response);

		setListAdapter(new StatusAdapter());
	}

	private class StatusAdapter extends BaseAdapter {
		private static final int TYPE_STATUS = 0;// TODO
		private static final int TYPE_COMMENT = 5;
		private static final int TYPE_MAX_COUNT = TYPE_COMMENT + 1;

		private LayoutInflater mInflater;

		public StatusAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			int count = 1;
			count += jsonParser.getCommentsCount();

			return count;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return position;
		}

		public int getItemViewType(int position) {
			/*
			 * if (position == 0) { return TYPE_STATUS; }
			 */

			return TYPE_COMMENT;
		}

		public int getViewTypeCount() {
			return TYPE_MAX_COUNT;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			int type = getItemViewType(position);
			if (convertView == null) {
				// switch (type) {
				// case TYPE_STATUS:
				// convertView = mInflater.inflate(R.layout.status_row, null);
				// holder.textView =
				// (TextView)convertView.findViewById(R.id.text);
				// break;
				// case TYPE_COMMENT:
				convertView = mInflater.inflate(R.layout.comments_row, null);
				// break;
				// }
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.from = (TextView) convertView
						.findViewById(R.id.from);
				viewHolder.message = (TextView) convertView
						.findViewById(R.id.message);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.time);
				viewHolder.picture = (ImageView) convertView
						.findViewById(R.id.picture);

				convertView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder) convertView.getTag();

			String from;
			String fromId;
			String message;
			String time;
			if (position == 0) {
				from = jsonParser.getFrom();
				fromId = jsonParser.getFromId();
				message = jsonParser.getMessage();
				time = DateUtil.format(jsonParser.getTime());
			} else {
				JSONParser comment = jsonParser.getComment(position - 1);

				from = comment.getFrom();
				fromId = comment.getFromId();
				message = comment.getMessage();
				time = DateUtil.format(comment.getTime());
			}

			holder.from.setText(from);
			holder.message.setText(message);
			holder.time.setText(time);
			imageDownloader.downloadProfilePicture(fromId, holder.picture);

			return convertView;
		}
	}

	static class ViewHolder {
		public TextView from;
		public TextView message;
		public TextView time;
		public ImageView picture;
	}
}