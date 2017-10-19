package biz.advancedcalendar.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.services.NotificationService;
import biz.advancedcalendar.utils.Helper;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialog;
import com.android.supportdatetimepicker.time.TimePickerDialog.OnTimeSetListener;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/** Activity which displays a quick reminder edit screen to the user. */
public class ActivityQuickReminder extends AppCompatActivity implements OnClickListener,
		OnCheckedChangeListener, OnDateSetListener, OnTimeSetListener {
	private boolean mActionDonePerformed = false;
	private LocalBroadcastManager mBroadcaster;
	private String mDatePickerDialogKey = "biz.advancedcalendar.activities.ActivityQuickReminder.DatePickerDialogKey";
	private String mTimePickerDialogKey = "biz.advancedcalendar.activities.ActivityQuickReminder.TimePickerDialogKey";
	private String mPendingBundleForDateOrTimeOrTimeSpanPickerDialogKey = "biz.advancedcalendar.activities.ActivityQuickReminder.PendingBundleForDateOrTimeOrTimeSpanPickerDialogKey";
	private String mLastSelectedAbsoluteTimeKey = "biz.advancedcalendar.activities.ActivityQuickReminder.LastSelectedAbsoluteTimeKey";
	private String mLastSelectedTimeSpanKey = "biz.advancedcalendar.activities.ActivityQuickReminder.LastSelectedTimeSpanKey";
	// variables to be saved in onSaveInstanceState()
	private long mLastSelectedAbsoluteTime;
	private long mLastSelectedTimeSpanMillis;
	private Bundle mPendingBundleForDateOrTimeOrTimeSpanPickerDialog;
	private ScheduledReminder mReminder;
	private boolean is24HourFormat;

	public static class ActivityQuickReminderRetainedData {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quick_reminder);
		is24HourFormat = android.text.format.DateFormat
				.is24HourFormat(getApplicationContext());
		if (savedInstanceState == null) {
			if (mReminder == null) {
				mReminder = (ScheduledReminder) getIntent().getParcelableExtra(
						CommonConstants.INTENT_EXTRA_REMINDER);
				long now = System.currentTimeMillis();
				mLastSelectedAbsoluteTime = mReminder.getAssignedRemindAtDateTime();
				// if (mLastSelectedAbsoluteTime <= now) {
				// mLastSelectedAbsoluteTime = now
				// + 1000L
				// * 60
				// * Helper.getIntegerFromStringPreferenceValue(
				// this,
				// R.string.preference_key_automatic_snooze_duration,
				// getResources()
				// .getInteger(
				// R.integer.automatic_snooze_duration_default_value),
				// getResources()
				// .getInteger(
				// R.integer.automatic_snooze_duration_min_value),
				// getResources()
				// .getInteger(
				// R.integer.automatic_snooze_duration_max_value));
				// }
				mLastSelectedTimeSpanMillis = mLastSelectedAbsoluteTime - now;
				if (mLastSelectedTimeSpanMillis < 0) {
					mLastSelectedTimeSpanMillis = 0;
				}
				// if (mLastSelectedTimeSpanMillis <= 0) {
				// mLastSelectedTimeSpanMillis = 1000L * 60 * Helper
				// .getIntegerFromStringPreferenceValue(
				// this,
				// R.string.preference_key_automatic_snooze_duration,
				// getResources()
				// .getInteger(
				// R.integer.automatic_snooze_duration_default_value),
				// getResources()
				// .getInteger(
				// R.integer.automatic_snooze_duration_min_value),
				// getResources()
				// .getInteger(
				// R.integer.automatic_snooze_duration_max_value));
				// }
			}
		} else {
			TimePickerDialog tpd = (TimePickerDialog) getSupportFragmentManager()
					.findFragmentByTag(mTimePickerDialogKey);
			if (tpd != null) {
				tpd.setOnTimeSetListener(this);
			}
			DatePickerDialog dpd = (DatePickerDialog) getSupportFragmentManager()
					.findFragmentByTag(mDatePickerDialogKey);
			if (dpd != null) {
				dpd.setOnDateSetListener(this);
			}
			mLastSelectedAbsoluteTime = savedInstanceState
					.getLong(mLastSelectedAbsoluteTimeKey);
			mLastSelectedTimeSpanMillis = savedInstanceState
					.getLong(mLastSelectedTimeSpanKey);
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog = (Bundle) savedInstanceState
					.getParcelable(mPendingBundleForDateOrTimeOrTimeSpanPickerDialogKey);
			mReminder = (ScheduledReminder) savedInstanceState
					.getParcelable(CommonConstants.INTENT_EXTRA_REMINDER);
		}
		mBroadcaster = LocalBroadcastManager.getInstance(this);
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(
				R.string.title_activity_quick_reminder));
		// setup listeners and set values on views
		// setup reminder enabled checkbox
		// CheckBox checkBox = (CheckBox)
		// findViewById(R.id.activity_quick_reminder_checkbox_reminder_enabled);
		// checkBox.setText(mReminder.getEnabled() ?
		// R.string.fragment_view_task_part_reminders_list_item_checkbox_reminder_enabled
		// :
		// R.string.fragment_view_task_part_reminders_list_item_checkbox_reminder_disabled);
		// checkBox.setChecked(mReminder.getEnabled());
		// checkBox.setOnCheckedChangeListener(this);
		// setup reminder text
		EditText editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminder_description);
		editText.setText("");
		editText.append(mReminder.getText() == null ? "" : mReminder.getText());
		//
		RadioButton rbAlarm = (RadioButton) findViewById(R.id.activity_quick_reminder_radio_alarm);
		RadioButton rbNotification = (RadioButton) findViewById(R.id.activity_quick_reminder_radio_notification);
		if (mReminder.getIsAlarm()) {
			rbAlarm.setChecked(true);
		} else {
			rbNotification.setChecked(true);
		}
		rbAlarm.setOnCheckedChangeListener(this);
		rbNotification.setOnCheckedChangeListener(this);
		//
		// setup radio reminder time type
		RadioButton radioButton1 = (RadioButton) findViewById(R.id.activity_quick_reminder_radio_reminder_time_absolute);
		RadioButton radioButton2 = (RadioButton) findViewById(R.id.activity_quick_reminder_radio_reminder_time_after_now);
		// LinearLayout linearLayout = (LinearLayout)
		// findViewById(R.id.activity_quick_reminder_linearlayout_reminder_time_absolute);
		// RelativeLayout relativeLayout = (RelativeLayout)
		// findViewById(R.id.activity_quick_reminder_linearlayout_reminder_time_before_event);
		radioButton1.setOnCheckedChangeListener(this);
		radioButton2.setOnCheckedChangeListener(this);
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
		Button button = (Button) findViewById(R.id.activity_quick_reminder_button_reminder_time_before_event_hours_minutes);
		button.setOnClickListener(this);
		setTimeSpanButtonText((int) (mLastSelectedTimeSpanMillis / 1000 / 60 / 60 % 24),
				(int) (mLastSelectedTimeSpanMillis / 1000 / 60 % 60));
		// setup advanced section
		CheckBox checkBox = (CheckBox) findViewById(R.id.activity_quick_reminder_checkbox_advanced_settings);
		checkBox.setOnCheckedChangeListener(null);
		boolean collapsed = Helper
				.getBooleanPreferenceValue(
						this,
						R.string.preference_key_activity_edit_reminder_advanced_settings_expand_state,
						getResources()
								.getBoolean(
										R.bool.activity_edit_reminder_advanced_settings_expand_state));
		checkBox.setChecked(collapsed);
		setupAdvancedSettingsSection(checkBox);
		checkBox.setOnCheckedChangeListener(this);
		RadioButton radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_alarm_ringtone_custom);
		radioButtonCustom.setOnClickListener(this);
		//
		RadioButton radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_custom);
		radioButtonDefault.setText(getResources().getString(
				R.string.activity_quick_reminder_radiobutton_text_from_settings)
				+ " ("
				+ Helper.getIntegerFromStringPreferenceValue(
						this,
						R.string.preference_key_automatic_snoozes_max_count,
						null,
						getResources().getInteger(
								R.integer.automatic_snoozes_max_count_default_value),
						getResources().getInteger(
								R.integer.automatic_snoozes_max_count_min_value), getResources().getInteger(
								R.integer.automatic_snoozes_max_count_max_value)) + ")");
		radioButtonCustom.setText(getResources().getString(R.string.custom));
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snoozes_max_count);
		editText.setTag(mReminder);
		EditTextAutomaticSnoozesMaxCountChangedListener editTextAutomaticSnoozesMaxCountChangedListener = new EditTextAutomaticSnoozesMaxCountChangedListener();
		editTextAutomaticSnoozesMaxCountChangedListener.editText = editText;
		editText.addTextChangedListener(editTextAutomaticSnoozesMaxCountChangedListener);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_custom);
		radioButtonDefault.setText(getResources().getString(
				R.string.activity_quick_reminder_radiobutton_text_from_settings)
				+ " ("
				+ Helper.getIntegerFromStringPreferenceValue(
						this,
						R.string.preference_key_automatic_snooze_duration,
						null,
						getResources().getInteger(
								R.integer.automatic_snooze_duration_default_value),
						getResources().getInteger(
								R.integer.automatic_snooze_duration_min_value), getResources().getInteger(
								R.integer.automatic_snooze_duration_max_value)) + ")");
		radioButtonCustom.setText(getResources().getString(R.string.custom));
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snooze_duration);
		editText.setTag(mReminder);
		EditTextAutomaticSnoozeDurationChangedListener editTextAutomaticSnoozeDurationChangedListener = new EditTextAutomaticSnoozeDurationChangedListener();
		editTextAutomaticSnoozeDurationChangedListener.editText = editText;
		editText.addTextChangedListener(editTextAutomaticSnoozeDurationChangedListener);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_custom);
		radioButtonDefault
				.setText(getResources().getString(
						R.string.activity_quick_reminder_radiobutton_text_from_settings)
						+ " ("
						+ Helper.getIntegerFromStringPreferenceValue(
								this,
								R.string.preference_key_reminders_popup_window_displaying_duration,
								null,
								getResources()
										.getInteger(
												R.integer.reminders_popup_window_displaying_duration_default_value),
								getResources()
										.getInteger(
												R.integer.reminders_popup_window_displaying_duration_min_value), getResources()
										.getInteger(
												R.integer.reminders_popup_window_displaying_duration_max_value))
						+ ")");
		radioButtonCustom.setText(getResources().getString(R.string.custom));
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminders_popup_window_displaying_duration);
		editText.setTag(mReminder);
		EditTextPlayingTimeChangedListener editTextPlayingTimeChangedListener = new EditTextPlayingTimeChangedListener();
		editTextPlayingTimeChangedListener.editText = editText;
		editText.addTextChangedListener(editTextPlayingTimeChangedListener);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_vibrate_with_alarm_ringtone_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_vibrate_with_alarm_ringtone_custom);
		radioButtonDefault
				.setText(getResources().getString(
						R.string.activity_quick_reminder_radiobutton_text_from_settings)
						+ " ("
						+ (Helper
								.getBooleanPreferenceValue(
										this,
										R.string.preference_key_vibrate_with_alarm_ringtone,
										getResources()
												.getBoolean(
														R.bool.vibrate_with_alarm_ringtone_default_value)) ? getResources()
								.getString(R.string.vibrate) : getResources().getString(
								R.string.do_not_vibrate)) + ")");
		radioButtonCustom.setText(getResources().getString(R.string.custom));
		//
		setupAdvancedSettingsSection();
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLong(mLastSelectedAbsoluteTimeKey,
				mLastSelectedAbsoluteTime);
		savedInstanceState.putLong(mLastSelectedTimeSpanKey, mLastSelectedTimeSpanMillis);
		savedInstanceState.putParcelable(
				mPendingBundleForDateOrTimeOrTimeSpanPickerDialogKey,
				mPendingBundleForDateOrTimeOrTimeSpanPickerDialog);
		savedInstanceState
				.putParcelable(CommonConstants.INTENT_EXTRA_REMINDER, mReminder);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_cancel_done, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_cancel:
			finish();
			return true;
		case R.id.action_done:
			if (collectData()) {
				mActionDonePerformed = true;
				mBroadcaster.sendBroadcast(new Intent(
						CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
				finish();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Check which request it is we're responding to
		switch (requestCode) {
		case CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE:
		case CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE:
			if (resultCode == android.app.Activity.RESULT_OK) {
				Uri uri = intent
						.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
				// RadioButton radioButtonCustom = (RadioButton)
				// findViewById(R.id.activity_quick_reminder_radiobutton_alarm_ringtone_custom);
				if (uri != null) {
					// uri contains the URI of the selected ringtone
					mReminder.setRingtone(uri.toString());
				} else {
					// user picked "silent" as the ringtone
					mReminder.setRingtone("");
				}
			} else {
				mReminder.setRingtone(null);
			}
			setupAdvancedSettingsSection();
			break;
		default:
			break;
		}
	}

	private void setupAdvancedSettingsSection() {
		RadioButton radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_alarm_ringtone_from_settings);
		RadioButton radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_alarm_ringtone_custom);
		radioButtonDefault.setOnCheckedChangeListener(null);
		// setup radioButtonDefault
		if (mReminder.getIsAlarm()) {
			radioButtonDefault.setText(getResources().getString(
					R.string.activity_quick_reminder_radiobutton_text_from_settings)
					+ " ("
					+ Helper.getRingtoneTitle(this, Helper.getStringPreferenceValue(
							this,
							getResources().getString(
									R.string.preference_key_alarm_ringtone),
							RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
									.toString())) + ")");
		} else {
			radioButtonDefault
					.setText(getResources()
							.getString(
									R.string.activity_quick_reminder_radiobutton_text_from_settings)
							+ " ("
							+ Helper.getRingtoneTitle(
									this,
									Helper.getStringPreferenceValue(
											this,
											getResources()
													.getString(
															R.string.preference_key_notification_ringtone),
											RingtoneManager.getDefaultUri(
													RingtoneManager.TYPE_NOTIFICATION)
													.toString())) + ")");
		}
		// setup radioButtonCustom
		if (mReminder.getRingtone() == null) {
			radioButtonCustom.setText(getResources().getString(
					R.string.activity_quick_reminder_radiobutton_text_custom));
			radioButtonDefault.setChecked(true);
		} else {
			radioButtonCustom.setText(getResources().getString(
					R.string.activity_quick_reminder_radiobutton_text_custom)
					+ " ("
					+ mReminder.getRingtoneTitle(this,
							((Global) getApplicationContext()).getDaoSession()) + ")");
			radioButtonCustom.setChecked(true);
		}
		radioButtonDefault.setOnCheckedChangeListener(this);
		// radioButtonCustom.setOnClickListener(this);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_custom);
		radioButtonDefault.setOnCheckedChangeListener(null);
		radioButtonCustom.setOnCheckedChangeListener(null);
		EditText editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snoozes_max_count);
		if (mReminder.getAutomaticSnoozesMaxCount() != null) {
			radioButtonCustom.setChecked(true);
			editText.setVisibility(View.VISIBLE);
			editText.setText(mReminder.getAutomaticSnoozesMaxCount().toString());
		} else {
			radioButtonDefault.setChecked(true);
			editText.setVisibility(View.GONE);
		}
		radioButtonDefault.setOnCheckedChangeListener(this);
		radioButtonCustom.setOnCheckedChangeListener(this);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_custom);
		radioButtonDefault.setOnCheckedChangeListener(null);
		radioButtonCustom.setOnCheckedChangeListener(null);
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snooze_duration);
		if (mReminder.getAutomaticSnoozeDuration() != null) {
			radioButtonCustom.setChecked(true);
			editText.setVisibility(View.VISIBLE);
			editText.setText(mReminder.getAutomaticSnoozeDuration().toString());
		} else {
			radioButtonDefault.setChecked(true);
			editText.setVisibility(View.GONE);
		}
		radioButtonDefault.setOnCheckedChangeListener(this);
		radioButtonCustom.setOnCheckedChangeListener(this);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_custom);
		radioButtonDefault.setOnCheckedChangeListener(null);
		radioButtonCustom.setOnCheckedChangeListener(null);
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminders_popup_window_displaying_duration);
		if (mReminder.getPlayingTime() != null) {
			radioButtonCustom.setChecked(true);
			editText.setVisibility(View.VISIBLE);
			editText.setText(mReminder.getPlayingTime().toString());
		} else {
			radioButtonDefault.setChecked(true);
			editText.setVisibility(View.GONE);
		}
		radioButtonDefault.setOnCheckedChangeListener(this);
		radioButtonCustom.setOnCheckedChangeListener(this);
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_vibrate_with_alarm_ringtone_from_settings);
		radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_vibrate_with_alarm_ringtone_custom);
		CheckBox checkBox = (CheckBox) findViewById(R.id.activity_quick_reminder_checkbox_vibrate_with_alarm_ringtone);
		radioButtonDefault.setOnCheckedChangeListener(null);
		radioButtonCustom.setOnCheckedChangeListener(null);
		checkBox.setOnCheckedChangeListener(null);
		if (mReminder.getVibrate() != null) {
			radioButtonCustom.setChecked(true);
			checkBox.setVisibility(View.VISIBLE);
			checkBox.setChecked(mReminder.getVibrate());
		} else {
			radioButtonDefault.setChecked(true);
			checkBox.setVisibility(View.GONE);
		}
		radioButtonDefault.setOnCheckedChangeListener(this);
		radioButtonCustom.setOnCheckedChangeListener(this);
		checkBox.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_quick_reminder_radiobutton_alarm_ringtone_custom:
			Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			// intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
			if (mReminder.getIsAlarm()) {
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
						RingtoneManager.TYPE_ALARM);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
						android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
				startActivityForResult(intent,
						CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE);
			} else {
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
						RingtoneManager.TYPE_NOTIFICATION);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
						android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
				startActivityForResult(intent,
						CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE);
			}
			break;
		case R.id.button_reminder_date:
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog = new Bundle();
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog.putInt(
					CommonConstants.CALLER_ID, R.id.button_reminder_date);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
			DatePickerDialog dpd = DatePickerDialog.newInstance(this,
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));
			dpd.show(getSupportFragmentManager(), mDatePickerDialogKey);
			break;
		case R.id.button_reminder_time:
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog = new Bundle();
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog.putInt(
					CommonConstants.CALLER_ID, R.id.button_reminder_time);
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
			TimePickerDialog tpd = TimePickerDialog.newInstance(this,
					calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),
					is24HourFormat);
			tpd.show(getSupportFragmentManager(), mDatePickerDialogKey);
			break;
		case R.id.activity_quick_reminder_button_reminder_time_before_event_hours_minutes:
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog = new Bundle();
			mPendingBundleForDateOrTimeOrTimeSpanPickerDialog
					.putInt(CommonConstants.CALLER_ID,
							R.id.activity_quick_reminder_button_reminder_time_before_event_hours_minutes);
			tpd = TimePickerDialog.newInstance(this,
					(int) (mLastSelectedTimeSpanMillis / 1000 / 60 / 60 % 24),
					(int) (mLastSelectedTimeSpanMillis / 1000 / 60 % 60), true);
			tpd.show(getSupportFragmentManager(), mDatePickerDialogKey);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		LinearLayout linearLayout;
		RelativeLayout relativeLayout;
		switch (buttonView.getId()) {
		case R.id.activity_quick_reminder_checkbox_advanced_settings:
			setupAdvancedSettingsSection((CheckBox) buttonView);
			break;
		case R.id.activity_quick_reminder_radio_reminder_time_absolute:
			if (isChecked) {
				linearLayout = (LinearLayout) findViewById(R.id.activity_quick_reminder_linearlayout_reminder_time_absolute);
				relativeLayout = (RelativeLayout) findViewById(R.id.activity_quick_reminder_linearlayout_reminder_time_before_event);
				linearLayout.setVisibility(View.VISIBLE);
				relativeLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.activity_quick_reminder_radio_reminder_time_after_now:
			if (isChecked) {
				linearLayout = (LinearLayout) findViewById(R.id.activity_quick_reminder_linearlayout_reminder_time_absolute);
				relativeLayout = (RelativeLayout) findViewById(R.id.activity_quick_reminder_linearlayout_reminder_time_before_event);
				linearLayout.setVisibility(View.GONE);
				relativeLayout.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.activity_quick_reminder_radio_alarm:
			if (isChecked) {
				mReminder.setIsAlarm(true);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radio_notification:
			if (isChecked) {
				mReminder.setIsAlarm(false);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_alarm_ringtone_from_settings:
			if (isChecked) {
				mReminder.setRingtone(null);
				RadioButton radioButtonCustom = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_alarm_ringtone_custom);
				radioButtonCustom.setText(getResources().getString(
						R.string.activity_quick_reminder_radiobutton_text_custom));
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_from_settings:
			EditText editText;
			if (isChecked) {
				editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snoozes_max_count);
				mReminder.setAutomaticSnoozesMaxCount(null);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_custom:
			if (isChecked) {
				editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snoozes_max_count);
				collectAndCorrectValueOnEditText(editText);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_from_settings:
			if (isChecked) {
				mReminder.setAutomaticSnoozeDuration(null);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_custom:
			if (isChecked) {
				editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snooze_duration);
				collectAndCorrectValueOnEditText(editText);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_from_settings:
			if (isChecked) {
				editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminders_popup_window_displaying_duration);
				mReminder.setPlayingTime(null);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_custom:
			if (isChecked) {
				editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminders_popup_window_displaying_duration);
				collectAndCorrectValueOnEditText(editText);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_vibrate_with_alarm_ringtone_from_settings:
			if (isChecked) {
				mReminder.setVibrate(null);
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_radiobutton_vibrate_with_alarm_ringtone_custom:
			if (isChecked) {
				CheckBox checkBox = (CheckBox) findViewById(R.id.activity_quick_reminder_checkbox_vibrate_with_alarm_ringtone);
				mReminder.setVibrate(checkBox.isChecked());
				setupAdvancedSettingsSection();
			}
			break;
		case R.id.activity_quick_reminder_checkbox_vibrate_with_alarm_ringtone:
			mReminder.setVibrate(isChecked);
			setupAdvancedSettingsSection();
			break;
		default:
			break;
		}
	}

	private void setupAdvancedSettingsSection(CheckBox checkBox) {
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_quick_reminder_linearlayout_advanced_settings);
		if (checkBox.isChecked()) {
			PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putBoolean(
							getResources()
									.getString(
											R.string.preference_key_activity_edit_reminder_advanced_settings_expand_state),
							true).commit();
			checkBox.setText(getResources().getString(R.string.action_less));
			linearLayout.setVisibility(View.VISIBLE);
		} else {
			PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putBoolean(
							getResources()
									.getString(
											R.string.preference_key_activity_edit_reminder_advanced_settings_expand_state),
							false).commit();
			checkBox.setText(getResources().getString(R.string.action_more));
			linearLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void finish() {
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

	private boolean collectData() {
		boolean cancel = false;
		View focusView = null;
		// collect reminder text
		EditText editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminder_description);
		String text = editText.getText().toString().trim();
		mReminder.setText(text);
		if (text == null || text.length() == 0) {
			editText.setError(getString(R.string.error_field_required));
			if (focusView == null) {
				focusView = editText;
			}
			cancel = true;
		}
		// back to edit
		if (cancel) {
			focusView.requestFocus();
			return false;
		}
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.activity_quick_reminder_radiogroup_notification_alarm);
		int radioButtonId = radioGroup.getCheckedRadioButtonId();
		switch (radioButtonId) {
		case R.id.activity_quick_reminder_radio_alarm:
			mReminder.setIsAlarm(true);
			break;
		case R.id.activity_quick_reminder_radio_notification:
			mReminder.setIsAlarm(false);
		}
		// collect reminder time type
		radioGroup = (RadioGroup) findViewById(R.id.activity_quick_reminder_radiogroup_reminder_time);
		radioButtonId = radioGroup.getCheckedRadioButtonId();
		switch (radioButtonId) {
		case R.id.activity_quick_reminder_radio_reminder_time_after_now:
			mReminder.setAssignedRemindAtDateTime(System.currentTimeMillis()
					+ mLastSelectedTimeSpanMillis);
			mReminder.setNextSnoozeDateTime(mReminder.getAssignedRemindAtDateTime());
			break;
		case R.id.activity_quick_reminder_radio_reminder_time_absolute:
			mReminder.setAssignedRemindAtDateTime(mLastSelectedAbsoluteTime);
			mReminder.setNextSnoozeDateTime(mReminder.getAssignedRemindAtDateTime());
			break;
		}
		RadioButton radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snoozes_max_count_from_settings);
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snoozes_max_count);
		if (radioButtonDefault.isChecked()) {
			mReminder.setAutomaticSnoozesMaxCount(null);
		} else {
			int automaticSnoozesMaxCount = Helper.getIntegerFromStringPreferenceValue(
					this,
					R.string.preference_key_automatic_snoozes_max_count,
					null,
					getResources().getInteger(
							R.integer.automatic_snoozes_max_count_default_value),
					getResources().getInteger(
							R.integer.automatic_snoozes_max_count_min_value), getResources().getInteger(
							R.integer.automatic_snoozes_max_count_max_value));
			try {
				text = editText.getText().toString().trim();
				if (text.length() == 0) {
					mReminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						mReminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
					} else {
						mReminder.setAutomaticSnoozesMaxCount(count);
					}
				}
			} catch (NumberFormatException e) {
				mReminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
			}
		}
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_automatic_snooze_duration_from_settings);
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_automatic_snooze_duration);
		if (radioButtonDefault.isChecked()) {
			mReminder.setAutomaticSnoozeDuration(null);
		} else {
			int automaticSnoozeDuration = Helper.getIntegerFromStringPreferenceValue(
					this,
					R.string.preference_key_automatic_snooze_duration,
					null,
					getResources().getInteger(
							R.integer.automatic_snooze_duration_default_value),
					getResources().getInteger(
							R.integer.automatic_snooze_duration_min_value), getResources().getInteger(
							R.integer.automatic_snooze_duration_max_value));
			try {
				text = editText.getText().toString().trim();
				if (text.length() == 0) {
					mReminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						mReminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
					} else {
						mReminder.setAutomaticSnoozeDuration(count);
					}
				}
			} catch (NumberFormatException e) {
				mReminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
			}
		}
		//
		radioButtonDefault = (RadioButton) findViewById(R.id.activity_quick_reminder_radiobutton_reminders_popup_window_displaying_duration_from_settings);
		editText = (EditText) findViewById(R.id.activity_quick_reminder_edittext_reminders_popup_window_displaying_duration);
		if (radioButtonDefault.isChecked()) {
			mReminder.setPlayingTime(null);
		} else {
			int playing_time = Helper
					.getIntegerFromStringPreferenceValue(
							this,
							R.string.preference_key_reminders_popup_window_displaying_duration,
							null,
							getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_default_value),
							getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_min_value), getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_max_value));
			try {
				text = editText.getText().toString().trim();
				if (text.length() == 0) {
					mReminder.setPlayingTime(playing_time);
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						mReminder.setPlayingTime(playing_time);
					} else {
						mReminder.setPlayingTime(count);
					}
				}
			} catch (NumberFormatException e) {
				mReminder.setPlayingTime(playing_time);
			}
		}
		boolean success = false;
		try {
			DataProvider.runInTx(null, getApplicationContext(), new Runnable() {
				@Override
				public void run() {
					DataProvider.insertOrReplaceScheduledReminder(
							null, getApplicationContext(), mReminder);
					AlarmService.setAlarmForReminder(getApplicationContext(),
							mReminder.getId(), mReminder.getNextSnoozeDateTime(), this);
				}
			});
			NotificationService.updateNotification(getApplicationContext());
			success = true;
		} catch (Exception e) {
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// 2. Chain together various setter methods to set the dialog
			// characteristics
			String exceptionMessage = e.getMessage();
			builder.setMessage(getString(R.string.an_error_occurred_while_saving_the_reminder)
					+ (exceptionMessage == null ? "" : "\n" + e.getMessage()));
			// Add the buttons
			builder.setPositiveButton(R.string.alert_dialog_ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
							// dialog.dismiss();
						}
					});
			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		return success;
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		Bundle b = mPendingBundleForDateOrTimeOrTimeSpanPickerDialog;
		int callerId = b.getInt(CommonConstants.CALLER_ID);
		if (callerId != 0) {
			if (callerId == R.id.button_reminder_date) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
				calendar.set(year, monthOfYear, dayOfMonth);
				mLastSelectedAbsoluteTime = calendar.getTimeInMillis();
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
		Bundle b = mPendingBundleForDateOrTimeOrTimeSpanPickerDialog;
		int callerId = b.getInt(CommonConstants.CALLER_ID);
		if (callerId != 0) {
			if (callerId == R.id.button_reminder_time) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(mLastSelectedAbsoluteTime);
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				mLastSelectedAbsoluteTime = calendar.getTimeInMillis();
				DateFormat mDateFormat = DateFormat.getTimeInstance();
				mDateFormat.setCalendar(calendar);
				String timeString = mDateFormat
						.format(new Date(mLastSelectedAbsoluteTime));
				Button button = (Button) findViewById(R.id.button_reminder_time);
				button.setError(null);
				button.setText(timeString);
			} else if (callerId == R.id.activity_quick_reminder_button_reminder_time_before_event_hours_minutes) {
				mLastSelectedTimeSpanMillis = (hourOfDay * 60L + minute) * 60 * 1000;
				setTimeSpanButtonText(hourOfDay, minute);
			}
		}
	}

	private void setTimeSpanButtonText(int hourOfDay, int minute) {
		Button button = (Button) findViewById(R.id.activity_quick_reminder_button_reminder_time_before_event_hours_minutes);
		button.setText((hourOfDay == 0 ? ""
				: hourOfDay
						+ " "
						+ getResources()
								.getString(
										R.string.activity_quick_reminder_header_textview_reminder_time_before_event_hours))
				+ (minute == 0 ? ""
						: (hourOfDay == 0 ? "" : " ")
								+ minute
								+ " "
								+ getResources()
										.getString(
												R.string.activity_quick_reminder_header_textview_reminder_time_before_event_minutes)));
	}

	private void collectAndCorrectValueOnEditText(EditText editText) {
		String text;
		switch (editText.getId()) {
		case R.id.activity_quick_reminder_edittext_automatic_snoozes_max_count:
			int automaticSnoozesMaxCount = Helper.getIntegerFromStringPreferenceValue(
					this,
					R.string.preference_key_automatic_snoozes_max_count,
					null,
					getResources().getInteger(
							R.integer.automatic_snoozes_max_count_default_value),
					getResources().getInteger(
							R.integer.automatic_snoozes_max_count_min_value), getResources().getInteger(
							R.integer.automatic_snoozes_max_count_max_value));
			try {
				text = editText.getText().toString().trim();
				if (text.length() == 0) {
					mReminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
					editText.setText(automaticSnoozesMaxCount + "");
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						int min = getResources().getInteger(
								R.integer.automatic_snoozes_max_count_min_value);
						mReminder.setAutomaticSnoozesMaxCount(min);
						editText.setText(min + "");
					} else {
						mReminder.setAutomaticSnoozesMaxCount(count);
					}
				}
			} catch (NumberFormatException e) {
				mReminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
				editText.setText(automaticSnoozesMaxCount + "");
			}
			break;
		case R.id.activity_quick_reminder_edittext_automatic_snooze_duration:
			int automaticSnoozeDuration = Helper.getIntegerFromStringPreferenceValue(
					this,
					R.string.preference_key_automatic_snooze_duration,
					null,
					getResources().getInteger(
							R.integer.automatic_snooze_duration_default_value),
					getResources().getInteger(
							R.integer.automatic_snooze_duration_min_value), getResources().getInteger(
							R.integer.automatic_snooze_duration_max_value));
			try {
				text = editText.getText().toString().trim();
				if (text.length() == 0) {
					mReminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
					editText.setText(automaticSnoozeDuration + "");
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						int min = getResources().getInteger(
								R.integer.automatic_snooze_duration_min_value);
						mReminder.setAutomaticSnoozeDuration(min);
						editText.setText(min + "");
					} else {
						mReminder.setAutomaticSnoozeDuration(count);
					}
				}
			} catch (NumberFormatException e) {
				mReminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
				editText.setText(automaticSnoozeDuration + "");
			}
			break;
		case R.id.activity_quick_reminder_edittext_reminders_popup_window_displaying_duration:
			int playing_time = Helper
					.getIntegerFromStringPreferenceValue(
							this,
							R.string.preference_key_reminders_popup_window_displaying_duration,
							null,
							getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_default_value),
							getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_min_value), getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_max_value));
			try {
				text = editText.getText().toString().trim();
				if (text.length() == 0) {
					mReminder.setPlayingTime(playing_time);
					editText.setText(playing_time + "");
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						int min = getResources()
								.getInteger(
										R.integer.reminders_popup_window_displaying_duration_min_value);
						mReminder.setPlayingTime(min);
						editText.setText(min + "");
					} else {
						mReminder.setPlayingTime(count);
					}
				}
			} catch (NumberFormatException e) {
				mReminder.setPlayingTime(playing_time);
				editText.setText(playing_time + "");
			}
			break;
		default:
			break;
		}
	}

	class EditTextAutomaticSnoozesMaxCountChangedListener implements TextWatcher {
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
		public void afterTextChanged(Editable s) {
			ScheduledReminder reminder = (ScheduledReminder) editText.getTag();
			int automaticSnoozesMaxCount = Helper.getIntegerFromStringPreferenceValue(
					ActivityQuickReminder.this,
					R.string.preference_key_automatic_snoozes_max_count,
					null,
					getResources().getInteger(
							R.integer.automatic_snoozes_max_count_default_value),
					getResources().getInteger(
							R.integer.automatic_snoozes_max_count_min_value), getResources().getInteger(
							R.integer.automatic_snoozes_max_count_max_value));
			try {
				String text = editText.getText().toString().trim();
				if (text.length() == 0) {
					reminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
					// editText.setText(automaticSnoozesMaxCount + "");
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						int min = getResources().getInteger(
								R.integer.automatic_snoozes_max_count_min_value);
						reminder.setAutomaticSnoozesMaxCount(min);
						// editText.setText(min + "");
					} else {
						reminder.setAutomaticSnoozesMaxCount(count);
					}
				}
			} catch (NumberFormatException e) {
				reminder.setAutomaticSnoozesMaxCount(automaticSnoozesMaxCount);
				// editText.setText(automaticSnoozesMaxCount + "");
			}
		}
	}

	class EditTextAutomaticSnoozeDurationChangedListener implements TextWatcher {
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
		public void afterTextChanged(Editable s) {
			ScheduledReminder reminder = (ScheduledReminder) editText.getTag();
			int automaticSnoozeDuration = Helper.getIntegerFromStringPreferenceValue(
					ActivityQuickReminder.this,
					R.string.preference_key_automatic_snooze_duration,
					null,
					getResources().getInteger(
							R.integer.automatic_snooze_duration_default_value),
					getResources().getInteger(
							R.integer.automatic_snooze_duration_min_value), getResources().getInteger(
							R.integer.automatic_snooze_duration_max_value));
			try {
				String text = editText.getText().toString().trim();
				if (text.length() == 0) {
					reminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
					// editText.setText(automaticSnoozesMaxCount + "");
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						int min = getResources().getInteger(
								R.integer.automatic_snoozes_max_count_min_value);
						reminder.setAutomaticSnoozeDuration(min);
						// editText.setText(min + "");
					} else {
						reminder.setAutomaticSnoozeDuration(count);
					}
				}
			} catch (NumberFormatException e) {
				reminder.setAutomaticSnoozeDuration(automaticSnoozeDuration);
				// editText.setText(automaticSnoozesMaxCount + "");
			}
		}
	}

	class EditTextPlayingTimeChangedListener implements TextWatcher {
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
		public void afterTextChanged(Editable s) {
			ScheduledReminder reminder = (ScheduledReminder) editText.getTag();
			int playing_time = Helper
					.getIntegerFromStringPreferenceValue(
							ActivityQuickReminder.this,
							R.string.preference_key_reminders_popup_window_displaying_duration,
							null,
							getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_default_value),
							getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_min_value), getResources()
									.getInteger(
											R.integer.reminders_popup_window_displaying_duration_max_value));
			try {
				String text = editText.getText().toString().trim();
				if (text.length() == 0) {
					reminder.setPlayingTime(playing_time);
					// editText.setText(automaticSnoozesMaxCount + "");
				} else {
					int count = Integer.parseInt(text);
					if (count < 0) {
						int min = getResources().getInteger(
								R.integer.automatic_snoozes_max_count_min_value);
						reminder.setPlayingTime(min);
						// editText.setText(min + "");
					} else {
						reminder.setPlayingTime(count);
					}
				}
			} catch (NumberFormatException e) {
				reminder.setPlayingTime(playing_time);
				// editText.setText(automaticSnoozesMaxCount + "");
			}
		}
	}
}
