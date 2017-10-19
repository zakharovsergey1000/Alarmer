package biz.advancedcalendar.db;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.TaskOccurrence;

public class TaskWithDependents implements Parcelable {
	public Task task;
	public List<Reminder> reminders;
	public List<TaskOccurrence> taskOccurrences;

	public TaskWithDependents(Task task, List<Reminder> reminders,
			List<TaskOccurrence> taskOccurrences) {
		super();
		this.task = task;
		this.reminders = reminders;
		this.taskOccurrences = taskOccurrences;
	}

	public TaskWithDependents() {
	}

	public void init(Task task, List<Reminder> reminders,
			List<TaskOccurrence> taskOccurrences) {
		this.task = task;
		this.reminders = reminders;
		this.taskOccurrences = taskOccurrences;
	}

	public static TaskWithDependents clone(Context context,
			TaskWithDependents taskWithDependents) {
		TaskWithDependents newTaskWithDependents = new TaskWithDependents();
		newTaskWithDependents.task = new Task(taskWithDependents.task);
		List<Reminder> newReminders = new ArrayList<Reminder>();
		for (Reminder reminder : taskWithDependents.reminders) {
			Reminder newReminder = new Reminder(reminder);
			newReminders.add(newReminder);
		}
		newTaskWithDependents.reminders = newReminders;
		List<TaskOccurrence> newTaskOccurrences = new ArrayList<TaskOccurrence>();
		for (TaskOccurrence taskOccurrence : taskWithDependents.taskOccurrences) {
			TaskOccurrence newTaskOccurrence = new TaskOccurrence(taskOccurrence);
			newTaskOccurrences.add(newTaskOccurrence);
		}
		newTaskWithDependents.taskOccurrences = newTaskOccurrences;
		return newTaskWithDependents;
	}

	protected TaskWithDependents(Parcel in) {
		task = (Task) in.readValue(Task.class.getClassLoader());
		if (in.readByte() == 0x01) {
			reminders = new ArrayList<Reminder>();
			in.readList(reminders, Reminder.class.getClassLoader());
		} else {
			reminders = null;
		}
		if (in.readByte() == 0x01) {
			taskOccurrences = new ArrayList<TaskOccurrence>();
			in.readList(taskOccurrences, TaskOccurrence.class.getClassLoader());
		} else {
			taskOccurrences = null;
		}
	}

	public TaskWithDependents(Context context,
			TaskWithDependentsUiData taskWithDependentsUiData) {
		task = new Task(taskWithDependentsUiData.TaskUiData);
		List<Reminder> newReminders = new ArrayList<Reminder>();
		for (Reminder reminder : taskWithDependentsUiData.RemindersUiData) {
			Reminder newReminder = new Reminder(reminder);
			newReminders.add(newReminder);
		}
		reminders = newReminders;
		List<TaskOccurrence> newTaskOccurrences = new ArrayList<TaskOccurrence>();
		for (TaskOccurrence taskOccurrence : taskWithDependentsUiData.TaskOccurrences) {
			TaskOccurrence newTaskOccurrence = new TaskOccurrence(taskOccurrence);
			newTaskOccurrences.add(newTaskOccurrence);
		}
		taskOccurrences = newTaskOccurrences;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(task);
		if (reminders == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(reminders);
		}
		if (taskOccurrences == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(taskOccurrences);
		}
	}

	public static final Parcelable.Creator<TaskWithDependents> CREATOR = new Parcelable.Creator<TaskWithDependents>() {
		@Override
		public TaskWithDependents createFromParcel(Parcel in) {
			return new TaskWithDependents(in);
		}

		@Override
		public TaskWithDependents[] newArray(int size) {
			return new TaskWithDependents[size];
		}
	};
}