package cz.cvut.skvarjak.activity;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.listener.BaseDialogListener;
import cz.cvut.skvarjak.listener.BaseRequestListener;
import cz.cvut.skvarjak.model.GlobalState;
import cz.cvut.skvarjak.model.SessionStore;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

abstract public class BaseActivity extends Activity {
	protected static final String TAG = "FacebookClient.BaseActivity";
	protected Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Facebook mFacebook = ((GlobalState) getApplication()).getFacebook();
		SessionStore.restore(mFacebook, getBaseContext());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginItem = menu.findItem(R.id.login);
		MenuItem friendsItem = menu.findItem(R.id.friends);
		MenuItem newsItem = menu.findItem(R.id.newsfeed);

		Facebook mFacebook = ((GlobalState) getApplication()).getFacebook();
		if (mFacebook.isSessionValid()) {
			loginItem.setTitle(getString(R.string.logout));
			friendsItem.setEnabled(true);
			newsItem.setEnabled(true);
		} else {
			loginItem.setTitle(getString(R.string.login));
			friendsItem.setEnabled(false);
			newsItem.setEnabled(false);
		}
		loginItem.setEnabled(true);

		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// login
			case R.id.login:
				Facebook mFacebook = ((GlobalState) getApplication()).getFacebook();
				if (mFacebook.isSessionValid()) { // logout
					((GlobalState) getApplication()).getAsyncRunner()
						.logout(getBaseContext(), new LogoutRequestListener());
					SessionStore.clear(getBaseContext());
				} else { // login
					mFacebook.authorize(this, GlobalState.PERMISSIONS, new LoginDialogListener());
				}
				break;
	
			// friends
			case R.id.friends:
				startFriendsActivity();
				break;
			
			// news
			case R.id.newsfeed:
				startNewsActivity();
				break;
	
			default:
				return false;
		}

		return true;
	}
	
	public class LoginDialogListener extends BaseDialogListener {
		public void onComplete(Bundle values) {
			Facebook mFacebook = ((GlobalState) getApplication()).getFacebook();
			SessionStore.save(mFacebook, getBaseContext());
			Toast.makeText(getApplicationContext(), getString(R.string.logged_in), 
					Toast.LENGTH_LONG).show();
		}
		@Override
		public void onError(DialogError e) {
			super.onError(e);
			Toast.makeText(getApplicationContext(),
					getString(R.string.no_internet), Toast.LENGTH_LONG)
					.show();
		}
	}

	public class LogoutRequestListener extends BaseRequestListener {
		public void onComplete(String response, Object state) {
			// logout runs in another Thread
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), 
							getString(R.string.logged_out), Toast.LENGTH_LONG)
							.show();
				}
			});
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Facebook mFacebook = ((GlobalState) getApplication()).getFacebook();
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	protected void startFriendsActivity() {
		Intent intent = new Intent(this, FriendsActivity.class);
		startActivity(intent);
	}
	
	protected void startNewsActivity() {
		Intent intent2 = new Intent(this, NewsActivity.class);
		startActivity(intent2);
	}
}
