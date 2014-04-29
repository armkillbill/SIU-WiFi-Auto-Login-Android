package com.armkillbill.android.siuwifiautologin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.widget.Toast;

public class MainActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private final int TIMES_TO_TAP = 10;
	private int developerTabCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

		Preference developerPref = findPreference("developerPref");
		developerPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference p) {

						developerTabCount++;

						if (developerTabCount == TIMES_TO_TAP) {
							Toast.makeText(getApplicationContext(),
									"Congratulation!", Toast.LENGTH_LONG)
									.show();
						} else if (developerTabCount >= 5) {
							String msg = "Tab "
									+ (TIMES_TO_TAP - developerTabCount)
									+ " more time(s)";
							Toast.makeText(getApplicationContext(), msg,
									Toast.LENGTH_SHORT).show();
						}

						if (developerTabCount == TIMES_TO_TAP) {
							developerTabCount = 0;
							try {
								String facebookUri = getResources().getString(
										R.string.developer_facebook);
								Intent intent = new Intent(Intent.ACTION_VIEW,
										Uri.parse(facebookUri));
								startActivity(intent);
								return true;
							} catch (Exception e) {
								String url = getResources().getString(
										R.string.developer_website);
								Intent intent = new Intent(Intent.ACTION_VIEW,
										Uri.parse(url));
								startActivity(intent);
								return true;
							}
						}
						return false;
					}
				});

		Preference versionPref = findPreference("versionPref");
		versionPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference p) {

						try {
							String marketUri = getResources().getString(
									R.string.app_market);
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(marketUri));
							startActivity(intent);
							return true;
						} catch (Exception e) {
							String url = getResources().getString(
									R.string.app_website);
							Intent intent = new Intent(Intent.ACTION_VIEW, Uri
									.parse(url));
							startActivity(intent);
							return true;
						}
					}
				});
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceCategory) {
			PreferenceCategory pCat = (PreferenceCategory) p;
			for (int i = 0; i < pCat.getPreferenceCount(); i++) {
				initSummary(pCat.getPreference(i));
			}
		} else {
			updatePrefSummary(p);
		}
	}

	private void updatePrefSummary(Preference p) {
		if (p.getKey().equals("usernamePref")) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			String summary = editTextPref.getText();
			if (summary == null || summary.length() == 0) {
				summary = getResources().getString(
						R.string.pref_summary_username);
			}
			p.setSummary(summary);
		} else if (p.getKey().equals("passwordPref")) {
			EditTextPreference editTextPref = (EditTextPreference) p;
			String password = editTextPref.getText();

			int charCount;
			if (password == null) {
				charCount = 0;
			} else {
				charCount = editTextPref.getText().length();
			}

			String summary;
			if (charCount == 0) {
				summary = "Enter your password here";
			} else {
				summary = "";
				for (int i = 0; i < charCount; i++) {
					summary += "•";
				}
			}
			p.setSummary(summary);
		} else if (p.getKey().equals("versionPref")) {
			PackageInfo pInfo;
			String version = "";
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				version = pInfo.versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			p.setSummary(version);
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		updatePrefSummary(findPreference(key));

	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

}
