package biz.advancedcalendar.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityEditTask.ActivityEditTaskParcelableDataStore;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.TaskUiData;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;
import biz.advancedcalendar.fragments.FragmentViewTaskPartContacts;
import biz.advancedcalendar.fragments.FragmentViewTaskPartLabels;
import biz.advancedcalendar.fragments.FragmentViewTaskPartMain;
import biz.advancedcalendar.fragments.FragmentViewTaskPartReminders;
import biz.advancedcalendar.fragments.TaskWithDependentsUiDataHolder;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.TaskEditMode;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.server.GetUserInfoResult;
import biz.advancedcalendar.server.ServerProvider;
import biz.advancedcalendar.sync.SyncService;
import biz.advancedcalendar.utils.Helper;

public class ActivityViewTask extends AppCompatActivity implements
		android.support.design.widget.TabLayout.OnTabSelectedListener,
		TaskWithDependentsUiDataHolder {
	ActivityViewTaskPagerAdapter mActivityPagerAdapter;
	private ViewPager mViewPager;
	private RetainedFragmentForActivityViewTask mParcelableDataStore;
	private GetUserInfoTask mGetUserInfoTask = null;
	private TaskWithDependentsUiData taskWithDependentsUiData;
	private TaskUiData taskUiData;
	private Toolbar mToolbar;
	private TabLayout mTabLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pager);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		mTabLayout = (TabLayout) findViewById(R.id.tablayout);
		mTabLayout.setOnTabSelectedListener(this);
		mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
		FragmentManager fm = getSupportFragmentManager();
