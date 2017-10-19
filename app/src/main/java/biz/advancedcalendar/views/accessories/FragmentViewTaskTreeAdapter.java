package biz.advancedcalendar.views.accessories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.view.View;
import android.widget.TextView;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.greendao.Task;
import biz.advancedcalendar.greendao.Task.SyncStatus;
import biz.advancedcalendar.utils.Helper;

/** This is a very simple adapter that provides very basic tree view with a checkboxes and
 * simple item description. */
public class FragmentViewTaskTreeAdapter extends AbstractTreeViewAdapter {
	private Map<Long, TreeViewListItemDescription> mTreeViewListItemDescriptions;

	// private Integer mResource;
	// private Integer mTextViewResourceId;
	public FragmentViewTaskTreeAdapter(final Activity treeViewListDemo,
			final TreeStateManager treeStateManager, final int numberOfLevels) {
		super(treeViewListDemo, treeStateManager, numberOfLevels);
		mTreeViewListItemDescriptions = new HashMap<Long, TreeViewListItemDescription>();
	}

	@Override
	public View getNewChildView(final TreeNodeInfo treeNodeInfo) {
		final View viewLayout = getActivity().getLayoutInflater().inflate(
				R.layout.task_tree_view_list_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public View updateView(final View view, final TreeNodeInfo treeNodeInfo) {
		TextView tv = (TextView) view
				.findViewById(R.id.task_treeview_list_item_textview_description);
		String text;
		switch (SyncStatus.fromInt(((Task) mTreeViewListItemDescriptions.get(
				treeNodeInfo.getId()).getTag()).getSyncStatusValue())) {
		case SYNCHRONIZED:
		default:
			text = mTreeViewListItemDescriptions.get(treeNodeInfo.getId())
					.getDescription();
			break;
		case SYNC_UP_REQUIRED:
			text = "\u2191"
					+ mTreeViewListItemDescriptions.get(treeNodeInfo.getId())
							.getDescription();
			break;
		case SYNC_DOWN_REQUIRED:
			text = "\u2193"
					+ mTreeViewListItemDescriptions.get(treeNodeInfo.getId())
							.getDescription();
			break;
		}
		int backgroundColor = ((Task) mTreeViewListItemDescriptions.get(
				treeNodeInfo.getId()).getTag()).getColor2(getActivity());
		int textColor;
		// Helper. getContrast50(backgroundColor);
		if (Helper.getContrastYIQ(backgroundColor)) {
			textColor = getActivity().getResources().getColor(
					R.color.task_view_text_synchronized_dark);
		} else {
			textColor = getActivity().getResources().getColor(
					R.color.task_view_text_synchronized_light);
		}
		tv.setBackgroundColor(backgroundColor);
		tv.setTextColor(textColor);
		tv.setText(text);
		view.setTag(treeNodeInfo.getId());
		return view;
	}

	@Override
	public long getItemId(final int position) {
		return getTreeId(position);
	}

	public void setTreeViewListItemDescriptions(
			ArrayList<ArrayList<TreeViewListItemDescription>> treeViewListItemDescriptions) {
		mTreeViewListItemDescriptions.clear();
		for (ArrayList<TreeViewListItemDescription> item2 : treeViewListItemDescriptions) {
			for (TreeViewListItemDescription item : item2) {
				mTreeViewListItemDescriptions.put(item.getId(), item);
			}
		}
	}
}