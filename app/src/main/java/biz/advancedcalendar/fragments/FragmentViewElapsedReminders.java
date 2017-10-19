package biz.advancedcalendar.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.BooleanHolder;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.ElapsedReminder;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CheckableLinearLayout;
import biz.advancedcalendar.views.ExpandableListView2;
import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialog2.OnTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.OnMultipleTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.TimeAttribute;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FragmentViewElapsedReminders extends Fragment implements OnTimeSetListener,
		OnMultipleTimeSetListener {
	final int ADD_REMINDER_ID = Menu.FIRST + 100 - 1;
	final int EDIT_REMINDER_REQUEST = 1;
	final int ADD_REMINDER_REQUEST = 2;
	private BroadcastReceiver mBroadcastReceiver;
	private ExpandableListView2 mExpandableListView2;
	private FragmentViewElapsedRemindersAdapter mAdapter;
	List<ElapsedReminder> mElapsedReminders;
	private String mTimePickerDialogKey = "biz.advancedcalendar.fragments.FragmentViewElapsedReminders.TimePickerDialog";
	private String mPendingScheduledReminderForTimePickerDialogKey = "mPendingScheduledReminderForTimePickerDialogKey";
	private String mAdapterStateKey = "biz.advancedcalendar.fragments.FragmentViewElapsedReminders.AdapterStateKey";
	// variables to be saved in onSaveInstanceState()
	private ScheduledReminder mPendingScheduledReminderForTimePickerDialog;
	private ColorStateList mDefaultTextColorStateList;
	private Drawable mDefaultBackground;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS)) {
					mPendingScheduledReminderForTimePickerDialog = null;
					if (isAdded()) {
						if (getActivity() == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						fillListView(null);
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_elapsed_reminders, container, false);
		mExpandableListView2 = (ExpandableListView2) v
				.findViewById(R.id.fragment_elapsed_reminders_expandable_list_view);
		// mExpandableListView2.setOverScrollMode(View.OVER_SCROLL_NEVER);
		// expandList.setOnChildClickListener(new OnChildClickListener() {
		// @Override
		// public boolean onChildClick(ExpandableListView parent, View v,
		// int groupPosition, int childPosition, final long id) {
		// DataProvider.runInTx(getActivity().getApplicationContext(),
		// new Runnable() {
		// @Override
		// public void run() {
		// ElapsedReminder reminder = DataProvider
		// .getElapsedReminder(getActivity()
		// .getApplicationContext(), id);
		// if (reminder != null && reminder.getReminder() != null) {
		// Intent intent = new Intent(getActivity(),
		// ActivityEditTask.class);
		// intent.putExtra(CommonConstants.INTENT_EXTRA_ID,
		// reminder.getReminder()
		// .getTask2(getActivity()).getId());
		// intent.putExtra(
		// CommonConstants.INTENT_EXTRA_TAB,
		// CommonConstants.INTENT_EXTRA_VALUE_TAB_REMINDERS);
		// startActivity(intent);
		// }
		// }
		// });
		// return true;
		// }
		// });
		TableRow tableRow = (TableRow) inflater.inflate(
				R.layout.fragment_view_task_part_main_table_row, null);
		TextView textView = (TextView) tableRow
				.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
		mDefaultTextColorStateList = textView.getTextColors();
		mDefaultBackground = textView.getBackground();
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			TimePickerDialogMultiple tpd = (TimePickerDialogMultiple) getFragmentManager()
					.findFragmentByTag(mTimePickerDialogKey);
			if (tpd != null) {
				tpd.setOnTimeSetListener(this);
			}
			mPendingScheduledReminderForTimePickerDialog = savedInstanceState
					.getParcelable(mPendingScheduledReminderForTimePickerDialogKey);
		}
		setHasOptionsMenu(true);
		final FragmentActivity context = getActivity();
		if (isVisible()) {
			// Dismiss notification
			NotificationManager notificationManager = (NotificationManager) context
					.getApplicationContext().getSystemService(
							Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(CommonConstants.REMINDER_NOTIFICATION_ID);
			final BooleanHolder changed = new BooleanHolder(false);
			DataProvider.runInTx(null, context, new Runnable() {
				@Override
				public void run() {
					List<ElapsedReminder> reminderList = DataProvider
							.getElapsedReminders(null, context.getApplicationContext());
					for (ElapsedReminder elapsedReminder : reminderList) {
						if (elapsedReminder.getShowInNotifications()) {
							elapsedReminder.setShowInNotifications(false);
							DataProvider.insertOrReplaceElapsedReminder(null,
									context.getApplicationContext(), elapsedReminder);
							changed.value = true;
						}
					}
				}
			});
			if (changed.value) {
				LocalBroadcastManager.getInstance(context.getApplicationContext())
						.unregisterReceiver(mBroadcastReceiver);
				LocalBroadcastManager
						.getInstance(context.getApplicationContext())
						.sendBroadcast(
								new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
				LocalBroadcastManager
						.getInstance(context.getApplicationContext())
						.sendBroadcast(
								new Intent(
										CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			}
		}
		Parcelable state = null;
		if (savedInstanceState != null) {
			state = savedInstanceState.getParcelable(mAdapterStateKey);
		}
		fillListView(state);
		IntentFilter filter = new IntentFilter(
				CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
		filter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS);
		LocalBroadcastManager.getInstance(context.getApplicationContext())
				.registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem addItem;
		boolean isAscending = Helper.getBooleanPreferenceValue(getActivity(),
				R.string.preference_key_sort_order_ascending, false);
		Resources resources = getResources();
		if (isAscending) {
			addItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SORT_DESCENDING,
					Menu.FIRST + 100,
					resources.getString(R.string.action_show_latest_first));
			addItem.setIcon(R.drawable.ic_swap_vert_black_24dp);
		} else {
			addItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_SORT_ASCENDING,
					Menu.FIRST + 100,
					resources.getString(R.string.action_show_oldest_first));
			addItem.setIcon(R.drawable.ic_swap_vert_black_24dp);
		}
		MenuItemCompat.setShowAsAction(addItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
		//
		addItem = menu.add(Menu.NONE,
				CommonConstants.MENU_ID_DISCARD_ELAPSED_REMINDERS_ALL, Menu.FIRST + 200,
				resources.getString(R.string.action_discard_all));
		MenuItemCompat.setShowAsAction(addItem, MenuItemCompat.SHOW_AS_ACTION_NEVER);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		final FragmentActivity context = getActivity();
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_SORT_DESCENDING:
			PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit()
					.putBoolean(
							getResources().getString(
									R.string.preference_key_sort_order_ascending), false)
					.commit();
			// LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
			// new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			fillListView(null);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				context.invalidateOptionsMenu();
			} else {
				ActivityCompat.invalidateOptionsMenu(context);
			}
			return true;
		case CommonConstants.MENU_ID_SORT_ASCENDING:
			PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit()
					.putBoolean(
							getResources().getString(
									R.string.preference_key_sort_order_ascending), true)
					.commit();
			// LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
			// new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			fillListView(null);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				context.invalidateOptionsMenu();
			} else {
				ActivityCompat.invalidateOptionsMenu(context);
			}
			return true;
		case CommonConstants.MENU_ID_DISCARD_ELAPSED_REMINDERS_ALL:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			// String[] items = new String[] {getResources().getString(
			// R.string.action_dont_bother_anymore)};
			// final boolean[] selectedItems = new boolean[] {false};
			alertDialogBuilder
					.setTitle(
							getResources()
									.getString(
											R.string.action_discard_all_elapsed_reminders_confirmation))
					//
					// .setMultiChoiceItems(items, selectedItems,
					// new DialogInterface.OnMultiChoiceClickListener() {
					// @Override
					// public void onClick(DialogInterface dialog,
					// int indexSelected, boolean isChecked) {
					// selectedItems[0] = isChecked;
					// }
					// })
					//
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
									DataProvider.deleteElapsedReminders(null, context);
									// notify about changes
									LocalBroadcastManager
											.getInstance(context)
											.sendBroadcast(
													new Intent(
															CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
								}
							}).setNegativeButton(R.string.alert_dialog_cancel, null);
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		// menu.setHeaderTitle("Context Menu");
		menu.add(0, CommonConstants.MENU_ID_DISCARD, 0,
				getResources().getString(R.string.action_discard));
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final FragmentActivity context = getActivity();
		final Resources resources = getResources();
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_DISCARD:
			boolean showAlertDialog = Helper
					.getBooleanPreferenceValue(
							context,
							resources
									.getString(R.string.preference_key_show_alert_dialog_on_discard_selected_elapsed_reminders_button_press),
							true);
			if (showAlertDialog) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				String[] items = new String[] {resources
						.getString(R.string.action_dont_bother_anymore)};
				final boolean[] selectedItems = new boolean[] {false};
				alertDialogBuilder
						.setTitle(
								resources
										.getString(R.string.action_discard_elapsed_reminder_confirmation))
						.setMultiChoiceItems(items, selectedItems,
								new DialogInterface.OnMultiChoiceClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int indexSelected, boolean isChecked) {
										selectedItems[0] = isChecked;
									}
								})
						.setCancelable(true)
						.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo) item
												.getMenuInfo();
										long packedPosition = menuInfo.packedPosition;
										int groupPosition = ExpandableListView
												.getPackedPositionGroup(packedPosition);
										int childPosition = ExpandableListView
												.getPackedPositionChild(packedPosition);
										DataProvider.deleteElapsedReminder(
												null,
												context,
												mAdapter.getChild(groupPosition,
														childPosition).getId());
										// notify about changes
										LocalBroadcastManager
												.getInstance(context)
												.sendBroadcast(
														new Intent(
																CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
										if (selectedItems[0]) {
											PreferenceManager
													.getDefaultSharedPreferences(context)
													.edit()
													.putBoolean(
															resources
																	.getString(R.string.preference_key_show_alert_dialog_on_discard_selected_elapsed_reminders_button_press),
															false).commit();
										}
									}
								}).setNegativeButton(R.string.alert_dialog_cancel, null);
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
			} else {
				ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListContextMenuInfo) item
						.getMenuInfo();
				long packedPosition = menuInfo.packedPosition;
				int groupPosition = ExpandableListView
						.getPackedPositionGroup(packedPosition);
				int childPosition = ExpandableListView
						.getPackedPositionChild(packedPosition);
				DataProvider.deleteElapsedReminder(null, context,
						mAdapter.getChild(groupPosition, childPosition).getId());
				// notify about changes
				LocalBroadcastManager.getInstance(context).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
			}
			//
			//
			//
			// Action picked, so close the CAB
			return true;
			// case CommonConstants.MENU_ID_CHECK_ALL:
			// ArrayList<ExpandListGroup> entries = mAdapter.getData();
			// for (int j = 0; j < entries.size(); j++) {
			// ExpandListGroup expandListGroup = entries.get(j);
			// ArrayList<ElapsedReminder> list = expandListGroup.Entities;
			// for (int i = 0, count = list.size(); i < count; i++) {
			// mExpandableListView2.setChildChecked(j, i, true);
			// }
			// }
			// return false;
			// case CommonConstants.MENU_ID_UNCHECK_ALL:
			// entries = mAdapter.getData();
			// for (int j = 0; j < entries.size(); j++) {
			// ExpandListGroup expandListGroup = entries.get(j);
			// ArrayList<ElapsedReminder> list = expandListGroup.Entities;
			// for (int i = 0, count = list.size(); i < count; i++) {
			// mExpandableListView2.setChildChecked(j, i, false);
			// }
			// }
			// return false;
		default:
			return false;
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(mPendingScheduledReminderForTimePickerDialogKey,
				mPendingScheduledReminderForTimePickerDialog);
		// outState.putParcelable(mAdapterStateKey, mAdapter.onSaveInstanceState());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mBroadcastReceiver);
		}
	}

	private class FragmentViewElapsedRemindersAdapter extends BaseExpandableListAdapter {
		Context mContext;
		private ArrayList<ExpandListGroup> mGroups;
		private LayoutInflater mLayoutInflater;
		ExpandableListView2 mExpandableListView;

		public FragmentViewElapsedRemindersAdapter(Context context,
				int textViewResourceId, List<ElapsedReminder> objects,
				ExpandableListView2 expandableListView) {
			mContext = context;
			mLayoutInflater = getActivity().getLayoutInflater();
			mExpandableListView = expandableListView;
			// super(context, textViewResourceId, objects);
			boolean isAscending = Helper.getBooleanPreferenceValue(getActivity(),
					R.string.preference_key_sort_order_ascending, false);
			if (isAscending) {
				Collections
						.sort(objects,
								new AlarmService.ElapsedReminderComparatorByActualAlarmedDateTime());
			} else {
				Collections
						.sort(objects,
								Collections
										.reverseOrder(new AlarmService.ElapsedReminderComparatorByActualAlarmedDateTime()));
			}
			mGroups = new ArrayList<FragmentViewElapsedReminders.ExpandListGroup>();
			ExpandListGroup group = null;
			// ScheduledReminder previousScheduledReminder = null;
			Calendar previousCalendar = null;
			Calendar currentCalendar = null;
			DateFormat dateFormat = null;
			String dateString = null;
			int year, month, day;
			if (objects.size() > 0) {
				// previousScheduledReminder = objects.get(0);
				dateFormat = DateFormat.getDateInstance();
				dateString = dateFormat.format(new Date(objects.get(0)
						.getActualLastAlarmedDateTime()));
				group = new ExpandListGroup();
				group.Entities = new ArrayList<ElapsedReminder>();
				group.setName(dateString);
				previousCalendar = Calendar.getInstance();
				previousCalendar.setTimeInMillis(objects.get(0)
						.getActualLastAlarmedDateTime());
				year = previousCalendar.get(Calendar.YEAR);
				month = previousCalendar.get(Calendar.MONTH);
				day = previousCalendar.get(Calendar.DAY_OF_MONTH);
				previousCalendar.clear();
				previousCalendar.set(year, month, day);
				currentCalendar = Calendar.getInstance();
				mGroups.add(group);
			}
			for (ElapsedReminder currentScheduledReminder : objects) {
				currentCalendar.setTimeInMillis(currentScheduledReminder
						.getActualLastAlarmedDateTime());
				year = currentCalendar.get(Calendar.YEAR);
				month = currentCalendar.get(Calendar.MONTH);
				day = currentCalendar.get(Calendar.DAY_OF_MONTH);
				currentCalendar.clear();
				currentCalendar.set(year, month, day);
				Date currentDate = new Date(currentCalendar.getTimeInMillis());
				if (currentCalendar.getTimeInMillis() != previousCalendar
						.getTimeInMillis()) {
					dateString = dateFormat.format(currentDate);
					group = new ExpandListGroup();
					group.Entities = new ArrayList<ElapsedReminder>();
					group.setName(dateString);
					mGroups.add(group);
				}
				ElapsedReminder expandListChild = currentScheduledReminder;
				group.Entities.add(expandListChild);
				// previousScheduledReminder = currentScheduledReminder;
				previousCalendar.setTimeInMillis(currentCalendar.getTimeInMillis());
			}
		}

		public ArrayList<ExpandListGroup> getData() {
			return mGroups;
		}

		// public void addItem(ElapsedReminder item, ExpandListGroup group) {
		// if (!mGroups.contains(group)) {
		// mGroups.add(group);
		// }
		// int index = mGroups.indexOf(group);
		// ArrayList<ElapsedReminder> ch = mGroups.get(index).getItems();
		// ch.add(item);
		// mGroups.get(index).setItems(ch);
		// }
		@Override
		public ElapsedReminder getChild(int groupPosition, int childPosition) {
			ArrayList<ElapsedReminder> chList = mGroups.get(groupPosition).getItems();
			return chList.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0L | groupPosition << 12 | childPosition;
		}

		@Override
		public long getCombinedChildId(long groupId, long childId) {
			return groupId << 32 | childId << 1 | 1;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			final CheckableLinearLayout checkableLinearLayout;
			if (convertView == null) {
				checkableLinearLayout = (CheckableLinearLayout) mLayoutInflater.inflate(
						R.layout.fragment_view_elapsed_reminders_listitem, parent, false);
			} else {
				checkableLinearLayout = (CheckableLinearLayout) convertView;
			}
			Button buttonResnooze = (Button) checkableLinearLayout
					.findViewById(R.id.fragment_view_elapsed_reminders_listitem_button_resnooze);
			// Button buttonDismiss = (Button) checkableLinearLayout
			// .findViewById(R.id.fragment_view_elapsed_reminders_listitem_button_dismiss);
			// buttonDismiss.setText(R.string.button_dismiss);
			final ElapsedReminder reminder = getChild(groupPosition, childPosition);
			ImageView imageView = (ImageView) checkableLinearLayout
					.findViewById(R.id.fragment_view_elapsed_reminders_listitem_icon);
			if (reminder.getReminder() == null ? reminder.getIsAlarm() : reminder
					.getReminder().getIsAlarm()) {
				// textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm,
				// 0,
				// 0, 0);
				if (reminder.getWasEnabled()) {
					imageView.setImageDrawable(ContextCompat.getDrawable(mContext,
							R.drawable.ic_alarm_on_black_24dp));
				} else {
					imageView.setImageDrawable(ContextCompat.getDrawable(mContext,
							R.drawable.ic_alarm_off_black_24dp));
				}
			} else {
				// textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bell_on,
				// 0, 0, 0);
				if (reminder.getWasEnabled()) {
					imageView.setImageDrawable(ContextCompat.getDrawable(mContext,
							R.drawable.ic_notifications_black_24dp));
				} else {
					imageView.setImageDrawable(ContextCompat.getDrawable(mContext,
							R.drawable.ic_notifications_off_black_24dp));
				}
			}
			// setup delete button listener
			Integer[] positions = new Integer[] {groupPosition, childPosition};
			buttonResnooze.setTag(positions);
			buttonResnooze.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(final View v) {
					final FragmentActivity context = getActivity();
					DataProvider.runInTx(null, context, new Runnable() {
						@Override
						public void run() {
							Integer[] positions = (Integer[]) v.getTag();
							ElapsedReminder elapsedReminder = getChild(positions[0],
									positions[1]);
							elapsedReminder = DataProvider.getElapsedReminder(null,
									context, elapsedReminder.getId());
							Long id;
							Long localReminderId;
							boolean isQuickReminder;
							Long assignedRemindAtDateTime;
							Long nextSnoozeDateTime;
							Long actualLastAlarmedDateTime;
							Integer snoozeCount;
							String text;
							boolean isAlarm;
							String ringtone;
							Long ringtoneFadeInTime;
							Integer playingTime;
							Integer automaticSnoozeDuration;
							Integer automaticSnoozesMaxCount;
							Boolean vibrate = false;
							Boolean led = false;
							ScheduledReminder scheduledReminder1 = elapsedReminder
									.getScheduledReminder2(context);
							Reminder reminder;
							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.SECOND, 0);
							calendar.set(Calendar.MILLISECOND, 0);
							long now = calendar.getTimeInMillis();
							Resources resources = getResources();
							if (scheduledReminder1 != null) {
								id = scheduledReminder1.getId();
								localReminderId = scheduledReminder1.getReminderId();
								// elapsedReminder.getReminderId()
								isQuickReminder = scheduledReminder1.getIsQuickReminder();
								assignedRemindAtDateTime = scheduledReminder1
										.getAssignedRemindAtDateTime();
								if (ScheduledReminder.State.fromInt(
										scheduledReminder1.getStateValue()).equals(
										ScheduledReminder.State.SCHEDULED)) {
									nextSnoozeDateTime = scheduledReminder1
											.getNextSnoozeDateTime();
								} else {
									nextSnoozeDateTime = now
											+ scheduledReminder1
													.getAutomaticSnoozeDuration2(
															context,
															R.string.preference_key_automatic_snooze_duration,
															resources
																	.getInteger(R.integer.automatic_snooze_duration_default_value),
															resources
																	.getInteger(R.integer.automatic_snooze_duration_min_value),
															resources
																	.getInteger(R.integer.automatic_snooze_duration_max_value))
											* 60L * 1000;
								}
								actualLastAlarmedDateTime = scheduledReminder1
										.getActualLastAlarmedDateTime();
								snoozeCount = scheduledReminder1.getSnoozeCount();
								text = scheduledReminder1.getText();
								isAlarm = scheduledReminder1.getIsAlarm();
								ringtone = scheduledReminder1.getRingtone();
								ringtoneFadeInTime = scheduledReminder1
										.getRingtoneFadeInTime();
								playingTime = scheduledReminder1.getPlayingTime();
								automaticSnoozeDuration = scheduledReminder1
										.getAutomaticSnoozeDuration();
								automaticSnoozesMaxCount = scheduledReminder1
										.getAutomaticSnoozesMaxCount();
								vibrate = scheduledReminder1.getVibrate();
								led = scheduledReminder1.getLed();
							} else if ((reminder = elapsedReminder
									.getReminder(((Global) context
											.getApplicationContext()).getDaoSession())) != null) {
								id = null;
								localReminderId = reminder.getId();
								isQuickReminder = false;
								assignedRemindAtDateTime = elapsedReminder
										.getAssignedRemindAtDateTime();
								nextSnoozeDateTime = now
										+ reminder
												.getAutomaticSnoozeDuration2(
														context,
														R.string.preference_key_automatic_snooze_duration,
														resources
																.getInteger(R.integer.automatic_snooze_duration_default_value),
														resources
																.getInteger(R.integer.automatic_snooze_duration_min_value),
														resources
																.getInteger(R.integer.automatic_snooze_duration_max_value))
										* 60L * 1000;
								actualLastAlarmedDateTime = elapsedReminder
										.getActualLastAlarmedDateTime();
								snoozeCount = elapsedReminder.getSnoozeCount() + 1;
								text = reminder.getText();
								isAlarm = reminder.getIsAlarm();
								ringtone = reminder.getRingtone();
								ringtoneFadeInTime = reminder.getRingtoneFadeInTime();
								playingTime = reminder.getPlayingTime();
								automaticSnoozeDuration = reminder
										.getAutomaticSnoozeDuration();
								automaticSnoozesMaxCount = reminder
										.getAutomaticSnoozesMaxCount();
								vibrate = reminder.getVibrate();
								led = reminder.getLed();
							} else {
								id = null;
								localReminderId = null;
								isQuickReminder = true;
								assignedRemindAtDateTime = elapsedReminder
										.getAssignedRemindAtDateTime();
								nextSnoozeDateTime = now
										+ Helper.getIntegerFromStringPreferenceValue(
												context,
												R.string.preference_key_automatic_snooze_duration,
												null,
												resources
														.getInteger(R.integer.automatic_snooze_duration_default_value),
												resources
														.getInteger(R.integer.automatic_snooze_duration_min_value),
												resources
														.getInteger(R.integer.automatic_snooze_duration_max_value))
										* 60L * 1000;
								actualLastAlarmedDateTime = elapsedReminder
										.getActualLastAlarmedDateTime();
								snoozeCount = elapsedReminder.getSnoozeCount() + 1;
								text = elapsedReminder.getText();
								isAlarm = elapsedReminder.getIsAlarm();
								ringtone = Helper
										.getStringPreferenceValue(
												context,
												R.string.preference_key_alarm_ringtone,
												RingtoneManager
														.getDefaultUri(
																isAlarm ? RingtoneManager.TYPE_ALARM
																		: RingtoneManager.TYPE_NOTIFICATION)
														.toString());
								ringtoneFadeInTime = Helper
										.getLongFromStringPreferenceValue(
												context,
												R.string.preference_key_ringtone_fade_in_time,
												resources
														.getInteger(R.integer.ringtone_fade_in_time_default_value),
												(long) resources
														.getInteger(R.integer.ringtone_fade_in_time_min_value),
												(long) resources
														.getInteger(R.integer.ringtone_fade_in_time_max_value));
								playingTime = Helper
										.getIntegerFromStringPreferenceValue(
												context,
												R.string.preference_key_reminders_popup_window_displaying_duration,
												null,
												resources
														.getInteger(R.integer.reminders_popup_window_displaying_duration_default_value),
												resources
														.getInteger(R.integer.reminders_popup_window_displaying_duration_min_value),
												resources
														.getInteger(R.integer.reminders_popup_window_displaying_duration_max_value));
								automaticSnoozeDuration = Helper
										.getIntegerFromStringPreferenceValue(
												context,
												R.string.preference_key_automatic_snooze_duration,
												null,
												resources
														.getInteger(R.integer.automatic_snooze_duration_default_value),
												resources
														.getInteger(R.integer.automatic_snooze_duration_min_value),
												resources
														.getInteger(R.integer.automatic_snooze_duration_max_value));
								automaticSnoozesMaxCount = Helper
										.getIntegerFromStringPreferenceValue(
												context,
												R.string.preference_key_automatic_snoozes_max_count,
												null,
												resources
														.getInteger(R.integer.automatic_snoozes_max_count_default_value),
												resources
														.getInteger(R.integer.automatic_snoozes_max_count_min_value),
												resources
														.getInteger(R.integer.automatic_snoozes_max_count_max_value));
								vibrate = Helper
										.getBooleanPreferenceValue(
												context,
												R.string.preference_key_vibrate_with_alarm_ringtone,
												resources
														.getBoolean(R.bool.vibrate_with_alarm_ringtone_default_value));
								led = Helper
										.getBooleanPreferenceValue(
												context,
												R.string.preference_key_vibrate_with_alarm_ringtone,
												resources
														.getBoolean(R.bool.vibrate_with_alarm_ringtone_default_value));
							}
							mPendingScheduledReminderForTimePickerDialog = new ScheduledReminder(
									id, localReminderId, elapsedReminder.getId(),
									isQuickReminder, assignedRemindAtDateTime,
									nextSnoozeDateTime, actualLastAlarmedDateTime,
									snoozeCount, ScheduledReminder.State.SCHEDULED
											.getValue(), text, isAlarm, true, ringtone,
									ringtoneFadeInTime, playingTime,
									automaticSnoozeDuration, automaticSnoozesMaxCount,
									vibrate, null, led, null, null);
							calendar.setTimeInMillis(mPendingScheduledReminderForTimePickerDialog
									.getNextSnoozeDateTime());
							int hours = calendar.get(Calendar.HOUR_OF_DAY);
							int minutes = calendar.get(Calendar.MINUTE);
							ArrayList<TimeAttribute> timeAttributes = new ArrayList<TimeAttribute>();
							boolean is24HourFormat = Helper.is24HourFormat(context);
							timeAttributes
									.add(new TimeAttribute(
											hours,
											minutes,
											R.string.radiobutton_text_reminder_snooze_mode_absolute_time,
											1, is24HourFormat, true));
							hours = 0;
							minutes = 0;
							long timeSpan = calendar.getTimeInMillis() - now;
							if (timeSpan > 0) {
								hours = (int) (timeSpan / 1000 / 60 / 60);
								if (hours > 23) {
									hours = 23;
								}
								minutes = (int) (timeSpan / 1000 / 60 % 60);
							}
							timeAttributes
									.add(new TimeAttribute(
											hours,
											minutes,
											R.string.radiobutton_text_reminder_snooze_mode_timespan,
											0, true, false));
							Bundle bundle = new Bundle();
							bundle.putInt(
									"callerId",
									R.id.fragment_view_elapsed_reminders_listitem_button_resnooze);
							TimePickerDialogMultiple tpd = TimePickerDialogMultiple
									.newInstance(FragmentViewElapsedReminders.this,
											bundle,
											R.string.time_picker_title_for_snooze_mode,
											timeAttributes, 0);
							tpd.show(getFragmentManager(), mTimePickerDialogKey);
						}
					});
				}
			});
			TableLayout tableLayout = (TableLayout) checkableLinearLayout
					.findViewById(R.id.fragment_view_elapsed_reminders_listitem_tablelayout);
			tableLayout.setTag(positions);
			setupText(mLayoutInflater, reminder, tableLayout);
			return checkableLinearLayout;
		}

		private void setupText(LayoutInflater inflater,
				final ElapsedReminder elapsedReminder, TableLayout tableLayout) {
			tableLayout.removeAllViews();
			TableRow tableRow;
			TextView textViewHeader;
			TextView textViewValue;
			String text;
			// setup reminder text
			tableRow = (TableRow) inflater.inflate(R.layout.tablerow_single_textview,
					tableLayout, false);
			textViewValue = (TextView) tableRow
					.findViewById(R.id.tablerow_single_textview_textview);
			text = elapsedReminder.getReminder() == null ? elapsedReminder.getText()
					: elapsedReminder.getReminder().getText();
			textViewValue.setText(text);
			FragmentActivity context = getActivity();
			Reminder reminder = elapsedReminder.getReminder(((Global) context
					.getApplicationContext()).getDaoSession());
			Task task = null;
			if (reminder != null) {
				task = DataProvider.getTask(null, context, reminder.getTaskId(), false);
			}
			if (task != null) {
				textViewValue.setTypeface(null, Typeface.NORMAL);
			} else {
				textViewValue.setTypeface(null, Typeface.BOLD_ITALIC);
			}
			//
			if (task != null) {
				int backgroundColor = task.getColor2(context);
				int textColor;
				if (Helper.getContrastYIQ(backgroundColor)) {
					textColor = ContextCompat.getColor(context,
							R.color.task_view_text_synchronized_dark);
				} else {
					textColor = ContextCompat.getColor(context,
							R.color.task_view_text_synchronized_light);
				}
				textViewValue.setBackgroundColor(backgroundColor);
				textViewValue.setTextColor(textColor);
			} else {
				textViewValue.setBackgroundDrawable(mDefaultBackground);
				textViewValue.setTextColor(mDefaultTextColorStateList);
			}
			tableLayout.addView(tableRow);
			// setup sheduled_for
			tableRow = (TableRow) inflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			Resources resources = getResources();
			textViewHeader.setText(resources
					.getString(R.string.fragment_view_reminders_reminder_scheduled_time));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			text = elapsedReminder.getReminder() == null ? elapsedReminder.getText()
					: elapsedReminder.getReminder().getText();
			DateFormat mDateFormat = DateFormat.getDateTimeInstance();
			text = mDateFormat.format(new Date(elapsedReminder
					.getAssignedRemindAtDateTime()));
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
			// setup last snooze
			tableRow = (TableRow) inflater.inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			if (elapsedReminder.getSnoozeCount() == 0) {
				text = resources.getString(R.string.fragment_view_reminders_alarmed_at)
						+ ":";
			} else {
				text = resources
						.getString(R.string.fragment_view_reminders_last_snooze_at)
						+ " (" + elapsedReminder.getSnoozeCount() + "):";
			}
			textViewHeader.setText(text);
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			text = mDateFormat.format(new Date(elapsedReminder
					.getActualLastAlarmedDateTime()));
			textViewValue.setText(text);
			tableLayout.addView(tableRow);
			// setup will alarm again
			ScheduledReminder scheduledReminder = elapsedReminder
					.getScheduledReminder2(context);
			if (scheduledReminder != null
					&& ScheduledReminder.State.SCHEDULED.equals(ScheduledReminder.State
							.fromInt(scheduledReminder.getStateValue()))) {
				tableRow = (TableRow) inflater.inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
				textViewHeader = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				if (scheduledReminder.getEnabled()) {
					if (elapsedReminder.getIsAlarm()) {
						text = String
								.format(resources
										.getString(R.string.fragment_view_reminders_will_alarm_again_at),
										elapsedReminder.getSnoozeCount() + 1)
								+ ":";
					} else {
						text = String
								.format(resources
										.getString(R.string.fragment_view_reminders_will_notificate_again_at),
										elapsedReminder.getSnoozeCount() + 1)
								+ ":";
					}
				} else {
					if (elapsedReminder.getIsAlarm()) {
						text = String
								.format(resources
										.getString(R.string.fragment_view_reminders_will_not_alarm_again_at),
										elapsedReminder.getSnoozeCount() + 1)
								+ ":";
					} else {
						text = String
								.format(resources
										.getString(R.string.fragment_view_reminders_will_not_notificate_again_at),
										elapsedReminder.getSnoozeCount() + 1)
								+ ":";
					}
				}
				textViewHeader.setText(text);
				textViewValue = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				text = mDateFormat.format(new Date(scheduledReminder
						.getNextSnoozeDateTime()));
				textViewValue.setText(text);
				tableLayout.addView(tableRow);
			}
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			ArrayList<ElapsedReminder> chList = mGroups.get(groupPosition).getItems();
			return chList.size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mGroups.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return mGroups.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getCombinedGroupId(long groupId) {
			return (groupId & 0x7FFFFFFF) << 32;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isLastChild, View view,
				ViewGroup parent) {
			if (view == null) {
				view = mLayoutInflater.inflate(R.layout.expandlist_group_item, parent,
						false);
			}
			TextView tv = (TextView) view.findViewById(R.id.tvGroup);
			LinearLayout linearLayoutCheckBox1 = (LinearLayout) view
					.findViewById(R.id.linearLayoutCheckBox1);
			if (mExpandableListView.isInActionMode()) {
				linearLayoutCheckBox1.setVisibility(View.VISIBLE);
				Map<Integer, ArrayList<Integer>> checkedItemPositions = mExpandableListView
						.getCheckedItemPositions2();
				TextView textViewCheckBox1 = (TextView) view
						.findViewById(R.id.textViewCheckBox1);
				CheckBox checkBox1 = (CheckBox) view.findViewById(R.id.checkBox1);
				ArrayList<Integer> list = checkedItemPositions.get(groupPosition);
				int totalChildrenCount = getChildrenCount(groupPosition);
				if (list != null) {
					int checkedChildrenCount = list.size();
					textViewCheckBox1.setText(checkedChildrenCount + " of "
							+ totalChildrenCount);
					checkBox1.setChecked(true);
				} else {
					int checkedChildrenCount = 0;
					textViewCheckBox1.setText(checkedChildrenCount + " of "
							+ totalChildrenCount);
					checkBox1.setChecked(false);
				}
				checkBox1.setTag(groupPosition);
				checkBox1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox checkBox = (CheckBox) v;
						int group = (Integer) checkBox.getTag();
						boolean newCheckedState = checkBox.isChecked();
						ArrayList<Integer> list = mExpandableListView
								.getCheckedItemPositions2().get(group);
						int totalChildrenCount = getChildrenCount(group);
						int checkedChildrenCount = list == null ? 0 : list.size();
						if (totalChildrenCount == 0) {
							newCheckedState = false;
						} else if (checkedChildrenCount == 0) {
							newCheckedState = true;
						} else if (checkedChildrenCount == totalChildrenCount) {
							newCheckedState = false;
						} else {
							newCheckedState = true;
						}
						checkBox.setChecked(newCheckedState);
						for (int i = 0, count = mGroups.get(group).Entities.size(); i < count; i++) {
							mExpandableListView
									.setChildChecked(group, i, newCheckedState);
						}
					}
				});
			} else {
				linearLayoutCheckBox1.setVisibility(View.GONE);
			}
			// ExpandListGroup<String, String> group = getGroup(groupPosition);
			ExpandListGroup group = (ExpandListGroup) getGroup(groupPosition);
			tv.setText(group.getName());
			return view;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int arg0, int arg1) {
			return true;
		}
	}

	public class ExpandListGroup {
		private String Name;
		private ArrayList<ElapsedReminder> Entities;

		public String getName() {
			return Name;
		}

		public void setName(String name) {
			Name = name;
		}

		public ArrayList<ElapsedReminder> getItems() {
			return Entities;
		}

		public void setItems(ArrayList<ElapsedReminder> Entities) {
			this.Entities = Entities;
		}
	}

	private void fillListView(final Parcelable state) {
		final FragmentActivity context = getActivity();
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				mElapsedReminders = DataProvider.getElapsedReminders(null,
						context.getApplicationContext());
				for (int i = 0; i < mElapsedReminders.size(); i++) {
					ElapsedReminder scheduledReminder = mElapsedReminders.get(i);
					scheduledReminder.setShowInNotifications(false);
					DataProvider.insertOrReplaceElapsedReminder(null,
							context.getApplicationContext(), scheduledReminder);
				}
				// for (int i = 0; i < reminderList.size(); i++) {
				// ScheduledReminder scheduledReminder = reminderList.get(i);
				// scheduledReminder.setShowInNotifications(false);
				// DataProvider.insertOrReplaceScheduledReminder(getActivity()
				// .getApplicationContext(), scheduledReminder);
				// }
				mAdapter = new FragmentViewElapsedRemindersAdapter(context,
						android.R.layout.simple_list_item_1, mElapsedReminders,
						mExpandableListView2);
				mExpandableListView2.setAdapter(mAdapter);
				// To expand all groups
				int count = mAdapter.getGroupCount();
				for (int position = 0; position < count; position++) {
					mExpandableListView2.expandGroup(position);
				}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					mExpandableListView2
							.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
					mExpandableListView2
							.setMultiChoiceModeListener(new MultiChoiceModeListener() {
								@Override
								public boolean onActionItemClicked(final ActionMode mode,
										MenuItem item) {
									final Resources resources = getResources();
									switch (item.getItemId()) {
									case CommonConstants.MENU_ID_DISCARD:
										boolean showAlertDialog = Helper
												.getBooleanPreferenceValue(
														context,
														resources
																.getString(R.string.preference_key_show_alert_dialog_on_discard_selected_elapsed_reminders_button_press),
														true);
										if (showAlertDialog) {
											AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
													context);
											String[] items = new String[] {resources
													.getString(R.string.action_dont_bother_anymore)};
											final boolean[] selectedItems = new boolean[] {false};
											alertDialogBuilder
													.setTitle(
															resources
																	.getString(R.string.action_discard_checked_elapsed_reminders_confirmation))
													.setMultiChoiceItems(
															items,
															selectedItems,
															new DialogInterface.OnMultiChoiceClickListener() {
																@Override
																public void onClick(
																		DialogInterface dialog,
																		int indexSelected,
																		boolean isChecked) {
																	selectedItems[0] = isChecked;
																}
															})
													.setCancelable(true)
													.setPositiveButton(
															R.string.alert_dialog_ok,
															new DialogInterface.OnClickListener() {
																@Override
																public void onClick(
																		DialogInterface dialog,
																		int id) {
																	discard();
																	mode.finish();
																	if (selectedItems[0]) {
																		PreferenceManager
																				.getDefaultSharedPreferences(
																						context)
																				.edit()
																				.putBoolean(
																						resources
																								.getString(R.string.preference_key_show_alert_dialog_on_discard_selected_elapsed_reminders_button_press),
																						false)
																				.commit();
																	}
																}
															})
													.setNegativeButton(
															R.string.alert_dialog_cancel,
															null);
											// create alert dialog
											AlertDialog alertDialog = alertDialogBuilder
													.create();
											// show it
											alertDialog.show();
										} else {
											discard();
											mode.finish();
										}
										//
										//
										//
										// Action picked, so close the CAB
										return true;
									case CommonConstants.MENU_ID_CHECK_ALL:
										ArrayList<ExpandListGroup> entries = mAdapter
												.getData();
										for (int j = 0; j < entries.size(); j++) {
											ExpandListGroup expandListGroup = entries
													.get(j);
											ArrayList<ElapsedReminder> list = expandListGroup.Entities;
											for (int i = 0, count = list.size(); i < count; i++) {
												mExpandableListView2.setChildChecked(j,
														i, true);
											}
										}
										return false;
									case CommonConstants.MENU_ID_UNCHECK_ALL:
										entries = mAdapter.getData();
										for (int j = 0; j < entries.size(); j++) {
											ExpandListGroup expandListGroup = entries
													.get(j);
											ArrayList<ElapsedReminder> list = expandListGroup.Entities;
											for (int i = 0, count = list.size(); i < count; i++) {
												mExpandableListView2.setChildChecked(j,
														i, false);
											}
										}
										return false;
									default:
										return false;
									}
								}

								@Override
								public boolean onCreateActionMode(ActionMode mode,
										Menu menu) {
									// Inflate the menu for the CAB
									// mActionMode = mode;
									// MenuInflater inflater = mode.getMenuInflater();
									// inflater.inflate(
									// R.menu.fragment_task_viewing_reminders_list_item,
									// menu);
									mode.setTitle("Select Items");
									MenuItem menuItem;
									Resources resources = getResources();
									menuItem = menu.add(Menu.NONE,
											CommonConstants.MENU_ID_DISCARD,
											Menu.FIRST + 200,
											resources.getString(R.string.action_discard));
									menuItem.setIcon(R.drawable.ic_delete_black_24dp);
									MenuItemCompat.setShowAsAction(menuItem,
											MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
									menuItem = menu.add(Menu.NONE,
											CommonConstants.MENU_ID_CHECK_ALL,
											Menu.FIRST + 300, resources
													.getString(R.string.action_check_all));
									menuItem.setIcon(R.drawable.ic_check_box_black_24dp);
									MenuItemCompat.setShowAsAction(menuItem,
											MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
									menuItem = menu.add(
											Menu.NONE,
											CommonConstants.MENU_ID_UNCHECK_ALL,
											Menu.FIRST + 400,
											resources
													.getString(R.string.action_uncheck_all));
									menuItem.setIcon(R.drawable.ic_check_box_outline_blank_black_24dp);
									MenuItemCompat.setShowAsAction(menuItem,
											MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
									return true;
								}

								@Override
								public void onDestroyActionMode(ActionMode mode) {
									// Here you can make any necessary updates to the
									// activity
									// when
									// the CAB is removed. By default, selected items are
									// deselected/unchecked.
								}

								@Override
								public boolean onPrepareActionMode(ActionMode mode,
										Menu menu) {
									// Here you can perform updates to the CAB due to
									// an invalidate() request
									return false;
								}

								@Override
								public void onItemCheckedStateChanged(ActionMode mode,
										int position, long id, boolean checked) {
									// TODO Auto-generated method stub
								}
							});
				} else {
					registerForContextMenu(mExpandableListView2);
				}
			}
		});
	}

	private void discard() {
		Iterator<Map.Entry<Integer, ArrayList<Integer>>> entries = mExpandableListView2
				.getCheckedItemPositions2().entrySet().iterator();
		FragmentActivity context = getActivity();
		while (entries.hasNext()) {
			Map.Entry<Integer, ArrayList<Integer>> entry = entries.next();
			int groupPosition = entry.getKey();
			ArrayList<Integer> list = entry.getValue();
			for (int i = 0, count = list.size(); i < count; i++) {
				DataProvider.deleteElapsedReminder(null, context,
						mAdapter.getChild(groupPosition, list.get(i)).getId());
			}
		}
		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, int callerId, int hours, int minutes,
			boolean isTimeSpan) {
		final FragmentActivity activity = getActivity();
		if (mPendingScheduledReminderForTimePickerDialog != null) {
			Resources resources = getResources();
			mPendingScheduledReminderForTimePickerDialog.setEnabled(true);
			String text;
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			if (isTimeSpan) {
				calendar.add(Calendar.HOUR_OF_DAY, hours);
				calendar.add(Calendar.MINUTE, minutes);
				String textTime = Helper.getTextForTimeInterval(activity, 0, hours,
						minutes, 0);
				text = String
						.format(resources
								.getString(R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_timespan),
								textTime);
			} else {
				Calendar calendar2 = (Calendar) calendar.clone();
				calendar2.set(Calendar.HOUR_OF_DAY, hours);
				calendar2.set(Calendar.MINUTE, minutes);
				calendar2.set(Calendar.SECOND, 0);
				int textId;
				if (calendar2.getTimeInMillis() < calendar.getTimeInMillis()) {
					calendar2.add(Calendar.DAY_OF_YEAR, 1);
					textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_tomorrow;
				} else {
					textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_today;
				}
				calendar = calendar2;
				DateFormat timeFormat = DateFormat.getTimeInstance();
				text = String.format(resources.getString(textId),
						timeFormat.format(new Date(calendar2.getTimeInMillis())));
			}
			Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			mPendingScheduledReminderForTimePickerDialog.setNextSnoozeDateTime(calendar
					.getTimeInMillis());
			// mPendingScheduledReminderForTimePickerDialog.setActualLastAlarmedDateTime(null);
			mPendingScheduledReminderForTimePickerDialog
					.setStateValue(ScheduledReminder.State.SCHEDULED.getValue());
			DataProvider.runInTx(null, activity, new Runnable() {
				@Override
				public void run() {
					DataProvider.insertOrReplaceScheduledReminder(null, activity,
							mPendingScheduledReminderForTimePickerDialog);
					AlarmService.setAlarmForReminder(activity,
							mPendingScheduledReminderForTimePickerDialog.getId(),
							mPendingScheduledReminderForTimePickerDialog
									.getNextSnoozeDateTime(), this);
				}
			});
			mPendingScheduledReminderForTimePickerDialog = null;
			LocalBroadcastManager.getInstance(activity).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		} else {
			Toast.makeText(
					activity,
					getString(R.string.fragment_view_scheduled_reminders_toast_the_reminder_does_not_exist_anymore),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, Bundle bundle,
			ArrayList<TimeAttribute> timeAttributes, int ordinalNumber) {
		final FragmentActivity activity = getActivity();
		if (mPendingScheduledReminderForTimePickerDialog != null) {
			Resources resources = getResources();
			mPendingScheduledReminderForTimePickerDialog.setEnabled(true);
			boolean isTimeSpan = ordinalNumber == 0;
			TimeAttribute timeAttribute = timeAttributes.get(ordinalNumber);
			int hours = timeAttribute.getHours();
			int minutes = timeAttribute.getMinutes();
			mPendingScheduledReminderForTimePickerDialog.setEnabled(true);
			String text;
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			if (isTimeSpan) {
				calendar.add(Calendar.HOUR_OF_DAY, hours);
				calendar.add(Calendar.MINUTE, minutes);
				String textTime = Helper.getTextForTimeInterval(activity, 0, hours,
						minutes, 0);
				text = String
						.format(resources
								.getString(R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_timespan),
								textTime);
			} else {
				Calendar calendar2 = (Calendar) calendar.clone();
				calendar2.set(Calendar.HOUR_OF_DAY, hours);
				calendar2.set(Calendar.MINUTE, minutes);
				calendar2.set(Calendar.SECOND, 0);
				int textId;
				if (calendar2.getTimeInMillis() < calendar.getTimeInMillis()) {
					calendar2.add(Calendar.DAY_OF_YEAR, 1);
					textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_tomorrow;
				} else {
					textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_today;
				}
				calendar = calendar2;
				DateFormat timeFormat = DateFormat.getTimeInstance();
				text = String.format(resources.getString(textId),
						timeFormat.format(new Date(calendar2.getTimeInMillis())));
			}
			Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
			mPendingScheduledReminderForTimePickerDialog.setNextSnoozeDateTime(calendar
					.getTimeInMillis());
			// mPendingScheduledReminderForTimePickerDialog.setActualLastAlarmedDateTime(null);
			mPendingScheduledReminderForTimePickerDialog
					.setStateValue(ScheduledReminder.State.SCHEDULED.getValue());
			DataProvider.runInTx(null, activity, new Runnable() {
				@Override
				public void run() {
					DataProvider.insertOrReplaceScheduledReminder(null, activity,
							mPendingScheduledReminderForTimePickerDialog);
					AlarmService.setAlarmForReminder(activity,
							mPendingScheduledReminderForTimePickerDialog.getId(),
							mPendingScheduledReminderForTimePickerDialog
									.getNextSnoozeDateTime(), this);
				}
			});
			mPendingScheduledReminderForTimePickerDialog = null;
			LocalBroadcastManager.getInstance(activity).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		} else {
			Toast.makeText(
					activity,
					getString(R.string.fragment_view_scheduled_reminders_toast_the_reminder_does_not_exist_anymore),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean isTimeConsistent(ArrayList<TimeAttribute> timeAttributes, Bundle bundle) {
		return true;
	}
}
