
package com.tywholland.kitchensync.view.grocery;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.tywholland.kitchensync.model.grocery.GroceryItem.RecentItems;

import other.com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import roboguice.inject.InjectView;

public class GroceryAddItemFragment extends RoboSherlockFragment
{
    @InjectView(R.id.grocery_add_item_add_button)
    Button mAddToListButton;
    @InjectView(R.id.grocery_add_item_itemname_field)
    EditText mItemName;
    @InjectView(R.id.grocery_add_item_amount_field)
    EditText mAmount;
    @InjectView(R.id.grocery_add_item_store_field)
    EditText mStore;
    @InjectView(R.id.grocery_add_item_category_field)
    EditText mCategory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_add_groceryitem, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        mAddToListButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ContentValues values = new ContentValues();
                values.put(GroceryItems.ITEMNAME, mItemName.getText().toString());
                values.put(GroceryItems.AMOUNT, mAmount.getText().toString());
                values.put(GroceryItems.STORE, mStore.getText().toString());
                values.put(GroceryItems.CATEGORY, mCategory.getText().toString());
                values.put(GroceryItems.ROWINDEX, "");
                getActivity().getContentResolver().insert(GroceryItems.CONTENT_URI, values);
                getActivity().getContentResolver().notifyChange(
                        RecentItems.CONTENT_URI, null);
                // Reset text fields
                mItemName.setText("");
                mAmount.setText("");
                mStore.setText("");
                mCategory.setText("");
            }
        });
    }
}
