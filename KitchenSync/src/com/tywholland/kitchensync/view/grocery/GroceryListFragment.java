package com.tywholland.kitchensync.view.grocery;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;

public class GroceryListFragment extends RoboSherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor>
{
   private static final int    GROCERY_LIST_LOADER = 0x01;

   private SimpleCursorAdapter adapter;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      String[] uiBindFrom =
      { GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE, GroceryItems.ROWINDEX, GroceryItems.ITEMNAME };
      int[] uiBindTo =
      { R.id.grocery_row_item_name, R.id.grocery_row_amount, R.id.grocery_row_store, R.id.grocery_row_syncing_icon,
            R.id.grocery_row_cross_off_button };

      getActivity().getSupportLoaderManager().initLoader(GROCERY_LIST_LOADER, null, this);

      adapter = new SimpleCursorAdapter(getActivity().getApplicationContext(), R.layout.grocery_list_row, null,
            uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
      adapter.setViewBinder(new GroceryListViewBinder());
      setListAdapter(adapter);
   }

   private class GroceryListViewBinder implements SimpleCursorAdapter.ViewBinder
   {

      @Override
      public boolean setViewValue(final View view, final Cursor cursor, int columnIndex)
      {
         int viewId = view.getId();
         switch (viewId)
         {
            case R.id.grocery_row_syncing_icon:
               // Syncing Icon
               ImageView syncingView = (ImageView) view;

               if (cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.ROWINDEX)).length() > 0)
               {
                  syncingView.setAlpha(0);
               }
               return true;

            case R.id.grocery_row_cross_off_button:
               // Delete Button
               ImageButton deleteButton = (ImageButton) view.findViewById(R.id.grocery_row_cross_off_button);
               final String itemName = cursor.getString(cursor.getColumnIndexOrThrow(GroceryItems.ITEMNAME));
               final View parent = (View) view.getParent();
               deleteButton.setOnClickListener(new OnClickListener()
               {
                  @Override
                  public void onClick(View v)
                  {
                     Log.i("GroceryListFragment", "deleting itemName: " + itemName);
                     Animation anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                           R.anim.slide_to_right);
                     anim.setDuration(300);
                     parent.startAnimation(anim);
                     new Handler().postDelayed(new Runnable()
                     {
                        public void run()
                        {
                           getActivity().getContentResolver().delete(GroceryItems.CONTENT_URI,
                                 GroceryItems.ITEMNAME + "=?", new String[]
                                 { itemName });

                        }
                     }, 299);
                     getActivity().getContentResolver().insert(RecentItems.CONTENT_URI,
                           GroceryItem.makeGenericContentValuesFromCursor(cursor));
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
      { GroceryItems.GROCERY_ITEM_ID, GroceryItems.ITEMNAME, GroceryItems.AMOUNT, GroceryItems.STORE,
            GroceryItems.CATEGORY, GroceryItems.ROWINDEX };

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
