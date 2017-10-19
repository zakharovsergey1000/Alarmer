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
import biz.advancedcalendar.greendao.File;

public class FragmentViewFilePartMain extends Fragment {

	private File mFile;
	private TextView mTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(
				R.layout.fragment_view_file_part_main,
				container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle b = getArguments();
		long id = b.getLong(CommonConstants.INTENT_EXTRA_ID);	
		
		Activity activity = getActivity();
		Global global = ((Global) activity.getApplicationContext());
		mFile = DataProvider.getFile(null, global, id);

		mTextView = (TextView) activity
				.findViewById(R.id.fragment_view_file_part_main_header_textview_file_name);
		mTextView.setText(mFile.getFileName());

		mTextView = (TextView) activity
				.findViewById(R.id.fragment_view_file_part_main_header_textview_file_description);
		mTextView.setText(mFile.getDescription());
	}
}
