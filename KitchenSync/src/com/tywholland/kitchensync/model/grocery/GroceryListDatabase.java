package com.tywholland.kitchensync.model.grocery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;

public class GroceryListDatabase extends SQLiteOpenHelper
{
   private static final String DEBUG_TAG      = "GroceryListDatabase";
   private static final int    DB_VERSION     = 3;
   private static final String DB_NAME        = "grocery_list_data";

   public static final String TABLE_GROCERY  = "grocerylist";
   private static final String CREATE_TABLE_GROCERY = "create table " + TABLE_GROCERY + " (" + GroceryItems.GROCERY_ITEM_ID
                                                    + " integer primary key " + "autoincrement, " + GroceryItems.ITEMNAME
                                                    + " text not null, " + GroceryItems.AMOUNT + " text not null, " + GroceryItems.STORE
                                                    + " text not null, " + GroceryItems.CATEGORY + " text not null, "
                                                    + GroceryItems.ROWINDEX + " text not null, " + "UNIQUE " + " ("
                                                    + GroceryItems.ITEMNAME + " )" + ");";
   
   public static final String TABLE_RECENT  = "recentitems";
   private static final String CREATE_TABLE_RECENT = "create table " + TABLE_RECENT+ " (" + RecentItems.GROCERY_ITEM_ID
                                                    + " integer primary key " + "autoincrement, " + RecentItems.ITEMNAME
                                                    + " text not null, " + RecentItems.AMOUNT + " text not null, " + RecentItems.STORE
                                                    + " text not null, " + RecentItems.CATEGORY + " text not null, "
                                                    + RecentItems.FREQUENCY + " integer not null, "
                                                    + RecentItems.TIMESTAMP + " integer not null, " + "UNIQUE " + " ("
                                                    + RecentItems.ITEMNAME + " )" + ");";

   public GroceryListDatabase(Context context)
   {
      super(context, DB_NAME, null, DB_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
       db.execSQL(CREATE_TABLE_GROCERY);
       db.execSQL(CREATE_TABLE_RECENT);
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       Log.w(DEBUG_TAG, "Upgrading database. Existing contents will be lost. ["
               + oldVersion + "]->[" + newVersion + "]");
       db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY);
       db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECENT);
       onCreate(db);
   }
}
