package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.ReminderUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.ValueMustBe;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.utils.Helper;
import java.util.Set;

public class EditTextValueChangedListener implements TextWatcher {
	private EditText editText;
	private TextView textView;
	private RadioButton radioButtonCustom;
	private int divider;
	private Integer integerValue;
	private int minValue;
	private int maxValue;
	private int singularStringId;
	private int pluralStringId;
	private String pluralString;
	private String stringCustom;
	private String radiobuttonTextCustom;
	private String singularSummaryString;
	private String pluralSummaryString;
	private Set<WantingItem> wantingItemSet;
	private WantingItem wantingItemValueToBeGreaterThanOrEqualTo;
	private WantingItem wantingItemValueToBeLessThanOrEqualTo;
	private WantingItem wantingItemValueToBeWithinBounds;
	private Resources resources;
	private ValueMustBe valueMustBe;

	protected EditTextValueChangedListener(Activity activity, EditText editText,
			TextView textView, RadioButton radioButtonCustom, int divider,
			Integer integerValue, int minValue, int maxValue, int singularStringId,
			int pluralStringId, int singularSummaryStringId, int pluralSummaryStringId,
			int radioButtonCustomTextId, int stringCustomId,
			Set<WantingItem> wantingItemSet,
			WantingItem wantingItemValueToBeGreaterThanOrEqualTo,
			WantingItem wantingItemValueToBeLessThanOrEqualTo,
			WantingItem wantingItemValueToBeWithinBounds) {
		this.editText = editText;
		this.textView = textView;
		this.radioButtonCustom = radioButtonCustom;
		this.divider = divider;
		this.integerValue = integerValue;
		this.singularStringId = singularStringId;
		this.pluralStringId = pluralStringId;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.wantingItemSet = wantingItemSet;
		this.wantingItemValueToBeGreaterThanOrEqualTo = wantingItemValueToBeGreaterThanOrEqualTo;
		this.wantingItemValueToBeLessThanOrEqualTo = wantingItemValueToBeLessThanOrEqualTo;
		this.wantingItemValueToBeWithinBounds = wantingItemValueToBeWithinBounds;
		initialize(activity, singularSummaryStringId, pluralSummaryStringId,
				radioButtonCustomTextId, stringCustomId);
	}

	private void initialize(Activity activity, int singularSummaryStringId,
			int pluralSummaryStringId, int radioButtonCustomTextId, int stringCustomId) {
		resources = activity.getResources();
		pluralString = resources.getString(pluralStringId);
		singularSummaryString = resources.getString(singularSummaryStringId);
		pluralSummaryString = resources.getString(pluralSummaryStringId);
		stringCustom = resources.getString(stringCustomId);
		radiobuttonTextCustom = resources.getString(radioButtonCustomTextId);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		integerValue = Helper.getValidEditTextIntegerValueOrNull(editText, pluralString,
				minValue, maxValue, false, false);
		valueMustBe = (ValueMustBe) editText.getTag(R.string.tag_key_value_must_be);
		wantingItemSet.remove(wantingItemValueToBeGreaterThanOrEqualTo);
		wantingItemSet.remove(wantingItemValueToBeLessThanOrEqualTo);
		wantingItemSet.remove(wantingItemValueToBeWithinBounds);
		if (valueMustBe != ValueMustBe.WITHIN_BOUNDS) {
			modifyWordFormOfCustomText();
			if (valueMustBe != null) {
				switch (valueMustBe) {
				case GREATER_THAN_OR_EQUAL_TO:
					wantingItemValueToBeGreaterThanOrEqualTo.setText(ValueMustBe
							.getText());
					wantingItemSet.add(wantingItemValueToBeGreaterThanOrEqualTo);
					break;
				case LESS_THAN_OR_EQUAL_TO:
					wantingItemValueToBeLessThanOrEqualTo.setText(ValueMustBe.getText());
					wantingItemSet.add(wantingItemValueToBeLessThanOrEqualTo);
					break;
				default:
					break;
				}
			}
		} else {
			radioButtonCustom.setText(stringCustom);
			wantingItemValueToBeWithinBounds.setText(ValueMustBe.getText());
			wantingItemSet.add(wantingItemValueToBeWithinBounds);
		}
	}

