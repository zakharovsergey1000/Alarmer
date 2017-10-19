package biz.advancedcalendar.fragments;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;

public class FragmentViewTaskPartContacts extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_view_task_part_contacts,
				container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		ListView lv = (ListView) getActivity().findViewById(
				R.id.fragment_task_viewing_contacts_listview);

		List<biz.advancedcalendar.greendao.Contact> todoList = DataProvider
				.getContactList(null, getActivity().getApplicationContext());
		ArrayAdapter<biz.advancedcalendar.greendao.Contact> arrayAdapter = new ArrayAdapter<biz.advancedcalendar.greendao.Contact>(
				getActivity(), android.R.layout.simple_list_item_1, todoList);

		lv.setAdapter(arrayAdapter);
	}
}
