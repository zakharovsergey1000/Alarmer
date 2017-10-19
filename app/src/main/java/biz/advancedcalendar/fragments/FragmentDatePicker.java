package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.DatePicker;
import biz.advancedcalendar.CommonConstants;
import java.util.Calendar;

public class FragmentDatePicker extends DialogFragment implements
		DatePickerDialog.OnDateSetListener {
	// Container Activity must implement this interface
	public interface OnDateSelectedListener {
		public void onDateSelected(FragmentDatePicker fragmentDatePicker, int year,
				int month, int day, boolean cancelled);
	}

	private OnDateSelectedListener mListener;
	// private Integer mCallerId;
	private boolean mCancelled;
	private int mYear, mMonthOfYear, mDayOfMonth;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Bundle b = getArguments();
		if (b != null) {
			try {
				// mCallerId = null;
				// if (b.containsKey(CommonConstants.CALLER_ID)) {
				// mCallerId = b.getInt(CommonConstants.CALLER_ID);
				// }
				if (b.containsKey(CommonConstants.TAG)) {
					if (activity instanceof AppCompatActivity) {
						Fragment f = ((AppCompatActivity) activity)
								.getSupportFragmentManager().findFragmentByTag(
										b.getString(CommonConstants.TAG));
						mListener = (OnDateSelectedListener) f;
					} else {
						android.app.Fragment f = activity.getFragmentManager()
								.findFragmentByTag(b.getString(CommonConstants.TAG));
						mListener = (OnDateSelectedListener) f;
					}
				} else {
					mListener = (OnDateSelectedListener) activity;
				}
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement FragmentDatePicker.OnDateSelectedListener");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current date as the default date in the picker
		final Calendar calendar = Calendar.getInstance();
		int year, monthOfYear, dayOfMonth;
		Bundle b = getArguments();
		if (b.containsKey(CommonConstants.YEAR)) {
			year = b.getInt(CommonConstants.YEAR);
		} else {
			year = calendar.get(Calendar.YEAR);
		}
		if (b.containsKey(CommonConstants.MONTH)) {
			monthOfYear = b.getInt(CommonConstants.MONTH);
		} else {
			monthOfYear = calendar.get(Calendar.MONTH);
		}
		if (b.containsKey(CommonConstants.DAY_OF_MONTH)) {
			dayOfMonth = b.getInt(CommonConstants.DAY_OF_MONTH);
		} else {
			dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		}
		DatePickerDialog dpd = new DatePickerDialog(getActivity(), this, year,
				monthOfYear, dayOfMonth);
		dpd.getDatePicker().setCalendarViewShown(true);
		return dpd;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mListener != null) {
			mListener.onDateSelected(this, mYear, mMonthOfYear, mDayOfMonth, mCancelled);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		mCancelled = true;
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		// Do something with the time chosen by the user
		// if (!mCancelled && mListener != null) {
		// mListener.onDateSelected(this, mYear, mMonthOfYear, mDayOfMonth);
		// }
		mYear = year;
		mMonthOfYear = monthOfYear;
		mDayOfMonth = dayOfMonth;
	}
	// @Override
	// public void onSaveInstanceState(Bundle savedInstanceState) {
	// super.onSaveInstanceState(savedInstanceState);
	// savedInstanceState.putInt(CommonConstants.SAVED_INSTANCE_STATE_YEAR, mYear);
	// savedInstanceState.putInt(CommonConstants.SAVED_INSTANCE_STATE_MONTH,
	// mMonthOfYear);
	// savedInstanceState.putInt(CommonConstants.SAVED_INSTANCE_STATE_DAY_OF_MONTH,
	// mDayOfMonth);
	// savedInstanceState.putBoolean(CommonConstants.SAVED_INSTANCE_STATE_CANCELLED,
	// mCancelled);
	// }
	//
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// if (savedInstanceState != null) {
	// mYear = savedInstanceState.getInt(CommonConstants.SAVED_INSTANCE_STATE_YEAR);
	// mMonthOfYear = savedInstanceState
	// .getInt(CommonConstants.SAVED_INSTANCE_STATE_MONTH);
	// mDayOfMonth = savedInstanceState
	// .getInt(CommonConstants.SAVED_INSTANCE_STATE_DAY_OF_MONTH);
	// mCancelled = savedInstanceState
	// .getBoolean(CommonConstants.SAVED_INSTANCE_STATE_CANCELLED);
	// }
	// }
}
