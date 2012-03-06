package com.tyhollan.kitchensync.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DBAdapter
{

   // Event fields
   public static final String  KEY_ID           = BaseColumns._ID;
   public static final String  KEY_ITEMNAME     = "itemname";
   public static final String  KEY_AMOUNT       = "amount";
   public static final String  KEY_STORE        = "store";
   public static final String  KEY_CATEGORY     = "category";
   public static final String  KEY_ROWINDEX     = "rowindex";
   public static final String  KEY_TIMESTAMP    = "timestamp";
   public static final String  KEY_FREQUENCY    = "frequency";

   private static final String TAG              = "DBAdapter";
   private DatabaseHelper      mDbHelper;
   private SQLiteDatabase      mDb;

   /**
    * Database creation sql statement
    */
   private static final String DATABASE_NAME    = "data";
   private static final String GROCERY_TABLE    = "grocerylist";
   private static final String GROCERY_CREATE   = "create table " + GROCERY_TABLE + " (" + KEY_ID
                                                      + " integer primary key " + "autoincrement, " + KEY_ITEMNAME
                                                      + " text not null, " + KEY_AMOUNT + " text not null, "
                                                      + KEY_STORE + " text not null, " + KEY_CATEGORY
                                                      + " text not null, " + KEY_ROWINDEX + " text not null, "
                                                      + "UNIQUE " + " (" + KEY_ITEMNAME + " )" + ");";
   private static final String RECENT_TABLE     = "recentitems";
   private static final String RECENT_CREATE    = "create table " + RECENT_TABLE + " (" + KEY_ID
                                                      + " integer primary key " + "autoincrement, " + KEY_ITEMNAME
                                                      + " text not null, " + KEY_AMOUNT + " text not null, "
                                                      + KEY_STORE + " text not null, " + KEY_CATEGORY
                                                      + " text not null, " + KEY_FREQUENCY + " integer not null, "
                                                      + KEY_TIMESTAMP + " integer not null, " + "UNIQUE " + " ("
                                                      + KEY_ITEMNAME + " )" + ");";

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
         db.execSQL(GROCERY_CREATE);
         db.execSQL(RECENT_CREATE);
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {
         // TODO: Make this alter tables instead of drop everything
         Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
               + ", which will destroy all data");
         Log.i(TAG, "DB version is: " + db.getVersion());
         db.execSQL("drop table " + GROCERY_TABLE + ";");
         db.execSQL("drop table " + RECENT_TABLE + ";");
         db.execSQL(GROCERY_CREATE);
         db.execSQL(RECENT_CREATE);
      }
   }

   /**
    * Constructor - takes the context to allow the database to be opened/created
    * 
    * @param ctx
    *           the Context within which to work
    */
   public DBAdapter(Context ctx)
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
   public DBAdapter open() throws SQLException
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

   // GROCERY_TABLE things
   /**
    * Add a grocery item to the database
    */
   public long saveGroceryItem(GroceryItem item)
   {
      Log.i(TAG, "In DatabaseHelper.saveGroceryItem");
      ContentValues initialValues = makeValuesFromGroceryItem(item);
      if (item.getRowIndex() != null)
      {
         initialValues.put(KEY_ROWINDEX, item.getRowIndex());
      }
      else
      {
         initialValues.put(KEY_ROWINDEX, "");
      }
      return mDb.replace(GROCERY_TABLE, null, initialValues);
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
      return mDb.delete(GROCERY_TABLE, KEY_ID + "=" + item.getId(), null) > 0;
   }

   /**
    * Return an ArrayList of all notes in the database
    * 
    * @return ArrayList<Note> All Notes in the database
    */
   public ArrayList<GroceryItem> getGroceryList()
   {
      Cursor cursor = mDb.rawQuery("SELECT * FROM " + GROCERY_TABLE, null);
      return makeListFromCursor(cursor);
   }

   // RECENT_TABLE things
   public void saveRecentItem(GroceryItem item)
   {
      if (doesGroceryItemExist(RECENT_TABLE, item))
      {
         // Increment frequency and update timestamp
         mDb.execSQL("UPDATE " + RECENT_TABLE + " SET " + KEY_FREQUENCY + "=" + KEY_FREQUENCY + "+1, " + KEY_TIMESTAMP
               + "=" + System.currentTimeMillis() + " WHERE " + KEY_ITEMNAME + "=?", new String[]
         { item.getItemName() });
      }
      else
      {
         // New value
         ContentValues initialValues = makeValuesFromGroceryItem(item);
         initialValues.put(KEY_FREQUENCY, 1);
         initialValues.put(KEY_TIMESTAMP, System.currentTimeMillis());
         mDb.insert(RECENT_TABLE, null, initialValues);
      }
   }

   public ArrayList<GroceryItem> getRecentItemsList()
   {
      String[] columns =
      { KEY_ITEMNAME, KEY_AMOUNT, KEY_CATEGORY, KEY_STORE };
      Cursor cursor = mDb.query(RECENT_TABLE, columns, null, null, null, null, KEY_FREQUENCY + " ASC, " + KEY_TIMESTAMP
            + " ASC");
      return makeListFromCursor(cursor);
   }

   // Helper methods
   private ContentValues makeValuesFromGroceryItem(GroceryItem item)
   {
      ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_ITEMNAME, item.getItemName());
      initialValues.put(KEY_AMOUNT, item.getAmount());
      initialValues.put(KEY_STORE, item.getStore());
      initialValues.put(KEY_CATEGORY, item.getCategory());
      return initialValues;
   }

   private ArrayList<GroceryItem> makeListFromCursor(Cursor cursor)
   {
      int id = cursor.getColumnIndexOrThrow(KEY_ID);
      int itemname = cursor.getColumnIndexOrThrow(KEY_ITEMNAME);
      int amount = cursor.getColumnIndexOrThrow(KEY_AMOUNT);
      int store = cursor.getColumnIndexOrThrow(KEY_STORE);
      int group = cursor.getColumnIndexOrThrow(KEY_CATEGORY);
      int rowindex = cursor.getColumnIndexOrThrow(KEY_ROWINDEX);

      ArrayList<GroceryItem> list = new ArrayList<GroceryItem>();
      if (cursor.moveToFirst())
      {
         do
         {
            list.add(new GroceryItem(cursor.getLong(id), cursor.getString(itemname), cursor.getString(amount), cursor
                  .getString(store), cursor.getString(group), cursor.getString(rowindex)));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return list;
   }

   private boolean doesGroceryItemExist(String tableName, GroceryItem item)
   {
      return (mDb.query(tableName, new String[]
      { KEY_ITEMNAME }, KEY_ITEMNAME + "=?", new String[]
      { item.getItemName() }, null, null, null)).getCount() > 0;

   }

}
