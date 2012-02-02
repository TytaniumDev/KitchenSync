package com.tyhollan.grocerylist.model;

public class GroceryItem
{
   String  itemName;
   String amount;
   String  store;
   String  group;   // Change to an enum?

   public GroceryItem(String itemName, String amount, String store,
         String group)
   {
      this.itemName = (itemName == null) ? "" : itemName;
      this.amount = (amount == null) ? "" : amount;
      this.store = (store == null) ? "" : store;
      this.group = (group == null) ? "" : group;
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
   public String getGroup()
   {
      return group;
   }

   /**
    * @param group
    *           the group to set
    */
   public void setGroup(String group)
   {
      this.group = group;
   }
   
   @Override
   public String toString()
   {
      return itemName + " " + amount + " " + store + " " + group;
   }
}
