package biz.advancedcalendar.fragments;

import java.util.ArrayList;
import java.util.List;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.ContactWithDependents;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.views.accessories.FragmentViewTaskTreeAdapter;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;

public class FragmentEditContactPartTasks extends Fragment implements DataSaver {
	private TreeViewList mTreeViewList;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptionList = null;

	// private Long mEntityToEditId = null;
	// private List<Long> mRelatedEntityIdList;
	// private int mSelectMode;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We could lazily load the list in onResume() but lets load it
		// here to minimize latency when we will actually view this fragment
		// This is because the fragment could be attached to the activity but
		// not viewed at the time
		ContactWithDependents entityWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		if (entityWithDependencies.taskIdList == null) {
			entityWithDependencies.taskIdList = DataProvider.getTaskIdListForContact(
					null, getActivity(), entityWithDependencies.contact.getId(), false,
					true, false, true, false);
		}
		// Restore state here
		if (savedInstanceState != null) {
			mTreeViewListItemDescriptionList = savedInstanceState
					.getParcelable(CommonConstants.treeViewListItemDescriptionList);
			mManager = (TreeStateManager) savedInstanceState
					.getParcelable(CommonConstants.treeManager);
		}
		Log.i(CommonConstants.DEBUG_TAG, "onCreate");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(CommonConstants.DEBUG_TAG, "onCreateView");
		mTreeViewList = (TreeViewList) inflater.inflate(R.layout.tree_view_list,
				container, false);
		return mTreeViewList;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		super.onActivityCreated(savedInstanceState);
		Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		if (savedInstanceState == null) {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					fillAdapter();
				}
				mTreeViewList.setAdapter(mMySimpleAdapter);
			}
			mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			for (Long id : contactWithDependencies.taskIdList) {
				mTreeViewList.setItemChecked(mTreeViewList.getPositionInVisibleList(id),
						true);
			}
		} else {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(),
							mManager, FragmentEditContactPartTasks.LEVEL_NUMBER);
					mMySimpleAdapter
							.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
				}
				mTreeViewList.setAdapter(mMySimpleAdapter);
			}
			mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
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
	public void onDestroyView() {
		super.onDestroyView();
		Log.i(CommonConstants.DEBUG_TAG, "onDestroyView");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(CommonConstants.DEBUG_TAG, "onDestroy");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(CommonConstants.DEBUG_TAG, "onDetach");
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(CommonConstants.treeManager, mManager);
		outState.putParcelable(CommonConstants.treeViewListItemDescriptionList,
				mTreeViewListItemDescriptionList);
		super.onSaveInstanceState(outState);
		Log.i(CommonConstants.DEBUG_TAG, "onSaveInstanceState");
	}

	/**
	 *
	 */
	private void fillAdapter() {
		mTreeViewListItemDescriptionList.value = DataProvider
				.getTreeViewListItemDescriptionForest(null, getActivity(), null, null,
						true, false, true, false);
		mManager = new InMemoryTreeStateManager();
		final TreeBuilder treeBuilder = new TreeBuilder(mManager);
		for (ArrayList<TreeViewListItemDescription> tree : mTreeViewListItemDescriptionList.value) {
			for (TreeViewListItemDescription treeViewListItemDescription : tree) {
				treeBuilder.sequentiallyAddNextNode(treeViewListItemDescription.getId(),
						treeViewListItemDescription.getDeepLevel());
			}
		}
		List<Long> idList = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies().taskIdList;
		for (Long id : idList) {
			mTreeViewList.setItemChecked(mManager.getVisibleList().lastIndexOf(id), true);
		}
		// mManager.setCheckedItemIds(idArray);
		for (long id : idList) {
			long currentId = id;
			Long parentId = mManager.getParent(currentId);
			while (parentId != null) {
				currentId = parentId;
				parentId = mManager.getParent(currentId);
			}
			mManager.expandEverythingBelow(currentId);
		}
		mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
				FragmentEditContactPartTasks.LEVEL_NUMBER);
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
	}

	@Override
	public boolean isDataCollected() {
		// collect tasks
		ContactWithDependents entityWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		// contactWithDependencies.taskIdList should never be null here
		entityWithDependencies.taskIdList.clear();
		long[] idArray = mTreeViewList.getCheckedItemIds();
		for (long id : idArray) {
			entityWithDependencies.taskIdList.add(id);
		}
		return true;
	}
}
