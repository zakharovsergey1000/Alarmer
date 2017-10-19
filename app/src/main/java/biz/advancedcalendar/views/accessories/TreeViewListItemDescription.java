package biz.advancedcalendar.views.accessories;

import android.os.Parcelable;

public interface TreeViewListItemDescription extends Parcelable {
	Parcelable getTag();

	Long getId();

	Long getParentId();

	short getDeepLevel();

	String getDescription();

	int getSortOrder();
	// void setTag(Object tag);
	//
	// void setId(Long id);
	//
	// void setParentId(Long id);
	//
	// void setDeepLevel(short id);
	//
	// void setDescription(String description);
	//
	// void setSortOrder(int sortOrder);
}
