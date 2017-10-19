package biz.advancedcalendar.fragments;

import java.util.ArrayList;
import pl.polidea.treeview.InMemoryTreeStateManager;
import pl.polidea.treeview.TreeBuilder;
import pl.polidea.treeview.TreeStateManager;
import pl.polidea.treeview.TreeViewList;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.views.accessories.FragmentViewTaskTreeAdapter;
import biz.advancedcalendar.views.accessories.TreeViewListItemDescription;

public class FragmentEditLabelPartTasks extends Fragment implements DataSaver {
	private static final String TREE_MANAGER_KEY = "biz.advancedcalendar.TREE_MANAGER_KEY";
	private static final String TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY = "biz.advancedcalendar.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY";
	private TreeViewList mTreeViewList;
	private static final int LEVEL_NUMBER = 4;
	private TreeStateManager mManager = null;
	private FragmentViewTaskTreeAdapter mMySimpleAdapter = null;
	private TreeViewListItemDescriptionArrayListOfArrayLists mTreeViewListItemDescriptionList;
	ActionMode mActionMode;
	private BroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						CommonConstants.ACTION_ENTITIES_CHANGED_TASKS)) {
					// invalidate adapter
					mMySimpleAdapter = null;
					mManager = null;
					if (isAdded()) {
						if (getActivity() == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						fillAdapter();
						mTreeViewList.setAdapter(mMySimpleAdapter);
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mTreeViewList = (TreeViewList) inflater.inflate(R.layout.tree_view_list,
				container, false);
		mTreeViewList.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		mTreeViewList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View v, int position,
					long id) {
				// Toast.makeText(getActivity().getApplicationContext(),
				// "Click TreeItem id " + id, Toast.LENGTH_LONG).show();
				// launchTaskEditor(id);
			}
		});
		return mTreeViewList;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (Global.getLabelToEditTaskIdList() == null) {
			ArrayList<Long> idList = DataProvider.getTaskIdListForLabel(null, getActivity()
							.getApplicationContext(), Global.getLabelToEdit().getId(),
					false, true, false, true, false);
			Global.setLabelToEditTaskIdList(idList);
		}
		LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
				.registerReceiver(receiver,
						new IntentFilter(CommonConstants.ACTION_ENTITIES_CHANGED_TASKS));
		if (savedInstanceState == null) {
			if (mTreeViewList.getAdapter() == null) {
				if (mMySimpleAdapter == null) {
					fillAdapter();
				}
				mTreeViewList.setAdapter(mMySimpleAdapter);
				for (long id : Global.getLabelToEditTaskIdList()) {
					int position = mTreeViewList.getPositionInVisibleList(id);
					if (position >= 0) {
						mTreeViewList.setItemChecked(position, true);
					}
				}
			}
		} else {
			mTreeViewListItemDescriptionList = savedInstanceState
					.getParcelable(FragmentEditLabelPartTasks.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY);
			TreeStateManager manager1 = (TreeStateManager) savedInstanceState
					.getParcelable(FragmentEditLabelPartTasks.TREE_MANAGER_KEY);
			mManager = manager1;
			mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
					FragmentEditLabelPartTasks.LEVEL_NUMBER);
			mMySimpleAdapter
					.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
			mTreeViewList.setAdapter(mMySimpleAdapter);
			for (long id : Global.getLabelToEditTaskIdList()) {
				mTreeViewList.setItemChecked(mTreeViewList.getPositionInVisibleList(id),
						true);
			}
		}
		// mTreeView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
		//
		// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
		// @Override
		// public void onItemCheckedStateChanged(ActionMode mode,
		// int position, long id, boolean checked) {
		// // Here you can do something when items are
		// // selected/de-selected,
		// // such as update the title in the CAB
		// }
		//
		// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
		// @Override
		// public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		// // Respond to clicks on the actions in the CAB
		// return true;
		// }
		//
		// @TargetApi(Build.VERSION_CODES.HONEYCOMB)
		// @Override
		// public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// // Inflate the menu for the CAB
		// mActionMode = mode;
		// // MenuInflater inflater = mode.getMenuInflater();
		// // Bundle b = getArguments();
		// // int menuId = 0;
		// // if (b != null) {
		// // menuId = b.getInt(CommonConstants.MENU_ID, 0);
		// // }
		// //
		// // inflater.inflate(menuId, menu);
		// return true;
		// }
		//
		// @Override
		// public void onDestroyActionMode(ActionMode mode) {
		// // Here you can make any necessary updates to the activity
		// // when
		// // the CAB is removed. By default, selected items are
		// // deselected/unchecked.
		// long[] idArray = mTreeView.getCheckedItemIds();
		// ArrayList<Long> idList = new ArrayList<Long>();
		// for (int i = 0; i < idArray.length; i++) {
		// idList.add(idArray[i]);
		// }
		// Global.setLabelToEditTaskIdList(idList);
		//
		// // Define the Handler that receives messages from the thread and
		// // update the progress
		// Handler handler = new Handler();
		// handler.post(new Runnable() {
		//
		// @Override
		// public void run() {
		// for (long id : Global.getLabelToEditTaskIdList()) {
		//
		// mTreeView.setItemChecked(
		// mTreeView.getPositionInVisibleList(id),
		// true);
		//
		// }
		//
		// }
		// });
		//
		// }
		//
		// @Override
		// public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// // Here you can perform updates to the CAB due to
		// // an invalidate() request
		// return false;
		// }
		// });
	}

	@Override
	public void onStart() {
		super.onStart();
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
	public void onDestroy() {
		super.onDestroy();
		if (getActivity().isFinishing()) {
			// DataProvider.removeDataChangedListener(this);
			LocalBroadcastManager.getInstance(getActivity().getApplicationContext())
					.unregisterReceiver(receiver);
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(FragmentEditLabelPartTasks.TREE_MANAGER_KEY, mManager);
		outState.putParcelable(
				FragmentEditLabelPartTasks.TREE_VIEW_LIST_ITEM_DESCRIPTION_LIST_KEY,
				mTreeViewListItemDescriptionList);
		isDataCollected();
		super.onSaveInstanceState(outState);
	}

	/**
	 *
	 */
	private void fillAdapter() {
		// List<Long> rootList = new ArrayList<Long>(1);
		// rootList.add(Global.getLabelToEdit().getId());
		mTreeViewListItemDescriptionList.value = DataProvider
				.getTreeViewListItemDescriptionForest(null, getActivity()
								.getApplicationContext(), null, null, true, false, true, false);
		mManager = new InMemoryTreeStateManager();
		final TreeBuilder treeBuilder = new TreeBuilder(mManager);
		for (ArrayList<TreeViewListItemDescription> iterable_element : mTreeViewListItemDescriptionList.value) {
			for (int i = 0; i < iterable_element.size(); i++) {
				treeBuilder.sequentiallyAddNextNode(iterable_element.get(i).getId(),
						iterable_element.get(i).getDeepLevel());
			}
		}
		// long[] idArray = new long[idList.size()];
		// for (int i = 0; i < idList.size(); i++) {
		// idArray[i] = idList.get(i);
		// }
		//
		// long[] idArray = new long[rootList.size()];
		// for (int i = 0; i < rootList.size(); i++) {
		// idArray[i] = rootList.get(i);
		// }
		// mManager.setCheckedItemIds(idArray);
		mMySimpleAdapter = new FragmentViewTaskTreeAdapter(getActivity(), mManager,
				FragmentEditLabelPartTasks.LEVEL_NUMBER);
		mMySimpleAdapter
				.setTreeViewListItemDescriptions(mTreeViewListItemDescriptionList.value);
	}

	@Override
	public boolean isDataCollected() {
		// collect tasks
		final long[] idArray = mTreeViewList.getCheckedItemIds();
		ArrayList<Long> idList = new ArrayList<Long>();
		for (long id : idArray) {
			idList.add(id);
		}
		Global.setLabelToEditTaskIdList(idList);
		return true;
	}
}
