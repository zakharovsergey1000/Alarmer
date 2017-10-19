package biz.advancedcalendar.server;

import android.content.Context;
import android.preference.PreferenceManager;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.server.LoginData.LoginType;
import biz.advancedcalendar.server.serialisers.CreateUserResultSerializer;
import biz.advancedcalendar.wsdl.sync.CreateUserResponse;
import biz.advancedcalendar.wsdl.sync.Enums.CreateUserResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerProvider {
	public static UserLoginResult tryLogin(Context context, LoginData loginData) {
		if (loginData.isRegistration()) {
			try {
				RegisterBindingModel model = new RegisterBindingModel(
						loginData.getAdvancedCalendarEmail(), loginData.getPassword(),
						loginData.getPassword());
				JSONHttpClient httpClient = new JSONHttpClient();
				HttpURLConnection httpURLConnection = httpClient
						.getHttpURLConnectionForPostMethod(ServiceUrl.REGISTER, null,
								model);
				int result = -1;
				Long dateTime = null;
				try {
					result = httpURLConnection.getResponseCode();
					dateTime = null;
					// CommonConstants.RFC1123_DATE_TIME_FORMATTER.parseDateTime(
					// httpURLConnection.getHeaderField("Date")).getMillis();
					PreferenceManager
							.getDefaultSharedPreferences(context)
							.edit()
							.putLong(CommonConstants.TIME_SKEW,
									Calendar.getInstance().getTimeInMillis() - dateTime)
							.commit();
				} catch (IOException e) {
					try {
						result = httpURLConnection.getResponseCode();
						dateTime = null;
						// CommonConstants.RFC1123_DATE_TIME_FORMATTER
						// .parseDateTime(httpURLConnection.getHeaderField("Date"))
						// .getMillis();
						PreferenceManager
								.getDefaultSharedPreferences(context)
								.edit()
								.putLong(
										CommonConstants.TIME_SKEW,
										Calendar.getInstance().getTimeInMillis()
												- dateTime).commit();
						return new UserLoginResult(
								UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail(), null, e
										.getLocalizedMessage(), e);
					} catch (IOException e1) {
						return new UserLoginResult(
								UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail(), null, e1
										.getLocalizedMessage(), e1);
					}
				}
				switch (result) {
				case 200:
					break;
				case 500:
					return new UserLoginResult(
							UserLoginResult.Type.INTERNAL_SERVER_ERROR,
							loginData.getAdvancedCalendarEmail(), null, null, null);
				case 400:
				default:
					return new UserLoginResult(
							UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN,
							loginData.getAdvancedCalendarEmail(), null, null, null);
				}
				CreateUserResponse createUserResponse = null;
				InputStream inputStream = httpURLConnection.getInputStream();
				String resultString = JSONHttpClient.convertStreamToString(inputStream);
				inputStream.close();
				GsonBuilder gsonBuilder = new GsonBuilder();
				gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer())
						.registerTypeAdapter(CreateUserResult.class,
								new CreateUserResultSerializer());
				createUserResponse = gsonBuilder.create().fromJson(resultString,
						CreateUserResponse.class);
				switch (createUserResponse.Result) {
				case SUCCESS:
					return new UserLoginResult(
							UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_SUCCESS,
							loginData.getAdvancedCalendarEmail(), null, null, null);
				case THE_USER_WITH_REQUESTED_USERNAME_ALREADY_EXISTS:
					return new UserLoginResult(
							UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_ERROR_USER_NAME_ALREADY_EXISTS,
							loginData.getAdvancedCalendarEmail(), null, null, null);
				case ERROR_UNKNOWN:
				default:
					return new UserLoginResult(
							UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN,
							loginData.getAdvancedCalendarEmail(), null, null, null);
				}
			} catch (Exception e) {
				return new UserLoginResult(
						UserLoginResult.Type.SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN,
						loginData.getAdvancedCalendarEmail(), null,
						e.getLocalizedMessage(), e);
			}
		} else if (loginData.getLoginType().equals(LoginType.ADVANCED_CALENDAR)) {
			try {
				JSONHttpClient httpClient = new JSONHttpClient();
				String object = String.format(
						"grant_type=password&username=%s&password=%s",
						URLEncoder.encode(loginData.getAdvancedCalendarEmail(), "UTF-8"),
						URLEncoder.encode(loginData.getPassword(), "UTF-8"));
				HttpURLConnection httpURLConnection = httpClient
						.getHttpURLConnectionForPostStringAsFormUrlEncodedContent(
								ServiceUrl.TOKEN, object);
				int result = -1;
				Long dateTime = null;
				try {
					result = httpURLConnection.getResponseCode();
					dateTime = null;
					// CommonConstants.RFC1123_DATE_TIME_FORMATTER.parseDateTime(
					// httpURLConnection.getHeaderField("Date")).getMillis();
					PreferenceManager
							.getDefaultSharedPreferences(context)
							.edit()
							.putLong(CommonConstants.TIME_SKEW,
									Calendar.getInstance().getTimeInMillis() - dateTime)
							.commit();
				} catch (IOException e) {
					try {
						result = httpURLConnection.getResponseCode();
						dateTime = null;
						// CommonConstants.RFC1123_DATE_TIME_FORMATTER
						// .parseDateTime(httpURLConnection.getHeaderField("Date"))
						// .getMillis();
						PreferenceManager
								.getDefaultSharedPreferences(context)
								.edit()
								.putLong(
										CommonConstants.TIME_SKEW,
										Calendar.getInstance().getTimeInMillis()
												- dateTime).commit();
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail(), null, e
										.getLocalizedMessage(), e);
					} catch (IOException e1) {
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail(), null, e1
										.getLocalizedMessage(), e1);
					}
				}
				if (result >= 400) {
					ErrorResponseModel errorResponseModel = null;
					InputStream inputStream = httpURLConnection.getErrorStream();
					String resultString = JSONHttpClient
							.convertStreamToString(inputStream);
					inputStream.close();
					GsonBuilder gsonBuilder = new GsonBuilder();
					errorResponseModel = gsonBuilder.create().fromJson(resultString,
							ErrorResponseModel.class);
					if (errorResponseModel.error.equals("email_is_not_confirmed")) {
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_ERROR_EMAIL_IS_NOT_CONFIRMED,
								loginData.getAdvancedCalendarEmail(), null,
								errorResponseModel.error_description, null);
					} else {
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail(), null,
								errorResponseModel.error_description, null);
					}
				} else {
					switch (result) {
					case 200:
						break;
					default:
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail(), null, null, null);
					}
				}
				InputStream inputStream = httpURLConnection.getInputStream();
				String resultString = JSONHttpClient.convertStreamToString(inputStream);
				inputStream.close();
				JSONObject json = (JSONObject) new JSONParser().parse(resultString);
				// TokenModel tokenModel = new TokenModel();
				// tokenModel.AccessToken = (String) json.get("access_token");
				// tokenModel.ExpiresAt = (String) json.get(".expires");
				// tokenModel.ExpiresIn = ((Long)
				// json.get("expires_in")).intValue();
				// tokenModel.IssuedAt = (String) json.get(".issued");
				// tokenModel.TokenType = (String) json.get("token_type");
				// tokenModel.Username = (String) json.get("userName");
				return new UserLoginResult(
						UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_SUCCESS,
						loginData.getAdvancedCalendarEmail(),
						(String) json.get("access_token"), null, null);
			} catch (Exception e) {
				return new UserLoginResult(
						UserLoginResult.Type.SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN,
						loginData.getAdvancedCalendarEmail(), null,
						e.getLocalizedMessage(), e);
			}
		} else if (loginData.getLoginType().equals(LoginType.GOOGLE)) {
			/** Gets an authentication token from Google and handles any
			 * GoogleAuthException that may occur. */
			try {
				String googleIdToken = GoogleAuthUtil.getToken(context,
						loginData.getGoogleEmail(), loginData.getScope());
				// String googleIdToken = GoogleAuthUtil.getToken(context, new Account(
				// loginData.getGoogleEmail(), GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE),
				// loginData.getScope());
				if (googleIdToken != null) {
					try {
						JSONHttpClient httpClient = new JSONHttpClient();
						HttpURLConnection httpURLConnection = httpClient
								.getHttpURLConnectionForPostMethod(
										ServiceUrl.GOOGLE_LOGIN,
										null,
										new AddExternalLoginBindingModel(loginData
												.getAdvancedCalendarEmail(),
												googleIdToken));
						int result = -1;
						Long dateTime = null;
						try {
							result = httpURLConnection.getResponseCode();
							dateTime = null;
							// CommonConstants.RFC1123_DATE_TIME_FORMATTER
							// .parseDateTime(
							// httpURLConnection.getHeaderField("Date"))
							// .getMillis();
							PreferenceManager
									.getDefaultSharedPreferences(context)
									.edit()
									.putLong(
											CommonConstants.TIME_SKEW,
											Calendar.getInstance().getTimeInMillis()
													- dateTime).commit();
						} catch (IOException e) {
							try {
								result = httpURLConnection.getResponseCode();
								dateTime = null;
								// CommonConstants.RFC1123_DATE_TIME_FORMATTER
								// .parseDateTime(
								// httpURLConnection.getHeaderField("Date"))
								// .getMillis();
								PreferenceManager
										.getDefaultSharedPreferences(context)
										.edit()
										.putLong(
												CommonConstants.TIME_SKEW,
												Calendar.getInstance().getTimeInMillis()
														- dateTime).commit();
								return new UserLoginResult(
										UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
										loginData.getAdvancedCalendarEmail() != null ? loginData
												.getAdvancedCalendarEmail() : loginData
												.getGoogleEmail(), null,
										e.getLocalizedMessage(), e);
							} catch (IOException e1) {
								return new UserLoginResult(
										UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
										loginData.getAdvancedCalendarEmail() != null ? loginData
												.getAdvancedCalendarEmail() : loginData
												.getGoogleEmail(), null,
										e1.getLocalizedMessage(), e1);
							}
						}
						if (result >= 400) {
							ErrorResponseModel errorResponseModel = null;
							InputStream inputStream = httpURLConnection.getErrorStream();
							String resultString = JSONHttpClient
									.convertStreamToString(inputStream);
							inputStream.close();
							GsonBuilder gsonBuilder = new GsonBuilder();
							errorResponseModel = gsonBuilder.create().fromJson(
									resultString, ErrorResponseModel.class);
							return new UserLoginResult(
									UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
									loginData.getAdvancedCalendarEmail() != null ? loginData
											.getAdvancedCalendarEmail() : loginData
											.getGoogleEmail(), null,
									errorResponseModel.error_description, null);
						} else {
							switch (result) {
							case 200:
								break;
							default:
								return new UserLoginResult(
										UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
										loginData.getAdvancedCalendarEmail() != null ? loginData
												.getAdvancedCalendarEmail() : loginData
												.getGoogleEmail(), null, null, null);
							}
						}
						InputStream inputStream = httpURLConnection.getInputStream();
						String resultString = JSONHttpClient
								.convertStreamToString(inputStream);
						inputStream.close();
						JSONObject json = (JSONObject) new JSONParser()
								.parse(resultString);
						// TokenModel tokenModel = new TokenModel();
						// tokenModel.AccessToken = (String) json.get("access_token");
						// tokenModel.ExpiresAt = (String) json.get(".expires");
						// tokenModel.ExpiresIn = ((Long)
						// json.get("expires_in")).intValue();
						// tokenModel.IssuedAt = (String) json.get(".issued");
						// tokenModel.TokenType = (String) json.get("token_type");
						// tokenModel.Username = (String) json.get("userName");
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_GOOGLE_SUCCESS,
								loginData.getAdvancedCalendarEmail() != null ? loginData
										.getAdvancedCalendarEmail() : loginData
										.getGoogleEmail(),
								(String) json.get("access_token"), null, null);
					} catch (Exception e) {
						return new UserLoginResult(
								UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
								loginData.getAdvancedCalendarEmail() != null ? loginData
										.getAdvancedCalendarEmail() : loginData
										.getGoogleEmail(), null, e.getLocalizedMessage(),
								e);
					}
				}
			} catch (UserRecoverableAuthException userRecoverableException) {
				// GooglePlayServices.apk is either old, disabled, or not present
				// so we need to show the user some UI in the activity to recover.
				// activity.handleException(userRecoverableException);
				return new UserLoginResult(
						UserLoginResult.Type.SIGNIN_GOOGLE_USER_RECOVERABLE_AUTH_EXCEPTION,
						loginData.getAdvancedCalendarEmail() != null ? loginData
								.getAdvancedCalendarEmail() : loginData.getGoogleEmail(),
						null, userRecoverableException.getLocalizedMessage(),
						userRecoverableException);
			} catch (GoogleAuthException fatalException) {
				// Some other type of unrecoverable exception has occurred.
				// Report and log the error as appropriate for your app.
				// ...
				// onError("Unrecoverable error " + fatalException.getMessage(),
				// fatalException);
				return new UserLoginResult(
						UserLoginResult.Type.SIGNIN_GOOGLE_GOOGLE_AUTH_EXCEPTION,
						loginData.getAdvancedCalendarEmail() != null ? loginData
								.getAdvancedCalendarEmail() : loginData.getGoogleEmail(),
						null, "GoogleAuthException", fatalException);
			} catch (IOException e) {
				// this indicates something went wrong at a higher level.
				// TIP: Check for network connectivity before starting the AsyncTask.
				// ...
				// onError("An error is occurred, please try again. " + e.getMessage(),
				// e);
				// } catch (JSONException e) {
				// onError("Bad response: " + e.getMessage(), e);
				return new UserLoginResult(
						UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
						loginData.getAdvancedCalendarEmail() != null ? loginData
								.getAdvancedCalendarEmail() : loginData.getGoogleEmail(),
						null, e.getLocalizedMessage(), e);
			} catch (Exception e) {
				return new UserLoginResult(
						UserLoginResult.Type.SIGNIN_GOOGLE_ERROR_UNKNOWN,
						loginData.getAdvancedCalendarEmail() != null ? loginData
								.getAdvancedCalendarEmail() : loginData.getGoogleEmail(),
						null, e.getLocalizedMessage(), e);
			}
		}
		throw new IllegalArgumentException();
	}

	public static GetUserInfoResult GetUserInfo(Context context, String bearerToken) {
		try {
			JSONHttpClient httpClient = new JSONHttpClient();
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", String.format("Bearer %s", bearerToken));
			HttpURLConnection httpURLConnection = httpClient
					.getHttpURLConnectionForPostMethod(ServiceUrl.GET_USER_INFO, headers,
							null);
			int result = -1;
			Long dateTime = null;
			try {
				result = httpURLConnection.getResponseCode();
				dateTime = null;
				// CommonConstants.RFC1123_DATE_TIME_FORMATTER.parseDateTime(
				// httpURLConnection.getHeaderField("Date")).getMillis();
				PreferenceManager
						.getDefaultSharedPreferences(context)
						.edit()
						.putLong(CommonConstants.TIME_SKEW,
								Calendar.getInstance().getTimeInMillis() - dateTime)
						.commit();
			} catch (IOException e) {
				try {
					result = httpURLConnection.getResponseCode();
					dateTime = null;
					// CommonConstants.RFC1123_DATE_TIME_FORMATTER.parseDateTime(
					// httpURLConnection.getHeaderField("Date")).getMillis();
					PreferenceManager
							.getDefaultSharedPreferences(context)
							.edit()
							.putLong(CommonConstants.TIME_SKEW,
									Calendar.getInstance().getTimeInMillis() - dateTime)
							.commit();
					return new GetUserInfoResult(null, e.getLocalizedMessage(), e);
				} catch (IOException e1) {
					return new GetUserInfoResult(null, e1.getLocalizedMessage(), e1);
				}
			}
			switch (result) {
			case 200:
				break;
			case 500:
			case 400:
			default:
				return new GetUserInfoResult(null, "Response code: " + result, null);
			}
			UserInfoViewModel userInfoViewModel = null;
			InputStream inputStream;
			inputStream = httpURLConnection.getInputStream();
			String resultString = JSONHttpClient.convertStreamToString(inputStream);
			inputStream.close();
			GsonBuilder gsonBuilder = new GsonBuilder();
			userInfoViewModel = gsonBuilder.create().fromJson(resultString,
					UserInfoViewModel.class);
			return new GetUserInfoResult(userInfoViewModel, null, null);
		} catch (Exception e) {
			return new GetUserInfoResult(null, e.getLocalizedMessage(), e);
		}
	}
}
