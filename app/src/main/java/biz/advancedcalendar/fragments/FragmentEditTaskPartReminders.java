package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityEditReminder;
import biz.advancedcalendar.activities.ActivityEditReminder.ActivityEditReminderDataFragment;
import biz.advancedcalendar.activities.ActivityEditTask;
import biz.advancedcalendar.activities.ActivityEditTask.ActivityEditTaskDataFragment;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.db.TaskUiData;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Reminder.AlarmMethod;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.greendao.Task.TaskEditMode;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CheckableLinearLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class FragmentEditTaskPartReminders extends Fragment implements OnClickListener,
		OnCheckedChangeListener, DataSaver {
	final int ADD_REMINDER_ID = Menu.FIRST;
	final int EDIT_REMINDER_REQUEST = 1;
	final int ADD_REMINDER_REQUEST = 2;
	ArrayAdapterReminder mArrayAdapter;
	ListView listviewReminders;
	protected SparseBooleanArray mSparseBooleanArray1;
	protected SparseBooleanArray mSparseBooleanArray2;
	private TaskWithDependentsUiDataHolder taskWithDependentsUiDataHolder;
	private ActivityEditTaskDataFragment dataFragment;
	private TaskWithDependentsUiData taskWithDependentsUiData;
	private TaskUiData taskUiData;
	private List<ReminderUiData> remindersUiData;
	private UserInterfaceData userInterfaceData;
	private ScrollView advancedSettingsSegment;
	private Set<WantingItem> wantingItemSet;
	private String radiobuttonTextCustom;
	final static private String rotateMark = "rotateMark";
	private static final boolean Debug = true;
	private static final String DebugTag = "CreateReminderDebugTag";
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
	private boolean isRotated;
	private boolean isRadioButtonNotificationRingtoneCustomChecked;
	private String alarmRingtoneCustom;
	private String notificationRingtoneCustom;
	private TaskEditMode taskEditMode;
	private BroadcastReceiverForFragmentEditTaskPartReminders mReceiver;

	class BroadcastReceiverForFragmentEditTaskPartReminders extends BroadcastReceiver {
		private IntentFilter intentFilter;

		public BroadcastReceiverForFragmentEditTaskPartReminders(Context context) {
			super();
			intentFilter = new IntentFilter();
			intentFilter
					.addAction(CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_START_TIME_SELECTED);
			intentFilter.addAction(CommonConstants.ACTION_END_TIME_SELECTED);
			intentFilter.addAction(CommonConstants.ACTION_REPETITION_END_TIME_SELECTED);
			LocalBroadcastManager.getInstance(context).registerReceiver(this,
					intentFilter);
		}

		public void addAction(String action) {
			intentFilter.addAction(action);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED)) {
			} else if (intent.getAction().equals(
					CommonConstants.ACTION_START_TIME_SELECTED)) {
				for (ReminderUiData reminderUiData : remindersUiData) {
					reminderUiData.setTask(taskUiData);
				}
				mArrayAdapter.notifyDataSetChanged();
			} else if (intent.getAction()
					.equals(CommonConstants.ACTION_END_TIME_SELECTED)) {
				for (ReminderUiData reminderUiData : remindersUiData) {
					reminderUiData.setTask(taskUiData);
				}
				mArrayAdapter.notifyDataSetChanged();
			} else if (intent.getAction().equals(
					CommonConstants.ACTION_REPETITION_END_TIME_SELECTED)) {
				for (ReminderUiData reminderUiData : remindersUiData) {
					reminderUiData.setTask(taskUiData);
				}
				mArrayAdapter.notifyDataSetChanged();
			}
		}
	}

	public FragmentEditTaskPartReminders() {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders() " + System.identityHashCode(this));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders " + "onAttach(Activity activity) "
							+ System.identityHashCode(this));
		}
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders "
							+ "onCreate(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			isRotated = savedInstanceState
					.getBoolean(FragmentEditTaskPartReminders.rotateMark);
		} else {
			isRotated = false;
		}
		mReceiver = new BroadcastReceiverForFragmentEditTaskPartReminders(getActivity()
				.getApplicationContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//
		taskWithDependentsUiDataHolder = (TaskWithDependentsUiDataHolder) getActivity();
		dataFragment = taskWithDependentsUiDataHolder.getDataFragment();
		taskEditMode = dataFragment.getTaskEditMode();
		taskWithDependentsUiData = taskWithDependentsUiDataHolder
				.getTaskWithDependentsUiData();
		taskUiData = taskWithDependentsUiData.TaskUiData;
		remindersUiData = taskWithDependentsUiData.RemindersUiData;
		userInterfaceData = taskWithDependentsUiDataHolder.getUserInterfaceData();
		//
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders "
							+ "onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		View v = inflater.inflate(R.layout.fragment_edit_task_part_reminders, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders "
							+ "onActivityCreated(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		wantingItemSet = EnumSet.noneOf(WantingItem.class);
		final FragmentActivity activity = getActivity();
		advancedSettingsSegment = (ScrollView) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_scrollview_advanced_settings);
		CheckBox checkBox = (CheckBox) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_checkbox_advanced_settings);
		checkBox.setChecked(false);
		advancedSettingsSegment.setVisibility(View.GONE);
		checkBox.setOnCheckedChangeListener(this);
		alarmRingtoneCustom = taskUiData.getAlarmRingtone();
		notificationRingtoneCustom = taskUiData.getNotificationRingtone();
		final Resources resources = getResources();
		listviewReminders = (ListView) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_listview_reminders);
		radiobuttonTextCustom = resources
				.getString(R.string.fragment_edit_task_part_reminders_radiobutton_text_custom);
		linearlayoutRingtoneFadeInTimeCustomSegment = (LinearLayout) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_linearlayout_ringtone_fade_in_time_custom_segment);
		linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment = (LinearLayout) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_linearlayout_reminders_popup_window_displaying_duration_custom_segment);
		linearlayoutAutomaticSnoozeDurationCustomSegment = (LinearLayout) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_linearlayout_automatic_snooze_duration_custom_segment);
		linearlayoutAutomaticSnoozesMaxCountCustomSegment = (LinearLayout) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_linearlayout_automatic_snoozes_max_count_custom_segment);
		linearlayoutVibrateWithAlarmRingtoneCustomSegment = (LinearLayout) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_linearlayout_vibrate_with_alarm_ringtone_custom_segment);
		radiobuttonVibrateWithAlarmRingtoneCustom = (RadioButton) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_radiobutton_vibrate_with_alarm_ringtone_custom);
		checkboxVibrateWithAlarmRingtone = (CheckBox) activity
				.findViewById(R.id.fragment_edit_task_part_reminders_checkbox_vibrate_with_alarm_ringtone);
		isRadioButtonAlarmRingtoneCustomChecked = EditTextValueChangedListener
				.setupRadiogroupRingtoneFromPreference(
						activity,
						R.string.preference_key_alarm_ringtone,
						R.id.fragment_edit_task_part_reminders_radiobutton_alarm_ringtone_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_alarm_ringtone_custom,
						taskUiData, taskUiData.getAlarmRingtone(), this, this);
		isRadioButtonNotificationRingtoneCustomChecked = EditTextValueChangedListener
				.setupRadiogroupRingtoneFromPreference(
						activity,
						R.string.preference_key_notification_ringtone,
						R.id.fragment_edit_task_part_reminders_radiobutton_notification_ringtone_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_notification_ringtone_custom,
						taskUiData, taskUiData.getNotificationRingtone(), this, this);
		edittextRingtoneFadeInTimeChangedListener = EditTextValueChangedListener
				.setupRadiogroupForModifiedIntegerValueWithResourceBounds(
						activity,
						R.id.fragment_edit_task_part_reminders_textview_ringtone_fade_in_time_measurement_unit,
						R.id.fragment_edit_task_part_reminders_radiobutton_ringtone_fade_in_time_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_ringtone_fade_in_time_custom,
						R.id.fragment_edit_task_part_reminders_edittext_ringtone_fade_in_time,
						R.string.preference_key_ringtone_fade_in_time,
						R.integer.ringtone_fade_in_time_divider,
						R.integer.ringtone_fade_in_time_default_value,
						R.integer.ringtone_fade_in_time_min_value,
						R.integer.ringtone_fade_in_time_max_value,
						R.string.text_second,
						R.string.text_seconds,
						R.string.text_seconds_count_singular,
						R.string.text_seconds_count_plural,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_from_settings,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.RINGTONE_FADE_IN_TIME_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.RINGTONE_FADE_IN_TIME_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.RINGTONE_FADE_IN_TIME_TO_BE_WITHIN_BOUNDS,
						R.id.fragment_edit_task_part_reminders_linearlayout_ringtone_fade_in_time_custom_segment,
						null, taskUiData.getRingtoneFadeInTime(), this, true);
		edittextRemindersPopupWindowDisplayingDurationChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithResourceBounds(
						activity,
						R.id.fragment_edit_task_part_reminders_textview_reminders_popup_window_displaying_duration_measurement_unit,
						R.id.fragment_edit_task_part_reminders_radiobutton_reminders_popup_window_displaying_duration_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_reminders_popup_window_displaying_duration_custom,
						R.id.fragment_edit_task_part_reminders_edittext_reminders_popup_window_displaying_duration,
						R.string.preference_key_reminders_popup_window_displaying_duration,
						R.integer.reminders_popup_window_displaying_duration_default_value,
						R.integer.reminders_popup_window_displaying_duration_min_value,
						R.integer.reminders_popup_window_displaying_duration_max_value,
						R.string.text_second,
						R.string.text_seconds,
						R.string.text_seconds_count_singular,
						R.string.text_seconds_count_plural,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_from_settings,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_TO_BE_WITHIN_BOUNDS,
						R.id.fragment_edit_task_part_reminders_linearlayout_reminders_popup_window_displaying_duration_custom_segment,
						null, taskUiData.getPlayingTime(), this, true);
		edittextAutomaticSnoozeDurationChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithResourceBounds(
						activity,
						R.id.fragment_edit_task_part_reminders_textview_automatic_snooze_duration_measurement_unit,
						R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snooze_duration_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snooze_duration_custom,
						R.id.fragment_edit_task_part_reminders_edittext_automatic_snooze_duration,
						R.string.preference_key_automatic_snooze_duration,
						R.integer.automatic_snooze_duration_default_value,
						R.integer.automatic_snooze_duration_min_value,
						R.integer.automatic_snooze_duration_max_value,
						R.string.text_minute,
						R.string.text_minutes,
						R.string.text_minutes_count_singular,
						R.string.text_minutes_count_plural,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_from_settings,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.AUTOMATIC_SNOOZE_DURATION_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZE_DURATION_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZE_DURATION_TO_BE_WITHIN_BOUNDS,
						R.id.fragment_edit_task_part_reminders_linearlayout_automatic_snooze_duration_custom_segment,
						null, taskUiData.getAutomaticSnoozeDuration(), this, true);
		edittextAutomaticSnoozesMaxCountChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithResourceBounds(
						activity,
						R.id.fragment_edit_task_part_reminders_textview_automatic_snoozes_max_count_measurement_unit,
						R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snoozes_max_count_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snoozes_max_count_custom,
						R.id.fragment_edit_task_part_reminders_edittext_automatic_snoozes_max_count,
						R.string.preference_key_automatic_snoozes_max_count,
						R.integer.automatic_snoozes_max_count_default_value,
						R.integer.automatic_snoozes_max_count_min_value,
						R.integer.automatic_snoozes_max_count_max_value,
						R.string.textview_automatic_snoozes_max_count_text_measurement_unit_singular,
						R.string.textview_automatic_snoozes_max_count_text_measurement_unit_plural,
						R.string.pref_summary_automatic_snoozes_max_count_singular,
						R.string.pref_summary_automatic_snoozes_max_count_plural,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_from_settings,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_custom,
						R.string.custom,
						wantingItemSet,
						WantingItem.AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_GREATER_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_LESS_THAN_OR_EQUAL_TO,
						WantingItem.AUTOMATIC_SNOOZES_MAX_COUNT_TO_BE_WITHIN_BOUNDS,
						R.id.fragment_edit_task_part_reminders_linearlayout_automatic_snoozes_max_count_custom_segment,
						null, taskUiData.getAutomaticSnoozesMaxCount(), this, true);
		EditTextValueChangedListener
				.setupRadiogroupForBooleanValue(
						activity,
						R.id.fragment_edit_task_part_reminders_radiobutton_vibrate_with_alarm_ringtone_from_settings,
						R.id.fragment_edit_task_part_reminders_radiobutton_vibrate_with_alarm_ringtone_custom,
						R.id.fragment_edit_task_part_reminders_checkbox_vibrate_with_alarm_ringtone,
						R.string.preference_key_vibrate_with_alarm_ringtone,
						R.bool.vibrate_with_alarm_ringtone_default_value,
						R.string.vibrate,
						R.string.do_not_vibrate,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_from_settings,
						R.string.fragment_edit_task_part_reminders_radiobutton_text_custom,
						R.string.custom,
						R.id.fragment_edit_task_part_reminders_linearlayout_vibrate_with_alarm_ringtone_custom_segment,
						null, taskUiData.getVibrate(), this, true);
		listviewReminders.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				ReminderUiData reminderUiData = remindersUiData.get(position);
				TaskUiData newTaskUiData = reminderUiData.getTask();
				newTaskUiData.setAlarmRingtone(taskUiData.getAlarmRingtone());
				newTaskUiData.setNotificationRingtone(taskUiData
						.getNotificationRingtone());
				if (edittextRingtoneFadeInTimeChangedListener.getRadioButtonCustom()
						.isChecked()) {
					newTaskUiData
							.setRingtoneFadeInTime(edittextRingtoneFadeInTimeChangedListener
									.getLongValue());
				} else {
					newTaskUiData.setRingtoneFadeInTime(taskUiData
							.getRingtoneFadeInTime());
				}
				if (edittextRemindersPopupWindowDisplayingDurationChangedListener
						.getRadioButtonCustom().isChecked()) {
					newTaskUiData
							.setPlayingTime(edittextRemindersPopupWindowDisplayingDurationChangedListener
									.getIntegerValue());
				} else {
					newTaskUiData.setPlayingTime(taskUiData.getPlayingTime());
				}
				if (edittextAutomaticSnoozeDurationChangedListener.getRadioButtonCustom()
						.isChecked()) {
					newTaskUiData
							.setAutomaticSnoozeDuration(edittextAutomaticSnoozeDurationChangedListener
									.getIntegerValue());
				} else {
					newTaskUiData.setAutomaticSnoozeDuration(taskUiData
							.getAutomaticSnoozeDuration());
				}
				if (edittextAutomaticSnoozesMaxCountChangedListener
						.getRadioButtonCustom().isChecked()) {
					newTaskUiData
							.setAutomaticSnoozesMaxCount(edittextAutomaticSnoozesMaxCountChangedListener
									.getIntegerValue());
				} else {
					newTaskUiData.setAutomaticSnoozesMaxCount(taskUiData
							.getAutomaticSnoozesMaxCount());
				}
				newTaskUiData.setVibrate(taskUiData.getVibrate());
				int requestCode = CommonConstants.REQUEST_CODE_EDIT_REMINDER;
				taskEditMode = TaskEditMode.EDIT;
				startActivityEditReminder(reminderUiData, position, requestCode);
			}
		});
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			listviewReminders.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
			listviewReminders.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					// Here you can do something when items are
					// selected/re-selected,
					// such as update the title in the CAB
				}

				@Override
				public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
					final SparseBooleanArray checkedItemPositions = listviewReminders
							.getCheckedItemPositions();
					final TaskWithDependentsUiData entityWithDependents = ((TaskWithDependentsUiDataHolder) activity)
							.getTaskWithDependentsUiData();
					switch (item.getItemId()) {
					case R.id.set_enabled:
					case R.id.set_disabled:
						for (int i = 0; i < checkedItemPositions.size(); i++) {
							// positions.size() == 2
							if (item.getItemId() == R.id.set_enabled) {
								entityWithDependents.RemindersUiData.get(
										checkedItemPositions.keyAt(i)).setEnabled(true);
							} else {
								entityWithDependents.RemindersUiData.get(
										checkedItemPositions.keyAt(i)).setEnabled(false);
							}
						}
						// Action picked, so close the CAB
						mode.finish();
						return true;
					case R.id.delete:
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								activity);
						alertDialogBuilder
								.setMessage(
										resources
												.getString(R.string.action_delete_checked_reminders_confirmation))
								// .setMessage(
								// getResources().getString(
								// R.string.action_delete_task_confirmation))
								.setCancelable(true)
								// Set the action buttons
								.setPositiveButton(R.string.alert_dialog_ok,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog,
													int id) {
												int j = 0;
												for (int i = 0; i < checkedItemPositions
														.size(); i++) {
													entityWithDependents.RemindersUiData
															.remove(checkedItemPositions
																	.keyAt(i) - j);
													j++;
												}
												mode.finish();
											}
										})
								.setNegativeButton(R.string.alert_dialog_cancel, null);
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
						// show it
						alertDialog.show();
						//
						//
						//
						// Action picked, so close the CAB
						return true;
					default:
						return false;
					}
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// Inflate the menu for the CAB
					// mActionMode = mode;
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.fragment_task_viewing_reminders_list_item,
							menu);
					mode.setTitle(R.string.fragment_edit_task_part_reminders_select_reminders);
					return true;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// Here you can make any necessary updates to the activity
					// when
					// the CAB is removed. By default, selected items are
					// deselected/unchecked.
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Here you can perform updates to the CAB due to
					// an invalidate() request
					return false;
				}
			});
		} else {
			registerForContextMenu(listviewReminders);
		}
		//
		// If mArrayAdapter.notifyDataSetChanged() don't work, make sure and
		// double check you are passing into mArrayAdapter exactly the same list, you
		// later will add items into.
		//
		List<ReminderUiData> reminderUiDataList = new ArrayList<ReminderUiData>();
		for (Reminder reminder : remindersUiData) {
			reminderUiDataList.add(new ReminderUiData(reminder, taskUiData));
		}
		//
		// This was bug
		//
		mArrayAdapter = new ArrayAdapterReminder(activity,
				R.layout.fragment_view_task_part_reminders_list_item2, reminderUiDataList);
		//
		// make sure you are passing into mArrayAdapter exactly the same list, you later
		// will add items into
		//
		mArrayAdapter = new ArrayAdapterReminder(activity,
				R.layout.fragment_view_task_part_reminders_list_item2, remindersUiData);
		listviewReminders.setAdapter(mArrayAdapter);
		if (taskEditMode == TaskEditMode.QUICK && !isRotated) {
			tryShowReminderEditor();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders "
							+ "onCreateOptionsMenu(Menu menu, MenuInflater inflater) "
							+ System.identityHashCode(this));
		}
		Bundle b = getArguments();
		boolean showAddMenu = b.getBoolean(CommonConstants.INTENT_EXTRA_SHOW_ADD_MENU,
				false);
		if (showAddMenu) {
			MenuItem addItem = menu.add(Menu.NONE, ADD_REMINDER_ID, 0, getResources()
					.getString(R.string.action_reminder_new));
			addItem.setIcon(R.drawable.ic_alarm_add_black_24dp);
			MenuItemCompat.setShowAsAction(addItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case ADD_REMINDER_ID:
			return tryShowReminderEditor();
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// menu.setHeaderTitle("Context Menu");
		menu.add(0, R.id.delete, 0,
				R.string.fragment_view_scheduled_reminders_menu_delete);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final SparseBooleanArray checkedItemPositions = listviewReminders
				.getCheckedItemPositions();
		final TaskWithDependentsUiData entityWithDependents = ((TaskWithDependentsUiDataHolder) getActivity())
				.getTaskWithDependentsUiData();
		switch (item.getItemId()) {
		// case R.id.set_enabled:
		// case R.id.set_disabled:
		// for (int i = 0; i < checkedItemPositions.size(); i++) {
		// // positions.size() == 2
		// if (item.getItemId() == R.id.set_enabled) {
		// entityWithDependents.RemindersUiData.get(
		// checkedItemPositions.keyAt(i)).setEnabled(true);
		// } else {
		// entityWithDependents.RemindersUiData.get(
		// checkedItemPositions.keyAt(i)).setEnabled(false);
		// }
		// }
		// return true;
		case R.id.delete:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			final Resources resources = getResources();
			alertDialogBuilder
					.setMessage(
							resources
									.getString(R.string.action_delete_reminder_confirmation))
					// .setMessage(
					// getResources().getString(
					// R.string.action_delete_task_confirmation))
					.setCancelable(true)
					// Set the action buttons
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									ListView.AdapterContextMenuInfo menuInfo = (ListView.AdapterContextMenuInfo) item
											.getMenuInfo();
									entityWithDependents.RemindersUiData
											.remove(menuInfo.position);
								}
							}).setNegativeButton(R.string.alert_dialog_cancel, null);
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			//
			//
			//
			return true;
		default:
			return false;
		}
	}

	private boolean tryShowReminderEditor() {
		if (edittextRingtoneFadeInTimeChangedListener.getRadioButtonCustom().isChecked()) {
			taskUiData.setRingtoneFadeInTime(edittextRingtoneFadeInTimeChangedListener
					.getLongValue());
		}
		if (edittextRemindersPopupWindowDisplayingDurationChangedListener
				.getRadioButtonCustom().isChecked()) {
			taskUiData
					.setPlayingTime(edittextRemindersPopupWindowDisplayingDurationChangedListener
							.getIntegerValue());
		}
		if (edittextAutomaticSnoozeDurationChangedListener.getRadioButtonCustom()
				.isChecked()) {
			taskUiData
					.setAutomaticSnoozeDuration(edittextAutomaticSnoozeDurationChangedListener
							.getIntegerValue());
		}
		if (edittextAutomaticSnoozesMaxCountChangedListener.getRadioButtonCustom()
				.isChecked()) {
			taskUiData
					.setAutomaticSnoozesMaxCount(edittextAutomaticSnoozesMaxCountChangedListener
							.getIntegerValue());
		}
		FragmentActivity context = getActivity();
		Resources resources = context.getResources();
		userInterfaceData.isRecurrenceDataComplete(context);
		Set<WantingItem> wantingItemSet = userInterfaceData.wantingItemSet;
		if (wantingItemSet.isEmpty()) {
			taskUiData.setTaskOccurrenceList(userInterfaceData.recurrenceInterval
					.getTaskOccurrences());
			taskUiData.setName(userInterfaceData.textTaskName.toString());
			byte reminderTimeModeValue;
			long lastSelectedTimeSpanMillis;
			long lastSelectedAbsoluteTime;
			lastSelectedTimeSpanMillis = 1000L * 60 * Helper
					.getIntegerFromStringPreferenceValue(
							context,
							R.string.preference_key_automatic_snooze_duration,
							null,
							resources
									.getInteger(R.integer.automatic_snooze_duration_default_value),
							resources
									.getInteger(R.integer.automatic_snooze_duration_min_value),
							resources
									.getInteger(R.integer.automatic_snooze_duration_max_value));
			if (userInterfaceData.recurrenceInterval == RecurrenceInterval.ONE_TIME) {
				reminderTimeModeValue = ReminderTimeMode.AFTER_NOW.getValue();
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.add(Calendar.MILLISECOND, (int) lastSelectedTimeSpanMillis);
				lastSelectedAbsoluteTime = calendar.getTimeInMillis();
			} else {
				reminderTimeModeValue = (byte) Helper
						.getIntegerPreferenceValueFromStringArray(context,
								R.string.preference_key_reminder_time_mode,
								R.array.reminder_time_mode_values_array,
								R.integer.reminder_time_mode_default_value);
				if (reminderTimeModeValue == ReminderTimeMode.ABSOLUTE_TIME.getValue()
						|| reminderTimeModeValue == ReminderTimeMode.AFTER_NOW.getValue()) {
					Calendar calendar = Calendar.getInstance();
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					calendar.add(Calendar.MILLISECOND, (int) lastSelectedTimeSpanMillis);
					lastSelectedAbsoluteTime = calendar.getTimeInMillis();
				} else {
					lastSelectedAbsoluteTime = lastSelectedTimeSpanMillis;
				}
			}
			byte alarmMethodValue = (byte) Helper
					.getIntegerPreferenceValueFromStringArray(context,
							R.string.preference_key_alarm_method,
							R.array.alarm_method_values_array,
							R.integer.alarm_method_default_value);
			AlarmMethod alarmMethod = AlarmMethod.fromInt(alarmMethodValue);
			boolean isAlarm;
			switch (alarmMethod) {
			case POPUP_WINDOW:
			default:
				isAlarm = true;
				break;
			case STATUS_BAR:
				isAlarm = false;
				break;
			}
			String remindText = getResources().getString(
					R.string.activity_edit_reminder_edittext_template_text);
			if (taskUiData.getName() != null) {
				remindText = getResources().getString(
						R.string.activity_edit_reminder_edittext_template_text)
						+ " " + taskUiData.getName();
			}
			Reminder r = new Reminder(null, null, null, 0, lastSelectedAbsoluteTime,
					reminderTimeModeValue, remindText, true, isAlarm, null, null, null,
					null, null, null, null, null, null, null);
			ReminderUiData reminderUiData = new ReminderUiData(r, taskUiData);
			// reminderUiData.setEnabled(true);
			int position = 0;
			int requestCode = CommonConstants.REQUEST_CODE_ADD_REMINDER;
			startActivityEditReminder(reminderUiData, position, requestCode);
			return true;
		} else {
			String title = getResources()
					.getString(
							R.string.fragment_edit_task_part_reminders_cannot_launch_reminder_editor_because_the_task_is_in_inconsistent_state);
			Helper.showWantingItemSetMessages(context, wantingItemSet, title);
			return false;
		}
	}

	private void startActivityEditReminder(ReminderUiData reminderUiData, int position,
			int requestCode) {
		Resources resources = getResources();
		FragmentActivity context = getActivity();
		long lastSelectedTimeSpanMillis;
		long lastSelectedAbsoluteTime;
		if (taskEditMode == TaskEditMode.QUICK) {
			reminderUiData
					.setReminderTimeModeValue(ReminderTimeMode.AFTER_NOW.getValue());
			lastSelectedTimeSpanMillis = 1000L * 60 * Helper
					.getIntegerFromStringPreferenceValue(
							context,
							R.string.preference_key_automatic_snooze_duration,
							null,
							resources
									.getInteger(R.integer.automatic_snooze_duration_default_value),
							resources
									.getInteger(R.integer.automatic_snooze_duration_min_value),
							resources
									.getInteger(R.integer.automatic_snooze_duration_max_value));
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.add(Calendar.MILLISECOND, (int) lastSelectedTimeSpanMillis);
			lastSelectedAbsoluteTime = calendar.getTimeInMillis();
		} else {
			if (reminderUiData.getReminderTimeModeValue() == ReminderTimeMode.ABSOLUTE_TIME
					.getValue()
					|| reminderUiData.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW
							.getValue()) {
				lastSelectedTimeSpanMillis = 1000L * 60 * Helper
						.getIntegerFromStringPreferenceValue(
								context,
								R.string.preference_key_automatic_snooze_duration,
								null,
								resources
										.getInteger(R.integer.automatic_snooze_duration_default_value),
								resources
										.getInteger(R.integer.automatic_snooze_duration_min_value),
								resources
										.getInteger(R.integer.automatic_snooze_duration_max_value));
				lastSelectedAbsoluteTime = reminderUiData.getReminderDateTime();
			} else {
				lastSelectedTimeSpanMillis = reminderUiData.getReminderDateTime();
				Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				calendar.add(Calendar.MILLISECOND, (int) lastSelectedTimeSpanMillis);
				lastSelectedAbsoluteTime = calendar.getTimeInMillis();
			}
		}
		boolean is24HourFormat = Helper.is24HourFormat(context);
		ActivityEditReminderDataFragment activityEditReminderDataFragment = new ActivityEditReminderDataFragment(
				taskWithDependentsUiData, userInterfaceData, lastSelectedAbsoluteTime,
				lastSelectedTimeSpanMillis, null, reminderUiData, is24HourFormat);
		Intent intent = new Intent(context, ActivityEditReminder.class);
		intent.putExtra(CommonConstants.INIT_ARGUMENTS, activityEditReminderDataFragment);
		intent.putExtra(CommonConstants.INTENT_EXTRA_POSITION, position);
		startActivityForResult(intent, requestCode);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders "
							+ "onActivityResult(int requestCode, int resultCode, Intent intent)  "
							+ System.identityHashCode(this));
		}
		switch (requestCode) {
		case CommonConstants.REQUEST_CODE_EDIT_REMINDER:
			if (resultCode == android.app.Activity.RESULT_OK) {
				ReminderUiData reminderUiData = (ReminderUiData) intent.getExtras()
						.getParcelable(CommonConstants.INTENT_EXTRA_REMINDER);
				remindersUiData.set(
						intent.getExtras().getInt(CommonConstants.INTENT_EXTRA_POSITION),
						reminderUiData);
				// mArrayAdapter = new ArrayAdapterReminder(getActivity(),
				// R.layout.fragment_view_task_part_reminders_list_item2,
				// entityWithDependents.reminders, entityWithDependents.task);
				// mListView.setAdapter(mArrayAdapter);
				mArrayAdapter.notifyDataSetChanged();
				if (reminderUiData.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW
						.getValue()) {
					convertAfterNowTimeToAbsolute(reminderUiData);
				}
			}
			break;
		case CommonConstants.REQUEST_CODE_ADD_REMINDER:
			// Make sure the request was successful
			if (resultCode == android.app.Activity.RESULT_OK) {
				ReminderUiData reminderUiData = (ReminderUiData) intent.getExtras()
						.getParcelable(CommonConstants.INTENT_EXTRA_REMINDER);
				String taskTitle = String.format(
						getResources().getString(R.string.task_title_for_quick_reminder),
						reminderUiData.getText());
				taskUiData.setTaskTitleForQuickReminder(taskTitle);
				// if (taskEditMode == TaskEditMode.QUICK) {
				taskUiData.setName(taskTitle);
				// }
				remindersUiData.add(0, reminderUiData);
				// If mArrayAdapter.notifyDataSetChanged() don't work, make sure and
				// double check you are passing into mArrayAdapter the list you are adding
				// items into later.
				mArrayAdapter.notifyDataSetChanged();
				//
				if (reminderUiData.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW
						.getValue()) {
					convertAfterNowTimeToAbsolute(reminderUiData);
				}
				if (taskEditMode == TaskEditMode.QUICK) {
					ActivityEditTask activity = (ActivityEditTask) getActivity();
					if (reminderUiData.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW
							.getValue()
							|| reminderUiData.getReminderTimeModeValue() == ReminderTimeMode.ABSOLUTE_TIME
									.getValue()) {
						userInterfaceData.startDateTime = userInterfaceData.endDateTime = reminderUiData
								.getReminderDateTime();
						taskUiData.setStartDateTime(reminderUiData.getReminderDateTime());
						taskUiData.setEndDateTime(reminderUiData.getReminderDateTime());
						// if (!isDataCollected()) {
						// break;
						// }
						// ActivityEditTask activity = (ActivityEditTask) getActivity();
						activity.trySave();
						// activity.setupUi(taskUiData);
					} else {
						taskUiData
								.setStartDateTime(userInterfaceData.startDateTimePreviousValue);
						taskUiData
								.setEndDateTime(userInterfaceData.startDateTimePreviousValue);
						taskUiData
								.setRepetitionEndDateTime(userInterfaceData.repetitionEndDateTimePreviousValue);
						activity.setupUi(taskUiData);
					}
				}
			} else {
				if (taskEditMode == TaskEditMode.QUICK) {
					getActivity().finish();
				}
			}
			break;
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
				taskUiData.setAlarmRingtone(ringtone);
				alarmRingtoneCustom = ringtone;
				String textCustom = String
						.format(getResources()
								.getString(
										R.string.fragment_edit_task_part_reminders_radiobutton_text_custom),
								taskUiData.getRingtoneTitle(getActivity(),
										R.string.preference_key_alarm_ringtone));
				RadioButton radioButtonCustom = (RadioButton) getActivity()
						.findViewById(
								R.id.fragment_edit_task_part_reminders_radiobutton_alarm_ringtone_custom);
				radioButtonCustom.setText(textCustom);
				mArrayAdapter.notifyDataSetChanged();
			} else {
				if (!isRadioButtonAlarmRingtoneCustomChecked) {
					RadioButton radioButtonDefault = (RadioButton) getActivity()
							.findViewById(
									R.id.fragment_edit_task_part_reminders_radiobutton_alarm_ringtone_from_settings);
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
				taskUiData.setNotificationRingtone(ringtone);
				notificationRingtoneCustom = ringtone;
				String textCustom = String
						.format(getResources()
								.getString(
										R.string.fragment_edit_task_part_reminders_radiobutton_text_custom),
								taskUiData.getRingtoneTitle(getActivity(),
										R.string.preference_key_notification_ringtone));
				RadioButton radioButtonCustom = (RadioButton) getActivity()
						.findViewById(
								R.id.fragment_edit_task_part_reminders_radiobutton_notification_ringtone_custom);
				radioButtonCustom.setText(textCustom);
				mArrayAdapter.notifyDataSetChanged();
			} else {
				if (!isRadioButtonNotificationRingtoneCustomChecked) {
					RadioButton radioButtonDefault = (RadioButton) getActivity()
							.findViewById(
									R.id.fragment_edit_task_part_reminders_radiobutton_notification_ringtone_from_settings);
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

	public void convertAfterNowTimeToAbsolute(ReminderUiData reminderUiData) {
		long nowTime = System.currentTimeMillis();
		long reminderDateTime = nowTime + reminderUiData.getReminderDateTime();
		reminderUiData.setReminderDateTime(reminderDateTime);
		reminderUiData
				.setReminderTimeModeValue(ReminderTimeMode.ABSOLUTE_TIME.getValue());
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders "
							+ "onSaveInstanceState(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putBoolean(FragmentEditTaskPartReminders.rotateMark, true);
	}

	@Override
	public void onPause() {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders " + "onPause() "
							+ System.identityHashCode(this));
		}
		super.onPause();
		if (!getActivity().isFinishing()) {
		}
	}

	@Override
	public void onResume() {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders " + "onResume() "
							+ System.identityHashCode(this));
		}
		super.onResume();
	}

	@Override
	public void onStop() {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders " + "onStop() "
							+ System.identityHashCode(this));
		}
		super.onStop();
		if (!getActivity().isFinishing()) {
			// collect info
			// saveData();
		}
	}

	@Override
	public void onDestroy() {
		if (FragmentEditTaskPartReminders.Debug) {
			Log.d(FragmentEditTaskPartReminders.DebugTag,
					"FragmentEditTaskPartReminders " + "onDestroy() "
							+ System.identityHashCode(this));
		}
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
	}

	@Override
	public boolean isDataCollected() {
		// Currently all changes to reminder made on ui immediately applied to variable
		// reminder in ram. There might be
		// another approach to apply where all changes are saved to reminder variable in
		// ram in this method which would be called
		// every time ui might disappear, i.e. in onpause()
		//
		//
		// I changed my mind. The another approach is not possible. Because list items
		// will be recycled by listview once they go out if visibility.
		// Therefore we have to remeber settings to reminder variable in ram immediately
		// after change in the ui has been made
		boolean isCollected = true;
		for (Reminder newReminder : remindersUiData) {
			if (newReminder.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW
					.getValue()) {
				newReminder.setReminderTimeModeValue(ReminderTimeMode.ABSOLUTE_TIME
						.getValue());
			}
			if (newReminder.getReminderDateTime() < 0) {
				throw new IllegalArgumentException("reminder.getReminderDateTime() < 0");
			}
			if (taskUiData.getRecurrenceIntervalValue() == RecurrenceInterval.ONE_TIME
					.getValue()) {
				if (taskUiData.getStartDateTime() == null) {
					if (newReminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
							.getValue()
							&& newReminder.getReminderTimeModeValue() != ReminderTimeMode.AFTER_NOW
									.getValue()) {
						isCollected = false;
						Toast.makeText(
								getActivity(),
								String.format(
										getResources()
												.getString(
														R.string.toast_text_set_reminders_start_type_to_absolute_time_or_select_task_start_time),
										newReminder.getText()), Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		if (!wantingItemSet.isEmpty()) {
			isCollected = false;
			Helper.showWantingItemSetMessages(getActivity(), wantingItemSet, null);
		} else {
			if (edittextRingtoneFadeInTimeChangedListener.getRadioButtonCustom()
					.isChecked()) {
				taskUiData
						.setRingtoneFadeInTime(edittextRingtoneFadeInTimeChangedListener
								.getLongValue());
			}
			if (edittextRemindersPopupWindowDisplayingDurationChangedListener
					.getRadioButtonCustom().isChecked()) {
				taskUiData
						.setPlayingTime(edittextRemindersPopupWindowDisplayingDurationChangedListener
								.getIntegerValue());
			}
			if (edittextAutomaticSnoozeDurationChangedListener.getRadioButtonCustom()
					.isChecked()) {
				taskUiData
						.setAutomaticSnoozeDuration(edittextAutomaticSnoozeDurationChangedListener
								.getIntegerValue());
			}
			if (edittextAutomaticSnoozesMaxCountChangedListener.getRadioButtonCustom()
					.isChecked()) {
				taskUiData
						.setAutomaticSnoozesMaxCount(edittextAutomaticSnoozesMaxCountChangedListener
								.getIntegerValue());
			}
		}
		return isCollected;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.fragment_edit_task_part_reminders_radiobutton_alarm_ringtone_custom:
			Intent intent = Helper.initializeIntentForRingtonePicker(getActivity(),
					alarmRingtoneCustom, R.string.preference_key_alarm_ringtone,
					RingtoneManager.TYPE_ALARM, Settings.System.DEFAULT_ALARM_ALERT_URI,
					CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE);
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_notification_ringtone_custom:
			intent = Helper.initializeIntentForRingtonePicker(getActivity(),
					notificationRingtoneCustom,
					R.string.preference_key_notification_ringtone,
					RingtoneManager.TYPE_NOTIFICATION,
					Settings.System.DEFAULT_NOTIFICATION_URI,
					CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE);
			startActivityForResult(intent,
					CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int buttonViewId = buttonView.getId();
		switch (buttonViewId) {
		case R.id.fragment_edit_task_part_reminders_checkbox_advanced_settings:
			if (isChecked) {
				buttonView.setText(R.string.action_less);
				advancedSettingsSegment.setVisibility(View.VISIBLE);
				listviewReminders.setVisibility(View.GONE);
			} else {
				buttonView.setText(R.string.action_more);
				advancedSettingsSegment.setVisibility(View.GONE);
				listviewReminders.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_alarm_ringtone_from_settings:
			if (isChecked) {
				isRadioButtonAlarmRingtoneCustomChecked = false;
				taskUiData.setAlarmRingtone(null);
				mArrayAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_notification_ringtone_from_settings:
			if (isChecked) {
				isRadioButtonNotificationRingtoneCustomChecked = false;
				taskUiData.setNotificationRingtone(null);
				mArrayAdapter.notifyDataSetChanged();
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_ringtone_fade_in_time_from_settings:
			if (isChecked) {
				taskUiData.setRingtoneFadeInTime(null);
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutRingtoneFadeInTimeCustomSegment.setVisibility(View.GONE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_ringtone_fade_in_time_custom:
			if (isChecked) {
				taskUiData
						.setRingtoneFadeInTime(edittextRingtoneFadeInTimeChangedListener
								.getLongValue());
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutRingtoneFadeInTimeCustomSegment.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_reminders_popup_window_displaying_duration_from_settings:
			if (isChecked) {
				taskUiData.setPlayingTime(null);
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment
						.setVisibility(View.GONE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_reminders_popup_window_displaying_duration_custom:
			if (isChecked) {
				taskUiData
						.setPlayingTime(edittextRemindersPopupWindowDisplayingDurationChangedListener
								.getIntegerValue());
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutRemindersPopupWindowDisplayingDurationCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snooze_duration_from_settings:
			if (isChecked) {
				taskUiData.setAutomaticSnoozeDuration(null);
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutAutomaticSnoozeDurationCustomSegment.setVisibility(View.GONE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snooze_duration_custom:
			if (isChecked) {
				taskUiData
						.setAutomaticSnoozeDuration(edittextAutomaticSnoozeDurationChangedListener
								.getIntegerValue());
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutAutomaticSnoozeDurationCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snoozes_max_count_from_settings:
			if (isChecked) {
				taskUiData.setAutomaticSnoozesMaxCount(null);
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutAutomaticSnoozesMaxCountCustomSegment
						.setVisibility(View.GONE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_automatic_snoozes_max_count_custom:
			if (isChecked) {
				taskUiData
						.setAutomaticSnoozesMaxCount(edittextAutomaticSnoozesMaxCountChangedListener
								.getIntegerValue());
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutAutomaticSnoozesMaxCountCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_vibrate_with_alarm_ringtone_from_settings:
			if (isChecked) {
				taskUiData.setVibrate(null);
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutVibrateWithAlarmRingtoneCustomSegment
						.setVisibility(View.GONE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_radiobutton_vibrate_with_alarm_ringtone_custom:
			if (isChecked) {
				taskUiData.setVibrate(checkboxVibrateWithAlarmRingtone.isChecked());
				mArrayAdapter.notifyDataSetChanged();
				linearlayoutVibrateWithAlarmRingtoneCustomSegment
						.setVisibility(View.VISIBLE);
			}
			break;
		case R.id.fragment_edit_task_part_reminders_checkbox_vibrate_with_alarm_ringtone:
			String summaryTextCustom = Helper.getBooleanPreferenceSummary(getActivity(),
					isChecked, R.string.vibrate, R.string.do_not_vibrate);
			String textCustom = String.format(radiobuttonTextCustom, summaryTextCustom);
			radiobuttonVibrateWithAlarmRingtoneCustom.setText(textCustom);
			taskUiData.setVibrate(isChecked);
			mArrayAdapter.notifyDataSetChanged();
			break;
		default:
			break;
		}
	}

	class ArrayAdapterReminder extends ArrayAdapter<ReminderUiData> {
		private LayoutInflater mLayoutInflater;

		// List<ReminderUiData> mReminderUiDataList;
		// private int mListRowPosition;
		// Task mTask;
		// private AlertDialog mDialog;
		public ArrayAdapterReminder(Context context, int textViewResourceId,
				List<ReminderUiData> reminderUiDataList) {
			super(context, textViewResourceId, reminderUiDataList);
			mLayoutInflater = LayoutInflater.from(context);
			// mReminderUiDataList = reminderUiDataList;
			// mTask = task;
			// Create AlertDialog here
			// AlertDialog.Builder builder = new AlertDialog.Builder(context);
			// builder.setMessage(
			// R.string.fragment_task_viewing_reminders_list_item_delete_warning)
			// .setPositiveButton(R.string.alert_dialog_ok,
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int id) {
			// // Use mListRowPosition for clicked list row...
			// // User clicked OK button
			// TaskWithDependentsUiData entityWithDependents =
			// ((TaskWithDependentsUiDataHolder) getActivity())
			// .getTaskWithDependentsUiData();
			// entityWithDependents.reminders
			// .remove(mListRowPosition);
			// mArrayAdapter.notifyDataSetChanged();
			// dialog.dismiss();
			// }
			// })
			// .setNegativeButton(R.string.alert_dialog_cancel,
			// new DialogInterface.OnClickListener() {
			// @Override
			// public void onClick(DialogInterface dialog, int id) {
			// // User cancelled the dialog
			// }
			// });
			// // Create the AlertDialog object
			// mDialog = builder.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CheckableLinearLayout checkableLinearLayout;
			FragmentActivity activity = getActivity();
			if (convertView == null) {
				checkableLinearLayout = (CheckableLinearLayout) activity
						.getLayoutInflater().inflate(
								R.layout.fragment_view_task_part_reminders_list_item2,
								parent, false);
			} else {
				checkableLinearLayout = (CheckableLinearLayout) convertView;
			}
			TableLayout tableLayout = (TableLayout) checkableLinearLayout
					.findViewById(R.id.fragment_view_task_part_reminders_list_item_tablelayout);
			tableLayout.removeAllViews();
			ReminderUiData reminder = getItem(position);
			checkableLinearLayout.setTag(position);
			// fill task text row
			Resources resources = getResources();
			String text;
			boolean isAlarm = reminder.getIsAlarm();
			text = reminder.getEnabled() ? resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_state_enabled)
					: resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_state_disabled);
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_reminder_state,
					text);
			// setup reminder text
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_reminder_text,
					reminder.getText());
			// setup reminder type
			text = isAlarm ? resources
					.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_type_alarm)
					: resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_type_notification);
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_alarm_mode,
					text);
			// setup IsRingtoneCustom
			int preferenceId;
			if (isAlarm) {
				preferenceId = R.string.preference_key_alarm_ringtone;
			} else {
				preferenceId = R.string.preference_key_notification_ringtone;
			}
			if (reminder.getRingtone() == null) {
				String defaultRingtoneTitle = taskUiData.getRingtoneTitle(activity,
						preferenceId);
				text = String
						.format(resources
								.getString(R.string.activity_edit_reminder_radiobutton_text_from_task),
								defaultRingtoneTitle);
			} else {
				text = String
						.format(resources
								.getString(R.string.activity_edit_reminder_radiobutton_text_custom),
								reminder.getRingtoneTitle(activity));
			}
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_reminder_ringtone,
					text);
			if (isAlarm) {
				Integer inheritedValueInteger = null;
				Long inheritedValueLong = taskUiData.getRingtoneFadeInTime();
				if (inheritedValueLong != null) {
					inheritedValueInteger = inheritedValueLong.intValue();
				}
				Integer valueInteger = null;
				Long valueLong = reminder.getRingtoneFadeInTime();
				if (valueLong != null) {
					valueInteger = valueLong.intValue();
				}
				text = getTextForIntegerValue(activity, resources, inheritedValueInteger,
						valueInteger, R.integer.ringtone_fade_in_time_default_value,
						R.integer.ringtone_fade_in_time_min_value,
						R.integer.ringtone_fade_in_time_max_value,
						R.string.preference_key_ringtone_fade_in_time,
						R.string.text_seconds_count_singular,
						R.string.text_seconds_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom);
				reminder.generateTableRow(
						tableLayout,
						mLayoutInflater,
						resources,
						R.string.fragment_view_task_part_reminders_header_textview_ringtone_fade_in_time,
						text);
				text = getTextForIntegerValue(
						activity,
						resources,
						taskUiData.getPlayingTime(),
						reminder.getPlayingTime(),
						R.integer.reminders_popup_window_displaying_duration_default_value,
						R.integer.reminders_popup_window_displaying_duration_min_value,
						R.integer.reminders_popup_window_displaying_duration_max_value,
						R.string.preference_key_reminders_popup_window_displaying_duration,
						R.string.text_seconds_count_singular,
						R.string.text_seconds_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom);
				reminder.generateTableRow(
						tableLayout,
						mLayoutInflater,
						resources,
						R.string.fragment_view_task_part_reminders_header_textview_reminders_popup_window_displaying_duration,
						text);
				text = getTextForIntegerValue(activity, resources,
						taskUiData.getAutomaticSnoozeDuration(),
						reminder.getAutomaticSnoozeDuration(),
						R.integer.automatic_snooze_duration_default_value,
						R.integer.automatic_snooze_duration_min_value,
						R.integer.automatic_snooze_duration_max_value,
						R.string.preference_key_automatic_snooze_duration,
						R.string.text_minutes_count_singular,
						R.string.text_minutes_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom);
				reminder.generateTableRow(
						tableLayout,
						mLayoutInflater,
						resources,
						R.string.fragment_view_task_part_reminders_header_textview_automatic_snooze_duration,
						text);
				text = getTextForIntegerValue(activity, resources,
						taskUiData.getAutomaticSnoozesMaxCount(),
						reminder.getAutomaticSnoozesMaxCount(),
						R.integer.automatic_snoozes_max_count_default_value,
						R.integer.automatic_snoozes_max_count_min_value,
						R.integer.automatic_snoozes_max_count_max_value,
						R.string.preference_key_automatic_snoozes_max_count,
						R.string.pref_summary_automatic_snoozes_max_count_singular,
						R.string.pref_summary_automatic_snoozes_max_count_plural,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom);
				reminder.generateTableRow(
						tableLayout,
						mLayoutInflater,
						resources,
						R.string.fragment_view_task_part_reminders_header_textview_automatic_snoozes_max_count,
						text);
				text = getTextForBooleanValue(activity, resources,
						taskUiData.getVibrate(), reminder.getVibrate(),
						R.bool.vibrate_with_alarm_ringtone_default_value,
						R.string.preference_key_vibrate_with_alarm_ringtone,
						R.string.vibrate, R.string.do_not_vibrate,
						R.string.activity_edit_reminder_radiobutton_text_from_task,
						R.string.activity_edit_reminder_radiobutton_text_custom);
				reminder.generateTableRow(
						tableLayout,
						mLayoutInflater,
						resources,
						R.string.fragment_view_task_part_reminders_header_textview_vibrate_with_alarm_ringtone,
						text);
			}
			// setup reminder time type
			switch (ReminderTimeMode.fromInt(reminder.getReminderTimeModeValue())) {
			case ABSOLUTE_TIME:
				text = resources
						.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_absolute_time);
				break;
			case AFTER_NOW:
				text = resources
						.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_absolute_time);
				break;
			case TIME_BEFORE_EVENT:
				text = resources
						.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_time_before_event);
				break;
			case TIME_AFTER_EVENT:
				text = resources
						.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_time_after_event);
				break;
			default:
				text = "";
				break;
			}
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_reminder_time_mode,
					text);
			// setup reminder type absolute (time and date buttons)
			// setup reminder type offset
			switch (ReminderTimeMode.fromInt(reminder.getReminderTimeModeValue())) {
			case ABSOLUTE_TIME:
			case AFTER_NOW:
				text = reminder.getTextForAbsoluteTime();
				break;
			case TIME_AFTER_EVENT:
			case TIME_BEFORE_EVENT:
				text = reminder
						.getTextForReminderTimeIntervalAndTimeModeAndNextAlarmTime(
								activity, userInterfaceData);
				break;
			default:
				text = "";
				break;
			}
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_reminder_time,
					text);
			// setup RemindEachIteration
			text = resources
					.getString(reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
							.getValue()
							&& reminder.getReminderTimeModeValue() != ReminderTimeMode.AFTER_NOW
									.getValue()
							&& userInterfaceData.recurrenceInterval != RecurrenceInterval.ONE_TIME ? R.string.fragment_view_task_part_reminders_value_textview_is_recurrent_reminder_yes
							: R.string.fragment_view_task_part_reminders_value_textview_is_recurrent_reminder_no);
			reminder.generateTableRow(
					tableLayout,
					mLayoutInflater,
					resources,
					R.string.fragment_view_task_part_reminders_header_textview_is_recurrent_reminder,
					text);
			return checkableLinearLayout;
		}

		// @Override
		// public long getItemId(int position) {
		// return mReminders.get(position).getId();
		// }
		//
		// @Override
		// public boolean hasStableIds() {
		// return true;
		// }
		private String getTextForBooleanValue(FragmentActivity activity,
				Resources resources, Boolean inheritedBooleanWrapper,
				Boolean booleanWrapper, int defaultValueId, int preferenceKeyId,
				int checkedBeingStringId, int uncheckedBeingStringId,
				int radioButtonDefaultTextId, int radioButtonCustomTextId) {
			String text;
			if (booleanWrapper == null) {
				boolean inheritedValue;
				if (inheritedBooleanWrapper == null) {
					inheritedValue = Helper.getBooleanPreferenceValue(activity,
							preferenceKeyId, defaultValueId);
				} else {
					inheritedValue = inheritedBooleanWrapper.booleanValue();
				}
				String summaryTextDefault = Helper.getBooleanPreferenceSummary(activity,
						inheritedValue, checkedBeingStringId, uncheckedBeingStringId);
				text = String.format(resources.getString(radioButtonDefaultTextId),
						summaryTextDefault);
			} else {
				String summaryTextCustom = Helper.getBooleanPreferenceSummary(activity,
						booleanWrapper.booleanValue(), checkedBeingStringId,
						uncheckedBeingStringId);
				text = String.format(resources.getString(radioButtonCustomTextId),
						summaryTextCustom);
			}
			return text;
		}

		private String getTextForIntegerValue(FragmentActivity activity,
				Resources resources, Integer inheritedIntegerWrapper,
				Integer integerWrapper, int defaultValueId, int minValueId,
				int maxValueId, int preferenceKeyId, int singularSummaryStringId,
				int pluralSummaryStringId, int radioButtonDefaultTextId,
				int radioButtonCustomTextId) {
			String summaryText;
			String valueWithMeasurementUnit;
			int value;
			int summaryTextId;
			if (integerWrapper == null) {
				if (inheritedIntegerWrapper == null) {
					int defaultValue = resources.getInteger(defaultValueId);
					int minValue = resources.getInteger(minValueId);
					int maxValue = resources.getInteger(maxValueId);
					value = Helper.getIntegerFromStringPreferenceValue(activity,
							preferenceKeyId, null, defaultValue, minValue, maxValue);
				} else {
					value = inheritedIntegerWrapper.intValue();
				}
				summaryTextId = radioButtonDefaultTextId;
			} else {
				value = integerWrapper.intValue();
				summaryTextId = radioButtonCustomTextId;
			}
			switch (preferenceKeyId) {
			case R.string.preference_key_ringtone_fade_in_time:
				valueWithMeasurementUnit = Helper.getTextForTimeInterval(activity, value);
				break;
			default:
				valueWithMeasurementUnit = Helper.getIntegerPreferenceSummary(activity,
						value, singularSummaryStringId, pluralSummaryStringId);
				break;
			}
			summaryText = String.format(resources.getString(summaryTextId),
					valueWithMeasurementUnit);
			return summaryText;
		}
	}
}
