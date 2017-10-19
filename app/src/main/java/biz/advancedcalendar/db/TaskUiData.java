package biz.advancedcalendar.db;

import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.TaskOccurrence;
import java.util.ArrayList;
import java.util.List;

public class TaskUiData extends Task implements Parcelable {
	private List<TaskOccurrence> taskOccurrenceList;
	private String taskTitleForQuickReminder;

	@Override
	public List<TaskOccurrence> getTaskOccurrenceList() {
		return taskOccurrenceList;
	}

	public void setTaskOccurrenceList(List<TaskOccurrence> taskOccurrenceList) {
		this.taskOccurrenceList = taskOccurrenceList;
	}

	public TaskUiData() {
	}

	public TaskUiData(Task task, List<TaskOccurrence> taskOccurrenceList) {
		super(task);
		this.taskOccurrenceList = new ArrayList<TaskOccurrence>();
		for (TaskOccurrence taskOccurrence : taskOccurrenceList) {
			this.taskOccurrenceList.add(new TaskOccurrence(taskOccurrence));
		}
	}

	public String getTaskTitleForQuickReminder() {
		return taskTitleForQuickReminder;
	}

	public void setTaskTitleForQuickReminder(String taskTitleForQuickReminder) {
		this.taskTitleForQuickReminder = taskTitleForQuickReminder;
	}

	protected TaskUiData(Parcel in) {
		super(in);
		if (in.readByte() == 0x01) {
			taskOccurrenceList = new ArrayList<TaskOccurrence>();
			in.readList(taskOccurrenceList, TaskOccurrence.class.getClassLoader());
		} else {
			taskOccurrenceList = null;
		}
		taskTitleForQuickReminder = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		if (taskOccurrenceList == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(taskOccurrenceList);
		}
		dest.writeString(taskTitleForQuickReminder);
	}

	public static final Parcelable.Creator<TaskUiData> CREATOR = new Parcelable.Creator<TaskUiData>() {
		@Override
		public TaskUiData createFromParcel(Parcel in) {
			return new TaskUiData(in);
		}

		@Override
		public TaskUiData[] newArray(int size) {
			return new TaskUiData[size];
		}
	};
}