//		dataFragment = (RetainedFragmentForActivityViewTask) fm
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
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
					TaskWithDependents taskWithDependents = DataProvider.getTaskWithDependents(
							null, context, taskUiData.getId());
					if (taskWithDependents == null) {
						finish();
					} else {
						taskWithDependentsUiData = new TaskWithDependentsUiData(
								taskWithDependents);
						mParcelableDataStore.setTaskWithDependentsUiData(taskWithDependentsUiData);
					}
				}
			}
		}, new IntentFilter(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		mActivityPagerAdapter = new ActivityViewTaskPagerAdapter(this,
				getSupportFragmentManager());
		final ActionBar actionBar = getSupportActionBar();
		setTitle(taskUiData.getName());
		actionBar.setDisplayHomeAsUpEnabled(true);
		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the user swipes between sections.
		mViewPager = (ViewPager) findViewById(R.id.activity_pager);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.logout_help_settings, menu);
		MenuItem editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_EDIT, 0,
				getResources().getString(R.string.action_edit));
		editItem.setIcon(R.drawable.ic_mode_edit_black_24dp);
		MenuItemCompat.setShowAsAction(editItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_COPY, 0, getResources()
				.getString(R.string.action_copy));
		editItem.setIcon(R.drawable.ic_content_copy_black_24dp);
		MenuItemCompat.setShowAsAction(editItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		// if (dataFragment.getTaskWithDependencies().task.getPercentOfCompletion() ==
		// 100) {
		// editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SET_ACTIVE, 1,
		// getResources().getString(R.string.action_set_active));
		// // editItem.setIcon(R.drawable.ic_navigation_accept);
		// } else {
		// editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SET_COMPLETED, 1,
		// getResources().getString(R.string.action_set_completed));
		// }
		// MenuItemCompat.setShowAsAction(editItem,
		// MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		editItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_DELETE, 1, getResources()
				.getString(R.string.action_delete));
		editItem.setIcon(R.drawable.ic_delete_black_24dp);
		MenuItemCompat.setShowAsAction(editItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case CommonConstants.MENU_ID_EDIT:
			int tab = CommonConstants.INTENT_EXTRA_VALUE_TAB_TASK;
			switch (mViewPager.getCurrentItem()) {
			case 0:
				tab = CommonConstants.INTENT_EXTRA_VALUE_TAB_TASK;
				break;
			case 1:
				tab = CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS;
			}
			ActivityMain.launchTaskEditor(this, taskUiData.getId(), TaskEditMode.EDIT,
					tab);
			return true;
		case CommonConstants.MENU_ID_COPY:
			ActivityMain.launchTaskEditor2(this, taskWithDependentsUiData,
					TaskEditMode.ADD, CommonConstants.INTENT_EXTRA_VALUE_TAB_TASK);
			return true;
		case CommonConstants.MENU_ID_SET_ACTIVE:
		case CommonConstants.MENU_ID_SET_COMPLETED:
			if (item.getItemId() == CommonConstants.MENU_ID_SET_ACTIVE) {
				DataProvider.markTasksAsCompleted(null, this,
						new long[] {taskUiData.getId()}, false);
			} else {
				DataProvider.markTasksAsCompleted(null, this,
						new long[] {taskUiData.getId()}, true);
			}
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
					.getIntegerPreferenceValueFromStringArray(ActivityViewTask.this,
							R.string.preference_key_sync_policy,
							R.array.sync_policy_values_array,
							R.integer.sync_policy_default_value));
			switch (syncPolicy) {
			case DO_NOT_SYNC:
				return true;
			default:
				break;
			}
			if (!Helper.isDeviceOnline(ActivityViewTask.this)) {
				Toast.makeText(ActivityViewTask.this, R.string.the_device_is_not_online,
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
									DataProvider.markTasksAsDeleted(null,
											getApplicationContext(),
											new long[] {taskUiData.getId()},
											selectedItems[0]);
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
													ActivityViewTask.this,
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
									if (!Helper.isDeviceOnline(ActivityViewTask.this)) {
										Toast.makeText(ActivityViewTask.this,
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
		}
		return super.onOptionsItemSelected(item);
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

	private class ActivityViewTaskPagerAdapter extends FragmentPagerAdapter {
		SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();
		private Context mContext;

		public ActivityViewTaskPagerAdapter(Context context, FragmentManager fm) {
			super(fm);
			mContext = context;
		}

		@Override
		public Fragment getItem(int i) {
			Bundle b = new Bundle();
			// b.putLong(CommonConstants.INTENT_EXTRA_ID,
			// dataFragment.getTaskWithDependencies().task.getId());
			Fragment f;
			switch (i) {
			case 0:
				f = new FragmentViewTaskPartMain();
				f.setArguments(b);
				return f;
			case 1:
				f = new FragmentViewTaskPartReminders();
				f.setArguments(b);
				return f;
			case 2:
				f = new FragmentViewTaskPartLabels();
				f.setArguments(b);
				return f;
			case 3:
				f = new FragmentViewTaskPartContacts();
				f.setArguments(b);
				return f;
			case 4:
				f = new FragmentViewTaskPartMain();
				f.setArguments(b);
				return f;
			case 5:
				f = new FragmentViewTaskPartMain();
				f.setArguments(b);
				return f;
			case 6:
				f = new FragmentViewTaskPartMain();
				f.setArguments(b);
				return f;
			default:
				f = new FragmentViewTaskPartMain();
				f.setArguments(b);
				return f;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Fragment fragment = (Fragment) super.instantiateItem(container, position);
			registeredFragments.put(position, fragment);
			return fragment;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			registeredFragments.remove(position);
			super.destroyItem(container, position, object);
		}

		// public Fragment getRegisteredFragment(int position) {
		// return registeredFragments.get(position);
		// }
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
						R.string.activity_view_task_tab_labels);
			case 3:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_contacts);
			case 4:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_reminders);
			case 5:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_access);
			case 6:
				return mContext.getResources().getString(
						R.string.activity_view_task_tab_messages);
			default:
				return "";
			}
		}
	}

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

	@Override
	public TaskWithDependentsUiData getTaskWithDependentsUiData() {
		return mParcelableDataStore.getTaskWithDependentsUiData();
	}

	@Override
	public UserInterfaceData getUserInterfaceData() {
		return null;
	}

	public ActivityEditTaskParcelableDataStore getmParcelableDataStore() {
		return null;
	}
}
