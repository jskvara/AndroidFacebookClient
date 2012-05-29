package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.asyncTask.FacebookDownloaderAsyncTask;
import cz.cvut.skvarjak.asyncTask.FriendsDownloaderAsyncTask;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import cz.cvut.skvarjak.model.GlobalState;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class FacebookDownloaderService extends Service {
	protected static final String TAG = "FacebookClient.FacebookDownloaderService";
	public static final String RESPONSE = "response";
	public static final String DATA_CHANGED = "data_changed";
	public static final String FRIENDS_CHANGED = "friends_changed";
	private ServiceDownloaderTask facebookDownloader;
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String resp = intent.getStringExtra(RESPONSE);
		if (resp != null && resp.length() > 0) {
			facebookDownloader = new ServiceDownloaderTask(getApplicationContext());
			facebookDownloader.execute(resp);
		}

		return Service.START_REDELIVER_INTENT; 
	}

	private class ServiceDownloaderTask extends FacebookDownloaderAsyncTask {
		private static final int LIST_UPDATE_NOTIFICATION = 100;

		public ServiceDownloaderTask(Context context) {
			super(context, (GlobalState) getApplication());
		}
		
		public void importFriends() {
			Bundle params = new Bundle();
			params.putString("limit", "1000");
			((GlobalState) getApplication()).getAsyncRunner().request("me/friends",
						params, new FriendsRequestListener());
		}

		private class FriendsRequestListener extends BaseRequestListener {
			public void onComplete(final String response, final Object state) {
				new FriendsImportAsyncTask(getApplicationContext())
						.execute(response);
			}
		}

		private class FriendsImportAsyncTask extends FriendsDownloaderAsyncTask {
			public FriendsImportAsyncTask(Context context) {
				super(context);
			}
			
			@Override
			protected void onPostExecute(Boolean result) {
				// send to Activity
				sendBroadcast(new Intent(FacebookDownloaderService.FRIENDS_CHANGED));
				
				super.onPostExecute(result);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			
			// update friends
			importFriends();
			
			if (updated) {
				// send to activity
				sendBroadcast(new Intent(FacebookDownloaderService.DATA_CHANGED));
				
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

				Intent notificationIntent = new Intent(context,
						NewsActivity.class);
				notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
