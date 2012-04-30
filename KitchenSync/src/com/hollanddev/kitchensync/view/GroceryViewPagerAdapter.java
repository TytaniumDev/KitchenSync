
package com.hollanddev.kitchensync.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hollanddev.kitchensync.R;
import com.viewpagerindicator.TitleProvider;

public class GroceryViewPagerAdapter extends FragmentPagerAdapter implements TitleProvider
{
    private String[] titles;

    public GroceryViewPagerAdapter(FragmentManager fm)
    {
        super(fm);
        titles = new String[]
        {};
    }

    public GroceryViewPagerAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        this.titles = new String[]
        {
                context.getResources().getString(R.string.grocery_list_view_add_item_tab_name),
                context.getResources().getString(R.string.grocery_list_view_list_tab_name)
        };
    }

    @Override
    public String getTitle(int position)
    {
        return titles[position];
    }

    @Override
    public int getCount()
    {
        return titles.length;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new GroceryAddItemWrapperFragment();
            case 1:
                return new GroceryListFragment();
            default:
                return new NYIFragment();
        }
    }

    public static class NYIFragment extends Fragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState)
        {
            TextView text = new TextView(getActivity());
            text.setText("Not Yet Implemented");

            LinearLayout layout = new LinearLayout(getActivity());
            layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT));
            layout.addView(text);

            return layout;
        }
    }
}
