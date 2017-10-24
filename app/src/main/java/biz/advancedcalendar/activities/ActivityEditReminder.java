package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.EditTextValueChangedListener;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.utils.Helper;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialog;
import com.android.supportdatetimepicker.time.TimePickerDialog.OnTimeSetListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

/** Activity which displays a task reminder edit screen to the user. */
public class ActivityEditReminder extends AppCompatActivity implements OnClickListener,
		OnCheckedChangeListener, OnDateSetListener, OnTimeSetListener {
	private boolean mActionDonePerformed = false;
	private String mDatePickerDialogKey = "biz.advancedcalendar.activities.ActivityEditReminder.DatePickerDialogKey";
	private String mTimePickerDialogKey = "biz.advancedcalendar.activities.ActivityEditReminder.TimePickerDialog";
	private long mLastSelectedAbsoluteTime;
	private long mLastSelectedTimeSpanMillisAfterNow;
	private long mLastSelectedTimeSpanMillisBeforeEvent;
	private Bundle bundle;
	private ReminderUiData mReminder;
	private boolean is24HourFormat;
	private BroadcastReceiver mReceiver;
	private ActivityEditReminderParcelableDataStore mParcelableDataStore;
	private RadioButton radiobuttonAbsolute;
	private RadioButton radiobuttonAfterNow;
	private RadioButton lastSelectedRadiobuttonAbsoluteOrAfterNow;
	private RadioButton radiobuttonBeforeEvent;
	private RadioButton lastSelectedRadiobuttonBeforeEventOrAfterEvent;
	private RadioButton radiobuttonAfterEvent;
	// private UserInterfaceData mUserInterfaceData;
	// private TaskWithDependentsUiData mTaskWithDependentsUiData;
	private LinearLayout advancedSettingsSegment;
	private Set<WantingItem> wantingItemSet;
	private String radiobuttonTextCustom;
	private LinearLayout linearlayoutRingtoneFadeInTimeCustomSegment;
	private LinearLayout linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment;
	private LinearLayout linearlayoutAutomaticSnoozeDurationCustomSegment;
	private LinearLayout linearlayoutAutomaticSnoozesMaxCountCustomSegment;
	private LinearLayout linearlayoutVibrateWithAlarmRingtoneCustomSegment;
	private RadioButton radiobuttonVibrateWithAlarmRingtoneCustom;
	private EditTextValueChangedListener edittextRingtoneFadeInTimeChangedListener;
	private EditTextValueChangedListener edittextRemindersPopupWindowDisplayingDurationChangedListener;
	private EditTextValueChangedListener edittextAutomaticSnoozeDurationChangedListener;
	private EditTextValueChangedListener edittextAutomaticSnoozesMaxCountChangedListener;
	private CheckBox checkboxVibrateWithAlarmRingtone;
	private boolean isRadioButtonAlarmRingtoneCustomChecked;
	private boolean isRadioButtonNotificationRingtoneCustomChecked;
	private String alarmRingtoneCustom;
	private String notificationRingtoneCustom;
	private LinearLayout linearlayoutReminderTimeAbsolute;
	private RelativeLayout relativelayoutReminderTimeBeforeEvent;
	private RelativeLayout relativelayoutReminderTimeAfterNow;
	private LinearLayout linearlayoutAlarmRingtoneSegment;
	private LinearLayout linearlayoutNotificationRingtoneSegment;
	private Task task;
	private boolean isAlarm;
	private EditText edittextTitle;
	private RadioGroup radiogroupNotificationAlarm;
	private RadioGroup radiogroupReminderTime;
	private Toolbar mToolbar;
	private static final boolean Debug = true;
	private static final String DebugTag = "CreateReminderDebugTag";

	public ActivityEditReminder() {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag,
					"ActivityEditReminder() " + System.identityHashCode(this));
		}
	}

	/** @return the mParcelableDataStore */
	public ActivityEditReminderParcelableDataStore getmParcelableDataStore() {
		return mParcelableDataStore;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag,
					"ActivityEditReminder " + "onCreate(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_reminder);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		FragmentManager fm = getSupportFragmentManager();
//		mParcelableDataStore = (ActivityEditReminderParcelableDataStore) fm
//				.findFragmentByTag(CommonConstants.DATA_FRAGMENT);
		Resources resources = getResources();
		edittextTitle = (EditText) findViewById(R.id.activity_edit_reminder_edittext_reminder_description);
		radiogroupNotificationAlarm = (RadioGroup) findViewById(R.id.activity_edit_reminder_radiogroup_notification_alarm);
		radiogroupReminderTime = (RadioGroup) findViewById(R.id.activity_edit_reminder_radiogroup_reminder_time);
		wantingItemSet = EnumSet.noneOf(WantingItem.class);
		if (savedInstanceState != null) {
			mParcelableDataStore = savedInstanceState.getParcelable(CommonConstants.INIT_ARGUMENTS);
		} else {
			if (mParcelableDataStore == null) {
				mParcelableDataStore = getIntent().getParcelableExtra(CommonConstants.INIT_ARGUMENTS);
			}
		}
		{
			bundle = mParcelableDataStore.bundle;
			TimePickerDialog tpd = (TimePickerDialog) fm
					.findFragmentByTag(mTimePickerDialogKey);
			if (tpd != null) {
				tpd.setOnTimeSetListener(this);
			}
			DatePickerDialog dpd = (DatePickerDialog) fm
					.findFragmentByTag(mDatePickerDialogKey);
			if (dpd != null) {
				dpd.setOnDateSetListener(this);
			}
		}
		// mTaskWithDependentsUiData = mParcelableDataStore.taskWithDependentsUiData;
		// mUserInterfaceData = mParcelableDataStore.userInterfaceData;
		mReminder = mParcelableDataStore.reminderUiData;
		is24HourFormat = mParcelableDataStore.is24HourFormat;
		mLastSelectedAbsoluteTime = mParcelableDataStore.lastSelectedAbsoluteTime;
		mLastSelectedTimeSpanMillisAfterNow = mParcelableDataStore.lastSelectedTimeSpanMillisAfterNow;
		mLastSelectedTimeSpanMillisBeforeEvent = mParcelableDataStore.lastSelectedTimeSpanMillisBeforeEvent;
		radiobuttonAbsolute = (RadioButton) findViewById(R.id.activity_edit_reminder_radio_reminder_time_absolute);
		radiobuttonAfterNow = (RadioButton) findViewById(R.id.activity_edit_reminder_radio_reminder_time_after_now);
		radiobuttonBeforeEvent = (RadioButton) findViewById(R.id.activity_edit_reminder_radio_reminder_time_before_event);
		radiobuttonAfterEvent = (RadioButton) findViewById(R.id.activity_edit_reminder_radio_reminder_time_after_event);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(CommonConstants.ACTION_START_TIME_SELECTED)) {
				} else if (intent.getAction().equals(
						CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED)) {
					is24HourFormat = Helper.is24HourFormat(getApplicationContext());
					mParcelableDataStore.is24HourFormat = is24HourFormat;
				}
			}
		};
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommonConstants.ACTION_START_TIME_SELECTED);
		intentFilter.addAction(CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
				mReceiver, intentFilter);
		final ActionBar actionBar = getSupportActionBar();
		task = mReminder.getTask();
		String name = task.getName().trim();
		if (name.equals("")) {
			actionBar.setTitle(getTitle());
		} else {
			actionBar.setTitle(name);
		}
		// setup listeners and set values on views
		// setup reminder enabled checkbox
		CheckBox checkBox = (CheckBox) findViewById(R.id.activity_edit_reminder_checkbox_reminder_enabled);
		checkBox.setText(mReminder.getEnabled() ? R.string.fragment_view_task_part_reminders_list_item_checkbox_reminder_enabled
				: R.string.fragment_view_task_part_reminders_list_item_checkbox_reminder_disabled);
		checkBox.setChecked(mReminder.getEnabled());
		checkBox.setOnCheckedChangeListener(this);
		// setup reminder text
		edittextTitle.setText("");
		edittextTitle.append(mReminder.getText() == null ? "" : mReminder.getText());
		//
		linearlayoutAlarmRingtoneSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_alarm_ringtone_segment);
		linearlayoutNotificationRingtoneSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_notification_ringtone_segment);
		RadioButton rbAlarm = (RadioButton) findViewById(R.id.activity_edit_reminder_radio_alarm);
		RadioButton rbNotification = (RadioButton) findViewById(R.id.activity_edit_reminder_radio_notification);
		isAlarm = mReminder.getIsAlarm();
		if (isAlarm) {
			rbAlarm.setChecked(true);
			alarmRingtoneCustom = mReminder.getRingtone();
			linearlayoutAlarmRingtoneSegment.setVisibility(View.VISIBLE);
			linearlayoutNotificationRingtoneSegment.setVisibility(View.GONE);
		} else {
			notificationRingtoneCustom = mReminder.getRingtone();
			rbNotification.setChecked(true);
			linearlayoutNotificationRingtoneSegment.setVisibility(View.VISIBLE);
			linearlayoutAlarmRingtoneSegment.setVisibility(View.GONE);
		}
		rbAlarm.setOnCheckedChangeListener(this);
		rbNotification.setOnCheckedChangeListener(this);
		linearlayoutReminderTimeAbsolute = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_reminder_time_absolute);
		relativelayoutReminderTimeBeforeEvent = (RelativeLayout) findViewById(R.id.activity_edit_reminder_relativelayout_reminder_time_before_event);
		relativelayoutReminderTimeAfterNow = (RelativeLayout) findViewById(R.id.activity_edit_reminder_relativelayout_reminder_time_after_now_hours_minutes);
		switch (ReminderTimeMode.fromInt(mReminder.getReminderTimeModeValue())) {
		case ABSOLUTE_TIME:
			lastSelectedRadiobuttonAbsoluteOrAfterNow = radiobuttonAbsolute;
			lastSelectedRadiobuttonBeforeEventOrAfterEvent = (RadioButton) findViewById(mParcelableDataStore.lastSelectedRadiobuttonIdBeforeEventOrAfterEvent);
			radiobuttonAbsolute.setChecked(true);
			relativelayoutReminderTimeBeforeEvent.setVisibility(View.GONE);
			relativelayoutReminderTimeAfterNow.setVisibility(View.GONE);
			linearlayoutReminderTimeAbsolute.setVisibility(View.VISIBLE);
			break;
		case AFTER_NOW:
			lastSelectedRadiobuttonAbsoluteOrAfterNow = radiobuttonAfterNow;
			lastSelectedRadiobuttonBeforeEventOrAfterEvent = (RadioButton) findViewById(mParcelableDataStore.lastSelectedRadiobuttonIdBeforeEventOrAfterEvent);
			radiobuttonAfterNow.setChecked(true);
			linearlayoutReminderTimeAbsolute.setVisibility(View.GONE);
			relativelayoutReminderTimeBeforeEvent.setVisibility(View.GONE);
			relativelayoutReminderTimeAfterNow.setVisibility(View.VISIBLE);
			break;
		case TIME_BEFORE_EVENT:
			lastSelectedRadiobuttonBeforeEventOrAfterEvent = radiobuttonBeforeEvent;
			lastSelectedRadiobuttonAbsoluteOrAfterNow = (RadioButton) findViewById(mParcelableDataStore.lastSelectedRadiobuttonIdAbsoluteOrAfterNow);
			radiobuttonBeforeEvent.setChecked(true);
			linearlayoutReminderTimeAbsolute.setVisibility(View.GONE);
			relativelayoutReminderTimeAfterNow.setVisibility(View.GONE);
			relativelayoutReminderTimeBeforeEvent.setVisibility(View.VISIBLE);
			break;
		case TIME_AFTER_EVENT:
			lastSelectedRadiobuttonBeforeEventOrAfterEvent = radiobuttonAfterEvent;
			lastSelectedRadiobuttonAbsoluteOrAfterNow = (RadioButton) findViewById(mParcelableDataStore.lastSelectedRadiobuttonIdAbsoluteOrAfterNow);
			radiobuttonAfterEvent.setChecked(true);
			linearlayoutReminderTimeAbsolute.setVisibility(View.GONE);
			relativelayoutReminderTimeAfterNow.setVisibility(View.GONE);
			relativelayoutReminderTimeBeforeEvent.setVisibility(View.VISIBLE);
			break;
		}
		// setup button text for reminder type absolute (Time and Date buttons)
		Button button1 = (Button) findViewById(R.id.button_reminder_date);
		button1.setOnClickListener(this);
		Button button2 = (Button) findViewById(R.id.button_reminder_time);
		button2.setOnClickListener(this);
		DateFormat mDateFormat = DateFormat.getDateInstance();
		String dateString = mDateFormat.format(new Date(mLastSelectedAbsoluteTime));
		button1.setText(dateString);
		mDateFormat = DateFormat.getTimeInstance();
		String timeString = mDateFormat.format(new Date(mLastSelectedAbsoluteTime));
		button2.setText(timeString);
		// setup button text for reminder type offset (TimeSpan button)
		Button button;
		button = (Button) findViewById(R.id.button_reminder_time_after_now);
		button.setOnClickListener(this);
		setTimeSpanButtonText(R.id.button_reminder_time_after_now,
				(int) (mLastSelectedTimeSpanMillisAfterNow / 1000 / 60 / 60 % 24),
				(int) (mLastSelectedTimeSpanMillisAfterNow / 1000 / 60 % 60));
		button = (Button) findViewById(R.id.button_reminder_time_before_event);
		button.setOnClickListener(this);
		setTimeSpanButtonText(R.id.button_reminder_time_before_event,
				(int) (mLastSelectedTimeSpanMillisBeforeEvent / 1000 / 60 / 60 % 24),
				(int) (mLastSelectedTimeSpanMillisBeforeEvent / 1000 / 60 % 60));
		advancedSettingsSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_advanced_settings);
		checkBox = (CheckBox) findViewById(R.id.activity_edit_reminder_checkbox_advanced_settings);
		boolean collapsed = Helper
				.getBooleanPreferenceValue(
						this,
						R.string.preference_key_activity_edit_reminder_advanced_settings_expand_state,
						R.bool.activity_edit_reminder_advanced_settings_expand_state);
		checkBox.setChecked(collapsed);
		if (collapsed) {
			checkBox.setText(R.string.action_less);
			advancedSettingsSegment.setVisibility(View.VISIBLE);
		} else {
			checkBox.setText(R.string.action_more);
			advancedSettingsSegment.setVisibility(View.GONE);
		}
		checkBox.setOnCheckedChangeListener(this);
		radiobuttonTextCustom = resources
				.getString(R.string.activity_edit_reminder_radiobutton_text_custom);
		linearlayoutRingtoneFadeInTimeCustomSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_ringtone_fade_in_time_custom_segment);
		linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_reminders_popup_window_displaying_duration_custom_segment);
		linearlayoutAutomaticSnoozeDurationCustomSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_automatic_snooze_duration_custom_segment);
		linearlayoutAutomaticSnoozesMaxCountCustomSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_automatic_snoozes_max_count_custom_segment);
		linearlayoutVibrateWithAlarmRingtoneCustomSegment = (LinearLayout) findViewById(R.id.activity_edit_reminder_linearlayout_vibrate_with_alarm_ringtone_custom_segment);
		radiobuttonVibrateWithAlarmRingtoneCustom = (RadioButton) findViewById(R.id.activity_edit_reminder_radiobutton_vibrate_with_alarm_ringtone_custom);
		checkboxVibrateWithAlarmRingtone = (CheckBox) findViewById(R.id.activity_edit_reminder_checkbox_vibrate_with_alarm_ringtone);
		isRadioButtonAlarmRingtoneCustomChecked = EditTextValueChangedListener
				.setupRadiogroupRingtoneFromTask(this,
						R.string.preference_key_alarm_ringtone,
						R.id.activity_edit_reminder_radiobutton_alarm_ringtone_from_task,
						R.id.activity_edit_reminder_radiobutton_alarm_ringtone_custom,
						task, alarmRingtoneCustom, mReminder, this, this);
		isRadioButtonNotificationRingtoneCustomChecked = EditTextValueChangedListener
				.setupRadiogroupRingtoneFromTask(
						this,
						R.string.preference_key_notification_ringtone,
						R.id.activity_edit_reminder_radiobutton_notification_ringtone_from_task,
						R.id.activity_edit_reminder_radiobutton_notification_ringtone_custom,
						task, notificationRingtoneCustom, mReminder, this, this);
		if (alarmRingtoneCustom == null) {
			alarmRingtoneCustom = task.getAlarmRingtone();
		}
		if (notificationRingtoneCustom == null) {
			notificationRingtoneCustom = task.getNotificationRingtone();
		}
		edittextRingtoneFadeInTimeChangedListener = EditTextValueChangedListener
				.setupRadiogroupForModifiedIntegerValueWithResourceBounds(
						this,
						R.id.activity_edit_reminder_textview_ringtone_fade_in_time_measurement_unit,
						R.id.activity_edit_reminder_radiobutton_ringtone_fade_in_time_from_task,
						R.id.activity_edit_reminder_radiobutton_ringtone_fade_in_time_custom,
						R.id.activity_edit_reminder_edittext_ringtone_fade_in_time,
						R.string.preference_key_ringtone_fade_in_time,
						R.integer.ringtone_fade_in_time_divider,
						R.integer.ringtone_fade_in_time_default_value,
						R.integer.ringtone_fade_in_time_min_value,
						R.integer.ringtone_fade_in_time_max_value,
						R.string.text_second,
						R.string.text_seconds,
						R.string.text_seconds_count_singular,
						R.string.text_seconds_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.RINGTONE_FADE_IN_TIME_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.RINGTONE_FADE_IN_TIME_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.RINGTONE_FADE_IN_TIME_TO_BE_WITHIN_BOUNDS,
						R.id.activity_edit_reminder_linearlayout_ringtone_fade_in_time_custom_segment,
						task.getRingtoneFadeInTime(), mReminder.getRingtoneFadeInTime(),
						this, false);
		edittextRemindersPopupWindowDisplayingDurationChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithResourceBounds(
						this,
						R.id.activity_edit_reminder_textview_reminders_popup_window_displaying_duration_measurement_unit,
						R.id.activity_edit_reminder_radiobutton_reminders_popup_window_displaying_duration_from_task,
						R.id.activity_edit_reminder_radiobutton_reminders_popup_window_displaying_duration_custom,
						R.id.activity_edit_reminder_edittext_reminders_popup_window_displaying_duration,
						R.string.preference_key_reminders_popup_window_displaying_duration,
						R.integer.reminders_popup_window_displaying_duration_default_value,
						R.integer.reminders_popup_window_displaying_duration_min_value,
						R.integer.reminders_popup_window_displaying_duration_max_value,
						R.string.text_second,
						R.string.text_seconds,
						R.string.text_seconds_count_singular,
						R.string.text_seconds_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_WITHIN_BOUNDS,
						R.id.activity_edit_reminder_linearlayout_reminders_popup_window_displaying_duration_custom_segment,
						task.getPlayingTime(), mReminder.getPlayingTime(), this, false);
		edittextAutomaticSnoozeDurationChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithResourceBounds(
						this,
						R.id.activity_edit_reminder_textview_automatic_snooze_duration_measurement_unit,
						R.id.activity_edit_reminder_radiobutton_automatic_snooze_duration_from_task,
						R.id.activity_edit_reminder_radiobutton_automatic_snooze_duration_custom,
						R.id.activity_edit_reminder_edittext_automatic_snooze_duration,
						R.string.preference_key_automatic_snooze_duration,
						R.integer.automatic_snooze_duration_default_value,
						R.integer.automatic_snooze_duration_min_value,
						R.integer.automatic_snooze_duration_max_value,
						R.string.text_minute,
						R.string.text_minutes,
						R.string.text_minutes_count_singular,
						R.string.text_minutes_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.AUTOMATIC_SNOOZE_DURATION_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZE_DURATION_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZE_DURATION_TO_BE_WITHIN_BOUNDS,
						R.id.activity_edit_reminder_linearlayout_automatic_snooze_duration_custom_segment,
						task.getAutomaticSnoozeDuration(),
						mReminder.getAutomaticSnoozeDuration(), this, false);
		edittextAutomaticSnoozesMaxCountChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithResourceBounds(
						this,
						R.id.activity_edit_reminder_textview_automatic_snoozes_max_count_measurement_unit,
						R.id.activity_edit_reminder_radiobutton_automatic_snoozes_max_count_from_task,
						R.id.activity_edit_reminder_radiobutton_automatic_snoozes_max_count_custom,
						R.id.activity_edit_reminder_edittext_automatic_snoozes_max_count,
						R.string.preference_key_automatic_snoozes_max_count,
						R.integer.automatic_snoozes_max_count_default_value,
						R.integer.automatic_snoozes_max_count_min_value,
						R.integer.automatic_snoozes_max_count_max_value,
						R.string.textview_automatic_snoozes_max_count_text_measurement_unit_singular,
						R.string.textview_automatic_snoozes_max_count_text_measurement_unit_plural,
						R.string.pref_summary_automatic_snoozes_max_count_singular,
						R.string.pref_summary_automatic_snoozes_max_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_WITHIN_BOUNDS,
						R.id.activity_edit_reminder_linearlayout_automatic_snoozes_max_count_custom_segment,
						task.getAutomaticSnoozesMaxCount(),
						mReminder.getAutomaticSnoozesMaxCount(), this, false);
		EditTextValueChangedListener
				.setupRadiogroupForBooleanValue(
						this,
						R.id.activity_edit_reminder_radiobutton_vibrate_with_alarm_ringtone_from_task,
						R.id.activity_edit_reminder_radiobutton_vibrate_with_alarm_ringtone_custom,
						R.id.activity_edit_reminder_checkbox_vibrate_with_alarm_ringtone,
						R.string.preference_key_vibrate_with_alarm_ringtone,
						R.bool.vibrate_with_alarm_ringtone_default_value,
						R.string.vibrate,
						R.string.do_not_vibrate,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom,
						R.string.custom,
						R.id.activity_edit_reminder_linearlayout_vibrate_with_alarm_ringtone_custom_segment,
						task.getVibrate(), mReminder.getVibrate(), this, false);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag,
					"ActivityEditReminder " + "onPostCreate(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onPostCreate(savedInstanceState);
		radiobuttonAbsolute.setOnCheckedChangeListener(this);
		radiobuttonAfterNow.setOnCheckedChangeListener(this);
		radiobuttonBeforeEvent.setOnCheckedChangeListener(this);
		radiobuttonAfterEvent.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag,
					"ActivityEditReminder "
							+ "onSaveInstanceState(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onSaveInstanceState(savedInstanceState);
		mParcelableDataStore.lastSelectedRadiobuttonIdBeforeEventOrAfterEvent = lastSelectedRadiobuttonBeforeEventOrAfterEvent
				.getId();
		mParcelableDataStore.lastSelectedRadiobuttonIdAbsoluteOrAfterNow = lastSelectedRadiobuttonAbsoluteOrAfterNow
				.getId();
		savedInstanceState.putParcelable(CommonConstants.INIT_ARGUMENTS, mParcelableDataStore);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag, "ActivityEditReminder "
					+ "onCreateOptionsMenu(Menu menu) " + System.identityHashCode(this));
		}
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_CANCEL, 0,
				getResources().getString(R.string.action_cancel));
		menuItem.setIcon(R.drawable.ic_cancel_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SAVE, 0, getResources()
				.getString(R.string.action_done));
		menuItem.setIcon(R.drawable.ic_save_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_SAVE:
			if (isDataCollected()) {
				mActionDonePerformed = true;
				finish();
			}
			return true;
		case CommonConstants.MENU_ID_CANCEL:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag,
					"ActivityEditReminder "
							+ "onActivityResult(int requestCode, int resultCode, Intent intent)  "
							+ System.identityHashCode(this));
		}
		// Check which request it is we're responding to
		switch (requestCode) {
		case CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE:
			if (resultCode == android.app.Activity.RESULT_OK) {
				isRadioButtonAlarmRingtoneCustomChecked = true;
				Uri uri = intent
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				String ringtone;
				if (uri != null) {
					ringtone = uri.toString();
				} else {
					ringtone = "";
				}
				mReminder.setRingtone(ringtone);
				alarmRingtoneCustom = ringtone;
				String textCustom = String.format(
						getResources().getString(
								R.string.activity_edit_reminder_radiobutton_text_custom),
						mReminder.getRingtoneTitle(this));
				RadioButton radioButtonCustom = (RadioButton) findViewById(R.id.activity_edit_reminder_radiobutton_alarm_ringtone_custom);
				radioButtonCustom.setText(textCustom);
			} else {
				if (!isRadioButtonAlarmRingtoneCustomChecked) {
					RadioButton radioButtonDefault = (RadioButton) findViewById(R.id.activity_edit_reminder_radiobutton_alarm_ringtone_from_task);
					radioButtonDefault.setOnCheckedChangeListener(null);
					radioButtonDefault.setChecked(true);
					radioButtonDefault.setOnCheckedChangeListener(this);
				}
			}
			break;
		case CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE:
			if (resultCode == android.app.Activity.RESULT_OK) {
				isRadioButtonNotificationRingtoneCustomChecked = true;
				Uri uri = intent
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				String ringtone;
				if (uri != null) {
					ringtone = uri.toString();
				} else {
					ringtone = "";
				}
				mReminder.setRingtone(ringtone);
				notificationRingtoneCustom = ringtone;
				String textCustom = String.format(
						getResources().getString(
								R.string.activity_edit_reminder_radiobutton_text_custom),
						mReminder.getRingtoneTitle(this));
				RadioButton radioButtonCustom = (RadioButton) findViewById(R.id.activity_edit_reminder_radiobutton_notification_ringtone_custom);
				radioButtonCustom.setText(textCustom);
			} else {
				if (!isRadioButtonNotificationRingtoneCustomChecked) {
					RadioButton radioButtonDefault = (RadioButton) findViewById(R.id.activity_edit_reminder_radiobutton_notification_ringtone_from_task);
					radioButtonDefault.setOnCheckedChangeListener(null);
					radioButtonDefault.setChecked(true);
					radioButtonDefault.setOnCheckedChangeListener(this);
				}
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_edit_reminder_radiobutton_alarm_ringtone_custom:
			Intent intent = Helper.initializeIntentForRingtonePicker(this,
					alarmRingtoneCustom, R.string.preference_key_alarm_ringtone,
					RingtoneManager.TYPE_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI,
					CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE);
			break;
		case R.id.activity_edit_reminder_radiobutton_notification_ringtone_custom:
			intent = Helper.initializeIntentForRingtonePicker(this,
					notificationRingtoneCustom,
					R.string.preference_key_notification_ringtone,
					RingtoneManager.TYPE_NOTIFICATION,
					Settings.System.DEFAULT_NOTIFICATION_URI,
					CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE);
			break;
		case R.id.button_reminder_date:
			bundle = new Bundle();
			bundle.putInt(CommonConstants.CALLER_ID, R.id.button_reminder_date);
			mParcelableDataStore.bundle = bundle;
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
			DatePickerDialog dpd = DatePickerDialog.newInstance(this,
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));
			dpd.show(getSupportFragmentManager(), mDatePickerDialogKey);
			break;
		case R.id.button_reminder_time:
			bundle = new Bundle();
			bundle.putInt(CommonConstants.CALLER_ID, R.id.button_reminder_time);
			mParcelableDataStore.bundle = bundle;
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
			TimePickerDialog tpd = TimePickerDialog.newInstance(this,
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
					is24HourFormat);
			tpd.show(getSupportFragmentManager(), mTimePickerDialogKey);
			break;
		case R.id.button_reminder_time_after_now:
			bundle = new Bundle();
			bundle.putInt(CommonConstants.CALLER_ID, R.id.button_reminder_time_after_now);
			mParcelableDataStore.bundle = bundle;
			tpd = TimePickerDialog.newInstance(this,
					(int) (mLastSelectedTimeSpanMillisAfterNow / 1000 / 60 / 60 % 24),
					(int) (mLastSelectedTimeSpanMillisAfterNow / 1000 / 60 % 60), true);
			tpd.show(getSupportFragmentManager(), mTimePickerDialogKey);
			break;
		case R.id.button_reminder_time_before_event:
			bundle = new Bundle();
			bundle.putInt(CommonConstants.CALLER_ID,
					R.id.button_reminder_time_before_event);
			mParcelableDataStore.bundle = bundle;
			tpd = TimePickerDialog
					.newInstance(
							this,
							(int) (mLastSelectedTimeSpanMillisBeforeEvent / 1000 / 60 / 60 % 24),
							(int) (mLastSelectedTimeSpanMillisBeforeEvent / 1000 / 60 % 60),
							true);
			tpd.show(getSupportFragmentManager(), mTimePickerDialogKey);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.activity_edit_reminder_checkbox_advanced_settings:
			if (isChecked) {
				buttonView.setText(R.string.action_less);
				advancedSettingsSegment.setVisibility(View.VISIBLE);
			} else {
				buttonView.setText(R.string.action_more);
				advancedSettingsSegment.setVisibility(View.GONE);
			}
			PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putBoolean(
							getResources()
									.getString(
											R.string.preference_key_activity_edit_reminder_advanced_settings_expand_state),
							isChecked).commit();
			break;
		case R.id.activity_edit_reminder_checkbox_reminder_enabled:
			if (isChecked) {
				buttonView.setText(R.string.activity_edit_reminder_checkbox_text_enabled);
			} else {
				buttonView
						.setText(R.string.activity_edit_reminder_checkbox_text_disabled);
			}
			break;
		case R.id.activity_edit_reminder_radio_reminder_time_absolute:
			if (isChecked) {
				lastSelectedRadiobuttonAbsoluteOrAfterNow = (RadioButton) buttonView;
				mReminder.setReminderTimeModeValue(ReminderTimeMode.ABSOLUTE_TIME
						.getValue());
				relativelayoutReminderTimeBeforeEvent.setVisibility(View.GONE);
				relativelayoutReminderTimeAfterNow.setVisibility(View.GONE);
				linearlayoutReminderTimeAbsolute.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radio_reminder_time_after_now:
			if (isChecked) {
				lastSelectedRadiobuttonAbsoluteOrAfterNow = (RadioButton) buttonView;
				mReminder.setReminderTimeModeValue(ReminderTimeMode.AFTER_NOW.getValue());
				linearlayoutReminderTimeAbsolute.setVisibility(View.GONE);
				relativelayoutReminderTimeBeforeEvent.setVisibility(View.GONE);
				relativelayoutReminderTimeAfterNow.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radio_reminder_time_before_event:
		case R.id.activity_edit_reminder_radio_reminder_time_after_event:
			if (isChecked) {
				lastSelectedRadiobuttonBeforeEventOrAfterEvent = (RadioButton) buttonView;
				mReminder.setReminderTimeModeValue(ReminderTimeMode.TIME_BEFORE_EVENT
						.getValue());
				linearlayoutReminderTimeAbsolute.setVisibility(View.GONE);
				relativelayoutReminderTimeAfterNow.setVisibility(View.GONE);
				relativelayoutReminderTimeBeforeEvent.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radio_alarm:
			if (isChecked) {
				isAlarm = true;
				mReminder.setIsAlarm(isAlarm);
				linearlayoutNotificationRingtoneSegment.setVisibility(View.GONE);
				linearlayoutAlarmRingtoneSegment.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radio_notification:
			if (isChecked) {
				isAlarm = false;
				mReminder.setIsAlarm(isAlarm);
				linearlayoutAlarmRingtoneSegment.setVisibility(View.GONE);
				linearlayoutNotificationRingtoneSegment.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_alarm_ringtone_from_task:
			if (isChecked) {
				isRadioButtonAlarmRingtoneCustomChecked = false;
				mReminder.setRingtone(null);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_notification_ringtone_from_task:
			if (isChecked) {
				isRadioButtonNotificationRingtoneCustomChecked = false;
				mReminder.setRingtone(null);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_ringtone_fade_in_time_from_task:
			if (isChecked) {
				mReminder.setRingtoneFadeInTime(null);
				linearlayoutRingtoneFadeInTimeCustomSegment.setVisibility(View.GONE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_ringtone_fade_in_time_custom:
			if (isChecked) {
				mReminder.setRingtoneFadeInTime(edittextRingtoneFadeInTimeChangedListener
						.getLongValue());
				linearlayoutRingtoneFadeInTimeCustomSegment.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_reminders_popup_window_displaying_duration_from_task:
			if (isChecked) {
				mReminder.setPlayingTime(null);
				linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment
						.setVisibility(View.GONE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_reminders_popup_window_displaying_duration_custom:
			if (isChecked) {
				mReminder
						.setPlayingTime(edittextRemindersPopupWindowDisplayingDurationChangedListener
								.getIntegerValue());
				linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_automatic_snooze_duration_from_task:
			if (isChecked) {
				mReminder.setAutomaticSnoozeDuration(null);
				linearlayoutAutomaticSnoozeDurationCustomSegment.setVisibility(View.GONE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_automatic_snooze_duration_custom:
			if (isChecked) {
				mReminder
						.setAutomaticSnoozeDuration(edittextAutomaticSnoozeDurationChangedListener
								.getIntegerValue());
				linearlayoutAutomaticSnoozeDurationCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_automatic_snoozes_max_count_from_task:
			if (isChecked) {
				mReminder.setAutomaticSnoozesMaxCount(null);
				linearlayoutAutomaticSnoozesMaxCountCustomSegment
						.setVisibility(View.GONE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_automatic_snoozes_max_count_custom:
			if (isChecked) {
				mReminder
						.setAutomaticSnoozesMaxCount(edittextAutomaticSnoozesMaxCountChangedListener
								.getIntegerValue());
				linearlayoutAutomaticSnoozesMaxCountCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_vibrate_with_alarm_ringtone_from_task:
			if (isChecked) {
				mReminder.setVibrate(null);
				linearlayoutVibrateWithAlarmRingtoneCustomSegment
						.setVisibility(View.GONE);
			}
			break;
		case R.id.activity_edit_reminder_radiobutton_vibrate_with_alarm_ringtone_custom:
			if (isChecked) {
				mReminder.setVibrate(checkboxVibrateWithAlarmRingtone.isChecked());
				linearlayoutVibrateWithAlarmRingtoneCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_edit_reminder_checkbox_vibrate_with_alarm_ringtone:
			String summaryTextCustom = Helper.getBooleanPreferenceSummary(this,
					isChecked, R.string.vibrate, R.string.do_not_vibrate);
			String textCustom = String.format(radiobuttonTextCustom, summaryTextCustom);
			radiobuttonVibrateWithAlarmRingtoneCustom.setText(textCustom);
			mReminder.setVibrate(isChecked);
			break;
		default:
			break;
		}
	}

	@Override
	public void finish() {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag, "ActivityEditReminder " + "finish() "
					+ System.identityHashCode(this));
		}
		Intent intent = getIntent();
		if (!mActionDonePerformed) {
			// Activity finished by cancel, return no data
			setResult(Activity.RESULT_CANCELED);
			super.finish();
			return;
		}
		// Activity finished ok, return the data
		intent.putExtra(CommonConstants.INTENT_EXTRA_REMINDER, mReminder);
		setResult(Activity.RESULT_OK, intent);
		super.finish();
	}

	@Override
	public void onPause() {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag, "ActivityEditReminder " + "onPause() "
					+ System.identityHashCode(this));
		}
		super.onPause();
		if (!isFinishing()) {
		}
	}

	@Override
	public void onDestroy() {
		if (ActivityEditReminder.Debug) {
			Log.d(ActivityEditReminder.DebugTag, "ActivityEditReminder " + "onDestroy() "
					+ System.identityHashCode(this));
		}
		super.onDestroy();
		if (isFinishing()) {
			LocalBroadcastManager.getInstance(getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
	}

	private boolean isDataCollected() {
		boolean isCollected = true;
		View focusView = null;
		// collect enabled state
		CheckBox checkBox = (CheckBox) findViewById(R.id.activity_edit_reminder_checkbox_reminder_enabled);
		mReminder.setEnabled(checkBox.isChecked());
		String text = edittextTitle.getText().toString().trim();
		mReminder.setText(text);
		if (text == null || text.length() == 0) {
			edittextTitle.setError(getString(R.string.error_field_required));
			if (focusView == null) {
				focusView = edittextTitle;
			}
			isCollected = false;
			wantingItemSet.add(WantingItem.REMINDER_NAME);
		} else {
			wantingItemSet.remove(WantingItem.REMINDER_NAME);
		}
		// back to edit
		int radioButtonId = radiogroupNotificationAlarm.getCheckedRadioButtonId();
		switch (radioButtonId) {
		case R.id.activity_edit_reminder_radio_alarm:
			mReminder.setIsAlarm(true);
			break;
		case R.id.activity_edit_reminder_radio_notification:
			mReminder.setIsAlarm(false);
		}
		radioButtonId = radiogroupReminderTime.getCheckedRadioButtonId();
		switch (radioButtonId) {
		case R.id.activity_edit_reminder_radio_reminder_time_absolute:
			mReminder.setReminderTimeModeValue(ReminderTimeMode.ABSOLUTE_TIME.getValue());
			mReminder.setReminderDateTime(mLastSelectedAbsoluteTime);
			break;
		case R.id.activity_edit_reminder_radio_reminder_time_after_now:
			mReminder.setReminderTimeModeValue(ReminderTimeMode.AFTER_NOW.getValue());
			mReminder.setReminderDateTime(mLastSelectedTimeSpanMillisAfterNow);
			break;
		case R.id.activity_edit_reminder_radio_reminder_time_before_event:
			mReminder.setReminderTimeModeValue(ReminderTimeMode.TIME_BEFORE_EVENT
					.getValue());
			mReminder.setReminderDateTime(mLastSelectedTimeSpanMillisBeforeEvent);
			break;
		case R.id.activity_edit_reminder_radio_reminder_time_after_event:
			mReminder.setReminderTimeModeValue(ReminderTimeMode.TIME_AFTER_EVENT
					.getValue());
			mReminder.setReminderDateTime(mLastSelectedTimeSpanMillisBeforeEvent);
			break;
		}
		if (!wantingItemSet.isEmpty()) {
			isCollected = false;
			if (focusView != null) {
				focusView.requestFocus();
			}
			Helper.showWantingItemSetMessages(this, wantingItemSet, null);
		} else {
			if (edittextRingtoneFadeInTimeChangedListener.getRadioButtonCustom()
					.isChecked()) {
				mReminder.setRingtoneFadeInTime(edittextRingtoneFadeInTimeChangedListener
						.getLongValue());
			}
			if (edittextRemindersPopupWindowDisplayingDurationChangedListener
					.getRadioButtonCustom().isChecked()) {
				mReminder
						.setPlayingTime(edittextRemindersPopupWindowDisplayingDurationChangedListener
								.getIntegerValue());
			}
			if (edittextAutomaticSnoozeDurationChangedListener.getRadioButtonCustom()
					.isChecked()) {
				mReminder
						.setAutomaticSnoozeDuration(edittextAutomaticSnoozeDurationChangedListener
								.getIntegerValue());
			}
			if (edittextAutomaticSnoozesMaxCountChangedListener.getRadioButtonCustom()
					.isChecked()) {
				mReminder
						.setAutomaticSnoozesMaxCount(edittextAutomaticSnoozesMaxCountChangedListener
								.getIntegerValue());
			}
		}
		return isCollected;
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		int callerId = bundle.getInt(CommonConstants.CALLER_ID);
		if (callerId != 0) {
			if (callerId == R.id.button_reminder_date) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
				calendar.set(year, monthOfYear, dayOfMonth);
				mParcelableDataStore.lastSelectedAbsoluteTime = mLastSelectedAbsoluteTime = calendar
						.getTimeInMillis();
				DateFormat mDateFormat = DateFormat.getDateInstance();
				mDateFormat.setCalendar(calendar);
				String dateString = mDateFormat
						.format(new Date(mLastSelectedAbsoluteTime));
				Button button = (Button) findViewById(R.id.button_reminder_date);
				button.setError(null);
				button.setText(dateString);
			}
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		int callerId = bundle.getInt(CommonConstants.CALLER_ID);
		if (callerId != 0) {
			if (callerId == R.id.button_reminder_time) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				mParcelableDataStore.lastSelectedAbsoluteTime = mLastSelectedAbsoluteTime = calendar
						.getTimeInMillis();
				DateFormat mDateFormat = DateFormat.getTimeInstance();
				mDateFormat.setCalendar(calendar);
				String timeString = mDateFormat
						.format(new Date(mLastSelectedAbsoluteTime));
				Button button = (Button) findViewById(R.id.button_reminder_time);
				button.setError(null);
				button.setText(timeString);
			} else if (callerId == R.id.button_reminder_time_after_now) {
				mLastSelectedTimeSpanMillisAfterNow = (hourOfDay * 60L + minute) * 60 * 1000;
				mParcelableDataStore.lastSelectedTimeSpanMillisAfterNow = mLastSelectedTimeSpanMillisAfterNow;
				setTimeSpanButtonText(callerId, hourOfDay, minute);
			} else if (callerId == R.id.button_reminder_time_before_event) {
				mLastSelectedTimeSpanMillisBeforeEvent = (hourOfDay * 60L + minute) * 60 * 1000;
				mParcelableDataStore.lastSelectedTimeSpanMillisBeforeEvent = mLastSelectedTimeSpanMillisBeforeEvent;
				setTimeSpanButtonText(callerId, hourOfDay, minute);
			}
		}
	}

	private void setTimeSpanButtonText(int buttonId, int hourOfDay, int minute) {
		Button button = (Button) findViewById(buttonId);
		button.setError(null);
		if (hourOfDay == 0 && minute == 0) {
			int textAtTaskStartId;
			if (buttonId == R.id.button_reminder_time_after_now) {
				textAtTaskStartId = R.string.activity_edit_reminder_button_reminder_time_after_now_text_now;
			} else {
				textAtTaskStartId = R.string.activity_edit_reminder_button_reminder_time_before_event_text_at_task_start;
			}
			String textAtTaskStart = getResources().getString(textAtTaskStartId);
			button.setText("" + textAtTaskStart);
		} else {
			button.setText((hourOfDay == 0 ? ""
					: hourOfDay
							+ " "
							+ getResources()
									.getString(
											R.string.activity_edit_reminder_header_textview_reminder_time_before_event_hours))
					+ (minute == 0 ? ""
							: (hourOfDay == 0 ? "" : " ")
									+ minute
									+ " "
									+ getResources()
											.getString(
													R.string.activity_edit_reminder_header_textview_reminder_time_before_event_minutes)));
		}
	}

	public static class ActivityEditReminderParcelableDataStore implements
			Parcelable {
		private TaskWithDependentsUiData taskWithDependentsUiData;
		private UserInterfaceData userInterfaceData;
		private long lastSelectedAbsoluteTime;
		private long lastSelectedTimeSpanMillisAfterNow;
		private long lastSelectedTimeSpanMillisBeforeEvent;
		private Bundle bundle;
		private ReminderUiData reminderUiData;
		private boolean is24HourFormat;
		private int lastSelectedRadiobuttonIdAbsoluteOrAfterNow;
		private int lastSelectedRadiobuttonIdBeforeEventOrAfterEvent;

		public ActivityEditReminderParcelableDataStore() {
		}

		public ActivityEditReminderParcelableDataStore(
				TaskWithDependentsUiData taskWithDependentsUiData,
				UserInterfaceData userInterfaceData, long lastSelectedAbsoluteTime,
				long lastSelectedTimeSpanMillis, Bundle bundle,
				ReminderUiData reminderUiData, boolean is24HourFormat) {
			this.taskWithDependentsUiData = taskWithDependentsUiData;
			this.userInterfaceData = userInterfaceData;
			this.lastSelectedAbsoluteTime = lastSelectedAbsoluteTime;
			lastSelectedTimeSpanMillisAfterNow = lastSelectedTimeSpanMillis;
			lastSelectedTimeSpanMillisBeforeEvent = lastSelectedTimeSpanMillis;
			this.bundle = bundle;
			this.reminderUiData = reminderUiData;
			this.is24HourFormat = is24HourFormat;
			lastSelectedRadiobuttonIdAbsoluteOrAfterNow = R.id.activity_edit_reminder_radio_reminder_time_absolute;
			lastSelectedRadiobuttonIdBeforeEventOrAfterEvent = R.id.activity_edit_reminder_radio_reminder_time_before_event;
		}

//		@Override
		public void onCreate(Bundle savedInstanceState) {
//			super.onCreate(savedInstanceState);
//			setRetainInstance(true);
			//
			if (savedInstanceState != null) {
				taskWithDependentsUiData = savedInstanceState
						.getParcelable("taskWithDependentsUiData");
				userInterfaceData = savedInstanceState.getParcelable("userInterfaceData");
				lastSelectedAbsoluteTime = savedInstanceState
						.getLong("lastSelectedAbsoluteTime");
				lastSelectedTimeSpanMillisAfterNow = savedInstanceState
						.getLong("lastSelectedTimeSpanMillisAfterNow");
				lastSelectedTimeSpanMillisBeforeEvent = savedInstanceState
						.getLong("lastSelectedTimeSpanMillisBeforeEvent");
				bundle = savedInstanceState.getParcelable("bundle");
				reminderUiData = savedInstanceState.getParcelable("reminderUiData");
				is24HourFormat = savedInstanceState.getBoolean("is24HourFormat");
				lastSelectedRadiobuttonIdAbsoluteOrAfterNow = savedInstanceState
						.getInt("lastSelectedRadiobuttonIdAbsoluteOrAfterNow");
				lastSelectedRadiobuttonIdBeforeEventOrAfterEvent = savedInstanceState
						.getInt("lastSelectedRadiobuttonIdBeforeEventOrAfterEvent");
			}
			//
		}

//		@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
//			super.onSaveInstanceState(savedInstanceState);
			savedInstanceState.putParcelable("taskWithDependentsUiData",
					taskWithDependentsUiData);
			savedInstanceState.putParcelable("userInterfaceData", userInterfaceData);
			savedInstanceState.putLong("lastSelectedAbsoluteTime",
					lastSelectedAbsoluteTime);
			savedInstanceState.putLong("lastSelectedTimeSpanMillisAfterNow",
					lastSelectedTimeSpanMillisAfterNow);
			savedInstanceState.putLong("lastSelectedTimeSpanMillisBeforeEvent",
					lastSelectedTimeSpanMillisBeforeEvent);
			savedInstanceState.putParcelable("bundle", bundle);
			savedInstanceState.putParcelable("reminderUiData", reminderUiData);
			savedInstanceState.putBoolean("is24HourFormat", is24HourFormat);
			savedInstanceState.putInt("lastSelectedRadiobuttonIdAbsoluteOrAfterNow",
					lastSelectedRadiobuttonIdAbsoluteOrAfterNow);
			savedInstanceState.putInt("lastSelectedRadiobuttonIdBeforeEventOrAfterEvent",
					lastSelectedRadiobuttonIdBeforeEventOrAfterEvent);
		}

		protected ActivityEditReminderParcelableDataStore(Parcel in) {
			taskWithDependentsUiData = (TaskWithDependentsUiData) in
					.readValue(TaskWithDependentsUiData.class.getClassLoader());
			userInterfaceData = (UserInterfaceData) in.readValue(UserInterfaceData.class
					.getClassLoader());
			lastSelectedAbsoluteTime = in.readLong();
			lastSelectedTimeSpanMillisAfterNow = in.readLong();
			lastSelectedTimeSpanMillisBeforeEvent = in.readLong();
			bundle = in.readBundle();
			reminderUiData = (ReminderUiData) in.readValue(ReminderUiData.class
					.getClassLoader());
			is24HourFormat = in.readByte() != 0x00;
			lastSelectedRadiobuttonIdAbsoluteOrAfterNow = in.readInt();
			lastSelectedRadiobuttonIdBeforeEventOrAfterEvent = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeValue(taskWithDependentsUiData);
			dest.writeValue(userInterfaceData);
			dest.writeLong(lastSelectedAbsoluteTime);
			dest.writeLong(lastSelectedTimeSpanMillisAfterNow);
			dest.writeLong(lastSelectedTimeSpanMillisBeforeEvent);
			dest.writeBundle(bundle);
			dest.writeValue(reminderUiData);
			dest.writeByte((byte) (is24HourFormat ? 0x01 : 0x00));
			dest.writeInt(lastSelectedRadiobuttonIdAbsoluteOrAfterNow);
			dest.writeInt(lastSelectedRadiobuttonIdBeforeEventOrAfterEvent);
		}

		public static final Parcelable.Creator<ActivityEditReminderParcelableDataStore> CREATOR = new Parcelable.Creator<ActivityEditReminderParcelableDataStore>() {
			@Override
			public ActivityEditReminderParcelableDataStore createFromParcel(Parcel in) {
				return new ActivityEditReminderParcelableDataStore(in);
			}

			@Override
			public ActivityEditReminderParcelableDataStore[] newArray(int size) {
				return new ActivityEditReminderParcelableDataStore[size];
			}
		};
	}
}
