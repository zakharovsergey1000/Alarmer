package biz.advancedcalendar.activities;

import android.content.Context;
import android.preference.RingtonePreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyRingtonePreference extends RingtonePreference {
	public MyRingtonePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		makeMultiline(view);
	}

	protected void makeMultiline(View view) {
		TextView textView = (TextView) view.findViewById(android.R.id.title);
		if (textView != null) {
			textView.setSingleLine(false);
			textView.setEllipsize(null);
		}
	}
}
