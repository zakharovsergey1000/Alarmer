package biz.advancedcalendar.server;

public class UserLoginResult {
	public enum Type {
		SIGNIN_ADVANCED_CALENDAR_SUCCESS,
		//
		SIGNIN_ADVANCED_CALENDAR_ERROR_USER_NAME_DOES_NOT_EXIST,
		//
		SIGNIN_ADVANCED_CALENDAR_ERROR_EMAIL_IS_NOT_CONFIRMED,
		//
		SIGNIN_ADVANCED_CALENDAR_ERROR_LOGIN_AND_PASSWORD_DO_NOT_MATCH,
		//
		SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN,
		//
		SIGNUP_ADVANCED_CALENDAR_SUCCESS,
		//
		SIGNUP_ADVANCED_CALENDAR_ERROR_USER_NAME_ALREADY_EXISTS,
		//
		SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN,
		//
		SIGNIN_GOOGLE_SUCCESS,
		//
		SIGNIN_GOOGLE_USER_RECOVERABLE_AUTH_EXCEPTION,
		//
		SIGNIN_GOOGLE_GOOGLE_AUTH_EXCEPTION,
		//
		SIGNIN_GOOGLE_ERROR_UNKNOWN,
		//
		INTERNAL_SERVER_ERROR,
		//
		NETWORK_CONNECTION_FAILED,
		//
		UKNOWN_ERROR
	}

	private Type type;
	private String email;
	private String bearerToken;
	private String errorMessage;
	private Exception exception;

	public UserLoginResult(Type type, String email, String bearerToken,
			String errorMessage, Exception exception) {
		super();
		this.type = type;
		this.email = email;
		this.bearerToken = bearerToken;
		this.errorMessage = errorMessage;
		this.exception = exception;
	}

	public Type getType() {
		return type;
	}

	public String getEmail() {
		return email;
	}

	public String getBearerToken() {
		return bearerToken;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Exception getException() {
		return exception;
	}
}
