package biz.advancedcalendar.views;

import biz.advancedcalendar.activities.activityedittask.TaskUiData2;

public class CalendarViewTaskOccurrence2 {
	public TaskUiData2 Task;
	public Long StartDateTime;
	public Long EndDateTime;
	public int Left, Top, Right, Bottom, RealTop, RealBottom;

	public CalendarViewTaskOccurrence2() {
	}

	public CalendarViewTaskOccurrence2(
			CalendarViewTaskOccurrence2 calendarViewTaskOccurrence) {
		Task = calendarViewTaskOccurrence.Task;
		StartDateTime = calendarViewTaskOccurrence.StartDateTime;
		EndDateTime = calendarViewTaskOccurrence.EndDateTime;
		Left = calendarViewTaskOccurrence.Left;
		Top = calendarViewTaskOccurrence.Top;
		Right = calendarViewTaskOccurrence.Right;
		Bottom = calendarViewTaskOccurrence.Bottom;
		RealTop = calendarViewTaskOccurrence.RealTop;
		RealBottom = calendarViewTaskOccurrence.RealBottom;
	}

	public CalendarViewTaskOccurrence2(TaskUiData2 task, Long startDateTime,
			Long endDateTime) {
		Task = task;
		StartDateTime = startDateTime;
		EndDateTime = endDateTime;
	}

	public CalendarViewTaskOccurrence2 init(TaskUiData2 task, Long startDateTime,
			Long endDateTime) {
		Task = task;
		StartDateTime = startDateTime;
		EndDateTime = endDateTime;
		return this;
	}
}