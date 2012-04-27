
package com.hollanddev.kitchensync.model;

import android.app.Activity;
import android.app.Application;

import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;
import com.hollanddev.kitchensync.util.AndroidAuthenticator;
import com.pras.SpreadSheetFactory;

public class KitchenSyncApplication extends Application
{
    private AndroidAuthenticator mAuth = null;
    public static final String ANDROID_AUTH = "androidauth";
    private GoogleDocsProviderWrapper gDocsProviderWrapper;

    @Override
    public void onCreate()
    {
        super.onCreate();
        gDocsProviderWrapper = new GoogleDocsProviderWrapper(this);
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
                gDocsProviderWrapper.setAndroidAuth(mAuth);
            }
        }).start();
    }

    public AndroidAuthenticator getAndroidAuthenticator(Activity activity)
    {
        initAndroidAuth(activity);
        return mAuth;
    }

    public GoogleDocsProviderWrapper getGoogleDocsProviderWrapper()
    {
        return gDocsProviderWrapper;
    }

}
