package biz.advancedcalendar.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.db.TaskUiData;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartContacts;
import biz.advancedcalendar.fragments.FragmentEditTaskPartFiles;
import biz.advancedcalendar.fragments.FragmentEditTaskPartLabels;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMessages;
import biz.advancedcalendar.fragments.FragmentEditTaskPartNotes;
import biz.advancedcalendar.fragments.FragmentEditTaskPartReminders;
import biz.advancedcalendar.fragments.TaskWithDependentsUiDataHolder;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.TaskEditMode;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.server.GetUserInfoResult;
import biz.advancedcalendar.server.ServerProvider;
import biz.advancedcalendar.services.NotificationService;
import biz.advancedcalendar.sync.SyncService;
import biz.advancedcalendar.utils.Helper;

public class ActivityEditTask extends AppCompatActivity implements
		android.support.design.widget.TabLayout.OnTabSelectedListener,
		TaskWithDependentsUiDataHolder {
	private ActivityEditTaskPagerAdapter mActivityPagerAdapter;
	private ViewPager mViewPager;
	// Menu identifiers
	static final int EDIT_REMINDER_REQUEST = 1; // The request code
	private static final boolean Debug = true;
	private static final String DebugTag = "CreateReminderDebugTag";
	private LocalBroadcastManager mBroadcaster;
	private ActivityEditTaskParcelableDataStore mParcelableDataStore;
	private GetUserInfoTask mGetUserInfoTask = null;
	private TaskWithDependentsUiData taskWithDependentsUiData;
	private TaskUiData taskUiData;
	private UserInterfaceData userInterfaceData;
	private TaskEditMode taskEditMode;
	private Toolbar mToolbar;
	private TabLayout mTabLayout;

	public ActivityEditTask() {
		if (ActivityEditTask.Debug) {
			Log.d(ActivityEditTask.DebugTag,
					"ActivityEditTask() " + System.identityHashCode(this));
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (ActivityEditTask.Debug) {
			Log.d(ActivityEditTask.DebugTag,
					"ActivityEditTask " + "onCreate(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pager);
		// Set a Toolbar to replace the ActionBar.
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mTabLayout = (TabLayout) findViewById(R.id.tablayout);
		mTabLayout.setOnTabSelectedListener(this);
		mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		FragmentManager fm = getSupportFragmentManager();
//		dataFragment = (ActivityEditTaskDataFragment) fm
//				.findFragmentByTag(CommonConstants.DATA_FRAGMENT);
		if (savedInstanceState != null) {
			mParcelableDataStore = savedInstanceState.getParcelable(CommonConstants.INIT_ARGUMENTS);
		} else {
			if (mParcelableDataStore == null) {
				mParcelableDataStore = getIntent().getParcelableExtra(CommonConstants.INIT_ARGUMENTS);
			}
		}

		taskWithDependentsUiData = mParcelableDataStore.mTaskWithDependentsUiData;
		taskUiData = taskWithDependentsUiData.TaskUiData;
		userInterfaceData = mParcelableDataStore.userInterfaceData;
		taskEditMode = mParcelableDataStore.taskEditMode;
		mBroadcaster = LocalBroadcastManager.getInstance(this);
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mActivityPagerAdapter = new ActivityEditTaskPagerAdapter(this,
				getSupportFragmentManager());
		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(taskUiData.getId() == null ? getResources().getString(
				R.string.action_edit_task_new_task_name) : taskUiData.getName());
		// Specify that the Home/Up button should not be enabled, since there is
		// no hierarchical parent.
		actionBar.setHomeButtonEnabled(true);
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.activity_pager);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setAdapter(mActivityPagerAdapter);
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				android.support.design.widget.TabLayout.Tab selectedTab = mTabLayout
						.getTabAt(position);
				selectedTab.select();
			}
		});
		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mActivityPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the listener for when this tab is
			// selected.
			//
			mTabLayout.addTab(
					mTabLayout.newTab().setText(mActivityPagerAdapter.getPageTitle(i)),
					false);
		}
		int tabIndex = 0;
		int tabCode = mParcelableDataStore.tab;
		if (tabCode == CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS) {
			tabIndex = 1;
		}
		android.support.design.widget.TabLayout.Tab selectedTab = mTabLayout
				.getTabAt(tabIndex);
		selectedTab.select();
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		if (ActivityEditTask.Debug) {
			Log.d(ActivityEditTask.DebugTag,
					"ActivityEditTask "
							+ "onSaveInstanceState(Bundle savedInstanceState) "
							+ System.identityHashCode(this));
		}
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable(CommonConstants.INIT_ARGUMENTS, mParcelableDataStore);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_CANCEL, 0,
				getResources().getString(R.string.action_cancel));
		menuItem.setIcon(R.drawable.ic_cancel_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SAVE, 0, getResources()
				.getString(R.string.action_done));
		menuItem.setIcon(R.drawable.ic_save_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		if (taskUiData.getId() != null) {
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_COPY, 0,
					getResources().getString(R.string.action_copy));
			menuItem.setIcon(R.drawable.ic_content_copy_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_DELETE, 1,
					getResources().getString(R.string.action_delete));
			menuItem.setIcon(R.drawable.ic_delete_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_COPY:
			ActivityMain.launchTaskEditor2(this, new TaskWithDependentsUiData(
					taskWithDependentsUiData), TaskEditMode.ADD,
					CommonConstants.INTENT_EXTRA_VALUE_TAB_TASK
			//
					);
			return true;
		case CommonConstants.MENU_ID_SET_COMPLETED:
			DataProvider
					.markTasksAsCompleted(null, this, new long[] {taskUiData.getId()}, true);
			// notify about changes
			LocalBroadcastManager.getInstance(this).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			if (true) {
				return true;
			}
			// TODO sync deleted only
			// sync with server
			@SuppressWarnings("unused")
			SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(ActivityEditTask.this,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			switch (syncPolicy) {
			case DO_NOT_SYNC:
				return true;
			default:
				break;
			}
			if (!Helper.isDeviceOnline(this)) {
				Toast.makeText(this, R.string.the_device_is_not_online,
						Toast.LENGTH_SHORT).show();
				return true;
			}
			UserProfile userProfile = DataProvider
					.getUserProfile(null, getApplicationContext());
			if (userProfile == null) {
				Intent intent1 = new Intent(getApplicationContext(), ActivityLogin.class);
				startActivity(intent1);
				return true;
			}
			mGetUserInfoTask = new GetUserInfoTask();
			mGetUserInfoTask.execute(userProfile.getAuthToken());
			return true;
		case CommonConstants.MENU_ID_DELETE:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			// final DBHelper dbHelper = new DBHelper(this);
			// final List<MeasurementDevice> totalDeviceList = dbHelper.getAllDevices();
			String[] items = new String[] {getResources().getString(
					R.string.action_delete_task_subtree_confirmation)};
			final boolean[] selectedItems = new boolean[] {false};
			alertDialogBuilder
					.setTitle(
							getResources().getString(
									R.string.action_delete_task_confirmation))
					.setMultiChoiceItems(items, selectedItems,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int indexSelected, boolean isChecked) {
									selectedItems[0] = isChecked;
								}
							})
					// .setMessage(
					// getResources().getString(
					// R.string.action_delete_task_confirmation))
					.setCancelable(true)
					// Set the action buttons
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									// User clicked OK button
									DataProvider.markTasksAsDeleted(
											null,
											getApplicationContext(),
											new long[] {taskUiData.getId()}, selectedItems[0]);
									// notify about changes
									LocalBroadcastManager
											.getInstance(getApplicationContext())
											.sendBroadcast(
													new Intent(
															CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
									LocalBroadcastManager
											.getInstance(getApplicationContext())
											.sendBroadcast(
													new Intent(
															CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
									// sync with server
									SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
											.getIntegerPreferenceValueFromStringArray(
													ActivityEditTask.this,
													R.string.preference_key_sync_policy,
													R.array.sync_policy_values_array,
													R.integer.sync_policy_default_value));
									syncPolicy = SyncPolicy.DO_NOT_SYNC;
									switch (syncPolicy) {
									case DO_NOT_SYNC:
										finish();
										return;
									default:
										break;
									}
									if (!Helper.isDeviceOnline(ActivityEditTask.this)) {
										Toast.makeText(ActivityEditTask.this,
												R.string.the_device_is_not_online,
												Toast.LENGTH_SHORT).show();
										finish();
										return;
									}
									UserProfile userProfile = DataProvider
											.getUserProfile(null, getApplicationContext());
									if (userProfile == null) {
										Intent intent1 = new Intent(
												getApplicationContext(),
												ActivityLogin.class);
										startActivity(intent1);
										finish();
										return;
									}
									mGetUserInfoTask = new GetUserInfoTask();
									mGetUserInfoTask.execute(userProfile.getAuthToken());
									finish();
									return;
								}
							}).setNegativeButton(R.string.alert_dialog_cancel, null);
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return true;
		case CommonConstants.MENU_ID_SAVE:
			return trySave();
		case CommonConstants.MENU_ID_CANCEL:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean trySave() {
		UserProfile userProfile;
		if (isEntitySaved()) {
			NavUtils.navigateUpFromSameTask(this);
			// notify about changes
			mBroadcaster.sendBroadcast(new Intent(
					CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			mBroadcaster.sendBroadcast(new Intent(
					CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			if (true) {
				return true;
			}
			// sync with server
			@SuppressWarnings("unused")
			SyncPolicy syncPolicy;
			syncPolicy = SyncPolicy.fromInt((byte) Helper
					.getIntegerPreferenceValueFromStringArray(ActivityEditTask.this,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			switch (syncPolicy) {
			case DO_NOT_SYNC:
				return true;
			default:
				break;
			}
			if (!Helper.isDeviceOnline(this)) {
				Toast.makeText(this, R.string.the_device_is_not_online,
						Toast.LENGTH_SHORT).show();
				return true;
			}
			userProfile = DataProvider.getUserProfile(null, getApplicationContext());
			if (userProfile == null) {
				Intent intent1 = new Intent(getApplicationContext(), ActivityLogin.class);
				startActivity(intent1);
				return true;
			}
			mGetUserInfoTask = new GetUserInfoTask();
			mGetUserInfoTask.execute(userProfile.getAuthToken());
			return false;
		} else {
			taskEditMode = TaskEditMode.EDIT;
			return true;
		}
	}

	@Override
	public void onTabUnselected(Tab tab) {
	}

	@Override
	public void onTabSelected(Tab tab) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		// adjust action bar
		supportInvalidateOptionsMenu();
	}

	@Override
	public void onTabReselected(Tab tab) {
	}

	private static class ActivityEditTaskPagerAdapter extends FragmentPagerAdapter {
		SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
		private Context mContext;
		private boolean setupUiPending;
		private TaskUiData setupUiTaskUiData2;

		public ActivityEditTaskPagerAdapter(Context context, FragmentManager fm) {
			super(fm);
			mContext = context;
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0:
				return new FragmentEditTaskPartMain();
			case 1:
				Bundle b = new Bundle();
				b.putBoolean(CommonConstants.INTENT_EXTRA_SHOW_ADD_MENU, true);
				Fragment f = new FragmentEditTaskPartReminders();
				f.setArguments(b);
				return f;
			case 2:
				return new FragmentEditTaskPartNotes();
			case 3:
				return new FragmentEditTaskPartLabels();
			case 4:
				return new FragmentEditTaskPartContacts();
			case 5:
				return new FragmentEditTaskPartMessages();
			case 6:
				return new FragmentEditTaskPartFiles();
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragments.put(position, fragment);
			if (position == 0 && setupUiPending) {
				if (fragment.isResumed()) {
					((FragmentEditTaskPartMain) fragment).setupUi(setupUiTaskUiData2);
				} else {
					((FragmentEditTaskPartMain) fragment)
							.setupUiPending(setupUiTaskUiData2);
				}
				setupUiPending = false;
				setupUiTaskUiData2 = null;
			}
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		public Fragment getRegisteredFragment(int position) {
			return registeredFragments.get(position);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_main);
			case 1:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_reminders);
			case 2:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_notes);
			case 3:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_labels);
			case 4:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_contacts);
			case 5:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_messages);
			case 6:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_files);
			default:
				return "";
			}
		}

		public void setSetupUiPending(TaskUiData taskUiData2) {
			setupUiPending = true;
			setupUiTaskUiData2 = taskUiData2;
		}
	}

	private boolean isEntitySaved() {
		boolean isCollected = true;
		for (int j = 0; j < mActivityPagerAdapter.getCount(); j++) {
			// mViewPager.setCurrentItem(j);
			Fragment f = mActivityPagerAdapter.getRegisteredFragment(j);
			if (f != null && !f.isDetached()) {
				if (!((DataSaver) f).isDataCollected()) {
					isCollected = false;
					break;
				}
			}
		}
		if (isCollected) {
			// mViewPager.setCurrentItem(currentItem);
			long timeSkew = Helper.getLongPreferenceValue(this,
					CommonConstants.TIME_SKEW, 0, null, null);
			long nowTime = System.currentTimeMillis();
			if (taskEditMode == TaskEditMode.ADD) {
				taskUiData.setId(null);
				taskUiData.setServerId(null);
				taskUiData.setLocalCreateDateTime(nowTime);
				for (ReminderUiData reminderUiData : taskWithDependentsUiData.RemindersUiData) {
					reminderUiData.setId(null);
					reminderUiData.setServerId(null);
				}
			}
			Helper.setLocalChangeDateTimeToCurrentTime(taskUiData, nowTime, timeSkew);
			try {
				DataProvider.insertOrReplaceTaskWithDependents(null,
						this, new TaskWithDependents(this, taskWithDependentsUiData), true);
			} catch (Exception e) {
				isCollected = false;
				// boolean scheduleRemindersOfCompletedTasks =
				// CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
				// .getIntegerPreferenceValueFromStringArray(
				// this,
				// R.string.preference_key_reminder_behavior_for_completed_task,
				// R.array.reminder_behavior_for_completed_task_values_array,
				// R.integer.reminder_default_behavior_for_completed_task);
				// if (mEntityWithDependents.TaskUiData.getId() != null) {
				// Alarm.resetupRemindersOfTask(this, mEntityWithDependents.TaskUiData
				// .getId(), scheduleRemindersOfCompletedTasks, Calendar
				// .getInstance().getTimeInMillis());
				// // Alarm.resetupAlarmsOfTask(this,
				// // mEntityWithDependents.TaskUiData.getId());
				// }
				// 1. Instantiate an AlertDialog.Builder with its constructor
				Helper.showAlertDialog(this, e);
				throw new IllegalArgumentException(e);
			} finally {
				NotificationService.updateNotification(this);
			}
		}
		return isCollected;
	}

	public ActivityEditTaskParcelableDataStore getmParcelableDataStore() {
		return mParcelableDataStore;
	}

	@Override
	public TaskWithDependentsUiData getTaskWithDependentsUiData() {
		if (taskWithDependentsUiData == null) {
			mParcelableDataStore = getmParcelableDataStore();
			taskWithDependentsUiData = mParcelableDataStore.mTaskWithDependentsUiData;
		}
		return taskWithDependentsUiData;
	}

	@Override
	public UserInterfaceData getUserInterfaceData() {
		if (userInterfaceData == null) {
			mParcelableDataStore = getmParcelableDataStore();
			userInterfaceData = mParcelableDataStore.userInterfaceData;
		}
		return userInterfaceData;
	}

	// @Override
	// public void setTaskValuesFromUiElements(String exceptFragment) {
	// for (int j = 0; j < mActivityPagerAdapter.getCount(); j++) {
	// Fragment f = mActivityPagerAdapter.getRegisteredFragment(j);
	// if (f != null && !f.isDetached() && !f.getTag().equals(exceptFragment)) {
	// // ((DataSaver) f).saveData();
	// }
	// }
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
				serviceIntent.putExtra(CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
						CommonConstants.SYNC_SERVICE_REQUEST_SYNC_UP_TASKS);
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

	public static class ActivityEditTaskParcelableDataStore implements
			Parcelable {
		private TaskWithDependentsUiData mTaskWithDependentsUiData;
		private UserInterfaceData userInterfaceData;
		private TaskEditMode taskEditMode;
		private int tab;

		public ActivityEditTaskParcelableDataStore() {
			if (ActivityEditTask.Debug) {
				Log.d(ActivityEditTask.DebugTag, "ActivityEditTaskParcelableDataStore()"
						+ System.identityHashCode(this));
			}
		}

		public ActivityEditTaskParcelableDataStore(
				TaskWithDependentsUiData mTaskWithDependentsUiData,
				UserInterfaceData userInterfaceData, TaskEditMode taskEditMode, int tab) {
			if (ActivityEditTask.Debug) {
				Log.d(ActivityEditTask.DebugTag,
						"ActivityEditTaskParcelableDataStore(TaskWithDependentsUiData mTaskWithDependentsUiData, UserInterfaceData userInterfaceData, TaskEditMode taskEditMode, int tab) "
								+ System.identityHashCode(this));
			}
			this.mTaskWithDependentsUiData = mTaskWithDependentsUiData;
			this.userInterfaceData = userInterfaceData;
			this.taskEditMode = taskEditMode;
			this.tab = tab;
		}

		//@Override
		public void onCreate(Bundle savedInstanceState) {
			if (ActivityEditTask.Debug) {
				Log.d(ActivityEditTask.DebugTag,
						"ActivityEditTaskParcelableDataStore "
								+ "onCreate(Bundle savedInstanceState) "
								+ System.identityHashCode(this));
			}
			//super.onCreate(savedInstanceState);
			//setRetainInstance(true);
			//
			if (savedInstanceState != null) {
				mTaskWithDependentsUiData = savedInstanceState
						.getParcelable("mTaskWithDependentsUiData");
				userInterfaceData = savedInstanceState.getParcelable("userInterfaceData");
				taskEditMode = TaskEditMode.fromInt(savedInstanceState
						.getByte("taskEditMode"));
				tab = savedInstanceState.getInt("tab");
			}
			//
		}

		//@Override
		public void onSaveInstanceState(Bundle savedInstanceState) {
			if (ActivityEditTask.Debug) {
				Log.d(ActivityEditTask.DebugTag,
						"ActivityEditTaskParcelableDataStore "
								+ "onSaveInstanceState(Bundle savedInstanceState) "
								+ System.identityHashCode(this));
			}
			//super.onSaveInstanceState(savedInstanceState);
			savedInstanceState.putParcelable("mTaskWithDependentsUiData",
					mTaskWithDependentsUiData);
			savedInstanceState.putParcelable("userInterfaceData", userInterfaceData);
			savedInstanceState.putByte("taskEditMode", taskEditMode.getValue());
			savedInstanceState.putInt("tab", tab);
		}

		public void setTaskWithDependentsUiData(TaskWithDependentsUiData data) {
			mTaskWithDependentsUiData = data;
		}

		public TaskWithDependentsUiData getTaskWithDependentsUiData() {
			return mTaskWithDependentsUiData;
		}

		public UserInterfaceData getUserInterfaceData() {
			return userInterfaceData;
		}

		public void setUserInterfaceData(UserInterfaceData userInterfaceData) {
			this.userInterfaceData = userInterfaceData;
		}

		public TaskEditMode getTaskEditMode() {
			return taskEditMode;
		}

		public void setTaskEditMode(TaskEditMode taskEditMode) {
			this.taskEditMode = taskEditMode;
		}

		public int getTab() {
			return tab;
		}

		public void setTab(int tab) {
			this.tab = tab;
		}

		protected ActivityEditTaskParcelableDataStore(Parcel in) {
			if (ActivityEditTask.Debug) {
				Log.d(ActivityEditTask.DebugTag,
						"ActivityEditTaskParcelableDataStore(Parcel in)"
								+ System.identityHashCode(this));
			}
			mTaskWithDependentsUiData = (TaskWithDependentsUiData) in
					.readValue(TaskWithDependentsUiData.class.getClassLoader());
			userInterfaceData = (UserInterfaceData) in.readValue(UserInterfaceData.class
					.getClassLoader());
			taskEditMode = in.readByte() == 0x00 ? null : TaskEditMode.fromInt(in
					.readByte());
			tab = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeValue(mTaskWithDependentsUiData);
			dest.writeValue(userInterfaceData);
			if (taskEditMode == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(taskEditMode.getValue());
			}
			dest.writeInt(tab);
		}

		public static final Parcelable.Creator<ActivityEditTaskParcelableDataStore> CREATOR = new Parcelable.Creator<ActivityEditTaskParcelableDataStore>() {
			@Override
			public ActivityEditTaskParcelableDataStore createFromParcel(Parcel in) {
				return new ActivityEditTaskParcelableDataStore(in);
			}

			@Override
			public ActivityEditTaskParcelableDataStore[] newArray(int size) {
				return new ActivityEditTaskParcelableDataStore[size];
			}
		};
	}

	public void setupUi(TaskUiData taskUiData2) {
		mViewPager.setCurrentItem(0);
		FragmentEditTaskPartMain fragmentEditTaskPartMain = (biz.advancedcalendar.fragments.FragmentEditTaskPartMain) mActivityPagerAdapter
				.getRegisteredFragment(0);
		if (fragmentEditTaskPartMain != null && fragmentEditTaskPartMain.isAdded()) {
			fragmentEditTaskPartMain.setupUi(taskUiData2);
		} else {
			mActivityPagerAdapter.setSetupUiPending(taskUiData2);
		}
	}
}
