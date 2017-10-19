package biz.advancedcalendar.utils;

import biz.advancedcalendar.calendar2.Period;
import java.util.Calendar;

public class CalendarHelper {
	public static void toBeginningOfMonth(Calendar calendar) {
		calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0,
				0);
		calendar.set(Calendar.MILLISECOND, 0);
		// now the calendar has been moved to the beginning of the month
	}

	public static void toBeginningOfWeek(Calendar calendar, int firstDayOfWeek) {
		CalendarHelper.toBeginningOfDay(calendar);
		// move the calendar to the beginning of the week
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		calendar.add(Calendar.DAY_OF_YEAR, firstDayOfWeek - dayOfWeek);
		if (firstDayOfWeek > dayOfWeek) {
			calendar.add(Calendar.DAY_OF_YEAR, -7);
		}
		// now the calendar has been moved to the beginning of the week
	}

	public static void toBeginningOfDay(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		// now the calendar has been moved to the beginning of the day
	}

	public static int yearsBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		if (year1 == year2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000 / 60 / 60 / 24 / 365);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.YEAR, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.YEAR, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.YEAR, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.YEAR, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static int monthsBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		if (year1 == year2 && month1 == month2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000 / 60 / 60 / 24 / 30);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.MONTH, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.MONTH, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.MONTH, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.MONTH, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static int weeksBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		int week1 = calendar1.get(Calendar.WEEK_OF_YEAR);
		int week2 = calendar2.get(Calendar.WEEK_OF_YEAR);
		if (year1 == year2 && month1 == month2 && week1 == week2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000 / 60 / 60 / 24 / 7);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.WEEK_OF_YEAR, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.WEEK_OF_YEAR, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.WEEK_OF_YEAR, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.WEEK_OF_YEAR, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static int daysBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		int day1 = startCalendar.get(Calendar.DAY_OF_YEAR);
		int day2 = endCalendar.get(Calendar.DAY_OF_YEAR);
		if (year1 == year2 && month1 == month2 && day1 == day2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000 / 60 / 60 / 24);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.DAY_OF_YEAR, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.DAY_OF_YEAR, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.DAY_OF_YEAR, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.DAY_OF_YEAR, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static int hoursBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		int day1 = startCalendar.get(Calendar.DAY_OF_YEAR);
		int day2 = endCalendar.get(Calendar.DAY_OF_YEAR);
		int hour1 = startCalendar.get(Calendar.HOUR_OF_DAY);
		int hour2 = endCalendar.get(Calendar.HOUR_OF_DAY);
		if (year1 == year2 && month1 == month2 && day1 == day2 && hour1 == hour2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000 / 60 / 60);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.HOUR_OF_DAY, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.HOUR_OF_DAY, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.HOUR_OF_DAY, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.HOUR_OF_DAY, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static int minutesBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		int day1 = startCalendar.get(Calendar.DAY_OF_YEAR);
		int day2 = endCalendar.get(Calendar.DAY_OF_YEAR);
		int hour1 = startCalendar.get(Calendar.HOUR_OF_DAY);
		int hour2 = endCalendar.get(Calendar.HOUR_OF_DAY);
		int minute1 = startCalendar.get(Calendar.MINUTE);
		int minute2 = endCalendar.get(Calendar.MINUTE);
		if (year1 == year2 && month1 == month2 && day1 == day2 && hour1 == hour2
				&& minute1 == minute2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000 / 60);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.MINUTE, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.MINUTE, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.MINUTE, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.MINUTE, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static int secondsBetween(Calendar calendar1, Calendar calendar2) {
		Calendar startCalendar;
		Calendar endCalendar;
		if (calendar1.getTimeInMillis() < calendar2.getTimeInMillis()) {
			startCalendar = calendar1;
			endCalendar = calendar2;
		} else {
			startCalendar = calendar2;
			endCalendar = calendar1;
		}
		if (startCalendar.getTimeInMillis() == endCalendar.getTimeInMillis()) {
			return 0;
		}
		int year1 = startCalendar.get(Calendar.YEAR);
		int year2 = endCalendar.get(Calendar.YEAR);
		int month1 = startCalendar.get(Calendar.MONTH);
		int month2 = endCalendar.get(Calendar.MONTH);
		int day1 = startCalendar.get(Calendar.DAY_OF_YEAR);
		int day2 = endCalendar.get(Calendar.DAY_OF_YEAR);
		int hour1 = startCalendar.get(Calendar.HOUR_OF_DAY);
		int hour2 = endCalendar.get(Calendar.HOUR_OF_DAY);
		int minute1 = startCalendar.get(Calendar.MINUTE);
		int minute2 = endCalendar.get(Calendar.MINUTE);
		int second1 = calendar1.get(Calendar.SECOND);
		int second2 = calendar2.get(Calendar.SECOND);
		if (year1 == year2 && month1 == month2 && day1 == day2 && hour1 == hour2
				&& minute1 == minute2 && second1 == second2) {
			return 0;
		}
		int count = (int) ((endCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / 1000);
		Calendar date = (Calendar) startCalendar.clone();
		date.add(Calendar.SECOND, count);
		if (date.getTimeInMillis() < endCalendar.getTimeInMillis()) {
			date.add(Calendar.SECOND, 1);
			while (date.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
				count++;
				date.add(Calendar.SECOND, 1);
			}
		} else {
			while (endCalendar.getTimeInMillis() < date.getTimeInMillis()) {
				count--;
				date.add(Calendar.SECOND, -1);
			}
		}
		return startCalendar == calendar1 ? count : -count;
	}

	public static long millisecondsBetween(Calendar calendar1, Calendar calendar2) {
		Calendar calendar1Clone = (Calendar) calendar1.clone();
		Calendar calendar2Clone = (Calendar) calendar2.clone();
		return calendar2Clone.getTimeInMillis() - calendar1Clone.getTimeInMillis();
	}

	public static Calendar plus(Calendar calendar, Period period) {
		Calendar calendar1 = (Calendar) calendar.clone();
		if (period.years > 0) {
			calendar1.add(Calendar.YEAR, period.years);
		}
		if (period.months > 0) {
			calendar1.add(Calendar.MONTH, period.months);
		}
		if (period.days > 0) {
			calendar1.add(Calendar.DAY_OF_YEAR, period.days);
		}
		if (period.hours > 0) {
			calendar1.add(Calendar.HOUR_OF_DAY, period.hours);
		}
		if (period.minutes > 0) {
			calendar1.add(Calendar.MINUTE, period.minutes);
		}
		if (period.seconds > 0) {
			calendar1.add(Calendar.SECOND, period.seconds);
		}
		if (period.milliseconds > 0) {
			calendar1.add(Calendar.MILLISECOND, period.milliseconds);
		}
		return calendar1;
	}

	public static Calendar minus(Calendar calendar, Period period) {
		Calendar calendar1 = (Calendar) calendar.clone();
		if (period.years > 0) {
			calendar1.add(Calendar.YEAR, -period.years);
		}
		if (period.months > 0) {
			calendar1.add(Calendar.MONTH, -period.months);
		}
		if (period.days > 0) {
			calendar1.add(Calendar.DAY_OF_YEAR, -period.days);
		}
		if (period.hours > 0) {
			calendar1.add(Calendar.HOUR_OF_DAY, -period.hours);
		}
		if (period.minutes > 0) {
			calendar1.add(Calendar.MINUTE, -period.minutes);
		}
		if (period.seconds > 0) {
			calendar1.add(Calendar.SECOND, -period.seconds);
		}
		if (period.milliseconds > 0) {
			calendar1.add(Calendar.MILLISECOND, -period.milliseconds);
		}
		return calendar1;
	}

	public static int getMillisOfDay(Calendar calendar) {
		Calendar clone = (Calendar) calendar.clone();
		clone.set(Calendar.HOUR_OF_DAY, 0);
		clone.set(Calendar.MINUTE, 0);
		clone.set(Calendar.SECOND, 0);
		clone.set(Calendar.MILLISECOND, 0);
		return (int) (calendar.getTimeInMillis() - clone.getTimeInMillis());
	}

	public static boolean isLeapYear(int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR) > 365;
	}

	public static boolean isLastDayOfWeek(Calendar calendar, int firstDayOfWeek) {
		return (firstDayOfWeek == 1 ? 7 : firstDayOfWeek - 1) == calendar
				.get(Calendar.DAY_OF_WEEK);
	}
}
