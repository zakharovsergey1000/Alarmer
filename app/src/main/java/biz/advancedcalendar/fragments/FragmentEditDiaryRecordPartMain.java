package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivitySelectTreeItems;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.FragmentDatePicker.OnDateSelectedListener;
import biz.advancedcalendar.fragments.FragmentTimePicker.OnTimeSelectedListener;
import biz.advancedcalendar.greendao.Task;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FragmentEditDiaryRecordPartMain extends Fragment implements
		OnCheckedChangeListener, OnDateSelectedListener, OnTimeSelectedListener,
		DataSaver {
	// static final int SELECT_TASK_REQUEST = 1; // The request code
	// private DiaryRecord mEntityToEdit;
	private String mDatePickerDialogKey = "biz.advancedcalendar.fragments.FragmentEditDiaryRecordPartMain.DatePickerDialogKey";
	// private String mTimePickerDialogKey =
	// "biz.advancedcalendar.fragments.FragmentEditDiaryRecordPartMain.TimePickerDialog";
	private FragmentActivity mActivity;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_edit_diary_record_part_main,
				container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
		} else {
		}
		mActivity = getActivity();
		// setup listeners and set values on views
		// setup EditText task name
		// EditText editText;
		// editText = (EditText) mActivity
		// .findViewById(R.id.fragment_edit_diary_record_part_main_edittext_diary_record_text);
		// String recordText = null;
		// Global.getDiaryRecordToEdit().getRecordText();
		// if (recordText != null) {
		// editText.setText(Global.getDiaryRecordToEdit().getRecordText());
		// }
		// setup CheckBox linked task
		CheckBox checkBox = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_linked_task);
		// if (Global.getDiaryRecordToEdit().getTaskId() != null) {
		// Task t = DataProvider.getTask(getActivity(), Global.getDiaryRecordToEdit()
		// .getLocalTaskId(), false);
		// if (t != null) {
		// checkBox.setText(t.getText());
		// } else {
		// checkBox.setText("");
		// }
		// checkBox.setOnCheckedChangeListener(null);
		// checkBox.setChecked(true);
		// }
		checkBox.setOnCheckedChangeListener(this);
		// setup diary record task was completed checkBox
		CheckBox checkBoxTaskWasCompleted = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_task_was_completed);
		checkBoxTaskWasCompleted.setChecked(Global.getDiaryRecordToEdit()
				.getWasCompleted());
		// setup diary record waste time checkBox
		CheckBox checkBoxWasteTime = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_waste_time);
		checkBoxWasteTime.setChecked(Global.getDiaryRecordToEdit().getIsWasteTime());
		// setup diary record start date, start time and end time checkBoxes
		CheckBox checkBoxDate = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_date);
		// if (Global.getDiaryRecordToEdit().getDate() != null) {
		checkBoxDate.setChecked(true);
		// }
		CheckBox checkBoxStartTime = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime);
		if (Global.getDiaryRecordToEdit().getStartTime() != null) {
			checkBoxStartTime.setChecked(true);
		}
		CheckBox checkBoxEndTime = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime);
		if (Global.getDiaryRecordToEdit().getEndTime() != null) {
			checkBoxEndTime.setChecked(true);
		}
		setupDateTimeCheckboxes();
	}

	private void setupDateTimeCheckboxes() {
		// render diary record start date, start time and end time checkBoxes
		CheckBox checkBoxStartDate = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_date);
		checkBoxStartDate.setOnCheckedChangeListener(null);
		CheckBox checkBoxStartTime = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime);
		checkBoxStartTime.setOnCheckedChangeListener(null);
		CheckBox checkBoxEndTime = (CheckBox) mActivity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime);
		checkBoxEndTime.setOnCheckedChangeListener(null);
		Long dateUtc0 = Global.getDiaryRecordToEdit().getDate();
		if (dateUtc0 != null) {
			DateFormat dateFormat = DateFormat.getDateTimeInstance();
			String dateString = dateFormat.format(new Date(dateUtc0));
			checkBoxStartDate.setText(dateString);
			checkBoxStartDate.setChecked(true);
			Long startTime = Global.getDiaryRecordToEdit().getStartTime();
			if (checkBoxStartTime.isChecked() && startTime != null) {
				int hours = (int) (startTime / (10000 * 1000 * 60 * 60L));
				int minutes = (int) ((startTime - hours * 60L * 60 * 1000 * 10000) / 10000 / 1000 / 60);
				String timeString = (hours > 9 ? "" + hours : "0" + hours) + ":"
						+ (minutes > 9 ? "" + minutes : "0" + minutes);
				checkBoxStartTime.setText(timeString);
				checkBoxStartTime.setChecked(true);
			}
			Long endTime = Global.getDiaryRecordToEdit().getEndTime();
			if (checkBoxEndTime.isChecked() && endTime != null) {
				int hours = (int) (endTime / (10000 * 1000 * 60 * 60L));
				int minutes = (int) ((endTime - hours * 60L * 60 * 1000 * 10000) / 10000 / 1000 / 60);
				// timeFormat = new SimpleDateFormat("HH:mm");
				String timeString = (hours > 9 ? "" + hours : "0" + hours) + ":"
						+ (minutes > 9 ? "" + minutes : "0" + minutes);
				checkBoxEndTime.setText(timeString);
				checkBoxEndTime.setChecked(true);
			}
		} else {
			checkBoxStartDate
					.setText(getResources()
							.getString(
									R.string.fragment_edit_diary_record_part_main_checkbox_start_date_not_set));
			checkBoxStartDate.setChecked(false);
			checkBoxStartTime
					.setText(getResources()
							.getString(
									R.string.fragment_edit_diary_record_part_main_checkbox_start_time_not_set));
			checkBoxStartTime.setChecked(false);
			checkBoxEndTime
					.setText(getResources()
							.getString(
									R.string.fragment_edit_diary_record_part_main_checkbox_end_time_not_set));
			checkBoxEndTime.setChecked(false);
		}
		checkBoxStartDate.setOnCheckedChangeListener(this);
		checkBoxStartTime.setOnCheckedChangeListener(this);
		checkBoxEndTime.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_linked_task:
			if (isChecked) {
				Bundle bundle = new Bundle();
				bundle.putInt(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE,
						CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_TASK_TREE);
				bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
						AbsListView.CHOICE_MODE_SINGLE);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK,
						true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK,
						false);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK,
						false);
				Intent intent = new Intent(getActivity(), ActivitySelectTreeItems.class);
				intent.putExtra(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE, bundle);
				intent.putExtra(
						ActivitySelectTreeItems.IntentExtraTitle,
						getResources().getString(
								R.string.activity_select_task_title_select_parent_task));
				startActivityForResult(intent,
						CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE);
			} else {
				buttonView
						.setText(R.string.fragment_edit_diary_record_part_main_checkbox_diary_record_linked_task_not_set);
			}
			break;
		case R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_date:
			if (isChecked) {
				FragmentDatePicker newFragment = new FragmentDatePicker();
				Bundle b = new Bundle();
				b.putInt(
						CommonConstants.CALLER_ID,
						R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_date);
				b.putString(CommonConstants.TAG, getTag());
				Long dateTimeUtc0 = Global.getDiaryRecordToEdit().getDate();
				if (dateTimeUtc0 != null) {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(dateTimeUtc0);
					b.putInt(CommonConstants.YEAR, calendar.get(Calendar.YEAR));
					b.putInt(CommonConstants.MONTH, calendar.get(Calendar.MONTH));
					b.putInt(CommonConstants.DAY_OF_MONTH,
							calendar.get(Calendar.DAY_OF_MONTH));
				}
				newFragment.setArguments(b);
				newFragment.show(getFragmentManager(), mDatePickerDialogKey);
			} else {
				buttonView
						.setText(getResources()
								.getString(
										R.string.fragment_edit_diary_record_part_main_checkbox_start_date_not_set));
				CheckBox checkBox = (CheckBox) getActivity()
						.findViewById(
								R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime);
				checkBox.setChecked(false);
				checkBox = (CheckBox) getActivity()
						.findViewById(
								R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime);
				checkBox.setChecked(false);
			}
			break;
		case R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime:
			if (buttonView.isChecked()) {
				CheckBox checkBoxStartDate = (CheckBox) mActivity
						.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime);
				if (checkBoxStartDate.isChecked()) {
					FragmentTimePicker newFragment = new FragmentTimePicker();
					Bundle b = new Bundle();
					b.putInt(
							CommonConstants.CALLER_ID,
							R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime);
					b.putString(CommonConstants.TAG, getTag());
					Long startTime = Global.getDiaryRecordToEdit().getStartTime();
					if (startTime != null) {
						int hours = (int) (startTime / (10000 * 1000 * 60 * 60L));
						int minutes = (int) ((startTime - hours * 60L * 60 * 1000 * 10000) / 10000 / 1000 / 60);
						// Use the preset date in the picker
						b.putInt(CommonConstants.HOUR_OF_DAY, hours);
						b.putInt(CommonConstants.MINUTE, minutes);
					}
					newFragment.setArguments(b);
					newFragment.show(getFragmentManager(),
							CommonConstants.FragmentDatePicker);
				} else {
					Toast.makeText(
							getActivity().getApplicationContext(),
							getResources()
									.getString(
											R.string.fragment_edit_diary_record_part_main_checkbox_please_select_date_first),
							Toast.LENGTH_SHORT).show();
					buttonView.setChecked(false);
				}
			} else {
				buttonView.setText(getResources().getString(
						R.string.fragment_edit_diary_record_part_main_checkbox_hh_mm));
			}
			break;
		case R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime:
			if (buttonView.isChecked()) {
				CheckBox checkBoxEndDate = (CheckBox) mActivity
						.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime);
				if (checkBoxEndDate.isChecked()) {
					FragmentTimePicker newFragment = new FragmentTimePicker();
					Bundle b = new Bundle();
					b.putInt(
							CommonConstants.CALLER_ID,
							R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime);
					b.putString(CommonConstants.TAG, getTag());
					Long endTime = Global.getDiaryRecordToEdit().getEndTime();
					if (endTime != null) {
						int hours = (int) (endTime / (10000 * 1000 * 60 * 60L));
						int minutes = (int) ((endTime - hours * 60L * 60 * 1000 * 10000) / 10000 / 1000 / 60);
						// Use the preset date in the picker
						b.putInt(CommonConstants.HOUR_OF_DAY, hours);
						b.putInt(CommonConstants.MINUTE, minutes);
					}
					newFragment.setArguments(b);
					newFragment.show(getFragmentManager(),
							CommonConstants.FragmentTimePicker);
				} else {
					Toast.makeText(
							getActivity().getApplicationContext(),
							getResources()
									.getString(
											R.string.fragment_edit_diary_record_part_main_checkbox_please_select_date_first),
							Toast.LENGTH_SHORT).show();
					buttonView.setChecked(false);
				}
			} else {
				buttonView.setText(getResources().getString(
						R.string.fragment_edit_diary_record_part_main_checkbox_hh_mm));
			}
			break;
		default:
			break;
		}
	}

	// @Override
	// public void onSaveInstanceState(Bundle savedInstanceState) {
	// super.onSaveInstanceState(savedInstanceState);
	// }
	@Override
	public void onStop() {
		if (!getActivity().isFinishing()) {
			// collect info
			isDataCollected();
		}
		super.onStop();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Check which request it is that we're responding to
		if (requestCode == CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE) {
			// Make sure the request was successful
			if (resultCode == android.app.Activity.RESULT_OK) {
				if (intent.hasExtra(CommonConstants.ID_ARRAY)) {
					long[] idArray = intent.getLongArrayExtra(CommonConstants.ID_ARRAY);
					Task t = null;
					if (idArray.length > 0) {
						t = DataProvider.getTask(null, getActivity(), idArray[0], false);
					}
					// setup CheckBox
					CheckBox checkBox = (CheckBox) getActivity()
							.findViewById(
									R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_linked_task);
					if (t == null) {
						checkBox.setChecked(false);
					} else {
						checkBox.setText(t.getName());
						// store linked task
						Global.getDiaryRecordToEdit().setLocalTaskId(idArray[0]);
					}
				}
			} else if (resultCode == android.app.Activity.RESULT_CANCELED) {
				// checkBox.setChecked(false);
			}
		}
	}

	@Override
	public boolean isDataCollected() {
		Activity activity = getActivity();
		// collect diary record text
		// EditText editText = (EditText) activity
		// .findViewById(R.id.fragment_edit_diary_record_part_main_edittext_diary_record_text);
		// String text = editText.getText().toString().trim();
		// Global.getDiaryRecordToEdit().setRecordText(text);
		// collect linked task
		// It is already collected in onActivityResult but adjust it in case
		// checkbox_task_parent is unchecked
		CheckBox checkBox = (CheckBox) getActivity()
				.findViewById(
						R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_linked_task);
		if (!checkBox.isChecked()) {
			// wipe out linked task
			// Global.getDiaryRecordToEdit().setTaskId(null);
		}
		// collect task was completed
		checkBox = (CheckBox) getActivity()
				.findViewById(
						R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_task_was_completed);
		Global.getDiaryRecordToEdit().setWasCompleted(checkBox.isChecked());
		// collect is waste time
		checkBox = (CheckBox) getActivity()
				.findViewById(
						R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_waste_time);
		Global.getDiaryRecordToEdit().setIsWasteTime(checkBox.isChecked());
		// collect diary record date
		// It is already collected in onDateSelected but adjust it in case
		// checkbox_diary_record_date is unchecked
		checkBox = (CheckBox) activity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_date);
		if (!checkBox.isChecked()) {
			// wipe out start date
			// Global.getDiaryRecordToEdit().setDate(null);
		}
		// collect diary record start time
		// It is already collected in onCheckedChanged but adjust it in case
		// checkbox_diary_record_starttime is unchecked
		checkBox = (CheckBox) activity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime);
		if (!checkBox.isChecked()) {
			// wipe out start time
			Global.getDiaryRecordToEdit().setStartTime(null);
		}
		// collect diary record end time
		// It is already collected in onCheckedChanged but adjust it in case
		// checkbox_diary_record_endtime is unchecked
		checkBox = (CheckBox) activity
				.findViewById(R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime);
		if (!checkBox.isChecked()) {
			// wipe out end time
			Global.getDiaryRecordToEdit().setEndTime(null);
		}
		return true;
	}

	@Override
	public void onDateSelected(FragmentDatePicker fragmentDatePicker, int year,
			int month, int day, boolean cancelled) {
		if (!cancelled) {
			// Bundle b = fragmentDatePicker.getArguments();
			// int callerId = b.getInt(CommonConstants.CALLER_ID);
			// String tag = b.getString(CommonConstants.TAG);
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month, day, 0, 0, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			// save into database as UTC0 date
			Global.getDiaryRecordToEdit().setDate(calendar.getTimeInMillis());
			setupDateTimeCheckboxes();
		}
	}

	@Override
	public void onTimeSelected(FragmentTimePicker fragmentTimePicker, int hourOfDay,
			int minute, boolean cancelled) {
		if (!cancelled) {
			Bundle b = fragmentTimePicker.getArguments();
			int callerId = b.getInt(CommonConstants.CALLER_ID);
			// String tag = b.getString(CommonConstants.TAG);
			if (callerId == 0) {
				throw new IllegalArgumentException(
						"The callerId is 0. Must not be zero in onTimeSelected for FragmentEditDiaryRecordPartMain.");
				// return;
			}
			Calendar calendar = new GregorianCalendar();
			calendar.clear();
			switch (callerId) {
			case R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_starttime:
				Global.getDiaryRecordToEdit().setStartTime(
						(hourOfDay * 60L + minute) * 60 * 1000 * 10000);
				break;
			case R.id.fragment_edit_diary_record_part_main_checkbox_diary_record_endtime:
				Global.getDiaryRecordToEdit().setEndTime(
						(hourOfDay * 60L + minute) * 60 * 1000 * 10000);
				break;
			}
			setupDateTimeCheckboxes();
		}
	}
}
