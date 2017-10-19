package biz.advancedcalendar.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.ScheduledRemindersSupplier;
import biz.advancedcalendar.activities.ActivityMain;
import biz.advancedcalendar.activities.ActivityQuickReminder;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.greendao.ElapsedReminder;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.TaskEditMode;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.utils.Helper;
import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialog2.OnTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.OnMultipleTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.TimeAttribute;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FragmentViewScheduledReminders extends Fragment implements /* BundleAcceptor, */
OnTimeSetListener, OnMultipleTimeSetListener, ScheduledRemindersSupplier {
	final int ADD_REMINDER_ID = Menu.FIRST + 100 - 1;
	final int EDIT_REMINDER_REQUEST = 1;
	final int ADD_REMINDER_REQUEST = 2;
	private BroadcastReceiver mBroadcastReceiver;
	private ExpandableListView mExpandableListView;
	private FragmentViewScheduledRemindersAdapter mAdapter;
	private String mTimePickerDialogKey = "biz.advancedcalendar.fragments.FragmentViewScheduledReminders.TimePickerDialog";
	private String mPendingScheduledReminderForTimePickerDialogKey = "mPendingScheduledReminderForTimePickerDialogKey";
	private String mPendingScheduledReminderForDismissScheduledReminderDialogKey = "mPendingScheduledReminderForDismissScheduledReminderDialogKey";
	// variables to be saved in onSaveInstanceState()
	private ScheduledReminder mPendingScheduledReminderForTimePickerDialog;
	private ScheduledReminder mPendingScheduledReminderForDismissScheduledReminderDialog;
	private DateFormat mDateFormatTimeInstance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDateFormatTimeInstance = DateFormat.getTimeInstance();
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS)) {
					mPendingScheduledReminderForTimePickerDialog = null;
					mPendingScheduledReminderForDismissScheduledReminderDialog = null;
					if (isAdded()) {
						fillContent();
					}
				}
				if (action.equals(Intent.ACTION_TIME_CHANGED)) {
					if (isAdded()) {
						fillContent();
					}
				}
				if (action.equals(CommonConstants.ACTION_REMINDERS_UNSILENSED)) {
					if (isAdded()) {
						fillContent();
					}
				}
				if (action.equals(CommonConstants.ACTION_REMINDERS_SILENSED)) {
					if (isAdded()) {
						fillContent();
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_scheduled_reminders, container, false);
		mExpandableListView = (ExpandableListView) v
				.findViewById(R.id.fragment_scheduled_reminders_expandable_list_view);
		// mExpandableListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, final long id) {
				// DataProvider.runInTx(getActivity().getApplicationContext(),
				// new Runnable() {
				// @Override
				// public void run() {
				// ScheduledReminder reminder = DataProvider
				// .getScheduledReminder(getActivity()
				// .getApplicationContext(), id);
				// if (reminder != null && reminder.getReminder2(getActivity()) != null) {
				// Intent intent = new Intent(getActivity(),
				// ActivityEditTask.class);
				// intent.putExtra(CommonConstants.INTENT_EXTRA_ID,
				// reminder.getReminder2(getActivity()).getTask().getId());
				// intent.putExtra(
				// CommonConstants.INTENT_EXTRA_TAB,
				// CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS);
				// startActivity(intent);
				// }
				// }
				// });
				FragmentActivity context = getActivity();
				ScheduledReminder scheduledReminder = DataProvider.getScheduledReminder(
						null, context.getApplicationContext(), id);
				if (scheduledReminder != null) {
					Reminder reminder = scheduledReminder.getReminder(((Global) context
							.getApplicationContext()).getDaoSession());
					if (reminder != null) {
						Task task = reminder.getTask();
						TaskWithDependents taskWithDependents = DataProvider
								.getTaskWithDependents(null,
										context.getApplicationContext(), task);
						ActivityMain.launchTaskEditor2(context.getApplicationContext(),
								new TaskWithDependentsUiData(taskWithDependents),
								TaskEditMode.EDIT,
								CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS);
					}
				}
				return true;
			}
		});
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			FragmentManager fragmentManager = getFragmentManager();
			TimePickerDialogMultiple tpd = (TimePickerDialogMultiple) fragmentManager
					.findFragmentByTag(mTimePickerDialogKey);
			if (tpd != null) {
				tpd.setOnTimeSetListener(this);
			}
			mPendingScheduledReminderForTimePickerDialog = savedInstanceState
					.getParcelable(mPendingScheduledReminderForTimePickerDialogKey);
			mPendingScheduledReminderForDismissScheduledReminderDialog = savedInstanceState
					.getParcelable(mPendingScheduledReminderForDismissScheduledReminderDialogKey);
		}
		setHasOptionsMenu(true);
		FragmentActivity context = getActivity();
		mAdapter = new FragmentViewScheduledRemindersAdapter(context,
				android.R.layout.simple_list_item_1, FragmentViewScheduledReminders.this);
		mExpandableListView.setAdapter(mAdapter);
		int count = mAdapter.getGroupCount();
		for (int position = 0; position < count; position++) {
			mExpandableListView.expandGroup(position);
		}
		fillContent();
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_CHANGED);
		filter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS);
		filter.addAction(CommonConstants.ACTION_REMINDERS_SILENSED);
		filter.addAction(CommonConstants.ACTION_REMINDERS_UNSILENSED);
		LocalBroadcastManager.getInstance(context.getApplicationContext())
				.registerReceiver(mBroadcastReceiver, filter);
	}

	private void fillContent() {
		// fill warning the reminders are silenced until DateTime
		FragmentActivity activity = getActivity();
		TextView textView = (TextView) activity
				.findViewById(R.id.fragment_scheduled_reminders_textview_warning_alarms_are_silenced_until_datetime);
		// View delimiterView = getActivity().findViewById(R.id.delimiter_view);
		Resources resources = getResources();
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Calendar silenceUntilDatetime = Calendar.getInstance();
		silenceUntilDatetime
				.setTimeInMillis(PreferenceManager
						.getDefaultSharedPreferences(activity)
						.getLong(
								resources
										.getString(R.string.preference_key_silence_alarms_until_datetime),
								calendar.getTimeInMillis()));
		if (calendar.getTimeInMillis() < silenceUntilDatetime.getTimeInMillis()) {
			DateFormat timeFormat;
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			if (calendar.getTimeInMillis() < silenceUntilDatetime.getTimeInMillis()) {
				timeFormat = DateFormat.getDateTimeInstance();
			} else {
				timeFormat = DateFormat.getTimeInstance();
			}
			textView.setText(String.format(
					resources.getString(R.string.alarms_are_silenced_until_the_time),
					timeFormat.format(new Date(silenceUntilDatetime.getTimeInMillis()))));
			textView.setVisibility(View.VISIBLE);
			// delimiterView.setVisibility(View.VISIBLE);
		} else {
			textView.setVisibility(View.GONE);
			// delimiterView.setVisibility(View.GONE);
		}
		mAdapter.invalidateData();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SILENCE_ALARMS,
				0, getResources().getString(R.string.action_silence_alarms));
		menuItem.setIcon(R.drawable.ic_volume_off_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		//
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_UNSILENCE_ALARMS, 0,
				getResources().getString(R.string.action_unsilence_alarms));
		menuItem.setIcon(R.drawable.ic_volume_up_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		final FragmentActivity context = getActivity();
		Resources resources = getResources();
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_SILENCE_ALARMS:
			int milliseconds = Helper.getIntegerFromStringPreferenceValue(context,
					R.string.preference_key_silence_time_duration, null,
					resources.getInteger(R.integer.silence_time_duration_default_value),
					resources.getInteger(R.integer.silence_time_duration_min_value),
					resources.getInteger(R.integer.silence_time_duration_max_value));
			int hours = 0;
			int minutes = 0;
			if (milliseconds > 0) {
				minutes = milliseconds / (1000 * 60) % 60;
				hours = milliseconds / (1000 * 60 * 60);
				if (hours > 23) {
					hours = 23;
				}
			}
			ArrayList<TimeAttribute> timeAttributes = new ArrayList<TimeAttribute>();
			timeAttributes.add(new TimeAttribute(hours, minutes,
					R.string.radiobutton_text_reminder_snooze_mode_timespan, 0, true,
					false));
			Calendar calendar2 = Calendar.getInstance();
			calendar2.add(Calendar.HOUR_OF_DAY, hours);
			calendar2.add(Calendar.MINUTE, minutes);
			hours = calendar2.get(Calendar.HOUR_OF_DAY);
			minutes = calendar2.get(Calendar.MINUTE);
			boolean is24HourFormat = Helper.is24HourFormat(context);
			timeAttributes.add(new TimeAttribute(hours, minutes,
					R.string.radiobutton_text_reminder_snooze_mode_absolute_time, 1,
					is24HourFormat, true));
			Bundle bundle = new Bundle();
			bundle.putInt("callerId", CommonConstants.MENU_ID_SILENCE_ALARMS);
			TimePickerDialogMultiple tpd = TimePickerDialogMultiple.newInstance(
					FragmentViewScheduledReminders.this, bundle,
					R.string.time_picker_title_for_silence_time, timeAttributes, 0);
			tpd.show(getFragmentManager(), mTimePickerDialogKey);
			return true;
		case CommonConstants.MENU_ID_UNSILENCE_ALARMS:
			Toast.makeText(context,
					resources.getString(R.string.alarms_have_been_unsilenced),
					Toast.LENGTH_LONG).show();
			DataProvider.runInTx(null, context, new Runnable() {
				@Override
				public void run() {
					AlarmService.unsilenseAlarms(context, this);
				}
			});
			LocalBroadcastManager.getInstance(context).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			return true;
		case CommonConstants.MENU_ID_DISCARD_ELAPSED_REMINDERS_ALL:
			DataProvider.deleteElapsedReminders(null, context);
			LocalBroadcastManager.getInstance(context).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(mPendingScheduledReminderForTimePickerDialogKey,
				mPendingScheduledReminderForTimePickerDialog);
		outState.putParcelable(
				mPendingScheduledReminderForDismissScheduledReminderDialogKey,
				mPendingScheduledReminderForDismissScheduledReminderDialog);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mBroadcastReceiver);
		}
	}

	private class FragmentViewScheduledRemindersAdapter extends BaseExpandableListAdapter {
		// do not use this field directly. use getGroups method instead
		private ArrayList<ExpandListGroup> mGroups;
		private LayoutInflater mLayoutInflater;
		ScheduledRemindersSupplier mScheduledRemindersSupplier;

		public FragmentViewScheduledRemindersAdapter(Context context,
				int textViewResourceId, ScheduledRemindersSupplier dataSupplier) {
			mScheduledRemindersSupplier = dataSupplier;
			mLayoutInflater = getActivity().getLayoutInflater();
		}

		public synchronized void invalidateData() {
			mGroups = null;
			notifyDataSetChanged();
		}

		private synchronized ArrayList<ExpandListGroup> getGroups() {
			if (mGroups == null) {
				initGroups(mScheduledRemindersSupplier.getScheduledReminders());
			}
			return mGroups;
		}

		private void initGroups(List<ScheduledReminder> scheduledReminders) {
			Collections.sort(scheduledReminders,
					new AlarmService.ScheduledReminderComparatorByNextSnoozeDateTime());
			mGroups = new ArrayList<FragmentViewScheduledReminders.ExpandListGroup>();
			ExpandListGroup group = null;
			// ScheduledReminder previousScheduledReminder = null;
			Calendar previousCalendar = null;
			Calendar currentCalendar = null;
			DateFormat dateFormat = null;
			String dateString = null;
			int year, month, day;
			if (scheduledReminders.size() > 0) {
				// previousScheduledReminder = objects.get(0);
				dateFormat = DateFormat.getDateInstance();
				dateString = dateFormat.format(new Date(scheduledReminders.get(0)
						.getNextSnoozeDateTime()));
				group = new ExpandListGroup();
				group.Entities = new ArrayList<ScheduledReminder>();
				group.setName(dateString);
				previousCalendar = Calendar.getInstance();
				previousCalendar.setTimeInMillis(scheduledReminders.get(0)
						.getNextSnoozeDateTime());
				year = previousCalendar.get(Calendar.YEAR);
				month = previousCalendar.get(Calendar.MONTH);
				day = previousCalendar.get(Calendar.DAY_OF_MONTH);
				previousCalendar.clear();
				previousCalendar.set(year, month, day);
				currentCalendar = Calendar.getInstance();
				mGroups.add(group);
			}
			for (ScheduledReminder currentScheduledReminder : scheduledReminders) {
				currentCalendar.setTimeInMillis(currentScheduledReminder
						.getNextSnoozeDateTime());
				year = currentCalendar.get(Calendar.YEAR);
				month = currentCalendar.get(Calendar.MONTH);
				day = currentCalendar.get(Calendar.DAY_OF_MONTH);
				currentCalendar.clear();
				currentCalendar.set(year, month, day);
				Date currentDate = new Date(currentCalendar.getTimeInMillis());
				if (currentCalendar.getTimeInMillis() != previousCalendar
						.getTimeInMillis()) {
					dateString = dateFormat.format(currentDate);
					group = new ExpandListGroup();
					group.Entities = new ArrayList<ScheduledReminder>();
					group.setName(dateString);
					mGroups.add(group);
				}
				ScheduledReminder expandListChild = currentScheduledReminder;
				group.Entities.add(expandListChild);
				// previousScheduledReminder = currentScheduledReminder;
				previousCalendar.setTimeInMillis(currentCalendar.getTimeInMillis());
			}
		}

		// public void addItem(ScheduledReminder item, ExpandListGroup group) {
		// if (!mGroups.contains(group)) {
		// mGroups.add(group);
		// }
		// int index = mGroups.indexOf(group);
		// ArrayList<ScheduledReminder> ch = mGroups.get(index).getItems();
		// ch.add(item);
		// mGroups.get(index).setItems(ch);
		// }
		@Override
		public ScheduledReminder getChild(int groupPosition, int childPosition) {
			ArrayList<ScheduledReminder> chList = getGroups().get(groupPosition)
					.getItems();
			return chList.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0L | groupPosition << 12 | childPosition;
		}

		@Override
		public long getCombinedChildId(long groupId, long childId) {
			return groupId << 32 | childId << 1 | 1;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			final LinearLayout linearLayout;
			if (convertView == null) {
				linearLayout = (LinearLayout) mLayoutInflater.inflate(
						R.layout.fragment_view_scheduled_reminders_listitem, parent,
						false);
			} else {
				linearLayout = (LinearLayout) convertView;
			}
			final ScheduledReminder scheduledReminder = getChild(groupPosition,
					childPosition);
			LinearLayout linearLayoutButtonsResnoozeDismiss = (LinearLayout) linearLayout
					.findViewById(R.id.fragment_view_scheduled_reminders_listitem_linearlayout_buttons_resnooze_dismiss);
			Integer[] positions = new Integer[] {groupPosition, childPosition};
			if (scheduledReminder.getSnoozeCount() == 0
					&& scheduledReminder.getReminderId() != null) {
				linearLayoutButtonsResnoozeDismiss.setVisibility(View.GONE);
			} else {
				linearLayoutButtonsResnoozeDismiss.setVisibility(View.VISIBLE);
				Button buttonDismiss = (Button) linearLayout
						.findViewById(R.id.fragment_view_scheduled_reminders_listitem_button_dismiss);
				Button buttonResnooze = (Button) linearLayout
						.findViewById(R.id.fragment_view_scheduled_reminders_listitem_button_resnooze);
				// setup delete button listener
				buttonDismiss.setTag(positions);
				buttonDismiss.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer[] positions = (Integer[]) v.getTag();
						mPendingScheduledReminderForDismissScheduledReminderDialog = getChild(
								positions[0], positions[1]);
						boolean showAlertDialog = Helper
								.getBooleanPreferenceValue(
										getActivity(),
										getResources()
												.getString(
														R.string.preference_key_show_alert_dialog_on_dismiss_scheduled_reminder_button_press),
										true);
						if (showAlertDialog) {
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									getActivity());
							// final DBHelper dbHelper = new DBHelper(this);
							// final List<MeasurementDevice> totalDeviceList =
							// dbHelper.getAllDevices();
							String[] items = new String[] {getResources().getString(
									R.string.action_dont_bother_anymore)};
							final boolean[] selectedItems = new boolean[] {false};
							alertDialogBuilder
									.setTitle(
											getResources()
													.getString(
															R.string.action_dismiss_scheduled_reminder_confirmation))
									.setMultiChoiceItems(
											items,
											selectedItems,
											new DialogInterface.OnMultiChoiceClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int indexSelected,
														boolean isChecked) {
													selectedItems[0] = isChecked;
												}
											})
									// .setMessage(
									// getResources().getString(
									// R.string.action_delete_task_confirmation))
									.setCancelable(true)
									// Set the action buttons
									.setPositiveButton(R.string.alert_dialog_ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog, int id) {
													dismissScheduledReminder(mPendingScheduledReminderForDismissScheduledReminderDialog);
													mPendingScheduledReminderForDismissScheduledReminderDialog = null;
													if (selectedItems[0]) {
														PreferenceManager
																.getDefaultSharedPreferences(
																		getActivity())
																.edit()
																.putBoolean(
																		getResources()
																				.getString(
																						R.string.preference_key_show_alert_dialog_on_dismiss_scheduled_reminder_button_press),
																		false).commit();
													}
												}
											})
									.setNegativeButton(R.string.alert_dialog_cancel, null);
							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder.create();
							// show it
							alertDialog.show();
						} else {
							dismissScheduledReminder(mPendingScheduledReminderForDismissScheduledReminderDialog);
							mPendingScheduledReminderForDismissScheduledReminderDialog = null;
						}
					}

					private void dismissScheduledReminder(
							final ScheduledReminder scheduledReminder) {
						DataProvider.runInTx(null, getActivity(), new Runnable() {
							@Override
							public void run() {
								if (scheduledReminder.getSnoozeCount() > 0
										&& scheduledReminder.getElapsedReminder() == null) {
									DataProvider.insertOrReplaceElapsedReminder(
											null,
											getActivity(),
											new ElapsedReminder(
													null,
													scheduledReminder.getReminderId(),
													null,
													scheduledReminder
															.getAssignedRemindAtDateTime(),
													scheduledReminder
															.getActualLastAlarmedDateTime(),
													scheduledReminder.getSnoozeCount() - 1,
													scheduledReminder.getText(),
													scheduledReminder.getIsAlarm(),
													false,
													scheduledReminder.getEnabled(),
													scheduledReminder.getRingtone(),
													scheduledReminder
															.getRingtoneFadeInTime(),
													scheduledReminder.getPlayingTime(),
													scheduledReminder
															.getAutomaticSnoozeDuration(),
													scheduledReminder
															.getAutomaticSnoozesMaxCount(),
													scheduledReminder.getVibrate(),
													scheduledReminder.getVibratePattern(),
													scheduledReminder.getLed(),
													scheduledReminder.getLedPattern(),
													scheduledReminder.getLedColor()));
								}
								DataProvider.deleteScheduledReminder(null, getActivity(),
										scheduledReminder.getId());
								AlarmService.cancelScheduledAlarm(getActivity(),
										scheduledReminder.getId());
							}
						});
						// notifyDataSetChanged();
						LocalBroadcastManager
								.getInstance(getActivity().getApplicationContext())
								.sendBroadcast(
										new Intent(
												CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
					}
				});
				buttonResnooze.setTag(positions);
				buttonResnooze.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer[] positions = (Integer[]) v.getTag();
						mPendingScheduledReminderForTimePickerDialog = getChild(
								positions[0], positions[1]);
						Calendar calendar2 = Calendar.getInstance();
						calendar2
								.setTimeInMillis(mPendingScheduledReminderForTimePickerDialog
										.getNextSnoozeDateTime());
						int hours = calendar2.get(Calendar.HOUR_OF_DAY);
						int minutes = calendar2.get(Calendar.MINUTE);
						ArrayList<TimeAttribute> timeAttributes = new ArrayList<TimeAttribute>();
						boolean is24HourFormat = Helper.is24HourFormat(getActivity());
						timeAttributes
								.add(new TimeAttribute(
										hours,
										minutes,
										R.string.radiobutton_text_reminder_snooze_mode_absolute_time,
										1, is24HourFormat, true));
						hours = 0;
						minutes = 0;
						long timeSpan = calendar2.getTimeInMillis()
								- Calendar.getInstance().getTimeInMillis();
						if (timeSpan > 0) {
							hours = (int) (timeSpan / 1000 / 60 / 60);
							if (hours > 23) {
								hours = 23;
							}
							minutes = (int) (timeSpan / 1000 / 60 % 60);
						}
						timeAttributes.add(new TimeAttribute(hours, minutes,
								R.string.radiobutton_text_reminder_snooze_mode_timespan,
								0, true, false));
						Bundle bundle = new Bundle();
						bundle.putInt(
								"callerId",
								R.id.fragment_view_scheduled_reminders_listitem_button_resnooze);
						TimePickerDialogMultiple tpd = TimePickerDialogMultiple
								.newInstance(FragmentViewScheduledReminders.this, bundle,
										R.string.time_picker_title_for_snooze_mode,
										timeAttributes, 0);
						tpd.show(getFragmentManager(), mTimePickerDialogKey);
					}
				});
			}
			CheckBox checkBox = (CheckBox) linearLayout
					.findViewById(R.id.fragment_view_scheduled_reminders_listitem_checkbox);
			checkBox.setTag(positions);
			Reminder reminder = scheduledReminder.getReminder(((Global) getActivity()
					.getApplicationContext()).getDaoSession());
			if (reminder == null ? scheduledReminder.getIsAlarm() : reminder.getIsAlarm()) {
				checkBox.setButtonDrawable(R.drawable.alarm_clock_with_checkbox);
			} else {
				checkBox.setButtonDrawable(R.drawable.bell_with_checkbox);
			}
			checkBox.setChecked(scheduledReminder.getEnabled());
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						final boolean isChecked) {
					Integer[] positions = (Integer[]) buttonView.getTag();
					final ScheduledReminder scheduledReminder = getChild(positions[0],
							positions[1]);
					DataProvider.runInTx(null, getActivity(), new Runnable() {
						@Override
						public void run() {
							ScheduledReminder scheduledReminder2 = DataProvider
									.getScheduledReminder(null, getActivity(),
											scheduledReminder.getId());
							scheduledReminder2.setEnabled(isChecked);
							DataProvider.insertOrReplaceScheduledReminder(null,
									getActivity(), scheduledReminder2);
							// AlarmService.setAlarmForReminder(getActivity(),
							// scheduledReminder2,
							// this);
						}
					});
					notifyDataSetChanged();
					LocalBroadcastManager
							.getInstance(getActivity())
							.sendBroadcast(
									new Intent(
											CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
				}
			});
			TableLayout tableLayout = (TableLayout) linearLayout
					.findViewById(R.id.fragment_view_scheduled_reminders_listitem_tablelayout);
			tableLayout.setTag(positions);
			tableLayout.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Integer[] positions = (Integer[]) v.getTag();
					final ScheduledReminder scheduledReminder = getChild(positions[0],
							positions[1]);
					FragmentActivity context = getActivity();
					Reminder reminder = scheduledReminder.getReminder(((Global) context
							.getApplicationContext()).getDaoSession());
					if (reminder != null) {
						// Intent intent = new Intent(getActivity(),
						// ActivityEditTask.class);
						// intent.putExtra(CommonConstants.INTENT_EXTRA_ID,
						// scheduledReminder.getReminder().getTask().getId());
						// intent.putExtra(CommonConstants.INTENT_EXTRA_TAB,
						// CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS);
						// startActivity(intent);
						ActivityMain.launchTaskViewerOrEditor(context, reminder.getTask()
								.getId(),
								CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS);
					} else if (scheduledReminder.getIsQuickReminder()) {
						Intent intent = new Intent(context, ActivityQuickReminder.class);
						intent.putExtra(CommonConstants.INTENT_EXTRA_REMINDER,
								scheduledReminder);
						startActivity(intent);
					}
					return true;
					// return false;
				}
			});
			setupText(scheduledReminder, tableLayout);
			return linearLayout;
		}

		private void setupText(final ScheduledReminder scheduledReminder,
				TableLayout tableLayout) {
			FragmentActivity activity = getActivity();
			Reminder reminder = scheduledReminder.getReminder(((Global) activity
					.getApplicationContext()).getDaoSession());
			tableLayout.removeAllViews();
			TableRow tableRow;
			TextView textViewHeader;
			TextView textViewValue;
			String text;
			// DateFormat dateFormat;
			// setup reminder text
			tableRow = (TableRow) mLayoutInflater.inflate(
					R.layout.tablerow_single_textview, tableLayout, false);
			textViewValue = (TextView) tableRow
					.findViewById(R.id.tablerow_single_textview_textview);
			text = reminder == null ? scheduledReminder.getText() : reminder.getText();
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
			// setup alarm time
			tableRow = (TableRow) LayoutInflater.from(activity).inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			boolean isAlarm = scheduledReminder.getIsAlarm();
			boolean enabled = scheduledReminder.getEnabled();
			int snoozeCount = scheduledReminder.getSnoozeCount();
			// scheduledReminder.refresh();
			snoozeCount = scheduledReminder.getSnoozeCount();
			String willAlarmAt;
			String willAlarmSnoozeAt;
			if (scheduledReminder.getNextSnoozeDateTime() < PreferenceManager
					.getDefaultSharedPreferences(activity)
					.getLong(
							getResources()
									.getString(
											R.string.preference_key_silence_alarms_until_datetime),
							scheduledReminder.getNextSnoozeDateTime())) {
				// setup alarm time
				willAlarmAt = getResources().getString(
						R.string.fragment_view_reminders_would_alarm_at);
				willAlarmSnoozeAt = String.format(
						getResources().getString(
								R.string.fragment_view_reminders_would_alarm_snooze_at),
						snoozeCount);
				tableLayout.addView(tableRow);
				// fill warning the reminder is in silence time interval
				TableRow tableRow2 = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.tablerow_single_textview, tableLayout, false);
				textViewValue = (TextView) tableRow2
						.findViewById(R.id.tablerow_single_textview_textview);
				textViewValue.setTypeface(null, Typeface.ITALIC);
				textViewValue
						.setText(getResources()
								.getString(
										R.string.fragment_view_task_part_reminders_header_textview_warning_the_reminder_is_in_silence_time_interval));
				tableLayout.addView(tableRow2);
			} else {
				willAlarmAt = getResources().getString(
						R.string.fragment_view_reminders_will_alarm_at);
				willAlarmSnoozeAt = String.format(
						getResources().getString(
								R.string.fragment_view_reminders_will_alarm_snooze_at),
						snoozeCount);
				tableLayout.addView(tableRow);
			}
			((TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview))
					.setText(
					//
					isAlarm ?
					//
					enabled ?
					//
					snoozeCount == 0 ?
					//
					willAlarmAt
							:
							//
							willAlarmSnoozeAt
							:
							//
							snoozeCount == 0 ?
							//
							getResources().getString(
									R.string.fragment_view_reminders_will_not_alarm_at)
									:
									//
									String.format(
											getResources()
													.getString(
															R.string.fragment_view_reminders_will_not_alarm_snooze_at),
											snoozeCount)
							:
							//
							enabled ?
							//
							snoozeCount == 0 ?
							//
							getResources().getString(
									R.string.fragment_view_reminders_will_notificate_at)
									:
									//
									String.format(
											getResources()
													.getString(
															R.string.fragment_view_reminders_will_notificate_snooze_at),
											snoozeCount)
									:
									//
									snoozeCount == 0 ?
									//
									getResources()
											.getString(
													R.string.fragment_view_reminders_will_not_notificate_at)
											:
											//
											String.format(
													getResources()
															.getString(
																	R.string.fragment_view_reminders_will_not_notificate_snooze_at),
													snoozeCount)
					//
					);
			((TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview))
					.setText(mDateFormatTimeInstance.format(new Date(scheduledReminder
							.getNextSnoozeDateTime())));
			// setup scheduled time
			tableRow = (TableRow) LayoutInflater.from(activity).inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader.setText(getResources().getString(
					R.string.fragment_view_reminders_reminder_scheduled_time));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			text = mDateFormatTimeInstance.format(new Date(scheduledReminder
					.getAssignedRemindAtDateTime()));
			if (reminder != null
					&& reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
							.getValue()
					&& reminder.getReminderTimeModeValue() != ReminderTimeMode.AFTER_NOW
							.getValue()) {
				long absRemindOffset = Math.abs(reminder.getReminderDateTime());
				Integer days = (int) (absRemindOffset / (1000L * 60 * 60 * 24));
				Integer hours = (int) (absRemindOffset / (1000L * 60 * 60) % 24);
				Integer minutes = (int) (absRemindOffset / (1000L * 60) % 60);
				if (days == 0 && hours == 0 && minutes == 0) {
					text += ", "
							+ getResources()
									.getString(
											R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_at_task_start);
				} else {
					text += ", "
							+ (days == 0 ? ""
									: days
											+ " "
											+ getResources()
													.getString(
															R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_days)
											+ " ")
							+ (hours == 0 ? ""
									: hours
											+ " "
											+ getResources()
													.getString(
															R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_hours)
											+ " ")
							+ (minutes == 0 ? ""
									: minutes
											+ " "
											+ getResources()
													.getString(
															R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_minutes)
											+ " ")
							+ (reminder.getReminderDateTime() == 0 ? getResources()
									.getString(
											R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_at_task_start)
									: reminder.getReminderTimeModeValue() == ReminderTimeMode.TIME_BEFORE_EVENT
											.getValue() ? getResources()
											.getString(
													R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_before_task_start)
											: getResources()
													.getString(
															R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_after_task_start));
				}
			}
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
			// fill attached task text row
			if (reminder != null) {
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
				textViewHeader = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textViewHeader.setText(getResources().getString(
						R.string.fragment_view_reminders_attached_to_task));
				textViewValue = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				Task task = DataProvider.getTask(null, activity, reminder.getTaskId(),
						false);
				text = task.getName();
				textViewValue.setText(text);
				int backgroundColor = task.getColor2(activity);
				int textColor;
				if (Helper.getContrastYIQ(backgroundColor)) {
					textColor = ContextCompat.getColor(activity,
							R.color.task_view_text_synchronized_dark);
				} else {
					textColor = ContextCompat.getColor(activity,
							R.color.task_view_text_synchronized_light);
				}
				textViewValue.setBackgroundColor(backgroundColor);
				textViewValue.setTextColor(textColor);
				tableLayout.addView(tableRow);
			}
			// fill Ringtone
			tableRow = (TableRow) LayoutInflater.from(activity).inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(getResources()
							.getString(
									R.string.fragment_view_task_part_reminders_header_textview_reminder_ringtone));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			text = scheduledReminder.getRingtoneTitle(activity,
					((Global) activity.getApplicationContext()).getDaoSession());
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
			// // setup reminder snooze count
			// if (reminder.getSnoozeCount() > 0) {
			// tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(
			// R.layout.fragment_view_task_part_main_table_row, null);
			// textViewHeader = (TextView) tableRow
			// .findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			// textViewHeader.setText(getResources().getString(
			// R.string.fragment_view_reminders_snooze_count));
			// textViewValue = (TextView) tableRow
			// .findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			// textViewValue.setText(reminder.getSnoozeCount() + "");
			// tableLayout.addView(tableRow);
			// }
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			ArrayList<ScheduledReminder> chList = getGroups().get(groupPosition)
					.getItems();
			return chList.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return getGroups().get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return getGroups().size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getCombinedGroupId(long groupId) {
			return (groupId & 0x7FFFFFFF) << 32;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isLastChild, View view,
				ViewGroup parent) {
			ExpandListGroup group = (ExpandListGroup) getGroup(groupPosition);
			if (view == null) {
				view = mLayoutInflater.inflate(R.layout.expandlist_group_item, parent,
						false);
			}
			TextView tv = (TextView) view.findViewById(R.id.tvGroup);
			tv.setText(group.getName());
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}
	}

	public class ExpandListGroup {
		private String Name;
		private ArrayList<ScheduledReminder> Entities;

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public ArrayList<ScheduledReminder> getItems() {
			return Entities;
		}

		public void setItems(ArrayList<ScheduledReminder> Entities) {
			this.Entities = Entities;
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int callerId, int hours, int minutes,
			boolean isTimeSpan) {
		Resources resources = getResources();
		final FragmentActivity activity = getActivity();
		switch (callerId) {
		case CommonConstants.MENU_ID_SILENCE_ALARMS:
			String text;
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			if (isTimeSpan) {
				calendar.add(Calendar.HOUR_OF_DAY, hours);
				calendar.add(Calendar.MINUTE, minutes);
				String textTime = Helper.getTextForTimeInterval(activity, 0, hours,
						minutes, 0);
				text = String.format(resources
						.getString(R.string.alarms_are_silenced_for_time_interval),
						textTime);
			} else {
				Calendar calendar2 = (Calendar) calendar.clone();
				calendar2.set(Calendar.HOUR_OF_DAY, hours);
				calendar2.set(Calendar.MINUTE, minutes);
				calendar2.set(Calendar.SECOND, 0);
				DateFormat timeFormat;
				if (calendar2.getTimeInMillis() < calendar.getTimeInMillis()) {
					calendar2.add(Calendar.DAY_OF_YEAR, 1);
					timeFormat = DateFormat.getDateTimeInstance();
				} else {
					timeFormat = DateFormat.getTimeInstance();
				}
				calendar = calendar2;
				text = String.format(
						resources.getString(R.string.alarms_are_silenced_until_the_time),
						timeFormat.format(new Date(calendar2.getTimeInMillis())));
			}
			final Calendar calendar3 = calendar;
			Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			DataProvider.runInTx(null, activity, new Runnable() {
				@Override
				public void run() {
					AlarmService.silenseAlarms(activity, calendar3.getTimeInMillis(),
							this);
				}
			});
			mPendingScheduledReminderForTimePickerDialog = null;
			LocalBroadcastManager.getInstance(activity).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			break;
		case R.id.fragment_view_scheduled_reminders_listitem_button_resnooze:
			if (mPendingScheduledReminderForTimePickerDialog != null) {
				mPendingScheduledReminderForTimePickerDialog.setEnabled(true);
				calendar = Calendar.getInstance();
				calendar.set(Calendar.MILLISECOND, 0);
				if (isTimeSpan) {
					calendar.add(Calendar.HOUR_OF_DAY, hours);
					calendar.add(Calendar.MINUTE, minutes);
					String textTime = Helper.getTextForTimeInterval(activity, 0, hours,
							minutes, 0);
					text = String
							.format(resources
									.getString(R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_timespan),
									textTime);
				} else {
					Calendar calendar2 = (Calendar) calendar.clone();
					calendar2.set(Calendar.HOUR_OF_DAY, hours);
					calendar2.set(Calendar.MINUTE, minutes);
					calendar2.set(Calendar.SECOND, 0);
					DateFormat timeFormat = DateFormat.getTimeInstance();
					int textId;
					if (calendar2.getTimeInMillis() < calendar.getTimeInMillis()) {
						calendar2.add(Calendar.DAY_OF_YEAR, 1);
						textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_tomorrow;
					} else {
						textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_today;
					}
					calendar = calendar2;
					text = String.format(resources.getString(textId),
							timeFormat.format(new Date(calendar2.getTimeInMillis())));
				}
				Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
				mPendingScheduledReminderForTimePickerDialog
						.setNextSnoozeDateTime(calendar.getTimeInMillis());
				// mPendingScheduledReminderForTimePickerDialog.setActualLastAlarmedDateTime(null);
				mPendingScheduledReminderForTimePickerDialog
						.setStateValue(ScheduledReminder.State.SCHEDULED.getValue());
				DataProvider.runInTx(null, activity, new Runnable() {
					@Override
					public void run() {
						DataProvider.insertOrReplaceScheduledReminder(null, activity,
								mPendingScheduledReminderForTimePickerDialog);
						AlarmService.setAlarmForReminder(activity,
								mPendingScheduledReminderForTimePickerDialog.getId(),
								mPendingScheduledReminderForTimePickerDialog
										.getNextSnoozeDateTime(), this);
					}
				});
				mPendingScheduledReminderForTimePickerDialog = null;
				LocalBroadcastManager.getInstance(activity).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			} else {
				Toast.makeText(
						activity,
						getString(R.string.fragment_view_scheduled_reminders_toast_the_reminder_does_not_exist_anymore),
						Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public List<ScheduledReminder> getScheduledReminders() {
		List<ScheduledReminder> reminderList = DataProvider.getScheduledReminders(null,
				getActivity().getApplicationContext(), null,
				new int[] {ScheduledReminder.State.SCHEDULED.getValue()});
		return reminderList;
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, Bundle bundle,
			ArrayList<TimeAttribute> timeAttributes, int ordinalNumber) {
		Resources resources = getResources();
		final FragmentActivity activity = getActivity();
		boolean isTimeSpan = ordinalNumber == 0;
		TimeAttribute timeAttribute = timeAttributes.get(ordinalNumber);
		int hours = timeAttribute.getHours();
		int minutes = timeAttribute.getMinutes();
		int callerId = bundle.getInt("callerId");
		switch (callerId) {
		case CommonConstants.MENU_ID_SILENCE_ALARMS:
			String text;
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			if (isTimeSpan) {
				calendar.add(Calendar.HOUR_OF_DAY, hours);
				calendar.add(Calendar.MINUTE, minutes);
				String textTime = Helper.getTextForTimeInterval(activity, 0, hours,
						minutes, 0);
				text = String.format(resources
						.getString(R.string.alarms_are_silenced_for_time_interval),
						textTime);
			} else {
				Calendar calendar2 = (Calendar) calendar.clone();
				calendar2.set(Calendar.HOUR_OF_DAY, hours);
				calendar2.set(Calendar.MINUTE, minutes);
				DateFormat timeFormat;
				if (calendar2.getTimeInMillis() < calendar.getTimeInMillis()) {
					calendar2.add(Calendar.DAY_OF_YEAR, 1);
					timeFormat = DateFormat.getDateTimeInstance();
				} else {
					timeFormat = DateFormat.getTimeInstance();
				}
				calendar = calendar2;
				text = String.format(
						resources.getString(R.string.alarms_are_silenced_until_the_time),
						timeFormat.format(new Date(calendar2.getTimeInMillis())));
			}
			final Calendar calendar3 = calendar;
			Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			DataProvider.runInTx(null, activity, new Runnable() {
				@Override
				public void run() {
					AlarmService.silenseAlarms(activity, calendar3.getTimeInMillis(),
							this);
				}
			});
			mPendingScheduledReminderForTimePickerDialog = null;
			LocalBroadcastManager.getInstance(activity).sendBroadcast(
					new Intent(CommonConstants.ACTION_REMINDERS_SILENSED));
			break;
		case R.id.fragment_view_scheduled_reminders_listitem_button_resnooze:
			if (mPendingScheduledReminderForTimePickerDialog != null) {
				mPendingScheduledReminderForTimePickerDialog.setEnabled(true);
				calendar = Calendar.getInstance();
				calendar.set(Calendar.MILLISECOND, 0);
				if (isTimeSpan) {
					calendar.add(Calendar.HOUR_OF_DAY, hours);
					calendar.add(Calendar.MINUTE, minutes);
					String textTime = Helper.getTextForTimeInterval(activity, 0, hours,
							minutes, 0);
					text = String
							.format(resources
									.getString(R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_timespan),
									textTime);
				} else {
					Calendar calendar2 = (Calendar) calendar.clone();
					calendar2.set(Calendar.HOUR_OF_DAY, hours);
					calendar2.set(Calendar.MINUTE, minutes);
					calendar2.set(Calendar.SECOND, 0);
					DateFormat timeFormat = DateFormat.getTimeInstance();
					int textId;
					if (calendar2.getTimeInMillis() < calendar.getTimeInMillis()) {
						calendar2.add(Calendar.DAY_OF_YEAR, 1);
						textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_tomorrow;
					} else {
						textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_today;
					}
					calendar = calendar2;
					text = String.format(resources.getString(textId),
							timeFormat.format(new Date(calendar2.getTimeInMillis())));
				}
				Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
				mPendingScheduledReminderForTimePickerDialog
						.setNextSnoozeDateTime(calendar.getTimeInMillis());
				// mPendingScheduledReminderForTimePickerDialog.setActualLastAlarmedDateTime(null);
				mPendingScheduledReminderForTimePickerDialog
						.setStateValue(ScheduledReminder.State.SCHEDULED.getValue());
				DataProvider.runInTx(null, activity, new Runnable() {
					@Override
					public void run() {
						DataProvider.insertOrReplaceScheduledReminder(null, activity,
								mPendingScheduledReminderForTimePickerDialog);
						AlarmService.setAlarmForReminder(activity,
								mPendingScheduledReminderForTimePickerDialog.getId(),
								mPendingScheduledReminderForTimePickerDialog
										.getNextSnoozeDateTime(), this);
					}
				});
				mPendingScheduledReminderForTimePickerDialog = null;
				LocalBroadcastManager.getInstance(activity).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			} else {
				Toast.makeText(
						activity,
						getString(R.string.fragment_view_scheduled_reminders_toast_the_reminder_does_not_exist_anymore),
						Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean isTimeConsistent(ArrayList<TimeAttribute> timeAttributes, Bundle bundle) {
		return true;
	}
}
