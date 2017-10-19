package biz.advancedcalendar.fragments;

import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;

public class FragmentEditTaskPartLabels extends Fragment implements DataSaver {
	private TreeViewList mTreeViewList;
	// private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	// private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptionList = null;

	// private int mSelectMode;
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(CommonConstants.DEBUG_TAG, "onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We could lazily load the list in onResume() but lets load it
		// here to minimize latency when we will actually view this fragment
		// This is because the fragment could be attached to the activity but
		// not viewed at the time
		// TaskWithDependents entityWithDependencies = ((TaskWithDependentsHolder)
		// getActivity())
		// .getTaskWithDependents();
		// // if (entityWithDependencies.labelIdList == null) {
		// // entityWithDependencies.labelIdList = DataProvider.getLabelIdListForTask(
		// // getActivity(), entityWithDependencies.task.getId(), false, true,
		// // false);
		// // }
		// // Restore state here
		// if (savedInstanceState != null) {
		// mTreeViewListItemDescriptionList = savedInstanceState
		// .getParcelable(CommonConstants.treeViewListItemDescriptionList);
		// mManager = (TreeStateManager) savedInstanceState
		// .getParcelable(CommonConstants.treeManager);
		// }
		// Log.i(CommonConstants.DEBUG_TAG, "onCreate");
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
		// TaskWithDependents entityWithDependencies = ((TaskWithDependentsHolder)
		// getActivity())
		// .getTaskWithDependents();
		// super.onActivityCreated(savedInstanceState);
		// Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		// if (savedInstanceState == null) {
		// if (mTreeViewList.getAdapter() == null) {
		// if (mMySimpleAdapter == null) {
		// fillAdapter();
		// }
		// mTreeViewList.setAdapter(mMySimpleAdapter);
		// }
		// mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		// // for (Long id : entityWithDependencies.labelIdList) {
		// // mTreeViewList.setItemChecked(mTreeViewList.getPositionInVisibleList(id),
		// // true);
		// // }
		// } else {
		// if (mTreeViewList.getAdapter() == null) {
		// if (mMySimpleAdapter == null) {
		// mMySimpleAdapter = new MyTaskTreeAdapter(getActivity(), mManager,
		// FragmentEditTaskPartLabels.LEVEL_NUMBER,
		// android.R.layout.simple_list_item_multiple_choice);
		// mMySimpleAdapter
		// .setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
		// }
		// mTreeViewList.setAdapter(mMySimpleAdapter);
		// }
		// mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		// }
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
		Log.i(CommonConstants.DEBUG_TAG, "onSaveInstanceState");
	}

	@Override
	public boolean isDataCollected() {
		// collect labels
		// TaskWithDependents entityWithDependencies = ((TaskWithDependentsHolder)
		// getActivity())
		// .getTaskWithDependents();
		// // contactWithDependencies.taskIdList should never be null here
		// // entityWithDependencies.labelIdList.clear();
		// long[] idArray = mTreeViewList.getCheckedItemIds();
		// for (long id : idArray) {
		// // entityWithDependencies.labelIdList.add(id);
		// }
		return true;
	}
	// /**
	// *
	// */
	// private void fillAdapter() {
	// mTreeViewListItemDescriptionList.value = DataProvider.getLabelForest(
	// getActivity(), null, null, false, true);
	// mManager = new InMemoryTreeStateManager();
	// final TreeBuilder treeBuilder = new TreeBuilder(mManager);
	// for (ArrayList<TreeViewListItemDescription> tree :
	// mTreeViewListItemDescriptionList.value) {
	// for (TreeViewListItemDescription treeViewListItemDescription : tree) {
	// treeBuilder.sequentiallyAddNextNode(treeViewListItemDescription.getId(),
	// treeViewListItemDescription.getDeepLevel());
	// }
	// }
	// // List<Long> idList = ((TaskWithDependenciesHolder) getActivity())
	// // .getTaskWithDependencies().labelIdList;
	// // for (Long id : idList) {
	// // mTreeViewList.setItemChecked(mManager.getVisibleList().lastIndexOf(id), true);
	// // }
	// // // mManager.setCheckedItemIds(idArray);
	// // for (long id : idList) {
	// // long currentId = id;
	// // Long parentId = mManager.getParent(currentId);
	// // while (parentId != null) {
	// // currentId = parentId;
	// // parentId = mManager.getParent(currentId);
	// // }
	// // mManager.expandEverythingBelow(currentId);
	// // }
	// mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
	// FragmentEditTaskPartLabels.LEVEL_NUMBER);
	// mMySimpleAdapter
	// .setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
	// }
}
