package biz.advancedcalendar.views;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.greendao.Task;

public class MonthView extends ViewGroup {
	public class DayHeader {
		TextView day;
		int left, top, right, bottom;

		public DayHeader(TextView day, int left, int top, int right, int bottom) {
			super();
			this.day = day;
			this.left = left;
			this.top = top;
			this.right = right;
			this.bottom = bottom;
		}
	}

	private static final int MAX_WEEK_HEADER_HEIGHT_DIP = 20;
	private static final int WEEK_HEADER_TEXT_SIZE_SP = 15;
	private static final int WEEK_HEADER_AREA_BACKGROUND_COLOR = 0xFFFFFFFF;
	// private static final int ALL_DAY_EVENTS_AREA_COLOR = 0xFFFFFFFF;
	// private static final int WEEK_HEADER_TEXT_COLOR = 0xFF11ED4B;
	private MonthViewCore mMonthViewCore;
	private final LayoutParams mWrapContentParams;
	// private final LayoutParams mMatchParentParams;
	private final Paint mPaint;
	private final Paint mTextPaint;
	private int mMaxWeekHeaderHeightPx;
	private int mWeekHeaderTextSizePx;
	private final float mDensity;
	private final Calendar mCalendar;
	private Rect mWeekHeaderRect;
	// private final Rect bounds;
	private SimpleDateFormat mDateFormat;

	// private List<DayHeader> mCachedDayHeaders;
	// private Configuration mConfig;
	/** Constructor
	 *
	 * @param context */
	public MonthView(Context context) {
		this(context, null, 0);
	}

