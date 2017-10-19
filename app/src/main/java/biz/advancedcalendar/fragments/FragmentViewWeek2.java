package biz.advancedcalendar.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityMain.FragmentTags;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.fragments.FragmentViewMonth.DATE_PICKER_DATE;
import biz.advancedcalendar.fragments.FragmentViewWeek2Adapter.DayViewHolder;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewWeek2.CalculateAndDistributeTaskOccurrencesAsyncTaskArgument;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewWeek2.CalculateAndDistributeTaskOccurrencesAsyncTaskResult;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewWeek2.TaskCallbacks;
import biz.advancedcalendar.greendao.Task.BusinessHoursTaskDisplayingPolicy;
import biz.advancedcalendar.greendao.Task.HoursRulerDisplayingPolicy;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.FragmentViewWeek2RecyclerView;
import biz.advancedcalendar.views.TaskClickedListener;
import biz.advancedcalendar.views.TaskOccurrencesDistribution;
import biz.advancedcalendar.views.TaskOccurrencesDistributionState;
import biz.advancedcalendar.views.WeekViewCoreTimeRuler;
import biz.advancedcalendar.views.WeekViewDayHeaderClickedListener;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentViewWeek2 extends Fragment implements OnDateSetListener,
		TaskCallbacks {
	public interface RetainedFragmentForFragmentViewWeek2Holder {
		RetainedFragmentForFragmentViewWeek2 getRetainedFragmentForFragmentViewWeek2(
				String tag);
	}

	private WeekViewDayHeaderClickedListener mDayClickedListener;
	private TaskClickedListener mTaskClickedListener;
	FragmentViewWeek2RecyclerView mRecyclerView;
	private WeekViewCoreTimeRuler mWeekViewCoreTimeRuler;
	private ImageButton mButtonPrevious;
	private Button mButtonPickupDate;
	private ImageButton mButtonNext;
	private FragmentViewWeek2Adapter mAdapter;
	private SimpleDateFormat mMonthDateFormat = new SimpleDateFormat("MMMM",
			Locale.getDefault());
	private SimpleDateFormat mYearDateFormat = new SimpleDateFormat("yyyy",
			Locale.getDefault());
	private DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.FULL,
			Locale.getDefault());
	private String mPendingBundleForDatePickerDialogKey = "mPendingBundleForDatePickerDialogKey";
	public static final String InitArgumentsKeyFirstDay = "biz.advancedcalendar.fragments.FragmentViewWeek.InitArgumentsKeyFirstDay";
	public static final String InitArgumentsKeyDaysCount = "biz.advancedcalendar.fragments.FragmentViewWeek.InitArgumentsKeyDaysCount";
	public static final String StateParametersKey = "biz.advancedcalendar.fragments.FragmentViewWeek.StateParametersKey";
	public static final String ScrollStateKey = "biz.advancedcalendar.fragments.FragmentViewWeek.ScrollStateKey";
	static final boolean DEBUG = false;
	public static final boolean DEBUG5 = true;
	public static final boolean SCROLL_DEBUG = true;
	// private static final boolean DEBUG3 = false;
	public static final String FragmentViewWeek2Debug = "FragmentViewWeek2Debug";
	private static final int MIN_PAGING_VELOCITY_DIP_PER_SEC = 100;
	private static final int MAX_PAGING_VELOCITY_DIP_PER_SEC = 1000;
	// variables to be saved in onSaveInstanceState()
	private Bundle mPendingBundleForDatePickerDialog;
	private String mDatePickerDialogKey = "biz.advancedcalendar.fragments.FragmentViewWeek.DatePickerDialogKey";
	private RetainedFragmentForFragmentViewWeek2 mRetainedFragment;
	private TaskOccurrencesDistribution mPreallocatedTaskOccurrencesDistribution;
	// private List<Task> mTasks;
	private BroadcastReceiver mReceiver;
	FragmentViewWeek2StateParameters mStateParameters;
	LinearLayoutManager mLinearLayoutManager;
	Runnable mReconcileRunnable;
	boolean mIsReconcileRunnablePending;
	protected Integer mLastRegisteredFirstVisibleItemPosition;
	protected int mLastRegisteredLastVisibleItemPosition;
	private Integer mLastRegisteredPrimaryItemPosition;
	protected Long mLastRegisteredOnScrolledTime;
	private float mScaledMinPagingVelocity;
	private float mScaledMaxPagingVelocity;
	private float mDensity;
	private String mTag;
	private ColorStateList mDefaultTextColorStateList;
	private Drawable mDefaultBackground;
	protected boolean mIsSmoothScrollingToSpecificPositionAfterFling;
	public int mTargetHeadItemPosition;
	public int mSmoothScrollingToSpecificPositionAfterFlingDirection;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restoreState(savedInstanceState);
		mTag = getTag();
		final Resources resources = getResources();
		mDensity = resources.getDisplayMetrics().density;
		mScaledMinPagingVelocity = mDensity
				* FragmentViewWeek2.MIN_PAGING_VELOCITY_DIP_PER_SEC;
		mScaledMaxPagingVelocity = mDensity
				* FragmentViewWeek2.MAX_PAGING_VELOCITY_DIP_PER_SEC;
		mPreallocatedTaskOccurrencesDistribution = new TaskOccurrencesDistribution(
				mStateParameters.DaysCount);
		mPreallocatedTaskOccurrencesDistribution.TaskOccurrencesTakingWholeIntervalList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		mPreallocatedTaskOccurrencesDistribution.TaskOccurrencesTouchingPreBusinessHoursList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		mPreallocatedTaskOccurrencesDistribution.TaskOccurrencesTouchingBusinessHoursList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		mPreallocatedTaskOccurrencesDistribution.TaskOccurrencesTouchingPostBusinessHoursList = new ArrayList<List<CalendarViewTaskOccurrence>>();
		//
		//
		// mTasks = DataProvider.getActiveNotDeletedTasks(getActivity()
		// .getApplicationContext());
		//
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if (action.equals(CommonConstants.ACTION_CALENDARS_CHANGED)
							|| action
									.equals(CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED)) {
						// if (!isDetached()) {
						if (isAdded()) {
							mAdapter.invalidateTasks();
							// TODO: Are the lines commented below needed?
							// mWeekViewCoreTimeRuler
							// .setAssignedTaskOccurrencesDistributionState(mAdapter
							// .getAssignedTaskOccurrencesDistributionState());
						} else {
							mStateParameters.TasksNeedReload = true;
						}
					} else {
						if (action
								.equals(CommonConstants.ACTION_DAYS_COUNT_FOR_WEEKVIEW_CHANGED)) {
							if (getTag()
									.equals(intent
											.getStringExtra(CommonConstants.INTENT_EXTRA_TAG))) {
								// if (!isDetached()) {
								int daysCount = Helper
										.getIntegerPreferenceValue(
												context,
												resources
														.getString(R.string.preference_key_days_count_for_weekview),
												resources
														.getInteger(R.integer.weekview_days_count_default_value),
												resources
														.getInteger(R.integer.weekview_days_count_min_value),
												resources
														.getInteger(R.integer.weekview_days_count_max_value));
								mStateParameters.DaysCount = daysCount;
								if (mAdapter != null) {
									mAdapter.setDaysCount(daysCount);
									mRecyclerView.setDaysCount(daysCount);
								}
							}
						} else if (action
								.equals(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
							// if (!isDetached()) {
							if (isAdded()) {
								mAdapter.invalidateTasks();
								// TODO: Are the lines commented below needed?
								// mWeekViewCoreTimeRuler
								// .setAssignedTaskOccurrencesDistributionState(mAdapter
								// .getAssignedTaskOccurrencesDistributionState());
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else if (action
								.equals(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED)) {
							if (isAdded()) {
								mAdapter.invalidateTasks();
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else if (action
								.equals(CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED)) {
							// if (!isDetached()) {
							if (isAdded()) {
								mAdapter.invalidateTasks();
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else if (action
								.equals(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED)) {
							// if (!isDetached()) {
							mStateParameters.InformationUnitMatrixForTimeIntervalsMode = Helper
									.createInformationUnitMatrix(
											context,
											R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode,
											R.string.information_unit_matrix_for_calendar_time_intervals_mode_default_value);
							if (isAdded()) {
								mAdapter.invalidateTaskOccurrencesDistributions();
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else if (action
								.equals(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED)) {
							// if (!isDetached()) {
							mStateParameters.InformationUnitMatrixForTextMode = Helper
									.createInformationUnitMatrix(
											context,
											R.string.preference_key_information_unit_matrix_for_calendar_text_mode,
											R.string.information_unit_matrix_for_calendar_text_mode_default_value);
							if (isAdded()) {
								mAdapter.invalidateTaskOccurrencesDistributions();
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else {
							Resources resources = context.getResources();
							if (action
									.equals(CommonConstants.ACTION_BUSINESS_HOURS_START_TIME_CHANGED)) {
								mStateParameters.BusinessHoursStartTime = (int) Helper
										.getLongPreferenceValue(
												context,
												R.string.preference_key_business_hours_start_time,
												resources
														.getInteger(R.integer.business_hours_start_time_default_value),
												(long) resources
														.getInteger(R.integer.business_hours_start_time_min_value),
												(long) resources
														.getInteger(R.integer.business_hours_start_time_max_value));
								if (isAdded()) {
									mAdapter.invalidateTaskOccurrencesDistributions();
									mWeekViewCoreTimeRuler
											.setBusinessHoursStartTime(mStateParameters.BusinessHoursStartTime);
									// TODO: Are the lines commented below needed?
									// mWeekViewCoreTimeRuler
									// .setAssignedTaskOccurrencesDistributionState(mAdapter
									// .getAssignedTaskOccurrencesDistributionState());
								} else {
									mStateParameters.TasksNeedReload = true;
								}
							} else if (action
									.equals(CommonConstants.ACTION_BUSINESS_HOURS_END_TIME_CHANGED)) {
								mStateParameters.BusinessHoursEndTime = (int) Helper
										.getLongPreferenceValue(
												context,
												R.string.preference_key_business_hours_end_time,
												resources
														.getInteger(R.integer.business_hours_end_time_default_value),
												(long) resources
														.getInteger(R.integer.business_hours_end_time_min_value),
												(long) resources
														.getInteger(R.integer.business_hours_end_time_max_value));
								if (isAdded()) {
									mAdapter.invalidateTaskOccurrencesDistributions();
									mWeekViewCoreTimeRuler
											.setBusinessHoursEndTime(mStateParameters.BusinessHoursEndTime);
									// TODO: Are the lines commented below needed?
									// mWeekViewCoreTimeRuler
									// .setAssignedTaskOccurrencesDistributionState(mAdapter
									// .getAssignedTaskOccurrencesDistributionState());
								} else {
									mStateParameters.TasksNeedReload = true;
								}
							} else if (action
									.equals(CommonConstants.ACTION_BUSINESS_HOURS_TASK_DISPLAYING_POLICY_CHANGED)) {
								mStateParameters.BusinessHoursTaskDisplayingPolicy = BusinessHoursTaskDisplayingPolicy
										.fromInt((byte) Helper
												.getIntegerPreferenceValueFromStringArray(
														context,
														R.string.preference_key_business_hours_task_displaying_policy,
														R.array.business_hours_task_displaying_policy_values_array,
														R.integer.business_hours_task_displaying_policy_default_value));
								if (isAdded()) {
									mAdapter.invalidateTaskOccurrencesDistributions();
								} else {
									mStateParameters.TasksNeedReload = true;
								}
							}
							//
							// else if (intent.getAction().equals(
							// CommonConstants.ACTION_HOURS_RULER_DISPLAYING_POLICY_CHANGED))
							// {
							// mFragmentViewWeek2StateParameters.HoursRulerDisplayingPolicy
							// =
							// HoursRulerDisplayingPolicy
							// .fromInt((byte) Helper
							// .getIntegerPreferenceValueFromStringArray(
							// context,
							// R.string.preference_key_hours_ruler_displaying_policy,
							// R.array.hours_ruler_displaying_policy_values_array,
							// R.integer.hours_ruler_displaying_policy_default_value));
							// }
							//
							else if (action
									.equals(CommonConstants.ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED)) {
								if (getTag()
										.equals(intent
												.getStringExtra(CommonConstants.INTENT_EXTRA_TAG))) {
									mStateParameters.FirstDay = intent.getLongExtra(
											CommonConstants.INTENT_EXTRA_DATETIME, 0);
									int daysCount = intent.getIntExtra(
											CommonConstants.INTENT_EXTRA_DAYS_COUNT, 0);
									mStateParameters.DaysCount = daysCount;
									mAdapter.setDaysCount(daysCount);
									if (isAdded()) {
										mRecyclerView
												.scrollPositionToFirst(
														mAdapter.getItemPositionForDateTime(mStateParameters.FirstDay),
														null);
									}
								}
							} else if (action
									.equals(CommonConstants.ACTION_SYNC_POLICY_CHANGED)
									|| action
											.equals(CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED)) {
								SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
										.getIntegerPreferenceValueFromStringArray(
												context,
												R.string.preference_key_sync_policy,
												R.array.sync_policy_values_array,
												R.integer.sync_policy_default_value));
								mStateParameters.SyncPolicy = syncPolicy;
								MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
										.fromInt((byte) Helper
												.getIntegerPreferenceValueFromStringArray(
														context,
														R.string.preference_key_mark_sync_needed,
														R.array.mark_sync_needed_values_array,
														R.integer.mark_sync_needed_default_value));
								mStateParameters.MarkSyncNeeded = markSyncNeededPolicy;
								if (isAdded()) {
									mAdapter.setMarkSyncParameters(syncPolicy,
											markSyncNeededPolicy);
									//
									// FragmentViewWeek2.this.getView().invalidate();
									// FragmentViewWeek2.this.getView().requestLayout();
								}
							} else if (action.equals(Intent.ACTION_TIME_CHANGED)) {
								if (isAdded()) {
									if (Build.VERSION.SDK_INT >= 11) {
										getActivity().invalidateOptionsMenu();
									}
								}
							}
						}
					}
				}
			};
			//
			final FragmentActivity context = getActivity();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_CALENDARS_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_DAYS_COUNT_FOR_WEEKVIEW_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
			intentFilter.addAction(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_MONTH_RECURRENCE_MODE_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_START_TIME_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_BUSINESS_HOURS_START_TIME_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_BUSINESS_HOURS_END_TIME_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_BUSINESS_HOURS_TASK_DISPLAYING_POLICY_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_HOURS_RULER_DISPLAYING_POLICY_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_START_TIME_REQUIRED_ACTION_CHANGED);
			intentFilter
					.addAction(CommonConstants.ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_SYNC_POLICY_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED);
			intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
			LocalBroadcastManager.getInstance(context).registerReceiver(mReceiver,
					intentFilter);
			//
			if (savedInstanceState != null) {
				// because we just now registered receiver and savedInstanceState != null
				// we might miss some broadcasts so setup anew all settings might be
				// changed because of broadcast
				TextView tv = new TextView(context);
				SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
						.getIntegerPreferenceValueFromStringArray(context,
								R.string.preference_key_sync_policy,
								R.array.sync_policy_values_array,
								R.integer.sync_policy_default_value));
				MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
						.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(
								context, R.string.preference_key_mark_sync_needed,
								R.array.mark_sync_needed_values_array,
								R.integer.mark_sync_needed_default_value));
				syncPolicy = SyncPolicy.DO_NOT_SYNC;
				markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
				InformationUnitMatrix informationUnitMatrixForTimeIntervalsMode;
				InformationUnitMatrix informationUnitMatrixForTextMode;
				informationUnitMatrixForTimeIntervalsMode = Helper
						.createInformationUnitMatrix(
								context,
								R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode,
								R.string.information_unit_matrix_for_calendar_time_intervals_mode_default_value);
				informationUnitMatrixForTextMode = Helper
						.createInformationUnitMatrix(
								context,
								R.string.preference_key_information_unit_matrix_for_calendar_text_mode,
								R.string.information_unit_matrix_for_calendar_text_mode_default_value);
				mStateParameters = new FragmentViewWeek2StateParameters(
						mStateParameters.DayBeginningOnVirtualMiddleOffset,
						mStateParameters.FirstDay,
						mStateParameters.DaysCount,
						mStateParameters.ShowDayHeaders,
						mStateParameters.ShowNavigationBar,
						informationUnitMatrixForTimeIntervalsMode,
						informationUnitMatrixForTextMode,
						(int) Helper
								.getLongPreferenceValue(
										context,
										R.string.preference_key_business_hours_start_time,
										resources
												.getInteger(R.integer.business_hours_start_time_default_value),
										(long) resources
												.getInteger(R.integer.business_hours_start_time_min_value),
										(long) resources
												.getInteger(R.integer.business_hours_start_time_max_value)),
						(int) Helper
								.getLongPreferenceValue(
										context,
										R.string.preference_key_business_hours_end_time,
										resources
												.getInteger(R.integer.business_hours_end_time_default_value),
										(long) resources
												.getInteger(R.integer.business_hours_end_time_min_value),
										(long) resources
												.getInteger(R.integer.business_hours_end_time_max_value)),
						BusinessHoursTaskDisplayingPolicy.fromInt((byte) Helper
								.getIntegerPreferenceValueFromStringArray(
										context,
										R.string.preference_key_business_hours_task_displaying_policy,
										R.array.business_hours_task_displaying_policy_values_array,
										R.integer.business_hours_task_displaying_policy_default_value)),
						HoursRulerDisplayingPolicy.fromInt((byte) Helper
								.getIntegerPreferenceValueFromStringArray(
										context,
										R.string.preference_key_hours_ruler_displaying_policy,
										R.array.hours_ruler_displaying_policy_values_array,
										R.integer.hours_ruler_displaying_policy_default_value)),
						syncPolicy, markSyncNeededPolicy, tv.getTextSize(),
						mStateParameters.Mode);
				mStateParameters.TasksNeedReload = true;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		restoreState(savedInstanceState);
		View root = inflater.inflate(R.layout.fragment_week_view2, container, false);
		mRecyclerView = (FragmentViewWeek2RecyclerView) root.findViewById(R.id.pager);
		mRecyclerView.setScaledPagingVelocity(mScaledMinPagingVelocity,
				mScaledMaxPagingVelocity);
		// mRecyclerView.setOnGenericMotionListener(null);
		mReconcileRunnable = new Runnable() {
			@Override
			public void run() {
				if (!isAdded()) {
					mIsReconcileRunnablePending = false;
					return;
				}
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				if (firstVisibleItemPosition != -1) {
					TaskOccurrencesDistributionState reconciledTaskOccurrencesDistributionState = mAdapter
							.calculateReconciledTaskOccurrencesDistributionState();
					TaskOccurrencesDistributionState currentTaskOccurrencesDistributionState = mAdapter
							.getTaskOccurrencesDistributionState();
					DayViewHolder dayViewHolder = (DayViewHolder) mRecyclerView
							.findViewHolderForLayoutPosition(firstVisibleItemPosition);
					if (currentTaskOccurrencesDistributionState == null
							|| !currentTaskOccurrencesDistributionState
									.equals(reconciledTaskOccurrencesDistributionState)
							|| mAdapter.getTimeIntervalViewsPositions() == null) {
						mAdapter.setTaskOccurrencesDistributionState(
								reconciledTaskOccurrencesDistributionState,
								dayViewHolder.mDayViewCore.getHeight());
						mWeekViewCoreTimeRuler.setTaskOccurrencesDistributionState(
								reconciledTaskOccurrencesDistributionState,
								mAdapter.getTimeIntervalViewsPositions());
					}
					Integer weekViewCoreTop = mWeekViewCoreTimeRuler.getWeekViewCoreTop();
					Integer weekViewCoreTopToSet = dayViewHolder.getWeekViewCoreTop();
					if (weekViewCoreTop != null
							&& weekViewCoreTop == weekViewCoreTopToSet) {
						weekViewCoreTopToSet = null;
					}
					if (weekViewCoreTopToSet != null) {
						mWeekViewCoreTimeRuler.setWeekViewCoreTop(weekViewCoreTopToSet);
					}
					Integer weekViewCoreHoursDrawingTop = mWeekViewCoreTimeRuler
							.getWeekViewCoreHoursDrawingTop();
					Integer weekViewCoreHoursDrawingTopToSet = dayViewHolder
							.getWeekViewCoreHoursDrawingTop();
					if (weekViewCoreHoursDrawingTop != null
							&& weekViewCoreHoursDrawingTop == weekViewCoreHoursDrawingTopToSet) {
						weekViewCoreHoursDrawingTopToSet = null;
					}
					if (weekViewCoreHoursDrawingTopToSet != null) {
						mWeekViewCoreTimeRuler
								.setWeekViewCoreHoursDrawingTop(weekViewCoreHoursDrawingTopToSet);
					}
				}
				mIsReconcileRunnablePending = false;
			}
		};
		mRecyclerView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			@Override
			public void onChildViewRemoved(View parent, View child) {
				if (!mIsReconcileRunnablePending) {
					mIsReconcileRunnablePending = true;
					mRecyclerView.post(mReconcileRunnable);
				}
			}

			@Override
			public void onChildViewAdded(View parent, View child) {
				if (!mIsReconcileRunnablePending) {
					mIsReconcileRunnablePending = true;
					mRecyclerView.post(mReconcileRunnable);
				}
			}
		});
		mRecyclerView.setHasFixedSize(true);
		//
		mWeekViewCoreTimeRuler = (biz.advancedcalendar.views.WeekViewCoreTimeRuler) root
				.findViewById(R.id.weekviewcoretimeruler);
		//
		mButtonPrevious = (ImageButton) root
				.findViewById(R.id.fragment_weekview_button_previous);
		mButtonPrevious.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				if (firstVisibleItemPosition != -1) {
					DayViewHolder DayViewHolder = (DayViewHolder) mRecyclerView
							.findViewHolderForLayoutPosition(firstVisibleItemPosition);
					// if (DayViewHolder.itemView.getLeft() > -3) {
					// mRecyclerView.smoothScrollToPosition(firstVisibleItemPosition
					// - mFragmentViewWeek2StateParameters.DaysCount);
					// } else {
					// mRecyclerView.smoothScrollToPosition(firstVisibleItemPosition
					// - mFragmentViewWeek2StateParameters.DaysCount + 1);
					// }
					//
					if (DayViewHolder.itemView.getLeft() != 0) {
						mRecyclerView.smoothScrollPositionToFirst(
								firstVisibleItemPosition, firstVisibleItemPosition);
					} else {
						mRecyclerView.smoothScrollPositionToFirst(
								firstVisibleItemPosition - mStateParameters.DaysCount,
								firstVisibleItemPosition);
					}
					// updateDateText();
				}
			}
		});
		mButtonPickupDate = (Button) root
				.findViewById(R.id.fragment_weekview_button_pickup_date);
		mDefaultTextColorStateList = mButtonPickupDate.getTextColors();
		mDefaultBackground = mButtonPickupDate.getBackground();
		mButtonPickupDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DATE_PICKER_DATE datePickerDate = DATE_PICKER_DATE.fromInt((short) Helper
						.getIntegerPreferenceValueFromStringArray(
								getActivity(),
								R.string.preference_key_date_picker_default_selected_date,
								R.array.date_picker_selected_date_values_array,
								R.integer.date_picker_default_selected_date));
				Calendar calendar = Calendar.getInstance();
				switch (datePickerDate) {
				case LAST_BROWSING:
				default:
					calendar.setTimeInMillis(mStateParameters.DayBeginningOnVirtualMiddleOffset);
					calendar.add(Calendar.DAY_OF_YEAR,
							mLinearLayoutManager.findFirstVisibleItemPosition()
									- FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
					break;
				case TODAY:
					break;
				}
				mPendingBundleForDatePickerDialog = new Bundle();
				mPendingBundleForDatePickerDialog.putInt(CommonConstants.CALLER_ID,
						R.id.fragment_weekview_button_pickup_date);
				DatePickerDialog dpd = DatePickerDialog.newInstance(
						FragmentViewWeek2.this, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				dpd.show(getFragmentManager(), mDatePickerDialogKey);
			}
		});
		mButtonNext = (ImageButton) root.findViewById(R.id.fragment_weekview_button_next);
		mButtonNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				if (firstVisibleItemPosition != -1) {
					DayViewHolder DayViewHolder = (DayViewHolder) mRecyclerView
							.findViewHolderForLayoutPosition(firstVisibleItemPosition);
					// if (DayViewHolder.itemView.getRight() < DayViewHolder.mParent
					// .getWidth() + 3) {
					// mRecyclerView.smoothScrollToPosition(lastVisibleItemPosition
					// + mFragmentViewWeek2StateParameters.DaysCount);
					// } else {
					// mRecyclerView.smoothScrollToPosition(lastVisibleItemPosition
					// + mFragmentViewWeek2StateParameters.DaysCount - 1);
					// }
					//
					if (DayViewHolder.itemView.getLeft() != 0) {
						mRecyclerView.smoothScrollPositionToFirst(
								firstVisibleItemPosition + 1, firstVisibleItemPosition);
					} else {
						mRecyclerView.smoothScrollPositionToFirst(
								firstVisibleItemPosition + mStateParameters.DaysCount,
								firstVisibleItemPosition);
					}
				}
			}
		});
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getFragmentManager();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"FragmentViewAgenda onActivityCreated() thread: %s", Thread
							.currentThread().getName()));
		}
		restoreState(savedInstanceState);
		FragmentActivity activity = getActivity();
		mRetainedFragment = ((RetainedFragmentForFragmentViewWeek2Holder) activity)
				.getRetainedFragmentForFragmentViewWeek2(getTag());
		if (mStateParameters.TasksNeedReload) {
			mRetainedFragment.invalidateTasks();
			mStateParameters.TasksNeedReload = false;
		}
		mRetainedFragment.setCallbacks(this);
		setHasOptionsMenu(true);
		mTaskClickedListener = (TaskClickedListener) activity;
		mDayClickedListener = (WeekViewDayHeaderClickedListener) activity;
		mLinearLayoutManager = new LinearLayoutManager(activity,
				LinearLayoutManager.HORIZONTAL, false);
		mRecyclerView.getRecycledViewPool().setMaxRecycledViews(0, 50);
		mRecyclerView.setItemViewCacheSize(14);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setDaysCount(mStateParameters.DaysCount);
		mRecyclerView.setScaledPagingVelocity(mScaledMinPagingVelocity,
				mScaledMaxPagingVelocity);
		mAdapter = new FragmentViewWeek2Adapter(this, mRetainedFragment,
				mTaskClickedListener, mDayClickedListener);
		// mAdapter.setTextSize(mStateParameters.TextSize);
		mAdapter.setMarkSyncParameters(mStateParameters.SyncPolicy,
				mStateParameters.MarkSyncNeeded);
		mAdapter.setMode(mStateParameters.Mode);
		mAdapter.setInformationUnitMatrices(
				mStateParameters.InformationUnitMatrixForTimeIntervalsMode,
				mStateParameters.InformationUnitMatrixForTextMode);
		mRecyclerView.setAdapter(mAdapter);
		mRecyclerView.scrollToPosition(FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
		// mRecyclerView.scrollToPosition(mAdapter
		// .getItemPositionForDateTime(mStateParameters.FirstDay));
		mRecyclerView.addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				updateDateText();
			}
		});
		//
		mWeekViewCoreTimeRuler.setTextSize(mStateParameters.TextSize);
		mWeekViewCoreTimeRuler
				.setBusinessHoursStartTime(mStateParameters.BusinessHoursStartTime);
		mWeekViewCoreTimeRuler
				.setBusinessHoursEndTime(mStateParameters.BusinessHoursEndTime);
		mWeekViewCoreTimeRuler.setTaskOccurrencesDistributionState(
				mAdapter.getTaskOccurrencesDistributionState(),
				mAdapter.getTimeIntervalViewsPositions());
		//
		if (mStateParameters.Mode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
			mWeekViewCoreTimeRuler.setVisibility(View.VISIBLE);
		} else {
			mWeekViewCoreTimeRuler.setVisibility(View.GONE);
		}
		//
		updateDateText();
		//
		DatePickerDialog dpd = (DatePickerDialog) fm
				.findFragmentByTag(mDatePickerDialogKey);
		if (dpd != null) {
			dpd.setOnDateSetListener(this);
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Resources resources = getResources();
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_GOTO_TODAY,
				Menu.FIRST + 100, resources.getString(R.string.action_goto_today));
		Calendar calendar = Calendar.getInstance();
		menuItem.setIcon(CommonConstants.DAY_OF_MONTH_ICON_RESOURCES[calendar
				.get(Calendar.DAY_OF_MONTH) - 1]);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		if (mStateParameters.Mode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
			menuItem = menu
					.add(Menu.NONE,
							CommonConstants.MENU_ID_WEEKVIEW_MODE,
							Menu.FIRST + 100,
							resources
									.getString(R.string.action_weekview_toggle_display_mode_list));
			menuItem.setIcon(R.drawable.ic_view_list_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		} else {
			menuItem = menu
					.add(Menu.NONE,
							CommonConstants.MENU_ID_WEEKVIEW_MODE,
							Menu.FIRST + 100,
							resources
									.getString(R.string.action_weekview_toggle_display_mode_time_intervals));
			menuItem.setIcon(R.drawable.ic_view_quilt_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
		//
		// boolean collapsed = Helper.getBooleanPreferenceValue(getActivity(),
		// R.string.preference_key_fragment_view_week_collapse_expand_state,
		// getResources()
		// .getBoolean(R.bool.fragment_view_week_collapse_expand_state));
		// //
		// if (collapsed) {
		// menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_EXPAND_COLLAPSE,
		// Menu.FIRST + 100, getResources().getString(R.string.action_expand));
		// menuItem.setIcon(R.drawable.ic_expand_more_black_24dp);
		// MenuItemCompat.setShowAsAction(menuItem,
		// MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		// } else {
		// menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_EXPAND_COLLAPSE,
		// Menu.FIRST + 100, getResources().getString(R.string.action_collapse));
		// menuItem.setIcon(R.drawable.ic_expand_less_black_24dp);
		// MenuItemCompat.setShowAsAction(menuItem,
		// MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		// }
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable(FragmentViewWeek2.StateParametersKey,
				mStateParameters);
		savedInstanceState.putParcelable(mPendingBundleForDatePickerDialogKey,
				mPendingBundleForDatePickerDialog);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (true /* || getActivity().isFinishing() */) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		Resources resources = getResources();
		FragmentActivity context = getActivity();
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_GOTO_TODAY:
			Calendar calendar1 = Calendar.getInstance();
			calendar1.setTimeInMillis(mStateParameters.DayBeginningOnVirtualMiddleOffset);
			Calendar calendar2 = Calendar.getInstance();
			CalendarHelper.toBeginningOfDay(calendar2);
			mRecyclerView.scrollToPosition(FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET
					+ CalendarHelper.daysBetween(calendar1, calendar2));
			// updateDateText();
			return true;
		case CommonConstants.MENU_ID_WEEKVIEW_MODE:
			if (mStateParameters.Mode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
				mStateParameters.Mode = CommonConstants.CALENDAR_MODE_TEXT;
				item.setIcon(R.drawable.ic_view_quilt_black_24dp);
				item.setTitle(R.string.action_weekview_toggle_display_mode_time_intervals);
				mWeekViewCoreTimeRuler.setVisibility(View.GONE);
			} else {
				mStateParameters.Mode = CommonConstants.CALENDAR_MODE_TIME_INTERVALS;
				item.setIcon(R.drawable.ic_view_list_black_24dp);
				item.setTitle(R.string.action_weekview_toggle_display_mode_list);
				mWeekViewCoreTimeRuler.setVisibility(View.VISIBLE);
			}
			if (mTag.equals(FragmentTags.DAY.name())) {
				PreferenceManager
						.getDefaultSharedPreferences(context)
						.edit()
						.putInt(resources.getString(R.string.preference_key_mode_for_day),
								mStateParameters.Mode).commit();
			} else if (mTag.equals(FragmentTags.WEEK.name())) {
				PreferenceManager
						.getDefaultSharedPreferences(context)
						.edit()
						.putInt(resources
								.getString(R.string.preference_key_mode_for_week),
								mStateParameters.Mode).commit();
			}
			mAdapter.setMode(mStateParameters.Mode);
			// mRecyclerView.requestLayout();
			return true;
		case CommonConstants.MENU_ID_EXPAND_COLLAPSE:
			boolean isCurrentlyCollapsed = Helper
					.getBooleanPreferenceValue(
							context,
							R.string.preference_key_fragment_view_week_collapse_expand_state,
							resources
									.getBoolean(R.bool.fragment_view_week_collapse_expand_state));
			//
			PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit()
					.putBoolean(
							resources
									.getString(R.string.preference_key_fragment_view_week_collapse_expand_state),
							!isCurrentlyCollapsed).commit();
			//
			if (isCurrentlyCollapsed) {
				item.setIcon(R.drawable.ic_expand_less_black_24dp);
				item.setTitle(R.string.action_collapse);
			} else {
				item.setIcon(R.drawable.ic_expand_more_black_24dp);
				item.setTitle(R.string.action_expand);
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void restoreState(Bundle savedInstanceState) {
		mTag = getTag();
		if (savedInstanceState != null) {
			if (mStateParameters == null) {
				mStateParameters = savedInstanceState
						.getParcelable(FragmentViewWeek2.StateParametersKey);
				mPendingBundleForDatePickerDialog = (Bundle) savedInstanceState
						.getParcelable(mPendingBundleForDatePickerDialogKey);
			}
		} else if (mStateParameters == null) {
			long firstDay;
			firstDay = getArguments().getLong(FragmentViewWeek2.InitArgumentsKeyFirstDay);
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(firstDay);
			CalendarHelper.toBeginningOfDay(calendar);
			firstDay = calendar.getTimeInMillis();
			int daysCount = getArguments().getInt(
					FragmentViewWeek2.InitArgumentsKeyDaysCount);
			FragmentActivity context = getActivity();
			TextView tv = new TextView(context);
			SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(context,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
					.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(
							context, R.string.preference_key_mark_sync_needed,
							R.array.mark_sync_needed_values_array,
							R.integer.mark_sync_needed_default_value));
			syncPolicy = SyncPolicy.DO_NOT_SYNC;
			markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
			Resources resources = getResources();
			int mode = mTag.equals(FragmentTags.DAY.name()) ? Helper
					.getIntegerPreferenceValue(context,
							R.string.preference_key_mode_for_day,
							CommonConstants.CALENDAR_MODE_TIME_INTERVALS, null, null)
					: mTag.equals(FragmentTags.WEEK.name()) ? Helper
							.getIntegerPreferenceValue(context,
									R.string.preference_key_mode_for_week,
									CommonConstants.CALENDAR_MODE_TIME_INTERVALS, null,
									null) : CommonConstants.CALENDAR_MODE_TIME_INTERVALS;
			InformationUnitMatrix informationUnitMatrixForTimeIntervalsMode;
			InformationUnitMatrix informationUnitMatrixForTextMode;
			informationUnitMatrixForTimeIntervalsMode = Helper
					.createInformationUnitMatrix(
							context,
							R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode,
							R.string.information_unit_matrix_for_calendar_time_intervals_mode_default_value);
			informationUnitMatrixForTextMode = Helper
					.createInformationUnitMatrix(
							context,
							R.string.preference_key_information_unit_matrix_for_calendar_text_mode,
							R.string.information_unit_matrix_for_calendar_text_mode_default_value);
			mStateParameters = new FragmentViewWeek2StateParameters(
					firstDay,
					firstDay,
					daysCount,
					daysCount > 1,
					true,
					informationUnitMatrixForTimeIntervalsMode,
					informationUnitMatrixForTextMode,
					(int) Helper.getLongPreferenceValue(
							context,
							R.string.preference_key_business_hours_start_time,
							resources
									.getInteger(R.integer.business_hours_start_time_default_value),
							(long) resources
									.getInteger(R.integer.business_hours_start_time_min_value),
							(long) resources
									.getInteger(R.integer.business_hours_start_time_max_value)),
					(int) Helper.getLongPreferenceValue(
							context,
							R.string.preference_key_business_hours_end_time,
							resources
									.getInteger(R.integer.business_hours_end_time_default_value),
							(long) resources
									.getInteger(R.integer.business_hours_end_time_min_value),
							(long) resources
									.getInteger(R.integer.business_hours_end_time_max_value)),
					BusinessHoursTaskDisplayingPolicy.fromInt((byte) Helper
							.getIntegerPreferenceValueFromStringArray(
									context,
									R.string.preference_key_business_hours_task_displaying_policy,
									R.array.business_hours_task_displaying_policy_values_array,
									R.integer.business_hours_task_displaying_policy_default_value)),
					HoursRulerDisplayingPolicy.fromInt((byte) Helper
							.getIntegerPreferenceValueFromStringArray(
									context,
									R.string.preference_key_hours_ruler_displaying_policy,
									R.array.hours_ruler_displaying_policy_values_array,
									R.integer.hours_ruler_displaying_policy_default_value)),
					syncPolicy, markSyncNeededPolicy, tv.getTextSize(), mode);
		}
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(mStateParameters.DayBeginningOnVirtualMiddleOffset);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
		calendar2.set(Calendar.MILLISECOND, 0);
		mRecyclerView.scrollToPosition(FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET
				+ CalendarHelper.daysBetween(calendar1, calendar2));
		// updateDateText();
	}

	private void updateDateText() {
		int firstVisibleItemPosition = mLinearLayoutManager
				.findFirstVisibleItemPosition();
		int lastVisibleItemPosition = firstVisibleItemPosition;
		if (firstVisibleItemPosition != -1) {
			lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
		}
		if (mLastRegisteredFirstVisibleItemPosition != null
				&& mLastRegisteredFirstVisibleItemPosition == firstVisibleItemPosition
				&& mLastRegisteredLastVisibleItemPosition == lastVisibleItemPosition
				&& mStateParameters.DaysCount != 1) {
			return;
		}
		String result = "";
		Calendar currentItemStartDateTime = null;
		if (firstVisibleItemPosition != -1) {
			if (mStateParameters.DaysCount == 1) {
				DayViewHolder firstVisibleItemPositionDayViewHolder = (DayViewHolder) mRecyclerView
						.findViewHolderForLayoutPosition(firstVisibleItemPosition);
				Integer currentPrimaryItemPosition = firstVisibleItemPositionDayViewHolder.itemView
						.getRight() > firstVisibleItemPositionDayViewHolder.mParent
						.getWidth() / 2 ? firstVisibleItemPosition
						: lastVisibleItemPosition;
				if (mLastRegisteredPrimaryItemPosition != null
						&& mLastRegisteredPrimaryItemPosition
								.equals(currentPrimaryItemPosition)) {
					return;
				} else {
					mLastRegisteredPrimaryItemPosition = currentPrimaryItemPosition;
					Calendar firstVisibleItemStartDateTime = Calendar.getInstance();
					firstVisibleItemStartDateTime
							.setTimeInMillis(firstVisibleItemPositionDayViewHolder.mDayViewCore
									.getDayStartDateTime());
					firstVisibleItemStartDateTime.set(Calendar.HOUR_OF_DAY, 0);
					firstVisibleItemStartDateTime.set(Calendar.MINUTE, 0);
					firstVisibleItemStartDateTime.set(Calendar.SECOND, 0);
					firstVisibleItemStartDateTime.set(Calendar.MILLISECOND, 0);
					if (currentPrimaryItemPosition != firstVisibleItemPosition) {
						firstVisibleItemStartDateTime.add(Calendar.DAY_OF_YEAR, 1);
					}
					currentItemStartDateTime = firstVisibleItemStartDateTime;
					result = mDateFormat.format(new Date(currentItemStartDateTime
							.getTimeInMillis()));
				}
			} else {
				int visibleDaysCount = lastVisibleItemPosition - firstVisibleItemPosition
						+ 1;
				Calendar firstDayStartDateTime = Calendar.getInstance();
				firstDayStartDateTime
						.setTimeInMillis(((DayViewHolder) mRecyclerView
								.findViewHolderForLayoutPosition(firstVisibleItemPosition)).mDayViewCore
								.getDayStartDateTime());
				firstDayStartDateTime.set(Calendar.HOUR_OF_DAY, 0);
				firstDayStartDateTime.set(Calendar.MINUTE, 0);
				firstDayStartDateTime.set(Calendar.SECOND, 0);
				firstDayStartDateTime.set(Calendar.MILLISECOND, 0);
				Calendar lastDayEndDateTime = (Calendar) firstDayStartDateTime.clone();
				lastDayEndDateTime.add(Calendar.DAY_OF_YEAR, visibleDaysCount);
				lastDayEndDateTime.add(Calendar.MILLISECOND, -1);
				if (visibleDaysCount == 1) {
					result = mDateFormat.format(new Date(lastDayEndDateTime
							.getTimeInMillis()));
				} else {
					String month1 = mMonthDateFormat.format(new Date(
							firstDayStartDateTime.getTimeInMillis()));
					if (firstDayStartDateTime.get(Calendar.YEAR) != lastDayEndDateTime
							.get(Calendar.YEAR)) {
						String year1 = mYearDateFormat.format(new Date(
								firstDayStartDateTime.getTimeInMillis()));
						String year2 = mYearDateFormat.format(new Date(lastDayEndDateTime
								.getTimeInMillis()));
						String month2 = mMonthDateFormat.format(new Date(
								lastDayEndDateTime.getTimeInMillis()));
						result = month1 + ", " + year1 + "-" + month2 + ", " + year2;
					} else if (firstDayStartDateTime.get(Calendar.MONTH) != lastDayEndDateTime
							.get(Calendar.MONTH)) {
						String year1 = mYearDateFormat.format(new Date(
								firstDayStartDateTime.getTimeInMillis()));
						String month2 = mMonthDateFormat.format(new Date(
								lastDayEndDateTime.getTimeInMillis()));
						result = month1 + "-" + month2 + ", " + year1;
					} else {
						String year1 = mYearDateFormat.format(new Date(
								firstDayStartDateTime.getTimeInMillis()));
						result = month1 + ", " + year1;
					}
				}
			}
		} else {
			mLastRegisteredPrimaryItemPosition = -1;
		}
		if (mStateParameters.DaysCount == 1 && currentItemStartDateTime != null) {
			Calendar dateTimeToday = Calendar.getInstance();
			dateTimeToday.set(Calendar.HOUR_OF_DAY, 0);
			dateTimeToday.set(Calendar.MINUTE, 0);
			dateTimeToday.set(Calendar.SECOND, 0);
			dateTimeToday.set(Calendar.MILLISECOND, 0);
			if (dateTimeToday.getTimeInMillis() == currentItemStartDateTime
					.getTimeInMillis()) {
				Context context = getContext();
				Resources resources = context.getResources();
				int color = Helper
						.getIntegerPreferenceValue(
								context,
								R.string.preference_key_calendar_today_date_text_color,
								resources
										.getColor(R.color.calendar_today_date_text_color_default_value),
								null, null);
				mButtonPickupDate.setTextColor(color);
				color = Helper
						.getIntegerPreferenceValue(
								context,
								R.string.preference_key_calendar_today_date_highlight_color,
								resources
										.getColor(R.color.calendar_today_date_highlight_color_default_value),
								null, null);
				mButtonPickupDate.setBackgroundColor(color);
			} else {
				mButtonPickupDate.setTextColor(mDefaultTextColorStateList);
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					mButtonPickupDate.setBackground(mDefaultBackground.getConstantState()
							.newDrawable());
				} else {
					mButtonPickupDate.setBackgroundDrawable(mDefaultBackground
							.getConstantState().newDrawable());
				}
			}
		}
		mButtonPickupDate.setText(result);
		mLastRegisteredFirstVisibleItemPosition = firstVisibleItemPosition;
		mLastRegisteredLastVisibleItemPosition = lastVisibleItemPosition;
	}

	// OnPageChangeListener methods end
	public static class FragmentViewWeek2StateParameters implements Parcelable {
		long DayBeginningOnVirtualMiddleOffset;
		long FirstDay;
		int DaysCount;
		boolean ShowDayHeaders;
		boolean ShowNavigationBar;
		InformationUnitMatrix InformationUnitMatrixForTimeIntervalsMode;
		InformationUnitMatrix InformationUnitMatrixForTextMode;
		int BusinessHoursStartTime;
		int BusinessHoursEndTime;
		BusinessHoursTaskDisplayingPolicy BusinessHoursTaskDisplayingPolicy;
		HoursRulerDisplayingPolicy HoursRulerDisplayingPolicy;
		SyncPolicy SyncPolicy;
		MarkSyncNeededPolicy MarkSyncNeeded;
		boolean TasksNeedReload = false;
		boolean DaysCountChanged = false;
		boolean TaskOccurrencesDistributionsInvalidated = false;
		public float TextSize;
		public int Mode;

		public FragmentViewWeek2StateParameters(long dayBeginningOnVirtualMiddleOffset,
				long firstDay, int daysCount, boolean showDayHeaders,
				boolean showNavigationBar,
				InformationUnitMatrix informationUnitMatrixForTimeIntervalsMode,
				InformationUnitMatrix informationUnitMatrixForTextMode,
				int businessHoursStartHour, int businessHoursEndHour,
				BusinessHoursTaskDisplayingPolicy businessHoursTaskDisplayingPolicy,
				HoursRulerDisplayingPolicy hoursRulerDisplayingPolicy,
				SyncPolicy syncPolicy, MarkSyncNeededPolicy markSyncNeeded,
				float textSize, int mode) {
			DayBeginningOnVirtualMiddleOffset = dayBeginningOnVirtualMiddleOffset;
			FirstDay = firstDay;
			DaysCount = daysCount;
			ShowDayHeaders = showDayHeaders;
			ShowNavigationBar = showNavigationBar;
			InformationUnitMatrixForTimeIntervalsMode = informationUnitMatrixForTimeIntervalsMode;
			InformationUnitMatrixForTextMode = informationUnitMatrixForTextMode;
			BusinessHoursStartTime = businessHoursStartHour;
			BusinessHoursEndTime = businessHoursEndHour;
			BusinessHoursTaskDisplayingPolicy = businessHoursTaskDisplayingPolicy;
			HoursRulerDisplayingPolicy = hoursRulerDisplayingPolicy;
			SyncPolicy = syncPolicy;
			MarkSyncNeeded = markSyncNeeded;
			TextSize = textSize;
			Mode = mode;
		}

		protected FragmentViewWeek2StateParameters(Parcel in) {
			DayBeginningOnVirtualMiddleOffset = in.readLong();
			FirstDay = in.readLong();
			DaysCount = in.readInt();
			ShowDayHeaders = in.readByte() != 0x00;
			ShowNavigationBar = in.readByte() != 0x00;
			InformationUnitMatrixForTimeIntervalsMode = (InformationUnitMatrix) in
					.readValue(InformationUnitMatrix.class.getClassLoader());
			InformationUnitMatrixForTextMode = (InformationUnitMatrix) in
					.readValue(InformationUnitMatrix.class.getClassLoader());
			BusinessHoursStartTime = in.readInt();
			BusinessHoursEndTime = in.readInt();
			BusinessHoursTaskDisplayingPolicy = in.readByte() == 0x00 ? null
					: biz.advancedcalendar.greendao.Task.BusinessHoursTaskDisplayingPolicy
							.fromInt(in.readByte());
			HoursRulerDisplayingPolicy = in.readByte() == 0x00 ? null
					: biz.advancedcalendar.greendao.Task.HoursRulerDisplayingPolicy
							.fromInt(in.readByte());
			SyncPolicy = in.readByte() == 0x00 ? null
					: biz.advancedcalendar.greendao.Task.SyncPolicy
							.fromInt(in.readByte());
			MarkSyncNeeded = in.readByte() == 0x00 ? null : MarkSyncNeededPolicy
					.fromInt(in.readByte());
			TasksNeedReload = in.readByte() != 0x00;
			DaysCountChanged = in.readByte() != 0x00;
			TaskOccurrencesDistributionsInvalidated = in.readByte() != 0x00;
			TextSize = in.readFloat();
			Mode = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(DayBeginningOnVirtualMiddleOffset);
			dest.writeLong(FirstDay);
			dest.writeInt(DaysCount);
			dest.writeByte((byte) (ShowDayHeaders ? 0x01 : 0x00));
			dest.writeByte((byte) (ShowNavigationBar ? 0x01 : 0x00));
			dest.writeValue(InformationUnitMatrixForTimeIntervalsMode);
			dest.writeValue(InformationUnitMatrixForTextMode);
			dest.writeInt(BusinessHoursStartTime);
			dest.writeInt(BusinessHoursEndTime);
			if (BusinessHoursTaskDisplayingPolicy == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(BusinessHoursTaskDisplayingPolicy.getValue());
			}
			if (HoursRulerDisplayingPolicy == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(HoursRulerDisplayingPolicy.getValue());
			}
			if (SyncPolicy == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(SyncPolicy.getValue());
			}
			if (MarkSyncNeeded == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(MarkSyncNeeded.getValue());
			}
			dest.writeByte((byte) (TasksNeedReload ? 0x01 : 0x00));
			dest.writeByte((byte) (DaysCountChanged ? 0x01 : 0x00));
			dest.writeByte((byte) (TaskOccurrencesDistributionsInvalidated ? 0x01 : 0x00));
			dest.writeFloat(TextSize);
			dest.writeInt(Mode);
		}

		public static final Parcelable.Creator<FragmentViewWeek2StateParameters> CREATOR = new Parcelable.Creator<FragmentViewWeek2StateParameters>() {
			@Override
			public FragmentViewWeek2StateParameters createFromParcel(Parcel in) {
				return new FragmentViewWeek2StateParameters(in);
			}

			@Override
			public FragmentViewWeek2StateParameters[] newArray(int size) {
				return new FragmentViewWeek2StateParameters[size];
			}
		};
	}

	// @Override
	// public List<Task> getTasks() {
	// return mRetainedFragmentForFragmentViewWeek2Holder
	// .getRetainedFragmentForFragmentViewWeek2(getTag()).getTasks();
	// }
	@Override
	public void onPreExecute() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProgressUpdate(
			final CalculateAndDistributeTaskOccurrencesAsyncTaskResult asyncTaskResult) {
		int firstVisibleItemPosition = mLinearLayoutManager
				.findFirstVisibleItemPosition();
		int lastVisibleItemPosition = 0;
		if (firstVisibleItemPosition != -1) {
			lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
			mAdapter.purgeTaskOccurrencesDistributionsIfNeeded(firstVisibleItemPosition,
					lastVisibleItemPosition,
					mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions,
					mAdapter.visibleThreshold, mAdapter.startPurgeThreshold,
					mAdapter.mCacheOfTaskOccurrencesDistributions);
		}
		for (int i = 0; i < asyncTaskResult.Positions.length; i++) {
			mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions.put(
					asyncTaskResult.Positions[i],
					asyncTaskResult.TaskOccurrencesDistribution[i]);
			mAdapter.notifyItemChanged(asyncTaskResult.Positions[i]);
		}
	}

	@Override
	public void onCancelled() {
	}

	@Override
	public void onPostExecute(
			final CalculateAndDistributeTaskOccurrencesAsyncTaskResult asyncTaskResult) {
		int firstVisibleItemPosition = mLinearLayoutManager
				.findFirstVisibleItemPosition();
		Integer lastVisibleItemPosition = null;
		if (asyncTaskResult != null) {
			if (firstVisibleItemPosition != -1) {
				lastVisibleItemPosition = mLinearLayoutManager
						.findLastVisibleItemPosition();
				// if (mFragmentViewWeek2StateParameters.Mode == WeekViewCore.MODE_QUILTS)
				// {
				// mAdapter.purgeTaskOccurrencesDistributionsIfNeeded(
				// firstVisibleItemPosition,
				// lastVisibleItemPosition,
				// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForQuiltMode,
				// mAdapter.visibleThreshold, mAdapter.startPurgeThreshold,
				// mAdapter.mCacheOfTaskOccurrencesDistributions);
				// } else {
				// mAdapter.purgeTaskOccurrencesDistributionsIfNeeded(
				// firstVisibleItemPosition,
				// lastVisibleItemPosition,
				// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForListMode,
				// mAdapter.visibleThreshold, mAdapter.startPurgeThreshold,
				// mAdapter.mCacheOfTaskOccurrencesDistributions);
				// }
				mAdapter.purgeTaskOccurrencesDistributionsIfNeeded(
						firstVisibleItemPosition, lastVisibleItemPosition,
						mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions,
						mAdapter.visibleThreshold, mAdapter.startPurgeThreshold,
						mAdapter.mCacheOfTaskOccurrencesDistributions);
			}
			// if (mFragmentViewWeek2StateParameters.Mode == WeekViewCore.MODE_QUILTS) {
			// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForQuiltMode
			// .put(asyncTaskResult.Position,
			// asyncTaskResult.TaskOccurrencesDistribution);
			// } else {
			// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForListMode
			// .put(asyncTaskResult.Position,
			// asyncTaskResult.TaskOccurrencesDistribution);
			// }
			for (int i = 0; i < asyncTaskResult.Positions.length; i++) {
				mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions.put(
						asyncTaskResult.Positions[i],
						asyncTaskResult.TaskOccurrencesDistribution[i]);
				mAdapter.notifyItemChanged(asyncTaskResult.Positions[i]);
			}
		}
		// launch task again if needed
		if (!mRetainedFragment.isAsyncTaskRunning()) {
			if (firstVisibleItemPosition != -1) {
				if (lastVisibleItemPosition == null) {
					lastVisibleItemPosition = mLinearLayoutManager
							.findLastVisibleItemPosition();
				}
				ArrayList<Long> daysBordersList = new ArrayList<Long>();
				ArrayList<Integer> positionsList = new ArrayList<Integer>();
				Calendar calendar1 = Calendar.getInstance();
				calendar1
						.setTimeInMillis(mStateParameters.DayBeginningOnVirtualMiddleOffset);
				calendar1.add(Calendar.DAY_OF_YEAR, firstVisibleItemPosition
						- FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
				long dayStartDateTime = calendar1.getTimeInMillis();
				long dayStartDateTime1 = dayStartDateTime;
				for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
					TaskOccurrencesDistribution taskOccurrencesDistribution;
					// if (mFragmentViewWeek2StateParameters.Mode ==
					// WeekViewCore.MODE_QUILTS) {
					// taskOccurrencesDistribution =
					// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForQuiltMode
					// .get(i);
					// } else {
					// taskOccurrencesDistribution =
					// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForListMode
					// .get(i);
					// }
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(dayStartDateTime1);
					calendar.add(Calendar.DAY_OF_YEAR, 1);
					long dayEndDateTime = calendar.getTimeInMillis();
					taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
							.get(i);
					if (taskOccurrencesDistribution == null) {
						//
						daysBordersList.add(dayStartDateTime1);
						daysBordersList.add(dayEndDateTime);
						positionsList.add(i);
					}
					dayStartDateTime1 = dayEndDateTime;
					//
				}
				long[] daysBorders = new long[daysBordersList.size()];
				for (int i1 = 0; i1 < daysBorders.length; i1++) {
					daysBorders[i1] = daysBordersList.get(i1);
				}
				int[] positions = new int[positionsList.size()];
				for (int i1 = 0; i1 < positions.length; i1++) {
					positions[i1] = positionsList.get(i1);
				}
				if (daysBorders.length > 0) {
					if (FragmentViewWeek2.DEBUG) {
						Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
								"FragmentViewWeek2 onPostExecute launching AsyncTask.");
					}
					mRetainedFragment
							.launchAsyncTask(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] {new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
									positions, daysBorders, Helper
											.getFirstDayOfWeek(getContext()),
									mRetainedFragment.getTasks(), mStateParameters)});
				} else {
					int border = lastVisibleItemPosition + mAdapter.visibleThreshold;
					daysBordersList = new ArrayList<Long>();
					positionsList = new ArrayList<Integer>();
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(mStateParameters.DayBeginningOnVirtualMiddleOffset);
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					calendar.add(Calendar.DAY_OF_YEAR, lastVisibleItemPosition + 1
							- FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
					dayStartDateTime = calendar.getTimeInMillis();
					dayStartDateTime1 = dayStartDateTime;
					for (int i = lastVisibleItemPosition + 1; i <= border; i++) {
						TaskOccurrencesDistribution taskOccurrencesDistribution;
						// if (mFragmentViewWeek2StateParameters.Mode ==
						// WeekViewCore.MODE_QUILTS) {
						// taskOccurrencesDistribution =
						// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForQuiltMode
						// .get(i);
						// } else {
						// taskOccurrencesDistribution =
						// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForListMode
						// .get(i);
						// }
						Calendar calendar2 = Calendar.getInstance();
						calendar2.setTimeInMillis(dayStartDateTime1);
						calendar2.add(Calendar.DAY_OF_YEAR, 1);
						long dayEndDateTime = calendar2.getTimeInMillis();
						taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
								.get(i);
						if (taskOccurrencesDistribution == null) {
							//
							daysBordersList.add(dayStartDateTime1);
							daysBordersList.add(dayEndDateTime);
							positionsList.add(i);
						}
						dayStartDateTime1 = dayEndDateTime;
						//
					}
					daysBorders = new long[daysBordersList.size()];
					for (int i1 = 0; i1 < daysBorders.length; i1++) {
						daysBorders[i1] = daysBordersList.get(i1);
					}
					positions = new int[positionsList.size()];
					for (int i1 = 0; i1 < positions.length; i1++) {
						positions[i1] = positionsList.get(i1);
					}
					if (daysBorders.length > 0) {
						if (FragmentViewWeek2.DEBUG) {
							Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
									"FragmentViewWeek2 onPostExecute launching AsyncTask.");
						}
						mRetainedFragment
								.launchAsyncTask(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] {new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
										positions, daysBorders, Helper
												.getFirstDayOfWeek(getContext()),
										mRetainedFragment.getTasks(), mStateParameters)});
					} else {
						border = firstVisibleItemPosition - mAdapter.visibleThreshold;
						daysBordersList = new ArrayList<Long>();
						positionsList = new ArrayList<Integer>();
						Calendar calendar2 = Calendar.getInstance();
						calendar2
								.setTimeInMillis(mStateParameters.DayBeginningOnVirtualMiddleOffset);
						calendar2.set(Calendar.HOUR_OF_DAY, 0);
						calendar2.set(Calendar.MINUTE, 0);
						calendar2.set(Calendar.SECOND, 0);
						calendar2.set(Calendar.MILLISECOND, 0);
						calendar2.add(Calendar.DAY_OF_YEAR, border
								- FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
						dayStartDateTime = calendar2.getTimeInMillis();
						dayStartDateTime1 = dayStartDateTime;
						for (int i = border; i < firstVisibleItemPosition; i++) {
							TaskOccurrencesDistribution taskOccurrencesDistribution;
							// if (mFragmentViewWeek2StateParameters.Mode ==
							// WeekViewCore.MODE_QUILTS) {
							// taskOccurrencesDistribution =
							// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForQuiltMode
							// .get(i);
							// } else {
							// taskOccurrencesDistribution =
							// mRetainedFragmentForFragmentViewWeek2.mCachedCalculatedTaskOccurrencesDistributionsForListMode
							// .get(i);
							// }
							Calendar calendar3 = Calendar.getInstance();
							calendar3.setTimeInMillis(dayStartDateTime1);
							calendar3.add(Calendar.DAY_OF_YEAR, 1);
							long dayEndDateTime = calendar3.getTimeInMillis();
							taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
									.get(i);
							if (taskOccurrencesDistribution == null) {
								//
								daysBordersList.add(dayStartDateTime1);
								daysBordersList.add(dayEndDateTime);
								positionsList.add(i);
							}
							dayStartDateTime1 = dayEndDateTime;
							//
						}
						daysBorders = new long[daysBordersList.size()];
						for (int i1 = 0; i1 < daysBorders.length; i1++) {
							daysBorders[i1] = daysBordersList.get(i1);
						}
						positions = new int[positionsList.size()];
						for (int i1 = 0; i1 < positions.length; i1++) {
							positions[i1] = positionsList.get(i1);
						}
						if (daysBorders.length > 0) {
							if (FragmentViewWeek2.DEBUG) {
								Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
										"FragmentViewWeek2 onPostExecute launching AsyncTask.");
							}
							mRetainedFragment
									.launchAsyncTask(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] {new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
											positions, daysBorders, Helper
													.getFirstDayOfWeek(getContext()),
											mRetainedFragment.getTasks(),
											mStateParameters)});
						}
					}
				}
			}
		}
	}
}
