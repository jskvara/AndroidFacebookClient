package cz.cvut.skvarjak.model;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import android.app.Application;

public class GlobalState extends Application {
	public static final String APP_ID = "117390701725489";
	public static final String[] PERMISSIONS = {"email", "offline_access",
		"publish_stream", "read_stream"};
	private AsyncFacebookRunner mAsyncRunner;
	private Facebook mFacebook;
	private String until = "";

	public AsyncFacebookRunner getAsyncRunner() {
		if (mAsyncRunner == null) {
			mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		}
		
		return mAsyncRunner;
	}

	public void setAsyncRunner(AsyncFacebookRunner mAsyncRunner) {
		this.mAsyncRunner = mAsyncRunner;
	}

	public Facebook getFacebook() {
		if (mFacebook == null) {
			mFacebook = new Facebook(APP_ID);
		}
		
		return mFacebook;
	}

	public void setFacebook(Facebook mFacebook) {
		this.mFacebook = mFacebook;
	}
	
	public void setUntil(String until) {
		this.until = until;
	}
	
	public String getUntil() {
		return until;
	}
}
