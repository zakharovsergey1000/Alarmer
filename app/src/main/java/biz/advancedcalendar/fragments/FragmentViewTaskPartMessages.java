package biz.advancedcalendar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import biz.advancedcalendar.alarmer.R;

public class FragmentViewTaskPartMessages extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_view_task_part_messages,
				container, false);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		ListView lv = (ListView) getActivity().findViewById(
//				R.id.fragment_task_viewing_messages_listview);
//
//		Bundle b = getArguments();
//		int taskId = b.getInt("taskId");

//		List<MessageForViewing> messages = DataProvider.getMessageListOld(taskId);
//		ArrayAdapter<MessageForViewing> arrayAdapter = new ArrayAdapter<MessageForViewing>(
//				getActivity(), android.R.layout.simple_list_item_1, messages) {
//			@Override
//			public View getView(int position, View convertView, ViewGroup parent) {
//
//				final LinearLayout layout;
//				if (convertView == null) {
//					layout = (LinearLayout) getActivity().getLayoutInflater()
//							.inflate(R.layout.fragment_view_task_part_messages_list_item,
//									null);
//				} else {
//					layout = (LinearLayout) convertView;
//				}
//				final MessageForViewing m = getItem(position);
//				TextView tv;
//
//				tv = (TextView) layout
//						.findViewById(R.id.fragment_task_viewing_messages_list_item_textvew_name);
//				tv.setText(m.getFromUser());
//
//				tv = (TextView) layout
//						.findViewById(R.id.fragment_task_viewing_messages_list_item_textvew_date);
//				tv.setText(m.getDate().toString());
//
//				tv = (TextView) layout
//						.findViewById(R.id.fragment_task_viewing_messages_list_item_textvew_message);
//				tv.setText(m.getText());
//
//				return layout;
//			}
//		};
//
//		lv.setAdapter(arrayAdapter);
	}
}
