package com.tywholland.kitchensync.model;

import android.app.Application;

import com.tywholland.kitchensync.model.grocery.GroceryListModel;

public class KitchenSyncApplication extends Application
{
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
