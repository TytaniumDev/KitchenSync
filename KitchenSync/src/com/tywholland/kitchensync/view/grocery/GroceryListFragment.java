
package com.tywholland.kitchensync.view.grocery;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.KitchenSyncApplication;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

public class GroceryListFragment extends RoboSherlockFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROCERY_LIST_LOADER = 0x01;
    private static final String ALL_STORES = "All Stores";
    private static final String PREFS_FILTER = "curfilter";

    private SimpleCursorAdapter mAdapter;
    private ListView mListView;
    private Spinner mSpinner;
    private String mCurFilter;
    private final String[] mGroceryItemProjection =
    {
            GroceryItems.GROCERY_ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT,
            GroceryItems.STORE,
            GroceryItems.CATEGORY, GroceryItems.ROWINDEX
    };
    private ArrayAdapter<String> mFilterAdapter;
    private Multiset<String> mStoreBag;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_grocerylist, container, false);
        mListView = (ListView) root.findViewById(R.id.grocerylist_listview);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

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
                                ((KitchenSyncApplication) getActivity().getApplication())
                                        .getGoogleDocsProviderWrapper().delete(
                                                GroceryItems.CONTENT_URI,
                                                GroceryItems.ITEMNAME + "=?", new String[]
                                                {
                                                    itemName
                                                }, rowIndex);

                                ((KitchenSyncApplication) getActivity().getApplication())
                                        .getGoogleDocsProviderWrapper().insert(
                                                RecentItems.CONTENT_URI, itemValues);
                                removeStores(storesAsCSV);
                            }
                        }, anim.getDuration() - 15);
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
                return true;
            }
            return false;
        }
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
                    mGroceryItemProjection, GroceryItems.STORE + " LIKE ?", new String[] {
                            "%" + mCurFilter + "%"
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
                        ((KitchenSyncApplication) getActivity().getApplication())
                                .getGoogleDocsProviderWrapper().syncWithGoogleDocs();
                        fillStoreSelectionSpinner();
                    }
                }, 150);
                return true;
            }
        });
        // Grocery store spinner filter stuff
        mSpinner = (Spinner) menu.findItem(R.id.grocery_list_menu_filter).getActionView();
        // Get last filter from sharedprefs
        mCurFilter = getActivity().getPreferences(0).getString(PREFS_FILTER, ALL_STORES);
        Log.i("GroceryListFragment", "Loaded from sharedprefs, curfilter is: " + mCurFilter);

        // Filter ArrayAdapter
        mFilterAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                R.layout.filter_spinner);
        mSpinner.setAdapter(mFilterAdapter);
        fillStoreSelectionSpinner();
        final LoaderCallbacks<Cursor> lc = this;
        mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mCurFilter = mFilterAdapter.getItem(position);
                Log.i("GroceryListFragment",
                        "Filter item selected, setting current filter to: "
                                + mCurFilter);
                getLoaderManager().restartLoader(0, null, lc);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                Log.i("GroceryListFragment", "Nothing selected for some reason?");
            }
        });

    }

    private void fillStoreSelectionSpinner()
    {
        mStoreBag = HashMultiset.create();
        mFilterAdapter.clear();
        mFilterAdapter.add(ALL_STORES);
        // Set spinner items
        Cursor c = ((KitchenSyncApplication) getActivity().getApplication())
                .getGoogleDocsProviderWrapper().query(GroceryItems.CONTENT_URI, new String[] {
                        GroceryItems.GROCERY_ITEM_ID, GroceryItems.STORE
                }, null, null, null);
        while (c.moveToNext())
        {
            // Add store to FilterSpinner
            filterAdapterAdd(c.getString(c.getColumnIndexOrThrow(GroceryItems.STORE)));
        }
        // Set to last selected store
        Log.i("GroceryListFragment", "mFilteAdapter size is: " + mFilterAdapter.getCount());
        int position = mFilterAdapter.getPosition(mCurFilter);
        Log.i("GroceryListFragment", "Trying to set spinner position to " + mCurFilter
                + ", is: " + position);
        if (position != -1)
        {
            mSpinner.setSelection(position);
        }
    }

    private void filterAdapterAdd(String storesAsCSV)
    {
        for (String store : storesAsCSV.split(","))
        {
            store = store.trim();
            // Add to bag always
            mStoreBag.add(store);
            if (store.length() > 0 && mFilterAdapter.getPosition(store) == -1)
            {
                // Doesn't exist, add to ArrayAdapter
                mFilterAdapter.add(store);
            }
        }
    }

    private void removeStores(String storesAsCSV)
    {
        for (String store : storesAsCSV.split(","))
        {
            store = store.trim();
            mStoreBag.remove(store);
            if (mStoreBag.count(store) <= 0)
            {
                Log.i("GroceryListFragment", "Removing " + store + " from spinner");
                mFilterAdapter.remove(store);
                // Reset to All Stores
                mSpinner.setSelection(0);
            }
        }
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        // Save spinner state to sharedprefs
        SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
        editor.putString(PREFS_FILTER, mCurFilter);
        editor.commit();
        Log.i("GroceryListFragment", "Saving current filter, is: " + mCurFilter);
    }
}
