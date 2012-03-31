package com.tywholland.kitchensync.view.home;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.view.grocery.GroceryActivity;

public class DashboardFragment extends RoboSherlockFragment
{
   @InjectView(R.id.grocery_list_launcher)
   Button groceryLauncher;

   @InjectView(R.id.recipe_book_launcher)
   Button recipeLauncher;

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      return inflater.inflate(R.layout.fragment_dashboard, container);
   }

   @Override
   public void onViewCreated(View view, Bundle savedInstanceState)
   {
      super.onViewCreated(view, savedInstanceState);
      groceryLauncher.setOnClickListener(new OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            startActivity(new Intent(getActivity(), GroceryActivity.class));
            getActivity().overridePendingTransition(R.anim.slide_to_top_enter, R.anim.slide_to_top_exit);
         }
      });

      recipeLauncher.setOnClickListener(new OnClickListener()
      {

         @Override
         public void onClick(View v)
         {
            showNYIAlert(getActivity());
         }
      });
   }

   private static void showNYIAlert(Context context)
   {
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setMessage("Not yet implemented").setPositiveButton("OK", new DialogInterface.OnClickListener()
      {

         @Override
         public void onClick(DialogInterface dialog, int which)
         {
            dialog.cancel();
         }
      });
      builder.show();
   }
}
