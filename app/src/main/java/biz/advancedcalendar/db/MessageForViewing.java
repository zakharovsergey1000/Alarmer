package biz.advancedcalendar.db;

import java.util.Date;

public class MessageForViewing {
	private String mFromUser;
	private Date mDate;
	private String mText;
	
	
	/**
	 * @param fromUser
	 * @param date
	 * @param text
	 */
	public MessageForViewing(String fromUser, Date date, String text) {
		super();
		this.mFromUser = fromUser;
		this.mDate = date;
		this.mText = text;
	}
	/**
	 * @return the fromUser
	 */
	public String getFromUser() {
		return mFromUser;
	}
	/**
	 * @param fromUser the fromUser to set
	 */
	public void setFromUser(String fromUser) {
		this.mFromUser = fromUser;
	}
	/**
	 * @return the date
	 */
	public Date getDate() {
		return mDate;
	}
	/**
	 * @param date the date to set
	 */
	public void setDate(Date date) {
		this.mDate = date;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return mText;
	}
	/**
	 * @param text the text to set
	 */
	public void setText(String text) {
		this.mText = text;
	}

	
}
