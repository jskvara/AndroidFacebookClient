package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import cz.cvut.skvarjak.model.FacebookDownloaderService;
import cz.cvut.skvarjak.model.GlobalState;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FacebookClientActivity extends BaseActivity {
	protected static final String TAG = "FacebookClient.FacebookClientActivity";
	protected Button newsButton;
	protected Button friendsButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		newsButton = (Button) findViewById(R.id.news);
		friendsButton = (Button) findViewById(R.id.friends);
		newsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startNewsActivity();
			}
		});
		friendsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startFriendsActivity();
			}
		});
		
		if (!isLogged()) {
			newsButton.setEnabled(false);
			friendsButton.setEnabled(false);
		}
		

		// start background service
		if (isLogged()) {
//			Cursor c = newsDataSource.getMaxTime(); // TODO
//			long newestTime = 0;
//			if (c.moveToFirst()) {
//				do {
//					newestTime = c.getLong(0);
//				} while (c.moveToNext());
//			}
//			c.close();
			
			((GlobalState) getApplication()).getAsyncRunner()
					.request("me/home", new NewsRequestListener());
		}
	}
	
	public void onResume() {    
        super.onResume();
        ((GlobalState) getApplication()).getFacebook()
        	.extendAccessTokenIfNeeded(this, null);
    }
	
	private class NewsRequestListener extends BaseRequestListener {
		public void onComplete(String response, Object state) {
			Intent intent = new Intent(getApplicationContext(),
					FacebookDownloaderService.class);
			intent.putExtra(FacebookDownloaderService.RESPONSE, response);
			Log.d(TAG, "start service");
			startService(intent);
		}
	}
	
	@Override
	protected void onLogin() {
		newsButton.setEnabled(true);
		friendsButton.setEnabled(true);
	}
	
	@Override
	protected void onLogout() {
		newsButton.setEnabled(false);
		friendsButton.setEnabled(false);
	}
}