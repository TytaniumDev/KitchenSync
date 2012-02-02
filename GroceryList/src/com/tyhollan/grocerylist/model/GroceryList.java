package com.tyhollan.grocerylist.model;

import java.util.ArrayList;

public class GroceryList
{
   private ArrayList<GroceryItem> groceryList;

   public GroceryList()
   {
      groceryList = new ArrayList<GroceryItem>();
   }

   /**
    * @return the groceryList
    */
   public ArrayList<GroceryItem> getGroceryList()
   {
      return groceryList;
   }

   /**
    * @param groceryList
    *           the groceryList to set
    */
   public void setGroceryList(ArrayList<GroceryItem> groceryList)
   {
      this.groceryList = groceryList;
   }
   
   public void addGroceryItem(GroceryItem item)
   {
      this.groceryList.add(item);
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
}
