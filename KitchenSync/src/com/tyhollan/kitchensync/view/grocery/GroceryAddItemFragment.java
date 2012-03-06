package com.tyhollan.kitchensync.view.grocery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.model.GroceryItem;
import com.tyhollan.kitchensync.model.GroceryListModel;
import com.tyhollan.kitchensync.model.KitchenSyncApplication;
import com.tyhollan.kitchensync.view.AnalyticsFragment;

public class GroceryAddItemFragment extends AnalyticsFragment
{
   private Button           mAddToListButton;
   private EditText         mItemName;
   private EditText         mAmount;
   private EditText         mStore;
   private EditText         mCategory;
   private GroceryListModel mGroceryListModel;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_add_groceryitem, container, false);

      mGroceryListModel = ((KitchenSyncApplication) getActivity().getApplicationContext()).getGroceryListModel();
      mAddToListButton = (Button) root.findViewById(R.id.grocery_add_item_add_button);
      mItemName = (EditText) root.findViewById(R.id.grocery_add_item_itemname_field);
      mAmount = (EditText) root.findViewById(R.id.grocery_add_item_amount_field);
      mStore = (EditText) root.findViewById(R.id.grocery_add_item_store_field);
      mCategory = (EditText) root.findViewById(R.id.grocery_add_item_category_field);
      
      
      mAddToListButton.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            mGroceryListModel.saveGroceryItem(new GroceryItem(mItemName.getText().toString(), mAmount.getText()
                  .toString(), mStore.getText().toString(), mCategory.getText().toString()));
            mItemName.setText("");
            mAmount.setText("");
            mStore.setText("");
            mCategory.setText("");
         }
      });

      return root;
   }
}
