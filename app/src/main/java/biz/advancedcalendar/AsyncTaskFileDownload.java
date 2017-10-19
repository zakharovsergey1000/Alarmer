package biz.advancedcalendar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.os.Environment;

import biz.advancedcalendar.alarmer.R;
import biz.advancedcalendar.db.DataProvider;

// Class with extends AsyncTask class
public class AsyncTaskFileDownload extends AsyncTask<String, Integer, Void> {
	// private String Content;
	private String Error = null;
	private Context mContext;
	private ProgressDialog Dialog;
	private AsyncTask<String, Integer, Void> mAsyncTaskFileDownload;

	// TextView uiUpdate = (TextView) getActivity().findViewById(
	// R.id.action_bar_title);
	@Override
	protected void onPreExecute() {
		// NOTE: You can call UI Element here.
		Dialog = new ProgressDialog(mContext);
		// pb = (ProgressBar)Dialog.findViewById(R.id.progress_bar);
		// pb.setProgress(0);
		// pb.setProgressDrawable(getResources().getDrawable(R.drawable.green_progress));
		// pb = Dialog.findViewById(id)
		// UI Element
		// uiUpdate.setText("Output : ");
		Dialog.setMessage(mContext
				.getResources()
				.getString(
						R.string.advanced_calendar_file_download_dialog_message_downloading_source));
		Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, mContext.getResources()
				.getString(R.string.action_cancel), new OnClickListener() {
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
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Cookie", "advanced_calendar_user="
					+ DataProvider.getUserProfile(null, mContext).getAuthToken());
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
				biz.advancedcalendar.greendao.File f = DataProvider.getFile(null, mContext, id);
				if (f != null) {
					f.setLocalPath(file.getPath());
					Long currentTime = Calendar.getInstance().getTimeInMillis();
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
		Dialog.setMessage(mContext.getResources().getString(
				R.string.advanced_calendar_file_download_dialog_message_downloaded)
				+ " "
				+ progress[0]
				+ mContext.getResources().getString(
						R.string.advanced_calendar_file_download_dialog_message_kb)
				+ " / "
				+ progress[1]
				+ mContext.getResources().getString(
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
