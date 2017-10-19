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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Message;

public class FragmentEditTaskPartMessages extends Fragment implements DataSaver {
	private BroadcastReceiver receiver;
	private View mFragmentView;
	private Long mEntityToEditId = null;

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
		// Restore state here
		if (getActivity().getIntent().hasExtra(CommonConstants.INTENT_EXTRA_ID)) {
			mEntityToEditId = getActivity().getIntent().getLongExtra(
					CommonConstants.INTENT_EXTRA_ID, -1);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mFragmentView = inflater.inflate(R.layout.fragment_edit_task_part_messages,
				container, false);
		return mFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LocalBroadcastManager
				.getInstance(getActivity().getApplicationContext())
				.registerReceiver(
						receiver,
						new IntentFilter(CommonConstants.ACTION_ENTITIES_CHANGED_MESSAGES));
		fillListView();
		Button b = (Button) getActivity().findViewById(
				R.id.fragment_edit_task_part_messages_button_send);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mEntityToEditId != null) {
					// EditText editText = (EditText) getActivity().findViewById(
					// R.id.fragment_edit_task_part_messages_edittext);
					// DataProvider.insertMessage(getActivity(), editText.getText()
					// .toString(), mEntityToEditId);
				}
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mEntityToEditId != null) {
			// Alarm.setAlarmForSyncServiceSyncMessagesForTask(getActivity(), 1000 * 60 *
			// 5,
			// mEntityToEditId);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mEntityToEditId != null) {
			// Alarm.cancelAlarmForSyncServiceSyncMessagesForTask(getActivity(),
			// mEntityToEditId);
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
	public void onDetach() {
		super.onDetach();
	}

	private void fillListView() {
		List<Message> entityList = DataProvider.getMessageListForTask(null, getActivity()
						.getApplicationContext(), mEntityToEditId);
		ListView lv = (ListView) getActivity().findViewById(
				R.id.fragment_edit_task_part_messages_listview);
		ArrayAdapterMessage arrayAdapter = new ArrayAdapterMessage(getActivity(),
				android.R.layout.simple_list_item_1, entityList);
		lv.setAdapter(arrayAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				Toast.makeText(getActivity().getApplicationContext(),
						"position " + position + " id " + id, Toast.LENGTH_LONG).show();
			}
		});
	}

	@Override
	public boolean isDataCollected() {
		return true;
	}

	private class ArrayAdapterMessage extends ArrayAdapter<Message> {
		List<Message> objects;

		public ArrayAdapterMessage(Context context, int textViewResourceId,
				List<Message> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CheckedTextView textView;
			if (convertView == null) {
				textView = (CheckedTextView) getActivity().getLayoutInflater().inflate(
						android.R.layout.simple_list_item_multiple_choice, parent, false);
			} else {
				textView = (CheckedTextView) convertView;
			}
			textView.setText(objects.get(position).getText());
			return textView;
		}

		@Override
		public long getItemId(int position) {
			Message item = objects.get(position);
			return item.getId();
		}

		// public int getItemPosition(long id) {
		// for (int i = 0; i < objects.size(); i++) {
		// Message item = objects.get(i);
		// if (item.getId() == id) {
		// return i;
		// }
		// }
		// return -1;
		// }
		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}
