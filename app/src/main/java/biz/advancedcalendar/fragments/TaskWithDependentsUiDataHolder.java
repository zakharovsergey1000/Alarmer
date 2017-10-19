package biz.advancedcalendar.fragments;

import biz.advancedcalendar.activities.ActivityEditTask.ActivityEditTaskDataFragment;
import biz.advancedcalendar.db.TaskWithDependentsUiData;
import biz.advancedcalendar.fragments.FragmentEditTaskPartMain.UserInterfaceData;

public interface TaskWithDependentsUiDataHolder {
	TaskWithDependentsUiData getTaskWithDependentsUiData();

	UserInterfaceData getUserInterfaceData();

	ActivityEditTaskDataFragment getDataFragment();
}
