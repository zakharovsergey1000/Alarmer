package biz.advancedcalendar.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import java.util.ArrayList;
import java.util.List;

public class FragmentViewAgendaExpandableListView extends ExpandableListView implements
		android.widget.AbsListView.OnScrollListener {
	private static final float MIN_FLING_STARTING_VELOCITY_DIP_PER_SEC = 100;
	private static final boolean SCROLL_STATE_TOUCH_SCROLL_DEBUG = true;
	private static final String SCROLL_STATE_TOUCH_SCROLL_DEBUG_TAG = "SCROLL_STATE_TOUCH_SCROLL_DEBUG_TAG";
	private float mScaledMinFlingStartingVelocity;
	private List<MotionEvent> mMotionEvents = new ArrayList<MotionEvent>();
	private boolean mIsDragging;
	private OnScrollListener mExternalOnScrollListener;
	private OnScrollListener mInternalOnScrollListener;
	private float mYcoordOnDown;
	private float mTouchSlop;

	// private int mHorizontalSpacerPx;
	public FragmentViewAgendaExpandableListView(Context context) {
		super(context);
		init();
	}

	public FragmentViewAgendaExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FragmentViewAgendaExpandableListView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		final Resources resources = getResources();
		float mDensity = resources.getDisplayMetrics().density;
		mScaledMinFlingStartingVelocity = mDensity
				* FragmentViewAgendaExpandableListView.MIN_FLING_STARTING_VELOCITY_DIP_PER_SEC;
		setOnScrollListener(this);
		super.setOnScrollListener(this);
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		// mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		// mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		// mOverscrollDistance = configuration.getScaledOverscrollDistance();
		// mOverflingDistance = configuration.getScaledOverflingDistance();
		//
		// mDensityScale = getContext().getResources().getDisplayMetrics().density;
	}

	@Override
	public void setOnScrollListener(OnScrollListener onScrollListener) {
		if (onScrollListener != this) {
			mExternalOnScrollListener = onScrollListener;
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int x = Math.round(event.getX());
		int y = Math.round(event.getY());
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					String.format("onInterceptTouchEvent(%s, %s, %s)", "" + x, "" + y, ""
							+ event.getAction()));
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    mIsDragging: "
					+ mIsDragging);
		}
		boolean result;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mYcoordOnDown = event.getY();
			mIsDragging = false;
			mMotionEvents.add(MotionEvent.obtain(event));
			if (MonthRecyclerView.DEBUG) {
				logMotionEvents();
			}
			// boolean result = super.onInterceptTouchEvent(event);
			// if (MonthRecyclerView.DEBUG) {
			// Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
			// "    onInterceptTouchEvent: ");
			// Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
			// "    result = super.onInterceptTouchEvent(event): " + result);
			// Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    return result");
			// }
			// return result;
		default:
		case MotionEvent.ACTION_MOVE:
			if (MonthRecyclerView.DEBUG) {
				logMotionEvents();
			}
			for (int i = 0; i < mMotionEvents.size() - 3; i++) {
				MotionEvent motionEvent = mMotionEvents.get(i);
				if (motionEvent.getEventTime() + 250 < event.getEventTime()) {
					mMotionEvents.remove(i);
					i--;
				} else {
					break;
				}
			}
			if (MonthRecyclerView.DEBUG) {
				logMotionEvents();
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			for (int i = 0; i < mMotionEvents.size() - 3; i++) {
				MotionEvent motionEvent = mMotionEvents.get(i);
				if (motionEvent.getEventTime() + 250 < event.getEventTime()) {
					mMotionEvents.remove(i);
					i--;
				} else {
					break;
				}
			}
			result = super.onInterceptTouchEvent(event);
			if (MonthRecyclerView.DEBUG) {
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    onInterceptTouchEvent: ");
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    result = super.onInterceptTouchEvent(event): " + result);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    return result");
			}
			return result;
			// case MotionEvent.ACTION_UP:
			// if (mIsDragging) {
			// return true;
			// }
			// default:
			// mIsDragging = false;
			// mMotionEvents.clear();
			// result = super.onInterceptTouchEvent(event);
			// if (MonthRecyclerView.DEBUG) {
			// Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
			// "    onInterceptTouchEvent: ");
			// Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
			// "    mMotionEvents.clear(); mIsDragging = false; return false");
			// }
			// return false;
		}
	}

	private void logMotionEvents() {
		String motionEventsString = "";
		for (int i = 0; i < mMotionEvents.size(); i++) {
			MotionEvent motionEvent = mMotionEvents.get(i);
			motionEventsString += motionEvent.getEventTime() + " ";
		}
		MotionEvent motionEvent1 = mMotionEvents.get(0);
		MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
		long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
		Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
				+ motionEventsString);
		Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: " + deltaTime);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = Math.round(event.getX());
		int y = Math.round(event.getY());
		// Here we actually handle the touch event.
		// This method will only be called if the touch event was intercepted in
		// onInterceptTouchEvent
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					String.format("onTouchEvent(%s, %s, %s)", "" + x, "" + y,
							"" + event.getAction()));
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    mIsDragging: "
					+ mIsDragging);
		}
		boolean result;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsDragging = false;
			mMotionEvents.add(MotionEvent.obtain(event));
			result = super.onTouchEvent(event);
			if (MonthRecyclerView.DEBUG) {
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "onTouchEvent: ");
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    result = super.onTouchEvent(event): " + result);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    return result");
			}
			return result;
		case MotionEvent.ACTION_MOVE:
			float deltaY = mYcoordOnDown - event.getY();
			float distance = Math.abs(deltaY);
			if (distance > mTouchSlop) {
				mIsDragging = true;
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			if (MonthRecyclerView.DEBUG) {
				logMotionEvents();
			}
			for (int i = 0; i < mMotionEvents.size() - 3; i++) {
				MotionEvent motionEvent = mMotionEvents.get(i);
				if (motionEvent.getEventTime() + 250 < event.getEventTime()) {
					mMotionEvents.remove(i);
					i--;
				} else {
					break;
				}
			}
			if (MonthRecyclerView.DEBUG) {
				logMotionEvents();
			}
			result = super.onTouchEvent(event);
			if (MonthRecyclerView.DEBUG) {
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    onTouchEvent: ");
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    result = super.onTouchEvent(event): " + result);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    return result");
			}
			return result;
		case MotionEvent.ACTION_UP:
			if (mIsDragging) {
				mMotionEvents.add(MotionEvent.obtain(event));
				if (MonthRecyclerView.DEBUG) {
					logMotionEvents();
				}
				for (int i = 0; i < mMotionEvents.size() - 3; i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					if (motionEvent.getEventTime() + 250 < event.getEventTime()) {
						mMotionEvents.remove(i);
						i--;
					} else {
						break;
					}
				}
				if (MonthRecyclerView.DEBUG) {
					logMotionEvents();
				}
				//
				long historicalEventTime = mMotionEvents.get(0).getEventTime();
				float historicalY = mMotionEvents.get(0).getY();
				label: for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					int historySize = motionEvent.getHistorySize();
					for (int i1 = 0; i1 < historySize; i1++) {
						historicalEventTime = motionEvent.getHistoricalEventTime(i1);
						if (historicalEventTime + 250 >= event.getEventTime()) {
							historicalY = motionEvent.getHistoricalY(i1);
							break label;
						}
					}
				}
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				deltaY = motionEvent2.getY() - historicalY;
				long deltaTime = motionEvent2.getEventTime() - historicalEventTime;
				float velocityY = deltaY / deltaTime * 1000;
				//
				if (MonthRecyclerView.DEBUG) {
					String motionEventsString = "";
					for (int i = 0; i < mMotionEvents.size(); i++) {
						MotionEvent motionEvent = mMotionEvents.get(i);
						motionEventsString += motionEvent.getEventTime() + " ";
					}
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
							+ motionEventsString);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaY: "
							+ deltaY);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
							+ deltaTime);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    velocityY: "
							+ velocityY);
				}
				//
				if (Math.abs(velocityY) > mScaledMinFlingStartingVelocity) {
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
							"    super.onTouchEvent(event): ");
					// Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					// "    smoothScrollBy: " + (int) velocityY);
					super.onTouchEvent(event);
					// smoothScrollBy(-(int) velocityY, (int) velocityY);
				} else {
					MotionEvent event2 = MotionEvent.obtain(event);
					event2.setAction(MotionEvent.ACTION_CANCEL);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
							"    super.onTouchEvent(event2): ");
					super.onTouchEvent(event2);
				}
				mMotionEvents.clear();
				mIsDragging = false;
				return true;
			}
		default:
			// mIsDragging = false;
			mMotionEvents.clear();
			result = super.onTouchEvent(event);
			if (MonthRecyclerView.DEBUG) {
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    onTouchEvent: ");
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    mIsDragging: "
						+ mIsDragging);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    result = super.onTouchEvent(event): " + result);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    return result");
			}
			return result;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// if (MonthRecyclerView.DEBUG) {
		// Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
		// "onScrollStateChanged(), scrollState:" + scrollState);
		// Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    mIsDragging: "
		// + mIsDragging);
		// }
		// if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
		// if (FragmentViewAgendaExpandableListView.SCROLL_STATE_TOUCH_SCROLL_DEBUG) {
		// Log.d(FragmentViewAgendaExpandableListView.SCROLL_STATE_TOUCH_SCROLL_DEBUG_TAG,
		// "    onScrollStateChanged(): SCROLL_STATE_TOUCH_SCROLL");
		// }
		// mIsDragging = true;
		// }
		// if (MonthRecyclerView.DEBUG) {
		// Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    mIsDragging: "
		// + mIsDragging);
		// }
		if (mExternalOnScrollListener != null) {
			mExternalOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (mExternalOnScrollListener != null) {
			mExternalOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount,
					totalItemCount);
		}
	}
}
