
package com.tywholland.kitchensync.view.grocery;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.tywholland.kitchensync.model.providers.GroceryItemProvider;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;

public class GroceryListFragment extends RoboSherlockListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROCERY_LIST_LOADER = 0x01;
    private static final String ALL_STORES = "All Stores"; 

    private SimpleCursorAdapter mAdapter;
    private String mCurFilter;
    private final String[] mGroceryItemProjection =
    {
            GroceryItems.GROCERY_ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT,
            GroceryItems.STORE,
            GroceryItems.CATEGORY, GroceryItems.ROWINDEX
    };
    private ArrayAdapter<String> mFilterAdapter;
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // TODO: Change this to load last filter from sharedprefs
        mCurFilter = ALL_STORES;
        String[] uiBindFrom =
        {
                GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE,
                GroceryItems.ROWINDEX
        };
        int[] uiBindTo =
        {
                R.id.grocery_row_item_name, R.id.grocery_row_amount, R.id.grocery_row_store,
                R.id.grocery_row_syncing_icon
        };

        getActivity().getSupportLoaderManager().initLoader(GROCERY_LIST_LOADER, null, this);

        mAdapter = new SimpleCursorAdapter(getActivity().getApplicationContext(),
                R.layout.grocery_list_row, null,
                uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mAdapter.setViewBinder(new GroceryListViewBinder());
        setListAdapter(mAdapter);
        setListShown(false);
        getLoaderManager().initLoader(0, null, this);

        // Spinner stuff for filtering by store
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        // Filter ArrayAdapter
        mFilterAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                R.layout.filter_spinner);
        mFilterAdapter.add(ALL_STORES);
        // Filter NavigationListener
        final LoaderCallbacks<Cursor> lc = this;
        OnNavigationListener onNavigationListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                mCurFilter = mFilterAdapter.getItem(position);
                Log.i("GroceryListFragment", "Changing filter to: " + mCurFilter);
                filterAdapterReset();
                getLoaderManager().restartLoader(0, null, lc);
                return true;
            }
        };
        actionBar.setListNavigationCallbacks(mFilterAdapter, onNavigationListener);

    }

    // TODO: Find out how to do this without using a viewbinder for better
    // performance
    private class GroceryListViewBinder implements SimpleCursorAdapter.ViewBinder
    {

        @Override
        public boolean setViewValue(final View view, final Cursor cursor, int columnIndex) {
            int viewId = view.getId();
            if (viewId == R.id.grocery_row_syncing_icon)
            {
                // Initialize everything that isn't text
                final View parent = (View) view.getParent();
                // Syncing Icon
                ImageView syncingView = (ImageView) view;

                if (cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.ROWINDEX))
                        .length() > 0) {
                    syncingView.setVisibility(View.GONE);
                }

                // Delete Button
                ImageButton deleteButton = (ImageButton) parent
                        .findViewById(R.id.grocery_row_cross_off_button);
                final ContentValues fullItemValues = GroceryItem
                        .makeFullContentValuesFromCursor(cursor);
                final ContentValues itemValues = GroceryItem
                        .makeGenericContentValuesFromCursor(cursor);
                final String itemName = itemValues.getAsString(GroceryItems.ITEMNAME);
                final String rowIndex = fullItemValues.getAsString(GroceryItems.ROWINDEX);
                final String storesAsCSV = itemValues.getAsString(GroceryItems.STORE);
                deleteButton.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v) {
                        Log.i("GroceryListFragment", "deleting itemName: " + itemName);
                        Animation anim = AnimationUtils.loadAnimation(getActivity()
                                .getApplicationContext(),
                                R.anim.slide_to_right);
                        anim.setDuration(300);
                        parent.startAnimation(anim);
                        new Handler().postDelayed(new Runnable()
                        {
                            public void run()
                            {
                                getActivity().getContentResolver().delete(
                                        GroceryItems.CONTENT_URI,
                                        GroceryItems.ITEMNAME + "=?", new String[]
                                        {
                                                itemName, rowIndex
                                        });

                                getActivity().getContentResolver().insert(
                                        RecentItems.CONTENT_URI, itemValues);
                                filterAdapterReset();
                            }
                        }, 285);
                    }
                });

                // Dropdown button
                ImageButton dropDownButton = (ImageButton) parent
                        .findViewById(R.id.grocery_row_dropdown);
                // registerForContextMenu(dropDownButton);
                registerForContextMenu(parent);
                dropDownButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showOptionsDialog(fullItemValues);
                    }
                });

                // Longclick (for older android users)
                parent.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showOptionsDialog(fullItemValues);
                        return true;
                    }
                });

                // Add store to FilterSpinner
                filterAdapterAdd(storesAsCSV);
                return true;
            }
            return false;
        }
    }
    
    private void filterAdapterAdd(String storesAsCSV)
    {
        for (String store : storesAsCSV.split(","))
        {
            store = store.trim();
            if (store.length() > 0 && mFilterAdapter.getPosition(store) == -1)
            {
                // Doesn't exist, add to ArrayAdapter
                mFilterAdapter.add(store);
            }
        }
    }
    
    private void filterAdapterReset()
    {
        if(mCurFilter.equals(ALL_STORES))
        {
            //On All Stores, reset the adapter
            mFilterAdapter.clear();
            mFilterAdapter.add(ALL_STORES);
        }
        //Not on All Stores, keep existing adapter
    }

    private void showOptionsDialog(final ContentValues values)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.grocery_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getActivity(), GroceryEditItemActivity.class);
                intent.putExtra(GroceryItem.CONTENT_VALUES, values);
                startActivity(intent);
            }
        });
        AlertDialog alert = builder.create();
        alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alert.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        if (mCurFilter != null && !mCurFilter.equals(ALL_STORES))
        {
            // Apply filter
            cursorLoader = new CursorLoader(getActivity(), GroceryItems.CONTENT_URI,
                    mGroceryItemProjection, GroceryItems.STORE + "=?", new String[] {
                            mCurFilter
                    },
                    null);
        }
        else
        {
            // No filter
            cursorLoader = new CursorLoader(getActivity(), GroceryItems.CONTENT_URI,
                    mGroceryItemProjection, null, null,
                    null);
        }
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.grocery_list_menu, menu);
        final MenuItem refresh = (MenuItem) menu.findItem(R.id.grocery_list_menu_refresh);
        refresh.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(MenuItem item)
            {
                new Handler().postDelayed(new Runnable()
                {
                    public void run()
                    {
                        getActivity().getContentResolver().call(GroceryItems.CONTENT_URI,
                                GroceryItemProvider.SYNC_WITH_GOOGLE_DOCS_CALL, null, null);
                    }
                }, 500);
                return true;
            }
        });
    }
}
