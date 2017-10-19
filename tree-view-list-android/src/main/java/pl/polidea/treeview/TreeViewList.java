package pl.polidea.treeview;

import java.util.List;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

/** Tree view, expandable multi-level.
 *
 * <pre>
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_collapsible
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_src_expanded
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_src_collapsed
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_indent_width
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_handle_trackball_press
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_indicator_gravity
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_indicator_background
 * attr ref pl.polidea.treeview.R.styleable#TreeViewList_row_background
 * </pre> */
public class TreeViewList extends ListView {
	// public interface OnTreeItemClickListener {
	// public void OnTreeItemClick(AbstractTreeViewAdapter adapter, View view,
	// long id);
	// };
	//
	// public interface OnTreeItemLongClickListener {
	// public boolean OnTreeItemLongClick(AbstractTreeViewAdapter adapter, View
	// view,
	// long id);
	// };
	private static final int DEFAULT_COLLAPSED_RESOURCE = R.drawable.collapsed;
	private static final int DEFAULT_EXPANDED_RESOURCE = R.drawable.expanded;
	private static final int DEFAULT_INDENT = 0;
	private static final int DEFAULT_GRAVITY = Gravity.LEFT | Gravity.CENTER_VERTICAL;
	private Drawable expandedDrawable;
	private Drawable collapsedDrawable;
	private Drawable rowBackgroundDrawable;
	private Drawable indicatorBackgroundDrawable;
	private int indentWidth = 0;
	private int indicatorGravity = 0;
	private AbstractTreeViewAdapter treeAdapter;
	private boolean collapsible;

