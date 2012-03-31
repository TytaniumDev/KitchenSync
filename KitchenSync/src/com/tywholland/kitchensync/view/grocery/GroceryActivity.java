package com.tywholland.kitchensync.view.grocery;

import other.com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.MenuItem;
import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.view.home.HomeActivity;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

@ContentView(R.layout.activity_grocery)
public class GroceryActivity extends RoboSherlockFragmentActivity
{
   @InjectView(R.id.grocery_viewpager)
   ViewPager          pager;
   @InjectView(R.id.grocery_titles)
   TitlePageIndicator indicator;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      getSupportActionBar().setHomeButtonEnabled(true);
      getSupportActionBar().setIcon(R.drawable.grocery_list_icon);
      GroceryViewPagerAdapter adapter = new GroceryViewPagerAdapter(getSupportFragmentManager(),
            getApplicationContext());
      pager.setAdapter(adapter);
      indicator.setViewPager(pager);
      pager.setCurrentItem(1);
      
      //Set ViewPagerIndicator theme
      final float density = getResources().getDisplayMetrics().density;
      indicator.setFooterLineHeight(1 * density); //1dp
      indicator.setFooterIndicatorHeight(3 * density); //3dp
      indicator.setFooterIndicatorStyle(IndicatorStyle.Underline);
      indicator.setTextColor(0xAA000000);
      indicator.setSelectedColor(0xFF000000);
      indicator.setSelectedBold(true);
      indicator.setClipPadding(50 * density);
   }

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
}
