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
import biz.advancedcalendar.fragments.FragmentViewMonth2Adapter.MonthWeekViewHolder;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewMonth2.CalculateAndDistributeTaskOccurrencesAsyncTaskArgument;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewMonth2.CalculateAndDistributeTaskOccurrencesAsyncTaskResult;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewMonth2.TaskCallbacks;
import biz.advancedcalendar.greendao.Task.BusinessHoursTaskDisplayingPolicy;
import biz.advancedcalendar.greendao.Task.HoursRulerDisplayingPolicy;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.DaysSelectedListener;
import biz.advancedcalendar.views.MonthRecyclerView;
import biz.advancedcalendar.views.MonthRecyclerView.CurrentMonthChangedListener;
import biz.advancedcalendar.views.MonthViewWeekDaysHeader;
import biz.advancedcalendar.views.MonthWeekView;
import biz.advancedcalendar.views.TaskClickedListener;
import biz.advancedcalendar.views.TaskOccurrencesDistribution;
import biz.advancedcalendar.views.TaskOccurrencesDistributionState;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentViewMonth2 extends Fragment implements OnDateSetListener,
		DaysSelectedListener, TaskCallbacks, CurrentMonthChangedListener {
	public interface RetainedFragmentForFragmentViewMonth2Holder {
		RetainedFragmentForFragmentViewMonth2 getRetainedFragmentForFragmentViewMonth2(
				String tag);
	}

	// private WeekViewDayHeaderClickedListener mDayClickedListener;
	private DaysSelectedListener mDaysSelectedListener;
	private TaskClickedListener mTaskClickedListener;
	MonthRecyclerView mRecyclerView;
	MonthViewWeekDaysHeader mMonthViewWeekDaysHeader;
	// private WeekViewCoreTimeRuler mWeekViewCoreTimeRuler;
	private ImageButton mButtonPrevious;
	private Button mButtonPickupDate;
	private ImageButton mButtonNext;
	private FragmentViewMonth2Adapter mAdapter;
	private SimpleDateFormat mMonthDateFormat = new SimpleDateFormat("MMMM",
			Locale.getDefault());
	private SimpleDateFormat mYearDateFormat = new SimpleDateFormat("yyyy",
			Locale.getDefault());
	// private DateFormat mDateFormat =
	// DateFormat.getDateInstance(DateFormat.MONTH_FIELD);
	private String mPendingBundleForDatePickerDialogKey = "mPendingBundleForDatePickerDialogKey";
	public static final String InitArgumentsKeyFirstDay = "biz.advancedcalendar.fragments.FragmentViewWeek.InitArgumentsKeyFirstDay";
	public static final String InitArgumentsKeyDaysCount = "biz.advancedcalendar.fragments.FragmentViewWeek.InitArgumentsKeyDaysCount";
	public static final String StateParametersKey = "biz.advancedcalendar.fragments.FragmentViewWeek.StateParametersKey";
	public static final String ScrollStateKey = "biz.advancedcalendar.fragments.FragmentViewWeek.ScrollStateKey";
	static final boolean DEBUG = false;
	static final boolean DEBUG5 = true;
	private static final boolean DEBUG4 = false;
	private static final boolean DEBUG2 = false;
	// private static final boolean DEBUG3 = false;
	protected static final String FragmentViewMonth2Debug = "FragmentViewMonth2Debug";
	protected static final String FragmentViewMonth2Debug2 = "FragmentViewMonth2Debug2";
	private static final int MIN_PAGING_VELOCITY_DIP_PER_SEC = 100;
	private static final int MAX_PAGING_VELOCITY_DIP_PER_SEC = 1000;
	// variables to be saved in onSaveInstanceState()
	private Bundle mPendingBundleForDatePickerDialog;
	private String mDatePickerDialogKey = "biz.advancedcalendar.fragments.FragmentViewWeek.DatePickerDialogKey";
	private RetainedFragmentForFragmentViewMonth2 mRetainedFragment;
	private TaskOccurrencesDistribution mPreallocatedTaskOccurrencesDistribution;
	// private List<Task> mTasks;
	private BroadcastReceiver mReceiver;
	StateParameters mStateParameters;
	LinearLayoutManager mLinearLayoutManager;
	Runnable mReconcileRunnable;
	protected Integer mLastRegisteredFirstVisibleItemPosition;
	protected int mLastRegisteredLastVisibleItemPosition;
	protected Long mLastRegisteredOnScrolledTime;
	private float mScaledMinPagingVelocity;
	private float mScaledMaxPagingVelocity;
	private float mDensity;
	private String mTag;
	private ColorStateList mDefaultTextColorStateList;
	private Drawable mDefaultBackground;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restoreState(savedInstanceState);
		mTag = getTag();
		Resources resources = getResources();
		mDensity = resources.getDisplayMetrics().density;
		mScaledMinPagingVelocity = mDensity
				* FragmentViewMonth2.MIN_PAGING_VELOCITY_DIP_PER_SEC;
		mScaledMaxPagingVelocity = mDensity
				* FragmentViewMonth2.MAX_PAGING_VELOCITY_DIP_PER_SEC;
		mPreallocatedTaskOccurrencesDistribution = new TaskOccurrencesDistribution(6);
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
			final FragmentActivity activity = getActivity();
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(
							CommonConstants.ACTION_CALENDARS_CHANGED)
							|| intent.getAction().equals(
									CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED)) {
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
					} else if (intent.getAction().equals(
							CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
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
					} else if (intent.getAction().equals(
							CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED)) {
						if (isAdded()) {
							mAdapter.invalidateTasks();
							int firstDayOfWeek = Helper.getFirstDayOfWeek(getActivity());
							mMonthViewWeekDaysHeader.setFirstDayOfWeek(firstDayOfWeek);
						} else {
							mStateParameters.TasksNeedReload = true;
						}
					} else if (intent.getAction().equals(
							CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED)) {
						// if (!isDetached()) {
						if (isAdded()) {
							mAdapter.invalidateTasks();
						} else {
							mStateParameters.TasksNeedReload = true;
						}
					} else if (intent
							.getAction()
							.equals(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED)) {
						mStateParameters.InformationUnitMatrix = Helper
								.createInformationUnitMatrix(
										context,
										R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode,
										R.string.information_unit_matrix_for_calendar_time_intervals_mode_default_value);
						// if (!isDetached()) {
						if (isAdded()) {
							mAdapter.invalidateTasks();
						} else {
							mStateParameters.TasksNeedReload = true;
						}
					} else if (intent
							.getAction()
							.equals(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED)) {
						mStateParameters.InformationUnitMatrix = Helper
								.createInformationUnitMatrix(
										context,
										R.string.preference_key_information_unit_matrix_for_calendar_text_mode,
										R.string.information_unit_matrix_for_calendar_text_mode_default_value);
						// if (!isDetached()) {
						if (isAdded()) {
							mAdapter.invalidateTasks();
						} else {
							mStateParameters.TasksNeedReload = true;
						}
					} else {
						Resources resources = context.getResources();
						if (intent.getAction().equals(
								CommonConstants.ACTION_BUSINESS_HOURS_START_TIME_CHANGED)) {
							mStateParameters.BusinessHoursStartHour = (int) Helper
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
								// mWeekViewCoreTimeRuler
								// .setBusinessHoursStartTime(mStateParameters.BusinessHoursStartHour);
								// TODO: Are the lines commented below needed?
								// mWeekViewCoreTimeRuler
								// .setAssignedTaskOccurrencesDistributionState(mAdapter
								// .getAssignedTaskOccurrencesDistributionState());
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else if (intent.getAction().equals(
								CommonConstants.ACTION_BUSINESS_HOURS_END_TIME_CHANGED)) {
							mStateParameters.BusinessHoursEndHour = (int) Helper
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
								// mWeekViewCoreTimeRuler
								// .setBusinessHoursEndTime(mStateParameters.BusinessHoursEndHour);
								// TODO: Are the lines commented below needed?
								// mWeekViewCoreTimeRuler
								// .setAssignedTaskOccurrencesDistributionState(mAdapter
								// .getAssignedTaskOccurrencesDistributionState());
							} else {
								mStateParameters.TasksNeedReload = true;
							}
						} else if (intent
								.getAction()
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
						// mFragmentViewWeek2StateParameters.HoursRulerDisplayingPolicy =
						// HoursRulerDisplayingPolicy
						// .fromInt((byte) Helper
						// .getIntegerPreferenceValueFromStringArray(
						// context,
						// R.string.preference_key_hours_ruler_displaying_policy,
						// R.array.hours_ruler_displaying_policy_values_array,
						// R.integer.hours_ruler_displaying_policy_default_value));
						// }
						//
						else if (intent
								.getAction()
								.equals(CommonConstants.ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED)) {
							if (getTag()
									.equals(intent
											.getStringExtra(CommonConstants.INTENT_EXTRA_TAG))) {
								mStateParameters.CurrentlySelectedDay = intent
										.getLongExtra(
												CommonConstants.INTENT_EXTRA_DATETIME, 0);
								if (isAdded()) {
									mRecyclerView
											.scrollPositionToFirst(
													mAdapter.getItemPositionForDateTime(mStateParameters.CurrentlySelectedDay),
													null);
								}
							}
						} else if (intent.getAction().equals(
								CommonConstants.ACTION_SYNC_POLICY_CHANGED)
								|| intent.getAction().equals(
										CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED)) {
							SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
									.getIntegerPreferenceValueFromStringArray(context,
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
						} else if (intent.getAction().equals(Intent.ACTION_TIME_CHANGED)) {
							if (isAdded()) {
								if (Build.VERSION.SDK_INT >= 11) {
									activity.invalidateOptionsMenu();
								}
							}
						}
					}
				}
			};
			//
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_CALENDARS_CHANGED);
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
			LocalBroadcastManager.getInstance(activity.getApplicationContext())
					.registerReceiver(mReceiver, intentFilter);
			//
			if (savedInstanceState != null) {
				// because we just now registered receiver and savedInstanceState != null
				// we might miss some broadcasts so setup anew all settings might be
				// changed because of broadcast
				TextView tv = new TextView(activity);
				float dayNumberTextSize = tv.getTextSize() * 0.75f;
				SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
						.getIntegerPreferenceValueFromStringArray(activity,
								R.string.preference_key_sync_policy,
								R.array.sync_policy_values_array,
								R.integer.sync_policy_default_value));
				MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
						.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(
								activity, R.string.preference_key_mark_sync_needed,
								R.array.mark_sync_needed_values_array,
								R.integer.mark_sync_needed_default_value));
				syncPolicy = SyncPolicy.DO_NOT_SYNC;
				markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
				mStateParameters = new StateParameters(
						mStateParameters.DateTimeOnVirtualMiddleOffset,
						mStateParameters.CurrentMonthBeginning,
						mStateParameters.CurrentlySelectedDay,
						mStateParameters.ShowNavigationBar,
						(int) Helper
								.getLongPreferenceValue(
										activity,
										R.string.preference_key_business_hours_start_time,
										resources
												.getInteger(R.integer.business_hours_start_time_default_value),
										(long) resources
												.getInteger(R.integer.business_hours_start_time_min_value),
										(long) resources
												.getInteger(R.integer.business_hours_start_time_max_value)),
						(int) Helper
								.getLongPreferenceValue(
										activity,
										R.string.preference_key_business_hours_end_time,
										resources
												.getInteger(R.integer.business_hours_end_time_default_value),
										(long) resources
												.getInteger(R.integer.business_hours_end_time_min_value),
										(long) resources
												.getInteger(R.integer.business_hours_end_time_max_value)),
						BusinessHoursTaskDisplayingPolicy.fromInt((byte) Helper
								.getIntegerPreferenceValueFromStringArray(
										activity,
										R.string.preference_key_business_hours_task_displaying_policy,
										R.array.business_hours_task_displaying_policy_values_array,
										R.integer.business_hours_task_displaying_policy_default_value)),
						HoursRulerDisplayingPolicy.fromInt((byte) Helper
								.getIntegerPreferenceValueFromStringArray(
										activity,
										R.string.preference_key_hours_ruler_displaying_policy,
										R.array.hours_ruler_displaying_policy_values_array,
										R.integer.hours_ruler_displaying_policy_default_value)),
						syncPolicy, markSyncNeededPolicy, dayNumberTextSize,
						mStateParameters.Mode);
				mStateParameters.TasksNeedReload = true;
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		restoreState(savedInstanceState);
		View root = inflater.inflate(R.layout.fragment_month_view2, container, false);
		mRecyclerView = (MonthRecyclerView) root.findViewById(R.id.pager);
		mMonthViewWeekDaysHeader = (MonthViewWeekDaysHeader) root
				.findViewById(R.id.fragment_monthview_weekdaysheader);
		mRecyclerView.setScaledPagingVelocity(mScaledMinPagingVelocity,
				mScaledMaxPagingVelocity);
		mRecyclerView.setCurrentMonthChangedListener(this);
		// mRecyclerView.setOnGenericMotionListener(null);
		mRecyclerView.setOnHierarchyChangeListener(new OnHierarchyChangeListener() {
			@Override
			public void onChildViewRemoved(View parent, View child) {
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				int lastVisibleItemPosition = mLinearLayoutManager
						.findLastVisibleItemPosition();
				// Calendar calendar = Calendar.getInstance();
				// calendar.setTimeInMillis(((MonthWeekView) child)
				// .getFirstDayStartDateTime());
				TaskOccurrencesDistribution taskOccurrencesDistribution = ((MonthWeekView) child)
						.getTaskOccurrencesDistribution();
				MonthWeekViewHolder dayViewHolder = (MonthWeekViewHolder) mRecyclerView
						.getChildViewHolder(child);
				int layoutPosition = dayViewHolder.getLayoutPosition();
				// if (firstVisibleItemPosition != -1
				// && firstVisibleItemPosition <= layoutPosition
				// && layoutPosition <= lastVisibleItemPosition) {
				// updateDateText();
				// }
				if (firstVisibleItemPosition != -1
						&& firstVisibleItemPosition <= layoutPosition
						&& layoutPosition <= lastVisibleItemPosition
						&& taskOccurrencesDistribution != null) {
					if (mReconcileRunnable == null) {
						mReconcileRunnable = getReconcileRunnable();
						mRecyclerView.post(mReconcileRunnable);
					}
				}
				if (FragmentViewMonth2.DEBUG2) {
					// TaskOccurrencesDistributionState taskOccurrencesDistributionState =
					// ((MonthWeekView) child)
					// .getAssignedTaskOccurrencesDistributionState();
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "Removed: ");
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
							+ child.toString().substring(child.toString().length() - 4));
					// Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
					// + dt.toString().substring(0, 10));
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
							+ "taskOccurrencesDistribution: "
							+ taskOccurrencesDistribution);
					// Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
					// + "taskOccurrencesDistributionState: "
					// + taskOccurrencesDistributionState);
				}
			}

			@Override
			public void onChildViewAdded(View parent, View child) {
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				int lastVisibleItemPosition = mLinearLayoutManager
						.findLastVisibleItemPosition();
				// DateTime dt = new DateTime(((MonthWeekView) child)
				// .getFirstDayStartDateTime());
				TaskOccurrencesDistribution taskOccurrencesDistribution = ((MonthWeekView) child)
						.getTaskOccurrencesDistribution();
				MonthWeekViewHolder dayViewHolder = (MonthWeekViewHolder) mRecyclerView
						.getChildViewHolder(child);
				int layoutPosition = dayViewHolder.getLayoutPosition();
				// if (firstVisibleItemPosition == -1
				// || firstVisibleItemPosition <= layoutPosition
				// && layoutPosition <= lastVisibleItemPosition) {
				// updateDateText();
				// }
				if ((firstVisibleItemPosition == -1 || firstVisibleItemPosition <= layoutPosition
						&& layoutPosition <= lastVisibleItemPosition)
						&& taskOccurrencesDistribution != null) {
					if (mReconcileRunnable == null) {
						mReconcileRunnable = getReconcileRunnable();
						mRecyclerView.post(mReconcileRunnable);
					}
				}
				if (FragmentViewMonth2.DEBUG2) {
					// TaskOccurrencesDistributionState taskOccurrencesDistributionState =
					// ((MonthWeekView) child)
					// .getAssignedTaskOccurrencesDistributionState();
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "Added: ");
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
							+ child.toString().substring(child.toString().length() - 4));
					// Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
					// + dt.toString().substring(0, 10));
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
							+ "taskOccurrencesDistribution: "
							+ taskOccurrencesDistribution);
					// Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug2, "  "
					// + "taskOccurrencesDistributionState: "
					// + taskOccurrencesDistributionState);
				}
			}
		});
		mRecyclerView.setHasFixedSize(true);
		//
		// mWeekViewCoreTimeRuler = (biz.advancedcalendar.views.WeekViewCoreTimeRuler)
		// root
		// .findViewById(R.id.weekviewcoretimeruler);
		//
		mButtonPrevious = (ImageButton) root
				.findViewById(R.id.fragment_monthview_button_previous);
		mButtonPrevious.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				if (firstVisibleItemPosition != -1) {
					// mStateParameters.CurrentMonthBeginning = new DateTime(
					// mStateParameters.CurrentMonthBeginning).minusMonths(1)
					// .getMillis();
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(mStateParameters.CurrentMonthBeginning);
					calendar.add(Calendar.MONTH, -1);
					mRecyclerView.smoothScrollToPosition(mAdapter
							.getItemPositionForDateTime(calendar.getTimeInMillis()));
				}
			}
		});
		mButtonPickupDate = (Button) root
				.findViewById(R.id.fragment_monthview_button_pickup_date);
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
					calendar.setTimeInMillis(mStateParameters.CurrentMonthBeginning);
					break;
				case TODAY:
					break;
				}
				mPendingBundleForDatePickerDialog = new Bundle();
				mPendingBundleForDatePickerDialog.putInt(CommonConstants.CALLER_ID,
						R.id.fragment_weekview_button_pickup_date);
				DatePickerDialog dpd = DatePickerDialog.newInstance(
						FragmentViewMonth2.this, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				dpd.show(getFragmentManager(), mDatePickerDialogKey);
			}
		});
		mButtonNext = (ImageButton) root
				.findViewById(R.id.fragment_monthview_button_next);
		mButtonNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int lastVisibleItemPosition = mLinearLayoutManager
						.findLastVisibleItemPosition();
				if (lastVisibleItemPosition != -1) {
					// mStateParameters.CurrentMonthBeginning = new DateTime(
					// mStateParameters.CurrentMonthBeginning).plusMonths(1)
					// .getMillis();
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(mStateParameters.CurrentMonthBeginning);
					calendar.add(Calendar.MONTH, 1);
					mRecyclerView.smoothScrollToPosition(mAdapter
							.getItemPositionForDateTime(calendar.getTimeInMillis()) + 5);
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
		mRetainedFragment = ((RetainedFragmentForFragmentViewMonth2Holder) activity)
				.getRetainedFragmentForFragmentViewMonth2(getTag());
		if (mStateParameters.TasksNeedReload) {
			mRetainedFragment.invalidateTasks();
			mStateParameters.TasksNeedReload = false;
		}
		mRetainedFragment.setCallbacks(this);
		setHasOptionsMenu(true);
		mTaskClickedListener = (TaskClickedListener) activity;
		mDaysSelectedListener = (DaysSelectedListener) activity;
		// mDayClickedListener = (WeekViewDayHeaderClickedListener) activity;
		mLinearLayoutManager = new LinearLayoutManager(activity,
				LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(mLinearLayoutManager);
		mRecyclerView.setDaysSelectedListener(mDaysSelectedListener);
		mRecyclerView.setScaledPagingVelocity(mScaledMinPagingVelocity,
				mScaledMaxPagingVelocity);
		mRecyclerView.setCurrentMonthChangedListener(this);
		mAdapter = new FragmentViewMonth2Adapter(this, mRetainedFragment,
				mTaskClickedListener, mDaysSelectedListener, mRecyclerView);
		mAdapter.setDayNumberTextSize(mStateParameters.DayNumberTextSize);
		mAdapter.setDayClickedListener(mDaysSelectedListener);
		mAdapter.setMarkSyncParameters(mStateParameters.SyncPolicy,
				mStateParameters.MarkSyncNeeded);
		mAdapter.setMode(mStateParameters.Mode);
		mAdapter.setCurrentMonth(mStateParameters.CurrentMonthBeginning);
		mRecyclerView.post(new Runnable() {
			@Override
			public void run() {
				if (isAdded()) {
					List<Integer> dayHeadersCoords = mAdapter
							.getDayHeadersCoords(mRecyclerView.getWidth());
					int firstDayOfWeek = Helper.getFirstDayOfWeek(getActivity());
					mMonthViewWeekDaysHeader.init(firstDayOfWeek, dayHeadersCoords);
				}
			}
		});
		mRecyclerView.setAdapter(mAdapter);
		// mRecyclerView.scrollToPosition(FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET);
		mRecyclerView.scrollToPosition(mAdapter
				.getItemPositionForDateTime(mStateParameters.CurrentMonthBeginning));
		//
		// mWeekViewCoreTimeRuler.setTextSize(mStateParameters.TextSize);
		// mWeekViewCoreTimeRuler
		// .setBusinessHoursStartTime(mStateParameters.BusinessHoursStartHour);
		// mWeekViewCoreTimeRuler
		// .setBusinessHoursEndTime(mStateParameters.BusinessHoursEndHour);
		// mWeekViewCoreTimeRuler.setAssignedTaskOccurrencesDistributionState(mAdapter
		// .getAssignedTaskOccurrencesDistributionState());
		// //
		// if (mStateParameters.Mode == WeekViewCore.MODE_WINDOWS) {
		// mWeekViewCoreTimeRuler.setVisibility(View.VISIBLE);
		// } else {
		// mWeekViewCoreTimeRuler.setVisibility(View.GONE);
		// }
		//
		updateCurrentMonth(mStateParameters.CurrentMonthBeginning);
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
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_GOTO_TODAY,
				Menu.FIRST + 100, getResources().getString(R.string.action_goto_today));
		Calendar calendar = Calendar.getInstance();
		menuItem.setIcon(CommonConstants.DAY_OF_MONTH_ICON_RESOURCES[calendar
				.get(Calendar.DAY_OF_MONTH) - 1]);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		// if (mStateParameters.Mode == WeekViewCore.MODE_QUILTS) {
		// menuItem = menu.add(
		// Menu.NONE,
		// CommonConstants.MENU_ID_WEEKVIEW_MODE,
		// Menu.FIRST + 100,
		// getResources().getString(
		// R.string.action_weekview_toggle_display_mode_list));
		// menuItem.setIcon(R.drawable.ic_view_list_black_24dp);
		// MenuItemCompat
		// .setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		// } else {
		// menuItem = menu.add(
		// Menu.NONE,
		// CommonConstants.MENU_ID_WEEKVIEW_MODE,
		// Menu.FIRST + 100,
		// getResources().getString(
		// R.string.action_weekview_toggle_display_mode_time_intervals));
		// menuItem.setIcon(R.drawable.ic_view_quilt_black_24dp);
		// MenuItemCompat
		// .setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		// }
		//
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
		savedInstanceState.putParcelable(FragmentViewMonth2.StateParametersKey,
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
		if (true/* || getActivity().isFinishing() */) {
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
			int itemPositionForDateTime = FragmentViewMonth2Adapter.VIRTUAL_MIDDLE_OFFSET;
			int firstVisibleItemPosition = mLinearLayoutManager
					.findFirstVisibleItemPosition();
			if (firstVisibleItemPosition != -1) {
				int delta = itemPositionForDateTime - firstVisibleItemPosition;
				int smoothScrollThreshold = 5;
				if (Math.abs(delta) > smoothScrollThreshold) {
					mStateParameters.CurrentMonthBeginning = mStateParameters.DateTimeOnVirtualMiddleOffset;
					mAdapter.setCurrentMonth(mStateParameters.DateTimeOnVirtualMiddleOffset);
					mRecyclerView.scrollPositionToFirst(itemPositionForDateTime,
							firstVisibleItemPosition);
				} else {
					mRecyclerView.smoothScrollToMonth(
							mStateParameters.DateTimeOnVirtualMiddleOffset,
							firstVisibleItemPosition);
				}
			}
			return true;
		case CommonConstants.MENU_ID_WEEKVIEW_MODE:
			if (mStateParameters.Mode == CommonConstants.CALENDAR_MODE_TIME_INTERVALS) {
				mStateParameters.Mode = CommonConstants.CALENDAR_MODE_TEXT;
				item.setIcon(R.drawable.ic_view_quilt_black_24dp);
				item.setTitle(R.string.action_weekview_toggle_display_mode_time_intervals);
				// mWeekViewCoreTimeRuler.setVisibility(View.GONE);
			} else {
				mStateParameters.Mode = CommonConstants.CALENDAR_MODE_TIME_INTERVALS;
				item.setIcon(R.drawable.ic_view_list_black_24dp);
				item.setTitle(R.string.action_weekview_toggle_display_mode_list);
				// mWeekViewCoreTimeRuler.setVisibility(View.VISIBLE);
			}
			if (mStateParameters.Mode != CommonConstants.CALENDAR_MODE_TEXT) {
				mStateParameters.InformationUnitMatrix = Helper
						.createInformationUnitMatrix(
								context,
								R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode,
								R.string.information_unit_matrix_for_calendar_time_intervals_mode_default_value);
			} else {
				mStateParameters.InformationUnitMatrix = Helper
						.createInformationUnitMatrix(
								context,
								R.string.preference_key_information_unit_matrix_for_calendar_text_mode,
								R.string.information_unit_matrix_for_calendar_text_mode_default_value);
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
			mAdapter.setInformationUnitMatrix(mStateParameters.InformationUnitMatrix);
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
						.getParcelable(FragmentViewMonth2.StateParametersKey);
				mPendingBundleForDatePickerDialog = (Bundle) savedInstanceState
						.getParcelable(mPendingBundleForDatePickerDialogKey);
			}
		} else if (mStateParameters == null) {
			long currentMonthBeginning;
			currentMonthBeginning = getArguments().getLong(
					FragmentViewMonth2.InitArgumentsKeyFirstDay);
			FragmentActivity context = getActivity();
			TextView tv = new TextView(context);
			float dayNumberTextSize = tv.getTextSize() * 0.75f;
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
			mStateParameters = new StateParameters(
					currentMonthBeginning,
					currentMonthBeginning,
					currentMonthBeginning,
					true,
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
					syncPolicy, markSyncNeededPolicy, dayNumberTextSize, mTag
							.equals(FragmentTags.DAY.name()) ? Helper
							.getIntegerPreferenceValue(context,
									R.string.preference_key_mode_for_day,
									CommonConstants.CALENDAR_MODE_TIME_INTERVALS, null,
									null)
							: mTag.equals(FragmentTags.WEEK.name()) ? Helper
									.getIntegerPreferenceValue(context,
											R.string.preference_key_mode_for_week,
											CommonConstants.CALENDAR_MODE_TIME_INTERVALS,
											null, null)
									: CommonConstants.CALENDAR_MODE_TIME_INTERVALS);
		}
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, 1, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		int itemPositionForDateTime = mAdapter.getItemPositionForDateTime(calendar
				.getTimeInMillis());
		int firstVisibleItemPosition = mLinearLayoutManager
				.findFirstVisibleItemPosition();
		if (firstVisibleItemPosition != -1) {
			int delta = itemPositionForDateTime - firstVisibleItemPosition;
			int smoothScrollThreshold = 5;
			if (delta < -smoothScrollThreshold) {
				mStateParameters.CurrentMonthBeginning = mStateParameters.DateTimeOnVirtualMiddleOffset;
				mAdapter.setCurrentMonth(mStateParameters.DateTimeOnVirtualMiddleOffset);
				mRecyclerView.scrollToPosition(itemPositionForDateTime);
			} else if (delta <= 0) {
				mRecyclerView.smoothScrollToPosition(itemPositionForDateTime);
			} else if (delta <= smoothScrollThreshold) {
				mRecyclerView.smoothScrollToPosition(itemPositionForDateTime + 5);
			} else {
				mStateParameters.CurrentMonthBeginning = mStateParameters.DateTimeOnVirtualMiddleOffset;
				mAdapter.setCurrentMonth(mStateParameters.DateTimeOnVirtualMiddleOffset);
				mRecyclerView.scrollToPosition(itemPositionForDateTime + 5);
			}
		}
	}

	Runnable getReconcileRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				if (FragmentViewMonth2.DEBUG4) {
					Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
							"inside ReconcileRunnable: isAdded() == " + isAdded());
				}
				if (!isAdded()) {
					return;
				}
				int firstVisibleItemPosition = mLinearLayoutManager
						.findFirstVisibleItemPosition();
				if (firstVisibleItemPosition != -1) {
					TaskOccurrencesDistributionState reconciledTaskOccurrencesDistributionState = mAdapter
							.calculateReconciledTaskOccurrencesDistributionState();
					TaskOccurrencesDistributionState assignedTaskOccurrencesDistributionState = mAdapter
							.getAssignedTaskOccurrencesDistributionState();
					if (FragmentViewMonth2.DEBUG) {
						Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
								"mAction Runnable"
										+ "\t reconciledTaskOccurrencesDistributionState: "
										+ reconciledTaskOccurrencesDistributionState
												.toString()
										+ "\t assignedTaskOccurrencesDistributionState: "
										+ assignedTaskOccurrencesDistributionState
												.toString());
					}
					if (assignedTaskOccurrencesDistributionState == null
							|| !assignedTaskOccurrencesDistributionState
									.equals(reconciledTaskOccurrencesDistributionState)) {
						// mWeekViewCoreTimeRuler
						// .setAssignedTaskOccurrencesDistributionState(reconciledTaskOccurrencesDistributionState);
						mAdapter.setAssignedTaskOccurrencesDistributionState(reconciledTaskOccurrencesDistributionState);
					}
					// MonthWeekViewHolder dayViewHolder = (MonthWeekViewHolder)
					// mRecyclerView
					// .findViewHolderForLayoutPosition(firstVisibleItemPosition);
					// Integer weekViewCoreTop =
					// mWeekViewCoreTimeRuler.getWeekViewCoreTop();
					// if (weekViewCoreTop == null
					// || weekViewCoreTop != dayViewHolder.mWeekView
					// .getWeekViewCoreTop()) {
					// mWeekViewCoreTimeRuler.setWeekViewCoreTop(dayViewHolder.mWeekView
					// .getWeekViewCoreTop());
					// }
					// Integer weekViewCoreHoursDrawingTop = mWeekViewCoreTimeRuler
					// .getWeekViewCoreHoursDrawingTop();
					// if (weekViewCoreHoursDrawingTop == null
					// || weekViewCoreHoursDrawingTop != dayViewHolder.mWeekView
					// .getWeekViewCoreHoursDrawingTop()) {
					// mWeekViewCoreTimeRuler
					// .setWeekViewCoreHoursDrawingTop(dayViewHolder.mWeekView
					// .getWeekViewCoreHoursDrawingTop());
					// }
					// Integer weekViewCoreTop =
					// mWeekViewCoreTimeRuler.getWeekViewCoreTop();
					// Integer weekViewCoreTopToSet = dayViewHolder.mWeekView
					// .getWeekViewCoreTop();
					// if (weekViewCoreTop != null
					// && weekViewCoreTop == weekViewCoreTopToSet) {
					// weekViewCoreTopToSet = null;
					// }
					// if (weekViewCoreTopToSet != null) {
					// mWeekViewCoreTimeRuler.setWeekViewCoreTop(weekViewCoreTopToSet);
					// }
					// Integer weekViewCoreHoursDrawingTop = mWeekViewCoreTimeRuler
					// .getWeekViewCoreHoursDrawingTop();
					// Integer weekViewCoreHoursDrawingTopToSet = dayViewHolder.mWeekView
					// .getWeekViewCoreHoursDrawingTop();
					// if (weekViewCoreHoursDrawingTop != null
					// && weekViewCoreHoursDrawingTop == weekViewCoreHoursDrawingTopToSet)
					// {
					// weekViewCoreHoursDrawingTopToSet = null;
					// }
					// if (weekViewCoreHoursDrawingTopToSet != null) {
					// mWeekViewCoreTimeRuler
					// .setWeekViewCoreHoursDrawingTop(weekViewCoreHoursDrawingTopToSet);
					// }
				}
				mReconcileRunnable = null;
			}
		};
	}

	private void updateCurrentMonth(long currentMonth) {
		String result = "";
		mStateParameters.CurrentMonthBeginning = currentMonth;
		mAdapter.setCurrentMonth(currentMonth);
		Date date = new Date(currentMonth);
		String monthStr = mMonthDateFormat.format(date);
		String yearStr = mYearDateFormat.format(date);
		result = monthStr + ", " + yearStr;
		//
		Calendar calendar = Calendar.getInstance();
		CalendarHelper.toBeginningOfMonth(calendar);
		if (calendar.getTimeInMillis() == mStateParameters.CurrentMonthBeginning) {
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
		//
		mButtonPickupDate.setText(result);
	}

	// OnPageChangeListener methods end
	public static class StateParameters implements Parcelable {
		long DateTimeOnVirtualMiddleOffset;
		long CurrentMonthBeginning;
		long CurrentlySelectedDay;
		boolean ShowNavigationBar;
		InformationUnitMatrix InformationUnitMatrix;
		int BusinessHoursStartHour;
		int BusinessHoursEndHour;
		BusinessHoursTaskDisplayingPolicy BusinessHoursTaskDisplayingPolicy;
		HoursRulerDisplayingPolicy HoursRulerDisplayingPolicy;
		SyncPolicy SyncPolicy;
		MarkSyncNeededPolicy MarkSyncNeeded;
		boolean TasksNeedReload = false;
		boolean TaskOccurrencesDistributionsInvalidated = false;
		public float DayNumberTextSize;
		public int Mode;

		public StateParameters(long dateTimeOnVirtualMiddleOffset,
				long currentMonthBeginning, long currentlySelectedDay,
				boolean showNavigationBar, int businessHoursStartHour,
				int businessHoursEndHour,
				BusinessHoursTaskDisplayingPolicy businessHoursTaskDisplayingPolicy,
				HoursRulerDisplayingPolicy hoursRulerDisplayingPolicy,
				SyncPolicy syncPolicy, MarkSyncNeededPolicy markSyncNeeded,
				float textSize, int mode) {
			DateTimeOnVirtualMiddleOffset = dateTimeOnVirtualMiddleOffset;
			CurrentMonthBeginning = currentMonthBeginning;
			CurrentlySelectedDay = currentlySelectedDay;
			ShowNavigationBar = showNavigationBar;
			BusinessHoursStartHour = businessHoursStartHour;
			BusinessHoursEndHour = businessHoursEndHour;
			BusinessHoursTaskDisplayingPolicy = businessHoursTaskDisplayingPolicy;
			HoursRulerDisplayingPolicy = hoursRulerDisplayingPolicy;
			SyncPolicy = syncPolicy;
			MarkSyncNeeded = markSyncNeeded;
			DayNumberTextSize = textSize;
			Mode = mode;
		}

		protected StateParameters(Parcel in) {
			DateTimeOnVirtualMiddleOffset = in.readLong();
			CurrentMonthBeginning = in.readLong();
			CurrentlySelectedDay = in.readLong();
			ShowNavigationBar = in.readByte() != 0x00;
			BusinessHoursStartHour = in.readInt();
			BusinessHoursEndHour = in.readInt();
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
			TaskOccurrencesDistributionsInvalidated = in.readByte() != 0x00;
			DayNumberTextSize = in.readFloat();
			Mode = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(DateTimeOnVirtualMiddleOffset);
			dest.writeLong(CurrentMonthBeginning);
			dest.writeLong(CurrentlySelectedDay);
			dest.writeByte((byte) (ShowNavigationBar ? 0x01 : 0x00));
			dest.writeInt(BusinessHoursStartHour);
			dest.writeInt(BusinessHoursEndHour);
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
			dest.writeByte((byte) (TaskOccurrencesDistributionsInvalidated ? 0x01 : 0x00));
			dest.writeFloat(DayNumberTextSize);
			dest.writeInt(Mode);
		}

		public static final Parcelable.Creator<StateParameters> CREATOR = new Parcelable.Creator<StateParameters>() {
			@Override
			public StateParameters createFromParcel(Parcel in) {
				return new StateParameters(in);
			}

			@Override
			public StateParameters[] newArray(int size) {
				return new StateParameters[size];
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
		mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions.put(
				asyncTaskResult.Position, asyncTaskResult.TaskOccurrencesDistribution);
		mAdapter.notifyItemChanged(asyncTaskResult.Position);
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
				mAdapter.purgeTaskOccurrencesDistributionsIfNeeded(
						firstVisibleItemPosition, lastVisibleItemPosition,
						mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions,
						mAdapter.visibleThreshold, mAdapter.startPurgeThreshold,
						mAdapter.mCacheOfTaskOccurrencesDistributions);
			}
			mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
					.put(asyncTaskResult.Position,
							asyncTaskResult.TaskOccurrencesDistribution);
			mAdapter.notifyItemChanged(asyncTaskResult.Position);
		}
		// launch task again if needed
		if (!mRetainedFragment.isAsyncTaskRunning()) {
			if (firstVisibleItemPosition != -1) {
				if (lastVisibleItemPosition == null) {
					lastVisibleItemPosition = mLinearLayoutManager
							.findLastVisibleItemPosition();
				}
				int firstDayOfWeek = Helper.getFirstDayOfWeek(getActivity());
				List<CalculateAndDistributeTaskOccurrencesAsyncTaskArgument> calculateAndDistributeTaskOccurrencesAsyncTaskArguments = new ArrayList<CalculateAndDistributeTaskOccurrencesAsyncTaskArgument>();
				for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
					TaskOccurrencesDistribution taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
							.get(i);
					if (taskOccurrencesDistribution == null) {
						long dayStartDateTime = mAdapter.getBeginningOfWeekForPosition(i,
								firstDayOfWeek);
						long[] daysBorders = new long[7 * 2];
						long dayStartDateTime1 = dayStartDateTime;
						for (int i1 = 0; i1 < 7 * 2; i1 += 2) {
							Calendar calendar = Calendar.getInstance();
							calendar.setTimeInMillis(dayStartDateTime1);
							calendar.add(Calendar.DAY_OF_YEAR, 1);
							long dayEndDateTime = calendar.getTimeInMillis();
							daysBorders[i1] = dayStartDateTime1;
							daysBorders[i1 + 1] = dayEndDateTime;
							dayStartDateTime1 = dayEndDateTime;
						}
						calculateAndDistributeTaskOccurrencesAsyncTaskArguments
								.add(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
										i,
										daysBorders,
										mAdapter.getInstanceOfTaskOccurrencesDistribution(i),
										Helper.getFirstDayOfWeek(getContext()),
										mRetainedFragment.getTasks(), mStateParameters));
					}
				}
				if (calculateAndDistributeTaskOccurrencesAsyncTaskArguments.size() > 0) {
					if (FragmentViewMonth2.DEBUG) {
						Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
								"FragmentViewWeek2 onPostExecute launching AsyncTask.");
					}
					mRetainedFragment
							.launchAsyncTask(calculateAndDistributeTaskOccurrencesAsyncTaskArguments
									.toArray(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[calculateAndDistributeTaskOccurrencesAsyncTaskArguments
											.size()]));
				} else {
					int border = lastVisibleItemPosition
							+ Math.max(mAdapter.visibleThreshold, 6);
					for (int i = lastVisibleItemPosition; i <= border; i++) {
						TaskOccurrencesDistribution taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
								.get(i);
						if (taskOccurrencesDistribution == null) {
							long dayStartDateTime = mAdapter
									.getBeginningOfWeekForPosition(i, firstDayOfWeek);
							long[] daysBorders = new long[7 * 2];
							long dayStartDateTime1 = dayStartDateTime;
							for (int i1 = 0; i1 < 7 * 2; i1 += 2) {
								Calendar calendar = Calendar.getInstance();
								calendar.setTimeInMillis(dayStartDateTime1);
								calendar.add(Calendar.DAY_OF_YEAR, 1);
								long dayEndDateTime = calendar.getTimeInMillis();
								daysBorders[i1] = dayStartDateTime1;
								daysBorders[i1 + 1] = dayEndDateTime;
								dayStartDateTime1 = dayEndDateTime;
							}
							calculateAndDistributeTaskOccurrencesAsyncTaskArguments
									.add(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
											i,
											daysBorders,
											mAdapter.getInstanceOfTaskOccurrencesDistribution(i),
											Helper.getFirstDayOfWeek(getContext()),
											mRetainedFragment.getTasks(),
											mStateParameters));
						}
					}
					if (calculateAndDistributeTaskOccurrencesAsyncTaskArguments.size() > 0) {
						if (FragmentViewMonth2.DEBUG) {
							Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
									"FragmentViewWeek2 onPostExecute launching AsyncTask.");
						}
						mRetainedFragment
								.launchAsyncTask(calculateAndDistributeTaskOccurrencesAsyncTaskArguments
										.toArray(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[calculateAndDistributeTaskOccurrencesAsyncTaskArguments
												.size()]));
					} else {
						border = firstVisibleItemPosition
								- Math.max(mAdapter.visibleThreshold, 6);
						for (int i = firstVisibleItemPosition; i >= border; i--) {
							TaskOccurrencesDistribution taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
									.get(i);
							if (taskOccurrencesDistribution == null) {
								long dayStartDateTime = mAdapter
										.getBeginningOfWeekForPosition(i, firstDayOfWeek);
								long[] daysBorders = new long[7 * 2];
								long dayStartDateTime1 = dayStartDateTime;
								for (int i1 = 0; i1 < 7 * 2; i1 += 2) {
									Calendar calendar = Calendar.getInstance();
									calendar.setTimeInMillis(dayStartDateTime1);
									calendar.add(Calendar.DAY_OF_YEAR, 1);
									long dayEndDateTime = calendar.getTimeInMillis();
									daysBorders[i1] = dayStartDateTime1;
									daysBorders[i1 + 1] = dayEndDateTime;
									dayStartDateTime1 = dayEndDateTime;
								}
								calculateAndDistributeTaskOccurrencesAsyncTaskArguments
										.add(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
												i,
												daysBorders,
												mAdapter.getInstanceOfTaskOccurrencesDistribution(i),
												Helper.getFirstDayOfWeek(getContext()),
												mRetainedFragment.getTasks(),
												mStateParameters));
							}
						}
						if (calculateAndDistributeTaskOccurrencesAsyncTaskArguments
								.size() > 0) {
							if (FragmentViewMonth2.DEBUG) {
								Log.d(FragmentViewWeek2Adapter.FragmentViewWeek2Debug,
										"FragmentViewWeek2 onPostExecute launching AsyncTask.");
							}
							mRetainedFragment
									.launchAsyncTask(calculateAndDistributeTaskOccurrencesAsyncTaskArguments
											.toArray(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[calculateAndDistributeTaskOccurrencesAsyncTaskArguments
													.size()]));
						}
					}
				}
			}
		}
	}

	@Override
	public void onDaysSelected(long firstDay, long lastDay) {
		if (mDaysSelectedListener != null) {
			mDaysSelectedListener.onDaysSelected(firstDay, lastDay);
		}
	}

	@Override
	public void onCurrentMonthChanged(long currentMonth) {
		updateCurrentMonth(currentMonth);
	}
}
