package com.tyhollan.grocerylist.grocery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tyhollan.grocerylist.R;

public class GroceryItemsFragment extends Fragment
{
   /**
    * Create a new instance of DetailsFragment, initialized to show the text at
    * 'index'.
    */
   public static GroceryItemsFragment newInstance(int index)
   {
      GroceryItemsFragment f = new GroceryItemsFragment();

      // Supply index input as an argument.
      Bundle args = new Bundle();
      args.putInt("index", index);
      f.setArguments(args);

      return f;
   }

   public int getShownIndex()
   {
      return getArguments().getInt("index", 0);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState)
   {
      if (container == null)
      {
         // Currently in a layout without a container, so no
         // reason to create our view.
         return null;
      }
      return inflater.inflate(R.layout.fragment_groceryitems, container);
   }
}
