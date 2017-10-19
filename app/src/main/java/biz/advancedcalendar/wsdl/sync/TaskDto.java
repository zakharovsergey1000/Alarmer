package biz.advancedcalendar.wsdl.sync;

import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import biz.advancedcalendar.wsdl.sync.Enums.TaskPriority;
import java.util.ArrayList;
import java.util.List;

public class TaskDto {
	public Long Id;
	public Long ParentId;
	public Long Created;
	public Long LastMod;
	public String Name;
	public Enums.TaskPriority Priority;
	public Integer Color;
	public Long StartDateTime;
	public Long EndDateTime;
	public Integer RequiredLength;
	public Integer ActualLength;
	public Boolean IsCompleted;
	public short PercentOfCompletion;
	public Long CompletedDateTime;
	public boolean Deleted;
	public int SortOrder;
	public String Description;
	public String Location;
	// public TaskType Type;
	public Integer TimeUnitsCount;
	public RecurrenceInterval RecurrenceInterval;
	public Integer OccurrencesMaxCount;
	public Long RepetitionEndDateTime;
	public Long RingtoneFadeInTime;
	public Integer PlayingTime;
	public Integer AutomaticSnoozeDuration;
	public Integer AutomaticSnoozesMaxCount;
	public Boolean Vibrate;
	public String VibratePattern;
	public Boolean Led;
	public String LedPattern;
	public Integer LedColor;
	public List<Long> Labels = new ArrayList<Long>();
	public List<ReminderDto> Reminders = new ArrayList<ReminderDto>();
	public List<TaskOccurrenceDto> TaskOccurrences = new ArrayList<TaskOccurrenceDto>();

	public TaskDto(Long id, Long parentId, Long created, Long lastMod, String text,
			TaskPriority priority, Integer color, Long startDateTime, Long endDateTime,
			Integer requiredLength, Integer actualLength, Boolean isCompleted,
			Short percentOfCompletion, Long completedTime, boolean deleted,
			Integer sortOrder, String description, String location,
			Integer timeUnitsCount, RecurrenceInterval recurrenceInterval,
			Integer occurrencesMaxCount, Long repetitionEndDateTime,
			Long ringtoneFadeInTime, Integer playingTime,
			Integer automaticSnoozeDuration, Integer automaticSnoozesMaxCount,
			Boolean vibrate, String vibratePattern, Boolean led, String ledPattern,
			Integer ledColor, List<Long> labels, List<ReminderDto> reminders,
			List<TaskOccurrenceDto> taskOccurrences) {
		super();
		Id = id;
		ParentId = parentId;
		Created = created;
		LastMod = lastMod;
		Name = text;
		Priority = priority;
		Color = color;
		StartDateTime = startDateTime;
		EndDateTime = endDateTime;
		RequiredLength = requiredLength;
		ActualLength = actualLength;
		IsCompleted = isCompleted;
		PercentOfCompletion = percentOfCompletion == null ? 0 : percentOfCompletion;
		CompletedDateTime = completedTime;
		Deleted = deleted;
		SortOrder = sortOrder;
		Description = description;
		Location = location;
		// Type = type;
		TimeUnitsCount = timeUnitsCount;
		RecurrenceInterval = recurrenceInterval;
		OccurrencesMaxCount = occurrencesMaxCount;
		RepetitionEndDateTime = repetitionEndDateTime;
		RingtoneFadeInTime = ringtoneFadeInTime;
		PlayingTime = playingTime;
		AutomaticSnoozeDuration = automaticSnoozeDuration;
		AutomaticSnoozesMaxCount = automaticSnoozesMaxCount;
		Vibrate = vibrate;
		VibratePattern = vibratePattern;
		Led = led;
		LedPattern = ledPattern;
		LedColor = ledColor;
		Labels = labels;
		Reminders = reminders;
		TaskOccurrences = taskOccurrences;
	}
}
