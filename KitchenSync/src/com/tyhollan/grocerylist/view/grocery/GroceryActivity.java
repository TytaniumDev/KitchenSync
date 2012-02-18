package com.tyhollan.grocerylist.view.grocery;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.tyhollan.grocerylist.R;
import com.tyhollan.grocerylist.view.BaseFragmentActivity;
import com.viewpagerindicator.TitlePageIndicator;


public class GroceryActivity extends BaseFragmentActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_grocerylist);
      // Set the pager with an adapter
      ViewPager pager = (ViewPager) findViewById(R.id.grocery_pager);
      pager.setAdapter(new GroceryViewPagerAdapter(getSupportFragmentManager(), getApplicationContext()));

//      // Bind the title indicator to the adapter
//      TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.titles);
//      titleIndicator.setViewPager(pager);
   }
}
