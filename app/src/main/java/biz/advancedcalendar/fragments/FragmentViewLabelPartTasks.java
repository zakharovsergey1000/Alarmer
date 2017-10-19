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

public class FragmentViewLabelPartTasks extends Fragment {
	// private Set<Long> mSelected = new HashSet<Long>();
	private static final String TAG = ActivityMain.class.getSimpleName();
	private TreeViewList mTreeView;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mySimpleAdapter;
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
			// mySimpleAdapter.setmActionMode(null);
			mTreeView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_view_label_part_tasks, container,
				false);
		mTreeView = (TreeViewList) v
				.findViewById(R.id.fragment_view_label_part_tasks_treeview);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle b = getArguments();
		long id = b.getLong(CommonConstants.INTENT_EXTRA_ID);
		if (savedInstanceState == null) {
			mManager = new InMemoryTreeStateManager();
			final TreeBuilder treeBuilder = new TreeBuilder(mManager);
			mTreeViewListItemDescriptions.value = DataProvider
					.getTreeViewListItemDescriptionForest(null, getActivity()
									.getApplicationContext(), null, null, true, false, true, true);
			for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptions.value) {
				for (int i = 0; i < iterable_element.size(); i++) {
					treeBuilder.sequentiallyAddNextNode(iterable_element.get(i).getId(),
							iterable_element.get(i).getDeepLevel());
				}
			}
			Log.d(FragmentViewLabelPartTasks.TAG, mManager.toString());
			for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptions.value) {
				for (TreeViewListItemDescription treeViewListItemDescription : iterable_element) {
					mManager.collapseChildren(treeViewListItemDescription.getId());
				}
			}
			ArrayList<Long> tasksIds = DataProvider.getTaskIdListForLabel(null, getActivity()
							.getApplicationContext(), id, false, true, false, true, false);
			for (Long taskId : tasksIds) {
				// mSelected.add(taskId);
				// mTreeView.setItemCheckedById(taskId, true);
				mTreeView.setItemChecked(mManager.getVisibleList().lastIndexOf(taskId),
						true);
				Long currentTaskId = taskId;
				Long parentTaskId = mManager.getParent(currentTaskId);
				while (parentTaskId != null) {
					currentTaskId = parentTaskId;
					parentTaskId = mManager.getParent(currentTaskId);
				}
				mManager.expandEverythingBelow(currentTaskId);
			}
		} else {
			TreeStateManager manager1 = (TreeStateManager) savedInstanceState
					.getSerializable(CommonConstants.treeManager);
			// @SuppressWarnings("unchecked")
			// HashSet<Long> mSelected1 = ( HashSet<Long>) savedInstanceState
			// .getSerializable("mSelected");
			mManager = manager1;
			if (mManager == null) {
				mManager = new InMemoryTreeStateManager();
			}
			// mSelected = mSelected1;
			// if (mSelected == null) {
			// mSelected = new HashSet<Long>();
			// }
		}
		mySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
				FragmentViewLabelPartTasks.LEVEL_NUMBER);
		mTreeViewListItemDescriptions.value = DataProvider
				.getTreeViewListItemDescriptionForest(null, getActivity()
								.getApplicationContext(), null, null, true, false, true, true);
		mySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptions.value);
		mTreeView.setAdapter(mySimpleAdapter);
		// setCollapsible(newCollapsible);
		registerForContextMenu(mTreeView);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(CommonConstants.treeManager, mManager);
		// outState.putSerializable("mSelected", (Serializable) mSelected);
		super.onSaveInstanceState(outState);
	}
}
