package biz.advancedcalendar.views;

import pl.polidea.treeview.InMemoryTreeStateManager;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

public class MyHorizontalScrollView extends HorizontalScrollView {

	private static final String TAG = InMemoryTreeStateManager.class
			.getSimpleName();

	public MyHorizontalScrollView(Context p_context, AttributeSet p_attrs) {
		super(p_context, p_attrs);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (no_scrolling)
			return false;

		// Standard behavior
		//
		return super.onTouchEvent(ev);
	}

	boolean no_scrolling = false;
	float old_x, old_y;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getActionMasked();
		Log.d(TAG, "HSV scroll intercept: " + String.format("0x%08x", action));

		if (action == MotionEvent.ACTION_DOWN) {
			old_x = ev.getX();
			old_y = ev.getY();
			no_scrolling = false;

		} else if (action == MotionEvent.ACTION_MOVE) {
			float dx = ev.getX() - old_x;
			float dy = ev.getY() - old_y;

			if (Math.abs(dx) > Math.abs(dy) && dx != 0) {
				View hsvChild = getChildAt(0);
				int childW = hsvChild.getWidth();
				int W = getWidth();

				Log.d(TAG, "HSV " + childW + " > " + W + " ? dx = " + dx
						+ " dy = " + dy);
				if (childW > W) {
					int scrollx = getScrollX();
					if ((dx < 0 && scrollx + W >= childW)
							|| (dx > 0 && scrollx <= 0)) {
						Log.d(TAG, "HSV Wider: on edge already");
						no_scrolling = true;
						return false;
					} else {
						Log.d(TAG, "HSV Wider: can scroll");
						no_scrolling = false;
					}
				} else {
					Log.d(TAG, "HSV cannot scroll in desired direction");
					no_scrolling = true;
				}
			}
		}

		// Standard behavior
		//
		return super.onInterceptTouchEvent(ev);
	}

}
