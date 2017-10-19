package biz.advancedcalendar.activities;

import android.content.Context;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.PreferenceViewHolder;
// import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyCheckBoxPreference extends CheckBoxPreference {
	public MyCheckBoxPreference(Context context) {
		super(context);
	}

	public MyCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onBindViewHolder(PreferenceViewHolder arg0) {
		super.onBindViewHolder(arg0);
		makeMultiline(arg0.itemView);
	}

	protected void makeMultiline(View view) {
		TextView textView = (TextView) view.findViewById(android.R.id.title);
		if (textView != null) {
			textView.setSingleLine(false);
			textView.setEllipsize(null);
		}
	}
}
