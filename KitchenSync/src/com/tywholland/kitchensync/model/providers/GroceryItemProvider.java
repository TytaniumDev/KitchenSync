
package com.tywholland.kitchensync.model.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.tywholland.kitchensync.model.adapter.SQLiteAdapter;
import com.tywholland.kitchensync.model.grocery.GroceryItem.Categories;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.Stores;

import java.util.HashMap;

public class GroceryItemProvider extends ContentProvider
{
    private static final String TAG = "GroceryItemProvider";
    public static final String AUTHORITY = "com.tywholland.kitchensync.model.providers.GroceryItemProvider";
    public static final String SYNC_WITH_GOOGLE_DOCS_CALL = "syncWithGoogleDocs";
    public static final String SET_ANDROID_AUTH_CALL = "setAndroidAuth";
    public static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    public static final int GROCERYITEMS = 100;
    public static final int RECENTITEMS = 110;
    public static final int STORES = 120;
    public static final int CATEGORIES = 130;
    public static final String GROCERYITEMS_TABLE_NAME = SQLiteAdapter.TABLE_GROCERY;
    public static final String RECENTITEMS_TABLE_NAME = SQLiteAdapter.TABLE_RECENT;
    public static final String STORES_TABLE_NAME = SQLiteAdapter.TABLE_STORES;
    public static final String CATEGORIES_TABLE_NAME = SQLiteAdapter.TABLE_CATEGORIES;

    private static HashMap<String, String> groceryItemProjectionMap;
    private SQLiteAdapter dbHelper;

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
                count = db.delete(GROCERYITEMS_TABLE_NAME, where, whereArgs);
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

                case STORES:
                    Log.i(TAG, "Inserting a store");
                    executeIncrementSQL(db, STORES_TABLE_NAME, Stores.FREQUENCY, Stores.STORE,
                            values.getAsString(Stores.STORE));
                    Uri storesUri = Stores.CONTENT_URI;
                    getContext().getContentResolver().notifyChange(storesUri, null);
                    return storesUri;

                case CATEGORIES:
                    Log.i(TAG, "Inserting a category");
                    executeIncrementSQL(db, CATEGORIES_TABLE_NAME, Categories.FREQUENCY,
                            Categories.CATEGORY, values.getAsString(Categories.CATEGORY));
                    Uri categoriesUri = Categories.CONTENT_URI;
                    getContext().getContentResolver().notifyChange(categoriesUri, null);
                    return categoriesUri;

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
        dbHelper = new SQLiteAdapter(getContext());
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
                // Log.i(TAG, "Querying a grocery item");
                qb.setTables(GROCERYITEMS_TABLE_NAME);
                qb.setProjectionMap(groceryItemProjectionMap);
                cursor = qb.query(dbHelper.getReadableDatabase(), projection, selection,
                        selectionArgs, null, null,
                        sortOrder);
                break;

            case RECENTITEMS:
                // Log.i(TAG, "Querying a recent item");
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
                break;

            case RECENTITEMS:
                Log.i(TAG, "Updating a recent item");
                count = db.updateWithOnConflict(RECENTITEMS_TABLE_NAME, values, where, whereArgs,
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static
    {
        sUriMatcher.addURI(AUTHORITY, GROCERYITEMS_TABLE_NAME, GROCERYITEMS);
        sUriMatcher.addURI(AUTHORITY, RECENTITEMS_TABLE_NAME, RECENTITEMS);
        sUriMatcher.addURI(AUTHORITY, STORES_TABLE_NAME, STORES);
        sUriMatcher.addURI(AUTHORITY, CATEGORIES_TABLE_NAME, CATEGORIES);

        groceryItemProjectionMap = new HashMap<String, String>();
        groceryItemProjectionMap.put(GroceryItems.ITEM_ID, GroceryItems.ITEM_ID);
        groceryItemProjectionMap.put(GroceryItems.ITEMNAME, GroceryItems.ITEMNAME);
        groceryItemProjectionMap.put(GroceryItems.AMOUNT, GroceryItems.AMOUNT);
        groceryItemProjectionMap.put(GroceryItems.STORE, GroceryItems.STORE);
        groceryItemProjectionMap.put(GroceryItems.CATEGORY, GroceryItems.CATEGORY);
        groceryItemProjectionMap.put(GroceryItems.ROWINDEX, GroceryItems.ROWINDEX);
    }

    // Helper methods
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

    private void executeIncrementSQL(SQLiteDatabase db, String tableName, String columnToIncrement,
            String uniqueKey, String uniqueKeyValue)
    {
        db.execSQL("INSERT OR REPLACE INTO "
                + tableName
                + " VALUES (?, COALESCE((SELECT " + columnToIncrement + " FROM " + tableName
                + "WHERE " + uniqueKey + "=? ), 0) + 1);", new String[] {
                uniqueKeyValue, uniqueKeyValue
        });
    }
}
