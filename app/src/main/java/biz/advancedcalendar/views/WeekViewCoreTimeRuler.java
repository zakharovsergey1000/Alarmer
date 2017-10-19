package biz.advancedcalendar.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import biz.advancedcalendar.greendao.Task.BusinessHoursTaskDisplayingPolicy;
import java.util.ArrayList;
import java.util.List;

/** A week view that displays the events in one week */
public class WeekViewCoreTimeRuler extends View {
	private class TimeIntervalCoords {
		private int[] PreBusinessHoursTimeIntervalCoords;
		private int[] BusinessHoursTimeIntervalFirstNotWholeHourCoords;
		private List<Integer> BusinessHoursTimeIntervalWholeHoursCoords;
		private int[] BusinessHoursTimeIntervalLastNotWholeHourCoords;
		private int[] PostBusinessHoursTimeIntervalCoords;
	}

	//
	private static final int MIN_TASK_HEIGHT_DIP = 48;
	private static final int PRE_BUSINESS_HOURS_TASKS_HEIGHT_DIP = WeekViewCoreTimeRuler.MIN_TASK_HEIGHT_DIP;
	private static final int POST_BUSINESS_HOURS_TASKS_HEIGHT_DIP = WeekViewCoreTimeRuler.MIN_TASK_HEIGHT_DIP;
	private static final int WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_DIP = 3;
	private static final int MIN_HOURS_UNIT_HEIGHT_DIP = 20;
	private static final int HOUR_HEADER_AREA_COLOR = 0xFFFFFFFF;
	// private static final int HOUR_HEADER_TEXT_SIZE_SP = 15;
	// private static final float HOURS_TEXT_SIZE_SP = 15.0f;// 20.0f;// 12.0f;
	private static final int HOUR_HEADER_AND_DAYS_SPACER_DIP = 1;
	private static final int HOUR_HEADER_AND_DAYS_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static final int HOURS_Y_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static final int DEFAULT_HOUR_HEIGHT_DIP = 50;
	// private static final int BOTTOM_BORDER_SPACER_DIP = 0;
	// private static final int BOTTOM_BORDER_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static final int HOURS_NUMBERS_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private final String[] mHours = new String[] {"00", "01", "02", "03", "04", "05",
			"06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18",
			"19", "20", "21", "22", "23",};
	private final Paint mPaint;
	private final TextPaint mTextPaint;
	private final float mDensity;
	private final int mHourHeaderAndDaysSpacerPx;
	private final int mHoursSpacerPx;
	private int mHourHeaderWidthPx;
	private final int mHourMarkerLineWidthPx;
	private final int mDefaultHourHeightPx;
	private int mBusinessHoursStartTime, mBusinessHoursEndTime;
	private BusinessHoursTaskDisplayingPolicy mPrePostBusinessHoursTaskOccurrenceDistributionMode;
	// private Rect mWholeDayTasksThisDayTasksSpacerRect;
	private Rect hourHeaderRect;
	private int mWholeDayTasksThisDayTasksSpacerPx;
	// private final int topBorderSpacerPx;
	private final int mMinHoursUnitHeight;
	private final int mMinTaskHeightPx;
	private final int mPreBusinessHoursTasksHeightPx;
	private final int mPostBusinessHoursTasksHeightPx;
	private int mWholeDayTasksHeightPx;
	private final Rect mTextBounds;
	private Integer mSetHeight;
	private TimeIntervalCoords mTimeIntervalCoords;
	private List<Integer> mIntegersList;
	private TaskOccurrencesDistributionState mTaskOccurrencesDistributionState;
	private Integer mWeekViewCoreTop;
	private Integer mWeekViewCoreHoursDrawingTop;
	private TimeIntervalViewsPositions mTimeIntervalViewsPositions;

	public WeekViewCoreTimeRuler(Context context) {
		this(context, null, 0);
	}

	public WeekViewCoreTimeRuler(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WeekViewCoreTimeRuler(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = getResources();
		// final CompatibilityInfo compat = res.getCompatibilityInfo();
		mDensity = res.getDisplayMetrics().density;
		mHourHeaderAndDaysSpacerPx = Math.max(
				1,
				Math.round(mDensity
						* WeekViewCoreTimeRuler.HOUR_HEADER_AND_DAYS_SPACER_DIP));
		mWholeDayTasksThisDayTasksSpacerPx = Math.max(1, Math.round(mDensity
				* WeekViewCoreTimeRuler.WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_DIP));
		mMinHoursUnitHeight = Math.max(1,
				Math.round(mDensity * WeekViewCoreTimeRuler.MIN_HOURS_UNIT_HEIGHT_DIP));
		mHoursSpacerPx = 1;
		mHourMarkerLineWidthPx = 1;// (int) (mDensity *
									// WeekViewCoreTimeRuler.HOUR_MARKER_LINE_WIDTH_DIP);
		// mWholeDayTasksHeightPx = Math.max(1,
		// Math.round(mDensity * WeekViewCore.WHOLE_DAY_TASKS_AREA_HEIGHT_DIP));
		mDefaultHourHeightPx = (int) (mDensity * WeekViewCoreTimeRuler.DEFAULT_HOUR_HEIGHT_DIP);
		mPaint = new Paint();
		mPaint.setColor(0xFF808080);
		// mPaint.setStrokeWidth(mHoursSpacerPx);
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		// mTextPaint.density = res.getDisplayMetrics().density;
		mTextPaint.setColor(WeekViewCoreTimeRuler.HOURS_NUMBERS_COLOR);
		mTextPaint.setTextAlign(Paint.Align.RIGHT);
		mTextBounds = new Rect();
		mMinTaskHeightPx = Math.max(mTextBounds.bottom - mTextBounds.top,
				Math.round(mDensity * WeekViewCoreTimeRuler.MIN_TASK_HEIGHT_DIP));
		mPreBusinessHoursTasksHeightPx = Math.max(
				mTextBounds.bottom - mTextBounds.top,
				Math.round(mDensity
						* WeekViewCoreTimeRuler.PRE_BUSINESS_HOURS_TASKS_HEIGHT_DIP));
		mPostBusinessHoursTasksHeightPx = Math.max(
				mTextBounds.bottom - mTextBounds.top,
				Math.round(mDensity
						* WeekViewCoreTimeRuler.POST_BUSINESS_HOURS_TASKS_HEIGHT_DIP));
		mWholeDayTasksHeightPx = mMinTaskHeightPx;
		mTextPaint.getTextBounds("0123456789", 0, 10, mTextBounds);
		hourHeaderRect = new Rect(0, 0, mHourHeaderWidthPx, 0);
		mBusinessHoursStartTime = 0;
		mBusinessHoursEndTime = 24 * 60 * 60 * 1000;
		mIntegersList = new ArrayList<Integer>();
		mPrePostBusinessHoursTaskOccurrenceDistributionMode = BusinessHoursTaskDisplayingPolicy.DISPLAY_SEPARATED;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		int proposedWidth = mHourHeaderWidthPx + mHourHeaderAndDaysSpacerPx;
		int proposedHeight;
		if (mSetHeight != null) {
			proposedHeight = mSetHeight;
		} else {
			int mBusinessHoursStartTimeHourRemainder = mBusinessHoursStartTime % 3600000;
			int mBusinessHoursEndTimeHourRemainder = mBusinessHoursEndTime % 3600000;
			//
			int businessHoursTimeInterval = mBusinessHoursEndTime
					- mBusinessHoursStartTime;
			int wholeHoursCount = (mBusinessHoursEndTime - (mBusinessHoursEndTimeHourRemainder == 0 ? 0
					: 60 * 60 * 1000 - mBusinessHoursEndTimeHourRemainder))
					/ (60 * 60 * 1000)
					- (mBusinessHoursStartTime + mBusinessHoursStartTimeHourRemainder)
					/ (60 * 60 * 1000);
			int businessHoursSpacersCount = wholeHoursCount - 1
					+ (mBusinessHoursStartTimeHourRemainder == 0 ? 0 : 1)
					+ (mBusinessHoursEndTimeHourRemainder == 0 ? 0 : 1);
			int businessHoursHeight = (int) (businessHoursTimeInterval
					/ (60 * 60 * 1000f) * mDefaultHourHeightPx - mHoursSpacerPx
					* businessHoursSpacersCount + 0.5);
			proposedHeight = businessHoursHeight;
		}
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

	private TimeIntervalCoords calculateTimeIntervalCoords(final int left, final int top,
			final int right, final int bottom) {
		//
		TimeIntervalCoords timeIntervalViewsPositions = new TimeIntervalCoords();
		// if (mAssignedTaskOccurrencesDistributionState == null) {
		// mAssignedTaskOccurrencesDistributionState = new
		// TaskOccurrencesDistributionState();
		// }
		if (mTaskOccurrencesDistributionState.PreBusinessHoursTasksExist) {
			timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords = new int[2];
			//
			timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[0] = top
					+ (mTaskOccurrencesDistributionState.AllDayTasksExist ? mWholeDayTasksHeightPx
							+ mWholeDayTasksThisDayTasksSpacerPx
							: 0);
			timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[1] = timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[0]
					+ mPreBusinessHoursTasksHeightPx;
		} else {
			timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords = null;
		}
		//
		if (mTaskOccurrencesDistributionState.PostBusinessHoursTasksExist) {
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords = new int[2];
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[1] = bottom;
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[0] = bottom
					- mPreBusinessHoursTasksHeightPx;
		} else {
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords = null;
		}
		//
		int mBusinessHoursStartTimeHourRemainder = mBusinessHoursStartTime % 3600000;
		int mBusinessHoursEndTimeHourRemainder = mBusinessHoursEndTime % 3600000;
		int wholeHoursCount = (mBusinessHoursEndTime - mBusinessHoursEndTimeHourRemainder)
				/ (60 * 60 * 1000)
				- (mBusinessHoursStartTime + (mBusinessHoursStartTimeHourRemainder == 0 ? 0
						: 60 * 60 * 1000 - mBusinessHoursStartTimeHourRemainder))
				/ (60 * 60 * 1000);
		if (wholeHoursCount < 0) {
			wholeHoursCount = 0;
		}
		int businessHoursSpacersCount = wholeHoursCount - 1
				+ (mBusinessHoursStartTimeHourRemainder == 0 ? 0 : 1)
				+ (mBusinessHoursEndTimeHourRemainder == 0 ? 0 : 1);
		if (businessHoursSpacersCount < 0) {
			businessHoursSpacersCount = 0;
		}
		int businessHoursTop = top
				+ (mTaskOccurrencesDistributionState.AllDayTasksExist ? mWholeDayTasksHeightPx
						+ mWholeDayTasksThisDayTasksSpacerPx
						: 0)
				+ (mTaskOccurrencesDistributionState.PreBusinessHoursTasksExist ? mPreBusinessHoursTasksHeightPx
						+ mHoursSpacerPx
						: 0);
		int businessHoursBottom = bottom
				- (mTaskOccurrencesDistributionState.PostBusinessHoursTasksExist ? mPostBusinessHoursTasksHeightPx
						+ mHoursSpacerPx
						: 0);
		int businessHoursPixels = businessHoursBottom - businessHoursTop - mHoursSpacerPx
				* businessHoursSpacersCount;
		float pixelsPerMillisecond = businessHoursPixels
				/ (float) (mBusinessHoursEndTime - mBusinessHoursStartTime);
		//
		int wholeBusinessHoursTop = businessHoursTop;
		int wholeBusinessHoursBottom = businessHoursBottom;
		//
		if (mBusinessHoursStartTimeHourRemainder != 0) {
			timeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords = new int[2];
			//
			timeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[0] = businessHoursTop;
			timeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[1] = (int) (businessHoursTop
					+ (3600000 - mBusinessHoursStartTimeHourRemainder)
					* pixelsPerMillisecond + 0.5);
			wholeBusinessHoursTop = timeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords[1]
					+ mHoursSpacerPx;
		} else {
			timeIntervalViewsPositions.BusinessHoursTimeIntervalFirstNotWholeHourCoords = null;
		}
		if (mBusinessHoursEndTimeHourRemainder != 0) {
			timeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords = new int[2];
			//
			timeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[1] = businessHoursBottom;
			timeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[0] = (int) (businessHoursBottom
					- mBusinessHoursEndTimeHourRemainder * pixelsPerMillisecond + 0.5);
			wholeBusinessHoursBottom = timeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords[0]
					- mHoursSpacerPx;
		} else {
			timeIntervalViewsPositions.BusinessHoursTimeIntervalLastNotWholeHourCoords = null;
		}
		timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords = new ArrayList<Integer>(
				wholeHoursCount * 2);
		//
		float pixelsPerHour = pixelsPerMillisecond * 3600000;
		int childViewTop, childViewBottom;
		childViewTop = wholeBusinessHoursTop;
		for (int i1 = 0; i1 < wholeHoursCount; i1++) {
			if (i1 > 0) {
				childViewTop = timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
						.get(timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
								.size() - 1)
						+ mHoursSpacerPx;
			}
			childViewBottom = (int) (wholeBusinessHoursTop
					+ (pixelsPerHour + mHoursSpacerPx) * i1 + pixelsPerHour + 0.5);
			timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
					.add(childViewTop);
			timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
					.add(childViewBottom);
		}
		if (timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords.size() > 0) {
			timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords.set(
					timeIntervalViewsPositions.BusinessHoursTimeIntervalWholeHoursCoords
							.size() - 1, wholeBusinessHoursBottom);
		}
		return timeIntervalViewsPositions;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Fill hour header area with color
		mPaint.setColor(WeekViewCoreTimeRuler.HOUR_HEADER_AREA_COLOR);
		canvas.drawRect(hourHeaderRect, mPaint);
		if (mWeekViewCoreTop == null) {
			// we are not ready to draw yet
			return;
		}
		int left = 0;
		int top = mWeekViewCoreHoursDrawingTop;
		int right = getWidth();
		int bottom = getHeight();
		if (mTimeIntervalCoords == null) {
			mTimeIntervalCoords = calculateTimeIntervalCoords(left, top, right, bottom);
		}
		top = mWeekViewCoreTop;
		// draw right spacer
		mPaint.setColor(WeekViewCoreTimeRuler.HOUR_HEADER_AND_DAYS_SPACER_COLOR);
		canvas.drawRect(right - mHourHeaderAndDaysSpacerPx, top, right, bottom, mPaint);
		// Draw horizontal lines and hours
		mPaint.setColor(WeekViewCoreTimeRuler.HOURS_Y_SPACER_COLOR);
		int mHoursStep = 1;
		int mWholeHourHeight = mTimeIntervalCoords.BusinessHoursTimeIntervalWholeHoursCoords
				.get(1)
				- mTimeIntervalCoords.BusinessHoursTimeIntervalWholeHoursCoords.get(0);
		int currentUnitWidth = mWholeHourHeight;
		while (currentUnitWidth < mMinHoursUnitHeight && mHoursStep < 24) {
			switch (mHoursStep) {
			case 1:
				mHoursStep = 2;
				break;
			case 2:
				mHoursStep = 3;
				break;
			case 3:
				mHoursStep = 4;
				break;
			case 4:
				mHoursStep = 6;
				break;
			case 6:
				mHoursStep = 8;
				break;
			case 8:
				mHoursStep = 12;
				break;
			case 12:
				mHoursStep = 24;
			}
			currentUnitWidth = (int) (mWholeHourHeight * mHoursStep + 0.5);
		}
		// draw hour markers
		int hourMarkerLineLeft = mHourHeaderWidthPx - mHourMarkerLineWidthPx;
		mIntegersList.clear();
		List<Integer> hoursCoords = mIntegersList;
		if (mTimeIntervalCoords.PreBusinessHoursTimeIntervalCoords != null
				|| mTimeIntervalCoords.BusinessHoursTimeIntervalFirstNotWholeHourCoords != null) {
			// initialIndex = 2;
			if (mTimeIntervalCoords.BusinessHoursTimeIntervalFirstNotWholeHourCoords != null) {
				hoursCoords
						.add(mTimeIntervalCoords.BusinessHoursTimeIntervalFirstNotWholeHourCoords[1]);
			} else {
				hoursCoords
						.add(mTimeIntervalCoords.PreBusinessHoursTimeIntervalCoords[1]);
			}
			hoursCoords.add(mTimeIntervalCoords.BusinessHoursTimeIntervalWholeHoursCoords
					.get(0));
		}
		for (int i = mHoursStep * 2 - 1, count = mTimeIntervalCoords.BusinessHoursTimeIntervalWholeHoursCoords
				.size() - 1; i < count; i += mHoursStep * 2) {
			hoursCoords.add(mTimeIntervalCoords.BusinessHoursTimeIntervalWholeHoursCoords
					.get(i));
			hoursCoords.add(mTimeIntervalCoords.BusinessHoursTimeIntervalWholeHoursCoords
					.get(i + 1));
		}
		//
		int hour = mHoursStep;
		if (mBusinessHoursStartTime != 0) {
			hour = (mBusinessHoursStartTime + (mBusinessHoursStartTime % 3600000 == 0 ? 0
					: 3600000 - mBusinessHoursStartTime % 3600000)) / 3600000;
			if (mTimeIntervalCoords.PreBusinessHoursTimeIntervalCoords == null
					&& mTimeIntervalCoords.BusinessHoursTimeIntervalFirstNotWholeHourCoords == null) {
				hour += mHoursStep;
			}
		}
		//
		for (int i = 0, count = hoursCoords.size() - 1; i < count; i += 2) {
			top = hoursCoords.get(i);
			// bottom = top + mHoursSpacerPx;
			bottom = hoursCoords.get(i + 1);
			// Draw Hour marker
			canvas.drawRect(hourMarkerLineLeft, top, right, bottom, mPaint);
			// Draw the hour text
			canvas.drawText(mHours[hour],
			//
					hourMarkerLineLeft,
					//
					bottom - (bottom - top) / 2.0f
							+ (mTextBounds.bottom - mTextBounds.top) / 2.0f - 1,
					//
					mTextPaint);
			hour += mHoursStep;
		}
	}

	public int getHourHeaderAndDaysSpacerWidth() {
		return mHourHeaderAndDaysSpacerPx;
	}

	public void setBusinessHoursStartTime(int businessHoursStartTime) {
		if (mBusinessHoursStartTime != businessHoursStartTime) {
			mBusinessHoursStartTime = businessHoursStartTime;
			mTimeIntervalCoords = null;
			invalidate();
		}
	}

	public void setBusinessHoursEndTime(int businessHoursEndTime) {
		if (mBusinessHoursEndTime != businessHoursEndTime) {
			mBusinessHoursEndTime = businessHoursEndTime;
			mTimeIntervalCoords = null;
			invalidate();
		}
	}

	public void setPrePostBusinessHoursTaskOccurrenceDistributionMode(
			BusinessHoursTaskDisplayingPolicy mode) {
		if (mPrePostBusinessHoursTaskOccurrenceDistributionMode != mode) {
			mPrePostBusinessHoursTaskOccurrenceDistributionMode = mode;
			mTimeIntervalCoords = null;
			invalidate();
		}
	}

	public void setTaskOccurrencesDistributionState(
			TaskOccurrencesDistributionState taskOccurrencesDistributionState,
			TimeIntervalViewsPositions timeIntervalViewsPositions) {
		if (mTaskOccurrencesDistributionState == null
				|| !mTaskOccurrencesDistributionState
						.equals(taskOccurrencesDistributionState)) {
			mTaskOccurrencesDistributionState = taskOccurrencesDistributionState;
			mTimeIntervalCoords = null;
			mTimeIntervalViewsPositions = timeIntervalViewsPositions;
			invalidate();
		}
	}

	public void setWeekViewCoreTop(int weekViewCoreTop) {
		if (mWeekViewCoreTop == null || mWeekViewCoreTop != weekViewCoreTop) {
			mWeekViewCoreTop = weekViewCoreTop;
			mTimeIntervalCoords = null;
			invalidate();
		}
	}

	public Integer getWeekViewCoreTop() {
		return mWeekViewCoreTop;
	}

	public void setWeekViewCoreHoursDrawingTop(int weekViewCoreHoursDrawingTop) {
		if (mWeekViewCoreHoursDrawingTop == null
				|| mWeekViewCoreHoursDrawingTop != weekViewCoreHoursDrawingTop) {
			mWeekViewCoreHoursDrawingTop = weekViewCoreHoursDrawingTop;
			mTimeIntervalCoords = null;
			invalidate();
		}
	}

	public Integer getWeekViewCoreHoursDrawingTop() {
		return mWeekViewCoreHoursDrawingTop;
	}

	public void setHeight(Integer height) {
		mSetHeight = height;
		mTimeIntervalCoords = null;
		invalidate();
	}

	public void setTextSize(float size) {
		mTextPaint.setTextSize(size);
		mTextPaint.getTextBounds("MM", 0, 2, mTextBounds);
		mHourHeaderWidthPx = mTextBounds.right - mTextBounds.left;
	}
}
