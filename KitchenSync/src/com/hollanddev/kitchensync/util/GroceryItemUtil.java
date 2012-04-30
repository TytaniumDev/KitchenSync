
package com.hollanddev.kitchensync.util;

import android.content.ContentValues;
import android.database.Cursor;

import com.hollanddev.kitchensync.model.GroceryItem;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;

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

    public static GroceryItem makeGroceryItemFromCursor(Cursor cursor)
    {
        setColumnIndicies(cursor);
        return new GroceryItem(cursor.getLong(idColumn), cursor.getString(itemnameColumn),
                cursor.getString(amountColumn), cursor.getString(storeColumn),
                cursor.getString(categoryColumm),
                cursor.getString(rowindexColumn));
    }
    
    public static ContentValues makeGenericContentValuesFromCursor(Cursor cursor)
    {
        ContentValues values = new ContentValues();
        values.put(GroceryItems.ITEMNAME,
                cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.ITEMNAME)));
        values.put(GroceryItems.AMOUNT,
                cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.AMOUNT)));
        values.put(GroceryItems.STORE,
                cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.STORE)));
        values.put(GroceryItems.CATEGORY,
                cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.CATEGORY)));
        return values;
    }
    
    public static ContentValues makeFullContentValuesFromCursor(Cursor cursor)
    {
        ContentValues values = makeGenericContentValuesFromCursor(cursor);
        values.put(GroceryItems.ITEM_ID,
                cursor.getString(cursor.getColumnIndex(GroceryItems.ITEM_ID)));
        values.put(GroceryItems.ROWINDEX,
                cursor.getString(cursor.getColumnIndex(GroceryItems.ROWINDEX)));
        return values;
    }

    private static void setColumnIndicies(Cursor cursor)
    {
        idColumn = cursor.getColumnIndexOrThrow(GroceryItems.ITEM_ID);
        itemnameColumn = cursor.getColumnIndexOrThrow(GroceryItems.ITEMNAME);
        amountColumn = cursor.getColumnIndexOrThrow(GroceryItems.AMOUNT);
        storeColumn = cursor.getColumnIndexOrThrow(GroceryItems.STORE);
        categoryColumm = cursor.getColumnIndexOrThrow(GroceryItems.CATEGORY);
        rowindexColumn = cursor.getColumnIndexOrThrow(GroceryItems.ROWINDEX);
    }
}
