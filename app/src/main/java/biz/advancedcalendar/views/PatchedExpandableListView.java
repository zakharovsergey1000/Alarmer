package biz.advancedcalendar.views;

import java.util.ArrayList;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.widget.ExpandableListView;

public class PatchedExpandableListView extends ExpandableListView {
	ArrayList<ArrayList<Integer>> mCheckedItemPositions;
	private ActionMode mActionMode;

	public PatchedExpandableListView(Context context) {
		super(context);
	}

	public PatchedExpandableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PatchedExpandableListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PatchedExpandableListView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		// super(context, attrs, defStyleAttr, defStyleRes);
		super(context, attrs, defStyleAttr);
		// mCheckedItemPositions = new ArrayList<ArrayList<Integer>>();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState, mCheckedItemPositions);
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.mSuperState);
		mCheckedItemPositions = ss.mCheckedItemPositions;
	}

	@Override
	public ActionMode startActionMode(ActionMode.Callback c) {
		mActionMode = super.startActionMode(c);
		return mActionMode;
	}

	public static class SavedState implements Parcelable {
		Parcelable mSuperState;
		ArrayList<ArrayList<Integer>> mCheckedItemPositions;

		public SavedState(Parcelable superState,
				ArrayList<ArrayList<Integer>> checkedItemPositions) {
			mSuperState = superState;
			mCheckedItemPositions = checkedItemPositions;
		}

		protected SavedState(Parcel in) {
			int size = in.readInt();
			mCheckedItemPositions = new ArrayList<ArrayList<Integer>>();
			for (int i = 0; i < size; i++) {
				mCheckedItemPositions.add(new ArrayList<Integer>());
				int size2 = in.readInt();
				for (int i1 = 0; i1 < size2; i1++) {
					mCheckedItemPositions.get(i).add(in.readInt());
				}
			}
		}

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			int size = mCheckedItemPositions.size();
			dest.writeInt(size);
			for (int i = 0; i < size; i++) {
				ArrayList<Integer> al = mCheckedItemPositions.get(i);
				int size2 = al.size();
				for (int i1 = 0; i1 < size2; i1++) {
					dest.writeInt(al.get(i1));
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
