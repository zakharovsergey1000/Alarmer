package biz.advancedcalendar.views.accessories;

import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VerticalTasksLayouter implements TasksLayouter {
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
				startDateTime, startDateTime + totalTimeLength,
				calendarViewTaskOccurrences, minTaskDuration);
		CalendarViewTaskOccurrence currentCalendarViewTaskOccurrence;
		// Here we have all the tasks in the tasks placed into the
		// taskLayoutStructure.
		// Next we need to adjust coordinates for all the tasks based on
		// the structure of taskLayoutStructure.
		int calendarViewTaskOccurrenceLeft, calendarViewTaskOccurrenceTop, calendarViewTaskOccurrenceRight, calendarViewTaskOccurrenceBottom, calendarViewTaskOccurrenceRealTop, calendarViewTaskOccurrenceRealBottom;
		// int previousLowestTaskViewBottom = Integer.MIN_VALUE;
		int lastBottomCoord = periodViewsTopBottomCoords[periodViewsTopBottomCoords.length - 1];
		for (int k = 0; k < calendarViewTaskOccurrenceLayoutStructure.size(); k++) {
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = calendarViewTaskOccurrenceLayoutStructure
					.get(k);
			int currentSlotsSize = currentSlots.size();
			float calendarViewTaskOccurrenceWidthForCurrentSlots = (float) (boundaryWidthPx - spacerXPx
					* (currentSlotsSize - 1))
					/ currentSlotsSize;
			calendarViewTaskOccurrenceRight = -spacerXPx;
			for (int j = 0; j < currentSlotsSize; j++) {
				ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots.get(j);
				int calendarViewTaskOccurrenceRight1 = calendarViewTaskOccurrenceRight;
				int currentSlotSize = currentSlot.size();
				for (int i = 0; i < currentSlotSize; i++) {
					currentCalendarViewTaskOccurrence = currentSlot.get(i);
					// reset calendarViewTaskOccurrenceRight for the
					// calendarViewTaskOccurrences in the same slot
					calendarViewTaskOccurrenceRight = calendarViewTaskOccurrenceRight1;
					long calendarViewTaskOccurrenceTopBottomCoords = getCalendarViewTaskOccurrenceTopBottomCoords(
							startDateTime, timeIntervalDurations,
							periodViewsTopBottomCoords,
							currentCalendarViewTaskOccurrence.StartDateTime,
							currentCalendarViewTaskOccurrence.EndDateTime);
					calendarViewTaskOccurrenceTop = (int) (calendarViewTaskOccurrenceTopBottomCoords >> 32);
					calendarViewTaskOccurrenceRealTop = calendarViewTaskOccurrenceTop;
					calendarViewTaskOccurrenceBottom = (int) (calendarViewTaskOccurrenceTopBottomCoords & 0xFFFFFFFF);
					calendarViewTaskOccurrenceRealBottom = calendarViewTaskOccurrenceBottom;
					if (calendarViewTaskOccurrenceRealBottom
							- calendarViewTaskOccurrenceTop == 0) {
						calendarViewTaskOccurrenceBottom = calendarViewTaskOccurrenceRealBottom = calendarViewTaskOccurrenceTop + 1;
					}
					if (calendarViewTaskOccurrenceBottom - calendarViewTaskOccurrenceTop < calendarViewTaskOccurrenceMinHeightPx) {
						if (calendarViewTaskOccurrenceTop
								+ calendarViewTaskOccurrenceMinHeightPx > lastBottomCoord) {
							calendarViewTaskOccurrenceTop = lastBottomCoord
									- calendarViewTaskOccurrenceMinHeightPx;
							calendarViewTaskOccurrenceBottom = lastBottomCoord;
						} else {
							calendarViewTaskOccurrenceBottom = calendarViewTaskOccurrenceTop
									+ calendarViewTaskOccurrenceMinHeightPx;
						}
					}
					if (calendarViewTaskOccurrenceBottom >= lastBottomCoord) {
						calendarViewTaskOccurrenceBottom = lastBottomCoord;
						if (calendarViewTaskOccurrenceRealBottom > calendarViewTaskOccurrenceBottom) {
							calendarViewTaskOccurrenceRealBottom = calendarViewTaskOccurrenceBottom;
						}
					}
					calendarViewTaskOccurrenceLeft = calendarViewTaskOccurrenceRight
							+ spacerXPx;
					if (calendarViewTaskOccurrenceLeft > boundaryWidthPx) {
						calendarViewTaskOccurrenceLeft = boundaryWidthPx;
					}
					calendarViewTaskOccurrenceRight = (int) ((calendarViewTaskOccurrenceWidthForCurrentSlots + spacerXPx)
							* j + calendarViewTaskOccurrenceWidthForCurrentSlots);
					if (calendarViewTaskOccurrenceRight < calendarViewTaskOccurrenceLeft) {
						calendarViewTaskOccurrenceRight = calendarViewTaskOccurrenceLeft;
					}
					if (calendarViewTaskOccurrenceRight > boundaryWidthPx) {
						calendarViewTaskOccurrenceRight = boundaryWidthPx;
					}
					// check if the higher located task in the same current
					// slot
					// (or the lowest task in right adjacent slot) (not implemented)
					// ends at the same
					// time as current task starts. If
					// this is the case then raise the lower boundary of the
					// higher task view by spacerYPx
					if (i > 0) {
						CalendarViewTaskOccurrence higherCalendarViewTaskOccurrence = currentSlot
								.get(i - 1);
						if (higherCalendarViewTaskOccurrence.Bottom == calendarViewTaskOccurrenceTop) {
							if (higherCalendarViewTaskOccurrence.Bottom
									- higherCalendarViewTaskOccurrence.Top > spacerYPx) {
								higherCalendarViewTaskOccurrence.Bottom = higherCalendarViewTaskOccurrence.Bottom
										- spacerYPx;
								if (higherCalendarViewTaskOccurrence.RealBottom > higherCalendarViewTaskOccurrence.Bottom) {
									higherCalendarViewTaskOccurrence.RealBottom = higherCalendarViewTaskOccurrence.Bottom;
								}
							}
						}
					}
					if (k > 0) {
						ArrayList<ArrayList<CalendarViewTaskOccurrence>> previousSlots = calendarViewTaskOccurrenceLayoutStructure
								.get(k - 1);
						int previousSlotsSize = previousSlots.size();
						for (int j1 = 0; j1 < previousSlotsSize; j1++) {
							ArrayList<CalendarViewTaskOccurrence> previousSlot = previousSlots
									.get(j1);
							CalendarViewTaskOccurrence higherCalendarViewTaskOccurrence = previousSlot
									.get(previousSlot.size() - 1);
							if (higherCalendarViewTaskOccurrence.Bottom == calendarViewTaskOccurrenceTop) {
								if (higherCalendarViewTaskOccurrence.Bottom
										- higherCalendarViewTaskOccurrence.Top > spacerYPx) {
									higherCalendarViewTaskOccurrence.Bottom = higherCalendarViewTaskOccurrence.Bottom
											- spacerYPx;
									if (higherCalendarViewTaskOccurrence.RealBottom > higherCalendarViewTaskOccurrence.Bottom) {
										higherCalendarViewTaskOccurrence.RealBottom = higherCalendarViewTaskOccurrence.Bottom;
									}
								}
							}
						}
					}
					currentCalendarViewTaskOccurrence.Left = calendarViewTaskOccurrenceLeft;
					currentCalendarViewTaskOccurrence.Top = calendarViewTaskOccurrenceTop;
					currentCalendarViewTaskOccurrence.Right = calendarViewTaskOccurrenceRight;
					currentCalendarViewTaskOccurrence.Bottom = calendarViewTaskOccurrenceBottom;
					currentCalendarViewTaskOccurrence.RealTop = calendarViewTaskOccurrenceRealTop;
					currentCalendarViewTaskOccurrence.RealBottom = calendarViewTaskOccurrenceRealBottom;
				}
			}
		}
		// expand tasks to the right if possible
		for (int k = 0; k < calendarViewTaskOccurrenceLayoutStructure.size(); k++) {
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = calendarViewTaskOccurrenceLayoutStructure
					.get(k);
			int currentSlotsSize = currentSlots.size();
			for (int j = 0; j < currentSlotsSize; j++) {
				ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots.get(j);
				int currentSlotSize = currentSlot.size();
				for (int i = 0; i < currentSlotSize; i++) {
					currentCalendarViewTaskOccurrence = currentSlot.get(i);
					for (int j1 = j + 1; j1 < currentSlotsSize; j1++) {
						ArrayList<CalendarViewTaskOccurrence> nextAdjacentSlot = currentSlots
								.get(j1);
						CalendarViewTaskOccurrence lowestInAdjacentSlotCalendarViewTaskOccurrence = nextAdjacentSlot
								.get(nextAdjacentSlot.size() - 1);
						if (currentCalendarViewTaskOccurrence.Top > lowestInAdjacentSlotCalendarViewTaskOccurrence.Bottom) {
							currentCalendarViewTaskOccurrence.Right = lowestInAdjacentSlotCalendarViewTaskOccurrence.Right;
						} else {
							break;
						}
					}
				}
			}
		}
	}

	private ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> getTaskLayoutStructure(
			final long startDateTime, final long endDateTime,
			final List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences,
			final long minTaskDuration) {
		Collections.sort(calendarViewTaskOccurrences,
				new CalendarViewTaskOccurrenceComparator(startDateTime, endDateTime));
		ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> taskLayoutStructure = new ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>>();
		ArrayList<ArrayList<CalendarViewTaskOccurrence>> seedSlots = new ArrayList<ArrayList<CalendarViewTaskOccurrence>>();
		ArrayList<CalendarViewTaskOccurrence> seedSlot = new ArrayList<CalendarViewTaskOccurrence>();
		taskLayoutStructure.add(seedSlots);
		seedSlots.add(seedSlot);
		seedSlot.add(calendarViewTaskOccurrences.get(0));
		CalendarViewTaskOccurrence tempTask, currentTask;
		for (int i = 1; i < calendarViewTaskOccurrences.size(); i++) {
			currentTask = calendarViewTaskOccurrences.get(i);
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = taskLayoutStructure
					.get(taskLayoutStructure.size() - 1);
			ArrayList<CalendarViewTaskOccurrence> slot0 = currentSlots.get(0);
			Long currentTaskStartDateTime, previousTaskStartDateTime, previousTaskEndDateTime;
			long currentTaskImaginaryStartDateTime, previousTaskImaginaryStartDateTime, previousTaskImaginaryEndDateTime;
			currentTaskStartDateTime = currentTask.StartDateTime;
			previousTaskStartDateTime = slot0.get(slot0.size() - 1).StartDateTime;
			previousTaskEndDateTime = slot0.get(slot0.size() - 1).EndDateTime;
			// setup Imaginary times
			if (currentTaskStartDateTime == null
					|| currentTaskStartDateTime < startDateTime) {
				currentTaskImaginaryStartDateTime = startDateTime;
			} else {
				currentTaskImaginaryStartDateTime = currentTaskStartDateTime;
			}
			// adjust currentTaskImaginaryStartDateTime to ensure minTaskDuration.
			if (endDateTime - currentTaskImaginaryStartDateTime < minTaskDuration) {
				// increase currentTaskImaginaryStartDateTime to ensure minTaskDuration.
				currentTaskImaginaryStartDateTime = endDateTime - minTaskDuration;
			}
			//
			if (previousTaskStartDateTime == null
					|| previousTaskStartDateTime < startDateTime) {
				previousTaskImaginaryStartDateTime = startDateTime;
			} else {
				previousTaskImaginaryStartDateTime = previousTaskStartDateTime;
			}
			//
			if (previousTaskEndDateTime == null || previousTaskEndDateTime > endDateTime) {
				previousTaskImaginaryEndDateTime = endDateTime;
			} else {
				previousTaskImaginaryEndDateTime = previousTaskEndDateTime;
			}
			// adjust previousTaskImaginaryEndDateTime to ensure minTaskDuration.
			if (previousTaskImaginaryEndDateTime - previousTaskImaginaryStartDateTime < minTaskDuration) {
				// increase previousTaskImaginaryEndDateTime to ensure minTaskDuration.
				previousTaskImaginaryEndDateTime = previousTaskImaginaryStartDateTime
						+ minTaskDuration;
			}
			// Does currentTask fit into slot0?
			if (previousTaskImaginaryEndDateTime <= currentTaskImaginaryStartDateTime) {
				// Does current WeekViewCoreTaskWithCoords intersect any other
				// slot?
				boolean isCurrentTaskIntersectSomeSlot = false;
				for (int i1 = 1; i1 < currentSlots.size(); i1++) {
					ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots
							.get(i1);
					tempTask = currentSlot.get(currentSlot.size() - 1);
					previousTaskStartDateTime = tempTask.StartDateTime;
					previousTaskEndDateTime = tempTask.EndDateTime;
					// setup Imaginary times
					if (previousTaskStartDateTime == null
							|| previousTaskStartDateTime < startDateTime) {
						previousTaskImaginaryStartDateTime = startDateTime;
					} else {
						previousTaskImaginaryStartDateTime = previousTaskStartDateTime;
					}
					//
					if (previousTaskEndDateTime == null
							|| previousTaskEndDateTime > endDateTime) {
						previousTaskImaginaryEndDateTime = endDateTime;
					} else {
						previousTaskImaginaryEndDateTime = previousTaskEndDateTime;
					}
					// adjust previousTaskImaginaryEndDateTime to ensure minTaskDuration.
					if (previousTaskImaginaryEndDateTime
							- previousTaskImaginaryStartDateTime < minTaskDuration) {
						// increase previousTaskImaginaryEndDateTime to ensure
						// minTaskDuration.
						previousTaskImaginaryEndDateTime = previousTaskImaginaryStartDateTime
								+ minTaskDuration;
					}
					//
					if (previousTaskImaginaryEndDateTime > currentTaskImaginaryStartDateTime) {
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
					ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots
							.get(currentSlotIndex);
					tempTask = currentSlot.get(currentSlot.size() - 1);
					previousTaskStartDateTime = tempTask.StartDateTime;
					previousTaskEndDateTime = tempTask.EndDateTime;
					// setup Imaginary times
					if (previousTaskStartDateTime == null
							|| previousTaskStartDateTime < startDateTime) {
						previousTaskImaginaryStartDateTime = startDateTime;
					} else {
						previousTaskImaginaryStartDateTime = previousTaskStartDateTime;
					}
					//
					if (previousTaskEndDateTime == null
							|| previousTaskEndDateTime > endDateTime) {
						previousTaskImaginaryEndDateTime = endDateTime;
					} else {
						previousTaskImaginaryEndDateTime = previousTaskEndDateTime;
					}
					// adjust previousTaskImaginaryEndDateTime to ensure minTaskDuration.
					if (previousTaskImaginaryEndDateTime
							- previousTaskImaginaryStartDateTime < minTaskDuration) {
						// increase previousTaskImaginaryEndDateTime to ensure
						// minTaskDuration.
						previousTaskImaginaryEndDateTime = previousTaskImaginaryStartDateTime
								+ minTaskDuration;
					}
					//
					if (previousTaskImaginaryEndDateTime <= currentTaskImaginaryStartDateTime) {
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

	private long getCalendarViewTaskOccurrenceTopBottomCoords(final long startDateTime,
			final long[] timeIntervalDurations,
			final int[] timeIntervalViewsTopBottomCoords,
			final Long taskOccurrenceStartDateTimeArg,
			final Long taskOccurrenceEndDateTimeArg) {
		//
		long totalLength = 0;
		for (long timeInterval : timeIntervalDurations) {
			totalLength += timeInterval;
		}
		//
		long endDateTime = startDateTime + totalLength;
		//
		long taskOccurrenceStartDateTime;
		if (taskOccurrenceStartDateTimeArg == null) {
			taskOccurrenceStartDateTime = 0;
		} else if (taskOccurrenceStartDateTimeArg <= startDateTime) {
			taskOccurrenceStartDateTime = 0;
		} else if (endDateTime <= taskOccurrenceStartDateTimeArg) {
			taskOccurrenceStartDateTime = totalLength;
		} else {
			taskOccurrenceStartDateTime = taskOccurrenceStartDateTimeArg - startDateTime;
		}
		//
		long taskOccurrenceEndDateTime;
		if (taskOccurrenceEndDateTimeArg == null) {
			taskOccurrenceEndDateTime = totalLength;
		} else if (taskOccurrenceEndDateTimeArg <= startDateTime) {
			taskOccurrenceEndDateTime = 0;
		} else if (endDateTime <= taskOccurrenceEndDateTimeArg) {
			taskOccurrenceEndDateTime = totalLength;
		} else {
			taskOccurrenceEndDateTime = taskOccurrenceEndDateTimeArg - startDateTime;
		}
		//
		int taskStartPeriod = 0;
		int taskEndPeriod = 0;
		long millisecondsInStartPeriod = taskOccurrenceStartDateTime;
		long millisecondsInEndPeriod = taskOccurrenceEndDateTime;
		boolean isTaskStartPeriodCalculated = false;
		boolean isTaskEndPeriodCalculated = false;
		long timeAccumulatedIncludingCurrentTimeIntervalDuration = 0;
		for (int currentTimeIntervalDuration = 0; !(isTaskStartPeriodCalculated && isTaskEndPeriodCalculated)
				&& currentTimeIntervalDuration < timeIntervalDurations.length; currentTimeIntervalDuration++) {
			timeAccumulatedIncludingCurrentTimeIntervalDuration += timeIntervalDurations[currentTimeIntervalDuration];
			if (!isTaskStartPeriodCalculated) {
				if (taskOccurrenceStartDateTime < timeAccumulatedIncludingCurrentTimeIntervalDuration) {
					isTaskStartPeriodCalculated = true;
					taskStartPeriod = currentTimeIntervalDuration;
					millisecondsInStartPeriod -= timeAccumulatedIncludingCurrentTimeIntervalDuration
							- timeIntervalDurations[currentTimeIntervalDuration];
				} else if (taskOccurrenceStartDateTime == timeAccumulatedIncludingCurrentTimeIntervalDuration) {
					isTaskStartPeriodCalculated = true;
					taskStartPeriod = currentTimeIntervalDuration + 1;
					millisecondsInStartPeriod = 0;
				}
			}
			if (!isTaskEndPeriodCalculated) {
				if (taskOccurrenceEndDateTime < timeAccumulatedIncludingCurrentTimeIntervalDuration) {
					isTaskEndPeriodCalculated = true;
					taskEndPeriod = currentTimeIntervalDuration;
					millisecondsInEndPeriod -= timeAccumulatedIncludingCurrentTimeIntervalDuration
							- timeIntervalDurations[currentTimeIntervalDuration];
				} else if (taskOccurrenceEndDateTime == timeAccumulatedIncludingCurrentTimeIntervalDuration) {
					isTaskEndPeriodCalculated = true;
					if (taskOccurrenceStartDateTime == taskOccurrenceEndDateTime) {
						taskEndPeriod = currentTimeIntervalDuration + 1;
						millisecondsInEndPeriod = 0;
					} else {
						taskEndPeriod = currentTimeIntervalDuration;
						millisecondsInEndPeriod = timeIntervalDurations[currentTimeIntervalDuration];
					}
				}
			}
			// if (!isTaskEndPeriodCalculated
			// && taskOccurrenceEndDateTime <=
			// timeAccumulatedIncludingCurrentTimeIntervalDuration) {
			// isTaskEndPeriodCalculated = true;
			// taskEndPeriod = currentTimeIntervalDuration;
			// millisecondsInEndPeriod -=
			// timeAccumulatedIncludingCurrentTimeIntervalDuration
			// // - timeIntervalDurations[taskEndPeriod]
			// ;
			// }
			// // check for special case when task end time is strictly
			// // between two periods and set taskEndPeriod and
			// // millisecondsInEndPeriod
			// // accordingly
			// if (taskOccurrenceEndDateTime ==
			// timeAccumulatedIncludingCurrentTimeIntervalDuration) {
			// isTaskEndPeriodCalculated = true;
			// if (taskOccurrenceEndDateTime != taskOccurrenceStartDateTime) {
			// taskEndPeriod = currentTimeIntervalDuration - 1;
			// millisecondsInEndPeriod = timeIntervalDurations[taskEndPeriod];
			// } else {
			// taskEndPeriod = taskStartPeriod;
			// millisecondsInEndPeriod = 0;
			// }
			// }
		}
		//
		// int startPeriodHeight = periodViewsTopBottomCoords[taskStartPeriod * 2 + 1]
		// - periodViewsTopBottomCoords[taskStartPeriod * 2];
		// int endPeriodHeight = periodViewsTopBottomCoords[taskEndPeriod * 2 + 1]
		// - periodViewsTopBottomCoords[taskEndPeriod * 2];
		// int taskStartPeriod_inPeriodPx = (int) ((float) startPeriodHeight
		// * millisecondsInStartPeriod / timeIntervalDurations[taskStartPeriod] + 0.5);
		// int taskEndPeriod_inPeriodPx = (int) ((float) endPeriodHeight
		// * millisecondsInEndPeriod / timeIntervalDurations[taskEndPeriod] + 0.5);
		// int taskViewTop = periodViewsTopBottomCoords[taskStartPeriod * 2]
		// + taskStartPeriod_inPeriodPx;
		// int taskViewBottom = periodViewsTopBottomCoords[taskEndPeriod * 2]
		// + taskEndPeriod_inPeriodPx;
		//
		long resultTop;
		long resultBottom;
		if (taskStartPeriod * 2 >= timeIntervalViewsTopBottomCoords.length) {
			resultTop = resultBottom = timeIntervalViewsTopBottomCoords[timeIntervalViewsTopBottomCoords.length - 1];
		} else {
			resultTop = timeIntervalViewsTopBottomCoords[taskStartPeriod * 2]
					+ (int) ((float) (timeIntervalViewsTopBottomCoords[taskStartPeriod * 2 + 1] - timeIntervalViewsTopBottomCoords[taskStartPeriod * 2])
							* millisecondsInStartPeriod
							/ timeIntervalDurations[taskStartPeriod] + 0.5);
			resultBottom = timeIntervalViewsTopBottomCoords[taskEndPeriod * 2]
					+ (int) ((float) (timeIntervalViewsTopBottomCoords[taskEndPeriod * 2 + 1] - timeIntervalViewsTopBottomCoords[taskEndPeriod * 2])
							* millisecondsInEndPeriod
							/ timeIntervalDurations[taskEndPeriod] + 0.5);
		}
		long result = resultTop << 32 | resultBottom;
		return result;
	}
}
