package biz.advancedcalendar.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.activities.activityedittask.TaskUiData2;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.ConfirmationAlertDialogFragment.YesNoListener;
import biz.advancedcalendar.greendao.Task;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/** A list fragment representing a list of Calendars. This fragment also supports tablet
 * devices by allowing list items to be given an 'activated' state upon selection. This
 * helps indicate which item is currently being viewed in a {@link FragmentCalendarDetail}
 * .
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface. */
public class FragmentRecyclebinList extends ListFragment implements
		ConfirmationAlertDialogFragment.YesNoListener {
	/** The serialization (saved instance state) Bundle key representing the activated item
	 * position. Only used on tablets. */
	/** The fragment's current callback object, which is notified of list item clicks. */
	private Callbacks mCallbacks = FragmentRecyclebinList.sDummyCallbacks;
	/** The current activated item position. Only used on tablets. */
	// private int mActivatedPosition = ListView.INVALID_POSITION;
	// private ArrayList<TaskWithDependents> mTasks;
	private BroadcastReceiver mReceiver;
	private StateParameters mStateParameters;

	private static class StateParameters implements Parcelable {
		protected TreeMap<Integer, TaskUiData2> SelectedTasks;
		protected boolean TasksNeedReload;
		protected boolean IsActionMode;

		protected StateParameters(Parcel in) {
			if (in.readByte() == 0x01) {
				SelectedTasks = new TreeMap<Integer, TaskUiData2>();
				int size = in.readInt();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						int key = in.readInt();
						TaskUiData2 taskUiData2;
						if (in.readByte() == 0x01) {
							taskUiData2 = (TaskUiData2) in.readValue(TaskUiData2.class
									.getClassLoader());
						} else {
							taskUiData2 = null;
						}
						SelectedTasks.put(key, taskUiData2);
					}
				}
			} else {
				SelectedTasks = null;
			}
			TasksNeedReload = in.readByte() != 0x00;
			IsActionMode = in.readByte() != 0x00;
		}

		public StateParameters() {
			// TODO Auto-generated constructor stub
		}

		public StateParameters(TreeMap<Integer, TaskUiData2> selectedTasks,
				boolean tasksNeedReload, boolean isActionMode) {
			super();
			SelectedTasks = selectedTasks;
			TasksNeedReload = tasksNeedReload;
			IsActionMode = isActionMode;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (SelectedTasks == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(SelectedTasks.size());
				Iterator<Entry<Integer, TaskUiData2>> iterator = SelectedTasks.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<Integer, TaskUiData2> entry = iterator.next();
					dest.writeInt(entry.getKey());
					TaskUiData2 value = entry.getValue();
					if (value == null) {
						dest.writeByte((byte) 0x00);
					} else {
						dest.writeByte((byte) 0x01);
						dest.writeValue(value);
					}
				}
			}
			dest.writeByte((byte) (TasksNeedReload ? 0x01 : 0x00));
			dest.writeByte((byte) (IsActionMode ? 0x01 : 0x00));
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

	/** A callback interface that all activities containing this fragment must implement.
	 * This mechanism allows activities to be notified of item selections. */
	public interface Callbacks {
		/** Callback for when an item has been selected. */
		public void onItemsSelected(ArrayList<TaskUiData2> taskWithDependentsList);

		public void onActionModeStart();

		public void onActionModeEnd();
	}

	/** A dummy implementation of the {@link Callbacks} interface that does nothing. Used
	 * only when this fragment is not attached to an activity. */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemsSelected(ArrayList<TaskUiData2> taskWithDependentsList) {
		}

		@Override
		public void onActionModeStart() {
		}

		@Override
		public void onActionModeEnd() {
		}
	};
	private ListView mListview;
	private FragmentRecyclebinListAdapter mAdapter;
	// protected ActionMode mActionMode;
	protected YesNoListener mConfirmationAlertDialogFragmentUndeleteYesNoListener;
	protected YesNoListener mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener;
	private ArrayList<TaskUiData2> mTaskWithDependentsList;

	// private ArrayList<TaskUiData2> mSelectedTaskWithDependentsList;
	/** Mandatory empty constructor for the fragment manager to instantiate the fragment
	 * (e.g. upon screen orientation changes). */
	public FragmentRecyclebinList() {
		mStateParameters = new StateParameters(new TreeMap<Integer, TaskUiData2>(),
				false, false);
	}

	private static class FragmentRecyclebinListAdapter extends ArrayAdapter<TaskUiData2> {
		private Integer mActivatedPosition;
		// private ActionMode mActionMode;
		private Drawable mDefaultBackground;
		private Drawable mStateActivatedDrawable;
		private Drawable mStateCheckedDrawable;
		private Set<Integer> mSelectedPositionsSet;

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public FragmentRecyclebinListAdapter(Context context, int resource,
				int textViewResourceId, List<TaskUiData2> objects) {
			super(context, resource, textViewResourceId, objects);
			View convertView = LayoutInflater.from(getContext()).inflate(resource, null);
			mDefaultBackground = convertView.getBackground();
			mStateActivatedDrawable = null;
			if (mDefaultBackground != null) {
				mDefaultBackground
						.setState(new int[] {android.os.Build.VERSION.SDK_INT >= 11 ? android.R.attr.state_activated
								: android.R.attr.state_selected});
				mDefaultBackground.jumpToCurrentState();
				mStateActivatedDrawable = mDefaultBackground.getCurrent();
				mDefaultBackground.setState(new int[] {android.R.attr.state_checked});
				mDefaultBackground.jumpToCurrentState();
				mStateCheckedDrawable = mDefaultBackground.getCurrent();
			}
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			TaskUiData2 taskWithDependents = getItem(position);
			TaskUiData2 task = taskWithDependents;
			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				convertView = LayoutInflater
						.from(getContext())
						.inflate(
								android.os.Build.VERSION.SDK_INT >= 11 ? android.R.layout.simple_list_item_activated_1
										: android.R.layout.simple_list_item_checked,
								parent, false);
			}
			// Lookup view for data population
			TextView tvName = (TextView) convertView.findViewById(android.R.id.text1);
			// Populate the data into the template view using the data object
			tvName.setText(task.getName());
			if (mSelectedPositionsSet != null && mSelectedPositionsSet.contains(position)) {
				// convertView.setBackgroundColor(getContext().getResources().getColor(
				// android.R.color.holo_green_light));
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					convertView.setBackground(mStateActivatedDrawable);
				} else if (android.os.Build.VERSION.SDK_INT >= 11) {
					convertView.setBackgroundDrawable(mStateActivatedDrawable);
				} else {
					Checkable CheckedTextView = (Checkable) convertView;
					CheckedTextView.setChecked(true);
				}
				// convertView.setActivated(true);
			} else {
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					convertView.setBackground(mDefaultBackground == null ? null
							: mDefaultBackground.getConstantState().newDrawable());
				} else if (android.os.Build.VERSION.SDK_INT >= 11) {
					convertView.setBackgroundDrawable(mDefaultBackground == null ? null
							: mDefaultBackground.getConstantState().newDrawable());
				} else {
					Checkable CheckedTextView = (Checkable) convertView;
					CheckedTextView.setChecked(false);
				}
				// convertView.setActivated(false);
			}
			// Return the completed view to render on screen
			return convertView;
		}

		public void setSelectedPositions(Set<Integer> selectedPositionsSet) {
			mSelectedPositionsSet = selectedPositionsSet;
			notifyDataSetChanged();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		mCallbacks = (Callbacks) activity;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mStateParameters = savedInstanceState.getParcelable("mStateParameters");
		}
		mTaskWithDependentsList = getTaskWithDependentsList();
		// mSelectedTaskWithDependentsList = new ArrayList<TaskUiData2>();
		mAdapter = new FragmentRecyclebinListAdapter(
				getActivity(),
				android.os.Build.VERSION.SDK_INT >= 11 ? android.R.layout.simple_list_item_activated_1
						: android.R.layout.simple_list_item_checked, android.R.id.text1,
				mTaskWithDependentsList);
		mAdapter.setSelectedPositions(mStateParameters.SelectedTasks.keySet());
		setListAdapter(mAdapter);
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@SuppressLint("InlinedApi")
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(
							CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
						if (isAdded()) {
							ArrayList<TaskUiData2> taskWithDependentsList = getTaskWithDependentsList();
							mTaskWithDependentsList.clear();
							mTaskWithDependentsList.addAll(taskWithDependentsList);
							mAdapter.notifyDataSetChanged();
						} else {
							mStateParameters.TasksNeedReload = true;
						}
					}
				}
			};
			//
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.registerReceiver(mReceiver, intentFilter);
		}
	}

	private ArrayList<TaskUiData2> getTaskWithDependentsList() {
		List<Task> tasks = DataProvider.getDeletedTasks(null, getActivity());
		ArrayList<TaskUiData2> taskWithDependentsList = new ArrayList<TaskUiData2>(
				tasks.size());
		for (Task task : tasks) {
			TaskUiData2 taskWithDependents = new TaskUiData2(
					DataProvider.getTaskWithDependents(null, getActivity(), task.getId()));
			taskWithDependentsList.add(taskWithDependents);
		}
		return taskWithDependentsList;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && mStateParameters == null) {
			mStateParameters = savedInstanceState.getParcelable("mStateParameters");
		}
		// if (mStateParameters.mActivatedPosition != null) {
		// setActivatedPosition(mStateParameters.mActivatedPosition);
		// }
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mListview = getListView();
		mListview.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mStateParameters.IsActionMode) {
					if (mStateParameters.SelectedTasks.containsKey(position)) {
						mStateParameters.SelectedTasks.remove(position);
					} else {
						TaskUiData2 calendar = (TaskUiData2) mListview
								.getItemAtPosition(position);
						mStateParameters.SelectedTasks.put(position, calendar);
					}
					Iterator<TaskUiData2> iterator = mStateParameters.SelectedTasks
							.values().iterator();
					ArrayList<TaskUiData2> checkedTaskWithDependentsList = new ArrayList<TaskUiData2>();
					if (!iterator.hasNext()) {
						mStateParameters.IsActionMode = false;
						// mActionMode.finish();
						// mActionMode = null;
						mCallbacks.onActionModeEnd();
					} else {
						while (iterator.hasNext()) {
							TaskUiData2 entry = iterator.next();
							checkedTaskWithDependentsList.add(entry);
						}
						mCallbacks.onItemsSelected(checkedTaskWithDependentsList);
					}
					mAdapter.setSelectedPositions(mStateParameters.SelectedTasks.keySet());
				} else {
					mStateParameters.SelectedTasks.clear();
					TaskUiData2 calendar = (TaskUiData2) mListview
							.getItemAtPosition(position);
					mStateParameters.SelectedTasks.put(position, calendar);
					ArrayList<TaskUiData2> checkedTaskWithDependentsList = new ArrayList<TaskUiData2>();
					checkedTaskWithDependentsList.add(calendar);
					mStateParameters.IsActionMode = true;
					mAdapter.setSelectedPositions(mStateParameters.SelectedTasks.keySet());
					mCallbacks.onActionModeStart();
					mCallbacks.onItemsSelected(checkedTaskWithDependentsList);
				}
				return true;
			}
		});
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// FragmentActivity activity = getActivity();
	// if (activity != null) {
	// MenuItem addItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_ADD_CALENDAR,
	// 0, activity.getResources().getString(R.string.action_calendar_new));
	// addItem.setIcon(R.drawable.ic_add_black_24dp);
	// MenuItemCompat.setShowAsAction(addItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
	// }
	// }
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// // case CommonConstants.MENU_ID_ADD_CALENDAR:
	// // mStateParameters.PendingTasks = new ArrayList<TaskUiData2>();
	// // //
	// // CalendarNameDialogFragment calendarNameDialogFragment = new
	// // CalendarNameDialogFragment();
	// // mCalendarNameDialogFragmentYesNoListener = new
	// // CalendarNameDialogFragment.YesNoListener() {
	// // @Override
	// // public void onYesInCalendarNameDialogFragment(String calendarName) {
	// // mStateParameters.PendingTasks.get(0).setName(calendarName);
	// // LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
	// // new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
	// // }
	// //
	// // @Override
	// // public void onNoInCalendarNameDialogFragment() {
	// // // TODO Auto-generated method stub
	// // }
	// // };
	// // calendarNameDialogFragment
	// // .setCallback(mCalendarNameDialogFragmentYesNoListener);
	// // calendarNameDialogFragment.show(getFragmentManager(),
	// // "CalendarNameDialogFragment");
	// // return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }
	// @Override
	// public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
	// {
	// super.onCreateContextMenu(menu, v, menuInfo);
	// getActivity().getMenuInflater().inflate(
	// R.menu.fragment_recyclebin_list_context_menu, menu);
	// }
	// @Override
	// public boolean onContextItemSelected(final MenuItem item) {
	// ListView.AdapterContextMenuInfo menuInfo = (ListView.AdapterContextMenuInfo) item
	// .getMenuInfo();
	// int position = menuInfo.position;
	// final TaskUiData2 taskWithDependents = (TaskUiData2) mListview
	// .getItemAtPosition(position);
	// final Long taskId;
	// taskId = taskWithDependents.getId();
	// if (taskId == null) {
	// mStateParameters.PendingTasks = null;
	// return true;
	// }
	// mStateParameters.PendingTasks = new ArrayList<TaskUiData2>();
	// mStateParameters.PendingTasks.add(taskWithDependents);
	// switch (item.getItemId()) {
	// case R.id.undelete:
	// ConfirmationAlertDialogFragment confirmationAlertDialogFragment = new
	// ConfirmationAlertDialogFragment();
	// confirmationAlertDialogFragment.setTitleAndMessage(
	// null,
	// getActivity().getResources().getString(
	// R.string.action_undelete_the_task_confirmation));
	// setupConfirmationAlertDialogFragmentUndeleteYesNoListener(confirmationAlertDialogFragment);
	// confirmationAlertDialogFragment.show(getFragmentManager(),
	// "ConfirmationAlertDialogFragmentUndelete");
	// return true;
	// case R.id.delete_permanently:
	// confirmationAlertDialogFragment = new ConfirmationAlertDialogFragment();
	// confirmationAlertDialogFragment.setTitleAndMessage(
	// null,
	// getActivity().getResources().getString(
	// R.string.action_delete_the_task_permanently_confirmation));
	// setupConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener(confirmationAlertDialogFragment);
	// confirmationAlertDialogFragment.show(getFragmentManager(),
	// "ConfirmationAlertDialogFragmentDeletePermanently");
	// return true;
	// default:
	// return false;
	// }
	// }
	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = FragmentRecyclebinList.sDummyCallbacks;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (true /* || getActivity().isFinishing() */) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		if (mStateParameters.IsActionMode) {
			if (mStateParameters.SelectedTasks.containsKey(position)) {
				mStateParameters.SelectedTasks.remove(position);
			} else {
				TaskUiData2 calendar = (TaskUiData2) mListview
						.getItemAtPosition(position);
				mStateParameters.SelectedTasks.put(position, calendar);
			}
			Iterator<TaskUiData2> iterator = mStateParameters.SelectedTasks.values()
					.iterator();
			ArrayList<TaskUiData2> checkedTaskWithDependentsList = new ArrayList<TaskUiData2>();
			if (!iterator.hasNext()) {
				mStateParameters.IsActionMode = false;
				// mActionMode.finish();
				// mActionMode = null;
				mCallbacks.onActionModeEnd();
			} else {
				while (iterator.hasNext()) {
					TaskUiData2 entry = iterator.next();
					checkedTaskWithDependentsList.add(entry);
				}
				mCallbacks.onItemsSelected(checkedTaskWithDependentsList);
			}
			mAdapter.setSelectedPositions(mStateParameters.SelectedTasks.keySet());
		} else {
			mStateParameters.SelectedTasks.clear();
			TaskUiData2 calendar = (TaskUiData2) mListview.getItemAtPosition(position);
			mStateParameters.SelectedTasks.put(position, calendar);
			ArrayList<TaskUiData2> checkedTaskWithDependentsList = new ArrayList<TaskUiData2>();
			checkedTaskWithDependentsList.add(calendar);
			mCallbacks.onItemsSelected(checkedTaskWithDependentsList);
			mAdapter.setSelectedPositions(mStateParameters.SelectedTasks.keySet());
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putParcelable("mStateParameters", mStateParameters);
		super.onSaveInstanceState(outState);
	}

	/** Turns on activate-on-click mode. When this mode is on, list items will be given the
	 * 'activated' state when touched. */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? AbsListView.CHOICE_MODE_MULTIPLE
						: AbsListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(Integer position) {
		if (position == null) {
			// getListView().setItemChecked(mStateParameters.mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
			// getListView().setSelection(position);
		}
		// mStateParameters.mActivatedPosition = position;
		// mAdapter.setActivatedItem(mStateParameters.mActivatedPosition);
	}

	@Override
	public void onYesInConfirmationAlertDialogFragment(
			ConfirmationAlertDialogFragment dialogFragment) {
		// for (TaskUiData2 taskWithDependents : mStateParameters.SelectedTasks) {
		// DataProvider.deleteTaskPermanently(getActivity(), taskWithDependents.getId(),
		// false);
		// }
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
				new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
	}

	@Override
	public void onNoInConfirmationAlertDialogFragment(
			ConfirmationAlertDialogFragment dialogFragment) {
		// TODO Auto-generated method stub
	}

	public ArrayList<TaskUiData2> getSelectedTasks() {
		Iterator<TaskUiData2> iterator = mStateParameters.SelectedTasks.values()
				.iterator();
		ArrayList<TaskUiData2> checkedTaskWithDependentsList = new ArrayList<TaskUiData2>();
		while (iterator.hasNext()) {
			TaskUiData2 entry = iterator.next();
			checkedTaskWithDependentsList.add(entry);
		}
		return checkedTaskWithDependentsList;
	}

	public void clearSelectedTasks() {
		mStateParameters.SelectedTasks.clear();
		mStateParameters.IsActionMode = false;
		mAdapter.setSelectedPositions(mStateParameters.SelectedTasks.keySet());
	}
}
