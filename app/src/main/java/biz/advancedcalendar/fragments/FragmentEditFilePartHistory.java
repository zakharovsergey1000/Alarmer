package biz.advancedcalendar.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.Global;
import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.activities.accessories.DataSaver;
import biz.advancedcalendar.db.DataProvider;
import biz.advancedcalendar.greendao.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class FragmentEditFilePartHistory extends Fragment implements DataSaver {
	private AsyncTaskFileDownload mAsyncTaskFileDownload;
	// private ProgressBar pb;
	private ProgressDialog Dialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_edit_file_part_history, container,
				false);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		CheckBox checkBox = (CheckBox) getActivity().findViewById(
				R.id.fragment_edit_file_part_history_checkbox);
		checkBox.setChecked(Global.getFileToEdit().getEnableVersions());
		Long id = Global.getFileToEdit().getLocalId();
		if (id != null) {
			List<File> entityList = DataProvider.getFileHistoryList(null, getActivity()
							.getApplicationContext(), id);
			ListView lv = (ListView) getActivity().findViewById(
					R.id.fragment_edit_file_part_history_listview);
			ArrayAdapterFileHistory arrayAdapter = new ArrayAdapterFileHistory(
					getActivity(), android.R.layout.simple_list_item_1, entityList);
			lv.setAdapter(arrayAdapter);
			// lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			// ArrayList<Long> contactIdList =
			// DataProvider.getContactIdListForLabel(
			// getActivity().getApplicationContext(), Global.getFileToEdit()
			// .getId());
			//
			// for (Long long1 : contactIdList) {
			// lv.setItemChecked(arrayAdapter.getItemPosition(long1), true);
			// }
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position,
						final long fileId) {
					Toast.makeText(getActivity().getApplicationContext(),
							"position " + position + " id " + fileId, Toast.LENGTH_LONG)
							.show();
					// launchMetadataEditor(position, id);
					// lv.setSelection( position);
					Intent intent = new Intent();
					intent.setAction(android.content.Intent.ACTION_VIEW);
					final File file = DataProvider.getFile(null, getActivity(), fileId);
					if (file == null) {
						LocalBroadcastManager
								.getInstance(getActivity())
								.sendBroadcast(
										new Intent(
												CommonConstants.ACTION_ENTITIES_CHANGED_FILES));
						return;
					}
					if (file.getLocalPath() != null) {
						java.io.File file2 = new java.io.File(file.getLocalPath());
						intent.setDataAndType(Uri.fromFile(file2), file.getContentType());
						try {
							startActivity(intent);
						} catch (android.content.ActivityNotFoundException ex) {
							Toast.makeText(
									getActivity(),
									getResources()
											.getString(
													R.string.there_are_no_applications_installed_can_handle_this_file),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								getActivity());
						builder.setMessage(R.string.file_out_of_sync_warning);
						builder.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										// User clicked OK button
										// Server Request URL
										String serverURL = file.getHref();
										String fileName = file.getUID();
										// Create Object and call AsyncTask
										// execute
										// Method
										mAsyncTaskFileDownload = new AsyncTaskFileDownload();
										mAsyncTaskFileDownload.execute(serverURL,
												fileName, "" + fileId);
									}
								});
						builder.setNegativeButton(R.string.alert_dialog_cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int id) {
										// User clicked OK button
									}
								});
						AlertDialog dialog = builder.create();
						dialog.show();
					}
				}
			});
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
	public boolean isDataCollected() {
		// collect contacts
		// ListView lv = (ListView) getActivity().findViewById(
		// R.id.fragment_edit_file_part_history_listview);
		// final long[] contactIdArray = lv.getCheckedItemIds();
		// Global.mFileToEditContactIdArray = contactIdArray;
		CheckBox checkBox = (CheckBox) getActivity().findViewById(
				R.id.fragment_edit_file_part_history_checkbox);
		Global.getFileToEdit().setEnableVersions(checkBox.isChecked());
		return true;
	}

	private class ArrayAdapterFileHistory extends ArrayAdapter<File> {
		List<File> objects;

		public ArrayAdapterFileHistory(Context context, int textViewResourceId,
				List<File> objects) {
			super(context, textViewResourceId, objects);
			this.objects = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final TextView textView;
			if (convertView == null) {
				textView = (TextView) getActivity().getLayoutInflater().inflate(
						android.R.layout.simple_list_item_1, parent, false);
			} else {
				textView = (TextView) convertView;
			}
			textView.setText(objects.get(position).getFileName());
			return textView;
		}

		@Override
		public long getItemId(int position) {
			File item = objects.get(position);
			return item.getId();
		}

		@SuppressWarnings("unused")
		public int getItemPosition(long id) {
			for (int i = 0; i < objects.size(); i++) {
				File contact = objects.get(i);
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

	// Class with extends AsyncTask class
	private class AsyncTaskFileDownload extends AsyncTask<String, Integer, Void> {
		// private String Content;
		private String Error = null;

		// TextView uiUpdate = (TextView) getActivity().findViewById(
		// R.id.action_bar_title);
		@Override
		protected void onPreExecute() {
			// NOTE: You can call UI Element here.
			Dialog = new ProgressDialog(getActivity());
			// pb = (ProgressBar)Dialog.findViewById(R.id.progress_bar);
			// pb.setProgress(0);
			// pb.setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
			// pb = Dialog.findViewById(id)
			// UI Element
			// uiUpdate.setText("Output : ");
			Dialog.setMessage(getResources()
					.getString(
							R.string.advanced_calendar_file_download_dialog_message_downloading_source));
			Dialog.setButton(DialogInterface.BUTTON_NEGATIVE,
					getResources().getString(R.string.action_cancel),
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							mAsyncTaskFileDownload.cancel(false);
						}
					});
			Dialog.show();
		}

		// Call after onPreExecute method
		@Override
		protected Void doInBackground(String... urls) {
			try {
				URL url = new URL(urls[0]);
				HttpURLConnection urlConnection = (HttpURLConnection) url
						.openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				urlConnection.setRequestProperty("Cookie", "advanced_calendar_user="
						+ DataProvider.getUserProfile(null, getActivity()).getAuthToken());
				// connect
				urlConnection.connect();
				// set the path where we want to save the file
				java.io.File SDCardRoot = Environment.getExternalStorageDirectory();
				// create a new file, to save the downloaded file
				java.io.File file = new java.io.File(SDCardRoot, urls[1]);
				FileOutputStream fileOutput = new FileOutputStream(file);
				// Stream used for reading the data from the internet
				InputStream inputStream = urlConnection.getInputStream();
				// this is the total size of the file which we are downloading
				final int totalSize = urlConnection.getContentLength();
				// create a buffer...
				byte[] buffer = new byte[1024];
				int bufferLength = 0;
				int downloadedSize = 0;
				boolean cancelled = false;
				while ((bufferLength = inputStream.read(buffer)) > 0) {
					// check for cancel
					if (isCancelled()) {
						cancelled = true;
						break;
					}
					fileOutput.write(buffer, 0, bufferLength);
					downloadedSize += bufferLength;
					// update the progressbar //
					publishProgress(totalSize, downloadedSize, totalSize * 100
							/ downloadedSize);
				}
				// close the output stream when complete //
				fileOutput.close();
				if (cancelled) {
					file.delete();
				} else {
					long id = Long.valueOf(urls[2]);
					File f = DataProvider.getFile(null, getActivity(), id);
					if (f != null) {
						f.setLocalPath(file.getPath());
						Long currentTime = System.currentTimeMillis();
						f.setLocalCreateDateTime(currentTime);
						f.setLocalChangeDateTime(currentTime);
						f.update();
					}
				}
			} catch (final MalformedURLException e) {
				// showError("Error : MalformedURLException " + e);
				e.printStackTrace();
			} catch (final IOException e) {
				// showError("Error : IOException " + e);
				e.printStackTrace();
			} catch (final Exception e) {
				// showError("Error : Please <span id="IL_AD7" class="IL_AD">check your internet connection</span> "
				// + e);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			Dialog.setProgress(progress[0]);
			Dialog.setMessage(getResources().getString(
					R.string.advanced_calendar_file_download_dialog_message_downloaded)
					+ " "
					+ progress[0]
					+ getResources().getString(
							R.string.advanced_calendar_file_download_dialog_message_kb)
					+ " / "
					+ progress[1]
					+ getResources().getString(
							R.string.advanced_calendar_file_download_dialog_message_kb)
					+ " (" + progress[2] + "%)");
		}

		@Override
		protected void onPostExecute(Void unused) {
			// NOTE: You can call UI Element here.
			// Close progress dialog
			Dialog.dismiss();
			if (Error != null) {
				// uiUpdate.setText("Output : " + Error);
			} else {
				// uiUpdate.setText("Output : " + Content);
			}
		}
	}
}
