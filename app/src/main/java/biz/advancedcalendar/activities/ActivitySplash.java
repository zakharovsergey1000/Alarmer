package biz.advancedcalendar.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.greendao.DaoMaster;
import biz.advancedcalendar.greendao.DaoMaster.DevOpenHelper;
import java.io.File;

public class ActivitySplash extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		// DataProvider.setLogged(this, false);
		new PrepareDatabaseTask(getApplicationContext()).execute((Void) null);
	}

	private class PrepareDatabaseTask extends AsyncTask<Void, Integer, SQLiteException> {
		Context context;
		SQLiteException sqliteException;

		/** @param context
		 *            A Context. This should be getApplicationContext(), not Activity. */
		public PrepareDatabaseTask(Context context) {
			super();
			this.context = context;
			sqliteException = null;
		}

		@Override
		protected SQLiteException doInBackground(Void... params) {
			File folder = new File(Environment.getExternalStorageDirectory()
					+ "/media/audio/ringtones");
			// boolean success = true;
			if (!folder.exists()) {
				// success = folder.mkdirs();
			}
			folder = new File(Environment.getExternalStorageDirectory()
					+ "/media/audio/notifications");
			// success = true;
			if (!folder.exists()) {
				// success = folder.mkdirs();
			}
			folder = new File(Environment.getExternalStorageDirectory()
					+ "/media/audio/alarms");
			// success = true;
			if (!folder.exists()) {
				// success = folder.mkdirs();
			}
			java.io.File database = getApplicationContext().getDatabasePath(
					"AdvancedCalendar.db");
			if (!database.exists()) {
				// Database does not exist so copy it from assets here
				Log.i("AdvancedCalendar", "AdvancedCalendar.db Not Found");
				// context.deleteDatabase("AdvancedCalendar.db");
				DevOpenHelper helper = new DaoMaster.DevOpenHelper(context,
						"AdvancedCalendar.db", null);
				try {
					SQLiteDatabase db = helper.getWritableDatabase();
					db.close();
					// createSampleRecords();
					// prepare some default settings in SharedPreferences
					PreferenceManager
							.getDefaultSharedPreferences(context)
							.edit()
							.putString(
									getResources().getString(
											R.string.preference_key_first_day_of_week),
									"0").commit();
				} catch (SQLiteException e) {
					sqliteException = e;
				}
			} else {
				Log.i("AdvancedCalendar", "AdvancedCalendar.db Found");
			}
			return sqliteException;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
		}

		@Override
		protected void onPostExecute(SQLiteException result) {
			if (result == null) {
				login();
			} else {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				// set title
				alertDialogBuilder.setTitle(R.string.database_creation_failed);
				// set dialog message
				alertDialogBuilder
						.setMessage(
								getResources().getString(
										R.string.database_creation_failed)
										+ "\n"
										+ getResources().getString(R.string.reason)
										+ result.getMessage())
						.setCancelable(false)
						.setPositiveButton(R.string.close_application,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										// finish();
										finish();
									}
								});
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
				// show it
				alertDialog.show();
			}
		}

		private void login() {
			if (true /* || DataProvider.isSignedIn(ActivitySplash.this) */) {
				Intent intent = new Intent(ActivitySplash.this, ActivityMain.class);
				startActivity(intent);
			}
			// else {
			// Intent intent = new Intent(ActivitySplash.this, ActivityLogin.class);
			// intent.putExtra(
			// CommonConstants.INTENT_EXTRA_SYNCHRONIZATION_AFTER_SUCCESSFUL_SIGNIN_REQUIRED,
			// true);
			// startActivity(intent);
			// }
		}
	}
}
