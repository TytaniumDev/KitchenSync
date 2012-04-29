
package com.hollanddev.kitchensync.view.grocery;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.view.CustomAutoCompleteTextView;
import com.hollanddev.kitchensync.R;

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
    CustomAutoCompleteTextView mStore;
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
        setupCustomEditViews();
        mCategory.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                    addCurrentItem();
                    return true;
                }
                return false;
            }
        });
        mAddToListButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                addCurrentItem();
            }
        });
    }
    
    private void setupCustomEditViews()
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getSherlockActivity(), R.layout.filter_spinner);
        adapter.add("Ralph's");
        adapter.add("Costco");
        adapter.add("Vons");
        adapter.add("Trader Joe's");
        mStore.setAdapter(adapter);
    }
    
    private void addCurrentItem()
    {
        ContentValues values = new ContentValues();
        values.put(GroceryItems.ITEMNAME, mItemName.getText().toString());
        values.put(GroceryItems.AMOUNT, mAmount.getText().toString());
        values.put(GroceryItems.STORE, mStore.getText().toString());
        values.put(GroceryItems.CATEGORY, mCategory.getText().toString());
        values.put(GroceryItems.ROWINDEX, "");
        ((KitchenSyncApplication) getSherlockActivity().getApplication())
        .getGoogleDocsProviderWrapper().insert(GroceryItems.CONTENT_URI, values);
        getSherlockActivity().getContentResolver().notifyChange(
                RecentItems.CONTENT_URI, null);
        // Reset text fields
        mItemName.setText("");
        mAmount.setText("");
        mStore.setText("");
        mCategory.setText("");
    }
}
