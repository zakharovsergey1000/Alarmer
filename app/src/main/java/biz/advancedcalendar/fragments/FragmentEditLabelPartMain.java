package biz.advancedcalendar.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivitySelectTreeItems;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.Label;

public class FragmentEditLabelPartMain extends Fragment implements
		OnCheckedChangeListener, DataSaver {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_edit_label_part_main, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (savedInstanceState == null) {
			Activity activity = getActivity();
			// setup name field
			EditText editText = (EditText) activity
					.findViewById(R.id.fragment_edit_label_part_main_edittext_label_name);
			// editText.setText(Global.getLabelToEdit().getLabelName());
			// setup CheckBox parent entity
			CheckBox checkBox = (CheckBox) activity
					.findViewById(R.id.fragment_edit_label_part_main_checkbox_label_parent);
			if (Global.getLabelToEdit().getLocalParentId() != null) {
				// checkBox.setText(DataProvider.getLabel(getActivity(),
				// Global.getLabelToEdit().getLocalParentId(), false).getLabelName());
				checkBox.setOnCheckedChangeListener(null);
				checkBox.setChecked(true);
			}
			checkBox.setOnCheckedChangeListener(this);
			// if (Global.getLabelToEdit().getServerId() != null) {
			// LinearLayout ll = (LinearLayout) activity
			// .findViewById(R.id.fragment_edit_label_part_main_linearlayout_label_parent);
			// ll.setVisibility(View.GONE);
			// } else
			// setup description field
			editText = (EditText) activity
					.findViewById(R.id.fragment_edit_label_part_main_edittext_label_description);
			editText.setText(Global.getLabelToEdit().getDescription());
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.fragment_edit_label_part_main_checkbox_label_parent:
			if (isChecked) {
				Bundle bundle = new Bundle();
				bundle.putInt(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_TYPE,
						CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE_FOR_LABEL_TREE);
				bundle.putInt(CommonConstants.TREE_VIEW_LIST_CHOICE_MODE,
						AbsListView.CHOICE_MODE_SINGLE);
				if (Global.getLabelToEdit().getId() != null) {
					bundle.putLong(CommonConstants.TREE_ELEMENTS_EXCLUDE_SUBTREE, Global
							.getLabelToEdit().getId());
				}
				bundle.putBoolean(
						CommonConstants.TREE_ELEMENTS_INCLUDE_NON_DELETED_LABEL, true);
				bundle.putBoolean(CommonConstants.TREE_ELEMENTS_INCLUDE_DELETED_LABEL,
						false);
				Intent intent = new Intent(getActivity(), ActivitySelectTreeItems.class);
				intent.putExtra(CommonConstants.SELECT_TREE_ITEM_REQUEST_BUNDLE, bundle);
				intent.putExtra(
						ActivitySelectTreeItems.IntentExtraTitle,
						getResources().getString(
								R.string.activity_select_task_title_select_parent_task));
				startActivityForResult(intent,
						CommonConstants.SELECT_TREE_ITEM_REQUEST_FOR_LABEL_TREE);
			} else {
				buttonView
						.setText(R.string.fragment_edit_label_part_main_checkbox_label_parent_not_set);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onStop() {
		if (!getActivity().isFinishing()) {
			// collect info
			isDataCollected();
		}
		super.onStop();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Check which request it is that we're responding to
		if (requestCode == CommonConstants.SELECT_TREE_ITEM_REQUEST_FOR_LABEL_TREE) {
			CheckBox checkBox = (CheckBox) getActivity().findViewById(
					R.id.fragment_edit_label_part_main_checkbox_label_parent);
			// Make sure the request was successful
			if (resultCode == android.app.Activity.RESULT_OK) {
				long[] idArray = intent.getLongArrayExtra(CommonConstants.ID_ARRAY);
				Label parent = null;
				if (idArray.length > 0) {
					parent = DataProvider.getLabel(null, getActivity(), idArray[0], false);
				}
				// setup CheckBox
				if (parent == null) {
					// throw new IllegalStateException(
					// "The activity have returned wrong id for the parent entity id: "
					// + idArray);
					checkBox.setChecked(false);
				} else {
					checkBox.setText(parent.getText());
					// store parent entity
					Global.getLabelToEdit().setLocalParentId(parent.getId());
					// Global.getLabelToEdit().setServerParentId(parent.getServerId());
				}
			} else if (resultCode == android.app.Activity.RESULT_CANCELED) {
				// setup CheckBox
				checkBox.setChecked(false);
			}
		}
	}

	@Override
	public boolean isDataCollected() {
		Activity activity = getActivity();
		// collect label name
		EditText editText = (EditText) activity
				.findViewById(R.id.fragment_edit_label_part_main_edittext_label_name);
		String text = editText.getText().toString().trim();
		Global.getLabelToEdit().setText(text);
		// collect parent entity
		// It is already collected in onActivityResult but adjust it in case
		// checkbox_label_parent is unchecked
		CheckBox checkBox = (CheckBox) getActivity().findViewById(
				R.id.fragment_edit_label_part_main_checkbox_label_parent);
		if (!checkBox.isChecked()) {
			// wipe out parent entity
			Global.getLabelToEdit().setLocalParentId(null);
			// sync with server
			// Global.getLabelToEdit().setServerParentId(0);
		}
		// collect label description
		editText = (EditText) activity
				.findViewById(R.id.fragment_edit_label_part_main_edittext_label_description);
		text = editText.getText().toString().trim();
		Global.getLabelToEdit().setDescription(text);
		return true;
	}
}
