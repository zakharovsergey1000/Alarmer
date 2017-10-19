package biz.advancedcalendar.db;

import java.util.List;

import biz.advancedcalendar.greendao.File;

public class FileWithDependents {
	public	File file;
	public	List<Long> taskIdList;
	public	boolean isServerTaskIdList;
	public	List<Long> labelIdList;
	public	boolean isServerLabelIdList;
	public	List<Long> contactIdList;
	public	boolean isServerContactIdList;
	public	List<File> oldVersion;
}
