package com.tyhollan.kitchensync.view;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsFragment extends SherlockFragment
{
   protected GoogleAnalyticsTracker tracker;
   protected String                 activityId;
   protected String                 fragmentId;

   @Override
   public void onCreate(final Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      this.tracker = GoogleAnalyticsTracker.getInstance();
      this.fragmentId = getClass().getSimpleName();
      this.activityId = getActivity().getClass().getSimpleName();
   }

   @Override
   public void onResume()
   {
      super.onResume();
      this.tracker.trackPageView("/" + this.activityId + "/" + this.fragmentId);
   }
}
