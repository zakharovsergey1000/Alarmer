package biz.advancedcalendar.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.TimeFormat;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.ValueMustBe;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData.WantingItem;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.greendao.TaskOccurrence;
import biz.advancedcalendar.receivers.AlarmReceiver;
import biz.advancedcalendar.receivers.BootCompletedReceiver;
import biz.advancedcalendar.server.serialisers.InformationUnitSelectorSerializer;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.views.accessories.InformationUnit;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitRow;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrder;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrderComparatorBySortOrder;
import biz.advancedcalendar.views.accessories.InformationUnit.InformationUnitSortOrdersHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Helper {
	public static String getRingtoneTitle2(Context context, int preferenceId,
			String ringtoneString) {
		String silentRingtoneTitle = context.getResources().getString(
				R.string.ringtone_silent);
		if (ringtoneString.length() == 0) {
			return silentRingtoneTitle;
		} else {
			Uri ringtoneUri = Uri.parse(ringtoneString);
			Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
			// Uri defaultUri;
			int ringtoneType;
			if (preferenceId == R.string.preference_key_alarm_ringtone) {
				// defaultUri = Settings.System.DEFAULT_ALARM_ALERT_URI;
				ringtoneType = RingtoneManager.TYPE_ALARM;
			} else {
				// defaultUri = Settings.System.DEFAULT_NOTIFICATION_URI;
				ringtoneType = RingtoneManager.TYPE_NOTIFICATION;
			}
			// String defaultSilentRingtoneTitle = context.getResources().getString(
			// R.string.default_phone_ringtone_with_actual, silentRingtoneTitle);
			// String defaultUriString = defaultUri.toString();
			// String ringtoneUriString = ringtoneUri.toString();
			Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, ringtoneType);
			if (uri != null) {
				ringtone = RingtoneManager
						.getRingtone(context, Uri.parse(ringtoneString));
				if (ringtone == null) {
					ringtone = RingtoneManager.getRingtone(context,
							RingtoneManager.getDefaultUri(ringtoneType));
					if (ringtone != null) {
						return ringtone.getTitle(context);
					} else {
						return null;
					}
				} else {
					return ringtone.getTitle(context);
				}
			} else {
				return silentRingtoneTitle;
			}
		}
	}

	public static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT,
			Locale.US);

	public static boolean is24HourFormat(Context context) {
		boolean is24HourFormat;
		TimeFormat timeFormat = TimeFormat.fromInt((short) Helper
				.getIntegerPreferenceValueFromStringArray(context,
						R.string.preference_key_time_picker_time_format,
						R.array.time_picker_time_format_values_array,
						R.integer.time_picker_time_format_default_value));
		switch (timeFormat) {
		case FROM_LOCALE:
		default:
			is24HourFormat = android.text.format.DateFormat.is24HourFormat(context);
			break;
		case HOURS_12:
			is24HourFormat = false;
			break;
		case HOURS_24:
			is24HourFormat = true;
			break;
		}
		return is24HourFormat;
	}

	public static int getFirstDayOfWeek(Context context) {
		int firstDayOfWeek = Helper.getIntegerPreferenceValueFromStringArray(context,
				R.string.preference_key_first_day_of_week,
				R.array.first_day_of_week_values_array,
				R.integer.first_day_of_week_default_value);
		if (firstDayOfWeek == 0) {
			Calendar calendar = Calendar.getInstance();
			firstDayOfWeek = calendar.getFirstDayOfWeek();
		}
		return firstDayOfWeek;
	}

	/** Checks whether the device currently has a network connection */
	public static boolean isDeviceOnline(Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public static int getWeekdayNumberInMonthFromEntityOrDefault(Context context,
			RecurrenceInterval recurrenceInterval) {
		int weekDayNumberInMonth;
		if (recurrenceInterval == RecurrenceInterval.MONTHS_ON_NTH_WEEK_DAY) {
			List<TaskOccurrence> taskOccurrences = recurrenceInterval
					.getTaskOccurrences();
			if (taskOccurrences.size() > 0) {
				TaskOccurrence taskOccurrence = taskOccurrences.get(taskOccurrences
						.size() - 1);
				weekDayNumberInMonth = Helper.getIntegerOrPreferenceValueFromStringArray(
						context, taskOccurrence.getOrdinalNumber(),
						R.string.preference_key_week_day_number,
						R.array.week_day_number_values_array,
						R.integer.week_day_number_default_value);
			} else {
				weekDayNumberInMonth = Helper.getIntegerPreferenceValueFromStringArray(
						context, R.string.preference_key_week_day_number,
						R.array.week_day_number_values_array,
						R.integer.week_day_number_default_value);
			}
		} else {
			weekDayNumberInMonth = Helper.getIntegerPreferenceValueFromStringArray(
					context, R.string.preference_key_week_day_number,
					R.array.week_day_number_values_array,
					R.integer.week_day_number_default_value);
		}
		return weekDayNumberInMonth;
	}

	// public static RecurrenceInterval getRecurrenceIntervalFromEntityOrDefault(Context
	// context,
	// short value) {
	// RecurrenceInterval recurrenceInterval = RecurrenceInterval.fromInt(value);
	// if (recurrenceInterval == null) {
	// value = (short) Helper.getIntegerPreferenceValueFromStringArray(context,
	// R.string.preference_key_time_unit,
	// R.array.time_unit_values_array,
	// R.integer.time_unit_default_value);
	// recurrenceInterval = RecurrenceInterval.fromInt(value);
	// }
	// return recurrenceInterval;
	// }
	public static int setSpinnerToPreferenceValueFromStringArray(Activity context,
			int spinnerId, int preferenceKey, int stringArrayId, int defaultValue,
			OnItemSelectedListener listener) {
		int index = Helper.getIndexOfPreferenceValueInStringArray(context, preferenceKey,
				stringArrayId, defaultValue);
		Spinner spinner = (Spinner) context.findViewById(spinnerId);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(index);
		spinner.setOnItemSelectedListener(listener);
		return index;
	}

	public static int setSpinnerToPreferenceValueFromStringArray(Activity context,
			Spinner spinner, int preferenceKey, int stringArrayId, int defaultValue,
			OnItemSelectedListener listener) {
		int index = Helper.getIndexOfPreferenceValueInStringArray(context, preferenceKey,
				stringArrayId, defaultValue);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(index);
		spinner.setOnItemSelectedListener(listener);
		return index;
	}

	public static int setSpinnerToValueFromStringArray(Activity context, Spinner spinner,
			Integer value, int preferenceKey, int stringArrayId, int defaultValue,
			OnItemSelectedListener listener) {
		int index = Helper.getIndexOfValueInStringArray(context, value, preferenceKey,
				stringArrayId, defaultValue);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(index);
		spinner.setOnItemSelectedListener(listener);
		return index;
	}

	public static int setSpinnerToValueFromStringArray(Activity context, int spinnerId,
			Integer value, int preferenceKey, int stringArrayId, int defaultValue,
			OnItemSelectedListener listener) {
		int index = Helper.getIndexOfValueInStringArray(context, value, preferenceKey,
				stringArrayId, defaultValue);
		Spinner spinner = (Spinner) context.findViewById(spinnerId);
		spinner.setOnItemSelectedListener(null);
		spinner.setSelection(index);
		spinner.setOnItemSelectedListener(listener);
		return index;
	}

	public static int getIndexOfPreferenceValueInStringArray(Context context,
			int preferenceKey, int stringArrayId, int defaultValue) {
		int index;
		try {
			String stringValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(context.getResources().getString(preferenceKey), null);
			index = Helper.findIndexOfValueInStringArray(context, stringValue,
					stringArrayId);
			if (index == -1) {
				index = Helper.findIndexOfValueInStringArray(context,
						String.valueOf(defaultValue), stringArrayId);
			}
		} catch (ClassCastException e) {
			index = Helper.findIndexOfValueInStringArray(context,
					String.valueOf(defaultValue), stringArrayId);
		}
		return index;
	}

	public static int getIndexOfPreferenceValueInStringArray2(Context context,
			int preferenceKey, int stringArrayId, int defaultValue) {
		int index;
		try {
			String stringValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(context.getResources().getString(preferenceKey), null);
			if (stringValue == null
					|| (index = Helper.findIndexOfValueInStringArray(context,
							stringValue, stringArrayId)) == -1) {
				stringValue = String.valueOf(defaultValue);
			} else {
				return index;
			}
			index = Helper.findIndexOfValueInStringArray(context, stringValue,
					stringArrayId);
		} catch (ClassCastException e) {
			index = Helper.findIndexOfValueInStringArray(context,
					String.valueOf(defaultValue), stringArrayId);
		}
		return index;
	}

	public static int getIndexOfValueInStringArray(Context context, Integer value,
			int preferenceKey, int stringArrayId, int defaultValue) {
		int index;
		if (value != null) {
			index = Helper.findIndexOfValueInStringArray(context, String.valueOf(value),
					stringArrayId);
		} else {
			index = -1;
		}
		if (index == -1) {
			index = Helper.getIndexOfPreferenceValueInStringArray(context, preferenceKey,
					stringArrayId, defaultValue);
		}
		return index;
	}

	public static String getStringValueFromStringArray(Context context, int index,
			int stringArrayId, String defaultValue) {
		String[] entryValues = context.getResources().getStringArray(stringArrayId);
		String value;
		if (index >= 0 && index < entryValues.length) {
			value = entryValues[index];
		} else {
			value = defaultValue;
		}
		return value;
	}

	public static int getIntegerValueFromStringArray(Context context, int stringArrayId,
			int index, int defaultValue) {
		String[] entryValues = context.getResources().getStringArray(stringArrayId);
		String stringValue;
		int intValue;
		try {
			stringValue = entryValues[index];
			intValue = Integer.parseInt(stringValue);
		} catch (ArrayIndexOutOfBoundsException e) {
			intValue = defaultValue;
		} catch (NumberFormatException e) {
			intValue = defaultValue;
		}
		return intValue;
	}

	public static int getIntegerValueFromIntegerArray(Context context, int index,
			int integerArrayId, int defaultValue) {
		int[] entryValues = context.getResources().getIntArray(integerArrayId);
		int value;
		if (index >= 0 && index < entryValues.length) {
			value = entryValues[index];
		} else {
			value = defaultValue;
		}
		return value;
	}

	public static Long[] getLongArray(Context context, String preferenceKey,
			Long[] defaultValue) {
		String jsonString;
		try {
			jsonString = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(preferenceKey, null);
		} catch (ClassCastException e) {
			jsonString = null;
		}
		if (jsonString == null) {
			return null;
		}
		Long[] longArray;
		try {
			longArray = new GsonBuilder().create().fromJson(jsonString, Long[].class);
		} catch (JsonSyntaxException e) {
			longArray = null;
		}
		return longArray;
	}

	public static void setLongArray(Context context, String preferenceKey,
			Long[] selectedCalendars) {
		String jsonString = new GsonBuilder().create().toJson(selectedCalendars,
				Long[].class);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
				context).edit();
		editor.putString(preferenceKey, jsonString);
		editor.commit();
	}

	public static int getIntegerOrPreferenceValueFromStringArray(Context context,
			int intValue, int preferenceKeyId, int stringArrayId, int defaultValueId) {
		int index = Helper.findIndexOfValueInStringArray(context,
				String.valueOf(intValue), stringArrayId);
		if (index < 0) {
			return Helper.getIntegerPreferenceValueFromStringArray(context,
					preferenceKeyId, stringArrayId, defaultValueId);
		} else {
			return intValue;
		}
	}

	public static int getIntegerPreferenceValueFromStringArray(Context context,
			int preferenceKeyId, int stringArrayId, int defaultValueId) {
		int intValue;
		int defaultValue = context.getResources().getInteger(defaultValueId);
		try {
			String stringValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(context.getResources().getString(preferenceKeyId), null);
			if (stringValue == null) {
				intValue = defaultValue;
			} else {
				intValue = Integer.parseInt(stringValue);
				int index = Helper.findIndexOfValueInStringArray(context, stringValue,
						stringArrayId);
				if (index == -1) {
					intValue = defaultValue;
				}
			}
		} catch (ClassCastException e) {
			intValue = defaultValue;
		} catch (NumberFormatException e) {
			intValue = defaultValue;
		}
		return intValue;
	}

	public static boolean getBooleanPreferenceValue(Context context, int preferenceKeyId,
			int defaultValueId) {
		boolean booleanValue;
		boolean defaultValue = context.getResources().getBoolean(defaultValueId);
		try {
			booleanValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean(context.getResources().getString(preferenceKeyId),
							defaultValue);
		} catch (ClassCastException e) {
			booleanValue = defaultValue;
		}
		return booleanValue;
	}

	public static boolean getBooleanPreferenceValue(Context context, int preferenceKeyId,
			boolean defaultValue) {
		boolean booleanValue;
		try {
			booleanValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean(context.getResources().getString(preferenceKeyId),
							defaultValue);
		} catch (ClassCastException e) {
			booleanValue = defaultValue;
		}
		return booleanValue;
	}

	public static boolean getBooleanPreferenceValue(Context context,
			String preferenceKey, boolean defaultValue) {
		boolean booleanValue;
		try {
			booleanValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getBoolean(preferenceKey, defaultValue);
		} catch (ClassCastException e) {
			booleanValue = defaultValue;
		}
		return booleanValue;
	}

	public static int getIntegerFromStringPreferenceValue(Context context,
			int preferenceKeyId, Integer divider, int defaultValue, Integer minValue,
			Integer maxValue) {
		int intValue;
		try {
			String stringValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(context.getResources().getString(preferenceKeyId), null);
			if (stringValue == null) {
				intValue = defaultValue;
			} else {
				intValue = Integer.parseInt(stringValue);
				if (divider != null) {
					intValue = intValue / divider;
				}
				if (minValue != null && intValue < minValue) {
					intValue = defaultValue;
				}
				if (maxValue != null && intValue > maxValue) {
					intValue = defaultValue;
				}
			}
		} catch (ClassCastException e) {
			intValue = defaultValue;
		} catch (NumberFormatException e) {
			intValue = defaultValue;
		}
		return intValue;
	}

	public static long getLongFromStringPreferenceValue(Context context,
			int preferenceKeyId, long defaultValue, Long minValue, Long maxValue) {
		long intValue;
		try {
			String stringValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(context.getResources().getString(preferenceKeyId), null);
			if (stringValue == null) {
				intValue = defaultValue;
			} else {
				intValue = Long.parseLong(stringValue);
				if (minValue != null && intValue < minValue) {
					intValue = defaultValue;
				}
				if (maxValue != null && intValue > maxValue) {
					intValue = defaultValue;
				}
			}
		} catch (ClassCastException e) {
			intValue = defaultValue;
		} catch (NumberFormatException e) {
			intValue = defaultValue;
		}
		return intValue;
	}

	public static InformationUnitMatrix createInformationUnitMatrix(Context context,
			int preferenceKey, int defaultValueId) {
		Resources resources = context.getResources();
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(InformationUnitSelector.class,
				new InformationUnitSelectorSerializer());
		Gson gson = gsonBuilder.create();
		String informationUnitMatrixString;
		InformationUnitMatrix informationUnitMatrix;
		try {
			informationUnitMatrixString = Helper.getStringPreferenceValue(context,
					preferenceKey, resources.getString(defaultValueId));
			informationUnitMatrix = gson.fromJson(informationUnitMatrixString,
					InformationUnitMatrix.class);
		} catch (JsonSyntaxException e) {
			informationUnitMatrixString = resources.getString(defaultValueId);
			informationUnitMatrix = gson.fromJson(informationUnitMatrixString,
					InformationUnitMatrix.class);
		}
		if (informationUnitMatrix != null) {
			ArrayList<InformationUnitRow> informationUnitRows = informationUnitMatrix
					.getInformationUnitRows();
			if (informationUnitRows != null) {
				for (Iterator<InformationUnitRow> rowIterator = informationUnitRows
						.iterator(); rowIterator.hasNext();) {
					InformationUnitRow informationUnitRow = rowIterator.next();
					if (informationUnitRow == null) {
						rowIterator.remove();
					} else {
						ArrayList<InformationUnit> informationUnits = informationUnitRow
								.getInformationUnits();
						if (informationUnits == null) {
							rowIterator.remove();
						} else {
							for (Iterator<InformationUnit> columnIterator = informationUnits
									.iterator(); columnIterator.hasNext();) {
								InformationUnit informationUnit = columnIterator.next();
								if (informationUnit == null) {
									columnIterator.remove();
								} else {
									InformationUnitSelector informationUnitSelector = informationUnit
											.getInformationUnitSelector();
									if (informationUnitSelector == null) {
										columnIterator.remove();
									} else if (informationUnitSelector == InformationUnitSelector.ANY_STRING) {
										String whateverDelimiterString = informationUnit
												.getWhateverDelimiterString();
										if (whateverDelimiterString == null) {
											whateverDelimiterString = " ";
										}
									}
								}
							}
							int size = informationUnits.size();
							if (size == 0) {
								rowIterator.remove();
							}
						}
					}
				}
			} else {
				informationUnitRows = new ArrayList<InformationUnitRow>();
				informationUnitMatrix.setInformationUnitRows(informationUnitRows);
			}
		} else {
			ArrayList<InformationUnitRow> informationUnitRows = new ArrayList<InformationUnitRow>();
			informationUnitMatrix = new InformationUnitMatrix(informationUnitRows);
		}
		return informationUnitMatrix;
	}

	public static InformationUnitSortOrdersHolder createInformationUnitSortOrdersHolder(
			Context context,
			int preferenceKey,
			int defaultValueId,
			InformationUnitSortOrderComparatorBySortOrder informationUnitSortOrderComparatorBySortOrder) {
		Resources resources = context.getResources();
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		String string;
		InformationUnitSortOrdersHolder informationUnitSortOrdersHolder;
		try {
			string = Helper.getStringPreferenceValue(context, preferenceKey,
					resources.getString(defaultValueId));
			informationUnitSortOrdersHolder = gson.fromJson(string,
					InformationUnitSortOrdersHolder.class);
		} catch (JsonSyntaxException e) {
			string = resources.getString(defaultValueId);
			informationUnitSortOrdersHolder = gson.fromJson(string,
					InformationUnitSortOrdersHolder.class);
		}
		if (informationUnitSortOrdersHolder != null) {
			ArrayList<InformationUnitSortOrder> informationUnitSortOrders = informationUnitSortOrdersHolder
					.getInformationUnitSortOrders();
			if (informationUnitSortOrders != null) {
				Collections.sort(informationUnitSortOrders,
						informationUnitSortOrderComparatorBySortOrder);
				Iterator<InformationUnitSortOrder> ordersIterator = informationUnitSortOrders
						.iterator();
				InformationUnitSortOrder current = null;
				while (current == null && ordersIterator.hasNext()) {
					current = ordersIterator.next();
					if (current == null) {
						ordersIterator.remove();
					}
				}
				while (ordersIterator.hasNext()) {
					InformationUnitSortOrder next = ordersIterator.next();
					if (next == null) {
						ordersIterator.remove();
					} else {
						if (next.getSortOrder() == current.getSortOrder()) {
							ordersIterator.remove();
						} else {
							current = next;
						}
					}
				}
				Collections.sort(informationUnitSortOrders);
				int index = 0;
				for (ordersIterator = informationUnitSortOrders.iterator(); ordersIterator
						.hasNext();) {
					InformationUnitSortOrder informationUnitSortOrder = ordersIterator
							.next();
					InformationUnitSelector informationUnitSelector = InformationUnitSelector
							.fromInt((byte) informationUnitSortOrder.getSortOrder());
					if (informationUnitSelector == null) {
						ordersIterator.remove();
					} else {
						informationUnitSortOrder.setIndex(index);
						index = index + 1;
					}
				}
			} else {
				informationUnitSortOrders = new ArrayList<InformationUnitSortOrder>();
				informationUnitSortOrdersHolder
						.setInformationUnitSortOrders(informationUnitSortOrders);
			}
		} else {
			ArrayList<InformationUnitSortOrder> informationUnitSortOrders = new ArrayList<InformationUnitSortOrder>();
			informationUnitSortOrdersHolder = new InformationUnitSortOrdersHolder(
					informationUnitSortOrders);
		}
		return informationUnitSortOrdersHolder;
	}

	public static String getStringPreferenceValue(Context context, String preferenceKey,
			String defaultValue) {
		String stringValue;
		try {
			stringValue = PreferenceManager.getDefaultSharedPreferences(context)
					.getString(preferenceKey, defaultValue);
		} catch (ClassCastException e) {
			stringValue = defaultValue;
		}
		return stringValue;
	}

	public static String getStringPreferenceValue(Context context, int preferenceKey,
			String defaultValue) {
		return Helper.getStringPreferenceValue(context,
				context.getResources().getString(preferenceKey), defaultValue);
	}

	public static int getIntegerPreferenceValue(Context context, String preferenceKey,
			int defaultValue, Integer minValue, Integer maxValue) {
		int value;
		try {
			value = PreferenceManager.getDefaultSharedPreferences(context).getInt(
					preferenceKey, defaultValue);
			if (minValue != null && value < minValue) {
				value = defaultValue;
			}
			if (maxValue != null && value > maxValue) {
				value = defaultValue;
			}
		} catch (ClassCastException e) {
			value = defaultValue;
		}
		return value;
	}

	public static int getIntegerPreferenceValue(Context context, int preferenceKeyId,
			int defaultValue, Integer minValue, Integer maxValue) {
		int value;
		try {
			value = PreferenceManager.getDefaultSharedPreferences(context).getInt(
					context.getResources().getString(preferenceKeyId), defaultValue);
			if (minValue != null && value < minValue) {
				value = defaultValue;
			}
			if (maxValue != null && value > maxValue) {
				value = defaultValue;
			}
		} catch (ClassCastException e) {
			value = defaultValue;
		}
		return value;
	}

	public static long getLongPreferenceValue(Context context, String preferenceKey,
			long defaultValue, Long minValue, Long maxValue) {
		long value;
		try {
			value = PreferenceManager.getDefaultSharedPreferences(context).getLong(
					preferenceKey, defaultValue);
			if (minValue != null && value < minValue) {
				value = defaultValue;
			}
			if (maxValue != null && value > maxValue) {
				value = defaultValue;
			}
		} catch (ClassCastException e) {
			value = defaultValue;
		}
		return value;
	}

	public static long getLongPreferenceValue(Context context, int preferenceKeyId,
			long defaultValue, Long minValue, Long maxValue) {
		long value;
		try {
			value = PreferenceManager.getDefaultSharedPreferences(context).getLong(
					context.getResources().getString(preferenceKeyId), defaultValue);
			if (minValue != null && value < minValue) {
				value = defaultValue;
			}
			if (maxValue != null && value > maxValue) {
				value = defaultValue;
			}
		} catch (ClassCastException e) {
			value = defaultValue;
		}
		return value;
	}

	public static int getIntegerFromIntegerValue(int intValue, int minValue, int maxValue) {
		if (intValue < minValue) {
			intValue = minValue;
		}
		if (intValue > maxValue) {
			intValue = maxValue;
		}
		return intValue;
	}

	public static int getIntegerFromStringValue(String stringValue, int defaultValue,
			int minValue, int maxValue) {
		int intValue;
		stringValue = stringValue.trim();
		if (stringValue.length() == 0) {
			intValue = minValue;
		} else {
			try {
				intValue = Integer.parseInt(stringValue);
				if (intValue < minValue) {
					intValue = minValue;
				}
				if (intValue > maxValue) {
					intValue = maxValue;
				}
			} catch (NumberFormatException e) {
				intValue = minValue;
			}
		}
		return intValue;
	}

	public static Integer getValidEditTextIntegerValueOrNull(EditText editText,
			String pluralString, int minValue, int maxValue,
			boolean isErrorTextDisplayed, boolean isToastTextDisplayed) {
		String stringValue;
		Editable editable = editText.getText();
		if (editable == null) {
			return null;
		} else {
			stringValue = editable.toString().trim();
		}
		String title = (String) editText.getTag(R.string.tag_key_checkbox_title);
		Context context = editText.getContext();
		Integer value = Helper.getValidIntegerFromStringOrNull(context, stringValue,
				pluralString, minValue, maxValue, title, isToastTextDisplayed, editText);
		editText.setError(null);
		if (isErrorTextDisplayed && value == null) {
			String errorText = (String) editText
					.getTag(R.string.tag_key_checkbox_error_text);
			editText.setError(errorText);
		}
		return value;
	}

	private static Integer getValidIntegerFromStringOrNull(Context context,
			String stringValue, String pluralString, int minValue, int maxValue,
			String title, boolean isToastTextDisplayed, EditText editText) {
		if (title == null) {
			title = "";
		}
		try {
			int intValue = Integer.parseInt(stringValue);
			if (intValue < minValue) {
				ValueMustBe.setMinValue(minValue);
				String text = String
						.format(context
								.getResources()
								.getString(
										(Integer) editText
												.getTag(R.string.tag_key_value_must_be_greater_than_or_equal_to)),
								minValue);
				ValueMustBe.setText(text);
				editText.setTag(R.string.tag_key_value_must_be,
						ValueMustBe.GREATER_THAN_OR_EQUAL_TO);
				if (isToastTextDisplayed) {
					Toast.makeText(context, text, Toast.LENGTH_LONG).show();
				}
				return intValue;
			} else if (intValue > maxValue) {
				ValueMustBe.setMaxValue(maxValue);
				String text = String
						.format(context
								.getResources()
								.getString(
										(Integer) editText
												.getTag(R.string.tag_key_value_must_be_less_than_or_equal_to)),
								maxValue);
				ValueMustBe.setText(text);
				editText.setTag(R.string.tag_key_value_must_be,
						ValueMustBe.LESS_THAN_OR_EQUAL_TO);
				if (isToastTextDisplayed) {
					Toast.makeText(context, text, Toast.LENGTH_LONG).show();
				}
				return intValue;
			} else {
				editText.setTag(R.string.tag_key_value_must_be, null);
				return intValue;
			}
		} catch (NumberFormatException e) {
			ValueMustBe.setMinValue(minValue);
			ValueMustBe.setMaxValue(maxValue);
			String text = String
					.format(context
							.getResources()
							.getString(
									(Integer) editText
											.getTag(R.string.tag_key_value_must_be_within_bounds)),
							minValue, maxValue, pluralString);
			ValueMustBe.setText(text);
			editText.setTag(R.string.tag_key_value_must_be, ValueMustBe.WITHIN_BOUNDS);
			if (isToastTextDisplayed) {
				Toast.makeText(context, text, Toast.LENGTH_LONG).show();
			}
			return null;
		}
	}

	public static boolean isEditTextIntegerValueValid(EditText editText, int minValue,
			int maxValue, boolean isErrorTextDisplayed, boolean isToastTextDisplayed) {
		String title = (String) editText.getTag(R.string.tag_key_checkbox_title);
		String stringValue = editText.getText().toString().trim();
		Context context = editText.getContext();
		boolean valid = Helper.isStringIntegerValueValid(context, stringValue, minValue,
				maxValue, title, isToastTextDisplayed);
		editText.setError(null);
		if (isErrorTextDisplayed && !valid) {
			String errorText = (String) editText
					.getTag(R.string.tag_key_checkbox_error_text);
			editText.setError(errorText);
		}
		return valid;
	}

	public static boolean isStringIntegerValueValid(Context context, String stringValue,
			int minValue, int maxValue, String title, boolean isToastTextDisplayed) {
		stringValue = stringValue.trim();
		if (title == null) {
			title = "";
		}
		if (stringValue.length() == 0 && minValue == 0) {
			return true;
		} else {
			try {
				int intValue = Integer.parseInt(stringValue);
				if (intValue < minValue) {
					if (isToastTextDisplayed) {
						String text = String
								.format(context
										.getResources()
										.getString(
												R.string.toast_text_value_must_be_greater_than_or_equal_to),
										minValue, title);
						Toast.makeText(context, text, Toast.LENGTH_LONG).show();
					}
					return false;
				} else if (intValue > maxValue) {
					if (isToastTextDisplayed) {
						String text = String
								.format(context
										.getResources()
										.getString(
												R.string.toast_text_value_must_be_less_than_or_equal_to),
										maxValue, title);
						Toast.makeText(context, text, Toast.LENGTH_LONG).show();
					}
					return false;
				} else {
					return true;
				}
			} catch (NumberFormatException e) {
				if (isToastTextDisplayed) {
					String text = String.format(
							context.getResources().getString(
									R.string.toast_text_value_must_be_within_bounds),
							minValue, maxValue, title);
					Toast.makeText(context, text, Toast.LENGTH_LONG).show();
				}
				return false;
			}
		}
	}

	public static void displayMessageValueMustBeWithinBounds(Context context) {
		Toast.makeText(
				context,
				R.string.fragment_edit_task_part_main_checkbox_task_end_time_please_select_date_first,
				Toast.LENGTH_SHORT).show();
	}

	public static int findIndexOfValueInStringArray(Context context, String value,
			int stringArrayId) {
		int index = -1;
		if (value != null) {
			String[] entryValues = context.getResources().getStringArray(stringArrayId);
			for (int i = 0; i < entryValues.length; i++) {
				if (entryValues[i].equals(value)) {
					index = i;
					break;
				}
			}
		}
		return index;
	}

	// public static List<CalendarViewTaskOccurrence>
	// selectTaskOccurrencesTakingWholeDateTimeInterval(
	// Context context, long borderStartDateTime, long borderEndDateTime,
	// List<CalendarViewTaskOccurrence> selectedCalendarViewTaskOccurrences,
	// LinkedList<CalendarViewTaskOccurrence> cacheOfCalendarViewTaskOccurrences,
	// List<Task> totalCalendarViewTaskOccurrences) {
	// if (cacheOfCalendarViewTaskOccurrences == null) {
	// cacheOfCalendarViewTaskOccurrences = new LinkedList<CalendarViewTaskOccurrence>();
	// }
	// selectedCalendarViewTaskOccurrences.clear();
	// for (Task task : totalCalendarViewTaskOccurrences) {
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = task
	// .selectTaskOccurrencesTakingWholeDateTimeInterval(context,
	// borderStartDateTime, borderEndDateTime);
	// for (CalendarViewTaskOccurrence calendarViewTaskOccurrence :
	// calendarViewTaskOccurrences) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
	// }
	// }
	// return selectedCalendarViewTaskOccurrences;
	// }
	// public static List<CalendarViewTaskOccurrence>
	// selectTasksIntersectingTimeIntervalOnOneBorder(
	// Context context, long borderStartDateTime, long borderEndDateTime,
	// List<CalendarViewTaskOccurrence> selectedCalendarViewTaskOccurrences,
	// List<Task> tasks) {
	// selectedCalendarViewTaskOccurrences.clear();
	// for (Task task : tasks) {
	// if (task.getRecurrenceIntervalValue() != RecurrenceInterval.ONE_TIME.getValue()) {
	// long taskStartDateTime;
	// taskStartDateTime = task.getStartDateTime();
	// if (borderEndDateTime <= taskStartDateTime) {
	// continue;
	// }
	// Long eventStartDateTime = null;
	// boolean searchForMoreRepetitions = false;
	// long controlDateTime = borderStartDateTime;
	// List<TaskOccurrence> repetitions = task
	// .getTaskOccurrenceList();
	// // search for first repetition
	// if (borderStartDateTime < taskStartDateTime
	// && taskStartDateTime < borderEndDateTime) {
	// eventStartDateTime = taskStartDateTime;
	// } else {
	// eventStartDateTime = task.getClosestRepetitionStartDateTime(context,
	// borderStartDateTime, true, false);
	// }
	// if (eventStartDateTime != null) {
	// long eventEndDateTime = eventStartDateTime + task.getEndDateTime()
	// - taskStartDateTime;
	// if (eventStartDateTime < borderStartDateTime
	// && borderStartDateTime < eventEndDateTime
	// && eventEndDateTime < borderEndDateTime
	// || borderStartDateTime < eventStartDateTime
	// && eventStartDateTime < borderEndDateTime
	// && borderEndDateTime < eventEndDateTime) {
	// selectedCalendarViewTaskOccurrences
	// .add(new CalendarViewTaskOccurrence(task,
	// eventStartDateTime, eventEndDateTime));
	// }
	// if (eventStartDateTime < borderEndDateTime) {
	// controlDateTime = eventStartDateTime;
	// searchForMoreRepetitions = true;
	// }
	// }
	// // search for next repetition
	// if (searchForMoreRepetitions) {
	// eventStartDateTime = task.getClosestRepetitionStartDateTime(context,
	// controlDateTime, false, false);
	// if (eventStartDateTime != null) {
	// long eventEndDateTime = eventStartDateTime
	// + task.getEndDateTime() - taskStartDateTime;
	// if (eventStartDateTime < borderStartDateTime
	// && borderStartDateTime < eventEndDateTime
	// && eventEndDateTime < borderEndDateTime
	// || borderStartDateTime < eventStartDateTime
	// && eventStartDateTime < borderEndDateTime
	// && borderEndDateTime < eventEndDateTime) {
	// selectedCalendarViewTaskOccurrences
	// .add(new CalendarViewTaskOccurrence(task,
	// eventStartDateTime, eventEndDateTime));
	// }
	// }
	// }
	// continue;
	// }
	// Long taskStartDateTime, taskEndDateTime;
	// taskStartDateTime = task.getStartDateTime();
	// taskEndDateTime = task.getEndDateTime();
	// if (taskStartDateTime == null && taskEndDateTime == null) {
	// continue;
	// }
	// CalendarViewTaskOccurrence calendarViewTaskOccurrence = new
	// CalendarViewTaskOccurrence(
	// task, taskStartDateTime, taskEndDateTime);
	// if (taskStartDateTime != null && taskEndDateTime == null) {
	// if (borderStartDateTime <= taskStartDateTime
	// && taskStartDateTime < borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
	// }
	// continue;
	// }
	// if (taskStartDateTime == null && taskEndDateTime != null) {
	// if (borderStartDateTime <= taskEndDateTime
	// && taskEndDateTime < borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
	// }
	// continue;
	// }
	// if (taskStartDateTime != null && taskEndDateTime != null) {
	// if (borderStartDateTime <= taskStartDateTime
	// && taskStartDateTime < borderEndDateTime
	// && borderEndDateTime < taskEndDateTime
	// || taskStartDateTime < borderStartDateTime
	// && borderStartDateTime <= taskEndDateTime
	// && taskEndDateTime < borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
	// }
	// continue;
	// }
	// }
	// return selectedCalendarViewTaskOccurrences;
	// }
	// public static List<CalendarViewTaskOccurrence>
	// selectTaskOccurrencesTouchingDateTimeInterval(
	// Context context, long borderStartDateTime, long borderEndDateTime,
	// List<CalendarViewTaskOccurrence> selectedCalendarViewTaskOccurrences,
	// HashSet<CalendarViewTaskOccurrence> cacheOfCalendarViewTaskOccurrences,
	// List<Task> tasks) {
	// selectedCalendarViewTaskOccurrences.clear();
	// for (Task task : tasks) {
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = task
	// .selectTaskOccurrencesTouchingDateTimeInterval(context,
	// borderStartDateTime, borderEndDateTime);
	// for (CalendarViewTaskOccurrence calendarViewTaskOccurrence :
	// calendarViewTaskOccurrences) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
	// }
	// }
	// return selectedCalendarViewTaskOccurrences;
	// }
	// public static List<CalendarViewTaskOccurrence>
	// selectTaskOccurrencesGoingInsideDateTimeIntervalOrIntersectingTimeIntervalOnOneBorder(
	// Context context, long borderStartDateTime, long borderEndDateTime,
	// List<CalendarViewTaskOccurrence> selectedCalendarViewTaskOccurrences,
	// List<Task> tasks) {
	// for (Task task : tasks) {
	// List<CalendarViewTaskOccurrence> calendarViewTaskOccurrences = task
	// .selectTaskOccurrencesGoingInsideDateTimeIntervalOrIntersectingTimeIntervalOnOneBorder(
	// context, borderStartDateTime, borderEndDateTime);
	// for (CalendarViewTaskOccurrence calendarViewTaskOccurrence :
	// calendarViewTaskOccurrences) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTaskOccurrence);
	// }
	// }
	// return selectedCalendarViewTaskOccurrences;
	// }
	// public static List<CalendarViewTaskOccurrence>
	// selectTaskOccurrencesInsideDateTimeInterval2(
	// Context context, long borderStartDateTime, long borderEndDateTime,
	// List<CalendarViewTaskOccurrence> selectedCalendarViewTaskOccurrences,
	// List<Task> tasks) {
	// selectedCalendarViewTaskOccurrences.clear();
	// for (Task task : tasks) {
	// if (task.getRecurrenceIntervalValue() != RecurrenceInterval.ONE_TIME.getValue()) {
	// // if (borderStartDateTime == null || borderEndDateTime == null) {
	// // continue;
	// // }
	// long taskStartDateTime, taskEndDateTime;
	// taskStartDateTime = task.getStartDateTime();
	// taskEndDateTime = task.getEndDateTime();
	// if (borderEndDateTime <= taskStartDateTime) {
	// continue;
	// }
	// long length = taskEndDateTime - taskStartDateTime;
	// Long eventStartDateTime = null;
	// boolean searchForMoreRepetitions = true;
	// long controlDateTime = borderStartDateTime - 1;
	// while (searchForMoreRepetitions) {
	// searchForMoreRepetitions = false;
	// if (taskStartDateTime == borderStartDateTime) {
	// eventStartDateTime = taskStartDateTime;
	// } else {
	// eventStartDateTime = task.getClosestEventStartDateTime(context,
	// controlDateTime, false, false,
	// task.getDailyRepetitionList2(context));
	// }
	// if (eventStartDateTime != null) {
	// Long eventEndDateTimeMillis = eventStartDateTime + length;
	// if (eventStartDateTime >= borderStartDateTime
	// && eventEndDateTimeMillis <= borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences
	// .add(new CalendarViewTaskOccurrence(task,
	// // task.getId(),
	// eventStartDateTime, eventEndDateTimeMillis
	// // ,
	// // task.getText(),
	// // RecurrenceInterval.fromInt(task.getRecurrenceIntervalValue()),
	// // task.getPriority(),
	// // task.getColor(),
	// // SyncStatus.fromInt(task.getSyncStatus())
	// ));
	// controlDateTime = eventStartDateTime;
	// searchForMoreRepetitions = true;
	// }
	// }
	// }
	// continue;
	// }
	// Long taskStartDateTime, taskEndDateTime;
	// taskStartDateTime = task.getStartDateTime();
	// taskEndDateTime = task.getEndDateTime();
	// CalendarViewTaskOccurrence calendarViewTask = new CalendarViewTaskOccurrence(
	// task/* , task.getId() */, taskStartDateTime, taskEndDateTime/* , task.
	// * getText
	// * (),
	// * RecurrenceInterval
	// * .
	// * fromInt
	// * (task.
	// * getRecurrenceIntervalValue
	// * ()),
	// * task.
	// * getPriority
	// * (),
	// * task
	// * .getColor
	// * (),
	// * Task.
	// * SYNC_STATUS
	// * .
	// * fromInt
	// * (task .
	// * getSyncStatus
	// * ()) */
	// );
	// // if (borderStartDateTime == null && borderEndDateTime == null) {
	// // selectedCalendarViewTaskOccurrences.add(calendarViewTask);
	// // continue;
	// // }
	// if (taskStartDateTime != null && taskEndDateTime == null
	// && borderStartDateTime != null && borderEndDateTime == null) {
	// if (borderStartDateTime <= taskStartDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTask);
	// }
	// continue;
	// }
	// if (taskStartDateTime == null && taskEndDateTime != null
	// && borderStartDateTime != null && borderEndDateTime == null) {
	// continue;
	// }
	// if (taskStartDateTime != null && taskEndDateTime != null
	// && borderStartDateTime != null && borderEndDateTime == null) {
	// if (borderStartDateTime <= taskStartDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTask);
	// }
	// continue;
	// }
	// if (taskStartDateTime != null && taskEndDateTime == null
	// && borderStartDateTime == null && borderEndDateTime != null) {
	// continue;
	// }
	// if (taskStartDateTime == null && taskEndDateTime != null
	// && borderStartDateTime == null && borderEndDateTime != null) {
	// if (taskEndDateTime <= borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTask);
	// }
	// continue;
	// }
	// if (taskStartDateTime != null && taskEndDateTime != null
	// && borderStartDateTime == null && borderEndDateTime != null) {
	// if (taskEndDateTime <= borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTask);
	// }
	// continue;
	// }
	// if ((taskStartDateTime == null || taskEndDateTime == null)
	// && borderStartDateTime != null && borderEndDateTime != null) {
	// continue;
	// }
	// if (taskStartDateTime != null && taskEndDateTime != null
	// && borderStartDateTime != null && borderEndDateTime != null) {
	// // check if task goes into the given time interval
	// if (borderStartDateTime <= taskStartDateTime
	// && taskEndDateTime <= borderEndDateTime) {
	// selectedCalendarViewTaskOccurrences.add(calendarViewTask);
	// continue;
	// }
	// }
	// }
	// return selectedCalendarViewTaskOccurrences;
	// }
	public static void showAlertDialog(Context context, Exception e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		// 2. Chain together various setter methods to set the dialog
		// characteristics
		try {
			String exceptionMessage = e.getMessage();
			builder.setMessage(context
					.getString(R.string.an_error_occurred_while_saving_a_task)
					+ (exceptionMessage == null ? "" : "\n" + e.getMessage()));
		} catch (NullPointerException e2) {
			builder.setMessage("NullPointerException: " + e2.getMessage());
		}
		// Add the buttons
		builder.setPositiveButton(R.string.alert_dialog_ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						// User clicked OK button
						// dialog.dismiss();
					}
				});
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public static String getRingtoneTitle(Context context, String ringtoneString) {
		String silentRingtoneTitle = context.getResources().getString(
				R.string.ringtone_silent);
		if (ringtoneString.length() == 0) {
			return silentRingtoneTitle;
		} else {
			String defaultSilentPhoneRingtoneTitle = context.getResources().getString(
					R.string.default_phone_ringtone_with_actual, silentRingtoneTitle);
			String defaultSilentAlarmRingtoneTitle = context.getResources().getString(
					R.string.default_alarm_ringtone_with_actual, silentRingtoneTitle);
			String defaultSilentNotificationRingtoneTitle = context.getResources()
					.getString(R.string.default_notification_ringtone_with_actual,
							silentRingtoneTitle);
			String defaultAlarmUriString = Settings.System.DEFAULT_ALARM_ALERT_URI
					.toString();
			String defaultNotificationUriString = Settings.System.DEFAULT_NOTIFICATION_URI
					.toString();
			String defaultPhoneRingtoneUriString = Settings.System.DEFAULT_RINGTONE_URI
					.toString();
			Uri ringtoneUri = Uri.parse(ringtoneString);
			Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
			String ringtoneUriString = ringtoneUri.toString();
			if (ringtone == null) {
				// check is this the case when "Silence" is selected as ringtone sound
				// in system settings
				if (ringtoneUriString.equals(defaultAlarmUriString)) {
					return defaultSilentAlarmRingtoneTitle;
				}
				if (ringtoneUriString.equals(defaultNotificationUriString)) {
					return defaultSilentNotificationRingtoneTitle;
				}
				if (ringtoneUriString.equals(defaultPhoneRingtoneUriString)) {
					return defaultSilentPhoneRingtoneTitle;
				}
				return null;
			} else {
				// check is this the case when "Silence" is selected as ringtone sound
				// in system settings
				if (ringtoneUriString.equals(defaultAlarmUriString)
						&& RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_ALARM) == null) {
					// assume here this is the case when ringtoneString is pointing to the
					// Alarm ringtone and "Silence" is selected as ringtone sound in
					// system settings
					return defaultSilentAlarmRingtoneTitle;
				}
				if (ringtoneUriString.equals(defaultNotificationUriString)
						&& RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_NOTIFICATION) == null) {
					// assume here this is the case when ringtoneString is pointing to the
					// Notification ringtone and "Silence" is selected as ringtone sound
					// in system settings
					return defaultSilentNotificationRingtoneTitle;
				}
				if (ringtoneUriString.equals(defaultPhoneRingtoneUriString)
						&& RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_RINGTONE) == null) {
					// assume here this is the case when ringtoneString is pointing to the
					// Ringtone and "Silence" is selected as ringtone sound in system
					// settings
					return defaultSilentPhoneRingtoneTitle;
				}
				return ringtone.getTitle(context);
			}
		}
	}

	public static Boolean isRingtoneSilent(Context context, String ringtoneString) {
		if (ringtoneString.length() == 0) {
			return true;
		} else {
			String defaultAlarmUriString = Settings.System.DEFAULT_ALARM_ALERT_URI
					.toString();
			String defaultNotificationUriString = Settings.System.DEFAULT_NOTIFICATION_URI
					.toString();
			String defaultPhoneRingtoneUriString = Settings.System.DEFAULT_RINGTONE_URI
					.toString();
			Uri ringtoneUri = Uri.parse(ringtoneString);
			Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
			String ringtoneUriString = ringtoneUri.toString();
			if (ringtone == null) {
				// check is this the case when "Silence" is selected as ringtone sound
				// in system settings
				if (ringtoneUriString.equals(defaultAlarmUriString)
						|| ringtoneUriString.equals(defaultNotificationUriString)
						|| ringtoneUriString.equals(defaultPhoneRingtoneUriString)) {
					return true;
				}
				return null;
			} else {
				// check is this the case when "Silence" is selected as ringtone sound
				// in system settings
				if (ringtoneUriString.equals(defaultAlarmUriString)
						&& RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_ALARM) == null) {
					// assume here this is the case when ringtoneString is pointing to the
					// Alarm ringtone and "Silence" is selected as ringtone sound in
					// system settings
					return true;
				}
				if (ringtoneUriString.equals(defaultNotificationUriString)
						&& RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_NOTIFICATION) == null) {
					// assume here this is the case when ringtoneString is pointing to the
					// Notification ringtone and "Silence" is selected as ringtone sound
					// in system settings
					return true;
				}
				if (ringtoneUriString.equals(defaultPhoneRingtoneUriString)
						&& RingtoneManager.getActualDefaultRingtoneUri(context,
								RingtoneManager.TYPE_RINGTONE) == null) {
					// assume here this is the case when ringtoneString is pointing to the
					// Phone ringtone and "Silence" is selected as ringtone sound in
					// system
					// settings
					return true;
				}
				return false;
			}
		}
	}


	// public static long getBeginningOfWeek2(long startDateTime, int firstDayOfWeek) {
	// Calendar dt = Calendar.getInstance();
	// dt.setTimeInMillis(startDateTime);
	// dt.set(dt.get(Calendar.YEAR), dt.get(Calendar.MONTH),
	// dt.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
	// dt.set(Calendar.MILLISECOND, 0);
	// // move dt to the beginning of the week
	// dt.setFirstDayOfWeek(firstDayOfWeek);
	// dt.set(Calendar.DAY_OF_WEEK, 1);
	// // now dt has been moved to the beginning of the week
	// return dt.getTimeInMillis();
	// }
	public static boolean getContrast50(int color) {
		return (color & 0x00FFFFFF) > 0xFFFFFF / 2;
	}

	public static boolean getContrastYIQ(int color) {
		// color = color & 0x00FFFFFF;
		float yiq = ((color & 0x000000FF) * 114 + (color >> 8 & 0x000000FF) * 587 + (color >> 16 & 0x000000FF) * 299) / 1000.0f;
		return yiq >= 128;
	}

	public static void setLocalChangeDateTimeToCurrentTime(final Task newEntity,
			long timeSkew) {
		long nowTime = Calendar.getInstance().getTimeInMillis();
		newEntity.setLocalChangeDateTime(nowTime);
		newEntity.setLastMod(nowTime - timeSkew);
		newEntity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED.getValue());
	}

	public static void setLocalChangeDateTimeToCurrentTime(final Task newEntity,
			long nowTime, long timeSkew) {
		newEntity.setLocalChangeDateTime(nowTime);
		newEntity.setLastMod(nowTime - timeSkew);
		newEntity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED.getValue());
	}

	public static String getBooleanPreferenceSummary(Context context, boolean isChecked,
			int checkedBeingStringId, int uncheckedBeingStringId) {
		Resources resources = context.getResources();
		String text;
		if (isChecked) {
			text = resources.getString(checkedBeingStringId);
		} else {
			text = resources.getString(uncheckedBeingStringId);
		}
		return text;
	}

	public static String getIntegerPreferenceSummary(Context context, int intValue,
			int singularSummaryStringId, int pluralSummaryStringId) {
		Resources resources = context.getResources();
		String text;
		if (intValue == 1) {
			text = String.format(resources.getString(singularSummaryStringId), intValue);
		} else {
			text = String.format(resources.getString(pluralSummaryStringId), intValue);
		}
		return text;
	}

	public static String getIntegerPreferenceSummary(Context context, int intValue,
			int minValueId, int maxValueId, int singularStringId, int pluralStringId) {
		Resources resources = context.getResources();
		intValue = Helper.getIntegerFromIntegerValue(intValue,
				resources.getInteger(minValueId), resources.getInteger(maxValueId));
		String text;
		if (intValue == 1) {
			text = String.format(resources.getString(singularStringId), intValue);
		} else {
			text = String.format(resources.getString(pluralStringId), intValue);
		}
		return text;
	}

	public static String getIntegerPreferenceSummaryFromInteger(Context context,
			int intValue, int singularStringId, int pluralStringId) {
		Resources resources = context.getResources();
		String text;
		if (intValue == 1) {
			text = String.format(resources.getString(singularStringId), intValue);
		} else {
			text = String.format(resources.getString(pluralStringId), intValue);
		}
		return text;
	}

	public static String getIntegerPreferenceSummaryFromStringAndIntegerBounds(
			Context context, String stringValue, int defaultValue, int minValue,
			int maxValue, int singularStringId, int pluralStringId) {
		Resources resources = context.getResources();
		int intValue = Helper.getIntegerFromStringValue(stringValue, defaultValue,
				minValue, maxValue);
		String text;
		if (intValue == 1) {
			text = String.format(resources.getString(singularStringId), intValue);
		} else {
			text = String.format(resources.getString(pluralStringId), intValue);
		}
		return text;
	}

	public static String getIntegerPreferenceSummaryFromStringAndResourceBounds(
			Context context, String stringValue, int defaultValueId, int minValueId,
			int maxValueId, int singularStringId, int pluralStringId) {
		Resources resources = context.getResources();
		int intValue = Helper.getIntegerFromStringValue(stringValue,
				resources.getInteger(defaultValueId), resources.getInteger(minValueId),
				resources.getInteger(maxValueId));
		String text;
		if (intValue == 1) {
			text = String.format(resources.getString(singularStringId), intValue);
		} else {
			text = String.format(resources.getString(pluralStringId), intValue);
		}
		return text;
	}

	public static String getRingtoneTitleFromPreference(Context context, int preferenceId) {
		String stringValue = Helper
				.getStringPreferenceValue(
						context,
						preferenceId,
						RingtoneManager
								.getDefaultUri(
										preferenceId == R.string.preference_key_alarm_ringtone ? RingtoneManager.TYPE_ALARM
												: RingtoneManager.TYPE_NOTIFICATION)
								.toString());
		// String stringValue3 = Helper
		// .getStringPreferenceValue(
		// context,
		// preferenceId,
		// (preferenceId == R.string.preference_key_alarm_ringtone ?
		// Settings.System.DEFAULT_ALARM_ALERT_URI
		// : Settings.System.DEFAULT_NOTIFICATION_URI).toString());
		String title = Helper.getRingtoneTitle(context, stringValue);
		return title;
	}

	public static Intent initializeIntentForRingtonePicker(Context context,
			String ringtone, int preferenceId, int soundType, Uri defaultUri,
			int requestCodePickRingtone) {
		Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
		// intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Tone");
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, soundType);
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, defaultUri);
		Uri ringtoneUri = null;
		if (ringtone == null) {
			String stringValue = Helper
					.getStringPreferenceValue(
							context,
							preferenceId,
							RingtoneManager
									.getDefaultUri(
											preferenceId == R.string.preference_key_alarm_ringtone ? RingtoneManager.TYPE_ALARM
													: RingtoneManager.TYPE_NOTIFICATION)
									.toString());
			ringtoneUri = Uri.parse(stringValue);
		} else if (ringtone.length() != 0) {
			ringtoneUri = Uri.parse(ringtone);
		}
		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);
		// activity.startActivityForResult(intent, requestCodePickRingtone);
		return intent;
	}

	public static void showWantingItemSetMessages(Context context,
			Set<WantingItem> wantingItemSet, String title) {
		StringBuilder message;
		if (title != null) {
			message = new StringBuilder(title);
			message.append(String.format("%n"));
		} else {
			message = new StringBuilder();
		}
		Iterator<WantingItem> iterator = wantingItemSet.iterator();
		if (iterator.hasNext()) {
			WantingItem wantingItem = iterator.next();
			message.append(wantingItem.getText());
		}
		while (iterator.hasNext()) {
			WantingItem wantingItem = iterator.next();
			message.append(String.format("%n"));
			message.append(wantingItem.getText());
		}
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public static void setLastTimeDifference(Context context, long millis) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
				context).edit();
		editor.putLong("BOOT_TIME", millis);
		editor.commit();
	}

	public static long getLastTimeDifference(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getLong(
				"BOOT_TIME", -1);
	}

	public static String getTextForTimeInterval(Context context, long timeIntervalMillis) {
		int days = (int) (timeIntervalMillis / (1000 * 60 * 60 * 24));
		int timeIntervalHoursRemainder = (int) (timeIntervalMillis / (1000 * 60 * 60) % 24);
		int timeIntervalMinutesRemainder = (int) (timeIntervalMillis / (1000 * 60) % 60);
		int timeIntervalSecondsRemainder = (int) (timeIntervalMillis / 1000 % 60);
		String textForTimeInterval = Helper.getTextForTimeInterval(context, days,
				timeIntervalHoursRemainder, timeIntervalMinutesRemainder,
				timeIntervalSecondsRemainder);
		return textForTimeInterval;
	}

	public static String getTextForTimeInterval(Context context, long days,
			long timeIntervalHoursRemainder, long timeIntervalMinutesRemainder,
			long timeIntervalSecondsRemainder) {
		final Resources resources = context.getResources();
		String textTime;
		StringBuilder stringBuilder = new StringBuilder();
		String delimiter = ", ";
		int textIdTimeUnitsCount;
		if (days != 0) {
			if (days == 1) {
				textIdTimeUnitsCount = R.string.text_days_count_singular;
			} else {
				textIdTimeUnitsCount = R.string.text_days_count_plural;
			}
			textTime = String.format(resources.getString(textIdTimeUnitsCount), days);
			stringBuilder.append(textTime);
		}
		if (timeIntervalHoursRemainder != 0) {
			if (timeIntervalHoursRemainder == 1) {
				textIdTimeUnitsCount = R.string.text_hours_count_singular;
			} else {
				textIdTimeUnitsCount = R.string.text_hours_count_plural;
			}
			textTime = String.format(resources.getString(textIdTimeUnitsCount),
					timeIntervalHoursRemainder);
			stringBuilder.append(delimiter);
			stringBuilder.append(textTime);
		}
		if (timeIntervalMinutesRemainder != 0) {
			if (timeIntervalMinutesRemainder == 1) {
				textIdTimeUnitsCount = R.string.text_minutes_count_singular;
			} else {
				textIdTimeUnitsCount = R.string.text_minutes_count_plural;
			}
			textTime = String.format(resources.getString(textIdTimeUnitsCount),
					timeIntervalMinutesRemainder);
			stringBuilder.append(delimiter);
			stringBuilder.append(textTime);
		}
		if (timeIntervalSecondsRemainder != 0) {
			if (timeIntervalSecondsRemainder == 1) {
				textIdTimeUnitsCount = R.string.text_seconds_count_singular;
			} else {
				textIdTimeUnitsCount = R.string.text_seconds_count_plural;
			}
			textTime = String.format(resources.getString(textIdTimeUnitsCount),
					timeIntervalSecondsRemainder);
			stringBuilder.append(delimiter);
			stringBuilder.append(textTime);
		}
		int delimiterLength = delimiter.length();
		if (stringBuilder.length() >= delimiterLength) {
			if (stringBuilder.substring(0, delimiterLength).equals(delimiter)) {
				stringBuilder.delete(0, delimiterLength);
			}
		}
		textTime = stringBuilder.toString();
		return textTime;
	}

	public static boolean isTakingWholeInterval(long startDateTime, long endDateTime,
			long borderStartDateTime, long borderEndDateTime) {
		if (startDateTime <= borderStartDateTime && borderEndDateTime <= endDateTime) {
			return true;
		}
		return false;
	}

	public static boolean isTakingWholeInterval(Long startDateTime, Long endDateTime,
			long borderStartDateTime, long borderEndDateTime) {
		if (startDateTime == null
				&& endDateTime == null
				//
				|| startDateTime != null
				&& endDateTime == null
				&& startDateTime <= borderStartDateTime
				//
				|| startDateTime == null
				&& endDateTime != null
				&& borderEndDateTime <= endDateTime
				//
				|| startDateTime != null
				&& endDateTime != null
				&& Helper.isTakingWholeInterval(startDateTime.longValue(),
						endDateTime.longValue(), borderStartDateTime, borderEndDateTime)) {
			return true;
		}
		return false;
	}

	public static boolean isTakingNotWholeInterval(long startDateTime, long endDateTime,
			long borderStartDateTime, long borderEndDateTime) {
		if (startDateTime == endDateTime
				&& borderStartDateTime <= startDateTime
				&& startDateTime < borderEndDateTime
				//
				|| startDateTime != endDateTime
				//
				&& (startDateTime <= borderStartDateTime
						&& borderStartDateTime < endDateTime
						&& endDateTime < borderEndDateTime
						//
						|| borderStartDateTime <= startDateTime
						&& endDateTime < borderEndDateTime
						//
						|| borderStartDateTime < startDateTime
						&& endDateTime <= borderEndDateTime
				//
				|| borderStartDateTime < startDateTime
						&& startDateTime < borderEndDateTime
						&& borderEndDateTime <= endDateTime)) {
			return true;
		}
		return false;
	}

	public static boolean isTakingNotWholeInterval(Long startDateTime, Long endDateTime,
			long borderStartDateTime, long borderEndDateTime) {
		if (startDateTime != null
				&& endDateTime == null
				&& borderStartDateTime < startDateTime
				&& startDateTime < borderEndDateTime
				//
				|| startDateTime == null && endDateTime != null
				&& borderStartDateTime < endDateTime
				&& endDateTime < borderEndDateTime
				//
				|| startDateTime != null
				&& endDateTime != null
				&& Helper.isTakingNotWholeInterval(startDateTime.longValue(),
						endDateTime.longValue(), borderStartDateTime, borderEndDateTime)) {
			return true;
		}
		return false;
	}

	public static boolean isTouchingInterval(Long startDateTime, Long endDateTime,
			long borderStartDateTime, long borderEndDateTime) {
		return Helper.isTakingWholeInterval(startDateTime, endDateTime,
				borderStartDateTime, borderEndDateTime)
				|| Helper.isTakingNotWholeInterval(startDateTime, endDateTime,
						borderStartDateTime, borderEndDateTime);
	}

	public static void copy(File src, File dst) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			// Transfer bytes from in to out
			byte[] buf = new byte[1024 * 4];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}
}
