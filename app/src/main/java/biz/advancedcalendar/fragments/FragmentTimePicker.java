package biz.advancedcalendar.fragments;

import java.util.Calendar;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import biz.advancedcalendar.CommonConstants;

public class FragmentTimePicker extends DialogFragment implements
		TimePickerDialog.OnTimeSetListener {
	// Container Activity must implement this interface
	public interface OnTimeSelectedListener {
		public void onTimeSelected(FragmentTimePicker fragmentTimePicker, int hourOfDay,
				int minute, boolean cancelled);
	}

	private OnTimeSelectedListener mListener;
	// private int mCallerId;
	private boolean mCancelled;
	private int mHourOfDay, mMinute;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Bundle b = getArguments();
		if (b != null) {
			try {
				// if (b.containsKey(CommonConstants.CALLER_ID)) {
				// mCallerId = b.getInt(CommonConstants.CALLER_ID);
				// }
				if (b.containsKey(CommonConstants.TAG)) {
					if (activity instanceof AppCompatActivity) {
						Fragment f = ((AppCompatActivity) activity)
								.getSupportFragmentManager().findFragmentByTag(
										b.getString(CommonConstants.TAG));
						mListener = (OnTimeSelectedListener) f;
					} else {
						android.app.Fragment f = activity.getFragmentManager()
								.findFragmentByTag(b.getString(CommonConstants.TAG));
						mListener = (OnTimeSelectedListener) f;
					}
				} else {
					mListener = (OnTimeSelectedListener) activity;
				}
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement FragmentTimePicker.OnTimeSelectedListener");
			}
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the current time as the default values for the picker
		Calendar calendar = Calendar.getInstance();
		int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		Bundle b = getArguments();
		if (b.containsKey(CommonConstants.HOUR_OF_DAY)) {
			hourOfDay = b.getInt(CommonConstants.HOUR_OF_DAY);
		}
		if (b.containsKey(CommonConstants.MINUTE)) {
			minute = b.getInt(CommonConstants.MINUTE);
		}
		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), this, hourOfDay, minute,
				DateFormat.is24HourFormat(getActivity()));
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (mListener != null) {
			mListener.onTimeSelected(this, mHourOfDay, mMinute, mCancelled);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		mCancelled = true;
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the time chosen by the user
		// if (!mDismissed && mListener != null) {
		// mListener.onTimeSelected(this, hourOfDay, minute);
		// }
		mHourOfDay = hourOfDay;
		mMinute = minute;
	}
	// @Override
	// public void onSaveInstanceState(Bundle savedInstanceState) {
	// super.onSaveInstanceState(savedInstanceState);
	// savedInstanceState.putInt(CommonConstants.SAVED_INSTANCE_STATE_HOUR_OF_DAY,
	// mHourOfDay);
	// savedInstanceState.putInt(CommonConstants.SAVED_INSTANCE_STATE_MINUTE, mMinute);
	// savedInstanceState.putBoolean(CommonConstants.SAVED_INSTANCE_STATE_CANCELLED,
	// mCancelled);
	// }
	//
	// @Override
	// public void onCreate(Bundle savedInstanceState) {
	// if (savedInstanceState != null) {
	// mHourOfDay = savedInstanceState
	// .getInt(CommonConstants.SAVED_INSTANCE_STATE_HOUR_OF_DAY);
	// mMinute = savedInstanceState
	// .getInt(CommonConstants.SAVED_INSTANCE_STATE_MINUTE);
	// mCancelled = savedInstanceState
	// .getBoolean(CommonConstants.SAVED_INSTANCE_STATE_CANCELLED);
	// }
	// }
}
