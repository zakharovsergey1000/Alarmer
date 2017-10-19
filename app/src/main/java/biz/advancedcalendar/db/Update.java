package biz.advancedcalendar.db;

public class Update {
	private String mUpdateText;

	/** @param updateText */
	public Update(String updateText) {
		super();
		mUpdateText = updateText;
	}

	/** @return the updateText */
	public String getUpdateText() {
		return mUpdateText;
	}

	/** @param updateText
	 *            the updateText to set */
	public void setUpdateText(String updateText) {
		mUpdateText = updateText;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString() */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Update [UpdateText=").append(mUpdateText).append("]");
		return builder.toString();
	}
}
