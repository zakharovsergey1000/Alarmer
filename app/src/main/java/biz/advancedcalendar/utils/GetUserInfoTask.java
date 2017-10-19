package biz.advancedcalendar.utils;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.activities.ActivityLogin;
import biz.advancedcalendar.server.GetUserInfoResult;
import biz.advancedcalendar.server.ServerProvider;
import biz.advancedcalendar.sync.SyncService;

/** Represents an asynchronous authentication task used to authenticate the user. */
public class GetUserInfoTask extends AsyncTask<Object, Void, GetUserInfoResult> {
	Context mContext;
	@Override
	protected GetUserInfoResult doInBackground(Object... params) {
		mContext = (Context)params[0];
		return ServerProvider.GetUserInfo((Context)params[0], (String)params[1]);
	}

	@Override
	protected void onPostExecute(final GetUserInfoResult userInfoViewModel) {
//		mGetUserInfoTask = null;
		if (userInfoViewModel.getUserInfoViewModel() != null) {
			Intent serviceIntent = new Intent(mContext,
					SyncService.class);
			serviceIntent.putExtra(CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
					CommonConstants.SYNC_SERVICE_REQUEST_SYNC_UP_TASKS);
			// Start the service
			mContext.	startService(serviceIntent);
		} else {
			Intent intent = new Intent(mContext, ActivityLogin.class);
			intent.putExtra(
					CommonConstants.INTENT_EXTRA_SYNCHRONIZATION_AFTER_SUCCESSFUL_SIGNIN_REQUIRED,
					true);
			mContext.	startActivity(intent);
		}
	}

	@Override
	protected void onCancelled() {
//		mGetUserInfoTask = null;
	}
}