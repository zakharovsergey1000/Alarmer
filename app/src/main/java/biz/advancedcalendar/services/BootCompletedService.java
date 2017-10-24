package biz.advancedcalendar.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.receivers.BootCompletedReceiver;
import biz.advancedcalendar.utils.Helper;

public class BootCompletedService extends IntentService {
	public BootCompletedService() {
		super(CommonConstants.BOOT_COMPLETED_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			if (BootCompletedReceiver.DEBUG) {
				Log.d(BootCompletedReceiver.BootCompletedReceiverDB,
						"onHandleIntent(): ");
			}
			// store the new difference as current difference for the next time
			Helper.setLastTimeDifference(this,
					System.currentTimeMillis() - SystemClock.elapsedRealtime());
			// setup alarms for reminders
			// we have to setup alarms anew on every reboot
			AlarmService.setupAlarmsForScheduledReminders(this);
			// set alarm to unsilence silenced alarms
			AlarmService.setupAlarmsToUnsilenceSilencedAlarms(this);
			//
			NotificationService.updateNotification(this);
		}
		// Release the wake lock provided by the BroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
	}
}
