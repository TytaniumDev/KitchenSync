package com.tyhollan.kitchensync.view.grocery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.AnalyticsFragment;

public class GroceryQuickAddItemFragment extends AnalyticsFragment
{
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_quickadd_groceryitem, container, false);

      return root;
   }
}
