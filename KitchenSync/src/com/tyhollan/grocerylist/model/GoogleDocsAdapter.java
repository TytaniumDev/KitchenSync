package com.tyhollan.grocerylist.model;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;
import com.tyhollan.grocerylist.view.grocery.GroceryListFragment;

public class GoogleDocsAdapter
{
   private static final String   GROCERY_LIST_DOC_NAME = "GroceryListAppData";
   private static final String   tag                   = "SpreadsheetTestActivity";
   private static final String[] columns               =
                                                       { "Item Name", "Amount", "Store", "Category" };

   private WorkSheet             worksheet;
   private String                spreadsheetKey;

   public GoogleDocsAdapter(AndroidAuthenticator auth)
   {
      SpreadSheetFactory ssf = SpreadSheetFactory.getInstance(auth);
      Log.i(tag, "Got ssf");
      ArrayList<SpreadSheet> ssList = ssf.getSpreadSheet(GROCERY_LIST_DOC_NAME, true);
      Log.i(tag, "Got sslist");
      // Check for spreadsheet
      if (ssList == null || ssList.size() == 0)
      {
         Log.e(tag, "SpreadSheet " + GROCERY_LIST_DOC_NAME + " does not exist");
         // Create spreadsheet
         ssf.createSpreadSheet(GROCERY_LIST_DOC_NAME);
         ssList = ssf.getSpreadSheet(GROCERY_LIST_DOC_NAME, true);
      }
      SpreadSheet ss = ssList.get(0);
      Log.i(tag, "Got ss");
      ArrayList<WorkSheet> wsList = ss.getAllWorkSheets();
      Log.i(tag, "Got wslist");
      if (wsList == null || wsList.size() == 0)
      {
         Log.e(tag, "WorkSheet does not exist");
         // Create worksheet
         ss.addWorkSheet(GROCERY_LIST_DOC_NAME, columns);
         wsList = ss.getAllWorkSheets();
      }
      worksheet = wsList.get(0);
      Log.i(tag, "Got worksheet: " + worksheet.getTitle());
      spreadsheetKey = ss.getKey();
      Log.i(tag, "Got ss key");
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

   public void addGroceryItem(final GroceryItem item)
   {
      new AddRowTask().execute(item);
   }

   private class AddRowTask extends AsyncTask<GroceryItem, Void, Void>
   {
      @Override
      protected Void doInBackground(GroceryItem... arg0)
      {
         worksheet.addListRow(convertGroceryItemToRecords(arg0[0]));
         return null;
      }
   }

   public void editGroceryItem(final GroceryItem item)
   {
      new EditRowTask().execute(item);
   }

   private class EditRowTask extends AsyncTask<GroceryItem, Void, Void>
   {
      @Override
      protected Void doInBackground(GroceryItem... arg0)
      {
         WorkSheetRow row = new WorkSheetRow();
         row.setRowIndex(arg0[0].getRowIndex());
         worksheet.updateListRow(spreadsheetKey, row, convertGroceryItemToRecords(arg0[0]));
         return null;
      }
   }

   public void deleteGroceryItem(GroceryItem item)
   {
      new DeleteRowTask().execute(item);
   }

   private class DeleteRowTask extends AsyncTask<GroceryItem, Void, Void>
   {
      @Override
      protected Void doInBackground(GroceryItem... arg0)
      {
         WorkSheetRow row = new WorkSheetRow();
         row.setRowIndex(arg0[0].getRowIndex());
         worksheet.deleteListRow(spreadsheetKey, row);
         return null;
      }

      // @Override
      // protected void onPostExecute(Void result)
      // {
      // new Thread(new Runnable()
      // {
      // public void run()
      // {
      // GroceryListFragment.updateListView();
      // }
      // }).start();
      // }
   }

   private HashMap<String, String> convertGroceryItemToRecords(GroceryItem item)
   {
      HashMap<String, String> records = new HashMap<String, String>();
      records.put(DBAdapter.KEY_ITEMNAME, item.getItemName());
      records.put(DBAdapter.KEY_AMOUNT, item.getAmount());
      records.put(DBAdapter.KEY_STORE, item.getStore());
      records.put(DBAdapter.KEY_CATEGORY, item.getCategory());
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
         if (name.equals(DBAdapter.KEY_ITEMNAME))
         {
            item.setItemName(cell.getValue());
         }
         else if (name.equals(DBAdapter.KEY_AMOUNT))
         {
            item.setAmount(cell.getValue());
         }
         else if (name.equals(DBAdapter.KEY_STORE))
         {
            item.setStore(cell.getValue());
         }
         else if (name.equals(DBAdapter.KEY_CATEGORY))
         {
            item.setCategory(cell.getValue());
         }
      }
      return item;
   }
}
