package biz.advancedcalendar.sync;

import biz.advancedcalendar.greendao.Task;

public class SyncTaskHolder {
	public Task entity;
	public Long[] localReminderIds;
	public Long localChangeDateTime;

	public SyncTaskHolder(Task entity, Long[] localReminderIds, Long localChangeDateTime) {
		super();
		this.entity = entity;
		this.localReminderIds = localReminderIds;
		this.localChangeDateTime = localChangeDateTime;
	}
}
