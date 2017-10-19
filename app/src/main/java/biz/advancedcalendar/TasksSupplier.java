package biz.advancedcalendar;

import biz.advancedcalendar.greendao.Task;
import java.util.List;

public interface TasksSupplier {
	List<Task> getTasks();
}