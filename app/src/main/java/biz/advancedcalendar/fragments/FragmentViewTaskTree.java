package biz.advancedcalendar.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityLogin;
import biz.advancedcalendar.activities.ActivityMain;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.sync.SyncService;
import biz.advancedcalendar.utils.GetUserInfoTask;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.CheckableFrameLayout;
import biz.advancedcalendar.views.CheckableLinearLayout;
import java.util.ArrayList;
import java.util.List;

public class FragmentViewTaskTree extends Fragment {
	private ListView mTreeView;
	private FragmentViewTaskTreeAdapter mAdapter = null;
	private int mSelectMode;
	private BroadcastReceiver receiver;
	private boolean mIsInActionMode;

	private class FragmentViewTaskTreeAdapter extends BaseAdapter implements
			OnCheckedChangeListener, OnClickListener {
		Context mContext;
		List<Node<Task>> mForest;
		private LayoutInflater mLayoutInflater;
		List<Node<Task>> mVisibleList;
		private static final int INDENT_WIDTH_DIP = 24;
		private int mIndentWidth;
		private boolean mMarkSyncNeeded;
		private InformationUnitMatrix informationUnitMatrix;
		private ArrayList<String> taskInformationStrings;
		private String firstRow;
		private String secondRow;

		public FragmentViewTaskTreeAdapter(Context context, List<Node<Task>> forest) {
			mContext = context;
			mForest = forest;
			mLayoutInflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mVisibleList = new ArrayList<Node<Task>>();
			createVisibleListRecursively(mForest);
			mIndentWidth = Math.max(1, (int) (getResources().getDisplayMetrics().density
					* FragmentViewTaskTreeAdapter.INDENT_WIDTH_DIP + 0.5));
		}

		public void setNodes(List<Node<Task>> forest) {
			mForest = forest;
			mVisibleList.clear();
			createVisibleListRecursively(mForest);
			notifyDataSetChanged();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.tree_list_item_wrapper2,
						parent, false);
			}
			CheckableLinearLayout checkableLinearLayout = (CheckableLinearLayout) convertView;
			//
			final LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(
					calculateIndentation(position),
					android.view.ViewGroup.LayoutParams.MATCH_PARENT);
			final LinearLayout indicatorLayout = (LinearLayout) checkableLinearLayout
					.findViewById(R.id.treeview_list_item_image_layout);
			indicatorLayout.setLayoutParams(indicatorLayoutParams);
			//
			CheckableFrameLayout checkableFrameLayout = (CheckableFrameLayout) checkableLinearLayout
					.findViewById(R.id.treeview_list_item_frame);
			if (checkableFrameLayout.getChildCount() == 0) {
				mLayoutInflater.inflate(R.layout.fragment_view_task_tree_item,
						checkableFrameLayout);
			}
			LinearLayout linearLayout = (LinearLayout) checkableFrameLayout.getChildAt(0);
			View colorBox = linearLayout
					.findViewById(R.id.task_treeview_list_item_color_box);
			TextView tv = (TextView) linearLayout
					.findViewById(R.id.task_treeview_list_item_textview_description);
			Node<Task> node = getNodeAtVisibleListPosition(position);
			Task task = node.mTag;
			CalendarViewTaskOccurrence calendarViewTaskOccurrence = new CalendarViewTaskOccurrence(
					task);
			taskInformationStrings = informationUnitMatrix
					.createInformationComposerStrings(mContext,
							calendarViewTaskOccurrence);
			if (taskInformationStrings.size() > 0) {
				firstRow = taskInformationStrings.get(0);
			} else {
				firstRow = "";
			}
			String text = firstRow;
			if (mMarkSyncNeeded) {
				switch (SyncStatus.fromInt(task.getSyncStatusValue())) {
				case SYNCHRONIZED:
				default:
					break;
				case SYNC_UP_REQUIRED:
					text = "\u2191" + " " + firstRow;
					break;
				case SYNC_DOWN_REQUIRED:
					text = "\u2193" + " " + firstRow;
					break;
				}
			}
			tv.setText(text);
			tv.setTag(task.getId());
			// tv.setOnClickListener(this);
			int backgroundColor = task.getColor2(mContext);
			colorBox.setBackgroundColor(backgroundColor);
			tv.setBackgroundColor(backgroundColor);
			//
			int textColor;
			// Helper. getContrast50(backgroundColor);
			if (Helper.getContrastYIQ(backgroundColor)) {
				textColor = ContextCompat.getColor(mContext,
						R.color.task_view_text_synchronized_dark);
			} else {
				textColor = ContextCompat.getColor(mContext,
						R.color.task_view_text_synchronized_light);
			}
			tv.setTextColor(textColor);
			//
			ImageView imageView = (ImageView) linearLayout
					.findViewById(R.id.task_treeview_list_item_imageview_completed);
			if (task.getIsCompleted()) {
				imageView.setVisibility(View.VISIBLE);
			} else {
				imageView.setVisibility(View.GONE);
			}
			//
			CheckBox checkBox = (CheckBox) linearLayout
					.findViewById(R.id.task_treeview_list_item_checkbox);
			if (mIsInActionMode) {
				// starting state is unchecked
				checkBox.setChecked(false);
				checkBox.setVisibility(View.VISIBLE);
			} else {
				checkBox.setVisibility(View.GONE);
			}
			// checkBox.setTag(task);
			// checkBox.setOnCheckedChangeListener(null);
			// if (task.getIsCompleted()) {
			// checkBox.setChecked(true);
			// } else {
			// checkBox.setChecked(false);
			// }
			// checkBox.setOnCheckedChangeListener(this);
			//
			return checkableLinearLayout;
		}

		private int calculateIndentation(int position) {
			Node<Task> node = getNodeAtVisibleListPosition(position);
			return mIndentWidth * (node.getLevel() + 0);
		}

		private Node<Task> getNodeAtVisibleListPosition(int position) {
			return mVisibleList.get(position);
		}

		private void createVisibleListRecursively(List<Node<Task>> nodes) {
			for (Node<Task> node : nodes) {
				mVisibleList.add(node);
				if (node.mIsExpanded) {
					createVisibleListRecursively(node.mChildren);
				}
			}
		}

		@Override
		public int getCount() {
			return mVisibleList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return mVisibleList.get(position).mId;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.task_treeview_list_item_checkbox:
				Task task = (Task) buttonView.getTag();
				if (isChecked) {
					setCompleted(new long[] {task.getId()});
				} else {
					setActive(new long[] {task.getId()});
				}
				break;
			default:
				break;
			}
		}

		@Override
		public void onClick(View v) {
			Long id = (Long) v.getTag();
			ActivityMain.launchTaskViewerOrEditor(getActivity(), id, 0);
		}

		public void setParameters(SyncPolicy syncPolicy,
				MarkSyncNeededPolicy markSyncNeededPolicy,
				InformationUnitMatrix informationUnitMatrix) {
			boolean markSyncNeeded;
			switch (markSyncNeededPolicy) {
			case ALWAYS:
				markSyncNeeded = true;
				break;
			case IF_SYNC_IS_SWITCHED_ON:
				markSyncNeeded = syncPolicy.equals(SyncPolicy.DO_SYNC);
				break;
			case NEVER:
			default:
				markSyncNeeded = false;
				break;
			}
			mMarkSyncNeeded = markSyncNeeded;
			this.informationUnitMatrix = informationUnitMatrix;
			notifyDataSetChanged();
		}

		public void reloadTasks() {
			Bundle b = getArguments();
			boolean getActive = b.getBoolean(
					CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
			if (getActive) {
				mAdapter.setNodes(DataProvider.getNodesForestOfActiveTasks(null,
						getActivity(), true));
			} else {
				mAdapter.setNodes(DataProvider.getNodesForestOfCompletedTasks(null,
						getActivity(), true));
			}
		}

		public void setInformationUnitMatrix(InformationUnitMatrix informationUnitMatrix) {
			this.informationUnitMatrix = informationUnitMatrix;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(CommonConstants.ACTION_CALENDARS_CHANGED)
						|| intent.getAction().equals(
								CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED)
						|| intent.getAction().equals(
								CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
					if (isAdded()) {
						if (getActivity() == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						mAdapter.reloadTasks();
					}
				} else if (intent
						.getAction()
						.equals(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE_CHANGED)) {
					InformationUnitMatrix informationUnitMatrix = Helper
							.createInformationUnitMatrix(
									context,
									R.string.preference_key_information_unit_matrix_for_task_tree,
									R.string.information_unit_matrix_for_task_tree_default_value);
					mAdapter.setInformationUnitMatrix(informationUnitMatrix);
					if (isAdded()) {
						if (getActivity() == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						mAdapter.reloadTasks();
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTreeView = (ListView) inflater.inflate(R.layout.tree_view_list2, container,
				false);
		mTreeView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position,
					long id) {
				ActivityMain.launchTaskViewerOrEditor(getActivity(), id, 0);
			}
		});
		return mTreeView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommonConstants.ACTION_SELECTED_CALENDARS_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_CALENDARS_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
		intentFilter
				.addAction(CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE_CHANGED);
		final Context context = getActivity();
		LocalBroadcastManager.getInstance(context.getApplicationContext())
				.registerReceiver(receiver, intentFilter);
		Bundle b = getArguments();
		if (b != null) {
			mSelectMode = b.getInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
					AbsListView.CHOICE_MODE_NONE);
		}
		createIfNeededAndFillAdapter();
		mTreeView.setAdapter(mAdapter);
		SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_sync_policy,
						R.array.sync_policy_values_array,
						R.integer.sync_policy_default_value));
		MarkSyncNeededPolicy markSyncNeededPolicy = MarkSyncNeededPolicy
				.fromInt((byte) Helper.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_mark_sync_needed,
						R.array.mark_sync_needed_values_array,
						R.integer.mark_sync_needed_default_value));
		syncPolicy = SyncPolicy.DO_NOT_SYNC;
		markSyncNeededPolicy = MarkSyncNeededPolicy.NEVER;
		InformationUnitMatrix informationUnitMatrix = Helper.createInformationUnitMatrix(
				context, R.string.preference_key_information_unit_matrix_for_task_tree,
				R.string.information_unit_matrix_for_task_tree_default_value);
		mAdapter.setParameters(syncPolicy, markSyncNeededPolicy, informationUnitMatrix);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			switch (mSelectMode) {
			case AbsListView.CHOICE_MODE_NONE:
				mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
				break;
			case AbsListView.CHOICE_MODE_SINGLE:
				mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
				break;
			case AbsListView.CHOICE_MODE_MULTIPLE:
				mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
				break;
			case AbsListView.CHOICE_MODE_MULTIPLE_MODAL:
				mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
				mTreeView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
					@Override
					public void onItemCheckedStateChanged(ActionMode mode, int position,
							long id, boolean checked) {
						// Here you can do something when items are
						// selected/de-selected,
						// such as update the title in the CAB
					}

					@Override
					public boolean onActionItemClicked(final ActionMode mode,
							MenuItem item) {
						// long dateTime;
						// Respond to clicks on the actions in the CAB
						switch (item.getItemId()) {
						case R.id.fragment_view_task_tree_cab_menu_delete:
							AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
									context);
							// final DBHelper dbHelper = new DBHelper(this);
							// final List<MeasurementDevice> totalDeviceList =
							// dbHelper.getAllDevices();
							String[] items = new String[] {getResources().getString(
									R.string.action_delete_task_subtree_confirmation)};
							final boolean[] selectedItems = new boolean[] {false};
							alertDialogBuilder
									.setTitle(
											getResources()
													.getString(
															R.string.action_delete_task_confirmation))
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
									// .setMessage(
									// getResources().getString(
									// R.string.action_delete_task_confirmation))
									.setCancelable(true)
									// Set the action buttons
									.setPositiveButton(R.string.alert_dialog_ok,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog, int id) {
													// User clicked OK button
													DataProvider
															.markTasksAsDeleted(
																	null,
																	context.getApplicationContext(),
																	mTreeView
																			.getCheckedItemIds(),
																	selectedItems[0]);
													// notify about changes
													LocalBroadcastManager
															.getInstance(
																	context.getApplicationContext())
															.sendBroadcast(
																	new Intent(
																			CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
													LocalBroadcastManager
															.getInstance(context)
															.sendBroadcast(
																	new Intent(
																			CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
													if (true) {
														mode.finish();
														return;
													}
													// TODO sync deleted only
													// sync with server
													@SuppressWarnings("unused")
													SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
															.getIntegerPreferenceValueFromStringArray(
																	context,
																	R.string.preference_key_sync_policy,
																	R.array.sync_policy_values_array,
																	R.integer.sync_policy_default_value));
													switch (syncPolicy) {
													case DO_NOT_SYNC:
														// Action picked, so close the CAB
														mode.finish();
														return;
													default:
														break;
													}
													if (!Helper.isDeviceOnline(context)) {
														Toast.makeText(
																context,
																R.string.the_device_is_not_online,
																Toast.LENGTH_SHORT)
																.show();
														// Action picked, so close the CAB
														mode.finish();
														return;
													}
													UserProfile userProfile = DataProvider
															.getUserProfile(null, context);
													if (userProfile == null) {
														Intent intent1 = new Intent(
																context,
																ActivityLogin.class);
														startActivity(intent1);
														// Action picked, so close the CAB
														mode.finish();
														return;
													}
													GetUserInfoTask getUserInfoTask = new GetUserInfoTask();
													getUserInfoTask.execute(userProfile
															.getAuthToken());
													// Action picked, so close the CAB
													mode.finish();
													return;
												}
											})
									.setNegativeButton(R.string.alert_dialog_cancel, null);
							// create alert dialog
							AlertDialog alertDialog = alertDialogBuilder.create();
							// show it
							alertDialog.show();
							return true;
						case R.id.fragment_view_task_tree_cab_menu_set_complete:
							// The method getCheckedItemIds() returns a valid result only
							// if
							// your adapter has stable ids. Otherwise you can use
							// getCheckedItemPositions();
							setCompleted(mTreeView.getCheckedItemIds());
							mode.finish(); // Action picked, so close the CAB
							return true;
						case R.id.fragment_view_task_tree_cab_menu_set_active:
							setActive(mTreeView.getCheckedItemIds());
							mode.finish(); // Action picked, so close the CAB
							return true;
						default:
							return false;
						}
					}

					@Override
					public boolean onCreateActionMode(ActionMode mode, Menu menu) {
						// Inflate the menu for the CAB
						// mActionMode = mode;
						MenuInflater inflater = mode.getMenuInflater();
						Bundle b = getArguments();
						int menuId = 0;
						if (b != null) {
							menuId = b.getInt(CommonConstants.MENU_ID, 0);
						}
						inflater.inflate(menuId, menu);
						mIsInActionMode = true;
						return true;
					}

					@Override
					public void onDestroyActionMode(ActionMode mode) {
						// Here you can make any necessary updates to the activity
						// when
						// the CAB is removed. By default, selected items are
						// deselected/unchecked.
						mIsInActionMode = false;
					}

					@Override
					public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
						// Here you can perform updates to the CAB due to
						// an invalidate() request
						return false;
					}
				});
				break;
			default:
				mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
				break;
			}
		} else {
			registerForContextMenu(mTreeView);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// long dateTime;
		// Respond to clicks on the actions in the CAB
		switch (item.getItemId()) {
		case R.id.fragment_view_task_tree_cab_menu_delete:
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getActivity());
			// final DBHelper dbHelper = new DBHelper(this);
			// final List<MeasurementDevice> totalDeviceList =
			// dbHelper.getAllDevices();
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
									FragmentActivity context = getActivity();
									DataProvider.markTasksAsDeleted(null,
											context.getApplicationContext(),
											mTreeView.getCheckedItemIds(),
											selectedItems[0]);
									// notify about changes
									LocalBroadcastManager
											.getInstance(context.getApplicationContext())
											.sendBroadcast(
													new Intent(
															CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
									LocalBroadcastManager
											.getInstance(context)
											.sendBroadcast(
													new Intent(
															CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
									if (true) {
										return;
									}
									// TODO sync deleted only
									// sync with server
									@SuppressWarnings("unused")
									SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
											.getIntegerPreferenceValueFromStringArray(
													context,
													R.string.preference_key_sync_policy,
													R.array.sync_policy_values_array,
													R.integer.sync_policy_default_value));
									switch (syncPolicy) {
									case DO_NOT_SYNC:
										return;
									default:
										break;
									}
									if (!Helper.isDeviceOnline(context)) {
										Toast.makeText(context,
												R.string.the_device_is_not_online,
												Toast.LENGTH_SHORT).show();
										return;
									}
									UserProfile userProfile = DataProvider
											.getUserProfile(null, context);
									if (userProfile == null) {
										Intent intent1 = new Intent(context,
												ActivityLogin.class);
										startActivity(intent1);
										return;
									}
									GetUserInfoTask getUserInfoTask = new GetUserInfoTask();
									getUserInfoTask.execute(userProfile.getAuthToken());
									return;
								}
							}).setNegativeButton(R.string.alert_dialog_cancel, null);
			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();
			// show it
			alertDialog.show();
			return true;
		case R.id.fragment_view_task_tree_cab_menu_set_complete:
			// The method getCheckedItemIds() returns a valid result only
			// if
			// your adapter has stable ids. Otherwise you can use
			// getCheckedItemPositions();
			setCompleted(mTreeView.getCheckedItemIds());
			return true;
		case R.id.fragment_view_task_tree_cab_menu_set_active:
			setActive(mTreeView.getCheckedItemIds());
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(receiver);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		// outState.putParcelable(FragmentViewTaskTree.TREE_MANAGER_KEY, mManager);
		// outState.putParcelable(
		// FragmentViewTaskTree.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY,
		// mTreeViewListItemDescriptionArrayListOfArrayLists);
		// saveTreeViewListState();
		// outState.putParcelable(FragmentViewTaskTree.TREE_VIEW_LIST_STATE_KEY,
		// mTreeViewListState);
		super.onSaveInstanceState(outState);
	}

	// private void saveTreeViewListState() {
	// int index = mTreeView.getFirstVisiblePosition();
	// View v = mTreeView.getChildAt(0);
	// int top = v == null ? 0 : v.getTop();
	// if (top < 0 && mTreeView.getChildAt(1) != null) {
	// index++;
	// v = mTreeView.getChildAt(1);
	// top = v.getTop();
	// }
	// mTreeViewListState = new TreeViewListState(index, top, null, false, null);
	// }
	private void setCompleted(long[] ids) {
		Intent serviceIntent;
		FragmentActivity context = getActivity();
		DataProvider.markTasksAsCompleted(null, context.getApplicationContext(), ids,
				true);
		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		if (true) {
			return;
		}
		// sync with server
		@SuppressWarnings("unused")
		SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_sync_policy,
						R.array.sync_policy_values_array,
						R.integer.sync_policy_default_value));
		switch (syncPolicy) {
		case DO_NOT_SYNC:
			return;
		default:
			break;
		}
		if (!Helper.isDeviceOnline(context)) {
			Toast.makeText(context, R.string.the_device_is_not_online, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// sync with server
		if (DataProvider.isSignedIn(null, context)) {
			serviceIntent = new Intent(context, SyncService.class);
			serviceIntent.putExtra(CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
					CommonConstants.SYNC_SERVICE_REQUEST_SYNC_UP_TASKS);
			// Start the service
			context.startService(serviceIntent);
		} else {
			Intent intent = new Intent(context, ActivityLogin.class);
			startActivity(intent);
			// finish();
			// return;
		}
	}

	private void setActive(long[] ids) {
		Intent serviceIntent;
		FragmentActivity context = getActivity();
		DataProvider.markTasksAsCompleted(null, context.getApplicationContext(), ids,
				false);
		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		if (true) {
			return;
		}
		// sync with server
		@SuppressWarnings("unused")
		SyncPolicy syncPolicy = SyncPolicy.fromInt((byte) Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_sync_policy,
						R.array.sync_policy_values_array,
						R.integer.sync_policy_default_value));
		switch (syncPolicy) {
		case DO_NOT_SYNC:
			return;
		default:
			break;
		}
		if (!Helper.isDeviceOnline(context)) {
			Toast.makeText(context, R.string.the_device_is_not_online, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// sync with server
		if (DataProvider.isSignedIn(null, context)) {
			serviceIntent = new Intent(context, SyncService.class);
			serviceIntent.putExtra(CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
					CommonConstants.SYNC_SERVICE_REQUEST_SYNC_UP_TASKS);
			// Start the service
			context.startService(serviceIntent);
		} else {
			Intent intent = new Intent(context, ActivityLogin.class);
			startActivity(intent);
			// finish();
			// return;
		}
	}

	private void createIfNeededAndFillAdapter() {
		Bundle b = getArguments();
		boolean getActive = b.getBoolean(
				CommonConstants.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK, true);
		FragmentActivity context = getActivity();
		if (getActive) {
			if (mAdapter == null) {
				mAdapter = new FragmentViewTaskTreeAdapter(context,
						DataProvider.getNodesForestOfActiveTasks(null, context, true));
			} else {
				mAdapter.setNodes(DataProvider.getNodesForestOfActiveTasks(null, context,
						true));
			}
		} else {
			if (mAdapter == null) {
				mAdapter = new FragmentViewTaskTreeAdapter(context,
						DataProvider.getNodesForestOfCompletedTasks(null, context, true));
			} else {
				mAdapter.setNodes(DataProvider.getNodesForestOfCompletedTasks(null,
						context, true));
			}
		}
	}
}
