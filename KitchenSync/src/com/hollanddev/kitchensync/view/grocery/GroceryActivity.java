
package com.hollanddev.kitchensync.view.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.view.MenuItem;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.view.KitchenSyncFragmentActivity;
import com.hollanddev.kitchensync.view.home.HomeActivity;
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
        ((KitchenSyncApplication) getApplication()).promptForAuth(this);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setIcon(R.drawable.grocery_list_icon);
        GroceryViewPagerAdapter adapter = new GroceryViewPagerAdapter(getSupportFragmentManager(),
                getApplicationContext());
        pager.setAdapter(adapter);
        indicator.setViewPager(pager);
        pager.setCurrentItem(1);
        
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
