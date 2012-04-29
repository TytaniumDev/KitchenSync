
package com.hollanddev.kitchensync.view.grocery;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.view.MenuItem;
import com.hollanddev.kitchensync.model.KitchenSyncApplication;
import com.hollanddev.kitchensync.model.grocery.GroceryItem;
import com.hollanddev.kitchensync.model.grocery.GroceryItem.GroceryItems;
import com.hollanddev.kitchensync.view.CustomAutoCompleteTextView;
import com.hollanddev.kitchensync.R;

import other.com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_edit_item)
public class GroceryEditItemActivity extends RoboSherlockActivity {
    @InjectView(R.id.grocery_add_item_title)
    TextView mTitle;
    @InjectView(R.id.grocery_add_item_add_button)
    Button mButton;
    @InjectView(R.id.grocery_add_item_itemname_field)
    EditText mItemName;
    @InjectView(R.id.grocery_add_item_amount_field)
    EditText mAmount;
    @InjectView(R.id.grocery_add_item_store_field)
    CustomAutoCompleteTextView mStore;
    @InjectView(R.id.grocery_add_item_category_field)
    EditText mCategory;
    private ContentValues mOldValues;
    private ContentValues mNewValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle.setText(R.string.grocery_edit_item);
        mButton.setText(R.string.save);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.grocery_list_icon);
        setupCustomEditViews();
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
        ((KitchenSyncApplication) getApplication())
                .getGoogleDocsProviderWrapper().update(GroceryItems.CONTENT_URI, mNewValues,
                        GroceryItems.ITEM_ID + "=?", new String[] {
                            mNewValues.getAsString(GroceryItems.ITEM_ID)
                        });
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
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.filter_spinner);
        adapter.add("Ralph's");
        adapter.add("Costco");
        adapter.add("Vons");
        adapter.add("Trader Joe's");
        mStore.setAdapter(adapter);
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
