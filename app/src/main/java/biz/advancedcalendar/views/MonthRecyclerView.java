package biz.advancedcalendar.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.fragments.FragmentViewMonth2Adapter;
import biz.advancedcalendar.fragments.FragmentViewMonth2Adapter.MonthWeekViewHolder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MonthRecyclerView extends RecyclerView implements DayLongClickedListener {
	private static final float MIN_PAGING_VELOCITY_DIP_PER_SEC = 100;
	private static final float MAX_PAGING_VELOCITY_DIP_PER_SEC = 1000;
	public static final boolean DEBUG = true;
	public static final String MonthRecyclerViewDebug = "MonthRecyclerViewDebug";
	// private static final int HORIZONTAL_SPACER_DIP = 1;
	private Integer mLastRegisteredFirstVisibleItemPositionOnDown;
	private float mScaledMinPagingVelocity;
	private float mScaledMaxPagingVelocity;
	private List<MotionEvent> mMotionEvents = new ArrayList<MotionEvent>();
	private boolean mIsMoving;
	private int mTargetHeadItemPosition;
	private int mSmoothScrollingToSpecificPositionAfterFlingDirection;
	private boolean mIsSmoothScrollingToSpecificPositionAfterFling;
	private LinearLayoutManager mLinearLayoutManager;
	private boolean mIsSelecting;
	// private MonthDayView mLastRegisteredMonthDayViewOnLongClick;
	private int mMaxDaysCountInWeek;
	// private MotionEvent mLastRegisteredMotionEventOnDown;
	private ArrayList<MonthDayView> mMonthDayViewsForwardMaxList;
	private ArrayList<MonthDayView> mMonthDayViewsBackwardMaxList;
	private int[] mRowColumnOfMonthDayViewOnDown;
	private Long[] mStartAndEndDays = new Long[2];
	private DaysSelectedListener mDaysSelectedListener;
	private FragmentViewMonth2Adapter mAdapter;
	private CurrentMonthChangedListener mCurrentMonthChangedListener;
	protected Integer mLastRegisteredFirstVisibleItemPosition;
	protected int mLastRegisteredLastVisibleItemPosition;
	private Long mLastRegisteredFirstMonth;
	private float mYcoordOnDown;
	private float mTouchSlop;

	public void setCurrentMonthChangedListener(
			CurrentMonthChangedListener currentMonthChangedListener) {
		mCurrentMonthChangedListener = currentMonthChangedListener;
	}

	public interface CurrentMonthChangedListener {
		public void onCurrentMonthChanged(long currentMonth);
	}

	// private int mHorizontalSpacerPx;
	public MonthRecyclerView(Context context) {
		super(context);
		init();
	}

	public MonthRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MonthRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mMaxDaysCountInWeek = getResources().getInteger(
				R.integer.weekview_days_count_max_value);
		final Resources resources = getResources();
		float mDensity = resources.getDisplayMetrics().density;
		mScaledMinPagingVelocity = mDensity
				* MonthRecyclerView.MIN_PAGING_VELOCITY_DIP_PER_SEC;
		mScaledMaxPagingVelocity = mDensity
				* MonthRecyclerView.MAX_PAGING_VELOCITY_DIP_PER_SEC;
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		addOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				if (MonthRecyclerView.DEBUG) {
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "onScrolled: dy: "
							+ dy);
				}
				updateCurrentMonth();
				if (mIsSmoothScrollingToSpecificPositionAfterFling) {
					int targetItemPosition;
					if (mSmoothScrollingToSpecificPositionAfterFlingDirection == -1) {
						targetItemPosition = mTargetHeadItemPosition;
					} else {
						targetItemPosition = mTargetHeadItemPosition + 6 - 1;
					}
					int firstVisibleItemPosition = mLinearLayoutManager
							.findFirstVisibleItemPosition();
					if (firstVisibleItemPosition != -1) {
						if (mSmoothScrollingToSpecificPositionAfterFlingDirection == -1) {
							if (firstVisibleItemPosition < targetItemPosition) {
								if (MonthRecyclerView.DEBUG) {
									Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
											"Smoothscrolled past targetItemPosition: "
													+ targetItemPosition);
									Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
											"   firstVisibleItemPosition: "
													+ firstVisibleItemPosition);
									Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
											"   scrollPositionToFirst: "
													+ mTargetHeadItemPosition);
								}
								scrollPositionToFirst(mTargetHeadItemPosition,
										firstVisibleItemPosition);
								mIsSmoothScrollingToSpecificPositionAfterFling = false;
							}
						} else {
							int lastVisibleItemPosition = mLinearLayoutManager
									.findLastVisibleItemPosition();
							if (targetItemPosition < lastVisibleItemPosition) {
								if (MonthRecyclerView.DEBUG) {
									Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
											"Smoothscrolled past targetItemPosition: "
													+ targetItemPosition);
									Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
											"   lastVisibleItemPosition: "
													+ lastVisibleItemPosition);
									Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
											"   scrollPositionToFirst: "
													+ mTargetHeadItemPosition);
								}
								scrollPositionToFirst(mTargetHeadItemPosition,
										firstVisibleItemPosition);
								mIsSmoothScrollingToSpecificPositionAfterFling = false;
							}
						}
					}
				}
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
				if (MonthRecyclerView.DEBUG) {
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
							"onScrollStateChanged: ");
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "   newState: "
							+ newState);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
							"   mIsSmoothScrollingToSpecificPositionAfterFling: "
									+ mIsSmoothScrollingToSpecificPositionAfterFling);
				}
				if (newState == RecyclerView.SCROLL_STATE_IDLE) {
					mIsSmoothScrollingToSpecificPositionAfterFling = false;
				}
			}
		});
	}

	@Override
	public void setLayoutManager(LayoutManager layoutManager) {
		if (!(layoutManager instanceof LinearLayoutManager)) {
			throw new IllegalArgumentException(
					"The LayoutManager must be instanceof LinearLayoutManager");
		}
		mLinearLayoutManager = (LinearLayoutManager) layoutManager;
		super.setLayoutManager(layoutManager);
	}

	@Override
	public void setAdapter(RecyclerView.Adapter adapter) {
		if (!(adapter instanceof FragmentViewMonth2Adapter)) {
			throw new IllegalArgumentException(
					"The Adapter must be instanceof FragmentViewMonth2Adapter");
		}
		mAdapter = (FragmentViewMonth2Adapter) adapter;
		super.setAdapter(adapter);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int x = Math.round(event.getX());
		int y = Math.round(event.getY());
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					String.format(
							"onInterceptTouchEvent(%s, %s, %s), mIsSelecting: %s",
							"" + x,
							"" + y,
							""
									+ (event.getAction() == MotionEvent.ACTION_DOWN ? "ACTION_DOWN"
											: event.getAction() == MotionEvent.ACTION_MOVE ? "ACTION_MOVE"
													: event.getAction() == MotionEvent.ACTION_UP ? "ACTION_UP"
															: event.getAction()),
							mIsSelecting));
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mYcoordOnDown = event.getY();
			mLastRegisteredFirstVisibleItemPositionOnDown = ((LinearLayoutManager) getLayoutManager())
					.findFirstVisibleItemPosition();
			mIsMoving = false;
			mMotionEvents.add(MotionEvent.obtain(event));
			return super.onInterceptTouchEvent(event);
		case MotionEvent.ACTION_MOVE:
			if (mIsSelecting) {
				// We're currently selecting, so yes, intercept the
				// touch event!
				mMotionEvents.clear();
				return true;
			}
			float deltaY = mYcoordOnDown - event.getY();
			float distance = Math.abs(deltaY);
			if (distance > mTouchSlop) {
				mIsMoving = true;
			}
			mMotionEvents.add(MotionEvent.obtain(event));
			if (MonthRecyclerView.DEBUG) {
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime()
						- motionEvent1.getEventTime();
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
						+ motionEventsString);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
						+ deltaTime);
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
				String motionEventsString = "";
				for (int i = 0; i < mMotionEvents.size(); i++) {
					MotionEvent motionEvent = mMotionEvents.get(i);
					motionEventsString += motionEvent.getEventTime() + " ";
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				long deltaTime = motionEvent2.getEventTime()
						- motionEvent1.getEventTime();
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
						+ motionEventsString);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
						+ deltaTime);
			}
			return mIsMoving = super.onInterceptTouchEvent(event);
		case MotionEvent.ACTION_UP:
			if (mIsMoving) {
				mMotionEvents.add(MotionEvent.obtain(event));
				if (MonthRecyclerView.DEBUG) {
					String motionEventsString = "";
					for (int i = 0; i < mMotionEvents.size(); i++) {
						MotionEvent motionEvent = mMotionEvents.get(i);
						motionEventsString += motionEvent.getEventTime() + " ";
					}
					MotionEvent motionEvent1 = mMotionEvents.get(0);
					MotionEvent motionEvent2 = mMotionEvents
							.get(mMotionEvents.size() - 1);
					long deltaTime = motionEvent2.getEventTime()
							- motionEvent1.getEventTime();
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
							+ motionEventsString);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
							+ deltaTime);
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
					String motionEventsString = "";
					for (int i = 0; i < mMotionEvents.size(); i++) {
						MotionEvent motionEvent = mMotionEvents.get(i);
						motionEventsString += motionEvent.getEventTime() + " ";
					}
					MotionEvent motionEvent1 = mMotionEvents.get(0);
					MotionEvent motionEvent2 = mMotionEvents
							.get(mMotionEvents.size() - 1);
					long deltaTime = motionEvent2.getEventTime()
							- motionEvent1.getEventTime();
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
							+ motionEventsString);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
							+ deltaTime);
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				deltaY = motionEvent2.getY() - motionEvent1.getY();
				long deltaTime = motionEvent2.getEventTime()
						- motionEvent1.getEventTime();
				float velocityY = deltaY / deltaTime * 1000;
				//
				float absVelocityX = Math.abs(velocityY);
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
				MotionEvent event2 = MotionEvent.obtain(event);
				event2.setAction(MotionEvent.ACTION_CANCEL);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    super.onInterceptTouchEvent(event2): ");
				super.onInterceptTouchEvent(event2);
				//
				if (mScaledMinPagingVelocity <= absVelocityX) {
					int firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager())
							.findFirstVisibleItemPosition();
					MonthWeekViewHolder firstVisibleItemPositionViewHolder = (MonthWeekViewHolder) findViewHolderForLayoutPosition(firstVisibleItemPosition);
					long lastRegisteredFirstMonthOnUp = firstVisibleItemPositionViewHolder
							.getFirstMonth();
					int pagesCount = (int) (absVelocityX / mScaledMaxPagingVelocity + 1);
					if (pagesCount != 0) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(lastRegisteredFirstMonthOnUp);
						calendar.add(Calendar.MONTH, deltaY > 0 ? -pagesCount + 1
								: pagesCount);
						smoothScrollToMonth(calendar.getTimeInMillis(),
								firstVisibleItemPosition);
					}
				}
				mMotionEvents.clear();
				mIsMoving = false;
				return true;
			} else if (mIsSelecting) {
				MotionEvent event2 = MotionEvent.obtain(event);
				event2.setAction(MotionEvent.ACTION_CANCEL);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    super.onInterceptTouchEvent(event2): ");
				super.onInterceptTouchEvent(event2);
				//
				// launch weekview with selected days here
				if (mDaysSelectedListener != null) {
					mDaysSelectedListener.onDaysSelected(mStartAndEndDays[0],
							mStartAndEndDays[1]);
				}
				if (mMonthDayViewsForwardMaxList != null) {
					for (MonthDayView monthDayView : mMonthDayViewsForwardMaxList) {
						monthDayView.setSelected(false);
					}
				}
				if (mMonthDayViewsBackwardMaxList != null) {
					for (MonthDayView monthDayView : mMonthDayViewsBackwardMaxList) {
						monthDayView.setSelected(false);
					}
				}
				mIsSelecting = false;
				return true;
			}
		default:
			if (mMonthDayViewsForwardMaxList != null) {
				for (MonthDayView monthDayView : mMonthDayViewsForwardMaxList) {
					monthDayView.setSelected(false);
				}
			}
			if (mMonthDayViewsBackwardMaxList != null) {
				for (MonthDayView monthDayView : mMonthDayViewsBackwardMaxList) {
					monthDayView.setSelected(false);
				}
			}
			mIsSelecting = false;
			mIsMoving = false;
			mMotionEvents.clear();
			return super.onInterceptTouchEvent(event);
		}
	}

	public void smoothScrollToMonth(long month, int firstVisibleItemPosition) {
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "scrollToMonth: ");
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    monthsCount: " + month);
		}
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					"    lastRegisteredFirstVisibleItemPositionOnDown:"
							+ mLastRegisteredFirstVisibleItemPositionOnDown);
		}
		mTargetHeadItemPosition = mAdapter.getItemPositionForDateTime(month);
		mSmoothScrollingToSpecificPositionAfterFlingDirection = mTargetHeadItemPosition <= firstVisibleItemPosition ? -1
				: 1;
		//
		int targetItemPosition;
		if (mSmoothScrollingToSpecificPositionAfterFlingDirection == -1) {
			targetItemPosition = mTargetHeadItemPosition;
		} else {
			targetItemPosition = mTargetHeadItemPosition + 6 - 1;
		}
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					"    mTargetHeadItemPosition: " + mTargetHeadItemPosition);
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					"    mSmoothScrollingToSpecificPositionAfterFlingDirection: "
							+ mSmoothScrollingToSpecificPositionAfterFlingDirection);
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					"    smoothScrollToPosition: " + targetItemPosition);
		}
		smoothScrollToPosition(targetItemPosition);
		mIsSmoothScrollingToSpecificPositionAfterFling = true;
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
					String.format(
							"onTouchEvent(%s, %s, %s), mIsSelecting: %s",
							"" + x,
							"" + y,
							""
									+ (event.getAction() == MotionEvent.ACTION_DOWN ? "ACTION_DOWN"
											: event.getAction() == MotionEvent.ACTION_MOVE ? "ACTION_MOVE"
													: event.getAction() == MotionEvent.ACTION_UP ? "ACTION_UP"
															: event.getAction()),
							mIsSelecting));
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mIsMoving = false;
			mMotionEvents.add(MotionEvent.obtain(event));
			return super.onTouchEvent(event);
		case MotionEvent.ACTION_MOVE:
			if (mIsSelecting) {
				int monthRecyclerViewChildCount = getChildCount();
				for (int i = 0; i < monthRecyclerViewChildCount; i++) {
					MonthWeekView monthWeekView = (MonthWeekView) getChildAt(i);
					if (x > monthWeekView.getLeft() && x < monthWeekView.getRight()
							&& y > monthWeekView.getTop()
							&& y < monthWeekView.getBottom()) {
						// touch is within this MonthWeekView
						int monthWeekViewChildCount = monthWeekView.getChildCount();
						for (int i1 = 0; i1 < monthWeekViewChildCount; i1++) {
							MonthDayView monthDayView = (MonthDayView) monthWeekView
									.getChildAt(i1);
							int x2 = Math.round(x - monthWeekView.getX());
							int y2 = Math.round(y - monthWeekView.getY());
							if (x2 > monthDayView.getLeft()
									&& x2 < monthDayView.getRight()
									&& y2 > monthDayView.getTop()
									&& y2 < monthDayView.getBottom()) {
								// touch is within this MonthDayView
								// MonthDayView lastRegisteredMonthDayViewOnMove =
								// monthDayView;
								int[] mRowAndColumnOfCurrentMonthDayView = (int[]) monthDayView
										.getTag();
								int rowCurrent = mRowAndColumnOfCurrentMonthDayView[0];
								int rowOnDown = mRowColumnOfMonthDayViewOnDown[0];
								int columnCurrent = mRowAndColumnOfCurrentMonthDayView[1];
								int columnOnDown = mRowColumnOfMonthDayViewOnDown[1];
								if (rowCurrent > rowOnDown || rowCurrent == rowOnDown
										&& columnCurrent >= columnOnDown) {
									// forward selection
									int count = (rowCurrent - rowOnDown) * 7
											- columnOnDown + columnCurrent + 1;
									int i2;
									mStartAndEndDays[0] = mMonthDayViewsForwardMaxList
											.get(0).getDayStartDateTime();
									for (i2 = 0; i2 < count
											&& i2 < mMonthDayViewsForwardMaxList.size(); i2++) {
										mMonthDayViewsForwardMaxList.get(i2).setSelected(
												true);
									}
									mStartAndEndDays[1] = mMonthDayViewsForwardMaxList
											.get(i2 - 1).getDayStartDateTime();
									for (; i2 < mMonthDayViewsForwardMaxList.size(); i2++) {
										mMonthDayViewsForwardMaxList.get(i2).setSelected(
												false);
									}
									for (i2 = 1; i2 < mMonthDayViewsBackwardMaxList
											.size(); i2++) {
										mMonthDayViewsBackwardMaxList.get(i2)
												.setSelected(false);
									}
								} else {
									// backward selection
									int count = (rowOnDown - rowCurrent) * 7
											- columnCurrent + columnOnDown + 1;
									mStartAndEndDays[1] = mMonthDayViewsBackwardMaxList
											.get(0).getDayStartDateTime();
									int i2;
									for (i2 = 0; i2 < count
											&& i2 < mMonthDayViewsBackwardMaxList.size(); i2++) {
										mMonthDayViewsBackwardMaxList.get(i2)
												.setSelected(true);
									}
									mStartAndEndDays[0] = mMonthDayViewsBackwardMaxList
											.get(i2 - 1).getDayStartDateTime();
									for (; i2 < mMonthDayViewsBackwardMaxList.size(); i2++) {
										mMonthDayViewsBackwardMaxList.get(i2)
												.setSelected(false);
									}
									for (i2 = 1; i2 < mMonthDayViewsForwardMaxList.size(); i2++) {
										mMonthDayViewsForwardMaxList.get(i2).setSelected(
												false);
									}
								}
								break;
							}
						}
						break;
					}
				}
				return true;
			} else {
				float deltaY = mYcoordOnDown - event.getY();
				float distance = Math.abs(deltaY);
				if (distance > mTouchSlop) {
					mIsMoving = true;
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
				return super.onTouchEvent(event);
			}
		case MotionEvent.ACTION_UP:
			if (mIsMoving) {
				mMotionEvents.add(MotionEvent.obtain(event));
				if (MonthRecyclerView.DEBUG) {
					String motionEventsString = "";
					for (int i = 0; i < mMotionEvents.size(); i++) {
						MotionEvent motionEvent = mMotionEvents.get(i);
						motionEventsString += motionEvent.getEventTime() + " ";
					}
					MotionEvent motionEvent1 = mMotionEvents.get(0);
					MotionEvent motionEvent2 = mMotionEvents
							.get(mMotionEvents.size() - 1);
					long deltaTime = motionEvent2.getEventTime()
							- motionEvent1.getEventTime();
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
							+ motionEventsString);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
							+ deltaTime);
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
					String motionEventsString = "";
					for (int i = 0; i < mMotionEvents.size(); i++) {
						MotionEvent motionEvent = mMotionEvents.get(i);
						motionEventsString += motionEvent.getEventTime() + " ";
					}
					MotionEvent motionEvent1 = mMotionEvents.get(0);
					MotionEvent motionEvent2 = mMotionEvents
							.get(mMotionEvents.size() - 1);
					long deltaTime = motionEvent2.getEventTime()
							- motionEvent1.getEventTime();
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    motionEvents: "
							+ motionEventsString);
					Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "    deltaTime: "
							+ deltaTime);
				}
				MotionEvent motionEvent1 = mMotionEvents.get(0);
				MotionEvent motionEvent2 = mMotionEvents.get(mMotionEvents.size() - 1);
				float deltaY = motionEvent2.getY() - motionEvent1.getY();
				long deltaTime = motionEvent2.getEventTime()
						- motionEvent1.getEventTime();
				float velocityY = deltaY / deltaTime * 1000;
				//
				float absVelocityX = Math.abs(velocityY);
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
				MotionEvent event2 = MotionEvent.obtain(event);
				event2.setAction(MotionEvent.ACTION_CANCEL);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    super.onTouchEvent(event2): ");
				super.onTouchEvent(event2);
				//
				if (mScaledMinPagingVelocity <= absVelocityX) {
					int firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager())
							.findFirstVisibleItemPosition();
					MonthWeekViewHolder firstVisibleItemPositionViewHolder = (MonthWeekViewHolder) findViewHolderForLayoutPosition(firstVisibleItemPosition);
					long lastRegisteredFirstMonthOnUp = firstVisibleItemPositionViewHolder
							.getFirstMonth();
					int pagesCount = (int) (absVelocityX / mScaledMaxPagingVelocity + 1);
					if (pagesCount != 0) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(lastRegisteredFirstMonthOnUp);
						calendar.add(Calendar.MONTH, deltaY > 0 ? -pagesCount + 1
								: pagesCount);
						smoothScrollToMonth(calendar.getTimeInMillis(),
								firstVisibleItemPosition);
					}
				}
				mMotionEvents.clear();
				mIsMoving = false;
				return true;
			} else if (mIsSelecting) {
				MotionEvent event2 = MotionEvent.obtain(event);
				event2.setAction(MotionEvent.ACTION_CANCEL);
				Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
						"    super.onTouchEvent(event2): ");
				super.onTouchEvent(event2);
				//
				// launch weekview with selected days here
				if (mDaysSelectedListener != null) {
					mDaysSelectedListener.onDaysSelected(mStartAndEndDays[0],
							mStartAndEndDays[1]);
				}
				if (mMonthDayViewsForwardMaxList != null) {
					for (MonthDayView monthDayView : mMonthDayViewsForwardMaxList) {
						monthDayView.setSelected(false);
					}
				}
				if (mMonthDayViewsBackwardMaxList != null) {
					for (MonthDayView monthDayView : mMonthDayViewsBackwardMaxList) {
						monthDayView.setSelected(false);
					}
				}
				mIsSelecting = false;
				return true;
			} else {
				return super.onTouchEvent(event);
			}
		default:
			if (mMonthDayViewsForwardMaxList != null) {
				for (MonthDayView monthDayView : mMonthDayViewsForwardMaxList) {
					monthDayView.setSelected(false);
				}
			}
			if (mMonthDayViewsBackwardMaxList != null) {
				for (MonthDayView monthDayView : mMonthDayViewsBackwardMaxList) {
					monthDayView.setSelected(false);
				}
			}
			mIsSelecting = false;
			mIsMoving = false;
			mMotionEvents.clear();
			return super.onTouchEvent(event);
		}
	}

	public void scrollPositionToFirst(final int position,
			Integer firstVisibleItemPositionArg) {
		int firstVisibleItemPosition;
		if (firstVisibleItemPositionArg == null) {
			firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager())
					.findFirstVisibleItemPosition();
		} else {
			firstVisibleItemPosition = firstVisibleItemPositionArg;
		}
		int mTargetItemPosition;
		if (firstVisibleItemPosition != -1 && firstVisibleItemPosition < position) {
			mTargetItemPosition = position + 6 - 1;
		} else {
			mTargetItemPosition = position;
		}
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "scrollPositionToFirst: ");
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "   position: " + position);
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "   mTargetItemPosition: "
					+ mTargetItemPosition);
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug, "   scrollToPosition: "
					+ mTargetItemPosition);
		}
		scrollToPosition(mTargetItemPosition);
	}

	public void setScaledPagingVelocity(float scaledMinPagingVelocity,
			float scaledMaxPagingVelocity) {
		mScaledMinPagingVelocity = scaledMinPagingVelocity;
		mScaledMaxPagingVelocity = scaledMaxPagingVelocity;
	}

	private void updateCurrentMonth() {
		int firstVisibleItemPosition = mLinearLayoutManager
				.findFirstVisibleItemPosition();
		int lastVisibleItemPosition = firstVisibleItemPosition;
		if (firstVisibleItemPosition != -1) {
			lastVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition();
		}
		if (mLastRegisteredFirstVisibleItemPosition != null
				&& mLastRegisteredFirstVisibleItemPosition == firstVisibleItemPosition
				&& mLastRegisteredLastVisibleItemPosition == lastVisibleItemPosition) {
			return;
		}
		if (firstVisibleItemPosition != -1) {
			MonthWeekViewHolder middleVisibleItemPositionViewHolder = (MonthWeekViewHolder) findViewHolderForLayoutPosition(firstVisibleItemPosition
					+ (lastVisibleItemPosition - firstVisibleItemPosition) / 2);
			long currentMonth = middleVisibleItemPositionViewHolder.getPrevailingMonth();
			if (mLastRegisteredFirstMonth != null
					&& mLastRegisteredFirstMonth.equals(currentMonth)) {
				return;
			} else {
				mLastRegisteredFirstMonth = currentMonth;
				if (mCurrentMonthChangedListener != null) {
					mCurrentMonthChangedListener.onCurrentMonthChanged(currentMonth);
				}
			}
			//
		} else {
			mLastRegisteredFirstMonth = -1L;
		}
		mLastRegisteredFirstVisibleItemPosition = firstVisibleItemPosition;
		mLastRegisteredLastVisibleItemPosition = lastVisibleItemPosition;
	}

	@Override
	public void onDayLongClicked(long dateTime, MonthDayView monthDayView) {
		if (MonthRecyclerView.DEBUG) {
			Log.d(MonthRecyclerView.MonthRecyclerViewDebug,
					String.format("onDayLongClicked(%s)", "" + dateTime));
		}
		mStartAndEndDays[0] = mStartAndEndDays[1] = monthDayView.getDayStartDateTime();
		if (mMonthDayViewsForwardMaxList == null) {
			mMonthDayViewsForwardMaxList = new ArrayList<MonthDayView>(
					mMaxDaysCountInWeek);
		} else {
			mMonthDayViewsForwardMaxList.clear();
		}
		mRowColumnOfMonthDayViewOnDown = (int[]) monthDayView.getTag();
		LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
		int lastVisibleRow = layoutManager.findLastVisibleItemPosition();
		for (int i = 0, row = mRowColumnOfMonthDayViewOnDown[0]; i < mMaxDaysCountInWeek
				&& row <= lastVisibleRow; row++) {
			MonthWeekView monthWeekView = (MonthWeekView) layoutManager
					.findViewByPosition(row);
			for (int column = row == mRowColumnOfMonthDayViewOnDown[0] ? mRowColumnOfMonthDayViewOnDown[1]
					: 0; i < mMaxDaysCountInWeek && column < 7; i++, column++) {
				mMonthDayViewsForwardMaxList.add((MonthDayView) monthWeekView
						.getChildAt(column));
			}
		}
		if (mMonthDayViewsBackwardMaxList == null) {
			mMonthDayViewsBackwardMaxList = new ArrayList<MonthDayView>(
					mMaxDaysCountInWeek);
		} else {
			mMonthDayViewsBackwardMaxList.clear();
		}
		int firstVisibleRow = layoutManager.findFirstVisibleItemPosition();
		for (int i = 0, row = mRowColumnOfMonthDayViewOnDown[0]; i < mMaxDaysCountInWeek
				&& row >= firstVisibleRow; row--) {
			MonthWeekView monthWeekView = (MonthWeekView) layoutManager
					.findViewByPosition(row);
			for (int column = row == mRowColumnOfMonthDayViewOnDown[0] ? mRowColumnOfMonthDayViewOnDown[1]
					: 6; i < mMaxDaysCountInWeek && column >= 0; i++, column--) {
				mMonthDayViewsBackwardMaxList.add((MonthDayView) monthWeekView
						.getChildAt(column));
			}
		}
		mIsSelecting = true;
	}

	public void setDaysSelectedListener(DaysSelectedListener daysSelectedListener) {
		mDaysSelectedListener = daysSelectedListener;
	}
}