
package com.hollanddev.kitchensync.model.grocery;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.hollanddev.kitchensync.model.adapter.SQLiteAdapter;
import com.hollanddev.kitchensync.model.providers.GroceryItemProvider;

public class GroceryItem
{
    public static final String CONTENT_VALUES = "groceryitemvalues";
    private long id;
    private String itemName;
    private String amount;
    private String store;
    private String category;
    // Used to store Google Docs row location
    private String rowIndex;

    // Constructor used in the DBAdapter
    public GroceryItem(long id, String itemName, String amount, String store, String category,
            String rowIndex)
    {
        this.id = id;
        this.itemName = (itemName == null) ? "" : itemName;
        this.amount = (amount == null) ? "" : amount;
        this.store = (store == null) ? "" : store;
        this.category = (category == null) ? "" : category;
        this.rowIndex = (rowIndex == null) ? "" : rowIndex;
    }

    public GroceryItem(String itemName, String amount, String store, String category)
    {
        this.id = -1;
        this.itemName = (itemName == null) ? "" : itemName;
        this.amount = (amount == null) ? "" : amount;
        this.store = (store == null) ? "" : store;
        this.category = (category == null) ? "" : category;
        this.rowIndex = "";
    }

    public GroceryItem()
    {
        this.id = -1;
        this.itemName = "";
        this.amount = "";
        this.store = "";
        this.category = "";
        this.rowIndex = "";
    }

    public static final class GroceryItems implements BaseColumns
    {
        // SQL Columns
        public static final String ITEM_ID = BaseColumns._ID;
        public static final String ITEMNAME = "itemname";
        public static final String AMOUNT = "amount";
        public static final String STORE = "store";
        public static final String CATEGORY = "category";
        public static final String ROWINDEX = "rowindex";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hollanddev.grocerylist";
        //Content URIs
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + GroceryItemProvider.AUTHORITY
                + "/" + SQLiteAdapter.TABLE_GROCERY);
    }

    public static final class RecentItems implements BaseColumns
    {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + GroceryItemProvider.AUTHORITY
                + "/" + SQLiteAdapter.TABLE_RECENT);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hollanddev.recentitems";
        // SQL Columns
        public static final String ITEM_ID = BaseColumns._ID;
        public static final String ITEMNAME = "itemname";
        public static final String AMOUNT = "amount";
        public static final String STORE = "store";
        public static final String CATEGORY = "category";
        public static final String FREQUENCY = "frequency";
        public static final String TIMESTAMP = "timestamp";
    }
    
    public static final class Stores implements BaseColumns
    {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + GroceryItemProvider.AUTHORITY
                + "/" + SQLiteAdapter.TABLE_STORES);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hollanddev.stores";
        // SQL Columns
        public static final String ITEM_ID = BaseColumns._ID;
        public static final String STORE = GroceryItems.STORE;
        public static final String FREQUENCY = RecentItems.FREQUENCY;
    }
    
    public static final class Categories implements BaseColumns
    {
        public static final Uri CONTENT_URI = Uri.parse("content://"
                + GroceryItemProvider.AUTHORITY
                + "/" + SQLiteAdapter.TABLE_CATEGORIES);
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hollanddev.categories";
        // SQL Columns
        public static final String ITEM_ID = BaseColumns._ID;
        public static final String CATEGORY = GroceryItems.CATEGORY;
        public static final String FREQUENCY = RecentItems.FREQUENCY;
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

    /**
     * @return the id
     */
    public long getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id)
    {
        this.id = id;
    }

    /**
     * @return the itemName
     */
    public String getItemName()
    {
        return itemName;
    }

    /**
     * @param itemName the itemName to set
     */
    public void setItemName(String itemName)
    {
        this.itemName = itemName;
    }

    /**
     * @return the amount
     */
    public String getAmount()
    {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(String amount)
    {
        this.amount = amount;
    }

    /**
     * @return the store
     */
    public String getStore()
    {
        return store;
    }

    /**
     * @param store the store to set
     */
    public void setStore(String store)
    {
        this.store = store;
    }

    /**
     * @return the group
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * @param group the group to set
     */
    public void setCategory(String category)
    {
        this.category = category;
    }

    /**
     * @return the rowIndex
     */
    public String getRowIndex()
    {
        return rowIndex;
    }

    /**
     * @param rowIndex the rowIndex to set
     */
    public void setRowIndex(String rowIndex)
    {
        this.rowIndex = rowIndex;
    }

    @Override
    public String toString()
    {
        return itemName + " " + amount + " " + store + " " + category;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
        {
            return false;
        }
        if (o == this)
        {
            return true;
        }
        if (o.getClass() != getClass())
        {
            return false;
        }
        if (((GroceryItem) o).getItemName().equals(this.itemName))
        {
            return true;
        }
        return false;
    }

    public boolean fullEquals(GroceryItem item)
    {
        if (!this.itemName.equals(item.getItemName()))
        {
            return false;
        }
        else if (!this.amount.equals(item.getAmount()))
        {
            return false;
        }
        else if (!this.store.equals(item.getStore()))
        {
            return false;
        }
        else if (!this.category.equals(item.getCategory()))
        {
            return false;
        }
        else if (!this.rowIndex.equals(item.getRowIndex()))
        {
            return false;
        }
        return true;
    }
}
