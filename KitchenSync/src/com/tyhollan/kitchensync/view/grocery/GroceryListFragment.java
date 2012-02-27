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
import com.tyhollan.kitchensync.model.GroceryItem;
import com.tyhollan.kitchensync.model.GroceryListModel;
import com.tyhollan.kitchensync.model.KitchenSyncApplication;

//TODO: make this implement google analytics stuff
public class GroceryListFragment extends ListFragment
{
   private boolean                   mDualPane;
   private int                       mCurCheckPosition = 0;
   private ArrayAdapter<GroceryItem> mGroceryListAdapter;
   private GroceryListModel          mGroceryListModel;

   @Override
   public void onActivityCreated(Bundle savedState)
   {
      super.onActivityCreated(savedState);
      setHasOptionsMenu(true);
      mGroceryListModel = ((KitchenSyncApplication) getActivity().getApplicationContext()).getGroceryListModel();

      mGroceryListAdapter = getGroceryListAdapter();
      this.setListAdapter(mGroceryListAdapter);
      mGroceryListModel.setGroceryListAdapter(mGroceryListAdapter);
      mGroceryListModel.setGroceryListActivity(getActivity());
      mGroceryListModel.syncGroceryListData(getActivity());

      // Check to see if we have a frame in which to embed the items
      // fragment directly in the containing UI.
      // View itemsframe = getActivity().findViewById(R.id.frame_groceryitems);
      // mDualPane = itemsframe != null
      // && itemsframe.getVisibility() == View.VISIBLE;

      if (savedState != null)
      {
         // Restore last state for checked position.
         mCurCheckPosition = savedState.getInt("curChoice", 0);
      }

      // if (mDualPane)
      // {
      // // In dual-pane mode, list view highlights selected item.
      // getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      // // Make sure our UI is in the correct state.
      // showGroceryItems(mCurCheckPosition);
      // }
   }

   private ArrayAdapter<GroceryItem> getGroceryListAdapter()
   {
      return new ArrayAdapter<GroceryItem>(getActivity(), R.layout.grocery_list_row,
            mGroceryListModel.getGroceryListArray())
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

   @Override
   public void onSaveInstanceState(Bundle outState)
   {
      super.onSaveInstanceState(outState);
      outState.putInt("curChoice", mCurCheckPosition);
   }

   //
   // @Override
   // public void onListItemClick(ListView l, View v, int pos, long id)
   // {
   // showGroceryItems(pos);
   // }

   /**
    * Helper function to show the details of a selected item, either by
    * displaying a fragment in-place in the current UI, or starting a whole new
    * activity in which it is displayed.
    */
   // void showGroceryItems(int index)
   // {
   // mCurCheckPosition = index;
   //
   // if (mDualPane)
   // {
   // // We can display everything in-place with fragments.
   // // Have the list highlight this item and show the data.
   // getListView().setItemChecked(index, true);
   //
   // // Check what fragment is shown, replace if needed.
   // GroceryItemsFragment groceryitems = (GroceryItemsFragment)
   // getFragmentManager()
   // .findFragmentById(R.id.frame_groceryitems);
   // if (groceryitems == null || groceryitems.getShownIndex() != index)
   // {
   // // Make new fragment to show this selection.
   // groceryitems = GroceryItemsFragment.newInstance(index);
   //
   // // Execute a transaction, replacing any existing
   // // fragment with this one inside the frame.
   // FragmentTransaction ft = getFragmentManager().beginTransaction();
   // ft.replace(R.id.frame_groceryitems, groceryitems);
   // ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
   // ft.commit();
   // }
   //
   // }
   // else
   // {
   // // Otherwise we need to launch a new activity to display
   // // the dialog fragment with selected text.
   // Intent intent = new Intent();
   // // TODO: CHANGE THIS
   // intent.setClass(getActivity(), HomeActivity.class);
   // intent.putExtra("index", index);
   // startActivity(intent);
   // }
   // }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      inflater.inflate(R.menu.grocery_list_menu, menu);
      final MenuItem refresh = (MenuItem) menu.findItem(R.id.grocery_list_menu_refresh);
      refresh.setOnMenuItemClickListener(new OnMenuItemClickListener()
      {
         public boolean onMenuItemClick(MenuItem item)
         {
            mGroceryListModel.syncGroceryListData(getActivity());
            return false;
         }
      });
      super.onCreateOptionsMenu(menu, inflater);
   }
}
