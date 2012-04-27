
package com.hollanddev.kitchensync.view.home;

import android.os.Bundle;

import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.view.KitchenSyncFragmentActivity;
import com.hollanddev.kitchensync.R;

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
