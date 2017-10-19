package biz.advancedcalendar.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.LinearLayout;

public class CheckableLinearLayout extends LinearLayout implements Checkable {
	private boolean mChecked;
	private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

	public CheckableLinearLayout(Context context) {
		this(context, null);
		init();
	}

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		// setClickable(true);
	}

	/**********************/
	/** Handle clicks **/
	/**********************/
	@Override
	public boolean performClick() {
		toggle();
		return super.performClick();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return onTouchEvent(ev);
	}

	/**************************/
	/** Checkable **/
	/**************************/
	@Override
	public void toggle() {
		setChecked(!mChecked);
	}

	@Override
	public boolean isChecked() {
		return mChecked;
	}

	@Override
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
			setCheckedRecursive(this, checked);
		}
	}

	private void setCheckedRecursive(ViewGroup parent, boolean checked) {
		int count = parent.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = parent.getChildAt(i);
			if (v instanceof Checkable) {
				((Checkable) v).setChecked(checked);
			}
			if (v instanceof ViewGroup) {
				setCheckedRecursive((ViewGroup) v, checked);
			}
		}
	}

	/**************************/
	/** Drawable States **/
	/**************************/
	@Override
	protected int[] onCreateDrawableState(int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			View.mergeDrawableStates(drawableState,
					CheckableLinearLayout.CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		Drawable drawable = getBackground();
		if (drawable != null) {
			int[] myDrawableState = getDrawableState();
			drawable.setState(myDrawableState);
			invalidate();
		}
	}

	/**************************/
	/** State persistency **/
	/**************************/
	static class SavedState extends BaseSavedState {
		boolean checked;

		SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			checked = in.readByte() != 0x00;
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeByte((byte) (checked ? 0x01 : 0x00));
		}

		@Override
		public String toString() {
			return "CheckableLinearLayout.SavedState{"
					+ Integer.toHexString(System.identityHashCode(this)) + " checked="
					+ checked + "}";
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	@Override
	public Parcelable onSaveInstanceState() {
		// Force our ancestor class to save its state
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.checked = isChecked();
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		setChecked(ss.checked);
		requestLayout();
	}
}