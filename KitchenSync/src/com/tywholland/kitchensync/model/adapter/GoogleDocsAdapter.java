
package com.tywholland.kitchensync.model.adapter;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.AsyncTask;
import android.util.Log;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.providers.GroceryItemProvider;
import com.tywholland.kitchensync.util.AndroidAuthenticator;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleDocsAdapter
{
    private static final String GROCERY_LIST_DOC_NAME = "Kitchen Sync Grocery List";
    private static final String tag = "GoogleDocsAdapter";
    private static final String[] columns =
    {
            "Item Name", "Amount", "Store", "Category"
    };

    private WorkSheet worksheet;
    private String spreadsheetKey;
    private boolean newDocument = false;
    private final ContentResolver mContentResolver;
    private boolean authenticated = false;

    public GoogleDocsAdapter(final AndroidAuthenticator auth, final ContentResolver contentResolver)
    {
        mContentResolver = contentResolver;
        new Thread(new Runnable() {

            @Override
            public void run() {
                newDocument = false;
                SpreadSheetFactory ssf = SpreadSheetFactory.getInstance(auth);
                if (auth.getAuthToken("wise") != null)
                {
                    authenticated = true;
                    Log.i(tag, "Got ssf");
                    ArrayList<SpreadSheet> ssList = ssf.getSpreadSheet(GROCERY_LIST_DOC_NAME, true);
                    Log.i(tag, "Got sslist");
                    // Check for spreadsheet
                    if (ssList == null || ssList.size() == 0)
                    {
                        Log.i(tag, "SpreadSheet " + GROCERY_LIST_DOC_NAME + " does not exist");
                        // Create spreadsheet
                        ssf.createSpreadSheet(GROCERY_LIST_DOC_NAME);
                        ssList = ssf.getSpreadSheet(GROCERY_LIST_DOC_NAME, true);
                        newDocument = true;
                    }
                    SpreadSheet ss = ssList.get(0);
                    Log.i(tag, "Got ss");
                    if (newDocument)
                    {
                        Log.i(tag, "Adding new worksheet");
                        // Create worksheet
                        ss.addListWorkSheet(GROCERY_LIST_DOC_NAME, columns.length, columns);
                        // Remove old default worksheet
                        ss.deleteWorkSheet(ss.getAllWorkSheets().get(0));
                        Log.i(tag, "Columns: " + ss.getAllWorkSheets().get(0).getColumns());
                    }
                    ArrayList<WorkSheet> wsList = ss.getAllWorkSheets();
                    Log.i(tag, "Got wslist");
                    worksheet = wsList.get(0);
                    Log.i(tag, "Got worksheet: " + worksheet.getTitle());
                    spreadsheetKey = ss.getKey();
                    Log.i(tag, "Got ss key");
                    contentResolver.call(GroceryItems.CONTENT_URI,
                            GroceryItemProvider.SYNC_WITH_GOOGLE_DOCS_CALL, null, null);
                }
            }
        }).start();

    }

    public ArrayList<GroceryItem> getGroceryList()
    {
        ArrayList<GroceryItem> list = new ArrayList<GroceryItem>();
        ArrayList<WorkSheetRow> rows = worksheet.getData(false);
        for (WorkSheetRow row : rows)
        {
            ArrayList<WorkSheetCell> cells = row.getCells();
            Log.i(tag, "got cells");
            GroceryItem temp = makeGroceryItemFromCells(cells);
            temp.setRowIndex(row.getRowIndex());
            list.add(temp);
        }

        return list;
    }

    public HashMap<String, GroceryItem> getGroceryListMap()
    {
        HashMap<String, GroceryItem> map = new HashMap<String, GroceryItem>();
        ArrayList<WorkSheetRow> rows = worksheet.getData(false);
        for (WorkSheetRow row : rows)
        {
            ArrayList<WorkSheetCell> cells = row.getCells();
            Log.i(tag, "got cells");
            GroceryItem temp = makeGroceryItemFromCells(cells);
            temp.setRowIndex(row.getRowIndex());
            map.put(temp.getItemName(), temp);
        }

        return map;
    }

    public void addGroceryItem(final ContentValues values)
    {
        if (authenticated)
        {
            Log.i(tag, "Adding an item to gdocs");
            new AddRowTask().execute(values);
        }
    }

    private class AddRowTask extends AsyncTask<ContentValues, Void, WorkSheetRow>
    {
        private ContentValues values;

        @Override
        protected WorkSheetRow doInBackground(ContentValues... arg0)
        {
            values = arg0[0];
            return worksheet.addListRow(convertContentValuesToRecords(values));
        }

        @Override
        protected void onPostExecute(WorkSheetRow result) {
            super.onPostExecute(result);
            values.put(GroceryItems.ROWINDEX, result.getRowIndex());
            mContentResolver.update(GroceryItems.CONTENT_URI,
                    values, GroceryItems.ITEMNAME
                            + "=?", new String[] {
                        values.getAsString(GroceryItems.ITEMNAME)
                    });
        }
    }

    public void editGroceryItem(final ContentValues values)
    {
        if (authenticated)
        {
            new EditRowTask().execute(values);
        }
    }

    private class EditRowTask extends AsyncTask<ContentValues, Void, Void>
    {
        @Override
        protected Void doInBackground(ContentValues... arg0)
        {
            WorkSheetRow row = new WorkSheetRow();
            row.setRowIndex(arg0[0].getAsString(GroceryItems.ROWINDEX));
            worksheet.updateListRow(spreadsheetKey, row, convertContentValuesToRecords(arg0[0]));
            return null;
        }
    }

    public void deleteGroceryItem(ContentValues values)
    {
        if (authenticated)
        {
            new DeleteRowTask().execute(values);
        }
    }

    private class DeleteRowTask extends AsyncTask<ContentValues, Void, Void>
    {
        @Override
        protected Void doInBackground(ContentValues... arg0)
        {
            WorkSheetRow row = new WorkSheetRow();
            row.setRowIndex(arg0[0].getAsString(GroceryItems.ROWINDEX));
            worksheet.deleteListRow(spreadsheetKey, row);
            return null;
        }
    }

    private HashMap<String, String> convertContentValuesToRecords(ContentValues values)
    {
        HashMap<String, String> records = new HashMap<String, String>();
        records.put(GroceryItems.ITEMNAME, values.getAsString(GroceryItems.ITEMNAME));
        records.put(GroceryItems.AMOUNT, values.getAsString(GroceryItems.AMOUNT));
        records.put(GroceryItems.STORE, values.getAsString(GroceryItems.STORE));
        records.put(GroceryItems.CATEGORY, values.getAsString(GroceryItems.CATEGORY));
        return records;
    }

    private GroceryItem makeGroceryItemFromCells(ArrayList<WorkSheetCell> cells)
    {
        GroceryItem item = new GroceryItem();
        String name;
        for (WorkSheetCell cell : cells)
        {
            name = cell.getName();
            Log.i(tag, "Cell name: " + name);
            if (name.equals(GroceryItems.ITEMNAME))
            {
                item.setItemName(cell.getValue());
            }
            else if (name.equals(GroceryItems.AMOUNT))
            {
                item.setAmount(cell.getValue());
            }
            else if (name.equals(GroceryItems.STORE))
            {
                item.setStore(cell.getValue());
            }
            else if (name.equals(GroceryItems.CATEGORY))
            {
                item.setCategory(cell.getValue());
            }
        }
        return item;
    }
}
