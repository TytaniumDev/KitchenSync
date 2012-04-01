package com.tywholland.kitchensync.view.grocery;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;

public class GroceryListFragment extends RoboSherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
   private static final int    GROCERY_LIST_LOADER = 0x01;

   private SimpleCursorAdapter adapter;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      String[] uiBindFrom =
      { GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE };
      int[] uiBindTo =
      { R.id.grocery_row_item_name, R.id.grocery_row_amount, R.id.grocery_row_store };

      getLoaderManager().initLoader(GROCERY_LIST_LOADER, null, this);

      adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.grocery_list_row, null,
            uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

      setListAdapter(adapter);
   }

   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args)
   {
      String[] projection =
      { GroceryItems.GROCERY_ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE };

      CursorLoader cursorLoader = new CursorLoader(getActivity(), GroceryItems.CONTENT_URI, projection, null, null,
            null);
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
