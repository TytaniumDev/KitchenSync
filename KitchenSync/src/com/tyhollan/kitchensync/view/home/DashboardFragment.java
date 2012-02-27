package com.tyhollan.kitchensync.view.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.AnalyticsFragment;
import com.tyhollan.kitchensync.view.grocery.GroceryActivity;

public class DashboardFragment extends AnalyticsFragment
{
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_dashboard, container);
      final FragmentActivity activity = getActivity();

      root.findViewById(R.id.grocery_list_launcher).setOnClickListener(
            new View.OnClickListener()
            {
               @Override
               public void onClick(View v)
               {
                  startActivity(new Intent(getActivity(), GroceryActivity.class));
                  activity.overridePendingTransition(R.anim.slide_to_top_enter, R.anim.slide_to_top_exit);
               }
            });
      
      //TODO: Add recipe book
      return root;
   }
}
