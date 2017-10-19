package biz.advancedcalendar.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import biz.advancedcalendar.alarmer.R;

public class AlertDialogFragment extends DialogFragment {
	private EditText mEdittext;
	private AlertDialog mAlertDialog;
	private Builder mBuilder;

	public Builder getBuilder() {
		if (mBuilder == null) {
			mBuilder = new Builder(getActivity());
		}
		return mBuilder;
	}

	class Builder extends AlertDialog.Builder {
		private Context mContext;
		private int mTheme;

		public Builder(Context context) {
			super(context);
			mContext = context;
			// TODO Auto-generated constructor stub
		}

		public Builder(Context context, int theme) {
			super(context, theme);
			mContext = context;
			mTheme = theme;
		}

		@Override
		public AlertDialog show() {
			throw new IllegalAccessError("The method is not implemented");
		}
	}

	public AlertDialogFragment() {
		super();
	}

	public interface YesNoNeutralListener {
		void onYes(AlertDialog alertDialog);

		void onNo(AlertDialog alertDialog);

		void onNeutral(AlertDialog alertDialog);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof AlertDialogFragment.YesNoNeutralListener)) {
			throw new ClassCastException(activity.toString()
					+ " must implement YesNoListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String calendarName = "";
		if (savedInstanceState != null) {
			calendarName = savedInstanceState.getString("calendarName");
		}
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		mEdittext = new EditText(getActivity());
		mEdittext.setText(calendarName);
		alert.setMessage(getResources().getString(R.string.input_calendar_name));
		alert.setView(mEdittext);
		alert.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newCalendarName = mEdittext.getText().toString();
						((AlertDialogFragment.YesNoNeutralListener) getActivity())
								.onYes(mAlertDialog);
					}
				});
		alert.setNegativeButton(R.string.alert_dialog_cancel,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						((AlertDialogFragment.YesNoNeutralListener) getActivity())
								.onNo(mAlertDialog);
					}
				});
		mAlertDialog = alert.create();
		return alert.create();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString("calendarName", mEdittext.getText().toString());
	}
}