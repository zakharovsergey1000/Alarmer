package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task;

public class FragmentViewTaskPartNotes extends Fragment {
	private Task mTask;
	private TextView mTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_view_task_part_notes, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		long id = b.getLong(CommonConstants.INTENT_EXTRA_ID);
		Activity activity = getActivity();
		Global global = (Global) activity.getApplicationContext();
		mTask = DataProvider.getTask(null, global, id, false);
		mTextView = (TextView) activity
				.findViewById(R.id.fragment_view_task_part_notes_value_textview_task_notes);
		mTextView.setText(mTask.getDescription());
	}
}
