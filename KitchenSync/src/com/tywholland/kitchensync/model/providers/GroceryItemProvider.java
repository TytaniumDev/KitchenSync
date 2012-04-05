package com.tywholland.kitchensync.model.providers;

import java.util.HashMap;

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

import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.tywholland.kitchensync.model.grocery.GroceryListDatabase;

public class GroceryItemProvider extends ContentProvider
{
   private static final String TAG = "GroceryItemProvider";
   public static final String             AUTHORITY               = "com.tywholland.kitchensync.model.providers.GroceryItemProvider";
   private static final UriMatcher        sUriMatcher             = new UriMatcher(UriMatcher.NO_MATCH);
   private static final int               GROCERYITEMS            = 100;
   private static final int               RECENTITEMS             = 110;
   private static final String            GROCERYITEMS_TABLE_NAME = GroceryListDatabase.TABLE_GROCERY;
   private static final String            RECENTITEMS_TABLE_NAME  = GroceryListDatabase.TABLE_RECENT;

   private static HashMap<String, String> groceryItemProjectionMap;
   private GroceryListDatabase            dbHelper;

   @Override
   public int delete(Uri uri, String where, String[] whereArgs)
   {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      int count;
      switch (sUriMatcher.match(uri))
      {
         case GROCERYITEMS:
            Log.i(TAG, "Deleting a grocery item");
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
               Log.i(TAG, "Inserting a grocery item: " + values.getAsString(GroceryItems.ITEMNAME));
               if (!values.containsKey(GroceryItems.ROWINDEX))
               {
                  values.put(GroceryItems.ROWINDEX, "");
               }
               rowId = db.replace(GROCERYITEMS_TABLE_NAME, null, values);
               if (rowId > 0)
               {
                  Uri groceryitemsUri = ContentUris.withAppendedId(GroceryItems.CONTENT_URI, rowId);
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
                  rowId = db.update(RECENTITEMS_TABLE_NAME, values, RecentItems.ITEMNAME + "=?", new String[]
                  { itemName });
                  db.execSQL("UPDATE " + RECENTITEMS_TABLE_NAME + " SET " + RecentItems.FREQUENCY + "="
                        + RecentItems.FREQUENCY + "+1, " + RecentItems.TIMESTAMP + "=" + System.currentTimeMillis()
                        + " WHERE " + RecentItems.ITEMNAME + "=?", new String[]
                  { itemName });
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
                  Uri recentitemsUri = ContentUris.withAppendedId(RecentItems.CONTENT_URI, rowId);
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
      return true;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
   {
      SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
      Cursor cursor;
      switch (sUriMatcher.match(uri))
      {
         case GROCERYITEMS:
            Log.i(TAG, "Querying a grocery item");
            qb.setTables(GROCERYITEMS_TABLE_NAME);
            qb.setProjectionMap(groceryItemProjectionMap);
            cursor = qb.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null,
                  sortOrder);
            break;

         case RECENTITEMS:
            Log.i(TAG, "Querying a recent item");
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String query = "SELECT * FROM " + RECENTITEMS_TABLE_NAME + " WHERE NOT EXISTS (SELECT * FROM "
                  + GROCERYITEMS_TABLE_NAME + " WHERE " + RECENTITEMS_TABLE_NAME + "." + RecentItems.ITEMNAME + " = "
                  + GROCERYITEMS_TABLE_NAME + "." + GroceryItems.ITEMNAME + ") ORDER BY " + RecentItems.FREQUENCY
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
            count = db.update(RECENTITEMS_TABLE_NAME, values, where, whereArgs);
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
      { itemname }, itemname + "=?", new String[]
      { (String) values.get(itemname) }, null, null, null)).getCount() > 0;

   }

}
