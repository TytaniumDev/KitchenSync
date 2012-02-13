package com.tyhollan.grocerylist.view.grocery;

import android.os.Bundle;

import com.tyhollan.grocerylist.R;
import com.tyhollan.grocerylist.view.BaseFragmentActivity;

public class GroceryActivity extends BaseFragmentActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_grocerylist);
   }
}