	private void modifyWordFormOfCustomText() {
		String textCustom;
		String summaryTextCustom;
		if (integerValue == 1) {
			textView.setText(singularStringId);
			summaryTextCustom = String.format(singularSummaryString, integerValue);
		} else {
			textView.setText(pluralStringId);
			summaryTextCustom = String.format(pluralSummaryString, integerValue);
		}
		textCustom = String.format(radiobuttonTextCustom, summaryTextCustom);
		radioButtonCustom.setText(textCustom);
	}

	public void setDivider(int divider) {
		this.divider = divider;
	}

	public Long getLongValue() {
		if (integerValue != null) {
			return integerValue.longValue() * divider;
		} else {
			return null;
		}
	}

	public Integer getIntegerValue() {
		return integerValue;
	}

	public RadioButton getRadioButtonCustom() {
		return radioButtonCustom;
	}

	public ValueMustBe getValueMustBe() {
		return valueMustBe;
	}

	public Set<WantingItem> getWantingItemSet() {
		return wantingItemSet;
	}

	public WantingItem getWantingItemValueToBeGreaterThanOrEqualTo() {
		return wantingItemValueToBeGreaterThanOrEqualTo;
	}

	public WantingItem getWantingItemValueToBeLessThanOrEqualTo() {
		return wantingItemValueToBeLessThanOrEqualTo;
	}

	public WantingItem getWantingItemValueToBeWithinBounds() {
		return wantingItemValueToBeWithinBounds;
	}

	public static boolean setupRadiogroupRingtoneFromPreference(Activity activity,
			int preferenceId, int radioButtonDefaultId, int radioButtonCustomId,
			Task task, String ringtone, OnCheckedChangeListener checkedChangeListener,
			OnClickListener clickListener) {
		Resources resources = activity.getResources();
		RadioButton radioButtonDefault = (RadioButton) activity
				.findViewById(radioButtonDefaultId);
		RadioButton radioButtonCustom = (RadioButton) activity
				.findViewById(radioButtonCustomId);
		String textCustom;
		boolean isRadioButtonCustomChecked;
		if (ringtone == null) {
			textCustom = resources.getString(R.string.custom);
			radioButtonDefault.setChecked(true);
			isRadioButtonCustomChecked = false;
		} else {
			textCustom = String
					.format(resources
							.getString(R.string.fragment_edit_task_part_reminders_radiobutton_text_custom),
							task.getRingtoneTitle(activity, preferenceId));
			radioButtonCustom.setChecked(true);
			isRadioButtonCustomChecked = true;
		}
		String defaultRingtoneTitle = Helper.getRingtoneTitleFromPreference(activity,
				preferenceId);
		radioButtonDefault
				.setText(String.format(
						resources
								.getString(R.string.fragment_edit_task_part_reminders_radiobutton_text_from_settings),
						defaultRingtoneTitle));
		radioButtonCustom.setText(textCustom);
		radioButtonDefault.setOnCheckedChangeListener(checkedChangeListener);
		radioButtonCustom.setOnClickListener(clickListener);
		return isRadioButtonCustomChecked;
	}

	public static boolean setupRadiogroupRingtoneFromTask(Activity activity,
			int preferenceId, int radioButtonDefaultId, int radioButtonCustomId,
			Task task, String ringtone, ReminderUiData reminder,
			OnCheckedChangeListener checkedChangeListener, OnClickListener clickListener) {
		Resources resources = activity.getResources();
		RadioButton radioButtonDefault = (RadioButton) activity
				.findViewById(radioButtonDefaultId);
		RadioButton radioButtonCustom = (RadioButton) activity
				.findViewById(radioButtonCustomId);
		String textCustom;
		boolean isRadioButtonCustomChecked;
		if (ringtone == null) {
			textCustom = resources.getString(R.string.custom);
			radioButtonDefault.setChecked(true);
			isRadioButtonCustomChecked = false;
		} else {
			textCustom = String.format(resources
					.getString(R.string.activity_edit_reminder_radiobutton_text_custom),
					reminder.getRingtoneTitle(activity));
			radioButtonCustom.setChecked(true);
			isRadioButtonCustomChecked = true;
		}
		String defaultRingtoneTitle = task.getRingtoneTitle(activity, preferenceId);
		radioButtonDefault.setText(String.format(resources
				.getString(R.string.activity_edit_reminder_radiobutton_text_from_task),
				defaultRingtoneTitle));
		radioButtonCustom.setText(textCustom);
		radioButtonDefault.setOnCheckedChangeListener(checkedChangeListener);
		radioButtonCustom.setOnClickListener(clickListener);
		return isRadioButtonCustomChecked;
	}

