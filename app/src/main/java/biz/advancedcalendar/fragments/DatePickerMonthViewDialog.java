package biz.advancedcalendar.fragments;

import biz.advancedcalendar.alarmer.R;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class DatePickerMonthViewDialog extends DialogFragment implements
		OnEditorActionListener {
	public interface EditNameDialogListener {
		void onFinishEditDialog(String inputText);
	}
	public DatePickerMonthViewDialog() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.date_picker, container);
		//getDialog().setTitle(null);
		return view;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	  Dialog dialog = super.onCreateDialog(savedInstanceState);

	  // request a window without the title
	  dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
	  return dialog;
	}
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			// Return input text to activity
			EditNameDialogListener activity = (EditNameDialogListener) getActivity();
			activity.onFinishEditDialog("");
			this.dismiss();
			return true;
		}
		return false;
	}
}
