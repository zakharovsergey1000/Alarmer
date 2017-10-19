package biz.advancedcalendar.fragments;

import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.FragmentViewWeek2.FragmentViewWeek2StateParameters;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.BusinessHoursTaskDisplayingPolicy;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.TaskOccurrencesDistribution;
import biz.advancedcalendar.views.accessories.EndlessScrollBaseExpandableListAdapter.ScrollPositionData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

/** This Fragment manages a single background task and retains itself across configuration
 * changes. */
public class RetainedFragmentForFragmentViewWeek2 extends Fragment {
	TreeMap<Integer, TaskOccurrencesDistribution> mCachedCalculatedTaskOccurrencesDistributions = new TreeMap<Integer, TaskOccurrencesDistribution>();

	public static class CalculateAndDistributeTaskOccurrencesAsyncTaskArgument {
		long[] DaysBorders;
		int[] Positions;
		int FirstDayOfWeek;
		List<Task> Tasks;
		FragmentViewWeek2StateParameters FragmentViewWeek2StateParameters;

		public CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(int[] positions,
				long[] daysBorders, int firstDayOfWeek, List<Task> tasks,
				FragmentViewWeek2StateParameters fragmentViewWeek2StateParameters) {
			super();
			Positions = positions;
			DaysBorders = daysBorders;
			FirstDayOfWeek = firstDayOfWeek;
			Tasks = tasks;
			FragmentViewWeek2StateParameters = fragmentViewWeek2StateParameters;
		}
	}

	public static class CalculateAndDistributeTaskOccurrencesAsyncTaskResult {
		public TaskOccurrencesDistribution[] TaskOccurrencesDistribution;
		public int[] Positions;

		public CalculateAndDistributeTaskOccurrencesAsyncTaskResult(
				TaskOccurrencesDistribution[] taskOccurrencesDistribution, int[] positions) {
			TaskOccurrencesDistribution = taskOccurrencesDistribution;
			Positions = positions;
		}
	}

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

