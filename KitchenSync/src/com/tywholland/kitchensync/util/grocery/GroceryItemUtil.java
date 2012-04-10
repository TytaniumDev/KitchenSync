
package com.tywholland.kitchensync.util.grocery;

import android.content.ContentValues;
import android.database.Cursor;

import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;

public class GroceryItemUtil
{
    private static int idColumn;
    private static int itemnameColumn;
    private static int amountColumn;
    private static int storeColumn;
    private static int categoryColumm;
    private static int rowindexColumn;

    public static ContentValues makeContentValuesFromGroceryItem(GroceryItem
            item)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(GroceryItems.ITEMNAME, item.getItemName());
        initialValues.put(GroceryItems.AMOUNT, item.getAmount());
        initialValues.put(GroceryItems.STORE, item.getStore());
        initialValues.put(GroceryItems.CATEGORY, item.getCategory());
        if (item.getRowIndex() != null)
        {
            initialValues.put(GroceryItems.ROWINDEX, item.getRowIndex());
        }
        else
        {
            initialValues.put(GroceryItems.ROWINDEX, "");
        }
        return initialValues;
    }

//    public static ArrayList<GroceryItem>
//            makeGroceryItemArrayListFromCursor(Cursor cursor)
//    {
//        idColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID);
//        itemnameColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ITEMNAME);
//        amountColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_AMOUNT);
//        storeColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_STORE);
//        groupColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_CATEGORY);
//        rowindexColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWINDEX);
//
//        ArrayList<GroceryItem> list = new ArrayList<GroceryItem>();
//        if (cursor.moveToFirst())
//        {
//            do
//            {
//                list.add(makeGroceryItemFromCursor(cursor));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return list;
//    }
//
//    public static HashMap<String, GroceryItem>
//            makeGroceryItemHashMapFromCursor(Cursor cursor)
//    {
//        idColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ID);
//        itemnameColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ITEMNAME);
//        amountColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_AMOUNT);
//        storeColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_STORE);
//        groupColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_CATEGORY);
//        rowindexColumn = cursor.getColumnIndexOrThrow(DBAdapter.KEY_ROWINDEX);
//
//        HashMap<String, GroceryItem> map = new HashMap<String, GroceryItem>();
//        if (cursor.moveToFirst())
//        {
//            do
//            {
//                map.put(cursor.getString(itemnameColumn),
//                        makeGroceryItemFromCursor(cursor));
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//        return map;
//    }

    public static GroceryItem makeGroceryItemFromCursor(Cursor cursor)
    {
        setColumnIndicies(cursor);
        return new GroceryItem(cursor.getLong(idColumn), cursor.getString(itemnameColumn),
                cursor.getString(amountColumn), cursor.getString(storeColumn),
                cursor.getString(categoryColumm),
                cursor.getString(rowindexColumn));
    }

    private static void setColumnIndicies(Cursor cursor)
    {
        idColumn = cursor.getColumnIndexOrThrow(GroceryItems.GROCERY_ITEM_ID);
        itemnameColumn = cursor.getColumnIndexOrThrow(GroceryItems.ITEMNAME);
        amountColumn = cursor.getColumnIndexOrThrow(GroceryItems.AMOUNT);
        storeColumn = cursor.getColumnIndexOrThrow(GroceryItems.STORE);
        categoryColumm = cursor.getColumnIndexOrThrow(GroceryItems.CATEGORY);
        rowindexColumn = cursor.getColumnIndexOrThrow(GroceryItems.ROWINDEX);
    }
}
