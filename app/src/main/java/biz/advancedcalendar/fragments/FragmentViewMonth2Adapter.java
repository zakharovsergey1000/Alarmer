package biz.advancedcalendar.fragments;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import biz.advancedcalendar.activities.accessories.InformationUnitMatrix;
import biz.advancedcalendar.fragments.RetainedFragmentForFragmentViewMonth2.CalculateAndDistributeTaskOccurrencesAsyncTaskArgument;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.MarkSyncNeededPolicy;
import biz.advancedcalendar.greendao.Task.SyncPolicy;
import biz.advancedcalendar.utils.CalendarHelper;
import biz.advancedcalendar.utils.Helper;
import biz.advancedcalendar.views.DayLongClickedListener;
import biz.advancedcalendar.views.DaysSelectedListener;
import biz.advancedcalendar.views.MonthWeekView;
import biz.advancedcalendar.views.TaskClickedListener;
import biz.advancedcalendar.views.TaskOccurrencesDistribution;
import biz.advancedcalendar.views.TaskOccurrencesDistributionState;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class FragmentViewMonth2Adapter extends
		RecyclerView.Adapter<FragmentViewMonth2Adapter.MonthWeekViewHolder> {
	private static final int HORIZONTAL_SPACER_DIP = 1;
	private final FragmentViewMonth2 mFragmentViewMonth2;
	private int mHorizontalSpacerPx;

	public class MonthWeekViewHolder extends RecyclerView.ViewHolder {
		MonthWeekView mWeekView;
		ViewGroup mParent;
		LayoutParams mLayoutParams;
		TaskOccurrencesDistribution mTaskOccurrencesDistribution;

		MonthWeekViewHolder(View itemView, ViewGroup parent,
				TaskClickedListener taskClickedListener,
				DaysSelectedListener dayClickedListener,
				DayLongClickedListener dayLongClickedListener) {
			super(itemView);
			mWeekView = (MonthWeekView) itemView;
			mWeekView.setTaskClickedListener(taskClickedListener);
			mWeekView.setDayClickedListener(dayClickedListener);
			mWeekView.setDayLongClickedListener(dayLongClickedListener);
			mParent = parent;
			mLayoutParams = new LayoutParams(0, 0);
			mTaskOccurrencesDistribution = new TaskOccurrencesDistribution(1);
		}

		public long getPrevailingMonth() {
			return mWeekView.getPrevailingMonth();
		}

		public long getFirstMonth() {
			return mWeekView.getFirstMonth();
		}
	}

	private List<Task> mTasks;
	public static final int VIRTUAL_MIDDLE_OFFSET = Integer.MAX_VALUE / 2;
	public static final String FragmentViewWeek2Debug = "FragmentViewWeek2Debug";
	public static final String FragmentViewWeek2Debug2 = "FragmentViewWeek2Debug2";
	List<TaskOccurrencesDistribution> mCacheOfTaskOccurrencesDistributions = new ArrayList<TaskOccurrencesDistribution>();
	private TreeMap<Integer, Integer> mCachedItemsHeights = new TreeMap<Integer, Integer>();
	// private TreeMap<Integer, Integer> mCachedItemsWidthsForListMode = new
	// TreeMap<Integer, Integer>();
	private TaskOccurrencesDistributionState mAssignedTaskOccurrencesDistributionState;
	protected int visibleThreshold = 5;
	protected int startPurgeThreshold = visibleThreshold * 4;
	private RetainedFragmentForFragmentViewMonth2 mRetainedFragment;
	private TaskClickedListener mTaskClickedListener;
	private DaysSelectedListener mDayClickedListener;
	private DayLongClickedListener mDayLongClickedListener;
	// private WeekViewDayHeaderClickedListener mWeekViewDayHeaderClickedListener;
	// private Object mLoadGroupAsyncTask;
	private boolean mMarkSyncNeeded;
	private float mDayNumberTextSize;
	private int mMode;
	private InformationUnitMatrix informationUnitMatrix;
	private long mCurrentMonth;

	// private List<Integer> mDayHeadersCoords;
	// public void setLoadGroupAsyncTask(Object loadGroupAsyncTask) {
	// if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
	// Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
	// "setLoadGroupAsyncTask thread: %s", Thread.currentThread().getName()));
	// Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
	// "    mLoadGroupAsyncTask before: %s", mLoadGroupAsyncTask));
	// }
	// mLoadGroupAsyncTask = loadGroupAsyncTask;
	// if (FragmentViewAgenda.LoadGroupAsyncTaskDebug) {
	// Log.d(FragmentViewAgenda.LoadGroupAsyncTaskDebugTag, String.format(
	// "    mLoadGroupAsyncTask after: %s", mLoadGroupAsyncTask));
	// }
	// }
	public FragmentViewMonth2Adapter(FragmentViewMonth2 fragmentViewWeek2,
			RetainedFragmentForFragmentViewMonth2 retainedFragmentForFragmentViewWeek2,
			TaskClickedListener taskClickedListener,
			DaysSelectedListener dayClickedListener,
			DayLongClickedListener dayLongClickedListener) {
		mFragmentViewMonth2 = fragmentViewWeek2;
		mRetainedFragment = retainedFragmentForFragmentViewWeek2;
		mTaskClickedListener = taskClickedListener;
		mDayClickedListener = dayClickedListener;
		mDayLongClickedListener = dayLongClickedListener;
		mHorizontalSpacerPx = Math.max(
				1,
				Math.round(fragmentViewWeek2.getResources().getDisplayMetrics().density
						* FragmentViewMonth2Adapter.HORIZONTAL_SPACER_DIP));
	}

	public synchronized void invalidateTasks() {
		mTasks = null;
		mRetainedFragment.invalidateTasks();
		notifyDataSetChanged();
	}

	public synchronized void invalidateTaskOccurrencesDistributions() {
		mRetainedFragment.invalidateTaskOccurrencesDistributions();
		notifyDataSetChanged();
	}

	public TaskOccurrencesDistributionState calculateReconciledTaskOccurrencesDistributionState() {
		TaskOccurrencesDistributionState reconciledTaskOccurrencesDistributionState = new TaskOccurrencesDistributionState();
		int firstVisibleItemPosition = mFragmentViewMonth2.mLinearLayoutManager
				.findFirstVisibleItemPosition();
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
					"calculateReconciledTaskOccurrencesDistributionState."
							+ "\t firstVisibleItemPosition: " + firstVisibleItemPosition);
		}
		if (firstVisibleItemPosition == -1) {
			if (FragmentViewWeek2.DEBUG) {
				Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
						"calculateReconciledTaskOccurrencesDistributionState."
								+ "\t return: "
								+ reconciledTaskOccurrencesDistributionState);
			}
			return reconciledTaskOccurrencesDistributionState;
		}
		int lastVisibleItemPosition = mFragmentViewMonth2.mLinearLayoutManager
				.findLastVisibleItemPosition();
		//
		// Reconcile TaskOccurrencesDistributionState from all fragments into the
		// taskOccurrencesDistributionState
		for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition
				&& !(reconciledTaskOccurrencesDistributionState.AllDayTasksExist
						&& reconciledTaskOccurrencesDistributionState.PreBusinessHoursTasksExist && reconciledTaskOccurrencesDistributionState.PostBusinessHoursTasksExist); i++) {
			TaskOccurrencesDistribution taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
					.get(i);
			if (taskOccurrencesDistribution == null) {
				// taskOccurrencesDistribution = new TaskOccurrencesDistribution(1);
				// List<Task> tasks = getTasks();
				// taskOccurrencesDistribution = mRetainedFragmentForFragmentViewWeek2
				// .calculateAndDistributeTaskOccurrencesForDay(
				// tasks,
				// new DateTime(
				// mFragmentViewWeek2.mFragmentViewWeek2StateParameters.FirstDay)
				// .withMillisOfDay(0)
				// .plusDays(
				// i
				// - FragmentViewWeek2Adapter.VIRTUAL_MIDDLE_OFFSET)
				// .getMillis(), taskOccurrencesDistribution, Helper
				// .getFirstDayOfWeek(mFragmentViewWeek2
				// .getContext()),
				// mFragmentViewWeek2.mFragmentViewWeek2StateParameters);
				// mCachedCalculatedTaskOccurrencesDistributions.put(i,
				// taskOccurrencesDistribution);
				// notifyItemChanged(i);
				// // if (FragmentViewWeek2.DEBUG) {
				// // Log.d(SectionsPagerAdapter.FragmentViewWeek2Debug,
				// // "calculateReconciledTaskOccurrencesDistributionState."
				// // + "\t taskOccurrencesDistribution == null. continue");
				// // }
				continue;
			}
			if (taskOccurrencesDistribution.isAllDayTasksExist()) {
				reconciledTaskOccurrencesDistributionState.AllDayTasksExist = true;
			}
			if (taskOccurrencesDistribution.isPreBusinessHoursTasksExist()) {
				reconciledTaskOccurrencesDistributionState.PreBusinessHoursTasksExist = true;
			}
			if (taskOccurrencesDistribution.isPostBusinessHoursTasksExist()) {
				reconciledTaskOccurrencesDistributionState.PostBusinessHoursTasksExist = true;
			}
		}
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
					"calculateReconciledTaskOccurrencesDistributionState."
							+ "\t return: " + reconciledTaskOccurrencesDistributionState);
		}
		return reconciledTaskOccurrencesDistributionState;
	}

	public int getItemPositionForDateTime(long dateTime) {
		int firstDayOfWeek = Helper.getFirstDayOfWeek(mFragmentViewMonth2.getActivity());
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTimeInMillis(mFragmentViewMonth2.mStateParameters.DateTimeOnVirtualMiddleOffset);
		CalendarHelper.toBeginningOfWeek(calendar1, firstDayOfWeek);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTimeInMillis(dateTime);
		CalendarHelper.toBeginningOfWeek(calendar2, firstDayOfWeek);
		int weeksBetween = CalendarHelper.weeksBetween(calendar1, calendar2);
		return FragmentViewMonth2Adapter.VIRTUAL_MIDDLE_OFFSET + weeksBetween;
	}

	@Override
	public MonthWeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		MonthWeekView weekView = new MonthWeekView(mFragmentViewMonth2.getActivity());
		return new MonthWeekViewHolder(weekView, parent, mTaskClickedListener,
				mDayClickedListener, mDayLongClickedListener);
	}

	@Override
	public void onBindViewHolder(MonthWeekViewHolder holder, int position) {
		//
		Integer height1 = mCachedItemsHeights.get(position);
		if (height1 != null) {
			holder.mLayoutParams.height = height1;
			holder.mLayoutParams.width = holder.mParent.getWidth();
		} else {
			int height;
			Integer firstKey = null;
			if (!mCachedItemsHeights.isEmpty()) {
				firstKey = mCachedItemsHeights.firstKey();
			}
			if (firstKey == null) {
				height = (int) (holder.mParent.getHeight() / 6.0f);
				mCachedItemsHeights.put(position, height);
				holder.mLayoutParams.height = height;
				holder.mLayoutParams.width = holder.mParent.getWidth();
			} else {
				int firstCachedPosition = firstKey;
				int lastCachedPosition = mCachedItemsHeights.lastKey();
				if (position < firstCachedPosition - 6
						|| lastCachedPosition + 6 < position) {
					height = (int) (holder.mParent.getHeight() / 6.0f);
					mCachedItemsHeights.put(position, height);
					holder.mLayoutParams.height = height;
					holder.mLayoutParams.width = holder.mParent.getWidth();
				} else {
					if (position < firstCachedPosition) {
						Iterator<Integer> it = mCachedItemsHeights.keySet().iterator();
						int cachedPosition;
						int accumulatedWidth = 0;
						int accumulatedCount = 0;
						while (it.hasNext()) {
							cachedPosition = it.next();
							if (cachedPosition < position + 6) {
								accumulatedWidth += mCachedItemsHeights
										.get(cachedPosition);
								accumulatedCount++;
								if (accumulatedCount >= 6 - 1) {
									break;
								}
							} else {
								break;
							}
						}
						int remainedCount = 6 - accumulatedCount;
						if (remainedCount == 1) {
							height = holder.mParent.getHeight() - accumulatedWidth;
						} else {
							height = (int) ((holder.mParent.getHeight() - accumulatedWidth) / (float) remainedCount);
						}
						mCachedItemsHeights.put(position, height);
						holder.mLayoutParams.height = height;
						holder.mLayoutParams.width = holder.mParent.getWidth();
					} else if (lastCachedPosition < position) {
						Iterator<Integer> it;
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
							it = mCachedItemsHeights.descendingKeySet().iterator();
						} else {
							TreeMap<Integer, Integer> cachedItemsWidthsForTimeIntervalsModeReversed = new TreeMap<Integer, Integer>(
									Collections.reverseOrder());
							cachedItemsWidthsForTimeIntervalsModeReversed
									.putAll(mCachedItemsHeights);
							it = cachedItemsWidthsForTimeIntervalsModeReversed.keySet()
									.iterator();
						}
						int cachedPosition;
						int accumulatedWidth = 0;
						int accumulatedCount = 0;
						while (it.hasNext()) {
							cachedPosition = it.next();
							if (cachedPosition > position - 6) {
								accumulatedWidth += mCachedItemsHeights
										.get(cachedPosition);
								accumulatedCount++;
								if (accumulatedCount >= 6 - 1) {
									break;
								}
							} else {
								break;
							}
						}
						int remainedCount = 6 - accumulatedCount;
						if (remainedCount == 1) {
							height = holder.mParent.getHeight() - accumulatedWidth;
						} else {
							height = (int) ((holder.mParent.getHeight() - accumulatedWidth) / (float) remainedCount);
						}
						mCachedItemsHeights.put(position, height);
						holder.mLayoutParams.height = height;
						holder.mLayoutParams.width = holder.mParent.getWidth();
					} else {
						Iterator<Integer> it = mCachedItemsHeights.keySet().iterator();
						int cachedPosition;
						int accumulatedWidth = 0;
						int accumulatedCount = 0;
						while (it.hasNext()) {
							cachedPosition = it.next();
							if (cachedPosition > position - 6
									&& cachedPosition < position + 6) {
								accumulatedWidth += mCachedItemsHeights
										.get(cachedPosition);
								accumulatedCount++;
								if (accumulatedCount >= 6 - 1) {
									break;
								}
							} else {
								if (cachedPosition > position + 6) {
									break;
								}
							}
						}
						int remainedCount = 6 - accumulatedCount;
						if (remainedCount == 1) {
							height = holder.mParent.getHeight() - accumulatedWidth;
						} else {
							height = (int) ((holder.mParent.getHeight() - accumulatedWidth) / (float) remainedCount);
						}
						mCachedItemsHeights.put(position, height);
						holder.mLayoutParams.height = height;
						holder.mLayoutParams.width = holder.mParent.getHeight();
					}
				}
			}
		}
		//
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug, "onBindViewHolder."
					+ "\t position: " + position);
		}
		holder.mWeekView.setPosition(position);
		holder.mWeekView.setLayoutParams(holder.mLayoutParams);
		holder.mWeekView.setDayNumberTextSize(mDayNumberTextSize);
		TaskOccurrencesDistributionState assignedTaskOccurrencesDistributionState = getAssignedTaskOccurrencesDistributionState();
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
					"onBindViewHolder setAssignedTaskOccurrencesDistributionState(getAssignedTaskOccurrencesDistributionState())."
							+ "\t getAssignedTaskOccurrencesDistributionState(): "
							+ assignedTaskOccurrencesDistributionState);
		}
		// holder.mWeekView
		// .setAssignedTaskOccurrencesDistributionState(assignedTaskOccurrencesDistributionState);
		// holder.mWeekView.setTaskOccurrencesDistribution(
		// calculateAndDistributeTaskOccurrencesForDay(dayStartDateTime),
		// daysBorders);
		TaskOccurrencesDistribution taskOccurrencesDistribution = mRetainedFragment.mCachedCalculatedTaskOccurrencesDistributions
				.get(position);
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
					"onBindViewHolder setTaskOccurrencesDistribution(mCachedTaskOccurrencesDistributions.get(position)). mCachedTaskOccurrencesDistributions.get(position): "
							+ taskOccurrencesDistribution);
		}
		int firstDayOfWeek = Helper.getFirstDayOfWeek(mFragmentViewMonth2.getActivity());
		long dayStartDateTime = getBeginningOfWeekForPosition(position, firstDayOfWeek);
		long[] daysBorders = new long[7 * 2];
		long dayStartDateTime1 = dayStartDateTime;
		for (int i = 0; i < 7 * 2; i += 2) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(dayStartDateTime1);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			long dayEndDateTime = calendar.getTimeInMillis();
			daysBorders[i] = dayStartDateTime1;
			daysBorders[i + 1] = dayEndDateTime;
			dayStartDateTime1 = dayEndDateTime;
		}
		holder.mWeekView.setTaskOccurrencesDistribution(taskOccurrencesDistribution,
				daysBorders);
		if (taskOccurrencesDistribution == null
				&& !mRetainedFragment.isAsyncTaskRunning()) {
			if (FragmentViewWeek2.DEBUG) {
				Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
						"onBindViewHolder launching AsyncTask.");
			}
			mRetainedFragment
					.launchAsyncTask(new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument[] {new CalculateAndDistributeTaskOccurrencesAsyncTaskArgument(
							position, daysBorders,
							getInstanceOfTaskOccurrencesDistribution(position),
							firstDayOfWeek, getTasks(),
							mFragmentViewMonth2.mStateParameters)});
		} else {
			if (mFragmentViewMonth2.mReconcileRunnable == null) {
				mFragmentViewMonth2.mReconcileRunnable = mFragmentViewMonth2
						.getReconcileRunnable();
				mFragmentViewMonth2.mRecyclerView.postDelayed(
						mFragmentViewMonth2.mReconcileRunnable, 0);
			}
		}
		// holder.mWeekView.setCollapsed(Helper.getBooleanPreferenceValue(
		// mFragmentViewWeek2.getActivity(),
		// R.string.preference_key_fragment_view_week_collapse_expand_state,
		// mFragmentViewWeek2.getResources().getBoolean(
		// R.bool.fragment_view_week_collapse_expand_state)));
		// holder.mWeekView.setCollapsed(true);
		holder.mWeekView
				.setBusinessHoursStartTime(mFragmentViewMonth2.mStateParameters.BusinessHoursStartHour);
		holder.mWeekView
				.setBusinessHoursEndTime(mFragmentViewMonth2.mStateParameters.BusinessHoursEndHour);
		holder.mWeekView.setMarkSyncNeeded(mMarkSyncNeeded);
		holder.mWeekView.setMode(mMode);
		holder.mWeekView.setCurrentMonth(mCurrentMonth);
	}

	long getBeginningOfWeekForPosition(int position, int firstDayOfWeek) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(mFragmentViewMonth2.mStateParameters.DateTimeOnVirtualMiddleOffset);
		calendar.add(Calendar.WEEK_OF_YEAR, position
				- FragmentViewMonth2Adapter.VIRTUAL_MIDDLE_OFFSET);
		CalendarHelper.toBeginningOfWeek(calendar, firstDayOfWeek);
		return calendar.getTimeInMillis();
	}

	TaskOccurrencesDistribution getInstanceOfTaskOccurrencesDistribution(int position) {
		TaskOccurrencesDistribution taskOccurrencesDistribution;
		if (mCacheOfTaskOccurrencesDistributions.size() > 0) {
			taskOccurrencesDistribution = mCacheOfTaskOccurrencesDistributions
					.remove(mCacheOfTaskOccurrencesDistributions.size() - 1);
			taskOccurrencesDistribution.init();
		} else {
			taskOccurrencesDistribution = new TaskOccurrencesDistribution(7);
		}
		return taskOccurrencesDistribution;
		//
		// TaskOccurrencesDistribution taskOccurrencesDistribution;
		// if (mCachedCalculatedTaskOccurrencesDistributions.containsKey(position)) {
		// taskOccurrencesDistribution = mCachedCalculatedTaskOccurrencesDistributions
		// .get(position);
		// taskOccurrencesDistribution.init();
		// } else if (mCachedCalculatedTaskOccurrencesDistributions.size() <
		// startPurgeThreshold) {
		// taskOccurrencesDistribution = new TaskOccurrencesDistribution(1);
		// mCachedCalculatedTaskOccurrencesDistributions.put(position,
		// taskOccurrencesDistribution);
		// } else {
		// int firstKey = mCachedCalculatedTaskOccurrencesDistributions.firstKey();
		// int lastKey = mCachedCalculatedTaskOccurrencesDistributions.lastKey();
		// if (position < firstKey || position - firstKey < lastKey - position) {
		// taskOccurrencesDistribution = mCachedCalculatedTaskOccurrencesDistributions
		// .remove(lastKey);
		// mCachedCalculatedTaskOccurrencesDistributions.put(position,
		// taskOccurrencesDistribution);
		// taskOccurrencesDistribution.init();
		// } else {
		// taskOccurrencesDistribution = mCachedCalculatedTaskOccurrencesDistributions
		// .remove(firstKey);
		// mCachedCalculatedTaskOccurrencesDistributions.put(position,
		// taskOccurrencesDistribution);
		// taskOccurrencesDistribution.init();
		// }
		// }
		// return taskOccurrencesDistribution;
	}

	void purgeTaskOccurrencesDistributionsIfNeeded(
			int firstVisibleItemPosition,
			int lastVisibleItemPosition,
			TreeMap<Integer, TaskOccurrencesDistribution> cachedCalculatedTaskOccurrencesDistributions,
			int visibleThreshold, int startPurgeThreshold,
			List<TaskOccurrencesDistribution> mCacheOfTaskOccurrencesDistributions) {
		if (firstVisibleItemPosition == -1 || lastVisibleItemPosition == -1) {
			return;
		}
		final int visibleItemsCount = lastVisibleItemPosition - firstVisibleItemPosition
				+ 1;
		int mTaskOccurrencesDistributionsSizeBeforePurging = cachedCalculatedTaskOccurrencesDistributions
				.size();
		if (visibleItemsCount > 0
				&& cachedCalculatedTaskOccurrencesDistributions.size()
						- visibleItemsCount > startPurgeThreshold) {
			int countBefore = 0;
			int countAfter = 0;
			Iterator<Integer> it = cachedCalculatedTaskOccurrencesDistributions.keySet()
					.iterator();
			while (it.hasNext()) {
				int virtualGroup = it.next();
				if (virtualGroup < firstVisibleItemPosition) {
					countBefore++;
				} else if (virtualGroup > lastVisibleItemPosition) {
					countAfter++;
				}
			}
			//
			if (countBefore > visibleThreshold) {
				it = cachedCalculatedTaskOccurrencesDistributions.keySet().iterator();
				while (it.hasNext() && countBefore > visibleThreshold
						&& countBefore + countAfter > visibleThreshold * 2) {
					int key = it.next();
					mCacheOfTaskOccurrencesDistributions
							.add(cachedCalculatedTaskOccurrencesDistributions.get(key));
					it.remove();
					countBefore--;
				}
			}
			//
			it = cachedCalculatedTaskOccurrencesDistributions.keySet().iterator();
			for (int i = 0; it.hasNext() && i < visibleThreshold * 2 + visibleItemsCount; i++) {
				it.next();
			}
			while (it.hasNext()) {
				int key = it.next();
				mCacheOfTaskOccurrencesDistributions
						.add(cachedCalculatedTaskOccurrencesDistributions.get(key));
				it.remove();
			}
			if (FragmentViewWeek2.DEBUG) {
				Log.d("FragmentViewWeek2Debug", "purging executed "
						+ mTaskOccurrencesDistributionsSizeBeforePurging + " "
						+ firstVisibleItemPosition + " " + lastVisibleItemPosition + " "
						+ cachedCalculatedTaskOccurrencesDistributions.keySet());
			}
		}
	}

	@Override
	public int getItemCount() {
		// Show virtually infinite number of items.
		return Integer.MAX_VALUE;
	}

	public void setDayNumberTextSize(float size) {
		if (mDayNumberTextSize != size) {
			mDayNumberTextSize = size;
			notifyDataSetChanged();
		}
	}

	public void setMode(int mode) {
		if (mMode != mode) {
			mMode = mode;
			notifyDataSetChanged();
		}
	}

	public void setInformationUnitMatrix(InformationUnitMatrix informationUnitMatrix) {
		if (this.informationUnitMatrix != informationUnitMatrix) {
			this.informationUnitMatrix = informationUnitMatrix;
			notifyDataSetChanged();
		}
	}

	public void setAssignedTaskOccurrencesDistributionState(
			TaskOccurrencesDistributionState taskOccurrencesDistributionState) {
		if (FragmentViewWeek2.DEBUG) {
			Log.d(FragmentViewMonth2Adapter.FragmentViewWeek2Debug,
					"setAssignedTaskOccurrencesDistributionState"
							+ "\t	taskOccurrencesDistributionState"
							+ taskOccurrencesDistributionState.toString());
		}
		mAssignedTaskOccurrencesDistributionState = taskOccurrencesDistributionState;
		notifyDataSetChanged();
	}

	public TaskOccurrencesDistributionState getAssignedTaskOccurrencesDistributionState() {
		// if (mAssignedTaskOccurrencesDistributionState == null) {
		// mAssignedTaskOccurrencesDistributionState =
		// calculateReconciledTaskOccurrencesDistributionState();
		// }
		return mAssignedTaskOccurrencesDistributionState;
	}

	private synchronized List<Task> getTasks() {
		if (mTasks == null) {
			mTasks = mRetainedFragment.getTasks();
		}
		return mTasks;
	}

	public void setMarkSyncParameters(SyncPolicy syncPolicy,
			MarkSyncNeededPolicy markSyncNeededPolicy) {
		boolean markSyncNeeded;
		switch (markSyncNeededPolicy) {
		case ALWAYS:
			markSyncNeeded = true;
			break;
		case IF_SYNC_IS_SWITCHED_ON:
			markSyncNeeded = syncPolicy.equals(SyncPolicy.DO_SYNC);
			break;
		case NEVER:
		default:
			markSyncNeeded = false;
			break;
		}
		mMarkSyncNeeded = markSyncNeeded;
		notifyDataSetChanged();
	}

	public void setCurrentMonth(long currentMonth) {
		if (FragmentViewMonth2.DEBUG5) {
			Log.d(FragmentViewMonth2.FragmentViewMonth2Debug, "setCurrentMonth: ");
			// Log.d(FragmentViewMonth2.FragmentViewMonth2Debug, "    mCurrentMonth: "
			// + new DateTime(mCurrentMonth));
			// Log.d(FragmentViewMonth2.FragmentViewMonth2Debug, "    currentMonth: "
			// + new DateTime(currentMonth));
		}
		if (mCurrentMonth != currentMonth) {
			mCurrentMonth = currentMonth;
			notifyDataSetChanged();
		}
	}

	public List<Integer> getDayHeadersCoords(int width) {
		List<Integer> dayHeadersCoords = new ArrayList<Integer>(7 * 2);
		int childViewLeft = 0;
		int childViewRight = width;
		float dayWidth = (width - mHorizontalSpacerPx * 6) / (float) 7;
		// Prepare childViewRight for the cycle below
		childViewRight = -mHorizontalSpacerPx;
		for (int i = 0; i < 7; i++) {
			childViewLeft = childViewRight + mHorizontalSpacerPx;
			childViewRight = (int) ((dayWidth + mHorizontalSpacerPx) * (i + 1) - mHorizontalSpacerPx);
			if (childViewLeft > width) {
				childViewLeft = width;
			}
			if (childViewRight > width) {
				childViewRight = width;
			}
			dayHeadersCoords.add(childViewLeft);
			dayHeadersCoords.add(childViewRight);
		}
		return dayHeadersCoords;
	}

	public void setDayClickedListener(DaysSelectedListener dayClickedListener) {
		mDayClickedListener = dayClickedListener;
	}

	public void setDayLongClickedListener(DayLongClickedListener dayLongClickedListener) {
		mDayLongClickedListener = dayLongClickedListener;
	}
}