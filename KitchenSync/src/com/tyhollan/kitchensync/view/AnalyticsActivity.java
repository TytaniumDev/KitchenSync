package com.tyhollan.kitchensync.view;

import android.content.Context;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsActivity extends SherlockFragmentActivity {
   protected static final String TRACKER_ID = "UA-29319402-1";
   protected static final boolean DISPATCH_MANUAL = false;
   protected static final int DISPATCH_INTERVAL = 20;
   protected static final boolean DEBUG = false; // If true stores analytics requests in the log cat
   protected static final boolean DRY_RUN = false; // If true only stores data locally (doesn't send data to analytics)

   @SuppressWarnings("unused")
   private static final int SCOPE_VISITOR = 1; // Call the first time your application is run on a device. Useful for anything that won't change during the lifetime of the installation of the application (app version, lite vs full, type of phone).
    
   @SuppressWarnings("unused")
   private static final int SCOPE_SESSION = 2; // Call at the beginning of an Activity. Applies to all pageviews and events for the lifecycle of the activity.
    
   @SuppressWarnings("unused")
   private static final int SCOPE_PAGE = 3; // Call before trackEvent or trackPageView that the custom variable should apply to.
    
   @SuppressWarnings("unused")
   private static final int SLOT_1 = 1;
    
   @SuppressWarnings("unused")
   private static final int SLOT_2 = 2;
    
   @SuppressWarnings("unused")
   private static final int SLOT_3 = 3;
    
   @SuppressWarnings("unused")
   private static final int SLOT_4 = 4;
    
   @SuppressWarnings("unused")
   private static final int SLOT_5 = 5;
    
   private GoogleAnalyticsTracker tracker;
   private String pageId;

   @Override
   protected void onCreate(final Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);

       this.pageId = getClass().getSimpleName();
       this.tracker = GoogleAnalyticsTracker.getInstance();
        
       if (DISPATCH_MANUAL) {
           this.tracker.startNewSession(TRACKER_ID, this);
       } else {
           this.tracker.startNewSession(TRACKER_ID, DISPATCH_INTERVAL, getApplicationContext());
       }
       this.tracker.setDebug(DEBUG);
       this.tracker.setDryRun(DRY_RUN);
   }

   @Override
   protected void onResume() {
       super.onResume();

       this.tracker.trackPageView("/" + this.pageId);
   }

   @Override
   protected void onDestroy() {
       super.onDestroy();
       this.tracker.dispatch();
       this.tracker.stopSession();
   }

   public void dispatch() {
       if (this.tracker != null) {
           this.tracker.dispatch();
       }
   }

   public static void dispatch(final Context baseContext) {
       final GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
       tracker.startNewSession(TRACKER_ID, baseContext);
       tracker.dispatch();
       tracker.stopSession();
   }

   protected void trackPage(final String page) {
       this.tracker.trackPageView(page);
   }

   protected void trackEvent(final String category, final String action, final String label, final int value) {
       this.tracker.trackEvent(category, action, label, value);
   }

   protected void trackEvent(final String category, final String action, final String label) {
       trackEvent(category, action, label, 0);
   }

   protected void trackEvent(final String action, final String label, final int value) {
       this.tracker.trackEvent(this.pageId, action, label, value);
   }

   protected void trackEvent(final String action, final String label) {
       trackEvent(this.pageId, action, label, 0);
   }

   protected void setCustomVar(final int slot, final String name, final String value, final int scope) {
       this.tracker.setCustomVar(slot, name, value, scope);
   }

   protected void setCustomVar(final int slot, final String name, final String value) {
       this.tracker.setCustomVar(slot, name, value);
   }
}