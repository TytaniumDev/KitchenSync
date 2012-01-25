package com.tyhollan.grocerylist.view.grocery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.tyhollan.grocerylist.R;
import com.tyhollan.grocerylist.view.home.HomeActivity;

public class GroceryListFragment extends ListFragment
{
   boolean mDualPane;
   int     mCurCheckPosition = 0;

   @Override
   public void onActivityCreated(Bundle savedState)
   {
      super.onActivityCreated(savedState);

      // Populate list with our static array of titles.
      //Hook up to database
//      setListAdapter(new ArrayAdapter<String>(getActivity(),
//            R.layout.simple_list_item_checkable_1, Shakespeare.TITLES));

      // Check to see if we have a frame in which to embed the items
      // fragment directly in the containing UI.
      View itemsframe = getActivity().findViewById(R.id.frame_groceryitems);
      mDualPane = itemsframe != null
            && itemsframe.getVisibility() == View.VISIBLE;

      if (savedState != null)
      {
         // Restore last state for checked position.
         mCurCheckPosition = savedState.getInt("curChoice", 0);
      }

      if (mDualPane)
      {
         // In dual-pane mode, list view highlights selected item.
         getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         // Make sure our UI is in the correct state.
         showGroceryItems(mCurCheckPosition);
      }
   }

   @Override
   public void onSaveInstanceState(Bundle outState)
   {
      super.onSaveInstanceState(outState);
      outState.putInt("curChoice", mCurCheckPosition);
   }

   @Override
   public void onListItemClick(ListView l, View v, int pos, long id)
   {
      showGroceryItems(pos);
   }

   /**
    * Helper function to show the details of a selected item, either by
    * displaying a fragment in-place in the current UI, or starting a whole new
    * activity in which it is displayed.
    */
   void showGroceryItems(int index)
   {
      mCurCheckPosition = index;

      if (mDualPane)
      {
         // We can display everything in-place with fragments.
         // Have the list highlight this item and show the data.
         getListView().setItemChecked(index, true);

         // Check what fragment is shown, replace if needed.
         GroceryItemsFragment groceryitems = (GroceryItemsFragment) getFragmentManager()
               .findFragmentById(R.id.frame_groceryitems);
         if (groceryitems == null || groceryitems.getShownIndex() != index)
         {
            // Make new fragment to show this selection.
            groceryitems = GroceryItemsFragment.newInstance(index);

            // Execute a transaction, replacing any existing
            // fragment with this one inside the frame.
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.frame_groceryitems, groceryitems);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
         }

      }
      else
      {
         // Otherwise we need to launch a new activity to display
         // the dialog fragment with selected text.
         Intent intent = new Intent();
         //TODO: CHANGE THIS
         intent.setClass(getActivity(), HomeActivity.class);
         intent.putExtra("index", index);
         startActivity(intent);
      }
   }
}
