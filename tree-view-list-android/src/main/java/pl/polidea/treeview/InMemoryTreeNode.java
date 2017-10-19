package pl.polidea.treeview;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

/** Node. It is package protected so that it cannot be used outside.
 * 
 * @param type
 *            of the identifier used by the tree */
class InMemoryTreeNode implements Parcelable {
	private static final long serialVersionUID = 1L;
	private final Long id;
	private final Long parent;
	private final int level;
	private boolean visible = true;
	private List<InMemoryTreeNode> children = new LinkedList<InMemoryTreeNode>();
	private List<Long> childIdListCache = null;

	public InMemoryTreeNode(final Long id, final Long parent, final int level,
			final boolean visible) {
		super();
		this.id = id;
		this.parent = parent;
		this.level = level;
		this.visible = visible;
	}

	public int indexOf(final Long id) {
		return getChildIdList().indexOf(id);
	}

	/** Cache is built lasily only if needed. The cache is cleaned on any structure change
	 * for that node!).
	 * 
	 * @return list of ids of children */
	public synchronized List<Long> getChildIdList() {
		if (childIdListCache == null) {
			childIdListCache = new LinkedList<Long>();
			for (final InMemoryTreeNode n : children) {
				childIdListCache.add(n.getId());
			}
		}
		return childIdListCache;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public int getChildrenListSize() {
		return children.size();
	}

	public synchronized InMemoryTreeNode add(final int index, final Long child,
			final boolean visible) {
		childIdListCache = null;
		// Note! top levell children are always visible (!)
		final InMemoryTreeNode newNode = new InMemoryTreeNode(child, getId(),
				getLevel() + 1, getId() == null ? true : visible);
		children.add(index, newNode);
		return newNode;
	}

	/** Note. This method should technically return unmodifiable collection, but for
	 * performance reason on small devices we do not do it.
	 * 
	 * @return children list */
	public List<InMemoryTreeNode> getChildren() {
		return children;
	}

	public synchronized void clearChildren() {
		children.clear();
		childIdListCache = null;
	}

	public synchronized void removeChild(final Long child) {
		final int childIndex = indexOf(child);
		if (childIndex != -1) {
			children.remove(childIndex);
			childIdListCache = null;
		}
	}

	@Override
	public String toString() {
		return "InMemoryTreeNode [id=" + getId() + ", parent=" + getParent() + ", level="
				+ getLevel() + ", visible=" + visible + ", children=" + children
				+ ", childIdListCache=" + childIdListCache + "]";
	}

	Long getId() {
		return id;
	}

	Long getParent() {
		return parent;
	}

	int getLevel() {
		return level;
	}

	protected InMemoryTreeNode(Parcel in) {
		id = in.readByte() == 0x00 ? null : in.readLong();
		parent = in.readByte() == 0x00 ? null : in.readLong();
		level = in.readInt();
		visible = in.readByte() != 0x00;
		if (in.readByte() == 0x01) {
			children = new ArrayList<InMemoryTreeNode>();
			in.readList(children, InMemoryTreeNode.class.getClassLoader());
		} else {
			children = null;
		}
		if (in.readByte() == 0x01) {
			childIdListCache = new ArrayList<Long>();
			in.readList(childIdListCache, Long.class.getClassLoader());
		} else {
			childIdListCache = null;
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if (id == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(id);
		}
		if (parent == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeLong(parent);
		}
		dest.writeInt(level);
		dest.writeByte((byte) (visible ? 0x01 : 0x00));
		if (children == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(children);
		}
		if (childIdListCache == null) {
			dest.writeByte((byte) 0x00);
		} else {
			dest.writeByte((byte) 0x01);
			dest.writeList(childIdListCache);
		}
	}

	public static final Parcelable.Creator<InMemoryTreeNode> CREATOR = new Parcelable.Creator<InMemoryTreeNode>() {
		@Override
		public InMemoryTreeNode createFromParcel(Parcel in) {
			return new InMemoryTreeNode(in);
		}

		@Override
		public InMemoryTreeNode[] newArray(int size) {
			return new InMemoryTreeNode[size];
		}
	};
}