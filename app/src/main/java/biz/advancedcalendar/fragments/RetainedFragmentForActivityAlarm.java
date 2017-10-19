package biz.advancedcalendar.fragments;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import biz.advancedcalendar.greendao.ScheduledReminder;
import java.util.ArrayList;
import java.util.List;

// TODO have to implement onSaveInstanceState and state restoration
public class RetainedFragmentForActivityAlarm extends Fragment {
	public MediaPlayer mediaPlayer;
	public Vibrator vibrator;
	public long[] pattern = new long[] {0, 750, 500};
	public boolean muted;
	public boolean currentAlarmsSoundIsMuted;
	public List<ScheduledReminder> scheduledReminders;

	// public ScheduledReminder scheduledReminderPendingOnTimePickerDialog;
	// private int mSnoozeCount;
	// this method is only called once for this fragment
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			pattern = savedInstanceState.getLongArray("pattern");
			muted = savedInstanceState.getBoolean("muted");
			currentAlarmsSoundIsMuted = savedInstanceState
					.getBoolean("currentAlarmsSoundIsMuted");
			scheduledReminders = savedInstanceState
					.getParcelableArrayList("scheduledReminders");
			// scheduledReminderPendingOnTimePickerDialog = savedInstanceState
			// .getParcelable("scheduledReminderPendingOnTimePickerDialog");
		}
		// retain this fragment
		setRetainInstance(true);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putLongArray("pattern", pattern);
		savedInstanceState.putBoolean("muted", muted);
		savedInstanceState.putBoolean("currentAlarmsSoundIsMuted",
				currentAlarmsSoundIsMuted);
		ArrayList<ScheduledReminder> scheduledReminders2 = new ArrayList<ScheduledReminder>(
				scheduledReminders);
		savedInstanceState.putParcelableArrayList("scheduledReminders",
				scheduledReminders2);
		// savedInstanceState.putParcelable("scheduledReminderPendingOnTimePickerDialog",
		// scheduledReminderPendingOnTimePickerDialog);
	}
}