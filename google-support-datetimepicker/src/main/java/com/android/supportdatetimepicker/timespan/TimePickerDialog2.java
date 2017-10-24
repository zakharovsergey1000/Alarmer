/* Copyright (C) 2013 The Android Open Source Project Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the
 * License */
package com.android.supportdatetimepicker.timespan;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import com.android.supportdatetimepicker.HapticFeedbackController;
import com.android.supportdatetimepicker.R;
import com.android.supportdatetimepicker.Utils;
import com.android.supportdatetimepicker.timespan.RadialPickerLayout.OnValueSelectedListener;
import java.util.ArrayList;
import java.util.Locale;

/** Dialog to set a time. */
public class TimePickerDialog2 extends DialogFragment implements OnValueSelectedListener {
	private static final String TAG = "TimeSpanPickerDialog";
	private static final String KEY_DAYS = "days";
	private static final String KEY_HOURS = "hours";
	private static final String KEY_MINUTES = "minutes";
	private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
	private static final String KEY_IN_KB_MODE = "in_kb_mode";
	private static final String KEY_TYPED_TIMES = "typed_times";
	private static final String KEY_DARK_THEME = "dark_theme";
	public static final int DAY_INDEX = 0;
	public static final int HOUR_INDEX = 1;
	public static final int MINUTE_INDEX = 2;
	// NOT a real index for the purpose of what's showing.
	public static final int AMPM_INDEX = 2;
	// Also NOT a real index, just used for keyboard mode.
	public static final int ENABLE_PICKER_INDEX = 3;
	public static final int AM = 0;
	public static final int PM = 1;
	// Delay before starting the pulse animation, in ms.
	private static final int PULSE_ANIMATOR_DELAY = 300;
	private OnTimeSetListener mCallback;
	private HapticFeedbackController mHapticFeedbackController;
	private TextView mDoneButton;
	private TextView mDaysView;
	private TextView mDaySpaceView;
	private TextView mDayTextView;
	private TextView mHoursView;
	private TextView mHourSpaceView;
	private TextView mHourTextView;
	private TextView mMinutesView;
	private TextView mMinuteSpaceView;
	private TextView mMinuteTextView;
	private RadialPickerLayout mTimeSpanPicker;
	private int mSelectedColor;
	private int mUnselectedColor;
	private boolean mAllowAutoAdvance;
	private int mInitialDays;
	private int mInitialHours;
	private int mInitialMinutes;
	private boolean mIs24HourMode;
	private boolean mThemeDark;
	// For hardware IME input.
	private char mPlaceholderText;
	private String mDoublePlaceholderText;
	private String mDeletedKeyFormat;
	private boolean mInKbMode;
	private ArrayList<Integer> mTypedTimes;
	private Node mLegalTimesTree;
	// Accessibility strings.
	private String mDaysPickerDescription;
	private String mSelectDays;
	private String mHoursPickerDescription;
	private String mSelectHours;
	private String mMinutesPickerDescription;
	private String mSelectMinutes;

	/** The callback interface used to indicate the user is done filling in the timespan
	 * (they clicked on the 'Set' button). */
	public interface OnTimeSetListener {
		void onTimeSpanSet(RadialPickerLayout view, int days, int hours, int minutes);
	}

	public TimePickerDialog2() {
		// Empty constructor required for dialog fragment.
	}

	public static TimePickerDialog2 newInstance(OnTimeSetListener callback, int days,
			int hours, int minutes) {
		TimePickerDialog2 ret = new TimePickerDialog2();
		ret.initialize(callback, days, hours, minutes);
		return ret;
	}

	public void initialize(OnTimeSetListener callback, int days, int hours, int minutes) {
		mCallback = callback;
		mInitialDays = days;
		mInitialHours = hours;
		mInitialMinutes = minutes;
		mInKbMode = false;
		mThemeDark = false;
	}

	/** Set a dark or light theme. NOTE: this will only take effect for the next
	 * onCreateView. */
	public void setThemeDark(boolean dark) {
		mThemeDark = dark;
	}

	public boolean isThemeDark() {
		return mThemeDark;
	}

	public void setOnTimeSpanSetListener(OnTimeSetListener callback) {
		mCallback = callback;
	}

