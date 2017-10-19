package biz.advancedcalendar.views;

public class TextModeViewCoords {
	public TextModeViewCoords(
			biz.advancedcalendar.views.CalendarViewTaskOccurrence calendarViewTaskOccurrence,
			int top, int bottom) {
		super();
		CalendarViewTaskOccurrence = calendarViewTaskOccurrence;
		this.top = top;
		this.bottom = bottom;
	}

	public CalendarViewTaskOccurrence CalendarViewTaskOccurrence;
	public int top, bottom;
}
