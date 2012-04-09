
package com.tywholland.kitchensync.model;

import android.app.Activity;
import android.app.Application;

import com.pras.SpreadSheetFactory;
import com.tywholland.kitchensync.util.AndroidAuthenticator;

public class KitchenSyncApplication extends Application
{
    private AndroidAuthenticator mAuth = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
    }
    
    private void initAndroidAuth(Activity activity)
    {
        if (mAuth == null)
        {
            mAuth = new AndroidAuthenticator(activity);
        }
    }

    public void promptForAuth(Activity activity)
    {
        initAndroidAuth(activity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SpreadSheetFactory factory = SpreadSheetFactory.getInstance(mAuth);
                factory.getAllSpreadSheets();
            }
        }).start();
    }

    public AndroidAuthenticator getAndroidAuthenticator(Activity activity)
    {
        initAndroidAuth(activity);
        return mAuth;
    }
}
