package biz.advancedcalendar.fragments;

import java.util.List;

public class Node<T> {
	public T mTag;
	public long mId;
	public boolean mIsExpanded;
	public Node<T> mParent;
	public List<Node<T>> mChildren;

	public Node(T mTag, long mId, boolean mIsExpanded, Node<T> parent,
			List<Node<T>> mChildren) {
		super();
		this.mTag = mTag;
		this.mId = mId;
		this.mIsExpanded = mIsExpanded;
		this.mParent = parent;
		this.mChildren = mChildren;
	}

	public int getLevel() {
		int level = 0;
		Node<T> currentNode = this;
		while (currentNode.mParent != null) {
			level++;
			currentNode = currentNode.mParent;
		}
		return level;
	}
}