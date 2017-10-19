package biz.advancedcalendar.fragments;

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

public class FragmentEditTaskPartFiles extends Fragment implements DataSaver {
	View mView;

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
		// if (entityWithDependencies.fileIdList == null) {
		// entityWithDependencies.fileIdList = DataProvider
		// .getFileIdListForTask(getActivity(),
		// entityWithDependencies.task.getId(), false, true,
		// false);
		// }
		// Restore state here
		if (savedInstanceState != null) {
			// mTreeViewListItemDescriptionList =
			// (ArrayList<ArrayList<TreeViewListItemDescription>>)
			// savedInstanceState
			// .getSerializable("treeViewListItemDescriptionList");
			//
			// mManager = (TreeStateManager) savedInstanceState
			// .getSerializable("treeManager");
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
		mView = inflater.inflate(R.layout.fragment_list, container, false);
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TaskWithDependents contactWithDependencies = ((TaskWithDependentsHolder)
		// getActivity())
		// .getTaskWithDependents();
		// super.onActivityCreated(savedInstanceState);
		// Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		// List<File> entityList = DataProvider.getFileList(getActivity()
		// .getApplicationContext());
		// ListView lv = (ListView) mView.findViewById(R.id.fragment_list_listview);
		// ArrayAdapterFile arrayAdapter = new ArrayAdapterFile(getActivity(),
		// android.R.layout.simple_list_item_multiple_choice, entityList);
		// lv.setAdapter(arrayAdapter);
		// lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		// ArrayList<Long> idList = DataProvider.getFileIdListForTask(getActivity()
		// .getApplicationContext(), contactWithDependencies.task.getId(), false,
		// true, false);
		// for (Long long1 : idList) {
		// int position = arrayAdapter.getItemPosition(long1);
		// if (position != -1) {
		// lv.setItemChecked(position, true);
		// }
		// }
		// lv.setOnItemClickListener(new OnItemClickListener() {
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int position,
		// long id) {
		// Toast.makeText(getActivity().getApplicationContext(),
		// "position " + position + " id " + id, Toast.LENGTH_LONG).show();
		// }
		// });
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
		// outState.putSerializable("treeManager", mManager);
		// outState.putSerializable("treeViewListItemDescriptionList",
		// mTreeViewListItemDescriptionList);
		super.onSaveInstanceState(outState);
		Log.i(CommonConstants.DEBUG_TAG, "onSaveInstanceState");
	}

	@Override
	public boolean isDataCollected() {
		// collect iles
		// TaskWithDependents entityWithDependencies = ((TaskWithDependentsHolder)
		// getActivity())
		// .getTaskWithDependents();
		// ListView lv = (ListView)
		// getActivity().findViewById(R.id.fragment_list_listview);
		// // contactWithDependencies.taskIdList should never be null here
		// // entityWithDependencies.fileIdList.clear();
		// long[] idArray = lv.getCheckedItemIds();
		// for (long id : idArray) {
		// // entityWithDependencies.fileIdList.add(id);
		// }
		return true;
	}
	// private class ArrayAdapterFile extends ArrayAdapter<File> {
	// List<File> objects;
	//
	// public ArrayAdapterFile(Context context, int textViewResourceId,
	// List<File> objects) {
	// super(context, textViewResourceId, objects);
	// this.objects = objects;
	// }
	//
	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// final CheckedTextView textView;
	// if (convertView == null) {
	// textView = (CheckedTextView) getActivity().getLayoutInflater().inflate(
	// android.R.layout.simple_list_item_multiple_choice, null);
	// } else {
	// textView = (CheckedTextView) convertView;
	// }
	// textView.setText(objects.get(position).getFileName());
	// return textView;
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// File item = objects.get(position);
	// return item.getId();
	// }
	//
	// public int getItemPosition(long id) {
	// for (int i = 0; i < objects.size(); i++) {
	// File item = objects.get(i);
	// if (item.getId() == id) {
	// return i;
	// }
	// }
	// return -1;
	// }
	//
	// @Override
	// public boolean hasStableIds() {
	// return true;
	// }
	// }
}
