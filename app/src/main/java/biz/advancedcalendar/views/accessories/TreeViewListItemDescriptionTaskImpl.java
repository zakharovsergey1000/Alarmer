package biz.advancedcalendar.views.accessories;

import android.os.Parcel;
import android.os.Parcelable;
import biz.advancedcalendar.greendao.Task;
import java.util.ArrayList;

public class TreeViewListItemDescriptionTaskImpl implements TreeViewListItemDescription,
		Parcelable {
	private Task task;

	public static class TreeViewListItemDescriptionRow implements Parcelable {
		ArrayList<TreeViewListItemDescription> treeViewListItemDescriptions;

		public TreeViewListItemDescriptionRow(
				ArrayList<TreeViewListItemDescription> treeViewListItemDescriptions) {
			this.treeViewListItemDescriptions = treeViewListItemDescriptions;
		}

		public ArrayList<TreeViewListItemDescription> getTreeViewListItemDescriptions() {
			return treeViewListItemDescriptions;
		}

		protected TreeViewListItemDescriptionRow(Parcel in) {
			if (in.readByte() == 0x01) {
				treeViewListItemDescriptions = new ArrayList<TreeViewListItemDescription>();
				in.readList(treeViewListItemDescriptions,
						TreeViewListItemDescription.class.getClassLoader());
			} else {
				treeViewListItemDescriptions = null;
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (treeViewListItemDescriptions == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(treeViewListItemDescriptions);
			}
		}

		public static final Parcelable.Creator<TreeViewListItemDescriptionRow> CREATOR = new Parcelable.Creator<TreeViewListItemDescriptionRow>() {
			@Override
			public TreeViewListItemDescriptionRow createFromParcel(Parcel in) {
				return new TreeViewListItemDescriptionRow(in);
			}

			@Override
			public TreeViewListItemDescriptionRow[] newArray(int size) {
				return new TreeViewListItemDescriptionRow[size];
			}
		};
	}

	public static class TreeViewListItemDescriptionMatrix implements Parcelable {
		private ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows;

		public TreeViewListItemDescriptionMatrix(
				ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows) {
			this.treeViewListItemDescriptionRows = treeViewListItemDescriptionRows;
		}

		public ArrayList<TreeViewListItemDescriptionRow> getTreeViewListItemDescriptionRows() {
			return treeViewListItemDescriptionRows;
		}

		public void setTreeViewListItemDescriptionRows(
				ArrayList<TreeViewListItemDescriptionRow> treeViewListItemDescriptionRows) {
			this.treeViewListItemDescriptionRows = treeViewListItemDescriptionRows;
		}

		protected TreeViewListItemDescriptionMatrix(Parcel in) {
			if (in.readByte() == 0x01) {
				treeViewListItemDescriptionRows = new ArrayList<TreeViewListItemDescriptionRow>();
				in.readList(treeViewListItemDescriptionRows,
						TreeViewListItemDescriptionRow.class.getClassLoader());
			} else {
				treeViewListItemDescriptionRows = null;
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			if (treeViewListItemDescriptionRows == null) {
				dest.writeByte((byte) 0x00);
			} else {
				dest.writeByte((byte) 0x01);
				dest.writeList(treeViewListItemDescriptionRows);
			}
		}

		public static final Parcelable.Creator<TreeViewListItemDescriptionMatrix> CREATOR = new Parcelable.Creator<TreeViewListItemDescriptionMatrix>() {
			@Override
			public TreeViewListItemDescriptionMatrix createFromParcel(Parcel in) {
				return new TreeViewListItemDescriptionMatrix(in);
			}

			@Override
			public TreeViewListItemDescriptionMatrix[] newArray(int size) {
				return new TreeViewListItemDescriptionMatrix[size];
			}
		};
	}

	public TreeViewListItemDescriptionTaskImpl(Task task) {
		this.task = task;
	}

	@Override
	public Long getId() {
		return task.getId();
	}

	// @Override
	// public void setId(Long id) {
	// this.id = id;
	// }
	@Override
	public String getDescription() {
		return task.getName();
	}

	// @Override
	// public void setDescription(String description) {
	// this.description = description;
	// }
	@Override
	public Long getParentId() {
		return task.getParentId();
	}

	// @Override
	// public void setParentId(Long parentId) {
	// this.parentId = parentId;
	// }
	@Override
	public short getDeepLevel() {
		return task.getDeepLevel();
	}

	// @Override
	// public void setDeepLevel(short level) {
	// this.level = level;
	// }
	@Override
	public int getSortOrder() {
		return task.getSortOrder();
	}

	// @Override
	// public void setSortOrder(int sortOrder) {
	// this.sortOrder = sortOrder;
	// }
	@Override
	public Task getTag() {
		return task;
	}

	// @Override
	// public void setTag(Object tag) {
	// this.tag = (Task) tag;
	// }
	protected TreeViewListItemDescriptionTaskImpl(Parcel in) {
		task = (Task) in.readValue(Task.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeValue(task);
	}

	public static final Parcelable.Creator<TreeViewListItemDescriptionTaskImpl> CREATOR = new Parcelable.Creator<TreeViewListItemDescriptionTaskImpl>() {
		@Override
		public TreeViewListItemDescriptionTaskImpl createFromParcel(Parcel in) {
			return new TreeViewListItemDescriptionTaskImpl(in);
		}

		@Override
		public TreeViewListItemDescriptionTaskImpl[] newArray(int size) {
			return new TreeViewListItemDescriptionTaskImpl[size];
		}
	};
}
