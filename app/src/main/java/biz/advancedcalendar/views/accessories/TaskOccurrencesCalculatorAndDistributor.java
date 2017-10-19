package biz.advancedcalendar.views.accessories;

import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class TaskOccurrencesCalculatorAndDistributor {
	private final List<List<CalendarViewTaskOccurrence>> mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList;
	private final List<List<CalendarViewTaskOccurrence>> mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList;
	private List<Task> mTasks;
	private final HashSet<CalendarViewTaskOccurrence> mCacheOfCalendarViewTaskOccurrences;
	private HashSet<List<CalendarViewTaskOccurrence>> mCacheOfCalendarViewTaskOccurrencesLists;
	private int mFirstDayOfWeek;

	public TaskOccurrencesCalculatorAndDistributor(List<Task> tasks, int firstDayOfWeek) {
		mFirstDayOfWeek = firstDayOfWeek;
		mTasks = tasks;
		mCacheOfCalendarViewTaskOccurrences = new HashSet<CalendarViewTaskOccurrence>();
		mCacheOfCalendarViewTaskOccurrencesLists = new HashSet<List<CalendarViewTaskOccurrence>>();
		mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList = new ArrayList<List<CalendarViewTaskOccurrence>>();
	}

	public void calculateAndDistributeTaskOccurrences(long startDayDateTime, int daysCount) {
		cacheCalendarViewTaskOccurrences();
		long borderStartDateTime;
		long borderEndDateTime;
		long[] mDaysBorders = new long[daysCount * 2];
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(startDayDateTime);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		for (int i = 0; i < daysCount; i++) {
			borderStartDateTime = calendar.getTimeInMillis();
			// currentDayStartDateTime = currentDayStartDateTime.plusDays(1);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			borderEndDateTime = calendar.getTimeInMillis();
			mDaysBorders[i * 2] = borderStartDateTime;
			mDaysBorders[i * 2 + 1] = borderEndDateTime;
			List<CalendarViewTaskOccurrence> wholeDayCalendarViewTaskOccurrences = getCachedCalendarViewTaskOccurrences();
			mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList
					.add(wholeDayCalendarViewTaskOccurrences);
			List<CalendarViewTaskOccurrence> notWholeDayCalendarViewTaskOccurrences = getCachedCalendarViewTaskOccurrences();
			mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList
					.add(notWholeDayCalendarViewTaskOccurrences);
		}
		List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
		for (Task task : mTasks) {
			calendarViewTaskOccurrences.addAll(task.selectTaskOccurrences(
					mDaysBorders[0], mDaysBorders[mDaysBorders.length - 1],
					mFirstDayOfWeek, mCacheOfCalendarViewTaskOccurrences));
		}
		// now distribute calendarViewTaskOccurrences
		for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
			distributeTaskOccurrence(calendarViewTaskOccurrence, mDaysBorders,
					mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList,
					mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList);
		}
	}

	private void distributeTaskOccurrence(
			CalendarViewTaskOccurrence calendarViewTaskOccurrence,
			long[] borders,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesTakingWholeIntervalList,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesNotTakingWholeIntervalList) {
		for (int i = 0; i < borders.length; i += 2) {
			long currentBorderStartDateTime = borders[i];
			long currentBorderEndDateTime = borders[i + 1];
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTakingWholeInterval = calendarViewTaskOccurrencesTakingWholeIntervalList
					.get(i / 2);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesNotTakingWholeInterval = calendarViewTaskOccurrencesNotTakingWholeIntervalList
					.get(i / 2);
			if (Helper.isTakingWholeInterval(calendarViewTaskOccurrence.StartDateTime,
					calendarViewTaskOccurrence.EndDateTime, currentBorderStartDateTime,
					currentBorderEndDateTime)) {
				calendarViewTaskOccurrencesTakingWholeInterval
						.add(calendarViewTaskOccurrence);
			} else if (Helper.isTakingNotWholeInterval(
					calendarViewTaskOccurrence.StartDateTime,
					calendarViewTaskOccurrence.EndDateTime, currentBorderStartDateTime,
					currentBorderEndDateTime)) {
				calendarViewTaskOccurrencesNotTakingWholeInterval
						.add(calendarViewTaskOccurrence);
			}
		}
	}

	public List<List<CalendarViewTaskOccurrence>> getCalendarViewTaskOccurrencesTakingWholeInterval() {
		return mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList;
	}

	public List<List<CalendarViewTaskOccurrence>> getCalendarViewTaskOccurrencesNotTakingWholeInterval() {
		return mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList;
	}

	private void cacheCalendarViewTaskOccurrences() {
		for (List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences : mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList) {
			for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
				mCacheOfCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
			}
			calendarViewTaskOccurrences.clear();
			mCacheOfCalendarViewTaskOccurrencesLists.add(calendarViewTaskOccurrences);
		}
		Log.d(CommonConstants.DEBUG_TAG,
				"mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList is about to be cleared, "
						+ ", hash: "
						+ mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList
								.hashCode()
						+ ", thread: "
						+ Thread.currentThread().getName());
		mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList.clear();
		Log.d(CommonConstants.DEBUG_TAG,
				"mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList is cleared, "
						+ ", hash: "
						+ mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList
								.hashCode() + ", thread: "
						+ Thread.currentThread().getName());
		//
		for (List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences : mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList) {
			for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
				mCacheOfCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
			}
			calendarViewTaskOccurrences.clear();
			mCacheOfCalendarViewTaskOccurrencesLists.add(calendarViewTaskOccurrences);
		}
		mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList.clear();
	}

	private List<CalendarViewTaskOccurrence> getCachedCalendarViewTaskOccurrences() {
		Iterator<List<CalendarViewTaskOccurrence>> iterator = mCacheOfCalendarViewTaskOccurrencesLists
				.iterator();
		if (iterator.hasNext()) {
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = iterator
					.next();
			iterator.remove();
			return calendarViewTaskOccurrences;
		}
		List<CalendarViewTaskOccurrence> list = new ArrayList<CalendarViewTaskOccurrence>();
		return list;
	}
}
