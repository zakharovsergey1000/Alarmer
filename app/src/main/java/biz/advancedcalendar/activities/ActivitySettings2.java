package biz.advancedcalendar.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.fragments.FragmentSettings;

public class ActivitySettings2 extends ActionBarActivity {
	private static final String TAG = "FragmentSettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings2);
		Toolbar myToolbar = (Toolbar) findViewById(R.id.activity_settings2_toolbar);
		setSupportActionBar(myToolbar);
		// Get a support ActionBar corresponding to this toolbar
		ActionBar ab = getSupportActionBar();
		// Show the Up button in the action bar.
		ab.setDisplayHomeAsUpEnabled(true);
		FragmentManager supportFragmentManager = getSupportFragmentManager();
		Fragment f = supportFragmentManager.findFragmentByTag(ActivitySettings2.TAG);
		if (f == null) {
			f = new FragmentSettings();
			supportFragmentManager.beginTransaction()
					.add(R.id.activity_settings2_container, f, ActivitySettings2.TAG)
					.commit();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown.
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
