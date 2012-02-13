package com.tyhollan.grocerylist.model;

import android.app.Application;

public class AppNameApplication extends Application
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
