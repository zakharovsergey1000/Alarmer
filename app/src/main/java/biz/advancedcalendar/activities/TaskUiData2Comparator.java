package biz.advancedcalendar.activities;

import biz.advancedcalendar.importtask.TaskUiData3;
import java.util.Comparator;

public class TaskUiData2Comparator implements Comparator<TaskUiData3> {
	TaskUiData3 taskWithDependents;

	public float getWeightId(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getId() == null && taskWithDependents.getId() == null) {
			return 3;
		} else if (lhs.getId() != null && taskWithDependents.getId() != null
				&& lhs.getId().equals(taskWithDependents.getId())) {
			return 3;
		}
		return 0;
	}

	public float getWeightParentId(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getParentId() == null && taskWithDependents.getParentId() == null) {
			return 3;
		} else if (lhs.getParentId() != null && taskWithDependents.getParentId() != null
				&& lhs.getParentId().equals(taskWithDependents.getParentId())) {
			return 2;
		}
		return 0;
	}

	public float getWeightName(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getName() == null && taskWithDependents.getName() == null) {
			return 3;
		} else if (lhs.getName() != null && taskWithDependents.getName() != null
				&& lhs.getName().equals(taskWithDependents.getName())) {
			return 4;
		}
		return 0;
	}

	public float getWeightColor(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getColor() == null && taskWithDependents.getColor() == null) {
			return 3;
		} else if (lhs.getColor() != null && taskWithDependents.getColor() != null
				&& lhs.getColor().equals(taskWithDependents.getColor())) {
			return 0.5f;
		}
		return 0;
	}

	public float getWeightStartDateTime(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getStartDateTime() == null
				&& taskWithDependents.getStartDateTime() == null) {
			return 3;
		} else if (lhs.getStartDateTime() != null
				&& taskWithDependents.getStartDateTime() != null
				&& lhs.getStartDateTime().equals(taskWithDependents.getStartDateTime())) {
			return 1;
		}
		return 0;
	}

	public float getWeightEndDateTime(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getEndDateTime() == null && taskWithDependents.getEndDateTime() == null) {
			return 3;
		} else if (lhs.getEndDateTime() != null
				&& taskWithDependents.getEndDateTime() != null
				&& lhs.getEndDateTime().equals(taskWithDependents.getEndDateTime())) {
			return 1;
		}
		return 0;
	}

	public float getWeightIsCompleted(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getIsCompleted() == taskWithDependents.getIsCompleted()) {
			return 0.1f;
		}
		return 0;
	}

	public float getWeightPercentOfCompletion(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getPercentOfCompletion() == taskWithDependents.getPercentOfCompletion()) {
			return 0.1f;
		}
		return 0;
	}

	public float getWeightPriority(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getPriority() == taskWithDependents.getPriority()) {
			return 0.1f;
		}
		return 0;
	}

	public float getWeightSortOrder(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getSortOrder() == taskWithDependents.getSortOrder()) {
			return 0.1f;
		}
		return 0;
	}

	public float getWeightDescription(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getDescription() == null && taskWithDependents.getDescription() == null) {
			return 3;
		} else if (lhs.getDescription() != null
				&& taskWithDependents.getDescription() != null
				&& lhs.getDescription().equals(taskWithDependents.getDescription())) {
			return 4;
		}
		return 0;
	}

	public float getWeightLocation(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getLocation() == null && taskWithDependents.getLocation() == null) {
			return 3;
		} else if (lhs.getLocation() != null && taskWithDependents.getLocation() != null
				&& lhs.getLocation().equals(taskWithDependents.getLocation())) {
			return 0.3f;
		}
		return 0;
	}

	public float getWeightRecurrenceIntervalValue(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getRecurrenceIntervalValue() == taskWithDependents
				.getRecurrenceIntervalValue()) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightTimeUnitsCount(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getTimeUnitsCount() == null
				&& taskWithDependents.getTimeUnitsCount() == null) {
			return 3;
		} else if (lhs.getTimeUnitsCount() != null
				&& taskWithDependents.getTimeUnitsCount() != null
				&& lhs.getTimeUnitsCount().equals(taskWithDependents.getTimeUnitsCount())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightOccurrencesMaxCount(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getOccurrencesMaxCount() == null
				&& taskWithDependents.getOccurrencesMaxCount() == null) {
			return 3;
		} else if (lhs.getOccurrencesMaxCount() != null
				&& taskWithDependents.getOccurrencesMaxCount() != null
				&& lhs.getOccurrencesMaxCount().equals(
						taskWithDependents.getOccurrencesMaxCount())) {
			return 0.1f;
		}
		return 0;
	}

	public float getWeightRepetitionEndDateTime(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getRepetitionEndDateTime() == null
				&& taskWithDependents.getRepetitionEndDateTime() == null) {
			return 3;
		} else if (lhs.getRepetitionEndDateTime() != null
				&& taskWithDependents.getRepetitionEndDateTime() != null
				&& lhs.getRepetitionEndDateTime().equals(
						taskWithDependents.getRepetitionEndDateTime())) {
			return 0.5f;
		}
		return 0;
	}

	public float getWeightAlarmRingtone(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getAlarmRingtone() == null
				&& taskWithDependents.getAlarmRingtone() == null) {
			return 3;
		} else if (lhs.getAlarmRingtone() != null
				&& taskWithDependents.getAlarmRingtone() != null
				&& lhs.getAlarmRingtone().equals(taskWithDependents.getAlarmRingtone())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightNotificationRingtone(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getNotificationRingtone() == null
				&& taskWithDependents.getNotificationRingtone() == null) {
			return 3;
		} else if (lhs.getNotificationRingtone() != null
				&& taskWithDependents.getNotificationRingtone() != null
				&& lhs.getNotificationRingtone().equals(
						taskWithDependents.getNotificationRingtone())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightRingtoneFadeInTime(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getRingtoneFadeInTime() == null
				&& taskWithDependents.getRingtoneFadeInTime() == null) {
			return 3;
		} else if (lhs.getRingtoneFadeInTime() != null
				&& taskWithDependents.getRingtoneFadeInTime() != null
				&& lhs.getRingtoneFadeInTime().equals(
						taskWithDependents.getRingtoneFadeInTime())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightPlayingTime(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getPlayingTime() == null && taskWithDependents.getPlayingTime() == null) {
			return 3;
		} else if (lhs.getPlayingTime() != null
				&& taskWithDependents.getPlayingTime() != null
				&& lhs.getPlayingTime().equals(taskWithDependents.getPlayingTime())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightAutomaticSnoozeDuration(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getAutomaticSnoozeDuration() == null
				&& taskWithDependents.getAutomaticSnoozeDuration() == null) {
			return 3;
		} else if (lhs.getAutomaticSnoozeDuration() != null
				&& taskWithDependents.getAutomaticSnoozeDuration() != null
				&& lhs.getAutomaticSnoozeDuration().equals(
						taskWithDependents.getAutomaticSnoozeDuration())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightAutomaticSnoozesMaxCount(TaskUiData3 lhs,
			TaskUiData3 taskWithDependents) {
		if (lhs.getAutomaticSnoozesMaxCount() == null
				&& taskWithDependents.getAutomaticSnoozesMaxCount() == null) {
			return 3;
		} else if (lhs.getAutomaticSnoozesMaxCount() != null
				&& taskWithDependents.getAutomaticSnoozesMaxCount() != null
				&& lhs.getAutomaticSnoozesMaxCount().equals(
						taskWithDependents.getAutomaticSnoozesMaxCount())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightVibrate(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getVibrate() == null && taskWithDependents.getVibrate() == null) {
			return 3;
		} else if (lhs.getVibrate() != null && taskWithDependents.getVibrate() != null
				&& lhs.getVibrate().equals(taskWithDependents.getVibrate())) {
			return 0.1f;
		}
		return 0;
	}

	public float getWeightVibratePattern(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getVibratePattern() == null
				&& taskWithDependents.getVibratePattern() == null) {
			return 3;
		} else if (lhs.getVibratePattern() != null
				&& taskWithDependents.getVibratePattern() != null
				&& lhs.getVibratePattern().equals(taskWithDependents.getVibratePattern())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightLed(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getLed() == null && taskWithDependents.getLed() == null) {
			return 3;
		} else if (lhs.getLed() != null && taskWithDependents.getLed() != null
				&& lhs.getLed().equals(taskWithDependents.getLed())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightLedPattern(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getLedPattern() == null && taskWithDependents.getLedPattern() == null) {
			return 3;
		} else if (lhs.getLedPattern() != null
				&& taskWithDependents.getLedPattern() != null
				&& lhs.getLedPattern().equals(taskWithDependents.getLedPattern())) {
			return 0.2f;
		}
		return 0;
	}

	public float getWeightLedColor(TaskUiData3 lhs, TaskUiData3 taskWithDependents) {
		if (lhs.getLedColor() == null && taskWithDependents.getLedColor() == null) {
			return 3;
		} else if (lhs.getLedColor() != null && taskWithDependents.getLedColor() != null
				&& lhs.getLedColor().equals(taskWithDependents.getLedColor())) {
			return 0.2f;
		}
		return 0;
	}

	public TaskUiData2Comparator(TaskUiData3 taskWithDependents) {
		this.taskWithDependents = taskWithDependents;
	}

	@Override
	public int compare(TaskUiData3 lhs, TaskUiData3 rhs) {
		float lhsWeightSum = getWeightSum(lhs);
		float rhsWeightSum = getWeightSum(rhs);
		return lhsWeightSum == rhsWeightSum ? 0 : lhsWeightSum > rhsWeightSum ? -1 : 1;
	}

	private float getWeightSum(TaskUiData3 lhs) {
		float lhsWeightSum = 0 + getWeightId(lhs, taskWithDependents)
				+ getWeightParentId(lhs, taskWithDependents)
				+ getWeightName(lhs, taskWithDependents)
				+ getWeightColor(lhs, taskWithDependents)
				+ getWeightStartDateTime(lhs, taskWithDependents)
				+ getWeightEndDateTime(lhs, taskWithDependents)
				+ getWeightIsCompleted(lhs, taskWithDependents)
				+ getWeightPercentOfCompletion(lhs, taskWithDependents)
				+ getWeightPriority(lhs, taskWithDependents)
				+ getWeightSortOrder(lhs, taskWithDependents)
				+ getWeightDescription(lhs, taskWithDependents)
				+ getWeightLocation(lhs, taskWithDependents)
				+ getWeightRecurrenceIntervalValue(lhs, taskWithDependents)
				+ getWeightTimeUnitsCount(lhs, taskWithDependents)
				+ getWeightOccurrencesMaxCount(lhs, taskWithDependents)
				+ getWeightRepetitionEndDateTime(lhs, taskWithDependents)
				+ getWeightAlarmRingtone(lhs, taskWithDependents)
				+ getWeightNotificationRingtone(lhs, taskWithDependents)
				+ getWeightRingtoneFadeInTime(lhs, taskWithDependents)
				+ getWeightPlayingTime(lhs, taskWithDependents)
				+ getWeightAutomaticSnoozeDuration(lhs, taskWithDependents)
				+ getWeightAutomaticSnoozesMaxCount(lhs, taskWithDependents)
				+ getWeightVibrate(lhs, taskWithDependents)
				+ getWeightVibratePattern(lhs, taskWithDependents)
				+ getWeightLed(lhs, taskWithDependents)
				+ getWeightLedPattern(lhs, taskWithDependents)
				+ getWeightLedColor(lhs, taskWithDependents);
		// if(lhs.getId() == null && taskWithDependents.getId() == null){lhsWeightSum+=3;}
		// else if(lhs.getId() != null && taskWithDependents.getId() !=
		// null&&lhs.getId().equals(taskWithDependents.getId())){lhsWeightSum+=3;}
		// if(lhs.getParentId() == null && taskWithDependents.getParentId() ==
		// null){lhsWeightSum+=3;} else if(lhs.getParentId() != null &&
		// taskWithDependents.getParentId() !=
		// null&&lhs.getParentId().equals(taskWithDependents.getParentId())){lhsWeightSum+=2;}
		// if(lhs.getName() == null && taskWithDependents.getName() ==
		// null){lhsWeightSum+=3;} else if(lhs.getName() != null &&
		// taskWithDependents.getName() !=
		// null&&lhs.getName().equals(taskWithDependents.getName())){lhsWeightSum+=4;}
		// if(lhs.getColor() == null && taskWithDependents.getColor() ==
		// null){lhsWeightSum+=3;} else if(lhs.getColor() != null &&
		// taskWithDependents.getColor() !=
		// null&&lhs.getColor().equals(taskWithDependents.getColor())){lhsWeightSum+=0.5f;}
		// if(lhs.getStartDateTime() == null && taskWithDependents.getStartDateTime() ==
		// null){lhsWeightSum+=3;} else if(lhs.getStartDateTime() != null &&
		// taskWithDependents.getStartDateTime() !=
		// null&&lhs.getStartDateTime().equals(taskWithDependents.getStartDateTime())){lhsWeightSum+=1;}
		// if(lhs.getEndDateTime() == null && taskWithDependents.getEndDateTime() ==
		// null){lhsWeightSum+=3;} else if(lhs.getEndDateTime() != null &&
		// taskWithDependents.getEndDateTime() !=
		// null&&lhs.getEndDateTime().equals(taskWithDependents.getEndDateTime())){lhsWeightSum+=1;}
		// if(lhs.getIsCompleted() ==
		// taskWithDependents.getIsCompleted()){lhsWeightSum+=0.1f;}
		// if(lhs.getPercentOfCompletion() ==
		// taskWithDependents.getPercentOfCompletion()){lhsWeightSum+=0.1f;}
		// if(lhs.getPriority() == taskWithDependents.getPriority()){lhsWeightSum+=0.1f;}
		// if(lhs.getSortOrder() ==
		// taskWithDependents.getSortOrder()){lhsWeightSum+=0.1f;}
		// if(lhs.getDescription() == null && taskWithDependents.getDescription() ==
		// null){lhsWeightSum+=3;} else if(lhs.getDescription() != null &&
		// taskWithDependents.getDescription() !=
		// null&&lhs.getDescription().equals(taskWithDependents.getDescription())){lhsWeightSum+=4;}
		// if(lhs.getLocation() == null && taskWithDependents.getLocation() ==
		// null){lhsWeightSum+=3;} else if(lhs.getLocation() != null &&
		// taskWithDependents.getLocation() !=
		// null&&lhs.getLocation().equals(taskWithDependents.getLocation())){lhsWeightSum+=0.3f;}
		// if(lhs.getRecurrenceIntervalValue() ==
		// taskWithDependents.getRecurrenceIntervalValue()){lhsWeightSum+=0.2f;}
		// if(lhs.getTimeUnitsCount() == null && taskWithDependents.getTimeUnitsCount() ==
		// null){lhsWeightSum+=3;} else if(lhs.getTimeUnitsCount() != null &&
		// taskWithDependents.getTimeUnitsCount() !=
		// null&&lhs.getTimeUnitsCount().equals(taskWithDependents.getTimeUnitsCount())){lhsWeightSum+=0.2f;}
		// if(lhs.getOccurrencesMaxCount() == null &&
		// taskWithDependents.getOccurrencesMaxCount() == null){lhsWeightSum+=3;} else
		// if(lhs.getOccurrencesMaxCount() != null &&
		// taskWithDependents.getOccurrencesMaxCount() !=
		// null&&lhs.getOccurrencesMaxCount().equals(taskWithDependents.getOccurrencesMaxCount())){lhsWeightSum+=0.1f;}
		// if(lhs.getRepetitionEndDateTime() == null &&
		// taskWithDependents.getRepetitionEndDateTime() == null){lhsWeightSum+=3;} else
		// if(lhs.getRepetitionEndDateTime() != null &&
		// taskWithDependents.getRepetitionEndDateTime() !=
		// null&&lhs.getRepetitionEndDateTime().equals(taskWithDependents.getRepetitionEndDateTime())){lhsWeightSum+=0.5f;}
		// if(lhs.getAlarmRingtone() == null && taskWithDependents.getAlarmRingtone() ==
		// null){lhsWeightSum+=3;} else if(lhs.getAlarmRingtone() != null &&
		// taskWithDependents.getAlarmRingtone() !=
		// null&&lhs.getAlarmRingtone().equals(taskWithDependents.getAlarmRingtone())){lhsWeightSum+=0.2f;}
		// if(lhs.getNotificationRingtone() == null &&
		// taskWithDependents.getNotificationRingtone() == null){lhsWeightSum+=3;} else
		// if(lhs.getNotificationRingtone() != null &&
		// taskWithDependents.getNotificationRingtone() !=
		// null&&lhs.getNotificationRingtone().equals(taskWithDependents.getNotificationRingtone())){lhsWeightSum+=0.2f;}
		// if(lhs.getRingtoneFadeInTime() == null &&
		// taskWithDependents.getRingtoneFadeInTime() == null){lhsWeightSum+=3;} else
		// if(lhs.getRingtoneFadeInTime() != null &&
		// taskWithDependents.getRingtoneFadeInTime() !=
		// null&&lhs.getRingtoneFadeInTime().equals(taskWithDependents.getRingtoneFadeInTime())){lhsWeightSum+=0.2f;}
		// if(lhs.getPlayingTime() == null && taskWithDependents.getPlayingTime() ==
		// null){lhsWeightSum+=3;} else if(lhs.getPlayingTime() != null &&
		// taskWithDependents.getPlayingTime() !=
		// null&&lhs.getPlayingTime().equals(taskWithDependents.getPlayingTime())){lhsWeightSum+=0.2f;}
		// if(lhs.getAutomaticSnoozeDuration() == null &&
		// taskWithDependents.getAutomaticSnoozeDuration() == null){lhsWeightSum+=3;} else
		// if(lhs.getAutomaticSnoozeDuration() != null &&
		// taskWithDependents.getAutomaticSnoozeDuration() !=
		// null&&lhs.getAutomaticSnoozeDuration().equals(taskWithDependents.getAutomaticSnoozeDuration())){lhsWeightSum+=0.2f;}
		// if(lhs.getAutomaticSnoozesMaxCount() == null &&
		// taskWithDependents.getAutomaticSnoozesMaxCount() == null){lhsWeightSum+=3;}
		// else if(lhs.getAutomaticSnoozesMaxCount() != null &&
		// taskWithDependents.getAutomaticSnoozesMaxCount() !=
		// null&&lhs.getAutomaticSnoozesMaxCount().equals(taskWithDependents.getAutomaticSnoozesMaxCount())){lhsWeightSum+=0.2f;}
		// if(lhs.getVibrate() == null && taskWithDependents.getVibrate() ==
		// null){lhsWeightSum+=3;} else if(lhs.getVibrate() != null &&
		// taskWithDependents.getVibrate() !=
		// null&&lhs.getVibrate().equals(taskWithDependents.getVibrate())){lhsWeightSum+=0.1f;}
		// if(lhs.getVibratePattern() == null && taskWithDependents.getVibratePattern() ==
		// null){lhsWeightSum+=3;} else if(lhs.getVibratePattern() != null &&
		// taskWithDependents.getVibratePattern() !=
		// null&&lhs.getVibratePattern().equals(taskWithDependents.getVibratePattern())){lhsWeightSum+=0.2f;}
		// if(lhs.getLed() == null && taskWithDependents.getLed() ==
		// null){lhsWeightSum+=3;} else if(lhs.getLed() != null &&
		// taskWithDependents.getLed() !=
		// null&&lhs.getLed().equals(taskWithDependents.getLed())){lhsWeightSum+=0.2f;}
		// if(lhs.getLedPattern() == null && taskWithDependents.getLedPattern() ==
		// null){lhsWeightSum+=3;} else if(lhs.getLedPattern() != null &&
		// taskWithDependents.getLedPattern() !=
		// null&&lhs.getLedPattern().equals(taskWithDependents.getLedPattern())){lhsWeightSum+=0.2f;}
		// if(lhs.getLedColor() == null && taskWithDependents.getLedColor() ==
		// null){lhsWeightSum+=3;} else if(lhs.getLedColor() != null &&
		// taskWithDependents.getLedColor() !=
		// null&&lhs.getLedColor().equals(taskWithDependents.getLedColor())){lhsWeightSum+=0.2f;}
		// if(lhs.getTaskOccurrenceList() == null &&
		// taskWithDependents.getTaskOccurrenceList() == null){lhsWeightSum+=3;} else
		// if(lhs.getTaskOccurrenceList() != null &&
		// taskWithDependents.getTaskOccurrenceList() !=
		// null&&lhs.getTaskOccurrenceList().equals(taskWithDependents.getTaskOccurrenceList())){lhsWeightSum+=1;}
		// if(lhs.getReminderList() == null && taskWithDependents.getReminderList() ==
		// null){lhsWeightSum+=3;} else if(lhs.getReminderList() != null &&
		// taskWithDependents.getReminderList() !=
		// null&&lhs.getReminderList().equals(taskWithDependents.getReminderList())){lhsWeightSum+=1;}
		return lhsWeightSum;
	}
}
