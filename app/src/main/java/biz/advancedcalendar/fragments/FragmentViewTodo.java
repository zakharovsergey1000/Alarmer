package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityMain;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class FragmentViewTodo extends Fragment implements OnCheckedChangeListener,
		OnClickListener {
	private BroadcastReceiver receiver;
	// int dateCompareState = 1;
	// int orderCompareState = 2;
	// int priorityCompareState = 3;
	boolean dateReversed = false;
	boolean orderReversed = false;
	boolean priorityReversed = false;

	private class SortOrderComparator implements Comparator<Task> {
		@Override
		public int compare(Task a, Task b) {
			return a.getSortOrder() < b.getSortOrder() ? -1 : a.getSortOrder() == b
					.getSortOrder() ? 0 : 1;
		}
	}

	private class PriorityComparator implements Comparator<Task> {
		@Override
		public int compare(Task a, Task b) {
			int t1_priority = a.getPriority() == 0 ? 1 : a.getPriority() == 1 ? 0 : 2;
			int t2_priority = b.getPriority() == 0 ? 1 : b.getPriority() == 1 ? 0 : 2;
			return t1_priority < t2_priority ? -1 : t1_priority == t2_priority ? 0 : 1;
		}
	}

	// private class StartDateTimeComparator2 implements Comparator<Task> {
	// @Override
	// public int compare(Task a, Task b) {
	// if (a.getStartDateTimeUtc0() == null
	// && b.getStartDateTimeUtc0() == null) {
	// return 0;
	// }
	// if (a.getStartDateTimeUtc0() == null
	// && b.getStartDateTimeUtc0() != null) {
	// return -1;
	// }
	// if (a.getStartDateTimeUtc0() != null
	// && b.getStartDateTimeUtc0() == null) {
	// return 1;
	// }
	// return a.getStartDateTimeUtc0() < b.getStartDateTimeUtc0() ? -1 : a
	// .getStartDateTimeUtc0() == b.getStartDateTimeUtc0() ? 0 : 1;
	// }
	// }
	private class EndDateTimeComparator implements Comparator<Task> {
		@Override
		public int compare(Task a, Task b) {
			if (a.getEndDateTime() == null && b.getEndDateTime() == null) {
				return 0;
			}
			if (a.getEndDateTime() == null && b.getEndDateTime() != null) {
				return -1;
			}
			if (a.getEndDateTime() != null && b.getEndDateTime() == null) {
				return 1;
			}
			return a.getEndDateTime() < b.getEndDateTime() ? -1 : a.getEndDateTime() == b
					.getEndDateTime() ? 0 : 1;
		}
	}

	private class TodoArrayAdapter extends ArrayAdapter<Task> {
		private final Context context;
		private final List<Task> values;

		public TodoArrayAdapter(Context context, List<Task> values) {
			super(context, R.layout.week_multiday_task_view, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView;
			if (convertView != null) {
				rowView = convertView;
			} else {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.week_multiday_task_view, parent,
						false);
			}
			Button button = (Button) rowView.findViewById(R.id.button_task);
			Task t = values.get(position);
			DateFormat dateFormat = DateFormat.getDateTimeInstance();
			String startDateTimeString = t.getStartDateTime() == null ? "null_0000000000000"
					: dateFormat.format(new Date(t.getStartDateTime()));
			String endDateTimeString = t.getEndDateTime() == null ? "null_0000000000000"
					: dateFormat.format(new Date(t.getEndDateTime()));
			String dateString = startDateTimeString + " - " + endDateTimeString;
			// button.setText(+t.getSortOrder()
			// + " : "
			// + (t.getPriority() == 0 ? 1
			// : (t.getPriority() == 1 ? 0 : 2)) + " : "
			// + dateString + " : " + t.getText());
			button.setText(+t.getSortOrder()
					+ " : "
					+ (t.getPriority() == 0 ? getResources().getString(
							R.string.fragment_view_todo_medium)
							: t.getPriority() == 1 ? getResources().getString(
									R.string.fragment_view_todo_low)
									+ "000" : getResources().getString(
									R.string.fragment_view_todo_high)
									+ "00") + " : " + dateString + " : " + t.getName());
			button.setTag(t);
			button.setOnClickListener(FragmentViewTodo.this);
			return rowView;
		}
	}

	private List<Task> mTodoList = null;
	// private List<Task> mTodoListSortOrderPriority = null;
	// private List<Task> mTodoListSortOrderDate = null;
	private ListView lv;
	private ArrayAdapter<Task> arrayAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
					// if (!isDetached()) {
					if (isAdded()) {
						if (getActivity() == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						fillListView();
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = null;// inflater.inflate(R.layout.fragment_todo, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
				.registerReceiver(receiver,
						new IntentFilter(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		fillListView();
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
	public void onPause() {
		super.onPause();
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
		if (getActivity().isFinishing()) {
			// DataProvider.removeDataChangedListener(this);
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(receiver);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	private void fillListView() {
		// lv = (ListView) getActivity().findViewById(R.id.todo_listview);
		mTodoList = DataProvider.getTodoList(null, getActivity().getApplicationContext());
		// case R.id.radio_sort_order_default:
		Collections.sort(mTodoList, new EndDateTimeComparator());
		Collections.sort(mTodoList, new PriorityComparator());
		Collections.sort(mTodoList, new SortOrderComparator());
		arrayAdapter = new TodoArrayAdapter(getActivity(), mTodoList);
		lv.setAdapter(arrayAdapter);
		// RadioGroup rg = null;// (RadioGroup) getActivity().findViewById(
		// R.id.radiogroup_sort_order);
		// rg.setOnCheckedChangeListener(this);
		// RadioButton rb = null;// (RadioButton) getActivity().findViewById(
		// R.id.radio_sort_order_default);
		// rb.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// orderReversed = !orderReversed;
		// if (!orderReversed) {
		// Collections.sort(mTodoList, new SortOrderComparator());
		// ((RadioButton) v).setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_expand_more_black_24dp, 0, 0, 0);
		// } else {
		// Collections.sort(mTodoList,
		// Collections.reverseOrder(new SortOrderComparator()));
		// ((RadioButton) v).setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_expand_less_black_24dp, 0, 0, 0);
		// }
		// arrayAdapter.notifyDataSetChanged();
		// }
		// });
		// rb = (RadioButton) getActivity().findViewById(R.id.radio_sort_order_priority);
		// rb.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// priorityReversed = !priorityReversed;
		// if (!priorityReversed) {
		// Collections.sort(mTodoList, new PriorityComparator());
		// ((RadioButton) v).setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_expand_more_black_24dp, 0, 0, 0);
		// } else {
		// Collections.sort(mTodoList,
		// Collections.reverseOrder(new PriorityComparator()));
		// ((RadioButton) v).setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_expand_less_black_24dp, 0, 0, 0);
		// }
		// arrayAdapter.notifyDataSetChanged();
		// }
		// });
		// rb = (RadioButton) getActivity().findViewById(R.id.radio_sort_order_date);
		// rb.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// RadioButton rb1 = (RadioButton) v;
		// dateReversed = !dateReversed;
		// if (!dateReversed) {
		// Collections.sort(mTodoList, new EndDateTimeComparator());
		// rb1.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_expand_more_black_24dp, 0, 0, 0);
		// } else {
		// Collections.sort(mTodoList,
		// Collections.reverseOrder(new EndDateTimeComparator()));
		// rb1.setCompoundDrawablesWithIntrinsicBounds(
		// R.drawable.ic_expand_less_black_24dp, 0, 0, 0);
		// }
		// arrayAdapter.notifyDataSetChanged();
		// }
		// });
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		// case R.id.radio_sort_order_default:
		// orderReversed = !orderReversed;
		// // if (!orderReversed)
		// // Collections.sort(mTodoList, new SortOrderComparator());
		// // else
		// // Collections.sort(mTodoList,
		// // Collections.reverseOrder(new SortOrderComparator()));
		// break;
		// case R.id.radio_sort_order_priority:
		// priorityReversed = !priorityReversed;
		// // if (!priorityReversed)
		// // Collections.sort(mTodoList, new PriorityComparator());
		// // else
		// // Collections.sort(mTodoList,
		// // Collections.reverseOrder(new PriorityComparator()));
		// break;
		// case R.id.radio_sort_order_date:
		// dateReversed = !dateReversed;
		// // if (!dateReversed)
		// // Collections.sort(mTodoList, new StartDateTimeComparator());
		// // else
		// // Collections
		// // .sort(mTodoList, Collections
		// // .reverseOrder(new StartDateTimeComparator()));
		// break;
		default:
			break;
		}
		// arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		Task task = (Task) v.getTag();
		long taskId = task.getId();
		ActivityMain.launchTaskViewerOrEditor(getActivity(), taskId, 0);
	}
	// @Override
	// public void onDataChanged(DataTypeChanged dataType) {
	// if (dataType == DataTypeChanged.TASKS) {
	// // if (!isDetached()) {
	// if (isAdded()) {
	// fillListView();
	// }
	// }
	// }
}
