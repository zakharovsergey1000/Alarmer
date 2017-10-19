package biz.advancedcalendar.views.accessories;

import java.util.ArrayList;
import java.util.List;

public class ExpandListGroup<T1, T2> {
	private int virtualGroupPosition;
	// private boolean isExpanded = false;
	private T1 Data;
	List<T2> Entities = new ArrayList<T2>();

	public ExpandListGroup(int virtualGroupPosition, T1 data, List<T2> entities) {
		this.virtualGroupPosition = virtualGroupPosition;
		Data = data;
		Entities = entities;
	}

	public T1 getData() {
		return Data;
	}

	public List<T2> getItems() {
		return Entities;
	}

	public int getVirtualGroupPosition() {
		return virtualGroupPosition;
	}
	// public boolean isExpanded() {
	// return isExpanded;
	// }
	//
	// public void setExpanded(boolean isExpanded) {
	// this.isExpanded = isExpanded;
	// }
}