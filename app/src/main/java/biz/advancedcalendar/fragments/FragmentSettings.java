package biz.advancedcalendar.fragments;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceActivity;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.widget.Toast;

import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityCalendarList;
import biz.advancedcalendar.activities.ActivityColorPicker;
import biz.advancedcalendar.activities.ActivityImportTasks;
import biz.advancedcalendar.activities.ActivityInformationComposer;
import biz.advancedcalendar.activities.MyPreference;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import biz.advancedcalendar.server.serialisers.InformationUnitSelectorSerializer;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.utils.DirectoryChooserDialog;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.accessories.InformationUnit;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitRow;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrderComparatorBySortOrder;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrdersHolder;

import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.OnMultipleTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.TimeAttribute;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On handset
 * devices, settings are presented as a single list. On tablets, settings are split by
 * category, with category headers shown to the left of the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html"> Android
 * Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings API
 * Guide</a> for more information on developing a Settings UI.
 */
public class FragmentSettings extends PreferenceFragmentCompat {
    private static String KEY_CALLER_ID = "KEY_CALLER_ID";
    private BroadcastReceiverForActivitySettings mReceiver;
    private ArrayList<ColorPickerCaller> colorPickerCallers;
    private ArrayList<RingtonePickerCaller> ringtonePickerCallers;
    private ColorPickerCallerComparatorByRequestCode colorPickerCallerComparatorByRequestCode;
    private ArrayList<InformationComposerCaller> informationComposerCallers;
    private InformationComposerCallerComparatorByRequestCode informationComposerCallerComparatorByRequestCode;
    private InformationUnitSortOrderComparatorBySortOrder informationUnitSortOrderComparatorBySortOrder;
    private RingtonePickerCallerComparatorByRequestCode ringtonePickerCallerComparatorByRequestCode;
    /**
     * Determines whether to always show the simplified settings UI, where settings are
     * presented in a single list. When false, settings are shown as a master/detail
     * two-pane view on tablets. When true, a single pane is shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;
    private static TimeZone timeZone = TimeZone.getTimeZone("GMT");

    static {
        Helper.timeFormat.setTimeZone(FragmentSettings.timeZone);
    }

    private OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;
    private RecyclerView mListView;
    private LayoutManager mLayoutManager;
    private static final String KEY_LIST_STATE = "KEY_LIST_STATE";
    private Parcelable mListState;
    private int mCallerId;
    private OnPreferenceClickListener onPreferenceClickListener;

    private class IntegerEditTextPreferenceClickListener implements
            OnPreferenceClickListener {
        private Context context;
        private int dividerId;
        private int defaultValue;
        private int minValue;
        private int maxValue;
        private int defaultValueId;
        private int minValueId;
        private int maxValueId;
        private Resources resources;
        private int divider;
        private SharedPreferences sharedPreferences;

        public IntegerEditTextPreferenceClickListener(Context context, int dividerId,
                                                      int defaultValueId, int minValueId, int maxValueId) {
            this.context = context;
            this.dividerId = dividerId;
            this.defaultValueId = defaultValueId;
            this.minValueId = minValueId;
            this.maxValueId = maxValueId;
            initialize();
        }

        private void initialize() {
            resources = context.getResources();
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            divider = resources.getInteger(dividerId);
            defaultValue = resources.getInteger(defaultValueId);
            minValue = resources.getInteger(minValueId);
            maxValue = resources.getInteger(maxValueId);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            String stringValue = Helper.getStringPreferenceValue(context, key, null);
            int value = Helper.getIntegerFromStringValue(stringValue, defaultValue,
                    minValue, maxValue);
            int minutes = value / divider;
            sharedPreferences
                    .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            editTextPreference.setText(String.valueOf(minutes));
            Editor editor = sharedPreferences.edit();
            editor.putString(key, stringValue);
            editor.commit();
            sharedPreferences
                    .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
            return false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(FragmentSettings.KEY_LIST_STATE, mListState);
        outState.putInt(FragmentSettings.KEY_CALLER_ID, mCallerId);
    }

    // @Override
    // protected void onRestoreInstanceState(Bundle state) {
    // super.onRestoreInstanceState(state);
    // listState = state.getParcelable(ActivitySettings.KEY_LIST_STATE);
    // }
    // @Override
    // protected void onPostCreate(Bundle savedInstanceState) {
    // super.onPostCreate(savedInstanceState);
    // // final android.app.ActionBar actionBar = getActionBar();
    // // actionBar.setHomeButtonEnabled(true);
    // setupSimplePreferencesScreen();
    // }
    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(
                        mOnSharedPreferenceChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentActivity activity = getActivity();
        if (activity.isFinishing()) {
            LocalBroadcastManager.getInstance(activity.getApplicationContext())
                    .unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Use instance field for listener
        // It will not be gc'd as long as this instance is kept referenced
        final FragmentActivity activity = getActivity();
        mOnSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(getResources().getString(
                        R.string.preference_key_first_day_of_week))) {
                    //
                    final Intent serviceIntent = new Intent(activity, AlarmService.class);
                    serviceIntent
                            .setAction(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED);
                    activity.startService(serviceIntent);
                    //
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(
                            new Intent(CommonConstants.ACTION_FIRST_DAY_OF_WEEK_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_month_recurrence_mode))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_MONTH_RECURRENCE_MODE_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_time_picker_time_format))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_start_time))) {
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(
                            new Intent(CommonConstants.ACTION_START_TIME_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_task_unset_color))) {
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(
                            new Intent(CommonConstants.ACTION_TASK_UNSET_COLOR_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_information_unit_matrix_for_task_tree))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE_CHANGED));
                } else if (key
                        .equals(getResources()
                                .getString(
                                        R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED));
                } else if (key
                        .equals(getResources()
                                .getString(
                                        R.string.preference_key_information_unit_matrix_for_calendar_text_mode))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_information_unit_matrix_for_agenda))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_INFORMATION_UNIT_MATRIX_FOR_AGENDA_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_business_hours_start_time))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_BUSINESS_HOURS_START_TIME_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_business_hours_end_time))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_BUSINESS_HOURS_END_TIME_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_business_hours_task_displaying_policy))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_BUSINESS_HOURS_TASK_DISPLAYING_POLICY_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_hours_ruler_displaying_policy))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_HOURS_RULER_DISPLAYING_POLICY_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_start_time_required_action))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_START_TIME_REQUIRED_ACTION_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_ringtone_fade_in_time))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_RINGTONE_FADE_IN_TIME_CHANGED));
                } else if (key
                        .equals(getResources()
                                .getString(
                                        R.string.preference_key_reminders_popup_window_displaying_duration))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_automatic_snooze_duration))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_AUTOMATIC_SNOOZE_DURATION_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_automatic_snoozes_max_count))) {
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_AUTOMATIC_SNOOZES_MAX_COUNT_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_reminder_behavior_for_completed_task))) {
                    AlarmService.resetupRemindersOfTasks(activity, false, true);
                    // notify about changes
                    // if we didn't declared receivers in manifest then we have to use
                    // LocalBroadcastManager
                    // sendBroadcast(new Intent(
                    // CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
                    LocalBroadcastManager
                            .getInstance(activity)
                            .sendBroadcast(
                                    new Intent(
                                            CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_sync_policy))) {
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(
                            new Intent(CommonConstants.ACTION_SYNC_POLICY_CHANGED));
                } else if (key.equals(getResources().getString(
                        R.string.preference_key_mark_sync_needed))) {
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(
                            new Intent(CommonConstants.ACTION_MARK_SYNC_NEEDED_CHANGED));
                }
            }
        };
        PreferenceManager.getDefaultSharedPreferences(activity)
                .registerOnSharedPreferenceChangeListener(
                        mOnSharedPreferenceChangeListener);
    }

    /**
     * Shows the simplified settings UI if the device configuration if the device
     * configuration dictates that a simplified, single-pane UI should be shown.
     */
    private void setupSimplePreferencesScreen() {
        FragmentActivity activity = getActivity();
        if (!FragmentSettings.isSimplePreferences(activity)) {
            return;
        }
        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.
        // Add 'general' preferences.
        mReceiver = new BroadcastReceiverForActivitySettings(activity);
        addPreferencesFromResource(R.xml.pref_general);
        initializeCalendarsPreference((MyPreference) findPreference(getResources()
                .getString(R.string.preference_key_calendars)));
        FragmentSettings
                .initializeFirstDayOfWeekPreference(setupListPreference(R.string.preference_key_first_day_of_week));
        FragmentSettings
                .initializeTimeFormatPreference(setupListPreference(R.string.preference_key_time_picker_time_format));
        boolean is24HourFormat = Helper.is24HourFormat(activity);
        colorPickerCallerComparatorByRequestCode = new ColorPickerCallerComparatorByRequestCode();
        colorPickerCallers = new ArrayList<ColorPickerCaller>();
        ArrayList<ColorPickerCallerAttribute> colorPickerCallerAttributes = new ArrayList<ColorPickerCallerAttribute>();
        ColorPickerCallerAttribute colorPickerCallerAttribute = new ColorPickerCallerAttribute(
                R.string.preference_key_task_unset_color,
                R.color.task_unset_color_default_value,
                R.string.color_picker_radiobutton_text_task_unset_color, 1);
        colorPickerCallerAttributes.add(colorPickerCallerAttribute);
        initializeColorPickerCaller(CommonConstants.REQUEST_CODE_PICK_TASK_UNSET_COLOR,
                colorPickerCallerAttributes,
                R.string.color_picker_title_for_task_unset_color);
        colorPickerCallerAttributes = new ArrayList<ColorPickerCallerAttribute>();
        colorPickerCallerAttribute = new ColorPickerCallerAttribute(
                R.string.preference_key_calendar_today_date_text_color,
                R.color.calendar_today_date_text_color_default_value,
                R.string.color_picker_radiobutton_text_calendar_today_date_text_color, 1);
        colorPickerCallerAttributes.add(colorPickerCallerAttribute);
        initializeColorPickerCaller(
                CommonConstants.REQUEST_CODE_PICK_CALENDAR_TODAY_DATE_TEXT_COLOR,
                colorPickerCallerAttributes,
                R.string.color_picker_title_for_calendar_today_date_text_color);
        colorPickerCallerAttributes = new ArrayList<ColorPickerCallerAttribute>();
        colorPickerCallerAttribute = new ColorPickerCallerAttribute(
                R.string.preference_key_calendar_today_date_highlight_color,
                R.color.calendar_today_date_highlight_color_default_value,
                R.string.color_picker_radiobutton_text_calendar_today_date_highlight_color,
                1);
        colorPickerCallerAttributes.add(colorPickerCallerAttribute);
        initializeColorPickerCaller(
                CommonConstants.REQUEST_CODE_PICK_CALENDAR_TODAY_DATE_HIGHLIGHT_COLOR,
                colorPickerCallerAttributes,
                R.string.color_picker_title_for_calendar_today_date_highlight_color);
        informationComposerCallerComparatorByRequestCode = new InformationComposerCallerComparatorByRequestCode();
        informationUnitSortOrderComparatorBySortOrder = new InformationUnitSortOrderComparatorBySortOrder();
        informationComposerCallers = new ArrayList<InformationComposerCaller>();
        ArrayList<InformationComposerCallerAttribute> informationComposerCallerAttributes = new ArrayList<InformationComposerCallerAttribute>();
        InformationComposerCallerAttribute informationComposerCallerAttribute = new InformationComposerCallerAttribute(
                R.string.preference_key_information_unit_matrix_for_task_tree,
                R.string.preference_key_information_unit_sort_orders_holder_for_task_tree,
                R.string.information_unit_matrix_for_task_tree_default_value,
                R.string.information_unit_sort_orders_holder_for_task_tree_default_value,
                R.string.information_composer_radiobutton_text_task_tree, 1);
        informationComposerCallerAttributes.add(informationComposerCallerAttribute);
        initializeInformationComposerCaller(
                CommonConstants.REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE,
                informationComposerCallerAttributes,
                R.string.information_composer_title_for_task_tree);
        informationComposerCallerAttributes = new ArrayList<InformationComposerCallerAttribute>();
        informationComposerCallerAttribute = new InformationComposerCallerAttribute(
                R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode,
                R.string.preference_key_information_unit_sort_orders_holder_for_calendar_time_intervals_mode,
                R.string.information_unit_matrix_for_calendar_time_intervals_mode_default_value,
                R.string.information_unit_sort_orders_holder_for_calendar_time_intervals_mode_default_value,
                R.string.information_composer_radiobutton_text_calendar_time_intervals_mode,
                1);
        informationComposerCallerAttributes.add(informationComposerCallerAttribute);
        informationComposerCallerAttribute = new InformationComposerCallerAttribute(
                R.string.preference_key_information_unit_matrix_for_calendar_text_mode,
                R.string.preference_key_information_unit_sort_orders_holder_for_calendar_text_mode,
                R.string.information_unit_matrix_for_calendar_text_mode_default_value,
                R.string.information_unit_sort_orders_holder_for_calendar_text_mode_default_value,
                R.string.information_composer_radiobutton_text_calendar_text_mode, 2);
        informationComposerCallerAttributes.add(informationComposerCallerAttribute);
        initializeInformationComposerCaller(
                CommonConstants.REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_AND_TEXT_MODE,
                informationComposerCallerAttributes,
                R.string.information_composer_title_for_calendar_time_intervals_and_text_mode);
        informationComposerCallerAttributes = new ArrayList<InformationComposerCallerAttribute>();
        informationComposerCallerAttribute = new InformationComposerCallerAttribute(
                R.string.preference_key_information_unit_matrix_for_agenda,
                R.string.preference_key_information_unit_sort_orders_holder_for_agenda,
                R.string.information_unit_matrix_for_agenda_default_value,
                R.string.information_unit_sort_orders_holder_for_agenda_default_value,
                R.string.information_composer_radiobutton_text_agenda, 1);
        informationComposerCallerAttributes.add(informationComposerCallerAttribute);
        initializeInformationComposerCaller(
                CommonConstants.REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_AGENDA,
                informationComposerCallerAttributes,
                R.string.information_composer_title_for_agenda);
        ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes = new ArrayList<TimePickerCallerAttribute>();
        TimePickerCallerAttribute timePickerCallerAttribute = new TimePickerCallerAttribute(
                R.string.preference_key_business_hours_start_time,
                R.integer.business_hours_start_time_default_value,
                R.integer.business_hours_start_time_min_value,
                R.integer.business_hours_start_time_max_value,
                R.string.time_picker_radiobutton_text_business_hours_start_time, 1,
                is24HourFormat, true);
        timePickerCallerAttributes.add(timePickerCallerAttribute);
        timePickerCallerAttribute = new TimePickerCallerAttribute(
                R.string.preference_key_business_hours_end_time,
                R.integer.business_hours_end_time_default_value,
                R.integer.business_hours_end_time_min_value,
                R.integer.business_hours_end_time_max_value,
                R.string.time_picker_radiobutton_text_business_hours_end_time, 2,
                is24HourFormat, true);
        timePickerCallerAttributes.add(timePickerCallerAttribute);
        initializeMultipleTimePickerCaller(timePickerCallerAttributes,
                R.string.time_picker_title_for_business_hours);
        timePickerCallerAttributes = new ArrayList<TimePickerCallerAttribute>();
        timePickerCallerAttribute = new TimePickerCallerAttribute(
                R.string.preference_key_silence_time_duration,
                R.integer.silence_time_duration_default_value,
                R.integer.silence_time_duration_min_value,
                R.integer.silence_time_duration_max_value,
                R.string.time_picker_radiobutton_text_silence_time_duration, 1, true,
                false);
        timePickerCallerAttributes.add(timePickerCallerAttribute);
        initializeMultipleTimePickerCaller(timePickerCallerAttributes,
                R.string.time_picker_title_for_silence_time_duration_default_value);
        timePickerCallerAttributes = new ArrayList<TimePickerCallerAttribute>();
        timePickerCallerAttribute = new TimePickerCallerAttribute(
                R.string.preference_key_start_time, R.integer.start_time_default_value,
                R.integer.start_time_min_value, R.integer.start_time_max_value,
                R.string.time_picker_radiobutton_text_start_time, 1, is24HourFormat, true);
        timePickerCallerAttributes.add(timePickerCallerAttribute);
        timePickerCallerAttribute = new TimePickerCallerAttribute(
                R.string.preference_key_task_duration,
                R.integer.task_duration_default_value, R.integer.task_duration_min_value,
                R.integer.task_duration_max_value,
                R.string.time_picker_radiobutton_text_task_duration, 2, true, false);
        timePickerCallerAttributes.add(timePickerCallerAttribute);
        initializeMultipleTimePickerCaller(timePickerCallerAttributes,
                R.string.time_picker_title_for_task_time_default_value);
        // initializeTimePreferenceClickListener(R.string.preference_key_start_time,
        // R.integer.start_time_default_value, R.integer.start_time_min_value,
        // R.integer.start_time_max_value);
        initializeIntegerEditTextPreference(R.string.preference_key_repetitions_duration,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.repetitions_duration_default_value,
                R.integer.repetitions_duration_min_value,
                R.integer.repetitions_duration_max_value,
                R.string.text_days_count_singular, R.string.text_days_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_days);
        setupListPreference(R.string.preference_key_business_hours_task_displaying_policy);
        // setupListPreference(R.string.preference_key_hours_ruler_displaying_policy);
        setupListPreference(R.string.preference_key_start_time_required_action);
        setupListPreference(R.string.preference_key_date_picker_default_selected_date);
        setupListPreference(R.string.preference_key_on_task_select_action);
        setupListPreference(R.string.preference_key_reminder_behavior_for_completed_task);
        setupListPreference(R.string.preference_key_time_unit);
        setupListPreference(R.string.preference_key_week_day_number);
        setupListPreference(R.string.preference_key_month_based_recurrences_date);
        setupListPreference(R.string.preference_key_month_recurrence_mode);
        initializeIntegerEditTextPreference(R.string.preference_key_time_units_count,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_default_value,
                R.integer.time_units_count_min_value,
                R.integer.time_units_count_max_value,
                R.string.pref_summary_time_units_count_singular,
                R.string.pref_summary_time_units_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_seconds);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_minutely_recurrent,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_minutely_recurrent_default_value,
                R.integer.time_units_count_minutely_recurrent_min_value,
                R.integer.time_units_count_minutely_recurrent_max_value,
                R.string.pref_summary_time_units_count_minutely_recurrent_singular,
                R.string.pref_summary_time_units_count_minutely_recurrent_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_minutes);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_hourly_recurrent,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_hourly_recurrent_default_value,
                R.integer.time_units_count_hourly_recurrent_min_value,
                R.integer.time_units_count_hourly_recurrent_max_value,
                R.string.text_hours_count_singular, R.string.text_hours_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_hours);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_daily_recurrent,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_daily_recurrent_default_value,
                R.integer.time_units_count_daily_recurrent_min_value,
                R.integer.time_units_count_daily_recurrent_max_value,
                R.string.text_days_count_singular, R.string.text_days_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_days);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_weekly_recurrent,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_weekly_recurrent_default_value,
                R.integer.time_units_count_weekly_recurrent_min_value,
                R.integer.time_units_count_weekly_recurrent_max_value,
                R.string.text_weeks_count_singular, R.string.text_weeks_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_weeks);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_monthly_recurrent_on_date,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_monthly_recurrent_on_date_default_value,
                R.integer.time_units_count_monthly_recurrent_on_date_min_value,
                R.integer.time_units_count_monthly_recurrent_on_date_max_value,
                R.string.text_months_count_singular, R.string.text_months_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_months);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_monthly_recurrent_on_nth_week_day,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_monthly_recurrent_on_nth_week_day_default_value,
                R.integer.time_units_count_monthly_recurrent_on_nth_week_day_min_value,
                R.integer.time_units_count_monthly_recurrent_on_nth_week_day_max_value,
                R.string.text_months_count_singular, R.string.text_months_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_months);
        initializeIntegerEditTextPreference(
                R.string.preference_key_time_units_count_yearly_recurrent,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.time_units_count_yearly_recurrent_default_value,
                R.integer.time_units_count_yearly_recurrent_min_value,
                R.integer.time_units_count_yearly_recurrent_max_value,
                R.string.text_years_count_singular, R.string.text_years_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_years);
        setupListPreference(R.string.preference_key_time_units_starting_index);
        setupListPreference(R.string.preference_key_task_copying_approach);
        initializeBackupPreference((MyPreference) findPreference(getResources()
                .getString(R.string.preference_key_backup_now)));
        initializeRestorePreference((MyPreference) findPreference(getResources()
                .getString(R.string.preference_key_restore)));
        // Add 'notifications' preferences, and a corresponding header.
        addPreferencesFromResource(R.xml.pref_notification);
        // PreferenceCategory fakeHeader = new PreferenceCategory(this);
        // fakeHeader.setTitle(R.string.pref_header_notifications);
        // getPreferenceScreen().addPreference(fakeHeader);
        setupListPreference(R.string.preference_key_alarm_method);
        setupListPreference(R.string.preference_key_reminder_time_mode);
        initializeIntegerEditTextPreference(
                R.string.preference_key_ringtone_fade_in_time,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.ringtone_fade_in_time_default_value,
                R.integer.ringtone_fade_in_time_min_value,
                R.integer.ringtone_fade_in_time_max_value,
                R.string.text_seconds_count_singular, R.string.text_seconds_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_seconds);
        initializeIntegerEditTextPreference(
                R.string.preference_key_reminders_popup_window_displaying_duration,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.reminders_popup_window_displaying_duration_default_value,
                R.integer.reminders_popup_window_displaying_duration_min_value,
                R.integer.reminders_popup_window_displaying_duration_max_value,
                R.string.text_seconds_count_singular, R.string.text_seconds_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_seconds);
        initializeIntegerEditTextPreference(
                R.string.preference_key_automatic_snooze_duration,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.automatic_snooze_duration_default_value,
                R.integer.automatic_snooze_duration_min_value,
                R.integer.automatic_snooze_duration_max_value,
                R.string.text_minutes_count_singular, R.string.text_minutes_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_minutes);
        initializeIntegerEditTextPreference(
                R.string.preference_key_automatic_snoozes_max_count,
                R.integer.ringtone_fade_in_time_divider,
                R.integer.automatic_snoozes_max_count_default_value,
                R.integer.automatic_snoozes_max_count_min_value,
                R.integer.automatic_snoozes_max_count_max_value,
                R.string.pref_summary_automatic_snoozes_max_count_singular,
                R.string.pref_summary_automatic_snoozes_max_count_plural,
                R.string.text_ringtone_fade_in_duration_zero, R.string.text_seconds);
        // setupIntegerEditTextPreference(
        // R.string.preference_key_max_number_of_elapsed_reminders,
        // R.integer.max_number_of_elapsed_reminders,
        // R.integer.max_number_of_elapsed_reminders_min_value,
        // R.integer.max_number_of_elapsed_reminders_max_value);
        // Add 'data and sync' preferences, and a corresponding header.
        // fakeHeader = new PreferenceCategory(this);
        // fakeHeader.setTitle(R.string.pref_header_data_sync);
        // getPreferenceScreen().addPreference(fakeHeader);
        // addPreferencesFromResource(R.xml.pref_data_sync);
        // setupListPreference(R.string.preference_key_sync_policy);
        // setupListPreference(R.string.preference_key_mark_sync_needed);
        // Preference forceSyncPref = findPreference(CommonConstants.force_sync);
        // forceSyncPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        // @Override
        // public boolean onPreferenceClick(Preference preference) {
        // // 1. Instantiate an AlertDialog.Builder with its
        // // constructor
        // AlertDialog.Builder builder = new AlertDialog.Builder(preference
        // .getContext());
        // // 2. Chain together various setter methods to set the
        // // dialog
        // // characteristics
        // builder.setMessage(R.string.pref_sync_settings_force_sync_alert_dialog_message);
        // // Add the buttons
        // builder.setPositiveButton(R.string.alert_dialog_ok,
        // new DialogInterface.OnClickListener() {
        // @Override
        // public void onClick(DialogInterface dialog, int id) {
        // // User clicked OK button
        // DataProvider
        // .markEntitiesForSynchronization(getApplicationContext());
        // LocalBroadcastManager
        // .getInstance(ActivitySettings.this)
        // .sendBroadcast(
        // new Intent(
        // CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
        // LocalBroadcastManager
        // .getInstance(ActivitySettings.this)
        // .sendBroadcast(
        // new Intent(
        // CommonConstants.ACTION_ENTITIES_CHANGED_LABELS));
        // LocalBroadcastManager
        // .getInstance(ActivitySettings.this)
        // .sendBroadcast(
        // new Intent(
        // CommonConstants.ACTION_ENTITIES_CHANGED_CONTACTS));
        // if (DataProvider.isSignedIn(getApplicationContext())) {
        // Intent serviceIntent = new Intent(
        // getApplicationContext(), SyncService.class);
        // serviceIntent
        // .putExtra(
        // CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
        // CommonConstants.INTENT_EXTRA_VALUE_SYNC_SERVICE_REQUEST_FORCE_SYNC);
        // // Start the service
        // startService(serviceIntent);
        // } else {
        // Intent intent = new Intent(getApplicationContext(),
        // ActivityLogin.class);
        // startActivity(intent);
        // // finish();
        // // return;
        // }
        // }
        // });
        // builder.setNegativeButton(R.string.alert_dialog_cancel, null);
        // // 3. Get the AlertDialog from create()
        // AlertDialog dialog = builder.create();
        // dialog.show();
        // return true;
        // }
        // });
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        ringtonePickerCallerComparatorByRequestCode = new RingtonePickerCallerComparatorByRequestCode();
        ringtonePickerCallers = new ArrayList<RingtonePickerCaller>();
        ArrayList<RingtonePickerCallerAttribute> ringtonePickerCallerAttributes = new ArrayList<RingtonePickerCallerAttribute>();
        RingtonePickerCallerAttribute ringtonePickerCallerAttribute = new RingtonePickerCallerAttribute(
                R.string.preference_key_alarm_ringtone,
                R.color.task_unset_color_default_value,
                R.string.color_picker_radiobutton_text_task_unset_color, 1,
                CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE);
        ringtonePickerCallerAttributes.add(ringtonePickerCallerAttribute);
        initializeRingtonePickerCaller(CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE,
                ringtonePickerCallerAttributes,
                R.string.time_picker_title_for_alarm_ringtone);
        ringtonePickerCallerAttributes = new ArrayList<RingtonePickerCallerAttribute>();
        ringtonePickerCallerAttribute = new RingtonePickerCallerAttribute(
                R.string.preference_key_notification_ringtone,
                R.color.task_unset_color_default_value,
                R.string.color_picker_radiobutton_text_task_unset_color, 1,
                CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE);
        ringtonePickerCallerAttributes.add(ringtonePickerCallerAttribute);
        initializeRingtonePickerCaller(
                CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE,
                ringtonePickerCallerAttributes,
                R.string.time_picker_title_for_alarm_ringtone);
        // R.string.preference_key_notification_ringtone);
    }

    private ListPreference setupListPreference(int preferenceId) {
        ListPreference listPreference = (ListPreference) findPreference(getResources()
                .getString(preferenceId));
        listPreference.setSummary(listPreference.getEntry());
        listPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        return listPreference;
    }

    private void initializeIntegerEditTextPreference(int preferenceId, int dividerId,
                                                     int defaultValueId, int minValueId, int maxValueId,
                                                     int singularSummaryStringId, int pluralSummaryStringId,
                                                     int textIdValueIsZero, int pluralStringId) {
        Resources resources = getResources();
        EditTextPreference editTextPreference = (EditTextPreference) findPreference(resources
                .getString(preferenceId));
        if (editTextPreference != null) {
            FragmentActivity context = getActivity();
            String stringValue = editTextPreference.getText();
            int value = Helper.getIntegerFromStringValue(stringValue,
                    resources.getInteger(defaultValueId),
                    resources.getInteger(minValueId), resources.getInteger(maxValueId));
            String text;
            switch (preferenceId) {
                case R.string.preference_key_automatic_snoozes_max_count:
                    text = Helper.getIntegerPreferenceSummaryFromInteger(context, value,
                            singularSummaryStringId, pluralSummaryStringId);
                    break;
                default:
                    String dialogTitle = String.format(editTextPreference.getDialogTitle()
                            .toString(), resources.getString(pluralStringId));
                    editTextPreference.setDialogTitle(dialogTitle);
                    if (preferenceId == R.string.preference_key_ringtone_fade_in_time) {
                        int divider = resources.getInteger(dividerId);
                        int modifiedValue = value / divider;
                        if (modifiedValue == 0) {
                            text = resources.getString(textIdValueIsZero);
                        } else {
                            text = Helper.getTextForTimeInterval(context, value);
                        }
                        onPreferenceClickListener = new IntegerEditTextPreferenceClickListener(
                                context, dividerId, defaultValueId, minValueId, maxValueId);
                        editTextPreference
                                .setOnPreferenceClickListener(onPreferenceClickListener);
                    } else {
                        text = Helper.getIntegerPreferenceSummaryFromInteger(context, value,
                                singularSummaryStringId, pluralSummaryStringId);
                    }
                    break;
            }
            editTextPreference.setSummary(text);
            Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putString(editTextPreference.getKey(), String.valueOf(value));
            editor.commit();
            editTextPreference.setOnPreferenceChangeListener(onPreferenceChangeListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    // @Override
    public boolean onIsMultiPane() {
        FragmentActivity activity = getActivity();
        return FragmentSettings.isXLargeTablet(activity)
                && !FragmentSettings.isSimplePreferences(activity);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For example,
     * 10" tablets are extra-large.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is true if this
     * is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device doesn't have newer APIs
     * like {@link PreferenceFragment}, or the device doesn't have an extra-large screen.
     * In these cases, a single-pane "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return FragmentSettings.ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !FragmentSettings.isXLargeTablet(context);
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        if (!FragmentSettings.isSimplePreferences(getActivity())) {
            // loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    /**
     * A preference value change listener that updates the preference's summary to reflect
     * its new value.
     */
    private OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean isPreferenceValid = true;
            String stringValue = newValue.toString();
            Context context = preference.getContext();
            Resources resources = context.getResources();
            String key = preference.getKey();
            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int newIndex = listPreference.findIndexOfValue(stringValue);
                preference
                        .setSummary(newIndex >= 0 ? listPreference.getEntries()[newIndex]
                                : null);
            } else if (preference instanceof ListPreference) {
                String preferenceKeyAlarmRingtone = context.getResources().getString(
                        R.string.preference_key_alarm_ringtone);
                int preferenceId;
                if (key.equals(preferenceKeyAlarmRingtone)) {
                    preferenceId = R.string.preference_key_alarm_ringtone;
                } else {
                    preferenceId = R.string.preference_key_notification_ringtone;
                }
                String ringtoneTitle = Helper.getRingtoneTitle(context, stringValue);
                preference.setSummary(ringtoneTitle);
            } else if (preference instanceof EditTextPreference) {
                if (key.equals(resources
                        .getString(R.string.preference_key_time_units_count))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_default_value,
                            R.integer.time_units_count_min_value,
                            R.integer.time_units_count_max_value,
                            R.string.pref_summary_time_units_count_singular,
                            R.string.pref_summary_time_units_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_minutely_recurrent))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_minutely_recurrent_default_value,
                            R.integer.time_units_count_minutely_recurrent_min_value,
                            R.integer.time_units_count_minutely_recurrent_max_value,
                            R.string.text_minutes_count_singular,
                            R.string.text_minutes_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_minutely_recurrent);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_hourly_recurrent))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_hourly_recurrent_default_value,
                            R.integer.time_units_count_hourly_recurrent_min_value,
                            R.integer.time_units_count_hourly_recurrent_max_value,
                            R.string.text_hours_count_singular,
                            R.string.text_hours_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_hourly_recurrent);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_daily_recurrent))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_daily_recurrent_default_value,
                            R.integer.time_units_count_daily_recurrent_min_value,
                            R.integer.time_units_count_daily_recurrent_max_value,
                            R.string.text_days_count_singular,
                            R.string.text_days_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_daily_recurrent);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_weekly_recurrent))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_weekly_recurrent_default_value,
                            R.integer.time_units_count_weekly_recurrent_min_value,
                            R.integer.time_units_count_weekly_recurrent_max_value,
                            R.string.text_weeks_count_singular,
                            R.string.text_weeks_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_weekly_recurrent);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_monthly_recurrent_on_date))) {
                    isPreferenceValid = showIntegerEditTextPreference(
                            preference,
                            stringValue,
                            resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_monthly_recurrent_on_date_default_value,
                            R.integer.time_units_count_monthly_recurrent_on_date_min_value,
                            R.integer.time_units_count_monthly_recurrent_on_date_max_value,
                            R.string.text_months_count_singular,
                            R.string.text_months_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_monthly_recurrent_on_date);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_monthly_recurrent_on_nth_week_day))) {
                    isPreferenceValid = showIntegerEditTextPreference(
                            preference,
                            stringValue,
                            resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_monthly_recurrent_on_nth_week_day_default_value,
                            R.integer.time_units_count_monthly_recurrent_on_nth_week_day_min_value,
                            R.integer.time_units_count_monthly_recurrent_on_nth_week_day_max_value,
                            R.string.text_months_count_singular,
                            R.string.text_months_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_monthly_recurrent_on_nth_week_day);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_time_units_count_yearly_recurrent))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.time_units_count_yearly_recurrent_default_value,
                            R.integer.time_units_count_yearly_recurrent_min_value,
                            R.integer.time_units_count_yearly_recurrent_max_value,
                            R.string.text_years_count_singular,
                            R.string.text_years_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_time_units_count_yearly_recurrent);
                } else if (key.equals(resources
                        .getString(R.string.preference_key_ringtone_fade_in_time))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.ringtone_fade_in_time_default_value,
                            R.integer.ringtone_fade_in_time_min_value,
                            R.integer.ringtone_fade_in_time_max_value,
                            R.string.text_seconds_count_singular,
                            R.string.text_seconds_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_ringtone_fade_in_time);
                } else if (key
                        .equals(resources
                                .getString(R.string.preference_key_reminders_popup_window_displaying_duration))) {
                    isPreferenceValid = showIntegerEditTextPreference(
                            preference,
                            stringValue,
                            resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.reminders_popup_window_displaying_duration_default_value,
                            R.integer.reminders_popup_window_displaying_duration_min_value,
                            R.integer.reminders_popup_window_displaying_duration_max_value,
                            R.string.text_seconds_count_singular,
                            R.string.text_seconds_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_reminders_popup_window_displaying_duration);
                } else if (key.equals(resources
                        .getString(R.string.preference_key_automatic_snooze_duration))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.automatic_snooze_duration_default_value,
                            R.integer.automatic_snooze_duration_min_value,
                            R.integer.automatic_snooze_duration_max_value,
                            R.string.text_minutes_count_singular,
                            R.string.text_minutes_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_automatic_snooze_duration);
                } else if (key.equals(resources
                        .getString(R.string.preference_key_automatic_snoozes_max_count))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.automatic_snoozes_max_count_default_value,
                            R.integer.automatic_snoozes_max_count_min_value,
                            R.integer.automatic_snoozes_max_count_max_value,
                            R.string.pref_summary_automatic_snoozes_max_count_singular,
                            R.string.pref_summary_automatic_snoozes_max_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_automatic_snoozes_max_count);
                } else if (key.equals(resources
                        .getString(R.string.preference_key_repetitions_duration))) {
                    isPreferenceValid = showIntegerEditTextPreference(preference,
                            stringValue, resources,
                            R.integer.ringtone_fade_in_time_divider,
                            R.integer.repetitions_duration_default_value,
                            R.integer.repetitions_duration_min_value,
                            R.integer.repetitions_duration_max_value,
                            R.string.text_days_count_singular,
                            R.string.text_days_count_plural,
                            R.string.text_ringtone_fade_in_duration_zero,
                            R.string.preference_key_repetitions_duration);
                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return isPreferenceValid;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the preference's
     * value is changed, its summary (line of text below the preference title) is updated
     * to reflect the value. The summary is also immediately updated upon calling this
     * method. The exact display format is dependent on the type of preference.
     *
     * @see #onPreferenceChangeListener
     */
    // private void setupRingtonePreference(Context context, int preferenceId) {
    // Preference preference = findPreference(getResources().getString(preferenceId));
    // String ringtoneString = Helper
    // .getStringPreferenceValue(
    // context,
    // preferenceId,
    // RingtoneManager
    // .getDefaultUri(
    // preferenceId == R.string.preference_key_alarm_ringtone ? RingtoneManager.TYPE_ALARM
    // : RingtoneManager.TYPE_NOTIFICATION)
    // .toString());
    // String ringtoneTitle = Helper.getRingtoneTitle(context, preferenceId,
    // ringtoneString);
    // preference.setSummary(ringtoneTitle);
    // preference
    // .setOnPreferenceChangeListener(FragmentSettings.sBindPreferenceSummaryToValueListener);
    // }
    private void initializeCalendarsPreference(
            biz.advancedcalendar.activities.MyPreference preference) {
        List<biz.advancedcalendar.greendao.Calendar> mCalendars =
                new ArrayList<biz.advancedcalendar.greendao.Calendar>(DataProvider
                        .getCalendars(null, getActivity()));
        mCalendars.add(0, new biz.advancedcalendar.greendao.Calendar(null, null,
                getResources().getString(R.string.default_calendar_name)));
        String summary = "";
        for (biz.advancedcalendar.greendao.Calendar calendar : mCalendars) {
            summary += calendar.getName() + ", ";
        }
        summary = summary.substring(0, summary.length() - 2);
        preference.setSummary(summary);
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(getActivity(), ActivityCalendarList.class);
                startActivityForResult(intent,
                        CommonConstants.REQUEST_CODE_EDIT_CALENDARS);
                return true;
            }
        });
    }

    private void initializeBackupPreference(
            biz.advancedcalendar.activities.MyPreference preference) {
        // Resources resources = getResources();
        // String stringValue = Helper
        // .getStringPreferenceValue(
        // getActivity(),
        // R.string.preference_key_backup_now_last_used_path,
        // resources
        // .getString(R.string.preference_key_backup_now_last_used_path_default_value));
        // String summary = stringValue;
        // preference.setSummary(summary);
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            private String m_chosenDir = "";
            private boolean m_newFolderEnabled = true;

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                // Create DirectoryChooserDialog and register a callback
                DirectoryChooserDialog directoryChooserDialog = new DirectoryChooserDialog(
                        getActivity(),
                        new DirectoryChooserDialog.ChosenDirectoryListener() {
                            @Override
                            public void onChosenDir(String chosenDir) {
                                m_chosenDir = chosenDir;
                                PreferenceManager
                                        .getDefaultSharedPreferences(getActivity())
                                        .edit()
                                        .putString(
                                                getResources()
                                                        .getString(
                                                                R.string.preference_key_backup_now_last_used_path),
                                                chosenDir).commit();
                                java.io.File database = getActivity()
                                        .getApplicationContext().getDatabasePath(
                                                "AdvancedCalendar.db");
                                Calendar calendar = Calendar.getInstance();
                                int year = calendar.get(Calendar.YEAR);
                                int month = calendar.get(Calendar.MONTH);
                                int day = calendar.get(Calendar.DAY_OF_MONTH);
                                String backupFilename = String.format(getResources()
                                        .getString(R.string.app_name)
                                        + "_backup_%s_%s_%s.db", year, month, day);
                                boolean successfullyCopied = false;
                                boolean foundFreeFileName = false;
                                File backupFile = null;
                                IOException resultException = null;
                                try {
                                    int i = 0;
                                    backupFile = new File(chosenDir, backupFilename);
                                    boolean created = backupFile.createNewFile();
                                    if (created) {
                                        foundFreeFileName = true;
                                    }
                                    while (!created) {
                                        if (i > 1000) {
                                            break;
                                        }
                                        i++;
                                        backupFilename = String.format(getResources()
                                                        .getString(R.string.app_name)
                                                        + "_backup_%d_%d_%d(%d).db", year, month,
                                                day, i);
                                        backupFile = new File(chosenDir, backupFilename);
                                        created = backupFile.createNewFile();
                                        if (created) {
                                            foundFreeFileName = true;
                                        }
                                    }
                                    if (foundFreeFileName) {
                                        Helper.copy(database, backupFile);
                                        successfullyCopied = true;
                                    }
                                } catch (IOException e) {
                                    successfullyCopied = false;
                                    resultException = e;
                                }
                                if (successfullyCopied) {
                                    Toast.makeText(
                                            getActivity(),
                                            getResources()
                                                    .getString(
                                                            R.string.toast_message_the_backup_file_was_successfully_created)
                                                    + "\n" + backupFilename,
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(
                                            getActivity(),
                                            getResources()
                                                    .getString(
                                                            R.string.toast_message_the_backup_file_was_not_created)
                                                    + (resultException == null ? ""
                                                    : "\n"
                                                    + resultException
                                                    .getLocalizedMessage()),
                                            Toast.LENGTH_LONG).show();
                                }
                                // initializeBackupPreference((MyPreference)
                                // findPreference(getResources()
                                // .getString(R.string.preference_key_backup_now)));
                            }
                        });
                // Toggle new folder button enabling
                directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory(m_chosenDir);
                m_newFolderEnabled = !m_newFolderEnabled;
                return true;
            }
        });
    }

    private void initializeRestorePreference(
            biz.advancedcalendar.activities.MyPreference preference) {
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(getActivity(), FileChooserActivity.class);
                startActivityForResult(intent, CommonConstants.REQUEST_CODE_CHOOSE_FILE);
                return true;
            }
        });
    }

    private static void initializeFirstDayOfWeekPreference(ListPreference listPreference) {
        CharSequence[] entries = listPreference.getEntries();
        CharSequence entryDefault = entries[0];
        entries[0] = String.format(entryDefault.toString(), entries[Calendar
                .getInstance().getFirstDayOfWeek()]);
        int index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(entries[index]);
    }

    // private static void showTime(Preference preference, long milliseconds) {
    // String timeString = Helper.timeFormat.format(new Date(milliseconds));
    // preference.setSummary(timeString);
    // }
    private class ColorPickerCallerAttribute implements
            Comparable<ColorPickerCallerAttribute> {
        private int callerId;
        private String callerKey;
        private int defaultValueId;
        private Preference preference;
        private int textId;
        private int color;
        private int ordinalNumber;

        public ColorPickerCallerAttribute(int callerId) {
            this.callerId = callerId;
        }

        public ColorPickerCallerAttribute(Preference preference) {
            callerKey = preference.getKey();
        }

        public ColorPickerCallerAttribute(int callerId, int defaultValueId, int textId,
                                          int ordinalNumber) {
            this.callerId = callerId;
            this.defaultValueId = defaultValueId;
            this.textId = textId;
            this.ordinalNumber = ordinalNumber;
            initialize();
        }

        private void initialize() {
            Resources resources = getResources();
            preference = findPreference(resources.getString(callerId));
            callerKey = preference.getKey();
            color = Helper.getIntegerPreferenceValue(getActivity(), callerKey,
                    resources.getInteger(defaultValueId), null, null);
        }

        @Override
        public int compareTo(ColorPickerCallerAttribute another) {
            if (ordinalNumber < another.ordinalNumber) {
                return -1;
            } else if (ordinalNumber > another.ordinalNumber) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class TimePickerCallerAttribute implements
            Comparable<TimePickerCallerAttribute> {
        private int callerId;
        private String callerKey;
        private int defaultValueId;
        private int minValueId;
        private int maxValueId;
        private Preference preference;
        private int textId;
        private long milliseconds;
        private int ordinalNumber;
        private boolean is24HourMode;
        private boolean isHourModeChangeable;

        public TimePickerCallerAttribute(Preference preference) {
            callerKey = preference.getKey();
        }

        public TimePickerCallerAttribute(int callerId, int defaultValueId,
                                         int minValueId, int maxValueId, int textId, int ordinalNumber,
                                         boolean is24HourMode, boolean isHourModeChangeable) {
            this.callerId = callerId;
            this.defaultValueId = defaultValueId;
            this.minValueId = minValueId;
            this.maxValueId = maxValueId;
            this.textId = textId;
            this.ordinalNumber = ordinalNumber;
            this.is24HourMode = is24HourMode;
            this.isHourModeChangeable = isHourModeChangeable;
            initialize();
        }

        private void initialize() {
            Resources resources = getResources();
            preference = findPreference(resources.getString(callerId));
            callerKey = preference.getKey();
            milliseconds = Helper.getLongPreferenceValue(getActivity(), callerKey,
                    resources.getInteger(defaultValueId),
                    (long) resources.getInteger(minValueId),
                    (long) resources.getInteger(maxValueId));
        }

        @Override
        public int compareTo(TimePickerCallerAttribute another) {
            if (ordinalNumber < another.ordinalNumber) {
                return -1;
            } else if (ordinalNumber > another.ordinalNumber) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class RingtonePickerCallerAttribute implements
            Comparable<RingtonePickerCallerAttribute> {
        private int callerId;
        private String callerKey;
        private int defaultValueId;
        private Preference preference;
        // private int textId;
        private String ringtone;
        private int ordinalNumber;
        private int requestCode;

        public RingtonePickerCallerAttribute(int callerId) {
            this.callerId = callerId;
        }

        public RingtonePickerCallerAttribute(Preference preference) {
            callerKey = preference.getKey();
        }

        public RingtonePickerCallerAttribute(int callerId, int defaultValueId,
                                             int textId, int ordinalNumber, int requestCode) {
            this.callerId = callerId;
            this.requestCode = requestCode;
            this.defaultValueId = defaultValueId;
            // this.textId = textId;
            this.ordinalNumber = ordinalNumber;
            initialize();
        }

        private void initialize() {
            Resources resources = getResources();
            preference = findPreference(resources.getString(callerId));
            callerKey = preference.getKey();
            // ringtone = Helper.getStringPreferenceValue(getActivity(),
            // resources.getInteger(defaultValueId), null);
            ringtone = Helper
                    .getStringPreferenceValue(
                            getActivity(),
                            callerKey,
                            RingtoneManager
                                    .getDefaultUri(
                                            defaultValueId == R.string.preference_key_alarm_ringtone ? RingtoneManager.TYPE_ALARM
                                                    : RingtoneManager.TYPE_NOTIFICATION)
                                    .toString());
        }

        @Override
        public int compareTo(RingtonePickerCallerAttribute another) {
            if (ordinalNumber < another.ordinalNumber) {
                return -1;
            } else if (ordinalNumber > another.ordinalNumber) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class MultipleTimeSetListener implements OnMultipleTimeSetListener {
        private ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes;

        public MultipleTimeSetListener(
                ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes) {
            this.timePickerCallerAttributes = timePickerCallerAttributes;
        }

        @Override
        public void onTimeSet(RadialPickerLayout view, Bundle bundle,
                              ArrayList<TimeAttribute> timeAttributes, int ordinalNumber) {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .edit();
            int size = timeAttributes.size();
            Collections.sort(timeAttributes);
            Collections.sort(timePickerCallerAttributes);
            for (int i = 0; i < size; i++) {
                TimeAttribute timeAttribute = timeAttributes.get(i);
                TimePickerCallerAttribute timePickerCallerAttribute = timePickerCallerAttributes
                        .get(i);
                long milliseconds = (timeAttribute.getHours() * 60 + timeAttribute
                        .getMinutes()) * 60 * 1000;
                timePickerCallerAttribute.milliseconds = milliseconds;
                editor.putLong(timePickerCallerAttribute.callerKey, milliseconds);
            }
            editor.commit();
            showTimes(timePickerCallerAttributes);
        }

        @Override
        public boolean isTimeConsistent(ArrayList<TimeAttribute> timeAttributes,
                                        Bundle bundle) {
            int callerId = bundle.getInt("callerId");
            switch (callerId) {
                case R.string.preference_key_silence_time_duration:
                    return true;
                case R.string.preference_key_business_hours_start_time:
                case R.string.preference_key_business_hours_end_time:
                    TimeAttribute startTimeAttribute = timeAttributes.get(0);
                    long startTimeMilliseconds = (startTimeAttribute.getHours() * 60 + startTimeAttribute
                            .getMinutes()) * 60 * 1000;
                    TimeAttribute endTimeAttribute = timeAttributes.get(1);
                    long endTimeMilliseconds = (endTimeAttribute.getHours() * 60 + endTimeAttribute
                            .getMinutes()) * 60 * 1000;
                    long timeIntervalMilliseconds = endTimeMilliseconds
                            - startTimeMilliseconds;
                    final Resources resources = getResources();
                    long timeIntervalMinValueMilliseconds = resources
                            .getInteger(R.integer.business_hours_time_interval_min_value);
                    long timeIntervalMinValueMinutes = timeIntervalMinValueMilliseconds / 1000 / 60;
                    long timeIntervalMinutes = timeIntervalMilliseconds / 1000 / 60;
                    long timeIntervalHours = timeIntervalMinutes / 60;
                    String text;
                    if (timeIntervalMinutes < timeIntervalMinValueMinutes) {
                        long timeIntervalMinValueHours = timeIntervalMinValueMinutes / 60;
                        long timeIntervalMinValueMinutesRemainder = timeIntervalMinValueMinutes % 60;
                        FragmentActivity activity = getActivity();
                        String textTimeMinValue = Helper.getTextForTimeInterval(activity, 0,
                                timeIntervalMinValueHours,
                                timeIntervalMinValueMinutesRemainder, 0);
                        if (timeIntervalMinutes <= 0) {
                            text = String
                                    .format(resources
                                                    .getString(R.string.toast_text_business_hours_start_time_must_be_less_than_business_hours_end_time),
                                            textTimeMinValue);
                        } else {
                            long timeIntervalMinutesRemainder = timeIntervalMinutes % 60;
                            String textTime = Helper.getTextForTimeInterval(activity, 0,
                                    timeIntervalHours, timeIntervalMinutesRemainder, 0);
                            text = String
                                    .format(resources
                                                    .getString(R.string.toast_text_time_interval_between_business_hours_start_time_and_business_hours_end_time_must_be_greater),
                                            textTimeMinValue, textTime);
                        }
                        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                default:
                    return true;
            }
        }
    }

    private class ColorPickerCallerComparatorByRequestCode implements
            Comparator<ColorPickerCaller>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(ColorPickerCaller lhs, ColorPickerCaller rhs) {
            if (lhs.requestCode < rhs.requestCode) {
                return -1;
            } else if (lhs.requestCode > rhs.requestCode) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class ColorPickerCallerAttributeComparatorByCallerId implements
            Comparator<ColorPickerCallerAttribute>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(ColorPickerCallerAttribute lhs, ColorPickerCallerAttribute rhs) {
            if (lhs.callerId < rhs.callerId) {
                return -1;
            } else if (lhs.callerId > rhs.callerId) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class InformationComposerCallerAttribute implements
            Comparable<InformationComposerCallerAttribute> {
        private int callerId;
        private int informationUnitSortOrdersHolderKeyId;
        private String callerKey;
        private String keyInformationUnitSortOrders;
        private int informationUnitMatrixDefaultValueId;
        private int informationUnitSortOrdersHolderDefaultValueId;
        private Preference preference;
        private int textId;
        private InformationUnitMatrix informationUnitMatrix;
        private InformationUnitSortOrdersHolder informationUnitSortOrdersHolder;
        private boolean isInformationUnitsSortOrderChanged;
        private int ordinalNumber;

        public InformationComposerCallerAttribute(int callerId) {
            this.callerId = callerId;
        }

        public InformationComposerCallerAttribute(Preference preference) {
            callerKey = preference.getKey();
        }

        public InformationComposerCallerAttribute(int callerId,
                                                  int informationUnitSortOrdersHolderKeyId,
                                                  int informationUnitMatrixDefaultValueId,
                                                  int informationUnitSortOrdersHolderDefaultValueId, int textId,
                                                  int ordinalNumber) {
            this.callerId = callerId;
            this.informationUnitSortOrdersHolderKeyId = informationUnitSortOrdersHolderKeyId;
            this.informationUnitMatrixDefaultValueId = informationUnitMatrixDefaultValueId;
            this.informationUnitSortOrdersHolderDefaultValueId = informationUnitSortOrdersHolderDefaultValueId;
            this.textId = textId;
            this.ordinalNumber = ordinalNumber;
            initialize();
        }

        private void initialize() {
            Resources resources = getResources();
            Context context = getActivity();
            preference = findPreference(resources.getString(callerId));
            callerKey = preference.getKey();
            keyInformationUnitSortOrders = resources
                    .getString(informationUnitSortOrdersHolderKeyId);
            informationUnitMatrix = Helper.createInformationUnitMatrix(context, callerId,
                    informationUnitMatrixDefaultValueId);
            informationUnitSortOrdersHolder = Helper
                    .createInformationUnitSortOrdersHolder(context,
                            informationUnitSortOrdersHolderKeyId,
                            informationUnitSortOrdersHolderDefaultValueId,
                            informationUnitSortOrderComparatorBySortOrder);
            isInformationUnitsSortOrderChanged = false;
            if (true) {
                // createInformationUnitMatrix();
            }
        }

        @SuppressWarnings("unused")
        private void createInformationUnitMatrix() {
            ArrayList<InformationUnitRow> informationUnitRows = new ArrayList<InformationUnitRow>();
            ArrayList<InformationUnit> informationUnits = new ArrayList<InformationUnit>();
            InformationUnit informationUnit = new InformationUnit(
                    InformationUnitSelector.CURRENT_SCHEDULED_START_TIME);
            informationUnits.add(informationUnit);
            informationUnit = new InformationUnit(InformationUnitSelector.ANY_STRING);
            informationUnit.setWhateverDelimiterString(" ");
            informationUnits.add(informationUnit);
            informationUnit = new InformationUnit(InformationUnitSelector.TASK_NAME);
            informationUnits.add(informationUnit);
            InformationUnitRow informationUnitRow = new InformationUnitRow(
                    informationUnits);
            informationUnitRows.add(informationUnitRow);
            informationUnitMatrix = new InformationUnitMatrix(informationUnitRows);
        }

        @Override
        public int compareTo(InformationComposerCallerAttribute another) {
            if (ordinalNumber < another.ordinalNumber) {
                return -1;
            } else if (ordinalNumber > another.ordinalNumber) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class InformationComposerCallerAttributeComparatorByCallerId implements
            Comparator<InformationComposerCallerAttribute>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(InformationComposerCallerAttribute lhs,
                           InformationComposerCallerAttribute rhs) {
            if (lhs.callerId < rhs.callerId) {
                return -1;
            } else if (lhs.callerId > rhs.callerId) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class InformationComposerCaller implements OnPreferenceClickListener {
        private int requestCode;
        private ArrayList<InformationComposerCallerAttribute> informationComposerCallerAttributes;
        private InformationComposerCallerAttributeComparatorByPreferenceKey informationComposerCallerAttributeComparatorByPreferenceKey;
        private InformationComposerCallerAttributeComparatorByCallerId informationComposerCallerAttributeComparatorByCallerId;

        public InformationComposerCaller(int requestCode) {
            this.requestCode = requestCode;
        }

        public InformationComposerCaller(
                int requestCode,
                ArrayList<InformationComposerCallerAttribute> informationComposerCallerAttributes) {
            this.requestCode = requestCode;
            this.informationComposerCallerAttributes = informationComposerCallerAttributes;
            initialize();
        }

        private void initialize() {
            informationComposerCallerAttributeComparatorByPreferenceKey = new InformationComposerCallerAttributeComparatorByPreferenceKey();
            informationComposerCallerAttributeComparatorByCallerId = new InformationComposerCallerAttributeComparatorByCallerId();
        }

        private class InformationComposerCallerAttributeComparatorByPreferenceKey
                implements Comparator<InformationComposerCallerAttribute>, Serializable {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(InformationComposerCallerAttribute lhs,
                               InformationComposerCallerAttribute rhs) {
                return lhs.callerKey.compareTo(rhs.callerKey);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            InformationComposerCallerAttribute informationComposerCallerAttribute = new InformationComposerCallerAttribute(
                    preference);
            Collections.sort(informationComposerCallerAttributes,
                    informationComposerCallerAttributeComparatorByPreferenceKey);
            int index = Collections.binarySearch(informationComposerCallerAttributes,
                    informationComposerCallerAttribute,
                    informationComposerCallerAttributeComparatorByPreferenceKey);
            informationComposerCallerAttribute = informationComposerCallerAttributes
                    .get(index);
            Intent intent = new Intent(getActivity(), ActivityInformationComposer.class);
            intent.putExtra(FragmentSettings.KEY_CALLER_ID,
                    informationComposerCallerAttribute.callerId);
            intent.putExtra(CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_MATRIX,
                    informationComposerCallerAttribute.informationUnitMatrix);
            intent.putExtra(
                    CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_SORT_ORDERS_HOLDER,
                    informationComposerCallerAttribute.informationUnitSortOrdersHolder);
            intent.putExtra(CommonConstants.INTENT_EXTRA_LAST_SELECTED_CHECKBOX_ID,
                    CommonConstants.INITIAL_VALUE_FOR_RECTANGULAR_CHECK_BOX_IDS);
            intent.putExtra(CommonConstants.INTENT_EXTRA_LAST_SELECTED_RADIOBUTTON_ID,
                    InformationUnitSelector.ANY_STRING.getValue());
            startActivityForResult(intent, requestCode);
            return true;
        }
    }

    private class InformationComposerCallerComparatorByRequestCode implements
            Comparator<InformationComposerCaller>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(InformationComposerCaller lhs, InformationComposerCaller rhs) {
            if (lhs.requestCode < rhs.requestCode) {
                return -1;
            } else if (lhs.requestCode > rhs.requestCode) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private String createInformationComposerSummary(
            InformationUnitMatrix informationUnitMatrix) {
        ArrayList<InformationUnitRow> informationUnitRows = informationUnitMatrix
                .getInformationUnitRows();
        StringBuilder stringBuilder = new StringBuilder();
        Resources resources = getResources();
        if (informationUnitRows != null) {
            int rowsCount = informationUnitRows.size();
            int previousSize = 0;
            for (int i = 0; i < rowsCount; i++) {
                InformationUnitRow informationUnitRow = informationUnitRows.get(i);
                ArrayList<InformationUnit> informationUnits = informationUnitRow
                        .getInformationUnits();
                if (informationUnits != null) {
                    int size = informationUnits.size();
                    if (size != 0) {
                        if (previousSize != 0) {
                            stringBuilder.append(String.format("%n"));
                        }
                        previousSize = size;
                    }
                    for (int j = 0; j < size; j++) {
                        InformationUnit informationUnit = informationUnits.get(j);
                        InformationUnitSelector informationUnitSelector = informationUnit
                                .getInformationUnitSelector();
                        if (informationUnitSelector != InformationUnitSelector.ANY_STRING) {
                            stringBuilder.append("(");
                            stringBuilder.append(resources
                                    .getString(informationUnitSelector.getTextId()));
                            stringBuilder.append(")");
                            stringBuilder.append(" ");
                        } else {
                            stringBuilder.append("(");
                            stringBuilder.append(informationUnit
                                    .getWhateverDelimiterString());
                            stringBuilder.append(")");
                        }
                    }
                }
            }
        }
        String text;
        if (stringBuilder.length() > 0) {
            text = stringBuilder.toString();
        } else {
            text = resources.getString(R.string.text_information_unit_matrix_unset);
        }
        return text;
    }

    private void showInformationUnitMatrix(
            ArrayList<InformationComposerCallerAttribute> informationComposerCallerAttributes) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        sharedPreferences
                .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        Editor editor = sharedPreferences.edit();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(InformationUnitSelector.class,
                new InformationUnitSelectorSerializer());
        Gson gson = gsonBuilder.create();
        for (InformationComposerCallerAttribute informationComposerCallerAttribute : informationComposerCallerAttributes) {
            int callerId = informationComposerCallerAttribute.callerId;
            String informationUnitMatrixString = gson.toJson(
                    informationComposerCallerAttribute.informationUnitMatrix,
                    InformationUnitMatrix.class);
            String informationUnitSortOrdersString = gson.toJson(
                    informationComposerCallerAttribute.informationUnitSortOrdersHolder,
                    InformationUnitSortOrdersHolder.class);
            String text;
            switch (callerId) {
                case R.string.preference_key_information_unit_matrix_for_task_tree:
                case R.string.preference_key_information_unit_matrix_for_calendar_time_intervals_mode:
                case R.string.preference_key_information_unit_matrix_for_calendar_text_mode:
                case R.string.preference_key_information_unit_matrix_for_agenda:
                    text = createInformationComposerSummary(informationComposerCallerAttribute.informationUnitMatrix);
                    editor.putString(informationComposerCallerAttribute.callerKey,
                            informationUnitMatrixString);
                    if (informationComposerCallerAttribute.isInformationUnitsSortOrderChanged) {
                        editor.putString(
                                informationComposerCallerAttribute.keyInformationUnitSortOrders,
                                informationUnitSortOrdersString);
                    }
                    break;
                default:
                    Resources resources = getResources();
                    text = resources.getString(R.string.text_information_unit_matrix_unset);
                    break;
            }
            informationComposerCallerAttribute.preference.setSummary(text);
        }
        editor.commit();
        sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    private void initializeInformationComposerCaller(
            int requestCode,
            ArrayList<InformationComposerCallerAttribute> informationComposerCallerAttributes,
            int titleId) {
        InformationComposerCaller informationComposerCaller = new InformationComposerCaller(
                requestCode, informationComposerCallerAttributes);
        informationComposerCallers.add(informationComposerCaller);
        int size = informationComposerCallerAttributes.size();
        for (int i = 0; i < size; i++) {
            InformationComposerCallerAttribute informationComposerCallerAttribute = informationComposerCallerAttributes
                    .get(i);
            informationComposerCallerAttribute.preference
                    .setOnPreferenceClickListener(informationComposerCaller);
        }
        showInformationUnitMatrix(informationComposerCallerAttributes);
    }

    private class RingtonePickerCallerComparatorByRequestCode implements
            Comparator<RingtonePickerCaller>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(RingtonePickerCaller lhs, RingtonePickerCaller rhs) {
            if (lhs.requestCode < rhs.requestCode) {
                return -1;
            } else if (lhs.requestCode > rhs.requestCode) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private class RingtonePickerCallerAttributeComparatorByCallerId implements
            Comparator<RingtonePickerCallerAttribute>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(RingtonePickerCallerAttribute lhs,
                           RingtonePickerCallerAttribute rhs) {
            if (lhs.callerId < rhs.callerId) {
                return -1;
            } else if (lhs.callerId > rhs.callerId) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CommonConstants.REQUEST_CODE_PICK_TASK_UNSET_COLOR:
            case CommonConstants.REQUEST_CODE_PICK_CALENDAR_TODAY_DATE_TEXT_COLOR:
            case CommonConstants.REQUEST_CODE_PICK_CALENDAR_TODAY_DATE_HIGHLIGHT_COLOR:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    ColorPickerCaller colorPickerCaller = new ColorPickerCaller(requestCode);
                    Collections.sort(colorPickerCallers,
                            colorPickerCallerComparatorByRequestCode);
                    int index = Collections.binarySearch(colorPickerCallers,
                            colorPickerCaller, colorPickerCallerComparatorByRequestCode);
                    colorPickerCaller = colorPickerCallers.get(index);
                    ArrayList<ColorPickerCallerAttribute> colorPickerCallerAttributes = colorPickerCaller.colorPickerCallerAttributes;
                    int callerId = data.getIntExtra(FragmentSettings.KEY_CALLER_ID, 0);
                    ColorPickerCallerAttribute colorPickerCallerAttribute = new ColorPickerCallerAttribute(
                            callerId);
                    ColorPickerCallerAttributeComparatorByCallerId colorPickerCallerAttributeComparatorByCallerId = colorPickerCaller.colorPickerCallerAttributeComparatorByCallerId;
                    Collections.sort(colorPickerCallerAttributes,
                            colorPickerCallerAttributeComparatorByCallerId);
                    index = Collections.binarySearch(colorPickerCallerAttributes,
                            colorPickerCallerAttribute,
                            colorPickerCallerAttributeComparatorByCallerId);
                    colorPickerCallerAttribute = colorPickerCallerAttributes.get(index);
                    int color = data.getIntExtra(
                            CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, 0);
                    colorPickerCallerAttribute.color = color;
                    showDataFromColorPicker(colorPickerCallerAttributes);
                } else if (resultCode == android.app.Activity.RESULT_CANCELED) {
                }
                break;
            case CommonConstants.REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_AGENDA:
            case CommonConstants.REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE:
            case CommonConstants.REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_AND_TEXT_MODE:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    InformationComposerCaller informationComposerCaller = new InformationComposerCaller(
                            requestCode);
                    Collections.sort(informationComposerCallers,
                            informationComposerCallerComparatorByRequestCode);
                    int index = Collections.binarySearch(informationComposerCallers,
                            informationComposerCaller,
                            informationComposerCallerComparatorByRequestCode);
                    informationComposerCaller = informationComposerCallers.get(index);
                    ArrayList<InformationComposerCallerAttribute> informationComposerCallerAttributes = informationComposerCaller.informationComposerCallerAttributes;
                    int callerId = data.getIntExtra(FragmentSettings.KEY_CALLER_ID, 0);
                    InformationComposerCallerAttribute informationComposerCallerAttribute = new InformationComposerCallerAttribute(
                            callerId);
                    InformationComposerCallerAttributeComparatorByCallerId informationComposerCallerAttributeComparatorByCallerId = informationComposerCaller.informationComposerCallerAttributeComparatorByCallerId;
                    Collections.sort(informationComposerCallerAttributes,
                            informationComposerCallerAttributeComparatorByCallerId);
                    index = Collections.binarySearch(informationComposerCallerAttributes,
                            informationComposerCallerAttribute,
                            informationComposerCallerAttributeComparatorByCallerId);
                    informationComposerCallerAttribute = informationComposerCallerAttributes
                            .get(index);
                    InformationUnitMatrix informationUnitMatrix = data
                            .getParcelableExtra(CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_MATRIX);
                    if (informationUnitMatrix != null) {
                        informationComposerCallerAttribute.informationUnitMatrix = informationUnitMatrix;
                    }
                    boolean isInformationUnitsSortOrderChanged = data
                            .getBooleanExtra(
                                    CommonConstants.INTENT_EXTRA_IS_INFORMATION_UNITS_SORT_ORDER_CHANGED,
                                    false);
                    informationComposerCallerAttribute.isInformationUnitsSortOrderChanged = isInformationUnitsSortOrderChanged;
                    InformationUnitSortOrdersHolder informationUnitSortOrdersHolder = data
                            .getParcelableExtra(CommonConstants.INTENT_EXTRA_INFORMATION_UNIT_SORT_ORDERS_HOLDER);
                    if (informationUnitSortOrdersHolder != null) {
                        informationComposerCallerAttribute.informationUnitSortOrdersHolder = informationUnitSortOrdersHolder;
                    }
                    showInformationUnitMatrix(informationComposerCallerAttributes);
                } else if (resultCode == android.app.Activity.RESULT_CANCELED) {
                }
                break;
            case CommonConstants.REQUEST_CODE_PICK_ALARM_RINGTONE:
            case CommonConstants.REQUEST_CODE_PICK_NOTIFICATION_RINGTONE:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    RingtonePickerCaller ringtonePickerCaller = new RingtonePickerCaller(
                            requestCode);
                    Collections.sort(ringtonePickerCallers,
                            ringtonePickerCallerComparatorByRequestCode);
                    int index = Collections
                            .binarySearch(ringtonePickerCallers, ringtonePickerCaller,
                                    ringtonePickerCallerComparatorByRequestCode);
                    ringtonePickerCaller = ringtonePickerCallers.get(index);
                    ArrayList<RingtonePickerCallerAttribute> ringtonePickerCallerAttributes = ringtonePickerCaller.ringtonePickerCallerAttributes;
                    int callerId = mCallerId;// data.getIntExtra(FragmentSettings.KEY_CALLER_ID,
                    // 0);
                    RingtonePickerCallerAttribute ringtonePickerCallerAttribute = new RingtonePickerCallerAttribute(
                            callerId);
                    RingtonePickerCallerAttributeComparatorByCallerId ringtonePickerCallerAttributeComparatorByCallerId = ringtonePickerCaller.ringtonePickerCallerAttributeComparatorByCallerId;
                    Collections.sort(ringtonePickerCallerAttributes,
                            ringtonePickerCallerAttributeComparatorByCallerId);
                    index = Collections.binarySearch(ringtonePickerCallerAttributes,
                            ringtonePickerCallerAttribute,
                            ringtonePickerCallerAttributeComparatorByCallerId);
                    ringtonePickerCallerAttribute = ringtonePickerCallerAttributes.get(index);
                    Uri uri = data
                            .getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    String ringtone;
                    if (uri != null) {
                        ringtone = uri.toString();
                    } else {
                        ringtone = "";
                    }
                    ringtonePickerCallerAttribute.ringtone = ringtone;
                    // // showRingtones(ringtonePickerCallerAttributes);
                    // //
                    // String ringtoneTitle = Helper.getRingtoneTitle(getActivity(),
                    // R.string.preference_key_alarm_ringtone, ringtone);
                    // ringtoneAttribute.preference.setSummary(ringtoneTitle);
                    showRingtones(ringtonePickerCallerAttributes);
                }
                break;
            case CommonConstants.REQUEST_CODE_EDIT_CALENDARS:
                initializeCalendarsPreference((MyPreference) findPreference(getResources()
                        .getString(R.string.preference_key_calendars)));
                break;
            case CommonConstants.REQUEST_CODE_CHOOSE_FILE:
                if (resultCode == android.app.Activity.RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(getActivity(), uri);
                            Intent intent = new Intent(getActivity(),
                                    ActivityImportTasks.class);
                            intent.putExtra("path", path);
                            startActivityForResult(intent,
                                    CommonConstants.REQUEST_CODE_EDIT_CALENDARS);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(),
                                    "File select error\n" + e.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private class ColorPickerCaller implements OnPreferenceClickListener {
        private int requestCode;
        private ArrayList<ColorPickerCallerAttribute> colorPickerCallerAttributes;
        private ColorPickerCallerAttributeComparatorByPreferenceKey colorPickerCallerAttributeComparatorByPreferenceKey;
        private ColorPickerCallerAttributeComparatorByCallerId colorPickerCallerAttributeComparatorByCallerId;

        public ColorPickerCaller(int requestCode) {
            this.requestCode = requestCode;
        }

        public ColorPickerCaller(int requestCode,
                                 ArrayList<ColorPickerCallerAttribute> colorPickerCallerAttributes) {
            this.requestCode = requestCode;
            this.colorPickerCallerAttributes = colorPickerCallerAttributes;
            initialize();
        }

        private void initialize() {
            colorPickerCallerAttributeComparatorByPreferenceKey = new ColorPickerCallerAttributeComparatorByPreferenceKey();
            colorPickerCallerAttributeComparatorByCallerId = new ColorPickerCallerAttributeComparatorByCallerId();
        }

        private class ColorPickerCallerAttributeComparatorByPreferenceKey implements
                Comparator<ColorPickerCallerAttribute>, Serializable {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(ColorPickerCallerAttribute lhs,
                               ColorPickerCallerAttribute rhs) {
                return lhs.callerKey.compareTo(rhs.callerKey);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            ColorPickerCallerAttribute colorPickerCallerAttribute = new ColorPickerCallerAttribute(
                    preference);
            Collections.sort(colorPickerCallerAttributes,
                    colorPickerCallerAttributeComparatorByPreferenceKey);
            int index = Collections.binarySearch(colorPickerCallerAttributes,
                    colorPickerCallerAttribute,
                    colorPickerCallerAttributeComparatorByPreferenceKey);
            colorPickerCallerAttribute = colorPickerCallerAttributes.get(index);
            Integer color = colorPickerCallerAttribute.color;
            Intent intent = new Intent(getActivity(), ActivityColorPicker.class);
            intent.putExtra(FragmentSettings.KEY_CALLER_ID,
                    colorPickerCallerAttribute.callerId);
            intent.putExtra(CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, color);
            startActivityForResult(intent, requestCode);
            return true;
        }
    }

    private class MultipleTimePickerCaller implements OnPreferenceClickListener {
        private ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes;
        private ArrayList<TimeAttribute> timeAttributes;
        private MultipleTimeSetListener multipleTimeSetListener;
        private int titleId;
        private CallerAttributeComparatorByPreferenceKey callerAttributeComparatorByPreferenceKey;

        public MultipleTimePickerCaller(
                ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes,
                MultipleTimeSetListener multipleTimeSetListener, int titleId) {
            this.timePickerCallerAttributes = timePickerCallerAttributes;
            this.multipleTimeSetListener = multipleTimeSetListener;
            this.titleId = titleId;
            initialize();
        }

        private void initialize() {
            callerAttributeComparatorByPreferenceKey = new CallerAttributeComparatorByPreferenceKey();
            timeAttributes = new ArrayList<TimeAttribute>(
                    timePickerCallerAttributes.size());
        }

        private class CallerAttributeComparatorByPreferenceKey implements
                Comparator<TimePickerCallerAttribute>, Serializable {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(TimePickerCallerAttribute lhs,
                               TimePickerCallerAttribute rhs) {
                return lhs.callerKey.compareTo(rhs.callerKey);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            resetTime();
            TimePickerCallerAttribute timePickerCallerAttribute = new TimePickerCallerAttribute(
                    preference);
            Collections.sort(timePickerCallerAttributes,
                    callerAttributeComparatorByPreferenceKey);
            int index = Collections.binarySearch(timePickerCallerAttributes,
                    timePickerCallerAttribute, callerAttributeComparatorByPreferenceKey);
            timePickerCallerAttribute = timePickerCallerAttributes.get(index);
            int ordinalNumber = timePickerCallerAttribute.ordinalNumber;
            Bundle bundle = new Bundle();
            bundle.putInt("callerId", timePickerCallerAttribute.callerId);
            TimePickerDialogMultiple tpd = TimePickerDialogMultiple.newInstance(
                    multipleTimeSetListener, bundle, titleId, timeAttributes,
                    ordinalNumber);
            tpd.show(getFragmentManager(), timePickerCallerAttribute.callerKey);
            return true;
        }

        private void resetTime() {
            int size = timeAttributes.size();
            Collections.sort(timeAttributes);
            Collections.sort(timePickerCallerAttributes);
            for (int i = 0; i < size; i++) {
                TimePickerCallerAttribute timePickerCallerAttribute = timePickerCallerAttributes
                        .get(i);
                int minutes = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60) % 60);
                int hours = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60 * 60));
                TimeAttribute timeAttribute = timeAttributes.get(i);
                timeAttribute.setHours(hours);
                timeAttribute.setMinutes(minutes);
                int amOrPm = hours < 12 ? TimePickerDialogMultiple.AM
                        : TimePickerDialogMultiple.PM;
                timeAttribute.setAmOrPm(amOrPm);
            }
        }

        private void updateHourMode(boolean is24HourMode) {
            int size = timeAttributes.size();
            for (int i = 0; i < size; i++) {
                TimeAttribute timeAttribute = timeAttributes.get(i);
                timeAttribute.setIs24HourMode(is24HourMode);
            }
        }
    }

    private class RingtonePickerCaller implements OnPreferenceClickListener {
        private int requestCode;
        private ArrayList<RingtonePickerCallerAttribute> ringtonePickerCallerAttributes;
        // private int titleId;
        private RingtoneAttributeComparatorByPreferenceKey ringtoneAttributeComparatorByPreferenceKey;
        private RingtonePickerCallerAttributeComparatorByCallerId ringtonePickerCallerAttributeComparatorByCallerId;

        public RingtonePickerCaller(int requestCode) {
            this.requestCode = requestCode;
        }

        public RingtonePickerCaller(int requestCode,
                                    ArrayList<RingtonePickerCallerAttribute> ringtonePickerCallerAttributes,
                                    int titleId) {
            this.requestCode = requestCode;
            this.ringtonePickerCallerAttributes = ringtonePickerCallerAttributes;
            // this.titleId = titleId;
            initialize();
        }

        private void initialize() {
            ringtoneAttributeComparatorByPreferenceKey = new RingtoneAttributeComparatorByPreferenceKey();
            ringtonePickerCallerAttributeComparatorByCallerId = new RingtonePickerCallerAttributeComparatorByCallerId();
        }

        private class RingtoneAttributeComparatorByPreferenceKey implements
                Comparator<RingtonePickerCallerAttribute>, Serializable {
            private static final long serialVersionUID = 1L;

            @Override
            public int compare(RingtonePickerCallerAttribute lhs,
                               RingtonePickerCallerAttribute rhs) {
                return lhs.callerKey.compareTo(rhs.callerKey);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            RingtonePickerCallerAttribute ringtonePickerCallerAttribute = new RingtonePickerCallerAttribute(
                    preference);
            Collections.sort(ringtonePickerCallerAttributes,
                    ringtoneAttributeComparatorByPreferenceKey);
            int index = Collections.binarySearch(ringtonePickerCallerAttributes,
                    ringtonePickerCallerAttribute,
                    ringtoneAttributeComparatorByPreferenceKey);
            ringtonePickerCallerAttribute = ringtonePickerCallerAttributes.get(index);
            String ringtone = ringtonePickerCallerAttribute.ringtone;
            Intent intent = null;
            FragmentActivity activity = getActivity();
            switch (ringtonePickerCallerAttribute.callerId) {
                case R.string.preference_key_alarm_ringtone:
                    intent = Helper.initializeIntentForRingtonePicker(activity, null,
                            ringtonePickerCallerAttribute.callerId,
                            RingtoneManager.TYPE_ALARM,
                            Settings.System.DEFAULT_ALARM_ALERT_URI,
                            ringtonePickerCallerAttribute.requestCode);
                    break;
                case R.string.preference_key_notification_ringtone:
                    intent = Helper.initializeIntentForRingtonePicker(activity, null,
                            ringtonePickerCallerAttribute.callerId,
                            RingtoneManager.TYPE_NOTIFICATION,
                            Settings.System.DEFAULT_NOTIFICATION_URI,
                            ringtonePickerCallerAttribute.requestCode);
                    break;
                default:
                    break;
            }
            if (intent != null) {
                intent.putExtra(FragmentSettings.KEY_CALLER_ID,
                        ringtonePickerCallerAttribute.callerId);
                mCallerId = ringtonePickerCallerAttribute.callerId;
                intent.putExtra(CommonConstants.INTENT_EXTRA_FOR_COLOR_PICKER, ringtone);
                startActivityForResult(intent, ringtonePickerCallerAttribute.requestCode);
            }
            // startActivityForResult(intent, requestCode);
            return true;
        }
    }

    class BroadcastReceiverForActivitySettings extends BroadcastReceiver {
        private ArrayList<MultipleTimePickerCaller> timePickerCallers;
        private IntentFilter intentFilter;

        public BroadcastReceiverForActivitySettings(Context context) {
            super();
            timePickerCallers = new ArrayList<MultipleTimePickerCaller>(3);
            intentFilter = new IntentFilter();
            intentFilter
                    .addAction(CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED);
            LocalBroadcastManager.getInstance(context).registerReceiver(this,
                    intentFilter);
        }

        private void addTimePickerCaller(MultipleTimePickerCaller multipleTimePickerCaller) {
            timePickerCallers.add(multipleTimePickerCaller);
        }

        public void addAction(String action) {
            intentFilter.addAction(action);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    CommonConstants.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED)) {
                boolean is24HourMode = Helper.is24HourFormat(context);
                int size = timePickerCallers.size();
                for (int i = 0; i < size; i++) {
                    MultipleTimePickerCaller multipleTimePickerCaller = timePickerCallers
                            .get(i);
                    multipleTimePickerCaller.updateHourMode(is24HourMode);
                }
            }
        }
    }

    private void initializeColorPickerCaller(int requestCode,
                                             ArrayList<ColorPickerCallerAttribute> colorPickerCallerAttributes, int titleId) {
        ColorPickerCaller colorPickerCaller = new ColorPickerCaller(requestCode,
                colorPickerCallerAttributes);
        colorPickerCallers.add(colorPickerCaller);
        int size = colorPickerCallerAttributes.size();
        for (int i = 0; i < size; i++) {
            ColorPickerCallerAttribute colorPickerCallerAttribute = colorPickerCallerAttributes
                    .get(i);
            colorPickerCallerAttribute.preference
                    .setOnPreferenceClickListener(colorPickerCaller);
        }
        showDataFromColorPicker(colorPickerCallerAttributes);
    }

    private void initializeMultipleTimePickerCaller(
            ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes, int titleId) {
        MultipleTimeSetListener multipleTimeSetListener = new MultipleTimeSetListener(
                timePickerCallerAttributes);
        MultipleTimePickerCaller multipleTimePickerCaller = new MultipleTimePickerCaller(
                timePickerCallerAttributes, multipleTimeSetListener, titleId);
        ArrayList<TimeAttribute> timeAttributes = multipleTimePickerCaller.timeAttributes;
        int size = timePickerCallerAttributes.size();
        for (int i = 0; i < size; i++) {
            TimePickerCallerAttribute timePickerCallerAttribute = timePickerCallerAttributes
                    .get(i);
            timePickerCallerAttribute.preference
                    .setOnPreferenceClickListener(multipleTimePickerCaller);
            int minutes = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60) % 60);
            int hours = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60 * 60));
            TimeAttribute timeAttribute = new TimeAttribute(hours, minutes,
                    timePickerCallerAttribute.textId,
                    timePickerCallerAttribute.ordinalNumber,
                    timePickerCallerAttribute.is24HourMode,
                    timePickerCallerAttribute.isHourModeChangeable);
            timeAttributes.add(timeAttribute);
            TimePickerDialogMultiple tpd = (TimePickerDialogMultiple) getFragmentManager()
                    .findFragmentByTag(timePickerCallerAttribute.callerKey);
            if (tpd != null) {
                tpd.setOnTimeSetListener(multipleTimeSetListener);
            }
        }
        showTimes(timePickerCallerAttributes);
        mReceiver.addTimePickerCaller(multipleTimePickerCaller);
    }

    private void initializeRingtonePickerCaller(int requestCode,
                                                ArrayList<RingtonePickerCallerAttribute> ringtonePickerCallerAttributes,
                                                int titleId) {
        RingtonePickerCaller ringtonePickerCaller = new RingtonePickerCaller(requestCode,
                ringtonePickerCallerAttributes, titleId);
        ringtonePickerCallers.add(ringtonePickerCaller);
        int size = ringtonePickerCallerAttributes.size();
        for (int i = 0; i < size; i++) {
            RingtonePickerCallerAttribute ringtonePickerCallerAttribute = ringtonePickerCallerAttributes
                    .get(i);
            ringtonePickerCallerAttribute.preference
                    .setOnPreferenceClickListener(ringtonePickerCaller);
        }
        showRingtones(ringtonePickerCallerAttributes);
    }

    private void showDataFromColorPicker(
            ArrayList<ColorPickerCallerAttribute> colorPickerCallerAttributes) {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        sharedPreferences
                .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        Editor editor = sharedPreferences.edit();
        for (ColorPickerCallerAttribute colorPickerCallerAttribute : colorPickerCallerAttributes) {
            int callerId = colorPickerCallerAttribute.callerId;
            String text = "unset";
            int color = colorPickerCallerAttribute.color;
            switch (callerId) {
                case R.string.preference_key_task_unset_color:
                case R.string.preference_key_calendar_today_date_text_color:
                case R.string.preference_key_calendar_today_date_highlight_color:
                    text = Integer.toHexString(color);
                    editor.putInt(colorPickerCallerAttribute.callerKey, color);
                    break;
                default:
                    break;
            }
            colorPickerCallerAttribute.preference.setSummary(text);
        }
        editor.commit();
        sharedPreferences
                .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
    }

    private void showTimes(ArrayList<TimePickerCallerAttribute> timePickerCallerAttributes) {
        for (TimePickerCallerAttribute timePickerCallerAttribute : timePickerCallerAttributes) {
            int callerId = timePickerCallerAttribute.callerId;
            String text = "";
            FragmentActivity activity = getActivity();
            switch (callerId) {
                case R.string.preference_key_silence_time_duration:
                    if (timePickerCallerAttribute.milliseconds <= 0) {
                        timePickerCallerAttribute.milliseconds = 86400000;
                    }
                    int minutes = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60) % 60);
                    int hours = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60 * 60));
                    text = Helper.getTextForTimeInterval(activity, 0, hours, minutes, 0);
                    break;
                case R.string.preference_key_task_duration:
                    minutes = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60) % 60);
                    hours = (int) (timePickerCallerAttribute.milliseconds / (1000 * 60 * 60));
                    text = Helper.getTextForTimeInterval(activity, 0, hours, minutes, 0);
                    if (text.length() == 0) {
                        Resources resources = getResources();
                        text = resources.getString(R.string.zero_length);
                    }
                    break;
                case R.string.preference_key_business_hours_start_time:
                case R.string.preference_key_business_hours_end_time:
                case R.string.preference_key_start_time:
                    text = Helper.timeFormat.format(new Date(
                            timePickerCallerAttribute.milliseconds));
                    break;
                default:
                    break;
            }
            timePickerCallerAttribute.preference.setSummary(text);
        }
    }

    private void showRingtones(
            ArrayList<RingtonePickerCallerAttribute> ringtonePickerCallerAttributes) {
        FragmentActivity activity = getActivity();
        Editor editor = PreferenceManager.getDefaultSharedPreferences(activity).edit();
        for (RingtonePickerCallerAttribute ringtonePickerCallerAttribute : ringtonePickerCallerAttributes) {
            int callerId = ringtonePickerCallerAttribute.callerId;
            String ringtoneTitle;
            String ringtone = ringtonePickerCallerAttribute.ringtone;
            switch (callerId) {
                case R.string.preference_key_alarm_ringtone:
                case R.string.preference_key_notification_ringtone:
                    ringtoneTitle = Helper.getRingtoneTitle(activity, ringtone);
                    editor.putString(ringtonePickerCallerAttribute.callerKey, ringtone);
                    break;
                default:
                    ringtoneTitle = "unset";
                    break;
            }
            ringtonePickerCallerAttribute.preference.setSummary(ringtoneTitle);
        }
        editor.commit();
    }

    private boolean showIntegerEditTextPreference(Preference preference,
                                                  String stringValue, Resources resources, int dividerId, int defaultValueId,
                                                  int minValueId, int maxValueId, int singularStringId, int pluralStringId,
                                                  int textIdValueIsZero, int callerId) {
        boolean isPreferenceValid = true;
        String title = preference.getTitle().toString();
        int defaultValue;
        int minValue;
        int maxValue;
        String text;
        Context context = preference.getContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        EditTextPreference editTextPreference = (EditTextPreference) preference;
        switch (callerId) {
            case R.string.preference_key_ringtone_fade_in_time:
                int divider = resources.getInteger(dividerId);
                defaultValue = resources.getInteger(defaultValueId) / divider;
                minValue = resources.getInteger(minValueId) / divider;
                maxValue = resources.getInteger(maxValueId) / divider;
                int value;
                if (Helper.isStringIntegerValueValid(context, stringValue, minValue,
                        maxValue, title, true)) {
                    value = Helper.getIntegerFromStringValue(stringValue, defaultValue,
                            minValue, maxValue);
                    int timeIntervalMillis = value * divider;
                    if (value == 0) {
                        text = resources.getString(textIdValueIsZero);
                    } else {
                        text = Helper.getTextForTimeInterval(context, timeIntervalMillis);
                    }
                    preference.setSummary(text);
                    sharedPreferences
                            .unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
                    editTextPreference.setText(String.valueOf(timeIntervalMillis));
                    sharedPreferences
                            .registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
                    mOnSharedPreferenceChangeListener.onSharedPreferenceChanged(null,
                            editTextPreference.getKey());
                    isPreferenceValid = false;
                } else {
                    isPreferenceValid = false;
                }
                break;
            default:
                defaultValue = resources.getInteger(defaultValueId);
                minValue = resources.getInteger(minValueId);
                maxValue = resources.getInteger(maxValueId);
                if (Helper.isStringIntegerValueValid(context, stringValue, minValue,
                        maxValue, title, true)) {
                    value = Helper.getIntegerFromStringValue(stringValue, defaultValue,
                            minValue, maxValue);
                    if (value == 1) {
                        text = String.format(resources.getString(singularStringId), value);
                    } else {
                        text = String.format(resources.getString(pluralStringId), value);
                    }
                    editTextPreference.setText(String.valueOf(value));
                    preference.setSummary(text);
                } else {
                    isPreferenceValid = false;
                }
                break;
        }
        return isPreferenceValid;
    }

    private static void initializeTimeFormatPreference(ListPreference listPreference) {
        CharSequence[] entries = listPreference.getEntries();
        CharSequence entryDefault = entries[0];
        boolean is24HourFormat = android.text.format.DateFormat
                .is24HourFormat(listPreference.getContext());
        CharSequence entry;
        if (is24HourFormat) {
            entry = entries[2];
        } else {
            entry = entries[1];
        }
        entries[0] = String.format(entryDefault.toString(), entry);
        int index = listPreference.findIndexOfValue(listPreference.getValue());
        listPreference.setSummary(entries[index]);
    }

    /**
     * This fragment shows general preferences only. It is used when the activity is
     * showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the activity is
     * showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            // ActivitySettings
            // .bindPreferenceSummaryToValue(findPreference(CommonConstants.notifications_new_message_ringtone));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the activity is
     * showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
        }
    }

    // // @SuppressWarnings("deprecation")
    // // @Override
    // // protected void onResume() {
    // // super.onResume();
    // // getPreferenceScreen().getSharedPreferences()
    // // .registerOnSharedPreferenceChangeListener(this);
    // // }
    // //
    // // @SuppressWarnings("deprecation")
    // // @Override
    // // protected void onPause() {
    // // super.onPause();
    // // getPreferenceScreen().getSharedPreferences()
    // // .unregisterOnSharedPreferenceChangeListener(this);
    // // }
    // private class ActivitySettingsTimeSetListener implements OnTimeSetListener {
    // private Preference preference;
    //
    // public ActivitySettingsTimeSetListener(Preference preference) {
    // this.preference = preference;
    // }
    //
    // @Override
    // public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
    // long milliseconds = (hourOfDay * 60 + minute) * 60 * 1000;
    // Editor editor = PreferenceManager.getDefaultSharedPreferences(
    // ActivitySettings.this).edit();
    // editor.putLong(preference.getKey(), milliseconds);
    // editor.commit();
    // ActivitySettings.showTime(preference, milliseconds);
    // }
    // }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        if (savedInstanceState != null) {
            mListState = savedInstanceState
                    .getParcelable(FragmentSettings.KEY_LIST_STATE);
            mCallerId = savedInstanceState.getInt(FragmentSettings.KEY_CALLER_ID);
        }
        // super.onCreatePreferences( arg0, arg1) ;
        // Load the preferences from an XML resource
        // setupSimplePreferencesScreen();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState
                    .getParcelable(FragmentSettings.KEY_LIST_STATE);
            mCallerId = savedInstanceState.getInt(FragmentSettings.KEY_CALLER_ID);
        }
        // Load the preferences from an XML resource
        setupSimplePreferencesScreen();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView = getListView();
        mLayoutManager = mListView.getLayoutManager();
        mListView.post(new Runnable() {
            @Override
            public void run() {
                if (mListState != null) {
                    mLayoutManager.onRestoreInstanceState(mListState);
                    mListState = null;
                }
            }
        });
    }
}
