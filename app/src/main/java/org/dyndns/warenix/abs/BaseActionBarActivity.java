package org.dyndns.warenix.abs;

import android.os.Bundle;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class BaseActionBarActivity extends SherlockFragmentActivity implements
                                                                    ActionBar.OnNavigationListener {

    private int mLastSelectedItemPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setActionBarList(SpinnerAdapter adapter, int selectedIndex) {
        if (adapter == null) {
            mLastSelectedItemPosition = -1;
            getSupportActionBar().setNavigationMode(
                ActionBar.DISPLAY_SHOW_TITLE);
            getSupportActionBar().setListNavigationCallbacks(adapter, this);
        } else {
            getSupportActionBar().setNavigationMode(
                ActionBar.NAVIGATION_MODE_LIST);
            getSupportActionBar().setListNavigationCallbacks(adapter, this);
            getSupportActionBar().setSelectedNavigationItem(selectedIndex);
        }

    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (itemPosition == mLastSelectedItemPosition) {
            // skip when selecting the same index again
            return true;
        }
        mLastSelectedItemPosition = itemPosition;
        return onNavigationItemChanged(itemPosition, itemId);
    }

    /**
     * When navigation item index has changed.
     *
     * @param itemPosition
     * @param itemId
     * @return
     */
    public boolean onNavigationItemChanged(int itemPosition, long itemId) {
        return true;
    }

    public void resetListNavigation() {
        mLastSelectedItemPosition = -1;
    }
}