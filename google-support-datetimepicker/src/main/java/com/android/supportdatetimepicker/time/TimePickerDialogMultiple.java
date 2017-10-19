/* Copyright (C) 2013 The Android Open Source Project Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the
 * License */
package com.android.supportdatetimepicker.time;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.android.supportdatetimepicker.HapticFeedbackController;
import com.android.supportdatetimepicker.R;
import com.android.supportdatetimepicker.Utils;
import com.android.supportdatetimepicker.time.RadialPickerLayout.OnValueSelectedListener;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;

/** Dialog to set a time. */
public class TimePickerDialogMultiple extends DialogFragment implements
		OnValueSelectedListener {
	private static final String TAG = "TimePickerDialog";
	// private static final String KEY_IS_TIMESPAN = "is_time_span";
	private static final String KEY_CALLER_ID = "KEY_CALLER_ID";
	private static final String KEY_SHOW_TIME_SELECTOR = "KEY_SHOW_TIME_SELECTOR";
	private static final String KEY_TITLE_ID = "KEY_TITLE_ID";
	private static final String KEY_ORDINAL_NUMBER = "KEY_ORDINAL_NUMBER";
	private static final String KEY_PREVIOUS_RADIO_BUTTON_ID = "KEY_PREVIOUS_RADIO_BUTTON_ID";
	private static final String KEY_TIME_ATTRIBUTES = "KEY_TIME_ATTRIBUTES";
	private static final String KEY_HOUR_OF_DAY = "hour_of_day";
	private static final String KEY_MINUTE = "minute";
	private static final String KEY_IS_24_HOUR_VIEW_INITIAL = "is_24_hour_view_initial";
	private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
	private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
	private static final String KEY_AM_OR_PM = "KEY_AM_OR_PM";
	private static final String KEY_IN_KB_MODE = "in_kb_mode";
	private static final String KEY_TYPED_TIMES = "typed_times";
	private static final String KEY_DARK_THEME = "dark_theme";
	public static final int HOUR_INDEX = 0;
	public static final int MINUTE_INDEX = 1;
	// NOT a real index for the purpose of what's showing.
	public static final int AMPM_INDEX = 2;
	// Also NOT a real index, just used for keyboard mode.
	public static final int ENABLE_PICKER_INDEX = 3;
	public static final int AM = 0;
	public static final int PM = 1;
	// Delay before starting the pulse animation, in ms.
	private static final int PULSE_ANIMATOR_DELAY = 300;
	private OnMultipleTimeSetListener mCallback;
	private AmPmHitspaceClickListener mAmPmHitspaceClickListener;
	private HapticFeedbackController mHapticFeedbackController;
	private LinearLayout mLinearlayoutAlarmSnoozeModeSelector;
	private RadioGroup mRadioGroupTime;
	private TextView mDoneButton;
	private TextView mHourView;
	private TextView mHourSpaceView;
	private TextView mMinuteView;
	private TextView mMinuteSpaceView;
	private TextView mAmPmTextView;
	private View mAmPmHitspace;
	private RadialPickerLayout mTimePicker12;
	private RadialPickerLayout mTimePicker24;
	private RadialPickerLayout mTimePicker;
	private int mSelectedColor;
	private int mUnselectedColor;
	private String mAmText;
	private String mPmText;
	private boolean mAllowAutoAdvance;
	// private boolean mIsTimeSpan;
	private Bundle mBundle;
	private int mTitleId;
	private ArrayList<TimeAttribute> mTimeAttributes;
	private int mOrdinalNumber;
	private int index;
	private int mCurrentItemShowing;
	private int mAmOrPm;
	private int mPreviousRadioButtonId;
	private boolean mShowTimeSelector;
	private int mInitialHourOfDay;
	private int mInitialMinute;
	private boolean mIs24HourMode;
	private boolean mIs24HourModeInitial;
	private boolean mThemeDark;
	// For hardware IME input.
	private char mPlaceholderText;
	private String mDoublePlaceholderText;
	private String mDeletedKeyFormat;
	private boolean mInKbMode;
	private ArrayList<Integer> mTypedTimes;
	private Node mLegalTimesTree;
	private int mAmKeyCode;
	private int mPmKeyCode;
	// Accessibility strings.
	private String mHourPickerDescription;
	private String mSelectHours;
	private String mMinutePickerDescription;
	private String mSelectMinutes;
	private View view;

	/** The callback interface used to indicate the user is done filling in the time (they
	 * clicked on the 'Set' button). */
	public interface OnMultipleTimeSetListener {
		/** @param view
		 *            The view associated with this listener.
		 * @param bundle TODO
		 * @param timeAttributes
		 *            TODO
		 * @param ordinalNumber
		 *            TODO */
		void onTimeSet(RadialPickerLayout view, Bundle bundle,
				ArrayList<TimeAttribute> timeAttributes, int ordinalNumber);

		boolean isTimeConsistent(ArrayList<TimeAttribute> timeAttributes, Bundle bundle);
	}

	class AmPmHitspaceClickListener implements OnClickListener, Parcelable {
		@Override
		public void onClick(View v) {
			tryVibrate();
			TimeAttribute timeAttribute = mTimeAttributes.get(index);
			if (mAmOrPm == TimePickerDialogMultiple.AM) {
				mAmOrPm = TimePickerDialogMultiple.PM;
				timeAttribute.amOrPm = mAmOrPm;
			} else if (mAmOrPm == TimePickerDialogMultiple.PM) {
				mAmOrPm = TimePickerDialogMultiple.AM;
				timeAttribute.amOrPm = mAmOrPm;
			}
			updateAmPmDisplay(mAmOrPm);
			mTimePicker.setAmOrPm(mAmOrPm);
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
		}
	}

	public static class TimeAttribute implements Parcelable, Comparable<TimeAttribute> {
		private int hours;
		private int minutes;
		private boolean is24HourMode;
		private boolean isHourModeChangeable;
		private int amOrPm;
		private int textId;
		private int ordinalNumber;
		private String keyTimeHours;
		private String keyTimeMinutes;
		private RadioButton radioButton;
		private RadialPickerLayout timePicker;
		private int currentItemShowing;

		public TimeAttribute(int ordinalNumber) {
			this.ordinalNumber = ordinalNumber;
		}

		public TimeAttribute(int hours, int minutes, int textId, int ordinalNumber,
				boolean is24HourMode, boolean isHourModeChangeable) {
			this.hours = hours;
			this.is24HourMode = is24HourMode;
			this.isHourModeChangeable = isHourModeChangeable;
			amOrPm = hours < 12 ? TimePickerDialogMultiple.AM
					: TimePickerDialogMultiple.PM;
			this.minutes = minutes;
			this.textId = textId;
			this.ordinalNumber = ordinalNumber;
			keyTimeHours = "keyTimeHours" + ordinalNumber;
			keyTimeMinutes = "keyTimeMinutes" + ordinalNumber;
			currentItemShowing = TimePickerDialogMultiple.HOUR_INDEX;
		}

		@Override
		public int compareTo(TimeAttribute another) {
			if (ordinalNumber < another.ordinalNumber) {
				return -1;
			} else if (ordinalNumber > another.ordinalNumber) {
				return 1;
			} else {
				return 0;
			}
		}

		public int getHours() {
			return hours;
		}

		public void setHours(int hours) {
			this.hours = hours;
		}

		public int getMinutes() {
			return minutes;
		}

		public void setMinutes(int minutes) {
			this.minutes = minutes;
		}

		public int getAmOrPm() {
			return amOrPm;
		}

		public void setAmOrPm(int amOrPm) {
			this.amOrPm = amOrPm;
		}

		public RadioButton getRadioButton() {
			return radioButton;
		}

		public void setRadioButton(RadioButton radioButton) {
			this.radioButton = radioButton;
		}

		public int getOrdinalNumber() {
			return ordinalNumber;
		}

		public String getKeyTimeHours() {
			return keyTimeHours;
		}

		public String getKeyTimeMinutes() {
			return keyTimeMinutes;
		}

		public boolean getIs24HourMode() {
			return is24HourMode;
		}

		public void setIs24HourMode(boolean is24HourMode) {
			if (isHourModeChangeable) {
				this.is24HourMode = is24HourMode;
			}
		}

		protected TimeAttribute(Parcel in) {
			hours = in.readInt();
			minutes = in.readInt();
			is24HourMode = in.readByte() != 0x00;
			isHourModeChangeable = in.readByte() != 0x00;
			amOrPm = in.readInt();
			textId = in.readInt();
			ordinalNumber = in.readInt();
			keyTimeHours = in.readString();
			keyTimeMinutes = in.readString();
			currentItemShowing = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(hours);
			dest.writeInt(minutes);
			dest.writeByte((byte) (is24HourMode ? 0x01 : 0x00));
			dest.writeByte((byte) (isHourModeChangeable ? 0x01 : 0x00));
			dest.writeInt(amOrPm);
			dest.writeInt(textId);
			dest.writeInt(ordinalNumber);
			dest.writeString(keyTimeHours);
			dest.writeString(keyTimeMinutes);
			dest.writeInt(currentItemShowing);
		}

		public static final Parcelable.Creator<TimeAttribute> CREATOR = new Parcelable.Creator<TimeAttribute>() {
			@Override
			public TimeAttribute createFromParcel(Parcel in) {
				return new TimeAttribute(in);
			}

			@Override
			public TimeAttribute[] newArray(int size) {
				return new TimeAttribute[size];
			}
		};
	}

	public TimePickerDialogMultiple() {
		// Empty constructor required for dialog fragment.
	}

	public static TimePickerDialogMultiple newInstance(
			OnMultipleTimeSetListener callback, Bundle bundle, int titleId,
			ArrayList<TimeAttribute> timeAttributes, int ordinalNumber) {
		TimePickerDialogMultiple timePicker = new TimePickerDialogMultiple();
		timePicker.initialize(callback, bundle, titleId, timeAttributes, ordinalNumber,
				true);
		return timePicker;
	}

	private void initialize(OnMultipleTimeSetListener callback, Bundle bundle,
			int titleId, ArrayList<TimeAttribute> timeAttributes, int ordinalNumber,
			boolean showTimeSelector) {
		mCallback = callback;
		mBundle = bundle;
		mTitleId = titleId;
		mTimeAttributes = timeAttributes;
		mOrdinalNumber = ordinalNumber;
		TimeAttribute timeAttribute = new TimeAttribute(ordinalNumber);
		Collections.sort(mTimeAttributes);
		index = Collections.binarySearch(mTimeAttributes, timeAttribute);
		timeAttribute = mTimeAttributes.get(index);
		mIs24HourModeInitial = timeAttribute.is24HourMode;
		mIs24HourMode = timeAttribute.is24HourMode;
		mInitialHourOfDay = timeAttribute.hours;
		mInitialMinute = timeAttribute.minutes;
		mInKbMode = false;
		mThemeDark = false;
		mShowTimeSelector = showTimeSelector;
		mCurrentItemShowing = timeAttribute.currentItemShowing;
		mAmOrPm = timeAttribute.amOrPm;
	}

	public Bundle getBundle() {
		return mBundle;
	}

	/** Set a dark or light theme. NOTE: this will only take effect for the next
	 * onCreateView. */
	public void setThemeDark(boolean dark) {
		mThemeDark = dark;
	}

	public boolean isThemeDark() {
		return mThemeDark;
	}

	public void setOnTimeSetListener(OnMultipleTimeSetListener callback) {
		mCallback = callback;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState
						.containsKey(TimePickerDialogMultiple.KEY_HOUR_OF_DAY)
				&& savedInstanceState.containsKey(TimePickerDialogMultiple.KEY_MINUTE)
				&& savedInstanceState
						.containsKey(TimePickerDialogMultiple.KEY_IS_24_HOUR_VIEW)) {
			// mIsTimeSpan = savedInstanceState
			// .getBoolean(TimePickerDialog2.KEY_IS_TIMESPAN);
			mBundle = savedInstanceState
					.getBundle(TimePickerDialogMultiple.KEY_CALLER_ID);
			mShowTimeSelector = savedInstanceState
					.getBoolean(TimePickerDialogMultiple.KEY_SHOW_TIME_SELECTOR);
			mTitleId = savedInstanceState.getInt(TimePickerDialogMultiple.KEY_TITLE_ID);
			mOrdinalNumber = savedInstanceState
					.getInt(TimePickerDialogMultiple.KEY_ORDINAL_NUMBER);
			mPreviousRadioButtonId = savedInstanceState
					.getInt(TimePickerDialogMultiple.KEY_PREVIOUS_RADIO_BUTTON_ID);
			mTimeAttributes = savedInstanceState
					.getParcelableArrayList(TimePickerDialogMultiple.KEY_TIME_ATTRIBUTES);
			mInitialHourOfDay = savedInstanceState
					.getInt(TimePickerDialogMultiple.KEY_HOUR_OF_DAY);
			mInitialMinute = savedInstanceState
					.getInt(TimePickerDialogMultiple.KEY_MINUTE);
			mIs24HourModeInitial = savedInstanceState
					.getBoolean(TimePickerDialogMultiple.KEY_IS_24_HOUR_VIEW_INITIAL);
			mIs24HourMode = savedInstanceState
					.getBoolean(TimePickerDialogMultiple.KEY_IS_24_HOUR_VIEW);
			mInKbMode = savedInstanceState
					.getBoolean(TimePickerDialogMultiple.KEY_IN_KB_MODE);
			mThemeDark = savedInstanceState
					.getBoolean(TimePickerDialogMultiple.KEY_DARK_THEME);
			mCurrentItemShowing = savedInstanceState
					.getInt(TimePickerDialogMultiple.KEY_CURRENT_ITEM_SHOWING);
			mAmOrPm = savedInstanceState.getInt(TimePickerDialogMultiple.KEY_AM_OR_PM);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		view = inflater.inflate(R.layout.support_time_picker_multiple, container, false);
		TextView textViewTitle = (TextView) view
				.findViewById(R.id.time_picker_textview_title);
		textViewTitle.setText(mTitleId);
		KeyboardListener keyboardListener = new KeyboardListener();
		view.findViewById(R.id.time_picker_dialog).setOnKeyListener(keyboardListener);
		Resources res = getResources();
		mHourPickerDescription = res.getString(R.string.hour_picker_description);
		mSelectHours = res.getString(R.string.select_hours);
		mMinutePickerDescription = res.getString(R.string.minute_picker_description);
		mSelectMinutes = res.getString(R.string.select_minutes);
		mSelectedColor = res.getColor(mThemeDark ? R.color.red : R.color.blue);
		mUnselectedColor = res.getColor(mThemeDark ? R.color.white
				: R.color.numbers_text_color);
		mHourView = (TextView) view.findViewById(R.id.hours);
		mHourView.setOnKeyListener(keyboardListener);
		mHourSpaceView = (TextView) view.findViewById(R.id.hour_space);
		mMinuteSpaceView = (TextView) view.findViewById(R.id.minutes_space);
		mMinuteView = (TextView) view.findViewById(R.id.minutes);
		mMinuteView.setOnKeyListener(keyboardListener);
		mAmPmTextView = (TextView) view.findViewById(R.id.ampm_label);
		mAmPmTextView.setOnKeyListener(keyboardListener);
		String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
		mAmText = amPmTexts[Calendar.AM];
		mPmText = amPmTexts[Calendar.PM];
		mHapticFeedbackController = new HapticFeedbackController(getActivity());
		mTimePicker12 = (RadialPickerLayout) view.findViewById(R.id.time_picker_12);
		mTimePicker12.setVisibility(View.GONE);
		mTimePicker12.setOnValueSelectedListener(this);
		mTimePicker12.setOnKeyListener(keyboardListener);
		mTimePicker12.initialize(getActivity(), mHapticFeedbackController,
				mInitialHourOfDay, mInitialMinute, false);
		mTimePicker24 = (RadialPickerLayout) view.findViewById(R.id.time_picker_24);
		mTimePicker24.setVisibility(View.GONE);
		mTimePicker24.setOnValueSelectedListener(this);
		mTimePicker24.setOnKeyListener(keyboardListener);
		mTimePicker24.initialize(getActivity(), mHapticFeedbackController,
				mInitialHourOfDay, mInitialMinute, true);
		mRadioGroupTime = (RadioGroup) view.findViewById(R.id.radiogroup_time_selector);
		int size = mTimeAttributes.size();
		if (size < 2) {
			mRadioGroupTime.setVisibility(View.GONE);
		}
		for (int i = 0; i < size; i++) {
			TimeAttribute timeAttribute = mTimeAttributes.get(i);
			if (timeAttribute.is24HourMode) {
				timeAttribute.timePicker = mTimePicker24;
			} else {
				timeAttribute.timePicker = mTimePicker12;
			}
			RadioButton radioButton = (RadioButton) inflater.inflate(
					R.layout.support_time_selection_radiobutton, mRadioGroupTime, false);
			radioButton.setText(timeAttribute.textId);
			radioButton.setId(timeAttribute.ordinalNumber);
			mRadioGroupTime.addView(radioButton, i);
			if (mOrdinalNumber == timeAttribute.ordinalNumber) {
				index = i;
				mTimePicker = timeAttribute.timePicker;
				mTimePicker.setVisibility(View.VISIBLE);
				radioButton.setChecked(true);
				mPreviousRadioButtonId = radioButton.getId();
			}
			timeAttribute.setRadioButton(radioButton);
			radioButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						TimeAttribute timeAttribute;
						timeAttribute = new TimeAttribute(mPreviousRadioButtonId);
						index = Collections.binarySearch(mTimeAttributes, timeAttribute);
						timeAttribute = mTimeAttributes.get(index);
						mTimePicker.setVisibility(View.GONE);
						timeAttribute.currentItemShowing = mCurrentItemShowing;
						timeAttribute.hours = mTimePicker.getHours();
						timeAttribute.minutes = mTimePicker.getMinutes();
						timeAttribute.amOrPm = mAmOrPm;
						mPreviousRadioButtonId = buttonView.getId();
						timeAttribute = new TimeAttribute(mPreviousRadioButtonId);
						index = Collections.binarySearch(mTimeAttributes, timeAttribute);
						timeAttribute = mTimeAttributes.get(index);
						mTimePicker = timeAttribute.timePicker;
						mTimePicker.setVisibility(View.VISIBLE);
						mOrdinalNumber = timeAttribute.ordinalNumber;
						mIs24HourMode = timeAttribute.is24HourMode;
						mTimePicker.setTime(timeAttribute.hours, timeAttribute.minutes);
						setMinute(timeAttribute.minutes);
						setHour(timeAttribute.hours, true);
						mCurrentItemShowing = timeAttribute.currentItemShowing;
						setCurrentItemShowing(mCurrentItemShowing, true, false, true);
						mAmOrPm = timeAttribute.amOrPm;
						updateAmPmDisplay(mAmOrPm);
						mTimePicker.setAmOrPm(mAmOrPm);
						if (mIs24HourMode) {
							mAmPmTextView.setVisibility(View.GONE);
							mAmPmHitspace.setVisibility(View.GONE);
						} else {
							mAmPmTextView.setVisibility(View.VISIBLE);
							mAmPmHitspace.setVisibility(View.VISIBLE);
							updateAmPmDisplay(mAmOrPm);
						}
						tryVibrate();
					}
				}
			});
		}
		setCurrentItemShowing(mCurrentItemShowing, false, true, true);
		mLinearlayoutAlarmSnoozeModeSelector = (LinearLayout) view
				.findViewById(R.id.linearlayout_time_selector);
		if (mShowTimeSelector) {
			mLinearlayoutAlarmSnoozeModeSelector.setVisibility(View.VISIBLE);
		} else {
			mLinearlayoutAlarmSnoozeModeSelector.setVisibility(View.GONE);
		}
		mTimePicker.invalidate();
		mHourView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentItemShowing = TimePickerDialogMultiple.HOUR_INDEX;
				setCurrentItemShowing(mCurrentItemShowing, true, false, true);
				tryVibrate();
			}
		});
		mMinuteView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCurrentItemShowing = TimePickerDialogMultiple.MINUTE_INDEX;
				setCurrentItemShowing(mCurrentItemShowing, true, false, true);
				tryVibrate();
			}
		});
		mDoneButton = (TextView) view.findViewById(R.id.done_button);
		mDoneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mInKbMode && isTypedTimeFullyLegal()) {
					finishKbMode(false);
				} else {
					tryVibrate();
				}
				TimeAttribute timeAttribute = mTimeAttributes.get(index);
				timeAttribute.setHours(mTimePicker.getHours());
				timeAttribute.setMinutes(mTimePicker.getMinutes());
				timeAttribute.amOrPm = mAmOrPm;
				int size = mTimeAttributes.size();
				if (!mIs24HourMode) {
					for (int i = 0; i < size; i++) {
						timeAttribute = mTimeAttributes.get(i);
						if (timeAttribute.amOrPm == TimePickerDialogMultiple.AM) {
							timeAttribute.hours = timeAttribute.hours % 12;
						} else if (timeAttribute.amOrPm == TimePickerDialogMultiple.PM) {
							timeAttribute.hours = timeAttribute.hours % 12 + 12;
						}
					}
				}
				if (mCallback != null) {
					if (mCallback.isTimeConsistent(mTimeAttributes, mBundle)) {
						for (int i = 0; i < size; i++) {
							timeAttribute = mTimeAttributes.get(i);
							timeAttribute.currentItemShowing = TimePickerDialogMultiple.HOUR_INDEX;
						}
						mCallback.onTimeSet(mTimePicker, mBundle, mTimeAttributes,
								mOrdinalNumber);
						dismiss();
					}
				} else {
					dismiss();
				}
			}
		});
		mDoneButton.setOnKeyListener(keyboardListener);
		mAmPmHitspaceClickListener = new AmPmHitspaceClickListener();
		mAmPmHitspace = view.findViewById(R.id.ampm_hitspace);
		mAmPmHitspace.setOnClickListener(mAmPmHitspaceClickListener);
		updateAmPmDisplay(mAmOrPm);
		// Enable or disable the AM/PM view.
		if (mIs24HourMode) {
			mAmPmTextView.setVisibility(View.GONE);
			mAmPmHitspace.setVisibility(View.GONE);
		} else {
			mAmPmTextView.setVisibility(View.VISIBLE);
			mAmPmHitspace.setVisibility(View.VISIBLE);
		}
		mAllowAutoAdvance = true;
		setHour(mInitialHourOfDay, true);
		setMinute(mInitialMinute);
		// Set up for keyboard mode.
		mDoublePlaceholderText = res.getString(R.string.time_placeholder);
		mDeletedKeyFormat = res.getString(R.string.deleted_key);
		mPlaceholderText = mDoublePlaceholderText.charAt(0);
		mAmKeyCode = mPmKeyCode = -1;
		generateLegalTimesTree();
		if (mInKbMode) {
			mTypedTimes = savedInstanceState
					.getIntegerArrayList(TimePickerDialogMultiple.KEY_TYPED_TIMES);
			tryStartingKbMode(-1);
			mHourView.invalidate();
		} else if (mTypedTimes == null) {
			mTypedTimes = new ArrayList<Integer>();
		}
		// Set the theme at the end so that the initialize()s above don't counteract the
		// theme.
		mTimePicker.setTheme(getActivity().getApplicationContext(), mThemeDark);
		// Prepare some colors to use.
		int white = res.getColor(R.color.white);
		int circleBackground = res.getColor(R.color.circle_background);
		int line = res.getColor(R.color.line_background);
		int timeDisplay = res.getColor(R.color.numbers_text_color);
		ColorStateList doneTextColor = res.getColorStateList(R.color.done_text_color);
		int doneBackground = R.drawable.done_background_color;
		int darkGray = res.getColor(R.color.dark_gray);
		int lightGray = res.getColor(R.color.light_gray);
		int darkLine = res.getColor(R.color.line_dark);
		ColorStateList darkDoneTextColor = res
				.getColorStateList(R.color.done_text_color_dark);
		int darkDoneBackground = R.drawable.done_background_color_dark;
		// Set the colors for each view based on the theme.
		view.findViewById(R.id.time_display_background).setBackgroundColor(
				mThemeDark ? darkGray : white);
		view.findViewById(R.id.time_display).setBackgroundColor(
				mThemeDark ? darkGray : white);
		((TextView) view.findViewById(R.id.separator)).setTextColor(mThemeDark ? white
				: timeDisplay);
		((TextView) view.findViewById(R.id.ampm_label)).setTextColor(mThemeDark ? white
				: timeDisplay);
		view.findViewById(R.id.line).setBackgroundColor(mThemeDark ? darkLine : line);
		mDoneButton.setTextColor(mThemeDark ? darkDoneTextColor : doneTextColor);
		mTimePicker.setBackgroundColor(mThemeDark ? lightGray : circleBackground);
		mDoneButton.setBackgroundResource(mThemeDark ? darkDoneBackground
				: doneBackground);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		mHapticFeedbackController.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		mHapticFeedbackController.stop();
	}

	public void tryVibrate() {
		mHapticFeedbackController.tryVibrate();
	}

	private void updateAmPmDisplay(int amOrPm) {
		if (amOrPm == TimePickerDialogMultiple.AM) {
			mAmPmTextView.setText(mAmText);
			Utils.tryAccessibilityAnnounce(mTimePicker, mAmText);
			mAmPmHitspace.setContentDescription(mAmText);
		} else if (amOrPm == TimePickerDialogMultiple.PM) {
			mAmPmTextView.setText(mPmText);
			Utils.tryAccessibilityAnnounce(mTimePicker, mPmText);
			mAmPmHitspace.setContentDescription(mPmText);
		} else {
			mAmPmTextView.setText(mDoublePlaceholderText);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mTimePicker != null) {
			outState.putBundle(TimePickerDialogMultiple.KEY_CALLER_ID, mBundle);
			outState.putInt(TimePickerDialogMultiple.KEY_TITLE_ID, mTitleId);
			outState.putInt(TimePickerDialogMultiple.KEY_ORDINAL_NUMBER, mOrdinalNumber);
			outState.putInt(TimePickerDialogMultiple.KEY_PREVIOUS_RADIO_BUTTON_ID,
					mPreviousRadioButtonId);
			TimeAttribute timeAttribute = mTimeAttributes.get(index);
			timeAttribute.setHours(mTimePicker.getHours());
			timeAttribute.setMinutes(mTimePicker.getMinutes());
			outState.putParcelableArrayList(TimePickerDialogMultiple.KEY_TIME_ATTRIBUTES,
					mTimeAttributes);
			outState.putBoolean(TimePickerDialogMultiple.KEY_SHOW_TIME_SELECTOR,
					mShowTimeSelector);
			outState.putInt(TimePickerDialogMultiple.KEY_HOUR_OF_DAY,
					mTimePicker.getHours());
			outState.putInt(TimePickerDialogMultiple.KEY_MINUTE, mTimePicker.getMinutes());
			outState.putBoolean(TimePickerDialogMultiple.KEY_IS_24_HOUR_VIEW_INITIAL,
					mIs24HourModeInitial);
			outState.putBoolean(TimePickerDialogMultiple.KEY_IS_24_HOUR_VIEW,
					mIs24HourMode);
			outState.putInt(TimePickerDialogMultiple.KEY_CURRENT_ITEM_SHOWING,
					mCurrentItemShowing);
			outState.putInt(TimePickerDialogMultiple.KEY_AM_OR_PM, mAmOrPm);
			outState.putBoolean(TimePickerDialogMultiple.KEY_IN_KB_MODE, mInKbMode);
			if (mInKbMode) {
				outState.putIntegerArrayList(TimePickerDialogMultiple.KEY_TYPED_TIMES,
						mTypedTimes);
			}
			outState.putBoolean(TimePickerDialogMultiple.KEY_DARK_THEME, mThemeDark);
		}
	}

	/** Called by the picker for updating the header display. */
	@Override
	public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
		TimeAttribute timeAttribute = mTimeAttributes.get(index);
		if (pickerIndex == TimePickerDialogMultiple.HOUR_INDEX) {
			setHour(newValue, false);
			timeAttribute.setHours(mTimePicker.getHours());
			String announcement = String.format("%d", newValue);
			if (mAllowAutoAdvance && autoAdvance) {
				mCurrentItemShowing = TimePickerDialogMultiple.MINUTE_INDEX;
				setCurrentItemShowing(mCurrentItemShowing, true, true, false);
				announcement += ". " + mSelectMinutes;
			} else {
				mTimePicker.setContentDescription(mHourPickerDescription + ": "
						+ newValue);
			}
			Utils.tryAccessibilityAnnounce(mTimePicker, announcement);
		} else if (pickerIndex == TimePickerDialogMultiple.MINUTE_INDEX) {
			setMinute(newValue);
			timeAttribute.setMinutes(mTimePicker.getMinutes());
			mTimePicker.setContentDescription(mMinutePickerDescription + ": " + newValue);
		} else if (pickerIndex == TimePickerDialogMultiple.AMPM_INDEX) {
			mAmOrPm = newValue;
			updateAmPmDisplay(mAmOrPm);
			timeAttribute.amOrPm = mAmOrPm;
		} else if (pickerIndex == TimePickerDialogMultiple.ENABLE_PICKER_INDEX) {
			if (!isTypedTimeFullyLegal()) {
				mTypedTimes.clear();
			}
			finishKbMode(true);
		}
	}

	private void setHour(int value, boolean announce) {
		String format;
		if (mIs24HourMode) {
			format = "%02d";
		} else {
			format = "%d";
			value = value % 12;
			if (value == 0) {
				value = 12;
			}
		}
		CharSequence text = String.format(format, value);
		mHourView.setText(text);
		mHourSpaceView.setText(text);
		if (announce) {
			Utils.tryAccessibilityAnnounce(mTimePicker, text);
		}
	}

	private void setMinute(int value) {
		if (value == 60) {
			value = 0;
		}
		CharSequence text = String.format(Locale.getDefault(), "%02d", value);
		Utils.tryAccessibilityAnnounce(mTimePicker, text);
		mMinuteView.setText(text);
		mMinuteSpaceView.setText(text);
	}

	// Show either Hours or Minutes.
	private void setCurrentItemShowing(int index, boolean animateCircle,
			boolean delayLabelAnimate, boolean announce) {
		mTimePicker.setCurrentItemShowing(index, animateCircle);
		TextView labelToAnimate;
		if (index == TimePickerDialogMultiple.HOUR_INDEX) {
			int hours = mTimePicker.getHours();
			if (!mIs24HourMode) {
				hours = hours % 12;
			}
			mTimePicker.setContentDescription(mHourPickerDescription + ": " + hours);
			if (announce) {
				Utils.tryAccessibilityAnnounce(mTimePicker, mSelectHours);
			}
			labelToAnimate = mHourView;
		} else {
			int minutes = mTimePicker.getMinutes();
			mTimePicker.setContentDescription(mMinutePickerDescription + ": " + minutes);
			if (announce) {
				Utils.tryAccessibilityAnnounce(mTimePicker, mSelectMinutes);
			}
			labelToAnimate = mMinuteView;
		}
		int hourColor = index == TimePickerDialogMultiple.HOUR_INDEX ? mSelectedColor
				: mUnselectedColor;
		int minuteColor = index == TimePickerDialogMultiple.MINUTE_INDEX ? mSelectedColor
				: mUnselectedColor;
		mHourView.setTextColor(hourColor);
		mMinuteView.setTextColor(minuteColor);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f,
					1.1f);
			if (delayLabelAnimate) {
				pulseAnimator
						.setStartDelay(TimePickerDialogMultiple.PULSE_ANIMATOR_DELAY);
			}
			pulseAnimator.start();
		}
	}

	/** For keyboard mode, processes key events.
	 *
	 * @param keyCode
	 *            the pressed key.
	 * @return true if the key was successfully processed, false otherwise. */
	private boolean processKeyUp(int keyCode) {
		if (keyCode == KeyEvent.KEYCODE_ESCAPE || keyCode == KeyEvent.KEYCODE_BACK) {
			int size = mTimeAttributes.size();
			for (int i = 0; i < size; i++) {
				TimeAttribute timeAttribute = mTimeAttributes.get(i);
				timeAttribute.currentItemShowing = TimePickerDialogMultiple.HOUR_INDEX;
			}
			dismiss();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_TAB) {
			if (mInKbMode) {
				if (isTypedTimeFullyLegal()) {
					finishKbMode(true);
				}
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_ENTER) {
			if (mInKbMode) {
				if (!isTypedTimeFullyLegal()) {
					return true;
				}
				finishKbMode(false);
			}
			TimeAttribute timeAttribute = mTimeAttributes.get(index);
			timeAttribute.setHours(mTimePicker.getHours());
			timeAttribute.setMinutes(mTimePicker.getMinutes());
			timeAttribute.amOrPm = mAmOrPm;
			int size = mTimeAttributes.size();
			for (int i = 0; i < size; i++) {
				timeAttribute = mTimeAttributes.get(i);
				if (timeAttribute.amOrPm == TimePickerDialogMultiple.AM) {
					timeAttribute.hours = timeAttribute.hours % 12;
				} else if (timeAttribute.amOrPm == TimePickerDialogMultiple.PM) {
					timeAttribute.hours = timeAttribute.hours % 12 + 12;
				}
			}
			if (mCallback != null) {
				if (mCallback.isTimeConsistent(mTimeAttributes, mBundle)) {
					for (int i = 0; i < size; i++) {
						timeAttribute = mTimeAttributes.get(i);
						timeAttribute.currentItemShowing = TimePickerDialogMultiple.HOUR_INDEX;
					}
					mCallback.onTimeSet(mTimePicker, mBundle, mTimeAttributes,
							mOrdinalNumber);
					dismiss();
					return true;
				}
			} else {
				dismiss();
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (mInKbMode) {
				if (!mTypedTimes.isEmpty()) {
					int deleted = deleteLastTypedKey();
					String deletedKeyStr;
					if (deleted == getAmOrPmKeyCode(TimePickerDialogMultiple.AM)) {
						deletedKeyStr = mAmText;
					} else if (deleted == getAmOrPmKeyCode(TimePickerDialogMultiple.PM)) {
						deletedKeyStr = mPmText;
					} else {
						deletedKeyStr = String.format("%d",
								TimePickerDialogMultiple.getValFromKeyCode(deleted));
					}
					Utils.tryAccessibilityAnnounce(mTimePicker,
							String.format(mDeletedKeyFormat, deletedKeyStr));
					updateDisplay(true);
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_0
				|| keyCode == KeyEvent.KEYCODE_1
				|| keyCode == KeyEvent.KEYCODE_2
				|| keyCode == KeyEvent.KEYCODE_3
				|| keyCode == KeyEvent.KEYCODE_4
				|| keyCode == KeyEvent.KEYCODE_5
				|| keyCode == KeyEvent.KEYCODE_6
				|| keyCode == KeyEvent.KEYCODE_7
				|| keyCode == KeyEvent.KEYCODE_8
				|| keyCode == KeyEvent.KEYCODE_9
				|| !mIs24HourMode
				&& (keyCode == getAmOrPmKeyCode(TimePickerDialogMultiple.AM) || keyCode == getAmOrPmKeyCode(TimePickerDialogMultiple.PM))) {
			if (!mInKbMode) {
				if (mTimePicker == null) {
					// Something's wrong, because time picker should definitely not be
					// null.
					Log.e(TimePickerDialogMultiple.TAG,
							"Unable to initiate keyboard mode, TimePicker was null.");
					return true;
				}
				mTypedTimes.clear();
				tryStartingKbMode(keyCode);
				return true;
			}
			// We're already in keyboard mode.
			if (addKeyIfLegal(keyCode)) {
				updateDisplay(false);
			}
			return true;
		}
		return false;
	}

	/** Try to start keyboard mode with the specified key, as long as the timepicker is not
	 * in the middle of a touch-event.
	 *
	 * @param keyCode
	 *            The key to use as the first press. Keyboard mode will not be started if
	 *            the key is not legal to start with. Or, pass in -1 to get into keyboard
	 *            mode without a starting key. */
	private void tryStartingKbMode(int keyCode) {
		if (mTimePicker.trySettingInputEnabled(false)
				&& (keyCode == -1 || addKeyIfLegal(keyCode))) {
			mInKbMode = true;
			mDoneButton.setEnabled(false);
			updateDisplay(false);
		}
	}

	private boolean addKeyIfLegal(int keyCode) {
		// If we're in 24hour mode, we'll need to check if the input is full. If in AM/PM
		// mode,
		// we'll need to see if AM/PM have been typed.
		if (mIs24HourMode && mTypedTimes.size() == 4 || !mIs24HourMode
				&& isTypedTimeFullyLegal()) {
			return false;
		}
		mTypedTimes.add(keyCode);
		if (!isTypedTimeLegalSoFar()) {
			deleteLastTypedKey();
			return false;
		}
		int val = TimePickerDialogMultiple.getValFromKeyCode(keyCode);
		Utils.tryAccessibilityAnnounce(mTimePicker, String.format("%d", val));
		// Automatically fill in 0's if AM or PM was legally entered.
		if (isTypedTimeFullyLegal()) {
			if (!mIs24HourMode && mTypedTimes.size() <= 3) {
				mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
				mTypedTimes.add(mTypedTimes.size() - 1, KeyEvent.KEYCODE_0);
			}
			mDoneButton.setEnabled(true);
		}
		return true;
	}

	/** Traverse the tree to see if the keys that have been typed so far are legal as is,
	 * or may become legal as more keys are typed (excluding backspace). */
	private boolean isTypedTimeLegalSoFar() {
		Node node = mLegalTimesTree;
		for (int keyCode : mTypedTimes) {
			node = node.canReach(keyCode);
			if (node == null) {
				return false;
			}
		}
		return true;
	}

	/** Check if the time that has been typed so far is completely legal, as is. */
	private boolean isTypedTimeFullyLegal() {
		if (mIs24HourMode) {
			// For 24-hour mode, the time is legal if the hours and minutes are each
			// legal. Note:
			// getEnteredTime() will ONLY call isTypedTimeFullyLegal() when NOT in 24hour
			// mode.
			int[] values = getEnteredTime(null);
			return values[0] >= 0 && values[1] >= 0 && values[1] < 60;
		} else {
			// For AM/PM mode, the time is legal if it contains an AM or PM, as those can
			// only be
			// legally added at specific times based on the tree's algorithm.
			return mTypedTimes.contains(getAmOrPmKeyCode(TimePickerDialogMultiple.AM))
					|| mTypedTimes
							.contains(getAmOrPmKeyCode(TimePickerDialogMultiple.PM));
		}
	}

	private int deleteLastTypedKey() {
		int deleted = mTypedTimes.remove(mTypedTimes.size() - 1);
		if (!isTypedTimeFullyLegal()) {
			mDoneButton.setEnabled(false);
		}
		return deleted;
	}

	/** Get out of keyboard mode. If there is nothing in typedTimes, revert to TimePicker's
	 * time.
	 *
	 * @param changeDisplays
	 *            If true, update the displays with the relevant time. */
	private void finishKbMode(boolean updateDisplays) {
		mInKbMode = false;
		if (!mTypedTimes.isEmpty()) {
			int values[] = getEnteredTime(null);
			mTimePicker.setTime(values[0], values[1]);
			if (!mIs24HourMode) {
				mTimePicker.setAmOrPm(values[2]);
			}
			mTypedTimes.clear();
		}
		if (updateDisplays) {
			updateDisplay(false);
			mTimePicker.trySettingInputEnabled(true);
		}
	}

	/** Update the hours, minutes, and AM/PM displays with the typed times. If the
	 * typedTimes is empty, either show an empty display (filled with the placeholder
	 * text), or update from the timepicker's values.
	 *
	 * @param allowEmptyDisplay
	 *            if true, then if the typedTimes is empty, use the placeholder text.
	 *            Otherwise, revert to the timepicker's values. */
	private void updateDisplay(boolean allowEmptyDisplay) {
		if (!allowEmptyDisplay && mTypedTimes.isEmpty()) {
			int hour = mTimePicker.getHours();
			int minute = mTimePicker.getMinutes();
			setHour(hour, true);
			setMinute(minute);
			if (!mIs24HourMode) {
				updateAmPmDisplay(hour < 12 ? TimePickerDialogMultiple.AM
						: TimePickerDialogMultiple.PM);
			}
			setCurrentItemShowing(mTimePicker.getCurrentItemShowing(), true, true, true);
			mDoneButton.setEnabled(true);
		} else {
			Boolean[] enteredZeros = {false, false};
			int[] values = getEnteredTime(enteredZeros);
			String hourFormat = enteredZeros[0] ? "%02d" : "%2d";
			String minuteFormat = enteredZeros[1] ? "%02d" : "%2d";
			String hourStr = values[0] == -1 ? mDoublePlaceholderText : String.format(
					hourFormat, values[0]).replace(' ', mPlaceholderText);
			String minuteStr = values[1] == -1 ? mDoublePlaceholderText : String.format(
					minuteFormat, values[1]).replace(' ', mPlaceholderText);
			mHourView.setText(hourStr);
			mHourSpaceView.setText(hourStr);
			mHourView.setTextColor(mUnselectedColor);
			mMinuteView.setText(minuteStr);
			mMinuteSpaceView.setText(minuteStr);
			mMinuteView.setTextColor(mUnselectedColor);
			if (!mIs24HourMode) {
				updateAmPmDisplay(values[2]);
			}
		}
	}

	private static int getValFromKeyCode(int keyCode) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_0:
			return 0;
		case KeyEvent.KEYCODE_1:
			return 1;
		case KeyEvent.KEYCODE_2:
			return 2;
		case KeyEvent.KEYCODE_3:
			return 3;
		case KeyEvent.KEYCODE_4:
			return 4;
		case KeyEvent.KEYCODE_5:
			return 5;
		case KeyEvent.KEYCODE_6:
			return 6;
		case KeyEvent.KEYCODE_7:
			return 7;
		case KeyEvent.KEYCODE_8:
			return 8;
		case KeyEvent.KEYCODE_9:
			return 9;
		default:
			return -1;
		}
	}

	/** Get the currently-entered time, as integer values of the hours and minutes typed.
	 *
	 * @param enteredZeros
	 *            A size-2 boolean array, which the caller should initialize, and which
	 *            may then be used for the caller to know whether zeros had been
	 *            explicitly entered as either hours of minutes. This is helpful for
	 *            deciding whether to show the dashes, or actual 0's.
	 * @return A size-3 int array. The first value will be the hours, the second value
	 *         will be the minutes, and the third will be either TimePickerDialog.AM or
	 *         TimePickerDialog.PM. */
	private int[] getEnteredTime(Boolean[] enteredZeros) {
		int amOrPm = -1;
		int startIndex = 1;
		if (!mIs24HourMode && isTypedTimeFullyLegal()) {
			int keyCode = mTypedTimes.get(mTypedTimes.size() - 1);
			if (keyCode == getAmOrPmKeyCode(TimePickerDialogMultiple.AM)) {
				amOrPm = TimePickerDialogMultiple.AM;
			} else if (keyCode == getAmOrPmKeyCode(TimePickerDialogMultiple.PM)) {
				amOrPm = TimePickerDialogMultiple.PM;
			}
			startIndex = 2;
		}
		int minute = -1;
		int hour = -1;
		for (int i = startIndex; i <= mTypedTimes.size(); i++) {
			int val = TimePickerDialogMultiple.getValFromKeyCode(mTypedTimes
					.get(mTypedTimes.size() - i));
			if (i == startIndex) {
				minute = val;
			} else if (i == startIndex + 1) {
				minute += 10 * val;
				if (enteredZeros != null && val == 0) {
					enteredZeros[1] = true;
				}
			} else if (i == startIndex + 2) {
				hour = val;
			} else if (i == startIndex + 3) {
				hour += 10 * val;
				if (enteredZeros != null && val == 0) {
					enteredZeros[0] = true;
				}
			}
		}
		int[] ret = {hour, minute, amOrPm};
		return ret;
	}

	/** Get the keycode value for AM and PM in the current language. */
	private int getAmOrPmKeyCode(int amOrPm) {
		// Cache the codes.
		if (mAmKeyCode == -1 || mPmKeyCode == -1) {
			// Find the first character in the AM/PM text that is unique.
			KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
			char amChar;
			char pmChar;
			for (int i = 0; i < Math.max(mAmText.length(), mPmText.length()); i++) {
				amChar = mAmText.toLowerCase(Locale.getDefault()).charAt(i);
				pmChar = mPmText.toLowerCase(Locale.getDefault()).charAt(i);
				if (amChar != pmChar) {
					KeyEvent[] events = kcm.getEvents(new char[] {amChar, pmChar});
					// There should be 4 events: a down and up for both AM and PM.
					if (events != null && events.length == 4) {
						mAmKeyCode = events[0].getKeyCode();
						mPmKeyCode = events[2].getKeyCode();
					} else {
						Log.e(TimePickerDialogMultiple.TAG,
								"Unable to find keycodes for AM and PM.");
					}
					break;
				}
			}
		}
		if (amOrPm == TimePickerDialogMultiple.AM) {
			return mAmKeyCode;
		} else if (amOrPm == TimePickerDialogMultiple.PM) {
			return mPmKeyCode;
		}
		return -1;
	}

	/** Create a tree for deciding what keys can legally be typed. */
	private void generateLegalTimesTree() {
		// Create a quick cache of numbers to their keycodes.
		int k0 = KeyEvent.KEYCODE_0;
		int k1 = KeyEvent.KEYCODE_1;
		int k2 = KeyEvent.KEYCODE_2;
		int k3 = KeyEvent.KEYCODE_3;
		int k4 = KeyEvent.KEYCODE_4;
		int k5 = KeyEvent.KEYCODE_5;
		int k6 = KeyEvent.KEYCODE_6;
		int k7 = KeyEvent.KEYCODE_7;
		int k8 = KeyEvent.KEYCODE_8;
		int k9 = KeyEvent.KEYCODE_9;
		// The root of the tree doesn't contain any numbers.
		mLegalTimesTree = new Node();
		if (mIs24HourMode) {
			// We'll be re-using these nodes, so we'll save them.
			Node minuteFirstDigit = new Node(k0, k1, k2, k3, k4, k5);
			Node minuteSecondDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			// The first digit must be followed by the second digit.
			minuteFirstDigit.addChild(minuteSecondDigit);
			// The first digit may be 0-1.
			Node firstDigit = new Node(k0, k1);
			mLegalTimesTree.addChild(firstDigit);
			// When the first digit is 0-1, the second digit may be 0-5.
			Node secondDigit = new Node(k0, k1, k2, k3, k4, k5);
			firstDigit.addChild(secondDigit);
			// We may now be followed by the first minute digit. E.g. 00:09, 15:58.
			secondDigit.addChild(minuteFirstDigit);
			// When the first digit is 0-1, and the second digit is 0-5, the third digit
			// may be 6-9.
			Node thirdDigit = new Node(k6, k7, k8, k9);
			// The time must now be finished. E.g. 0:55, 1:08.
			secondDigit.addChild(thirdDigit);
			// When the first digit is 0-1, the second digit may be 6-9.
			secondDigit = new Node(k6, k7, k8, k9);
			firstDigit.addChild(secondDigit);
			// We must now be followed by the first minute digit. E.g. 06:50, 18:20.
			secondDigit.addChild(minuteFirstDigit);
			// The first digit may be 2.
			firstDigit = new Node(k2);
			mLegalTimesTree.addChild(firstDigit);
			// When the first digit is 2, the second digit may be 0-3.
			secondDigit = new Node(k0, k1, k2, k3);
			firstDigit.addChild(secondDigit);
			// We must now be followed by the first minute digit. E.g. 20:50, 23:09.
			secondDigit.addChild(minuteFirstDigit);
			// When the first digit is 2, the second digit may be 4-5.
			secondDigit = new Node(k4, k5);
			firstDigit.addChild(secondDigit);
			// We must now be followd by the last minute digit. E.g. 2:40, 2:53.
			secondDigit.addChild(minuteSecondDigit);
			// The first digit may be 3-9.
			firstDigit = new Node(k3, k4, k5, k6, k7, k8, k9);
			mLegalTimesTree.addChild(firstDigit);
			// We must now be followed by the first minute digit. E.g. 3:57, 8:12.
			firstDigit.addChild(minuteFirstDigit);
		} else {
			// We'll need to use the AM/PM node a lot.
			// Set up AM and PM to respond to "a" and "p".
			Node ampm = new Node(getAmOrPmKeyCode(TimePickerDialogMultiple.AM),
					getAmOrPmKeyCode(TimePickerDialogMultiple.PM));
			// The first hour digit may be 1.
			Node firstDigit = new Node(k1);
			mLegalTimesTree.addChild(firstDigit);
			// We'll allow quick input of on-the-hour times. E.g. 1pm.
			firstDigit.addChild(ampm);
			// When the first digit is 1, the second digit may be 0-2.
			Node secondDigit = new Node(k0, k1, k2);
			firstDigit.addChild(secondDigit);
			// Also for quick input of on-the-hour times. E.g. 10pm, 12am.
			secondDigit.addChild(ampm);
			// When the first digit is 1, and the second digit is 0-2, the third digit may
			// be 0-5.
			Node thirdDigit = new Node(k0, k1, k2, k3, k4, k5);
			secondDigit.addChild(thirdDigit);
			// The time may be finished now. E.g. 1:02pm, 1:25am.
			thirdDigit.addChild(ampm);
			// When the first digit is 1, the second digit is 0-2, and the third digit is
			// 0-5,
			// the fourth digit may be 0-9.
			Node fourthDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			thirdDigit.addChild(fourthDigit);
			// The time must be finished now. E.g. 10:49am, 12:40pm.
			fourthDigit.addChild(ampm);
			// When the first digit is 1, and the second digit is 0-2, the third digit may
			// be 6-9.
			thirdDigit = new Node(k6, k7, k8, k9);
			secondDigit.addChild(thirdDigit);
			// The time must be finished now. E.g. 1:08am, 1:26pm.
			thirdDigit.addChild(ampm);
			// When the first digit is 1, the second digit may be 3-5.
			secondDigit = new Node(k3, k4, k5);
			firstDigit.addChild(secondDigit);
			// When the first digit is 1, and the second digit is 3-5, the third digit may
			// be 0-9.
			thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			secondDigit.addChild(thirdDigit);
			// The time must be finished now. E.g. 1:39am, 1:50pm.
			thirdDigit.addChild(ampm);
			// The hour digit may be 2-9.
			firstDigit = new Node(k2, k3, k4, k5, k6, k7, k8, k9);
			mLegalTimesTree.addChild(firstDigit);
			// We'll allow quick input of on-the-hour-times. E.g. 2am, 5pm.
			firstDigit.addChild(ampm);
			// When the first digit is 2-9, the second digit may be 0-5.
			secondDigit = new Node(k0, k1, k2, k3, k4, k5);
			firstDigit.addChild(secondDigit);
			// When the first digit is 2-9, and the second digit is 0-5, the third digit
			// may be 0-9.
			thirdDigit = new Node(k0, k1, k2, k3, k4, k5, k6, k7, k8, k9);
			secondDigit.addChild(thirdDigit);
			// The time must be finished now. E.g. 2:57am, 9:30pm.
			thirdDigit.addChild(ampm);
		}
	}

	/** Simple node class to be used for traversal to check for legal times. mLegalKeys
	 * represents the keys that can be typed to get to the node. mChildren are the
	 * children that can be reached from this node. */
	private class Node {
		private int[] mLegalKeys;
		private ArrayList<Node> mChildren;

		public Node(int... legalKeys) {
			mLegalKeys = legalKeys;
			mChildren = new ArrayList<Node>();
		}

		public void addChild(Node child) {
			mChildren.add(child);
		}

		public boolean containsKey(int key) {
			for (int i = 0; i < mLegalKeys.length; i++) {
				if (mLegalKeys[i] == key) {
					return true;
				}
			}
			return false;
		}

		public Node canReach(int key) {
			if (mChildren == null) {
				return null;
			}
			for (Node child : mChildren) {
				if (child.containsKey(key)) {
					return child;
				}
			}
			return null;
		}
	}

	private class KeyboardListener implements OnKeyListener {
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_UP) {
				return processKeyUp(keyCode);
			}
			return false;
		}
	}
}
