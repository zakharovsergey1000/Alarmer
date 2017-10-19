package biz.advancedcalendar.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import biz.advancedcalendar.alarmer.R;

public class CalendarNameDialogFragment extends DialogFragment {
	private EditText mEdittext;
	private String mCalendarName;
	private YesNoListener mCallback;

	public interface YesNoListener {
		void onYesInCalendarNameDialogFragment(String calendarName);

		void onNoInCalendarNameDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mCalendarName = savedInstanceState.getString("calendarName");
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		mEdittext = new EditText(getActivity());
		mEdittext.setText(mCalendarName);
		alert.setMessage(getResources().getString(R.string.input_calendar_name));
		alert.setView(mEdittext);
		alert.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mCallback != null) {
							String newCalendarName = mEdittext.getText().toString();
							mCallback.onYesInCalendarNameDialogFragment(newCalendarName);
						}
					}
				});
		alert.setNegativeButton(R.string.alert_dialog_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mCallback != null) {
							mCallback.onNoInCalendarNameDialogFragment();
						}
					}
				});
		return alert.create();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("calendarName", mEdittext.getText().toString());
	}

	public void setCalendarName(String calendarName) {
		mCalendarName = calendarName;
	}

	public void setCallback(YesNoListener callback) {
		mCallback = callback;
	}
}