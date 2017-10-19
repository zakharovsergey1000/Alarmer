package biz.advancedcalendar.fragments;

import java.util.ArrayList;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivityMain;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.views.accessories.FragmentViewTaskTreeAdapter;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;

public class FragmentViewTaskPartLabels extends Fragment {
	private static final String TREE_MANAGER_KEY = "biz.advancedcalendar.TREE_MANAGER_KEY";
	// private static final String TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY =
	// "biz.advancedcalendar.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY";
	private static final String TAG = ActivityMain.class.getSimpleName();
	private TreeViewList mTreeView;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptions;
	@SuppressLint("NewApi")
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		// Called when the action mode is created; startActionMode() was called
		@SuppressLint("NewApi")
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.fragment_view_task_tree_active_cab_menu, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@SuppressLint("NewApi")
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			return false;
		}

		// Called when the user exits the action mode
		@SuppressLint("NewApi")
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// int cnt = mTreeView.getCheckedItemCount();
			// long[] checkedItemIdArray = mTreeView.getCheckedItemIds();
			// mMySimpleAdapter.setmActionMode(null);
			mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_view_task_part_labels, container,
				false);
		mTreeView = (TreeViewList) v
				.findViewById(R.id.fragment_view_task_part_labels_treeview);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		long id = b.getLong(CommonConstants.INTENT_EXTRA_ID);
		mTreeViewListItemDescriptions.value = DataProvider.getLabelForest(null,
				getActivity(), null, null, false, true);
		if (savedInstanceState == null) {
			mManager = new InMemoryTreeStateManager();
			final TreeBuilder treeBuilder = new TreeBuilder(mManager);
			for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptions.value) {
				for (int i = 0; i < iterable_element.size(); i++) {
					treeBuilder.sequentiallyAddNextNode(iterable_element.get(i).getId(),
							iterable_element.get(i).getDeepLevel());
				}
			}
			Log.d(FragmentViewTaskPartLabels.TAG, mManager.toString());
			for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptions.value) {
				for (TreeViewListItemDescription treeViewListItemDescription : iterable_element) {
					mManager.collapseChildren(treeViewListItemDescription.getId());
				}
			}
			ArrayList<Long> idList = DataProvider.getLabelIdListForTask(null, getActivity()
							.getApplicationContext(), id, false, true, false);
			for (Long id1 : idList) {
				// mTreeView.setItemCheckedById(id1, true);
				mTreeView
						.setItemChecked(mManager.getVisibleList().lastIndexOf(id1), true);
				Long currentId = id1;
				Long parentId = mManager.getParent(currentId);
				while (parentId != null) {
					currentId = parentId;
					parentId = mManager.getParent(currentId);
				}
				mManager.expandEverythingBelow(currentId);
			}
		} else {
			TreeStateManager manager1 = (TreeStateManager) savedInstanceState
					.getSerializable(FragmentViewTaskPartLabels.TREE_MANAGER_KEY);
			mManager = manager1;
			if (mManager == null) {
				mManager = new InMemoryTreeStateManager();
			}
		}
		mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
				FragmentViewTaskPartLabels.LEVEL_NUMBER);
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptions.value);
		mTreeView.setAdapter(mMySimpleAdapter);
		registerForContextMenu(mTreeView);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(FragmentViewTaskPartLabels.TREE_MANAGER_KEY, mManager);
		super.onSaveInstanceState(outState);
	}
}
