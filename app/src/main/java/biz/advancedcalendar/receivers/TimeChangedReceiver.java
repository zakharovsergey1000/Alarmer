package biz.advancedcalendar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import biz.advancedcalendar.services.ResetupRemindersService;
import biz.advancedcalendar.utils.Helper;

public class TimeChangedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		// BEGIN_INCLUDE(alarm_onreceive)
		/* If your receiver intent includes extras that need to be passed along to the
		 * service, use setComponent() to indicate that the service should handle the
		 * receiver's intent. For example: ComponentName comp = new
		 * ComponentName(context.getPackageName(), MyService.class.getName()); // This
		 * intent passed in this call will include the wake lock extra as well as // the
		 * receiver intent contents. startWakefulService(context,
		 * (intent.setComponent(comp))); In this example, we simply create a new intent to
		 * deliver to the service. This intent holds an extra identifying the wake lock. */
		//
		long currentDifference = System.currentTimeMillis()
				- SystemClock.elapsedRealtime();
		long lastDifference = Helper.getLastTimeDifference(context);
		Helper.setLastTimeDifference(context, currentDifference);
		long userChangeInMillis = currentDifference - lastDifference;
		//
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent(Intent.ACTION_TIME_CHANGED));
		//
		if (userChangeInMillis < 0) {
			final Intent serviceIntent = new Intent(context,
					ResetupRemindersService.class);
			serviceIntent.putExtras(intent);
			// Start the service, keeping the device awake while it is
			// launching.
			WakefulBroadcastReceiver.startWakefulService(context, serviceIntent);
		}
	}
}
