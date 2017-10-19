package biz.advancedcalendar.sync;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.DatabaseStateAndNewEntityArgumentsInconsistencyException;
import biz.advancedcalendar.db.TaskWithDependents;
import biz.advancedcalendar.greendao.DaoSession;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.server.DateSerializer;
import biz.advancedcalendar.server.JSONHttpClient;
import biz.advancedcalendar.server.ServiceUrl;
import biz.advancedcalendar.server.serialisers.EntityTypeSerializer;
import biz.advancedcalendar.server.serialisers.GetEntityListResultSerializer;
import biz.advancedcalendar.server.serialisers.RecurrenceIntervalSerializer;
import biz.advancedcalendar.server.serialisers.ReminderTimeModeSerializer;
import biz.advancedcalendar.server.serialisers.SetEntityResultSerializer;
import biz.advancedcalendar.server.serialisers.TaskPrioritySerializer;
import biz.advancedcalendar.services.NotificationService;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.wsdl.sync.ArrayOfSyncEntityInfo;
import biz.advancedcalendar.wsdl.sync.ArrayOfint;
import biz.advancedcalendar.wsdl.sync.EntityData;
import biz.advancedcalendar.wsdl.sync.EntityListResponse;
import biz.advancedcalendar.wsdl.sync.Enums.EntityType;
import biz.advancedcalendar.wsdl.sync.Enums.GetEntityListResult;
import biz.advancedcalendar.wsdl.sync.Enums.SetEntityResult;
import biz.advancedcalendar.wsdl.sync.Enums.TaskPriority;
import biz.advancedcalendar.wsdl.sync.SetTaskResponse;
import biz.advancedcalendar.wsdl.sync.SyncEntityInfo;
import biz.advancedcalendar.wsdl.sync.TaskDto;
import biz.advancedcalendar.wsdl.sync.TasksListResponse;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SyncTask {
	// static BasicHttpBinding_ISyncService service = new BasicHttpBinding_ISyncService();
	static synchronized void syncUpAll(final Context context, final boolean isForceSync) {
		List<Long> syncUpIdList = DataProvider.getSyncUpTaskIdList(null, context);
		LinkedList<Long> syncUpIdLinkedList = new LinkedList<Long>();
		for (Long id : syncUpIdList) {
			syncUpIdLinkedList.add(id);
		}
		syncUpIdList = null;
		class Holder {
			boolean value = false;
			List<Task> entityList = new ArrayList<Task>();
		}
		while (syncUpIdLinkedList.size() > 0) {
			final Holder holder = new Holder();
			int k = 0;
			while (k < CommonConstants.SYNC_UP_MAX_BUNCH_SIZE
					&& syncUpIdLinkedList.size() > 0) {
				k++;
				final Long id = syncUpIdLinkedList.poll();
				final DaoSession daoSession = ((Global) context.getApplicationContext())
						.getDaoSession();
				daoSession.runInTx(new Runnable() {
					@Override
					public void run() {
						Task syncUpEntity = DataProvider
								.getTask(null, context, id, false);
						if (syncUpEntity == null) {
							return;
						}
						if (syncUpEntity.getDeleted()
								&& syncUpEntity.getServerId() == null) {
							DataProvider.deleteTaskPermanently(null, context,
									syncUpEntity.getId(), false);
							return;
						}
						if (syncUpEntity.getSyncStatusValue() == SyncStatus.SYNC_UP_REQUIRED
								.getValue()
								|| syncUpEntity.getSyncStatusValue() == SyncStatus.SYNCHRONIZED
										.getValue()
								&& isForceSync
								|| syncUpEntity.getDeleted()) {
							syncUpEntity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
									.getValue());
							holder.value = true;
							holder.entityList.add(syncUpEntity);
							daoSession.getTaskDao().detach(syncUpEntity);
						}
					}
				});
			}
			if (holder.value) {
				if (SyncTask.syncUpEntityList(context, holder.entityList)) {
					SyncTask.notifyAboutChanges(context);
				}
			}
		}
	}

	static class BooleanHolder {
		boolean value = false;

		public BooleanHolder(boolean value) {
			super();
			this.value = value;
		}
	}

	static synchronized boolean syncUpEntityList(final Context context,
			final List<Task> syncUpEntityList) {
		UserProfile userProfile = DataProvider.getUserProfile(null, context);
		final List<TaskDto> syncUpEntityDtoList = new ArrayList<TaskDto>();
		// TODO rename syncTaskHolderList to syncEntityHolderList
		final List<SyncTaskHolder> syncTaskHolderList = new ArrayList<SyncTaskHolder>();
		final List<Long> entitiesToDeleteIdAndLastModList = new ArrayList<Long>();
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				for (Task syncUpEntity : syncUpEntityList) {
					if (syncUpEntity.getDeleted()) {
						if (syncUpEntity.getServerId() != null) {
							entitiesToDeleteIdAndLastModList.add(syncUpEntity
									.getServerId());
							entitiesToDeleteIdAndLastModList.add(syncUpEntity
									.getLastMod());
						}
					} else {
						final TaskDtoWithDependents taskDtoHolder = DataProvider
								.getTaskDtoHolder(null, context, syncUpEntity.getId());
						syncUpEntityDtoList.add(taskDtoHolder.taskDto);
						SyncTaskHolder syncEntityHolder = new SyncTaskHolder(
								syncUpEntity, taskDtoHolder.reminderIds, syncUpEntity
										.getLocalChangeDateTime());
						syncTaskHolderList.add(syncEntityHolder);
					}
				}
			}
		});
		// delete entities
		final BooleanHolder bh = new BooleanHolder(true);
		for (int i = 0; i < entitiesToDeleteIdAndLastModList.size(); i += 2) {
			final long id = entitiesToDeleteIdAndLastModList.get(i);
			final long lastMod = entitiesToDeleteIdAndLastModList.get(i + 1);
			final HttpURLConnection httpURLConnection = JSONHttpClient
					.getResponseForDeleteEntity(context, id, lastMod,
							userProfile.getAuthToken());
			if (httpURLConnection == null) {
				return false;
			}
			int result = -1;
			try {
				result = httpURLConnection.getResponseCode();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(CommonConstants.DEBUG_TAG, "entityResponse == null.");
				return false;
			}
			switch (result) {
			case 200:
			case 404:
				final DaoSession daoSession = ((Global) context.getApplicationContext())
						.getDaoSession();
				daoSession.runInTx(new Runnable() {
					@Override
					public void run() {
						Task localEntity = DataProvider.getTask(null, context, id, true);
						if (localEntity != null && localEntity.getDeleted()) {
							DataProvider.deleteTaskPermanently(null, context, id, true);
							bh.value = true;
						}
					}
				});
				break;
			case 401:
				DataProvider.signOut(null, context);
				return false;
			default:
				return false;
			}
		}
		// push the entity list to the server
		if (syncUpEntityDtoList.size() == 0) {
			return bh.value;
		}
		HttpURLConnection httpURLConnection = null;
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization",
				String.format("Bearer %s", userProfile.getAuthToken()));
		JSONHttpClient httpClient = new JSONHttpClient();
		try {
			httpURLConnection = httpClient.getHttpURLConnectionForPostMethod(
					ServiceUrl.TASK_ARRAY, headers, syncUpEntityDtoList);
		} catch (Exception e2) {
		}
		if (httpURLConnection == null) {
			return false;
		}
		int result = -1;
		Long dateTime = null;
		try {
			result = httpURLConnection.getResponseCode();
			dateTime = null;
			// CommonConstants.RFC1123_DATE_TIME_FORMATTER.parseDateTime(
			// httpURLConnection.getHeaderField("Date")).getMillis();
			PreferenceManager
					.getDefaultSharedPreferences(context)
					.edit()
					.putLong(CommonConstants.TIME_SKEW,
							Calendar.getInstance().getTimeInMillis() - dateTime).commit();
		} catch (IOException e) {
			try {
				result = httpURLConnection.getResponseCode();
				dateTime = null;
				// CommonConstants.RFC1123_DATE_TIME_FORMATTER.parseDateTime(
				// httpURLConnection.getHeaderField("Date")).getMillis();
				PreferenceManager
						.getDefaultSharedPreferences(context)
						.edit()
						.putLong(CommonConstants.TIME_SKEW,
								Calendar.getInstance().getTimeInMillis() - dateTime)
						.commit();
				return false;
			} catch (IOException e1) {
				return false;
			}
		}
		switch (result) {
		case 200:
			break;
		case 401:
			DataProvider.signOut(null, context);
			return false;
		default:
			return false;
		}
		// Now we have got the list.
		final DaoSession daoSession = ((Global) context.getApplicationContext())
				.getDaoSession();
		List<SetTaskResponse> SetEntityResponseList = new ArrayList<SetTaskResponse>();
		try {
			InputStream inputStream = httpURLConnection.getInputStream();
			String resultString = JSONHttpClient.convertStreamToString(inputStream);
			inputStream.close();
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder
					.registerTypeAdapter(Date.class, new DateSerializer())
					//
					.registerTypeAdapter(SetEntityResult.class,
							new SetEntityResultSerializer())
					//
					.registerTypeAdapter(EntityType.class, new EntityTypeSerializer())
			//
			;
			//
			SetTaskResponse[] SyncEntityInfoArray = gsonBuilder.create().fromJson(
					resultString, SetTaskResponse[].class);
			// SetEntityResponse[] SyncEntityInfoArray = new GsonBuilder().create()
			// .fromJson(resultString, SetEntityResponse[].class);
			for (SetTaskResponse syncEntityInfo : SyncEntityInfoArray) {
				SetEntityResponseList.add(syncEntityInfo);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// final BooleanHolder bh = new BooleanHolder();
		for (int i = 0; i < syncTaskHolderList.size(); i++) {
			final biz.advancedcalendar.wsdl.sync.TaskDto syncUpEntityDto = syncUpEntityDtoList
					.get(i);
			final SyncTaskHolder syncUpEntityHolder = syncTaskHolderList.get(i);
			final Long localChangeDateTime = syncUpEntityHolder.localChangeDateTime;
			final Task syncUpEntity = syncUpEntityHolder.entity;
			final SetTaskResponse entityResponse = SetEntityResponseList.get(i);
			if (entityResponse == null) {
				Log.e(CommonConstants.DEBUG_TAG, "SetEntityResponse == null.");
				if (syncUpEntity.getDeleted()) {
					// DataProvider.deleteTask(context, syncUpEntity.getId(), false);
					// bh.value = true;
					// return true;
				}
				continue;
			}
			daoSession.runInTx(new Runnable() {
				@Override
				public void run() {
					Task localEntity = DataProvider.getTask(null, context,
							syncUpEntity.getId(), false);
					if (localEntity != null
							&& localEntity.getDeleted()
							&& (entityResponse.Result == SetEntityResult.ENTITY_IS_DELETED || entityResponse.Result == SetEntityResult.ENTITY_IS_NOT_FOUND)) {
						DataProvider.deleteTaskPermanently(null, context,
								syncUpEntity.getId(), false);
						bh.value = true;
						return;
					} else if (localEntity != null
							&& localEntity.getSyncStatusValue() == SyncStatus.SYNC_UP_REQUIRED
									.getValue()
							&& localEntity.getLocalChangeDateTime() == localChangeDateTime) {
						switch (entityResponse.Result) {
						case ENTITY_ON_THE_SERVER_IS_NEWER:
							localEntity.setSyncStatusValue(SyncStatus.SYNC_DOWN_REQUIRED
									.getValue());
							// localEntity.setLastMod(entityResponse.LastMod.getTime());
							DataProvider.insertOrReplaceTask(null, context, localEntity,
									false);
							bh.value = true;
							return;
						case SAVED:
							if (localEntity.getServerId() == null) {
								localEntity.setServerId(entityResponse.EntityId);
							}
							if (!DataProvider.isSyncUpRequiredForTask(null, context,
									syncUpEntity.getId(), syncUpEntityDto)
									&& (entityResponse.NotFoundRelatedEntities == null || entityResponse.NotFoundRelatedEntities
											.size() == 0)
									&& !(syncUpEntityDto.ParentId == null && localEntity
											.getParentId() != null)) {
								localEntity.setSyncStatusValue(SyncStatus.SYNCHRONIZED
										.getValue());
							} else {
								localEntity
										.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
												.getValue());
								localEntity.setLastMod(localEntity.getLastMod() + 1);
								if (entityResponse.NotFoundRelatedEntities != null
										&& entityResponse.NotFoundRelatedEntities.size() >= 0) {
									List<Long> notFoundRelatedRemindersServerIdList = new ArrayList<Long>();
									for (EntityData entityData : entityResponse.NotFoundRelatedEntities) {
										if (entityData.entityType
												.equals(EntityType.REMINDER)) {
											notFoundRelatedRemindersServerIdList
													.add(entityData.id);
										}
									}
									if (notFoundRelatedRemindersServerIdList.size() > 0) {
										DataProvider.eliminateServerReminderId(null,
												context,
												notFoundRelatedRemindersServerIdList);
									}
								}
							}
							if (entityResponse.ReminderIds != null) {
								for (int j = 0; j < syncUpEntityHolder.localReminderIds.length
										&& j < syncUpEntityDto.Reminders.size()
										&& j < entityResponse.ReminderIds.length; j++) {
									if (syncUpEntityDto.Reminders.get(j).Id == null) {
										Reminder reminder = DataProvider.getReminder(
												null, context,
												syncUpEntityHolder.localReminderIds[j]);
										if (reminder != null) {
											if (reminder.getServerId() == null) {
												reminder.setServerId(entityResponse.ReminderIds[j]);
												DataProvider.insertOrReplaceReminder(
														null, context, reminder);
											}
										}
									}
								}
							}
							// localEntity.setLastMod(entityResponse.LastMod.getTime());
							DataProvider.insertOrReplaceTask(null, context, localEntity,
									false);
							bh.value = true;
							return;
						case PARENT_ENTITY_IS_NOT_FOUND:
							DataProvider.eliminateServerTaskId(null, context,
									syncUpEntityDto.ParentId, false);
							bh.value = true;
							return;
						case ENTITY_IS_DELETED:
							if (entityResponse.LastMod == null
									|| entityResponse.LastMod.getTime() < syncUpEntity
											.getLastMod()) {
								DataProvider.eliminateServerTaskId(null, context,
										localEntity.getServerId(), true);
								localEntity
										.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
												.getValue());
								// localEntity.setLastMod(localEntity.getLastMod() + 1);
								DataProvider.insertOrReplaceTask(null, context,
										localEntity, false);
							} else {
								DataProvider.deleteTaskPermanently(null, context,
										syncUpEntity.getId(), false);
							}
							bh.value = true;
							return;
						case ENTITY_IS_NOT_FOUND:
						case ACCESS_DENIED:
							DataProvider.eliminateServerTaskId(null, context,
									localEntity.getServerId(), false);
							localEntity.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
									.getValue());
							// localEntity.setLastMod(localEntity.getLastMod() + 1);
							DataProvider.insertOrReplaceTask(null, context, localEntity,
									false);
							return;
						case ENTITY_HAS_INCORRECT_DATA:
							// TODO
							return;
						case PARENT_ENTITY_IS_DELETED:
							if (entityResponse.LastMod == null
									|| entityResponse.LastMod.getTime() < syncUpEntity
											.getLastMod()) {
								DataProvider.eliminateServerTaskId(null, context,
										syncUpEntityDto.ParentId, false);
								localEntity
										.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
												.getValue());
								// localEntity.setLastMod(localEntity.getLastMod() + 1);
								DataProvider.insertOrReplaceTask(null, context,
										localEntity, false);
							} else {
								DataProvider.deleteTaskPermanently(null, context,
										syncUpEntityDto.ParentId, true);
								localEntity.setParentId(null);
								localEntity.setServerId(null);
								localEntity.setSyncStatusValue(SyncStatus.SYNCHRONIZED
										.getValue());
								localEntity.setLastMod(entityResponse.LastMod.getTime());
								DataProvider.insertOrReplaceTask(null, context,
										localEntity, false);
								bh.value = true;
							}
						}
					}
				}
			});
		}
		return bh.value;
	}

	static synchronized void syncDownAll(final Context context, final boolean isForceSync) {
		// Get data from the incoming Intent
		Long mSyncDownDateTime = null;
		if (!isForceSync) {
			mSyncDownDateTime = Helper.getLongPreferenceValue(context,
					R.string.preference_key_tasks_next_sync_down_datetime, 0, null, null);
		}
		UserProfile userProfile = DataProvider.getUserProfile(null, context);
		long mNextSyncDownDateTime = new Date().getTime();
		HttpURLConnection httpURLConnection = SyncTask
				.getEntityListResponseForGetSyncEntitiesList(mSyncDownDateTime,
						userProfile.getAuthToken());
		if (httpURLConnection == null) {
			return;
		}
		int result = -1;
		try {
			result = httpURLConnection.getResponseCode();
		} catch (IOException e) {
			try {
				result = httpURLConnection.getResponseCode();
			} catch (IOException e1) {
				e.printStackTrace();
				Log.e(CommonConstants.DEBUG_TAG, "entityResponse == null.");
				return;
			}
		}
		switch (result) {
		case 401:
			DataProvider.signOut(null, context);
			return;
		case 200:
			break;
		default:
			return;
		}
		List<SyncEntityInfo> SyncEntityInfoList = new ArrayList<SyncEntityInfo>();
		try {
			InputStream inputStream = httpURLConnection.getInputStream();
			String resultString = JSONHttpClient.convertStreamToString(inputStream);
			inputStream.close();
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer())
					.registerTypeAdapter(GetEntityListResult.class,
							new GetEntityListResultSerializer());
			EntityListResponse entityListResponse = gsonBuilder.create().fromJson(
					resultString, EntityListResponse.class);
			ArrayOfSyncEntityInfo SyncEntityInfoArray = entityListResponse.SyncEntities;
			for (SyncEntityInfo syncEntityInfo : SyncEntityInfoArray) {
				SyncEntityInfoList.add(syncEntityInfo);
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Now we have got the list.
		// long mNextSyncDownDateTime;
		// try {
		// mNextSyncDownDateTime =
		// DateUtils.parseDate(httpResponse.getFirstHeader("Date").getValue()).getTime();
		// } catch (DateParseException e1) {
		// mNextSyncDownDateTime = new Date().getTime();
		// return;
		// }
		// rearrange SyncEntities so that any parent is before its children
		// TODO Generalize SyncEntityInfoComparator class
		class SyncEntityInfoComparator implements
				Comparator<biz.advancedcalendar.wsdl.sync.TaskDto> {
			List<TaskDto> Entities;

			public SyncEntityInfoComparator(List<TaskDto> Entities) {
				this.Entities = Entities;
			}

			@Override
			public int compare(biz.advancedcalendar.wsdl.sync.TaskDto a,
					biz.advancedcalendar.wsdl.sync.TaskDto b) {
				if (isAncestry(a, b)) {
					return 1;
				} else if (isAncestry(b, a)) {
					return -1;
				} else {
					return 0;
				}
			}

			private boolean isAncestry(biz.advancedcalendar.wsdl.sync.TaskDto a,
					biz.advancedcalendar.wsdl.sync.TaskDto b) {
				Long parentEntityId = a.ParentId;
				biz.advancedcalendar.wsdl.sync.TaskDto ancestrySyncEntityInfo = null;
				for (biz.advancedcalendar.wsdl.sync.TaskDto syncObjectInfo : Entities) {
					if (syncObjectInfo.Id.equals(parentEntityId)) {
						ancestrySyncEntityInfo = syncObjectInfo;
						parentEntityId = ancestrySyncEntityInfo.ParentId;
						break;
					}
				}
				while (ancestrySyncEntityInfo != null && ancestrySyncEntityInfo != b) {
					ancestrySyncEntityInfo = null;
					for (biz.advancedcalendar.wsdl.sync.TaskDto syncObjectInfo : Entities) {
						if (syncObjectInfo.Id == parentEntityId) {
							ancestrySyncEntityInfo = syncObjectInfo;
							parentEntityId = ancestrySyncEntityInfo.ParentId;
							break;
						}
					}
				}
				if (ancestrySyncEntityInfo == b) {
					return true;
				} else {
					return false;
				}
			}
		}
		while (SyncEntityInfoList.size() > 0) {
			ArrayOfint syncEntityIdList = new ArrayOfint();
			int k = 0;
			while (k < CommonConstants.SYNC_DOWN_MAX_BUNCH_SIZE
					&& SyncEntityInfoList.size() > 0) {
				final SyncEntityInfo syncObjectInfo = SyncEntityInfoList
						.remove(SyncEntityInfoList.size() - 1);
				Task localEntity = DataProvider.getTask(null, context,
						syncObjectInfo.EntityId.longValue(), true);
				if (syncObjectInfo.Deleted) {
					DataProvider.runInTx(null, context, new Runnable() {
						@Override
						public void run() {
							Task localEntity = DataProvider.getTask(null, context,
									syncObjectInfo.EntityId.longValue(), true);
							if (localEntity != null) {
								if (syncObjectInfo.LastMod.getTime() > localEntity
										.getLastMod()
										|| syncObjectInfo.LastMod.getTime() == localEntity
												.getLastMod() && isForceSync) {
									DataProvider.deleteTaskPermanently(null, context,
											syncObjectInfo.EntityId.longValue(), true);
								} else {
									localEntity
											.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
													.getValue());
									DataProvider.insertOrReplaceTask(null, context,
											localEntity, false);
								}
							}
						}
					});
				} else {
					if (localEntity == null
							|| syncObjectInfo.LastMod.getTime() > localEntity
									.getLastMod()
							|| syncObjectInfo.LastMod.getTime() == localEntity
									.getLastMod()
							&& isForceSync
							|| localEntity.getSyncStatusValue() == SyncStatus.SYNC_DOWN_REQUIRED
									.getValue()) {
						syncEntityIdList.add(syncObjectInfo.EntityId);
						k++;
					}
				}
			}
			if (syncEntityIdList.size() == 0) {
				continue;
			}
			HttpURLConnection httpResponse2 = JSONHttpClient
					.getEntityListResponseForGetEntityList(context, syncEntityIdList,
							true, userProfile.getAuthToken());
			result = -1;
			try {
				result = httpResponse2.getResponseCode();
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(CommonConstants.DEBUG_TAG, "entityResponse == null.");
				return;
			}
			switch (result) {
			case 401:
				DataProvider.signOut(null, context);
				return;
			case 200:
				break;
			default:
				return;
			}
			TasksListResponse dtoEntitiesListResponse = null;
			InputStream inputStream;
			try {
				inputStream = httpResponse2.getInputStream();
				String resultString = JSONHttpClient.convertStreamToString(inputStream);
				inputStream.close();
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder
						.registerTypeAdapter(Date.class, new DateSerializer())
						//
						// .registerTypeAdapter(TaskType.class, new TaskTypeSerializer())
						.registerTypeAdapter(RecurrenceInterval.class,
								new RecurrenceIntervalSerializer())
						.registerTypeAdapter(TaskPriority.class,
								new TaskPrioritySerializer())
						.registerTypeAdapter(ReminderTimeMode.class,
								new ReminderTimeModeSerializer())
				//
				;
				dtoEntitiesListResponse = gsonBuilder.create().fromJson(resultString,
						TasksListResponse.class);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
			if (dtoEntitiesListResponse == null) {
				Log.e(CommonConstants.DEBUG_TAG, "dtoEntitiesListResponse == null.");
				return;
			}
			Collections.sort(dtoEntitiesListResponse.Entities,
					new SyncEntityInfoComparator(dtoEntitiesListResponse.Entities));
			// now we have pulled the list from the server
			for (final TaskDto syncDownEntityDto : dtoEntitiesListResponse.Entities) {
				try {
					final BooleanHolder bh = new BooleanHolder(false);
					DataProvider.runInTx(null, context, new Runnable() {
						@Override
						public void run() {
							final TaskDto syncDownEntityDto1 = syncDownEntityDto;
							Task localEntity = DataProvider.getTask(null, context,
									syncDownEntityDto1.Id.longValue(), true);
							if (syncDownEntityDto1.Deleted) {
								if (localEntity != null) {
									if (localEntity.getLastMod() > syncDownEntityDto1.LastMod) {
										// We have edited the task after the task have
										// been deleted on
										// other device. Keep the task
										// This will mark the entity as SYNC_UP_REQUIRED
										DataProvider.eliminateServerTaskId(null, context,
												localEntity.getServerId(), true);
									} else {
										DataProvider.deleteTaskPermanently(null, context,
												syncDownEntityDto1.Id, true);
									}
								}
								bh.value = true;
							} else {
								if (localEntity == null
										|| localEntity.getLastMod() <= syncDownEntityDto1.LastMod) {
									TaskWithDependents entityWithDependents = DataProvider
											.getTaskWithDependents(null, context,
													localEntity == null ? null
															: localEntity.getId(),
													syncDownEntityDto1);
									DataProvider.insertOrReplaceTaskWithDependents(null,
											context, entityWithDependents, false);
									bh.value = true;
								} else if (localEntity.getSyncStatusValue() != SyncStatus.SYNC_UP_REQUIRED
										.getValue()) {
									localEntity
											.setSyncStatusValue(SyncStatus.SYNC_UP_REQUIRED
													.getValue());
									DataProvider.insertOrReplaceTask(null, context,
											localEntity, false);
								}
							}
						}
					});
				} catch (DatabaseStateAndNewEntityArgumentsInconsistencyException e) {
					Log.e(CommonConstants.DEBUG_TAG, e.toString());
					if (mNextSyncDownDateTime > syncDownEntityDto.LastMod) {
						mNextSyncDownDateTime = syncDownEntityDto.LastMod;
					}
				}
				//
				// finally {
				// Long id = DataProvider.getTaskId(context, syncDownEntityDto.Id);
				// if (id != null) {
				// boolean scheduleRemindersOfCompletedTasks =
				// CommonConstants.SCHEDULE_REMINDERS_OF_COMPLETED_TASKS == Helper
				// .getIntegerPreferenceValueFromStringArray(
				// context,
				// R.string.preference_key_reminder_behavior_for_completed_task,
				// R.array.reminder_behavior_for_completed_task_values_array,
				// R.integer.reminder_default_behavior_for_completed_task);
				// Alarm.resetupRemindersOfTask(context, id,
				// scheduleRemindersOfCompletedTasks, Calendar.getInstance()
				// .getTimeInMillis());
				// // Alarm.resetupAlarmsOfTask(context, id);
				// }
				// }
			}
			NotificationService.updateNotification(context);
			SyncTask.notifyAboutChanges(context);
		}
		// save mNextSyncDownTime
		PreferenceManager
				.getDefaultSharedPreferences(context)
				.edit()
				.putLong(CommonConstants.PREF_TASKS_NEXT_SYNC_DOWN_DATETIME,
						mNextSyncDownDateTime).commit();
	}

	private static HttpURLConnection getEntityListResponseForGetSyncEntitiesList(
			Long syncDateTime, String authToken) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", String.format("Bearer %s", authToken));
		JSONHttpClient httpClient = new JSONHttpClient();
		Map<String, String> params = null;
		if (syncDateTime != null) {
			params = new HashMap<String, String>();
			params.put("date", null
			// new org.joda.time.DateTime(syncDateTime).toDateTime(
			// org.joda.time.DateTimeZone.UTC).toString(
			// org.joda.time.format.ISODateTimeFormat.dateTime())
			);
		}
		try {
			return httpClient.GetWithHeader(ServiceUrl.GET_SYNC_TASKS, headers, params);
		} catch (Exception e) {
			Log.e(CommonConstants.DEBUG_TAG, e.toString());
		}
		return null;
	}

	static void notifyAboutChanges(Context context) {
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
	}
}
