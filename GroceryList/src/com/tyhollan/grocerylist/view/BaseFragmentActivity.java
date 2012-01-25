package com.tyhollan.grocerylist.view;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;

import com.tyhollan.grocerylist.R;
import com.tyhollan.grocerylist.view.home.HomeActivity;

public abstract class BaseFragmentActivity extends FragmentActivity
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
