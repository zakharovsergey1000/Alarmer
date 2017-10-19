package biz.advancedcalendar.activities.accessories;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.accessories.InformationUnit;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitRow;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class InformationUnitMatrix implements Parcelable {
	private static final String WHITE_SPACE = " ";
	private static final char WHITE_SPACE_CHAR = ' ';
	private static final String DASH_AND_SPACE = "- ";
	private ArrayList<InformationUnitRow> informationUnitRows;

	public InformationUnitMatrix(ArrayList<InformationUnitRow> informationUnitRows) {
		this.informationUnitRows = informationUnitRows;
	}

	public ArrayList<InformationUnitRow> getInformationUnitRows() {
		return informationUnitRows;
	}

	public void setInformationUnitRows(ArrayList<InformationUnitRow> informationUnitRows) {
		this.informationUnitRows = informationUnitRows;
	}

	public boolean equals(InformationUnitMatrix second) {
		return super.equals(second);
	}

	public ArrayList<String> createInformationComposerStrings(Context context,
			CalendarViewTaskOccurrence calendarViewTaskOccurrence) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(InformationUnitMatrix.WHITE_SPACE);
		int length = stringBuilder.length();
		char lastChar = stringBuilder.charAt(length - 1);
		boolean isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
		Resources resources = context.getResources();
		ArrayList<String> strings = new ArrayList<String>();
		String text;
		if (informationUnitRows != null) {
			DateFormat dateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
					DateFormat.SHORT);
			DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
			SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy",
					Locale.getDefault());
			SimpleDateFormat weekDayFormat = new SimpleDateFormat("EEE",
					Locale.getDefault());
			SimpleDateFormat monthFormat = new SimpleDateFormat("MMM",
					Locale.getDefault());
			SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
			Date mDate = new Date();
			Task task = calendarViewTaskOccurrence.Task;
			Long startDateTime = task.getStartDateTime();
			Long endDateTime = task.getEndDateTime();
			Long taskDuration = null;
			if (startDateTime != null) {
				if (endDateTime != null) {
					taskDuration = endDateTime - startDateTime;
				}
			}
			int rowsCount = informationUnitRows.size();
			int firstDayOfWeek = Helper.getFirstDayOfWeek(context);
			long controlStartDateTime;
			Long nearestCurrentOccurrenceStartDateTime;
			Long nearestNextOccurrenceStartDateTime;
			Long nearestPreviousOccurrenceStartDateTime;
			String textNextScheduledTimeNone = resources
					.getString(R.string.information_unit_text_next_scheduled_time_none);
			long now = System.currentTimeMillis();
			if (calendarViewTaskOccurrence.StartDateTime != null) {
				controlStartDateTime = calendarViewTaskOccurrence.StartDateTime;
			} else {
				controlStartDateTime = now;
			}
			nearestCurrentOccurrenceStartDateTime = task
					.getNearestOccurrenceStartDateTime(controlStartDateTime, true, false,
							firstDayOfWeek);
			if (nearestCurrentOccurrenceStartDateTime == null) {
				nearestCurrentOccurrenceStartDateTime = task
						.getNearestOccurrenceStartDateTime(controlStartDateTime, true,
								true, firstDayOfWeek);
			}
			nearestNextOccurrenceStartDateTime = task.getNearestOccurrenceStartDateTime(
					controlStartDateTime, false, false, firstDayOfWeek);
			if (nearestCurrentOccurrenceStartDateTime != null
					&& nearestNextOccurrenceStartDateTime != null
					&& nearestCurrentOccurrenceStartDateTime
							.equals(nearestNextOccurrenceStartDateTime)) {
				nearestPreviousOccurrenceStartDateTime = task
						.getNearestOccurrenceStartDateTime(controlStartDateTime, true,
								true, firstDayOfWeek);
				if (nearestPreviousOccurrenceStartDateTime != null) {
					nearestCurrentOccurrenceStartDateTime = nearestPreviousOccurrenceStartDateTime;
				}
			}
			for (int i = 0; i < rowsCount; i++) {
				InformationUnitRow informationUnitRow = informationUnitRows.get(i);
				ArrayList<InformationUnit> informationUnits = informationUnitRow
						.getInformationUnits();
				if (informationUnits != null) {
					int size = informationUnits.size();
					for (int j = 0; j < size; j++) {
						InformationUnit informationUnit = informationUnits.get(j);
						InformationUnitSelector informationUnitSelector = informationUnit
								.getInformationUnitSelector();
						switch (informationUnitSelector) {
						case FIRST_SCHEDULED_START_DATE_TIME:
							if (startDateTime != null) {
								mDate.setTime(startDateTime);
								text = dateTimeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_START_TIME:
							if (startDateTime != null) {
								mDate.setTime(startDateTime);
								text = timeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_START_YEAR:
							if (startDateTime != null) {
								mDate.setTime(startDateTime);
								text = yearFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_START_MONTH:
							if (startDateTime != null) {
								mDate.setTime(startDateTime);
								text = monthFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_START_DATE_OF_MONTH:
							if (startDateTime != null) {
								mDate.setTime(startDateTime);
								text = dayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_START_WEEK_DAY:
							if (startDateTime != null) {
								mDate.setTime(startDateTime);
								text = weekDayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case CURRENT_SCHEDULED_START_DATE_TIME:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								mDate.setTime(nearestCurrentOccurrenceStartDateTime);
								text = dateTimeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case CURRENT_SCHEDULED_START_TIME:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								mDate.setTime(nearestCurrentOccurrenceStartDateTime);
								text = timeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case CURRENT_SCHEDULED_START_YEAR:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								mDate.setTime(nearestCurrentOccurrenceStartDateTime);
								text = yearFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case CURRENT_SCHEDULED_START_MONTH:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								mDate.setTime(nearestCurrentOccurrenceStartDateTime);
								text = monthFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case CURRENT_SCHEDULED_START_DATE_OF_MONTH:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								mDate.setTime(nearestCurrentOccurrenceStartDateTime);
								text = dayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case CURRENT_SCHEDULED_START_WEEK_DAY:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								mDate.setTime(nearestCurrentOccurrenceStartDateTime);
								text = weekDayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case NEXT_SCHEDULED_START_DATE_TIME:
							if (nearestNextOccurrenceStartDateTime != null) {
								mDate.setTime(nearestNextOccurrenceStartDateTime);
								text = dateTimeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case NEXT_SCHEDULED_START_TIME:
							if (nearestNextOccurrenceStartDateTime != null) {
								mDate.setTime(nearestNextOccurrenceStartDateTime);
								text = timeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case NEXT_SCHEDULED_START_YEAR:
							if (nearestNextOccurrenceStartDateTime != null) {
								mDate.setTime(nearestNextOccurrenceStartDateTime);
								text = yearFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case NEXT_SCHEDULED_START_MONTH:
							if (nearestNextOccurrenceStartDateTime != null) {
								mDate.setTime(nearestNextOccurrenceStartDateTime);
								text = monthFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case NEXT_SCHEDULED_START_DATE_OF_MONTH:
							if (nearestNextOccurrenceStartDateTime != null) {
								mDate.setTime(nearestNextOccurrenceStartDateTime);
								text = dayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case NEXT_SCHEDULED_START_WEEK_DAY:
							if (nearestNextOccurrenceStartDateTime != null) {
								mDate.setTime(nearestNextOccurrenceStartDateTime);
								text = weekDayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_END_DATE_TIME:
							if (endDateTime != null) {
								mDate.setTime(endDateTime);
								text = dateTimeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								int position = stringBuilder
										.lastIndexOf(InformationUnitMatrix.DASH_AND_SPACE);
								if (position != stringBuilder.length()
										- InformationUnitMatrix.DASH_AND_SPACE.length()) {
									stringBuilder
											.append(InformationUnitMatrix.DASH_AND_SPACE);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_END_TIME:
							endDateTime = task.getEndDateTime();
							if (endDateTime != null) {
								mDate.setTime(endDateTime);
								text = timeFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								int position = stringBuilder
										.lastIndexOf(InformationUnitMatrix.DASH_AND_SPACE);
								if (position != stringBuilder.length()
										- InformationUnitMatrix.DASH_AND_SPACE.length()) {
									stringBuilder
											.append(InformationUnitMatrix.DASH_AND_SPACE);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_END_YEAR:
							endDateTime = task.getEndDateTime();
							if (endDateTime != null) {
								mDate.setTime(endDateTime);
								text = yearFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_END_MONTH:
							endDateTime = task.getEndDateTime();
							if (endDateTime != null) {
								mDate.setTime(endDateTime);
								text = monthFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_END_DATE_OF_MONTH:
							endDateTime = task.getEndDateTime();
							if (endDateTime != null) {
								mDate.setTime(endDateTime);
								text = dayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case FIRST_SCHEDULED_END_WEEK_DAY:
							endDateTime = task.getEndDateTime();
							if (endDateTime != null) {
								mDate.setTime(endDateTime);
								text = weekDayFormat.format(mDate);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case DURATION_OF_THE_TASK:
							if (taskDuration != null) {
								text = Helper.getTextForTimeInterval(context,
										taskDuration);
								if (isLastCharNotWhiteSpace) {
									stringBuilder
											.append(InformationUnitMatrix.WHITE_SPACE);
								}
								stringBuilder.append(text);
								length = stringBuilder.length();
								lastChar = stringBuilder.charAt(length - 1);
								isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							}
							break;
						case TASK_NAME:
							text = task.getName();
							if (isLastCharNotWhiteSpace) {
								stringBuilder.append(InformationUnitMatrix.WHITE_SPACE);
							}
							stringBuilder.append(text);
							length = stringBuilder.length();
							lastChar = stringBuilder.charAt(length - 1);
							isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							break;
						case INFORMATION_ABOUT_REMINDERS:
							break;
						case INFORMATION_ABOUT_REPETITION:
							break;
						case LOCATION:
							text = task.getLocation();
							if (text != null) {
								if (!text.isEmpty()) {
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case ANY_STRING:
							stringBuilder.append(informationUnit
									.getWhateverDelimiterString());
							length = stringBuilder.length();
							lastChar = stringBuilder.charAt(length - 1);
							isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
							break;
						case CURRENT_SCHEDULED_END_DATE_TIME:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestCurrentOccurrenceStartDateTime
											+ taskDuration);
									text = dateTimeFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									int position = stringBuilder
											.lastIndexOf(InformationUnitMatrix.DASH_AND_SPACE);
									if (position != stringBuilder.length()
											- InformationUnitMatrix.DASH_AND_SPACE
													.length()) {
										stringBuilder
												.append(InformationUnitMatrix.DASH_AND_SPACE);
										length = stringBuilder.length();
										lastChar = stringBuilder.charAt(length - 1);
										isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
									}
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case CURRENT_SCHEDULED_END_TIME:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestCurrentOccurrenceStartDateTime
											+ taskDuration);
									text = timeFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									int position = stringBuilder
											.lastIndexOf(InformationUnitMatrix.DASH_AND_SPACE);
									if (position != stringBuilder.length()
											- InformationUnitMatrix.DASH_AND_SPACE
													.length()) {
										stringBuilder
												.append(InformationUnitMatrix.DASH_AND_SPACE);
										length = stringBuilder.length();
										lastChar = stringBuilder.charAt(length - 1);
										isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
									}
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case CURRENT_SCHEDULED_END_YEAR:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestCurrentOccurrenceStartDateTime
											+ taskDuration);
									text = yearFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case CURRENT_SCHEDULED_END_MONTH:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestCurrentOccurrenceStartDateTime
											+ taskDuration);
									text = monthFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case CURRENT_SCHEDULED_END_DATE_OF_MONTH:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestCurrentOccurrenceStartDateTime
											+ taskDuration);
									text = dayFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case CURRENT_SCHEDULED_END_WEEK_DAY:
							if (nearestCurrentOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestCurrentOccurrenceStartDateTime
											+ taskDuration);
									text = weekDayFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case NEXT_SCHEDULED_END_DATE_TIME:
							if (nearestNextOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestNextOccurrenceStartDateTime
											+ taskDuration);
									text = dateTimeFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									int position = stringBuilder
											.lastIndexOf(InformationUnitMatrix.DASH_AND_SPACE);
									if (position != stringBuilder.length()
											- InformationUnitMatrix.DASH_AND_SPACE
													.length()) {
										stringBuilder
												.append(InformationUnitMatrix.DASH_AND_SPACE);
										length = stringBuilder.length();
										lastChar = stringBuilder.charAt(length - 1);
										isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
									}
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case NEXT_SCHEDULED_END_TIME:
							if (nearestNextOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestNextOccurrenceStartDateTime
											+ taskDuration);
									text = timeFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									int position = stringBuilder
											.lastIndexOf(InformationUnitMatrix.DASH_AND_SPACE);
									if (position != stringBuilder.length()
											- InformationUnitMatrix.DASH_AND_SPACE
													.length()) {
										stringBuilder
												.append(InformationUnitMatrix.DASH_AND_SPACE);
										length = stringBuilder.length();
										lastChar = stringBuilder.charAt(length - 1);
										isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
									}
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case NEXT_SCHEDULED_END_YEAR:
							if (nearestNextOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestNextOccurrenceStartDateTime
											+ taskDuration);
									text = yearFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case NEXT_SCHEDULED_END_MONTH:
							if (nearestNextOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestNextOccurrenceStartDateTime
											+ taskDuration);
									text = monthFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case NEXT_SCHEDULED_END_DATE_OF_MONTH:
							if (nearestNextOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestNextOccurrenceStartDateTime
											+ taskDuration);
									text = dayFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						case NEXT_SCHEDULED_END_WEEK_DAY:
							if (nearestNextOccurrenceStartDateTime != null) {
								if (taskDuration != null) {
									mDate.setTime(nearestNextOccurrenceStartDateTime
											+ taskDuration);
									text = weekDayFormat.format(mDate);
									if (isLastCharNotWhiteSpace) {
										stringBuilder
												.append(InformationUnitMatrix.WHITE_SPACE);
									}
									stringBuilder.append(text);
									length = stringBuilder.length();
									lastChar = stringBuilder.charAt(length - 1);
									isLastCharNotWhiteSpace = lastChar != InformationUnitMatrix.WHITE_SPACE_CHAR;
								}
							}
							break;
						default:
							break;
						}
					}
					if (size != 0) {
						stringBuilder.deleteCharAt(0);
						if (stringBuilder.length() > 0) {
							text = stringBuilder.toString();
							strings.add(text);
							stringBuilder.setLength(0);
						}
					}
				}
			}
		}
		return strings;
	}

	protected InformationUnitMatrix(Parcel in) {
		if (in.readByte() == 0x01) {
			informationUnitRows = new ArrayList<InformationUnitRow>();
			in.readList(informationUnitRows, InformationUnitRow.class.getClassLoader());
		} else {
			informationUnitRows = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (informationUnitRows == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(informationUnitRows);
		}
	}

	public static final Parcelable.Creator<InformationUnitMatrix> CREATOR = new Parcelable.Creator<InformationUnitMatrix>() {
		@Override
		public InformationUnitMatrix createFromParcel(Parcel in) {
			return new InformationUnitMatrix(in);
		}

		@Override
		public InformationUnitMatrix[] newArray(int size) {
			return new InformationUnitMatrix[size];
		}
	};
}
