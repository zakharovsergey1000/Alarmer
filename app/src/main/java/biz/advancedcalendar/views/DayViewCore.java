package biz.advancedcalendar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.fragments.FragmentViewWeek2;
import biz.advancedcalendar.fragments.FragmentViewWeek2Adapter;
import biz.advancedcalendar.views.accessories.CalendarViewTaskOccurrenceComparator;
import biz.advancedcalendar.views.accessories.TasksLayouter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/** A week view that displays the events in one week */
public class DayViewCore extends View implements OnClickListener {
	private static int WHOLE_DAY_TASKS_AREA_COLOR = 0xFFFFFFFF;
	private static int WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static int HOURS_Y_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static int RIGHT_HEADER_AND_DAYS_SPACER_COLOR = 0xFFAAAAAA;// 0xFFAAAAAA;
	private static int RIGHT_HEADER_AND_DAYS_SPACER_COLOR_IF_WEEK_END = 0xFF606060;
	private static int TOP_BORDER_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static int BOTTOM_BORDER_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private TaskClickedListener mTaskClickedListener;
	private Paint mPaint;
	private TasksLayouter mTasksLayouter;
	private int mTasksSpacerXPx;
	private int mTasksSpacerYPx;
	private int mRightHeaderAndDaysSpacerPx;
	private int mBusinessHoursStartTime, mBusinessHoursEndTime;
	private Rect mWholeDayTasksAreaRect;
	private Rect mWholeDayTasksThisDayTasksSpacerRect;
	private int mWholeDayTasksThisDayTasksSpacerPx;
	private int mMinTaskHeightPx;
	private int mWholeDayTasksHeightPx;
	private int mWholeDayTasksSpacerPx;
	private Rect mTextBounds;
	private long[] mDayBorders;
	// private boolean areTaskOccurrencesCalculated;
	private TimeIntervalViewsPositions mTimeIntervalViewsPositions;
	private List<TextModeViewCoords> mTextModeViewsCoords;
	private List<Integer> mIntegersList;
	private TaskOccurrencesDistribution mTaskOccurrencesDistribution;
	private TaskOccurrencesDistributionState mTaskOccurrencesDistributionState;
	private float mTextSize;
	private int mMode = CommonConstants.CALENDAR_MODE_TIME_INTERVALS;
	private float mLastRegisteredXOnDown;
	private float mLastRegisteredYOnDown;
	private float mLastRegisteredXOnUp;
	private float mLastRegisteredYOnUp;
	private ArrayList<WholeDayWeekTaskView> mWholeDayTaskOccurrencesList;
	private ArrayList<WholeDayWeekTaskView> mTaskOccurrencesTouchingPreBusinessHoursList;
	private ArrayList<WeekTaskView> mTaskOccurrencesTouchingBusinessHoursList;
	private ArrayList<WholeDayWeekTaskView> mTaskOccurrencesTouchingPostBusinessHoursList;
	private boolean mPreparedForOnDraw;
	private ArrayList<TextWeekTaskView> mTextModeViews;
	private boolean mIsLastDayOfWeek;

	public DayViewCore(Context context) {
		this(context, null, 0);
	}

