package biz.advancedcalendar.views.accessories;

import java.util.ArrayList;
import java.util.Calendar;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;

/** @author Sergey */
public class EventsLayouterForMonth {
	/** Calculates coordinates to the event views on one week. Implements some intricate
	 * logic to calculate coordinates.
	 * 
	 * @param weekStartTime
	 *            The start time. Must be at beginning of a day
	 * @param eventViewCoords
	 *            The list of events for which coordinates to be calculated
	 * @param dayViewsLeftRightCoords
	 *            The list of coordinates of hour views relative to which event views have
	 *            to be layouted
	 * @param dayViewHeight
	 *            The height of the day views
	 * @param density
	 *            This should be the getResources().getDisplayMetrics().density */
	public void calculateEventsCoords(long weekStartTime,
			ArrayList<CalendarViewTaskOccurrence> eventViewCoordsWithAdjustedTime,
			ArrayList<Integer> dayViewsLeftRightCoords, int dayViewHeight, float mDensity) {
		if (eventViewCoordsWithAdjustedTime == null
				|| eventViewCoordsWithAdjustedTime.size() == 0) {
			return;
		}
		ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>> positionedEventViewCoords = new ArrayList<ArrayList<ArrayList<CalendarViewTaskOccurrence>>>();
		ArrayList<ArrayList<CalendarViewTaskOccurrence>> seedSlots = new ArrayList<ArrayList<CalendarViewTaskOccurrence>>();
		ArrayList<CalendarViewTaskOccurrence> seedSlot = new ArrayList<CalendarViewTaskOccurrence>();
		positionedEventViewCoords.add(seedSlots);
		seedSlots.add(seedSlot);
		seedSlot.add(eventViewCoordsWithAdjustedTime.get(0));
		CalendarViewTaskOccurrence tempCalendarViewTaskOccurrence, currentCalendarViewTaskOccurrence;
		for (int i = 1; i < eventViewCoordsWithAdjustedTime.size(); i++) {
			currentCalendarViewTaskOccurrence = eventViewCoordsWithAdjustedTime.get(i);
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = positionedEventViewCoords
					.get(positionedEventViewCoords.size() - 1);
			ArrayList<CalendarViewTaskOccurrence> slot0 = currentSlots.get(0);
			// Does current view fit into slot0?
			if (slot0.get(slot0.size() - 1).EndDateTime <= currentCalendarViewTaskOccurrence.StartDateTime) {
				// Does current view intersect any other slot?
				boolean currentViewIntersectsSomeSlot = false;
				for (int currentSlotIndex = 1; currentSlotIndex < currentSlots.size(); currentSlotIndex++) {
					ArrayList<CalendarViewTaskOccurrence> tempSlot = currentSlots
							.get(currentSlotIndex);
					tempCalendarViewTaskOccurrence = tempSlot.get(tempSlot.size() - 1);
					if (tempCalendarViewTaskOccurrence.EndDateTime > currentCalendarViewTaskOccurrence.StartDateTime) {
						// The current event view does intersect some other slot
						currentViewIntersectsSomeSlot = true;
						break;
					}
				}
				if (currentViewIntersectsSomeSlot) {
					// Just add current view into the current system of slots
					slot0.add(currentCalendarViewTaskOccurrence);
				} else {
					// First, begin whole new system of slots
					ArrayList<ArrayList<CalendarViewTaskOccurrence>> newSlots = new ArrayList<ArrayList<CalendarViewTaskOccurrence>>();
					ArrayList<CalendarViewTaskOccurrence> newSlot = new ArrayList<CalendarViewTaskOccurrence>();
					positionedEventViewCoords.add(newSlots);
					newSlots.add(newSlot);
					// Next, add current view into the newly created system of
					// slots
					newSlot.add(currentCalendarViewTaskOccurrence);
				}
			} else // When current view does not fit into slot0
			{
				// Does current event view fit into any other slot?
				boolean isFitAnyOtherSlot = false;
				int indexFitted = -1;
				for (int currentSlotIndex = 1; currentSlotIndex < currentSlots.size(); currentSlotIndex++) {
					ArrayList<CalendarViewTaskOccurrence> tempSlot = currentSlots
							.get(currentSlotIndex);
					tempCalendarViewTaskOccurrence = tempSlot.get(tempSlot.size() - 1);
					if (tempCalendarViewTaskOccurrence.EndDateTime <= currentCalendarViewTaskOccurrence.StartDateTime) {
						// The current event view does fit into some other slot
						isFitAnyOtherSlot = true;
						indexFitted = currentSlotIndex;
						break;
					}
				}
				if (isFitAnyOtherSlot) {
					// Just add current view into the existing slot in the
					// current system of slots
					currentSlots.get(indexFitted).add(currentCalendarViewTaskOccurrence);
				} else {
					// First, add new slot to the current system of slots
					ArrayList<CalendarViewTaskOccurrence> newSlot = new ArrayList<CalendarViewTaskOccurrence>();
					currentSlots.add(newSlot);
					// Next, add current view into the newly created slot
					newSlot.add(currentCalendarViewTaskOccurrence);
				}
			}
		}
		// Here we have placed all the events in the events into the
		// positionedMyEvents.
		// Next we need to adjust coordinates for all the events based on
		// the structure of positionedMyEvents.
		int dayHeightPx = dayViewHeight;
		int eventViewLeft, eventViewTop, eventViewRight, eventViewBottom;
		for (int i = 0; i < positionedEventViewCoords.size(); i++) {
			ArrayList<ArrayList<CalendarViewTaskOccurrence>> currentSlots = positionedEventViewCoords
					.get(i);
			int eventViewHeightForCurrentSlots = (int) ((float) dayHeightPx / currentSlots
					.size());
			for (int j = 0; j < currentSlots.size(); j++) {
				ArrayList<CalendarViewTaskOccurrence> currentSlot = currentSlots.get(j);
				for (int k = 0; k < currentSlot.size(); k++) {
					currentCalendarViewTaskOccurrence = currentSlot.get(k);
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(weekStartTime);
					// int year = calendar.get(Calendar.YEAR);
					// int month = calendar.get(Calendar.MONTH);
					// int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
					// int hour = calendar.get(Calendar.HOUR_OF_DAY);
					// int minute = calendar.get(Calendar.MINUTE);
					// int second = calendar.get(Calendar.SECOND);
					// int millisecond = calendar.get(Calendar.MILLISECOND);
					long start = currentCalendarViewTaskOccurrence.StartDateTime - weekStartTime;
					if (start < 0) {
						start = 0;
					}
					long end = currentCalendarViewTaskOccurrence.EndDateTime - weekStartTime;
					if (end >= 1000 * 60 * 60 * 24 * 7) {
						end = 1000 * 60 * 60 * 24 * 7;
					}
					boolean endTimeIsStrictlyBeetwenTwoDays = end % (1000 * 60 * 60 * 24) == 0;
					int currentEventStartDay = (int) (start / (1000 * 60 * 60 * 24));
					int currentEventEndDay = (int) (end / (1000 * 60 * 60 * 24));
					// check for special case when event end time is strictly
					// between two hours and adjust currentEventEndHour
					// accordingly
					if (endTimeIsStrictlyBeetwenTwoDays) {
						currentEventEndDay--;
					}
					int startDayWidth = dayViewsLeftRightCoords
							.get(currentEventStartDay * 2 + 1)
							- dayViewsLeftRightCoords.get(currentEventStartDay * 2);
					int endDayWidth = dayViewsLeftRightCoords
							.get(currentEventEndDay * 2 + 1)
							- dayViewsLeftRightCoords.get(currentEventEndDay * 2);
					long millisecondsInStartDay = start % (1000 * 60 * 60 * 24);
					long millisecondsInEndDay = end % (1000 * 60 * 60 * 24);
					long millisecondsInWholeDay = 1000 * 60 * 60 * 24;
					// check for special case when event end time is strictly
					// between two hours and adjust millisecondsInEndHour
					// accordingly
					if (endTimeIsStrictlyBeetwenTwoDays) {
						millisecondsInEndDay = millisecondsInWholeDay;
					}
					int currentEventStartDay_inDayPx = Math.round(0.0f + startDayWidth
							* millisecondsInStartDay / millisecondsInWholeDay);
					int currentEventEndDay_inDayPx = Math.round(0.0f + endDayWidth
							* millisecondsInEndDay / millisecondsInWholeDay);
					eventViewTop = eventViewHeightForCurrentSlots * j;
					eventViewBottom = eventViewTop + eventViewHeightForCurrentSlots - 1;
					int startDayLeft = dayViewsLeftRightCoords
							.get(currentEventStartDay * 2);
					eventViewLeft = startDayLeft + currentEventStartDay_inDayPx;
					int endDayLeft = dayViewsLeftRightCoords.get(currentEventEndDay * 2);
					eventViewRight = endDayLeft + currentEventEndDay_inDayPx;
					// check if the higher located event in the same current
					// slot ends at the same time as current event starts. If
					// this is the case then raise the lower boundary of the
					// higher event view by 1 dip
					// if (k > 0) {
					// EventViewCoord higherEventView = currentSlot.get(k - 1);
					// if ((higherEventView.bottom) == eventViewTop) {
					// int oneDip = (int) density;
					// if (oneDip == 0) {
					// oneDip = 1;
					// }
					// if ((higherEventView.bottom - higherEventView.top) >
					// oneDip) {
					// higherEventView.bottom = higherEventView.bottom
					// - oneDip;
					// }
					// }
					// }
					currentCalendarViewTaskOccurrence.Left = eventViewLeft;
					currentCalendarViewTaskOccurrence.Top = eventViewTop;
					currentCalendarViewTaskOccurrence.Right = eventViewRight;
					currentCalendarViewTaskOccurrence.Bottom = eventViewBottom;
				}
			}
		}
	}
}
