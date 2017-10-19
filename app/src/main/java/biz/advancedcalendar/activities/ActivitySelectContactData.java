package biz.advancedcalendar.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.greendao.ContactData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/** Activity which displays a login screen to the user, offering registration as well. */
public class ActivitySelectContactData extends Activity {
	private boolean mActionDonePerformed = false;
	private HashMap<Short, Integer> mCheckBoxMap = new HashMap<Short, Integer>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCheckBoxMap.put(ContactData.TYPE.PHONE_CELLULAR.getValue(),
				R.id.activity_select_contact_data_checkbox_phone_cellular);
		mCheckBoxMap.put(ContactData.TYPE.PHONE_COMPANY.getValue(),
				R.id.activity_select_contact_data_checkbox_phone_work);
		mCheckBoxMap.put(ContactData.TYPE.PHONE_CELLULAR_2.getValue(),
				R.id.activity_select_contact_data_checkbox_phone_cellular_2);
		mCheckBoxMap.put(ContactData.TYPE.FAX.getValue(),
				R.id.activity_select_contact_data_checkbox_fax);
		mCheckBoxMap.put(ContactData.TYPE.FAX_COMPANY.getValue(),
				R.id.activity_select_contact_data_checkbox_fax_work);
		mCheckBoxMap.put(ContactData.TYPE.PHONE_COMPANY_2.getValue(),
				R.id.activity_select_contact_data_checkbox_phone_work_2);
		mCheckBoxMap.put(ContactData.TYPE.PHONE_HOME.getValue(),
				R.id.activity_select_contact_data_checkbox_phone_home);
		mCheckBoxMap.put(ContactData.TYPE.PHONE_HOME_2.getValue(),
				R.id.activity_select_contact_data_checkbox_phone_home_2);
		mCheckBoxMap.put(ContactData.TYPE.EMAIL.getValue(),
				R.id.activity_select_contact_data_checkbox_email);
		mCheckBoxMap.put(ContactData.TYPE.EMAIL_PERSONAL.getValue(),
				R.id.activity_select_contact_data_checkbox_email_personal);
		mCheckBoxMap.put(ContactData.TYPE.EMAIL_PERSONAL_2.getValue(),
				R.id.activity_select_contact_data_checkbox_email_personal_2);
		mCheckBoxMap.put(ContactData.TYPE.EMAIL_COMPANY.getValue(),
				R.id.activity_select_contact_data_checkbox_email_work);
		mCheckBoxMap.put(ContactData.TYPE.EMAIL_COMPANY_2.getValue(),
				R.id.activity_select_contact_data_checkbox_email_work_2);
		mCheckBoxMap.put(ContactData.TYPE.ICQ.getValue(),
				R.id.activity_select_contact_data_checkbox_icq);
		mCheckBoxMap.put(ContactData.TYPE.FACEBOOK.getValue(),
				R.id.activity_select_contact_data_checkbox_facebook);
		mCheckBoxMap.put(ContactData.TYPE.MAIL_AGENT.getValue(),
				R.id.activity_select_contact_data_checkbox_mail_agent);
		mCheckBoxMap.put(ContactData.TYPE.QIP.getValue(),
				R.id.activity_select_contact_data_checkbox_qip);
		mCheckBoxMap.put(ContactData.TYPE.MSN.getValue(),
				R.id.activity_select_contact_data_checkbox_msn);
		mCheckBoxMap.put(ContactData.TYPE.GOOGLE_TALK.getValue(),
				R.id.activity_select_contact_data_checkbox_google_talk);
		mCheckBoxMap.put(ContactData.TYPE.LIVE_JOURNAL.getValue(),
				R.id.activity_select_contact_data_checkbox_live_journal);
		mCheckBoxMap.put(ContactData.TYPE.PERSONAL_SITE.getValue(),
				R.id.activity_select_contact_data_checkbox_personal_site);
		mCheckBoxMap.put(ContactData.TYPE.PERSONAL_SITE_2.getValue(),
				R.id.activity_select_contact_data_checkbox_personal_site_2);
		mCheckBoxMap.put(ContactData.TYPE.SKYPE.getValue(),
				R.id.activity_select_contact_data_checkbox_skype);
		mCheckBoxMap.put(ContactData.TYPE.TWITTER.getValue(),
				R.id.activity_select_contact_data_checkbox_twitter);
		mCheckBoxMap.put(ContactData.TYPE.JABBER.getValue(),
				R.id.activity_select_contact_data_checkbox_jabber);
		mCheckBoxMap.put(ContactData.TYPE.ADDRESS.getValue(),
				R.id.activity_select_contact_data_checkbox_address);
		mCheckBoxMap.put(ContactData.TYPE.ADDRESS_2.getValue(),
				R.id.activity_select_contact_data_checkbox_address_2);
		mCheckBoxMap.put(ContactData.TYPE.LEGAL_ADDRESS.getValue(),
				R.id.activity_select_contact_data_checkbox_legal_address);
		mCheckBoxMap.put(ContactData.TYPE.COMPANY.getValue(),
				R.id.activity_select_contact_data_checkbox_company);
		mCheckBoxMap.put(ContactData.TYPE.OCCUPATION.getValue(),
				R.id.activity_select_contact_data_checkbox_occupation);
		mCheckBoxMap.put(ContactData.TYPE.FIELD_1.getValue(),
				R.id.activity_select_contact_data_checkbox_field_1);
		mCheckBoxMap.put(ContactData.TYPE.FIELD_2.getValue(),
				R.id.activity_select_contact_data_checkbox_field_2);
		mCheckBoxMap.put(ContactData.TYPE.FIELD_3.getValue(),
				R.id.activity_select_contact_data_checkbox_field_3);
		mCheckBoxMap.put(ContactData.TYPE.FIELD_4.getValue(),
				R.id.activity_select_contact_data_checkbox_field_4);
		mCheckBoxMap.put(ContactData.TYPE.FIELD_5.getValue(),
				R.id.activity_select_contact_data_checkbox_field_5);
		mCheckBoxMap.put(ContactData.TYPE.FIELD_6.getValue(),
				R.id.activity_select_contact_data_checkbox_field_6);
		setContentView(R.layout.activity_select_contact_data);
		Intent intent = getIntent();
		short[] visibleContactDataFieldKeyArray = intent
				.getShortArrayExtra(CommonConstants.visibleContactDataFieldKeyArray);
		for (int i = 0; i < visibleContactDataFieldKeyArray.length; i++) {
			// short key = visibleContactDataFieldKeyArray[i];
			CheckBox cb = (CheckBox) findViewById(mCheckBoxMap
					.get(visibleContactDataFieldKeyArray[i]));
			cb.setChecked(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu_cancel_done, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_cancel:
			finish();
			return true;
		case R.id.action_done:
			mActionDonePerformed = true;
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void finish() {
		Intent intent = new Intent();
		if (!mActionDonePerformed) {
			// Activity finished by cancel, return no data
			setResult(Activity.RESULT_CANCELED);
			super.finish();
			return;
		}
		List<Short> visibleContactDataFieldKeyList = new ArrayList<Short>();
		Set<Entry<Short, Integer>> d = mCheckBoxMap.entrySet();
		for (Entry<Short, Integer> contactData : d) {
			CheckBox cb = (CheckBox) findViewById(contactData.getValue());
			if (cb.isChecked()) {
				visibleContactDataFieldKeyList.add(contactData.getKey());
			}
		}
		short[] visibleContactDataFieldKeyArray = new short[visibleContactDataFieldKeyList
				.size()];
		for (int i = 0; i < visibleContactDataFieldKeyList.size(); i++) {
			visibleContactDataFieldKeyArray[i] = visibleContactDataFieldKeyList.get(i);
		}
		intent.putExtra(CommonConstants.visibleContactDataFieldKeyArray,
				visibleContactDataFieldKeyArray);
		// Activity finished ok, return the data
		setResult(Activity.RESULT_OK, intent);
		super.finish();
	}
}