	public static void setupRadiogroupForBooleanValue(Activity activity,
			int radioButtonDefaultId, int radioButtonCustomId, int checkBoxId,
			int preferenceKeyId, int defaultValueId, int checkedBeingStringId,
			int uncheckedBeingStringId, int radioButtonDefaultTextId,
			int radioButtonCustomTextId, int stringCustomId,
			int linearlayoutCustomSegmentId, Boolean inheritedBooleanWrapper,
			Boolean booleanWrapper, OnCheckedChangeListener listener,
			boolean isFromPreference) {
		Resources resources = activity.getResources();
		RadioButton radioButtonDefault = (RadioButton) activity
				.findViewById(radioButtonDefaultId);
		RadioButton radioButtonCustom = (RadioButton) activity
				.findViewById(radioButtonCustomId);
		CheckBox checkBox = (CheckBox) activity.findViewById(checkBoxId);
		boolean inheritedValue;
		if (isFromPreference || inheritedBooleanWrapper == null) {
			inheritedValue = Helper.getBooleanPreferenceValue(activity, preferenceKeyId,
					defaultValueId);
		} else {
			inheritedValue = inheritedBooleanWrapper.booleanValue();
		}
		String summaryTextDefault = Helper.getBooleanPreferenceSummary(activity,
				inheritedValue, checkedBeingStringId, uncheckedBeingStringId);
		radioButtonDefault.setText(String.format(
				resources.getString(radioButtonDefaultTextId), summaryTextDefault));
		String textCustom;
		LinearLayout linearlayoutCustomSegment = (LinearLayout) activity
				.findViewById(linearlayoutCustomSegmentId);
		if (booleanWrapper == null) {
			linearlayoutCustomSegment.setVisibility(View.GONE);
			textCustom = resources.getString(stringCustomId);
			radioButtonDefault.setChecked(true);
			checkBox.setChecked(inheritedValue);
		} else {
			String summaryTextCustom = Helper.getBooleanPreferenceSummary(activity,
					booleanWrapper.booleanValue(), checkedBeingStringId,
					uncheckedBeingStringId);
			textCustom = String.format(resources.getString(radioButtonCustomTextId),
					summaryTextCustom);
			radioButtonCustom.setChecked(true);
			checkBox.setChecked(booleanWrapper.booleanValue());
			linearlayoutCustomSegment.setVisibility(View.VISIBLE);
		}
		radioButtonCustom.setText(textCustom);
		checkBox.setOnCheckedChangeListener(listener);
		radioButtonDefault.setOnCheckedChangeListener(listener);
		radioButtonCustom.setOnCheckedChangeListener(listener);
	}

