
package com.hollanddev.kitchensync.view;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.GroceryItem.Categories;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_edit_categories)
public class EditCategoriesActivity extends RoboSherlockFragmentActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener
{
    @InjectView(R.id.activity_edit_categories_listview)
    ListView mListView;
    @InjectView(R.id.activity_edit_categories_edittext)
    EditText mEditText;
    @InjectView(R.id.activity_edit_categories_button)
    Button mButton;
    
    private static final int CATEGORIES_LOADER = 0x03;
    private static final String[] CATEGORIES_PROJECTION =
    {
            Categories.ITEM_ID, Categories.CATEGORY
    };


    private SimpleCursorAdapter mAdapter;
    private GoogleDocsProviderWrapper mContentResolver;
    private ActionMode mMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mContentResolver = ((KitchenSyncApplication) getApplication())
                .getGoogleDocsProviderWrapper();
        mAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_multiple_choice, null, new String[] {
                Categories.CATEGORY
                }, new int[] {
                        android.R.id.text1
                }, 0);
        mListView.setAdapter(mAdapter);

        mMode = null;
        mListView.setItemsCanFocus(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(this);
        getSupportLoaderManager().initLoader(CATEGORIES_LOADER, null, this);
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add category, and clear text field
                ContentValues categoryValues = new ContentValues();
                String categoryText = mEditText.getText().toString().trim();
                if(categoryText.length() > 0)
                {
                    categoryValues.put(Categories.CATEGORY, categoryText);
                    mContentResolver.insert(Categories.CONTENT_URI, categoryValues);
                }
                mEditText.setText("");
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Notice how the ListView api is lame
        // You can use mListView.getCheckedItemIds() if the adapter
        // has stable ids, e.g you're using a CursorAdaptor
        SparseBooleanArray checked = mListView.getCheckedItemPositions();
        boolean hasCheckedElement = false;
        for (int i = 0; i < checked.size() && !hasCheckedElement; i++) {
            hasCheckedElement = checked.valueAt(i);
        }

        if (hasCheckedElement) {
            if (mMode == null) {
                mMode = startActionMode(new ModeCallback());
            }
        } else {
            if (mMode != null) {
                mMode.finish();
            }
        }
    };

    private final class ModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Create the menu from the xml file
            MenuInflater inflater = getSupportMenuInflater();
            inflater.inflate(R.menu.settings_value_edit_cab, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Here, you can checked selected items to adapt available actions
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // Destroying action mode, let's unselect all items
            for (int i = 0; i < mListView.getAdapter().getCount(); i++)
                mListView.setItemChecked(i, false);

            if (mode == mMode) {
                mMode = null;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            long[] selected = mListView.getCheckedItemIds();
            if (selected.length > 0) {
                for (long id : selected) {
                    mContentResolver.delete(Categories.CONTENT_URI, Categories.ITEM_ID + "=?",
                            new String[] {
                                String.valueOf(id)
                            });
                }
            }
            mode.finish();
            return true;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        return new CursorLoader(this, Categories.CONTENT_URI, CATEGORIES_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
