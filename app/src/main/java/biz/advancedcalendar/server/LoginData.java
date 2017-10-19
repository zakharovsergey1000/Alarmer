package biz.advancedcalendar.server;

public class LoginData {
	private String advancedCalendarEmail, password, googleEmail, scope;
	private boolean isRegistration;
	private LoginType loginType;

	/** @param advancedCalendarEmail
	 * @param password
	 * @param isRegistration */
	public LoginData(String advancedCalendarEmail, String password, String googleEmail,
			String scope, boolean isRegistration, LoginType loginType) {
		super();
		this.advancedCalendarEmail = advancedCalendarEmail;
		this.password = password;
		this.googleEmail = googleEmail;
		this.scope = scope;
		this.isRegistration = isRegistration;
		this.loginType = loginType;
	}

	/** @return the login */
	public String getAdvancedCalendarEmail() {
		return advancedCalendarEmail;
	}

	public String getGoogleEmail() {
		return googleEmail;
	}

	/** @return the password */
	public String getPassword() {
		return password;
	}

	public String getScope() {
		return scope;
	}

	/** @return the isRegistration */
	public boolean isRegistration() {
		return isRegistration;
	}

	public LoginType getLoginType() {
		return loginType;
	}

	public static enum LoginType {
		ADVANCED_CALENDAR, GOOGLE
	}
}
