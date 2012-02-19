package com.tyhollan.kitchensync.view;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.actionbarsherlock.view.MenuItem;
import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.home.HomeActivity;


public abstract class BaseFragmentActivity extends AnalyticsActivity
{
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId()) {
         case android.R.id.home:
             // app icon in action bar clicked; go home
             Intent intent = new Intent(this, HomeActivity.class);
             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
             startActivity(intent);
             overridePendingTransition(R.anim.home_enter, R.anim.home_exit);
             return true;
         default:
             return super.onOptionsItemSelected(item);
     }
   }
}
