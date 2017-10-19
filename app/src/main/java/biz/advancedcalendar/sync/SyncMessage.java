package biz.advancedcalendar.sync;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Message;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.wsdl.sync.ArrayOfSyncEntityInfo;
import biz.advancedcalendar.wsdl.sync.ArrayOfint;
import biz.advancedcalendar.wsdl.sync.EntityListResponse;
import biz.advancedcalendar.wsdl.sync.GetTaskResponse;
import biz.advancedcalendar.wsdl.sync.SendTaskMessageResponse;
import biz.advancedcalendar.wsdl.sync.SyncEntityInfo;
import java.util.LinkedList;
import java.util.List;

public class SyncMessage {
	// static BasicHttpBinding_ISyncService service = new BasicHttpBinding_ISyncService();
	static synchronized void syncUpAll(Context context, Intent workIntent) {
		List<Long> syncUpIdList = DataProvider.getSyncUpMessageIdList(null, context);
		LinkedList<Long> syncUpIdLinkedList = new LinkedList<Long>();
		for (Long id : syncUpIdList) {
			syncUpIdLinkedList.add(id);
		}
		syncUpIdList = null;
		while (syncUpIdLinkedList.size() > 0) {
			Long id = syncUpIdLinkedList.poll();
			Message syncUpEntity = DataProvider.getMessage(null, context, id, false);
			if (syncUpEntity == null) {
				continue;
			}
			if (SyncMessage.syncUpSingleEntity(context, syncUpEntity, true)) {
				SyncMessage.notifyAboutChanges(context);
			}
		}
	}

