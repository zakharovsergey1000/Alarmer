package biz.advancedcalendar.activities;

import biz.advancedcalendar.Global;
import biz.advancedcalendar.fragments.FragmentViewAgenda;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewAgenda;
import biz.advancedcalendar.views.WeekViewDayHeaderClickedListener;
import biz.advancedcalendar.views.TaskClickedListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.MultipartUtility;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityEditTask.ActivityEditTaskParcelableDataStore;
import biz.advancedcalendar.activities.accessories.OnTabSelectedListener;
import biz.advancedcalendar.activities.accessories.OnTabSelectedListener.SelectedIndexChangedListener;
import biz.advancedcalendar.activities.accessories.OnTabSelectedListener.TabTag;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;
import biz.advancedcalendar.fragments.FragmentViewAgenda.RetainedFragmentForFragmentViewAgendaHolder;
import biz.advancedcalendar.fragments.FragmentViewElapsedReminders;
import biz.advancedcalendar.fragments.FragmentViewMonth2;
import biz.advancedcalendar.fragments.FragmentViewMonth2.RetainedFragmentForFragmentViewMonth2Holder;
import biz.advancedcalendar.fragments.FragmentViewScheduledReminders;
import biz.advancedcalendar.fragments.FragmentViewTaskTree;
import biz.advancedcalendar.fragments.FragmentViewWeek2;
import biz.advancedcalendar.fragments.FragmentViewWeek2.RetainedFragmentForFragmentViewWeek2Holder;
import biz.advancedcalendar.fragments.RetainedFragmentForActivityMain;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewMonth2;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewWeek2;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.OnTaskSelectAction;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.greendao.Task.TaskEditMode;
import biz.advancedcalendar.greendao.TaskOccurrence;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.server.GetUserInfoResult;
import biz.advancedcalendar.server.ServerProvider;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.services.NotificationService;
import biz.advancedcalendar.sync.SyncService;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.DaysSelectedListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
//import javax.activation.MimetypesFileTypeMap;

