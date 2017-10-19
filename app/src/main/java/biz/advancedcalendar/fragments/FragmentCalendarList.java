package biz.advancedcalendar.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.CalendarNameDialogFragment;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.ConfirmationAlertDialogFragment.YesNoListener;
import biz.advancedcalendar.greendao.Calendar;
import java.util.ArrayList;
import java.util.List;

/** A list fragment representing a list of Calendars. This fragment also supports tablet
 * devices by allowing list items to be given an 'activated' state upon selection. This
 * helps indicate which item is currently being viewed in a {@link FragmentCalendarDetail}
 * .
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface. */
public class FragmentCalendarList extends ListFragment implements
		CalendarNameDialogFragment.YesNoListener,
		ConfirmationAlertDialogFragment.YesNoListener {
	/** The serialization (saved instance state) Bundle key representing the activated item
	 * position. Only used on tablets. */
	/** The fragment's current callback object, which is notified of list item clicks. */
	private Callbacks mCallbacks = FragmentCalendarList.sDummyCallbacks;
	/** The current activated item position. Only used on tablets. */
	// private int mActivatedPosition = ListView.INVALID_POSITION;
	private List<Calendar> mCalendars;
	private BroadcastReceiver mReceiver;
	private StateParameters mStateParameters;
	private CalendarNameDialogFragment.YesNoListener mCalendarNameDialogFragmentRenameYesNoListener;

	private static class StateParameters implements Parcelable {
		Integer mActivatedPosition;
		// Calendar PendingCalendar;
		protected List<Calendar> PendingCalendars;

		protected StateParameters(Parcel in) {
			mActivatedPosition = in.readByte() == 0x00 ? null : in.readInt();
			// PendingCalendar = (Calendar) in.readValue(Calendar.class.getClassLoader());
			in.readList(PendingCalendars, Calendar.class.getClassLoader());
		}

		public StateParameters() {
			// TODO Auto-generated constructor stub
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (mActivatedPosition == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeInt(mActivatedPosition);
			}
			// dest.writeValue(PendingCalendar);
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

	/** A callback interface that all activities containing this fragment must implement.
	 * This mechanism allows activities to be notified of item selections. */
	public interface Callbacks {
		/** Callback for when an item has been selected. */
		public void onItemSelected(Long id);

		public void onItemLongClick(Long id);
	}

	/** A dummy implementation of the {@link Callbacks} interface that does nothing. Used
	 * only when this fragment is not attached to an activity. */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(Long id) {
		}

		@Override
		public void onItemLongClick(Long id) {
		}
	};
	private ListView mListview;
	private FragmentCalendarListAdapter mAdapter;
	protected ActionMode mActionMode;
	protected YesNoListener mConfirmationAlertDialogFragmentDeleteYesNoListener;

	/** Mandatory empty constructor for the fragment manager to instantiate the fragment
	 * (e.g. upon screen orientation changes). */
	public FragmentCalendarList() {
		mStateParameters = new StateParameters();
	}

	private static class FragmentCalendarListAdapter extends
			ArrayAdapter<biz.advancedcalendar.greendao.Calendar> {
		private Integer mActivatedPosition;
		private ActionMode mActionMode;
		private Drawable mDefaultBackground;
		private Drawable mStateActivatedDrawable;

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		public FragmentCalendarListAdapter(Context context, int resource,
				int textViewResourceId, List<Calendar> objects) {
			super(context, resource, textViewResourceId, objects);
			View convertView = LayoutInflater.from(getContext()).inflate(resource, null);
			mDefaultBackground = convertView.getBackground();
			mStateActivatedDrawable = null;
			if (mDefaultBackground != null) {
				mDefaultBackground
						.setState(new int[] {android.os.Build.VERSION.SDK_INT >= 11 ? android.R.attr.state_activated
								: android.R.attr.state_selected});
				mDefaultBackground.jumpToCurrentState();
				mStateActivatedDrawable = mDefaultBackground.getCurrent();
			}
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Get the data item for this position
			biz.advancedcalendar.greendao.Calendar calendar = getItem(position);
			// Check if an existing view is being reused, otherwise inflate the view
			if (convertView == null) {
				convertView = LayoutInflater
						.from(getContext())
						.inflate(
								android.os.Build.VERSION.SDK_INT >= 11 ? android.R.layout.simple_list_item_activated_1
										: android.R.layout.simple_list_item_checked,
								parent, false);
			}
			// Lookup view for data population
			TextView tvName = (TextView) convertView.findViewById(android.R.id.text1);
			// Populate the data into the template view using the data object
			tvName.setText(calendar.getName());
			if (mActionMode == null && mActivatedPosition != null
					&& mActivatedPosition == position) {
				// convertView.setBackgroundColor(getContext().getResources().getColor(
				// android.R.color.holo_green_light));
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					convertView.setBackground(mStateActivatedDrawable);
				} else {
					convertView.setBackgroundDrawable(mStateActivatedDrawable);
				}
				// convertView.setActivated(true);
			} else {
				if (android.os.Build.VERSION.SDK_INT >= 16) {
					convertView.setBackground(mDefaultBackground == null ? null
							: mDefaultBackground.getConstantState().newDrawable());
				} else {
					convertView.setBackgroundDrawable(mDefaultBackground == null ? null
							: mDefaultBackground.getConstantState().newDrawable());
				}
				// convertView.setActivated(false);
			}
			// Return the completed view to render on screen
			return convertView;
		}

		public void setActivatedItem(int position) {
			if (mActivatedPosition == null || mActivatedPosition != position) {
				mActivatedPosition = position;
				notifyDataSetChanged();
			}
		}

		public void setActionMode(ActionMode mode) {
			mActionMode = mode;
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}
		mCallbacks = (Callbacks) activity;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mStateParameters = savedInstanceState.getParcelable("mStateParameters");
		}
		// TODO: replace with a real list adapter.
		mCalendars = new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider.getCalendars(null, getActivity()));
		mCalendars.add(0, new biz.advancedcalendar.greendao.Calendar(null, null,
				getResources().getString(R.string.default_calendar_name)));
		mAdapter = new FragmentCalendarListAdapter(
				getActivity(),
				android.os.Build.VERSION.SDK_INT >= 11 ? android.R.layout.simple_list_item_activated_1
						: android.R.layout.simple_list_item_checked, android.R.id.text1,
				mCalendars);
		setListAdapter(mAdapter);
		if (mReceiver == null) {
			mReceiver = new BroadcastReceiver() {
				@SuppressLint("InlinedApi")
				@Override
				public void onReceive(Context context, Intent intent) {
					if (intent.getAction().equals(
							CommonConstants.ACTION_CALENDARS_CHANGED)) {
						// if (!isDetached()) {
						if (isAdded()) {
							List<Calendar> calendars = new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider.getCalendars(null,
									getActivity()));
							mCalendars.clear();
							mCalendars.add(
									0,
									new biz.advancedcalendar.greendao.Calendar(null,
											null, getResources().getString(
													R.string.default_calendar_name)));
							mCalendars.addAll(calendars);
							mAdapter.notifyDataSetChanged();
						} else {
							// mStateParameters.TasksNeedReload = true;
						}
					}
				}
			};
			//
			IntentFilter intentFilter = new IntentFilter();
			// intentFilter.addAction(CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED);
			intentFilter.addAction(CommonConstants.ACTION_CALENDARS_CHANGED);
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.registerReceiver(mReceiver, intentFilter);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null && mStateParameters == null) {
			mStateParameters = savedInstanceState.getParcelable("mStateParameters");
		}
		if (mStateParameters.mActivatedPosition != null) {
			setActivatedPosition(mStateParameters.mActivatedPosition);
		}
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		// setup listener
		CalendarNameDialogFragment calendarNameDialogFragment = (CalendarNameDialogFragment) getFragmentManager()
				.findFragmentByTag("CalendarNameDialogFragmentRename");
		if (calendarNameDialogFragment != null) {
			setupCalendarNameDialogFragmentRenameYesNoListener(calendarNameDialogFragment);
		}
		ConfirmationAlertDialogFragment confirmationAlertDialogFragment = (ConfirmationAlertDialogFragment) getFragmentManager()
				.findFragmentByTag("ConfirmationAlertDialogFragmentDelete");
		if (confirmationAlertDialogFragment != null) {
			setupCalendarNameDialogFragmentDeleteYesNoListener(confirmationAlertDialogFragment);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mListview = getListView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			//
			mListview.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
			mListview.setMultiChoiceModeListener(new MultiChoiceModeListener() {
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position,
						long id, boolean checked) {
					// Here you can do something when items are
					// selected/re-selected,
					// such as update the title in the CAB
					mode.invalidate();
				}

				@Override
				public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
					final SparseBooleanArray checkedItemPositions = mListview
							.getCheckedItemPositions();
					final int size = checkedItemPositions.size();
					List<Calendar> checkedCalendars = new ArrayList<Calendar>();
					for (int i = 0; i < size; i++) {
						if (checkedItemPositions.valueAt(i)) {
							Calendar calendar = (Calendar) mListview
									.getItemAtPosition(checkedItemPositions.keyAt(i));
							checkedCalendars.add(calendar);
						}
					}
					mStateParameters.PendingCalendars = checkedCalendars;
					switch (item.getItemId()) {
					case CommonConstants.MENU_ID_RENAME:
						Long id;
						Calendar calendar1 = null;
						for (int i = 0; i < size; i++) {
							if (checkedItemPositions.valueAt(i)) {
								calendar1 = (Calendar) mListview
										.getItemAtPosition(checkedItemPositions.keyAt(i));
								id = calendar1.getId();
								if (id == null) {
									return true;
								}
							}
						}
						//
						CalendarNameDialogFragment calendarNameDialogFragment = new CalendarNameDialogFragment();
						calendarNameDialogFragment
								.setCalendarName(mStateParameters.PendingCalendars.get(0)
										.getName());
						setupCalendarNameDialogFragmentRenameYesNoListener(calendarNameDialogFragment);
						calendarNameDialogFragment.show(getFragmentManager(),
								"CalendarNameDialogFragmentRename");
						return true;
					case CommonConstants.MENU_ID_DELETE:
						ConfirmationAlertDialogFragment confirmationAlertDialogFragment = new ConfirmationAlertDialogFragment();
						confirmationAlertDialogFragment
								.setTitleAndMessage(
										null,
										getActivity()
												.getResources()
												.getString(
														R.string.action_delete_checked_calendars_confirmation));
						setupCalendarNameDialogFragmentDeleteYesNoListener(confirmationAlertDialogFragment);
						confirmationAlertDialogFragment.show(getFragmentManager(),
								"ConfirmationAlertDialogFragmentDelete");
						//
						//
						//
						// Action picked, so close the CAB
						return true;
					default:
						return false;
					}
				}

				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// Inflate the menu for the CAB
					mActionMode = mode;
					mAdapter.setActionMode(mode);
					// onPrepareActionMode(mode, menu);
					return true;
				}

				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// Here you can make any necessary updates to the activity
					// when
					// the CAB is removed. By default, selected items are
					// deselected/unchecked.
					mActionMode = null;
					mAdapter.setActionMode(null);
				}

				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Here you can perform updates to the CAB due to
					// an invalidate() request
					final SparseBooleanArray checkedItemPositions = mListview
							.getCheckedItemPositions();
					int size = checkedItemPositions.size();
					boolean addRenameAction = true;
					boolean addDeleteAction = true;
					int cerber = 0;
					for (int i = 0; i < size; i++) {
						if (checkedItemPositions.valueAt(i)) {
							cerber++;
							addRenameAction = cerber == 1 ? true : false;
							final Calendar calendar = (Calendar) mListview
									.getItemAtPosition(checkedItemPositions.keyAt(i));
							if (calendar.getId() == null) {
								addRenameAction = false;
								addDeleteAction = false;
							}
							if (!addRenameAction && !addDeleteAction) {
								break;
							}
						}
					}
					menu.clear();
					if (addRenameAction) {
						MenuItem menuItem = menu.add(Menu.NONE,
								CommonConstants.MENU_ID_RENAME, Menu.FIRST + 100,
								getResources().getString(R.string.rename));
						menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					}
					if (addDeleteAction) {
						MenuItem menuItem = menu.add(Menu.NONE,
								CommonConstants.MENU_ID_DELETE, Menu.FIRST + 200,
								getResources().getString(R.string.delete));
						menuItem.setIcon(R.drawable.ic_delete_black_24dp);
						menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					}
					return true;
				}
			});
			//
		} else {
			registerForContextMenu(mListview);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			MenuItem addItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_ADD_CALENDAR,
					0, activity.getResources().getString(R.string.action_calendar_new));
			addItem.setIcon(R.drawable.ic_add_black_24dp);
			MenuItemCompat.setShowAsAction(addItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_ADD_CALENDAR:
			mStateParameters.PendingCalendars = new ArrayList<Calendar>();
			mStateParameters.PendingCalendars.add(new Calendar(null, null, ""));
			//
			CalendarNameDialogFragment calendarNameDialogFragment = new CalendarNameDialogFragment();
			mCalendarNameDialogFragmentRenameYesNoListener = new CalendarNameDialogFragment.YesNoListener() {
				@Override
				public void onYesInCalendarNameDialogFragment(String calendarName) {
					mStateParameters.PendingCalendars.get(0).setName(calendarName);
					DataProvider.insertOrReplaceCalendar(null, getActivity(),
							mStateParameters.PendingCalendars.get(0));
					LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
							new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
				}

				@Override
				public void onNoInCalendarNameDialogFragment() {
					// TODO Auto-generated method stub
				}
			};
			calendarNameDialogFragment
					.setCallback(mCalendarNameDialogFragmentRenameYesNoListener);
			calendarNameDialogFragment.show(getFragmentManager(),
					"CalendarNameDialogFragmentRename");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// menu.setHeaderTitle("Context Menu");
		menu.add(0, R.id.rename, 0, R.string.rename);
		menu.add(0, R.id.delete, 0, R.string.delete);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		ListView.AdapterContextMenuInfo menuInfo = (ListView.AdapterContextMenuInfo) item
				.getMenuInfo();
		final Calendar calendar = (Calendar) mListview
				.getItemAtPosition(menuInfo.position);
		final Long calendarId;
		calendarId = calendar.getId();
		if (calendarId == null) {
			mStateParameters.PendingCalendars = null;
			return true;
		}
		mStateParameters.PendingCalendars = new ArrayList<Calendar>();
		mStateParameters.PendingCalendars.add(calendar);
		switch (item.getItemId()) {
		case R.id.rename:
			CalendarNameDialogFragment calendarNameDialogFragment = new CalendarNameDialogFragment();
			calendarNameDialogFragment.setCalendarName(calendar.getName());
			setupCalendarNameDialogFragmentRenameYesNoListener(calendarNameDialogFragment);
			calendarNameDialogFragment.show(getFragmentManager(),
					"CalendarNameDialogFragmentRename");
			return true;
		case R.id.delete:
			ConfirmationAlertDialogFragment confirmationAlertDialogFragment = new ConfirmationAlertDialogFragment();
			confirmationAlertDialogFragment.setTitleAndMessage(
					null,
					getActivity().getResources().getString(
							R.string.action_delete_checked_calendar_confirmation));
			setupCalendarNameDialogFragmentDeleteYesNoListener(confirmationAlertDialogFragment);
			confirmationAlertDialogFragment.show(getFragmentManager(),
					"ConfirmationAlertDialogFragmentDelete");
			return true;
		default:
			return false;
		}
	}

	private void setupCalendarNameDialogFragmentRenameYesNoListener(
			CalendarNameDialogFragment calendarNameDialogFragment) {
		mCalendarNameDialogFragmentRenameYesNoListener = new CalendarNameDialogFragment.YesNoListener() {
			@Override
			public void onYesInCalendarNameDialogFragment(String calendarName) {
				mStateParameters.PendingCalendars.get(0).setName(calendarName);
				DataProvider.insertOrReplaceCalendar(null, getActivity(),
						mStateParameters.PendingCalendars.get(0));
				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
						new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
				if (mActionMode != null) {
					mActionMode.finish();
				}
			}

			@Override
			public void onNoInCalendarNameDialogFragment() {
				// TODO Auto-generated method stub
			}
		};
		calendarNameDialogFragment
				.setCallback(mCalendarNameDialogFragmentRenameYesNoListener);
	}

	private void setupCalendarNameDialogFragmentDeleteYesNoListener(
			ConfirmationAlertDialogFragment confirmationAlertDialogFragment) {
		mConfirmationAlertDialogFragmentDeleteYesNoListener = new ConfirmationAlertDialogFragment.YesNoListener() {
			@Override
			public void onYesInConfirmationAlertDialogFragment(
					ConfirmationAlertDialogFragment dialogFragment) {
				for (int i = 0; i < mStateParameters.PendingCalendars.size(); i++) {
					Calendar calendar = mStateParameters.PendingCalendars.get(i);
					Long id2 = calendar.getId();
					if (id2 != null) {
						DataProvider.deleteCalendar(null, getActivity(), id2);
					}
				}
				LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
						new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
				if (mActionMode != null) {
					mActionMode.finish();
				}
			}

			@Override
			public void onNoInConfirmationAlertDialogFragment(
					ConfirmationAlertDialogFragment dialogFragment) {
				// TODO Auto-generated method stub
			}
		};
		confirmationAlertDialogFragment
				.setCallback(mConfirmationAlertDialogFragmentDeleteYesNoListener);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = FragmentCalendarList.sDummyCallbacks;
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
	public void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(mCalendars.get(position).getId());
		setActivatedPosition(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("mStateParameters", mStateParameters);
	}

	/** Turns on activate-on-click mode. When this mode is on, list items will be given the
	 * 'activated' state when touched. */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE
						: AbsListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		// if (position == AdapterView.INVALID_POSITION) {
		// getListView().setItemChecked(mStateParameters.mActivatedPosition, false);
		// } else {
		// getListView().setItemChecked(position, true);
		// // getListView().setSelection(position);
		// }
		mStateParameters.mActivatedPosition = position;
		mAdapter.setActivatedItem(mStateParameters.mActivatedPosition);
	}

	@Override
	public void onYesInCalendarNameDialogFragment(String calendarName) {
		mStateParameters.PendingCalendars.get(0).setName(calendarName);
		DataProvider.insertOrReplaceCalendar(null, getActivity(),
				mStateParameters.PendingCalendars.get(0));
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
				new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
	}

	@Override
	public void onNoInCalendarNameDialogFragment() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onYesInConfirmationAlertDialogFragment(
			ConfirmationAlertDialogFragment dialogFragment) {
		for (Calendar calendar : mStateParameters.PendingCalendars) {
			DataProvider.deleteCalendar(null, getActivity(), calendar.getId());
		}
		LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
				new Intent(CommonConstants.ACTION_CALENDARS_CHANGED));
	}

	@Override
	public void onNoInConfirmationAlertDialogFragment(
			ConfirmationAlertDialogFragment dialogFragment) {
		// TODO Auto-generated method stub
	}
}
