package biz.advancedcalendar.activities.accessories;

import android.os.Parcel;
import android.os.Parcelable;

public class QiuckReminder implements Parcelable {
	public boolean IsAlarm;
	public boolean IsAbsoluteRemindTime;
	public int DaysAfterNow;
	public int HoursAfterNow;
	public int MinutesAfterNow;
	public Long RemindAt;
	public String RemindText;

	protected QiuckReminder(Parcel in) {
		IsAlarm = in.readByte() != 0x00;
		IsAbsoluteRemindTime = in.readByte() != 0x00;
		DaysAfterNow = in.readInt();
		HoursAfterNow = in.readInt();
		MinutesAfterNow = in.readInt();
		RemindAt = in.readByte() == 0x00 ? null : in.readLong();
		RemindText = in.readString();
	}

	public QiuckReminder(boolean isAlarm, boolean isAbsoluteRemindTime, int daysAfterNow,
			int hoursAfterNow, int minutesAfterNow, Long remindAt, String remindText) {
		super();
		IsAlarm = isAlarm;
		IsAbsoluteRemindTime = isAbsoluteRemindTime;
		DaysAfterNow = daysAfterNow;
		HoursAfterNow = hoursAfterNow;
		MinutesAfterNow = minutesAfterNow;
		RemindAt = remindAt;
		RemindText = remindText;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte((byte) (IsAlarm ? 0x01 : 0x00));
		dest.writeByte((byte) (IsAbsoluteRemindTime ? 0x01 : 0x00));
		dest.writeInt(DaysAfterNow);
		dest.writeInt(HoursAfterNow);
		dest.writeInt(MinutesAfterNow);
		if (RemindAt == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(RemindAt);
		}
		dest.writeString(RemindText);
	}

	public static final Parcelable.Creator<QiuckReminder> CREATOR = new Parcelable.Creator<QiuckReminder>() {
		@Override
		public QiuckReminder createFromParcel(Parcel in) {
			return new QiuckReminder(in);
		}

		@Override
		public QiuckReminder[] newArray(int size) {
			return new QiuckReminder[size];
		}
	};
}