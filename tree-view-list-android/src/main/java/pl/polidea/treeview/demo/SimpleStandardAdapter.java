package pl.polidea.treeview.demo;

import java.util.Arrays;
import java.util.Set;
import pl.polidea.treeview.AbstractTreeViewAdapter;
import pl.polidea.treeview.R;
import pl.polidea.treeview.TreeNodeInfo;
import pl.polidea.treeview.TreeStateManager;
import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** This is a very simple adapter that provides very basic tree view with a checkboxes and
 * simple item description. */
public class SimpleStandardAdapter extends AbstractTreeViewAdapter {
	public SimpleStandardAdapter(final Activity treeViewListDemo,
			final Set<Long> selected, final TreeStateManager treeStateManager,
			final int numberOfLevels) {
		super(treeViewListDemo, treeStateManager, numberOfLevels);
	}

	private String getDescription(final long id) {
		final Integer[] hierarchy = getManager().getHierarchyDescription(id);
		return "Node " + id + Arrays.asList(hierarchy);
	}

	@Override
	public View getNewChildView(final TreeNodeInfo treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) getActivity().getLayoutInflater()
				.inflate(R.layout.demo_list_item, null);
		return updateView(viewLayout, treeNodeInfo);
	}

	@Override
	public LinearLayout updateView(final View view, final TreeNodeInfo treeNodeInfo) {
		final LinearLayout viewLayout = (LinearLayout) view;
		final TextView descriptionView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_description);
		final TextView levelView = (TextView) viewLayout
				.findViewById(R.id.demo_list_item_level);
		descriptionView.setText(getDescription(treeNodeInfo.getId()));
		levelView.setText(Integer.toString(treeNodeInfo.getLevel()));
		return viewLayout;
	}

	// @Override
	// public void handleItemClick(final View view, final Object id) {
	// //final Long longId = (Long) id;
	// //final TreeNodeInfo info = getManager().getNodeInfo(longId);
	// super.handleItemClick(view, id);
	// }
	@Override
	public long getItemId(final int position) {
		return getTreeId(position);
	}
}