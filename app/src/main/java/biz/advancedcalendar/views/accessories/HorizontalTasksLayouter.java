package biz.advancedcalendar.views.accessories;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;

/** @author Sergey */
public class HorizontalTasksLayouter implements TasksLayouter {
	/** Calculates coordinates to the task views on set of periods. Implements some
	 * intricate logic to calculate coordinates.
	 *
	 * @param startTime
	 *            The start time.
	 * @param periodLength
	 *            The length of a period.
	 * @param calendarViewTaskOccurrences
	 *            The list of tasks for which coordinates to be calculated
	 * @param periodViewsTopBottomCoords
	 *            The list of coordinates of views relative to which task views have to be
	 *            layouted
	 * @param externalBoundaryWidth
	 *            The external boundary width
	 * @param minTaskHeightDip
	 *            The minimum Task Height Dip
	 * @param spacerXPx
	 *            The space to be leaved between task views
	 * @param spacerYPx
	 *            The space to be leaved between task views
	 * @param density
	 *            This should be the getResources().getDisplayMetrics().density */
	@Override
	public void calculateTasksCoords1(final long startDateTime,
			final List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences,
			final long[] timeIntervalDurations, final int[] periodViewsTopBottomCoords,
			final int boundaryWidthPx, final int calendarViewTaskOccurrenceMinHeightPx,
			final int spacerXPx, final int spacerYPx) {
		// int periodLength = 0;
		// long startDateTime = 0;
		long totalTimeLength = 0;
		for (long timeInterval : timeIntervalDurations) {
			totalTimeLength += timeInterval;
		}
		int totalSpaceLengthPx = 0;
		for (int i = 0; i < periodViewsTopBottomCoords.length; i += 2) {
			totalSpaceLengthPx += periodViewsTopBottomCoords[i + 1]
					- periodViewsTopBottomCoords[i];
		}
		long minTaskDuration = (long) (1.0 * totalTimeLength
				* calendarViewTaskOccurrenceMinHeightPx / totalSpaceLengthPx + 0.5);
		ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> calendarViewTaskOccurrenceLayoutStructure = getTaskLayoutStructure(
				calendarViewTaskOccurrences, minTaskDuration);
		CalendarViewTaskOccurrence currentCalendarViewTaskOccurrence;
		// Here we have all the tasks in the tasks placed into the
		// taskLayoutStructure.
		// Next we need to adjust coordinates for all the tasks based on
		// the structure of taskLayoutStructure.
		int calendarViewTaskOccurrenceLeft, calendarViewTaskOccurrenceTop, calendarViewTaskOccurrenceRight, calendarViewTaskOccurrenceBottom;
		// int previousLowestTaskViewBottom = Integer.MIN_VALUE;
		for (int i = 0; i < calendarViewTaskOccurrenceLayoutStructure.size(); i++) {
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = calendarViewTaskOccurrenceLayoutStructure
					.get(i);
			float taskViewWidthForCurrentSlots = (float) (boundaryWidthPx - spacerXPx
					* (currentSlots.size() - 1))
					/ currentSlots.size();
			calendarViewTaskOccurrenceRight = -spacerXPx;
			for (int j = 0; j < currentSlots.size(); j++) {
				ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots.get(j);
				int taskViewRight1 = calendarViewTaskOccurrenceRight;
				for (int k = 0; k < currentSlot.size(); k++) {
					currentCalendarViewTaskOccurrence = currentSlot.get(k);
					// reset taskViewRight for the tasks in the same slot
					calendarViewTaskOccurrenceRight = taskViewRight1;
					long taskViewTopBottomCoords = getTaskViewTopBottomCoords(
							0,
							periodViewsTopBottomCoords,
							currentCalendarViewTaskOccurrence.StartDateTime == null ? Long.MIN_VALUE
									: currentCalendarViewTaskOccurrence.StartDateTime
											- startDateTime,
							currentCalendarViewTaskOccurrence.EndDateTime == null ? Long.MAX_VALUE
									: currentCalendarViewTaskOccurrence.EndDateTime
											- startDateTime);
					calendarViewTaskOccurrenceTop = (int) (taskViewTopBottomCoords >> 32);
					calendarViewTaskOccurrenceBottom = (int) (taskViewTopBottomCoords & 0xFFFFFFFF);
					if (calendarViewTaskOccurrenceBottom - calendarViewTaskOccurrenceTop < calendarViewTaskOccurrenceMinHeightPx) {
						calendarViewTaskOccurrenceBottom = calendarViewTaskOccurrenceTop
								+ calendarViewTaskOccurrenceMinHeightPx;
					}
					if (calendarViewTaskOccurrenceBottom >= periodViewsTopBottomCoords[periodViewsTopBottomCoords.length - 1]) {
						calendarViewTaskOccurrenceBottom = periodViewsTopBottomCoords[periodViewsTopBottomCoords.length - 1];
					}
					calendarViewTaskOccurrenceLeft = calendarViewTaskOccurrenceRight
							+ spacerXPx;
					if (calendarViewTaskOccurrenceLeft > boundaryWidthPx) {
						calendarViewTaskOccurrenceLeft = boundaryWidthPx;
					}
					calendarViewTaskOccurrenceRight = (int) ((taskViewWidthForCurrentSlots + spacerXPx)
							* j + taskViewWidthForCurrentSlots);
					if (calendarViewTaskOccurrenceRight < calendarViewTaskOccurrenceLeft) {
						calendarViewTaskOccurrenceRight = calendarViewTaskOccurrenceLeft;
					}
					if (calendarViewTaskOccurrenceRight > boundaryWidthPx) {
						calendarViewTaskOccurrenceRight = boundaryWidthPx;
					}
					// check if the higher located task in the same current
					// slot ends at the same time as current task starts. If
					// this is the case then raise the lower boundary of the
					// higher task view by spacerYPx
					if (k > 0) {
						CalendarViewTaskOccurrence higherTaskView = currentSlot
								.get(k - 1);
						if (higherTaskView.Bottom == calendarViewTaskOccurrenceTop) {
							if (higherTaskView.Bottom - higherTaskView.Top > spacerYPx) {
								higherTaskView.Bottom = higherTaskView.Bottom - spacerYPx;
							}
						}
					}
					if (i > 0) {
						ArrayList<ArrayList<CalendarViewTaskOccurrence>> previousSlots = calendarViewTaskOccurrenceLayoutStructure
								.get(i - 1);
						for (int j1 = 0; j1 < previousSlots.size(); j1++) {
							ArrayList<CalendarViewTaskOccurrence> previousSlot = previousSlots
									.get(j1);
							CalendarViewTaskOccurrence higherTaskView = previousSlot
									.get(previousSlot.size() - 1);
							if (higherTaskView.Bottom == calendarViewTaskOccurrenceTop) {
								if (higherTaskView.Bottom - higherTaskView.Top > spacerYPx) {
									higherTaskView.Bottom = higherTaskView.Bottom
											- spacerYPx;
								}
							}
						}
					}
					currentCalendarViewTaskOccurrence.Left = calendarViewTaskOccurrenceLeft;
					currentCalendarViewTaskOccurrence.Top = calendarViewTaskOccurrenceTop;
					currentCalendarViewTaskOccurrence.Right = calendarViewTaskOccurrenceRight;
					currentCalendarViewTaskOccurrence.Bottom = calendarViewTaskOccurrenceBottom;
				}
			}
		}
	}

