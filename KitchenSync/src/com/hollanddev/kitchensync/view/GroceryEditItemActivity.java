
package com.hollanddev.kitchensync.view;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.CursorToStringConverter;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.hollanddev.kitchensync.R;
import com.hollanddev.kitchensync.model.GroceryItem;
import com.hollanddev.kitchensync.model.GroceryItem.Categories;
import com.hollanddev.kitchensync.model.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.model.GroceryItem.RecentItems;
import com.hollanddev.kitchensync.model.GroceryItem.Stores;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.providers.GoogleDocsProviderWrapper;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_edit_item)
public class GroceryEditItemActivity extends RoboSherlockActivity {
    @InjectView(R.id.grocery_add_item_add_button)
    Button mButton;
    @InjectView(R.id.grocery_add_item_itemname_field)
    AutoCompleteTextView mItemName;
    @InjectView(R.id.grocery_add_item_amount_field)
    EditText mAmount;
    @InjectView(R.id.grocery_add_item_store_field)
    CustomAutoCompleteTextView mStore;
    @InjectView(R.id.grocery_add_item_category_field)
    AutoCompleteTextView mCategory;
    @InjectView(R.id.grocery_add_item_store_field_button)
    ImageButton mStoreButton;
    @InjectView(R.id.grocery_add_item_category_field_button)
    ImageButton mCategoryButton;
    private ContentValues mOldValues;
    private ContentValues mNewValues;
    private GoogleDocsProviderWrapper mContentResolver;
    private SimpleCursorAdapter mStoreAdapter;
    private SimpleCursorAdapter mCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentResolver = ((KitchenSyncApplication) getApplication())
                .getGoogleDocsProviderWrapper();
        mButton.setText(R.string.save);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.grocery_list_icon);
        setupCustomEditViews();
        setupStoreButton();
        setupCategoryButton();
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            mOldValues = extras.getParcelable(GroceryItem.CONTENT_VALUES);
            mNewValues = new ContentValues();
            setFields();
        }
        else
        {
            Toast.makeText(this, "ERROR: Didn't receive values", Toast.LENGTH_LONG).show();
            finish();
        }
        mCategory.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.clearFocus();
                    saveCurrentItem();
                    return true;
                }
                return false;
            }
        });
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentItem();
            }
        });
    }

    private void saveCurrentItem()
    {
        getNewValues();
        mContentResolver.update(GroceryItems.CONTENT_URI, mNewValues,
                GroceryItems.ITEM_ID + "=?", new String[] {
                    mNewValues.getAsString(GroceryItems.ITEM_ID)
                });
        // Update recent items so it doesn't show newly added item
        getContentResolver().notifyChange(
                RecentItems.CONTENT_URI, null);
        // Insert/increment values in stores and categories
        finish();
    }

    private void setFields()
    {
        mItemName.setText(mOldValues.getAsString(GroceryItems.ITEMNAME));
        mAmount.setText(mOldValues.getAsString(GroceryItems.AMOUNT));
        mStore.setText(mOldValues.getAsString(GroceryItems.STORE));
        mCategory.setText(mOldValues.getAsString(GroceryItems.CATEGORY));
    }

    private void getNewValues()
    {
        mNewValues.put(GroceryItems.ITEMNAME, mItemName.getText().toString());
        mNewValues.put(GroceryItems.AMOUNT, mAmount.getText().toString());
        mNewValues.put(GroceryItems.STORE, mStore.getText().toString());
        mNewValues.put(GroceryItems.CATEGORY, mCategory.getText().toString());
        mNewValues.put(GroceryItems.ITEM_ID, mOldValues.getAsInteger(GroceryItems.ITEM_ID));
        mNewValues.put(GroceryItems.ROWINDEX, mOldValues.getAsString(GroceryItems.ROWINDEX));
    }

    private void setupCustomEditViews()
    {
        mStore.setAdapter(getAutoCompleteViewAdapter(Stores.STORE, Stores.CONTENT_URI,
                Stores.ITEM_ID, Stores.FREQUENCY));
        mCategory.setAdapter(getAutoCompleteViewAdapter(Categories.CATEGORY,
                Categories.CONTENT_URI, Categories.ITEM_ID, Categories.FREQUENCY));
        mCategory.setThreshold(0);
        mItemName.setAdapter(getAutoCompleteViewAdapter(RecentItems.ITEMNAME,
                RecentItems.CONTENT_URI, RecentItems.ITEM_ID, RecentItems.FREQUENCY));
        mItemName.setThreshold(0);
    }
    
    private void setupStoreButton()
    {
        // Build alert dialog for stores
        final AlertDialog.Builder storeBuilder = new AlertDialog.Builder(this);
        storeBuilder.setTitle(R.string.grocery_store_selector);
        mStoreAdapter = new SimpleCursorAdapter(this,
                R.layout.filter_spinner, getStoreCursor(), new String[] {
                        Stores.STORE
                }, new int[] {
                        android.R.id.text1
                }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mStoreAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        storeBuilder.setAdapter(mStoreAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor cursor = (Cursor) mStoreAdapter.getItem(which);
                String storeName = cursor.getString(cursor.getColumnIndexOrThrow(Stores.STORE));
                String currentText = mStore.getText().toString();
                if (currentText.length() > 0)
                {
                    mStore.setText(currentText + ", " + storeName);
                }
                else
                {
                    mStore.setText(storeName);
                }
            }
        });
        final AlertDialog storeAlert = storeBuilder.create();
        storeAlert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mStoreButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mStoreAdapter.changeCursor(getStoreCursor());
                storeAlert.show();
            }
        });
    }

    private Cursor getStoreCursor()
    {
        return mContentResolver.query(Stores.CONTENT_URI,
                new String[] {
                        Stores.ITEM_ID, Stores.STORE
                }, null, null, Stores.FREQUENCY + " DESC");
    }

    private void setupCategoryButton()
    {
        // Build alert dialog for categories
        final AlertDialog.Builder categoryBuilder = new AlertDialog.Builder(this);
        categoryBuilder.setTitle(R.string.grocery_category_selector);
        mCategoryAdapter = new SimpleCursorAdapter(this,
                R.layout.filter_spinner, getCategoryCursor(), new String[] {
                        Categories.CATEGORY
                }, new int[] {
                        android.R.id.text1
                }, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryBuilder.setAdapter(mCategoryAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Cursor cursor = (Cursor) mCategoryAdapter.getItem(which);
                String category = cursor.getString(cursor
                        .getColumnIndexOrThrow(Categories.CATEGORY));
                mCategory.setText(category);
            }
        });
        final AlertDialog categoryAlert = categoryBuilder.create();
        categoryAlert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mCategoryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCategoryAdapter.changeCursor(getCategoryCursor());
                categoryAlert.show();
            }
        });
    }

    private Cursor getCategoryCursor()
    {
        return mContentResolver.query(Categories.CONTENT_URI,
                new String[] {
                        Categories.ITEM_ID, Categories.CATEGORY
                }, null, null, Categories.FREQUENCY + " DESC");
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

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(),
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                // app icon in action bar clicked; go back to grocery list
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
