package biz.advancedcalendar.wsdl.sync;

public class EntityListResponse {
	public Enums.GetEntityListResult Result;
	public java.util.Date ServerTime;
	public ArrayOfSyncEntityInfo SyncEntities = new ArrayOfSyncEntityInfo();
}
