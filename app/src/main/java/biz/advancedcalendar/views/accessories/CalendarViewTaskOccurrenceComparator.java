package biz.advancedcalendar.views.accessories;

import java.util.Comparator;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;

public class CalendarViewTaskOccurrenceComparator implements
		Comparator<CalendarViewTaskOccurrence> {
	private long mStartDateTime;
	private long mEndDateTime;

	public CalendarViewTaskOccurrenceComparator(long startDateTime, long endDateTime) {
		mStartDateTime = startDateTime;
		mEndDateTime = endDateTime;
	}

	@Override
	public int compare(CalendarViewTaskOccurrence a, CalendarViewTaskOccurrence b) {
		if (a.StartDateTime == null) {
			if (b.StartDateTime == null) {
				return compareEndDateTime(a, b);
			} else {
				if (mStartDateTime < b.StartDateTime) {
					// a<b
					return -1;
				} else {
					return compareEndDateTime(a, b);
				}
			}
		} else {
			if (b.StartDateTime == null) {
				if (mStartDateTime < a.StartDateTime) {
					// a>b
					return 1;
				} else {
					return compareEndDateTime(a, b);
				}
			} else {
				if (a.StartDateTime < b.StartDateTime) {
					// a<b
					return -1;
				} else if (a.StartDateTime > b.StartDateTime) {
					// a>b
					return 1;
				} else {
					return compareEndDateTime(a, b);
				}
			}
		}
		//
		// if ((a.StartDateTime == null || a.StartDateTime <= mStartDateTime)
		// && (b.StartDateTime == null || b.StartDateTime <= mStartDateTime)
		// || a.StartDateTime != null && b.StartDateTime != null
		// && a.StartDateTime == b.StartDateTime) {
		// // analyze EndDateTime
		// if ((a.EndDateTime == null || a.EndDateTime >= mEndDateTime)
		// && (b.EndDateTime == null || b.EndDateTime >= mEndDateTime)
		// || a.EndDateTime != null && b.EndDateTime != null
		// && a.EndDateTime == b.EndDateTime) {
		// return 0;
		// }
		// if (a.EndDateTime != null && b.EndDateTime == null
		// || a.EndDateTime < b.EndDateTime) {
		// return -1;
		// } else
		// return 1;
		// }
		// if (a.StartDateTime == null && b.StartDateTime != null
		// || a.StartDateTime < b.StartDateTime) {
		// return -1;
		// } else
		// return 1;
	}

	private int compareEndDateTime(CalendarViewTaskOccurrence a,
			CalendarViewTaskOccurrence b) {
		if (a.EndDateTime == null) {
			if (b.EndDateTime == null) {
				return 0;
			} else {
				if (b.EndDateTime < mEndDateTime) {
					// a>b
					return 1;
				} else {
					return 0;
				}
			}
		} else {
			if (b.EndDateTime == null) {
				if (a.EndDateTime < mEndDateTime) {
					// a<b
					return -1;
				} else {
					return 0;
				}
			} else {
				if (a.EndDateTime < b.EndDateTime) {
					// a<b
					return -1;
				} else if (a.EndDateTime > b.EndDateTime) {
					// a>b
					return 1;
				} else {
					return 0;
				}
			}
		}
	}
}