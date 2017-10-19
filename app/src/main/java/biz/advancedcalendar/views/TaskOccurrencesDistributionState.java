package biz.advancedcalendar.views;

public class TaskOccurrencesDistributionState {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (AllDayTasksExist ? 1231 : 1237);
		result = prime * result + (BusinessHoursTasksExist ? 1231 : 1237);
		result = prime * result + (PostBusinessHoursTasksExist ? 1231 : 1237);
		result = prime * result + (PreBusinessHoursTasksExist ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskOccurrencesDistributionState other = (TaskOccurrencesDistributionState) obj;
		if (AllDayTasksExist != other.AllDayTasksExist)
			return false;
		if (BusinessHoursTasksExist != other.BusinessHoursTasksExist)
			return false;
		if (PostBusinessHoursTasksExist != other.PostBusinessHoursTasksExist)
			return false;
		if (PreBusinessHoursTasksExist != other.PreBusinessHoursTasksExist)
			return false;
		return true;
	}

	public boolean AllDayTasksExist, PreBusinessHoursTasksExist, BusinessHoursTasksExist,
			PostBusinessHoursTasksExist;
}