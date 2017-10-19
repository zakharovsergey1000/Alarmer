package biz.advancedcalendar.db;


public class ReminderForViewing {

	private long mDate;
	private String mText;
	private String mWhere;
	private boolean mActive;

	/**
	 * @param date
	 * @param text
	 * @param where
	 */
	public ReminderForViewing(long date, String text, String where,boolean active) {
		super();
		this.mDate = date;
		this.mText = text;
		this.mWhere = where;
		this.mActive = active;
	}

	/**
	 * @return the date
	 */
	public long getDate() {
		return mDate;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(long date) {
		this.mDate = date;
	}

	/**
	 * @return the text
	 */
	public String getText() {
		return mText;
	}

	/**
	 * @param text
	 *            the text to set
	 */
	public void setText(String text) {
		this.mText = text;
	}

	/**
	 * @return the where
	 */
	public String getWhere() {
		return mWhere;
	}

	/**
	 * @param where
	 *            the where to set
	 */
	public void setWhere(String where) {
		this.mWhere = where;
	}

	/**
	 * @return the active
	 */
	public boolean getActive() {
		return mActive;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(boolean active) {
		this.mActive = active;
	}

}
