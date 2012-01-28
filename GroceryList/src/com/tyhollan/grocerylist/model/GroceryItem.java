package com.tyhollan.grocerylist.model;

public class GroceryItem
{
   String  itemName;
   Integer amount;
   String  store;
   String  group;   // Change to an enum?

   public GroceryItem(String itemName, Integer amount, String store,
         String group)
   {
      this.itemName = (itemName == null) ? "" : itemName;
      this.amount = (amount == null) ? 0 : amount;
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
   public Integer getAmount()
   {
      return amount;
   }

   /**
    * @param amount
    *           the amount to set
    */
   public void setAmount(Integer amount)
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
}
