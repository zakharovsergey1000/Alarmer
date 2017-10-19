package biz.advancedcalendar.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.activityedittask.ReminderUiData2;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.fragments.ConfirmationAlertDialogFragment;
import biz.advancedcalendar.fragments.ConfirmationAlertDialogFragment.YesNoListener;
import biz.advancedcalendar.fragments.RetainedFragmentForActivityImportTasks;
import biz.advancedcalendar.greendao.DowngradeException;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.importtask.TaskUiData3;
import biz.advancedcalendar.utils.Helper;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("UseSparseArrays")
public class ActivityImportTasks extends AppCompatActivity {
	public static class ImportTasksState implements Parcelable {
		public List<TaskUiData3> TasksFromBackupFile;
		public List<TaskUiData3> TasksFromApp;
		public List<List<Integer>> mTasksFromAppSortedLists;
		public int[] mSelectedRadioButtons;
		public ArrayList<Integer> mCorrelatedTasksFromApp;

		public ImportTasksState(List<TaskUiData3> tasksFromBackupFile, List<TaskUiData3> tasksFromApp) {
			super();
			TasksFromBackupFile = new ArrayList<TaskUiData3>(tasksFromBackupFile);
			TasksFromApp = new ArrayList<TaskUiData3>(tasksFromApp);
			//
			int tasksFromAppSize = tasksFromApp.size();
			int tasksFromBackupFileSize = tasksFromBackupFile.size();
			mTasksFromAppSortedLists = new ArrayList<List<Integer>>(tasksFromBackupFileSize);
			ArrayList<TaskUiData3> tasksFromAppTempList = new ArrayList<TaskUiData3>(tasksFromApp);
			for (int i = 0; i < tasksFromBackupFileSize; i++) {
				TaskUiData3 taskFromBackupFile = tasksFromBackupFile.get(i);
				Comparator<TaskUiData3> comparator = new TaskUiData2Comparator(taskFromBackupFile);
				Collections.sort(tasksFromAppTempList, comparator);
				List<Integer> tasksFromAppSortedList = new ArrayList<Integer>(tasksFromAppSize);
				for (TaskUiData3 taskUiData2 : tasksFromAppTempList) {
					tasksFromAppSortedList.add(TasksFromApp.indexOf(taskUiData2));
				}
				mTasksFromAppSortedLists.add(tasksFromAppSortedList);
			}
			mCorrelatedTasksFromApp = new ArrayList<Integer>(tasksFromBackupFileSize);
			for (int i = 0; i < tasksFromBackupFileSize; i++) {
				mCorrelatedTasksFromApp.add(tasksFromAppSize > 0 ? mTasksFromAppSortedLists.get(i).get(0) : null);
			}
			mSelectedRadioButtons = new int[tasksFromBackupFileSize];
			int id = tasksFromAppSize == 0 ? R.id.activity_import_tasks_recyclerview_item_radio_add
					: R.id.activity_import_tasks_recyclerview_item_radio_skip;
			for (int i = 0; i < tasksFromBackupFileSize; i++) {
				mSelectedRadioButtons[i] = id;
			}
		}

		protected ImportTasksState(Parcel in) {
			if (in.readByte() == 0x01) {
				TasksFromBackupFile = new ArrayList<TaskUiData3>();
				in.readList(TasksFromBackupFile, TaskUiData3.class.getClassLoader());
			} else {
				TasksFromBackupFile = null;
			}
			if (in.readByte() == 0x01) {
				TasksFromApp = new ArrayList<TaskUiData3>();
				in.readList(TasksFromApp, TaskUiData3.class.getClassLoader());
			} else {
				TasksFromApp = null;
			}
			if (in.readByte() == 0x01) {
				int size = in.readInt();
				mTasksFromAppSortedLists = new ArrayList<List<Integer>>(size);
				for (int i = 0; i < size; i++) {
					int size2 = in.readInt();
					List<Integer> tasksFromAppSortedList = new ArrayList<Integer>(size2);
					for (int i2 = 0; i2 < size2; i2++) {
						tasksFromAppSortedList.add(in.readInt());
					}
					mTasksFromAppSortedLists.add(tasksFromAppSortedList);
				}
			} else {
				mTasksFromAppSortedLists = null;
			}
			if (in.readByte() == 0x01) {
				int size = in.readInt();
				mSelectedRadioButtons = new int[size];
				for (int i = 0; i < size; i++) {
					mSelectedRadioButtons[i] = in.readInt();
				}
			} else {
				mTasksFromAppSortedLists = null;
			}
			if (in.readByte() == 0x01) {
				int size = in.readInt();
				mCorrelatedTasksFromApp = new ArrayList<Integer>(size);
				for (int i = 0; i < size; i++) {
					mCorrelatedTasksFromApp.add(in.readInt());
				}
			} else {
				mCorrelatedTasksFromApp = null;
			}
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (TasksFromBackupFile == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(TasksFromBackupFile);
			}
			if (TasksFromApp == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(TasksFromApp);
			}
			if (mTasksFromAppSortedLists == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				int size = mTasksFromAppSortedLists.size();
				dest.writeInt(size);
				for (int i = 0; i < size; i++) {
					List<Integer> tasksFromAppSortedList = mTasksFromAppSortedLists.get(i);
					int size2 = tasksFromAppSortedList.size();
					dest.writeInt(size2);
					for (int i2 = 0; i2 < size2; i2++) {
						dest.writeInt(tasksFromAppSortedList.get(i2));
					}
				}
			}
			if (mSelectedRadioButtons == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				int size = mSelectedRadioButtons.length;
				dest.writeInt(size);
				for (int i = 0; i < size; i++) {
					dest.writeInt(mSelectedRadioButtons[i]);
				}
			}
			if (mCorrelatedTasksFromApp == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				int size = mCorrelatedTasksFromApp.size();
				dest.writeInt(size);
				for (int i = 0; i < size; i++) {
					dest.writeInt(mCorrelatedTasksFromApp.get(i));
				}
			}
		}

		public static final Parcelable.Creator<ImportTasksState> CREATOR = new Parcelable.Creator<ImportTasksState>() {
			@Override
			public ImportTasksState createFromParcel(Parcel in) {
				return new ImportTasksState(in);
			}

			@Override
			public ImportTasksState[] newArray(int size) {
				return new ImportTasksState[size];
			}
		};
	}

	private class Node1 implements Comparable<Node1> {

		long id;
		Long parentId = null;
		private Integer level = null;
		Map<Long, Node1> node1Map;
		private int position;

