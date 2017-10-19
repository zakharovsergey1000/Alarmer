package biz.advancedcalendar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;

public class FragmentEditDiaryRecordPartNotes extends Fragment implements DataSaver {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_edit_diary_record_part_notes,
				container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			// set values on views
			// setup EditText task notes
			((EditText) getActivity()
					.findViewById(
							R.id.fragment_edit_diary_record_part_notes_edittext_diary_record_notes))
					.setText(Global.getDiaryRecordToEdit().getFullText());
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
		// collect info
		// EditText mEditText = (EditText) getActivity()
		// .findViewById(R.id.fragment_edit_task_part_notes_edittext);
		Global.getDiaryRecordToEdit()
				.setFullText(
						((EditText) getActivity()
								.findViewById(
										R.id.fragment_edit_diary_record_part_notes_edittext_diary_record_notes))
								.getText().toString());
		return true;
	}
}
