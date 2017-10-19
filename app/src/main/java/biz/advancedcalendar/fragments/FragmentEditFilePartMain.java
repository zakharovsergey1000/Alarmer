package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;

public class FragmentEditFilePartMain extends Fragment implements DataSaver {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater
				.inflate(R.layout.fragment_edit_file_part_main, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			Activity activity = getActivity();
			// setup name field
			EditText editText = (EditText) activity
					.findViewById(R.id.fragment_edit_file_part_main_edittext_file_name);
			editText.setText(Global.getFileToEdit().getFileName());
			// setup description field
			editText = (EditText) activity
					.findViewById(R.id.fragment_edit_file_part_main_edittext_file_description);
			editText.setText(Global.getFileToEdit().getDescription());
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

	@Override
	public boolean isDataCollected() {
		Activity activity = getActivity();
		// collect File name
		EditText editText = (EditText) activity
				.findViewById(R.id.fragment_edit_file_part_main_edittext_file_name);
		String text = editText.getText().toString().trim();
		Global.getFileToEdit().setFileName(text);
		// collect File description
		editText = (EditText) activity
				.findViewById(R.id.fragment_edit_file_part_main_edittext_file_description);
		text = editText.getText().toString().trim();
		Global.getFileToEdit().setDescription(text);
		return true;
	}
}
