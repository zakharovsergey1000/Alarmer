package pl.polidea.treeview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/** In-memory manager of tree state. */
public class InMemoryTreeStateManager implements TreeStateManager {
	private static final String TAG = InMemoryTreeStateManager.class.getSimpleName();
	private final Map<Long, InMemoryTreeNode> allNodes = new HashMap<Long, InMemoryTreeNode>();
	private InMemoryTreeNode topSentinel = new InMemoryTreeNode(null, null, -1, true);
	private transient List<Long> visibleListCache = null; // lasy initialised
	private transient List<Long> unmodifiableVisibleList = null;
	private boolean visibleByDefault = true;
	private transient Set<DataSetObserver> observers = new HashSet<DataSetObserver>();

	public InMemoryTreeStateManager() {
		// TODO Auto-generated constructor stub
	}

	/** Controls if/how the user may choose/check items in the list */
	// int mChoiceMode = TreeViewList.CHOICE_MODE_NONE;
	// private final Set<Long> selectedNodes = new HashSet<Long>();
	// private transient AbstractTreeViewAdapter mAbstractTreeViewAdapter = null;
	private synchronized void internalDataSetChanged() {
		visibleListCache = null;
		unmodifiableVisibleList = null;
		for (final DataSetObserver observer : observers) {
			observer.onChanged();
		}
	}

	/** If true new nodes are visible by default.
	 * 
	 * @param visibleByDefault
	 *            if true, then newly added nodes are expanded by default */
	public void setVisibleByDefault(final boolean visibleByDefault) {
		this.visibleByDefault = visibleByDefault;
	}

	private InMemoryTreeNode getNodeFromTreeOrThrow(final Long id) {
		if (id == null) {
			throw new NodeNotInTreeException("(null)");
		}
		final InMemoryTreeNode node = allNodes.get(id);
		if (node == null) {
			throw new NodeNotInTreeException(id.toString());
		}
		return node;
	}

	private InMemoryTreeNode getNodeFromTreeOrThrowAllowRoot(final Long id) {
		if (id == null) {
			return topSentinel;
		}
		return getNodeFromTreeOrThrow(id);
	}

	private void expectNodeNotInTreeYet(final Long id) {
		final InMemoryTreeNode node = allNodes.get(id);
		if (node != null) {
			throw new NodeAlreadyInTreeException(id.toString(), node.toString());
		}
	}

	@Override
	public synchronized TreeNodeInfo getNodeInfo(final Long id) {
		final InMemoryTreeNode node = getNodeFromTreeOrThrow(id);
		final List<InMemoryTreeNode> children = node.getChildren();
		boolean expanded = false;
		if (!children.isEmpty() && children.get(0).isVisible()) {
			expanded = true;
		}
		return new TreeNodeInfo(id, node.getLevel(), !children.isEmpty(),
				node.isVisible(), expanded);
	}

	@Override
	public synchronized List<Long> getChildren(final Long id) {
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		return node.getChildIdList();
	}

	@Override
	public synchronized Long getParent(final Long id) {
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		return node.getParent();
	}

	private boolean getChildrenVisibility(final InMemoryTreeNode node) {
		boolean visibility;
		final List<InMemoryTreeNode> children = node.getChildren();
		if (children.isEmpty()) {
			visibility = visibleByDefault;
		} else {
			visibility = children.get(0).isVisible();
		}
		return visibility;
	}

	@Override
	public synchronized void addBeforeChild(final Long parent, final Long newChild,
			final Long beforeChild) {
		expectNodeNotInTreeYet(newChild);
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(parent);
		final boolean visibility = getChildrenVisibility(node);
		// top nodes are always expanded.
		if (beforeChild == null) {
			final InMemoryTreeNode added = node.add(0, newChild, visibility);
			allNodes.put(newChild, added);
		} else {
			final int index = node.indexOf(beforeChild);
			final InMemoryTreeNode added = node.add(index == -1 ? 0 : index, newChild,
					visibility);
			allNodes.put(newChild, added);
		}
		if (visibility) {
			internalDataSetChanged();
		}
	}

	@Override
	public synchronized void addAfterChild(final Long parent, final Long newChild,
			final Long afterChild) {
		expectNodeNotInTreeYet(newChild);
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(parent);
		final boolean visibility = getChildrenVisibility(node);
		if (afterChild == null) {
			final InMemoryTreeNode added = node.add(node.getChildrenListSize(), newChild,
					visibility);
			allNodes.put(newChild, added);
		} else {
			final int index = node.indexOf(afterChild);
			final InMemoryTreeNode added = node.add(
					index == -1 ? node.getChildrenListSize() : index + 1, newChild,
					visibility);
			allNodes.put(newChild, added);
		}
		if (visibility) {
			internalDataSetChanged();
		}
	}

	@Override
	public synchronized void removeNodeRecursively(final Long id) {
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		final boolean visibleNodeChanged = removeNodeRecursively(node);
		final Long parent = node.getParent();
		final InMemoryTreeNode parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
		parentNode.removeChild(id);
		if (visibleNodeChanged) {
			internalDataSetChanged();
		}
	}

