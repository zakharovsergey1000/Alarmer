package biz.advancedcalendar.views.accessories;

import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class InformationUnit implements Parcelable {
	String whateverDelimiterString;
	InformationUnitSelector informationUnitSelector;

	public InformationUnit(InformationUnitSelector informationUnitSelector) {
		this.informationUnitSelector = informationUnitSelector;
		initialize();
	}

	private void initialize() {
		whateverDelimiterString = "";
	}

	public String getWhateverDelimiterString() {
		return whateverDelimiterString;
	}

	public void setWhateverDelimiterString(String whateverDelimiterString) {
		this.whateverDelimiterString = whateverDelimiterString;
	}

	public InformationUnitSelector getInformationUnitSelector() {
		return informationUnitSelector;
	}

	public void setInformationUnitSelector(InformationUnitSelector informationUnitSelector) {
		this.informationUnitSelector = informationUnitSelector;
	}

	protected InformationUnit(Parcel in) {
		whateverDelimiterString = in.readString();
		informationUnitSelector = in.readByte() == 0x00 ? null : InformationUnitSelector
				.fromInt(in.readByte());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(whateverDelimiterString);
		if (informationUnitSelector == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeByte(informationUnitSelector.getValue());
		}
	}

	public static final Parcelable.Creator<InformationUnit> CREATOR = new Parcelable.Creator<InformationUnit>() {
		@Override
		public InformationUnit createFromParcel(Parcel in) {
			return new InformationUnit(in);
		}

		@Override
		public InformationUnit[] newArray(int size) {
			return new InformationUnit[size];
		}
	};

	public static class InformationUnitRow implements Parcelable {
		private ArrayList<InformationUnit> informationUnits;

		public InformationUnitRow(ArrayList<InformationUnit> informationUnits) {
			this.informationUnits = informationUnits;
		}

		public ArrayList<InformationUnit> getInformationUnits() {
			return informationUnits;
		}

		protected InformationUnitRow(Parcel in) {
			if (in.readByte() == 0x01) {
				informationUnits = new ArrayList<InformationUnit>();
				in.readList(informationUnits, InformationUnit.class.getClassLoader());
			} else {
				informationUnits = null;
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (informationUnits == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(informationUnits);
			}
		}

		public static final Parcelable.Creator<InformationUnitRow> CREATOR = new Parcelable.Creator<InformationUnitRow>() {
			@Override
			public InformationUnitRow createFromParcel(Parcel in) {
				return new InformationUnitRow(in);
			}

			@Override
			public InformationUnitRow[] newArray(int size) {
				return new InformationUnitRow[size];
			}
		};
	}

	public static class InformationUnitSortOrderComparatorBySortOrder implements
			Comparator<InformationUnitSortOrder>, Serializable {
		private static final long serialVersionUID = 2L;

		@Override
		public int compare(InformationUnitSortOrder lhs, InformationUnitSortOrder rhs) {
			if (lhs == null) {
				return 1;
			} else if (rhs == null) {
				return -1;
			} else if (lhs.sortOrder < rhs.sortOrder) {
				return -1;
			} else if (lhs.sortOrder > rhs.sortOrder) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public static class InformationUnitSortOrder implements Parcelable,
			Comparable<InformationUnitSortOrder> {
		private int index;
		private int sortOrder;

		public InformationUnitSortOrder(int index, int sortOrder) {
			this.index = index;
			this.sortOrder = sortOrder;
		}

		@Override
		public int compareTo(InformationUnitSortOrder another) {
			if (index < another.index) {
				return -1;
			} else if (index > another.index) {
				return 1;
			} else {
				return 0;
			}
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public int getSortOrder() {
			return sortOrder;
		}

		public void setSortOrder(int sortOrder) {
			this.sortOrder = sortOrder;
		}

		protected InformationUnitSortOrder(Parcel in) {
			index = in.readInt();
			sortOrder = in.readInt();
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(index);
			dest.writeInt(sortOrder);
		}

		public static final Parcelable.Creator<InformationUnitSortOrder> CREATOR = new Parcelable.Creator<InformationUnitSortOrder>() {
			@Override
			public InformationUnitSortOrder createFromParcel(Parcel in) {
				return new InformationUnitSortOrder(in);
			}

			@Override
			public InformationUnitSortOrder[] newArray(int size) {
				return new InformationUnitSortOrder[size];
			}
		};
	}

	public static class InformationUnitSortOrdersHolder implements Parcelable {
		private ArrayList<InformationUnitSortOrder> informationUnitSortOrders;

		public InformationUnitSortOrdersHolder(
				ArrayList<InformationUnitSortOrder> informationUnitSortOrders) {
			this.informationUnitSortOrders = informationUnitSortOrders;
		}

		public ArrayList<InformationUnitSortOrder> getInformationUnitSortOrders() {
			return informationUnitSortOrders;
		}

		public void setInformationUnitSortOrders(
				ArrayList<InformationUnitSortOrder> informationUnitSortOrders) {
			this.informationUnitSortOrders = informationUnitSortOrders;
		}

		protected InformationUnitSortOrdersHolder(Parcel in) {
			if (in.readByte() == 0x01) {
				informationUnitSortOrders = new ArrayList<InformationUnitSortOrder>();
				in.readList(informationUnitSortOrders,
						InformationUnitSortOrder.class.getClassLoader());
			} else {
				informationUnitSortOrders = null;
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (informationUnitSortOrders == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(informationUnitSortOrders);
			}
		}

		public static final Parcelable.Creator<InformationUnitSortOrdersHolder> CREATOR = new Parcelable.Creator<InformationUnitSortOrdersHolder>() {
			@Override
			public InformationUnitSortOrdersHolder createFromParcel(Parcel in) {
				return new InformationUnitSortOrdersHolder(in);
			}

			@Override
			public InformationUnitSortOrdersHolder[] newArray(int size) {
				return new InformationUnitSortOrdersHolder[size];
			}
		};
	}
}
