package biz.advancedcalendar.fragments;

import biz.advancedcalendar.alarmer.R;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class EditNameDialog extends DialogFragment implements OnEditorActionListener {
	public interface EditNameDialogListener {
		void onFinishEditDialog(String inputText);
	}

	private EditText mEditText;

	public EditNameDialog() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(
				R.layout.fragment_view_task_part_reminders_list_item, container);
		mEditText = (EditText) view
				.findViewById(R.id.activity_edit_reminder_edittext_reminder_description);
		getDialog().setTitle("Hello");
		// Show soft keyboard automatically
		mEditText.requestFocus();
		getDialog().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		mEditText.setOnEditorActionListener(this);
		return view;
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			// Return input text to activity
			EditNameDialogListener activity = (EditNameDialogListener) getActivity();
			activity.onFinishEditDialog(mEditText.getText().toString());
			this.dismiss();
			return true;
		}
		return false;
	}
}
