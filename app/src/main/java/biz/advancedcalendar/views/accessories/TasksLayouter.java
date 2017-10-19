package biz.advancedcalendar.views.accessories;

import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import java.util.List;

public interface TasksLayouter {
	public void calculateTasksCoords1(final long startDateTime,
			final List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences,
			final long[] timeIntervalDurations, final int[] periodViewsTopBottomCoords,
			final int boundaryWidthPx, final int calendarViewTaskOccurrenceMinHeightPx,
			final int spacerXPx, final int spacerYPx);
}
