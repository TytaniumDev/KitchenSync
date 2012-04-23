
package com.tywholland.kitchensync.view.home;

import android.os.Bundle;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.KitchenSyncApplication;
import com.tywholland.kitchensync.view.KitchenSyncFragmentActivity;

import roboguice.inject.ContentView;

@ContentView(R.layout.activity_home)
public class HomeActivity extends KitchenSyncFragmentActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ((KitchenSyncApplication) getApplication()).promptForAuth(this);
    }
}
