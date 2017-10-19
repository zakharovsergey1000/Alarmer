package biz.advancedcalendar.wsdl.sync;

public class EntityListRequest extends AuthServiceRequest {
	public ArrayOfint EntityIdArray = new ArrayOfint();
	public Boolean IncludeDependentEntities;
}
