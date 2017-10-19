package biz.advancedcalendar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import biz.advancedcalendar.activities.ActivityMain.ActivityState;

public class RetainedFragmentForActivityMain extends Fragment {
	// public Bundle fragmentViewWeekData;
	public Long lastUsedFirstDayOfWeekOnWeekViewOnTabDay;
	public Long lastUsedFirstDayOfWeekOnWeekViewOnTabWeek;
	public boolean selectItemIsPending;
	public ActivityState selectItemArg1;
	public int selectItemArg2;

	//
	public RetainedFragmentForActivityMain() {
	}

	// this method is only called once for this fragment
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// retain this fragment
		setRetainInstance(true);
	}
}