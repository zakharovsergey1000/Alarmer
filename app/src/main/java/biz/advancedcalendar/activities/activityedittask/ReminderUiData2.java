package biz.advancedcalendar.activities.activityedittask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.utils.Helper;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ReminderUiData2 implements Parcelable {
	private Long id;
	private TaskUiData2 taskUiData;
	private long reminderDateTime;
	private byte reminderTimeModeValue;
	private String text;
	private boolean enabled;
	private boolean isAlarm;
	private String ringtone;
	private Long ringtoneFadeInTime;
	private Integer playingTime;
	private Integer automaticSnoozeDuration;
	private Integer automaticSnoozesMaxCount;
	private Boolean vibrate;
	private String vibratePattern;
	private Boolean led;
	private String ledPattern;
	private Integer ledColor;

	private class LongHolder {
		Long value = null;
	}

	public enum AlarmMethod {
		POPUP_WINDOW((byte) 1), STATUS_BAR((byte) 2);
		private byte value;

		private AlarmMethod(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, AlarmMethod> map = new HashMap<Byte, AlarmMethod>();
		private static Map<String, AlarmMethod> stringToObjectMap = new HashMap<String, AlarmMethod>();
		static {
			for (AlarmMethod alarmMethod : AlarmMethod.values()) {
				AlarmMethod.map.put(alarmMethod.value, alarmMethod);
				AlarmMethod.stringToObjectMap.put(alarmMethod.name(), alarmMethod);
			}
		}

		public static AlarmMethod fromInt(byte value) {
			AlarmMethod alarmMethod = AlarmMethod.map.get(value);
			return alarmMethod;
		}

		public static AlarmMethod fromString(String name) {
			AlarmMethod alarmMethod = AlarmMethod.stringToObjectMap.get(name);
			return alarmMethod;
		}
	}

	@SuppressLint("UseSparseArrays")
	public enum ReminderTimeMode {
		ABSOLUTE_TIME((byte) 1, R.id.activity_edit_reminder_radio_reminder_time_absolute), AFTER_NOW(
				(byte) 2, R.id.activity_edit_reminder_radio_reminder_time_after_now), TIME_BEFORE_EVENT(
				(byte) 3, R.id.activity_edit_reminder_radio_reminder_time_before_event), TIME_AFTER_EVENT(
				(byte) 4, R.id.activity_edit_reminder_radio_reminder_time_after_event);
		private byte value;
		private int radioButtonId;

		private ReminderTimeMode(byte value, int radioButtonId) {
			this.value = value;
			this.radioButtonId = radioButtonId;
		}

		public byte getValue() {
			return value;
		}

		public int getRadioButtonId() {
			return radioButtonId;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, ReminderTimeMode> map = new HashMap<Byte, ReminderTimeMode>();
		private static Map<String, ReminderTimeMode> stringToObjectMap = new HashMap<String, ReminderTimeMode>();
		static {
			for (ReminderTimeMode reminderTimeMode : ReminderTimeMode.values()) {
				ReminderTimeMode.map.put(reminderTimeMode.value, reminderTimeMode);
				ReminderTimeMode.stringToObjectMap.put(reminderTimeMode.name(),
						reminderTimeMode);
			}
		}

		public static ReminderTimeMode fromInt(byte value) {
			ReminderTimeMode reminderTimeMode = ReminderTimeMode.map.get(value);
			return reminderTimeMode;
		}

		public static ReminderTimeMode fromString(String name) {
			ReminderTimeMode reminderTimeMode = ReminderTimeMode.stringToObjectMap
					.get(name);
			return reminderTimeMode;
		}
	}

	public ReminderUiData2(Long id, TaskUiData2 taskUiData, long reminderDateTime,
			byte reminderTimeModeValue, String text, boolean enabled, boolean isAlarm,
			String ringtone, Long ringtoneFadeInTime, Integer playingTime,
			Integer automaticSnoozeDuration, Integer automaticSnoozesMaxCount,
			Boolean vibrate, String vibratePattern, Boolean led, String ledPattern,
			Integer ledColor) {
		this.id = id;
		this.taskUiData = taskUiData;
		this.reminderDateTime = reminderDateTime;
		this.reminderTimeModeValue = reminderTimeModeValue;
		this.text = text;
		this.enabled = enabled;
		this.isAlarm = isAlarm;
		this.ringtone = ringtone;
		this.ringtoneFadeInTime = ringtoneFadeInTime;
		this.playingTime = playingTime;
		this.automaticSnoozeDuration = automaticSnoozeDuration;
		this.automaticSnoozesMaxCount = automaticSnoozesMaxCount;
		this.vibrate = vibrate;
		this.vibratePattern = vibratePattern;
		this.led = led;
		this.ledPattern = ledPattern;
		this.ledColor = ledColor;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TaskUiData2 getTask() {
		return taskUiData;
	}

	public void setTask(TaskUiData2 task) {
		taskUiData = task;
	}

	public long getReminderDateTime() {
		return reminderDateTime;
	}

	public void setReminderDateTime(long reminderDateTime) {
		this.reminderDateTime = reminderDateTime;
	}

	public byte getReminderTimeModeValue() {
		return reminderTimeModeValue;
	}

	public void setReminderTimeModeValue(byte reminderTimeModeValue) {
		this.reminderTimeModeValue = reminderTimeModeValue;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean getIsAlarm() {
		return isAlarm;
	}

	public void setIsAlarm(boolean isAlarm) {
		this.isAlarm = isAlarm;
	}

	public String getRingtone() {
		return ringtone;
	}

	public void setRingtone(String ringtone) {
		this.ringtone = ringtone;
	}

	public Long getRingtoneFadeInTime() {
		return ringtoneFadeInTime;
	}

	public void setRingtoneFadeInTime(Long ringtoneFadeInTime) {
		this.ringtoneFadeInTime = ringtoneFadeInTime;
	}

	public Integer getPlayingTime() {
		return playingTime;
	}

	public void setPlayingTime(Integer playingTime) {
		this.playingTime = playingTime;
	}

	public Integer getAutomaticSnoozeDuration() {
		return automaticSnoozeDuration;
	}

	public void setAutomaticSnoozeDuration(Integer automaticSnoozeDuration) {
		this.automaticSnoozeDuration = automaticSnoozeDuration;
	}

	public Integer getAutomaticSnoozesMaxCount() {
		return automaticSnoozesMaxCount;
	}

	public void setAutomaticSnoozesMaxCount(Integer automaticSnoozesMaxCount) {
		this.automaticSnoozesMaxCount = automaticSnoozesMaxCount;
	}

	public Boolean getVibrate() {
		return vibrate;
	}

	public void setVibrate(Boolean vibrate) {
		this.vibrate = vibrate;
	}

	public String getVibratePattern() {
		return vibratePattern;
	}

	public void setVibratePattern(String vibratePattern) {
		this.vibratePattern = vibratePattern;
	}

	public Boolean getLed() {
		return led;
	}

	public void setLed(Boolean led) {
		this.led = led;
	}

	public String getLedPattern() {
		return ledPattern;
	}

	public void setLedPattern(String ledPattern) {
		this.ledPattern = ledPattern;
	}

	public Integer getLedColor() {
		return ledColor;
	}

	public void setLedColor(Integer ledColor) {
		this.ledColor = ledColor;
	}

	public String getTextForAbsoluteTime() {
		DateFormat mDateFormat = DateFormat.getDateTimeInstance();
		String dateString = mDateFormat.format(new Date(reminderDateTime));
		return dateString;
	}

	public String getTextForAbsoluteTime(long reminderDateTime) {
		DateFormat mDateFormat = DateFormat.getDateTimeInstance();
		String dateString = mDateFormat.format(new Date(reminderDateTime));
		return dateString;
	}

	public void generateTableRow(TableLayout tableLayout, LayoutInflater mLayoutInflater,
			Resources resources, int headerStringId, String text) {
		TableRow tableRow = (TableRow) mLayoutInflater.inflate(
				R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
		TextView textViewHeader = (TextView) tableRow
				.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
		textViewHeader.setText(resources.getString(headerStringId));
		TextView textViewValue = (TextView) tableRow
				.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
		textViewValue.setText(text);
		tableLayout.addView(tableRow);
	}

	public String getTextForReminderTimeIntervalAndTimeModeAndNextAlarmTime2(
			Context context, TaskUiData2 taskUiData2) {
		Resources resources = context.getResources();
		long now = System.currentTimeMillis();
		String textForReminderTimeIntervalAndTimeMode;
		if (reminderDateTime == 0) {
			textForReminderTimeIntervalAndTimeMode = resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_at_task_start);
		} else {
			long absRemindOffset = Math.abs(reminderDateTime);
			String textForTimeInterval = Helper.getTextForTimeInterval(context,
					absRemindOffset);
			String textForReminderTimeMode = reminderDateTime > 0
					&& reminderTimeModeValue == ReminderTimeMode.TIME_BEFORE_EVENT
							.getValue()
					|| reminderDateTime < 0
					&& reminderTimeModeValue == ReminderTimeMode.TIME_AFTER_EVENT
							.getValue() ? resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_before_task_start)
					: resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_after_task_start);
			textForReminderTimeIntervalAndTimeMode = String.format(resources
					.getString(R.string.text_for_reminder_time_interval_and_time_mode),
					textForTimeInterval, textForReminderTimeMode);
		}
		Long nextReminderDateTime;
		Long startDateTime = taskUiData2.getStartDateTime();
		if (startDateTime != null) {
			if (RecurrenceInterval.fromInt(taskUiData2.getRecurrenceIntervalValue()) == RecurrenceInterval.ONE_TIME) {
				if (ReminderTimeMode.fromInt(reminderTimeModeValue) == ReminderTimeMode.TIME_BEFORE_EVENT) {
					nextReminderDateTime = startDateTime - reminderDateTime;
				} else {
					nextReminderDateTime = startDateTime + reminderDateTime;
				}
				if (nextReminderDateTime <= now) {
					nextReminderDateTime = null;
				}
			} else {
				nextReminderDateTime = getNextReminderDateTime(context, now, true,
						Helper.getFirstDayOfWeek(context));
			}
		} else {
			nextReminderDateTime = null;
		}
		String timeText;
		if (nextReminderDateTime != null) {
			timeText = getTextForAbsoluteTime(nextReminderDateTime);
		} else {
			timeText = resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_next_reminder_time_none);
		}
		String textNextAlarmTime = String
				.format(resources
						.getString(R.string.fragment_view_task_part_reminders_text_next_alarm_time),
						timeText);
		String textForReminderTimeIntervalAndTimeModeAndNextAlarmTime = String
				.format(resources
						.getString(R.string.text_for_reminder_time_interval_and_time_mode_and_next_alarm_time),
						textForReminderTimeIntervalAndTimeMode, textNextAlarmTime);
		return textForReminderTimeIntervalAndTimeModeAndNextAlarmTime;
	}

	public String getTextForReminderTimeIntervalAndTimeModeAndNextAlarmTime(
			Context context, TaskUiData2 taskUiData2) {
		Resources resources = context.getResources();
		long now = System.currentTimeMillis();
		String textForReminderTimeIntervalAndTimeMode;
		if (reminderDateTime == 0) {
			textForReminderTimeIntervalAndTimeMode = resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_at_task_start);
		} else {
			long absRemindOffset = Math.abs(reminderDateTime);
			String textForTimeInterval = Helper.getTextForTimeInterval(context,
					absRemindOffset);
			String textForReminderTimeMode = reminderDateTime > 0
					&& reminderTimeModeValue == ReminderTimeMode.TIME_BEFORE_EVENT
							.getValue()
					|| reminderDateTime < 0
					&& reminderTimeModeValue == ReminderTimeMode.TIME_AFTER_EVENT
							.getValue() ? resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_before_task_start)
					: resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_after_task_start);
			textForReminderTimeIntervalAndTimeMode = String.format(resources
					.getString(R.string.text_for_reminder_time_interval_and_time_mode),
					textForTimeInterval, textForReminderTimeMode);
		}
		Long nextReminderDateTime;
		Long startDateTime = taskUiData2.getStartDateTime();
		if (startDateTime != null) {
			if (RecurrenceInterval.fromInt(taskUiData2.getRecurrenceIntervalValue()) == RecurrenceInterval.ONE_TIME) {
				if (ReminderTimeMode.fromInt(reminderTimeModeValue) == ReminderTimeMode.TIME_BEFORE_EVENT) {
					nextReminderDateTime = startDateTime - reminderDateTime;
				} else {
					nextReminderDateTime = startDateTime + reminderDateTime;
				}
				if (nextReminderDateTime <= now) {
					nextReminderDateTime = null;
				}
			} else {
				nextReminderDateTime = getNextReminderDateTime(context, now, true,
						Helper.getFirstDayOfWeek(context));
			}
		} else {
			nextReminderDateTime = null;
		}
		String timeText;
		if (nextReminderDateTime != null) {
			timeText = getTextForAbsoluteTime(nextReminderDateTime);
		} else {
			timeText = resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_next_reminder_time_none);
		}
		String textNextAlarmTime = String
				.format(resources
						.getString(R.string.fragment_view_task_part_reminders_text_next_alarm_time),
						timeText);
		String textForReminderTimeIntervalAndTimeModeAndNextAlarmTime = String
				.format(resources
						.getString(R.string.text_for_reminder_time_interval_and_time_mode_and_next_alarm_time),
						textForReminderTimeIntervalAndTimeMode, textNextAlarmTime);
		return textForReminderTimeIntervalAndTimeModeAndNextAlarmTime;
	}

	public String getRingtone2(final Context context, final int preferenceId) {
		try {
			return DataProvider.callInTx(null, context, new Callable<String>() {
				@Override
				public String call() {
					if (ringtone != null) {
						return ringtone;
					} else {
						TaskUiData2 task = getTask();
						if (task != null) {
							return task.getRingtone2(context, preferenceId);
						} else {
							return Helper.getStringPreferenceValue(
									context,
									preferenceId,
									RingtoneManager.getDefaultUri(
											isAlarm ? RingtoneManager.TYPE_ALARM
													: RingtoneManager.TYPE_NOTIFICATION)
											.toString());
						}
					}
				}
			});
		} catch (Exception e) {
			return null;
		}
	}

	public String getRingtoneTitle(Context context) {
		int preferenceId = isAlarm ? R.string.preference_key_alarm_ringtone
				: R.string.preference_key_notification_ringtone;
		String ringtoneString = getRingtone2(context, preferenceId);
		return Helper.getRingtoneTitle(context, ringtoneString);
	}

	public long getRingtoneFadeInTime2(Context context, int preferenceKey,
			long defaultValue, Long minValue, Long maxValue) {
		if (ringtoneFadeInTime != null) {
			return ringtoneFadeInTime;
		}
		TaskUiData2 task = getTask();
		if (task != null) {
			return task.getRingtoneFadeInTime2(context, preferenceKey, defaultValue,
					minValue, maxValue);
		}
		return Helper.getLongFromStringPreferenceValue(context, preferenceKey,
				defaultValue, minValue, maxValue);
	}

	public int getPlayingTime2(Context context, int preferenceKey, int defaultValue,
			Integer minValue, Integer maxValue) {
		if (playingTime != null) {
			return playingTime;
		}
		TaskUiData2 task = getTask();
		if (task != null) {
			return task.getPlayingTime2(context, preferenceKey, defaultValue, minValue,
					maxValue);
		}
		return Helper.getIntegerFromStringPreferenceValue(context, preferenceKey, null,
				defaultValue, minValue, maxValue);
	}

	public int getAutomaticSnoozeDuration2(Context context, int preferenceKey,
			int defaultValue, Integer minValue, Integer maxValue) {
		if (automaticSnoozeDuration != null) {
			return automaticSnoozeDuration;
		}
		TaskUiData2 task = getTask();
		if (task != null) {
			return task.getAutomaticSnoozeDuration2(context, preferenceKey, defaultValue,
					minValue, maxValue);
		}
		return Helper.getIntegerFromStringPreferenceValue(context, preferenceKey, null,
				defaultValue, minValue, maxValue);
	}

	public int getAutomaticSnoozesMaxCount2(Context context, int preferenceKey,
			int defaultValue, Integer minValue, Integer maxValue) {
		if (automaticSnoozesMaxCount != null) {
			return automaticSnoozesMaxCount;
		}
		TaskUiData2 task = getTask();
		if (task != null) {
			return task.getAutomaticSnoozesMaxCount2(context, preferenceKey,
					defaultValue, minValue, maxValue);
		}
		return Helper.getIntegerFromStringPreferenceValue(context, preferenceKey, null,
				defaultValue, minValue, maxValue);
	}

	public boolean getVibrate2(Context context, String preferenceKey, boolean defaultValue) {
		if (vibrate != null) {
			return vibrate;
		}
		TaskUiData2 task = getTask();
		if (task != null) {
			return task.getVibrate2(context, preferenceKey, defaultValue);
		}
		return Helper.getBooleanPreferenceValue(context, preferenceKey, defaultValue);
	}

	public Long getNextReminderDateTime(final Context context,
			final long controlDateTime,
			final boolean includeEventsMatchingControlDateTime, final int firstDayOfWeek) {
		if (reminderTimeModeValue == ReminderTimeMode.ABSOLUTE_TIME.getValue()) {
			if (reminderDateTime < controlDateTime) {
				return null;
			} else {
				return reminderDateTime;
			}
		}
		final LongHolder resultReminderDateTime = new LongHolder();
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				TaskUiData2 task = getTask();
				Long occurrenceStartDateTime;
				long controlDateTime2;
				long supposedResultReminderDateTime;
				// check PreviousRepetitionStartDateTime
				if (ReminderTimeMode.fromInt(reminderTimeModeValue).equals(
						ReminderTimeMode.TIME_AFTER_EVENT)) {
					occurrenceStartDateTime = task.getNearestOccurrenceStartDateTime(
							controlDateTime, includeEventsMatchingControlDateTime, true,
							firstDayOfWeek);
					if (occurrenceStartDateTime != null) {
						supposedResultReminderDateTime = occurrenceStartDateTime
								+ reminderDateTime;
						if (includeEventsMatchingControlDateTime
								&& supposedResultReminderDateTime >= controlDateTime
								|| !includeEventsMatchingControlDateTime
								&& supposedResultReminderDateTime > controlDateTime) {
							resultReminderDateTime.value = supposedResultReminderDateTime;
							// check for the earliest suitable recurrence of the task
							controlDateTime2 = occurrenceStartDateTime;
							while (true) {
								occurrenceStartDateTime = task
										.getNearestOccurrenceStartDateTime(
												controlDateTime2, false, true,
												firstDayOfWeek);
								if (occurrenceStartDateTime == null) {
									return;
								}
								supposedResultReminderDateTime = occurrenceStartDateTime
										+ reminderDateTime;
								if (includeEventsMatchingControlDateTime
										&& supposedResultReminderDateTime >= controlDateTime
										|| !includeEventsMatchingControlDateTime
										&& supposedResultReminderDateTime > controlDateTime) {
									resultReminderDateTime.value = supposedResultReminderDateTime;
								}
								controlDateTime2 = occurrenceStartDateTime;
							}
						}
					}
				}
				// check NextRepetitionStartDateTime
				controlDateTime2 = includeEventsMatchingControlDateTime ? controlDateTime - 1
						: controlDateTime;
				while (true) {
					occurrenceStartDateTime = task.getNearestOccurrenceStartDateTime(
							controlDateTime2, false, false, firstDayOfWeek);
					if (occurrenceStartDateTime == null) {
						resultReminderDateTime.value = null;
						return;
					}
					supposedResultReminderDateTime = occurrenceStartDateTime
							- (reminderTimeModeValue == ReminderTimeMode.TIME_BEFORE_EVENT
									.getValue() ? reminderDateTime : -reminderDateTime);
					if (includeEventsMatchingControlDateTime
							&& supposedResultReminderDateTime >= controlDateTime
							|| !includeEventsMatchingControlDateTime
							&& supposedResultReminderDateTime > controlDateTime) {
						resultReminderDateTime.value = supposedResultReminderDateTime;
						return;
					}
					controlDateTime2 = occurrenceStartDateTime;
				}
			}
		});
		return resultReminderDateTime.value;
	}

	protected ReminderUiData2(Parcel in) {
		id = in.readByte() == 0x00 ? null : in.readLong();
		// taskUiData = (TaskUiData2) in.readValue(TaskUiData2.class.getClassLoader());
		reminderDateTime = in.readLong();
		reminderTimeModeValue = in.readByte();
		text = in.readString();
		enabled = in.readByte() != 0x00;
		isAlarm = in.readByte() != 0x00;
		ringtone = in.readString();
		ringtoneFadeInTime = in.readByte() == 0x00 ? null : in.readLong();
		playingTime = in.readByte() == 0x00 ? null : in.readInt();
		automaticSnoozeDuration = in.readByte() == 0x00 ? null : in.readInt();
		automaticSnoozesMaxCount = in.readByte() == 0x00 ? null : in.readInt();
		byte vibrateVal = in.readByte();
		vibrate = vibrateVal == 0x02 ? null : vibrateVal != 0x00;
		vibratePattern = in.readString();
		byte ledVal = in.readByte();
		led = ledVal == 0x02 ? null : ledVal != 0x00;
		ledPattern = in.readString();
		ledColor = in.readByte() == 0x00 ? null : in.readInt();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (id == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(id);
		}
		// dest.writeValue(taskUiData);
		dest.writeLong(reminderDateTime);
		dest.writeByte(reminderTimeModeValue);
		dest.writeString(text);
		dest.writeByte((byte) (enabled ? 0x01 : 0x00));
		dest.writeByte((byte) (isAlarm ? 0x01 : 0x00));
		dest.writeString(ringtone);
		if (ringtoneFadeInTime == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(ringtoneFadeInTime);
		}
		if (playingTime == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(playingTime);
		}
		if (automaticSnoozeDuration == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(automaticSnoozeDuration);
		}
		if (automaticSnoozesMaxCount == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(automaticSnoozesMaxCount);
		}
		if (vibrate == null) {
			dest.writeByte((byte) 0x02);
		} else {
			dest.writeByte((byte) (vibrate ? 0x01 : 0x00));
		}
		dest.writeString(vibratePattern);
		if (led == null) {
			dest.writeByte((byte) 0x02);
		} else {
			dest.writeByte((byte) (led ? 0x01 : 0x00));
		}
		dest.writeString(ledPattern);
		if (ledColor == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(ledColor);
		}
	}

	public static final Parcelable.Creator<ReminderUiData2> CREATOR = new Parcelable.Creator<ReminderUiData2>() {
		@Override
		public ReminderUiData2 createFromParcel(Parcel in) {
			return new ReminderUiData2(in);
		}

		@Override
		public ReminderUiData2[] newArray(int size) {
			return new ReminderUiData2[size];
		}
	};
}