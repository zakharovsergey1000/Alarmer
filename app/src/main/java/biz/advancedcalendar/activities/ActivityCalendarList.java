package biz.advancedcalendar.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.fragments.FragmentCalendarDetail;
import biz.advancedcalendar.fragments.FragmentCalendarList;
import biz.advancedcalendar.utils.LongParcelable;

/** An activity representing a list of Calendars. This activity has different presentations
 * for handset and tablet-size devices. On handsets, the activity presents a list of
 * items, which when touched, lead to a {@link ActivityCalendarDetail} representing item
 * details. On tablets, the activity presents the list of items and item details
 * side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link FragmentCalendarList} and the item details (if present) is a
 * {@link FragmentCalendarDetail}.
 * <p>
 * This activity also implements the required {@link FragmentCalendarList.Callbacks}
 * interface to listen for item selections. */
public class ActivityCalendarList extends AppCompatActivity implements
		FragmentCalendarList.Callbacks {
	/** Whether or not the activity is in two-pane mode, i.e. running on a tablet device. */
	private boolean mTwoPane;
	private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar_list);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();
		// Enable the Up button
		ab.setDisplayHomeAsUpEnabled(true);
		if (findViewById(R.id.calendar_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;
			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((FragmentCalendarList) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}
		// TODO: If exposing deep links into your app, handle intents here.
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Respond to the action bar's Up/Home button
		case android.R.id.home:
			finish();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/** Callback method from {@link FragmentCalendarList.Callbacks} indicating that the
	 * item with the given ID was selected. */
	@Override
	public void onItemSelected(Long id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			FragmentCalendarDetail fragment = new FragmentCalendarDetail();
			Bundle arguments = new Bundle();
			arguments.putParcelable(FragmentCalendarDetail.ARG_ITEM_ID,
					new LongParcelable(id));
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.calendar_detail_container, fragment).commit();
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ActivityCalendarDetail.class);
			detailIntent.putExtra(FragmentCalendarDetail.ARG_ITEM_ID, new LongParcelable(
					id));
			startActivity(detailIntent);
		}
	}

	@Override
	public void onItemLongClick(Long id) {
		// TODO Auto-generated method stub
	}
}
