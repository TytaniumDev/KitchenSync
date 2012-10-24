/*
 * Copyright (C) 2011 Prasanta Paul, http://prasanta-paul.blogspot.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hollanddev.kitchensync.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.pras.auth.Authenticator;
import com.pras.conn.HttpConHandler;
import com.pras.conn.Response;

import java.util.HashMap;

/**
 * @author Prasanta Paul
 * 
 */
public class AndroidAuthenticator implements Authenticator {

	Activity activity;
	AccountManager manager;
	private final String mService = null;
	private String auth_token = "";

	public AndroidAuthenticator(Activity activity) {
		this.activity = activity;
		manager = AccountManager.get(activity.getApplicationContext());
	}
	
	public String getAuthToken(String service) {
		if (service == null) {
			throw new IllegalAccessError(
					"No Service name defined, Can't create Auth Token...");
		}

		if (mService != null && !mService.equals(service)) {
			// Reset previous Token
			manager.invalidateAuthToken("com.google", auth_token);
		}

		Account[] acs = manager.getAccountsByType("com.google");
		Log.i("AndroidAuthenticator", "Num of Matching account: " + acs.length);

		if (acs == null || acs.length == 0) {
			Toast.makeText(this.activity.getApplicationContext(),
					"No Google Account Added...", Toast.LENGTH_LONG).show();
			return null;
		}

		for (int i = 0; i < acs.length; i++) {
			Account account = acs[i];
			if (account.type.equals("com.google")) {
				// The first Gmail Account will be selected
				String result = fetchAuthTokenFromAccount(service, account);
				if ((result != null) && (result.length() > 0)) {
					return result;
				}
			}
		}
		Log.i("AndroidAuthenticator", "Problem in getting Auth Token...");
		return null;
	}

	private String fetchAuthTokenFromAccount(String service, Account account) {
		Log.i("AndroidAuthenticator", "Selected Google Account " + account.name);

		int attempt = 2;

		while (attempt-- > 0) {
			AccountManagerFuture<Bundle> result = manager.getAuthToken(account,
					service, null, activity, null, null);
			try {
				Bundle b = result.getResult();
				auth_token = b.getString(AccountManager.KEY_AUTHTOKEN);
				Log.i("AndroidAuthenticator", "Auth_Token: " + auth_token);

				if (!validateAuthToken()) {
					Log.i("AndroidAuthenticator", "Invalidating auth token");
					manager.invalidateAuthToken(account.type, auth_token);
				} else {
					return auth_token;
				}
			} catch (Exception ex) {
				Log.i("AndroidAuthenticator", "Error: " + ex.toString());
			}
		}
		return null;
	}

	private boolean validateAuthToken() {
		HashMap<String, String> httpHeaders = new HashMap<String, String>();
		httpHeaders.put(HttpConHandler.AUTHORIZATION_HTTP_HEADER,
				"GoogleLogin auth=" + auth_token);
		httpHeaders.put(HttpConHandler.GDATA_VERSION_HTTP_HEADER, "3.0");

		HttpConHandler http = new HttpConHandler();
		String url = "https://spreadsheets.google.com/feeds/spreadsheets/private/full";

		Response res = http.doConnect(url, HttpConHandler.HTTP_GET,
				httpHeaders, null);

		Log.i("AndroidAuthenticator", "Auth token is valid: " + !res.isError());

		return !res.isError();
	}
}
