package biz.advancedcalendar.fragments;

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;

public class TreeViewListItemDescriptionArrayListOfArrayLists implements Parcelable {
	ArrayList<ArrayList<TreeViewListItemDescription>> value;

	public TreeViewListItemDescriptionArrayListOfArrayLists() {
		// TODO Auto-generated constructor stub
	}

	protected TreeViewListItemDescriptionArrayListOfArrayLists(Parcel in) {
		if (in.readByte() == 0x01) {
			value = new ArrayList<ArrayList<TreeViewListItemDescription>>();
			int size = in.readInt();
			for (int i = 0; i < size; i++) {
				ArrayList<TreeViewListItemDescription> x;
				if (in.readByte() == 0x01) {
					x = new ArrayList<TreeViewListItemDescription>();
					in.readList(x, TreeViewListItemDescription.class.getClassLoader());
				} else {
					x = null;
				}
				value.add(x);
			}
		} else {
			value = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (value == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeInt(value.size());
			for (int i = 0; i < value.size(); i++) {
				ArrayList<TreeViewListItemDescription> x = value.get(i);
				if (x == null) {
					dest.writeByte((byte) 0x00);
				} else {
					dest.writeByte((byte) 0x01);
					dest.writeList(x);
				}
			}
		}
	}

	public static final Parcelable.Creator<TreeViewListItemDescriptionArrayListOfArrayLists> CREATOR = new Parcelable.Creator<TreeViewListItemDescriptionArrayListOfArrayLists>() {
		@Override
		public TreeViewListItemDescriptionArrayListOfArrayLists createFromParcel(Parcel in) {
			return new TreeViewListItemDescriptionArrayListOfArrayLists(in);
		}

		@Override
		public TreeViewListItemDescriptionArrayListOfArrayLists[] newArray(int size) {
			return new TreeViewListItemDescriptionArrayListOfArrayLists[size];
		}
	};
}