package biz.advancedcalendar.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.ObjectHolder;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityCalendarDetail;
import biz.advancedcalendar.activities.ActivityCalendarList;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.utils.LongParcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a single Calendar detail screen. This fragment is either
 * contained in a {@link ActivityCalendarList} in two-pane mode (on tablets) or a
 * {@link ActivityCalendarDetail} on handsets.
 */
public class FragmentCalendarDetail extends ListFragment {
    /**
     * The fragment argument representing the item ID that this fragment represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    /**
     * The dummy content this fragment is presenting.
     */
    private List<Task> mTasks;
    private BroadcastReceiver mReceiver;
    private ListView mListview;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment
     * (e.g. upon screen orientation changes).
     */
    public FragmentCalendarDetail() {
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments.containsKey(FragmentCalendarDetail.ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            Long calendarId = ((LongParcelable) arguments
                    .getParcelable(FragmentCalendarDetail.ARG_ITEM_ID)).value;
            List<Long> calendarIds = new ArrayList<Long>();
            calendarIds.add(calendarId);
            mTasks = DataProvider.getNonDeletedTasks(null, getActivity(), calendarIds,
                    true, true);
            setListAdapter(new ArrayAdapter<Task>(
                    getActivity(),
                    android.os.Build.VERSION.SDK_INT >= 11 ? android.R.layout.simple_list_item_activated_1
                            : android.R.layout.simple_list_item_checked,
                    android.R.id.text1, mTasks) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    // Get the data item for this position
                    Task task = getItem(position);
                    // Check if an existing view is being reused, otherwise inflate the
                    // view
                    if (convertView == null) {
                        convertView = LayoutInflater
                                .from(getContext())
                                .inflate(
                                        android.os.Build.VERSION.SDK_INT >= 11 ? android.R.layout.simple_list_item_activated_1
                                                : android.R.layout.simple_list_item_checked,
                                        parent, false);
                    }
                    // Lookup view for data population
                    TextView tvName = (TextView) convertView
                            .findViewById(android.R.id.text1);
                    // Populate the data into the template view using the data object
                    tvName.setText(task.getName());
                    // Return the completed view to render on screen
                    return convertView;
                }
            });
        }
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @SuppressLint("InlinedApi")
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction().equals(
                            CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)
                            || intent.getAction().equals(
                            CommonConstants.ACTION_CALENDARS_CHANGED)) {
                        // if (!isDetached()) {
                        if (isAdded()) {
                            Bundle arguments = getArguments();
                            Long calendarId = ((LongParcelable) arguments
                                    .getParcelable(FragmentCalendarDetail.ARG_ITEM_ID)).value;
                            List<Long> calendarIds = new ArrayList<Long>();
                            calendarIds.add(calendarId);
                            List<Task> tasks = DataProvider.getNonDeletedTasks(null,
                                    getActivity(), calendarIds, true, true);
                            mTasks.clear();
                            mTasks.addAll(tasks);
                            ArrayAdapter<Task> listAdapter = (ArrayAdapter<Task>) getListAdapter();
                            listAdapter.notifyDataSetChanged();
                        } else {
                            // mStateParameters.TasksNeedReload = true;
                        }
                    }
                }
            };
            //
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
            intentFilter.addAction(CommonConstants.ACTION_CALENDARS_CHANGED);
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                    .registerReceiver(mReceiver, intentFilter);
        }
    }

    // @Override
    // public View onCreateView(LayoutInflater inflater, ViewGroup container,
    // Bundle savedInstanceState) {
    // View rootView = inflater.inflate(R.layout.fragment_calendar_detail, container,
    // false);
    // // Show the dummy content as text in a TextView.
    // if (mItem != null) {
    // ((TextView) rootView.findViewById(R.id.calendar_detail))
    // .setText(mItem.content);
    // }
    // return rootView;
    // }
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
                    final List<Long> taskIds = new ArrayList<Long>();
                    switch (item.getItemId()) {
                        case CommonConstants.MENU_ID_MOVE:
                            Long id;
                            Task task = null;
                            for (int i = 0; i < size; i++) {
                                if (checkedItemPositions.valueAt(i)) {
                                    task = (Task) mListview
                                            .getItemAtPosition(checkedItemPositions.keyAt(i));
                                    id = task.getId();
                                    taskIds.add(id);
                                }
                            }
                            //
                            final List<biz.advancedcalendar.greendao.Calendar> calendars =
                                    new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider
                                            .getCalendars(null, getActivity()));
                            calendars.add(
                                    0,
                                    new biz.advancedcalendar.greendao.Calendar(null, null,
                                            getResources().getString(
                                                    R.string.default_calendar_name)));
                            CharSequence[] calendarsNames = new CharSequence[calendars.size()];
                            for (int i = 0; i < calendarsNames.length; i++) {
                                calendarsNames[i] = calendars.get(i).getName();
                            }
                            //
                            // Where we track the selected item
                            final ObjectHolder mSelectedItem = new ObjectHolder(0);
                            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                            alert.setTitle(R.string.choose_calendar);
                            // Specify the list array, the items to be selected by default
                            // (null for
                            // none),
                            // and the listener through which to receive callbacks when items
                            // are selected
                            alert.setSingleChoiceItems(calendarsNames, -1,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mSelectedItem.value = which;
                                        }
                                    });
                            alert.setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            DataProvider.moveTasks(
                                                    null,
                                                    getActivity(),
                                                    taskIds,
                                                    calendars.get(
                                                            (Integer) mSelectedItem.value)
                                                            .getId());
                                            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                                                    .getInstance(getActivity());
                                            localBroadcastManager
                                                    .sendBroadcast(new Intent(
                                                            CommonConstants.ACTION_CALENDARS_CHANGED));
                                            localBroadcastManager
                                                    .sendBroadcast(new Intent(
                                                            CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
                                            mode.finish();
                                        }
                                    });
                            alert.setNegativeButton(R.string.alert_dialog_cancel,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            // what ever you want to do with No
                                            // option.
                                        }
                                    });
                            alert.show();
                            return true;
                        case CommonConstants.MENU_ID_DELETE:
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    getActivity());
                            alertDialogBuilder
                                    .setMessage(
                                            getResources().getString(
                                                    R.string.action_delete_task_confirmation))
                                    // .setMessage(
                                    // getResources().getString(
                                    // R.string.action_delete_task_confirmation))
                                    .setCancelable(true)
                                    // Set the action buttons
                                    .setPositiveButton(R.string.alert_dialog_ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int id) {
                                                    for (Long taskId : taskIds) {
                                                        DataProvider.deleteTaskPermanently(
                                                                null, getActivity(), taskId,
                                                                false);
                                                    }
                                                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                                                            .getInstance(getActivity());
                                                    localBroadcastManager
                                                            .sendBroadcast(new Intent(
                                                                    CommonConstants.ACTION_CALENDARS_CHANGED));
                                                    localBroadcastManager
                                                            .sendBroadcast(new Intent(
                                                                    CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
                                                    mode.finish();
                                                }
                                            })
                                    .setNegativeButton(R.string.alert_dialog_cancel, null);
                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();
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
                    // mActionMode = mode;
                    // onPrepareActionMode(mode, menu);
                    MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_MOVE,
                            Menu.FIRST + 100, getResources().getString(R.string.move));
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_DELETE,
                            Menu.FIRST + 200, getResources().getString(R.string.delete));
                    menuItem.setIcon(R.drawable.ic_delete_black_24dp);
                    menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                    return true;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    // Here you can make any necessary updates to the activity
                    // when
                    // the CAB is removed. By default, selected items are
                    // deselected/unchecked.
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // Here you can perform updates to the CAB due to
                    // an invalidate() request
                    return false;
                }
            });
            //
        } else {
            registerForContextMenu(mListview);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // menu.setHeaderTitle("Context Menu");
        menu.add(0, R.id.move, 0, R.string.move);
        menu.add(0, R.id.delete, 0, R.string.delete);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        ListView.AdapterContextMenuInfo menuInfo = (ListView.AdapterContextMenuInfo) item
                .getMenuInfo();
        final Task task = (Task) mListview.getItemAtPosition(menuInfo.position);
        final Long taskId;
        taskId = task.getId();
        if (taskId == null) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.move:
                final List<biz.advancedcalendar.greendao.Calendar> calendars = new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider
                        .getCalendars(null, getActivity()));
                calendars.add(0, new biz.advancedcalendar.greendao.Calendar(null, null,
                        getResources().getString(R.string.default_calendar_name)));
                CharSequence[] calendarsNames = new CharSequence[calendars.size()];
                for (int i = 0; i < calendarsNames.length; i++) {
                    calendarsNames[i] = calendars.get(i).getName();
                }
                //
                // Where we track the selected item
                final ObjectHolder mSelectedItem = new ObjectHolder(0);
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle(R.string.choose_calendar);
                // Specify the list array, the items to be selected by default (null for
                // none),
                // and the listener through which to receive callbacks when items are selected
                alert.setSingleChoiceItems(calendarsNames, 0,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mSelectedItem.value = which;
                            }
                        });
                alert.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                List<Long> taskIds = new ArrayList<Long>();
                                taskIds.add(taskId);
                                DataProvider.moveTasks(null, getActivity(), taskIds,
                                        calendars.get((Integer) mSelectedItem.value).getId());
                                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                                        .getInstance(getActivity());
                                localBroadcastManager.sendBroadcast(new Intent(
                                        CommonConstants.ACTION_CALENDARS_CHANGED));
                                localBroadcastManager.sendBroadcast(new Intent(
                                        CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
                            }
                        });
                alert.setNegativeButton(R.string.alert_dialog_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // what ever you want to do with No
                                // option.
                            }
                        });
                alert.show();
                return true;
            case R.id.delete:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());
                alertDialogBuilder
                        .setMessage(
                                getResources().getString(
                                        R.string.action_delete_task_confirmation))
                        // .setMessage(
                        // getResources().getString(
                        // R.string.action_delete_task_confirmation))
                        .setCancelable(true)
                        // Set the action buttons
                        .setPositiveButton(R.string.alert_dialog_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        DataProvider.markTasksAsDeleted(null, getActivity(),
                                                new long[]{taskId}, false);
                                        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
                                                .getInstance(getActivity());
                                        localBroadcastManager.sendBroadcast(new Intent(
                                                CommonConstants.ACTION_CALENDARS_CHANGED));
                                        localBroadcastManager
                                                .sendBroadcast(new Intent(
                                                        CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
                                    }
                                }).setNegativeButton(R.string.alert_dialog_cancel, null);
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
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
    public void onDestroy() {
        super.onDestroy();
        if (true /* || getActivity().isFinishing() */) {
            LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
                    .unregisterReceiver(mReceiver);
        }
    }
}
