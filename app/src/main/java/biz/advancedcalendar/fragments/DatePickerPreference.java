package biz.advancedcalendar.fragments;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.DatePicker;
import android.widget.EditText;

//import com.social.R;

/**
 * The {@link EditTextPreference} class is a preference that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog. This {@link EditText} can be modified either programmatically
 * via {@link #getEditText()}, or through XML by setting any EditText attributes
 * on the EditTextPreference.
 * <p>
 * This preference will store a string into the SharedPreferences.
 * <p>
 * See {@link android.R.styleable#EditText EditText Attributes}.
 */
public class DatePickerPreference extends DialogPreference {
	/**
	 * The edit text shown in the dialog.
	 */
	private DatePicker mDatePicker;

	private Calendar mCalendar = Calendar.getInstance();

	public DatePickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

		mDatePicker = new DatePicker(context, attrs);

		// Give it an ID so it can be saved/restored
		mDatePicker.setId(/* R.id.dialDatePicker */1);
		// setDialogLayoutResource(android.R.attr.dialogLayout);
		/*
		 * The preference framework and view framework both have an 'enabled'
		 * attribute. Most likely, the 'enabled' specified in this XML is for
		 * the preference framework, but it was also given to the view
		 * framework. We reset the enabled state.
		 */
		mDatePicker.setEnabled(true);
	}

	public DatePickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public DatePickerPreference(Context context) {
		this(context, null);
	}

	/**
	 * Saves the text to the {@link SharedPreferences}.
	 * 
	 * @param text
	 *            The text to save
	 */
	public void setDate(Date date) {
		final boolean wasBlocking = shouldDisableDependents();

		mCalendar.setTime(date);
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		persistString(dateFormat.format(mCalendar.getTime()));
		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
	}

	/**
	 * Gets the text from the {@link SharedPreferences}.
	 * 
	 * @return The current preference value.
	 */
	public Date getDate() {
		return mCalendar.getTime();
	}

	// @Override
	// protected void onBindDialogView(View view) {
	// super.onBindDialogView(view);
	//
	// DatePicker datePicker = mDatePicker;
	// datePicker.updateDate(getCalendar().get(Calendar.YEAR),
	// getCalendar().get(Calendar.MONTH),
	// getCalendar().get(Calendar.DAY_OF_MONTH));
	//
	// ViewParent oldParent = datePicker.getParent();
	// if (oldParent != view) {
	// if (oldParent != null) {
	// ((ViewGroup) oldParent).removeView(datePicker);
	// }
	// onAddEditTextToDialogView(view, datePicker);
	// }
	// }
	//
	//
	//
	// /**
	// * Adds the EditText widget of this preference to the dialog's view.
	// *
	// * @param dialogView The dialog view.
	// */
	// protected void onAddEditTextToDialogView(View dialogView, DatePicker
	// datePicker) {
	// FrameLayout container = (FrameLayout)
	// dialogView.findViewById(android.R.id.custom);
	// if (container != null) {
	// container.addView(datePicker, ViewGroup.LayoutParams.FILL_PARENT,
	// ViewGroup.LayoutParams.WRAP_CONTENT);
	// }
	// }

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		DatePicker datePicker = mDatePicker;
		datePicker.updateDate(getCalendar().get(Calendar.YEAR), getCalendar()
				.get(Calendar.MONTH), getCalendar().get(Calendar.DAY_OF_MONTH));
		ViewParent oldParent = datePicker.getParent();
		if (oldParent != null) {
			((ViewGroup) oldParent).removeView(datePicker);
		}
		builder.setView(mDatePicker);
		super.onPrepareDialogBuilder(builder);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			@SuppressWarnings("deprecation")
			Date date = new Date(mDatePicker.getYear() - 1900,
					mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
			if (callChangeListener(DateFormat
					.getDateInstance(DateFormat.MEDIUM).format(date))) {
				setDate(date);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		try {
			String string = getPersistedString(DateFormat.getDateInstance(
					DateFormat.SHORT).format(Calendar.getInstance().getTime()));
			setDate(restoreValue ? DateFormat.getDateInstance(DateFormat.SHORT)
					.parse(string) : (Date) defaultValue);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean shouldDisableDependents() {
		return (mCalendar == null) || super.shouldDisableDependents();
	}

	/**
	 * Returns the {@link EditText} widget that will be shown in the dialog.
	 * 
	 * @return The {@link EditText} widget that will be shown in the dialog.
	 */

	protected Calendar getCalendar() {
		return mCalendar;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			// No need to save instance state since it's persistent
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.text = DateFormat.getDateInstance(DateFormat.SHORT).format(
				mCalendar.getTime());
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		try {
			setDate(DateFormat.getDateInstance(DateFormat.SHORT).parse(
					myState.text));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static class SavedState extends BaseSavedState {
		String text;

		public SavedState(Parcel source) {
			super(source);
			text = source.readString();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeString(text);
		}

		public SavedState(Parcelable superState) {
			super(superState);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

}