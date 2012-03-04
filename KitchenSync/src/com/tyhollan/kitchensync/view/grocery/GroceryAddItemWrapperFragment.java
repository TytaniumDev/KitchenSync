package com.tyhollan.kitchensync.view.grocery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.AnalyticsFragment;
import com.tyhollan.kitchensync.view.DisplayNextView;
import com.tyhollan.kitchensync.view.Flip3dAnimation;

public class GroceryAddItemWrapperFragment extends AnalyticsFragment
{
   private RelativeLayout normalAdd;
   private RelativeLayout quickAdd;

   private boolean        isNormalAdd;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_add_wrapper, container, false);
      setHasOptionsMenu(true);
      normalAdd = (RelativeLayout) root.findViewById(R.id.add_groceryitem_frame);
      quickAdd = (RelativeLayout) root.findViewById(R.id.quickadd_groceryitem_frame);
      isNormalAdd = true;
      quickAdd.setVisibility(View.GONE);

      return root;
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
   {
      Log.i("AddItemWrapper", "Inside additem createoptionsmenu");
      inflater.inflate(R.menu.grocery_additem_menu, menu);
      super.onCreateOptionsMenu(menu, inflater);
      final MenuItem switchViews = (MenuItem) menu.findItem(R.id.grocery_additem_menu_switch);
      switchViews.setOnMenuItemClickListener(new OnMenuItemClickListener()
      {
         public boolean onMenuItemClick(MenuItem item)
         {
            if (isNormalAdd)
            {
               applyRotation(0, 90);
               isNormalAdd = !isNormalAdd;
            }
            else
            {
               applyRotation(0, -90);
               isNormalAdd = !isNormalAdd;
            }
            return true;
         }
      });
   }

   private void applyRotation(float start, float end)
   {
      // Find the center of image
      final float centerX = normalAdd.getWidth() / 2.0f;
      final float centerY = 0;

      // Create a new 3D rotation with the supplied parameter
      // The animation listener is used to trigger the next animation
      final Flip3dAnimation rotation = new Flip3dAnimation(start, end, centerX, centerY);
      rotation.setDuration(500);
      rotation.setFillAfter(true);
      rotation.setInterpolator(new AccelerateInterpolator());
      rotation.setAnimationListener(new DisplayNextView(isNormalAdd, normalAdd, quickAdd));

      if (isNormalAdd)
      {
         normalAdd.startAnimation(rotation);
      }
      else
      {
         quickAdd.startAnimation(rotation);
      }

   }
}
