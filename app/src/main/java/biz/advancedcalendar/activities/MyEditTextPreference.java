package biz.advancedcalendar.activities;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyEditTextPreference extends EditTextPreference {
	public MyEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// @Override
	// protected void onBindDialogView(View view) {
	// super.onBindDialogView(view);
	// EditText editText = getEditText();
	// int length = editText.getText().length();
	// editText.setSelection(0, length);
	// }
	//
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

	protected void makeMultiline2(View view) {
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = (ViewGroup) view;
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				makeMultiline2(viewGroup.getChildAt(i));
			}
		} else if (view instanceof TextView) {
			TextView textView = (TextView) view;
			textView.setSingleLine(false);
			textView.setEllipsize(null);
		}
	}
}
