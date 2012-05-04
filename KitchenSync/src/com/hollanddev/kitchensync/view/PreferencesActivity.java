package com.hollanddev.kitchensync.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.hollanddev.kitchensync.R;

public class PreferencesActivity extends SherlockPreferenceActivity{
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.app_icon);
        addPreferencesFromResource(R.xml.settings);
        Preference editStoresPref = findPreference(getString(R.string.key_edit_stores));
        editStoresPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, EditStoresActivity.class);
                PreferencesActivity.this.startActivity(intent);
                return true;
            }
        });
        Preference editCategoriesPref = findPreference(getString(R.string.key_edit_categories));
        editCategoriesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(PreferencesActivity.this, EditCategoriesActivity.class);
                PreferencesActivity.this.startActivity(intent);
                return true;
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
