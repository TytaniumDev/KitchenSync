
package com.hollanddev.kitchensync.view;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;
import com.hollanddev.kitchensync.util.GroceryItemUtil;

public class GroceryRecentItemsFragment extends RoboSherlockFragment implements
        LoaderManager.LoaderCallbacks<Cursor>
{
    private SimpleCursorAdapter mAdapter;
    private static final int RECENT_LIST_LOADER = 0x02;
    private ListView mListView;
    private GoogleDocsProviderWrapper mContentResolver;
    private LoaderManager mLoaderManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_quickadd_groceryitem, container, false);

        mListView = (ListView) root.findViewById(R.id.grocery_quickadd_listview);
        mContentResolver = ((KitchenSyncApplication) getSherlockActivity().getApplication())
                .getGoogleDocsProviderWrapper();

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoaderManager = getSherlockActivity().getSupportLoaderManager();
        String[] uiBindFrom =
        {
                GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE
        };
        int[] uiBindTo =
        {
                R.id.grocery_quickadd_row_item_name, R.id.grocery_quickadd_row_amount,
                R.id.grocery_quickadd_row_store
        };


        mAdapter = new RecentItemsAdapter(getSherlockActivity().getApplicationContext(),
                R.layout.recent_items_row, null,
                uiBindFrom, uiBindTo, 0);
        mListView.setAdapter(mAdapter);
        mLoaderManager.initLoader(RECENT_LIST_LOADER, null, this);
        getActivity().getSupportLoaderManager().initLoader(RECENT_LIST_LOADER, null, this);
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
            final ContentValues values = GroceryItemUtil
                    .makeGenericContentValuesFromCursor(cursor);
            view.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                    Animation anim = AnimationUtils.loadAnimation(getSherlockActivity()
                            .getApplicationContext(),
                            R.anim.slide_to_right);
                    anim.setDuration(300);
                    view.startAnimation(anim);
                    new Handler().postDelayed(new Runnable()
                    {
                        public void run()
                        {
                            mContentResolver.insert(
                                    GroceryItems.CONTENT_URI, values);
                            mContentResolver.notifyChange(
                                    RecentItems.CONTENT_URI, null);
                            view.invalidate();
                        }
                    }, anim.getDuration());
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        String[] projection =
        {
                RecentItems.ITEM_ID, RecentItems.ITEMNAME, RecentItems.AMOUNT,
                RecentItems.STORE, RecentItems.CATEGORY
        };

        CursorLoader cursorLoader = new CursorLoader(getSherlockActivity(),
                RecentItems.CONTENT_URI,
                projection, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor)
    {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        mAdapter.swapCursor(null);
    }
}
