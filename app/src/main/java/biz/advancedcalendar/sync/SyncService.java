package biz.advancedcalendar.sync;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.wsdl.sync.ClientCredentials;
import biz.advancedcalendar.wsdl.sync.EntityListResponse;
import biz.advancedcalendar.wsdl.sync.EntityTypesListRequest;
import java.util.Date;

public class SyncService extends IntentService {
	public SyncService() {
		super(CommonConstants.SYNC_SERVICE);
	}

	@Override
	protected void onHandleIntent(Intent workIntent) {
		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo cellularNetwork = connec
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean syncOnCellularInternet = Helper.getBooleanPreferenceValue(this,
				R.string.preference_key_sync_on_cellular_internet, getResources()
						.getBoolean(R.bool.sync_on_cellular_internet));
		if (!(wifi != null && wifi.isConnected() || syncOnCellularInternet
				&& cellularNetwork != null && cellularNetwork.isConnected())) {
			return;
		}
		// Check if wifi or cellular network is available or not. If any of them is
		// available or connected then it will return true, otherwise false;
		Bundle bundle = workIntent.getExtras();
		NotificationManager notificationManager = null;
		try {
			Notification notification = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.stat_notify_sync_anim0)
					.setContentTitle(
							getString(R.string.advanced_calendar_task_notification_content_title))
					.setOngoing(true)
					.setWhen(System.currentTimeMillis())
					.setStyle(
							new NotificationCompat.BigTextStyle().bigText(getResources()
									.getString(R.string.synchronizing)))
					.setContentText(
							getResources().getString(R.string.synchronizing)
									+ System.currentTimeMillis())
					.setPriority(NotificationCompat.PRIORITY_LOW)
					//
					// .setTicker("")
					//
					.build();
			notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager
					.notify(CommonConstants.SYNC_NOTIFICATION_ID, notification);
			int syncServiceRequest = bundle
					.getInt(CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST);
			switch (syncServiceRequest) {
			case CommonConstants.SYNC_SERVICE_REQUEST_REGULAR_SYNC:
				syncAll(workIntent, false);
				break;
			case CommonConstants.INTENT_EXTRA_VALUE_SYNC_SERVICE_REQUEST_FORCE_SYNC:
				syncAll(workIntent, true);
				break;
			case CommonConstants.SYNC_SERVICE_REQUEST_SYNC_UP_TASKS:
				if (DataProvider.isSignedIn(null, getApplicationContext())) {
					SyncTask.syncUpAll(getApplicationContext(), false);
				}
				// send broadcast
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_LABELS));
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_CONTACTS));
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_FILES));
				LocalBroadcastManager
						.getInstance(this)
						.sendBroadcast(
								new Intent(
										CommonConstants.ACTION_ENTITIES_CHANGED_DIARY_RECORDS));
				break;
			case CommonConstants.SYNC_SERVICE_REQUEST_SYNC_MESSAGES:
				// SyncMessage.syncDownAll(getApplicationContext(), workIntent);
				// send broadcast
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
				LocalBroadcastManager.getInstance(this).sendBroadcast(
						new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_MESSAGES));
			}
		} finally {
			if (notificationManager != null) {
				notificationManager.cancel(CommonConstants.SYNC_NOTIFICATION_ID);
			}
		}
	}

	private void syncAll(Intent intent, boolean isForceSync) {
		// LocalConfirmedLogin localConfirmedLogin = DataProvider
		// .getSignedInUser(getApplicationContext());
		// List<NameValuePair> headers = new ArrayList<NameValuePair>();
		// headers.add(new BasicNameValuePair("Authorization", String.format("Bearer %s",
		// localConfirmedLogin.getAuthToken())));
		// JSONHttpClient httpClient = new JSONHttpClient();
		// try {
		// // String[] values = httpClient.GetWithHeader(ServiceUrl.VALUES, headers,
		// // new ArrayList<NameValuePair>(), String[].class);
		// // Task[] tasks = httpClient.GetWithHeader(ServiceUrl.TASKS, headers,
		// // new ArrayList<NameValuePair>(), Task[].class);
		//
		// Contact[] tasks = httpClient.GetWithHeader(ServiceUrl.LABELS, headers,
		// new ArrayList<NameValuePair>(), Contact[].class);
		//
		// biz.advancedcalendar.wsdl.sync.Label l = new
		// biz.advancedcalendar.wsdl.sync.Label();
		// // biz.advancedcalendar.wsdl.sync.Label r = httpClient.PostObject(
		// ServiceUrl.LABELS, l, biz.advancedcalendar.wsdl.sync.Label.class);
		// } catch (Exception e) {
		// int i = 0;
		// }
		// if (1 == 1)
		// return;
		// first pass: download all entities
		if (DataProvider.isSignedIn(null, getApplicationContext())) {
			SyncTask.syncDownAll(getApplicationContext(), isForceSync);
		}
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// SyncContact.syncDownAll(getApplicationContext(), isForceSync);
		// }
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// // SyncLabel.syncDownAll(getApplicationContext(), workIntent, true);
		// } // SyncFile.syncDownAll(getApplicationContext(), workIntent, true);
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// SyncDiaryRecord.syncDownAll(getApplicationContext(), isForceSync);
		// }
		// // first pass: upload all entities
		if (DataProvider.isSignedIn(null, getApplicationContext())) {
			SyncTask.syncUpAll(getApplicationContext(), isForceSync);
		}
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// SyncContact.syncUpAll(getApplicationContext());
		// }
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// // SyncLabel.syncUpAll(getApplicationContext());
		// } // SyncFile.syncUpAll(getApplicationContext());
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// SyncDiaryRecord.syncUpAll(getApplicationContext());
		// }
		// // second pass
		// List<Long> entityIdList;
		// LinkedList<Long> entityIdLinkedList;
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// entityIdList = DataProvider.getSyncUpTaskIdList(getApplicationContext());
		// entityIdLinkedList = new LinkedList<Long>();// TODO sort it
		// for (Long id : entityIdList) {
		// entityIdLinkedList.add(id);
		// }
		// entityIdList = null;
		// while (entityIdLinkedList.size() > 0) {
		// final Long id = entityIdLinkedList.poll();
		// Task syncUpEntity = DataProvider.getTask(getApplicationContext(), id,
		// false);
		// if (syncUpEntity.getSyncStatus() ==
		// CommonConstants.SYNC_STATUS_SYNC_UP_REQUIRED) {
		// if (SyncTask
		// .syncUpSingleEntity(getApplicationContext(), syncUpEntity)) {
		// SyncTask.notifyAboutChanges(getApplicationContext());
		// }
		// } else if (syncUpEntity.getSyncStatus() ==
		// CommonConstants.SYNC_STATUS_SYNC_DOWN_REQUIRED) {
		// SyncTask.syncDownSingleEntity(getApplicationContext(),
		// syncUpEntity.getServerId(), true);
		// }
		// }
		// }
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// entityIdList = DataProvider.getSyncUpLabelIdList(getApplicationContext());
		// entityIdLinkedList = new LinkedList<Long>();
		// for (Long id : entityIdList) {
		// entityIdLinkedList.add(id);
		// }
		// entityIdList = null;
		// while (entityIdLinkedList.size() > 0) {
		// final Long id = entityIdLinkedList.poll();
		// Label syncUpEntity = DataProvider.getLabel(getApplicationContext(), id,
		// false);
		// if (syncUpEntity.getSyncStatus() ==
		// CommonConstants.SYNC_STATUS_SYNC_UP_REQUIRED) {
		// // if (SyncLabel.syncUpSingleEntity(getApplicationContext(),
		// // syncUpEntity, false)) {
		// // SyncLabel.notifyAboutChanges(getApplicationContext());
		// // }
		// } else if (syncUpEntity.getSyncStatus() ==
		// CommonConstants.SYNC_STATUS_SYNC_DOWN_REQUIRED) {
		// // SyncLabel.syncDownSingleEntity(getApplicationContext(),
		// // syncUpEntity.getServerId(), true);
		// }
		// }
		// }
		// if (DataProvider.isSignedIn(getApplicationContext())) {
		// entityIdList = DataProvider.getSyncUpContactIdList(getApplicationContext());
		// entityIdLinkedList = new LinkedList<Long>();
		// for (Long id : entityIdList) {
		// entityIdLinkedList.add(id);
		// }
		// entityIdList = null;
		// while (entityIdLinkedList.size() > 0) {
		// final Long id = entityIdLinkedList.poll();
		// Contact syncUpEntity = DataProvider.getContact(getApplicationContext(),
		// id, false);
		// if (syncUpEntity.getSyncStatus() ==
		// CommonConstants.SYNC_STATUS_SYNC_UP_REQUIRED) {
		// if (SyncContact.syncUpSingleEntity(getApplicationContext(),
		// syncUpEntity, false)) {
		// SyncContact.notifyAboutChanges(getApplicationContext());
		// }
		// } else if (syncUpEntity.getSyncStatus() ==
		// CommonConstants.SYNC_STATUS_SYNC_DOWN_REQUIRED) {
		// SyncContact.syncDownSingleEntity(getApplicationContext(),
		// syncUpEntity.getServerId(), true);
		// }
		// }
		// }
		// send broadcast
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_LABELS));
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_CONTACTS));
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_FILES));
		LocalBroadcastManager.getInstance(this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_DIARY_RECORDS));
	}

	public static EntityListResponse getEntityListResponseForGetSyncEntitiesList(
			boolean getTasks, boolean getLabels, boolean getContacts, boolean getFiles,
			boolean getDiary, Long syncDateTime, String username, String authToken) {
		// BasicHttpBinding_ISyncService service = new BasicHttpBinding_ISyncService();
		EntityTypesListRequest entityListRequest = new EntityTypesListRequest();
		entityListRequest.GetContacts = getContacts;
		entityListRequest.GetLabels = getLabels;
		entityListRequest.GetDiary = getDiary;
		entityListRequest.GetFiles = getFiles;
		entityListRequest.GetTasks = getTasks;
		entityListRequest.SyncDateTime = syncDateTime == null ? null : new Date(
				syncDateTime);
		entityListRequest.Credentials = new ClientCredentials();
		entityListRequest.Credentials.Username = username;
		entityListRequest.Credentials.AuthToken = authToken;
		EntityListResponse objectsListResponse = null;
		// give it 3 tries
		int triesCount = 3;
		for (int i = 0; i < triesCount; i++) {
			try {
				// objectsListResponse = service.GetSyncEntityList(entityListRequest);
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
		return objectsListResponse;
	}
}
