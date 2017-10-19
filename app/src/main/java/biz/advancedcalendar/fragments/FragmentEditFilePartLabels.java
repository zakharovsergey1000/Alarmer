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
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.views.accessories.FragmentViewTaskTreeAdapter;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;

public class FragmentEditFilePartLabels extends Fragment implements DataSaver {
	private TreeViewList mTreeViewList;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptionList = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (null != savedInstanceState) {
			// Restore state here
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
			List<Long> idList = Global.getFileToEditLabelIdList();
			for (Long id : idList) {
				mTreeViewList.setItemChecked(mTreeViewList.getPositionInVisibleList(id),
						true);
			}
		} else {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(),
							mManager, FragmentEditFilePartLabels.LEVEL_NUMBER);
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
		mTreeViewListItemDescriptionList.value = DataProvider.getLabelForest(
				null, getActivity(), null, null, false, true);
		mManager = new InMemoryTreeStateManager();
		final TreeBuilder treeBuilder = new TreeBuilder(mManager);
		for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptionList.value) {
			for (int i = 0; i < iterable_element.size(); i++) {
				treeBuilder.sequentiallyAddNextNode(iterable_element.get(i).getId(),
						iterable_element.get(i).getDeepLevel());
			}
		}
		List<Long> idList = Global.getFileToEditLabelIdList();
		long[] idArray = new long[idList.size()];
		for (int i = 0; i < idList.size(); i++) {
			idArray[i] = idList.get(i);
			mTreeViewList.setItemChecked(
					mManager.getVisibleList().lastIndexOf(idList.get(i)), true);
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
				FragmentEditFilePartLabels.LEVEL_NUMBER);
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
	}

	@Override
	public boolean isDataCollected() {
		// collect labels
		long[] idArray = mTreeViewList.getCheckedItemIds();
		ArrayList<Long> idList = new ArrayList<Long>();
		for (long id : idArray) {
			idList.add(id);
		}
		Global.setFileToEditLabelIdList(idList);
		return true;
	}
}
