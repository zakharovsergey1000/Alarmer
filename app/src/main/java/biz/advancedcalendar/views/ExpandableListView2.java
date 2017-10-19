package biz.advancedcalendar.views;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExpandableListView2 extends ExpandableListView implements
		OnGroupExpandListener, OnGroupCollapseListener {
	Map<Integer, ArrayList<Integer>> mCheckedItemPositions = new HashMap<Integer, ArrayList<Integer>>();
	private ActionMode mActionMode = null;
	private OnChildrenCountCheckedInGroupChangedListener mOnChildrenCountCheckedInGroupChangedListener = null;

	public interface OnChildrenCountCheckedInGroupChangedListener {
		void OnChildrenCountCheckedInGroupChanged(int group, int totalChildrenCount,
				int checkedChildrenCount);
	}

	// private class MultiChoiceModeListenerWrapper implements MultiChoiceModeListener {
	// MultiChoiceModeListener mWrapped;
	//
	// public MultiChoiceModeListenerWrapper(MultiChoiceModeListener c) {
	// mWrapped = c;
	// }
	//
	// @Override
	// public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	// boolean result = mWrapped.onCreateActionMode(mode, menu);
	// if (result) {
	// mActionMode = mode;
	// mCheckedItemPositions.clear();
	// // post(new Runnable() {
	// // @Override
	// // public void run() {
	// // if (mActionMode != null) {
	// // SparseBooleanArray positions = getCheckedItemPositionsFromSuper();
	// // for (int i = 0; i < positions.size(); i++) {
	// // if (positions.valueAt(i)) {
	// // int checkedItemPosition = positions.keyAt(i);
	// // long packedPosition = getExpandableListPosition(checkedItemPosition);
	// // int childPosition = ExpandableListView
	// // .getPackedPositionChild(packedPosition);
	// // if (childPosition != -1) {
	// // int groupPosition = ExpandableListView
	// // .getPackedPositionGroup(packedPosition);
	// // ArrayList<Integer> list = mCheckedItemPositions
	// // .get(groupPosition);
	// // if (list != null) {
	// // int index = list.lastIndexOf(childPosition);
	// // if (index == -1) {
	// // list.add(childPosition);
	// // }
	// // } else {
	// // ArrayList<Integer> newList = new ArrayList<Integer>();
	// // newList.add(childPosition);
	// // mCheckedItemPositions.put(groupPosition,
	// // newList);
	// // }
	// // translateCheckedItemPositionsToAbsListViewCheckedItemPositions();
	// // // setItemChecked(position,
	// // // !isItemChecked(position));
	// // }
	// // }
	// // }
	// // }
	// // }
	// // });
	// } else {
	// mActionMode = null;
	// }
	// return result;
	// }
	//
	// @Override
	// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	// boolean result = mWrapped.onPrepareActionMode(mode, menu);
	// if (result) {
	// mActionMode = mode;
	// mCheckedItemPositions.clear();
	// } else {
	// mActionMode = null;
	// }
	// return result;
	// }
	//
	// @Override
	// public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	// return mWrapped.onActionItemClicked(mode, item);
	// }
	//
	// @Override
	// public void onDestroyActionMode(ActionMode mode) {
	// mWrapped.onDestroyActionMode(mode);
	// mCheckedItemPositions.clear();
	// mActionMode = null;
	// }
	//
	// @Override
	// public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
	// boolean checked) {
	// mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
	// }
	// }
	private class ActionModeCallbackWrapper implements ActionMode.Callback {
		ActionMode.Callback wrapped;

		public ActionModeCallbackWrapper(Callback c) {
			wrapped = c;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			boolean result = wrapped.onCreateActionMode(mode, menu);
			if (result) {
				mActionMode = mode;
				mCheckedItemPositions.clear();
				post(new Runnable() {
					@Override
					public void run() {
						if (mActionMode != null) {
							SparseBooleanArray positions = getCheckedItemPositionsFromSuper();
							for (int i = 0; i < positions.size(); i++) {
								if (positions.valueAt(i)) {
									int checkedItemPosition = positions.keyAt(i);
									long packedPosition = getExpandableListPosition(checkedItemPosition);
									int childPosition = ExpandableListView
											.getPackedPositionChild(packedPosition);
									if (childPosition != -1) {
										int groupPosition = ExpandableListView
												.getPackedPositionGroup(packedPosition);
										ArrayList<Integer> list = mCheckedItemPositions
												.get(groupPosition);
										if (list != null) {
											int index = list.lastIndexOf(childPosition);
											if (index == -1) {
												list.add(childPosition);
											}
										} else {
											list = new ArrayList<Integer>();
											list.add(childPosition);
											mCheckedItemPositions
													.put(groupPosition, list);
										}
										if (mOnChildrenCountCheckedInGroupChangedListener != null) {
											int totalChildrenCount = getExpandableListAdapter()
													.getChildrenCount(groupPosition);
											mOnChildrenCountCheckedInGroupChangedListener
													.OnChildrenCountCheckedInGroupChanged(
															groupPosition,
															totalChildrenCount,
															list.size());
										}
									}
									//
									else {
										int groupPosition = ExpandableListView
												.getPackedPositionGroup(packedPosition);
										if (groupPosition != -1
												&& mCheckedItemPositions
														.get(groupPosition) == null) {
											ArrayList<Integer> newList = new ArrayList<Integer>();
											mCheckedItemPositions.put(groupPosition,
													newList);
											int count = getExpandableListAdapter()
													.getChildrenCount(groupPosition);
											for (int i1 = 0; i1 < count; i1++) {
												newList.add(i1);
											}
											if (mOnChildrenCountCheckedInGroupChangedListener != null) {
												mOnChildrenCountCheckedInGroupChangedListener
														.OnChildrenCountCheckedInGroupChanged(
																groupPosition, count,
																count);
											}
										}
									}
									translateCheckedItemPositionsToAbsListViewCheckedItemPositions();
								}
							}
						}
					}
				});
			} else {
				mActionMode = null;
			}
			return result;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return wrapped.onPrepareActionMode(mode, menu);
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return wrapped.onActionItemClicked(mode, item);
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			wrapped.onDestroyActionMode(mode);
			mCheckedItemPositions.clear();
			mActionMode = null;
		}
		// @Override
		// public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
		// boolean checked) {
		// // TODO Auto-generated method stub
		// }
	}

	public ExpandableListView2(Context context) {
		super(context);
		// setOnItemClickListener(this);
		// setOnItemLongClickListener(this);
		// setMultiChoiceModeListener(new MultiChoiceModeListener() {
		// @Override
		// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// return false;
		// }
		//
		// @Override
		// public void onDestroyActionMode(ActionMode mode) {
		// }
		//
		// @Override
		// public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// return true;
		// }
		//
		// @Override
		// public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// return false;
		// }
		//
		// @Override
		// public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
		// boolean checked) {
		// }
		// });
		setOnGroupExpandListener(this);
		setOnGroupCollapseListener(this);
	}

	public ExpandableListView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		// setOnItemClickListener(this);
		// setOnItemLongClickListener(this);
		// setMultiChoiceModeListener(new MultiChoiceModeListener() {
		// @Override
		// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// return false;
		// }
		//
		// @Override
		// public void onDestroyActionMode(ActionMode mode) {
		// }
		//
		// @Override
		// public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// return true;
		// }
		//
		// @Override
		// public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// return false;
		// }
		//
		// @Override
		// public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
		// boolean checked) {
		// }
		// });
		setOnGroupExpandListener(this);
		setOnGroupCollapseListener(this);
	}

	public ExpandableListView2(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// setOnItemClickListener(this);
		// setOnItemLongClickListener(this);
		// setMultiChoiceModeListener(new MultiChoiceModeListener() {
		// @Override
		// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// return false;
		// }
		//
		// @Override
		// public void onDestroyActionMode(ActionMode mode) {
		// }
		//
		// @Override
		// public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// return true;
		// }
		//
		// @Override
		// public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// return false;
		// }
		//
		// @Override
		// public void onItemCheckedStateChanged(ActionMode mode, int position, long id,
		// boolean checked) {
		// }
		// });
		setOnGroupExpandListener(this);
		setOnGroupCollapseListener(this);
	}

	// public MyExpandableListView(Context context, AttributeSet attrs, int defStyleAttr,
	// int defStyleRes) {
	// super(context, attrs, defStyleAttr, defStyleRes);
	// }
	public void setOnChildrenCountCheckedInGroupChangedListener(
			OnChildrenCountCheckedInGroupChangedListener onChildrenCountCheckedInGroupChangedListener) {
		mOnChildrenCountCheckedInGroupChangedListener = onChildrenCountCheckedInGroupChangedListener;
	}

	public OnChildrenCountCheckedInGroupChangedListener getOnChildrenCountCheckedInGroupChangedListener() {
		return mOnChildrenCountCheckedInGroupChangedListener;
	}

	private SparseBooleanArray getCheckedItemPositionsFromSuper() {
		SparseBooleanArray positions = super.getCheckedItemPositions();
		return positions;
	}

	public void setChildChecked(int group, int child, boolean checked) {
		long packedPosition = ExpandableListView.getPackedPositionForChild(group, child);
		int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
		if (childPosition != -1) {
			int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
			ArrayList<Integer> list = mCheckedItemPositions.get(groupPosition);
			if (list != null) {
				int index = list.lastIndexOf(childPosition);
				if (index != -1 && !checked) {
					list.remove(index);
					if (mOnChildrenCountCheckedInGroupChangedListener != null) {
						ExpandableListAdapter expandableListAdapter = getExpandableListAdapter();
						int totalChildrenCount = expandableListAdapter
								.getChildrenCount(childPosition);
						mOnChildrenCountCheckedInGroupChangedListener
								.OnChildrenCountCheckedInGroupChanged(groupPosition,
										totalChildrenCount, list.size());
					}
					if (list.size() == 0) {
						mCheckedItemPositions.remove(groupPosition);
					}
					if (mCheckedItemPositions.size() == 0) {
						mActionMode.finish();
					}
				} else if (index == -1 && checked) {
					list.add(childPosition);
					if (mOnChildrenCountCheckedInGroupChangedListener != null) {
						ExpandableListAdapter expandableListAdapter = getExpandableListAdapter();
						int totalChildrenCount = expandableListAdapter
								.getChildrenCount(groupPosition);
						mOnChildrenCountCheckedInGroupChangedListener
								.OnChildrenCountCheckedInGroupChanged(groupPosition,
										totalChildrenCount, list.size());
					}
				}
			} else if (checked) {
				ArrayList<Integer> newList = new ArrayList<Integer>();
				mCheckedItemPositions.put(groupPosition, newList);
				newList.add(childPosition);
				if (mOnChildrenCountCheckedInGroupChangedListener != null) {
					ExpandableListAdapter expandableListAdapter = getExpandableListAdapter();
					int totalChildrenCount = expandableListAdapter
							.getChildrenCount(childPosition);
					mOnChildrenCountCheckedInGroupChangedListener
							.OnChildrenCountCheckedInGroupChanged(groupPosition,
									totalChildrenCount, newList.size());
				}
			}
		}
		translateCheckedItemPositionsToAbsListViewCheckedItemPositions();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, mCheckedItemPositions);
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		mCheckedItemPositions = ss.mCheckedItemPositions;
	}

	@Override
	public ActionMode startActionMode(ActionMode.Callback c) {
		return super.startActionMode(new ActionModeCallbackWrapper(c));
	}

	// @Override
	// public void setMultiChoiceModeListener(MultiChoiceModeListener l) {
	// mMultiChoiceModeListenerWrapper = new MultiChoiceModeListenerWrapper(l);
	// super.setMultiChoiceModeListener(mMultiChoiceModeListenerWrapper);
	// }
	public boolean isInActionMode() {
		return mActionMode != null;
	}

	@Override
	public SparseBooleanArray getCheckedItemPositions() {
		throw new RuntimeException(
				"For ExpandableListView2, use getCheckedItemPositions2() instead of "
						+ "getCheckedItemPositions()");
	}

	public Map<Integer, ArrayList<Integer>> getCheckedItemPositions2() {
		return mCheckedItemPositions;
	}

	@Override
	public boolean performItemClick(View v, int position, long id) {
		boolean result = super.performItemClick(v, position, id);
		if (isInActionMode()) {
			long packedPosition = getExpandableListPosition(position);
			int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);
			if (childPosition != -1) {
				int groupPosition = ExpandableListView
						.getPackedPositionGroup(packedPosition);
				ArrayList<Integer> list = mCheckedItemPositions.get(groupPosition);
				if (list != null) {
					int index = list.lastIndexOf(childPosition);
					if (index != -1) {
						list.remove(index);
						if (mOnChildrenCountCheckedInGroupChangedListener != null) {
							ExpandableListAdapter expandableListAdapter = getExpandableListAdapter();
							int totalChildrenCount = expandableListAdapter
									.getChildrenCount(childPosition);
							mOnChildrenCountCheckedInGroupChangedListener
									.OnChildrenCountCheckedInGroupChanged(groupPosition,
											totalChildrenCount, list.size());
						}
						if (list.size() == 0) {
							mCheckedItemPositions.remove(groupPosition);
						}
						if (mCheckedItemPositions.size() == 0) {
							mActionMode.finish();
						}
					} else {
						list.add(childPosition);
						if (mOnChildrenCountCheckedInGroupChangedListener != null) {
							ExpandableListAdapter expandableListAdapter = getExpandableListAdapter();
							int totalChildrenCount = expandableListAdapter
									.getChildrenCount(groupPosition);
							mOnChildrenCountCheckedInGroupChangedListener
									.OnChildrenCountCheckedInGroupChanged(groupPosition,
											totalChildrenCount, list.size());
						}
					}
				} else {
					ArrayList<Integer> newList = new ArrayList<Integer>();
					mCheckedItemPositions.put(groupPosition, newList);
					newList.add(childPosition);
					if (mOnChildrenCountCheckedInGroupChangedListener != null) {
						ExpandableListAdapter expandableListAdapter = getExpandableListAdapter();
						int totalChildrenCount = expandableListAdapter
								.getChildrenCount(childPosition);
						mOnChildrenCountCheckedInGroupChangedListener
								.OnChildrenCountCheckedInGroupChanged(groupPosition,
										totalChildrenCount, newList.size());
					}
				}
			}
			translateCheckedItemPositionsToAbsListViewCheckedItemPositions();
		}
		return result;
	}

	// @Override
	// public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	// if (isInActionMode) {
	// long expandableListPosition = getExpandableListPosition(position);
	// int packedPositionChild = ExpandableListView
	// .getPackedPositionChild(expandableListPosition);
	// if (packedPositionChild != -1) {
	// }
	// }
	// // super.onItemClick(parent, view, position, id);
	// // onItemClick(parent, view, position, id);
	// }
	//
	// @Override
	// public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long
	// id) {
	// // TODO Auto-generated method stub
	// return false;
	// }
	@Override
	public void onGroupCollapse(int groupPosition) {
		if (isInActionMode()) {
			translateCheckedItemPositionsToAbsListViewCheckedItemPositions();
		}
	}

	@Override
	public void onGroupExpand(int groupPosition) {
		if (isInActionMode()) {
			translateCheckedItemPositionsToAbsListViewCheckedItemPositions();
		}
	}

	private void translateCheckedItemPositionsToAbsListViewCheckedItemPositions() {
		// first uncheck all positions except one cerber position in current flat list
		// state
		// uncheckAllPositionsInCurrentFlatListStateExceptOneCerberPosition
		SparseBooleanArray positions = getCheckedItemPositionsFromSuper();
		ArrayList<Integer> checkedItemPositionsList = new ArrayList<Integer>();
		for (int i = 0, size = positions.size(); i < size; i++) {
			if (positions.valueAt(i)) {
				int checkedItemPosition = positions.keyAt(i);
				checkedItemPositionsList.add(checkedItemPosition);
			}
		}
		int size = checkedItemPositionsList.size();
		for (int i = 1; i < size; i++) {
			int checkedItemPosition = checkedItemPositionsList.get(i);
			setItemChecked(checkedItemPosition, false);
		}
		Integer cerberPosition = size > 0 ? checkedItemPositionsList.get(0) : null;
		// next check required (currently visible) positions in current flat list state
		Iterator<Map.Entry<Integer, ArrayList<Integer>>> entries = mCheckedItemPositions
				.entrySet().iterator();
		ArrayList<Integer> flatListPositionsToCheck = new ArrayList<Integer>();
		while (entries.hasNext()) {
			Map.Entry<Integer, ArrayList<Integer>> entry = entries.next();
			int currentGroupPosition = entry.getKey();
			int groupFlatListPosition = getFlatListPosition(ExpandableListView
					.getPackedPositionForGroup(currentGroupPosition));
			flatListPositionsToCheck.add(groupFlatListPosition);
			if (isGroupExpanded(currentGroupPosition)) {
				ArrayList<Integer> list = entry.getValue();
				int count2 = list.size();
				for (int i1 = 0; i1 < count2; i1++) {
					int childPosition = list.get(i1);
					int childFlatListPosition = getFlatListPosition(ExpandableListView
							.getPackedPositionForChild(currentGroupPosition,
									childPosition));
					flatListPositionsToCheck.add(childFlatListPosition);
				}
			}
		}
		for (Integer flatListPosition : flatListPositionsToCheck) {
			setItemChecked(flatListPosition, true);
		}
		// next resolve cerber position in current flat list state
		if (cerberPosition != null && !flatListPositionsToCheck.contains(cerberPosition)) {
			setItemChecked(cerberPosition, false);
		}
	}

	// private Integer uncheckAllPositionsInCurrentFlatListStateExceptOneCerberPosition()
	// {
	// SparseBooleanArray positions = getCheckedItemPositionsFromSuper();
	// ArrayList<Integer> checkedItemPositionsList = new ArrayList<Integer>();
	// for (int i = 0, size = positions.size(); i < size; i++) {
	// if (positions.valueAt(i)) {
	// int checkedItemPosition = positions.keyAt(i);
	// checkedItemPositionsList.add(checkedItemPosition);
	// }
	// }
	// int size = checkedItemPositionsList.size();
	// for (int i = 1; i < size; i++) {
	// int checkedItemPosition = checkedItemPositionsList.get(i);
	// setItemChecked(checkedItemPosition, false);
	// }
	// return size > 0 ? checkedItemPositionsList.get(0) : null;
	// }
	static class SavedState extends BaseSavedState {
		Map<Integer, ArrayList<Integer>> mCheckedItemPositions;

		SavedState(Parcelable superState,
				Map<Integer, ArrayList<Integer>> checkedItemPositions) {
			super(superState);
			mCheckedItemPositions = checkedItemPositions;
		}

		private SavedState(Parcel in) {
			super(in);
			int count = in.readInt();
			mCheckedItemPositions = new HashMap<Integer, ArrayList<Integer>>();
			for (int i = 0; i < count; i++) {
				ArrayList<Integer> list = new ArrayList<Integer>();
				mCheckedItemPositions.put(in.readInt(), list);
				int size2 = in.readInt();
				for (int i1 = 0; i1 < size2; i1++) {
					list.add(in.readInt());
				}
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			int count = mCheckedItemPositions.size();
			dest.writeInt(count);
			Iterator<Map.Entry<Integer, ArrayList<Integer>>> entries = mCheckedItemPositions
					.entrySet().iterator();
			while (entries.hasNext()) {
				Map.Entry<Integer, ArrayList<Integer>> entry = entries.next();
				dest.writeInt(entry.getKey());
				ArrayList<Integer> list = entry.getValue();
				int count2 = list.size();
				dest.writeInt(count2);
				for (int i1 = 0; i1 < count2; i1++) {
					dest.writeInt(list.get(i1));
				}
			}
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
}
