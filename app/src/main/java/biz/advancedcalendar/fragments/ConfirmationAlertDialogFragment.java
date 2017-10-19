package biz.advancedcalendar.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import biz.advancedcalendar.alarmer.R;

public class ConfirmationAlertDialogFragment extends DialogFragment {
	public interface YesNoListener {
		void onYesInConfirmationAlertDialogFragment(
				ConfirmationAlertDialogFragment dialogFragment);

		void onNoInConfirmationAlertDialogFragment(
				ConfirmationAlertDialogFragment dialogFragment);
		// void onNeutral();
	}

	private String mTitle;
	private String mMessage;
	private YesNoListener mCallback;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTitle = savedInstanceState.getString("mTitle");
			mMessage = savedInstanceState.getString("mMessage");
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		if (mTitle != null) {
			alert.setTitle(mTitle);
		}
		if (mMessage != null) {
			alert.setMessage(mMessage);
		}
		alert.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mCallback != null) {
							mCallback
									.onYesInConfirmationAlertDialogFragment(ConfirmationAlertDialogFragment.this);
						}
					}
				});
		alert.setNegativeButton(R.string.alert_dialog_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						if (mCallback != null) {
							mCallback
									.onNoInConfirmationAlertDialogFragment(ConfirmationAlertDialogFragment.this);
						}
					}
				});
		return alert.create();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("mTitle", mTitle);
		savedInstanceState.putString("mMessage", mMessage);
	}

	public void setTitleAndMessage(String title, String message) {
		mTitle = title;
		mMessage = message;
	}

	public void setCallback(YesNoListener callback) {
		mCallback = callback;
	}
}