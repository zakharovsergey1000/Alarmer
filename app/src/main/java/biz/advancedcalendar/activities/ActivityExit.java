package biz.advancedcalendar.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class ActivityExit extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("ActivityExit: onCreate()"));
		}
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			// finishAndRemoveTask();
			finish();
		} else {
			finish();
		}
	}

	public static void exitApplicationAndRemoveFromRecent(Context mContext) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("exitApplicationAndRemoveFromRecent()"));
		}
		Intent intent = new Intent(mContext, ActivityExit.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| Intent.FLAG_ACTIVITY_NO_ANIMATION);
		mContext.startActivity(intent);
	}
}
