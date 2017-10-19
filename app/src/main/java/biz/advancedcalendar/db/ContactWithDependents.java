package biz.advancedcalendar.db;

import java.util.List;
import biz.advancedcalendar.greendao.Contact;
import biz.advancedcalendar.greendao.ContactData;

public class ContactWithDependents {
	public	Contact contact;
	public	List<Long> taskIdList;
	//public	boolean isServerTaskIdList;
	public	List<Long> labelIdList;
	//public	boolean isServerLabelIdList;
	public	List<Long> fileIdList;
	//public	boolean isServerFileIdList;
	public	List<ContactData> contactDataList;
}
