package biz.advancedcalendar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;

public class FragmentEditTaskPartNotes extends Fragment implements DataSaver {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(CommonConstants.DEBUG_TAG, "onCreateView");
		View v = inflater.inflate(R.layout.fragment_edit_task_part_notes, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		if (savedInstanceState == null) {
			FragmentActivity context = getActivity();
			UserInterfaceData userInterfaceData = ((TaskWithDependentsUiDataHolder) context)
					.getUserInterfaceData();
			// Activity activity = getActivity();
			// set values on views
			// setup EditText task notes
			((EditText) context.findViewById(R.id.fragment_edit_task_part_notes_edittext))
					.setText(userInterfaceData.taskDescription);
		}
	}

	@Override
	public void onStop() {
		if (!getActivity().isFinishing()) {
			// collect info
			isDataCollected();
		}
		super.onStop();
	}

	private boolean isTaskNotesCollected() {
		boolean isCollected = true;
		EditText editText = (EditText) getActivity().findViewById(
				R.id.fragment_edit_task_part_notes_edittext);
		String description = editText.getText().toString().trim();
		FragmentActivity activity = getActivity();
		if (activity != null) {
			UserInterfaceData userInterfaceData = ((TaskWithDependentsUiDataHolder) activity)
					.getUserInterfaceData();
			userInterfaceData.taskDescription = description;
			TaskWithDependentsUiData taskWithDependentsUiData = ((TaskWithDependentsUiDataHolder) activity)
					.getTaskWithDependentsUiData();
			taskWithDependentsUiData.TaskUiData.setDescription(description);
		}
		return isCollected;
	}

	@Override
	public boolean isDataCollected() {
		boolean isCollected = true;
		if (!isTaskNotesCollected()) {
			isCollected = false;
		}
		return isCollected;
	}
}
