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
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.views.accessories.FragmentViewTaskTreeAdapter;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;

public class FragmentEditLabelPartParent extends Fragment implements DataSaver {
	private TreeViewList mTreeViewList;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptionList = null;
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
			mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTreeViewList = (TreeViewList) inflater.inflate(R.layout.tree_view_list,
				container, false);
		return mTreeViewList;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		if (savedInstanceState == null) {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					fillAdapter();
				}
				mTreeViewList.setAdapter(mMySimpleAdapter);
			}
		} else {
			mTreeViewListItemDescriptionList = savedInstanceState
					.getParcelable(CommonConstants.treeViewListItemDescriptionList);
			TreeStateManager manager1 = (TreeStateManager) savedInstanceState
					.getParcelable(CommonConstants.treeManager);
			mManager = manager1;
			mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
					FragmentEditLabelPartParent.LEVEL_NUMBER);
			mMySimpleAdapter
					.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
			mTreeViewList.setAdapter(mMySimpleAdapter);
		}
		mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
		Long parentId = Global.getLabelToEdit().getLocalParentId();
		if (parentId != null) {
			mTreeViewList.setItemChecked(mManager.getVisibleList().lastIndexOf(parentId),
					true);
		}
	}

	@Override
	public void onStop() {
		// collect info
		if (!getActivity().isFinishing()) {
			// collect info
			isDataCollected();
		}
		super.onStop();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(CommonConstants.treeManager, mManager);
		outState.putParcelable(CommonConstants.treeViewListItemDescriptionList,
				mTreeViewListItemDescriptionList);
		super.onSaveInstanceState(outState);
	}

	/**
	 *
	 */
	private void fillAdapter() {
		Bundle b = getArguments();
		Long excludeSubtree = null;
		if (b != null && b.containsKey(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE)) {
			excludeSubtree = b.getLong(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE);
		}
		mTreeViewListItemDescriptionList.value = DataProvider.getLabelForest(
				null, getActivity().getApplicationContext(), null, excludeSubtree, false, true);
		mManager = new InMemoryTreeStateManager();
		final TreeBuilder treeBuilder = new TreeBuilder(mManager);
		for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptionList.value) {
			for (int i = 0; i < iterable_element.size(); i++) {
				treeBuilder.sequentiallyAddNextNode(iterable_element.get(i).getId(),
						iterable_element.get(i).getDeepLevel());
			}
		}
		mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
				FragmentEditLabelPartParent.LEVEL_NUMBER);
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
	}

	@Override
	public boolean isDataCollected() {
		// collect labels
		long[] checkedItemIdArray = mTreeViewList.getCheckedItemIds();
		if (checkedItemIdArray.length > 0) {
			Global.getLabelToEdit().setLocalParentId(checkedItemIdArray[0]);
		}
		return true;
	}
}
