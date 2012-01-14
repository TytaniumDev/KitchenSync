package com.tyhollan.grocerylist.home;

import com.tyhollan.grocerylist.R;
import com.tyhollan.grocerylist.grocery.GroceryActivity;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends Fragment
{
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_dashboard, container);

      root.findViewById(R.id.grocery_list_launcher).setOnClickListener(
            new View.OnClickListener()
            {
               @Override
               public void onClick(View v)
               {
                  startActivity(new Intent(getActivity(), GroceryActivity.class));
               }
            });
      
      
      //TODO: Add recipe book
      return root;
   }
}