	public void launchAsyncTask(
			CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] calculateAndDistributeTaskOccurrencesAsyncTaskArgumentsArray) {
		mIsAsyncTaskRunning = true;
		CalculateAndDistributeTaskOccurrencesAsyncTask asyncTask = new CalculateAndDistributeTaskOccurrencesAsyncTask();
		asyncTask.execute(calculateAndDistributeTaskOccurrencesAsyncTaskArgumentsArray);
	}

	private TaskCallbacks mCallbacks;
	private boolean mIsAsyncTaskRunning;

	/** This method will only be called once when the retained Fragment is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Retain this fragment across configuration changes.
		setRetainInstance(true);
	}

	/** Set the callback to null so we don't accidentally leak the Activity instance. */
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = null;
	}

	public class CalculateAndDistributeTaskOccurrencesAsyncTask
			extends
			AsyncTask<CalculateAndDistributeTaskOccurrencesAsyncTaskArgument, CalculateAndDistributeTaskOccurrencesAsyncTaskResult, CalculateAndDistributeTaskOccurrencesAsyncTaskResult> {
		@Override
		protected CalculateAndDistributeTaskOccurrencesAsyncTaskResult doInBackground(
				CalculateAndDistributeTaskOccurrencesAsyncTaskArgument... params) {
			TaskOccurrencesDistribution taskOccurrencesDistribution;
			TextPaint textPaint = new TextPaint();
			textPaint.setTextSize(params[0].FragmentViewWeek2StateParameters.TextSize);
			for (int i = 0; i < params.length; i++) {
				int count = params[i].DaysBorders.length / 2;
				taskOccurrencesDistribution = calculateAndDistributeTaskOccurrencesForDays(
						params[i].Tasks, params[i].DaysBorders,
						new TaskOccurrencesDistribution(count), params[i].FirstDayOfWeek,
						params[i].FragmentViewWeek2StateParameters, textPaint);
				TaskOccurrencesDistribution[] taskOccurrencesDistributions = new TaskOccurrencesDistribution[count];
				for (int j = 0; j < count; j++) {
					TaskOccurrencesDistribution taskOccurrencesDistribution1 = new TaskOccurrencesDistribution(
							1);
					taskOccurrencesDistribution1.TaskOccurrencesTakingWholeIntervalList
							.get(0)
							.addAll(taskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList
									.get(j));
					taskOccurrencesDistribution1.TaskOccurrencesTouchingPreBusinessHoursList
							.get(0)
							.addAll(taskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList
									.get(j));
					taskOccurrencesDistribution1.TaskOccurrencesTouchingBusinessHoursList
							.get(0)
							.addAll(taskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList
									.get(j));
					taskOccurrencesDistribution1.TaskOccurrencesTouchingPostBusinessHoursList
							.get(0)
							.addAll(taskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList
									.get(j));
					taskOccurrencesDistributions[j] = taskOccurrencesDistribution1;
				}
				publishProgress(new CalculateAndDistributeTaskOccurrencesAsyncTaskResult(
						taskOccurrencesDistributions, params[i].Positions));
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

	public List<Task> getTasks() {
		if (Tasks == null) {
			Long[] selectedCalendars = Helper.getLongArray(getActivity(), getResources()
					.getString(R.string.preference_key_selected_calendars), null);
			Tasks = DataProvider.getActiveNotDeletedTasks(null, getActivity(),
					selectedCalendars);
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

	synchronized TaskOccurrencesDistribution calculateAndDistributeTaskOccurrencesForDays(
			List<Task> tasks, long[] daysBorders,
			TaskOccurrencesDistribution taskOccurrencesDistribution, int firstDayOfWeek,
			FragmentViewWeek2StateParameters fragmentViewWeek2StateParameters,
			TextPaint textPaint) {
		List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
		for (Task task : tasks) {
			calendarViewTaskOccurrences.addAll(task.selectTaskOccurrences(daysBorders[0],
					daysBorders[daysBorders.length - 1], firstDayOfWeek, null));
		}
		//
		// now assign TaskInformationStrings and distribute calendarViewTaskOccurrences
		for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
			calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode = fragmentViewWeek2StateParameters.InformationUnitMatrixForTimeIntervalsMode
					.createInformationComposerStrings(getActivity(),
							calendarViewTaskOccurrence);
			Rect[] textBoundsArrayForTimeIntervalsMode = new Rect[calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode
					.size()];
			float[] textBaselinesArrayForTimeIntervalsMode = new float[calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode
					.size()];
			for (int i = 0; i < calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode
					.size(); i++) {
				Rect textBounds = new Rect();
				String text = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode
						.get(i);
				textPaint.getTextBounds(text, 0, text.length(), textBounds);
				textBoundsArrayForTimeIntervalsMode[i] = textBounds;
				textBaselinesArrayForTimeIntervalsMode[i] = textPaint.descent();
			}
			calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsModeTextBounds = textBoundsArrayForTimeIntervalsMode;
			calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsModeTextBaselines = textBaselinesArrayForTimeIntervalsMode;
			//
			calendarViewTaskOccurrence.TaskInformationStringsForTextMode = fragmentViewWeek2StateParameters.InformationUnitMatrixForTextMode
					.createInformationComposerStrings(getActivity(),
							calendarViewTaskOccurrence);
			Rect[] textBoundsArrayForTextMode = new Rect[calendarViewTaskOccurrence.TaskInformationStringsForTextMode
					.size()];
			float[] textBaselinesArrayForTextMode = new float[calendarViewTaskOccurrence.TaskInformationStringsForTextMode
					.size()];
			for (int i = 0; i < calendarViewTaskOccurrence.TaskInformationStringsForTextMode
					.size(); i++) {
				Rect textBounds = new Rect();
				String text = calendarViewTaskOccurrence.TaskInformationStringsForTextMode
						.get(i);
				textPaint.getTextBounds(text, 0, text.length(), textBounds);
				textBoundsArrayForTextMode[i] = textBounds;
				textBaselinesArrayForTextMode[i] = textPaint.descent();
			}
			calendarViewTaskOccurrence.TaskInformationStringsForTextModeTextBounds = textBoundsArrayForTextMode;
			calendarViewTaskOccurrence.TaskInformationStringsForTextModeTextBaselines = textBaselinesArrayForTextMode;
			int backgroundColor = calendarViewTaskOccurrence.Task
					.getColor2(getActivity());
			calendarViewTaskOccurrence.BackgroundColor = backgroundColor;
			boolean b = Helper.getContrastYIQ(backgroundColor);
			int textColor;
			if (b) {
				textColor = getActivity().getResources().getColor(
						R.color.task_view_text_synchronized_dark);
			} else {
				textColor = getActivity().getResources().getColor(
						R.color.task_view_text_synchronized_light);
			}
			calendarViewTaskOccurrence.TextColor = textColor;
			calendarViewTaskOccurrence.SyncStatus = SyncStatus
					.fromInt(calendarViewTaskOccurrence.Task.getSyncStatusValue());
			distributeTaskOccurrence(
					calendarViewTaskOccurrence,
					daysBorders,
					fragmentViewWeek2StateParameters.BusinessHoursStartTime,
					fragmentViewWeek2StateParameters.BusinessHoursEndTime,
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