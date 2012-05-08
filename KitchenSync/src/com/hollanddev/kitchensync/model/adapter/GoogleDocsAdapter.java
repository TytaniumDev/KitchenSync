
package com.hollanddev.kitchensync.model.adapter;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;

import com.hollanddev.kitchensync.model.GroceryItem;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;
import com.hollanddev.kitchensync.util.AndroidAuthenticator;
import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleDocsAdapter
{
    private static final String GROCERY_LIST_DOC_NAME = "Kitchen Sync Grocery List";
    private static final String tag = "GoogleDocsAdapter";
    private static final String SKIP_ROWS_WARNING = "WARNING: Syncing stops at first blank row. Please do not leave blank rows between items.";
    private static final String[] columns =
    {
            "Item Name", "Amount", "Store", "Category", SKIP_ROWS_WARNING
    };

    private WorkSheet worksheet;
    private String spreadsheetKey;
    private boolean newDocument = false;
    private final GoogleDocsProviderWrapper mProvider;
    private final AndroidAuthenticator mAndroidAuth;
    private boolean connected = false;

    public GoogleDocsAdapter(AndroidAuthenticator auth,
            GoogleDocsProviderWrapper googleDocsProviderWrapper)
    {
        mProvider = googleDocsProviderWrapper;
        mAndroidAuth = auth;
        initGoogleDocs();
    }

    private void initGoogleDocs()
    {
        Looper.prepare();
        new InitGoogleDocsTask().execute();
    }

    private class InitGoogleDocsTask extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... arg0) {
            newDocument = false;
            SpreadSheetFactory ssf = SpreadSheetFactory.getInstance(mAndroidAuth);
            if (mAndroidAuth.getAuthToken("wise") != null)
            {
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
                if (ssList != null && ssList.size() > 0)
                {
                    SpreadSheet ss = ssList.get(0);
                    Log.i(tag, "Got ss");
                    ArrayList<WorkSheet> wsList = ss.getAllWorkSheets();
                    Log.i(tag, "Got wslist");
                    worksheet = wsList.get(0);
                    Log.i(tag, "Number of rows: " + worksheet.getRowCount());
                    Log.i(tag, "Got worksheet: " + worksheet.getTitle());
                    if (!worksheet.getTitle().equals(GROCERY_LIST_DOC_NAME))
                    {
                        newDocument = true;
                    }
                    if (newDocument)
                    {
                        Log.i(tag, "Adding new worksheet");
                        // Create worksheet
                        ss.addListWorkSheet(GROCERY_LIST_DOC_NAME, columns.length, columns);
                        // Remove old default worksheet
                        ss.deleteWorkSheet(ss.getAllWorkSheets().get(0));
                        // Log.i(tag, "Columns: " +
                        // ss.getAllWorkSheets().get(0).getColumns());
                        wsList = ss.getAllWorkSheets();
                        Log.i(tag, "Got wslist");
                        worksheet = wsList.get(0);
                        Log.i(tag, "Got worksheet: " + worksheet.getTitle());
                    }
                    // Add warning column
                    if (worksheet.getColCount() < columns.length)
                    {
                        Log.i(tag, "Adding skip rows warning to gdoc");
                        ArrayList<WorkSheetRow> olddata = worksheet.getData(false);
                        ss.addListWorkSheet(GROCERY_LIST_DOC_NAME, columns.length, columns);
                        ss.deleteWorkSheet(ss.getAllWorkSheets().get(0));
                        worksheet = ss.getAllWorkSheets().get(0);
                        for (WorkSheetRow row : olddata)
                        {
                            worksheet.addListRow(convertWorkSheetRowToRecord(row));
                        }
                    }
                    spreadsheetKey = ss.getKey();
                    Log.i(tag, "Got ss key");
                    connected = true;
                }
            }
            return null;
        }

        private HashMap<String, String> convertWorkSheetRowToRecord(WorkSheetRow row) {
            HashMap<String, String> map = new HashMap<String, String>();
            String name;
            for (WorkSheetCell cell : row.getCells())
            {
                name = cell.getName();
                if (name.equals(GroceryItems.ITEMNAME))
                {
                    map.put(GroceryItems.ITEMNAME, (cell.getValue()));
                }
                else if (name.equals(GroceryItems.AMOUNT))
                {
                    map.put(GroceryItems.AMOUNT, (cell.getValue()));
                }
                else if (name.equals(GroceryItems.STORE))
                {
                    map.put(GroceryItems.STORE, (cell.getValue()));
                }
                else if (name.equals(GroceryItems.CATEGORY))
                {
                    map.put(GroceryItems.CATEGORY, (cell.getValue()));
                }
            }
            return map;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            mProvider.syncWithGoogleDocs();
        }

    }

    public HashMap<String, GroceryItem> getGroceryListMap()
    {
        HashMap<String, GroceryItem> map = null;
        if (connected)
        {
            map = new HashMap<String, GroceryItem>();
            if (worksheet == null)
            {
                Log.e(tag, "WARNING: Worksheet is null in getGroceryListMap");
            }
            else
            {
                ArrayList<WorkSheetRow> rows = worksheet.getData(false);
                if (rows != null)
                {
                    for (WorkSheetRow row : rows)
                    {
                        ArrayList<WorkSheetCell> cells = row.getCells();
                        Log.i(tag, "got cells");
                        GroceryItem temp = makeGroceryItemFromCells(cells);
                        temp.setRowIndex(row.getRowIndex());
                        map.put(temp.getItemName(), temp);
                    }
                }
                else
                {
                    Log.e(tag, "WARNING: Worksheet rows are null in getGroceryListMap");
                }
            }
        }
        return map;
    }

    public void addGroceryItem(final ContentValues values)
    {
        if (connected)
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
            if (values != null)
            {
                return worksheet.addListRow(convertContentValuesToRecords(values));
            }
            else
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(WorkSheetRow result) {
            if (result != null && result.getRowIndex() != null) {
                super.onPostExecute(result);
                values.put(GroceryItems.ROWINDEX, result.getRowIndex());
                mProvider.update(GroceryItems.CONTENT_URI,
                        values, GroceryItems.ITEMNAME
                                + "=?", new String[] {
                            values.getAsString(GroceryItems.ITEMNAME)
                        });
            }
        }
    }

    public void editGroceryItem(final ContentValues values)
    {
        if (connected)
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
        if (connected)
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