	private boolean removeNodeRecursively(final InMemoryTreeNode node) {
		boolean visibleNodeChanged = false;
		for (final InMemoryTreeNode child : node.getChildren()) {
			if (removeNodeRecursively(child)) {
				visibleNodeChanged = true;
			}
		}
		node.clearChildren();
		if (node.getId() != null) {
			allNodes.remove(node.getId());
			if (node.isVisible()) {
				visibleNodeChanged = true;
			}
		}
		return visibleNodeChanged;
	}

	private void setChildrenVisibility(final InMemoryTreeNode node,
			final boolean visible, final boolean recursive) {
		for (final InMemoryTreeNode child : node.getChildren()) {
			child.setVisible(visible);
			if (recursive) {
				setChildrenVisibility(child, visible, true);
			}
		}
	}

	@Override
	public synchronized void expandDirectChildren(final Long id) {
		Log.d(InMemoryTreeStateManager.TAG, "Expanding direct children of " + id);
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		setChildrenVisibility(node, true, false);
		internalDataSetChanged();
	}

	@Override
	public synchronized void expandEverythingBelow(final Long id) {
		Log.d(InMemoryTreeStateManager.TAG, "Expanding all children below " + id);
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		setChildrenVisibility(node, true, true);
		internalDataSetChanged();
	}

	@Override
	public synchronized void collapseChildren(final Long id) {
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		if (node == topSentinel) {
			for (final InMemoryTreeNode n : topSentinel.getChildren()) {
				setChildrenVisibility(n, false, true);
			}
		} else {
			setChildrenVisibility(node, false, true);
		}
		internalDataSetChanged();
	}

	@Override
	public synchronized Long getNextSibling(final Long id) {
		final Long parent = getParent(id);
		final InMemoryTreeNode parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
		boolean returnNext = false;
		for (final InMemoryTreeNode child : parentNode.getChildren()) {
			if (returnNext) {
				return child.getId();
			}
			if (child.getId().equals(id)) {
				returnNext = true;
			}
		}
		return null;
	}

	@Override
	public synchronized Long getPreviousSibling(final Long id) {
		final Long parent = getParent(id);
		final InMemoryTreeNode parentNode = getNodeFromTreeOrThrowAllowRoot(parent);
		Long previousSibling = null;
		for (final InMemoryTreeNode child : parentNode.getChildren()) {
			if (child.getId().equals(id)) {
				return previousSibling;
			}
			previousSibling = child.getId();
		}
		return null;
	}

	@Override
	public synchronized boolean isInTree(final Long id) {
		return allNodes.containsKey(id);
	}

	@Override
	public synchronized int getVisibleCount() {
		return getVisibleList().size();
	}

	@Override
	public synchronized List<Long> getVisibleList() {
		Long currentId = null;
		if (visibleListCache == null) {
			visibleListCache = new ArrayList<Long>(allNodes.size());
			do {
				currentId = getNextVisible(currentId);
				if (currentId == null) {
					break;
				} else {
					visibleListCache.add(currentId);
				}
			} while (true);
		}
		if (unmodifiableVisibleList == null) {
			unmodifiableVisibleList = Collections.unmodifiableList(visibleListCache);
		}
		return unmodifiableVisibleList;
	}

	// @Override
	// public int getPositionInVisibleList(Long id) {
	// List<Long> visibleList = getVisibleList();
	// return visibleList.lastIndexOf(id);
	// }
	public synchronized Long getNextVisible(final Long id) {
		final InMemoryTreeNode node = getNodeFromTreeOrThrowAllowRoot(id);
		if (!node.isVisible()) {
			return null;
		}
		final List<InMemoryTreeNode> children = node.getChildren();
		if (!children.isEmpty()) {
			final InMemoryTreeNode firstChild = children.get(0);
			if (firstChild.isVisible()) {
				return firstChild.getId();
			}
		}
		final Long sibl = getNextSibling(id);
		if (sibl != null) {
			return sibl;
		}
		Long parent = node.getParent();
		do {
			if (parent == null) {
				return null;
			}
			final Long parentSibling = getNextSibling(parent);
			if (parentSibling != null) {
				return parentSibling;
			}
			parent = getNodeFromTreeOrThrow(parent).getParent();
		} while (true);
	}

	@Override
	public synchronized void registerDataSetObserver(final DataSetObserver observer) {
		observers.add(observer);
	}

	@Override
	public synchronized void unregisterDataSetObserver(final DataSetObserver observer) {
		observers.remove(observer);
	}

	@Override
	public int getLevel(final Long id) {
		return getNodeFromTreeOrThrow(id).getLevel();
	}

