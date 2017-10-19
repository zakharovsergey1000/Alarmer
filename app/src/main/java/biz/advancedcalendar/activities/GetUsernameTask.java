package biz.advancedcalendar.activities;

import java.io.IOException;
import android.os.AsyncTask;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

public class GetUsernameTask extends AsyncTask<Void, Void, Void> {
	ActivityLogin mActivity;
	String mScope;
	String mEmail;

	GetUsernameTask(ActivityLogin activity, String email, String scope) {
		mActivity = activity;
		mScope = scope;
		mEmail = email;
	}

	/** Executes the asynchronous job. This runs when you call execute() on the AsyncTask
	 * instance. */
	@Override
	protected Void doInBackground(Void... params) {
		try {
			String token = fetchToken();
			if (token != null) {
				// Insert the good stuff here.
				// Use the token to access the user's Google data.
				// ...
				// mActivity.show(token);
			}
		} catch (IOException e) {
			// The fetchToken() method handles Google-specific exceptions,
			// so this indicates something went wrong at a higher level.
			// TIP: Check for network connectivity before starting the AsyncTask.
			// ...
			onError("An error is occurred, please try again. " + e.getMessage(), e);
			// } catch (JSONException e) {
			// onError("Bad response: " + e.getMessage(), e);
		}
		return null;
	}

	protected void onError(String msg, Exception e) {
		if (e != null) {
			Log.e(CommonConstants.DEBUG_TAG, "Exception: ", e);
		}
		// mActivity.show(msg); // will be run in UI thread
	}

	/** Gets an authentication token from Google and handles any GoogleAuthException that
	 * may occur. */
	protected String fetchToken() throws IOException {
		try {
			return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
		} catch (UserRecoverableAuthException userRecoverableException) {
			// GooglePlayServices.apk is either old, disabled, or not present
			// so we need to show the user some UI in the activity to recover.
			mActivity.handleException(userRecoverableException);
		} catch (GoogleAuthException fatalException) {
			// Some other type of unrecoverable exception has occurred.
			// Report and log the error as appropriate for your app.
			// ...
			onError("Unrecoverable error " + fatalException.getMessage(), fatalException);
		}
		return null;
	}
	// ...
}