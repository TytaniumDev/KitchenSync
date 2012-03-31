package com.tywholland.kitchensync.util.grocery;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.database.Cursor;

import com.tywholland.kitchensync.model.adapter.DBAdapter;
import com.tywholland.kitchensync.model.grocery.GroceryItem;

public class GroceryItemUtil
{
   private static int idColumn;
   private static int itemnameColumn;
   private static int amountColumn;
   private static int storeColumn;
   private static int groupColumn;
   private static int rowindexColumn;

   public static ContentValues makeContentValuesFromGroceryItem(GroceryItem item)
   {
      ContentValues initialValues = new ContentValues();
      initialValues.put(DBAdapter.KEY_ITEMNAME, item.getItemName());
      initialValues.put(DBAdapter.KEY_AMOUNT, item.getAmount());
      initialValues.put(DBAdapter.KEY_STORE, item.getStore());
      initialValues.put(DBAdapter.KEY_CATEGORY, item.getCategory());
      if (item.getRowIndex() != null)
      {
         initialValues.put(DBAdapter.KEY_ROWINDEX, item.getRowIndex());
      }
      else
      {
         initialValues.put(DBAdapter.KEY_ROWINDEX, "");
      }
      return initialValues;
   }

   public static ArrayList<GroceryItem> makeGroceryItemArrayListFromCursor(Cursor cursor)
   {
      idColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID);
      itemnameColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ITEMNAME);
      amountColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_AMOUNT);
      storeColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_STORE);
      groupColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_CATEGORY);
      rowindexColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWINDEX);

      ArrayList<GroceryItem> list = new ArrayList<GroceryItem>();
      if (cursor.moveToFirst())
      {
         do
         {
            list.add(makeGroceryItemFromCursor(cursor));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return list;
   }

   public static HashMap<String, GroceryItem> makeGroceryItemHashMapFromCursor(Cursor cursor)
   {
      idColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID);
      itemnameColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ITEMNAME);
      amountColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_AMOUNT);
      storeColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_STORE);
      groupColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_CATEGORY);
      rowindexColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWINDEX);

      HashMap<String, GroceryItem> map = new HashMap<String, GroceryItem>();
      if (cursor.moveToFirst())
      {
         do
         {
            map.put(cursor.getString(itemnameColumn), makeGroceryItemFromCursor(cursor));
         } while (cursor.moveToNext());
      }
      cursor.close();
      return map;
   }

   public static GroceryItem makeGroceryItemFromCursor(Cursor cursor)
   {
      return new GroceryItem(cursor.getLong(idColumn), cursor.getString(itemnameColumn),
            cursor.getString(amountColumn), cursor.getString(storeColumn), cursor.getString(groupColumn),
            cursor.getString(rowindexColumn));
   }
}
