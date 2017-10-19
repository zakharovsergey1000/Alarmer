package biz.advancedcalendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ViewConfiguration;
import biz.advancedcalendar.activities.ActivityAlarm;
import biz.advancedcalendar.activities.ActivityAlarm.AutomaticSnoozeListener;
import biz.advancedcalendar.activities.ActivityAlarm.AutomaticSnoozer;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.DaoMaster;
import biz.advancedcalendar.greendao.DaoMaster.DevOpenHelper;
import biz.advancedcalendar.greendao.DaoSession;
import biz.advancedcalendar.greendao.DiaryRecord;
import biz.advancedcalendar.greendao.File;
import biz.advancedcalendar.greendao.Label;
import biz.advancedcalendar.services.NotificationService;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import org.acra.*;
import org.acra.annotation.*;


@ReportsCrashes(
mailTo = "advancedcalendar0@gmail.com", mode = ReportingInteractionMode.DIALOG,
// optional, displayed as soon as the crash occurs, before collecting data which
// can take a few seconds
resToastText = R.string.crash_toast_text, resDialogText = R.string.crash_dialog_text,
// optional. default is a warning sign
resDialogIcon = android.R.drawable.ic_dialog_info,
// optional. default is your application name
resDialogTitle = R.string.crash_dialog_title,
// optional. when defined, adds a user text field input with this text resource
// as a label
resDialogCommentPrompt = R.string.crash_dialog_comment_prompt,
// optional. displays a Toast message when the user accepts to send a report.
resDialogOkToast = R.string.crash_dialog_ok_toast)
// @ReportsCrashes(formKey = "", // will not be used
// formUri = "https://advancedcalendar.biz/acra", formUriBasicAuthLogin = "yourlogin", //
// optional
// formUriBasicAuthPassword = "y0uRpa$$w0rd", // optional
// httpMethod = org.acra.sender.HttpSender.Method.POST, mode =
// ReportingInteractionMode.SILENT)
public class Global extends Application implements AutomaticSnoozer {
	private static final boolean NO_MENU_BUTTON_DEBUG = false;
	private static final String NO_MENU_BUTTON_DEBUG_TAG = "NO_MENU_BUTTON_DT";

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
	}
	@Override
	public void onCreate() {
		super.onCreate();

		// make android to think there is no menu button
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
				if (Global.NO_MENU_BUTTON_DEBUG) {
					Log.d(Global.NO_MENU_BUTTON_DEBUG_TAG,
							"Android is made to think there is no menu button");
				}
			}
		} catch (Exception ex) {
			// Ignore
			if (Global.NO_MENU_BUTTON_DEBUG) {
				Log.d(Global.NO_MENU_BUTTON_DEBUG_TAG, ex.getMessage());
			}
		}
		// update notifications
		NotificationService.updateNotification(this);
	}

	// Task to edit
	// private static Task taskToEdit = null;
	// private static Long taskToEditStartDate;
	// private static Long taskToEditEndDate;
	// private static Long taskToEditStartTime;
	// private static Long taskToEditEndTime;
	// private static Long taskToEditRepeatEndDate;
	// private static Long taskToEditRepeatEndTime;
	// // private static Long taskToEditSortOrderPlaceAfter;
	// private static List<Long> taskToEditLabelIdList;
	// private static List<Long> taskToEditContactIdList;
	// private static List<Long> taskToEditFileIdList;
	// private static List<Reminder> taskToEditReminderList;
	// private static String taskToEditTimezoneStringId;
	// Label to edit
	private static Label labelToEdit = null;
	private static List<Long> labelToEditTaskIdList;
	private static List<Long> labelToEditContactIdList;
	private static List<Long> labelToEditFileIdList;
	// File to edit
	private static File fileToEdit;
	private static List<Long> fileToEditLabelIdList;
	private static List<Long> fileToEditContactIdList;
	private static List<Long> fileToEditTaskIdList;
	// DiaryRecord to edit
	private static DiaryRecord diaryRecordToEdit;
	// Reminder to edit
	// private static Reminder reminderToEdit;
	private DevOpenHelper helper;
	private SQLiteDatabase db;
	private DaoMaster daoMaster;
	private DaoSession daoSession = null;
	private AutomaticSnoozeListener mAutomaticSnoozeListener;
	protected HashSet<Long> mSnoozedIds;
	private HashMap<Long, Runnable> mPostedRunnables;
	private Handler mTimerHandler;

	public DaoSession getDaoSession() {
		if (daoSession == null) {
			helper = new DaoMaster.DevOpenHelper(this, "AdvancedCalendar.db", null);
			db = helper.getWritableDatabase();
			daoMaster = new DaoMaster(db);
			daoSession = daoMaster.newSession();
		}
		return daoSession;
	}

	public void updateDaoSession() {
		if (db != null) {
			db.close();
		}
		helper = new DaoMaster.DevOpenHelper(this, "AdvancedCalendar.db", null);
		db = helper.getWritableDatabase();
		daoMaster = new DaoMaster(db);
		daoSession = daoMaster.newSession();
	}

	// public static Task getTaskToEdit() {
	// return taskToEdit;
	// }
	//
	// public static void setTaskToEdit(Task taskToEdit) {
	// Global.taskToEdit = taskToEdit;
	// }
	//
	// public static Long getTaskToEditStartDate() {
	// return taskToEditStartDate;
	// }
	//
	// public static void setTaskToEditStartDate(Long taskToEditStartDate) {
	// Global.taskToEditStartDate = taskToEditStartDate;
	// }
	//
	// public static Long getTaskToEditEndDate() {
	// return taskToEditEndDate;
	// }
	//
	// public static void setTaskToEditEndDate(Long taskToEditEndDate) {
	// Global.taskToEditEndDate = taskToEditEndDate;
	// }
	//
	// public static Long getTaskToEditStartTime() {
	// return taskToEditStartTime;
	// }
	//
	// public static void setTaskToEditStartTime(Long taskToEditStartTime) {
	// Global.taskToEditStartTime = taskToEditStartTime;
	// }
	//
	// public static Long getTaskToEditEndTime() {
	// return taskToEditEndTime;
	// }
	//
	// public static void setTaskToEditEndTime(Long taskToEditEndTime) {
	// Global.taskToEditEndTime = taskToEditEndTime;
	// }
	//
	// public static Long getTaskToEditRepeatEndDate() {
	// return taskToEditRepeatEndDate;
	// }
	//
	// public static void setTaskToEditRepeatEndDate(Long taskToEditRepeatEndDate) {
	// Global.taskToEditRepeatEndDate = taskToEditRepeatEndDate;
	// }
	//
	// public static Long getTaskToEditRepeatEndTime() {
	// return taskToEditRepeatEndTime;
	// }
	//
	// public static void setTaskToEditRepeatEndTime(Long taskToEditRepeatEndTime) {
	// Global.taskToEditRepeatEndTime = taskToEditRepeatEndTime;
	// }
	//
	// public static Long getTaskToEditSortOrderPlaceAfter() {
	// return taskToEditSortOrderPlaceAfter;
	// }
	//
	// public static void setTaskToEditSortOrderPlaceAfter(
	// Long taskToEditSortOrderPlaceAfter) {
	// Global.taskToEditSortOrderPlaceAfter = taskToEditSortOrderPlaceAfter;
	// }
	//
	// public static List<Long> getTaskToEditLabelIdList() {
	// return taskToEditLabelIdList;
	// }
	//
	// public static void setTaskToEditLabelIdList(
	// List<Long> taskToEditLabelIdList) {
	// Global.taskToEditLabelIdList = taskToEditLabelIdList;
	// }
	//
	// public static List<Long> getTaskToEditContactIdList() {
	// return taskToEditContactIdList;
	// }
	//
	// public static void setTaskToEditContactIdList(
	// List<Long> taskToEditContactIdList) {
	// Global.taskToEditContactIdList = taskToEditContactIdList;
	// }
	//
	// public static List<Long> getTaskToEditFileIdList() {
	// return taskToEditFileIdList;
	// }
	//
	// public static void setTaskToEditFileIdList(List<Long>
	// taskToEditFileIdList) {
	// Global.taskToEditFileIdList = taskToEditFileIdList;
	// }
	//
	// public static List<Reminder> getTaskToEditReminderList() {
	// return taskToEditReminderList;
	// }
	//
	// public static void setTaskToEditReminderList(
	// List<Reminder> taskToEditReminderList) {
	// Global.taskToEditReminderList = taskToEditReminderList;
	// }
	//
	// public static String getTaskToEditTimezoneStringId() {
	// return taskToEditTimezoneStringId;
	// }
	//
	// public static void setTaskToEditTimezoneStringId(
	// String taskToEditTimezoneStringId) {
	// Global.taskToEditTimezoneStringId = taskToEditTimezoneStringId;
	// }
	// labels
	public static Label getLabelToEdit() {
		return Global.labelToEdit;
	}

	public static void setLabelToEdit(Label labelToEdit) {
		Global.labelToEdit = labelToEdit;
	}

	public static void setLabelToEditTaskIdList(List<Long> labelToEditTaskIdList) {
		Global.labelToEditTaskIdList = labelToEditTaskIdList;
	}

	public static void setLabelToEditContactIdList(List<Long> labelToEditContactIdList) {
		Global.labelToEditContactIdList = labelToEditContactIdList;
	}

	public static void setLabelToEditFileIdList(List<Long> labelToEditFileIdList) {
		Global.labelToEditFileIdList = labelToEditFileIdList;
	}

	public static List<Long> getLabelToEditTaskIdList() {
		return Global.labelToEditTaskIdList;
	}

	public static List<Long> getLabelToEditContactIdList() {
		return Global.labelToEditContactIdList;
	}

	public static List<Long> getLabelToEditFileIdList() {
		return Global.labelToEditFileIdList;
	}

	// public static Contact getContactToEdit() {
	// return contactToEdit;
	// }
	//
	// public static void setContactToEdit(Contact contactToEdit) {
	// Global.contactToEdit = contactToEdit;
	// }
	// public static List<Long> getContactToEditTaskIdList() {
	// return contactToEditTaskIdList;
	// }
	//
	// public static void setContactToEditTaskIdList(
	// List<Long> contactToEditTaskIdList) {
	// Global.contactToEditTaskIdList = contactToEditTaskIdList;
	// }
	//
	// public static List<Long> getContactToEditLabelIdList() {
	// return contactToEditLabelIdList;
	// }
	//
	// public static void setContactToEditLabelIdList(
	// List<Long> contactToEditLabelIdList) {
	// Global.contactToEditLabelIdList = contactToEditLabelIdList;
	// }
	//
	// public static List<ContactData> getContactToEditContactDataList() {
	// return contactToEditContactDataList;
	// }
	//
	// public static void setContactToEditContactDataList(
	// List<ContactData> contactToEditContactDataList) {
	// Global.contactToEditContactDataList = contactToEditContactDataList;
	// }
	public static File getFileToEdit() {
		return Global.fileToEdit;
	}

	public static void setFileToEdit(File fileToEdit) {
		Global.fileToEdit = fileToEdit;
	}

	public static List<Long> getFileToEditTaskIdList() {
		return Global.fileToEditTaskIdList;
	}

	public static void setFileToEditTaskIdList(List<Long> fileToEditTaskIdList) {
		Global.fileToEditTaskIdList = fileToEditTaskIdList;
	}

	public static List<Long> getFileToEditLabelIdList() {
		return Global.fileToEditLabelIdList;
	}

	public static void setFileToEditLabelIdList(List<Long> fileToEditLabelIdList) {
		Global.fileToEditLabelIdList = fileToEditLabelIdList;
	}

	public static List<Long> getFileToEditContactIdList() {
		return Global.fileToEditContactIdList;
	}

	public static void setFileToEditContactIdList(List<Long> fileToEditContactIdList) {
		Global.fileToEditContactIdList = fileToEditContactIdList;
	}

	public static DiaryRecord getDiaryRecordToEdit() {
		return Global.diaryRecordToEdit;
	}

	public static void setDiaryRecordToEdit(DiaryRecord diaryRecordToEdit) {
		Global.diaryRecordToEdit = diaryRecordToEdit;
	}

	// public static Reminder getReminderToEdit() {
	// return reminderToEdit;
	// }
	//
	// public static void setReminderToEdit(Reminder reminderToEdit) {
	// Global.reminderToEdit = reminderToEdit;
	// }
	public void setupAlarmManager() {
		AlarmManager am = (AlarmManager) Global.this
				.getSystemService(Context.ALARM_SERVICE);
		PendingIntent operation = PendingIntent.getActivity(getApplicationContext(), 0,
				null, 0);
		am.set(AlarmManager.RTC_WAKEUP, 0L, operation);
	}

	// public static void setupTaskToEdit(Context context, Long id) {
	//
	// if (Global.getTaskToEdit() == null) {
	//
	// Task task;
	//
	// if (id != null) {
	// task = DataProvider.getTask(context, id, false);
	//
	// Global.setTaskToEdit(task);
	//
	// Global.setTaskToEditStartDate(task.getStartDateTimeUtc0());
	// Global.setTaskToEditStartTime(task.getStartDateTimeUtc0());
	//
	// Global.setTaskToEditEndDate(task.getEndDateTimeUtc0());
	// Global.setTaskToEditEndTime(task.getEndDateTimeUtc0());
	//
	// Global.setTaskToEditRepeatEndDate(task
	// .getRepeatEndDateTimeUtc0());
	// Global.setTaskToEditRepeatEndTime(task
	// .getRepeatEndDateTimeUtc0());
	//
	// Global.setTaskToEditLabelIdList(DataProvider
	// .getLabelIdListForTask(context, id, false, true,
	// false));
	// Global.setTaskToEditContactIdList(DataProvider
	// .getContactIdListForTask(context, id, false, true,
	// false));
	// Global.setTaskToEditFileIdList(DataProvider
	// .getFileIdListForTask(context, id, false, true, false));
	// Global.setTaskToEditReminderList(DataProvider
	// .getReminderListForTask(context, id, true, true, false,
	// true, true, true));
	//
	// } else {
	// task = new Task();
	// task.setUserName(DataProvider.getSignedInUser(context)
	// .getUserName());
	// task.setType(TYPE.SIMPLE.getValue());
	// task.setPriority(PRIORITY.MEDIUM.getValue());
	// task.setTimezoneOffset((short) (TimeZone.getDefault()
	// .getOffset(Calendar.getInstance().getTimeInMillis()) / 1000 / 60));
	//
	// Global.setTaskToEdit(task);
	//
	// Global.setTaskToEditStartDate(null);
	// Global.setTaskToEditStartTime(null);
	//
	// Global.setTaskToEditEndDate(null);
	// Global.setTaskToEditEndTime(null);
	//
	// Global.setTaskToEditRepeatEndDate(null);
	// Global.setTaskToEditRepeatEndTime(null);
	//
	// Global.setTaskToEditLabelIdList(null);
	// Global.setTaskToEditContactIdList(null);
	// Global.setTaskToEditReminderList(new ArrayList<Reminder>());
	//
	// }
	// }
	//
	// }
	public static void setupLabelToEdit(Context context, Long id) {
		if (Global.getLabelToEdit() == null) {
			Label entity;
			if (id != null) {
				entity = DataProvider.getLabel(null, context, id, false);
				Global.setLabelToEdit(entity);
				Global.setLabelToEditContactIdList(DataProvider.getContactIdListForLabel(
						null, context, id, false, true, false));
				Global.setLabelToEditFileIdList(DataProvider.getFileIdListForLabel(
						null, context, id, false, true, false));
				Global.setLabelToEditTaskIdList(DataProvider.getTaskIdListForLabel(
						null, context, id, false, true, false, true, false));
			} else {
				entity = new Label();
				// entity.setUserName(DataProvider.getSignedInUser(context).getUserName());
				Global.setLabelToEdit(entity);
				Global.setLabelToEditContactIdList(null);
				Global.setLabelToEditTaskIdList(null);
			}
		}
	}

	public static void setupDiaryRecordToEdit(Context context, Long id) {
		if (Global.getDiaryRecordToEdit() == null) {
			DiaryRecord entity;
			if (id != null) {
				entity = DataProvider.getDiaryRecord(null, context, id, false);
				Global.setDiaryRecordToEdit(entity);
			} else {
				entity = new DiaryRecord();
				Global.setDiaryRecordToEdit(entity);
			}
		}
	}

	@Override
	public void setListener(AutomaticSnoozeListener listener) {
		// if (listener == null) {
		// mSnoozedIds = null;
		// mPostedRunnables = null;
		// }
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("setListener(%s)", "" + listener));
		}
		mAutomaticSnoozeListener = listener;
	}

	@Override
	public Long[] pullSnoozedAlarms() {
		Long[] result;
		if (mSnoozedIds == null) {
			result = null;
		} else {
			Long[] array = new Long[mSnoozedIds.size()];
			mSnoozedIds.toArray(array);
			mSnoozedIds = null;
			result = array;
		}
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, "pullSnoozedAlarms(): " + result);
		}
		return result;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	public void postDelayed(final long id, long delayMillis) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("postDelayed(long id: %s, long delayMillis: %s)", ""
							+ id, "" + delayMillis));
		}
		if (mTimerHandler == null) {
			mTimerHandler = new Handler();
		}
		if (mPostedRunnables == null) {
			mPostedRunnables = new HashMap<Long, Runnable>();
		}
		Runnable r = mPostedRunnables.remove(id);
		if (r != null) {
			if (ActivityAlarm.DEBUG) {
				Log.d(ActivityAlarm.ActivityAlarmDebug,
						"mTimerHandler.removeCallbacks(mPostedRunnables.remove(id): %s)"
								+ r);
			}
			mTimerHandler.removeCallbacks(r);
		}
		r = new Runnable() {
			@Override
			public void run() {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug,
							String.format("run(), id: %s", "" + id));
				}
				ActivityAlarm.snoozeReminder(Global.this, id, null, true);
				biz.advancedcalendar.services.NotificationService
						.updateNotification(Global.this);
				LocalBroadcastManager.getInstance(Global.this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
				if (mAutomaticSnoozeListener != null) {
					mAutomaticSnoozeListener.onAutomaticSnooze(id);
				} else {
					if (mSnoozedIds == null) {
						mSnoozedIds = new HashSet<Long>();
					}
					mSnoozedIds.add(id);
				}
			}
		};
		mPostedRunnables.put(id, r);
		mTimerHandler.postDelayed(r, delayMillis);
	}

	@Override
	public void cancel(Long id) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("cancel(Long id: %s)", "" + id));
		}
		if (mPostedRunnables != null && mTimerHandler != null) {
			if (id != null) {
				Runnable runnable = mPostedRunnables.remove(id);
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug,
							"mTimerHandler.removeCallbacks(mPostedRunnables.remove(id): %s)"
									+ runnable);
				}
				mTimerHandler.removeCallbacks(runnable);
			} else {
				Iterator<Entry<Long, Runnable>> iterator = mPostedRunnables.entrySet()
						.iterator();
				while (iterator.hasNext()) {
					Entry<Long, Runnable> entry = iterator.next();
					iterator.remove();
					mTimerHandler.removeCallbacks(entry.getValue());
				}
			}
		}
	}
}
