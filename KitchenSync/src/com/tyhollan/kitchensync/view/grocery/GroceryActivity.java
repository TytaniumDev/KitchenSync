package com.tyhollan.kitchensync.view.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.MenuItem;
import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.AnalyticsActivity;
import com.tyhollan.kitchensync.view.home.HomeActivity;

public class GroceryActivity extends AnalyticsActivity
{
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case android.R.id.home:
            // app icon in action bar clicked; go home
            transitionToHome();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }
   
   @Override
   public void onBackPressed()
   {
      transitionToHome();
      super.onBackPressed();
   }
   
   private void transitionToHome()
   {
      Intent intent = new Intent(this, HomeActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);
      overridePendingTransition(R.anim.slide_to_bottom_enter, R.anim.slide_to_bottom_exit);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      getSupportActionBar().setHomeButtonEnabled(true);
      getSupportActionBar().setIcon(R.drawable.grocery_list_icon);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_grocerylist);
      // Set the pager with an adapter
      ViewPager pager = (ViewPager) findViewById(R.id.grocery_pager);
      pager.setAdapter(new GroceryViewPagerAdapter(getSupportFragmentManager(), getApplicationContext()));
      pager.setCurrentItem(1);

      // Bind the title indicator to the adapter
      // TitlePageIndicator titleIndicator = (TitlePageIndicator)
      // findViewById(R.id.titles);
      // titleIndicator.setViewPager(pager);
   }
}
