package biz.advancedcalendar.services;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import biz.advancedcalendar.BooleanHolder;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.ObjectHolder;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityMain;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.ElapsedReminder;
import biz.advancedcalendar.utils.Helper;
import java.util.Collections;
import java.util.List;

/** This {@code IntentService} does the app's actual work. {@code SampleAlarmReceiver} (a
 * {@code WakefulBroadcastReceiver}) holds a partial wake lock for this service while the
 * service does its work. When the service is finished, it calls
 * {@code completeWakefulIntent()} to release the wake lock. */
public class NotificationService extends IntentService {
	LocalBroadcastManager mBroadcaster;

	public NotificationService() {
		super(CommonConstants.NOTIFICATION_SERVICE);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mBroadcaster = LocalBroadcastManager.getInstance(this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onHandleIntent(Intent intent) {
		// this will come from alarm
		if (intent.getAction().equals(
				CommonConstants.ACTION_REMINDER_NOTIFICATION_REQUEST_FROM_ALARM)) {
			Long elapsedReminderId = null;
			if (intent.hasExtra(CommonConstants.INTENT_EXTRA_ID)) {
				elapsedReminderId = intent.getLongExtra(AlarmService.ELAPSED_REMINDER_ID,
						-1);
			}
			Log.d(CommonConstants.DEBUG_TAG,
					"AlarmReceiver's onReceive() was called, id == " + elapsedReminderId);
			if (elapsedReminderId == null) {
				throw new IllegalStateException(
						"Alarm received without reminderId in intent");
			}
			final long scheduledReminderId = elapsedReminderId;
			NotificationService.issueNotification(NotificationService.this,
					scheduledReminderId);
		}
		// this will come when I will tap notification
		else if (intent.getAction().equals(
				CommonConstants.ACTION_REMINDER_NOTIFICATION_IS_TAPPED)) {
			// Creates an explicit intent for an Activity in your app
			Intent activityMainIntent = new Intent(this, ActivityMain.class);
			ComponentName activityMainComponentName = activityMainIntent.getComponent();
			Intent activityMainIntentCompat = IntentCompat
					.makeRestartActivityTask(activityMainComponentName);
			activityMainIntentCompat.putExtra(CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE,
					CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_ELAPSED);
			activityMainIntentCompat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(activityMainIntentCompat);
		}
		// this will come when I will delete notification
		else if (intent.getAction().equals(
				CommonConstants.ACTION_REMINDER_NOTIFICATION_IS_DELETED)) {
			final BooleanHolder changed = new BooleanHolder(false);
			DataProvider.runInTx(null, NotificationService.this, new Runnable() {
				@Override
				public void run() {
					List<ElapsedReminder> reminderList = DataProvider
							.getElapsedReminders(null, NotificationService.this
									.getApplicationContext());
					for (ElapsedReminder elapsedReminder : reminderList) {
						if (elapsedReminder.getShowInNotifications()) {
							elapsedReminder.setShowInNotifications(false);
							DataProvider.insertOrReplaceElapsedReminder(
									null,
									NotificationService.this.getApplicationContext(), elapsedReminder);
							changed.value = true;
						}
					}
				}
			});
			if (changed.value) {
				LocalBroadcastManager.getInstance(
						NotificationService.this.getApplicationContext()).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
				LocalBroadcastManager.getInstance(
						NotificationService.this.getApplicationContext()).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			}
		}
		// Release the wake lock provided by the BroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
	}

	public static synchronized void issueNotification(final Context context,
			final long elapsedReminderId) {
		final ObjectHolder elapsedRemindersHolder = new ObjectHolder(null);
		final ObjectHolder elapsedReminderHolder = new ObjectHolder(null);
		final ObjectHolder totalHolder = new ObjectHolder(null);
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				elapsedRemindersHolder.value = DataProvider
						.getElapsedRemindersForNotificationBar(null, context
								.getApplicationContext());
				elapsedReminderHolder.value = DataProvider.getElapsedReminder(null,
						context, elapsedReminderId);
				totalHolder.value = DataProvider.getElapsedReminders(
						null, context.getApplicationContext()).size();
			}
		});
		final ElapsedReminder elapsedReminder = (ElapsedReminder) elapsedReminderHolder.value;
		if (elapsedReminder == null || !elapsedReminder.getShowInNotifications()) {
			// do nothing
			return;
		}
		final NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		@SuppressWarnings("unchecked")
		List<ElapsedReminder> elapsedReminders = (List<ElapsedReminder>) elapsedRemindersHolder.value;
		NotificationCompat.Builder mBuilder = null;
		if (elapsedReminders.size() == 0) {
			notificationManager.cancel(CommonConstants.REMINDER_NOTIFICATION_ID);
			return;
		}
		String ringtone = elapsedReminder.getRingtone(context,
				((Global) context.getApplicationContext()).getDaoSession());
		// if (ringtone == null) {
		// ringtone = Helper.getStringPreferenceValue(context, context.getResources()
		// .getString(R.string.preference_key_notification_ringtone),
		// RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
		// .toString());
		// }
		Uri soundUri1 = Uri.parse(ringtone);
		// verify that this ringtone is playable and try to find another playable ringtone
		// if not
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(context, soundUri1);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			mediaPlayer.prepare();
		} catch (Exception e) {
			mediaPlayer.release();
			try {
				ringtone = Helper.getStringPreferenceValue(
						context,
						context.getResources().getString(
								R.string.preference_key_notification_ringtone), null);
				mediaPlayer = new MediaPlayer();
				soundUri1 = Uri.parse(ringtone);
				mediaPlayer.setDataSource(context, soundUri1);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
				mediaPlayer.prepare();
			} catch (Exception e1) {
				mediaPlayer.release();
				try {
					soundUri1 = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setDataSource(context, soundUri1);
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
					mediaPlayer.prepare();
				} catch (Exception e2) {
					mediaPlayer.release();
					try {
						soundUri1 = RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_NOTIFICATION);
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(context, soundUri1);
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
						mediaPlayer.prepare();
					} catch (Exception e3) {
						// mediaPlayer.release();
					}
				}
			}
		}
		mediaPlayer.release();
		final Uri soundUri = soundUri1;
		int total = (Integer) totalHolder.value;
		String msg = elapsedReminder.getReminder() == null ? elapsedReminder.getText()
				: elapsedReminder.getReminder().getText()
						+ (elapsedReminder.getSnoozeCount() > 0 ? " ("
								+ context.getResources().getString(R.string.snooze) + " "
								+ elapsedReminder.getSnoozeCount() + ")" : "");
		if (elapsedReminders.size() == 1) {
			mBuilder = new NotificationCompat.Builder(context)
					.setContentTitle(
							context.getString(R.string.advanced_calendar_task_notification_content_title))
					.setContentText(msg)
					// .setSubText(msg)
					//
					// .setNumber(total)
					//
					.setSmallIcon(R.drawable.ic_notification)
					//
					.setTicker(msg)
					// .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
					.setAutoCancel(true)
					//
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					//
					.setContentInfo("<" + total + ">")
					.setCategory(NotificationCompat.CATEGORY_EVENT)
			//
			;
		} else {
			Collections
					.sort(elapsedReminders,
							Collections
									.reverseOrder(new AlarmService.ElapsedReminderComparatorByActualAlarmedDateTime()));
			String bigTextMsg = "";
			for (int i = 0; i < 5 && i < elapsedReminders.size(); i++) {
				ElapsedReminder elapsedReminder1 = elapsedReminders.get(i);
				bigTextMsg += (elapsedReminder1.getReminder() == null ? elapsedReminder1
						.getText() : elapsedReminder1.getReminder().getText())
						+ (elapsedReminder1.getSnoozeCount() > 0 ? " ("
								+ context.getResources().getString(R.string.snooze) + " "
								+ elapsedReminder1.getSnoozeCount() + ")" : "") + "\n";
			}
			if (elapsedReminders.size() > 5) {
				bigTextMsg += "+ " + (elapsedReminders.size() - 5) + " more ...";
			} else {
				bigTextMsg = bigTextMsg.substring(0, bigTextMsg.length() - 1);
			}
			// for (ScheduledReminder scheduledReminder :
			// scheduledReminderList) {
			// bigTextMsg += scheduledReminder.getReminder() == null ?
			// scheduledReminder
			// .getRemindText() : scheduledReminder.getReminder()
			// .getRemindText() + "\n";
			// }
			// bigTextMsg = bigTextMsg.substring(0, bigTextMsg.length() - 1);
			// Creates an explicit intent for an Activity in your app
			String contentTitle = elapsedReminders.size()
					+ " "
					+ context
							.getString(R.string.advanced_calendar_task_notification_content_title_plural);
			// String contentText = elapsedReminders.get(0).getReminder() == null ?
			// elapsedReminders
			// .get(0).getText() : elapsedReminders.get(0).getReminder().getText();
			mBuilder = new NotificationCompat.Builder(context)
					.setContentTitle(contentTitle)
					//
					.setContentText(msg)
					//
					.setSubText(
							context.getString(R.string.advanced_calendar_task_notification_subtext_total))
					//
					// .setNumber(total)
					//
					.setSmallIcon(R.drawable.ic_notification)
					//
					.setTicker(msg + "\n" + contentTitle)
					//
					.setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextMsg))
					//
					.setAutoCancel(true)
					//
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					//
					.setContentInfo("<" + total + ">")
					.setCategory(NotificationCompat.CATEGORY_EVENT)
			//
			;
		}
		mBuilder.setSound(soundUri);
		if (Helper.getBooleanPreferenceValue(
				context,
				R.string.preference_key_vibrate_with_alarm_ringtone,
				context.getResources().getBoolean(
						R.bool.vibrate_with_alarm_ringtone_default_value))) {
			// long[] pattern = {500, 500, 500, 500, 500};
			long[] pattern = {0, 166, 166, 166, 166, 166};
			mBuilder.setVibrate(pattern);
		}
		Intent notificationServiceIntent = new Intent(context, NotificationService.class);
		notificationServiceIntent
				.setAction(CommonConstants.ACTION_REMINDER_NOTIFICATION_IS_TAPPED);
		long[] reminderIdArray = new long[elapsedReminders.size()];
		for (int i = 0; i < elapsedReminders.size(); i++) {
			reminderIdArray[i] = elapsedReminders.get(i).getId();
		}
		notificationServiceIntent.putExtra(CommonConstants.REMINDER_ID_ARRAY,
				reminderIdArray);
		PendingIntent notificationServicePendingIntent = PendingIntent.getService(
				context, 0, notificationServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(notificationServicePendingIntent);
		Notification notification = mBuilder.build();
		mBuilder.getNotification();
		// for (int i = 1; i < total; i++) {
		// notification.number = i;
		// notificationManager.notify(CommonConstants.REMINDER_NOTIFICATION_ID,
		// notification);
		// }
		notificationManager.cancel(CommonConstants.REMINDER_NOTIFICATION_ID);
		notificationManager
				.notify(CommonConstants.REMINDER_NOTIFICATION_ID, notification);
	}

	public static synchronized void updateNotification(final Context context) {
		final boolean makeSound = false;
		final ObjectHolder elapsedRemindersHolder = new ObjectHolder(null);
		final ObjectHolder totalHolder = new ObjectHolder(null);
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				elapsedRemindersHolder.value = DataProvider
						.getElapsedRemindersForNotificationBar(null, context
								.getApplicationContext());
				totalHolder.value = DataProvider.getElapsedReminders(
						null, context.getApplicationContext()).size();
			}
		});
		final NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		@SuppressWarnings("unchecked")
		List<ElapsedReminder> elapsedReminders = (List<ElapsedReminder>) elapsedRemindersHolder.value;
		if (elapsedReminders.size() == 0) {
			notificationManager.cancel(CommonConstants.REMINDER_NOTIFICATION_ID);
			return;
		}
		String ringtone = Helper.getStringPreferenceValue(context, context.getResources()
				.getString(R.string.preference_key_notification_ringtone),
				RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
						.toString());
		Uri soundUri1 = Uri.parse(ringtone);
		// verify that this ringtone is playable and try to find another playable ringtone
		// if not
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(context, soundUri1);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
			mediaPlayer.prepare();
		} catch (Exception e) {
			mediaPlayer.release();
			try {
				ringtone = Helper.getStringPreferenceValue(
						context,
						context.getResources().getString(
								R.string.preference_key_notification_ringtone), null);
				mediaPlayer = new MediaPlayer();
				soundUri1 = Uri.parse(ringtone);
				mediaPlayer.setDataSource(context, soundUri1);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
				mediaPlayer.prepare();
			} catch (Exception e1) {
				mediaPlayer.release();
				try {
					soundUri1 = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setDataSource(context, soundUri1);
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
					mediaPlayer.prepare();
				} catch (Exception e2) {
					mediaPlayer.release();
					try {
						soundUri1 = RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_NOTIFICATION);
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(context, soundUri1);
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
						mediaPlayer.prepare();
					} catch (Exception e3) {
						// mediaPlayer.release();
					}
				}
			}
		}
		mediaPlayer.release();
		final Uri soundUri = soundUri1;
		int total = (Integer) totalHolder.value;
		NotificationCompat.Builder mBuilder = null;
		if (elapsedReminders.size() == 1) {
			String msg = elapsedReminders.get(0).getReminder() == null ? elapsedReminders
					.get(0).getText() : elapsedReminders.get(0).getReminder().getText()
					+ (elapsedReminders.get(0).getSnoozeCount() > 0 ? " ("
							+ context.getResources().getString(R.string.snooze) + " "
							+ elapsedReminders.get(0).getSnoozeCount() + ")" : "");
			mBuilder = new NotificationCompat.Builder(context)
					.setContentTitle(
							context.getString(R.string.advanced_calendar_task_notification_content_title))
					.setContentText(msg)
					// .setSubText(msg)
					//
					// .setNumber(total)
					.setSmallIcon(R.drawable.ic_notification)
					// .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
					.setAutoCancel(true)
					//
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					//
					.setContentInfo("<" + total + ">")
					.setCategory(NotificationCompat.CATEGORY_EVENT)
			//
			;
			if (makeSound) {
				mBuilder.setTicker(msg);
			}
		} else {
			// Collections.sort(elapsedReminders,
			// new Alarm.ElapsedReminderComparatorByActualAlarmedDateTime());
			Collections
					.sort(elapsedReminders,
							Collections
									.reverseOrder(new AlarmService.ElapsedReminderComparatorByActualAlarmedDateTime()));
			String bigTextMsg = "";
			for (int i = 0; i < 5 && i < elapsedReminders.size(); i++) {
				ElapsedReminder elapsedReminder1 = elapsedReminders.get(i);
				bigTextMsg += (elapsedReminder1.getReminder() == null ? elapsedReminder1
						.getText() : elapsedReminder1.getReminder().getText())
						+ (elapsedReminder1.getSnoozeCount() > 0 ? " ("
								+ context.getResources().getString(R.string.snooze) + " "
								+ elapsedReminder1.getSnoozeCount() + ")" : "") + "\n";
			}
			if (elapsedReminders.size() > 5) {
				bigTextMsg += "+ " + (elapsedReminders.size() - 5) + " more ...";
			} else {
				bigTextMsg = bigTextMsg.substring(0, bigTextMsg.length() - 1);
			}
			// for (ScheduledReminder scheduledReminder :
			// scheduledReminderList) {
			// bigTextMsg += scheduledReminder.getReminder() == null ?
			// scheduledReminder
			// .getRemindText() : scheduledReminder.getReminder()
			// .getRemindText() + "\n";
			// }
			// bigTextMsg = bigTextMsg.substring(0, bigTextMsg.length() - 1);
			// Creates an explicit intent for an Activity in your app
			String contentTitle = elapsedReminders.size()
					+ " "
					+ context
							.getString(R.string.advanced_calendar_task_notification_content_title_plural);
			String contentText = elapsedReminders.get(0).getReminder() == null ? elapsedReminders
					.get(0).getText() : elapsedReminders.get(0).getReminder().getText();
			mBuilder = new NotificationCompat.Builder(context)
					.setContentTitle(contentTitle)
					//
					.setContentText(contentText)
					//
					.setSubText(
							context.getString(R.string.advanced_calendar_task_notification_subtext_total))
					// .setNumber(total)
					//
					.setSmallIcon(R.drawable.ic_notification)
					.setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextMsg))
					.setAutoCancel(true)
					//
					.setPriority(NotificationCompat.PRIORITY_HIGH)
					.setContentInfo("<" + total + ">")
					.setCategory(NotificationCompat.CATEGORY_EVENT)
			//
			;
			if (makeSound) {
				mBuilder.setTicker(contentTitle);
			}
		}
		if (makeSound) {
			mBuilder.setSound(soundUri);
		}
		// if (makeSound
		// && Helper
		// .getBooleanPreferenceValue(
		// context,
		// R.string.preference_key_vibrate_with_alarm_ringtone,
		// context.getResources()
		// .getBoolean(
		// R.bool.vibrate_with_alarm_ringtone_default_value))) {
		// // long[] pattern = {500, 500, 500, 500, 500};
		// long[] pattern = {0, 166, 166, 166, 166, 166};
		// mBuilder.setVibrate(pattern);
		// }
		//
		Intent contentIntent = new Intent(context, NotificationService.class);
		contentIntent.setAction(CommonConstants.ACTION_REMINDER_NOTIFICATION_IS_TAPPED);
		long[] reminderIdArray = new long[elapsedReminders.size()];
		for (int i = 0; i < elapsedReminders.size(); i++) {
			reminderIdArray[i] = elapsedReminders.get(i).getId();
		}
		contentIntent.putExtra(CommonConstants.REMINDER_ID_ARRAY, reminderIdArray);
		PendingIntent contentPendingIntent = PendingIntent.getService(context, 0,
				contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(contentPendingIntent);
		//
		final Intent deleteIntent = new Intent(context, NotificationService.class);
		deleteIntent.setAction(CommonConstants.ACTION_REMINDER_NOTIFICATION_IS_DELETED);
		PendingIntent deletePendingIntent = PendingIntent.getService(context, 0,
				deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setDeleteIntent(deletePendingIntent);
		//
		Notification notification = mBuilder.build();
		// for (int i = 1; i < total; i++) {
		// notification.number = i;
		// notificationManager.notify(CommonConstants.REMINDER_NOTIFICATION_ID,
		// notification);
		// }
		// notificationManager.cancel(CommonConstants.REMINDER_NOTIFICATION_ID);
		notificationManager
				.notify(CommonConstants.REMINDER_NOTIFICATION_ID, notification);
	}
}
