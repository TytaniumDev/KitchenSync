package com.tyhollan.kitchensync.model;

import android.app.Application;

public class KitchenSyncApplication extends Application
{
   private static final String tag = "SpreadsheetTestActivity";
   
   private GroceryListModel mGroceryList;
   
   
   @Override
   public void onCreate()
   {
      super.onCreate();
      mGroceryList = new GroceryListModel(getApplicationContext());
   }
   
   public GroceryListModel getGroceryListModel()
   {
      return mGroceryList;
   }
}
