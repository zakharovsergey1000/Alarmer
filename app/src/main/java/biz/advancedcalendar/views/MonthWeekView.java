package biz.advancedcalendar.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import java.util.Calendar;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MonthWeekView extends ViewGroup {
	private static final int DEFAULT_HEIGHT_DIP = 48;
	private static final int DEFAULT_WIDTH_DIP = 72;
	private static final int BACKGROUND_COLOR = 0xFFEEEEEE;
	private static final int HORIZONTAL_SPACER_DIP = 1;
	private static final int TOP_SPACER_DIP = 1;
	private static final int BOTTOM_SPACER_DIP = 1;
	// private static final int LEFT_SPACER_DIP = 1;
	// private static final int RIGHT_SPACER_DIP = 1;
	private long mCurrentlySelectedDay;
	private Paint mPaint;
	private int mDefaultHeightPx;
	private int mDefaultWidthPx;
	private int mHorizontalSpacerPx;
	private int mTopSpacerPx;
	private int mBottomSpacerPx;
	// private int mLeftSpacerPx;
	// private int mRightSpacerPx;
	// private List<Integer> mDayHeadersCoords;
	private float mDensity;
	private MonthDayView[] mMonthDayViews = new MonthDayView[7];
	private long mCurrentMonth;
	private long mCurrentMonthEndDateTime;
	private long[] mDaysBorders;
	private Integer mPosition;

	// private float mTextSize;
	// private TaskOccurrencesDistribution mTaskOccurrencesDistribution;
	public MonthWeekView(Context context) {
		super(context);
		init(context);
	}

	/** Constructor
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle */
	public MonthWeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	/** Constructor
	 *
	 * @param context
	 *            The context
	 * @param attrs
	 *            Attributes
	 * @param defStyle
	 *            defStyle */
	public MonthWeekView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public MonthWeekView(Context context, AttributeSet attrs, int defStyle,
			int defStyleRes) {
		// super(context, attrs, defStyle, defStyleRes);
		super(context, attrs, defStyle);
		init(context);
	}

	public int getWeekViewCoreTop() {
		return mDefaultWidthPx;
	}

	public int getWeekViewCoreHoursDrawingTop() {
		return mDefaultWidthPx + mHorizontalSpacerPx;
	}

	private void init(final Context context) {
		for (int i = 0; i < 7; i++) {
			mMonthDayViews[i] = new MonthDayView(context);
			addView(mMonthDayViews[i]);
		}
		mPaint = new Paint();
		mPaint.setColor(MonthWeekView.BACKGROUND_COLOR);
		mHorizontalSpacerPx = Math.max(
				1,
				Math.round(getResources().getDisplayMetrics().density
						* MonthWeekView.HORIZONTAL_SPACER_DIP));
		setWillNotDraw(false);
		final Resources res = getResources();
		mDensity = res.getDisplayMetrics().density;
		mDefaultWidthPx = (int) (mDensity * MonthWeekView.DEFAULT_WIDTH_DIP);
		mDefaultHeightPx = (int) (mDensity * MonthWeekView.DEFAULT_HEIGHT_DIP);
		mTopSpacerPx = (int) (mDensity * MonthWeekView.TOP_SPACER_DIP);
		mBottomSpacerPx = (int) (mDensity * MonthWeekView.BOTTOM_SPACER_DIP);
		// mLeftSpacerPx = (int) (mDensity * MonthWeekView.LEFT_SPACER_DIP);
		// mRightSpacerPx = (int) (mDensity * MonthWeekView.RIGHT_SPACER_DIP);
	}

	public void setTaskClickedListener(TaskClickedListener taskClickedListener) {
		// mWeekViewCore.setTaskClickedListener(taskClickedListener);
	}

	public void setDayClickedListener(DaysSelectedListener dayClickedListener) {
		for (int i = 0; i < 7; i++) {
			mMonthDayViews[i].setDaysSelectedListener(dayClickedListener);
		}
	}

	public void setDayLongClickedListener(DayLongClickedListener dayLongClickedListener) {
		for (int i = 0; i < 7; i++) {
			mMonthDayViews[i].setDayLongClickedListener(dayLongClickedListener);
		}
	}

	// public void setDayHeaderClickedListener(
	// WeekViewDayHeaderClickedListener dayHeaderClickedListener) {
	// // mWeekViewWeekDaysHeader.setDayHeaderClickedListener(dayHeaderClickedListener);
	// }
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		//
		int proposedWidth = mDefaultWidthPx;
		//
		switch (specModeWidth) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (proposedWidth > specSizeWidth) {
				proposedWidth = specSizeWidth;
			}
			break;
		case MeasureSpec.EXACTLY:
			proposedWidth = specSizeWidth;
			break;
		}
		//
		int proposedHeight = mDefaultHeightPx + mTopSpacerPx + mBottomSpacerPx;
		//
		switch (specModeHeight) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (proposedHeight > specSizeHeight) {
				proposedHeight = specSizeHeight;
			}
			break;
		case MeasureSpec.EXACTLY:
			proposedHeight = specSizeHeight;
			break;
		}
		setMeasuredDimension(proposedWidth, proposedHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childViewLeft = 0;
		int childViewTop = mTopSpacerPx;
		int childViewRight = r - l;
		int childViewBottom = b - t;
		float dayWidth = (r - l - mHorizontalSpacerPx * 6) / (float) 7;
		// Prepare childViewRight for the cycle below
		childViewRight = l - mHorizontalSpacerPx;
		for (int i = 0; i < 7; i++) {
			childViewLeft = childViewRight + mHorizontalSpacerPx;
			childViewRight = (int) ((dayWidth + mHorizontalSpacerPx) * (i + 1) - mHorizontalSpacerPx);
			if (childViewLeft > r) {
				childViewLeft = r;
			}
			if (childViewRight > r) {
				childViewRight = r;
			}
			MonthDayView monthDayView = mMonthDayViews[i];
			long dayStartDateTime = mDaysBorders[i * 2];
			monthDayView.setDayStartDateTime(dayStartDateTime);
			if (mCurrentMonth <= dayStartDateTime
					&& dayStartDateTime < mCurrentMonthEndDateTime) {
				monthDayView.setPale(false);
			} else {
				monthDayView.setPale(true);
			}
			monthDayView.layout(childViewLeft, childViewTop, childViewRight,
					childViewBottom);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// if (!mShowDayHeaders) {
		// return;
		// }
		mPaint.setColor(MonthWeekView.BACKGROUND_COLOR);
		canvas.drawRect(0, 0, getWidth(), mDefaultWidthPx + mHorizontalSpacerPx, mPaint);
		mPaint.setColor(MonthWeekView.BACKGROUND_COLOR);
		canvas.drawRect(0, mDefaultWidthPx, getWidth(), mDefaultWidthPx
				+ mHorizontalSpacerPx, mPaint);
	}

	public void setBusinessHoursStartTime(int businessHoursStartTime) {
		// mWeekViewCore.setBusinessHoursStartTime(businessHoursStartTime);
	}

	public void setBusinessHoursEndTime(int businessHoursEndTime) {
		// mWeekViewCore.setBusinessHoursEndTime(businessHoursEndTime);
	}

	public long getCurrentlySelectedDay() {
		return mCurrentlySelectedDay;
	}

	public long getFirstDayStartDateTime() {
		return mDaysBorders[0];
	}

	public long getLastDayEndDateTime() {
		return mDaysBorders[mDaysBorders.length - 1];
	}

	public long getPrevailingMonth() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(mDaysBorders[0]);
		int firstDayMonth = calendar1.get(Calendar.MONTH);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTimeInMillis(mDaysBorders[6]);
		int fourthDayMonth = calendar2.get(Calendar.MONTH);
		if (firstDayMonth == fourthDayMonth) {
			calendar1.set(Calendar.DAY_OF_MONTH, 1);
			return calendar1.getTimeInMillis();
		} else {
			calendar2.setTimeInMillis(mDaysBorders[mDaysBorders.length - 2]);
			calendar2.set(Calendar.DAY_OF_MONTH, 1);
			return calendar2.getTimeInMillis();
		}
	}

	public long getFirstMonth() {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(mDaysBorders[0]);
		calendar1.set(Calendar.DAY_OF_MONTH, 1);
		return calendar1.getTimeInMillis();
	}

	public void setDayNumberTextSize(float size) {
		for (int i = 0; i < 7; i++) {
			mMonthDayViews[i].setDayNumberTextSize(size);
		}
	}

	public void setMarkSyncNeeded(boolean markSyncNeeded) {
		// mWeekViewCore.setMarkSyncNeeded(markSyncNeeded);
	}

	public void setMode(Integer mode) {
		// mWeekViewCore.setMode(mode);
	}

	public void setTaskOccurrencesDistribution(
			TaskOccurrencesDistribution taskOccurrencesDistribution, long[] daysBorders) {
		mDaysBorders = daysBorders;
		if (taskOccurrencesDistribution != null) {
			for (int i = 0; i < 7; i++) {
				TaskOccurrencesDistribution taskOccurrencesDistribution1 = new TaskOccurrencesDistribution(
						1);
				taskOccurrencesDistribution1.TaskOccurrencesTakingWholeIntervalList
						.set(0,
								taskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList
										.get(i));
				taskOccurrencesDistribution1.TaskOccurrencesTouchingPreBusinessHoursList
						.set(0,
								taskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList
										.get(i));
				taskOccurrencesDistribution1.TaskOccurrencesTouchingBusinessHoursList
						.set(0,
								taskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList
										.get(i));
				taskOccurrencesDistribution1.TaskOccurrencesTouchingPostBusinessHoursList
						.set(0,
								taskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList
										.get(i));
				mMonthDayViews[i]
						.setTaskOccurrencesDistribution(taskOccurrencesDistribution1);
			}
		}
		// mWeekViewCore.setTaskOccurrencesDistribution(taskOccurrencesDistribution,
		// daysBorders);
		requestLayout();
	}

	public TaskOccurrencesDistribution getTaskOccurrencesDistribution() {
		return null;
		// return mWeekViewCore.getTaskOccurrencesDistribution();
	}

	public void setCurrentMonth(Long currentMonth) {
		mCurrentMonth = currentMonth;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mCurrentMonth);
		calendar.add(Calendar.MONTH, 1);
		mCurrentMonthEndDateTime = calendar.getTimeInMillis();
	}

	public void setPosition(int position) {
		if (mPosition == null || mPosition != position) {
			mPosition = position;
			for (int i = 0; i < 7; i++) {
				MonthDayView monthDayView = mMonthDayViews[i];
				monthDayView.setTag(new int[] {position, i});
			}
		}
	}
}
