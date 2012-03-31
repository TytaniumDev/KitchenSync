package com.tywholland.kitchensync.model.grocery;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;

public class GroceryItemsDatabase extends SQLiteOpenHelper
{
   private static final String DEBUG_TAG      = "GroceryItemsDatabase";
   private static final int    DB_VERSION     = 1;
   private static final String DB_NAME        = "grocery_item_data";

   public static final String TABLE_GROCERY  = "grocerylist";
   private static final String CREATE_TABLE_GROCERY = "create table " + TABLE_GROCERY + " (" + GroceryItems.GROCERY_ITEM_ID
                                                    + " integer primary key " + "autoincrement, " + GroceryItems.ITEMNAME
                                                    + " text not null, " + GroceryItems.AMOUNT + " text not null, " + GroceryItems.STORE
                                                    + " text not null, " + GroceryItems.CATEGORY + " text not null, "
                                                    + GroceryItems.ROWINDEX + " text not null, " + "UNIQUE " + " ("
                                                    + GroceryItems.ITEMNAME + " )" + ");";

   private static final String DB_SCHEMA      = CREATE_TABLE_GROCERY;

   public GroceryItemsDatabase(Context context)
   {
      super(context, DB_NAME, null, DB_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
       db.execSQL(DB_SCHEMA);
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       Log.w(DEBUG_TAG, "Upgrading database. Existing contents will be lost. ["
               + oldVersion + "]->[" + newVersion + "]");
       db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROCERY);
       onCreate(db);
   }
}
