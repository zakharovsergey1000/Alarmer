package biz.advancedcalendar.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.DaysSelectedListener;
import biz.advancedcalendar.views.MonthView;
import com.android.supportdatetimepicker.date.DatePickerDialog;
import com.android.supportdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// import biz.advancedcalendar.activities.accessories.SupportTabListener.BundleAcceptor;
public class FragmentViewMonth extends Fragment implements OnDateSetListener,
		DaysSelectedListener {
	private DaysSelectedListener mDayClickedListener;
	private MonthView mMonthView = null;
	private ImageButton mButtonPrevious;
	private Button mButtonPickupDate;
	private ImageButton mButtonNext;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("MMMM, yyyy",
			Locale.getDefault());
	private BroadcastReceiver mReceiver;
	private String mPendingBundleForDatePickerDialogKey = "mPendingBundleForDatePickerDialogKey";
	public static final String StateParametersKey = "biz.advancedcalendar.fragments.FragmentViewMonth.StateParametersKey";
	// variables to be saved in onSaveInstanceState()
	private FragmentViewMonthStateParameters retainedData;
	private Bundle mPendingBundleForDatePickerDialog;
	private String mDatePickerDialogKey = "biz.advancedcalendar.fragments.FragmentViewMonth.DatePickerDialogKey";
	private SyncPolicy syncPolicy;
	private MarkSyncNeededPolicy markSyncNeeded;

	// private String mTimePickerDialogKey =
	// "biz.advancedcalendar.fragments.FragmentViewMonth.TimePickerDialog";
	public static class FragmentViewMonthStateParameters implements Parcelable {
		long MonthStartDateTime;
		SyncPolicy SyncPolicy;
		MarkSyncNeededPolicy MarkSyncNeeded;

		public FragmentViewMonthStateParameters(
				long monthStartDateTime,
				biz.advancedcalendar.greendao.Task.SyncPolicy syncPolicy,
				biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy markSyncNeededPolicy) {
			MonthStartDateTime = monthStartDateTime;
			SyncPolicy = syncPolicy;
			MarkSyncNeeded = markSyncNeededPolicy;
		}

		protected FragmentViewMonthStateParameters(Parcel in) {
			MonthStartDateTime = in.readLong();
			SyncPolicy = in.readByte() == 0x00 ? null
					: biz.advancedcalendar.greendao.Task.SyncPolicy
							.fromInt(in.readByte());
			MarkSyncNeeded = in.readByte() == 0x00 ? null : MarkSyncNeededPolicy
					.fromInt(in.readByte());
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeLong(MonthStartDateTime);
			if (SyncPolicy == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(SyncPolicy.getValue());
			}
			if (MarkSyncNeeded == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeByte(MarkSyncNeeded.getValue());
			}
		}

		public static final Parcelable.Creator<FragmentViewMonthStateParameters> CREATOR = new Parcelable.Creator<FragmentViewMonthStateParameters>() {
			@Override
			public FragmentViewMonthStateParameters createFromParcel(Parcel in) {
				return new FragmentViewMonthStateParameters(in);
			}

			@Override
			public FragmentViewMonthStateParameters[] newArray(int size) {
				return new FragmentViewMonthStateParameters[size];
			}
		};
	}

	public enum DATE_PICKER_DATE {
		LAST_BROWSING((short) CommonConstants.DATE_PICKER_LAST_BROWSING_DATE), TODAY(
				(short) CommonConstants.DATE_PICKER_TODAY_DATE);
		private short value;

		private DATE_PICKER_DATE(short value) {
			this.value = value;
		}

		/** @return the value */
		public short getValue() {
			return value;
		}

		private static Map<Short, DATE_PICKER_DATE> map = new HashMap<Short, DATE_PICKER_DATE>();
		static {
			for (DATE_PICKER_DATE datePickerDate : DATE_PICKER_DATE.values()) {
				DATE_PICKER_DATE.map.put(datePickerDate.value, datePickerDate);
			}
		}

		public static DATE_PICKER_DATE fromInt(short calendarSelectedDate) {
			DATE_PICKER_DATE datePickerDate = DATE_PICKER_DATE.map
					.get(calendarSelectedDate);
			return datePickerDate;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		restoreState(savedInstanceState);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
					// if (!isDetached()) {
					if (isAdded()) {
						if (context == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						Long[] selectedCalendars = Helper
								.getLongArray(
										context,
										getResources()
												.getString(
														R.string.preference_key_selected_calendars),
										null);
						List<Task> tasks = DataProvider.getActiveNotDeletedTasks(null,
								context.getApplicationContext(), selectedCalendars);
						mMonthView.setTasks(tasks);
					}
				} else if (intent.getAction().equals(
						CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED)) {
					if (isAdded()) {
						mMonthView.setStartMonth(mMonthView.getStartMonth());
						retainedData.MonthStartDateTime = mMonthView.getStartMonth();
					}
				} else if (intent.getAction().equals(
						CommonConstants.ACTION_SYNC_POLICY_CHANGED)) {
					if (isAdded()) {
						syncPolicy = SyncPolicy.fromInt((byte) Helper
								.getIntegerPreferenceValueFromStringArray(context,
										R.string.preference_key_sync_policy,
										R.array.sync_policy_values_array,
										R.integer.sync_policy_default_value));
						retainedData.SyncPolicy = syncPolicy;
					}
				} else if (intent.getAction().equals(
						CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED)) {
					if (isAdded()) {
						markSyncNeeded = MarkSyncNeededPolicy.fromInt((byte) Helper
								.getIntegerPreferenceValueFromStringArray(context,
										R.string.preference_key_mark_sync_needed,
										R.array.mark_sync_needed_values_array,
										R.integer.mark_sync_needed_default_value));
						retainedData.MarkSyncNeeded = markSyncNeeded;
					}
				} else if (intent.getAction().equals("android.intent.action.TIME_SET")) {
					if (isAdded()) {
						if (Build.VERSION.SDK_INT >= 11) {
							getActivity().invalidateOptionsMenu();
						}
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		restoreState(savedInstanceState);
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_month_view, container, false);
		mMonthView = (MonthView) v.findViewById(R.id.fragment_month_view);
		// mMonthView.setDaysSelectedListener(this);
		Long[] selectedCalendars = Helper.getLongArray(getActivity(), getResources()
				.getString(R.string.preference_key_selected_calendars), null);
		List<Task> tasks = DataProvider.getActiveNotDeletedTasks(null, getActivity()
				.getApplicationContext(), selectedCalendars);
		mMonthView.setTasks(tasks);
		mButtonPrevious = (ImageButton) v
				.findViewById(R.id.fragment_month_view_button_previous);
		mButtonPrevious.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(mMonthView.getStartMonth());
				calendar.add(Calendar.MONTH, -1);
				mMonthView.setStartMonth(calendar.getTimeInMillis());
				retainedData.MonthStartDateTime = mMonthView.getStartMonth();
				updateDateText();
			}
		});
		mButtonPickupDate = (Button) v
				.findViewById(R.id.fragment_month_view_button_pickup_date);
		mButtonPickupDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DATE_PICKER_DATE datePickerDate = DATE_PICKER_DATE.fromInt((short) Helper
						.getIntegerPreferenceValueFromStringArray(
								getActivity(),
								R.string.preference_key_date_picker_default_selected_date,
								R.array.date_picker_selected_date_values_array,
								R.integer.date_picker_default_selected_date));
				Calendar calendar = Calendar.getInstance();
				switch (datePickerDate) {
				case LAST_BROWSING:
				default:
					calendar.setTimeInMillis(mMonthView.getStartMonth());
					break;
				case TODAY:
					break;
				}
				mPendingBundleForDatePickerDialog = new Bundle();
				mPendingBundleForDatePickerDialog.putInt(CommonConstants.CALLER_ID,
						R.id.fragment_month_view_button_pickup_date);
				DatePickerDialog dpd = DatePickerDialog.newInstance(
						FragmentViewMonth.this, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				dpd.show(getFragmentManager(), mDatePickerDialogKey);
			}
		});
		mButtonNext = (ImageButton) v.findViewById(R.id.fragment_month_view_button_next);
		mButtonNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(mMonthView.getStartMonth());
				calendar.add(Calendar.MONTH, 1);
				mMonthView.setStartMonth(calendar.getTimeInMillis());
				retainedData.MonthStartDateTime = mMonthView.getStartMonth();
				updateDateText();
			}
		});
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getFragmentManager();
		restoreState(savedInstanceState);
		syncPolicy = retainedData.SyncPolicy;
		markSyncNeeded = retainedData.MarkSyncNeeded;
		setHasOptionsMenu(true);
		FragmentActivity activity = getActivity();
		mDayClickedListener = (DaysSelectedListener) activity;
		mMonthView.setDaysSelectedListener(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS);
		intentFilter.addAction(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_MONTH_RECURRENCE_MODE_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_START_TIME_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_START_TIME_REQUIRED_ACTION_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_SYNC_POLICY_CHANGED);
		intentFilter.addAction(CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED);
		LocalBroadcastManager.getInstance(activity.getApplicationContext())
				.registerReceiver(mReceiver, intentFilter);
		mMonthView.setStartMonth(retainedData.MonthStartDateTime);
		updateDateText();
		DatePickerDialog dpd = (DatePickerDialog) fm
				.findFragmentByTag(mDatePickerDialogKey);
		if (dpd != null) {
			dpd.setOnDateSetListener(this);
		}
	}

	private void restoreState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			retainedData = savedInstanceState
					.getParcelable(FragmentViewMonth.StateParametersKey);
			mPendingBundleForDatePickerDialog = (Bundle) savedInstanceState
					.getParcelable(mPendingBundleForDatePickerDialogKey);
		} else if (retainedData == null) {
			retainedData = getArguments().getParcelable(
					FragmentViewMonth.StateParametersKey);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelable(FragmentViewMonth.StateParametersKey,
				retainedData);
		savedInstanceState.putParcelable(mPendingBundleForDatePickerDialogKey,
				mPendingBundleForDatePickerDialog);
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_GOTO_TODAY,
				Menu.FIRST + 100, getResources().getString(R.string.action_goto_today));
		int iconRes;
		Calendar calendar = Calendar.getInstance();
		switch (calendar.get(Calendar.DAY_OF_MONTH)) {
		case 1:
			iconRes = R.drawable.calendar_1_24dp;
			break;
		case 2:
			iconRes = R.drawable.calendar_2_24dp;
			break;
		case 3:
			iconRes = R.drawable.calendar_3_24dp;
			break;
		case 4:
			iconRes = R.drawable.calendar_4_24dp;
			break;
		case 5:
			iconRes = R.drawable.calendar_5_24dp;
			break;
		case 6:
			iconRes = R.drawable.calendar_6_24dp;
			break;
		case 7:
			iconRes = R.drawable.calendar_7_24dp;
			break;
		case 8:
			iconRes = R.drawable.calendar_8_24dp;
			break;
		case 9:
			iconRes = R.drawable.calendar_9_24dp;
			break;
		case 10:
			iconRes = R.drawable.calendar_10_24dp;
			break;
		case 11:
			iconRes = R.drawable.calendar_11_24dp;
			break;
		case 12:
			iconRes = R.drawable.calendar_12_24dp;
			break;
		case 13:
			iconRes = R.drawable.calendar_13_24dp;
			break;
		case 14:
			iconRes = R.drawable.calendar_14_24dp;
			break;
		case 15:
			iconRes = R.drawable.calendar_15_24dp;
			break;
		case 16:
			iconRes = R.drawable.calendar_16_24dp;
			break;
		case 17:
			iconRes = R.drawable.calendar_17_24dp;
			break;
		case 18:
			iconRes = R.drawable.calendar_18_24dp;
			break;
		case 19:
			iconRes = R.drawable.calendar_19_24dp;
			break;
		case 20:
			iconRes = R.drawable.calendar_20_24dp;
			break;
		case 21:
			iconRes = R.drawable.calendar_21_24dp;
			break;
		case 22:
			iconRes = R.drawable.calendar_22_24dp;
			break;
		case 23:
			iconRes = R.drawable.calendar_23_24dp;
			break;
		case 24:
			iconRes = R.drawable.calendar_24_24dp;
			break;
		case 25:
			iconRes = R.drawable.calendar_25_24dp;
			break;
		case 26:
			iconRes = R.drawable.calendar_26_24dp;
			break;
		case 27:
			iconRes = R.drawable.calendar_27_24dp;
			break;
		case 28:
			iconRes = R.drawable.calendar_28_24dp;
			break;
		case 29:
			iconRes = R.drawable.calendar_29_24dp;
			break;
		case 30:
			iconRes = R.drawable.calendar_30_24dp;
			break;
		case 31:
			iconRes = R.drawable.calendar_31_24dp;
			break;
		default:
			iconRes = R.drawable.calendar_1_24dp;
			break;
		}
		menuItem.setIcon(iconRes);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case CommonConstants.MENU_ID_GOTO_TODAY:
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			mMonthView.setStartMonth(calendar.getTimeInMillis());
			retainedData.MonthStartDateTime = mMonthView.getStartMonth();
			updateDateText();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(mReceiver);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long monthStartDateTime = calendar.getTimeInMillis();
		mMonthView.setStartMonth(monthStartDateTime);
		retainedData.MonthStartDateTime = mMonthView.getStartMonth();
		updateDateText();
	}

	public void updateDateText() {
		String date = mDateFormat.format(new Date(mMonthView.getStartMonth()));
		mButtonPickupDate.setText(date);
	}

	@Override
	public void onDaysSelected(long firstDay, long lastDay) {
		if (mDayClickedListener != null) {
			mDayClickedListener.onDaysSelected(firstDay, lastDay);
		}
	}
}
