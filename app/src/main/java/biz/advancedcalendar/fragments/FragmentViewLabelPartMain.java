package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Label;

public class FragmentViewLabelPartMain extends Fragment {

	private Label label;
	private EditText editText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(
				R.layout.fragment_view_label_part_main,
				container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle b = getArguments();
		long id = b.getLong(CommonConstants.INTENT_EXTRA_ID);

		Activity activity = getActivity();
		Global global = (Global) activity.getApplicationContext();
		label = DataProvider.getLabel(null, global, id, false);

		editText = (EditText) activity
				.findViewById(R.id.fragment_edit_label_part_main_edittext_label_name);
		editText.setText(label.getText() );

		editText = (EditText) activity
				.findViewById(R.id.fragment_edit_label_part_main_edittext_label_description);
		editText.setText(label.getDescription());
	}
}
