package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;

public class FragmentTaskPicker extends DialogFragment {
	// Container Activity must implement this interface
	public interface OnTaskSelectedListener {
		public void onTaskSelected(String tag, String callerTag, Long taskId);
	}

	private OnTaskSelectedListener mListener;
	private String mCallerTag;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Bundle b = getArguments();
		if (b != null) {
			try {
				mCallerTag = b.getString("callerTag");
				if (b.getBoolean("isFromFragment", false)) {
					if (activity instanceof AppCompatActivity) {
						Fragment f = ((AppCompatActivity) activity)
								.getSupportFragmentManager().findFragmentByTag(
										b.getString("tag"));
						mListener = (OnTaskSelectedListener) f;
					} else {
						android.app.Fragment f = activity.getFragmentManager()
								.findFragmentByTag(b.getString("tag"));
						mListener = (OnTaskSelectedListener) f;
					}
				} else {
					mListener = (OnTaskSelectedListener) activity;
				}
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement FragmentTaskPicker.OnTaskSelectedListener");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		DatePickerDialog dpd = new DatePickerDialog(getActivity(), null, 0, 0, 0);
		// Create a new instance of DatePickerDialog and return it
		return dpd;
	}

	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		if (mListener != null) {
			mListener.onTaskSelected(getTag(), mCallerTag, 0L);
		}
	}
}
