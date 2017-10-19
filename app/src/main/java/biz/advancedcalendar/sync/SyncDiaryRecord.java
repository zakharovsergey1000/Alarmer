package biz.advancedcalendar.sync;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.db.DatabaseStateAndNewEntityArgumentsInconsistencyException;
import biz.advancedcalendar.greendao.DiaryRecord;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.wsdl.sync.ArrayOfSyncEntityInfo;
import biz.advancedcalendar.wsdl.sync.ArrayOfint;
import biz.advancedcalendar.wsdl.sync.ClientCredentials;
import biz.advancedcalendar.wsdl.sync.DiaryRecordResponse;
import biz.advancedcalendar.wsdl.sync.DiaryRecordsListResponse;
import biz.advancedcalendar.wsdl.sync.EntityListRequest;
import biz.advancedcalendar.wsdl.sync.EntityListResponse;
import biz.advancedcalendar.wsdl.sync.EntityRequest;
import biz.advancedcalendar.wsdl.sync.SetEntityResponse2;
import biz.advancedcalendar.wsdl.sync.SyncEntityInfo;
import java.util.LinkedList;
import java.util.List;

public class SyncDiaryRecord {
	// static BasicHttpBinding_ISyncService service = new BasicHttpBinding_ISyncService();
	static synchronized void syncUpAll(Context context) {
		List<Long> syncUpIdList = DataProvider.getSyncUpDiaryRecordIdList(null, context);
		LinkedList<Long> syncUpIdLinkedList = new LinkedList<Long>();
		for (Long id : syncUpIdList) {
			syncUpIdLinkedList.add(id);
		}
		syncUpIdList = null;
		while (syncUpIdLinkedList.size() > 0) {
			Long id = syncUpIdLinkedList.poll();
			DiaryRecord syncUpEntity = DataProvider.getDiaryRecord(null, context, id, false);
			if (syncUpEntity == null) {
				continue;
			}
			if (syncUpEntity.getDeleted() && syncUpEntity.getServerId() == 0) {
				DataProvider.deleteDiaryRecord(null, context, syncUpEntity.getId(), false);
				continue;
			}
			if (SyncDiaryRecord.syncUpSingleEntity(context, syncUpEntity)) {
				SyncDiaryRecord.notifyAboutChanges(context);
			}
		}
	}

	@SuppressWarnings("null")
	static synchronized boolean syncUpSingleEntity(final Context context,
			final DiaryRecord syncUpEntity) {
		// biz.advancedcalendar.wsdl.sync.Record wsdlSyncUpEntity = DataProvider
		// .getRecordDto(context, syncUpEntity.getId());
		// UserProfile userProfile = DataProvider.getUserProfile(context);
		// push the entity to the server
		SetEntityResponse2 entityResponse = null;
		// SyncDiaryRecord
		// .getDiaryRecordResponseForSetDiaryRecord(context,
		// SyncDiaryRecord.service, wsdlSyncUpEntity,
		// userProfile.getUserName(),
		// userProfile.getAuthToken());
		// if (entityResponse == null) {
		// Log.e(CommonConstants.DEBUG_TAG, "entityResponse == null.");
		// if (syncUpEntity.getDeleted()) {
		// DataProvider.deleteDiaryRecord(context, syncUpEntity.getId(), false);
		// return true;
		// }
		// return false;
		// }
		if (entityResponse.Result == null) {
			Log.e(CommonConstants.DEBUG_TAG, "entityResponse.Result == null");
			if (syncUpEntity.getDeleted()) {
				DataProvider.deleteDiaryRecord(null, context, syncUpEntity.getId(), false);
				return true;
			}
			return false;
		}
		switch (entityResponse.Result) {
		case ENTITY_IS_DELETED:
			DataProvider.deleteDiaryRecord(null, context, syncUpEntity.getId(), false);
			return true;
		case ACCESS_DENIED:
			DataProvider.signOut(null, context);
			return false;
		case SAVED:
			return false;
		case ENTITY_IS_NOT_FOUND:
			break;
		default:
			break;
		}
		return false;
	}

