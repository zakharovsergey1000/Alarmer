package biz.advancedcalendar.calendar2;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class Calendar2 {
	Calendar mCalendar;

	void add(int field, int amount) {
		mCalendar.add(field, amount);
	}

	boolean after(Object when) {
		return mCalendar.after(when);
	}

	boolean before(Object when) {
		return mCalendar.before(when);
	}

	void clear() {
		mCalendar.clear();
	}

	void clear(int field) {
		mCalendar.clear(field);
	}

	@Override
	public Object clone() {
		return mCalendar.clone();
	}

	int compareTo(Calendar anotherCalendar) {
		return mCalendar.compareTo(anotherCalendar);
	}

	@Override
	public boolean equals(Object obj) {
		return mCalendar.equals(obj);
	}

	@Override
	public int hashCode() {
		return 1;
	}

	int get(int field) {
		return mCalendar.get(field);
	}

	int getActualMaximum(int field) {
		return mCalendar.getActualMaximum(field);
	}

	int getActualMinimum(int field) {
		return mCalendar.getActualMinimum(field);
	}

	static Locale[] getAvailableLocales() {
		return Calendar.getAvailableLocales();
	}

	String getDisplayName(int field, int style, Locale locale) {
		return mCalendar.getDisplayName(field, style, locale);
	}

	Map<String, Integer> getDisplayNames(int field, int style, Locale locale) {
		return mCalendar.getDisplayNames(field, style, locale);
	}

	int getFirstDayOfWeek() {
		return mCalendar.getFirstDayOfWeek();
	}

	int getGreatestMinimum(int field) {
		return mCalendar.getGreatestMinimum(field);
	}

	int getLeastMaximum(int field) {
		return mCalendar.getLeastMaximum(field);
	}

	int getMaximum(int field) {
		return mCalendar.getMaximum(field);
	}

	int getMinimalDaysInFirstWeek() {
		return mCalendar.getMinimalDaysInFirstWeek();
	}

	int getMinimum(int field) {
		return mCalendar.getMinimum(field);
	}

	Date getTime() {
		return mCalendar.getTime();
	}

	long getTimeInMillis() {
		return mCalendar.getTimeInMillis();
	}

	TimeZone getTimeZone() {
		return mCalendar.getTimeZone();
	}

	boolean isLenient() {
		return mCalendar.isLenient();
	}

	boolean isSet(int field) {
		return mCalendar.isSet(field);
	}

	void roll(int field, boolean up) {
		mCalendar.roll(field, up);
	}

	void roll(int field, int amount) {
		mCalendar.roll(field, amount);
	}

	void set(int field, int value) {
		mCalendar.set(field, value);
	}

	void set(int year, int month, int date) {
		mCalendar.set(year, month, date);
	}

	void set(int year, int month, int date, int hourOfDay, int minute) {
		mCalendar.set(year, month, date, hourOfDay, minute);
	}

	void set(int year, int month, int date, int hourOfDay, int minute, int second) {
		mCalendar.set(year, month, date, hourOfDay, minute, second);
	}

	void setFirstDayOfWeek(int value) {
		mCalendar.setFirstDayOfWeek(value);
	}

	void setLenient(boolean lenient) {
		mCalendar.setLenient(lenient);
	}

	void setMinimalDaysInFirstWeek(int value) {
		mCalendar.setMinimalDaysInFirstWeek(value);
	}

	void setTime(Date date) {
		mCalendar.setTime(date);
	}

	void setTimeInMillis(long millis) {
		mCalendar.setTimeInMillis(millis);
	}

	void setTimeZone(TimeZone value) {
		mCalendar.setTimeZone(value);
	}

	void setWeekDate(int weekYear, int weekOfYear, int dayOfWeek) {
		setWeekDate(weekYear, weekOfYear, dayOfWeek);
	}
}
