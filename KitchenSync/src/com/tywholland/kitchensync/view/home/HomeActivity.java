package com.tywholland.kitchensync.view.home;

import other.com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import roboguice.inject.ContentView;
import android.os.Bundle;

import com.tywholland.kitchensync.R;

@ContentView(R.layout.activity_home)
public class HomeActivity extends RoboSherlockFragmentActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
   }
}
