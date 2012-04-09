
package com.tywholland.kitchensync.view.home;

import android.os.Bundle;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.KitchenSyncApplication;

import other.com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_home)
public class HomeActivity extends RoboSherlockFragmentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ((KitchenSyncApplication) getApplication()).promptForAuth(this);
    }
}