	@SuppressWarnings("null")
	static synchronized boolean syncUpSingleEntity(Context context, Message syncUpEntity,
			boolean syncUpRelatedEntities) {
		// biz.advancedcalendar.wsdl.sync.Message wsdlSyncUpEntity = null;//
		// DataProvider.getWsdlSyncMessage(context,
		// syncUpEntity.getId());
		// UserProfile userProfile = DataProvider.getUserProfile(context);
		// push the entity to the server
		SendTaskMessageResponse entityResponse = null;
		try {
			// entityResponse = SyncMessage
			// .getSendTaskMessageResponseForSendWorkgroupMessage(context,
			// SyncMessage.service, wsdlSyncUpEntity,
			// userProfile.getUserName(),
			// userProfile.getAuthToken());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// if (entityResponse == null) {
		// Log.e(CommonConstants.DEBUG_TAG, "entityResponse == null.");
		// return false;
		// }
		if (entityResponse.Result == null) {
			Log.e(CommonConstants.DEBUG_TAG, "entityResponse.Result == null");
			return false;
		}
		switch (entityResponse.Result) {
		case ENTITY_IS_NOT_FOUND:
			Message entityWithDependencies = DataProvider.getMessage(null,
					context, syncUpEntity.getId(), false);
			// DataProvider.deleteTask(context, syncUpEntity.getId(), false);
			entityWithDependencies.setServerId(null);
			// entityWithDependencies.task.setId(null);
			// entityWithDependencies
			// .setSyncStatus(CommonConstants.SYNC_STATUS_SYNC_UP_REQUIRED);
			// DataProvider.insertOrReplaceMessagesWithSyncEntity(context,
			// entityResponse.Entities);
			return true;
		case ENTITY_IS_DELETED:
			// DataProvider.deleteMessage(context, syncUpEntity.getId(), false);
			return true;
		case ACCESS_DENIED:
			DataProvider.signOut(null, context);
			return false;
		case SUCCESS:
			return false;
		}
		return false;
	}

	static synchronized void syncDownAll(Context context, boolean isForceSync) {
		Long mSyncDownDateTime = null;
		if (isForceSync)
			mSyncDownDateTime = Helper.getLongPreferenceValue(context,
					R.string.preference_key_tasks_next_sync_down_datetime, 0, null, null);
		UserProfile userProfile = DataProvider.getUserProfile(null, context);
		EntityListResponse entityResponse = SyncService
				.getEntityListResponseForGetSyncEntitiesList(true, false, false, false,
						false, mSyncDownDateTime, userProfile.getEmail(),
						userProfile.getAuthToken());
		if (entityResponse == null) {
			Log.e(CommonConstants.DEBUG_TAG, "entityResponse == null.");
			return;
		}
		if (entityResponse.Result == null) {
			Log.e(CommonConstants.DEBUG_TAG, "entityResponse.Result == null");
			return;
		}
		switch (entityResponse.Result) {
		case ACCESS_DENIED:
			DataProvider.signOut(null, context);
			return;
		case SUCCESS:
		case NOT_ALL_ENTITIES_ARE_FOUND:
			break;
		}
		// Now we have got the list.
		long mNextSyncDownDateTime = entityResponse.ServerTime.getTime();
		// rearrange SyncEntities so that any parent is before children
		ArrayOfSyncEntityInfo SyncEntities = new ArrayOfSyncEntityInfo();
		if (entityResponse.SyncEntities.size() > 0) {
			while (entityResponse.SyncEntities.size() > 0) {
				SyncEntityInfo syncObjectInfo = entityResponse.SyncEntities.remove(0);
				int i = 0;
				while (i < SyncEntities.size()
				/* && SyncEntities.get(i).EntityId != syncObjectInfo.ParentEntityId */) {
					i++;
				}
				if (i < SyncEntities.size()) {
					i = 0;
				} else {
					i++;
				}
				SyncEntities.add(i, syncObjectInfo);
			}
		}
		entityResponse.SyncEntities = SyncEntities;
		while (entityResponse.SyncEntities.size() > 0) {
			ArrayOfint syncEntityIdList = new ArrayOfint();
			int k = 0;
			while (k < CommonConstants.SYNC_DOWN_MAX_BUNCH_SIZE
					&& entityResponse.SyncEntities.size() > 0) {
				SyncEntityInfo syncObjectInfo = entityResponse.SyncEntities
						.remove(entityResponse.SyncEntities.size() - 1);
				Message t = DataProvider.getMessage(null,
						context, syncObjectInfo.EntityId.longValue(), true);
				if (syncObjectInfo.Deleted) {
					// DataProvider.deleteTask(context,
					// syncObjectInfo.EntityId.longValue(), true);
				} else {
					if (t != null
							&& syncObjectInfo.LastMod.getTime() < t
									.getLocalChangeDateTime()) {
						// t.setSyncStatus(CommonConstants.SYNC_STATUS_SYNC_UP_REQUIRED);
						// DataProvider.insertOrReplaceMessage(context, t);
					} else {
						syncEntityIdList.add(syncObjectInfo.EntityId);
						k++;
					}
				}
			}
			// MessagesListResponse wsdlEntitiesListResponse = SyncTask
			// .getTaskListResponseForGetTaskList(syncEntityIdList, true,
			// userProfile.getUserName(),
			// userProfile.getAuthToken());
			// if (wsdlEntitiesListResponse == null) {
			// Log.e(CommonConstants.DEBUG_TAG, "wsdlEntitiesListResponse == null.");
			// return;
			// }
			// if (wsdlEntitiesListResponse.Result == null) {
			// Log.e(CommonConstants.DEBUG_TAG,
			// "wsdlEntitiesListResponse.Result == null");
			// return;
			// }
			// switch (entityResponse.Result.ResultCode) {
			// case AccessDenied:
			// DataProvider.signOut(context);
			// return;
			// case IncorrectData:
			// Log.e(CommonConstants.DEBUG_TAG,
			// "SERVER_RESPONSE_RESULT_ERROR_TYPE_4_INCORRECT_DATA");
			// return;
			// case NotAllRequestedObjectsFound:
			// case Success:
			// break;
			// default:
			// return;
			// }
			// // now we have pulled the list from the server
			// LinkedList<biz.advancedcalendar.wsdl.sync.Task>
			// wsdlSyncDownEntityLinkedList = new
			// LinkedList<biz.advancedcalendar.wsdl.sync.Task>();
			// while (wsdlEntitiesListResponse.Entities.size() > 0) {
			// wsdlSyncDownEntityLinkedList.add(wsdlEntitiesListResponse.Entities
			// .remove(wsdlEntitiesListResponse.Entities.size() - 1).Task);
			// }
			// wsdlEntitiesListResponse = null;
			// while (wsdlSyncDownEntityLinkedList.size() > 0) {
			// biz.advancedcalendar.wsdl.sync.Task wsdlSyncEntity =
			// wsdlSyncDownEntityLinkedList
			// .poll();
			// // TODO: the server returns wrong repeat_on_days_of_week values
			// // when task list is requested.
			// // When single task is requested the repeat_on_days_of_week
			// // values
			// // are correct
			// try {
			// DataProvider.insertOrReplaceTaskWithSyncEntityIfNewer(context, null,
			// wsdlSyncEntity, null);
			// } catch (DatabaseStateAndNewEntityArgumentsInconsistensyException e) {
			// Log.e(CommonConstants.DEBUG_TAG, e.toString());
			// if (mNextSyncDownDateTime > wsdlSyncEntity.LastMod.getTime())
			// mNextSyncDownDateTime = wsdlSyncEntity.LastMod.getTime();
			// }
			// }
			// SyncTask.notifyAboutChanges(context);
		}
		entityResponse = null;
		// save mNextSyncDownTime
		PreferenceManager
				.getDefaultSharedPreferences(context)
				.edit()
				.putLong(CommonConstants.PREF_TASKS_NEXT_SYNC_DOWN_DATETIME,
						mNextSyncDownDateTime).commit();
	}

	static synchronized boolean syncDownSingleEntity(Context context, int objectId,
			boolean syncDownMissingRelatedEntities) {
		// UserProfile userProfile = DataProvider.getUserProfile(context);
		// pull the entity from the server
		// TaskResponse entityResponse = SyncTask.getTaskResponseForGetTask(objectId,
		// true,
		// userProfile.getUserName(), userProfile.getAuthToken());
		// switch (entityResponse.Result.ResultCode) {
		// case ObjectDeleted:
		// try {
		// DataProvider.deleteTask(context, entityResponse.TaskId, true);
		// } catch (DatabaseStateAndNewEntityArgumentsInconsistensyException e) {
		// Log.e(CommonConstants.DEBUG_TAG, e.getMessage());
		// return false;
		// }
		// return true;
		// case AccessDenied:
		// DataProvider.signOut(context);
		// return false;
		// case Success:
		// default:
		// return false;
		// }
		return false;
	}

	static GetTaskResponse getTaskResponseForGetTask2(int EntityId,
			boolean includeDependentObjects, String Username, String AuthToken) {
		// EntityRequest objectRequest = new EntityRequest();
		// Message lastMessageForTask = DataProvider.getLastMessageForTask(
		// context, taskId);
		//
		// BasicHttpBinding_ISyncService service = new BasicHttpBinding_ISyncService();
		// WorkgroupMessagesRequest entityListRequest = null;
		//
		// entityListRequest = new WorkgroupMessagesRequest();
		//
		// if (lastMessageForTask == null) {
		// entityListRequest.QueryType = 0;
		// entityListRequest.RelatedMessageID = null;
		// } else {
		// entityListRequest.QueryType = 2;
		// entityListRequest.RelatedMessageID = lastMessageForTask
		// .getServerId();
		// }
		//
		// entityListRequest.WorkgroupID = workGroupList.get(0).getServerId();
		// entityListRequest.Credentials = new ClientCredentials();
		// userProfile userProfile = DataProvider
		// .getUserProfile(context);
		// entityListRequest.Credentials.Username = userProfile
		// .getUserName();
		// entityListRequest.Credentials.AuthToken = userProfile
		// .getAuthToken();
		// WorkgroupMessagesResponse objectsListResponse = null;
		//
		//
		// // give it 3 tries
		// int triesCount = 3;
		// for (int i = 0; i < triesCount; i++) {
		// try {
		// objectsListResponse = service
		// .GetWorkgroupMessages(entityListRequest);
		// i = 3;
		// } catch (SSLException e1) {
		// Log.e(CommonConstants.DEBUG_TAG, e1.toString());
		// // An error sometimes can occur:
		// // Write error: ssl=0xb90a64e0: I/O error during system call,
		// // Connection reset by peer
		// // so let's give it another try
		// try {
		// switch (i) {
		// case 0:
		// break;
		// case 1:
		// Thread.sleep(1000);
		// break;
		// case 2:
		// Thread.sleep(1000 * 3);
		// break;
		// default:
		// break;
		// }
		// } catch (InterruptedException e2) {
		// }
		// continue;
		// } catch (SocketTimeoutException e) {
		// Log.e(CommonConstants.DEBUG_TAG, e.toString());
		// try {
		// switch (i) {
		// case 0:
		// break;
		// case 1:
		// Thread.sleep(1000);
		// break;
		// case 2:
		// Thread.sleep(1000 * 3);
		// break;
		// default:
		// break;
		// }
		// } catch (InterruptedException e1) {
		// }
		// continue;
		// } catch (SocketException e) {
		// Log.e(CommonConstants.DEBUG_TAG, e.toString());
		// try {
		// switch (i) {
		// case 0:
		// break;
		// case 1:
		// Thread.sleep(1000);
		// break;
		// case 2:
		// Thread.sleep(1000 * 3);
		// break;
		// default:
		// break;
		// }
		// } catch (InterruptedException e1) {
		// }
		// continue;
		// } catch (Exception e) {
		// Log.e(CommonConstants.DEBUG_TAG, e.toString());
		// try {
		// switch (i) {
		// case 0:
		// break;
		// case 1:
		// Thread.sleep(1000);
		// break;
		// case 2:
		// Thread.sleep(1000 * 3);
		// break;
		// default:
		// break;
		// }
		// } catch (InterruptedException e1) {
		// }
		// continue;
		// }
		// }
		return null;
	}

	// private static SendTaskMessageResponse
	// getSendTaskMessageResponseForSendWorkgroupMessage(
	// Context context, biz.advancedcalendar.wsdl.sync.Message wsdlSyncUpEntity,
	// String Username, String AuthToken) throws Exception {
	// SendTaskMessageRequest setEntityRequest = new SendTaskMessageRequest();
	// setEntityRequest.Credentials = new ClientCredentials();
	// UserProfile userProfile = DataProvider.getUserProfile(context);
	// setEntityRequest.Credentials.Username = userProfile.getEmail();
	// setEntityRequest.Credentials.AuthToken = userProfile.getAuthToken();
	// setEntityRequest.Message = wsdlSyncUpEntity;
	// // push the entity to the server
	// SendTaskMessageResponse entityResponse = null;
	// // give it 3 tries
	// int triesCount = 3;
	// for (int i = 0; i < triesCount; i++) {
	// try {
	// // entityResponse = service.SendWorkgroupMessage(setEntityRequest);
	// i = 3;
	// } catch (Exception e) {
	// Log.e(CommonConstants.DEBUG_TAG, e.toString());
	// try {
	// switch (i) {
	// case 0:
	// break;
	// case 1:
	// Thread.sleep(1000);
	// break;
	// case 2:
	// Thread.sleep(1000 * 3);
	// break;
	// default:
	// break;
	// }
	// } catch (InterruptedException e1) {
	// }
	// continue;
	// }
	// }
	// return entityResponse;
	// }
	private static void notifyAboutChanges(Context context) {
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_MESSAGES));
	}
}
