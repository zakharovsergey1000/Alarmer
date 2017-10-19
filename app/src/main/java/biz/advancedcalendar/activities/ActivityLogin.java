package biz.advancedcalendar.activities;

import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.UserProfile;
import biz.advancedcalendar.server.LoginData;
import biz.advancedcalendar.server.LoginData.LoginType;
import biz.advancedcalendar.server.ServerProvider;
import biz.advancedcalendar.server.UserLoginResult;
import biz.advancedcalendar.sync.SyncService;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;

/** Activity which displays a login screen to the user, offering registration as well. */
@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class ActivityLogin extends Activity implements OnCheckedChangeListener,
		OnClickListener {
	/** Keep track of the login task to ensure we can cancel it if requested. */
	private UserLoginTask mAuthTask = null;
	// Values for email and password at the time of the login attempt.
	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private EditText mConfirmPasswordView;
	private Button mSigninButton;
	private CheckBox mCheckboxGoogleSignin;
	private RadioButton mRadioButtonSignin;
	private RadioButton mRadioButtonSignup;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	// google
	// private static final String SCOPE =
	// "oauth2:https://www.googleapis.com/auth/userinfo.profile";
	// private static final String SCOPE = "audience:server:client_id:X";
	private static final String SCOPE = "audience:server:client_id:371943187438-3898288fpejdpui9dndhdb46d7tu63vp.apps.googleusercontent.com";
	static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	static final int REQUEST_CODE_RECOVER_FROM_AUTH_ERROR = 1001;
	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1002;
	private String mEmail; // Received from newChooseAccountIntent(); passed to getToken()
	private RadioGroup mRadioGroupSigninSignupSelector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		// Set up the login form.
		mEmailView = (EditText) findViewById(R.id.email);
		UserProfile login = DataProvider.getUserProfile(null, getApplicationContext());
		if (login != null) {
			mEmailView.setText("");
			mEmailView.append(login.getEmail());
			// mEmailView.setEnabled(false);
			// RadioButton rb = (RadioButton) findViewById(R.id.radio_signup);
			// rb.setEnabled(false);
		}
		mPasswordView = (EditText) findViewById(R.id.password);
		mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);
		mRadioGroupSigninSignupSelector = (RadioGroup) findViewById(R.id.radiogroup_signin_signup_selector);
		mRadioButtonSignin = (RadioButton) findViewById(R.id.radio_signin);
		mRadioButtonSignin.setOnCheckedChangeListener(this);
		mRadioButtonSignup = (RadioButton) findViewById(R.id.radio_signup);
		mRadioButtonSignup.setOnCheckedChangeListener(this);
		mSigninButton = (Button) findViewById(R.id.signin_button);
		mSigninButton.setOnClickListener(this);
		mCheckboxGoogleSignin = (CheckBox) findViewById(R.id.checkbox_google_signin);
		mCheckboxGoogleSignin.setOnCheckedChangeListener(this);
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ActivityLogin.REQUEST_CODE_PICK_ACCOUNT) {
			// Receiving a result from the AccountPicker
			if (resultCode == Activity.RESULT_OK) {
				mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
				// With the account name acquired, go get the auth token
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				UserProfile userProfile = DataProvider
						.getUserProfile(null, getApplicationContext());
				LoginData loginData = new LoginData(userProfile == null ? null
						: userProfile.getEmail(), null, mEmail, ActivityLogin.SCOPE,
						false, LoginType.GOOGLE);
				mAuthTask.execute(loginData);
				// getUsername();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				// The account picker dialog closed without selecting an account.
				// Notify users that they must pick an account to proceed.
				Toast.makeText(this, R.string.pick_account, Toast.LENGTH_SHORT).show();
			}
		} else if (requestCode == ActivityLogin.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR
				&& resultCode == Activity.RESULT_OK) {
			// Receiving a result that follows a GoogleAuthException, try auth again
			// getUsername();
			mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			// With the account name acquired, go get the auth token
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			LoginData loginData = new LoginData(mEmailView.getText().toString(), null,
					mEmail, ActivityLogin.SCOPE, false, LoginType.GOOGLE);
			mAuthTask.execute(loginData);
		}
	}

	/** Attempts to retrieve the username. If the account is not yet known, invoke the
	 * picker. Once the account is known, start an instance of the AsyncTask to get the
	 * auth token and do work with it. */
	// private void getUsername() {
	// if (mEmail == null) {
	// pickUserAccount();
	// } else {
	// if (isDeviceOnline()) {
	// // new GetGoogleTokenTask(ActivityLogin.this, mEmail,
	// // ActivityLogin.SCOPE).execute();
	// } else {
	// Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
	// }
	// }
	// }
	private void pickUserAccount() {
		String[] accountTypes = new String[] {GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
		Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes,
				false, null, null, null, null);
		startActivityForResult(intent, ActivityLogin.REQUEST_CODE_PICK_ACCOUNT);
	}

	/** Checks whether the device currently has a network connection */
	// private boolean isDeviceOnline() {
	// ConnectivityManager connMgr = (ConnectivityManager)
	// getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	// if (networkInfo != null && networkInfo.isConnected()) {
	// return true;
	// }
	// return false;
	// }
	/** This method is a hook for background threads and async tasks that need to provide
	 * the user a response UI when an exception occurs. */
	public void handleException(final Exception e) {
		// Because this call comes from the AsyncTask, we must ensure that the following
		// code instead executes on the UI thread.
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (e instanceof GooglePlayServicesAvailabilityException) {
					// The Google Play services APK is old, disabled, or not present.
					// Show a dialog created by Google Play services that allows
					// the user to update the APK
					int statusCode = ((GooglePlayServicesAvailabilityException) e)
							.getConnectionStatusCode();
					Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode,
							ActivityLogin.this,
							ActivityLogin.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
					dialog.show();
				} else if (e instanceof UserRecoverableAuthException) {
					// Unable to authenticate, such as when the user has not yet granted
					// the app access to the account, but the user can fix this.
					// Forward the user to an activity in Google Play services.
					Intent intent = ((UserRecoverableAuthException) e).getIntent();
					startActivityForResult(intent,
							ActivityLogin.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/** Attempts to sign in or register the account specified by the login form. If there
	 * are form errors (invalid email, missing fields, etc.), the errors are presented and
	 * no actual login attempt is made.
	 *
	 * @param isRegistration */
	public void attemptLogin(boolean isRegistration) {
		if (mAuthTask != null) {
			return;
		}
		if (mCheckboxGoogleSignin.isChecked()) {
			pickUserAccount();
		} else {
			// Reset errors.
			mEmailView.setError(null);
			mPasswordView.setError(null);
			// Store values at the time of the login attempt.
			String mEmail = mEmailView.getText().toString();
			String mPassword = mPasswordView.getText().toString();
			String mConfirmPassword = mConfirmPasswordView.getText().toString();
			boolean cancel = false;
			View focusView = null;
			// Check for a valid password.
			int minimumPasswordLength = getResources().getInteger(
					R.integer.minimum_password_length);
			if (TextUtils.isEmpty(mPassword)) {
				mPasswordView.setError(getString(R.string.error_field_required));
				focusView = mPasswordView;
				cancel = true;
			} else if (mPassword.length() < minimumPasswordLength) {
				mPasswordView.setError(String
						.format(getString(R.string.error_invalid_password),
								minimumPasswordLength));
				focusView = mPasswordView;
				cancel = true;
			}
			// if it is registration then compare passwords
			else if (isRegistration) {
				if (!mPassword.equals(mConfirmPassword)) {
					mConfirmPasswordView
							.setError(getString(R.string.error_invalid_confirm_password));
					focusView = mConfirmPasswordView;
					cancel = true;
				}
			}
			// Check for a valid email address.
			if (TextUtils.isEmpty(mEmail)) {
				mEmailView.setError(getString(R.string.error_field_required));
				focusView = mEmailView;
				cancel = true;
			} else if (!mEmail.contains("@")) {
				mEmailView.setError(getString(R.string.error_invalid_email));
				focusView = mEmailView;
				cancel = true;
			}
			if (cancel) {
				// There was an error; don't attempt login and focus the first
				// form field with an error.
				focusView.requestFocus();
			} else {
				// Show a progress spinner, and kick off a background task to
				// perform the user login attempt.
				mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
				showProgress(true);
				mAuthTask = new UserLoginTask();
				LoginData loginData = new LoginData(mEmail, mPassword, null, null,
						isRegistration, LoginType.ADVANCED_CALENDAR);
				mAuthTask.execute(loginData);
			}
		}
	}

	/** Shows the progress UI and hides the login form. */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);
			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/** Represents an asynchronous login/registration task used to authenticate the user. */
	public class UserLoginTask extends AsyncTask<LoginData, Void, UserLoginResult> {
		@Override
		protected UserLoginResult doInBackground(LoginData... params) {
			return ServerProvider.tryLogin(getApplicationContext(), params[0]);
		}

		@Override
		protected void onPostExecute(final UserLoginResult userLoginResult) {
			mAuthTask = null;
			showProgress(false);
			String message = "";
			switch (userLoginResult.getType()) {
			case SIGNUP_ADVANCED_CALENDAR_SUCCESS:
				DataProvider.setSignedInUser(null,
						ActivityLogin.this, userLoginResult.getEmail(), userLoginResult.getBearerToken());
				mRadioGroupSigninSignupSelector.check(R.id.radio_signin);
			case SIGNIN_ADVANCED_CALENDAR_ERROR_EMAIL_IS_NOT_CONFIRMED:
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						ActivityLogin.this);
				// set title
				alertDialogBuilder.setTitle(R.string.signup_success);
				// set dialog message
				alertDialogBuilder
						.setMessage(
								getResources()
										.getString(
												R.string.signup_success_email_confirmation_required))
						.setCancelable(false)
						.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										// User clicked OK button
									}
								});
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
				return;
			case SIGNIN_ADVANCED_CALENDAR_SUCCESS:
			case SIGNIN_GOOGLE_SUCCESS:
				DataProvider.setSignedInUser(null,
						ActivityLogin.this, userLoginResult.getEmail(), userLoginResult.getBearerToken());
				// Intent intent = new Intent(ActivityLogin.this, ActivityMain.class);
				// startActivity(intent);
				if (getIntent()
						.getBooleanExtra(
								CommonConstants.INTENT_EXTRA_SYNCHRONIZATION_AFTER_SUCCESSFUL_SIGNIN_REQUIRED,
								false)) {
					Intent serviceIntent = new Intent(getApplicationContext(),
							SyncService.class);
					serviceIntent
							.putExtra(
									CommonConstants.INTENT_EXTRA_SYNC_SERVICE_REQUEST,
									CommonConstants.INTENT_EXTRA_VALUE_SYNC_SERVICE_REQUEST_FORCE_SYNC);
					startService(serviceIntent);
				}
				finish();
				return;
			case SIGNIN_ADVANCED_CALENDAR_ERROR_LOGIN_AND_PASSWORD_DO_NOT_MATCH:
				message = getResources().getString(
						R.string.error_the_password_do_not_match_to_the_login);
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
				break;
			case SIGNIN_ADVANCED_CALENDAR_ERROR_USER_NAME_DOES_NOT_EXIST:
				message = getResources()
						.getString(R.string.error_no_user_with_such_login);
				break;
			case SIGNUP_ADVANCED_CALENDAR_ERROR_USER_NAME_ALREADY_EXISTS:
				message = getResources().getString(
						R.string.error_the_login_is_already_taken_up);
				break;
			case INTERNAL_SERVER_ERROR:
				message = getResources()
						.getString(
								R.string.error_the_server_returned_an_error_INTERNAL_SERVER_ERROR);
				break;
			case NETWORK_CONNECTION_FAILED:
				message = getResources().getString(
						R.string.error_the_network_connection_failed);
				break;
			case SIGNIN_GOOGLE_USER_RECOVERABLE_AUTH_EXCEPTION:
				// GooglePlayServices.apk is either old, disabled, or not present
				// so we need to show the user some UI in the activity to recover.
				handleException(userLoginResult.getException());
				return;
			case SIGNIN_GOOGLE_GOOGLE_AUTH_EXCEPTION:
				// Some other type of unrecoverable exception has occurred.
				// Report and log the error as appropriate for your app.
				// ...
				// onError("Unrecoverable error " + userLoginResult.getErrorMessage(),
				// userLoginResult.getException());
			case SIGNIN_GOOGLE_ERROR_UNKNOWN:
				// The fetchToken() method handles Google-specific exceptions,
				// so this indicates something went wrong at a higher level.
				// TIP: Check for network connectivity before starting the AsyncTask.
				// ...
				// onError("An error is occurred, please try again. " + e.getMessage(),
				// e);
				// } catch (JSONException e) {
				// onError("Bad response: " + e.getMessage(), e);
				message = getResources().getString(R.string.error_an_error_occurred)
						+ " "
						+ (userLoginResult.getException() == null ? "" : userLoginResult
								.getException().getLocalizedMessage());
				break;
			case SIGNUP_ADVANCED_CALENDAR_ERROR_UNKNOWN:
			case SIGNIN_ADVANCED_CALENDAR_ERROR_UNKNOWN:
			case UKNOWN_ERROR:
				if (userLoginResult.getErrorMessage() != null) {
					message = getResources().getString(R.string.error_an_error_occurred)
							+ " " + userLoginResult.getErrorMessage();
					if (userLoginResult.getException() != null) {
						message += "\n"
								+ userLoginResult.getException().getLocalizedMessage();
					}
				} else {
					message = getResources().getString(
							R.string.error_an_unknown_error_occurred);
					if (userLoginResult.getException() != null) {
						message += "\n"
								+ userLoginResult.getException().getLocalizedMessage();
					}
				}
				break;
			default:
				break;
			}
			// 1. Instantiate an AlertDialog.Builder with its constructor
			AlertDialog.Builder builder = new AlertDialog.Builder(ActivityLogin.this);
			// 2. Chain together various setter methods to set the dialog
			// characteristics
			builder.setMessage(message);
			// Add the buttons
			builder.setPositiveButton(R.string.alert_dialog_ok,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							// User clicked OK button
						}
					});
			// 3. Get the AlertDialog from create()
			AlertDialog dialog = builder.create();
			dialog.show();
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.signin_button:
			RadioButton rb = (RadioButton) findViewById(R.id.radio_signup);
			attemptLogin(rb.isChecked());
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.radio_signin:
			if (isChecked) {
				mEmailView.setVisibility(View.VISIBLE);
				mPasswordView.setVisibility(View.VISIBLE);
				mConfirmPasswordView.setVisibility(View.GONE);
				mSigninButton.setText(getString(R.string.action_signin));
			}
			break;
		case R.id.radio_signup:
			if (isChecked) {
				mEmailView.setVisibility(View.VISIBLE);
				mPasswordView.setVisibility(View.VISIBLE);
				mConfirmPasswordView.setVisibility(View.VISIBLE);
				mSigninButton.setText(getString(R.string.action_signup));
			}
			break;
		case R.id.checkbox_google_signin:
			LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearlayout_radiogroup_email_password_confirm_password);
			if (isChecked) {
				linearLayout.setVisibility(View.GONE);
			} else {
				linearLayout.setVisibility(View.VISIBLE);
			}
		}
	}
}