	// @Override
	// public int getWidth(final List<CalendarViewTaskOccurrence> tasks,
	// final long minTaskDuration, final int taskWidth, final int spacerXPx,
	// final float density) {
	// if (tasks.size() == 0) {
	// return 0;
	// }
	// // List<CalendarViewTaskOccurrence> weekViewCoreTaskWithCoordsList = new
	// // ArrayList<CalendarViewTaskOccurrence>();
	// // for (CalendarViewTaskOccurrence task : tasks) {
	// // CalendarViewTaskOccurrence weekViewCoreTaskWithCoords = new
	// // CalendarViewTaskOccurrence();
	// // weekViewCoreTaskWithCoords = task;
	// // weekViewCoreTaskWithCoordsList.add(weekViewCoreTaskWithCoords);
	// // }
	// ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> taskLayoutStructure =
	// getTaskLayoutStructure(
	// tasks, minTaskDuration);
	// // Here we have all the tasks in the tasks placed into the
	// // positionedMyTasks.
	// // Next we need to adjust coordinates for all the tasks based on
	// // the structure of positionedMyTasks.
	// CalendarViewTaskOccurrence currentTaskWithCoords;
	// int taskViewLeft, taskViewRight;
	// for (int i = 0; i < taskLayoutStructure.size(); i++) {
	// ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = taskLayoutStructure
	// .get(i);
	// for (int j = 0; j < currentSlots.size(); j++) {
	// ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots.get(j);
	// for (int k = 0; k < currentSlot.size(); k++) {
	// currentTaskWithCoords = currentSlot.get(k);
	// taskViewLeft = (taskWidth + spacerXPx) * j;
	// taskViewRight = taskViewLeft + taskWidth;
	// currentTaskWithCoords.Left = taskViewLeft;
	// currentTaskWithCoords.Right = taskViewRight;
	// }
	// }
	// }
	// int right = 0;
	// for (int i = 0; i < tasks.size(); i++) {
	// if (right < tasks.get(i).Right)
	// right = tasks.get(i).Right;
	// }
	// return right;
	// }
	private class WeekViewCoreTaskWithCoordsComparator implements
			Comparator<CalendarViewTaskOccurrence> {
		@Override
		public int compare(CalendarViewTaskOccurrence a, CalendarViewTaskOccurrence b) {
			if (a.StartDateTime == null && b.StartDateTime == null) {
				return 0;
			}
			if (a.StartDateTime == null && b.StartDateTime != null) {
				return -1;
			}
			if (a.StartDateTime != null && b.StartDateTime == null) {
				return 1;
			}
			return a.StartDateTime < b.StartDateTime ? -1
					: a.StartDateTime == b.StartDateTime ? 0 : 1;
		}
	}