		public Node1(long id, Long parentId, Map<Long, Node1> node1Map, int position) {
			this.id = id;
			this.parentId = parentId;
			if (parentId == null) {
				level = 0;
			}
			this.node1Map = node1Map;
			this.position = position;
		}

		@Override
		public int compareTo(Node1 other) {
			return getLevel() - other.getLevel();
		}

		public int getLevel() {
			if (level != null) {
				return level;
			} else {
				Node1 node1 = node1Map.get(parentId);
				if (node1 != null) {
					level = node1.getLevel() + 1;
					return level;
				} else {
					level = 0;
					return level;
				}
			}
		}

		public int getPosition() {
			return position;
		}
	}

	private class ActivityImportTasksAdapter extends RecyclerView.Adapter<ActivityImportTasksAdapter.ViewHolder>
			implements OnCheckedChangeListener {
		class ViewHolder extends RecyclerView.ViewHolder {
			LinearLayout itemView;
			TableLayout backupTaskTableLayout;
			RadioGroup radioGroup;
			// ImageButton imageButtonSelectTask;
			ImageButton imageButtonRestore;
			int position;

			// Context context;
			ViewHolder(Context context, int position, LinearLayout itemView, TableLayout backupTaskTableLayout,
					RadioGroup radioGroup, ImageButton imageButtonSelectTask, ImageButton imageButtonRestore) {
				super(itemView);
				this.position = position;
				this.itemView = itemView;
				this.backupTaskTableLayout = backupTaskTableLayout;
				this.radioGroup = radioGroup;
				// this.imageButtonSelectTask = imageButtonSelectTask;
				this.imageButtonRestore = imageButtonRestore;
			}

			public void setParent(ViewGroup parent) {
				itemView.setLayoutParams(parent.getLayoutParams());
			}
		}

		protected static final String IntentExtraPosition = "position";
		private AppCompatActivity mAppCompatActivity;
		private LayoutInflater mLayoutInflater;
		private ImportTasksState mImportTasksState;
		private String[] mTaskHeaders;
		private String[] mReminderHeaders;
		private ViewHolder[] mViewHolders;
		private int mTasksFromBackupFileSize;

		public ActivityImportTasksAdapter(final AppCompatActivity appCompatActivity, ImportTasksState importTasksState,
				LinearLayoutManager linearLayoutManager) {
			mAppCompatActivity = appCompatActivity;
			mImportTasksState = importTasksState;
			mLayoutInflater = (LayoutInflater) appCompatActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mTaskHeaders = getTaskHeaders(appCompatActivity);
			mReminderHeaders = getReminderHeaders(appCompatActivity);
			mTasksFromBackupFileSize = mImportTasksState.TasksFromBackupFile.size();
			mViewHolders = new ViewHolder[mTasksFromBackupFileSize];
			for (int i = 0; i < mTasksFromBackupFileSize; i++) {
				mViewHolders[i] = createViewHolderForPosition(i);
			}
		}

		@SuppressLint("InflateParams")
		private ViewHolder createViewHolderForPosition(int position) {
			LinearLayout linearLayout = (LinearLayout) mLayoutInflater
					.inflate(R.layout.activity_import_tasks_recyclerview_item, null);
			TableLayout tableLayout = (TableLayout) linearLayout
					.findViewById(R.id.activity_import_tasks_recyclerview_item_backup_task_tablelayout);
			RadioGroup radioGroup = (RadioGroup) linearLayout
					.findViewById(R.id.activity_import_tasks_recyclerview_item_radiogroup);
			ImageButton imageButtonSelectTask = (ImageButton) linearLayout.findViewById(
					R.id.activity_import_tasks_recyclerview_item_imagebutton_choose_another_correlated_app_task);
			ImageButton imageButtonRestore = (ImageButton) linearLayout.findViewById(
					R.id.activity_import_tasks_recyclerview_imagebutton_replace_task_in_app_by_the_task_from_backup_file);
			//
			// setup radioGroup
			radioGroup.setTag(position);
			radioGroup.setOnCheckedChangeListener(null);
			int checkedId = mImportTasksState.mSelectedRadioButtons[position];
			radioGroup.check(checkedId);
			radioGroup.setOnCheckedChangeListener(this);
			// setup imageButtonSelectTask
			imageButtonSelectTask.setTag(position);
			imageButtonSelectTask.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					Bundle bundle = new Bundle();
					bundle.putInt(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE,
							CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_TASK_TREE);
					bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE, AbsListView.CHOICE_MODE_SINGLE);
					bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK, true);
					bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK, false);
					bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
					bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK, true);
					Intent intent = new Intent(mAppCompatActivity, ActivitySelectTreeItems.class);
					intent.putExtra(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE, bundle);
					intent.putExtra(ActivityImportTasksAdapter.IntentExtraPosition, position);
					intent.putExtra(ActivitySelectTreeItems.IntentExtraTitle, mAppCompatActivity.getResources()
							.getString(R.string.activity_select_task_title_select_correlated_task));
					mAppCompatActivity.startActivityForResult(intent,
							CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE);
				}
			});
			// setup imageButtonRestore
			imageButtonRestore.setTag(position);
			if (checkedId == R.id.activity_import_tasks_recyclerview_item_radio_skip) {
				imageButtonRestore.setEnabled(false);
			} else {
				imageButtonRestore.setEnabled(true);
			}
			imageButtonRestore.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = (Integer) v.getTag();
					int[] selectedRadioButtons = mAdapter.getSelectedRadioButtons();
					ArrayList<Integer> tasksToUpdate = new ArrayList<Integer>();
					ArrayList<Integer> tasksToAdd = new ArrayList<Integer>();
					switch (selectedRadioButtons[position]) {
					case R.id.activity_import_tasks_recyclerview_item_radio_skip:
						break;
					case R.id.activity_import_tasks_recyclerview_item_radio_replace:
						tasksToUpdate.add(position);
						break;
					case R.id.activity_import_tasks_recyclerview_item_radio_add:
						tasksToAdd.add(position);
						break;
					default:
						break;
					}
					setupAndShowConfirmationAlertDialogFragmentExecuteRestoreIfNeeded(tasksToUpdate, tasksToAdd);
				}
			});
			//
			ViewHolder viewHolder = new ViewHolder(mAppCompatActivity, position, linearLayout, tableLayout, radioGroup,
					imageButtonSelectTask, imageButtonRestore);
			updateViewHolder(viewHolder);
			return viewHolder;
		}

		@SuppressLint("InflateParams")
		private void updateViewHolder(ViewHolder viewHolder) {
			int position = viewHolder.position;
			TaskUiData3 taskFromBackupFile = mImportTasksState.TasksFromBackupFile.get(position);
			Integer correlatedTaskFromApp = mImportTasksState.mCorrelatedTasksFromApp.get(position);
			TaskUiData3 taskFromApp = correlatedTaskFromApp == null ? null
					: mImportTasksState.TasksFromApp.get(correlatedTaskFromApp);
			String[] taskFromBackupFileDetails = getTaskDetails(mAppCompatActivity, taskFromBackupFile);
			String[] taskFromAppDetails = getTaskDetails(mAppCompatActivity, taskFromApp);
			TableLayout tableLayout = viewHolder.backupTaskTableLayout;
			tableLayout.removeAllViews();
			for (int r = 0; r < 15; r++) {
				TableRow tableRow = (TableRow) mLayoutInflater
						.inflate(r == 0 ? R.layout.activity_import_tasks_table_row_bold_italic
								: R.layout.activity_import_tasks_table_row, tableLayout, false);
				TextView textView = (TextView) tableRow.getChildAt(0);
				textView.setText(mTaskHeaders[r]);
				textView = (TextView) tableRow.getChildAt(1);
				textView.setText(taskFromBackupFileDetails[r]);
				//
				textView = (TextView) tableRow.getChildAt(2);
				textView.setText(taskFromAppDetails[r]);
				tableLayout.addView(tableRow);
			}
			//
			List<ReminderUiData2> reminderListOfTaskFromBackupFile = taskFromBackupFile.getReminderList();
			List<ReminderUiData2> reminderListOfTaskFromApp = taskFromApp == null ? new ArrayList<ReminderUiData2>()
					: taskFromApp.getReminderList();
			int reminderListSizeFromBackupTask = reminderListOfTaskFromBackupFile.size();
			int reminderListSizeFromAppTask = reminderListOfTaskFromApp.size();
			int maxSize = Math.max(reminderListSizeFromBackupTask, reminderListSizeFromAppTask);
			for (int j = 0; j < maxSize; j++) {
				ReminderUiData2 reminderOfTaskFromBackupFile;
				String[] reminderOfTaskFromBackupFileDetails = null;
				if (j < reminderListSizeFromBackupTask) {
					reminderOfTaskFromBackupFile = reminderListOfTaskFromBackupFile.get(j);
					reminderOfTaskFromBackupFileDetails = getReminderDetails(mAppCompatActivity,
							reminderOfTaskFromBackupFile);
				}
				ReminderUiData2 reminderOfTaskFromApp;
				String[] reminderOfTaskFromAppDetails = null;
				if (j < reminderListSizeFromAppTask) {
					reminderOfTaskFromApp = reminderListOfTaskFromApp.get(j);
					reminderOfTaskFromAppDetails = getReminderDetails(mAppCompatActivity, reminderOfTaskFromApp);
				}
				for (int r1 = 0; r1 < 7; r1++) {
					TableRow tableRow = (TableRow) mLayoutInflater
							.inflate(r1 == 0 ? R.layout.activity_import_tasks_table_row_bold_italic
									: R.layout.activity_import_tasks_table_row, tableLayout, false);
					TextView textView = (TextView) tableRow.getChildAt(0);
					textView.setText(mReminderHeaders[r1]);
					if (j < reminderListSizeFromBackupTask) {
						textView = (TextView) tableRow.getChildAt(1);
						textView.setText(reminderOfTaskFromBackupFileDetails[r1]);
					}
					if (j < reminderListSizeFromAppTask) {
						textView = (TextView) tableRow.getChildAt(2);
						textView.setText(reminderOfTaskFromAppDetails[r1]);
					}
					tableLayout.addView(tableRow);
				}
			}
			//
			RadioButton radioButtonReplace = (RadioButton) viewHolder.radioGroup
					.findViewById(R.id.activity_import_tasks_recyclerview_item_radio_replace);
			if (correlatedTaskFromApp == null) {
				radioButtonReplace.setEnabled(false);
			} else {
				radioButtonReplace.setEnabled(true);
			}
		}

		private void updateTaskFromAppByTaskFromBackupFile(int position) {
			TaskUiData3 taskFromBackupFile = mImportTasksState.TasksFromBackupFile.get(position);
			Integer indexOfTaskFromApp = mImportTasksState.mCorrelatedTasksFromApp.get(position);
			if (indexOfTaskFromApp == null) {
				TaskUiData3 updatedTask = new TaskUiData3(taskFromBackupFile);
				updatedTask.setId(null);
				updatedTask.setDeleted(false);
				TaskWithDependents generatedTaskWithDependents = updatedTask.generateTaskWithDependents(null,
						mAppCompatActivity);
				DataProvider.insertOrReplaceTaskWithDependents(null, mAppCompatActivity, generatedTaskWithDependents,
						true);
				// mImportTasksState.TasksFromApp.set(indexOfTaskFromApp,
				// updatedTask);
				// int tasksFromBackupFileSize =
				// mImportTasksState.TasksFromBackupFile
				// .size();
				// for (int i = 0; i < tasksFromBackupFileSize; i++) {
				// if (mImportTasksState.mCorrelatedTasksFromApp.get(i) ==
				// indexOfTaskFromApp) {
				// updateTableLayoutInViewHolderForPosition(i);
				// notifyItemChanged(i);
				// }
				// }
			} else {
				TaskUiData3 taskFromApp = mImportTasksState.TasksFromApp.get(indexOfTaskFromApp);
				TaskUiData3 updatedTask = new TaskUiData3(taskFromBackupFile);
				updatedTask.setId(taskFromApp.getId());
				updatedTask.setDeleted(false);
				TaskWithDependents generatedTaskWithDependents = updatedTask.generateTaskWithDependents(null,
						mAppCompatActivity);
				DataProvider.insertOrReplaceTaskWithDependents(null, mAppCompatActivity, generatedTaskWithDependents,
						true);
				mImportTasksState.TasksFromApp.set(indexOfTaskFromApp, updatedTask);
				int tasksFromBackupFileSize = mImportTasksState.TasksFromBackupFile.size();
				for (int i = 0; i < tasksFromBackupFileSize; i++) {
					if (mImportTasksState.mCorrelatedTasksFromApp.get(i) == indexOfTaskFromApp) {
						updateViewHolder(mViewHolders[i]);
						notifyItemChanged(i);
					}
				}
			}
			LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(ActivityImportTasks.this);
			mBroadcaster.sendBroadcast(new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			mBroadcaster.sendBroadcast(new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			reconcileRadiobuttonsWithPosition(position);
			notifyItemChanged(position);
		}

		private void insertTaskFromBackupFile(int position) {
			TaskUiData3 taskFromBackupFile = mImportTasksState.TasksFromBackupFile.get(position);
			final TaskUiData3 newTask = new TaskUiData3(taskFromBackupFile);
			newTask.setId(null);
			DataProvider.runInTx(null, mAppCompatActivity, new Runnable() {
				@Override
				public void run() {
					boolean wasMarkedAsDeleted = newTask.isDeleted();
					if (newTask.getParentId() != null) {
						Task parentTask = DataProvider.getTask(null, mAppCompatActivity, newTask.getParentId(), false);
						if (parentTask != null) {
							if (!wasMarkedAsDeleted) {
								// set consistency with parent task
								DataProvider.undeleteTask(null, mAppCompatActivity, newTask.getParentId());
							}
						} else {
							newTask.setParentId(null);
						}
					}
					newTask.setDeleted(false);
					TaskWithDependents generatedTaskWithDependents = newTask.generateTaskWithDependents(null,
							mAppCompatActivity);
					DataProvider.insertOrReplaceTaskWithDependents(null, mAppCompatActivity,
							generatedTaskWithDependents, true);
					if (wasMarkedAsDeleted) {
						if (newTask.getParentId() != null) {
							// set consistency with parent task
							DataProvider.markTasksAsDeleted(null, mAppCompatActivity, new long[] { newTask.getId() },
									false);
						}
					}
				}
			});
			LocalBroadcastManager mBroadcaster = LocalBroadcastManager.getInstance(ActivityImportTasks.this);
			mBroadcaster.sendBroadcast(new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			mBroadcaster.sendBroadcast(new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			ViewHolder viewHolder = mViewHolders[viewType];
			viewHolder.setParent(parent);
			// viewHolder.itemView.invalidate();
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			//
		}

		private String[] getTaskDetails(Context context, TaskUiData3 taskUiData3) {
			String[] taskDetails = new String[15];
			if (taskUiData3 == null) {
				return taskDetails;
			}
			try {
				// fill task text row
				taskDetails[0] = taskUiData3.getName();
				// task_start
				DateFormat dateFormat = DateFormat.getDateTimeInstance();
				if (taskUiData3.getStartDateTime() != null) {
					String dateString = dateFormat.format(new Date(taskUiData3.getStartDateTime()));
					taskDetails[1] = dateString;
				} else {
					taskDetails[1] = context.getResources()
							.getString(R.string.fragment_view_task_part_main_value_textview_task_start_date_not_set);
				}
				// task_end
				if (taskUiData3.getEndDateTime() != null) {
					String dateString = dateFormat.format(new Date(taskUiData3.getEndDateTime()));
					taskDetails[2] = dateString;
				} else {
					taskDetails[2] = context.getResources()
							.getString(R.string.fragment_view_task_part_main_value_textview_task_end_date_not_set);
				}
				// task_type
				switch (RecurrenceInterval.fromInt(taskUiData3.getRecurrenceIntervalValue())) {
				case ONE_TIME:
				default:
					taskDetails[3] = context.getResources()
							.getString(R.string.fragment_view_task_value_textview_is_recurrent_no);
					break;
				case DAYS:
				case HOURS:
				case MINUTES:
				case MONTHS_ON_DATE:
				case MONTHS_ON_NTH_WEEK_DAY:
				case WEEKS:
				case YEARS:
					taskDetails[3] = context.getResources()
							.getString(R.string.fragment_view_task_value_textview_is_recurrent_yes);
					break;
				}
				// hours_required
				// tableRow = (TableRow)
				// LayoutInflater.from(getActivity()).inflate(
				// R.layout.activity_import_tasks_table_row, null);
				// textView = (TextView) tableRow
				// .findViewById(R.id.activity_import_tasks_table_row_left_textview);
				// taskDetails[0]=(context.getResources().getString(
				// R.string.fragment_view_task_header_textview_task_hours_required));
				// textView = (TextView) tableRow
				// .findViewById(textViewId);
				// taskDetails[0]=("" + taskWithDependents.getRequiredLength());
				//
				// // hours_spent
				// tableRow = (TableRow)
				// LayoutInflater.from(getActivity()).inflate(
				// R.layout.activity_import_tasks_table_row, null);
				// textView = (TextView) tableRow
				// .findViewById(R.id.activity_import_tasks_table_row_left_textview);
				// taskDetails[0]=(context.getResources().getString(
				// R.string.fragment_view_task_header_textview_task_hours_spent));
				// textView = (TextView) tableRow
				// .findViewById(textViewId);
				// taskDetails[0]=("" + taskWithDependents.getActualLength());
				//
				// repetition_every
				switch (RecurrenceInterval.fromInt(taskUiData3.getRecurrenceIntervalValue())) {
				case ONE_TIME:
					taskDetails[4] = context.getResources()
							.getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_not_set);
					break;
				case MINUTES:
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = context.getResources()
								.getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_minute);
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_minutes),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbers(context),
								taskUiData3.getTimeUnitsStartingIndexValue());
					}
					break;
				case HOURS:
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = context.getResources()
								.getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_hour);
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_hours),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbers(context),
								taskUiData3.getTimeUnitsStartingIndexValue());
					}
					break;
				case DAYS:
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = context.getResources()
								.getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_day);
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_days),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbers(context),
								taskUiData3.getTimeUnitsStartingIndexValue());
					}
					break;
				case WEEKS:
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_week),
								taskUiData3.getDelimitedSequenceOfWeekDays(context, false));
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_weeks),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfWeekDays(context, false));
					}
					break;
				case MONTHS_ON_DATE:
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_month_on_date),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbersForMonth(context));
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_months_on_date),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbersForMonth(context));
					}
					break;
				case MONTHS_ON_NTH_WEEK_DAY:
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_month_on_nth_week_day),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(context),
								taskUiData3.getDelimitedSequenceOfWeekDays(context, true));
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_months_on_nth_week_day),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfOrdinalNumbersOfOccurrencesOfWeekdaysInMonth(context),
								taskUiData3.getDelimitedSequenceOfWeekDays(context, true));
					}
					break;
				case YEARS:
					taskDetails[4] = taskUiData3.getTimeUnitsCount() == 1
							? context.getResources()
									.getString(R.string.fragment_view_task_value_textview_task_recurrence_interval_year)
							: taskUiData3.getTimeUnitsCount() + context.getResources().getString(
									R.string.fragment_view_task_value_textview_task_recurrence_interval_years);
					if (taskUiData3.getTimeUnitsCount() == 1) {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_year),
								taskUiData3.getDelimitedSequenceOfDatesOfYear(context));
					} else {
						taskDetails[4] = String.format(
								context.getResources().getString(
										R.string.fragment_view_task_value_textview_task_recurrence_interval_years),
								taskUiData3.getTimeUnitsCount(),
								taskUiData3.getDelimitedSequenceOfDatesOfYear(context));
					}
					break;
				default:
					break;
				}
				// no_more_than
				if (taskUiData3.getRecurrenceIntervalValue() != RecurrenceInterval.ONE_TIME.getValue()) {
					if (taskUiData3.getOccurrencesMaxCount() != null) {
						taskDetails[5] = "" + taskUiData3.getOccurrencesMaxCount();
					} else {
						taskDetails[5] = context.getResources().getString(
								R.string.fragment_view_task_header_textview_task_repetition_no_more_than_not_set);
					}
					// until_date
					if (taskUiData3.getRepetitionEndDateTime() != null) {
						String dateString = dateFormat.format(new Date(taskUiData3.getRepetitionEndDateTime()));
						taskDetails[6] = dateString;
					} else {
						taskDetails[6] = context.getResources().getString(
								R.string.fragment_view_task_value_textview_task_repetition_until_date_not_set);
					}
				} else {
					taskDetails[5] = "";
					taskDetails[6] = "";
				}
				// fill parent_task row
				taskDetails[7] = taskUiData3.getParentId() == null
						? context.getResources()
								.getString(R.string.fragment_view_task_value_textview_parent_task_not_set)
						: taskUiData3.getParentTaskName();
				// fill sort_order row
				if (taskUiData3.getSortOrderSiblingName() != null) {
					taskDetails[8] = context.getResources()
							.getString(R.string.fragment_view_task_value_textview_sort_order_aftertask) + " "
							+ taskUiData3.getSortOrderSiblingName();
				} else {
					taskDetails[8] = context.getResources()
							.getString(R.string.fragment_view_task_value_textview_sort_order_first);
				}
				// fill completed_percent row
				// ProgressBar progressBar = (ProgressBar) context
				// .findViewById(R.id.fragment_view_task_progressbar_completed_percent);
				// progressBar.setProgress(t.getPercentOfCompletion());
				taskDetails[9] = taskUiData3.getIsCompleted()
						? context.getResources().getString(R.string.fragment_view_task_value_textview_is_completed_yes)
						: context.getResources().getString(R.string.fragment_view_task_value_textview_is_completed_no);
				String text = String.format(
						context.getResources()
								.getString(R.string.fragment_view_task_value_textview_percent_of_completion),
						taskUiData3.getPercentOfCompletion());
				taskDetails[10] = text;
				//
				// priority
				switch (Task.PRIORITY.fromInt(taskUiData3.getPriority())) {
				case HIGH:
					taskDetails[11] = context.getResources()
							.getString(R.string.fragment_view_task_part_main_value_textview_task_priority_high);
					break;
				case LOW:
					taskDetails[11] = context.getResources()
							.getString(R.string.fragment_view_task_part_main_value_textview_task_priority_low);
					break;
				case MEDIUM:
					taskDetails[11] = context.getResources()
							.getString(R.string.fragment_view_task_part_main_value_textview_task_priority_medium);
					break;
				default:
					break;
				}
				//
				// color
				taskDetails[12] = Integer.toHexString(taskUiData3.getColor2(context));
				// notes
				taskDetails[13] = taskUiData3.getDescription();
				//
				taskDetails[14] = taskUiData3.isDeleted()
						? context.getResources().getString(R.string.fragment_view_task_value_textview_is_deleted_yes)
						: context.getResources().getString(R.string.fragment_view_task_value_textview_is_deleted_no);
			} catch (NullPointerException e) {
				Log.wtf(CommonConstants.WTF_TAG, "taskId = " + taskUiData3.getId() + " and t = " + taskUiData3 + " in "
						+ getClass() + " class in " + new Object() {
						}.getClass().getEnclosingMethod().getName() + " method", e);
			}
			return taskDetails;
		}

		private String[] getReminderDetails(Context context, ReminderUiData2 reminder) {
			String[] reminderDetails = new String[7];
			try {
				//
				Resources resources = mAppCompatActivity.getResources();
				reminderDetails[0] = reminder.getText();
				reminderDetails[1] = reminder.getEnabled()
						? resources.getString(
								R.string.fragment_view_task_part_reminders_value_textview_reminder_state_enabled)
						: resources.getString(
								R.string.fragment_view_task_part_reminders_value_textview_reminder_state_disabled);
				// setup reminder type
				reminderDetails[2] = reminder.getIsAlarm()
						? resources.getString(
								R.string.fragment_view_task_part_reminders_value_textview_reminder_type_alarm)
						: resources.getString(
								R.string.fragment_view_task_part_reminders_value_textview_reminder_type_notification);
				// setup reminder time type
				String text = null;
				switch (ReminderTimeMode.fromInt(reminder.getReminderTimeModeValue())) {
				case ABSOLUTE_TIME:
					text = resources.getString(
							R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_absolute_time);
					break;
				case AFTER_NOW:
					text = resources.getString(
							R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_absolute_time);
					break;
				case TIME_BEFORE_EVENT:
					text = resources.getString(
							R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_time_before_event);
					break;
				case TIME_AFTER_EVENT:
					text = resources.getString(
							R.string.fragment_view_task_part_reminders_value_textview_reminder_time_mode_time_after_event);
					break;
				default:
					text = "";
					break;
				}
				reminderDetails[3] = text;
				// setup reminder type absolute (time and date buttons)
				// setup reminder type offset
				if (reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME.getValue()) {
					// Calendar calendar = Calendar.getInstance();
					long now = System.currentTimeMillis();
					if (reminder.getReminderTimeModeValue() == ReminderTimeMode.AFTER_NOW.getValue()) {
						reminder.setReminderDateTime(now);
					}
					long absRemindOffset = Math.abs(reminder.getReminderDateTime());
					Integer days = (int) (absRemindOffset / (1000L * 60 * 60 * 24));
					Integer hours = (int) (absRemindOffset / (1000L * 60 * 60) % 24);
					Integer minutes = (int) (absRemindOffset / (1000L * 60) % 60);
					Long nextReminderDateTime = reminder.getNextReminderDateTime(mAppCompatActivity, now, true,
							Helper.getFirstDayOfWeek(mAppCompatActivity));
					String timeText;
					if (nextReminderDateTime != null) {
						// calendar.setTimeInMillis(nextReminderDateTime);
						DateFormat mDateFormat = DateFormat.getDateTimeInstance();
						timeText = mDateFormat.format(new Date(nextReminderDateTime));
					} else {
						timeText = resources.getString(
								R.string.fragment_view_task_part_reminders_value_textview_reminder_time_next_reminder_time_none);
					}
					String textNextAlarmTime = String.format(
							resources.getString(R.string.fragment_view_task_part_reminders_text_next_alarm_time),
							timeText);
					String nextReminderTimeString = (days == 0 ? ""
							: days + " "
									+ resources.getString(
											R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_days)
									+ " ")
							+ (hours == 0 ? ""
									: hours + " "
											+ resources.getString(
													R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_hours)
											+ " ")
							+ (minutes == 0 ? ""
									: minutes + " "
											+ resources.getString(
													R.string.fragment_view_task_part_reminders_value_textview_reminder_time_remind_before_after_minutes)
											+ " ")
							+ (reminder.getReminderDateTime() == 0
									? resources.getString(
											R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_at_task_start)
									: reminder.getReminderDateTime() > 0
											&& reminder.getReminderTimeModeValue() == ReminderTimeMode.TIME_BEFORE_EVENT
													.getValue()
											|| reminder.getReminderDateTime() < 0 && reminder
													.getReminderTimeModeValue() == ReminderTimeMode.TIME_AFTER_EVENT
															.getValue()
																	? resources.getString(
																			R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_before_task_start)
																	: resources.getString(
																			R.string.fragment_view_task_part_reminders_value_textview_reminder_time_type_after_task_start));
					reminderDetails[4] = nextReminderTimeString + ". " + textNextAlarmTime;
				} else {
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(reminder.getReminderDateTime());
					DateFormat mDateFormat = DateFormat.getDateTimeInstance();
					String dateString = mDateFormat.format(new Date(calendar.getTimeInMillis()));
					reminderDetails[4] = dateString;
				}
				// setup RemindEachIteration
				reminderDetails[5] = context.getResources()
						.getString(reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME.getValue()
								? R.string.fragment_view_task_part_reminders_value_textview_is_recurrent_reminder_yes
								: R.string.fragment_view_task_part_reminders_value_textview_is_recurrent_reminder_no);
				// setup IsRingtoneCustom
				text = String.format(
						resources
								.getString(R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone),
						reminder.getRingtone() == null
								? resources.getString(
										R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_from_task)
								: resources.getString(
										R.string.fragment_view_task_part_reminders_value_textview_reminder_ringtone_custom),
						reminder.getRingtoneTitle(mAppCompatActivity));
				reminderDetails[6] = text;
			} catch (NullPointerException e) {
				Log.wtf(CommonConstants.WTF_TAG, "taskId = " + reminder.getId() + " and t = " + reminder + " in "
						+ getClass() + " class in " + new Object() {
						}.getClass().getEnclosingMethod().getName() + " method", e);
			}
			return reminderDetails;
		}

		private String[] getTaskHeaders(Context context) {
			String[] taskHeaders = new String[15];
			// fill task text row
			taskHeaders[0] = context.getResources().getString(R.string.fragment_view_task_header_textview_task_name);
			// task_start
			taskHeaders[1] = context.getResources().getString(R.string.fragment_view_task_header_textview_task_start);
			// task_end
			taskHeaders[2] = context.getResources().getString(R.string.fragment_view_task_header_textview_task_end);
			// task_type
			taskHeaders[3] = context.getResources().getString(R.string.fragment_view_task_header_textview_is_recurrent);
			// hours_required
			// tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(
			// R.layout.activity_import_tasks_table_row, null);
			// textView = (TextView) tableRow
			// .getChildAt(0);
			// taskHeaders[0]=(context.getResources().getString(
			// R.string.fragment_view_task_header_textview_task_hours_required));
			// textView = (TextView) tableRow
			// .findViewById(textViewId);
			// taskHeaders[0]=("" + task.getRequiredLength());
			//
			// // hours_spent
			// tableRow = (TableRow) LayoutInflater.from(getActivity()).inflate(
			// R.layout.activity_import_tasks_table_row, null);
			// textView = (TextView) tableRow
			// .getChildAt(0);
			// taskHeaders[0]=(context.getResources().getString(
			// R.string.fragment_view_task_header_textview_task_hours_spent));
			// textView = (TextView) tableRow
			// .findViewById(textViewId);
			// taskHeaders[0]=("" + task.getActualLength());
			//
			// repetition_every
			taskHeaders[4] = context.getResources()
					.getString(R.string.fragment_view_task_header_textview_task_recurrence_interval);
			// no_more_than
			taskHeaders[5] = context.getResources()
					.getString(R.string.fragment_view_task_header_textview_task_repetition_no_more_than);
			// until_date
			taskHeaders[6] = context.getResources()
					.getString(R.string.fragment_view_task_header_textview_task_repetition_until_date);
			// fill parent_task row
			taskHeaders[7] = context.getResources().getString(R.string.fragment_view_task_header_textview_parent_task);
			// fill sort_order row
			taskHeaders[8] = context.getResources().getString(R.string.fragment_view_task_header_textview_sort_order);
			taskHeaders[9] = context.getResources().getString(R.string.fragment_view_task_header_textview_is_completed);
			taskHeaders[10] = context.getResources()
					.getString(R.string.fragment_view_task_header_textview_percent_of_completion);
			//
			// priority
			taskHeaders[11] = context.getResources().getString(R.string.fragment_view_task_header_textview_priority);
			//
			// color
			taskHeaders[12] = context.getResources().getString(R.string.fragment_view_task_header_textview_color);
			// notes
			taskHeaders[13] = context.getResources().getString(R.string.fragment_view_task_header_textview_task_notes);
			taskHeaders[14] = context.getResources().getString(R.string.fragment_view_task_header_textview_is_deleted);
			return taskHeaders;
		}

		private String[] getReminderHeaders(Context context) {
			String[] reminderHeaders = new String[7];
			Resources resources = mAppCompatActivity.getResources();
			// fill task text row
			reminderHeaders[0] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_reminder_name);
			reminderHeaders[1] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_state);
			// setup reminder type
			reminderHeaders[2] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_alarm_mode);
			// setup reminder time type
			reminderHeaders[3] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_time_mode);
			// setup reminder type absolute (time and date buttons)
			// setup reminder type offset
			reminderHeaders[4] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_time);
			// setup RemindEachIteration
			reminderHeaders[5] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_is_recurrent_reminder);
			// setup IsRingtoneCustom
			reminderHeaders[6] = resources
					.getString(R.string.fragment_view_task_part_reminders_header_textview_reminder_ringtone);
			return reminderHeaders;
		}

		@Override
		public int getItemCount() {
			return mImportTasksState.TasksFromBackupFile.size();
		}

		@Override
		public int getItemViewType(int position) {
			return position;
		}

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			int position = (Integer) group.getTag();
			mImportTasksState.mSelectedRadioButtons[position] = checkedId;
			if (checkedId == R.id.activity_import_tasks_recyclerview_item_radio_skip) {
				mViewHolders[position].imageButtonRestore.setEnabled(false);
			} else {
				mViewHolders[position].imageButtonRestore.setEnabled(true);
				reconcileRadiobuttonsWithPosition(position);
			}
			// mViewHolders[position].imageButtonRestore.invalidate();
			notifyItemChanged(position);
		}

		private void reconcileRadiobuttonsWithPosition(int position) {
			if (mImportTasksState.mSelectedRadioButtons[position] == R.id.activity_import_tasks_recyclerview_item_radio_replace) {
				Integer correlatedTaskFromApp = mImportTasksState.mCorrelatedTasksFromApp.get(position);
				if (correlatedTaskFromApp == null) {
					return;
				}
				for (int i = 0; i < mImportTasksState.mSelectedRadioButtons.length; i++) {
					int selectedRadioButton = mImportTasksState.mSelectedRadioButtons[i];
					if (selectedRadioButton == R.id.activity_import_tasks_recyclerview_item_radio_replace) {
						int correlatedTaskFromApp2 = mImportTasksState.mCorrelatedTasksFromApp.get(i);
						if (i != position && correlatedTaskFromApp == correlatedTaskFromApp2) {
							int selectedRadioButtonId = mImportTasksState.mSelectedRadioButtons[i] = R.id.activity_import_tasks_recyclerview_item_radio_skip;
							mViewHolders[i].radioGroup.setOnCheckedChangeListener(null);
							mViewHolders[i].radioGroup.check(selectedRadioButtonId);
							mViewHolders[i].radioGroup.setOnCheckedChangeListener(this);
							if (selectedRadioButtonId == R.id.activity_import_tasks_recyclerview_item_radio_skip) {
								mViewHolders[i].imageButtonRestore.setEnabled(false);
							} else {
								mViewHolders[i].imageButtonRestore.setEnabled(true);
							}
							notifyItemChanged(i);
						}
					}
				}
			}
		}

		public int[] getSelectedRadioButtons() {
			return mImportTasksState.mSelectedRadioButtons;
		}

		public void setCorrelatedTask(int position, Long id) {
			List<TaskUiData3> TasksFromApp = mImportTasksState.TasksFromApp;
			Integer index = null;
			if (id != null) {
				for (int i = 0; i < TasksFromApp.size(); i++) {
					TaskUiData3 taskUiData2 = TasksFromApp.get(i);
					if (taskUiData2.getId() == id) {
						index = i;
						break;
					}
				}
			}
			mImportTasksState.mCorrelatedTasksFromApp.set(position, index);
			updateViewHolder(mViewHolders[position]);
			reconcileRadiobuttonsWithPosition(position);
			notifyItemChanged(position);
		}

		public void executeBatchRestore(ArrayList<Integer> tasksToUpdate, ArrayList<Integer> tasksToAdd) {
			for (Integer position : tasksToUpdate) {
				updateTaskFromAppByTaskFromBackupFile(position);
			}

			Map<Long, Node1> node1Map = new HashMap<Long, Node1>();
			ArrayList<Node1> node1List = new ArrayList<Node1>();

			for (Integer position : tasksToAdd) {
				TaskUiData3 taskFromBackupFile = mImportTasksState.TasksFromBackupFile.get(position);
				Node1 node = new Node1(taskFromBackupFile.getId(), taskFromBackupFile.getParentId(), node1Map,
						position);
				node1List.add(node);
				node1Map.put(taskFromBackupFile.getId(), node);
			}

			Collections.sort(node1List);

			for (Node1 node : node1List) {
				insertTaskFromBackupFile(node.getPosition());
			}

			Toast.makeText(mAppCompatActivity,
					R.string.action_restore_tasks_from_backup_file_tasks_restored_successfully, Toast.LENGTH_LONG)
					.show();
		}
	}

	private static final String ARG_TASKS_TO_UPDATE = "ARG_TASKS_TO_UPDATE";
	private static final String ARG_TASKS_TO_ADD = "ARG_TASKS_TO_ADD";
	private Toolbar mToolbar;
	private RecyclerView mRecyclerView;
	private RetainedFragmentForActivityImportTasks mDataFragment;
	private LinearLayoutManager mLinearLayoutManager;
	private ActivityImportTasksAdapter mAdapter;
	private YesNoListener mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_import_tasks);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(getTitle());
		mRecyclerView = (RecyclerView) findViewById(R.id.activity_import_tasks_recyclerview);
		mRecyclerView.setHasFixedSize(true);
		mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		//
		FragmentManager fm = getSupportFragmentManager();
		mDataFragment = (RetainedFragmentForActivityImportTasks) fm.findFragmentByTag(CommonConstants.DATA_FRAGMENT);
		if (mDataFragment == null) {
			// create the fragment and data the first time
			String path = getIntent().getStringExtra("path");
			List<TaskUiData3> tasksFromBackupFile = null;
			Exception e1 = null;
			try {
				tasksFromBackupFile = DataProvider.getTasksFromBackupFile(this, path);
			} catch (SQLiteException e) {
				e1 = e;
			} catch (DowngradeException e) {
				e1 = e;
			} catch (IOException e) {
				e1 = e;
			}
			if (e1 != null) {
				// wrong code, causes null pointer exception
				// if (isTaskRoot()) {
				// ActivityExit.exitApplicationAndRemoveFromRecent(context);
				// } else {
				// finish();
				// }
				//
				finish();
				if (isTaskRoot()) {
					ActivityExit.exitApplicationAndRemoveFromRecent(this);
				}
				// toast
				Toast.makeText(this, getResources().getString(R.string.toast_text_unable_to_load_tasks_from_file)
						+ ":\n" + e1.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				return;
			}
			if (tasksFromBackupFile.size() == 0) {
				// wrong code, causes null pointer exception
				// if (isTaskRoot()) {
				// ActivityExit.exitApplicationAndRemoveFromRecent(context);
				// } else {
				// finish();
				// }
				//
				finish();
				if (isTaskRoot()) {
					ActivityExit.exitApplicationAndRemoveFromRecent(this);
				}
				// toast
				Toast.makeText(this, R.string.toast_text_there_are_no_tasks_in_the_backup_file, Toast.LENGTH_LONG)
						.show();
				return;
			}
			// add the fragment
			mDataFragment = new RetainedFragmentForActivityImportTasks();
			fm.beginTransaction().add(mDataFragment, CommonConstants.DATA_FRAGMENT).commit();
			List<TaskUiData3> tasksFromApp = DataProvider.getTaskUiData3ListOfAllTasks(null, this);
			mDataFragment.ImportTasksState = new ImportTasksState(tasksFromBackupFile, tasksFromApp);
		}
		mAdapter = new ActivityImportTasksAdapter(this, mDataFragment.ImportTasksState, mLinearLayoutManager);
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// setup listener
		ConfirmationAlertDialogFragment confirmationAlertDialogFragment = (ConfirmationAlertDialogFragment) getSupportFragmentManager()
				.findFragmentByTag("ConfirmationAlertDialogFragmentExecuteBatchRestore");
		if (confirmationAlertDialogFragment != null) {
			setupConfirmationAlertDialogFragmentExecuteBatchRestoreYesNoListener(confirmationAlertDialogFragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_RESTORE, 0,
				getResources().getString(R.string.action_batch_restore));
		menuItem.setIcon(R.drawable.ic_file_download_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_RESTORE:
			int[] selectedRadioButtons = mAdapter.getSelectedRadioButtons();
			ArrayList<Integer> tasksToUpdate = new ArrayList<Integer>();
			ArrayList<Integer> tasksToAdd = new ArrayList<Integer>();
			for (int position = 0; position < selectedRadioButtons.length; position++) {
				switch (selectedRadioButtons[position]) {
				case R.id.activity_import_tasks_recyclerview_item_radio_skip:
					break;
				case R.id.activity_import_tasks_recyclerview_item_radio_replace:
					tasksToUpdate.add(position);
					break;
				// case 2:
				// break;
				case R.id.activity_import_tasks_recyclerview_item_radio_add:
					tasksToAdd.add(position);
					break;
				default:
					break;
				}
			}
			setupAndShowConfirmationAlertDialogFragmentExecuteRestoreIfNeeded(tasksToUpdate, tasksToAdd);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setupAndShowConfirmationAlertDialogFragmentExecuteRestoreIfNeeded(ArrayList<Integer> tasksToUpdate,
			ArrayList<Integer> tasksToAdd) {
		int tasksCountToUpdate = tasksToUpdate.size();
		int tasksCountToAdd = tasksToAdd.size();
		if (tasksCountToUpdate == 0 && tasksCountToAdd == 0) {
			Toast.makeText(this,
					R.string.action_restore_tasks_from_backup_file_no_tasks_selected_to_replace_or_add_from_backup_file,
					Toast.LENGTH_LONG).show();
			return;
		}
		String tasksCountToUpdateStr = "";
		String andStr = "";
		String tasksCountToAddStr = "";
		Resources resources = getResources();
		if (tasksCountToUpdate > 0 && tasksCountToAdd == 0) {
			tasksCountToUpdateStr = String.format(
					resources.getString(R.string.action_restore_tasks_from_backup_file__replace_tasks),
					"" + tasksCountToUpdate);
		} else if (tasksCountToUpdate == 0 && tasksCountToAdd > 0) {
			tasksCountToAddStr = String.format(
					resources.getString(R.string.action_restore_tasks_from_backup_file__add_tasks),
					"" + tasksCountToAdd);
		} else {
			tasksCountToUpdateStr = String.format(
					resources.getString(R.string.action_restore_tasks_from_backup_file__replace_tasks),
					"" + tasksCountToUpdate);
			tasksCountToAddStr = String.format(
					resources.getString(R.string.action_restore_tasks_from_backup_file__add_tasks),
					"" + tasksCountToAdd);
			andStr = " " + resources.getString(R.string.action_restore_tasks_from_backup_file__and) + " ";
		}
		ConfirmationAlertDialogFragment confirmationAlertDialogFragmentExecuteRestore = new ConfirmationAlertDialogFragment();
		Bundle arguments = new Bundle();
		arguments.putIntegerArrayList(ActivityImportTasks.ARG_TASKS_TO_UPDATE, tasksToUpdate);
		arguments.putIntegerArrayList(ActivityImportTasks.ARG_TASKS_TO_ADD, tasksToAdd);
		confirmationAlertDialogFragmentExecuteRestore.setArguments(arguments);
		confirmationAlertDialogFragmentExecuteRestore.setTitleAndMessage(null,
				resources.getString(R.string.action_restore_tasks_from_backup_file__you_are_about_to) + " "
						+ tasksCountToUpdateStr + andStr + tasksCountToAddStr + " "
						+ resources.getString(R.string.action_restore_tasks_from_backup_file__from_backup_file) + "\n"
						+ resources.getString(R.string.action_restore_tasks_from_backup_file__do_you_want_to_continue));
		setupConfirmationAlertDialogFragmentExecuteBatchRestoreYesNoListener(
				confirmationAlertDialogFragmentExecuteRestore);
		confirmationAlertDialogFragmentExecuteRestore.show(getSupportFragmentManager(),
				"ConfirmationAlertDialogFragmentExecuteBatchRestore");
	}

	private void setupConfirmationAlertDialogFragmentExecuteBatchRestoreYesNoListener(
			ConfirmationAlertDialogFragment confirmationAlertDialogFragment) {
		mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener = new ConfirmationAlertDialogFragment.YesNoListener() {
			@Override
			public void onYesInConfirmationAlertDialogFragment(ConfirmationAlertDialogFragment dialogFragment) {
				Bundle arguments = dialogFragment.getArguments();
				ArrayList<Integer> tasksToUpdate = arguments
						.getIntegerArrayList(ActivityImportTasks.ARG_TASKS_TO_UPDATE);
				ArrayList<Integer> tasksToAdd = arguments.getIntegerArrayList(ActivityImportTasks.ARG_TASKS_TO_ADD);
				mAdapter.executeBatchRestore(tasksToUpdate, tasksToAdd);
			}

			@Override
			public void onNoInConfirmationAlertDialogFragment(ConfirmationAlertDialogFragment dialogFragment) {
				// TODO Auto-generated method stub
			}
		};
		confirmationAlertDialogFragment.setCallback(mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Check which request it is we're responding to
		switch (requestCode) {
		case CommonConstants.REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE:
			if (resultCode == Activity.RESULT_OK) {
				int position = intent.getIntExtra(ActivityImportTasksAdapter.IntentExtraPosition, 0);
				long[] idArray = intent.getLongArrayExtra(CommonConstants.ID_ARRAY);
				if (idArray.length > 0) {
					mAdapter.setCorrelatedTask(position, idArray[0]);
				}
			}
			break;
		default:
			break;
		}
	}
}
