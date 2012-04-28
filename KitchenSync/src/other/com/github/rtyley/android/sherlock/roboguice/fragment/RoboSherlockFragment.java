
package other.com.github.rtyley.android.sherlock.roboguice.fragment;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

import roboguice.RoboGuice;

public abstract class RoboSherlockFragment extends SherlockFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RoboGuice.getInjector(getSherlockActivity()).injectMembersWithoutViews(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoboGuice.getInjector(getSherlockActivity()).injectViewMembers(this);
    }
}
