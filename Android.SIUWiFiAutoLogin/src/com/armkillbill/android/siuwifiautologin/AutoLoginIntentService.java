package com.armkillbill.android.siuwifiautologin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AutoLoginIntentService extends IntentService {

	public AutoLoginIntentService() {
		super("AutoLoginService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Intent i = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);

		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				this);
		builder.setSmallIcon(R.drawable.ic_launcher)
				.setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setContentTitle("SIU WiFi Auto Login").setContentIntent(pi);

		if (sp.getBoolean("showOnGoingNotificationPref", true)) {
			builder.setOngoing(true);
		}

		String url = getResources().getString(R.string.testing_url);

		StringBuilder result = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			result = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Log.d("tag", line);
				result.append(line);
			}

			reader.close();

			// Log.d("tag", result.toString());
			String html = result.toString();
			if (!findTestingTitle(html)) {
				Log.d("tag", "This is SIU wifi login page");

				String magic = findMagic(html);
				Log.d("tag", "magic = " + magic);
				String redir = find4Tredir(html);
				Log.d("tag", "4Tredir = " + redir);

				HttpPost httpPost = new HttpPost(url);

				String username = sp.getString("usernamePref", null);
				String password = sp.getString("passwordPref", null);

				Log.d("tag", "username = " + username);
				// Log.d("tag", "password = " + password);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("magic", magic));
				nameValuePairs.add(new BasicNameValuePair("4Tredir", redir));
				nameValuePairs
						.add(new BasicNameValuePair("username", username));
				nameValuePairs
						.add(new BasicNameValuePair("password", password));
				nameValuePairs.add(new BasicNameValuePair("submit", "Login"));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				response = httpClient.execute(httpPost);
				reader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				result = new StringBuilder();
				line = null;
				while ((line = reader.readLine()) != null) {
					// Log.d("response", line);
					result.append(line);
				}

				reader.close();

				html = result.toString();
				if (!findTestingTitle(html)) {
					if (findAuthenFail(html)) {
						Log.d("tag", "Authentication Failed");

						builder.setTicker("Login Failed")
								.setContentTitle("Invalid username/password")
								.setContentText(
										"Tap to change username/password")
								.setOngoing(false);
						Notification note = builder.build();
						nm.cancelAll();
						nm.notify(100, note);
					} else {
						Log.d("tag", "Something wrong");

						builder.setTicker("Login Failed")
								.setContentText(
										"Please turn WiFi off and on again.")
								.setContentTitle("Login Failed")
								.setOngoing(false);

						Notification note = builder.build();
						nm.cancelAll();
						nm.notify(100, note);
					}
				} else {
					Log.d("tag", "Login successfully");

					boolean showNotification = sp.getBoolean(
							"showNotificationPref", true);

					if (showNotification) {
						builder.setTicker("Login successful")
								.setContentText("Login successful")
								.setAutoCancel(false);
						Notification note = builder.build();
						nm.cancelAll();
						nm.notify(100, note);
					}
				}
			} else {
				Log.d("tag", "Already login");

				boolean showNotification = sp.getBoolean(
						"showNotificationPref", true);

				if (showNotification) {

					builder.setTicker("Connected to SIU WiFi")
							.setContentText("Connected to SIU WiFi")
							.setAutoCancel(false);
					Notification note = builder.build();
					nm.cancelAll();
					nm.notify(100, note);
				}
			}
		} catch (UnknownHostException e) {
			builder.setTicker("Network Error")
					.setContentText("Please turn WiFi off and on again.")
					.setContentTitle("Network Error");
			Notification note = builder.build();
			nm.cancelAll();
			nm.notify(100, note);
		} catch (Exception e) {
			e.printStackTrace();
			builder.setTicker("Unknown Error")
					.setContentText("Please turn WiFi off and on again.")
					.setContentTitle("Unknown Error");
			Notification note = builder.build();
			nm.cancelAll();
			nm.notify(100, note);
		}

	}

	private boolean findAuthenFail(String html) {
		int titleIndex = html.indexOf("Authentication Failed");
		if (titleIndex == -1) {
			return false;
		}
		return true;
	}

	private String find4Tredir(String html) throws Exception {
		Pattern pattern = Pattern
				.compile("((?i)(?<=name=\"4Tredir\" value=\"))[^\">]+");
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			return matcher.group();
		}

		throw new Exception();
	}

	private String findMagic(String html) throws Exception {
		Pattern pattern = Pattern
				.compile("((?i)(?<=name=\"magic\" value=\"))\\w[^\">]+");
		Matcher matcher = pattern.matcher(html);
		if (matcher.find()) {
			return matcher.group();
		}

		throw new Exception();
	}

	private boolean findTestingTitle(String html) {
		String test_title = getResources().getString(R.string.testing_title);
		int titleIndex = html.indexOf(test_title);
		if (titleIndex == -1) {
			return false;
		}
		return true;
	}

}
