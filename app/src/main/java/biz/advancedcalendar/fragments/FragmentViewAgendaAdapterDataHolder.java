package biz.advancedcalendar.fragments;

import java.util.List;

import biz.advancedcalendar.greendao.Task;

public interface FragmentViewAgendaAdapterDataHolder {
    Long getDateTimeOnVirtualMiddleOffsetWithTimeAtStartOfDay();

    void setDateTimeOnVirtualMiddleOffset(Long dateTimeOnVirtualMiddleOffset);

    List<Task> getTasks();

    void setTasks(List<Task> tasks);
}