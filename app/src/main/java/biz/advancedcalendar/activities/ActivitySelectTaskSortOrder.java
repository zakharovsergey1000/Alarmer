package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.utils.Helper;
import java.util.List;

public class ActivitySelectTaskSortOrder extends AppCompatActivity {
	private ListView mListView;
	private boolean mActionDonePerformed = false;
	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setTitle(getResources().getString(
				R.string.activity_select_task_sort_order_title));
		// Task task = getIntent().getParcelableExtra(CommonConstants.INTENT_EXTRA_TASK);
		UserInterfaceData userInterfaceData = getIntent().getParcelableExtra(
				CommonConstants.INTENT_EXTRA_USER_INTERFACE_DATA);
		List<Task> list;
		// if (task.getPercentOfCompletion() < 100) {
		boolean isActive = userInterfaceData.percentOfCompletion < 100;
		list = DataProvider.getTaskSiblingsFromParent(null,
				getApplicationContext(), userInterfaceData.id, userInterfaceData.parentId, false, isActive);
		// if (userInterfaceData.percentOfCompletion < 100) {
		// entityList = DataProvider.getActiveTaskSiblings(getApplicationContext(),
		// userInterfaceData.id, false);
		// } else {
		// entityList = DataProvider.getCompletedTaskSiblings(getApplicationContext(),
		// userInterfaceData.id, false);
		// }
		mListView = (ListView) findViewById(R.id.list_view);
		TaskArrayAdapter arrayAdapter = new TaskArrayAdapter(this,
				android.R.layout.simple_list_item_single_choice, list);
		mListView.setAdapter(arrayAdapter);
		mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem;
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_OK, 2, getResources()
				.getString(R.string.action_done));
		menuItem.setIcon(R.drawable.ic_done_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_CANCEL, 1, getResources()
				.getString(R.string.action_cancel));
		menuItem.setIcon(R.drawable.ic_cancel_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_CANCEL:
			finish();
			return true;
		case CommonConstants.MENU_ID_OK:
			mActionDonePerformed = true;
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void finish() {
		Intent intent = getIntent();
		if (!mActionDonePerformed) {
			// Activity finished by cancel, return no data
			setResult(Activity.RESULT_CANCELED);
			super.finish();
			return;
		}
		long[] idArray = mListView.getCheckedItemIds();
		if (idArray.length > 0) {
			intent.putExtra(CommonConstants.RETURN_ID, idArray[0]);
		}
		// Activity finished ok, return the data
		setResult(Activity.RESULT_OK, intent);
		super.finish();
	}

	private class TaskArrayAdapter extends ArrayAdapter<Task> {
		List<Task> objects;

		public TaskArrayAdapter(Context context, int textViewResourceId,
				List<Task> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CheckedTextView textView;
			if (convertView == null) {
				textView = (CheckedTextView) getLayoutInflater().inflate(
						android.R.layout.simple_list_item_single_choice, parent, false);
			} else {
				textView = (CheckedTextView) convertView;
			}
			Task task = objects.get(position);
			int backgroundColor = task.getColor2(getApplicationContext());
			int textColor;
			// Helper. getContrast50(backgroundColor);
			if (Helper.getContrastYIQ(backgroundColor)) {
				textColor = getApplicationContext().getResources().getColor(
						R.color.task_view_text_synchronized_dark);
			} else {
				textColor = getApplicationContext().getResources().getColor(
						R.color.task_view_text_synchronized_light);
			}
			textView.setBackgroundColor(backgroundColor);
			textView.setTextColor(textColor);
			textView.setText(task.getName());
			return textView;
		}

		@Override
		public long getItemId(int position) {
			Task item = objects.get(position);
			return item.getId();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}
