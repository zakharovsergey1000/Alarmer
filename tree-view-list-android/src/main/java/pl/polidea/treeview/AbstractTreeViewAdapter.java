package pl.polidea.treeview;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

/** Adapter used to feed the table view. */
public abstract class AbstractTreeViewAdapter extends BaseAdapter {
	private static final String TAG = AbstractTreeViewAdapter.class.getSimpleName();
	private final TreeStateManager treeStateManager;
	private final int numberOfLevels;
	private final LayoutInflater layoutInflater;
	// private ActionMode mActionMode = null;
	private int indentWidth = 0;
	private int indicatorGravity = 0;
	private Drawable collapsedDrawable;
	private Drawable expandedDrawable;
	private Drawable indicatorBackgroundDrawable;
	private Drawable rowBackgroundDrawable;
	// private ActionMode.Callback actionModeCallback;
	private final OnClickListener indicatorClickListener = new OnClickListener() {
		@Override
		public void onClick(final View v) {
			final Long id = (Long) v.getTag();
			expandCollapse(id);
		}
	};
	// private final OnClickListener treeItemClickListener = new
	// OnClickListener() {
	// @Override
	// public void onClick(final View v) {
	//
	// OnItemClickListener onItemClickListener = null;//
	// mTreeViewList.getOnTreeItemClickListener();
	// if (onItemClickListener != null) {
	// long id = (Long) v.getTag();
	// int position = mTreeViewList.getPositionInVisibleList(id);
	// onItemClickListener.onItemClick(
	// AbstractTreeViewAdapter.this.mTreeViewList, v, position,id);
	//
	// }
	// }
	// };
	//
	// private final OnLongClickListener treeItemLongClickListener = new
	// OnLongClickListener() {
	// @Override
	// public boolean onLongClick(final View v) {
	//
	// OnItemLongClickListener onItemLongClickListener = null;//
	// mTreeViewList.getOnTreeItemLongClickListener();
	// if (onItemLongClickListener != null) {
	// long id = (Long) v.getTag();
	// int position = mTreeViewList.getPositionInVisibleList(id);
	// return onItemLongClickListener.onItemLongClick(
	// AbstractTreeViewAdapter.this.mTreeViewList, v,position, id);
	// } else {
	// return false;
	// }
	// }
	// };
	// private final OnCheckedChangeListener checkboxCheckedChangeListener = new
	// OnCheckedChangeListener() {
	// @Override
	// public void onCheckedChanged(CompoundButton button, boolean value) {
	// long id = (Long) button.getTag();
	// treeStateManager.setItemCheckedById(id, value);
	// }
	// };
	public/* final */Activity mActivity;

	/** @return the mContext */
	public Activity getActivity() {
		return mActivity;
	}

	private TreeViewList mTreeViewList = null;

	// private MultiChoiceModeListener mMultiChoiceModeListener;
	protected TreeViewList getTreeViewList() {
		return mTreeViewList;
	}

	protected void setTreeViewList(TreeViewList treeViewList) {
		mTreeViewList = treeViewList;
		// sync ChoiceMode
		// treeStateManager.setChoiceMode(treeViewList.getChoiceMode());
	}

	protected TreeStateManager getManager() {
		return treeStateManager;
	}

	protected void expandCollapse(final Long id) {
		final TreeNodeInfo info = treeStateManager.getNodeInfo(id);
		if (!info.isWithChildren()) {
			// ignore - no default action
			return;
		}
		if (info.isExpanded()) {
			treeStateManager.collapseChildren(id);
		} else {
			treeStateManager.expandDirectChildren(id);
		}
	}

	private void calculateIndentWidth() {
		if (expandedDrawable != null) {
			indentWidth = Math
					.max(getIndentWidth(), expandedDrawable.getIntrinsicWidth());
		}
		if (collapsedDrawable != null) {
			indentWidth = Math.max(getIndentWidth(),
					collapsedDrawable.getIntrinsicWidth());
		}
	}