	public static EditTextValueChangedListener setupRadiogroupForModifiedIntegerValueWithResourceBounds(
			Activity activity, int textviewMeasurementUnitId, int radioButtonDefaultId,
			int radioButtonCustomId, int editTextId, int preferenceKeyId, int dividerId,
			int defaultValueId, int minValueId, int maxValueId, int singularStringId,
			int pluralStringId, int singularSummaryStringId, int pluralSummaryStringId,
			int radioButtonDefaultTextId, int radioButtonCustomTextId,
			int stringCustomId, Set<WantingItem> wantingItemSet,
			WantingItem wantingItemValueToBeGreaterThanOrEqualTo,
			WantingItem wantingItemValueToBeLessThanOrEqualTo,
			WantingItem wantingItemValueToBeWithinBounds,
			int linearlayoutCustomSegmentId, Long inheritedLongWrapper, Long longWrapper,
			OnCheckedChangeListener onCheckedChangeListener, boolean isFromPreference) {
		Resources resources = activity.getResources();
		int defaultValue = resources.getInteger(defaultValueId);
		int minValue = resources.getInteger(minValueId);
		int maxValue = resources.getInteger(maxValueId);
		int divider = resources.getInteger(dividerId);
		int modifiedDefaultValue = defaultValue / divider;
		int modifiedMinValue = minValue / divider;
		int modifiedMaxValue = maxValue / divider;
		Integer inheritedIntegerWrapper;
		if (inheritedLongWrapper != null) {
			inheritedIntegerWrapper = (int) (inheritedLongWrapper.longValue() / divider);
		} else {
			inheritedIntegerWrapper = null;
		}
		Integer integerWrapper;
		if (longWrapper != null) {
			integerWrapper = (int) (longWrapper.longValue() / divider);
		} else {
			integerWrapper = null;
		}
		EditTextValueChangedListener editTextValueChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithIntegerBounds(activity,
						textviewMeasurementUnitId, radioButtonDefaultId,
						radioButtonCustomId, editTextId, preferenceKeyId, divider,
						modifiedDefaultValue, modifiedMinValue, modifiedMaxValue,
						singularStringId, pluralStringId, singularSummaryStringId,
						pluralSummaryStringId, radioButtonDefaultTextId,
						radioButtonCustomTextId, stringCustomId, wantingItemSet,
						wantingItemValueToBeGreaterThanOrEqualTo,
						wantingItemValueToBeLessThanOrEqualTo,
						wantingItemValueToBeWithinBounds, linearlayoutCustomSegmentId,
						inheritedIntegerWrapper, integerWrapper, onCheckedChangeListener,
						isFromPreference);
		editTextValueChangedListener.setDivider(divider);
		return editTextValueChangedListener;
	}

	public static EditTextValueChangedListener setupRadiogroupForIntegerValueWithResourceBounds(
			Activity activity, int textviewMeasurementUnitId, int radioButtonDefaultId,
			int radioButtonCustomId, int editTextId, int preferenceKeyId,
			int defaultValueId, int minValueId, int maxValueId, int singularStringId,
			int pluralStringId, int singularSummaryStringId, int pluralSummaryStringId,
			int radioButtonDefaultTextId, int radioButtonCustomTextId,
			int stringCustomId, Set<WantingItem> wantingItemSet,
			WantingItem wantingItemValueToBeGreaterThanOrEqualTo,
			WantingItem wantingItemValueToBeLessThanOrEqualTo,
			WantingItem wantingItemValueToBeWithinBounds,
			int linearlayoutCustomSegmentId, Integer inheritedIntegerWrapper,
			Integer integerWrapper, OnCheckedChangeListener onCheckedChangeListener,
			boolean isFromPreference) {
		Resources resources = activity.getResources();
		int defaultValue = resources.getInteger(defaultValueId);
		int minValue = resources.getInteger(minValueId);
		int maxValue = resources.getInteger(maxValueId);
		EditTextValueChangedListener editTextValueChangedListener = EditTextValueChangedListener
				.setupRadiogroupForIntegerValueWithIntegerBounds(activity,
						textviewMeasurementUnitId, radioButtonDefaultId,
						radioButtonCustomId, editTextId, preferenceKeyId, 1,
						defaultValue, minValue, maxValue, singularStringId,
						pluralStringId, singularSummaryStringId, pluralSummaryStringId,
						radioButtonDefaultTextId, radioButtonCustomTextId,
						stringCustomId, wantingItemSet,
						wantingItemValueToBeGreaterThanOrEqualTo,
						wantingItemValueToBeLessThanOrEqualTo,
						wantingItemValueToBeWithinBounds, linearlayoutCustomSegmentId,
						inheritedIntegerWrapper, integerWrapper, onCheckedChangeListener,
						isFromPreference);
		return editTextValueChangedListener;
	}

