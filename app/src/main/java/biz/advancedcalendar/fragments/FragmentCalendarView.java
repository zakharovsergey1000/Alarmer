package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;

public class FragmentCalendarView extends DialogFragment {
	// Container Activity must implement this interface
	public interface OnDateSelectedListener {
		public void onDateSelected(FragmentCalendarView fragmentDatePicker, int year,
				int month, int day);
	}

	// private int mYear, mMonthOfYear, mDayOfMonth;
	// private OnDateSelectedListener mListener;
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Bundle b = getArguments();
		if (b != null) {
			try {
				if (b.containsKey(CommonConstants.CALLER_ID)) {
					b.getInt(CommonConstants.CALLER_ID);
				}
				if (b.containsKey(CommonConstants.TAG)) {
					if (activity instanceof AppCompatActivity) {
						// Fragment f = ((AppCompatActivity) activity)
						// .getSupportFragmentManager().findFragmentByTag(
						// b.getString(CommonConstants.TAG));
						// mListener = (OnDateSelectedListener) f;
					} else {
						// android.app.Fragment f = activity.getFragmentManager()
						// .findFragmentByTag(b.getString(CommonConstants.TAG));
						// mListener = (OnDateSelectedListener) f;
					}
				} else {
					// mListener = (OnDateSelectedListener) activity;
				}
			} catch (ClassCastException e) {
				throw new ClassCastException(activity.toString()
						+ " must implement FragmentDatePicker.OnDateSelectedListener");
			}
		}
	}

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	// View view = inflater.inflate(R.layout.fragment_calendar_view, container);
	// CalendarView cal = (CalendarView) view
	// .findViewById(R.id.fragment_calendar_view_calendar);
	// cal.setOnDateChangeListener(new OnDateChangeListener() {
	// @Override
	// public void onSelectedDayChange(CalendarView view, int year, int month,
	// int dayOfMonth) {
	// mYear = year;
	// mMonthOfYear = month;
	// mDayOfMonth = dayOfMonth;
	// Toast.makeText(
	// getActivity(),
	// "Selected Date is\n\n" + dayOfMonth + " : " + month + " : "
	// + year, Toast.LENGTH_SHORT).show();
	// }
	// });
	// Button Button = (Button) view
	// .findViewById(R.id.fragment_calendar_view_done_button);
	// Button.setOnClickListener(new OnClickListener() {
	// @Override
	// public void onClick(View v) {
	// if (mListener != null) {
	// mListener.onDateSelected(FragmentCalendarView.this, mYear,
	// mMonthOfYear, mDayOfMonth);
	// }
	// }
	// });
	// return view;
	// }
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// final Calendar c = Calendar.getInstance();
		// Use the current date as the default date in the picker
		// mYear = c.get(Calendar.YEAR);
		// mMonthOfYear = c.get(Calendar.MONTH);
		// mDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
		Bundle b = getArguments();
		if (b.containsKey(CommonConstants.YEAR)) {
			// mYear = b.getInt(CommonConstants.YEAR);
		}
		if (b.containsKey(CommonConstants.MONTH)) {
			// mMonthOfYear = b.getInt(CommonConstants.MONTH);
		}
		if (b.containsKey(CommonConstants.DAY_OF_MONTH)) {
			// mDayOfMonth = b.getInt(CommonConstants.DAY_OF_MONTH);
		}
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.calendar_view, null);
		Dialog dateDialog = new AlertDialog.Builder(getActivity())
				// .setTitle("Date")
				.setView(view)
				.setPositiveButton("Set Date", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// GregorianCalendar gCal = new GregorianCalendar();
						// gCal.add(Calendar.DATE, -1);
						// long lMillis = gCal.getTime().getTime();
						// if(taskDateLong != null && taskDateLong < lMillis){
						// fieldTaskDate.setHint("Date cannot be in the past.");
						// fieldTaskDate.setText("");
						// } else {
						// fieldTaskDate.setHint(getString(R.string.textfield_hint_date));
						// fieldTaskDate.setText(taskDate);
						// }
					}
				})
				// .setNeutralButton("Remove", new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int whichButton) {
				// // fieldTaskDate.setText("");
				// }
				// })
				// .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				// @Override
				// public void onClick(DialogInterface dialog, int whichButton) {
				// // Don't do anything.
				// }
				// })
				.create();
		dateDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dateDialog;
		// Dialog dialog = super.onCreateDialog(savedInstanceState);
		// // request a window without the title
		// //dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		// return dialog;
	}
}
