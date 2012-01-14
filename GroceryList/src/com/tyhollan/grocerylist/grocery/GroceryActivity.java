package com.tyhollan.grocerylist.grocery;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.tyhollan.grocerylist.R;
import com.viewpagerindicator.TitlePageIndicator;

public class GroceryActivity extends FragmentActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_grocerylist);

      GroceryPagerAdapter adapter = new GroceryPagerAdapter(this);
      ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
      TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
      pager.setAdapter(adapter);
      indicator.setViewPager(pager);
   }
}
