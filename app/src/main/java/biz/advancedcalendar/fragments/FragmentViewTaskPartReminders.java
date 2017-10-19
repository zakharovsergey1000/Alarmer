package biz.advancedcalendar.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.db.TaskUiData;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CheckableLinearLayout;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class FragmentViewTaskPartReminders extends Fragment implements OnClickListener {
	final int ADD_REMINDER_ID = Menu.FIRST + 100 - 1;
	final int EDIT_REMINDER_REQUEST = 1;
	final int ADD_REMINDER_REQUEST = 2;
	ArrayAdapterReminder mArrayAdapter;
	ListView mListView;
	private TaskWithDependentsUiDataHolder taskWithDependentsUiDataHolder;
	private TaskWithDependentsUiData taskWithDependentsUiData;

	private class Holder {
		// View listItemView;
		int position;

		public Holder(View listItemView, int position) {
			super();
			// this.listItemView = listItemView;
			this.position = position;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We could lazily load the list in onResume() but lets load it
		// here to minimize latency when we will actually view this fragment
		// This is because the fragment could be attached to the activity but
		// not viewed at the time
		// TaskWithDependents entityWithDependencies = ((TaskWithDependentsHolder)
		// getActivity())
		// .getTaskWithDependents();
		// if (entityWithDependencies.reminders == null) {
		// entityWithDependencies.reminders = DataProvider.getReminderListOfTask(
		// getActivity(), entityWithDependencies.task.getId(), true, true);
		// DataProvider.refreshAndDetach(getActivity(),
		// entityWithDependencies.reminders);
		// }
		// Restore state here
		// Log.i(CommonConstants.DEBUG_TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(CommonConstants.DEBUG_TAG, "onCreateView");
		View v = inflater.inflate(R.layout.fragment_view_task_part_reminders, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		setHasOptionsMenu(true);
		taskWithDependentsUiDataHolder = (TaskWithDependentsUiDataHolder) getActivity();
		taskWithDependentsUiData = taskWithDependentsUiDataHolder
				.getTaskWithDependentsUiData();
		mListView = (ListView) getActivity().findViewById(
				R.id.fragment_edit_task_part_reminders_listview_reminders);
		mArrayAdapter = new ArrayAdapterReminder(getActivity(),
				R.layout.fragment_view_task_part_reminders_list_item2,
				taskWithDependentsUiData.RemindersUiData,
				taskWithDependentsUiData.TaskUiData);
		mListView.setAdapter(mArrayAdapter);
	}

	@Override
	public void onClick(View v) {
		Bundle b;
		Holder holder = (Holder) v.getTag();
		Reminder reminder = mArrayAdapter.getItem(holder.position);
		switch (v.getId()) {
		case R.id.button_reminder_date:
			FragmentDatePicker fragmentDatePicker = new FragmentDatePicker();
			b = new Bundle();
			b.putInt(CommonConstants.CALLER_ID, R.id.button_reminder_date);
			b.putString(CommonConstants.TAG, getTag());
			Calendar calendar = Calendar.getInstance();
			if (reminder.getReminderTimeModeValue() == ReminderTimeMode.ABSOLUTE_TIME
					.getValue()) {
				calendar.setTimeInMillis(reminder.getReminderDateTime());
			}
			// Use the preset date in the picker
			b.putInt(CommonConstants.YEAR, calendar.get(Calendar.YEAR));
			b.putInt(CommonConstants.MONTH, calendar.get(Calendar.MONTH));
			b.putInt(CommonConstants.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
			fragmentDatePicker.setArguments(b);
			fragmentDatePicker.show(getFragmentManager(), "FragmentDatePicker");
			break;
		case R.id.button_reminder_time:
			FragmentTimePicker fragmentTimePicker = new FragmentTimePicker();
			b = new Bundle();
			b.putInt(CommonConstants.CALLER_ID, R.id.button_reminder_time);
			b.putString(CommonConstants.TAG, getTag());
			calendar = Calendar.getInstance();
			if (reminder.getReminderTimeModeValue() == ReminderTimeMode.ABSOLUTE_TIME
					.getValue()) {
				calendar.setTimeInMillis(reminder.getReminderDateTime());
			}			// Use the preset time in the picker
			b.putInt(CommonConstants.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
			b.putInt(CommonConstants.MINUTE, calendar.get(Calendar.MINUTE));
			fragmentTimePicker.setArguments(b);
			fragmentTimePicker.show(getFragmentManager(), "FragmentDatePicker");
			break;
		// case R.id.fragment_task_viewing_reminders_list_item_button_delete:
		// mArrayAdapter.showDialog(holder.position);
		default:
			break;
		}
	}

	class ArrayAdapterReminder extends ArrayAdapter<ReminderUiData> {
		List<ReminderUiData> mReminders;
		private int mListRowPosition;
		TaskUiData mTask;

		// private AlertDialog mDialog;
		public ArrayAdapterReminder(Context context, int textViewResourceId,
				List<ReminderUiData> reminders, TaskUiData task) {
			super(context, textViewResourceId, reminders);
			mReminders = reminders;
			mTask = task;
			// Create AlertDialog here
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setMessage(
					R.string.fragment_task_viewing_reminders_list_item_delete_warning)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									// Use mListRowPosition for clicked list row...
									// User clicked OK button
									TaskWithDependentsUiData entityWithDependents = ((TaskWithDependentsUiDataHolder) getActivity())
											.getTaskWithDependentsUiData();
									entityWithDependents.RemindersUiData
											.remove(mListRowPosition);
									mArrayAdapter.notifyDataSetChanged();
									dialog.dismiss();
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									// User cancelled the dialog
								}
							});
			// Create the AlertDialog object
			// mDialog = builder.create();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CheckableLinearLayout checkableLinearLayout;
			FragmentActivity context = getActivity();
			if (convertView == null) {
				checkableLinearLayout = (CheckableLinearLayout) context
						.getLayoutInflater().inflate(
								R.layout.fragment_view_task_part_reminders_list_item2,
								parent, false);
			} else {
				checkableLinearLayout = (CheckableLinearLayout) convertView;
			}
			TableLayout tableLayout = (TableLayout) checkableLinearLayout
					.findViewById(R.id.fragment_view_task_part_reminders_list_item_tablelayout);
			tableLayout.removeAllViews();
			Reminder reminder = getItem(position);
			Holder holder = new Holder(checkableLinearLayout, position);
			checkableLinearLayout.setTag(holder);
			LayoutInflater layoutInflater = LayoutInflater.from(context);
			TableRow tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			// fill task text row
			TextView textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			Resources resources = getResources();
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_state));
			TextView textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			textViewValue
					.setText(reminder.getEnabled() ? resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_state_enabled)
							: resources
									.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_state_disabled));
			tableLayout.addView(tableRow);
			// setup reminder text
			tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_text));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			textViewValue.setText(reminder.getText());
			tableLayout.addView(tableRow);
			// setup reminder type
			tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_alarm_mode));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			textViewValue
					.setText(reminder.getIsAlarm() ? resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_type_alarm)
							: resources
									.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_type_notification));
			tableLayout.addView(tableRow);
			// setup reminder time type
			tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_time_mode));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			String text = null;
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
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
			// setup reminder type absolute (time and date buttons)
			// setup reminder type offset
			tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_time));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			if (reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
					.getValue()) {
				// Calendar calendar = Calendar.getInstance();
				long now = System.currentTimeMillis();
				if (reminder.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW
						.getValue()) {
					reminder.setReminderDateTime(now);
				}
				long absRemindOffset = Math.abs(reminder.getReminderDateTime());
				Integer days = (int) (absRemindOffset / (1000L * 60 * 60 * 24));
				Integer hours = (int) (absRemindOffset / (1000L * 60 * 60) % 24);
				Integer minutes = (int) (absRemindOffset / (1000L * 60) % 60);
				Long nextReminderDateTime = reminder.getNextReminderDateTime(context,
						now, true, Helper.getFirstDayOfWeek(context));
				String timeText;
				if (nextReminderDateTime != null) {
					// calendar.setTimeInMillis(nextReminderDateTime);
					DateFormat mDateFormat = DateFormat.getDateTimeInstance();
					timeText = mDateFormat.format(new Date(nextReminderDateTime));
				} else {
					timeText = resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_next_reminder_time_none);
				}
				String textNextAlarmTime = String
						.format(resources
								.getString(R.string.fragment_view_task_part_reminders_text_next_alarm_time),
								timeText);
				String nextReminderTimeString = (days == 0 ? ""
						: days
								+ " "
								+ resources
										.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_days)
								+ " ")
						+ (hours == 0 ? ""
								: hours
										+ " "
										+ resources
												.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_hours)
										+ " ")
						+ (minutes == 0 ? ""
								: minutes
										+ " "
										+ resources
												.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_minutes)
										+ " ")
						+ (reminder.getReminderDateTime() == 0 ? resources
								.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_at_task_start)
								: reminder.getReminderDateTime() > 0
										&& reminder.getReminderTimeModeValue() == ReminderTimeMode.TIME_BEFORE_EVENT
												.getValue()
										|| reminder.getReminderDateTime() < 0
										&& reminder.getReminderTimeModeValue() == ReminderTimeMode.TIME_AFTER_EVENT
												.getValue() ? resources
										.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_before_task_start)
										: resources
												.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_after_task_start));
				textViewValue.setText(nextReminderTimeString + ". " + textNextAlarmTime);
			} else {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(reminder.getReminderDateTime());
				DateFormat mDateFormat = DateFormat.getDateTimeInstance();
				String dateString = mDateFormat.format(new Date(calendar
						.getTimeInMillis()));
				textViewValue.setText(dateString);
			}
			tableLayout.addView(tableRow);
			// setup RemindEachIteration
			tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_is_recurrent_reminder));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			textViewValue
					.setText(reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
							.getValue()
							&& reminder.getReminderTimeModeValue() != ReminderTimeMode.AFTER_NOW
									.getValue()
							&& taskWithDependentsUiData.TaskUiData
									.getRecurrenceIntervalValue() != RecurrenceInterval.ONE_TIME
									.getValue() ? R.string.fragment_view_task_part_reminders_value_textview_is_recurrent_reminder_yes
							: R.string.fragment_view_task_part_reminders_value_textview_is_recurrent_reminder_no);
			tableLayout.addView(tableRow);
			// setup IsRingtoneCustom
			tableRow = (TableRow) layoutInflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader
					.setText(resources
							.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_ringtone));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			// if (reminder.getRingtone() == null) {
			// textViewValue
			// .setText(getResources()
			// .getString(
			// R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_from_task));
			// } else if (reminder.getRingtone().length() == 0) {
			// textViewValue
			// .setText(getResources().getString(
			// R.string.activity_edit_reminder_radiobutton_text_custom)
			// + " ("
			// + getResources().getString(R.string.ringtone_silent)
			// + ")");
			// } else {
			// Ringtone ringtone = RingtoneManager.getRingtone(getActivity(),
			// Uri.parse(reminder.getRingtone()));
			// if (ringtone != null) {
			// String name = ringtone.getTitle(getActivity());
			// textViewValue.setText(getResources().getString(
			// R.string.activity_edit_reminder_radiobutton_text_custom)
			// + " (" + name + ")");
			// } else {
			// textViewValue
			// .setText(getResources()
			// .getString(
			// R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_from_task));
			// }
			// }
			text = String
					.format(resources
							.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone),
							reminder.getRingtone() == null ? resources
									.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_from_task)
									: resources
											.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_custom),
							reminder.getRingtoneTitle(context));
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
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
	}
}
