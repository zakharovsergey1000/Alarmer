package biz.advancedcalendar.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import biz.advancedcalendar.services.BootCompletedService;

public class BootCompletedReceiver extends BroadcastReceiver {
	public static final boolean DEBUG = false;
	public static final String BootCompletedReceiverDebug = "BootCompletedReceiverDebug";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (!(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
				|| intent.getAction().equals("android.intent.action.QUICKBOOT_POWERON") || intent
				.getAction().equals("com.htc.intent.action.QUICKBOOT_POWERON"))) {
			throw new IllegalArgumentException(
					"An intent received with !(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && intent.getAction().equals(\"android.intent.action.QUICKBOOT_POWERON\")) in BootCompletedReceiver");
		}
		//
		if (BootCompletedReceiver.DEBUG) {
			Log.d(BootCompletedReceiver.BootCompletedReceiverDebug, "onReceive(): ");
			// MediaPlayer mp = MediaPlayer.create(context, R.raw.greeting);
			// mp.start();
		}
		final Intent serviceIntent = new Intent(context, BootCompletedService.class);
		serviceIntent.setAction(Intent.ACTION_BOOT_COMPLETED);
		serviceIntent.putExtras(intent);
		// Start the service, keeping the device awake while it is
		// launching.
		WakefulBroadcastReceiver.startWakefulService(context, serviceIntent);
	}
}
