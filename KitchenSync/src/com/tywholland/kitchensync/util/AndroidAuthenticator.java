
package com.tywholland.kitchensync.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.pras.auth.Authenticator;

public class AndroidAuthenticator implements Authenticator
{
    private final String TAG = "AndroidAuthenticator";
    private Activity activity;
    private AccountManager manager;
    private String mService = null;
    private String mAuthToken = "";

    public AndroidAuthenticator(Activity activity)
    {
        this.activity = activity;
        manager = AccountManager.get(activity.getApplicationContext());
    }

    public AndroidAuthenticator(Context context)
    {
        manager = AccountManager.get(context);
    }

    public String getAuthToken(String service)
    {
        if (service == null)
        {
            throw new IllegalAccessError("No Service name defined, Can't create Auth Token...");
        }

        if (mService != null && !mService.equals(service))
        {
            // Reset previous Token
            manager.invalidateAuthToken("com.google", mAuthToken);
        }

        Account[] acs = manager.getAccountsByType("com.google");
        Log.i(TAG, "Num of Matching account: " + acs.length);

        for (int i = 0; i < acs.length; i++)
        {
            if (acs[i].type.equals("com.google"))
            {
                // The first Gmail Account will be selected
                Log.i(TAG, "Selected Google Account " + acs[i].name);
                AccountManagerFuture result;
                if (activity != null)
                {
                    result = (AccountManagerFuture) (manager.getAuthToken(
                            acs[i],
                            service, null, activity,
                            null, null));
                }
                else
                {
                    result = (AccountManagerFuture) (manager.getAuthToken(
                            acs[i], service, true, null, null));
                }

                try
                {
                    Bundle b = (Bundle) result.getResult();
                    mAuthToken = b.getString(AccountManager.KEY_AUTHTOKEN);
                    Log.i(TAG, "Auth_Token: " + mAuthToken);
                    return mAuthToken;
                } catch (Exception ex)
                {
                    Log.i(TAG, "Error: " + ex.toString());
                }
            }
        }
        Log.i(TAG, "Problem in getting Auth Token...");
        return null;
    }
}
