package biz.advancedcalendar.wsdl.sync;

public class TaskOccurrenceDto {
	public Long Id;
	public Long TaskId;
	public int OrdinalNumber;

	public TaskOccurrenceDto(Long id, Long taskId, int ordinalNumber) {
		super();
		Id = id;
		TaskId = taskId;
		OrdinalNumber = ordinalNumber;
	}
}
