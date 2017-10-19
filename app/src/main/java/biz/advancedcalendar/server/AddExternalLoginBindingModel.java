package biz.advancedcalendar.server;

public class AddExternalLoginBindingModel {
	public AddExternalLoginBindingModel(String email, String token) {
		Email = email;
		ExternalAccessToken = token;
	}

	public String Email;
	public String ExternalAccessToken;
}