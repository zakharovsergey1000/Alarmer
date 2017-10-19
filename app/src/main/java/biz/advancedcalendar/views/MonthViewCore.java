package biz.advancedcalendar.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class MonthViewCore extends ViewGroup {
	// public static interface DayClickedListener {
	// public void onDayClicked(long day);
	// }
	private static final int DAYS_Y_SPACER_DIP = 1;
	private static final int DAYS_X_SPACER_DIP = 1;
	private static final int LEFT_BORDER_DIP = 1;
	private static final int TOP_BORDER_DIP = 1;
	private static final int RIGHT_BORDER_DIP = 1;
	private static final int BOTTOM_BORDER_DIP = 1;
	private static final int DAYS_Y_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA; //
																// 0xFFEEEEEE;//0xFFFF0000
	private static final int DAYS_X_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;//
																// 0xFFEEEEEE;//0xFF00FF00
	// private static final int DAY_NUMBER_TEXT_COLOR_PALE = 0xFFBBBBBB;
	private static final float DAY_NUMBER_TEXT_SIZE_SP = 12.0f;
	private static final int DAY_WIDTH_DEFAULT_DIP = 100;
	private static final int DAY_WIDTH_DESIRABLE_MINIMUM_DIP = 70;
	private static final int DAY_HEIGHT_DEFAULT_DIP = 100;
	private static final int DAY_HEIGHT_DESIRABLE_MINIMUM_DIP = 70;
	// private static final float TASK_TEXT_SIZE_SP = 12.0f;
	// private static final int TASKS_SPACER_X_DIP = 1;
	// private static final int TASKS_SPACER_Y_DIP = 1;
	// private static final int MIN_TASK_HEIGHT_DIP = 5;// 25;
	// private static final int WHOLE_DAY_TASKS_HEIGHT_DIP = 5;
	// private DayClickedListener mDayClickedListener;
	// private final ArrayList<ArrayList<View>> mPreallocatedHourViewsInDays;
	private final List<List<CalendarViewTaskOccurrence>> mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList;
	private final List<List<CalendarViewTaskOccurrence>> mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList;
	// private List<CalendarViewTaskOccurrence> mPreallocatedCalendarViewTaskOccurrences;
	private final ArrayList<Integer> mCachedDayHeaderCoords;
	// private final HashSet<View> mCacheOfHourViews;
	private final int mDaysYSpacerPx;
	private final int mDaysXSpacerPx;
	private final int mDayWidthPx;
	private final int mDayHeightPx;
	private final int mLeftBorderSpacerPx;
	private final int topBorderSpacerPx;
	private final int mRightBorderSpacerPx;
	private final int mBottomBorderSpacerPx;
	private final float mDayNumberTextSizePx;
	// private final float mDayNumberTextHeightPx;
	// private final int mTaskHeightPx;
	// private final float mTaskTextSizePx;
	// private final float mTaskTextHeightPx;
	// private final int mTasksSpacerXPx;
	// private final int mTasksSpacerYPx;
	private final float mDensity;
	private final ArrayList<ArrayList<MonthDayView>> mWeeks;
	// private final LayoutParams mParams;
	private Paint mPaint;
	private Paint mTextPaint;
	// private List<Task> mTasks;
	private int mSuggestedViewWidthOnLastRequestOfDayHeaderCoords;
	private Calendar mMonthStartDateTime;
	private Calendar mMonthEndTime;
	// private Rect mWholeDayTasksAreaRect;
	// private Rect mWholeDayTasksThisDayTasksSpacerRect;
	// private final LinkedList<WeekViewCoreTaskWithCoords> mTaskWithCoordsCacheList;
	// private final ArrayList<WeekViewCoreTaskWithCoords> mTaskWithCoordsList;
	// private final TasksLayouter mLayouter;
	// private final ArrayList<Integer> mDayViewLeftRightCoordList;
	// private final LinkedList<View> mTaskViewCacheList;
	// private ArrayList<View> mTaskViewList;
	private Calendar mMonthSquareStartDateTime;
	// private final Rect bounds;
	// private final int wholeDayTasksHeightPx;
	// private Context mContext;
	// private SimpleDateFormat mDateFormat = new
	// SimpleDateFormat("MMMMM, yyyy", Locale.getDefault());
	// private final ArrayList<WeekViewCoreTask> mPreallocatedTasksGoingIntoTimeInterval =
	// new ArrayList<WeekViewCoreTask>();
	// private final ArrayList<CalendarViewTaskOccurrence>
	// mPreallocatedTasksIntersectingTimeInterval = new
	// ArrayList<CalendarViewTaskOccurrence>();
	// private List<CalendarViewTaskOccurrence> mCalendarViewTasks;
	private List<Task> mTasks;
	// private int mBottomTaskDrawingSpacer;
	// private int mRightTaskDrawingSpacer;
	private final HashSet<CalendarViewTaskOccurrence> mCacheOfCalendarViewTaskOccurrences;
	private HashSet<List<CalendarViewTaskOccurrence>> mCacheOfCalendarViewTaskOccurrencesLists;
	private long[] mDaysBorders;
	private boolean areTaskOccurrencesCalculated;

	// private SimpleDateFormat dateFormatWeekHeader;
	// private String[] mWeekDayHeaderArray;
	// private final int minTaskHeightPx;
	/** Constructor
	 *
	 * @param context */
	public MonthViewCore(Context context) {
		this(context, null, 0);
	}

	/** Constructor
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle */
	public MonthViewCore(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/** Constructor
	 *
	 * @param context
	 *            The context
	 * @param attrs
	 *            Attributes
	 * @param defStyle
	 *            defStyle */
	public MonthViewCore(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// mContext = context;
		mDensity = getResources().getDisplayMetrics().density;
		// bounds = new Rect();
		mDayNumberTextSizePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, MonthViewCore.DAY_NUMBER_TEXT_SIZE_SP,
				getResources().getDisplayMetrics());
		// mBottomTaskDrawingSpacer = (int) (mDayNumberTextSizePx + 1);
		// mTaskTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
		// MonthViewCore.TASK_TEXT_SIZE_SP, getResources().getDisplayMetrics());
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		mTextPaint.setTextSize(mDayNumberTextSizePx);
		mDaysYSpacerPx = Math.max(1,
				Math.round(mDensity * MonthViewCore.DAYS_Y_SPACER_DIP));
		mDaysXSpacerPx = Math.max(1,
				Math.round(mDensity * MonthViewCore.DAYS_X_SPACER_DIP));
		mDayWidthPx = (int) (mDensity * MonthViewCore.DAY_WIDTH_DEFAULT_DIP);
		mDayHeightPx = (int) (mDensity * MonthViewCore.DAY_HEIGHT_DEFAULT_DIP);
		//
		mLeftBorderSpacerPx = Math.max(1,
				Math.round(mDensity * MonthViewCore.LEFT_BORDER_DIP));
		topBorderSpacerPx = Math.max(1,
				Math.round(mDensity * MonthViewCore.TOP_BORDER_DIP));
		mRightBorderSpacerPx = Math.max(1,
				Math.round(mDensity * MonthViewCore.RIGHT_BORDER_DIP));
		mBottomBorderSpacerPx = Math.max(1,
				Math.round(mDensity * MonthViewCore.BOTTOM_BORDER_DIP));
		//
		// wholeDayTasksHeightPx = (int) (mDensity *
		// MonthViewCore.WHOLE_DAY_TASKS_HEIGHT_DIP);
		// minTaskHeightPx = Math.max(1,
		// Math.round(mDensity * MonthViewCore.MIN_TASK_HEIGHT_DIP));
		mWeeks = new ArrayList<ArrayList<MonthDayView>>();
		// DateTime currentDayStartDateTime = new DateTime(mMonthSquareStartDateTime)
		// .minusMonths(1);
		// int count = Days.daysBetween(currentDayStartDateTime,
		// currentDayStartDateTime.plusMonths(3)).getDays();
		for (int i = 0; i < 6; i++) {
			ArrayList<MonthDayView> week = new ArrayList<MonthDayView>();
			mWeeks.add(week);
			for (int j = 0; j < 7; j++) {
				MonthDayView dayView = new MonthDayView(context);
				dayView.setDayNumberTextSize(mDayNumberTextSizePx);
				addView(dayView);
				week.add(dayView);
			}
		}
		// MonthDayView b = mWeeks.get(0).get(0);
		// mTaskHeightPx = (int) (mTaskTextHeightPx + b.getPaddingTop() + b
		// .getPaddingBottom());
		// mParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// day = mDateFormat.format(new Date(mMonthSquareStartTime));
		// mTaskWithCoordsCacheList = new LinkedList<WeekViewCoreTaskWithCoords>();
		// mTaskWithCoordsList = new ArrayList<WeekViewCoreTaskWithCoords>();
		// mLayouter = new VerticalTasksLayouter();
		// mDayViewLeftRightCoordList = new ArrayList<Integer>();
		// mTaskViewCacheList = new LinkedList<View>();
		// mTaskViewList = new ArrayList<View>();
		// mTasksSpacerXPx = Math.max(1,
		// Math.round(mDensity * MonthViewCore.TASKS_SPACER_X_DIP));
		// mTasksSpacerYPx = Math.max(1,
		// Math.round(mDensity * MonthViewCore.TASKS_SPACER_Y_DIP));
		mMonthEndTime = null;
		// dateFormatWeekHeader = new SimpleDateFormat("EEE", Locale.getDefault());
		mPaint = new Paint();
		mPaint.setColor(0xFF808080);
		mCachedDayHeaderCoords = new ArrayList<Integer>();
		mCacheOfCalendarViewTaskOccurrences = new HashSet<CalendarViewTaskOccurrence>();
		mCacheOfCalendarViewTaskOccurrencesLists = new HashSet<List<CalendarViewTaskOccurrence>>();
		// mCacheOfHourViews = new HashSet<View>();
		// mPreallocatedHourViewsInDays = new ArrayList<ArrayList<View>>();
		// mWholeDayTasksAreaRect = new Rect(0, 0, 0, 0);
		// mWholeDayTasksThisDayTasksSpacerRect = new Rect(0, 0, 0, 0);
		mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		// mPreallocatedCalendarViewTaskOccurrences = new
		// ArrayList<CalendarViewTaskOccurrence>();
		mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		setWillNotDraw(false);
	}

	public void setDaysSelectedListener(DaysSelectedListener daysSelectedListener) {
		for (ArrayList<MonthDayView> week : mWeeks) {
			for (MonthDayView monthDayView : week) {
				monthDayView.setDaysSelectedListener(daysSelectedListener);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		if (!areTaskOccurrencesCalculated) {
			calculateAndDistributeTaskOccurrences();
			areTaskOccurrencesCalculated = true;
		}
		int width = mDayWidthPx * 7 + mDaysXSpacerPx * 6 + mLeftBorderSpacerPx
				+ mRightBorderSpacerPx;
		//
		int height = mDayHeightPx * 6 + mDaysYSpacerPx * 5 + topBorderSpacerPx
				+ mBottomBorderSpacerPx;
		switch (specModeWidth) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (width > specSizeWidth) {
				width = specSizeWidth;
				int minDesirableWidth = (int) (mDensity
						* MonthViewCore.DAY_WIDTH_DESIRABLE_MINIMUM_DIP + 0.5)
						* 7
						+ mDaysXSpacerPx
						* 6
						+ mLeftBorderSpacerPx
						+ mRightBorderSpacerPx;
				if (width < minDesirableWidth) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						width |= View.MEASURED_STATE_TOO_SMALL;
					}
				}
			}
			break;
		case MeasureSpec.EXACTLY:
			width = specSizeWidth;
			int minDesirableWidth = (int) (mDensity
					* MonthViewCore.DAY_WIDTH_DESIRABLE_MINIMUM_DIP + 0.5)
					* 7 + mDaysXSpacerPx * 6 + mLeftBorderSpacerPx + mRightBorderSpacerPx;
			if (width < minDesirableWidth) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					width |= View.MEASURED_STATE_TOO_SMALL;
				}
			}
			break;
		}
		switch (specModeHeight) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (height > specSizeHeight) {
				height = specSizeHeight;
				int minDesirableHeight = (int) (mDensity
						* MonthViewCore.DAY_HEIGHT_DESIRABLE_MINIMUM_DIP + 0.5)
						* 6
						+ mDaysYSpacerPx
						* 5
						+ topBorderSpacerPx
						+ mBottomBorderSpacerPx;
				if (height < minDesirableHeight) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
						height |= View.MEASURED_STATE_TOO_SMALL;
					}
				}
			}
			break;
		case MeasureSpec.EXACTLY:
			height = specSizeHeight;
			int minDesirableHeight = (int) (mDensity
					* MonthViewCore.DAY_HEIGHT_DESIRABLE_MINIMUM_DIP + 0.5)
					* 6 + mDaysYSpacerPx * 5 + topBorderSpacerPx + mBottomBorderSpacerPx;
			if (height < minDesirableHeight) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					height |= View.MEASURED_STATE_TOO_SMALL;
				}
			}
			break;
		}
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top,
			final int right, final int bottom) {
		// removeAllViewsInLayout();
		int childViewLeft;
		int childViewTop;
		int childViewRight;
		int childViewBottom;
		float dayHeight = (bottom - top - mDaysYSpacerPx * 5 - topBorderSpacerPx - mBottomBorderSpacerPx) / 6.0f;
		// Place day views first
		List<Integer> dayHeaderCoords = getDayHeadersCoords(right - left);
		// Set calendar instance to the beginning of the month rectangle
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mMonthStartDateTime.getTimeInMillis());
		calendar.add(Calendar.MONTH, 1);
		long monthEndTime = calendar.getTimeInMillis();
		calendar.setTimeInMillis(mMonthSquareStartDateTime.getTimeInMillis());
		// Prepare calendar for the cycle below
		calendar.add(Calendar.DAY_OF_YEAR, -1);
		childViewBottom = topBorderSpacerPx - mDaysYSpacerPx;
		for (int i = 0; i < 6; i++) {
			ArrayList<MonthDayView> week = mWeeks.get(i);
			childViewTop = childViewBottom + mDaysYSpacerPx;
			childViewBottom = (int) (topBorderSpacerPx + (dayHeight + mDaysYSpacerPx) * i
					+ dayHeight + 0.5);
			for (int j = 0; j < week.size(); j++) {
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				long dayStartDateTime = calendar.getTimeInMillis();
				calendar.add(Calendar.DAY_OF_YEAR, 1);
				// long dayEndDateTime = calendar.getTimeInMillis();
				calendar.add(Calendar.DAY_OF_YEAR, -1);
				MonthDayView dayView = week.get(j);
				// dayView.setTextSize(mDayNumberTextSizePx);
				// dayView.setBottomtTaskDrawingSpacer(mBottomtTaskDrawingSpacer);
				//
				dayView.setDayStartDateTime(dayStartDateTime);
				//
				// dayView.setWholeDayCalendarViewTaskOccurrences(Helper.selectTaskOccurrencesTakingWholeDateTimeInterval(
				// getContext(), dayStartDateTime, dayEndDateTime,
				// dayView.getWholeDayPreallocatedCalendarViewTaskOccurrences(),
				// null, mTasks));
				// dayView.setNotWholeDayCalendarViewTaskOccurrences(Helper
				// .selectTaskOccurrencesGoingInsideDateTimeIntervalOrIntersectingTimeIntervalOnOneBorder(
				// getContext(),
				// dayStartDateTime,
				// dayEndDateTime,
				// dayView.getNotWholeDayPreallocatedCalendarViewTaskOccurrences(),
				// mTasks));
				//
				if (mMonthStartDateTime.getTimeInMillis() > dayStartDateTime
						|| dayStartDateTime >= monthEndTime) {
					dayView.setPale(true);
				} else {
					dayView.setPale(false);
				}
				childViewLeft = dayHeaderCoords.get(j * 2);
				childViewRight = dayHeaderCoords.get(j * 2 + 1);
				// addViewInLayout(dayView, -1, mParams, true);
				dayView.measure(MeasureSpec.EXACTLY | childViewRight - childViewLeft,
						MeasureSpec.EXACTLY | childViewBottom - childViewTop);
				dayView.layout(childViewLeft, childViewTop, childViewRight,
						childViewBottom);
			}
		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mWeeks.size() == 0) {
			return;
		}
		int left, top, right, bottom;
		left = 0;
		right = getWidth();
		// Draw horizontal lines
		mPaint.setColor(MonthViewCore.DAYS_Y_SPACER_COLOR);
		for (int i = 1; i < mWeeks.size(); i++) {
			top = mWeeks.get(i - 1).get(0).getBottom();
			bottom = top + mDaysYSpacerPx;
			canvas.drawRect(left, top, right, bottom, mPaint);
		}
		top = 0;
		bottom = top + topBorderSpacerPx;
		canvas.drawRect(left, top, right, bottom, mPaint);
		bottom = getHeight();
		top = bottom - mBottomBorderSpacerPx;
		canvas.drawRect(left, top, right, bottom, mPaint);
		// Draw vertical lines
		mPaint.setColor(MonthViewCore.DAYS_X_SPACER_COLOR);
		top = 0;
		bottom = getHeight();
		List<Integer> dayHeaderCoords = getDayHeadersCoords(right - left);
		for (int j = 1; j < dayHeaderCoords.size() - 1; j += 2) {
			left = dayHeaderCoords.get(j);
			right = dayHeaderCoords.get(j + 1);
			canvas.drawRect(left, top, right, bottom, mPaint);
		}
		left = 0;
		right = dayHeaderCoords.get(0);
		canvas.drawRect(left, top, right, bottom, mPaint);
		left = dayHeaderCoords.get(dayHeaderCoords.size() - 1);
		right = getWidth();
		canvas.drawRect(left, top, right, bottom, mPaint);
		// draw today day's frame
		// Integer todayDayindex = getTodayDayIndex();
		// if (todayDayindex != null) {
		// int x = todayDayindex % 7;
		// int y = todayDayindex / 7;
		// mPaint.setColor(getResources().getColor(
		// R.color.week_view_days_header_button_today_background_color));
		// // Draw horizontal lines
		// for (int i = y + 1; i <= y + 2; i++) {
		// top = mWeeks.get(i - 1).get(0).getBottom();
		// bottom = top + mDaysYSpacerPx;
		// canvas.drawRect(left, top, right, bottom, mPaint);
		// }
		// if (y == 0) {
		// top = 0;
		// bottom = top + topBorderSpacerPx;
		// canvas.drawRect(left, top, right, bottom, mPaint);
		// } else if (y == 5) {
		// bottom = getHeight();
		// top = bottom - mBottomBorderSpacerPx;
		// canvas.drawRect(left, top, right, bottom, mPaint);
		// }
		// // Draw vertical lines
		// top = 0;
		// bottom = getHeight();
		// for (int j = x + 1; j <= x + 2; j += 2) {
		// left = dayHeaderCoords.get(j);
		// right = dayHeaderCoords.get(j + 1);
		// canvas.drawRect(left, top, right, bottom, mPaint);
		// }
		// if (x == 0) {
		// left = 0;
		// right = dayHeaderCoords.get(0);
		// canvas.drawRect(left, top, right, bottom, mPaint);
		// } else if (x == 6) {
		// left = dayHeaderCoords.get(dayHeaderCoords.size() - 1);
		// right = getWidth();
		// canvas.drawRect(left, top, right, bottom, mPaint);
		// }
		// }
	}

	// private Integer getTodayDayIndex() {
	// long now = DateTime.now().getMillis();
	// DateTime mMonthSquareEndDateTime = mMonthSquareStartDateTime.plusDays(42);
	// if (mMonthSquareStartDateTime.getMillis() <= now
	// && now < mMonthSquareEndDateTime.getMillis()) {
	// DateTime currentDayStartDateTime = new DateTime(mMonthSquareStartDateTime);
	// for (int i = 0; i < 42; i++) {
	// long borderStartDateTime = currentDayStartDateTime.getMillis();
	// currentDayStartDateTime = currentDayStartDateTime.plusDays(1);
	// long borderEndDateTime = currentDayStartDateTime.getMillis();
	// if (borderStartDateTime <= now && now < borderEndDateTime) {
	// return i;
	// }
	// }
	// }
	// return null;
	// }
	/** Returns a list of day headers x coordinates */
	public ArrayList<Integer> getDayHeadersCoords(int suggestedViewWidth) {
		if (mSuggestedViewWidthOnLastRequestOfDayHeaderCoords != suggestedViewWidth) {
			mCachedDayHeaderCoords.clear();
		}
		if (mSuggestedViewWidthOnLastRequestOfDayHeaderCoords == suggestedViewWidth
				&& mCachedDayHeaderCoords.size() > 0) {
			return mCachedDayHeaderCoords;
		}
		mCachedDayHeaderCoords.clear();
		int childViewLeft;
		int childViewRight;
		float dayWidth = (suggestedViewWidth - mLeftBorderSpacerPx - mDaysXSpacerPx * 6 - mRightBorderSpacerPx) / 7.0f;
		// Prepare childViewRight for the cycle below
		childViewRight = mLeftBorderSpacerPx - mDaysXSpacerPx;
		for (int i = 0; i < 7; i++) {
			childViewLeft = childViewRight + mDaysXSpacerPx;
			childViewRight = (int) ((dayWidth + mDaysXSpacerPx) * (i + 1) - mDaysXSpacerPx);
			if (childViewLeft > suggestedViewWidth) {
				childViewLeft = suggestedViewWidth;
			}
			if (childViewRight > suggestedViewWidth) {
				childViewRight = suggestedViewWidth;
			}
			mCachedDayHeaderCoords.add(i * 2, childViewLeft);
			mCachedDayHeaderCoords.add(i * 2 + 1, childViewRight);
		}
		mCachedDayHeaderCoords.set(6 * 2 + 1, suggestedViewWidth - mRightBorderSpacerPx);
		mSuggestedViewWidthOnLastRequestOfDayHeaderCoords = suggestedViewWidth;
		return mCachedDayHeaderCoords;
	}

	/** @param month
	 *            the month to set */
	public long getStartMonth() {
		return mMonthStartDateTime.getTimeInMillis();
	}

	public long getMonthSquareStartDateTime() {
		return mMonthSquareStartDateTime.getTimeInMillis();
	}

	public long getMonthEndTime() {
		if (mMonthEndTime == null) {
			// recalculate mWeekEndTime
			mMonthEndTime = (Calendar) mMonthStartDateTime.clone();
			mMonthEndTime.add(Calendar.MONTH, 1);
		}
		return mMonthEndTime.getTimeInMillis();
	}

	public void setStartMonth(long startMonth) {
		Calendar newMonthStartDateTime = Calendar.getInstance();
		newMonthStartDateTime.setTimeInMillis(startMonth);
		CalendarHelper.toBeginningOfMonth(newMonthStartDateTime);
		if (mMonthStartDateTime == null
				|| mMonthStartDateTime.getTimeInMillis() != newMonthStartDateTime
						.getTimeInMillis()) {
			mMonthStartDateTime = newMonthStartDateTime;
			int firstDayOfWeek = Helper.getFirstDayOfWeek(getContext());
			// JodaTime count days of week starting from Monday
			int dayOfWeek = newMonthStartDateTime.get(Calendar.DAY_OF_WEEK);
			if (firstDayOfWeek <= dayOfWeek) {
				mMonthSquareStartDateTime = (Calendar) newMonthStartDateTime.clone();
				mMonthSquareStartDateTime.add(Calendar.DAY_OF_YEAR,
						-(dayOfWeek - firstDayOfWeek));
			} else {
				mMonthSquareStartDateTime = (Calendar) newMonthStartDateTime.clone();
				mMonthSquareStartDateTime.add(Calendar.DAY_OF_YEAR, -(dayOfWeek
						- firstDayOfWeek + 7));
			}
			mMonthEndTime = (Calendar) newMonthStartDateTime.clone();
			mMonthEndTime.add(Calendar.MONTH, 1);
			areTaskOccurrencesCalculated = false;
			requestLayout();
		}
	}

	private void calculateAndDistributeTaskOccurrences() {
		cacheCalendarViewTaskOccurrences();
		// List<List<CalendarViewTaskOccurrence>>
		// calendarViewTaskOccurrencesTakingWholeIntervalList = new
		// ArrayList<List<CalendarViewTaskOccurrence>>();
		// List<List<CalendarViewTaskOccurrence>>
		// calendarViewTaskOccurrencesNotTakingWholeIntervalList = new
		// ArrayList<List<CalendarViewTaskOccurrence>>();
		long borderStartDateTime;
		long borderEndDateTime;
		mDaysBorders = new long[84];
		Calendar currentDayStartDateTime = Calendar.getInstance();
		currentDayStartDateTime.setTimeInMillis(mMonthSquareStartDateTime
				.getTimeInMillis());
		for (int i = 0; i < 42; i++) {
			borderStartDateTime = currentDayStartDateTime.getTimeInMillis();
			currentDayStartDateTime.add(Calendar.DAY_OF_YEAR, 1);
			borderEndDateTime = currentDayStartDateTime.getTimeInMillis();
			mDaysBorders[i * 2] = borderStartDateTime;
			mDaysBorders[i * 2 + 1] = borderEndDateTime;
			MonthDayView monthDayView = mWeeks.get(i / 7).get(i % 7);
			List<CalendarViewTaskOccurrence> wholeDayCalendarViewTaskOccurrences = monthDayView
					.getWholeDayPreallocatedCalendarViewTaskOccurrences();
			wholeDayCalendarViewTaskOccurrences.clear();
			mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList
					.add(wholeDayCalendarViewTaskOccurrences);
			List<CalendarViewTaskOccurrence> notWholeDayCalendarViewTaskOccurrences = monthDayView
					.getNotWholeDayPreallocatedCalendarViewTaskOccurrences();
			notWholeDayCalendarViewTaskOccurrences.clear();
			mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList
					.add(notWholeDayCalendarViewTaskOccurrences);
		}
		List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
		for (Task task : mTasks) {
			calendarViewTaskOccurrences.addAll(task.selectTaskOccurrences(
					mDaysBorders[0], mDaysBorders[mDaysBorders.length - 1],
					Helper.getFirstDayOfWeek(getContext()),
					mCacheOfCalendarViewTaskOccurrences));
		}
		// now distribute calendarViewTaskOccurrences
		for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
			distributeTaskOccurrence(calendarViewTaskOccurrence, mDaysBorders,
					mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList,
					mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList);
		}
	}

	private void distributeTaskOccurrence(
			CalendarViewTaskOccurrence calendarViewTaskOccurrence,
			long[] borders,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesTakingWholeIntervalList,
			List<List<CalendarViewTaskOccurrence>> calendarViewTaskOccurrencesNotTakingWholeIntervalList) {
		for (int i = 0; i < borders.length; i += 2) {
			long currentBorderStartDateTime = borders[i];
			long currentBorderEndDateTime = borders[i + 1];
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesTakingWholeInterval = calendarViewTaskOccurrencesTakingWholeIntervalList
					.get(i / 2);
			List<CalendarViewTaskOccurrence> calendarViewTaskOccurrencesNotTakingWholeInterval = calendarViewTaskOccurrencesNotTakingWholeIntervalList
					.get(i / 2);
			if (Helper.isTakingWholeInterval(calendarViewTaskOccurrence.StartDateTime,
					calendarViewTaskOccurrence.EndDateTime, currentBorderStartDateTime,
					currentBorderEndDateTime)) {
				calendarViewTaskOccurrencesTakingWholeInterval
						.add(calendarViewTaskOccurrence);
			} else if (Helper.isTakingNotWholeInterval(
					calendarViewTaskOccurrence.StartDateTime,
					calendarViewTaskOccurrence.EndDateTime, currentBorderStartDateTime,
					currentBorderEndDateTime)) {
				calendarViewTaskOccurrencesNotTakingWholeInterval
						.add(calendarViewTaskOccurrence);
			}
		}
	}

	public void setTasks(List<Task> tasks) {
		mTasks = tasks;
		areTaskOccurrencesCalculated = false;
		requestLayout();
	}

	// private void cacheMonthDayViews() {
	// View v;
	// ArrayList<View> cv;
	// for (int i = 0; i < mPreallocatedHourViewsInDays.size(); i++) {
	// cv = mPreallocatedHourViewsInDays.get(i);
	// for (int j = 0; j < cv.size(); j++) {
	// v = cv.get(j);
	// mCacheOfHourViews.add(v);
	// }
	// }
	// mPreallocatedHourViewsInDays.clear();
	// }
	private void cacheCalendarViewTaskOccurrences() {
		for (List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences : mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList) {
			for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
				mCacheOfCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
			}
			calendarViewTaskOccurrences.clear();
			mCacheOfCalendarViewTaskOccurrencesLists.add(calendarViewTaskOccurrences);
		}
		mPreallocatedCalendarViewTaskOccurrencesTakingWholeIntervalList.clear();
		//
		for (List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences : mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList) {
			for (CalendarViewTaskOccurrence calendarViewTaskOccurrence : calendarViewTaskOccurrences) {
				mCacheOfCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
			}
			calendarViewTaskOccurrences.clear();
			mCacheOfCalendarViewTaskOccurrencesLists.add(calendarViewTaskOccurrences);
		}
		mPreallocatedCalendarViewTaskOccurrencesNotTakingWholeIntervalList.clear();
	}

	// private List<CalendarViewTaskOccurrence> getCachedCalendarViewTaskOccurrences() {
	// Iterator<List<CalendarViewTaskOccurrence>> iterator =
	// mCacheOfCalendarViewTaskOccurrencesLists
	// .iterator();
	// if (iterator.hasNext()) {
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = iterator
	// .next();
	// iterator.remove();
	// return calendarViewTaskOccurrences;
	// }
	// List<CalendarViewTaskOccurrence> list = new
	// ArrayList<CalendarViewTaskOccurrence>();
	// return list;
	// }
	/** Checks if there is a cached multiday task view that can be used
	 *
	 * @return A cached multiday task view or, if none was found, newly inflated */
	// private Button getCachedMultiDayTaskView() {
	// Iterator<Button> iterator = mCacheOfMultiDayTaskViews.iterator();
	// if (iterator.hasNext()) {
	// Button v = iterator.next();
	// iterator.remove();
	// return v;
	// }
	// Button b = (Button) LayoutInflater.from(getContext()).inflate(
	// R.layout.week_multiday_task_view, null);
	// // b.setBackgroundColor(0xAAFFFF00);
	// return b;
	// }
	/** Checks if there is a cached hour view that can be used
	 *
	 * @return A cached hour view or, if none was found, newly created */
	// private View getCachedHourView() {
	// Iterator<View> iterator = mCacheOfHourViews.iterator();
	// if (iterator.hasNext()) {
	// View v = iterator.next();
	// iterator.remove();
	// return v;
	// }
	// View view = new View(getContext());
	// view.setBackgroundColor(getResources().getColor(R.color.White));
	// return view;
	// }
	/** Checks if there is a cached event view that can be used
	 *
	 * @return A cached event view or, if none was found, newly inflated */
	// private WeekTaskView getCachedTaskView() {
	// Iterator<WeekTaskView> iterator = mCacheOfTaskViews.iterator();
	// if (iterator.hasNext()) {
	// WeekTaskView v = iterator.next();
	// iterator.remove();
	// return v;
	// }
	// // WeekTaskView b = (WeekTaskView)
	// // LayoutInflater.from(getContext()).inflate(R.layout.task_view,
	// // null);
	// WeekTaskView b = new WeekTaskView(getContext());
	// // b.setOnClickListener(this);
	// return b;
	// }
	public static int getTextHeight(String text, float textSize, Typeface typeface) {
		TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		paint.setTextSize(textSize);
		paint.setTypeface(typeface);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		return bounds.height();
	}

	public int getTopBorderWidth() {
		// TODO Auto-generated method stub
		return topBorderSpacerPx;
	}
}
