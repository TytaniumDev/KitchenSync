package com.tywholland.kitchensync.view.grocery;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;

public class GroceryQuickAddItemFragment extends RoboSherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
   private SimpleCursorAdapter adapter;
   private static final int    RECENT_LIST_LOADER = 0x02;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_quickadd_groceryitem, container, true);

      ListView listView = (ListView) root.findViewById(R.id.grocery_quickadd_listview);
      String[] uiBindFrom =
      { GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE, GroceryItems.ITEMNAME };
      int[] uiBindTo =
      { R.id.grocery_quickadd_row_item_name, R.id.grocery_quickadd_row_amount, R.id.grocery_quickadd_row_store,
            R.id.grocery_quickadd_row_rightbuffer };

      getActivity().getSupportLoaderManager().initLoader(RECENT_LIST_LOADER, null, this);

      adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.recent_items_row, null,
            uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
      adapter.setViewBinder(new RecentItemsListViewBinder());
      listView.setAdapter(adapter);
      return root;
   }

   private class RecentItemsListViewBinder implements SimpleCursorAdapter.ViewBinder
   {
      @Override
      public boolean setViewValue(final View view, final Cursor cursor, int columnIndex)
      {

         int viewId = view.getId();
         switch (viewId)
         {
            case R.id.grocery_quickadd_row_rightbuffer:
               // TODO: Put this in an onItemClickListener?
               final View parent = (View) view.getParent();
               final ContentValues values = GroceryItem.makeGenericContentValuesFromCursor(cursor);
               parent.setOnClickListener(new OnClickListener()
               {
                  @Override
                  public void onClick(View v)
                  {

                     Animation anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                           R.anim.slide_to_right);
                     anim.setDuration(300);
                     parent.startAnimation(anim);
                     new Handler().postDelayed(new Runnable()
                     {
                        public void run()
                        {
                           getActivity().getContentResolver().insert(GroceryItems.CONTENT_URI, values);
                           getActivity().getContentResolver().notifyChange(RecentItems.CONTENT_URI, null);
                        }
                     }, 150);
                  }
               });
               return true;
         }
         return false;
      }

   }

   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args)
   {
      String[] projection =
      { RecentItems.GROCERY_ITEM_ID, RecentItems.ITEMNAME, RecentItems.AMOUNT, RecentItems.STORE, RecentItems.CATEGORY };

      CursorLoader cursorLoader = new CursorLoader(getActivity(), RecentItems.CONTENT_URI, projection, null, null, null);
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
