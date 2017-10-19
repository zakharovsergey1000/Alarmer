package biz.advancedcalendar.views;

import android.graphics.Rect;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import java.util.ArrayList;

public class CalendarViewTaskOccurrence {
	public Task Task;
	public Long StartDateTime, EndDateTime;
	public int Left, Top, Right, Bottom, RealTop, RealBottom;
	public ArrayList<String> TaskInformationStringsForTimeIntervalsMode;
	public Rect[] TaskInformationStringsForTimeIntervalsModeTextBounds;
	public float[] TaskInformationStringsForTimeIntervalsModeTextBaselines;
	public ArrayList<String> TaskInformationStringsForTextMode;
	public Rect[] TaskInformationStringsForTextModeTextBounds;
	public float[] TaskInformationStringsForTextModeTextBaselines;
	public int BackgroundColor;
	public int TextColor;
	public SyncStatus SyncStatus;

	public CalendarViewTaskOccurrence() {
	}

	public CalendarViewTaskOccurrence(
			CalendarViewTaskOccurrence calendarViewTaskOccurrence) {
		Task = calendarViewTaskOccurrence.Task;
		StartDateTime = calendarViewTaskOccurrence.StartDateTime;
		EndDateTime = calendarViewTaskOccurrence.EndDateTime;
		Left = calendarViewTaskOccurrence.Left;
		Top = calendarViewTaskOccurrence.Top;
		Right = calendarViewTaskOccurrence.Right;
		Bottom = calendarViewTaskOccurrence.Bottom;
		RealTop = calendarViewTaskOccurrence.RealTop;
		RealBottom = calendarViewTaskOccurrence.RealBottom;
		TaskInformationStringsForTimeIntervalsMode = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode;
		TaskInformationStringsForTimeIntervalsModeTextBounds = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsModeTextBounds;
		TaskInformationStringsForTimeIntervalsModeTextBaselines = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsModeTextBaselines;
		TaskInformationStringsForTextMode = calendarViewTaskOccurrence.TaskInformationStringsForTextMode;
		TaskInformationStringsForTextModeTextBounds = calendarViewTaskOccurrence.TaskInformationStringsForTextModeTextBounds;
		TaskInformationStringsForTextModeTextBaselines = calendarViewTaskOccurrence.TaskInformationStringsForTextModeTextBaselines;
		BackgroundColor = calendarViewTaskOccurrence.BackgroundColor;
		TextColor = calendarViewTaskOccurrence.TextColor;
		SyncStatus = calendarViewTaskOccurrence.SyncStatus;
	}

	public CalendarViewTaskOccurrence(biz.advancedcalendar.greendao.Task task) {
		Task = task;
	}

	public CalendarViewTaskOccurrence(Task task, Long startDateTime, Long endDateTime) {
		Task = task;
		StartDateTime = startDateTime;
		EndDateTime = endDateTime;
	}

	public CalendarViewTaskOccurrence init(Task task, Long startDateTime, Long endDateTime) {
		Task = task;
		StartDateTime = startDateTime;
		EndDateTime = endDateTime;
		return this;
	}
}