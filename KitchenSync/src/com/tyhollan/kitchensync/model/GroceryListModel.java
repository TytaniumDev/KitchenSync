package com.tyhollan.kitchensync.model;

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
   private ArrayList<GroceryItem>    crossedOffList;
   private GoogleDocsAdapter         gdocAdapter;
   private DBAdapter                 dbAdapter;
   private boolean                   currentlySyncing = false;
   private ArrayAdapter<GroceryItem> mGroceryListAdapter;
   private Activity                  mGroceryListActivity;
   private ArrayAdapter<GroceryItem> mCrossedOffListAdapter;
   private Activity                  mCrossedOffListActivity;

   public GroceryListModel(Context context)
   {
      groceryList = new ArrayList<GroceryItem>();
      crossedOffList = new ArrayList<GroceryItem>();
      dbAdapter = new DBAdapter(context);
      initialDataPull();
   }

   private void initialDataPull()
   {
      dbAdapter.open();
      groceryList = dbAdapter.getGroceryList();
      crossedOffList = dbAdapter.getCrossedOffList();
      dbAdapter.close();
   }

   /**
    * @return the groceryList
    */
   public ArrayList<GroceryItem> getGroceryListArray()
   {
      return groceryList;
   }

   /**
    * @return the crossed off list
    */
   public ArrayList<GroceryItem> getCrossedOffListArray()
   {
      return crossedOffList;
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
      else if (crossedOffList.contains(item))
      {
         item.setCrossedOff(false);
         groceryList.add(item);
         crossedOffList.remove(item);
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
      dbAdapter.open();
      dbAdapter.saveGroceryItem(item);
      dbAdapter.close();
      updateGroceryListView();
   }

   public void crossOffGroceryitem(GroceryItem item)
   {
      groceryList.remove(item);
      item.setCrossedOff(true);
      crossedOffList.add(item);
      dbAdapter.open();
      dbAdapter.saveGroceryItem(item);
      dbAdapter.close();
      updateGroceryListView();
      updateCrossedOffListView();
   }

   public void deleteGroceryItem(GroceryItem item)
   {
      Log.i(tag, "Deleting " + item.getItemName());
      crossedOffList.remove(item);
      dbAdapter.open();
      dbAdapter.deleteGroceryItem(item);
      dbAdapter.close();
      if (isGDocSyncEnabled() && gdocAdapter != null)
      {
         gdocAdapter.deleteGroceryItem(item);
      }
      updateCrossedOffListView();
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
         ArrayList<GroceryItem> tempGDocsList = gdocAdapter.getGroceryList();
         Log.i(tag, "Got new grocerylist from gdocs");
         // If in DB, overwrite it, if not delete from DB
         Log.i(tag, "Syncing grocery items with database");
         ArrayList<GroceryItem> tempSQLList = dbAdapter.getFullList();
         //Remove items that aren't in GDocs
         for(GroceryItem item: tempSQLList)
         {
            Log.i(tag, "in removing from SQL");
            if(!tempGDocsList.contains(item))
            {
               //GDocs doesn't have item, delete from DB
               dbAdapter.deleteGroceryItem(item);
            }
         }
         //Add items from GDocs
         for (GroceryItem item : tempGDocsList)
         {
            Log.i(tag, "in saving to SQL");
            if(tempSQLList.contains(item))
            {
               Log.i(tag, "Does contain");
               //Already has item, check to see if fields are the same
               GroceryItem dbItem = tempSQLList.get(tempSQLList.indexOf(item));
               if(!dbItem.fullEquals(item))
               {
                  Log.i(tag, item.getItemName() + " does not full equal");
                  //Fields aren't the same, reset crossed-off-ness
                  item.setCrossedOff(false);
                  dbAdapter.saveGroceryItem(item);
               }
               //Otherwise do nothing, all fields are the same
            }
            else
            {
               Log.i(tag, "Does not contain");
               Log.i(tag, item.getItemName() + " not contained in SQL");
               //Doesn't have item, save to DB
               dbAdapter.saveGroceryItem(item);
            }
         }
         Log.i(tag, "All items saved");
         // Recreate GroceryList with new DB data
         groceryList = dbAdapter.getGroceryList();
         crossedOffList = dbAdapter.getCrossedOffList();
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
         updateCrossedOffListView();
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

   private void updateCrossedOffListView()
   {
      mCrossedOffListActivity.runOnUiThread(new Runnable()
      {
         public void run()
         {
            mCrossedOffListAdapter.clear();
            for (GroceryItem item : crossedOffList)
            {
               mCrossedOffListAdapter.add(item);
            }
            mCrossedOffListAdapter.notifyDataSetChanged();
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

   public void setCrossedOffListAdapter(ArrayAdapter<GroceryItem> crossedOffListAdapter)
   {
      this.mCrossedOffListAdapter = crossedOffListAdapter;
   }

   public void setCrossedOffListActivity(Activity activity)
   {
      this.mCrossedOffListActivity = activity;
   }

   private boolean isGDocSyncEnabled()
   {
      // TODO: Actually make this work
      return true;
   }
}
