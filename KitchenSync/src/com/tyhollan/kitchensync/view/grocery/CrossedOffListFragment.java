package com.tyhollan.kitchensync.view.grocery;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.model.KitchenSyncApplication;
import com.tyhollan.kitchensync.model.GroceryItem;
import com.tyhollan.kitchensync.model.GroceryListModel;

//TODO: make this implement google analytics stuff
public class CrossedOffListFragment extends ListFragment
{
   private ArrayAdapter<GroceryItem> mCrossedOffListAdapter;
   private GroceryListModel          mGroceryListModel;

   @Override
   public void onActivityCreated(Bundle savedState)
   {
      super.onActivityCreated(savedState);
      setHasOptionsMenu(true);
      mGroceryListModel = ((KitchenSyncApplication) getActivity().getApplicationContext()).getGroceryListModel();

      mCrossedOffListAdapter = getCrossedOffListAdapter();
      this.setListAdapter(mCrossedOffListAdapter);
      mGroceryListModel.setCrossedOffListAdapter(mCrossedOffListAdapter);
      mGroceryListModel.setCrossedOffListActivity(getActivity());
   }

   private ArrayAdapter<GroceryItem> getCrossedOffListAdapter()
   {
      return new ArrayAdapter<GroceryItem>(getActivity(), R.layout.grocery_list_row,
            mGroceryListModel.getCrossedOffListArray())
      {
         @Override
         public View getView(final int position, View convertView, ViewGroup parent)
         {
            final View row;
            if (null == convertView)
            {
               LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(
                     Context.LAYOUT_INFLATER_SERVICE);
               row = inflater.inflate(R.layout.grocery_list_row, null);
            }
            else
            {
               row = convertView;
            }
            // Item Name
            TextView itemNameView = (TextView) row.findViewById(R.id.groceryRowItemName);
            itemNameView.setText(getItem(position).getItemName());

            // Amount
            TextView amountView = (TextView) row.findViewById(R.id.groceryRowAmount);
            amountView.setText(getItem(position).getAmount());

            // Delete Button
            ImageButton deleteButton = (ImageButton) row.findViewById(R.id.groceryRowCrossedOffButton);
            deleteButton.setOnClickListener(new OnClickListener()
            {
               @Override
               public void onClick(View v)
               {
                  Animation anim = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.slide_to_right);
                  anim.setDuration(300);
                  row.startAnimation(anim);
                  new Handler().postDelayed(new Runnable() {
                     public void run() {
                        mGroceryListModel.deleteGroceryItem(getItem(position));
                     }

                 }, 300);
               }
            });
            
            return row;
         }
      };
   }
}
