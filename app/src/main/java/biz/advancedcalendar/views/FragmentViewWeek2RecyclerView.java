package biz.advancedcalendar.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import biz.advancedcalendar.fragments.FragmentViewWeek2;
import java.util.ArrayList;
import java.util.List;

public class FragmentViewWeek2RecyclerView extends RecyclerView {
	private static final float MIN_PAGING_VELOCITY_DIP_PER_SEC = 100;
	private static final float MAX_PAGING_VELOCITY_DIP_PER_SEC = 1000;
	private Integer mLastRegisteredFirstVisibleItemPositionOnDown;
	private float mScaledMinPagingVelocity;
	private float mScaledMaxPagingVelocity;
	private List<MotionEvent> mMotionEvents = new ArrayList<MotionEvent>();
	private boolean mIsMoving;
	private int mTargetHeadItemPosition;
	private int mDaysCount;
	private int mSmoothScrollingToSpecificPositionDirection;
	private boolean mIsSmoothScrollingToSpecificPosition;
	private LinearLayoutManager mLinearLayoutManager;
	private float mXcoordOnDown;
	private float mTouchSlop;

	public int getDaysCount() {
		return mDaysCount;
	}

	public void setDaysCount(int daysCount) {
		mDaysCount = daysCount;
	}

	public FragmentViewWeek2RecyclerView(Context context) {
		super(context);
		init();
	}

