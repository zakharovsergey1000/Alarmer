package biz.advancedcalendar.views.accessories;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/** A child class shall subclass this Adapter and implement method getDataRow(int position,
 * View convertView, ViewGroup parent), which supplies a View present data in a ListRow.
 * This parent Adapter takes care of displaying ProgressBar in a row or indicating that it
 * has reached the last row. */
public abstract class EndlessScrollBaseExpandableListAdapter<T1, T2> extends
		BaseExpandableListAdapter implements OnScrollListener, OnGroupExpandListener,
		OnGroupCollapseListener {
	public static final int SCROLL_TO_THE_TOP_OF_THE_LOADED_GROUP = 0;
	public static final int RETAIN_CURRENT_SCROLL_POSITION = 1;
	protected Map<Integer, ExpandListGroup<T1, T2>> mLoadedGroups;
	protected ArrayList<Integer> mExplicitlyCollapsedVirtualGroups;
	protected ScrollPositionData mScrollPositionData;
	protected Context mContext;
	protected int visibleThreshold = 5;
	private int startPurgeThreshold = visibleThreshold * 4;
	// the serverListSize is the total number of items on the server side,
	// which should be returned from the web request results
	protected int serverListSize = -1;
	public static final int VIEW_TYPE_LOADING = 0;
	public static final int VIEW_TYPE_ACTIVITY = 1;
	// empirical value which should be adjusted in the inherited class for every use case
	// of this adapter
	protected static final int AVERAGE_NUMBER_OF_CHILDS_IN_GROUPS = 6;
	public static final int VIRTUAL_MIDDLE_OFFSET = Integer.MAX_VALUE
			/ (EndlessScrollBaseExpandableListAdapter.AVERAGE_NUMBER_OF_CHILDS_IN_GROUPS * 2);
	protected ExpandableListView mExpandableListView;

	public interface EndlessScrollBaseExpandableListAdapterDataHolder<T1, T2> {
		Map<Integer, ExpandListGroup<T1, T2>> getLoadedGroups();

		void setLoadedGroups(Map<Integer, ExpandListGroup<T1, T2>> loadedGroups);

		ArrayList<Integer> getExplicitlyCollapsedVirtualGroups();

		void setExplicitlyCollapsedVirtualGroups(
				ArrayList<Integer> explicitlyCollapsedVirtualGroups);

		ScrollPositionData getScrollPositionData();

		void setScrollPositionData(ScrollPositionData scrollPositionData);
	}

	// private List<Integer> mPreallocatedVirtualGroupPositions;
	@SuppressLint("UseSparseArrays")
	public EndlessScrollBaseExpandableListAdapter(Context context,
			EndlessScrollBaseExpandableListAdapterDataHolder<T1, T2> dataHolder,
			ExpandableListView expandList) {
		this.mContext = context;
		mLoadedGroups = dataHolder.getLoadedGroups();
		mExplicitlyCollapsedVirtualGroups = dataHolder
				.getExplicitlyCollapsedVirtualGroups();
		mScrollPositionData = dataHolder.getScrollPositionData();
		mExpandableListView = expandList;
	}

	@Override
	public T2 getChild(int groupPosition, int childPosition) {
		ExpandListGroup<T1, T2> group = getGroup(groupPosition);
		if (group != null) {
			return group.getItems().get(childPosition);
		}
		return null;
	}

	@Override
	public int getChildrenCount(final int groupPosition) {
		Log.d("JumpDebug", String.format("getChildrenCount thread: %s", Thread
				.currentThread().getName()));
		ExpandListGroup<T1, T2> group = getGroup(groupPosition);
		if (group != null) {
			return group.getItems().size();
		} else {
			// mExpandableListView.post(new Runnable() {
			// @Override
			// public void run() {
			// onLoadMore(groupPosition
			// - EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
			// }
			// });
			return 0;
		}
	}

	// abstract public biz.advancedcalendar.views.accessories.ExpandListGroup<T1, T2>
	// loadVirtualGroupPosition(
	// int i);
	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return 0L | getGroupId(groupPosition) << 12 | childPosition;
	}

	@Override
	public long getCombinedChildId(long groupId, long childId) {
		// return super.getCombinedChildId(groupId, childId);
		return groupId << 32 | childId << 1 | 1;
	}

	@Override
	public int getGroupTypeCount() {
		return 2;
	}

	@Override
	public int getGroupType(int groupPosition) {
		return getItemViewType(groupPosition);
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		Log.d("JumpDebug", String.format("getChildView thread: %s", Thread
				.currentThread().getName()));
		return getChildDataRow(groupPosition, childPosition, isLastChild, convertView,
				parent);
	}

	protected abstract View getGroupDataRow(int groupPosition, View convertView,
			ViewGroup parent);

	protected abstract View getChildDataRow(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent);

	/** returns a View to be displayed in the last row.
	 *
	 * @param groupPosition
	 * @param convertView
	 * @param parent
	 * @return */
	protected View getFooterView(int groupPosition, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = new TextView(mContext);
		}
		convertView.setFocusable(false);
		convertView.setClickable(false);
		((TextView) convertView).setGravity(Gravity.CENTER);
		if (groupPosition >= serverListSize && serverListSize > 0) {
			// the ListView has reached the last row
			((TextView) convertView).setHint("Reached the last row.");
			return convertView;
		} else {
			((TextView) convertView)
					.setHint("Loading...\n VirtualGroupPosition: "
							+ (groupPosition - EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET));
			return convertView;
		}
	}

	/** return the type of the row, the last row indicates the user that the ListView is
	 * loading more data */
	public int getItemViewType(int position) {
		return mLoadedGroups.containsKey(position
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET) ? EndlessScrollBaseExpandableListAdapter.VIEW_TYPE_ACTIVITY
				: EndlessScrollBaseExpandableListAdapter.VIEW_TYPE_LOADING;
	}

	@Override
	public ExpandListGroup<T1, T2> getGroup(int groupPosition) {
		return mLoadedGroups.get(groupPosition
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
	}

	// @Override
	// public void onGroupCollapsed(int groupPosition) {
	// }
	//
	// @Override
	// public void onGroupExpanded(int groupPosition) {
	// }
	@Override
	public int getGroupCount() {
		Log.d("JumpDebug", String.format("getGroupCount thread: %s", Thread
				.currentThread().getName()));
		return EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET * 2;
	}

	@Override
	public long getGroupId(int groupPosition) {
		int virtualGroupPosition = groupPosition
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
		if (virtualGroupPosition >= 0) {
			return virtualGroupPosition << 1;
		}
		return -virtualGroupPosition << 1 | 1;
	}

	@Override
	public long getCombinedGroupId(long groupId) {
		return (groupId & 0x7FFFFFFF) << 32;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		if (getItemViewType(groupPosition) == EndlessScrollBaseExpandableListAdapter.VIEW_TYPE_LOADING) {
			return getFooterView(groupPosition, convertView, parent);
		}
		View v = getGroupDataRow(groupPosition, convertView, parent);
		// ExpandableListView mExpandableListView = (ExpandableListView) parent;
		// mExpandableListView.expandGroup(groupPosition);
		return v;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return true;
	}

	public int getFirstVisibleGroup(ExpandableListView list) {
		// int firstVis = list.getFirstVisiblePosition();
		// long packedPosition =
		// list.getExpandableListPosition(list.getFirstVisiblePosition());
		int groupPosition = ExpandableListView.getPackedPositionGroup(list
				.getExpandableListPosition(list.getFirstVisiblePosition()));
		return groupPosition;
	}

	public int getLastVisibleGroup(ExpandableListView list) {
		int lastVis = list.getLastVisiblePosition();
		long packedPosition = list.getExpandableListPosition(lastVis);
		int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
		return groupPosition;
	}

	// Defines the process for actually loading more data based on page
	// The item in the virtualGroupPositionsToLoad should be prioritized to be loaded
	// earlier and the item should be loaded in the order they are in the list
	public abstract void onLoadMore(Integer virtualGroupPosition);

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
		if (visibleItemCount != 0) {
			mScrollPositionData.FirstVisibleFlatListPosition = firstVisibleItem;
			long packedPosition = ((ExpandableListView) view)
					.getExpandableListPosition(firstVisibleItem);
			mScrollPositionData.FirstVisibleGroup = ExpandableListView
					.getPackedPositionGroup(packedPosition);
			mScrollPositionData.FirstVisibleGroupsFirstVisibleChild = ExpandableListView
					.getPackedPositionChild(packedPosition);
			mScrollPositionData.FirstVisibleGroupsFirstVisibleChildsTop = view
					.getChildAt(0).getTop();
			mScrollPositionData.LastVisibleGroup = ExpandableListView
					.getPackedPositionGroup(((ExpandableListView) view)
							.getExpandableListPosition(firstVisibleItem
									+ visibleItemCount - 1));
			mScrollPositionData.FirstVisibleLoadedGroup = null;
			mScrollPositionData.FirstVisibleLoadedGroupsFirstVisibleChild = null;
			mScrollPositionData.setFirstVisibleLoadedGroupsFirstVisibleChildsTop(null);
			for (int i = mScrollPositionData.FirstVisibleGroup; i <= mScrollPositionData.LastVisibleGroup; i++) {
				if (isGroupLoaded(i
						- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET)) {
					mScrollPositionData.FirstVisibleLoadedGroup = i;
					if (mScrollPositionData.FirstVisibleGroup == i) {
						mScrollPositionData.FirstVisibleLoadedGroupsFirstVisibleChild = mScrollPositionData.FirstVisibleGroupsFirstVisibleChild;
						mScrollPositionData
								.setFirstVisibleLoadedGroupsFirstVisibleChildsTop(mScrollPositionData.FirstVisibleGroupsFirstVisibleChildsTop);
					} else {
						mScrollPositionData.FirstVisibleLoadedGroupsFirstVisibleChild = -1;
						mScrollPositionData
								.setFirstVisibleLoadedGroupsFirstVisibleChildsTop(view
										.getChildAt(
												((ExpandableListView) view)
														.getFlatListPosition(ExpandableListView
																.getPackedPositionForGroup(i))
														- firstVisibleItem).getTop());
					}
					break;
				}
			}
			//
			int start = mScrollPositionData.FirstVisibleGroup
					- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET
					- visibleThreshold;
			int end = mScrollPositionData.LastVisibleGroup
					- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET
					+ visibleThreshold;
			for (int virtualPosition = start; virtualPosition <= end; virtualPosition++) {
				if (!mLoadedGroups.containsKey(virtualPosition)) {
					onLoadMore(null);
					break;
				}
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	public int getVisibleThreshold() {
		return visibleThreshold;
	}

	public void setVisibleThreshold(int visibleThreshold) {
		this.visibleThreshold = visibleThreshold;
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		mExplicitlyCollapsedVirtualGroups.remove(Integer.valueOf(groupPosition
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET));
	}

	@Override
	public void onGroupCollapse(int groupPosition) {
		mExplicitlyCollapsedVirtualGroups.add(groupPosition
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
	}

	public boolean isGroupLoaded(int virtualGroupPosition) {
		return mLoadedGroups.containsKey(virtualGroupPosition);
	}

	protected void purgeGroupsIfNeeded() {
		final int firstVisibleVirtualGroup = ExpandableListView
				.getPackedPositionGroup(mExpandableListView
						.getExpandableListPosition(mExpandableListView
								.getFirstVisiblePosition()))
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
		final int lastVisibleVirtualGroup = ExpandableListView
				.getPackedPositionGroup(mExpandableListView
						.getExpandableListPosition(mExpandableListView
								.getLastVisiblePosition()))
				- EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET;
		final int visibleGroupCount = lastVisibleVirtualGroup - firstVisibleVirtualGroup
				+ 1;
		int mGroupsSizeBeforePurging = mLoadedGroups.size();
		if (visibleGroupCount > 0
				&& mLoadedGroups.size() - visibleGroupCount > startPurgeThreshold) {
			int countBefore = 0;
			int countAfter = 0;
			Iterator<Integer> it = mLoadedGroups.keySet().iterator();
			while (it.hasNext()) {
				int virtualGroup = it.next();
				if (virtualGroup < firstVisibleVirtualGroup) {
					countBefore++;
				} else if (virtualGroup > lastVisibleVirtualGroup) {
					countAfter++;
				}
			}
			//
			if (countBefore > visibleThreshold) {
				it = mLoadedGroups.keySet().iterator();
				while (it.hasNext() && countBefore > visibleThreshold
						&& countBefore + countAfter > visibleThreshold * 2) {
					it.next();
					it.remove();
					countBefore--;
				}
			}
			//
			it = mLoadedGroups.keySet().iterator();
			for (int i = 0; it.hasNext() && i < visibleThreshold * 2 + visibleGroupCount; i++) {
				it.next();
			}
			while (it.hasNext()) {
				it.next();
				it.remove();
			}
			Log.d("JumpDebug", "purging executed " + mGroupsSizeBeforePurging + " "
					+ firstVisibleVirtualGroup + " " + lastVisibleVirtualGroup + " "
					+ mLoadedGroups.keySet());
		}
	}

	// public ScrollPositionData getExpandableListViewScrollPositionData() {
	// View v = mExpandableListView.getChildAt(0);
	// if (v == null) {
	// return null;
	// }
	// ScrollPositionData data = new ScrollPositionData();
	// data.FirstVisibleFlatListPosition = mExpandableListView.getFirstVisiblePosition();
	// data.FirstVisibleGroup = ExpandableListView
	// .getPackedPositionGroup(mExpandableListView
	// .getExpandableListPosition(data.FirstVisibleFlatListPosition));
	// data.FirstVisibleGroupsFirstVisibleChild = ExpandableListView
	// .getPackedPositionChild(mExpandableListView
	// .getExpandableListPosition(data.FirstVisibleFlatListPosition));
	// data.FirstVisibleGroupsFirstVisibleChildsTop = v.getTop();
	// data.LastVisibleGroup = ExpandableListView
	// .getPackedPositionGroup(mExpandableListView
	// .getExpandableListPosition(mExpandableListView
	// .getLastVisiblePosition()));
	// for (int i = data.FirstVisibleGroup; i <= data.LastVisibleGroup; i++) {
	// if (isGroupLoaded(i
	// - EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET)) {
	// data.FirstVisibleLoadedGroup = i;
	// break;
	// }
	// }
	// // data.FirstVisibleLoadedViewFlatListPosition = null;
	// // data.FirstVisibleLoadedViewTop = null;
	// // data.FirstVisibleLoadedGroupsFirstVisibleChild = null;
	// // data.FirstVisibleLoadedGroupsFirstVisibleChildsTop = null;
	// // if (data.FirstVisibleLoadedGroup != null) {
	// // if (data.FirstVisibleFlatListPosition <
	// // data.FirstVisibleLoadedGroupFlatListPosition) {
	// // data.FirstVisibleLoadedViewFlatListPosition =
	// // data.FirstVisibleLoadedGroupFlatListPosition;
	// // data.FirstVisibleLoadedGroupsFirstVisibleChildsTop =
	// // data.FirstVisibleLoadedViewTop = mExpandableListView
	// // .getChildAt(
	// // data.FirstVisibleLoadedGroupFlatListPosition
	// // - data.FirstVisibleFlatListPosition).getTop();
	// // data.FirstVisibleLoadedGroupsFirstVisibleChild = 0;
	// // } else {
	// // data.FirstVisibleLoadedViewFlatListPosition =
	// // data.FirstVisibleFlatListPosition;
	// // data.FirstVisibleLoadedGroupsFirstVisibleChildsTop =
	// // data.FirstVisibleLoadedViewTop = data.Top;
	// // data.FirstVisibleLoadedGroupsFirstVisibleChild =
	// // data.FirstVisibleFlatListPosition
	// // - data.FirstVisibleLoadedGroupFlatListPosition;
	// // }
	// // }
	// return data;
	// }
	public void addLoadedGroup(ExpandListGroup<T1, T2> expandListGroup, int whereToScroll) {
		final int virtualGroupPosition = expandListGroup.getVirtualGroupPosition();
		switch (whereToScroll) {
		case RETAIN_CURRENT_SCROLL_POSITION:
			if (!mLoadedGroups.containsKey(virtualGroupPosition)) {
				ScrollPositionData data = mScrollPositionData;
				mLoadedGroups.put(virtualGroupPosition, expandListGroup);
				purgeGroupsIfNeeded();
				// notifyDataSetChanged() should be called after every change in
				// mGroups
				// otherwise group views for unrecognized reason will become
				// unclickable/unexpandable/uncollapsible
				notifyDataSetChanged();
				if (!mExplicitlyCollapsedVirtualGroups.contains(virtualGroupPosition)
						&& !mExpandableListView
								.isGroupExpanded(virtualGroupPosition
										+ EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET)) {
					mExpandableListView
							.expandGroup(virtualGroupPosition
									+ EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
				}
				//
				// groupFlatListPosition should be recalculated from scratch after
				// the
				// expandGroup() call. It must not be relied on that group
				// actually
				// gets expanded after expandGroup() has been called. Also
				// setSelectionFromTop() should be called in all cases, not only
				// when
				// addedGroup < firstVisibleLoadedGroup
				//
				if (data.FirstVisibleLoadedGroup != null) {
					int flatListPosition = mExpandableListView
							.getFlatListPosition(ExpandableListView
									.getPackedPositionForGroup(data.FirstVisibleLoadedGroup))
							+ data.FirstVisibleLoadedGroupsFirstVisibleChild + 1;
					Log.d("JumpDebug", String.format(
							"addLoadedGroup setSelectionFromTop(%d, %d) thread: %s",
							flatListPosition,
							data.getFirstVisibleLoadedGroupsFirstVisibleChildsTop(),
							Thread.currentThread().getName()));
					mExpandableListView.setSelectionFromTop(flatListPosition,
							data.getFirstVisibleLoadedGroupsFirstVisibleChildsTop());
				} else {
					int flatListPosition = mExpandableListView
							.getFlatListPosition(ExpandableListView
									.getPackedPositionForGroup(data.FirstVisibleGroup))
							+ data.FirstVisibleGroupsFirstVisibleChild + 1;
					Log.d("JumpDebug", String.format(
							"addLoadedGroup setSelectionFromTop(%d, %d) thread: %s",
							flatListPosition,
							data.FirstVisibleGroupsFirstVisibleChildsTop, Thread
									.currentThread().getName()));
					mExpandableListView.setSelectionFromTop(flatListPosition,
							data.FirstVisibleGroupsFirstVisibleChildsTop);
				}
			}
			break;
		case SCROLL_TO_THE_TOP_OF_THE_LOADED_GROUP:
			if (!mLoadedGroups.containsKey(virtualGroupPosition)) {
				mLoadedGroups.put(virtualGroupPosition, expandListGroup);
				purgeGroupsIfNeeded();
				// notifyDataSetChanged() should be called after every change in
				// mGroups
				// otherwise group views for unrecognized reason will become
				// unclickable/unexpandable/uncollapsible
				notifyDataSetChanged();
				if (!mExplicitlyCollapsedVirtualGroups.contains(virtualGroupPosition)
						&& !mExpandableListView
								.isGroupExpanded(virtualGroupPosition
										+ EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET)) {
					mExpandableListView
							.expandGroup(virtualGroupPosition
									+ EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET);
				}
			}
			int flatListPosition = mExpandableListView
					.getFlatListPosition(ExpandableListView
							.getPackedPositionForGroup(virtualGroupPosition
									+ EndlessScrollBaseExpandableListAdapter.VIRTUAL_MIDDLE_OFFSET));
			Log.d("JumpDebug", String.format(
					"addLoadedGroup setSelectionFromTop(%d, %d)", flatListPosition, 0));
			mExpandableListView.setSelectionFromTop(flatListPosition, 0);
			break;
		default:
			break;
		}
	}

	protected void clearLoadedGroups() {
		mLoadedGroups.clear();
	}

	public static class ScrollPositionData {
		public int FirstVisibleFlatListPosition;
		public int FirstVisibleGroup;
		public int FirstVisibleGroupsFirstVisibleChild;
		public int FirstVisibleGroupsFirstVisibleChildsTop;
		public int LastVisibleGroup;
		public Integer FirstVisibleLoadedGroup;
		public Integer FirstVisibleLoadedGroupsFirstVisibleChild;
		private Integer FirstVisibleLoadedGroupsFirstVisibleChildsTop;

		public ScrollPositionData(int firstVisibleFlatListPosition,
				int firstVisibleGroup, int firstVisibleGroupsFirstVisibleChild,
				int firstVisibleGroupsFirstVisibleChildsTop, int lastVisibleGroup,
				Integer firstVisibleLoadedGroup,
				Integer firstVisibleLoadedGroupsFirstVisibleChild,
				Integer firstVisibleLoadedGroupsFirstVisibleChildsTop) {
			super();
			FirstVisibleFlatListPosition = firstVisibleFlatListPosition;
			FirstVisibleGroup = firstVisibleGroup;
			FirstVisibleGroupsFirstVisibleChild = firstVisibleGroupsFirstVisibleChild;
			FirstVisibleGroupsFirstVisibleChildsTop = firstVisibleGroupsFirstVisibleChildsTop;
			LastVisibleGroup = lastVisibleGroup;
			FirstVisibleLoadedGroup = firstVisibleLoadedGroup;
			FirstVisibleLoadedGroupsFirstVisibleChild = firstVisibleLoadedGroupsFirstVisibleChild;
			FirstVisibleLoadedGroupsFirstVisibleChildsTop = firstVisibleLoadedGroupsFirstVisibleChildsTop;
		}

		public Integer getFirstVisibleLoadedGroupsFirstVisibleChildsTop() {
			return FirstVisibleLoadedGroupsFirstVisibleChildsTop;
		}

		public void setFirstVisibleLoadedGroupsFirstVisibleChildsTop(
				Integer firstVisibleLoadedGroupsFirstVisibleChildsTop) {
			FirstVisibleLoadedGroupsFirstVisibleChildsTop = firstVisibleLoadedGroupsFirstVisibleChildsTop;
		}
	}
}
