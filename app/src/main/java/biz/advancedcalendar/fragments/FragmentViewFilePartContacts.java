package biz.advancedcalendar.fragments;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;

public class FragmentViewFilePartContacts extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_view_file_part_contacts,
				container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Bundle b = getArguments();
		long id = b.getLong(CommonConstants.INTENT_EXTRA_ID);

		List<Long> entityList = DataProvider.getContactIdListForFile(
				null, getActivity().getApplicationContext(), id, 0, false, true, false);
		ListView lv = (ListView) getActivity().findViewById(
				R.id.fragment_view_file_part_contacts_listview);

		MyArrayAdapter arrayAdapter = new MyArrayAdapter(getActivity(),
				android.R.layout.simple_list_item_1, entityList);

		lv.setAdapter(arrayAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Toast.makeText(getActivity().getApplicationContext(),
						"position " + position + " id " + id, Toast.LENGTH_LONG)
						.show();
				// Intent intent = new Intent(getActivity(),
				// ActivityViewContact.class);
				// intent.putExtra("biz.advancedcalendar.id", id);
				// startActivity(intent);
			}
		});

	}

	private class MyArrayAdapter extends ArrayAdapter<Long> {

		List<Long> objects;

		public MyArrayAdapter(Context context, int textViewResourceId,
				List<Long> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public long getItemId(int position) {
			Long item = objects.get(position);
			return item;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}
}
