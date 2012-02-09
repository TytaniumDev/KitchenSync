package com.tyhollan.grocerylist.model;

public class GroceryItem
{
   private long    id;
   private String itemName;
   private String amount;
   private String store;
   private String category;

   public GroceryItem(long id, String itemName, String amount, String store, String category)
   {
      this.id = id;
      this.itemName = (itemName == null) ? "" : itemName;
      this.amount = (amount == null) ? "" : amount;
      this.store = (store == null) ? "" : store;
      this.category = (category == null) ? "" : category;
   }
   
   public GroceryItem(String itemName, String amount, String store, String category)
   {
      this.id = -1;
      this.itemName = (itemName == null) ? "" : itemName;
      this.amount = (amount == null) ? "" : amount;
      this.store = (store == null) ? "" : store;
      this.category = (category == null) ? "" : category;
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

   @Override
   public String toString()
   {
      return itemName + " " + amount + " " + store + " " + category;
   }
}
