package biz.advancedcalendar.receivers;

import java.util.Calendar;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.services.AlarmService;

/** When the alarm fires, this WakefulBroadcastReceiver receives the broadcast Intent and
 * then starts the IntentService {@code SampleSchedulingService} to do some work. */
public class AlarmReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final long alarmReceivedDateTime = Calendar.getInstance().getTimeInMillis();
		// Log.d(CommonConstants.DEBUG_TAG, "AlarmReceiver's onReceive() was called");
		// BEGIN_INCLUDE(alarm_onreceive)
		/* If your receiver intent includes extras that need to be passed along to the
		 * service, use setComponent() to indicate that the service should handle the
		 * receiver's intent. For example: ComponentName comp = new
		 * ComponentName(context.getPackageName(), MyService.class.getName()); // This
		 * intent passed in this call will include the wake lock extra as well as // the
		 * receiver intent contents. startWakefulService(context,
		 * (intent.setComponent(comp))); In this example, we simply create a new intent to
		 * deliver to the service. This intent holds an extra identifying the wake lock. */
		if (intent.getAction() == null) {
			Log.e(CommonConstants.ERROR_TAG,
					"AlarmReceiver: onReceive() intent.getAction() == null");
		} else {
			final Intent serviceIntent = new Intent(context, AlarmService.class);
			serviceIntent.setAction(intent.getAction());
			serviceIntent.putExtras(intent);
			serviceIntent.putExtra(CommonConstants.AlarmReceivedDateTime,
					alarmReceivedDateTime);
			WakefulBroadcastReceiver.startWakefulService(context, serviceIntent);
		}
	}
}
