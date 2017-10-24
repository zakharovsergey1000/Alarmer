package biz.advancedcalendar.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.accessories.TasksLayouter;
import biz.advancedcalendar.views.accessories.VerticalTasksLayouter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthDayView extends android.support.v7.widget.AppCompatButton /* implements OnClickListener */{
	private static final int MIN_TASK_HEIGHT_DIP = 3;// 25;
	private static final int WHOLE_DAY_TASKS_AREA_MIN_HEIGHT_DIP = MonthDayView.MIN_TASK_HEIGHT_DIP;// 3;
	private static final int WHOLE_DAY_TASKS_AREA_MAX_HEIGHT_DIP = 48;
	private static final int WHOLE_DAY_TASKS_AREA_DEFAULT_HEIGHT_PERCENT = 7;// 3;
	private static final int WHOLE_DAY_TASKS_AREA_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static final int WHOLE_DAY_TASKS_SPACER_DIP = 1;
	private static final int BACKGROUND_COLOR = 0xFFFFFFFF;
	private static final int BACKGROUND_COLOR_PALE = 0xFFDDDDDD;
	private static final int BACKGROUND_COLOR_SELECTED = 0xFF050505;
	private static final int DEFAULT_DAY_NUMBER_TEXT_COLOR = 0xFF808080;// 0xFFAAAAAA;
	private static final int WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_DIP = 0;
	private static final int WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_COLOR = 0xFF808080;// 0xFFAAAAAA;
	// private static final int DAY_NUMBER_TEXT_COLOR = 0xFF000000;
	// private static final int DAY_NUMBER_TEXT_COLOR_PALE = 0xFFAAAAAA;
	private static final float DAY_NUMBER_TEXT_SIZE_SP = 12.0f;
	private static final int DAY_WIDTH_DEFAULT_DIP = 100;
	private static final int DAY_WIDTH_DESIRABLE_MINIMUM_DIP = 70;
	private static final int DAY_HEIGHT_DEFAULT_DIP = 100;
	private static final int DAY_HEIGHT_DESIRABLE_MINIMUM_DIP = 70;
	private static final float TASK_TEXT_SIZE_SP = 12.0f;
	private static final int TASKS_SPACER_X_DIP = 1;
	private static final int TASKS_SPACER_Y_DIP = 1;
	private final int mWholeDayTasksSpacerPx;
	private final int mDayWidthPx;
	private final int mDayHeightPx;
	private final float mDayNumberTextSizePx;
	// private final float mDayNumberTextHeightPx;
	private final float mTaskTextSizePx;
	// private final float mTaskTextHeightPx;
	private final int mTasksSpacerXPx;
	private final int mTasksSpacerYPx;
	private final int mWholeDayTasksThisDayTasksSpacerPx;
	private final float mDensity;
	// private final LayoutParams mParams;
	private Paint mBackgroundPaint;
	private Paint mTaskPaint;
	private Paint mTextPaint;
	// private List<Task> mTasks;
	// private int mSuggestedViewWidthOnLastRequestOfDayHeaderCoords;
	// private final LinkedList<WeekViewCoreTaskWithCoords> mTaskWithCoordsCacheList;
	// private final ArrayList<CalendarViewTaskOccurrence>
	// mCalendarViewTaskOccurrences;
	private final TasksLayouter mLayouter;
	// private final Rect bounds;
	private int mWholeDayTasksHeightPx;
	private List<CalendarViewTaskOccurrence> mWholeDayCalendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
	private List<CalendarViewTaskOccurrence> mNotWholeDayCalendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
	// private List<WeekViewCoreTask> mWeekViewCoreTaskList;
	private final int[] mPeriodViewsTopBottomCoords;
	private final int minTaskHeightPx;
	private long mDayStartDateTime;
	private long mDayEndDateTime;
	private long mDayNumber;
	private DaysSelectedListener mDaysSelectedListener;
	private DayLongClickedListener mDayLongClickedListener;
	private int mBottomTaskDrawingSpacer;
	private int mRightTaskDrawingSpacer;
	private boolean mIsPale;
	private Rect mTextBounds;

	// private TaskOccurrencesDistribution mTaskOccurrencesDistribution;
	/** Constructor
	 *
	 * @param context */
	public MonthDayView(Context context) {
		this(context, null, 0);
	}

	/** Constructor
	 *
	 * @param context
	 * @param attrs */
	public MonthDayView(final Context context, final AttributeSet attrs) {
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
	public MonthDayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDensity = getResources().getDisplayMetrics().density;
		mDayNumberTextSizePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_SP, MonthDayView.DAY_NUMBER_TEXT_SIZE_SP,
				getResources().getDisplayMetrics());
		mTaskTextSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
				MonthDayView.TASK_TEXT_SIZE_SP, getResources().getDisplayMetrics());
		mDayWidthPx = (int) (mDensity * MonthDayView.DAY_WIDTH_DEFAULT_DIP + 0.5);
		mDayHeightPx = (int) (mDensity * MonthDayView.DAY_HEIGHT_DEFAULT_DIP + 0.5);
		minTaskHeightPx = Math.max(1,
				(int) (mDensity * MonthDayView.MIN_TASK_HEIGHT_DIP + 0.5));
		mWholeDayTasksSpacerPx = Math.max(1, (int) (mDensity
				* MonthDayView.WHOLE_DAY_TASKS_SPACER_DIP + 0.5));
		mTasksSpacerXPx = Math.max(1,
				(int) (mDensity * MonthDayView.TASKS_SPACER_X_DIP + 0.5));
		mTasksSpacerYPx = Math.max(1,
				(int) (mDensity * MonthDayView.TASKS_SPACER_Y_DIP + 0.5));
		mWholeDayTasksThisDayTasksSpacerPx = (int) (mDensity
				* MonthDayView.WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_DIP + 0.5);
		//
		// mCalendarViewTaskOccurrences = new ArrayList<CalendarViewTaskOccurrence>();
		mLayouter = new VerticalTasksLayouter();
		//
		mBackgroundPaint = new Paint();
		//
		mTaskPaint = new Paint();
		//
		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		mTextPaint.setTextSize(mDayNumberTextSizePx);
		mTextPaint.setTextSize(mTaskTextSizePx);
		// Rect bounds = new Rect();
		// mTextPaint.getTextBounds("0123456789", 0, 9, bounds);
		// mTextPaint.getTextBounds("Ag", 0, 2, bounds);
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDaysSelectedListener != null) {
					mDaysSelectedListener.onDaysSelected(mDayStartDateTime,
							mDayStartDateTime);
				}
			}
		});
		setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (mDayLongClickedListener != null) {
					v.setSelected(true);
					mDayLongClickedListener.onDayLongClicked(mDayStartDateTime,
							MonthDayView.this);
					return true;
				}
				return false;
			}
		});
		mPeriodViewsTopBottomCoords = new int[2];
		mTextBounds = new Rect();
	}

	public void setDaysSelectedListener(DaysSelectedListener daysSelectedListener) {
		mDaysSelectedListener = daysSelectedListener;
	}

	public void setDayLongClickedListener(DayLongClickedListener dayLongClickedListener) {
		mDayLongClickedListener = dayLongClickedListener;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = mDayWidthPx;
		int specSizeWidth = MeasureSpec.getSize(widthMeasureSpec);
		int specModeWidth = MeasureSpec.getMode(widthMeasureSpec);
		//
		int height = mDayHeightPx;
		int specSizeHeight = MeasureSpec.getSize(heightMeasureSpec);
		int specModeHeight = MeasureSpec.getMode(heightMeasureSpec);
		switch (specModeWidth) {
		case MeasureSpec.UNSPECIFIED:
			break;
		case MeasureSpec.AT_MOST:
			if (width > specSizeWidth) {
				width = specSizeWidth;
				int minDesirableWidth = (int) (mDensity
						* MonthDayView.DAY_WIDTH_DESIRABLE_MINIMUM_DIP + 0.5);
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
					* MonthDayView.DAY_WIDTH_DESIRABLE_MINIMUM_DIP + 0.5);
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
						* MonthDayView.DAY_HEIGHT_DESIRABLE_MINIMUM_DIP + 0.5);
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
					* MonthDayView.DAY_HEIGHT_DESIRABLE_MINIMUM_DIP + 0.5);
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
		// mCalendarViewTaskOccurrences.clear();
		mWholeDayTasksHeightPx = Math
				.min((int) (mDensity * MonthDayView.WHOLE_DAY_TASKS_AREA_MAX_HEIGHT_DIP + 0.5),
						Math.max(
								(int) ((bottom - top)
										/ 100.0f
										* MonthDayView.WHOLE_DAY_TASKS_AREA_DEFAULT_HEIGHT_PERCENT + 0.5),
								(int) (mDensity
										* MonthDayView.WHOLE_DAY_TASKS_AREA_MIN_HEIGHT_DIP + 0.5)));
		initCalendarViewTaskOccurrenceList();
	}

	private void initCalendarViewTaskOccurrenceList() {
		// if (mCalendarViewTaskOccurrences.size() == 0) {
		if (mNotWholeDayCalendarViewTaskOccurrences.size() > 0) {
			// Collections.copy(mCalendarViewTaskOccurrences,
			// mNotWholeDayCalendarViewTaskOccurrences);
			mPeriodViewsTopBottomCoords[0] = mWholeDayTasksHeightPx
					+ mWholeDayTasksThisDayTasksSpacerPx;
			mPeriodViewsTopBottomCoords[1] = getHeight() - mBottomTaskDrawingSpacer;
			//
			long[] timeIntervalDurations = new long[] {24 * 60 * 60 * 1000L};
			//
			mLayouter.calculateTasksCoords1(
			// final long startDateTime,
					mDayStartDateTime,
					// final List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences,
					mNotWholeDayCalendarViewTaskOccurrences,
					// final long[] timeIntervalDurations,
					timeIntervalDurations,
					// final int[] periodViewsTopBottomCoords,
					mPeriodViewsTopBottomCoords,
					// final int boundaryWidthPx,
					getWidth() - mRightTaskDrawingSpacer,
					// final int calendarViewTaskOccurrenceMinHeightPx,
					minTaskHeightPx,
					// final int spacerXPx,
					mTasksSpacerXPx,
					// final int spacerYPx
					mTasksSpacerYPx
			// mDayStartDateTime, mDayEndDateTime
			// - mDayStartDateTime, mNotWholeDayCalendarViewTaskOccurrences,
			// mPeriodViewsTopBottomCoords, getWidth(), minTaskHeightPx,
			// mTasksSpacerXPx, mTasksSpacerYPx
					);
		}
		// }
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// fill background
		mBackgroundPaint.setColor(MonthDayView.WHOLE_DAY_TASKS_AREA_COLOR);
		int width = getWidth();
		int height = getHeight();
		canvas.drawRect(0, 0, width, mWholeDayTasksHeightPx, mBackgroundPaint);
		mBackgroundPaint
				.setColor(MonthDayView.WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_COLOR);
		canvas.drawRect(0, mWholeDayTasksHeightPx, width, mWholeDayTasksHeightPx
				+ mWholeDayTasksThisDayTasksSpacerPx, mBackgroundPaint);
		if (isSelected()) {
			mBackgroundPaint.setColor(MonthDayView.BACKGROUND_COLOR_SELECTED);
		} else if (mIsPale) {
			mBackgroundPaint.setColor(MonthDayView.BACKGROUND_COLOR_PALE);
		} else {
			mBackgroundPaint.setColor(MonthDayView.BACKGROUND_COLOR);
		}
		canvas.drawRect(0, mWholeDayTasksHeightPx + mWholeDayTasksThisDayTasksSpacerPx,
				width, height, mBackgroundPaint);
		//
		long now = System.currentTimeMillis();
		String dayNumberText = "" + mDayNumber;
		mTextPaint.getTextBounds(dayNumberText, 0, dayNumberText.length(), mTextBounds);
		int textPosition = width - mTextBounds.width();
		if (mDayStartDateTime <= now && now < mDayEndDateTime) {
			int color = Helper.getIntegerPreferenceValue(
					getContext(),
					R.string.preference_key_calendar_today_date_text_color,
					getContext().getResources().getColor(
							R.color.calendar_today_date_text_color_default_value), null,
					null);
			mTextPaint.setColor(color);
			color = Helper.getIntegerPreferenceValue(
					getContext(),
					R.string.preference_key_calendar_today_date_highlight_color,
					getContext().getResources().getColor(
							R.color.calendar_today_date_highlight_color_default_value),
					null, null);
			mBackgroundPaint.setColor(color);
			// mTextBounds.left = 0;
			// mTextBounds.top = 0;
			canvas.drawRect(textPosition, height - mTextBounds.height(), textPosition
					+ mTextBounds.width(), height, mBackgroundPaint);
		} else {
			mTextPaint.setColor(MonthDayView.DEFAULT_DAY_NUMBER_TEXT_COLOR);
		}
		canvas.drawText(dayNumberText, textPosition, height, mTextPaint);
		//
		initCalendarViewTaskOccurrenceList();
		if (mWholeDayCalendarViewTaskOccurrences.size() > 0) {
			int left = 0;
			int top = 0;
			int right = 0;
			int bottom = mWholeDayTasksHeightPx;
			float taskWidth = (width - mWholeDayTasksSpacerPx
					* (mWholeDayCalendarViewTaskOccurrences.size() - 1))
					/ (float) mWholeDayCalendarViewTaskOccurrences.size();
			right = -mWholeDayTasksSpacerPx;
			for (int i = 0; i < mWholeDayCalendarViewTaskOccurrences.size(); i++) {
				CalendarViewTaskOccurrence calendarViewTaskOccurrence = mWholeDayCalendarViewTaskOccurrences
						.get(i);
				left = right + mWholeDayTasksSpacerPx;
				// if (i == mWholeDayWeekViewCoreTasks.size() - 1) {
				// right = getWidth();
				// } else {
				// right = (int) ((width + mWholeDayTasksSpacerPx) * (i + 1) + 0.5);
				// }
				right = (int) ((taskWidth + mWholeDayTasksSpacerPx) * (i + 1)
						- mWholeDayTasksSpacerPx + 0.5);
				mTaskPaint.setColor(calendarViewTaskOccurrence.Task
						.getColor2(getContext()));
				canvas.drawRect(left, top, right, bottom, mTaskPaint);
			}
		}
		//
		int size = mNotWholeDayCalendarViewTaskOccurrences.size();
		for (int j = 0; j < size; j++) {
			CalendarViewTaskOccurrence calendarViewTaskOccurrence = mNotWholeDayCalendarViewTaskOccurrences
					.get(j);
			int left = calendarViewTaskOccurrence.Left;
			int top = calendarViewTaskOccurrence.Top;
			int right = calendarViewTaskOccurrence.Right;
			int bottom = calendarViewTaskOccurrence.Bottom;
			int taskColor = calendarViewTaskOccurrence.Task.getColor2(getContext());
			mTaskPaint.setColor(taskColor);
			canvas.drawRect(left, top, right, bottom, mTaskPaint);
			// canvas.set
		}
	}

	// @Override
	// public void onClick(View v) {
	// }
	public void setDayStartDateTime(long dayStartDateTime) {
		mDayStartDateTime = dayStartDateTime;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dayStartDateTime);
		mDayNumber = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		mDayEndDateTime = calendar.getTimeInMillis();
	}

	public long getDayStartDateTime() {
		return mDayStartDateTime;
	}

	public void setPale(boolean isPale) {
		mIsPale = isPale;
	}

	public void setDayNumberTextSize(float size) {
		mTextPaint.setTextSize(size);
		mTextPaint.getTextBounds("MM" + mDayNumber, 0, 2, mTextBounds);
		mBottomTaskDrawingSpacer = mTextBounds.height();
		mBottomTaskDrawingSpacer = 0;
		mRightTaskDrawingSpacer = mTextBounds.width();
	}

	// public void setBottomTaskDrawingSpacer(int spacer) {
	// mBottomTaskDrawingSpacer = spacer;
	// }
	//
	// public void setRightTaskDrawingSpacer(int spacer) {
	// mRightTaskDrawingSpacer = spacer;
	// }
	// public void setWholeDayCalendarViewTaskOccurrences(
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences) {
	// mWholeDayCalendarViewTaskOccurrences = calendarViewTaskOccurrences;
	// }
	public List<CalendarViewTaskOccurrence> getWholeDayPreallocatedCalendarViewTaskOccurrences() {
		return mWholeDayCalendarViewTaskOccurrences;
	}

	// public void setNotWholeDayCalendarViewTaskOccurrences(
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences) {
	// mNotWholeDayCalendarViewTaskOccurrences = calendarViewTaskOccurrences;
	// // mCalendarViewTaskOccurrences.clear();
	// }
	public List<CalendarViewTaskOccurrence> getNotWholeDayPreallocatedCalendarViewTaskOccurrences() {
		return mNotWholeDayCalendarViewTaskOccurrences;
	}

	public void setTaskOccurrencesDistribution(
			TaskOccurrencesDistribution taskOccurrencesDistribution) {
		// mTaskOccurrencesDistribution = taskOccurrencesDistribution;
		mWholeDayCalendarViewTaskOccurrences = taskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList
				.get(0);
		mNotWholeDayCalendarViewTaskOccurrences.clear();
		List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = taskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList
				.get(0);
		int count = calendarViewTaskOccurrences.size();
		for (int i = 0; i < count; i++) {
			mNotWholeDayCalendarViewTaskOccurrences.add(calendarViewTaskOccurrences
					.get(i));
		}
		calendarViewTaskOccurrences = taskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList
				.get(0);
		count = calendarViewTaskOccurrences.size();
		for (int i = 0; i < count; i++) {
			mNotWholeDayCalendarViewTaskOccurrences.add(calendarViewTaskOccurrences
					.get(i));
		}
		calendarViewTaskOccurrences = taskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList
				.get(0);
		count = calendarViewTaskOccurrences.size();
		for (int i = 0; i < count; i++) {
			mNotWholeDayCalendarViewTaskOccurrences.add(calendarViewTaskOccurrences
					.get(i));
		}
		invalidate();
	}
}
