package biz.advancedcalendar.wsdl.sync;

import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;

public class ReminderDto {
	public Long Id;
	public Long TaskId;
	public long ReminderDateTime;
	public ReminderTimeMode ReminderTimeMode;
	public String Text;
	public boolean Enabled;
	public boolean IsAlarm;
	public Long RingtoneFadeInTime;
	public Integer PlayingTime;
	public Integer AutomaticSnoozeDuration;
	public Integer AutomaticSnoozesMaxCount;
	public Boolean Vibrate;
	public String VibratePattern;
	public Boolean Led;
	public String LedPattern;
	public Integer LedColor;

	public ReminderDto(Long id, Long taskId, long reminderDateTime,
			ReminderTimeMode reminderTimeMode, String text, boolean enabled,
			boolean isAlarm, Long ringtoneFadeInTime, Integer playingTime,
			Integer automaticSnoozeDuration, Integer automaticSnoozesMaxCount,
			Boolean vibrate, String vibratePattern, Boolean led, String ledPattern,
			Integer ledColor) {
		super();
		Id = id;
		TaskId = taskId;
		ReminderDateTime = reminderDateTime;
		ReminderTimeMode = reminderTimeMode;
		Text = text;
		Enabled = enabled;
		IsAlarm = isAlarm;
		RingtoneFadeInTime = ringtoneFadeInTime;
		PlayingTime = playingTime;
		AutomaticSnoozeDuration = automaticSnoozeDuration;
		AutomaticSnoozesMaxCount = automaticSnoozesMaxCount;
		Vibrate = vibrate;
		VibratePattern = vibratePattern;
		Led = led;
		LedPattern = ledPattern;
		LedColor = ledColor;
	}
}
