package cz.cvut.skvarjak.model;

import com.facebook.android.Facebook;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SessionStore {
	protected static final String TAG = "FacebookClient";
	protected static final String TOKEN = "access_token";
	protected static final String EXPIRES = "expires_in";
	protected static final String PREFS_NAME = "facebook-session";

	public static boolean save(Facebook session, Context context) {
		Editor editor = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE).edit();
		editor.putString(TOKEN, session.getAccessToken());
		editor.putLong(EXPIRES, session.getAccessExpires());
		return editor.commit();
	}

	public static boolean restore(Facebook session, Context context) {
		SharedPreferences savedSession = context.getSharedPreferences(
				PREFS_NAME, Context.MODE_PRIVATE);
		session.setAccessToken(savedSession.getString(TOKEN, null));
		session.setAccessExpires(savedSession.getLong(EXPIRES, 0));
		return session.isSessionValid();
	}

	public static void clear(Context context) {
		Editor editor = context.getSharedPreferences(PREFS_NAME,
				Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}
}
