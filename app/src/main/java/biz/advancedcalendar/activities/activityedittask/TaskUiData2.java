package biz.advancedcalendar.activities.activityedittask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.ValueMustBe;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.DaoSession;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.PRIORITY;
import biz.advancedcalendar.greendao.TaskOccurrence;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence2;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaskUiData2 implements Parcelable {
	private Long id;
	private Long calendarId;
	private Long parentId;
	private String name;
	private Integer color;
	private Long startDateTime;
	private Long endDateTime;
	private boolean isCompleted;
	private short percentOfCompletion;
	private boolean deleted;
	private short priority;
	private int sortOrder;
	private String description;
	private String location;
	private short recurrenceIntervalValue;
	private Integer timeUnitsCount;
	private Integer occurrencesMaxCount;
	private Long repetitionEndDateTime;
	private String alarmRingtone;
	private String notificationRingtone;
	private Long ringtoneFadeInTime;
	private Integer playingTime;
	private Integer automaticSnoozeDuration;
	private Integer automaticSnoozesMaxCount;
	private Boolean vibrate;
	private String vibratePattern;
	private Boolean led;
	private String ledPattern;
	private Integer ledColor;
	private List<Integer> taskOccurrenceList;
	private List<ReminderUiData2> reminderList;
	private String taskTitleForQuickReminder;
	private int timeUnitsStartingIndexValue;

	public String getDelimitedSequenceOfDatesOfYear(Context context) {
		StringBuilder stringBuilder = new StringBuilder();
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

	public String getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(
			Context context) {
		StringBuilder stringBuilder = new StringBuilder();
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

	public int getTimeUnitsStartingIndexValue() {
		return timeUnitsStartingIndexValue;
	}

	public class DateTimeHolder {
		public DateTimeHolder(Calendar dateTime, DateTimeResult result) {
			super();
			DateTime = dateTime;
			this.result = result;
		}

		Calendar DateTime;
		DateTimeResult result;
	}

	public enum DateTimeResult {
		NOT_EXISTS(0), INFINITY(1), EXACT(2);
		private int value;

		private DateTimeResult(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static DateTimeResult fromInt(int x) {
			switch (x) {
			case 0:
				return NOT_EXISTS;
			case 1:
				return INFINITY;
			case 2:
				return EXACT;
			default:
				return null;
			}
		}
	}

	public enum TimeUnit {
		ONE_TIME((short) CommonConstants.TIME_UNIT_ONE_TIME), MINUTE(
				(short) CommonConstants.TIME_UNIT_MINUTE), HOUR(
				(short) CommonConstants.TIME_UNIT_HOUR), DAY(
				(short) CommonConstants.TIME_UNIT_DAY), WEEK(
				(short) CommonConstants.TIME_UNIT_WEEK), MONTH(
				(short) CommonConstants.TIME_UNIT_MONTH), YEAR(
				(short) CommonConstants.TIME_UNIT_YEAR);
		private short value;

		private TimeUnit(short value) {
			this.value = value;
		}

		public short getShortValue() {
			return value;
		}

		public int getIntegerValue() {
			return value;
		}

		private static TimeUnit fromRecurrenceIntervalInternal(
				RecurrenceInterval recurrenceInterval) {
			switch (recurrenceInterval) {
			case ONE_TIME:
			default:
				return ONE_TIME;
			case DAYS:
				return DAY;
			case WEEKS:
				return WEEK;
			case MONTHS_ON_DATE:
			case MONTHS_ON_NTH_WEEK_DAY:
				return MONTH;
			case YEARS:
				return YEAR;
			case MINUTES:
				return MINUTE;
			case HOURS:
				return HOUR;
			}
		}

		private static Map<Short, TimeUnit> map = new HashMap<Short, TimeUnit>();
		private static Map<RecurrenceInterval, TimeUnit> recurrenceIntervalToTimeUnitMap = new HashMap<RecurrenceInterval, TimeUnit>();
		static {
			for (TimeUnit timeUnit : TimeUnit.values()) {
				TimeUnit.map.put(timeUnit.value, timeUnit);
			}
			for (RecurrenceInterval recurrenceInterval : RecurrenceInterval.values()) {
				TimeUnit timeUnit = TimeUnit
						.fromRecurrenceIntervalInternal(recurrenceInterval);
				TimeUnit.recurrenceIntervalToTimeUnitMap
						.put(recurrenceInterval, timeUnit);
			}
		}

		public static TimeUnit fromInt(short value) {
			TimeUnit timeUnit = TimeUnit.map.get(value);
			return timeUnit;
		}

		public static TimeUnit fromRecurrenceInterval(RecurrenceInterval value) {
			TimeUnit timeUnit = TimeUnit.recurrenceIntervalToTimeUnitMap.get(value);
			return timeUnit;
		}
	}

	public enum BusinessHoursTaskDisplayingPolicy {
		DISPLAY_SEPARATED((byte) 1), DISPLAY_AS_USUAL((byte) 2), DO_NOT_DISPLAY((byte) 3), ;
		private byte value;

		private BusinessHoursTaskDisplayingPolicy(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, BusinessHoursTaskDisplayingPolicy> map = new HashMap<Byte, BusinessHoursTaskDisplayingPolicy>();
		private static Map<String, BusinessHoursTaskDisplayingPolicy> stringToObjectMap = new HashMap<String, BusinessHoursTaskDisplayingPolicy>();
		static {
			for (BusinessHoursTaskDisplayingPolicy businessHoursTaskDisplayingPolicy : BusinessHoursTaskDisplayingPolicy
					.values()) {
				BusinessHoursTaskDisplayingPolicy.map.put(
						businessHoursTaskDisplayingPolicy.value,
						businessHoursTaskDisplayingPolicy);
				BusinessHoursTaskDisplayingPolicy.stringToObjectMap.put(
						businessHoursTaskDisplayingPolicy.name(),
						businessHoursTaskDisplayingPolicy);
			}
		}

		public static BusinessHoursTaskDisplayingPolicy fromInt(byte value) {
			BusinessHoursTaskDisplayingPolicy businessHoursTaskDisplayingPolicy = BusinessHoursTaskDisplayingPolicy.map
					.get(value);
			return businessHoursTaskDisplayingPolicy;
		}

		public static BusinessHoursTaskDisplayingPolicy fromString(String name) {
			BusinessHoursTaskDisplayingPolicy businessHoursTaskDisplayingPolicy = BusinessHoursTaskDisplayingPolicy.stringToObjectMap
					.get(name);
			return businessHoursTaskDisplayingPolicy;
		}
	}

	@SuppressLint("UseSparseArrays")
	public enum HoursRulerDisplayingPolicy {
		DISPLAY_ONLY_ONE_HOURS_RULER((byte) 1), DISPLAY_SEPARATE_HOURS_RULER_FOR_EACH_DAY(
				(byte) 2), ;
		private byte value;

		private HoursRulerDisplayingPolicy(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, HoursRulerDisplayingPolicy> map = new HashMap<Byte, HoursRulerDisplayingPolicy>();
		private static Map<String, HoursRulerDisplayingPolicy> stringToObjectMap = new HashMap<String, HoursRulerDisplayingPolicy>();
		static {
			for (HoursRulerDisplayingPolicy hoursRulerDisplayingPolicy : HoursRulerDisplayingPolicy
					.values()) {
				HoursRulerDisplayingPolicy.map.put(hoursRulerDisplayingPolicy.value,
						hoursRulerDisplayingPolicy);
				HoursRulerDisplayingPolicy.stringToObjectMap.put(
						hoursRulerDisplayingPolicy.name(), hoursRulerDisplayingPolicy);
			}
		}

		public static HoursRulerDisplayingPolicy fromInt(byte value) {
			HoursRulerDisplayingPolicy hoursRulerDisplayingPolicy = HoursRulerDisplayingPolicy.map
					.get(value);
			return hoursRulerDisplayingPolicy;
		}

		public static HoursRulerDisplayingPolicy fromString(String name) {
			HoursRulerDisplayingPolicy hoursRulerDisplayingPolicy = HoursRulerDisplayingPolicy.stringToObjectMap
					.get(name);
			return hoursRulerDisplayingPolicy;
		}
	}

	@SuppressLint("UseSparseArrays")
	public enum TaskEditMode {
		ADD((byte) 1), QUICK((byte) 2), EDIT((byte) 3), ;
		private byte value;

		private TaskEditMode(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, TaskEditMode> map = new HashMap<Byte, TaskEditMode>();
		private static Map<String, TaskEditMode> stringToObjectMap = new HashMap<String, TaskEditMode>();
		static {
			for (TaskEditMode taskEditMode : TaskEditMode.values()) {
				TaskEditMode.map.put(taskEditMode.value, taskEditMode);
				TaskEditMode.stringToObjectMap.put(taskEditMode.name(), taskEditMode);
			}
		}

		public static TaskEditMode fromInt(byte value) {
			TaskEditMode taskEditMode = TaskEditMode.map.get(value);
			return taskEditMode;
		}

		public static TaskEditMode fromString(String name) {
			TaskEditMode taskEditMode = TaskEditMode.stringToObjectMap.get(name);
			return taskEditMode;
		}
	}

	public enum OnTaskSelectAction {
		OPEN_TASK_IN_EDIT_MODE(
				(short) CommonConstants.ON_TASK_SELECT_ACTION_OPEN_TASK_IN_EDIT_MODE), OPEN_TASK_IN_VIEW_MODE(
				(short) CommonConstants.ON_TASK_SELECT_ACTION_OPEN_TASK_IN_VIEW_MODE);
		private short value;

		private OnTaskSelectAction(short value) {
			this.value = value;
		}

		/** @return the value */
		public short getValue() {
			return value;
		}

		private static Map<Short, OnTaskSelectAction> map = new HashMap<Short, OnTaskSelectAction>();
		static {
			for (OnTaskSelectAction datePickerDate : OnTaskSelectAction.values()) {
				OnTaskSelectAction.map.put(datePickerDate.value, datePickerDate);
			}
		}

		public static OnTaskSelectAction fromInt(short calendarSelectedDate) {
			OnTaskSelectAction datePickerDate = OnTaskSelectAction.map
					.get(calendarSelectedDate);
			return datePickerDate;
		}
	}

	public enum StartTimeRequiredAction {
		LOAD_FROM_SETTINGS(
				(short) CommonConstants.START_TIME_REQUIRED_ACTION_LOAD_FROM_SETTINGS), SET_TO_CURRENT_TIME(
				(short) CommonConstants.START_TIME_REQUIRED_ACTION_SET_TO_CURRENT_TIME), DO_NOT_AUTOMATICALLY_SET(
				(short) CommonConstants.START_TIME_REQUIRED_ACTION_DO_NOT_AUTOMATICALLY_SET), ;
		private short value;

		private StartTimeRequiredAction(short value) {
			this.value = value;
		}

		/** @return the value */
		public short getValue() {
			return value;
		}

		private static Map<Short, StartTimeRequiredAction> map = new HashMap<Short, StartTimeRequiredAction>();
		static {
			for (StartTimeRequiredAction datePickerDate : StartTimeRequiredAction
					.values()) {
				StartTimeRequiredAction.map.put(datePickerDate.value, datePickerDate);
			}
		}

		public static StartTimeRequiredAction fromInt(short calendarSelectedDate) {
			StartTimeRequiredAction datePickerDate = StartTimeRequiredAction.map
					.get(calendarSelectedDate);
			return datePickerDate;
		}
	}

	public enum RecurrenceInterval {
		ONE_TIME((short) CommonConstants.RECURRENCE_INTERVAL_ONE_TIME,
				R.string.toast_text_time_units_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_time_units_count_must_be_less_than_or_equal_to,
				R.string.toast_text_time_units_count_must_be_within_bounds,
				R.string.text_minute, R.string.text_minutes,
				R.integer.time_units_count_default_value,
				R.integer.time_units_count_min_value,
				R.integer.time_units_count_max_value,
				R.string.preference_key_time_units_count,
				R.id.scrollview_occurrences_picker_minutely_recurrent,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.TIME_UNITS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.TIME_UNITS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.TIME_UNITS_COUNT_TO_BE_WITHIN_BOUNDS, WantingItem.OCCURRENCES), MINUTES(
				(short) CommonConstants.RECURRENCE_INTERVAL_MINUTES,
				R.string.toast_text_minutes_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_minutes_count_must_be_less_than_or_equal_to,
				R.string.toast_text_minutes_count_must_be_within_bounds,
				R.string.text_minute, R.string.text_minutes,
				R.integer.time_units_count_minutely_recurrent_default_value,
				R.integer.time_units_count_minutely_recurrent_min_value,
				R.integer.time_units_count_minutely_recurrent_max_value,
				R.string.preference_key_time_units_count_minutely_recurrent,
				R.id.scrollview_occurrences_picker_minutely_recurrent,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.MINUTES_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.MINUTES_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.MINUTES_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_MINUTELY_RECURRENT_TASK), HOURS(
				(short) CommonConstants.RECURRENCE_INTERVAL_HOURS,
				R.string.toast_text_hours_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_hours_count_must_be_less_than_or_equal_to,
				R.string.toast_text_hours_count_must_be_within_bounds,
				R.string.text_hour, R.string.text_hours,
				R.integer.time_units_count_hourly_recurrent_default_value,
				R.integer.time_units_count_hourly_recurrent_min_value,
				R.integer.time_units_count_hourly_recurrent_max_value,
				R.string.preference_key_time_units_count_hourly_recurrent,
				R.id.scrollview_occurrences_picker_hourly_recurrent,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.HOURS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.HOURS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.HOURS_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_HOURLY_RECURRENT_TASK), DAYS(
				(short) CommonConstants.RECURRENCE_INTERVAL_DAYS,
				R.string.toast_text_days_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_days_count_must_be_less_than_or_equal_to,
				R.string.toast_text_days_count_must_be_within_bounds, R.string.text_day,
				R.string.text_days,
				R.integer.time_units_count_daily_recurrent_default_value,
				R.integer.time_units_count_daily_recurrent_min_value,
				R.integer.time_units_count_daily_recurrent_max_value,
				R.string.preference_key_time_units_count_daily_recurrent,
				R.id.scrollview_occurrences_picker_daily_recurrent,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.DAYS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.DAYS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.DAYS_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_DAILY_RECURRENT_TASK), WEEKS(
				(short) CommonConstants.RECURRENCE_INTERVAL_WEEKS,
				R.string.toast_text_weeks_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_weeks_count_must_be_less_than_or_equal_to,
				R.string.toast_text_weeks_count_must_be_within_bounds,
				R.string.text_week, R.string.text_weeks,
				R.integer.time_units_count_weekly_recurrent_default_value,
				R.integer.time_units_count_weekly_recurrent_min_value,
				R.integer.time_units_count_weekly_recurrent_max_value,
				R.string.preference_key_time_units_count_weekly_recurrent,
				R.id.scrollview_week_days_picker, R.id.linearlayout_occurrences_picker,
				new ArrayList<Integer>(),
				WantingItem.WEEKS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.WEEKS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.WEEKS_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_WEEKLY_RECURRENT_TASK), MONTHS_ON_DATE(
				(short) CommonConstants.RECURRENCE_INTERVAL_MONTHS_ON_DATE,
				R.string.toast_text_months_on_date_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_months_on_date_count_must_be_less_than_or_equal_to,
				R.string.toast_text_months_on_date_count_must_be_within_bounds,
				R.string.text_month, R.string.text_months,
				R.integer.time_units_count_monthly_recurrent_on_date_default_value,
				R.integer.time_units_count_monthly_recurrent_on_date_min_value,
				R.integer.time_units_count_monthly_recurrent_on_date_max_value,
				R.string.preference_key_time_units_count_monthly_recurrent_on_date,
				R.id.scrollview_occurrences_picker_monthly_recurrent_on_date,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.MONTHS_ON_DATE_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.MONTHS_ON_DATE_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.MONTHS_ON_DATE_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_TASK_MONTHLY_RECURRENT_ON_DATE), MONTHS_ON_NTH_WEEK_DAY(
				(short) CommonConstants.RECURRENCE_INTERVAL_MONTHS_ON_NTH_WEEK_DAY,
				R.string.toast_text_months_on_nth_week_day_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_months_on_nth_week_day_count_must_be_less_than_or_equal_to,
				R.string.toast_text_months_on_nth_week_day_count_must_be_within_bounds,
				R.string.text_month,
				R.string.text_months,
				R.integer.time_units_count_monthly_recurrent_on_nth_week_day_default_value,
				R.integer.time_units_count_monthly_recurrent_on_nth_week_day_min_value,
				R.integer.time_units_count_monthly_recurrent_on_nth_week_day_max_value,
				R.string.preference_key_time_units_count_monthly_recurrent_on_nth_week_day,
				R.id.linearlayout_occurrences_picker_monthly_recurrent_on_nth_week_day,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.MONTHS_ON_NTH_WEEK_DAY_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.MONTHS_ON_NTH_WEEK_DAY_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.MONTHS_ON_NTH_WEEK_DAY_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_TASK_MONTHLY_RECURRENT_ON_NTH_WEEK_DAY), YEARS(
				(short) CommonConstants.RECURRENCE_INTERVAL_YEARS,
				R.string.toast_text_years_count_must_be_greater_than_or_equal_to,
				R.string.toast_text_years_count_must_be_less_than_or_equal_to,
				R.string.toast_text_years_count_must_be_within_bounds,
				R.string.text_year, R.string.text_years,
				R.integer.time_units_count_yearly_recurrent_default_value,
				R.integer.time_units_count_yearly_recurrent_min_value,
				R.integer.time_units_count_yearly_recurrent_max_value,
				R.string.preference_key_time_units_count_yearly_recurrent,
				R.id.linearlayout_occurrences_picker_yearly_recurrent,
				R.id.linearlayout_occurrences_picker, new ArrayList<Integer>(),
				WantingItem.YEARS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
				WantingItem.YEARS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
				WantingItem.YEARS_COUNT_TO_BE_WITHIN_BOUNDS,
				WantingItem.OCCURRENCES_OF_YEARLY_RECURRENT_TASK);
		private short value;
		private int toastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo;
		private int toastTextIdTimeUnitsCountMustBeLessThanOrEqualTo;
		private int toastTextIdTimeUnitsCountMustBeWithinBounds;
		private int textviewIdTimeUnitTextSingular;
		private int textviewIdTimeUnitTextPlural;
		private int timeUnitsCountDefaultValueId;
		private int timeUnitsCountMinValueId;
		private int timeUnitsCountMaxValueId;
		private int preferenceKeyIdTimeUnitsCount;
		private int scrollviewIdOccurrencesPicker;
		private int linearlayoutIdOccurrencesPicker;
		private int timeUnitsCountDefaultValue;
		private int timeUnitsCountMinValue;
		private int timeUnitsCountMaxValue;
		private int timeUnitsCount;
		private int checkBoxCount;
		private CharSequence textTimeUnitsCount;
		private ValueMustBe timeUnitsCountMustBe;
		private List<Integer> taskOccurrences;
		private WantingItem wantingItemTimeUnitsCountToBeGreaterThanOrEqualTo;
		private WantingItem wantingItemTimeUnitsCountToBeLessThanOrEqualTo;
		private WantingItem wantingItemTimeUnitsCountToBeWithinBounds;
		private WantingItem wantingItemOccurrences;
		private HorizontalScrollView scrollviewOccurrencesPicker;
		private LinearLayout linearlayoutOccurrencesPicker;
		private LinearLayout occurrencesPicker;

		public static void initializeValueBounds(Context context) {
			Resources resources = context.getResources();
			for (RecurrenceInterval recurrenceInterval : RecurrenceInterval.values()) {
				recurrenceInterval.taskOccurrences.clear();
				recurrenceInterval.timeUnitsCountDefaultValue = resources
						.getInteger(recurrenceInterval.timeUnitsCountDefaultValueId);
				recurrenceInterval.timeUnitsCountMinValue = resources
						.getInteger(recurrenceInterval.timeUnitsCountMinValueId);
				recurrenceInterval.timeUnitsCountMaxValue = resources
						.getInteger(recurrenceInterval.timeUnitsCountMaxValueId);
				recurrenceInterval.timeUnitsCount = Helper
						.getIntegerFromStringPreferenceValue(context,
								recurrenceInterval.preferenceKeyIdTimeUnitsCount, null,
								recurrenceInterval.timeUnitsCountDefaultValue,
								recurrenceInterval.timeUnitsCountMinValue,
								recurrenceInterval.timeUnitsCountMaxValue);
				recurrenceInterval.textTimeUnitsCount = String
						.valueOf(recurrenceInterval.timeUnitsCount);
				recurrenceInterval.timeUnitsCountMustBe = null;
			}
		}

		public static void initializeOccurrencesPicker(Activity activity) {
			for (RecurrenceInterval recurrenceInterval : RecurrenceInterval.values()) {
				if (recurrenceInterval == RecurrenceInterval.MINUTES
						|| recurrenceInterval == RecurrenceInterval.HOURS
						|| recurrenceInterval == RecurrenceInterval.DAYS
						|| recurrenceInterval == RecurrenceInterval.WEEKS
						|| recurrenceInterval == RecurrenceInterval.MONTHS_ON_DATE) {
					recurrenceInterval.scrollviewOccurrencesPicker = (HorizontalScrollView) activity
							.findViewById(recurrenceInterval.scrollviewIdOccurrencesPicker);
					recurrenceInterval.occurrencesPicker = (LinearLayout) recurrenceInterval.scrollviewOccurrencesPicker
							.findViewById(recurrenceInterval.linearlayoutIdOccurrencesPicker);
				} else if (recurrenceInterval == RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY
						|| recurrenceInterval == RecurrenceInterval.YEARS) {
					recurrenceInterval.linearlayoutOccurrencesPicker = (LinearLayout) activity
							.findViewById(recurrenceInterval.scrollviewIdOccurrencesPicker);
					recurrenceInterval.occurrencesPicker = (LinearLayout) recurrenceInterval.linearlayoutOccurrencesPicker
							.findViewById(recurrenceInterval.linearlayoutIdOccurrencesPicker);
				}
			}
		}

		private RecurrenceInterval(short value,
				int toastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo,
				int toastTextIdTimeUnitsCountMustBeLessThanOrEqualTo,
				int toastTextIdTimeUnitsCountMustBeWithinBounds,
				int textviewIdTimeUnitTextSingular, int textviewIdTimeUnitTextPlural,
				int timeUnitsCountDefaultValueId, int timeUnitsCountMinValueId,
				int timeUnitsCountMaxValueId, int preferenceKeyIdTimeUnitsCount,
				int scrollviewIdOccurrencesPicker, int linearlayoutIdOccurrencesPicker,
				List<Integer> taskOccurrences,
				WantingItem wantingItemTimeUnitsCountToBeGreaterThanOrEqualTo,
				WantingItem wantingItemTimeUnitsCountToBeLessThanOrEqualTo,
				WantingItem wantingItemTimeUnitsCountToBeWithinBounds,
				WantingItem wantingItemOccurrences) {
			this.value = value;
			this.toastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo = toastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo;
			this.toastTextIdTimeUnitsCountMustBeLessThanOrEqualTo = toastTextIdTimeUnitsCountMustBeLessThanOrEqualTo;
			this.toastTextIdTimeUnitsCountMustBeWithinBounds = toastTextIdTimeUnitsCountMustBeWithinBounds;
			this.textviewIdTimeUnitTextSingular = textviewIdTimeUnitTextSingular;
			this.textviewIdTimeUnitTextPlural = textviewIdTimeUnitTextPlural;
			this.timeUnitsCountDefaultValueId = timeUnitsCountDefaultValueId;
			this.timeUnitsCountMinValueId = timeUnitsCountMinValueId;
			this.timeUnitsCountMaxValueId = timeUnitsCountMaxValueId;
			this.preferenceKeyIdTimeUnitsCount = preferenceKeyIdTimeUnitsCount;
			this.scrollviewIdOccurrencesPicker = scrollviewIdOccurrencesPicker;
			this.linearlayoutIdOccurrencesPicker = linearlayoutIdOccurrencesPicker;
			this.taskOccurrences = taskOccurrences;
			this.wantingItemTimeUnitsCountToBeGreaterThanOrEqualTo = wantingItemTimeUnitsCountToBeGreaterThanOrEqualTo;
			this.wantingItemTimeUnitsCountToBeLessThanOrEqualTo = wantingItemTimeUnitsCountToBeLessThanOrEqualTo;
			this.wantingItemTimeUnitsCountToBeWithinBounds = wantingItemTimeUnitsCountToBeWithinBounds;
			this.wantingItemOccurrences = wantingItemOccurrences;
		}

		public short getValue() {
			return value;
		}

		public int getToastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo() {
			return toastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo;
		}

		public int getToastTextIdTimeUnitsCountMustBeLessThanOrEqualTo() {
			return toastTextIdTimeUnitsCountMustBeLessThanOrEqualTo;
		}

		public int getToastTextIdTimeUnitsCountMustBeWithinBounds() {
			return toastTextIdTimeUnitsCountMustBeWithinBounds;
		}

		public int getTextviewIdTimeUnitTextSingular() {
			return textviewIdTimeUnitTextSingular;
		}

		public int getTextviewIdTimeUnitTextPlural() {
			return textviewIdTimeUnitTextPlural;
		}

		public int getTimeUnitsCountDefaultValue() {
			return timeUnitsCountDefaultValue;
		}

		public int getTimeUnitsCountMinValue() {
			return timeUnitsCountMinValue;
		}

		public int getTimeUnitsCountMaxValue() {
			return timeUnitsCountMaxValue;
		}

		public int getTimeUnitsCount() {
			return timeUnitsCount;
		}

		public void setTimeUnitsCount(int timeUnitsCount) {
			this.timeUnitsCount = timeUnitsCount;
		}

		public int getCheckBoxCount() {
			return checkBoxCount;
		}

		public void setCheckBoxCount(int checkBoxCount) {
			this.checkBoxCount = checkBoxCount;
		}

		public CharSequence getTextTimeUnitsCount() {
			return textTimeUnitsCount;
		}

		public void setTextTimeUnitsCount(CharSequence textTimeUnitsCount) {
			this.textTimeUnitsCount = textTimeUnitsCount;
		}

		public ValueMustBe getTimeUnitsCountMustBe() {
			return timeUnitsCountMustBe;
		}

		public void setTimeUnitsCountMustBe(ValueMustBe timeUnitsCountMustBe) {
			this.timeUnitsCountMustBe = timeUnitsCountMustBe;
		}

		public List<Integer> getTaskOccurrences() {
			return taskOccurrences;
		}

		public void setTaskOccurrences(List<Integer> taskOccurrences) {
			this.taskOccurrences = taskOccurrences;
		}

		public WantingItem getWantingItemTimeUnitsCountToBeGreaterThanOrEqualTo() {
			return wantingItemTimeUnitsCountToBeGreaterThanOrEqualTo;
		}

		public WantingItem getWantingItemTimeUnitsCountToBeLessThanOrEqualTo() {
			return wantingItemTimeUnitsCountToBeLessThanOrEqualTo;
		}

		public WantingItem getWantingItemTimeUnitsCountToBeWithinBounds() {
			return wantingItemTimeUnitsCountToBeWithinBounds;
		}

		public WantingItem getWantingItemOccurrences() {
			return wantingItemOccurrences;
		}

		public HorizontalScrollView getScrollviewOccurrencesPicker() {
			return scrollviewOccurrencesPicker;
		}

		public LinearLayout getOccurrencesPicker() {
			return occurrencesPicker;
		}

		private static Map<Short, RecurrenceInterval> map = new HashMap<Short, RecurrenceInterval>();
		// private static Map<RecurrenceInterval, ValueBounds<Integer>>
		// recurrenceIntervalToValueBoundsMap = new HashMap<RecurrenceInterval,
		// ValueBounds<Integer>>();
		private static Map<String, RecurrenceInterval> stringToObjectMap = new HashMap<String, RecurrenceInterval>();
		private static Map<Short, RecurrenceInterval> timeUnitToRecurrenceIntervalMap = new HashMap<Short, RecurrenceInterval>();
		static {
			for (RecurrenceInterval recurrenceInterval : RecurrenceInterval.values()) {
				RecurrenceInterval.map.put(recurrenceInterval.value, recurrenceInterval);
				RecurrenceInterval.stringToObjectMap.put(recurrenceInterval.name(),
						recurrenceInterval);
			}
			for (MonthRecurrenceMode monthRecurrenceMode : MonthRecurrenceMode.values()) {
				for (TimeUnit timeUnit : TimeUnit.values()) {
					short key = (short) (2 + timeUnit.value + (monthRecurrenceMode.value << 8));
					RecurrenceInterval recurrenceInterval = RecurrenceInterval
							.fromTimeUnitInternal(timeUnit, monthRecurrenceMode);
					RecurrenceInterval.timeUnitToRecurrenceIntervalMap.put(key,
							recurrenceInterval);
				}
			}
		}

		public static RecurrenceInterval fromInt(short value) {
			RecurrenceInterval recurrenceInterval = RecurrenceInterval.map.get(value);
			return recurrenceInterval;
		}

		public static RecurrenceInterval fromTimeUnit(short timeUnitValue,
				byte monthRecurrenceModeValue) {
			short key = (short) (2 + timeUnitValue + (monthRecurrenceModeValue << 8));
			RecurrenceInterval recurrenceInterval = RecurrenceInterval.timeUnitToRecurrenceIntervalMap
					.get(key);
			return recurrenceInterval;
		}

		public static RecurrenceInterval fromTimeUnit(TimeUnit timeUnit,
				MonthRecurrenceMode monthRecurrenceMode) {
			RecurrenceInterval recurrenceInterval = RecurrenceInterval.fromTimeUnit(
					timeUnit.value, monthRecurrenceMode.value);
			return recurrenceInterval;
		}

		public static RecurrenceInterval fromTimeUnitInternal(TimeUnit timeUnit,
				MonthRecurrenceMode monthRecurrenceMode) {
			switch (timeUnit) {
			case ONE_TIME:
				return ONE_TIME;
			case MINUTE:
				return MINUTES;
			case HOUR:
				return HOURS;
			case DAY:
				return DAYS;
			case WEEK:
				return WEEKS;
			case MONTH:
				switch (monthRecurrenceMode) {
				case RECURRENT_ON_DATE:
					return MONTHS_ON_DATE;
				case RECURRENT_ON_NTH_WEEK_DAY:
					return MONTHS_ON_NTH_WEEK_DAY;
				default:
					return null;
				}
			case YEAR:
				return YEARS;
			default:
				return null;
			}
		}

		public static RecurrenceInterval fromString(String str) {
			RecurrenceInterval recurrenceInterval = RecurrenceInterval.stringToObjectMap
					.get(str);
			return recurrenceInterval;
		}
	}

	@SuppressLint("UseSparseArrays")
	public enum MonthRecurrenceMode {
		RECURRENT_ON_DATE((byte) 1,
				R.id.fragment_edit_task_part_main_radio_monthly_recurrent_on_date), RECURRENT_ON_NTH_WEEK_DAY(
				(byte) 2,
				R.id.fragment_edit_task_part_main_radio_monthly_recurrent_on_nth_week_day);
		private byte value;
		private int radioButtonId;

		private MonthRecurrenceMode(byte value, int radioButtonId) {
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
		private static Map<Byte, MonthRecurrenceMode> map = new HashMap<Byte, MonthRecurrenceMode>();
		@SuppressLint("UseSparseArrays")
		private static Map<Integer, MonthRecurrenceMode> radioButtonIdToMonthRecurrenceModeMap = new HashMap<Integer, MonthRecurrenceMode>();
		private static Map<RecurrenceInterval, MonthRecurrenceMode> recurrenceIntervalToMonthRecurrenceModeMap = new HashMap<RecurrenceInterval, MonthRecurrenceMode>();
		static {
			for (MonthRecurrenceMode monthRecurrenceMode : MonthRecurrenceMode.values()) {
				MonthRecurrenceMode.map.put(monthRecurrenceMode.value,
						monthRecurrenceMode);
				MonthRecurrenceMode.radioButtonIdToMonthRecurrenceModeMap.put(
						monthRecurrenceMode.radioButtonId, monthRecurrenceMode);
			}
			for (RecurrenceInterval recurrenceInterval : RecurrenceInterval.values()) {
				MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode
						.fromRecurrenceIntervalInternal(recurrenceInterval);
				MonthRecurrenceMode.recurrenceIntervalToMonthRecurrenceModeMap.put(
						recurrenceInterval, monthRecurrenceMode);
			}
		}

		public static void initializeRecurrenceIntervalToMonthRecurrenceModeMap(
				Context context) {
			MonthRecurrenceMode monthRecurrenceModeFromPreference = MonthRecurrenceMode
					.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(
							context, R.string.preference_key_month_recurrence_mode,
							R.array.month_recurrence_mode_values_array,
							R.integer.month_recurrence_mode_default_value));
			for (RecurrenceInterval recurrenceInterval : RecurrenceInterval.values()) {
				MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode
						.fromRecurrenceIntervalInternal(recurrenceInterval);
				if (monthRecurrenceMode == null) {
					MonthRecurrenceMode.recurrenceIntervalToMonthRecurrenceModeMap.put(
							recurrenceInterval, monthRecurrenceModeFromPreference);
				} else {
					MonthRecurrenceMode.recurrenceIntervalToMonthRecurrenceModeMap.put(
							recurrenceInterval, monthRecurrenceMode);
				}
			}
		}

		public static MonthRecurrenceMode fromInt(byte value) {
			MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode.map.get(value);
			return monthRecurrenceMode;
		}

		private static MonthRecurrenceMode fromRecurrenceIntervalInternal(
				RecurrenceInterval recurrenceInterval) {
			switch (recurrenceInterval) {
			case MONTHS_ON_DATE:
				return RECURRENT_ON_DATE;
			case MONTHS_ON_NTH_WEEK_DAY:
				return RECURRENT_ON_NTH_WEEK_DAY;
			default:
				return null;
			}
		}

		public static MonthRecurrenceMode fromRecurrenceInterval(RecurrenceInterval value) {
			MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode.recurrenceIntervalToMonthRecurrenceModeMap
					.get(value);
			return monthRecurrenceMode;
		}

		public static MonthRecurrenceMode fromRadioGroup(RadioGroup radioGroup) {
			MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode
					.fromRadioButtonId(radioGroup.getCheckedRadioButtonId());
			if (monthRecurrenceMode == null) {
				int monthRecurrenceModeValue = Helper
						.getIntegerPreferenceValueFromStringArray(
								radioGroup.getContext(),
								R.string.preference_key_month_recurrence_mode,
								R.array.month_recurrence_mode_values_array,
								R.integer.month_recurrence_mode_default_value);
				monthRecurrenceMode = MonthRecurrenceMode
						.fromInt((byte) monthRecurrenceModeValue);
				RadioButton radioButton = (RadioButton) radioGroup
						.findViewById(monthRecurrenceMode.radioButtonId);
				radioButton.setChecked(true);
			}
			return monthRecurrenceMode;
		}

		public static MonthRecurrenceMode fromRadioButtonId(int radioButtonId) {
			MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode.radioButtonIdToMonthRecurrenceModeMap
					.get(radioButtonId);
			return monthRecurrenceMode;
		}
	}

	@SuppressLint("UseSparseArrays")
	public enum SyncPolicy {
		DO_SYNC((byte) 1), DO_NOT_SYNC((byte) 2), ;
		private byte value;

		private SyncPolicy(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, SyncPolicy> map = new HashMap<Byte, SyncPolicy>();
		private static Map<String, SyncPolicy> stringToObjectMap = new HashMap<String, SyncPolicy>();
		static {
			for (SyncPolicy syncPolicy : SyncPolicy.values()) {
				SyncPolicy.map.put(syncPolicy.value, syncPolicy);
				SyncPolicy.stringToObjectMap.put(syncPolicy.name(), syncPolicy);
			}
		}

		public static SyncPolicy fromInt(byte value) {
			SyncPolicy syncPolicy = SyncPolicy.map.get(value);
			return syncPolicy;
		}

		public static SyncPolicy fromString(String name) {
			SyncPolicy syncPolicy = SyncPolicy.stringToObjectMap.get(name);
			return syncPolicy;
		}
	}

	@SuppressLint("UseSparseArrays")
	public enum MarkSyncNeededPolicy {
		ALWAYS((byte) 1), IF_SYNC_IS_SWITCHED_ON((byte) 2), NEVER((byte) 3), ;
		private byte value;

		private MarkSyncNeededPolicy(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

		@SuppressLint("UseSparseArrays")
		private static Map<Byte, MarkSyncNeededPolicy> map = new HashMap<Byte, MarkSyncNeededPolicy>();
		private static Map<String, MarkSyncNeededPolicy> stringToObjectMap = new HashMap<String, MarkSyncNeededPolicy>();
		static {
			for (MarkSyncNeededPolicy markSyncNeeded : MarkSyncNeededPolicy.values()) {
				MarkSyncNeededPolicy.map.put(markSyncNeeded.value, markSyncNeeded);
				MarkSyncNeededPolicy.stringToObjectMap.put(markSyncNeeded.name(),
						markSyncNeeded);
			}
		}

		public static MarkSyncNeededPolicy fromInt(byte value) {
			MarkSyncNeededPolicy markSyncNeeded = MarkSyncNeededPolicy.map.get(value);
			return markSyncNeeded;
		}

		public static MarkSyncNeededPolicy fromString(String name) {
			MarkSyncNeededPolicy markSyncNeeded = MarkSyncNeededPolicy.stringToObjectMap
					.get(name);
			return markSyncNeeded;
		}
	}

	public enum SyncStatus {
		SYNCHRONIZED((byte) 1), SYNC_UP_REQUIRED((byte) 2), SYNC_DOWN_REQUIRED((byte) 4);
		private byte value;

		private SyncStatus(byte value) {
			this.value = value;
		}

		/** @return the value */
		public byte getValue() {
			return value;
		}

		public static SyncStatus fromInt(int x) {
			switch (x) {
			case 1:
				return SYNCHRONIZED;
			case 2:
				return SYNC_UP_REQUIRED;
			case 4:
				return SYNC_DOWN_REQUIRED;
			default:
				return null;
			}
		}
	}

	// public enum PrePostBusinessHoursTaskOccurrenceDistributionMode {
	// SHOW_PRE_POST_AND_BUSINESS_HOURS_SEPARATELY((byte) 1),
	// SHOW_PRE_AND_POST_IN_BUSINESS_HOURS(
	// (byte) 2), DO_NOT_SHOW_PRE_AND_POST_BUSINESS_HOURS((byte) 3), ;
	// private byte value;
	//
	// private PrePostBusinessHoursTaskOccurrenceDistributionMode(byte value) {
	// this.value = value;
	// }
	//
	// public byte getValue() {
	// return value;
	// }
	//
	// private static Map<Byte, TaskEditMode> map = new HashMap<Byte, TaskEditMode>();
	// private static Map<String, TaskEditMode> stringToObjectMap = new HashMap<String,
	// TaskEditMode>();
	// static {
	// for (TaskEditMode taskEditMode : TaskEditMode.values()) {
	// TaskEditMode.map.put(taskEditMode.value, taskEditMode);
	// TaskEditMode.stringToObjectMap.put(taskEditMode.name(), taskEditMode);
	// }
	// }
	//
	// public static TaskEditMode fromInt(byte value) {
	// TaskEditMode taskEditMode = TaskEditMode.map.get(value);
	// return taskEditMode;
	// }
	//
	// public static TaskEditMode fromString(String name) {
	// TaskEditMode taskEditMode = TaskEditMode.stringToObjectMap.get(name);
	// return taskEditMode;
	// }
	// }
	class TaskOccurrenceComparator implements Comparator<Integer> {
		@Override
		public int compare(Integer lhs, Integer rhs) {
			if (lhs < rhs) {
				return -1;
			} else if (lhs == rhs) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	class TaskOccurrenceComparatorWeek implements Comparator<Integer> {
		private int mFirstDayOfWeek;

		public TaskOccurrenceComparatorWeek(int firstDayOfWeek) {
			mFirstDayOfWeek = firstDayOfWeek;
		}

		@Override
		public int compare(Integer lhs, Integer rhs) {
			int lhsValue = lhs;
			int rhsValue = rhs;
			if (lhsValue < mFirstDayOfWeek && rhsValue < mFirstDayOfWeek
					|| lhsValue >= mFirstDayOfWeek && rhsValue >= mFirstDayOfWeek) {
				if (lhsValue < rhsValue) {
					return -1;
				} else if (lhsValue == rhsValue) {
					return 0;
				} else {
					return 1;
				}
			} else if (lhsValue < mFirstDayOfWeek && rhsValue >= mFirstDayOfWeek) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	public TaskUiData2(Long id, Long parentId, String name, Integer color,
			Long startDateTime, Long endDateTime, boolean isCompleted,
			short percentOfCompletion, boolean deleted, short priority, int sortOrder,
			String description, String location, short recurrenceIntervalValue,
			Integer timeUnitsCount, Integer occurrencesMaxCount,
			Long repetitionEndDateTime, String alarmRingtone,
			String notificationRingtone, Long ringtoneFadeInTime, Integer playingTime,
			Integer automaticSnoozeDuration, Integer automaticSnoozesMaxCount,
			Boolean vibrate, String vibratePattern, Boolean led, String ledPattern,
			Integer ledColor, List<Integer> taskOccurrenceList,
			List<ReminderUiData2> reminderList) {
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.color = color;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.isCompleted = isCompleted;
		this.percentOfCompletion = percentOfCompletion;
		this.deleted = deleted;
		this.priority = priority;
		this.sortOrder = sortOrder;
		this.description = description;
		this.location = location;
		this.recurrenceIntervalValue = recurrenceIntervalValue;
		this.timeUnitsCount = timeUnitsCount;
		this.occurrencesMaxCount = occurrencesMaxCount;
		this.repetitionEndDateTime = repetitionEndDateTime;
		this.alarmRingtone = alarmRingtone;
		this.notificationRingtone = notificationRingtone;
		this.ringtoneFadeInTime = ringtoneFadeInTime;
		this.playingTime = playingTime;
		this.automaticSnoozeDuration = automaticSnoozeDuration;
		this.automaticSnoozesMaxCount = automaticSnoozesMaxCount;
		this.vibrate = vibrate;
		this.vibratePattern = vibratePattern;
		this.led = led;
		this.ledPattern = ledPattern;
		this.ledColor = ledColor;
		this.taskOccurrenceList = taskOccurrenceList;
		this.reminderList = reminderList;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Long calendarId) {
		this.calendarId = calendarId;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getColor() {
		return color;
	}

	public void setColor(Integer color) {
		this.color = color;
	}

	public Long getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(Long startDateTime) {
		this.startDateTime = startDateTime;
	}

	public Long getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Long endDateTime) {
		this.endDateTime = endDateTime;
	}

	public boolean getIsCompleted() {
		return isCompleted;
	}

	public void setIsCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public short getPercentOfCompletion() {
		return percentOfCompletion;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void setPercentOfCompletion(short percentOfCompletion) {
		this.percentOfCompletion = percentOfCompletion;
	}

	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public short getRecurrenceIntervalValue() {
		return recurrenceIntervalValue;
	}

	public void setRecurrenceIntervalValue(short recurrenceIntervalValue) {
		this.recurrenceIntervalValue = recurrenceIntervalValue;
	}

	public Integer getTimeUnitsCount() {
		return timeUnitsCount;
	}

	public void setTimeUnitsCount(Integer timeUnitsCount) {
		this.timeUnitsCount = timeUnitsCount;
	}

	public Integer getOccurrencesMaxCount() {
		return occurrencesMaxCount;
	}

	public void setOccurrencesMaxCount(Integer occurrencesMaxCount) {
		this.occurrencesMaxCount = occurrencesMaxCount;
	}

	public Long getRepetitionEndDateTime() {
		return repetitionEndDateTime;
	}

	public void setRepetitionEndDateTime(Long repetitionEndDateTime) {
		this.repetitionEndDateTime = repetitionEndDateTime;
	}

	public String getAlarmRingtone() {
		return alarmRingtone;
	}

	public void setAlarmRingtone(String alarmRingtone) {
		this.alarmRingtone = alarmRingtone;
	}

	public String getNotificationRingtone() {
		return notificationRingtone;
	}

	public void setNotificationRingtone(String notificationRingtone) {
		this.notificationRingtone = notificationRingtone;
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

	public List<Integer> getTaskOccurrenceList() {
		return taskOccurrenceList;
	}

	public List<ReminderUiData2> getReminderList() {
		return reminderList;
	}

	public TaskUiData2(TaskUiData2 task) {
		this(task.id, task.parentId, task.name, task.color, task.startDateTime,
				task.endDateTime, task.isCompleted, task.percentOfCompletion,
				task.deleted, task.priority, task.sortOrder, task.description,
				task.location, task.recurrenceIntervalValue, task.timeUnitsCount,
				task.occurrencesMaxCount, task.repetitionEndDateTime, task.alarmRingtone,
				task.notificationRingtone, task.ringtoneFadeInTime, task.playingTime,
				task.automaticSnoozeDuration, task.automaticSnoozesMaxCount,
				task.vibrate, task.vibratePattern, task.led, task.ledPattern,
				task.ledColor, task.taskOccurrenceList, task.reminderList);
	}

	public String getRingtone(int preferenceId) {
		return preferenceId == R.string.preference_key_alarm_ringtone ? alarmRingtone
				: notificationRingtone;
	}

	public String getRingtone2(Context context, int preferenceId) {
		switch (preferenceId) {
		case R.string.preference_key_alarm_ringtone:
			if (alarmRingtone != null) {
				return alarmRingtone;
			}
			break;
		case R.string.preference_key_notification_ringtone:
			if (notificationRingtone != null) {
				return notificationRingtone;
			}
			break;
		default:
			break;
		}
		return Helper
				.getStringPreferenceValue(
						context,
						preferenceId,
						RingtoneManager
								.getDefaultUri(
										preferenceId == R.string.preference_key_alarm_ringtone ? RingtoneManager.TYPE_ALARM
												: RingtoneManager.TYPE_NOTIFICATION)
								.toString());
	}

	public String getAlarmRingtone2(Context context) {
		if (alarmRingtone != null) {
			return alarmRingtone;
		} else {
			return Helper.getStringPreferenceValue(context, context.getResources()
					.getString(R.string.preference_key_alarm_ringtone), RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM).toString());
		}
	}

	public String getRingtoneTitle(Context context, int preferenceId) {
		String ringtoneString = getRingtone2(context, preferenceId);
		return Helper.getRingtoneTitle(context, ringtoneString);
	}

	public String getAlarmRingtoneTitle(Context context) {
		String ringtoneString = getAlarmRingtone2(context);
		return Helper.getRingtoneTitle(context, ringtoneString);
	}

	public String getNotificationRingtone2(Context context) {
		if (notificationRingtone != null) {
			return notificationRingtone;
		} else {
			return Helper.getStringPreferenceValue(context, context.getResources()
					.getString(R.string.preference_key_notification_ringtone),
					RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
							.toString());
		}
	}

	public String getNotificationRingtoneTitle(Context context) {
		String ringtoneString = getNotificationRingtone2(context);
		return Helper.getRingtoneTitle(context, ringtoneString);
	}

	public long getRingtoneFadeInTime2(Context context, int preferenceKey,
			long defaultValue, Long minValue, Long maxValue) {
		if (ringtoneFadeInTime != null) {
			return ringtoneFadeInTime;
		}
		return Helper.getLongFromStringPreferenceValue(context, preferenceKey,
				defaultValue, minValue, maxValue);
	}

	public int getPlayingTime2(Context context, int preferenceKey, int defaultValue,
			Integer minValue, Integer maxValue) {
		if (playingTime != null) {
			return playingTime;
		}
		return Helper.getIntegerFromStringPreferenceValue(context, preferenceKey, null,
				defaultValue, minValue, maxValue);
	}

	public int getAutomaticSnoozeDuration2(Context context, int preferenceKey,
			int defaultValue, Integer minValue, Integer maxValue) {
		if (automaticSnoozeDuration != null) {
			return automaticSnoozeDuration;
		}
		return Helper.getIntegerFromStringPreferenceValue(context, preferenceKey, null,
				defaultValue, minValue, maxValue);
	}

	public int getAutomaticSnoozesMaxCount2(Context context, int preferenceKey,
			int defaultValue, Integer minValue, Integer maxValue) {
		if (automaticSnoozesMaxCount != null) {
			return automaticSnoozesMaxCount;
		}
		return Helper.getIntegerFromStringPreferenceValue(context, preferenceKey, null,
				defaultValue, minValue, maxValue);
	}

	public boolean getVibrate2(Context context, String preferenceKey, boolean defaultValue) {
		if (vibrate != null) {
			return vibrate;
		}
		return Helper.getBooleanPreferenceValue(context, preferenceKey, defaultValue);
	}

	public Integer getColor2(Context context) {
		if (color != null) {
			return color;
		} else {
			Resources resources = context.getResources();
			switch (PRIORITY.fromInt(priority)) {
			case HIGH:
				return resources.getColor(R.color.task_high_priority_default_color);
			case LOW:
				return resources.getColor(R.color.task_low_priority_default_color);
			case MEDIUM:
			default:
				int color = Helper.getIntegerPreferenceValue(context,
						R.string.preference_key_task_unset_color,
						resources.getColor(R.color.task_unset_color_default_value),
						null, null);
				return color;
			}
		}
	}

	public TaskUiData2() {
	}

	public TaskUiData2(TaskWithDependents taskWithDependents) {
		Task task = taskWithDependents.task;
		id = task.getId();
		parentId = task.getParentId();
		name = task.getName();
		color = task.getColor();
		startDateTime = task.getStartDateTime();
		endDateTime = task.getEndDateTime();
		isCompleted = task.getIsCompleted();
		percentOfCompletion = task.getPercentOfCompletion();
		deleted = task.getDeleted();
		priority = task.getPriority();
		sortOrder = task.getSortOrder();
		description = task.getDescription();
		location = task.getLocation();
		recurrenceIntervalValue = task.getRecurrenceIntervalValue();
		timeUnitsCount = task.getTimeUnitsCount();
		occurrencesMaxCount = task.getOccurrencesMaxCount();
		repetitionEndDateTime = task.getRepetitionEndDateTime();
		alarmRingtone = task.getAlarmRingtone();
		notificationRingtone = task.getNotificationRingtone();
		ringtoneFadeInTime = task.getRingtoneFadeInTime();
		playingTime = task.getPlayingTime();
		automaticSnoozeDuration = task.getAutomaticSnoozeDuration();
		automaticSnoozesMaxCount = task.getAutomaticSnoozesMaxCount();
		vibrate = task.getVibrate();
		vibratePattern = task.getVibratePattern();
		led = task.getLed();
		ledPattern = task.getLedPattern();
		ledColor = task.getLedColor();
		ArrayList<Integer> taskOccurrenceList1 = new ArrayList<Integer>();
		for (TaskOccurrence taskOccurrence : taskWithDependents.taskOccurrences) {
			Integer newTaskOccurrence = taskOccurrence.getOrdinalNumber();
			taskOccurrenceList1.add(newTaskOccurrence);
		}
		taskOccurrenceList = taskOccurrenceList1;
		ArrayList<ReminderUiData2> reminderUiData2List = new ArrayList<ReminderUiData2>();
		for (Reminder reminder : taskWithDependents.reminders) {
			ReminderUiData2 newReminderUiData = new ReminderUiData2(reminder.getId(),
					this, reminder.getReminderDateTime(),
					reminder.getReminderTimeModeValue(), reminder.getText(),
					reminder.getEnabled(), reminder.getIsAlarm(), reminder.getRingtone(),
					reminder.getRingtoneFadeInTime(), reminder.getPlayingTime(),
					reminder.getAutomaticSnoozeDuration(),
					reminder.getAutomaticSnoozesMaxCount(), reminder.getVibrate(),
					reminder.getVibratePattern(), reminder.getLed(),
					reminder.getLedPattern(), reminder.getLedColor());
			reminderUiData2List.add(newReminderUiData);
		}
		reminderList = reminderUiData2List;
	}

	public String getTaskTitleForQuickReminder() {
		return taskTitleForQuickReminder;
	}

	public Long getNearestOccurrenceStartDateTime(long controlDateTime,
			boolean includeEventsMatchingControlDateTime, boolean searchPrevious,
			int firstDayOfWeek) {
		switch (RecurrenceInterval.fromInt(recurrenceIntervalValue)) {
		case ONE_TIME:
			if (searchPrevious) {
				return getPreviousOccurrenceStartDateTimeOfOneTimeTask(controlDateTime,
						includeEventsMatchingControlDateTime);
			} else {
				return getNextOccurrenceStartDateTimeOfOneTimeTask(controlDateTime,
						includeEventsMatchingControlDateTime);
			}
		case DAYS:
		case WEEKS:
		case HOURS:
		case MINUTES:
		case MONTHS_ON_DATE:
		case MONTHS_ON_NTH_WEEK_DAY:
		case YEARS:
		default:
			Long borderStartDateTime = null;
			Long borderEndDateTime = null;
			if (searchPrevious) {
				borderEndDateTime = controlDateTime;
			} else {
				borderStartDateTime = controlDateTime;
			}
			List<CalendarViewTaskOccurrence2> calendarViewTaskOccurrences = selectTaskOccurrencesOfRecurrentTask(
					borderStartDateTime, borderEndDateTime,
					includeEventsMatchingControlDateTime, null, firstDayOfWeek);
			if (calendarViewTaskOccurrences.size() == 0) {
				return null;
			} else {
				return calendarViewTaskOccurrences.get(0).StartDateTime;
			}
		}
	}

	private List<CalendarViewTaskOccurrence2> selectTaskOccurrencesOfRecurrentTask(
			Long borderStartDateTime, Long borderEndDateTime,
			boolean includeEventsMatchingControlDateTime,
			HashSet<CalendarViewTaskOccurrence2> cacheOfCalendarViewTaskOccurrences,
			int firstDayOfWeek) {
		if (borderStartDateTime == null && borderEndDateTime == null) {
			throw new IllegalArgumentException(
					"Both borderStartDateTime1 and borderEndDateTime1 cannot be null");
		}
		int selectMode = 0;
		if (borderStartDateTime == null) {
			selectMode = -1;
		} else if (borderEndDateTime == null) {
			selectMode = 1;
		}
		List<CalendarViewTaskOccurrence2> result = new ArrayList<CalendarViewTaskOccurrence2>();
		Long previousOccurrenceStartDateTime = null;
		List<Integer> taskOccurrences = getTaskOccurrenceList();
		RecurrenceInterval recurrenceInterval = RecurrenceInterval
				.fromInt(recurrenceIntervalValue);
		int count;
		if (recurrenceInterval.equals(RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY)) {
			count = taskOccurrences.size() - 1;
		} else {
			count = taskOccurrences.size();
		}
		if (count <= 0) {
			return result;
		}
		Long controlDateTime = null;
		if (selectMode == -1) {
			controlDateTime = borderEndDateTime;
		} else if (selectMode == 1) {
			controlDateTime = borderStartDateTime;
		}
		Calendar startDateTimeCalendar = Calendar.getInstance();
		startDateTimeCalendar.setTimeInMillis(startDateTime);
		int startDateTimeMillisOfDay = CalendarHelper
				.getMillisOfDay(startDateTimeCalendar);
		Calendar currentPeriodBeginning = (Calendar) startDateTimeCalendar.clone();
		if (recurrenceInterval.equals(RecurrenceInterval.WEEKS)) {
			CalendarHelper.toBeginningOfWeek(currentPeriodBeginning, firstDayOfWeek);
			currentPeriodBeginning.add(Calendar.MILLISECOND, startDateTimeMillisOfDay);
		} else if (recurrenceInterval.equals(RecurrenceInterval.MONTHS_ON_DATE)) {
			currentPeriodBeginning.set(Calendar.DAY_OF_MONTH, 1);
		} else if (recurrenceInterval.equals(RecurrenceInterval.YEARS)) {
			currentPeriodBeginning.set(Calendar.DAY_OF_YEAR, 1);
		}
		List<Calendar> taskOccurrencesStartDateTimes = new ArrayList<Calendar>(count);
		int month;
		int year;
		int weekCode = taskOccurrences.get(taskOccurrences.size() - 1);
		biz.advancedcalendar.calendar2.Period occurrencePeriod = null;
		if (startDateTime != null && endDateTime != null) {
			Calendar endDateTimeCalendar = Calendar.getInstance();
			endDateTimeCalendar.setTimeInMillis(endDateTime);
			occurrencePeriod = new biz.advancedcalendar.calendar2.Period(
					startDateTimeCalendar, endDateTimeCalendar);
		}
		//
		if (!recurrenceInterval.equals(RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY)) {
			if (recurrenceInterval.equals(RecurrenceInterval.WEEKS)) {
				Collections.sort(taskOccurrences, new TaskOccurrenceComparatorWeek(
						firstDayOfWeek));
			} else {
				Collections.sort(taskOccurrences, new TaskOccurrenceComparator());
			}
		}
		int currentTaskOccurrencesCount = 0;
		boolean isDayFebruary28Checked = false;
		boolean isDayFebruary29Checked = false;
		boolean isDayFebruaryLastChecked = false;
		if (recurrenceInterval.equals(RecurrenceInterval.YEARS)) {
			for (Integer taskOccurrence : taskOccurrences) {
				if (taskOccurrence == 59) {
					isDayFebruary28Checked = true;
				}
				if (taskOccurrence == 60) {
					isDayFebruary29Checked = true;
				}
				if (taskOccurrence == 61) {
					isDayFebruaryLastChecked = true;
				}
			}
		}
		int countNotIn_28_29_last = 0;
		if (recurrenceInterval.equals(RecurrenceInterval.YEARS)) {
			for (Integer taskOccurrence : taskOccurrences) {
				int dayCode = taskOccurrence;
				if (dayCode < 59 || dayCode > 61) {
					countNotIn_28_29_last++;
				}
			}
		}
		Iterator<CalendarViewTaskOccurrence2> iterator = null;
		if (cacheOfCalendarViewTaskOccurrences != null) {
			iterator = cacheOfCalendarViewTaskOccurrences.iterator();
		}
		while (true) {
			taskOccurrencesStartDateTimes.clear();
			Calendar supposedTaskOccurrenceStartDateTime;
			switch (recurrenceInterval) {
			case MONTHS_ON_NTH_WEEK_DAY:
				// we get here zero-based month (zero-based month is used in function
				// getNthOfMonth())
				month = currentPeriodBeginning.get(Calendar.MONTH);
				year = currentPeriodBeginning.get(Calendar.YEAR);
				for (int i = 0; i < count; i++) {
					supposedTaskOccurrenceStartDateTime = getNthOfMonth(weekCode,
							taskOccurrences.get(i), month, year);
					supposedTaskOccurrenceStartDateTime.add(Calendar.MILLISECOND,
							startDateTimeMillisOfDay);
					if (startDateTime <= supposedTaskOccurrenceStartDateTime
							.getTimeInMillis()) {
						taskOccurrencesStartDateTimes
								.add(supposedTaskOccurrenceStartDateTime);
					}
				}
				break;
			case MINUTES:
				for (Integer taskOccurrence : taskOccurrences) {
					supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
							.clone();
					supposedTaskOccurrenceStartDateTime.add(Calendar.MINUTE,
							taskOccurrence - 1);
					if (startDateTime <= supposedTaskOccurrenceStartDateTime
							.getTimeInMillis()) {
						taskOccurrencesStartDateTimes
								.add(supposedTaskOccurrenceStartDateTime);
					}
				}
				break;
			case HOURS:
				for (Integer taskOccurrence : taskOccurrences) {
					supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
							.clone();
					supposedTaskOccurrenceStartDateTime.add(Calendar.HOUR_OF_DAY,
							taskOccurrence - 1);
					if (startDateTime <= supposedTaskOccurrenceStartDateTime
							.getTimeInMillis()) {
						taskOccurrencesStartDateTimes
								.add(supposedTaskOccurrenceStartDateTime);
					}
				}
				break;
			case WEEKS:
				for (Integer taskOccurrence : taskOccurrences) {
					Calendar currentPeriodBeginningClone = (Calendar) currentPeriodBeginning
							.clone();
					currentPeriodBeginningClone.add(Calendar.DAY_OF_YEAR,
							firstDayOfWeek <= taskOccurrence ? taskOccurrence
									- firstDayOfWeek : taskOccurrence - firstDayOfWeek
									+ 7);
					supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginningClone
							.clone();
					if (startDateTime <= supposedTaskOccurrenceStartDateTime
							.getTimeInMillis()) {
						taskOccurrencesStartDateTimes
								.add(supposedTaskOccurrenceStartDateTime);
					}
				}
				break;
			case DAYS:
			case MONTHS_ON_DATE:
				boolean isLastDayAlreadyWasTriedToAdd = false;
				for (Integer taskOccurrence : taskOccurrences) {
					int day = taskOccurrence;
					boolean tryToAdd = true;
					switch (day) {
					case 28:
						int lastDay = currentPeriodBeginning
								.getActualMaximum(Calendar.DAY_OF_MONTH);
						if (lastDay == 28) {
							isLastDayAlreadyWasTriedToAdd = true;
						}
						break;
					case 29:
						if (!isLastDayAlreadyWasTriedToAdd) {
							int month1 = currentPeriodBeginning.get(Calendar.MONTH);
							if (month1 == Calendar.FEBRUARY) {
								boolean isLeap = CalendarHelper
										.isLeapYear(currentPeriodBeginning
												.get(Calendar.YEAR));
								if (!isLeap) {
									tryToAdd = false;
								}
							}
							lastDay = currentPeriodBeginning
									.getActualMaximum(Calendar.DAY_OF_MONTH);
							if (lastDay == 29) {
								isLastDayAlreadyWasTriedToAdd = true;
							}
						}
						break;
					case 30:
						if (!isLastDayAlreadyWasTriedToAdd) {
							int month1 = currentPeriodBeginning.get(Calendar.MONTH);
							if (month1 == Calendar.FEBRUARY) {
								tryToAdd = false;
							}
							lastDay = currentPeriodBeginning
									.getActualMaximum(Calendar.DAY_OF_MONTH);
							if (lastDay == 30) {
								isLastDayAlreadyWasTriedToAdd = true;
							}
							break;
						}
					case 31:
						if (!isLastDayAlreadyWasTriedToAdd) {
							int month1 = currentPeriodBeginning.get(Calendar.MONTH);
							if (month1 == Calendar.FEBRUARY || month1 == Calendar.APRIL
									|| month1 == Calendar.JUNE
									|| month1 == Calendar.SEPTEMBER
									|| month1 == Calendar.NOVEMBER) {
								tryToAdd = false;
							}
							lastDay = currentPeriodBeginning
									.getActualMaximum(Calendar.DAY_OF_MONTH);
							if (lastDay == 31) {
								isLastDayAlreadyWasTriedToAdd = true;
							}
							break;
						}
					case 32:
						if (!isLastDayAlreadyWasTriedToAdd) {
							// int month1 = currentPeriodBeginning.getMonthOfYear();
							if (isLastDayAlreadyWasTriedToAdd) {
								tryToAdd = false;
							} else {
								day = currentPeriodBeginning
										.getActualMaximum(Calendar.DAY_OF_MONTH);
							}
							break;
						} else {
							tryToAdd = false;
						}
					default:
						break;
					}
					if (tryToAdd) {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								day - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					}
				}
				break;
			case YEARS:
				boolean isLeap = CalendarHelper.isLeapYear(currentPeriodBeginning
						.get(Calendar.YEAR));
				if (isDayFebruary28Checked
						&& (isDayFebruary29Checked || isDayFebruaryLastChecked)) {
					if (isLeap) {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								59 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								60 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					} else {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								59 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					}
				} else if (isDayFebruary28Checked && !isDayFebruary29Checked
						&& !isDayFebruaryLastChecked) {
					supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
							.clone();
					supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR, 59 - 1);
					if (startDateTime <= supposedTaskOccurrenceStartDateTime
							.getTimeInMillis()) {
						taskOccurrencesStartDateTimes
								.add(supposedTaskOccurrenceStartDateTime);
					}
				} else if (!isDayFebruary28Checked && isDayFebruary29Checked
						&& isDayFebruaryLastChecked) {
					if (isLeap) {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								60 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					} else {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								59 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					}
				} else if (!isDayFebruary28Checked && isDayFebruary29Checked
						&& !isDayFebruaryLastChecked) {
					if (isLeap) {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								60 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					}
				} else if (!isDayFebruary28Checked && !isDayFebruary29Checked
						&& isDayFebruaryLastChecked) {
					if (isLeap) {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								60 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					} else {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								59 - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					}
				}
				for (Integer taskOccurrence : taskOccurrences) {
					int dayCode = taskOccurrence;
					if (dayCode < 59) {
						supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
								.clone();
						supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
								dayCode - 1);
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					} else if (dayCode > 61) {
						if (isLeap) {
							supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
									.clone();
							supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
									dayCode - 2);
						} else {
							supposedTaskOccurrenceStartDateTime = (Calendar) currentPeriodBeginning
									.clone();
							supposedTaskOccurrenceStartDateTime.add(Calendar.DAY_OF_YEAR,
									dayCode - 3);
						}
						if (startDateTime <= supposedTaskOccurrenceStartDateTime
								.getTimeInMillis()) {
							taskOccurrencesStartDateTimes
									.add(supposedTaskOccurrenceStartDateTime);
						}
					}
				}
				break;
			case ONE_TIME:
				throw new IllegalArgumentException(
						"selectTaskOccurrencesOfRecurrentTask() has been called on Task with recurrence interval ONE_TIME");
			}
			Collections.sort(taskOccurrencesStartDateTimes);
			for (Calendar currentTaskOccurrenceStartDateTime : taskOccurrencesStartDateTimes) {
				currentTaskOccurrencesCount++;
				long currentTaskOccurrenceStartDateTimeMillis = currentTaskOccurrenceStartDateTime
						.getTimeInMillis();
				if (selectMode == -1) {
					if (repetitionEndDateTime != null
							&& repetitionEndDateTime <= currentTaskOccurrenceStartDateTimeMillis
							|| occurrencesMaxCount != null
							&& occurrencesMaxCount < currentTaskOccurrencesCount
							//
							|| includeEventsMatchingControlDateTime
							&& controlDateTime <= currentTaskOccurrenceStartDateTimeMillis
							|| !includeEventsMatchingControlDateTime
							&& controlDateTime < currentTaskOccurrenceStartDateTimeMillis) {
						if (previousOccurrenceStartDateTime != null
								&& (includeEventsMatchingControlDateTime
										&& previousOccurrenceStartDateTime <= controlDateTime || !includeEventsMatchingControlDateTime
										&& previousOccurrenceStartDateTime < controlDateTime)) {
							Long previousOccurrenceEndDateTime = null;
							if (occurrencePeriod != null) {
								Calendar calendar1 = Calendar.getInstance();
								calendar1
										.setTimeInMillis(previousOccurrenceStartDateTime);
								previousOccurrenceEndDateTime = CalendarHelper.plus(
										calendar1, occurrencePeriod).getTimeInMillis();
							}
							result.add(getCalendarViewTaskOccurrence(
									previousOccurrenceStartDateTime,
									previousOccurrenceEndDateTime, iterator));
						}
						return result;
					}
					previousOccurrenceStartDateTime = currentTaskOccurrenceStartDateTimeMillis;
				} else if (selectMode == 1) {
					if (repetitionEndDateTime != null
							&& repetitionEndDateTime <= currentTaskOccurrenceStartDateTimeMillis
							|| occurrencesMaxCount != null
							&& occurrencesMaxCount < currentTaskOccurrencesCount) {
						return result;
					}
					if (includeEventsMatchingControlDateTime
							&& controlDateTime <= currentTaskOccurrenceStartDateTimeMillis
							|| !includeEventsMatchingControlDateTime
							&& controlDateTime < currentTaskOccurrenceStartDateTimeMillis) {
						Long currentTaskOccurrenceEndDateTimeMillis = null;
						if (occurrencePeriod != null) {
							Calendar calendar1 = Calendar.getInstance();
							calendar1
									.setTimeInMillis(currentTaskOccurrenceStartDateTimeMillis);
							currentTaskOccurrenceEndDateTimeMillis = CalendarHelper.plus(
									calendar1, occurrencePeriod).getTimeInMillis();
						}
						result.add(getCalendarViewTaskOccurrence(
								currentTaskOccurrenceStartDateTimeMillis,
								currentTaskOccurrenceEndDateTimeMillis, iterator));
						return result;
					}
				} else {
					if (repetitionEndDateTime != null
							&& repetitionEndDateTime <= currentTaskOccurrenceStartDateTimeMillis
							|| occurrencesMaxCount != null
							&& occurrencesMaxCount < currentTaskOccurrencesCount
							|| borderEndDateTime <= currentTaskOccurrenceStartDateTimeMillis) {
						return result;
					}
					Long currentTaskOccurrenceEndDateTimeMillis = null;
					if (occurrencePeriod != null) {
						Calendar calendar1 = Calendar.getInstance();
						calendar1
								.setTimeInMillis(currentTaskOccurrenceStartDateTimeMillis);
						currentTaskOccurrenceEndDateTimeMillis = CalendarHelper.plus(
								calendar1, occurrencePeriod).getTimeInMillis();
					}
					result.add(getCalendarViewTaskOccurrence(
							currentTaskOccurrenceStartDateTimeMillis,
							currentTaskOccurrenceEndDateTimeMillis, iterator));
				}
			}
			//
			int wholePeriodsCount = 1;
			boolean isBorderStartDateTimeGreater = currentPeriodBeginning
					.getTimeInMillis() < borderStartDateTime;
			Calendar calendar = null;
			if (isBorderStartDateTimeGreater) {
				Calendar borderStartDateTimeCalendar = Calendar.getInstance();
				borderStartDateTimeCalendar.setTimeInMillis(borderStartDateTime);
				if (selectMode != 0) {
					calendar = borderStartDateTimeCalendar;
				} else {
					biz.advancedcalendar.calendar2.Period occurrencePeriodMinusOneMillis = occurrencePeriod
							.minusMillis(1);
					calendar = CalendarHelper.minus(borderStartDateTimeCalendar,
							occurrencePeriodMinusOneMillis);
				}
			}
			switch (recurrenceInterval) {
			case MINUTES:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.minutesBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				currentPeriodBeginning.add(Calendar.MINUTE,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				break;
			case HOURS:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.hoursBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				currentPeriodBeginning.add(Calendar.HOUR_OF_DAY,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				break;
			case DAYS:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.daysBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				currentPeriodBeginning.add(Calendar.DAY_OF_YEAR,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				break;
			case WEEKS:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.weeksBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				currentPeriodBeginning.add(Calendar.WEEK_OF_YEAR,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				break;
			case MONTHS_ON_DATE:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.monthsBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				currentPeriodBeginning.add(Calendar.MONTH,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				break;
			case MONTHS_ON_NTH_WEEK_DAY:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.monthsBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				currentPeriodBeginning.add(Calendar.MONTH,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				break;
			case YEARS:
				if (isBorderStartDateTimeGreater) {
					wholePeriodsCount = CalendarHelper.yearsBetween(
							currentPeriodBeginning, calendar) / timeUnitsCount;
				}
				Calendar nextCurrentPeriodBeginning = (Calendar) currentPeriodBeginning
						.clone();
				nextCurrentPeriodBeginning.add(Calendar.YEAR,
						(wholePeriodsCount = Math.max(1, wholePeriodsCount))
								* timeUnitsCount);
				int yearsBetween = CalendarHelper.yearsBetween(currentPeriodBeginning,
						nextCurrentPeriodBeginning);
				for (int i = 1; i < yearsBetween; i++) {
					int countIn_28_29_last = 0;
					currentPeriodBeginning.add(Calendar.YEAR, 1);
					boolean isLeap = CalendarHelper.isLeapYear(currentPeriodBeginning
							.get(Calendar.YEAR));
					if (isDayFebruary28Checked
							&& (isDayFebruary29Checked || isDayFebruaryLastChecked)
							&& isLeap) {
						countIn_28_29_last = 2;
					} else if (isDayFebruary28Checked
							&& (isDayFebruary29Checked || isDayFebruaryLastChecked)
							&& !isLeap && isDayFebruary28Checked
							&& !isDayFebruary29Checked && !isDayFebruaryLastChecked
							|| !isDayFebruary28Checked && isDayFebruary29Checked
							&& isDayFebruaryLastChecked || !isDayFebruary28Checked
							&& isDayFebruary29Checked && !isDayFebruaryLastChecked
							&& isLeap || !isDayFebruary28Checked
							&& !isDayFebruary29Checked && isDayFebruaryLastChecked) {
						countIn_28_29_last = 1;
					}
					currentTaskOccurrencesCount += countIn_28_29_last
							+ countNotIn_28_29_last;
				}
				currentPeriodBeginning = nextCurrentPeriodBeginning;
				break;
			case ONE_TIME:
				throw new IllegalArgumentException(
						"selectTaskOccurrencesOfRecurrentTask() has been called on Task with recurrence interval ONE_TIME");
			}
			if (!recurrenceInterval.equals(RecurrenceInterval.YEARS)) {
				currentTaskOccurrencesCount += (wholePeriodsCount - 1)
						* taskOccurrences.size();
			}
		}
	}

	private CalendarViewTaskOccurrence2 getCalendarViewTaskOccurrence(
			Long taskOccurrenceStartDateTime, Long taskOccurrenceEndDateTime,
			Iterator<CalendarViewTaskOccurrence2> iterator) {
		CalendarViewTaskOccurrence2 calendarViewTaskOccurrence;
		if (iterator != null && iterator.hasNext()) {
			calendarViewTaskOccurrence = iterator.next();
			iterator.remove();
			calendarViewTaskOccurrence.init(this, taskOccurrenceStartDateTime,
					taskOccurrenceEndDateTime);
		} else {
			calendarViewTaskOccurrence = new CalendarViewTaskOccurrence2(this,
					taskOccurrenceStartDateTime, taskOccurrenceEndDateTime);
		}
		return calendarViewTaskOccurrence;
	}

	private Calendar getNthOfMonth(int n, int day_of_week, int month, int year) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, month, 1, 0, 0, 0);
		calendar.set(Calendar.DAY_OF_WEEK, day_of_week);
		calendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, n);
		return calendar;
	}

	private Long getPreviousOccurrenceStartDateTimeOfOneTimeTask(long controlDateTime,
			boolean includeEventsMatchingControlDateTime) {
		if (startDateTime != null
				&& (includeEventsMatchingControlDateTime
						&& startDateTime <= controlDateTime || !includeEventsMatchingControlDateTime
						&& startDateTime < controlDateTime)) {
			return startDateTime;
		} else {
			return null;
		}
	}

	private Long getNextOccurrenceStartDateTimeOfOneTimeTask(long controlDateTime,
			boolean includeEventsMatchingControlDateTime) {
		if (startDateTime != null
				&& (includeEventsMatchingControlDateTime
						&& controlDateTime <= startDateTime || !includeEventsMatchingControlDateTime
						&& controlDateTime < startDateTime)) {
			return startDateTime;
		} else {
			return null;
		}
	}

	protected TaskUiData2(Parcel in) {
		id = in.readByte() == 0x00 ? null : in.readLong();
		parentId = in.readByte() == 0x00 ? null : in.readLong();
		name = in.readString();
		color = in.readByte() == 0x00 ? null : in.readInt();
		startDateTime = in.readByte() == 0x00 ? null : in.readLong();
		endDateTime = in.readByte() == 0x00 ? null : in.readLong();
		isCompleted = in.readByte() != 0x00;
		percentOfCompletion = (short) in.readInt();
		deleted = in.readByte() != 0x00;
		priority = (short) in.readInt();
		sortOrder = in.readInt();
		description = in.readString();
		location = in.readString();
		recurrenceIntervalValue = (short) in.readInt();
		timeUnitsCount = in.readByte() == 0x00 ? null : in.readInt();
		occurrencesMaxCount = in.readByte() == 0x00 ? null : in.readInt();
		repetitionEndDateTime = in.readByte() == 0x00 ? null : in.readLong();
		alarmRingtone = in.readString();
		notificationRingtone = in.readString();
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
		if (in.readByte() == 0x01) {
			taskOccurrenceList = new ArrayList<Integer>();
			in.readList(taskOccurrenceList, Integer.class.getClassLoader());
		} else {
			taskOccurrenceList = null;
		}
		if (in.readByte() == 0x01) {
			reminderList = new ArrayList<ReminderUiData2>();
			in.readList(reminderList, ReminderUiData2.class.getClassLoader());
			for (ReminderUiData2 reminderUiData2 : reminderList) {
				reminderUiData2.setTask(this);
			}
		} else {
			reminderList = null;
		}
		taskTitleForQuickReminder = in.readString();
		timeUnitsStartingIndexValue = in.readInt();
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
		if (parentId == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(parentId);
		}
		dest.writeString(name);
		if (color == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(color);
		}
		if (startDateTime == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(startDateTime);
		}
		if (endDateTime == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(endDateTime);
		}
		dest.writeByte((byte) (isCompleted ? 0x01 : 0x00));
		dest.writeInt(percentOfCompletion);
		dest.writeByte((byte) (deleted ? 0x01 : 0x00));
		dest.writeInt(priority);
		dest.writeInt(sortOrder);
		dest.writeString(description);
		dest.writeString(location);
		dest.writeInt(recurrenceIntervalValue);
		if (timeUnitsCount == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(timeUnitsCount);
		}
		if (occurrencesMaxCount == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(occurrencesMaxCount);
		}
		if (repetitionEndDateTime == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(repetitionEndDateTime);
		}
		dest.writeString(alarmRingtone);
		dest.writeString(notificationRingtone);
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
		if (taskOccurrenceList == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(taskOccurrenceList);
		}
		if (reminderList == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(reminderList);
		}
		dest.writeString(taskTitleForQuickReminder);
		dest.writeInt(timeUnitsStartingIndexValue);
	}

	public static final Parcelable.Creator<TaskUiData2> CREATOR = new Parcelable.Creator<TaskUiData2>() {
		@Override
		public TaskUiData2 createFromParcel(Parcel in) {
			return new TaskUiData2(in);
		}

		@Override
		public TaskUiData2[] newArray(int size) {
			return new TaskUiData2[size];
		}
	};

	public biz.advancedcalendar.db.TaskWithDependents generateTaskWithDependents(
			DaoSession daoSession, Context context) {
		long currentTimeMillis = System.currentTimeMillis();
		Task generatedTask;
		Task localTask = null;
		if (id != null) {
			localTask = DataProvider.getTask(daoSession, context, id, false);
		}
		if (localTask != null) {
			generatedTask = new Task(id, calendarId, parentId,
					localTask.getLocalCreateDateTime(),
					localTask.getLocalChangeDateTime(), localTask.getServerId(),
					localTask.getCreated(), localTask.getLastMod(),
					localTask.getSyncStatusValue(), name, priority, color, startDateTime,
					endDateTime, localTask.getRequiredLength(),
					localTask.getActualLength(), isCompleted, percentOfCompletion,
					localTask.getCompletedTime(), deleted, sortOrder, description,
					location, recurrenceIntervalValue, timeUnitsCount,
					occurrencesMaxCount, repetitionEndDateTime, alarmRingtone,
					notificationRingtone, ringtoneFadeInTime, playingTime,
					automaticSnoozeDuration, automaticSnoozesMaxCount, vibrate,
					vibratePattern, led, ledPattern, ledColor);
		} else {
			generatedTask = new Task(id, calendarId, parentId, currentTimeMillis,
					currentTimeMillis, null, currentTimeMillis, null,
					Task.SyncStatus.SYNC_UP_REQUIRED.getValue(), name, priority, color,
					startDateTime, endDateTime, 0, 0, isCompleted, percentOfCompletion,
					null, deleted, sortOrder, description, location,
					recurrenceIntervalValue, timeUnitsCount, occurrencesMaxCount,
					repetitionEndDateTime, alarmRingtone, notificationRingtone,
					ringtoneFadeInTime, playingTime, automaticSnoozeDuration,
					automaticSnoozesMaxCount, vibrate, vibratePattern, led, ledPattern,
					ledColor);
		}
		List<Reminder> generatedReminders = new ArrayList<Reminder>();
		for (ReminderUiData2 r : reminderList) {
			Reminder reminder = new Reminder(r.getId(), null, generatedTask.getId(),
					currentTimeMillis, r.getReminderDateTime(),
					r.getReminderTimeModeValue(), r.getText(), r.getEnabled(),
					r.getIsAlarm(), r.getRingtone(), r.getRingtoneFadeInTime(),
					r.getPlayingTime(), r.getAutomaticSnoozeDuration(),
					r.getAutomaticSnoozesMaxCount(), r.getVibrate(),
					r.getVibratePattern(), r.getLed(), r.getLedPattern(), r.getLedColor());
			generatedReminders.add(reminder);
		}
		List<TaskOccurrence> generatedTaskOccurrences = new ArrayList<TaskOccurrence>();
		for (Integer ordinalNumber : taskOccurrenceList) {
			TaskOccurrence TaskOccurrence = new TaskOccurrence(null,
					generatedTask.getId(), ordinalNumber);
			generatedTaskOccurrences.add(TaskOccurrence);
		}
		return new TaskWithDependents(generatedTask, generatedReminders,
				generatedTaskOccurrences);
	}
}