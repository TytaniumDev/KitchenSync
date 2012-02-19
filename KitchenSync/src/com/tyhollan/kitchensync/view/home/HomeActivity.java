package com.tyhollan.kitchensync.view.home;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.model.KitchenSyncApplication;
import com.tyhollan.kitchensync.view.AnalyticsActivity;

public class HomeActivity extends AnalyticsActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_home);
   }
}
