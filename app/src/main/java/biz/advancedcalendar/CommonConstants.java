package biz.advancedcalendar;

import android.view.Menu;

import biz.advancedcalendar.alarmer.R;

/** A set of constants used by all of the components in this application. */
public final class CommonConstants {
	private CommonConstants() {
		// don't allow the class to be instantiated
	}

	// public static final int SNOOZE_DURATION = 20000;
	// public static final int DEFAULT_TIMER_DURATION = 10000;
	// public static final String ACTION_SNOOZE =
	// "com.example.android.pingme.ACTION_SNOOZE";
	// public static final String ACTION_DISMISS =
	// "com.example.android.pingme.ACTION_DISMISS";
	// public static final String ACTION_PING =
	// "com.example.android.pingme.ACTION_PING";
	// public static final String EXTRA_MESSAGE=
	// "com.example.android.pingme.EXTRA_MESSAGE";
	// public static final String EXTRA_TIMER =
	// "com.example.android.pingme.EXTRA_TIMER";
	// public static final int NOTIFICATION_ID = 001;
	public static final int CALENDAR_MODE_TIME_INTERVALS = 1;
	public static final int CALENDAR_MODE_TEXT = 2;
	public static final String DEBUG_TAG = "biz.advancedcalendar.DT";
	public static final String TIME_LOGGER_DEBUG_TAG = "biz.advancedcalendar.TIME_LOGGER_DEBUG_TAG";
	public static final String WTF_TAG = "biz.advancedcalendar.WTF_TAG";
	public static final String ERROR_TAG = "biz.advancedcalendar.ERROR_TAG";
	public static final String ACTIVITY_MAIN_LAUNCH_MODE = "biz.advancedcalendar.ACTIVITY_MAIN_LAUNCH_MODE";
	public static final int ACTIVITY_MAIN_LAUNCH_MODE_AGENDA = 1;
	public static final int ACTIVITY_MAIN_LAUNCH_MODE_DAY = 2;
	public static final int ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_ELAPSED = 3;
	public static final int ACTIVITY_MAIN_LAUNCH_MODE_REMINDERS_SCHEDULED = 4;
	public static final int ACTIVITY_MAIN_LAUNCH_MODE_TASKS = 5;
	public static final int REMINDER_NOTIFICATION_ID = 1;
	public static final int SYNC_NOTIFICATION_ID = 2;
	//
	//
	public static final String ALARM_REMINDER = "biz.advancedcalendar.action.ALARM_REMINDER";
	public static final String ALARM_UNSILENSE_REMINDERS = "biz.advancedcalendar.action.ALARM_UNSILENSE_REMINDERS";
	public static final String CONTENT_BIZ_ADVANCEDCALENDAR_ALARM_REMINDER = "content://biz.advancedcalendar/alarm/reminder/";
	public static final String CONTENT_BIZ_ADVANCEDCALENDAR_ALARM_UNSILENSE_REMINDERS = "content://biz.advancedcalendar/alarm/unsilense_reminders";
	public static final int ALARM_REMINDER_TYPE_ALARM = 1;
	public static final int ALARM_REMINDER_TYPE_NOTIFICATION = 2;
	public static final int DO_NOT_SCHEDULE_REMINDER_FOR_COMPLETED_TASK = 1;
	public static final int SCHEDULE_REMINDERS_OF_COMPLETED_TASKS = 2;
	public static final int DATE_PICKER_LAST_BROWSING_DATE = 1;
	public static final int DATE_PICKER_TODAY_DATE = 2;
	public static final int TIME_FORMAT_FROM_LOCALE = 1;
	public static final int TIME_FORMAT_12_HOUR_CLOCK = 2;
	public static final int TIME_FORMAT_24_HOUR_CLOCK = 3;
	public static final int START_TIME_REQUIRED_ACTION_LOAD_FROM_SETTINGS = 1;
	public static final int START_TIME_REQUIRED_ACTION_SET_TO_CURRENT_TIME = 2;
	public static final int START_TIME_REQUIRED_ACTION_DO_NOT_AUTOMATICALLY_SET = 3;
	public static final int ON_TASK_SELECT_ACTION_OPEN_TASK_IN_EDIT_MODE = 1;
	public static final int ON_TASK_SELECT_ACTION_OPEN_TASK_IN_VIEW_MODE = 2;
	public static final int TIME_UNIT_ONE_TIME = 1;
	public static final int TIME_UNIT_MINUTE = 2;
	public static final int TIME_UNIT_HOUR = 3;
	public static final int TIME_UNIT_DAY = 4;
	public static final int TIME_UNIT_WEEK = 5;
	public static final int TIME_UNIT_MONTH = 6;
	public static final int TIME_UNIT_YEAR = 7;
	public static final int RECURRENCE_INTERVAL_ONE_TIME = 1;
	public static final int RECURRENCE_INTERVAL_MINUTES = 2;
	public static final int RECURRENCE_INTERVAL_HOURS = 3;
	public static final int RECURRENCE_INTERVAL_DAYS = 4;
	public static final int RECURRENCE_INTERVAL_WEEKS = 5;
	public static final int RECURRENCE_INTERVAL_MONTHS_ON_DATE = 6;
	public static final int RECURRENCE_INTERVAL_MONTHS_ON_NTH_WEEK_DAY = 7;
	public static final int RECURRENCE_INTERVAL_YEARS = 8;
	// public static final int TASK_TYPE_RECURRENT = 1;
	// public static final int TASK_TYPE_SIMPLE = 2;
	// public static final long ALARM_REMINDER_SNOOZE_DEFAULT_TIMESPAN = 1000 * 60 * 10;
	// public static final int ALARM_REMINDER_MAX_DEFAULT_SNOOZE_COUNT = 6;
	// public static final long ALARM_REMINDER_ALARM_RINGING_DEFAULT_TIMESPAN = 1000 * 60;
	// public static final long ALARM_REMINDER_SNOOZE_DEFAULT_TIMESPAN = 1000 * 10;
	// public static final int ALARM_REMINDER_MAX_DEFAULT_SNOOZE_COUNT = 2;
	// public static final long ALARM_REMINDER_ALARM_RINGING_DEFAULT_TIMESPAN = 1000 * 5;
	// public static final String INTENT_EXTRA_ALARM_REMINDER_SNOOZE_COUNT2 =
	// "biz.advancedcalendar.action.ALARM_REMINDER_SNOOZE_COUNT_INTENT_EXTRA";
	public static final String ALARM_SYNC_ALL = "biz.advancedcalendar.action.ALARM_SYNC_ALL";
	public static final String ALARM_SYNC_MESSAGES_FOR_TASK = "biz.advancedcalendar.action.ALARM_SYNC_MESSAGES_FOR_TASK";
	public static final String ACTION_REMINDER_NOTIFICATION_REQUEST_FROM_ALARM = "biz.advancedcalendar.action.REMINDER_NOTIFICATION_REQUEST";
	// public static final String REMINDER_SINGLE_NOTIFICATION_REQUEST =
	// "biz.advancedcalendar.action.REMINDER_SINGLE_NOTIFICATION_REQUEST";
	public static final String ACTION_REMINDER_NOTIFICATION_IS_TAPPED = "biz.advancedcalendar.action.ACTION_REMINDER_NOTIFICATION_IS_TAPPED";
	public static final String ACTION_REMINDER_NOTIFICATION_IS_DELETED = "biz.advancedcalendar.action.ACTION_REMINDER_NOTIFICATION_IS_DELETED";
	public static final String INTENT_EXTRA_ALARM_NEXT_SNOOZE_TIME = "biz.advancedcalendar.INTENT_EXTRA_ALARM_NEXT_SNOOZE_TIME";
	public static final String INTENT_EXTRA_ALARM_REMINDER_TYPE2 = "biz.advancedcalendar.action.INTENT_EXTRA_ALARM_REMINDER_TYPE";
	public static final String INTENT_EXTRA_ID = "biz.advancedcalendar.INTENT_EXTRA_ID";
	public static final String INTENT_EXTRA_TAG = "biz.advancedcalendar.INTENT_EXTRA_TAG";
	public static final String INTENT_EXTRA_SCHEDULED_REMINDER = "biz.advancedcalendar.INTENT_EXTRA_SCHEDULED_REMINDER";
	public static final String INTENT_EXTRA_ACCOUNT_TYPES = "biz.advancedcalendar.INTENT_EXTRA_ACCOUNT_TYPES";
	public static final String INTENT_EXTRA_ACTIVITY_ALARM_WAS_FINISHED = "biz.advancedcalendar.INTENT_EXTRA_ACTIVITY_ALARM_WAS_FINISHED";
	public static final String INTENT_EXTRA_REQUEST_PICK_USER_ACCOUNT = "biz.advancedcalendar.INTENT_EXTRA_REQUEST_PICK_USER_ACCOUNT";
	public static final String INTENT_EXTRA_REQUEST_CODE = "biz.advancedcalendar.INTENT_EXTRA_REQUEST_CODE";
	public static final String INTENT_EXTRA_RESULT_CODE = "biz.advancedcalendar.INTENT_EXTRA_RESULT_CODE";
	public static final String INTENT_EXTRA_TASK = "biz.advancedcalendar.INTENT_EXTRA_TASK";
	public static final String INTENT_EXTRA_USER_INTERFACE_DATA = "biz.advancedcalendar.INTENT_EXTRA_USER_INTERFACE_DATA";
	public static final String INTENT_EXTRA_REMINDER = "biz.advancedcalendar.INTENT_EXTRA_REMINDER";
	public static final String INTENT_EXTRA_FOR_COLOR_PICKER = "biz.advancedcalendar.INTENT_EXTRA_FOR_COLOR_PICKER";
	public static final String INTENT_EXTRA_INFORMATION_UNIT_MATRIX = "biz.advancedcalendar.INTENT_EXTRA_INFORMATION_UNIT_MATRIX";
	public static final String INTENT_EXTRA_IS_INFORMATION_UNITS_SORT_ORDER_CHANGED = "biz.advancedcalendar.INTENT_EXTRA_INFORMATION_UNITS_SORT_ORDER_CHANGED";
	public static final String INTENT_EXTRA_INFORMATION_UNIT_SORT_ORDERS_HOLDER = "biz.advancedcalendar.INTENT_EXTRA_INFORMATION_UNITS_SORT_ORDER_HOLDER";
	public static final String INTENT_EXTRA_LAST_SELECTED_CHECKBOX_ID = "biz.advancedcalendar.INTENT_EXTRA_LAST_SELECTED_CHECKBOX_ID";
	public static final String INTENT_EXTRA_LAST_SELECTED_RADIOBUTTON_ID = "biz.advancedcalendar.INTENT_EXTRA_LAST_SELECTED_RADIOBUTTON_ID";
	public static final int INITIAL_VALUE_FOR_RECTANGULAR_CHECK_BOX_IDS = 0;
	public static final String INTENT_EXTRA_DATETIME = "biz.advancedcalendar.INTENT_EXTRA_DATETIME";
	public static final String INTENT_EXTRA_DAYS_COUNT = "biz.advancedcalendar.INTENT_EXTRA_DAYS_COUNT";
	public static final String INTENT_EXTRA_SELECTED_DAYS_OF_YEAR = "biz.advancedcalendar.INTENT_EXTRA_SELECTED_DAYS_OF_YEAR";
	public static final String SAVED_INSTANCE_STATE_YEAR = "biz.advancedcalendar.SAVED_INSTANCE_STATE_YEAR";
	public static final String SAVED_INSTANCE_STATE_MONTH = "biz.advancedcalendar.SAVED_INSTANCE_STATE_MONTH";
	public static final String SAVED_INSTANCE_STATE_DAY_OF_MONTH = "biz.advancedcalendar.SAVED_INSTANCE_STATE_DAY_OF_MONTH";
	public static final String SAVED_INSTANCE_STATE_HOUR_OF_DAY = "biz.advancedcalendar.SAVED_INSTANCE_STATE_HOUR_OF_DAY";
	public static final String SAVED_INSTANCE_STATE_MINUTE = "biz.advancedcalendar.SAVED_INSTANCE_STATE_MINUTE";
	public static final String SAVED_INSTANCE_STATE_CANCELLED = "biz.advancedcalendar.SAVED_INSTANCE_STATE_CANCELLED";
	// public static final String INTENT_EXTRA_ENTITY_CHANGED_TYPES =
	// "biz.advancedcalendar.INTENT_EXTRA_DATA_CHANGED_TYPES";
	public static final String INTENT_EXTRA_FIRST_DAY_OF_WEEK = "biz.advancedcalendar.INTENT_EXTRA_FIRST_DAY_OF_WEEK";
	public static final String INTENT_EXTRA_POSITION = "biz.advancedcalendar.INTENT_EXTRA_POSITION";
	public static final String INTENT_EXTRA_FRAGMENT_VIEW_REMINDERS_WHICH_REMINDERS_TO_SHOW = "biz.advancedcalendar.INTENT_EXTRA_FRAGMENT_VIEW_REMINDERS_WHICH_REMINDERS_TO_SHOW";
	public static final int INTENT_EXTRA_VALUE_FRAGMENT_VIEW_REMINDERS_ELAPSED = 1;
	public static final int INTENT_EXTRA_FRAGMENT_VIEW_REMINDERS_SCHEDULED = 2;
	public static final int INTENT_EXTRA_VALUE_TAB_TASK = 0;
	public static final int INTENT_EXTRA_VALUE_TAB_REMINDERS = 1;
	public static final String INTENT_EXTRA_ACTIVITY_ALARM_LAUNCH_MODE = "biz.advancedcalendar.INTENT_EXTRA_ACTIVITY_ALARM_LAUNCH_MODE";
	public static final int INTENT_EXTRA_VALUE_ACTIVITY_ALARM_LAUNCH_MODE_ALARMED = 0;
	public static final String INTENT_EXTRA_ACTIVITY_MAIN_LAUNCH_MODE = "biz.advancedcalendar.INTENT_EXTRA_ACTIVITY_MAIN_LAUNCH_MODE";
	public static final int INTENT_EXTRA_VALUE_ACTIVITY_MAIN_LAUNCH_MODE_CALENDAR_TAB_DAY = 1;
	public static final int INTENT_EXTRA_VALUE_ACTIVITY_MAIN_LAUNCH_MODE_CALENDAR_TAB_WEEK = 2;
	public static final String INTENT_EXTRA_ACTIVITY_MAIN_TAB_INDEX = "biz.advancedcalendar.INTENT_EXTRA_ACTIVITY_MAIN_TAB_INDEX";
	public static final String INTENT_EXTRA_ACTIVITY_MAIN_CALENDAR_DATE = "biz.advancedcalendar.INTENT_EXTRA_ACTIVITY_MAIN_CALENDAR_DATE";
	public static final String INTENT_EXTRA_SYNCHRONIZATION_AFTER_SUCCESSFUL_SIGNIN_REQUIRED = "biz.advancedcalendar.INTENT_EXTRA_SYNCHRONIZATION_AFTER_SUCCESSFUL_SIGNIN_REQUIRED";
	public static final String INTENT_EXTRA_SHOW_ADD_MENU = "biz.advancedcalendar.INTENT_EXTRA_SHOW_ADD_MENU";
	public static final String INTENT_EXTRA_IS_EDIT_MODE = "biz.advancedcalendar.INTENT_EXTRA_IS_EDIT_MODE";
	public static final String INTENT_EXTRA_SYNC_SERVICE_REQUEST_SYNC_MESSAGES_FOR_TASK = "biz.advancedcalendar.INTENT_EXTRA_SYNC_SERVICE_REQUEST_SYNC_MESSAGES_FOR_TASK";
	public static final String RETURN_ID = "biz.advancedcalendar.RETURN_ID";
	public static final String PREVIOUS_RADIOBUTTON_ID = "biz.advancedcalendar.PREVIOUS_RADIOBUTTON_ID";
	public static final String ID_ARRAY = "biz.advancedcalendar.ID_ARRAY";
	public static final String TAG = "biz.advancedcalendar.TAG";
	public static final String CALLER_ID = "biz.advancedcalendar.CALLER_ID";
	public static final String TASK_DATE = "biz.advancedcalendar.TASK_DATE";
	// public static final String TASK_ID = "biz.advancedcalendar.TASK_ID";
	// public static final String REMINDER_ID = "biz.advancedcalendar.REMINDER_ID";
	public static final String REMINDER_ID_ARRAY = "biz.advancedcalendar.REMINDER_ID_ARRAY";
	public static final String REMINDER_ID_FROM_NOTIFICATION = "biz.advancedcalendar.REMINDER_ID_FROM_NOTIFICATION";
	public static final String MENU_ID = "biz.advancedcalendar.MENU_ID";
	public static final String YEAR = "biz.advancedcalendar.YEAR";
	public static final String MONTH = "biz.advancedcalendar.MONTH_OF_YEAR";
	public static final String DAY_OF_MONTH = "biz.advancedcalendar.DAY_OF_MONTH";
	public static final String HOUR_OF_DAY = "biz.advancedcalendar.HOUR";
	public static final String MINUTE = "biz.advancedcalendar.MINUTE";
	public static final String SECOND = "biz.advancedcalendar.SECOND";
	public static final String MILLISECOND = "biz.advancedcalendar.MILLISECOND";
	public static final String ACTION_CALENDARS_CHANGED = "biz.advancedcalendar.ACTION_CALENDARS_CHANGED";
	public static final String ACTION_SELECTED_CALENDARS_CHANGED = "biz.advancedcalendar.ACTION_SELECTED_CALENDARS_CHANGED";
	public static final String ACTION_DAYS_COUNT_FOR_WEEKVIEW_CHANGED = "biz.advancedcalendar.ACTION_DAYS_COUNT_FOR_WEEKVIEW_CHANGED";
	public static final String ACTION_ENTITIES_CHANGED_TASKS = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_TASKS";
	public static final String ACTION_ENTITIES_CHANGED_REMINDERS = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_REMINDERS";
	public static final String ACTION_REMINDERS_SILENSED = "biz.advancedcalendar.ACTION_REMINDERS_SILENSED";
	public static final String ACTION_REMINDERS_UNSILENSED = "biz.advancedcalendar.ACTION_REMINDERS_UNSILENSED";
	public static final String ACTION_ENTITIES_CHANGED_LABELS = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_LABELS";
	public static final String ACTION_ENTITIES_CHANGED_CONTACTS = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_CONTACTS";
	public static final String ACTION_ENTITIES_CHANGED_FILES = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_FILES";
	public static final String ACTION_ENTITIES_CHANGED_DIARY_RECORDS = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_DIARY_RECORDS";
	public static final String ACTION_ENTITIES_CHANGED_MESSAGES = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_MESSAGES";
	public static final String ACTION_ENTITIES_CHANGED_USER_PROFILE = "biz.advancedcalendar.ACTION_ENTITIES_CHANGED_USER_PROFILE";
	public static final String ACTION_FIRST_DAY_OF_WEEK_CHANGED = "biz.advancedcalendar.ACTION_FIRST_DAY_OF_WEEK_CHANGED";
	public static final String ACTION_MONTH_RECURRENCE_MODE_CHANGED = "biz.advancedcalendar.ACTION_MONTH_RECURRENCE_MODE_CHANGED";
	public static final String ACTION_TIME_PICKER_TIME_FORMAT_CHANGED = "biz.advancedcalendar.ACTION_TIME_PICKER_TIME_FORMAT_CHANGED";
	public static final String ACTION_START_TIME_CHANGED = "biz.advancedcalendar.ACTION_START_TIME_CHANGED";
	public static final String ACTION_REPETITIONS_DURATION_CHANGED = "biz.advancedcalendar.ACTION_REPETITIONS_DURATION_CHANGED";
	public static final String ACTION_TASK_UNSET_COLOR_CHANGED = "biz.advancedcalendar.ACTION_TASK_UNSET_COLOR_CHANGED";
	public static final String ACTION_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE_CHANGED = "biz.advancedcalendar.ACTION_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE_CHANGED";
	public static final String ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED = "biz.advancedcalendar.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_MODE_CHANGED";
	public static final String ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED = "biz.advancedcalendar.ACTION_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TEXT_MODE_CHANGED";
	public static final String ACTION_INFORMATION_UNIT_MATRIX_FOR_AGENDA_CHANGED = "biz.advancedcalendar.ACTION_INFORMATION_UNIT_MATRIX_FOR_AGENDA_CHANGED";
	public static final String ACTION_BUSINESS_HOURS_START_TIME_CHANGED = "biz.advancedcalendar.ACTION_BUSINESS_HOURS_START_TIME_CHANGED";
	public static final String ACTION_BUSINESS_HOURS_END_TIME_CHANGED = "biz.advancedcalendar.ACTION_BUSINESS_HOURS_END_TIME_CHANGED";
	public static final String ACTION_BUSINESS_HOURS_TASK_DISPLAYING_POLICY_CHANGED = "biz.advancedcalendar.ACTION_BUSINESS_HOURS_TASK_DISPLAYING_POLICY_CHANGED";
	public static final String ACTION_HOURS_RULER_DISPLAYING_POLICY_CHANGED = "biz.advancedcalendar.ACTION_HOURS_RULER_DISPLAYING_POLICY_CHANGED";
	public static final String ACTION_START_TIME_SELECTED = "biz.advancedcalendar.ACTION_START_TIME_SELECTED";
	public static final String ACTION_END_TIME_SELECTED = "biz.advancedcalendar.ACTION_END_TIME_SELECTED";
	public static final String ACTION_REPETITION_END_TIME_SELECTED = "biz.advancedcalendar.ACTION_REPETITION_END_TIME_SELECTED";
	public static final String ACTION_START_TIME_REQUIRED_ACTION_CHANGED = "biz.advancedcalendar.ACTION_START_TIME_REQUIRED_ACTION_CHANGED";
	public static final String ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED = "biz.advancedcalendar.ACTION_WEEK_VIEW_CURRENTLY_SELECTED_DAY_CHANGED";
	public static final String ACTION_RINGTONE_FADE_IN_TIME_CHANGED = "biz.advancedcalendar.ACTION_RINGTONE_FADE_IN_TIME_CHANGED";
	public static final String ACTION_REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_CHANGED = "biz.advancedcalendar.ACTION_REMINDERS_POPUP_WINDOW_DISPLAYING_DURATION_CHANGED";
	public static final String ACTION_AUTOMATIC_SNOOZE_DURATION_CHANGED = "biz.advancedcalendar.ACTION_AUTOMATIC_SNOOZE_DURATION_CHANGED";
	public static final String ACTION_AUTOMATIC_SNOOZES_MAX_COUNT_CHANGED = "biz.advancedcalendar.ACTION_AUTOMATIC_SNOOZES_MAX_COUNT_CHANGED";
	public static final String ACTION_MAX_NUMBER_OF_ELAPSED_REMINDERS_CHANGED = "biz.advancedcalendar.ACTION_MAX_NUMBER_OF_ELAPSED_REMINDERS_CHANGED";
	public static final String ACTION_REMINDER_BEHAVIOR_FOR_COMPLETED_TASK_CHANGED = "biz.advancedcalendar.ACTION_REMINDER_BEHAVIOR_FOR_COMPLETED_TASK_CHANGED";
	public static final String ACTION_SYNC_POLICY_CHANGED = "biz.advancedcalendar.ACTION_SYNC_POLICY_CHANGED";
	public static final String ACTION_MARK_SYNC_NEEDED_CHANGED = "biz.advancedcalendar.ACTION_MARK_SYNC_NEEDED_CHANGED";
	public static final String ACTION_SYNC_FREQUENCY_CHANGED = "biz.advancedcalendar.ACTION_SYNC_FREQUENCY_CHANGED";
	// public static final String ACTION_WEEK_VIEW_START_DAY_CHANGED =
	// "biz.advancedcalendar.ACTION_WEEK_VIEW_START_DAY_CHANGED";
	public static final long ENTITY_CHANGED_TYPE_TASKS = 1;
	// public static final String DATA_CHANGED_TYPE_TASKS_Str =
	// "biz.advancedcalendar.DATA_CHANGED_TYPE_TASKS";
	public static final long ENTITY_CHANGED_TYPE_LABELS = 2;// 0b10
	public static final long ENTITY_CHANGED_TYPE_CONTACTS = 4;// "biz.advancedcalendar.DATA_CHANGED_TYPE_CONTACTS";
	public static final long ENTITY_CHANGED_TYPE_FILES = 8;// "biz.advancedcalendar.DATA_CHANGED_TYPE_FILES";
	public static final long ENTITY_CHANGED_TYPE_DIARY_RECORDS = 16;// "biz.advancedcalendar.DATA_CHANGED_TYPE_DIARY";
	public static final long ENTITY_CHANGED_TYPE_REMINDERS = 32;// "biz.advancedcalendar.DATA_CHANGED_TYPE_REMINDERS";
	public static final long ENTITY_CHANGED_TYPE_MESSAGES = 64;// "biz.advancedcalendar.DATA_CHANGED_TYPE_MESSAGES";
	public static final long ENTITY_CHANGED_TYPE_USER_PROFILE = 128;// "biz.advancedcalendar.DATA_CHANGED_TYPE_USER_PROFILE";
	public static final long ENTITY_CHANGED_TYPE_FIRST_DAY_OF_WEEK = 256;
	// public static final String SYNC_SERVICE_SYNC_ALL =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_ALL";
	// public static final String SYNC_SERVICE_SYNC_TASKS =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_TASKS";
	// public static final String SYNC_SERVICE_SYNC_TASKS_FIRST_SYNC =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_TASKS_FIRST_SYNC";
	// public static final String SYNC_SERVICE_SYNC_LABELS =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_LABELS";
	// public static final String SYNC_SERVICE_SYNC_LABELS_FIRST_SYNC =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_LABELS_FIRST_SYNC";
	// public static final String SYNC_SERVICE_SYNC_CONTACTS =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_CONTACTS";
	// public static final String SYNC_SERVICE_SYNC_CONTACTS_FIRST_SYNC =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_CONTACTS_FIRST_SYNC";
	// public static final String SYNC_SERVICE_SYNC_DIARY_RECORDS =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_DIARY_RECORDS";
	// public static final String SYNC_SERVICE_SYNC_DIARY_RECORDS_FIRST_SYNC =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_DIARY_RECORDS_FIRST_SYNC";
	// public static final String SYNC_SERVICE_SYNC_FILES =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_FILES";
	// public static final String SYNC_SERVICE_SYNC_FILES_FIRST_SYNC =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_FILES_FIRST_SYNC";
	// public static final String SYNC_SERVICE_FIRST_SYNC =
	// "biz.advancedcalendar.SYNC_SERVICE_FIRST_SYNC";
	public static final String INTENT_EXTRA_SYNC_SERVICE_REQUEST = "biz.advancedcalendar.SYNC_SERVICE_REQUEST";

