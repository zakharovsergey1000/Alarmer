package biz.advancedcalendar.fragments;

import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.Update;

public class FragmentViewUpdates extends Fragment {
	private BroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(
						CommonConstants.ACTION_ENTITIES_CHANGED_MESSAGES)) {
					// if (!isDetached()) {
					if (isAdded()) {
						if (getActivity() == null) {
							Log.d(CommonConstants.DEBUG_TAG, "Yes, it is null.");
						}
						fillListView();
					}
				}
			}
		};
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_updates, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// DataProvider.addDataChangedListener(this);
		LocalBroadcastManager
				.getInstance(getActivity().getApplicationContext())
				.registerReceiver(
						receiver,
						new IntentFilter(CommonConstants.ACTION_ENTITIES_CHANGED_MESSAGES));
		fillListView();
		ListView lv = (ListView) getActivity().findViewById(R.id.updates_list_view);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Toast.makeText(getActivity().getApplicationContext(),
						"position " + position + " id " + id, Toast.LENGTH_LONG).show();
				// Intent intent = new Intent(getActivity(),
				// ActivityViewUpdate.class);
				// startActivity(intent);
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
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

	private class StableArrayAdapter extends ArrayAdapter<Update> {
		List<Update> objects;

		public StableArrayAdapter(Context context, int textViewResourceId,
				List<Update> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TextView tv;
			if (convertView == null) {
				tv = (TextView) getActivity().getLayoutInflater().inflate(
						R.layout.fragment_view_list_list_item, null);
			} else {
				tv = (TextView) convertView;
			}
			final Update m = getItem(position);
			tv.setText(m.toString());
			return tv;
		}

		@Override
		public long getItemId(int position) {
			Update item = objects.get(position);
			item.getUpdateText();
			return 0L;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	// @Override
	// public void onDataChanged(DataTypeChanged dataType) {
	// if (dataType == DataTypeChanged.MESSAGES) {
	// // if (!isDetached()) {
	// if (isAdded()) {
	// fillListView();
	// }
	// }
	// }
	private void fillListView() {
		ListView lv = (ListView) getActivity().findViewById(R.id.updates_list_view);
		List<Update> updateList = null; // DataProvider.getUpdates();
		StableArrayAdapter arrayAdapter = new StableArrayAdapter(getActivity(),
				R.layout.fragment_view_list_list_item, updateList);
		lv.setAdapter(arrayAdapter);
	}
}