	@Override
	public Integer[] getHierarchyDescription(final Long id) {
		final int level = getLevel(id);
		final Integer[] hierarchy = new Integer[level + 1];
		int currentLevel = level;
		Long currentId = id;
		Long parent = getParent(currentId);
		while (currentLevel >= 0) {
			hierarchy[currentLevel--] = getChildren(parent).indexOf(currentId);
			currentId = parent;
			parent = getParent(parent);
		}
		return hierarchy;
	}

	private void appendToSb(final StringBuilder sb, final Long id) {
		if (id != null) {
			final TreeNodeInfo node = getNodeInfo(id);
			final int indent = node.getLevel() * 4;
			final char[] indentString = new char[indent];
			Arrays.fill(indentString, ' ');
			sb.append(indentString);
			sb.append(node.toString());
			sb.append(Arrays.asList(getHierarchyDescription(id)).toString());
			sb.append("\n");
		}
		final List<Long> children = getChildren(id);
		for (final Long child : children) {
			appendToSb(sb, child);
		}
	}

	@Override
	public synchronized String toString() {
		final StringBuilder sb = new StringBuilder();
		appendToSb(sb, null);
		return sb.toString();
	}

	@Override
	public synchronized void clear() {
		allNodes.clear();
		topSentinel.clearChildren();
		internalDataSetChanged();
	}

	@Override
	public void refresh() {
		internalDataSetChanged();
	}

	// @Override
	// public void setChoiceMode(final int choiceMode) {
	// if (mAbstractTreeViewAdapter.getTreeViewList().getChoiceMode() != choiceMode) {
	// selectedNodes.clear();
	// }
	// internalDataSetChanged();
	// }
	// @Override
	// public long[] getCheckedItemIds() {
	// long[] checkedItemIds = new long[selectedNodes.size()];
	// int i = 0;
	// for (Long selectedNode : selectedNodes) {
	// checkedItemIds[i++] = selectedNode;
	// }
	// return checkedItemIds;
	// }
	//
	// @Override
	// public void setCheckedItemIds(long[] idArray) {
	// for (long id : idArray) {
	// // first, ensure the node is in tree
	// getNodeFromTreeOrThrow(id);
	// selectedNodes.add(id);
	// }
	// internalDataSetChanged();
	// }
	//
	// @Override
	// public void setItemCheckedById(long id, boolean isChecked) {
	// if (mAbstractTreeViewAdapter.getTreeViewList().getChoiceMode() ==
	// TreeViewList.CHOICE_MODE_NONE) {
	// return;
	// }
	//
	// if (isChecked) {
	// // first, ensure the node is in tree
	// getNodeFromTreeOrThrow(id);
	//
	// if (mAbstractTreeViewAdapter.getTreeViewList().getChoiceMode() ==
	// TreeViewList.CHOICE_MODE_SINGLE) {
	// selectedNodes.clear();
	// }
	//
	// selectedNodes.add(id);
	// } else {
	// selectedNodes.remove(id);
	// }
	// internalDataSetChanged();
	// }
	//
	// @Override
	// public void setItemCheckedByPositionInVisibleList(int position,
	// boolean value) {
	// selectedNodes.add(getVisibleList().get(position));
	// }
	//
	// @Override
	// public boolean getItemCheckedByPositionInVisibleList(int position) {
	// return selectedNodes.contains(getVisibleList().get(position));
	// }
	//
	// @Override
	// public boolean isItemChecked(long id) {
	// return selectedNodes.contains(id);
	// }
	// @Override
	// public void setAbstractTreeViewAdapter(AbstractTreeViewAdapter adapter) {
	// mAbstractTreeViewAdapter = adapter;
	// }
	protected InMemoryTreeStateManager(Parcel in) {
		topSentinel = (InMemoryTreeNode) in.readValue(InMemoryTreeNode.class
				.getClassLoader());
		if (in.readByte() == 0x01) {
			visibleListCache = new ArrayList<Long>();
			in.readList(visibleListCache, Long.class.getClassLoader());
		} else {
			visibleListCache = null;
		}
		if (in.readByte() == 0x01) {
			unmodifiableVisibleList = new ArrayList<Long>();
			in.readList(unmodifiableVisibleList, Long.class.getClassLoader());
		} else {
			unmodifiableVisibleList = null;
		}
		visibleByDefault = in.readByte() != 0x00;
		// observers = (Set) in.readValue(Set.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(topSentinel);
		if (visibleListCache == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(visibleListCache);
		}
		if (unmodifiableVisibleList == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(unmodifiableVisibleList);
		}
		dest.writeByte((byte) (visibleByDefault ? 0x01 : 0x00));
		// dest.writeValue(observers);
	}

	public static final Parcelable.Creator<InMemoryTreeStateManager> CREATOR = new Parcelable.Creator<InMemoryTreeStateManager>() {
		@Override
		public InMemoryTreeStateManager createFromParcel(Parcel in) {
			return new InMemoryTreeStateManager(in);
		}

		@Override
		public InMemoryTreeStateManager[] newArray(int size) {
			return new InMemoryTreeStateManager[size];
		}
	};
}