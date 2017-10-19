package biz.advancedcalendar.db;

import java.util.List;
import biz.advancedcalendar.greendao.Label;

public class LabelWithDependents {
	public	Label label;
	public	List<Long> taskIdList;
	//public	boolean isServerTaskIdList;
	public	List<Long> contactIdList;
	//public	boolean isServerContactIdList;
	public	List<Long> fileIdList;
	//public	boolean isServerFileIdList;
}
