package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import biz.advancedcalendar.fragments.FragmentViewAgendaAdapter.ExpandListGroupData;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.accessories.CalendarViewTaskOccurrenceComparator;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter.EndlessScrollBaseExpandableListAdapterDataHolder;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter.ScrollPositionData;
import biz.advancedcalendar.views.accessories.ExpandListGroup;
import biz.advancedcalendar.views.accessories.TaskOccurrencesCalculatorAndDistributor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** This Fragment manages a single background task and retains itself across configuration
 * changes. */
public class RetainedFragmentForFragmentViewAgenda extends Fragment
		implements
		EndlessScrollBaseExpandableListAdapterDataHolder<ExpandListGroupData, CalendarViewTaskOccurrence>,
		FragmentViewAgendaAdapterDataHolder {
	public RetainedFragmentForFragmentViewAgenda() {
		super();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"RetainedFragmentForFragmentViewAgenda() thread: %s", Thread
							.currentThread().getName()));
		}
	}

	public RetainedFragmentForFragmentViewAgenda(String fragmentViewAgendaTag) {
		super();
		mFragmentViewAgendaTag = fragmentViewAgendaTag;
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format(
							"RetainedFragmentForFragmentViewAgenda(String fragmentViewAgendaTag) thread: %s",
							Thread.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    fragmentViewAgendaTag: %s", fragmentViewAgendaTag));
		}
	}

	public static class LoadGroupAsyncTaskArguments {
		public int VirtualGroupPosition;
		public boolean LaunchGroupLoaderIfNeeded;
		public int WhereToScroll;
		public int FirstDayOfWeek;

		public LoadGroupAsyncTaskArguments(
				// ExpandableListView expandableListView,
				int virtualGroupPosition, boolean launchGroupLoaderIfNeeded,
				int whereToScroll, int firstDayOfWeek) {
			// ExpandableListView = expandableListView;
			VirtualGroupPosition = virtualGroupPosition;
			LaunchGroupLoaderIfNeeded = launchGroupLoaderIfNeeded;
			WhereToScroll = whereToScroll;
			FirstDayOfWeek = firstDayOfWeek;
		}
	}

	public static class LoadGroupAsyncTaskResult {
		// public ExpandableListView ExpandableListView;
		public ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> ExpandListGroup;
		public boolean LaunchGroupLoaderIfNeeded;
		public int WhereToScroll;

		public LoadGroupAsyncTaskResult(
				// android.widget.ExpandableListView expandableListView,
				ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence> expandListGroup,
				boolean launchGroupLoaderIfNeeded, int whereToScroll) {
			// ExpandableListView = expandableListView;
			ExpandListGroup = expandListGroup;
			LaunchGroupLoaderIfNeeded = launchGroupLoaderIfNeeded;
			WhereToScroll = whereToScroll;
		}
	}

	String mFragmentViewAgendaTag;
	public Map<Integer, ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>> LoadedGroups;
	public ArrayList<Integer> ExplicitlyCollapsedVirtualGroups;
	public ScrollPositionData ScrollPositionData;
	//
	public List<Task> Tasks;
	public Long DateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay;

	/** Callback interface through which the fragment will report the task's progress and
	 * results back to the Activity. */
	interface TaskCallbacks {
		void onPreExecute();

		void onProgressUpdate(int percent);

		void onCancelled();

		void onPostExecute(LoadGroupAsyncTaskResult asyncTaskResult);
	}

	// private LoadGroupAsyncTask mLoadGroupAsyncTask = null;
	public LoadGroupAsyncTask getLoadGroupAsyncTask() {
		return new LoadGroupAsyncTask();
	}

	private TaskCallbacks mCallbacks;
	private TaskOccurrencesCalculatorAndDistributor mTaskOccurrencesCalculatorAndDistributor;

	/** Hold a reference to the parent Activity so we can report the task's current
	 * progress and results. The Android framework will pass us a reference to the newly
	 * created Activity after each configuration change. */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		FragmentManager fm = getFragmentManager();
		FragmentViewAgenda fragmentViewAgenda = (FragmentViewAgenda) fm
				.findFragmentByTag(mFragmentViewAgendaTag);
		mCallbacks = fragmentViewAgenda;
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"RetainedFragmentForFragmentViewAgenda onAttach() thread: %s", Thread
							.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"    mFragmentViewAgendaTag: %s", mFragmentViewAgendaTag));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    mCallbacks assigned: %s", mCallbacks));
		}
	}

	/** This method will only be called once when the retained Fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retain this fragment across configuration changes.
		setRetainInstance(true);
		// Create and execute the background task.
		// mLoadGroupAsyncTask = new LoadGroupAsyncTask();
		// mTask.execute();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"RetainedFragmentForFragmentViewAgenda onCreate() thread: %s", Thread
							.currentThread().getName()));
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getFragmentManager();
		FragmentViewAgenda fragmentViewAgenda = (FragmentViewAgenda) fm
				.findFragmentByTag(mFragmentViewAgendaTag);
		mCallbacks = fragmentViewAgenda;
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format(
							"RetainedFragmentForFragmentViewAgenda onActivityCreated() thread: %s",
							Thread.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"    mFragmentViewAgendaTag: %s", mFragmentViewAgendaTag));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    mCallbacks assigned: %s", mCallbacks));
		}
	}

	/** Set the callback to null so we don't accidentally leak the Activity instance. */
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"RetainedFragmentForFragmentViewAgenda onDetach() thread: %s", Thread
							.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    mCallbacks assigned: %s", mCallbacks));
		}
	}

	public class LoadGroupAsyncTask extends
			AsyncTask<LoadGroupAsyncTaskArguments, Void, LoadGroupAsyncTaskResult> {
		@Override
		protected LoadGroupAsyncTaskResult doInBackground(
				LoadGroupAsyncTaskArguments... params) {
			Log.d("JumpDebug", String.format("doInBackground thread: %s", Thread
					.currentThread().getName()));
			LoadGroupAsyncTaskArguments asyncTaskArguments = params[0];
			Calendar dateTimeOnVirtualGroupPosition = Calendar.getInstance();
			dateTimeOnVirtualGroupPosition
					.setTimeInMillis(DateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay);
			dateTimeOnVirtualGroupPosition.add(Calendar.DAY_OF_YEAR,
					asyncTaskArguments.VirtualGroupPosition);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = getTaskOccurrences(
					dateTimeOnVirtualGroupPosition.getTimeInMillis(),
					params[0].FirstDayOfWeek);
			return new LoadGroupAsyncTaskResult(
					new ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>(
							asyncTaskArguments.VirtualGroupPosition,
							new ExpandListGroupData(dateTimeOnVirtualGroupPosition
									.getTimeInMillis()),
							calendarViewTaskOccurrences),
					asyncTaskArguments.LaunchGroupLoaderIfNeeded,
					asyncTaskArguments.WhereToScroll);
		}

		@Override
		protected void onPostExecute(final LoadGroupAsyncTaskResult asyncTaskResult) {
			if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format(
								"RetainedFragmentForFragmentViewAgenda onPostExecute() thread: %s",
								Thread.currentThread().getName()));
				Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
						String.format("    mCallbacks: %s", mCallbacks));
			}
			if (mCallbacks != null) {
				mCallbacks.onPostExecute(asyncTaskResult);
			}
		}

		@Override
		protected void onCancelled() {
			if (mCallbacks != null) {
				mCallbacks.onCancelled();
			}
		}
	}

	@Override
	public ArrayList<Integer> getExplicitlyCollapsedVirtualGroups() {
		return ExplicitlyCollapsedVirtualGroups;
	}

	@Override
	public void setLoadedGroups(
			Map<Integer, ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>> loadedGroups) {
		LoadedGroups = loadedGroups;
	}

	@Override
	public Map<Integer, ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>> getLoadedGroups() {
		// TODO Auto-generated method stub
		return LoadedGroups;
	}

	@Override
	public void setExplicitlyCollapsedVirtualGroups(
			ArrayList<Integer> explicitlyCollapsedVirtualGroups) {
		ExplicitlyCollapsedVirtualGroups = explicitlyCollapsedVirtualGroups;
	}

	@Override
	public ScrollPositionData getScrollPositionData() {
		// TODO Auto-generated method stub
		return ScrollPositionData;
	}

	@Override
	public void setScrollPositionData(ScrollPositionData scrollPositionData) {
		ScrollPositionData = scrollPositionData;
	}

	@Override
	public Long getDateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay() {
		return DateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay;
	}

	@Override
	public List<Task> getTasks() {
		return Tasks;
	}

	@Override
	public void setTasks(List<Task> tasks) {
		Tasks = tasks;
	}

	@Override
	public void setDateTimeOnVirtualMiddleOffset(Long dateTimeOnVirtualMiddleOffset) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dateTimeOnVirtualMiddleOffset);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		DateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay = calendar.getTimeInMillis();
	}

	List<CalendarViewTaskOccurrence> getTaskOccurrences(long dateTime, int firstDayOfWeek) {
		synchronized (RetainedFragmentForFragmentViewAgenda.class) {
			mTaskOccurrencesCalculatorAndDistributor = new TaskOccurrencesCalculatorAndDistributor(
					Tasks, firstDayOfWeek);
			mTaskOccurrencesCalculatorAndDistributor
					.calculateAndDistributeTaskOccurrences(dateTime, 1);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(dateTime);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			CalendarViewTaskOccurrenceComparator calendarViewTaskOccurrenceComparator = new CalendarViewTaskOccurrenceComparator(
					dateTime, calendar.getTimeInMillis());
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTakingWholeInterval = mTaskOccurrencesCalculatorAndDistributor
					.getCalendarViewTaskOccurrencesTakingWholeInterval().get(0);
			Collections.sort(calendarViewTaskOccurrencesTakingWholeInterval,
					calendarViewTaskOccurrenceComparator);
			for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrencesTakingWholeInterval) {
				calendarViewTaskOccurrences.add(new CalendarViewTaskOccurrence(
						calendarViewTaskOccurrence));
			}
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesNotTakingWholeInterval = mTaskOccurrencesCalculatorAndDistributor
					.getCalendarViewTaskOccurrencesNotTakingWholeInterval().get(0);
			Collections.sort(calendarViewTaskOccurrencesNotTakingWholeInterval,
					calendarViewTaskOccurrenceComparator);
			for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrencesNotTakingWholeInterval) {
				calendarViewTaskOccurrences.add(new CalendarViewTaskOccurrence(
						calendarViewTaskOccurrence));
			}
			return calendarViewTaskOccurrences;
		}
	}

	public void setCallbacks(FragmentActivity activity, String fragmentViewAgendaTag) {
		FragmentManager fm = activity.getSupportFragmentManager();
		FragmentViewAgenda fragmentViewAgenda = (FragmentViewAgenda) fm
				.findFragmentByTag(mFragmentViewAgendaTag = fragmentViewAgendaTag);
		mCallbacks = fragmentViewAgenda;
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format(
							"RetainedFragmentForFragmentViewAgenda setCallbacks(FragmentActivity activity, String fragmentViewAgendaTag) thread: %s",
							Thread.currentThread().getName()));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    activity: %s", activity));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"    mFragmentViewAgendaTag: %s", mFragmentViewAgendaTag));
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag,
					String.format("    mCallbacks assigned: %s", mCallbacks));
		}
	}
}