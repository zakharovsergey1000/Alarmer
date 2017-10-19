package biz.advancedcalendar.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityMain;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewAgenda.LoadGroupAsyncTaskResult;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewAgenda.TaskCallbacks;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.DaysSelectedListener;
import biz.advancedcalendar.views.FragmentViewAgendaExpandableListView;
import biz.advancedcalendar.views.TaskClickedListener;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter.ScrollPositionData;
import biz.advancedcalendar.views.accessories.ExpandListGroup;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;

public class FragmentViewAgenda extends Fragment implements OnDateSetListener,
		TaskCallbacks {
	public interface RetainedFragmentForFragmentViewAgendaHolder {
		RetainedFragmentForFragmentViewAgenda getRetainedFragmentForFragmentViewAgenda();
	}

	private DaysSelectedListener mDayClickedListener;
	private TaskClickedListener mTaskClickedListener;
	FragmentViewAgendaExpandableListView mExpandableListView;
	FragmentViewAgendaAdapter mAdapter;
	DateFormat mDateFormatTimeInstance;
	DateFormat mDateFormatDateInstance;
	private BroadcastReceiver mReceiver;
	// private String mPendingBundleForDatePickerDialogKey =
	// "mPendingBundleForDatePickerDialogKey";
	public static final String StateParametersKey = "biz.advancedcalendar.fragments.FragmentViewAgenda.StateParametersKey";
	public static final boolean LoadGroupAsyncTaskDebug = true;
	public static final String LoadGroupAsyncTaskDebugTag = "LoadGroupAsyncTaskDT";
	// variables to be saved in onSaveInstanceState()
	// private Parcelable mStateParameters;
	private Bundle mPendingBundleForDatePickerDialog;
	private String mDatePickerDialogKey = "biz.advancedcalendar.fragments.FragmentViewAgenda.DatePickerDialogKey";
	private RetainedFragmentForFragmentViewAgendaHolder mRetainedFragmentForFragmentViewAgendaHolder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("JumpDebug", String.format("FragmentViewAgenda onCreate(), thread: %s",
				Thread.currentThread().getName()));
		mDateFormatTimeInstance = DateFormat.getTimeInstance();
		mDateFormatDateInstance = DateFormat.getDateInstance();
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(CommonConstants.ACTION_CALENDARS_CHANGED)
						|| intent.getAction().equals(
								CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED)
						|| intent.getAction().equals(
								CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)
						|| intent.getAction().equals(
								CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED)
						|| intent.getAction().equals(
								CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED)) {
					mAdapter.reloadTasks();
				} else if (intent.getAction().equals("android.intent.action.TIME_SET")) {
					if (isAdded()) {
						if (Build.VERSION.SDK_INT >= 11) {
							getActivity().invalidateOptionsMenu();
						}
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mExpandableListView = (FragmentViewAgendaExpandableListView) inflater.inflate(
				R.layout.fragment_agenda, container, false);
		// mExpandableListView.setOverScrollMode(View.OVER_SCROLL_NEVER);
		mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, final long id) {
				ActivityMain.launchTaskViewerOrEditor(getActivity(),
						mAdapter.getChild(groupPosition, childPosition).Task.getId(),
						CommonConstants.INTENT_EXTRA_VALUE_TAB_TASK);
				return true;
			}
		});
		return mExpandableListView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getFragmentManager();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onActivityCreated() thread: %s", Thread
							.currentThread().getName()));
		}
		FragmentActivity context = getActivity();
		mRetainedFragmentForFragmentViewAgendaHolder = (RetainedFragmentForFragmentViewAgendaHolder) context;
		mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda().setCallbacks(context,
						getTag());
		setHasOptionsMenu(true);
		mTaskClickedListener = (TaskClickedListener) context;
		mDayClickedListener = (DaysSelectedListener) context;
		//
		if (mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda()
				.getExplicitlyCollapsedVirtualGroups() == null) {
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda()
					.setExplicitlyCollapsedVirtualGroups(new ArrayList<Integer>());
		}
		if (mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda().getLoadedGroups() == null) {
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda()
					.setLoadedGroups(
							new TreeMap<Integer, ExpandListGroup<FragmentViewAgendaAdapter.ExpandListGroupData, CalendarViewTaskOccurrence>>());
		}
		if (mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda().getScrollPositionData() == null) {
			ScrollPositionData scrollPositionData = new ScrollPositionData(0,
					EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET, -1, 0,
					EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET, null,
					null, null);
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda().setScrollPositionData(
							scrollPositionData);
		}
		if (mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda()
				.getDateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay() == null) {
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda()
					.setDateTimeOnVirtualMiddleOffset(System.currentTimeMillis());
		}
		if (mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda().getTasks() == null) {
			Long[] selectedCalendars = Helper.getLongArray(context, getResources()
					.getString(R.string.preference_key_selected_calendars), null);
			mRetainedFragmentForFragmentViewAgendaHolder
					.getRetainedFragmentForFragmentViewAgenda().setTasks(
							DataProvider.getActiveNotDeletedTasks(null, context,
									selectedCalendars));
		}
		mAdapter = new FragmentViewAgendaAdapter(context,
				mRetainedFragmentForFragmentViewAgendaHolder, mExpandableListView,
				mDayClickedListener, mTaskClickedListener);
		SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_sync_policy,
						R.array.sync_policy_values_array,
						R.integer.sync_policy_default_value));
		MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
				.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_mark_sync_needed,
						R.array.mark_sync_needed_values_array,
						R.integer.mark_sync_needed_default_value));
		syncPolicy = SyncPolicy.DO_NOT_SYNC;
		markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
		InformationUnitMatrix informationUnitMatrix = Helper.createInformationUnitMatrix(
				context, R.string.preference_key_information_unit_matrix_for_agenda,
				R.string.information_unit_matrix_for_agenda_default_value);
		mAdapter.setParameters(syncPolicy, markSyncNeededPolicy, informationUnitMatrix);
		mExpandableListView.setOnGroupExpandListener(mAdapter);
		mExpandableListView.setOnGroupCollapseListener(mAdapter);
		mExpandableListView.setAdapter(mAdapter);
		mExpandableListView.setOnScrollListener(mAdapter);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_CALENDARS_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
		intentFilter.addAction(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED);
		intentFilter.addAction("android.intent.action.TIME_SET");
		LocalBroadcastManager.getInstance(context.getApplicationContext())
				.registerReceiver(mReceiver, intentFilter);
		DatePickerDialog dpd = (DatePickerDialog) fm
				.findFragmentByTag(mDatePickerDialogKey);
		if (dpd != null) {
			dpd.setOnDateSetListener(this);
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		ScrollPositionData scrollPositionData = mRetainedFragmentForFragmentViewAgendaHolder
				.getRetainedFragmentForFragmentViewAgenda().getScrollPositionData();
		int flatListPosition;
		if (scrollPositionData.FirstVisibleGroupsFirstVisibleChild == -1) {
			flatListPosition = mExpandableListView.getFlatListPosition(ExpandableListView
					.getPackedPositionForGroup(scrollPositionData.FirstVisibleGroup));
		} else {
			flatListPosition = mExpandableListView.getFlatListPosition(ExpandableListView
					.getPackedPositionForChild(scrollPositionData.FirstVisibleGroup,
							scrollPositionData.FirstVisibleGroupsFirstVisibleChild));
		}
		mExpandableListView.setSelectionFromTop(flatListPosition,
				scrollPositionData.FirstVisibleGroupsFirstVisibleChildsTop);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onResume() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onPause() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onSaveInstanceState() thread: %s", Thread
							.currentThread().getName()));
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_GOTO_TODAY,
				Menu.FIRST + 100, getResources().getString(R.string.action_goto_today));
		Calendar calendar = Calendar.getInstance();
		menuItem.setIcon(CommonConstants.DAY_OF_MONTH_ICON_RESOURCES[calendar
				.get(Calendar.DAY_OF_MONTH) - 1]);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		//
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_GOTO_DATE,
				Menu.FIRST + 101, getResources().getString(R.string.action_goto_date));
		menuItem.setIcon(R.drawable.calendar_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_GOTO_TODAY:
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			mAdapter.launchTaskScrollVirtualGroupPositionToTop(mAdapter
					.getVirtualGroupPositionForDate(calendar.getTimeInMillis()));
			return true;
		case CommonConstants.MENU_ID_GOTO_DATE:
			calendar = Calendar.getInstance();
			mPendingBundleForDatePickerDialog = new Bundle();
			mPendingBundleForDatePickerDialog.putInt(CommonConstants.CALLER_ID,
					R.id.fragment_weekview_button_pickup_date);
			DatePickerDialog dpd = DatePickerDialog.newInstance(FragmentViewAgenda.this,
					calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH));
			dpd.show(getFragmentManager(), mDatePickerDialogKey);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onDestroy() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		mAdapter.launchTaskScrollVirtualGroupPositionToTop(mAdapter
				.getVirtualGroupPositionForDate(calendar.getTimeInMillis()));
	}

	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProgressUpdate(int percent) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onCancelled() {
		mAdapter.setLoadGroupAsyncTask(null);
	}

	@Override
	public void onPostExecute(final LoadGroupAsyncTaskResult asyncTaskResult) {
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onPostExecute thread: %s", Thread.currentThread()
							.getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"    asyncTaskResult.LaunchGroupLoaderIfNeeded: %s",
					asyncTaskResult.LaunchGroupLoaderIfNeeded));
		}
//		// check for null here because onPostExecute() might be called any time after
//		// onAttach()
		if (mAdapter != null) {
			mAdapter.addLoadedGroup(asyncTaskResult.ExpandListGroup,
					asyncTaskResult.WhereToScroll);
//			//
			if (asyncTaskResult.LaunchGroupLoaderIfNeeded) {
				Integer virtualPosition = mAdapter.launchGroupLoaderIfNeeded(null);
				if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
					Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
							"    launchGroupLoaderIfNeeded(null) returned: %s",
							virtualPosition));
				}
			}
		}
	}
}
