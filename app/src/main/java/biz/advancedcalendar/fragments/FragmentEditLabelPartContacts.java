package biz.advancedcalendar.fragments;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Contact;

public class FragmentEditLabelPartContacts extends Fragment implements DataSaver {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list, container, false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		List<Contact> entityList = DataProvider.getContactList(null, getActivity()
				.getApplicationContext());
		ListView lv = (ListView) getActivity().findViewById(R.id.fragment_list_listview);
		ArrayAdapterContact arrayAdapter = new ArrayAdapterContact(getActivity(),
				android.R.layout.simple_list_item_multiple_choice, entityList);
		lv.setAdapter(arrayAdapter);
		lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		List<Long> contactIdList = DataProvider.getContactIdListForLabel(null, getActivity()
						.getApplicationContext(), Global.getLabelToEdit().getId(), false,
				true, false);
		for (Long long1 : contactIdList) {
			lv.setItemChecked(arrayAdapter.getItemPosition(long1), true);
		}
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
	public void onStop() {
		// collect info
		if (!getActivity().isFinishing()) {
			// collect info
			isDataCollected();
		}
		super.onStop();
	}

	@Override
	public boolean isDataCollected() {
		// collect contacts
		ListView lv = (ListView) getActivity().findViewById(R.id.fragment_list_listview);
		final long[] idArray = lv.getCheckedItemIds();
		ArrayList<Long> idList = new ArrayList<Long>();
		for (long id : idArray) {
			idList.add(id);
		}
		Global.setLabelToEditContactIdList(idList);
		return true;
	}

	private class ArrayAdapterContact extends ArrayAdapter<Contact> {
		List<Contact> objects;

		public ArrayAdapterContact(Context context, int textViewResourceId,
				List<Contact> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final CheckedTextView textView;
			if (convertView == null) {
				textView = (CheckedTextView) getActivity().getLayoutInflater().inflate(
						android.R.layout.simple_list_item_multiple_choice, null);
			} else {
				textView = (CheckedTextView) convertView;
			}
			textView.setText(objects.get(position).getContactName());
			return textView;
		}

		@Override
		public long getItemId(int position) {
			Contact item = objects.get(position);
			return item.getId();
		}

		public int getItemPosition(long id) {
			for (int i = 0; i < objects.size(); i++) {
				Contact contact = objects.get(i);
				if (contact.getId() == id) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}
}