	public FragmentViewWeek2RecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FragmentViewWeek2RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mDaysCount = 1;
		final Resources resources = getResources();
		float mDensity = resources.getDisplayMetrics().density;
		mScaledMinPagingVelocity = mDensity * FragmentViewWeek2RecyclerView.MIN_PAGING_VELOCITY_DIP_PER_SEC;
		mScaledMaxPagingVelocity = mDensity * FragmentViewWeek2RecyclerView.MAX_PAGING_VELOCITY_DIP_PER_SEC;
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if (FragmentViewWeek2.SCROLL_DEBUG) {
					Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "onScrolled: dx: " + dx);
				}
				if (mIsSmoothScrollingToSpecificPosition) {
					int targetItemPosition;
					if (mSmoothScrollingToSpecificPositionDirection == -1) {
						targetItemPosition = mTargetHeadItemPosition;
					} else {
						targetItemPosition = mTargetHeadItemPosition + mDaysCount - 1;
					}
					int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
					if (firstVisibleItemPosition != -1) {
						if (mSmoothScrollingToSpecificPositionDirection == -1) {
							if (firstVisibleItemPosition < targetItemPosition) {
								if (FragmentViewWeek2.SCROLL_DEBUG) {
									Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
											"Smoothscrolled past targetItemPosition: " + targetItemPosition);
									Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
											"   firstVisibleItemPosition: " + firstVisibleItemPosition);
									Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
											"   scrollPositionToFirst: " + mTargetHeadItemPosition);
								}
								scrollPositionToFirst(mTargetHeadItemPosition, firstVisibleItemPosition);
								mIsSmoothScrollingToSpecificPosition = false;
							}
						} else {
							int lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
							if (targetItemPosition < lastVisibleItemPosition) {
								if (FragmentViewWeek2.SCROLL_DEBUG) {
									Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
											"Smoothscrolled past targetItemPosition: " + targetItemPosition);
									Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
											"   lastVisibleItemPosition: " + lastVisibleItemPosition);
									Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
											"   scrollPositionToFirst: " + mTargetHeadItemPosition);
								}
								scrollPositionToFirst(mTargetHeadItemPosition, firstVisibleItemPosition);
								mIsSmoothScrollingToSpecificPosition = false;
							}
						}
					}
				}
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (FragmentViewWeek2.SCROLL_DEBUG) {
					Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "onScrollStateChanged: ");
					Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "   newState: " + newState);
					Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
							"   mIsSmoothScrollingToSpecificPositionAfterFling: "
									+ mIsSmoothScrollingToSpecificPosition);
				}
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					// int firstVisibleItemPosition = mLinearLayoutManager
					// .findFirstVisibleItemPosition();
					// if (FragmentViewWeek2.SCROLL_DEBUG) {
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " firstVisibleItemPosition: "
					// + firstVisibleItemPosition);
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " mTargetHeadItemPosition: " + mTargetHeadItemPosition);
					// }
					// if (mIsSmoothScrollingToSpecificPositionAfterFling) {
					// boolean haveToScroll = false;
					// if (firstVisibleItemPosition != mTargetHeadItemPosition)
					// {
					// if (FragmentViewWeek2.SCROLL_DEBUG) {
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " firstVisibleItemPosition: "
					// + firstVisibleItemPosition);
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " mTargetHeadItemPosition: "
					// + mTargetHeadItemPosition);
					// }
					// haveToScroll = true;
					// } else {
					// DayViewHolder DayViewHolder = (DayViewHolder)
					// findViewHolderForLayoutPosition(mTargetHeadItemPosition);
					// if (FragmentViewWeek2.SCROLL_DEBUG) {
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " DayViewHolder.itemView.getLeft(): "
					// + DayViewHolder.itemView.getLeft());
					// }
					// if (DayViewHolder.itemView.getLeft() != 0) {
					// haveToScroll = true;
					// }
					// }
					// if (FragmentViewWeek2.SCROLL_DEBUG) {
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " haveToScroll: " + haveToScroll);
					// }
					// if (haveToScroll) {
					// if (FragmentViewWeek2.SCROLL_DEBUG) {
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// "onScrollStateChanged: ");
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " scrollPositionToFirst: "
					// + mTargetHeadItemPosition);
					// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					// " firstVisibleItemPosition: "
					// + firstVisibleItemPosition);
					// }
					// // scrollPositionToFirst(mTargetHeadItemPosition,
					// // firstVisibleItemPosition);
					// }
					// }
					mIsSmoothScrollingToSpecificPosition = false;
				}
			}
		});
	}

	@Override
	public void setLayoutManager(LayoutManager layoutManager) {
		if (!(layoutManager instanceof LinearLayoutManager)) {
			throw new IllegalArgumentException("The LayoutManager must be instanceof LinearLayoutManager");
		}
		mLinearLayoutManager = (LinearLayoutManager) layoutManager;
		super.setLayoutManager(layoutManager);
	}

	@Override
	public void onChildAttachedToWindow(View child) {
		View firstChild = getChildAt(0);
		int childCount = getChildCount();
		int totalWidth = 0;
		float floatWidth;
		int width = getWidth();
		floatWidth = width / (float) mDaysCount;
		int intWidth;
		//
		if (firstChild == child) {
			if (childCount < mDaysCount) {
				for (int i = 1; i < childCount; i++) {
					View nextChild = getChildAt(i);
					totalWidth += nextChild.getWidth();
				}
				intWidth = (int) (floatWidth * childCount - totalWidth);
			} else {
				for (int i = 1; i < mDaysCount; i++) {
					View nextChild = getChildAt(i);
					totalWidth += nextChild.getWidth();
				}
				intWidth = width - totalWidth;
			}
		} else {
			if (childCount < mDaysCount) {
				for (int i = 0; i < childCount - 1; i++) {
					View nextChild = getChildAt(i);
					totalWidth += nextChild.getWidth();
				}
				intWidth = (int) (floatWidth * childCount - totalWidth);
			} else {
				for (int i = childCount - mDaysCount; i < childCount - 1; i++) {
					View nextChild = getChildAt(i);
					totalWidth += nextChild.getWidth();
				}
				intWidth = width - totalWidth;
			}
		}
		RecyclerView.LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
		layoutParams.height = getHeight();
		layoutParams.width = intWidth;
		child.setLayoutParams(layoutParams);
		super.onChildAttachedToWindow(child);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (FragmentViewWeek2.SCROLL_DEBUG) {
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "onInterceptTouchEvent: ");
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					"    event: " + event.getX() + " " + event.getEventTime() + " " + event.getAction());
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mXcoordOnDown = event.getX();
			LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
			mLastRegisteredFirstVisibleItemPositionOnDown = layoutManager.findFirstVisibleItemPosition();
			View v = layoutManager.getChildAt(0);
			if (v.getRight() < v.getWidth() / 2) {
				mLastRegisteredFirstVisibleItemPositionOnDown++;
			}
			mIsMoving = false;
			mMotionEvents.add(MotionEvent.obtain(event));
			return super.onInterceptTouchEvent(event);
		case MotionEvent.ACTION_MOVE:
			float deltaX = mXcoordOnDown - event.getX();
			float distance = Math.abs(deltaX);
			if (distance > mTouchSlop) {
				mIsMoving = true;
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
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
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
			}
			boolean result = super.onInterceptTouchEvent(event);
			if (result) {
				mIsMoving = true;
			}
			return result;
		case MotionEvent.ACTION_UP:
			if (!mIsMoving) {
				return super.onInterceptTouchEvent(event);
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
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
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
			}
			MotionEvent motionEvent1 = mMotionEvents.get(0);
			MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
			deltaX = motionEvent2.getX() - motionEvent1.getX();
			long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
			float velocityX = deltaX / deltaTime * 1000;
			//
			float absVelocityX = Math.abs(velocityX);
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaX: " + deltaX);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    velocityX: " + velocityX);
			}
			MotionEvent event2 = MotionEvent.obtain(event);
			event2.setAction(MotionEvent.ACTION_CANCEL);
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    super.onInterceptTouchEvent(event2): ");
			super.onInterceptTouchEvent(event2);
			//
			if (mScaledMinPagingVelocity <= absVelocityX) {
				int pagesCount = (int) (absVelocityX / mScaledMaxPagingVelocity + 1);
				scrollPages(deltaX > 0 ? -pagesCount : deltaX < 0 ? pagesCount : 0);
			}
			mMotionEvents.clear();
			mIsMoving = false;
			return true;
		case MotionEvent.ACTION_CANCEL:
			mIsMoving = false;
			mMotionEvents.clear();
			return super.onInterceptTouchEvent(event);
		}
		return super.onInterceptTouchEvent(event);
	}

	private void scrollPages(int pagesCount) {
		if (FragmentViewWeek2.SCROLL_DEBUG) {
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "scrollPages: ");
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    pagesCount: " + pagesCount);
		}
		if (pagesCount == 0) {
			return;
		}
		if (FragmentViewWeek2.SCROLL_DEBUG) {
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    lastRegisteredFirstVisibleItemPositionOnDown:"
					+ mLastRegisteredFirstVisibleItemPositionOnDown);
		}
		if (/* firstVisibleItemPosition != -1 */mLastRegisteredFirstVisibleItemPositionOnDown != null
				&& mLastRegisteredFirstVisibleItemPositionOnDown != -1) {
			mTargetHeadItemPosition = mLastRegisteredFirstVisibleItemPositionOnDown + mDaysCount * pagesCount;
			mSmoothScrollingToSpecificPositionDirection = pagesCount < 0 ? -1 : 1;
			//
			int targetItemPosition;
			if (mSmoothScrollingToSpecificPositionDirection == -1) {
				targetItemPosition = mTargetHeadItemPosition;
			} else {
				targetItemPosition = mTargetHeadItemPosition + mDaysCount - 1;
			}
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
						"    mTargetHeadItemPosition: " + mTargetHeadItemPosition);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
						"    mSmoothScrollingToSpecificPositionAfterFlingDirection: "
								+ mSmoothScrollingToSpecificPositionDirection);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    smoothScrollToPosition: " + targetItemPosition);
			}
			smoothScrollToPosition(targetItemPosition);
			mIsSmoothScrollingToSpecificPosition = true;
			//
			// mRecyclerView.postDelayed(new Runnable() {
			// @Override
			// public void run() {
			// int targetItemPosition;
			// if (mSmoothScrollingToSpecificPositionAfterFlingDirection == -1)
			// {
			// targetItemPosition = mTargetHeadItemPosition;
			// } else {
			// targetItemPosition = mTargetHeadItemPosition
			// + mStateParameters.DaysCount - 1;
			// }
			// if (FragmentViewWeek2.SCROLL_DEBUG) {
			// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
			// "postDelayed(): mTargetHeadItemPosition: "
			// + mTargetHeadItemPosition);
			// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
			// " mSmoothScrollingToSpecificPositionAfterFlingDirection: "
			// + mSmoothScrollingToSpecificPositionAfterFlingDirection);
			// Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
			// " smoothScrollToPosition: " + targetItemPosition);
			// }
			// mRecyclerView.smoothScrollToPosition(targetItemPosition);
			// mIsSmoothScrollingToSpecificPositionAfterFling = true;
			// }
			// }, 0);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (FragmentViewWeek2.SCROLL_DEBUG) {
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "onTouchEvent: ");
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug,
					"    event: " + event.getX() + " " + event.getEventTime() + " " + event.getAction());
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsMoving = false;
			mMotionEvents.add(MotionEvent.obtain(event));
			return super.onTouchEvent(event);
		case MotionEvent.ACTION_MOVE:
			float deltaX = mXcoordOnDown - event.getX();
			float distance = Math.abs(deltaX);
			if (distance > mTouchSlop) {
				mIsMoving = true;
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
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
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
			}
			boolean result = super.onTouchEvent(event);
			return result;
		case MotionEvent.ACTION_UP:
			if (!mIsMoving) {
				return super.onTouchEvent(event);
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
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
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
			}
			MotionEvent motionEvent1 = mMotionEvents.get(0);
			MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
			deltaX = motionEvent2.getX() - motionEvent1.getX();
			long deltaTime = motionEvent2.getEventTime() - motionEvent1.getEventTime();
			float velocityX = deltaX / deltaTime * 1000;
			//
			float absVelocityX = Math.abs(velocityX);
			if (FragmentViewWeek2.SCROLL_DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    motionEvents: " + motionEventsString);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaX: " + deltaX);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    deltaTime: " + deltaTime);
				Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "    velocityX: " + velocityX);
			}
			MotionEvent event2 = MotionEvent.obtain(event);
			event2.setAction(MotionEvent.ACTION_CANCEL);
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    super.onTouchEvent(event2): ");
			super.onTouchEvent(event2);
			//
			if (mScaledMinPagingVelocity <= absVelocityX) {
				int pagesCount = (int) (absVelocityX / mScaledMaxPagingVelocity + 1);
				scrollPages(deltaX > 0 ? -pagesCount : deltaX < 0 ? pagesCount : 0);
			}
			mMotionEvents.clear();
			mIsMoving = false;
			return true;
		case MotionEvent.ACTION_CANCEL:
			mIsMoving = false;
			mMotionEvents.clear();
			return super.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public void scrollPositionToFirst(final int position, Integer firstVisibleItemPositionArg) {
		int firstVisibleItemPosition;
		if (firstVisibleItemPositionArg == null) {
			firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
		} else {
			firstVisibleItemPosition = firstVisibleItemPositionArg;
		}
		int mTargetItemPosition;
		if (firstVisibleItemPosition != -1 && firstVisibleItemPosition < position) {
			mTargetItemPosition = position + mDaysCount - 1;
		} else {
			mTargetItemPosition = position;
		}
		if (FragmentViewWeek2.SCROLL_DEBUG) {
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "scrollPositionToFirst: ");
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "   position: " + position);
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "   mTargetItemPosition: " + mTargetItemPosition);
			Log.d(FragmentViewWeek2.FragmentViewWeek2Debug, "   scrollToPosition: " + mTargetItemPosition);
		}
		scrollToPosition(mTargetItemPosition);
	}

	public void smoothScrollPositionToFirst(final int position, Integer firstVisibleItemPositionArg) {
		int firstVisibleItemPosition;
		if (firstVisibleItemPositionArg == null) {
			firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
		} else {
			firstVisibleItemPosition = firstVisibleItemPositionArg;
		}
		mTargetHeadItemPosition = position;
		int mTargetItemPosition;
		if (firstVisibleItemPosition != -1 && firstVisibleItemPosition < position) {
			mTargetItemPosition = position + mDaysCount - 1;
		} else {
			mTargetItemPosition = position;
		}
		mSmoothScrollingToSpecificPositionDirection = firstVisibleItemPosition < position ? 1 : -1;
		smoothScrollToPosition(mTargetItemPosition);
		mIsSmoothScrollingToSpecificPosition = true;
	}

	public void setScaledPagingVelocity(float scaledMinPagingVelocity, float scaledMaxPagingVelocity) {
		mScaledMinPagingVelocity = scaledMinPagingVelocity;
		mScaledMaxPagingVelocity = scaledMaxPagingVelocity;
	}
}