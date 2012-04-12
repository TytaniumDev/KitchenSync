
package com.tywholland.kitchensync.model.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.tywholland.kitchensync.model.KitchenSyncApplication;
import com.tywholland.kitchensync.model.adapter.GoogleDocsAdapter;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.tywholland.kitchensync.model.grocery.GroceryListDatabase;
import com.tywholland.kitchensync.util.AndroidAuthenticator;
import com.tywholland.kitchensync.util.grocery.GroceryItemUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class GroceryItemProvider extends ContentProvider
{
    private static final String TAG = "GroceryItemProvider";
    public static final String AUTHORITY = "com.tywholland.kitchensync.model.providers.GroceryItemProvider";
    public static final String SYNC_WITH_GOOGLE_DOCS_CALL = "syncWithGoogleDocs";
    public static final String SET_ANDROID_AUTH_CALL = "setAndroidAuth";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int GROCERYITEMS = 100;
    private static final int RECENTITEMS = 110;
    private static final String GROCERYITEMS_TABLE_NAME = GroceryListDatabase.TABLE_GROCERY;
    private static final String RECENTITEMS_TABLE_NAME = GroceryListDatabase.TABLE_RECENT;

    private static HashMap<String, String> groceryItemProjectionMap;
    private GroceryListDatabase dbHelper;
    private GoogleDocsAdapter gdocsHelper = null;

    @Override
    public int delete(Uri uri, String where, String[] whereArgs)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri))
        {
            case GROCERYITEMS:
                Log.i(TAG, "Deleting a grocery item");
                // Only use the first whereArgs, as the second part is used for
                // the rowIndex for GoogleDocs syncing
                count = db.delete(GROCERYITEMS_TABLE_NAME, where, new String[] {
                        whereArgs[0]
                });
                // Sync with google docs
                if (isGoogleDocsEnabled() && whereArgs.length >= 2 && whereArgs[1].length() > 0)
                {
                    ContentValues values = new ContentValues();
                    values.put(GroceryItems.ROWINDEX, whereArgs[1]);
                    gdocsHelper.deleteGroceryItem(values);
                }
                break;
            case RECENTITEMS:
                Log.i(TAG, "Deleting a recent item");
                count = db.delete(RECENTITEMS_TABLE_NAME, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri)
    {
        switch (sUriMatcher.match(uri))
        {
            case GROCERYITEMS:
                Log.i(TAG, "Getting type of grocery item");
                return GroceryItems.CONTENT_TYPE;
            case RECENTITEMS:
                Log.i(TAG, "Getting type of recent item");
                return RecentItems.CONTENT_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues)
    {
        int uriMatchCode = sUriMatcher.match(uri);
        if (uriMatchCode != -1)
        {
            ContentValues values;
            if (initialValues != null)
            {
                values = new ContentValues(initialValues);
            }
            else
            {
                values = new ContentValues();
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            long rowId;

            switch (sUriMatcher.match(uri))
            {
                case GROCERYITEMS:
                    Log.i(TAG,
                            "Inserting a grocery item: "
                                    + values.getAsString(GroceryItems.ITEMNAME));
                    if (!values.containsKey(GroceryItems.ROWINDEX))
                    {
                        values.put(GroceryItems.ROWINDEX, "");
                    }
                    rowId = db.replace(GROCERYITEMS_TABLE_NAME, null, values);
                    // Sync with google docs
                    if (isGoogleDocsEnabled())
                    {
                        gdocsHelper.addGroceryItem(values);
                    }
                    if (rowId > 0)
                    {
                        Uri groceryitemsUri = ContentUris.withAppendedId(GroceryItems.CONTENT_URI,
                                rowId);
                        getContext().getContentResolver().notifyChange(groceryitemsUri, null);
                        return groceryitemsUri;
                    }
                    throw new SQLException("Failed to insert row into " + uri);

                case RECENTITEMS:

                    Log.i(TAG, "Inserting a recent item");
                    String itemName = values.getAsString(RecentItems.ITEMNAME);
                    if (doesGroceryItemExist(RECENTITEMS_TABLE_NAME, values, db))
                    {
                        // Increment frequency and update timestamp
                        rowId = db.update(RECENTITEMS_TABLE_NAME, values, RecentItems.ITEMNAME
                                + "=?", new String[]
                        {
                                itemName
                        });
                        db.execSQL("UPDATE " + RECENTITEMS_TABLE_NAME + " SET "
                                + RecentItems.FREQUENCY + "="
                                + RecentItems.FREQUENCY + "+1, " + RecentItems.TIMESTAMP + "="
                                + System.currentTimeMillis()
                                + " WHERE " + RecentItems.ITEMNAME + "=?", new String[]
                        {
                                itemName
                        });
                    }
                    else
                    {
                        // New value
                        values.put(RecentItems.FREQUENCY, 1);
                        values.put(RecentItems.TIMESTAMP, System.currentTimeMillis());
                        rowId = db.insert(RECENTITEMS_TABLE_NAME, null, values);
                    }

                    if (rowId > 0)
                    {
                        Uri recentitemsUri = ContentUris.withAppendedId(RecentItems.CONTENT_URI,
                                rowId);
                        getContext().getContentResolver().notifyChange(recentitemsUri, null);
                        return recentitemsUri;
                    }
                    throw new SQLException("Failed to insert row into " + uri);

                default:
                    throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }
        else
        {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

    }

    @Override
    public boolean onCreate()
    {
        dbHelper = new GroceryListDatabase(getContext());
        // if (isNetworkAvailable())
        // {
        // gdocsHelper = new GoogleDocsAdapter(new
        // AndroidAuthenticator(getContext()),
        // getContext()
        // .getContentResolver());
        // }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder)
    {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Cursor cursor;
        switch (sUriMatcher.match(uri))
        {
            case GROCERYITEMS:
                Log.i(TAG, "Querying a grocery item");
                qb.setTables(GROCERYITEMS_TABLE_NAME);
                qb.setProjectionMap(groceryItemProjectionMap);
                cursor = qb.query(dbHelper.getReadableDatabase(), projection, selection,
                        selectionArgs, null, null,
                        sortOrder);
                break;

            case RECENTITEMS:
                Log.i(TAG, "Querying a recent item");
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                String query = "SELECT * FROM " + RECENTITEMS_TABLE_NAME
                        + " WHERE NOT EXISTS (SELECT * FROM "
                        + GROCERYITEMS_TABLE_NAME + " WHERE " + RECENTITEMS_TABLE_NAME + "."
                        + RecentItems.ITEMNAME + " = "
                        + GROCERYITEMS_TABLE_NAME + "." + GroceryItems.ITEMNAME + ") ORDER BY "
                        + RecentItems.FREQUENCY
                        + " DESC ";
                cursor = db.rawQuery(query, null);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri))
        {
            case GROCERYITEMS:
                Log.i(TAG, "Updating a grocery item");
                count = db.update(GROCERYITEMS_TABLE_NAME, values, where, whereArgs);
                // Sync with google docs
                if (isGoogleDocsEnabled() && values.getAsString(GroceryItems.ROWINDEX).length() > 0)
                {
                    gdocsHelper.editGroceryItem(values);
                }
                break;

            case RECENTITEMS:
                Log.i(TAG, "Updating a recent item");
                count = db.update(RECENTITEMS_TABLE_NAME, values, where, whereArgs);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        if (isGoogleDocsEnabled() && method.equals(SYNC_WITH_GOOGLE_DOCS_CALL))
        {
            syncWithGoogleDocs();
        }
        else if (method.equals(SET_ANDROID_AUTH_CALL))
        {
            setAndroidAuth((AndroidAuthenticator) extras.get(KitchenSyncApplication.ANDROID_AUTH));
        }
        return super.call(method, arg, extras);
    }

    private void syncWithGoogleDocs()
    {
        Log.i("GroceryItemProvider", "Syncing with google docs!");
        new Thread(new Runnable() {

            @Override
            public void run() {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                // Grab all data
                HashMap<String, GroceryItem> googleDocsData = gdocsHelper.getGroceryListMap();
                ArrayList<GroceryItem> sqlData = getSqlData();
                // Go through every entry in sql:
                // If it has a rowindex, see if it is in googleDocsData. If it
                // is, update it. If it is not, delete it. Remove item from
                // googleDocsData
                // Add remaining googleDocsData to sql
                for (GroceryItem item : sqlData)
                {
                    if (item.getRowIndex().length() > 0)
                    {
                        String itemName = item.getItemName();
                        if (googleDocsData.containsKey(itemName))
                        {
                            Log.i("GroceryItemProvider", "GoogleDocsSync: updating row in sql");
                            db.updateWithOnConflict(GROCERYITEMS_TABLE_NAME,
                                    GroceryItemUtil.makeContentValuesFromGroceryItem(googleDocsData
                                            .get(itemName)), GroceryItems.ITEMNAME + "=?",
                                    new String[] {
                                        itemName
                                    }, SQLiteDatabase.CONFLICT_REPLACE);
                        }
                        else
                        {
                            Log.i("GroceryItemProvider", "GoogleDocsSync: deleting row in sql");
                            // It was deleted on google docs, remove from sql
                            db.delete(GROCERYITEMS_TABLE_NAME, GroceryItems.ITEMNAME + "=?",
                                    new String[] {
                                        itemName
                                    });
                            // Add to recent items
                            ContentValues recentValues = GroceryItemUtil
                                    .makeContentValuesFromGroceryItem(item);
                            recentValues.remove(GroceryItems.ROWINDEX);
                            db.insert(RECENTITEMS_TABLE_NAME, null,
                                    recentValues);
                        }
                        // Remove from googleDocsData so we don't add it at the
                        // end
                        googleDocsData.remove(itemName);
                    }
                    else
                    {
                        gdocsHelper.addGroceryItem(GroceryItemUtil.makeContentValuesFromGroceryItem(item));
                    }
                }
                for (GroceryItem itemToAdd : googleDocsData.values())
                {
                    Log.i("GroceryItemProvider", "GoogleDocsSync: inserting row into sql");
                    db.insert(GROCERYITEMS_TABLE_NAME, null,
                            GroceryItemUtil.makeContentValuesFromGroceryItem(itemToAdd));
                }
                Log.i("GroceryItemProvider", "GoogleDocsSync: about to notify change");
                getContext().getContentResolver().notifyChange(GroceryItems.CONTENT_URI, null);
                // Log.i("GroceryItemProvider",
                // "GoogleDocsSync: about to close database");
                // db.close();
            }
        }).start();
    }

    private void setAndroidAuth(AndroidAuthenticator auth)
    {
        if(auth.getAuthToken("wise") != null)
        {
            gdocsHelper = new GoogleDocsAdapter(auth, getContext().getContentResolver());
            Log.i("GroceryItemProvider", "Got an auth token, gdocshelper enabled");
        }
        else
        {
            Log.i("GroceryItemProvider", "Didn't get an auth token, gdocshelper should be null");
        }
    }

    private ArrayList<GroceryItem> getSqlData()
    {
        ArrayList<GroceryItem> sqlData = new ArrayList<GroceryItem>();
        String[] projection =
        {
                GroceryItems.GROCERY_ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT,
                GroceryItems.STORE,
                GroceryItems.CATEGORY, GroceryItems.ROWINDEX
        };

        Cursor sqlCursor = query(GroceryItems.CONTENT_URI, projection, null, null, null);
        sqlCursor.moveToFirst();

        while (!sqlCursor.isAfterLast())
        {
            sqlData.add(GroceryItemUtil.makeGroceryItemFromCursor(sqlCursor));
            sqlCursor.moveToNext();
        }
        sqlCursor.close();
        return sqlData;
    }

    static
    {
        sUriMatcher.addURI(AUTHORITY, GROCERYITEMS_TABLE_NAME, GROCERYITEMS);
        sUriMatcher.addURI(AUTHORITY, RECENTITEMS_TABLE_NAME, RECENTITEMS);

        groceryItemProjectionMap = new HashMap<String, String>();
        groceryItemProjectionMap.put(GroceryItems.GROCERY_ITEM_ID, GroceryItems.GROCERY_ITEM_ID);
        groceryItemProjectionMap.put(GroceryItems.ITEMNAME, GroceryItems.ITEMNAME);
        groceryItemProjectionMap.put(GroceryItems.AMOUNT, GroceryItems.AMOUNT);
        groceryItemProjectionMap.put(GroceryItems.STORE, GroceryItems.STORE);
        groceryItemProjectionMap.put(GroceryItems.CATEGORY, GroceryItems.CATEGORY);
        groceryItemProjectionMap.put(GroceryItems.ROWINDEX, GroceryItems.ROWINDEX);
    }

    private boolean doesGroceryItemExist(String tableName, ContentValues values, SQLiteDatabase db)
    {
        String itemname = GroceryItems.ITEMNAME;
        return (db.query(tableName, new String[]
        {
                itemname
        }, itemname + "=?", new String[]
        {
                (String) values.get(itemname)
        }, null, null, null)).getCount() > 0;

    }
    
    private boolean isGoogleDocsEnabled()
    {
        if(gdocsHelper != null)
        {
            if(isNetworkAvailable())
            {
                return true;
            }
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
