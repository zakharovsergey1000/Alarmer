package biz.advancedcalendar.server;

import biz.advancedcalendar.CommonConstants;

/** Created by ServusKevin on 7/27/14. */
public class ServiceUrl {
	public static final String REST_SERVICE_URL = CommonConstants.ADVANCEDCALENDAR_SERVER_ADDRESS;
	public static final String REST_SERVICE_API = ServiceUrl.REST_SERVICE_URL + "api/";
	public static final String REGISTER = ServiceUrl.REST_SERVICE_API
			+ "Account/Register";
	public static final String TOKEN = ServiceUrl.REST_SERVICE_URL + "Token";
	public static final String ADD_EXTERNAL_LOGIN = ServiceUrl.REST_SERVICE_API
			+ "Account/AddExternalLogin";
	public static final String GOOGLE_LOGIN = ServiceUrl.REST_SERVICE_API
			+ "Account/GoogleLogin";
	public static final String VALUES = ServiceUrl.REST_SERVICE_API + "Values";
	public static final String GET_SYNC_TASKS = ServiceUrl.REST_SERVICE_API
			+ "tasks/GetSyncTasks";
	public static final String GET_SYNC_CONTACTS = ServiceUrl.REST_SERVICE_API
			+ "contacts/GetSyncContacts";
	public static final String GET_TASKS = ServiceUrl.REST_SERVICE_API + "tasks/GetTasks";
	public static final String GET_USER_INFO = ServiceUrl.REST_SERVICE_API
			+ "Account/UserInfo";
	public static final String DELETE_TASK = ServiceUrl.REST_SERVICE_API
			+ "tasks/DeleteTask";
	public static final String DELETE_CONTACT = ServiceUrl.REST_SERVICE_API
			+ "contacts/DeleteContact";
	public static final String TASK_ARRAY = ServiceUrl.REST_SERVICE_API
			+ "tasks/TaskArray";
	public static final String CONTACT_ARRAY = ServiceUrl.REST_SERVICE_API
			+ "contacts/ContactArray";
	public static final String CONTACTS = ServiceUrl.REST_SERVICE_API + "Contacts";
	public static final String LABELS = ServiceUrl.REST_SERVICE_API + "Labels";
	public static final String CHANGED_LABELS = ServiceUrl.REST_SERVICE_API
			+ "ChangedLabels";
}
