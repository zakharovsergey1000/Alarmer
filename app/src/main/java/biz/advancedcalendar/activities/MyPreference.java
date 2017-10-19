package biz.advancedcalendar.activities;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
// import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class MyPreference extends Preference {
	public MyPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyPreference(Context context, AttributeSet attrs, int defStyleAttr,
			int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public MyPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public MyPreference(Context context) {
		super(context);
	}

	// @Override
	// protected void onBindView(View view) {
	// super.onBindView(view);
	// makeMultiline(view);
	// }
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