	static synchronized void syncDownAll(Context context, boolean isForceSync) {
		Long mSyncDownDateTime = null;
		if (!isForceSync)
			mSyncDownDateTime = Helper.getLongPreferenceValue(context,
					R.string.preference_key_diary_records_next_sync_down_datetime, 0,
					null, null);
		UserProfile userProfile = DataProvider.getUserProfile(null, context);
		EntityListResponse entityResponse = SyncService
				.getEntityListResponseForGetSyncEntitiesList(false, false, false, false,
						true, mSyncDownDateTime, userProfile.getEmail(),
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
			break;
		default:
			return;
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
						&& SyncEntities.get(i).EntityId != syncObjectInfo.ParentEntityId) {
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
				DiaryRecord t = DataProvider.getDiaryRecord(null,
						context, syncObjectInfo.EntityId.longValue(), true);
				if (syncObjectInfo.Deleted) {
					DataProvider.deleteDiaryRecord(null,
							context, syncObjectInfo.EntityId.longValue(), true);
				} else {
					if (t != null
							&& syncObjectInfo.LastMod.getTime() < t
									.getLocalChangeDateTime()) {
						t.setSyncStatus(SyncStatus.SYNC_UP_REQUIRED.getValue());
						DataProvider.insertOrReplaceDiaryRecord(null, context, t);
					} else {
						syncEntityIdList.add(syncObjectInfo.EntityId);
						k++;
					}
				}
			}
			DiaryRecordsListResponse wsdlEntitiesListResponse = SyncDiaryRecord
					.getDiaryRecordListResponseForGetDiaryRecordList(syncEntityIdList,
							true, userProfile.getEmail(), userProfile.getAuthToken());
			if (wsdlEntitiesListResponse == null) {
				Log.e(CommonConstants.DEBUG_TAG, "wsdlEntitiesListResponse == null.");
				return;
			}
			if (wsdlEntitiesListResponse.Result == null) {
				Log.e(CommonConstants.DEBUG_TAG,
						"wsdlEntitiesListResponse.Result == null");
				return;
			}
			switch (entityResponse.Result) {
			case ACCESS_DENIED:
				DataProvider.signOut(null, context);
				return;
			case SUCCESS:
				break;
			case NOT_ALL_ENTITIES_ARE_FOUND:
				break;
			default:
				break;
			}
			// now we have pulled the list from the server
			LinkedList<biz.advancedcalendar.wsdl.sync.Record> wsdlSyncDownEntityLinkedList = new LinkedList<biz.advancedcalendar.wsdl.sync.Record>();
			while (wsdlEntitiesListResponse.Entities.size() > 0) {
				wsdlSyncDownEntityLinkedList.add(wsdlEntitiesListResponse.Entities
						.remove(wsdlEntitiesListResponse.Entities.size() - 1));
			}
			wsdlEntitiesListResponse = null;
			while (wsdlSyncDownEntityLinkedList.size() > 0) {
				biz.advancedcalendar.wsdl.sync.Record wsdlSyncDownEntity = wsdlSyncDownEntityLinkedList
						.poll();
				try {
					DataProvider.insertOrReplaceDiaryRecordWithSyncEntity(null, context,
							null, wsdlSyncDownEntity);
				} catch (DatabaseStateAndNewEntityArgumentsInconsistencyException e) {
					Log.e(CommonConstants.DEBUG_TAG, e.toString());
					if (mNextSyncDownDateTime > wsdlSyncDownEntity.LastMod.getTime())
						mNextSyncDownDateTime = wsdlSyncDownEntity.LastMod.getTime();
				}
			}
			SyncDiaryRecord.notifyAboutChanges(context);
		}
		entityResponse = null;
		// save mNextSyncDownTime
		PreferenceManager
				.getDefaultSharedPreferences(context)
				.edit()
				.putLong(CommonConstants.PREF_DIARY_RECORDS_NEXT_SYNC_DOWN_DATETIME,
						mNextSyncDownDateTime).commit();
	}

	static synchronized boolean syncDownSingleEntity(Context context, int objectId,
			boolean syncDownMissingRelatedEntities) {
		UserProfile userProfile = DataProvider.getUserProfile(null, context);
		// pull the entity from the server
		DiaryRecordResponse entityResponse = SyncDiaryRecord
				.getDiaryRecordResponseForGetDiaryRecord(objectId, true,
						userProfile.getEmail(), userProfile.getAuthToken());
		switch (entityResponse.Result) {
		case ENTITY_IS_DELETED:
			try {
				DataProvider.deleteDiaryRecord(null, context,
						(long) entityResponse.Entity.ID, true);
			} catch (DatabaseStateAndNewEntityArgumentsInconsistencyException e) {
				Log.e(CommonConstants.DEBUG_TAG, e.getMessage());
				return false;
			}
			return true;
		case ACCESS_DENIED:
			DataProvider.signOut(null, context);
			return false;
		case SUCCESS:
		default:
			return false;
		}
	}

	private static DiaryRecordResponse getDiaryRecordResponseForGetDiaryRecord(
			int EntityId, boolean includeDependentObjects, String Username,
			String AuthToken) {
		EntityRequest objectRequest = new EntityRequest();
		objectRequest.EntityID = EntityId;
		objectRequest.Credentials = new ClientCredentials();
		objectRequest.Credentials.Username = Username;
		objectRequest.Credentials.AuthToken = AuthToken;
		objectRequest.IncludeDependentEntities = includeDependentObjects;
		DiaryRecordResponse entityResponse = null;
		// give it 3 tries
		int triesCount = 3;
		for (int i = 0; i < triesCount; i++) {
			try {
				// entityResponse = SyncDiaryRecord.service.GetDiaryRecord(objectRequest);
				i = 3;
			} catch (Exception e) {
				Log.e(CommonConstants.DEBUG_TAG, e.toString());
				try {
					switch (i) {
					case 0:
						break;
					case 1:
						Thread.sleep(1000);
						break;
					case 2:
						Thread.sleep(1000 * 3);
						break;
					default:
						break;
					}
				} catch (InterruptedException e1) {
				}
				continue;
			}
		}
		return entityResponse;
	}

	private static DiaryRecordsListResponse getDiaryRecordListResponseForGetDiaryRecordList(
			ArrayOfint objectIdArray, boolean includeDependentObjects, String Username,
			String AuthToken) {
		EntityListRequest objectListRequest = new EntityListRequest();
		objectListRequest.EntityIdArray = objectIdArray;
		objectListRequest.Credentials = new ClientCredentials();
		objectListRequest.Credentials.Username = Username;
		objectListRequest.Credentials.AuthToken = AuthToken;
		objectListRequest.IncludeDependentEntities = includeDependentObjects;
		DiaryRecordsListResponse entityListResponse = null;
		// give it 3 tries
		int triesCount = 3;
		for (int i = 0; i < triesCount; i++) {
			try {
				// entityListResponse = SyncDiaryRecord.service
				// .GetDiaryRecords(objectListRequest);
				i = 3;
			} catch (Exception e) {
				Log.e(CommonConstants.DEBUG_TAG, e.toString());
				try {
					switch (i) {
					case 0:
						break;
					case 1:
						Thread.sleep(1000);
						break;
					case 2:
						Thread.sleep(1000 * 3);
						break;
					default:
						break;
					}
				} catch (InterruptedException e1) {
				}
				continue;
			}
		}
		return entityListResponse;
	}

	// private static SetEntityResponse2 getDiaryRecordResponseForSetDiaryRecord(
	// Context context, biz.advancedcalendar.wsdl.sync.Record wsdlSyncUpEntity,
	// String Username, String AuthToken) {
	// SetDiaryRecordRequest setEntityRequest = new SetDiaryRecordRequest();
	// setEntityRequest.Credentials = new ClientCredentials();
	// UserProfile userProfile = DataProvider.getUserProfile(context);
	// setEntityRequest.Credentials.Username = userProfile.getEmail();
	// setEntityRequest.Credentials.AuthToken = userProfile.getAuthToken();
	// setEntityRequest.Record = wsdlSyncUpEntity;
	// // push the entity to the server
	// SetEntityResponse2 entityResponse = null;
	// // give it 3 tries
	// int triesCount = 3;
	// for (int i = 0; i < triesCount; i++) {
	// try {
	// // entityResponse = service.SetDiaryRecord(setEntityRequest);
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
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_DIARY_RECORDS));
	}
}
