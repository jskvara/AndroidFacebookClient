package cz.cvut.skvarjak.listener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import android.util.Log;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;

public abstract class BaseRequestListener implements RequestListener {
	protected static final String TAG = "FacebookClient.BaseRequestListener";
	
	public void onFacebookError(FacebookError e, Object state) {
		Log.e(TAG, e.getMessage());
		e.printStackTrace();
	}
	
	public void onFileNotFoundException(FileNotFoundException e, Object state) {
		Log.e(TAG, e.getMessage());
		e.printStackTrace();
	};
	
	public void onIOException(IOException e, Object state) {
		Log.e(TAG, e.getMessage());
		e.printStackTrace();
	}
	
	public void onMalformedURLException(MalformedURLException e, Object state) {
		Log.e(TAG, e.getMessage());
		e.printStackTrace();
	}
}
