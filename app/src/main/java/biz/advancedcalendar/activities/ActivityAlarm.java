package biz.advancedcalendar.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.fragments.RetainedFragmentForActivityAlarm;
import biz.advancedcalendar.greendao.Reminder;
import biz.advancedcalendar.greendao.ScheduledReminder;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.services.AlarmService;
import biz.advancedcalendar.services.NotificationService;
import biz.advancedcalendar.utils.Helper;
import com.android.supportdatetimepicker.time.RadialPickerLayout;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.OnMultipleTimeSetListener;
import com.android.supportdatetimepicker.time.TimePickerDialogMultiple.TimeAttribute;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ActivityAlarm extends AppCompatActivity implements OnMultipleTimeSetListener {
	private class VolumeIncreaserRunnable implements Runnable {
		float mVolume;
		private final static float mStep = 0.01f;
		long mTimeSpan;
		float mInitialVolume;
		float mFinalVolume;
		float mFinalInitialVolumeDelta;
		private boolean mFirstRun;
		private long mFirstRunDateTime;
		private boolean mCancelled;

		public VolumeIncreaserRunnable(long timeSpan, float initialVolume,
				float finalVolume) {
			if (initialVolume < 0) {
				initialVolume = 0;
			} else if (initialVolume > 1.0f) {
				initialVolume = 1.0f;
			}
			if (finalVolume < 0) {
				finalVolume = 0;
			} else if (finalVolume > 1.0f) {
				finalVolume = 1.0f;
			}
			if (finalVolume < initialVolume) {
				finalVolume = initialVolume;
			}
			mInitialVolume = initialVolume;
			mFinalVolume = finalVolume;
			mFinalInitialVolumeDelta = finalVolume - initialVolume;
			mVolume = initialVolume;
			mTimeSpan = timeSpan;
			mFirstRun = true;
			mCancelled = false;
		}

		@Override
		public void run() {
			long currentTimeMillis = System.currentTimeMillis();
			if (mCancelled) {
				return;
			}
			if (mFirstRun) {
				mFirstRunDateTime = currentTimeMillis;
				mFirstRun = false;
				if (mTimeSpan == 0) {
					if (mDataFragment.mediaPlayer != null) {
						mDataFragment.mediaPlayer.setVolume(mFinalVolume, mFinalVolume);
						if (ActivityAlarm.DEBUG) {
							Log.d(ActivityAlarm.ActivityAlarmDebug, "mFinalVolume "
									+ mFinalVolume);
						}
					}
					return;
				}
				if (mDataFragment.mediaPlayer != null) {
					mDataFragment.mediaPlayer.setVolume(mInitialVolume, mInitialVolume);
					if (ActivityAlarm.DEBUG) {
						Log.d(ActivityAlarm.ActivityAlarmDebug, "mInitialVolume "
								+ mInitialVolume);
					}
				}
				mTimerHandler.post(mRunnable);
				return;
			}
			float currentPlayingTime = currentTimeMillis - mFirstRunDateTime;
			float nextFraction = currentPlayingTime / mTimeSpan
					+ VolumeIncreaserRunnable.mStep;
			if (nextFraction >= 1.0f - VolumeIncreaserRunnable.mStep) {
				if (mDataFragment.mediaPlayer != null) {
					mDataFragment.mediaPlayer.setVolume(mFinalVolume, mFinalVolume);
					if (ActivityAlarm.DEBUG) {
						Log.d(ActivityAlarm.ActivityAlarmDebug, "mFinalVolume "
								+ mFinalVolume);
					}
				}
				return;
			}
			mVolume = mInitialVolume + mFinalInitialVolumeDelta * nextFraction;
			if (mDataFragment.mediaPlayer != null) {
				mDataFragment.mediaPlayer.setVolume(mVolume, mVolume);
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, "mVolume " + mVolume);
				}
			}
			mTimerHandler.postDelayed(mRunnable,
					(long) (mTimeSpan * nextFraction - currentPlayingTime));
		}

		private void cancel() {
			mCancelled = true;
			if (ActivityAlarm.DEBUG) {
				Log.d(ActivityAlarm.ActivityAlarmDebug, "mCancelled " + mCancelled);
			}
		}
	}

	public static final boolean DEBUG = true;
	private static final boolean DEBUG2 = false;
	public static final String ActivityAlarmDebug = "ActivityAlarmDebug";
	private RetainedFragmentForActivityAlarm mDataFragment;
	private ActivityAlarmArrayAdapter mAdapter;
	private DateFormat mDateTimeFormat;
	private String mTimePickerDialogKey = "biz.advancedcalendar.activities.ActivityAlarm.TimePickerDialog";
	private AutomaticSnoozer mAutomaticSnoozer;
	private boolean isUserLeaving;
	private boolean isBackPressed;
	private TimePickerDialogMultiple mTimePickerDialogMultiple;
	private Handler mTimerHandler;
	private VolumeIncreaserRunnable mRunnable;
	private Toolbar mToolbar;

	public interface AutomaticSnoozer {
		public void setListener(AutomaticSnoozeListener listener);

		public Long[] pullSnoozedAlarms();

		public void postDelayed(long id, long delayMillis);

		public void cancel(Long id);
	}

	public interface AutomaticSnoozeListener {
		public void onAutomaticSnooze(long id);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, "onCreate(): " + this);
		}
		super.onCreate(savedInstanceState);
		mAutomaticSnoozer = (AutomaticSnoozer) getApplication();
		if (savedInstanceState != null) {
			mTimePickerDialogMultiple = (TimePickerDialogMultiple) getSupportFragmentManager()
					.findFragmentByTag(mTimePickerDialogKey);
			if (mTimePickerDialogMultiple != null) {
				mTimePickerDialogMultiple.setOnTimeSetListener(this);
			}
		}
		// Log.d(CommonConstants.DEBUG_TAG, "ActivityAlarm onCreate()");
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.screenBrightness = 1;
		Window w = getWindow(); // in Activity's onCreate() for instance
		w.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setAttributes(params);
		//
		setContentView(R.layout.activity_alarm);
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
		mDateTimeFormat = DateFormat.getDateTimeInstance();
		// find the retained fragment on activity restarts
		FragmentManager fm = getSupportFragmentManager();
		mDataFragment = (RetainedFragmentForActivityAlarm) fm
				.findFragmentByTag(CommonConstants.DATA_FRAGMENT);
		// create the fragment and data the first time
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, "mDataFragment: " + mDataFragment);
		}
		ActivityAlarm context = ActivityAlarm.this;
		if (mDataFragment == null) {
			List<ScheduledReminder> alarmedAlarms = DataProvider.getAlarmedAlarms(null,
					context);
			if (ActivityAlarm.DEBUG) {
				Log.d(ActivityAlarm.ActivityAlarmDebug, "alarmedAlarms: " + alarmedAlarms);
			}
			if (alarmedAlarms.size() == 0) {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, "isTaskRoot(): "
							+ isTaskRoot());
				}
				// wrong code, causes null pointer exception
				// if (isTaskRoot()) {
				// ActivityExit.exitApplicationAndRemoveFromRecent(context);
				// } else {
				// finish();
				// }
				finish();
				if (isTaskRoot()) {
					ActivityExit.exitApplicationAndRemoveFromRecent(ActivityAlarm.this);
				}
				return;
			}
			// add the fragment
			mDataFragment = new RetainedFragmentForActivityAlarm();
			fm.beginTransaction().add(mDataFragment, CommonConstants.DATA_FRAGMENT)
					.commit();
			mDataFragment.scheduledReminders = alarmedAlarms;
		}
		mAdapter = new ActivityAlarmArrayAdapter(context,
				R.layout.activity_alarm_list_item, mDataFragment.scheduledReminders);
		ListView listView = (ListView) findViewById(R.id.alarming_reminders_list_view);
		listView.setAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		isUserLeaving = false;
		isBackPressed = false;
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, String.format("onResume(): ", this));
		}
		relaunchMediaPlayerAndVibratorIfNeeded();
		final ActivityAlarm context = ActivityAlarm.this;
		mAutomaticSnoozer.setListener(new AutomaticSnoozeListener() {
			@Override
			public void onAutomaticSnooze(long id) {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug,
							String.format("onAutomaticSnooze(long id: %s)", "" + id));
				}
				if (mTimePickerDialogMultiple != null
						&& id == ((ScheduledReminder) mTimePickerDialogMultiple
								.getBundle().getParcelable("scheduledReminder")).getId()) {
					mTimePickerDialogMultiple.dismiss();
				}
				mDataFragment.currentAlarmsSoundIsMuted = false;
				releaseMediaPlayerAndCancelVibrator();
				for (int i = 0; i < mDataFragment.scheduledReminders.size(); i++) {
					if (mDataFragment.scheduledReminders.get(i).getId() == id) {
						if (ActivityAlarm.DEBUG) {
							Log.d(ActivityAlarm.ActivityAlarmDebug,
									String.format(
											"mDataFragment.scheduledReminders.remove(%s)",
											"" + i));
						}
						mDataFragment.scheduledReminders.remove(i);
						mAdapter.notifyDataSetChanged();
						break;
					}
				}
				//
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, String.format(
							"mDataFragment.scheduledReminders.size(): %s", ""
									+ mDataFragment.scheduledReminders.size()));
				}
				if (mDataFragment.scheduledReminders.size() == 0) {
					if (isTaskRoot()) {
						ActivityExit.exitApplicationAndRemoveFromRecent(context);
					} else {
						if (ActivityAlarm.DEBUG) {
							Log.d(ActivityAlarm.ActivityAlarmDebug,
									String.format("finish()"));
						}
						finish();
					}
				} else {
					relaunchMediaPlayerAndVibratorIfNeeded();
				}
			}
		});
		// Long[] snoozedAlarms = mAutomaticSnoozer.pullSnoozedAlarms();
		// if (snoozedAlarms != null) {
		// for (long id : snoozedAlarms) {
		// // ActivityAlarm.snoozeReminder(ActivityAlarm.this, id, null);
		// for (int i = 0; i < mDataFragment.scheduledReminders.size(); i++) {
		// if (mDataFragment.scheduledReminders.get(i).getId() == id) {
		// if (ActivityAlarm.DEBUG) {
		// Log.d(ActivityAlarm.ActivityAlarmDebug,
		// String.format(
		// "mDataFragment.scheduledReminders.remove(%s)",
		// "" + i));
		// }
		// mDataFragment.scheduledReminders.remove(i);
		// mAdapter.notifyDataSetChanged();
		// break;
		// }
		// }
		// }
		// //
		// if (mDataFragment.scheduledReminders.size() == 0) {
		// if (isTaskRoot()) {
		// ActivityExit.exitApplicationAndRemoveFromRecent(ActivityAlarm.this);
		// } else {
		// finish();
		// }
		// }
		// }
		for (ScheduledReminder scheduledReminder : mDataFragment.scheduledReminders) {
			Long id = scheduledReminder.getId();
			Resources resources = getResources();
			int delayMillis = (int) (-Math.max(System.currentTimeMillis()
					- scheduledReminder.getActualLastAlarmedDateTime(), 1) + 1000 * scheduledReminder
					.getPlayingTime2(
							context,
							R.string.preference_key_reminders_popup_window_displaying_duration,
							resources
									.getInteger(R.integer.reminders_popup_window_displaying_duration_default_value),
							resources
									.getInteger(R.integer.reminders_popup_window_displaying_duration_min_value),
							resources
									.getInteger(R.integer.reminders_popup_window_displaying_duration_max_value)));
			mAutomaticSnoozer.postDelayed(id, delayMillis);
		}
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		isUserLeaving = true;
		finish();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isBackPressed = true;
	}

	@Override
	protected void onPause() {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, String.format("onPause(): %s", this));
		}
		super.onPause();
		mAutomaticSnoozer.setListener(null);
		boolean isFinishing = isFinishing();
		if (isFinishing) {
			releaseMediaPlayerAndCancelVibrator();
			mAutomaticSnoozer.cancel(null);
			while (mDataFragment.scheduledReminders.size() > 0) {
				ActivityAlarm.snoozeReminder(ActivityAlarm.this,
						mDataFragment.scheduledReminders.remove(0).getId(), null,
						!(isUserLeaving | isBackPressed));
			}
			biz.advancedcalendar.services.NotificationService
					.updateNotification(ActivityAlarm.this);
			LocalBroadcastManager.getInstance(ActivityAlarm.this).sendBroadcast(
					new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_alarm, menu);
		MenuItem menuItem = menu.add(Menu.NONE, CommonConstants.MENU_ID_MUTE_ALARM, 0,
				getResources().getString(R.string.action_mute_alarm));
		menuItem.setIcon(R.drawable.ic_volume_off_black_24dp);
		MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		//
		// editItem = menu.add(Menu.NONE,
		// CommonConstants.MENU_ID_SNOOZE_MODE_TIME_INTERVAL,
		// 0, getResources().getString(R.string.action_mute_alarm));
		// //editItem.setIcon(R.drawable.ic_volume_off);
		// MenuItemCompat.setShowAsAction(editItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			return true;
		case CommonConstants.MENU_ID_MUTE_ALARM:
			if (mDataFragment.muted) {
				mDataFragment.muted = false;
				item.setIcon(R.drawable.ic_volume_off_black_24dp);
				relaunchMediaPlayerAndVibratorIfNeeded();
			} else {
				mDataFragment.muted = true;
				item.setIcon(R.drawable.ic_volume_up_black_24dp);
				releaseMediaPlayerAndCancelVibrator();
			}
			return true;
			// case CommonConstants.MENU_ID_SNOOZE_MODE_TIME_INTERVAL:
			// if (mDataFragment.muted) {
			// mDataFragment.muted = false;
			// item.setIcon(R.drawable.ic_volume_off);
			// relaunchMediaPlayerAndVibratorIfNeeded();
			// } else {
			// mDataFragment.muted = true;
			// item.setIcon(R.drawable.ic_volume_on);
			// releaseMediaPlayerAndCancelVibrator();
			// }
			// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("onNewIntent(): %s", this));
		}
		super.onNewIntent(intent);
		List<ScheduledReminder> alarmedAlarms = DataProvider.getAlarmedAlarms(null,
				ActivityAlarm.this);
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, "alarmedAlarms: " + alarmedAlarms);
		}
		if (alarmedAlarms == null || alarmedAlarms.size() == 0) {
			if (ActivityAlarm.DEBUG) {
				Log.d(ActivityAlarm.ActivityAlarmDebug, "isTaskRoot(): " + isTaskRoot());
			}
			if (isTaskRoot()) {
				ActivityExit.exitApplicationAndRemoveFromRecent(this);
			} else {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, String.format("finish()"));
				}
				finish();
			}
			return;
		}
		for (int i = 0; i < mDataFragment.scheduledReminders.size(); i++) {
			ScheduledReminder scheduledReminder = mDataFragment.scheduledReminders.get(i);
			boolean found = false;
			for (int i1 = 0; i1 < alarmedAlarms.size(); i1++) {
				ScheduledReminder scheduledReminder2 = alarmedAlarms.get(i1);
				if (scheduledReminder2.getId().equals(scheduledReminder.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, String.format(
							"mDataFragment.scheduledReminders.remove(%s)", "" + i));
				}
				mDataFragment.scheduledReminders.remove(i);
				i--;
			}
		}
		List<ScheduledReminder> toAddScheduledReminders = new ArrayList<ScheduledReminder>();
		for (ScheduledReminder scheduledReminder : alarmedAlarms) {
			boolean found = false;
			for (int i = 0; i < mDataFragment.scheduledReminders.size(); i++) {
				ScheduledReminder scheduledReminder2 = mDataFragment.scheduledReminders
						.get(i);
				if (scheduledReminder2.getId().equals(scheduledReminder.getId())) {
					found = true;
					break;
				}
			}
			if (!found) {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, String.format(
							"toAddScheduledReminders.add(scheduledReminder: %s);", ""
									+ scheduledReminder));
				}
				toAddScheduledReminders.add(scheduledReminder);
			}
		}
		boolean isDataSetChanged = false;
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug, "toAddScheduledReminders: "
					+ toAddScheduledReminders);
		}
		for (ScheduledReminder scheduledReminder : toAddScheduledReminders) {
			isDataSetChanged = true;
			if (ActivityAlarm.DEBUG) {
				Log.d(ActivityAlarm.ActivityAlarmDebug,
						String.format("mDataFragment.scheduledReminders.add(%s)", ""
								+ scheduledReminder));
			}
			mDataFragment.scheduledReminders.add(scheduledReminder);
		}
		if (isDataSetChanged) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private MediaPlayer initializeMediaPlayer(String ringtone) {
		if (ActivityAlarm.DEBUG2) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("initializeMediaPlayer(String ringtone: %s)", ""
							+ ringtone));
		}
		// Log.d(CommonConstants.DEBUG_TAG, "ActivityAlarm initializeMediaPlayer()");
		MediaPlayer mediaPlayer;
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(ActivityAlarm.this, Uri.parse(ringtone));
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
			mediaPlayer.prepare();
		} catch (Exception e) {
			try {
				String ringtone2 = Helper.getStringPreferenceValue(this, getResources()
						.getString(R.string.preference_key_alarm_ringtone), null);
				mediaPlayer = new MediaPlayer();
				mediaPlayer.setDataSource(ActivityAlarm.this, Uri.parse(ringtone2));
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.prepare();
			} catch (Exception e1) {
				try {
					Uri defaultUri = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_ALARM);
					mediaPlayer = new MediaPlayer();
					mediaPlayer.setDataSource(ActivityAlarm.this, defaultUri);
					mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
					mediaPlayer.prepare();
				} catch (Exception e2) {
					try {
						Uri actualDefaultRingtoneUri = RingtoneManager
								.getActualDefaultRingtoneUri(this,
										RingtoneManager.TYPE_ALARM);
						mediaPlayer = new MediaPlayer();
						mediaPlayer.setDataSource(ActivityAlarm.this,
								actualDefaultRingtoneUri);
						mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
						mediaPlayer.prepare();
					} catch (Exception e3) {
						// this should always work
						mediaPlayer = new MediaPlayer();
						try {
							AssetFileDescriptor assetFileDescriptor = getAssets().openFd(
									"greeting.mid");
							mediaPlayer.setDataSource(
									assetFileDescriptor.getFileDescriptor(),
									assetFileDescriptor.getStartOffset(),
									assetFileDescriptor.getLength());
							assetFileDescriptor.close();
							mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
							mediaPlayer.prepare();
						} catch (IllegalArgumentException e5) {
							// TODO Auto-generated catch block
							e5.printStackTrace();
						} catch (IllegalStateException e5) {
							// TODO Auto-generated catch block
							e5.printStackTrace();
						} catch (IOException e5) {
							// TODO Auto-generated catch block
							e5.printStackTrace();
						}
					}
				}
			}
		}
		// TODO setScreenOnWhilePlaying(true) is ineffective without a
		// SurfaceHolder
		mediaPlayer.setScreenOnWhilePlaying(true);
		mediaPlayer.setLooping(true);
		return mediaPlayer;
	}

	public static synchronized void snoozeReminder(final Context context, final long id,
			final Long snoozeDateTime, final boolean isAutomatic) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format(
							"snoozeReminder(final Context context, final long id: %s, final Long snoozeDateTime: %s)",
							"" + id, ""
									+ (snoozeDateTime == null ? null : new Date(
											snoozeDateTime))));
		}
		DataProvider.runInTx(null, context, new Runnable() {
			@Override
			public void run() {
				// mAutomaticSnoozer.cancel(id);
				ScheduledReminder scheduledReminder = DataProvider.getScheduledReminder(
						null, context, id);
				if (scheduledReminder != null) {
					Resources resources = context.getResources();
					int automaticSnoozesMaxCount = scheduledReminder
							.getAutomaticSnoozesMaxCount2(
									context,
									R.string.preference_key_automatic_snoozes_max_count,
									resources
											.getInteger(R.integer.automatic_snoozes_max_count_default_value),
									resources
											.getInteger(R.integer.automatic_snoozes_max_count_min_value),
									resources
											.getInteger(R.integer.automatic_snoozes_max_count_max_value));
					if (!isAutomatic
							|| scheduledReminder.getSnoozeCount() < automaticSnoozesMaxCount) {
						if (ScheduledReminder.State.fromInt(
								scheduledReminder.getStateValue()).equals(
								ScheduledReminder.State.ALARMED)
								&& (scheduledReminder.getReminder() == null ? scheduledReminder
										.getIsAlarm() : scheduledReminder.getReminder()
										.getIsAlarm())) {
							long nextSheduledReminderDateTime;
							if (snoozeDateTime == null) {
								if (Math.abs(scheduledReminder
										.getActualLastAlarmedDateTime()
										- scheduledReminder.getNextSnoozeDateTime()) > CommonConstants.SCHEDULED_ALARM_DATE_TIME_TO_ACTUAL_ALARMED_DATE_TIME_TOLERANCE) {
									nextSheduledReminderDateTime = System
											.currentTimeMillis();
								} else {
									nextSheduledReminderDateTime = scheduledReminder
											.getNextSnoozeDateTime();
								}
								nextSheduledReminderDateTime += scheduledReminder
										.getAutomaticSnoozeDuration2(
												context,
												R.string.preference_key_automatic_snooze_duration,
												resources
														.getInteger(R.integer.automatic_snooze_duration_default_value),
												resources
														.getInteger(R.integer.automatic_snooze_duration_min_value),
												resources
														.getInteger(R.integer.automatic_snooze_duration_max_value)) * 60L * 1000;
							} else {
								nextSheduledReminderDateTime = snoozeDateTime;
							}
							scheduledReminder.setSnoozeCount(scheduledReminder
									.getSnoozeCount() + 1);
							scheduledReminder
									.setNextSnoozeDateTime(nextSheduledReminderDateTime);
							// scheduledReminder.setActualLastAlarmedDateTime(null);
							scheduledReminder
									.setStateValue(ScheduledReminder.State.SCHEDULED
											.getValue());
							DataProvider.insertOrReplaceScheduledReminder(null, context,
									scheduledReminder);
							if (ActivityAlarm.DEBUG) {
								Log.d(ActivityAlarm.ActivityAlarmDebug,
										String.format(
												"AlarmService.setAlarmForReminder(final Context context, final long reminderId: %s, final long dateTime: %s, final Runnable r)",
												"" + scheduledReminder.getId(),
												""
														+ new Date(scheduledReminder
																.getNextSnoozeDateTime())));
							}
							AlarmService.setAlarmForReminder(context,
									scheduledReminder.getId(),
									scheduledReminder.getNextSnoozeDateTime(), this);
						}
					} else {
						if (ActivityAlarm.DEBUG) {
							Log.d(ActivityAlarm.ActivityAlarmDebug,
									String.format(
											"DataProvider.deleteScheduledReminder(context, id: %s)",
											"" + scheduledReminder.getId()));
						}
						DataProvider.deleteScheduledReminder(null, context,
								scheduledReminder.getId());
					}
				}
			}
		});
	}

	private void relaunchMediaPlayerAndVibratorIfNeeded() {
		if (mDataFragment.currentAlarmsSoundIsMuted) {
			return;
		}
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("relaunchMediaPlayerAndVibratorIfNeeded()"));
		}
		ScheduledReminder scheduledReminder = mDataFragment.scheduledReminders.get(0);
		Resources resources = getResources();
		if (mDataFragment.mediaPlayer == null) {
			if (!mDataFragment.muted) {
				String ringtone = scheduledReminder.getRingtone(this,
						((Global) getApplicationContext()).getDaoSession());
				Boolean isRingtoneSilent = Helper.isRingtoneSilent(this, ringtone);
				if (isRingtoneSilent == null || !isRingtoneSilent) {
					mDataFragment.mediaPlayer = initializeMediaPlayer(ringtone);
					mDataFragment.mediaPlayer.setVolume(0.0f, 0.0f);
					if (ActivityAlarm.DEBUG) {
						Log.d(ActivityAlarm.ActivityAlarmDebug, "0.0f " + 0.0f);
					}
					mDataFragment.mediaPlayer.start();
					long fadeInTime = scheduledReminder
							.getRingtoneFadeInTime2(
									this,
									R.string.preference_key_ringtone_fade_in_time,
									resources
											.getInteger(R.integer.ringtone_fade_in_time_default_value),
									(long) resources
											.getInteger(R.integer.ringtone_fade_in_time_min_value),
									(long) resources
											.getInteger(R.integer.ringtone_fade_in_time_max_value));
					startVolumeIncreaser(fadeInTime, 0.0f, 1.0f);
				}
			}
		}
		if (mDataFragment.vibrator == null) {
			if (!mDataFragment.muted) {
				if (scheduledReminder
						.getVibrate2(
								this,
								resources
										.getString(R.string.preference_key_vibrate_with_alarm_ringtone),
								resources
										.getBoolean(R.bool.vibrate_with_alarm_ringtone_default_value))) {
					mDataFragment.vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					mDataFragment.vibrator.vibrate(mDataFragment.pattern, 0);
				}
			}
		}
	}

	public void startVolumeIncreaser(long delayMillis, float initialVolume,
			float finalVolume) {
		if (mTimerHandler == null) {
			mTimerHandler = new Handler();
		} else if (mRunnable != null) {
			mTimerHandler.removeCallbacks(mRunnable);
		}
		mRunnable = new VolumeIncreaserRunnable(delayMillis, initialVolume, finalVolume);
		mTimerHandler.post(mRunnable);
	}

	public void stopVolumeIncreaser() {
		if (mTimerHandler != null && mRunnable != null) {
			mTimerHandler.removeCallbacks(mRunnable);
		}
		if (mRunnable != null) {
			mRunnable.cancel();
		}
	}

	private void releaseMediaPlayerAndCancelVibrator() {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("releaseMediaPlayerAndCancelVibrator()"));
		}
		if (mDataFragment.mediaPlayer != null) {
			try {
				stopVolumeIncreaser();
				// mDataFragment.mediaPlayer.stop();
				mDataFragment.mediaPlayer.release();
				mDataFragment.mediaPlayer = null;
			} catch (IllegalStateException e) {
				Log.e(CommonConstants.ERROR_TAG, e.getMessage());
			}
		}
		if (mDataFragment.vibrator != null) {
			mDataFragment.vibrator.cancel();
			mDataFragment.vibrator = null;
		}
	}

	private class ActivityAlarmArrayAdapter extends ArrayAdapter<ScheduledReminder> {
		List<ScheduledReminder> objects;
		private LayoutInflater mLayoutInflater;

		class ScheduledReminderComparator implements Comparator<ScheduledReminder> {
			@Override
			public int compare(ScheduledReminder a, ScheduledReminder b) {
				if (a.getNextSnoozeDateTime() > a.getNextSnoozeDateTime()) {
					return 1;
				} else if (a.getNextSnoozeDateTime() < a.getNextSnoozeDateTime()) {
					return -1;
				} else {
					return 0;
				}
			}
		}

		public ActivityAlarmArrayAdapter(Context context, int textViewResourceId,
				List<ScheduledReminder> objects) {
			super(context, textViewResourceId, objects);
			mLayoutInflater = LayoutInflater.from(context);
			this.objects = objects;
			Collections.sort(this.objects, new ScheduledReminderComparator());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final LinearLayout convertView2;
			if (convertView == null) {
				convertView2 = (LinearLayout) getLayoutInflater().inflate(
						R.layout.activity_alarm_list_item, parent, false);
			} else {
				convertView2 = (LinearLayout) convertView;
			}
			ScheduledReminder activityAlarmScheduledReminderInfoHolder = getItem(position);
			Reminder reminder = activityAlarmScheduledReminderInfoHolder
					.getReminder(((Global) getApplicationContext()).getDaoSession());
			Task task = null;
			View colorBox = convertView2
					.findViewById(R.id.activity_alarm_list_item_color_box);
			if (reminder != null) {
				task = DataProvider.getTask(null, ActivityAlarm.this,
						reminder.getTaskId(), false);
				int backgroundColor = task.getColor2(ActivityAlarm.this);
				colorBox.setBackgroundColor(backgroundColor);
			}
			colorBox.setVisibility(View.GONE);
			// activityAlarmScheduledReminderInfoHolder.listItemView = convertView2;
			TableLayout tableLayout = (TableLayout) convertView2
					.findViewById(R.id.activity_alarm_tablelayout);
			setupText(activityAlarmScheduledReminderInfoHolder, tableLayout, task);
			Button buttonSnooze = (Button) convertView2
					.findViewById(R.id.activity_alarm_button_snooze);
			Button buttonDismiss = (Button) convertView2
					.findViewById(R.id.activity_alarm_button_dismiss);
			// setup buttonDismiss listener
			buttonDismiss.setTag(activityAlarmScheduledReminderInfoHolder);
			buttonDismiss.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ScheduledReminder activityAlarmScheduledReminderInfoHolder = (ScheduledReminder) v
							.getTag();
					// activityAlarmScheduledReminderInfoHolder.timerHandler
					// .removeCallbacks(activityAlarmScheduledReminderInfoHolder.timerRunnable);
					int index = mDataFragment.scheduledReminders
							.lastIndexOf(activityAlarmScheduledReminderInfoHolder);
					mDataFragment.scheduledReminders.remove(index);
					if (index == 0) {
						releaseMediaPlayerAndCancelVibrator();
					}
					DataProvider.deleteScheduledReminder(null, ActivityAlarm.this,
							activityAlarmScheduledReminderInfoHolder.getId());
					notifyDataSetChanged();
					NotificationService.updateNotification(ActivityAlarm.this);
					LocalBroadcastManager
							.getInstance(ActivityAlarm.this)
							.sendBroadcast(
									new Intent(
											CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
					if (objects.size() == 0) {
						if (isTaskRoot()) {
							ActivityExit
									.exitApplicationAndRemoveFromRecent(ActivityAlarm.this);
						} else {
							finish();
						}
					} else {
						relaunchMediaPlayerAndVibratorIfNeeded();
					}
				}
			});
			buttonSnooze.setText(getResources().getString(
					R.string.activity_alarm_button_snooze));
			buttonSnooze.setTag(activityAlarmScheduledReminderInfoHolder);
			buttonSnooze.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					releaseMediaPlayerAndCancelVibrator();
					mDataFragment.currentAlarmsSoundIsMuted = true;
					ScheduledReminder scheduledReminder = (ScheduledReminder) v.getTag();
					Resources resources = getResources();
					ActivityAlarm context = ActivityAlarm.this;
					int automaticSnoozeDurationMinutes = scheduledReminder
							.getAutomaticSnoozeDuration2(
									context,
									R.string.preference_key_automatic_snooze_duration,
									resources
											.getInteger(R.integer.automatic_snooze_duration_default_value),
									resources
											.getInteger(R.integer.automatic_snooze_duration_min_value),
									resources
											.getInteger(R.integer.automatic_snooze_duration_max_value));
					int hours = automaticSnoozeDurationMinutes / 60;
					if (hours > 23) {
						hours = 23;
					}
					int minutes = automaticSnoozeDurationMinutes % 60;
					ArrayList<TimeAttribute> timeAttributes = new ArrayList<TimeAttribute>();
					timeAttributes.add(new TimeAttribute(hours, minutes,
							R.string.radiobutton_text_reminder_snooze_mode_timespan, 0,
							true, false));
					Calendar calendar = Calendar.getInstance();
					calendar.add(Calendar.HOUR_OF_DAY, hours);
					calendar.add(Calendar.MINUTE, minutes);
					hours = calendar.get(Calendar.HOUR_OF_DAY);
					minutes = calendar.get(Calendar.MINUTE);
					boolean is24HourFormat = Helper.is24HourFormat(context);
					Bundle bundle = new Bundle();
					bundle.putParcelable("scheduledReminder", scheduledReminder);
					timeAttributes.add(new TimeAttribute(hours, minutes,
							R.string.radiobutton_text_reminder_snooze_mode_absolute_time,
							1, is24HourFormat, true));
					mTimePickerDialogMultiple = TimePickerDialogMultiple.newInstance(
							context, bundle, R.string.time_picker_title_for_snooze_mode,
							timeAttributes, 0);
					mTimePickerDialogMultiple.show(getSupportFragmentManager(),
							mTimePickerDialogKey);
				}
			});
			return convertView2;
		}

		private void setupText(ScheduledReminder scheduledReminder,
				TableLayout tableLayout, Task task) {
			tableLayout.removeAllViews();
			TableRow tableRow;
			TextView textViewHeader;
			TextView textViewValue;
			// setup reminder text
			Reminder reminder = scheduledReminder
					.getReminder(((Global) getApplicationContext()).getDaoSession());
			((TextView) (tableRow = (TableRow) mLayoutInflater.inflate(
					R.layout.tablerow_single_textview, tableLayout, false))
					.findViewById(R.id.tablerow_single_textview_textview))
					.setText(reminder == null ? scheduledReminder.getText() : reminder
							.getText());
			tableLayout.addView(tableRow);
			// setup task text
			if (task != null) {
				tableRow = (TableRow) LayoutInflater.from(ActivityAlarm.this).inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
				textViewHeader = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textViewHeader.setText(getResources().getString(
						R.string.activity_alarm_header_textview_task_name));
				textViewValue = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				textViewValue.setText(task.getName());
				int backgroundColor = task.getColor2(ActivityAlarm.this);
				int textColor;
				if (Helper.getContrastYIQ(backgroundColor)) {
					textColor = ContextCompat.getColor(ActivityAlarm.this,
							R.color.task_view_text_synchronized_dark);
				} else {
					textColor = ContextCompat.getColor(ActivityAlarm.this,
							R.color.task_view_text_synchronized_light);
				}
				textViewValue.setBackgroundColor(backgroundColor);
				textViewValue.setTextColor(textColor);
				tableLayout.addView(tableRow);
			}
			// setup alarm time
			tableRow = (TableRow) LayoutInflater.from(ActivityAlarm.this).inflate(
					R.layout.fragment_view_task_part_main_table_row, tableLayout, false);
			textViewHeader = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
			textViewHeader.setText(getResources().getString(
					R.string.fragment_view_reminders_reminder_scheduled_time));
			textViewValue = (TextView) tableRow
					.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
			textViewValue.setText(mDateTimeFormat.format(new Date(scheduledReminder
					.getAssignedRemindAtDateTime())));
			tableLayout.addView(tableRow);
			// setup reminder snooze count
			if (scheduledReminder.getSnoozeCount() > 0) {
				tableRow = (TableRow) mLayoutInflater.inflate(
						R.layout.fragment_view_task_part_main_table_row, tableLayout,
						false);
				textViewHeader = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_left_textview);
				textViewHeader.setText(getResources().getString(
						R.string.fragment_view_reminders_snooze_count));
				textViewValue = (TextView) tableRow
						.findViewById(R.id.fragment_view_task_part_main_tablerow_right_textview);
				textViewValue.setText(String.valueOf(scheduledReminder.getSnoozeCount()));
				tableLayout.addView(tableRow);
			}
		}

		@Override
		public long getItemId(int position) {
			return objects.get(position).getId();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	@Override
	public void onTimeSet(RadialPickerLayout view, Bundle bundle,
			ArrayList<TimeAttribute> timeAttributes, int ordinalNumber) {
		if (ActivityAlarm.DEBUG) {
			Log.d(ActivityAlarm.ActivityAlarmDebug,
					String.format("onTimeSet(): %s", this));
		}
		mDataFragment.currentAlarmsSoundIsMuted = false;
		Resources resources = getResources();
		ScheduledReminder scheduledReminder = bundle.getParcelable("scheduledReminder");
		mAutomaticSnoozer.cancel(scheduledReminder.getId());
		// mDataFragment.scheduledReminderPendingOnTimePickerDialog = null;
		int index = mDataFragment.scheduledReminders.lastIndexOf(scheduledReminder);
		if (index >= 0) {
			if (ActivityAlarm.DEBUG) {
				Log.d(ActivityAlarm.ActivityAlarmDebug,
						String.format("mDataFragment.scheduledReminders.remove(%s)", ""
								+ index));
			}
			mDataFragment.scheduledReminders.remove(index);
			mAdapter.notifyDataSetChanged();
		}
		String text;
		long snoozeDateTime;
		boolean isTimeSpan = ordinalNumber == 0;
		TimeAttribute timeAttribute = timeAttributes.get(ordinalNumber);
		int hours = timeAttribute.getHours();
		int minutes = timeAttribute.getMinutes();
		if (isTimeSpan) {
			long timeSpan = (hours * 60 + minutes) * 60 * 1000L;
			String textTime = Helper.getTextForTimeInterval(this, 0, hours, minutes, 0);
			text = String
					.format(resources
							.getString(R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_timespan),
							textTime);
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MILLISECOND, 0);
			snoozeDateTime = calendar.getTimeInMillis() + timeSpan;
		} else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			Calendar calendar2 = (Calendar) calendar.clone();
			calendar2.set(Calendar.HOUR_OF_DAY, hours);
			calendar2.set(Calendar.MINUTE, minutes);
			DateFormat timeFormat = DateFormat.getTimeInstance();
			int textId;
			snoozeDateTime = calendar2.getTimeInMillis();
			long calendarTimeInMillis = calendar.getTimeInMillis();
			if (snoozeDateTime < calendarTimeInMillis) {
				calendar2.add(Calendar.DAY_OF_YEAR, 1);
				textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_tomorrow;
			} else {
				textId = R.string.activity_alarm_toast_next_alarm_time_for_snooze_mode_absolute_time_today;
			}
			text = String.format(resources.getString(textId),
					timeFormat.format(new Date(snoozeDateTime)));
		}
		ActivityAlarm.snoozeReminder(ActivityAlarm.this, scheduledReminder.getId(),
				snoozeDateTime, false);
		Toast.makeText(this, text, Toast.LENGTH_LONG).show();
		NotificationService.updateNotification(ActivityAlarm.this);
		LocalBroadcastManager.getInstance(ActivityAlarm.this).sendBroadcast(
				new Intent(CommonConstants.ACTION_ENTITIES_CHANGED_REMINDERS));
		if (mAdapter.objects.size() == 0) {
			if (isTaskRoot()) {
				ActivityExit.exitApplicationAndRemoveFromRecent(ActivityAlarm.this);
			} else {
				if (ActivityAlarm.DEBUG) {
					Log.d(ActivityAlarm.ActivityAlarmDebug, String.format("finish()"));
				}
				finish();
			}
		} else {
			relaunchMediaPlayerAndVibratorIfNeeded();
		}
	}

	@Override
	public boolean isTimeConsistent(ArrayList<TimeAttribute> timeAttributes, Bundle bundle) {
		return true;
	}
}
