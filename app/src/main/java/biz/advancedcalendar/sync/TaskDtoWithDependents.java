package biz.advancedcalendar.sync;

import biz.advancedcalendar.wsdl.sync.TaskDto;

public class TaskDtoWithDependents {
	public biz.advancedcalendar.wsdl.sync.TaskDto taskDto;
	public Long[] reminderIds;
	public Long[] dailyRepetitionIds;

	public TaskDtoWithDependents(TaskDto taskDto, Long[] reminderIds,
			Long[] dailyRepetitionIds) {
		super();
		this.taskDto = taskDto;
		this.reminderIds = reminderIds;
		this.dailyRepetitionIds = dailyRepetitionIds;
	}
}
