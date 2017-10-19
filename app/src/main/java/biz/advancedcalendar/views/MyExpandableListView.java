package biz.advancedcalendar.views;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.ExpandableListView;

public class MyExpandableListView extends ExpandableListView {
	public MyExpandableListView(Context context) {
		super(context);
	}

	public MyExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	// public MyExpandableListView(Context context, AttributeSet attrs, int defStyleAttr,
	// int defStyleRes) {
	// super(context, attrs, defStyleAttr, defStyleRes);
	// }
	@Override
	public Parcelable onSaveInstanceState() {
		return super.onSaveInstanceState();
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
	}
}
