package biz.advancedcalendar.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityColorPicker;
import biz.advancedcalendar.activities.ActivityEditTask.ActivityEditTaskParcelableDataStore;
import biz.advancedcalendar.activities.ActivitySelectDaysOfYear2;
import biz.advancedcalendar.activities.ActivitySelectTaskSortOrder;
import biz.advancedcalendar.activities.ActivitySelectTreeItems;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.db.TaskUiData;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.db.TaskWithDependentsUiData.TaskUiDataWithCurrentStateSupplier;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.ValueMustBe;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.MonthRecurrenceMode;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.greendao.Task.StartTimeRequiredAction;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.TaskEditMode;
import biz.advancedcalendar.greendao.Task.TimeUnit;
import biz.advancedcalendar.greendao.TaskOccurrence;
import biz.advancedcalendar.utils.Helper;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.android.supportdatetimepicker.date.TextViewWithCircularIndicator;
import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.OnMultipleTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.TimeAttribute;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FragmentEditTaskPartMain extends Fragment implements
		OnCheckedChangeListener, OnClickListener, OnItemSelectedListener, DataSaver,
		TaskUiDataWithCurrentStateSupplier {
	private static class Calendar2 implements Parcelable {
		public Long id;
		public Long syncId;
		/** Not-null value. */
		public String name;

		public Calendar2(Long id, Long syncId, String name) {
			this.id = id;
			this.syncId = syncId;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		protected Calendar2(Parcel in) {
			id = in.readByte() == 0x00 ? null : in.readLong();
			syncId = in.readByte() == 0x00 ? null : in.readLong();
			name = in.readString();
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
			if (syncId == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeLong(syncId);
			}
			dest.writeString(name);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<Calendar2> CREATOR = new Parcelable.Creator<Calendar2>() {
			@Override
			public Calendar2 createFromParcel(Parcel in) {
				return new Calendar2(in);
			}

			@Override
			public Calendar2[] newArray(int size) {
				return new Calendar2[size];
			}
		};
	}

	private LinearLayout linearlayoutOccurrencesPickerMonthlyRecurrentOnNthWeekDay;
	private Spinner spinnerWeekDayNumberInMonth;
	private int weekDayNumberInMonth;
	private int weekDayNumberInMonthDefaultValue;
	private TextView textViewTimeUnit;
	private Spinner spinnerTimeUnit;
	private int spinnerTimeUnitPreviousPosition;
	private Spinner spinnerCalendar;
	private int spinnerCalendarPreviousPosition;
	private int spinnerWeekDayNumberInMonthPreviousPosition;
	private int timeUnitDefaultValue;
	private RecurrenceInterval recurrenceInterval;
	private Calendar2 calendar2;
	private EditText edittextTimeUnitsCount;
	private int timeUnitsCountMinValue;
	private int timeUnitsCountMaxValue;
	private byte timeUnitsStartingIndex;
	private EditText edittextOccurrencesMaxCount;
	private Integer occurrencesMaxCount;
	private int occurrencesMaxCountDefaultValue;
	private int occurrencesMaxCountMinValue;
	private int occurrencesMaxCountMaxValue;
	private ActivityEditTaskParcelableDataStore dataFragment;
	private TaskWithDependentsUiData taskWithDependentsUiData;
	private TaskUiData taskUiData;
	private List<TaskOccurrence> taskOccurrences;
	private UserInterfaceData mUserInterfaceData;
	private static String KEY_DATE_CALLER_ID = "KEY_DATE_CALLER_ID";
	// variables to be saved in onSaveInstanceState()
	private int dateCallerId;
	private HorizontalScrollView scrollviewOccurrencesPickerMinutelyRecurrent;
	private HorizontalScrollView scrollviewOccurrencesPickerHourlyRecurrent;
	private HorizontalScrollView scrollviewOccurrencesPickerDailyRecurrent;
	private HorizontalScrollView scrollviewWeekDaysPicker;
	private HorizontalScrollView scrollviewOccurrencesPickerMonthlyRecurrentOnDate;
	private LinearLayout linearlayoutOccurrencesPickerYearlyRecurrent;
	private Button buttonDaysPickerOfYearlyRecurrentTask;
	private RadioGroup radiogroupMonthBasedRecurrenceMode;
	private EditTextTaskTimeUnitsCountChangedListener editTextTaskTimeUnitsCountChangedListener;
	private EditText edittextTaskName;
	private TaskDate taskStartDate;
	private TaskDate taskEndDate;
	private TaskDate taskRepetitionEndDate;
	private boolean is24HourFormat;
	private StartTimeRequiredAction startTimeRequiredAction;
	private boolean isRecurrenceDataComplete;
	private long startTime;
	private long startDateTimePreviousValue;
	private long endDateTimePreviousValue;
	private long repetitionEndDateTimePreviousValue;
	private BroadcastReceiverForFragmentEditTaskPartMain mReceiver;
	private LinearLayout linearlayoutRecurrent;
	private LinearLayout linearlayoutOccurrencesPickerMonthlyRecurrent;
	private TaskEditMode taskEditMode;
	// private OnCheckedChangeListener occurrencesPickerCheckBoxOnCheckedChangeListener;
	private OnClickListener occurrencesPickerCheckBoxOnClickListener;
	private boolean setupUiPending;
	private TaskUiData setupUiTaskUiData2;
	private ColorStateList mDefaultTextColorStateList;
	private Drawable mDefaultBackground;
	private ScrollView scrollView;
	private Integer scrollPos;
	private String selectedConfiguration;
	private static Map<String, Integer> mapStringToScrollPos = new HashMap<String, Integer>();
	public static final boolean OnTouchDebug = false;
	public static final String OnTouchDebugTag = "OnTouchDebugTag";
	private ColorStateList mDefaultTextColorStateListForRadioButtonSortOrderAfterTask;
	private Drawable mDefaultBackgroundForRadioButtonSortOrderAfterTask;
	private CheckedTextView checkedTextView;
	private RadioButton radioButtonSortOrderAfterTask;
	private TextView textViewTaskSortOrder;
	private TextView textViewTaskParent;
	private RadioButton radioButtonSortOrderFirst;

	public static class UserInterfaceData implements Parcelable {
		public TaskUiData taskUiData;
		public Set<WantingItem> wantingItemSet;
		public Set<WantingItem> wantingItemSetForTask;
		public CharSequence textTaskName;
		public CharSequence textTimeUnitsCount;
		public CharSequence textTimeUnitsCountHourlyRecurrent;
		public CharSequence textTimeUnitsCountDailyRecurrent;
		public CharSequence textTimeUnitsCountWeeklyRecurrent;
		public CharSequence textTimeUnitsCountMonthlyRecurrentOnDate;
		public CharSequence textTimeUnitsCountMonthlyRecurrentOnNthWeekDay;
		public CharSequence textTimeUnitsCountYearlyRecurrent;
		public CharSequence textOccurrencesMaxCount;
		public int timeUnitDefaultValue;
		public short recurrenceIntervalValue;
		//
		public int spinnerWeekDayNumberInMonthPosition;
		public int weekDayNumberInMonth;
		public int weekDayNumberInMonthDefaultValue;
		public RecurrenceInterval recurrenceInterval;
		public MonthRecurrenceMode monthRecurrenceMode;
		public int radiogroupMonthBasedRecurrenceMode;
		public Long id;
		public String taskName;
		public List<Calendar2> calendar2s;
		public Calendar2 calendar2;
		public Long parentId;
		public boolean is24HourFormat;
		public StartTimeRequiredAction startTimeRequiredAction;
		public boolean isRecurrenceDataComplete;
		public long startTime;
		public long startDateTimePreviousValue;
		public long endDateTimePreviousValue;
		public long repetitionEndDateTimePreviousValue;
		public Integer color;
		public short priority;
		public Long startDateTime;
		public Long endDateTime;
		public short percentOfCompletion;
		public Long completedTime;
		public int sortOrder;
		public String taskDescription;
		public Integer timeUnitsCount;
		public byte timeUnitsStartingIndexValue;
		public Integer occurrencesMaxCount;
		public int occurrencesMaxCountDefaultValue;
		public int occurrencesMaxCountMinValue;
		public int occurrencesMaxCountMaxValue;
		public Long repetitionEndDateTime;
		public String alarmRingtone;
		public String notificationRingtone;
		public Long ringtoneFadeInTime;
		public Integer playingTime;
		public Integer automaticSnoozeDuration;
		public Integer automaticSnoozesMaxCount;
		public Boolean vibrate;
		public String vibratePattern;
		public Boolean led;
		public String ledPattern;
		public Integer ledColor;
		public List<ReminderUiData> reminderUiDataList;
		public ValueMustBe timeUnitsCountMustBe;
		public SyncPolicy syncPolicy;
		public MarkSyncNeededPolicy markSyncNeededPolicy;

		public enum ValueMustBe {
			GREATER_THAN_OR_EQUAL_TO((byte) 1), LESS_THAN_OR_EQUAL_TO((byte) 2), WITHIN_BOUNDS(
					(byte) 3), ;
			private byte value;
			private static int minValue;
			private static int maxValue;
			private static String text;

			private ValueMustBe(byte value) {
				this.value = value;
			}

			public byte getValue() {
				return value;
			}

			public static int getMinValue() {
				return ValueMustBe.minValue;
			}

			public static void setMinValue(int minValue) {
				ValueMustBe.minValue = minValue;
			}

			public static int getMaxValue() {
				return ValueMustBe.maxValue;
			}

			public static void setMaxValue(int maxValue) {
				ValueMustBe.maxValue = maxValue;
			}

			public static String getText() {
				return ValueMustBe.text;
			}

			public static void setText(String text) {
				ValueMustBe.text = text;
			}

			@SuppressLint("UseSparseArrays")
			private static Map<Byte, ValueMustBe> map = new HashMap<Byte, ValueMustBe>();
			private static Map<String, ValueMustBe> stringToObjectMap = new HashMap<String, ValueMustBe>();
			static {
				for (ValueMustBe valueMustBe : ValueMustBe.values()) {
					ValueMustBe.map.put(valueMustBe.value, valueMustBe);
					ValueMustBe.stringToObjectMap.put(valueMustBe.name(), valueMustBe);
				}
			}

			public static ValueMustBe fromInt(byte value) {
				ValueMustBe valueMustBe = ValueMustBe.map.get(value);
				return valueMustBe;
			}

			public static ValueMustBe fromString(String name) {
				ValueMustBe valueMustBe = ValueMustBe.stringToObjectMap.get(name);
				return valueMustBe;
			}
		}

		public enum WantingItem {
			TASK_NAME((short) 17, R.string.toast_text_task_name_is_required), TIME_UNIT(
					(short) 18, R.string.toast_text_time_unit_is_not_selected), TIME_UNITS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 19,
					R.string.toast_text_time_units_count_must_be_greater_than_or_equal_to), TIME_UNITS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 20,
					R.string.toast_text_time_units_count_must_be_less_than_or_equal_to), TIME_UNITS_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 21,
					R.string.toast_text_time_units_count_must_be_within_bounds), MINUTES_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 22,
					R.string.toast_text_minutes_count_must_be_greater_than_or_equal_to), MINUTES_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 23,
					R.string.toast_text_minutes_count_must_be_less_than_or_equal_to), MINUTES_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 24, R.string.toast_text_minutes_count_must_be_within_bounds), HOURS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 25,
					R.string.toast_text_hours_count_must_be_greater_than_or_equal_to), HOURS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 26,
					R.string.toast_text_hours_count_must_be_less_than_or_equal_to), HOURS_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 27, R.string.toast_text_hours_count_must_be_within_bounds), DAYS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 28,
					R.string.toast_text_days_count_must_be_greater_than_or_equal_to), DAYS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 29,
					R.string.toast_text_days_count_must_be_less_than_or_equal_to), DAYS_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 30, R.string.toast_text_days_count_must_be_within_bounds), WEEKS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 31,
					R.string.toast_text_weeks_count_must_be_greater_than_or_equal_to), WEEKS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 32,
					R.string.toast_text_weeks_count_must_be_less_than_or_equal_to), WEEKS_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 33, R.string.toast_text_weeks_count_must_be_within_bounds), MONTHS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 34,
					R.string.toast_text_months_count_must_be_greater_than_or_equal_to), MONTHS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 35,
					R.string.toast_text_months_count_must_be_less_than_or_equal_to), MONTHS_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 36, R.string.toast_text_months_count_must_be_within_bounds), MONTHS_ON_DATE_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 37,
					R.string.toast_text_months_on_date_count_must_be_greater_than_or_equal_to), MONTHS_ON_DATE_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 38,
					R.string.toast_text_months_on_date_count_must_be_less_than_or_equal_to), MONTHS_ON_DATE_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 39,
					R.string.toast_text_months_on_date_count_must_be_within_bounds), MONTHS_ON_NTH_WEEK_DAY_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 40,
					R.string.toast_text_months_on_nth_week_day_count_must_be_greater_than_or_equal_to), MONTHS_ON_NTH_WEEK_DAY_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 41,
					R.string.toast_text_months_on_nth_week_day_count_must_be_less_than_or_equal_to), MONTHS_ON_NTH_WEEK_DAY_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 42,
					R.string.toast_text_months_on_nth_week_day_count_must_be_within_bounds), YEARS_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 43,
					R.string.toast_text_years_count_must_be_greater_than_or_equal_to), YEARS_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 44,
					R.string.toast_text_years_count_must_be_less_than_or_equal_to), YEARS_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 45, R.string.toast_text_years_count_must_be_within_bounds), OCCURRENCES_MAX_COUNT_TO_BE_GREATER(
					(short) 46,
					R.string.toast_text_occurrences_max_count_must_be_greater_than_or_equal_to), OCCURRENCES_MAX_COUNT_TO_BE_LESS(
					(short) 47,
					R.string.toast_text_occurrences_max_count_must_be_greater_than_or_equal_to), OCCURRENCES_MAX_COUNT_TO_BE_VALID(
					(short) 48,
					R.string.toast_text_occurrences_max_count_must_be_greater_than_or_equal_to), START_DATE_TIME(
					(short) 49,
					R.string.toast_text_start_date_is_required_for_recurrent_task), START_DATE_TIME_FOR_ONE_TIME(
					(short) 50,
					R.string.toast_text_start_date_time_for_one_time_is_not_selected), START_DATE_TIME_TO_BE_BEFORE_END_DATE_TIME(
					(short) 51, R.string.toast_text_start_time_must_be_before_end_time), START_DATE_TIME_TO_BE_BEFORE_END_DATE_TIME_FOR_ONE_TIME(
					(short) 52, R.string.toast_text_start_time_must_be_before_end_time), RECURRENCE_INTERVAL_TO_BE_GREATER_THAN_TASK_LENGTH(
					(short) 53,
					R.string.toast_text_task_length_is_longer_than_task_recurrence_interval), OCCURRENCES(
					(short) 54, R.string.toast_text_occurrences_is_not_selected), OCCURRENCES_OF_MINUTELY_RECURRENT_TASK(
					(short) 55,
					R.string.toast_text_minutely_recurrent_tasks_occurrences_is_not_selected), OCCURRENCES_OF_HOURLY_RECURRENT_TASK(
					(short) 56,
					R.string.toast_text_hourly_recurrent_tasks_occurrences_is_not_selected), OCCURRENCES_OF_DAILY_RECURRENT_TASK(
					(short) 57,
					R.string.toast_text_daily_recurrent_tasks_occurrences_is_not_selected), OCCURRENCES_OF_WEEKLY_RECURRENT_TASK(
					(short) 58,
					R.string.toast_text_weekly_recurrent_tasks_occurrences_is_not_selected), OCCURRENCES_OF_TASK_MONTHLY_RECURRENT_ON_DATE(
					(short) 59,
					R.string.toast_text_monthly_recurrent_on_date_tasks_occurrences_is_not_selected), OCCURRENCES_OF_TASK_MONTHLY_RECURRENT_ON_NTH_WEEK_DAY(
					(short) 60,
					R.string.toast_text_monthly_recurrent_on_nth_week_day_tasks_occurrences_is_not_selected), OCCURRENCES_OF_YEARLY_RECURRENT_TASK(
					(short) 61,
					R.string.toast_text_yearly_recurrent_tasks_occurrences_is_not_selected), REMINDER_NAME(
					(short) 62, R.string.toast_text_reminder_name_is_required), RINGTONE_FADE_IN_TIME_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 63,
					R.string.toast_text_ringtone_fade_in_time_must_be_greater_than_or_equal_to), RINGTONE_FADE_IN_TIME_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 64,
					R.string.toast_text_ringtone_fade_in_time_must_be_less_than_or_equal_to), RINGTONE_FADE_IN_TIME_TO_BE_WITHIN_BOUNDS(
					(short) 65,
					R.string.toast_text_ringtone_fade_in_time_must_be_within_bounds), REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 66,
					R.string.toast_text_reminders_popup_window_displaying_duration_must_be_greater_than_or_equal_to), REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 67,
					R.string.toast_text_reminders_popup_window_displaying_duration_must_be_less_than_or_equal_to), REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_WITHIN_BOUNDS(
					(short) 68,
					R.string.toast_text_reminders_popup_window_displaying_duration_must_be_within_bounds), AUTOMATIC_SNOOZE_DURATION_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 69,
					R.string.toast_text_automatic_snooze_duration_must_be_greater_than_or_equal_to), AUTOMATIC_SNOOZE_DURATION_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 70,
					R.string.toast_text_automatic_snooze_duration_must_be_less_than_or_equal_to), AUTOMATIC_SNOOZE_DURATION_TO_BE_WITHIN_BOUNDS(
					(short) 71,
					R.string.toast_text_automatic_snooze_duration_must_be_within_bounds), AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO(
					(short) 72,
					R.string.toast_text_automatic_snoozes_max_count_must_be_greater_than_or_equal_to), AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO(
					(short) 73,
					R.string.toast_text_automatic_snooze_duration_must_be_less_than_or_equal_to), AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_WITHIN_BOUNDS(
					(short) 74,
					R.string.toast_text_automatic_snooze_duration_must_be_within_bounds);
			private short value;
			private int resid;
			private String text;

			public static void initializeText(Context context) {
				Resources resources = context.getResources();
				for (WantingItem wantingItem : WantingItem.values()) {
					String text;
					int messageId = wantingItem.getMessageId();
					switch (messageId) {
					case R.string.toast_text_yearly_recurrent_tasks_occurrences_is_not_selected:
						text = String
								.format(resources
										.getString(R.string.toast_text_yearly_recurrent_tasks_occurrences_is_not_selected),
										resources
												.getString(R.string.fragment_edit_task_part_main_button_days_picker_of_yearly_recurrent_task),
										resources.getString(R.string.button_ok));
						break;
					default:
						text = resources.getString(messageId);
						break;
					}
					wantingItem.setText(text);
				}
			}

			private WantingItem(short value, int resid) {
				this.value = value;
				this.resid = resid;
			}

			public short getValue() {
				return value;
			}

			public int getMessageId() {
				return resid;
			}

			public String getText() {
				return text;
			}

			public void setText(String text) {
				this.text = text;
			}

			private static Map<Short, WantingItem> map = new HashMap<Short, WantingItem>();
			static {
				for (WantingItem wantingItem : WantingItem.values()) {
					WantingItem.map.put(wantingItem.value, wantingItem);
				}
			}

			public static WantingItem fromInt(short value) {
				WantingItem wantingItem = WantingItem.map.get(value);
				return wantingItem;
			}
		}

		public enum TimeFormat {
			FROM_LOCALE((short) CommonConstants.TIME_FORMAT_FROM_LOCALE), HOURS_12(
					(short) CommonConstants.TIME_FORMAT_12_HOUR_CLOCK), HOURS_24(
					(short) CommonConstants.TIME_FORMAT_24_HOUR_CLOCK);
			private short value;

			private TimeFormat(short value) {
				this.value = value;
			}

			public short getValue() {
				return value;
			}

			private static Map<Short, TimeFormat> map = new HashMap<Short, TimeFormat>();
			static {
				for (TimeFormat timeFormat : TimeFormat.values()) {
					TimeFormat.map.put(timeFormat.value, timeFormat);
				}
			}

			public static TimeFormat fromInt(short calendarSelectedDate) {
				TimeFormat datePickerDate = TimeFormat.map.get(calendarSelectedDate);
				return datePickerDate;
			}
		}

		public UserInterfaceData(Context context,
				TaskWithDependentsUiData taskWithDependentsUiData) {
			super();
			taskUiData = taskWithDependentsUiData.TaskUiData;
			wantingItemSet = EnumSet.noneOf(WantingItem.class);
			wantingItemSetForTask = EnumSet.noneOf(WantingItem.class);
			id = taskUiData.getId();
			textTaskName = taskUiData.getName();
			if (textTaskName == null) {
				textTaskName = "";
			}
			parentId = taskUiData.getParentId();
			is24HourFormat = Helper.is24HourFormat(context);
			Resources resources = context.getResources();
			startTime = Helper.getLongPreferenceValue(context,
					R.string.preference_key_start_time,
					resources.getInteger(R.integer.start_time_default_value),
					(long) resources.getInteger(R.integer.start_time_min_value),
					(long) resources.getInteger(R.integer.start_time_max_value));
			startTimeRequiredAction = StartTimeRequiredAction.fromInt((short) Helper
					.getIntegerPreferenceValueFromStringArray(context,
							R.string.preference_key_start_time_required_action,
							R.array.start_time_required_action_values_array,
							R.integer.start_time_required_action_default_value));
			startDateTimePreviousValue = FragmentEditTaskPartMain
					.getTodayStartTime(startTime);
			long taskDuration = Helper.getLongPreferenceValue(context,
					R.string.preference_key_task_duration,
					resources.getInteger(R.integer.task_duration_default_value),
					(long) resources.getInteger(R.integer.task_duration_min_value),
					(long) resources.getInteger(R.integer.task_duration_max_value));
			endDateTimePreviousValue = startDateTimePreviousValue + taskDuration;
			long repetitionsDuration = Helper
					.getLongPreferenceValue(
							context,
							R.string.preference_key_repetitions_duration,
							resources
									.getInteger(R.integer.repetitions_duration_default_value),
							(long) resources
									.getInteger(R.integer.repetitions_duration_min_value),
							(long) resources
									.getInteger(R.integer.repetitions_duration_max_value));
			repetitionEndDateTimePreviousValue = startDateTimePreviousValue
					+ repetitionsDuration * 24 * 3600 * 1000;
			color = taskUiData.getColor();
			priority = taskUiData.getPriority();
			startDateTime = taskUiData.getStartDateTime();
			endDateTime = taskUiData.getEndDateTime();
			percentOfCompletion = taskUiData.getPercentOfCompletion();
			completedTime = taskUiData.getCompletedTime();
			sortOrder = taskUiData.getSortOrder();
			taskDescription = taskUiData.getDescription();
			timeUnitDefaultValue = resources
					.getInteger(R.integer.time_unit_default_value);
			WantingItem.initializeText(context);
			RecurrenceInterval.initializeValueBounds(context);
			recurrenceIntervalValue = taskUiData.getRecurrenceIntervalValue();
			recurrenceInterval = RecurrenceInterval.fromInt(recurrenceIntervalValue);
			//
			List<biz.advancedcalendar.greendao.Calendar> calendars = new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider
					.getCalendars(null, context));
			calendars.add(0, new biz.advancedcalendar.greendao.Calendar(null, null,
					resources.getString(R.string.default_calendar_name)));
			calendar2s = new ArrayList<Calendar2>(calendars.size());
			for (biz.advancedcalendar.greendao.Calendar calendar : calendars) {
				calendar2s.add(new Calendar2(calendar.getId(), calendar.getSyncId(),
						calendar.getName()));
			}
			int i;
			for (i = 0; i < calendar2s.size(); i++) {
				Calendar2 calendar2 = calendar2s.get(i);
				if (calendar2.id == null) {
					if (taskUiData.getCalendarId() == null) {
						break;
					}
				} else if (calendar2.id.equals(taskUiData.getCalendarId())) {
					break;
				}
			}
			calendar2 = calendar2s.get(i);
			//
			MonthRecurrenceMode
					.initializeRecurrenceIntervalToMonthRecurrenceModeMap(context);
			monthRecurrenceMode = MonthRecurrenceMode
					.fromRecurrenceInterval(context, recurrenceInterval);
			timeUnitsStartingIndexValue = (byte) (Helper
					.getIntegerPreferenceValueFromStringArray(context,
							R.string.preference_key_time_units_starting_index,
							R.array.time_units_starting_index_values_array,
							R.integer.time_units_starting_index_default_value) - 1);
			List<TaskOccurrence> taskOccurrences = recurrenceInterval
					.getTaskOccurrences();
			for (TaskOccurrence taskOccurrence : taskWithDependentsUiData.TaskOccurrences) {
				taskOccurrences.add(new TaskOccurrence(taskOccurrence));
			}
			weekDayNumberInMonthDefaultValue = resources
					.getInteger(R.integer.week_day_number_default_value);
			weekDayNumberInMonth = Helper.getWeekdayNumberInMonthFromEntityOrDefault(
					context, recurrenceInterval);
			occurrencesMaxCountDefaultValue = resources
					.getInteger(R.integer.occurrences_max_count_default_value);
			occurrencesMaxCountMinValue = resources
					.getInteger(R.integer.occurrences_max_count_min_value);
			occurrencesMaxCountMaxValue = resources
					.getInteger(R.integer.occurrences_max_count_max_value);
			occurrencesMaxCount = taskUiData.getOccurrencesMaxCount();
			if (occurrencesMaxCount != null) {
				textOccurrencesMaxCount = occurrencesMaxCount.toString();
			} else {
				textOccurrencesMaxCount = "";
			}
			repetitionEndDateTime = taskUiData.getRepetitionEndDateTime();
			Integer timeUnitsCount = taskUiData.getTimeUnitsCount();
			if (timeUnitsCount != null) {
				recurrenceInterval.setCheckBoxCount(timeUnitsCount.intValue());
				recurrenceInterval.setTextTimeUnitsCount(timeUnitsCount.toString());
			}
			alarmRingtone = taskUiData.getAlarmRingtone();
			notificationRingtone = taskUiData.getNotificationRingtone();
			ringtoneFadeInTime = taskUiData.getRingtoneFadeInTime();
			playingTime = taskUiData.getPlayingTime();
			automaticSnoozeDuration = taskUiData.getAutomaticSnoozeDuration();
			automaticSnoozesMaxCount = taskUiData.getAutomaticSnoozesMaxCount();
			vibrate = taskUiData.getVibrate();
			vibratePattern = taskUiData.getVibratePattern();
			led = taskUiData.getLed();
			ledPattern = taskUiData.getLedPattern();
			ledColor = taskUiData.getLedColor();
			reminderUiDataList = taskWithDependentsUiData.RemindersUiData;
			syncPolicy = SyncPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(context,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			markSyncNeededPolicy = MarkSyncNeededPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(context,
							R.string.preference_key_mark_sync_needed,
							R.array.mark_sync_needed_values_array,
							R.integer.mark_sync_needed_default_value));
			syncPolicy = SyncPolicy.DO_NOT_SYNC;
			markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
			isRecurrenceDataComplete = isRecurrenceDataComplete(context);
		}

		public UserInterfaceData(UserInterfaceData userInterfaceData) {
			super();
			wantingItemSet = new HashSet<WantingItem>(
					userInterfaceData.wantingItemSet.size());
			for (WantingItem wantingItem : wantingItemSet) {
				wantingItemSet.add(wantingItem);
			}
			textTaskName = new SpannableStringBuilder(userInterfaceData.textTaskName);
			textOccurrencesMaxCount = new SpannableStringBuilder(
					userInterfaceData.textOccurrencesMaxCount);
			recurrenceIntervalValue = userInterfaceData.recurrenceIntervalValue;
			spinnerWeekDayNumberInMonthPosition = userInterfaceData.spinnerWeekDayNumberInMonthPosition;
			weekDayNumberInMonth = userInterfaceData.weekDayNumberInMonth;
			timeUnitDefaultValue = userInterfaceData.timeUnitDefaultValue;
			recurrenceInterval = userInterfaceData.recurrenceInterval;
			weekDayNumberInMonthDefaultValue = userInterfaceData.weekDayNumberInMonthDefaultValue;
			monthRecurrenceMode = userInterfaceData.monthRecurrenceMode;
			radiogroupMonthBasedRecurrenceMode = userInterfaceData.radiogroupMonthBasedRecurrenceMode;
			id = userInterfaceData.id;
			taskName = userInterfaceData.taskName;
			calendar2 = userInterfaceData.calendar2;
			calendar2s = userInterfaceData.calendar2s;
			parentId = userInterfaceData.parentId;
			is24HourFormat = userInterfaceData.is24HourFormat;
			startTime = userInterfaceData.startTime;
			startTimeRequiredAction = userInterfaceData.startTimeRequiredAction;
			isRecurrenceDataComplete = userInterfaceData.isRecurrenceDataComplete;
			startDateTimePreviousValue = userInterfaceData.startDateTimePreviousValue;
			endDateTimePreviousValue = userInterfaceData.endDateTimePreviousValue;
			repetitionEndDateTimePreviousValue = userInterfaceData.repetitionEndDateTimePreviousValue;
			color = userInterfaceData.color;
			priority = userInterfaceData.priority;
			startDateTime = userInterfaceData.startDateTime;
			endDateTime = userInterfaceData.endDateTime;
			percentOfCompletion = userInterfaceData.percentOfCompletion;
			completedTime = userInterfaceData.completedTime;
			sortOrder = userInterfaceData.sortOrder;
			taskDescription = userInterfaceData.taskDescription;
			occurrencesMaxCountDefaultValue = userInterfaceData.occurrencesMaxCountDefaultValue;
			occurrencesMaxCountMinValue = userInterfaceData.occurrencesMaxCountMinValue;
			occurrencesMaxCountMaxValue = userInterfaceData.occurrencesMaxCountMaxValue;
			occurrencesMaxCount = userInterfaceData.occurrencesMaxCount;
			repetitionEndDateTime = userInterfaceData.repetitionEndDateTime;
			alarmRingtone = userInterfaceData.alarmRingtone;
			notificationRingtone = userInterfaceData.notificationRingtone;
			automaticSnoozeDuration = userInterfaceData.automaticSnoozeDuration;
			automaticSnoozesMaxCount = userInterfaceData.automaticSnoozesMaxCount;
			playingTime = userInterfaceData.playingTime;
			vibrate = userInterfaceData.vibrate;
			vibratePattern = userInterfaceData.vibratePattern;
			led = userInterfaceData.led;
			ledPattern = userInterfaceData.ledPattern;
			ledColor = userInterfaceData.ledColor;
			reminderUiDataList = new ArrayList<ReminderUiData>(
					userInterfaceData.reminderUiDataList.size());
			for (ReminderUiData reminder : userInterfaceData.reminderUiDataList) {
				ReminderUiData newReminder = new ReminderUiData(reminder, null);
				reminderUiDataList.add(newReminder);
			}
		}

		public Integer getColor(Context context) {
			if (color != null) {
				return color;
			} else {
				// switch (PRIORITY.fromInt(priority)) {
				// case HIGH:
				// return context.getResources().getColor(
				// R.color.task_high_priority_default_color);
				// case LOW:
				// return context.getResources().getColor(
				// R.color.task_low_priority_default_color);
				// case MEDIUM:
				// default:
				// return context.getResources().getColor(
				// R.color.task_unset_color_default_value);
				// }
				return color;
			}
		}

		public boolean isRecurrenceDataComplete(Context context) {
			boolean isCollected = true;
			wantingItemSet.clear();
			isRecurrenceDataCompleteForTaskName();
			if (!isRecurrenceDataCompleteForRecurrenceInterval(context)) {
				isCollected = false;
			}
			isRecurrenceDataComplete = isCollected;
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForTaskName() {
			boolean isCollected = true;
			taskUiData.setName(textTaskName.toString());
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForRecurrenceInterval(Context context) {
			boolean isCollected = true;
			if (recurrenceInterval != null) {
				if (recurrenceInterval != RecurrenceInterval.ONE_TIME) {
					if (!isRecurrenceDataCompleteForOccurrences(context)) {
						isCollected = false;
					}
				} else {
					taskUiData.setRecurrenceIntervalValue(recurrenceInterval.getValue());
					taskUiData.setTimeUnitsCount(null);
					taskUiData.setOccurrencesMaxCount(null);
					if (!isRecurrenceDataCompleteForStartDateTimeForOneTime()) {
						isCollected = false;
					}
				}
			} else {
				isCollected = false;
			}
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForTimeUnitsCount() {
			boolean isCollected = true;
			if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
				isCollected = false;
				switch (recurrenceInterval.getTimeUnitsCountMustBe()) {
				case GREATER_THAN_OR_EQUAL_TO:
					recurrenceInterval
							.getWantingItemTimeUnitsCountToBeGreaterThanOrEqualTo()
							.setText(ValueMustBe.getText());
					wantingItemSet.add(recurrenceInterval
							.getWantingItemTimeUnitsCountToBeGreaterThanOrEqualTo());
					break;
				case LESS_THAN_OR_EQUAL_TO:
					recurrenceInterval
							.getWantingItemTimeUnitsCountToBeLessThanOrEqualTo().setText(
									ValueMustBe.getText());
					wantingItemSet.add(recurrenceInterval
							.getWantingItemTimeUnitsCountToBeLessThanOrEqualTo());
					break;
				case WITHIN_BOUNDS:
					recurrenceInterval.getWantingItemTimeUnitsCountToBeWithinBounds()
							.setText(ValueMustBe.getText());
					wantingItemSet.add(recurrenceInterval
							.getWantingItemTimeUnitsCountToBeWithinBounds());
					break;
				default:
					break;
				}
			}
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForOccurrencesMaxCount() {
			boolean isCollected = true;
			Integer occurrencesMaxCount = taskUiData.getOccurrencesMaxCount();
			if (occurrencesMaxCount != null) {
				int occurrencesMaxCountValue = occurrencesMaxCount.intValue();
				if (occurrencesMaxCountValue < occurrencesMaxCountMinValue) {
					isCollected = false;
					wantingItemSet.add(WantingItem.OCCURRENCES_MAX_COUNT_TO_BE_GREATER);
				} else if (occurrencesMaxCountValue > occurrencesMaxCountMaxValue) {
					isCollected = false;
					wantingItemSet.add(WantingItem.OCCURRENCES_MAX_COUNT_TO_BE_LESS);
				}
			} else if (textOccurrencesMaxCount != null
					&& textOccurrencesMaxCount.length() != 0) {
				isCollected = false;
				wantingItemSet.add(WantingItem.OCCURRENCES_MAX_COUNT_TO_BE_VALID);
			}
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForStartDateTimeForOneTime() {
			boolean isCollected = true;
			if (startDateTime != null && endDateTime != null
					&& startDateTime > endDateTime) {
				isCollected = false;
				wantingItemSet
						.add(WantingItem.START_DATE_TIME_TO_BE_BEFORE_END_DATE_TIME_FOR_ONE_TIME);
			} else {
				taskUiData.setStartDateTime(startDateTime);
				taskUiData.setEndDateTime(endDateTime);
				taskUiData.setRepetitionEndDateTime(null);
			}
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForStartDateTime(Context context) {
			boolean isCollected = true;
			Resources resources = context.getResources();
			if (startDateTime == null) {
				switch (startTimeRequiredAction) {
				case DO_NOT_AUTOMATICALLY_SET:
				default:
					isCollected = false;
					wantingItemSet.add(WantingItem.START_DATE_TIME);
					break;
				case LOAD_FROM_SETTINGS:
					long todayStartTime = FragmentEditTaskPartMain
							.getTodayStartTime(startTime);
					taskUiData.setStartDateTime(todayStartTime);
					long taskDuration = Helper.getLongPreferenceValue(context,
							R.string.preference_key_task_duration, resources
									.getInteger(R.integer.task_duration_default_value),
							(long) resources
									.getInteger(R.integer.task_duration_min_value),
							(long) resources
									.getInteger(R.integer.task_duration_max_value));
					taskUiData.setEndDateTime(todayStartTime + taskDuration);
					// mUserInterfaceData.task
					// .setRepetitionEndDateTime(todayStartTime + 24 * 3600 * 1000);
					break;
				case SET_TO_CURRENT_TIME:
					long currentTime = FragmentEditTaskPartMain.getCurrentTime();
					taskUiData.setStartDateTime(currentTime);
					taskDuration = Helper.getLongPreferenceValue(context,
							R.string.preference_key_task_duration, resources
									.getInteger(R.integer.task_duration_default_value),
							(long) resources
									.getInteger(R.integer.task_duration_min_value),
							(long) resources
									.getInteger(R.integer.task_duration_max_value));
					taskUiData.setEndDateTime(currentTime + taskDuration);
					// mUserInterfaceData.task
					// .setRepetitionEndDateTime(currentTime + 24 * 3600 * 1000);
					break;
				}
				// long milliseconds = Helper.getLongPreferenceValue(getActivity(),
				// R.string.preference_key_start_time,
				// getResources().getInteger(R.integer.start_time_default_value),
				// (long) getResources().getInteger(R.integer.start_time_min_value),
				// (long) getResources().getInteger(R.integer.start_time_max_value));
				// FragmentEditTaskPartMain.getTodayStartTime(startTime);
				// int minutes = (int) (milliseconds / (1000 * 60) % 60);
				// int hours = (int) (milliseconds / (1000 * 60 * 60));
			} else {
				long taskDuration = Helper.getLongPreferenceValue(context,
						R.string.preference_key_task_duration,
						resources.getInteger(R.integer.task_duration_default_value),
						(long) resources.getInteger(R.integer.task_duration_min_value),
						(long) resources.getInteger(R.integer.task_duration_max_value));
				if (endDateTime == null) {
					endDateTime = startDateTime + taskDuration;
				}
				if (startDateTime > endDateTime) {
					isCollected = false;
					wantingItemSet
							.add(WantingItem.START_DATE_TIME_TO_BE_BEFORE_END_DATE_TIME);
				} else {
					long taskLength = endDateTime - startDateTime;
					if (recurrenceInterval.getTimeUnitsCountMustBe() == null) {
						long taskPeriod = FragmentEditTaskPartMain
								.measureTaskPeriod(recurrenceInterval);
						if (taskLength > taskPeriod) {
							isCollected = false;
							wantingItemSet
									.add(WantingItem.RECURRENCE_INTERVAL_TO_BE_GREATER_THAN_TASK_LENGTH);
						} else {
							taskUiData.setStartDateTime(startDateTime);
							taskUiData.setEndDateTime(endDateTime);
							taskUiData.setRepetitionEndDateTime(repetitionEndDateTime);
						}
					}
				}
			}
			return isCollected;
		}

		private boolean isRecurrenceDataCompleteForOccurrences(Context context) {
			boolean isCollected = true;
			if (!isRecurrenceDataCompleteForTimeUnitsCount()
					|| !isRecurrenceDataCompleteForOccurrencesMaxCount()
					|| !isRecurrenceDataCompleteForStartDateTime(context)) {
				isCollected = false;
			}
			if (recurrenceInterval.getTaskOccurrences().size() == 0
					&& recurrenceInterval.getTimeUnitsCount() > 1
					|| recurrenceInterval.getTaskOccurrences().size() == 1
					&& recurrenceInterval == RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY) {
				isCollected = false;
				wantingItemSet.add(recurrenceInterval.getWantingItemOccurrences());
			}
			return isCollected;
		}

		protected UserInterfaceData(Parcel in) {
			taskUiData = (TaskUiData) in.readValue(TaskUiData.class.getClassLoader());
			if (in.readByte() == 0x01) {
				wantingItemSet = new HashSet<WantingItem>();
				int size = in.readInt();
				for (int i = 0; i < size; i++) {
					wantingItemSet.add(in.readByte() == 0x00 ? null : WantingItem
							.fromInt((short) in.readInt()));
				}
			} else {
				wantingItemSet = null;
			}
			if (in.readByte() == 0x01) {
				wantingItemSetForTask = new HashSet<WantingItem>();
				int size = in.readInt();
				for (int i = 0; i < size; i++) {
					wantingItemSetForTask.add(in.readByte() == 0x00 ? null : WantingItem
							.fromInt((short) in.readInt()));
				}
			} else {
				wantingItemSetForTask = null;
			}
			textTaskName = (CharSequence) in.readValue(CharSequence.class
					.getClassLoader());
			textTimeUnitsCount = (CharSequence) in.readValue(CharSequence.class
					.getClassLoader());
			textTimeUnitsCountHourlyRecurrent = (CharSequence) in
					.readValue(CharSequence.class.getClassLoader());
			textTimeUnitsCountDailyRecurrent = (CharSequence) in
					.readValue(CharSequence.class.getClassLoader());
			textTimeUnitsCountWeeklyRecurrent = (CharSequence) in
					.readValue(CharSequence.class.getClassLoader());
			textTimeUnitsCountMonthlyRecurrentOnDate = (CharSequence) in
					.readValue(CharSequence.class.getClassLoader());
			textTimeUnitsCountMonthlyRecurrentOnNthWeekDay = (CharSequence) in
					.readValue(CharSequence.class.getClassLoader());
			textTimeUnitsCountYearlyRecurrent = (CharSequence) in
					.readValue(CharSequence.class.getClassLoader());
			textOccurrencesMaxCount = (CharSequence) in.readValue(CharSequence.class
					.getClassLoader());
			timeUnitDefaultValue = in.readInt();
			recurrenceIntervalValue = (short) in.readInt();
			spinnerWeekDayNumberInMonthPosition = in.readInt();
			weekDayNumberInMonth = in.readInt();
			weekDayNumberInMonthDefaultValue = in.readInt();
			recurrenceInterval = in.readByte() == 0x00 ? null : RecurrenceInterval
					.fromInt((short) in.readInt());
			monthRecurrenceMode = in.readByte() == 0x00 ? null : MonthRecurrenceMode
					.fromInt(in.readByte());
			radiogroupMonthBasedRecurrenceMode = in.readInt();
			id = in.readByte() == 0x00 ? null : in.readLong();
			taskName = in.readString();
			if (in.readByte() == 0x01) {
				calendar2s = new ArrayList<Calendar2>();
				in.readList(calendar2s, Calendar2.class.getClassLoader());
			} else {
				calendar2s = null;
			}
			calendar2 = (Calendar2) in.readValue(Calendar2.class.getClassLoader());
			parentId = in.readByte() == 0x00 ? null : in.readLong();
			is24HourFormat = in.readByte() != 0x00;
			startTimeRequiredAction = in.readByte() == 0x00 ? null
					: StartTimeRequiredAction.fromInt((short) in.readInt());
			isRecurrenceDataComplete = in.readByte() != 0x00;
			startTime = in.readLong();
			startDateTimePreviousValue = in.readLong();
			endDateTimePreviousValue = in.readLong();
			repetitionEndDateTimePreviousValue = in.readLong();
			color = in.readByte() == 0x00 ? null : in.readInt();
			priority = (short) in.readInt();
			startDateTime = in.readByte() == 0x00 ? null : in.readLong();
			endDateTime = in.readByte() == 0x00 ? null : in.readLong();
			percentOfCompletion = (short) in.readInt();
			completedTime = in.readByte() == 0x00 ? null : in.readLong();
			sortOrder = in.readInt();
			taskDescription = in.readString();
			timeUnitsCount = in.readByte() == 0x00 ? null : in.readInt();
			timeUnitsStartingIndexValue = in.readByte();
			occurrencesMaxCount = in.readByte() == 0x00 ? null : in.readInt();
			occurrencesMaxCountDefaultValue = in.readInt();
			occurrencesMaxCountMinValue = in.readInt();
			occurrencesMaxCountMaxValue = in.readInt();
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
				reminderUiDataList = new ArrayList<ReminderUiData>();
				in.readList(reminderUiDataList, ReminderUiData.class.getClassLoader());
			} else {
				reminderUiDataList = null;
			}
			timeUnitsCountMustBe = in.readByte() == 0x00 ? null : ValueMustBe.fromInt(in
					.readByte());
			syncPolicy = in.readByte() == 0x00 ? null : SyncPolicy.fromInt(in.readByte());
			markSyncNeededPolicy = in.readByte() == 0x00 ? null : MarkSyncNeededPolicy
					.fromInt(in.readByte());
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeValue(taskUiData);
			if (wantingItemSet == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(wantingItemSet.size());
				Iterator<WantingItem> iterator = wantingItemSet.iterator();
				while (iterator.hasNext()) {
					WantingItem entry = iterator.next();
					if (entry == null) {
						dest.writeByte((byte) 0x00);
					} else {
						dest.writeByte((byte) 0x01);
						dest.writeInt(entry.value);
					}
				}
			}
			if (wantingItemSetForTask == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(wantingItemSetForTask.size());
				Iterator<WantingItem> iterator = wantingItemSetForTask.iterator();
				while (iterator.hasNext()) {
					WantingItem entry = iterator.next();
					if (entry == null) {
						dest.writeByte((byte) 0x00);
					} else {
						dest.writeByte((byte) 0x01);
						dest.writeInt(entry.value);
					}
				}
			}
			dest.writeValue(textTaskName);
			dest.writeValue(textTimeUnitsCount);
			dest.writeValue(textTimeUnitsCountHourlyRecurrent);
			dest.writeValue(textTimeUnitsCountDailyRecurrent);
			dest.writeValue(textTimeUnitsCountWeeklyRecurrent);
			dest.writeValue(textTimeUnitsCountMonthlyRecurrentOnDate);
			dest.writeValue(textTimeUnitsCountMonthlyRecurrentOnNthWeekDay);
			dest.writeValue(textTimeUnitsCountYearlyRecurrent);
			dest.writeValue(textOccurrencesMaxCount);
			dest.writeInt(timeUnitDefaultValue);
			dest.writeInt(recurrenceIntervalValue);
			dest.writeInt(spinnerWeekDayNumberInMonthPosition);
			dest.writeInt(weekDayNumberInMonth);
			dest.writeInt(weekDayNumberInMonthDefaultValue);
			if (recurrenceInterval == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(recurrenceInterval.getValue());
			}
			if (monthRecurrenceMode == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(monthRecurrenceMode.getValue());
			}
			dest.writeInt(radiogroupMonthBasedRecurrenceMode);
			if (id == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeLong(id);
			}
			dest.writeString(taskName);
			if (calendar2s == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(calendar2s);
			}
			dest.writeValue(calendar2);
			if (parentId == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeLong(parentId);
			}
			dest.writeByte((byte) (is24HourFormat ? 0x01 : 0x00));
			if (startTimeRequiredAction == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(startTimeRequiredAction.getValue());
			}
			dest.writeByte((byte) (isRecurrenceDataComplete ? 0x01 : 0x00));
			dest.writeLong(startTime);
			dest.writeLong(startDateTimePreviousValue);
			dest.writeLong(endDateTimePreviousValue);
			dest.writeLong(repetitionEndDateTimePreviousValue);
			if (color == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(color);
			}
			dest.writeInt(priority);
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
			dest.writeInt(percentOfCompletion);
			if (completedTime == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeLong(completedTime);
			}
			dest.writeInt(sortOrder);
			dest.writeString(taskDescription);
			if (timeUnitsCount == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(timeUnitsCount);
			}
			dest.writeByte(timeUnitsStartingIndexValue);
			if (occurrencesMaxCount == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(occurrencesMaxCount);
			}
			dest.writeInt(occurrencesMaxCountDefaultValue);
			dest.writeInt(occurrencesMaxCountMinValue);
			dest.writeInt(occurrencesMaxCountMaxValue);
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
			if (reminderUiDataList == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(reminderUiDataList);
			}
			if (timeUnitsCountMustBe == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(timeUnitsCountMustBe.value);
			}
			if (syncPolicy == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(syncPolicy.getValue());
			}
			if (markSyncNeededPolicy == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(markSyncNeededPolicy.getValue());
			}
		}

		public static final Parcelable.Creator<UserInterfaceData> CREATOR = new Parcelable.Creator<UserInterfaceData>() {
			@Override
			public UserInterfaceData createFromParcel(Parcel in) {
				return new UserInterfaceData(in);
			}

			@Override
			public UserInterfaceData[] newArray(int size) {
				return new UserInterfaceData[size];
			}
		};
	}

	private class TimePickerCallerAttribute implements
			Comparable<TimePickerCallerAttribute> {
		private int year;
		private int monthOfYear;
		private int dayOfMonth;
		private int timeCallerId;
		private int dateCallerId;
		private CheckBox checkboxDate;
		private CheckBox checkboxTime;
		private TaskDate taskDate;
		private int textId;
		private long milliseconds;
		private int ordinalNumber;
		private boolean is24HourMode;
		private boolean isHourModeChangeable;
		private String timeCallerKey;
		private String dateCallerKey;
		private Calendar calendar;

		public TimePickerCallerAttribute(CompoundButton checkbox) {
			timeCallerId = dateCallerId = checkbox.getId();
		}

		public TimePickerCallerAttribute(int dateCallerId) {
			this.dateCallerId = dateCallerId;
		}

		public TimePickerCallerAttribute(TaskDate taskDate, int textId,
				int ordinalNumber, boolean is24HourMode, boolean isHourModeChangeable) {
			this.taskDate = taskDate;
			this.textId = textId;
			this.ordinalNumber = ordinalNumber;
			this.is24HourMode = is24HourMode;
			this.isHourModeChangeable = isHourModeChangeable;
			initialize();
		}

		private void initialize() {
			checkboxTime = taskDate.getTimeCheckBox();
			checkboxDate = taskDate.getDateCheckBox();
			timeCallerId = checkboxTime.getId();
			dateCallerId = checkboxDate.getId();
			timeCallerKey = String.valueOf(timeCallerId);
			dateCallerKey = String.valueOf(dateCallerId);
			Long startDateTime = taskDate.getTaskDateTime();
			if (startDateTime != null) {
				milliseconds = startDateTime.longValue();
			} else {
				milliseconds = taskDate.retrievePreviousTimeValue();
			}
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(milliseconds);
			year = calendar.get(Calendar.YEAR);
			monthOfYear = calendar.get(Calendar.MONTH);
			dayOfMonth = calendar.get(Calendar.DATE);
			int hours = calendar.get(Calendar.HOUR_OF_DAY);
			int minutes = calendar.get(Calendar.MINUTE);
			milliseconds = (hours * 60 + minutes) * 60 * 1000;
		}

		@Override
		public int compareTo(TimePickerCallerAttribute another) {
			if (ordinalNumber < another.ordinalNumber) {
				return -1;
			} else if (ordinalNumber > another.ordinalNumber) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private class MultipleTimeSetListener implements OnMultipleTimeSetListener,
			OnDateSetListener {
		private ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes;
		private CallerAttributeComparatorByDateCallerId callerAttributeComparatorByDateCallerId;

		public MultipleTimeSetListener(
				ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes) {
			this.timePickerCallerAttributes = timePickerCallerAttributes;
		}

		@Override
		public void onTimeSet(RadialPickerLayout view, Bundle bundle,
				ArrayList<TimeAttribute> timeAttributes, int ordinalNumber) {
			int size = timeAttributes.size();
			Collections.sort(timeAttributes);
			Collections.sort(timePickerCallerAttributes);
			for (int i = 0; i < size; i++) {
				TimeAttribute timeAttribute = timeAttributes.get(i);
				TimePickerCallerAttribute timePickerCallerAttribute = timePickerCallerAttributes
						.get(i);
				int hours = timeAttribute.getHours();
				int minutes = timeAttribute.getMinutes();
				long milliseconds = (hours * 60 + minutes) * 60 * 1000;
				timePickerCallerAttribute.milliseconds = milliseconds;
				Calendar calendar = timePickerCallerAttribute.calendar;
				calendar.set(Calendar.HOUR_OF_DAY, hours);
				calendar.set(Calendar.MINUTE, minutes);
				timePickerCallerAttribute.taskDate.setTaskDateTime(calendar
						.getTimeInMillis());
				setupDateTimeCheckboxes(timePickerCallerAttribute.taskDate);
			}
		}

		@Override
		public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
				int dayOfMonth) {
			TimePickerCallerAttribute timePickerCallerAttribute = new TimePickerCallerAttribute(
					dateCallerId);
			Collections.sort(timePickerCallerAttributes,
					callerAttributeComparatorByDateCallerId);
			int index = Collections.binarySearch(timePickerCallerAttributes,
					timePickerCallerAttribute, callerAttributeComparatorByDateCallerId);
			timePickerCallerAttribute = timePickerCallerAttributes.get(index);
			timePickerCallerAttribute.year = year;
			timePickerCallerAttribute.monthOfYear = monthOfYear;
			timePickerCallerAttribute.dayOfMonth = dayOfMonth;
			Calendar calendar = timePickerCallerAttribute.calendar;
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, monthOfYear);
			calendar.set(Calendar.DATE, dayOfMonth);
			timePickerCallerAttribute.taskDate
					.setTaskDateTime(calendar.getTimeInMillis());
			setupDateTimeCheckboxes(timePickerCallerAttribute.taskDate);
		}

		@Override
		public boolean isTimeConsistent(ArrayList<TimeAttribute> timeAttributes,
				Bundle bundle) {
			return true;
		}
	}

	private class CallerAttributeComparatorByDateCallerId implements
			Comparator<TimePickerCallerAttribute>, Serializable {
		private static final long serialVersionUID = 2L;

		@Override
		public int compare(TimePickerCallerAttribute lhs, TimePickerCallerAttribute rhs) {
			if (lhs.dateCallerId < rhs.dateCallerId) {
				return -1;
			} else if (lhs.dateCallerId > rhs.dateCallerId) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private class MultipleTimePickerCaller implements OnCheckedChangeListener {
		private ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes;
		private ArrayList<TimeAttribute> timeAttributes;
		private MultipleTimeSetListener multipleTimeSetListener;
		private int titleId;
		private CallerAttributeComparatorByTimeCallerId callerAttributeComparatorByTimeCallerId;
		private CallerAttributeComparatorByDateCallerId callerAttributeComparatorByDateCallerId;

		public MultipleTimePickerCaller(
				ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes,
				MultipleTimeSetListener multipleTimeSetListener, int titleId) {
			this.timePickerCallerAttributes = timePickerCallerAttributes;
			this.multipleTimeSetListener = multipleTimeSetListener;
			this.titleId = titleId;
			initialize();
		}

		private void initialize() {
			callerAttributeComparatorByTimeCallerId = new CallerAttributeComparatorByTimeCallerId();
			callerAttributeComparatorByDateCallerId = new CallerAttributeComparatorByDateCallerId();
			timeAttributes = new ArrayList<TimeAttribute>(
					timePickerCallerAttributes.size());
			multipleTimeSetListener.callerAttributeComparatorByDateCallerId = callerAttributeComparatorByDateCallerId;
		}

		private class CallerAttributeComparatorByTimeCallerId implements
				Comparator<TimePickerCallerAttribute>, Serializable {
			private static final long serialVersionUID = 1L;

			@Override
			public int compare(TimePickerCallerAttribute lhs,
					TimePickerCallerAttribute rhs) {
				if (lhs.timeCallerId < rhs.timeCallerId) {
					return -1;
				} else if (lhs.timeCallerId > rhs.timeCallerId) {
					return 1;
				} else {
					return 0;
				}
			}
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			TimePickerCallerAttribute timePickerCallerAttribute = new TimePickerCallerAttribute(
					buttonView);
			Collections.sort(timePickerCallerAttributes,
					callerAttributeComparatorByTimeCallerId);
			int index = Collections.binarySearch(timePickerCallerAttributes,
					timePickerCallerAttribute, callerAttributeComparatorByTimeCallerId);
			if (index >= 0) {
				resetTime();
				timePickerCallerAttribute = timePickerCallerAttributes.get(index);
				int ordinalNumber = timePickerCallerAttribute.ordinalNumber;
				if (isChecked) {
					buttonView.setChecked(false);
				} else {
					buttonView.setChecked(true);
				}
				Bundle bundle = new Bundle();
				bundle.putInt("callerId", timePickerCallerAttribute.timeCallerId);
				TimePickerDialogMultiple tpd = TimePickerDialogMultiple.newInstance(
						multipleTimeSetListener, bundle, titleId, timeAttributes,
						ordinalNumber);
				tpd.show(getFragmentManager(), timePickerCallerAttribute.timeCallerKey);
			} else {
				Collections.sort(timePickerCallerAttributes,
						callerAttributeComparatorByDateCallerId);
				index = Collections.binarySearch(timePickerCallerAttributes,
						timePickerCallerAttribute,
						callerAttributeComparatorByDateCallerId);
				timePickerCallerAttribute = timePickerCallerAttributes.get(index);
				dateCallerId = timePickerCallerAttribute.dateCallerId;
				if (isChecked) {
					buttonView.setChecked(false);
					DatePickerDialog dpd = DatePickerDialog.newInstance(
							multipleTimeSetListener, timePickerCallerAttribute.year,
							timePickerCallerAttribute.monthOfYear,
							timePickerCallerAttribute.dayOfMonth);
					dpd.show(getFragmentManager(),
							timePickerCallerAttribute.dateCallerKey);
				} else {
					timePickerCallerAttribute.taskDate.setTaskDateTime(null);
					timePickerCallerAttribute.taskDate.tellDateNotBeingSet();
					timePickerCallerAttribute.taskDate.setTimeCheckBoxChecked(false);
					timePickerCallerAttribute.taskDate.tellTimeNotBeingSet();
				}
			}
		}

		private void resetTime() {
			int size = timeAttributes.size();
			Collections.sort(timeAttributes);
			Collections.sort(timePickerCallerAttributes);
			for (int i = 0; i < size; i++) {
				TimePickerCallerAttribute timePickerCallerAttribute = timePickerCallerAttributes
						.get(i);
				int minutes = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60) % 60);
				int hours = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60 * 60));
				TimeAttribute timeAttribute = timeAttributes.get(i);
				timeAttribute.setHours(hours);
				timeAttribute.setMinutes(minutes);
				int amOrPm = hours < 12 ? TimePickerDialogMultiple.AM
						: TimePickerDialogMultiple.PM;
				timeAttribute.setAmOrPm(amOrPm);
			}
		}

		private void updateHourMode(boolean is24HourMode) {
			int size = timeAttributes.size();
			for (int i = 0; i < size; i++) {
				TimeAttribute timeAttribute = timeAttributes.get(i);
				timeAttribute.setIs24HourMode(is24HourMode);
			}
		}
	}

	class BroadcastReceiverForFragmentEditTaskPartMain extends BroadcastReceiver {
		private ArrayList<MultipleTimePickerCaller> timePickerCallers;
		private IntentFilter intentFilter;

		public BroadcastReceiverForFragmentEditTaskPartMain(Context context) {
			super();
			timePickerCallers = new ArrayList<MultipleTimePickerCaller>(3);
			intentFilter = new IntentFilter();
			intentFilter
					.addAction(CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_MONTH_RECURRENCE_MODE_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_START_TIME_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_BUSINESS_HOURS_START_TIME_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_BUSINESS_HOURS_END_TIME_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_START_TIME_REQUIRED_ACTION_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_RINGTONE_FADE_IN_TIME_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_AUTOMATIC_SNOOZE_DURATION_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_AUTOMATIC_SNOOZES_MAX_COUNT_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_SYNC_POLICY_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED);
			LocalBroadcastManager.getInstance(context).registerReceiver(this,
					intentFilter);
		}

		private void addTimePickerCaller(MultipleTimePickerCaller multipleTimePickerCaller) {
			timePickerCallers.add(multipleTimePickerCaller);
		}

		public void addAction(String action) {
			intentFilter.addAction(action);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED)) {
				is24HourFormat = Helper.is24HourFormat(context);
				int size = timePickerCallers.size();
				for (int i = 0; i < size; i++) {
					MultipleTimePickerCaller multipleTimePickerCaller = timePickerCallers
							.get(i);
					multipleTimePickerCaller.updateHourMode(is24HourFormat);
				}
			} else if (intent.getAction().equals(
					CommonConstants.ACTION_MONTH_RECURRENCE_MODE_CHANGED)) {
				if (isAdded()) {
					MonthRecurrenceMode
							.initializeRecurrenceIntervalToMonthRecurrenceModeMap(context);
				}
			} else if (intent.getAction().equals(
					CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED)) {
				if (isAdded()) {
					is24HourFormat = Helper.is24HourFormat(context);
				}
			} else if (intent.getAction().equals(
					CommonConstants.ACTION_START_TIME_CHANGED)) {
				if (isAdded()) {
					Resources resources = context.getResources();
					startTime = Helper.getLongPreferenceValue(context,
							R.string.preference_key_start_time,
							resources.getInteger(R.integer.start_time_default_value),
							(long) resources.getInteger(R.integer.start_time_min_value),
							(long) resources.getInteger(R.integer.start_time_max_value));
				}
			} else if (intent.getAction().equals(
					CommonConstants.ACTION_START_TIME_REQUIRED_ACTION_CHANGED)) {
				if (isAdded()) {
					startTimeRequiredAction = StartTimeRequiredAction
							.fromInt((short) Helper
									.getIntegerPreferenceValueFromStringArray(
											context,
											R.string.preference_key_start_time_required_action,
											R.array.start_time_required_action_values_array,
											R.integer.start_time_required_action_default_value));
				}
			}
		}
	}

	private void initializeMultipleTimePickerCaller(
			ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes, int titleId) {
		MultipleTimeSetListener multipleTimeSetListener = new MultipleTimeSetListener(
				timePickerCallerAttributes);
		MultipleTimePickerCaller multipleTimePickerCaller = new MultipleTimePickerCaller(
				timePickerCallerAttributes, multipleTimeSetListener, titleId);
		ArrayList<TimeAttribute> timeAttributes = multipleTimePickerCaller.timeAttributes;
		int size = timePickerCallerAttributes.size();
		for (int i = 0; i < size; i++) {
			TimePickerCallerAttribute timePickerCallerAttribute = timePickerCallerAttributes
					.get(i);
			timePickerCallerAttribute.taskDate
					.setOnCheckedChangeListener(multipleTimePickerCaller);
			int minutes = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60) % 60);
			int hours = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60 * 60));
			TimeAttribute timeAttribute = new TimeAttribute(hours, minutes,
					timePickerCallerAttribute.textId,
					timePickerCallerAttribute.ordinalNumber,
					timePickerCallerAttribute.is24HourMode,
					timePickerCallerAttribute.isHourModeChangeable);
			timeAttributes.add(timeAttribute);
			setupDateTimeCheckboxes(timePickerCallerAttribute.taskDate);
			TimePickerDialogMultiple tpd = (TimePickerDialogMultiple) getFragmentManager()
					.findFragmentByTag(timePickerCallerAttribute.timeCallerKey);
			if (tpd != null) {
				tpd.setOnTimeSetListener(multipleTimeSetListener);
			}
			DatePickerDialog dpd = (DatePickerDialog) getFragmentManager()
					.findFragmentByTag(timePickerCallerAttribute.dateCallerKey);
			if (dpd != null) {
				dpd.setOnDateSetListener(multipleTimeSetListener);
			}
		}
		mReceiver.addTimePickerCaller(multipleTimePickerCaller);
	}

	private class TaskStartDate implements TaskDate {
		private CheckBox checkboxDate;
		private CheckBox checkboxTime;
		private OnCheckedChangeListener onCheckedChangeListener;

		public TaskStartDate() {
			checkboxDate = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_checkbox_task_start_date);
			checkboxTime = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_checkbox_task_start_time);
		}

		@Override
		public void setOnCheckedChangeListener(
				OnCheckedChangeListener onCheckedChangeListener) {
			this.onCheckedChangeListener = onCheckedChangeListener;
		}

		@Override
		public void requestFocus() {
			checkboxDate.requestFocus();
		}

		@Override
		public Long retrievePreviousDateValue() {
			return startDateTimePreviousValue;
		}

		@Override
		public void savePreviousDateValue() {
			if (mUserInterfaceData.startDateTime != null) {
				startDateTimePreviousValue = mUserInterfaceData.startDateTime;
			}
		}

		@Override
		public void tellDateNotBeingSet() {
			checkboxDate
					.setText(R.string.fragment_edit_task_part_main_checkbox_task_start_date_not_set);
		}

		@Override
		public CheckBox getTimeCheckBox() {
			return checkboxTime;
		}

		@Override
		public void tellTimeNotBeingSet() {
			checkboxTime
					.setText(R.string.fragment_edit_task_part_main_checkbox_task_start_time_not_set);
		}

		@Override
		public void setTaskDateTime(Long dateTime) {
			mUserInterfaceData.startDateTime = dateTime;
			taskUiData.setStartDateTime(dateTime);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
					new Intent(CommonConstants.ACTION_START_TIME_SELECTED));
		}

		@Override
		public CheckBox getDateCheckBox() {
			return checkboxDate;
		}

		@Override
		public long retrievePreviousTimeValue() {
			return startDateTimePreviousValue;
		}

		@Override
		public void displayMessageSelectDateFirst() {
			Toast.makeText(
					getActivity().getApplicationContext(),
					R.string.fragment_edit_task_part_main_checkbox_task_start_time_please_select_date_first,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void savePreviousTimeValue() {
			if (mUserInterfaceData.startDateTime != null) {
				startDateTimePreviousValue = mUserInterfaceData.startDateTime;
			}
		}

		@Override
		public Calendar fillCalendarWithTimeValue(Calendar calendar) {
			calendar.setTimeInMillis(mUserInterfaceData.startDateTime);
			return calendar;
		}

		@Override
		public Calendar fillCalendarWithPreviousTimeValue(Calendar calendar) {
			Long dateTimeUtc0 = retrievePreviousDateValue();
			if (dateTimeUtc0 != null) {
				calendar.setTimeInMillis(dateTimeUtc0);
			}
			return calendar;
		}

		@Override
		public Long getTaskDateTime() {
			return mUserInterfaceData.startDateTime;
		}

		@Override
		public void setDateCheckBoxText(String dateString) {
			checkboxDate.setText(dateString);
		}

		@Override
		public void setDateCheckBoxText(Long dateTimeUtc0) {
			if (dateTimeUtc0 != null) {
				// TimeZone timeZoneSelectedInSpinner = getTimeZoneSelectedInSpinner();
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
				// dateFormat.setTimeZone(timeZoneSelectedInSpinner);
				String dateString = dateFormat.format(new Date(dateTimeUtc0));
				setDateCheckBoxText(dateString);
				setDateCheckBoxChecked(true);
			} else {
				tellDateNotBeingSet();
				setDateCheckBoxChecked(false);
			}
		}

		@Override
		public void setTimeCheckBoxText(String timeString) {
			checkboxTime.setText(timeString);
		}

		@Override
		public void setTimeCheckBoxText(Long dateTimeUtc0) {
			if (dateTimeUtc0 != null) {
				// TimeZone timeZoneSelectedInSpinner = getTimeZoneSelectedInSpinner();
				DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
				// timeFormat.setTimeZone(timeZoneSelectedInSpinner);
				String timeString = timeFormat.format(new Date(dateTimeUtc0));
				setTimeCheckBoxText(timeString);
				setTimeCheckBoxChecked(true);
			} else {
				tellTimeNotBeingSet();
				setTimeCheckBoxChecked(false);
			}
		}

		@Override
		public void setDateCheckBoxChecked(boolean checked) {
			checkboxDate.setOnCheckedChangeListener(null);
			checkboxDate.setChecked(checked);
			checkboxDate.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		@Override
		public void setTimeCheckBoxChecked(boolean checked) {
			checkboxTime.setOnCheckedChangeListener(null);
			checkboxTime.setChecked(checked);
			checkboxTime.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		public TaskStartDate(Parcel in) {
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}

		@SuppressWarnings("unused")
		public final Parcelable.Creator<TaskStartDate> CREATOR = new Parcelable.Creator<TaskStartDate>() {
			@Override
			public TaskStartDate createFromParcel(Parcel in) {
				return new TaskStartDate(in);
			}

			@Override
			public TaskStartDate[] newArray(int size) {
				return new TaskStartDate[size];
			}
		};
	}

	private class TaskEndDate implements TaskDate {
		private CheckBox checkboxDate;
		private CheckBox checkboxTime;
		private OnCheckedChangeListener onCheckedChangeListener;

		public TaskEndDate() {
			checkboxDate = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_checkbox_task_end_date);
			checkboxTime = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_checkbox_task_end_time);
		}

		@Override
		public void setOnCheckedChangeListener(
				OnCheckedChangeListener onCheckedChangeListener) {
			this.onCheckedChangeListener = onCheckedChangeListener;
		}

		@Override
		public Long retrievePreviousDateValue() {
			return endDateTimePreviousValue;
		}

		@Override
		public void savePreviousDateValue() {
			if (mUserInterfaceData.endDateTime != null) {
				endDateTimePreviousValue = mUserInterfaceData.endDateTime;
			}
		}

		@Override
		public void tellDateNotBeingSet() {
			checkboxDate
					.setText(R.string.fragment_edit_task_part_main_checkbox_task_end_date_not_set);
		}

		@Override
		public CheckBox getTimeCheckBox() {
			return checkboxTime;
		}

		@Override
		public void tellTimeNotBeingSet() {
			checkboxTime
					.setText(R.string.fragment_edit_task_part_main_checkbox_task_end_time_not_set);
		}

		@Override
		public void setTaskDateTime(Long dateTime) {
			mUserInterfaceData.endDateTime = dateTime;
			taskUiData.setEndDateTime(dateTime);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
					new Intent(CommonConstants.ACTION_END_TIME_SELECTED));
		}

		@Override
		public CheckBox getDateCheckBox() {
			return checkboxDate;
		}

		@Override
		public long retrievePreviousTimeValue() {
			return endDateTimePreviousValue;
		}

		@Override
		public void displayMessageSelectDateFirst() {
			Toast.makeText(
					getActivity().getApplicationContext(),
					R.string.fragment_edit_task_part_main_checkbox_task_end_time_please_select_date_first,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void savePreviousTimeValue() {
			if (mUserInterfaceData.endDateTime != null) {
				endDateTimePreviousValue = mUserInterfaceData.endDateTime;
			}
		}

		@Override
		public Calendar fillCalendarWithTimeValue(Calendar calendar) {
			calendar.setTimeInMillis(mUserInterfaceData.endDateTime);
			return calendar;
		}

		@Override
		public Long getTaskDateTime() {
			return mUserInterfaceData.endDateTime;
		}

		@Override
		public void setDateCheckBoxText(String dateString) {
			checkboxDate.setText(dateString);
		}

		@Override
		public void setTimeCheckBoxText(String timeString) {
			checkboxTime.setText(timeString);
		}

		@Override
		public void setDateCheckBoxChecked(boolean checked) {
			checkboxDate.setOnCheckedChangeListener(null);
			checkboxDate.setChecked(checked);
			checkboxDate.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		@Override
		public void setTimeCheckBoxChecked(boolean checked) {
			CheckBox checkBox = checkboxTime;
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(checked);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		@Override
		public void setDateCheckBoxText(Long dateTimeUtc0) {
			if (dateTimeUtc0 != null) {
				// TimeZone timeZoneSelectedInSpinner = getTimeZoneSelectedInSpinner();
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
				// dateFormat.setTimeZone(timeZoneSelectedInSpinner);
				String dateString = dateFormat.format(new Date(dateTimeUtc0));
				setDateCheckBoxText(dateString);
				setDateCheckBoxChecked(true);
			} else {
				tellDateNotBeingSet();
				setDateCheckBoxChecked(false);
			}
		}

		@Override
		public void setTimeCheckBoxText(Long dateTimeUtc0) {
			if (dateTimeUtc0 != null) {
				// TimeZone timeZoneSelectedInSpinner = getTimeZoneSelectedInSpinner();
				DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
				// timeFormat.setTimeZone(timeZoneSelectedInSpinner);
				String timeString = timeFormat.format(new Date(dateTimeUtc0));
				setTimeCheckBoxText(timeString);
				setTimeCheckBoxChecked(true);
			} else {
				tellTimeNotBeingSet();
				setTimeCheckBoxChecked(false);
			}
		}

		@Override
		public Calendar fillCalendarWithPreviousTimeValue(Calendar calendar) {
			Long dateTimeUtc0 = retrievePreviousDateValue();
			if (dateTimeUtc0 != null) {
				calendar.setTimeInMillis(dateTimeUtc0);
			}
			return calendar;
		}

		@Override
		public void requestFocus() {
			checkboxDate.requestFocus();
		}

		public TaskEndDate(Parcel in) {
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}

		@SuppressWarnings("unused")
		public final Parcelable.Creator<TaskEndDate> CREATOR = new Parcelable.Creator<TaskEndDate>() {
			@Override
			public TaskEndDate createFromParcel(Parcel in) {
				return new TaskEndDate(in);
			}

			@Override
			public TaskEndDate[] newArray(int size) {
				return new TaskEndDate[size];
			}
		};
	}

	private class TaskRepetitionEndDate implements TaskDate {
		private CheckBox checkboxDate;
		private CheckBox checkboxTime;
		private OnCheckedChangeListener onCheckedChangeListener;

		public TaskRepetitionEndDate() {
			checkboxDate = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_checkbox_task_repetition_end_date);
			checkboxTime = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_checkbox_task_repetition_end_time);
		}

		@Override
		public void setOnCheckedChangeListener(
				OnCheckedChangeListener onCheckedChangeListener) {
			this.onCheckedChangeListener = onCheckedChangeListener;
		}

		@Override
		public Long retrievePreviousDateValue() {
			return repetitionEndDateTimePreviousValue;
		}

		@Override
		public void savePreviousDateValue() {
			if (mUserInterfaceData.repetitionEndDateTime != null) {
				repetitionEndDateTimePreviousValue = mUserInterfaceData.repetitionEndDateTime;
			}
		}

		@Override
		public void tellDateNotBeingSet() {
			checkboxDate
					.setText(R.string.fragment_edit_task_part_main_checkbox_task_repetition_end_date_not_set);
		}

		@Override
		public CheckBox getTimeCheckBox() {
			return checkboxTime;
		}

		@Override
		public void tellTimeNotBeingSet() {
			checkboxTime
					.setText(R.string.fragment_edit_task_part_main_checkbox_task_repetition_end_time_not_set);
		}

		@Override
		public void setTaskDateTime(Long dateTime) {
			mUserInterfaceData.repetitionEndDateTime = dateTime;
			taskUiData.setRepetitionEndDateTime(dateTime);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
					new Intent(CommonConstants.ACTION_REPETITION_END_TIME_SELECTED));
		}

		@Override
		public CheckBox getDateCheckBox() {
			return checkboxDate;
		}

		@Override
		public long retrievePreviousTimeValue() {
			return repetitionEndDateTimePreviousValue;
		}

		@Override
		public void displayMessageSelectDateFirst() {
			Toast.makeText(
					getActivity().getApplicationContext(),
					R.string.fragment_edit_task_part_main_checkbox_task_repetition_end_time_please_select_date_first,
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void savePreviousTimeValue() {
			if (mUserInterfaceData.repetitionEndDateTime != null) {
				repetitionEndDateTimePreviousValue = mUserInterfaceData.repetitionEndDateTime;
			}
		}

		@Override
		public Calendar fillCalendarWithTimeValue(Calendar calendar) {
			calendar.setTimeInMillis(mUserInterfaceData.repetitionEndDateTime);
			return calendar;
		}

		@Override
		public Long getTaskDateTime() {
			return mUserInterfaceData.repetitionEndDateTime;
		}

		@Override
		public void setDateCheckBoxText(String dateString) {
			checkboxDate.setText(dateString);
		}

		@Override
		public void setTimeCheckBoxText(String timeString) {
			checkboxTime.setText(timeString);
		}

		@Override
		public void setDateCheckBoxChecked(boolean checked) {
			checkboxDate.setOnCheckedChangeListener(null);
			checkboxDate.setChecked(checked);
			checkboxDate.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		@Override
		public void setTimeCheckBoxChecked(boolean checked) {
			CheckBox checkBox = checkboxTime;
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(checked);
			checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
		}

		@Override
		public void setDateCheckBoxText(Long dateTimeUtc0) {
			if (dateTimeUtc0 != null) {
				// TimeZone timeZoneSelectedInSpinner = getTimeZoneSelectedInSpinner();
				DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);
				// dateFormat.setTimeZone(timeZoneSelectedInSpinner);
				String dateString = dateFormat.format(new Date(dateTimeUtc0));
				setDateCheckBoxText(dateString);
				setDateCheckBoxChecked(true);
			} else {
				tellDateNotBeingSet();
				setDateCheckBoxChecked(false);
			}
		}

		@Override
		public void setTimeCheckBoxText(Long dateTimeUtc0) {
			if (dateTimeUtc0 != null) {
				// TimeZone timeZoneSelectedInSpinner = getTimeZoneSelectedInSpinner();
				DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
				// timeFormat.setTimeZone(timeZoneSelectedInSpinner);
				String timeString = timeFormat.format(new Date(dateTimeUtc0));
				setTimeCheckBoxText(timeString);
				setTimeCheckBoxChecked(true);
			} else {
				tellTimeNotBeingSet();
				setTimeCheckBoxChecked(false);
			}
		}

		@Override
		public Calendar fillCalendarWithPreviousTimeValue(Calendar calendar) {
			Long dateTimeUtc0 = retrievePreviousDateValue();
			if (dateTimeUtc0 != null) {
				calendar.setTimeInMillis(dateTimeUtc0);
			}
			return calendar;
		}

		@Override
		public void requestFocus() {
			checkboxDate.requestFocus();
		}

		public TaskRepetitionEndDate(Parcel in) {
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}

		@SuppressWarnings("unused")
		public final Parcelable.Creator<TaskRepetitionEndDate> CREATOR = new Parcelable.Creator<TaskRepetitionEndDate>() {
			@Override
			public TaskRepetitionEndDate createFromParcel(Parcel in) {
				return new TaskRepetitionEndDate(in);
			}

			@Override
			public TaskRepetitionEndDate[] newArray(int size) {
				return new TaskRepetitionEndDate[size];
			}
		};
	}

	private interface TaskDate extends Parcelable {
		Long retrievePreviousDateValue();

		void savePreviousDateValue();

		void savePreviousTimeValue();

		void tellDateNotBeingSet();

		CheckBox getTimeCheckBox();

		void tellTimeNotBeingSet();

		void setTaskDateTime(Long dateTime);

		CheckBox getDateCheckBox();

		long retrievePreviousTimeValue();

		void displayMessageSelectDateFirst();

		Calendar fillCalendarWithTimeValue(Calendar calendar);

		Long getTaskDateTime();

		void setDateCheckBoxText(String dateString);

		void setTimeCheckBoxText(String timeString);

		void setDateCheckBoxChecked(boolean checked);

		void setTimeCheckBoxChecked(boolean checked);

		void setDateCheckBoxText(Long dateTimeUtc0);

		void setTimeCheckBoxText(Long dateTimeUtc0);

		Calendar fillCalendarWithPreviousTimeValue(Calendar calendar);

		void requestFocus();

		void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener);
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	// private boolean mRepeatSectionInitialized = false;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(CommonConstants.DEBUG_TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Restore state here
		Log.i(CommonConstants.DEBUG_TAG, "onCreate");
		if (savedInstanceState != null) {
			dateCallerId = savedInstanceState
					.getInt(FragmentEditTaskPartMain.KEY_DATE_CALLER_ID);
		}
		// occurrencesPickerCheckBoxOnCheckedChangeListener = new
		// OccurrencesPickerCheckBoxOnCheckedChangeListener();
		occurrencesPickerCheckBoxOnClickListener = new OccurrencesPickerCheckBoxOnClickListener();
		mReceiver = new BroadcastReceiverForFragmentEditTaskPartMain(getActivity()
				.getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Log.i(CommonConstants.DEBUG_TAG, "onCreateView");
		//
		TaskWithDependentsUiDataHolder taskWithDependentsUiDataHolder = (TaskWithDependentsUiDataHolder) getActivity();
		dataFragment = taskWithDependentsUiDataHolder.getmParcelableDataStore();
		taskEditMode = dataFragment.getTaskEditMode();
		taskWithDependentsUiData = taskWithDependentsUiDataHolder
				.getTaskWithDependentsUiData();
		taskWithDependentsUiData.setTaskUiDataWithCurrentStateSupplier(this);
		taskUiData = taskWithDependentsUiData.TaskUiData;
		taskOccurrences = taskWithDependentsUiData.TaskOccurrences;
		mUserInterfaceData = taskWithDependentsUiDataHolder.getUserInterfaceData();
		//
		View v = inflater
				.inflate(R.layout.fragment_edit_task_part_main, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentActivity activity = getActivity();
		scrollView = (ScrollView) activity
				.findViewById(R.id.fragment_edit_task_part_main_scrollview);
		selectedConfiguration = activity.getResources().getString(
				R.string.selected_configuration);
		scrollPos = FragmentEditTaskPartMain.mapStringToScrollPos
				.get(selectedConfiguration);
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				if (scrollPos != null) {
					scrollView.scrollTo(scrollView.getScrollX(), scrollPos);
				}
			}
		});
		scrollView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
		scrollView.setFocusableInTouchMode(true);
		scrollView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				v.requestFocusFromTouch();
				if (FragmentEditTaskPartMain.OnTouchDebug) {
					Log.d(FragmentEditTaskPartMain.OnTouchDebugTag, String.format(
							"onTouch thread: %s", Thread.currentThread().getName()));
				}
				return false;
			}
		});
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		// setup listeners and set values on views
		restoreMajorSegmentForTask();
		restoreRecurrentSegment();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (setupUiPending) {
			setupUi(setupUiTaskUiData2);
			setupUiPending = false;
			setupUiTaskUiData2 = null;
		}
	}

	private void restoreMajorSegmentForTask() {
		restoreTaskNameSegmentForTask();
		// setup CheckBox parent entity
		restoreParentSegmentForTask();
		restoreSortOrderSegmentForTask();
		// setup PercentOfCompletion field
		restorePercentOfCompletionSegmentForTask();
		restorePrioritySegmentForTask();
		is24HourFormat = mUserInterfaceData.is24HourFormat;
		startTime = mUserInterfaceData.startTime;
		startTimeRequiredAction = mUserInterfaceData.startTimeRequiredAction;
		isRecurrenceDataComplete = mUserInterfaceData.isRecurrenceDataComplete;
		startDateTimePreviousValue = mUserInterfaceData.startDateTimePreviousValue;
		endDateTimePreviousValue = mUserInterfaceData.endDateTimePreviousValue;
		repetitionEndDateTimePreviousValue = mUserInterfaceData.repetitionEndDateTimePreviousValue;
		restoreColorPickerSegmentForTask();
		ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes = new ArrayList<TimePickerCallerAttribute>();
		taskStartDate = new TaskStartDate();
		TimePickerCallerAttribute timePickerCallerAttribute = new TimePickerCallerAttribute(
				taskStartDate, R.string.time_picker_radiobutton_text_start_time, 1,
				is24HourFormat, true);
		timePickerCallerAttributes.add(timePickerCallerAttribute);
		taskEndDate = new TaskEndDate();
		timePickerCallerAttribute = new TimePickerCallerAttribute(taskEndDate,
				R.string.time_picker_radiobutton_text_end_time, 2, is24HourFormat, true);
		timePickerCallerAttributes.add(timePickerCallerAttribute);
		initializeMultipleTimePickerCaller(timePickerCallerAttributes,
				R.string.time_picker_title_for_task_time);
		timePickerCallerAttributes = new ArrayList<TimePickerCallerAttribute>();
		taskRepetitionEndDate = new TaskRepetitionEndDate();
		timePickerCallerAttribute = new TimePickerCallerAttribute(taskRepetitionEndDate,
				R.string.time_picker_radiobutton_text_repetition_end_time, 1,
				is24HourFormat, true);
		timePickerCallerAttributes.add(timePickerCallerAttribute);
		initializeMultipleTimePickerCaller(timePickerCallerAttributes,
				R.string.time_picker_title_for_repetition_end_time);
	}

	private void restorePercentOfCompletionSegmentForTask() {
		SeekBar seekBar = (SeekBar) getActivity().findViewById(
				R.id.fragment_edit_task_part_main_seekbar_task_percent_of_completion);
		seekBar.setProgress(mUserInterfaceData.percentOfCompletion);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				taskUiData.setPercentOfCompletion((short) seekBar.getProgress());
				mUserInterfaceData.percentOfCompletion = (short) seekBar.getProgress();
			}
		});
	}

	private void restoreSortOrderSegmentForTask() {
		FragmentActivity activity = getActivity();
		radioButtonSortOrderFirst = (RadioButton) activity
				.findViewById(R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_first);
		radioButtonSortOrderFirst.setOnCheckedChangeListener(null);
		radioButtonSortOrderAfterTask = (RadioButton) activity
				.findViewById(R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask);
		radioButtonSortOrderAfterTask.setOnCheckedChangeListener(null);
		textViewTaskSortOrder = (TextView) activity
				.findViewById(R.id.fragment_edit_task_part_main_textview_task_sortorder_aftertask);
		mDefaultTextColorStateListForRadioButtonSortOrderAfterTask = textViewTaskSortOrder
				.getTextColors();
		mDefaultBackgroundForRadioButtonSortOrderAfterTask = textViewTaskSortOrder
				.getBackground();
		RadioButton radioButtonSortOrderLast = (RadioButton) activity
				.findViewById(R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_last);
		radioButtonSortOrderLast.setOnCheckedChangeListener(null);
		if (mUserInterfaceData.sortOrder == 0) {
			radioButtonSortOrderFirst.setChecked(true);
		} else {
			Task t = DataProvider.getSortOrderSibling(null,
					activity.getApplicationContext(), mUserInterfaceData.sortOrder,
					mUserInterfaceData.parentId);
			Resources resources = getResources();
			if (t != null) {
				int backgroundColor = t.getColor2(activity);
				int textColor;
				if (Helper.getContrastYIQ(backgroundColor)) {
					textColor = resources
							.getColor(R.color.task_view_text_synchronized_dark);
				} else {
					textColor = resources
							.getColor(R.color.task_view_text_synchronized_light);
				}
				textViewTaskSortOrder.setBackgroundColor(backgroundColor);
				textViewTaskSortOrder.setTextColor(textColor);
				String name = t.getName();
				radioButtonSortOrderAfterTask
						.setText(R.string.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask_text_after);
				textViewTaskSortOrder.setText(name);
				radioButtonSortOrderAfterTask.setOnCheckedChangeListener(null);
				radioButtonSortOrderAfterTask.setChecked(true);
			} else {
				radioButtonSortOrderAfterTask
						.setText(resources
								.getString(R.string.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask_text_none));
				radioButtonSortOrderFirst.setOnCheckedChangeListener(null);
				radioButtonSortOrderFirst.setChecked(true);
			}
		}
		radioButtonSortOrderFirst.setOnCheckedChangeListener(this);
		radioButtonSortOrderAfterTask.setOnCheckedChangeListener(this);
		radioButtonSortOrderAfterTask.setOnClickListener(this);
		textViewTaskSortOrder.setOnClickListener(this);
		radioButtonSortOrderLast.setOnCheckedChangeListener(this);
	}

	private void restoreParentSegmentForTask() {
		FragmentActivity activity = getActivity();
		checkedTextView = (CheckedTextView) activity
				.findViewById(R.id.fragment_edit_task_part_main_checkedtextview_task_parent);
		textViewTaskParent = (TextView) activity
				.findViewById(R.id.fragment_edit_task_part_main_textview_task_parent);
		mDefaultTextColorStateList = textViewTaskParent.getTextColors();
		mDefaultBackground = textViewTaskParent.getBackground();
		CheckBox checkBox = (CheckBox) activity
				.findViewById(R.id.fragment_edit_task_part_main_checkbox_task_parent);
		if (mUserInterfaceData.parentId != null) {
			Task parent = DataProvider.getTask(null, activity,
					mUserInterfaceData.parentId, false);
			int backgroundColor = parent.getColor2(activity);
			int textColor;
			Resources resources = getResources();
			if (Helper.getContrastYIQ(backgroundColor)) {
				textColor = resources.getColor(R.color.task_view_text_synchronized_dark);
			} else {
				textColor = resources.getColor(R.color.task_view_text_synchronized_light);
			}
			textViewTaskParent.setBackgroundColor(backgroundColor);
			textViewTaskParent.setTextColor(textColor);
			String name = parent.getName();
			checkedTextView.setChecked(true);
			textViewTaskParent.setText(name);
			checkBox.setText(name);
			checkBox.setOnCheckedChangeListener(null);
			checkBox.setChecked(true);
		}
		checkedTextView.setOnClickListener(this);
		textViewTaskParent.setOnClickListener(this);
		checkBox.setOnCheckedChangeListener(this);
		checkBox.setOnClickListener(this);
	}

	private void restorePrioritySegmentForTask() {
		// setup priority RadioButton
		RadioButton radioButtonTaskPriorityMedium = (RadioButton) getActivity()
				.findViewById(
						R.id.fragment_edit_task_part_main_radiobutton_task_priority_medium);
		radioButtonTaskPriorityMedium.setOnCheckedChangeListener(null);
		RadioButton radioButtonTaskPriorityLow = (RadioButton) getActivity()
				.findViewById(
						R.id.fragment_edit_task_part_main_radiobutton_task_priority_low);
		radioButtonTaskPriorityLow.setOnCheckedChangeListener(null);
		RadioButton radioButtonTaskPriorityHigh = (RadioButton) getActivity()
				.findViewById(
						R.id.fragment_edit_task_part_main_radiobutton_task_priority_high);
		radioButtonTaskPriorityHigh.setOnCheckedChangeListener(null);
		switch (Task.PRIORITY.fromInt(mUserInterfaceData.priority)) {
		case MEDIUM:
		default:
			radioButtonTaskPriorityMedium.setChecked(true);
			break;
		case LOW:
			radioButtonTaskPriorityLow.setChecked(true);
			break;
		case HIGH:
			radioButtonTaskPriorityHigh.setChecked(true);
			break;
		}
		radioButtonTaskPriorityMedium.setOnCheckedChangeListener(this);
		radioButtonTaskPriorityLow.setOnCheckedChangeListener(this);
		radioButtonTaskPriorityHigh.setOnCheckedChangeListener(this);
	}

	private void restoreColorPickerSegmentForTask() {
		// setup task color
		ImageButton buttonTaskColor = (ImageButton) getActivity().findViewById(
				R.id.fragment_edit_task_part_main_button_task_color);
		buttonTaskColor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), ActivityColorPicker.class);
				Integer color = mUserInterfaceData.getColor(getActivity());
				if (color == null) {
					color = Helper.getIntegerPreferenceValue(getActivity(),
							R.string.preference_key_task_unset_color, getResources()
									.getColor(R.color.task_unset_color_default_value),
							null, null);
				}
				intent.putExtra(CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, color);
				startActivityForResult(intent, CommonConstants.REQUEST_CODE_PICK_COLOR);
				scrollPos = scrollView.getScrollY();
				FragmentEditTaskPartMain.mapStringToScrollPos.put(selectedConfiguration,
						scrollPos);
			}
		});
		// editText.setTag(mUserInterfaceData);
		if (mUserInterfaceData.color != null) {
			buttonTaskColor.setBackgroundColor(mUserInterfaceData.color.intValue());
			EditText editText = (EditText) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_edittext_task_color);
			editText.setText(Integer.toHexString(mUserInterfaceData.color.intValue()));
		}
	}

	private void restoreTaskNameSegmentForTask(CharSequence textTaskName) {
		edittextTaskName.setText("");
		edittextTaskName.append(textTaskName);
	}

	private void restoreTaskNameSegmentForTask() {
		edittextTaskName = (EditText) getActivity().findViewById(
				R.id.fragment_edit_task_part_main_edittext_task_name);
		edittextTaskName.setText(mUserInterfaceData.textTaskName);
		int length = edittextTaskName.getText().length();
		edittextTaskName.setSelection(length, length);
		edittextTaskName.setTag(mUserInterfaceData);
		EditTextTaskTextChangedListener editTextTaskTextChangedListener = new EditTextTaskTextChangedListener();
		editTextTaskTextChangedListener.editText = edittextTaskName;
		edittextTaskName.addTextChangedListener(editTextTaskTextChangedListener);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(FragmentEditTaskPartMain.KEY_DATE_CALLER_ID, dateCallerId);
	}

	private void restoreRecurrentSegment() {
		restoreSpinnerSegment();
		//
		restoreTimeUnitsCountSegment();
		restoreOccurrencesMaxCountSegment();
		restoreMinutelyOrHourlyOrDailyRecurrentSegment(RecurrenceInterval.MINUTES);
		restoreMinutelyOrHourlyOrDailyRecurrentSegment(RecurrenceInterval.HOURS);
		restoreMinutelyOrHourlyOrDailyRecurrentSegment(RecurrenceInterval.DAYS);
		restoreWeeklyRecurrentSegment();
		restoreMonthlyRecurrentSegment();
		restoreYearlyRecurrentSegment();
	}

	private void restoreOccurrencesMaxCountSegment() {
		occurrencesMaxCountDefaultValue = mUserInterfaceData.occurrencesMaxCountDefaultValue;
		occurrencesMaxCountMinValue = mUserInterfaceData.occurrencesMaxCountMinValue;
		occurrencesMaxCountMaxValue = mUserInterfaceData.occurrencesMaxCountMaxValue;
		edittextOccurrencesMaxCount = (EditText) getActivity().findViewById(
				R.id.fragment_edit_task_part_main_edittext_occurrences_max_count);
		edittextOccurrencesMaxCount
				.setTag(R.string.tag_key_value_must_be_greater_than_or_equal_to,
						R.string.toast_text_occurrences_max_count_must_be_greater_than_or_equal_to);
		edittextOccurrencesMaxCount.setTag(
				R.string.tag_key_value_must_be_less_than_or_equal_to,
				R.string.toast_text_occurrences_max_count_must_be_less_than_or_equal_to);
		edittextOccurrencesMaxCount.setTag(R.string.tag_key_value_must_be_within_bounds,
				R.string.toast_text_occurrences_max_count_must_be_within_bounds);
		occurrencesMaxCount = mUserInterfaceData.occurrencesMaxCount;
		edittextOccurrencesMaxCount.setText(mUserInterfaceData.textOccurrencesMaxCount);
		// edittextOccurrencesMaxCount.setTag(mEntityWithDependents.task);
		EditTextTaskOccurrencesMaxCountChangedListener taskOccurrencesMaxCountChangedListener = new EditTextTaskOccurrencesMaxCountChangedListener();
		taskOccurrencesMaxCountChangedListener.editText = edittextOccurrencesMaxCount;
		edittextOccurrencesMaxCount
				.addTextChangedListener(taskOccurrencesMaxCountChangedListener);
	}

	private void restoreTimeUnitsCountSegment() {
		edittextTimeUnitsCount = (EditText) getActivity().findViewById(
				R.id.fragment_edit_task_part_main_edittext_time_units_count);
		editTextTaskTimeUnitsCountChangedListener = new EditTextTaskTimeUnitsCountChangedListener();
		editTextTaskTimeUnitsCountChangedListener.editText = edittextTimeUnitsCount;
		editTextTaskTimeUnitsCountChangedListener.textView = textViewTimeUnit;
		edittextTimeUnitsCount
				.addTextChangedListener(editTextTaskTimeUnitsCountChangedListener);
		updateEdittextTimeUnitsCount();
	}

	private void restoreSpinnerSegment() {
		FragmentActivity activity = getActivity();
		RecurrenceInterval.initializeOccurrencesPicker(activity);
		linearlayoutRecurrent = (LinearLayout) activity
				.findViewById(R.id.linearlayout_recurrent);
		radiogroupMonthBasedRecurrenceMode = (RadioGroup) activity
				.findViewById(R.id.fragment_edit_task_part_main_radiogroup_month_based_recurrence_mode);
		scrollviewOccurrencesPickerMinutelyRecurrent = (HorizontalScrollView) activity
				.findViewById(R.id.scrollview_occurrences_picker_minutely_recurrent);
		scrollviewOccurrencesPickerHourlyRecurrent = (HorizontalScrollView) activity
				.findViewById(R.id.scrollview_occurrences_picker_hourly_recurrent);
		scrollviewOccurrencesPickerDailyRecurrent = (HorizontalScrollView) activity
				.findViewById(R.id.scrollview_occurrences_picker_daily_recurrent);
		scrollviewWeekDaysPicker = (HorizontalScrollView) activity
				.findViewById(R.id.scrollview_week_days_picker);
		linearlayoutOccurrencesPickerMonthlyRecurrent = (LinearLayout) activity
				.findViewById(R.id.linearlayout_occurrences_picker_monthly_recurrent);
		scrollviewOccurrencesPickerMonthlyRecurrentOnDate = (HorizontalScrollView) activity
				.findViewById(R.id.scrollview_occurrences_picker_monthly_recurrent_on_date);
		linearlayoutOccurrencesPickerMonthlyRecurrentOnNthWeekDay = (LinearLayout) activity
				.findViewById(R.id.linearlayout_occurrences_picker_monthly_recurrent_on_nth_week_day);
		linearlayoutOccurrencesPickerYearlyRecurrent = (LinearLayout) activity
				.findViewById(R.id.linearlayout_occurrences_picker_yearly_recurrent);
		buttonDaysPickerOfYearlyRecurrentTask = (Button) linearlayoutOccurrencesPickerYearlyRecurrent
				.findViewById(R.id.button_days_picker_of_yearly_recurrent_task);
		buttonDaysPickerOfYearlyRecurrentTask.setOnClickListener(this);
		textViewTimeUnit = (TextView) activity
				.findViewById(R.id.fragment_edit_task_part_main_textview_time_unit);
		recurrenceInterval = mUserInterfaceData.recurrenceInterval;
		spinnerWeekDayNumberInMonth = (Spinner) linearlayoutOccurrencesPickerMonthlyRecurrentOnNthWeekDay
				.findViewById(R.id.fragment_edit_task_part_main_spinner_week_day_number);
		weekDayNumberInMonthDefaultValue = mUserInterfaceData.weekDayNumberInMonthDefaultValue;
		weekDayNumberInMonth = mUserInterfaceData.weekDayNumberInMonth;
		spinnerWeekDayNumberInMonthPreviousPosition = Helper
				.setSpinnerToValueFromStringArray(activity, spinnerWeekDayNumberInMonth,
						weekDayNumberInMonth, R.string.preference_key_week_day_number,
						R.array.week_day_number_values_array,
						weekDayNumberInMonthDefaultValue, this);
		spinnerTimeUnit = (Spinner) activity
				.findViewById(R.id.fragment_edit_task_part_main_spinner_time_unit);
		timeUnitDefaultValue = mUserInterfaceData.timeUnitDefaultValue;
		timeUnitsStartingIndex = mUserInterfaceData.timeUnitsStartingIndexValue;
		spinnerTimeUnitPreviousPosition = Helper.setSpinnerToValueFromStringArray(
				activity, spinnerTimeUnit,
				TimeUnit.fromRecurrenceInterval(recurrenceInterval).getIntegerValue(),
				R.string.preference_key_time_unit, R.array.time_unit_values_array,
				timeUnitDefaultValue, this);
		setVisibilityBasedOnTimeUnitSelection(recurrenceInterval);
		//
		spinnerCalendar = (Spinner) activity
				.findViewById(R.id.fragment_edit_task_part_main_spinner_calendar);
		// Create an ArrayAdapter using default spinner layout
		ArrayAdapter<Calendar2> adapter = new ArrayAdapter<Calendar2>(activity,
				android.R.layout.simple_spinner_item, mUserInterfaceData.calendar2s) {
		};
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinnerCalendar.setAdapter(adapter);
		Calendar2 calendar2 = mUserInterfaceData.calendar2;
		for (spinnerCalendarPreviousPosition = 0; spinnerCalendarPreviousPosition < mUserInterfaceData.calendar2s
				.size(); spinnerCalendarPreviousPosition++) {
			Calendar2 calendar = mUserInterfaceData.calendar2s
					.get(spinnerCalendarPreviousPosition);
			if (calendar2.id == null) {
				if (calendar.id == null) {
					break;
				}
			} else if (calendar2.id.equals(calendar.id)) {
				break;
			}
		}
		spinnerCalendar.setSelection(spinnerCalendarPreviousPosition);
		spinnerCalendar.setOnItemSelectedListener(this);
	}

	private void setupDayOfWeekCheckBoxes(TextViewWithCircularIndicator[] checkBoxes,
			boolean[] occurrencesOnDaysOfWeek) {
		Calendar calendar = Calendar.getInstance();
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		int firstDayOfWeek = Helper.getFirstDayOfWeek(getActivity());
		// move calendar to the beginning of the week
		calendar.add(Calendar.DAY_OF_YEAR, firstDayOfWeek - dayOfWeek);
		if (firstDayOfWeek > dayOfWeek) {
			calendar.add(Calendar.DAY_OF_YEAR, -7);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
		for (int i = 0; i < checkBoxes.length; i++) {
			checkBoxes[i].setText(sdf.format(new Date(calendar.getTimeInMillis())));
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			if (day <= occurrencesOnDaysOfWeek.length) {
				checkBoxes[i].drawIndicator(occurrencesOnDaysOfWeek[day - 1]);
			}
			// checkBoxes[i].setOnCheckedChangeListener(this);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
	}

	private void setupDateTimeCheckboxes(TaskDate date) {
		date.setDateCheckBoxText(date.getTaskDateTime());
		date.setTimeCheckBoxText(date.getTaskDateTime());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		FragmentActivity activity = getActivity();
		int callerId = buttonView.getId();
		switch (callerId) {
		case R.id.fragment_edit_task_part_main_checkbox_task_parent:
			if (isChecked) {
				Bundle bundle = new Bundle();
				bundle.putInt(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE,
						CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_TASK_TREE);
				bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
						AbsListView.CHOICE_MODE_SINGLE);
				if (mUserInterfaceData.id != null) {
					bundle.putLong(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE,
							mUserInterfaceData.id);
				}
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK,
						true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK,
						false);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK,
						true);
				Intent intent = new Intent(activity, ActivitySelectTreeItems.class);
				intent.putExtra(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE, bundle);
				intent.putExtra(
						ActivitySelectTreeItems.IntentExtraTitle,
						getResources().getString(
								R.string.activity_select_task_title_select_parent_task));
				startActivityForResult(intent,
						CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE);
			} else {
				buttonView
						.setText(R.string.fragment_edit_task_part_main_checkbox_task_parent_not_set);
				taskUiData.setParentId(null);
				mUserInterfaceData.parentId = null;
				radioButtonSortOrderFirst.setChecked(true);
			}
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_first:
			if (isChecked) {
				mUserInterfaceData.sortOrder = 0;
				taskUiData.setSortOrder(0);
			}
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask:
			if (!isChecked) {
				textViewTaskSortOrder
						.setTextColor(mDefaultTextColorStateListForRadioButtonSortOrderAfterTask);
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					textViewTaskSortOrder
							.setBackground(mDefaultBackgroundForRadioButtonSortOrderAfterTask);
				} else {
					textViewTaskSortOrder
							.setBackgroundDrawable(mDefaultBackgroundForRadioButtonSortOrderAfterTask);
				}
				textViewTaskSortOrder
						.setText(R.string.fragment_edit_task_part_main_textview_task_sortorder_aftertask_text_none);
				buttonView
						.setText(R.string.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask_text_none);
			}
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_last:
			if (isChecked) {
				Task t = DataProvider.getSortOrderSiblingLast2(null,
						activity.getApplicationContext(), mUserInterfaceData.id,
						mUserInterfaceData.parentId);
				if (t != null) {
					taskUiData.setSortOrder(t.getSortOrder() + 1);
					mUserInterfaceData.sortOrder = t.getSortOrder() + 1;
				}
			}
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_priority_high:
			taskUiData.setPriority(Task.PRIORITY.HIGH.getValue());
			mUserInterfaceData.priority = Task.PRIORITY.HIGH.getValue();
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_priority_low:
			taskUiData.setPriority(Task.PRIORITY.LOW.getValue());
			mUserInterfaceData.priority = Task.PRIORITY.LOW.getValue();
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_priority_medium:
			taskUiData.setPriority(Task.PRIORITY.MEDIUM.getValue());
			mUserInterfaceData.priority = Task.PRIORITY.MEDIUM.getValue();
			break;
		case R.id.fragment_edit_task_part_main_radio_monthly_recurrent_on_date:
			if (isChecked) {
				recurrenceInterval = getRecurrenceIntervalFromUserInterface(MonthRecurrenceMode.RECURRENT_ON_DATE);
				mUserInterfaceData.recurrenceInterval = recurrenceInterval;
				updateEdittextTimeUnitsCount();
				showOccurrencesSelectionSegmentMonthlyRecurrentOnDate();
				hideOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay();
			}
			break;
		case R.id.fragment_edit_task_part_main_radio_monthly_recurrent_on_nth_week_day:
			if (isChecked) {
				recurrenceInterval = getRecurrenceIntervalFromUserInterface(MonthRecurrenceMode.RECURRENT_ON_NTH_WEEK_DAY);
				mUserInterfaceData.recurrenceInterval = recurrenceInterval;
				updateEdittextTimeUnitsCount();
				showOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay();
				hideOccurrencesSelectionSegmentMonthlyRecurrentOnDate();
			}
			break;
		default:
			break;
		}
	}

	private void hideRecurrenceSegment() {
		linearlayoutRecurrent.setVisibility(View.GONE);
	}

	private void showRecurrenceSegment() {
		linearlayoutRecurrent.setVisibility(View.VISIBLE);
	}

	private boolean collectOccurrencesMaxCount() {
		boolean isCollected = true;
		Editable editable = edittextOccurrencesMaxCount.getText();
		if (editable != null && editable.length() != 0) {
			mUserInterfaceData.occurrencesMaxCount = Helper
					.getValidEditTextIntegerValueOrNull(edittextOccurrencesMaxCount,
							null, occurrencesMaxCountMinValue,
							occurrencesMaxCountMaxValue, false, false);
			ValueMustBe valueMustBe = (ValueMustBe) edittextOccurrencesMaxCount
					.getTag(R.string.tag_key_value_must_be);
			if (valueMustBe != null) {
				isCollected = false;
			}
		} else {
			mUserInterfaceData.occurrencesMaxCount = null;
		}
		mUserInterfaceData.textOccurrencesMaxCount = editable;
		return isCollected;
	}

	private boolean isOccurrencesMaxCountCollected() {
		boolean isCollected = true;
		Editable editable = edittextOccurrencesMaxCount.getText();
		if (editable != null && editable.length() != 0) {
			occurrencesMaxCount = Helper.getValidEditTextIntegerValueOrNull(
					edittextOccurrencesMaxCount, null, occurrencesMaxCountMinValue,
					occurrencesMaxCountMaxValue, false, true);
			ValueMustBe valueMustBe = (ValueMustBe) edittextOccurrencesMaxCount
					.getTag(R.string.tag_key_value_must_be);
			if (valueMustBe != null) {
				isCollected = false;
			}
		} else {
			occurrencesMaxCount = null;
		}
		taskUiData.setOccurrencesMaxCount(occurrencesMaxCount);
		return isCollected;
	}

	@Override
	public void onStop() {
		if (!getActivity().isFinishing()) {
			// collect info
			collectTimeUnit();
		}
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		FragmentActivity activity = getActivity();
		switch (requestCode) {
		case CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE:
			if (resultCode == android.app.Activity.RESULT_OK) {
				long[] idArray = intent.getLongArrayExtra(CommonConstants.ID_ARRAY);
				Task parent = null;
				if (idArray.length > 0) {
					parent = DataProvider.getTask(null, activity, idArray[0], false);
				}
				CheckBox checkBox = (CheckBox) activity
						.findViewById(R.id.fragment_edit_task_part_main_checkbox_task_parent);
				if (parent == null) {
					textViewTaskParent.setTextColor(mDefaultTextColorStateList);
					if (android.os.Build.VERSION.SDK_INT >= 16) {
						textViewTaskParent.setBackground(mDefaultBackground);
					} else {
						textViewTaskParent.setBackgroundDrawable(mDefaultBackground);
					}
					taskUiData.setParentId(null);
					mUserInterfaceData.parentId = null;
					checkedTextView.setChecked(false);
					textViewTaskParent
							.setText(R.string.fragment_edit_task_part_main_checkbox_task_parent_not_set);
					checkBox.setChecked(false);
				} else {
					Long id = parent.getId();
					taskUiData.setParentId(id);
					mUserInterfaceData.parentId = id;
					int backgroundColor = parent.getColor2(activity);
					int textColor;
					if (Helper.getContrastYIQ(backgroundColor)) {
						textColor = activity.getResources().getColor(
								R.color.task_view_text_synchronized_dark);
					} else {
						textColor = activity.getResources().getColor(
								R.color.task_view_text_synchronized_light);
					}
					textViewTaskParent.setBackgroundColor(backgroundColor);
					textViewTaskParent.setTextColor(textColor);
					String name = parent.getName();
					checkedTextView.setChecked(true);
					textViewTaskParent.setText(name);
					checkBox.setText(name);
				}
				radioButtonSortOrderFirst.setChecked(true);
			} else if (resultCode == android.app.Activity.RESULT_CANCELED) {
				if (mUserInterfaceData.parentId == null) {
					CheckBox checkBox = (CheckBox) activity
							.findViewById(R.id.fragment_edit_task_part_main_checkbox_task_parent);
					checkBox.setChecked(false);
				}
			}
			break;
		case CommonConstants.REQUEST_CODE_SELECT_TASK_FOR_SORT_ORDER:
			if (resultCode == android.app.Activity.RESULT_OK) {
				if (intent.hasExtra(CommonConstants.RETURN_ID)) {
					long id = intent.getLongExtra(CommonConstants.RETURN_ID, 0);
					Task t = DataProvider.getTask(null, activity, id, false);
					if (t == null) {
						textViewTaskSortOrder
								.setTextColor(mDefaultTextColorStateListForRadioButtonSortOrderAfterTask);
						if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							textViewTaskSortOrder
									.setBackground(mDefaultBackgroundForRadioButtonSortOrderAfterTask);
						}

						textViewTaskSortOrder
								.setText(R.string.fragment_edit_task_part_main_textview_task_sortorder_aftertask_text_none);
						radioButtonSortOrderFirst.setChecked(true);
					} else {
						int backgroundColor = t.getColor2(activity);
						int textColor;
						if (Helper.getContrastYIQ(backgroundColor)) {
							textColor = activity.getResources().getColor(
									R.color.task_view_text_synchronized_dark);
						} else {
							textColor = activity.getResources().getColor(
									R.color.task_view_text_synchronized_light);
						}
						textViewTaskSortOrder.setBackgroundColor(backgroundColor);
						textViewTaskSortOrder.setTextColor(textColor);
						String name = t.getName();
						textViewTaskSortOrder.setText(name);
						radioButtonSortOrderAfterTask
								.setText(R.string.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask_text_after);
						taskUiData.setSortOrder(t.getSortOrder() + 1);
						mUserInterfaceData.sortOrder = t.getSortOrder() + 1;
						radioButtonSortOrderAfterTask.setChecked(true);
					}
				} else {
					radioButtonSortOrderFirst.setChecked(true);
				}
			} else if (resultCode == android.app.Activity.RESULT_CANCELED) {
			}
			break;
		case CommonConstants.REQUEST_CODE_PICK_COLOR:
			if (resultCode == android.app.Activity.RESULT_OK) {
				int color = intent.getIntExtra(
						CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, 0);
				taskUiData.setColor(color);
				mUserInterfaceData.color = color;
				ImageButton buttonTaskColor = (ImageButton) activity
						.findViewById(R.id.fragment_edit_task_part_main_button_task_color);
				EditText editText = (EditText) activity
						.findViewById(R.id.fragment_edit_task_part_main_edittext_task_color);
				buttonTaskColor.setBackgroundColor(color);
				editText.setText(Integer.toHexString(color));
			} else if (resultCode == android.app.Activity.RESULT_CANCELED) {
			}
			break;
		case CommonConstants.REQUEST_CODE_SELECT_DAYS_OF_YEAR:
			if (resultCode == android.app.Activity.RESULT_OK) {
				@SuppressWarnings("unchecked")
				HashSet<Integer> selectedDays = (HashSet<Integer>) intent
						.getSerializableExtra(CommonConstants.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR);
				List<TaskOccurrence> taskOccurrences = RecurrenceInterval.YEARS
						.getTaskOccurrences();
				taskOccurrences.clear();
				for (int selectedDay : selectedDays) {
					taskOccurrences.add(new TaskOccurrence(null, null, selectedDay));
				}
				Collections.sort(taskOccurrences);
				restoreYearlyRecurrentSegment();
			} else if (resultCode == android.app.Activity.RESULT_CANCELED) {
			}
			break;
		}
	}

	@Override
	public boolean isDataCollected() {
		boolean isCollected = true;
		mUserInterfaceData.wantingItemSetForTask.clear();
		// collect task name
		if (!isTaskNameCollected()) {
			isCollected = false;
		} else {
			if (!checkedTextView.isChecked()) {
				taskUiData.setParentId(null);
			}
			SeekBar seekBar = (SeekBar) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_seekbar_task_percent_of_completion);
			taskUiData.setPercentOfCompletion((short) seekBar.getProgress());
			// collect task priority
			RadioGroup radioGroup = (RadioGroup) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_radiogroup_task_priority);
			int radioButtonId = radioGroup.getCheckedRadioButtonId();
			switch (radioButtonId) {
			case R.id.fragment_edit_task_part_main_radiobutton_task_priority_high:
				taskUiData.setPriority(Task.PRIORITY.HIGH.getValue());
				break;
			case R.id.fragment_edit_task_part_main_radiobutton_task_priority_low:
				taskUiData.setPriority(Task.PRIORITY.LOW.getValue());
				break;
			case R.id.fragment_edit_task_part_main_radiobutton_task_priority_medium:
				taskUiData.setPriority(Task.PRIORITY.MEDIUM.getValue());
				break;
			default:
				taskUiData.setPriority(Task.PRIORITY.MEDIUM.getValue());
				break;
			}
			// collect task Color
			EditText editText = (EditText) getActivity().findViewById(
					R.id.fragment_edit_task_part_main_edittext_task_color);
			String text = editText.getText().toString().trim();
			Integer color;
			try {
				color = (int) Long.parseLong(text, 16);
			} catch (NumberFormatException e) {
				color = null;
			}
			taskUiData.setColor(color);
			if (!isTimeUnitCollected()) {
				isCollected = false;
			}
			taskUiData.setCalendarId(getCalendarFromSpinnerOrDefault().id);
		}
		return isCollected;
	}

	private boolean collectTaskName() {
		boolean isCollected = true;
		Editable text = edittextTaskName.getText();
		if (text == null || text.length() == 0) {
			isCollected = false;
		}
		mUserInterfaceData.textTaskName = edittextTaskName.getText();
		return isCollected;
	}

	private boolean isTaskNameCollected() {
		boolean isCollected = true;
		View focusView = null;
		String text = edittextTaskName.getText().toString().trim();
		if (text.length() == 0) {
			if (taskEditMode == TaskEditMode.QUICK) {
				taskUiData.setName(taskUiData.getTaskTitleForQuickReminder());
			} else {
				focusView = edittextTaskName;
				focusView.requestFocus();
				Toast.makeText(getActivity(), R.string.toast_text_task_name_is_required,
						Toast.LENGTH_LONG).show();
				isCollected = false;
			}
		} else {
			taskUiData.setName(text);
		}
		return isCollected;
	}

	private boolean collectTimeUnitsCount() {
		boolean isCollected = true;
		recurrenceInterval.setTextTimeUnitsCount(edittextTimeUnitsCount.getText());
		recurrenceInterval.setTimeUnitsCountMustBe((ValueMustBe) edittextTimeUnitsCount
				.getTag(R.string.tag_key_value_must_be));
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		}
		return isCollected;
	}

	private boolean isTimeUnitsCountCollected() {
		boolean isCollected = true;
		Integer timeUnitsCount = Helper.getValidEditTextIntegerValueOrNull(
				edittextTimeUnitsCount, null, timeUnitsCountMinValue,
				timeUnitsCountMaxValue, false, true);
		ValueMustBe valueMustBe = (ValueMustBe) edittextTimeUnitsCount
				.getTag(R.string.tag_key_value_must_be);
		recurrenceInterval.setTimeUnitsCountMustBe(valueMustBe);
		if (valueMustBe != null) {
			isCollected = false;
		} else {
			taskUiData.setTimeUnitsCount(timeUnitsCount);
		}
		return isCollected;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
		case R.id.fragment_edit_task_part_main_spinner_time_unit:
			if (position != spinnerTimeUnitPreviousPosition) {
				spinnerTimeUnitPreviousPosition = position;
				recurrenceInterval = getRecurrenceIntervalFromUserInterface();
				mUserInterfaceData.recurrenceInterval = recurrenceInterval;
				updateEdittextTimeUnitsCount();
				setVisibilityBasedOnTimeUnitSelection(recurrenceInterval);
			}
			break;
		case R.id.fragment_edit_task_part_main_spinner_calendar:
			if (position != spinnerCalendarPreviousPosition) {
				spinnerCalendarPreviousPosition = position;
				calendar2 = mUserInterfaceData.calendar2 = (Calendar2) parent
						.getItemAtPosition(position);
			}
			break;
		case R.id.fragment_edit_task_part_main_spinner_week_day_number:
			if (position != spinnerWeekDayNumberInMonthPreviousPosition) {
				spinnerWeekDayNumberInMonthPreviousPosition = position;
				weekDayNumberInMonth = getOrdinalNumberOfOccurrenceOfWeekdayInMonthFromSpinnerOrDefault();
			}
			break;
		default:
			break;
		}
	}

	private void updateEdittextTimeUnitsCount() {
		timeUnitsCountMinValue = recurrenceInterval.getTimeUnitsCountMinValue();
		timeUnitsCountMaxValue = recurrenceInterval.getTimeUnitsCountMaxValue();
		edittextTimeUnitsCount.setTag(
				R.string.tag_key_value_must_be_greater_than_or_equal_to,
				recurrenceInterval
						.getToastTextIdTimeUnitsCountMustBeGreaterThanOrEqualTo());
		edittextTimeUnitsCount.setTag(
				R.string.tag_key_value_must_be_less_than_or_equal_to,
				recurrenceInterval.getToastTextIdTimeUnitsCountMustBeLessThanOrEqualTo());
		edittextTimeUnitsCount.setTag(R.string.tag_key_value_must_be_within_bounds,
				recurrenceInterval.getToastTextIdTimeUnitsCountMustBeWithinBounds());
		edittextTimeUnitsCount.setText(recurrenceInterval.getTextTimeUnitsCount());
		int length = edittextTimeUnitsCount.getText().length();
		edittextTimeUnitsCount.setSelection(length, length);
	}

	private void setVisibilityBasedOnTimeUnitSelection(
			RecurrenceInterval recurrenceInterval) {
		switch (recurrenceInterval) {
		case ONE_TIME:
			hideRecurrenceSegment();
			break;
		case MINUTES:
			hideOccurrencesSelectionSegmentHourlyRecurrent();
			hideOccurrencesSelectionSegmentDailyRecurrent();
			hideOccurrencesSelectionSegmentWeeklyRecurrent();
			hideOccurrencesSelectionSegmentMonthlyRecurrent();
			hideOccurrencesSelectionSegmentYearlyRecurrent();
			showOccurrencesSelectionSegmentMinutelyRecurrent();
			showRecurrenceSegment();
			break;
		case HOURS:
			hideOccurrencesSelectionSegmentMinutelyRecurrent();
			hideOccurrencesSelectionSegmentDailyRecurrent();
			hideOccurrencesSelectionSegmentWeeklyRecurrent();
			hideOccurrencesSelectionSegmentMonthlyRecurrent();
			hideOccurrencesSelectionSegmentYearlyRecurrent();
			showOccurrencesSelectionSegmentHourlyRecurrent();
			showRecurrenceSegment();
			break;
		case DAYS:
			hideOccurrencesSelectionSegmentMinutelyRecurrent();
			hideOccurrencesSelectionSegmentHourlyRecurrent();
			hideOccurrencesSelectionSegmentWeeklyRecurrent();
			hideOccurrencesSelectionSegmentMonthlyRecurrent();
			hideOccurrencesSelectionSegmentYearlyRecurrent();
			showOccurrencesSelectionSegmentDailyRecurrent();
			showRecurrenceSegment();
			break;
		case WEEKS:
			hideOccurrencesSelectionSegmentMinutelyRecurrent();
			hideOccurrencesSelectionSegmentHourlyRecurrent();
			hideOccurrencesSelectionSegmentDailyRecurrent();
			hideOccurrencesSelectionSegmentMonthlyRecurrent();
			hideOccurrencesSelectionSegmentYearlyRecurrent();
			showOccurrencesSelectionSegmentWeeklyRecurrent();
			showRecurrenceSegment();
			break;
		case MONTHS_ON_DATE:
		case MONTHS_ON_NTH_WEEK_DAY:
			hideOccurrencesSelectionSegmentMinutelyRecurrent();
			hideOccurrencesSelectionSegmentHourlyRecurrent();
			hideOccurrencesSelectionSegmentDailyRecurrent();
			hideOccurrencesSelectionSegmentWeeklyRecurrent();
			hideOccurrencesSelectionSegmentYearlyRecurrent();
			showOccurrencesSelectionSegmentMonthlyRecurrent();
			showRecurrenceSegment();
			break;
		case YEARS:
			hideOccurrencesSelectionSegmentMinutelyRecurrent();
			hideOccurrencesSelectionSegmentHourlyRecurrent();
			hideOccurrencesSelectionSegmentDailyRecurrent();
			hideOccurrencesSelectionSegmentWeeklyRecurrent();
			hideOccurrencesSelectionSegmentMonthlyRecurrent();
			showOccurrencesSelectionSegmentYearlyRecurrent();
			showRecurrenceSegment();
			break;
		default:
			hideRecurrenceSegment();
			break;
		}
	}

	private void showOccurrencesSelectionSegmentMinutelyRecurrent() {
		scrollviewOccurrencesPickerMinutelyRecurrent.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentMinutelyRecurrent() {
		scrollviewOccurrencesPickerMinutelyRecurrent.setVisibility(View.GONE);
	}

	private void showOccurrencesSelectionSegmentHourlyRecurrent() {
		scrollviewOccurrencesPickerHourlyRecurrent.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentHourlyRecurrent() {
		scrollviewOccurrencesPickerHourlyRecurrent.setVisibility(View.GONE);
	}

	private void showOccurrencesSelectionSegmentDailyRecurrent() {
		scrollviewOccurrencesPickerDailyRecurrent.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentDailyRecurrent() {
		scrollviewOccurrencesPickerDailyRecurrent.setVisibility(View.GONE);
	}

	private void showOccurrencesSelectionSegmentWeeklyRecurrent() {
		scrollviewWeekDaysPicker.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentWeeklyRecurrent() {
		scrollviewWeekDaysPicker.setVisibility(View.GONE);
	}

	private void showOccurrencesSelectionSegmentMonthlyRecurrent() {
		linearlayoutOccurrencesPickerMonthlyRecurrent.setVisibility(View.VISIBLE);
	}

	private void showOccurrencesSelectionSegmentYearlyRecurrent() {
		linearlayoutOccurrencesPickerYearlyRecurrent.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentYearlyRecurrent() {
		linearlayoutOccurrencesPickerYearlyRecurrent.setVisibility(View.GONE);
	}

	private class OccurrencesPickerCheckBoxOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			TextViewWithCircularIndicator textViewWithCircularIndicator = (TextViewWithCircularIndicator) v;
			textViewWithCircularIndicator.drawIndicator(!textViewWithCircularIndicator
					.isDrawingIndicator());
			boolean selected = textViewWithCircularIndicator.isDrawingIndicator();
			LinearLayout occurrencesPicker = (LinearLayout) v.getParent();
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) v;
			int i = occurrencesPicker.indexOfChild(checkBox);
			List<TaskOccurrence> taskOccurrences = recurrenceInterval
					.getTaskOccurrences();
			if (recurrenceInterval == RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY) {
				int taskOccurrencesSize = taskOccurrences.size();
				if (taskOccurrencesSize > 0) {
					taskOccurrences = taskOccurrences.subList(0, taskOccurrencesSize - 1);
				}
			}
			if (selected) {
				taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(), i + 1));
			} else {
				TaskOccurrence taskOccurrence = new TaskOccurrence(null, null, i + 1);
				taskOccurrences.remove(taskOccurrence);
			}
		}
	}

	private void adjustOccurrencesPicker(LinearLayout occurrencesPicker) {
		if (recurrenceInterval.getTimeUnitsCountMustBe() == null) {
			int timeUnitsCount = recurrenceInterval.getTimeUnitsCount();
			int checkBoxCount = occurrencesPicker.getChildCount();
			LayoutInflater layoutInflater = getActivity().getLayoutInflater();
			for (int i = 0; i < timeUnitsCount || i < checkBoxCount; i++) {
				TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
						.getChildAt(i);
				if (checkBox == null) {
					checkBox = (TextViewWithCircularIndicator) layoutInflater.inflate(
							R.layout.repetition_selection_checkbox, occurrencesPicker,
							false);
					checkBox.setOnClickListener(occurrencesPickerCheckBoxOnClickListener);
					checkBox.setText(String.valueOf(i + timeUnitsStartingIndex));
					occurrencesPicker.addView(checkBox);
					continue;
				}
				if (i < timeUnitsCount) {
					checkBox.setVisibility(View.VISIBLE);
				} else {
					checkBox.setVisibility(View.GONE);
				}
			}
		}
	}

	private void restoreMinutelyOrHourlyOrDailyRecurrentSegment(
			RecurrenceInterval recurrenceInterval) {
		LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
		occurrencesPicker.removeAllViews();
		LayoutInflater inflater = getActivity().getLayoutInflater();
		int checkBoxCount = recurrenceInterval.getCheckBoxCount();
		for (int i = 0; i < checkBoxCount; i++) {
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) inflater
					.inflate(R.layout.repetition_selection_checkbox, occurrencesPicker,
							false);
			checkBox.setText(String.valueOf(i + timeUnitsStartingIndex));
			occurrencesPicker.addView(checkBox);
		}
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		int occurrencesCount = taskOccurrences.size();
		// for (int i = 0; i < repetitionDaysCount && i < timeUnitsCount; i++) {
		for (int i = 0; i < occurrencesCount; i++) {
			TaskOccurrence taskOccurrence = taskOccurrences.get(i);
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
					.getChildAt(taskOccurrence.getOrdinalNumber() - 1);
			if (checkBox != null) {
				checkBox.drawIndicator(true);
			}
		}
		setOccurrencesPickerCheckBoxOnCheckedChangeListener(occurrencesPicker);
	}

	private void setOccurrencesPickerCheckBoxOnCheckedChangeListener(
			LinearLayout occurrencesPicker) {
		int checkBoxCount;
		checkBoxCount = occurrencesPicker.getChildCount();
		for (int i = 0; i < checkBoxCount; i++) {
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
					.getChildAt(i);
			checkBox.setOnClickListener(occurrencesPickerCheckBoxOnClickListener);
		}
	}

	private void restoreYearlyRecurrentSegment() {
		RecurrenceInterval recurrenceInterval = RecurrenceInterval.YEARS;
		LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
		occurrencesPicker.removeAllViews();
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		int occurrencesCount = taskOccurrences.size();
		SimpleDateFormat s = new SimpleDateFormat("MMMM, dd");
		SimpleDateFormat s2 = new SimpleDateFormat("MMMM");
		for (int i = 0; i < occurrencesCount; i++) {
			TextView textView = new TextView(getActivity());
			occurrencesPicker.addView(textView);
			int occurrenceCode = taskOccurrences.get(i).getOrdinalNumber();
			String dateString = taskWithDependentsUiData.getDateString(getActivity(), s,
					s2, occurrenceCode);
			textView.setText(dateString);
		}
	}

	private void restoreWeeklyRecurrentSegment() {
		RecurrenceInterval recurrenceInterval = RecurrenceInterval.WEEKS;
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		setCheckBoxSaveEnabled(recurrenceInterval, taskOccurrences);
		setOccurrencesPickerCheckBoxOnCheckedChangeListener(recurrenceInterval
				.getOccurrencesPicker());
	}

	private void setCheckBoxSaveEnabled(RecurrenceInterval recurrenceInterval,
			List<TaskOccurrence> taskOccurrences) {
		LinearLayout occurrencesPicker;
		boolean[] occurrencesOnDaysOfWeek = new boolean[] {false, false, false, false,
				false, false, false};
		for (TaskOccurrence taskOccurrence : taskOccurrences) {
			int ordinalNumber = taskOccurrence.getOrdinalNumber();
			if (ordinalNumber <= occurrencesOnDaysOfWeek.length) {
				occurrencesOnDaysOfWeek[ordinalNumber - 1] = true;
			}
		}
		occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
		TextViewWithCircularIndicator cb1 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_1);
		cb1.setSaveEnabled(false);
		TextViewWithCircularIndicator cb2 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_2);
		cb2.setSaveEnabled(false);
		TextViewWithCircularIndicator cb3 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_3);
		cb3.setSaveEnabled(false);
		TextViewWithCircularIndicator cb4 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_4);
		cb4.setSaveEnabled(false);
		TextViewWithCircularIndicator cb5 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_5);
		cb5.setSaveEnabled(false);
		TextViewWithCircularIndicator cb6 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_6);
		cb6.setSaveEnabled(false);
		TextViewWithCircularIndicator cb7 = (TextViewWithCircularIndicator) occurrencesPicker
				.findViewById(R.id.checkbox_day_of_week_7);
		cb7.setSaveEnabled(false);
		setupDayOfWeekCheckBoxes(new TextViewWithCircularIndicator[] {cb1, cb2, cb3, cb4,
				cb5, cb6, cb7}, occurrencesOnDaysOfWeek);
	}

	private void restoreMonthlyRecurrentSegment() {
		RadioButton radioButtonMonthlyRecurrentOnDate = (RadioButton) getActivity()
				.findViewById(
						R.id.fragment_edit_task_part_main_radio_monthly_recurrent_on_date);
		RadioButton radioButtonMonthlyRecurrentOnNthWeekDay = (RadioButton) getActivity()
				.findViewById(
						R.id.fragment_edit_task_part_main_radio_monthly_recurrent_on_nth_week_day);
		radioButtonMonthlyRecurrentOnDate.setOnCheckedChangeListener(null);
		radioButtonMonthlyRecurrentOnNthWeekDay.setOnCheckedChangeListener(null);
		switch (recurrenceInterval) {
		case MONTHS_ON_DATE:
			radioButtonMonthlyRecurrentOnDate.setChecked(true);
			hideOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay();
			showOccurrencesSelectionSegmentMonthlyRecurrentOnDate();
			break;
		case MONTHS_ON_NTH_WEEK_DAY:
			radioButtonMonthlyRecurrentOnNthWeekDay.setChecked(true);
			hideOccurrencesSelectionSegmentMonthlyRecurrentOnDate();
			showOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay();
			break;
		default:
			restoreMonthlyRecurrentSegmentFromPreference(
					radioButtonMonthlyRecurrentOnDate,
					radioButtonMonthlyRecurrentOnNthWeekDay);
			break;
		}
		LinearLayout occurrencesPicker = RecurrenceInterval.MONTHS_ON_DATE
				.getOccurrencesPicker();
		for (TaskOccurrence taskOccurrence : RecurrenceInterval.MONTHS_ON_DATE
				.getTaskOccurrences()) {
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
					.getChildAt(taskOccurrence.getOrdinalNumber() - 1);
			if (checkBox != null) {
				checkBox.drawIndicator(true);
			}
		}
		setOccurrencesPickerCheckBoxOnCheckedChangeListener(occurrencesPicker);
		RecurrenceInterval recurrenceInterval = RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY;
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		int taskOccurrencesSize = taskOccurrences.size();
		if (taskOccurrencesSize > 0) {
			taskOccurrences = taskOccurrences.subList(0, taskOccurrencesSize - 1);
		}
		setCheckBoxSaveEnabled(recurrenceInterval, taskOccurrences);
		setOccurrencesPickerCheckBoxOnCheckedChangeListener(recurrenceInterval
				.getOccurrencesPicker());
		radioButtonMonthlyRecurrentOnDate.setOnCheckedChangeListener(this);
		radioButtonMonthlyRecurrentOnNthWeekDay.setOnCheckedChangeListener(this);
	}

	private void restoreMonthlyRecurrentSegmentFromPreference(
			RadioButton radioButtonMonthlyRecurrentOnDate,
			RadioButton radioButtonMonthlyRecurrentOnNthWeekDay) {
		MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode
				.fromRecurrenceInterval(getActivity(), recurrenceInterval);
		switch (monthRecurrenceMode) {
		case RECURRENT_ON_DATE:
		default:
			radioButtonMonthlyRecurrentOnDate.setChecked(true);
			hideOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay();
			showOccurrencesSelectionSegmentMonthlyRecurrentOnDate();
			break;
		case RECURRENT_ON_NTH_WEEK_DAY:
			radioButtonMonthlyRecurrentOnNthWeekDay.setChecked(true);
			hideOccurrencesSelectionSegmentMonthlyRecurrentOnDate();
			showOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay();
			break;
		}
	}

	private void hideOccurrencesSelectionSegmentMonthlyRecurrent() {
		linearlayoutOccurrencesPickerMonthlyRecurrent.setVisibility(View.GONE);
	}

	private void showOccurrencesSelectionSegmentMonthlyRecurrentOnDate() {
		scrollviewOccurrencesPickerMonthlyRecurrentOnDate.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentMonthlyRecurrentOnDate() {
		scrollviewOccurrencesPickerMonthlyRecurrentOnDate.setVisibility(View.GONE);
	}

	private void showOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay() {
		linearlayoutOccurrencesPickerMonthlyRecurrentOnNthWeekDay
				.setVisibility(View.VISIBLE);
	}

	private void hideOccurrencesSelectionSegmentMonthlyRecurrentOnNthWeekDay() {
		linearlayoutOccurrencesPickerMonthlyRecurrentOnNthWeekDay
				.setVisibility(View.GONE);
	}

	private boolean collectTimeUnit() {
		collectTaskName();
		boolean isCollected = true;
		mUserInterfaceData.is24HourFormat = is24HourFormat;
		mUserInterfaceData.startTime = startTime;
		mUserInterfaceData.startTimeRequiredAction = startTimeRequiredAction;
		mUserInterfaceData.isRecurrenceDataComplete = isRecurrenceDataComplete;
		mUserInterfaceData.startDateTimePreviousValue = startDateTimePreviousValue;
		mUserInterfaceData.endDateTimePreviousValue = endDateTimePreviousValue;
		mUserInterfaceData.repetitionEndDateTimePreviousValue = repetitionEndDateTimePreviousValue;
		recurrenceInterval = getRecurrenceIntervalFromUserInterface();
		mUserInterfaceData.recurrenceInterval = recurrenceInterval;
		weekDayNumberInMonth = getOrdinalNumberOfOccurrenceOfWeekdayInMonthFromSpinnerOrDefault();
		mUserInterfaceData.weekDayNumberInMonth = weekDayNumberInMonth;
		mUserInterfaceData.recurrenceIntervalValue = recurrenceInterval.getValue();
		mUserInterfaceData.timeUnitDefaultValue = timeUnitDefaultValue;
		mUserInterfaceData.weekDayNumberInMonthDefaultValue = weekDayNumberInMonthDefaultValue;
		mUserInterfaceData.occurrencesMaxCountDefaultValue = occurrencesMaxCountDefaultValue;
		mUserInterfaceData.occurrencesMaxCountMinValue = occurrencesMaxCountMinValue;
		mUserInterfaceData.occurrencesMaxCountMaxValue = occurrencesMaxCountMaxValue;
		mUserInterfaceData.radiogroupMonthBasedRecurrenceMode = radiogroupMonthBasedRecurrenceMode
				.getCheckedRadioButtonId();
		collectTimeUnitsCount();
		mUserInterfaceData.timeUnitsStartingIndexValue = timeUnitsStartingIndex;
		collectOccurrencesMaxCount();
		// if (!isStartDateTimeCollected()) {
		// isCollected = false;
		// }
		collectOccurrencesOfMinutelyOrHourlyOrDailyRecurrentTask(RecurrenceInterval.MINUTES);
		collectOccurrencesOfMinutelyOrHourlyOrDailyRecurrentTask(RecurrenceInterval.HOURS);
		collectOccurrencesOfMinutelyOrHourlyOrDailyRecurrentTask(RecurrenceInterval.DAYS);
		collectOccurrencesOfWeeklyRecurrentTaskOrTaskMonthlyRecurrentOnNthWeekDay(RecurrenceInterval.WEEKS);
		collectOccurrencesOfTaskMonthlyRecurrentOnDate();
		collectOccurrencesOfWeeklyRecurrentTaskOrTaskMonthlyRecurrentOnNthWeekDay(RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY);
		// collectRepetitionsOfYearlyRecurrentTask();
		return isCollected;
	}

	private boolean isTimeUnitCollected() {
		boolean isCollected = true;
		recurrenceInterval = getRecurrenceIntervalFromUserInterface();
		taskUiData.setRecurrenceIntervalValue(recurrenceInterval.getValue());
		if (recurrenceInterval == RecurrenceInterval.ONE_TIME) {
			taskUiData.setTimeUnitsCount(null);
			taskUiData.setOccurrencesMaxCount(null);
			taskOccurrences.clear();
			if (!isStartDateTimeForOneTimeCollected()) {
				isCollected = false;
			}
		} else {
			if (!isTimeUnitsCountCollected() || !isOccurrencesMaxCountCollected()) {
				isCollected = false;
			} else if (!isStartDateTimeCollected()) {
				isCollected = false;
			} else {
				switch (recurrenceInterval) {
				case MINUTES:
				case HOURS:
				case DAYS:
					if (!isOccurrencesOfMinutelyOrHourlyOrDailyRecurrentTaskCollected()) {
						isCollected = false;
					}
					break;
				case WEEKS:
				case MONTHS_ON_NTH_WEEK_DAY:
					if (!isOccurrencesOfWeeklyRecurrentTaskOrTaskMonthlyRecurrentOnNthWeekDayCollected()) {
						isCollected = false;
					}
					break;
				case MONTHS_ON_DATE:
					if (!isOccurrencesOfTaskMonthlyRecurrentOnDateCollected()) {
						isCollected = false;
					}
					break;
				case YEARS:
					if (!isOccurrencesOfYearlyRecurrentTaskCollected()) {
						isCollected = false;
					}
					break;
				case ONE_TIME:
					break;
				default:
					break;
				}
			}
		}
		return isCollected;
	}

	private boolean isStartDateTimeForOneTimeCollected() {
		boolean isCollected = true;
		if (mUserInterfaceData.startDateTime != null
				&& mUserInterfaceData.endDateTime != null
				&& mUserInterfaceData.startDateTime > mUserInterfaceData.endDateTime) {
			isCollected = false;
			taskStartDate.requestFocus();
			Toast.makeText(getActivity(),
					R.string.toast_text_start_time_must_be_before_end_time,
					Toast.LENGTH_LONG).show();
		} else {
			taskUiData.setStartDateTime(mUserInterfaceData.startDateTime);
			taskUiData.setEndDateTime(mUserInterfaceData.endDateTime);
			taskUiData.setRepetitionEndDateTime(null);
		}
		return isCollected;
	}

	private boolean isStartDateTimeCollected() {
		boolean isCollected = true;
		Context context = getActivity();
		Resources resources = context.getResources();
		if (mUserInterfaceData.startDateTime == null) {
			StartTimeRequiredAction action = StartTimeRequiredAction
					.fromInt((short) Helper.getIntegerPreferenceValueFromStringArray(
							context, R.string.preference_key_start_time_required_action,
							R.array.start_time_required_action_values_array,
							R.integer.start_time_required_action_default_value));
			switch (action) {
			case DO_NOT_AUTOMATICALLY_SET:
			default:
				isCollected = false;
				taskStartDate.requestFocus();
				Toast.makeText(context,
						R.string.toast_text_start_date_is_required_for_recurrent_task,
						Toast.LENGTH_LONG).show();
				break;
			case LOAD_FROM_SETTINGS:
				long todayStartTime = FragmentEditTaskPartMain
						.getTodayStartTime(mUserInterfaceData.startTime);
				taskUiData.setStartDateTime(todayStartTime);
				long taskDuration = Helper.getLongPreferenceValue(context,
						R.string.preference_key_task_duration,
						resources.getInteger(R.integer.task_duration_default_value),
						(long) resources.getInteger(R.integer.task_duration_min_value),
						(long) resources.getInteger(R.integer.task_duration_max_value));
				taskUiData.setEndDateTime(todayStartTime + taskDuration);
				// mEntityWithDependents.task
				// .setRepetitionEndDateTime(todayStartTime + 24 * 3600 * 1000);
				break;
			case SET_TO_CURRENT_TIME:
				long currentTime = FragmentEditTaskPartMain.getCurrentTime();
				taskUiData.setStartDateTime(currentTime);
				taskDuration = Helper.getLongPreferenceValue(context,
						R.string.preference_key_task_duration,
						resources.getInteger(R.integer.task_duration_default_value),
						(long) resources.getInteger(R.integer.task_duration_min_value),
						(long) resources.getInteger(R.integer.task_duration_max_value));
				taskUiData.setEndDateTime(currentTime + taskDuration);
				// mEntityWithDependents.task
				// .setRepetitionEndDateTime(currentTime + 24 * 3600 * 1000);
				break;
			}
			// long milliseconds = Helper.getLongPreferenceValue(getActivity(),
			// R.string.preference_key_start_time,
			// getResources().getInteger(R.integer.start_time_default_value),
			// (long) getResources().getInteger(R.integer.start_time_min_value),
			// (long) getResources().getInteger(R.integer.start_time_max_value));
			// FragmentEditTaskPartMain.getTodayStartTime(startTime);
			// int minutes = (int) (milliseconds / (1000 * 60) % 60);
			// int hours = (int) (milliseconds / (1000 * 60 * 60));
		} else {
			long taskDuration;
			if (mUserInterfaceData.endDateTime == null) {
				taskDuration = Helper.getLongPreferenceValue(context,
						R.string.preference_key_task_duration,
						resources.getInteger(R.integer.task_duration_default_value),
						(long) resources.getInteger(R.integer.task_duration_min_value),
						(long) resources.getInteger(R.integer.task_duration_max_value));
				// mUserInterfaceData.endDateTime = mUserInterfaceData.startDateTime
				// + taskDuration;
			} else {
				taskDuration = mUserInterfaceData.endDateTime
						- mUserInterfaceData.startDateTime;
			}
			if (taskDuration < 0) {
				isCollected = false;
				taskStartDate.requestFocus();
				Toast.makeText(context,
						R.string.toast_text_start_time_must_be_before_end_time,
						Toast.LENGTH_LONG).show();
			} else {
				// long taskLength;
				// taskLength = mUserInterfaceData.endDateTime
				// - mUserInterfaceData.startDateTime;
				if (recurrenceInterval.getTimeUnitsCountMustBe() == null) {
					long taskPeriod = FragmentEditTaskPartMain
							.measureTaskPeriod(recurrenceInterval);
					if (taskDuration > taskPeriod) {
						// isCollected = false;
						// edittextTimeUnitsCount.requestFocus();
						// Toast.makeText(
						// context,
						// R.string.toast_text_task_length_is_longer_than_task_recurrence_interval,
						// Toast.LENGTH_SHORT).show();
						taskDuration = taskPeriod;
					}
					taskUiData.setStartDateTime(mUserInterfaceData.startDateTime);
					taskUiData.setEndDateTime(mUserInterfaceData.startDateTime
							+ taskDuration);
					taskUiData
							.setRepetitionEndDateTime(mUserInterfaceData.repetitionEndDateTime);
				}
			}
		}
		return isCollected;
	}

	private static long measureTaskPeriod(RecurrenceInterval recurrenceInterval) {
		int timeUnitsCount = recurrenceInterval.getTimeUnitsCount();
		long taskPeriod;
		switch (recurrenceInterval) {
		case MINUTES:
			taskPeriod = timeUnitsCount * 1000L * 60;
			break;
		case HOURS:
			taskPeriod = timeUnitsCount * 1000L * 60 * 60;
			break;
		case DAYS:
		case WEEKS:
		case MONTHS_ON_NTH_WEEK_DAY:
			taskPeriod = timeUnitsCount * 1000L * 60 * 60 * 24;
			break;
		case MONTHS_ON_DATE:
			taskPeriod = timeUnitsCount * 1000L * 60 * 60 * 24 * 30;
			break;
		case ONE_TIME:
		case YEARS:
		default:
			taskPeriod = timeUnitsCount * 1000L * 60 * 60 * 24 * 365;
			break;
		}
		return taskPeriod;
	}

	private RecurrenceInterval getRecurrenceIntervalFromUserInterface(
			MonthRecurrenceMode monthRecurrenceMode) {
		TimeUnit timeUnit = getTimeUnitFromSpinnerOrDefault();
		RecurrenceInterval recurrenceInterval = RecurrenceInterval.fromTimeUnit(timeUnit,
				monthRecurrenceMode);
		return recurrenceInterval;
	}

	private RecurrenceInterval getRecurrenceIntervalFromUserInterface() {
		TimeUnit timeUnit = getTimeUnitFromSpinnerOrDefault();
		MonthRecurrenceMode monthRecurrenceMode = MonthRecurrenceMode
				.fromRadioGroup(radiogroupMonthBasedRecurrenceMode);
		RecurrenceInterval recurrenceInterval = RecurrenceInterval.fromTimeUnit(timeUnit,
				monthRecurrenceMode);
		return recurrenceInterval;
	}

	private TimeUnit getTimeUnitFromSpinnerOrDefault() {
		int position = spinnerTimeUnit.getSelectedItemPosition();
		if (position < 0) {
			position = Helper.setSpinnerToPreferenceValueFromStringArray(getActivity(),
					spinnerTimeUnit, R.string.preference_key_time_unit,
					R.array.time_unit_values_array, timeUnitDefaultValue, this);
		}
		short timeUnitValue = (short) Helper.getIntegerValueFromStringArray(
				getActivity(), R.array.time_unit_values_array, position,
				timeUnitDefaultValue);
		TimeUnit timeUnit = TimeUnit.fromInt(timeUnitValue);
		return timeUnit;
	}

	private Calendar2 getCalendarFromSpinnerOrDefault() {
		Calendar2 calendar = (Calendar2) spinnerCalendar.getSelectedItem();
		return calendar;
	}

	private int getOrdinalNumberOfOccurrenceOfWeekdayInMonthFromSpinnerOrDefault() {
		int position = spinnerWeekDayNumberInMonth.getSelectedItemPosition();
		if (position < 0) {
			position = Helper.setSpinnerToPreferenceValueFromStringArray(getActivity(),
					spinnerWeekDayNumberInMonth, R.string.preference_key_week_day_number,
					R.array.week_day_number_values_array,
					weekDayNumberInMonthDefaultValue, this);
		}
		int weekDayNumberInMonth = Helper.getIntegerValueFromStringArray(getActivity(),
				R.array.week_day_number_values_array, position,
				weekDayNumberInMonthDefaultValue);
		return weekDayNumberInMonth;
	}

	private boolean collectOccurrencesOfTaskMonthlyRecurrentOnDate() {
		boolean isCollected = true;
		RecurrenceInterval recurrenceInterval = RecurrenceInterval.MONTHS_ON_DATE;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		}
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		taskOccurrences.clear();
		LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
		int checkBoxCount = occurrencesPicker.getChildCount();
		recurrenceInterval.setCheckBoxCount(checkBoxCount);
		for (int i = 0; i < checkBoxCount; i++) {
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
					.getChildAt(i);
			if (checkBox.isDrawingIndicator()) {
				taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(), i + 1));
			}
		}
		return isCollected;
	}

	private boolean isOccurrencesOfTaskMonthlyRecurrentOnDateCollected() {
		boolean isCollected = true;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		} else {
			taskOccurrences.clear();
			LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
			int checkBoxCount = occurrencesPicker.getChildCount();
			for (int i = 0; i < checkBoxCount; i++) {
				TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
						.getChildAt(i);
				if (checkBox.isDrawingIndicator()) {
					taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(),
							i + 1));
				}
			}
			if (!isOccurrencesCollected()) {
				isCollected = false;
			}
		}
		return isCollected;
	}

	private boolean isOccurrencesOfMinutelyOrHourlyOrDailyRecurrentTaskCollected() {
		boolean isCollected = true;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		} else {
			taskOccurrences.clear();
			LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
			int timeUnitsCount = recurrenceInterval.getTimeUnitsCount();
			try {
				for (int i = 0; i < timeUnitsCount; i++) {
					TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
							.getChildAt(i);
					if (checkBox != null) {
						if (checkBox.isDrawingIndicator()) {
							taskOccurrences.add(new TaskOccurrence(null, taskUiData
									.getId(), i + 1));
						}
					} else {
						isCollected = false;
						String text = String
								.format(getResources()
										.getString(
												R.string.error_text_checkbox_count_is_less_than_occurrences_count),
										occurrencesPicker.getChildCount(), timeUnitsCount);
						throw new IllegalStateException(text);
					}
				}
				if (!isOccurrencesCollected()) {
					isCollected = false;
				}
			} catch (Exception e) {
				Helper.showAlertDialog(getActivity(), e);
				throw new IllegalStateException(e);
			}
		}
		return isCollected;
	}

	private boolean isOccurrencesOfYearlyRecurrentTaskCollected() {
		boolean isCollected = true;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		} else {
			taskOccurrences.clear();
			List<TaskOccurrence> taskOccurrences2 = recurrenceInterval
					.getTaskOccurrences();
			int occurrencesCount = taskOccurrences2.size();
			for (int i = 0; i < occurrencesCount; i++) {
				taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(),
						taskOccurrences2.get(i).getOrdinalNumber()));
			}
			if (!isOccurrencesCollected()) {
				isCollected = false;
			}
		}
		return isCollected;
	}

	private boolean collectOccurrencesOfMinutelyOrHourlyOrDailyRecurrentTask(
			RecurrenceInterval recurrenceInterval) {
		boolean isCollected = true;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		}
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		taskOccurrences.clear();
		LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
		int checkBoxCount = occurrencesPicker.getChildCount();
		recurrenceInterval.setCheckBoxCount(checkBoxCount);
		try {
			for (int i = 0; i < checkBoxCount; i++) {
				// for (int i = 0; i < timeUnitsCount; i++) {
				TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
						.getChildAt(i);
				if (checkBox != null) {
					if (checkBox.isDrawingIndicator()) {
						taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(),
								i + 1));
					}
				} else {
					isCollected = false;
					String text = String
							.format(getResources()
									.getString(
											R.string.error_text_checkbox_count_is_less_than_occurrences_count),
									occurrencesPicker.getChildCount(), i);
					throw new IllegalStateException(text);
				}
			}
		} catch (Exception e) {
			Helper.showAlertDialog(getActivity(), e);
			throw new IllegalStateException(e);
		}
		return isCollected;
	}

	private boolean isOccurrencesCollected() {
		boolean isCollected = true;
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		if (taskOccurrences.size() == 0) {
			int timeUnitsCount = recurrenceInterval.getTimeUnitsCount();
			if (timeUnitsCount == 1
					&& (recurrenceInterval == RecurrenceInterval.MINUTES
							|| recurrenceInterval == RecurrenceInterval.HOURS || recurrenceInterval == RecurrenceInterval.DAYS)) {
				taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(),
						timeUnitsCount));
			} else {
				isCollected = false;
				Toast.makeText(getActivity(),
						recurrenceInterval.getWantingItemOccurrences().getText(),
						Toast.LENGTH_LONG).show();
			}
		}
		return isCollected;
	}

	private boolean collectOccurrencesOfWeeklyRecurrentTaskOrTaskMonthlyRecurrentOnNthWeekDay(
			RecurrenceInterval recurrenceInterval) {
		boolean isCollected = true;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		}
		int x = Helper.getFirstDayOfWeek(getActivity());
		List<TaskOccurrence> taskOccurrences = recurrenceInterval.getTaskOccurrences();
		taskOccurrences.clear();
		LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
		int checkBoxCount = occurrencesPicker.getChildCount();
		recurrenceInterval.setCheckBoxCount(checkBoxCount);
		for (int i = 0; i < checkBoxCount; i++) {
			TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
					.getChildAt(i);
			if (checkBox.isDrawingIndicator()) {
				taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(), x));
			}
			x++;
			if (x > checkBoxCount) {
				x = 1;
			}
		}
		if (recurrenceInterval == RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY) {
			int index = spinnerWeekDayNumberInMonth.getSelectedItemPosition();
			mUserInterfaceData.spinnerWeekDayNumberInMonthPosition = index;
			weekDayNumberInMonth = Helper.getIntegerValueFromStringArray(getActivity(),
					R.array.week_day_number_values_array, index,
					weekDayNumberInMonthDefaultValue);
			taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(),
					weekDayNumberInMonth));
			mUserInterfaceData.weekDayNumberInMonth = weekDayNumberInMonth;
		}
		return isCollected;
	}

	private boolean isOccurrencesOfWeeklyRecurrentTaskOrTaskMonthlyRecurrentOnNthWeekDayCollected() {
		boolean isCollected = true;
		if (recurrenceInterval.getTimeUnitsCountMustBe() != null) {
			isCollected = false;
		} else {
			int x = Helper.getFirstDayOfWeek(getActivity());
			taskOccurrences.clear();
			LinearLayout occurrencesPicker = recurrenceInterval.getOccurrencesPicker();
			int checkBoxCount = occurrencesPicker.getChildCount();
			for (int i = 0; i < checkBoxCount; i++) {
				TextViewWithCircularIndicator checkBox = (TextViewWithCircularIndicator) occurrencesPicker
						.getChildAt(i);
				if (checkBox.isDrawingIndicator()) {
					taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(), x));
				}
				x++;
				if (x > checkBoxCount) {
					x = 1;
				}
			}
			if (!isOccurrencesCollected()) {
				isCollected = false;
			} else if (recurrenceInterval == RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY) {
				weekDayNumberInMonth = Helper.getIntegerValueFromStringArray(
						getActivity(), R.array.week_day_number_values_array,
						spinnerWeekDayNumberInMonth.getSelectedItemPosition(),
						weekDayNumberInMonthDefaultValue);
				taskOccurrences.add(new TaskOccurrence(null, taskUiData.getId(),
						weekDayNumberInMonth));
			}
		}
		return isCollected;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		switch (parent.getId()) {
		case R.id.fragment_edit_task_part_main_spinner_time_unit:
			recurrenceInterval = RecurrenceInterval.ONE_TIME;
			mUserInterfaceData.recurrenceInterval = recurrenceInterval;
			setVisibilityBasedOnTimeUnitSelection(recurrenceInterval);
			// LinearLayout linearLayoutSelectWeekDays = (LinearLayout) getActivity()
			// .findViewById(
			// R.id.fragment_edit_task_part_main_layout_week_days_picker_for_weekly_repeated_tasks);
			// linearLayoutSelectWeekDays.setVisibility(View.GONE);
			break;
		}
	}

	class EditTextTaskTextChangedListener implements TextWatcher {
		EditText editText;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// TODO Auto-generated method stub
		}

		@Override
		public void afterTextChanged(Editable text) {
			mUserInterfaceData.textTaskName = text;
		}
	}

	class EditTextTaskTimeUnitsCountChangedListener implements TextWatcher {
		EditText editText;
		TextView textView;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			recurrenceInterval.setTextTimeUnitsCount(s);
			Integer timeUnitsCount = Helper.getValidEditTextIntegerValueOrNull(
					edittextTimeUnitsCount, null, timeUnitsCountMinValue,
					timeUnitsCountMaxValue, false, false);
			ValueMustBe valueMustBe = (ValueMustBe) edittextTimeUnitsCount
					.getTag(R.string.tag_key_value_must_be);
			recurrenceInterval.setTimeUnitsCountMustBe(valueMustBe);
			if (valueMustBe != ValueMustBe.WITHIN_BOUNDS) {
				if (valueMustBe == null) {
					recurrenceInterval.setTimeUnitsCount(timeUnitsCount);
				}
				if (timeUnitsCount == 1) {
					textViewTimeUnit.setText(recurrenceInterval
							.getTextviewIdTimeUnitTextSingular());
				} else {
					textViewTimeUnit.setText(recurrenceInterval
							.getTextviewIdTimeUnitTextPlural());
				}
			}
			if (recurrenceInterval == RecurrenceInterval.MINUTES
					|| recurrenceInterval == RecurrenceInterval.HOURS
					|| recurrenceInterval == RecurrenceInterval.DAYS) {
				adjustOccurrencesPicker(recurrenceInterval.getOccurrencesPicker());
			}
		}
	}

	class EditTextTaskOccurrencesMaxCountChangedListener implements TextWatcher {
		EditText editText;

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}

	@Override
	public void onClick(View v) {
		int callerId = v.getId();
		FragmentActivity activity = getActivity();
		switch (callerId) {
		case R.id.fragment_edit_task_part_main_checkedtextview_task_parent:
		case R.id.fragment_edit_task_part_main_textview_task_parent:
			if (!checkedTextView.isChecked()) {
				Bundle bundle = new Bundle();
				bundle.putInt(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE,
						CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_TASK_TREE);
				bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
						AbsListView.CHOICE_MODE_SINGLE);
				// if (mEntityWithDependents.task.getId() != null) {
				if (mUserInterfaceData.id != null) {
					bundle.putLong(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE,
							mUserInterfaceData.id);
				}
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK,
						true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK,
						false);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK,
						true);
				Intent intent = new Intent(activity, ActivitySelectTreeItems.class);
				intent.putExtra(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE, bundle);
				intent.putExtra(
						ActivitySelectTreeItems.IntentExtraTitle,
						getResources().getString(
								R.string.activity_select_task_title_select_parent_task));
				startActivityForResult(intent,
						CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE);
				scrollPos = scrollView.getScrollY();
				FragmentEditTaskPartMain.mapStringToScrollPos.put(selectedConfiguration,
						scrollPos);
			} else {
				TextView textView = (TextView) activity
						.findViewById(R.id.fragment_edit_task_part_main_textview_task_parent);
				textView.setTextColor(mDefaultTextColorStateList);
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					textView.setBackground(mDefaultBackground);
				} else {
					textView.setBackgroundDrawable(mDefaultBackground);
				}
				// if (android.os.Build.VERSION.SDK_INT >= 16) {
				// textViewTaskParent.setBackground(mDefaultBackground
				// .getConstantState().newDrawable());
				// } else {
				// textViewTaskParent.setBackgroundDrawable(mDefaultBackground
				// .getConstantState().newDrawable());
				// }
				checkedTextView.setChecked(false);
				textView.setText(R.string.fragment_edit_task_part_main_checkbox_task_parent_not_set);
				taskUiData.setParentId(null);
				mUserInterfaceData.parentId = null;
				radioButtonSortOrderFirst.setChecked(true);
			}
			break;
		case R.id.fragment_edit_task_part_main_radiobutton_task_sortorder_aftertask:
		case R.id.fragment_edit_task_part_main_textview_task_sortorder_aftertask:
			Intent intent = new Intent(activity, ActivitySelectTaskSortOrder.class);
			// intent.putExtra(CommonConstants.INTENT_EXTRA_TASK,
			// mEntityWithDependents.task);
			intent.putExtra(CommonConstants.INTENT_EXTRA_USER_INTERFACE_DATA,
					mUserInterfaceData);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_SELECT_TASK_FOR_SORT_ORDER);
			scrollPos = scrollView.getScrollY();
			FragmentEditTaskPartMain.mapStringToScrollPos.put(selectedConfiguration,
					scrollPos);
			break;
		case R.id.button_days_picker_of_yearly_recurrent_task:
			intent = new Intent(activity, ActivitySelectDaysOfYear2.class);
			int size = RecurrenceInterval.YEARS.getTaskOccurrences().size();
			HashSet<Integer> selectedDays = new HashSet<Integer>(size);
			// ArrayList<Integer> selectedDays2 = new ArrayList<Integer>(size);
			for (int i = 0; i < size; i++) {
				selectedDays.add(RecurrenceInterval.YEARS.getTaskOccurrences().get(i)
						.getOrdinalNumber());
			}
			intent.putExtra(CommonConstants.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR,
					selectedDays);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_SELECT_DAYS_OF_YEAR);
			scrollPos = scrollView.getScrollY();
			FragmentEditTaskPartMain.mapStringToScrollPos.put(selectedConfiguration,
					scrollPos);
			break;
		}
	}

	private static long getCurrentTime() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTimeInMillis();
	}

	private static long getTodayStartTime(long milliseconds) {
		int minutes = (int) (milliseconds / (1000 * 60) % 60);
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, hours);
		now.set(Calendar.MINUTE, minutes);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		return now.getTimeInMillis();
	}

	@Override
	public Set<WantingItem> getWantingItemSetForRecurrenceData() {
		return mUserInterfaceData.wantingItemSet;
	}

	@Override
	public Set<WantingItem> getWantingItemSetForTask() {
		return mUserInterfaceData.wantingItemSetForTask;
	}

	@Override
	public Set<WantingItem> testTaskUiDataWithCurrentState() {
		collectTimeUnit();
		Context context = getActivity();
		mUserInterfaceData.isRecurrenceDataComplete(context);
		return mUserInterfaceData.wantingItemSetForTask;
	}

	@Override
	public TaskUiData getTaskUiDataWithCurrentState() {
		// temporary code
		collectTimeUnit();
		Context context = getActivity();
		if (mUserInterfaceData.isRecurrenceDataComplete(context)) {
			return mUserInterfaceData.taskUiData;
		} else {
			return null;
		}
	}

	public void setupUi(TaskUiData taskUiData2) {
		restoreTaskNameSegmentForTask(taskUiData2.getName());
		taskStartDate.setTaskDateTime(taskUiData2.getStartDateTime());
		setupDateTimeCheckboxes(taskStartDate);
		taskEndDate.setTaskDateTime(taskUiData2.getEndDateTime());
		setupDateTimeCheckboxes(taskEndDate);
		taskRepetitionEndDate.setTaskDateTime(taskUiData2.getRepetitionEndDateTime());
		setupDateTimeCheckboxes(taskRepetitionEndDate);
	}

	public void setupUiPending(TaskUiData taskUiData2) {
		setupUiPending = true;
		setupUiTaskUiData2 = taskUiData2;
	}
}
