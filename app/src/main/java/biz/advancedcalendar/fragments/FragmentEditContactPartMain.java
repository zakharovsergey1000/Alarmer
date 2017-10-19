package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.ContactWithDependents;

public class FragmentEditContactPartMain extends Fragment implements DataSaver {
	// private Long mEntityToEditId = null;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(CommonConstants.DEBUG_TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Restore state here
		Log.i(CommonConstants.DEBUG_TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(CommonConstants.DEBUG_TAG, "onCreateView");
		View v = inflater.inflate(R.layout.fragment_edit_contact_part_main, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		if (savedInstanceState == null) {
			ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
					.getContactWithDependencies();
			// setup name field
			EditText editText = (EditText) getActivity().findViewById(
					R.id.fragment_edit_contact_part_main_edittext_contact_name);
			editText.setText(contactWithDependencies.contact.getContactName());
			// setup description field
			editText = (EditText) getActivity().findViewById(
					R.id.fragment_edit_contact_part_main_edittext_contact_description);
			editText.setText(contactWithDependencies.contact.getDescription());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(CommonConstants.DEBUG_TAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(CommonConstants.DEBUG_TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(CommonConstants.DEBUG_TAG, "onPause");
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
	public void onSaveInstanceState(final Bundle outState) {
		// outState.putSerializable("mEntityToEdit", mEntityToEdit);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(CommonConstants.DEBUG_TAG, "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(CommonConstants.DEBUG_TAG, "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(CommonConstants.DEBUG_TAG, "onDetach");
	}

	@Override
	public boolean isDataCollected() {
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		// collect label name
		EditText editText = (EditText) getActivity().findViewById(
				R.id.fragment_edit_contact_part_main_edittext_contact_name);
		String text = editText.getText().toString().trim();
		contactWithDependencies.contact.setContactName(text);
		// collect label description
		editText = (EditText) getActivity().findViewById(
				R.id.fragment_edit_contact_part_main_edittext_contact_description);
		text = editText.getText().toString().trim();
		contactWithDependencies.contact.setDescription(text);
		return true;
	}
}
