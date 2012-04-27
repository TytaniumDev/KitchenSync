package com.hollanddev.kitchensync.view;

import android.content.Intent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.hollanddev.kitchensync.R;

import other.com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;

public class KitchenSyncFragmentActivity extends RoboSherlockFragmentActivity{
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.kitchensync_default_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.settings_button:
                startActivity(new Intent(getApplicationContext(), PreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
