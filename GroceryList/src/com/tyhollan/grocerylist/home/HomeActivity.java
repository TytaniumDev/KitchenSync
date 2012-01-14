package com.tyhollan.grocerylist.home;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.tyhollan.grocerylist.R;

public class HomeActivity extends FragmentActivity
{
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_home);
   }
}
