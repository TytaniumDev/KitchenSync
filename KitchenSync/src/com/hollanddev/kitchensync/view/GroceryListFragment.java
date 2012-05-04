
package com.hollanddev.kitchensync.view;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
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
import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.R.anim;
import com.hollanddev.kitchensync.R.layout;
import com.hollanddev.kitchensync.model.GroceryItem;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;
import com.hollanddev.kitchensync.util.GroceryItemUtil;


public class GroceryListFragment extends RoboSherlockFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROCERY_LIST_LOADER = 0x01;
    private static final String ALL_STORES = "All Stores";
    private static final String PREFS_FILTER = "curfilter";
    private static final String[] GROCERY_ITEMS_PROJECTION =
        {
        GroceryItems.ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT,
        GroceryItems.STORE,
        GroceryItems.CATEGORY, GroceryItems.ROWINDEX
        };

    private SimpleCursorAdapter mAdapter;
    private ListView mListView;
    private String mCurFilter;
    private MenuItem mRefreshItem;
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

        getSherlockActivity().getSupportLoaderManager().initLoader(GROCERY_LIST_LOADER, null, this);
        mAdapter = new GroceryListAdapter(getSherlockActivity().getApplicationContext(),
                R.layout.grocery_list_row, null,
                uiBindFrom, uiBindTo, 0);
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
            final ContentValues fullItemValues = GroceryItemUtil
                    .makeFullContentValuesFromCursor(cursor);
            // Need just generic values for adding to recent items
            final ContentValues itemValues = GroceryItemUtil
                    .makeGenericContentValuesFromCursor(cursor);
            final String rowIndex = fullItemValues.getAsString(GroceryItems.ROWINDEX);
            final String itemName = itemValues.getAsString(GroceryItems.ITEMNAME);
            final String storesAsCSV = itemValues.getAsString(GroceryItems.STORE);
            final GoogleDocsProviderWrapper gdocProviderWrapper = ((KitchenSyncApplication) getSherlockActivity()
                    .getApplication()).getGoogleDocsProviderWrapper();
            final Animation anim = AnimationUtils.loadAnimation(getSherlockActivity()
                    .getApplicationContext(),
                    R.anim.shrink_fade_out_from_bottom);
            deleteButton.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    Log.i("GroceryListFragment", "deleting itemName: " + itemName);
                    view.startAnimation(anim);
                    new Handler().postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            gdocProviderWrapper.delete(
                                    GroceryItems.CONTENT_URI,
                                    GroceryItems.ITEMNAME + "=?", new String[]
                                    {
                                        itemName
                                    }, rowIndex);
                            gdocProviderWrapper.insert(RecentItems.CONTENT_URI, itemValues);
                            getSherlockActivity().getContentResolver().notifyChange(
                                    GroceryItems.CONTENT_URI, null);
                            getSherlockActivity().getContentResolver().notifyChange(
                                    RecentItems.CONTENT_URI, null);
                            view.invalidate();
                        }
                    }, anim.getDuration());
                    // Move this out of postDelayed for better animation
                    removeStores(storesAsCSV);
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

            view.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                    {
                        v.setBackgroundColor(Color.parseColor("#8833B5E5"));
                    }
                    else
                    {
                        v.setBackgroundColor(Color.TRANSPARENT);
                    }
                    return false;
                }
            });
        }
    }

    private void showOptionsDialog(final ContentValues values)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setItems(R.array.grocery_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getSherlockActivity(), GroceryEditItemActivity.class);
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
            cursorLoader = new CursorLoader(getSherlockActivity(), GroceryItems.CONTENT_URI,
                    GROCERY_ITEMS_PROJECTION, GroceryItems.STORE + " LIKE ?", new String[] {
                            "%" + mCurFilter + "%"
                    },
                    null);
        }
        else
        {
            // No filter
            cursorLoader = new CursorLoader(getSherlockActivity(), GroceryItems.CONTENT_URI,
                    GROCERY_ITEMS_PROJECTION, null, null,
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
        if (PreferenceManager.getDefaultSharedPreferences(getSherlockActivity()).getBoolean(
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
                    LayoutInflater inflater = (LayoutInflater) getSherlockActivity()
                            .getSystemService(
                                    Context.LAYOUT_INFLATER_SERVICE);
                    ImageView iv = (ImageView) inflater.inflate(layout.refresh_action_view, null);

                    Animation rotation = AnimationUtils.loadAnimation(getSherlockActivity(),
                            anim.clockwise_refresh);
                    rotation.setRepeatCount(Animation.INFINITE);
                    iv.startAnimation(rotation);

                    mRefreshItem.setActionView(iv);
                    new Handler().postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            ((KitchenSyncApplication) getSherlockActivity().getApplication())
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
        mCurFilter = getSherlockActivity().getPreferences(0).getString(PREFS_FILTER, ALL_STORES);
        Log.i("GroceryListFragment", "Loaded from sharedprefs, curfilter is: " + mCurFilter);

        // Filter ArrayAdapter
        final LoaderCallbacks<Cursor> lc = this;
        mFilterAdapter = new ArrayAdapter<String>(getSherlockActivity().getApplicationContext(),
                R.layout.filter_spinner);
        OnNavigationListener navListener = new OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                mCurFilter = mFilterAdapter.getItem(itemPosition);
                Log.i("GroceryListFragment",
                        "Filter item selected, setting current filter to: "
                                + mCurFilter);
                // Save spinner state to sharedprefs
                SharedPreferences.Editor editor = getSherlockActivity().getPreferences(0).edit();
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
            Cursor c = ((KitchenSyncApplication) getSherlockActivity().getApplication())
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
