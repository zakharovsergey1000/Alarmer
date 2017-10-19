package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.FragmentViewMonth2.StateParameters;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.BusinessHoursTaskDisplayingPolicy;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.TaskOccurrencesDistribution;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter.ScrollPositionData;
import biz.advancedcalendar.views.accessories.TaskOccurrencesCalculatorAndDistributor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

/** This Fragment manages a single background task and retains itself across configuration
 * changes. */
public class RetainedFragmentForFragmentViewMonth2 extends Fragment {
	TreeMap<Integer, TaskOccurrencesDistribution> mCachedCalculatedTaskOccurrencesDistributions = new TreeMap<Integer, TaskOccurrencesDistribution>();

	public static class CalculateAndDistributeTaskOccurrencesAsyncTaskArgument {
		public CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
				int position,
				long[] daysBorders,
				biz.advancedcalendar.views.TaskOccurrencesDistribution taskOccurrencesDistribution,
				int firstDayOfWeek, List<Task> tasks,
				StateParameters fragmentViewWeek2StateParameters) {
			super();
			Position = position;
			DaysBorders = daysBorders;
			TaskOccurrencesDistribution = taskOccurrencesDistribution;
			FirstDayOfWeek = firstDayOfWeek;
			Tasks = tasks;
			FragmentViewWeek2StateParameters = fragmentViewWeek2StateParameters;
		}

