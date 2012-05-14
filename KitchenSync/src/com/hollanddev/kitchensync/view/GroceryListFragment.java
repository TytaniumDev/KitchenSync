
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

import java.util.Comparator;

public class GroceryListFragment extends RoboSherlockFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int GROCERY_LIST_LOADER = 0x01;
    private static final String ALL_STORES = "All Stores";
    private static final String PREFS_FILTER = "curfilter";
    private static final String SORT_MODE_CURRENT = "sortmode_current";
    private static final String SORT_MODE_ALPHABETICAL = "sortmode_alphabetical";
    private static final String SORT_MODE_CATEGORY = "sortmode_category";
    private static final String SORT_MODE_ASC = "ASC";
    private static final String SORT_MODE_DESC = "DESC";
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
    private MenuItem mSortAlphabetical;
    private MenuItem mSortCategory;
    private MenuItem mSortOrder;
    private ArrayAdapter<String> mFilterAdapter;
    private Multiset<String> mStoreBag;
    private GoogleDocsProviderWrapper mContentResolver;
    private LoaderCallbacks<Cursor> mLoaderCallbacks;
    private LoaderManager mLoaderManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_grocerylist, container, false);
        mListView = (ListView) root.findViewById(R.id.grocerylist_listview);
        mContentResolver = ((KitchenSyncApplication) getSherlockActivity().getApplication())
                .getGoogleDocsProviderWrapper();
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mLoaderManager = getSherlockActivity().getSupportLoaderManager();
        mLoaderCallbacks = this;
        setHasOptionsMenu(true);

        String[] uiBindFrom =
        {
                GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE,
                GroceryItems.CATEGORY
        };
        int[] uiBindTo =
        {
                R.id.grocery_row_item_name, R.id.grocery_row_amount, R.id.grocery_row_store,
                R.id.grocery_row_category
        };

        mAdapter = new GroceryListAdapter(getSherlockActivity().getApplicationContext(),
                R.layout.grocery_list_row, null,
                uiBindFrom, uiBindTo, 0);
        mListView.setAdapter(mAdapter);
        mLoaderManager.initLoader(GROCERY_LIST_LOADER, null, this);

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
//            fillStoreSelectionSpinner();
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
            if (PreferenceManager.getDefaultSharedPreferences(getSherlockActivity()).getBoolean(
                    "GOOGLE_DOCS_SYNC", true)
                    && cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.ROWINDEX))
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
                            mContentResolver.delete(
                                    GroceryItems.CONTENT_URI,
                                    GroceryItems.ITEMNAME + "=?", new String[]
                                    {
                                        itemName
                                    }, rowIndex);
                            mContentResolver.insert(RecentItems.CONTENT_URI, itemValues);
                            mContentResolver.notifyChange(
                                    GroceryItems.CONTENT_URI, null);
                            mContentResolver.notifyChange(
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
                    showOptionsDialog(fullItemValues, v);
                }
            });

            // Longclick (for older android users)
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    showOptionsDialog(fullItemValues, v);
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

    private void showOptionsDialog(final ContentValues values, final View view)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
        builder.setItems(R.array.grocery_dialog_options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0)
                {
                    // Edit
                    Intent intent = new Intent(getSherlockActivity(), GroceryEditItemActivity.class);
                    intent.putExtra(GroceryItem.CONTENT_VALUES, values);
                    startActivity(intent);
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        alert.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader;
        String orderBy = getOrderBySQL();
        Log.i("Loader", "SQL orderby is " + orderBy);
        Log.i("Loader", "mCurFilter in cursor loader is  " + mCurFilter);
        if (mCurFilter != null && !mCurFilter.equals(ALL_STORES))
        {
            // Apply filter
            cursorLoader = new CursorLoader(getSherlockActivity(), GroceryItems.CONTENT_URI,
                    GROCERY_ITEMS_PROJECTION, GroceryItems.STORE + " LIKE ?", new String[] {
                            "%" + mCurFilter + "%"
                    },
                    orderBy);
        }
        else
        {
            // No filter
            cursorLoader = new CursorLoader(getSherlockActivity(), GroceryItems.CONTENT_URI,
                    GROCERY_ITEMS_PROJECTION, null, null,
                    orderBy);
        }
        return cursorLoader;
    }

    private String getOrderBySQL()
    {
        String alphaSort = getSherlockActivity().getPreferences(0).getString(
                SORT_MODE_ALPHABETICAL, SORT_MODE_ASC);
        String categorySort = getSherlockActivity().getPreferences(0).getString(SORT_MODE_CATEGORY,
                SORT_MODE_ASC);
        if (getSherlockActivity().getPreferences(0)
                .getString(SORT_MODE_CURRENT, SORT_MODE_CATEGORY).equals(SORT_MODE_CATEGORY))
        {
            // Sort by category first
            return GroceryItems.CATEGORY + " " + categorySort + ", " + GroceryItems.ITEMNAME
                    + " COLLATE NOCASE " + alphaSort;
        }
        else
        {
            // Sort by alphabetical first
            return GroceryItems.ITEMNAME + " COLLATE NOCASE " + alphaSort + ", "
                    + GroceryItems.CATEGORY + " " + categorySort;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i("Loader", "in loadfinished, loader id is: " + loader.getId() + ", cursor rows: " + cursor.getCount());
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i("Loader", "in loaderreset");
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.grocery_list_menu, menu);
        mRefreshItem = (MenuItem) menu.findItem(R.id.grocery_list_menu_refresh);
        mSortAlphabetical = (MenuItem) menu.findItem(R.id.grocery_list_menu_sort_alphabetical);
        mSortOrder = (MenuItem) menu.findItem(R.id.grocery_list_menu_sort_order);
        mSortCategory = (MenuItem) menu.findItem(R.id.grocery_list_menu_sort_category);
        setupSortingMenuItems(mLoaderCallbacks);
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
                            mContentResolver.syncWithGoogleDocs();
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
                mLoaderManager.restartLoader(GROCERY_LIST_LOADER, null, mLoaderCallbacks);
                return true;
            }
        };
        actionBar.setListNavigationCallbacks(mFilterAdapter, navListener);
        fillStoreSelectionSpinner();
    }

    private void setupSortingMenuItems(final LoaderCallbacks<Cursor> lc)
    {
        // Restore values
        if (getSherlockActivity().getPreferences(0)
                .getString(SORT_MODE_ALPHABETICAL, SORT_MODE_ASC).equals(SORT_MODE_ASC))
        {
            mSortAlphabetical.setTitle(R.string.grocery_menu_sort_alphabetical_desc);
        }
        else
        {
            mSortAlphabetical.setTitle(R.string.grocery_menu_sort_alphabetical_asc);
        }
        if (getSherlockActivity().getPreferences(0).getString(SORT_MODE_CATEGORY, SORT_MODE_ASC)
                .equals(SORT_MODE_ASC))
        {
            mSortCategory.setTitle(R.string.grocery_menu_sort_category_desc);
        }
        else
        {
            mSortCategory.setTitle(R.string.grocery_menu_sort_category_asc);
        }
        if (getSherlockActivity().getPreferences(0)
                .getString(SORT_MODE_CURRENT, SORT_MODE_CATEGORY).equals(SORT_MODE_CATEGORY))
        {
            mSortOrder.setTitle(R.string.grocery_menu_sort_alphabetical);
        }
        else
        {
            mSortOrder.setTitle(R.string.grocery_menu_sort_category);
        }
        // Set listeners
        mSortAlphabetical.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = getSherlockActivity().getPreferences(0).edit();
                // If it's already on alphabetical, switch asc/desc
                if (item.getTitle().equals(
                        getResources().getString(R.string.grocery_menu_sort_alphabetical_asc)))
                {
                    editor.putString(SORT_MODE_ALPHABETICAL, SORT_MODE_ASC);
                    item.setTitle(R.string.grocery_menu_sort_alphabetical_desc);
                }
                else
                {
                    editor.putString(SORT_MODE_ALPHABETICAL, SORT_MODE_DESC);
                    item.setTitle(R.string.grocery_menu_sort_alphabetical_asc);
                }
                editor.commit();
                mLoaderManager.restartLoader(GROCERY_LIST_LOADER, null, lc);
                return true;
            }
        });
        mSortCategory.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = getSherlockActivity().getPreferences(0).edit();
                // If it's already on alphabetical, switch asc/desc
                if (item.getTitle().equals(
                        getResources().getString(R.string.grocery_menu_sort_category_asc)))
                {
                    editor.putString(SORT_MODE_CATEGORY, SORT_MODE_ASC);
                    item.setTitle(R.string.grocery_menu_sort_category_desc);
                }
                else
                {
                    editor.putString(SORT_MODE_CATEGORY, SORT_MODE_DESC);
                    item.setTitle(R.string.grocery_menu_sort_category_asc);
                }
                editor.commit();
                mLoaderManager.restartLoader(GROCERY_LIST_LOADER, null, lc);
                return true;
            }
        });
        mSortOrder.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                SharedPreferences.Editor editor = getSherlockActivity().getPreferences(0).edit();
                // If it's already on alphabetical, switch to category, and vice
                // versa
                if (item.getTitle().equals(
                        getResources().getString(R.string.grocery_menu_sort_alphabetical)))
                {
                    editor.putString(SORT_MODE_CURRENT, SORT_MODE_ALPHABETICAL);
                    item.setTitle(R.string.grocery_menu_sort_category);
                }
                else
                {
                    editor.putString(SORT_MODE_CURRENT, SORT_MODE_CATEGORY);
                    item.setTitle(R.string.grocery_menu_sort_alphabetical);
                }
                editor.commit();
                mLoaderManager.restartLoader(GROCERY_LIST_LOADER, null, lc);
                return true;
            }
        });
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
            mFilterAdapter.sort(new Comparator<String>() {
                @Override
                public int compare(String arg0, String arg1) {
                    // Keep All Stores at the top, otherwise sort alphabetically
                    if (arg0.equals(ALL_STORES))
                    {
                        return -1;
                    }
                    else if (arg1.equals(ALL_STORES))
                    {
                        return 1;
                    }
                    else
                    {
                        return arg0.toUpperCase().compareTo(arg1.toUpperCase());
                    }
                }
            });
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
        boolean moreThanOneStore = false;
        for (String store : storesAsCSV.split(","))
        {
            moreThanOneStore = true;
            store = store.trim();
            mStoreBag.remove(store);
            if (mStoreBag.count(store) <= 0)
            {
                Log.i("GroceryListFragment", "Removing " + store + " from spinner");
                mFilterAdapter.remove(store);
                // Reset to All Stores
                getSherlockActivity().getSupportActionBar().setSelectedNavigationItem(0);
            }
        }
        if (moreThanOneStore)
        {
            mLoaderManager.restartLoader(GROCERY_LIST_LOADER, null, mLoaderCallbacks);
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
