package biz.advancedcalendar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import biz.advancedcalendar.activities.ActivityImportTasks.ImportTasksState;

// TODO have to implement onSaveInstanceState and state restoration
public class RetainedFragmentForActivityImportTasks extends Fragment {
	public ImportTasksState ImportTasksState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			ImportTasksState = savedInstanceState.getParcelable("ImportTasksState");
		}
		// retain this fragment
		setRetainInstance(true);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable("ImportTasksState", ImportTasksState);
	}
}