package biz.advancedcalendar.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.ActivitySelectContactData;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.ContactWithDependents;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.ContactData;

public class FragmentEditContactPartData extends Fragment implements DataSaver {
	private View mFragmentView;
	// private Long mEntityToEditId = null;
	private Set<Short> mVisibleContactDataFieldKeySet;
	private HashMap<Short, Integer> mEditTextMap = new HashMap<Short, Integer>();
	private HashMap<Short, Integer> mTextViewMap = new HashMap<Short, Integer>();
	final int SELECT_CONTACT_DATA_FIELDS_ID = Menu.FIRST + 100 - 1;
	final int SELECT_CONTACT_DATA_FIELDS_REQUEST = 2; // The request code

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// We could lazily load the list in onResume() but lets load it
		// here to minimize latency when we will actually view this fragment
		// This is because the fragment could be attached to the activity but
		// not viewed at the time
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		if (contactWithDependencies.contactDataList == null) {
			contactWithDependencies.contactDataList = DataProvider
					.getContactDataListForContact(null,
							getActivity(), contactWithDependencies.contact.getId());
		}
		// Restore state here
		if (savedInstanceState != null) {
			short[] visibleContactDataFieldKeyArray = savedInstanceState
					.getShortArray(CommonConstants.visibleContactDataFieldKeySet);
			mVisibleContactDataFieldKeySet = new HashSet<Short>();
			for (short visibleContactDataFieldKey : visibleContactDataFieldKeyArray) {
				mVisibleContactDataFieldKeySet.add(visibleContactDataFieldKey);
			}
		} else {
			if (mVisibleContactDataFieldKeySet == null) {
				mVisibleContactDataFieldKeySet = new HashSet<Short>();
				for (ContactData visibleContactDataField : contactWithDependencies.contactDataList) {
					mVisibleContactDataFieldKeySet.add(visibleContactDataField.getType());
				}
				// always show these fields
				mVisibleContactDataFieldKeySet.add(ContactData.TYPE.PHONE_CELLULAR
						.getValue());
				mVisibleContactDataFieldKeySet.add(ContactData.TYPE.EMAIL.getValue());
			}
		}
		fillMaps();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(CommonConstants.DEBUG_TAG, "onCreateView");
		mFragmentView = inflater.inflate(R.layout.fragment_edit_contact_part_data,
				container, false);
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		for (ContactData visibleContactDataField : contactWithDependencies.contactDataList) {
			int id = mEditTextMap.get(visibleContactDataField.getType());
			EditText editText = (EditText) mFragmentView.findViewById(id);
			editText.setText(visibleContactDataField.getValue());
		}
		setSelectedFieldsVisible();
		return mFragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.i(CommonConstants.DEBUG_TAG, "onActivityCreated");
		setHasOptionsMenu(true);
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
	public void onStop() {
		if (!getActivity().isFinishing()) {
			// collect info
			isDataCollected();
		}
		super.onStop();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		short[] array = new short[mVisibleContactDataFieldKeySet.size()];
		int i = 0;
		for (Short s : mVisibleContactDataFieldKeySet) {
			array[i++] = s;
		}
		savedInstanceState.putShortArray(CommonConstants.visibleContactDataFieldKeySet,
				array);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		MenuItem addItem = menu.add(Menu.NONE, SELECT_CONTACT_DATA_FIELDS_ID, 0,
				getResources().getString(R.string.action_reminder_new));
		// addItem.setIcon(R.drawable.ic_content_new);
		MenuItemCompat.setShowAsAction(addItem, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case SELECT_CONTACT_DATA_FIELDS_ID:
			Intent intent = new Intent(getActivity(), ActivitySelectContactData.class);
			short[] visibleContactDataFieldKeyArray = new short[mVisibleContactDataFieldKeySet
					.size()];
			int i = 0;
			for (Short key : mVisibleContactDataFieldKeySet) {
				visibleContactDataFieldKeyArray[i++] = key;
			}
			intent.putExtra(CommonConstants.visibleContactDataFieldKeyArray,
					visibleContactDataFieldKeyArray);
			startActivityForResult(intent, SELECT_CONTACT_DATA_FIELDS_REQUEST);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// Check which request it is that we're responding to
		if (requestCode == SELECT_CONTACT_DATA_FIELDS_REQUEST) {
			// Make sure the request was successful
			if (resultCode == android.app.Activity.RESULT_OK) {
				mVisibleContactDataFieldKeySet.clear();
				// always show these fields
				mVisibleContactDataFieldKeySet.add(ContactData.TYPE.PHONE_CELLULAR
						.getValue());
				mVisibleContactDataFieldKeySet.add(ContactData.TYPE.EMAIL.getValue());
				short[] visibleContactDataFieldKeyArray = intent
						.getShortArrayExtra(CommonConstants.visibleContactDataFieldKeyArray);
				for (short key : visibleContactDataFieldKeyArray) {
					mVisibleContactDataFieldKeySet.add(key);
				}
				setSelectedFieldsVisible();
			}
		}
	}

	private void setSelectedFieldsVisible() {
		// first hide all fields
		LinearLayout ll = (LinearLayout) mFragmentView
				.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_phone_numbers);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) mFragmentView
				.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_email);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) mFragmentView
				.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_internet_communication);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) mFragmentView
				.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_addresses_and_requisites);
		ll.setVisibility(View.GONE);
		ll = (LinearLayout) mFragmentView
				.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_own_fields);
		ll.setVisibility(View.GONE);
		TextView textView;
		EditText editText;
		Set<Entry<Short, Integer>> EditTextSet = mEditTextMap.entrySet();
		for (Entry<Short, Integer> entry : EditTextSet) {
			editText = (EditText) mFragmentView.findViewById(entry.getValue());
			editText.setVisibility(View.GONE);
		}
		Set<Entry<Short, Integer>> TextViewSet = mTextViewMap.entrySet();
		for (Entry<Short, Integer> entry : TextViewSet) {
			textView = (TextView) mFragmentView.findViewById(entry.getValue());
			textView.setVisibility(View.GONE);
		}
		// next show selected fields
		for (short key : mVisibleContactDataFieldKeySet) {
			editText = (EditText) mFragmentView.findViewById(mEditTextMap.get(key));
			editText.setVisibility(View.VISIBLE);
			textView = (TextView) mFragmentView.findViewById(mTextViewMap.get(key));
			textView.setVisibility(View.VISIBLE);
			ContactData.TYPE cdt = ContactData.TYPE.fromInt(key);
			// show the group in which resides selected field
			switch (cdt) {
			case PHONE_CELLULAR:
			case PHONE_COMPANY:
			case PHONE_CELLULAR_2:
			case FAX:
			case FAX_COMPANY:
			case PHONE_COMPANY_2:
			case PHONE_HOME:
			case PHONE_HOME_2:
				ll = (LinearLayout) mFragmentView
						.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_phone_numbers);
				ll.setVisibility(View.VISIBLE);
				break;
			case EMAIL:
			case EMAIL_PERSONAL:
			case EMAIL_PERSONAL_2:
			case EMAIL_COMPANY:
			case EMAIL_COMPANY_2:
				ll = (LinearLayout) mFragmentView
						.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_email);
				ll.setVisibility(View.VISIBLE);
				break;
			case ICQ:
			case FACEBOOK:
			case MAIL_AGENT:
			case QIP:
			case MSN:
			case GOOGLE_TALK:
			case LIVE_JOURNAL:
			case PERSONAL_SITE:
			case PERSONAL_SITE_2:
			case SKYPE:
			case TWITTER:
			case JABBER:
				ll = (LinearLayout) mFragmentView
						.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_internet_communication);
				ll.setVisibility(View.VISIBLE);
				break;
			case ADDRESS:
			case ADDRESS_2:
			case LEGAL_ADDRESS:
			case COMPANY:
			case OCCUPATION:
				ll = (LinearLayout) mFragmentView
						.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_addresses_and_requisites);
				ll.setVisibility(View.VISIBLE);
				break;
			case FIELD_1:
			case FIELD_2:
			case FIELD_3:
			case FIELD_4:
			case FIELD_5:
			case FIELD_6:
				ll = (LinearLayout) mFragmentView
						.findViewById(R.id.fragment_edit_contact_part_data_linearlayout_own_fields);
				ll.setVisibility(View.VISIBLE);
				break;
			}
		}
	}

	@Override
	public boolean isDataCollected() {
		// Activity activity = getActivity();
		ContactWithDependents contactWithDependencies = ((ContactWithDependenciesHolder) getActivity())
				.getContactWithDependencies();
		if (contactWithDependencies.contactDataList == null) {
			contactWithDependencies.contactDataList = new ArrayList<ContactData>(
					mVisibleContactDataFieldKeySet.size());
		} else {
			contactWithDependencies.contactDataList.clear();
		}
		for (Iterator<Short> iterator = mVisibleContactDataFieldKeySet.iterator(); iterator
				.hasNext();) {
			// Short key = iterator.next();
			// EditText editText = (EditText) mFragmentView.findViewById(mEditTextMap
			// .get(key));
			// String contactDataStr = editText.getText().toString().trim();
			// UserProfile userProfile = DataProvider.getUserProfile(getActivity());
			// if (contactDataStr != null && !(contactDataStr.length() == 0)) {
			ContactData contactData = null;
			// new ContactData(null, 0,
			// mEntityToEditId, key, contactDataStr,
			// localConfirmedLogin.getUserName(), 0);
			contactWithDependencies.contactDataList.add(contactData);
			// }
		}
		return true;
	}

	/**
	 *
	 */
	private void fillMaps() {
		mEditTextMap.put(ContactData.TYPE.PHONE_CELLULAR.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_phone_cellular);
		mEditTextMap.put(ContactData.TYPE.PHONE_COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_phone_work);
		mEditTextMap.put(ContactData.TYPE.PHONE_CELLULAR_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_phone_cellular_2);
		mEditTextMap.put(ContactData.TYPE.FAX.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_fax);
		mEditTextMap.put(ContactData.TYPE.FAX_COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_fax_work);
		mEditTextMap.put(ContactData.TYPE.PHONE_COMPANY_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_phone_work_2);
		mEditTextMap.put(ContactData.TYPE.PHONE_HOME.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_phone_home);
		mEditTextMap.put(ContactData.TYPE.PHONE_HOME_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_phone_home_2);
		mEditTextMap.put(ContactData.TYPE.EMAIL.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_email);
		mEditTextMap.put(ContactData.TYPE.EMAIL_PERSONAL.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_email_personal);
		mEditTextMap.put(ContactData.TYPE.EMAIL_PERSONAL_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_email_personal_2);
		mEditTextMap.put(ContactData.TYPE.EMAIL_COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_email_work);
		mEditTextMap.put(ContactData.TYPE.EMAIL_COMPANY_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_email_work_2);
		mEditTextMap.put(ContactData.TYPE.ICQ.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_icq);
		mEditTextMap.put(ContactData.TYPE.FACEBOOK.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_facebook);
		mEditTextMap.put(ContactData.TYPE.MAIL_AGENT.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_mail_agent);
		mEditTextMap.put(ContactData.TYPE.QIP.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_qip);
		mEditTextMap.put(ContactData.TYPE.MSN.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_msn);
		mEditTextMap.put(ContactData.TYPE.GOOGLE_TALK.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_google_talk);
		mEditTextMap.put(ContactData.TYPE.LIVE_JOURNAL.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_live_journal);
		mEditTextMap.put(ContactData.TYPE.PERSONAL_SITE.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_personal_site);
		mEditTextMap.put(ContactData.TYPE.PERSONAL_SITE_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_personal_site_2);
		mEditTextMap.put(ContactData.TYPE.SKYPE.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_skype);
		mEditTextMap.put(ContactData.TYPE.TWITTER.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_twitter);
		mEditTextMap.put(ContactData.TYPE.JABBER.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_jabber);
		mEditTextMap.put(ContactData.TYPE.ADDRESS.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_address);
		mEditTextMap.put(ContactData.TYPE.ADDRESS_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_address_2);
		mEditTextMap.put(ContactData.TYPE.LEGAL_ADDRESS.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_legal_address);
		mEditTextMap.put(ContactData.TYPE.COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_company);
		mEditTextMap.put(ContactData.TYPE.OCCUPATION.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_occupation);
		mEditTextMap.put(ContactData.TYPE.FIELD_1.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_field_1);
		mEditTextMap.put(ContactData.TYPE.FIELD_2.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_field_2);
		mEditTextMap.put(ContactData.TYPE.FIELD_3.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_field_3);
		mEditTextMap.put(ContactData.TYPE.FIELD_4.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_field_4);
		mEditTextMap.put(ContactData.TYPE.FIELD_5.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_field_5);
		mEditTextMap.put(ContactData.TYPE.FIELD_6.getValue(),
				R.id.fragment_edit_contact_part_data_edittext_field_6);
		mTextViewMap.put(ContactData.TYPE.PHONE_CELLULAR.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_phone_cellular);
		mTextViewMap.put(ContactData.TYPE.PHONE_COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_phone_work);
		mTextViewMap.put(ContactData.TYPE.PHONE_CELLULAR_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_phone_cellular_2);
		mTextViewMap.put(ContactData.TYPE.FAX.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_fax);
		mTextViewMap.put(ContactData.TYPE.FAX_COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_fax_work);
		mTextViewMap.put(ContactData.TYPE.PHONE_COMPANY_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_phone_work_2);
		mTextViewMap.put(ContactData.TYPE.PHONE_HOME.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_phone_home);
		mTextViewMap.put(ContactData.TYPE.PHONE_HOME_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_phone_home_2);
		mTextViewMap.put(ContactData.TYPE.EMAIL.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_email);
		mTextViewMap.put(ContactData.TYPE.EMAIL_PERSONAL.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_email_personal);
		mTextViewMap.put(ContactData.TYPE.EMAIL_PERSONAL_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_email_personal_2);
		mTextViewMap.put(ContactData.TYPE.EMAIL_COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_email_work);
		mTextViewMap.put(ContactData.TYPE.EMAIL_COMPANY_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_email_work_2);
		mTextViewMap.put(ContactData.TYPE.ICQ.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_icq);
		mTextViewMap.put(ContactData.TYPE.FACEBOOK.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_facebook);
		mTextViewMap.put(ContactData.TYPE.MAIL_AGENT.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_mail_agent);
		mTextViewMap.put(ContactData.TYPE.QIP.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_qip);
		mTextViewMap.put(ContactData.TYPE.MSN.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_msn);
		mTextViewMap.put(ContactData.TYPE.GOOGLE_TALK.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_google_talk);
		mTextViewMap.put(ContactData.TYPE.LIVE_JOURNAL.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_live_journal);
		mTextViewMap.put(ContactData.TYPE.PERSONAL_SITE.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_personal_site);
		mTextViewMap.put(ContactData.TYPE.PERSONAL_SITE_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_personal_site_2);
		mTextViewMap.put(ContactData.TYPE.SKYPE.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_skype);
		mTextViewMap.put(ContactData.TYPE.TWITTER.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_twitter);
		mTextViewMap.put(ContactData.TYPE.JABBER.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_jabber);
		mTextViewMap.put(ContactData.TYPE.ADDRESS.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_address);
		mTextViewMap.put(ContactData.TYPE.ADDRESS_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_address_2);
		mTextViewMap.put(ContactData.TYPE.LEGAL_ADDRESS.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_legal_address);
		mTextViewMap.put(ContactData.TYPE.COMPANY.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_company);
		mTextViewMap.put(ContactData.TYPE.OCCUPATION.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_occupation);
		mTextViewMap.put(ContactData.TYPE.FIELD_1.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_field_1);
		mTextViewMap.put(ContactData.TYPE.FIELD_2.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_field_2);
		mTextViewMap.put(ContactData.TYPE.FIELD_3.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_field_3);
		mTextViewMap.put(ContactData.TYPE.FIELD_4.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_field_4);
		mTextViewMap.put(ContactData.TYPE.FIELD_5.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_field_5);
		mTextViewMap.put(ContactData.TYPE.FIELD_6.getValue(),
				R.id.fragment_edit_contact_part_data_header_textview_field_6);
	}
}
