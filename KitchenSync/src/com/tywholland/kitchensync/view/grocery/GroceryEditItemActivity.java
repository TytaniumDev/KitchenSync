package com.tywholland.kitchensync.view.grocery;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.tywholland.kitchensync.R;
import com.tywholland.kitchensync.model.grocery.GroceryItem;
import com.tywholland.kitchensync.model.grocery.GroceryItem.GroceryItems;

import other.com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.fragment_add_groceryitem)
public class GroceryEditItemActivity extends RoboSherlockActivity{
    @InjectView(R.id.grocery_add_item_title) TextView mTitle;
    @InjectView(R.id.grocery_add_item_add_button) Button mButton;
    @InjectView(R.id.grocery_add_item_itemname_field) EditText mItemName;
    @InjectView(R.id.grocery_add_item_amount_field) EditText mAmount;
    @InjectView(R.id.grocery_add_item_store_field) EditText mStore;
    @InjectView(R.id.grocery_add_item_category_field) EditText mCategory;
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
        Bundle extras = getIntent().getExtras();
        if(extras != null)
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
        mButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewValues();
                getContentResolver().update(GroceryItems.CONTENT_URI, mNewValues, GroceryItems.GROCERY_ITEM_ID + "=?", new String[] {mNewValues.getAsString(GroceryItems.GROCERY_ITEM_ID)});
                finish();
            }
        });
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
        mNewValues.put(GroceryItems.GROCERY_ITEM_ID, mOldValues.getAsInteger(GroceryItems.GROCERY_ITEM_ID));
        mNewValues.put(GroceryItems.ROWINDEX, mOldValues.getAsString(GroceryItems.ROWINDEX));
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
