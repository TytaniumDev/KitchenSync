
package com.hollanddev.kitchensync.view.grocery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.grocery.GroceryItem;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.R;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;

public class GroceryQuickAddItemFragment extends RoboSherlockFragment implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    private SimpleCursorAdapter adapter;
    private static final int RECENT_LIST_LOADER = 0x02;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_quickadd_groceryitem, container, false);

        ListView listView = (ListView) root.findViewById(R.id.grocery_quickadd_listview);
        String[] uiBindFrom =
        {
                GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE
        };
        int[] uiBindTo =
        {
                R.id.grocery_quickadd_row_item_name, R.id.grocery_quickadd_row_amount,
                R.id.grocery_quickadd_row_store
        };

        getActivity().getSupportLoaderManager().initLoader(RECENT_LIST_LOADER, null, this);

        adapter = new RecentItemsAdapter(getActivity().getApplicationContext(),
                R.layout.recent_items_row, null,
                uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listView.setAdapter(adapter);
        return root;
    }

    private class RecentItemsAdapter extends SimpleCursorAdapter
    {
        public RecentItemsAdapter(Context context, int layout, Cursor c, String[] from, int[] to,
                int flags) {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(final View view, Context context, Cursor cursor) {
            super.bindView(view, context, cursor);
            final ContentValues values = GroceryItem
                    .makeGenericContentValuesFromCursor(cursor);
            view.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

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
                                    .getGoogleDocsProviderWrapper().insert(
                                            GroceryItems.CONTENT_URI, values);
                            getActivity().getContentResolver().notifyChange(
                                    RecentItems.CONTENT_URI, null);
                        }
                    }, 150);
                }
            });
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection =
        {
                RecentItems.ITEM_ID, RecentItems.ITEMNAME, RecentItems.AMOUNT,
                RecentItems.STORE, RecentItems.CATEGORY
        };

        CursorLoader cursorLoader = new CursorLoader(getActivity(), RecentItems.CONTENT_URI,
                projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        adapter.swapCursor(null);
    }
}
