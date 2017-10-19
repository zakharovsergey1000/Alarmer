package biz.advancedcalendar.calendar2;

import biz.advancedcalendar.utils.CalendarHelper;
import java.util.Calendar;

public class Period {
	final Calendar calendar1;
	final Calendar calendar2;
	public int years;
	public int months;
	public int weeks;
	public int days;
	public int hours;
	public int minutes;
	public int seconds;
	public int milliseconds;

	public Period(Calendar calendar1arg, Calendar calendar2arg) {
		calendar1 = (Calendar) calendar1arg.clone();
		calendar2 = (Calendar) calendar2arg.clone();
		setupFields();
	}

	private void setupFields() {
		Calendar calendar = (Calendar) calendar1.clone();
		years = CalendarHelper.yearsBetween(calendar, calendar2);
		if (years > 0) {
			calendar.add(Calendar.YEAR, years);
		}
		months = CalendarHelper.monthsBetween(calendar, calendar2);
		if (months > 0) {
			calendar.add(Calendar.MONTH, months);
		}
		weeks = CalendarHelper.weeksBetween(calendar, calendar2);
		// if (weeks > 0) {
		// calendar.add(Calendar.WEEK_OF_YEAR, weeks);
		// }
		days = CalendarHelper.daysBetween(calendar, calendar2);
		if (days > 0) {
			calendar.add(Calendar.DAY_OF_YEAR, days);
		}
		hours = CalendarHelper.hoursBetween(calendar, calendar2);
		if (hours > 0) {
			calendar.add(Calendar.HOUR_OF_DAY, hours);
		}
		minutes = CalendarHelper.minutesBetween(calendar, calendar2);
		if (minutes > 0) {
			calendar.add(Calendar.MINUTE, minutes);
		}
		seconds = CalendarHelper.secondsBetween(calendar, calendar2);
		if (seconds > 0) {
			calendar.add(Calendar.SECOND, seconds);
		}
		milliseconds = (int) CalendarHelper.millisecondsBetween(calendar, calendar2);
	}

	public Period minusMillis(int i) {
		Calendar c2 = (java.util.Calendar) calendar2.clone();
		c2.add(Calendar.MILLISECOND, -i);
		Calendar c1 = calendar1;
		if (c2.getTimeInMillis() < c1.getTimeInMillis()) {
			Calendar calendar = c2;
			c2 = c1;
			c1 = calendar;
		}
		Period period = new Period(c1, c2);
		return period;
	}
}
