package biz.advancedcalendar.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import biz.advancedcalendar.CommonConstants;

public class ResetupRemindersService extends IntentService {
	public ResetupRemindersService() {
		super(CommonConstants.TIME_CHANGED_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		AlarmService.resetupRemindersOfTasks(this, true, true);
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		// Release the wake lock provided by the BroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
	}
}
