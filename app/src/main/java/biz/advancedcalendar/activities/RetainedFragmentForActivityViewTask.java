package biz.advancedcalendar.activities;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import biz.advancedcalendar.db.TaskWithDependentsUiData;

public class RetainedFragmentForActivityViewTask implements Parcelable {
	TaskWithDependentsUiData mTaskWithDependentsUiData;
	int tab;

	public void setTaskWithDependentsUiData(TaskWithDependentsUiData data) {
		mTaskWithDependentsUiData = data;
	}

	public TaskWithDependentsUiData getTaskWithDependentsUiData() {
		return mTaskWithDependentsUiData;
	}

	public int getTab() {
		return tab;
	}

	public void setTab(int tab) {
		this.tab = tab;
	}

	protected RetainedFragmentForActivityViewTask(Parcel in) {
		mTaskWithDependentsUiData = (TaskWithDependentsUiData) in
				.readValue(TaskWithDependentsUiData.class.getClassLoader());
		tab = in.readInt();
	}

	public RetainedFragmentForActivityViewTask() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(mTaskWithDependentsUiData);
		dest.writeInt(tab);
	}

	public static final Parcelable.Creator<RetainedFragmentForActivityViewTask> CREATOR = new Parcelable.Creator<RetainedFragmentForActivityViewTask>() {
		@Override
		public RetainedFragmentForActivityViewTask createFromParcel(Parcel in) {
			return new RetainedFragmentForActivityViewTask(in);
		}

		@Override
		public RetainedFragmentForActivityViewTask[] newArray(int size) {
			return new RetainedFragmentForActivityViewTask[size];
		}
	};
}