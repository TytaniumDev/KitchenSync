package com.tywholland.kitchensync.view.grocery;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.adapter.DBAdapter;

public class GroceryQuickAddItemFragment extends RoboSherlockFragment
{

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_quickadd_groceryitem, container, true);
      
      ListView listView = (ListView) root.findViewById(R.id.grocery_quickadd_listview);
      return root;
   }
   
   

   private class RecentItemsCursorAdapter extends CursorAdapter
   {
      public RecentItemsCursorAdapter(Context context, Cursor c, boolean autoRequery)
      {
         super(context, c, autoRequery);
      }

      @Override
      public void bindView(final View view, Context context, final Cursor c)
      {
         int itemNameIndex = c.getColumnIndex(DBAdapter.KEY_ITEMNAME);
         int amountIndex = c.getColumnIndex(DBAdapter.KEY_AMOUNT);
         int storeIndex = c.getColumnIndex(DBAdapter.KEY_STORE);
         int categoryIndex = c.getColumnIndex(DBAdapter.KEY_CATEGORY);
         
         final String itemName = c.getString(itemNameIndex);
         final String amount = c.getString(amountIndex);
         final String store = c.getString(storeIndex);
         final String category = c.getString(categoryIndex);
         
         // Item Name
         TextView itemNameView = (TextView) view.findViewById(R.id.grocery_quickadd_row_item_name);
         itemNameView.setText(itemName);

         // Amount
         TextView amountView = (TextView) view.findViewById(R.id.grocery_quickadd_row_amount);
         amountView.setText(amount);

         // Store
         TextView storeView = (TextView) view.findViewById(R.id.grocery_quickadd_row_store);
         storeView.setText(store);
         
         view.setOnClickListener(new OnClickListener()
         {
            
            @Override
            public void onClick(View v)
            {
               Animation anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                     R.anim.slide_to_right);
               anim.setDuration(300);
               view.startAnimation(anim);
               new Handler().postDelayed(new Runnable()
               {
                  public void run()
                  {
                  }
               }, 300);               
            }
         });
      }

      @Override
      public View newView(Context arg0, Cursor arg1, ViewGroup arg2)
      {
         return LayoutInflater.from(arg0).inflate(R.layout.recent_items_row, null);
      }
   }
}
