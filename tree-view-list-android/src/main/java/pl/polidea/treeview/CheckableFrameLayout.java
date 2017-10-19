/**
 * 
 */
package pl.polidea.treeview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Extension of a frame layout to provide a checkable behaviour
 * 
 */
public class CheckableFrameLayout extends RelativeLayout implements Checkable {

	/**
	 * Interface definition for a callback to be invoked when the checked state of a CheckableFrameLayout changed.
	 */
	public static interface OnCheckedChangeListener {
		public void onCheckedChanged(CheckableFrameLayout layout, boolean isChecked);
	}

	public CheckableFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialise(attrs);
	}

	public CheckableFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise(attrs);
	}

	public CheckableFrameLayout(Context context, int checkableId) {
		super(context);
		initialise(null);
	}

	/*
	 * @see android.widget.Checkable#isChecked()
	 */
	@Override
	public boolean isChecked() {
		return isChecked;
	}

	/*
	 * @see android.widget.Checkable#setChecked(boolean)
	 */
	@Override
	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;

		for (int i = 0; i < getChildCount(); ++i) {
			View v = getChildAt(i);
			if (v instanceof Checkable) {
				Checkable c = (Checkable) v;
				c.setChecked(isChecked);
			}
		}

		if (onCheckedChangeListener != null) {
			onCheckedChangeListener.onCheckedChanged(this, isChecked);
		}
	}

	/*
	 * @see android.widget.Checkable#toggle()
	 */
	@Override
	public void toggle() {
		this.isChecked = !this.isChecked;

		for (int i = 0; i < getChildCount(); ++i) {
			View v = getChildAt(i);
			if (v instanceof Checkable) {
				Checkable c = (Checkable) v;
				c.setChecked(isChecked);
			}
		}

		if (onCheckedChangeListener != null) {
			onCheckedChangeListener.onCheckedChanged(this, isChecked);
		}
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
		this.onCheckedChangeListener = onCheckedChangeListener;
	}

//	@Override
//	protected void onFinishInflate() {
//		super.onFinishInflate();
//
//		final int childCount = this.getChildCount();
//		for (int i = 0; i < childCount; ++i) {
//			findCheckableChildren(this.getChildAt(i));
//		}
//	}

	/**
	 * Read the custom XML attributes
	 */
	private void initialise(AttributeSet attrs) {
		this.isChecked = false;
	}

	private boolean isChecked;
	private OnCheckedChangeListener onCheckedChangeListener;
}
