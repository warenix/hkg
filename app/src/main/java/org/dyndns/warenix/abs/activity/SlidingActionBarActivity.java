package org.dyndns.warenix.abs.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.dyndns.warenix.hkg.R;

public abstract class SlidingActionBarActivity extends ABSActionbarActivity {
    private int mTitleRes;
    protected Fragment mFrag;

    public SlidingActionBarActivity(int titleRes) {
        mTitleRes = titleRes;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(mTitleRes);

        // set the Behind View
        setBehindContentView(R.layout.menu_frame);
        FragmentTransaction t = this.getSupportFragmentManager()
            .beginTransaction();
        mFrag = getBehindFragment();
        t.replace(R.id.menu_frame, mFrag);
        t.commit();

        // customize the SlidingMenu
        SlidingMenu sm = getSlidingMenu();
        sm.setShadowWidthRes(R.dimen.shadow_width);
        sm.setShadowDrawable(R.drawable.shadow);
        sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);

        // // customize the ActionBar
        // if (Build.VERSION.SDK_INT >= 11) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // }
    }

    public abstract Fragment getBehindFragment();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                toggle();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // public class PagerAdapter extends FragmentPagerAdapter {
    // private List<Fragment> mFragments = new ArrayList<Fragment>();
    // private ViewPager mPager;
    //
    // public PagerAdapter(FragmentManager fm, ViewPager vp) {
    // super(fm);
    // mPager = vp;
    // mPager.setAdapter(this);
    // for (int i = 0; i < 3; i++) {
    // addTab(new SampleListFragment());
    // }
    // }
    //
    // public void addTab(Fragment frag) {
    // mFragments.add(frag);
    // }
    //
    // @Override
    // public Fragment getItem(int position) {
    // return mFragments.get(position);
    // }
    //
    // @Override
    // public int getCount() {
    // return mFragments.size();
    // }
    // }

}
