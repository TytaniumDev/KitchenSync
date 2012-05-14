
package com.hollanddev.kitchensync.view;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.viewpagerindicator.TabPageIndicator;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_grocery)
public class GroceryActivity extends KitchenSyncFragmentActivity
{
    @InjectView(R.id.grocery_viewpager)
    ViewPager pager;
    @InjectView(R.id.grocery_titles)
    TabPageIndicator indicator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(R.drawable.app_icon);
        GroceryViewPagerAdapter adapter = new GroceryViewPagerAdapter(getSupportFragmentManager(),
                getApplicationContext());
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        pager.setCurrentItem(1);
    }
    @Override
    protected void onResume() {
        super.onResume();
        ((KitchenSyncApplication) getApplication()).promptForAuth(this);
        
    }
}
