package biz.advancedcalendar.activities.activityedittask;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.alarmer.R;
//import biz.advancedcalendar.R.string;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.TaskOccurrence;
import biz.advancedcalendar.utils.Helper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TaskWithDependentsUiData3 implements Parcelable {
	public TaskUiData2 TaskUiData;
	public List<ReminderUiData2> RemindersUiData;
	public List<Integer> TaskOccurrences;
	private TaskUiDataWithCurrentStateSupplier taskUiDataWithCurrentStateSupplier;
	private byte timeUnitsStartingIndexValue;

	public interface TaskUiDataWithCurrentStateSupplier {
		TaskUiData2 getTaskUiDataWithCurrentState();

		Set<WantingItem> getWantingItemSetForRecurrenceData();

		Set<WantingItem> getWantingItemSetForTask();

		Set<WantingItem> testTaskUiDataWithCurrentState();
	}

	public String getDelimitedSequenceOfDatesOfYear(Context context) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Integer> taskOccurrenceList = TaskOccurrences;
		String delimiter = "; ";
		int occurrencesCount = taskOccurrenceList.size();
		SimpleDateFormat s = new SimpleDateFormat("MMMMM, dd");
		SimpleDateFormat s2 = new SimpleDateFormat("MMMMM");
		for (int i = 0; i < occurrencesCount; i++) {
			stringBuilder.append(delimiter);
			stringBuilder
					.append(getDateString(context, s, s2, taskOccurrenceList.get(i)));
		}
		if (stringBuilder.length() > 0) {
			stringBuilder.delete(0, delimiter.length());
		}
		String string = stringBuilder.toString();
		return string;
	}

	public String getDelimitedSequenceOfOrdinalNumbers(Context context) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Integer> taskOccurrenceList = TaskOccurrences;
		String delimiter = ", ";
		int occurrencesCount = taskOccurrenceList.size();
		timeUnitsStartingIndexValue = (byte) (Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_time_units_starting_index,
						R.array.time_units_starting_index_values_array,
						R.integer.time_units_starting_index_default_value) - 1);
		int delta = timeUnitsStartingIndexValue - 1;
		for (int i = 0; i < occurrencesCount; i++) {
			stringBuilder.append(delimiter);
			stringBuilder.append(taskOccurrenceList.get(i) + delta);
		}
		if (stringBuilder.length() > 0) {
			stringBuilder.delete(0, delimiter.length());
		}
		String string = stringBuilder.toString();
		return string;
	}

	public String getDelimitedSequenceOfOrdinalNumbersForMonth(Context context) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Integer> taskOccurrenceList = TaskOccurrences;
		String delimiter = ", ";
		int occurrencesCount = taskOccurrenceList.size();
		for (int i = 0; i < occurrencesCount; i++) {
			stringBuilder.append(delimiter);
			int ordinalNumber = taskOccurrenceList.get(i);
			if (ordinalNumber == 32) {
				stringBuilder.append(context.getResources().getString(R.string.last));
			} else {
				stringBuilder.append(ordinalNumber);
			}
		}
		int delimiterLength = stringBuilder.length();
		if (delimiterLength > 0) {
			stringBuilder.delete(0, delimiter.length());
		}
		String string = stringBuilder.toString();
		return string;
	}

	public int getOrdinalNumberOfOccurrenceOfWeekdayInMonth() {
		List<Integer> taskOccurrenceList = TaskOccurrences;
		int ordinalNumber = 1;
		int occurrencesCount = taskOccurrenceList.size();
		for (int i = occurrencesCount - 1; i < occurrencesCount; i++) {
			ordinalNumber = taskOccurrenceList.get(i);
		}
		return ordinalNumber;
	}

	public String getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(
			Context context) {
		StringBuilder stringBuilder = new StringBuilder();
		List<Integer> taskOccurrenceList = TaskOccurrences;
		String delimiter = ", ";
		int occurrencesCount = taskOccurrenceList.size();
		if (occurrencesCount > 0) {
			for (int i = occurrencesCount - 1; i < occurrencesCount; i++) {
				int ordinalNumber = taskOccurrenceList.get(i);
				int index = Helper.findIndexOfValueInStringArray(context,
						String.valueOf(ordinalNumber),
						R.array.week_day_number_values_array);
				String ordinalNumberString = Helper.getStringValueFromStringArray(
						context, index, R.array.week_day_number_lower_case_titles_array,
						null);
				if (ordinalNumberString != null) {
					stringBuilder.append(delimiter);
					stringBuilder.append(ordinalNumberString);
				}
			}
		}
		if (stringBuilder.length() > delimiter.length()) {
			stringBuilder.delete(0, delimiter.length());
		}
		String string = stringBuilder.toString();
		return string;
	}

	public String getDelimitedSequenceOfWeekDays(Context context,
			boolean isMonthlyRecurrentOnNthWeekDay) {
		boolean[] occurrencesOnDaysOfWeek = new boolean[] {false, false, false, false,
				false, false, false};
		List<Integer> taskOccurrenceList = TaskOccurrences;
		int occurrencesCount = taskOccurrenceList.size();
		if (isMonthlyRecurrentOnNthWeekDay) {
			--occurrencesCount;
		}
		for (int i = 0; i < occurrencesCount; i++) {
			int ordinalNumber = taskOccurrenceList.get(i);
			if (ordinalNumber <= occurrencesOnDaysOfWeek.length) {
				occurrencesOnDaysOfWeek[ordinalNumber - 1] = true;
			}
		}
		StringBuilder stringBuilder = new StringBuilder();
		String delimiter = ", ";
		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		int firstDayOfWeek = Helper.getFirstDayOfWeek(context);
		// move calendar to the beginning of the week
		calendar.add(Calendar.DAY_OF_YEAR, firstDayOfWeek - dayOfWeek);
		if (firstDayOfWeek > dayOfWeek) {
			calendar.add(Calendar.DAY_OF_YEAR, -7);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
		for (int i = 0; i < occurrencesOnDaysOfWeek.length; i++) {
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			if (day <= occurrencesOnDaysOfWeek.length) {
				if (occurrencesOnDaysOfWeek[day - 1]) {
					stringBuilder.append(delimiter);
					stringBuilder
							.append(sdf.format(new Date(calendar.getTimeInMillis())));
				}
			}
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		if (stringBuilder.length() > 0) {
			stringBuilder.delete(0, delimiter.length());
		}
		String string = stringBuilder.toString();
		return string;
	}

	public TaskWithDependentsUiData3(TaskWithDependentsUiData3 taskWithDependentsUiData) {
		TaskOccurrences = new ArrayList<Integer>();
		for (Integer taskOccurrence : taskWithDependentsUiData.TaskOccurrences) {
			Integer newTaskOccurrence = taskOccurrence;
			TaskOccurrences.add(newTaskOccurrence);
		}
		TaskUiData = new TaskUiData2(taskWithDependentsUiData.TaskUiData);
		RemindersUiData = new ArrayList<ReminderUiData2>();
		for (ReminderUiData2 reminder : taskWithDependentsUiData.RemindersUiData) {
			ReminderUiData2 newReminderUiData = new ReminderUiData2(reminder.getId(),
					TaskUiData, reminder.getReminderDateTime(),
					reminder.getReminderTimeModeValue(), reminder.getText(),
					reminder.getEnabled(), reminder.getIsAlarm(), reminder.getRingtone(),
					reminder.getRingtoneFadeInTime(), reminder.getPlayingTime(),
					reminder.getAutomaticSnoozeDuration(),
					reminder.getAutomaticSnoozesMaxCount(), reminder.getVibrate(),
					reminder.getVibratePattern(), reminder.getLed(),
					reminder.getLedPattern(), reminder.getLedColor());
			RemindersUiData.add(newReminderUiData);
		}
	}

	public TaskWithDependentsUiData3(TaskWithDependents taskWithDependents) {
		TaskOccurrences = new ArrayList<Integer>();
		for (TaskOccurrence taskOccurrence : taskWithDependents.taskOccurrences) {
			Integer newTaskOccurrence = taskOccurrence.getOrdinalNumber();
			TaskOccurrences.add(newTaskOccurrence);
		}
		Task task = taskWithDependents.task;
		TaskUiData = new TaskUiData2(task.getId(), task.getParentId(), task.getName(),
				task.getColor(), task.getStartDateTime(), task.getEndDateTime(),
				task.getIsCompleted(), task.getPercentOfCompletion(), task.getDeleted(),
				task.getPriority(), task.getSortOrder(), task.getDescription(),
				task.getLocation(), task.getRecurrenceIntervalValue(),
				task.getTimeUnitsCount(), task.getOccurrencesMaxCount(),
				task.getRepetitionEndDateTime(), task.getAlarmRingtone(),
				task.getNotificationRingtone(), task.getRingtoneFadeInTime(),
				task.getPlayingTime(), task.getAutomaticSnoozeDuration(),
				task.getAutomaticSnoozesMaxCount(), task.getVibrate(),
				task.getVibratePattern(), task.getLed(), task.getLedPattern(),
				task.getLedColor(), TaskOccurrences, RemindersUiData);
		RemindersUiData = new ArrayList<ReminderUiData2>();
		for (Reminder reminder : taskWithDependents.reminders) {
			ReminderUiData2 newReminderUiData = new ReminderUiData2(reminder.getId(),
					TaskUiData, reminder.getReminderDateTime(),
					reminder.getReminderTimeModeValue(), reminder.getText(),
					reminder.getEnabled(), reminder.getIsAlarm(), reminder.getRingtone(),
					reminder.getRingtoneFadeInTime(), reminder.getPlayingTime(),
					reminder.getAutomaticSnoozeDuration(),
					reminder.getAutomaticSnoozesMaxCount(), reminder.getVibrate(),
					reminder.getVibratePattern(), reminder.getLed(),
					reminder.getLedPattern(), reminder.getLedColor());
			RemindersUiData.add(newReminderUiData);
		}
	}

	public void setTaskUiDataWithCurrentStateSupplier(
			TaskUiDataWithCurrentStateSupplier taskUiDataWithCurrentStateSupplier) {
		this.taskUiDataWithCurrentStateSupplier = taskUiDataWithCurrentStateSupplier;
	}

	public int getTimeUnitsStartingIndexValue() {
		return timeUnitsStartingIndexValue;
	}

	public Set<WantingItem> getWantingItemSet() {
		return taskUiDataWithCurrentStateSupplier.getWantingItemSetForRecurrenceData();
	}

	public Set<WantingItem> testTaskUiDataWithCurrentState() {
		return taskUiDataWithCurrentStateSupplier.testTaskUiDataWithCurrentState();
	}

	public TaskUiData2 getTaskUiDataWithCurrentState() {
		return taskUiDataWithCurrentStateSupplier.getTaskUiDataWithCurrentState();
	}

	public String getDateString(Context context, SimpleDateFormat s, SimpleDateFormat s2,
			int occurrenceCode) {
		int month;
		int day;
		if (occurrenceCode <= 31) {
			month = 1;
			day = occurrenceCode;
		} else if (occurrenceCode <= 61) {
			month = 2;
			day = occurrenceCode - 31;
			if (occurrenceCode == 61) {
				day = -1;
			}
		} else if (occurrenceCode <= 92) {
			month = 3;
			day = occurrenceCode - 61;
		} else if (occurrenceCode <= 122) {
			month = 4;
			day = occurrenceCode - 92;
		} else if (occurrenceCode <= 153) {
			month = 5;
			day = occurrenceCode - 122;
		} else if (occurrenceCode <= 183) {
			month = 6;
			day = occurrenceCode - 153;
		} else if (occurrenceCode <= 214) {
			month = 7;
			day = occurrenceCode - 183;
		} else if (occurrenceCode <= 245) {
			month = 8;
			day = occurrenceCode - 214;
		} else if (occurrenceCode <= 275) {
			month = 9;
			day = occurrenceCode - 245;
		} else if (occurrenceCode <= 306) {
			month = 10;
			day = occurrenceCode - 275;
		} else if (occurrenceCode <= 336) {
			month = 11;
			day = occurrenceCode - 306;
		} else {
			month = 12;
			day = occurrenceCode - 336;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(2016, month - 1, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		String d;
		if (day != -1) {
			calendar.set(Calendar.DAY_OF_MONTH, day);
			d = s.format(new Date(calendar.getTimeInMillis()));
		} else {
			d = s2.format(new Date(calendar.getTimeInMillis()))
					+ ", "
					+ context.getResources().getString(
							R.string.fragment_edit_task_part_main_last_day);
		}
		return d;
	}

	protected TaskWithDependentsUiData3(Parcel in) {
		TaskUiData = (TaskUiData2) in.readValue(TaskUiData2.class.getClassLoader());
		if (in.readByte() == 0x01) {
			RemindersUiData = new ArrayList<ReminderUiData2>();
			in.readList(RemindersUiData, ReminderUiData.class.getClassLoader());
		} else {
			RemindersUiData = null;
		}
		if (in.readByte() == 0x01) {
			TaskOccurrences = new ArrayList<Integer>();
			in.readList(TaskOccurrences, Integer.class.getClassLoader());
		} else {
			TaskOccurrences = null;
		}
		timeUnitsStartingIndexValue = in.readByte();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(TaskUiData);
		if (RemindersUiData == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(RemindersUiData);
		}
		if (TaskOccurrences == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(TaskOccurrences);
		}
		dest.writeByte(timeUnitsStartingIndexValue);
	}

	public static final Parcelable.Creator<TaskWithDependentsUiData3> CREATOR = new Parcelable.Creator<TaskWithDependentsUiData3>() {
		@Override
		public TaskWithDependentsUiData3 createFromParcel(Parcel in) {
			return new TaskWithDependentsUiData3(in);
		}

		@Override
		public TaskWithDependentsUiData3[] newArray(int size) {
			return new TaskWithDependentsUiData3[size];
		}
	};
}