	public static EditTextValueChangedListener setupRadiogroupForIntegerValueWithIntegerBounds(
			Activity activity, int textviewMeasurementUnitId, int radioButtonDefaultId,
			int radioButtonCustomId, int editTextId, int preferenceKeyId, int divider,
			int defaultValue, int minValue, int maxValue, int singularStringId,
			int pluralStringId, int singularSummaryStringId, int pluralSummaryStringId,
			int radioButtonDefaultTextId, int radioButtonCustomTextId,
			int stringCustomId, Set<WantingItem> wantingItemSet,
			WantingItem wantingItemValueToBeGreaterThanOrEqualTo,
			WantingItem wantingItemValueToBeLessThanOrEqualTo,
			WantingItem wantingItemValueToBeWithinBounds,
			int linearlayoutCustomSegmentId, Integer inheritedIntegerWrapper,
			Integer integerWrapper, OnCheckedChangeListener onCheckedChangeListener,
			boolean isFromPreference) {
		Resources resources = activity.getResources();
		RadioButton radioButtonDefault = (RadioButton) activity
				.findViewById(radioButtonDefaultId);
		RadioButton radioButtonCustom = (RadioButton) activity
				.findViewById(radioButtonCustomId);
		EditText editText = (EditText) activity.findViewById(editTextId);
		TextView textviewMeasurementUnit = (TextView) activity
				.findViewById(textviewMeasurementUnitId);
		editText.setTag(R.string.tag_key_value_must_be_greater_than_or_equal_to,
				wantingItemValueToBeGreaterThanOrEqualTo.getMessageId());
		editText.setTag(R.string.tag_key_value_must_be_less_than_or_equal_to,
				wantingItemValueToBeLessThanOrEqualTo.getMessageId());
		editText.setTag(R.string.tag_key_value_must_be_within_bounds,
				wantingItemValueToBeWithinBounds.getMessageId());
		EditTextValueChangedListener editTextValueChangedListener = new EditTextValueChangedListener(
				activity, editText, textviewMeasurementUnit, radioButtonCustom, divider,
				integerWrapper, minValue, maxValue, singularStringId, pluralStringId,
				singularSummaryStringId, pluralSummaryStringId, radioButtonCustomTextId,
				stringCustomId, wantingItemSet, wantingItemValueToBeGreaterThanOrEqualTo,
				wantingItemValueToBeLessThanOrEqualTo, wantingItemValueToBeWithinBounds);
		editText.addTextChangedListener(editTextValueChangedListener);
		int inheritedValue;
		if (isFromPreference || inheritedIntegerWrapper == null) {
			inheritedValue = Helper.getIntegerFromStringPreferenceValue(activity,
					preferenceKeyId, divider, defaultValue, minValue, maxValue);
		} else {
			inheritedValue = inheritedIntegerWrapper.intValue();
		}
		String summaryTextDefault = Helper.getIntegerPreferenceSummary(activity,
				inheritedValue, singularSummaryStringId, pluralSummaryStringId);
		radioButtonDefault.setText(String.format(
				resources.getString(radioButtonDefaultTextId), summaryTextDefault));
		String textCustom;
		LinearLayout linearlayoutCustomSegment = (LinearLayout) activity
				.findViewById(linearlayoutCustomSegmentId);
		if (integerWrapper == null) {
			linearlayoutCustomSegment.setVisibility(View.GONE);
			textCustom = resources.getString(stringCustomId);
			radioButtonDefault.setChecked(true);
			editText.setText(String.valueOf(inheritedValue));
		} else {
			String summaryTextCustom = Helper.getIntegerPreferenceSummary(activity,
					integerWrapper.intValue(), singularSummaryStringId,
					pluralSummaryStringId);
			textCustom = String.format(resources.getString(radioButtonCustomTextId),
					summaryTextCustom);
			radioButtonCustom.setChecked(true);
			editText.setText(integerWrapper.toString());
			linearlayoutCustomSegment.setVisibility(View.VISIBLE);
		}
		radioButtonCustom.setText(textCustom);
		radioButtonDefault.setOnCheckedChangeListener(onCheckedChangeListener);
		radioButtonCustom.setOnCheckedChangeListener(onCheckedChangeListener);
		return editTextValueChangedListener;
	}
}