	// private boolean handleTrackballPress;
	/** Controls if/how the user may choose/check items in the list */
	// private int mChoiceMode = CHOICE_MODE_NONE;
	// private OnTreeItemClickListener mOnTreeItemClickListener;
	// private OnTreeItemLongClickListener mOnTreeItemLongClickListener;
	// private OnItemClickListener onItemClickListener;
	// private OnItemLongClickListener onItemLongClickListener;
	public TreeViewList(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.style.treeViewListStyle);
	}

	public TreeViewList(final Context context) {
		this(context, null);
	}

	public TreeViewList(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		parseAttributes(context, attrs);
	}

	private void parseAttributes(final Context context, final AttributeSet attrs) {
		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TreeViewList);
		expandedDrawable = a.getDrawable(R.styleable.TreeViewList_src_expanded);
		if (expandedDrawable == null) {
			expandedDrawable = context.getResources().getDrawable(
					TreeViewList.DEFAULT_EXPANDED_RESOURCE);
		}
		collapsedDrawable = a.getDrawable(R.styleable.TreeViewList_src_collapsed);
		if (collapsedDrawable == null) {
			collapsedDrawable = context.getResources().getDrawable(
					TreeViewList.DEFAULT_COLLAPSED_RESOURCE);
		}
		indentWidth = a.getDimensionPixelSize(R.styleable.TreeViewList_indent_width,
				TreeViewList.DEFAULT_INDENT);
		indicatorGravity = a.getInteger(R.styleable.TreeViewList_indicator_gravity,
				TreeViewList.DEFAULT_GRAVITY);
		indicatorBackgroundDrawable = a
				.getDrawable(R.styleable.TreeViewList_indicator_background);
		rowBackgroundDrawable = a.getDrawable(R.styleable.TreeViewList_row_background);
		collapsible = a.getBoolean(R.styleable.TreeViewList_collapsible, true);
		// handleTrackballPress = a.getBoolean(
		// R.styleable.TreeViewList_handle_trackball_press, true);
		a.recycle();
	}

	public int getPositionInVisibleList(Long id) {
		List<Long> visibleList = treeAdapter.getManager().getVisibleList();
		return visibleList.lastIndexOf(id);
	}

	// @Override
	// public void setOnItemClickListener(OnItemClickListener
	// onItemClickListener) {
	// this.onItemClickListener = onItemClickListener;
	// }
	//
	// @Override
	// public void setOnItemLongClickListener(OnItemLongClickListener
	// onItemLongClickListener) {
	// this.onItemLongClickListener = onItemLongClickListener;
	// }
	// @Override
	// public void setOnItemClickListener(OnItemClickListener
	// onItemClickListener) {
	// throw new
	// RuntimeException("Method not implemented: setOnItemClickListener(OnItemClickListener listener)");
	// }
	//
	// @Override
	// public void setOnItemLongClickListener(OnItemLongClickListener
	// onItemLongClickListener) {
	// throw new
	// RuntimeException("Method not implemented: setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener)");
	// }
	// public void setOnTreeItemClickListener(OnItemClickListener
	// onItemClickListener) {
	// this.onItemClickListener =onItemClickListener;
	// }
	//
	// public OnItemClickListener getOnTreeItemClickListener() {
	// return this.onItemClickListener ;
	// }
	//
	// public void setOnTreeItemLongClickListener(OnItemLongClickListener
	// onItemLongClickListener) {
	// this.onItemLongClickListener =onItemLongClickListener;
	// }
	//
	// public OnItemLongClickListener getOnTreeItemLongClickListener() {
	// return this.onItemLongClickListener ;
	// }
	// @Override
	// public void setChoiceMode(final int choiceMode) {
	//
	// super.setChoiceMode( choiceMode);
	// mChoiceMode = choiceMode;
	// // notify TreeStateManager
	// if (treeAdapter != null) {
	// // treeAdapter.getManager().setChoiceMode(choiceMode);
	// }
	// }
	// @Override
	// public int getChoiceMode() {
	// return super.getChoiceMode();
	// //return mChoiceMode;
	// }
	// @Override
	// public long[] getCheckedItemIds() {
	// return treeAdapter.getManager().getCheckedItemIds();
	// }
	//
	// public void setCheckedItemIds(long[] idArray) {
	// treeAdapter.getManager().setCheckedItemIds(idArray);
	// }
	//
	// @Override
	// public int getCheckedItemCount() {
	// return treeAdapter.getManager().getCheckedItemIds().length;
	// }
	//
	// @Override
	// public void setItemChecked(int position, boolean value) {
	// // throw new
	// //
	// RuntimeException("The method setItemChecked(int position, boolean value) is not implemented.");
	// super.setItemChecked(position, value);
	// treeAdapter.getManager().setItemCheckedByPositionInVisibleList(
	// position, value);
	// treeAdapter.refresh();
	// }
	// public void setItemCheckedById(long id, boolean value) {
	// treeAdapter.getManager().setItemCheckedById(id, value);
	// }
	//
	// @Override
	// public boolean isItemChecked(int position) {
	// // throw new
	// // RuntimeException("The method isItemChecked(int position) is not implemented.");
	// return treeAdapter.getManager().getItemCheckedByPositionInVisibleList(
	// position);
	//
	// }
	// public boolean isItemChecked(long id) {
	// return treeAdapter.getManager().isItemChecked(id);
	// }
	// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	// @Override
	// public void setMultiChoiceModeListener(
	// MultiChoiceModeListener multiChoiceModeListener) {
	// super.setMultiChoiceModeListener(multiChoiceModeListener);
	// }
	@Override
	public void setAdapter(final ListAdapter adapter) {
		if (!(adapter instanceof AbstractTreeViewAdapter)) {
			throw new TreeConfigurationException(
					"The adapter is not of TreeViewAdapter type");
		}
		treeAdapter = (AbstractTreeViewAdapter) adapter;
		syncAdapter();
		super.setAdapter(treeAdapter);
	}

	private void syncAdapter() {
		treeAdapter.setCollapsedDrawable(collapsedDrawable);
		treeAdapter.setExpandedDrawable(expandedDrawable);
		treeAdapter.setIndicatorGravity(indicatorGravity);
		treeAdapter.setIndentWidth(indentWidth);
		treeAdapter.setIndicatorBackgroundDrawable(indicatorBackgroundDrawable);
		treeAdapter.setRowBackgroundDrawable(rowBackgroundDrawable);
		// treeAdapter.setCollapsible(isCollapsible());
		// if (handleTrackballPress) {
		// setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(final AdapterView<?> parent,
		// final View view, final int position, final long id) {
		// treeAdapter.handleItemClick(view, view.getTag());
		// }
		// });
		// } else {
		// setOnClickListener(null);
		// }
		// link treeAdapter to this
		treeAdapter.setTreeViewList(this);
		// treeAdapter.getManager().setChoiceMode(mChoiceMode);
	}

	public void setExpandedDrawable(final Drawable expandedDrawable) {
		this.expandedDrawable = expandedDrawable;
		syncAdapter();
		treeAdapter.refresh();
	}

	public void setCollapsedDrawable(final Drawable collapsedDrawable) {
		this.collapsedDrawable = collapsedDrawable;
		syncAdapter();
		treeAdapter.refresh();
	}

	public void setRowBackgroundDrawable(final Drawable rowBackgroundDrawable) {
		this.rowBackgroundDrawable = rowBackgroundDrawable;
		syncAdapter();
		treeAdapter.refresh();
	}

	public void setIndicatorBackgroundDrawable(final Drawable indicatorBackgroundDrawable) {
		this.indicatorBackgroundDrawable = indicatorBackgroundDrawable;
		syncAdapter();
		treeAdapter.refresh();
	}

	public void setIndentWidth(final int indentWidth) {
		this.indentWidth = indentWidth;
		syncAdapter();
		treeAdapter.refresh();
	}

	public void setIndicatorGravity(final int indicatorGravity) {
		this.indicatorGravity = indicatorGravity;
		syncAdapter();
		treeAdapter.refresh();
	}

	public void setCollapsible(final boolean collapsible) {
		this.collapsible = collapsible;
		syncAdapter();
		treeAdapter.refresh();
	}

	// public void setHandleTrackballPress(final boolean handleTrackballPress) {
	// this.handleTrackballPress = handleTrackballPress;
	// syncAdapter();
	// treeAdapter.refresh();
	// }
	public Drawable getExpandedDrawable() {
		return expandedDrawable;
	}

	public Drawable getCollapsedDrawable() {
		return collapsedDrawable;
	}

	public Drawable getRowBackgroundDrawable() {
		return rowBackgroundDrawable;
	}

	public Drawable getIndicatorBackgroundDrawable() {
		return indicatorBackgroundDrawable;
	}

	public int getIndentWidth() {
		return indentWidth;
	}

	public int getIndicatorGravity() {
		return indicatorGravity;
	}

	public boolean isCollapsible() {
		if (getChoiceMode() == AbsListView.CHOICE_MODE_NONE) {
			return collapsible;
		}
		return false;
	}
	// public boolean isHandleTrackballPress() {
	// return handleTrackballPress;
	// }
}
