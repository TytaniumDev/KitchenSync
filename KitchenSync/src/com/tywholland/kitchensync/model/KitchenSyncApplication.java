
package com.tywholland.kitchensync.model;

import android.app.Activity;
import android.app.Application;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.os.Bundle;

import com.pras.SpreadSheetFactory;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.providers.GroceryItemProvider;
import com.tywholland.kitchensync.util.AndroidAuthenticator;

public class KitchenSyncApplication extends Application
{
    private AndroidAuthenticator mAuth = null;
    public static final String ANDROID_AUTH = "androidauth";

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
                Bundle auth = new Bundle();
                auth.putSerializable(ANDROID_AUTH, mAuth);
                if(android.os.Build.VERSION.SDK_INT >= 11)
                {
                    getContentResolver().call(GroceryItems.CONTENT_URI,
                            GroceryItemProvider.SET_ANDROID_AUTH_CALL, null,
                            auth);
                }
                else
                {
                    //GROSS STUFF
                    //Use startSync instead of call(SET_ANDROID_AUTH_CALL)
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(ANDROID_AUTH, auth);
                    getContentResolver().startSync(GroceryItems.CONTENT_URI, bundle);
                }
            }
        }).start();
    }

    public AndroidAuthenticator getAndroidAuthenticator(Activity activity)
    {
        initAndroidAuth(activity);
        return mAuth;
    }
}
