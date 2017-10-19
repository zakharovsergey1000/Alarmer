package biz.advancedcalendar.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import java.util.ArrayList;

public class WeekTaskView {
	private static final int TRANSPARENT_COLOR = 0;
	private static final int TRANSPARENT_COLOR_WIDTH_DIP = 5;
	private static final int TASK_NAME_TASK_TIME_SPACER_DIP = 1;
	private final float transparentColorWidthPx;
	private final float mDensity;
	private final Paint mBackgroundPaint;
	private int mBackgroundColor;
	private int mTextColor;
	private final Paint mTextPaint;
	private int mRealTaskTopPx;
	private int mRealTaskBottomPx;
	private ArrayList<String> mTaskInformationStrings;
	private final int taskNameTaskTimeSpacerPx;
	private CalendarViewTaskOccurrence mCalendarViewTaskOccurrence;
	private int mLeft;
	private int mTop;
	private int mRight;
	private int mBottom;
	private Rect[] mTaskInformationStringsTextBoundsArray;
	private float[] mTaskInformationStringsTextBaselinesArray;

	/** Constructor
	 *
	 * @param context */
	public WeekTaskView(Context context) {
		this(context, null, 0);
	}

	/** Constructor
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle */
	public WeekTaskView(final Context context, final AttributeSet attrs) {
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
	public WeekTaskView(Context context, AttributeSet attrs, int defStyle) {
		mDensity = context.getResources().getDisplayMetrics().density;
		transparentColorWidthPx = (int) (mDensity
				* WeekTaskView.TRANSPARENT_COLOR_WIDTH_DIP + 0.5);
		mBackgroundPaint = new Paint();
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		taskNameTaskTimeSpacerPx = (int) (mDensity * WeekTaskView.TASK_NAME_TASK_TIME_SPACER_DIP);
	}

	protected void draw(Canvas canvas) {
		final int left = mLeft;
		final int top = mTop;
		final int right = mRight;
		final int bottom = mBottom;
		canvas.save();
		canvas.clipRect(left, top, right, bottom);
		// draw RealTaskHeight background
		mBackgroundPaint.setColor(mBackgroundColor);
		canvas.drawRect(left, top + mRealTaskTopPx, right, top + mRealTaskBottomPx,
				mBackgroundPaint);
		// draw transparent background
		mBackgroundPaint.setColor(WeekTaskView.TRANSPARENT_COLOR);
		canvas.drawRect(left, top, left + transparentColorWidthPx, top + mRealTaskTopPx,
				mBackgroundPaint);
		canvas.drawRect(left, top + mRealTaskBottomPx, left + transparentColorWidthPx,
				bottom, mBackgroundPaint);
		// draw semitransparent background
		// mBackgroundPaint.setColor(mBackgroundSemitransparentColor);
		mBackgroundPaint.setColor(mBackgroundColor);
		canvas.drawRect(left + transparentColorWidthPx, top, right, top + mRealTaskTopPx,
				mBackgroundPaint);
		canvas.drawRect(left + transparentColorWidthPx, top + mRealTaskBottomPx, right,
				bottom, mBackgroundPaint);
		//
		float sum = top;
		for (int i = 0; i < mTaskInformationStrings.size(); i++) {
			String text = mTaskInformationStrings.get(i);
			sum += mTaskInformationStringsTextBoundsArray[i].height()
					+ mTaskInformationStringsTextBaselinesArray[i]
					+ taskNameTaskTimeSpacerPx;
			canvas.drawText(text, left + transparentColorWidthPx, sum, mTextPaint);
		}
		//
		canvas.restore();
	}

	public void setTextSize(float size) {
		mTextPaint.setTextSize(size);
	}

	public void setRealTaskTopPx(int realTaskTop) {
		mRealTaskTopPx = realTaskTop;
	}

	public void setRealTaskBottomPx(int realTaskBottom) {
		mRealTaskBottomPx = realTaskBottom;
	}

	public void setCalendarViewTaskOccurrence(
			CalendarViewTaskOccurrence calendarViewTaskOccurrence) {
		mCalendarViewTaskOccurrence = calendarViewTaskOccurrence;
		mTaskInformationStrings = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsMode;
		mTaskInformationStringsTextBoundsArray = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsModeTextBounds;
		mTaskInformationStringsTextBaselinesArray = calendarViewTaskOccurrence.TaskInformationStringsForTimeIntervalsModeTextBaselines;
		mBackgroundColor = calendarViewTaskOccurrence.BackgroundColor;
		mTextColor = calendarViewTaskOccurrence.TextColor;
		mTextPaint.setColor(mTextColor);
	}

	public CalendarViewTaskOccurrence getCalendarViewTaskOccurrence() {
		return mCalendarViewTaskOccurrence;
	}

	public void setCoords(int childViewLeft, int childViewTop, int childViewRight,
			int childViewBottom) {
		mLeft = childViewLeft;
		mTop = childViewTop;
		mRight = childViewRight;
		mBottom = childViewBottom;
	}

	public int getLeft() {
		return mLeft;
	}

	public int getTop() {
		return mTop;
	}

	public int getRight() {
		return mRight;
	}

	public int getBottom() {
		return mBottom;
	}
}
