package com.tyhollan.kitchensync.view.grocery;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.tyhollan.kitchensync.R;
import com.tyhollan.kitchensync.view.AnalyticsFragment;

public class GroceryQuickAddItemFragment extends AnalyticsFragment
{
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
   {
      View root = inflater.inflate(R.layout.fragment_quickadd_groceryitem, container, false);

      ListView listView = (ListView) root.findViewById(R.id.grocery_quickadd_listview);
//      listView.setAdapter(new RecentItemsCursorAdapter(getActivity(), getId(), null, null, null));
      return root;
   }

//   private class RecentItemsCursorAdapter extends SimpleCursorAdapter implements Filterable {
//
//      private Context context;
//
//      private int layout;
//
//      public RecentItemsCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to) {
//          super(context, layout, c, from, to);
//          this.context = context;
//          this.layout = layout;
//      }
//
//      @Override
//      public View newView(Context context, Cursor cursor, ViewGroup parent) {
//
//          Cursor c = getCursor();
//
//          final LayoutInflater inflater = LayoutInflater.from(context);
//          View v = inflater.inflate(layout, parent, false);
//
//          int nameCol = c.getColumnIndex(People.NAME);
//
//          String name = c.getString(nameCol);
//
//          /**
//           * Next set the name of the entry.
//           */     
//          TextView name_text = (TextView) v.findViewById(R.id.name_entry);
//          if (name_text != null) {
//              name_text.setText(name);
//          }
//
//          return v;
//      }
//
//      @Override
//      public void bindView(View v, Context context, Cursor c) {
//
//          int nameCol = c.getColumnIndex(People.NAME);
//
//          String name = c.getString(nameCol);
//
//          /**
//           * Next set the name of the entry.
//           */     
//          TextView name_text = (TextView) v.findViewById(R.id.name_entry);
//          if (name_text != null) {
//              name_text.setText(name);
//          }
//      }
//
//      @Override
//      public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
//          if (getFilterQueryProvider() != null) { return getFilterQueryProvider().runQuery(constraint); }
//
//          StringBuilder buffer = null;
//          String[] args = null;
//          if (constraint != null) {
//              buffer = new StringBuilder();
//              buffer.append("UPPER(");
//              buffer.append(People.NAME);
//              buffer.append(") GLOB ?");
//              args = new String[] { constraint.toString().toUpperCase() + "*" };
//          }
//
//          return context.getContentResolver().query(People.CONTENT_URI, null,
//                  buffer == null ? null : buffer.toString(), args, People.NAME + " ASC");
//      }
//  }

}
