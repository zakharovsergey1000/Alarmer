package biz.advancedcalendar.greendao;

import android.database.sqlite.SQLiteDatabase;

public class DowngradeException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public SQLiteDatabase db;
	public int oldVersion;
	public int newVersion;

	public DowngradeException(SQLiteDatabase db, int oldVersion, int newVersion) {
		super();
		this.db = db;
		this.oldVersion = oldVersion;
		this.newVersion = newVersion;
	}
}
