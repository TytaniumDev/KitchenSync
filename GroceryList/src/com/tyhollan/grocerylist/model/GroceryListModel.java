package com.tyhollan.grocerylist.model;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * The Model for the GroceryList part of the app. Synchronizes all data
 * addition, editing, and deletion.
 * 
 * @author Tyler Holland
 * 
 */
public class GroceryListModel
{
   private static final String       tag              = "GroceryList";
   private ArrayList<GroceryItem>    groceryList;
   private GoogleDocsAdapter         gdocAdapter;
   private DBAdapter                 dbAdapter;
   private boolean                   currentlySyncing = false;
   private ArrayAdapter<GroceryItem> mGroceryListAdapter;
   private Activity mGroceryListActivity;

   public GroceryListModel(Context context)
   {
      groceryList = new ArrayList<GroceryItem>();
      dbAdapter = new DBAdapter(context);
      initialDataPull();
   }

   private void initialDataPull()
   {
      dbAdapter.open();
      groceryList = dbAdapter.getGroceryList();
      dbAdapter.close();
   }

   /**
    * @return the groceryList
    */
   public ArrayList<GroceryItem> getGroceryListArray()
   {
      return groceryList;
   }

   public void saveGroceryItem(GroceryItem item)
   {
      Log.i(tag, "Saving " + item.getItemName());
      if (groceryList.contains(item))
      {
         this.groceryList.set(groceryList.indexOf(item), item);
         if (isGDocSyncEnabled() && gdocAdapter != null)
         {
            gdocAdapter.editGroceryItem(item);
         }
      }
      else
      {
         this.groceryList.add(item);
         if (isGDocSyncEnabled() && gdocAdapter != null)
         {
            gdocAdapter.addGroceryItem(item);
         }
      }
      dbAdapter.open();
      dbAdapter.saveGroceryItem(item);
      dbAdapter.close();
   }

   public void deleteGroceryItem(GroceryItem item)
   {
      Log.i(tag, "Deleting " + item.getItemName());
      this.groceryList.remove(item);
      dbAdapter.open();
      dbAdapter.deleteGroceryItem(item);
      dbAdapter.close();
      if (isGDocSyncEnabled() && gdocAdapter != null)
      {
         gdocAdapter.deleteGroceryItem(item);
      }
      updateGroceryListView();
   }

   public void syncGroceryListData(Activity activity)
   {
      if (!currentlySyncing)
      {
         currentlySyncing = true;
         new DataSyncTask().execute(activity);
      }
   }

   private class DataSyncTask extends AsyncTask<Activity, Void, Void>
   {
      Activity activity;

      @Override
      protected Void doInBackground(Activity... params)
      {
         dbAdapter.open();
         activity = params[0];
         AndroidAuthenticator auth = new AndroidAuthenticator(activity);
         Log.i(tag, "Got auth");
         if (gdocAdapter == null)
         {
            gdocAdapter = new GoogleDocsAdapter(auth);
         }
         Log.i(tag, "Got gdoc adapter");
         // Check to see if we get data from GDocs + have internet
         // TODO
         // Pull data from GDocs into DB
         ArrayList<GroceryItem> tempList = gdocAdapter.getGroceryList();
         Log.i(tag, "Got new grocerylist from gdocs");
         // Delete DB
         dbAdapter.deleteAll();
         Log.i(tag, "deleted db");
         // Save data into empty database
         for (GroceryItem item : tempList)
         {
            Log.i(tag, "Saving a grocery item");
            dbAdapter.saveGroceryItem(item);
         }
         Log.i(tag, "All items saved");
         // Recreate GroceryList with new DB data
         groceryList = dbAdapter.getGroceryList();
         Log.i(tag, "Grocery list done updating");
         dbAdapter.close();
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
            for(GroceryItem item : groceryList)
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