	public DayViewCore(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DayViewCore(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnClickListener(this);
		mTextBounds = new Rect();
		mWholeDayTasksAreaRect = new Rect(0, 0, 0, 0);
		mWholeDayTasksThisDayTasksSpacerRect = new Rect(0, 0, 0, 0);
	}

	public void setDynamicParameters(
			TimeIntervalViewsPositions timeIntervalViewsPositions,
			TaskOccurrencesDistributionState taskOccurrencesDistributionState,
			TaskOccurrencesDistribution taskOccurrencesDistribution, long[] daysBorders,
			int businessHoursStartTime, int businessHoursEndTime, int mode,
			boolean isLastDayOfWeek) {
		mTimeIntervalViewsPositions = timeIntervalViewsPositions;
		mTaskOccurrencesDistributionState = taskOccurrencesDistributionState;
		mTaskOccurrencesDistribution = taskOccurrencesDistribution;
		mDayBorders = daysBorders;
		mBusinessHoursStartTime = businessHoursStartTime;
		mBusinessHoursEndTime = businessHoursEndTime;
		mMode = mode;
		mIsLastDayOfWeek = isLastDayOfWeek;
		mPreparedForOnDraw = false;
		invalidate();
	}

	public void setPermanentParameters(FragmentViewWeek2 mFragmentViewWeek2,
			int wholeDayTasksThisDayTasksSpacerPx, int wholeDayTasksSpacerPx,
			int minTaskHeightPx, int tasksSpacerXPx, int tasksSpacerYPx,
			int hoursSpacerPx, int rightHeaderAndDaysSpacerPx, Paint paint,
			TextPaint textPaint, float textSize, int preBusinessHoursTasksHeightPx,
			int postBusinessHoursTasksHeightPx, int wholeDayTasksHeightPx,
			TasksLayouter tasksLayouter, TaskClickedListener taskClickedListener,
			List<Integer> integersList, DateFormat timeFormatter,
			ArrayList<WholeDayWeekTaskView> wholeDayTaskOccurrencesList,
			ArrayList<WholeDayWeekTaskView> taskOccurrencesTouchingPreBusinessHoursList,
			ArrayList<WeekTaskView> taskOccurrencesTouchingBusinessHoursList,
			ArrayList<WholeDayWeekTaskView> taskOccurrencesTouchingPostBusinessHoursList,
			ArrayList<TextWeekTaskView> textWeekTaskViewList) {
		mWholeDayTasksThisDayTasksSpacerPx = wholeDayTasksThisDayTasksSpacerPx;
		mWholeDayTasksSpacerPx = wholeDayTasksSpacerPx;
		mMinTaskHeightPx = minTaskHeightPx;
		mTasksSpacerXPx = tasksSpacerXPx;
		mTasksSpacerYPx = tasksSpacerYPx;
		mRightHeaderAndDaysSpacerPx = rightHeaderAndDaysSpacerPx;
		mPaint = paint;
		mTextSize = textSize;
		mWholeDayTasksHeightPx = wholeDayTasksHeightPx;
		mTasksLayouter = tasksLayouter;
		mTaskClickedListener = taskClickedListener;
		mIntegersList = integersList;
		mWholeDayTaskOccurrencesList = wholeDayTaskOccurrencesList;
		mTaskOccurrencesTouchingPreBusinessHoursList = taskOccurrencesTouchingPreBusinessHoursList;
		mTaskOccurrencesTouchingBusinessHoursList = taskOccurrencesTouchingBusinessHoursList;
		mTaskOccurrencesTouchingPostBusinessHoursList = taskOccurrencesTouchingPostBusinessHoursList;
		mTextModeViews = textWeekTaskViewList;
	}

	protected void prepareForOnDraw(final int left, final int top, final int right,
			final int bottom) {
		long nanoTime = System.nanoTime();
		if (mMode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
			int childViewLeft = 0;
			int childViewTop = 0;
			int childViewRight = 0;
			int childViewBottom = 0;
			int width = right - left;
			mWholeDayTaskOccurrencesList.clear();
			mTaskOccurrencesTouchingPreBusinessHoursList.clear();
			mTaskOccurrencesTouchingBusinessHoursList.clear();
			mTaskOccurrencesTouchingPostBusinessHoursList.clear();
			// calculate coordinates of all day task views first
			if (mTaskOccurrencesDistribution != null
					&& mTimeIntervalViewsPositions != null) {
				if (mTaskOccurrencesDistributionState.AllDayTasksExist) {
					int occurrencesCountInDay = mTaskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList
							.get(0).size();
					if (occurrencesCountInDay > 0) {
						childViewLeft = left;
						childViewTop = 0;
						childViewRight = childViewLeft - mWholeDayTasksSpacerPx;
						childViewBottom = mWholeDayTasksHeightPx;
						float taskWidth = (right - mRightHeaderAndDaysSpacerPx - left - mWholeDayTasksSpacerPx
								* (occurrencesCountInDay - 1))
								/ (float) occurrencesCountInDay;
						for (int occurrenceInDay = 0; occurrenceInDay < occurrencesCountInDay; occurrenceInDay++) {
							CalendarViewTaskOccurrence calendarViewTaskOccurrence = mTaskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList
									.get(0).get(occurrenceInDay);
							WholeDayWeekTaskView currentWholeDayWeekTaskView = new WholeDayWeekTaskView(
									getContext());
							currentWholeDayWeekTaskView.setTextSize(mTextSize);
							currentWholeDayWeekTaskView
									.setCalendarViewTaskOccurrence(calendarViewTaskOccurrence);
							childViewLeft = childViewRight + mWholeDayTasksSpacerPx;
							childViewRight = left
									+ (int) ((taskWidth + mWholeDayTasksSpacerPx)
											* (occurrenceInDay + 1)
											- mWholeDayTasksSpacerPx + 0.5);
							currentWholeDayWeekTaskView.setCoords(childViewLeft,
									childViewTop, childViewRight, childViewBottom);
							mWholeDayTaskOccurrencesList.add(currentWholeDayWeekTaskView);
						}
					}
				}
				// calculate coordinates of task views next
				// calculate coordinates of PreBusinessHours task views
				// Debug.startMethodTracing("calc");
				if (mTaskOccurrencesDistributionState.PreBusinessHoursTasksExist) {
					int mCalendarViewTaskOccurrencesTakingPreBusinessHoursSize = mTaskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList
							.get(0).size();
					if (mCalendarViewTaskOccurrencesTakingPreBusinessHoursSize > 0) {
						childViewLeft = left;
						childViewTop = mTimeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[0];
						childViewRight = childViewLeft - mWholeDayTasksSpacerPx;
						childViewBottom = mTimeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[1];
						float taskWidth = (right - mRightHeaderAndDaysSpacerPx - left - mWholeDayTasksSpacerPx
								* (mCalendarViewTaskOccurrencesTakingPreBusinessHoursSize - 1))
								/ (float) mCalendarViewTaskOccurrencesTakingPreBusinessHoursSize;
						for (int i1 = 0; i1 < mCalendarViewTaskOccurrencesTakingPreBusinessHoursSize; i1++) {
							CalendarViewTaskOccurrence calendarViewTaskOccurrence = mTaskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList
									.get(0).get(i1);
							WholeDayWeekTaskView currentPreBusinessHoursTaskView = new WholeDayWeekTaskView(
									getContext());
							currentPreBusinessHoursTaskView.setTextSize(mTextSize);
							currentPreBusinessHoursTaskView
									.setCalendarViewTaskOccurrence(calendarViewTaskOccurrence);
							childViewLeft = childViewRight + mWholeDayTasksSpacerPx;
							childViewRight = left
									+ (int) ((taskWidth + mWholeDayTasksSpacerPx)
											* (i1 + 1) - mWholeDayTasksSpacerPx + 0.5);
							currentPreBusinessHoursTaskView.setCoords(childViewLeft,
									childViewTop, childViewRight, childViewBottom);
							mTaskOccurrencesTouchingPreBusinessHoursList
									.add(currentPreBusinessHoursTaskView);
						}
					}
				}
				// Place BusinessHours task views
				List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = mTaskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList
						.get(0);
				if (calendarViewTaskOccurrences.size() > 0) {
					mIntegersList.clear();
					if (mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords != null) {
						mIntegersList
								.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[0]);
						mIntegersList
								.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[1]);
					}
					for (int i1 = 0, count = mTimeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
							.size(); i1 < count; i1 += 2) {
						mIntegersList
								.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
										.get(i1));
						mIntegersList
								.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
										.get(i1 + 1));
					}
					//
					if (mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords != null) {
						mIntegersList
								.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[0]);
						mIntegersList
								.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[1]);
					}
					//
					int[] timeIntervalViewsTopBottomCoords = new int[mIntegersList.size()];
					for (int i = 0; i < timeIntervalViewsTopBottomCoords.length; i++) {
						timeIntervalViewsTopBottomCoords[i] = mIntegersList.get(i);
					}
					long[] timeIntervalDurations = new long[mIntegersList.size() / 2];
					for (int i = 0; i < timeIntervalDurations.length; i++) {
						if (i == 0
								&& mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords != null) {
							timeIntervalDurations[i] = 3600000 - mBusinessHoursStartTime % 3600000;
						} else if (i == timeIntervalDurations.length - 1
								&& mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords != null) {
							timeIntervalDurations[i] = mBusinessHoursEndTime % 3600000;
						} else {
							timeIntervalDurations[i] = 3600000;
						}
					}
					Calendar calendar = Calendar.getInstance();
					int hourViewWidth = right - mRightHeaderAndDaysSpacerPx - left;
					mTasksLayouter.calculateTasksCoords1(mDayBorders[0]
							+ mBusinessHoursStartTime, calendarViewTaskOccurrences,
							timeIntervalDurations, timeIntervalViewsTopBottomCoords,
							hourViewWidth, mMinTaskHeightPx, mTasksSpacerXPx,
							mTasksSpacerYPx);
					for (int j = 0; j < calendarViewTaskOccurrences.size(); j++) {
						CalendarViewTaskOccurrence calendarViewTaskOccurrence = calendarViewTaskOccurrences
								.get(j);
						WeekTaskView currentTaskView = new WeekTaskView(getContext());
						currentTaskView.setTextSize(mTextSize);
						currentTaskView
								.setCalendarViewTaskOccurrence(calendarViewTaskOccurrence);
						int currentTaskViewRealTopOffset = calendarViewTaskOccurrence.RealTop
								- calendarViewTaskOccurrence.Top;
						currentTaskView.setRealTaskTopPx(currentTaskViewRealTopOffset);
						if (calendarViewTaskOccurrence.StartDateTime != null
								&& calendarViewTaskOccurrence.StartDateTime
										.equals(calendarViewTaskOccurrence.EndDateTime)) {
							currentTaskView
									.setRealTaskBottomPx(currentTaskViewRealTopOffset);
						} else {
							Integer calendarViewTaskOccurrenceEndTime = null;
							if (calendarViewTaskOccurrence.EndDateTime != null) {
								calendar.setTimeInMillis(calendarViewTaskOccurrence.EndDateTime);
								calendarViewTaskOccurrenceEndTime = ((calendar
										.get(Calendar.HOUR_OF_DAY) * 60 + calendar
										.get(Calendar.MINUTE)) * 60 + calendar
										.get(Calendar.SECOND)) * 1000;
							}
							if (calendarViewTaskOccurrenceEndTime != null
									&& calendarViewTaskOccurrenceEndTime <= mBusinessHoursStartTime) {
								currentTaskView
										.setRealTaskBottomPx(currentTaskViewRealTopOffset);
							} else {
								Integer calendarViewTaskOccurrenceStartTime = null;
								if (calendarViewTaskOccurrence.StartDateTime != null) {
									calendar.setTimeInMillis(calendarViewTaskOccurrence.StartDateTime);
									calendarViewTaskOccurrenceStartTime = ((calendar
											.get(Calendar.HOUR_OF_DAY) * 60 + calendar
											.get(Calendar.MINUTE)) * 60 + calendar
											.get(Calendar.SECOND)) * 1000;
								}
								if (calendarViewTaskOccurrenceStartTime != null
										&& mBusinessHoursEndTime <= calendarViewTaskOccurrenceStartTime) {
									currentTaskView
											.setRealTaskBottomPx(currentTaskViewRealTopOffset);
								} else {
									currentTaskView
											.setRealTaskBottomPx(calendarViewTaskOccurrence.RealBottom
													- calendarViewTaskOccurrence.Top);
								}
							}
						}
						childViewLeft = calendarViewTaskOccurrence.Left + left;
						childViewTop = calendarViewTaskOccurrence.Top;
						childViewRight = calendarViewTaskOccurrence.Right + left;
						childViewBottom = calendarViewTaskOccurrence.Bottom;
						currentTaskView.setCoords(childViewLeft, childViewTop,
								childViewRight, childViewBottom);
						mTaskOccurrencesTouchingBusinessHoursList.add(currentTaskView);
					}
				}
				// Place PostBusinessHours task views
				if (mTaskOccurrencesDistributionState.PostBusinessHoursTasksExist) {
					int mCalendarViewTaskOccurrencesTakingPostBusinessHoursSize = mTaskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList
							.get(0).size();
					if (mCalendarViewTaskOccurrencesTakingPostBusinessHoursSize > 0) {
						childViewLeft = left;
						childViewTop = mTimeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[0];
						childViewRight = childViewLeft - mWholeDayTasksSpacerPx;
						childViewBottom = mTimeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[1];
						float taskWidth = (right - mRightHeaderAndDaysSpacerPx - left - mWholeDayTasksSpacerPx
								* (mCalendarViewTaskOccurrencesTakingPostBusinessHoursSize - 1))
								/ (float) mCalendarViewTaskOccurrencesTakingPostBusinessHoursSize;
						for (int i1 = 0; i1 < mCalendarViewTaskOccurrencesTakingPostBusinessHoursSize; i1++) {
							CalendarViewTaskOccurrence calendarViewTaskOccurrence = mTaskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList
									.get(0).get(i1);
							WholeDayWeekTaskView currentPostBusinessHoursTaskView = new WholeDayWeekTaskView(
									getContext());
							currentPostBusinessHoursTaskView.setTextSize(mTextSize);
							currentPostBusinessHoursTaskView
									.setCalendarViewTaskOccurrence(calendarViewTaskOccurrence);
							childViewLeft = childViewRight + mWholeDayTasksSpacerPx;
							childViewRight = left
									+ (int) ((taskWidth + mWholeDayTasksSpacerPx)
											* (i1 + 1) - mWholeDayTasksSpacerPx + 0.5);
							currentPostBusinessHoursTaskView.setCoords(childViewLeft,
									childViewTop, childViewRight, childViewBottom);
							mTaskOccurrencesTouchingPostBusinessHoursList
									.add(currentPostBusinessHoursTaskView);
						}
					}
				}
			}
			// Debug.stopMethodTracing();
			//
			mWholeDayTasksAreaRect.right = width;
			mWholeDayTasksAreaRect.bottom = mTaskOccurrencesDistributionState != null
					&& mTaskOccurrencesDistributionState.AllDayTasksExist ? mWholeDayTasksHeightPx
					: 0;
			//
			mWholeDayTasksThisDayTasksSpacerRect.right = width;
			mWholeDayTasksThisDayTasksSpacerRect.top = mWholeDayTasksAreaRect.bottom;
			mWholeDayTasksThisDayTasksSpacerRect.bottom = mTaskOccurrencesDistributionState != null
					&& mTaskOccurrencesDistributionState.AllDayTasksExist ? mWholeDayTasksAreaRect.bottom
					+ mWholeDayTasksThisDayTasksSpacerPx
					: mWholeDayTasksAreaRect.bottom;
			//
		}
		// text mode
		else {
			mTextModeViews.clear();
			if (mTaskOccurrencesDistribution != null) {
				mTextModeViewsCoords = calculateTextModeViewsCoords(left, top, right,
						bottom);
				int childViewLeft = 0;
				int childViewTop = 0;
				int childViewRight = right - left;
				int childViewBottom = -mWholeDayTasksHeightPx - mWholeDayTasksSpacerPx;
				//
				int occurrencesCountInDay = mTextModeViewsCoords.size();
				if (occurrencesCountInDay > 0) {
					childViewLeft = left;
					childViewRight = right - mRightHeaderAndDaysSpacerPx;
					for (int occurrenceInDay = 0; occurrenceInDay < occurrencesCountInDay; occurrenceInDay++) {
						childViewTop = mTextModeViewsCoords.get(occurrenceInDay).top;
						childViewBottom = mTextModeViewsCoords.get(occurrenceInDay).bottom;
						CalendarViewTaskOccurrence calendarViewTaskOccurrence = mTextModeViewsCoords
								.get(occurrenceInDay).CalendarViewTaskOccurrence;
						TextWeekTaskView currentTextWeekTaskView = new TextWeekTaskView(
								getContext());
						currentTextWeekTaskView.setTextSize(mTextSize);
						currentTextWeekTaskView
								.setCalendarViewTaskOccurrence(calendarViewTaskOccurrence);
						currentTextWeekTaskView.setCoords(childViewLeft, childViewTop,
								childViewRight, childViewBottom);
						mTextModeViews.add(currentTextWeekTaskView);
					}
				}
			}
		}
		long nanoTime2 = System.nanoTime();
		long delta = (nanoTime2 - nanoTime) / 1000000;
		if (FragmentViewWeek2.DEBUG5) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug5, "prepareForOnDraw."
					+ "\t time delta: " + delta);
		}
	}

	private List<TextModeViewCoords> calculateTextModeViewsCoords(int left, int top,
			int right, int bottom) {
		List<TextModeViewCoords> textModeViewsCoords = new ArrayList<TextModeViewCoords>();
		if (mTaskOccurrencesDistribution != null) {
			CalendarViewTaskOccurrenceComparator calendarViewTaskOccurrenceComparator = new CalendarViewTaskOccurrenceComparator(
					mDayBorders[0], mDayBorders[1]);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesInDayTotal = new ArrayList<CalendarViewTaskOccurrence>();
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesInDayCurrent = mTaskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList
					.get(0);
			Collections.sort(calendarViewTaskOccurrencesInDayCurrent,
					calendarViewTaskOccurrenceComparator);
			int count = calendarViewTaskOccurrencesInDayCurrent.size();
			for (int i = 0; i < count; i++) {
				calendarViewTaskOccurrencesInDayTotal
						.add(calendarViewTaskOccurrencesInDayCurrent.get(i));
			}
			//
			calendarViewTaskOccurrencesInDayCurrent = mTaskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList
					.get(0);
			Collections.sort(calendarViewTaskOccurrencesInDayCurrent,
					calendarViewTaskOccurrenceComparator);
			count = calendarViewTaskOccurrencesInDayCurrent.size();
			for (int i = 0; i < count; i++) {
				calendarViewTaskOccurrencesInDayTotal
						.add(calendarViewTaskOccurrencesInDayCurrent.get(i));
			}
			//
			calendarViewTaskOccurrencesInDayCurrent = mTaskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList
					.get(0);
			Collections.sort(calendarViewTaskOccurrencesInDayCurrent,
					calendarViewTaskOccurrenceComparator);
			count = calendarViewTaskOccurrencesInDayCurrent.size();
			for (int i = 0; i < count; i++) {
				calendarViewTaskOccurrencesInDayTotal
						.add(calendarViewTaskOccurrencesInDayCurrent.get(i));
			}
			//
			calendarViewTaskOccurrencesInDayCurrent = mTaskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList
					.get(0);
			Collections.sort(calendarViewTaskOccurrencesInDayCurrent,
					calendarViewTaskOccurrenceComparator);
			count = calendarViewTaskOccurrencesInDayCurrent.size();
			for (int i = 0; i < count; i++) {
				calendarViewTaskOccurrencesInDayTotal
						.add(calendarViewTaskOccurrencesInDayCurrent.get(i));
			}
			//
			count = calendarViewTaskOccurrencesInDayTotal.size();
			if (count > 0) {
				int childViewTop = 0;
				int childViewBottom = -mWholeDayTasksSpacerPx;
				for (int i = 0; i < count; i++) {
					childViewTop = childViewBottom + mWholeDayTasksSpacerPx;
					childViewBottom = childViewTop + mWholeDayTasksHeightPx;
					TextModeViewCoords TextModeViewCoords = new TextModeViewCoords(
							calendarViewTaskOccurrencesInDayTotal.get(i), childViewTop,
							childViewBottom);
					textModeViewsCoords.add(TextModeViewCoords);
				}
				if (textModeViewsCoords.get(count - 1).bottom > bottom - top) {
					float eventHeight = (bottom - top - mWholeDayTasksSpacerPx
							* (count - 1))
							/ (float) count;
					// Prepare childViewRight for the cycle below
					childViewBottom = top - mWholeDayTasksSpacerPx;
					for (int i = 0; i < count; i++) {
						childViewTop = childViewBottom + mWholeDayTasksSpacerPx;
						childViewBottom = (int) ((eventHeight + mWholeDayTasksSpacerPx)
								* (i + 1) - mWholeDayTasksSpacerPx);
						if (childViewTop > bottom) {
							childViewTop = bottom;
						}
						if (childViewBottom > bottom) {
							childViewBottom = bottom;
						}
						textModeViewsCoords.get(i).top = childViewTop;
						textModeViewsCoords.get(i).bottom = childViewBottom;
					}
				}
			}
		}
		return textModeViewsCoords;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		long nanoTime = System.nanoTime();
		super.onDraw(canvas);
		int left = 0, top = 0, right = getWidth(), bottom = getHeight();
		if (!mPreparedForOnDraw) {
			prepareForOnDraw(left, top, right, bottom);
			mPreparedForOnDraw = true;
		}
		// Draw vertical spacer before right header
		if (mIsLastDayOfWeek) {
			mPaint.setColor(DayViewCore.RIGHT_HEADER_AND_DAYS_SPACER_COLOR_IF_WEEK_END);
		} else {
			mPaint.setColor(DayViewCore.RIGHT_HEADER_AND_DAYS_SPACER_COLOR);
		}
		canvas.drawRect(right - mRightHeaderAndDaysSpacerPx, 0, right, bottom, mPaint);
		if (mMode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
			// Fill multiday tasks area with color
			mPaint.setColor(DayViewCore.WHOLE_DAY_TASKS_AREA_COLOR);
			canvas.drawRect(mWholeDayTasksAreaRect, mPaint);
			// Draw horizontal spacer after multiDay tasks area
			mPaint.setColor(DayViewCore.WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_COLOR);
			canvas.drawRect(mWholeDayTasksThisDayTasksSpacerRect, mPaint);
			// Draw vertical spacer before right header
			if (mIsLastDayOfWeek) {
				mPaint.setColor(DayViewCore.RIGHT_HEADER_AND_DAYS_SPACER_COLOR_IF_WEEK_END);
			} else {
				mPaint.setColor(DayViewCore.RIGHT_HEADER_AND_DAYS_SPACER_COLOR);
			}
			right = getWidth();
			canvas.drawRect(right - mRightHeaderAndDaysSpacerPx, 0, right, bottom, mPaint);
			// Draw topBorderSpacer
			top = 0;
			bottom = 0;
			mPaint.setColor(DayViewCore.TOP_BORDER_SPACER_COLOR);
			canvas.drawRect(left, top, right, bottom, mPaint);
			if (mTimeIntervalViewsPositions != null) {
				// Draw horizontal lines between hours
				mPaint.setColor(DayViewCore.HOURS_Y_SPACER_COLOR);
				mIntegersList.clear();
				if (mTimeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords != null) {
					mIntegersList
							.add(mTimeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[0]);
					mIntegersList
							.add(mTimeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[1]);
				}
				//
				if (mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords != null) {
					//
					mIntegersList
							.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[0]);
					mIntegersList
							.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[1]);
				}
				for (int i1 = 0, count = mTimeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
						.size(); i1 < count; i1 += 2) {
					//
					mIntegersList
							.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
									.get(i1));
					mIntegersList
							.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
									.get(i1 + 1));
				}
				//
				if (mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords != null) {
					//
					mIntegersList
							.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[0]);
					mIntegersList
							.add(mTimeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[1]);
				}
				//
				if (mTimeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords != null) {
					//
					mIntegersList
							.add(mTimeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[0]);
					mIntegersList
							.add(mTimeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[1]);
				}
				for (int i = 2, count = mIntegersList.size(); i < count; i += 2) {
					top = mIntegersList.get(i - 1);
					// bottom = top + mHoursSpacerPx;
					bottom = mIntegersList.get(i);
					// Draw horizontal line
					canvas.drawRect(left, top, right, bottom, mPaint);
				}
			}
			// Draw bottomBorderSpacer
			bottom = getHeight();
			top = bottom;
			mPaint.setColor(DayViewCore.BOTTOM_BORDER_SPACER_COLOR);
			canvas.drawRect(left, top, right, bottom, mPaint);
		}
		// draw tasks
		if (mTaskOccurrencesDistribution != null) {
			if (mMode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
				// Place all day task views first
				for (int j = 0; j < mWholeDayTaskOccurrencesList.size(); j++) {
					WholeDayWeekTaskView currentTaskView = mWholeDayTaskOccurrencesList
							.get(j);
					currentTaskView.draw(canvas);
				}
				// Place event views next
				// Place PreBusinessHours task views
				for (int j = 0; j < mTaskOccurrencesTouchingPreBusinessHoursList.size(); j++) {
					WholeDayWeekTaskView currentTaskView = mTaskOccurrencesTouchingPreBusinessHoursList
							.get(j);
					currentTaskView.draw(canvas);
				}
				// Place BusinessHours task views
				for (int j = 0; j < mTaskOccurrencesTouchingBusinessHoursList.size(); j++) {
					WeekTaskView currentTaskView = mTaskOccurrencesTouchingBusinessHoursList
							.get(j);
					currentTaskView.draw(canvas);
				}
				// Place PostBusinessHours task views
				for (int j = 0; j < mTaskOccurrencesTouchingPostBusinessHoursList.size(); j++) {
					WholeDayWeekTaskView currentTaskView = mTaskOccurrencesTouchingPostBusinessHoursList
							.get(j);
					currentTaskView.draw(canvas);
				}
			}
			// text mode
			else {
				for (int j = 0; j < mTextModeViews.size(); j++) {
					TextWeekTaskView currentTaskView = mTextModeViews.get(j);
					currentTaskView.draw(canvas);
				}
			}
			long nanoTime2 = System.nanoTime();
			long delta = (nanoTime2 - nanoTime) / 1000000;
			if (FragmentViewWeek2.DEBUG5) {
				Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug5, "onDraw."
						+ "\t time delta: " + delta);
			}
		}
	}

	public void drawTextCentred(Canvas canvas, Paint paint, String text, float cx,
			float cy) {
		paint.getTextBounds(text, 0, text.length(), mTextBounds);
		canvas.drawText(text, cx - mTextBounds.exactCenterX(),
				cy - mTextBounds.exactCenterY(), paint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastRegisteredXOnDown = event.getX();
			mLastRegisteredYOnDown = event.getY();
			boolean result = super.onTouchEvent(event);
			return result;
		case MotionEvent.ACTION_UP:
			mLastRegisteredXOnUp = event.getX();
			mLastRegisteredYOnUp = event.getY();
			result = super.onTouchEvent(event);
			return result;
		default:
			result = super.onTouchEvent(event);
			return result;
		}
	}

	@Override
	public void onClick(View v) {
		if (mTaskClickedListener != null) {
			if (mMode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
				for (WholeDayWeekTaskView view : mWholeDayTaskOccurrencesList) {
					if (view.getLeft() <= mLastRegisteredXOnDown
							&& mLastRegisteredXOnDown <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnDown
							&& mLastRegisteredYOnDown <= view.getBottom()
							&& view.getLeft() <= mLastRegisteredXOnUp
							&& mLastRegisteredXOnUp <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnUp
							&& mLastRegisteredYOnUp <= view.getBottom()) {
						mTaskClickedListener.onTaskClicked(view
								.getCalendarViewTaskOccurrence().Task.getId());
						return;
					}
				}
				for (WholeDayWeekTaskView view : mTaskOccurrencesTouchingPreBusinessHoursList) {
					if (view.getLeft() <= mLastRegisteredXOnDown
							&& mLastRegisteredXOnDown <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnDown
							&& mLastRegisteredYOnDown <= view.getBottom()
							&& view.getLeft() <= mLastRegisteredXOnUp
							&& mLastRegisteredXOnUp <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnUp
							&& mLastRegisteredYOnUp <= view.getBottom()) {
						mTaskClickedListener.onTaskClicked(view
								.getCalendarViewTaskOccurrence().Task.getId());
						return;
					}
				}
				for (WeekTaskView view : mTaskOccurrencesTouchingBusinessHoursList) {
					if (view.getLeft() <= mLastRegisteredXOnDown
							&& mLastRegisteredXOnDown <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnDown
							&& mLastRegisteredYOnDown <= view.getBottom()
							&& view.getLeft() <= mLastRegisteredXOnUp
							&& mLastRegisteredXOnUp <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnUp
							&& mLastRegisteredYOnUp <= view.getBottom()) {
						mTaskClickedListener.onTaskClicked(view
								.getCalendarViewTaskOccurrence().Task.getId());
						return;
					}
				}
				for (WholeDayWeekTaskView view : mTaskOccurrencesTouchingPostBusinessHoursList) {
					if (view.getLeft() <= mLastRegisteredXOnDown
							&& mLastRegisteredXOnDown <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnDown
							&& mLastRegisteredYOnDown <= view.getBottom()
							&& view.getLeft() <= mLastRegisteredXOnUp
							&& mLastRegisteredXOnUp <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnUp
							&& mLastRegisteredYOnUp <= view.getBottom()) {
						mTaskClickedListener.onTaskClicked(view
								.getCalendarViewTaskOccurrence().Task.getId());
						return;
					}
				}
			}
			// text mode
			else {
				for (TextWeekTaskView view : mTextModeViews) {
					if (view.getLeft() <= mLastRegisteredXOnDown
							&& mLastRegisteredXOnDown <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnDown
							&& mLastRegisteredYOnDown <= view.getBottom()
							&& view.getLeft() <= mLastRegisteredXOnUp
							&& mLastRegisteredXOnUp <= view.getRight()
							&& view.getTop() <= mLastRegisteredYOnUp
							&& mLastRegisteredYOnUp <= view.getBottom()) {
						mTaskClickedListener.onTaskClicked(view
								.getCalendarViewTaskOccurrence().Task.getId());
						return;
					}
				}
			}
		}
	}

	public long getDayStartDateTime() {
		return mDayBorders[0];
	}
}
