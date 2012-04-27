
package com.hollanddev.kitchensync.view.grocery;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.grocery.GroceryItem;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.R.anim;
import com.hollanddev.kitchensync.R.layout;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

public class GroceryListFragment extends RoboSherlockFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROCERY_LIST_LOADER = 0x01;
    private static final String ALL_STORES = "All Stores";
    private static final String PREFS_FILTER = "curfilter";

    private SimpleCursorAdapter mAdapter;
    private ListView mListView;
    private String mCurFilter;
    private MenuItem mRefreshItem;
    private final String[] mGroceryItemProjection =
    {
            GroceryItems.ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT,
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
                GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE
        };
        int[] uiBindTo =
        {
                R.id.grocery_row_item_name, R.id.grocery_row_amount, R.id.grocery_row_store
        };

        getActivity().getSupportLoaderManager().initLoader(GROCERY_LIST_LOADER, null, this);
        mAdapter = new GroceryListAdapter(getActivity().getApplicationContext(),
                R.layout.grocery_list_row, null,
                uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

    }

    private class GroceryListAdapter extends SimpleCursorAdapter
    {
        public GroceryListAdapter(Context context, int layout, Cursor c, String[] from, int[] to,
                int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            fillStoreSelectionSpinner();
            if (mRefreshItem != null && mRefreshItem.getActionView() != null) {
                mRefreshItem.getActionView().clearAnimation();
                mRefreshItem.setActionView(null);
            }
        }

        @Override
        public void bindView(final View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            // Syncing icon
            ImageView syncingView = (ImageView) view.findViewById(R.id.grocery_row_syncing_icon);
            if (cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.ROWINDEX))
                    .length() > 0) {
                syncingView.setVisibility(View.GONE);
            }
            // Delete Button
            ImageButton deleteButton = (ImageButton) view
                    .findViewById(R.id.grocery_row_cross_off_button);
            // Need full item values to delete from google docs
            final ContentValues fullItemValues = GroceryItem
                    .makeFullContentValuesFromCursor(cursor);
            // Need just generic values for adding to recent items
            final ContentValues itemValues = GroceryItem
                    .makeGenericContentValuesFromCursor(cursor);
            final String rowIndex = fullItemValues.getAsString(GroceryItems.ROWINDEX);
            final String itemName = itemValues.getAsString(GroceryItems.ITEMNAME);
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
                    view.startAnimation(anim);
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
            ImageButton dropDownButton = (ImageButton) view
                    .findViewById(R.id.grocery_row_dropdown);
            registerForContextMenu(view);
            dropDownButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    showOptionsDialog(fullItemValues);
                }
            });

            // Longclick (for older android users)
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showOptionsDialog(fullItemValues);
                    return true;
                }
            });
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
        mRefreshItem = (MenuItem) menu.findItem(R.id.grocery_list_menu_refresh);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(
                "GOOGLE_DOCS_SYNC", true))
        {
            mRefreshItem.setOnMenuItemClickListener(new OnMenuItemClickListener()
            {
                public boolean onMenuItemClick(MenuItem item)
                {
                    /*
                     * Attach a rotating ImageView to the refresh item as an
                     * ActionView
                     */
                    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                            Context.LAYOUT_INFLATER_SERVICE);
                    ImageView iv = (ImageView) inflater.inflate(layout.refresh_action_view, null);

                    Animation rotation = AnimationUtils.loadAnimation(getActivity(),
                            anim.clockwise_refresh);
                    rotation.setRepeatCount(Animation.INFINITE);
                    iv.startAnimation(rotation);

                    mRefreshItem.setActionView(iv);
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
        }
        else
        {
            // Google docs syncing turned off, hide syncing icon
            mRefreshItem.setVisible(false);
        }
        // Grocery store spinner filter stuff
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setDisplayShowTitleEnabled(false);
        // Get last filter from sharedprefs
        mCurFilter = getActivity().getPreferences(0).getString(PREFS_FILTER, ALL_STORES);
        Log.i("GroceryListFragment", "Loaded from sharedprefs, curfilter is: " + mCurFilter);

        // Filter ArrayAdapter
        final LoaderCallbacks<Cursor> lc = this;
        mFilterAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                R.layout.filter_spinner);
        OnNavigationListener navListener = new OnNavigationListener() {

            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                mCurFilter = mFilterAdapter.getItem(itemPosition);
                Log.i("GroceryListFragment",
                        "Filter item selected, setting current filter to: "
                                + mCurFilter);
                // Save spinner state to sharedprefs
                SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
                editor.putString(PREFS_FILTER, mCurFilter);
                editor.commit();
                getLoaderManager().restartLoader(0, null, lc);
                return true;
            }
        };
        actionBar.setListNavigationCallbacks(mFilterAdapter, navListener);
        fillStoreSelectionSpinner();

    }

    private void fillStoreSelectionSpinner()
    {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        if (actionBar.getNavigationMode() == ActionBar.NAVIGATION_MODE_LIST)
        {

            mStoreBag = HashMultiset.create();
            mFilterAdapter.clear();
            mFilterAdapter.add(ALL_STORES);
            // Set spinner items
            Cursor c = ((KitchenSyncApplication) getActivity().getApplication())
                    .getGoogleDocsProviderWrapper().query(GroceryItems.CONTENT_URI, new String[] {
                            GroceryItems.ITEM_ID, GroceryItems.STORE
                    }, null, null, null);
            while (c.moveToNext())
            {
                // Add store to FilterSpinner
                filterAdapterAdd(c.getString(c.getColumnIndexOrThrow(GroceryItems.STORE)));
            }
            c.close();
            // Set to last selected store
            Log.i("GroceryListFragment", "mFilteAdapter size is: " + mFilterAdapter.getCount());
            int position = mFilterAdapter.getPosition(mCurFilter);
            Log.i("GroceryListFragment", "Trying to set spinner position to " + mCurFilter
                    + ", is: " + position);
            if (position != -1)
            {
                getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(position);
            }
            else
            {
                // Set to all stores if no position
                getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(0);
            }
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
                getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(0);
                // mSpinner.setSelection(0);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getSherlockActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        // Stop refresh icon from spinning, otherwise it overlays on the view
        if (mRefreshItem != null && mRefreshItem.getActionView() != null) {
            mRefreshItem.getActionView().clearAnimation();
            mRefreshItem.setActionView(null);
        }
        // Change action bar back to not have store filter
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);

        Log.i("GroceryListFragment", "Saving current filter, is: " + mCurFilter);
    }
}
