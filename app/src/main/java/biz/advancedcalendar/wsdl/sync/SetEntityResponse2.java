package biz.advancedcalendar.wsdl.sync;

public class SetEntityResponse2 {
	public Long EntityId;
	public String ErrorMessage;
	public java.util.Date LastMod;
	public ArrayOfEntityData NotFoundRelatedEntities = new ArrayOfEntityData();
	public Enums.SetEntityResult Result;
	public java.util.Date ServerTime;
}
