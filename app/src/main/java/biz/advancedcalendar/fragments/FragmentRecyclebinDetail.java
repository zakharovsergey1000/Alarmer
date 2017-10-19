package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityCalendarDetail;
import biz.advancedcalendar.activities.ActivityCalendarList;
import biz.advancedcalendar.activities.activityedittask.ReminderUiData2;
import biz.advancedcalendar.activities.activityedittask.TaskUiData2;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CheckableLinearLayout;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/** A fragment representing a single Calendar detail screen. This fragment is either
 * contained in a {@link ActivityCalendarList} in two-pane mode (on tablets) or a
 * {@link ActivityCalendarDetail} on handsets. */
public class FragmentRecyclebinDetail extends ListFragment {
	/** The fragment argument representing the item ID that this fragment represents. */
	public static final String ARG_ITEM_ID = "item_id";
	/** The dummy content this fragment is presenting. */
	// private List<TaskWithDependents> mTasks;
	// private BroadcastReceiver mReceiver;
	// private ListView mListview;
	ArrayAdapterTask mAdapter;
	private StateParameters mStateParameters;

	private static class StateParameters implements Parcelable {
		private ArrayList<TaskUiData2> mTaskUiData2List;

		protected StateParameters(Parcel in) {
			if (in.readByte() == 0x01) {
				int size = in.readInt();
				mTaskUiData2List = new ArrayList<TaskUiData2>(size);
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						TaskUiData2 taskUiData2;
						if (in.readByte() == 0x01) {
							taskUiData2 = (TaskUiData2) in.readValue(TaskUiData2.class
									.getClassLoader());
						} else {
							taskUiData2 = null;
						}
						mTaskUiData2List.add(taskUiData2);
					}
				}
			} else {
				mTaskUiData2List = null;
			}
		}

		public StateParameters() {
			// TODO Auto-generated constructor stub
		}

		public StateParameters(ArrayList<TaskUiData2> taskUiData2List) {
			super();
			mTaskUiData2List = taskUiData2List;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (mTaskUiData2List == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(mTaskUiData2List.size());
				Iterator<TaskUiData2> iterator = mTaskUiData2List.iterator();
				while (iterator.hasNext()) {
					TaskUiData2 entry = iterator.next();
					if (entry == null) {
						dest.writeByte((byte) 0x00);
					} else {
						dest.writeByte((byte) 0x01);
						dest.writeValue(entry);
					}
				}
			}
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<StateParameters> CREATOR = new Parcelable.Creator<StateParameters>() {
			@Override
			public StateParameters createFromParcel(Parcel in) {
				return new StateParameters(in);
			}

			@Override
			public StateParameters[] newArray(int size) {
				return new StateParameters[size];
			}
		};
	}

	class ArrayAdapterTask extends ArrayAdapter<TaskUiData2> {
		List<TaskUiData2> mTasks1;
		// private int mListRowPosition;
		private LayoutInflater mLayoutInflater;

		// private AlertDialog mDialog;
		public ArrayAdapterTask(Context context, int textViewResourceId,
				List<TaskUiData2> tasks) {
			super(context, textViewResourceId, tasks);
			mLayoutInflater = LayoutInflater.from(context);
			mTasks1 = tasks;
		}

		public void setTasks(ArrayList<TaskUiData2> taskWithDependentsList) {
			mTasks1.clear();
			if (taskWithDependentsList != null) {
				mTasks1.addAll(taskWithDependentsList);
			}
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CheckableLinearLayout checkableLinearLayout;
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
			TaskUiData2 taskUiData2 = getItem(position);
			setupTaskDetails(tableLayout, taskUiData2);
			List<ReminderUiData2> reminders = taskUiData2.getReminderList();
			Resources resources = getResources();
			for (ReminderUiData2 reminder : reminders) {
				TableRow tableRow = (TableRow) mLayoutInflater.inflate(
						R.layout.tablerow_single_textview, tableLayout, false);
				((TextView) tableRow.findViewById(R.id.tablerow_single_textview_textview))
						.setText(reminder.getText());
				tableLayout.addView(tableRow);
				TextView textViewHeader;
				TextView textViewValue;
				String text;
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
				// setup reminder type
				text = reminder.getIsAlarm() ? resources
						.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_type_alarm)
						: resources
								.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_type_notification);
				reminder.generateTableRow(
						tableLayout,
						mLayoutInflater,
						resources,
						R.string.fragment_view_task_part_reminders_header_textview_alarm_mode,
						text);
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
				// reminder.generateTableRow(
				// tableLayout,
				// mLayoutInflater,
				// resources,
				// R.string.fragment_view_task_part_reminders_header_textview_reminder_time,
				// text);
				tableRow = (TableRow) mLayoutInflater.inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
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
					Long nextReminderDateTime = reminder.getNextReminderDateTime(
							activity, now, true, Helper.getFirstDayOfWeek(activity));
					String timeText;
					if (nextReminderDateTime != null) {
						// calendar.setTimeInMillis(nextReminderDateTime);
						DateFormat mDateFormat = DateFormat.getDateTimeInstance(
								DateFormat.MEDIUM, DateFormat.SHORT);
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
					textViewValue.setText(nextReminderTimeString + ". "
							+ textNextAlarmTime);
				} else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(reminder.getReminderDateTime());
					DateFormat mDateFormat = DateFormat.getDateTimeInstance(
							DateFormat.MEDIUM, DateFormat.SHORT);
					String dateString = mDateFormat.format(new Date(calendar
							.getTimeInMillis()));
					textViewValue.setText(dateString);
				}
				tableLayout.addView(tableRow);
				// setup RemindEachIteration
				tableRow = (TableRow) mLayoutInflater.inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
				textViewHeader = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textViewHeader
						.setText(resources
								.getString(R.string.fragment_view_task_part_reminders_header_textview_is_recurrent_reminder));
				textViewValue = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				tableLayout.addView(tableRow);
				// setup IsRingtoneCustom
				tableRow = (TableRow) mLayoutInflater.inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
				textViewHeader = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textViewHeader
						.setText(resources
								.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_ringtone));
				textViewValue = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				text = String
						.format(resources
								.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone),
								reminder.getRingtone() == null ? resources
										.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_from_task)
										: resources
												.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_custom),
								reminder.getRingtoneTitle(activity));
				textViewValue.setText(text);
				tableLayout.addView(tableRow);
			}
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
		private void setupTaskDetails(TableLayout mTableLayout,
				TaskUiData2 taskWithDependents) {
			TaskUiData2 task = taskWithDependents;
			try {
				mTableLayout.removeAllViews();
				FragmentActivity activity = getActivity();
				TableRow tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				// fill task text row
				TextView textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_task_name));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				textView.setText(task.getName().toString());
				mTableLayout.addView(tableRow);
				// task_start
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_task_start));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
						DateFormat.SHORT);
				if (task.getStartDateTime() != null) {
					String dateString = dateFormat.format(new Date(task
							.getStartDateTime()));
					textView.setText(dateString);
				} else {
					textView.setText(getString(R.string.fragment_view_task_part_main_value_textview_task_start_date_not_set));
				}
				mTableLayout.addView(tableRow);
				// task_end
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_task_end));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				if (task.getEndDateTime() != null) {
					String dateString = dateFormat
							.format(new Date(task.getEndDateTime()));
					textView.setText(dateString);
				} else {
					textView.setText(getString(R.string.fragment_view_task_part_main_value_textview_task_end_date_not_set));
				}
				mTableLayout.addView(tableRow);
				// task_type
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_is_recurrent));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				switch (RecurrenceInterval.fromInt(task.getRecurrenceIntervalValue())) {
				case ONE_TIME:
				default:
					textView.setText(R.string.fragment_view_task_value_textview_is_recurrent_no);
					break;
				case DAYS:
				case HOURS:
				case MINUTES:
				case MONTHS_ON_DATE:
				case MONTHS_ON_NTH_WEEK_DAY:
				case WEEKS:
				case YEARS:
					textView.setText(R.string.fragment_view_task_value_textview_is_recurrent_yes);
					break;
				}
				mTableLayout.addView(tableRow);
				// hours_required
				// tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(
				// R.layout.fragment_view_task_part_main_table_row, null);
				// textView = (TextView) tableRow
				// .findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				// textView.setText(getResources().getString(
				// R.string.fragment_view_task_header_textview_task_hours_required));
				// textView = (TextView) tableRow
				// .findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				// textView.setText("" + task.getRequiredLength());
				// mTableLayout.addView(tableRow);
				// // hours_spent
				// tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(
				// R.layout.fragment_view_task_part_main_table_row, null);
				// textView = (TextView) tableRow
				// .findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				// textView.setText(getResources().getString(
				// R.string.fragment_view_task_header_textview_task_hours_spent));
				// textView = (TextView) tableRow
				// .findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				// textView.setText("" + task.getActualLength());
				// mTableLayout.addView(tableRow);
				// repetition_every
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources()
						.getString(
								R.string.fragment_view_task_header_textview_task_recurrence_interval));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				TaskUiData2 taskWithDependentsUiData = taskWithDependents;
				switch (RecurrenceInterval.fromInt(task.getRecurrenceIntervalValue())) {
				case ONE_TIME:
					textView.setText(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_not_set));
					break;
				case MINUTES:
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(R.string.fragment_view_task_value_textview_task_recurrence_interval_minute);
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_minutes),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbers(activity),
										taskWithDependentsUiData
												.getTimeUnitsStartingIndexValue()));
					}
					break;
				case HOURS:
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(R.string.fragment_view_task_value_textview_task_recurrence_interval_hour);
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_hours),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbers(activity),
										taskWithDependentsUiData
												.getTimeUnitsStartingIndexValue()));
					}
					break;
				case DAYS:
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(R.string.fragment_view_task_value_textview_task_recurrence_interval_day);
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_days),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbers(activity),
										taskWithDependentsUiData
												.getTimeUnitsStartingIndexValue()));
					}
					break;
				case WEEKS:
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_week),
										taskWithDependentsUiData
												.getDelimitedSequenceOfWeekDays(activity,
														false)));
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_weeks),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfWeekDays(activity,
														false)));
					}
					break;
				case MONTHS_ON_DATE:
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_month_on_date),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbersForMonth(activity)));
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_months_on_date),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbersForMonth(activity)));
					}
					break;
				case MONTHS_ON_NTH_WEEK_DAY:
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_month_on_nth_week_day),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(activity),
										taskWithDependentsUiData
												.getDelimitedSequenceOfWeekDays(activity,
														true)));
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_months_on_nth_week_day),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(activity),
										taskWithDependentsUiData
												.getDelimitedSequenceOfWeekDays(activity,
														true)));
					}
					break;
				case YEARS:
					textView.setText(task.getTimeUnitsCount() == 1 ? getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_year)
							: task.getTimeUnitsCount()
									+ getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_years));
					if (task.getTimeUnitsCount() == 1) {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_year),
										taskWithDependentsUiData
												.getDelimitedSequenceOfDatesOfYear(activity)));
					} else {
						textView.setText(String
								.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_years),
										task.getTimeUnitsCount(),
										taskWithDependentsUiData
												.getDelimitedSequenceOfDatesOfYear(activity)));
					}
					break;
				default:
					break;
				}
				mTableLayout.addView(tableRow);
				// no_more_than
				if (task.getRecurrenceIntervalValue() != RecurrenceInterval.ONE_TIME
						.getValue()) {
					tableRow = (TableRow) LayoutInflater.from(activity).inflate(
							R.layout.fragment_view_task_part_main_table_row, null);
					textView = (TextView) tableRow
							.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
					textView.setText(getResources()
							.getString(
									R.string.fragment_view_task_header_textview_task_repetition_no_more_than));
					textView = (TextView) tableRow
							.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
					if (task.getOccurrencesMaxCount() != null) {
						textView.setText("" + task.getOccurrencesMaxCount());
					} else {
						textView.setText(getString(R.string.fragment_view_task_header_textview_task_repetition_no_more_than_not_set));
					}
					mTableLayout.addView(tableRow);
					// until_date
					tableRow = (TableRow) LayoutInflater.from(activity).inflate(
							R.layout.fragment_view_task_part_main_table_row, null);
					textView = (TextView) tableRow
							.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
					textView.setText(getResources()
							.getString(
									R.string.fragment_view_task_header_textview_task_repetition_until_date));
					textView = (TextView) tableRow
							.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
					if (task.getRepetitionEndDateTime() != null) {
						String dateString = dateFormat.format(new Date(task
								.getRepetitionEndDateTime()));
						textView.setText(dateString);
					} else {
						textView.setText(getString(R.string.fragment_view_task_value_textview_task_repetition_until_date_not_set));
					}
					mTableLayout.addView(tableRow);
				}
				// fill parent_task row
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_parent_task));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				Task parentTask = null;
				if (task.getParentId() != null) {
					parentTask = DataProvider.getTask(null, activity, task.getParentId(),
							false);
				}
				textView.setText(parentTask == null ? getResources().getString(
						R.string.fragment_view_task_value_textview_parent_task_not_set)
						: parentTask.getName().toString());
				mTableLayout.addView(tableRow);
				// fill sort_order row
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_sort_order));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				if (task.getSortOrder() == 0) {
					textView.setText(getResources().getString(
							R.string.fragment_view_task_value_textview_sort_order_first));
				} else {
					Task taskSibling = DataProvider.getSortOrderSibling(null,
							activity.getApplicationContext(), task.getSortOrder(),
							task.getParentId());
					if (taskSibling != null) {
						textView.setText(getResources()
								.getString(
										R.string.fragment_view_task_value_textview_sort_order_aftertask)
								+ " " + taskSibling.getName());
					} else {
						textView.setText(getResources()
								.getString(
										R.string.fragment_view_task_value_textview_sort_order_last));
					}
				}
				mTableLayout.addView(tableRow);
				// fill completed_percent row
				// ProgressBar progressBar = (ProgressBar) activity
				// .findViewById(R.id.fragment_view_task_progressbar_completed_percent);
				// progressBar.setProgress(t.getPercentOfCompletion());
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_is_completed));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				textView.setText(task.getIsCompleted() ? R.string.fragment_view_task_value_textview_is_completed_yes
						: R.string.fragment_view_task_value_textview_is_completed_no);
				mTableLayout.addView(tableRow);
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				String text = String
						.format(getResources()
								.getString(
										R.string.fragment_view_task_value_textview_percent_of_completion),
								task.getPercentOfCompletion());
				textView.setText(getResources()
						.getString(
								R.string.fragment_view_task_header_textview_percent_of_completion));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				textView.setText(text);
				// mTableLayout.addView(tableRow);
				// priority
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_priority));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				switch (Task.PRIORITY.fromInt(task.getPriority())) {
				case HIGH:
					textView.setText(getString(R.string.fragment_view_task_part_main_value_textview_task_priority_high));
					break;
				case LOW:
					textView.setText(getString(R.string.fragment_view_task_part_main_value_textview_task_priority_low));
					break;
				case MEDIUM:
					textView.setText(getString(R.string.fragment_view_task_part_main_value_textview_task_priority_medium));
					break;
				default:
					break;
				}
				// mTableLayout.addView(tableRow);
				// color
				tableRow = (TableRow) LayoutInflater.from(activity).inflate(
						R.layout.fragment_view_task_part_main_table_row, null);
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textView.setText(getResources().getString(
						R.string.fragment_view_task_header_textview_color));
				textView = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				int backgroundColor = taskWithDependentsUiData.getColor2(activity);
				int textColor;
				if (Helper.getContrastYIQ(backgroundColor)) {
					textColor = getResources().getColor(
							R.color.task_view_text_synchronized_dark);
				} else {
					textColor = getResources().getColor(
							R.color.task_view_text_synchronized_light);
				}
				textView.setBackgroundColor(backgroundColor);
				textView.setTextColor(textColor);
				textView.setText(Integer.toHexString(taskWithDependentsUiData
						.getColor2(activity)));
				mTableLayout.addView(tableRow);
				// notes
				if (task.getDescription() != null
						&& !(task.getDescription().length() == 0)) {
					tableRow = (TableRow) LayoutInflater.from(activity).inflate(
							R.layout.fragment_view_task_part_main_table_row, null);
					textView = (TextView) tableRow
							.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
					textView.setText(getResources().getString(
							R.string.fragment_view_task_header_textview_task_notes));
					textView = (TextView) tableRow
							.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
					textView.setText(task.getDescription());
					mTableLayout.addView(tableRow);
				}
				// tv = (TextView) activity
				// .findViewById(R.id.fragment_view_task_value_textview_task_labels);
				// tv.setText(parentTask == null ? "" : parentTask.getText().toString());
				// tv = (TextView) activity
				// .findViewById(R.id.fragment_view_task_value_textview_task_contacts);
				// tv.setText(parentTask == null ? "" : parentTask.getText().toString());
				// tv = (TextView) activity
				// .findViewById(R.id.fragment_view_task_value_textview_task_reminders);
				// tv.setText(parentTask == null ? "" : parentTask.getText().toString());
			} catch (NullPointerException e) {
				Log.wtf(CommonConstants.WTF_TAG, "taskId = " + task.getId() + " and t = "
						+ task + " in " + getClass() + " class in " + new Object() {
						}.getClass().getEnclosingMethod().getName() + " method", e);
			}
		}
	}

	/** Mandatory empty constructor for the fragment manager to instantiate the fragment
	 * (e.g. upon screen orientation changes). */
	public FragmentRecyclebinDetail() {
		mStateParameters = new StateParameters(new ArrayList<TaskUiData2>());
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mStateParameters = savedInstanceState.getParcelable("mStateParameters");
		}
		mAdapter = new ArrayAdapterTask(getActivity(), 0,
				mStateParameters.mTaskUiData2List);
		setListAdapter(mAdapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// mListview = getListView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("mStateParameters", mStateParameters);
		super.onSaveInstanceState(outState);
	}

	public void setTasks(ArrayList<TaskUiData2> taskWithDependentsList) {
		mStateParameters.mTaskUiData2List = taskWithDependentsList;
		if (mAdapter != null) {
			mAdapter.setTasks(taskWithDependentsList);
		}
	}
	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View rootView = inflater.inflate(R.layout.fragment_calendar_detail, container,
	// false);
	// // Show the dummy content as text in a TextView.
	// if (mItem != null) {
	// ((TextView) rootView.findViewById(R.id.calendar_detail))
	// .setText(mItem.content);
	// }
	// return rootView;
	// }
	//
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// super.onActivityCreated(savedInstanceState);
	// setHasOptionsMenu(true);
	// mListview = getListView();
	// }
}
