package biz.advancedcalendar.views;

import java.util.ArrayList;
import java.util.List;

public class TaskOccurrencesDistribution {
	private int mDaysCount;

	@Override
	public String toString() {
		return "TaskOccurrencesDistribution [mDaysCount=" + mDaysCount
				+ ", isAllDayTasksExist()=" + isAllDayTasksExist()
				+ ", isPreBusinessHoursTasksExist()=" + isPreBusinessHoursTasksExist()
				+ ", isPostBusinessHoursTasksExist()=" + isPostBusinessHoursTasksExist()
				+ "]";
	}

	public boolean isAllDayTasksExist() {
		boolean allDayTasksExist = false;
		for (int i = 0; i < mDaysCount; i++) {
			if (TaskOccurrencesTakingWholeIntervalList.get(i).size() > 0) {
				allDayTasksExist = true;
				break;
			}
		}
		return allDayTasksExist;
	}

	public boolean isPreBusinessHoursTasksExist() {
		boolean preBusinessHoursTasksExist = false;
		for (int i = 0; i < mDaysCount; i++) {
			if (TaskOccurrencesTouchingPreBusinessHoursList.get(i).size() > 0) {
				preBusinessHoursTasksExist = true;
				break;
			}
		}
		return preBusinessHoursTasksExist;
	}

	public boolean isBusinessHoursTasksExist() {
		boolean businessHoursTasksExist = false;
		for (int i = 0; i < mDaysCount; i++) {
			if (TaskOccurrencesTouchingBusinessHoursList.get(i).size() > 0) {
				businessHoursTasksExist = true;
				break;
			}
		}
		return businessHoursTasksExist;
	}

	public boolean isPostBusinessHoursTasksExist() {
		boolean postBusinessHoursTasksExist = false;
		for (int i = 0; i < mDaysCount; i++) {
			if (TaskOccurrencesTouchingPostBusinessHoursList.get(i).size() > 0) {
				postBusinessHoursTasksExist = true;
				break;
			}
		}
		return postBusinessHoursTasksExist;
	}

	public List<List<CalendarViewTaskOccurrence>> TaskOccurrencesTakingWholeIntervalList,
			TaskOccurrencesTouchingPreBusinessHoursList,
			TaskOccurrencesTouchingBusinessHoursList,
			TaskOccurrencesTouchingPostBusinessHoursList;

	public TaskOccurrencesDistribution(int daysCount) {
		mDaysCount = daysCount;
		TaskOccurrencesTakingWholeIntervalList = new ArrayList<List<CalendarViewTaskOccurrence>>(
				daysCount);
		TaskOccurrencesTouchingPreBusinessHoursList = new ArrayList<List<CalendarViewTaskOccurrence>>(
				daysCount);
		TaskOccurrencesTouchingBusinessHoursList = new ArrayList<List<CalendarViewTaskOccurrence>>(
				daysCount);
		TaskOccurrencesTouchingPostBusinessHoursList = new ArrayList<List<CalendarViewTaskOccurrence>>(
				daysCount);
		for (int i = 0; i < daysCount; i++) {
			TaskOccurrencesTakingWholeIntervalList
					.add(new ArrayList<CalendarViewTaskOccurrence>());
			TaskOccurrencesTouchingPreBusinessHoursList
					.add(new ArrayList<CalendarViewTaskOccurrence>());
			TaskOccurrencesTouchingBusinessHoursList
					.add(new ArrayList<CalendarViewTaskOccurrence>());
			TaskOccurrencesTouchingPostBusinessHoursList
					.add(new ArrayList<CalendarViewTaskOccurrence>());
		}
	}

	public void init() {
		for (int i = 0; i < mDaysCount; i++) {
			TaskOccurrencesTakingWholeIntervalList.get(i).clear();
			TaskOccurrencesTouchingPreBusinessHoursList.get(i).clear();
			TaskOccurrencesTouchingBusinessHoursList.get(i).clear();
			TaskOccurrencesTouchingPostBusinessHoursList.get(i).clear();
		}
	}
}