	private ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> getTaskLayoutStructure(
			final List<CalendarViewTaskOccurrence> taskList, final long minTaskDuration) {
		Collections.sort(taskList, new WeekViewCoreTaskWithCoordsComparator());
		ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> taskLayoutStructure = new ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>>();
		ArrayList<ArrayList<CalendarViewTaskOccurrence>> seedSlots = new ArrayList<ArrayList<CalendarViewTaskOccurrence>>();
		ArrayList<CalendarViewTaskOccurrence> seedSlot = new ArrayList<CalendarViewTaskOccurrence>();
		taskLayoutStructure.add(seedSlots);
		seedSlots.add(seedSlot);
		seedSlot.add(taskList.get(0));
		CalendarViewTaskOccurrence tempTask, currentTask;
		for (int i = 1; i < taskList.size(); i++) {
			currentTask = taskList.get(i);
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = taskLayoutStructure
					.get(taskLayoutStructure.size() - 1);
			ArrayList<CalendarViewTaskOccurrence> slot0 = currentSlots.get(0);
			Long currentTaskStartDateTime, previousTaskStartDateTime, previousTaskEndDateTime;
			currentTaskStartDateTime = currentTask.StartDateTime;
			previousTaskStartDateTime = slot0.get(slot0.size() - 1).StartDateTime;
			previousTaskEndDateTime = slot0.get(slot0.size() - 1).EndDateTime;
			// adjust previousTaskEndDateTime to ensure minTaskDuration.
			if (previousTaskEndDateTime != null
					&& previousTaskStartDateTime != null
					&& previousTaskEndDateTime - previousTaskStartDateTime < minTaskDuration) {
				// increase previousTaskEndDateTime to ensure minTaskDuration.
				previousTaskEndDateTime = previousTaskStartDateTime + minTaskDuration;
			}
			// Does currentTask fit into slot0?
			if (currentTaskStartDateTime != null && previousTaskEndDateTime != null
					&& previousTaskEndDateTime <= currentTaskStartDateTime) {
				// Does current WeekViewCoreTaskWithCoords intersect any other
				// slot?
				boolean isCurrentTaskIntersectSomeSlot = false;
				for (int currentSlotIndex = 1; currentSlotIndex < currentSlots.size(); currentSlotIndex++) {
					ArrayList<CalendarViewTaskOccurrence> tempSlot = currentSlots
							.get(currentSlotIndex);
					tempTask = tempSlot.get(tempSlot.size() - 1);
					currentTaskStartDateTime = currentTask.StartDateTime;
					previousTaskStartDateTime = tempTask.StartDateTime;
					previousTaskEndDateTime = tempTask.EndDateTime;
					// adjust previousTaskEndDateTime to ensure minTaskDuration.
					if (previousTaskEndDateTime != null
							&& previousTaskStartDateTime != null
							&& previousTaskEndDateTime - previousTaskStartDateTime < minTaskDuration) {
						// increase previousTaskEndDateTime to ensure
						// minTaskDuration.
						previousTaskEndDateTime = previousTaskStartDateTime
								+ minTaskDuration;
					}
					if (currentTaskStartDateTime == null
							|| previousTaskEndDateTime == null
							|| previousTaskEndDateTime > currentTaskStartDateTime) {
						// The current task view does intersect some other slot
						isCurrentTaskIntersectSomeSlot = true;
						break;
					}
				}
				if (isCurrentTaskIntersectSomeSlot) {
					// Just add current view into the current system of slots
					slot0.add(currentTask);
				} else {
					// First, begin whole new system of slots
					ArrayList<ArrayList<CalendarViewTaskOccurrence>> newSlots = new ArrayList<ArrayList<CalendarViewTaskOccurrence>>();
					ArrayList<CalendarViewTaskOccurrence> newSlot = new ArrayList<CalendarViewTaskOccurrence>();
					taskLayoutStructure.add(newSlots);
					newSlots.add(newSlot);
					// Next, add current view into the newly created system of
					// slots
					newSlot.add(currentTask);
				}
			} else // When current view does not fit into slot0
			{
				// Does current task view fit into any other slot?
				boolean isFitAnyOtherSlot = false;
				int indexFitted = -1;
				for (int currentSlotIndex = 1; currentSlotIndex < currentSlots.size(); currentSlotIndex++) {
					ArrayList<CalendarViewTaskOccurrence> tempSlot = currentSlots
							.get(currentSlotIndex);
					tempTask = tempSlot.get(tempSlot.size() - 1);
					currentTaskStartDateTime = currentTask.StartDateTime;
					previousTaskStartDateTime = tempTask.StartDateTime;
					previousTaskEndDateTime = tempTask.EndDateTime;
					// adjust previousTaskEndDateTime to ensure minTaskDuration.
					if (previousTaskEndDateTime != null
							&& previousTaskStartDateTime != null
							&& previousTaskEndDateTime - previousTaskStartDateTime < minTaskDuration) {
						// increase previousTaskEndDateTime to ensure
						// minTaskDuration.
						previousTaskEndDateTime = previousTaskStartDateTime
								+ minTaskDuration;
					}
					if (currentTaskStartDateTime != null
							&& previousTaskEndDateTime != null
							&& previousTaskEndDateTime <= currentTaskStartDateTime) {
						// The current task view does fit into some other slot
						isFitAnyOtherSlot = true;
						indexFitted = currentSlotIndex;
						break;
					}
				}
				if (isFitAnyOtherSlot) {
					// Just add current view into the existing slot in the
					// current system of slots
					currentSlots.get(indexFitted).add(currentTask);
				} else {
					// First, add new slot to the current system of slots
					ArrayList<CalendarViewTaskOccurrence> newSlot = new ArrayList<CalendarViewTaskOccurrence>();
					currentSlots.add(newSlot);
					// Next, add current view into the newly created slot
					newSlot.add(currentTask);
				}
			}
		}
		return taskLayoutStructure;
	}

