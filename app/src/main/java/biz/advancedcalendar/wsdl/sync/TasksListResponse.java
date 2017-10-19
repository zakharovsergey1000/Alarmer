package biz.advancedcalendar.wsdl.sync;

import java.util.ArrayList;
import java.util.List;

public class TasksListResponse {
	public List<TaskDto> Entities = new ArrayList<TaskDto>();
	public Enums.GetEntityListResult Result;
	public java.util.Date ServerTime;
}
