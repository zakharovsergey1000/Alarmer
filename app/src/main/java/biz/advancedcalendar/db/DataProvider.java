package biz.advancedcalendar.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import biz.advancedcalendar.BooleanHolder;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.activityedittask.TaskUiData2;
import biz.advancedcalendar.activities.activityedittask.TaskWithDependentsUiData3;
import biz.advancedcalendar.fragments.Node;
import biz.advancedcalendar.greendao.Calendar;
import biz.advancedcalendar.greendao.CalendarDao;
import biz.advancedcalendar.greendao.Contact;
import biz.advancedcalendar.greendao.ContactDao;
import biz.advancedcalendar.greendao.ContactData;
import biz.advancedcalendar.greendao.ContactDataDao;
import biz.advancedcalendar.greendao.DaoMaster;
import biz.advancedcalendar.greendao.DaoMaster2;
import biz.advancedcalendar.greendao.DaoMaster2.DevOpenHelper2;
import biz.advancedcalendar.greendao.DaoSession;
import biz.advancedcalendar.greendao.DiaryRecord;
import biz.advancedcalendar.greendao.DiaryRecordDao;
import biz.advancedcalendar.greendao.DiaryRecordLabel;
import biz.advancedcalendar.greendao.DiaryRecordLabelDao;
import biz.advancedcalendar.greendao.DowngradeException;
import biz.advancedcalendar.greendao.ElapsedReminder;
import biz.advancedcalendar.greendao.ElapsedReminderDao;
import biz.advancedcalendar.greendao.FileContact;
import biz.advancedcalendar.greendao.FileContactDao;
import biz.advancedcalendar.greendao.FileDao;
import biz.advancedcalendar.greendao.FileLabel;
import biz.advancedcalendar.greendao.FileLabelDao;
import biz.advancedcalendar.greendao.FileTask;
import biz.advancedcalendar.greendao.FileTaskDao;
import biz.advancedcalendar.greendao.Label;
import biz.advancedcalendar.greendao.LabelContact;
import biz.advancedcalendar.greendao.LabelContactDao;
import biz.advancedcalendar.greendao.LabelDao;
import biz.advancedcalendar.greendao.Message;
import biz.advancedcalendar.greendao.MessageDao;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.ReminderDao;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.ScheduledReminderDao;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.greendao.TaskContact;
import biz.advancedcalendar.greendao.TaskContactDao;
import biz.advancedcalendar.greendao.TaskDao;
import biz.advancedcalendar.greendao.TaskLabel;
import biz.advancedcalendar.greendao.TaskLabelDao;
import biz.advancedcalendar.greendao.TaskOccurrence;
import biz.advancedcalendar.greendao.TaskOccurrenceDao;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.greendao.UserProfileDao;
import biz.advancedcalendar.greendao.WorkGroup;
import biz.advancedcalendar.greendao.WorkGroupDao;
import biz.advancedcalendar.greendao.WorkGroupMember;
import biz.advancedcalendar.greendao.WorkGroupMemberDao;
import biz.advancedcalendar.importtask.TaskUiData3;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.sync.TaskDtoWithDependents;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.CalendarViewTaskOccurrence;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescriptionTaskImpl;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescriptionTaskImpl.TreeViewListItemDescriptionMatrix;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescriptionTaskImpl.TreeViewListItemDescriptionRow;
import biz.advancedcalendar.wsdl.sync.Record;
import biz.advancedcalendar.wsdl.sync.ReminderDto;
import biz.advancedcalendar.wsdl.sync.TaskDto;
import biz.advancedcalendar.wsdl.sync.TaskOccurrenceDto;
import de.greenrobot.dao.query.LazyList;
import de.greenrobot.dao.query.Query;
import de.greenrobot.dao.query.QueryBuilder;
import de.greenrobot.dao.query.WhereCondition;
import de.greenrobot.dao.query.WhereCondition.StringCondition;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class DataProvider {
	private DataProvider() {
		super();
	}

	public static synchronized boolean isSignedIn(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		UserProfileDao dao = daoSession.getUserProfileDao();
		QueryBuilder<UserProfile> qb2 = dao.queryBuilder();
		UserProfile entity = qb2.unique();
		return entity != null && entity.getAuthToken() != null;
	}

	public static synchronized void signOut(DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		UserProfileDao dao = daoSession.getUserProfileDao();
		QueryBuilder<UserProfile> qb2 = dao.queryBuilder();
		UserProfile entity = qb2.unique();
		if (entity != null) {
			entity.setAuthToken(null);
		}
	}

	public static synchronized void setSignedInUser(DaoSession daoSessionArg,
			Context context, String userName, String authToken) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		UserProfileDao dao = daoSession.getUserProfileDao();
		QueryBuilder<UserProfile> qb2 = dao.queryBuilder();
		UserProfile entity = qb2.unique();
		if (entity == null) {
			entity = new UserProfile();
		}
		entity.setEmail(userName);
		entity.setAuthToken(authToken);
		dao.insertOrReplace(entity);
	}

	public static synchronized UserProfile getUserProfile(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		UserProfileDao dao = daoSession.getUserProfileDao();
		QueryBuilder<UserProfile> qb2 = dao.queryBuilder();
		UserProfile entity = qb2.unique();
		return entity;
	}

	public static Task getTask(DaoSession daoSessionArg, Context context, Long id,
			boolean isServerId) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		QueryBuilder<Task> qb2 = dao.queryBuilder();
		if (!isServerId) {
			qb2.where(TaskDao.Properties.Id.eq(id));
		} else {
			qb2.where(TaskDao.Properties.ServerId.eq(id));
		}
		Task entity = qb2.unique();
		return entity;
	}

	public static List<Task> getActiveNotDeletedTasks(DaoSession daoSessionArg,
			Context context, Long[] from) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		QueryBuilder<Task> qb2 = dao.queryBuilder();
		if (from != null) {
			boolean includeDefault = false;
			List<Long> ids = new ArrayList<Long>(from.length);
			for (Long id : from) {
				if (id != null) {
					ids.add(id);
				} else {
					includeDefault = true;
				}
			}
			WhereCondition where = TaskDao.Properties.CalendarId.in(ids);
			if (includeDefault) {
				where = qb2.or(TaskDao.Properties.CalendarId.isNull(), where);
			}
			qb2.where(where, TaskDao.Properties.IsCompleted.eq(false),
					TaskDao.Properties.Deleted.eq(0));
		} else {
			qb2.where(TaskDao.Properties.IsCompleted.eq(false),
					TaskDao.Properties.Deleted.eq(0));
		}
		return qb2.listLazy();
	}

	public static Label getLabel(DaoSession daoSessionArg, Context context, Long id,
			boolean isServerId) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		LabelDao dao = daoSession.getLabelDao();
		QueryBuilder<Label> qb2 = dao.queryBuilder();
		if (!isServerId) {
			qb2.where(LabelDao.Properties.Id.eq(id));
		} else {
			qb2.where(LabelDao.Properties.ServerId.eq(id));
		}
		Label entity = qb2.unique();
		return entity;
	}

	public static biz.advancedcalendar.greendao.Contact getContact(
			DaoSession daoSessionArg, Context context, Long id, boolean isServerId) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ContactDao dao = daoSession.getContactDao();
		QueryBuilder<Contact> qb = dao.queryBuilder();
		if (!isServerId) {
			qb.where(ContactDao.Properties.Id.eq(id));
		} else {
			qb.where(ContactDao.Properties.ServerId.eq(id));
		}
		Contact entity = qb.unique();
		return entity;
	}

	public static List<Contact> getContactList(DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		return daoSession.getContactDao().queryBuilder()
				.where(ContactDao.Properties.Deleted.eq(0))
				.orderAsc(ContactDao.Properties.SortOrder).list();
	}

	public static ArrayList<Long> getLabelIdListForTask(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getTaskId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ TaskLabelDao.Properties.TaskId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<TaskLabel> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getTaskLabelDao()
						.queryRawCreate(
								"JOIN " + LabelDao.TABLENAME + " T1 ON T."
										+ TaskLabelDao.Properties.TaskId.columnName
										+ " = T1." + TaskDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (TaskLabel entity : list) {
					selectedIds.add(entity.getLabelId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getLabelIdListForFile(DaoSession daoSessionArg,
			final Context context, final Long id1, final Integer versionId,
			final boolean isServerId, final boolean includeNonDeleted,
			final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getFileId(daoSession, context, id1, versionId);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ FileLabelDao.Properties.FileId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<FileLabel> list2 = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getFileLabelDao()
						.queryRawCreate(
								"JOIN " + LabelDao.TABLENAME + " T1 ON T."
										+ FileLabelDao.Properties.FileId.columnName
										+ " = T1." + FileDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (FileLabel entity : list2) {
					selectedIds.add(entity.getLabelId());
				}
				list2.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getLabelIdListForContact(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getContactId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ LabelContactDao.Properties.ContactId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<LabelContact> list2 = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getLabelContactDao()
						.queryRawCreate(
								"JOIN " + LabelDao.TABLENAME + " T1 ON T."
										+ LabelContactDao.Properties.ContactId.columnName
										+ " = T1." + ContactDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (LabelContact entity : list2) {
					selectedIds.add(entity.getLabelId());
				}
				list2.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getTaskIdListForContact(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted,
			final boolean includeActive, final boolean includeCompleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getContactId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ TaskContactDao.Properties.ContactId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + TaskDao.Properties.Deleted.columnName + " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + TaskDao.Properties.Deleted.columnName + " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				if (includeActive && includeCompleted) {
				} else if (includeActive && !includeCompleted) {
					sb.append(" AND T1." + TaskDao.Properties.IsCompleted.columnName
							+ " = 0");
				} else if (!includeActive && includeCompleted) {
					sb.append(" AND T1." + TaskDao.Properties.IsCompleted.columnName
							+ " = 1");
				} else if (!includeActive && !includeCompleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<TaskContact> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getTaskContactDao()
						.queryRawCreate(
								"JOIN " + TaskDao.TABLENAME + " T1 ON T."
										+ TaskContactDao.Properties.ContactId.columnName
										+ " = T1." + ContactDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (TaskContact entity : list) {
					selectedIds.add(entity.getTaskId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static ContactData getContactData(DaoSession daoSessionArg, Context context,
			Long id, boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ContactDataDao contactDataDao = daoSession.getContactDataDao();
		QueryBuilder<ContactData> qb = contactDataDao.queryBuilder();
		if (!isServerId) {
			qb.where(ContactDataDao.Properties.Id.eq(id));
		} else {
			qb.where(ContactDataDao.Properties.ServerId.eq(id));
		}
		ContactData contactData = qb.unique();
		return contactData;
	}

	public static List<ContactData> getContactDataList(Context context, Long id) {
		List<ContactData> contactDataList = new ArrayList<ContactData>();
		if (id == null) {
			return contactDataList;
		}
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		ContactDataDao contactDataDao = daoSession.getContactDataDao();
		QueryBuilder<ContactData> qb2 = contactDataDao.queryBuilder();
		qb2.where(ContactDataDao.Properties.LocalContactId.eq(id));
		contactDataList = qb2.list();
		return contactDataList;
	}

	public static biz.advancedcalendar.greendao.File getFile(DaoSession daoSessionArg,
			Context context, Long id) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		FileDao dao = daoSession.getFileDao();
		QueryBuilder<biz.advancedcalendar.greendao.File> qb = dao.queryBuilder();
		qb.where(FileDao.Properties.Id.eq(id));
		biz.advancedcalendar.greendao.File entity = qb.unique();
		return entity;
	}

	public static List<biz.advancedcalendar.greendao.File> getFileList(
			DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		return daoSession.getFileDao().queryBuilder()
				.where(FileDao.Properties.LocalVersionId.isNull()).list();
	}

	public static List<biz.advancedcalendar.greendao.File> getFileHistoryList(
			DaoSession daoSessionArg, Context context, Long localId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		return daoSession.getFileDao().queryBuilder()
				.where(FileDao.Properties.LocalId.eq(localId)).list();
	}

	public static ArrayList<Long> getContactIdListForTask(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getTaskId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ TaskContactDao.Properties.TaskId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + ContactDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + ContactDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<TaskContact> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getTaskContactDao()
						.queryRawCreate(
								"JOIN " + ContactDao.TABLENAME + " T1 ON T."
										+ TaskContactDao.Properties.TaskId.columnName
										+ " = T1." + TaskDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (TaskContact entity : list) {
					selectedIds.add(entity.getContactId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getContactIdListForLabel(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getLabelId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ LabelContactDao.Properties.LabelId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + ContactDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + ContactDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<LabelContact> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getLabelContactDao()
						.queryRawCreate(
								"JOIN " + ContactDao.TABLENAME + " T1 ON T."
										+ LabelContactDao.Properties.LabelId.columnName
										+ " = T1." + LabelDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (LabelContact entity : list) {
					selectedIds.add(entity.getContactId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getFileIdListForTask(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getTaskId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ FileTaskDao.Properties.TaskId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + FileDao.Properties.Deleted.columnName + " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + FileDao.Properties.Deleted.columnName + " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<FileTask> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getFileTaskDao()
						.queryRawCreate(
								"JOIN " + FileDao.TABLENAME + " T1 ON T."
										+ FileTaskDao.Properties.TaskId.columnName
										+ " = T1." + TaskDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (FileTask entity : list) {
					selectedIds.add(entity.getFileId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getTaskIdListForFile(DaoSession daoSessionArg,
			final Context context, final Long id1, final Integer versionId,
			final boolean isServerId, final boolean includeNonDeleted,
			final boolean includeDeleted, final boolean includeActive,
			final boolean includeCompleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getFileId(daoSession, context, id1, versionId);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ FileTaskDao.Properties.FileId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + TaskDao.Properties.Deleted.columnName + " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + TaskDao.Properties.Deleted.columnName + " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				if (includeActive && includeCompleted) {
				} else if (includeActive && !includeCompleted) {
					sb.append(" AND T1." + TaskDao.Properties.IsCompleted.columnName
							+ " = 0");
				} else if (!includeActive && includeCompleted) {
					sb.append(" AND T1." + TaskDao.Properties.IsCompleted.columnName
							+ " = 1");
				} else if (!includeActive && !includeCompleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<FileTask> list2 = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getFileTaskDao()
						.queryRawCreate(
								"JOIN " + TaskDao.TABLENAME + " T1 ON T."
										+ FileTaskDao.Properties.FileId.columnName
										+ " = T1." + FileDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (FileTask entity : list2) {
					selectedIds.add(entity.getTaskId());
				}
				list2.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getContactIdListForFile(DaoSession daoSessionArg,
			final Context context, final Long id1, final Integer versionId,
			final boolean isServerId, final boolean includeNonDeleted,
			final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getFileId(daoSession, context, id1, versionId);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ FileContactDao.Properties.FileId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + ContactDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + ContactDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<FileContact> list2 = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getFileContactDao()
						.queryRawCreate(
								"JOIN " + ContactDao.TABLENAME + " T1 ON T."
										+ FileContactDao.Properties.FileId.columnName
										+ " = T1." + FileDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (FileContact entity : list2) {
					selectedIds.add(entity.getContactId());
				}
				list2.close();
			}
		});
		return selectedIds;
	}

	public static DiaryRecord getDiaryRecord(DaoSession daoSessionArg, Context context,
			long id, boolean isServerId) {
		if (id == 0) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		DiaryRecordDao dao = daoSession.getDiaryRecordDao();
		QueryBuilder<DiaryRecord> qb2 = dao.queryBuilder();
		if (!isServerId) {
			qb2.where(DiaryRecordDao.Properties.Id.eq(id));
		} else {
			qb2.where(DiaryRecordDao.Properties.ServerId.eq(id));
		}
		DiaryRecord task = qb2.unique();
		return task;
	}

	public static List<DiaryRecord> getDiaryRecordList(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		DiaryRecordDao dao = daoSession.getDiaryRecordDao();
		QueryBuilder<DiaryRecord> qb = dao.queryBuilder();
		qb.orderAsc(DiaryRecordDao.Properties.Date).orderAsc(
				DiaryRecordDao.Properties.StartTime);
		List<DiaryRecord> diaryRecordList = qb.list();
		return diaryRecordList;
	}

	public static List<Message> getMessageList(DaoSession daoSessionArg, Context context,
			Long id, boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		MessageDao dao = daoSession.getMessageDao();
		QueryBuilder<Message> qb = dao.queryBuilder();
		if (!isServerId) {
			qb.where(MessageDao.Properties.LocalWorkgroupId.eq(id));
		} else {
			qb.where(MessageDao.Properties.WorkgroupId.eq(id));
		}
		List<Message> list = qb.list();
		return list;
	}

	// public static List<NotificationContact> getConfirmedUserNotificationContactList(
	// Context context, long id, byte contactType) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// NotificationContactDao dao = daoSession.getNotificationContactDao();
	// QueryBuilder<NotificationContact> qb2 = dao.queryBuilder();
	// qb2.where(NotificationContactDao.Properties.LocalProfileId.eq(id), qb2.and(
	// NotificationContactDao.Properties.ContactType.eq(contactType),
	// NotificationContactDao.Properties.IsConfirmed.eq(true)));
	// List<NotificationContact> confirmedUserNotificationContactList = qb2.list();
	// return confirmedUserNotificationContactList;
	// }
	public static Reminder getReminder(DaoSession daoSessionArg, Context context, Long id) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ReminderDao dao = daoSession.getReminderDao();
		QueryBuilder<Reminder> qb = dao.queryBuilder();
		qb.where(ReminderDao.Properties.Id.eq(id));
		Reminder entity = qb.unique();
		// entity.refresh();
		// dao.refresh(entity);
		// dao.detach(entity);
		return entity;
	}

	public static TaskOccurrence getTaskOccurrence(DaoSession daoSessionArg,
			Context context, Long id) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskOccurrenceDao dao = daoSession.getTaskOccurrenceDao();
		QueryBuilder<TaskOccurrence> qb = dao.queryBuilder();
		qb.where(TaskOccurrenceDao.Properties.Id.eq(id));
		TaskOccurrence entity = qb.unique();
		// entity.refresh();
		// dao.refresh(entity);
		// dao.detach(entity);
		return entity;
	}

	// public static MonthlyRepetitionsByDate getMonthlyRepetitionsByDate(Context context,
	// Long id) {
	// if (id == null) {
	// return null;
	// }
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDateDao dao = daoSession.getMonthlyRepetitionsByDateDao();
	// QueryBuilder<MonthlyRepetitionsByDate> qb = dao.queryBuilder();
	// qb.where(MonthlyRepetitionsByDateDao.Properties.Id.eq(id));
	// MonthlyRepetitionsByDate entity = qb.unique();
	// // entity.refresh();
	// // dao.refresh(entity);
	// // dao.detach(entity);
	// return entity;
	// }
	// public static MonthlyRepetitionsByDayOfWeek getMonthlyRepetitionsByDayOfWeek(
	// Context context, Long id) {
	// if (id == null) {
	// return null;
	// }
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDayOfWeekDao dao = daoSession
	// .getMonthlyRepetitionsByDayOfWeekDao();
	// QueryBuilder<MonthlyRepetitionsByDayOfWeek> qb = dao.queryBuilder();
	// qb.where(MonthlyRepetitionsByDayOfWeekDao.Properties.Id.eq(id));
	// MonthlyRepetitionsByDayOfWeek entity = qb.unique();
	// // entity.refresh();
	// // dao.refresh(entity);
	// // dao.detach(entity);
	// return entity;
	// }
	public static ScheduledReminder getScheduledReminder(DaoSession daoSessionArg,
			Context context, Long id) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ScheduledReminderDao dao = daoSession.getScheduledReminderDao();
		QueryBuilder<ScheduledReminder> qb = dao.queryBuilder();
		qb.where(ScheduledReminderDao.Properties.Id.eq(id));
		return qb.unique();
	}

	public static ElapsedReminder getElapsedReminder(DaoSession daoSessionArg,
			Context context, Long id) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ElapsedReminder entity = daoSession.getElapsedReminderDao().queryBuilder()
				.where(ElapsedReminderDao.Properties.Id.eq(id)).unique();
		return entity;
	}

	private static ArrayList<TreeViewListItemDescription> getTaskTree(
			DaoSession daoSessionArg, final Context context, final long rootId,
			final Long excludeSubtree, final boolean includeNonDeleted,
			final boolean includeDeleted, final boolean includeActive,
			final boolean includeCompleted) {
		final ArrayList<TreeViewListItemDescription> taskTree = new ArrayList<TreeViewListItemDescription>();
		if (excludeSubtree != null && excludeSubtree == rootId) {
			return taskTree;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				TaskDao dao = daoSession.getTaskDao();
				Task root = dao.load(rootId);
				if (root == null) {
					return;
				}
				taskTree.add(new TreeViewListItemDescriptionTaskImpl(root));
				DataProvider.insertChildTasksRecursively(daoSession, context, taskTree,
						root, excludeSubtree, includeNonDeleted, includeDeleted,
						includeActive, includeCompleted);
			}
		});
		return taskTree;
	}

	private static ArrayList<TreeViewListItemDescription> getLabelTree(
			DaoSession daoSessionArg, final Context context, final long rootId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<TreeViewListItemDescription> taskTree = new ArrayList<TreeViewListItemDescription>();
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				LabelDao dao = daoSession.getLabelDao();
				Label root = dao.load(rootId);
				if (root == null) {
					return;
				}
				// taskTree.add(new TreeViewListItemDescriptionImpl((Parcelable) root,
				// root
				// .getId(), root.getLocalParentId(), root.getDeepLevel(daoSession),
				// root.getText(), root.getSortOrder()));
				DataProvider.insertChildLabelsRecursively(daoSession, context, taskTree,
						root, null, includeNonDeleted, includeDeleted);
			}
		});
		return taskTree;
	}

	private static void insertChildTasksRecursively(DaoSession daoSessionArg,
			final Context context, final List<TreeViewListItemDescription> list,
			final Task parent, final Long excludeSubtree,
			final boolean includeNonDeleted, final boolean includeDeleted,
			final boolean includeActive, final boolean includeCompleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Task> taskChildren = DataProvider.getTaskChildren(daoSession,
						context, parent.getId(), includeNonDeleted, includeDeleted,
						includeActive, includeCompleted);
				if (taskChildren.size() == 0) {
					return;
				}
				int i = 0;
				for (i = 0; i < list.size(); i++) {
					if (list.get(i).getId() == parent.getId()) {
						break;
					}
				}
				i++;
				for (Task child : taskChildren) {
					if (excludeSubtree != null && child.getId() == excludeSubtree) {
						continue;
					}
					list.add(i++, new TreeViewListItemDescriptionTaskImpl(child));
				}
				for (Task child : taskChildren) {
					if (excludeSubtree != null && child.getId() == excludeSubtree) {
						continue;
					}
					DataProvider.insertChildTasksRecursively(daoSession, context, list,
							child, excludeSubtree, includeNonDeleted, includeDeleted,
							includeActive, includeCompleted);
				}
			}
		});
	}

	private static void insertChildLabelsRecursively(DaoSession daoSessionArg,
			final Context context, final List<TreeViewListItemDescription> list,
			final Label parent, final Long excludeSubtree,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Label> taskChildren = DataProvider.getLabelChildren(daoSession,
						context, parent.getId(), includeNonDeleted, includeDeleted);
				if (taskChildren.size() == 0) {
					return;
				}
				int i = 0;
				for (i = 0; i < list.size(); i++) {
					if (list.get(i).getId() == parent.getId()) {
						break;
					}
				}
				i++;
				for (Label child : taskChildren) {
					if (excludeSubtree != null && child.getId() == excludeSubtree) {
						continue;
					}
					// list.add(
					// i++,
					// new TreeViewListItemDescriptionTaskImpl(child));
				}
				for (Label child : taskChildren) {
					if (excludeSubtree != null && child.getId() == excludeSubtree) {
						continue;
					}
					DataProvider.insertChildLabelsRecursively(daoSession, context, list,
							child, excludeSubtree, includeNonDeleted, includeDeleted);
				}
			}
		});
	}

	public static ArrayList<ArrayList<TreeViewListItemDescription>> getTreeViewListItemDescriptionForestOfActiveTasks(
			DaoSession daoSessionArg, final Context context,
			final boolean includeCompletedChildren) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ArrayList<ArrayList<TreeViewListItemDescription>> forest = new ArrayList<ArrayList<TreeViewListItemDescription>>();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				{
					final List<Task> taskList = DataProvider.getTaskChildren(daoSession,
							context, null, true, false, true, false);
					for (Task task : taskList) {
						localTempRootList.add(task.getId());
					}
				}
				for (long id : localTempRootList) {
					forest.add(DataProvider.getTaskTree(daoSession, context, id, null,
							true, false, true, includeCompletedChildren));
				}
			}
		});
		return forest;
	}

	public static List<Node<Task>> getNodesForestOfActiveTasks(DaoSession daoSessionArg,
			final Context context, final boolean includeCompletedChildren) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final List<Node<Task>> forest = new ArrayList<Node<Task>>();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final List<Task> taskList = DataProvider.getTaskChildren(daoSession,
						context, null, true, false, true, false);
				for (Task task : taskList) {
					Node<Task> node = new Node<Task>(task, task.getId(), true, null,
							new ArrayList<Node<Task>>());
					forest.add(node);
					addNodesRecursively(node, includeCompletedChildren);
				}
			}

			private void addNodesRecursively(Node<Task> node,
					final boolean includeCompletedChildren) {
				List<Task> children = DataProvider.getTaskChildren(daoSession, context,
						node.mId, true, false, true, includeCompletedChildren);
				for (Task task : children) {
					Node<Task> childNode = new Node<Task>(task, task.getId(), true, node,
							new ArrayList<Node<Task>>());
					node.mChildren.add(childNode);
					addNodesRecursively(childNode, includeCompletedChildren);
				}
			}
		});
		return forest;
	}

	public static List<Node<Task>> getNodesForestOfCompletedTasks(
			DaoSession daoSessionArg, final Context context,
			final boolean includeActiveParents) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final List<Node<Task>> forest = new ArrayList<Node<Task>>();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				{
					DataProvider.addCompletedRootNodesRecursively(daoSession, context,
							null, null, true, false, localTempRootList);
				}
				List<Long> localTempRootList2;
				if (includeActiveParents) {
					localTempRootList2 = new ArrayList<Long>();
					for (long id : localTempRootList) {
						Task task = DataProvider.getTask(daoSession, context, id, false);
						while (task.getParentId() != null) {
							task = DataProvider.getTask(daoSession, context,
									task.getParentId(), false);
						}
						if (!localTempRootList2.contains(task.getId())) {
							localTempRootList2.add(task.getId());
						}
					}
				} else {
					localTempRootList2 = localTempRootList;
				}
				final List<Task> taskList = new ArrayList<Task>();
				for (long id : localTempRootList2) {
					taskList.add(DataProvider.getTask(daoSession, context, id, false));
				}
				for (Task task : taskList) {
					Node<Task> node = new Node<Task>(task, task.getId(), true, null,
							new ArrayList<Node<Task>>());
					forest.add(node);
					addNodesRecursively(node, true);
				}
			}

			private void addNodesRecursively(Node<Task> node,
					final boolean includeCompletedChildren) {
				List<Task> children = DataProvider.getTaskChildren(daoSession, context,
						node.mId, true, false, true, includeCompletedChildren);
				for (Task task : children) {
					Node<Task> childNode = new Node<Task>(task, task.getId(), true, node,
							new ArrayList<Node<Task>>());
					node.mChildren.add(childNode);
					addNodesRecursively(childNode, includeCompletedChildren);
				}
			}
		});
		return forest;
	}

	public static ArrayList<ArrayList<TreeViewListItemDescription>> getTreeViewListItemDescriptionForestOfCompletedTasks(
			DaoSession daoSessionArg, final Context context,
			final boolean includeActiveParents) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ArrayList<ArrayList<TreeViewListItemDescription>> forest = new ArrayList<ArrayList<TreeViewListItemDescription>>();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				{
					DataProvider.addCompletedRootNodesRecursively(daoSession, context,
							null, null, true, false, localTempRootList);
				}
				List<Long> localTempRootList2;
				if (includeActiveParents) {
					localTempRootList2 = new ArrayList<Long>();
					for (long id : localTempRootList) {
						Task task = DataProvider.getTask(daoSession, context, id, false);
						while (task.getParentId() != null) {
							task = DataProvider.getTask(daoSession, context,
									task.getParentId(), false);
						}
						if (!localTempRootList2.contains(task.getId())) {
							localTempRootList2.add(task.getId());
						}
					}
				} else {
					localTempRootList2 = localTempRootList;
				}
				for (long id : localTempRootList2) {
					forest.add(DataProvider.getTaskTree(daoSession, context, id, null,
							true, false, includeActiveParents, true));
				}
			}
		});
		return forest;
	}

	public static TreeViewListItemDescriptionMatrix getTreeViewListItemDescriptionMatrix(
			DaoSession daoSessionArg, final Context context, final List<Long> rootList,
			final Long excludeSubtree, final boolean includeNonDeleted,
			final boolean includeDeleted, final boolean includeActive,
			final boolean includeCompleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows = new ArrayList<TreeViewListItemDescriptionRow>();
		final TreeViewListItemDescriptionMatrix treeViewListItemDescriptionMatrix = new TreeViewListItemDescriptionMatrix(
				treeViewListItemDescriptionRows);
		// check arguments
		if (rootList != null && rootList.size() == 0 || !includeActive
				&& !includeCompleted) {
			return treeViewListItemDescriptionMatrix;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				if (!includeActive && includeCompleted) {
					if (rootList == null) {
						DataProvider.addCompletedRootNodesRecursively(daoSession,
								context, null, excludeSubtree, includeNonDeleted,
								includeDeleted, localTempRootList);
					} else {
						for (Long id : rootList) {
							if (id != excludeSubtree) {
								Task task = DataProvider.getTask(daoSession, context, id,
										false);
								if (task.getIsCompleted()) {
									localTempRootList.add(id);
								}
							}
						}
					}
				} else {
					if (rootList == null) {
						final List<Task> taskList = DataProvider.getTaskChildren(
								daoSession, context, null, includeNonDeleted,
								includeDeleted, includeActive, includeCompleted);
						for (Task task : taskList) {
							if (task.getId() != excludeSubtree) {
								localTempRootList.add(task.getId());
							}
						}
					} else {
						for (Long id : rootList) {
							if (id != excludeSubtree) {
								localTempRootList.add(id);
							}
						}
					}
				}
				for (long id : localTempRootList) {
					ArrayList<TreeViewListItemDescription> taskTree = DataProvider
							.getTaskTree(daoSession, context, id, excludeSubtree,
									includeNonDeleted, includeDeleted, includeActive,
									includeCompleted);
					TreeViewListItemDescriptionRow treeViewListItemDescriptionRow = new TreeViewListItemDescriptionRow(
							taskTree);
					treeViewListItemDescriptionRows.add(treeViewListItemDescriptionRow);
				}
			}
		});
		return treeViewListItemDescriptionMatrix;
	}

	public static ArrayList<ArrayList<TreeViewListItemDescription>> getTreeViewListItemDescriptionForest(
			DaoSession daoSessionArg, final Context context, final List<Long> rootList,
			final Long excludeSubtree, final boolean includeNonDeleted,
			final boolean includeDeleted, final boolean includeActive,
			final boolean includeCompleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ArrayList<ArrayList<TreeViewListItemDescription>> forest = new ArrayList<ArrayList<TreeViewListItemDescription>>();
		// check arguments
		if (rootList != null && rootList.size() == 0 || !includeActive
				&& !includeCompleted) {
			return forest;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				if (!includeActive && includeCompleted) {
					if (rootList == null) {
						DataProvider.addCompletedRootNodesRecursively(daoSession,
								context, null, excludeSubtree, includeNonDeleted,
								includeDeleted, localTempRootList);
					} else {
						for (Long id : rootList) {
							if (id != excludeSubtree) {
								Task task = DataProvider.getTask(daoSession, context, id,
										false);
								if (task.getIsCompleted()) {
									localTempRootList.add(id);
								}
							}
						}
					}
				} else {
					if (rootList == null) {
						final List<Task> taskList = DataProvider.getTaskChildren(
								daoSession, context, null, includeNonDeleted,
								includeDeleted, includeActive, includeCompleted);
						for (Task task : taskList) {
							if (task.getId() != excludeSubtree) {
								localTempRootList.add(task.getId());
							}
						}
					} else {
						for (Long id : rootList) {
							if (id != excludeSubtree) {
								localTempRootList.add(id);
							}
						}
					}
				}
				for (long id : localTempRootList) {
					forest.add(DataProvider.getTaskTree(daoSession, context, id,
							excludeSubtree, includeNonDeleted, includeDeleted,
							includeActive, includeCompleted));
				}
			}
		});
		return forest;
	}

	private static void addCompletedRootNodesRecursively(DaoSession daoSessionArg,
			final Context context, final Long id, final Long excludeSubtree,
			final boolean includeNonDeleted, final boolean includeDeleted,
			List<Long> localTempRootList) {
		final List<Task> taskList = DataProvider.getTaskChildren(daoSessionArg, context,
				id, includeNonDeleted, includeDeleted, true, true);
		for (Task task : taskList) {
			if (task.getId() != excludeSubtree) {
				if (task.getIsCompleted()) {
					localTempRootList.add(task.getId());
				} else {
					DataProvider.addCompletedRootNodesRecursively(daoSessionArg, context,
							task.getId(), excludeSubtree, includeNonDeleted,
							includeDeleted, localTempRootList);
				}
			}
		}
	}

	public static ArrayList<Long> getTaskIdListForLabel(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted,
			final boolean includeActive, final boolean includeCompleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getLabelId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ TaskLabelDao.Properties.LabelId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + TaskDao.Properties.Deleted.columnName + " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + TaskDao.Properties.Deleted.columnName + " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				if (includeActive && includeCompleted) {
				} else if (includeActive && !includeCompleted) {
					sb.append(" AND T1." + TaskDao.Properties.IsCompleted.columnName
							+ " = 0");
				} else if (!includeActive && includeCompleted) {
					sb.append(" AND T1." + TaskDao.Properties.IsCompleted.columnName
							+ " = 1");
				} else if (!includeActive && !includeCompleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<TaskLabel> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getTaskLabelDao()
						.queryRawCreate(
								"JOIN " + TaskDao.TABLENAME + " T1 ON T."
										+ TaskLabelDao.Properties.LabelId.columnName
										+ " = T1." + LabelDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (TaskLabel entity : list) {
					selectedIds.add(entity.getTaskId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	// public static List<CalendarViewTaskOccurrence> getWeekViewCoreTasks(Context
	// context) {
	// ArrayList<ArrayList<TreeViewListItemDescription>> treeViewListItemDescriptionList =
	// DataProvider
	// .getTreeViewListItemDescriptionForest(context, null, null, true, false,
	// true, false);
	// List<CalendarViewTaskOccurrence> weekViewCoreTasks = new
	// ArrayList<CalendarViewTaskOccurrence>();
	// for (List<TreeViewListItemDescription> task2 : treeViewListItemDescriptionList) {
	// for (TreeViewListItemDescription task : task2) {
	// Task t = DataProvider.getTask(context, task.getId(), false);
	// CalendarViewTaskOccurrence weekViewCoreTask = new CalendarViewTaskOccurrence(
	// t, t.getId(), t.getStartDateTime(), t.getEndDateTime(),
	// t.getText(), RecurrenceInterval.fromInt(t.getRecurrenceIntervalValue()),
	// t.getPriority(), t.getColor(), SyncStatus.fromInt(t
	// .getSyncStatus()));
	// weekViewCoreTasks.add(weekViewCoreTask);
	// }
	// }
	// return weekViewCoreTasks;
	// }
	public static void insertOrReplaceTask(DaoSession daoSessionArg,
			final Context context, final Task newEntity,
			final boolean isBeingExplicitlySavedByUserFromUi) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				long timeSkew = Helper.getLongPreferenceValue(context,
						CommonConstants.TIME_SKEW, 0, null, null);
				TaskDao dao = daoSession.getTaskDao();
				if (newEntity.getDeleted()) {
					throw new IllegalArgumentException(
							"Illegal state: newEntity.getDeleted() == true");
				}
				// verify field consistency
				if (newEntity.getServerId() == null
						&& newEntity.getSyncStatusValue() != SyncStatus.SYNC_UP_REQUIRED
								.getValue()) {
					throw new IllegalArgumentException(
							"Illegal state: newEntity.getServerId() == null && newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED.getValue()");
				}
				if (RecurrenceInterval.fromInt(newEntity.getRecurrenceIntervalValue())
						.equals(RecurrenceInterval.ONE_TIME)) {
					if (newEntity.getRepetitionEndDateTime() != null) {
						newEntity.setRepetitionEndDateTime(null);
						Helper.setLocalChangeDateTimeToCurrentTime(newEntity, timeSkew);
					}
				}
				if (newEntity.getTimeUnitsCount() == null) {
					if (!RecurrenceInterval.fromInt(
							newEntity.getRecurrenceIntervalValue()).equals(
							RecurrenceInterval.ONE_TIME)) {
						throw new IllegalArgumentException(
								"newEntity.getTimeUnitsCount() == null && !RecurrenceInterval.fromInt(newEntity.getRecurrenceIntervalValue()).equals(RecurrenceInterval.ONE_TIME)");
					}
					if (newEntity.getOccurrencesMaxCount() != null) {
						throw new IllegalArgumentException(
								"newEntity.getTimeUnitsCount() == null && newEntity.getOccurrencesMaxCount() != null");
					}
				} else {
					if (newEntity.getTimeUnitsCount() <= 0) {
						throw new IllegalArgumentException(
								"Wrong number of time units for task with id "
										+ newEntity.getId() + ": "
										+ newEntity.getTimeUnitsCount());
					}
					if (newEntity.getRecurrenceIntervalValue() == RecurrenceInterval.ONE_TIME
							.getValue()) {
						throw new IllegalArgumentException(
								"TimeUnitsCount != null && RecurrenceInterval == RecurrenceInterval.ONE_TIME.getValue()");
					}
					if (newEntity.getStartDateTime() == null) {
						throw new IllegalArgumentException(
								"newEntity.getTimeUnitsCount() != null && newEntity.getStartDateTime() == null");
					}
					if (newEntity.getEndDateTime() == null) {
						throw new IllegalArgumentException(
								"newEntity.getTimeUnitsCount() != null && newEntity.getEndDateTime() == null");
					}
					// verify recurrence interval
					RecurrenceInterval recurrenceInterval = RecurrenceInterval
							.fromInt(newEntity.getRecurrenceIntervalValue());
					if (recurrenceInterval == null) {
						throw new IllegalArgumentException(
								"newEntity.getRecurrenceIntervalValue() == "
										+ newEntity.getRecurrenceIntervalValue());
					}
				}
				// TODO verify parent child relation consistency
				if (newEntity.getId() != null
						&& newEntity.getParentId() != null
						&& newEntity.getId().longValue() == newEntity.getParentId()
								.longValue()) {
					throw new IllegalArgumentException(
							"Illegal arguments: newEntity.getId() == newEntity.getParentId() || newEntity.getServerId() == newEntity.getServerParentId()");
				}
				// newEntity.getId() == null
				if (newEntity.getId() == null) {
					// newEntity.getId() == null
					// newEntity.getParentId() == null
					if (newEntity.getParentId() == null) {
						// newEntity.getId() == null
						// newEntity.getParentId() == null
						// newEntity.getServerId() != null
						if (newEntity.getServerId() != null) {
							Task localEntityByServerId = DataProvider.getTask(daoSession,
									context, newEntity.getServerId(), true);
							if (localEntityByServerId != null) {
								newEntity.setId(localEntityByServerId.getId());
							}
						}
					}
					// newEntity.getId() == null
					// newEntity.getParentId() != null
					else {
						Task localParentEntityByLocalParentId = DataProvider.getTask(
								daoSession, context, newEntity.getParentId(), false);
						if (localParentEntityByLocalParentId == null) {
							throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
									"The requested parent task with id "
											+ newEntity.getParentId()
											+ " does not exist.");
						}
						// newEntity.getId() == null
						// newEntity.getParentId() != null
						// newEntity.getServerId() != null
						if (newEntity.getServerId() != null) {
							Task localEntityByServerId = DataProvider.getTask(daoSession,
									context, newEntity.getServerId(), true);
							if (localEntityByServerId != null) {
								newEntity.setId(localEntityByServerId.getId());
							}
						}
					}
				}
				// newEntity.getId() != null
				else {
					Task localEntityByLocalId = DataProvider.getTask(daoSession, context,
							newEntity.getId(), false);
					if (localEntityByLocalId == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested task with id " + newEntity.getId()
										+ " does not exist.");
					}
					// newEntity.getId() != null
					// newEntity.getParentId() == null
					if (newEntity.getParentId() == null) {
						// newEntity.getId() != null
						// newEntity.getParentId() == null
						// newEntity.getServerId() == null
						if (newEntity.getServerId() == null) {
							newEntity.setServerId(localEntityByLocalId.getServerId());
						}
						// newEntity.getId() != null
						// newEntity.getParentId() == null
						// newEntity.getServerId() != null
						else {
							Task localEntityByServerId = DataProvider.getTask(daoSession,
									context, newEntity.getServerId(), true);
							if (localEntityByServerId != null
									&& localEntityByLocalId.getId() != localEntityByServerId
											.getId()) {
								throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
										"The LocalId and ServerId do not refer to the same entity");
							}
						}
					}
					// newEntity.getId() != null
					// newEntity.getParentId() != null
					else {
						Task localParentEntityByLocalParentId = DataProvider.getTask(
								daoSession, context, newEntity.getParentId(), false);
						if (localParentEntityByLocalParentId == null) {
							throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
									"The requested parent task with id "
											+ newEntity.getParentId()
											+ " does not exist.");
						}
						// newEntity.getId() != null
						// newEntity.getParentId() != null
						// newEntity.getServerId() == null
						if (newEntity.getServerId() == null) {
							newEntity.setServerId(localEntityByLocalId.getServerId());
						}
						// newEntity.getId() != null
						// newEntity.getParentId() != null
						// newEntity.getServerId() != null
						else {
							Task localEntityByServerId = DataProvider.getTask(daoSession,
									context, newEntity.getServerId(), true);
							if (localEntityByServerId != null
									&& localEntityByLocalId.getId() != localEntityByServerId
											.getId()) {
								throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
										"The LocalId and ServerId do not refer to the same entity");
							}
						}
					}
				}
				// verify completed consistency with parent
				if (newEntity.getParentId() != null) {
					Task localParentEntityByLocalParentId = DataProvider.getTask(
							daoSession, context, newEntity.getParentId(), false);
					if (localParentEntityByLocalParentId.getDeleted()
							&& !newEntity.getDeleted()) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"Illegal state: ParentTask.Deleted == true and NewTask.Deleted != true.");
					}
					if (localParentEntityByLocalParentId.getIsCompleted()
							&& !newEntity.getIsCompleted()) {
						// throw new
						// DatabaseStateAndNewEntityArgumentsInconsistencyException(
						// "Illegal state: ParentTask.Completed == true and NewTask.Completed != true.");
						//
						// TODO resetup alarms for tasks changed during
						// markTasksAsCompleted() call
						DataProvider.markTasksAsCompleted(daoSession, context,
								new long[] {newEntity.getParentId()}, false);
					}
				}
				if (newEntity.getId() != null) {
					// If completed set completed on the nodes of the subtree
					// If active set active on all parent nodes
					List<Task> taskList = new ArrayList<Task>();
					if (newEntity.getIsCompleted()) {
						List<Long> rootList = new ArrayList<Long>();
						rootList.add(newEntity.getId());
						ArrayList<ArrayList<TreeViewListItemDescription>> forest = DataProvider
								.getTreeViewListItemDescriptionForest(daoSession,
										context, rootList, null, true, true, true, true);
						for (ArrayList<TreeViewListItemDescription> tree : forest) {
							for (TreeViewListItemDescription treeViewListItemDescription : tree) {
								Task entity = dao.load(treeViewListItemDescription
										.getId());
								if (newEntity.getIsCompleted()) {
									entity.setIsCompleted(true);
									if (entity.getId() == newEntity.getId()
											|| isBeingExplicitlySavedByUserFromUi) {
										long nowTime = System.currentTimeMillis();
										entity.setLocalChangeDateTime(nowTime);
										entity.setLastMod(nowTime - timeSkew);
									}
								}
								taskList.add(entity);
							}
						}
					} else {
						Long nextId = newEntity.getParentId();
						long nowTime = System.currentTimeMillis();
						while (nextId != null) {
							Task entity = dao.load(nextId);
							entity.setIsCompleted(false);
							if (isBeingExplicitlySavedByUserFromUi) {
								entity.setLocalChangeDateTime(nowTime);
								entity.setLastMod(nowTime - timeSkew);
							}
							// entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
							taskList.add(entity);
							nextId = entity.getParentId();
						}
					}
					dao.updateInTx(taskList);
				}
				// adjust sort order with siblings
				// TODO no need to adjust sort order with siblings when task is deleted
				if (isBeingExplicitlySavedByUserFromUi) {
					List<Task> rightSiblings;
					if (newEntity.getId() == null) {
						if (newEntity.getParentId() == null) {
							rightSiblings = ((Global) context.getApplicationContext())
									.getDaoSession()
									.getTaskDao()
									.queryBuilder()
									.where(TaskDao.Properties.ParentId.isNull(),
											TaskDao.Properties.SortOrder.ge(newEntity
													.getSortOrder()))
									.orderAsc(TaskDao.Properties.SortOrder).build()
									.list();
						} else {
							rightSiblings = ((Global) context.getApplicationContext())
									.getDaoSession()
									.getTaskDao()
									.queryBuilder()
									.where(TaskDao.Properties.ParentId.eq(newEntity
											.getParentId()),
											TaskDao.Properties.SortOrder.ge(newEntity
													.getSortOrder()))
									.orderAsc(TaskDao.Properties.SortOrder).build()
									.list();
						}
					} else {
						if (newEntity.getParentId() == null) {
							rightSiblings = ((Global) context.getApplicationContext())
									.getDaoSession()
									.getTaskDao()
									.queryBuilder()
									.where(TaskDao.Properties.ParentId.isNull(),
											TaskDao.Properties.Id.notEq(newEntity.getId()),
											TaskDao.Properties.SortOrder.ge(newEntity
													.getSortOrder()))
									.orderAsc(TaskDao.Properties.SortOrder).build()
									.list();
						} else {
							rightSiblings = ((Global) context.getApplicationContext())
									.getDaoSession()
									.getTaskDao()
									.queryBuilder()
									.where(TaskDao.Properties.ParentId.eq(newEntity
											.getParentId()),
											TaskDao.Properties.Id.notEq(newEntity.getId()),
											TaskDao.Properties.SortOrder.ge(newEntity
													.getSortOrder()))
									.orderAsc(TaskDao.Properties.SortOrder).build()
									.list();
						}
					}
					int sortOrder = newEntity.getSortOrder();
					int i = 0;
					// if (rightSiblings.size() > 0
					// && rightSiblings.get(0).getId() != newEntity.getId()) {
					// i++;
					// }
					for (; i < rightSiblings.size(); i++) {
						Task nextRightSiblingTask = rightSiblings.get(i);
						int newSortOrder = ++sortOrder;
						if (nextRightSiblingTask.getSortOrder() >= newSortOrder) {
							break;
						}
						nextRightSiblingTask.setSortOrder(newSortOrder);
						Helper.setLocalChangeDateTimeToCurrentTime(nextRightSiblingTask,
								timeSkew);
						dao.insertOrReplace(nextRightSiblingTask);
					}
				}
				// At last, save our newEntity
				dao.insertOrReplace(newEntity);
			}
		});
	}

	public static void insertOrReplaceLabel(DaoSession daoSessionArg,
			final Context context, final Label newEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				LabelDao dao = daoSession.getLabelDao();
				// verify field consistency
				if (newEntity.getServerId() == null
						&& newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED
								.getValue()) {
					throw new IllegalArgumentException(
							"Illegal state: newEntity.getServerId() == null && newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED.getValue()");
				}
				// verify child parent relation consistency
				if (newEntity.getId() != null
						&& newEntity.getLocalParentId() != null
						&& newEntity.getId().longValue() == newEntity.getLocalParentId()
								.longValue()) {
					throw new IllegalArgumentException(
							"Illegal arguments: newEntity.getId() == newEntity.getLocalParentId() || newEntity.getServerId() == newEntity.getServerParentId()");
				}
				// newEntity.getId() == null
				if (newEntity.getId() == null) {
					// newEntity.getId() == null
					// newEntity.getLocalParentId() == null
					if (newEntity.getLocalParentId() == null) {
						// newEntity.getId() == null
						// newEntity.getLocalParentId() == null
						// newEntity.getServerId() != null
						if (newEntity.getServerId() != null) {
							Label localEntityByServerId = DataProvider.getLabel(
									daoSession, context, newEntity.getServerId(), true);
							if (localEntityByServerId != null) {
								newEntity.setId(localEntityByServerId.getId());
							}
						}
					}
					// newEntity.getId() == null
					// newEntity.getLocalParentId() != null
					else {
						Label localParentEntityByLocalParentId = DataProvider.getLabel(
								daoSession, context, newEntity.getLocalParentId(), false);
						if (localParentEntityByLocalParentId == null) {
							throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
									"The requested parent entity with id "
											+ newEntity.getLocalParentId()
											+ " does not exist.");
						}
						// newEntity.getId() == null
						// newEntity.getLocalParentId() != null
						// newEntity.getServerId() != null
						if (newEntity.getServerId() != null) {
							Label localEntityByServerId = DataProvider.getLabel(
									daoSession, context, newEntity.getServerId(), true);
							if (localEntityByServerId != null) {
								newEntity.setId(localEntityByServerId.getId());
							}
						}
					}
				}
				// newEntity.getId() != null
				else {
					Label localEntityByLocalId = DataProvider.getLabel(daoSession,
							context, newEntity.getId(), false);
					if (localEntityByLocalId == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested entity with id " + newEntity.getId()
										+ " does not exist.");
					}
					// newEntity.getId() != null
					// newEntity.getLocalParentId() == null
					if (newEntity.getLocalParentId() == null) {
						// newEntity.getId() != null
						// newEntity.getLocalParentId() == null
						// newEntity.getServerId() == null
						if (newEntity.getServerId() == null) {
							newEntity.setServerId(localEntityByLocalId.getServerId());
						}
						// newEntity.getId() != null
						// newEntity.getLocalParentId() == null
						// newEntity.getServerId() != null
						else {
							Label localEntityByServerId = DataProvider.getLabel(
									daoSession, context, newEntity.getServerId(), true);
							if (localEntityByServerId != null
									&& localEntityByLocalId.getId() != localEntityByServerId
											.getId()) {
								throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
										"The LocalId and ServerId do not refer to the same entity");
							}
						}
					}
					// newEntity.getId() != null
					// newEntity.getLocalParentId() != null
					else {
						Label localParentEntityByLocalParentId = DataProvider.getLabel(
								daoSession, context, newEntity.getLocalParentId(), false);
						if (localParentEntityByLocalParentId == null) {
							throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
									"The requested parent entity with id "
											+ newEntity.getLocalParentId()
											+ " does not exist.");
						}
						// newEntity.getId() != null
						// newEntity.getLocalParentId() != null
						// newEntity.getServerId() == null
						if (newEntity.getServerId() == null) {
							newEntity.setServerId(localEntityByLocalId.getServerId());
						}
						// newEntity.getId() != null
						// newEntity.getLocalParentId() != null
						// newEntity.getServerId() != null
						else {
							Label localEntityByServerId = DataProvider.getLabel(
									daoSession, context, newEntity.getServerId(), true);
							if (localEntityByServerId != null
									&& localEntityByLocalId.getId() != localEntityByServerId
											.getId()) {
								throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
										"The LocalId and ServerId do not refer to the same entity");
							}
						}
					}
				}
				// verify deleted consistency with parent
				if (newEntity.getLocalParentId() != null) {
					Label localParentEntityByLocalParentId = DataProvider.getLabel(
							daoSession, context, newEntity.getLocalParentId(), false);
					if (localParentEntityByLocalParentId.getDeleted()
							&& !newEntity.getDeleted()) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"Illegal state: ParentLabel.Deleted == true and NewLabel.Deleted != true.");
					}
				}
				// adjust DeepLevels on the node and the subtree
				if (newEntity.getId() != null) {
					// If deleted set deleted on the nodes of the subtree
					// If completed set completed on the nodes of the subtree
					// If active set active on all parent nodes
					List<Label> taskList = new ArrayList<Label>();
					if (newEntity.getDeleted()) {
						List<Long> rootList = new ArrayList<Long>();
						rootList.add(newEntity.getId());
						ArrayList<ArrayList<TreeViewListItemDescription>> forest = DataProvider
								.getLabelForest(daoSession, context, rootList, null,
										false, true);
						for (ArrayList<TreeViewListItemDescription> tree : forest) {
							for (TreeViewListItemDescription treeViewListItemDescription : tree) {
								Label entity = dao.load(treeViewListItemDescription
										.getId());
								if (newEntity.getDeleted()) {
									entity.setDeleted(true);
								}
								taskList.add(entity);
							}
						}
					}
					dao.updateInTx(taskList);
				}
				// adjust sort order with siblings
				// TODO no need to adjust sort order with siblings when task is deleted
				List<Label> rightSiblings;
				if (newEntity.getLocalParentId() == null) {
					rightSiblings = ((Global) context.getApplicationContext())
							.getDaoSession()
							.getLabelDao()
							.queryBuilder()
							.where(LabelDao.Properties.LocalParentId.isNull(),
									LabelDao.Properties.SortOrder.ge(newEntity
											.getSortOrder()))
							.orderAsc(LabelDao.Properties.SortOrder).build().list();
				} else {
					rightSiblings = ((Global) context.getApplicationContext())
							.getDaoSession()
							.getLabelDao()
							.queryBuilder()
							.where(LabelDao.Properties.LocalParentId.eq(newEntity
									.getLocalParentId()),
									LabelDao.Properties.SortOrder.ge(newEntity
											.getSortOrder()))
							.orderAsc(LabelDao.Properties.SortOrder).build().list();
				}
				int sortOrder = newEntity.getSortOrder();
				for (int i = 0; i < rightSiblings.size(); i++) {
					Label t = rightSiblings.get(i);
					if (t.getId() != newEntity.getId()) {
						int previousSortOrder = t.getSortOrder();
						int newSortOrder = ++sortOrder;
						if (previousSortOrder != newSortOrder
								&& t.getSyncStatus() != SyncStatus.SYNC_DOWN_REQUIRED
										.getValue()) {
							t.setSortOrder(newSortOrder);
							long nowTime = System.currentTimeMillis();
							t.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
							t.setLocalChangeDateTime(nowTime);
							Long timeSkew = Helper.getLongPreferenceValue(context,
									CommonConstants.TIME_SKEW, 0, null, null);
							t.setLastMod(nowTime - timeSkew);
							dao.insertOrReplace(t);
						}
					}
				}
				// At last, save our newEntity
				dao.insertOrReplace(newEntity);
			}
		});
	}

	public static void insertOrReplaceContact(DaoSession daoSessionArg,
			final Context context, final Contact newEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				ContactDao dao = daoSession.getContactDao();
				// verify field consistency
				if (newEntity.getServerId() == null
						&& newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED
								.getValue()) {
					throw new IllegalArgumentException(
							"Illegal state: newEntity.getServerId() == null && newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED.getValue()");
				}
				// newEntity.getId() == null
				if (newEntity.getId() == null) {
					// newEntity.getId() == null
					// newEntity.getLocalParentId() == null
				}
				// newEntity.getId() != null
				else {
					Contact localEntityByLocalId = DataProvider.getContact(daoSession,
							context, newEntity.getId(), false);
					if (localEntityByLocalId == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested entity with id " + newEntity.getId()
										+ " does not exist.");
					}
					// newEntity.getId() != null
					// newEntity.getLocalParentId() == null
				}
				// At last, save our newEntity
				dao.insertOrReplace(newEntity);
			}
		});
	}

	public static List<Task> getTaskSiblingsFromParent(DaoSession daoSessionArg,
			Context context, Long id, Long parentId, boolean includeSelf, boolean isActive) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		List<Task> taskSiblings = null;
		WhereCondition whereConditionParentAssigned;
		WhereCondition whereConditionActive;
		if (parentId != null) {
			whereConditionParentAssigned = TaskDao.Properties.ParentId.eq(parentId);
		} else {
			whereConditionParentAssigned = TaskDao.Properties.ParentId.isNull();
		}
		if (isActive) {
			whereConditionActive = TaskDao.Properties.IsCompleted.eq(false);
		} else {
			whereConditionActive = TaskDao.Properties.IsCompleted.eq(true);
		}
		if (!includeSelf && id != null) {
			taskSiblings = dao
					.queryBuilder()
					.where(whereConditionParentAssigned, TaskDao.Properties.Id.notEq(id),
							whereConditionActive, TaskDao.Properties.Deleted.eq(false))
					.orderAsc(TaskDao.Properties.SortOrder).list();
		} else {
			taskSiblings = dao
					.queryBuilder()
					.where(whereConditionParentAssigned, whereConditionActive,
							TaskDao.Properties.Deleted.eq(false))
					.orderAsc(TaskDao.Properties.SortOrder).list();
		}
		return taskSiblings;
	}

	public static List<Task> getActiveTaskSiblingsFromParent(DaoSession daoSessionArg,
			Context context, Long id, Long parentId, boolean includeSelf) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		List<Task> taskSiblings = null;
		if (parentId != null) {
			if (!includeSelf && id != null) {
				taskSiblings = dao
						.queryBuilder()
						.where(TaskDao.Properties.ParentId.eq(parentId),
								TaskDao.Properties.Id.notEq(id),
								TaskDao.Properties.IsCompleted.eq(false))
						.orderAsc(TaskDao.Properties.SortOrder).list();
			} else {
				taskSiblings = dao
						.queryBuilder()
						.where(TaskDao.Properties.ParentId.eq(parentId),
								TaskDao.Properties.IsCompleted.eq(false))
						.orderAsc(TaskDao.Properties.SortOrder).list();
			}
		} else {
			if (!includeSelf && id != null) {
				taskSiblings = dao
						.queryBuilder()
						.where(TaskDao.Properties.ParentId.isNull(),
								TaskDao.Properties.Id.notEq(id),
								TaskDao.Properties.IsCompleted.eq(false))
						.orderAsc(TaskDao.Properties.SortOrder).list();
			} else {
				taskSiblings = dao
						.queryBuilder()
						.where(TaskDao.Properties.ParentId.isNull(),
								TaskDao.Properties.IsCompleted.eq(false))
						.orderAsc(TaskDao.Properties.SortOrder).list();
			}
		}
		return taskSiblings;
	}

	public static List<Task> getActiveTaskSiblings(DaoSession daoSessionArg,
			Context context, long id, boolean includeSelf) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		List<Task> taskSiblings = null;
		Task t = dao.load(id);
		if (t != null) {
			if (t.getParentId() == null) {
				if (includeSelf) {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.isNull(),
									TaskDao.Properties.IsCompleted.eq(false))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				} else {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.isNull(),
									TaskDao.Properties.Id.notEq(t.getId()),
									TaskDao.Properties.IsCompleted.eq(false))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				}
			} else {
				if (includeSelf) {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.eq(t.getParentId()),
									TaskDao.Properties.IsCompleted.eq(false))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				} else {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.eq(t.getParentId()),
									TaskDao.Properties.Id.notEq(t.getId()),
									TaskDao.Properties.IsCompleted.eq(false))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				}
			}
		}
		return taskSiblings;
	}

	public static List<Task> getCompletedTaskSiblingsFromParent(DaoSession daoSessionArg,
			Context context, long id, boolean includeSelf) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		List<Task> taskSiblings = null;
		Task t = dao.load(id);
		if (t != null) {
			if (t.getParentId() == null) {
				if (includeSelf) {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.isNull(),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				} else {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.isNull(),
									TaskDao.Properties.Id.notEq(t.getId()),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				}
			} else {
				if (includeSelf) {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.eq(t.getParentId()),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				} else {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.eq(t.getParentId()),
									TaskDao.Properties.Id.notEq(t.getId()),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				}
			}
		}
		return taskSiblings;
	}

	public static List<Task> getCompletedTaskSiblings(DaoSession daoSessionArg,
			Context context, long id, boolean includeSelf) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		List<Task> taskSiblings = null;
		Task t = dao.load(id);
		if (t != null) {
			if (t.getParentId() == null) {
				if (includeSelf) {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.isNull(),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				} else {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.isNull(),
									TaskDao.Properties.Id.notEq(t.getId()),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				}
			} else {
				if (includeSelf) {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.eq(t.getParentId()),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				} else {
					taskSiblings = dao
							.queryBuilder()
							.where(TaskDao.Properties.ParentId.eq(t.getParentId()),
									TaskDao.Properties.Id.notEq(t.getId()),
									TaskDao.Properties.IsCompleted.eq(true))
							.orderAsc(TaskDao.Properties.SortOrder).list();
				}
			}
		}
		return taskSiblings;
	}

	public static List<Task> getTaskChildren(DaoSession daoSessionArg, Context context,
			Long id, final boolean includeNonDeleted, final boolean includeDeleted,
			final boolean includeActive, final boolean includeCompleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		StringBuilder sb;
		if (id == null) {
			sb = new StringBuilder(TaskDao.Properties.ParentId.columnName + " IS NULL");
		} else {
			sb = new StringBuilder(TaskDao.Properties.ParentId.columnName + " = " + id);
		}
		if (includeNonDeleted && includeDeleted) {
		} else if (includeNonDeleted && !includeDeleted) {
			sb.append(" AND " + TaskDao.Properties.Deleted.columnName + " = 0");
		} else if (!includeNonDeleted && includeDeleted) {
			sb.append(" AND " + TaskDao.Properties.Deleted.columnName + " = 1");
		} else if (!includeNonDeleted && !includeDeleted) {
			sb.append(" AND 1 <> 1");
		}
		if (includeActive && includeCompleted) {
		} else if (includeActive && !includeCompleted) {
			sb.append(" AND " + TaskDao.Properties.IsCompleted.columnName + " = 0");
		} else if (!includeActive && includeCompleted) {
			sb.append(" AND " + TaskDao.Properties.IsCompleted.columnName + " = 1");
		} else if (!includeActive && !includeCompleted) {
			sb.append(" AND 1 <> 1");
		}
		// QueryBuilder.LOG_SQL = true;
		// QueryBuilder.LOG_VALUES = true;
		Query<Task> query = dao.queryBuilder().where(new StringCondition(sb.toString()))
				.orderAsc(biz.advancedcalendar.greendao.TaskDao.Properties.SortOrder)
				.build();
		List<Task> taskChildren = query.listLazy();
		return taskChildren;
	}

	public static List<Label> getLabelChildren(DaoSession daoSessionArg, Context context,
			Long id, final boolean includeNonDeleted, final boolean includeDeleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		LabelDao dao = daoSession.getLabelDao();
		StringBuilder sb;
		if (id == null) {
			sb = new StringBuilder(LabelDao.Properties.LocalParentId.columnName
					+ " IS NULL");
		} else {
			sb = new StringBuilder(LabelDao.Properties.LocalParentId.columnName + " = "
					+ id);
		}
		if (includeNonDeleted && includeDeleted) {
		} else if (includeNonDeleted && !includeDeleted) {
			sb.append(" AND " + LabelDao.Properties.Deleted.columnName + " = 0");
		} else if (!includeNonDeleted && includeDeleted) {
			sb.append(" AND " + LabelDao.Properties.Deleted.columnName + " = 1");
		} else if (!includeNonDeleted && !includeDeleted) {
			sb.append(" AND 1 <> 1");
		}
		// QueryBuilder.LOG_SQL = true;
		// QueryBuilder.LOG_VALUES = true;
		Query<Label> query = dao.queryBuilder().where(new StringCondition(sb.toString()))
				.orderAsc(biz.advancedcalendar.greendao.LabelDao.Properties.SortOrder)
				.build();
		List<Label> taskChildren = query.listLazy();
		return taskChildren;
	}

	public static void insertOrReplaceReminder(DaoSession daoSessionArg, Context context,
			Reminder reminder) {
		if (reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
				.getValue() && reminder.getReminderDateTime() < 0) {
			// if (ReminderTimeMode.fromInt(reminder.getReminderTimeModeValue()).equals(
			// ReminderTimeMode.TIME_AFTER_EVENT)) {
			// reminder.setReminderTimeModeValue(ReminderTimeMode.TIME_BEFORE_EVENT
			// .getValue());
			// } else {
			// reminder.setReminderTimeModeValue(ReminderTimeMode.TIME_AFTER_EVENT
			// .getValue());
			// }
			// reminder.setReminderDateTime(-reminder.getReminderDateTime());
			throw new IllegalArgumentException(
					"reminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME.getValue() && reminder.getReminderDateTime() < 0");
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ReminderDao dao = daoSession.getReminderDao();
		dao.insertOrReplace(reminder);
	}

	public static void insertOrReplaceTaskOccurrence(DaoSession daoSessionArg,
			Context context, TaskOccurrence entity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskOccurrenceDao dao = daoSession.getTaskOccurrenceDao();
		dao.insertOrReplace(entity);
	}

	// public static void insertOrReplaceMonthlyRepetitionsByDate(Context context,
	// MonthlyRepetitionsByDate entity) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDateDao dao = daoSession.getMonthlyRepetitionsByDateDao();
	// dao.insertOrReplace(entity);
	// }
	// public static void insertOrReplaceMonthlyRepetitionsByDayOfWeek(Context context,
	// MonthlyRepetitionsByDayOfWeek reminder) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDayOfWeekDao dao = daoSession
	// .getMonthlyRepetitionsByDayOfWeekDao();
	// dao.insertOrReplace(reminder);
	// }
	public static void insertOrReplaceScheduledReminder(DaoSession daoSessionArg,
			final Context context, final ScheduledReminder scheduledReminder) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		if (scheduledReminder.getIsQuickReminder()
				&& scheduledReminder.getReminderId() != null) {
			throw new IllegalArgumentException(
					"Illegal state: scheduledReminder.getIsQuickReminder() && scheduledReminder.getReminderId() != null");
		}
		if (scheduledReminder.getSnoozeCount() > 0
				&& scheduledReminder.getActualLastAlarmedDateTime() == null) {
			throw new IllegalArgumentException(
					"Illegal state: scheduledReminder.getSnoozeCount() > 0 && scheduledReminder.getActualAlarmedDateTime() == null");
		}
		if (ScheduledReminder.State.fromInt(scheduledReminder.getStateValue()).equals(
				ScheduledReminder.State.ALARMED)
				&& scheduledReminder.getActualLastAlarmedDateTime() == null) {
			throw new IllegalArgumentException(
					"Illegal state: ScheduledReminder.STATE.fromInt(scheduledReminder.getState()).equals(ScheduledReminder.STATE.ALARMED) && scheduledReminder.getActualLastAlarmedDateTime() == null");
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				ScheduledReminderDao scheduledReminderDao = daoSession
						.getScheduledReminderDao();
				scheduledReminderDao.insertOrReplace(scheduledReminder);
				ElapsedReminder elapsedReminder = scheduledReminder
						.getElapsedReminder2(context);
				if (elapsedReminder != null) {
					elapsedReminder.setScheduledReminder(scheduledReminder);
				}
			}
		});
	}

	public static void insertOrReplaceElapsedReminder(DaoSession daoSessionArg,
			final Context context, final ElapsedReminder elapsedReminder) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				ElapsedReminderDao elapsedReminderDao = daoSession
						.getElapsedReminderDao();
				elapsedReminderDao.insertOrReplace(elapsedReminder);
				ScheduledReminder scheduledReminder = elapsedReminder
						.getScheduledReminder2(context);
				if (scheduledReminder != null) {
					scheduledReminder.setElapsedReminder(elapsedReminder);
				}
			}
		});
	}

	public static List<Reminder> getReminderListOfTask(DaoSession daoSessionArg,
			Context context, Long id, boolean includeEnabled, boolean includeNotEnabled) {
		ArrayList<Reminder> reminderArrayList = new ArrayList<Reminder>();
		if (id == null) {
			return reminderArrayList;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ReminderDao dao = daoSession.getReminderDao();
		StringBuilder sb = new StringBuilder(ReminderDao.Properties.TaskId.columnName
				+ " = " + id);
		if (includeEnabled && includeNotEnabled) {
		} else if (includeEnabled && !includeNotEnabled) {
			sb.append(" AND " + ReminderDao.Properties.Enabled.columnName + " = 1");
		} else if (!includeEnabled && includeNotEnabled) {
			sb.append(" AND " + ReminderDao.Properties.Enabled.columnName + " = 0");
		} else if (!includeEnabled && !includeNotEnabled) {
			sb.append(" AND 1 <> 1");
		}
		Query<Reminder> query = dao.queryBuilder()
				.where(new StringCondition(sb.toString()))
				.orderAsc(ReminderDao.Properties.ReminderDateTime).build();
		List<Reminder> entityList = query.list();
		return entityList;
	}

	public static List<Reminder> getReminderListOfTask(DaoSession daoSessionArg,
			Context context, Long taskId) {
		List<Reminder> reminders = new ArrayList<Reminder>();
		if (taskId == null) {
			return reminders;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ReminderDao dao = daoSession.getReminderDao();
		Query<Reminder> query = dao.queryBuilder()
				.where(ReminderDao.Properties.TaskId.eq(taskId))
				.orderAsc(ReminderDao.Properties.ReminderDateTime).build();
		reminders = query.list();
		return reminders;
	}

	private static List<TaskOccurrence> getTaskOccurrenceListOfTask(
			DaoSession daoSessionArg, Context context, Long id) {
		ArrayList<TaskOccurrence> taskOccurrences = new ArrayList<TaskOccurrence>();
		if (id == null) {
			return taskOccurrences;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskOccurrenceDao dao = daoSession.getTaskOccurrenceDao();
		Query<TaskOccurrence> query = dao.queryBuilder()
				.where(TaskOccurrenceDao.Properties.TaskId.eq(id)).build();
		List<TaskOccurrence> entityList = query.list();
		return entityList;
	}

	// public static List<MonthlyRepetitionsByDate>
	// getMonthlyRepetitionsByDateListForTask(
	// Context context, Long id) {
	// ArrayList<MonthlyRepetitionsByDate> entityList2 = new
	// ArrayList<MonthlyRepetitionsByDate>();
	// if (id == null) {
	// return entityList2;
	// }
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDateDao dao = daoSession.getMonthlyRepetitionsByDateDao();
	// Query<MonthlyRepetitionsByDate> query = dao.queryBuilder()
	// .where(MonthlyRepetitionsByDateDao.Properties.TaskId.eq(id)).build();
	// List<MonthlyRepetitionsByDate> entityList = query.list();
	// return entityList;
	// }
	// public static List<MonthlyRepetitionsByDayOfWeek>
	// getMonthlyRepetitionsByDayOfWeekListForTask(
	// Context context, Long id) {
	// ArrayList<MonthlyRepetitionsByDayOfWeek> monthlyRepetitionsByDayOfWeekList = new
	// ArrayList<MonthlyRepetitionsByDayOfWeek>();
	// if (id == null) {
	// return monthlyRepetitionsByDayOfWeekList;
	// }
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDayOfWeekDao dao = daoSession
	// .getMonthlyRepetitionsByDayOfWeekDao();
	// Query<MonthlyRepetitionsByDayOfWeek> query = dao.queryBuilder()
	// .where(MonthlyRepetitionsByDayOfWeekDao.Properties.TaskId.eq(id)).build();
	// List<MonthlyRepetitionsByDayOfWeek> entityList = query.list();
	// return entityList;
	// }
	public static List<Task> getTodoList(DaoSession daoSessionArg, Context context) {
		ArrayList<ArrayList<TreeViewListItemDescription>> forest = DataProvider
				.getTreeViewListItemDescriptionForest(daoSessionArg, context, null, null,
						true, false, true, false);
		List<Task> resultList = new ArrayList<Task>();
		for (List<TreeViewListItemDescription> tree : forest) {
			for (int i = 0; i < tree.size(); i++) {
				TreeViewListItemDescription currNode = tree.get(i);
				if (i == tree.size() - 1) {
					resultList.add(DataProvider.getTask(daoSessionArg, context,
							currNode.getId(), false));
				} else {
					TreeViewListItemDescription nextNode = tree.get(i + 1);
					if (nextNode.getDeepLevel() == currNode.getDeepLevel()) {
						resultList.add(DataProvider.getTask(daoSessionArg, context,
								currNode.getId(), false));
					}
				}
			}
		}
		return resultList;
	}

	private static void cancelAlarmsAndDeleteScheduledRemindersOfTask(
			DaoSession daoSessionArg, Context context, Long taskId) {
		List<Reminder> reminders = DataProvider.getReminderListOfTask(daoSessionArg,
				context, taskId);
		for (Reminder reminder : reminders) {
			List<ScheduledReminder> scheduledReminders = DataProvider
					.getScheduledReminders(daoSessionArg, context, reminder.getId(),
							new int[] {ScheduledReminder.State.SCHEDULED.getValue()});
			for (ScheduledReminder scheduledReminder : scheduledReminders) {
				AlarmService.cancelScheduledAlarm(context, scheduledReminder.getId());
				DataProvider.deleteScheduledReminder(daoSessionArg, context,
						scheduledReminder.getId());
			}
		}
	}

	private static void cancelAlarmsAndDeleteRemindersAndScheduledRemindersOfTask(
			DaoSession daoSessionArg, final Context context, final Long taskId) {
		List<Reminder> reminders = DataProvider.getReminderListOfTask(daoSessionArg,
				context, taskId);
		for (Reminder reminder : reminders) {
			List<ScheduledReminder> scheduledReminders = DataProvider
					.getScheduledReminders(daoSessionArg, context, reminder.getId(),
							new int[] {ScheduledReminder.State.SCHEDULED.getValue()});
			for (ScheduledReminder scheduledReminder : scheduledReminders) {
				AlarmService.cancelScheduledAlarm(context, scheduledReminder.getId());
				DataProvider.deleteScheduledReminder(daoSessionArg, context,
						scheduledReminder.getId());
			}
			DataProvider.deleteReminder(daoSessionArg, context, reminder.getId());
		}
	}

	public static void deleteTaskPermanently(DaoSession daoSessionArg,
			final Context context, final long id1, final boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = id1;
				if (isServerId) {
					id = DataProvider.getTaskId(daoSession, context, id1);
				}
				Task entity = DataProvider.getTask(daoSession, context, id, false);
				if (entity == null) {
					return;
				}
				long currentTime = System.currentTimeMillis();
				long adjustedTime = currentTime
						- Helper.getLongPreferenceValue(context,
								CommonConstants.TIME_SKEW, 0, null, null);
				List<Task> tasks = DataProvider.getTaskChildren(daoSession, context,
						entity.getId(), true, true, true, true);
				for (int j = 0; j < tasks.size(); j++) {
					entity = tasks.get(j);
					entity.setParentId(null);
					entity.setLocalChangeDateTime(currentTime);
					entity.setLastMod(adjustedTime);
					DataProvider.insertOrReplaceTask(daoSession, context, entity, true);
				}
				TaskDao dao = daoSession.getTaskDao();
				dao.delete(entity);
				// cascade delete repetitions
				final TaskOccurrenceDao taskOccurrenceDao = daoSession
						.getTaskOccurrenceDao();
				taskOccurrenceDao.queryBuilder()
						.where(TaskOccurrenceDao.Properties.TaskId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from labels
				final TaskLabelDao taskLabelDao = daoSession.getTaskLabelDao();
				taskLabelDao.queryBuilder()
						.where(TaskLabelDao.Properties.TaskId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from contacts
				final TaskContactDao taskContactDao = daoSession.getTaskContactDao();
				taskContactDao.queryBuilder()
						.where(TaskContactDao.Properties.TaskId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from files
				final FileTaskDao fileTaskDao = daoSession.getFileTaskDao();
				fileTaskDao.queryBuilder()
						.where(FileTaskDao.Properties.TaskId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade mark as deleted in diary records
				final DiaryRecordDao diaryRecordDao = daoSession.getDiaryRecordDao();
				List<DiaryRecord> diaryRecordList = diaryRecordDao.queryBuilder()
						.where(DiaryRecordDao.Properties.LocalTaskId.eq(entity.getId()))
						.list();
				for (DiaryRecord diaryRecord : diaryRecordList) {
					diaryRecord.setTaskDeleted(true);
					diaryRecord.update();
				}
				// cascade delete reminders
				DataProvider.cancelAlarmsAndDeleteRemindersAndScheduledRemindersOfTask(
						daoSession, context, entity.getId());
			}
		});
	}

	public static void deleteLabel(DaoSession daoSessionArg, final Context context,
			final long id1, final boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getLabelId(daoSession, context, id1);
				}
				Label entity = DataProvider.getLabel(daoSession, context, id, false);
				if (entity == null) {
					return;
				}
				ArrayList<Long> rootList = new ArrayList<Long>(1);
				rootList.add(entity.getId());
				ArrayList<ArrayList<TreeViewListItemDescription>> treeViewListItemDescriptionList2 = DataProvider
						.getLabelForest(daoSession, context, rootList, null, true, true);
				final LabelDao labelDao = daoSession.getLabelDao();
				for (List<TreeViewListItemDescription> treeViewListItemDescriptionList3 : treeViewListItemDescriptionList2) {
					for (TreeViewListItemDescription treeViewListItemDescription : treeViewListItemDescriptionList3) {
						labelDao.queryBuilder()
								.where(LabelDao.Properties.Id
										.eq(treeViewListItemDescription.getId()))
								.buildDelete().executeDeleteWithoutDetachingEntities();
						// cascade delete from tasks
						final TaskLabelDao taskLabelDao = daoSession.getTaskLabelDao();
						taskLabelDao
								.queryBuilder()
								.where(TaskLabelDao.Properties.LabelId
										.eq(treeViewListItemDescription.getId()))
								.buildDelete().executeDeleteWithoutDetachingEntities();
						// cascade delete from contacts
						final LabelContactDao labelContactDao = daoSession
								.getLabelContactDao();
						labelContactDao
								.queryBuilder()
								.where(LabelContactDao.Properties.LabelId
										.eq(treeViewListItemDescription.getId()))
								.buildDelete().executeDeleteWithoutDetachingEntities();
						// cascade delete from files
						final FileLabelDao fileLabelDao = daoSession.getFileLabelDao();
						fileLabelDao
								.queryBuilder()
								.where(FileLabelDao.Properties.LabelId
										.eq(treeViewListItemDescription.getId()))
								.buildDelete().executeDeleteWithoutDetachingEntities();
					}
				}
				// even if we didn't have the entity in the database
				// nevertheless delete
				// its traces from the related tables
				// cascade delete from tasks
				final TaskLabelDao taskLabelDao = daoSession.getTaskLabelDao();
				taskLabelDao.queryBuilder()
						.where(TaskLabelDao.Properties.LabelId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from contacts
				final LabelContactDao labelContactDao = daoSession.getLabelContactDao();
				labelContactDao.queryBuilder()
						.where(LabelContactDao.Properties.LabelId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from files
				final FileLabelDao fileLabelDao = daoSession.getFileLabelDao();
				fileLabelDao.queryBuilder()
						.where(FileLabelDao.Properties.LabelId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from diary records
				final DiaryRecordLabelDao diaryRecordLabelDao = daoSession
						.getDiaryRecordLabelDao();
				diaryRecordLabelDao.queryBuilder()
						.where(DiaryRecordLabelDao.Properties.LabelId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// delete the entity itself
				// entity.delete();
			}
		});
	}

	private static void deleteContact(DaoSession daoSessionArg, final Context context,
			final Long id1, final boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getContactId(daoSession, context, id1);
				}
				Contact entity = DataProvider.getContact(daoSession, context, id, false);
				if (entity == null) {
					return;
				}
				// even if we don't have the entity in the database
				// nevertheless delete
				// its traces from the related tables
				// cascade delete from labels
				final LabelContactDao labelContactDao = daoSession.getLabelContactDao();
				labelContactDao.queryBuilder()
						.where(LabelContactDao.Properties.ContactId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from contacts
				final TaskContactDao taskContactDao = daoSession.getTaskContactDao();
				taskContactDao.queryBuilder()
						.where(TaskContactDao.Properties.ContactId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// cascade delete from files
				final FileContactDao fileTaskDao = daoSession.getFileContactDao();
				fileTaskDao.queryBuilder()
						.where(FileContactDao.Properties.ContactId.eq(entity.getId()))
						.buildDelete().executeDeleteWithoutDetachingEntities();
				// delete the entity itself
				// entity.delete();
				ContactDao contactDao = daoSession.getContactDao();
				contactDao.delete(entity);
			}
		});
	}

	public static List<Long> getSyncUpTaskIdList(DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final TaskDao dao = daoSession.getTaskDao();
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		QueryBuilder<Task> qb = dao.queryBuilder().where(
				new StringCondition(TaskDao.Properties.SyncStatusValue.columnName + " = "
						+ SyncStatus.SYNC_UP_REQUIRED.getValue() + " OR "
						+ TaskDao.Properties.Deleted.columnName + " = 1" + " OR "
						+ TaskDao.Properties.ServerId.columnName + " IS NULL"));
		List<Task> entityList = qb.list();
		for (Task entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static List<Long> getSyncUpLabelIdList(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final LabelDao dao = daoSession.getLabelDao();
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		QueryBuilder<Label> qb = dao.queryBuilder().where(
				new StringCondition(LabelDao.Properties.SyncStatus.columnName + " = "
						+ SyncStatus.SYNC_UP_REQUIRED.getValue() + " OR "
						+ LabelDao.Properties.Deleted.columnName + " = 1" + " OR "
						+ LabelDao.Properties.ServerId.columnName + " IS NULL"));
		List<Label> entityList = qb.list();
		for (Label entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static List<Long> getSyncUpContactIdList(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ContactDao dao = daoSession.getContactDao();
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		QueryBuilder<Contact> qb = dao.queryBuilder().where(
				new StringCondition(ContactDao.Properties.SyncStatus.columnName + " = "
						+ SyncStatus.SYNC_UP_REQUIRED.getValue() + " OR "
						+ ContactDao.Properties.Deleted.columnName + " = 1" + " OR "
						+ ContactDao.Properties.ServerId.columnName + " IS NULL"));
		List<Contact> entityList = qb.list();
		for (Contact entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static Task getSortOrderSibling(DaoSession daoSessionArg, Context context,
			int sortOrder, Long parentId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		if (parentId == null) {
			return daoSession
					.getTaskDao()
					.queryBuilder()
					.where(TaskDao.Properties.ParentId.isNull(),
							TaskDao.Properties.Deleted.eq(0),
							TaskDao.Properties.SortOrder.lt(sortOrder))
					.orderDesc(TaskDao.Properties.SortOrder).limit(1).build().unique();
		} else {
			return daoSession
					.getTaskDao()
					.queryBuilder()
					.where(TaskDao.Properties.ParentId.eq(parentId),
							TaskDao.Properties.Deleted.eq(0),
							TaskDao.Properties.SortOrder.lt(sortOrder))
					.orderDesc(TaskDao.Properties.SortOrder).limit(1).build().unique();
		}
	}

	public static Task getSortOrderSiblingLast2(DaoSession daoSessionArg,
			Context context, Long id, Long parentId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		if (parentId == null) {
			return daoSession
					.getTaskDao()
					.queryBuilder()
					.where(TaskDao.Properties.ParentId.isNull(),
							TaskDao.Properties.Deleted.eq(0))
					.orderDesc(TaskDao.Properties.SortOrder).limit(1).build().unique();
		} else {
			if (id != null) {
				return daoSession
						.getTaskDao()
						.queryBuilder()
						.where(TaskDao.Properties.Id.notEq(id),
								TaskDao.Properties.ParentId.eq(parentId),
								TaskDao.Properties.Deleted.eq(0))
						.orderDesc(TaskDao.Properties.SortOrder).limit(1).build()
						.unique();
			} else {
				return daoSession
						.getTaskDao()
						.queryBuilder()
						.where(TaskDao.Properties.ParentId.eq(parentId),
								TaskDao.Properties.Deleted.eq(0))
						.orderDesc(TaskDao.Properties.SortOrder).limit(1).build()
						.unique();
			}
		}
	}

	public static Task getSortOrderSiblingLast(DaoSession daoSessionArg, Context context,
			Long parentId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		if (parentId == null) {
			return daoSession
					.getTaskDao()
					.queryBuilder()
					.where(TaskDao.Properties.ParentId.isNull(),
							TaskDao.Properties.Deleted.eq(0))
					.orderDesc(TaskDao.Properties.SortOrder).limit(1).build().unique();
		} else {
			return daoSession
					.getTaskDao()
					.queryBuilder()
					.where(TaskDao.Properties.ParentId.eq(parentId),
							TaskDao.Properties.Deleted.eq(0))
					.orderDesc(TaskDao.Properties.SortOrder).limit(1).build().unique();
		}
	}

	public static void markTasksAsDeleted(DaoSession daoSessionArg,
			final Context context, final long[] idArray,
			final boolean markSubtreeAsDeleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				long currentTime = System.currentTimeMillis();
				long skewAdjustedTime = currentTime
						- Helper.getLongPreferenceValue(context,
								CommonConstants.TIME_SKEW, 0, null, null);
				TaskDao dao = daoSession.getTaskDao();
				Task task;
				for (int i = 0; i < idArray.length; i++) {
					task = dao.load(idArray[i]);
					if (task != null) {
						if (!markSubtreeAsDeleted) {
							List<Task> tasks = DataProvider.getTaskChildren(daoSession,
									context, idArray[i], true, true, true, true);
							for (int j = 0; j < tasks.size(); j++) {
								Task task1;
								task1 = tasks.get(j);
								task1.setParentId(task.getParentId());
								task1.setLocalChangeDateTime(currentTime);
								task1.setLastMod(skewAdjustedTime);
								dao.update(task1);
								DataProvider
										.cancelAlarmsAndDeleteScheduledRemindersOfTask(
												daoSession, context, task1.getId());
							}
						}
						task.setDeleted(true);
						task.setLocalChangeDateTime(currentTime);
						task.setLastMod(skewAdjustedTime);
						dao.update(task);
						DataProvider.cancelAlarmsAndDeleteScheduledRemindersOfTask(
								daoSession, context, task.getId());
					}
				}
			}
		});
	}

	public static void markLabelsAsDeleted(DaoSession daoSessionArg,
			final Context context, final long[] idArray) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				LabelDao dao = daoSession.getLabelDao();
				List<Long> rootList = new ArrayList<Long>();
				for (int i = 0; i < idArray.length; i++) {
					rootList.add(idArray[i]);
				}
				ArrayList<ArrayList<TreeViewListItemDescription>> forest = DataProvider
						.getTreeViewListItemDescriptionForest(daoSession, context,
								rootList, null, true, true, true, true);
				for (ArrayList<TreeViewListItemDescription> tree : forest) {
					for (TreeViewListItemDescription treeViewListItemDescription : tree) {
						Label entity = dao.load(treeViewListItemDescription.getId());
						entity.setDeleted(true);
						long nowTime = System.currentTimeMillis();
						entity.setLocalChangeDateTime(nowTime);
						Long timeSkew = Helper.getLongPreferenceValue(context,
								CommonConstants.TIME_SKEW, 0, null, null);
						entity.setLastMod(nowTime - timeSkew);
						// entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
						DataProvider.insertOrReplaceLabel(daoSession, context, entity);
					}
				}
			}
		});
	}

	public static void markContactsAsDeleted(DaoSession daoSessionArg,
			final Context context, final long[] idArray) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				ContactDao dao = daoSession.getContactDao();
				for (int i = 0; i < idArray.length; i++) {
					long id = idArray[i];
					Contact entity = dao.load(id);
					entity.setDeleted(true);
					long nowTime = System.currentTimeMillis();
					entity.setLocalChangeDateTime(nowTime);
					Long timeSkew = Helper.getLongPreferenceValue(context,
							CommonConstants.TIME_SKEW, 0, null, null);
					entity.setLastMod(nowTime - timeSkew);
					DataProvider.insertOrReplaceContact(daoSession, context, entity);
				}
			}
		});
	}

	public static TreeViewListItemDescriptionMatrix getTreeViewListItemDescriptionMatrixForLabel(
			DaoSession daoSessionArg, final Context context, final List<Long> rootList,
			final Long excludeSubtree, final boolean includeNonDeleted,
			final boolean includeDeleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows = new ArrayList<TreeViewListItemDescriptionRow>();
		final TreeViewListItemDescriptionMatrix treeViewListItemDescriptionMatrix = new TreeViewListItemDescriptionMatrix(
				treeViewListItemDescriptionRows);
		// check arguments
		if (rootList != null && rootList.size() == 0) {
			return treeViewListItemDescriptionMatrix;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				if (rootList == null) {
					final List<Label> taskList = DataProvider.getLabelChildren(
							daoSession, context, null, includeNonDeleted, includeDeleted);
					for (Label task : taskList) {
						localTempRootList.add(task.getId());
					}
				} else {
					for (Long id : rootList) {
						localTempRootList.add(id);
					}
				}
				for (long id : localTempRootList) {
					ArrayList<TreeViewListItemDescription> labelTree = DataProvider
							.getLabelTree(daoSession, context, id, includeNonDeleted,
									includeDeleted);
					TreeViewListItemDescriptionRow treeViewListItemDescriptionRow = new TreeViewListItemDescriptionRow(
							labelTree);
					treeViewListItemDescriptionRows.add(treeViewListItemDescriptionRow);
				}
			}
		});
		return treeViewListItemDescriptionMatrix;
	}

	public static ArrayList<ArrayList<TreeViewListItemDescription>> getLabelForest(
			DaoSession daoSessionArg, final Context context, final List<Long> rootList,
			final Long excludeSubtree, final boolean includeNonDeleted,
			final boolean includeDeleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ArrayList<ArrayList<TreeViewListItemDescription>> forest = new ArrayList<ArrayList<TreeViewListItemDescription>>();
		// check arguments
		if (rootList != null && rootList.size() == 0) {
			return forest;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Long> localTempRootList = new ArrayList<Long>();
				if (rootList == null) {
					final List<Label> taskList = DataProvider.getLabelChildren(
							daoSession, context, null, includeNonDeleted, includeDeleted);
					for (Label task : taskList) {
						localTempRootList.add(task.getId());
					}
				} else {
					for (Long id : rootList) {
						localTempRootList.add(id);
					}
				}
				for (long id : localTempRootList) {
					forest.add(DataProvider.getLabelTree(daoSession, context, id,
							includeNonDeleted, includeDeleted));
				}
			}
		});
		return forest;
	}

	// public static ArrayList<ArrayList<TreeViewListItemDescription>> getLabelForest2(
	// final Context context, final List<Long> rootList, final Long excludeSubtree,
	// final boolean isServerId, final boolean includeNonDeleted,
	// final boolean includeDeleted) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// final ArrayList<ArrayList<TreeViewListItemDescription>> forest = new
	// ArrayList<ArrayList<TreeViewListItemDescription>>();
	// if (rootList != null && rootList.size() == 0) {
	// return forest;
	// }
	// daoSession.runInTx(new Runnable() {
	// @Override
	// public void run() {
	// LabelDao dao = daoSession.getLabelDao();
	// StringBuilder sb = new StringBuilder("1 = 1");
	// if (includeNonDeleted && includeDeleted) {
	// } else if (includeNonDeleted && !includeDeleted) {
	// sb.append(" AND " + LabelDao.Properties.Deleted.columnName + " = 0");
	// } else if (!includeNonDeleted && includeDeleted) {
	// sb.append(" AND " + LabelDao.Properties.Deleted.columnName + " = 1");
	// } else if (!includeNonDeleted && !includeDeleted) {
	// sb.append(" AND 1 <> 1");
	// }
	// // QueryBuilder.LOG_SQL = true;
	// // QueryBuilder.LOG_VALUES = true;
	// Query<Label> query = dao
	// .queryBuilder()
	// .where(new StringCondition(sb.toString()))
	// /* .orderAsc( biz.advancedcalendar.greendao.LabelDao
	// * .Properties.DeepLevel) */
	// .orderAsc(
	// biz.advancedcalendar.greendao.LabelDao.Properties.LocalParentId)
	// .orderDesc(
	// biz.advancedcalendar.greendao.LabelDao.Properties.SortOrder)
	// .build();
	// List<Label> entityList = query.list();
	// short deepLevelSubtractor = 0;
	// for (Label entity : entityList) {
	// TreeViewListItemDescription item = null;
	// // new TreeViewListItemDescriptionImpl(
	// // (Parcelable) entity, entity.getId(), entity
	// // .getLocalParentId(), (short) (entity
	// // .getDeepLevel(daoSession) - deepLevelSubtractor),
	// // entity.getText(), entity.getSortOrder());
	// if (rootList == null) {
	// int index = -1;
	// for (List<TreeViewListItemDescription> tree : forest) {
	// index = -1;
	// for (int i = 0; i < tree.size(); i++) {
	// if (tree.get(i).getId() == item.getParentId()) {
	// index = i;
	// break;
	// }
	// }
	// // parent for an item was found so add it as child
	// if (index != -1) {
	// tree.add(index + 1, item);
	// break;
	// }
	// }
	// // parent for an item was not found so add it as new
	// // root
	// if (index == -1) {
	// forest.add(0, new ArrayList<TreeViewListItemDescription>());
	// forest.get(0).add(item);
	// }
	// } else if (rootList.contains(entity.getId())) {
	// forest.add(new ArrayList<TreeViewListItemDescription>());
	// forest.get(forest.size() - 1).add(item);
	// } else {
	// for (List<TreeViewListItemDescription> tree : forest) {
	// int index = -1;
	// for (int i = 0; i < tree.size(); i++) {
	// if (tree.get(i).getId() == item.getParentId()) {
	// index = i;
	// break;
	// }
	// }
	// if (index != -1) {
	// tree.add(index + 1, item);
	// break;
	// }
	// }
	// }
	// }
	// if (excludeSubtree != null) {
	// for (List<TreeViewListItemDescription> tree : forest) {
	// int index = -1;
	// for (int i = 0; i < tree.size(); i++) {
	// if (tree.get(i).getId() == excludeSubtree) {
	// index = i;
	// break;
	// }
	// }
	// if (index != -1) {
	// for (int i = tree.size() - 1; i >= index; i--) {
	// tree.remove(i);
	// }
	// break;
	// }
	// }
	// }
	// }
	// });
	// return forest;
	// }
	public static void markTasksAsCompleted(DaoSession daoSessionArg,
			final Context context, final long[] idArray, final boolean isCompleted) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				try {
					TaskDao dao = daoSession.getTaskDao();
					List<Task> taskList = new ArrayList<Task>();
					if (isCompleted) {
						List<Long> rootList = new ArrayList<Long>();
						for (int i = 0; i < idArray.length; i++) {
							rootList.add(idArray[i]);
						}
						ArrayList<ArrayList<TreeViewListItemDescription>> forest = DataProvider
								.getTreeViewListItemDescriptionForest(daoSession,
										context, rootList, null, true, true, true, true);
						for (ArrayList<TreeViewListItemDescription> tree : forest) {
							boolean beginning = true;
							for (TreeViewListItemDescription treeViewListItemDescription : tree) {
								Task entity = dao.load(treeViewListItemDescription
										.getId());
								if (!entity.getIsCompleted()) {
									entity.setIsCompleted(true);
									long nowTime = System.currentTimeMillis();
									entity.setLocalChangeDateTime(nowTime);
									long timeSkew = Helper.getLongPreferenceValue(
											context, CommonConstants.TIME_SKEW, 0, null,
											null);
									entity.setLastMod(nowTime - timeSkew);
								}
								if (beginning) {
									entity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
											.getValue());
									beginning = false;
								}
								taskList.add(entity);
							}
						}
					} else {
						for (int i = 0; i < idArray.length; i++) {
							long id = idArray[i];
							Long nextId = id;
							boolean beginning = true;
							while (nextId != null) {
								Task entity = dao.load(nextId);
								if (!entity.getIsCompleted()) {
									break;
								}
								entity.setIsCompleted(false);
								long nowTime = System.currentTimeMillis();
								entity.setLocalChangeDateTime(nowTime);
								long timeSkew = Helper.getLongPreferenceValue(context,
										CommonConstants.TIME_SKEW, 0, null, null);
								entity.setLastMod(nowTime - timeSkew);
								if (beginning) {
									entity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
											.getValue());
									beginning = false;
								}
								taskList.add(entity);
								nextId = entity.getParentId();
							}
						}
					}
					dao.updateInTx(taskList);
				} finally {
					long nowTime = System.currentTimeMillis();
					boolean scheduleRemindersOfCompletedTasks = CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
							.getIntegerPreferenceValueFromStringArray(
									context,
									R.string.preference_key_reminder_behavior_for_completed_task,
									R.array.reminder_behavior_for_completed_task_values_array,
									R.integer.reminder_default_behavior_for_completed_task);
					for (long id : idArray) {
						AlarmService.resetupRemindersOfTask(context, id,
								scheduleRemindersOfCompletedTasks, nowTime);
					}
				}
			}
		});
	}

	public static List<Long> getSyncUpFileIdList(DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final FileDao dao = daoSession.getFileDao();
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		QueryBuilder<biz.advancedcalendar.greendao.File> qb = dao.queryBuilder().where(
				new StringCondition(FileDao.Properties.SyncStatus.columnName + " = "
						+ SyncStatus.SYNC_UP_REQUIRED.getValue() + " OR "
						+ FileDao.Properties.Deleted.columnName + " = 1" + " OR "
						+ FileDao.Properties.ServerId.columnName + " IS NULL"));
		List<biz.advancedcalendar.greendao.File> entityList = qb.list();
		for (biz.advancedcalendar.greendao.File entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static void deleteFile(DaoSession daoSessionArg, final Context context,
			final Long id, final boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final FileDao dao = daoSession.getFileDao();
				dao.queryBuilder()
						.where((isServerId ? FileDao.Properties.ServerId
								: FileDao.Properties.Id).eq(id)).buildDelete()
						.executeDeleteWithoutDetachingEntities();
				// even if we didn't have the entity in the database
				// nevertheless delete
				// its traces from the related tables
				// cascade delete from labels
				// daoSession
				// .getFileLabelDao()
				// .queryBuilder()
				// .where(isServerId ? FileLabelDao.Properties.ServerFileId.eq(id)
				// : FileLabelDao.Properties.FileId.eq(id)).buildDelete()
				// .executeDeleteWithoutDetachingEntities();
				// cascade delete from tasks
				// daoSession
				// .getFileTaskDao()
				// .queryBuilder()
				// .where(isServerId ? FileTaskDao.Properties.ServerFileId.eq(id)
				// : FileTaskDao.Properties.FileId.eq(id)).buildDelete()
				// .executeDeleteWithoutDetachingEntities();
				// cascade delete from contacts
				// daoSession
				// .getFileContactDao()
				// .queryBuilder()
				// .where(isServerId ? FileContactDao.Properties.ServerFileId.eq(id)
				// : FileContactDao.Properties.FileId.eq(id)).buildDelete()
				// .executeDeleteWithoutDetachingEntities();
			}
		});
	}

	public static void insertOrReplaceFile(DaoSession daoSessionArg,
			final Context context, final biz.advancedcalendar.greendao.File newEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				FileDao dao = daoSession.getFileDao();
				if (newEntity.getId() != null) {
					dao.detach(newEntity);
				}
				// verify arguments
				if (newEntity.getServerId() == 0
						&& newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED
								.getValue()) {
					throw new IllegalArgumentException(
							"Illegal state: newEntity.getServerId() == 0 && newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED.getValue()");
				}
				if (newEntity.getUserName() == null) {
					throw new IllegalArgumentException(
							"Illegal argument in insertFile(): UserName ==  null");
				}
				if (newEntity.getUserName().length() == 0) {
					throw new IllegalArgumentException(
							"Illegal argument in insertFile(): UserName is empty");
				}
				if (newEntity.getLocalVersionId() != null) {
					throw new IllegalArgumentException(
							"Illegal argument in insertFile(): newEntity.getLocalVersionId() != null");
				}
				if (newEntity.getVersionId() != null) {
					throw new IllegalArgumentException(
							"Illegal argument in insertFile(): newEntity.getVersionId() != null");
				}
				// newEntity.getId() == null
				if (newEntity.getId() == null) {
					// newEntity.getId() == null
					// newEntity.getServerId() == null
					if (newEntity.getServerId() == 0) {
						// nothing to do
					}
					// newEntity.getId() == null
					// newEntity.getServerId() != null
					else {
						biz.advancedcalendar.greendao.File localEntityByServerId = dao
								.queryBuilder()
								.where(FileDao.Properties.ServerId.eq(newEntity
										.getServerId()),
										FileDao.Properties.VersionId.isNull()).unique();
						if (localEntityByServerId != null) {
							newEntity.setId(localEntityByServerId.getId());
							newEntity.setLocalId(localEntityByServerId.getLocalId());
						}
					}
				}
				// newEntity.getId() != null
				else {
					biz.advancedcalendar.greendao.File localEntityById = dao
							.queryBuilder()
							.where(FileDao.Properties.Id.eq(newEntity.getId())).unique();
					dao.refresh(localEntityById);
					// check for consistency
					if (localEntityById == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested file with id " + newEntity.getId()
										+ " does not exist.");
					}
					if (localEntityById.getLocalVersionId() != null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested entity with id "
										+ newEntity.getId()
										+ " refers to the file history entity instead of file entity.");
					}
					newEntity.setLocalId(localEntityById.getLocalId());
					// newEntity.getId() != null
					// newEntity.getServerId() == null
					if (newEntity.getServerId() == 0) {
						// nothing to do
					}
					// newEntity.getId() != null
					// newEntity.getServerId() != null
					else {
						biz.advancedcalendar.greendao.File localEntityByServerId = dao
								.queryBuilder()
								.where(FileDao.Properties.ServerId.eq(newEntity
										.getServerId()),
										FileDao.Properties.VersionId.isNull()).unique();
						dao.refresh(localEntityByServerId);
						if (localEntityByServerId != null
								&& localEntityById.getId() != localEntityByServerId
										.getId()) {
							throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
									"The Id and ServerId do not refer to the same entity");
						}
					}
				}
				if (newEntity.getLocalId() == null) {
					newEntity.setLocalId(DataProvider.getNewLocalIdForFile(daoSession,
							context));
				}
				// adjust sort order with siblings
				// TODO: currently setting sort order via web-service is not
				// supported. Sort order is set by server. So do not adjust sort
				// order with siblings
				dao.insertOrReplace(newEntity);
			}
		});
	}

	public static void insertFileOrReplaceHistory(DaoSession daoSessionArg,
			final Context context, final biz.advancedcalendar.greendao.File newEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				FileDao dao = daoSession.getFileDao();
				if (newEntity.getId() != null) {
					dao.detach(newEntity);
				}
				// verify arguments
				if (newEntity.getUserName() == null) {
					throw new IllegalArgumentException(
							"Illegal argument in insertFileOrReplaceHistory(): UserName ==  null");
				}
				if (newEntity.getUserName().length() == 0) {
					throw new IllegalArgumentException(
							"Illegal argument in insertFileOrReplaceHistory(): UserName is empty");
				}
				if (newEntity.getServerId() == 0 && newEntity.getVersionId() != null) {
					throw new IllegalArgumentException(
							"Illegal arguments in insertFileOrReplaceHistory(): ServerId == null and VersionId != null");
				}
				if (newEntity.getServerId() != 0 && newEntity.getVersionId() == null) {
					throw new IllegalArgumentException(
							"Illegal arguments in insertFileOrReplaceHistory(): ServerId != null and VersionId == null");
				}
				// newEntity.getId() == null
				if (newEntity.getId() == null) {
					// newEntity.getId() == null
					// newEntity.getServerId() == null
					if (newEntity.getServerId() == 0) {
						// nothing to do
					}
					// newEntity.getId() == null
					// newEntity.getServerId() != null
					else {
						biz.advancedcalendar.greendao.File localEntityByServerId = dao
								.queryBuilder()
								.where(FileDao.Properties.ServerId.eq(newEntity
										.getServerId()),
										FileDao.Properties.VersionId.eq(newEntity
												.getVersionId())).unique();
						if (localEntityByServerId != null) {
							newEntity.setId(localEntityByServerId.getId());
							newEntity.setLocalId(localEntityByServerId.getLocalId());
							newEntity.setLocalVersionId(localEntityByServerId
									.getLocalVersionId());
						}
					}
				}
				// newEntity.getId() != null
				else {
					biz.advancedcalendar.greendao.File localEntityById = dao
							.queryBuilder()
							.where(FileDao.Properties.Id.eq(newEntity.getId())).unique();
					// check for consistency
					if (localEntityById == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested file history entity with id "
										+ newEntity.getId() + " does not exist.");
					}
					if (localEntityById.getLocalVersionId() == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"The requested entity with id "
										+ newEntity.getId()
										+ " refers to the file entity instead of file history entity.");
					}
					newEntity.setLocalId(localEntityById.getLocalId());
					newEntity.setLocalVersionId(localEntityById.getLocalVersionId());
					// newEntity.getId() != null
					// newEntity.getServerId() == null
					if (newEntity.getServerId() == 0) {
						// nothing to do
					}
					// newEntity.getId() != null
					// newEntity.getServerId() != null
					else {
						biz.advancedcalendar.greendao.File localEntityByServerId = dao
								.queryBuilder()
								.where(FileDao.Properties.ServerId.eq(newEntity
										.getServerId()),
										FileDao.Properties.VersionId.isNull()).unique();
						if (localEntityByServerId != null
								&& localEntityById.getId() != localEntityByServerId
										.getId()) {
							throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
									"The Id and ServerId do not refer to the same entity");
						}
					}
				}
				if (newEntity.getLocalId() == null) {
					newEntity.setLocalId(DataProvider.getNewLocalIdForFile(daoSession,
							context));
					newEntity.setLocalVersionId(DataProvider.getNewLocalVersionIdForFile(
							daoSession, context));
				}
				// adjust sort order with siblings
				// TODO: currently setting sort order via web-service is not
				// supported. Sort order is set by server. So do not adjust sort
				// order with siblings
				dao.insertOrReplace(newEntity);
			}
		});
	}

	public static void updateFileHistory(DaoSession daoSessionArg, final Context context,
			final biz.advancedcalendar.greendao.File newEntity)
			throws DatabaseStateAndNewEntityArgumentsInconsistencyException {
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				FileDao dao = daoSession.getFileDao();
				// verify arguments
				if (newEntity.getUserName() == null) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): UserName ==  null");
				}
				if (newEntity.getUserName().length() == 0) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): UserName is empty");
				}
				if (newEntity.getId() == null) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): newEntity.getId() == null");
				}
				if (newEntity.getLocalId() != null) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): newEntity.getLocalId() != null");
				}
				if (newEntity.getLocalVersionId() != null) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): newEntity.getLocalVersionId() != null");
				}
				if (newEntity.getServerId() != 0) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): newEntity.getServerId() != 0");
				}
				if (newEntity.getVersionId() != null) {
					throw new IllegalArgumentException(
							"Illegal argument in updateFileHistory(): newEntity.getVersionId() != null");
				}
				// verify consistency
				dao.detach(newEntity);
				biz.advancedcalendar.greendao.File file = dao.load(newEntity.getId());
				if (file == null) {
					throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
							"Inconsistensy in updateFileHistory(): "
									+ "The entity with Id == " + newEntity.getId()
									+ " does not exist");
				}
				file.refresh();
				newEntity.setLocalId(file.getLocalId());
				newEntity.setLocalVersionId(file.getLocalVersionId());
				newEntity.setServerId(file.getServerId());
				newEntity.setVersionId(file.getVersionId());
				// adjust sort order with siblings
				// TODO: currently setting sort order via web-service is not
				// supported. Sort order is set by server. So do not adjust sort
				// order with siblings
				dao.insertOrReplace(newEntity);
			}
		});
	}

	public static biz.advancedcalendar.greendao.File getFileByLocalId(
			DaoSession daoSessionArg, Context context, Long localId, Long localVersionId) {
		if (localId == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		FileDao dao = daoSession.getFileDao();
		QueryBuilder<biz.advancedcalendar.greendao.File> qb = dao.queryBuilder();
		biz.advancedcalendar.greendao.File entity;
		if (localVersionId == null) {
			entity = qb.where(FileDao.Properties.LocalId.isNull()).unique();
		} else {
			entity = qb.where(FileDao.Properties.LocalId.eq(localVersionId)).unique();
		}
		return entity;
	}

	public static List<Long> getReminderIdListForTask(DaoSession daoSessionArg,
			Context context, Long id, boolean includeEnabled, boolean includeNotEnabled) {
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id == null) {
			return selectedIds;
		}
		List<Reminder> entityList = DataProvider.getReminderListOfTask(daoSessionArg,
				context, id, includeEnabled, includeNotEnabled);
		for (Reminder entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static List<Long> getTaskOccurrenceIdListForTask(DaoSession daoSessionArg,
			Context context, Long id) {
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id == null) {
			return selectedIds;
		}
		List<TaskOccurrence> taskOccurrenceList = DataProvider
				.getTaskOccurrenceListOfTask(daoSessionArg, context, id);
		for (TaskOccurrence entity : taskOccurrenceList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	// public static List<Long> getMonthlyRepetitionsByDateIdListForTask(Context context,
	// Long id) {
	// ArrayList<Long> selectedIds = new ArrayList<Long>();
	// if (id == null) {
	// return selectedIds;
	// }
	// List<MonthlyRepetitionsByDate> entityList = DataProvider
	// .getMonthlyRepetitionsByDateListForTask(context, id);
	// for (MonthlyRepetitionsByDate entity : entityList) {
	// selectedIds.add(entity.getId());
	// }
	// return selectedIds;
	// }
	// public static List<Long> getMonthlyRepetitionsByDayOfWeekIdListForTask(
	// Context context, Long id) {
	// ArrayList<Long> selectedIds = new ArrayList<Long>();
	// if (id == null) {
	// return selectedIds;
	// }
	// List<MonthlyRepetitionsByDayOfWeek> entityList = DataProvider
	// .getMonthlyRepetitionsByDayOfWeekListForTask(context, id);
	// for (MonthlyRepetitionsByDayOfWeek entity : entityList) {
	// selectedIds.add(entity.getId());
	// }
	// return selectedIds;
	// }
	public static void deleteReminder(DaoSession daoSessionArg, final Context context,
			final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final ElapsedReminderDao elapsedReminderDao = daoSession
						.getElapsedReminderDao();
				List<ElapsedReminder> elapsedReminders = elapsedReminderDao
						.queryBuilder()
						.where(ElapsedReminderDao.Properties.ReminderId.eq(id)).list();
				for (ElapsedReminder elapsedReminder : elapsedReminders) {
					elapsedReminder.setReminderId(null);
					elapsedReminderDao.insertOrReplace(elapsedReminder);
				}
				final ScheduledReminderDao scheduledReminderDao = daoSession
						.getScheduledReminderDao();
				List<ScheduledReminder> scheduledReminders = scheduledReminderDao
						.queryBuilder()
						.where(ElapsedReminderDao.Properties.ReminderId.eq(id)).list();
				for (ScheduledReminder scheduledReminder : scheduledReminders) {
					scheduledReminder.setReminderId(null);
					scheduledReminderDao.insertOrReplace(scheduledReminder);
				}
				final ReminderDao reminderDao = daoSession.getReminderDao();
				reminderDao.deleteByKey(id);
			}
		});
	}

	public static TaskWithDependents getTaskWithDependents(DaoSession daoSessionArg,
			final Context context, final Long localId1, final TaskDto syncEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final TaskWithDependents entityWithDependents = new TaskWithDependents();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long localId = null;
				// check for consistency
				Task localEntityByLocalId = null;
				if (localId1 != null) {
					localEntityByLocalId = DataProvider.getTask(daoSession, context,
							localId1, false);
					if (localEntityByLocalId == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"Inconsistensy in getTaskWithDependencies(): "
										+ "The entity with id == " + localId1
										+ " was not found");
					}
				}
				Task localEntityByServerId = DataProvider.getTask(daoSession, context,
						syncEntity.Id, true);
				if (localEntityByServerId != null
						&& localEntityByLocalId != null
						&& localEntityByServerId.getId().longValue() != localEntityByLocalId
								.getId().longValue()) {
					throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
							"Inconsistensy in getTaskWithDependencies(): "
									+ "The Id and ServerId do not refer to the same entity");
				}
				localId = localId1;
				if (localEntityByServerId != null) {
					localId = localEntityByServerId.getId();
				}
				RecurrenceInterval recurrenceInterval = syncEntity.RecurrenceInterval;
				long nowTime = System.currentTimeMillis();
				final Task newEntity = new Task(
						localId,
						null,
						null,// will setup parent later
						nowTime, nowTime, syncEntity.Id, syncEntity.Created,
						syncEntity.LastMod, SyncStatus.SYNCHRONIZED.getValue(),
						syncEntity.Name, (short) syncEntity.Priority.getCode(),
						syncEntity.Color, syncEntity.StartDateTime,
						syncEntity.EndDateTime, syncEntity.RequiredLength,
						syncEntity.ActualLength, syncEntity.IsCompleted == null ? false
								: syncEntity.IsCompleted, syncEntity.PercentOfCompletion,
						syncEntity.CompletedDateTime, syncEntity.Deleted,
						syncEntity.SortOrder, syncEntity.Description,
						syncEntity.Location, recurrenceInterval.getValue(),
						syncEntity.TimeUnitsCount, syncEntity.OccurrencesMaxCount,
						syncEntity.RepetitionEndDateTime, null, null,
						syncEntity.RingtoneFadeInTime, syncEntity.PlayingTime,
						syncEntity.AutomaticSnoozeDuration,
						syncEntity.AutomaticSnoozesMaxCount, syncEntity.Vibrate,
						syncEntity.VibratePattern, syncEntity.Led, syncEntity.LedPattern,
						syncEntity.LedColor);
				entityWithDependents.task = newEntity;
				boolean allDependentsAreInPlace = true;
				// // prepare labelIdList
				// ArrayList<Long> entityIdList = new ArrayList<Long>();
				// if (syncEntity.Labels != null) {
				// for (Long id : syncEntity.Labels) {
				// Label entity = DataProvider.getLabel(context, id, true);
				// if (entity == null) {
				// allDependenciesAreInPlace = false;
				// } else {
				// entityIdList.add(entity.getId());
				// }
				// }
				// }
				// entityWithDependencies.labelIdList = entityIdList;
				// entityIdList.clear();
				// // prepare contactIdList
				// if (syncEntity.Contacts != null) {
				// for (Long id : syncEntity.Contacts) {
				// Contact entity = DataProvider.getContact(context, id, true);
				// if (entity == null) {
				// allDependenciesAreInPlace = false;
				// } else {
				// entityIdList.add(entity.getId());
				// }
				// }
				// }
				// entityWithDependencies.contactIdList = entityIdList;
				// // prepare fileIdList
				// entityIdList.clear();
				// if (syncEntity.Files != null) {
				// for (Long id : syncEntity.Files) {
				// File entity = DataProvider.getFile(context, id);
				// if (entity == null) {
				// allDependenciesAreInPlace = false;
				// } else {
				// entityIdList.add(entity.getId());
				// }
				// }
				// }
				// entityWithDependencies.fileIdList = entityIdList;
				// setup parent
				if (syncEntity.ParentId != null) {
					Task parentEntity = DataProvider.getTask(daoSession, context,
							syncEntity.ParentId, true);
					if (parentEntity == null) {
						allDependentsAreInPlace = false;
					} else {
						entityWithDependents.task.setParentId(parentEntity.getId());
					}
				}
				if (!allDependentsAreInPlace) {
					entityWithDependents.task
							.setSyncStatusValue(SyncStatus.SYNC_DOWN_REQUIRED.getValue());
				}
				ArrayList<Reminder> newReminderList = new ArrayList<Reminder>();
				if (syncEntity.Reminders != null) {
					for (ReminderDto reminderDto : syncEntity.Reminders) {
						Reminder currentReminder = DataProvider.getReminder(daoSession,
								context, DataProvider.getReminderId(daoSession, context,
										reminderDto.Id));
						Reminder newReminder = new Reminder(
								currentReminder == null ? null : currentReminder.getId(),
								reminderDto.Id, localId, nowTime,
								reminderDto.ReminderDateTime,
								reminderDto.ReminderTimeMode.getValue(),
								reminderDto.Text, reminderDto.Enabled,
								reminderDto.IsAlarm, currentReminder == null ? null
										: currentReminder.getRingtone(),
								reminderDto.RingtoneFadeInTime, reminderDto.PlayingTime,
								reminderDto.AutomaticSnoozeDuration,
								reminderDto.AutomaticSnoozesMaxCount,
								reminderDto.Vibrate, reminderDto.VibratePattern,
								reminderDto.Led, reminderDto.LedPattern,
								reminderDto.LedColor);
						//
						newReminderList.add(newReminder);
					}
				}
				entityWithDependents.reminders = newReminderList;
				//
				ArrayList<TaskOccurrence> newTaskOccurrenceList = new ArrayList<TaskOccurrence>();
				TaskDto syncEntity2 = syncEntity;
				if (syncEntity2.TaskOccurrences != null) {
					for (TaskOccurrenceDto dto : syncEntity.TaskOccurrences) {
						TaskOccurrence newTaskOccurrence = new TaskOccurrence(dto.Id,
								dto.TaskId, dto.OrdinalNumber);
						newTaskOccurrenceList.add(newTaskOccurrence);
					}
				}
				entityWithDependents.taskOccurrences = newTaskOccurrenceList;
			}
		});
		return entityWithDependents;
	}

	public static LabelWithDependents getLabelWithDependents(DaoSession daoSessionArg,
			final Context context, final Long localId1,
			final biz.advancedcalendar.wsdl.sync.Label syncEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final LabelWithDependents entityWithDependents = new LabelWithDependents();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long localId = null;
				// check for consistency
				Label localEntityByLocalId = null;
				if (localId1 != null) {
					localEntityByLocalId = DataProvider.getLabel(daoSession, context,
							localId1, false);
					if (localEntityByLocalId == null) {
						throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
								"Inconsistensy in getLabelWithDependencies(): "
										+ "The entity with id == " + localId1
										+ " was not found");
					}
				}
				Label localEntityByServerId = DataProvider.getLabel(daoSession, context,
						syncEntity.Id, true);
				if (localEntityByServerId != null
						&& localEntityByLocalId != null
						&& localEntityByServerId.getId().longValue() != localEntityByLocalId
								.getId().longValue()) {
					throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
							"Inconsistensy in getLabelWithDependencies(): "
									+ "The Id and ServerId do not refer to the same entity");
				}
				localId = localId1;
				if (localEntityByServerId != null) {
					localId = localEntityByServerId.getId();
				}
				long nowTime = System.currentTimeMillis();
				final Label newEntity = new Label(localId, null, nowTime, nowTime,
						SyncStatus.SYNCHRONIZED.getValue(), syncEntity.Id,
						syncEntity.Text, syncEntity.Description, syncEntity.SortOrder,
						(short) 0, syncEntity.Deleted, null, null, syncEntity.IsCompany,
						syncEntity.LastMod.getTime(), syncEntity.Created == null ? null
								: syncEntity.Created.getTime(), syncEntity.IsSection,
						(byte) 0);
				entityWithDependents.label = newEntity;
				boolean allDependenciesAreInPlace = true;
				// prepare taskIdList
				ArrayList<Long> entityIdList = new ArrayList<Long>();
				if (syncEntity.Tasks != null) {
					for (Long id : syncEntity.Tasks) {
						Task entity = DataProvider.getTask(daoSession, context, id, true);
						if (entity == null) {
							allDependenciesAreInPlace = false;
						} else {
							entityIdList.add(entity.getId());
						}
					}
				}
				entityWithDependents.taskIdList = entityIdList;
				entityIdList.clear();
				// prepare contactIdList
				if (syncEntity.Contacts != null) {
					for (Long id : syncEntity.Contacts) {
						Contact entity = DataProvider.getContact(daoSession, context, id,
								true);
						if (entity == null) {
							allDependenciesAreInPlace = false;
						} else {
							entityIdList.add(entity.getId());
						}
					}
				}
				entityWithDependents.contactIdList = entityIdList;
				// prepare fileIdList
				entityIdList.clear();
				if (syncEntity.Files != null) {
					for (Long id : syncEntity.Files) {
						biz.advancedcalendar.greendao.File entity = DataProvider.getFile(
								daoSession, context, id);
						if (entity == null) {
							allDependenciesAreInPlace = false;
						} else {
							entityIdList.add(entity.getId());
						}
					}
				}
				entityWithDependents.fileIdList = entityIdList;
				// setup parent
				if (syncEntity.ParentId != null) {
					Label parentEntity = DataProvider.getLabel(daoSession, context,
							syncEntity.ParentId, true);
					if (parentEntity == null) {
						allDependenciesAreInPlace = false;
					} else {
						entityWithDependents.label.setLocalParentId(parentEntity.getId());
					}
				}
				if (!allDependenciesAreInPlace) {
					entityWithDependents.label
							.setSyncStatus(SyncStatus.SYNC_DOWN_REQUIRED.getValue());
				}
			}
		});
		return entityWithDependents;
	}

	public static void markEntitiesForSynchronization(DaoSession daoSessionArg,
			final Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// tasks
				TaskDao dao = daoSession.getTaskDao();
				QueryBuilder<Task> qb2 = dao.queryBuilder().where(
						TaskDao.Properties.SyncStatusValue.eq(SyncStatus.SYNCHRONIZED
								.getValue()));
				List<Task> taskList = qb2.listLazyUncached();
				for (Task task : taskList) {
					task.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED.getValue());
				}
				dao.updateInTx(taskList);
				// labels
				LabelDao dao2 = daoSession.getLabelDao();
				QueryBuilder<Label> qb3 = dao2.queryBuilder().where(
						LabelDao.Properties.SyncStatus.eq(SyncStatus.SYNCHRONIZED
								.getValue()));
				List<Label> entityList2 = qb3.listLazyUncached();
				for (Label entity : entityList2) {
					entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
				}
				dao2.updateInTx(entityList2);
				// contacts
				ContactDao dao4 = daoSession.getContactDao();
				QueryBuilder<Contact> qb4 = dao4.queryBuilder().where(
						ContactDao.Properties.SyncStatus.eq(SyncStatus.SYNCHRONIZED
								.getValue()));
				List<Contact> entityList3 = qb4.listLazyUncached();
				for (Contact task : entityList3) {
					task.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
				}
				dao4.updateInTx(entityList3);
				// Diary records
				DiaryRecordDao dao5 = daoSession.getDiaryRecordDao();
				QueryBuilder<DiaryRecord> qb5 = dao5.queryBuilder().where(
						DiaryRecordDao.Properties.SyncStatus.eq(SyncStatus.SYNCHRONIZED
								.getValue()));
				List<DiaryRecord> entityList5 = qb5.listLazyUncached();
				for (DiaryRecord task : entityList5) {
					task.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
				}
				dao5.updateInTx(entityList5);
				// TODO mark other entities
			}
		});
	}

	// public static void insertOrReplaceTaskWithDependents(final Context context,
	// final FragmentEditTaskPartMainUserInterfaceRetainer userInterfaceData,
	// final boolean isBeingExplicitlySavedByUserFromUi) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// // TODO: verify related entities. Web-service returns deleted related
	// // entities also.
	// daoSession.runInTx(new Runnable() {
	// @Override
	// public void run() {
	// // verify task and reminder consistency
	// for (Reminder newReminder : userInterfaceData.reminderList) {
	// if (newReminder.getReminderDateTime() < 0) {
	// // throw new IllegalArgumentException(
	// // "reminder.getReminderTime() < 0");
	// }
	// if (userInterfaceData.recurrenceIntervalValue ==
	// RecurrenceInterval.ONE_TIME
	// .getValue()) {
	// if (newReminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
	// .getValue()) {
	// throw new IllegalArgumentException(
	// "userInterfaceData.task.getType() == Task.TYPE.SIMPLE.getValue() && reminder.getRemindEachIteration()");
	// }
	// }
	// }
	// // verify task and repetitions consistency
	// if (userInterfaceData.recurrenceIntervalValue == RecurrenceInterval.ONE_TIME
	// .getValue()) {
	// if (userInterfaceData.taskOccurrences.size() > 0) {
	// throw new IllegalArgumentException(
	// "userInterfaceData.task.getRecurrenceIntervalValue() == RecurrenceInterval.ONE_TIME.getValue() && userInterfaceData.taskOccurrences.size() > 0");
	// }
	// } else {
	// if (RecurrenceInterval.fromInt(userInterfaceData.recurrenceIntervalValue)
	// .equals(RecurrenceInterval.DAY)) {
	// for (TaskOccurrence taskOccurrence : userInterfaceData.taskOccurrences) {
	// if (taskOccurrence.getRepetition() < 1
	// || taskOccurrence.getRepetition() > userInterfaceData.timeUnitsCount) {
	// throw new IllegalArgumentException(
	// "RecurrenceInterval.fromInt(userInterfaceData.task.getRecurrenceIntervalValue()).equals(RecurrenceInterval.DAY) && taskOccurrence.getRepetition() < 1 || taskOccurrence.getRepetition() > userInterfaceData.task.getTimeUnitsCount()");
	// }
	// }
	// } else if
	// (RecurrenceInterval.fromInt(userInterfaceData.recurrenceIntervalValue)
	// .equals(RecurrenceInterval.WEEK)) {
	// for (TaskOccurrence taskOccurrence : userInterfaceData.taskOccurrences) {
	// if (taskOccurrence.getRepetition() < 1
	// || taskOccurrence.getRepetition() > /* userInterfaceData
	// * .task
	// * .getTimeUnitsCount
	// * () * */7) {
	// throw new IllegalArgumentException(
	// "RecurrenceInterval.fromInt(userInterfaceData.task.getRecurrenceIntervalValue()).equals(RecurrenceInterval.WEEK) && taskOccurrence.getRepetition() < 1 || taskOccurrence.getRepetition() > 7");
	// }
	// }
	// } else if
	// (RecurrenceInterval.fromInt(userInterfaceData.recurrenceIntervalValue)
	// .equals(RecurrenceInterval.MONTH_BY_DATES)) {
	// for (TaskOccurrence taskOccurrence : userInterfaceData.taskOccurrences) {
	// if (taskOccurrence.getRepetition() < 1
	// || taskOccurrence.getRepetition() > /* userInterfaceData
	// * .task
	// * .getTimeUnitsCount
	// * () * */31) {
	// throw new IllegalArgumentException(
	// "RecurrenceInterval.fromInt(userInterfaceData.task.getRecurrenceIntervalValue()).equals(RecurrenceInterval.MONTH_BY_DATES) && taskOccurrence.getRepetition() < 1 || taskOccurrence.getRepetition() > 31");
	// }
	// }
	// } else if
	// (RecurrenceInterval.fromInt(userInterfaceData.recurrenceIntervalValue)
	// .equals(RecurrenceInterval.MONTH_BY_DAYS_OF_WEEKS)) {
	// for (TaskOccurrence taskOccurrence : userInterfaceData.taskOccurrences) {
	// if (taskOccurrence.getRepetition() < 1
	// || taskOccurrence.getRepetition() > /* userInterfaceData
	// * .task
	// * .getTimeUnitsCount
	// * () * */7) {
	// throw new IllegalArgumentException(
	// "RecurrenceInterval.fromInt(userInterfaceData.task.getRecurrenceIntervalValue()).equals(RecurrenceInterval.MONTH_BY_DAYS_OF_WEEKS) && taskOccurrence.getRepetition() < 1 || taskOccurrence.getRepetition() > 7");
	// }
	// }
	// }
	// }
	// // DataProvider.insertOrReplaceTask(context, userInterfaceData.task,
	// // isBeingExplicitlySavedByUserFromUi);
	// // select and remove current scheduled reminders and cancel their alarms
	// ReminderDao reminderDao = daoSession.getReminderDao();
	// List<Reminder> oldReminders = reminderDao
	// .queryBuilder()
	// .where(ReminderDao.Properties.TaskId.eq(userInterfaceData.id))
	// .list();
	// for (Reminder oldReminder : oldReminders) {
	// List<ScheduledReminder> scheduledReminders = oldReminder
	// .getScheduledReminderList(context, null);
	// for (ScheduledReminder scheduledReminder : scheduledReminders) {
	// switch (ScheduledReminder.STATE.fromInt(scheduledReminder
	// .getState())) {
	// case SCHEDULED:
	// DataProvider.deleteScheduledReminder(context,
	// scheduledReminder.getId());
	// break;
	// case ALARMED:
	// if (isBeingExplicitlySavedByUserFromUi) {
	// DataProvider.deleteScheduledReminder(context,
	// scheduledReminder.getId());
	// } else {
	// scheduledReminder.setReminderId(null);
	// DataProvider.insertOrReplaceScheduledReminder(context,
	// scheduledReminder);
	// ElapsedReminder elapsedReminder = scheduledReminder
	// .getElapsedReminder2(context);
	// if (elapsedReminder != null) {
	// elapsedReminder.setReminderId(null);
	// DataProvider.insertOrReplaceElapsedReminder(context,
	// elapsedReminder);
	// }
	// }
	// break;
	// }
	// }
	// // entity.delete() requires an entity to be "active",
	// // dao.delete(entity) works for all entities.
	// // oldReminder.delete();
	// reminderDao.delete(oldReminder);
	// }
	// // insert new scheduled reminders and set their alarms
	// if (userInterfaceData.reminderList.size() > 0) {
	// boolean scheduleScheduledRemindersOfCompletedTasks =
	// CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
	// .getIntegerPreferenceValueFromStringArray(
	// context,
	// R.string.preference_key_reminder_behavior_for_completed_task,
	// R.array.reminder_behavior_for_completed_task_values_array,
	// R.integer.reminder_default_behavior_for_completed_task);
	// if (scheduleScheduledRemindersOfCompletedTasks
	// || userInterfaceData.percentOfCompletion != 100) {
	// for (Reminder newReminder : userInterfaceData.reminderList) {
	// // assign localTaskId to new local reminders
	// newReminder.setTaskId(userInterfaceData.id);
	// DataProvider.insertOrReplaceReminder(context, newReminder);
	// Long proposedNextReminderDateTime = null;
	// // newReminder
	// // .getNextReminderDateTime(context, Calendar
	// // .getInstance().getTimeInMillis(),
	// // userInterfaceData.task,
	// // userInterfaceData.taskOccurrences);
	// if (proposedNextReminderDateTime != null) {
	// ScheduledReminder scheduledReminder = new ScheduledReminder(
	// null, newReminder.getId(), null, false,
	// proposedNextReminderDateTime,
	// proposedNextReminderDateTime, null, 0,
	// ScheduledReminder.STATE.SCHEDULED.getValue(),
	// newReminder.getText(), newReminder.getIsAlarm(),
	// newReminder.getEnabled(), newReminder
	// .getRingtone(), newReminder
	// .getAutomaticSnoozeDuration(), newReminder
	// .getAutomaticSnoozesMaxCount(),
	// newReminder.getPlayingTime(), newReminder
	// .getVibrate(), newReminder
	// .getVibratePattern(), newReminder
	// .getLed(), newReminder.getLedPattern(),
	// newReminder.getLedColor());
	// DataProvider.insertOrReplaceScheduledReminder(context,
	// scheduledReminder);
	// Alarm.setAlarm(context, scheduledReminder.getId(),
	// scheduledReminder.getNextSnoozeDateTime(), this);
	// }
	// }
	// }
	// }
	// //
	// TaskOccurrenceDao taskOccurrenceDao = daoSession.getTaskOccurrenceDao();
	// List<TaskOccurrence> oldTaskOccurrences = taskOccurrenceDao
	// .queryBuilder()
	// .where(TaskOccurrenceDao.Properties.TaskId
	// .eq(userInterfaceData.id)).list();
	// for (TaskOccurrence oldTaskOccurrence : oldTaskOccurrences) {
	// taskOccurrenceDao.delete(oldTaskOccurrence);
	// }
	// for (TaskOccurrence newTaskOccurrence : userInterfaceData.taskOccurrences) {
	// newTaskOccurrence.setTaskId(userInterfaceData.id);
	// DataProvider.insertOrReplaceTaskOccurrence(context,
	// newTaskOccurrence);
	// }
	// // userInterfaceData.task.resetTaskOccurrenceList();
	// // userInterfaceData.task.resetReminderList();
	// //
	// // MonthlyRepetitionsByDateDao monthlyRepetitionsByDateDao = daoSession
	// // .getMonthlyRepetitionsByDateDao();
	// // List<MonthlyRepetitionsByDate> oldMonthlyRepetitionsByDates =
	// // monthlyRepetitionsByDateDao
	// // .queryBuilder()
	// // .where(MonthlyRepetitionsByDateDao.Properties.TaskId
	// // .eq(userInterfaceData.task.getId())).list();
	// // for (MonthlyRepetitionsByDate oldMonthlyRepetitionsByDate :
	// // oldMonthlyRepetitionsByDates) {
	// // monthlyRepetitionsByDateDao.delete(oldMonthlyRepetitionsByDate);
	// // }
	// // for (MonthlyRepetitionsByDate newMonthlyRepetitionsByDate :
	// // userInterfaceData.monthlyRepetitionsByDateList) {
	// // newMonthlyRepetitionsByDate.setTaskId(userInterfaceData.task
	// // .getId());
	// // DataProvider.insertOrReplaceMonthlyRepetitionsByDate(context,
	// // newMonthlyRepetitionsByDate);
	// // }
	// //
	// // MonthlyRepetitionsByDayOfWeekDao monthlyRepetitionsByDayOfWeekDao =
	// // daoSession
	// // .getMonthlyRepetitionsByDayOfWeekDao();
	// // List<MonthlyRepetitionsByDayOfWeek> oldMonthlyRepetitionsByDayOfWeeks =
	// // monthlyRepetitionsByDayOfWeekDao
	// // .queryBuilder()
	// // .where(MonthlyRepetitionsByDayOfWeekDao.Properties.TaskId
	// // .eq(userInterfaceData.task.getId())).list();
	// // for (MonthlyRepetitionsByDayOfWeek oldMonthlyRepetitionsByDayOfWeek :
	// // oldMonthlyRepetitionsByDayOfWeeks) {
	// // monthlyRepetitionsByDayOfWeekDao
	// // .delete(oldMonthlyRepetitionsByDayOfWeek);
	// // }
	// // for (MonthlyRepetitionsByDayOfWeek newMonthlyRepetitionsByDayOfWeek :
	// // userInterfaceData.monthlyRepetitionsByDayOfWeekList) {
	// // newMonthlyRepetitionsByDayOfWeek.setTaskId(userInterfaceData.task
	// // .getId());
	// // DataProvider.insertOrReplaceMonthlyRepetitionsByDayOfWeek(context,
	// // newMonthlyRepetitionsByDayOfWeek);
	// // }
	// }
	// });
	// }
	public static void insertOrReplaceTaskWithDependents(DaoSession daoSessionArg,
			final Context context, final TaskWithDependents entityWithDependents1,
			final boolean isBeingExplicitlySavedByUserFromUi) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		// TODO: verify related entities. Web-service returns deleted related
		// entities also.
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				TaskWithDependents entityWithDependents = entityWithDependents1;
				try {
					long timeSkew = Helper.getLongPreferenceValue(context,
							CommonConstants.TIME_SKEW, 0, null, null);
					// verify task and reminder consistency
					for (Reminder newReminder : entityWithDependents.reminders) {
						if (newReminder.getReminderDateTime() < 0) {
							throw new IllegalArgumentException(
									"reminder.getReminderDateTime() < 0");
						}
						if (entityWithDependents.task.getRecurrenceIntervalValue() == RecurrenceInterval.ONE_TIME
								.getValue()) {
							if (entityWithDependents.task.getStartDateTime() == null) {
								if (newReminder.getReminderTimeModeValue() != ReminderTimeMode.ABSOLUTE_TIME
										.getValue()
										&& newReminder.getReminderTimeModeValue() != ReminderTimeMode.AFTER_NOW
												.getValue()) {
									throw new IllegalArgumentException(
											"RecurrenceInterval == "
													+ RecurrenceInterval
															.fromInt(entityWithDependents.task
																	.getRecurrenceIntervalValue())
													+ " && StartDateTime == "
													+ entityWithDependents.task
															.getStartDateTime()
													+ " && ReminderTimeMode != "
													+ ReminderTimeMode.fromInt(newReminder
															.getReminderTimeModeValue()));
								}
							}
						}
					}
					// verify task and repetitions consistency
					RecurrenceInterval recurrenceInterval = RecurrenceInterval
							.fromInt(entityWithDependents.task
									.getRecurrenceIntervalValue());
					switch (recurrenceInterval) {
					case ONE_TIME:
						if (entityWithDependents.taskOccurrences.size() > 0) {
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("task id = "
									+ entityWithDependents.task.getId() + ", name = "
									+ entityWithDependents.task.getName()
									+ ", RecurrenceInterval == " + recurrenceInterval
									+ ", taskOccurrences.size() == "
									+ entityWithDependents.taskOccurrences.size());
							throw new IllegalArgumentException(stringBuilder.toString());
						}
						break;
					case MINUTES:
					case HOURS:
					case DAYS:
						checkEntityWithDependentsTaskOccurrencesSizeForHoursMinutesDays(
								context, entityWithDependents, recurrenceInterval,
								timeSkew);
						for (TaskOccurrence taskOccurrence : entityWithDependents.taskOccurrences) {
							if (taskOccurrence.getOrdinalNumber() < 1
									|| taskOccurrence.getOrdinalNumber() > entityWithDependents.task
											.getTimeUnitsCount()) {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("task id = "
										+ entityWithDependents.task.getId() + ", name = "
										+ entityWithDependents.task.getName()
										+ ", RecurrenceInterval == " + recurrenceInterval
										+ ", OrdinalNumber() == " + recurrenceInterval
										+ ", TimeUnitsCount() == "
										+ entityWithDependents.task.getTimeUnitsCount());
								throw new IllegalArgumentException(stringBuilder
										.toString());
							}
						}
						break;
					case WEEKS:
						checkEntityWithDependentsTaskOccurrencesSize(context,
								entityWithDependents, recurrenceInterval, timeSkew);
						for (TaskOccurrence taskOccurrence : entityWithDependents.taskOccurrences) {
							if (taskOccurrence.getOrdinalNumber() < 1
									|| taskOccurrence.getOrdinalNumber() > 7) {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("task id = "
										+ entityWithDependents.task.getId() + ", name = "
										+ entityWithDependents.task.getName()
										+ ", RecurrenceInterval == " + recurrenceInterval
										+ ", OrdinalNumber() == " + recurrenceInterval);
								throw new IllegalArgumentException(stringBuilder
										.toString());
							}
						}
						break;
					case MONTHS_ON_DATE:
						checkEntityWithDependentsTaskOccurrencesSize(context,
								entityWithDependents, recurrenceInterval, timeSkew);
						for (TaskOccurrence taskOccurrence : entityWithDependents.taskOccurrences) {
							if (taskOccurrence.getOrdinalNumber() < 1
									|| taskOccurrence.getOrdinalNumber() > 32) {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("task id = "
										+ entityWithDependents.task.getId() + ", name = "
										+ entityWithDependents.task.getName()
										+ ", RecurrenceInterval == " + recurrenceInterval
										+ ", OrdinalNumber() == " + recurrenceInterval);
								throw new IllegalArgumentException(stringBuilder
										.toString());
							}
						}
						break;
					case MONTHS_ON_NTH_WEEK_DAY:
						checkEntityWithDependentsTaskOccurrencesSizeForMonthsOnNthWeekDay(
								context, entityWithDependents, recurrenceInterval,
								timeSkew);
						for (int i = 0; i < entityWithDependents.taskOccurrences.size() - 2; i++) {
							TaskOccurrence taskOccurrence = entityWithDependents.taskOccurrences
									.get(i);
							if (taskOccurrence.getOrdinalNumber() < 1
									|| taskOccurrence.getOrdinalNumber() > 7) {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("task id = "
										+ entityWithDependents.task.getId() + ", name = "
										+ entityWithDependents.task.getName()
										+ ", RecurrenceInterval == " + recurrenceInterval
										+ ", OrdinalNumber() == " + recurrenceInterval);
								throw new IllegalArgumentException(stringBuilder
										.toString());
							}
						}
						int location = entityWithDependents.taskOccurrences.size() - 1;
						int weekCode = entityWithDependents.taskOccurrences.get(location)
								.getOrdinalNumber();
						if (weekCode == 0 || weekCode < -1 || weekCode > 4) {
							StringBuilder stringBuilder = new StringBuilder();
							stringBuilder.append("task id = "
									+ entityWithDependents.task.getId()
									+ ", name = "
									+ entityWithDependents.task.getName()
									+ ", RecurrenceInterval == "
									+ recurrenceInterval
									+ ", taskOccurrences.size() == "
									+ entityWithDependents.taskOccurrences.size()
									+ ", week == "
									+ entityWithDependents.taskOccurrences.get(location)
											.getOrdinalNumber());
							throw new IllegalArgumentException(stringBuilder.toString());
						}
						break;
					case YEARS:
						checkEntityWithDependentsTaskOccurrencesSize(context,
								entityWithDependents, recurrenceInterval, timeSkew);
						for (TaskOccurrence taskOccurrence : entityWithDependents.taskOccurrences) {
							if (taskOccurrence.getOrdinalNumber() < 1
									|| taskOccurrence.getOrdinalNumber() > 367) {
								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("task id = "
										+ entityWithDependents.task.getId() + ", name = "
										+ entityWithDependents.task.getName()
										+ ", RecurrenceInterval == " + recurrenceInterval
										+ ", OrdinalNumber() == " + recurrenceInterval);
								throw new IllegalArgumentException(stringBuilder
										.toString());
							}
						}
						break;
					default:
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("task id = "
								+ entityWithDependents.task.getId() + ", name = "
								+ entityWithDependents.task.getName()
								+ ", RecurrenceInterval == " + recurrenceInterval);
						throw new IllegalArgumentException(stringBuilder.toString());
					}
					DataProvider
							.insertOrReplaceTask(daoSession, context,
									entityWithDependents.task,
									isBeingExplicitlySavedByUserFromUi);
					// select and remove current scheduled reminders and cancel their
					// alarms
					ReminderDao reminderDao = daoSession.getReminderDao();
					List<Reminder> oldReminders = reminderDao
							.queryBuilder()
							.where(ReminderDao.Properties.TaskId
									.eq(entityWithDependents.task.getId())).list();
					for (Reminder oldReminder : oldReminders) {
						List<ScheduledReminder> scheduledReminders = oldReminder
								.getScheduledReminderList(context, null);
						for (ScheduledReminder scheduledReminder : scheduledReminders) {
							switch (ScheduledReminder.State.fromInt(scheduledReminder
									.getStateValue())) {
							case SCHEDULED:
								DataProvider.deleteScheduledReminder(daoSession, context,
										scheduledReminder.getId());
								break;
							case ALARMED:
								if (isBeingExplicitlySavedByUserFromUi) {
									DataProvider.deleteScheduledReminder(daoSession,
											context, scheduledReminder.getId());
								} else {
									scheduledReminder.setReminderId(null);
									DataProvider.insertOrReplaceScheduledReminder(
											daoSession, context, scheduledReminder);
									ElapsedReminder elapsedReminder = scheduledReminder
											.getElapsedReminder2(context);
									if (elapsedReminder != null) {
										elapsedReminder.setReminderId(null);
										DataProvider.insertOrReplaceElapsedReminder(
												daoSession, context, elapsedReminder);
									}
								}
								break;
							}
						}
						// entity.delete() requires an entity to be "active",
						// dao.delete(entity) works for all entities.
						// oldReminder.delete();
						reminderDao.delete(oldReminder);
					}
					// insert new reminders
					for (Reminder newReminder : entityWithDependents.reminders) {
						// nullify id
						newReminder.setId(null);
						// assign localTaskId
						newReminder.setTaskId(entityWithDependents.task.getId());
						DataProvider.insertOrReplaceReminder(daoSession, context,
								newReminder);
					}
					//
					TaskOccurrenceDao taskOccurrenceDao = daoSession
							.getTaskOccurrenceDao();
					List<TaskOccurrence> oldTaskOccurrences = taskOccurrenceDao
							.queryBuilder()
							.where(TaskOccurrenceDao.Properties.TaskId
									.eq(entityWithDependents.task.getId())).list();
					for (TaskOccurrence oldTaskOccurrence : oldTaskOccurrences) {
						taskOccurrenceDao.delete(oldTaskOccurrence);
					}
					for (TaskOccurrence newTaskOccurrence : entityWithDependents.taskOccurrences) {
						newTaskOccurrence.setTaskId(entityWithDependents.task.getId());
						DataProvider.insertOrReplaceTaskOccurrence(daoSession, context,
								newTaskOccurrence);
					}
					entityWithDependents.task.resetTaskOccurrenceList();
					entityWithDependents.task.resetReminderList();
				} finally {
					if (entityWithDependents.task.getId() != null) {
						long nowTime = System.currentTimeMillis();
						boolean scheduleRemindersOfCompletedTasks = CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
								.getIntegerPreferenceValueFromStringArray(
										context,
										R.string.preference_key_reminder_behavior_for_completed_task,
										R.array.reminder_behavior_for_completed_task_values_array,
										R.integer.reminder_default_behavior_for_completed_task);
						AlarmService.resetupRemindersOfTask(context,
								entityWithDependents.task.getId(),
								scheduleRemindersOfCompletedTasks, nowTime);
					}
				}
			}

			private void checkEntityWithDependentsTaskOccurrencesSizeForHoursMinutesDays(
					Context context, final TaskWithDependents entityWithDependents,
					RecurrenceInterval recurrenceInterval, long timeSkew) {
				if (entityWithDependents.taskOccurrences.size() == 0) {
					if (entityWithDependents.task.getTimeUnitsCount() >= 1) {
						entityWithDependents.taskOccurrences.add(new TaskOccurrence(null,
								entityWithDependents.task.getId(), 1));
						Helper.setLocalChangeDateTimeToCurrentTime(
								entityWithDependents.task, timeSkew);
					} else {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("task id = "
								+ entityWithDependents.task.getId() + ", name = "
								+ entityWithDependents.task.getName()
								+ ", RecurrenceInterval == " + recurrenceInterval
								+ ", taskOccurrences.size() == "
								+ entityWithDependents.taskOccurrences.size());
						throw new IllegalArgumentException(stringBuilder.toString());
					}
				}
			}

			void checkEntityWithDependentsTaskOccurrencesSizeForMonthsOnNthWeekDay(
					Context context, final TaskWithDependents entityWithDependents,
					RecurrenceInterval recurrenceInterval, long timeSkew) {
				if (entityWithDependents.taskOccurrences.size() == 1
						|| entityWithDependents.taskOccurrences.size() == 0) {
					if (entityWithDependents.task.getTimeUnitsCount() >= 1) {
						entityWithDependents.taskOccurrences.add(new TaskOccurrence(null,
								entityWithDependents.task.getId(), 1));
						entityWithDependents.taskOccurrences.add(new TaskOccurrence(null,
								entityWithDependents.task.getId(), 1));
						Helper.setLocalChangeDateTimeToCurrentTime(
								entityWithDependents.task, timeSkew);
					} else {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("task id = "
								+ entityWithDependents.task.getId() + ", name = "
								+ entityWithDependents.task.getName()
								+ ", RecurrenceInterval == " + recurrenceInterval
								+ ", taskOccurrences.size() == "
								+ entityWithDependents.taskOccurrences.size());
						throw new IllegalArgumentException(stringBuilder.toString());
					}
				}
			}

			private void checkEntityWithDependentsTaskOccurrencesSize(Context context,
					final TaskWithDependents entityWithDependents,
					RecurrenceInterval recurrenceInterval, long timeSkew) {
				if (entityWithDependents.taskOccurrences.size() == 0) {
					if (entityWithDependents.task.getTimeUnitsCount() >= 1) {
						entityWithDependents.taskOccurrences.add(new TaskOccurrence(null,
								entityWithDependents.task.getId(), 1));
						Helper.setLocalChangeDateTimeToCurrentTime(
								entityWithDependents.task, timeSkew);
					} else {
						StringBuilder stringBuilder = new StringBuilder();
						stringBuilder.append("task id = "
								+ entityWithDependents.task.getId() + ", name = "
								+ entityWithDependents.task.getName()
								+ ", RecurrenceInterval == " + recurrenceInterval
								+ ", taskOccurrences.size() == "
								+ entityWithDependents.taskOccurrences.size());
						throw new IllegalArgumentException(stringBuilder.toString());
					}
				}
			}
		});
	}

	public static void insertOrReplaceContactWithDependents(DaoSession daoSessionArg,
			final Context context, final ContactWithDependents entityWithDependents) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		// TODO: verify related entities. Web-service returns deleted related
		// entities also.
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				DataProvider.insertOrReplaceContact(daoSession, context,
						entityWithDependents.contact);
				// save related entities
				boolean entityChanged = false;
				// save labels
				if (entityWithDependents.labelIdList != null) {
					StringCondition stringCondition = new StringCondition(
							LabelContactDao.Properties.ContactId.columnName + " = "
									+ entityWithDependents.contact.getId());
					LabelContactDao dao = daoSession.getLabelContactDao();
					dao.queryBuilder().where(stringCondition).buildDelete()
							.executeDeleteWithoutDetachingEntities();
					List<LabelContact> list2 = new ArrayList<LabelContact>();
					for (Long relatedEntityId : entityWithDependents.labelIdList) {
						LabelDao dao2 = daoSession.getLabelDao();
						Label relatedEntity;
						relatedEntity = dao2.queryBuilder()
								.where(LabelDao.Properties.Id.eq(relatedEntityId))
								.unique();
						list2.add(new LabelContact(null, entityWithDependents.contact
								.getId(), relatedEntity.getId(), false));
					}
					dao.insertOrReplaceInTx(list2);
				}
				// save tasks
				if (entityWithDependents.taskIdList != null) {
					TaskContactDao dao = daoSession.getTaskContactDao();
					StringCondition stringCondition = new StringCondition(
							TaskContactDao.Properties.ContactId.columnName + " = "
									+ entityWithDependents.contact.getId());
					dao.queryBuilder().where(stringCondition).buildDelete()
							.executeDeleteWithoutDetachingEntities();
					List<TaskContact> list1 = new ArrayList<TaskContact>();
					for (Long relatedEntityId : entityWithDependents.taskIdList) {
						TaskDao dao2 = daoSession.getTaskDao();
						Task relatedEntity;
						relatedEntity = dao2.queryBuilder()
								.where(TaskDao.Properties.Id.eq(relatedEntityId))
								.unique();
						list1.add(new TaskContact(null, entityWithDependents.contact
								.getId(), relatedEntity.getId(), false));
					}
					dao.insertOrReplaceInTx(list1);
				}
				// save files
				if (entityWithDependents.fileIdList != null) {
					FileContactDao dao = daoSession.getFileContactDao();
					StringCondition stringCondition = new StringCondition(
							FileContactDao.Properties.ContactId.columnName + " = "
									+ entityWithDependents.contact.getId());
					dao.queryBuilder().where(stringCondition).buildDelete()
							.executeDeleteWithoutDetachingEntities();
					List<FileContact> list2 = new ArrayList<FileContact>();
					for (Long relatedEntityId : entityWithDependents.fileIdList) {
						FileDao dao2 = daoSession.getFileDao();
						biz.advancedcalendar.greendao.File relatedEntity;
						relatedEntity = dao2
								.queryBuilder()
								.where(FileDao.Properties.Id.eq(relatedEntityId),
										FileDao.Properties.VersionId.isNull()).unique();
						list2.add(new FileContact(null, relatedEntity.getId(),
								entityWithDependents.contact.getId(), false));
					}
					dao.insertOrReplaceInTx(list2);
				}
				// save ContactDatas
				if (entityWithDependents.contactDataList != null) {
					ContactDataDao dao = daoSession.getContactDataDao();
					dao.queryBuilder()
							.where(ContactDataDao.Properties.LocalContactId
									.eq(entityWithDependents.contact.getId()))
							.buildDelete().executeDeleteWithoutDetachingEntities();
					for (ContactData contactData : entityWithDependents.contactDataList) {
						contactData.setLocalContactId(entityWithDependents.contact
								.getId());
					}
					dao.insertOrReplaceInTx(entityWithDependents.contactDataList);
				}
				if (entityChanged) {
					DataProvider.insertOrReplaceContact(daoSession, context,
							entityWithDependents.contact);
				}
			}
		});
	}

	public static ArrayList<Long> getFileIdListForLabel(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getLabelId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ FileLabelDao.Properties.LabelId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + FileDao.Properties.Deleted.columnName + " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + FileDao.Properties.Deleted.columnName + " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<FileLabel> list = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getFileLabelDao()
						.queryRawCreate(
								"JOIN " + FileDao.TABLENAME + " T1 ON T."
										+ FileLabelDao.Properties.LabelId.columnName
										+ " = T1." + LabelDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (FileLabel entity : list) {
					selectedIds.add(entity.getFileId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static void insertOrReplaceLabelWithDependencies(DaoSession daoSessionArg,
			final Context context, final LabelWithDependents entityWithDependencies) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		// TODO: verify related entities. Web-service returns deleted related
		// entities also.
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				DataProvider.insertOrReplaceLabel(daoSession, context,
						entityWithDependencies.label);
				boolean entityChanged = false;
				// save tasks
				if (entityWithDependencies.taskIdList != null) {
					StringCondition stringCondition = new StringCondition(
							TaskLabelDao.Properties.LabelId.columnName + " = "
									+ entityWithDependencies.label.getId().toString());
					TaskLabelDao dao = daoSession.getTaskLabelDao();
					dao.queryBuilder().where(stringCondition).buildDelete()
							.executeDeleteWithoutDetachingEntities();
					List<TaskLabel> list1 = new ArrayList<TaskLabel>();
					for (Long relatedEntityId : entityWithDependencies.taskIdList) {
						TaskDao dao2 = daoSession.getTaskDao();
						Task relatedEntity;
						relatedEntity = dao2.queryBuilder()
								.where(TaskDao.Properties.Id.eq(relatedEntityId))
								.unique();
						list1.add(new TaskLabel(null, entityWithDependencies.label
								.getId(), relatedEntity.getId(), false));
					}
					dao.insertOrReplaceInTx(list1);
				}
				// save contacts
				if (entityWithDependencies.contactIdList != null) {
					LabelContactDao dao = daoSession.getLabelContactDao();
					StringCondition stringCondition = new StringCondition(
							LabelContactDao.Properties.LabelId.columnName + " = "
									+ entityWithDependencies.label.getId().toString());
					dao.queryBuilder().where(stringCondition).buildDelete()
							.executeDeleteWithoutDetachingEntities();
					List<LabelContact> list2 = new ArrayList<LabelContact>();
					for (Long relatedEntityId : entityWithDependencies.contactIdList) {
						ContactDao dao2 = daoSession.getContactDao();
						Contact relatedEntity;
						relatedEntity = dao2.queryBuilder()
								.where(ContactDao.Properties.Id.eq(relatedEntityId))
								.unique();
						list2.add(new LabelContact(null, entityWithDependencies.label
								.getId(), relatedEntity.getId(), false));
					}
					dao.insertOrReplaceInTx(list2);
				}
				// save files
				if (entityWithDependencies.fileIdList != null) {
					FileLabelDao dao = daoSession.getFileLabelDao();
					StringCondition stringCondition = new StringCondition(
							FileLabelDao.Properties.LabelId.columnName + " = "
									+ entityWithDependencies.label.getId().toString());
					dao.queryBuilder().where(stringCondition).buildDelete()
							.executeDeleteWithoutDetachingEntities();
					List<FileLabel> list2 = new ArrayList<FileLabel>();
					for (Long relatedEntityId : entityWithDependencies.fileIdList) {
						FileDao dao2 = daoSession.getFileDao();
						biz.advancedcalendar.greendao.File relatedEntity;
						relatedEntity = dao2
								.queryBuilder()
								.where(FileDao.Properties.Id.eq(relatedEntityId),
										FileDao.Properties.VersionId.isNull()).unique();
						list2.add(new FileLabel(null, relatedEntity.getId(),
								entityWithDependencies.label.getId(), false));
					}
					dao.insertOrReplaceInTx(list2);
				}
				if (entityChanged) {
					DataProvider.insertOrReplaceLabel(daoSession, context,
							entityWithDependencies.label);
				}
			}
		});
	}

	public static ArrayList<Long> getFileIdListForContact(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getContactId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ FileContactDao.Properties.ContactId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + FileDao.Properties.Deleted.columnName + " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + FileDao.Properties.Deleted.columnName + " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<FileContact> list2 = ((Global) context.getApplicationContext())
						.getDaoSession()
						.getFileContactDao()
						.queryRawCreate(
								"JOIN " + FileDao.TABLENAME + " T1 ON T."
										+ FileContactDao.Properties.ContactId.columnName
										+ " = T1." + ContactDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (FileContact entity : list2) {
					selectedIds.add(entity.getFileId());
				}
				list2.close();
			}
		});
		return selectedIds;
	}

	public static Long getTaskId(DaoSession daoSessionArg, final Context context,
			final Long serverId) {
		if (serverId == null) {
			return null;
		}
		Task entity = ((Global) context.getApplicationContext()).getDaoSession()
				.getTaskDao().queryBuilder()
				.where(TaskDao.Properties.ServerId.eq(serverId)).unique();
		if (entity == null) {
			return null;
		}
		return entity.getId();
	}

	protected static Long getContactId(DaoSession daoSessionArg, final Context context,
			Long serverId) {
		if (serverId == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		Contact entity = daoSession.getContactDao().queryBuilder()
				.where(ContactDao.Properties.ServerId.eq(serverId)).unique();
		if (entity == null) {
			return null;
		}
		return entity.getId();
	}

	protected static Long getLabelId(DaoSession daoSessionArg, final Context context,
			Long serverId) {
		if (serverId == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		Label entity = daoSession.getLabelDao().queryBuilder()
				.where(LabelDao.Properties.ServerId.eq(serverId)).unique();
		if (entity == null) {
			return null;
		}
		return entity.getId();
	}

	public static Long getFileId(DaoSession daoSessionArg, final Context context,
			Long serverId, Integer versionId) {
		if (serverId == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		FileDao dao = daoSession.getFileDao();
		QueryBuilder<biz.advancedcalendar.greendao.File> qb = dao.queryBuilder();
		if (versionId == null) {
			qb.where(FileDao.Properties.ServerId.eq(serverId),
					FileDao.Properties.VersionId.isNull());
		} else {
			qb.where(FileDao.Properties.ServerId.eq(serverId),
					FileDao.Properties.VersionId.eq(versionId));
		}
		biz.advancedcalendar.greendao.File entity = qb.unique();
		if (entity == null) {
			return null;
		}
		return entity.getId();
	}

	public static Long getReminderId(DaoSession daoSessionArg, final Context context,
			final Long serverId) {
		if (serverId == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		Reminder entity = daoSession.getReminderDao().queryBuilder()
				.where(ReminderDao.Properties.ServerId.eq(serverId)).unique();
		if (entity == null) {
			return null;
		}
		return entity.getId();
	}

	/* public static void insertOrReplaceFileWithSyncEntity(final Context context, final
	 * Long localId, final biz.advancedcalendar.wsdl.sync.File wsdlSyncEntity) { final
	 * DaoSession daoSession = ((Global) context.getApplicationContext())
	 * .getDaoSession(); daoSession.runInTx(new Runnable() {
	 * @Override public void run() {
	 * DataProvider.insertOrReplaceFileWithDependencies(context, DataProvider
	 * .getFileWithDependencies(context, localId, wsdlSyncEntity)); } }); } public static
	 * FileWithDependencies getFileWithDependencies(final Context context, final Long
	 * localId1, final biz.advancedcalendar.wsdl.sync.File syncEntity) { final DaoSession
	 * daoSession = ((Global) context.getApplicationContext()) .getDaoSession(); final
	 * FileWithDependencies entityWithDependencies = new FileWithDependencies();
	 * daoSession.runInTx(new Runnable() {
	 * @Override public void run() { Long localId = null; // check for consistency File
	 * localEntityByLocalId = null; if (localId1 != null) { localEntityByLocalId =
	 * DataProvider.getFile(context, localId1); if (localEntityByLocalId == null) { throw
	 * new DatabaseStateAndNewEntityArgumentsInconsistensyException(
	 * "Inconsistensy in getFileWithDependencies(): " + "The entity with id == " +
	 * localId1 + " was not found"); } } File localEntityByServerId =
	 * DataProvider.getFile(context, DataProvider.getFileId(context, syncEntity.FileID,
	 * null)); if (localEntityByServerId != null && localEntityByLocalId != null &&
	 * localEntityByServerId.getId() != localEntityByLocalId.getId()) { throw new
	 * DatabaseStateAndNewEntityArgumentsInconsistensyException(
	 * "Inconsistensy in getFileWithDependencies(): " +
	 * "The Id and ServerId do not refer to the same entity"); } localId = localId1; if
	 * (localEntityByServerId != null) { localId = localEntityByServerId.getId(); } final
	 * File newEntity = new File(localId, null, null, SyncStatus.SYNCHRONIZED.getValue(),
	 * null, null, null, syncEntity.FileID, syncEntity.VersionID, syncEntity.Href,
	 * syncEntity.UID.toString(), syncEntity.Username, syncEntity.FileName,
	 * syncEntity.ContentType, syncEntity.Size, syncEntity.Description,
	 * syncEntity.ServerID, syncEntity.Created .getTime(), syncEntity.Deleted,
	 * syncEntity.EnableVersions, syncEntity.Path.toString()); entityWithDependencies.file
	 * = newEntity; // TODO: verify related entities. Web-service returns deleted //
	 * related entities also. // prepare taskIdList ArrayList<Long> taskIdList = new
	 * ArrayList<Long>(); for (Integer integer : syncEntity.Tasks) {
	 * taskIdList.add(integer.longValue()); } entityWithDependencies.taskIdList =
	 * taskIdList; entityWithDependencies.isServerTaskIdList = true; // prepare
	 * labelIdList ArrayList<Long> labelIdList = new ArrayList<Long>(); for (Integer
	 * integer : syncEntity.Labels) { labelIdList.add(integer.longValue()); }
	 * entityWithDependencies.labelIdList = labelIdList;
	 * entityWithDependencies.isServerLabelIdList = true; // prepare contactIdList
	 * ArrayList<Long> contactIdList = new ArrayList<Long>(); for (Integer integer :
	 * syncEntity.Contacts) { contactIdList.add(integer.longValue()); }
	 * entityWithDependencies.contactIdList = contactIdList;
	 * entityWithDependencies.isServerContactIdList = true; } }); return
	 * entityWithDependencies; } */
	public static Long getNewLocalIdForFile(DaoSession daoSessionArg,
			final Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		FileDao dao = daoSession.getFileDao();
		QueryBuilder<biz.advancedcalendar.greendao.File> qb = dao.queryBuilder();
		biz.advancedcalendar.greendao.File entity = qb
				.where(FileDao.Properties.LocalVersionId.isNull())
				.orderDesc(FileDao.Properties.LocalId).limit(1).unique();
		return entity == null ? 1 : entity.getLocalId() + 1;
	}

	public static Long getNewLocalVersionIdForFile(DaoSession daoSessionArg,
			final Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		FileDao dao = daoSession.getFileDao();
		QueryBuilder<biz.advancedcalendar.greendao.File> qb = dao.queryBuilder();
		biz.advancedcalendar.greendao.File entity = qb
				.where(FileDao.Properties.LocalVersionId.isNotNull())
				.orderDesc(FileDao.Properties.LocalVersionId).limit(1).unique();
		return entity == null ? 1 : entity.getLocalVersionId() + 1;
	}

	// public static void insertOrReplaceFileWithDependents(final Context context,
	// final FileWithDependents entityWithDependencies) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// // TODO: verify related entities. Web-service returns deleted related
	// // entities also.
	// daoSession.runInTx(new Runnable() {
	// @Override
	// public void run() {
	// if (entityWithDependencies.file.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED
	// .getValue()) {
	// entityWithDependencies.file.setSyncStatus(SyncStatus.SYNCHRONIZED
	// .getValue());
	// }
	// if (entityWithDependencies.file.getId() == null
	// && entityWithDependencies.file.getVersionId() == null
	// || entityWithDependencies.file.getId() != null
	// && entityWithDependencies.file.getLocalVersionId() == null) {
	// DataProvider
	// .insertOrReplaceFile(context, entityWithDependencies.file);
	// } else {
	// DataProvider.insertFileOrReplaceHistory(context,
	// entityWithDependencies.file);
	// }
	// boolean entityChanged = false;
	// // save related entities
	// if (entityWithDependencies.file.getLocalVersionId() == null
	// && entityWithDependencies.file.getVersionId() == null) {
	// // save tasks
	// // if (entityWithDependencies.taskIdList != null) {
	// // StringCondition stringCondition = new StringCondition(
	// // FileTaskDao.Properties.FileId.columnName
	// // + " = "
	// // + entityWithDependencies.file.getId()
	// // + (entityWithDependencies.file.getServerId() == 0 ? ""
	// // : " OR "
	// // + FileTaskDao.Properties.ServerFileId.columnName
	// // + " = "
	// // + entityWithDependencies.file
	// // .getServerId()));
	// // FileTaskDao dao = daoSession.getFileTaskDao();
	// // dao.queryBuilder().where(stringCondition).buildDelete()
	// // .executeDeleteWithoutDetachingEntities();
	// // List<FileTask> list1 = new ArrayList<FileTask>();
	// // for (Long relatedEntityId : entityWithDependencies.taskIdList) {
	// // TaskDao dao2 = daoSession.getTaskDao();
	// // Task relatedEntity;
	// // relatedEntity = dao2
	// // .queryBuilder()
	// // .where((entityWithDependencies.isServerTaskIdList ?
	// // TaskDao.Properties.ServerId
	// // : TaskDao.Properties.Id).eq(relatedEntityId))
	// // .unique();
	// // // if (relatedEntity != null) {
	// // // list1.add(new FileTask(null, entityWithDependencies.file
	// // // .getId(), relatedEntity.getId(),
	// // // entityWithDependencies.file.getServerId(),
	// // // // relatedEntity.getServerId(),
	// // // false));
	// // // } else {
	// // // list1.add(new FileTask(null, null, null,
	// // // entityWithDependencies.file.getServerId(),
	// // // // relatedEntityId.intValue(),
	// // // false));
	// // // if (entityWithDependencies.file.getSyncStatus() !=
	// // SyncStatus.SYNC_UP_REQUIRED.getValue()) {
	// // // entityWithDependencies.file
	// // // .setSyncStatus(SyncStatus.SYNC_DOWN_REQUIRED.getValue());
	// // // entityChanged = true;
	// // // }
	// // // }
	// // }
	// // dao.insertOrReplaceInTx(list1);
	// // }
	// // save labels
	// if (entityWithDependencies.labelIdList != null) {
	// StringCondition stringCondition = null;
	// // new StringCondition(
	// // FileLabelDao.Properties.FileId.columnName
	// // + " = "
	// // + entityWithDependencies.file.getId()
	// // + (entityWithDependencies.file.getServerId() == 0 ? ""
	// // : " OR "
	// // + FileLabelDao.Properties.ServerFileId.columnName
	// // + " = "
	// // + entityWithDependencies.file
	// // .getServerId()));
	// FileLabelDao dao = daoSession.getFileLabelDao();
	// dao.queryBuilder().where(stringCondition).buildDelete()
	// .executeDeleteWithoutDetachingEntities();
	// List<FileLabel> list2 = new ArrayList<FileLabel>();
	// for (Long relatedEntityId : entityWithDependencies.labelIdList) {
	// LabelDao dao2 = daoSession.getLabelDao();
	// Label relatedEntity;
	// relatedEntity = dao2
	// .queryBuilder()
	// .where((entityWithDependencies.isServerLabelIdList ? LabelDao.Properties.ServerId
	// : LabelDao.Properties.Id).eq(relatedEntityId))
	// .unique();
	// // if (relatedEntity != null) {
	// // list2.add(new FileLabel(null, entityWithDependencies.file
	// // .getId(), relatedEntity.getId(),
	// // entityWithDependencies.file.getServerId(),
	// // relatedEntity.getServerId(), false));
	// // } else {
	// // list2.add(new FileLabel(null, null, null,
	// // entityWithDependencies.file.getServerId(),
	// // relatedEntityId.intValue(), false));
	// // if (entityWithDependencies.file.getSyncStatus() !=
	// // SyncStatus.SYNC_UP_REQUIRED.getValue()) {
	// // entityWithDependencies.file
	// // .setSyncStatus(SyncStatus.SYNC_DOWN_REQUIRED.getValue());
	// // entityChanged = true;
	// // }
	// // }
	// }
	// dao.insertOrReplaceInTx(list2);
	// }
	// // save contacts
	// if (entityWithDependencies.contactIdList != null) {
	// StringCondition stringCondition = null;
	// // new StringCondition(
	// // FileContactDao.Properties.FileId.columnName
	// // + " = "
	// // + entityWithDependencies.file.getId()
	// // + (entityWithDependencies.file.getServerId() == 0 ? ""
	// // : " OR "
	// // + FileContactDao.Properties.ServerFileId.columnName
	// // + " = "
	// // + entityWithDependencies.file
	// // .getServerId()));
	// FileContactDao dao = daoSession.getFileContactDao();
	// dao.queryBuilder().where(stringCondition).buildDelete()
	// .executeDeleteWithoutDetachingEntities();
	// List<FileContact> list2 = new ArrayList<FileContact>();
	// for (Long relatedEntityId : entityWithDependencies.contactIdList) {
	// ContactDao dao2 = daoSession.getContactDao();
	// Contact relatedEntity;
	// relatedEntity = dao2
	// .queryBuilder()
	// .where((entityWithDependencies.isServerContactIdList ?
	// ContactDao.Properties.ServerId
	// : ContactDao.Properties.Id)
	// .eq(relatedEntityId)).unique();
	// // if (relatedEntity != null) {
	// // list2.add(new FileContact(null,
	// // entityWithDependencies.file.getId(),
	// // relatedEntity.getId(),
	// // entityWithDependencies.file.getServerId(),
	// // relatedEntity.getServerId(), false));
	// // } else {
	// // list2.add(new FileContact(null, null, null,
	// // entityWithDependencies.file.getServerId(),
	// // relatedEntityId.intValue(), false));
	// // if (entityWithDependencies.file.getSyncStatus() !=
	// // SyncStatus.SYNC_UP_REQUIRED.getValue()) {
	// // entityWithDependencies.file
	// // .setSyncStatus(SyncStatus.SYNC_DOWN_REQUIRED.getValue());
	// // entityChanged = true;
	// // }
	// // }
	// }
	// dao.insertOrReplaceInTx(list2);
	// }
	// }
	// if (entityChanged) {
	// if (entityWithDependencies.file.getId() == null
	// && entityWithDependencies.file.getVersionId() == null
	// || entityWithDependencies.file.getId() != null
	// && entityWithDependencies.file.getLocalVersionId() == null) {
	// DataProvider.insertOrReplaceFile(context,
	// entityWithDependencies.file);
	// } else {
	// DataProvider.insertFileOrReplaceHistory(context,
	// entityWithDependencies.file);
	// }
	// }
	// }
	// });
	// }
	public static WorkGroup getWorkGroup(DaoSession daoSessionArg, Context context,
			Long id, boolean isServerId) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		WorkGroupDao dao = daoSession.getWorkGroupDao();
		QueryBuilder<WorkGroup> qb = dao.queryBuilder();
		if (!isServerId) {
			qb.where(WorkGroupDao.Properties.Id.eq(id));
		} else {
			qb.where(WorkGroupDao.Properties.ServerId.eq(id));
		}
		WorkGroup entity = qb.unique();
		return entity;
	}

	public static List<Long> getWorkGroupIdListForTask(DaoSession daoSessionArg,
			Context context, Long id, boolean isServerId) {
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id == null) {
			return selectedIds;
		}
		// TODO replace getWorkGroupListForTask with getWorkGroupIdListForTask
		List<WorkGroup> entityList = DataProvider.getWorkGroupListForTask(daoSessionArg,
				context, id, isServerId);
		for (WorkGroup entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static List<WorkGroup> getWorkGroupListForTask(DaoSession daoSessionArg,
			final Context context, final Long id, final boolean isServerId) {
		final List<WorkGroup> list = new ArrayList<WorkGroup>();
		if (id == null) {
			return list;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				WorkGroupDao dao = daoSession.getWorkGroupDao();
				QueryBuilder<WorkGroup> qb = dao.queryBuilder();
				if (!isServerId) {
					qb.where(WorkGroupDao.Properties.LocalTaskId.eq(id));
				} else {
					qb.where(WorkGroupDao.Properties.TaskId.eq(id));
				}
				List<WorkGroup> list2 = qb.list();
				for (WorkGroup workGroup : list2) {
					workGroup.setWorkGroupMemberListNotAttachedToDatabase(DataProvider
							.getWorkGroupMemberList(daoSession, context,
									workGroup.getId(), false));
				}
				for (WorkGroup workGroup2 : list2) {
					list.add(workGroup2);
				}
			}
		});
		return list;
	}

	public static List<WorkGroupMember> getWorkGroupMemberList(DaoSession daoSessionArg,
			Context context, Long id, boolean isServerId) {
		List<WorkGroupMember> list = new ArrayList<WorkGroupMember>();
		if (id == null) {
			return list;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		WorkGroupMemberDao dao = daoSession.getWorkGroupMemberDao();
		QueryBuilder<WorkGroupMember> qb = dao.queryBuilder();
		if (!isServerId) {
			qb.where(WorkGroupMemberDao.Properties.LocalWorkGroupId.eq(id));
		} else {
			qb.where(WorkGroupMemberDao.Properties.GroupId.eq(id));
		}
		list = qb.list();
		return list;
	}

	public static List<Long> getContactDataIdListForContact(Context context, Long id,
			boolean b, boolean c, boolean d, boolean e, boolean f, boolean g) {
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id == null) {
			return selectedIds;
		}
		List<ContactData> entityList = DataProvider.getContactDataListForContact(null,
				context, id);
		for (ContactData entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static List<ContactData> getContactDataListForContact(
			DaoSession daoSessionArg, Context context, Long id) {
		ArrayList<ContactData> selectedIds = new ArrayList<ContactData>();
		if (id == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ContactDataDao dao = daoSession.getContactDataDao();
		StringBuilder sb = new StringBuilder(
				ContactDataDao.Properties.LocalContactId.columnName + " = " + id);
		// QueryBuilder.LOG_SQL = true;
		// QueryBuilder.LOG_VALUES = true;
		Query<ContactData> query = dao.queryBuilder()
				.where(new StringCondition(sb.toString()))
				.orderAsc(ContactDataDao.Properties.SortOrder).build();
		List<ContactData> entityList = query.list();
		return entityList;
	}

	// private static WorkGroupMember getWorkGroupMember(Context context, Long
	// id,
	// boolean isServerId) {
	//
	// if (id == null) {
	// return null;
	// }
	//
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	//
	// WorkGroupMemberDao dao = daoSession.getWorkGroupMemberDao();
	//
	// QueryBuilder<WorkGroupMember> qb = dao.queryBuilder();
	//
	// if (!isServerId) {
	// qb.where(WorkGroupMemberDao.Properties.Id.eq(id));
	// } else {
	// qb.where(WorkGroupMemberDao.Properties.MemberId.eq(id));
	// }
	//
	// WorkGroupMember entity = qb.unique();
	//
	// return entity;
	//
	// }
	public static List<Long> getSyncUpMessageIdList(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final MessageDao dao = daoSession.getMessageDao();
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		QueryBuilder<Message> qb = dao.queryBuilder().where(
				new StringCondition(MessageDao.Properties.SyncStatus.columnName + " = "
						+ SyncStatus.SYNC_UP_REQUIRED.getValue() + " OR "
						+ MessageDao.Properties.Deleted.columnName + " = 1" + " OR "
						+ MessageDao.Properties.ServerId.columnName + " IS NULL"));
		List<Message> entityList = qb.list();
		for (Message entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static Message getMessage(DaoSession daoSessionArg, Context context, Long id,
			boolean isServerId) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		MessageDao dao = daoSession.getMessageDao();
		QueryBuilder<Message> qb = dao.queryBuilder();
		if (!isServerId) {
			qb.where(MessageDao.Properties.Id.eq(id));
		} else {
			qb.where(MessageDao.Properties.ServerId.eq(id));
		}
		Message entity = qb.unique();
		return entity;
	}

	// public static void insertOrReplaceMessagesWithSyncEntity(final Context context,
	// final ArrayOfMessage syncResponseEntity) {
	// final long currentTime = Calendar.getInstance().getTimeInMillis();
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// daoSession.runInTx(new Runnable() {
	// @Override
	// public void run() {
	// List<Message> messageList = new ArrayList<Message>();
	// for (biz.advancedcalendar.wsdl.sync.Message syncEntity : syncResponseEntity) {
	// Message localEntity = DataProvider.getMessage(context,
	// syncEntity.MessageID.longValue(), true);
	// WorkGroupDao dao = daoSession.getWorkGroupDao();
	// WorkGroup workGroup = dao
	// .queryBuilder()
	// .where(WorkGroupDao.Properties.ServerId
	// .eq(syncEntity.WorkgroupID)).unique();
	// final Message newEntity = new Message(localEntity == null ? null
	// : localEntity.getId(), workGroup.getId(),// private Long
	// // LocalWorkgroupId;
	// localEntity.getLocalCreateDateTime(),// private long
	// // LocalCreateDateTime;
	// localEntity.getLocalChangeDateTime(),// private Long
	// // LocalChangeDateTime;
	// localEntity.getSyncStatus(),
	// // private Long SyncChangeDateTime;
	// syncEntity.MessageID,// private Integer ServerId;
	// false, syncEntity.WorkgroupID,// private Integer
	// // WorkgroupId;
	// syncEntity.From,// private int FromUser;
	// syncEntity.To,// private Integer ToUser;
	// syncEntity.Type.shortValue(),// private short Type;
	// syncEntity.Date.getTime(),// private long Date;
	// syncEntity.Status.shortValue(),// private short
	// // Status;
	// syncEntity.Text,// private String Text;
	// syncEntity.DiscussionID // private Integer
	// // DiscussionId
	// );
	// messageList.add(newEntity);
	// }
	// MessageDao dao = daoSession.getMessageDao();
	// dao.insertOrReplaceInTx(messageList);
	// }
	// });
	// }
	public static ArrayList<Long> getMessageIdListForTask(DaoSession daoSessionArg,
			Context context, final Long id) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				WorkGroupDao dao = daoSession.getWorkGroupDao();
				WorkGroup workGroup = dao.queryBuilder()
						.where(WorkGroupDao.Properties.TaskId.eq(id)).unique();
				MessageDao dao2 = daoSession.getMessageDao();
				List<Message> list2 = dao2
						.queryBuilder()
						.where(MessageDao.Properties.LocalWorkgroupId.eq(workGroup
								.getId())).listLazy();
				for (Message entity2 : list2) {
					selectedIds.add(entity2.getId());
				}
			}
		});
		return selectedIds;
	}

	public static Message getLastMessageForTask(DaoSession daoSessionArg,
			Context context, final Long id) {
		if (id == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final List<Message> messageIdList = new ArrayList<Message>();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				WorkGroupDao dao = daoSession.getWorkGroupDao();
				WorkGroup workGroup = dao.queryBuilder()
						.where(WorkGroupDao.Properties.LocalTaskId.eq(id)).unique();
				MessageDao dao2 = daoSession.getMessageDao();
				List<Message> list2 = dao2
						.queryBuilder()
						.where(MessageDao.Properties.WorkgroupId.eq(workGroup
								.getServerId())).orderDesc(MessageDao.Properties.Date)
						.limit(1).listLazy();
				for (Message entity2 : list2) {
					messageIdList.add(entity2);
					return;
				}
			}
		});
		if (messageIdList.size() > 0) {
			return messageIdList.get(0);
		} else {
			return null;
		}
	}

	public static List<Message> getMessageListForTask(DaoSession daoSessionArg,
			Context context, final Long id) {
		final ArrayList<Message> selectedEntities = new ArrayList<Message>();
		if (id == null) {
			return selectedEntities;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				WorkGroupDao dao = daoSession.getWorkGroupDao();
				WorkGroup workGroup = dao.queryBuilder()
						.where(WorkGroupDao.Properties.LocalTaskId.eq(id)).unique();
				if (workGroup != null) {
					MessageDao dao2 = daoSession.getMessageDao();
					List<Message> list2 = dao2
							.queryBuilder()
							.where(MessageDao.Properties.LocalWorkgroupId.eq(workGroup
									.getId())).orderAsc(MessageDao.Properties.Date)
							.listLazy();
					for (Message entity2 : list2) {
						selectedEntities.add(entity2);
					}
				}
			}
		});
		return selectedEntities;
	}

	// public static void insertMessage(Context context, final String message,
	// final long taskId) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// daoSession.runInTx(new Runnable() {
	// @Override
	// public void run() {
	// WorkGroupDao dao = daoSession.getWorkGroupDao();
	// WorkGroup workGroup = dao.queryBuilder()
	// .where(WorkGroupDao.Properties.LocalTaskId.eq(taskId)).unique();
	// MessageDao dao2 = daoSession.getMessageDao();
	// // Message m = new Message(null, workGroup.getId(), 0L, 0L, 0L, null,
	// // workGroup.getServerId(), 0, 0, (short) 0, 0L, (short) 0, message,
	// // null);
	// // dao2.insert(m);
	// }
	// });
	// return;
	// }
	private static void copyToExternalStoragePrivateFile(DaoSession daoSessionArg,
			final Context context, final String filePath, final String fileName,
			final long id) throws IOException {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				java.io.File inputFile = new java.io.File(filePath);
				java.io.File outputFile = new java.io.File(context
						.getExternalFilesDir(null), fileName);
				try {
					InputStream is = new FileInputStream(inputFile);
					OutputStream os = new FileOutputStream(outputFile);
					byte[] data = new byte[is.available()];
					is.read(data);
					os.write(data);
					is.close();
					os.close();
					FileDao dao = daoSession.getFileDao();
					biz.advancedcalendar.greendao.File f = dao.load(id);
					f.setLocalPath(outputFile.getPath());
					dao.insertOrReplace(f);
				} catch (IOException e) {
					// Unable to create file, likely because external storage is
					// not currently mounted.
					Log.w(CommonConstants.DEBUG_TAG, "Error writing " + outputFile, e);
					// throw e;
				}
			}
		});
	}

	// public static void deleteExternalStoragePrivateFile(final Context context,
	// String filePath) {
	// java.io.File file = new java.io.File(filePath);
	// if (file != null) {
	// file.delete();
	// }
	// }
	public static boolean hasExternalStoragePrivateFile(final Context context,
			String filePath) {
		java.io.File file = new java.io.File(filePath);
		return file.exists();
	}

	public static List<Long> getSyncUpDiaryRecordIdList(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final DiaryRecordDao dao = daoSession.getDiaryRecordDao();
		ArrayList<Long> selectedIds = new ArrayList<Long>();
		QueryBuilder<DiaryRecord> qb = dao.queryBuilder().where(
				new StringCondition(DiaryRecordDao.Properties.SyncStatus.columnName
						+ " = " + SyncStatus.SYNC_UP_REQUIRED.getValue() + " OR "
						+ DiaryRecordDao.Properties.Deleted.columnName + " = 1" + " OR "
						+ DiaryRecordDao.Properties.ServerId.columnName + " IS NULL"));
		List<DiaryRecord> entityList = qb.list();
		for (DiaryRecord entity : entityList) {
			selectedIds.add(entity.getId());
		}
		return selectedIds;
	}

	public static void insertOrReplaceDiaryRecordWithSyncEntity(DaoSession daoSessionArg,
			final Context context, final Long localId,
			final biz.advancedcalendar.wsdl.sync.Record syncEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				DataProvider.insertOrReplaceDiaryRecord(daoSession, context, DataProvider
						.getDiaryRecordWithDependencies(daoSession, context, localId,
								syncEntity));
			}
		});
	}

	protected static DiaryRecord getDiaryRecordWithDependencies(DaoSession daoSessionArg,
			final Context context, final Long localId, final Record syncEntity) {
		final DiaryRecord newEntity = new DiaryRecord(localId, 0, 0,
				SyncStatus.SYNCHRONIZED.getValue(), syncEntity.ID, syncEntity.Deleted,
				syncEntity.Date.getTime(), syncEntity.StartTime, syncEntity.EndTime,
				DataProvider.getTask(daoSessionArg, context, syncEntity.TaskID, true)
						.getId(), false, false, syncEntity.Waste,
				syncEntity.WasCompleted, syncEntity.RecordText, syncEntity.FullText);
		return newEntity;
	}

	public static Long getDiaryRecordId(DaoSession daoSessionArg, final Context context,
			Long serverId) {
		if (serverId == null) {
			return null;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		DiaryRecord entity = daoSession.getDiaryRecordDao().queryBuilder()
				.where(DiaryRecordDao.Properties.ServerId.eq(serverId)).unique();
		if (entity == null) {
			return null;
		}
		return entity.getId();
	}

	public static void deleteDiaryRecord(DaoSession daoSessionArg, final Context context,
			final Long id, final boolean isServerId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final DiaryRecordDao dao = daoSession.getDiaryRecordDao();
		dao.deleteByKey(id);
	}

	public static void markDiaryRecordDeleted(DaoSession daoSessionArg,
			final Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				DiaryRecordDao dao = daoSession.getDiaryRecordDao();
				DiaryRecord diaryRecord = dao.load(id);
				diaryRecord.setDeleted(true);
				dao.insertOrReplace(diaryRecord);
			}
		});
	}

	public static void insertOrReplaceDiaryRecord(DaoSession daoSessionArg,
			Context context, DiaryRecord newEntity) {
		if (newEntity.getServerId() == 0
				&& newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED.getValue()) {
			throw new IllegalArgumentException(
					"Illegal state: newEntity.getServerId() == null || newEntity.getServerId() == 0 && newEntity.getSyncStatus() != SyncStatus.SYNC_UP_REQUIRED.getValue()");
		}
		// check for start time end time consistency:
		if (newEntity.getStartTime() != null && newEntity.getEndTime() != null
				&& newEntity.getStartTime() > newEntity.getEndTime()) {
			throw new IllegalArgumentException("Illegal state: StartTime > EndTime");
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		// newEntity.getId() == null
		if (newEntity.getId() == null) {
			// newEntity.getId() == null
			// newEntity.getServerId() != null
			if (newEntity.getServerId() != null) {
				DiaryRecord localEntityByServerId = DataProvider.getDiaryRecord(
						daoSession, context, newEntity.getServerId(), true);
				if (localEntityByServerId != null) {
					newEntity.setId(localEntityByServerId.getId());
				}
			}
		}
		// newEntity.getId() != null
		else {
			DiaryRecord localEntityByLocalId = DataProvider.getDiaryRecord(daoSession,
					context, newEntity.getId(), false);
			if (localEntityByLocalId == null) {
				throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
						"The requested DiaryRecord with id " + newEntity.getId()
								+ " does not exist.");
			}
			// newEntity.getId() != null
			// newEntity.getServerId() == null
			if (newEntity.getServerId() == null) {
				newEntity.setServerId(localEntityByLocalId.getServerId());
			}
			// newEntity.getId() != null
			// newEntity.getServerId() != null
			else {
				DiaryRecord localEntityByServerId = DataProvider.getDiaryRecord(
						daoSession, context, newEntity.getServerId(), true);
				if (localEntityByServerId != null
						&& localEntityByLocalId.getId() != localEntityByServerId.getId()) {
					throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
							"The LocalId and ServerId do not refer to the same entity");
				}
			}
		}
		// check related entity
		// newEntity.getLocalTaskId() != null
		if (newEntity.getLocalTaskId() != null) {
			Task localRelatedEntityByLocalId = DataProvider.getTask(daoSession, context,
					newEntity.getLocalTaskId(), false);
			if (localRelatedEntityByLocalId == null) {
				throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
						"The requested related Task entity with id "
								+ newEntity.getLocalTaskId() + " does not exist.");
			}
		}
		DiaryRecordDao dao = daoSession.getDiaryRecordDao();
		dao.insertOrReplace(newEntity);
	}

	public static TaskDtoWithDependents getTaskDtoHolder(DaoSession daoSessionArg,
			final Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final BooleanWrapper found = new BooleanWrapper();
		final TaskDtoWithDependentsHolder taskDtoWithDependentsHolder = new TaskDtoWithDependentsHolder();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Task localEntity = DataProvider.getTask(daoSession, context, id, false);
				if (localEntity == null) {
					found.value = false;
				} else {
					found.value = true;
					// Calendar calendar = Calendar.getInstance();
					// prepare wsdlSyncUpEntity
					// Date startDate = null;
					// Long startTime = null;
					// if (localEntity.getStartDateTime() != null) {
					// calendar.setTimeInMillis(localEntity.getStartDateTime());
					// startTime = (((calendar.get(Calendar.HOUR_OF_DAY) * 60L + calendar
					// .get(Calendar.MINUTE)) * 60 + calendar
					// .get(Calendar.SECOND)) * 1000 + calendar
					// .get(Calendar.MILLISECOND)) * 10000;
					// calendar.clear(Calendar.MINUTE);
					// calendar.clear(Calendar.SECOND);
					// calendar.clear(Calendar.MILLISECOND);
					// calendar.set(Calendar.HOUR_OF_DAY, 0);
					// startDate = new Date(calendar.getTimeInMillis());
					// }
					// Date endDate = null;
					// Long endTime = null;
					// if (localEntity.getEndDateTime() != null) {
					// calendar.setTimeInMillis(localEntity.getEndDateTime());
					// endTime = (((calendar.get(Calendar.HOUR_OF_DAY) * 60L + calendar
					// .get(Calendar.MINUTE)) * 60 + calendar
					// .get(Calendar.SECOND)) * 1000 + calendar
					// .get(Calendar.MILLISECOND)) * 10000;
					// calendar.clear(Calendar.MINUTE);
					// calendar.clear(Calendar.SECOND);
					// calendar.clear(Calendar.MILLISECOND);
					// calendar.set(Calendar.HOUR_OF_DAY, 0);
					// endDate = new Date(calendar.getTimeInMillis());
					// }
					List<Long> localIdList;
					List<Long> serverIdList;
					List<Long> labelIdList = null;
					List<ReminderDto> reminderDtos = null;
					List<TaskOccurrenceDto> taskOccurrenceDtos = null;
					if (localEntity.getDeleted()) {
						// labelIdList = null;
						// reminderDtos = null;
						// taskOccurrenceDtos = null;
					} else {
						localIdList = DataProvider.getLabelIdListForTask(daoSession,
								context, localEntity.getId(), false, true, false);
						labelIdList = new ArrayList<Long>();
						for (Long id : localIdList) {
							Label tempEntity = DataProvider.getLabel(daoSession, context,
									id, false);
							if (tempEntity != null) {
								if (tempEntity.getServerId() != null) {
									labelIdList.add(tempEntity.getServerId());
								}
							}
						}
						localIdList = DataProvider.getContactIdListForTask(daoSession,
								context, localEntity.getId(), false, true, false);
						serverIdList = new ArrayList<Long>();
						for (Long id : localIdList) {
							Contact tempEntity = DataProvider.getContact(daoSession,
									context, id, false);
							if (tempEntity != null) {
								if (tempEntity.getServerId() != 0) {
									serverIdList.add(tempEntity.getServerId());
								}
							}
						}
						// syncUpEntityDto.Contacts = serverIdList;
						localIdList = DataProvider.getFileIdListForTask(daoSession,
								context, localEntity.getId(), false, true, false);
						serverIdList = new ArrayList<Long>();
						for (Long id : localIdList) {
							biz.advancedcalendar.greendao.File tempEntity = DataProvider
									.getFile(daoSession, context, id);
							if (tempEntity != null) {
								if (tempEntity.getServerId() != 0) {
									serverIdList.add(tempEntity.getServerId());
								}
							}
						}
						localIdList = DataProvider.getReminderIdListForTask(daoSession,
								context, localEntity.getId(), true, true);
						Long[] ids = new Long[localIdList.size()];
						for (int i = 0; i < localIdList.size(); i++) {
							ids[i] = localIdList.get(i);
						}
						Long[] reminderIds = ids;
						reminderDtos = new ArrayList<ReminderDto>();
						for (Long id : localIdList) {
							Reminder reminder = DataProvider.getReminder(daoSession,
									context, id);
							ReminderDto reminderDto = new ReminderDto(reminder
									.getServerId(), localEntity.getServerId(), reminder
									.getReminderDateTime(), ReminderTimeMode
									.fromInt(reminder.getReminderTimeModeValue()),
									reminder.getText(), reminder.getEnabled(), reminder
											.getIsAlarm(), reminder
											.getRingtoneFadeInTime(), reminder
											.getPlayingTime(), reminder
											.getAutomaticSnoozeDuration(), reminder
											.getAutomaticSnoozesMaxCount(), reminder
											.getVibrate(), reminder.getVibratePattern(),
									reminder.getLed(), reminder.getLedPattern(), reminder
											.getLedColor());
							reminderDtos.add(reminderDto);
						}
						//
						localIdList = DataProvider.getTaskOccurrenceIdListForTask(
								daoSession, context, localEntity.getId());
						ids = new Long[localIdList.size()];
						for (int i = 0; i < localIdList.size(); i++) {
							ids[i] = localIdList.get(i);
						}
						Long[] dailyRepetitionIds = ids;
						taskOccurrenceDtos = new ArrayList<TaskOccurrenceDto>();
						for (Long id : localIdList) {
							TaskOccurrence taskOccurrence = DataProvider
									.getTaskOccurrence(daoSession, context, id);
							TaskOccurrenceDto taskOccurrenceDto = new TaskOccurrenceDto(
									null, null, taskOccurrence.getOrdinalNumber());
							taskOccurrenceDtos.add(taskOccurrenceDto);
						}
						//
						// localIdList = DataProvider
						// .getMonthlyRepetitionsByDateIdListForTask(context,
						// localEntity.getId());
						// ids = new Long[localIdList.size()];
						// for (int i = 0; i < localIdList.size(); i++) {
						// ids[i] = localIdList.get(i);
						// }
						// Long[] monthlyRepetitionsByDateIds = ids;
						// monthlyRepetitionsByDateDtos = new
						// ArrayList<MonthlyRepetitionsByDateDto>();
						// for (Long id : localIdList) {
						// MonthlyRepetitionsByDate monthlyRepetitionsByDate =
						// DataProvider
						// .getMonthlyRepetitionsByDate(context, id);
						// MonthlyRepetitionsByDateDto monthlyRepetitionsByDateDto = new
						// MonthlyRepetitionsByDateDto(
						// null, null, monthlyRepetitionsByDate.getDate());
						// monthlyRepetitionsByDateDtos.add(monthlyRepetitionsByDateDto);
						// }
						//
						// localIdList = DataProvider
						// .getMonthlyRepetitionsByDayOfWeekIdListForTask(context,
						// localEntity.getId());
						// ids = new Long[localIdList.size()];
						// for (int i = 0; i < localIdList.size(); i++) {
						// ids[i] = localIdList.get(i);
						// }
						// Long[] monthlyRepetitionsByDayOfWeekIds = ids;
						// monthlyRepetitionsByDayOfWeekDtos = new
						// ArrayList<MonthlyRepetitionsByDayOfWeekDto>();
						// for (Long id : localIdList) {
						// MonthlyRepetitionsByDayOfWeek monthlyRepetitionsByDayOfWeek =
						// DataProvider
						// .getMonthlyRepetitionsByDayOfWeek(context, id);
						// MonthlyRepetitionsByDayOfWeekDto
						// monthlyRepetitionsByDayOfWeekDto = new
						// MonthlyRepetitionsByDayOfWeekDto(
						// null, null, monthlyRepetitionsByDayOfWeek
						// .getRepeatOnMonday(),
						// monthlyRepetitionsByDayOfWeek.getRepeatOnTuesday(),
						// monthlyRepetitionsByDayOfWeek.getRepeatOnWednesday(),
						// monthlyRepetitionsByDayOfWeek.getRepeatOnThursday(),
						// monthlyRepetitionsByDayOfWeek.getRepeatOnFriday(),
						// monthlyRepetitionsByDayOfWeek.getRepeatOnSaturday(),
						// monthlyRepetitionsByDayOfWeek.getRepeatOnSunday(),
						// monthlyRepetitionsByDayOfWeek
						// .getMonthlyRepetitionsByDayOfWeekType());
						// monthlyRepetitionsByDayOfWeekDtos
						// .add(monthlyRepetitionsByDayOfWeekDto);
						// }
						final biz.advancedcalendar.wsdl.sync.TaskDto syncUpEntityDto = new biz.advancedcalendar.wsdl.sync.TaskDto(
								localEntity.getServerId(), DataProvider
										.getServerParentIdForTask(daoSession, context,
												localEntity.getId()), null, localEntity
										.getLastMod(), localEntity.getName(),
								biz.advancedcalendar.wsdl.sync.Enums.TaskPriority
										.fromInt(localEntity.getPriority()), localEntity
										.getColor(), localEntity.getStartDateTime(),
								localEntity.getEndDateTime(),
								localEntity.getRequiredLength(),
								localEntity.getActualLength(),
								localEntity.getIsCompleted() ? true : null,
								localEntity.getPercentOfCompletion(),
								localEntity.getCompletedTime(),
								localEntity.getDeleted(),
								localEntity.getSortOrder(),// null
								localEntity.getDescription(), localEntity.getLocation(),
								localEntity.getTimeUnitsCount(),
								RecurrenceInterval.fromInt(localEntity
										.getRecurrenceIntervalValue()), localEntity
										.getOccurrencesMaxCount(), localEntity
										.getRepetitionEndDateTime(), localEntity
										.getRingtoneFadeInTime(), localEntity
										.getPlayingTime(), localEntity
										.getAutomaticSnoozeDuration(), localEntity
										.getAutomaticSnoozesMaxCount(), localEntity
										.getVibrate(), localEntity.getVibratePattern(),
								localEntity.getLed(), localEntity.getLedPattern(),
								localEntity.getLedColor(), null, reminderDtos,
								taskOccurrenceDtos);
						taskDtoWithDependentsHolder.taskDtoWithDependents = new TaskDtoWithDependents(
								syncUpEntityDto, reminderIds, dailyRepetitionIds);
					}
				}
			}
		});
		if (found.value) {
			return taskDtoWithDependentsHolder.taskDtoWithDependents;
		} else {
			return null;
		}
	}

	public static biz.advancedcalendar.wsdl.sync.Label getLabelDto(
			DaoSession daoSessionArg, final Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final BooleanWrapper found = new BooleanWrapper();
		final biz.advancedcalendar.wsdl.sync.Label wsdlSyncUpEntity = new biz.advancedcalendar.wsdl.sync.Label();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Label localEntity = DataProvider.getLabel(daoSession, context, id, false);
				if (localEntity == null) {
					found.value = false;
				} else {
					found.value = true;
					wsdlSyncUpEntity.Contacts = new ArrayList<Long>();
					wsdlSyncUpEntity.Id = localEntity.getServerId();
					wsdlSyncUpEntity.Text = localEntity.getText();
					wsdlSyncUpEntity.Created = null;
					wsdlSyncUpEntity.Deleted = localEntity.getDeleted();
					wsdlSyncUpEntity.SortOrder = localEntity.getSortOrder();// null
					wsdlSyncUpEntity.LastMod = null;
					wsdlSyncUpEntity.Description = localEntity.getDescription();
					wsdlSyncUpEntity.Files = new ArrayList<Long>();
					wsdlSyncUpEntity.IsCompany = localEntity.getIsCompany();
					wsdlSyncUpEntity.IsSection = localEntity.getIsSection();
					wsdlSyncUpEntity.OriginalID = localEntity.getOriginalId();
					wsdlSyncUpEntity.Tasks = new ArrayList<Long>();
					List<Long> localIdList;
					List<Long> serverIdList;
					if (wsdlSyncUpEntity.Deleted) {
						wsdlSyncUpEntity.Tasks = null;
						wsdlSyncUpEntity.Contacts = null;
						wsdlSyncUpEntity.Files = null;
					} else {
						localIdList = DataProvider.getTaskIdListForLabel(daoSession,
								context, localEntity.getId(), false, true, false, true,
								false);
						serverIdList = new ArrayList<Long>();
						for (Long id : localIdList) {
							Task tempEntity = DataProvider.getTask(daoSession, context,
									id, false);
							if (tempEntity != null) {
								if (tempEntity.getServerId() != null) {
									serverIdList.add(tempEntity.getServerId());
								}
							}
						}
						wsdlSyncUpEntity.Tasks = serverIdList;
						localIdList = DataProvider.getContactIdListForLabel(daoSession,
								context, localEntity.getId(), false, true, false);
						serverIdList = new ArrayList<Long>();
						for (Long id : localIdList) {
							Contact tempEntity = DataProvider.getContact(daoSession,
									context, id, false);
							if (tempEntity != null) {
								if (tempEntity.getServerId() != 0) {
									serverIdList.add(tempEntity.getServerId());
								}
							}
						}
						wsdlSyncUpEntity.Contacts = serverIdList;
						localIdList = DataProvider.getFileIdListForLabel(daoSession,
								context, localEntity.getId(), false, true, false);
						serverIdList = new ArrayList<Long>();
						for (Long id : localIdList) {
							biz.advancedcalendar.greendao.File tempEntity = DataProvider
									.getFile(daoSession, context, id);
							if (tempEntity != null) {
								if (tempEntity.getServerId() != 0) {
									serverIdList.add(tempEntity.getServerId());
								}
							}
						}
						wsdlSyncUpEntity.Files = serverIdList;
					}
				}
			}
		});
		if (found.value) {
			return wsdlSyncUpEntity;
		} else {
			return null;
		}
	}

	protected static Long getServerParentIdForTask(DaoSession daoSessionArg,
			Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		class LongHolder {
			Long value = null;
		}
		final LongHolder longHolder = new LongHolder();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				TaskDao dao = daoSession.getTaskDao();
				Task entity = dao.queryBuilder().where(TaskDao.Properties.Id.eq(id))
						.unique();
				if (entity.getParentId() != null) {
					Task parent = dao.queryBuilder()
							.where(TaskDao.Properties.Id.eq(entity.getParentId()))
							.unique();
					longHolder.value = parent.getServerId();
				}
			}
		});
		return longHolder.value;
	}

	protected static Long getServerParentIdForLabel(DaoSession daoSessionArg,
			Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		class LongHolder {
			Long value = null;
		}
		final LongHolder longHolder = new LongHolder();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				LabelDao dao = daoSession.getLabelDao();
				Label entity = dao.queryBuilder().where(LabelDao.Properties.Id.eq(id))
						.unique();
				if (entity.getLocalParentId() != null) {
					Label parent = dao.queryBuilder()
							.where(LabelDao.Properties.Id.eq(entity.getLocalParentId()))
							.unique();
					longHolder.value = parent.getServerId();
				}
			}
		});
		return longHolder.value;
	}

	public static Record getRecordDto(DaoSession daoSessionArg, final Context context,
			final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final BooleanWrapper found = new BooleanWrapper();
		final biz.advancedcalendar.wsdl.sync.Record wsdlSyncUpEntity = new biz.advancedcalendar.wsdl.sync.Record();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				DiaryRecord localEntity = DataProvider.getDiaryRecord(daoSession,
						context, id, false);
				if (localEntity == null) {
					found.value = false;
				} else {
					found.value = true;
					wsdlSyncUpEntity.Created = null;
					wsdlSyncUpEntity.Date = new Date(localEntity.getDate());
					wsdlSyncUpEntity.Deleted = localEntity.getDeleted();
					wsdlSyncUpEntity.EndTime = localEntity.getEndTime();
					wsdlSyncUpEntity.FullText = localEntity.getFullText();
					wsdlSyncUpEntity.LastMod = null;
					wsdlSyncUpEntity.ID = localEntity.getServerId();
					wsdlSyncUpEntity.RecordText = localEntity.getText();
					wsdlSyncUpEntity.StartTime = localEntity.getStartTime();
					// wsdlSyncEntity.TaskID = localEntity.getTaskId();
					// wsdlSyncEntity.UserName = localEntity.getUserName();
					wsdlSyncUpEntity.WasCompleted = localEntity.getWasCompleted();
					wsdlSyncUpEntity.Waste = localEntity.getIsWasteTime();
					// TODO:
					// At this point, push the stub sync entity to the server
					// (without
					// related entities)
					if (wsdlSyncUpEntity.Deleted) {
						wsdlSyncUpEntity.TaskID = null;
					} else {
						// wsdlSyncEntity.TaskID = localEntity.getTaskId();
					}
				}
			}
		});
		if (found.value) {
			return wsdlSyncUpEntity;
		} else {
			return null;
		}
	}

	/* public static biz.advancedcalendar.wsdl.sync.File getWsdlSyncFile( final Context
	 * context, final Long id) { final DaoSession daoSession = ((Global)
	 * context.getApplicationContext()) .getDaoSession(); final
	 * biz.advancedcalendar.wsdl.sync.File wsdlSyncEntity = new
	 * biz.advancedcalendar.wsdl.sync.File(); final BooleanWrapper found = new
	 * BooleanWrapper(); daoSession.runInTx(new Runnable() {
	 * @Override public void run() { File syncUpEntity = DataProvider.getFile(context,
	 * id); if (syncUpEntity == null) { found.value = false; } else { found.value = true;
	 * // prepare wsdlSyncUpEntity wsdlSyncEntity.Contacts = new ArrayList<Long>();
	 * wsdlSyncEntity.ContentType = syncUpEntity.getContentType(); wsdlSyncEntity.Labels =
	 * new ArrayList<Long>(); wsdlSyncEntity.Created = null; wsdlSyncEntity.Deleted =
	 * syncUpEntity.getDeleted(); wsdlSyncEntity.Description =
	 * syncUpEntity.getDescription(); wsdlSyncEntity.EnableVersions =
	 * syncUpEntity.getEnableVersions(); wsdlSyncEntity.FileID =
	 * syncUpEntity.getServerId(); wsdlSyncEntity.FileName = syncUpEntity.getFileName();
	 * wsdlSyncEntity.Href = syncUpEntity.getHref(); wsdlSyncEntity.LastMod = null;
	 * wsdlSyncEntity.OldVersions = new ArrayOfFile(); wsdlSyncEntity.Path =
	 * UUID.fromString(syncUpEntity.getPath()); wsdlSyncEntity.ServerID =
	 * syncUpEntity.getServerId(); wsdlSyncEntity.Size = syncUpEntity.getSize();
	 * wsdlSyncEntity.Tasks = new ArrayList<Long>(); wsdlSyncEntity.UID =
	 * UUID.fromString(syncUpEntity.getUID()); wsdlSyncEntity.Username =
	 * syncUpEntity.getUserName(); wsdlSyncEntity.VersionID = syncUpEntity.getVersionId();
	 * List<Long> idList; List<Long> arrayOfint; // TODO: // At this point, push the stub
	 * sync entity to the server // (without // related entities) if
	 * (wsdlSyncEntity.Deleted) { wsdlSyncEntity.Tasks = null; wsdlSyncEntity.Labels =
	 * null; wsdlSyncEntity.Contacts = null; } else { idList =
	 * DataProvider.getTaskIdListForFile(context, syncUpEntity.getId(), false, true,
	 * false, true, false); arrayOfint = new ArrayList<Long>(); for (Long id : idList) {
	 * Task tempEntity = DataProvider.getTask(context, id, false); if (tempEntity != null
	 * && tempEntity.getServerId() != null && tempEntity.getServerId() != 0) {
	 * arrayOfint.add(tempEntity.getServerId()); } } wsdlSyncEntity.Tasks = arrayOfint;
	 * idList = DataProvider.getLabelIdListForFile(context, syncUpEntity.getId(), false,
	 * true, false); arrayOfint = new ArrayList<Long>(); for (Long id : idList) { Label
	 * tempEntity = DataProvider.getLabel( context, id, false); if (tempEntity != null &&
	 * tempEntity.getServerId() != null && tempEntity.getServerId() != 0) {
	 * arrayOfint.add(tempEntity.getServerId()); } } wsdlSyncEntity.Labels = arrayOfint;
	 * idList = DataProvider.getContactIdListForFile(context, syncUpEntity.getId(), false,
	 * true, false); arrayOfint = new ArrayList<Long>(); for (Long id : idList) { Contact
	 * tempEntity = DataProvider.getContact(context, id, false); if (tempEntity != null &&
	 * tempEntity.getServerId() != null && tempEntity.getServerId() != 0) {
	 * arrayOfint.add(tempEntity.getServerId()); } } wsdlSyncEntity.Contacts = arrayOfint;
	 * } } } }); if (found.value) { return wsdlSyncEntity; } else { return null; } } */
	public static boolean insertOrReplaceLabelWithSyncEntityIfNewer(
			DaoSession daoSessionArg, final Context context,
			final biz.advancedcalendar.wsdl.sync.Label wsdlSyncEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final BooleanHolder bh = new BooleanHolder(false);
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				if (wsdlSyncEntity.Deleted) {
					DataProvider
							.deleteLabel(daoSession, context, wsdlSyncEntity.Id, true);
					bh.value = true;
				} else {
					Label localEntity = DataProvider.getLabel(daoSession, context,
							wsdlSyncEntity.Id.longValue(), true);
					if (localEntity == null
							|| localEntity.getSyncStatus() == SyncStatus.SYNCHRONIZED
									.getValue()
							|| localEntity.getSyncStatus() == SyncStatus.SYNC_DOWN_REQUIRED
									.getValue()
							|| localEntity.getLastMod() < wsdlSyncEntity.LastMod
									.getTime()) {
						// TODO
						// decide
						// <
						// or
						// <=
						LabelWithDependents entityWithDependencies = DataProvider
								.getLabelWithDependents(daoSession, context,
										localEntity == null ? null : localEntity.getId(),
										wsdlSyncEntity);
						DataProvider.insertOrReplaceLabelWithDependencies(daoSession,
								context, entityWithDependencies);
						bh.value = true;
					}
				}
			}
		});
		return bh.value;
	}

	static class BooleanWrapper {
		boolean value;
	}

	public static ArrayList<CalendarViewTaskOccurrence> selectTasksGoingIntoDateTimeInterval(
			Long borderStartDateTime,
			Long borderEndDateTime,
			ArrayList<CalendarViewTaskOccurrence> mPreallocatedTasksGoingIntoDateTimeInterval,
			List<CalendarViewTaskOccurrence> mWeekViewCoreTaskList) {
		mPreallocatedTasksGoingIntoDateTimeInterval.clear();
		Long taskStartDateTime, taskEndDateTime;
		CalendarViewTaskOccurrence weekViewCoreTask;
		for (int i = 0; mWeekViewCoreTaskList != null && i < mWeekViewCoreTaskList.size(); i++) {
			weekViewCoreTask = mWeekViewCoreTaskList.get(i);
			taskStartDateTime = weekViewCoreTask.StartDateTime;
			taskEndDateTime = weekViewCoreTask.EndDateTime;
			if (borderStartDateTime == null && borderEndDateTime == null) {
				mPreallocatedTasksGoingIntoDateTimeInterval.add(mWeekViewCoreTaskList
						.get(i));
				continue;
			}
			if (taskStartDateTime != null && taskEndDateTime == null
					&& borderStartDateTime != null && borderEndDateTime == null) {
				if (borderStartDateTime <= taskStartDateTime) {
					mPreallocatedTasksGoingIntoDateTimeInterval.add(mWeekViewCoreTaskList
							.get(i));
				}
				continue;
			}
			if (taskStartDateTime == null && taskEndDateTime != null
					&& borderStartDateTime != null && borderEndDateTime == null) {
				continue;
			}
			if (taskStartDateTime != null && taskEndDateTime != null
					&& borderStartDateTime != null && borderEndDateTime == null) {
				if (borderStartDateTime <= taskStartDateTime) {
					mPreallocatedTasksGoingIntoDateTimeInterval.add(mWeekViewCoreTaskList
							.get(i));
				}
				continue;
			}
			if (taskStartDateTime != null && taskEndDateTime == null
					&& borderStartDateTime == null && borderEndDateTime != null) {
				continue;
			}
			if (taskStartDateTime == null && taskEndDateTime != null
					&& borderStartDateTime == null && borderEndDateTime != null) {
				if (taskEndDateTime <= borderEndDateTime) {
					mPreallocatedTasksGoingIntoDateTimeInterval.add(mWeekViewCoreTaskList
							.get(i));
				}
				continue;
			}
			if (taskStartDateTime != null && taskEndDateTime != null
					&& borderStartDateTime == null && borderEndDateTime != null) {
				if (taskEndDateTime <= borderEndDateTime) {
					mPreallocatedTasksGoingIntoDateTimeInterval.add(mWeekViewCoreTaskList
							.get(i));
				}
				continue;
			}
			if ((taskStartDateTime == null || taskEndDateTime == null)
					&& borderStartDateTime != null && borderEndDateTime != null) {
				continue;
			}
			if (taskStartDateTime != null && taskEndDateTime != null
					&& borderStartDateTime != null && borderEndDateTime != null) {
				// check if task goes into the given time interval
				if (borderStartDateTime <= taskStartDateTime
						&& taskEndDateTime <= borderEndDateTime) {
					mPreallocatedTasksGoingIntoDateTimeInterval.add(mWeekViewCoreTaskList
							.get(i));
					continue;
				}
			}
		}
		return mPreallocatedTasksGoingIntoDateTimeInterval;
	}

	public static void runInTx(DaoSession daoSessionArg, final Context context, Runnable r) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(r);
	}

	public static <V> V callInTx(DaoSession daoSessionArg, final Context context,
			Callable<V> callable) throws Exception {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		return daoSession.callInTx(callable);
	}

	public static TaskWithDependents getTaskWithDependents(DaoSession daoSessionArg,
			final Context context, final Task task) {
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		final TaskWithDependents entityWithDependents = new TaskWithDependents();
		final BooleanHolder bh = new BooleanHolder(false);
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// check for consistency
				if (task != null) {
					Long id = task.getId();
					entityWithDependents.task = task;
					entityWithDependents.reminders = DataProvider.getReminderListOfTask(
							daoSession, context, id, true, true);
					entityWithDependents.taskOccurrences = DataProvider
							.getTaskOccurrenceListOfTask(daoSession, context, id);
					bh.value = true;
				}
			}
		});
		if (bh.value) {
			return entityWithDependents;
		} else {
			return null;
		}
	}

	public static TaskWithDependents getTaskWithDependents(DaoSession daoSessionArg,
			final Context context, final long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final TaskWithDependents entityWithDependents = new TaskWithDependents();
		final BooleanHolder bh = new BooleanHolder(false);
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// check for consistency
				Task localEntityByLocalId = DataProvider.getTask(daoSession, context, id,
						false);
				if (localEntityByLocalId == null) {
					return;
				} else {
					bh.value = true;
				}
				entityWithDependents.task = localEntityByLocalId;
				entityWithDependents.reminders = DataProvider.getReminderListOfTask(
						daoSession, context, id, true, true);
				entityWithDependents.taskOccurrences = DataProvider
						.getTaskOccurrenceListOfTask(daoSession, context, id);
			}
		});
		if (bh.value) {
			return entityWithDependents;
		} else {
			return null;
		}
	}

	public static TaskWithDependentsUiData3 getTaskWithDependents2(
			DaoSession daoSessionArg, final Context context, final long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final TaskWithDependents entityWithDependents = new TaskWithDependents();
		final BooleanHolder bh = new BooleanHolder(false);
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// check for consistency
				Task localEntityByLocalId = DataProvider.getTask(daoSession, context, id,
						false);
				if (localEntityByLocalId == null) {
					return;
				} else {
					bh.value = true;
				}
				entityWithDependents.task = localEntityByLocalId;
				entityWithDependents.reminders = DataProvider.getReminderListOfTask(
						daoSession, context, id, true, true);
				entityWithDependents.taskOccurrences = DataProvider
						.getTaskOccurrenceListOfTask(daoSession, context, id);
			}
		});
		if (bh.value) {
			final TaskWithDependentsUiData3 entityWithDependents2 = new TaskWithDependentsUiData3(
					entityWithDependents);
			return entityWithDependents2;
		} else {
			return null;
		}
	}

	public static LabelWithDependents getLabelWithDependencies(DaoSession daoSessionArg,
			final Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final LabelWithDependents entityWithDependencies = new LabelWithDependents();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// check for consistency
				Label localEntityByLocalId = DataProvider.getLabel(daoSession, context,
						id, false);
				if (localEntityByLocalId == null) {
					throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
							"Inconsistensy in getTaskWithDependencies(): "
									+ "The entity with id == " + id + " was not found");
				}
				// daoSession.getTaskDao().insertOrReplace(newEntity);
				entityWithDependencies.label = localEntityByLocalId;
				// TODO: verify related entities. Web-service returns deleted
				// related entities also.
				// prepare taskIdList
				ArrayList<Long> taskIdList = new ArrayList<Long>();
				localEntityByLocalId.resetTaskLabelList();
				for (TaskLabel relation : localEntityByLocalId.getTaskLabelList()) {
					taskIdList.add(relation.getTaskId());
				}
				entityWithDependencies.taskIdList = taskIdList;
				// entityWithDependencies.isServerTaskIdList = false;
				// prepare contactIdList
				ArrayList<Long> contactIdList = new ArrayList<Long>();
				localEntityByLocalId.resetLabelContactList();
				for (LabelContact relation : localEntityByLocalId.getLabelContactList()) {
					contactIdList.add(relation.getContactId());
				}
				entityWithDependencies.contactIdList = contactIdList;
				// entityWithDependencies.isServerContactIdList = false;
				// prepare fileIdList
				ArrayList<Long> fileIdList = new ArrayList<Long>();
				localEntityByLocalId.resetFileLabelList();
				for (FileLabel relation : localEntityByLocalId.getFileLabelList()) {
					fileIdList.add(relation.getFileId());
				}
				entityWithDependencies.fileIdList = fileIdList;
				// entityWithDependencies.isServerFileIdList = false;
			}
		});
		return entityWithDependencies;
	}

	public static ContactWithDependents getContactWithDependencies(
			DaoSession daoSessionArg, final Context context, final Long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ContactWithDependents entityWithDependencies = new ContactWithDependents();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// check for consistency
				Contact localEntityByLocalId = DataProvider.getContact(daoSession,
						context, id, false);
				if (localEntityByLocalId == null) {
					throw new DatabaseStateAndNewEntityArgumentsInconsistencyException(
							"Inconsistensy in getTaskWithDependencies(): "
									+ "The entity with id == " + id + " was not found");
				}
				// daoSession.getTaskDao().insertOrReplace(newEntity);
				entityWithDependencies.contact = localEntityByLocalId;
				// TODO: verify related entities. Web-service returns deleted
				// related entities also.
				// prepare taskIdList
				ArrayList<Long> taskIdList = new ArrayList<Long>();
				localEntityByLocalId.resetTaskContactList();
				for (TaskContact relation : localEntityByLocalId.getTaskContactList()) {
					taskIdList.add(relation.getTaskId());
				}
				entityWithDependencies.taskIdList = taskIdList;
				// entityWithDependencies.isServerTaskIdList = false;
				// prepare contactIdList
				ArrayList<Long> labelIdList = new ArrayList<Long>();
				localEntityByLocalId.resetLabelContactList();
				for (LabelContact relation : localEntityByLocalId.getLabelContactList()) {
					labelIdList.add(relation.getLabelId());
				}
				entityWithDependencies.labelIdList = labelIdList;
				// entityWithDependencies.isServerLabelIdList = false;
				// prepare fileIdList
				ArrayList<Long> fileIdList = new ArrayList<Long>();
				localEntityByLocalId.resetFileContactList();
				for (FileContact relation : localEntityByLocalId.getFileContactList()) {
					fileIdList.add(relation.getFileId());
				}
				entityWithDependencies.fileIdList = fileIdList;
				// entityWithDependencies.isServerFileIdList = false;
			}
		});
		return entityWithDependencies;
	}

	private static void checkAndFixDuplicateContactServerId(DaoSession daoSessionArg,
			final Context context, final Integer contactID) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Contact localEntityByServerId = DataProvider.getContact(daoSession,
						context, contactID.longValue(), true);
				if (localEntityByServerId == null) {
					return;
				}
				ContactWithDependents entityWithDependencies = DataProvider
						.getContactWithDependencies(daoSession, context,
								localEntityByServerId.getId());
				entityWithDependencies.contact.setServerId(null);
				entityWithDependencies.contact.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED
						.getValue());
				DataProvider.insertOrReplaceContactWithDependents(daoSession, context,
						entityWithDependencies);
			}
		});
	}

	// public static biz.advancedcalendar.wsdl.sync.Message getWsdlSyncMessage(
	// Context context, Long id) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	public static void eliminateServerTaskId(DaoSession daoSessionArg,
			final Context context, final Long serverId, final boolean eliminateOnSubtree) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// final TaskDao entityDao = daoSession.getTaskDao();
				// QueryBuilder<Task> qb = entityDao.queryBuilder();
				Task root = DataProvider.getTask(daoSession, context, serverId, true);
				if (root == null) {
					return;
				}
				ArrayList<TreeViewListItemDescription> taskTree;
				if (eliminateOnSubtree) {
					taskTree = DataProvider.getTaskTree(daoSession, context,
							root.getId(), null, true, true, true, true);
				} else {
					taskTree = new ArrayList<TreeViewListItemDescription>(1);
					taskTree.add(new TreeViewListItemDescriptionTaskImpl(root));
				}
				for (TreeViewListItemDescription treeViewListItemDescription : taskTree) {
					Task entity = DataProvider.getTask(daoSession, context,
							treeViewListItemDescription.getId(), false);
					entity.setServerId(null);
					entity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED.getValue());
					DataProvider.insertOrReplaceTask(daoSession, context, entity, false);
				}
			}
		});
	}

	public static void eliminateServerLabelId(DaoSession daoSessionArg, Context context,
			final Long serverId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final LabelDao entityDao = daoSession.getLabelDao();
				QueryBuilder<Label> qb = entityDao.queryBuilder();
				List<Label> entityList = qb.where(
						LabelDao.Properties.ServerId.eq(serverId)).listLazyUncached();
				for (Label entity : entityList) {
					if (entity.getServerId() == serverId) {
						entity.setServerId(null);
					}
					if (entity.getServerId() == null) {
						entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
					} else {
						if (entity.getSyncStatus() == SyncStatus.SYNCHRONIZED.getValue()) {
							entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
						}
					}
					entityDao.update(entity);
				}
			}
		});
	}

	public static void eliminateServerContactId(DaoSession daoSessionArg,
			Context context, final Long serverId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final ContactDao entityDao = daoSession.getContactDao();
				QueryBuilder<Contact> qb = entityDao.queryBuilder();
				List<Contact> entityList = qb.where(
						ContactDao.Properties.ServerId.eq(serverId)).listLazyUncached();
				for (Contact entity : entityList) {
					if (entity.getServerId() == serverId) {
						entity.setServerId(null);
					}
					if (entity.getServerId() == null) {
						entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
					} else {
						if (entity.getSyncStatus() == SyncStatus.SYNCHRONIZED.getValue()) {
							entity.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
						}
					}
					entityDao.update(entity);
				}
			}
		});
	}

	public static void eliminateServerReminderId(DaoSession daoSessionArg,
			Context context, final List<Long> serverIdList) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final ReminderDao entityDao = daoSession.getReminderDao();
				QueryBuilder<Reminder> qb = entityDao.queryBuilder();
				List<Reminder> entityList = qb.where(
						ReminderDao.Properties.ServerId.in(serverIdList))
						.listLazyUncached();
				for (Reminder entity : entityList) {
					entity.setServerId(null);
				}
				entityDao.updateInTx(entityList);
			}
		});
		// TODO Auto-generated method stub
	}

	public static boolean isSyncUpRequiredForTask(DaoSession daoSessionArg,
			final Context context, final Long id, final TaskDto taskDto) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final BooleanWrapper isSyncUpRequired = new BooleanWrapper();
		isSyncUpRequired.value = false;
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// Task localEntity = DataProvider.getTask(context, id, false);
				Long localEntityServerParentId = DataProvider.getServerParentIdForTask(
						daoSession, context, id);
				if (localEntityServerParentId != null
						&& taskDto.ParentId == null
						|| localEntityServerParentId == null
						&& taskDto.ParentId != null
						|| localEntityServerParentId != null
						&& taskDto.ParentId != null
						&& localEntityServerParentId.longValue() != taskDto.ParentId
								.longValue()) {
					isSyncUpRequired.value = true;
					return;
				}
				// List<Long> localIdList;
				// List<Long> serverIdList;
				// localIdList = DataProvider.getLabelIdListForTask(context,
				// localEntity.getId(), false, true, false);
				// serverIdList = wsdlSyncEntity.Labels;
				// if (localIdList.size() != serverIdList.size()) {
				// isSyncUpRequired.value = true;
				// return;
				// }
				// for (Long id : localIdList) {
				// Label tempEntity = DataProvider.getLabel(context, id, false);
				// if (tempEntity.getServerId() == null
				// || !serverIdList.contains(tempEntity.getServerId())) {
				// isSyncUpRequired.value = true;
				// return;
				// }
				// }
				// // wsdlSyncUpEntity.Labels = serverIdList;
				// localIdList = DataProvider.getContactIdListForTask(context,
				// localEntity.getId(), false, true, false);
				// // serverIdList = wsdlSyncEntity.Contacts;
				// if (localIdList.size() != serverIdList.size()) {
				// isSyncUpRequired.value = true;
				// return;
				// }
				// for (Long id : localIdList) {
				// Contact tempEntity = DataProvider.getContact(context, id, false);
				// if (tempEntity.getServerId() == null
				// || !serverIdList.contains(tempEntity.getServerId())) {
				// isSyncUpRequired.value = true;
				// return;
				// }
				// }
				// localIdList = DataProvider.getFileIdListForTask(context,
				// localEntity.getId(), false, true, false);
				// // serverIdList = wsdlSyncEntity.Files;
				// if (localIdList.size() != serverIdList.size()) {
				// isSyncUpRequired.value = true;
				// return;
				// }
				// for (Long id : localIdList) {
				// File tempEntity = DataProvider.getFile(context, id);
				// if (tempEntity.getServerId() == null
				// || !serverIdList.contains(tempEntity.getServerId())) {
				// isSyncUpRequired.value = true;
				// return;
				// }
				// }
			}
		});
		return isSyncUpRequired.value;
	}

	public static boolean isSyncUpRequiredForLabel(DaoSession daoSessionArg,
			final Context context, final Long id,
			final biz.advancedcalendar.wsdl.sync.Label wsdlSyncEntity) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final BooleanWrapper isSyncUpRequired = new BooleanWrapper();
		isSyncUpRequired.value = false;
		final biz.advancedcalendar.wsdl.sync.Label wsdlSyncUpEntity = new biz.advancedcalendar.wsdl.sync.Label();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Label localEntity = DataProvider.getLabel(daoSession, context, id, false);
				if (localEntity == null) {
					isSyncUpRequired.value = true;
				} else {
					Long serverParentIdForEntity = DataProvider
							.getServerParentIdForLabel(daoSession, context,
									localEntity.getId());
					if (serverParentIdForEntity != null
							&& wsdlSyncEntity.ParentId != null
							&& wsdlSyncEntity.ParentId.longValue() != serverParentIdForEntity
									.longValue()) {
						isSyncUpRequired.value = true;
						return;
					}
					List<Long> localIdList;
					List<Long> serverIdList;
					localIdList = DataProvider.getTaskIdListForLabel(daoSession, context,
							localEntity.getId(), false, true, false, true, true);
					serverIdList = wsdlSyncEntity.Tasks;
					if (localIdList.size() != serverIdList.size()) {
						isSyncUpRequired.value = true;
						return;
					}
					for (Long id : localIdList) {
						Task tempEntity = DataProvider.getTask(daoSession, context, id,
								false);
						if (tempEntity.getServerId() == null
								|| !serverIdList.contains(tempEntity.getServerId())) {
							isSyncUpRequired.value = true;
							return;
						}
					}
					wsdlSyncUpEntity.Tasks = serverIdList;
					localIdList = DataProvider.getContactIdListForLabel(daoSession,
							context, localEntity.getId(), false, true, false);
					serverIdList = wsdlSyncEntity.Contacts;
					if (localIdList.size() != serverIdList.size()) {
						isSyncUpRequired.value = true;
						return;
					}
					for (Long id : localIdList) {
						Contact tempEntity = DataProvider.getContact(daoSession, context,
								id, false);
						if (tempEntity.getServerId() == null
								|| !serverIdList.contains(tempEntity.getServerId())) {
							isSyncUpRequired.value = true;
							return;
						}
					}
					localIdList = DataProvider.getFileIdListForLabel(daoSession, context,
							localEntity.getId(), false, true, false);
					serverIdList = wsdlSyncEntity.Files;
					if (localIdList.size() != serverIdList.size()) {
						isSyncUpRequired.value = true;
						return;
					}
					for (Long id : localIdList) {
						biz.advancedcalendar.greendao.File tempEntity = DataProvider
								.getFile(daoSession, context, id);
						if (tempEntity.getServerId() == null
								|| !serverIdList.contains(tempEntity.getServerId())) {
							isSyncUpRequired.value = true;
							return;
						}
					}
				}
			}
		});
		return isSyncUpRequired.value;
	}

	public static ArrayList<Long> getLabelIdListForDiaryRecord(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getDiaryRecordId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ DiaryRecordLabelDao.Properties.DiaryRecordId.columnName + " = "
						+ id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + LabelDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<DiaryRecordLabel> list = ((Global) context
						.getApplicationContext())
						.getDaoSession()
						.getDiaryRecordLabelDao()
						.queryRawCreate(
								"JOIN "
										+ LabelDao.TABLENAME
										+ " T1 ON T."
										+ DiaryRecordLabelDao.Properties.DiaryRecordId.columnName
										+ " = T1."
										+ DiaryRecordDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (DiaryRecordLabel entity : list) {
					selectedIds.add(entity.getLabelId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static ArrayList<Long> getDiaryRecordIdListForLabel(DaoSession daoSessionArg,
			final Context context, final Long id1, final boolean isServerId,
			final boolean includeNonDeleted, final boolean includeDeleted) {
		final ArrayList<Long> selectedIds = new ArrayList<Long>();
		if (id1 == null) {
			return selectedIds;
		}
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Long id = null;
				if (isServerId) {
					id = DataProvider.getLabelId(daoSession, context, id1);
				}
				if (id == null) {
					return;
				}
				StringBuilder sb = new StringBuilder("T."
						+ DiaryRecordLabelDao.Properties.LabelId.columnName + " = " + id);
				if (includeNonDeleted && includeDeleted) {
				} else if (includeNonDeleted && !includeDeleted) {
					sb.append(" AND T1." + DiaryRecordDao.Properties.Deleted.columnName
							+ " = 0");
				} else if (!includeNonDeleted && includeDeleted) {
					sb.append(" AND T1." + DiaryRecordDao.Properties.Deleted.columnName
							+ " = 1");
				} else if (!includeNonDeleted && !includeDeleted) {
					sb.append(" AND 1 <> 1");
				}
				// QueryBuilder.LOG_SQL = true;
				// QueryBuilder.LOG_VALUES = true;
				LazyList<DiaryRecordLabel> list = ((Global) context
						.getApplicationContext())
						.getDaoSession()
						.getDiaryRecordLabelDao()
						.queryRawCreate(
								"JOIN "
										+ DiaryRecordDao.TABLENAME
										+ " T1 ON T."
										+ DiaryRecordLabelDao.Properties.LabelId.columnName
										+ " = T1." + LabelDao.Properties.Id.columnName
										+ " WHERE " + sb.toString()).listLazyUncached();
				for (DiaryRecordLabel entity : list) {
					selectedIds.add(entity.getDiaryRecordId());
				}
				list.close();
			}
		});
		return selectedIds;
	}

	public static void refreshAndDetach(DaoSession daoSessionArg, final Context context,
			Task entity) {
		entity.refresh();
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		TaskDao dao = daoSession.getTaskDao();
		dao.detach(entity);
	}

	public static void refreshAndDetach(DaoSession daoSessionArg, Context context,
			List<Reminder> reminderList) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		ReminderDao dao = daoSession.getReminderDao();
		for (Reminder entity : reminderList) {
			entity.refresh();
			dao.detach(entity);
		}
	}

	public static void refreshAndDetachDailyRepetitionList(DaoSession daoSessionArg,
			Context context, List<TaskOccurrence> entityList) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskOccurrenceDao dao = daoSession.getTaskOccurrenceDao();
		for (TaskOccurrence entity : entityList) {
			entity.refresh();
			dao.detach(entity);
		}
	}

	// public static void refreshAndDetachMonthlyRepetitionsByDateList(Context context,
	// List<MonthlyRepetitionsByDate> entityList) {
	// for (MonthlyRepetitionsByDate entity : entityList) {
	// entity.refresh();
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDateDao dao = daoSession.getMonthlyRepetitionsByDateDao();
	// dao.detach(entity);
	// }
	// }
	// public static void refreshAndDetachMonthlyRepetitionsByDayOfWeekList(Context
	// context,
	// List<MonthlyRepetitionsByDayOfWeek> entityList) {
	// for (MonthlyRepetitionsByDayOfWeek entity : entityList) {
	// entity.refresh();
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// MonthlyRepetitionsByDayOfWeekDao dao = daoSession
	// .getMonthlyRepetitionsByDayOfWeekDao();
	// dao.detach(entity);
	// }
	// }
	public static void deleteScheduledReminder(DaoSession daoSessionArg,
			final Context context, final long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final ElapsedReminderDao elapsedReminderDao = daoSession
						.getElapsedReminderDao();
				List<ElapsedReminder> elapsedReminders = elapsedReminderDao
						.queryBuilder()
						.where(ElapsedReminderDao.Properties.ScheduledReminderId.eq(id))
						.list();
				for (ElapsedReminder elapsedReminder : elapsedReminders) {
					elapsedReminder.setScheduledReminderId(null);
					elapsedReminderDao.insertOrReplace(elapsedReminder);
				}
				AlarmService.cancelScheduledAlarm(context, id);
				daoSession.getScheduledReminderDao().deleteByKey(id);
			}
		});
	}

	public static void deleteElapsedReminder(DaoSession daoSessionArg, Context context,
			final long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final ScheduledReminderDao scheduledReminderDao = daoSession
						.getScheduledReminderDao();
				List<ScheduledReminder> scheduledReminders = scheduledReminderDao
						.queryBuilder()
						.where(ScheduledReminderDao.Properties.ElapsedReminderId.eq(id))
						.list();
				for (ScheduledReminder scheduledReminder : scheduledReminders) {
					scheduledReminder.setElapsedReminderId(null);
					scheduledReminderDao.insertOrReplace(scheduledReminder);
				}
				daoSession.getElapsedReminderDao().deleteByKey(id);
			}
		});
	}

	public static void deleteElapsedReminders(DaoSession daoSessionArg,
			final Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				final ElapsedReminderDao elapsedReminderDao = daoSession
						.getElapsedReminderDao();
				List<ElapsedReminder> elapsedReminders = elapsedReminderDao
						.queryBuilder().list();
				for (ElapsedReminder elapsedReminder : elapsedReminders) {
					DataProvider.deleteElapsedReminder(daoSession, context,
							elapsedReminder.getId());
				}
			}
		});
	}

	public static List<ScheduledReminder> getScheduledReminders(DaoSession daoSessionArg,
			Context context, Long localReminderId, int[] states) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ScheduledReminderDao entityDao = daoSession.getScheduledReminderDao();
		QueryBuilder<ScheduledReminder> qb = entityDao.queryBuilder();
		List<ScheduledReminder> entityList = null;
		if (states == null) {
			if (localReminderId != null) {
				entityList = qb.where(
						ScheduledReminderDao.Properties.ReminderId.eq(localReminderId))
						.list();
			} else {
				entityList = qb.list();
			}
		} else {
			final List<Integer> stateList = new ArrayList<Integer>(states.length);
			for (int i = 0; i < states.length; i++) {
				stateList.add(states[i]);
			}
			if (localReminderId != null) {
				entityList = qb.where(
						ScheduledReminderDao.Properties.ReminderId.eq(localReminderId),
						ScheduledReminderDao.Properties.StateValue.in(stateList)).list();
			} else {
				entityList = qb.where(
						ScheduledReminderDao.Properties.StateValue.in(stateList)).list();
			}
		}
		return entityList;
	}

	public static List<ElapsedReminder> getElapsedReminders(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ElapsedReminderDao entityDao = daoSession.getElapsedReminderDao();
		QueryBuilder<ElapsedReminder> qb = entityDao.queryBuilder();
		List<ElapsedReminder> entityList = qb.list();
		return entityList;
	}

	public static List<ElapsedReminder> getElapsedRemindersForNotificationBar(
			DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ElapsedReminderDao entityDao = daoSession.getElapsedReminderDao();
		QueryBuilder<ElapsedReminder> qb = entityDao.queryBuilder();
		List<ElapsedReminder> entityList = qb.where(
				ElapsedReminderDao.Properties.ShowInNotifications.eq(true)).list();
		return entityList;
	}

	public static List<ScheduledReminder> getAlarmedAlarms(DaoSession daoSessionArg,
			Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		final ScheduledReminderDao entityDao = daoSession.getScheduledReminderDao();
		QueryBuilder<ScheduledReminder> qb = entityDao.queryBuilder();
		List<ScheduledReminder> entityList = qb
				.where(ScheduledReminderDao.Properties.StateValue.eq(ScheduledReminder.State.ALARMED
						.getValue()), ScheduledReminderDao.Properties.IsAlarm.eq(true))
				.list();
		return entityList;
	}

	// public static List<ScheduledReminder> selectScheduledReminderList(Context context,
	// int[] stateArray) {
	// final DaoSession daoSession = ((Global) context.getApplicationContext())
	// .getDaoSession();
	// ScheduledReminderDao dao = daoSession.getScheduledReminderDao();
	// // QueryBuilder.LOG_SQL = true;
	// // QueryBuilder.LOG_VALUES = true;
	// List<Integer> stateList = new ArrayList<Integer>(stateArray.length);
	// for (int i = 0; i < stateArray.length; i++) {
	// stateList.add(stateArray[i]);
	// }
	// Query<ScheduledReminder> query = dao.queryBuilder()
	// .where(ScheduledReminderDao.Properties.State.in(stateList)).build();
	// List<ScheduledReminder> localReminderList = query.list();
	// return localReminderList;
	// }
	static class MySpecialLongComparer {
		public static boolean equals(Long lhs, Long rhs) {
			if (lhs == null && rhs == null) {
				return true;
			}
			if (lhs != null && rhs == null || lhs == null && rhs != null) {
				return false;
			}
			return lhs.equals(rhs);
		}
	}

	static class MySpecialIntegerComparer {
		public static boolean equals(Integer lhs, Integer rhs) {
			if (lhs == null && rhs == null) {
				return true;
			}
			if (lhs != null && rhs == null || lhs == null && rhs != null) {
				return false;
			}
			return lhs.equals(rhs);
		}
	}

	static class MySpecialShortComparer {
		public static boolean equals(Short lhs, Short rhs) {
			if (lhs == null && rhs == null) {
				return true;
			}
			if (lhs != null && rhs == null || lhs == null && rhs != null) {
				return false;
			}
			return lhs.equals(rhs);
		}
	}

	public static List<biz.advancedcalendar.greendao.Calendar> getCalendars(
			DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		CalendarDao dao = daoSession.getCalendarDao();
		QueryBuilder<biz.advancedcalendar.greendao.Calendar> qb2 = dao.queryBuilder();
		return qb2.list();
	}

	public static void insertOrReplaceCalendar(DaoSession daoSessionArg, Context context,
			Calendar calendar) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		CalendarDao dao = daoSession.getCalendarDao();
		dao.insertOrReplace(calendar);
	}

	public static void deleteCalendar(DaoSession daoSessionArg, Context context,
			final long id) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				CalendarDao calendarDao = daoSession.getCalendarDao();
				Calendar calendar = calendarDao.load(id);
				calendarDao.delete(calendar);
				// move tasks to the default calendar
				final TaskDao taskDao = daoSession.getTaskDao();
				List<Task> tasks = taskDao.queryBuilder()
						.where(TaskDao.Properties.CalendarId.eq(id)).list();
				for (Task task : tasks) {
					task.setCalendarId(null);
				}
				taskDao.updateInTx(tasks);
			}
		});
	}

	public static List<biz.advancedcalendar.greendao.Task> getNonDeletedTasks(
			DaoSession daoSessionArg, Context context, Long calendarId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		TaskDao dao = daoSession.getTaskDao();
		QueryBuilder<biz.advancedcalendar.greendao.Task> qb2 = dao.queryBuilder().where(
				TaskDao.Properties.CalendarId.eq(calendarId));
		return qb2.list();
	}

	public static void moveTasks(DaoSession daoSessionArg, final Context context,
			final List<Long> taskIds, final Long calendarId) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				// move tasks
				final TaskDao taskDao = daoSession.getTaskDao();
				List<Task> tasks = taskDao.queryBuilder()
						.where(TaskDao.Properties.Id.in(taskIds)).list();
				for (Task task : tasks) {
					task.setCalendarId(calendarId);
				}
				taskDao.updateInTx(tasks);
			}
		});
	}

	public static List<Task> getNonDeletedTasks(DaoSession daoSessionArg,
			Context context, List<Long> calendarIds, boolean selectActive,
			boolean selectCompleted) {
		if (selectActive && !selectCompleted) {
			return new ArrayList<Task>();
		}
		if (daoSessionArg == null) {
			daoSessionArg = ((Global) context.getApplicationContext()).getDaoSession();
		}
		TaskDao dao = daoSessionArg.getTaskDao();
		QueryBuilder<biz.advancedcalendar.greendao.Task> qb2 = dao.queryBuilder();
		boolean includeDefault = false;
		List<Long> ids = new ArrayList<Long>(calendarIds.size());
		for (Long id : calendarIds) {
			if (id != null) {
				ids.add(id);
			} else {
				includeDefault = true;
			}
		}
		WhereCondition where = TaskDao.Properties.CalendarId.in(ids);
		if (includeDefault) {
			where = qb2.or(TaskDao.Properties.CalendarId.isNull(), where);
		}
		WhereCondition where2 = null;
		if (selectActive && !selectCompleted) {
			where2 = TaskDao.Properties.IsCompleted.eq(0);
		} else if (selectCompleted && !selectActive) {
			where2 = TaskDao.Properties.IsCompleted.eq(1);
		}
		if (where2 != null) {
			qb2.where(where, where2, TaskDao.Properties.Deleted.eq(0));
		} else {
			qb2.where(where, TaskDao.Properties.Deleted.eq(0));
		}
		return qb2.list();
	}

	public static List<Task> getDeletedTasks(DaoSession daoSessionArg, Context context) {
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		return daoSession.getTaskDao().queryBuilder()
				.where(TaskDao.Properties.Deleted.eq(1)).list();
	}

	public static void undeleteTask(final DaoSession daoSessionArg,
			final Context context, final long id) {
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				Task entity = DataProvider.getTask(daoSession, context, id, false);
				if (entity == null) {
					return;
				}
				if (entity.getDeleted()) {
					if (entity.getParentId() != null) {
						DataProvider.undeleteTask(daoSessionArg, context,
								entity.getParentId());
					}
					entity.setDeleted(false);
					DataProvider.insertOrReplaceTask(daoSession, context, entity, true);
					TaskWithDependents taskWithDependents = DataProvider
							.getTaskWithDependents(daoSession, context, entity);
					DataProvider.insertOrReplaceTaskWithDependents(daoSession, context,
							taskWithDependents, true);
				}
			}
		});
	}

	private static List<Task> getAllTasks(DaoSession daoSession, Context context,
			List<Long> calendarIds) {
		if (daoSession == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		}
		if (calendarIds == null) {
			List<Calendar> calendars = DataProvider.getCalendars(daoSession, context);
			calendars.add(0, new biz.advancedcalendar.greendao.Calendar(null, null,
					context.getResources().getString(R.string.default_calendar_name)));
			calendarIds = new ArrayList<Long>(calendars.size());
			for (Calendar calendar : calendars) {
				calendarIds.add(calendar.getId());
			}
		}
		TaskDao dao = daoSession.getTaskDao();
		QueryBuilder<biz.advancedcalendar.greendao.Task> qb2 = dao.queryBuilder();
		boolean includeDefault = false;
		List<Long> ids = new ArrayList<Long>(calendarIds.size());
		for (Long id : calendarIds) {
			if (id != null) {
				ids.add(id);
			} else {
				includeDefault = true;
			}
		}
		WhereCondition where = TaskDao.Properties.CalendarId.in(ids);
		if (includeDefault) {
			where = qb2.or(TaskDao.Properties.CalendarId.isNull(), where);
		}
		qb2.where(where);
		return qb2.list();
	}

	public static List<TaskUiData3> getTasksFromBackupFile(final Context context,
			String path) throws SQLiteException, DowngradeException, IOException {
		File tempFile = null;
		final List<TaskUiData3> taskWithDependentsList;
		try {
			tempFile = File.createTempFile("tmp", null, context.getCacheDir());
			File backupFile = new File(path);
			Helper.copy(backupFile, tempFile);
			DevOpenHelper2 helper = new DaoMaster2.DevOpenHelper2(context,
					tempFile.getAbsolutePath(), null);
			SQLiteDatabase db = helper.getWritableDatabase();
			DaoMaster daoMaster = new DaoMaster(db);
			final DaoSession daoSession = daoMaster.newSession();
			taskWithDependentsList = DataProvider.getTaskUiData3ListOfAllTasks(
					daoSession, context);
			db.close();
		} finally {
			if (tempFile != null) {
				boolean deleted = tempFile.delete();
				if (!deleted) {
					Log.d("Alarmer", "tempFile.delete() == " + deleted);
				}
			}
		}
		return taskWithDependentsList;
	}

	public static List<TaskUiData3> getTaskUiData3ListOfAllTasks(
			final DaoSession daoSessionArg, final Context context) {
		final ArrayList<TaskUiData3> taskWithDependentsList = new ArrayList<TaskUiData3>();
		final DaoSession daoSession;
		if (daoSessionArg == null) {
			daoSession = ((Global) context.getApplicationContext()).getDaoSession();
		} else {
			daoSession = daoSessionArg;
		}
		daoSession.runInTx(new Runnable() {
			@Override
			public void run() {
				List<Task> tasks = DataProvider.getAllTasks(daoSession, context, null);
				for (Task task : tasks) {
					TaskUiData2 taskUiData2 = new TaskUiData2(DataProvider
							.getTaskWithDependents(daoSession, context, task.getId()));
					Task parentTask = DataProvider.getTask(daoSession, context,
							task.getParentId(), false);
					Task taskSibling = DataProvider.getSortOrderSibling(null,
							context.getApplicationContext(), task.getSortOrder(),
							task.getParentId());
					TaskUiData3 taskUiData3 = new TaskUiData3(taskUiData2,
							parentTask == null ? null : parentTask.getName(),
							taskSibling == null ? null : taskSibling.getName());
					taskWithDependentsList.add(taskUiData3);
				}
			}
		});
		return taskWithDependentsList;
	}

	public static List<Task> getNonDeletedWeeklyRecurrentTasks(DaoSession daoSession,
			Context context) {
		List<Task> tasks = DataProvider.getAllTasks(daoSession, context, null);
		List<Task> tasks2 = new ArrayList<Task>();
		for (Task task : tasks) {
			if (task.getRecurrenceIntervalValue() == RecurrenceInterval.WEEKS.getValue()) {
				tasks2.add(task);
			}
		}
		return tasks2;
	}
}
