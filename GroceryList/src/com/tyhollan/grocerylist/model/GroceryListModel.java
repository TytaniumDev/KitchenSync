package com.tyhollan.grocerylist.model;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.tyhollan.grocerylist.view.grocery.GroceryListFragment;

/**
 * The Model for the GroceryList part of the app.
 * Synchronizes all data addition, editing, and deletion.
 * @author Tyler Holland
 *
 */
public class GroceryListModel
{
   private static final String tag = "GroceryList";
   private ArrayList<GroceryItem> groceryList;
   private GoogleDocsAdapter gdocAdapter;
   private DBAdapter dbAdapter;
   private boolean currentlySyncing = false;
   
   public GroceryListModel(Context context)
   {
      groceryList = new ArrayList<GroceryItem>();
      dbAdapter = new DBAdapter(context);
      dbAdapter.open();
      initialDataPull();
   }
   
   private void initialDataPull()
   {
      groceryList = dbAdapter.getGroceryList();
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
      if(groceryList.contains(item))
      {
         this.groceryList.set(groceryList.indexOf(item), item);
      }
      else
      {
         this.groceryList.add(item);
      }
      dbAdapter.saveGroceryItem(item);
      if(isGDocSyncEnabled() && gdocAdapter != null)
      {
         gdocAdapter.saveGroceryItem(item);
      }
   }
   
   public void deleteGroceryItem(GroceryItem item)
   {
      Log.i(tag, "Deleting " + item.getItemName());
      this.groceryList.remove(item);
      dbAdapter.deleteGroceryItem(item);
      if(isGDocSyncEnabled() && gdocAdapter != null)
      {
         gdocAdapter.deleteGroceryItem(item);
      }
   }
   
   public void syncGroceryListData(Activity activity)
   {
      if(!currentlySyncing)
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
         activity = params[0];
         AndroidAuthenticator auth = new AndroidAuthenticator(activity);
         Log.i(tag, "Got auth");
         if(gdocAdapter == null)
         {
            gdocAdapter = new GoogleDocsAdapter(auth);
         }
         Log.i(tag, "Got gdoc adapter");
         //Check to see if we get data from GDocs + have internet
         //TODO
         //Delete DB
         dbAdapter.deleteAll();
         Log.i(tag, "deleted db");
         //Pull data from GDocs into DB
         for(GroceryItem item : gdocAdapter.getGroceryList())
         {
            Log.i(tag, "Saving a grocery item");
            dbAdapter.saveGroceryItem(item);
         }
         Log.i(tag, "All items saved");
         //Recreate GroceryList with new DB data
         groceryList = dbAdapter.getGroceryList();
         Log.i(tag, "Grocery list done updating");
         return null;
      }
      
      @Override
      protected void onPostExecute(Void result)
      {
         Log.i(tag, "On post execute");
         super.onPostExecute(result);
         //Notify data set changed
         GroceryListFragment.updateListView();
         currentlySyncing = false;
      }
   }
   
   @Override
   public String toString()
   {
      String result = "";
      for(GroceryItem item : groceryList)
      {
         result = result.concat(item.toString()).concat("\n");
      }
      return result;
   }
   
   public void closeDBConnection()
   {
      dbAdapter.close();
   }
   
   private boolean isGDocSyncEnabled()
   {
      //TODO: Actually make this work
      return true;
   }
}