	/** Constructor
	 *
	 * @param context
	 * @param attrs
	 * @param defStyle */
	public MonthView(final Context context, final AttributeSet attrs) {
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
	public MonthView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mMonthViewCore = new MonthViewCore(context);
		mWrapContentParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// mMatchParentParams = new LayoutParams(LayoutParams.MATCH_PARENT,
		// LayoutParams.MATCH_PARENT);
		mDensity = getResources().getDisplayMetrics().density;
		mMaxWeekHeaderHeightPx = (int) (mDensity * MonthView.MAX_WEEK_HEADER_HEIGHT_DIP + 0.5);
		mWeekHeaderTextSizePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, MonthView.WEEK_HEADER_TEXT_SIZE_SP,
				getResources().getDisplayMetrics());
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setTextSize(mWeekHeaderTextSizePx);
		mPaint = new Paint();
		mCalendar = Calendar.getInstance();
		mWeekHeaderRect = new Rect(0, 0, 0, mMaxWeekHeaderHeightPx);
		// bounds = new Rect();
		mDateFormat = new SimpleDateFormat("EEE", Locale.getDefault());
	}

	public void setDaysSelectedListener(DaysSelectedListener daysSelectedListener) {
		mMonthViewCore.setDaysSelectedListener(daysSelectedListener);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top,
			final int right, final int bottom) {
		removeAllViewsInLayout();
		int width = right - left;
		int height = bottom - top;
		int childViewLeft, childViewTop, childViewRight, childViewBottom;
		childViewLeft = 0;
		childViewTop = 0;
		childViewRight = width;
		// Draw the days of week
		TableLayout weekHeaderTableLayout = getDayHeaders();
		List<Integer> dayHeadersCoords = mMonthViewCore.getDayHeadersCoords(width);
		childViewLeft = dayHeadersCoords.get(0);
		childViewRight = dayHeadersCoords.get(dayHeadersCoords.size() - 1);
		int weekHeaderTableLayoutHeightPx;
		// // check how big weekHeaderTableLayout wants to be in height
		// weekHeaderTableLayout.measure(MeasureSpec.EXACTLY | childViewRight
		// - childViewLeft, MeasureSpec.UNSPECIFIED);
		// int weekHeaderTableLayoutMeasuredHeightPx = weekHeaderTableLayout
		// .getMeasuredHeight();
		// if (weekHeaderTableLayoutMeasuredHeightPx > mMaxWeekHeaderHeightPx) {
		// weekHeaderTableLayoutHeightPx = mMaxWeekHeaderHeightPx;
		// } else {
		// weekHeaderTableLayoutHeightPx = weekHeaderTableLayoutMeasuredHeightPx;
		// }
		//
		// weekHeaderTableLayout.measure(MeasureSpec.EXACTLY | childViewRight
		// - childViewLeft, MeasureSpec.EXACTLY | weekHeaderTableLayoutHeightPx);
		weekHeaderTableLayoutHeightPx = mMaxWeekHeaderHeightPx;
		weekHeaderTableLayout.measure(MeasureSpec.EXACTLY | childViewRight
				- childViewLeft, MeasureSpec.EXACTLY | weekHeaderTableLayoutHeightPx);
		addViewInLayout(weekHeaderTableLayout, -1, mWrapContentParams, true);
		weekHeaderTableLayout.layout(childViewLeft, childViewTop, childViewRight,
				weekHeaderTableLayoutHeightPx);
		childViewTop = weekHeaderTableLayoutHeightPx;
		mMaxWeekHeaderHeightPx = childViewTop;
		mWeekHeaderRect.right = right;
		mWeekHeaderRect.bottom = childViewTop;
		childViewLeft = 0;
		childViewRight = width;
		childViewBottom = height;
		if (childViewLeft > width) {
			childViewLeft = width;
		}
		if (childViewTop > height) {
			childViewTop = height;
		}
		addViewInLayout(mMonthViewCore, -1, mWrapContentParams, true);
		mMonthViewCore.measure(MeasureSpec.EXACTLY | childViewRight - childViewLeft,
				MeasureSpec.EXACTLY | childViewBottom - childViewTop);
		mMonthViewCore.layout(childViewLeft, childViewTop, childViewRight,
				childViewBottom);
		// int delta = childViewRight - childViewLeft
		// - dayHeadersCoords.get(dayHeadersCoords.size() - 1);
		childViewTop += mMonthViewCore.getTopBorderWidth();
		mWeekHeaderRect.right = right - left;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setColor(MonthView.WEEK_HEADER_AREA_BACKGROUND_COLOR);
		canvas.drawRect(mWeekHeaderRect, mPaint);
	}

	// @Override
	// protected void onConfigurationChanged(Configuration newConfig) {
	// int mask = newConfig.diff(mConfig);
	// if ((mask & ActivityInfo.CONFIG_FONT_SCALE) == ActivityInfo.CONFIG_FONT_SCALE) {
	// mWeekHeaderTextSizePx = (int) (TypedValue.applyDimension(
	// TypedValue.COMPLEX_UNIT_SP, MonthView.WEEK_HEADER_TEXT_SIZE_SP,
	// getResources().getDisplayMetrics()) * newConfig.fontScale);
	// mCachedDayHeaders = null;
	// }
	// if ((mask & ActivityInfo.CONFIG_LOCALE) == ActivityInfo.CONFIG_LOCALE) {
	// mDateFormat = new SimpleDateFormat("EEE", Locale.getDefault());
	// mCachedDayHeaders = null;
	// }
	// invalidate();
	// }
	// public void setCalendarViewTaskOccurrences(List<CalendarViewTaskOccurrence> tasks)
	// {
	// mMonthViewCore.setCalendarViewTaskOccurrences(tasks);
	// requestLayout();
	// }
	public void setTasks(List<Task> tasks) {
		mMonthViewCore.setTasks(tasks);
		requestLayout();
	}

	public void setStartMonth(long startMonth) {
		mMonthViewCore.setStartMonth(startMonth);
	}

	public long getStartMonth() {
		return mMonthViewCore.getStartMonth();
	}

	private TableLayout getDayHeaders() {
		TableLayout dayHeadersTableLayout = (TableLayout) LayoutInflater.from(
				getContext()).inflate(R.layout.week_view_days_header, null);
		dayHeadersTableLayout.setStretchAllColumns(true);
		return fillDayHeadersTableLayout(dayHeadersTableLayout);
	}

	private TableLayout fillDayHeadersTableLayout(TableLayout dayHeadersTableLayout) {
		dayHeadersTableLayout.removeAllViews();
		mCalendar.setTimeInMillis(mMonthViewCore.getMonthSquareStartDateTime());
		mCalendar.add(Calendar.DAY_OF_YEAR, -1);
		Button date;
		TableRow.LayoutParams lp = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		lp.weight = 1;
		TableRow week = new TableRow(getContext());
		// this loop is used to fill out the days in the i-th row in the calendar
		for (int j = 0; j < 7; j++) {
			mCalendar.add(Calendar.DAY_OF_YEAR, 1);
			// TODO optimize this code. Preallocate week day headers array
			Date d = new Date(mCalendar.getTimeInMillis());
			// formatting date in Java using SimpleDateFormat
			String day = mDateFormat.format(d);
			date = (Button) LayoutInflater.from(getContext()).inflate(
					R.layout.week_view_days_header_button, null);
			// DateTime dateTimeNow = DateTime.now();
			// DateTime dateTimeButton = new DateTime(mCalendar.getTimeInMillis());
			// if (!isInEditMode()) {
			// if (dateTimeNow.withTimeAtStartOfDay().isEqual(
			// dateTimeButton.withTimeAtStartOfDay())) {
			// date.setTextColor(getContext().getResources().getColor(
			// R.color.calendar_today_date_text_color));
			// date.setBackgroundColor(getContext().getResources().getColor(
			// R.color.week_view_days_header_button_today_background_color));
			// // tv.setTextColor(0xFF000000);
			// }
			// }
			date.setPadding(0, 0, 0, 0);
			date.setLayoutParams(lp);
			date.setGravity(Gravity.CENTER);
			date.setTextSize(TypedValue.COMPLEX_UNIT_SP,
					MonthView.WEEK_HEADER_TEXT_SIZE_SP);
			// date.setTextColor(WeekView.WEEK_HEADER_TEXT_COLOR);
			// date.setTypeface(null, Typeface.BOLD);
			date.setText(day);
			week.addView(date);
		}
		dayHeadersTableLayout.addView(week);
		return dayHeadersTableLayout;
	}

	public void postRequestLayout() {
		// requestLayout();
		// forceLayout();
		post(new Runnable() {
			@Override
			public void run() {
				requestLayout();
			}
		});
	}

	public static int getTextHeight(String text, float textSize, Typeface typeface) {
		TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
		paint.setTextSize(textSize);
		paint.setTypeface(typeface);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		return bounds.height();
	}
}
