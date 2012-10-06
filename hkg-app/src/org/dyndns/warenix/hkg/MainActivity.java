package org.dyndns.warenix.hkg;

import org.dyndns.warenix.abs.activity.ABSActionbarActivity;
import org.dyndns.warenix.abs.activity.SwitchPageAdapter;
import org.dyndns.warenix.hkg.HKGTopicFragment.HKGThreadListener;
import org.dyndns.warenix.lab.hkg.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

public class MainActivity extends ABSActionbarActivity implements
		HKGThreadListener {

	static final String TAG = "Main";

	SwitchPageAdapter mAdapter;

	/**
	 * Currently displayed thread
	 */
	HKGThread mThread;

	int mCurrentTopicPage = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showTopic("BW", mCurrentTopicPage);
	}

	@Override
	public boolean onNavigationItemChanged(int itemPosition, long itemId) {
		Log.d(TAG, "onNavigationItemChanged(), switch page itemPosition "
				+ itemPosition);

		int newPageCount = itemPosition + 1;
		Toast.makeText(getApplicationContext(), "new page" + newPageCount,
				Toast.LENGTH_SHORT).show();

		HKGThreadFragment f = (HKGThreadFragment) getSupportFragmentManager()
				.findFragmentById(R.id.container);
		f.switchPage(mThread.mThreadId, itemPosition + 1);
		return true;
	}

	void showTopic(String type, int pageNo) {
		HKGTopicFragment f = HKGTopicFragment.newInstance(type, pageNo);
		f.setHKGThreadListener(this);
		FragmentTransaction ft = this.getSupportFragmentManager()
				.beginTransaction();
		ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right,
				R.anim.slide_in_left, R.anim.slide_out_right);

		ft.replace(R.id.container, f);
		ft.commit();

	}

	void setPageSwitcher(HKGThread thread) {
		Log.d(TAG, "setPageSwitcher(), update navigation list item");
		setSwitchThreadPageAdapter(thread.mTitle, thread.mPageCount);
	}

	@Override
	public void onHKGThreadSelected(HKGThread thread) {
		mThread = thread;

		HKGThreadFragment f = HKGThreadFragment.newInstance(thread, 1);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setCustomAnimations(0, R.anim.slide_out_right, 0,
				R.anim.slide_out_right);
		ft.add(R.id.container, f);
		ft.addToBackStack(null);
		ft.commit();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem refresh = menu.add("Refresh");
		refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		refresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (mThread == null) {
					showTopic("BW", 1);
				}
				return true;
			}
		});

		MenuItem more = menu.add("More");
		more.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM
				| MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		more.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (mThread == null) {
					showTopic("BW", ++mCurrentTopicPage);
				}
				return true;
			}
		});
		return true;
	}

	public void onBackPressed() {
		super.onBackPressed();

		if (mThread != null) {
			mThread = null;
			mCurrentTopicPage = 1;
			setActionBarList(null, -1);
		}
	}
}
