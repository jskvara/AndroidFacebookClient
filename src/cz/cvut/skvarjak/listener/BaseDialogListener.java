package cz.cvut.skvarjak.listener;

import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public abstract class BaseDialogListener implements DialogListener {
	
	protected static final String TAG = "FacebookClient";

    public void onFacebookError(FacebookError e) {
    	Log.e(TAG, e.getMessage());
        e.printStackTrace();
    }

    public void onError(DialogError e) {
    	Log.e(TAG, e.getMessage());
        e.printStackTrace();
    }

    public void onCancel() {
    }
}