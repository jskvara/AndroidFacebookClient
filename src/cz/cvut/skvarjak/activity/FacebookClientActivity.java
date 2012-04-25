package cz.cvut.skvarjak.activity;

import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import cz.cvut.skvarjak.model.FacebookDownloaderService;
import cz.cvut.skvarjak.model.GlobalState;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FacebookClientActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_layout);
		
		Button newsButton = (Button) findViewById(R.id.news);
		newsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startNewsActivity();
			}
		});
		
		Button friendsButton = (Button) findViewById(R.id.friends);
		friendsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startFriendsActivity();
			}
		});

		// start background service
//		GlobalState gs = (GlobalState) getApplicationContext();
//		if (gs.getFacebook().isSessionValid()) {
//			gs.getAsyncRunner().request("me/home", new NewsRequestListener());
//		}
	}
	
	private class NewsRequestListener extends BaseRequestListener {
		public void onComplete(String response, Object state) {
			Intent intent = new Intent(getApplicationContext(),
					FacebookDownloaderService.class);
			startService(intent);
		}
	}
}