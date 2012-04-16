
package com.tywholland.kitchensync.view.grocery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.tywholland.kitchensync.R;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import other.com.tekle.oss.android.animation.AnimationFactory;
import other.com.tekle.oss.android.animation.AnimationFactory.FlipDirection;
import roboguice.inject.InjectView;

public class GroceryAddItemWrapperFragment extends RoboSherlockFragment
{
    @InjectView(R.id.add_item_viewflipper)
    ViewFlipper viewFlipper;

    private ViewAnimator viewAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_add_wrapper, container, false);
        viewAnimator = (ViewAnimator) view.findViewById(R.id.add_item_viewflipper);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.grocery_additem_menu, menu);
        final MenuItem switchViews = (MenuItem) menu.findItem(R.id.grocery_additem_menu_switch);

        switchViews.setOnMenuItemClickListener(new OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                AnimationFactory.flipTransition(viewAnimator, FlipDirection.LEFT_RIGHT);
                // Switch switchViews button text based on what view is
                // currently
                // visible
                if (viewFlipper.getCurrentView().getId() == R.id.add_item_normal_add)
                {
                    // On Normal Add screen, switches to Quick Add
                    switchViews.setTitle(R.string.switch_to_quick_add);
                }
                else
                {
                    // On Quick Add screen, switches to Normal Add
                    switchViews.setTitle(R.string.switch_to_add);
                }
                // viewFlipper.showNext();
                return true;
            }
        });
    }
}