	public AbstractTreeViewAdapter(final Activity activity,
			final TreeStateManager treeStateManager, final int numberOfLevels) {
		mActivity = activity;
		this.treeStateManager = treeStateManager;
		// link treeStateManager to this
		// treeStateManager.setAbstractTreeViewAdapter(this);
		layoutInflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.numberOfLevels = numberOfLevels;
		collapsedDrawable = null;
		expandedDrawable = null;
		rowBackgroundDrawable = null;
		indicatorBackgroundDrawable = null;
		// this.actionModeCallback = actionModeCallback;
	}

	@Override
	public void registerDataSetObserver(final DataSetObserver observer) {
		treeStateManager.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(final DataSetObserver observer) {
		treeStateManager.unregisterDataSetObserver(observer);
	}

	@Override
	public int getCount() {
		return treeStateManager.getVisibleCount();
	}

	@Override
	public Object getItem(final int position) {
		return getTreeId(position);
	}

	public Long getTreeId(final int position) {
		return treeStateManager.getVisibleList().get(position);
	}

	public TreeNodeInfo getTreeNodeInfo(final int position) {
		return treeStateManager.getNodeInfo(getTreeId(position));
	}

	@Override
	public boolean hasStableIds() { // NOPMD
		return true;
	}

	@Override
	public int getItemViewType(final int position) {
		int itemViewType = getTreeNodeInfo(position).getLevel();
		if (itemViewType > numberOfLevels - 1) {
			itemViewType = numberOfLevels - 1;
		}
		return itemViewType;
	}

	@Override
	public int getViewTypeCount() {
		return numberOfLevels;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public boolean areAllItemsEnabled() { // NOPMD
		return true;
	}

	@Override
	public boolean isEnabled(final int position) { // NOPMD
		return true;
	}

	protected int getTreeListItemWrapperId() {
		return R.layout.tree_list_item_wrapper;
	}

	@Override
	public final View getView(final int position, final View convertView,
			final ViewGroup parent) {
		Log.d(AbstractTreeViewAdapter.TAG, "Creating a view based on " + convertView
				+ " with position " + position);
		final TreeNodeInfo nodeInfo = getTreeNodeInfo(position);
		if (convertView == null) {
			Log.d(AbstractTreeViewAdapter.TAG, "Creating the view a new");
			final pl.polidea.treeview.CheckableLinearLayout layout = (pl.polidea.treeview.CheckableLinearLayout) layoutInflater
					.inflate(getTreeListItemWrapperId(), null);
			return populateTreeItem(layout, getNewChildView(nodeInfo), nodeInfo, true);
		} else {
			Log.d(AbstractTreeViewAdapter.TAG, "Reusing the view");
			final LinearLayout linear = (LinearLayout) convertView;
			final pl.polidea.treeview.CheckableFrameLayout frameLayout = (pl.polidea.treeview.CheckableFrameLayout) linear
					.findViewById(R.id.treeview_list_item_frame);
			final View childView = frameLayout.getChildAt(0);
			updateView(childView, nodeInfo);
			return populateTreeItem(linear, childView, nodeInfo, false);
		}
	}

	/** Called when new view is to be created.
	 *
	 * @param treeNodeInfo
	 *            node info
	 * @return view that should be displayed as tree content */
	public abstract View getNewChildView(TreeNodeInfo treeNodeInfo);

	/** Called when new view is going to be reused. You should update the view and fill it
	 * in with the data required to display the new information. You can also create a new
	 * view, which will mean that the old view will not be reused.
	 *
	 * @param view
	 *            view that should be updated with the new values
	 * @param treeNodeInfo
	 *            node info used to populate the view
	 * @return view to used as row indented content */
	public abstract View updateView(View view, TreeNodeInfo treeNodeInfo);

	/** Retrieves background drawable for the node.
	 *
	 * @param treeNodeInfo
	 *            node info
	 * @return drawable returned as background for the whole row. Might be null, then
	 *         default background is used */
	public Drawable getBackgroundDrawable(final TreeNodeInfo treeNodeInfo) { // NOPMD
		return null;
	}

	// @SuppressLint("NewApi")
	private Drawable getDrawableOrDefaultBackground(final Drawable r) {
		if (r == null) {
			return mActivity.getResources()
					.getDrawable(R.drawable.list_selector_background).mutate();
		} else {
			return r;
		}
	}

	public final LinearLayout populateTreeItem(final LinearLayout layout,
			final View childView, final TreeNodeInfo nodeInfo, final boolean newChildView) {
		final Drawable individualRowDrawable = getBackgroundDrawable(nodeInfo);
		layout.setBackgroundDrawable(individualRowDrawable == null ? getDrawableOrDefaultBackground(rowBackgroundDrawable)
				: individualRowDrawable);
		final LinearLayout.LayoutParams indicatorLayoutParams = new LinearLayout.LayoutParams(
				calculateIndentation(nodeInfo),
				android.view.ViewGroup.LayoutParams.FILL_PARENT);
		final LinearLayout indicatorLayout = (LinearLayout) layout
				.findViewById(R.id.treeview_list_item_image_layout);
		indicatorLayout.setGravity(indicatorGravity);
		indicatorLayout.setLayoutParams(indicatorLayoutParams);
		final ImageView image = (ImageView) layout
				.findViewById(R.id.treeview_list_item_image);
		image.setImageDrawable(getDrawable(nodeInfo));
		image.setBackgroundDrawable(getDrawableOrDefaultBackground(indicatorBackgroundDrawable));
		image.setScaleType(ScaleType.CENTER);
		image.setTag(nodeInfo.getId());
		if (nodeInfo.isWithChildren() && getTreeViewList().isCollapsible()) {
			image.setOnClickListener(indicatorClickListener);
		} else {
			image.setOnClickListener(null);
		}
		layout.setTag(nodeInfo.getId());
		childView.setTag(nodeInfo.getId());
		final pl.polidea.treeview.CheckableFrameLayout frameLayout = (pl.polidea.treeview.CheckableFrameLayout) layout
				.findViewById(R.id.treeview_list_item_frame);
		final FrameLayout.LayoutParams childParams = new FrameLayout.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		if (newChildView) {
			// childView.setOnClickListener(treeItemClickListener);
			// childView.setOnLongClickListener(treeItemLongClickListener);
			frameLayout.addView(childView, childParams);
		}
		frameLayout.setTag(nodeInfo.getId());
		// boolean b =
		// getTreeViewList().isItemChecked(getManager().getVisibleList().lastIndexOf(nodeInfo.getId()));
		//
		// RadioButton rb = (RadioButton) layout
		// .findViewById(R.id.treeview_list_item_radiobutton);
		// rb.setTag(nodeInfo.getId());
		// rb.setOnCheckedChangeListener(null);
		//
		// CheckBox cb = (CheckBox) layout
		// .findViewById(R.id.treeview_list_item_checkbox);
		// cb.setTag(nodeInfo.getId());
		// cb.setOnCheckedChangeListener(null);
		//
		// switch (mTreeViewList.getChoiceMode()) {
		// case TreeViewList.CHOICE_MODE_NONE:
		// rb.setVisibility(View.GONE);
		// cb.setVisibility(View.GONE);
		// rb.setChecked(false);
		// cb.setChecked(false);
		// break;
		//
		// case TreeViewList.CHOICE_MODE_SINGLE:
		// rb.setVisibility(View.VISIBLE);
		// cb.setVisibility(View.GONE);
		// rb.setChecked(b);
		// cb.setChecked(b);
		//
		// break;
		//
		// case TreeViewList.CHOICE_MODE_MULTIPLE:
		// rb.setVisibility(View.GONE);
		// cb.setVisibility(View.VISIBLE);
		// rb.setChecked(b);
		// cb.setChecked(b);
		//
		// break;
		//
		// case TreeViewList.CHOICE_MODE_MULTIPLE_MODAL:
		// rb.setVisibility(View.GONE);
		// cb.setVisibility(View.VISIBLE);
		// rb.setChecked(b);
		// cb.setChecked(b);
		// break;
		//
		// default:
		// rb.setVisibility(View.GONE);
		// cb.setVisibility(View.GONE);
		// rb.setChecked(false);
		// cb.setChecked(false);
		// break;
		// }
		//
		// rb.setOnCheckedChangeListener(checkboxCheckedChangeListener);
		// cb.setOnCheckedChangeListener(checkboxCheckedChangeListener);
		//
		// if (layout instanceof Checkable) {
		// Checkable c = (Checkable) layout;
		// c.setChecked(b);
		// }
		// childView.setOnLongClickListener(new View.OnLongClickListener() {
		// // Called when the user long-clicks on childView
		// @SuppressLint("NewApi")
		// public boolean onLongClick(View view) {
		// if (mActionMode != null) {
		// return false;
		// }
		//
		// if (mTreeViewList.getChoiceMode() == TreeViewList.CHOICE_MODE_NONE) {
		// mTreeViewList
		// .setChoiceMode(TreeViewList.CHOICE_MODE_MULTIPLE_MODAL);
		//
		// // Start the CAB using the ActionMode.Callback defined above
		// mActionMode = getActivity().startActionMode(
		// actionModeCallback);
		//
		// mTreeViewList.setItemCheckedById((Long) childView.getTag(),
		// !mTreeViewList.isItemChecked((Long) childView
		// .getTag()));
		//
		// // view.setSelected(true);
		//
		// // if (layout instanceof Checkable) {
		// // Checkable c = (Checkable) layout;
		// // c.toggle();
		// // treeStateManager.setItemCheckedById(
		// // (Long) childView.getTag(), c.isChecked());
		// // }
		// return true;
		// }
		// return false;
		//
		// }
		// });
		// childView.setOnClickListener(new View.OnClickListener() {
		// // Called when the user long-clicks on childView
		// @SuppressLint("NewApi")
		// public void onClick(View view) {
		// if (mTreeViewList.getChoiceMode() != TreeViewList.CHOICE_MODE_NONE) {
		//
		// mTreeViewList.setItemCheckedById((Long) childView.getTag(),
		// !mTreeViewList.isItemChecked((Long) childView
		// .getTag()));
		//
		// // if (layout instanceof Checkable) {
		// // Checkable c = (Checkable) layout;
		// // c.toggle();
		// // treeStateManager.setItemCheckedById(
		// // (Long) childView.getTag(), c.isChecked());
		// // }
		// }
		// }
		// });
		return layout;
	}

	protected int calculateIndentation(final TreeNodeInfo nodeInfo) {
		return getIndentWidth()
				* (nodeInfo.getLevel() + (getTreeViewList().isCollapsible() ? 1 : 0));
	}

	protected Drawable getDrawable(final TreeNodeInfo nodeInfo) {
		if (!nodeInfo.isWithChildren() || !getTreeViewList().isCollapsible()) {
			return getDrawableOrDefaultBackground(indicatorBackgroundDrawable);
		}
		if (nodeInfo.isExpanded()) {
			return expandedDrawable;
		} else {
			return collapsedDrawable;
		}
	}

	public void setIndicatorGravity(final int indicatorGravity) {
		this.indicatorGravity = indicatorGravity;
	}

	public void setCollapsedDrawable(final Drawable collapsedDrawable) {
		this.collapsedDrawable = collapsedDrawable;
		calculateIndentWidth();
	}

	public void setExpandedDrawable(final Drawable expandedDrawable) {
		this.expandedDrawable = expandedDrawable;
		calculateIndentWidth();
	}

	public void setIndentWidth(final int indentWidth) {
		this.indentWidth = indentWidth;
		calculateIndentWidth();
	}

	public void setRowBackgroundDrawable(final Drawable rowBackgroundDrawable) {
		this.rowBackgroundDrawable = rowBackgroundDrawable;
	}

	public void setIndicatorBackgroundDrawable(final Drawable indicatorBackgroundDrawable) {
		this.indicatorBackgroundDrawable = indicatorBackgroundDrawable;
	}

	public void refresh() {
		treeStateManager.refresh();
	}

	private int getIndentWidth() {
		return indentWidth;
	}
	// public void handleItemClick(final View view, final Object id) {
	// treeStateManager.setItemCheckedById((Long) id,
	// !treeStateManager.isItemChecked((Long) id));
	// }
	// /**
	// * @return the mActionMode
	// */
	// public ActionMode getmActionMode() {
	// return mActionMode;
	// }
	//
	// /**
	// * @param mActionMode
	// * the mActionMode to set
	// */
	// public void setmActionMode(ActionMode mActionMode) {
	// this.mActionMode = mActionMode;
	// }
}
