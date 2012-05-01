
package com.hollanddev.kitchensync.view;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.GroceryItem.Categories;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.model.GroceryItem.Stores;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;

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
    AutoCompleteTextView mCategory;

    private GoogleDocsProviderWrapper mContentResolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_add_groceryitem, container, false);
        mContentResolver = ((KitchenSyncApplication) getSherlockActivity().getApplication())
                .getGoogleDocsProviderWrapper();
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
        mStore.setAdapter(getAutoCompleteViewAdapter(Stores.STORE, Stores.CONTENT_URI,
                Stores.ITEM_ID, Stores.FREQUENCY));
        mCategory.setAdapter(getAutoCompleteViewAdapter(Categories.CATEGORY,
                Categories.CONTENT_URI, Categories.ITEM_ID, Categories.FREQUENCY));
        mCategory.setThreshold(0);
    }

    private SimpleCursorAdapter getAutoCompleteViewAdapter(final String columnName,
            final Uri contentURI, final String itemID, final String orderBy)
    {
        String[] uiBindFrom =
        {
                columnName
        };
        int[] uiBindTo =
        {
                android.R.id.text1
        };

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getSherlockActivity()
                .getApplicationContext(),
                R.layout.filter_spinner, null,
                uiBindFrom, uiBindTo, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        // Set the CursorToStringConverter, to provide the labels for the
        // choices to be displayed in the AutoCompleteTextView.
        adapter.setCursorToStringConverter(new CursorToStringConverter() {
            public String convertToString(android.database.Cursor cursor) {
                return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
            }
        });

        // Set the FilterQueryProvider, to run queries for choices
        // that match the specified input.
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            public Cursor runQuery(CharSequence constraint) {
                Cursor cursor = mContentResolver.query(contentURI, new String[] {
                        itemID, columnName
                }, columnName + " LIKE ?", new String[] {
                        constraint + "%"
                }, orderBy + " DESC");
                return cursor;
            }
        });

        return adapter;
    }

    private void addCurrentItem()
    {
        // Make values
        ContentValues values = makeContentValuesFromViews();
        // Insert new grocery item
        mContentResolver.insert(GroceryItems.CONTENT_URI, values);
        // Update recent items so it doesn't show newly added item
        getSherlockActivity().getContentResolver().notifyChange(
                RecentItems.CONTENT_URI, null);
        // Reset text fields
        mItemName.setText("");
        mAmount.setText("");
        mStore.setText("");
        mCategory.setText("");
    }

    private ContentValues makeContentValuesFromViews()
    {
        ContentValues values = new ContentValues();
        values.put(GroceryItems.ITEMNAME, mItemName.getText().toString());
        values.put(GroceryItems.AMOUNT, mAmount.getText().toString());
        values.put(GroceryItems.STORE, mStore.getText().toString());
        values.put(GroceryItems.CATEGORY, mCategory.getText().toString());
        values.put(GroceryItems.ROWINDEX, "");
        return values;
    }
}