		long[] DaysBorders;
		int Position;
		TaskOccurrencesDistribution TaskOccurrencesDistribution;
		int FirstDayOfWeek;
		List<Task> Tasks;
		StateParameters FragmentViewWeek2StateParameters;
	}

	public static class CalculateAndDistributeTaskOccurrencesAsyncTaskResult {
		public TaskOccurrencesDistribution TaskOccurrencesDistribution;
		public int Position;

		public CalculateAndDistributeTaskOccurrencesAsyncTaskResult(
				TaskOccurrencesDistribution taskOccurrencesDistribution, int position) {
			TaskOccurrencesDistribution = taskOccurrencesDistribution;
			Position = position;
		}
	}

	// String mFragmentViewAgendaTag;
	// public Map<Integer, ExpandListGroup<ExpandListGroupData,
	// CalendarViewTaskOccurrence>> LoadedGroups;
	public ArrayList<Integer> ExplicitlyCollapsedVirtualGroups;
	public ScrollPositionData ScrollPositionData;
	//
	private List<Task> Tasks;
	public Long DateTimeOnVirtualMiddleOffset;

	/** Callback interface through which the fragment will report the task's progress and
	 * results back to the Activity. */
	interface TaskCallbacks {
		void onPreExecute();

		void onProgressUpdate(
				CalculateAndDistributeTaskOccurrencesAsyncTaskResult asyncTaskResult);

		void onCancelled();

		void onPostExecute(
				CalculateAndDistributeTaskOccurrencesAsyncTaskResult asyncTaskResult);
	}

	// private LoadGroupAsyncTask mLoadGroupAsyncTask = null;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void launchAsyncTask(
			CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] calculateAndDistributeTaskOccurrencesAsyncTaskArgumentsArray) {
		mIsAsyncTaskRunning = true;
		CalculateAndDistributeTaskOccurrencesAsyncTask asyncTask = new CalculateAndDistributeTaskOccurrencesAsyncTask();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					calculateAndDistributeTaskOccurrencesAsyncTaskArgumentsArray);
		} else {
			asyncTask
					.execute(calculateAndDistributeTaskOccurrencesAsyncTaskArgumentsArray);
		}
	}

	private TaskCallbacks mCallbacks;
	private TaskOccurrencesCalculatorAndDistributor mTaskOccurrencesCalculatorAndDistributor;
	private boolean mIsAsyncTaskRunning;

	/** This method will only be called once when the retained Fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retain this fragment across configuration changes.
		setRetainInstance(true);
		// Create and execute the background task.
		// mLoadGroupAsyncTask = new LoadGroupAsyncTask();
		// mTask.execute();
		// if (FragmentViewWeek2.LoadGroupAsyncTaskDebug) {
		// Log.d(FragmentViewWeek2.LoadGroupAsyncTaskDebugTag, String.format(
		// "RetainedFragmentForFragmentViewAgenda onCreate() thread: %s", Thread
		// .currentThread().getName()));
		// }
	}

	/** Set the callback to null so we don't accidentally leak the Activity instance. */
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
		// if (FragmentViewWeek2.LoadGroupAsyncTaskDebug) {
		// Log.d(FragmentViewWeek2.LoadGroupAsyncTaskDebugTag, String.format(
		// "RetainedFragmentForFragmentViewAgenda onDetach() thread: %s", Thread
		// .currentThread().getName()));
		// Log.d(FragmentViewWeek2.LoadGroupAsyncTaskDebugTag,
		// String.format("    mCallbacks assigned: %s", mCallbacks));
		// }
	}

	public class CalculateAndDistributeTaskOccurrencesAsyncTask
			extends
			AsyncTask<CalculateAndDistributeTaskOccurrencesAsyncTaskArgument, CalculateAndDistributeTaskOccurrencesAsyncTaskResult, CalculateAndDistributeTaskOccurrencesAsyncTaskResult> {
		@Override
		protected CalculateAndDistributeTaskOccurrencesAsyncTaskResult doInBackground(
				CalculateAndDistributeTaskOccurrencesAsyncTaskArgument... params) {
			TaskOccurrencesDistribution taskOccurrencesDistribution;
			for (int i = 0; i < params.length; i++) {
				taskOccurrencesDistribution = calculateAndDistributeTaskOccurrencesForWeek(
						params[i].Tasks, params[i].DaysBorders,
						params[i].TaskOccurrencesDistribution, params[i].FirstDayOfWeek,
						params[i].FragmentViewWeek2StateParameters);
				publishProgress(new CalculateAndDistributeTaskOccurrencesAsyncTaskResult(
						taskOccurrencesDistribution, params[i].Position));
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(
				CalculateAndDistributeTaskOccurrencesAsyncTaskResult... progress) {
			if (mCallbacks != null) {
				mCallbacks.onProgressUpdate(progress[0]);
			}
		}

		@Override
		protected void onPostExecute(
				final CalculateAndDistributeTaskOccurrencesAsyncTaskResult asyncTaskResult) {
			// if (FragmentViewWeek2.DEBUG) {
			// Log.d(SectionsPagerAdapter.FragmentViewWeek2Debug,
			// "onPostExecute."
			// + "\t mPosition: "
			// + mPosition
			// + "\t asyncTaskResult: "
			// + asyncTaskResult
			// +
			// "mCachedTaskOccurrencesDistributions.put(mPosition, asyncTaskResult). mPosition: "
			// + mPosition + " asyncTaskResult:" + asyncTaskResult);
			// }
			mIsAsyncTaskRunning = false;
			if (mCallbacks != null) {
				mCallbacks.onPostExecute(asyncTaskResult);
			}
		}

		@Override
		protected void onCancelled() {
			mIsAsyncTaskRunning = false;
			if (mCallbacks != null) {
				mCallbacks.onCancelled();
			}
		}
	}

	// @Override
	// public ArrayList<Integer> getExplicitlyCollapsedVirtualGroups() {
	// return ExplicitlyCollapsedVirtualGroups;
	// }
	// @Override
	// public void setLoadedGroups(
	// Map<Integer, ExpandListGroup<ExpandListGroupData, CalendarViewTaskOccurrence>>
	// loadedGroups) {
	// LoadedGroups = loadedGroups;
	// }
	//
	// @Override
	// public Map<Integer, ExpandListGroup<ExpandListGroupData,
	// CalendarViewTaskOccurrence>> getLoadedGroups() {
	// // TODO Auto-generated method stub
	// return LoadedGroups;
	// }
	// @Override
	// public void setExplicitlyCollapsedVirtualGroups(
	// ArrayList<Integer> explicitlyCollapsedVirtualGroups) {
	// ExplicitlyCollapsedVirtualGroups = explicitlyCollapsedVirtualGroups;
	// }
	//
	// @Override
	// public ScrollPositionData getScrollPositionData() {
	// // TODO Auto-generated method stub
	// return ScrollPositionData;
	// }
	//
	// @Override
	// public void setScrollPositionData(ScrollPositionData scrollPositionData) {
	// ScrollPositionData = scrollPositionData;
	// }
	//
	// @Override
	// public Long getDateTimeOnVirtualMiddleOffset() {
	// return DateTimeOnVirtualMiddleOffset;
	// }
	public List<Task> getTasks() {
		if (Tasks == null) {
			Long[] selectedCalendars = Helper.getLongArray(getActivity(), getResources()
					.getString(R.string.preference_key_selected_calendars), null);
			Tasks = DataProvider.getActiveNotDeletedTasks(null,
					getActivity(), selectedCalendars);
		}
		return Tasks;
	}

	public void invalidateTasks() {
		Tasks = null;
		mCachedCalculatedTaskOccurrencesDistributions.clear();
	}

	public void invalidateTaskOccurrencesDistributions() {
		mCachedCalculatedTaskOccurrencesDistributions.clear();
	}

	// @Override
	// public void setDateTimeOnVirtualMiddleOffset(Long dateTimeOnVirtualMiddleOffset) {
	// DateTimeOnVirtualMiddleOffset = dateTimeOnVirtualMiddleOffset;
	// }
	// List<CalendarViewTaskOccurrence> getTaskOccurrences(DateTime dateTime,
	// int firstDayOfWeek) {
	// synchronized (RetainedFragmentForFragmentViewMonth2.class) {
	// synchronized (RetainedFragmentForFragmentViewMonth2.class) {
	// mTaskOccurrencesCalculatorAndDistributor = new
	// TaskOccurrencesCalculatorAndDistributor(
	// Tasks, firstDayOfWeek);
	// }
	// mTaskOccurrencesCalculatorAndDistributor
	// .calculateAndDistributeTaskOccurrences(dateTime, 1);
	// Log.d(CommonConstants.DEBUG_TAG,
	// "mTaskOccurrencesCalculatorAndDistributor.getCalendarViewTaskOccurrencesTakingWholeInterval().size(): "
	// + mTaskOccurrencesCalculatorAndDistributor
	// .getCalendarViewTaskOccurrencesTakingWholeInterval()
	// .size()
	// + ", hash: "
	// + mTaskOccurrencesCalculatorAndDistributor
	// .getCalendarViewTaskOccurrencesTakingWholeInterval()
	// .hashCode()
	// + ", thread: "
	// + Thread.currentThread().getName());
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = new
	// ArrayList<CalendarViewTaskOccurrence>();
	// CalendarViewTaskOccurrenceComparator calendarViewTaskOccurrenceComparator = new
	// CalendarViewTaskOccurrenceComparator(
	// dateTime.getMillis(), dateTime.plusDays(1).getMillis());
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTakingWholeInterval =
	// mTaskOccurrencesCalculatorAndDistributor
	// .getCalendarViewTaskOccurrencesTakingWholeInterval().get(0);
	// Collections.sort(calendarViewTaskOccurrencesTakingWholeInterval,
	// calendarViewTaskOccurrenceComparator);
	// for (CalendarViewTaskOccurrence calendarViewTaskOccurrence :
	// calendarViewTaskOccurrencesTakingWholeInterval) {
	// calendarViewTaskOccurrences.add(new CalendarViewTaskOccurrence(
	// calendarViewTaskOccurrence));
	// }
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesNotTakingWholeInterval
	// = mTaskOccurrencesCalculatorAndDistributor
	// .getCalendarViewTaskOccurrencesNotTakingWholeInterval().get(0);
	// Collections.sort(calendarViewTaskOccurrencesNotTakingWholeInterval,
	// calendarViewTaskOccurrenceComparator);
	// for (CalendarViewTaskOccurrence calendarViewTaskOccurrence :
	// calendarViewTaskOccurrencesNotTakingWholeInterval) {
	// calendarViewTaskOccurrences.add(new CalendarViewTaskOccurrence(
	// calendarViewTaskOccurrence));
	// }
	// return calendarViewTaskOccurrences;
	// }
	// }
	synchronized TaskOccurrencesDistribution calculateAndDistributeTaskOccurrencesForWeek(
			List<Task> tasks, long[] daysBorders,
			TaskOccurrencesDistribution taskOccurrencesDistribution, int firstDayOfWeek,
			StateParameters fragmentViewWeek2StateParameters) {
		//
		List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
		for (Task task : tasks) {
			calendarViewTaskOccurrences.addAll(task.selectTaskOccurrences(daysBorders[0],
					daysBorders[daysBorders.length - 1], firstDayOfWeek, null));
			// task.selectAndDistributeTaskOccurrences2(
			// daysBorders,
			// fragmentViewWeek2StateParameters.BusinessHoursStartHour,
			// fragmentViewWeek2StateParameters.BusinessHoursEndHour,
			// fragmentViewWeek2StateParameters.BusinessHoursTaskDisplayingPolicy,
			// taskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList,
			// taskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList,
			// taskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList,
			// taskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList,
			// null, firstDayOfWeek);
		}
		//
		// now distribute calendarViewTaskOccurrences
		for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
			distributeTaskOccurrence(
					calendarViewTaskOccurrence,
					daysBorders,
					fragmentViewWeek2StateParameters.BusinessHoursStartHour,
					fragmentViewWeek2StateParameters.BusinessHoursEndHour,
					fragmentViewWeek2StateParameters.BusinessHoursTaskDisplayingPolicy,
					taskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList,
					taskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList,
					taskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList,
					taskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList,
					null);
		}
		//
		return taskOccurrencesDistribution;
	}

	private void distributeTaskOccurrence(
			CalendarViewTaskOccurrence calendarViewTaskOccurrence,
			long[] dayBorders,
			int businessHoursStart,
			int businessHoursEnd,
			BusinessHoursTaskDisplayingPolicy mode,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesTakingWholeIntervalList,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesTouchingPreBusinessHoursList,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesTouchingBusinessHoursList,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesTouchingPostBusinessHoursList,
			HashSet<CalendarViewTaskOccurrence> cacheOfCalendarViewTaskOccurrences) {
		Long taskOccurrenceStartDateTime = calendarViewTaskOccurrence.StartDateTime;
		Long taskOccurrenceEndDateTime = calendarViewTaskOccurrence.EndDateTime;
		for (int i = 0; i < dayBorders.length; i += 2) {
			long currentBorderStartDateTime = dayBorders[i];
			long currentBorderEndDateTime = dayBorders[i + 1];
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTakingWholeInterval = calendarViewTaskOccurrencesTakingWholeIntervalList
					.get(i / 2);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTouchingPreBusinessHours = calendarViewTaskOccurrencesTouchingPreBusinessHoursList
					.get(i / 2);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTouchingBusinessHours = calendarViewTaskOccurrencesTouchingBusinessHoursList
					.get(i / 2);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTouchingPostBusinessHours = calendarViewTaskOccurrencesTouchingPostBusinessHoursList
					.get(i / 2);
			if (Helper.isTakingWholeInterval(taskOccurrenceStartDateTime,
					taskOccurrenceEndDateTime, currentBorderStartDateTime,
					currentBorderEndDateTime)) {
				calendarViewTaskOccurrencesTakingWholeInterval
						.add(calendarViewTaskOccurrence);
			} else {
				switch (mode) {
				default:
				case DISPLAY_SEPARATED:
					if (Helper.isTouchingInterval(taskOccurrenceStartDateTime,
							taskOccurrenceEndDateTime, currentBorderStartDateTime
									+ businessHoursStart, currentBorderStartDateTime
									+ businessHoursEnd)
							&& currentBorderStartDateTime + businessHoursStart != currentBorderStartDateTime
									+ businessHoursEnd) {
						calendarViewTaskOccurrencesTouchingBusinessHours
								.add(calendarViewTaskOccurrence);
					} else if (Helper.isTouchingInterval(taskOccurrenceStartDateTime,
							taskOccurrenceEndDateTime, currentBorderStartDateTime,
							currentBorderStartDateTime + businessHoursStart)
							&& currentBorderStartDateTime != currentBorderStartDateTime
									+ businessHoursStart) {
						calendarViewTaskOccurrencesTouchingPreBusinessHours
								.add(calendarViewTaskOccurrence);
					} else if (Helper.isTouchingInterval(taskOccurrenceStartDateTime,
							taskOccurrenceEndDateTime, currentBorderStartDateTime
									+ businessHoursEnd, currentBorderEndDateTime)
							&& currentBorderStartDateTime + businessHoursEnd != currentBorderEndDateTime) {
						calendarViewTaskOccurrencesTouchingPostBusinessHours
								.add(calendarViewTaskOccurrence);
					}
					break;
				case DISPLAY_AS_USUAL:
					if (Helper.isTouchingInterval(taskOccurrenceStartDateTime,
							taskOccurrenceEndDateTime, currentBorderStartDateTime,
							currentBorderEndDateTime)) {
						calendarViewTaskOccurrencesTouchingBusinessHours
								.add(calendarViewTaskOccurrence);
					}
					break;
				case DO_NOT_DISPLAY:
					if (Helper.isTouchingInterval(taskOccurrenceStartDateTime,
							taskOccurrenceEndDateTime, currentBorderStartDateTime
									+ businessHoursStart, currentBorderStartDateTime
									+ businessHoursEnd)
							&& currentBorderStartDateTime + businessHoursStart != currentBorderStartDateTime
									+ businessHoursEnd) {
						calendarViewTaskOccurrencesTouchingBusinessHours
								.add(calendarViewTaskOccurrence);
					}
					break;
				}
			}
		}
	}

	public void setCallbacks(TaskCallbacks taskCallbacks) {
		mCallbacks = taskCallbacks;
	}

	public boolean isAsyncTaskRunning() {
		return mIsAsyncTaskRunning;
	}
}