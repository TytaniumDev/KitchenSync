package com.tyhollan.grocerylist.model;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.provider.ContactsContract.Data;
import android.util.Log;

public class DBAdapter
{

   // Event fields
   public static final String  KEY_ID           = BaseColumns._ID;
   public static final String  KEY_ITEMNAME     = "itemname";
   public static final String  KEY_AMOUNT       = "amount";
   public static final String  KEY_STORE        = "store";
   public static final String  KEY_GROUP        = "group";

   private static final String TAG              = "DBAdapter";
   private DatabaseHelper      mDbHelper;
   private SQLiteDatabase      mDb;

   /**
    * Database creation sql statement
    */
   private static final String DATABASE_NAME    = "data";
   private static final String GROCERY_TABLE    = "grocerylist";
   private static final String GROCERY_CREATE   = "create table "
                                                      + GROCERY_TABLE + " ("
                                                      + KEY_ID
                                                      + " integer primary key "
                                                      + "autoincrement, "
                                                      + KEY_ITEMNAME
                                                      + " text not null, "
                                                      + KEY_AMOUNT
                                                      + " text not null, "
                                                      + KEY_STORE
                                                      + " text not null, "
                                                      + KEY_GROUP
                                                      + " text not null);";

   private static final int    DATABASE_VERSION = 1;

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
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
      {
         // TODO: Make this alter tables instead of drop everything
         Log.i(TAG, "Upgrading database from version " + oldVersion + " to "
               + newVersion + ", which will destroy all data");
         Log.i(TAG, "DB version is: " + db.getVersion());
         db.execSQL("drop table " + GROCERY_TABLE + ";");
         db.execSQL(GROCERY_CREATE);
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

   /**
    * Add a grocery item to the database
    */
   public long addGroceryItem(GroceryItem item)
   {
      Log.i(TAG, "In DatabaseHelper.addGroceryItem");
      ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_ITEMNAME, item.getItemName());
      initialValues.put(KEY_AMOUNT, item.getAmount());
      initialValues.put(KEY_STORE, item.getStore());
      initialValues.put(KEY_GROUP, item.getGroup());
      return mDb.insert(GROCERY_TABLE, null, initialValues);
   }

   public long editGroceryItem(GroceryItem item)
   {
      Log.i(TAG, "In DatabaseHelper.editGroceryItem");
      ContentValues initialValues = new ContentValues();
      initialValues.put(KEY_ITEMNAME, item.getItemName());
      initialValues.put(KEY_AMOUNT, item.getAmount());
      initialValues.put(KEY_STORE, item.getStore());
      initialValues.put(KEY_GROUP, item.getGroup());
      String[] whereArgs =
      { Double.toString(item.getId()) };
      return mDb.update(GROCERY_TABLE, initialValues, KEY_ID + "=?", whereArgs);
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
   public ArrayList<GroceryItem> fetchAllGroceryItems()
   {
      Cursor noteCursor = mDb.rawQuery("SELECT * FROM " + GROCERY_TABLE, null);

      int id = noteCursor.getColumnIndexOrThrow(KEY_ID);
      int itemname = noteCursor.getColumnIndexOrThrow(KEY_ITEMNAME);
      int amount = noteCursor.getColumnIndexOrThrow(KEY_AMOUNT);
      int store = noteCursor.getColumnIndexOrThrow(KEY_STORE);
      int group = noteCursor.getColumnIndexOrThrow(KEY_GROUP);

      ArrayList<GroceryItem> groceryItems = new ArrayList<GroceryItem>();
      if (noteCursor.moveToFirst())
      {
         do
         {
            groceryItems.add(new GroceryItem(noteCursor.getLong(id), noteCursor
                  .getString(itemname), noteCursor.getString(amount),
                  noteCursor.getString(store), noteCursor.getString(group)));
         } while (noteCursor.moveToNext());
      }
      noteCursor.close();
      return groceryItems;
   }
}
