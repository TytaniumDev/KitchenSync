package com.tyhollan.kitchensync.model;

public class GroceryItem
{
   private long    id;
   private String itemName;
   private String amount;
   private String store;
   private String category;
   //Used to store Google Docs row location
   private String rowIndex;

   //Constructor used in the DBAdapter
   public GroceryItem(long id, String itemName, String amount, String store, String category, String rowIndex)
   {
      this.id = id;
      this.itemName = (itemName == null) ? "" : itemName;
      this.amount = (amount == null) ? "" : amount;
      this.store = (store == null) ? "" : store;
      this.category = (category == null) ? "" : category;
      this.rowIndex = (rowIndex == null) ? "" : rowIndex;
   }
   
   public GroceryItem(String itemName, String amount, String store, String category)
   {
      this.id = -1;
      this.itemName = (itemName == null) ? "" : itemName;
      this.amount = (amount == null) ? "" : amount;
      this.store = (store == null) ? "" : store;
      this.category = (category == null) ? "" : category;
      this.rowIndex = "";
   }
   
   public GroceryItem()
   {
      this.id = -1;
      this.itemName = "";
      this.amount = "";
      this.store = "";
      this.category = "";
      this.rowIndex = "";
   }

   /**
    * @return the id
    */
   public long getId()
   {
      return id;
   }

   /**
    * @param id the id to set
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * @return the itemName
    */
   public String getItemName()
   {
      return itemName;
   }

   /**
    * @param itemName
    *           the itemName to set
    */
   public void setItemName(String itemName)
   {
      this.itemName = itemName;
   }

   /**
    * @return the amount
    */
   public String getAmount()
   {
      return amount;
   }

   /**
    * @param amount
    *           the amount to set
    */
   public void setAmount(String amount)
   {
      this.amount = amount;
   }

   /**
    * @return the store
    */
   public String getStore()
   {
      return store;
   }

   /**
    * @param store
    *           the store to set
    */
   public void setStore(String store)
   {
      this.store = store;
   }

   /**
    * @return the group
    */
   public String getCategory()
   {
      return category;
   }

   /**
    * @param group
    *           the group to set
    */
   public void setCategory(String category)
   {
      this.category = category;
   }

   /**
    * @return the rowIndex
    */
   public String getRowIndex()
   {
      return rowIndex;
   }

   /**
    * @param rowIndex the rowIndex to set
    */
   public void setRowIndex(String rowIndex)
   {
      this.rowIndex = rowIndex;
   }

   @Override
   public String toString()
   {
      return itemName + " " + amount + " " + store + " " + category;
   }
   
   @Override
   public boolean equals(Object o)
   {
      if(o == null)
      {
         return false;
      }
      if(o == this)
      {
         return true;
      }
      if(o.getClass() != getClass())
      {
         return false;
      }
      if(((GroceryItem)o).getItemName().equals(this.itemName))
      {
         return true;
      }
      return false;
   }
   
   public boolean fullEquals(GroceryItem item)
   {
      if(!this.itemName.equals(item.getItemName()))
      {
         return false;
      }
      else if(!this.amount.equals(item.getAmount()))
      {
         return false;
      }
      else if(!this.store.equals(item.getStore()))
      {
         return false;
      }
      else if(!this.category.equals(item.getCategory()))
      {
         return false;
      }
      else if(!this.rowIndex.equals(item.getRowIndex()))
      {
         return false;
      }
      return true;
   }
}
