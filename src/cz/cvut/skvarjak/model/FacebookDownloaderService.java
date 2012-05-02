package cz.cvut.skvarjak.model;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.activity.NewsActivity;
import cz.cvut.skvarjak.asyncTask.FacebookDownloaderAsyncTask;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FacebookDownloaderService extends Service {
	protected static final String TAG = "FacebookClient.FacebookDownloaderService";
	public static final String RESPONSE = "response";
	private ServiceDownloaderTask facebookDownloader;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String response = intent.getStringExtra(RESPONSE);
		if (response != null && response.length() > 0) {
//			facebookDownloader = new ServiceDownloaderTask( // TODO
//					getApplicationContext());
//			facebookDownloader.execute();
		}

		return Service.START_FLAG_REDELIVERY;
	}

	private class ServiceDownloaderTask extends FacebookDownloaderAsyncTask {
		private static final int LIST_UPDATE_NOTIFICATION = 100;

		public ServiceDownloaderTask(Context context) {
			super(context, (GlobalState) getApplication());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (updated) {
				Context context = FacebookDownloaderService.this
						.getApplicationContext();
				NotificationManager notificationManager = (NotificationManager) context
						.getSystemService(NOTIFICATION_SERVICE);

				Notification updateComplete = new Notification();
				updateComplete.icon = android.R.drawable.stat_notify_sync;
				updateComplete.tickerText = context
						.getText(R.string.notification_title);
				updateComplete.when = System.currentTimeMillis();
				updateComplete.flags |= Notification.FLAG_AUTO_CANCEL;
				// http://stackoverflow.com/questions/5538969/click-on-notification-starts-activity-twice
				// TODO hide notification, start another Activity

				Intent notificationIntent = new Intent(context,
						NewsActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(
						context, 0, notificationIntent, 0);

				String contentTitle = context.getText(
						R.string.notification_title).toString();
				String contentText;
				if (!result) {
					Log.w(TAG, "Parse response had errors");
					contentText = context.getText(R.string.notification_fail)
							.toString();
				} else {
					contentText = context
							.getText(R.string.notification_success).toString();
				}
				updateComplete.setLatestEventInfo(context, contentTitle,
						contentText, contentIntent);

				notificationManager.notify(LIST_UPDATE_NOTIFICATION,
						updateComplete);
			}

			stopSelf();
		}
	}
}
