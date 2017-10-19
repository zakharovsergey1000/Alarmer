package biz.advancedcalendar.fragments;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewWeek2.CalculateAndDistributeTaskOccurrencesAsyncTaskArgument;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.DayViewCore;
import biz.advancedcalendar.views.TaskClickedListener;
import biz.advancedcalendar.views.TaskOccurrencesDistribution;
import biz.advancedcalendar.views.TaskOccurrencesDistributionState;
import biz.advancedcalendar.views.TextWeekTaskView;
import biz.advancedcalendar.views.TimeIntervalViewsPositions;
import biz.advancedcalendar.views.WeekTaskView;
import biz.advancedcalendar.views.WeekViewDayHeaderClickedListener;
import biz.advancedcalendar.views.WholeDayWeekTaskView;
import biz.advancedcalendar.views.accessories.TasksLayouter;
import biz.advancedcalendar.views.accessories.VerticalTasksLayouter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

public class FragmentViewWeek2Adapter extends
		RecyclerView.Adapter<FragmentViewWeek2Adapter.DayViewHolder> {
	private final FragmentViewWeek2 mFragmentViewWeek2;

	public class DayViewHolder extends RecyclerView.ViewHolder {
		Button mDayHeaderButton;
		View mSpacerView;
		DayViewCore mDayViewCore;
		ViewGroup mParent;
		WeekViewDayHeaderClickedListener mWeekViewDayHeaderClickedListener;
		public Calendar Calendar1;

		DayViewHolder(View itemView, ViewGroup parent,
				WeekViewDayHeaderClickedListener weekViewDayHeaderClickedListener) {
			super(itemView);
			mDayHeaderButton = (Button) itemView.findViewById(R.id.button1);
			if (mDaysCount == 1) {
				mDayHeaderButton.setVisibility(View.GONE);
			} else {
				mDayHeaderButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mWeekViewDayHeaderClickedListener != null) {
							mWeekViewDayHeaderClickedListener
									.onDayHeaderClicked(mDayViewCore
											.getDayStartDateTime());
						}
					}
				});
			}
			mSpacerView = itemView.findViewById(R.id.spacer);
			mDayViewCore = (DayViewCore) itemView.findViewById(R.id.weekdayview);
			mParent = parent;
			mWeekViewDayHeaderClickedListener = weekViewDayHeaderClickedListener;
			Calendar1 = Calendar.getInstance();
		}

		public Integer getWeekViewCoreTop() {
			return mSpacerView.getTop();
		}

		public Integer getWeekViewCoreHoursDrawingTop() {
			return mDayViewCore.getTop();
		}
	}

	private List<Task> mTasks;
	public static final int VIRTUAL_MIDDLE_OFFSET = Integer.MAX_VALUE / 2;
	public static final String FragmentViewWeek2Debug = "FragmentViewWeek2Debug";
	public static final String FragmentViewWeek2Debug2 = "FragmentViewWeek2Debug2";
	public static final String FragmentViewWeek2Debug5 = "FragmentViewWeek2Debug5";
	List<TaskOccurrencesDistribution> mCacheOfTaskOccurrencesDistributions = new ArrayList<TaskOccurrencesDistribution>();
	private TaskOccurrencesDistributionState mTaskOccurrencesDistributionState;
	private static final int VISIBLE_THRESHOLD = 14;
	protected int visibleThreshold;
	protected int startPurgeThreshold = visibleThreshold * 4;
	private RetainedFragmentForFragmentViewWeek2 mRetainedFragment;
	private WeekViewDayHeaderClickedListener mWeekViewDayHeaderClickedListener;
	// private Object mLoadGroupAsyncTask;
	private LayoutInflater mLayoutInflater;
	private String[] mWeekDaysHeaders;
	private ColorStateList mDefaultTextColorStateList;
	private Drawable mDefaultBackground;
	//
	private static final int MIN_TASK_HEIGHT_DIP = 48;
	// private static final int WHOLE_DAY_TASKS_HEIGHT_DIP =
	// WeekViewCore.MIN_TASK_HEIGHT_DIP;
	private static final int PRE_BUSINESS_HOURS_TASKS_HEIGHT_DIP = FragmentViewWeek2Adapter.MIN_TASK_HEIGHT_DIP;
	private static final int POST_BUSINESS_HOURS_TASKS_HEIGHT_DIP = FragmentViewWeek2Adapter.MIN_TASK_HEIGHT_DIP;
	private static final int WHOLE_DAY_TASKS_SPACER_DIP = 2;
	private static final int WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_DIP = 3;
	// private static final int HOUR_MARKER_LINE_WIDTH_DIP = 4;
	// private static final int HOUR_HEADER_TEXT_SIZE_SP = 15;
	// private static final int WEEKS_SPACER_COLOR = 0xFFAAAAAA;
	private static final int HOURS_SPACER_DIP = 1;
	// private static final float TASK_TEXT_SIZE_SP = 15.0f;// 20.0f;// 12.0f;
	private static final int RIGHT_HEADER_AND_DAYS_SPACER_DIP = 1;
	private static final int TASKS_SPACER_X_DIP = 1;
	private static final int TASKS_SPACER_Y_DIP = 1;
	// private static final int TOP_BORDER_SPACER_DIP = 0;
	// private static final int BOTTOM_BORDER_SPACER_DIP = 0;
	private static final int HOURS_NUMBERS_COLOR = 0xFF808080;// 0xFFAAAAAA;
	// private final String[] mHours = new String[] {"00", "01", "02", "03", "04", "05",
	// "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18",
	// "19", "20", "21", "22", "23",};
	private TaskClickedListener mTaskClickedListener;
	private final TasksLayouter mLayouter;
	private final float mDensity;
	private final int mTasksSpacerXPx;
	private final int mTasksSpacerYPx;
	private final int mHoursSpacerPx;
	private final int mRightHeaderAndDaysSpacerPx;
	private InformationUnitMatrix informationUnitMatrixForTimeIntervalsMode;
	private InformationUnitMatrix informationUnitMatrixForTextMode;
	private int mDaysCount, mBusinessHoursStartTime, mBusinessHoursEndTime;
	private int mWholeDayTasksThisDayTasksSpacerPx;
	private final int mMinTaskHeightPx;
	private final int mPreBusinessHoursTasksHeightPx;
	private final int mPostBusinessHoursTasksHeightPx;
	private int mWholeDayTasksHeightPx;
	private final int mWholeDayTasksSpacerPx;
	private final Rect mTextBounds;
	private TimeIntervalViewsPositions mTimeIntervalViewsPositions;
	private List<Integer> mIntegersList;
	private final DateFormat mTimeFormatter;
	private int mMode = CommonConstants.CALENDAR_MODE_TIME_INTERVALS;
	private Paint mPaint;
	private TextPaint mTextPaint;
	private float mTextSize;

	// public void setLoadGroupAsyncTask(Object loadGroupAsyncTask) {
	// if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
	// Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
	// "setLoadGroupAsyncTask thread: %s", Thread.currentThread().getName()));
	// Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
	// "    mLoadGroupAsyncTask before: %s", mLoadGroupAsyncTask));
	// }
	// mLoadGroupAsyncTask = loadGroupAsyncTask;
	// if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
	// Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
	// "    mLoadGroupAsyncTask after: %s", mLoadGroupAsyncTask));
	// }
	// }
	@SuppressLint("InflateParams")
	public FragmentViewWeek2Adapter(FragmentViewWeek2 fragmentViewWeek2,
			RetainedFragmentForFragmentViewWeek2 retainedFragmentForFragmentViewWeek2,
			TaskClickedListener taskClickedListener,
			WeekViewDayHeaderClickedListener weekViewDayHeaderClickedListener) {
		mFragmentViewWeek2 = fragmentViewWeek2;
		mRetainedFragment = retainedFragmentForFragmentViewWeek2;
		mTaskClickedListener = taskClickedListener;
		mWeekViewDayHeaderClickedListener = weekViewDayHeaderClickedListener;
		mDaysCount = fragmentViewWeek2.mStateParameters.DaysCount;
		visibleThreshold = FragmentViewWeek2Adapter.VISIBLE_THRESHOLD;
		mLayoutInflater = mFragmentViewWeek2.getActivity().getLayoutInflater();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE",
				Locale.getDefault());
		mWeekDaysHeaders = new String[7];
		Calendar calendar = Calendar.getInstance();
		CalendarHelper.toBeginningOfWeek(calendar, 1);
		Date d = new Date();
		for (int i = 0; i < 7; i++) {
			d.setTime(calendar.getTimeInMillis());
			mWeekDaysHeaders[i] = simpleDateFormat.format(d);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		LinearLayout linearLayout = (LinearLayout) mLayoutInflater.inflate(
				R.layout.week_day_view, null);
		Button dayHeaderButton = (Button) linearLayout.findViewById(R.id.button1);
		mDefaultTextColorStateList = dayHeaderButton.getTextColors();
		mDefaultBackground = dayHeaderButton.getBackground();
		mTaskOccurrencesDistributionState = new TaskOccurrencesDistributionState();
		//
		final Resources resources = mFragmentViewWeek2.getActivity().getResources();
		mDensity = resources.getDisplayMetrics().density;
		mWholeDayTasksThisDayTasksSpacerPx = Math.max(1, Math.round(mDensity
				* FragmentViewWeek2Adapter.WHOLE_DAY_TASKS_THIS_DAY_TASKS_SPACER_DIP));
		mWholeDayTasksSpacerPx = Math.max(1, (int) (mDensity
				* FragmentViewWeek2Adapter.WHOLE_DAY_TASKS_SPACER_DIP + 0.5));
		mTasksSpacerXPx = Math.max(1,
				Math.round(mDensity * FragmentViewWeek2Adapter.TASKS_SPACER_X_DIP));
		mTasksSpacerYPx = Math.max(1,
				Math.round(mDensity * FragmentViewWeek2Adapter.TASKS_SPACER_Y_DIP));
		mHoursSpacerPx = Math.max(1,
				Math.round(mDensity * FragmentViewWeek2Adapter.HOURS_SPACER_DIP));
		mRightHeaderAndDaysSpacerPx = Math.max(
				1,
				Math.round(mDensity
						* FragmentViewWeek2Adapter.RIGHT_HEADER_AND_DAYS_SPACER_DIP));
		mPaint = new Paint();
		mPaint.setColor(0xFF808080);
		mPaint.setStrokeWidth(mHoursSpacerPx);
		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.density = resources.getDisplayMetrics().density;
		mTextPaint.setColor(FragmentViewWeek2Adapter.HOURS_NUMBERS_COLOR);
		mTextPaint.setTextAlign(Paint.Align.RIGHT);
		mTextBounds = new Rect();
		mMinTaskHeightPx = Math.max(mTextBounds.bottom - mTextBounds.top,
				Math.round(mDensity * FragmentViewWeek2Adapter.MIN_TASK_HEIGHT_DIP));
		mPreBusinessHoursTasksHeightPx = Math.max(
				mTextBounds.bottom - mTextBounds.top,
				Math.round(mDensity
						* FragmentViewWeek2Adapter.PRE_BUSINESS_HOURS_TASKS_HEIGHT_DIP));
		mPostBusinessHoursTasksHeightPx = Math.max(
				mTextBounds.bottom - mTextBounds.top,
				Math.round(mDensity
						* FragmentViewWeek2Adapter.POST_BUSINESS_HOURS_TASKS_HEIGHT_DIP));
		mWholeDayTasksHeightPx = mMinTaskHeightPx;
		mTextPaint.getTextBounds("0123456789", 0, 10, mTextBounds);
		mTextSize = fragmentViewWeek2.mStateParameters.TextSize;
		mLayouter = new VerticalTasksLayouter();
		mBusinessHoursStartTime = fragmentViewWeek2.mStateParameters.BusinessHoursStartTime;
		mBusinessHoursEndTime = fragmentViewWeek2.mStateParameters.BusinessHoursEndTime;
		mIntegersList = new ArrayList<Integer>();
		mTimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);
	}

	public synchronized void invalidateTasks() {
		mTasks = null;
		mRetainedFragment.invalidateTasks();
		notifyDataSetChanged();
	}

	public synchronized void invalidateTaskOccurrencesDistributions() {
		mRetainedFragment.invalidateTaskOccurrencesDistributions();
		notifyDataSetChanged();
	}

	public TaskOccurrencesDistributionState calculateReconciledTaskOccurrencesDistributionState() {
		TaskOccurrencesDistributionState reconciledTaskOccurrencesDistributionState = new TaskOccurrencesDistributionState();
		int firstVisibleItemPosition = mFragmentViewWeek2.mLinearLayoutManager
				.findFirstVisibleItemPosition();
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
					"calculateReconciledTaskOccurrencesDistributionState."
							+ "\t firstVisibleItemPosition: " + firstVisibleItemPosition);
		}
		if (firstVisibleItemPosition == -1) {
			if (FragmentViewWeek2.DEBUG) {
				Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
						"calculateReconciledTaskOccurrencesDistributionState."
								+ "\t return: "
								+ reconciledTaskOccurrencesDistributionState);
			}
			return reconciledTaskOccurrencesDistributionState;
		}
		int lastVisibleItemPosition = mFragmentViewWeek2.mLinearLayoutManager
				.findLastVisibleItemPosition();
		//
		// Reconcile TaskOccurrencesDistributionState from all fragments into the
		// taskOccurrencesDistributionState
		for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition
				&& !(reconciledTaskOccurrencesDistributionState.AllDayTasksExist
						&& reconciledTaskOccurrencesDistributionState.PreBusinessHoursTasksExist && reconciledTaskOccurrencesDistributionState.PostBusinessHoursTasksExist); i++) {
			TaskOccurrencesDistribution taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
					.get(i);
			if (taskOccurrencesDistribution == null) {
				continue;
			}
			if (taskOccurrencesDistribution.isAllDayTasksExist()) {
				reconciledTaskOccurrencesDistributionState.AllDayTasksExist = true;
			}
			if (taskOccurrencesDistribution.isPreBusinessHoursTasksExist()) {
				reconciledTaskOccurrencesDistributionState.PreBusinessHoursTasksExist = true;
			}
			if (taskOccurrencesDistribution.isBusinessHoursTasksExist()) {
				reconciledTaskOccurrencesDistributionState.BusinessHoursTasksExist = true;
			}
			if (taskOccurrencesDistribution.isPostBusinessHoursTasksExist()) {
				reconciledTaskOccurrencesDistributionState.PostBusinessHoursTasksExist = true;
			}
		}
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
					"calculateReconciledTaskOccurrencesDistributionState."
							+ "\t return: " + reconciledTaskOccurrencesDistributionState);
		}
		return reconciledTaskOccurrencesDistributionState;
	}

	public int getItemPositionForDateTime(long dateTime) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1
				.setTimeInMillis(mFragmentViewWeek2.mStateParameters.DayBeginningOnVirtualMiddleOffset);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTimeInMillis(dateTime);
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.MINUTE, 0);
		calendar2.set(Calendar.SECOND, 0);
		calendar2.set(Calendar.MILLISECOND, 0);
		return FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET
				+ CalendarHelper.daysBetween(calendar1, calendar2);
	}

	@Override
	public DayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		long nanoTime = System.nanoTime();
		LinearLayout linearLayout = (LinearLayout) mLayoutInflater.inflate(
				R.layout.week_day_view, parent, false);
		DayViewHolder holder = new DayViewHolder(linearLayout, parent,
				mWeekViewDayHeaderClickedListener);
		ArrayList<WholeDayWeekTaskView> wholeDayTaskOccurrencesList = new ArrayList<WholeDayWeekTaskView>();
		ArrayList<WholeDayWeekTaskView> taskOccurrencesTouchingPreBusinessHoursList = new ArrayList<WholeDayWeekTaskView>();
		ArrayList<WeekTaskView> taskOccurrencesTouchingBusinessHoursList = new ArrayList<WeekTaskView>();
		ArrayList<WholeDayWeekTaskView> taskOccurrencesTouchingPostBusinessHoursList = new ArrayList<WholeDayWeekTaskView>();
		ArrayList<TextWeekTaskView> mTextModeViews = new ArrayList<TextWeekTaskView>();
		holder.mDayViewCore.setPermanentParameters(mFragmentViewWeek2,
				mWholeDayTasksThisDayTasksSpacerPx, mWholeDayTasksSpacerPx,
				mMinTaskHeightPx, mTasksSpacerXPx, mTasksSpacerYPx, mHoursSpacerPx,
				mRightHeaderAndDaysSpacerPx, mPaint, mTextPaint, mTextSize,
				mPreBusinessHoursTasksHeightPx, mPostBusinessHoursTasksHeightPx,
				mWholeDayTasksHeightPx, mLayouter, mTaskClickedListener, mIntegersList,
				mTimeFormatter, wholeDayTaskOccurrencesList,
				taskOccurrencesTouchingPreBusinessHoursList,
				taskOccurrencesTouchingBusinessHoursList,
				taskOccurrencesTouchingPostBusinessHoursList, mTextModeViews);
		long nanoTime2 = System.nanoTime();
		long delta = (nanoTime2 - nanoTime) / 1000000;
		if (FragmentViewWeek2.DEBUG5) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug5, "onCreateViewHolder."
					+ "\t time delta: " + delta);
		}
		return holder;
	}

	@Override
	public void onBindViewHolder(DayViewHolder holder, int position) {
		long nanoTime = System.nanoTime();
		// setup header
		Calendar calendar = holder.Calendar1;
		calendar.setTimeInMillis(mFragmentViewWeek2.mStateParameters.DayBeginningOnVirtualMiddleOffset);
		calendar.add(Calendar.DAY_OF_YEAR, position
				- FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
		long dayStartDateTime = calendar.getTimeInMillis();
		String day = mWeekDaysHeaders[calendar.get(Calendar.DAY_OF_WEEK) - 1] + "\n"
				+ calendar.get(Calendar.DATE);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		long dayEndDateTime = calendar.getTimeInMillis();
		long[] dayBorders = new long[] {dayStartDateTime, dayEndDateTime};
		calendar.setTimeInMillis(System.currentTimeMillis());
		CalendarHelper.toBeginningOfDay(calendar);
		if (calendar.getTimeInMillis() == dayStartDateTime) {
			int color = Helper
					.getIntegerPreferenceValue(
							mFragmentViewWeek2.getContext(),
							R.string.preference_key_calendar_today_date_text_color,
							mFragmentViewWeek2
									.getContext()
									.getResources()
									.getInteger(
											R.color.calendar_today_date_text_color_default_value),
							null, null);
			holder.mDayHeaderButton.setTextColor(color);
			color = Helper
					.getIntegerPreferenceValue(
							mFragmentViewWeek2.getContext(),
							R.string.preference_key_calendar_today_date_highlight_color,
							mFragmentViewWeek2
									.getContext()
									.getResources()
									.getInteger(
											R.color.calendar_today_date_highlight_color_default_value),
							null, null);
			holder.mDayHeaderButton.setBackgroundColor(color);
		} else {
			holder.mDayHeaderButton.setTextColor(mDefaultTextColorStateList);
			if (android.os.Build.VERSION.SDK_INT >= 16) {
				holder.mDayHeaderButton.setBackground(mDefaultBackground
						.getConstantState().newDrawable());
			} else {
				holder.mDayHeaderButton.setBackgroundDrawable(mDefaultBackground
						.getConstantState().newDrawable());
			}
		}
		holder.mDayHeaderButton.setPadding(0, 0, 0, 0);
		holder.mDayHeaderButton.setText(day);
		TaskOccurrencesDistribution taskOccurrencesDistribution;
		taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
				.get(position);
		if (taskOccurrencesDistribution == null
				&& !mRetainedFragment.isAsyncTaskRunning()) {
			Calendar calendar3 = holder.Calendar1;
			calendar3
					.setTimeInMillis(mFragmentViewWeek2.mStateParameters.DayBeginningOnVirtualMiddleOffset);
			CalendarHelper.toBeginningOfDay(calendar3);
			calendar3.add(Calendar.DAY_OF_YEAR, position
					- FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET
					- mFragmentViewWeek2.mStateParameters.DaysCount);
			dayStartDateTime = calendar3.getTimeInMillis();
			long dayStartDateTime1 = dayStartDateTime;
			ArrayList<Long> daysBordersList = new ArrayList<Long>();
			ArrayList<Integer> positionsList = new ArrayList<Integer>();
			for (int i = position - mFragmentViewWeek2.mStateParameters.DaysCount; i <= position
					+ mFragmentViewWeek2.mStateParameters.DaysCount; i++) {
				Calendar calendar4 = holder.Calendar1;
				calendar4.setTimeInMillis(dayStartDateTime1);
				calendar4.add(Calendar.DAY_OF_YEAR, 1);
				dayEndDateTime = calendar4.getTimeInMillis();
				TaskOccurrencesDistribution taskOccurrencesDistribution1 = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
						.get(i);
				if (taskOccurrencesDistribution1 == null) {
					daysBordersList.add(dayStartDateTime1);
					daysBordersList.add(dayEndDateTime);
					positionsList.add(i);
				}
				dayStartDateTime1 = dayEndDateTime;
			}
			long[] daysBorders = new long[daysBordersList.size()];
			for (int i1 = 0; i1 < daysBorders.length; i1++) {
				daysBorders[i1] = daysBordersList.get(i1);
			}
			int[] positions = new int[positionsList.size()];
			for (int i1 = 0; i1 < positions.length; i1++) {
				positions[i1] = positionsList.get(i1);
			}
			mRetainedFragment
					.launchAsyncTask(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] {new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
							positions, daysBorders, Helper
									.getFirstDayOfWeek(mFragmentViewWeek2.getContext()),
							getTasks(), mFragmentViewWeek2.mStateParameters)});
		}
		calendar.setTimeInMillis(dayBorders[0]);
		holder.mDayViewCore.setDynamicParameters(
				mTimeIntervalViewsPositions,
				mTaskOccurrencesDistributionState,
				taskOccurrencesDistribution,
				dayBorders,
				mBusinessHoursStartTime,
				mBusinessHoursEndTime,
				mFragmentViewWeek2.mStateParameters.Mode,
				CalendarHelper.isLastDayOfWeek(calendar,
						Helper.getFirstDayOfWeek(mFragmentViewWeek2.getContext())));
		long nanoTime2 = System.nanoTime();
		long delta = (nanoTime2 - nanoTime) / 1000000;
		if (FragmentViewWeek2.DEBUG5) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug5, "onBindViewHolder."
					+ "\t time delta: " + delta);
		}
	}

	TaskOccurrencesDistribution getInstanceOfTaskOccurrencesDistribution(int position) {
		TaskOccurrencesDistribution taskOccurrencesDistribution;
		if (mCacheOfTaskOccurrencesDistributions.size() > 0) {
			taskOccurrencesDistribution = mCacheOfTaskOccurrencesDistributions
					.remove(mCacheOfTaskOccurrencesDistributions.size() - 1);
			taskOccurrencesDistribution.init();
		} else {
			taskOccurrencesDistribution = new TaskOccurrencesDistribution(1);
		}
		return taskOccurrencesDistribution;
		//
		// TaskOccurrencesDistribution taskOccurrencesDistribution;
		// if (mCachedCalculatedTaskOccurrencesDistributions.containsKey(position)) {
		// taskOccurrencesDistribution = mCachedCalculatedTaskOccurrencesDistributions
		// .get(position);
		// taskOccurrencesDistribution.init();
		// } else if (mCachedCalculatedTaskOccurrencesDistributions.size() <
		// startPurgeThreshold) {
		// taskOccurrencesDistribution = new TaskOccurrencesDistribution(1);
		// mCachedCalculatedTaskOccurrencesDistributions.put(position,
		// taskOccurrencesDistribution);
		// } else {
		// int firstKey = mCachedCalculatedTaskOccurrencesDistributions.firstKey();
		// int lastKey = mCachedCalculatedTaskOccurrencesDistributions.lastKey();
		// if (position < firstKey || position - firstKey < lastKey - position) {
		// taskOccurrencesDistribution = mCachedCalculatedTaskOccurrencesDistributions
		// .remove(lastKey);
		// mCachedCalculatedTaskOccurrencesDistributions.put(position,
		// taskOccurrencesDistribution);
		// taskOccurrencesDistribution.init();
		// } else {
		// taskOccurrencesDistribution = mCachedCalculatedTaskOccurrencesDistributions
		// .remove(firstKey);
		// mCachedCalculatedTaskOccurrencesDistributions.put(position,
		// taskOccurrencesDistribution);
		// taskOccurrencesDistribution.init();
		// }
		// }
		// return taskOccurrencesDistribution;
	}

	void purgeTaskOccurrencesDistributionsIfNeeded(
			int firstVisibleItemPosition,
			int lastVisibleItemPosition,
			TreeMap<Integer, TaskOccurrencesDistribution> cachedCalculatedTaskOccurrencesDistributions,
			int visibleThreshold, int startPurgeThreshold,
			List<TaskOccurrencesDistribution> mCacheOfTaskOccurrencesDistributions) {
		if (firstVisibleItemPosition == -1 || lastVisibleItemPosition == -1) {
			return;
		}
		final int visibleItemsCount = lastVisibleItemPosition - firstVisibleItemPosition
				+ 1;
		int mTaskOccurrencesDistributionsSizeBeforePurging = cachedCalculatedTaskOccurrencesDistributions
				.size();
		if (visibleItemsCount > 0
				&& cachedCalculatedTaskOccurrencesDistributions.size()
						- visibleItemsCount > startPurgeThreshold) {
			int countBefore = 0;
			int countAfter = 0;
			Iterator<Integer> it = cachedCalculatedTaskOccurrencesDistributions.keySet()
					.iterator();
			while (it.hasNext()) {
				int virtualGroup = it.next();
				if (virtualGroup < firstVisibleItemPosition) {
					countBefore++;
				} else if (virtualGroup > lastVisibleItemPosition) {
					countAfter++;
				}
			}
			//
			if (countBefore > visibleThreshold) {
				it = cachedCalculatedTaskOccurrencesDistributions.keySet().iterator();
				while (it.hasNext() && countBefore > visibleThreshold
						&& countBefore + countAfter > visibleThreshold * 2) {
					int key = it.next();
					mCacheOfTaskOccurrencesDistributions
							.add(cachedCalculatedTaskOccurrencesDistributions.get(key));
					it.remove();
					countBefore--;
				}
			}
			//
			it = cachedCalculatedTaskOccurrencesDistributions.keySet().iterator();
			for (int i = 0; it.hasNext() && i < visibleThreshold * 2 + visibleItemsCount; i++) {
				it.next();
			}
			while (it.hasNext()) {
				int key = it.next();
				mCacheOfTaskOccurrencesDistributions
						.add(cachedCalculatedTaskOccurrencesDistributions.get(key));
				it.remove();
			}
			if (FragmentViewWeek2.DEBUG) {
				Log.d("FragmentViewWeek2Debug", "purging executed "
						+ mTaskOccurrencesDistributionsSizeBeforePurging + " "
						+ firstVisibleItemPosition + " " + lastVisibleItemPosition + " "
						+ cachedCalculatedTaskOccurrencesDistributions.keySet());
			}
		}
	}

	@Override
	public int getItemCount() {
		// Show virtually infinite number of items.
		return Integer.MAX_VALUE;
	}

	public void setMode(int mode) {
		if (mMode != mode) {
			mMode = mode;
			notifyDataSetChanged();
		}
	}

	public void setInformationUnitMatrices(
			InformationUnitMatrix informationUnitMatrixForTimeIntervalsMode,
			InformationUnitMatrix informationUnitMatrixForTextMode) {
		if (this.informationUnitMatrixForTimeIntervalsMode == null
				|| !this.informationUnitMatrixForTimeIntervalsMode
						.equals(informationUnitMatrixForTimeIntervalsMode)) {
			this.informationUnitMatrixForTimeIntervalsMode = informationUnitMatrixForTimeIntervalsMode;
			notifyDataSetChanged();
		}
		if (this.informationUnitMatrixForTextMode == null
				|| !this.informationUnitMatrixForTextMode
						.equals(informationUnitMatrixForTextMode)) {
			this.informationUnitMatrixForTextMode = informationUnitMatrixForTextMode;
			notifyDataSetChanged();
		}
	}

	public void setTaskOccurrencesDistributionState(
			TaskOccurrencesDistributionState taskOccurrencesDistributionState, int height) {
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
					"setAssignedTaskOccurrencesDistributionState"
							+ "\t	taskOccurrencesDistributionState"
							+ taskOccurrencesDistributionState.toString());
		}
		mTaskOccurrencesDistributionState = taskOccurrencesDistributionState;
		mTimeIntervalViewsPositions = calculateTimeIntervalViewsPositions(height);
		notifyDataSetChanged();
	}

	public TaskOccurrencesDistributionState getTaskOccurrencesDistributionState() {
		// if (mAssignedTaskOccurrencesDistributionState == null) {
		// mAssignedTaskOccurrencesDistributionState =
		// calculateReconciledTaskOccurrencesDistributionState();
		// }
		return mTaskOccurrencesDistributionState;
	}

	private TimeIntervalViewsPositions calculateTimeIntervalViewsPositions(int height) {
		long nanoTime = System.nanoTime();
		TimeIntervalViewsPositions timeIntervalViewsPositions = new TimeIntervalViewsPositions();
		//
		if (mTaskOccurrencesDistributionState.PreBusinessHoursTasksExist) {
			timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords = new int[2];
			//
			timeIntervalViewsPositions.PreBusinessHoursTimeIntervalCoords[0] = 0 + (mTaskOccurrencesDistributionState.AllDayTasksExist ? mWholeDayTasksHeightPx
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
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[1] = height;
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords[0] = height
					- mPreBusinessHoursTasksHeightPx;
		} else {
			timeIntervalViewsPositions.PostBusinessHoursTimeIntervalCoords = null;
		}
		//
		int mBusinessHoursStartTimeHourRemainder = mFragmentViewWeek2.mStateParameters.BusinessHoursStartTime % 3600000;
		int mBusinessHoursEndTimeHourRemainder = mFragmentViewWeek2.mStateParameters.BusinessHoursEndTime % 3600000;
		int wholeHoursCount = (mFragmentViewWeek2.mStateParameters.BusinessHoursEndTime - mBusinessHoursEndTimeHourRemainder)
				/ (60 * 60 * 1000)
				- (mFragmentViewWeek2.mStateParameters.BusinessHoursStartTime + (mBusinessHoursStartTimeHourRemainder == 0 ? 0
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
		int businessHoursTop = 0
				+ (mTaskOccurrencesDistributionState.AllDayTasksExist ? mWholeDayTasksHeightPx
						+ mWholeDayTasksThisDayTasksSpacerPx
						: 0)
				+ (mTaskOccurrencesDistributionState.PreBusinessHoursTasksExist ? mPreBusinessHoursTasksHeightPx
						+ mHoursSpacerPx
						: 0);
		int businessHoursBottom = height
				- (mTaskOccurrencesDistributionState.PostBusinessHoursTasksExist ? mPostBusinessHoursTasksHeightPx
						+ mHoursSpacerPx
						: 0);
		int businessHoursPixels = businessHoursBottom - businessHoursTop - mHoursSpacerPx
				* businessHoursSpacersCount;
		float pixelsPerMillisecond = businessHoursPixels
				/ (float) (mFragmentViewWeek2.mStateParameters.BusinessHoursEndTime - mFragmentViewWeek2.mStateParameters.BusinessHoursStartTime);
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
		long nanoTime2 = System.nanoTime();
		long delta = (nanoTime2 - nanoTime) / 1000000;
		if (FragmentViewWeek2.DEBUG5) {
			Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug5,
					"calculateTimeIntervalViewsPositions." + "\t time delta: " + delta);
		}
		return timeIntervalViewsPositions;
	}

	private synchronized List<Task> getTasks() {
		if (mTasks == null) {
			mTasks = mRetainedFragment.getTasks();
		}
		return mTasks;
	}

	public void setMarkSyncParameters(SyncPolicy syncPolicy,
			MarkSyncNeededPolicy markSyncNeededPolicy) {
		// TODO Auto-generated method stub
	}

	public void setDaysCount(int daysCount) {
		if (mDaysCount != daysCount) {
			mDaysCount = daysCount;
			visibleThreshold = FragmentViewWeek2Adapter.VISIBLE_THRESHOLD;
			notifyDataSetChanged();
		}
	}

	public TimeIntervalViewsPositions getTimeIntervalViewsPositions() {
		return mTimeIntervalViewsPositions;
	}
}