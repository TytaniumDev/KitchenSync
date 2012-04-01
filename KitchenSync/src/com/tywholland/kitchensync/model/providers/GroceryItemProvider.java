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

import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItemsDatabase;

public class GroceryItemProvider extends ContentProvider
{
   private static final String            DEBUG_TAG               = "GroceryItemsProvider";
   public static final String             AUTHORITY               = "com.tywholland.kitchensync.model.providers.GroceryItemProvider";
   private static final UriMatcher        sUriMatcher;
   private static final int               GROCERYITEMS            = 1;
   private static final String            GROCERYITEMS_TABLE_NAME = GroceryItemsDatabase.TABLE_GROCERY;

   private static HashMap<String, String> groceryItemProjectionMap;
   private GroceryItemsDatabase           dbHelper;

   @Override
   public int delete(Uri uri, String where, String[] whereArgs)
   {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      int count;
      switch (sUriMatcher.match(uri))
      {
         case GROCERYITEMS:
            count = db.delete(GROCERYITEMS_TABLE_NAME, where, whereArgs);
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
            return GroceryItems.CONTENT_TYPE;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }
   }

   @Override
   public Uri insert(Uri uri, ContentValues initialValues)
   {
      if (sUriMatcher.match(uri) != GROCERYITEMS)
      {
         throw new IllegalArgumentException("Unknown URI " + uri);
      }

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
      long rowId = db.replace(GROCERYITEMS_TABLE_NAME, null, values);
      if (rowId > 0)
      {
         Uri groceryitemsUri = ContentUris.withAppendedId(GroceryItems.CONTENT_URI, rowId);
         getContext().getContentResolver().notifyChange(groceryitemsUri, null);
         return groceryitemsUri;
      }

      throw new SQLException("Failed to insert row into " + uri);
   }

   @Override
   public boolean onCreate()
   {
      dbHelper = new GroceryItemsDatabase(getContext());
      return true;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
   {
      SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

      switch (sUriMatcher.match(uri))
      {
         case GROCERYITEMS:
            qb.setTables(GROCERYITEMS_TABLE_NAME);
            qb.setProjectionMap(groceryItemProjectionMap);
            break;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }
      Cursor cursor = qb.query(dbHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null,
            sortOrder);
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
            count = db.update(GROCERYITEMS_TABLE_NAME, values, where, whereArgs);
            break;

         default:
            throw new IllegalArgumentException("Unknown URI " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }
   
   static
   {
      sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
      sUriMatcher.addURI(AUTHORITY, GROCERYITEMS_TABLE_NAME, GROCERYITEMS);

      groceryItemProjectionMap = new HashMap<String, String>();
      groceryItemProjectionMap.put(GroceryItems.GROCERY_ITEM_ID, GroceryItems.GROCERY_ITEM_ID);
      groceryItemProjectionMap.put(GroceryItems.ITEMNAME, GroceryItems.ITEMNAME);
      groceryItemProjectionMap.put(GroceryItems.AMOUNT, GroceryItems.AMOUNT);
      groceryItemProjectionMap.put(GroceryItems.STORE, GroceryItems.STORE);
      groceryItemProjectionMap.put(GroceryItems.CATEGORY, GroceryItems.CATEGORY);
      groceryItemProjectionMap.put(GroceryItems.ROWINDEX, GroceryItems.ROWINDEX);

   }

}
