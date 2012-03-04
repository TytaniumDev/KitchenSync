package com.tyhollan.kitchensync.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Data;
import android.util.Log;

public class RecentItemDBAdapter
{

   // Event fields
   public static final String  KEY_ID           = BaseColumns._ID;
   public static final String  KEY_ITEMNAME     = GroceryListDBAdapter.KEY_ITEMNAME;
   public static final String  KEY_AMOUNT       = GroceryListDBAdapter.KEY_AMOUNT;
   public static final String  KEY_STORE        = GroceryListDBAdapter.KEY_STORE;
   public static final String  KEY_CATEGORY     = GroceryListDBAdapter.KEY_CATEGORY;

   private static final String TAG              = "DBAdapter";
   private DatabaseHelper      mDbHelper;
   private SQLiteDatabase      mDb;

   /**
    * Database creation sql statement
    */
   private static final String DATABASE_NAME    = "data";
   private static final String RECENT_TABLE    = "recentitems";
   private static final String RECENT_CREATE   = "create table " + RECENT_TABLE + " (" + KEY_ID
                                                      + " integer primary key " + "autoincrement, " + KEY_ITEMNAME
                                                      + " text not null, " + KEY_AMOUNT + " text not null, "
                                                      + KEY_STORE + " text not null, " + KEY_CATEGORY
                                                      + " text not null, " + ");";

   private static final int    DATABASE_VERSION = 5;

   private final Context       mCtx;

   private class DatabaseHelper extends SQLiteOpenHelper
   {

      DatabaseHelper(Context context)
      {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db)
      {
         Log.i(TAG, "In DatabaseHelper.onCreate");
         db.execSQL(RECENT_CREATE);
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {
         // TODO: Make this alter tables instead of drop everything
         Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
               + ", which will destroy all data");
         Log.i(TAG, "DB version is: " + db.getVersion());
         db.execSQL("drop table " + RECENT_TABLE + ";");
         db.execSQL(RECENT_CREATE);
      }
   }

   /**
    * Constructor - takes the context to allow the database to be opened/created
    * 
    * @param ctx
    *           the Context within which to work
    */
   public RecentItemDBAdapter(Context ctx)
   {
      this.mCtx = ctx;
   }

   /**
    * Open the database. If it cannot be opened, try to create a new instance of
    * the database. If it cannot be created, throw an exception to signal the
    * failure
    * 
    * @return this (self reference, allowing this to be chained in an
    *         initialization call)
    * @throws SQLException
    *            if the database could be neither opened or created
    */
   public RecentItemDBAdapter open() throws SQLException
   {
      Log.i(TAG, "In DatabaseHelper.open");
      mDbHelper = new DatabaseHelper(mCtx);
      mDb = mDbHelper.getWritableDatabase();
      return this;
   }

   public void close()
   {
      Log.i(TAG, "In DatabaseHelper.close");
      mDbHelper.close();
   }

   /**
    * Add a grocery item to the database
    */
   public long addGroceryItem(GroceryItem item)
   {
      Log.i(TAG, "In DatabaseHelper.saveGroceryItem");
      ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_ITEMNAME, item.getItemName());
      initialValues.put(KEY_AMOUNT, item.getAmount());
      initialValues.put(KEY_STORE, item.getStore());
      initialValues.put(KEY_CATEGORY, item.getCategory());
      return mDb.insert(RECENT_TABLE, null, initialValues);
   }

   /**
    * Delete the event with the given rowId
    * 
    * @param rowId
    *           id of note to delete
    * @return true if deleted, false otherwise
    */
   public boolean deleteGroceryItem(GroceryItem item)
   {
      return mDb.delete(RECENT_TABLE, KEY_ID + "=" + item.getId(), null) > 0;
   }

   public void deleteAll()
   {
      mDb.execSQL("drop table " + RECENT_TABLE + ";");
      mDb.execSQL(RECENT_CREATE);
   }

   /**
    * Return an ArrayList of all notes in the database
    * 
    * @return ArrayList<Note> All Notes in the database
    */
   public ArrayList<GroceryItem> getRecentItems()
   {
      Cursor cursor = mDb.rawQuery("SELECT * FROM " + RECENT_TABLE, null);
      return makeListFromCursor(cursor);
   }

   private ArrayList<GroceryItem> makeListFromCursor(Cursor cursor)
   {
      int itemname = cursor.getColumnIndexOrThrow(KEY_ITEMNAME);
      int amount = cursor.getColumnIndexOrThrow(KEY_AMOUNT);
      int store = cursor.getColumnIndexOrThrow(KEY_STORE);
      int group = cursor.getColumnIndexOrThrow(KEY_CATEGORY);

      ArrayList<GroceryItem> list = new ArrayList<GroceryItem>();
      if (cursor.moveToFirst())
      {
         do
         {
            list.add(new GroceryItem(cursor.getString(itemname), cursor.getString(amount), cursor
                  .getString(store), cursor.getString(group)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return list;
   }

   public Cursor getGroceryCursor()
   {
      return mDb.rawQuery("SELECT * FROM " + RECENT_TABLE, null);
   }
}