	// private long getTaskEndDateTime(final long periodLength,
	// final List<Integer> periodViewsTopBottomCoords,
	// final int taskViewTop, final int taskViewHeight) {
	// final int periodCount = periodViewsTopBottomCoords.size() / 2;
	// int taskViewBottom = taskViewTop + taskViewHeight;
	// if (taskViewBottom >= periodViewsTopBottomCoords
	// .get(periodViewsTopBottomCoords.size() - 1)) {
	// return periodLength * periodCount;
	//
	// }
	//
	// int i = 1;
	// while (i < periodViewsTopBottomCoords.size() - 1
	// && taskViewBottom > periodViewsTopBottomCoords.get(i)) {
	// i += 2;
	// }
	// long taskEndDateTime = (long) ((periodViewsTopBottomCoords.get(i) -
	// (taskViewTop + taskViewHeight))
	// * periodLength * 1.0 / (periodViewsTopBottomCoords.get(i) -
	// periodViewsTopBottomCoords
	// .get(i - 1)));
	// return taskEndDateTime;
	// }
	private long getTaskViewTopBottomCoords(final long periodLength,
			final int[] periodViewsTopBottomCoords, final Long taskStartDateTime,
			final Long taskEndDateTime) {
		final int periodCount = periodViewsTopBottomCoords.length / 2;
		Long startDateTime1 = taskStartDateTime;
		if (startDateTime1 == null) {
			startDateTime1 = 0L;
		}
		if (startDateTime1 < 0) {
			startDateTime1 = 0L;
		}
		Long endDateTime1 = taskEndDateTime;
		if (endDateTime1 == null) {
			endDateTime1 = periodLength * periodCount;
		}
		if (endDateTime1 >= periodLength * periodCount) {
			endDateTime1 = periodLength * periodCount;
		}
		boolean endTimeIsStrictlyBeetwenTwoPeriods = endDateTime1 % periodLength == 0;
		int currentTaskStartPeriod = (int) (startDateTime1 / periodLength);
		int currentTaskEndPeriod = (int) (endDateTime1 / periodLength);
		// check for special case when task end time is strictly
		// between two periods and adjust currentTaskEndPeriod
		// accordingly
		if (endTimeIsStrictlyBeetwenTwoPeriods) {
			currentTaskEndPeriod--;
		}
		int startPeriodHeight = periodViewsTopBottomCoords[currentTaskStartPeriod * 2 + 1]
				- periodViewsTopBottomCoords[currentTaskStartPeriod * 2];
		int endPeriodHeight = periodViewsTopBottomCoords[currentTaskEndPeriod * 2 + 1]
				- periodViewsTopBottomCoords[currentTaskEndPeriod * 2];
		long millisecondsInStartPeriod = startDateTime1 % periodLength;
		long millisecondsInEndPeriod = endDateTime1 % periodLength;
		// check for special case when task end time is strictly
		// between two periods and adjust millisecondsInEndPeriod
		// accordingly
		if (endTimeIsStrictlyBeetwenTwoPeriods) {
			millisecondsInEndPeriod = periodLength;
		}
		int currentTaskStartPeriod_inPeriodPx = Math.round(0.0f + startPeriodHeight
				* millisecondsInStartPeriod / periodLength);
		int currentTaskEndPeriod_inPeriodPx = Math.round(0.0f + endPeriodHeight
				* millisecondsInEndPeriod / periodLength);
		int startPeriodTop = periodViewsTopBottomCoords[currentTaskStartPeriod * 2];
		int taskViewTop, taskViewBottom;
		taskViewTop = startPeriodTop + currentTaskStartPeriod_inPeriodPx;
		int endPeriodTop = periodViewsTopBottomCoords[currentTaskEndPeriod * 2];
		taskViewBottom = endPeriodTop + currentTaskEndPeriod_inPeriodPx;
		long result = 0L;
		result = result | taskViewTop;
		result = result << 32;
		result = result | taskViewBottom;
		return result;
	}
}
