package biz.advancedcalendar.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import org.json.simple.parser.ParseException;
import android.content.Context;
import android.util.Log;
import biz.advancedcalendar.CommonConstants;
import biz.advancedcalendar.wsdl.sync.ArrayOfint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** Created with IntelliJ IDEA. User: ServusKevin Date: 5/6/13 Time: 8:23 PM To change this
 * template use File | Settings | File Templates. */
public class JSONHttpClient {
	public HttpURLConnection getHttpURLConnectionForPostMethod(final String urlString,
			final Map<String, String> headers, final Object object) throws Exception {
		URL url = new URL(urlString);
		HttpsURLConnection urlConnection;
		urlConnection = (HttpsURLConnection) url.openConnection();
		urlConnection.setRequestProperty("Accept", "application/json");
		urlConnection.setRequestProperty("Content-type", "application/json");
		if (headers != null) {
			for (String key : headers.keySet()) {
				urlConnection.setRequestProperty(key, headers.get(key));
			}
		}
		urlConnection.setReadTimeout(CommonConstants.HTTP_URL_CONNECTION_READ_TIMEOUT);
		if (object != null) {
			urlConnection.setDoOutput(true);
			OutputStream os = urlConnection.getOutputStream();
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(Date.class, new DateSerializer());
			Gson dateGson = gsonBuilder.create();
			os.write(dateGson.toJson(object).getBytes());
			os.flush();
			os.close();
		}
		return urlConnection;
	}

	public static String convertStreamToString(InputStream inputStream) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
				inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings
									// | File Templates.
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();  // To change body of catch statement use File |
										// Settings | File Templates.
			}
		}
		return stringBuilder.toString();
	}

	public <T> HttpURLConnection GetWithHeader(String urlString,
			final Map<String, String> headers, final Map<String, String> params) {
		URL url = null;
		HttpsURLConnection urlConnection;
		try {
			String queryString = JSONHttpClient.createQueryStringForParameters(params);
			if (!queryString.equals("")) {
				urlString += "?" + queryString;
			}
			url = new URL(urlString);
			urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setRequestProperty("Accept", "application/json");
			urlConnection.setRequestProperty("Content-type", "application/json");
			if (headers != null) {
				for (String key : headers.keySet()) {
					urlConnection.setRequestProperty(key, headers.get(key));
				}
			}
			urlConnection
					.setReadTimeout(CommonConstants.HTTP_URL_CONNECTION_READ_TIMEOUT);
			return urlConnection;
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings
									// | File Templates.
		}
		return null;
	}

	public HttpURLConnection Delete(String urlString, final Map<String, String> headers,
			final Map<String, String> params) throws UnsupportedEncodingException {
		String queryString = JSONHttpClient.createQueryStringForParameters(params);
		if (!queryString.equals("")) {
			urlString += "?" + queryString;
		}
		try {
			URL url = new URL(urlString);
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
			urlConnection.setRequestMethod("DELETE");
			urlConnection.setRequestProperty("Accept", "application/json");
			for (String key : headers.keySet()) {
				urlConnection.setRequestProperty(key, headers.get(key));
			}
			urlConnection
					.setReadTimeout(CommonConstants.HTTP_URL_CONNECTION_READ_TIMEOUT);
			return urlConnection;
		} catch (IOException e) {
			e.printStackTrace();  // To change body of catch statement use File | Settings
									// | File Templates.
		}
		return null;
	}

	public HttpURLConnection getHttpURLConnectionForPostStringAsFormUrlEncodedContent(
			final String urlString, final String object) throws ParseException,
			IOException {
		URL url = new URL(urlString);
		HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
		// urlConnection.setRequestMethod("POST");
		urlConnection.setRequestProperty("Accept", "application/json");
		urlConnection.setRequestProperty("Content-type",
				"application/x-www-form-urlencoded");
		urlConnection.setReadTimeout(CommonConstants.HTTP_URL_CONNECTION_READ_TIMEOUT);
		urlConnection.setDoOutput(true);
		OutputStream os = urlConnection.getOutputStream();
		os.write(object.getBytes());
		os.flush();
		os.close();
		return urlConnection;
	}

	public static HttpURLConnection getEntityListResponseForGetEntityList(
			final Context context, ArrayOfint entityIdArray,
			boolean includeDependentEntities, String authToken) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", String.format("Bearer %s", authToken));
		Map<String, String> params = new HashMap<String, String>();
		String ids = "";
		for (Integer id : entityIdArray) {
			ids += id + ",";
		}
		if (ids.length() > 0) {
			ids = ids.substring(0, ids.length() - 1);
		}
		params.put("ids", ids);
		params.put("includeDependentEntities", "" + includeDependentEntities);
		try {
			JSONHttpClient httpClient = new JSONHttpClient();
			return httpClient.GetWithHeader(ServiceUrl.GET_TASKS, headers, params);
		} catch (Exception e) {
			Log.e(CommonConstants.DEBUG_TAG, e.toString());
		}
		return null;
	}

	public static HttpURLConnection getResponseForDeleteEntity(Context context, long id,
			long lastMod, String authToken) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("id", "" + id);
		params.put("lastMod", "" + lastMod);
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", String.format("Bearer %s", authToken));
		JSONHttpClient httpClient = new JSONHttpClient();
		// give it several tries
		int triesCount = 1;
		for (int i = 0; i < triesCount; i++) {
			try {
				HttpURLConnection urlConnection = httpClient.Delete(
						ServiceUrl.DELETE_TASK, headers, params);
				return urlConnection;
			} catch (Exception e) {
				Log.e(CommonConstants.DEBUG_TAG, e.toString());
				if (i < triesCount - 1) {
					try {
						Thread.sleep(1000 * i);
					} catch (InterruptedException e1) {
					}
					continue;
				}
			}
		}
		return null;
	}

	public static String createQueryStringForParameters(Map<String, String> parameters)
			throws UnsupportedEncodingException {
		StringBuilder parametersAsQueryString = new StringBuilder();
		if (parameters != null && parameters.size() > 0) {
			for (String parameterName : parameters.keySet()) {
				parametersAsQueryString
						.append(parameterName)
						.append("=")
						.append(URLEncoder.encode(parameters.get(parameterName), "UTF-8"))
						.append("&");
			}
		}
		if (parametersAsQueryString.toString().length() > 0) {
			return parametersAsQueryString.substring(0,
					parametersAsQueryString.length() - 1);
		} else {
			return parametersAsQueryString.toString();
		}
	}
}
