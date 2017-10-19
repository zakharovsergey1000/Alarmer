package biz.advancedcalendar.db;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import de.greenrobot.dao.AbstractDao;
import java.util.List;

public class ReminderUiData extends Reminder implements Parcelable {
	TaskUiData taskUiData;

	@Override
	public TaskUiData getTask() {
		return taskUiData;
	}

	@Override
	public void setTask(Task task) {
		taskUiData = new TaskUiData(task, task.getTaskOccurrenceList());
	}

	@Override
	public List<ScheduledReminder> getScheduledReminderList() {
		throw new IllegalAccessError("Not implemented");
	}

	@Override
	public synchronized void resetScheduledReminderList() {
		throw new IllegalAccessError("Not implemented");
	}

	@Override
	public void delete() {
		throw new IllegalAccessError("Not implemented");
	}

	/** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an
	 * entity context. */
	@Override
	public void update() {
		throw new IllegalAccessError("Not implemented");
	}

	/** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an
	 * entity context. */
	@Override
	public void refresh() {
		throw new IllegalAccessError("Not implemented");
	}

	// KEEP METHODS - put your custom methods here
	public ReminderUiData() {
		super();
	}

	public ReminderUiData(Reminder r, Task task) {
		super(r);
		taskUiData = new TaskUiData(task, task.getTaskOccurrenceList());
	}

	@Override
	public List<ScheduledReminder> getScheduledReminderList(Context context, int[] states) {
		throw new IllegalAccessError("Not implemented");
	}

	protected ReminderUiData(Parcel in) {
		super(in);
		taskUiData = (TaskUiData) in.readValue(TaskUiData.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeValue(taskUiData);
	}

	public static final Parcelable.Creator<ReminderUiData> CREATOR = new Parcelable.Creator<ReminderUiData>() {
		@Override
		public ReminderUiData createFromParcel(Parcel in) {
			return new ReminderUiData(in);
		}

		@Override
		public ReminderUiData[] newArray(int size) {
			return new ReminderUiData[size];
		}
	};
}