public class ActivityMain extends AppCompatActivity implements
		SelectedIndexChangedListener, DaysSelectedListener, TaskClickedListener,
		WeekViewDayHeaderClickedListener, RetainedFragmentForFragmentViewAgendaHolder,
		RetainedFragmentForFragmentViewWeek2Holder,
		RetainedFragmentForFragmentViewMonth2Holder {
	private RetainedFragmentForActivityMain mRetainedFragmentForActivityMain;
	private RetainedFragmentForFragmentViewAgenda mRetainedFragmentForFragmentViewAgenda;
	private RetainedFragmentForFragmentViewWeek2 mRetainedFragmentForFragmentViewWeek2ForDay;
	private RetainedFragmentForFragmentViewWeek2 mRetainedFragmentForFragmentViewWeek2ForWeek;
	private RetainedFragmentForFragmentViewMonth2 mRetainedFragmentForFragmentViewMonth2;
	// private static final String BASE64_PUBLIC_KEY =
	// "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjMql1YwSmZbZtY1xp6QyJRFqyHbHYqgz2MKqGiSB7QpHdtoHinERaESyNU7Dircccn2n8LdxZeJzy/Hftx5TRPZ10YUJl1WrQlYtROLuy2SnQuxEifWkNlwmb2At/XRkawxoCMAMJ+UGhNIBKgy+6CAD9KVlbBDA3boWmCm3wO3A15ilOhIw8seFqtR1SMf5MTKOKti26U/JkTxqWvg8i1x2iqbyR7GDs9SNOqoJPXxVjLKHm4WqpxCX4wJbcVBNNZMB3NhTMSd+c6aa8HL6OwzAppl/4tZsVeWpcZeL6yowcUz+/ggUpCmMBXL/Xn+kL2/yZXhzFiU69VISs1TT4wIDAQAB";
	// Generate your own 20 random bytes, and put them here.
	// private static final byte[] SALT = new byte[] {56, 121, -11, 28, -98, -109, 28, 65,
	// 100, 38, 11, -92, 49, -71, -40, -118, 34, -11, 70, -7};
	private static final String RetainedFragmentForFragmentViewAgendaTag = "RetainedFragmentForFragmentViewAgendaTag";
	private static final String RetainedFragmentForFragmentViewWeek2ForDayTag = "RetainedFragmentForFragmentViewWeek2ForDayTag";
	private static final String RetainedFragmentForFragmentViewWeek2ForWeekTag = "RetainedFragmentForFragmentViewWeek2ForWeekTag";
	private static final String RetainedFragmentForFragmentViewMonth2Tag = "RetainedFragmentForFragmentViewMonth2Tag";
	private static final String RetainedFragmentForActivityMainTag = "RetainedFragmentForActivityMainTag";
	// private TextView mStatusText;
	// private Button mCheckLicenseButton;
	// private LicenseCheckerCallback mLicenseCheckerCallback;
	// private LicenseChecker mChecker;
	// A handler on the UI thread.
	// private Handler mHandler;
	private DrawerLayout mDrawerLayout;
	private Toolbar mToolbar;
	private HorizontalScrollView mHorizontalScrollViewCalendarsList;
	private LinearLayout mLinearLayoutCalendarsList;
	private LinearLayout mLinearLayoutDaysCountSelector;
	private TextView mTextViewDaysCountSelector;
	private SeekBar mSeekBarDaysCountSelector;
	// private CheckBox mCheckBoxCalendarsListSelectAllNone;
	private ImageButton mImageButtonCalendarsListSelectAll;
	private ImageButton mImageButtonCalendarsListSelectNone;
	private CharSequence mDrawerTitle;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mTitle;
	private NavigationView mNavigationView;
	private GetUserInfoTask mGetUserInfoTask = null;

	// static final int MENU_ID_ADD = Menu.FIRST;
	public enum FragmentTags {
		AGENDA, LABELS, CONTACTS, DIARY, TODO, DAY, WEEK, MONTH, ACTIVE_TASKS, COMPLETED_TASKS, FILES_MINE, FILES_SHARED, MESSAGES, REMINDERS_ELAPSED, REMINDERS_SCHEDULED, UPDATES,
	}

	private enum SavedStates {
		ACTIVITY_STATE, TAB_POSITION, TAB_POSITION_CALENDAR, TAB_POSITION_TASKS, TAB_POSITION_REMINDERS, TAB_POSITION_FILES, TAB_POSITION_MESSAGES2
	}

	public enum ActivityState {
		CALENDAR, TASKS, REMINDERS, LABELS, CONTACTS, FILES, DIARY, MESSAGES2
	}

	private ActivityState mActivityState;
	private int mSelectedTabIndex;
	private int mCalendarSelectedTabIndex;
	private Integer mTasksSelectedTabIndex;
	private Integer mRemindersSelectedTabIndex;
	private int mFilesSelectedTabIndex;
	private int mMessagesSelectedTabIndex2;
	private List<biz.advancedcalendar.greendao.Calendar> mCalendars;
	private HashSet<Long> mSelectedCalendars;
	private TabLayout mTabLayout;

	public static void launchTaskViewerOrEditor(Context context, long taskId, int tab) {
		OnTaskSelectAction onTaskSelectAction = OnTaskSelectAction.fromInt((short) Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_on_task_select_action,
						R.array.on_task_select_action_values_array,
						R.integer.on_task_select_action_default_value));
		switch (onTaskSelectAction) {
		case OPEN_TASK_IN_EDIT_MODE:
		default:
			TaskWithDependents taskWithDependents = DataProvider.getTaskWithDependents(
					null, context, taskId);
			if (taskWithDependents != null) {
				ActivityMain.launchTaskEditor2(context, new TaskWithDependentsUiData(
						taskWithDependents), TaskEditMode.EDIT, tab);
			}
			break;
		case OPEN_TASK_IN_VIEW_MODE:
			Intent intent = new Intent(context, ActivityViewTask.class);
			taskWithDependents = DataProvider
					.getTaskWithDependents(null, context, taskId);
			if (taskWithDependents != null) {
				TaskWithDependentsUiData taskWithDependentsUiData = new TaskWithDependentsUiData(
						taskWithDependents);
				RetainedFragmentForActivityViewTask activityViewTaskRetainedFragment = new RetainedFragmentForActivityViewTask();
				activityViewTaskRetainedFragment
						.setTaskWithDependentsUiData(taskWithDependentsUiData);
				activityViewTaskRetainedFragment.setTab(tab);
				intent.putExtra(CommonConstants.INIT_ARGUMENTS,
						activityViewTaskRetainedFragment);
				context.startActivity(intent);
			}
			break;
		}
	}

	public static void launchTaskEditor(Context context, Long taskId,
			TaskEditMode taskEditMode, int tab) {
		TaskWithDependents taskWithDependents;
		if (taskId != null) {
			taskWithDependents = DataProvider
					.getTaskWithDependents(null, context, taskId);
		} else {
			RecurrenceInterval.initializeValueBounds(context);
			RecurrenceInterval recurrenceInterval;
			if (taskEditMode == TaskEditMode.QUICK) {
				recurrenceInterval = RecurrenceInterval.ONE_TIME;
			} else {
				short timeUnitValue = (short) Helper
						.getIntegerPreferenceValueFromStringArray(context,
								R.string.preference_key_time_unit,
								R.array.time_unit_values_array,
								R.integer.time_unit_default_value);
				byte monthRecurrenceModeValue = (byte) Helper
						.getIntegerPreferenceValueFromStringArray(context,
								R.string.preference_key_month_recurrence_mode,
								R.array.month_recurrence_mode_values_array,
								R.integer.month_recurrence_mode_default_value);
				recurrenceInterval = RecurrenceInterval.fromTimeUnit(timeUnitValue,
						monthRecurrenceModeValue);
			}
			int timeUnitsCount = recurrenceInterval.getTimeUnitsCount();
			taskWithDependents = new TaskWithDependents(new Task(null, null, null, 0, 0,
					null, null, null, SyncStatus.SYNC_UP_REQUIRED.getValue(), null,
					Task.PRIORITY.MEDIUM.getValue(), null, null, null, null, null, false,
					(short) 0, null, false, 0, null, null, recurrenceInterval.getValue(),
					timeUnitsCount, null, null, null, null, null, null, null, null, null,
					null, null, null, null), new ArrayList<Reminder>(),
					new ArrayList<TaskOccurrence>());
		}
		if (taskWithDependents != null) {
			ActivityMain.launchTaskEditor2(context, new TaskWithDependentsUiData(
					taskWithDependents), taskEditMode, tab);
		}
	}

	public static void launchTaskEditor2(Context context,
			TaskWithDependentsUiData taskWithDependentsUiData, TaskEditMode taskEditMode,
			int tab) {
		UserInterfaceData userInterfaceData = new UserInterfaceData(context,
				taskWithDependentsUiData);
		ActivityEditTask.ActivityEditTaskParcelableDataStore activityEditTaskDataFragment = new ActivityEditTaskParcelableDataStore(
				taskWithDependentsUiData, userInterfaceData, taskEditMode, tab);
		Intent intent = new Intent(context, ActivityEditTask.class);
		intent.putExtra(CommonConstants.INIT_ARGUMENTS, activityEditTaskDataFragment);
		context.startActivity(intent);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			((Global) getApplicationContext()).updateDaoSession();
		}
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onCreate() thread: %s", Thread.currentThread()
							.getName()));
		}
		java.io.File database = getDatabasePath("AdvancedCalendar.db");
		if (!database.exists()) {
			// Database does not exist so finish this activity and launch
			// ActivitySplash
			Log.d(CommonConstants.DEBUG_TAG, "AdvancedCalendar.db Not Found");
			Intent intent = new Intent(this, ActivitySplash.class);
			startActivity(intent);
			finish();
			return;
		}
		Log.d(CommonConstants.DEBUG_TAG, "AdvancedCalendar.db Found");
		setContentView(R.layout.activity_main);
		// Set a Toolbar to replace the ActionBar.
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mTabLayout = (TabLayout) findViewById(R.id.tablayout);
		mTabLayout.setOnTabSelectedListener(new OnTabSelectedListener(this));
		mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		// find the retained fragment on activity restarts
		FragmentManager fm = getSupportFragmentManager();
		mRetainedFragmentForActivityMain = (RetainedFragmentForActivityMain) fm
				.findFragmentByTag(ActivityMain.RetainedFragmentForActivityMainTag);
		// create the fragment and data the first time
		if (mRetainedFragmentForActivityMain == null) {
			// Log.d(CommonConstants.DEBUG_TAG, "ActivityAlarm: dataFragment == null");
			DataProvider.runInTx(null, this, new Runnable() {
				@Override
				public void run() {
					List<ScheduledReminder> reminderList = DataProvider
							.getScheduledReminders(null, ActivityMain.this, null,
									new int[] {ScheduledReminder.State.SCHEDULED
											.getValue()});
					for (ScheduledReminder scheduledReminder : reminderList) {
						boolean scheduleRemindersOfCompletedTasks = CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
								.getIntegerPreferenceValueFromStringArray(
										ActivityMain.this,
										R.string.preference_key_reminder_behavior_for_completed_task,
										R.array.reminder_behavior_for_completed_task_values_array,
										R.integer.reminder_default_behavior_for_completed_task);
						if (!scheduleRemindersOfCompletedTasks) {
							Reminder reminder = DataProvider.getReminder(null,
									ActivityMain.this, scheduledReminder.getReminderId());
							if (reminder != null) {
								Task task = DataProvider.getTask(null, ActivityMain.this,
										reminder.getTaskId(), false);
								if (task.getIsCompleted()) {
									continue;
								}
							}
						}
						AlarmService.setAlarmForReminder(ActivityMain.this,
								scheduledReminder.getId(),
								scheduledReminder.getNextSnoozeDateTime(), this);
					}
				}
			});
			// add the fragment
			mRetainedFragmentForActivityMain = new RetainedFragmentForActivityMain();
			fm.beginTransaction()
					.add(mRetainedFragmentForActivityMain,
							ActivityMain.RetainedFragmentForActivityMainTag).commit();
			// dataFragment.LoadedGroups = new TreeMap<Integer, ExpandListGroup<Long,
			// Integer>>();
			// dataFragment.ExplicitlyCollapsedVirtualGroups = new ArrayList<Integer>();
			// dataFragment.Tasks = DataProvider.getActiveNotDeletedTasks(this);
			// dataFragment.DateTimeOnVirtualMiddleOffset = Calendar.getInstance()
			// .getTimeInMillis();
			// dataFragment.fragmentViewWeekData = fragmentViewWeekData;
		}
		//
		mRetainedFragmentForFragmentViewAgenda = (RetainedFragmentForFragmentViewAgenda) fm
				.findFragmentByTag(ActivityMain.RetainedFragmentForFragmentViewAgendaTag);
		// create the fragment and data the first time
		if (mRetainedFragmentForFragmentViewAgenda == null) {
			// add the fragment
			mRetainedFragmentForFragmentViewAgenda = new RetainedFragmentForFragmentViewAgenda();
			mRetainedFragmentForFragmentViewAgenda.setFragmentViewAgendaTag(
					FragmentTags.AGENDA.name());
			fm.beginTransaction()
					.add(mRetainedFragmentForFragmentViewAgenda,
							ActivityMain.RetainedFragmentForFragmentViewAgendaTag)
					.commit();
		}
		//
		mRetainedFragmentForFragmentViewWeek2ForDay = (RetainedFragmentForFragmentViewWeek2) fm
				.findFragmentByTag(ActivityMain.RetainedFragmentForFragmentViewWeek2ForDayTag);
		// create the fragment and data the first time
		if (mRetainedFragmentForFragmentViewWeek2ForDay == null) {
			// add the fragment
			mRetainedFragmentForFragmentViewWeek2ForDay = new RetainedFragmentForFragmentViewWeek2();
			fm.beginTransaction()
					.add(mRetainedFragmentForFragmentViewWeek2ForDay,
							ActivityMain.RetainedFragmentForFragmentViewWeek2ForDayTag)
					.commit();
		}
		//
		mRetainedFragmentForFragmentViewWeek2ForWeek = (RetainedFragmentForFragmentViewWeek2) fm
				.findFragmentByTag(ActivityMain.RetainedFragmentForFragmentViewWeek2ForWeekTag);
		// create the fragment and data the first time
		if (mRetainedFragmentForFragmentViewWeek2ForWeek == null) {
			// add the fragment
			mRetainedFragmentForFragmentViewWeek2ForWeek = new RetainedFragmentForFragmentViewWeek2();
			fm.beginTransaction()
					.add(mRetainedFragmentForFragmentViewWeek2ForWeek,
							ActivityMain.RetainedFragmentForFragmentViewWeek2ForWeekTag)
					.commit();
		}
		//
		mRetainedFragmentForFragmentViewMonth2 = (RetainedFragmentForFragmentViewMonth2) fm
				.findFragmentByTag(ActivityMain.RetainedFragmentForFragmentViewMonth2Tag);
		// create the fragment and data the first time
		if (mRetainedFragmentForFragmentViewMonth2 == null) {
			// add the fragment
			mRetainedFragmentForFragmentViewMonth2 = new RetainedFragmentForFragmentViewMonth2();
			fm.beginTransaction()
					.add(mRetainedFragmentForFragmentViewMonth2,
							ActivityMain.RetainedFragmentForFragmentViewMonth2Tag)
					.commit();
		}
		// setup mLinearLayoutCalendarsList
		mLinearLayoutDaysCountSelector = (LinearLayout) findViewById(R.id.activity_main_linearlayout_days_count_selector);
		mTextViewDaysCountSelector = (TextView) findViewById(R.id.activity_main_header_textview_days_count_selector);
		int maxValue = getResources().getInteger(R.integer.weekview_days_count_max_value);
		int daysCount = Helper
				.getIntegerPreferenceValue(
						this,
						getResources().getString(
								R.string.preference_key_days_count_for_weekview),
						getResources().getInteger(
								R.integer.weekview_days_count_default_value),
						getResources()
								.getInteger(R.integer.weekview_days_count_min_value),
						maxValue);
		String text = getResources().getString(
				R.string.fragment_view_week_days_count_selector_header_textview,
				"" + daysCount);
		mTextViewDaysCountSelector.setText(text);
		mSeekBarDaysCountSelector = (SeekBar) findViewById(R.id.activity_main_seekbar_days_count_selector);
		mSeekBarDaysCountSelector.setMax(maxValue - 2);
		mSeekBarDaysCountSelector.setProgress(daysCount - 2);
		mSeekBarDaysCountSelector
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onProgressChanged(SeekBar seekBar, int progress,
							boolean fromUser) {
						int daysCount = progress + 2;
						String text = getResources()
								.getString(
										R.string.fragment_view_week_days_count_selector_header_textview,
										"" + daysCount);
						mTextViewDaysCountSelector.setText(text);
						PreferenceManager
								.getDefaultSharedPreferences(ActivityMain.this)
								.edit()
								.putInt(getResources().getString(
										R.string.preference_key_days_count_for_weekview),
										daysCount).commit();
						Intent intent = new Intent(
								CommonConstants.ACTION_DAYS_COUNT_FOR_WEEKVIEW_CHANGED);
						intent.putExtra(CommonConstants.INTENT_EXTRA_TAG,
								FragmentTags.WEEK.name());
						LocalBroadcastManager.getInstance(ActivityMain.this)
								.sendBroadcast(intent);
					}
				});
		// setup mLinearLayoutCalendarsList
		mHorizontalScrollViewCalendarsList = (HorizontalScrollView) findViewById(R.id.activity_main_horizontalscrollview_calendars_list);
		mLinearLayoutCalendarsList = (LinearLayout) findViewById(R.id.activity_main_linearlayout_calendars_list);
		// mCheckBoxCalendarsListSelectAllNone = (CheckBox)
		// findViewById(R.id.checkbox_calendars_select_all_none);
		mImageButtonCalendarsListSelectAll = (ImageButton) findViewById(R.id.activity_main_linearlayout_calendars_list_imagebutton_select_all);
		mImageButtonCalendarsListSelectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int count = mLinearLayoutCalendarsList.getChildCount();
				for (int i = 0; i < count; i++) {
					CheckBox checkBox = (CheckBox) mLinearLayoutCalendarsList
							.getChildAt(i);
					checkBox.setChecked(true);
				}
			}
		});
		mImageButtonCalendarsListSelectNone = (ImageButton) findViewById(R.id.activity_main_linearlayout_calendars_list_imagebutton_select_none);
		mImageButtonCalendarsListSelectNone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int count = mLinearLayoutCalendarsList.getChildCount();
				for (int i = 0; i < count; i++) {
					CheckBox checkBox = (CheckBox) mLinearLayoutCalendarsList
							.getChildAt(i);
					checkBox.setChecked(false);
				}
			}
		});
		mCalendars = new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider.getCalendars(null, this));
		mCalendars.add(0, new biz.advancedcalendar.greendao.Calendar(null, null,
				getResources().getString(R.string.default_calendar_name)));
		mSelectedCalendars = new HashSet<Long>();
		Long[] selectedCalendars = Helper.getLongArray(ActivityMain.this, getResources()
				.getString(R.string.preference_key_selected_calendars), null);
		if (selectedCalendars == null) {
			for (biz.advancedcalendar.greendao.Calendar calendar : mCalendars) {
				mSelectedCalendars.add(calendar.getId());
			}
		} else {
			for (Long id : selectedCalendars) {
				mSelectedCalendars.add(id);
			}
		}
		mLinearLayoutCalendarsList.removeAllViews();
		mLinearLayoutCalendarsList.setBaselineAligned(false);
		LayoutInflater layoutInflater = getLayoutInflater();
		for (biz.advancedcalendar.greendao.Calendar calendar : mCalendars) {
			// CheckBox checkBox = new CheckBox(new ContextThemeWrapper(this,
			// R.style.AppTheme));
			CheckBox checkBox = (CheckBox) layoutInflater.inflate(R.layout.checkbox,
					mLinearLayoutCalendarsList, false);
			checkBox.setTag(calendar.getId());
			checkBox.setText(calendar.getId() != null ? calendar.getName()
					: getResources().getString(R.string.default_calendar_name));
			checkBox.setChecked(mSelectedCalendars.contains(calendar.getId()));
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					Long calendarId = (Long) buttonView.getTag();
					if (isChecked) {
						mSelectedCalendars.add(calendarId);
					} else {
						mSelectedCalendars.remove(calendarId);
					}
					Long[] selectedCalendars = new Long[mSelectedCalendars.size()];
					mSelectedCalendars.toArray(selectedCalendars);
					Helper.setLongArray(
							ActivityMain.this,
							getResources().getString(
									R.string.preference_key_selected_calendars),
							selectedCalendars);
					LocalBroadcastManager
							.getInstance(ActivityMain.this)
							.sendBroadcast(
									new Intent(
											CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED));
				}
			});
			mLinearLayoutCalendarsList.addView(checkBox);
		}
		// mLinearLayout = (LinearLayout)
		// findViewById(R.id.activity_main_content_linearlayout);
		// setup Drawer
		mTitle = mDrawerTitle = getTitle();
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mNavigationView = (NavigationView) findViewById(R.id.nvView);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		mNavigationView
				.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
					@Override
					public boolean onNavigationItemSelected(MenuItem menuItem) {
						selectDrawerItem(menuItem);
						return true;
					}
				});
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				// code here will execute once the drawer is opened
				getSupportActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				// Code here will execute once drawer is closed
				getSupportActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		if (savedInstanceState == null) {
			int launchMode = getIntent().getIntExtra(
					CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE,
					CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_DAY);
			// launchMode = CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_AGENDA;
			// launchMode = CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_DAY;
			// launchMode = CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_ELAPSED;
			// launchMode = CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_SCHEDULED;
			// launchMode = CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_TASKS;
			if (mActivityState == null) {
				switch (launchMode) {
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_DAY:
					mActivityState = ActivityState.CALENDAR;
					break;
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_AGENDA:
					mActivityState = ActivityState.CALENDAR;
					break;
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_ELAPSED:
					mActivityState = ActivityState.REMINDERS;
					break;
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_SCHEDULED:
					mActivityState = ActivityState.REMINDERS;
					break;
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_TASKS:
					mActivityState = ActivityState.TASKS;
					break;
				default:
					mActivityState = ActivityState.CALENDAR;
					break;
				}
			}
			if (mTasksSelectedTabIndex == null) {
				switch (launchMode) {
				default:
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_DAY:
					mTasksSelectedTabIndex = 0;
					break;
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_AGENDA:
					mTasksSelectedTabIndex = 3;
					break;
				}
			}
			if (mRemindersSelectedTabIndex == null) {
				switch (launchMode) {
				default:
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_ELAPSED:
					mRemindersSelectedTabIndex = 0;
					break;
				case CommonConstants.ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_SCHEDULED:
					mRemindersSelectedTabIndex = 1;
					break;
				}
			}
			switch (mActivityState) {
			case CALENDAR:
				selectItem(mActivityState, mCalendarSelectedTabIndex);
				break;
			default:
			case TASKS:
				selectItem(mActivityState, mTasksSelectedTabIndex);
				break;
			case REMINDERS:
				selectItem(mActivityState, mRemindersSelectedTabIndex);
				break;
			}
		} else {
			int state = savedInstanceState.getInt(SavedStates.ACTIVITY_STATE.name());
			mActivityState = ActivityState.values()[state];
			mSelectedTabIndex = savedInstanceState
					.getInt(SavedStates.TAB_POSITION.name());
			mCalendarSelectedTabIndex = savedInstanceState
					.getInt(SavedStates.TAB_POSITION_CALENDAR.name());
			mTasksSelectedTabIndex = savedInstanceState
					.getInt(SavedStates.TAB_POSITION_TASKS.name());
			mRemindersSelectedTabIndex = savedInstanceState
					.getInt(SavedStates.TAB_POSITION_REMINDERS.name());
			mFilesSelectedTabIndex = savedInstanceState
					.getInt(SavedStates.TAB_POSITION_FILES.name());
			mMessagesSelectedTabIndex2 = savedInstanceState
					.getInt(SavedStates.TAB_POSITION_MESSAGES2.name());
			switch (mActivityState) {
			case CALENDAR:
				selectItem(mActivityState, mCalendarSelectedTabIndex);
				break;
			case TASKS:
				selectItem(mActivityState, mTasksSelectedTabIndex);
				break;
			case REMINDERS:
				selectItem(mActivityState, mRemindersSelectedTabIndex);
				break;
			case CONTACTS:
				selectItem(mActivityState, 0);
				break;
			case LABELS:
				selectItem(mActivityState, 0);
				break;
			case FILES:
				selectItem(mActivityState, mFilesSelectedTabIndex);
				break;
			case DIARY:
				selectItem(mActivityState, 0);
				break;
			case MESSAGES2:
				selectItem(mActivityState, mMessagesSelectedTabIndex2);
				break;
			}
		}
	}

	@Override
	public void onPostResume() {
		super.onPostResume();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onPostResume() thread: %s", Thread.currentThread()
							.getName()));
		}
		if (mRetainedFragmentForActivityMain.selectItemIsPending) {
			mRetainedFragmentForActivityMain.selectItemIsPending = false;
			selectItem(mRetainedFragmentForActivityMain.selectItemArg1,
					mRetainedFragmentForActivityMain.selectItemArg2);
		}
	}

	@Override
	public void onNewIntent(final Intent newIntent) {
		super.onNewIntent(newIntent);
		Integer ACTIVITY_MAIN_LAUNCH_MODE = null;
		if (newIntent.hasExtra(CommonConstants.INTENT_EXTRA_ACTIVITY_MAIN_LAUNCH_MODE)) {
			ACTIVITY_MAIN_LAUNCH_MODE = newIntent
					.getIntExtra(
							CommonConstants.INTENT_EXTRA_ACTIVITY_MAIN_LAUNCH_MODE,
							CommonConstants.INTENT_EXTRA_VALUE_ACTIVITY_MAIN_LAUNCH_MODE_CALENDAR_TAB_DAY);
		}
		if (ACTIVITY_MAIN_LAUNCH_MODE != null) {
			switch (ACTIVITY_MAIN_LAUNCH_MODE) {
			case CommonConstants.INTENT_EXTRA_VALUE_ACTIVITY_MAIN_LAUNCH_MODE_CALENDAR_TAB_DAY:
				if (newIntent
						.hasExtra(CommonConstants.INTENT_EXTRA_ACTIVITY_MAIN_CALENDAR_DATE)) {
					mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabDay = newIntent
							.getLongExtra(
									CommonConstants.INTENT_EXTRA_ACTIVITY_MAIN_CALENDAR_DATE,
									0);
				}
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.CALENDAR;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				// selectItem(ActivityState.CALENDAR, 0);
				break;
			case 2:
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.TASKS;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				selectItem(ActivityState.TASKS, 0);
				break;
			case 3:
				// selectItem(ActivityState.LABELS, 0);
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.REMINDERS;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				selectItem(ActivityState.REMINDERS, 0);
				break;
			case 4:
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.CONTACTS;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				selectItem(ActivityState.CONTACTS, 0);
				break;
			case 5:
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.FILES;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				selectItem(ActivityState.FILES, 0);
				break;
			case 6:
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.DIARY;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				selectItem(ActivityState.DIARY, 0);
				break;
			case 7:
				mRetainedFragmentForActivityMain.selectItemIsPending = true;
				mRetainedFragmentForActivityMain.selectItemArg1 = ActivityState.MESSAGES2;
				mRetainedFragmentForActivityMain.selectItemArg2 = 0;
				selectItem(ActivityState.MESSAGES2, 0);
				break;
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onRestoreInstanceState() thread: %s", Thread
							.currentThread().getName()));
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onRestart() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
					.format("ActivityMain onStart() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	/** When using the ActionBarDrawerToggle, you must call it during onPostCreate() and
	 * onConfigurationChanged()... */
	@SuppressWarnings("unused")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (savedInstanceState == null) {
			// perform the below steps same as in the BootCompletedService as the safety
			// measure to ensure that at least the time user launches the application all
			// the approapriate alarms will be scheduled. This is because on some
			// devices ACTION_BOOT_COMPLETED is not guaranteed to be delivered even after
			// user launched the app once
			//
			// store the new difference as current difference for the next time
			Helper.setLastTimeDifference(this,
					System.currentTimeMillis() - SystemClock.elapsedRealtime());
			// setup alarms for remindersmParcelableDataStore
			// we have to setup alarms anew on every reboot
			AlarmService.setupAlarmsForScheduledReminders(this);
			// set alarm to unsilence silenced alarms
			AlarmService.setupAlarmsToUnsilenceSilencedAlarms(this);
			//
			NotificationService.updateNotification(this);
		}
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onPostCreate() thread: %s", Thread.currentThread()
							.getName()));
		}	// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
		if (true) {
			return;
		}
		if (!DataProvider.isSignedIn(null, this)) {
			SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(this,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			switch (syncPolicy) {
			case DO_SYNC:
				Intent intent = new Intent(this, ActivityLogin.class);
				startActivity(intent);
				// finish();
				return;
			default:
				break;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onResume() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String
					.format("ActivityMain onPause() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onSaveInstanceState() thread: %s", Thread
							.currentThread().getName()));
		}
		savedInstanceState.putInt(SavedStates.ACTIVITY_STATE.name(),
				mActivityState.ordinal());
		savedInstanceState.putInt(SavedStates.TAB_POSITION.name(), mSelectedTabIndex);
		savedInstanceState.putInt(SavedStates.TAB_POSITION_CALENDAR.name(),
				mCalendarSelectedTabIndex);
		savedInstanceState.putInt(SavedStates.TAB_POSITION_TASKS.name(),
				mTasksSelectedTabIndex);
		savedInstanceState.putInt(SavedStates.TAB_POSITION_REMINDERS.name(),
				mRemindersSelectedTabIndex);
		savedInstanceState.putInt(SavedStates.TAB_POSITION_FILES.name(),
				mFilesSelectedTabIndex);
		savedInstanceState.putInt(SavedStates.TAB_POSITION_MESSAGES2.name(),
				mMessagesSelectedTabIndex2);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onStop() thread: %s", Thread.currentThread().getName()));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onDestroy() thread: %s", Thread.currentThread()
							.getName()));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
			Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
					"ActivityMain onConfigurationChanged() thread: %s", Thread
							.currentThread().getName()));
		}
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.logout_help_settings, menu);
		MenuItem menuItem;
		switch (mActivityState) {
		case CALENDAR:
			//
			boolean isCalendarsBarShowed = Helper.getBooleanPreferenceValue(this,
					R.string.preference_key_is_calendars_bar_showed, getResources()
							.getBoolean(R.bool.is_calendars_bar_showed));
			//
			if (!isCalendarsBarShowed) {
				menuItem = menu
						.add(Menu.NONE,
								CommonConstants.MENU_ID_SHOW_HIDE_CALENDARS_SELECTOR_BAR,
								Menu.FIRST + 202,
								getResources().getString(
										R.string.action_show_calendars_selector));
				MenuItemCompat.setShowAsAction(menuItem,
						MenuItemCompat.SHOW_AS_ACTION_NEVER);
			} else {
				menuItem = menu
						.add(Menu.NONE,
								CommonConstants.MENU_ID_SHOW_HIDE_CALENDARS_SELECTOR_BAR,
								Menu.FIRST + 202,
								getResources().getString(
										R.string.action_hide_calendars_selector));
				MenuItemCompat.setShowAsAction(menuItem,
						MenuItemCompat.SHOW_AS_ACTION_NEVER);
			}
			//
			//
			// if week tab selected
			if (mCalendarSelectedTabIndex == 1) {
				boolean isDaysCountSelectorBarShowed = Helper.getBooleanPreferenceValue(
						this,
						R.string.preference_key_is_days_count_selector_bar_showed,
						getResources().getBoolean(
								R.bool.is_days_count_selector_bar_showed));
				if (!isDaysCountSelectorBarShowed) {
					menuItem = menu.add(
							Menu.NONE,
							CommonConstants.MENU_ID_SHOW_HIDE_DAYS_COUNT_SELECTOR_BAR,
							Menu.FIRST + 201,
							getResources().getString(
									R.string.action_show_days_count_selector));
					MenuItemCompat.setShowAsAction(menuItem,
							MenuItemCompat.SHOW_AS_ACTION_NEVER);
				} else {
					menuItem = menu.add(
							Menu.NONE,
							CommonConstants.MENU_ID_SHOW_HIDE_DAYS_COUNT_SELECTOR_BAR,
							Menu.FIRST + 201,
							getResources().getString(
									R.string.action_hide_days_count_selector));
					MenuItemCompat.setShowAsAction(menuItem,
							MenuItemCompat.SHOW_AS_ACTION_NEVER);
				}
			}
		case TASKS:
			// add new task
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_ADD_TASK,
					Menu.FIRST + 100, getResources().getString(R.string.action_task_new));
			menuItem.setIcon(R.drawable.ic_add_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			//
			if (true) {
				return true;
			}
			// sync
			@SuppressWarnings("unused")
			SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(this,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			switch (syncPolicy) {
			case DO_NOT_SYNC:
				break;
			case DO_SYNC:
				menuItem = menu.add(
						Menu.NONE,
						CommonConstants.MENU_ID_FORCE_SYNC,
						Menu.FIRST + 300,
						getResources().getString(
								R.string.pref_title_sync_settings_force_sync));
				menuItem.setIcon(R.drawable.ic_autorenew_black_24dp);
				MenuItemCompat.setShowAsAction(menuItem,
						MenuItemCompat.SHOW_AS_ACTION_NEVER);
				break;
			default:
				break;
			}
			break;
		case REMINDERS:
			// add new reminder
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_ADD_QUICK_REMINDER, 0,
					getResources().getString(R.string.action_new_reminder));
			menuItem.setIcon(R.drawable.ic_alarm_add_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			// sync
			// editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_FORCE_SYNC,
			// Menu.FIRST + 200,
			// getResources()
			// .getString(R.string.pref_title_sync_settings_force_sync));
			// editItem.setIcon(R.drawable.ic_autorenew_black_24dp);
			// MenuItemCompat.setShowAsAction(editItem,
			// MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
			//
			break;
		case LABELS:
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_ADD_LABEL, 0,
					getResources().getString(R.string.action_label_new));
			menuItem.setIcon(R.drawable.ic_add_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			break;
		default:
			break;
		}
		return true;
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		// boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		// Intent serviceIntent;
		// long dateTime;
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_ADD_TASK:
			ActivityMain.launchTaskEditor(this, null, TaskEditMode.ADD,
					CommonConstants.INTENT_EXTRA_VALUE_TAB_TASK);
			return true;
		case CommonConstants.MENU_ID_SHOW_HIDE_CALENDARS_SELECTOR_BAR:
			boolean isCalendarsBarShowed = Helper.getBooleanPreferenceValue(this,
					R.string.preference_key_is_calendars_bar_showed, getResources()
							.getBoolean(R.bool.is_calendars_bar_showed));
			//
			PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putBoolean(
							getResources().getString(
									R.string.preference_key_is_calendars_bar_showed),
							!isCalendarsBarShowed).commit();
			//
			if (!isCalendarsBarShowed) {
				// item.setIcon(R.drawable.ic_expand_less_black_24dp);
				item.setTitle(R.string.action_hide_calendars_selector);
				mHorizontalScrollViewCalendarsList.setVisibility(View.VISIBLE);
			} else {
				// item.setIcon(R.drawable.ic_expand_more_black_24dp);
				item.setTitle(R.string.action_show_calendars_selector);
				mHorizontalScrollViewCalendarsList.setVisibility(View.GONE);
			}
			android.support.design.widget.TabLayout.Tab selectedTab = mTabLayout
					.getTabAt(mSelectedTabIndex == -1 ? 0 : mSelectedTabIndex);
			((TabTag) selectedTab.getTag()).UnselectSelectTabOnTabReselected = true;
			selectedTab.select();
			return true;
		case CommonConstants.MENU_ID_SHOW_HIDE_DAYS_COUNT_SELECTOR_BAR:
			boolean isDaysCountSelectorBarShowed = Helper.getBooleanPreferenceValue(this,
					R.string.preference_key_is_days_count_selector_bar_showed,
					getResources().getBoolean(R.bool.is_days_count_selector_bar_showed));
			//
			PreferenceManager
					.getDefaultSharedPreferences(this)
					.edit()
					.putBoolean(
							getResources()
									.getString(
											R.string.preference_key_is_days_count_selector_bar_showed),
							!isDaysCountSelectorBarShowed).commit();
			//
			if (!isDaysCountSelectorBarShowed) {
				item.setTitle(R.string.action_hide_days_count_selector);
				mLinearLayoutDaysCountSelector.setVisibility(View.VISIBLE);
			} else {
				item.setTitle(R.string.action_show_days_count_selector);
				mLinearLayoutDaysCountSelector.setVisibility(View.GONE);
			}
			selectedTab = mTabLayout.getTabAt(mSelectedTabIndex == -1 ? 0
					: mSelectedTabIndex);
			((TabTag) selectedTab.getTag()).UnselectSelectTabOnTabReselected = true;
			selectedTab.select();
			return true;
		case CommonConstants.MENU_ID_FORCE_SYNC:
			// User clicked OK button
			DataProvider.markEntitiesForSynchronization(null, this);
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_LABELS));
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_CONTACTS));
			if (!Helper.isDeviceOnline(this)) {
				Toast.makeText(this, R.string.the_device_is_not_online,
						Toast.LENGTH_SHORT).show();
				return true;
			}
			UserProfile userProfile = DataProvider.getUserProfile(null,
					getApplicationContext());
			if (userProfile == null) {
				Intent intent1 = new Intent(getApplicationContext(), ActivityLogin.class);
				startActivity(intent1);
				return true;
			}
			mGetUserInfoTask = new GetUserInfoTask();
			mGetUserInfoTask.execute(userProfile.getAuthToken());
			return true;
		case CommonConstants.MENU_ID_ADD_QUICK_REMINDER:
			ActivityMain.launchTaskEditor(this, null, TaskEditMode.QUICK,
					CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS);
			return true;
		case R.id.action_logout:
			DataProvider.signOut(null, this);
			finish();
			Intent intent1 = new Intent(this, ActivityLogin.class);
			startActivity(intent1);
			return true;
		case R.id.action_settings:
			Intent intent2 = new Intent(this, ActivitySettings2.class);
			startActivity(intent2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void openFile(String mimeType) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(mimeType);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		// special intent for Samsung file manager
		Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
		// if you want any file type, you can skip next line
		// sIntent.putExtra("CONTENT_TYPE", minmeType);
		sIntent.addCategory(Intent.CATEGORY_DEFAULT);
		Intent chooserIntent;
		if (getPackageManager().resolveActivity(sIntent, 0) != null) {
			// it is device with samsung file manager
			chooserIntent = Intent.createChooser(sIntent, "Open file");
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {intent});
		} else {
			chooserIntent = Intent.createChooser(intent, "Open file");
		}
		try {
			startActivityForResult(chooserIntent, CommonConstants.CHOOSE_FILE_REQUESTCODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(getApplicationContext(),
					R.string.error_the_network_connection_failed, Toast.LENGTH_SHORT)
					.show();
		}
	}

	public static String getPath(Context context, Uri uri) throws URISyntaxException {
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = {"_data"};
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection, null, null,
						null);
				int column_index = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					return cursor.getString(column_index);
				}
			} catch (Exception e) {
				// Eat it
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// switch (requestCode) {
	// case CommonConstants.CHOOSE_FILE_REQUESTCODE:
	// if (resultCode == Activity.RESULT_OK) {
	// Uri uri = data.getData();
	// Log.d(CommonConstants.DEBUG_TAG, "File Uri: " + uri.toString());
	// try {
	// final String filePath = ActivityMain.getPath(ActivityMain.this, uri);
	// new AsyncHttpPostTask().execute(
	// "https://advancedcalendar.biz/uploadfile.aspx", filePath);
	// } catch (URISyntaxException e) {
	// // show error
	// // 1. Instantiate an AlertDialog.Builder with its
	// // constructor
	// AlertDialog.Builder builder = new AlertDialog.Builder(
	// ActivityMain.this);
	// // 2. Chain together various setter methods to set the
	// // dialog
	// // characteristics
	// builder.setMessage(getResources().getString(
	// R.string.an_error_occurred_while_uploading_the_file)
	// + " " + e);
	// // Add the buttons
	// builder.setPositiveButton(R.string.alert_dialog_ok,
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int id) {
	// // User clicked OK button
	// }
	// });
	// // 3. Get the AlertDialog from create()
	// AlertDialog dialog = builder.create();
	// dialog.show();
	// }
	// }
	// break;
	// }
	// }
	public String upload(String url, String filepath) throws Exception {
		String charset = "UTF-8";
		java.io.File file = new java.io.File(filepath);
		String requestURL = url
				+ "?share=1&mode=xml&fileinfo="
				+ URLEncoder
						.encode("{\"FileID\":0,\"FileName\":\""
								+ file.getName()
								+ "\",\"ContentType\":\""
//								+ new MimetypesFileTypeMap().getContentType(file)
								+ "\",\"Size\":"
								+ file.length()
								+ ",\"EnableVersions\":true,\"Description\":\"\",\"Created\":null,\"LastMod\":null,\"Href\":null}",
								"UTF-8");
		try {
			UserProfile userProfile = DataProvider.getUserProfile(null, this);
			String cookie = "advanced_calendar_user=" + userProfile.getAuthToken();
			MultipartUtility multipart = new MultipartUtility(requestURL, charset, cookie);
			multipart.addFilePart("fileUpload", file);
			String response = multipart.finish();
			return response;
		} catch (IOException ex) {
			System.err.println(ex);
		}
		return null;
		// String url = "https://advancedcalendar.biz/uploadfile.aspx";
		// java.io.File file = new java.io.File(filepath);
		//
		// HttpPost httppost = new HttpPost(url);
		//
		// HttpParams params = new BasicHttpParams();
		// params.setParameter("share", 1);
		// params.setParameter("mode", "xml");
		// params.setParameter(
		// "fileinfo",
		// "{\"FileID\":0,\"FileName\"\":\"hangouts_message.ogg\",\"ContentType\":\"application/ogg\",\"Size\":30056,\"EnableVersions\":true,\"Description\":\"\",\"Created\":\"2014-06-16T14:54:20.179712\",\"LastMod\":\"2014-06-16T14:54:20.179724\",\"Href\":null}");
		// httppost.setParams(params);
		// //httppost.addHeader("Host", "advancedcalendar.biz");
		// // httppost.addHeader("Content-Length", "30227");
		// // httppost.addHeader("Content-Disposition",
		// // "form-data; name=\"file\"; filename=\"hangouts_message.ogg\"");
		// // httppost.addHeader("Content-Type", "application/ogg");
		// LocalConfirmedLogin localConfirmedLogin = DataProvider
		// .getSignedInUser(this);
		//
		// httppost.addHeader("Cookie",
		// "advanced_calendar_user=" +
		// "94DB2E26FC3A28881427236AF9EAF96273D1491EB1C26B39F417E830AEBFF7E5F9B1D88E940A0DE285A713A8174547007A1EBB6020B81F9382DF1F2246DAE52F6BF90B36E8758E6EE206D0808133BEE5774C0D34B53684310CD34D9ECF1592E1FFFDA52018BA3BA255DB2BD5211D6CA5A50E26FA4C7AB41D038700D12297C18F049D65DD644C39D2D3D233F2C7C38D8350744961");
		// //localConfirmedLogin.getAuthToken());
		//
		// InputStreamEntity reqEntity = new InputStreamEntity(
		// new FileInputStream(file), -1);
		// reqEntity.setContentType("binary/octet-stream");
		// reqEntity.setChunked(false); // Send in multiple parts if needed
		// httppost.setEntity(reqEntity);
		// HttpClient httpclient = new DefaultHttpClient();
		// HttpResponse response = httpclient.execute(httppost);
		// // Do something with response...
		// return response;
	}

	// private class AsyncHttpPostTask extends AsyncTask<String, Void, Exception> {
	// @Override
	// protected Exception doInBackground(String... params) {
	// // Log.d(TAG, "doInBackground");
	// // Get the path
	// try {
	// final String url = params[0];
	// final String filePath = params[1];
	// String responseStr = upload(url, filePath);
	// // String responseStr =
	// // EntityUtils.toString(response.getEntity());
	// // XStream xstream = new XStream();
	// java.lang.String[] formats = new java.lang.String[] {
	// "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
	// "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd"};
	// XStream xstream = new XStream(new DomDriver()); // does not
	// // require
	// // XPP3
	// // library
	// // XStream xstream = new XStream(new StaxDriver()); // does
	// // not require XPP3 library starting with Java 6
	// xstream.registerConverter(new DateConverter("yyyy-MM-dd'T'HH:mm:ss.SSS",
	// formats));
	// xstream.alias("FileUploadResponse", FileUploadResponse.class);
	// xstream.alias("ServiceOperationResult", ServiceOperationResult.class);
	// xstream.alias("File", biz.advancedcalendar.File.class);
	// FileUploadResponse newJoe = (FileUploadResponse) xstream
	// .fromXML(responseStr);
	// // java.io.File f = new java.io.File(filePath);
	// // long size = f.length();
	// // final long currentTime = Calendar.getInstance().getTimeInMillis();
	// final File newEntity = null;
	// // new File(null, currentTime, currentTime,
	// // CommonConstants.SYNC_STATUS_SYNCHRONIZED, filePath, null, null,
	// // newJoe.File.ServerID, newJoe.File.VersionID == 0 ? null
	// // : newJoe.File.VersionID, newJoe.File.Href,
	// // newJoe.File.UID, newJoe.File.Username, newJoe.File.FileName,
	// // newJoe.File.ContentType, size, newJoe.File.Description,
	// // newJoe.File.ServerID, newJoe.File.Created.getTime(),
	// // newJoe.File.Deleted, newJoe.File.EnableVersions, newJoe.File.Path);
	// FileWithDependents entityWithDependents = new FileWithDependents();
	// entityWithDependents.file = newEntity;
	// entityWithDependents.taskIdList = null;
	// entityWithDependents.isServerTaskIdList = false;
	// entityWithDependents.labelIdList = null;
	// entityWithDependents.isServerLabelIdList = false;
	// entityWithDependents.contactIdList = null;
	// entityWithDependents.isServerContactIdList = false;
	// // DataProvider.insertOrReplaceFileWithDependents(getApplicationContext(),
	// // entityWithDependents);
	// // copy file into app private directory
	// DataProvider.copyToExternalStoragePrivateFile(ActivityMain.this,
	// filePath, newJoe.File.UID, newEntity.getId());
	// LocalBroadcastManager.getInstance(ActivityMain.this).sendBroadcast(
	// new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_FILES));
	// } catch (Exception e) {
	// // show error
	// // 1. Instantiate an AlertDialog.Builder with its
	// // constructor
	// AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
	// // 2. Chain together various setter methods to set the
	// // dialog
	// // characteristics
	// builder.setMessage(getResources().getString(
	// R.string.an_error_occurred_while_uploading_the_file)
	// + " " + e);
	// // Add the buttons
	// builder.setPositiveButton(R.string.alert_dialog_ok,
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int id) {
	// // User clicked OK button
	// }
	// });
	// // 3. Get the AlertDialog from create()
	// AlertDialog dialog = builder.create();
	// dialog.show();
	// }
	// return null;
	// }
	//
	// @Override
	// protected void onPostExecute(final Exception userLoginResult) {
	// String message;
	// if (userLoginResult == null) {
	// message = getResources().getString(
	// R.string.the_file_was_successfully_uploaded);
	// } else {
	// message = getResources().getString(
	// R.string.an_error_occurred_while_uploading_the_file)
	// + " " + userLoginResult;
	// }
	// // 1. Instantiate an AlertDialog.Builder with its constructor
	// AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
	// // 2. Chain together various setter methods to set the dialog
	// // characteristics
	// builder.setMessage(message);
	// // Add the buttons
	// builder.setPositiveButton(R.string.alert_dialog_ok,
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int id) {
	// // User clicked OK button
	// }
	// });
	// // 3. Get the AlertDialog from create()
	// AlertDialog dialog = builder.create();
	// dialog.show();
	// }
	// }
	/* The click listener for ListView in the navigation drawer */
	public void selectDrawerItem(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
		case R.id.drawer_item_calendar:
			// long weekStart;
			if (mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabWeek == null) {
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH);
				int day = calendar.get(Calendar.DAY_OF_MONTH);
				calendar.clear();
				calendar.set(year, month, day);
				// move calendar to the beginning of the week
				int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
				int firstDayOfWeek = Helper.getFirstDayOfWeek(ActivityMain.this);
				calendar.add(Calendar.DAY_OF_YEAR, firstDayOfWeek - dayOfWeek);
				if (firstDayOfWeek > dayOfWeek) {
					calendar.add(Calendar.DAY_OF_YEAR, -7);
				}
				// now calendar has been moved to the beginning of the week
				// weekStart = calendar.getTimeInMillis();
			} else {
				// weekStart = dataFragment.lastUsedFirstDayOfWeekOnWeekViewOnTabWeek;
			}
			mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabWeek = Calendar
					.getInstance().getTimeInMillis();
			// dataFragment.fragmentViewWeekData = fragmentViewWeekData;
			selectItem(ActivityState.CALENDAR, mCalendarSelectedTabIndex);
			mDrawerTitle = menuItem.getTitle();
			break;
		case R.id.drawer_item_taskstree:
			selectItem(ActivityState.TASKS, mTasksSelectedTabIndex);
			mDrawerTitle = menuItem.getTitle();
			break;
		case R.id.drawer_item_reminders:
			selectItem(ActivityState.REMINDERS, mRemindersSelectedTabIndex);
			mDrawerTitle = menuItem.getTitle();
			break;
		case R.id.drawer_item_settings:
			Intent intent2 = new Intent(ActivityMain.this, ActivitySettings2.class);
			startActivity(intent2);
			break;
		case 4:
			selectItem(ActivityState.FILES, mFilesSelectedTabIndex);
			break;
		case 5:
			selectItem(ActivityState.DIARY, 0);
			break;
		case 6:
			selectItem(ActivityState.MESSAGES2, mMessagesSelectedTabIndex2);
			break;
		}
		// Highlight the selected item, update the title, and close the drawer
		menuItem.setChecked(true);
		// setTitle(mTitle);
		mDrawerLayout.closeDrawers();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void selectItem(ActivityState newState, int tabIndex) {
		// update the main content by replacing fragments
		// FragmentTransaction ft;
		// Fragment currentFragment;
		Bundle bundle;
		// Context context = this;
		mActivityState = newState;
		android.support.design.widget.TabLayout.Tab tab;// , tab2;
		switch (newState) {
		case CALENDAR:// Calendar
			// setup tabs
			mTabLayout.removeAllTabs();
			//
			Bundle b = new Bundle();
			// day tab
			long firstDay = mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabDay == null ? Calendar
					.getInstance().getTimeInMillis()
					: mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabDay;
			b.putLong(FragmentViewWeek2.InitArgumentsKeyFirstDay, firstDay);
			b.putInt(FragmentViewWeek2.InitArgumentsKeyDaysCount, 1);
			mTabLayout.addTab(
					mTabLayout
							.newTab()
							.setText(R.string.tab_calendar_day)
							.setTag(new TabTag(FragmentTags.DAY.name(),
									FragmentViewWeek2.class,
									R.id.activity_main_fragment_frame, this, b, false)),
					false);
			// week tab
			b = new Bundle();
			firstDay = mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabWeek == null ? Calendar
					.getInstance().getTimeInMillis()
					: mRetainedFragmentForActivityMain.lastUsedFirstDayOfWeekOnWeekViewOnTabWeek;
			b.putLong(FragmentViewWeek2.InitArgumentsKeyFirstDay, firstDay);
			int daysCount = Helper.getIntegerPreferenceValue(this, getResources()
					.getString(R.string.preference_key_days_count_for_weekview),
					getResources()
							.getInteger(R.integer.weekview_days_count_default_value),
					getResources().getInteger(R.integer.weekview_days_count_min_value),
					getResources().getInteger(R.integer.weekview_days_count_max_value));
			b.putInt(FragmentViewWeek2.InitArgumentsKeyDaysCount, daysCount);
			mTabLayout.addTab(
					mTabLayout
							.newTab()
							.setText(R.string.tab_calendar_week)
							.setTag(new TabTag(FragmentTags.WEEK.name(),
									FragmentViewWeek2.class,
									R.id.activity_main_fragment_frame, this, b, false)),
					false);
			// month tab
			b = new Bundle();
			Calendar calendar = Calendar.getInstance();
			int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
			CalendarHelper.toBeginningOfMonth(calendar);
			b.putLong(FragmentViewMonth2.InitArgumentsKeyFirstDay,
					calendar.getTimeInMillis());
			mTabLayout.addTab(
					mTabLayout
							.newTab()
							.setText(R.string.tab_calendar_month)
							.setTag(new TabTag(FragmentTags.MONTH.name(),
									FragmentViewMonth2.class,
									R.id.activity_main_fragment_frame, this, b, false)),
					false);
			// agenda tab
			b = new Bundle();
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			b.putLong(FragmentViewAgenda.StateParametersKey, calendar.getTimeInMillis());
			mTabLayout.addTab(
					mTabLayout
							.newTab()
							.setText(R.string.tab_calendar_agenda)
							.setTag(new TabTag(FragmentTags.AGENDA.name(),
									FragmentViewAgenda.class,
									R.id.activity_main_fragment_frame, this, b, false)),
					false);
			// mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);
			tab = mTabLayout.getTabAt(tabIndex == -1 ? 0 : tabIndex);
			tab.select();
			break;
		case TASKS:// Tasks
			// setup tabs
			mTabLayout.removeAllTabs();
			setTitle(R.string.activity_view_tasks_title);
			bundle = new Bundle();
			bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
					AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK,
					true);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK, false);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK, false);
			bundle.putInt(CommonConstants.MENU_ID,
					R.menu.fragment_view_task_tree_active_cab_menu);
			mTabLayout.addTab(
					mTabLayout
							.newTab()
							.setText(R.string.activity_view_task_tree_tab_active)
							.setTag(new TabTag(FragmentTags.ACTIVE_TASKS.name(),
									FragmentViewTaskTree.class,
									R.id.activity_main_fragment_frame, this, bundle,
									false)), false);
			bundle = new Bundle();
			bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
					AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK,
					true);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_TASK, false);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, false);
			bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK, true);
			bundle.putInt(CommonConstants.MENU_ID,
					R.menu.fragment_view_task_tree_completed_cab_menu);
			mTabLayout.addTab(
					mTabLayout
							.newTab()
							.setText(R.string.activity_view_task_tree_tab_closed)
							.setTag(new TabTag(FragmentTags.COMPLETED_TASKS.name(),
									FragmentViewTaskTree.class,
									R.id.activity_main_fragment_frame, this, bundle,
									false)), false);
			// mTabLayout.setOnTabSelectedListener(mOnTabSelectedListener);
			tab = mTabLayout.getTabAt(tabIndex == -1 ? 0 : tabIndex);
			tab.select();
			break;
		case REMINDERS:// Messages
			// setup tabs
			mTabLayout.removeAllTabs();
			setTitle(R.string.actionbar_reminders);
			mTabLayout
					.addTab(mTabLayout
							.newTab()
							.setText(R.string.activity_view_task_tab_reminders_elapsed)
							.setTag(new TabTag(FragmentTags.REMINDERS_ELAPSED.name(),
									FragmentViewElapsedReminders.class,
									R.id.activity_main_fragment_frame, this, null, false)),
							false);
			// actionBar.addTab(tab, false);
			mTabLayout
					.addTab(mTabLayout
							.newTab()
							.setText(R.string.activity_view_task_tab_reminders_scheduled)
							.setTag(new TabTag(FragmentTags.REMINDERS_SCHEDULED.name(),
									FragmentViewScheduledReminders.class,
									R.id.activity_main_fragment_frame, this, null, false)),
							false);
			tab = mTabLayout.getTabAt(tabIndex == -1 ? 0 : tabIndex);
			tab.select();
			break;
		default:
			break;
		}
		//
		switch (newState) {
		case CALENDAR:
			boolean isCalendarsBarShowed = Helper.getBooleanPreferenceValue(this,
					R.string.preference_key_is_calendars_bar_showed, getResources()
							.getBoolean(R.bool.is_calendars_bar_showed));
			if (!isCalendarsBarShowed) {
				mHorizontalScrollViewCalendarsList.setVisibility(View.GONE);
			} else {
				mHorizontalScrollViewCalendarsList.setVisibility(View.VISIBLE);
			}
			if (mCalendarSelectedTabIndex == 1) {
				boolean isDaysCountSelectorBarShowed = Helper.getBooleanPreferenceValue(
						this,
						R.string.preference_key_is_days_count_selector_bar_showed,
						getResources().getBoolean(
								R.bool.is_days_count_selector_bar_showed));
				if (!isDaysCountSelectorBarShowed) {
					mLinearLayoutDaysCountSelector.setVisibility(View.GONE);
				} else {
					mLinearLayoutDaysCountSelector.setVisibility(View.VISIBLE);
				}
			} else {
				mLinearLayoutDaysCountSelector.setVisibility(View.GONE);
			}
			break;
		case TASKS:
		case REMINDERS:
		default:
			mHorizontalScrollViewCalendarsList.setVisibility(View.GONE);
			mLinearLayoutDaysCountSelector.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	public void onSelectedIndexChanged(int index) {
		mSelectedTabIndex = index;
		switch (mActivityState) {
		case CALENDAR:
			mCalendarSelectedTabIndex = index;
			if (mCalendarSelectedTabIndex == 1) {
				boolean isDaysCountSelectorBarShowed = Helper.getBooleanPreferenceValue(
						this,
						R.string.preference_key_is_days_count_selector_bar_showed,
						getResources().getBoolean(
								R.bool.is_days_count_selector_bar_showed));
				if (!isDaysCountSelectorBarShowed) {
					mLinearLayoutDaysCountSelector.setVisibility(View.GONE);
				} else {
					mLinearLayoutDaysCountSelector.setVisibility(View.VISIBLE);
				}
			} else {
				mLinearLayoutDaysCountSelector.setVisibility(View.GONE);
			}
			break;
		case TASKS:
			mTasksSelectedTabIndex = index;
			break;
		case REMINDERS:
			mRemindersSelectedTabIndex = index;
			break;
		default:
			break;
		}
	}

	// @Override
	// public Bundle getFragmentViewWeekData() {
	// return dataFragment.fragmentViewWeekData;
	// }
	/** Represents an asynchronous authentication task used to authenticate the user. */
	public class GetUserInfoTask extends AsyncTask<String, Void, GetUserInfoResult> {
		@Override
		protected GetUserInfoResult doInBackground(String... params) {
			return ServerProvider.GetUserInfo(getApplicationContext(), params[0]);
		}

		@Override
		protected void onPostExecute(final GetUserInfoResult userInfoViewModel) {
			mGetUserInfoTask = null;
			if (userInfoViewModel.getUserInfoViewModel() != null) {
				Intent serviceIntent = new Intent(getApplicationContext(),
						SyncService.class);
				serviceIntent
						.putExtra(
								CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
								CommonConstants.INTENT_EXTRA_VALUE_SYNC_SERVICE_REQUEST_FORCE_SYNC);
				// Start the service
				startService(serviceIntent);
			} else {
				Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
				intent.putExtra(
						CommonConstants.INTENT_EXTRA_SYNCHRONIZATION_AFTER_SUCCESSFUL_SIGNIN_REQUIRED,
						true);
				startActivity(intent);
			}
		}

		@Override
		protected void onCancelled() {
			mGetUserInfoTask = null;
		}
	}

	@Override
	public void onDaysSelected(long firstDay, long lastDay) {
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(firstDay);
		calendar1.set(Calendar.HOUR_OF_DAY, 0);
		calendar1.set(Calendar.MINUTE, 0);
		calendar1.set(Calendar.SECOND, 0);
		calendar1.set(Calendar.MILLISECOND, 0);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTimeInMillis(lastDay);
		calendar2.set(Calendar.HOUR_OF_DAY, 0);
		calendar2.set(Calendar.MINUTE, 0);
		calendar2.set(Calendar.SECOND, 0);
		calendar2.set(Calendar.MILLISECOND, 0);
		int daysCount = CalendarHelper.daysBetween(calendar1, calendar2) + 1;
		if (daysCount > 1) {
			mSeekBarDaysCountSelector.setProgress(daysCount - 2);
		}
		android.support.design.widget.TabLayout.Tab selectedTab = mTabLayout
				.getTabAt(daysCount == 1 ? 0 : 1);
		selectedTab.select();
		getSupportFragmentManager().executePendingTransactions();
		Intent intent = new Intent(
				CommonConstants.ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED);
		intent.putExtra(CommonConstants.INTENT_EXTRA_TAG,
				daysCount == 1 ? FragmentTags.DAY.name() : FragmentTags.WEEK.name());
		intent.putExtra(CommonConstants.INTENT_EXTRA_DATETIME, firstDay);
		intent.putExtra(CommonConstants.INTENT_EXTRA_DAYS_COUNT, daysCount);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	@Override
	public void onTaskClicked(long id) {
		ActivityMain.launchTaskViewerOrEditor(this, id, 0);
	}

	@Override
	public RetainedFragmentForFragmentViewAgenda getRetainedFragmentForFragmentViewAgenda() {
		return mRetainedFragmentForFragmentViewAgenda;
	}

	@Override
	public RetainedFragmentForFragmentViewWeek2 getRetainedFragmentForFragmentViewWeek2(
			String tag) {
		if (tag.equals(FragmentTags.DAY.name())) {
			return mRetainedFragmentForFragmentViewWeek2ForDay;
		} else if (tag.equals(FragmentTags.WEEK.name())) {
			return mRetainedFragmentForFragmentViewWeek2ForWeek;
		} else {
			return null;
		}
	}

	@Override
	public RetainedFragmentForFragmentViewMonth2 getRetainedFragmentForFragmentViewMonth2(
			String tag) {
		if (tag.equals(FragmentTags.MONTH.name())) {
			return mRetainedFragmentForFragmentViewMonth2;
		} else {
			return null;
		}
	}

	@Override
	public void onDayHeaderClicked(long dateTime) {
		android.support.design.widget.TabLayout.Tab selectedTab = mTabLayout.getTabAt(0);
		selectedTab.select();
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(
				FragmentTags.DAY.name());
		getSupportFragmentManager().executePendingTransactions();
		fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.DAY.name());
		if (fragment == null) {
		}
		Intent intent = new Intent(
				CommonConstants.ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED);
		intent.putExtra(CommonConstants.INTENT_EXTRA_TAG, FragmentTags.DAY.name());
		intent.putExtra(CommonConstants.INTENT_EXTRA_DATETIME, dateTime);
		intent.putExtra(CommonConstants.INTENT_EXTRA_DAYS_COUNT, 1);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
