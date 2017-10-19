package biz.advancedcalendar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import biz.advancedcalendar.activities.accessories.QiuckReminder;

public class ActivityQuickReminderRetainedFragment extends Fragment {
	// data object we want to retain
	public QiuckReminder QiuckReminder;

	// this method is only called once for this fragment
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// retain this fragment
		setRetainInstance(true);
	}
}