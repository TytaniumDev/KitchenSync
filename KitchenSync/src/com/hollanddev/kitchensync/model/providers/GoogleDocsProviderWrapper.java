
package com.hollanddev.kitchensync.model.providers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.GroceryItem;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.model.adapter.GoogleDocsAdapter;
import com.hollanddev.kitchensync.util.AndroidAuthenticator;
import com.hollanddev.kitchensync.util.GroceryItemUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleDocsProviderWrapper {

    private Context mContext;
    private GoogleDocsAdapter gDocsHelper = null;
    private ContentResolver mContentResolver;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public GoogleDocsProviderWrapper(Context appContext)
    {
        mContext = appContext;
        mContentResolver = appContext.getContentResolver();
    }

    // Wrapper methods for ContentProvider
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        return delete(uri, where, whereArgs, null);
    }
    public int delete(Uri uri, String where, String[] whereArgs, String rowIndex)
    {
        int count = mContentResolver.delete(uri, where, whereArgs);
        switch (sUriMatcher.match(uri))
        {
            case GroceryItemProvider.GROCERYITEMS:
                if (isGoogleDocsEnabled() && rowIndex != null && rowIndex.length() > 0)
                {
                    ContentValues values = new ContentValues();
                    values.put(GroceryItems.ROWINDEX, rowIndex);
                    gDocsHelper.deleteGroceryItem(values);
                }
        }
        return count;
    }

    public void notifyChange(Uri contentUri, ContentObserver object) {
        mContentResolver.notifyChange(contentUri, object);
    }
    public String getType(Uri uri)
    {
        return mContentResolver.getType(uri);
    }

    public Uri insert(Uri uri, ContentValues initialValues)
    {
        Uri retUri = mContentResolver.insert(uri, initialValues);
        switch (sUriMatcher.match(uri))
        {
            case GroceryItemProvider.GROCERYITEMS:
                ContentValues values;
                if (initialValues != null)
                {
                    values = new ContentValues(initialValues);
                }
                else
                {
                    values = new ContentValues();
                }
                if (!values.containsKey(GroceryItems.ROWINDEX))
                {
                    values.put(GroceryItems.ROWINDEX, "");
                }
                if (isGoogleDocsEnabled())
                {
                    gDocsHelper.addGroceryItem(values);
                }
        }
        return retUri;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder)
    {
        return mContentResolver.query(uri, projection, selection, selectionArgs,
                sortOrder);
    }

    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        int ret = mContentResolver.update(uri, values, where, whereArgs);
        switch (sUriMatcher.match(uri))
        {
            case GroceryItemProvider.GROCERYITEMS:
                if (isGoogleDocsEnabled() && values.getAsString(GroceryItems.ROWINDEX).length() > 0)
                {
                    gDocsHelper.editGroceryItem(values);
                }
        }
        return ret;
    }

    // Google Docs only methods
    public void syncWithGoogleDocs()
    {
        if (isGoogleDocsEnabled())
        {
            Log.i("GroceryItemProvider", "Syncing with google docs!");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // Grab all data
                    HashMap<String, GroceryItem> googleDocsData = gDocsHelper.getGroceryListMap();
                    if (googleDocsData != null)
                    {
                        ArrayList<GroceryItem> sqlData = getSqlData();
                        // Go through every entry in sql:
                        // If it has a rowindex, see if it is in googleDocsData.
                        // If it is, update it. If it is not, delete it. Remove
                        // item from googleDocsData
                        // Add remaining googleDocsData to sql
                        for (GroceryItem item : sqlData)
                        {
                            if (item.getRowIndex().length() > 0)
                            {
                                String itemName = item.getItemName();
                                if (googleDocsData.containsKey(itemName))
                                {
                                    //See if the two items are exactly the same, only update if they aren't
                                    if(!item.fullEquals(googleDocsData.get(itemName)))
                                    {
                                        Log.i("GroceryItemProvider", "GoogleDocsSync: updating "
                                                + itemName
                                                + " in sql");
                                        mContentResolver.update(
                                                GroceryItems.CONTENT_URI,
                                                GroceryItemUtil
                                                .makeContentValuesFromGroceryItem(googleDocsData
                                                        .get(itemName)), GroceryItems.ITEMNAME
                                                        + "=?", new String[] {
                                                    itemName
                                                });
                                    }
                                    else
                                    {
                                        Log.i("GroceryItemProvider", "GoogleDocsSync: items exactly the same, not updating");
                                    }
                                }
                                else
                                {
                                    Log.i("GroceryItemProvider", "GoogleDocsSync: deleting "
                                            + itemName
                                            + " in sql");
                                    // It was deleted on google docs, remove
                                    // from sql
                                    mContentResolver.delete(GroceryItems.CONTENT_URI,
                                            GroceryItems.ITEMNAME + "=?",
                                            new String[] {
                                                itemName
                                            });
                                    // Add to recent items
                                    ContentValues recentValues = GroceryItemUtil
                                            .makeContentValuesFromGroceryItem(item);
                                    recentValues.remove(GroceryItems.ROWINDEX);
                                    mContentResolver.insert(RecentItems.CONTENT_URI, recentValues);
                                }
                                // Remove from googleDocsData so we don't add it
                                // at the end
                                googleDocsData.remove(itemName);
                            }
                            else
                            {
                                gDocsHelper.addGroceryItem(GroceryItemUtil
                                        .makeContentValuesFromGroceryItem(item));
                            }
                        }
                        for (GroceryItem itemToAdd : googleDocsData.values())
                        {
                            Log.i("GroceryItemProvider", "GoogleDocsSync: inserting row into sql");
                            mContentResolver.insert(GroceryItems.CONTENT_URI,
                                    GroceryItemUtil.makeContentValuesFromGroceryItem(itemToAdd));
                        }
                        Log.i("GroceryItemProvider", "GoogleDocsSync: about to notify change");
                        mContext.getContentResolver().notifyChange(GroceryItems.CONTENT_URI, null);
                    }
                }
            }).start();
        }
    }

    private ArrayList<GroceryItem> getSqlData()
    {
        ArrayList<GroceryItem> sqlData = new ArrayList<GroceryItem>();
        String[] projection =
        {
                GroceryItems.ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT,
                GroceryItems.STORE,
                GroceryItems.CATEGORY, GroceryItems.ROWINDEX
        };

        Cursor sqlCursor = mContext.getContentResolver().query(GroceryItems.CONTENT_URI,
                projection, null, null, null);
        sqlCursor.moveToFirst();

        while (!sqlCursor.isAfterLast())
        {
            sqlData.add(GroceryItemUtil.makeGroceryItemFromCursor(sqlCursor));
            sqlCursor.moveToNext();
        }
        sqlCursor.close();
        return sqlData;
    }

    public void setAndroidAuth(AndroidAuthenticator auth)
    {
        if (auth.getAuthToken("wise") != null)
        {
            gDocsHelper = new GoogleDocsAdapter(auth, this);
            Log.i("GroceryItemProvider", "Got an auth token, gdocshelper enabled");
        }
        else
        {
            Log.i("GroceryItemProvider", "Didn't get an auth token, gdocshelper should be null");
        }
    }

    private boolean isGoogleDocsEnabled()
    {
        if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(
                mContext.getString(R.string.key_google_docs_sync), true))
        {
            if (gDocsHelper != null)
            {
                if (isNetworkAvailable())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    static
    {
        sUriMatcher.addURI(GroceryItemProvider.AUTHORITY,
                GroceryItemProvider.GROCERYITEMS_TABLE_NAME, GroceryItemProvider.GROCERYITEMS);
        sUriMatcher.addURI(GroceryItemProvider.AUTHORITY,
                GroceryItemProvider.RECENTITEMS_TABLE_NAME, GroceryItemProvider.RECENTITEMS);
    }

}
