package biz.advancedcalendar.fragments;

import android.app.Activity;
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
import java.util.ArrayList;
import java.util.List;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;

public class FragmentEditContactPartLabels extends Fragment implements DataSaver {
	private static final String TREE_MANAGER_KEY = "biz.advancedcalendar.TREE_MANAGER_KEY";
	private static final String TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY = "biz.advancedcalendar.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY";
	private TreeViewList mTreeViewList;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptionList = null;

	// private Long mEntityToEditId = null;
	// private List<Long> mRelatedEntityIdList;
	// private int mSelectMode;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(CommonConstants.DEBUG_TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		if (contactWithDependencies.labelIdList == null) {
			contactWithDependencies.labelIdList = DataProvider.getLabelIdListForContact(
					null, getActivity(), contactWithDependencies.contact.getId(), false,
					true, false);
		}
		// Restore state here
		if (savedInstanceState != null) {
			mTreeViewListItemDescriptionList = savedInstanceState
					.getParcelable(FragmentEditContactPartLabels.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY);
			mManager = (TreeStateManager) savedInstanceState
					.getSerializable("treeManager");
		}
		Log.i(CommonConstants.DEBUG_TAG, "onCreate");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i(CommonConstants.DEBUG_TAG, "onStart");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(CommonConstants.DEBUG_TAG, "onResume");
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(CommonConstants.DEBUG_TAG, "onPause");
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
			for (Long id : contactWithDependencies.labelIdList) {
				mTreeViewList.setItemChecked(mTreeViewList.getPositionInVisibleList(id),
						true);
			}
		} else {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(),
							mManager, FragmentEditContactPartLabels.LEVEL_NUMBER);
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
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(FragmentEditContactPartLabels.TREE_MANAGER_KEY, mManager);
		outState.putParcelable(
				FragmentEditContactPartLabels.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY,
				mTreeViewListItemDescriptionList);
		super.onSaveInstanceState(outState);
		Log.i(CommonConstants.DEBUG_TAG, "onSaveInstanceState");
	}

	/**
	 *
	 */
	private void fillAdapter() {
		mTreeViewListItemDescriptionList.value = DataProvider.getLabelForest(null,
				getActivity().getApplicationContext(), null, null, false, true);
		mManager = new InMemoryTreeStateManager();
		final TreeBuilder treeBuilder = new TreeBuilder(mManager);
		for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptionList.value) {
			for (int i = 0; i < iterable_element.size(); i++) {
				treeBuilder.sequentiallyAddNextNode(iterable_element.get(i).getId(),
						iterable_element.get(i).getDeepLevel());
			}
		}
		List<Long> idList = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies().labelIdList;
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
				FragmentEditContactPartLabels.LEVEL_NUMBER);
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
	}

	@Override
	public boolean isDataCollected() {
		// collect labels
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		// contactWithDependencies.taskIdList should never be null here
		contactWithDependencies.labelIdList.clear();
		long[] idArray = mTreeViewList.getCheckedItemIds();
		for (long id : idArray) {
			contactWithDependencies.labelIdList.add(id);
		}
		return true;
	}
}