	public void setStartTime(int days, int hours, int minutes) {
		mInitialDays = days;
		mInitialHours = hours;
		mInitialMinutes = minutes;
		mInKbMode = false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(TimePickerDialog2.KEY_DAYS)
				&& savedInstanceState.containsKey(TimePickerDialog2.KEY_HOURS)
				&& savedInstanceState.containsKey(TimePickerDialog2.KEY_MINUTES)) {
			mInitialDays = savedInstanceState.getInt(TimePickerDialog2.KEY_DAYS);
			mInitialHours = savedInstanceState.getInt(TimePickerDialog2.KEY_HOURS);
			mInitialMinutes = savedInstanceState.getInt(TimePickerDialog2.KEY_MINUTES);
			mInKbMode = savedInstanceState.getBoolean(TimePickerDialog2.KEY_IN_KB_MODE);
			mThemeDark = savedInstanceState.getBoolean(TimePickerDialog2.KEY_DARK_THEME);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.support_timespan_picker_dialog, null);
		KeyboardListener keyboardListener = new KeyboardListener();
		view.findViewById(R.id.timespan_picker_dialog).setOnKeyListener(keyboardListener);
		Resources res = getResources();
		mDaysPickerDescription = res.getString(R.string.timespan_day_picker_description);
		mSelectDays = res.getString(R.string.select_day);
		mHoursPickerDescription = res
				.getString(R.string.timespan_hour_picker_description);
		mSelectHours = res.getString(R.string.select_hours);
		mMinutesPickerDescription = res
				.getString(R.string.timespan_minute_picker_description);
		mSelectMinutes = res.getString(R.string.select_minutes);
		mSelectedColor = res.getColor(mThemeDark ? R.color.red : R.color.blue);
		mUnselectedColor = res.getColor(mThemeDark ? R.color.white
				: R.color.numbers_text_color);
		mDaysView = (TextView) view.findViewById(R.id.days);
		mDaysView.setOnKeyListener(keyboardListener);
		mDaySpaceView = (TextView) view.findViewById(R.id.day_space);
		mDayTextView = (TextView) view.findViewById(R.id.days_label);
		mDayTextView.setOnKeyListener(keyboardListener);
		mHoursView = (TextView) view.findViewById(R.id.hours);
		mHoursView.setOnKeyListener(keyboardListener);
		mHourSpaceView = (TextView) view.findViewById(R.id.hour_space);
		mHourTextView = (TextView) view.findViewById(R.id.hours_label);
		mHourTextView.setOnKeyListener(keyboardListener);
		mMinutesView = (TextView) view.findViewById(R.id.minutes);
		mMinutesView.setOnKeyListener(keyboardListener);
		mMinuteSpaceView = (TextView) view.findViewById(R.id.minutes_space);
		mMinuteTextView = (TextView) view.findViewById(R.id.minutes_label);
		mMinuteTextView.setOnKeyListener(keyboardListener);
		mHapticFeedbackController = new HapticFeedbackController(getActivity());
		mTimeSpanPicker = (RadialPickerLayout) view.findViewById(R.id.timespan_picker);
		mTimeSpanPicker.setOnValueSelectedListener(this);
		mTimeSpanPicker.setOnKeyListener(keyboardListener);
		mTimeSpanPicker.initialize(getActivity(), mHapticFeedbackController,
				mInitialDays, mInitialHours, mInitialMinutes);
		int currentItemShowing = TimePickerDialog2.HOUR_INDEX;
		if (savedInstanceState != null
				&& savedInstanceState
						.containsKey(TimePickerDialog2.KEY_CURRENT_ITEM_SHOWING)) {
			currentItemShowing = savedInstanceState
					.getInt(TimePickerDialog2.KEY_CURRENT_ITEM_SHOWING);
		}
		setCurrentItemShowing(currentItemShowing, false, true, true);
		mTimeSpanPicker.invalidate();
		mDaysView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItemShowing(TimePickerDialog2.DAY_INDEX, true, false, true);
				tryVibrate();
			}
		});
		mHoursView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItemShowing(TimePickerDialog2.HOUR_INDEX, true, false, true);
				tryVibrate();
			}
		});
		mMinutesView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setCurrentItemShowing(TimePickerDialog2.MINUTE_INDEX, true, false, true);
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
				if (mCallback != null) {
					mCallback.onTimeSpanSet(mTimeSpanPicker, mTimeSpanPicker.getDays(),
							mTimeSpanPicker.getHours(), mTimeSpanPicker.getMinutes());
				}
				dismiss();
			}
		});
		mDoneButton.setOnKeyListener(keyboardListener);
		mAllowAutoAdvance = true;
		setDays(mInitialDays, true);
		setHours(mInitialHours, true);
		setMinutes(mInitialMinutes);
		// Set up for keyboard mode.
		mDoublePlaceholderText = res.getString(R.string.time_placeholder);
		mDeletedKeyFormat = res.getString(R.string.deleted_key);
		mPlaceholderText = mDoublePlaceholderText.charAt(0);
		// mAmKeyCode = mPmKeyCode = -1;
		generateLegalTimesTree();
		if (mInKbMode) {
			mTypedTimes = savedInstanceState
					.getIntegerArrayList(TimePickerDialog2.KEY_TYPED_TIMES);
			tryStartingKbMode(-1);
			mHoursView.invalidate();
		} else if (mTypedTimes == null) {
			mTypedTimes = new ArrayList<Integer>();
		}
		// Set the theme at the end so that the initialize()s above don't counteract the
		// theme.
		mTimeSpanPicker.setTheme(getActivity().getApplicationContext(), mThemeDark);
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
		view.findViewById(R.id.timespan_display).setBackgroundColor(
				mThemeDark ? darkGray : white);
		((TextView) view.findViewById(R.id.separator1)).setTextColor(mThemeDark ? white
				: timeDisplay);
		((TextView) view.findViewById(R.id.separator2)).setTextColor(mThemeDark ? white
				: timeDisplay);
		((TextView) view.findViewById(R.id.minutes_label))
				.setTextColor(mThemeDark ? white : timeDisplay);
		view.findViewById(R.id.line).setBackgroundColor(mThemeDark ? darkLine : line);
		mDoneButton.setTextColor(mThemeDark ? darkDoneTextColor : doneTextColor);
		mTimeSpanPicker.setBackgroundColor(mThemeDark ? lightGray : circleBackground);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mTimeSpanPicker != null) {
			outState.putInt(TimePickerDialog2.KEY_DAYS, mTimeSpanPicker.getDays());
			outState.putInt(TimePickerDialog2.KEY_HOURS, mTimeSpanPicker.getHours());
			outState.putInt(TimePickerDialog2.KEY_MINUTES, mTimeSpanPicker.getMinutes());
			outState.putInt(TimePickerDialog2.KEY_CURRENT_ITEM_SHOWING,
					mTimeSpanPicker.getCurrentItemShowing());
			outState.putBoolean(TimePickerDialog2.KEY_IN_KB_MODE, mInKbMode);
			if (mInKbMode) {
				outState.putIntegerArrayList(TimePickerDialog2.KEY_TYPED_TIMES,
						mTypedTimes);
			}
			outState.putBoolean(TimePickerDialog2.KEY_DARK_THEME, mThemeDark);
		}
	}

	/** Called by the picker for updating the header display. */
	@Override
	public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
		if (pickerIndex == TimePickerDialog2.DAY_INDEX) {
			setDays(newValue, false);
			String announcement = String.format("%d", newValue);
			if (mAllowAutoAdvance && autoAdvance) {
				setCurrentItemShowing(TimePickerDialog2.HOUR_INDEX, true, true, false);
				announcement += ". " + mSelectHours;
			} else {
				mTimeSpanPicker.setContentDescription(mDaysPickerDescription + ": "
						+ newValue);
			}
			Utils.tryAccessibilityAnnounce(mTimeSpanPicker, announcement);
		} else if (pickerIndex == TimePickerDialog2.HOUR_INDEX) {
			setHours(newValue, false);
			String announcement = String.format("%d", newValue);
			if (mAllowAutoAdvance && autoAdvance) {
				setCurrentItemShowing(TimePickerDialog2.MINUTE_INDEX, true, true, false);
				announcement += ". " + mSelectMinutes;
			} else {
				mTimeSpanPicker.setContentDescription(mHoursPickerDescription + ": "
						+ newValue);
			}
			Utils.tryAccessibilityAnnounce(mTimeSpanPicker, announcement);
		} else if (pickerIndex == TimePickerDialog2.MINUTE_INDEX) {
			setMinutes(newValue);
			mTimeSpanPicker.setContentDescription(mMinutesPickerDescription + ": "
					+ newValue);
		} else if (pickerIndex == TimePickerDialog2.ENABLE_PICKER_INDEX) {
			if (!isTypedTimeFullyLegal()) {
				mTypedTimes.clear();
			}
			finishKbMode(true);
		}
	}

	private void setDays(int value, boolean announce) {
		String format = "%02d";
		CharSequence text = String.format(format, value);
		mDaysView.setText(text);
		mDaySpaceView.setText(text);
		if (announce) {
			Utils.tryAccessibilityAnnounce(mTimeSpanPicker, text);
		}
	}

	private void setHours(int value, boolean announce) {
		String format = "%02d";
		CharSequence text = String.format(format, value);
		mHoursView.setText(text);
		mHourSpaceView.setText(text);
		if (announce) {
			Utils.tryAccessibilityAnnounce(mTimeSpanPicker, text);
		}
	}

	private void setMinutes(int value) {
		if (value == 60) {
			value = 0;
		}
		CharSequence text = String.format(Locale.getDefault(), "%02d", value);
		Utils.tryAccessibilityAnnounce(mTimeSpanPicker, text);
		mMinutesView.setText(text);
		mMinuteSpaceView.setText(text);
	}

	// Show either Hours or Minutes.
	private void setCurrentItemShowing(int index, boolean animateCircle,
			boolean delayLabelAnimate, boolean announce) {
		mTimeSpanPicker.setCurrentItemShowing(index, animateCircle);
		TextView labelToAnimate;
		if (index == TimePickerDialog2.DAY_INDEX) {
			int days = mTimeSpanPicker.getDays();
			days = days % 31;
			mTimeSpanPicker.setContentDescription(mDaysPickerDescription + ": " + days);
			if (announce) {
				Utils.tryAccessibilityAnnounce(mTimeSpanPicker, mSelectDays);
			}
			labelToAnimate = mDaysView;
		} else if (index == TimePickerDialog2.HOUR_INDEX) {
			int hours = mTimeSpanPicker.getHours();
			if (!mIs24HourMode) {
				hours = hours % 12;
			}
			mTimeSpanPicker.setContentDescription(mHoursPickerDescription + ": " + hours);
			if (announce) {
				Utils.tryAccessibilityAnnounce(mTimeSpanPicker, mSelectHours);
			}
			labelToAnimate = mHoursView;
		} else {
			int minutes = mTimeSpanPicker.getMinutes();
			mTimeSpanPicker.setContentDescription(mMinutesPickerDescription + ": "
					+ minutes);
			if (announce) {
				Utils.tryAccessibilityAnnounce(mTimeSpanPicker, mSelectMinutes);
			}
			labelToAnimate = mMinutesView;
		}
		int dayColor = index == TimePickerDialog2.DAY_INDEX ? mSelectedColor
				: mUnselectedColor;
		int hourColor = index == TimePickerDialog2.HOUR_INDEX ? mSelectedColor
				: mUnselectedColor;
		int minuteColor = index == TimePickerDialog2.MINUTE_INDEX ? mSelectedColor
				: mUnselectedColor;
		mDaysView.setTextColor(dayColor);
		mHoursView.setTextColor(hourColor);
		mMinutesView.setTextColor(minuteColor);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f,
					1.1f);
			if (delayLabelAnimate) {
				pulseAnimator.setStartDelay(TimePickerDialog2.PULSE_ANIMATOR_DELAY);
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
			if (mCallback != null) {
				mCallback.onTimeSpanSet(mTimeSpanPicker, 0, mTimeSpanPicker.getHours(),
						mTimeSpanPicker.getMinutes());
			}
			dismiss();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DEL) {
			if (mInKbMode) {
				if (!mTypedTimes.isEmpty()) {
					int deleted = deleteLastTypedKey();
					String deletedKeyStr = String.format("%d",
							TimePickerDialog2.getValFromKeyCode(deleted));
					Utils.tryAccessibilityAnnounce(mTimeSpanPicker,
							String.format(mDeletedKeyFormat, deletedKeyStr));
					updateDisplay(true);
				}
			}
		} else if (keyCode == KeyEvent.KEYCODE_0 || keyCode == KeyEvent.KEYCODE_1
				|| keyCode == KeyEvent.KEYCODE_2 || keyCode == KeyEvent.KEYCODE_3
				|| keyCode == KeyEvent.KEYCODE_4 || keyCode == KeyEvent.KEYCODE_5
				|| keyCode == KeyEvent.KEYCODE_6 || keyCode == KeyEvent.KEYCODE_7
				|| keyCode == KeyEvent.KEYCODE_8 || keyCode == KeyEvent.KEYCODE_9) {
			if (!mInKbMode) {
				if (mTimeSpanPicker == null) {
					// Something's wrong, because time picker should definitely not be
					// null.
					Log.e(TimePickerDialog2.TAG,
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
		if (mTimeSpanPicker.trySettingInputEnabled(false)
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
		int val = TimePickerDialog2.getValFromKeyCode(keyCode);
		Utils.tryAccessibilityAnnounce(mTimeSpanPicker, String.format("%d", val));
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
		int[] values = getEnteredTime(null);
		return values[0] >= 0 && values[1] >= 0 && values[1] < 60;
	}

	private int deleteLastTypedKey() {
		int deleted = mTypedTimes.remove(mTypedTimes.size() - 1);
		if (!isTypedTimeFullyLegal()) {
			mDoneButton.setEnabled(false);
		}
		return deleted;
	}

	private void finishKbMode(boolean updateDisplays) {
		mInKbMode = false;
		if (!mTypedTimes.isEmpty()) {
			int values[] = getEnteredTime(null);
			mTimeSpanPicker.setTime(values[0], values[1], values[2]);
			mTypedTimes.clear();
		}
		if (updateDisplays) {
			updateDisplay(false);
			mTimeSpanPicker.trySettingInputEnabled(true);
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
			int day = mTimeSpanPicker.getDays();
			int hour = mTimeSpanPicker.getHours();
			int minute = mTimeSpanPicker.getMinutes();
			setDays(day, true);
			setHours(hour, true);
			setMinutes(minute);
			setCurrentItemShowing(mTimeSpanPicker.getCurrentItemShowing(), true, true,
					true);
			mDoneButton.setEnabled(true);
		} else {
			Boolean[] enteredZeros = {false, false, false};
			int[] values = getEnteredTime(enteredZeros);
			String dayFormat = enteredZeros[0] ? "%02d" : "%2d";
			String hourFormat = enteredZeros[1] ? "%02d" : "%2d";
			String minuteFormat = enteredZeros[2] ? "%02d" : "%2d";
			String dayStr = values[0] == -1 ? mDoublePlaceholderText : String.format(
					dayFormat, values[0]).replace(' ', mPlaceholderText);
			String hourStr = values[1] == -1 ? mDoublePlaceholderText : String.format(
					hourFormat, values[1]).replace(' ', mPlaceholderText);
			String minuteStr = values[2] == -1 ? mDoublePlaceholderText : String.format(
					minuteFormat, values[2]).replace(' ', mPlaceholderText);
			mDaysView.setText(dayStr);
			mDaySpaceView.setText(dayStr);
			mDaysView.setTextColor(mUnselectedColor);
			mHoursView.setText(hourStr);
			mHourSpaceView.setText(hourStr);
			mHoursView.setTextColor(mUnselectedColor);
			mMinutesView.setText(minuteStr);
			mMinuteSpaceView.setText(minuteStr);
			mMinutesView.setTextColor(mUnselectedColor);
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
		int startIndex = 1;
		int minute = -1;
		int hour = -1;
		int day = -1;
		for (int i = startIndex; i <= mTypedTimes.size(); i++) {
			int val = TimePickerDialog2.getValFromKeyCode(mTypedTimes.get(mTypedTimes
					.size() - i));
			if (i == startIndex) {
				minute = val;
			} else if (i == startIndex + 1) {
				minute += 10 * val;
				if (enteredZeros != null && val == 0) {
					enteredZeros[2] = true;
				}
			} else if (i == startIndex + 2) {
				hour = val;
			} else if (i == startIndex + 3) {
				hour += 10 * val;
				if (enteredZeros != null && val == 0) {
					enteredZeros[1] = true;
				}
			} else if (i == startIndex + 4) {
				day = val;
			} else if (i == startIndex + 5) {
				day += 10 * val;
				if (enteredZeros != null && val == 0) {
					enteredZeros[0] = true;
				}
			}
		}
		int[] ret = {day, hour, minute};
		return ret;
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
		{
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
