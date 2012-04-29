
package com.hollanddev.kitchensync.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.AutoCompleteTextView;

public class CustomAutoCompleteTextView extends AutoCompleteTextView {
    private String previous = "";
    private String seperator = ",";

    public CustomAutoCompleteTextView(final Context context, final AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
        this.setThreshold(0);
    }

    public CustomAutoCompleteTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.setThreshold(0);
    }

    public CustomAutoCompleteTextView(final Context context) {
        super(context);
        this.setThreshold(0);
    }

    /**
     * This method filters out the existing text till the separator and launched
     * the filtering process again
     */
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        String filterText = text.toString().trim();
        previous = filterText.substring(0, filterText.lastIndexOf(getSeperator()) + 1);
        filterText = filterText.substring(filterText.lastIndexOf(getSeperator()) + 1).trim();
        Log.i("CustomAuto", "Previous: " + previous + " and filter is: " + filterText);
        super.performFiltering(filterText, keyCode);
    }

    /**
     * After a selection, capture the new value and append to the existing text
     */
    @Override
    protected void replaceText(final CharSequence text) {
        super.replaceText(previous + " " + text);
    }

    public String getSeperator() {
        return seperator;
    }

    public void setSeperator(final String seperator) {
        this.seperator = seperator;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            performFiltering(getText(), 0);
        }
    }

    @Override
    public boolean enoughToFilter() {
        return true;
    }
}
