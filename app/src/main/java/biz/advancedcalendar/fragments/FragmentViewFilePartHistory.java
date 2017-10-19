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

import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.File;

public class FragmentViewFilePartHistory extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_view_file_part_history,
				container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Long id = Global.getFileToEdit().getLocalId();

		if (id != null) {
			List<File> fileHistoryList = DataProvider.getFileHistoryList(null, getActivity()
							.getApplicationContext(), id);
			ListView lv = (ListView) getActivity().findViewById(
					R.id.fragment_view_file_part_history_listview);

			FileHistoryListArrayAdapter arrayAdapter = new FileHistoryListArrayAdapter(
					getActivity(), android.R.layout.simple_list_item_1,
					fileHistoryList);

			lv.setAdapter(arrayAdapter);

			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Toast.makeText(getActivity().getApplicationContext(),
							"position " + position + " id " + id,
							Toast.LENGTH_LONG).show();
					// Intent intent = new Intent(getActivity(),
					// ActivityViewContact.class);
					// intent.putExtra("biz.advancedcalendar.id", id);
					// startActivity(intent);
				}
			});

		}
	}

	private class FileHistoryListArrayAdapter extends ArrayAdapter<File> {

		List<File> objects;

		public FileHistoryListArrayAdapter(Context context,
				int textViewResourceId, List<File> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public long getItemId(int position) {
			File item = objects.get(position);
			return item.getId();
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}
}