	public static enum SYNC_SERVICE_REQUEST {
		SYNC_UP_TASKS, FORCE_SYNC, REGULAR_SYNC, SYNC_USER_PROFILE, SYNC_MESSAGES, MESSAGES_FOR_TASK
	};

	public static final int INTENT_EXTRA_VALUE_SYNC_SERVICE_REQUEST_FORCE_SYNC = 0;
	public static final int SYNC_SERVICE_REQUEST_REGULAR_SYNC = 1;
	public static final int SYNC_SERVICE_REQUEST_SYNC_UP_TASKS = 2;
	public static final int SYNC_SERVICE_REQUEST_SYNC_USER_PROFILE = 3;
	public static final int SYNC_SERVICE_REQUEST_SYNC_MESSAGES = 4;
	public static final int SYNC_SERVICE_REQUEST_SYNC_MESSAGES_FOR_TASK = 5;
	// public static final String SYNC_SERVICE_SYNC_UP_SINGLE_TASK =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_UP_SINGLE_TASK";
	// public static final String SYNC_SERVICE_SYNC_UP_SINGLE_CONTACT =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_UP_SINGLE_CONTACT";
	// public static final String SYNC_SERVICE_SYNC_UP_SINGLE_LABEL =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_UP_SINGLE_LABEL";
	// public static final String SYNC_SERVICE_SYNC_UP_SINGLE_FILE =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_UP_SINGLE_FILE";
	// public static final String SYNC_SERVICE_SYNC_UP_SINGLE_DIARY_RECORD =
	// "biz.advancedcalendar.SYNC_SERVICE_SYNC_UP_SINGLE_DIARY_RECORD";
	// public static final String SYNC_DOWN_SERVER_USER_PROFILE_ID =
	// "biz.advancedcalendar.SYNC_DOWN_USER_PROFILE_ID";
	// public static final String SYNC_DOWN_USE_RESET_DATETIME =
	// "biz.advancedcalendar.SYNC_DOWN_USE_RESET_DATETIME";
	public static final String SYNC_DOWN_DATETIME_FOR_TASKS = "biz.advancedcalendar.SYNC_DOWN_DATETIME_FOR_TASKS";
	public static final String SYNC_DOWN_DATETIME_FOR_LABELS = "biz.advancedcalendar.SYNC_DOWN_DATETIME_FOR_LABELS";
	public static final String SYNC_DOWN_DATETIME_FOR_CONTACTS = "biz.advancedcalendar.SYNC_DOWN_DATETIME_FOR_CONTACTS";
	public static final String SYNC_DOWN_DATETIME_FOR_DIARY_RECORDS = "biz.advancedcalendar.SYNC_DOWN_DATETIME_FOR_DIARY_RECORDS";
	public static final String SYNC_DOWN_DATETIME_FOR_FILES = "biz.advancedcalendar.SYNC_DOWN_DATETIME_FOR_FILES";
	public static final String SYNC_DOWN_DATETIME_FOR_MESSAGES = "biz.advancedcalendar.SYNC_DOWN_DATETIME_FOR_MESSAGES";
	public static final int SYNC_DOWN_MAX_BUNCH_SIZE = 150;
	public static final int SYNC_UP_MAX_BUNCH_SIZE = 10;
	// public static final String ADVANCED_CALENDAR_PREFERENCES =
	// "biz.advancedcalendar.ADVANCED_CALENDAR_PREFERENCES";
	// public static final String SHARED_PREFERENCES_VALUES_IS_SIGNED_IN =
	// "biz.advancedcalendar.SHARED_PREFERENCES_VALUES_IS_SIGNED_IN";
	public static final String PREF_SIGNED_IN_USER_NAME = "biz.advancedcalendar.PREF_SIGNED_IN_USER_NAME";
	public static final String PREF_AUTH_TOKEN = "biz.advancedcalendar.PREF_AUTH_TOKEN";
	public static final String PREF_DEFAULT_NOTIFICATION_RINGTONE = "biz.advancedcalendar.PREF_DEFAULT_NOTIFICATION_RINGTONE";
	// public static final String PREF_DEFAULT_ALARM_RINGTONE =
	// "biz.advancedcalendar.PREF_DEFAULT_ALARM_RINGTONE";
	public static final String PREF_LAST_LOGGED_TIME = "biz.advancedcalendar.PREF_LAST_LOGGED_TIME";
	public static final String PREF_TASKS_NEXT_SYNC_DOWN_DATETIME = "biz.advancedcalendar.PREF_TASKS_NEXT_SYNC_DOWN_DATETIME";
	public static final String PREF_LABELS_NEXT_SYNC_DOWN_DATETIME = "biz.advancedcalendar.PREF_LABELS_NEXT_SYNC_DOWN_DATETIME";
	public static final String PREF_CONTACTS_NEXT_SYNC_DOWN_DATETIME = "biz.advancedcalendar.PREF_CONTACTS_NEXT_SYNC_DOWN_DATETIME";
	public static final String PREF_DIARY_RECORDS_NEXT_SYNC_DOWN_DATETIME = "biz.advancedcalendar.PREF_DIARY_RECORDS_NEXT_SYNC_DOWN_DATETIME";
	public static final String PREF_FILES_NEXT_SYNC_DOWN_DATETIME = "biz.advancedcalendar.PREF_FILES_NEXT_SYNC_DOWN_DATETIME";
	public static final String PREF_MESSAGES_NEXT_SYNC_DOWN_DATETIME = "biz.advancedcalendar.PREF_MESSAGES_NEXT_SYNC_DOWN_DATETIME";
	public static final String PREF_IS_REQUEST_PICK_USER_ACCOUNT_PENDING = "biz.advancedcalendar.PREF_IS_REQUEST_PICK_USER_ACCOUNT_PENDING";
	// public static final String PREF_NOTIFICATIONS_NEW_MESSAGE_VIBRATE =
	// "biz.advancedcalendar.PREF_NOTIFICATIONS_NEW_MESSAGE_VIBRATE";
	// public static final String PREF_SYNC_ON_CELLULAR_INTERNET =
	// "biz.advancedcalendar.PREF_SYNC_ON_CELLULAR_INTERNET";
	public static final String TREE_ELEMENTS_EXCLUDE_SUBTREE = "biz.advancedcalendar.TREE_ELEMENTS_EXCLUDE_SUBTREE";
	public static final String TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK = "biz.advancedcalendar.TREE_ELEMENTS_INCLUDE_NON_DELETED_TASK";
	public static final String TREE_ELEMENTS_INCLUDE_DELETED_TASK = "biz.advancedcalendar.TREE_ELEMENTS_INCLUDE_DELETED_TASK";
	public static final String TREE_ELEMENTS_INCLUDE_ACTIVE_TASK = "biz.advancedcalendar.TREE_ELEMENTS_INCLUDE_ACTIVE_TASK";
	public static final String TREE_ELEMENTS_INCLUDE_COMPLETED_TASK = "biz.advancedcalendar.TREE_ELEMENTS_INCLUDE_COMPLETED_TASK";
	public static final String TREE_ELEMENTS_INCLUDE_NON_DELETED_LABEL = "biz.advancedcalendar.TREE_ELEMENTS_INCLUDE_NON_DELETED_LABEL";
	public static final String TREE_ELEMENTS_INCLUDE_DELETED_LABEL = "biz.advancedcalendar.TREE_ELEMENTS_INCLUDE_DELETED_LABEL";
	// some magic codes empirically retrieved from server
	// magic code empirically retrieved from server: 5021
	public static final int TASK_RESPONSE_RESULT_ERROR_TYPE_5021 = 5021;
	public static final int LABEL_RESPONSE_RESULT_ERROR_TYPE_220_LABEL_NOT_FOUND = 220;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_5004_SIGNUP_ERROR_USER_NAME_ALREADY_EXISTS = 5004;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_5005_SIGNIN_ERROR_USER_NAME_DOES_NOT_EXIST = 5005;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_5028_FILE_CREATION_NOT_SUPPORTED_VIA_SYNCHRONIZATION_API = 5028;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_320_SET_CONTACT_ERROR_CONTACT_NOT_FOUND = 320;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_1554_GET_FILE_ERROR_FILE_NOT_FOUND = 1554;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_305_SET_CONTACT_ERROR_UNKNOWN_CONTACT_DATA_TYPE_FOR_CONTACT = 350;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_2_OBJECT_NOT_FOUND = 2;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_3_OBJECT_DELETED = 3;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_8_ACCESS_DENIED = 8;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_5_PARENT_OBJECT_NOT_FOUND = 5;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_9_NOT_ALL_REQUESTED_OBJECTS_FOUND = 10;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_9_NOT_ALL_RELATED_OBJECTS_FOUND = 9;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_4_INCORRECT_DATA = 4;
	public static final int SERVER_RESPONSE_RESULT_ERROR_TYPE_0_NO_ERROR = 0;
	public static final String TREE_VIEW_LIST_CHOICE_MODE = "biz.advancedcalendar.TREE_VIEW_LIST_CHOICE_MODE";
	public static final byte USER_NOTIFICATION_CONTACT_CONTACT_TYPE_EMAIL = 0;
	public static final byte USER_NOTIFICATION_CONTACT_CONTACT_TYPE_PHONE = 1;
	public static final byte USER_NOTIFICATION_CONTACT_CONTACT_TYPE_LOCAL = 2;
	public static final String SELECT_TREE_ITEM_REQUEST_BUNDLE = "biz.advancedcalendar.SELECT_TREE_ITEM_REQUEST_BUNDLE";
	public static final String SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE = "biz.advancedcalendar.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE";
	public static final int SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_TASK_TREE = 0;
	public static final int SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_LABEL_TREE = 1;
	public static final int SELECT_TREE_ITEM_REQUEST_FOR_LABEL_TREE = 1;
	//
	public static final int MENU_ID_EDIT = Menu.FIRST;
	public static final int MENU_ID_COPY = Menu.FIRST + 10;
	public static final int MENU_ID_SAVE = Menu.FIRST + 20;
	public static final int MENU_ID_SET_ACTIVE = Menu.FIRST + 23;
	public static final int MENU_ID_SET_COMPLETED = Menu.FIRST + 25;
	public static final int MENU_ID_DELETE = Menu.FIRST + 30;
	public static final int MENU_ID_UNDELETE = Menu.FIRST + 33;
	public static final int MENU_ID_DELETE_PERMANENTLY = Menu.FIRST + 35;
	public static final int MENU_ID_OK = Menu.FIRST + 40;
	public static final int MENU_ID_CANCEL = Menu.FIRST + 50;
	public static final int MENU_ID_ADD_TASK = Menu.FIRST + 60;
	public static final int MENU_ID_ADD_CONTACT = Menu.FIRST + 70;
	public static final int MENU_ID_ADD_FILE = Menu.FIRST + 80;
	public static final int MENU_ID_ADD_DIARY_RECORD = Menu.FIRST + 90;
	public static final int MENU_ID_ADD_MESSAGE = Menu.FIRST + 100;
	public static final int MENU_ID_ADD_LABEL = Menu.FIRST + 110;
	public static final int MENU_ID_ADD_CALENDAR = Menu.FIRST + 105;
	public static final int MENU_ID_FORCE_SYNC = Menu.FIRST + 120;
	public static final int MENU_ID_ADD_QUICK_REMINDER = Menu.FIRST + 150;
	public static final int MENU_ID_GOTO_TODAY = Menu.FIRST + 160;
	public static final int MENU_ID_GOTO_DATE = Menu.FIRST + 165;
	// public static final int MENU_ID_SYNCHRONIZE_ALL_TASKS = 6;
	// public static final int MENU_ID_SYNCHRONIZE_ALL_LABELS = 7;
	// public static final int MENU_ID_SYNCHRONIZE_ALL_CONTACTS = 8;
	// public static final int MENU_ID_SYNCHRONIZE_ALL_FILES = 9;
	// public static final int MENU_ID_SYNCHRONIZE_ALL_DIARY_RECORDS = 10;
	// public static final int MENU_ID_SYNCHRONIZE_ALL_MESSAGES = 11;
	public static final int MENU_ID_EXPAND_ALL = Menu.FIRST + 170;
	public static final int MENU_ID_COLLAPSE_ALL = Menu.FIRST + 180;
	public static final int MENU_ID_MUTE_ALARM = Menu.FIRST + 190;
	public static final int MENU_ID_SNOOZE_MODE_TIME_INTERVAL = Menu.FIRST + 192;
	public static final int MENU_ID_SNOOZE_MODE_ABSOLUTE_TIME = Menu.FIRST + 195;
	public static final int MENU_ID_PICK_PREDEFINED_COLOR = Menu.FIRST + 200;
	public static final int MENU_ID_SORT_ASCENDING = Menu.FIRST + 250;
	public static final int MENU_ID_SORT_DESCENDING = Menu.FIRST + 255;
	public static final int MENU_ID_DISCARD_ELAPSED_REMINDERS_ALL = Menu.FIRST + 300;
	public static final int MENU_ID_DISCARD = Menu.FIRST + 325;
	public static final int MENU_ID_DISMISS = Menu.FIRST + 350;
	public static final int MENU_ID_EXPAND = Menu.FIRST + 400;
	public static final int MENU_ID_COLLAPSE = Menu.FIRST + 450;
	public static final int MENU_ID_EXPAND_COLLAPSE = Menu.FIRST + 500;
	public static final int MENU_ID_SHOW_HIDE_CALENDARS_SELECTOR_BAR = Menu.FIRST + 525;
	public static final int MENU_ID_SHOW_HIDE_DAYS_COUNT_SELECTOR_BAR = Menu.FIRST + 535;
	public static final int MENU_ID_SILENCE_ALARMS = Menu.FIRST + 550;
	public static final int MENU_ID_UNSILENCE_ALARMS = Menu.FIRST + 600;
	public static final int MENU_ID_CHECK_ALL = Menu.FIRST + 700;
	public static final int MENU_ID_UNCHECK_ALL = Menu.FIRST + 800;
	public static final int MENU_ID_WEEKVIEW_MODE = Menu.FIRST + 900;
	public static final int MENU_ID_RENAME = Menu.FIRST + 950;
	public static final int MENU_ID_MOVE = Menu.FIRST + 1050;
	public static final int MENU_ID_VIEW = Menu.FIRST + 1100;
	public static final int MENU_ID_RESTORE = Menu.FIRST + 1200;
	//
	// public static final String WEEK_VIEW_START_HOUR =
	// "biz.advancedcalendar.WEEK_VIEW_START_HOUR";
	// public static final String WEEK_VIEW_START_DAY =
	// "biz.advancedcalendar.WEEK_VIEW_START_DAY";
	// public static final String WEEK_VIEW_DAYS_COUNT =
	// "biz.advancedcalendar.WEEK_VIEW_DAYS_COUNT";
	// public static final String WEEK_VIEW_HOURS_COUNT =
	// "biz.advancedcalendar.WEEK_VIEW_HOURS_COUNT";
	// public static final String WEEK_VIEW_SHOW_DAY_HEADERS =
	// "biz.advancedcalendar.WEEK_VIEW_SHOW_DAY_HEADERS";
	// public static final String WEEK_VIEW_INIT_ARGUMENTS =
	// "biz.advancedcalendar.WEEK_VIEW_INIT_ARGUMENTS";
	// public static final String MONTH_VIEW_START_MONTH =
	// "biz.advancedcalendar.MONTH_VIEW_START_MONTH";
	public static final String SYNC_SERVICE = "biz.advancedcalendar.SYNC_SERVICE";
	public static final String NOTIFICATION_SERVICE = "biz.advancedcalendar.NOTIFICATION_SERVICE";
	public static final String ALARM_SERVICE = "biz.advancedcalendar.ALARM_SERVICE";
	public static final String BOOT_COMPLETED_SERVICE = "biz.advancedcalendar.BOOT_COMPLETED_SERVICE";
	public static final String TIME_CHANGED_SERVICE = "biz.advancedcalendar.TIME_CHANGED_SERVICE";
	// public static final String ENTITY_TO_EDIT =
	// "biz.advancedcalendar.ENTITY_TO_EDIT";
	// public static final String CONTACT_RELATED_TASK_ID_ARRAY =
	// "biz.advancedcalendar.CONTACT_RELATED_TASK_ID_ARRAY";
	// public static final String CONTACT_RELATED_LABEL_ID_ARRAY =
	// "biz.advancedcalendar.CONTACT_RELATED_LABEL_ID_ARRAY";
	// public static final String CONTACT_RELATED_FILE_ID_ARRAY =
	// "biz.advancedcalendar.CONTACT_RELATED_FILE_ID_ARRAY";
	// public static final String CONTACT_RELATED_CONTACT_DATA_ARRAY =
	// "biz.advancedcalendar.CONTACT_RELATED_CONTACT_DATA_ARRAY";
	// public static final byte SYNC_STATUS_UNKNOWN = 0;
	// public static final byte SYNC_STATUS_SYNCHRONIZED = 1;
	// public static final byte SYNC_STATUS_SYNC_UP_REQUIRED = 2;
	// public static final byte SYNC_STATUS_SYNC_DOWN_REQUIRED = 4;
	// public static final byte SYNC_STATUS_SYNC_DOWN_RELATED_ENTITIES_REQUIRED = 4;
	public static final int CHOOSE_FILE_REQUESTCODE = 0;
	public static final int INSERT_OR_REPLACE_FILE_ACTION_INSERT_FILE = 0;
	public static final int INSERT_OR_REPLACE_FILE_ACTION_INSERT_FILE_HISTORY = 1;
	public static final int INSERT_OR_REPLACE_FILE_ACTION_UPDATE_FILE = 2;
	public static final int INSERT_OR_REPLACE_FILE_ACTION_UPDATE_FILE_HISTORY = 3;
	public static final String DATA_FRAGMENT = "biz.advancedcalendar.DATA_FRAGMENT";
	public static final String INIT_ARGUMENTS = "biz.advancedcalendar.INIT_ARGUMENTS";
	public static final String visibleContactDataFieldKeyArray = "biz.advancedcalendar.visibleContactDataFieldKeyArray";
	public static final String visibleContactDataFieldKeySet = "biz.advancedcalendar.visibleContactDataFieldKeySet";
	public static final String treeViewListItemDescriptionList = "biz.advancedcalendar.treeViewListItemDescriptionList";
	public static final String treeManager = "biz.advancedcalendar.treeManager";
	public static final String force_sync = "force_sync";
	public static final String notifications_new_message_ringtone = "notifications_new_message_ringtone";
	public static final String FragmentDatePicker = "biz.advancedcalendar.FragmentDatePicker";
	public static final String FragmentDatePicker2 = "biz.advancedcalendar.FragmentDatePicker2";
	public static final String FragmentCalendarView = "biz.advancedcalendar.FragmentCalendarView";
	public static final String FragmentTimePicker = "biz.advancedcalendar.FragmentTimePicker";
	public static final int MINIMUM_PASSWORD_LENGTH = 7;
	public static final String TIME_SKEW = "biz.advancedcalendar.TIME_SKEW";
	// public static final int
	// INTENT_EXTRA_ACTIVITY_ALARM_LAUNCH_MODE_SILENT_MODE_FOR_RESNOOZE = 1;
	// =Global.ACTIVITY_SERVICE;
	//
	// public static final String ADVANCEDCALENDAR_SERVER_ADDRESS =
	// "https://advancedcalendar.biz/";
	//
	public static final String ADVANCEDCALENDAR_SERVER_ADDRESS = "https://localhost:44304/";
	//
	// public static final String ADVANCEDCALENDAR_SERVER_ADDRESS =
	// "https://192.168.1.102:44304/";
	//
	public static final String SERVER_REGISTER_ACCOUNT_ADDRESS = CommonConstants.ADVANCEDCALENDAR_SERVER_ADDRESS
			+ "Account/Register";
	public static final int HTTP_URL_CONNECTION_READ_TIMEOUT = 60000;
	public static final int REQUEST_PICK_USER_ACCOUNT = 0;
	public static final int NUMBER_OF_TASKS_TO_SHOW_IN_COLLAPSED_STATE = 3;
	public static final String WEEK_VIEW_CORE_ID = "biz.advancedcalendar.WEEK_VIEW_CORE_ID";
	public static final int REQUEST_CODE_ADD_REMINDER = 0;
	public static final int REQUEST_CODE_EDIT_REMINDER = 1;
	public static final int REQUEST_CODE_PICK_ALARM_RINGTONE = 2;
	public static final int REQUEST_CODE_PICK_NOTIFICATION_RINGTONE = 3;
	public static final int REQUEST_CODE_PICK_COLOR = 4;
	public static final int REQUEST_CODE_PICK_PREDEFINED_COLOR = 5;
	public static final int REQUEST_CODE_SELECT_TASK_FOR_SORT_ORDER = 6;
	public static final int REQUEST_CODE_SELECT_TREE_ITEM_FOR_TASK_TREE = 7;
	public static final int REQUEST_CODE_SELECT_DAYS_OF_YEAR = 8;
	public static final int REQUEST_CODE_PICK_TASK_UNSET_COLOR = 9;
	public static final int REQUEST_CODE_PICK_CALENDAR_TODAY_DATE_TEXT_COLOR = 21;
	public static final int REQUEST_CODE_PICK_CALENDAR_TODAY_DATE_HIGHLIGHT_COLOR = 22;
	public static final int REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_CALENDAR_TIME_INTERVALS_AND_TEXT_MODE = 23;
	public static final int REQUEST_CODE_EDIT_CALENDARS = 24;
	public static final int REQUEST_CODE_CHOOSE_FILE = 25;
	public static final int REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_TASK_TREE = 26;
	public static final int REQUEST_CODE_PICK_INFORMATION_UNIT_MATRIX_FOR_AGENDA = 27;
	public static final long SCHEDULED_ALARM_DATE_TIME_TO_ACTUAL_ALARMED_DATE_TIME_TOLERANCE = 1000L * 5;
	// public static final String RINGTONE_SILENT =
	// "biz.advancedcalendar.RINGTONE_SILENT";
	// public static final DateTimeFormatter RFC1123_DATE_TIME_FORMATTER = DateTimeFormat
	// .forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC()
	// .withLocale(Locale.US);
	public static final String AlarmReceivedDateTime = "biz.advancedcalendar.AlarmReceivedDateTime";
	public static int[] DAY_OF_MONTH_ICON_RESOURCES = new int[] {
			R.drawable.calendar_1_24dp, R.drawable.calendar_2_24dp,
			R.drawable.calendar_3_24dp, R.drawable.calendar_4_24dp,
			R.drawable.calendar_5_24dp, R.drawable.calendar_6_24dp,
			R.drawable.calendar_7_24dp, R.drawable.calendar_8_24dp,
			R.drawable.calendar_9_24dp, R.drawable.calendar_10_24dp,
			R.drawable.calendar_11_24dp, R.drawable.calendar_12_24dp,
			R.drawable.calendar_13_24dp, R.drawable.calendar_14_24dp,
			R.drawable.calendar_15_24dp, R.drawable.calendar_16_24dp,
			R.drawable.calendar_17_24dp, R.drawable.calendar_18_24dp,
			R.drawable.calendar_19_24dp, R.drawable.calendar_20_24dp,
			R.drawable.calendar_21_24dp, R.drawable.calendar_22_24dp,
			R.drawable.calendar_23_24dp, R.drawable.calendar_24_24dp,
			R.drawable.calendar_25_24dp, R.drawable.calendar_26_24dp,
			R.drawable.calendar_27_24dp, R.drawable.calendar_28_24dp,
			R.drawable.calendar_29_24dp, R.drawable.calendar_30_24dp,
			R.drawable.calendar_31_24dp};
}
