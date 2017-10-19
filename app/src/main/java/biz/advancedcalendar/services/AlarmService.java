package biz.advancedcalendar.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import biz.advancedcalendar.BooleanHolder;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityAlarm;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.ElapsedReminder;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.receivers.AlarmReceiver;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class AlarmService extends IntentService {
	protected static final String ELAPSED_REMINDER_ID = "ELAPSED_REMINDER_ID";

	public AlarmService() {
		super(CommonConstants.ALARM_SERVICE);
	}

	@Override
	public void onHandleIntent(final Intent intent) {
		final long alarmReceivedDateTime = intent.getLongExtra(
				CommonConstants.AlarmReceivedDateTime, Calendar.getInstance()
						.getTimeInMillis());
		String action = intent.getAction();
		if (action.equals(CommonConstants.ALARM_REMINDER)) {
			Long scheduledReminderId2 = null;
			if (intent.hasExtra(CommonConstants.INTENT_EXTRA_ID)) {
				scheduledReminderId2 = intent.getLongExtra(
						CommonConstants.INTENT_EXTRA_ID, -1);
			}
			Log.d(CommonConstants.DEBUG_TAG,
					"AlarmReceiver's onReceive() was called, id == "
							+ scheduledReminderId2);
			if (scheduledReminderId2 == null) {
				throw new IllegalStateException(
						"Alarm received without reminderId in intent");
			}
			final long scheduledReminderId = scheduledReminderId2;
			final Intent notificationServiceIntent = new Intent(this,
					NotificationService.class);
			notificationServiceIntent
					.setAction(CommonConstants.ACTION_REMINDER_NOTIFICATION_REQUEST_FROM_ALARM);
			notificationServiceIntent.putExtras(intent);
			final Intent activityAlarmIntent;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				activityAlarmIntent = new Intent(this, ActivityAlarm.class);
			} else {
				activityAlarmIntent = new Intent(this, ActivityAlarm.class);
			}
			activityAlarmIntent.putExtras(intent);
			activityAlarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			final BooleanHolder changed = new BooleanHolder(false);
			DataProvider.runInTx(null, this, new Runnable() {
				@Override
				public void run() {
					ScheduledReminder scheduledReminder = DataProvider
							.getScheduledReminder(null,
									AlarmService.this.getApplicationContext(),
									scheduledReminderId);
					if (scheduledReminder == null) {
						// do nothing
						return;
					} else {
						ElapsedReminder newElapsedReminder = new ElapsedReminder(null,
								scheduledReminder.getReminderId(), scheduledReminder
										.getId(), scheduledReminder
										.getAssignedRemindAtDateTime(),
								alarmReceivedDateTime,
								scheduledReminder.getSnoozeCount(), scheduledReminder
										.getText(), scheduledReminder.getIsAlarm(), true,
								scheduledReminder.getEnabled(), scheduledReminder
										.getRingtone(), scheduledReminder
										.getRingtoneFadeInTime(), scheduledReminder
										.getPlayingTime(), scheduledReminder
										.getAutomaticSnoozeDuration(), scheduledReminder
										.getAutomaticSnoozesMaxCount(), scheduledReminder
										.getVibrate(), scheduledReminder
										.getVibratePattern(), scheduledReminder.getLed(),
								scheduledReminder.getLedPattern(), scheduledReminder
										.getLedColor());
						ElapsedReminder previousElapsedReminder = null;
						// if (scheduledReminder.getSnoozeCount() > 0) {
						previousElapsedReminder = scheduledReminder
								.getElapsedReminder2(AlarmService.this);
						// }
						if (previousElapsedReminder != null) {
							newElapsedReminder.setId(previousElapsedReminder.getId());
						}
						DataProvider.insertOrReplaceElapsedReminder(null,
								AlarmService.this, newElapsedReminder);
						changed.value = true;
						Reminder reminder;
						Task task;
						Long nextReminderDateTime;
						if (scheduledReminder.getSnoozeCount() == 0
						//
								&& (reminder = scheduledReminder.getReminder()) != null
								//
								&& reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
										.getValue()
								//
								&& (task = reminder.getTask()) != null
								//
								&& (!task.getIsCompleted() || CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
										.getIntegerPreferenceValueFromStringArray(
												AlarmService.this,
												R.string.preference_key_reminder_behavior_for_completed_task,
												R.array.reminder_behavior_for_completed_task_values_array,
												R.integer.reminder_default_behavior_for_completed_task))
								//
								&& (nextReminderDateTime = reminder.getNextReminderDateTime(
										AlarmService.this,
										scheduledReminder.getAssignedRemindAtDateTime(),
										false,
										Helper.getFirstDayOfWeek(AlarmService.this))) != null) {
							ScheduledReminder nextScheduledReminder = new ScheduledReminder(
									null, reminder.getId(), null, false,
									nextReminderDateTime, nextReminderDateTime, null, 0,
									ScheduledReminder.State.SCHEDULED.getValue(),
									reminder.getText(), reminder.getIsAlarm(), reminder
											.getEnabled(), scheduledReminder
											.getRingtone(), reminder
											.getRingtoneFadeInTime(), reminder
											.getPlayingTime(), reminder
											.getAutomaticSnoozeDuration(), reminder
											.getAutomaticSnoozesMaxCount(), reminder
											.getVibrate(), reminder.getVibratePattern(),
									reminder.getLed(), reminder.getLedPattern(), reminder
											.getLedColor());
							DataProvider.insertOrReplaceScheduledReminder(null,
									AlarmService.this, nextScheduledReminder);
							AlarmService.setAlarmForReminder(AlarmService.this,
									nextScheduledReminder.getId(),
									nextScheduledReminder.getNextSnoozeDateTime(), this);
						}
						if (scheduledReminder.getEnabled()) {
							reminder = scheduledReminder.getReminder();
							if (reminder == null ? scheduledReminder.getIsAlarm()
									: reminder.getIsAlarm()) {
								scheduledReminder.setElapsedReminder(newElapsedReminder);
								scheduledReminder
										.setActualLastAlarmedDateTime(alarmReceivedDateTime);
								scheduledReminder
										.setStateValue(ScheduledReminder.State.ALARMED
												.getValue());
								DataProvider.insertOrReplaceScheduledReminder(null,
										AlarmService.this, scheduledReminder);
								// only start activityAlarmIntent if we are currently not
								// in silence timespan
								if (alarmReceivedDateTime >= PreferenceManager
										.getDefaultSharedPreferences(AlarmService.this)
										.getLong(
												AlarmService.this
														.getResources()
														.getString(
																R.string.preference_key_silence_alarms_until_datetime),
												alarmReceivedDateTime)) {
									AlarmService.this.startActivity(activityAlarmIntent);
								}
							} else {
								DataProvider.deleteScheduledReminder(null,
										AlarmService.this, scheduledReminder.getId());
								notificationServiceIntent.putExtra(
										AlarmService.ELAPSED_REMINDER_ID,
										newElapsedReminder.getId());
								// Start the service, keeping the device awake while it is
								// launching.
								WakefulBroadcastReceiver.startWakefulService(
										AlarmService.this, notificationServiceIntent);
							}
						} else {
							DataProvider.deleteScheduledReminder(null, AlarmService.this,
									scheduledReminder.getId());
						}
					}
				}
			});
			if (changed.value) {
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			}
		}
		//
		else if (action.equals(CommonConstants.ALARM_UNSILENSE_REMINDERS)) {
			this.startActivity(new Intent(this, ActivityAlarm.class)
					.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent(CommonConstants.ACTION_REMINDERS_UNSILENSED));
		}
		//
		else if (action.equals(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED)) {
			AlarmService.resetupRemindersOfWeeklyRecurrentTasks(this);
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		}
		// Release the wake lock provided by the BroadcastReceiver.
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
	}

	public static void setAlarmForReminder(final Context context, final long reminderId,
			final long dateTime, final Runnable r) {
		// check we are inside DataProvider.runInTx
		if (r == null) {
			throw new IllegalAccessError();
		}
		// prepare intent
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(CommonConstants.ALARM_REMINDER);
		Uri reminder_uri = Uri
				.parse(CommonConstants.CONTENT_BIZ_ADVANCEDCALENDAR_ALARM_REMINDER
						+ reminderId);
		intent.setData(reminder_uri);
		intent.putExtra(CommonConstants.INTENT_EXTRA_ID, reminderId);
		// Set the alarm
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, dateTime, PendingIntent.getBroadcast(context, 0,
						intent, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static synchronized void resetupRemindersOfTasks(final Context context,
			final boolean resetupActive, final boolean resetupCompleted) {
		final ArrayList<ArrayList<TreeViewListItemDescription>> forest1 = new ArrayList<ArrayList<TreeViewListItemDescription>>();
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				ArrayList<ArrayList<TreeViewListItemDescription>> forest = DataProvider
						.getTreeViewListItemDescriptionForest(null, context, null, null,
								true, false, resetupActive, resetupCompleted);
				for (ArrayList<TreeViewListItemDescription> tree : forest) {
					forest1.add(tree);
				}
			}
		});
		boolean scheduleRemindersOfCompletedTasks = CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_reminder_behavior_for_completed_task,
						R.array.reminder_behavior_for_completed_task_values_array,
						R.integer.reminder_default_behavior_for_completed_task);
		for (List<TreeViewListItemDescription> tree : forest1) {
			for (TreeViewListItemDescription treeViewListItemDescriptionImpl : tree) {
				AlarmService.resetupRemindersOfTask(context,
						treeViewListItemDescriptionImpl.getId(),
						scheduleRemindersOfCompletedTasks, Calendar.getInstance()
								.getTimeInMillis());
			}
		}
	}

	public static void resetupRemindersOfTask(final Context context, final long taskId,
			final boolean scheduleRemindersIfTaskIsCompleted, final long controlDateTime) {
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				Task task = DataProvider.getTask(null, context, taskId, false);
				if (task == null) {
					return;
				}
				List<Reminder> reminders = DataProvider.getReminderListOfTask(null,
						context, task.getId(), true, true);
				for (Reminder reminder : reminders) {
					List<ScheduledReminder> scheduledReminders = DataProvider
							.getScheduledReminders(null, context, reminder.getId(),
									new int[] {ScheduledReminder.State.SCHEDULED
											.getValue()});
					for (int i = 0; i < scheduledReminders.size(); i++) {
						ScheduledReminder scheduledReminder = scheduledReminders.get(i);
						AlarmService.cancelScheduledAlarm(context,
								scheduledReminder.getId());
						DataProvider.deleteScheduledReminder(null, context,
								scheduledReminder.getId());
					}
					if (!task.getIsCompleted() || scheduleRemindersIfTaskIsCompleted) {
						Long nextReminderDateTime = reminder.getNextReminderDateTime(
								context, controlDateTime, true,
								Helper.getFirstDayOfWeek(context));
						if (nextReminderDateTime != null) {
							ScheduledReminder newScheduledReminder = new ScheduledReminder(
							//
							// Long id,
									null,
									// Long reminderId,
									reminder.getId(),
									// Long elapsedReminderId,
									null,
									// boolean isQuickReminder,
									false,
									// long assignedRemindAtDateTime,
									nextReminderDateTime,
									// long nextSnoozeDateTime,
									nextReminderDateTime,
									// Long actualLastAlarmedDateTime,
									null,
									// int snoozeCount,
									0,
									// int stateValue,
									ScheduledReminder.State.SCHEDULED.getValue(),
									// String text,
									reminder.getText(),
									// boolean isAlarm,
									reminder.getIsAlarm(),
									// boolean enabled,
									reminder.getEnabled(),
									// String ringtone,
									reminder.getRingtone(),
									// Long ringtoneFadeInTime,
									reminder.getRingtoneFadeInTime(),
									// Integer playingTime,
									reminder.getPlayingTime(),
									// Integer automaticSnoozeDuration,
									reminder.getAutomaticSnoozeDuration(),
									// Integer automaticSnoozesMaxCount,
									reminder.getAutomaticSnoozesMaxCount(),
									// Boolean vibrate,
									reminder.getVibrate(),
									// String vibratePattern,
									reminder.getVibratePattern(),
									// Boolean led,
									reminder.getLed(),
									// String ledPattern,
									reminder.getLedPattern(), // Integer ledColor
									reminder.getLedColor());
							DataProvider.insertOrReplaceScheduledReminder(null, context,
									newScheduledReminder);
							AlarmService.setAlarmForReminder(context,
									newScheduledReminder.getId(),
									newScheduledReminder.getNextSnoozeDateTime(), this);
						}
					}
				}
			}
		});
	}

	// public static void cancelScheduledRemindersOfTask(final Context context,
	// final long taskId, final boolean cancelSnoozed) {
	// DataProvider.runInTx(context, new Runnable() {
	// @Override
	// public void run() {
	// Task task = DataProvider.getTask(context, taskId, false);
	// if (task == null)
	// return;
	// List<Reminder> reminders = DataProvider.getReminderListOfTask(context,
	// task.getId(), true, true);
	// for (Reminder reminder : reminders) {
	// int[] states = new int[] {ScheduledReminder.State.SCHEDULED
	// .getValue()};
	// List<ScheduledReminder> scheduledReminders = DataProvider
	// .getScheduledReminders(context, reminder.getId(), states);
	// for (int i = 0; i < scheduledReminders.size(); i++) {
	// ScheduledReminder scheduledReminder = scheduledReminders.get(i);
	// if (!cancelSnoozed && scheduledReminder.getSnoozeCount() > 0) {
	// continue;
	// }
	// Alarm.cancelScheduledAlarm(context, scheduledReminder.getId());
	// DataProvider.deleteScheduledReminder(context,
	// scheduledReminder.getId());
	// }
	// }
	// }
	// });
	// }
	// public static void resetupAlarmsOfTask(final Context context, final long taskId) {
	// DataProvider.runInTx(context, new Runnable() {
	// @Override
	// public void run() {
	// Task task = DataProvider.getTask(context, taskId, false);
	// if (task != null) {
	// List<Reminder> reminders = DataProvider.getReminderListOfTask(
	// context, taskId);
	// if (reminders.size() > 0) {
	// boolean scheduleRemindersOfCompletedTasks =
	// CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
	// .getIntegerPreferenceValueFromStringArray(
	// context,
	// R.string.preference_key_reminder_behavior_for_completed_task,
	// R.array.reminder_behavior_for_completed_task_values_array,
	// R.integer.reminder_default_behavior_for_completed_task);
	// if (scheduleRemindersOfCompletedTasks || !task.getIsCompleted()) {
	// for (Reminder reminder : reminders) {
	// List<ScheduledReminder> scheduledReminders = reminder
	// .getScheduledReminderList(
	// context,
	// new int[] {ScheduledReminder.State.SCHEDULED
	// .getValue()});
	// for (ScheduledReminder scheduledReminder : scheduledReminders) {
	// Alarm.cancelScheduledAlarm(context,
	// scheduledReminder.getId());
	// DataProvider.deleteScheduledReminder(context,
	// scheduledReminder.getId());
	// }
	// Long nextReminderDateTime = reminder
	// .getNextReminderDateTime(context, Calendar
	// .getInstance().getTimeInMillis(), true,
	// Helper.getFirstDayOfWeek(context));
	// if (nextReminderDateTime != null) {
	// ScheduledReminder scheduledReminder = new ScheduledReminder(
	// null, reminder.getId(), null, false,
	// nextReminderDateTime, nextReminderDateTime,
	// null, 0, ScheduledReminder.State.SCHEDULED
	// .getValue(), reminder.getText(),
	// reminder.getIsAlarm(), reminder.getEnabled(),
	// reminder.getRingtone(), reminder
	// .getAutomaticSnoozeDuration(), reminder
	// .getAutomaticSnoozesMaxCount(),
	// reminder.getPlayingTime(), reminder
	// .getVibrate(), reminder
	// .getVibratePattern(), reminder
	// .getLed(), reminder.getLedPattern(),
	// reminder.getLedColor());
	// DataProvider.insertOrReplaceScheduledReminder(
	// context, scheduledReminder);
	// Alarm.setAlarm(context, scheduledReminder.getId(),
	// scheduledReminder.getNextSnoozeDateTime(),
	// this);
	// }
	// }
	// }
	// }
	// }
	// }
	// });
	// }
	public static synchronized void resetupRemindersOfWeeklyRecurrentTasks(
			final Context context) {
		List<Task> nonDeletedWeeklyRecurrentTasks = DataProvider
				.getNonDeletedWeeklyRecurrentTasks(null, context);
		boolean scheduleRemindersOfCompletedTasks = CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_reminder_behavior_for_completed_task,
						R.array.reminder_behavior_for_completed_task_values_array,
						R.integer.reminder_default_behavior_for_completed_task);
		for (Task task : nonDeletedWeeklyRecurrentTasks) {
			AlarmService.resetupRemindersOfTask(context, task.getId(),
					scheduleRemindersOfCompletedTasks, Calendar.getInstance()
							.getTimeInMillis());
		}
	}

	public static void cancelScheduledAlarm(Context context, long scheduledReminderId) {
		// prepare PendingIntent
		AlarmManager alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		Uri reminder_uri = Uri
				.parse(CommonConstants.CONTENT_BIZ_ADVANCEDCALENDAR_ALARM_REMINDER
						+ scheduledReminderId);
		intent.setData(reminder_uri);
		intent.setAction(CommonConstants.ALARM_REMINDER);
		intent.putExtra(CommonConstants.INTENT_EXTRA_ID, scheduledReminderId);
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		// cancel the alarm
		alarmMgr.cancel(alarmIntent);
	}

	public static class ScheduledReminderComparatorByNextSnoozeDateTime implements
			Comparator<ScheduledReminder> {
		@Override
		public int compare(ScheduledReminder a, ScheduledReminder b) {
			if (a.getNextSnoozeDateTime() > b.getNextSnoozeDateTime()) {
				return 1;
			} else if (a.getNextSnoozeDateTime() < b.getNextSnoozeDateTime()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	public static class ElapsedReminderComparatorByActualAlarmedDateTime implements
			Comparator<ElapsedReminder> {
		@Override
		public int compare(ElapsedReminder a, ElapsedReminder b) {
			if (a.getActualLastAlarmedDateTime() > b.getActualLastAlarmedDateTime()) {
				return 1;
			} else if (a.getActualLastAlarmedDateTime() < b
					.getActualLastAlarmedDateTime()) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	public static void silenseAlarms(Context context, long dateTime, Runnable r) {
		if (r == null) {
			throw new IllegalAccessError();
		}
		PreferenceManager
				.getDefaultSharedPreferences(context)
				.edit()
				.putLong(
						context.getResources().getString(
								R.string.preference_key_silence_alarms_until_datetime),
						dateTime).commit();
		Intent intent = new Intent(context, AlarmReceiver.class);
		// Uri reminder_uri = Uri
		// .parse(CommonConstants.CONTENT_BIZ_ADVANCEDCALENDAR_ALARM_UNSILENSE_REMINDERS);
		// intent.setData(reminder_uri);
		intent.setAction(CommonConstants.ALARM_UNSILENSE_REMINDERS);
		// Set the alarm
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(
				AlarmManager.RTC_WAKEUP, dateTime, PendingIntent.getBroadcast(context, 0,
						intent, PendingIntent.FLAG_UPDATE_CURRENT));
	}

	public static void unsilenseAlarms(Context context, Runnable r) {
		if (r == null) {
			throw new IllegalAccessError();
		}
		PreferenceManager
				.getDefaultSharedPreferences(context)
				.edit()
				.remove(context.getResources().getString(
						R.string.preference_key_silence_alarms_until_datetime)).commit();
		context.startActivity(new Intent(context, ActivityAlarm.class)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}
}
