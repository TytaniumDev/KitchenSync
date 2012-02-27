package com.tyhollan.kitchensync.view.recipe;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.MenuItem;
import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.AnalyticsActivity;
import com.tyhollan.kitchensync.view.home.HomeActivity;

public class RecipeActivity extends AnalyticsActivity
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
      overridePendingTransition(R.anim.slide_to_top_enter, R.anim.slide_to_top_exit);
   }

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      getActionBar().setHomeButtonEnabled(true);
      super.onCreate(savedInstanceState);
   }
}
