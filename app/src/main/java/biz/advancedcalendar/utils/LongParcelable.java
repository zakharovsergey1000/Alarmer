package biz.advancedcalendar.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class LongParcelable implements Parcelable {
	public Long value;

	public LongParcelable() {
	}

	public LongParcelable(Long value) {
		this.value = value;
	}

	protected LongParcelable(Parcel in) {
		value = in.readByte() == 0x00 ? null : in.readLong();
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
			dest.writeLong(value);
		}
	}

	public static final Parcelable.Creator<LongParcelable> CREATOR = new Parcelable.Creator<LongParcelable>() {
		@Override
		public LongParcelable createFromParcel(Parcel in) {
			return new LongParcelable(in);
		}

		@Override
		public LongParcelable[] newArray(int size) {
			return new LongParcelable[size];
		}
	};
}