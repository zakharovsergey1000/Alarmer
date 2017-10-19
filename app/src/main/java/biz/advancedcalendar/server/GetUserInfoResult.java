package biz.advancedcalendar.server;

public class GetUserInfoResult {
	private UserInfoViewModel userInfoViewModel;
	private String errorMessage;
	private Exception exception;

	public GetUserInfoResult(UserInfoViewModel userInfoViewModel, String errorMessage,
			Exception exception) {
		super();
		this.userInfoViewModel = userInfoViewModel;
		this.errorMessage = errorMessage;
		this.exception = exception;
	}

	public UserInfoViewModel getUserInfoViewModel() {
		return userInfoViewModel;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Exception getException() {
		return exception;
	}
}
