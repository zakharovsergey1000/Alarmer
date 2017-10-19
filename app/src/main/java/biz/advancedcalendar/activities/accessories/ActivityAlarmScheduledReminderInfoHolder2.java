package biz.advancedcalendar.activities.accessories;

import android.os.Handler;
import android.view.View;
import biz.advancedcalendar.greendao.ScheduledReminder;

public class ActivityAlarmScheduledReminderInfoHolder2 {
	public ActivityAlarmScheduledReminderInfoHolder2(
	/* int position, */Handler timerHandler, Runnable timerRunnable,
			ScheduledReminder scheduledReminder, View listItemView) {
		super();
		// this.position = position;
		this.timerHandler = timerHandler;
		this.timerRunnable = timerRunnable;
		this.scheduledReminder = scheduledReminder;
		this.listItemView = listItemView;
		// this.checkBoxIsChecked = checkBoxIsChecked;
		// this.spinnerSelectionPosition = spinnerPosition;
	}

	// public int position;
	public Handler timerHandler;
	public Runnable timerRunnable;
	public ScheduledReminder scheduledReminder;
	public View listItemView;

	// public boolean checkBoxIsChecked;
	// public int spinnerSelectionPosition;
	@Override
	public int hashCode() {
		return scheduledReminder.getId().intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityAlarmScheduledReminderInfoHolder2 other = (ActivityAlarmScheduledReminderInfoHolder2) obj;
		if (scheduledReminder.getId() != other.scheduledReminder.getId())
			return false;
		return true;
	}
}
