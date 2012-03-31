package com.tywholland.kitchensync.model.grocery;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.tywholland.kitchensync.model.adapter.DBAdapter;
import com.tywholland.kitchensync.model.adapter.GoogleDocsAdapter;
import com.tywholland.kitchensync.util.AndroidAuthenticator;

/**
 * The Model for the GroceryList part of the app. Synchronizes all data
 * addition, editing, and deletion.
 * 
 * @author Tyler Holland
 * 
 */
public class GroceryListModel
{
   private static final String       tag              = "GroceryListModel";
   private ArrayList<GroceryItem>    groceryList;
   private GoogleDocsAdapter         gdocAdapter;
   private DBAdapter                 dbAdapter;
   private boolean                   currentlySyncing = false;
   private ArrayAdapter<GroceryItem> mGroceryListAdapter;
   private Cursor                    mRecentItemsCursor;
   private Activity                  mGroceryListActivity;

   public GroceryListModel(Context context)
   {
      groceryList = new ArrayList<GroceryItem>();
      dbAdapter = new DBAdapter(context);
   }

   public void openDBConnection()
   {
      dbAdapter.open();
   }

   public void closeDBConnection()
   {
      dbAdapter.close();
   }

   public void initialDataPull()
   {
//      groceryList = dbAdapter.getGroceryList();
   }

   /**
    * @return the groceryList
    */
   public ArrayList<GroceryItem> getGroceryListArray()
   {
      return groceryList;
   }

   public Cursor getRecentItemListCursor()
   {
      mRecentItemsCursor = dbAdapter.getRecentItemsListCursor();
      return mRecentItemsCursor;
   }
   
   public void saveGroceryItem(GroceryItem item)
   {
      if (groceryList.contains(item))
      {
         groceryList.set(groceryList.indexOf(item), item);
         if (isGDocSyncEnabled() && gdocAdapter != null)
         {
            gdocAdapter.editGroceryItem(item);
         }
      }
      else
      {
         groceryList.add(item);
         if (isGDocSyncEnabled() && gdocAdapter != null)
         {
            gdocAdapter.addGroceryItem(item);
         }
      }
      dbAdapter.saveGroceryItem(item);
      updateGroceryListView();
   }

   public void deleteGroceryItem(GroceryItem item)
   {
      Log.i(tag, "Deleting " + item.getItemName());
      groceryList.remove(item);
      dbAdapter.deleteGroceryItem(item);
      dbAdapter.saveRecentItem(item);
      mRecentItemsCursor.requery();
      if (isGDocSyncEnabled() && gdocAdapter != null)
      {
         gdocAdapter.deleteGroceryItem(item);
      }
      updateGroceryListView();
   }

   public void syncGroceryListData()
   {
      if (!currentlySyncing)
      {
         currentlySyncing = true;
         new DataSyncTask().execute(mGroceryListActivity);
      }
   }

   private class DataSyncTask extends AsyncTask<Activity, Void, Void>
   {
      Activity activity;

      @Override
      protected Void doInBackground(Activity... params)
      {
//         activity = params[0];
//         AndroidAuthenticator auth = new AndroidAuthenticator(activity);
//         Log.i(tag, "Got auth");
//         if (gdocAdapter == null)
//         {
//            gdocAdapter = new GoogleDocsAdapter(auth);
//         }
//         Log.i(tag, "Got gdoc adapter");
//         // Check to see if we get data from GDocs + have internet
//         // TODO
//         // Pull data from GDocs into DB
//         HashMap<String, GroceryItem> tempGDocsMap = gdocAdapter.getGroceryListMap();
//         Log.i(tag, "Got new grocerylist from gdocs");
//         // If in DB, overwrite it, if not delete from DB
//         Log.i(tag, "Syncing grocery items with database");
//         HashMap<String, GroceryItem> tempSQLMap = dbAdapter.getGroceryListMap();
//         // Remove items that aren't in GDocs
//         for (String itemname : tempSQLMap.keySet())
//         {
//            if (!tempGDocsMap.containsKey(itemname))
//            {
//               Log.i(tag, "removing from SQL");
//               // GDocs doesn't have item, delete from DB
//               dbAdapter.deleteGroceryItem(tempSQLMap.get(itemname));
//            }
//         }
//         // Add items from GDocs
//         for (String itemname : tempGDocsMap.keySet())
//         {
//            Log.i(tag, "saving to SQL");
//            dbAdapter.saveGroceryItem(tempGDocsMap.get(itemname));
//         }
//         Log.i(tag, "All items saved");
//         // Recreate GroceryList with new DB data
//         groceryList = dbAdapter.getGroceryList();
//         Log.i(tag, "Grocery list done updating");
         return null;
      }

      @Override
      protected void onPostExecute(Void result)
      {
         Log.i(tag, "On post execute");
         super.onPostExecute(result);
         // Notify data set changed
         updateGroceryListView();
         currentlySyncing = false;
      }
   }

   private void updateGroceryListView()
   {
      mGroceryListActivity.runOnUiThread(new Runnable()
      {
         public void run()
         {
            mGroceryListAdapter.clear();
            for (GroceryItem item : groceryList)
            {
               mGroceryListAdapter.add(item);
            }
            mGroceryListAdapter.notifyDataSetChanged();
         }
      });
   }

   @Override
   public String toString()
   {
      String result = "";
      for (GroceryItem item : groceryList)
      {
         result = result.concat(item.toString()).concat("\n");
      }
      return result;
   }

   public void setGroceryListAdapter(ArrayAdapter<GroceryItem> groceryListAdapter)
   {
      this.mGroceryListAdapter = groceryListAdapter;
   }

   public void setGroceryListActivity(Activity activity)
   {
      this.mGroceryListActivity = activity;
   }

   private boolean isGDocSyncEnabled()
   {
      // TODO: Actually make this work
      return true;
   }
}
