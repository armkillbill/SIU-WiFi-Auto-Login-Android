package com.armkillbill.android.siuwifiautologin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WiFiBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();

		if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(context);
			NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			NetworkInfo ni = intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

			if (ni != null && ni.getState().equals(NetworkInfo.State.CONNECTED)) {

				if (!sp.getBoolean("enableAutoLoginPref", true)) {
					Log.d("tag", "Auto login turned off");
					return;
				}

				WifiInfo wi = intent
						.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);

				String ssid = wi.getSSID();
				if (isSIUWiFi(ssid)) {
					Log.d("tag", "Connected to SIU WiFi");
					context.startService(new Intent(context,
							AutoLoginIntentService.class));
				}
			} else if (ni != null
					&& ni.getState().equals(NetworkInfo.State.DISCONNECTED)) {
				Log.d("tag", "Disconnected to SIU WiFi");
				nm.cancelAll();
			}
		}
	}

	private boolean isSIUWiFi(String ssid) {
		if (ssid.equals("SIU WiFi") || ssid.equals("\"SIU WiFi\"")) {
			return true;
		}
		return false;
	}

}
