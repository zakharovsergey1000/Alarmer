package biz.advancedcalendar.importtask;

import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.activities.activityedittask.TaskUiData2;

public class TaskUiData3 extends TaskUiData2 {
	private String parentTaskName;
	private String sortOrderSiblingName;

	public TaskUiData3() {
	}

	public TaskUiData3(TaskUiData2 TaskUiData2, String parentTaskName,
			String sortOrderSiblingName) {
		super(TaskUiData2);
		this.parentTaskName = parentTaskName;
		this.sortOrderSiblingName = sortOrderSiblingName;
	}

	public TaskUiData3(TaskUiData3 TaskUiData3) {
		this(TaskUiData3, TaskUiData3.parentTaskName, TaskUiData3.sortOrderSiblingName);
	}

	protected TaskUiData3(Parcel in) {
		super(in);
		parentTaskName = in.readString();
		sortOrderSiblingName = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(parentTaskName);
		dest.writeString(sortOrderSiblingName);
	}

	public String getParentTaskName() {
		return parentTaskName;
	}

	public void setParentTaskName(String parentTaskName) {
		this.parentTaskName = parentTaskName;
	}

	public String getSortOrderSiblingName() {
		return sortOrderSiblingName;
	}

	public void setSortOrderSiblingName(String sortOrderSiblingName) {
		this.sortOrderSiblingName = sortOrderSiblingName;
	}

	public static final Parcelable.Creator<TaskUiData3> CREATOR = new Parcelable.Creator<TaskUiData3>() {
		@Override
		public TaskUiData3 createFromParcel(Parcel in) {
			return new TaskUiData3(in);
		}

		@Override
		public TaskUiData3[] newArray(int size) {
			return new TaskUiData3[size];
		}
	};
}
