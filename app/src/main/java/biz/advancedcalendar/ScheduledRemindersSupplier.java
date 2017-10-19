package biz.advancedcalendar;

import java.util.List;
import biz.advancedcalendar.greendao.ScheduledReminder;

public interface ScheduledRemindersSupplier {
	List<ScheduledReminder> getScheduledReminders();
}