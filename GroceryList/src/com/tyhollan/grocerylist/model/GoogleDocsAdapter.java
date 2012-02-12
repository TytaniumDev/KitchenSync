package com.tyhollan.grocerylist.model;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;
import com.pras.conn.HttpConHandler;
import com.pras.table.Record;

public class GoogleDocsAdapter
{
   private static final String GROCERY_LIST_DOC_NAME = "GroceryListAppData";
   private static final String tag                   = "SpreadsheetTestActivity";

   private WorkSheet           worksheet;
   private String              spreadsheetKey;

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
         // TODO: Create spreadsheet
      }
      SpreadSheet ss = ssList.get(0);
      Log.i(tag, "Got ss");
      ArrayList<WorkSheet> wsList = ss.getAllWorkSheets();
      Log.i(tag, "Got wslist");
      if (wsList == null || wsList.size() == 0)
      {
         Log.e(tag, "WorkSheet does not exist");
         // TODO: Create worksheet
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
         list.add(makeGroceryItemFromCells(cells));
      }

      return list;
   }

   public void saveGroceryItem(final GroceryItem item)
   {
      worksheet.addRecord(spreadsheetKey, convertGroceryItemToRecords(item));
   }

   public void deleteGroceryItem(GroceryItem item)
   {
      for(WorkSheetRow row : worksheet.getData(false))
      {
         for(WorkSheetCell cell : row.getCells())
         {
            if(cell.getName() == DBAdapter.KEY_ITEMNAME)
            {
               if(cell.getValue().equals(item.getItemName()))
               {
                  worksheet.deleteListRow(spreadsheetKey, row);
                  return;
               }
            }
            else
            {
               break;
            }
         }
      }
      worksheet.deleteListRow(spreadsheetKey, null);
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
