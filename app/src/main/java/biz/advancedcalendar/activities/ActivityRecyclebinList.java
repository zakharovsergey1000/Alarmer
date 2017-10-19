package biz.advancedcalendar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.activityedittask.TaskUiData2;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.ConfirmationAlertDialogFragment;
import biz.advancedcalendar.fragments.ConfirmationAlertDialogFragment.YesNoListener;
import biz.advancedcalendar.fragments.FragmentRecyclebinDetail;
import biz.advancedcalendar.fragments.FragmentRecyclebinList;
import biz.advancedcalendar.greendao.Calendar;
import java.util.ArrayList;
import java.util.List;

/** An activity representing a list of Calendars. This activity has different presentations
 * for handset and tablet-size devices. On handsets, the activity presents a list of
 * items, which when touched, lead to a {@link ActivityRecyclebinDetail} representing item
 * details. On tablets, the activity presents the list of items and item details
 * side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FragmentRecyclebinList} and the item details (if present) is a
 * {@link FragmentRecyclebinDetail}.
 * <p>
 * This activity also implements the required {@link FragmentRecyclebinList.Callbacks}
 * interface to listen for item selections. */
public class ActivityRecyclebinList extends AppCompatActivity implements
		FragmentRecyclebinList.Callbacks {
	/** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
	private Toolbar mToolbar;
	private StateParameters mStateParameters;
	private FragmentRecyclebinList mFragmentRecyclebinList;
	private FragmentRecyclebinDetail mFragmentRecyclebinDetail;
	private YesNoListener mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener;
	private YesNoListener mConfirmationAlertDialogFragmentUndeleteYesNoListener;

	private static class StateParameters implements Parcelable {
		private boolean IsTwoPane;
		private boolean IsActionMode;
		private boolean IsDetailMode;
		protected List<Calendar> PendingCalendars;

		protected StateParameters(Parcel in) {
			IsTwoPane = in.readByte() != 0x00;
			IsActionMode = in.readByte() != 0x00;
			IsDetailMode = in.readByte() != 0x00;
			in.readList(PendingCalendars, Calendar.class.getClassLoader());
		}

		// Mandatory empty constructor
		@SuppressWarnings("unused")
		public StateParameters() {
			// TODO Auto-generated constructor stub
		}

		public StateParameters(boolean isTwoPane, boolean isActionMode,
				boolean isDetailMode, List<Calendar> pendingCalendars) {
			super();
			IsTwoPane = isTwoPane;
			IsActionMode = isActionMode;
			IsDetailMode = isDetailMode;
			PendingCalendars = pendingCalendars;
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeByte((byte) (IsTwoPane ? 0x01 : 0x00));
			dest.writeByte((byte) (IsActionMode ? 0x01 : 0x00));
			dest.writeByte((byte) (IsDetailMode ? 0x01 : 0x00));
			dest.writeList(PendingCalendars);
		}

		@SuppressWarnings("unused")
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mStateParameters = savedInstanceState.getParcelable("mStateParameters");
		} else {
			mStateParameters = new StateParameters(false, false, false, null);
		}
		setContentView(R.layout.activity_recyclebin_singlepane);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();
		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);
		FragmentManager supportFragmentManager = getSupportFragmentManager();
		mFragmentRecyclebinList = (FragmentRecyclebinList) supportFragmentManager
				.findFragmentByTag("FragmentRecyclebinList");
		mFragmentRecyclebinDetail = (FragmentRecyclebinDetail) supportFragmentManager
				.findFragmentByTag("FragmentRecyclebinDetail");
		FragmentTransaction fragmentTransaction = supportFragmentManager
				.beginTransaction();
		if (mFragmentRecyclebinList == null) {
			mFragmentRecyclebinList = new FragmentRecyclebinList();
			fragmentTransaction.add(R.id.fragment_recyclebinlist_container,
					mFragmentRecyclebinList, "FragmentRecyclebinList");
		}
		if (mFragmentRecyclebinDetail == null) {
			mFragmentRecyclebinDetail = new FragmentRecyclebinDetail();
			fragmentTransaction.add(R.id.fragment_recyclebindetails_container,
					mFragmentRecyclebinDetail, "FragmentRecyclebinDetail");
		}
		if (findViewById(R.id.fragments_recyclebinlist_recyclebindetails_combined_container) == null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mStateParameters.IsTwoPane = true;
		} else {
			mStateParameters.IsTwoPane = false;
			if (mStateParameters.IsDetailMode) {
				fragmentTransaction.hide(mFragmentRecyclebinList);
				fragmentTransaction.show(mFragmentRecyclebinDetail);
			} else {
				fragmentTransaction.hide(mFragmentRecyclebinDetail);
				fragmentTransaction.show(mFragmentRecyclebinList);
			}
		}
		fragmentTransaction.commit();
		// supportFragmentManager.executePendingTransactions();
		mFragmentRecyclebinDetail.setTasks(mFragmentRecyclebinList.getSelectedTasks());
		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// setup listener
		ConfirmationAlertDialogFragment confirmationAlertDialogFragment = (ConfirmationAlertDialogFragment) getSupportFragmentManager()
				.findFragmentByTag("ConfirmationAlertDialogFragmentUndelete");
		if (confirmationAlertDialogFragment != null) {
			setupConfirmationAlertDialogFragmentUndeleteYesNoListener(confirmationAlertDialogFragment);
		}
		//
		confirmationAlertDialogFragment = (ConfirmationAlertDialogFragment) getSupportFragmentManager()
				.findFragmentByTag("ConfirmationAlertDialogFragmentDeletePermanently");
		if (confirmationAlertDialogFragment != null) {
			setupConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener(confirmationAlertDialogFragment);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		if (!mStateParameters.IsDetailMode && mStateParameters.IsActionMode
				|| mStateParameters.IsDetailMode && mStateParameters.IsActionMode
				&& mStateParameters.IsTwoPane) {
			MenuItem menuItem;
			if (!mStateParameters.IsTwoPane) {
				menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_VIEW, 0,
						getResources().getString(R.string.action_view));
				menuItem.setIcon(R.drawable.ic_visibility_black_24dp);
				MenuItemCompat.setShowAsAction(menuItem,
						MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			}
			//
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_UNDELETE, 0,
					getResources().getString(R.string.action_undelete));
			menuItem.setIcon(R.drawable.ic_undo_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
			//
			menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_DELETE_PERMANENTLY, 0,
					getResources().getString(R.string.action_delete_permanently));
			menuItem.setIcon(R.drawable.ic_delete_forever_black_24dp);
			MenuItemCompat
					.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable("mStateParameters", mStateParameters);
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			if (mStateParameters.IsDetailMode) {
				FragmentManager supportFragmentManager = getSupportFragmentManager();
				supportFragmentManager.beginTransaction().hide(mFragmentRecyclebinDetail)
						.show(mFragmentRecyclebinList).commit();
				mStateParameters.IsDetailMode = false;
				return true;
			}
			if (mStateParameters.IsActionMode) {
				mFragmentRecyclebinList.clearSelectedTasks();
				mStateParameters.IsActionMode = false;
				invalidateOptionsMenu();
				return true;
			}
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown.
			finish();
			return true;
		case CommonConstants.MENU_ID_VIEW:
			moveToDetailMode(mFragmentRecyclebinList.getSelectedTasks());
			return true;
		case CommonConstants.MENU_ID_UNDELETE:
			ConfirmationAlertDialogFragment confirmationAlertDialogFragmentUndelete = new ConfirmationAlertDialogFragment();
			confirmationAlertDialogFragmentUndelete.setTitleAndMessage(
					null,
					getResources().getString(
							R.string.action_undelete_checked_tasks_confirmation));
			setupConfirmationAlertDialogFragmentUndeleteYesNoListener(confirmationAlertDialogFragmentUndelete);
			confirmationAlertDialogFragmentUndelete.show(getSupportFragmentManager(),
					"ConfirmationAlertDialogFragmentUndelete");
			return true;
		case CommonConstants.MENU_ID_DELETE_PERMANENTLY:
			ConfirmationAlertDialogFragment confirmationAlertDialogFragmentDeletePermanently = new ConfirmationAlertDialogFragment();
			confirmationAlertDialogFragmentDeletePermanently
					.setTitleAndMessage(
							null,
							getResources()
									.getString(
											R.string.action_delete_checked_tasks_permanently_confirmation));
			setupConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener(confirmationAlertDialogFragmentDeletePermanently);
			confirmationAlertDialogFragmentDeletePermanently.show(
					getSupportFragmentManager(),
					"ConfirmationAlertDialogFragmentDeletePermanently");
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (mStateParameters.IsDetailMode) {
			FragmentManager supportFragmentManager = getSupportFragmentManager();
			supportFragmentManager.beginTransaction().hide(mFragmentRecyclebinDetail)
					.show(mFragmentRecyclebinList).commit();
			mStateParameters.IsDetailMode = false;
			return;
		} else if (mStateParameters.IsActionMode) {
			mFragmentRecyclebinList.clearSelectedTasks();
			mStateParameters.IsActionMode = false;
			invalidateOptionsMenu();
			return;
		}
		super.onBackPressed();
	}

	private void setupConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener(
			ConfirmationAlertDialogFragment confirmationAlertDialogFragment) {
		mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener = new ConfirmationAlertDialogFragment.YesNoListener() {
			@Override
			public void onYesInConfirmationAlertDialogFragment(
					ConfirmationAlertDialogFragment dialogFragment) {
				ArrayList<TaskUiData2> selectedTasks = mFragmentRecyclebinList
						.getSelectedTasks();
				for (int i = 0; i < selectedTasks.size(); i++) {
					TaskUiData2 taskWithDependents = selectedTasks.get(i);
					Long id2 = taskWithDependents.getId();
					if (id2 != null) {
						DataProvider.deleteTaskPermanently(null,
								ActivityRecyclebinList.this, id2, false);
					}
				}
				// mFragmentRecyclebinList.clearSelectedTasks();
				moveToInitialState();
				LocalBroadcastManager
						.getInstance(ActivityRecyclebinList.this)
						.sendBroadcast(
								new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			}

			@Override
			public void onNoInConfirmationAlertDialogFragment(
					ConfirmationAlertDialogFragment dialogFragment) {
				// TODO Auto-generated method stub
			}
		};
		confirmationAlertDialogFragment
				.setCallback(mConfirmationAlertDialogFragmentDeletePermanentlyYesNoListener);
	}

	private void setupConfirmationAlertDialogFragmentUndeleteYesNoListener(
			ConfirmationAlertDialogFragment confirmationAlertDialogFragment) {
		mConfirmationAlertDialogFragmentUndeleteYesNoListener = new ConfirmationAlertDialogFragment.YesNoListener() {
			@Override
			public void onYesInConfirmationAlertDialogFragment(
					ConfirmationAlertDialogFragment dialogFragment) {
				ArrayList<TaskUiData2> selectedTasks = mFragmentRecyclebinList
						.getSelectedTasks();
				for (int i = 0; i < selectedTasks.size(); i++) {
					TaskUiData2 taskWithDependents = selectedTasks.get(i);
					Long id2 = taskWithDependents.getId();
					if (id2 != null) {
						DataProvider.undeleteTask(null, ActivityRecyclebinList.this, id2);
					}
				}
				// mFragmentRecyclebinList.clearSelectedTasks();
				moveToInitialState();
				// mCallbacks.onItemsSelected(mStateParameters.PendingTasks);
				// if (mActionMode != null) {
				// mActionMode.finish();
				// }
				LocalBroadcastManager
						.getInstance(ActivityRecyclebinList.this)
						.sendBroadcast(
								new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
			}

			@Override
			public void onNoInConfirmationAlertDialogFragment(
					ConfirmationAlertDialogFragment dialogFragment) {
				// TODO Auto-generated method stub
			}
		};
		confirmationAlertDialogFragment
				.setCallback(mConfirmationAlertDialogFragmentUndeleteYesNoListener);
	}

	protected void moveToInitialState() {
		mStateParameters.IsDetailMode = false;
		mStateParameters.IsActionMode = false;
		mFragmentRecyclebinList.clearSelectedTasks();
		mFragmentRecyclebinDetail.setTasks(new ArrayList<TaskUiData2>());
		FragmentTransaction fragmentTransaction = getSupportFragmentManager()
				.beginTransaction();
		fragmentTransaction.show(mFragmentRecyclebinList);
		if (mStateParameters.IsTwoPane) {
			fragmentTransaction.show(mFragmentRecyclebinDetail);
		} else {
			fragmentTransaction.hide(mFragmentRecyclebinDetail);
		}
		fragmentTransaction.commit();
		invalidateOptionsMenu();
	}

	/** Callback method from {@link FragmentRecyclebinList.Callbacks} indicating that the
	 * item with the given ID was selected. */
	@Override
	public void onItemsSelected(ArrayList<TaskUiData2> taskWithDependentsList) {
		if (mStateParameters.IsTwoPane) {
			mFragmentRecyclebinDetail.setTasks(taskWithDependentsList);
		} else {
			if (taskWithDependentsList.size() > 0 && !mStateParameters.IsActionMode) {
				moveToDetailMode(taskWithDependentsList);
			}
		}
	}

	private void moveToDetailMode(ArrayList<TaskUiData2> taskWithDependentsList) {
		mStateParameters.IsDetailMode = true;
		mFragmentRecyclebinDetail.setTasks(taskWithDependentsList);
		FragmentManager supportFragmentManager = getSupportFragmentManager();
		supportFragmentManager.beginTransaction().hide(mFragmentRecyclebinList)
				.show(mFragmentRecyclebinDetail).commit();
		invalidateOptionsMenu();
	}

	@Override
	public void onActionModeStart() {
		mStateParameters.IsActionMode = true;
		mStateParameters.IsDetailMode = false;
		invalidateOptionsMenu();
	}

	@Override
	public void onActionModeEnd() {
		mStateParameters.IsActionMode = false;
		mStateParameters.IsDetailMode = false;
		invalidateOptionsMenu();
	}
	// @Override
	// public void onItemLongClick(TaskUiData2 calendar) {
	// // if (mStateParameters.IsTwoPane) {
	// // // In two-pane mode, show the detail view in this activity by
	// // // adding or replacing the detail fragment using a
	// // // fragment transaction.
	// // FragmentRecyclebinDetail fragment = new FragmentRecyclebinDetail();
	// // Bundle arguments = new Bundle();
	// // ArrayList<TaskUiData2> taskWithDependentsList = new ArrayList<TaskUiData2>();
	// // taskWithDependentsList.add(calendar);
	// // arguments.putParcelableArrayList(FragmentRecyclebinDetail.ARG_ITEM_ID,
	// // taskWithDependentsList);
	// // fragment.setArguments(arguments);
	// // getSupportFragmentManager().beginTransaction()
	// // .replace(R.id.task_detail_container, fragment).commit();
	// // } else {
	// // // In single-pane mode do nothing
	// // }
	// }
}
