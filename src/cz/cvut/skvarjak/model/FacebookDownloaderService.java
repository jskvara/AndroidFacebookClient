package cz.cvut.skvarjak.model;

import java.text.ParseException;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.activity.NewsActivity;
import cz.cvut.skvarjak.util.DateUtil;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class FacebookDownloaderService extends Service {
	protected static final String TAG = "FacebookClient.FacebookDownloaderService";
	private DownloaderTask facebookDownloader;
	private boolean updated = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// String url = intent.getDataString(); // TODO
		// if (url != null && (url.length() > 0)) {
		facebookDownloader = new DownloaderTask();
		facebookDownloader.execute(r);
		// }
		
		// TODO delete old

		return Service.START_FLAG_REDELIVERY;
	}

	private class DownloaderTask extends AsyncTask<String, Void, Boolean> {
		private static final int LIST_UPDATE_NOTIFICATION = 100;

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
			
			NewsDataSource newsDataSource = new NewsDataSource(FacebookDownloaderService.this);
			newsDataSource.open();
			Cursor c = newsDataSource.queryRowNewest();
			long newestTime = 0;
			if (c.moveToFirst()) {
		        do {
		            newestTime = c.getLong(0);
		        } while(c.moveToNext());
		    }
			c.close();

			try {
				JSONArray jsonArray = new JSONObject(facebookResult)
						.getJSONArray("data");
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
					
					Log.d(TAG, "T: " + time.getTime() + "," + newestTime);
					if (time.getTime() <= newestTime) {
						continue;
					}
					updated = true;
					
					String id = status.getId();
					String type = status.getType();
					String from = status.getFrom();
					String message = status.getMessage();
					int likes = status.getLikesCount();
					int comments = status.getCommentsCount();
					
					ContentValues values = new ContentValues();
					values.put(NewsDataSource.COLUMN_ID, id);
					values.put(NewsDataSource.COLUMN_FROM, from);
					values.put(NewsDataSource.COLUMN_MESSAGE, message);
					values.put(NewsDataSource.COLUMN_TYPE, type);
					values.put(NewsDataSource.COLUMN_TIME, time.getTime());
					values.put(NewsDataSource.COLUMN_LIKES, likes);
					values.put(NewsDataSource.COLUMN_COMMENTS, comments);
					
					newsDataSource.insert(values);
				}
				succeeded = true;
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
			} finally {
				newsDataSource.close();
			}

			return succeeded;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (updated) {
				Context context = FacebookDownloaderService.this.getApplicationContext();
				NotificationManager notificationManager = (NotificationManager) context
						.getSystemService(NOTIFICATION_SERVICE);
	
				Notification updateComplete = new Notification();
				updateComplete.icon = android.R.drawable.stat_notify_sync;
				updateComplete.tickerText = context.getText(R.string.notification_title);
				updateComplete.when = System.currentTimeMillis();
				updateComplete.flags |= Notification.FLAG_AUTO_CANCEL;
				// TODO http://stackoverflow.com/questions/5538969/click-on-notification-starts-activity-twice
				// TODO hide notification, start another Activity
				
				Intent notificationIntent = new Intent(context, NewsActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
						notificationIntent, 0);
	
				String contentTitle = context.getText(R.string.notification_title).toString();
				String contentText;
				if (!result) {
					Log.w(TAG, "Parse response had errors");
					contentText = context.getText(R.string.notification_fail).toString();
				} else {
					contentText = context.getText(R.string.notification_success).toString();
				}
				updateComplete.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
	
				notificationManager.notify(LIST_UPDATE_NOTIFICATION, updateComplete);
			}
			
			stopSelf();
		}
	}

	private static final String r = "{"
			+ "\"data\": [{\"id\": \"1319105922_2938047213113\",\"from\": {\"name\": \"Eli\u0161ka Ja\u010dkov\u00e1\",\"id\": \"1319105922\"},\"to\": {\"data\":[{\"name\": \"Andrea K\u00f6nigov\u00e1\",\"id\": \"1137040464\"}]},\"with_tags\": {\"data\": [{\"name\": \"Andrea K\u00f6nigov\u00e1\",\"id\": \"1137040464\"}]},\"picture\": \"https://fbcdn-profile-a.akamaihd.net/static-ak/rsrc.php/v1/yW/r/U2hFrus9i5v.png\",\"link\": \"https://www.facebook.com/pages/McDonalds/230428570301765\",\"name\": \"McDonalds\",\"caption\": \"Eli\u0161ka checked in at McDonalds.\",\"icon\": \"https://www.facebook.com/images/icons/place.png\",\"actions\": [{\"name\": \"Comment\",\"link\": \"https://www.facebook.com/1319105922/posts/2938047213113\"},{\"name\": \"Like\",\"link\": \"https://www.facebook.com/1319105922/posts/2938047213113\"}],\"place\": {\"id\": \"230428570301765\",\"name\": \"McDonalds\",\"location\": {\"city\": \"Wollein\",\"country\": \"Czech Republic\",\"latitude\": 49.377573351781,\"longitude\": 15.947404156113}},\"type\": \"checkin\",\"application\": {\"name\": \"Facebook for iPad\",\"id\": \"173847642670370\"},\"created_time\": \"2012-04-21T12:05:25+0000\",\"updated_time\": \"2012-04-21T12:05:25+0000\",\"likes\": {\"data\": [{\"name\": \"Mi\u0161ulka Navr\u00e1til\u01ffv\u00e1\",\"id\": \"1615156470\"}],\"count\": 1},\"comments\": {\"count\": 0}},"
			+ "{\"id\":\"1733552314_2066140469804\",\"from\": {\"name\": \"Bella Carina Nisterenkov\u00e1\",\"id\": \"1733552314\"},\"message\": \"N\u011bjak\u00fd jointy??? :))\",\"actions\": [{\"name\": \"Comment\",\"link\": \"https://www.facebook.com/1733552314/posts/2066140469804\"},{\"name\": \"Like\",\"link\": \"https://www.facebook.com/1733552314/posts/2066140469804\"}],\"type\": \"status\",\"created_time\": \"2012-04-21T12:02:58+0000\",\"updated_time\": \"2012-04-21T12:04:43+0000\",\"comments\": {\"data\": [{\"id\": \"1733552314_2066140469804_1816875\",\"from\": {\"name\": \"Elka Selka\",\"id\": \"1704684618\"},\"message\": \"yes. :)\",\"created_time\": \"2012-04-21T12:04:43+0000\"}],\"count\": 1}},"
			+ "{\"id\":\"1299222213_3078929534420\",\"from\": {\"name\": \"Tom\u00e1\u0161 Gul\u00e1\u0161 Prajzler\",\"id\": \"1299222213\" }, \"story\": \"Tom\u00e1\u0161 Gul\u00e1\u0161 Prajzler is in Turbenthal, Zurich.\", \"actions\": [{\"name\": \"Comment\",\"link\": \"https://www.facebook.com/1299222213/posts/3078929534420\"},{\"name\": \"Like\",\"link\": \"https://www.facebook.com/1299222213/posts/3078929534420\"}],\"place\": {\"id\":\"108721425825987\",\"name\": \"Turbenthal, Switzerland\",\"location\": {\"latitude\": 47.4333,\"longitude\": 8.85}}, \"type\": \"status\", \"created_time\": \"2012-04-21T11:56:57+0000\", \"updated_time\": \"2012-04-21T11:56:57+0000\", \"likes\": {\"data\": [{\"name\": \"Evu\u0161 Mike\u0161ov\u00e1\",\"id\": \"1755413877\"},{\"name\": \"Minh Do Hai\",\"id\": \"1244074453\"},{\"name\": \"Marie Prajzlerov\u00e1\",\"id\": \"1385912191\"},{\"name\": \"Ond\u0159ej Jando\u0161\",\"id\": \"1416690672\"}],\"count\": 5},\"comments\": {\"count\":0}},"
			+ "{\"id\":\"106654476032467_126451987488013\", \"from\": {\"name\": \"Twisted Rod\",\"category\": \"Musician/band\",\"id\": \"106654476032467\"}, \"message\": \"D\u00edky v\u0161em, co v\u010dera p\u0159i\u0161li do Mighty Baru, bylo to super! A za chv\u00edli vyr\u00e1\u017e\u00edme sm\u011br Brno!!\","
			+ "\"link\": \"https://www.facebook.com/photo.php?fbid=293676600708654&set=a.134793959930253.34496.129061447170171&type=1\",\"name\": \"Wall Photos\", \"caption\": \"21.4. na House Inn, Twisted Rod\u2026\", \"properties\": [{   \"name\": \"By\",   \"text\": \"House Inn: beers, liquers, music\",   \"href\": \"https://www.facebook.com/pages/House-Inn-beers-liquers-music/129061447170171\"} ], \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yD/r/aS8ecmYRys0.gif\",\"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/106654476032467/posts/126451987488013\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/106654476032467/posts/126451987488013\"} ], \"type\": \"photo\", \"object_id\": \"293676600708654\", \"application\": {\"name\": \"Links\",\"id\": \"2309869772\" }, \"created_time\": \"2012-04-21T11:55:43+0000\", \"updated_time\": \"2012-04-21T11:55:43+0000\", \"comments\": {\"count\": 0 }  },  { \"id\": \"1662195710_3494964187125\", \"from\": {\"name\": \"Adam Pr\u00edma Dole\u017eal\",\"id\": \"1662195710\" }, \"message\": \"douf\u00e1m \u017ee tam ve\u010der budou skre\u010de\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1662195710/posts/3494964187125\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1662195710/posts/3494964187125\"} ], \"type\": \"status\", \"created_time\": \"2012-04-21T11:43:43+0000\", \"updated_time\": \"2012-04-21T11:43:43+0000\", \"comments\": {\"count\": 0 }  },  { \"id\": \"1755413877_389600461071930\", \"from\": {\"name\": \"Evu\u0161 Mike\u0161ov\u00e1\",\"id\": \"1755413877\" }, \"message\": \"http://www.youtube.com/watch?v=DMR9_f2QBG0&feature=related\", \"picture\": \"https://s-external.ak.fbcdn.net/safe_image.php?d=AQDT_GyRNdJJjTuH&w=130&h=130&url=http\u00253A\u00252F\u00252Fi1.ytimg.com\u00252Fvi\u00252FDMR9_f2QBG0\u00252Fhqdefault.jpg\", \"link\": \"http://www.youtube.com/watch?v=DMR9_f2QBG0&feature=related\", \"source\": \"http://www.youtube.com/v/DMR9_f2QBG0?version=3&autohide=1&autoplay=1\", \"name\": \"Duality - Slipknot\", \"description\": \"the song Duality by Slipknot\", \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yj/r/v2OnaTyTQZE.gif\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1755413877/posts/389600461071930\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1755413877/posts/389600461071930\"} ], \"type\": \"video\", \"created_time\": \"2012-04-21T11:42:18+0000\", \"updated_time\": \"2012-04-21T11:42:18+0000\", \"comments\": {\"count\": 0 }  },  { \"id\": \"1214441432_105213746280692\", \"from\": {\"name\": \"Andrej Andyboj Urysiak\",\"id\": \"1214441432\" }, \"message\": \"NEPREME\u0160KAJ!!!\", \"picture\": \"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc4/373048_175926325853999_1026914706_t.jpg\", \"link\": \"https://www.facebook.com/events/175926325853999/\", \"name\": \"SUMMER STREET DANCE SCHOOL BDS 2012 od 16 let!\", \"description\": \"Thursday, July 26 at 9:00am\", \"properties\": [{   \"text\": \"Join\"} ], \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yD/r/aS8ecmYRys0.gif\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1214441432/posts/105213746280692\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1214441432/posts/105213746280692\"} ], \"type\": \"link\", \"application\": {\"name\": \"Events\",\"id\": \"2344061033\" }, \"created_time\": \"2012-04-21T11:40:50+0000\", \"updated_time\": \"2012-04-21T11:40:50+0000\", \"comments\": {\"count\": 0 }  },  { \"id\": \"1214441432_372274899478021\", \"from\": {\"name\": \"Andrej Andyboj Urysiak\",\"id\": \"1214441432\" }, \"message\": \"NEPREME\u0160KAJ!!!\", \"picture\": \"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc4/162026_301507033237364_456475927_t.jpg\", \"link\": \"https://www.facebook.com/events/301507033237364/\", \"name\": \"SUMMER STREET DANCE SCHOOL BDS do 15 let!\", \"description\": \"Thursday, August 2 at 9:00am\", \"properties\": [{   \"text\": \"Join\"} ], \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yD/r/aS8ecmYRys0.gif\", \"actions\": [{\"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1214441432/posts/372274899478021\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1214441432/posts/372274899478021\"} ], \"type\": \"link\", \"application\": {\"name\": \"Events\","
			+ "\"id\": \"2344061033\"},\"created_time\": \"2012-04-21T11:40:34+0000\",\"updated_time\": \"2012-04-21T11:40:34+0000\",\"comments\": {\"count\": 0 }  },  { \"id\": \"1231706677_3757682300564\", \"from\": {\"name\": \"Lin Da Mat\u00e1skov\u00e1\",\"id\": \"1231706677\" }, \"message\": \"bude kerka. ale ne od MUSY :(:( ani z triba.\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1231706677/posts/3757682300564\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1231706677/posts/3757682300564\"} ], \"type\": \"status\", \"created_time\": \"2012-04-21T11:11:12+0000\", \"updated_time\": \"2012-04-21T11:36:01+0000\", \"comments\": {\"data\": [   {  \"id\": \"1231706677_3757682300564_4704843\",  \"from\": { \"name\": \"Kry\u0161tof Kot\u00e1b\", \"id\": \"1437171035\"  },  \"message\": \"zajmavy. a jak se jmenuje ? ja du prvniho kvetna uz konecne do triba\",  \"created_time\": \"2012-04-21T11:34:12+0000\"   },   {  \"id\": \"1231706677_3757682300564_4704846\",  \"from\": { \"name\": \"Lin Da Mat\u00e1skov\u00e1\", \"id\": \"1231706677\"  },  \"message\": \"vojta linart, kluk z krumlova ale v pha studuje socharinu.\",  \"created_time\": \"2012-04-21T11:36:01+0000\",  \"likes\": 1   }],\"count\": 5 }  },  { \"id\": \"1272782923_3761173549213\", \"from\": {\"name\": \"Kafka Vysko\u010dilov\u00e1\",\"id\": \"1272782923\" }, \"story\": \"Kafka Vysko\u010dilov\u00e1 updated her cover photo.\", \"picture\": \"https://fbcdn-photos-a.akamaihd.net/hphotos-ak-ash3/564353_3761173309207_1272782923_33663126_1195431051_s.jpg\", \"link\": \"https://www.facebook.com/photo.php?fbid=3761173309207&set=a.3089014625660.2160992.1272782923&type=1\", \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yz/r/StEh3RhPvjk.gif\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1272782923/posts/3761173549213\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1272782923/posts/3761173549213\"} ], \"type\": \"photo\", \"object_id\": \"3761173309207\", \"created_time\": \"2012-04-21T11:10:58+0000\", \"updated_time\": \"2012-04-21T11:10:58+0000\", \"likes\": {\"data\": [   {  \"name\": \"Johana N\u011bme\u010dkov\u00e1\",  \"id\": \"1452336144\"   }],\"count\": 2 }, \"comments\": {\"count\": 0 }  },  { \"id\": \"1398949162_392881204078635\", \"from\": {\"name\": \"Krist\u00fdna Janou\u0161kov\u00e1\",\"id\": \"1398949162\" }, \"message\": \"http://www.youtube.com/watch?v=IIhZLr5B8kE\", \"picture\": \"https://s-external.ak.fbcdn.net/safe_image.php?d=AQBadJzPF-yhCA3N&w=130&h=130&url=http\u00253A\u00252F\u00252Fi2.ytimg.com\u00252Fvi\u00252FIIhZLr5B8kE\u00252Fhqdefault.jpg\", \"link\": \"http://www.youtube.com/watch?v=IIhZLr5B8kE\", \"source\": \"http://www.youtube.com/v/IIhZLr5B8kE?version=3&autohide=1&autoplay=1\", \"name\": \"Flux Pavilion - Got 2 Know\", \"description\": \"More disgusting dancefloor insanity from Flux and the Circus camp! http://www.myspace.com/fluxpavilion http://www.circus-records.co.uk\", \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yj/r/v2OnaTyTQZE.gif\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1398949162/posts/392881204078635\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1398949162/posts/392881204078635\"} ], \"type\": \"video\", \"created_time\": \"2012-04-21T10:35:13+0000\", \"updated_time\": \"2012-04-21T10:35:13+0000\", \"likes\": {\"data\": [   {  \"name\": \"Milan Li\u0161\u00e1k\",  \"id\": \"100000275041993\"   }],\"count\": 1 }, \"comments\": {\"count\": 0 }  },  { \"id\": \"1385912191_418372424841498\", \"from\": {\"name\": \"Marie Prajzlerov\u00e1\",\"id\": \"1385912191\" }, \"message\": \":DDDD\", \"link\": \"https://www.facebook.com/photo.php?fbid=300012526740491&set=a.155722104502868.39591.151782214896857&type=1\", \"name\": \"Wall Photos\", \"caption\": \"Vid\u00ed\u0161 se v tom? :DD LIKE\", \"properties\": [{   \"name\": \"By\",   \"text\": \"Pan vtipn\u00fd\",   \"href\": \"https://www.facebook.com/PanVtipny\"}], \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yD/r/aS8ecmYRys0.gif\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1385912191/posts/418372424841498\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1385912191/posts/418372424841498\"} ], \"type\": \"photo\", \"object_id\": \"300012526740491\", \"application\": {\"name\": \"Photos\",\"id\": \"2305272732\" }, \"created_time\": \"2012-04-21T10:18:59+0000\", \"updated_time\": \"2012-04-21T10:18:59+0000\",\"likes\": {\"data\": [   {  \"name\": \"Raylen Jircov\u00e1\",  \"id\": \"1217674698\"   },   {  \"name\": \"Eli\u0161ka Mifkov\u00e1\",  \"id\": \"1537261361\"   }],\"count\": 3 }, \"comments\": {\"count\": 0 }  },  { \"id\": \"1323604406_372304062807641\", \"from\": {\"name\": \"Tom\u00e1\u0161 K\u00e1pl\",\"id\": \"1323604406\" }, \"message\": \"N\u011bco m\u00e1lo o t\u00e9 skv\u011bl\u00e9 reklam\u011b Budvaru\", \"picture\": \"https://s-external.ak.fbcdn.net/safe_image.php?d=AQCILfnQFucWytRd&w=90&h=90&url=http\u00253A\u00252F\u00252Fwww.mediar.cz\u00252Fwp-content\u00252Fthumbnails\u00252F29013-w100-h.jpg\", \"link\": \"http://www.mediar.cz/budvar-ale-my-jsme-rekli-ne/\", \"name\": \"\u2026 ale my jsme \u0159ekli NE!\", \"caption\": \"www.mediar.cz\", \"description\": \"Budvar p\u0159edstavil kampa\u0148, ze kter\u00e9 m\u00e1m sm\u00ed\u0161en\u00e9 pocity, ov\u0161em velmi siln\u00e9 sm\u00ed\u0161en\u00e9 pocity.\", \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yf/r/0HZPW6-lhQu.png\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/1323604406/posts/372304062807641\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/1323604406/posts/372304062807641\"} ], \"type\": \"link\", \"application\": {\"name\": \"Likes\",\"namespace\": \"awesomeprops\",\"id\": \"2409997254\" }, \"created_time\": \"2012-04-21T10:12:14+0000\", \"updated_time\": \"2012-04-21T10:12:14+0000\", \"likes\": {\"data\": [   {  \"name\": \"Petr Miko\",  \"id\": \"1316043455\"   }],\"count\": 2 }, \"comments\": {\"count\": 0 }  },  { \"id\": \"100000667819825_392482510784006\", \"from\": {\"name\": \"Katka \u010cechov\u00e1\",\"id\": \"100000667819825\" }, \"message\": \"Ha,jde se demonstrovat!!\", \"actions\": [{   \"name\": \"Comment\",   \"link\": \"https://www.facebook.com/100000667819825/posts/392482510784006\"},{   \"name\": \"Like\",   \"link\": \"https://www.facebook.com/100000667819825/posts/392482510784006\"} ], \"type\": \"status\","
			+ "\"created_time\": \"2012-04-21T10:10:02+0000\",\"updated_time\": \"2012-04-21T10:10:02+0000\",\"likes\": {"
			+ "\"data\": [{\"name\": \"Radunka Mala\u0161\u010denkova\",\"id\": \"1414269323\"}],\"count\": 1},\"comments\": {\"count\": 0			 }			  },			  {			 \"id\": \"659221672_10150672001136673\",			 \"from\": {			\"name\": \"Ondra Macoszek\",			\"id\": \"659221672\"			 },			 \"message\": \"Tak dnes m\u016f\u017eu cel\u00fdch 24 hodin oslavovat sv\u00fdch 24 let. Cha! 8)\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/659221672/posts/10150672001136673\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/659221672/posts/10150672001136673\"			}			 ],			 \"type\": \"status\",			 \"created_time\": \"2012-04-21T10:05:45+0000\",			 \"updated_time\": \"2012-04-21T11:29:34+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Evka Hamouzov\u00e1\",			  \"id\": \"100000036630273\"			   }			],			\"count\": 5			 },			 \"comments\": {			\"data\": [			   {			  \"id\": \"659221672_10150672001136673_21139177\",			  \"from\": {			 \"name\": \"Evka Hamouzov\u00e1\",			 \"id\": \"100000036630273\"			  },			  \"message\": \"V\u0161echno nejlep\u010d\u00ed! :)\",			  \"created_time\": \"2012-04-21T11:21:01+0000\"			   },			   {			  \"id\": \"659221672_10150672001136673_21139241\",			  \"from\": {			 \"name\": \"Ondra Macoszek\",			 \"id\": \"659221672\"			  },			  \"message\": \"D\u00edk Evi! :)\",			  \"created_time\": \"2012-04-21T11:29:34+0000\"			   }			],			\"count\": 2			 }			  },			  {			 \"id\": \"1240825328_3033147467950\",			 \"from\": {			\"name\": \"Ani\u010dka V\u00e1lkov\u00e1\",			\"id\": \"1240825328\"			 },			 \"to\": {			\"data\": [			   {			  \"name\": \"Michal Hu\u017ev\u00e1r\",			  \"id\": \"1032720899\"			   },			   {			  \"name\": \"Milan V\u00e1lek\",			  \"id\": \"1391353913\"			   }			]			 },			 \"with_tags\": {			\"data\": [			   {			  \"name\": \"Michal Hu\u017ev\u00e1r\",			  \"id\": \"1032720899\"			   },			   {			  \"name\": \"Milan V\u00e1lek\",			  \"id\": \"1391353913\"			   }			]			 },			 \"message\": \"\u010dek\u00e1me na hv\u011bzdu. :)\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1240825328/posts/3033147467950\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1240825328/posts/3033147467950\"			}			 ],			 \"place\": {			\"id\": \"120717197981284\",			\"name\": \"Prague Ruzyn\u011b International Airport\",			\"location\": {			   \"street\": \"K Leti\u0161ti 6/1019\",			   \"city\": \"Prague\",			   \"country\": \"Czech Republic\",			   \"zip\": \"160 08\",			   \"latitude\": 50.106366590306,			   \"longitude\": 14.266910667218			}			 },			 \"type\": \"status\",			 \"application\": {			\"name\": \"Mobile\",			\"id\": \"2915120374\"			 },			 \"created_time\": \"2012-04-21T10:02:25+0000\",			 \"updated_time\": \"2012-04-21T10:02:25+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Zden\u011bk Masakr Such\u00fd\",			  \"id\": \"1581142457\"			   }			],			\"count\": 7			 },			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"1214441432_3058412178673\",			 \"from\": {			\"name\": \"Andrej Andyboj Urysiak\",			\"id\": \"1214441432\"			 },			 \"message\": \"Tak od dnes zase ZABAVA od 15.00 do 18.00:)\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1214441432/posts/3058412178673\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1214441432/posts/3058412178673\"			}			 ],			 \"type\": \"status\",			 \"created_time\": \"2012-04-21T09:59:51+0000\",			 \"updated_time\": \"2012-04-21T09:59:51+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Shahen Shahenyan\",			  \"id\": \"1122093722\"			   }			],			\"count\": 1			 },			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"1398949162_381628375215330\",			 \"from\": {			\"name\": \"Krist\u00fdna Janou\u0161kov\u00e1\",			\"id\": \"1398949162\"			 },			 \"story\": \"Krist\u00fdna Janou\u0161kov\u00e1 shared a link.\",			 \"picture\": \"https://s-external.ak.fbcdn.net/safe_image.php?d=AQC45O2fXriyRblP&w=130&h=130&url=http\u00253A\u00252F\u00252Fi4.ytimg.com\u00252Fvi\u00252FgGCh3iWYosY\u00252Fhqdefault.jpg\",			 \"link\": \"http://www.youtube.com/watch?v=gGCh3iWYosY\",			 \"source\": \"http://www.youtube.com/v/gGCh3iWYosY?version=3&autohide=1&autoplay=1\",			 \"name\": \"Lucie - Chci zas v tobe spat\",			 \"description\": \"Music video by Lucie performing Chci zas v tobe spat. (C) 2003 Sony Music / Bonton\",			 \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yj/r/v2OnaTyTQZE.gif\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1398949162/posts/381628375215330\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1398949162/posts/381628375215330\"			}			 ],			 \"type\": \"video\",			 \"created_time\": \"2012-04-21T09:10:31+0000\",			 \"updated_time\": \"2012-04-21T09:10:31+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Jakub Sv\u00e1tek\",			  \"id\": \"1435237759\"			   }			],			\"count\": 2			 },			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"100000763992341_363889710313159\",			 \"from\": {"
			+ "\"name\": \"Luk\u00e1\u0161 LakyLuky Musil\",\"id\": \"100000763992341\"},\"picture\": \"https://www.facebook.com/app_full_proxy.php?app=122704601091616&v=1&size=z&cksum=d348baa068b23258310261ce92dc746d&src=http\u00253A\u00252F\u00252F46.4.18.42\u00252Fapp\u00252F_gena\u00252Fangel_devil\u00252Fimg\u00252F0\u00252Fpercent\u00252F40.png\",			 \"link\": \"http://apps.facebook.com/angdevapp/?sc=12&parUid=100000763992341&pidUid=947925\",			 \"name\": \"You today are 40\u0025 Devil!\",\"icon\": \"https://fbcdn-photos-a.akamaihd.net/photos-ak-snc7/v27562/208/122704601091616/app_2_122704601091616_5218.gif\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/100000763992341/posts/363889710313159\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/100000763992341/posts/363889710313159\"			},			{			   \"name\": \"Mark as spam / Report\",			   \"link\": \"http://apps.facebook.com/angdevapp/?sc=22&uid=100000763992341&report\"			}			 ],			 \"type\": \"link\",			 \"application\": {			\"name\": \"Angel or Devil !\",			\"namespace\": \"angdevapp\",			\"id\": \"122704601091616\"			 },			 \"created_time\": \"2012-04-21T08:57:28+0000\",			 \"updated_time\": \"2012-04-21T08:57:28+0000\",			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"51538103524_173515342770310\",			 \"from\": {			\"name\": \"M1 Lounge\",			\"category\": \"Bar\",			\"id\": \"51538103524\"			 },			 \"message\": \"Sex in the City tonight!\n\nhttps://www.facebook.com/events/193888170729564/\",			 \"picture\": \"https://fbcdn-profile-a.akamaihd.net/hprofile-ak-snc4/373040_193888170729564_278975192_t.jpg\",			 \"link\": \"https://www.facebook.com/events/193888170729564/\",			 \"name\": \"Sex in the City / M1 Lounge / Saturday 21.4\",			 \"description\": \"Today at 9:00pm at M1 Lounge\",			 \"properties\": [			{			   \"text\": \"Join\"			}			 ],			 \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yD/r/aS8ecmYRys0.gif\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/51538103524/posts/173515342770310\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/51538103524/posts/173515342770310\"			}			 ],			 \"type\": \"link\",			 \"created_time\": \"2012-04-21T08:39:36+0000\",			 \"updated_time\": \"2012-04-21T08:39:36+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Dahlia Dirty-d Rawji\",			  \"id\": \"559855033\"			   }			],			\"count\": 3			 },			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"112304865453234_430699026947148\",			 \"from\": {			\"name\": \"Hentai Corporation\",			\"category\": \"Musician/band\",			\"id\": \"112304865453234\"			 },			 \"message\": \"danke ustiiii!!!\n\npa davidoff\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/112304865453234/posts/430699026947148\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/112304865453234/posts/430699026947148\"			}			 ],			 \"type\": \"status\",			 \"created_time\": \"2012-04-21T07:47:40+0000\",			 \"updated_time\": \"2012-04-21T07:47:40+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Stanley Eichler\",			  \"id\": \"1314110488\"			   }			],			\"count\": 8			 },			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"1809462182_335476386507340\",			 \"from\": {			\"name\": \"Vojta Navr\u00e1til\",			\"id\": \"1809462182\"			 },			 \"story\": \"Vojta Navr\u00e1til shared a link.\",			 \"picture\": \"https://s-external.ak.fbcdn.net/safe_image.php?d=AQDAqxpNsyTKmNb5&w=90&h=90&url=http\u00253A\u00252F\u00252Fpobavilo.eu\u00252Fwp-content\u00252Fuploads\u00252F2011\u00252F08\u00252Fnorm\u0025C3\u0025A1lka-n\u0025C3\u0025A9\u0025C3\u0025A9\u0025C3\u0025A9.jpg\",			 \"link\": \"http://pobavilo.eu/obrazky/proc-ne/\",			 \"name\": \"Pro\u010d ne ? - Pobavilo\",			 \"caption\": \"pobavilo.eu\",			 \"description\": \"Taky zvete n\u00e1v\u0161t\u011bvu a\u0165 se posad\u00ed na z\u00e1chod ?\",			 \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yD/r/aS8ecmYRys0.gif\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1809462182/posts/335476386507340\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1809462182/posts/335476386507340\"			}			 ],			 \"type\": \"link\","
			+ "\"application\": {\"name\": \"Share_bookmarklet\",\"id\": \"5085647995\"},\"created_time\": \"2012-04-21T07:20:31+0000\",			 \"updated_time\": \"2012-04-21T07:20:31+0000\",			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"1082496699_3383606543931\",			 \"from\": {			\"name\": \"Michal \u0160v\u00e1cha\",			\"id\": \"1082496699\"},\"story\": \"Michal \u0160v\u00e1cha added a photo from September 25, 2007 to his timeline.\",			 \"picture\": \"https://fbcdn-photos-a.akamaihd.net/hphotos-ak-snc6/148994_3383606303925_275431213_s.jpg\",			 \"link\": \"https://www.facebook.com/photo.php?fbid=3383606303925&set=a.1268040336098.2038613.1082496699&type=1\",			 \"icon\": \"https://s-static.ak.facebook.com/rsrc.php/v1/yz/r/StEh3RhPvjk.gif\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1082496699/posts/3383606543931\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1082496699/posts/3383606543931\"			}			 ],			 \"place\": {			\"id\": \"248519261832819\",			\"name\": \"Battle of Hastings Abbey and Battlefield\",			\"location\": {			   \"latitude\": 50.90336,			   \"longitude\": 0.58583			}			 },			 \"type\": \"photo\",			 \"object_id\": \"3383606303925\",			 \"created_time\": \"2012-04-21T07:18:58+0000\",			 \"updated_time\": \"2012-04-21T08:30:34+0000\",			 \"likes\": {			\"data\": [			   {			  \"name\": \"Magdalena Svobodov\u00e1\",			  \"id\": \"1243113820\"			   }			],			\"count\": 1			 },			 \"comments\": {			\"data\": [			   {			  \"id\": \"1082496699_3383606543931_2080798\",			  \"from\": {			 \"name\": \"Michal \u0160v\u00e1cha\",			 \"id\": \"1082496699\"			  },			  \"message\": \"zrza..coo?:D\",			  \"created_time\": \"2012-04-21T08:30:23+0000\"			   },			   {			  \"id\": \"1082496699_3383606543931_2080799\",			  \"from\": {			 \"name\": \"Ond\u0159ej Radosta\",			 \"id\": \"1237737342\"			  },			  \"message\": \"to byla a\u017e druh\u00e1 varianta :-D\",			  \"created_time\": \"2012-04-21T08:30:34+0000\"			   }			],			\"count\": 7			 }			  },			  {			 \"id\": \"1462720201_275582192534774\",			 \"from\": {			\"name\": \"Patrik Kroc\",			\"id\": \"1462720201\"			 },			 \"picture\": \"https://www.facebook.com/app_full_proxy.php?app=137827210650&v=1&size=z&cksum=7bd373c6a1b84a2332864d4e3ddc37f3&src=http\u00253A\u00252F\u00252Fwww.ninjasaga.com\u00252Fimages\u00252Ffriend_reward\u00252Ffr_lucky_draw_gold.jpg\",			 \"link\": \"https://app.ninjasaga.com/tracking/wallfeed_2.0.php?st=fr_lucky_draw_gold&feed_id=1190&lang=en&fr_ref_id=496724511&uuid=4f924f8b8d1f3e61\",			 \"name\": \"Patrik Kroc has free Gold to share!\",			 \"caption\": \"www.ninjasaga.com\",			 \"description\": \"Patrik Kroc finished the Ninja Scratch Card and received extra Gold to gift ninja friends who are fast!\",			 \"icon\": \"https://fbcdn-photos-a.akamaihd.net/photos-ak-snc7/v43/150/137827210650/app_2_137827210650_2205.gif\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1462720201/posts/275582192534774\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1462720201/posts/275582192534774\"			},			{			   \"name\": \"Get Gold Bonus\",			   \"link\": \"https://app.ninjasaga.com/tracking/wallfeed_2.0.php?st=fr_lucky_draw_gold&feed_id=1192&uuid=4f924f8b8d1f3e61&lang=en&fr_ref_id=496724511\"			}			 ],			 \"type\": \"link\",			 \"application\": {			\"name\": \"Ninja Saga\",			\"namespace\": \"ninjasaga\",			\"id\": \"137827210650\"			 },			 \"created_time\": \"2012-04-21T06:11:40+0000\",			 \"updated_time\": \"2012-04-21T06:11:40+0000\",			 \"comments\": {			\"count\": 0			 }			  },			  {			 \"id\": \"1231706677_3756749117235\",			 \"from\": {			\"name\": \"Lin Da Mat\u00e1skov\u00e1\",			\"id\": \"1231706677\"			 },			 \"message\": \"dobr\u00e9 r\u00e1no praho! tady nov\u00fd blog! v\u010dera nebyl, tak je r\u00e1no, pac a pusu! lindeer.tumblr.com\",			 \"actions\": [			{			   \"name\": \"Comment\",			   \"link\": \"https://www.facebook.com/1231706677/posts/3756749117235\"			},			{			   \"name\": \"Like\",			   \"link\": \"https://www.facebook.com/1231706677/posts/3756749117235\"			}			 ],			 \"place\": {			\"id\": \"222129911158602\",			\"name\": \"Ubud bali\",			\"location\": {			   \"latitude\": -6.19548,			   \"longitude\": 106.82391			}			 },			 \"type\": \"status\",			 \"created_time\": \"2012-04-21T04:34:41+0000\","
			+ "\"updated_time\": \"2012-04-21T04:34:41+0000\",\"likes\": {\"data\": [{\"name\": \"Dita Pech\u00e1\u010dkov\u00e1\",			  \"id\": \"1508211827\"			   }			],			\"count\": 1			 },			 \"comments\": {			\"count\": 0			 }			  }			   ],			   \"paging\": {			  \"previous\": \"https://graph.facebook.com/me/home?access_token=AAAAAAITEghMBAHaY1v4gYLNl57j2aPHDxliRGPkW7o9ZBbFbqhZBGfke74O85k5RmkZAFkU4OzeJh9JrhyWFpEqRvAPb7y8YulZCdeNmHXgOY88leykL&limit=25&since=1335009925&__previous=1\",			  \"next\": \"https://graph.facebook.com/me/home?access_token=AAAAAAITEghMBAHaY1v4gYLNl57j2aPHDxliRGPkW7o9ZBbFbqhZBGfke74O85k5RmkZAFkU4OzeJh9JrhyWFpEqRvAPb7y8YulZCdeNmHXgOY88leykL&limit=25&until=1334982880\"}"
			+ "}";
}
