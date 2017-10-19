package biz.advancedcalendar.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.utils.Helper;
import java.text.DateFormat;
import java.util.Date;

public class FragmentViewTaskPartMain extends Fragment {
	private TableLayout mTableLayout;
	private TaskWithDependentsUiData mEntityWithDependents;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_view_task_part_main, container, false);
		mTableLayout = (TableLayout) v
				.findViewById(R.id.fragment_view_task_part_main_tablelayout);
		mTableLayout.setStretchAllColumns(true);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
				new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						if (intent.getAction().equals(
								CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
							if (isAdded()) {
								mEntityWithDependents = ((TaskWithDependentsUiDataHolder) getActivity()).getTaskWithDependentsUiData();
								setupView();
							}
						}
					}
				}, new IntentFilter(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		mEntityWithDependents = ((TaskWithDependentsUiDataHolder) getActivity())
				.getTaskWithDependentsUiData();
		setupView();
	}

	private void setupView() {
		Task task = mEntityWithDependents.TaskUiData;
		// TODO check t for null
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
				String dateString = dateFormat.format(new Date(task.getStartDateTime()));
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
				String dateString = dateFormat.format(new Date(task.getEndDateTime()));
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
			textView.setText(getResources().getString(
					R.string.fragment_view_task_header_textview_task_recurrence_interval));
			textView = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
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
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbers(activity),
									mEntityWithDependents
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
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbers(activity),
									mEntityWithDependents
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
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbers(activity),
									mEntityWithDependents
											.getTimeUnitsStartingIndexValue()));
				}
				break;
			case WEEKS:
				if (task.getTimeUnitsCount() == 1) {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_week),
									mEntityWithDependents.getDelimitedSequenceOfWeekDays(
											activity, false)));
				} else {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_weeks),
									task.getTimeUnitsCount(), mEntityWithDependents
											.getDelimitedSequenceOfWeekDays(activity,
													false)));
				}
				break;
			case MONTHS_ON_DATE:
				if (task.getTimeUnitsCount() == 1) {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_month_on_date),
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbersForMonth(activity)));
				} else {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_months_on_date),
									task.getTimeUnitsCount(),
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbersForMonth(activity)));
				}
				break;
			case MONTHS_ON_NTH_WEEK_DAY:
				if (task.getTimeUnitsCount() == 1) {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_month_on_nth_week_day),
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(activity),
									mEntityWithDependents.getDelimitedSequenceOfWeekDays(
											activity, true)));
				} else {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_months_on_nth_week_day),
									task.getTimeUnitsCount(),
									mEntityWithDependents
											.getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(activity),
									mEntityWithDependents.getDelimitedSequenceOfWeekDays(
											activity, true)));
				}
				break;
			case YEARS:
				textView.setText(task.getTimeUnitsCount() == 1 ? getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_year)
						: task.getTimeUnitsCount()
								+ getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_years));
				if (task.getTimeUnitsCount() == 1) {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_year),
									mEntityWithDependents
											.getDelimitedSequenceOfDatesOfYear(activity)));
				} else {
					textView.setText(String
							.format(getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_years),
									task.getTimeUnitsCount(), mEntityWithDependents
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
					textView.setText(getResources().getString(
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
			textView.setText(getResources().getString(
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
			int backgroundColor = mEntityWithDependents.TaskUiData.getColor2(activity);
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
			textView.setText(Integer.toHexString(mEntityWithDependents.TaskUiData
					.getColor2(activity)));
			mTableLayout.addView(tableRow);
			// notes
			if (task.getDescription() != null && !(task.getDescription().length() == 0)) {
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
