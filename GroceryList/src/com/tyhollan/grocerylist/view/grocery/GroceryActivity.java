package com.tyhollan.grocerylist.view.grocery;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;
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

      GroceryPagerAdapter adapter = new GroceryPagerAdapter(this);
      if(findViewById(R.id.viewpager) != null)
      {
         ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
         TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
         pager.setAdapter(adapter);
         indicator.setViewPager(pager);
      }
   }
}
