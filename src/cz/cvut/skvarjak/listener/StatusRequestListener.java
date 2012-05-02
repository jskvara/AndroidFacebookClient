package cz.cvut.skvarjak.listener;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.widget.Toast;
import com.facebook.android.FacebookError;
import cz.cvut.skvarjak.R;
import cz.cvut.skvarjak.activity.StatusActivity;
import cz.cvut.skvarjak.model.GlobalState;

public class StatusRequestListener extends BaseRequestListener {
	protected ProgressDialog mDialog = null;
	protected Activity mActivity = null;
	
	public StatusRequestListener(Activity activity) {
		super();
		this.mActivity = activity;
	}
	
	public void getStatus(String statusId) {
		mDialog = ProgressDialog.show(mActivity, "", 
				mActivity.getString(R.string.loading));
		((GlobalState) mActivity.getApplication())
				.getAsyncRunner().request(statusId, this);
	}	

	public void onComplete(final String response, final Object state) {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}

		if (response.equals("false")) {
			mActivity.runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(mActivity.getApplicationContext(),
							mActivity.getString(R.string.status_not_exist),
							Toast.LENGTH_LONG).show();
				}
			});
			return;
		}

		Intent intent = new Intent(mActivity, StatusActivity.class);
		intent.putExtra(StatusActivity.RESPONSE, response);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		mActivity.startActivity(intent);
	}

	public void onFacebookError(FacebookError e, Object state) {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		
		mActivity.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(mActivity.getApplicationContext(),
						mActivity.getString(R.string.status_not_exist),
						Toast.LENGTH_SHORT).show();
			}
		});
		super.onFacebookError(e, state);
	}

	@Override
	public void onFileNotFoundException(FileNotFoundException e,
			Object state) {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		super.onFileNotFoundException(e, state);
	}

	@Override
	public void onIOException(IOException e, Object state) {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		super.onIOException(e, state);
	}

	@Override
	public void onMalformedURLException(MalformedURLException e,
			Object state) {
		if (mDialog != null && mDialog.isShowing()) {
			mDialog.dismiss();
		}
		super.onMalformedURLException(e, state);
	}
}